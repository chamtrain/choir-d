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
package edu.stanford.registry.server.shc.totaljoint;

import edu.stanford.registry.server.RegistryCustomizerDefault;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveyRegistrationAttributeDao;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.reports.ScoresExportReport;
import edu.stanford.registry.server.service.ReportGenerator;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.ApptAction;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.CustomClinicReportConfig;
import edu.stanford.registry.shared.MenuDef;
import edu.stanford.registry.shared.MenuDefIntfUtils;
import edu.stanford.registry.shared.MenuDefIntfUtils.MenuDefBeanFactory;
import edu.stanford.registry.shared.Notification;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class TotalJointCustomizer extends RegistryCustomizerDefault {

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

  public static final String JOINT_HIP = "Hip";
  public static final String JOINT_KNEE = "Knee";
  public static final String SIDE_LEFT = "Left";
  public static final String SIDE_RIGHT = "Right";
  public static final String SIDE_BILATERAL = "Bilateral";

  public static final String SURVEY_NOSURVEY = "NoSurvey";
  public static final String SURVEY_INELIGIBLE = "Ineligible";
  public static final String SURVEY_FOLLOW_UP_DONE = "FollowUpDone";
  public static final String SURVEY_INITIAL_LEFT_HIP = "InitialLeftHip";
  public static final String SURVEY_INITIAL_RIGHT_HIP = "InitialRightHip";
  public static final String SURVEY_INITIAL_BI_HIP = "InitialBiHip";
  public static final String SURVEY_INITIAL_LEFT_KNEE = "InitialLeftKnee";
  public static final String SURVEY_INITIAL_RIGHT_KNEE = "InitialRightKnee";
  public static final String SURVEY_INITIAL_BI_KNEE = "InitialBiKnee";
  public static final String SURVEY_FOLLOW_UP_LEFT_HIP = "FollowUpLeftHip";
  public static final String SURVEY_FOLLOW_UP_RIGHT_HIP = "FollowUpRightHip";
  public static final String SURVEY_FOLLOW_UP_BI_HIP = "FollowUpBiHip";
  public static final String SURVEY_FOLLOW_UP_LEFT_KNEE = "FollowUpLeftKnee";
  public static final String SURVEY_FOLLOW_UP_RIGHT_KNEE = "FollowUpRightKnee";
  public static final String SURVEY_FOLLOW_UP_BI_KNEE = "FollowUpBiKnee";
  public static final String SURVEY_INITIAL_LEFT_HIP_JR = "InitialLeftHipJR";
  public static final String SURVEY_INITIAL_RIGHT_HIP_JR = "InitialRightHipJR";
  public static final String SURVEY_INITIAL_BI_HIP_JR = "InitialBiHipJR";
  public static final String SURVEY_INITIAL_LEFT_KNEE_JR = "InitialLeftKneeJR";
  public static final String SURVEY_INITIAL_RIGHT_KNEE_JR = "InitialRightKneeJR";
  public static final String SURVEY_INITIAL_BI_KNEE_JR = "InitialBiKneeJR";
  public static final String SURVEY_FOLLOW_UP_LEFT_HIP_JR = "FollowUpLeftHipJR";
  public static final String SURVEY_FOLLOW_UP_RIGHT_HIP_JR = "FollowUpRightHipJR";
  public static final String SURVEY_FOLLOW_UP_BI_HIP_JR = "FollowUpBiHipJR";
  public static final String SURVEY_FOLLOW_UP_LEFT_KNEE_JR = "FollowUpLeftKneeJR";
  public static final String SURVEY_FOLLOW_UP_RIGHT_KNEE_JR = "FollowUpRightKneeJR";
  public static final String SURVEY_FOLLOW_UP_BI_KNEE_JR = "FollowUpBiKneeJR";

  public static final String ATTR_FOLLOW_UP_NAME = "FollowUpName";
  public static final String ATTR_FOLLOW_UP_COMPLETED = "FollowUpCompleted";
  public static final String ATTR_SURGERY_DATE = "SurgeryDate";

  public static final String ATTR_HOOS_KOOS_TYPE = "HoosKoosType";

  public static final String ATTR_PAPER_ASSIGNED = "PaperAssigned";
  public static final String ATTR_REFUSED_SURVEY = "RefusedSurvey";

  public static final String SURVEY_COMPLIANCE_REPORT = "Survey Compliance";

  //-------------------------------------------------------
  // Define custom actions
  //-------------------------------------------------------

  protected static final String[][] PARAMS_PAPER_ASSIGNED = new String[][] {
    new String[] { Constants.ACTION_CMD_PARAM_NAME, ATTR_PAPER_ASSIGNED },
    new String[] { Constants.ACTION_CMD_PARAM_VALUE, "true"}
  };

  protected static final String[][] PARAMS_RESET_PAPER_ASSIGNED = new String[][] {
    new String[] { Constants.ACTION_CMD_PARAM_NAME, ATTR_PAPER_ASSIGNED },
    new String[] { Constants.ACTION_CMD_PARAM_VALUE, null}
  };

  protected static final String[][] PARAMS_REFUSED_SURVEY = new String[][] {
    new String[] { Constants.ACTION_CMD_PARAM_NAME, ATTR_REFUSED_SURVEY },
    new String[] { Constants.ACTION_CMD_PARAM_VALUE, "true"}
  };

  protected static final String[][] PARAMS_RESET_REFUSED_SURVEY = new String[][] {
    new String[] { Constants.ACTION_CMD_PARAM_NAME, ATTR_REFUSED_SURVEY },
    new String[] { Constants.ACTION_CMD_PARAM_VALUE, null}
  };

  public TotalJointCustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public ClientConfig getClientConfig() {
    ClientConfig clientConfig = super.getClientConfig();

    Map<String,List<String>> clinicFilterMapping = new LinkedHashMap<>();
    clinicFilterMapping.put("Ortho-Joint", Collections.singletonList("ORTHO-JOINT CLINIC"));
    clinicFilterMapping.put("Ortho-Specialties", Collections.singletonList("ORTHO-SPECIALTIES"));
    clinicFilterMapping.put("Los Gatos Ortho Surgery", Collections.singletonList("LOS GATOS ORTHO SURG"));
    clientConfig.setClinicFilterEnabled(true);
    clientConfig.setClinicFilterAllEnabled(true);
    clientConfig.setClinicFilterValue(null);
    clientConfig.setClinicFilterMapping(clinicFilterMapping);

    clientConfig.addCustomPatientAttribute("TJAOutcomesStudyConsent", "Research consent:", ClientConfig.PATIENT_ATTRIBUTE_TYPE_YESNO);

    clientConfig.setCustomReports(new CustomClinicReportConfig[] {
        new CustomClinicReportConfig(SURVEY_COMPLIANCE_REPORT, SURVEY_COMPLIANCE_REPORT)
    });

    return clientConfig;
  }

  @Override
  public ArrayList<PatientRegistration> determinePatientRegActions(
      Database database, ArrayList<PatientRegistration> registrations) {
    MenuDefBeanFactory factory = AutoBeanFactorySource.create(MenuDefBeanFactory.class);
    MenuDefIntfUtils menuDefUtils = new MenuDefIntfUtils();

    //-------------------------------------------------------
    // Define custom actions
    //-------------------------------------------------------
    final ApptAction ACTION_ASSESSMENT = new ApptAction(
        Constants.ACTION_TYPE_ASSESSMENT, true, Constants.OPT_ASSESSMENT,menuDefUtils.asStringArray(
        Arrays.asList(new MenuDef[] {
            new MenuDef("Start assessment", Constants.ACTION_CMD_START_SURVEY_POPUP),
            new MenuDef("Send email link", Constants.ACTION_CMD_EMAIL_POPUP),
            new MenuDef("Paper assigned", Constants.ACTION_CMD_SET_SURVEY_ATTR, PARAMS_PAPER_ASSIGNED),
            new MenuDef("Refused survey", Constants.ACTION_CMD_SET_SURVEY_ATTR, PARAMS_REFUSED_SURVEY)
        }), factory
    ));

    final ApptAction ACTION_IN_PROGRESS = new ApptAction(
        Constants.ACTION_TYPE_IN_PROGRESS, true, Constants.OPT_IN_PROGRESS, menuDefUtils.asStringArray(
        Arrays.asList(new MenuDef[] {
            new MenuDef("Continue assessment", Constants.ACTION_CMD_START_SURVEY_POPUP),
            new MenuDef("Print partial result", Constants.ACTION_CMD_PRINT),
            new MenuDef("Send email link", Constants.ACTION_CMD_EMAIL_POPUP),
            new MenuDef("Paper assigned", Constants.ACTION_CMD_SET_SURVEY_ATTR, PARAMS_PAPER_ASSIGNED),
            new MenuDef("Refused survey", Constants.ACTION_CMD_SET_SURVEY_ATTR, PARAMS_REFUSED_SURVEY)
        }), factory
    ));

    final ApptAction ACTION_NOTHING_RECENTLY_COMPLETED = new ApptAction(
        Constants.ACTION_TYPE_OTHER, false, Constants.OPT_NOTHING_RECENTLY_COMPLETED, menuDefUtils.asStringArray(
        Arrays.asList(new MenuDef[] {
            new MenuDef("Re-print recent result", Constants.ACTION_CMD_PRINT_RECENT),
            new MenuDef("Start this assessment anyway", Constants.ACTION_CMD_START_SURVEY_POPUP),
            new MenuDef("Paper assigned", Constants.ACTION_CMD_SET_SURVEY_ATTR, PARAMS_PAPER_ASSIGNED),
            new MenuDef("Refused survey", Constants.ACTION_CMD_SET_SURVEY_ATTR, PARAMS_REFUSED_SURVEY)
        }), factory
    ));

    final ApptAction ACTION_NOTHING_COMPLETED = new ApptAction(
        Constants.ACTION_TYPE_OTHER, false, "Nothing (Completed)", menuDefUtils.asStringArray(
        Arrays.asList(new MenuDef[] { }), factory
    ));

    final ApptAction ACTION_PAPER_ASSIGNED = new ApptAction(
        Constants.ACTION_TYPE_OTHER, false, "Paper assigned | \u25bc", menuDefUtils.asStringArray(
        Arrays.asList(new MenuDef[] {
            new MenuDef("Reset paper assigned", Constants.ACTION_CMD_SET_SURVEY_ATTR, PARAMS_RESET_PAPER_ASSIGNED)
        }), factory
    ));

    final ApptAction ACTION_REFUSED_SURVEY = new ApptAction(
        Constants.ACTION_TYPE_OTHER, false, "Refused survey | \u25bc", menuDefUtils.asStringArray(
        Arrays.asList(new MenuDef[] {
            new MenuDef("Reset refused survey", Constants.ACTION_CMD_SET_SURVEY_ATTR, PARAMS_RESET_REFUSED_SURVEY)
        }), factory
    ));
    SurveyRegistrationAttributeDao regAttrDao = new SurveyRegistrationAttributeDao(database);

    for (PatientRegistration registration : registrations ) {
      String surveyType = registration.getSurveyType();
      Map<String,String> regAttrs = regAttrDao.getAttributes(registration.getSurveyReg().getSurveyRegId());

      if (Boolean.parseBoolean(regAttrs.get(ATTR_PAPER_ASSIGNED))) {
        registration.setAction(ACTION_PAPER_ASSIGNED);
      } else if (Boolean.parseBoolean(regAttrs.get(ATTR_REFUSED_SURVEY))) {
        registration.setAction(ACTION_REFUSED_SURVEY);
      } else {
        switch (surveyType) {
        case SURVEY_FOLLOW_UP_DONE:
          registration.setAction(ACTION_NOTHING_COMPLETED);
          break;
        case SURVEY_INELIGIBLE:
          registration.setAction(menuDefUtils.getActionNothingIneligible(factory));
          break;
        case SURVEY_NOSURVEY:
          registration.setAction(menuDefUtils.getActionAssignSurvey(factory));
          break;
        default:
          if (registration.hasDeclined()) {
            registration.setAction(menuDefUtils.getActionNothingDeclined(factory));
          } else if (!registration.getIsDone()) {
            if (Boolean.parseBoolean(regAttrs.get(ATTR_FOLLOW_UP_COMPLETED))) {
              registration.setAction(ACTION_NOTHING_COMPLETED);
            } else if (isRecentlyCompleted(database, registration)) {
              registration.setAction(ACTION_NOTHING_RECENTLY_COMPLETED);
            } else if (registration.getNumberCompleted() > 0) {
              registration.setAction(ACTION_IN_PROGRESS);
            } else {
              if (!registration.hasConsented()) {
                // If the patient has a survey assigned and is not registered for surveys
                // then automatically register the patient for surveys
                registerPatient(database, registration.getPatient());
              }
              registration.setAction(ACTION_ASSESSMENT);
            }
          } else {
            registration.setAction(ACTION_NOTHING_COMPLETED);
          }
          break;
        }
      }
    }
    return registrations;
  }

  @Override
  public boolean registrationNotifiable(Database database,
      PatientRegistration registration, int lastSurveyDaysOut, Date throughDate) {
    SurveyRegistrationAttributeDao regAttrDao = new SurveyRegistrationAttributeDao(database);
    Map<String,String> regAttrs = regAttrDao.getAttributes(registration.getSurveyReg().getSurveyRegId());

    if (Boolean.parseBoolean(regAttrs.get(ATTR_FOLLOW_UP_COMPLETED))) {
      return false;
    }
    if (Boolean.parseBoolean(regAttrs.get(ATTR_PAPER_ASSIGNED))) {
      return false;
    }
    if (Boolean.parseBoolean(regAttrs.get(ATTR_REFUSED_SURVEY))) {
      return false;
    }
    if (isRecentlyCompleted(database, registration)) {
      return false;
    }
    if (isRecentlyEmailed(database, registration)) {
      return false;
    }
    return true;
  }

  @Override
  public void scheduleSurveys(Database database, Date endDate, Long siteId) {
    TotalJointSurveyScheduler scheduler = new TotalJointSurveyScheduler(database, siteInfo);
    scheduler.scheduleSurveys(endDate);
  }

  @Override
  public Map<String,String> getEmailSubstitutions(Database database, PatientRegistration registration) {
    String surveyType = registration.getSurveyType();
    if (surveyType.contains(".")) {
      surveyType = surveyType.substring(0, surveyType.indexOf("."));
    }

    String joint;
    switch (surveyType) {
    case SURVEY_INITIAL_LEFT_HIP:
    case SURVEY_INITIAL_LEFT_HIP_JR:
    case SURVEY_FOLLOW_UP_LEFT_HIP:
    case SURVEY_FOLLOW_UP_LEFT_HIP_JR:
      joint = "left hip";
      break;
    case SURVEY_INITIAL_RIGHT_HIP:
    case SURVEY_INITIAL_RIGHT_HIP_JR:
    case SURVEY_FOLLOW_UP_RIGHT_HIP:
    case SURVEY_FOLLOW_UP_RIGHT_HIP_JR:
      joint = "right hip";
      break;
    case SURVEY_INITIAL_BI_HIP:
    case SURVEY_INITIAL_BI_HIP_JR:
    case SURVEY_FOLLOW_UP_BI_HIP:
    case SURVEY_FOLLOW_UP_BI_HIP_JR:
      joint = "left and right hips";
      break;
    case SURVEY_INITIAL_LEFT_KNEE:
    case SURVEY_INITIAL_LEFT_KNEE_JR:
    case SURVEY_FOLLOW_UP_LEFT_KNEE:
    case SURVEY_FOLLOW_UP_LEFT_KNEE_JR:
      joint = "left knee";
      break;
    case SURVEY_INITIAL_RIGHT_KNEE:
    case SURVEY_INITIAL_RIGHT_KNEE_JR:
    case SURVEY_FOLLOW_UP_RIGHT_KNEE:
    case SURVEY_FOLLOW_UP_RIGHT_KNEE_JR:
      joint = "right knee";
      break;
    case SURVEY_INITIAL_BI_KNEE:
    case SURVEY_INITIAL_BI_KNEE_JR:
    case SURVEY_FOLLOW_UP_BI_KNEE:
    case SURVEY_FOLLOW_UP_BI_KNEE_JR:
      joint = "left and right knees";
      break;
    default:
      joint = "joint";
      break;
    }

    SurveyRegistrationAttributeDao regAttrDao = new SurveyRegistrationAttributeDao(database);
    Map<String,String> regAttrs = regAttrDao.getAttributes(registration.getSurveyReg().getSurveyRegId());

    String surgeryDate = regAttrs.get(TotalJointCustomizer.ATTR_SURGERY_DATE);
    if (surgeryDate == null) {
      surgeryDate = "unknown date";
    }

    Map<String,String> substitutions = new HashMap<>();
    substitutions.put("[JOINT]", joint);
    substitutions.put("[SURGERY_DATE]", surgeryDate);
    substitutions.put("[SURVEY_DATE]", dateFormat.format(registration.getSurveyDt()));
    return substitutions;
  }

  @Override
  public ScoresExportReport getScoresExportReport(Database database, Map<String,String[]> params, SiteInfo siteInfo) {
    return new TotalJointCJRRData(database, siteInfo);
  }

  @Override
  public PatientReport getPatientReport(Database database, SiteInfo siteInfo) {
    return new TotalJointPatientReport(database, siteInfo, ServerUtils.getAdminUser(database));
  }

  @Override
  public ReportGenerator getCustomReportGenerator(String reportType) {
    if (SURVEY_COMPLIANCE_REPORT.equals(reportType)) {
      return new TotalJointReportGenerator();
    }
    return null;
  }

  private void registerPatient(Database database, Patient patient) {
    PatientAttribute pattribute = new PatientAttribute(
        patient.getPatientId(), Constants.ATTRIBUTE_PARTICIPATES, "y", PatientAttribute.STRING);
    Long siteId = siteInfo.getSiteId();
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    patAttribDao.insertAttribute(pattribute);
    patient.addAttribute(pattribute);
  }

  /**
   * Check if the patient has completed survey of the same type since
   * 42 days (6 weeks) prior to the registration.
   */
  private boolean isRecentlyCompleted(Database database, PatientRegistration reg) {
    String patientId = reg.getPatientId();
    String surveyType = reg.getSurveyType();
    Date cutoff = DateUtils.getDaysFromDate(reg.getSurveyDt(), -42);

    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<ApptRegistration> surveys = assessDao.getCompletedRegistrationsByPatient(patientId);
    for(ApptRegistration survey : surveys) {
      if (sameSurveyTypes(survey.getSurveyType(), surveyType)) {
        if (survey.getSurveyDt().after(cutoff)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Check if the patient has been emailed for survey of the same type where the survey
   * date is 14 days prior to the registration; or has a pending notification for survey
   * of the same type for an upcoming registration.
   */
  private boolean isRecentlyEmailed(Database database, PatientRegistration reg) {
    String regSurveyType = reg.getSurveyType();
    Date cutoff = DateUtils.getDaysFromDate(reg.getSurveyDt(), -14);
    Date today = DateUtils.getDateStart(new Date());

    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<Notification> notifications = assessDao.getNotificationsByPatient(reg.getPatientId());
    for(Notification notification : notifications) {
      if (sameSurveyTypes(notification.getEmailType(), regSurveyType)) {
        // Email was sent and survey is later than 14 days prior than this registration
        if ((notification.getEmailDt() != null) && notification.getSurveyDt().after(cutoff)) {
          return true;
        }
        // Pending notification exists and survey is after today
        if (notification.getSurveyDt().after(today)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Compares survey type names ignoring the version indicator
   */
  private static boolean sameSurveyTypes(String surveyType1, String surveyType2) {
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

}
