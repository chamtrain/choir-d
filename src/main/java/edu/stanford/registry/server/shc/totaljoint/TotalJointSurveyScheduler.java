package edu.stanford.registry.server.shc.totaljoint;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveyRegistrationAttributeDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

public class TotalJointSurveyScheduler {

  private static Logger logger = Logger.getLogger(TotalJointSurveyScheduler.class);
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

  private Database database;
  private SiteInfo siteInfo;
  private Long siteId;
  private PatientDao patientDao;
  private SurveyRegistrationAttributeDao regAttrDao;
  private Map<String,List<CompletedSurvey>> completedSurveys;

  public TotalJointSurveyScheduler(Database database, SiteInfo siteInfo) {
    this.database = database;
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
    patientDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    regAttrDao = new SurveyRegistrationAttributeDao(database);
    completedSurveys = CompletedSurvey.getCompletedSurveys(database);
  }

  /**
   * Create stand alone survey registrations for upcoming scheduled
   * follow ups through the toDate.
   */
  public void scheduleSurveys(Date toDate) {
    Date today = DateUtils.getDateStart(new Date());

    List<FollowUp> followUps = new ArrayList<>();

    // For each surgery, get the follow ups for that surgery which are
    // scheduled within the date range of today through the toDate and
    // which are not completed
    List<Surgery> surgeries = Surgery.getSurgeries(database);
    for(Surgery surgery : surgeries) {
      List<FollowUp> surgeryFollowUps = FollowUp.getFollowUps(siteInfo, surgery);
      for(FollowUp followUp : surgeryFollowUps) {
        Date scheduledDate = followUp.getScheduledDate();
        if (scheduledDate.after(today) && scheduledDate.before(toDate)) {
          if (!isCompleted(followUp)) {
            followUps.add(followUp);
          }
        }
      }
    }

    // Sort the list so that bilateral follow ups are consecutive
    Collections.sort(followUps, new FollowUp.BilateralSort());

    // Create an assessment for each of the follow ups
    for(int i=0; i<followUps.size(); i++) {
      FollowUp followUp = followUps.get(i);
      // Look at the next follow up to see if this is a bilateral follow up
      if (i < followUps.size()-1) {
        FollowUp next = followUps.get(i+1);
        if (FollowUp.isBilateral(followUp, next)) {
          followUp.setSide(TotalJointCustomizer.SIDE_BILATERAL);
          i = i+1;
        }
      }
      logger.debug("Processing follow up " + followUp);
      createAssessment(followUp);
    }
  }

  /**
   * Create a stand alone survey registration for the follow up.
   */
  protected void createAssessment(FollowUp followUp) {
    Patient patient = patientDao.getPatient(followUp.getPatientId());
    if (patient == null) {
      logger.error("Unable to get patient for patient Id " + followUp.getPatientId());
      return;
    }

    // By convention, stand alone surveys are at 11:59:59 PM
    Calendar cal = Calendar.getInstance();
    cal.setTime(followUp.getScheduledDate());
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
    Date surveyDate = cal.getTime();

    // Get the survey type and visit type for the joint
    String surveyType = followUp.getSurveyType(patient, surveyDate);
    String visitType = getVisitType(followUp);
    if ((surveyType == null) || (visitType == null)) {
      logger.error("Unable to get survey or visit type for joint " + followUp);
      return;
    }

    // Get existing registrations for the patient on the follow up date
    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<ApptRegistration> regs = assessDao.getApptRegistrationByPatientAndDate(followUp.getPatientId(), surveyDate);

    // See if an existing registration has already been created for
    // for the visit type
    ApptRegistration reg = null;
    for(ApptRegistration sr : regs) {
      if (visitType.equals(sr.getVisitType())) {
        reg = sr;
      }
    }

    // If not found then create a new registration
    if (reg == null) {
      reg = new ApptRegistration(siteId, followUp.getPatientId(), surveyDate,
          null, surveyType, Constants.REGISTRATION_TYPE_STANDALONE_SURVEY, visitType);
      SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
      reg = surveyRegUtils.createRegistration(assessDao, reg);

      String followUpName = followUp.getFollowUpName();
      String surgeryDate = dateFormat.format(followUp.getSurgeryDate());
      String completed = "false";
      Long surveyRegId = reg.getSurveyReg().getSurveyRegId();
      regAttrDao.setAttribute(surveyRegId, TotalJointCustomizer.ATTR_FOLLOW_UP_NAME, followUpName);
      regAttrDao.setAttribute(surveyRegId, TotalJointCustomizer.ATTR_SURGERY_DATE, surgeryDate);
      regAttrDao.setAttribute(surveyRegId, TotalJointCustomizer.ATTR_FOLLOW_UP_COMPLETED, completed);

      logger.debug("Creating new appt, survey type: " + reg.getSurveyType() +
          ", visit type: " + reg.getVisitType() + ", appt: " + reg.getApptId());
    } else {
      logger.debug("Survey registration already exists for follow up");
    }
  }

  /**
   * Check if the follow up has been completed by the patient.
   */
  protected boolean isCompleted(FollowUp followUp) {
    List<CompletedSurvey> surveys = completedSurveys.get(followUp.getPatientId());
    return followUp.isCompleted(surveys);
  }

  protected String getVisitType(FollowUp followUp) {
    String joint = followUp.getJoint();
    String side = followUp.getSide();
    if (side.equals(TotalJointCustomizer.SIDE_BILATERAL)) {
      side = "Bi";
    }
    return joint + " " + side  + " " + followUp.getFollowUpName();
  }
}
