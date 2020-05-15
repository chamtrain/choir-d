package edu.stanford.registry.server.database;

import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

  /**
   * Unit tests for the API for accessing / updating activities
   */

public class ActivityDbTest extends DatabaseTestCase {

    private static Logger logger = Logger.getLogger(ActivityDbTest.class);
    Calendar nowCal = Calendar.getInstance();

    ActivityDao activityDao;
    Utils utils;
    User user;
    String patientId = "8888888-8";
    Patient testPatient0;
    ApptRegistration registration;
    Activity activity;

    @Override
    protected void postSetUp() throws Exception {
      utils = new Utils(databaseProvider.get(), getSiteInfo());
      user = utils.getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
      activityDao = new ActivityDao(databaseProvider.get(), getSiteInfo().getSiteId());
      nowCal.setTime(new Date());
      nowCal.add(Calendar.YEAR, 10);

      PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
      testPatient0 = new Patient(patientId, "John", "Doe", new Date(DateUtils.getDaysAgoDate(35 * 365).getTime()));
      testPatient0 = patientDao.addPatient(testPatient0);
      registration = utils.addInitialRegistration(databaseProvider.get(), patientId, nowCal.getTime(), "", "NPV");
      activity = createActivity();
    }

    private Activity createActivity() {

      Activity activity = new Activity(patientId, Constants.ACTIVITY_CONSENTED, new AssessmentId(registration.getAssessmentRegId()), registration.getSurveyRegList().get(0).getToken(), user.getUserPrincipalId());
      activity.setActivityDt(nowCal.getTime());
      activityDao.createActivity(activity);
      return activity;
    }


    public void testGetActivityTrue() {
      logger.info("testGetAcivityType");
      ArrayList<Activity> activities = activityDao.getActivity(DateUtils.getDateStart(DateUtils.getTimestampStart(nowCal.getTime())), DateUtils.getTimestampEnd(nowCal.getTime()), true);
      for (Activity activity : activities) {
        logger.debug(activity.getActivityType() + " " + activity.getActivityDt());
      }
      assertEquals(activities.size(), 1);
      assertEquals(activities.get(0).getPatientId(), patientId);
    }


    public void testGetActivityTrueStatement() {
      logger.info("testGetActivityTrueStatement");
      ArrayList<Activity> activities = activityDao.getActivity(DateUtils.getTimestampStart(nowCal.getTime()), DateUtils.getTimestampEnd(nowCal.getTime()), true);
      assertEquals(activities.size(), 1);
      assertEquals(activities.get(0).getPatientId(), patientId);
    }


    public void testGetActivityFalse() {
      logger.info("testGetActivityFalse");
      ArrayList<Activity> activities = activityDao.getActivity(DateUtils.getTimestampStart(nowCal.getTime()), DateUtils.getTimestampEnd(nowCal.getTime()), false);
      assertEquals(activities.size(), 1);
      assertEquals(activities.get(0).getPatientId(), patientId);
    }


    public void testGetActivityByAssessmentId( ) {
      logger.info("testGetActivityByAssessmentId");
      ArrayList<Activity> activities = activityDao.getActivityByAssessmentId(new AssessmentId(registration.getAssessmentRegId()), Constants.ACTIVITY_CONSENTED);
      assertEquals(activities.size(), 1);
      assertEquals(Constants.ACTIVITY_CONSENTED, activities.get(0).getActivityType());
    }


    public void testGetPatientsActivity( ) {
      logger.info("testGetPatientsActivity");
      getPatientsActivity(true);
      getPatientsActivity(false);
    }

    private void getPatientsActivity(boolean includeCompleted) {
      ArrayList<Activity> activities = activityDao.getPatientsActivity(patientId, includeCompleted);
      assertEquals(activities.size(), 1);
      assertEquals(Constants.ACTIVITY_CONSENTED, activities.get(0).getActivityType());
    }


   public void testGetPatientsActivityByToken() {
     logger.info("testGetPatientsActivityByToken");
      ArrayList<Activity> activities = activityDao.getActivityByToken(activity.getToken());
      assertEquals(activities.size(), 1);
      assertEquals(activities.get(0).getToken(), activity.getToken());
      activities = activityDao.getActivityByToken(activity.getToken(), Constants.ACTIVITY_CONSENTED);
      assertEquals(activities.size(), 1);
      assertEquals(activities.get(0).getToken(), activity.getToken());
      assertEquals(Constants.ACTIVITY_CONSENTED, activities.get(0).getActivityType());
    }


    public void testDeleteActivity() {
      logger.info("testDeleteActivity");
      activityDao.deleteActivity(activity);
      ArrayList<Activity> activities = activityDao.getPatientsActivity(patientId, true);
      assertEquals(activities.size(), 0);
    }


    public void testCreateActivity() {
      Activity activity = new Activity(patientId, Constants.ACTIVITY_CHART_GENERATED, new AssessmentId(registration.getAssessmentRegId()), registration.getSurveyRegList().get(0).getToken(), user.getUserPrincipalId());
      activity.setActivityDt(nowCal.getTime());
      activityDao.createActivity(activity);
      assertNotNull(activity.getActivityId());
    }
}

