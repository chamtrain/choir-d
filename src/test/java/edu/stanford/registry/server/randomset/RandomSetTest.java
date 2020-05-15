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

import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.SqlSelect;
import com.github.susom.database.SqlUpdate;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.RandomSetDao;
import edu.stanford.registry.server.export.data.PatientAttribute;
import edu.stanford.registry.shared.RandomSetGroup;
import edu.stanford.registry.shared.RandomSet;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.FakeSiteInfo;
import edu.stanford.registry.tool.RandomSetsCreate;


public class RandomSetTest {
  private static Logger logger = LoggerFactory.getLogger(RandomSetTest.class);
  SiteInfo siteInfo;

  @Mock
  private Database database;
  @Mock
  private Supplier<Database> dbp;
  @Mock
  private SqlUpdate sqlUpdate;
  @Mock
  private SqlSelect sqlSelect;
  @Mock
  private PatientDao patDao;
  @Mock
  private PatientDao.Supplier patDaoSupplier;

  private RandomSetDao.Supplier rsetDaoSupplier;
  private RandomSetDao rsetDao;

  static class MyRandomSetDao extends RandomSetDao {
    public MyRandomSetDao(SiteInfo siteInfo, Database db) {
      super(siteInfo, db);
    }
    public MyRandomSetDao(Long siteId, Database db) {
      super(siteId, db);
    }
    @Override
    public Boolean fetchAndUpdateRandomState(RandomSet rset, String stratName, final RandomSetStateAccessor accessor) {
      return Boolean.valueOf(accessor.shouldUpdate(accessor.getCounter(), accessor.getData()));
    }
    @Override
    protected RandomSetParticipant insertPartOrHist(RandomSetParticipant rsp, boolean initDate, boolean init, String del) {
      return rsp;
    }
    @Override
    protected RandomSetParticipant updateParticipant(RandomSetParticipant rsp) {
      return rsp;
    }

  }


  @Test
  public void testAddKSortBackPainPatient() {
    logger.debug("Short message so logger isn't unused...");
    RandomSetter r = siteInfo.getRandomSet(RandomSetsCreate.TSET_KSort_BackPain);
    add3Patients(r);
  }

  @Test
  public void testAddPureBackPainPatient() {
    RandomSetter r = siteInfo.getRandomSet(RandomSetsCreate.TSET_KSort_BackPain);
    add3Patients(r);
  }

  @Test
  public void testAddKSortMigrainePatient() {
    RandomSetter r = siteInfo.getRandomSet(RandomSetsCreate.TSET_KSort_BackPain);
    add3Patients(r);
  }

  @Test
  public void testAddPureMigrainePatient() {
    RandomSetter r = siteInfo.getRandomSet(RandomSetsCreate.TSET_KSort_BackPain);
    add3Patients(r);
  }


  // utilities

  private void add3Patients(RandomSetter r) {
    when(database.get()).thenReturn(database);
    String patientId = "patientId";
    User admin = new User(1L, "admin", "Admin", 1L, "", true);
    RandomSetParticipant rsp;

    rsp = new RandomSetParticipant(patientId, r.getRandomSet(), RandomSetParticipant.State.Unset);
    rsp.setState(RandomSetParticipant.State.Declined);
    rsp = r.updateParticipant(siteInfo, dbp, admin, rsp);
    Assert.assertTrue(rsp.getGroup().isEmpty());

    rsp = new RandomSetParticipant(patientId, r.getRandomSet(), RandomSetParticipant.State.Unset);
    rsp.setState(RandomSetParticipant.State.Disqualified);
    rsp = r.updateParticipant(siteInfo, dbp, admin, rsp);
    Assert.assertTrue(rsp.getGroup().isEmpty());

    rsp = new RandomSetParticipant(patientId, r.getRandomSet(), RandomSetParticipant.State.Unset);
    rsp.setState(RandomSetParticipant.State.Assigned);
    rsp = r.updateParticipant(siteInfo, dbp, admin, rsp);
    Assert.assertFalse(rsp.getGroup().isEmpty()); // NOT EMPTY
  }

  String group(RandomSetGroup g) {
    return (g == null ? "declined" : g.getGroupName());
  }


  // convoluted setup
  @Before
  public void initializeMocks() {
    MockitoAnnotations.initMocks(this);
    when(database.get()).thenReturn(database);
    when(database.toUpdate(Mockito.anyString())).thenReturn(sqlUpdate);
    when(sqlUpdate.update()).thenReturn(1);
    when(sqlUpdate.argString(Mockito.anyString())).thenReturn(sqlUpdate);
    when(sqlUpdate.argLong(Mockito.anyLong())).thenReturn(sqlUpdate);

    when(database.toSelect(Mockito.anyString())).thenReturn(sqlSelect);
    when(sqlSelect.queryIntegerOrZero()).thenReturn(1);
    when(sqlSelect.argString(Mockito.anyString())).thenReturn(sqlSelect);
    when(sqlSelect.argLong(Mockito.anyLong())).thenReturn(sqlSelect);

    when(dbp.get()).thenReturn(database);

    rsetDao = new MyRandomSetDao(2L, null);
    rsetDaoSupplier = new RandomSetDao.Supplier() {
      @Override public RandomSetDao get(Long siteId, Database db) {
        return rsetDao;
      }
      @Override public RandomSetDao get(SiteInfo siteInfo, Database db) {
        return rsetDao;
      }
    };

    RandomSetKSort.setSuppliers(rsetDaoSupplier, patDaoSupplier);
    RandomSetPure.setSuppliers(rsetDaoSupplier, patDaoSupplier);

    when(patDaoSupplier.get(Mockito.any(), Mockito.any())).thenReturn(patDao);
    when(patDaoSupplier.get(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(patDao);

    when(patDao.insertAttribute(Mockito.any(PatientAttribute.class))).then(AdditionalAnswers.returnsFirstArg());
    siteInfo = new FakeSiteInfo();
  }

  @After
  public void undoStaticSuppliers() {
    rsetDaoSupplier = new RandomSetDao.Supplier();
    patDaoSupplier = new PatientDao.Supplier();
    RandomSetKSort.setSuppliers(rsetDaoSupplier, patDaoSupplier);
    RandomSetPure.setSuppliers(rsetDaoSupplier, patDaoSupplier);
  }
}