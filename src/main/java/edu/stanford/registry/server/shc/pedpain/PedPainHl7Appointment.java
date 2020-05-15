/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.pedpain;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.imports.data.Hl7Appointment;
import edu.stanford.registry.server.imports.data.Hl7AppointmentIntf;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.server.xchg.ImportException;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.Provider;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Flavor;

public class PedPainHl7Appointment extends Hl7Appointment implements Hl7AppointmentIntf {

  private static final Logger logger = LoggerFactory.getLogger(PedPainHl7Appointment.class);
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
  private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
  private static final SimpleDateFormat dateEncounterFormat = new SimpleDateFormat("yyyyMMdd");
  private final SimpleDateFormat apptDateTimeFormat;
  private final SiteInfo siteInfo;
  private final Long siteId;
  private final Database database;
  private final AssessDao assessDao;
  private final ActivityDao activityDao;
  private PatientDao patientDao = null;

  public PedPainHl7Appointment(Database database, SiteInfo siteInfo) {
    super(database, siteInfo);
    this.siteInfo = siteInfo;
    this.database = database;
    this.siteId = siteInfo.getSiteId();
    apptDateTimeFormat = DateUtils.newDateFormat(siteInfo, "yyyyMMddHHmmss");
    assessDao = new AssessDao(database, siteInfo);
    activityDao = new ActivityDao(database, siteId);
    refreshPatientAttrs(database, siteId);
  }

  public ApptRegistration processAppointment(Patient patient, String apptDateTmStr, String visitEid, String visitDescription,
                                             int apptStatus, String encounterEid, String providerEid, String department) throws ImportException {


    Date apptDate;
    try {
      apptDate = apptDateTimeFormat.parse(apptDateTmStr);
    } catch (Exception e) {
      throw new ImportException("Invalid data for appointment date/time: " + apptDateTmStr);
    }

    logger.debug("Processing appointment encounterEid {} with status {} for {} on {} with visit Eid {} and visit description {}",
        encounterEid, apptStatus, patient.getPatientId(), dateEncounterFormat.format(apptDate), visitEid, visitDescription);

    // Determine registration type from appointment status
    String regType;
    String apptComplete = null;
    switch (apptStatus) {
    case APPT_STATUS_SCHEDULED:
    case APPT_STATUS_ARRIVED:
      regType = Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT;
      break;
    case APPT_STATUS_COMPLETED:
      apptComplete = Constants.REGISTRATION_APPT_COMPLETED;
      regType = Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT;
      break;
    case APPT_STATUS_NO_SHOW:
    case APPT_STATUS_LEFT_NOT_SEEN:
      apptComplete = Constants.REGISTRATION_APPT_NOT_COMPLETED;
      regType = Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT;
      break;
    case APPT_STATUS_CANCELED:
      regType = Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT;
      break;
    default:
      logger.warn("Site# {}: Ignoring appointment, bad status: {} ", siteId, apptStatus);
      // Ignore appointment
      return null;
    }

    // Get the visit type from the hl7 visit eid or description
    String visitType = getVisitType(visitEid, visitDescription);
    if (visitType.isEmpty()) {
      visitType = visitDescription;
    }

    // Ignore certain visit types
    if (skipType(visitType, department)) {
      return null;
    }

    if ((encounterEid == null) || encounterEid.equals("")) {
      throw new ImportException("Missing value for encounter id");
    }

    // Create an encounter id to represent the patient and date
    String patDateEncounterId = patient.getPatientId() + dateEncounterFormat.format(apptDate);
    patDateEncounterId = patDateEncounterId.replace("-", "");

    // Look up the patient/date encounter information
    ApptRegistration reg = assessDao.getApptRegistrationByEncounterId(patDateEncounterId);
    if (reg == null) {
      // Create a new patient/date encounter with the first appointment data
      return super.processAppointment(patient, apptDateTmStr, visitEid, visitDescription,
          apptStatus, patDateEncounterId, providerEid, department);
    } else {
      // Roll up this appointment with the existing patient/date encounter
      return updateRegistration(reg, apptDate, visitType, regType, apptComplete, providerEid, department);
    }
  }

  /**
   * Include another appointment on the patient date encounter.
   */
  private ApptRegistration updateRegistration(ApptRegistration reg, Date apptDate, String visitType, String regType, String apptComplete,
                                              String providerEid, String department) {
    logger.debug("In updateRegistration, visitType is {}", visitType);
    String change = "";
    // If another appointment during the day has been canceled ignore it. The patient date encounter
    // will be marked as cancelled only if the matching appointment is cancelled.
    if (regType.equals(Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT)) {
      if (!reg.getRegistrationType().equals(Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT) &&
          reg.getSurveyDt().equals(apptDate) && (reg.getClinic() == null || reg.getClinic().equals(department))) {
        reg.setRegistrationType(regType);
        change += "RegistrationType=" + regType + ";";
      } else {
        logger.debug("Returning cancelled appt");
        return reg;
      }
    }

    // If the current patient date encounter is cancelled then replace the cancelled
    // appointment data with the new appointment data.
    if (reg.getRegistrationType().equals(Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT)) {
      if (!reg.getSurveyDt().equals(apptDate)) {
        reg.setSurveyDt(apptDate);
        List<SurveyRegistration> surveyRegs = reg.getSurveyRegList();
        for (SurveyRegistration surveyReg : surveyRegs) {
          surveyReg.setSurveyDt(apptDate);
        }
        change += "SurveyDt=" + dateTimeFormat.format(apptDate) + ";";
      }
      if (!areSame(reg.getVisitType(), visitType)) {
        reg.setVisitType(visitType);
        change += "VisitType=" + visitType + ";";
      }
      if (!areSame(reg.getRegistrationType(), regType)) {
        reg.setRegistrationType(regType);
        change += "RegistrationType=" + regType + ";";
      }
      if (apptComplete != null) {
        // Only update registration value if we have a apptComplete value
        if (!areSame(reg.getApptComplete(), apptComplete)) {
          reg.setApptComplete(apptComplete);
          change += "ApptComplete=" + apptComplete + ";";
        }
      }

      // Look up the provider from the provider Eid
      Provider provider = getProvider(providerEid);
      Long providerId = (provider == null) ? null : provider.getProviderId();
      if ((reg.getProviderId() == null && providerId != null) ||
          (reg.getProviderId() != null && !reg.getProviderId().equals(providerId))) {
        reg.setProviderId(providerId);
        change += "ProviderId=" + providerId + ";";
      }

      if (!areSame(reg.getClinic(), department)) {
        reg.setClinic(department);
        change += "Department=" + department + ";";
      }
      if ("".equals(change)) { // nothing changed
        return reg;
      }

      // Update existing registration with the changes
      SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
      surveyRegUtils.updateRegistration(database, reg, reg.getSurveyDt());
      logger.debug("Modified survey registration for encounterEid {} {}", reg.getEncounterEid(), change);
      return reg;
    }

    logger.debug("Checking if changes are needed");
    // If the new appointment is earlier in the day then use the new appointment time, provider and department
    if (apptDate.before(reg.getSurveyDt())) {
      reg.setSurveyDt(apptDate);
      List<SurveyRegistration> surveyRegs = reg.getSurveyRegList();
      for (SurveyRegistration surveyReg : surveyRegs) {
        surveyReg.setSurveyDt(apptDate);
      }
      change += "SurveyDt=" + dateTimeFormat.format(apptDate) + ";";

      Provider provider = getProvider(providerEid);
      Long providerId = (provider == null) ? null : provider.getProviderId();
      if ((reg.getProviderId() == null && providerId != null) ||
          (reg.getProviderId() != null && !reg.getProviderId().equals(providerId))) {
        reg.setProviderId(providerId);
        change += "ProviderId=" + providerId + ";";
      }
      if (!areSame(reg.getClinic(), department)) {
        reg.setClinic(department);
        change += "Department=" + department + ";";
      }
    }

    // Update the appointment complete status based on the priority of the values
    if (apptComplete == null && reg.getApptComplete() != null) {
      // null (unknown) has the highest priority
      change += "ApptComplete=null;";
      reg.setApptComplete(null);
    } else if (Constants.REGISTRATION_APPT_NOT_COMPLETED.equals(apptComplete)) {
      // Not Completed is the next priority
      if (reg.getApptComplete() != null && !Constants.REGISTRATION_APPT_NOT_COMPLETED.equals(reg.getApptComplete())) {
        reg.setApptComplete(Constants.REGISTRATION_APPT_NOT_COMPLETED);
        change += "ApptComplete=" + Constants.REGISTRATION_APPT_NOT_COMPLETED + ";";
      }
    } //else -- Completed has the lowest priority.

    logger.debug("Starting check of visitType {} reg has {} change is {}", visitType, reg.getVisitType(), change);
    // Update the visit type of multiple appointments based on the priority of the visit types

    if (visitType != null) {
      String regVisitType = reg.getVisitType(); // save original value
      if (reg.getVisitType() == null) {
        // If the current visit type is null use the new appointment visit type
        reg.setVisitType(visitType);
      } else {
        if (isPREPDayAppt(visitType)) {
          logger.debug("Its a day rehab");
          // PREP Day Appt is the highest priority
          reg.setVisitType("DAY REHAB");
        } else if (isPREPAppt(visitType)) {
          // PREP Appt is the next priority after PREP Day Appt
          if (!isPREPDayAppt(reg.getVisitType())) {
            reg.setVisitType("PREP");
          }
        } else if (isCaptivateAppt(visitType)) {
          // Captivate Appt is the next priority after PREP Day Appt, PREP Appt
          if (!isPREPDayAppt(reg.getVisitType()) &&
              !isPREPAppt(reg.getVisitType())) {
            reg.setVisitType("CAPTIVATE");
          }
        } else {
          // Last priority is Pain Appt
          if (!isPREPDayAppt(reg.getVisitType()) &&
              !isPREPAppt(reg.getVisitType()) &&
              !isCaptivateAppt(reg.getVisitType())) {
            if (isInitialAppt(reg.getVisitType()) || isInitialAppt(visitType)) {
              reg.setVisitType("New PAIN");
            } else {
              reg.setVisitType("PAIN");
            }
          }
        }
      }
      if (!regVisitType.equals(reg.getVisitType())) { // check if it was changed
        change += "VisitType=" + reg.getVisitType() + ";";
      }
    }
    if (!"".equals(change)) {
      SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
      surveyRegUtils.updateRegistration(database, reg, reg.getSurveyDt());
      logger.debug("Modified survey registration for encounterEid {} {}", reg.getEncounterEid(), change);
    }
    return reg;
  }

  @Override
  public String getSurveyType(String patientId, String visitType, Date apptDate, String curSurveyType) {

    String surveyType;

    try {
      if (visitType == null) {
        surveyType = PedPainCustomizer.SURVEY_NOSURVEY;
      }
      // PREP Day Rehab appointment
      else if (isPREPDayAppt(visitType)) {
        surveyType = PedPainCustomizer.SURVEY_NOSURVEY;
        if (!baselineDone(patientId, apptDate)) {
          // Baseline survey if PREPd baseline not done
          surveyType = getSurveyTypeFor(PedPainCustomizer.SURVEY_PREP_DAY_BASELINE, apptDate);
        }
      }
      // PREP program appointment
      else if (isPREPAppt(visitType)) {
        surveyType = PedPainCustomizer.SURVEY_NOSURVEY;
        if (!baselineDone(patientId, apptDate)) {
          // Baseline survey if PREP baseline not done
          surveyType = getSurveyTypeFor(PedPainCustomizer.SURVEY_PREP_BASELINE, apptDate);
        } else if (isFinalAppt(visitType)) {
          // Completion survey if last PREP appointment
          surveyType = getSurveyTypeFor(PedPainCustomizer.SURVEY_PREP_COMPLETION, apptDate);
        } else if (isFriday(apptDate)) {
          // PREP program has weekly survey on Fridays
          if (baselineDone(patientId, apptDate)) {
            surveyType = getSurveyTypeFor(PedPainCustomizer.SURVEY_PREP, apptDate);
          }
        }
      }
      // Captivate program appointment
      else if (isCaptivateAppt(visitType)) {
        int week = getCaptivateWeek(patientId, apptDate);
        if (week == 1) {
          surveyType = getSurveyTypeFor(PedPainCustomizer.SURVEY_CAPTIVATE_INITIAL, apptDate);
        } else if (week == 5) {
          surveyType = getSurveyTypeFor(PedPainCustomizer.SURVEY_CAPTIVATE_SHORT, apptDate);
        } else if (week == 9) {
          surveyType = getSurveyTypeFor(PedPainCustomizer.SURVEY_CAPTIVATE_FINAL, apptDate);
        } else {
          surveyType = PedPainCustomizer.SURVEY_NOSURVEY;
        }
      }
      // Comfortability appointment
      else if (isComfortabilityAppt(visitType)) {
        surveyType = PedPainCustomizer.SURVEY_NOSURVEY;
      }
      // Regular appointment
      else {
        if (PedPainCustomizer.PREPInProgress(database, siteInfo, patientId, apptDate)) {
          // No survey for regular appointment when PREP program in progress
          surveyType = PedPainCustomizer.SURVEY_NOSURVEY;
        } else if (PedPainCustomizer.CaptivateInProgress(database, siteInfo, patientId, apptDate)) {
          surveyType = PedPainCustomizer.SURVEY_NOSURVEY;
        } else if (!initialDone(patientId) || isInitialAppt(visitType)) {
          // Initial survey if an initial survey has not been completed or initial appointment
          surveyType = getSurveyTypeFor(PedPainCustomizer.SURVEY_INITIAL, apptDate);
        } else {
          // Otherwise follow up survey
          if (getSurveyTypeFor(PedPainCustomizer.SURVEY_FOLLOWUP, apptDate).equals(curSurveyType)
              || getSurveyTypeFor(PedPainCustomizer.SURVEY_FOLLOWUP_18, apptDate).equals(curSurveyType)) {
            surveyType = curSurveyType;
          } else if (PedPainCustomizer.is18AndOver(database, siteId, patientId)) {
            surveyType = getSurveyTypeFor(PedPainCustomizer.SURVEY_FOLLOWUP_SHORT_18, apptDate);
          } else {
            surveyType = getSurveyTypeFor(PedPainCustomizer.SURVEY_FOLLOWUP_SHORT, apptDate);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Unexpected error while processing appointment for {} on {}", patientId, dateTimeFormat.format(apptDate), e);
      surveyType = PedPainCustomizer.SURVEY_NOSURVEY;
    }

    return surveyType;
  }

  @Override
  /*
   * Assign a follow up survey to the appointment
   */
  public ApptRegistration postAppointment(ApptRegistration reg) {
    // If the survey has already been started don't do anything
    if (!canUpdateSurveyType(reg)) {
      logger.debug("Survey can not be modified because it is past or started");
      return reg;
    }
    try {
      if (isPREPAppt(reg.getVisitType())) {
        return reg;
      }
      if (PedPainCustomizer.PREPInProgress(database, siteInfo, reg.getPatientId(), reg.getVisitDt())) {
        return reg;
      }
      if (isCaptivateAppt(reg.getVisitType())) {
        return reg;
      }
      if (PedPainCustomizer.CaptivateInProgress(database, siteInfo, reg.getPatientId(), reg.getVisitDt())) {
        return reg;
      }
      if (isComfortabilityAppt(reg.getVisitType())) {
        return reg;
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    // Date range for FollowUp is between 30 days before and 14 days after this appointment
    Date dateFrom = DateUtils.getDaysFromDate(siteInfo, reg.getVisitDt(), -30);
    Date dateTo = DateUtils.getDateEnd(siteInfo, DateUtils.getDaysFromDate(siteInfo, reg.getVisitDt(), 14));

    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
    AssessDao assessDao = new AssessDao(database, siteInfo);
    User adminUser = ServerUtils.getAdminUser(database);

    // Get stand alone surveys for this patient
    ArrayList<PatientRegistration> standaloneSurveys = assessDao.getPatientRegistrationsByType(
        reg.getPatientId(), Constants.REGISTRATION_TYPE_STANDALONE_SURVEY);

    if (standaloneSurveys != null) {
      for (PatientRegistration survey : standaloneSurveys) {
        // If it is a FollowUp survey
        if (PedPainCustomizer.sameSurveyTypes(survey.getSurveyType(),
            new String[] { PedPainCustomizer.SURVEY_FOLLOWUP, PedPainCustomizer.SURVEY_FOLLOWUP_18 })) {
          // If the FollowUp is not done
          if (!followUpDone(assessDao, survey.getPatientId(), survey.getSurveyDt())) {
            // If the FollowUp is within the date range
            if (survey.getSurveyDt().after(dateFrom) & survey.getSurveyDt().before(dateTo)) {
              // Get the active follow up survey type
              XMLFileUtils xmlFileUtils = XMLFileUtils.getInstance(siteInfo);
              String surveyName = PedPainCustomizer.SURVEY_FOLLOWUP;
              if (PedPainCustomizer.is18AndOver(database, siteId, reg.getPatientId())) {
                surveyName = PedPainCustomizer.SURVEY_FOLLOWUP_18;
              }
              String surveyType = xmlFileUtils.getActiveProcessForName(surveyName, reg.getVisitDt());
              // If the appointment is not a FollowUp survey then change it to a FollowUp survey
              if (!areSame(reg.getSurveyType(), surveyType)) {
                surveyRegUtils.changeSurveyType(database, reg.getAssessment(), surveyType, adminUser);
                logger.debug("Modified survey registration for encounterEid {} SurveyType= {}", reg.getEncounterEid(), surveyType);
              }
            }
          }
        }
      }
    }
    return reg;
  }

  public boolean canUpdateSurveyType(ApptRegistration reg) {
    boolean canUpdate = super.canUpdateSurveyType(reg);
    if (canUpdate) {
      // Don't change the survey type if the survey was manually created
      // (i.e. registered by a user other than admin)
      AssessmentRegistration assessment = reg.getAssessment();
      List<SurveyRegistration> surveys = assessment.getSurveyRegList();
      for (SurveyRegistration survey : surveys) {
        ArrayList<Activity> typeChangedActivities =
            activityDao.getActivityByToken(survey.getToken(), Constants.ACTIVITY_REGISTERED);
        if (typeChangedActivities != null) {
          for (Activity activity : typeChangedActivities) {
            if (activity.getUserPrincipalId() != null) {
              if (!activity.getUserPrincipalId().equals(ServerUtils.getAdminUser(database).getUserPrincipalId())) {
                logger.debug("Can not modified survey for appointment registration {} because appointment was manually created", reg.getApptRegId());
                return false;
              }
            }
          }
        }
      }
    }

    return canUpdate;
  }

  private boolean isPREPAppt(String visitType) {
    String name = visitType.toUpperCase();
    return name.startsWith("PREP") || name.startsWith("PPPRC");
  }

  private boolean isPREPDayAppt(String visitType) {
    String name = visitType.toUpperCase();
    return name.startsWith("DAY REHAB");
  }

  private boolean isCaptivateAppt(String visitType) {
    String name = visitType.toUpperCase();
    return name.contains("CAP");
  }

  private boolean isComfortabilityAppt(String visitType) {
    String name = visitType.toUpperCase();
    return name.equals("COMFORT");
  }

  private boolean isInitialAppt(String visitType) {
    String name = visitType.toUpperCase();
    // 'PAIN URG MD' is the coded value for Epic visit type 'Pain New Urgent MD'
    // 'PAIN URG PSY' is the coded value for Epic visit type 'Pain New Urgent Psych'
    // 'PAIN NMDMWF' is the coded value for Epic visit type 'Pain New MD MWF'
    // 'PAIN NMD TTH' is the coded value for Epic visit type 'Pain New MD TTH'
    // 'PAIN NPSYMWF' is the coded value for Epic visit type 'Pain New Psych MWF'
    // 'PAIN NPSYTTH' is the coded value for Epic visit type 'Pain New Psych TTH'
    // 'PAINURGMDMWF' is the coded value for Epic visit type 'Pain New Urgent MD MWF'
    // 'PAINURGMDTTH' is the coded value for Epic visit type 'Pain New Urgent MD TTH'
    // 'PAINNUPSYMWF' is the coded value for Epic visit type 'Pain New Urgent Psych MWF'
    // 'PAINNUPSYTTH' is the coded value for Epic visit type 'Pain New Urgent Psych TTH'
    // 'PAIN IM PSY' is the coded value for Epic visit type 'PAIN NEW IM PSYCH'
    // 'PAIN IM MD' is the coded value for Epic visit type 'PAIN NEW MD PSYCH'
    // 'PAIN IM EVAL'
    return name.contains("NEW") ||
        name.equals("PAIN URG MD") || name.equals("PAIN URG PSY") ||
        name.contains("NMD") || name.contains("NPSY") ||
        name.contains("URGMD") || name.contains("NUPSY") ||
        name.equals("PAIN IM PSY") || name.equals("PAIN IM MD") || name.equals("PAIN IM EVAL");
  }

  private boolean isFinalAppt(String visitType) {
    String name = visitType.toUpperCase();
    return name.contains(" FIN");
  }

  private boolean isFriday(Date apptDate) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(apptDate);
    return (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY);
  }

  private boolean initialDone(String patientId) throws Exception {
    Date initial = getDateAttr(patientId, PedPainCustomizer.ATTR_INITIAL);
    Date baseline = getDateAttr(patientId, PedPainCustomizer.ATTR_BASELINE);
    return (initial != null) || (baseline != null);
  }

  private boolean baselineDone(String patientId, Date apptDate) throws Exception {
    Date start = getDateAttr(patientId, PedPainCustomizer.ATTR_PREP_START);
    Date end = getDateAttr(patientId, PedPainCustomizer.ATTR_PREP_END);

    if (start == null) {
      // New PREP appointment where the PREP start attribute has not been set yet,
      // so use the appointment date as start of a new PREP
      start = apptDate;
    } else if (end != null) {
      // If new PREP appointment is more than 28 days past the PREP end attribute
      // then consider it the start of a new PREP
      if (apptDate.after(DateUtils.getDaysFromDate(siteInfo, end, 28))) {
        start = apptDate;
      }
    }

    // Baseline cutoff is 14 days before the PREP start date
    Date cutoff = DateUtils.getDaysFromDate(siteInfo, start, -14);

    Date baseline = getDateAttr(patientId, PedPainCustomizer.ATTR_BASELINE);
    if ((baseline != null) && baseline.after(cutoff)) {
      return true;
    }

    Date initial = getDateAttr(patientId, PedPainCustomizer.ATTR_INITIAL);
    return (initial != null) && initial.after(cutoff);

  }

  private int getCaptivateWeek(String patientId, Date apptDate) throws Exception {
    Date start = getDateAttr(patientId, PedPainCustomizer.ATTR_CAPTIVATE_START);
    // If Captivate start not set yet then assume week 1
    if (start == null) {
      return 1;
    }

    Calendar cal = Calendar.getInstance();
    cal.setTime(start);
    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

    for (int i = 0; i <= 10; i++) {
      if (apptDate.before(cal.getTime())) {
        return i;
      }
      cal.add(Calendar.DATE, 7);
    }
    return 99;
  }

  /**
   * The follow up is done if a follow up survey was completed with a
   * survey date after 14 days before the follow up date.
   */
  private boolean followUpDone(AssessDao assessDao, String patientId, Date followUpDate) {
    Date cutoff = DateUtils.getDaysFromDate(siteInfo, followUpDate, -14);

    List<ApptRegistration> surveys = assessDao.getCompletedRegistrationsByPatient(patientId);
    for (ApptRegistration survey : surveys) {
      if (survey.getSurveyDt().after(cutoff) &&
          PedPainCustomizer.sameSurveyTypes(survey.getSurveyType(),
              new String[] { PedPainCustomizer.SURVEY_FOLLOWUP, PedPainCustomizer.SURVEY_FOLLOWUP_18 })) {
        return true;
      }
    }
    return false;
  }

  private String getSurveyTypeFor(String name, Date apptDate) throws Exception {
    XMLFileUtils xmlFileUtils = XMLFileUtils.getInstance(siteInfo);
    String surveyType = xmlFileUtils.getActiveProcessForName(name, apptDate);
    if (surveyType == null) {
      throw new Exception("Process not found for survey name " + name + " and date " + apptDate);
    }
    return surveyType;
  }

  private Date getDateAttr(String patientId, String dataName) throws Exception {
    if (patientDao == null) {
      patientDao = new PatientDao(database, siteId);
    }
    PatientAttribute attr = patientDao.getAttribute(patientId, dataName);
    if (attr == null) {
      return null;
    }

    String strValue = attr.getDataValue();
    if ((strValue == null) || strValue.trim().equals("")) {
      return null;
    }

    Date dateValue;
    try {
      dateValue = dateFormat.parse(strValue);
    } catch (ParseException e) {
      throw new Exception("Invalid date value for patient attribute id " + attr.getPatientAttributeId(), e);
    }
    return dateValue;
  }

  /**
   * Refresh the patient attributes. This refreshes the following patient attributes
   * which are based on the appointment data.
   * PREP - indicates if patient is a PREP program patient
   * PREP_start - start date of the patient's latest PREP program
   * PREP_end - end date of the patient's latest PREP program
   * Baseline - the most recently completed Initial survey date
   */
  private static void refreshPatientAttrs(Database database, Long siteId) {
    PatientDao patientDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));

    // Get the PREP patients
    List<String> PREPPatients = getPREPPatients(database, siteId);
    for (String patientId : PREPPatients) {
      // Get the patient's PREP appointments
      List<Date> appts = getPREPAppointments(database, siteId, patientId);
      // Refresh the patient's PREP attributes
      refreshPREPAttrs(patientDao, patientId, appts);
    }

    // Get the Captivate patients
    List<String> captivatePatients = getCaptivatePatients(database, siteId);
    for (String patientId : captivatePatients) {
      // Get the patient's Captivate appointments
      List<Date> appts = getCaptivateAppointments(database, siteId, patientId);
      // Refresh the patient's PREP attributes
      refreshCaptivateAttrs(patientDao, patientId, appts);
    }

    // Get the most recent completed Initial survey for each patient with
    // with a completed an Initial survey
    Map<String, Date> lastInitialCompletedSurveyDates = getLastCompletedSurveyDates(database, siteId,
        PedPainCustomizer.SURVEY_INITIAL, PedPainCustomizer.SURVEY_INITIAL );
    for (String patientId : lastInitialCompletedSurveyDates.keySet()) {
      // Update the Initial attribute for the patient
      Date initial = lastInitialCompletedSurveyDates.get(patientId);
      String initialValue = dateFormat.format(initial);
      PatientAttribute initialAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_INITIAL, initialValue, PatientAttribute.STRING);
      patientDao.insertAttribute(initialAttr);
    }

    // Get the most earliest completed FollowUpShort survey for each patient with
    // with a completed FollowUpShort survey
    Map<String, Date> firstFollowUpCompletedSurveyDates = getFirstCompletedSurveyDates(database, siteId);
    for (String patientId : firstFollowUpCompletedSurveyDates.keySet()) {
      // If the Initial attribute for the patient is not set then set it to
      // the first completed FollowUpShort survey
      PatientAttribute initialAttr = patientDao.getAttribute(patientId, PedPainCustomizer.ATTR_INITIAL);
      if (initialAttr == null) {
        Date initial = firstFollowUpCompletedSurveyDates.get(patientId);
        String initialValue = dateFormat.format(initial);
        initialAttr = new PatientAttribute(
            patientId, PedPainCustomizer.ATTR_INITIAL, initialValue, PatientAttribute.STRING);
        patientDao.insertAttribute(initialAttr);
      }
    }

    // Get the most recent completed InitialPReP survey for each patient with
    // with a completed a InitialPReP survey
    Map<String, Date> lastBaselineCompletedSurveyDates =
        getLastCompletedSurveyDates(database, siteId, PedPainCustomizer.SURVEY_PREP_BASELINE, PedPainCustomizer.SURVEY_PREP_DAY_BASELINE );
    for (String patientId : lastBaselineCompletedSurveyDates.keySet()) {
      // Update the Baseline attribute for the patient
      Date baseline = lastBaselineCompletedSurveyDates.get(patientId);
      String baselineValue = dateFormat.format(baseline);
      PatientAttribute baselineAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_BASELINE, baselineValue, PatientAttribute.STRING);
      patientDao.insertAttribute(baselineAttr);
    }
  }

  /**
   * Refresh the PREP attributes for a patient.
   */
  private static void refreshPREPAttrs(PatientDao patientDao, String patientId, List<Date> appts) {
    if (appts.isEmpty()) {
      // If the patient no longer has PREP appointments then they were canceled,
      // so clear the PREP attributes
      PatientAttribute prepAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_PREP_PATIENT, null, PatientAttribute.STRING);
      patientDao.insertAttribute(prepAttr);

      PatientAttribute endAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_PREP_END, null, PatientAttribute.STRING);
      patientDao.insertAttribute(endAttr);

      PatientAttribute startAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_PREP_START, null, PatientAttribute.STRING);
      patientDao.insertAttribute(startAttr);
    } else {
      // Set the PREP attribute to indicate the patient is a PREP program patient
      PatientAttribute prepAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_PREP_PATIENT, "y", PatientAttribute.STRING);
      patientDao.insertAttribute(prepAttr);

      // Set PREP_end to the latest PREP appointment
      Date latest = appts.get(0);
      String endValue = dateFormat.format(latest);
      PatientAttribute endAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_PREP_END, endValue, PatientAttribute.STRING);
      patientDao.insertAttribute(endAttr);

      // Scan the appointment dates from latest to earliest looking for a gap
      // of more than 28 days. If found assume the patient had completed the
      // PREP program and is repeating it. The start date is the date after
      // the gap.
      Date start = null;
      for (int i = 0; i < (appts.size() - 1); i++) {
        long diff = (appts.get(i).getTime() - appts.get(i + 1).getTime());
        long daysBetween = Math.round((double) diff / (double) DateUtils.MILISECONDS_PER_DAY);
        if (daysBetween > 28) {
          start = appts.get(i);
          break;
        }
      }
      // No gap found so start is the earliest appointment
      if (start == null) {
        start = appts.get(appts.size() - 1);
      }

      // Update the PREP_start date
      String startValue = dateFormat.format(start);
      PatientAttribute startAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_PREP_START, startValue, PatientAttribute.STRING);
      patientDao.insertAttribute(startAttr);
    }
  }

  /**
   * Get patients with a PREP appointment or one of the PREP attributes.
   */
  private static List<String> getPREPPatients(Database database, Long siteId) {
    String sql =
        "select distinct patient_id from " +
            "( select patient_id from appt_registration where survey_site_id = :site and " +
            "  (upper(visit_type) like 'PREP%' or upper(visit_type) like 'PPPRC%' or upper(visit_type) like 'DAY REHAB%') and "
            +
            "  lower(registration_type) = 'a' " +
            "union " +
            "  select patient_id from patient_attribute where survey_site_id = :site and " +
            "  data_name in ('PREP', 'PREP_start', 'PREP_end') " +
            ")";
    if (database.get().flavor() == Flavor.postgresql) {
      sql = sql + " as patients";
    }
    return database.toSelect(sql)
        .argLong(":site", siteId)
        .query(rs -> {
          List<String> results1 = new ArrayList<>();
          while (rs.next()) {
            results1.add(rs.getStringOrNull("patient_id"));
          }
          return results1;
        });
  }

  /**
   * Get the PREP appointment dates for a patient in descending order.
   */
  private static List<Date> getPREPAppointments(Database database, Long siteId, String patientId) {
    String sql =
        "select visit_dt from appt_registration where survey_site_id = :site and " +
            "(upper(visit_type) like 'PREP%' or upper(visit_type) like 'PPPRC%' or upper(visit_type) like 'DAY REHAB%') and "
            +
            "lower(registration_type) = 'a' and patient_id = ? " +
            "order by visit_dt desc";
    return database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(patientId)
        .query(rs -> {
          List<Date> results1 = new ArrayList<>();
          while (rs.next()) {
            results1.add(rs.getDateOrNull("visit_dt"));
          }
          return results1;
        });
  }

  /**
   * Refresh the CAPTIVATE attributes for a patient.
   */
  private static void refreshCaptivateAttrs(PatientDao patientDao, String patientId, List<Date> appts) {
    if (appts.isEmpty()) {
      // If the patient no longer has CAPTIVATE appointments then they were canceled,
      // so clear the CAPTIVATE attributes
      PatientAttribute captivateAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_CAPTIVATE_PATIENT, null, PatientAttribute.STRING);
      patientDao.insertAttribute(captivateAttr);

      PatientAttribute endAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_CAPTIVATE_END, null, PatientAttribute.STRING);
      patientDao.insertAttribute(endAttr);

      PatientAttribute startAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_CAPTIVATE_START, null, PatientAttribute.STRING);
      patientDao.insertAttribute(startAttr);
    } else {
      // Set the CAPTIVATE attribute to indicate the patient is a CAPTIVATE program patient
      PatientAttribute captivateAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_CAPTIVATE_PATIENT, "y", PatientAttribute.STRING);
      patientDao.insertAttribute(captivateAttr);

      // Set CAPTIVATE_end to the latest CAPTIVATE appointment
      Date latest = appts.get(0);
      String endValue = dateFormat.format(latest);
      PatientAttribute endAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_CAPTIVATE_END, endValue, PatientAttribute.STRING);
      patientDao.insertAttribute(endAttr);

      // Scan the appointment dates from latest to earliest looking for a gap
      // of more than 28 days. If found assume the patient had completed the
      // CAPTIVATE program and is repeating it. The start date is the date after
      // the gap.
      Date start = null;
      for (int i = 0; i < (appts.size() - 1); i++) {
        long diff = (appts.get(i).getTime() - appts.get(i + 1).getTime());
        long daysBetween = Math.round((double) diff / (double) DateUtils.MILISECONDS_PER_DAY);
        if (daysBetween > 28) {
          start = appts.get(i);
          break;
        }
      }
      // No gap found so start is the earliest appointment
      if (start == null) {
        start = appts.get(appts.size() - 1);
      }

      // Update the CAPTIVATE_start date
      String startValue = dateFormat.format(start);
      PatientAttribute startAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_CAPTIVATE_START, startValue, PatientAttribute.STRING);
      patientDao.insertAttribute(startAttr);
    }
  }

  /**
   * Get patients with a CAPTIVATE appointment or one of the CAPTIVATE attributes.
   */
  private static List<String> getCaptivatePatients(Database database, Long siteId) {
    String sql =
        "select distinct patient_id from " +
            "( select patient_id from appt_registration where survey_site_id = :site and " +
            "  upper(visit_type) like 'CAPTIVATE%' and " +
            "  lower(registration_type) = 'a' " +
            "union " +
            "  select patient_id from patient_attribute where survey_site_id = :site and " +
            "  data_name in ('CAPTIVATE', 'CAPTIVATE_start', 'CAPTIVATE_end') " +
            ")";
    if (database.get().flavor() == Flavor.postgresql) {
      sql = sql + " as patients";
    }
    return database.toSelect(sql)
        .argLong(":site", siteId)
        .query(rs -> {
          List<String> results1 = new ArrayList<>();
          while (rs.next()) {
            results1.add(rs.getStringOrNull("patient_id"));
          }
          return results1;
        });
  }

  /**
   * Get the CAPTIVATE appointment dates for a patient in descending order.
   */
  private static List<Date> getCaptivateAppointments(Database database, Long siteId, String patientId) {
    String sql =
        "select visit_dt from appt_registration where survey_site_id = :site and " +
            "upper(visit_type) like 'CAPTIVATE%' and " +
            "lower(registration_type) = 'a' and patient_id = ? " +
            "order by visit_dt desc";
    return database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(patientId)
        .query(rs -> {
          List<Date> results1 = new ArrayList<>();
          while (rs.next()) {
            results1.add(rs.getDateOrNull("visit_dt"));
          }
          return results1;
        });
  }

  /**
   * Get the date of the most recently completed survey of the survey type for patients
   * who have completed the survey type.
   */
  private static Map<String, Date> getLastCompletedSurveyDates(Database database, Long siteId, String surveyType1,
                                                               String surveyType2) {
    String sql =
        "select ar.patient_id, max(asmt.assessment_dt) as survey_dt " +
            "from appt_registration ar left join assessment_registration asmt on asmt.assessment_reg_id = ar.assessment_reg_id "
            +
            "where ar.survey_site_id = :site and (asmt.assessment_type like ? OR asmt.assessment_type like ?)" +
            "and not exists (" + AssessDao.getIncompleteSurveyRegSqlAr() + ") " +
            "group by ar.patient_id";
    return database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(surveyType1 + ".%")
        .argString(surveyType2 + ".%")
        .query(rs -> {
          Map<String, Date> results1 = new HashMap<>();
          while (rs.next()) {
            String patientId = rs.getStringOrNull("patient_id");
            Date surveyDate = rs.getDateOrNull("survey_dt");
            results1.put(patientId, surveyDate);
          }
          return results1;
        });
  }

  /**
   * Get the date of the earliest completed survey of the survey type for patients
   * who have completed the survey type.
   */
  private static Map<String, Date> getFirstCompletedSurveyDates(Database database, Long siteId) {
    String sql =
        "select ar.patient_id, min(asmt.assessment_dt) as survey_dt " +
            "from appt_registration ar left join assessment_registration asmt on asmt.assessment_reg_id = ar.assessment_reg_id "
            +
            "where ar.survey_site_id = :site and asmt.assessment_type like ? " +
            "and not exists (" + AssessDao.getIncompleteSurveyRegSqlAr() + ") " +
            "group by ar.patient_id";
    return database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(PedPainCustomizer.SURVEY_FOLLOWUP_SHORT + ".%")
        .query(rs -> {
          Map<String, Date> results1 = new HashMap<>();
          while (rs.next()) {
            String patientId = rs.getStringOrNull("patient_id");
            Date surveyDate = rs.getDateOrNull("survey_dt");
            results1.put(patientId, surveyDate);
          }
          return results1;
        });
  }

}
