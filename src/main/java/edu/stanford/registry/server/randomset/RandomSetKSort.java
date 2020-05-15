/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
 * All Rights Reserved.
 *
 * See the NOTICE and LICENSE files distributed with this work for information
 * regarding copyright ownership and licensing. You may not use this file except
 * in compliance with a written license agreement with Stanford University.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See your
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.stanford.registry.server.randomset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.github.susom.database.Database;

import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.RandomSetDao;
import edu.stanford.registry.shared.RandomSetGroup;
import edu.stanford.registry.shared.RandomSet;
import edu.stanford.registry.shared.RandomSetParticipant;

/**
 * The type of class that adds a patient to a RandomSet.  It implements its own custom algorithm.
 *
 * It creates a string of random assignments for the next block of patients, and assigns them
 * until it needs more.
 */
public class RandomSetKSort extends RandomSetter {
  private final static String ALGORITHM_NAME = "KSort";
  private static Logger logger = LoggerFactory.getLogger(RandomSetKSort.class);
  private static RandomSetDao.Supplier myRsetDaoSupplier = new RandomSetDao.Supplier();
  private static PatientDao.Supplier myPatDaoSupplier = new PatientDao.Supplier();

  private static Supplier<AddPatientAccessor> accessorSupplier;

  String nextAssignments;  // This could be a stack, or a backwards string in a StringBuilder
  // It needs mostly to pop off the front, but occasionally push onto the front, when someone withdraws

  /**
   * Called by RandomSetFactory
   */
  public RandomSetKSort(Long siteId, String name) {
    super(siteId, name, ALGORITHM_NAME, myRsetDaoSupplier, myPatDaoSupplier);
  }


  /**
   * For testing- RandomSetters are created through an initialization process, so these must be initialized at times.
   */
  public static void setSuppliers(RandomSetDao.Supplier testRsetDaoSupplier, PatientDao.Supplier testPatDaoSupplier) {
    myRsetDaoSupplier = testRsetDaoSupplier;
    myPatDaoSupplier = testPatDaoSupplier;
  }


  @Override
  public String getAlgorithm() {
    return ALGORITHM_NAME;
  }


  /**
   * This is the algorithm-specific function called by the superclass.
   */
  @Override
  protected RandomSetGroup addRandomPatient(Database db, String stratumName) {
    int chosenGroupNumber = getNextAssignmentAndUpdateDb(db, stratumName);

    RandomSetGroup chosenGroup = rset.getGroup(chosenGroupNumber);
    if (chosenGroup != null) {
      logger.info("Adding patient to group: "+chosenGroup.getGroupName());
      chosenGroup.addPatient();
    } else {
      logger.info("Adding patient to group: null, chosenGroupNumber="+chosenGroupNumber);
    }
    return chosenGroup;
  }


  private int getNextAssignmentAndUpdateDb(Database db, String stratumName) {
    // This could be a synchronized method, but a multiple-server system will want to lock on the database row.
    // Plus there's a tiny chance a new SiteInfo will create a new RandomSetKSort for this RandomSet and use it
    RandomSetDao rsetDao = rsetDaoSupplier.get(siteId, db);
    AddPatientAccessor acc = (accessorSupplier == null) ? new AddPatientAccessor(rset) : accessorSupplier.get();
    rsetDao.fetchAndUpdateRandomState(rset, stratumName, acc);
    return acc.nextRandomNumber;
  }


  static class AddPatientAccessor extends RandomSetDao.RandomSetStateAccessor {
    final int    numDigits;    // usually < 17 groups, so can use 1 digit, 0..f, else 2
    final String digitFormat;  // usually < 17 groups, so format is %x, else %2x

    private int nextRandomNumber = -1;
    RandomSet rset;

    AddPatientAccessor(RandomSet rset) {
      this.rset = rset;
      numDigits = computeNumDigits(rset);
      digitFormat = "%" + numDigits + "x";
    }

    /**
     * Not part of RandomSetDoa API - this is peeled off from current random-state string from DB
     * @return
     */
    int getNextRandomGroup() {
      return nextRandomNumber;
    }

    @Override
    public boolean shouldUpdate(long counter, String randomData) {
      if (randomData == null || randomData.isEmpty()) {
        ArrayList<RandomSetGroup> list = createRandomList(null);
        randomData = createRandomString(list);
      }
      String firstNumber = randomData.substring(0, numDigits);
      randomData = randomData.substring(numDigits);
      setData(randomData);

      nextRandomNumber = Integer.valueOf(firstNumber, 16);
      return true;
    }


    protected ArrayList<RandomSetGroup> createRandomList(RandomSetGroup plusThis) {
      ArrayList<RandomSetGroup> list = new ArrayList<RandomSetGroup>(rset.getBlockSize()+1);
      if (plusThis != null) {
        list.add(plusThis);
      }

      // Add each group to the list the number of times in its target percentage
      for (RandomSetGroup g: rset.getGroups()) {
        int num = g.getTargetPerBlock();
        for (int i = 0;  i < num;  i++) {
          list.add(g);
        }
      }
      Collections.shuffle(list);
      return list;
    }


    protected String createRandomString(ArrayList<RandomSetGroup> list) {
      StringBuilder sb = new StringBuilder(list.size() * numDigits);
      for (RandomSetGroup g: list) {
        sb.append(String.format(digitFormat, g.getGroupId()));
      }
      return sb.toString();
    }


    static private int computeNumDigits(RandomSet rset) {
      int biggestGroupNumber = rset.getGroups().size() - 1;
      String s = String.format("%x", biggestGroupNumber); // 0..numGroups-1, skip the Declined group
      return s.length();
    }
  }


  @Override
  protected void removeRandomPatient(RandomSetParticipant rsp, Database db) {
    RandomSetGroup oldGroup = rset.getGroup(rsp.getGroup());
    String oldStratum = rsp.getStratumName();
    if (oldGroup == null) {  // just defensive, shouldn't happen
      throw new RuntimeException("For randomset '"+rset.getName()+"' group name was bogus: "+rsp.getGroup());
    }
    undoAssignmentAndUpdateDb(oldGroup, oldStratum, db);
    logger.info("From random group {}/{}, Unassigned patient from group: {}", rset.getName(), rsp.getGroup());
  }


  private void undoAssignmentAndUpdateDb(RandomSetGroup group, String stratumName, Database db) {
    RemovePatientAccessor acc = new RemovePatientAccessor(rset, group);
    RandomSetDao rsetDao = rsetDaoSupplier.get(siteId, db);
    rsetDao.fetchAndUpdateRandomState(rset, stratumName, acc);
  }


  static class RemovePatientAccessor extends AddPatientAccessor {
    private final RandomSetGroup g;
    RemovePatientAccessor(RandomSet rset, RandomSetGroup group) {
      super(rset);
      g = group;
    }

    @Override
    public boolean shouldUpdate(long counter, String randomData) {
      randomData = (randomData == null) ? "" : randomData;
      String putBack = String.format(digitFormat, g.getGroupId());
      setData(putBack + randomData);
      return true;
    }
  }
}
