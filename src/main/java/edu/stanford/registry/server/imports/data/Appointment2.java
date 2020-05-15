/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.imports.data;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.imports.ImportDefinition;
import edu.stanford.registry.server.imports.ImportDefinitionQueue;
import edu.stanford.registry.server.imports.ImportResources;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.xchg.FormatterIntf;
import edu.stanford.registry.server.xchg.ImportException;
import edu.stanford.registry.server.xchg.QualifierIntf;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.Provider;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

/**
 * Appointment importer V2.
 *
 * This version of the appointment importer uses the appointment encounter id
 * as a unique key to match the appointment with the survey registration and
 * uses the appointment status to update the survey registration type.
 *
 * @author dtom2
 */
public class Appointment2 implements ImportDataSourceManagerIntf {
  private static final Logger logger = LoggerFactory.getLogger(Appointment2.class);

  private static final String[] DEPENDS = { "PatientProfile" };

  private static final int PATIENT = 0;
  private static final int APPTDT = 1;
  private static final int APPTTM = 2;
  private static final int VISITTP = 3;
  private static final int APPTST = 4;
  private static final int PROVID = 5;
  private static final int ENCID = 6;
  private static final int DEPT = 7;
  private static final String[] FIELDS = { "PatientId", "ApptDt", "ApptTime", "ApptType", "ApptStatus", "ProviderId", "EncounterId", "Department" };

  // Appointment Status values
  protected static final int APPT_STATUS_SCHEDULED = 1;
  protected static final int APPT_STATUS_COMPLETED = 2;
  protected static final int APPT_STATUS_CANCELED = 3;
  protected static final int APPT_STATUS_NO_SHOW = 4;
  protected static final int APPT_STATUS_LEFT_NOT_SEEN = 5;
  protected static final int APPT_STATUS_ARRIVED = 6;

  private SimpleDateFormat apptDateFormat;
  private SimpleDateFormat apptTimeFormat;
  private SimpleDateFormat apptDateTimeFormat;

  protected Database database;
  protected PatientDao patientDao;
  protected ActivityDao activityDao;
  protected UserDao userDao;
  protected Long siteId;
  protected SiteInfo siteInfo;

  private ImportResources resources;
  private ImportDefinitionQueue queue;
  private ImportDefinitionQueue profileQueue = null;

  private int[] COLUMNS = new int[FIELDS.length];
  private int minimumRowLength;  // max(COLUMNS)
  private FormatterIntf<?>[] FORMATTERS = new FormatterIntf[FIELDS.length];
  private QualifierIntf<?>[] QUALIFIERS = new QualifierIntf[FIELDS.length];
  private boolean initialized = false;

  public Appointment2() {  // A public default constructor is required- this an XchgUtil datasource class
    for (int c=0; c<FIELDS.length; c++) {
      COLUMNS[c] = -1;
    }
  }

  @Override
  public void setDatabase(Database database, SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
    this.database = database;
    
    apptDateFormat = DateUtils.newDateFormat(siteInfo, "MM/dd/yyyy");
    apptTimeFormat = DateUtils.newDateFormat(siteInfo, "HH:mm");
    apptDateTimeFormat = DateUtils.newDateFormat(siteInfo, "MM/dd/yyyy HH:mm");
  }

  @Override
  public void setQueue(ImportDefinitionQueue queue) {
    this.queue = queue;
  }

  @Override
  public void setResources(ImportResources res) {
    this.resources = res;
  }

  @Override
  public String getDataSource() {
    return "Appointment";
  }

  @Override
  public String[] getDataSourceDependancies() {
    return DEPENDS;
  }

  @Override
  public void addDependancyQueue(ImportDefinitionQueue queue) {
    this.profileQueue = queue;
  }

  protected void init() throws Exception {
    if (database == null) {
      throw new Exception(" no database ");
    }
    if (resources == null) {
      throw new Exception(" no definitions");
    }
    if ((queue == null) || (queue.getDefinitions() == null)) {
      throw new Exception(" queue definitions are missing");
    }
    if ((profileQueue == null) || (profileQueue.getDefinitions() == null)){
      throw new Exception(" profile definitions missing");
    }

    // Get the column index for each field
    setFieldColumns(queue.getDefinitions());
    setFieldColumns(profileQueue.getDefinitions());

    // Check column exists for all required fields
    for (int c = 0; c < COLUMNS.length; c++) {
      if (COLUMNS[c] < 0) {
        if ((c != PROVID) && (c != DEPT)) {
          throw new Exception(FIELDS[c] + " column is missing from import file ");
        }
      }
    }
    User adminUser = ServerUtils.getAdminUser(database);
    patientDao = new PatientDao(database, siteId, adminUser);
    activityDao = new ActivityDao(database, siteId);
    userDao = new UserDao(database, adminUser, adminUser);
    initialized = true;
  }

  protected void setFieldColumns(List<ImportDefinition> defs) throws Exception {
    for (ImportDefinition field : defs) {
      if (field == null) {
        throw new Exception(" field definition in queue is null");
      }
      for (int f = 0; f < FIELDS.length; f++) {
        if (FIELDS[f].equals(field.getField())) {
          COLUMNS[f] = field.getColumn();
          if (minimumRowLength <= field.getColumn()) {
            minimumRowLength = field.getColumn() + 1;
          }
          FORMATTERS[f] = field.getFormatter();
          QUALIFIERS[f] = field.getQualifier();
        }
      }
    }
  }

  private void sayFormat() {
    try {
      String[] a = new String[minimumRowLength];
      for (int c=0; c < COLUMNS.length; c++) {
        if (COLUMNS[c] > -1) {
          a[COLUMNS[c]] = FIELDS[c];
        }
      }
      StringBuilder b = new StringBuilder(100);
      String delim = "";
      for (String s: a) {
        b.append(delim).append(s == null ? "" : s);
        delim = delim.isEmpty() ? ", " : delim;
      }
      logger.info("Row format is: {}", b.toString());
    } catch (Throwable t) {
      logger.error("failure trying to say the format...", t);
    }
  }
  
  @Override
  public boolean importData(String[] fields) throws Exception {
    if (!initialized) {
      init();
    }
    if (fields.length < minimumRowLength) {
      sayFormat();
      throw new ImportException(siteInfo.getIdString()+"spreadsheet rows length ("+fields.length+") is too short, need "+minimumRowLength+" columns");
    }

    // Check the qualifiers
    for (int c=0; c < COLUMNS.length; c++) {
      if (QUALIFIERS[c] != null && !QUALIFIERS[c].qualifies(fields[COLUMNS[c]])) {
        throw new ImportException(QUALIFIERS[c].getClass().getName() + " failed to qualify (column " + c +"):" + fields[COLUMNS[c]]);
      }
    }

    // Get Patient Id
    String patientId;
    try {
      if (FORMATTERS[PATIENT] != null) {
        patientId = FORMATTERS[PATIENT].format(fields[COLUMNS[PATIENT]]).toString();
      } else {
        patientId = resources.validMrn(fields[COLUMNS[PATIENT]].toLowerCase());
      }
    } catch(Exception e) {
      throw new ImportException("Invalid data for patient id: " + fields[COLUMNS[PATIENT]]);
    }

    // Get Appointment date/time
    Date apptDate;
    try {
      Date date;
      if (FORMATTERS[APPTDT] != null) {
        date = FORMATTERS[APPTDT].toDate(fields[COLUMNS[APPTDT]]);
      } else {
        date = apptDateFormat.parse(fields[COLUMNS[APPTDT]]);
      }

      Date time;
      if (FORMATTERS[APPTTM] != null) {
        time = FORMATTERS[APPTTM].toDate(fields[COLUMNS[APPTTM]]);
      } else {
        time = apptTimeFormat.parse(fields[COLUMNS[APPTTM]]);
      }

      String dateString = apptDateFormat.format(date);
      String timeString = apptTimeFormat.format(time);
      apptDate = apptDateTimeFormat.parse(dateString + " " + timeString);
    } catch (Exception e) {
      throw new ImportException("Invalid data for appointment date/time: " + fields[COLUMNS[APPTDT]] + " " + fields[COLUMNS[APPTTM]]);
    }

    // Get Visit type
    String visitType;
    try {
      if (FORMATTERS[VISITTP] != null) {
        visitType = FORMATTERS[VISITTP].format(fields[COLUMNS[VISITTP]]).toString();
      } else {
        visitType = fields[COLUMNS[VISITTP]];
      }
    } catch(Exception e) {
      throw new ImportException("Invalid data for visit type: " + fields[COLUMNS[VISITTP]]);
    }

    // Get Appointment status
    Integer apptStatus;
    try {
      if (FORMATTERS[APPTST] != null) {
        apptStatus = Integer.valueOf(FORMATTERS[APPTST].format(fields[COLUMNS[APPTST]]).toString());
      } else {
        apptStatus = Integer.valueOf(fields[COLUMNS[APPTST]]);
      }
    } catch(Exception e) {
      throw new ImportException("Invalid data for appointment status: " + fields[COLUMNS[APPTST]]);
    }

    // Get Encounter Eid
    String encounterEid;
    try {
      if (FORMATTERS[ENCID] != null) {
        encounterEid = FORMATTERS[ENCID].format(fields[COLUMNS[ENCID]]).toString();
      } else {
        encounterEid = fields[COLUMNS[ENCID]];
      }
    } catch (Exception e) {
      throw new ImportException("Invalid data for encounter id: " + fields[COLUMNS[ENCID]]);
    }

    // Get Provider Eid
    String providerEid;
    try {
      if (COLUMNS[PROVID] > -1) { // This column is not required
        if (FORMATTERS[PROVID] != null) {
          providerEid = FORMATTERS[PROVID].format(fields[COLUMNS[PROVID]]).toString();
        } else {
          providerEid = fields[COLUMNS[PROVID]];
        }
      } else {
        providerEid = null;
      }
    } catch (Exception e) {
      throw new ImportException("Invalid data for provider id: " + fields[COLUMNS[PROVID]]);
    }

    // Get Department
    String department;
    try {
      if (COLUMNS[DEPT] > -1) { // This column is not required
        if (FORMATTERS[DEPT] != null) {
          department = FORMATTERS[DEPT].format(fields[COLUMNS[DEPT]]).toString();
        } else {
          department = fields[COLUMNS[DEPT]];
        }
      } else {
        department = null;
      }
    } catch (Exception e) {
      throw new ImportException("Invalid data for department: " + fields[COLUMNS[DEPT]]);
    }

    // Process the appointment
    processAppointment(patientId, apptDate, visitType, apptStatus, encounterEid, providerEid, department);

    return false;
  }

  @Override
  public void importDataEnd() throws Exception {
    // No processing needed on end of import data
  }

  protected void processAppointment(String patientId, Date apptDate, String visitType,
      Integer apptStatus, String encounterEid, String providerEid, String department) throws ImportException {

    logger.debug("Processing appointment encounterEid {} with status {} in clinic {} for {} on {}", encounterEid,
        apptStatus, department, patientId, apptDateFormat.format(apptDate));

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
        logger.warn("Site# {}: Ignoring appointment, bad status: {}", siteId, apptStatus);
        // Ignore appointment
        return;
    }

    // Ignore certain visit types
    if (skipType(visitType, department)) {
      return;
    }

    // Get the visit type based on the visit type and department
    visitType = getVisitType(visitType, department);
    
    if ((encounterEid == null) || encounterEid.equals("")) {
      throw new ImportException("Missing value for encounter id");
    }

    // Look up the patient from the Id
    Patient patient = patientDao.getPatient(patientId);
    if (patient == null) {
      throw new ImportException("Patient not found for mrn " + patientId);
    }

    // Look up the provider from the provider Eid
    Provider provider = getProvider(providerEid);
    Long providerId = (provider == null) ? null : provider.getProviderId();

    // Find the matching survey registration
    ApptRegistration reg = getSurveyRegistration(encounterEid, patientId, apptDate, visitType);
    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);

    if (reg == null) {
      // Not found, add new survey registration for active appointments
      if (regType.equals(Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT)) {
        // Determine the survey type for this appointment date
        String surveyType = getSurveyType(patient, visitType, apptDate, providerEid, null);

        reg = new ApptRegistration(siteId, patientId, apptDate, null, surveyType, regType, visitType);
        reg.setApptComplete(apptComplete);
        reg.setClinic(department);
        reg.setProviderId(providerId);
        reg.setEncounterEid(encounterEid);

        reg = surveyRegUtils.createRegistration(new AssessDao(database, siteInfo), reg);

        logger.debug("Added new survey registration for encounterEid {}", encounterEid);
        logger.debug("  PatientId={} ; SurveyDt={}; SurveyType={}; VisitType={}; ApptComplete={}",
            patientId, apptDateTimeFormat.format(apptDate), surveyType, visitType, apptComplete);
      }
    } else {
      // Found, update existing survey registration fields
      String change = "";

      if (!areSame(reg.getRegistrationType(), regType)) {
        reg.setRegistrationType(regType);
        change += "RegistrationType=" + regType + ";";
      }

      if (!reg.getSurveyDt().equals(apptDate)) {
        reg.setSurveyDt(apptDate);
        List<SurveyRegistration> surveyRegs = reg.getSurveyRegList();
        for(SurveyRegistration surveyReg : surveyRegs) {
          surveyReg.setSurveyDt(apptDate);
        }
        change += "SurveyDt=" + apptDateTimeFormat.format(apptDate) + ";";
      }

      if (!areSame(reg.getVisitType(), visitType)) {
        reg.setVisitType(visitType);
        change += "VisitType=" + visitType + ";";
      }

      if (apptComplete != null) {
        // Only update registration value if we have a apptComplete value
        if (!areSame(reg.getApptComplete(), apptComplete)) {
          reg.setApptComplete(apptComplete);
          change += "ApptComplete=" + apptComplete + ";";
        }
      }

      if ((reg.getProviderId() == null) ? (providerId != null) : !reg.getProviderId().equals(providerId)) {
        reg.setProviderId(providerId);
        change += "ProviderId=" + providerId + ";";
      }

      if (!areSame(reg.getEncounterEid(), encounterEid)) {
        reg.setEncounterEid(encounterEid);
        change += "EncounterEid=" + encounterEid + ";";
      }

      if (!areSame(reg.getClinic(), department)) {
        reg.setClinic(department);
        change += "Clinic=" + department + ";";
      }
      // If there were changes then update the database
      if (!change.equals("")) {
        surveyRegUtils.updateRegistration(database, reg, apptDate);
        logger.debug("Modified survey registration for encounterEid {} {}", encounterEid, change);
      }

      // Determine the new survey type for this appointment date
      String surveyType = getSurveyType(patient, visitType, apptDate, providerEid, reg.getSurveyType());

      // If the survey type has changed then update the survey type
      if (!areSame(reg.getSurveyType(), surveyType) && canUpdateSurveyType(reg)) {
        surveyRegUtils.changeSurveyType(database, reg.getAssessment(), surveyType, ServerUtils.getAdminUser(database));
        logger.debug("Modified survey registration for encounterEid {} SurveyType={}", reg.getEncounterEid(), surveyType);
      }
    }

    // If there is a registration then do any additional processing
    if (reg != null) {
      processRegistration(patient, reg);
    }
  }

  protected void processRegistration(Patient patient, ApptRegistration reg) {
    // Override this method to implement custom handling of the registration
  }

  protected String getSurveyType(Patient patient, String visitType, Date apptDate, String providerEid, String curSurveyType) {
    return getSurveyType(patient.getPatientId(), visitType, apptDate, curSurveyType);
  }

  protected String getSurveyType(String patientId, String visitType, Date apptDate, String curSurveyType) {
    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
    AssessDao assessDao = new AssessDao(database, siteInfo);
    ApptRegistration lastCompleted = assessDao.getLastCompletedRegistration(patientId, apptDate);
    int visits = lastCompleted == null ? 0 : 1;
    String surveyType = surveyRegUtils.getVisitType(visits, apptDate);
    return surveyType;
  }

  protected Provider getProvider(String providerEid) {
    if (providerEid == null || providerEid.trim().length() < 1) {
      return null;
    }
    Provider provider = userDao.getProviderByEid(providerEid);
    if (provider != null && provider.getProviderId() != null) {
      return provider;
    }
    provider = new Provider();
    provider.setProviderEid(providerEid);
    userDao.writeProvider(provider);
    return provider;
  }

  protected boolean skipType(String visitType, String department) {
    if (visitType != null) {
      // do not import CONF (conference about patient) appointments
      return visitType.toUpperCase().equals("CONF") || visitType.toUpperCase().equals("TEAM CONF");
    }
    return false;
  }

  protected String getVisitType(String visitType, String department) {
    return visitType;
  }

  protected ApptRegistration getSurveyRegistration(String encounterEid, String patientId,
      Date apptDate, String visitType) {
    AssessDao assessDao = new AssessDao(database, siteInfo);

    // Find the survey registration by the encounter id
    ApptRegistration reg = assessDao.getApptRegistrationByEncounterId(encounterEid);
    if (reg != null) return reg;
    // Look for a survey registration with the same appointment date which may
    // have been entered manually
    List<ApptRegistration> regs = assessDao.getApptRegistrationByPatientAndDate(patientId, apptDate);
    for(ApptRegistration apptReg : regs) {
      // Manually entered appointment do not have an encounter id
      if (apptReg.getEncounterEid() == null) {
        if (areSame(apptReg.getVisitType(), visitType)) {
          // If visit type matches then return it
          return apptReg;
        } else {
          // Visit type is different but still a possible match
          reg = apptReg;
        }
      }
    }
    return reg;
  }

  protected boolean canUpdateSurveyType(ApptRegistration reg) {
    AssessmentRegistration assessment = reg.getAssessment();

    // Don't change the survey types for prior appointments
    if (reg.getSurveyDt().before(DateUtils.getDateStart(siteInfo, new Date()))) {
      return false;
    }

    // Don't change the survey type if the survey type was manually assigned
    // (i.e. changed by a user other than admin)

    ArrayList<Activity> typeChangedActivities = 
        activityDao.getActivityByAssessmentId(assessment.getAssessmentId(), Constants.ACTIVITY_SURVEY_TYPE_CHANGED);
    if (typeChangedActivities != null) {
      for(Activity activity : typeChangedActivities) {
        if (activity.getUserPrincipalId() != null) {
          if (!activity.getUserPrincipalId().equals(ServerUtils.getAdminUser(database).getUserPrincipalId())) {
            return false;
          }
        }
      }
    }

    AssessDao assessDao = new AssessDao(database, siteInfo);
    PatientRegistration patientRegistration = assessDao.getPatientRegistrationByRegId(reg.getApptId());
    if (patientRegistration.getNumberCompleted() > 0) {
      // Can not update because the survey has been started
      logger.debug("Can not modified survey registration for encounterEid {} because survey has been started", reg.getEncounterEid());
      return false;
    }
    
    return true;
  }

  protected boolean areSame(String str1, String str2) {
    if (str1 == null) {
      return (str2 == null) || str2.equals("");
    } else if (str2 == null) {
      return (str1 == null) || str1.equals("");
    } else {
      return str1.equals(str2);
    }
  }
}
