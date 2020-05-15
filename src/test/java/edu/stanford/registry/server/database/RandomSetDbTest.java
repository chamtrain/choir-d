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
package edu.stanford.registry.server.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SiteDao;
import edu.stanford.registry.server.randomset.RandomSetter;

import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.RandomSetCategory;
import edu.stanford.registry.shared.RandomSet;
import edu.stanford.registry.shared.RandomSet.RSState;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.RandomSetParticipant.State;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import com.github.susom.database.Database;


public class RandomSetDbTest extends DatabaseTestCase {
  private static Logger logger = LoggerFactory.getLogger(RandomSetDbTest.class);
  final String LOCAL = Constants.REGISTRY_SURVEY_SYSTEM_NAME;

  public void testSiteInfoHasRandomSets() {
    Set<Entry<String, RandomSetter>> list = getSiteInfo().getRandomSets();
    assertTrue("Expected a list.size of 2-3, not: "+list.size(), list.size() != 0);
  }

  static final RandomSetParticipant.State UNSET = RandomSetParticipant.State.Unset;

  // The names are ordered by complexity- fix earlier ones first

  public void test00_justSayWhatSitesAreAvailable() {
    ArrayList<SiteInfo> list = new SiteDao(getDatabase()).getSurveySites();
    String s = "(flavor "+getDatabase().flavor()+") loaded";
    String delim = ": ";
    boolean have2 = false;
    for (SiteInfo si: list) {
      String idstr = si.getIdString();
      s += delim + idstr.substring(0, idstr.length()-2);
      delim = ", ";
      have2 |= (si.getSiteId().intValue() == 2);
    }
    assertTrue("Missing site 2! "+s, have2);
  }

  public void test01_Get0PatientParticipantsNoUnassigned() {
    RandomSetDao dao = new RandomSetDao(getSiteInfo(), getDatabase());
    ArrayList<RandomSetParticipant> list = dao.getPatientParticipation("patientId", true, false, null, false);
    assertNotNull("I'm expecting a list of surveys, not null...", list);
    // we've created no participants, so none should be found
    assertTrue("Expected a list.size of 0, not: "+list.size(), list.size() == 0);
  }


  public void test05_shouldBeAtLeast2RandomSets() {
    SiteInfo siteInfo = getSiteInfo();
    RandomSetter rsp1 = getNthRandomSet(siteInfo, 0);
    assertNotNull("1st random set shouldn't be null", rsp1);
    RandomSetter rsp2 = getNthRandomSet(getSiteInfo(), 1);
    assertNotNull("2nd random set shouldn't be null", rsp2);
    String name1 = rsp1.getName();
    assertFalse("The RandomSet names should differ, both are:"+name1, rsp2.getName().equals(name1));
  }


  public void test10_GetPatientParticipationWiUnassigned() {
    RandomSetDao dao = new RandomSetDao(getSiteInfo(), getDatabase());
    ArrayList<RandomSetParticipant> list = dao.getPatientParticipation("not-a-patient", false, false, null, true);
    assertNotNull("I'm expecting a list of surveys, not null...", list);
    logger.debug("The number of participants after adding none, were: "+list.size());
    assertTrue("Expe`cted a list.size of 2 or more, not "+list.size(), list.size() >= 2);
  }


  /**
   * Just does a select
   */
  public void test15_GetTreatmentSetForUIAlwaysGetsUnassignedOpenTrueIs2() {
    ArrayList<RandomSetParticipant> list = getParticipantsForUI("patientId");
    assertNotNull("I'm expecting a list of surveys, not null...", list);
    // we've created no participants, so none should be found
    assertEquals("Expected a list.size of 0, not: "+list.size(), list.size(), 3);
    assertEquals("expect all to be UNSET", 3, this.countStatesInList(list, UNSET));
    assertEquals("expect second list item to be UNSET", list.get(1).getState(), UNSET);
  }


  /**
   * Just does a select
   */
  public void test20_GetTreatmentSetForReporGetsNoUnassigned() {
    ArrayList<RandomSetParticipant> list = getParticipantsForReport("patientId20");
    assertNotNull("I'm expecting a list of surveys, not null...", list);
    // we've created no participants, so none should be found
    assertEquals("Patient assigned to none, so expected none for report", 0, list.size());
  }

  public void test25_InsertParticipant() {
    Database db = getDatabaseSupplier().get();
    int base = db.toSelect("SELECT COUNT(*) FROM randomset_participant_hist").queryIntegerOrZero();

    Patient pat = createPatient(db, "test-mrn-tip", "Clark", "Kent", 30);
    RandomSetParticipant rsp = createParticipant(pat, 0, null);
    assertEquals("After creating participant, ID should be -1", -1L, rsp.getRandomsetParticipantId());
    assertNull("update should return null, since it shouldnt save", updateParticipant(db, rsp));
    assertEquals("After an aborted update, ID should still be -1", -1L, rsp.getRandomsetParticipantId());
    State state = rsp.getState();
    assertEquals("An unspecified State should be Unset: "+state.toString(), state, UNSET);

    rsp.setReason("Need a reason to save it");
    rsp = updateParticipant(db, rsp);
    long id = rsp.getRandomsetParticipantId();
    assertTrue("After insertion, the id should be non-zero="+id, 0 != id);

    int num = db.toSelect("SELECT COUNT(*) FROM randomset_participant_hist").queryIntegerOrZero();
    assertEquals("After inserting, history should have a row", 1+base, num);
  }


  /**
   * Inserts 2 participations for a patient, then fetches them
   */
  public void test30_Get2TreatmentSetParticipants() {
    Database db = getDatabaseSupplier().get();
    Patient pat = createPatient(db, "test-mrn-g2tsp", "Lois", "Lane", 28);

    // Can't insert an Unset participant (unless there's a reason added)
    RandomSetParticipant rsp1 = createInsertedParticipant(db, pat, 0, RandomSetParticipant.State.Declined);
    assertNotNull("State wasnt set to Unset, so returned participant shouldnt be null", rsp1);
    RandomSetParticipant rsp2 = createInsertedParticipant(db, pat, 1, RandomSetParticipant.State.Disqualified);
    assertNotNull("State wasnt set to Unset, so returned participant shouldnt be null", rsp2);

    ArrayList<RandomSetParticipant> list = getParticipantsForUI(pat.getPatientId());
    assertNotNull("I'm expecting a list of surveys, not null...", list);
    // we've created no participants, so none should be found
    assertEquals("Expected a list.size of 2, not: "+list.size(), 3, list.size());
    String name1 = list.get(0).getName();
    String name2 = list.get(1).getName();
    assertFalse("The RandomSet names should differ, both are:"+name1, name1.equals(name2));
  }


  public void test35_UpdatePatientParticipation() {
    Database db = getDatabaseSupplier().get();
    String patientId = "test-mrn-upp";
    Patient pat = createPatient(db, patientId, "Louise", "Loins", 35);

    RandomSetParticipant rsp = createParticipant(pat, 0, RandomSetParticipant.State.Declined);
    String reason = "This is a reason";
    rsp.setReason(reason);
    assertTrue("After setting the reason, it should have a change", rsp.changed());
    rsp = updateParticipant(db, rsp);
    assertFalse("After putting into the database, it should be set to unchanged", rsp.changed());
    assertTrue("And the reason should still be there", reason.equals(rsp.getReason()));
    assertEquals("participant first seq number should be 0", 0, rsp.getUpdateSequence());
    Date date0 = rsp.getUpdateTime();
    assertHist1(db, patientId);

    RandomSetParticipant.State disqualified = RandomSetParticipant.State.Disqualified;
    rsp.setState(disqualified);
    assertTrue("After changing state to Declined, it should have a change", rsp.changed());
    rsp = updateParticipant(db, rsp);
    assertFalse("After putting into the database, it should not have a change", rsp.changed());
    assertTrue("And the state should still be there", rsp.getState().equals(disqualified));
    assertTrue("And the original state should be updated", rsp.getOriginalState().equals(disqualified));
    assertEquals("participant 2nd seq number should be 1", 1, rsp.getUpdateSequence());
    Date date1 = rsp.getUpdateTime();
    assertTrue("participant date should be later", date0.before(date1));
    assertHist2(db, patientId);
  }


  private void assertHist1(Database db, String patientId) {
    ArrayList<RandomSetParticipant> list = new RandomSetDao(getSiteInfo(), db).getParticipationHistory(patientId);
    assertEquals("Expect 1 participant history for "+patientId, 1, list.size());
  }

  private void assertHist2(Database db, String patientId) {
    ArrayList<RandomSetParticipant> list = new RandomSetDao(getSiteInfo(), db).getParticipationHistory(patientId);
    assertEquals("Expect 1 participant history for "+patientId, 2, list.size());
    assertEquals(list.get(0).getUpdateSequence(), 0);
    assertEquals(list.get(1).getUpdateSequence(), 1);
    Date date0 = list.get(0).getUpdateTime();
    Date date1 = list.get(1).getUpdateTime();
    assertTrue(date0.before(date1));
  }



  public void test40_InsertPatientToAssignedGroup() {
    // TODO - and check group counts
  }


  public void test45_UpdatePatientToAssignedGroup() {
    Database db = getDatabaseSupplier().get();
    Patient pat = createPatient(db, "test-mrn-uptag", "Downy", "Upman", 25);

    RandomSetParticipant rsp = createInsertedParticipant(db, pat, 0, RandomSetParticipant.State.Assigned);
    String grp = rsp.getGroup();
    assertFalse("After inserting as Assigned, group shouldnt be empty", grp == null || grp.isEmpty());
    RandomSetParticipant.State st = rsp.getState();
    assertEquals("After inserting, state should be Assigned", RandomSetParticipant.State.Assigned, st);
    assertTrue("After inserting as Assigned, id shoudnt be zero", rsp.getRandomsetParticipantId() > 0);
  }


  public void test50_WithdrawAssignedPatient() {
    Database db = getDatabaseSupplier().get();
    Patient pat = createPatient(db, "test-mrn-wap", "Downy", "Upman", 25);

    RandomSetParticipant rsp = createInsertedParticipant(db, pat, 0, RandomSetParticipant.State.Assigned);
    String grp = rsp.getGroup();
    assertFalse("After inserting as Assigned, group shouldnt be empty", grp == null || grp.isEmpty());
    rsp.setState(RandomSetParticipant.State.Withdrawn);

    rsp = updateParticipant(db, rsp);
    RandomSetParticipant.State st = rsp.getState();
    assertEquals("After updating, state should be Withdrawn", RandomSetParticipant.State.Withdrawn, st);
  }


  static final boolean INClUDES = true;
  static final boolean EXCLUDES = false;
  static final boolean UInotRpt = true;
  static final boolean RPTnotUI = false;

  public void test60_checkUIExcludesResearching() {
    assertListInExcludesRSetState(UInotRpt, RSState.Researching, EXCLUDES);
  }

  public void test61_checkUIExcludesClosed() {
    assertListInExcludesRSetState(UInotRpt, RSState.Closed, EXCLUDES);
  }

  public void test62_checkUIIncludesNotEnrolling() {
    assertListInExcludesRSetState(UInotRpt, RSState.NotEnrolling, INClUDES);
  }


  public void test65_checkUIIncludesAssigned() {
    assertListInExcludesPartState(UInotRpt, State.Completed, INClUDES);
  }

  public void test66_checkUIIncludesWithdrawn() {
    assertListInExcludesPartState(UInotRpt, State.Withdrawn, INClUDES);
  }

  public void test67_checkUIIncludesCompleted() {
    assertListInExcludesPartState(UInotRpt, State.Completed, INClUDES);
  }

  public void test68_checkUIIncludesDeclined() {
    assertListInExcludesPartState(UInotRpt, State.Declined, INClUDES);
  }

  public void test69_checkUIIncludesDisqualified() {
    assertListInExcludesPartState(UInotRpt, State.Disqualified, INClUDES);
  }


  // These test what's shown in reports- don't show participation after data collection is over
  public void test70_checkReportExcludesResearching() {
    assertListInExcludesRSetState(UInotRpt, RSState.Researching, EXCLUDES);
  }

  public void test71_checkReportExcludesClosed() {
    assertListInExcludesRSetState(UInotRpt, RSState.Closed, EXCLUDES);
  }

  public void test72_checkReportIncludesNotEnrolling() {
    assertListInExcludesRSetState(UInotRpt, RSState.NotEnrolling, INClUDES);
  }


  public void test75_checkReportIncludesAssigned() {
    assertListInExcludesPartState(UInotRpt, State.Assigned, INClUDES);
  }

  public void test76_checkReportIncludesWithdrawn() {
    assertListInExcludesPartState(UInotRpt, State.Withdrawn, INClUDES);
  }

  public void test77_checkReportIncludesCompleted() {
    assertListInExcludesPartState(UInotRpt, State.Completed, INClUDES);
  }

  public void test78_checkReportExcludesDeclined() {
    assertListInExcludesPartState(UInotRpt, State.Declined, INClUDES);
  }

  public void test79_checkReportExcludesDisqualified() {
    assertListInExcludesPartState(UInotRpt, State.Disqualified, INClUDES);
  }


  // TODO: Need a bunch of tests around state change...
  public void testUpdateParticipant() {
    /*
    RandomSetDao dao = new RandomSetDao(siteInfo, getDatabaseSupplier());
    updateOrInsertParticipant
    ArrayList<DataTable> list = dao.getSurveySystemsAsDataTables();
    assertNotNull("I'm expecting a list of surveys, not null...", list);
    assertTrue("Expected a list.size of 13 or so, not: "+list.size(), list.size() > 5);
    */
  }


  // ============= utilities

  int countStatesInList(ArrayList<RandomSetParticipant> list, State state) {
    int count = 0;
    for (RandomSetParticipant rsp: list)
      if (rsp.getState().equals(state))
        count++;
    return count;
  }

  RandomSetParticipant createParticipant(Patient patient, int which, RandomSetParticipant.State state) {
    SiteInfo siteInfo = getSiteInfo();
    RandomSetter rsetter = getNthRandomSet(siteInfo, which);
    assertNotNull(siteInfo.getIdString()+"RandomSetter shouldn't be null", rsetter);
    RandomSet rset = rsetter.getRandomSet();
    assertNotNull(siteInfo.getIdString()+"RandomSet shouldn't be null", rset);
    RandomSetParticipant rsp = new RandomSetParticipant(patient.getPatientId(), rset, state);
    assertTrue("Before inserting, ID should be -1", rsp.getRandomsetParticipantId() == -1);
    return rsp;
  }


  RandomSetParticipant createInsertedParticipant(Database db, Patient patient, int which, RandomSetParticipant.State state) {
    RandomSetParticipant rsp = createParticipant(patient, which, (state == null) ? null : UNSET);
    rsp.setState(state);  // must be changed to go into DB, never put an UNSET into DB
    if (state.isAnAssignedState()) {
      RandomSetCategory[] cats = rsp.getRandomSet().getCategories();
      rsp.setStratumName((cats == null) ? RandomSetCategory.NoStratumName : mkStratName(cats, 0, ""));
    }
    return updateParticipant(db, rsp);
  }

  private String mkStratName(RandomSetCategory[] cats, int ix, String result) {
    if (ix == cats.length) {
      return result;
    }
    RandomSetCategory cat = cats[ix];
    result += (result.isEmpty() ? "" : ",") + cat.getName() + "=" + cat.getValues()[0].getName();
    return mkStratName(cats, ix+1, result);
  }

  RandomSetParticipant updateParticipant(Database db, RandomSetParticipant rsp) {
    SiteInfo siteInfo = getSiteInfo();
    RandomSetter setter = siteInfo.getRandomSet(rsp.getName());
    User admin = new User(1L, "admin", "Admin", 1L, "", true);  // 1L is user id (not site id)
    return setter.updateParticipant(siteInfo, db, admin, rsp);
  }


  private RandomSetter getNthRandomSet(SiteInfo siteInfo, int which) {
    Set<Entry<String, RandomSetter>> rsets = siteInfo.getRandomSets();
    assertNotNull(siteInfo.getIdString()+"hash of sites shouldn't be null", rsets);
    assertTrue(siteInfo.getIdString()+"Should have multiple sets in the siteInfo", rsets.size() > 0);
    RandomSetter rsetter = null;
    Iterator<Entry<String, RandomSetter>> iter = rsets.iterator();
    for (int n = 0;  n <= which;  n++) {
      assertTrue("You wanted randomSet["+which+"] but there were only "+n, iter.hasNext());
      rsetter = iter.next().getValue();
    }
    return rsetter;
  }


  Patient createPatient(Database db, String mrn, String first, String last, int age) {
    Patient pat = new Patient(mrn, "Clark", "Kent", yearsAgo(age)); // in June, 1970
    pat = new PatientDao(db, getSiteInfo().getSiteId()).addPatient(pat);
    assertNotNull(pat);
    return pat;
  }


  Date yearsAgo(int n) {
    n = (n > 46) ? 46 : n;  // can the number go negative?
    return new Date(System.currentTimeMillis() - n*(365L * 24 * 3600 * 1000));
  }

  ArrayList<RandomSetParticipant> getParticipantsForUI(String patientId) {
    RandomSetDao dao = new RandomSetDao(getSiteInfo(), getDatabase());
    return dao.getTreatmentSetParticipantsForUi(patientId);
  }

  ArrayList<RandomSetParticipant> getParticipantsForReport(String patientId) {
    RandomSetDao dao = new RandomSetDao(getSiteInfo(), getDatabase());
    return dao.getTreatmentSetParticipantsForReport(patientId);
  }


  private void assertListInExcludesRSetState(boolean uiOrRpt, RSState rsState, boolean includes) {
    int whichRSet = 0;
    RandomSet rset = getNthRandomSet(getSiteInfo(), whichRSet).getRandomSet();
    RSState savedState = rset.getState();
    rset.setState(rsState.toString());

    Database db = getDatabaseSupplier().get();
    Patient pat = createPatient(db, "test-in/exclude", "Downy", "Upman", 25);
    createInsertedParticipant(db, pat, whichRSet, RandomSetParticipant.State.Assigned);

    try {
      long expectedNum = (includes ? 1 : 0);
      expectedNum += (uiOrRpt) ? 2 : 0;  // UI should include not-assigned reports
      String msg = String.format("For %s, RandomSet state=%s", uiOrRpt ? "UI" : "Report", rsState.toString());
      if (uiOrRpt) {
        ArrayList<RandomSetParticipant> list = getParticipantsForUI(pat.getPatientId());
        Assert.assertEquals(msg, expectedNum, list.size());
      } else {
        ArrayList<RandomSetParticipant> list = getParticipantsForReport(pat.getPatientId());
        Assert.assertEquals(msg, expectedNum, list.size());
      }
    } finally {
      rset.setState(savedState.toString());
    }
  }


  private void assertListInExcludesPartState(boolean uiOrRpt, State rspState, boolean includes) {
    int whichRSet = 0;  // check this state is Enrolling
    RSState rsState = getNthRandomSet(getSiteInfo(), whichRSet).getRandomSet().getState();
    Assert.assertEquals("Expected the 0th RandomSet to be enrolling", RSState.Enrolling, rsState);

    Database db = getDatabaseSupplier().get();
    Patient pat = createPatient(db, "test-in/exclude", "Downy", "Upman", 25);
    createInsertedParticipant(db, pat, whichRSet, rspState);

    long expectedNum = (includes ? 1 : 0);
    expectedNum += (uiOrRpt) ? 2 : 0;  // UI should include not-assigned reports
    String msg = String.format("For %s, RandomSet state=%s", uiOrRpt ? "UI" : "Report", rsState.toString());
    if (uiOrRpt) {
      ArrayList<RandomSetParticipant> list = getParticipantsForUI(pat.getPatientId());
      Assert.assertEquals(msg, expectedNum, list.size());
    } else {
      ArrayList<RandomSetParticipant> list = getParticipantsForReport(pat.getPatientId());
      Assert.assertEquals(msg, expectedNum, list.size());
    }
  }
}
