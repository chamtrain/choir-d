/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.shared;

public final class Constants {
  public static final String AUTHENTICATED_USER = "AUTHENTICATED_USER";
  public static final int COOKIE_TIMEOUT = 1000 * 60 * 60 * 24;
  public static final String COOKIE_NAME = "registry-user";
  public static final String SERVLET_PATH = "svc/";

  // URL params
  public static final String SITE_ID = "siteId";  // site name
  public static final String SITE_ID_HEADER = "Site-Id";  // site name
  public static final String SURVEY_SITE_PARAM = "s";  // ?s=ped, for example, when taking a survey

  // ChartServlet URL paramaters
  public static final String ASSESSMENT_ID = "assessmentId";
  public static final String ASSESSMENT_ID_LIST = "assessmentIdList";

  public static final String DEFAULT_PATIENTID_FORMATTER = "edu.stanford.registry.server.utils.PatientIdUnformatted";

  // Attributes
  public static final String ATTRIBUTE_PARTICIPATES = "participatesInSurveys";
  public static final String ATTRIBUTE_DECLINED_INVITATIONS = "participatesNoEmails";
  public static final String ATTRIBUTE_DECLINED_REMINDERS = "participatesNoReminders";
  public static final String ATTRIBUTE_DECLINE_REASON_CODE = "declineReasonCode";
  public static final String ATTRIBUTE_DECLINE_REASON_OTHER = "declineReasonOther";
  public static final String ATTRIBUTE_SURVEYEMAIL = "surveyEmailAddress";    // EPIC email address set by appointment loader
  public static final String ATTRIBUTE_SURVEYEMAIL_ALT = "surveyEmailAddressAlt";  // CHOIR email address entered by clinic
  public static final String ATTRIBUTE_SURVEYEMAIL_VALID = "surveyEmailAddressValid"; // y/n to indicate if email address is valid
  public static final String ATTRIBUTE_NOTES = "notes";
  public static final String ATTRIBUTE_GENDER = "gender";
  public static final String ATTRIBUTE_RACE = "race";
  public static final String ATTRIBUTE_ETHNICITY = "ethnicity";
  public static final String ATTRIBUTE_RESEARCH = "research";
  public static final String ATTRIBUTE_HEADACHE = "headachePatient";
  public static final String ATTRIBUTE_PROMIS_PAIN_MEDS = "opioidSurveys";
  public static final String ATTRIBUTE_CATEGORY = "category";
  public static final String ATTRIBUTE_CHRONIC_MIGRAINE="chronicMigraine";

  // Activities (up to 20 characters in field)
  public static final String ACTIVITY_REGISTERED = "Registered";
  public static final String ACTIVITY_CONSENTED = "Consented";
  public static final String ACTIVITY_RESPONDED = "Sent Response";
  public static final String ACTIVITY_DECLINED = "Declined";
  public static final String ACTIVITY_AGREED = "Consented";
  public static final String ACTIVITY_COMPLETED = "Completed";
  public static final String ACTIVITY_CHART_GENERATED= "Chart Generated";
  public static final String ACTIVITY_CHART_PRINTED = "Chart Printed";
  public static final String ACTIVITY_CHART_VIEWED = "Chart Viewed";
  public static final String ACTIVITY_DELETED = "Registration Deleted";
  public static final String ACTIVITY_SURVEY_TYPE_CHANGED = "Survey Type Changed";
  public static final String ACTIVITY_EXTENDED = "extended";


  public static final String EMAIL_REMINDER = "Reminder";
  public static final String EMAIL_TEMPLATE_DIRECTORY = "email.template.directory";
  public static final String EMAIL_TEMPLATE_DIRECTORY_DEFAULT = "default/email-templates/";
  // this is the part stripped off for display
  public static final String EMAIL_TYPE_SUFFIX = ".txt";
  public static final String UNKNOWN = "Unknown";
  public static final String REF_TESTQ = "Test Questionnaire";
  /**
   * file load status, file types and log names
   */

  public static final int FILE_LOAD_UNKNOWN_TYPE = 0;
  public static final int FILE_LOAD_SUCCESSFUL = 1;
  public static final int FILE_LOAD_INVALID_DATA = 2;

  public static final int FILE_TYPE_APPT = 1;
  public static final String[] FILE_LOAD_LOG = { "registry-load-errors", "registry-load-appointments.log" };

  public static final String CONSENT_FORM_DEFAULT = "default/consent_form.txt";
  public static final String XML_PATH_DEFAULT = "default/xml/";
  public static final String XML_INVERT = "invert";
  public static final String XML_PROCESS_ORDER = "order";
  public static final String XML_PROCESS_ORDER_PRINT = "print_order";
  public static final String XML_PROCESS_PRINT_TYPE = "print_type";
  public static final String XML_PROCESS_PRINT_VERSION = "print_version";

  public static final String[] XML_PROCESS_PRINT_TYPES = { "chart", "text", "img", "table", "none" };
  public static final int XML_PROCESS_PRINT_TYPE_CHART = 0;
  public static final int XML_PROCESS_PRINT_TYPE_TEXT = 1;
  public static final int XML_PROCESS_PRINT_TYPE_IMG = 2;
  public static final int XML_PROCESS_PRINT_TYPE_TABLE = 3;
  public static final int XML_PROCESS_PRINT_TYPE_NONE = 4;

  public static final int MS_MINUTE = 60000;
  public static final String ROLE_CLINIC_STAFF = "CLINIC_STAFF";
  public static final String ROLE_DATA_EXCHANGE = "DATA_EXCHANGE";
  public static final String ROLE_SECURTY = "SECURITY";
  public static final String ROLE_PATIENT = "PATIENT";
  public static final String ROLE_DEVELOPER = "DEVELOPER";
  public static final String ROLE_REGISTRATION = "REGISTRATION";
  public static final String ROLE_EDITOR = "EDITOR";
  public static final String ROLE_PHYSICIAN="PHYSICIAN";
  public static final String ROLE_BUILDER="BUILDER";
  public static final String ROLE_API_EXTRACT="API_EXTRACT";
  public static final String ROLE_ASSESSMENT_CONFIG_EDITOR="ASSESSMENT_CONFIG_EDITOR";
  public static final String ROLE_ASSIGN_ASSESSMENT="ASSIGN_ASSESSMENT";

  public static final int DPI_IMAGE_RESOLUTION = 300;
  public static final String VISIT_GROUP_DEFAULT = "return";

  public static final String XL_TEMPLATE_DIRECTORY_DEFAULT = "default/reports/";
  public static final String XL_TEMPLATE_DIRECTORY = "xlsx.template.directory";


  // Registration constants
  public static final String REGISTRATION_TYPE_ACTIVE_APPOINTMENT = "a";
  public static final String REGISTRATION_TYPE_STANDALONE_SURVEY = "s";
  public static final String REGISTRATION_TYPE_CANCELLED_APPOINTMENT = "c";
  public static final String REGISTRATION_APPT_COMPLETED = "Y";
  public static final String REGISTRATION_APPT_NOT_COMPLETED = "N";

  // Schedule widget patient registration action types

  public static final String ACTION_TYPE_ENROLL = "Enroll";
  public static final String ACTION_TYPE_PRINT = "Print";
  public static final String ACTION_TYPE_ASSESSMENT = "Assessment";
  public static final String ACTION_TYPE_IN_PROGRESS = "In progress";
  public static final String ACTION_TYPE_PRINTED = "Printed";
  public static final String ACTION_TYPE_OTHER = "Other";

  // Schedule widget action commands

  public static final String ACTION_CMD_PRINT = "PrintScheduleResults";
  public static final String ACTION_CMD_PRINT_RECENT = "PrintRecentResults";
  public static final String ACTION_CMD_DECLINE_POPUP = "ShowDeclinePopup";
  public static final String ACTION_CMD_EMAIL_POPUP = "ShowEmailPopup";
  public static final String ACTION_CMD_ENROLL_POPUP = "ShowEnrollPopup";
  public static final String ACTION_CMD_START_SURVEY_POPUP = "ShowStartupSurveyPopup";
  public static final String ACTION_CMD_ASSIGN_SURVEY_POPUP = "AssignSurveyPopup";
  public static final String ACTION_CMD_CANCEL_POPUP = "ShowCancelPopup";
  public static final String ACTION_CMD_SET_SURVEY_ATTR = "SetSurveyAttribute";
  public static final String ACTION_CMD_CUSTOM = "CustomAction";

  public static final String ACTION_CMD_PARAM_SURVEY_NAME = "surveyName";
  public static final String ACTION_CMD_PARAM_NAME = "name";
  public static final String ACTION_CMD_PARAM_VALUE = "value";

  // Define the standard Schedule Widget action column labels
  // \u25bc is the unicode character ▼ (down triangle)

  public static final String OPT_ENROLL = "Register the patient | \u25bc";
  public static final String OPT_ASSIGN_SURVEY = "Assign Survey | \u25bc";
  public static final String OPT_ASSESSMENT = "Start assessment | \u25bc";
  public static final String OPT_IN_PROGRESS = "In progress (%completed% of %total%) | \u25bc";
  public static final String OPT_PRINT = "Print results | \u25bc";
  public static final String OPT_NOTHING_DECLINED = "Nothing (patient declined) | \u25bc";
  public static final String OPT_NOTHING_PRINTED = "Nothing (already printed) | \u25bc";
  public static final String OPT_NOTHING_RECENTLY_COMPLETED = "Nothing (assessed %mmdd%) | \u25bc";
  public static final String OPT_NOTHING_INELIGIBLE = "Nothing (Ineligible) | \u25bc";

  // Schedule page default sort parameter
  public static final String SCHED_SORT_PARAM = "default.scheduleSort";
  public static final String SCHED_SORT_DEFAULT = "apptTime";
  // Report Headers
  public static final String[] AVG_TIME_RPT_MONTH_HEADERS = { "Month", "Average Time (MIN:SS)", "Std Deviation (MIN:SS)" };
  public static final String[] AVG_TIME_RPT_TYPE_HEADERS = { "Type", "Average Time (MIN:SS)", "Std Deviation (MIN:SS)" };
  public static final String[] AVG_TIME_RPT_SUMM_HEADERS = { "Average Time (MIN:SS)", "Std Deviation (MIN:SS)" };
  public static final String[] COMPLIANCE1_RPT_HEADERS = { "Status", "Number of Patients" };

  // Custom labels found in configuration parameters
  public static final String PATIENT_ID_LABEL = "PatientIdLabel";

  public static final String ENABLE_CUSTOM_ASSESSMENT_CONFIG = "enable.custom.assessment.config";
  // Default value for custom assessment configuration
  public static final String DEFAULT_CUSTOM_ASSESSMENT_CONFIG_VALUE = "{values:[]}";
  // Custom assessment configuration name in AppConfig entry
  public static final String CUSTOM_ASSESSMENT_CONFIG_NAME = "custom.assessment.config";
  // Custom configuration in Patient attribute to store additional assigned assessments
  public static final String ASSIGNED_ASSESSMENT_DATA_NAME = "assigned.assessment.config";
}
