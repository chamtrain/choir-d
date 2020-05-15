package edu.stanford.registry.server.database;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.service.EmailMonitor;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Notification;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.PatientRegistrationSearch;
import edu.stanford.registry.shared.PatientResSurveyRegLinkList;
import edu.stanford.registry.shared.PatientResult;
import edu.stanford.registry.shared.PatientResultType;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.Utils;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssessDaoTest extends DatabaseTestCase {
  private static final Logger logger = LoggerFactory.getLogger(AssessDaoTest.class);
  private final ApptId otherApptId = new ApptId(12345L);
  private final String patientId = "8888888-8";
  private final String patientId2 = "999999999";
  private final String surveyLink = "https://outcomes.stanford.edu";
  private final String emailAddress = "testing@test.stanford.edu";
  private final Date today = DateUtils.getDaysOutDate(getSiteInfo(),0);
  private final Date yesterday = DateUtils.getDaysAgoDate(getSiteInfo(), 1);
  private AssessDao assessDao;
  private Patient testPatient0, testPatient2;
  private ApptRegistration apptRegistration, apptRegistration2;
  private Notification notification;
  private SurveyRegUtils regUtils;
  private Utils utils;
  private User user;

  @Override
  protected void postSetUp() {
    assessDao = new AssessDao(databaseProvider.get(), getSiteInfo());
    utils = new Utils(databaseProvider.get(), getSiteInfo());
    user = utils.getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId(), user);
    testPatient0 = new Patient(patientId, "John", "Doe", new Date(DateUtils.getDaysAgoDate(getSiteInfo(), 35 * 365).getTime()));
    testPatient0 = patientDao.addPatient(testPatient0);
    testPatient2 = new Patient(patientId2, "Jane", "Smith", new Date(DateUtils.getDaysAgoDate(getSiteInfo(),33 * 365 ).getTime()));
    testPatient2 = patientDao.addPatient(testPatient2);
    PatientAttribute patientAttribute = new PatientAttribute(testPatient0.getPatientId(), Constants.ATTRIBUTE_PARTICIPATES, "y");
    testPatient0.addAttribute(patientAttribute);
    patientDao.insertAttribute(patientAttribute);
    apptRegistration = utils.addInitialRegistration(databaseProvider.get(), patientId, today, emailAddress, "NPV");
    apptRegistration2 = utils.addInitialRegistration(databaseProvider.get(),patientId, yesterday , emailAddress, "RPV30");
    regUtils = new SurveyRegUtils(getSiteInfo());
    notification = createNotification();
  }

  public void test_insertNotifications() {
    int notificationCount = assessDao.getNotificationCount(testPatient0.getPatientId(), DateUtils.getDaysOutDate(-5), DateUtils.getDaysOutDate(5), otherApptId);
    assertEquals(0, notificationCount);
    insertNotification();
    notificationCount = assessDao.getNotificationCount(testPatient0.getPatientId(), DateUtils.getDaysOutDate(-5), DateUtils.getDaysOutDate(5), otherApptId);
    assertEquals(1, notificationCount);
  }

  public void test_deleteNotification() {
    insertNotification();
    int notificationCount = assessDao.getNotificationCount(testPatient0.getPatientId(), DateUtils.getDaysOutDate(-5), DateUtils.getDaysOutDate(5), otherApptId);
    assertEquals(1, notificationCount);
    assessDao.deleteNotifications(notification.getAssessmentId());
    notificationCount = assessDao.getNotificationCount(testPatient0.getPatientId(), DateUtils.getDaysOutDate(-5), DateUtils.getDaysOutDate(5), otherApptId);
    assertEquals(0, notificationCount);
  }

  public void test_GetUnsentNotifications() {
    insertNotification();
    ArrayList<Notification> list = assessDao.getUnsentNotifications(notification.getAssessmentId());
    assertNotNull(list);
    assertEquals(1, list.size());
  }

  public void test_GetSentNotifications() {
    insertNotification();
    EmailMonitor monitor = new EmailMonitor(getSiteInfo().getMailer(), databaseProvider, surveyLink, getSiteInfo());
    monitor.sendEmail(getInitialTemplates(), DateUtils.getDaysFromDate(apptRegistration.getSurveyDt(), 1), apptRegistration.getAssessmentId());

    ArrayList<Notification> list = assessDao.getSentNotifications(notification.getAssessmentId());
    assertNotNull(list);
    assertEquals(1, list.size());
  }

  public void test_getNotificationsByPatient() {
    insertNotification();
    ArrayList<Notification> list = assessDao.getNotificationsByPatient(testPatient0.getPatientId());
    assertNotNull(list);
    assertEquals(1, list.size());
  }

  public void test_getPendingNotifications() {
    notification.setSurveyDt(DateUtils.getDateEnd(today));
    insertNotification();
    SurveyRegUtils utils = new SurveyRegUtils(getSiteInfo());
    utils.registerAssessments(databaseProvider.get(), apptRegistration.getAssessment(), user);
    ArrayList<Notification> list = assessDao.getPendingNotifications();
    assertNotNull(list);
    assertEquals(1, list.size());
  }

  public void test_tempEmailMonitorPending() {
    notification.setSurveyDt(DateUtils.getDateEnd(today));
    insertNotification();
    SurveyRegUtils utils = new SurveyRegUtils(getSiteInfo());
    utils.registerAssessments(databaseProvider.get(), apptRegistration.getAssessment(), user);
    EmailMonitor monitor = new EmailMonitor(getSiteInfo().getMailer(), databaseProvider, surveyLink, getSiteInfo());
    int emailsSent = monitor.handlePendingNotifications(getInitialTemplates(), DateUtils.getDaysFromDate(new Date(), 4));

    assertEquals(1, emailsSent);
  }
  public void test_insertPatientResult() {
    SiteInfo site1Info =  serverContext.getSitesInfo().getBySiteId(1L);
    AssessDao assessDao1 = new AssessDao(databaseProvider.get(), site1Info);
    PatientResultType resultType = assessDao1.getPatientResultType("PARPTJSON");
    AssessmentRegistration assessmentRegistration = createTestAssessmentRegistration(assessDao1);

    PatientResult result = new PatientResult();
    result.setAssessmentRegId(assessmentRegistration.getAssessmentId().getId());
    result.setSurveySiteId(getSiteInfo().getSiteId());
    result.setDocumentControlId("TESTING123");
    result.setPatientResTypId(resultType.getPatientResTypId());
    result.setPatientResVs(1L);

    String testString = "abcdefg";
    result.setResultBlob(testString.getBytes());
    result = assessDao1.insertPatientResult(result);
    assertNotNull("should find result", result);
    assertNotNull("should have a patient result id", result.getPatientResId());
  }

  public void test_insertPatientResSurveyRegLink() {
    SiteInfo site1Info =  serverContext.getSitesInfo().getBySiteId(1L);
    AssessDao assessDao1 = new AssessDao(databaseProvider.get(), site1Info);
    AssessmentRegistration assessmentRegistration = createTestAssessmentRegistration(assessDao1);

    PatientResultType resultType = assessDao1.getPatientResultType("PARPTJSON");

    PatientResult result = new PatientResult();
    result.setAssessmentRegId(assessmentRegistration.getAssessmentId().getId());
    result.setSurveySiteId(getSiteInfo().getSiteId());
    result.setDocumentControlId("TESTING123");
    result.setPatientResTypId(resultType.getPatientResTypId());
    result.setPatientResVs(1L);
    result.setResultBlob("abcdefg".getBytes());
    result = assessDao1.insertPatientResult(result);
    PatientResSurveyRegLinkList list =new PatientResSurveyRegLinkList(getSiteInfo().getSiteId(), result.getPatientResId());
    list.addRelationship(assessmentRegistration.getSurveyRegList().get(0).getSurveyRegId());
    assessDao1.insertPatientResSurveyRegLink(list.getRelationship(0));
  }

  public void test_getPatientResultTyp() {
    SiteInfo site1Info =  serverContext.getSitesInfo().getBySiteId(1L);
    AssessDao assessDao1 = new AssessDao(databaseProvider.get(), site1Info);
    PatientResultType resultType = assessDao1.getPatientResultType("PARPTJSON");
    assertNotNull("Expecting to find a resulttype for PARPTJSON", resultType);
    assertNotNull("Expecting a type id for PARPTJSON", resultType.getPatientResTypId());
  }

  public void test_getCompletedRegistrations() {
    createCompletedRegistrations();
    ArrayList<ApptRegistration> registrations = assessDao.getCompletedRegistrations();
    assertNotNull(registrations);
    assertEquals("Should get two", registrations.size(),2);
    Calendar cal = Calendar.getInstance();
    Date oneWeekBack = new Date();
    cal.setTime(oneWeekBack);
    cal.add(Calendar.DAY_OF_YEAR, -12);
    oneWeekBack.setTime(cal.getTimeInMillis());

    registrations = assessDao.getCompletedRegistrations(oneWeekBack, yesterday);
    assertNotNull(registrations);
    assertEquals("Should get one", 1, registrations.size());
  }

  public void test_getLastCompletedRegistration() { // 363
    createCompletedRegistrations();
    ApptRegistration apptRegistration = assessDao.getLastCompletedRegistration(patientId, today);
    assertNotNull("Expected appt registration ", apptRegistration);
    assertEquals("Expected yesterdays survey",  yesterday.getTime(), apptRegistration.getSurveyDt().getTime());
  }

  public void test_getLastCompletedRegistrationBeforeThis() { //381
    createCompletedRegistrations();
    ApptRegistration apptRegistration = assessDao.getLastCompletedRegistrationBeforeThis(patientId, today);
    assertNotNull("Expected appt registration ", apptRegistration);
    assertEquals("Expected yesterdays survey", apptRegistration.getSurveyDt().getTime(), yesterday.getTime());
  }

  public void test_getCompletedRegistrationsByPatient() { //395
    createCompletedRegistrations();
    List<ApptRegistration> apptRegistrations = assessDao.getCompletedRegistrationsByPatient(patientId);
    assertNotNull("Expected appt registration array", apptRegistrations);
    assertEquals("Expected two appt registrations", 2, apptRegistrations.size());
  }

  public void test_getApptRegistrationByRegId() { //449
    ApptRegistration apptRegistration = registerRegistrations().get(0);
    ApptRegistration result = assessDao.getApptRegistrationByRegId(apptRegistration.getApptId());
    assertNotNull("Expected appt registration ", result);
    assertEquals("ids should match", apptRegistration.getApptId().getId(), result.getApptId().getId());
  }

  public void test_getApptRegistrationByAssessmentId () {
    List<ApptRegistration> apptRegistrationList = registerRegistrations();
    ApptRegistration apptRegistration = assessDao.getApptRegistrationByAssessmentId(
        apptRegistrationList.get(0).getAssessmentId());
    assertNotNull("Expecting the apptRegistration for this assessment", apptRegistration);
    assertEquals("Assessment id's should match", apptRegistrationList.get(0).getAssessmentId().getId(),
        apptRegistration.getAssessmentId().getId());
    assertEquals("Assessment reg id's should match", apptRegistrationList.get(0).getAssessmentRegId(),
        apptRegistration.getAssessmentRegId());
  }

  public void test_getApptRegistrationBySurveyRegId () { //469
    List<ApptRegistration> apptRegistrationList = registerRegistrations();
    ApptRegistration apptRegistration = assessDao.getApptRegistrationBySurveyRegId(
        apptRegistrationList.get(0).getSurveyReg("Default").getSurveyRegId());
    assertNotNull("Expected to find an apptRegistration for this survey reg id", apptRegistration);
    assertEquals("Expected to find the apptRegistration for this survey reg id",
        apptRegistrationList.get(0).getSurveyReg("Default").getSurveyRegId(),
        apptRegistration.getSurveyReg("Default").getSurveyRegId());
  }

  public void test_getApptRegistrationByPatientAndDate () {
    registerRegistrations();
    ArrayList<ApptRegistration> apptRegistrations = assessDao.getApptRegistrationByPatientAndDate(patientId, today);
    assertNotNull(apptRegistrations);
    assertEquals("Expected to find one appt registration",  1, apptRegistrations.size());
  }

  public void test_getApptRegistrationByEncounterId() {
    List<ApptRegistration> apptRegistrations = registerRegistrations();
    for (ApptRegistration apptRegistration : apptRegistrations) {
      apptRegistration.setEncounterEid("encounterId" + apptRegistration.getSurveyReg("Default").getToken());
      assessDao.updateApptRegistration(apptRegistration);
    }
    ApptRegistration result = assessDao.getApptRegistrationByEncounterId(apptRegistrations.get(1).getEncounterEid());
    assertNotNull("Expected to find an ApptRegistration", result);
    assertEquals("Expected the ApptRegistration sought", result.getEncounterEid(),
        apptRegistrations.get(1).getEncounterEid());
  }

  public void test_insertApptRegistration() {
    ApptRegistration apptRegistration = new ApptRegistration(getSiteInfo().getSiteId(), patientId2, today, "emaii.edu",
        regUtils.getVisitType(1, today), Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT, "RPV30");
    assertNull("Expect null ApptRegId before insert", apptRegistration.getApptRegId());
    apptRegistration = assessDao.insertApptRegistration(apptRegistration);
    assertNotNull("Expect not null ApptRegId after insert", apptRegistration.getApptRegId());
  }

  public void test_updateApptRegistration() {
    ApptRegistration apptRegistration = new ApptRegistration(getSiteInfo().getSiteId(), patientId2, today, "emaii.edu",
        regUtils.getVisitType(1, today), Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT, "RPV30");
    apptRegistration = assessDao.insertApptRegistration(apptRegistration);
    apptRegistration.setVisitType("PSY");
    assessDao.updateApptRegistration(apptRegistration);
    ApptRegistration result = assessDao.getApptRegistrationByRegId(apptRegistration.getApptId());
    assertEquals(apptRegistration.getApptId().getId(), result.getApptId().getId());
    assertEquals(apptRegistration.getVisitType(), result.getVisitType());
    assertEquals("PSY", result.getVisitType());
  }

  public void test_deleteApptRegistration() {
    List<ApptRegistration> apptRegistrations = registerRegistrations();
    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(getSiteInfo());
    surveyRegUtils.deleteRegistration(databaseProvider.get(), apptRegistrations.get(0));
    ApptRegistration result0 = assessDao.getApptRegistrationByAssessmentId(apptRegistrations.get(0).getAssessmentId());
    assertNull("Expecting not to find the deleted apptRegistration", result0);
    ApptRegistration result1 = assessDao.getApptRegistrationByAssessmentId(apptRegistrations.get(1).getAssessmentId());
    assertNotNull("Expected to find the other apptRegistration ", result1);
  }

  private static final String[] registrationTypes = new String[]
      { Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT,
          Constants.REGISTRATION_TYPE_STANDALONE_SURVEY };

  public void test_getPatientRegistrationsByDates() {
    PatientRegistrationSearch searchOptions = new PatientRegistrationSearch();
    searchOptions.setOption(PatientRegistrationSearch.CONSENTED);
    searchOptions.setOption(PatientRegistrationSearch.UNOTIFIED);

    ArrayList<PatientRegistration> registrations = assessDao.getPatientRegistrations(yesterday, today,
        Arrays.asList(registrationTypes), searchOptions);
    assertNotNull("Expecting registrations array", registrations);
    assertEquals("Should have two registrations",  2, registrations.size());

    ArrayList<PatientRegistration> allRegistrations = assessDao.getPatientRegistrations(yesterday, today,
        searchOptions);
    assertNotNull("Expecting allRegistrations array", allRegistrations);
    assertEquals("Should have two registrations in allRegistrations",  2, allRegistrations.size());

    ArrayList<PatientRegistration> cancelledRegistrations = assessDao.getPatientRegistrations(yesterday, today,
        Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT , searchOptions);
    assertNotNull("Expecting cancelledRegistrations array", cancelledRegistrations);
    assertEquals("Should have no registrations in cancelledRegistrations",  0, cancelledRegistrations.size());

    ArrayList<PatientRegistration> activeRegistrations = assessDao.getPatientRegistrations(yesterday, today,
        Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT , searchOptions);
    assertNotNull("Expecting activeRegistrations array", activeRegistrations);
    assertEquals("Should have two registrations in activeRegistrations",  2, activeRegistrations.size());
  }

  public void test_getPatientRegistrationsBySearchOptions() {
    utils.addInitialRegistration(databaseProvider.get(), patientId2, today, emailAddress, "NPV");
    PatientRegistrationSearch searchOptions = new PatientRegistrationSearch();
    searchOptions.setOption(PatientRegistrationSearch.CONSENTED);
    searchOptions.setOption(PatientRegistrationSearch.UNOTIFIED);

    ArrayList<PatientRegistration> registrations = assessDao.getPatientRegistrations(patientId2, searchOptions);
    assertNotNull("Expecting registrations array", registrations);
    assertEquals("Should have one registrations", 1, registrations.size());
  }

  public void test_getRegistrationsByType() {
    ArrayList<PatientRegistration> registrations = assessDao.getPatientRegistrationsByType(patientId,
        Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT);
        //regUtils.getVisitType(0, new Date(today.getTime())));
    assertNotNull("Expecting registrations array", registrations);
    assertEquals("Should have two active registrations", 2, registrations.size());
  }

  public void test_getRegistrationsByRegId() {
    PatientRegistration registration = assessDao.getPatientRegistrationByRegId(apptRegistration.getApptId());
    assertNotNull("Expecting registration", registration);
    assertEquals("Should be for the same patient", apptRegistration.getPatientId(), registration.getPatientId());
    assertEquals("Should be for the same apptRegId", apptRegistration.getApptId().getId(), registration.getApptId().getId());
  }

  public void test_getRegistrationsByAssessmentId() {
    PatientRegistration registration = assessDao.getPatientRegistrationByAssessmentId(apptRegistration2.getAssessmentId());
    assertNotNull("Expecting registration", registration);
    assertEquals("Should be for the same patient", apptRegistration2.getPatientId(), registration.getPatientId());
    assertEquals("Should be for the same apptRegId", apptRegistration2.getAssessmentRegId(), registration.getAssessmentRegId());
  }

  public void test_getRegistrations() {
    ArrayList<PatientRegistration> registrations = assessDao.getPatientRegistrations(apptRegistration.getPatientId(), DateUtils.getDaysFromDate(getSiteInfo(),today, -1));
    assertNotNull("Expecting registrations array", registrations);
    assertEquals("Should have one registrations", 1, registrations.size());

    registrations = assessDao.getPatientRegistrations(apptRegistration.getPatientId(), DateUtils.getDaysFromDate(getSiteInfo(), yesterday, -1));
    assertNotNull("Expecting registrations array", registrations);
    assertEquals("Should have two registrations", 2, registrations.size());
  }

  private Notification createNotification() {
    return notification = new Notification(testPatient0.getPatientId(), apptRegistration.getAssessmentId(),
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

  private AssessmentRegistration createTestAssessmentRegistration(AssessDao assessDao1 ) {
    Date now = new Date();
    String type = "Initial";
    AssessmentRegistration registration = new AssessmentRegistration();
    SurveyRegistration surveyRegistration = new SurveyRegistration(getSiteInfo().getSiteId(), patientId, now, type);
    //surveyRegistration.setToken("1234567890");
    surveyRegistration.setToken(String.valueOf(System.currentTimeMillis()));
    surveyRegistration.setSurveyName("default");
    surveyRegistration.setSurveyOrder(1L);

    registration.setSurveySiteId(getSiteInfo().getSiteId());
    registration.addSurveyReg(surveyRegistration);
    registration.setPatientId(patientId);
    registration.setAssessmentDt(now);
    registration.setAssessmentType(type);
    registration = assessDao1.insertAssessmentRegistration(registration);
    surveyRegistration.setAssessmentRegId(registration.getAssessmentRegId());
    surveyRegistration = assessDao1.insertSurveyRegistration(surveyRegistration);
    registration.getSurveyRegList().get(0).setSurveyRegId(surveyRegistration.getSurveyRegId());
    return registration;
  }

  private void createCompletedRegistrations() {
    List<ApptRegistration> apptRegistrationList = registerRegistrations();
    SurveyRegistration surveyRegistration1 = apptRegistrationList.get(0).getSurveyReg("Default");
    SurveyRegistration surveyRegistration2 = apptRegistrationList.get(1).getSurveyReg("Default");
    PatStudyDao studyDao = new PatStudyDao(databaseProvider.get(), getSiteInfo());
    updatePatientStudy(studyDao, surveyRegistration1.getToken());
    updatePatientStudy(studyDao, surveyRegistration2.getToken());

    SurveyDao surveyDao = new SurveyDao(databaseProvider.get());
    surveyDao.createSurveyToken(getNewToken(surveyRegistration1.getToken()));
    surveyDao.createSurveyToken(getNewToken(surveyRegistration2.getToken()));
  }

  private List<ApptRegistration> registerRegistrations() {
    SurveyRegUtils utils = new SurveyRegUtils(getSiteInfo());
    utils.registerAssessments(databaseProvider.get(), apptRegistration.getAssessment(), user);
    utils.registerAssessments(databaseProvider.get(), apptRegistration2.getAssessment(), user);
    ArrayList<ApptRegistration> apptRegistrationArrayList = new ArrayList<>();
    apptRegistrationArrayList.add(apptRegistration);
    apptRegistrationArrayList.add(apptRegistration2);
    return apptRegistrationArrayList;
  }

  private void updatePatientStudy(PatStudyDao patStudyDao, String token) {
    SurveySystDao ssDao = new SurveySystDao(databaseProvider.get());
    Study bodymapStudy =ssDao.getStudy(ssDao.getSurveySystem("Local").getSurveySystemId(), "bodymap");
    PatientStudy patientStudy = patStudyDao.getPatientStudy(testPatient0, bodymapStudy, new Token(token));
    patStudyDao.setPatientStudyContents(patientStudy, "<Form></Form>");
  }

  private SurveyToken getNewToken(String tokenId) {
    SurveyToken token = new SurveyToken();
    token.setLastStepNumber(1L);
    token.setSurveySiteId(getSiteInfo().getSiteId());
    token.setSurveyToken(tokenId);
    token.setComplete(true);
    token.setSessionToken("SessionToken" + tokenId + today.getTime());
    token.setResumeToken("ResumeToken" + tokenId + today.getTime());
    return token;
  }
}
