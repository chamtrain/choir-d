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

package edu.stanford.registry.server;

import edu.stanford.registry.client.service.AppointmentStatus;
import edu.stanford.registry.shared.DeclineReason;
import edu.stanford.registry.client.service.PatientIdService;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.formatter.PatientIdFormatIntf;
import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.DisplayProvider;
import edu.stanford.registry.shared.EmailSendStatus;
import edu.stanford.registry.shared.InvalidPatientIdException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientActivity;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.PatientRegistrationSearch;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveyStart;
import edu.stanford.registry.shared.SurveySystem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClinicServiceImpl extends InitRegistryServlet implements
edu.stanford.registry.client.service.ClinicService, PatientIdService {

  private static final long serialVersionUID = -7951396264580399176L;
  private static final Logger logger = LoggerFactory.getLogger(ClinicServiceImpl.class);

  //private final String CLINICAL_DATA_XML = "clinical-data-form.xml";

  // initialize the template hash
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  // Implementation of service methods.
  // assessments

  /**
   * Get an xml document of all assessments for a survey system.
   */
  @Override
  public String getAssessments(String surveySystemName, int version) throws ServiceUnavailableException {
    try {
      return getService().getAssessments(surveySystemName, version);
    } catch (Exception e) {
      logger.error("Error occurred in getAssessments (" + surveySystemName + ")", e);
      throw new ServiceUnavailableException(e.getMessage());
    }
  }

  // XML Forms and templates

  /**
   * Get a list of process names that are currently active, ordered by the
   * process order attribute value
   */
  @Override
  public ArrayList<String> getActiveVisitProcessNames() throws Exception {
    return getService().getActiveVisitProcessNames();
  }

  /**
   * Get a list of process names from the proceess.xml file
   */
  @Override
  public ArrayList<String> getProcessNames() throws Exception {
    return getService().getProcessNames();
  }

  @Override
  /**
   * Get a list of process names from the process.xml file that are surveys
   */
  public ArrayList<String> getSurveyProcessNames() throws Exception {
    return getService().getSurveyProcessNames();
  }

  /**
   * Get a list of the attributes for the process
   *
  public HashMap<String, String> getProcessAttributes(String processType) throws ServiceUnavailableException {
    // Never called...  If needed, move to ClinicServicesImpl which has the xmlUtils
    //return xmlUtils.getAttributes(processType);
    throw new ServiceUnavailableException("getProcessAttributes() was not in an interface");
  }*/

  /**
   * Get a map of all process attributes by process name
   */
  @Override
  public HashMap<String, HashMap<String, String>> getAllProcessAttributes() throws Exception {
    return getService().getAllProcessAttributes();
  }

  /**
   * Get a map of process name and list of patient attributes for the process
   */
  @Override
  public HashMap<String, ArrayList<PatientAttribute>> getAllPatientAttributes() throws Exception {
    return getService().getAllPatientAttributes();
  }

  @Override
  public Patient getPatient(String patientId) throws ServiceUnavailableException, InvalidPatientIdException {
    /** First validate the patientId **/

    PatientIdFormatIntf formatter;
    Patient pat = null;
    boolean isValid = false;
    try {
      RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
      SiteInfo siteInfo = regRequest.getSiteInfo();
      formatter = siteInfo.getPatientIdFormatter();
      patientId = formatter.format(patientId);
      isValid = formatter.isValid(patientId);
    } catch (NumberFormatException e) {
      logger.error("Error in formatting(" + patientId + ")", e);
      throw new InvalidPatientIdException("Invalid number", false);
    }
    try {
      if (isValid) {
        pat = getService().getPatient(patientId);
      }
    } catch (Exception e) {
      logger.error("Error in getPatient(" + patientId + ")", e);
      throw new ServiceUnavailableException(e.getMessage());
    }
    if (pat != null) {
      return pat;
    }
    logger.debug("getPatient: patient not found, formatter.isValid = " + isValid);
    if (isValid) {
      throw new InvalidPatientIdException(patientId + " Not found", patientId);
    }
    throw new InvalidPatientIdException(formatter.getInvalidMessage(), isValid);

  }

  @Override
  public Date getPatientsLastAppointmentDate(String patientId) throws ServiceUnavailableException {
    try {
      return getService().getPatientsLastAppointmentDate(patientId);
    } catch (Exception e) {
      logger.error("Error in getPatientsLastAppointmentDate", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public Date getPatientsLastSurveyDate(String patientId) throws ServiceUnavailableException {
    try {
      return getService().getPatientsLastSurveyDate(patientId);
    } catch (Exception e) {
      logger.error("Error in getPatientsLastSurveyDate", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public Date getPatientsNextAppointmentDate(String patientId) throws ServiceUnavailableException {
    try {
      return getService().getPatientsNextAppointmentDate(patientId);
    } catch (Exception e) {
      logger.error("Error in getPatientsNextAppointmentDate", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public Date getPatientsNextSurveyDueDate(String patientId) throws ServiceUnavailableException {
    try {
      return getService().getPatientsNextSurveyDueDate(patientId);
    } catch (Exception e) {
      logger.error("Error in getPatientsNextSurveyDate", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<Study> getStudies() throws Exception {
    return getStudies(false);
  }

  @Override
  public ArrayList<Study> getStudies(boolean removeDuplicates) throws Exception {
    try {
      return getService().getStudies(removeDuplicates);

    } catch (Exception e) {
      logger.error("Error in getStudies", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<SurveySystem> getSurveySystems() throws Exception {
    try {
      return getService().getSurveySystems();
    } catch (Exception e) {
      logger.error("Error in getSurveySystems", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  /**
   * Get list of all registrations by date range.
   */
  @Override
  public ArrayList<PatientRegistration> searchForPatientRegistration(Date dateFrom, Date dateTo,
      PatientRegistrationSearch searchOptions) {

    try {
      return getService().searchForPatientRegistration(dateFrom, dateTo, searchOptions);

    } catch (Exception e) {
      logger.error("Error in searchForPatientRegistration", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  /**
   * Get list of registrations by date range and type.
   */
  @Override
  public ArrayList<PatientRegistration> searchForPatientRegistration(Date dateFrom, Date dateTo, String type,
      // boolean onlyConsented, boolean showCancelled, Boolean printed
      PatientRegistrationSearch searchOptions) {

    try {
      return getService().searchForPatientRegistration(dateFrom, dateTo, type, searchOptions); // onlyConsented,
      // showCancelled);

    } catch (Exception e) {
      logger.error("Error in searchForPatientRegistration", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  /**
   * Get the list of all registrations for a specified patient.
   */
  @Override
  public ArrayList<PatientRegistration> searchForPatientRegistration(String patientId, PatientRegistrationSearch searchOptions) {

    try {
      return getService().searchForPatientRegistration(patientId, searchOptions);

    } catch (Exception e) {
      logger.error("Error in searchForPatientRegistration", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  /**
   * Get the list of registrations by type for a specified patient.
   */
  @Override
  public ArrayList<PatientRegistration> searchForPatientRegistration(String patientId, String type) {
    try {
      return getService().searchForPatientRegistration(patientId, type);

    } catch (Exception e) {
      logger.error("Error in searchForPatientRegistration", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<Patient> searchForPatientsByPatientId(Long siteId, String patientId) {
    try {
      RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
      SiteInfo siteInfo = regRequest.getSiteInfo();
      String mrn = siteInfo.getPatientIdFormatter().format(patientId);
      if (!siteInfo.getPatientIdFormatter().isValid(mrn)) {
        throw new NumberFormatException("Invalid MRN");
      }
      return getService().searchPatientsByPatientId(mrn);
    } catch (NumberFormatException nfe) {
      throw new ServiceUnavailableException(patientId + " is not a valid MRN format.");
    } catch (Exception e) {
      logger.error("Error in searchForPatientsByPatientId", e);
      throw new ServiceUnavailableException(e.getMessage());
    }
  }

  @Override
  public ArrayList<Patient> searchForPatientsByName(String partialLastName) {
    try {
      return getService().searchPatientsByName(partialLastName);
    } catch (Exception e) {
      logger.error("Error in searchForPatientsByName", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<Patient> searchForPatientsByAttr(String attr, String value) {
    try {
      if (Constants.ATTRIBUTE_SURVEYEMAIL.equals(attr)) {
        return getService().searchPatientsByEmail(value);
      }
      return getService().searchPatientsByAttr(attr, value);
    } catch (Exception e) {
      logger.error("Error in searchForPatientsByAttr", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<Patient> searchForPatients(String searchString) {
    try {
      return getService().searchPatients(searchString);
    } catch (Exception e) {
      logger.error("Error in searchForPatients", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<PatientStudyExtendedData> searchForPatientStudyDataByName(String nameSearchString, Date dtFrom,
      Date dtTo) {
    try {
      return getService().searchForPatientStudyDataByName(nameSearchString, dtFrom, dtTo);
    } catch (Exception e) {
      logger.error("Error in searchForPatientStudyDataByName", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<PatientStudyExtendedData> searchForPatientStudyDataScores(String patientId, boolean saveToSession)
      throws Exception {
    try {

      ArrayList<PatientStudyExtendedData> patientData = getService().searchForPatientStudyDataScores(patientId);
      if (saveToSession) {
        // Authentication should have already created the session
        HttpSession httpSession = getThreadLocalRequest().getSession(true);
        if (httpSession != null) {
          httpSession.setAttribute("patientStudyData", patientData);
        }
      }
      return patientData;
    } catch (Exception e) {
      logger.error("Error in searchForPatientStudyDataScores", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<PatientStudyExtendedData> searchForPatientStudyDataByPatientId(String patientId, Date thruDate,
      boolean saveToSession) {
    try {

      ArrayList<PatientStudyExtendedData> patientData = getService().searchForPatientStudyDataByPatientId(patientId,
          thruDate);
      if (saveToSession) {
        // Authentication should have already created the session
        HttpSession httpSession = getThreadLocalRequest().getSession(true);
        if (httpSession != null) {
          httpSession.setAttribute("patientStudyData", patientData);
        }
      }
      return patientData;
    } catch (Exception e) {
      logger.error("Error in searchForPatientStudyDataScores", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public Patient addPatient(Patient patient) throws ServiceUnavailableException {
    try {
      return getService().addPatient(patient);

    } catch (Exception e) {
      logger.error("Error in addPatient", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public PatientRegistration addPatientRegistration(ApptRegistration appt, Patient pat) throws ServiceUnavailableException {
    try {

      RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
      ClinicServices clinicService = (ClinicServices) regRequest.getService();
      return clinicService.addPatientRegistration(appt, pat);
    } catch (Exception e) {
      logger.error("Error in addSurveyRegistration", e);
      throw new ServiceUnavailableException(e.toString());
    }

  }

  @Override
  public void updatePatientRegistrations(ArrayList<PatientRegistration> patRegistrationList,
      ArrayList<Date> newRegistrationTimeList) throws ServiceUnavailableException {
    try {
      if (patRegistrationList == null || newRegistrationTimeList == null) {
        logger.error("updatePatientRegistrations cannot process, at least one of the arrays is null!");
        throw new ServiceUnavailableException("Cannot process data!");
      }
      if (patRegistrationList.size() != newRegistrationTimeList.size()) {
        logger.error("updatePatientRegistrations cannot process, the size of the two arrays does not match!");
        throw new ServiceUnavailableException("Cannot process data!");
      }
      for (int inx = 0; inx < patRegistrationList.size(); inx++) {
        getService().updatePatientRegistration(patRegistrationList.get(inx), newRegistrationTimeList.get(inx));
      }

    } catch (Exception e) {
      logger.error("Error in updatePatientRegistration", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public PatientRegistration updatePatientRegistration(PatientRegistration patRegistration,
      Date newRegistrationTime) throws ServiceUnavailableException {
    try {
      return getService().updatePatientRegistration(patRegistration, newRegistrationTime);

    } catch (Exception e) {
      logger.error("Error in updatePatientRegistration", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public void deletePatientRegistration(ApptRegistration appt) throws ServiceUnavailableException {
    try {
      getService().deletePatientRegistration(appt);

    } catch (Exception e) {
      logger.error("Error in deletePatientRegistration", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public void extendRegistration(ApptRegistration appt, Date newDate) throws ServiceUnavailableException {
    try {
      getService().extendRegistration(appt, newDate);

    } catch (Exception e) {
      logger.error("Error in extendRegistration", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public Study registerAssessment(String surveySystemName, String assessmentName, String title, String explanation,
      int version) throws IllegalArgumentException {
    try {
      if (assessmentName != null) {
        assessmentName = escapeHtml(assessmentName);
      }
      if (surveySystemName != null) {
        surveySystemName = escapeHtml(surveySystemName);
      }
      return getService().registerAssessment(surveySystemName, assessmentName, title, explanation, version);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new IllegalArgumentException(e.toString());
    }
  }

  @Override
  public ArrayList<PatientActivity> searchForActivity(Date dtFrom, Date dtTo, boolean includeCompleted)
      throws ServiceUnavailableException {

    try {
      return getService().searchForActivity(dtFrom, dtTo, includeCompleted);
    } catch (Exception e) {
      logger.error("Error occurred in searchForActivity by date range ", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<PatientActivity> searchForActivity(String patientId, boolean includeCompleted)
      throws ServiceUnavailableException {

    try {
      return getService().searchForActivity(patientId, includeCompleted);
    } catch (Exception e) {
      logger.error("Error occurred in searchForActivity by patientId ", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public Integer sendEmails(Long siteId) throws ServiceUnavailableException {
    try {
      RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
      SiteInfo siteInfo = regRequest.getSiteInfo();
      String serverUrl = siteInfo.getProperty("survey.link");
      ClinicServices clinicService = (ClinicServices) regRequest.getService();
      HashMap<String, String> templates = siteInfo.getEmailTemplates();
      return clinicService.handlePendingNotifications(siteInfo.getMailer(), serverUrl, templates);
    } catch (Exception e) {
      logger.error("Error in sendEmails", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public EmailSendStatus sendEmail(ApptRegistration appt) throws ServiceUnavailableException {

    try {
      ArrayList<EmailSendStatus> emailStatus = getService().sendEmail(appt);
      if (emailStatus != null && emailStatus.size() > 0) {
        return emailStatus.get(0);
      }
    } catch (Exception e) {
      logger.error("Error in sendEmail", e);
      throw new ServiceUnavailableException(e.toString());
    }
    logger.error("No status returned from sendEmail(ApptRegistration) + " + appt.getApptId());
    throw new ServiceUnavailableException("Sending Email Failed)");
  }

  @Override
  public PatientAttribute addPatientAttribute(PatientAttribute patAttribute) throws ServiceUnavailableException {
    try {

      return getService().addPatientAttribute(patAttribute);
    } catch (Exception e) {
      logger.error("Error in addPatientAttribute", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public int deletePatientAttribute(PatientAttribute patAttribute) throws ServiceUnavailableException {
    try {

      return getService().deletePatientAttribute(patAttribute);
    } catch (Exception e) {
      logger.error("Error in deletePatientAttribute", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  private ClinicServices getService() {
    RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
    ClinicServices clinicService = (ClinicServices) regRequest.getService();
    return clinicService;
  }

  private String escapeHtml(String html) {
    if (html == null) {
      return null;
    }
    return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
  }

  // Sets the attribute and adds the agrees activity.
  @Override
  public Patient setPatientAgreesToSurvey(Patient patient) throws ServiceUnavailableException {

    try {

      return getService().setPatientAgreesToSurvey(patient);
    } catch (Exception e) {
      logger.error("Error in setPatientAgreesToSurvey", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  // Removes the agrees to survey attribute and adds the declines activity.
  @Override
  public Patient declineEnrollment(Patient patient, DeclineReason reasonCode, String reasonOther) {
    try {
      return getService().declineEnrollment(patient, reasonCode, reasonOther);
    } catch (Exception e) {
      logger.error("Error in setPatientDeclinesSurvey", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public String updatePatientStudy(PatientStudy patStudy) {
    try {
      return getService().updatePatientStudy(patStudy);
    } catch (Exception e) {
      logger.error("Error in updatePatientStudy", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public PatientStudy getPatientStudy(String patientId, Integer studyCode, String token)
      throws ServiceUnavailableException {
    try {
      return getService().getPatientStudy(patientId, studyCode, token);
    } catch (Exception e) {
      logger.error("Error in getPatientStudy", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public String getFormattedPatientId(Long siteId, String patientId) throws DataException, Exception {
    RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
    SiteInfo siteInfo = regRequest.getSiteInfo();
    return siteInfo.getPatientIdFormatter().format(patientId);
  }

  @Override
  public void printScorePdfs(ArrayList<AssessmentId> assessmentIds, int height, int width, int gap)
      throws ServiceUnavailableException {

    try {
      // Authentication should have already created the session, add the id's
      HttpSession httpSession = getThreadLocalRequest().getSession(true);
      if (httpSession != null) {
        ArrayList<Long> ids = new ArrayList<>();
        if (assessmentIds != null) {
          for(AssessmentId assessmentId : assessmentIds) {
            ids.add(assessmentId.getId());
          }
        }
        httpSession.setAttribute(Constants.ASSESSMENT_ID_LIST, ids);
      }

    } catch (Exception e) {
      logger.error("Error setting ids in the session for printing a pdf", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<ArrayList<Object>> averageSurveyTimeReport(Date fromDate, Date toDate)
      throws ServiceUnavailableException {
    try {
      return getService().averageSurveyTimeReport(fromDate, toDate);
    } catch (Exception e) {
      logger.error("Error creating averageSurveyTimeReport", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<ArrayList<Object>> averageSurveyTimeReportByMonth(Date fromDate, Date toDate)
      throws ServiceUnavailableException {
    try {
      return getService().averageSurveyTimeReportByMonth(fromDate, toDate);
    } catch (Exception e) {
      logger.error("Error creating averageSurveyTimeReportByMonth", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<ArrayList<Object>> averageSurveyTimeReportByType(Date fromDate, Date toDate)
      throws ServiceUnavailableException {
    try {
      return getService().averageSurveyTimeReportByType(fromDate, toDate);
    } catch (Exception e) {
      logger.error("Error creating averageSurveyTimeReportByType", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  @Override
  public ArrayList<ArrayList<Object>> complianceSummaryReport(Date from, Date to) throws ServiceUnavailableException {
    try {
      return getService().complianceSummaryReport(from, to);
    } catch (Exception e) {
      logger.error("Error creating complianceSummaryReport", e);
      throw new ServiceUnavailableException(e.toString());
    }

  }

  public ArrayList<ArrayList<Object>> complianceReport1() throws ServiceUnavailableException {
    try {
      return getService().complianceReport1();
    } catch (Exception e) {
      logger.error("Error generating compliance report 1", e);
      throw new ServiceUnavailableException("An error occurred creating the report");
    }
  }

  @Override
  public ArrayList<ArrayList<Object>> enrollmentReportData(Date startDt, Date endDt) throws ServiceUnavailableException {
    try {
      return getService().registrationReportData(startDt, endDt);
    } catch (Exception e) {
      logger.error("Error generating enrollment report", e);
      throw new ServiceUnavailableException(e);
    }
  }

  @Override
  public ArrayList<ArrayList<Object>> visitsReportData(Date startDt, Date endDt) throws ServiceUnavailableException {
    try {
      return getService().visitsReportData(startDt, endDt);
    } catch (Exception e) {
      logger.error("Error generating visits report", e);
      throw new ServiceUnavailableException(e);
    }
  }

  @Override
  public ArrayList<ArrayList<Object>> patientSurveysReport(Date startDt, Date endDt) {
    try {
      return getService().patientSurveysReport(startDt, endDt);
    } catch (Exception e) {
      logger.error("Error getting patient surveys report", e);
      throw new ServiceUnavailableException(e);
    }
  }

  @Override
  public ArrayList<ArrayList<Object>> standardReport(String report, Date startDt, Date endDt) {
    try {
      return getService().standardReport(report, startDt, endDt);
    } catch (Exception e) {
      logger.error("Error generating {} report", report, e);
      throw new ServiceUnavailableException(e);
    }
  }

  @Override
  public String customReport(String reportType, Date startDt, Date endDt) throws ServiceUnavailableException {
    try {
      return getService().customReport(reportType, startDt, endDt);
    } catch (Exception e) {
      logger.error("Error generating custom report", e);
      throw new ServiceUnavailableException(e);
    }
  }

  @Override
  public void setAppointmentStatus(ApptId apptId, AppointmentStatus status) {
    try {
      getService().setAppointmentStatus(apptId, status);
    } catch (Exception e) {
      logger.error("Error setting appointment status", e);
      throw new ServiceUnavailableException(e);
    }
  }

  @SuppressWarnings({ "unchecked", "unused" })
  private ArrayList<PatientStudyExtendedData> getPatientStudiesFromRequest() {

    ArrayList<PatientStudyExtendedData> patientStudies = null;
    HttpSession session = getThreadLocalRequest().getSession(true);

    if (session == null) {
      logger.error("Cannot get patient studies from session, session is null");
    } else {
      patientStudies = (ArrayList<PatientStudyExtendedData>) session.getAttribute("patientStudyData");
      if (patientStudies == null) {
        logger.error("patients is null");
      }
    }
    return patientStudies;
  }

  @Override
  public Boolean updatePatientEmail(Patient pat) {
    try {
      return getService().updatePatientEmail(pat);
    } catch (Exception e) {
      logger.error("Error updating patient email", e);
      logger.error("Error patient_id: " + pat.getPatientId());
    }
    return false;
  }

  @Override
  public Boolean updatePatientNotes(Patient pat) {
    try {
      return getService().updatePatientNotes(pat);
    } catch (Exception e) {
      logger.error("Error updating patient notes", e);
      logger.error("Error patient_id: " + pat.getPatientId());
    }
    return false;
  }


  @Override
  public SurveyStart getSurveyStartStatus(Patient patient, ApptId regId) {
    try {
      return getService().getSurveyStartStatus(patient, regId);
    } catch (Exception e) {
      logger.error("Error in getSurveyStartStatus", e);
      throw new ServiceUnavailableException(e);
    }
  }

  @Override
  public void acceptEnrollment(String patientId, String emailAddress) {
    try {
      getService().acceptEnrollment(patientId, emailAddress);
    } catch (Exception e) {
      logger.error("Error in acceptEnrollment of patientId " + patientId, e);
    }
  }

  @Override
  public ApptRegistration changeSurveyType(String patientId, ApptId apptId, String newType) {
    try {
      return getService().changeSurveyType(patientId, apptId, newType);

    } catch (Exception e) {
      logger.error("Error in changeSurveyType for patientId " + patientId + " ApptRegId " + apptId + " to type " + newType,
          e);
      throw new ServiceUnavailableException(e.getMessage(),e);
    }
  }

  @Override
  public List<DisplayProvider> findDisplayProviders() {
    try {
      return getService().findDisplayProviders();
    } catch (Exception e) {
      logger.error("Error in findDisplayProviders", e);
      throw new ServiceUnavailableException(e);
    }
  }

  @Override
  public void updateUserPreferences(String key, String preferences) {
    try {
      getService().updateUserPreferences(key, preferences);
    } catch (Exception e) {
      logger.error("Error in updatePreferences", e);
      throw new ServiceUnavailableException(e);
    }
  }

  @Override
  public void setSurveyRegAttribute(AssessmentId asmtId, String surveyName, String name, String newValue) {
    try {
      getService().setSurveyRegAttribute(asmtId, surveyName, name, newValue);
    } catch (Exception e) {
      logger.error("Error in setSurveyRegAttribute", e);
      throw new ServiceUnavailableException(e);
    }
  }

  @Override
  public SurveyRegistration getRegistration(String token) {
    return getService().getRegistration(token);
  }

  @Override
  public RandomSetParticipant updateRandomSetParticipant(RandomSetParticipant rsp) {
    try {
      return getService().updateRandomSetParticipant(rsp);
    } catch (Exception e) {
      logger.error("Error in setRandomSetWithdrawn", e);
      throw new ServiceUnavailableException(e);
    }
  }

  @Override
  public ArrayList<RandomSetParticipant> getRandomSets(String patientId) {
    try {
      return getService().getRandomSets(patientId);
    } catch (Exception e) {
      logger.error("Error in getRandomSets", e);
      throw new ServiceUnavailableException(e);
    }
  }

  @Override
  public Boolean customActionMenuCommand(String action, AssessmentId asmtId, Map<String,String> params) {
    try {
      return getService().customActionMenuCommand(action, asmtId, params);
    } catch (Exception e) {
      logger.error("Error in customActionMenuCommand", e);
      throw new ServiceUnavailableException(e);
    }
  }
}
