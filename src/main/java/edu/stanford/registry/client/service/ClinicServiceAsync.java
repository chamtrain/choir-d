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

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ClinicServiceAsync extends PatientIdServiceAsync {

  // RPC Methods for assessments

  void getAssessments(String surveySystemName, int version, AsyncCallback<String> callbak);

  void getActiveVisitProcessNames(AsyncCallback<ArrayList<String>> callback);

  void getProcessNames(AsyncCallback<ArrayList<String>> callback);

  void getSurveyProcessNames(AsyncCallback<ArrayList<String>> callback);

  void getAllProcessAttributes(AsyncCallback<HashMap<String, HashMap<String, String>>> callback);

  void getAllPatientAttributes(AsyncCallback<HashMap<String, ArrayList<PatientAttribute>>> callback);

  // Patients data

  void getPatientsLastAppointmentDate(String patientId, AsyncCallback<Date> callback)
      throws ServiceUnavailableException;

  void getPatientsLastSurveyDate(String patientId, AsyncCallback<Date> callback);

  void getPatientsNextAppointmentDate(String patientId, AsyncCallback<Date> callback)
      throws ServiceUnavailableException;

  void getPatientsNextSurveyDueDate(String patientId, AsyncCallback<Date> callback);

  void searchForPatientsByPatientId(Long siteId, String patientId, AsyncCallback<ArrayList<Patient>> callback);

  void searchForPatientsByName(String partialLastName, AsyncCallback<ArrayList<Patient>> callback);

  void searchForPatientsByAttr(String attr, String value, AsyncCallback<ArrayList<Patient>> callback);
  
  void searchForPatients(String searchString, AsyncCallback<ArrayList<Patient>> callback);

  void addPatient(Patient pat, AsyncCallback<Patient> callback);

  // Patients activity

  void searchForActivity(Date dtFrom, Date dtTo, boolean includeCompleted,
      AsyncCallback<ArrayList<PatientActivity>> callback);

  void searchForActivity(String patientId, boolean includeCompleted, AsyncCallback<ArrayList<PatientActivity>> callback);

  // Patient attributes

  void setPatientAgreesToSurvey(Patient patient, AsyncCallback<Patient> callback);

  void acceptEnrollment(String patientId, String emailAddress, AsyncCallback<Void> callback);

  void declineEnrollment(Patient patient, DeclineReason reasonCode, String reasonOther, AsyncCallback<Patient> callback);

  void updatePatientEmail(Patient pat, AsyncCallback<Boolean> callback);

  void updatePatientNotes(Patient pat, AsyncCallback<Boolean> callback);

  void addPatientAttribute(PatientAttribute patAttribute, AsyncCallback<PatientAttribute> callback);

  void deletePatientAttribute(PatientAttribute patAttribute, AsyncCallback<Integer> callback);

  // Appointment registrations

  void searchForPatientRegistration(Date dtFrom, Date dtTo, PatientRegistrationSearch searchOptions,
      AsyncCallback<ArrayList<PatientRegistration>> callback);

  void searchForPatientRegistration(Date dtFrom, Date dtTo, String type, PatientRegistrationSearch searchOptions,
      // boolean onlyConsented, boolean showCancelled, Boolean printed,
      AsyncCallback<ArrayList<PatientRegistration>> callback);

  void searchForPatientRegistration(String patientId, PatientRegistrationSearch searchOptions,
      AsyncCallback<ArrayList<PatientRegistration>> callback);

  void searchForPatientRegistration(String patientId, String type,
      AsyncCallback<ArrayList<PatientRegistration>> callback);

  void addPatientRegistration(ApptRegistration appt, Patient pat, AsyncCallback<PatientRegistration> callback);

  void deletePatientRegistration(ApptRegistration appt, AsyncCallback<Void> callback) throws ServiceUnavailableException;

  void extendRegistration(ApptRegistration appt, Date newDate, AsyncCallback<Void> callback) throws ServiceUnavailableException;

  void updatePatientRegistration(PatientRegistration patAppt, Date newApptTime,
      AsyncCallback<PatientRegistration> callback) throws ServiceUnavailableException;

  void updatePatientRegistrations(ArrayList<PatientRegistration> patAppt, ArrayList<Date> newApptTime,
      AsyncCallback<Void> callbak) throws ServiceUnavailableException;

  void setAppointmentStatus(ApptId apptId, AppointmentStatus status, AsyncCallback<Void> callback);

  void changeSurveyType(String patientId, ApptId apptId, String newType, AsyncCallback<ApptRegistration> callback)
      throws ServiceUnavailableException;

  void getSurveyStartStatus(Patient patient, ApptId regId, AsyncCallback<SurveyStart> callback);

  // Scores

  void getStudies(AsyncCallback<ArrayList<Study>> callback);

  void getStudies(boolean removeDuplicates, AsyncCallback<ArrayList<Study>> callback);

  void getSurveySystems(AsyncCallback<ArrayList<SurveySystem>> callback);

  void searchForPatientStudyDataByPatientId(String byPatientId, Date dtTo, boolean saveToSession,
      AsyncCallback<ArrayList<PatientStudyExtendedData>> callback) throws ServiceUnavailableException;

  void searchForPatientStudyDataByName(String nameSearchString, Date dtFrom, Date dtTo,
      AsyncCallback<ArrayList<PatientStudyExtendedData>> callback) throws ServiceUnavailableException;

  void searchForPatientStudyDataScores(String patientId, boolean saveToSession,
      AsyncCallback<ArrayList<PatientStudyExtendedData>> callback) throws ServiceUnavailableException;

  // Send out emails

  void sendEmails(Long siteId, AsyncCallback<Integer> callback) throws ServiceUnavailableException;

  void sendEmail(ApptRegistration appt, AsyncCallback<EmailSendStatus> callback)
      throws ServiceUnavailableException;

  void registerAssessment(String surveySystemName, String assessmentName, String title, String explanation,
      int version, AsyncCallback<Study> callback) throws IllegalArgumentException;

  void updatePatientStudy(PatientStudy patStudy, AsyncCallback<String> callback);

  void getPatientStudy(String patientId, Integer studyCode, String token, AsyncCallback<PatientStudy> callback);

  void getFormattedPatientId(Long siteId, String patientId, AsyncCallback<String> callback);

  void printScorePdfs(ArrayList<AssessmentId> assessmentIds, int height, int width, int gap, AsyncCallback<Void> callback)
      throws ServiceUnavailableException;

  void averageSurveyTimeReport(Date fromDate, Date toDate, AsyncCallback<ArrayList<ArrayList<Object>>> callback);

  void averageSurveyTimeReportByMonth(Date fromDate, Date toDate, AsyncCallback<ArrayList<ArrayList<Object>>> callback);

  void averageSurveyTimeReportByType(Date fromDate, Date toDate, AsyncCallback<ArrayList<ArrayList<Object>>> callback);

  void complianceSummaryReport(Date from, Date to, AsyncCallback<ArrayList<ArrayList<Object>>> callback);

  void enrollmentReportData(Date startDt, Date endDt, AsyncCallback<ArrayList<ArrayList<Object>>> callback)
      throws ServiceUnavailableException;

  void visitsReportData(Date startDt, Date endDt, AsyncCallback<ArrayList<ArrayList<Object>>> callback)
      throws ServiceUnavailableException;

  void patientSurveysReport(Date startDt, Date endDt, AsyncCallback<ArrayList<ArrayList<Object>>> callback)
      throws ServiceUnavailableException;

  void standardReport(String report, Date startDt, Date endDt, AsyncCallback<ArrayList<ArrayList<Object>>> callback)
      throws ServiceUnavailableException;

  void customReport(String reportType, Date startDt, Date endDt, AsyncCallback<String> callback)
      throws ServiceUnavailableException;

  void findDisplayProviders(AsyncCallback<List<DisplayProvider>> callback);

  void updateUserPreferences(String key, String preferences, AsyncCallback<Void> async);

  void setSurveyRegAttribute(AssessmentId asmtId, String surveyName, String name, String newValue, AsyncCallback<Void> async);

  void getRegistration(String token, AsyncCallback<SurveyRegistration> callback);

  void updateRandomSetParticipant(RandomSetParticipant rsp, AsyncCallback<RandomSetParticipant> async);

  void getRandomSets(String patientId, AsyncCallback<ArrayList<RandomSetParticipant>> async);
  
  void customActionMenuCommand(String action, AssessmentId asmtId, Map<String,String> params, AsyncCallback<Boolean> async);
}
