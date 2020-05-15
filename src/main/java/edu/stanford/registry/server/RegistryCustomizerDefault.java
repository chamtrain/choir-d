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
import edu.stanford.registry.server.hl7.Hl7Customizer;
import edu.stanford.registry.server.hl7.Hl7CustomizerIntf;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.reports.ScoresExportReport;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.service.ApiCustomHandler;
import edu.stanford.registry.server.service.ApiReportGenerator;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.ReportGenerator;
import edu.stanford.registry.server.service.rest.CustomRestletHandler;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.CommonUtils;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.MenuDefIntfUtils;
import edu.stanford.registry.shared.MenuDefIntfUtils.MenuDefBeanFactory;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.User;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
/**
 * Default implementation of the RegistryCustomizer interface.
 *
 * This potentially applies to each clinic.
 * Each clinic should it to their own class, change the "implements RegistryCustomizer"
 * to "extends RegistryCustomizerDefault" and then customize it.
 *
 * <p>Make sure you fix the site-specific RegistryCustomizerClass property.
 * For instance, if you copy this to org/myhospital/OurFirstClinic.java
 * for your clinic at siteId 1, set your site-1 specific property for
 * property "RegistryCustomizerClass" to "org.myhospital.OurFirstClinic".
 */
public class RegistryCustomizerDefault implements RegistryCustomizer {

  private static final Logger logger = LoggerFactory.getLogger(RegistryCustomizerDefault.class);
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
  protected final SiteInfo siteInfo;

  public RegistryCustomizerDefault(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  @Override
  public ClientConfig getClientConfig() {
    ClientConfig clientConfig = new ClientConfig();
    clientConfig.init(siteInfo.getSiteId(), siteInfo.getClientParams());
    return clientConfig;
  }

  @Override
  public ArrayList<PatientRegistration> determinePatientRegActions(
      Database database, ArrayList<PatientRegistration> registrations) {
    MenuDefBeanFactory factory = AutoBeanFactorySource.create(MenuDefBeanFactory.class);
    MenuDefIntfUtils menuDefUtils = new MenuDefIntfUtils();
    for (PatientRegistration registration : registrations ) {
      if (registration.hasDeclined()) {
        registration.setAction(menuDefUtils.getActionNothingDeclined(factory));
      } else if (!registration.hasConsented()) {
        registration.setAction(menuDefUtils.getActionEnroll(factory));
      } else if (!registration.getIsDone()) {
        if (!registration.getSurveyRequired()) {
          registration.setAction(menuDefUtils.getActionNothingRecentlyCompleted(factory));
        } else if (registration.getNumberCompleted() > 0) {
          registration.setAction(menuDefUtils.getActionInProgress(factory));
        } else {
          registration.setAction(menuDefUtils.getActionAssessment(factory));
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
    Date surveyWithinDate = DateUtils.getDaysFromDate(siteInfo, registration.getSurveyDt(), -lastSurveyDaysOut);

    AssessDao assessDao = new AssessDao(database, siteInfo);
    // Check if a notification already exists for the patient between surveyWithinDate
    // and throughDate for a different registration
    int notifications = assessDao.getNotificationCount(patientId, surveyWithinDate, throughDate, registration.getApptId());
    ArrayList<String> excludeTypes = XMLFileUtils.getInstance(siteInfo).getExcludeFromSurveyCntVisits();
    if (excludeTypes.contains(registration.getVisitType())) {
      logger.debug("Sending notification for patient {}, ApptRegId {}, ApptType {} even though other notified surveys" +
              "were found between {} and {}", patientId, apptId, registration.getVisitType(),
          siteInfo.getDateFormatter().getDateString(surveyWithinDate),
          siteInfo.getDateFormatter().getDateString(throughDate));
      notifications = 0;
    }
    if (notifications > 0) {
      logger.debug("Other notified surveys found for patient " + patientId + ", ApptRegId " + apptId +
          " between " + dateFormat.format(surveyWithinDate) + " and " + dateFormat.format(throughDate));
      return false;
    }
    // Check if the user has already completed a survey for between surveyWithinDate
    // and throughDate for a different registration
    int completedSurveys = assessDao.getCompletedSurveyCount(patientId, surveyWithinDate, throughDate,
        apptId);
    if (completedSurveys > 0) {
      logger.debug("Other completed surveys found for patient " + patientId + ", ApptRegId " + apptId +
          " between " + dateFormat.format(surveyWithinDate) + " and " + dateFormat.format(throughDate));
      return false;
    }
    return true;
  }

  @Override
  public void scheduleSurveys(Database database, Date throughDate, Long siteId) {
    // No scheduled stand alone surveys created
  }

  @Override
  public Map<String,String> getEmailSubstitutions(Database database, PatientRegistration registration) {
    Map<String,String> substitutions = new HashMap<>();
    return substitutions;
  }

  @Override
  public List<File> getEmailAttachments(String template) {
    return null;
  }

  @Override
  public ScoresExportReport getScoresExportReport(Database database, Map<String,String[]> params, SiteInfo siteInfo) {
    return new ScoresExportReport(database, siteInfo);
  }

  @Override
  public Map<String, Class<? extends CustomRestletHandler>> getRestletActions() {
    Map<String,Class<? extends CustomRestletHandler>> actions =
        new HashMap<>();
    return actions;
  }

  @Override
  public PatientReport getPatientReport(Database database, SiteInfo siteInfo) {
    return new PatientReport(database, siteInfo, ServerUtils.getAdminUser(database));
  }

  @Override
  public ChartConfigurationOptions getConfigurationOptions() {
    return new ChartConfigurationOptions(new ConfigurationOptions());
  }

  @Override
  public ReportGenerator getCustomReportGenerator(String reportType) {
    return null;
  }

  @Override
  public ApiReportGenerator getCustomApiReportGenerator(String reportType) { return null; }

  @Override
  public ArrayList<PatientRegistration> getPatientRegistrations(AssessDao assessDao, Patient pat, Date date) {
    return assessDao.getPatientRegistrations(pat.getPatientId(), date);
  }

  @Override
  public String getPatientSearchType(String searchString) {
    String patientIdPattern = siteInfo.getProperty("PatientIdFormat","\\d{5,7}-\\d{1}|\\d{5,9}");

    // TODO: check REGEX_EMAIL
    if (searchString.matches(CommonUtils.REGEX_EMAIL)) {
      return PATIENT_SEARCH_BY_EMAIL;
    }
    if (searchString.matches(patientIdPattern)) {
      return PATIENT_SEARCH_BY_PATIENT_ID;
    }
    // default to searching by name
    return PATIENT_SEARCH_BY_PARTIAL_NAME;
  }

  @Override
  public String getPatientSearchValue(String searchType, String searchString) {
    // If doing a patient id search then format the search string as a patient id
    if (searchType.equals(PATIENT_SEARCH_BY_PATIENT_ID)) {
      String patientId = siteInfo.getPatientIdFormatter().format(searchString);
      if (!siteInfo.getPatientIdFormatter().isValid(patientId)) {
        throw new IllegalArgumentException(searchString + " is not a valid MRN format.");
      }
      return patientId;
    }
    
    return searchString;
  }

  /**
   * Default behavior is to allow any treatment set defined in the config table to
   * appear in the "Enter Patient Data.menu" in the clinic PatientDetail tab.
   * <br>To disable a treatment set, call part.disable(String reasonToolTip).
   * <br>To remove it from the menu, return null;
   */
  @Override
  public RandomSetParticipant disableUiTreatmentSet(Database db, RandomSetParticipant part) {
    return part;  // return it as-is
  }
  
  @Override
  public Boolean customActionMenuCommand(Database db, ClinicServices clinicServices,
      String action, AssessmentId asmtId, Map<String,String> params) {
    throw new IllegalArgumentException("Custom menu action " + action + " is not supported");
  }

  @Override
  public void handlePatientRegistration(Database db, Patient patient) {
    // No-op
  }
  
  @Override
  public void batchSendEmails(DatabaseProvider dbp, AdministrativeServices adminServices) throws Exception {
    if (doSendEmails()) {
      String url = siteInfo.getGlobalProperty("survey.link");
      adminServices.doSurveyInvitations(siteInfo.getMailer(), url);
    }
  }

  protected Date lastRun = null;

  /**
   * Control the batch send email processing so that it is only done
   * once a day between 7am and 8am.
   */
  protected synchronized boolean doSendEmails() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    
    cal.set(Calendar.HOUR_OF_DAY, 0);
    Date midnight = cal.getTime();
    cal.set(Calendar.HOUR_OF_DAY, 7);
    Date today7am = cal.getTime();
    cal.set(Calendar.HOUR_OF_DAY, 8);
    Date today8am = cal.getTime();

    Date now = new Date();

    if (lastRun == null) {
      // On server start up we don't know if it has already been run today
      // so only run it after 7am but before 8am
      if (now.after(today7am) && now.before(today8am)) {
        lastRun = now;
        return true;
      }
    } else {
      // Otherwise run it after 7am if the last run was yesterday
      if (now.after(today7am) && lastRun.before(midnight)) {
        lastRun = now;
        return true;
      }
    }
    return false;
  }

  @Override
  public ArrayList<String> apiExportTables() {
    ArrayList<String> tables = new ArrayList<>();
    tables.add("rpt_pain_std_surveys_square");
    return tables;
  }

  @Override
  public List<String> getApiCustomPaths() {
    return null;
  }
  
  @Override
  public ApiCustomHandler getApiCustomHandler(Supplier<Database> dbp, SiteInfo siteInfo,
      User user, ClinicServices clinicServices) {
    return null;
  }

  @Override
  public String IRBCountsConsentAttribute() {
    return "";
  }

  @Override
  public Hl7CustomizerIntf getHl7Customizer() {
    return new Hl7Customizer(siteInfo);
  }
}
