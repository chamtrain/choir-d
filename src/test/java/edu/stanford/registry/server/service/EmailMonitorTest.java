package edu.stanford.registry.server.service;

import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.EmailSendStatus;
import edu.stanford.registry.shared.Notification;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailMonitorTest extends DatabaseTestCase {
  private static final Logger logger = LoggerFactory.getLogger(EmailMonitorTest.class);
  private final String patientId = "8888888-8";
  private final String surveyLink = "https://outcomes.stanford.edu";
  private final String emailAddress = "testing@test.stanford.edu";
  private final Date today = DateUtils.getDaysOutDate(0);
  private AssessDao assessDao;
  private Patient testPatient0;
  private ApptRegistration apptRegistration;
  private Notification notification;
  private Utils utils;
  private User user;
  private PatientDao patientDao;

  @Override
  protected void postSetUp() {
    assessDao = new AssessDao(databaseProvider.get(), getSiteInfo());
    utils = new Utils(databaseProvider.get(), getSiteInfo());
    user = utils.getUser(databaseProvider, serverContext.getSitesInfo(), "admin");


    patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId(), user);
    testPatient0 = new Patient(patientId, "John", "Doe", new Date(DateUtils.getDaysAgoDate(35 * 365).getTime()));
    testPatient0 = patientDao.addPatient(testPatient0);
    PatientAttribute patientAttribute = new PatientAttribute(testPatient0.getPatientId(), Constants.ATTRIBUTE_PARTICIPATES, "y");
    testPatient0.addAttribute(patientAttribute);
    patientDao.insertAttribute(patientAttribute);
    apptRegistration = utils.addInitialRegistration(databaseProvider.get(), patientId, today, emailAddress, "NPV");

    notification = createNotification();
  }

  public void test_sendEmail_valid() {
    insertNotification();
    EmailMonitor monitor = new EmailMonitor(getSiteInfo().getMailer(), databaseProvider, surveyLink, getSiteInfo());
    ArrayList<EmailSendStatus> sendStatuses = monitor.sendEmail(getInitialTemplates(), DateUtils.getDaysFromDate(apptRegistration.getSurveyDt(), 1), apptRegistration.getAssessmentId());
    assertNotNull(sendStatuses);
    assertEquals(1, sendStatuses.size());
    assertEquals(EmailSendStatus.sent, sendStatuses.get(0));

  }

  public void test_sendEmail_invalidemail() {
    insertNotification();
    PatientAttribute invalidEmailAttribute = new PatientAttribute(testPatient0.getPatientId(), Constants.ATTRIBUTE_SURVEYEMAIL_VALID, "n");
    EmailMonitor monitor = new EmailMonitor(getSiteInfo().getMailer(), databaseProvider, surveyLink, getSiteInfo());
    testPatient0.addAttribute(invalidEmailAttribute);
    patientDao.insertAttribute(invalidEmailAttribute);
    ArrayList<EmailSendStatus> sendStatuses = monitor.sendEmail(getInitialTemplates(), DateUtils.getDaysFromDate(apptRegistration.getSurveyDt(), 1), apptRegistration.getAssessmentId());
    assertNotNull(sendStatuses);
    assertEquals(1, sendStatuses.size());
    assertEquals(EmailSendStatus.invalid_email_addr, sendStatuses.get(0));
  }

  public void test_sendEmail_not18() {
    insertNotification();

    EmailMonitor monitor = new EmailMonitor(getSiteInfo().getMailer(), databaseProvider, surveyLink, getSiteInfo());
    testPatient0.setDtBirth(DateUtils.getDaysAgoDate(15 * 365));
    patientDao.updatePatient(testPatient0);
    ArrayList<EmailSendStatus> sendStatuses = monitor.sendEmail(getInitialTemplates(), DateUtils.getDaysFromDate(apptRegistration.getSurveyDt(), 1), apptRegistration.getAssessmentId());
    assertNotNull(sendStatuses);
    assertEquals(1, sendStatuses.size());
    assertEquals(EmailSendStatus.not_18, sendStatuses.get(0));
  }


  public void test_handlePendingNotifications() {
    notification.setSurveyDt(DateUtils.getDateEnd(today));
    insertNotification();
    SurveyRegUtils utils = new SurveyRegUtils(getSiteInfo());
    utils.registerAssessments(databaseProvider.get(), apptRegistration.getAssessment(), user);
    EmailMonitor monitor = new EmailMonitor(getSiteInfo().getMailer(), databaseProvider, surveyLink, getSiteInfo());
    int emailsSent = monitor.handlePendingNotifications(getInitialTemplates(), DateUtils.getDaysFromDate(new Date(), 4));

    assertEquals(1, emailsSent);
  }

  private Notification createNotification() {
    return new Notification(testPatient0.getPatientId(), apptRegistration.getAssessmentId(),
        apptRegistration.getAssessmentType(), apptRegistration.getSurveyDt(), apptRegistration.getMetaVersion(),
        apptRegistration.getSurveySiteId());
  }

  private void insertNotification() {
    assessDao.insertNotification(notification);
  }


  private HashMap<String, String> getInitialTemplates() {
    HashMap<String, String> initialTemplates = getSiteInfo().getInitialTemplates();
    if (!initialTemplates.containsKey("Initial")) {
      initialTemplates.put("Initial", "Subject\nBody\n");
    }
    if (!initialTemplates.containsKey("Initial-reminder")) {
      initialTemplates.put("Initial-reminder", "Subject\nBody\n");
    }
    return initialTemplates;
  }
}
