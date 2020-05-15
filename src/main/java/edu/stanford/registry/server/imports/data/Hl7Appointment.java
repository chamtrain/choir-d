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
package edu.stanford.registry.server.imports.data;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.ApptVisitDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.xchg.ImportException;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.ApptVisit;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.InvalidPatientIdException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.Provider;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

/**
 * Appointment importer for hl7 messages
 * <p>
 * This version is a copy of Appointment2 adapted to processing data from hl7 messages.
 *
 * @author tpacht@stanford.edu
 * @since 09/2019
 */

public class Hl7Appointment implements Hl7AppointmentIntf {
  private static final Logger logger = LoggerFactory.getLogger(Hl7Appointment.class);

  // Appointment Status values
  public static final int APPT_STATUS_SCHEDULED = 1;
  public static final int APPT_STATUS_COMPLETED = 2;
  public static final int APPT_STATUS_CANCELED = 3;
  public static final int APPT_STATUS_NO_SHOW = 4;
  public static final int APPT_STATUS_LEFT_NOT_SEEN = 5;
  public static final int APPT_STATUS_ARRIVED = 6;

  private final Database database;
  private final SiteInfo siteInfo;
  private final UserDao userDao;
  private final ApptVisitDao visitDao;
  private final AssessDao assessDao;
  private final User adminUser;
  private final PatientDao patientDao;

  private final HashMap<Long, PatientDao> patientDaoMap = new HashMap<>();
  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");

  private final HashMap<Long, SimpleDateFormat> dateFormatters = new HashMap<>();

  public Hl7Appointment(Database database, SiteInfo siteInfo) {
    this.database = database;
    this.siteInfo = siteInfo;
    this.adminUser = ServerUtils.getAdminUser(database);
    userDao = new UserDao(database, adminUser, adminUser);
    visitDao = new ApptVisitDao(database);
    assessDao = new AssessDao(database, siteInfo);
    patientDao = new PatientDao(database, siteInfo.getSiteId(), adminUser);
  }

  @Override
  public Patient processPatient(String patientIdStr, String firstName, String lastName, String dobString) throws ImportException {
    Date dob;
    try {
      dob = dateFormatter.parse(dobString);
    } catch (ParseException pe) {
      logger.error("Invalid dob string {}", dobString, pe);
      throw new ImportException(pe.getMessage());
    }
    Patient patient = processPatient(patientIdStr, firstName, lastName, dob);
    return postPatient(patient);
  }

  @Override
  public Patient postPatient(Patient patient) {
    return patient;
  }

  @Override
  public PatientAttribute processPatientAttribute(Patient patient, String attributeName, String attributeValue) {
    if (patient == null || attributeName == null || attributeValue == null) {
      return null;
    }
    PatientAttribute attribute = patient.getAttribute(attributeName);
    if (attribute != null && attributeValue.equals(attribute.getDataValue())) {
      return postPatientAttribute(attribute);
    }
    if (attribute == null) {
      attribute = new PatientAttribute(patient.getPatientId(), attributeName, attributeValue);
    }
    attribute.setDataValue(attributeValue);
    patientDao.insertAttribute(attribute);
    return postPatientAttribute(attribute);
  }

  @Override
  public PatientAttribute postPatientAttribute(PatientAttribute attribute) {
    return attribute;
  }

  @Override
  public ApptRegistration processAppointment(Patient patient, String apptDateTmStr, String visitEid, String visitDescription,
                                             int apptStatus, String encounterEid, String providerEid, String department) throws ImportException {
    Long siteId = siteInfo.getSiteId();
    SimpleDateFormat apptDateTimeFormat = dateFormatters.get(siteId);

    // Get Appointment date/time
    Date apptDate;
    try {

      if (apptDateTimeFormat == null) {
        apptDateTimeFormat = DateUtils.newDateFormat(siteInfo, "yyyyMMddHHmmss");
        dateFormatters.put(siteId, apptDateTimeFormat);
      }
      apptDate = apptDateTimeFormat.parse(apptDateTmStr);
    } catch (Exception e) {
      throw new ImportException("Invalid data for appointment date/time: " + apptDateTmStr);
    }

    logger.debug("Processing appointment encounterEid {} with status {} for {}  on {}", encounterEid, apptStatus,
        patient.getPatientId(), apptDate);

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
      logger.warn("Site#{}: Ignoring appointment, bad status: {}", siteId, apptStatus);
      // Ignore appointment
      return null;
    }

    // Get the visit type from the hl7 visit eid or description
    String visitType = getVisitType(visitEid, visitDescription);

    // Ignore certain visit types
    if (skipType(visitType, department)) {
      return null;
    }

    if ((encounterEid == null) || encounterEid.equals("")) {
      throw new ImportException("Missing value for encounter id");
    }

    // Look up the provider from the provider Eid
    Provider provider = getProvider(providerEid);
    Long providerId = (provider == null) ? null : provider.getProviderId();

    // Find the matching survey registration
    ApptRegistration reg = getSurveyRegistration(encounterEid, patient.getPatientId(), apptDate, visitType);
    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);

    if (reg == null) {
      // Not found, add new survey registration for active appointments
      if (regType.equals(Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT)) {
        // Determine the survey type for this appointment date
        String surveyType = getSurveyType(patient, visitType, apptDate, providerEid,null);

        reg = new ApptRegistration(siteId, patient.getPatientId(), apptDate, null, surveyType, regType, visitType);
        reg.setApptComplete(apptComplete);
        reg.setClinic(department);
        reg.setProviderId(providerId);
        reg.setEncounterEid(encounterEid);

        reg = surveyRegUtils.createRegistration(assessDao, reg);

        logger.debug("Added new survey registration for encounterEid " + encounterEid);
        logger.debug("  PatientId=" + patient.getPatientId() +
            "; SurveyDt=" + apptDateTimeFormat.format(apptDate) +
            "; SurveyType=" + surveyType +
            "; VisitType=" + visitType +
            "; ApptComplete=" + apptComplete);
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
        for (SurveyRegistration surveyReg : surveyRegs) {
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

      // If there were changes then update the database
      if (!change.equals("")) {
        surveyRegUtils.updateRegistration(database, reg, apptDate);
        logger.debug("Modified survey registration for encounterEid " + encounterEid + " " + change);
      }

      // Determine the new survey type for this appointment date
      String surveyType = getSurveyType(patient, visitType, apptDate, providerEid, reg.getSurveyType());

      // If the survey type has changed then update the survey type
      if (!areSame(reg.getSurveyType(), surveyType) && canUpdateSurveyType(reg)) {
        surveyRegUtils.changeSurveyType(database, reg.getAssessment(), surveyType, ServerUtils.getAdminUser(database));
        logger.debug(
            "Modified survey registration for encounterEid " + reg.getEncounterEid() + " SurveyType=" + surveyType);
      }
    }
    return postAppointment(reg);
  }

  private Patient processPatient(String patientIdStr, String firstName, String lastName, Date dtBirth) throws ImportException {

    // Get the formatted Patient Id
    String patientId;
    try {
      patientId = validMrn(patientIdStr);
    } catch (Exception e) {
      throw new ImportException("Invalid data for patient id: " + patientIdStr);
    }

    // Look up the patient from the Id
    PatientDao patientDao = new PatientDao(database, siteInfo.getSiteId(), ServerUtils.getAdminUser(database));
    Patient patient = patientDao.getPatient(patientId);
    if (patient == null) { // create the patient
      patient = new Patient(patientId, firstName, lastName, dtBirth);
      patientDao.addPatient(patient);
    } else {
      // see if anything changed
      boolean updatePatient = false;
      if (firstName != null && !(firstName.trim()).equals(patient.getFirstName())) {
        patient.setFirstName(firstName.trim());
        updatePatient = true;
      }
      if (lastName != null && !(lastName.trim()).equals(patient.getLastName())) {
        patient.setLastName(lastName.trim());
        updatePatient = true;
      }
      if (dtBirth != null) {
        if (patient.getDtBirth() == null) {
          patient.setDtBirth(dtBirth);
          updatePatient = true;
        } else {
          String newVal = siteInfo.getDateOnlyFormatter().getDateString(dtBirth);
          String oldVal = siteInfo.getDateOnlyFormatter().getDateString(patient.getDtBirth());
          if (!newVal.equals(oldVal)) {
            patient.setDtBirth(dtBirth);
            updatePatient = true;
          }
        }
      }
      if (updatePatient) {
        patientDao.updatePatient(patient);
      }
    }
    return patient;
  }

  @Override
  public ApptRegistration postAppointment(ApptRegistration reg) {
    return reg;
  }

  @Override
  public String getSurveyType(Patient patient, String visitType, Date apptDate, String providerEid, String curSurveyType) {
    if (patient == null) {
      return null;
    }
    return getSurveyType(patient.getPatientId(), visitType, apptDate, curSurveyType);
  }

  @Override
  public String getSurveyType(String patientId, String visitType, Date apptDate, String curSurveyType) {
    return  getSurveyType(patientId, apptDate);
  }

  @Override
  public String getSurveyType(String patientId, Date apptDate) {
    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
    ApptRegistration lastCompleted = assessDao.getLastCompletedRegistration(patientId, apptDate);
    int visits = lastCompleted == null ? 0 : 1;
    return surveyRegUtils.getVisitType(visits, apptDate);
  }

  public Provider getProvider(String providerEid) {
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

  @Override
  public boolean skipType(String visitType, String department) {
    if (visitType != null) {
      // do not import CONF (conference about patient) appointments
      return visitType.toUpperCase().equals("CONF") || visitType.toUpperCase().equals("TEAM CONF");
    }
    return false;
  }

  @Override
  public String getVisitType(String visitEidStr, String visitDescription) {
    try {
      Long visitEid = Long.parseLong(visitEidStr);
      ApptVisit visitType = visitDao.getApptVisitByEid(visitEid);
      if (visitType != null) {
        return visitType.getVisitType();
      }
    } catch (NumberFormatException nfe) {
      logger.error("hl7 message visit id is not a valid number!", visitEidStr);
    }
    ArrayList<ApptVisit> apptVisits = visitDao.getApptVisitByDescription(visitDescription);
    if (apptVisits != null && apptVisits.size() > 0) {
      return apptVisits.get(0).getVisitType();
    }
    return "";
  }

  /**
   * Checks if the patient ID is valid.
   *
   * @param mrn Patient ID.
   * @return Valid Patient ID.
   * @throws InvalidPatientIdException thrown if the patient id is not valid.
   */
  private String validMrn(String mrn) throws InvalidPatientIdException {
    if (mrn == null) {
      throw new InvalidPatientIdException("Missing mrn", true);
    }

    mrn = siteInfo.getPatientIdFormatter().format(mrn);
    if (siteInfo.getPatientIdFormatter().isValid(mrn)) {
      return mrn;
    }
    throw new InvalidPatientIdException(siteInfo.getPatientIdFormatter().getInvalidMessage(), true);
  }

  private ApptRegistration getSurveyRegistration(String encounterEid, String patientId,
                                                 Date apptDate, String visitType) {

    // Find the survey registration by the encounter id
    ApptRegistration reg = assessDao.getApptRegistrationByEncounterId(encounterEid);
    if (reg != null) return reg;
    // Look for a survey registration with the same appointment date which may
    // have been entered manually
    List<ApptRegistration> regs = assessDao.getApptRegistrationByPatientAndDate(patientId, apptDate);
    for (ApptRegistration apptReg : regs) {
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

  public boolean canUpdateSurveyType(ApptRegistration reg) {
    AssessmentRegistration assessment = reg.getAssessment();

    // Don't change the survey types for prior appointments
    if (reg.getSurveyDt().before(DateUtils.getDateStart(siteInfo, new Date()))) {
      return false;
    }

    // Don't change the survey type if the survey type was manually assigned
    // (i.e. changed by a user other than admin)
    ActivityDao activityDao = new ActivityDao(database, siteInfo.getSiteId());
    ArrayList<Activity> typeChangedActivities =
        activityDao.getActivityByAssessmentId(assessment.getAssessmentId(), Constants.ACTIVITY_SURVEY_TYPE_CHANGED);
    if (typeChangedActivities != null) {
      for (Activity activity : typeChangedActivities) {
        if (activity.getUserPrincipalId() != null) {
          if (!activity.getUserPrincipalId().equals(ServerUtils.getAdminUser(database).getUserPrincipalId())) {
            return false;
          }
        }
      }
    }

    PatientRegistration patientRegistration = assessDao.getPatientRegistrationByRegId(reg.getApptId());
    if (patientRegistration.getNumberCompleted() > 0) {
      // Can not update because the survey has been started
      logger.debug("Can not modified survey registration for encounterEid " + reg.getEncounterEid()
          + " because survey has been started");
      return false;
    }

    return true;
  }

  public boolean areSame(String str1, String str2) {
    if (str1 == null) {
      return (str2 == null) || str2.equals("");
    } else if (str2 == null) {
      return str1.equals("");
    } else {
      return str1.equals(str2);
    }
  }
}
