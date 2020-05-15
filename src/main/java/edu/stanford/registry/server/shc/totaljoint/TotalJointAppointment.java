package edu.stanford.registry.server.shc.totaljoint;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.database.SurveyRegistrationAttributeDao;
import edu.stanford.registry.server.imports.data.Appointment2;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Patient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class TotalJointAppointment extends Appointment2 {

  // Any creator must also call setDatabase(db, siteId)
  public TotalJointAppointment() {
    super();
  }

  private static Logger logger = Logger.getLogger(TotalJointAppointment.class);
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

  private SurveyRegistrationAttributeDao regAttrDao;
  private Map<String,List<Surgery>> patientSurgeries;
  private Map<String,List<PreOpSurgery>> patientPreOpSurgeries;
  private Map<String,List<CompletedSurvey>> completedSurveys;
  private List<String> npBellino;

  @Override
  protected void init() throws Exception {
    super.init();
    regAttrDao = new SurveyRegistrationAttributeDao(database);
    patientSurgeries = Surgery.getPatientSurgeries(database);
    patientPreOpSurgeries = PreOpSurgery.getPatientPreOpSurgeries(siteInfo, database);
    completedSurveys = CompletedSurvey.getCompletedSurveys(database);

    // List of nurse practitioners who see patient for Dr. Bellino
    //   Meenal Mistry (S0104516)
    //   Debra Boland (S0188917)
    //   Manfred Salvador (S0207505)
    //   Mythilla Karuanratne (S0066738)
    npBellino = Arrays.asList("S0104516","S0188917","S0207505","S0066738");
  }

  @Override
  protected boolean skipType(String visitType, String department) {
    if (visitType != null) {
      // do not import DISCHARGE appointments
      if (visitType.toUpperCase().equals("DISCHARGE")) {
        return true;
      }
    }
    return super.skipType(visitType, department);
  }

  @Override
  protected String getVisitType(String visitType, String department) {
    // Add a department prefix to the visit type
    if ((department != null) && department.toUpperCase().equals("ORTHO-JOINT CLINIC")) {
      return "OJ-" + visitType;
    }
    if ((department != null) && department.toUpperCase().equals("ORTHO-SPECIALTIES")) {
      return "OS-" + visitType;
    }
    if ((department != null) && department.toUpperCase().equals("LOS GATOS ORTHO SURG")) {
      return "LG-" + visitType;
    }

    return visitType;
  }

  @Override
  protected String getSurveyType(Patient patient, String visitType, Date apptDate, String providerEid, String curSurveyType) {
    // Remove the department prefix from the visit type
    if (visitType.startsWith("OJ-") || visitType.startsWith("OS-") || visitType.startsWith("LG-")) {
      visitType = visitType.substring(3);
    }

    if (curSurveyType != null) {
      logger.debug("Current survey type is " + curSurveyType);
    }

    // If a survey type has not been assigned yet, assign a survey type
    String surveyType = curSurveyType;
    if (isPreOpVisit(visitType)) {
      surveyType = getPreOpSurveyType(patient, apptDate, providerEid);
    } else {
      if (surveyType == null) {
        surveyType = TotalJointCustomizer.SURVEY_INELIGIBLE;
      }
    }

    return surveyType;
  }

  @Override
  /**
   * Assign a follow up survey to the appointment
   */
  protected void processRegistration(Patient patient, ApptRegistration reg) {
    // If the survey has already been started don't do anything
    if (!canUpdateSurveyType(reg)) {
      logger.debug("Survey can not be modified because it is past or started");
      return;
    }

    Long regId = reg.getSurveyReg().getSurveyRegId();
    String patientId = reg.getPatientId();
    String visitType = reg.getVisitType();
    String curSurveyType = reg.getSurveyType();
    Date surveyDate = reg.getSurveyDt();

    // Remove the department prefix from the visit type
    if (visitType.startsWith("OJ-") || visitType.startsWith("OS-") || visitType.startsWith("LG-")) {
      visitType = visitType.substring(3);
    }

    // Don't assign a follow up to a pre-op visit
    if (isPreOpVisit(visitType)) {
      logger.debug("Appointment is a pre-op appointment");
      Date surgeryDate = getPreOpSurgeryDate(patientId, surveyDate);
      Map<String,String> attrs = regAttrDao.getAttributes(regId);
      setAttribute(attrs, regId, TotalJointCustomizer.ATTR_SURGERY_DATE,
          (surgeryDate != null) ? dateFormat.format(surgeryDate) : null );
      return;
    }

    // Don't change a survey marked as FollowUpDone
    if (curSurveyType.equals(TotalJointCustomizer.SURVEY_FOLLOW_UP_DONE)) {
      logger.debug("Survey type is FollowUpDone");
      return;
    }

    // Look for a follow up
    FollowUp followUp = null;

    // Get all eligible follow ups for the survey date
    List<FollowUp> followUps = getFollowUps(patientId, surveyDate);
    if (followUps.size() > 0) {
      // First look for a pending (incomplete follow up)
      followUp = getPendingFollowUp(followUps);
      if (followUp != null) {
        logger.debug("Pending follow up found " + followUp);
      }
      if (followUp == null) {
        // Then look for a completed follow up
        followUp = getCompletedFollowUp(followUps, surveyDate);
        if (followUp != null) {
          logger.debug("Completed follow up found " + followUp);
        }
      }
    }

    // Get the survey type for the follow up
    String surveyType = TotalJointCustomizer.SURVEY_INELIGIBLE;
    if (followUp != null) {
      surveyType = followUp.getSurveyType(patient, surveyDate);
    }

    // If the survey type has changed then update the survey type
    if (!areSame(curSurveyType, surveyType)) {
      SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
      surveyRegUtils.changeSurveyType(database, reg.getAssessment(), surveyType, ServerUtils.getAdminUser(database));
      logger.debug("Modified survey registration for encounterEid " + reg.getEncounterEid() + " SurveyType=" + surveyType);
    }

    // Update the survey registration attributes
    Map<String,String> attrs = regAttrDao.getAttributes(regId);
    if (followUp != null) {
      String followUpName = followUp.getFollowUpName();
      String surgeryDate = dateFormat.format(followUp.getSurgeryDate());
      String completed = Boolean.toString(isCompleted(followUp));
      setAttribute(attrs, regId, TotalJointCustomizer.ATTR_FOLLOW_UP_NAME, followUpName);
      setAttribute(attrs, regId, TotalJointCustomizer.ATTR_SURGERY_DATE, surgeryDate);
      setAttribute(attrs, regId, TotalJointCustomizer.ATTR_FOLLOW_UP_COMPLETED, completed);
    } else {
      setAttribute(attrs, regId, TotalJointCustomizer.ATTR_FOLLOW_UP_NAME, null);
      setAttribute(attrs, regId, TotalJointCustomizer.ATTR_SURGERY_DATE, null);
      setAttribute(attrs, regId, TotalJointCustomizer.ATTR_FOLLOW_UP_COMPLETED, null);
    }
  }

  /**
   * Check if the visit type is for a Pre-op visit
   */
  protected boolean isPreOpVisit(String visitType) {
    if (visitType != null) {
      if (visitType.startsWith("POV")) {
        return true;
      }
      if (visitType.startsWith("Pre Op")) {
        return true;
      }
      if (visitType.startsWith("PREOP")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the survey type for a Pre-op appointment based on the upcoming
   * scheduled surgery.
   */
  protected String getPreOpSurveyType(Patient patient, Date apptDate, String providerEid) {
    String surveyType = TotalJointCustomizer.SURVEY_NOSURVEY;

    // Do not assign surveys for patients of Dr. Bellino.
    if ((providerEid != null) && npBellino.contains(providerEid)) {
      return TotalJointCustomizer.SURVEY_INELIGIBLE;
    }

    List<PreOpSurgery> preOpSurgeries = patientPreOpSurgeries.get(patient.getPatientId());
    Date startDate = DateUtils.getDateStart(apptDate);
    Date closestDate = null;
    if (preOpSurgeries != null) {
      for(PreOpSurgery preOpSurgery : preOpSurgeries) {
        Date surgeryDate = preOpSurgery.getSurgeryDate();
        if (surgeryDate.after(startDate)) {
          if ((closestDate == null) || (surgeryDate.before(closestDate))) {
            closestDate = surgeryDate;
            surveyType = preOpSurgery.getSurveyType(patient, apptDate);
          }
        }
      }
    }

    return surveyType;
  }

  /**
   * Get the surgery date for a Pre-op appointment based on the upcoming
   * scheduled surgery.
   */
  protected Date getPreOpSurgeryDate(String patientId, Date apptDate) {
    List<PreOpSurgery> preOpSurgeries = patientPreOpSurgeries.get(patientId);
    Date startDate = DateUtils.getDateStart(apptDate);
    Date closestDate = null;
    if (preOpSurgeries != null) {
      for(PreOpSurgery preOpSurgery : preOpSurgeries) {
        Date surgeryDate = preOpSurgery.getSurgeryDate();
        if (surgeryDate.after(startDate)) {
          if ((closestDate == null) || (surgeryDate.before(closestDate))) {
            closestDate = surgeryDate;
          }
        }
      }
    }

    return closestDate;
  }

  /**
   * Get all the follow ups for the patient for which the survey date is
   * eligible. The survey date is eligible for the follow up if it falls
   * within the eligibility window of the follow up.
   */
  protected List<FollowUp> getFollowUps(String patientId, Date surveyDate) {
    List<Surgery> surgeries = patientSurgeries.get(patientId);
    if (surgeries != null) {
      for(Surgery surgery : surgeries) {
        logger.debug("Surgery " + surgery);
      }
    }
    if ((surgeries == null) || (surgeries.size() == 0)) {
      logger.debug("No surgeries found for " + patientId);
    }

    List<FollowUp> result = new ArrayList<>();
    if (surgeries != null) {
      for(Surgery surgery : surgeries) {
        List<FollowUp> surgeryFollowUps = FollowUp.getFollowUps(siteInfo, surgery);
        for(FollowUp followUp : surgeryFollowUps) {
          if (followUp.isEligible(surveyDate)) {
            result.add(followUp);
            boolean done = isCompleted(followUp);
            logger.debug("Folow up " + followUp + " " + (done ? "Done" : "Not done"));
          }
        }
      }
    }

    if (result.size() == 0) {
      logger.debug("No follow ups found for " + patientId + " on " + dateFormat.format(surveyDate));
    }
    return result;
  }

  /**
   * Get the earliest follow up from the possible follow ups which are not completed.
   */
  protected FollowUp getPendingFollowUp(List<FollowUp> followUps) {
    long earliest = Long.MAX_VALUE;
    FollowUp current = null;

    for(FollowUp followUp : followUps) {
      if (!isCompleted(followUp)) {
        if (FollowUp.isBilateral(current, followUp)) {
          current.setSide(TotalJointCustomizer.SIDE_BILATERAL);
        } else {
          Date scheduledDate = followUp.getScheduledDate();
          if (scheduledDate.getTime() < earliest) {
            earliest = scheduledDate.getTime();
            current = followUp;
          }
        }
      }
    }

    return current;
  }

  /**
   * Get the latest follow up from the possible follow ups which are completed
   * within the last 60 days
   */
  protected FollowUp getCompletedFollowUp(List<FollowUp> followUps, Date surveyDate) {
    Date begin = DateUtils.getDaysFromDate(surveyDate, -60);
    Date end = DateUtils.getDaysFromDate(surveyDate, 30);

    long latest = Long.MIN_VALUE;
    FollowUp current = null;

    for(FollowUp followUp : followUps) {
      if (isCompleted(followUp)) {
        if (FollowUp.isBilateral(current, followUp)) {
          current.setSide(TotalJointCustomizer.SIDE_BILATERAL);
        } else {
          Date scheduledDate = followUp.getScheduledDate();
          if (scheduledDate.after(begin) && scheduledDate.before(end) &&
              (scheduledDate.getTime() > latest)) {
            latest = scheduledDate.getTime();
            current = followUp;
          }
        }
      }
    }

    return current;
  }

  /**
   * Check if the follow up has been completed by the patient.
   */
  protected boolean isCompleted(FollowUp followUp) {
    List<CompletedSurvey> surveys = completedSurveys.get(followUp.getPatientId());
    return followUp.isCompleted(surveys);
  }

  /**
   * Set the survey registration attribute. First check if the value
   * has changed to avoid a database access if the value has not changed.
   */
  protected void setAttribute(Map<String,String> attrs, Long surveyRegId, String name, String value) {
    String currentValue = attrs.get(name);
    if (((currentValue == null) && (value == null)) ||
        ((currentValue != null) && currentValue.equals(value))) {
      return;
    }
    regAttrDao.setAttribute(surveyRegId, name, value);
  }
}
