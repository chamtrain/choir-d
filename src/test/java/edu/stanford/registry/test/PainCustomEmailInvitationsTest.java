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

package edu.stanford.registry.test;

import edu.stanford.registry.server.PainManagementCustomizer;
import edu.stanford.registry.server.PainSurveyScheduler;
import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveyRegistrationAttributeDao;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.service.AdministrativeServicesImpl;
import edu.stanford.registry.server.utils.ClassCreator;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;

/**
 * To test
 * 1. EMPOWER appointments aren't sent emails by EmailMonitor.java.
 * 2. Follow up surveys are sent to patients who have been pushed a ProcBotox survey within the past year unless stopped.
 */
public class PainCustomEmailInvitationsTest extends DatabaseTestCase {
  private static final Logger logger = Logger.getLogger(PainCustomEmailInvitationsTest.class);
  private final String surveyLink = "https://outcomes.stanford.edu";
  private final String emailAddress = "testing@test.stanford.edu";
  private Date dob;
  private Supplier<Database> databaseProvider;
  private User user;
  private Mailer mailer = null;
  private SurveyRegUtils regUtils;
  private AssessDao assessDao;
  private AppConfigDao appConfigDao;
  private SurveyRegistrationAttributeDao surveyRegAttrDao;
  private AdministrativeServices adminSvc;
  private Patient patient70;
  @Override
  protected void postSetUp() {
    databaseProvider = getDatabaseProvider();
    user = new Utils(databaseProvider.get(), getSiteInfo()).getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
    adminSvc = getAdminService(user);
    mailer = getSiteInfo().getMailer();
    appConfigDao = new AppConfigDao(databaseProvider.get(), ServerUtils.getAdminUser(databaseProvider.get()));
    appConfigDao.addOrEnableAppConfigEntry(1L, "configparam", "RegistryCustomizerClass", "edu.stanford.registry.server.PainManagementCustomizer");
    appConfigDao.addOrEnableAppConfigEntry(1L, "configparam",PainManagementCustomizer.CONFIG_SEND_BOTOX, "42");
    appConfigDao.addOrEnableAppConfigEntry(1L, "configparam", PainManagementCustomizer.CONFIG_STOP_BOTOX, "365");
    appConfigDao.addOrEnableAppConfigEntry(1L, "emailtemplate", "PostProcedure", "Hello. We would like you to please complete a new questionnaire. You can access the questionnaire by clicking on the following link:\n"
        + "[SURVEY_LINK]  This link will be valid through  [SURVEY_DATE] only. \n\n"
        + "If you have any questions or have any troubles accessing the questionnaire via the link please give us a call at 1-555-1212.  \n\n"
        + "Thank you,\n"
        + "The Stanford Pain Management Center ");

    serverContext.reload(false, true);
    regUtils = new SurveyRegUtils(getSiteInfo());
    assessDao = new AssessDao(databaseProvider.get(), getSiteInfo());
    surveyRegAttrDao = new SurveyRegistrationAttributeDao(databaseProvider.get());
    dob = DateUtils.getDaysAgoDate(getSiteInfo(), 30 * 365);
    patient70 = createPatient("10070-1","One", "Patient");
  }

  /**
   * Initial Appointment within 7 days and have not been sent email for this appointment
   * consented and have email address but empower type appointment so no email should be sent
   */
  public void testNewEmpowerAppointment() throws Exception {
    logger.info("testNewEmpowerAppointments starting");

    // initialize some tools and variable
    AdministrativeServices adminSvc = getAdminService(user);
    Database db = databaseProvider.get();
    Utils utils = new Utils(db, getSiteInfo());
    Date surveyDt = initialDaysOut(0);

    // Create patient0 - consented and has email but the  appointment type is EMPOWER
    Patient testPatient0 = createPatient("10040-4", "John", "Doe");
    addEmailAttribute(testPatient0);
    adminSvc.setPatientAgreesToSurvey(testPatient0);
    utils.addInitialRegistration(db, testPatient0.getPatientId(), surveyDt, emailAddress, "NPV60EMPOWER");
    int emailsSent = adminSvc.doSurveyInvitations(mailer, surveyLink);
    logger.info("testNewEmpowerAppointment (0): " + emailsSent + " emails were sent for a patient with EMPOWER visit.");
    assertEquals(0, emailsSent);
  }

  /**
   * Follow up appointments within 7 days and have not been sent email for these appointments
   * consented and have email address but the empower type appointment should not be sent email
   */
  public void testReturnEmpowerAppointment() throws Exception {
    logger.info("testReturnEmpowerAppointments starting");

    // initialize some tools and variable
    AdministrativeServices adminSvc = getAdminService(user);
    Database db = databaseProvider.get();
    Utils utils = new Utils(db, getSiteInfo());
    Date surveyDt = initialDaysOut(-2);


    Patient testPatient1 = createPatient("10041-2", "John", "Smith3");
    adminSvc.setPatientAgreesToSurvey(testPatient1);
    utils.addFollowUpRegistration(db, testPatient1.getPatientId(), surveyDt, emailAddress, "RPV30");


    Patient testPatient2 = createPatient("10042-0", "Jane", "Doe4");
    adminSvc.setPatientAgreesToSurvey(testPatient2);
    addEmailAttribute(testPatient2);
    utils.addFollowUpRegistration(db, testPatient1.getPatientId(), surveyDt, emailAddress, "RPV30EMPOWER");

    int emailsSent = adminSvc.doSurveyInvitations(mailer, surveyLink);
    logger.info("testReturnEmpowerAppointment (1): " + emailsSent
        + " emails were sent for consented patients with email address on patient or on survey for the non empower visit.");
    assertEquals(1, emailsSent);

  }

  public void testSendBotoxFollowupsDaysBack() throws Exception {

    initFUPatient(patient70);

    /*
     * Calculate the number of days back for the initial registration
     */
    String sendDaysString = appConfigDao.findAppConfigEntry(getSiteInfo().getSiteId(), "configparam", "SendProcBotox").getConfigValue();
    String initDaysString = getSiteInfo().getProperty("appointment.initialemail.daysout");
    int procPushDays = Integer.parseInt(initDaysString) + Integer.parseInt(sendDaysString);
    /*
     * Make the initial survey registration
     */
    createTriggerRegistration(patient70, "ProcBotox", procPushDays);
    /*
     * Run send todays emails and check they've been sent another survey
     */
    int emailsSent1 = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals("Expecting a follow up botox survey registration to be created and one email sent", 1, emailsSent1);
    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting the patient to have 2 registrations, the trigger and the follow up", 2, registrationList.size());
  }

  public void testSendBotoxFollowupsAfterYear() throws Exception {

    /*
     * Create a patient
     */
    Patient testPatient2 = createPatient("10042-0", "Jane", "Jones");
    adminSvc.setPatientAgreesToSurvey(testPatient2);
    addEmailAttribute(testPatient2);

    String stopDaysString = appConfigDao.findAppConfigEntry(getSiteInfo().getSiteId(), "configparam", "StopProcBotox").getConfigValue();
    String initDaysString = getSiteInfo().getProperty("appointment.initialemail.daysout");

    /*
     * Make a procedure survey dated before the procedure stop days cutoff
     */
    ApptRegistration apptRegistration1 = addBotoxRegistration(testPatient2.getPatientId(),
        initialDaysOut((Integer.parseInt(initDaysString) + Integer.parseInt(stopDaysString) +4) * -1));
    apptRegistration1 = assessDao.getPatientRegistrationByRegId(apptRegistration1.getApptId());
    assertNotNull("Expecting to find the registration", apptRegistration1);
    /*
     * Make a trigger registration
     */
    ApptRegistration apptRegistration2 = createBotoxTriggerRegistration(testPatient2);
    /*
     * Set the first assessment as the parent of the 2nd one
     */
    SurveyRegistrationAttributeDao surveyRegAttrDao = new SurveyRegistrationAttributeDao(databaseProvider.get());
    surveyRegAttrDao.setAttribute(apptRegistration2.getSurveyRegList().get(0).getSurveyRegId(), "PARENT",
        apptRegistration1.getSurveyRegList().get(0).getSurveyRegId().toString() );

    int emailsSent2  = adminSvc.doSurveyInvitations(mailer, surveyLink);
    assertEquals("Expecting no no follow up survey email because the initial survey was before the stop days",
        0, emailsSent2);

    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(testPatient2.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting patient to still have only 2 ", 2, registrationList.size());
  }

  public void testSendAcupunctureFollowUps() throws Exception {
    initFUPatient(patient70);
    String initDaysString = getSiteInfo().getProperty("appointment.initialemail.daysout");
    int procPushDays = Integer.parseInt(initDaysString);
    createTriggerRegistration(patient70, PainSurveyScheduler.ACUPUNCT,  procPushDays + 7);
    createTriggerRegistration(patient70, PainSurveyScheduler.ACUPUNCT,  procPushDays + 14);
    createTriggerRegistration(patient70, PainSurveyScheduler.ACUPUNCT,  procPushDays + 28);
    int emailsSent1 = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals("Expecting a follow up " + PainSurveyScheduler.ACUPUNCT +
        " registration to be created and one email sent", 1, emailsSent1);

    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting a fourth follow up " + PainSurveyScheduler.ACUPUNCT + " registration to be created ",
        4, registrationList.size());
  }

  public void testStopSendAcupunctureFollowUps() throws Exception {
    initFUPatient(patient70);
    String initDaysString = getSiteInfo().getProperty("appointment.initialemail.daysout");
    ApptRegistration apptRegistration1 = createTriggerRegistration(patient70, PainSurveyScheduler.ACUPUNCT,
        Integer.parseInt(initDaysString) + 185);
    ApptRegistration apptRegistration2 = createTriggerRegistration(patient70, PainSurveyScheduler.ACUPUNCT,
        Integer.parseInt(initDaysString) + 7);
    surveyRegAttrDao.setAttribute(apptRegistration2.getSurveyRegList().get(0).getSurveyRegId(), "PARENT",
        apptRegistration1.getSurveyRegList().get(0).getSurveyRegId().toString());
    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting two " + PainSurveyScheduler.ACUPUNCT + " registration to have been created",
        2, registrationList.size());

    adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting no follow up " + PainSurveyScheduler.ACUPUNCT +
        " registration to be created past 90 days", 2, registrationList.size());
  }

  public void testSendLumbarEpiFollowUps() throws Exception {
    initFUPatient(patient70);
    String initDaysString = getSiteInfo().getProperty("appointment.initialemail.daysout");
    int procPushDays = Integer.parseInt(initDaysString);
    createTriggerRegistration(patient70, PainSurveyScheduler.LUMBAREPI,  procPushDays + 7);
    createTriggerRegistration(patient70, PainSurveyScheduler.LUMBAREPI,  procPushDays + 14);
    createTriggerRegistration(patient70, PainSurveyScheduler.LUMBAREPI,  procPushDays + 28);
    int emailsSent1 = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals("Expecting a follow up " + PainSurveyScheduler.LUMBAREPI +
        " registration to be created and one email sent", 1, emailsSent1);

    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting a follow up " + PainSurveyScheduler.LUMBAREPI + " registration to be created ",
        4, registrationList.size());
  }

  public void testStopSendLumbarEpiFollowUps() throws Exception {
    initFUPatient(patient70);
    String initDaysString = getSiteInfo().getProperty("appointment.initialemail.daysout");

    ApptRegistration apptRegistration1 = createTriggerRegistration(patient70, PainSurveyScheduler.LUMBAREPI,
        Integer.parseInt(initDaysString) + 185);
    ApptRegistration apptRegistration2 = createTriggerRegistration(patient70, PainSurveyScheduler.LUMBAREPI,
        Integer.parseInt(initDaysString) + 7);
    surveyRegAttrDao.setAttribute(apptRegistration2.getSurveyRegList().get(0).getSurveyRegId(), "PARENT",
        apptRegistration1.getSurveyRegList().get(0).getSurveyRegId().toString());

    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting two " + PainSurveyScheduler.LUMBAREPI + " registrations to have been created",
        2, registrationList.size());

    adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting no follow up " + PainSurveyScheduler.LUMBAREPI +
            " registration to be created past 90 days", 2, registrationList.size());
  }

  // Nerve block twice. First is 48 hours (sent manually) second is 2 weeks after (12 days after first)
  public void testSendNerveBlockFollowUps() throws Exception {
    initFUPatient(patient70);
    createTriggerRegistration(patient70, PainSurveyScheduler.NERVEBLOCK,  12);
    int emailsSent1 = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals("Expecting a follow up NerveBlock survey registration to be created and one email sent",
        1, emailsSent1);

    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting a follow up " + PainSurveyScheduler.NERVEBLOCK +
            " registration to be created as well as the trigger", 2, registrationList.size());
  }

  public void testStopSendNerveBlockFollowUps() throws Exception {
    initFUPatient(patient70);
    ApptRegistration parent = createTriggerRegistration(patient70, PainSurveyScheduler.NERVEBLOCK,  26);
    int emailsSent1 = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals("Expecting no follow up " + PainSurveyScheduler.NERVEBLOCK +
            " registrations to be created and no email sent", 0, emailsSent1);

    ApptRegistration followUp = createTriggerRegistration(patient70, PainSurveyScheduler.NERVEBLOCK,  12);
    surveyRegAttrDao.setAttribute(followUp.getSurveyRegList().get(0).getSurveyRegId(), "PARENT",
        parent.getSurveyRegList().get(0).getSurveyRegId().toString());
    emailsSent1 = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals("Expecting no more follow up " + PainSurveyScheduler.NERVEBLOCK +
        " registrations to be created and no email sent", 0, emailsSent1);

    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting no more follow up " + PainSurveyScheduler.NERVEBLOCK +
        " registration to be created ", 2, registrationList.size());
  }

  public void testSendSCSFollowUp1() throws Exception {
    // SCS Trial Three times; 3 days (sent manually) AND 5 AND 7 days after so followups are 2 and 4 days after the initial
    initFUPatient(patient70);
    createTriggerRegistration(patient70, PainSurveyScheduler.SCS,  2);
    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting one " + PainSurveyScheduler.SCS + " registration to have been created ", 1,
        registrationList.size());

    int emailsSent = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals("Expecting another " + PainSurveyScheduler.SCS +
        " registration to be created and 2 SCS emails to be sent", 2, emailsSent);

    registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting no more follow up " + PainSurveyScheduler.SCS + " registration to be created ",
        2, registrationList.size());
  }

  public void testSendSCSFollowUp2() throws Exception {
    // SCS Trial Three times; 3 days (sent manually) AND 5 AND 7 days after so followups are 2 and 4 days after the initial
    initFUPatient(patient70);
    createTriggerRegistration(patient70, PainSurveyScheduler.SCS,  4);
    createTriggerRegistration(patient70, PainSurveyScheduler.SCS, "SCSFU", 2);

    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting one " + PainSurveyScheduler.SCS + " registration to have been created ", 2,
        registrationList.size());

    int emailsSent = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals("Expecting another " + PainSurveyScheduler.SCS +
        " registration to be created and 2 emails to be sent", 2, emailsSent);

    registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting no more follow up " + PainSurveyScheduler.SCS + " registration to be created ",
        3, registrationList.size());
  }

  // Peripheral Nerve Stim: First one 2 weeks (sent manually), test sending second at 4 weeks
  public void testSendPeriNerve1FollowUps() throws Exception {
    initFUPatient(patient70);
    createTriggerRegistration(patient70, PainSurveyScheduler.PERINERVE,  14);
    int emailsSent1 = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals("Expecting a follow up Peripheral Nerve survey registration to be created and one email sent",
        1, emailsSent1);

    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting a follow up " + PainSurveyScheduler.PERINERVE +
        " registration to be created as well as the trigger", 2, registrationList.size());
  }

  // Peripheral Nerve Stim: test sending 3 months after implant
  public void testSendPeriNerve2FollowUps() throws Exception {
    initFUPatient(patient70);
    ApptRegistration parent = createTriggerRegistration(patient70, PainSurveyScheduler.PERINERVE,  76);
    ApptRegistration second = createTriggerRegistration(patient70, PainSurveyScheduler.PERINERVE,  14);
    surveyRegAttrDao.setAttribute(second.getSurveyRegList().get(0).getSurveyRegId(), "PARENT",
        parent.getSurveyRegList().get(0).getSurveyRegId().toString());
    int emailsSent1 = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals("Expecting a follow up " + PainSurveyScheduler.PERINERVE +
        " registration to be created and one email sent", 1, emailsSent1);

    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), -1234).getTime()));
    assertEquals("Expecting a follow up " + PainSurveyScheduler.PERINERVE +
        "registration to be created as well as the trigger", 3, registrationList.size());
  }

  public void testStoppingPeriNerveFollowUps() throws Exception {
    initFUPatient(patient70);
    ApptRegistration parent = createTriggerRegistration(patient70, PainSurveyScheduler.PERINERVE,  365 * 5 + 8);
    ApptRegistration second = createTriggerRegistration(patient70, PainSurveyScheduler.PERINERVE, "PNSFU", 14);
    surveyRegAttrDao.setAttribute(second.getSurveyRegList().get(0).getSurveyRegId(), "PARENT",
        parent.getSurveyRegList().get(0).getSurveyRegId().toString());
    int emailsSent1 = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    assertEquals("Expecting no follow up " + PainSurveyScheduler.PERINERVE +
        " registration to be created past the 5 years and therefore no email sent", 0, emailsSent1);

    ArrayList<PatientRegistration> registrationList = assessDao.getPatientRegistrations(patient70.getPatientId(),
        new Date(DateUtils.getDaysOutDate(getSiteInfo(), 365 * -6).getTime()));
    assertEquals("Expecting only the 2 trigger " + PainSurveyScheduler.PERINERVE + " registrations", 2,
        registrationList.size());
  }


  /*
   * Creates a ProcBotox registration dated back the number of days that triggers sending a follow-up
   */
  private ApptRegistration createBotoxTriggerRegistration(Patient testPatient) {
    /*
     * Make them a survey dated back to the date that triggers sending follow ups
     */
    AppConfigEntry configEntry = appConfigDao.findAppConfigEntry(getSiteInfo().getSiteId(), "configparam", "SendProcBotox");
    String sendDaysString = configEntry != null ? configEntry.getConfigValue() : "42";
    String initDaysString = getSiteInfo().getProperty("appointment.initialemail.daysout");
    int procPushDays = Integer.parseInt(initDaysString) + Integer.parseInt(sendDaysString);

    Date firstSurveyDt = initialDaysOut(procPushDays * -1);
    ApptRegistration apptRegistration = addBotoxRegistration(testPatient.getPatientId(), firstSurveyDt);
    apptRegistration = assessDao.getPatientRegistrationByRegId(apptRegistration.getApptId());
    assertNotNull("Expecting to find the registration", apptRegistration);
    return apptRegistration;
  }

  /*
   * Creates a registration dated back the number of days given for the processType specified
   */
  private ApptRegistration createTriggerRegistration(Patient testPatient, String processType, int procPushDays) {
    return createTriggerRegistration(testPatient, processType,
        XMLFileUtils.getInstance(getSiteInfo()).getAttribute(processType, "visitType"), procPushDays);
  }

  /*
   * Creates a registration dated back for the processType, and days specified,
   * overriding the visittype listed in process.xml with the one provided
   */
  private ApptRegistration createTriggerRegistration(Patient testPatient, String processType, String visitType, int procPushDays) {
    Date firstSurveyDt = initialDaysOut(procPushDays * -1);
    ApptRegistration apptRegistration = addRegistration(testPatient.getPatientId(), firstSurveyDt, processType, visitType);
    apptRegistration = assessDao.getPatientRegistrationByRegId(apptRegistration.getApptId());
    assertNotNull("Expecting to find the registration", apptRegistration);
    return apptRegistration;
  }

  private void addEmailAttribute(Patient patient) {
    AdministrativeServices adminSvc = getAdminService(user);
    String id = patient.getPatientId();
    PatientAttribute patAttribute =
        new PatientAttribute(id, Constants.ATTRIBUTE_SURVEYEMAIL, emailAddress, PatientAttribute.STRING);
    adminSvc.addPatientAttribute(patAttribute);
  }

  private Patient createPatient(String idea, String firstName, String lastName) {
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    Patient testPatient = new Patient(idea, firstName, lastName, new Date(dob.getTime()));
    testPatient = patientDao.addPatient(testPatient);
    return testPatient;
  }

  private Date initialDaysOut(int plusDays) {
    String dayString = getSiteInfo().getProperty("appointment.initialemail.daysout");
    int days = Integer.parseInt(dayString);
    return new Date(DateUtils.getDaysOutDate(getSiteInfo(), days + plusDays).getTime());
  }

  private ApptRegistration addBotoxRegistration(String mrn, Date surveyDt) {
    String surveyType = "ProcBotox";
    String processType = XMLFileUtils.getInstance(getSiteInfo()).getActiveProcessForName(surveyType, surveyDt);
    if (processType == null) {
      processType = XMLFileUtils.getInstance(getSiteInfo()).getActiveProcessForName(surveyType, new Date()); // test dates may predate V1
    }
    String visitType = XMLFileUtils.getInstance(getSiteInfo()).getAttribute(processType, "visitType");
    ApptRegistration registration = getScheduledRegistration(mrn, surveyDt, processType, visitType);
    return regUtils.createRegistration(assessDao, registration);
  }

  private ApptRegistration addRegistration(String mrn, Date surveyDt, String surveyType, String visitType) {
    String processType = XMLFileUtils.getInstance(getSiteInfo()).getActiveProcessForName(surveyType, surveyDt);
    if (processType == null) {
      processType = XMLFileUtils.getInstance(getSiteInfo()).getActiveProcessForName(surveyType, new Date()); // test dates may predate V1
    }

    ApptRegistration registration = getScheduledRegistration(mrn, surveyDt, processType, visitType);
    return regUtils.createRegistration(assessDao, registration);
  }

  private ApptRegistration getScheduledRegistration(String mrn, Date apptTime, String surveyType,
                                          String visitCode) {
    ApptRegistration registration = new ApptRegistration(getSiteInfo().getSiteId(), mrn, apptTime, null,
        surveyType, Constants.REGISTRATION_TYPE_STANDALONE_SURVEY, visitCode);
    registration.setSendEmail(true);
    return registration;
  }

  private AdministrativeServices getAdminService(User user) {
    SiteInfo siteInfo = getSiteInfo();
    return new AdministrativeServicesImpl(user, databaseProvider, serverContext(), new MySiteInfo(getDatabaseProvider(), serverContext.getSitesInfo(), siteInfo));
  }

  private void initFUPatient(Patient patient) {
    adminSvc.setPatientAgreesToSurvey(patient);
    PatientAttribute attribute0 = new PatientAttribute(patient.getPatientId(), Constants.ATTRIBUTE_SURVEYEMAIL,
        "testme@fusurvey.edu");
    adminSvc.addPatientAttribute(attribute0);
  }
}

class MySiteInfo extends SiteInfo {
  private static final Logger mylogger = Logger.getLogger(MySiteInfo.class);

  public MySiteInfo(DatabaseProvider databaseProvider, SitesInfo sitesInfo, SiteInfo siteInfo) {
    super(siteInfo.getSiteId(), siteInfo.getUrlParam(), siteInfo.getDisplayName(), true);
    initSiteConfig(databaseProvider, sitesInfo, sitesInfo.getGlobalPropertyMap().getMap(), siteInfo.getProperties(),
        siteInfo.getEmailTemplates(), new HashMap<>());

  }

  public RegistryCustomizer getRegistryCustomizer() {
    ClassCreator<RegistryCustomizer> customizerCreator =
        new ClassCreator<RegistryCustomizer>("RegistryCustomizerFactory.create", "RegistryCustomizer", mylogger, SiteInfo.class)
            .check("edu.stanford.registry.server.PainManagementCustomizer")
            .check("edu.stanford.registry.server.RegistryCustomizerDefault");
    return customizerCreator.createClass("edu.stanford.registry.server.PainManagementCustomizer", this);
  }

}

