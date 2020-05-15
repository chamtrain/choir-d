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

import java.util.Random;

import com.github.susom.database.Database;

import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.RandomSetDao;
import edu.stanford.registry.shared.RandomSetGroup;
import edu.stanford.registry.shared.RandomSetParticipant;

/**
 * Just adds a patient to a RandomSet randomly, never worrying about balancing or withdrawals
 *
 * @author rstr
 */
public class RandomSetPure extends RandomSetter {
  private static final String ALGORITHM_NAME = "Pure";
  private static RandomSetDao.Supplier myRsetDaoSupplier = new RandomSetDao.Supplier();
  private static PatientDao.Supplier myPatDaoSupplier = new PatientDao.Supplier();

  /**
   * Used just during tests, then with parent.setSiteInfo(info)
   */
  public RandomSetPure(Long siteId, String name) {
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
  protected RandomSetGroup addRandomPatient(Database dbpUnused, String stratumName) {
    int total = 0;
    for (RandomSetGroup g: rset.getGroups()) {
      if (!g.isClosed()) {
        total += g.getTargetPerBlock();
      }
    }
    Random r = new Random();
    int which = r.nextInt(total);

    int newTotal = 0;
    for (RandomSetGroup g: rset.getGroups()) {
      if (!g.isClosed()) {
        newTotal += g.getTargetPerBlock();
        if (which < newTotal) {
          return g.addPatient();
        }
      }
    }
    throw new AssertionError("Random newTotal="+newTotal+" <= which="+which+" < total="+total+" ???");
  }


  @Override
  public String getAlgorithm() {
    return "Pure";
  }


  @Override
  protected void removeRandomPatient(RandomSetParticipant rsp, Database db) {
    // Does nothing, since there's no state
  }
}
