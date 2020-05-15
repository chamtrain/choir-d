/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.RegistryCustomizerDefault;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.hl7.Hl7CustomizerIntf;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.reports.ScoresExportReport;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.ApptAction;
import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.MenuDef;
import edu.stanford.registry.shared.MenuDefIntfUtils;
import edu.stanford.registry.shared.MenuDefIntfUtils.MenuDefBeanFactory;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.SurveyRegistration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class PedPainCustomizer extends RegistryCustomizerDefault {

  private static final Logger logger = Logger.getLogger(PedPainCustomizer.class);
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

  public static final String ATTR_PREP_PATIENT = "PREP";
  public static final String ATTR_PREP_START = "PREP_start";
  public static final String ATTR_PREP_END = "PREP_end";
  public static final String ATTR_CAPTIVATE_PATIENT = "CAPTIVATE";
  public static final String ATTR_CAPTIVATE_START = "CAPTIVATE_start";
  public static final String ATTR_CAPTIVATE_END = "CAPTIVATE_end";
  public static final String ATTR_BASELINE = "Baseline";
  public static final String ATTR_INITIAL = "Initial";
  public static final String ATTR_CONSENT = "PedResearchConsent";
  public static final String ATTR_CONSENT_18 = "PedResearchConsent18";
  public static final String ATTR_ASSENT = "PedResearchAssent";
  public static final String ATTR_PREP_CONSENT = "PrepResearchConsent";
  public static final String ATTR_PREP_CONSENT_18 = "PrepResearchConsent18";
  public static final String ATTR_PREP_ASSENT = "PrepResearchAssent";
  public static final String ATTR_PHOTO_PERMISSION = "PhotoPermission";
  public static final String ATTR_CAPTIVATE_CONSENT = "CaptivateConsent";
  public static final String ATTR_CAPTIVATE_ASSENT = "CaptivateAssent";
  public static final String ATTR_CAPTIVATE_VIDEO_PERMISSION = "CapVideoPermission";
  public static final String ATTR_RESEARCH_DATABASE = "research";

  public static final String SURVEY_NOSURVEY = "NoSurvey";
  public static final String SURVEY_INITIAL = "Initial";
  public static final String SURVEY_FOLLOWUP = "FollowUp";
  public static final String SURVEY_FOLLOWUP_SHORT = "FollowUpShort";
  public static final String SURVEY_FOLLOWUP_18 = "FollowUp18";
  public static final String SURVEY_FOLLOWUP_SHORT_18 = "FollowUpShort18";
  public static final String SURVEY_PREP_BASELINE = "InitialPReP";
  public static final String SURVEY_PREP_COMPLETION = "EndPReP";
  public static final String SURVEY_PREP = "PReP";
  public static final String SURVEY_PREP_DAY_BASELINE = "InitialPRePd";
  public static final String SURVEY_PREP_DAY_COMPLETION = "EndPRePd";
  public static final String SURVEY_PREP_DAY = "PRePd";
  public static final String SURVEY_CAPTIVATE_INITIAL = "InitialCAP";
  public static final String SURVEY_CAPTIVATE_SHORT = "CAP";
  public static final String SURVEY_CAPTIVATE_FINAL = "EndCAP";
  public static final String SURVEY_CAPTIVATE_FOLLOWUP = "FollowUpCAP";

  //-------------------------------------------------------
  // Define custom actions
  //-------------------------------------------------------

  public static final String SURVEY_NAME_PARENT = "Parent";
  public static final String SURVEY_NAME_CHILD = "Child";
  public static final String CUSTOM_CMD_SKIP_PARENT = "SkipParentSurvey";
  public static final String CUSTOM_CMD_SKIP_CHILD = "SkipChildSurvey";

  public final MenuDef SKIP_PARENT_MENU_DEF = MenuDef.customMenuDef("Skip parent survey", CUSTOM_CMD_SKIP_PARENT, null,
      "This will remove the Parent survey. Any responses that may have been entered if the survey had already been started will be deleted. Select OK to continue.");
  public final MenuDef SKIP_CHILD_MENU_DEF = MenuDef.customMenuDef("Skip child survey", CUSTOM_CMD_SKIP_CHILD, null,
      "This will remove the Child survey. Any responses that may have been entered if the survey had already been started will be deleted. Select OK to continue.");

  public PedPainCustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public ClientConfig getClientConfig() {
    ClientConfig clientConfig = super.getClientConfig();
    Map<String,List<String>> clinicFilterMapping = new LinkedHashMap<>();
    clinicFilterMapping.put("Menlo Park", Arrays.asList("PAIN MENLO PARK 321 MIDDLEFIELD","PAIN PROGRAM MENLO PARK 321 MIDDLEFIELD"));
    clinicFilterMapping.put("Emeryville", Collections.singletonList("PAIN EMERYVILLE 6121 HOLLIS"));
    clinicFilterMapping.put("Los Gatos", Collections.singletonList("PAIN LOS GATOS 14601 S BASCOM"));
    clinicFilterMapping.put("Sunnyvale", Collections.singletonList("PAIN SUNNYVALE 1195 W FREMONT"));
    clientConfig.setClinicFilterEnabled(true);
    clientConfig.setClinicFilterAllEnabled(true);
    clientConfig.setClinicFilterValue(null);
    clientConfig.setClinicFilterMapping(clinicFilterMapping);
    clientConfig.addCustomTab(Constants.ROLE_API_EXTRACT, "shc/pedpain/pedSurveyExtract.html", "Extract square table data");
    return clientConfig;
  }

  @Override
  public ArrayList<PatientRegistration> determinePatientRegActions(
      Database database, ArrayList<PatientRegistration> registrations) {
    MenuDefBeanFactory factory = AutoBeanFactorySource.create(MenuDefBeanFactory.class);
    MenuDefIntfUtils menuDefUtils = new MenuDefIntfUtils();
    for (PatientRegistration registration : registrations ) {
      String surveyType = registration.getSurveyType();

      if (registration.hasDeclined()) {
        registration.setAction(menuDefUtils.getActionNothingDeclined(factory));
      } else if (surveyType.equals(SURVEY_NOSURVEY)) {
        registration.setAction(menuDefUtils.getActionNothingIneligible(factory));
      } else if (!registration.hasConsented()) {
        registration.setAction(menuDefUtils.getActionEnroll(factory));
      } else if (!registration.getIsDone()) {
        if (isRecentlyCompleted(database, registration)) {
          ApptAction action = getAction(Constants.OPT_NOTHING_RECENTLY_COMPLETED, registration, menuDefUtils, factory);
          registration.setAction(action);
        } else if (registration.getNumberCompleted() > 0) {
          ApptAction action = getAction(Constants.OPT_IN_PROGRESS, registration, menuDefUtils, factory);
          registration.setAction(action);
        } else {
          ApptAction action = getAction(Constants.OPT_ASSESSMENT, registration, menuDefUtils, factory);
          registration.setAction(action);
        }
      } else if (registration.getNumberPrints() == 0) {
        registration.setAction(menuDefUtils.getActionPrint(factory));
      } else {
        registration.setAction(menuDefUtils.getActionNothingPrinted(factory));
      }
    }
    return registrations;
  }

  @Override
  public boolean registrationNotifiable(Database database,
      PatientRegistration registration, int lastSurveyDaysOut, Date throughDate) {

    String patientId = registration.getPatientId();
    ApptId apptId = registration.getApptId();
    Date tomorrow = DateUtils.getDaysOutDate(siteInfo, 1);
    String surveyType = registration.getSurveyType();

    // Email for PReP surveys are only sent 2 days before
    if ((surveyType!=null) && surveyType.contains("PReP")) {
      Date now = new Date();
      Date emailAfterDate = DateUtils.getDaysFromDate(siteInfo, registration.getSurveyDt(), -2);
      if (now.before(emailAfterDate)) {
        logger.debug("Too early to send email for PReP survey " + patientId + ", ApptRegId " + apptId +
            " email to be sent after " + dateFormat.format(emailAfterDate)) ;
        return false;
      }
    }

    // If PReP in progress then don't send emails for other surveys
    try {
      if (PREPInProgress(database, siteInfo, patientId, registration.getSurveyDt())) {
        if ((surveyType == null) || !surveyType.contains("PReP")) {
          logger.debug("PReP in progress, not sending email for " + patientId + ", ApptRegId " + apptId + ", SurveyType " + surveyType);
          return false;
        }
      }
    } catch (Exception e) {
      logger.error("Unexpected error", e);
    }

    // CAP appointments are weekly so only sent 5 days before
    if ((surveyType!=null) && (surveyType.startsWith("CAP") || surveyType.startsWith("EndCAP"))) {
      Date now = new Date();
      Date emailAfterDate = DateUtils.getDaysFromDate(siteInfo, registration.getSurveyDt(), -5);
      if (now.before(emailAfterDate)) {
        logger.debug("Too early to send email for CAP survey " + patientId + ", ApptRegId " + apptId +
            " email to be sent after " + dateFormat.format(emailAfterDate)) ;
        return false;
      }
    }

    // If CAPTIVATE in progress then don't send emails for other surveys
    try {
      if (CaptivateInProgress(database, siteInfo, patientId, registration.getSurveyDt())) {
        if ((surveyType == null) || !surveyType.contains("CAP")) {
          logger.debug("CAPTIVATE in progress, not sending email for " + patientId + ", ApptRegId " + apptId + ", SurveyType " + surveyType);
          return false;
        }
      }
    } catch (Exception e) {
      logger.error("Unexpected error", e);
    }

    // Check if a notification already exists for a different registration
    // between tomorrow and throughDate
    AssessDao assessDao = new AssessDao(database, siteInfo);
    int notifications = assessDao.getNotificationCount(patientId, tomorrow, throughDate, registration.getApptId());
    if (notifications > 0) {
      logger.debug("Other notified surveys found for patient " + patientId + ", ApptRegId " + apptId +
          " between " + dateFormat.format(tomorrow) + " and " + dateFormat.format(throughDate));
      return false;
    }

    // Check if the user has already completed a survey for a different registration
    if (isRecentlyCompleted(database, registration)) {
      logger.debug("Other recently completed surveys found for patient " + patientId + ", ApptRegId " + apptId);
      return false;
    }

    return true;
  }

  @Override
  public void scheduleSurveys(Database database, Date endDate, Long siteId) {
    // Create scheduled surveys 15 days in advance
    endDate = DateUtils.getDaysOutDate(siteInfo, 15);
    PedPainSurveyScheduler scheduler = new PedPainSurveyScheduler(siteInfo);
    scheduler.scheduleSurveys(database, endDate);
  }

  @Override
  public Map<String,String> getEmailSubstitutions(Database database, PatientRegistration registration) {
    SurveyRegistration parentSurvey = registration.getSurveyReg(SURVEY_NAME_PARENT);
    SurveyRegistration childSurvey = registration.getSurveyReg(SURVEY_NAME_CHILD);

    Map<String,String> substitutions = new HashMap<>();
    if (parentSurvey == null) {
      substitutions.put("[SURVEY_LINK_PARENT]", "Parent/Caregiver questionnaire does not need to be completed at this time.");
    }
    if (childSurvey == null) {
      substitutions.put("[SURVEY_LINK_CHILD]", "Child questionnaire does not need to be completed at this time.");
    }
    return substitutions;
  }

  @Override
  public ScoresExportReport getScoresExportReport(Database database, Map<String,String[]> params, SiteInfo siteInfo) {
    String[] values = params.get("type");
    String type = (values != null) ? values[0] : null;
    if ("BackgroundData".equals(type)) {
      return new PedPainBackgroundData(database, siteInfo);
    }
   return new PedPainSurveyData(database, siteInfo);
  }

  @Override
  public PatientReport getPatientReport(Database database, SiteInfo siteInfo) {
    return new PedPainPatientReport(database, siteInfo, ServerUtils.getAdminUser(database));
  }

  @Override
  public ChartConfigurationOptions getConfigurationOptions() {
    ChartConfigurationOptions chartConfig = new ChartConfigurationOptions(new ConfigurationOptions());
    chartConfig.setOption(ConfigurationOptions.OPTION_CHART_PERCENTILES, ConfigurationOptions.FALSE);
    return chartConfig;
  }

  @Override
  public Boolean customActionMenuCommand(Database db, ClinicServices clinicServices,
      String action, AssessmentId asmtId, Map<String,String> params) {
    // Delete the parent survey
    if (CUSTOM_CMD_SKIP_PARENT.equals(action)) {
      AssessDao assessDao = new AssessDao(db, siteInfo);
      PatStudyDao patStudyDao = new PatStudyDao(db, siteInfo);

      AssessmentRegistration asmt = assessDao.getAssessmentById(asmtId);
      SurveyRegistration sreg = asmt.getSurveyReg(SURVEY_NAME_PARENT);
      if (sreg != null) {
        patStudyDao.deletePatientStudy(sreg.getToken());
        assessDao.deleteSurveyRegistration(sreg);
      }

      return Boolean.TRUE;
    }

    // Delete the Child survey
    if (CUSTOM_CMD_SKIP_CHILD.equals(action)) {
      AssessDao assessDao = new AssessDao(db, siteInfo);
      PatStudyDao patStudyDao = new PatStudyDao(db, siteInfo);

      AssessmentRegistration asmt = assessDao.getAssessmentById(asmtId);
      SurveyRegistration sreg = asmt.getSurveyReg(SURVEY_NAME_CHILD);
      if (sreg != null) {
        patStudyDao.deletePatientStudy(sreg.getToken());
        assessDao.deleteSurveyRegistration(sreg);
      }

      return Boolean.TRUE;
    }
    throw new IllegalArgumentException("Custom menu action " + action + " is not supported");
  }

  /**
   * Determine the actions for the menu options.
   */
  private ApptAction getAction(String menuOption, PatientRegistration registration, MenuDefIntfUtils menuDefUtils, MenuDefBeanFactory factory) {
    SurveyRegistration parentSurvey = registration.getSurveyReg(SURVEY_NAME_PARENT);
    SurveyRegistration childSurvey = registration.getSurveyReg(SURVEY_NAME_CHILD);

    ApptAction apptAction;
    if (menuOption.equals(Constants.OPT_NOTHING_RECENTLY_COMPLETED)) {
      List<MenuDef> actions = new ArrayList<>();
      actions.add(new MenuDef("Re-print recent result", Constants.ACTION_CMD_PRINT_RECENT));
      actions.add(new MenuDef("Start this assessment anyway", Constants.ACTION_CMD_START_SURVEY_POPUP));
      if ((parentSurvey != null) && (childSurvey != null)) {
        actions.add(SKIP_PARENT_MENU_DEF);
        actions.add(SKIP_CHILD_MENU_DEF);
      }
      actions.add(new MenuDef("Decline this patient", Constants.ACTION_CMD_DECLINE_POPUP));
      apptAction = new ApptAction(Constants.ACTION_TYPE_OTHER, false, Constants.OPT_NOTHING_RECENTLY_COMPLETED, menuDefUtils.asStringArray(actions, factory));
    } else if (menuOption.equals(Constants.OPT_IN_PROGRESS)) {
      List<MenuDef> actions = new ArrayList<>();
      actions.add(new MenuDef("Continue assessment", Constants.ACTION_CMD_START_SURVEY_POPUP));
      if ((parentSurvey != null) && (childSurvey != null)) {
        actions.add(SKIP_PARENT_MENU_DEF);
        actions.add(SKIP_CHILD_MENU_DEF);
      }
      actions.add(new MenuDef("Print partial result", Constants.ACTION_CMD_PRINT));
      actions.add(new MenuDef("Send email link", Constants.ACTION_CMD_EMAIL_POPUP));
      actions.add(new MenuDef("Decline this patient", Constants.ACTION_CMD_DECLINE_POPUP));
      apptAction = new ApptAction(Constants.ACTION_TYPE_IN_PROGRESS, true, Constants.OPT_IN_PROGRESS, menuDefUtils.asStringArray(actions, factory));
    } else if (menuOption.equals(Constants.OPT_ASSESSMENT)) {
      List<MenuDef> actions = new ArrayList<>();
      actions.add(new MenuDef("Start assessment", Constants.ACTION_CMD_START_SURVEY_POPUP));
      if ((parentSurvey != null) && (childSurvey != null)) {
        actions.add(SKIP_PARENT_MENU_DEF);
        actions.add(SKIP_CHILD_MENU_DEF);
      }
      actions.add(new MenuDef("Send email link", Constants.ACTION_CMD_EMAIL_POPUP));
      actions.add(new MenuDef("Decline this patient", Constants.ACTION_CMD_DECLINE_POPUP));
      apptAction = new ApptAction(Constants.ACTION_TYPE_ASSESSMENT, true, Constants.OPT_ASSESSMENT, menuDefUtils.asStringArray(actions, factory));
    } else {
      throw new IllegalArgumentException();
    }
    return apptAction;
  }

  /**
   * Check if another survey has been recently completed. The meaning of recently
   * completed depends on the type of the survey. For each of the survey types,
   * the survey is recently completed if
   *
   * Initial       An Initial survey was completed with a survey date after 29 days before this survey
   * FollowUpShort Any survey was completed with a survey date after 29 days before this survey
   * FollowUp      A FollowUp survey was completed with a survey date after 29 days before this survey or
   *               Any non-FollowUp survey was completed with a survey date after 13 days before this survey
   * InitialPReP   An InitialPReP survey was completed with a survey date after 29 days before this survey
   * PReP          Any survey was completed with a survey date after 6 days before this survey
   * EndPReP       An EndPReP survey was completed with a survey date after 6 days before this survey
   *
   * Since the time period for recently completed varies depending of the
   * survey type, the appointment.lastsurvey.daysout parameter is not used
   * and the values are hard coded.
   */
  private boolean isRecentlyCompleted(Database database, PatientRegistration reg) {
    String regSurveyType = reg.getSurveyType();

    Date cutoff = null;
    Date cutoff2 = null;
    if (sameSurveyTypes(regSurveyType, SURVEY_INITIAL)) {
      cutoff = DateUtils.getDaysFromDate(siteInfo, reg.getSurveyDt(), -29);
    } else if (sameSurveyTypes(regSurveyType, new String[] {SURVEY_FOLLOWUP_SHORT, SURVEY_FOLLOWUP_SHORT_18})) {
      cutoff = DateUtils.getDaysFromDate(siteInfo, reg.getSurveyDt(), -29);
    } else if (sameSurveyTypes(regSurveyType, new String[] {SURVEY_FOLLOWUP, SURVEY_FOLLOWUP_18})) {
      cutoff = DateUtils.getDaysFromDate(siteInfo, reg.getSurveyDt(), -29);
      cutoff2 = DateUtils.getDaysFromDate(siteInfo, reg.getSurveyDt(), -13);
    } else if (sameSurveyTypes(regSurveyType, new String[] {SURVEY_PREP_BASELINE, SURVEY_PREP_DAY_BASELINE})) {
      cutoff = DateUtils.getDaysFromDate(siteInfo, reg.getSurveyDt(), -29);
    } else if (sameSurveyTypes(regSurveyType, new String[] {SURVEY_PREP, SURVEY_PREP_DAY})) {
      cutoff = DateUtils.getDaysFromDate(siteInfo, reg.getSurveyDt(), -6);
    } else if (sameSurveyTypes(regSurveyType, new String[] {SURVEY_PREP_COMPLETION, SURVEY_PREP_DAY_COMPLETION})) {
      cutoff = DateUtils.getDaysFromDate(siteInfo, reg.getSurveyDt(), -6);
    } else {
      cutoff = DateUtils.getDaysFromDate(siteInfo, reg.getSurveyDt(), -29);
    }

    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<ApptRegistration> surveys = assessDao.getCompletedRegistrationsByPatient(reg.getPatientId());
    for(ApptRegistration survey : surveys) {
      String compSurveyType = survey.getSurveyType();

      // Initial - same survey
      if (sameSurveyTypes(regSurveyType, SURVEY_INITIAL) &&
          sameSurveyTypes(compSurveyType, SURVEY_INITIAL) &&
          survey.getSurveyDt().after(cutoff)) {
        return true;
      }
      // FollowUpShort - any survey
      if (sameSurveyTypes(regSurveyType, new String[] {SURVEY_FOLLOWUP_SHORT, SURVEY_FOLLOWUP_SHORT_18}) &&
          survey.getSurveyDt().after(cutoff)) {
        return true;
      }
      // FollowUp - same survey
      if (sameSurveyTypes(regSurveyType, new String[] {SURVEY_FOLLOWUP, SURVEY_FOLLOWUP_18}) &&
          sameSurveyTypes(compSurveyType, new String[] {SURVEY_FOLLOWUP, SURVEY_FOLLOWUP_18}) &&
          survey.getSurveyDt().after(cutoff)) {
        return true;
      }
      // FollowUp - any other survey
      if (sameSurveyTypes(regSurveyType, new String[] {SURVEY_FOLLOWUP, SURVEY_FOLLOWUP_18}) &&
          !sameSurveyTypes(compSurveyType, new String[] {SURVEY_FOLLOWUP, SURVEY_FOLLOWUP_18}) &&
          survey.getSurveyDt().after(cutoff2)) {
        return true;
      }
      // InitialPReP - same survey
      if (sameSurveyTypes(regSurveyType, SURVEY_PREP_BASELINE) &&
          sameSurveyTypes(compSurveyType, SURVEY_PREP_BASELINE) &&
          survey.getSurveyDt().after(cutoff)) {
        return true;
      }
      // PReP - any survey
      if (sameSurveyTypes(regSurveyType, SURVEY_PREP) &&
          survey.getSurveyDt().after(cutoff)) {
        return true;
      }
      // EndPReP - same survey
      if (sameSurveyTypes(regSurveyType, SURVEY_PREP_COMPLETION) &&
          sameSurveyTypes(compSurveyType, SURVEY_PREP_COMPLETION) &&
          survey.getSurveyDt().after(cutoff)) {
        return true;
      }
      // InitialPRePd - same survey
      if (sameSurveyTypes(regSurveyType, SURVEY_PREP_DAY_BASELINE) &&
          sameSurveyTypes(compSurveyType, SURVEY_PREP_DAY_BASELINE) &&
          survey.getSurveyDt().after(cutoff)) {
        return true;
      }
      // PRePd - any survey
      if (sameSurveyTypes(regSurveyType, SURVEY_PREP_DAY) &&
          survey.getSurveyDt().after(cutoff)) {
        return true;
      }
      // EndPRePd - same survey
      if (sameSurveyTypes(regSurveyType, SURVEY_PREP_DAY_COMPLETION) &&
          sameSurveyTypes(compSurveyType, SURVEY_PREP_DAY_COMPLETION) &&
          survey.getSurveyDt().after(cutoff)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Compares survey type names ignoring the version indicator
   */
  public static boolean sameSurveyTypes(String surveyType1, String[] surveyTypes) {
    for(String surveyType : surveyTypes) {
      if (sameSurveyTypes(surveyType1, surveyType)) {
        return true;
      }
    }
    return false;
  }

  public static boolean sameSurveyTypes(String surveyType1, String surveyType2) {
    String surveyName1 = surveyType1;
    if (surveyType1.contains(".")) {
      surveyName1 = surveyType1.substring(0, surveyType1.indexOf("."));
    }
    String surveyName2 = surveyType2;
    if (surveyType2.contains(".")) {
      surveyName2 = surveyType2.substring(0, surveyType2.indexOf("."));
    }

    return surveyName1.equals(surveyName2);
  }

  public static boolean is18AndOver(Database database, Long siteId, String patientId) {
    PatientDao patientDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    Patient patient = patientDao.getPatient(patientId);
    if (patient != null) {
      return DateUtils.getAge(patient.getDtBirth()) >= 18;
    }
    return false;
  }

  public static boolean PREPInProgress(Database database, SiteInfo siteInfo, String patientId, Date apptDate) throws Exception {
    Date start = getDateAttr(database, siteInfo.getSiteId(), patientId, PedPainCustomizer.ATTR_PREP_START);
    Date end = getDateAttr(database, siteInfo.getSiteId(), patientId, PedPainCustomizer.ATTR_PREP_END);
    if ((start == null) || (end == null)) {
      return false;
    }

    start = DateUtils.getDateStart(siteInfo, start);
    // Allow an extra 7 days as the PReP appointments are scheduled weekly and
    // the end date may be extended
    end = DateUtils.getDaysFromDate(siteInfo, end, 7);
    end = DateUtils.getDateEnd(siteInfo, end);

    return apptDate.after(start) && apptDate.before(end);
  }

  public static boolean CaptivateInProgress(Database database, SiteInfo siteInfo, String patientId, Date apptDate) throws Exception {
    Date start = getDateAttr(database, siteInfo.getSiteId(), patientId, PedPainCustomizer.ATTR_CAPTIVATE_START);
    Date end = getDateAttr(database, siteInfo.getSiteId(), patientId, PedPainCustomizer.ATTR_CAPTIVATE_END);
    if ((start == null) || (end == null)) {
      return false;
    }

    start = DateUtils.getDateStart(siteInfo, start);
    end = DateUtils.getDaysFromDate(siteInfo, end, 7);
    end = DateUtils.getDateEnd(siteInfo, end);

    return apptDate.after(start) && apptDate.before(end);
  }

  public static Date getDateAttr(Database database, Long siteId, String patientId, String dataName) throws Exception {
    PatientDao patientDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
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
  @Override
  public Hl7CustomizerIntf getHl7Customizer() {
    return new PedPainHl7Customizer(siteInfo);
  }
public ArrayList<String> apiExportTables() {
    String[] KNOWN_TABLES =  {  "rpt_pedpain_surveys" };
    ArrayList<String> tables = new ArrayList<>();
    Collections.addAll(tables, KNOWN_TABLES);
    return tables;
  }
}
