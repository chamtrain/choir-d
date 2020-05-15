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
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

/**
 * Mainly intended to test .adminSvc.setPatientAgreesToSurvey() and
 * especially EmailMonitor.java.
 */
public class NewEmailInvitationsTest extends DatabaseTestCase {
  private Supplier<Database> databaseProvider;
  private String surveyLink = "https://outcomes.stanford.edu";
  private String emailAddress = "testing@test.stanford.edu";

  final private Date dob = DateUtils.getDaysAgoDate(30 * 365);
  private User user;

  private static Logger logger = Logger.getLogger(NewEmailInvitationsTest.class);

  @Override
  protected void postSetUp() throws Exception {
    databaseProvider = getDatabaseProvider();
    user = new Utils(databaseProvider.get(), getSiteInfo()).getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
    mailer = getSiteInfo().getMailer();
  }

  Mailer mailer = null;

  String getPatientAtr(PatientDao pdau, Patient pat, String atrName) {
    ArrayList<PatientAttribute> attrs = pdau.getAttributes(pat.getPatientId());
    for (PatientAttribute atr: attrs) {
      if (atr.getDataName().equals(atrName))
        return atr.getDataValue();
    }
    return null;
  }

  public void testApptPatientAgreesEmailOnSurvey() throws Exception {
    Database db = databaseProvider.get();
    AdministrativeServices adminSvc = getAdminService(user);
    Utils utils = new Utils(db, getSiteInfo());

    Patient testPatient = createPatient("10041-2", "Patient", "LacksEmail");
    adminSvc.setPatientAgreesToSurvey(testPatient);

    // Initial registration with email address
    utils.addInitialRegistration(db, testPatient.getPatientId(), initialDaysOut(0), emailAddress, "NPV60");

    int emailsSent = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    logger.info("Test #3 (1): " + emailsSent
        + " emails were sent for consented patients with email address on patient or on survey.");
    assertEquals(1, emailsSent);
  }

  public void testApptPatientHasEmailAndAgrees() throws Exception {
    Database db = databaseProvider.get();
    AdministrativeServices adminSvc = getAdminService(user);
    Utils utils = new Utils(db, getSiteInfo());
    String noEmail = "";

    Patient testPatient = createPatient("10041-2", "Patient", "IHaveEmailAndConsent");
    addEmailAttribute(testPatient, emailAddress);
    adminSvc.setPatientAgreesToSurvey(testPatient);

    utils.addInitialRegistration(db, testPatient.getPatientId(), initialDaysOut(0), noEmail, "NPV60");

    int emailsSent = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    logger.info("Test #3 (1): " + emailsSent
        + " emails were sent for consented patients with email address on patient or on survey.");
    assertEquals(1, emailsSent);
  }

  /**
   * Appointment within 7 days ( at and < ) and have not been sent email for this appointment
   * <p/>
   * but no email address in attributes (no) case 3: consented and have email address but have not received email from us within the last 2
   * days case 4: consented, have email addr, no email in last 2 days, had an appointment where emails were sent within window (no) case 5:
   * consented, email addr, no email in 2 days, no appts with emails within window (SEND INITIAL EMAIL)
   */
  public void testNewAppointments() throws Exception {
    logger.info("testNewAppointments starting");

    // initialize some tools and variable
    AdministrativeServices adminSvc = getAdminService(user);
    Database db = databaseProvider.get();
    Utils utils = new Utils(db, getSiteInfo());
    String noEmail = "";
    // set up 2 days for emails
    Date surveyDt = initialDaysOut(0);
    Date surveyDtUnder = initialDaysOut(-2);

    // first send any pending, in case dirty from another test
    int emailsSent = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);

    // Create patient0 - no permission or email and appointment for the tests
    Patient testPatient0 = createPatient("10040-4", "John", "Doe");
    utils.addInitialRegistration(db, testPatient0.getPatientId(), surveyDt, noEmail, "NPV60");

    // Appointments with survey date = daysout & < daysout
    // Test 1: no one has consented so no emails should be sent
    emailsSent = adminSvc.doSurveyInvitations(mailer, surveyLink);
    logger.info("Test #1 (0): " + emailsSent + " emails were sent for a patient that has not consented.");
    assertEquals(0, emailsSent);


    // Test 2: patient0 consents but neither the patient nor survey has an email address
    adminSvc.setPatientAgreesToSurvey(testPatient0);
    emailsSent = adminSvc.doSurveyInvitations(mailer, surveyLink);
    logger.info("Test #2 (0): " + emailsSent + " emails were sent for consented but no email address.");
    assertEquals(0, emailsSent);


    // Test 3:Add email address attribute to patient0, set patient1 agrees
    addEmailAttribute(testPatient0, emailAddress);

    Patient testPatient1 = createPatient("10041-2", "John", "Smith3");
    utils.addInitialRegistration(db, testPatient1.getPatientId(), surveyDt, emailAddress, "NPV60");
    adminSvc.setPatientAgreesToSurvey(testPatient1);

    emailsSent = adminSvc.doSurveyInvitations(mailer, surveyLink);
    logger.info("Test #3 (2): " + emailsSent
        + " emails were sent for consented patients with email address on patient or on survey.");
    assertEquals(2, emailsSent);


    // Test 4: survey is < daysout, patient has email address and consents, survey has email
    Patient testPatient2 = createPatient("10042-0", "Jane", "Doe4");
    utils.addInitialRegistration(db, testPatient2.getPatientId(), surveyDtUnder, emailAddress, "NPV60");
    addEmailAttribute(testPatient2, emailAddress);
    adminSvc.setPatientAgreesToSurvey(testPatient2);

    emailsSent = adminSvc.doSurveyInvitations(mailer, surveyLink);
    logger.info("Test #4 (1): " + emailsSent + " emails were sent for an appt < the daysout setting.");
    assertEquals(1, emailsSent);


    // Test 5: survey < daysout but patient had survey within timeframe
    utils.addFollowUpRegistration(db, testPatient0.getPatientId(), surveyDtUnder, emailAddress, "RPV45");
    // adminSvc.addPatientRegistration(registration4, testPatient0);

    emailsSent = adminSvc.doSurveyInvitations(mailer, surveyLink);
    logger.info("Test #5 (0): " + emailsSent
        + " emails were sent for a patient with a 2nd appointment within the window.");
    assertEquals(0, emailsSent);
    logger.info("testNewAppointments finished");
  }

  void addEmailAttribute(Patient patient, String emailAddr) {
    AdministrativeServices adminSvc = getAdminService(user);
    String id = patient.getPatientId();
    PatientAttribute patAttribute =
        new PatientAttribute(id, Constants.ATTRIBUTE_SURVEYEMAIL, emailAddr, PatientAttribute.STRING);
    adminSvc.addPatientAttribute(patAttribute);
  }

  Patient createPatient(String idea, String firstName, String lastName) {
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    Patient testPatient = new Patient(idea, firstName, lastName, new java.util.Date(dob.getTime()));
    testPatient = patientDao.addPatient(testPatient);
    return testPatient;
  }

  Date initialDaysOut(int plusDays) {
    String dayString = getSiteInfo().getProperty("appointment.initialemail.daysout");
    int days = Integer.parseInt(dayString);
    return new Date(DateUtils.getDaysOutDate(days + plusDays).getTime());
  }

  private AdministrativeServices getAdminService(User user) {
    return new AdministrativeServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }
}
