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

import edu.stanford.registry.client.api.AssessmentObj;
import edu.stanford.registry.client.api.PluginPatientDataObj;
import edu.stanford.registry.client.api.PluginPatientHistoryDataObj;
import edu.stanford.registry.client.api.SurveyObj;
import edu.stanford.registry.client.service.AppointmentStatus;
import edu.stanford.registry.shared.DeclineReason;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.DisplayProvider;
import edu.stanford.registry.shared.EmailSendStatus;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientActivity;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.PatientRegistrationSearch;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.PatientStudyObject;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.ResultAction;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveyStart;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.api.SiteObj;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.jfree.chart.JFreeChart;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;

public interface ClinicServices {

  ArrayList<String> getActiveVisitProcessNames();

  ArrayList<String> getProcessNames();

  ArrayList<String> getSurveyProcessNames();

  HashMap<String, HashMap<String, String>> getAllProcessAttributes();

  HashMap<String, ArrayList<PatientAttribute>> getAllPatientAttributes();


  Patient getPatient(String patientId);

  Date getPatientsNextAppointmentDate(String patientId);

  Date getPatientsLastAppointmentDate(String patientId);

  Date getPatientsNextSurveyDueDate(String patientId);

  Date getPatientsLastSurveyDate(String patientId);

  ArrayList<Study> getStudies() throws Exception;

  ArrayList<Study> getStudies(boolean removeDuplicates) throws Exception;

  ArrayList<SurveySystem> getSurveySystems() throws Exception;

  AssessmentRegistration getAssessmentRegistration(AssessmentId assessmentId);

  String getScoresExplanation(String patientId, Integer studyCode,
      ArrayList<PatientStudyExtendedData> patientStudies) throws Exception;

  ArrayList<PatientRegistration> searchForPatientRegistration(Date dateFrom, Date dateTo,
      PatientRegistrationSearch searchOptions) throws DataException, ParseException;

  ArrayList<PatientRegistration> searchForPatientRegistration(Date dateFrom, Date dateTo,
      String registrationType, PatientRegistrationSearch searchOptions) throws DataException, ParseException;

  ArrayList<PatientRegistration> searchForPatientRegistration(String patientId, PatientRegistrationSearch searchOptions);

  ArrayList<PatientRegistration> searchForPatientRegistration(String patientId, String registrationType);

  ArrayList<PatientStudyExtendedData> searchForPatientStudyDataByPatientId(String patientId, Date dtTo);

  ArrayList<PatientStudyExtendedData> searchForPatientStudyDataByName(String nameSearchString, Date dtFrom, Date dtTo);

  ArrayList<PatientStudyExtendedData> searchForPatientStudyDataScores(String patientId) throws Exception;

  ArrayList<Patient> searchPatientsByPatientId(String patientId);

  ArrayList<Patient> searchPatientsByName(String partialLastName);

  ArrayList<Patient> searchPatientsByAttr(String attr, String value);
  
  ArrayList<Patient> searchPatientsByEmail(String value);
  
  ArrayList<Patient> searchPatients(String searchString);

  // Service methods to add or update data
  Patient addPatient(Patient patient);

  PatientRegistration addPatientRegistration(ApptRegistration appt, Patient pat) throws Exception;

  void deletePatientRegistration(ApptRegistration appt);

  void extendRegistration(ApptRegistration appt, Date newDate);

  Patient updatePatient(Patient patient);

  PatientRegistration updatePatientRegistration(PatientRegistration patientAppt, Date newApptTime);

  PatientAttribute addPatientAttribute(PatientAttribute patAttribute);

  int deletePatientAttribute(PatientAttribute patAttribute);

  // Service methods to manage processes
  Integer handlePendingNotifications(Mailer mailer, String serverUrl, HashMap<String, String> templates);

  ArrayList<EmailSendStatus> sendEmail(ApptRegistration appt) throws Exception;

  Study registerAssessment(String surveySystemName, String assessmentName, String title, String explanation,
                           int version);

  String getAssessments(String surveySystemName, int version) throws Exception;

  ArrayList<PatientActivity> searchForActivity(Date dtFrom, Date dtTo, boolean includeCompleted);

  ArrayList<PatientActivity> searchForActivity(String patientId, boolean includeCompleted);

  Patient setPatientAgreesToSurvey(Patient patient) throws ServiceUnavailableException;

  Patient declineEnrollment(Patient patient, DeclineReason reasonCode, String reasonOther) throws ServiceUnavailableException;

  String updatePatientStudy(PatientStudy patStudy);

  PatientStudy getPatientStudy(String patientId, Integer studyCode, String token);

  PatientStudyObject getPatientStudyObject(String patientId, Integer studyCode, String token)
      throws DataException;

  PDDocument getPdf(String patientId, ChartConfigurationOptions opts, ResultAction action)
      throws IOException, ServletException;

  PDDocument getPdf(AssessmentId assessmentId, ChartConfigurationOptions opts, ResultAction action)
      throws IOException, ServletException;

  PDDocument getPdfBefore(AssessmentId assessmentId, ChartConfigurationOptions opts, ResultAction action)
      throws IOException, ServletException;

  ArrayList<PDDocument> getPdfs(ArrayList<AssessmentId> assessmentIds, ChartConfigurationOptions opts, ResultAction action)
      throws IOException, ServletException;

  JSONObject getJSON(AssessmentId assessmentId, ChartConfigurationOptions opts, ResultAction action)
      throws IOException, ServletException, JSONException;

  ArrayList<String> getText(AssessmentId assessmentId, ChartConfigurationOptions opts, ResultAction action)
      throws IOException, ServletException, JSONException;

  byte[] makePng(ArrayList<PatientStudyExtendedData> patientStudies, ChartConfigurationOptions opts)
      throws IOException;

  byte[] getImage(JFreeChart chart, int scale) throws IOException;

  PDDocument getPrintPdfs(ArrayList<Long> surveyRegistrationIds, ChartConfigurationOptions opts)
      throws ServletException,
      IOException;

  Boolean updatePatientEmail(Patient pat);

  Boolean updatePatientNotes(Patient pat);

  /**
   * Reports
   */
  ArrayList<ArrayList<Object>> averageSurveyTimeReport(Date fromDate, Date toDate);

  ArrayList<ArrayList<Object>> averageSurveyTimeReportByMonth(Date fromDate, Date toDate);

  ArrayList<ArrayList<Object>> averageSurveyTimeReportByType(Date fromDate, Date toDate);

  ArrayList<ArrayList<Object>> complianceSummaryReport(Date from, Date to);

  ArrayList<ArrayList<Object>> complianceReport1() throws IOException;

  ArrayList<ArrayList<Object>> registrationReportData(Date startDt, Date endDt);

  ArrayList<ArrayList<Object>> visitsReportData(Date startDt, Date endDt);

  ArrayList<ArrayList<Object>> exportSquareTable(String tablename);

  ArrayList<ArrayList<Object>> exportSquareTable(String tablename, Date fromDt, Date toDt);

  ArrayList<ArrayList<Object>> patientSurveysReport(Date startDt, Date endDt);

  ArrayList<ArrayList<Object>> standardReport(String report, Date startDt, Date endDt);

  JSONObject customApiReport(String reportName, JsonRepresentation jsonRepresentation) throws Exception;

  String customReport(String reportType, Date startDt, Date endDt);

  void setAppointmentStatus(ApptId apptId, AppointmentStatus status);

  SurveyStart getSurveyStartStatus(Patient patient, ApptId regId);

  void acceptEnrollment(String patientId, String emailAddress);

  ApptRegistration changeSurveyType(String patientId, ApptId apptId, String newType) throws IllegalArgumentException;

  ScoreProvider getScoreProvider(ArrayList<PatientStudyExtendedData> patientStudies, Integer study);
  ScoreProvider getScoreProvider(String serviceName, String studyName);
  ChartInfo createChartInfo(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study,
                            boolean withChart, ChartConfigurationOptions opts);

  public List<DisplayProvider> findDisplayProviders();

  /**
   * Adds the preference to the database and current user's preferences on the server.
   * If the client wants to update its copy of the user object's preferences, the client
   * should do that itself before or after this call.
   */
  public void updateUserPreferences(String key, String preferences);

  void setSurveyRegAttribute(AssessmentId asmtId, String surveyName, String name, String newValue);
  void setSurveyRegistrationAttribute(Long surveyRegId, String attrName, String attrValue) throws NotFoundException;

  SurveyRegistration getRegistration(String token);

  ApptRegistration getLastCompletedRegistration(String patientId);

  AssessmentObj getAssessmentObj(Long assessmentId) throws NotFoundException;

  ArrayList<AssessmentObj> getAssessmentObjs(String patientId) throws NotFoundException;

  SurveyObj getSurveyObj(String token) throws NotFoundException;

  RandomSetParticipant updateRandomSetParticipant(RandomSetParticipant rsp);

  ArrayList<RandomSetParticipant> getRandomSets(String patientId);
  
  Boolean customActionMenuCommand(String action, AssessmentId asmtId, Map<String,String> params);

  ArrayList<SiteObj> getSiteObjs();

  SiteObj getSiteObjById(Long id);

  SiteObj getSiteObjByParam(String param);

  Map<String,String> getSurveyRegistrationAttributes(Long surveyRegId);

  ApptRegistration updateApptRegistration(ApptRegistration apptRegistration);

  void cancelRegistration(AssessmentId assessmentId) throws NotFoundException;

  PluginPatientDataObj findPluginPatientData(Long dataId);

  PluginPatientDataObj findPluginPatientData(String dataType, String patientId, String dataVersion);

  ArrayList<PluginPatientDataObj> findAllPluginPatientData(String dataType, String patientId, String dataVersion);

  ArrayList<PluginPatientDataObj> findAllPluginPatientData(String dataType, String patientId, String dataVersion, Date fromTime, Date toTime);

  ArrayList<PluginPatientHistoryDataObj> findAllPluginPatientDataHistory(String dataType, String patientId, String dataVersion);

  PluginPatientDataObj addPluginPatientData(String dataType, String patientId, String dataVersion, String dataValue);
  
  JSONObject handleCustomApi(String callString, Map<String, String[]> params, JsonRepresentation jsonRep) throws ApiStatusException;
}
