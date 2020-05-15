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

package edu.stanford.registry.client.service;

import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.DeclineReason;
import edu.stanford.registry.shared.DisplayProvider;
import edu.stanford.registry.shared.EmailSendStatus;
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

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("clinicService")
public interface ClinicService extends PatientIdService {

  // RPC Methods for assessments

  String getAssessments(String surveySystemName, int version) throws ServiceUnavailableException;

  ArrayList<String> getActiveVisitProcessNames() throws Exception;

  ArrayList<String> getProcessNames() throws ServiceUnavailableException, Exception;

  ArrayList<String> getSurveyProcessNames() throws ServiceUnavailableException, Exception;

  HashMap<String, HashMap<String, String>> getAllProcessAttributes() throws ServiceUnavailableException, Exception;

  HashMap<String, ArrayList<PatientAttribute>> getAllPatientAttributes() throws ServiceUnavailableException, Exception;

  // Patients data

  Date getPatientsLastAppointmentDate(String patientId) throws ServiceUnavailableException;

  Date getPatientsLastSurveyDate(String patientId) throws ServiceUnavailableException;

  Date getPatientsNextAppointmentDate(String patientId) throws ServiceUnavailableException;

  Date getPatientsNextSurveyDueDate(String patientId) throws ServiceUnavailableException;

  ArrayList<Patient> searchForPatientsByPatientId(Long siteId, String patientId) throws Exception;

  ArrayList<Patient> searchForPatientsByName(String partialLastName) throws Exception;

  ArrayList<Patient> searchForPatientsByAttr(String attr, String value);
  
  ArrayList<Patient> searchForPatients(String searchString) throws Exception;

  Patient addPatient(Patient pat) throws ServiceUnavailableException;

  // Patients activity

  ArrayList<PatientActivity> searchForActivity(Date dtFrom, Date dtTo, boolean includeCompleted)
      throws ServiceUnavailableException;

  ArrayList<PatientActivity> searchForActivity(String patientId, boolean includeCompleted)
      throws ServiceUnavailableException;

  // Patient attributes

  Patient setPatientAgreesToSurvey(Patient patient) throws ServiceUnavailableException;

  void acceptEnrollment(String patientId, String emailAddress);

  Patient declineEnrollment(Patient patient, DeclineReason reasonCode, String reasonOther) throws ServiceUnavailableException;

  Boolean updatePatientEmail(Patient pat);

  Boolean updatePatientNotes(Patient pat);

  PatientAttribute addPatientAttribute(PatientAttribute patAttribute) throws ServiceUnavailableException;

  int deletePatientAttribute(PatientAttribute patAttribute);

  // Appointment registrations

  ArrayList<PatientRegistration> searchForPatientRegistration(Date dtFrom, Date dtTo,
      PatientRegistrationSearch searchOptions);

  ArrayList<PatientRegistration> searchForPatientRegistration(Date dtFrom, Date dtTo, String type,
      // boolean onlyConsented, boolean showCancelled, Boolean printed);
      PatientRegistrationSearch searchOptions);

  ArrayList<PatientRegistration> searchForPatientRegistration(String patientId, PatientRegistrationSearch searchOptions);

  ArrayList<PatientRegistration> searchForPatientRegistration(String patientId, String type);

  PatientRegistration addPatientRegistration(ApptRegistration appt, Patient pat) throws ServiceUnavailableException;

  void deletePatientRegistration(ApptRegistration appt) throws ServiceUnavailableException;

  void extendRegistration(ApptRegistration appt, Date newDate) throws ServiceUnavailableException;

  PatientRegistration updatePatientRegistration(PatientRegistration patAppt, Date newApptTime)
      throws ServiceUnavailableException;

  void updatePatientRegistrations(ArrayList<PatientRegistration> patAppt, ArrayList<Date> newApptTime);

  void setAppointmentStatus(ApptId apptId, AppointmentStatus status);

  ApptRegistration changeSurveyType(String patientId, ApptId apptId, String newType)
      throws ServiceUnavailableException;

  SurveyStart getSurveyStartStatus(Patient patient, ApptId regId) throws Exception;

  // Scores

  ArrayList<Study> getStudies() throws Exception;

  ArrayList<Study> getStudies(boolean removeDuplicates) throws Exception;

  ArrayList<SurveySystem> getSurveySystems() throws Exception;

  ArrayList<PatientStudyExtendedData> searchForPatientStudyDataByPatientId(String patientId, Date dtTo,
      boolean saveToSession);

  ArrayList<PatientStudyExtendedData> searchForPatientStudyDataByName(String nameSearchString, Date dtFrom,
      Date dtTo);

  ArrayList<PatientStudyExtendedData> searchForPatientStudyDataScores(String patientId, boolean saveToSession)
      throws Exception;

  // Send out emails

  Integer sendEmails(Long siteId);

  /* returns object that identifies whether successful or why it failed */
  EmailSendStatus sendEmail(ApptRegistration appts) throws ServiceUnavailableException;

  Study registerAssessment(String surveySystemName, String assessmentName, String title, String explanation, int version)
      throws ServiceUnavailableException;

  String updatePatientStudy(PatientStudy patStudy);

  PatientStudy getPatientStudy(String patientId, Integer studyCode, String token);

  String getFormattedPatientId(Long siteId, String patientId) throws DataException, Exception;

  void printScorePdfs(ArrayList<AssessmentId> assessmentIds, int height, int width, int gap);

  ArrayList<ArrayList<Object>> averageSurveyTimeReport(Date fromDate, Date toDate);

  ArrayList<ArrayList<Object>> averageSurveyTimeReportByMonth(Date fromDate, Date toDate);

  ArrayList<ArrayList<Object>> averageSurveyTimeReportByType(Date fromDate, Date toDate);

  ArrayList<ArrayList<Object>> complianceSummaryReport(Date from, Date to);

  ArrayList<ArrayList<Object>> enrollmentReportData(Date startDt, Date endDt) throws ServiceUnavailableException;

  ArrayList<ArrayList<Object>> visitsReportData(Date startDt, Date endDt) throws ServiceUnavailableException;

  ArrayList<ArrayList<Object>> patientSurveysReport(Date startDt, Date endDt);

  ArrayList<ArrayList<Object>> standardReport(String report, Date startDt, Date endDt) throws ServiceUnavailableException;

  String customReport(String reportType, Date startDt, Date endDt) throws ServiceUnavailableException;

  List<DisplayProvider> findDisplayProviders();

  void updateUserPreferences(String key, String preferences);

  void setSurveyRegAttribute(AssessmentId asmtId, String surveyName, String name, String newValue);

  SurveyRegistration getRegistration(String token);

  RandomSetParticipant updateRandomSetParticipant(RandomSetParticipant rsp);

  ArrayList<RandomSetParticipant> getRandomSets(String patientId);
  
  Boolean customActionMenuCommand(String action, AssessmentId asmtId, Map<String,String> params);
}
