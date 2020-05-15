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

package edu.stanford.registry.server.service;

import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.config.tools.AppPropertyList;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.imports.ImportDataProcessor;
import edu.stanford.registry.server.reports.ScoresExportReport;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.server.utils.XchgUtils;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.ConfigParam;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataTable;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.Notification;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.PatientRegistrationSearch;
import edu.stanford.registry.shared.ProcessInfo;
import edu.stanford.registry.shared.RegConfigProperty;
import edu.stanford.registry.shared.RegConfigUsage;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

import static edu.stanford.registry.server.xchg.data.Constants.IMPORT_FILES_PENDING;
import static edu.stanford.registry.server.xchg.data.Constants.IMPORT_FILES_PROCESSED;

public class AdministrativeServicesImpl extends ClinicServicesImpl implements AdministrativeServices {

  private static final Logger logger = Logger.getLogger(AdministrativeServicesImpl.class);

  private final ServerContext serverContext;

  public AdministrativeServicesImpl(User usr, Supplier<Database> databaseProvider, ServerContext context, SiteInfo siteInfo) {
    super(usr, databaseProvider, context, siteInfo);
    serverContext = context;
  }

  @Override
  public boolean reloadXml() {
    xmlUtils.reload(siteInfo);
    return true;
  }

  @Override
  public ArrayList<DataTable> getTableData(final String className) throws InvalidDataElementException {
    if ("Study".equals(className)) {
      SurveySystDao ssDao = new SurveySystDao(dbp.get());
      return ssDao.getStudiesAsDataTables();
    }

    if ("SurveySystem".equals(className)) {
      SurveySystDao ssDao = new SurveySystDao(dbp.get());
      return ssDao.getSurveySystemsAsDataTables();
    }

    return null;
  }

  @Override
  public int loadCsv(File importDefinitionXlxs, File csvFile) throws IOException {
    ImportDataProcessor importer = new ImportDataProcessor(siteInfo);
    importer.doImport(dbp.get(), importDefinitionXlxs, csvFile);

    return 1;

  }

  /**
   * Imports all data files in the pending directory
   */
  @Override
  public int loadPendingImports(String definitionName) throws IOException {
    int count = 0;

    // allow multiple import processes across sites by appending the sites name to the process definition
    ProcessInfo thisProcess = new ProcessInfo(definitionName + siteInfo.getUrlParam(), user.getUsername());
    boolean added = serverContext.addProcess(thisProcess);
    if (!added) {
      throw new IOException("Already running");
    }

    try {
      ImportDataProcessor importer = new ImportDataProcessor(siteInfo);

      XchgUtils xchgUtils = new XchgUtils(siteInfo);
      File processDir = xchgUtils.getImportFilesProcessedDirectory(definitionName);
      if (processDir == null) {
        throw new IOException(siteInfo.getIdString() + " site property not set: "+IMPORT_FILES_PROCESSED);
      }

      File loadFileDir = xchgUtils.getImportFilesPendingDirectory(definitionName);
      if (loadFileDir == null) {
        throw new IOException(siteInfo.getIdString() + " site property not set: "+IMPORT_FILES_PENDING);
      }

      // process pending files
      File[] pendingFiles = loadFileDir.listFiles();
      logger.debug(pendingFiles.length + " " + definitionName + " files found in pending directory " + definitionName);
      for (File pendingFile : pendingFiles) {
        File processFile = new File(processDir + File.separator + pendingFile.getName());
        logger.debug("moving " + pendingFile.getName() + " to " + processFile.getAbsolutePath());
        boolean moved = pendingFile.renameTo(processFile);
        if (!moved) {
          throw new IOException("Could not move " + pendingFile.getName() + " to " + processDir);
        }
        importer.doImport(dbp.get(), xchgUtils.getImportTypeDefinitionFile(definitionName), processFile);
        count++;
      }

      // automate sending out emails for a site when an Appointment load completes
      if ("Appointment".equals(definitionName)) {
        String serverUrl = siteInfo.getProperty("survey.link");
        doSurveyInvitations(siteInfo.getMailer(), serverUrl);
      }
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception e) {
      throw new IOException(e);
    } finally {
      serverContext.removeProcess(thisProcess);
    }

    return count;

  }

  @Override
  public ArrayList<ProcessInfo> getRunningProcesses() {
    return serverContext.getProcesses();
  }

  @Override
  public void clearProcesses() {
    serverContext.clearProcesses();
  }

  @Override
  public ArrayList<String> getFileImportDefinitions() throws IOException {
    /**
     * Gets a list of all the files in the import types directory that end in
     * "xlsx"
     */
    try {
      return new XchgUtils(siteInfo).getImportTypeDefinitionDirectoryFiles(this.getClass());
    } catch (Exception e) {
      logger.error("Error getting import definition files " + e.toString(), e);
      throw new IOException(e);
    }
  }

  @Override
  public int doSurveyInvitations(Mailer mailer, String serverUrl) throws ServiceUnavailableException {
    // Set up the email monitor for sending emails
    if (mailer == null) {
      throw new ServiceUnavailableException("No mailer!");
    }

    // Get the configuration parameters
    SiteInfo siteInfo = serverContext.getSiteInfo(siteId);
    EmailMonitor monitor = new EmailMonitor(mailer, dbp, serverUrl, siteInfo);

    if (siteInfo == null) {
      throw new ServiceUnavailableException("siteInfo == null, but siteId = "+siteId);
    } else if (!siteInfo.getSiteId().equals(siteId)) {
      throw new ServiceUnavailableException(siteInfo.getIdString()+" is siteInfo, but siteId = "+siteId);
    }

    int initialDaysOutInt = getIntPropWithErrors(siteInfo, "appointment.initialemail.daysout", -1);
    if (initialDaysOutInt < 0) {
      throw new ServiceUnavailableException("Missing or invalid 'appointment.initialemail.daysout' parameters");
    }

    int lastSurveyDaysOutInt = getIntPropWithErrors(siteInfo, "appointment.lastsurvey.daysout", -1);
    int noEmailWithinDays = getIntPropWithErrors(siteInfo, "appointment.noemail.withindays", 2);
    Date[] emailReminderDates = getReminderEmailDaysOut(siteInfo);

    // Calculate some dates based on the config values
    Date daysOutDate = DateUtils.getDaysOutDate(siteInfo, initialDaysOutInt);
    Date throughDate = DateUtils.getDateEnd(siteInfo, daysOutDate);
    Date withinDate = DateUtils.getDaysAgoDate(siteInfo, noEmailWithinDays);

    // Get the process names and templates
    ArrayList<String> processNames = XMLFileUtils.getInstance(siteInfo).getProcessNames();
    Map<String,String> allTemplates = siteInfo.getEmailTemplates();
    HashMap<String,String> initialTemplates = getInitialTemplates(processNames, allTemplates);
    HashMap<String,String> reminderTemplates = getReminderTemplates(processNames, allTemplates);

    // Create upcoming stand alone surveys
    RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
    customizer.scheduleSurveys(dbp.get(), throughDate, siteId);

    // Process upcoming surveys
    ProcessCounts apptCounts = buildOutAppointments(processNames, throughDate, lastSurveyDaysOutInt, withinDate);
    logger.info("Upcoming appointments processed, " + apptCounts.success + " succeeded, " + apptCounts.failed + " failed.");
    // Commit the rows inserted into the notification table
    dbp.get().commitNow();

    // Send initial emails
    Integer sentInitial = monitor.handlePendingNotifications(initialTemplates, throughDate);
    logger.info("Initial email notifications processed, " + sentInitial + " emails sent.");

    // Process upcoming reminders
    ProcessCounts apptReminderCounts = buildOutReminders(emailReminderDates, withinDate);
    logger.info("Upcoming reminders processed, " + apptReminderCounts.success + " succeeded, " + apptReminderCounts.failed + " failed.");
    // Commit the rows inserted into the notification table
    dbp.get().commitNow();

    // Send reminder emails
    Integer sentReminders = monitor.handlePendingNotifications(reminderTemplates, throughDate);
    logger.info("Reminder email notifications processed, " + sentReminders + " emails sent.");
    int sent = sentInitial + sentReminders;
    return (sent);
  }

  private Date[] getReminderEmailDaysOut(SiteInfo siteInfo) {
    String prop = "appointment.reminderemail.daysout";
    String propVal = siteInfo.getProperty(prop);
    if (propVal == null || propVal.trim().length() < 1) {
      logger.error(siteInfo.getIdString()+"Missing value for '"+prop+"' parameter, no notifications will be created.");
      return new Date[0];
    }
    // Change comma-separated values into
    String[] dayStrings = propVal.split(",");
    ArrayList<Date> dates = new ArrayList<Date>(dayStrings.length);
    for (int d = 0; d < dayStrings.length; d++) {
      try {
        int days = Integer.parseInt(dayStrings[d]);
        dates.add(DateUtils.getDaysOutDate(siteInfo, days));
      } catch (Exception e) {
        logger.error(siteInfo.getIdString()+"Invalid integer value " + dayStrings[d] + " in '"+prop+"' parameter = "+propVal);
      }
    }
    if (dates.size()==0) {
      logger.error(siteInfo.getIdString()+"No notifications will be created, all values were bad in '"+prop+"' parameter = "+propVal);
    }
    return dates.toArray(new Date[dates.size()]);
  }

  /**
   * Gets an integer property, handling any errors.
   * @param dflt If -1, it's required, so it's an error if the prop is missing
   */
  private int getIntPropWithErrors(SiteInfo siteInfo, String prop, int dflt) {
    String propStringValue = siteInfo.getProperty(prop);
    if (propStringValue == null || propStringValue.trim().isEmpty()) {
      String msg = String.format("%sMissing value for %s parameter", siteInfo.getIdString(), prop);
      if (dflt < 0) {
        logger.error(msg);
      } else {
        logger.info(msg + ", using default of "+dflt);
      }
      return dflt;
    }

    try {
      return Integer.parseInt(propStringValue);
    } catch (Exception e) {
      logger.error(String.format("%sInvalid value '%s' for the %s parameter%s",
          siteInfo.getIdString(), propStringValue, prop, (dflt < 0 ? "" : ", using default of "+dflt)));
      return dflt;
    }
  }



  /**
   * Find the appointments that are 'appointment.initialemail.daysout' from
   * today. Check that they don't have another appointment in the preceding
   * 'appointment.lastsurvey.daysout' days and if they have an email address and
   * have agreed to participate then create a pending notification (unless one
   * exists) and register them in the assessments (unless already registered).
   * Appointments entered in the application will have both but appointments
   * loaded from an import file will not.
   *
   * @param processNames
   * @param throughDate
   * @return
   */
  private ProcessCounts buildOutAppointments(ArrayList<String> processNames, Date throughDate,
      int lastSurveyDaysOut, Date withinDate) {
    ProcessCounts counts = new ProcessCounts();
    Date tomorrow = DateUtils.getDaysOutDate(siteInfo, 1);

    // Get the registrations for participating patients between tomorrow
    // and the daysout parameter
    ArrayList<PatientRegistration> registrations;
    try {
      // Only consented and un-notified registrations
      PatientRegistrationSearch searchOptions = new PatientRegistrationSearch();
      searchOptions.setOption(PatientRegistrationSearch.CONSENTED);
      searchOptions.setOption(PatientRegistrationSearch.UNOTIFIED);
      searchOptions.excludeType(xmlUtils.getProcesses("notification", "false") );
      String[] registrationTypes = new String[]
          { Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT,
            Constants.REGISTRATION_TYPE_STANDALONE_SURVEY };

      registrations = assessDao.getPatientRegistrations(tomorrow, throughDate,
                                                        Arrays.asList(registrationTypes), searchOptions);
    } catch (Exception e) {
      logger.error("Error getting registrations between "  + tomorrow + " and " + throughDate, e);
      registrations = new ArrayList<>();
      counts.addFailed();
    }

    // Filter out any completed registrations
    ArrayList<PatientRegistration> regs = new ArrayList<>();
    for(PatientRegistration registration : registrations) {
      if (!registration.getIsDone()) {
        regs.add(registration);
      }
    }
    registrations = regs;

    // Sort the list of registrations so that notifications are generated in
    // a predictable order. This affects which notification gets generated first
    // and the check for a pending notification already exists.
    Collections.sort(registrations, new Comparator<PatientRegistration>(){
      @Override
      public int compare(PatientRegistration reg1, PatientRegistration reg2) {
        // Order first by registration type so that 'a'-appointments have
        // preference over 's'-stand alone surveys
        String regType1 = (reg1.getRegistrationType() != null) ? reg1.getRegistrationType() : "z";
        String regType2 = (reg2.getRegistrationType() != null) ? reg2.getRegistrationType() : "z";
        int result = regType1.compareToIgnoreCase(regType2);
        if (result == 0) {
          // Then order by survey date so that earlier survey dates have
          // preference over later dates
          result = reg1.getSurveyDt().compareTo(reg2.getSurveyDt());
        }
        return result;
      }
    });

    logger.debug("Found " + registrations.size() + " registrations for consented patients between " +
        tomorrow + " and " + throughDate);

    for (PatientRegistration registration : registrations) {
      boolean success = true;
      boolean createNotification = true;
      AssessmentRegistration assessment = registration.getAssessment();
      String patientId = assessment.getPatientId();

      try {
        // Check if an email has already been sent for the registration
        if (assessment.getEmailDt() != null) {
          logger.debug("Email already sent for " + patientId + ", registration  " + assessment.getAssessmentId());
          createNotification = false;
        }

        // Check if an unsent email already exists for the registration
        ArrayList<Notification> notifications =
            assessDao.getUnsentNotifications(assessment.getAssessmentId());
        if ((notifications != null) && (notifications.size() > 0)) {
          logger.debug("Pending notification already exists for patient " + patientId + ", registration " + assessment.getAssessmentId());
          createNotification = false;
        }

        // Check if an email should be sent for the registration
        RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
        if ( !customizer.registrationNotifiable(dbp.get(), registration, lastSurveyDaysOut, throughDate) ) {
          logger.debug("Registration not notifiable for patient " + patientId + ", registration " + assessment.getAssessmentId());
          createNotification = false;
        }

        // Check if another email has recently been sent to the patient
        Date lastEmail = assessDao.getLastEmailSentDate(patientId);
        if (lastEmail != null && lastEmail.after(withinDate)) {
          logger.debug("Another email was recently sent to patient " + patientId + " on " + lastEmail);
          createNotification = false;
        }
      } catch (Exception e) {
        logger.error("Exception checking surveys for patient " + patientId + ", registration " + assessment.getAssessmentId(), e);
        success = false;
      }

      // If no errors and need to send email then add the notification
      if (success && createNotification) {
        try {
          logger.debug("Adding notification for patient " + patientId + ", registration " + assessment.getAssessmentId());
          addNotification(assessment);
        } catch (Exception e) {
          logger.error("Exception adding notification for patient " + patientId + ", registration " + assessment.getAssessmentId(), e);
          success = false;
        }
      }

      // If no errors then build out the registration
      if (success) {
        try {
          SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
          surveyRegUtils.registerAssessments(dbp.get(), assessment, user);
        } catch (Exception e) {
          logger.error("Exception building registration for patient " + patientId + ", registration " + assessment.getAssessmentId(), e);
          success = false;
        }
      }

      // Update the counts
      if (success) {
        counts.addSuccess();
      } else {
        counts.addFailed();
      }
    }

    return counts;
  }

  /**
   * Create notifications to send reminder emails to patients with uncompleted
   * surveys due in the number of days listed in the
   * 'appointment.reminderemail.daysout' property who have an email address and
   * have agreed to participate.
   *
   * @return
   */
  private ProcessCounts buildOutReminders(Date[] dates, Date withinDate) {
    ProcessCounts counts = new ProcessCounts();

    for(Date date : dates) {
      if (date != null) {
        // Get the registrations for participating patients on the specified date
        ArrayList<PatientRegistration> registrations;
        try {
          // Only consented
          PatientRegistrationSearch searchOptions = new PatientRegistrationSearch();
          searchOptions.setOption(PatientRegistrationSearch.CONSENTED);
          searchOptions.excludeType( xmlUtils.getProcesses("notification", "false") );
          String[] registrationTypes = new String[]
              { Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT,
                Constants.REGISTRATION_TYPE_STANDALONE_SURVEY };

          registrations = assessDao.getPatientRegistrations(date, date, Arrays.asList(registrationTypes), searchOptions);
        } catch (Exception e) {
          logger.error("Error getting registrations for " + date, e);
          registrations = new ArrayList<>();
        }

        // Filter out any completed registrations
        ArrayList<PatientRegistration> regs = new ArrayList<>();
        for(PatientRegistration registration : registrations) {
          if (!registration.getIsDone()) {
            regs.add(registration);
          }
        }
        registrations = regs;

        logger.debug("Found " + registrations.size() + " registrations for consented patients on " + date);

        for (PatientRegistration registration : registrations) {
          boolean success = true;
          boolean createNotification = true;
          AssessmentRegistration assessment = registration.getAssessment();
          String patientId = assessment.getPatientId();

          try {
            // Check if the initial email was sent
            if (assessment.getEmailDt() == null) {
              createNotification = false;
            }

            // Check if consented
            if (!consented(registration.getPatient(), false)) {
              createNotification = false;
            }

            // Check if another email has recently been sent to the patient
            Date lastEmail = assessDao.getLastEmailSentDate(patientId);
            if (lastEmail != null && lastEmail.after(withinDate)) {
              logger.debug("Another email was recently sent to patient " + patientId + " on " + lastEmail);
              createNotification = false;
            }
          } catch (Exception e) {
            logger.error("Exception checking reminders for patient " + patientId + ", registration " + assessment.getAssessmentId(), e);
            success = false;
          }

          // If no errors and need to send reminder then add the notification
          if (success && createNotification) {
            try {
              logger.debug("Adding reminder notification for patient " + patientId + ", registration " + assessment.getAssessmentId());
              addNotification(assessment);
            } catch (Exception e) {
              logger.error("Exception adding notification for patient " + patientId + ", registration " + assessment.getAssessmentId(), e);
              success = false;
            }
          }

          // Update the counts
          if (success) {
            counts.addSuccess();
          } else {
            counts.addFailed();
          }
        }
      }
    }

    return counts;
  }

  /**
   * Add a notification to the database for this patients registration.
   *
   * @param registration SurveyRegistration.
   * @throws ServiceUnavailableException
   */
  private void addNotification(AssessmentRegistration registration) throws ServiceUnavailableException {
    Notification notify = new Notification(registration.getPatientId(), registration.getAssessmentId(),
        registration.getAssessmentType(), registration.getAssessmentDt(), 0, siteId);
    assessDao.insertNotification(notify);
  }

  private boolean consented(Patient patient, boolean isNew) {

    if (patient != null && patient.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)
        && "y".equals(patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue())
        && !patient.hasAttribute(Constants.ATTRIBUTE_DECLINED_INVITATIONS)) {
      if (isNew) {
        return true;
      } else if (!patient.hasAttribute(Constants.ATTRIBUTE_DECLINED_REMINDERS)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get a map of template name to template data for the appointment and schedule
   * template values for all processes.
   */
  @Override
  public HashMap<String,String> getInitialTemplates(List<String> processNames, Map<String,String> allTemplates) {
    HashMap<String, String> initialTemplates = siteInfo.getInitialTemplates();
    if (initialTemplates.size() > 0) {
      return initialTemplates;
    }

    int i = 0, notFound = 0;
    for (String processName : processNames) {
      String templateName = xmlUtils.getAttribute(processName, XMLFileUtils.ATTRIBUTE_APPOINTMENT_TEMPLATE);
      String templateText = allTemplates.get(templateName);
      if (templateText != null) {
        initialTemplates.put(templateName, templateText);
      } else {
        logger.warn(String.format("No 'appointment template' found for[%s] referenced in process[%s]", templateName, processName));
        notFound++;
      }

      templateName = xmlUtils.getAttribute(processName, XMLFileUtils.ATTRIBUTE_SCHEDULE_TEMPLATE);
      templateText = allTemplates.get(templateName);
      if (templateText != null) {
        initialTemplates.put(templateName, templateText);
      } else {
        logger.warn(String.format("No 'schedule template' found for[%s] referenced in process[%s]", templateName, processName));
        notFound++;
      }
      i+=2;
    }
    if (notFound > 0) {
      logger.debug(String.format("%sgetInitialTemplates: %d/%d were not found", siteInfo, notFound, i));
    }
    return initialTemplates;
  }

  /**
   * Get a map of template name to reminder template data for the appointment and schedule
   * template values for all processes.
   */
  @Override
  public HashMap<String,String> getReminderTemplates(List<String> processNames, Map<String,String> allTemplates) {
    HashMap<String, String> reminderTemplates = siteInfo.getReminderTemplates();
    if (reminderTemplates.size() > 0) {
      return reminderTemplates;
    }

    int i = 0, nf = 0;
    for (String processName : processNames) {
      String templateName = xmlUtils.getAttribute(processName, XMLFileUtils.ATTRIBUTE_APPOINTMENT_TEMPLATE);
      String templateText = allTemplates.get(templateName + "-reminder");
      if (templateText != null) {
        reminderTemplates.put(templateName, templateText);
      } else {
        templateText = allTemplates.get(templateName);
        if (templateText != null) {
          reminderTemplates.put(templateName, templateText);
          logger.debug(siteInfo.getIdString()+"Using template " + templateName + " for " + templateName + "-reminder");
        } else {
          logger.warn(siteInfo.getIdString()+"No template found for " + templateName + "-reminder, referenced in process " + processName);
          nf++;
        }
      }

      templateName = xmlUtils.getAttribute(processName, XMLFileUtils.ATTRIBUTE_SCHEDULE_TEMPLATE);
      templateText = allTemplates.get(templateName + "-reminder");
      if (templateText != null) {
        reminderTemplates.put(templateName, templateText);
      } else {
        templateText = allTemplates.get(templateName);
        if (templateText != null) {
          reminderTemplates.put(templateName, templateText);
          logger.debug(siteInfo.getIdString()+"Using template " + templateName + " for " + templateName + "-reminder");
        } else {
          logger.warn(siteInfo.getIdString()+"No template found for " + templateName + "-reminder, referenced in process " + processName);
          nf++;
        }
      }
      i+=2;
    }
    if (nf > 0) {
      logger.debug(String.format("%sgetReminderTemplates: %d/%d were not found", siteInfo, nf, i));
    }
    return reminderTemplates;
  }

  @Override
  public List<List<Object>> scoresExportData(Map<String,String[]> params) {
    RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
    ScoresExportReport report = customizer.getScoresExportReport(dbp.get(), params, siteInfo);
    List<List<Object>> data = report.getReportData(siteId);
    return data;
  }

  @Override
  public void reloadUsers() {
    serverContext.userInfo().load(dbp);
  }

  @Override
  public void reloadConfig() {
    serverContext.reload(false, true);
  }


  static private class ProcessCounts {
    int success = 0;
    int failed = 0;

    public ProcessCounts() {
    }

    public void addSuccess() {
      success++;
    }

    public void addFailed() {
      failed++;
    }

    /*
    public int getSuccess() {
      return success;
    }

    public int getFailed() {
      return failed;
    } */
  }

  @Override
  public ConfigParam updateConfig(ConfigParam param) throws Exception {
    if (param == null) {
      return null;
    }
    AppConfigDao configDao = new AppConfigDao(dbp.get(), ServerUtils.getAdminUser(dbp.get()));
    if (!param.isEnabled()) {
      configDao.disableAppConfig(param.getConfigId());
      return param;
    }
    String PROCESS_XML="process.xml";
    if (PROCESS_XML.equals(param.getConfigName())) {
      SiteInfo testSite = null;
      try {
        testSite = new SiteInfo(ThreadLocalRandom.current().nextLong(9999999,999999999), siteInfo.getUrlParam(), siteInfo.getDisplayName(), false);
        testSite.initSiteConfig(dbp, serverContext.getSitesInfo(), new HashMap<>(), siteInfo.getProperties(), siteInfo.getEmailTemplates(),
            new HashMap<>());
        testSite.getProperties().put(PROCESS_XML, param.getConfigValue());
        new ProcessXmlValidator(testSite);  // validates the xml
      } catch (Exception ex) {
        throw new Exception("XML contents are not valid. " + getUserXmlMessage(testSite,ex.getMessage()));
      } finally {
        if (testSite != null && testSite.getProperties() != null) {
          if (siteInfo.getProperty(PROCESS_XML) == null) {
            testSite.getProperties().remove(PROCESS_XML);
          } else {
            testSite.getProperties().put(PROCESS_XML, siteInfo.getProperty(PROCESS_XML));
          }
        }
      }
    }
    configDao.addOrEnableAppConfigEntry(siteInfo.getSiteId(), param.getConfigType(), param.getConfigName(), param.getConfigValue());
    if (param.getConfigId() == 0L) {
      AppConfigEntry appConfigEntry =   configDao.findAppConfigEntry(siteInfo.getSiteId(), param.getConfigType(), param.getConfigName());
      if (appConfigEntry != null) {
        param.setConfigId(appConfigEntry.getAppConfigId());
      }
    }
    return param;
  }

  @Override
  public ArrayList<RegConfigProperty> getRegConfigProperties() {
    AppPropertyList regPropList = new AppPropertyList();
    ArrayList<RegConfigProperty> configProperties = new ArrayList<>();
    for (RegConfigProperty rp : regPropList) {
      configProperties.add(rp);
    }
    configProperties.sort((o1, o2) -> {
      if (o1.getCategory() != o2.getCategory()) {
        return o1.getCategory().compareTo(o2.getCategory());
      }
      return o1.getName().compareTo(o2.getName());
    });
    return configProperties;
  }


  @Override
  public ConfigParam getConfig(RegConfigProperty property) {
    // Find the property value cached for the site
    String cachedValue = null;
    String paramType = AppPropertyList.getParamType(property);
    if (!"process.xml".equals(property.getName())) {
      cachedValue = siteInfo.getProperty(property.getName());

      if (cachedValue == null) {
        cachedValue = serverContext.getSitesInfo().getProperty(siteInfo.getSiteId(), property.getName());
        logger.trace("cached value of property " + property.getName() + " found in serverContext.getSitesInfo is " + cachedValue);
      }

      if (cachedValue == null && (
          property.getUsageAbbrev().equalsIgnoreCase(RegConfigUsage.Static.abbrev)
              || property.getUsageAbbrev().equalsIgnoreCase(RegConfigUsage.Global.abbrev)
              || property.getUsageAbbrev().equalsIgnoreCase(RegConfigUsage.StaticRec.abbrev))) {

        cachedValue = serverContext.getSitesInfo().getGlobalProperty(property.getName());
        logger.trace("cached value of property " + property.getName() + " found in gobal properties is " + cachedValue);
      }

      if (cachedValue == null) {
        cachedValue = serverContext.appConfig().forName(siteInfo.getSiteId(), paramType, property.getName());
        logger.trace("cached value of property " + property.getName() + " found in serverContext.appConfig.forname is " + cachedValue);
      }
    }
    logger.trace(" category " + property.getCategory().toString() + ", param type " + paramType + ", cached value " + cachedValue);

    // get the value from the database
    AppConfigDao configDao = new AppConfigDao(dbp.get(), ServerUtils.getAdminUser(dbp.get()));
    AppConfigEntry entry = configDao.findAppConfigEntry(siteInfo.getSiteId(), paramType, property.getName());
    if (entry != null) {
      ConfigParam param = new ConfigParam(entry.getAppConfigId(), entry.getSurveySiteId(), entry.getConfigType(),
          entry.getConfigName(), entry.getConfigValue(), entry.isEnabled());
      param.setCachedValue(cachedValue);
      if (param.getConfigName().toLowerCase().contains("password")) {
        param.setConfigValue( "********************");
      }
      logger.trace("Returning parameter with  value " + param.getConfigValue() +
          " for category " + property.getCategory().toString() + ", param type " + paramType +
          ", with cached value " + cachedValue);
      return param;
    }
    logger.trace("Returning empty parameter for category " + property.getCategory().toString() + ", param type " +
            paramType + ", with cached value " + cachedValue);
    ConfigParam param = new ConfigParam(0L, siteInfo.getSiteId(), property, property.getName(), "", true);
    param.setCachedValue(cachedValue);
    return param;
  }

  private String getUserXmlMessage(SiteInfo testSite, String msg) {
    if (testSite == null || msg == null) {
      return null;
    }
    String xmlPath = testSite.getPathProperty("xml_dir", null);
    String resourcePath = testSite.getPathProperty("xml_resource", Constants.XML_PATH_DEFAULT);
    String fileIdentifierForMsg =  String.format("Problem reading %s has %s/filename: '%s/%s", testSite.getIdString(),
        (xmlPath==null ? "xml_resource" : "xml_dir"), resourcePath, "");
    if (msg.startsWith(fileIdentifierForMsg)) {
      return msg.substring(fileIdentifierForMsg.length());
    }
    return msg;
  }

  private class ProcessXmlValidator extends XMLFileUtils {
    public ProcessXmlValidator(SiteInfo siteInfo) {
      super(siteInfo, "");
    }
  }
}

