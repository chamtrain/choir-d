/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server;

import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.RandomSetDao;
import edu.stanford.registry.server.randomset.RandomSetter;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.service.AdministrativeServicesImpl;
import edu.stanford.registry.server.survey.AngerService;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.RandomSetCategory;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.RandomSetParticipant.State;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.Utils;
import edu.stanford.registry.tool.RandomSetsCreate;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;


public class PainSurveySchedulerTest extends DatabaseTestCase {
  private static final Logger logger = LoggerFactory.getLogger(PainSurveySchedulerTest.class);
  private final String surveyLink = "https://outcomes.stanford.edu";
  private final String emailAddress = "testing@test.stanford.edu";

  private Date dob;
  private Supplier<Database> databaseProvider;
  private AdministrativeServices adminSvc;
  private RandomSetter rs;
  private Date surveyDt;
  private PainSurveyScheduler scheduler;

  @Override
  protected void postSetUp() {
    databaseProvider = getDatabaseProvider();
    User user = new Utils(databaseProvider.get(), getSiteInfo()).getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
    dob = DateUtils.getDaysAgoDate(getSiteInfo(), 30 * 365);
    adminSvc = getAdminService(user);
    rs = getSiteInfo().getRandomSet(RandomSetsCreate.TSET_KSort_DD);
    surveyDt = initialDaysOut();
    scheduler = getPainSurveyScheduler();
  }

  /**
   * This tests that only patients set to participates in surveys and have email will be sent follow-ups
   */
  public void testAngerSendFollowUpsOnlyWhenHasEmail() {
    // create two Trait anger patients for this test with the same assigned date
    // One with no email address, both have agreed to follow ups
    Date date = new Date();
    Calendar calendar =  Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.MONTH, -3);


    createAngerPatient("10040-4", "John", "Doe", null, true, calendar.getTime());
    // Another with an email address, participates is YES
    createAngerPatient("10041-2", "Jane", "Doe", emailAddress, true, calendar.getTime());

    scheduler.scheduleSurveys(surveyDt);
    assertEquals(patientSurveyCountByType(surveyDt, "AngerFollowUp"), 1);
  }

  /**
   * This tests that only patients set to participates in surveys will be sent follow-ups
   */
  public void testAngerSendFollowUpsOnlyWhenParticipates() {
    // create two Trait anger patients for this test with the same assigned date
    // One with no email address, both have agreed to follow ups
    Date date = new Date();
    Calendar calendar =  Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.MONTH, -3);


    createAngerPatient("10040-4", "John", "Doh", emailAddress, true, calendar.getTime());
    // Another with an email address, participates is YES
    createAngerPatient("10041-2", "Jane", "Doh", emailAddress, false, calendar.getTime());

    scheduler.scheduleSurveys(surveyDt);
    assertEquals(patientSurveyCountByType(surveyDt, "AngerFollowUp"), 1);
  }
  /**
   * This tests that only patients set to participates in surveys will be sent follow-ups
   */
  public void testSendFollowUpsOnlyWhenParticipates() {
    // create two Randomsetpatients for this test with the same assigned date
    // One with an email address and participates
    RandomSetParticipant rsp1 =
        createRandomsetParticipant("10040-4", "John", "Doe", emailAddress,
            true, getEnrolledLastMonth(0));
    logger.debug("testSendFollowUpsOnlyWhenParticipates participant 1 was added with an assigned date {}", rsp1.getAssignedDate());

    // Another with an email NOT set to participates
    RandomSetParticipant rsp2 = createRandomsetParticipant("10041-2", "Jane", "Doe", emailAddress,
        false, getEnrolledLastMonth(0));
    logger.debug("ztestSendFollowUpsOnlyWhenParticipates participant 2 was added with an assigned date {}", rsp2.getAssignedDate());
    scheduler.scheduleSurveys(surveyDt);
    assertEquals(patientSurveyCountByType(surveyDt, rs.getRandomSet().getName()), 1);
  }

  /**
   * This tests that only patients set to participates in surveys and have email will be sent follow-ups
   */
  public void testSendFollowUpsOnlyWhenHasEmail() {
    // create two Randomsetpatients for this test with the same assigned date
    // One with no email address, participates is YES

    createRandomsetParticipant("10040-4", "John", "Doe", null,
        true, getEnrolledLastMonth(0));
    // Another with an email address, participates is YES
    createRandomsetParticipant("10041-2", "Jane", "Doe", emailAddress,
        true, getEnrolledLastMonth(0));

    scheduler.scheduleSurveys(surveyDt);
    assertEquals(patientSurveyCountByType(surveyDt, rs.getRandomSet().getName()), 1);
  }

  /**
   * Tests that it creates surveys for patients assigned on the given date and the 3 days prior
   */
  public void testSendFollowUpsForThreeBack() {
    createRandomsetParticipant("10040-4", "Jane", "Doe", emailAddress,
        true, getEnrolledLastMonth(0));
    createRandomsetParticipant("10041-2", "Jane", "Doe", emailAddress,
        true, getEnrolledLastMonth(-1));
    createRandomsetParticipant("10042-0", "Jack", "Spratt", emailAddress,
        true, getEnrolledLastMonth(-2));
    createRandomsetParticipant("10043-8", "Jack", "Horner", emailAddress,
        true, getEnrolledLastMonth(-3));
    createRandomsetParticipant("100044-6", "Susie", "Q", emailAddress,
        true, getEnrolledLastMonth(-4));
    scheduler.scheduleSurveys(surveyDt);
    assertEquals(patientSurveyCountByType(surveyDt, rs.getRandomSet().getName()), 4);

  }

  /**
   * Tests that patients assigned in the three days prior that have already had followups created
   * will not have another survey created the next day.
   */
  public void testSendFollowUpsForThreeBackTwice() {
    createRandomsetParticipant("10040-4", "Jane", "Doe", emailAddress,
        true, getEnrolledLastMonth(0));
    createRandomsetParticipant("10041-2", "Jane", "Doe", emailAddress,
        true, getEnrolledLastMonth(-1));
    createRandomsetParticipant("10042-0", "Jack", "Spratt", emailAddress,
        true, getEnrolledLastMonth(-2));
    createRandomsetParticipant("10043-8", "Jack", "Horner", emailAddress,
        true, getEnrolledLastMonth(-3));
    scheduler.scheduleSurveys(surveyDt);

    // but not if they already exist, so running it for the next day won't create any and will still only find four
    Date tomorrow = DateUtils.getDaysFromDate(getSiteInfo(), surveyDt, 1);

    scheduler.scheduleSurveys(tomorrow);
    assertEquals(patientSurveyCountByType(surveyDt, rs.getRandomSet().getName()), 4);

  }

  /**
   * Tests that patients assigned in the three days prior that participate and have emails addresses
   * will  be sent an email for the survey.
   */
  public void testSendFollowUpEmails() throws Exception {
    createRandomsetParticipant("10040-4", "Jane", "Doe", emailAddress,
        true, getEnrolledLastMonth(0));
    createRandomsetParticipant("10041-2", "Jane", "Doe", emailAddress,
        true, getEnrolledLastMonth(-1));
    createRandomsetParticipant("10042-0", "Jack", "Spratt", emailAddress,
        true, getEnrolledLastMonth(-2));
    createRandomsetParticipant("10043-8", "Jack", "Horner", emailAddress,
        true, getEnrolledLastMonth(-3));
    scheduler.scheduleSurveys(surveyDt);

    int emails = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals(emails, 4);
  }

  /**
   * Tests that patients assigned in the three days prior that have already had emails sent
   * will not be sent another email the next day.
   */
  public void testSendFollowUpEmailsTwice() throws Exception {
    createRandomsetParticipant("10040-4", "Jane", "Doe", emailAddress,
        true, getEnrolledLastMonth(0));
    createRandomsetParticipant("10041-2", "Jane", "Doe", emailAddress,
        true, getEnrolledLastMonth(-1));
    createRandomsetParticipant("10042-0", "Jack", "Spratt", emailAddress,
        true, getEnrolledLastMonth(-2));
    createRandomsetParticipant("10043-8", "Jack", "Horner", emailAddress,
        true, getEnrolledLastMonth(-3));
    scheduler.scheduleSurveys(surveyDt);
    adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);

    // repeat for tomorrow
    Date tomorrow = DateUtils.getDaysFromDate(getSiteInfo(), surveyDt, 1);

    scheduler.scheduleSurveys(tomorrow);
    int emails = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals(emails, 0);
  }


  /**
   * Tests that patients will still be sent their appointment survey even if they have a treatment set follow-up pending.
   */
  public void testSendFollowUpsForOtherTypes() {
    Utils utils = new Utils(databaseProvider.get(), getSiteInfo());
    RandomSetParticipant rsp = createRandomsetParticipant("10040-4", "Jane", "Doe", emailAddress,
        true, getEnrolledLastMonth(0));
    scheduler.scheduleSurveys(surveyDt); // create a treatment  survey
    ApptRegistration apptRegistration0 = utils.getInitialRegistration(rsp.getPatientId(), surveyDt, emailAddress, "NPV60");
    apptRegistration0.setSendEmail(true);

    Patient patient = getPatient(rsp.getPatientId());
    adminSvc.setPatientAgreesToSurvey(patient);
    try {
      adminSvc.addPatientRegistration(apptRegistration0, patient);
      int emails = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
      assertEquals(emails, 2);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private int patientSurveyCountByType(Date surveyDt, final String type) {
    String q = "SELECT survey_type, (select appt_reg_id from appt_registration ar "
        + "where ar.assessment_reg_id = sr.assessment_reg_id) as appt_reg_id "
        + "FROM survey_registration sr where survey_dt = ? ";
    return databaseProvider.get().toSelect(q)
        .argDate(surveyDt)
        .query(rs -> {
          int surveysFound = 0;
          while (rs.next()) {

            String surveyType = rs.getStringOrNull(1);
            if (surveyType != null && surveyType.startsWith(type)) {
              surveysFound++;
            }

          }
          return surveysFound;
        });
  }

  private RandomSetParticipant createRandomsetParticipant(String patientId, String firstName, String lastName,
                                                          String email, boolean participates, Date assignedDt) {
    Patient testPatient0 = createPatient(patientId, firstName, lastName);
    if (participates) {
      adminSvc.setPatientAgreesToSurvey(testPatient0);
    }
    if (email != null) {
      addPatientEmail(testPatient0.getPatientId(), email);
    }
    RandomSetParticipant rsp = new RandomSetParticipant(-1, testPatient0.getPatientId(), rs.getRandomSet(), State.Unset, "", "",
        assignedDt, null, "");
    rsp.setRandomsetParticipantId(updateParticipant(databaseProvider.get(), rsp));
    return rsp;
  }

  private void createAngerPatient(String patientId, String firstName, String lastName, String email,
                                  boolean participates, Date angerSurveyDate) {
    Patient testPatient0 = createPatient(patientId, firstName, lastName);

    if (email != null) {
      addPatientEmail(patientId, email );
    }

    if (participates) {
      adminSvc.setPatientAgreesToSurvey(testPatient0);
    }

    addPatientAngerFollowUp(patientId, angerSurveyDate );
  }
  private Patient createPatient(String idea, String firstName, String lastName) {
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    Patient testPatient = new Patient(idea, firstName, lastName, new Date(dob.getTime()));
    testPatient = patientDao.addPatient(testPatient);
    return testPatient;
  }

  private Patient getPatient(String patientId) {
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    return patientDao.getPatient(patientId);
  }

  private long updateParticipant(Database db, RandomSetParticipant rsp) {
    RandomSetCategory[] cats = rsp.getRandomSet().getCategories();
    rsp.setStratumName((cats == null) ? RandomSetCategory.NoStratumName : "");
    rsp.setState(State.Assigned); // marks it as chaged so it writes into the database

    RandomSetDao dao = new RandomSetDao(getSiteInfo(), db);

    if (rsp.getState().isAnAssignedState() && rsp.getAssignedDate() == null) {
      logger.warn("It's going to override the assigned date!");
    }
    // save the assigned date
    Date assigned = rsp.getAssignedDate();
    rsp = dao.updateOrInsertParticipant(rsp); // this changes the assigned date so reset it
    db.toUpdate("UPDATE randomset_participant SET dt_assigned=? WHERE participant_id=?")
        .argDate(assigned)
        .argLong(rsp.getRandomsetParticipantId())
        .update(1);
    return rsp.getRandomsetParticipantId();
  }

  private Date initialDaysOut() {
    String dayString = getSiteInfo().getProperty("appointment.initialemail.daysout");
    int days = Integer.parseInt(dayString);
    return setEndOfDay(new Date(DateUtils.getDaysOutDate(getSiteInfo(), days).getTime()));
  }

  private Date getEnrolledLastMonth(int plusDays) {
    Calendar enrollCal = Calendar.getInstance();
    enrollCal.setTime(new Date());
    enrollCal.add(Calendar.MONTH, -1);
    enrollCal.add(Calendar.DAY_OF_MONTH, plusDays);
    return enrollCal.getTime();
  }

  private AdministrativeServices getAdminService(User user) {
    return new AdministrativeServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }

  private PainSurveyScheduler getPainSurveyScheduler() {
    return new PainSurveyScheduler(databaseProvider.get(), getSiteInfo());
  }

  private void addPatientEmail(String patientId, String value) {
    PatientAttribute attribute0 = new PatientAttribute(patientId, Constants.ATTRIBUTE_SURVEYEMAIL, value);
    adminSvc.addPatientAttribute(attribute0);
  }

  private void addPatientAngerFollowUp(String patientId, Date date) {
    PatientAttribute attribute = new PatientAttribute(patientId, AngerService.followConsent, "Y");
    attribute = adminSvc.addPatientAttribute(attribute);
    String updSql = "update patient_attribute set dt_created = ? where PATIENT_ATTRIBUTE_ID = ?";
    databaseProvider.get().toUpdate(updSql).argDate(date).argLong(attribute.getPatientAttributeId()).update(1);
  }

  private Date setEndOfDay(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    setEndOfDay(cal);
    date.setTime(cal.getTimeInMillis());
    return date;
  }

  private void setEndOfDay(Calendar cal) {
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
  }
}
