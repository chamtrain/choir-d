/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.test;

import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.service.AdministrativeServicesImpl;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.User;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

/**
 * Test cases to verify UserInfo works properly.
 */
public class StandAloneEmailInvitationsTest extends DatabaseTestCase {
  private String surveyLink = "https://outcomes.stanford.edu";
  private String emailAddress = "testing@test.stanford.edu";

  private Date dob = DateUtils.getDaysAgoDate(30 * 365);
  private User user;

  private static Logger logger = Logger.getLogger(StandAloneEmailInvitationsTest.class);

  @Override
  protected void postSetUp() throws Exception {
    Utils utils = new Utils(databaseProvider.get(), getSiteInfo());
    user = utils.getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
  }


  public void testPropertiesAreSetRight() {
    assertOnePropertyValue("appointment.initialemail.daysout", "7");
    assertOnePropertyValue("appointment.noemail.withindays", "2");
    assertOnePropertyValue("appointment.lastsurvey.daysout", "11");
    assertOnePropertyValue("appointment.reminderemail.daysout", "4,1");
    assertOnePropertyValue("appointment.scheduledsurvey.daysout", "90");
    assertOnePropertyValue("appointment_template", "apptTemplate");
  }


  class Days {
    final int days;
    final int daysOut;
    final Date lastSurveyDt;
    Days() {
      final String dayString = getSiteInfo().getProperty("appointment.initialemail.daysout");
      days = Integer.parseInt(dayString);
      final String daysOutString = getSiteInfo().getProperty("appointment.scheduledsurvey.daysout");
      daysOut = Integer.parseInt(daysOutString);
      logger.debug(" initialemail.daysout="+days+" scheduledsurvey.daysout="+daysOut
          + " so lastSurveyDt = "+(days-daysOut)+" days out");
      lastSurveyDt = new Date(DateUtils.getDaysOutDate(days - daysOut).getTime());
    }
  }


  public void testStandAloneSchedules() throws Exception {
    logger.info("testStandAloneSchedules starting");

    // first send any pending
    int emailsSent = 0;
    AdministrativeServices adminSvc = getAdminService(user);
    emailsSent = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);

    Days day = new Days();

    // Create patients and appointments for the tests
    Patient testPatient0 = new Patient("10045-3", "John", "Doe", new Date(dob.getTime()));
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    testPatient0 = patientDao.addPatient(testPatient0);
    Database database = databaseProvider.get();
    Utils utils = new Utils(databaseProvider.get(), getSiteInfo());
    utils.addInitialRegistration(database, testPatient0.getPatientId(), day.lastSurveyDt, "", "NPV60");

    /*
     * Test 1: Patient has not had an appointment in the
     * 'scheduledsurvey.daysout' number of days but has not consented (no)
     */
    emailsSent = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    logger.info("Test #1 (0): " + emailsSent + " standalone emails were sent. Patient has not consented.");
    assertEquals(0, emailsSent);

    /*
     * Test 2: Patient has not had an appointment in the
     * 'scheduledsurvey.daysout' days, have consented but we have no email
     * address for them (no)
     */
    adminSvc.setPatientAgreesToSurvey(testPatient0);
    emailsSent = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    logger.info("Test #2 (0): " + emailsSent
        + " standalone emails were sent. Patient consented but has no email address.");
    assertEquals(0, emailsSent);

    /*
     * Test 3: Patient has not had an appointment in the
     * 'scheduledsurvey.daysout' days, have consented and have an email address
     * and have no other appointments within the window.
     */
    PatientAttribute patAttribute = new PatientAttribute(testPatient0.getPatientId(), Constants.ATTRIBUTE_SURVEYEMAIL,
        emailAddress, PatientAttribute.STRING);

    adminSvc.addPatientAttribute(patAttribute);
    emailsSent = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    logger.info("Test #3 (0): " + emailsSent
        + " email sent for the patient that has consented and has an email address and has not had an appointment in "
        + day.days + "days. Turned off pending an IRB");
    assertEquals(0, emailsSent);

    logger.info("testStandAloneSchedules finished");
  }


  public void test4_just1EmailForNewApptWhen2ApptsArePending() throws IOException {
    Utils utils = new Utils(databaseProvider.get(), getSiteInfo());
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    AdministrativeServices adminSvc = getAdminService(user);

    Days day = new Days();
    /*
     * Test 4: Patient has not had an appointment in the
     * 'scheduledsurvey.daysout' days, have consented and have an email address
     * but have another appointment within the next 'initialemail.daysout' days
     * (send mail only to for the new appointment)
     */
    Patient testPatient1 = new Patient("10046-1", "John", "Smith", new Date(dob.getTime()));
    testPatient1 = patientDao.addPatient(testPatient1);
    utils.addInitialRegistration(databaseProvider.get(), testPatient1.getPatientId(), day.lastSurveyDt, emailAddress, "NPV60");

    Date surveyDt = new Date(DateUtils.getDaysOutDate(day.days - 1).getTime());
    adminSvc.setPatientAgreesToSurvey(testPatient1);
    // 2nd appointment, in 6 days
    utils.addFollowUpRegistration(databaseProvider.get(), testPatient1.getPatientId(), surveyDt, emailAddress, "RPV60"); // create

    int emailsSent = getAdminService(user).doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    logger.info("Test #4 (1): " + emailsSent
        + " emails were sent for patients upcoming appointment within the window, not the standalone.");
    logger.debug("Made initialReg on "+day.lastSurveyDt+" and followUp on "+surveyDt);

    // TODO: Is there a way to test for which appointment the email was sent?
    assertEquals(1, emailsSent);
  }


  private AdministrativeServices getAdminService(User user) {
    return new AdministrativeServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }

}
