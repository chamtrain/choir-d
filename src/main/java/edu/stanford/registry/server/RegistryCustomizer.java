/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server;

import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.hl7.Hl7CustomizerIntf;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.reports.ScoresExportReport;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.service.ApiCustomHandler;
import edu.stanford.registry.server.service.ApiReportGenerator;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.ReportGenerator;
import edu.stanford.registry.server.service.rest.CustomRestletHandler;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.User;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;

/**
 * A Registry site server side customizations. This interface defines methods which are
 * called on the server side to customize the registry.
 *
 */
public interface RegistryCustomizer {

  // Constants for pre-defined patient search types
  String PATIENT_SEARCH_BY_PATIENT_ID = "0";
  String PATIENT_SEARCH_BY_PARTIAL_NAME = "1";
  String PATIENT_SEARCH_BY_EMAIL = "2";
  
  /**
   * Get the Registry App client configuration.
   */
  ClientConfig getClientConfig();

  /**
   * Determine the recommended action to be displayed for each of the
   * patient registrations. This returns an updated list of patient
   * registrations.
   */
  ArrayList<PatientRegistration> determinePatientRegActions(
      Database database, ArrayList<PatientRegistration> registrations);

  /**
   * Determine if the patient registration should be sent an email notification.
   * This should check conditions such as if the patient has already been sent
   * another notification or if the patient has recently completed another survey.
   *
   * @param database
   * @param registration Patient registration
   * @param lastSurveyDaysOut Value of configuration parameter appointment.lastsurvey.daysout
   * @param throughDate Date based on configuration parameter appointment.initialemail.daysout
   * @return boolean indicating if the registration is notifiable
   */
  boolean registrationNotifiable(Database database, PatientRegistration registration,
                                 int lastSurveyDaysOut, Date throughDate);

  /**
   * Create survey registrations for upcoming stand alone surveys up through
   * the specified date.
   *
   * @param database
   * @param throughDate Date based on configuration parameter appointment.initialemail.daysout
   */
  void scheduleSurveys(Database database, Date throughDate, Long siteId);

  /**
   * Get the mapping from the email substitution variables to their values
   * for the patient registration.
   */
  Map<String,String> getEmailSubstitutions(Database database, PatientRegistration registration);

  /**
   * Get the ScoresExportReport instance used to get the survey scores data.
   */
  ScoresExportReport getScoresExportReport(Database database, Map<String, String[]> params, SiteInfo siteInfo);

  /**
   * Get the custom Restlet API actions
   */
  Map<String,Class<? extends CustomRestletHandler>> getRestletActions();

  /**
   * Get the PatientReport instance used to create a pdf for the registration.
   */
  PatientReport getPatientReport(Database database, SiteInfo siteInfo);

  /**
   * Get the report colors, line styles and sizing options
   * @return
   */
  ChartConfigurationOptions getConfigurationOptions();

  /**
   * Get attachment files for a given template
   */
  List<File> getEmailAttachments(String template);

  /**
   * Get the report generator for the custom report
   */
  ReportGenerator getCustomReportGenerator(String reportType);

  /**
   * Get the API report generator for a custom report
   */
  ApiReportGenerator getCustomApiReportGenerator(String reportType);

  /**
   * Get the patients registrations
   */
  ArrayList<PatientRegistration> getPatientRegistrations(AssessDao assessDao, Patient pat, Date date);

  /**
   * Determine the patient search type for the search string.
   * 
   * This method should return one of the predefined PATIENT_SEARCH_BY values
   * or the name of a patient attribute.
   */
  String getPatientSearchType(String searchString);
  
  /**
   * Return the actual search string to be used for the search type and user
   * entered search string.
   */
  String getPatientSearchValue(String searchType, String searchString);

  /**
   * This treatment set is new, so has a state=Unset.
   *
   * If it should not be offered to the UI, return null;
   *
   * If it should be grayed (disabled), set its state to NotYetQualified and optionally
   * set its reason for the tool tip.
   */
  RandomSetParticipant disableUiTreatmentSet(Database db, RandomSetParticipant part);
  
  /**
   * Handle the custom action menu command.
   */
  Boolean customActionMenuCommand(Database db, ClinicServices clinicServices,
                                  String action, AssessmentId asmtId, Map<String, String> params);
  
  /**
   * Patient registration callback
   * 
   * This method is called when a patient is registered and can be overridden to
   * perform actions when a patient is registered.
   */
  void handlePatientRegistration(Database db, Patient patient);
  
  /**
   * This method is called by the background thread to do the send email processing.
   * 
   * The global parameter registry.sendEmail.interval.minutes determines how often
   * this method will be called. The default is 0 (i.e. this method will not be called).
   * 
   * The site parameter batch.email.sending enables or disables batch sending of emails
   * for a particular site. The default is false (batch sending is disabled).
   */
  void batchSendEmails(DatabaseProvider dbp, AdministrativeServices adminServices) throws Exception;

  ArrayList<String> apiExportTables();
  
  /**
   * Return the path templates for any custom API calls
   */
  List<String> getApiCustomPaths();
  
  /**
   * Return the custom API handler that will process the custom API calls
   */
  ApiCustomHandler getApiCustomHandler(Supplier<Database> dbp, SiteInfo siteInfo,
                                       User user, ClinicServices clinicServices);

  String IRBCountsConsentAttribute();

  Hl7CustomizerIntf getHl7Customizer();
}
