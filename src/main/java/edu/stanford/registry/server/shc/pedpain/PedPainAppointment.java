package edu.stanford.registry.server.shc.pedpain;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.imports.data.Appointment2;
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
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

public class PedPainAppointment extends Appointment2 {

  // Any creator must also call setDatabase(db, siteInfo)
  public PedPainAppointment() {
    super();
  }

  private static final Logger logger = LoggerFactory.getLogger(PedPainAppointment.class);
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
  private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
  private static final SimpleDateFormat dateEncounterFormat = new SimpleDateFormat("yyyyMMdd");

  private Date conversionDate = null;
  private Map<String,PatDateEncounter> patDateEncounters;

  @Override
  protected void init() throws Exception {
    super.init();
    patDateEncounters = new HashMap<>();

    Calendar cal = DateUtils.newCalendar(siteInfo);
    cal.clear();
    cal.set(2018, Calendar.AUGUST, 12);
    conversionDate = cal.getTime();

    refreshPatientAttrs(database, siteId);
  }

  @Override
  protected void processAppointment(String patientId, Date apptDate, String visitType,
      Integer apptStatus, String encounterEid, String providerEid, String department) throws ImportException {

    // Use standard handling if the appointment is before the conversion date
    if (apptDate.before(conversionDate)) {
      super.processAppointment(patientId, apptDate, visitType, apptStatus, encounterEid, providerEid, department);
      return;
    }

    logger.debug("Processing appointment encounterEid {} with status {} for {} on {}", encounterEid, apptStatus, patientId, dateFormat.format(apptDate));

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
        return;
    }

    // Ignore certain visit types
    if (skipType(visitType, department)) {
      return;
    }

    if ((encounterEid == null) || encounterEid.equals("")) {
      throw new ImportException("Missing value for encounter id");
    }

    // Create an encounter id to represent the patient and date
    String patDateEncounterId = patientId + dateEncounterFormat.format(apptDate);
    patDateEncounterId = patDateEncounterId.replace("-", "");

    // Look up the patient/date encounter information
    PatDateEncounter enc = patDateEncounters.get(patDateEncounterId);
    if (enc == null) {
      // Create a new patient/date encounter with the first appointment data
      enc = new PatDateEncounter(patientId, apptDate, visitType, regType, apptComplete, providerEid, department);
      patDateEncounters.put(patDateEncounterId, enc);
    } else {
      // Add another appointment to the patient/date encounter information
      enc.add(apptDate, visitType, regType, apptComplete, providerEid, department);
    }
  }

  @Override
  public void importDataEnd() throws Exception {
    int errors = 0;
    // Process the patient/data encounter information as appointments
    for(String patDateEncounterId : patDateEncounters.keySet()) {
      PatDateEncounter enc = patDateEncounters.get(patDateEncounterId);
      try {
        super.processAppointment(enc.getPatientId(), enc.getApptDate(), enc.getVisitType(), enc.getApptStatus(),
            patDateEncounterId, enc.getProviderEid(), enc.getDepartment());
      } catch(ImportException e) {
        errors += 1;
        logger.error("Error importing appointments for {} on {} ", enc.getPatientId(), dateFormat.format(enc.getApptDate()), e);
      }
    }

    if (errors > 0) {
      throw new Exception("Found " + errors + " errors processing appointments.");
    }
  }

  @Override
  protected String getSurveyType(String patientId, String visitType, Date apptDate, String curSurveyType) {
    if (apptDate.before(conversionDate)) {
      return getSurveyType1(patientId, visitType, apptDate, curSurveyType);
    }
    return getSurveyType2(patientId, visitType, apptDate, curSurveyType);
  }

  protected String getSurveyType1(String patientId, String visitType, Date apptDate, String curSurveyType) {
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
        if (!baselineDone(patientId, apptDate) && !isPsychAppt(visitType)) {
          // Baseline survey if PREP baseline not done
          surveyType = getSurveyTypeFor(PedPainCustomizer.SURVEY_PREP_BASELINE, apptDate);
        } else if (isMDAppt(visitType) && isFinalAppt(visitType)) {
          // Completion survey if last PREP appointment
          surveyType = getSurveyTypeFor(PedPainCustomizer.SURVEY_PREP_COMPLETION, apptDate);
        } else if (isFriday(apptDate) && isMDAppt(visitType)) {
          // PREP program has weekly survey on Fridays for MD appointment
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
        } else if (!initialDone(patientId) && isInitialAppt(visitType)) {
          // Initial survey if an initial survey has not been completed
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

  protected String getSurveyType2(String patientId, String visitType, Date apptDate, String curSurveyType) {
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
  /**
   * Assign a follow up survey to the appointment
   */
  protected void processRegistration(Patient patient, ApptRegistration reg) {
    // If the survey has already been started don't do anything
    if (!canUpdateSurveyType(reg)) {
      logger.debug("Survey can not be modified because it is past or started");
      return;
    }
    try {
      if (isPREPAppt(reg.getVisitType())) {
        return;
      }
      if (PedPainCustomizer.PREPInProgress(database, siteInfo, reg.getPatientId(), reg.getVisitDt())) {
        return;
      }
      if (isCaptivateAppt(reg.getVisitType())) {
        return;
      }
      if (PedPainCustomizer.CaptivateInProgress(database, siteInfo, reg.getPatientId(), reg.getVisitDt())) {
        return;
      }
      if (isComfortabilityAppt(reg.getVisitType())) {
        return;
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
                new String[] {PedPainCustomizer.SURVEY_FOLLOWUP, PedPainCustomizer.SURVEY_FOLLOWUP_18})) {
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
  }

  @Override
  protected boolean canUpdateSurveyType(ApptRegistration reg) {
    boolean canUpdate = super.canUpdateSurveyType(reg);
    if (canUpdate) {
      // Don't change the survey type if the survey was manually created
      // (i.e. registered by a user other than admin)
      AssessmentRegistration assessment = reg.getAssessment();
      List<SurveyRegistration> surveys = assessment.getSurveyRegList();
      for(SurveyRegistration survey : surveys) {
        ArrayList<Activity> typeChangedActivities =
            activityDao.getActivityByToken(survey.getToken(), Constants.ACTIVITY_REGISTERED);
        if (typeChangedActivities != null) {
          for(Activity activity : typeChangedActivities) {
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

  private boolean isPsychAppt(String visitType) {
    String name = visitType.toUpperCase();
    return name.contains("PSY") || name.contains("PSYCH");
  }

  private boolean isMDAppt(String visitType) {
    String name = visitType.toUpperCase();
    return name.contains(" MD") || name.contains("MD ");
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

    for(int i=0; i<=10; i++) {
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
    for(ApptRegistration survey : surveys) {
      if (survey.getSurveyDt().after(cutoff) &&
          PedPainCustomizer.sameSurveyTypes(survey.getSurveyType(),
              new String[] {PedPainCustomizer.SURVEY_FOLLOWUP, PedPainCustomizer.SURVEY_FOLLOWUP_18})) {
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
    PatientAttribute attr = patientDao.getAttribute(patientId, dataName);
    if (attr == null) {
      return null;
    }

    String strValue = attr.getDataValue();
    if ((strValue == null) || strValue.trim().equals("")) {
      return null;
    }

    Date dateValue = null;
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
   *   PREP - indicates if patient is a PREP program patient
   *   PREP_start - start date of the patient's latest PREP program
   *   PREP_end - end date of the patient's latest PREP program
   *   Baseline - the most recently completed Initial survey date
   */
  public static void refreshPatientAttrs(Database database, Long siteId) {
    PatientDao patientDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));

    // Get the PREP patients
    List<String> PREPPatients = getPREPPatients(database, siteId);
    for(String patientId : PREPPatients) {
      // Get the patient's PREP appointments
      List<Date> appts = getPREPAppointments(database, siteId, patientId);
      // Refresh the patient's PREP attributes
      refreshPREPAttrs(patientDao, patientId, appts);
    }

    // Get the Captivate patients
    List<String> captivatePatients = getCaptivatePatients(database, siteId);
    for(String patientId : captivatePatients) {
      // Get the patient's Captivate appointments
      List<Date> appts = getCaptivateAppointments(database, siteId, patientId);
      // Refresh the patient's PREP attributes
      refreshCaptivateAttrs(patientDao, patientId, appts);
    }

    // Get the most recent completed Initial survey for each patient with
    // with a completed an Initial survey
    Map<String,Date> lastInitialCompletedSurveyDates = getLastCompletedSurveyDates(database, siteId, new String[] {PedPainCustomizer.SURVEY_INITIAL});
    for(String patientId : lastInitialCompletedSurveyDates.keySet()) {
      // Update the Initial attribute for the patient
      Date initial = lastInitialCompletedSurveyDates.get(patientId);
      String initialValue = dateFormat.format(initial);
      PatientAttribute initialAttr = new PatientAttribute(
          patientId, PedPainCustomizer.ATTR_INITIAL, initialValue, PatientAttribute.STRING);
      patientDao.insertAttribute(initialAttr);
    }

    // Get the most earliest completed FollowUpShort survey for each patient with
    // with a completed FollowUpShort survey
    Map<String,Date> firstFollowUpCompletedSurveyDates = getFirstCompletedSurveyDates(database, siteId, PedPainCustomizer.SURVEY_FOLLOWUP_SHORT);
    for(String patientId : firstFollowUpCompletedSurveyDates.keySet()) {
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
    Map<String,Date> lastBaselineCompletedSurveyDates =
        getLastCompletedSurveyDates(database, siteId, new String[] {PedPainCustomizer.SURVEY_PREP_BASELINE, PedPainCustomizer.SURVEY_PREP_DAY_BASELINE});
    for(String patientId : lastBaselineCompletedSurveyDates.keySet()) {
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
      for(int i=0; i<(appts.size()-1); i++) {
        long diff = (appts.get(i).getTime() - appts.get(i+1).getTime());
        long daysBetween = Math.round((double)diff / (double)DateUtils.MILISECONDS_PER_DAY);
        if (daysBetween > 28) {
          start = appts.get(i);
          break;
        }
      }
      // No gap found so start is the earliest appointment
      if (start == null) {
        start = appts.get(appts.size()-1);
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
        "  (upper(visit_type) like 'PREP%' or upper(visit_type) like 'PPPRC%' or upper(visit_type) like 'DAY REHAB%') and " +
        "  lower(registration_type) = 'a' " +
        "union " +
        "  select patient_id from patient_attribute where survey_site_id = :site and " +
        "  data_name in ('PREP', 'PREP_start', 'PREP_end') " +
        ") result";
    List<String> results = database.toSelect(sql)
        .argLong(":site", siteId)
        .query(new RowsHandler<List<String>>() {
          public List<String> process(Rows rs) throws Exception {
            List<String> results = new ArrayList<>();
            while (rs.next()) {
              results.add(rs.getStringOrNull("patient_id"));
            }
            return results;
          }
        });
    return results;
  }

  /**
   * Get the PREP appointment dates for a patient in descending order.
   */
  private static List<Date> getPREPAppointments(Database database, Long siteId, String patientId) {
    String sql =
        "select visit_dt from appt_registration where survey_site_id = :site and " +
        "(upper(visit_type) like 'PREP%' or upper(visit_type) like 'PPPRC%' or upper(visit_type) like 'DAY REHAB%') and " +
        "lower(registration_type) = 'a' and patient_id = ? " +
        "order by visit_dt desc";
    List<Date> results = database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(patientId)
        .query(new RowsHandler<List<Date>>() {
          public List<Date> process(Rows rs) throws Exception {
            List<Date> results = new ArrayList<>();
            while (rs.next()) {
              results.add(rs.getDateOrNull("visit_dt"));
            }
            return results;
          }
        });
    return results;
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
      for(int i=0; i<(appts.size()-1); i++) {
        long diff = (appts.get(i).getTime() - appts.get(i+1).getTime());
        long daysBetween = Math.round((double)diff / (double)DateUtils.MILISECONDS_PER_DAY);
        if (daysBetween > 28) {
          start = appts.get(i);
          break;
        }
      }
      // No gap found so start is the earliest appointment
      if (start == null) {
        start = appts.get(appts.size()-1);
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
        ") patientlist";
    List<String> results = database.toSelect(sql)
        .argLong(":site", siteId)
        .query(new RowsHandler<List<String>>() {
          public List<String> process(Rows rs) throws Exception {
            List<String> results = new ArrayList<>();
            while (rs.next()) {
              results.add(rs.getStringOrNull("patient_id"));
            }
            return results;
          }
        });
    return results;
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
    List<Date> results = database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(patientId)
        .query(new RowsHandler<List<Date>>() {
          public List<Date> process(Rows rs) throws Exception {
            List<Date> results = new ArrayList<>();
            while (rs.next()) {
              results.add(rs.getDateOrNull("visit_dt"));
            }
            return results;
          }
        });
    return results;
  }

  /**
   * Get the date of the most recently completed survey of the survey type for patients
   * who have completed the survey type.
   */
  private static Map<String,Date> getLastCompletedSurveyDates(Database database, Long siteId, String[] surveyTypes) {
    String asmtTypeSql = "(";
    for(String surveyType : surveyTypes) {
      if (!asmtTypeSql.equals("(")) {
        asmtTypeSql += " OR ";
      }
      asmtTypeSql += "asmt.assessment_type like '" + surveyType + ".%'";
    }
    asmtTypeSql += ")";
    String sql =
        "select ar.patient_id, max(asmt.assessment_dt) as survey_dt " +
        "from appt_registration ar left join assessment_registration asmt on asmt.assessment_reg_id = ar.assessment_reg_id " +
        "where ar.survey_site_id = :site and " + asmtTypeSql + " " +
        "and not exists (" + AssessDao.getIncompleteSurveyRegSqlAr() +") " +
        "group by ar.patient_id";
    Map<String,Date> results = database.toSelect(sql)
        .argLong(":site", siteId)
        .query(new RowsHandler<Map<String,Date>>() {
          public Map<String,Date> process(Rows rs) throws Exception {
            Map<String,Date> results = new HashMap<>();
            while (rs.next()) {
              String patientId = rs.getStringOrNull("patient_id");
              Date surveyDate = rs.getDateOrNull("survey_dt");
              results.put(patientId, surveyDate);
            }
            return results;
          }
        });
    return results;
  }

  /**
   * Get the date of the earliest completed survey of the survey type for patients
   * who have completed the survey type.
   */
  private static Map<String,Date> getFirstCompletedSurveyDates(Database database, Long siteId, String surveyType) {
    String sql =
        "select ar.patient_id, min(asmt.assessment_dt) as survey_dt " +
        "from appt_registration ar left join assessment_registration asmt on asmt.assessment_reg_id = ar.assessment_reg_id " +
        "where ar.survey_site_id = :site and asmt.assessment_type like '" + surveyType + ".%' " +
        "and not exists (" + AssessDao.getIncompleteSurveyRegSqlAr() +") " +
        "group by ar.patient_id";
    Map<String,Date> results = database.toSelect(sql)
        .argLong(":site", siteId)
        .query(new RowsHandler<Map<String,Date>>() {
          public Map<String,Date> process(Rows rs) throws Exception {
            Map<String,Date> results = new HashMap<>();
            while (rs.next()) {
              String patientId = rs.getStringOrNull("patient_id");
              Date surveyDate = rs.getDateOrNull("survey_dt");
              results.put(patientId, surveyDate);
            }
            return results;
          }
        });
    return results;
  }

  /**
   * Class to represent a patient encounter on a specific date. This class combines
   * appointment information from all of the patient's appointments on the same day.
   */
  public class PatDateEncounter {
    private String patientId;
    private Date apptDate;
    private String visitType;
    private String regType;
    private String apptComplete;
    private String providerEid;
    private String department;

    /**
     * Create a patient date encounter with the first appointment data.
     */
    public PatDateEncounter(String patientId, Date apptDate, String visitType, String regType, String apptComplete,
                            String providerEid, String department) {
      this.patientId = patientId;
      this.apptDate = apptDate;
      this.visitType = visitType;
      this.regType = regType;
      this.apptComplete = apptComplete;
      this.providerEid = providerEid;
      this.department = department;
    }

    /**
     * Add another appointment to the patient date encounter.
     */
    public void add(Date apptDate, String visitType, String regType, String apptComplete,
                    String providerEid, String department) {
      // If the appointment has been canceled then ignore it. The patient date encounter
      // will be marked as cancelled if the first and all added appointments are cancelled.
      if (regType.equals(Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT)) {
        return;
      }

      // If the current patient date encounter is cancelled then replace the cancelled
      // appointment data with the new appointment data.
      if (this.regType.equals(Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT)) {
        this.apptDate = apptDate;
        this.visitType = visitType;
        this.regType = regType;
        this.apptComplete = apptComplete;
        this.providerEid = providerEid;
        this.department = department;
        return;
      }

      // If the new appointment is earlier in the day then use the new appointment time
      if (apptDate.before(this.apptDate)) {
        this.apptDate = apptDate;
        this.providerEid = providerEid;
        this.department = department;
      }

      // Update the appointment complete status based on the priority of the values
      if (apptComplete == null) {
        // null (unknown) has the highest priority
        this.apptComplete = null;
      } else if (apptComplete.equals(Constants.REGISTRATION_APPT_NOT_COMPLETED)) {
        // Not Completed is the next priority
        if (this.apptComplete != null) {
          this.apptComplete = Constants.REGISTRATION_APPT_NOT_COMPLETED;
        }
      } else {
        // Completed has the lowest priority.
      }

      // Update the visit type of multiple appointments based on the priority of the visit types
      if (visitType != null) {
        if (this.visitType == null) {
          // If the current visit type is null use the new appointment visit type
          this.visitType = visitType;
        } else {
          if (isPREPDayAppt(visitType)) {
            // PREP Day Appt is the highest priority
            this.visitType = "DAY REHAB";
          } else if (isPREPAppt(visitType)) {
            // PREP Appt is the next priority after PREP Day Appt
            if (!isPREPDayAppt(this.visitType)) {
              this.visitType = "PREP";
            }
          } else if (isCaptivateAppt(visitType)) {
            // Captivate Appt is the next priority after PREP Day Appt, PREP Appt
            if (!isPREPDayAppt(this.visitType) &&
                !isPREPAppt(this.visitType)) {
              this.visitType = "CAPTIVATE";
            }
          } else {
            // Last priority is Pain Appt
            if (!isPREPDayAppt(this.visitType) &&
                !isPREPAppt(this.visitType) &&
                !isCaptivateAppt(this.visitType)) {
              if (isInitialAppt(this.visitType) || isInitialAppt(visitType)) {
                this.visitType = "New PAIN";
              } else {
                this.visitType = "PAIN";
              }
            }
          }
        }
      }
    }

    public String getPatientId() {
      return patientId;
    }

    public Date getApptDate() {
      return apptDate;
    }

    public String getVisitType() {
      return visitType;
    }

    public Integer getApptStatus() {
      if (regType.equals(Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT)) {
        return APPT_STATUS_CANCELED;
      }
      if ("Y".equals(apptComplete)) {
        return APPT_STATUS_COMPLETED;
      }
      if ("N".equals(apptComplete)) {
        return APPT_STATUS_NO_SHOW;
      }

      return APPT_STATUS_SCHEDULED;
    }

    String getProviderEid() {
      return providerEid;
    }

    String getDepartment() {
      return department;
    }
  }
}
