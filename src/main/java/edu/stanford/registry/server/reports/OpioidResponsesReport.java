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
package edu.stanford.registry.server.reports;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ApiReportGenerator;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.server.service.rest.api.ApiReportCommon;
import edu.stanford.registry.server.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

public class OpioidResponsesReport extends ApiReportCommon implements ApiReportGenerator {

  private static final String ALL_PATIENTS = "All patients";
  private static final String YES_PATIENTS = "Only patients that responded Yes";
  private static final String ANSWERED_PATIENTS = "Only patients with a response (Yes/No)";
  private static final String WITH_CLBP = "Only patients with chronic low back pain";

  private static final Logger logger = LoggerFactory.getLogger(OpioidResponsesReport.class);
  private static final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
  private static final SimpleDateFormat tmFormatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

  @Override
  public JSONObject getReportData(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    JSONObject jsonObject = jsonRepresentation.getJsonObject();
    if (jsonObject == null) {
      logger.warn("handling report {} jsonObject is NULL!", reportName);
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "Report " + reportName + " called with no json");
    }

    if (reportName == null || !reportName.equals("opioidResponses")) {
      logger.warn("reportname is null");
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "Report " + reportName + " not supported");
    }
    return getReturnObject(patientsResponses(databaseProvider.get(), siteInfo, getStartDt(jsonObject), getEndDt(jsonObject),
        getChoice(jsonObject, "allpatients"), getChoice(jsonObject, "clbppatients")));
  }

  @Override
  public JSONObject getReportParameters(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName) {
    JSONObject params = new JSONObject();

    // Default the from and to dates to today
    JSONObject fromParam = makeReportInputOption("From", "fromDt", "date");
    fromParam.put("value", formatter.format(DateUtils.getDateStart(siteInfo, new Date())));
    params.accumulate("reportParameters", fromParam);

    JSONObject toParam = makeReportInputOption("To", "toDt", "date");
    toParam.put("value", formatter.format(DateUtils.getDateEnd(siteInfo, new Date())));
    params.accumulate("reportParameters", toParam);

    ArrayList<String> choices = new ArrayList<>();
    choices.add(ALL_PATIENTS);
    choices.add(YES_PATIENTS);
    choices.add(ANSWERED_PATIENTS);
    params.accumulate("reportParameters", makeReportSelectOption("radio", "Include in report", "allpatients", choices));

    JSONObject optionObject = new JSONObject();
    optionObject.put("type", "checkbox");
    optionObject.put("title", WITH_CLBP);
    optionObject.put("name", "clbppatients");
    optionObject.put("value", "");
    params.accumulate("reportParameters",optionObject);

    return params;
  }

  private ArrayList<ArrayList<Object>> patientsResponses(Database database, SiteInfo siteInfo, Date fromDate, Date toDate, String patientResponse, String clbp) {
    boolean withClbp = "true".equals(clbp);
    logger.trace("OpioidResponses report for Date ranage from {} - to {} for: {} with cLBP {}", fromDate.toString(), toDate.toString(), patientResponse, withClbp);

    final String sql = "with patients_in_range as ("
        + "SELECT ar.patient_id, p.first_name, p.last_name, p.dt_birth, ar.visit_dt as visit_dt, ar.survey_site_id,"
        + "    (select max(survey_token_id) from survey_registration sr "
        + "      join survey_token st on sr.token = st.survey_token and sr.survey_site_id = st.survey_site_id "
        + "      where sr.patient_id = ar.patient_id and sr.survey_site_id = ar.survey_site_id and is_complete = 'Y') as last_survey_tkid ,"
        + "    (select max(survey_token_id) from survey_registration sr "
        + "       join survey_token st on sr.token = st.survey_token and sr.survey_site_id = st.survey_site_id "
        + "      where sr.patient_id = ar.patient_id and sr.survey_site_id = ar.survey_site_id and is_complete = 'Y' "
        + "        and sr.survey_type like 'Init%' ) as last_initial_survey_tkid, clinic, "
        + "     (select data_value from patient_attribute pa where data_name = 'research' "
        + "      and pa.patient_id = p.patient_id and pa.survey_site_id = ar.survey_site_id) as research, "
        + "     (select data_value from patient_attribute pa where data_name = 'surveyEmailAddressAlt'"
        + "       and pa.patient_id = p.patient_id and pa.survey_site_id = ar.survey_site_id ) as email_alt, "
        + "     (select data_value from patient_attribute pa where data_name = 'surveyEmailAddress'"
        + "       and pa.patient_id = p.patient_id and pa.survey_site_id = ar.survey_site_id ) as email_add"
        + "  from appt_registration ar join patient p on p.patient_id = ar.patient_id "
        + " where ar.survey_site_id = :site and (ar.visit_dt between ? and ?) and ar.registration_type != 'c') "
        + "SELECT p.patient_id, p.first_name, p.last_name, p.dt_birth, p.visit_dt, "
        + "    (select CASE WHEN std.ops_currently_taking = 0 THEN 'Yes' WHEN std.ops_currently_taking = 1 THEN 'No' ELSE null END "
        + "       from rpt_pain_std_surveys_square std where std.survey_token_id = p.last_survey_tkid "
        + "        and std.patient_id = p.patient_id and std.survey_site_id = p.survey_site_id) as last_Opioids_resp,"
        + "    (select survey_scheduled from rpt_pain_std_surveys_square std where std.survey_token_id = p.last_survey_tkid "
        + "        and std.patient_id = p.patient_id and std.survey_site_id = p.survey_site_id) as last_completed_survey_dt,"
        + "    (select survey_ended from rpt_pain_std_surveys_square std where std.survey_token_id = p.last_survey_tkid "
        + "        and std.patient_id = p.patient_id and std.survey_site_id = p.survey_site_id) as last_completed_ended_dt,"
        + "    (select CASE when ((M_OPIO_ACTIQ = '1' and M_OPIO_ACTIQ_STILLTAKE = '1') or (M_OPIO_DURAGESIC = '1' and M_OPIO_DURAGESIC_STILLTAKE = '1')"
        + "       or (M_OPIO_VICODIN = '1' and M_OPIO_VICODIN_STILLTAKE = '1') or (M_OPIO_DILAUDID = '1' and M_OPIO_DILAUDID_STILLTAKE = '1') "
        + "       or (M_OPIO_DEMEROL = '1' and M_OPIO_DEMEROL_STILLTAKE = '1') or (M_OPIO_METHADONE = '1' and M_OPIO_METHADONE_STILLTAKE = '1') "
        + "       or (M_OPIO_MS = '1' and M_OPIO_MS_STILLTAKE = '1') or (M_OPIO_PERCOCET = '1' and M_OPIO_PERCOCET_STILLTAKE = '1')"
        + "       or ( M_OPIO_OPANA = '1' and M_OPIO_OPANA_STILLTAKE = '1') or (M_OPIO_NUCYNTA = '1' and M_OPIO_NUCYNTA_STILLTAKE = '1') "
        + "       or (M_OPIO_ULTRAM = '1' and M_OPIO_ULTRAM_STILLTAKE = '1')) THEN 'Yes' ELSE 'No' END from rpt_treatmenthx_square tx  "
        + "     where tx.survey_token_id = p.last_initial_survey_tkid and tx.patient_id = p.patient_id "
        + "       and tx.survey_site_id = p.survey_site_id) as last_medsopioids_resp,"
        + "    (select survey_scheduled from rpt_pain_std_surveys_square std"
        + "       where std.survey_token_id = p.last_initial_survey_tkid and std.patient_id = p.patient_id "
        + "       and std.survey_site_id = p.survey_site_id) as last_completed_init_survey_dt,"
        + "    (select survey_ended from rpt_pain_std_surveys_square std"
        + "      where std.survey_token_id = p.last_initial_survey_tkid and std.patient_id = p.patient_id "
        + "        and std.survey_site_id = p.survey_site_id) as last_completed_Init_ended_dt, "
        + "     (select CASE when (COPCS_LBP_PAINHOWLONG >= 2 and COPCS_LBP_PAINFREQ >= 1) THEN 'Yes' ELSE 'No' END from rpt_pain_std_surveys_square tx      \n"
        + "        where tx.survey_token_id = p.last_initial_survey_tkid and tx.patient_id = p.patient_id        \n"
        + "        and tx.survey_site_id = p.survey_site_id) as chronic_lower_back_pain, "
        + "    clinic, research, email_alt, email_add"
        + "       from patients_in_range p order by visit_dt, last_name, first_name, patient_id ";

    /*
      Run query to get the data needed to create the report
     */
    ArrayList<OpioidResponse> opioidResponses = database.toSelect(sql)
        .argLong(":site", siteInfo.getSiteId())
        .argDate(DateUtils.getTimestampStart(siteInfo, fromDate))
        .argDate(DateUtils.getTimestampEnd(siteInfo, toDate))
        .query(rs -> {
          ArrayList<OpioidResponse> responses = new ArrayList<>();
          while (rs.next()) {
            OpioidResponse response = new OpioidResponse();
            response.setPatientId(rs.getStringOrEmpty(1));
            response.setFirstName(rs.getStringOrEmpty(2));
            response.setLastName(rs.getStringOrEmpty(3));
            response.setDtBirth(rs.getDateOrNull(4));
            response.setSurveyDt(rs.getDateOrNull(5));
            response.setOpioidsResponse(rs.getStringOrNull(6));
            response.setLastCompletedSurveyDt(rs.getDateOrNull(7));
            response.setLastCompletedEndedDt(rs.getDateOrNull(8));
            response.setMedsOpioidResponse(rs.getStringOrNull(9));
            response.setLastCompletedInitSurveyDt(rs.getDateOrNull(10));
            response.setLastCompletedInitEndedDt(rs.getDateOrNull(11));
            response.setChronicLowerBackPain(rs.getStringOrEmpty(12));
            response.setClinicName(rs.getStringOrEmpty(13));
            response.setResearch(rs.getStringOrEmpty(14));
            response.setEmailAlt(rs.getStringOrEmpty(15));
            response.setEmailAdd(rs.getStringOrEmpty(16));
            responses.add(response);
          }
          return responses;
        });

    ArrayList<ArrayList<Object>> report = new ArrayList<>();

    /* heading line */
    ArrayList<Object> head = new ArrayList<>();
    head.add("Patient Id");
    head.add("First Name");
    head.add("Last Name");
    head.add("Age");
    head.add("Survey Date");
    head.add("Response");
    head.add("Response Survey Date");
    head.add("Survey Completed Date");
    head.add("cLBP");
    head.add("Clinic");
    head.add("Contact");
    head.add("Email Address");
    report.add(head);

    Map<String, List<String>> clinicMapping =  siteInfo.getRegistryCustomizer().getClientConfig().getClinicFilterMapping();
    /* detail lines */
    for (OpioidResponse response : opioidResponses) {
      String opioidResponse = "N/A";
      Date opioidResponseSurveyDt = null;
      Date opioidResponseEndedDt = null;
      /*
        Use the latest follow up response unless an initial survey was the last type completed - odd but possible.
        WHen there is no follow up response, use the initial survey question response.
        First case: there is a follow up response and the last initial survey was not after the follow up.
       */
      if (response.getOpioidsResponse() != null && response.getLastCompletedInitSurveyDt() != null
          && response.getLastCompletedSurveyDt() != null
          && response.getLastCompletedInitSurveyDt().getTime() < response.getLastCompletedSurveyDt().getTime()) {
        opioidResponse = response.getOpioidsResponse();
        opioidResponseSurveyDt = response.getLastCompletedSurveyDt();
        opioidResponseEndedDt = response.getLastCompletedEndedDt();
      } else if (response.getOpioidsResponse() != null && response.getLastCompletedInitSurveyDt() == null) {
        /*
         Next case: there is a follow up response and the patient did not complete an initial survey.
         */
        opioidResponse = response.getOpioidsResponse();
        opioidResponseSurveyDt = response.getLastCompletedSurveyDt();
        opioidResponseEndedDt = response.getLastCompletedEndedDt();
      } else if (response.getMedsOpioidResponse() != null) {
        /*
         Last case: no qualifying follow up response but there is an initial survey response
         */
        opioidResponse = response.getMedsOpioidResponse();
        opioidResponseSurveyDt = response.getLastCompletedInitSurveyDt();
        opioidResponseEndedDt = response.getLastCompletedInitEndedDt();
      }

      /*
        The reportLine is included if option to include "all patients" was selected ( no matter what the response value ).
        OR The patient responded yes ( qualifies under all 3 options )
        OR The patient responded no and the option to include all patients with a response was selected.
       */
      if (ALL_PATIENTS.equalsIgnoreCase(patientResponse) || "Yes".equals(opioidResponse) ||
          ("No".equals(opioidResponse) && patientResponse.equals(ANSWERED_PATIENTS))) {
        ArrayList<Object> reportLine = new ArrayList<>();
        reportLine.add(response.getPatientId());
        reportLine.add(response.getFirstName());
        reportLine.add(response.getLastName());
        reportLine.add(response.getAge());
        reportLine.add(tmFormatter.format(response.getSurveyDt()));
        reportLine.add(opioidResponse);
        String surveyDtStr = "";
        if ( opioidResponseSurveyDt != null) {
          surveyDtStr = tmFormatter.format(opioidResponseSurveyDt);
        }
        reportLine.add(surveyDtStr);

        String endedDtStr = "";
        if (opioidResponseEndedDt != null) {
          endedDtStr = tmFormatter.format(opioidResponseEndedDt);
        }
        reportLine.add(endedDtStr);
        reportLine.add(response.getChronicLowerBackPain());
        reportLine.add(getClinicDisplayName(clinicMapping, response.getClinicName()));
        if ( !withClbp || "Yes".equals(response.getChronicLowerBackPain())) {
          report.add(reportLine);
        }
        reportLine.add(response.getResearch());
        reportLine.add(response.getEmail());
      }
    }
    return report;
  }

  /**
   * Object to hold a row of data returned from the query
   */
  private static class OpioidResponse {
    private String patientId;
    private String firstName;
    private String lastName;
    private Date dtBirth;
    private Date visitDt;
    private String opioidsResponse;
    private Date lastCompletedSurveyDt;
    private Date lastCompletedEndedDt;
    private String medsOpioidResponse;
    private Date lastCompletedInitSurveyDt;
    private Date lastCompletedInitEndedDt;
    private String clinicName;
    private String research;
    private String emailAlt;
    private String emailAdd;

    String getPatientId() {
      return patientId;
    }

    void setPatientId(String patientId) {
      this.patientId = patientId;
    }

    String getFirstName() {
      return firstName;
    }

    void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    String getLastName() {
      return lastName;
    }

    void setLastName(String lastName) {
      this.lastName = lastName;
    }

    Date getDtBirth() {
      return dtBirth;
    }

    Integer getAge() {
      return DateUtils.getAge(getDtBirth());
    }

    void setDtBirth(Date dtBirth) {
      this.dtBirth = dtBirth;
    }

    Date getSurveyDt() {
      return visitDt;
    }

    void setSurveyDt(Date visitDt) {
      this.visitDt = visitDt;
    }

    String getOpioidsResponse() {
      return opioidsResponse;
    }

    void setOpioidsResponse(String opioidsResponse) {
      this.opioidsResponse = opioidsResponse;
    }

    Date getLastCompletedSurveyDt() {
      return lastCompletedSurveyDt;
    }

    void setLastCompletedSurveyDt(Date lastCompletedSurveyDt) {
      this.lastCompletedSurveyDt = lastCompletedSurveyDt;
    }

    Date getLastCompletedEndedDt() {
      return lastCompletedEndedDt;
    }

    void setLastCompletedEndedDt(Date lastCompletedEndedDt) {
      this.lastCompletedEndedDt = lastCompletedEndedDt;
    }

    String getMedsOpioidResponse() {
      return medsOpioidResponse;
    }

    void setMedsOpioidResponse(String opioidsResponse) {
      this.medsOpioidResponse = opioidsResponse;
    }

    Date getLastCompletedInitSurveyDt() {
      return lastCompletedInitSurveyDt;
    }

    void setLastCompletedInitSurveyDt(Date lastCompletedInitSurveyDt) {
      this.lastCompletedInitSurveyDt = lastCompletedInitSurveyDt;
    }

    Date getLastCompletedInitEndedDt() {
      return lastCompletedInitEndedDt;
    }

    void setLastCompletedInitEndedDt(Date lastCompletedInitEndedDt) {
      this.lastCompletedInitEndedDt = lastCompletedInitEndedDt;
    }

    String getChronicLowerBackPain() {
      return medsOpioidResponse;
    }

    void setChronicLowerBackPain(String opioidsResponse) {
      this.medsOpioidResponse = opioidsResponse;
    }

    String getClinicName() {
      return clinicName;
    }

    void setClinicName(String clinicName) {
      this.clinicName = clinicName;
    }

    private String getResearch() {
      if ("y".equalsIgnoreCase(research)) {
        return "Yes";
      }
      return "No";
    }

    private void setResearch(String answer) {
      research = answer;
    }

    private String getEmail() {
      if (emailAlt != null) {
        return emailAlt;
      }
      return emailAdd;
    }

    private void setEmailAlt(String email) {
      emailAlt = email;
    }

    private void setEmailAdd(String email) {
      emailAdd = email;
    }
  }
}

