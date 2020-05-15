/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.client.api.AssessmentObj;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ApiReportGenerator;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.server.service.rest.api.ApiReportCommon;
import edu.stanford.registry.server.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Supplier;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.SqlSelect;

public class SurveyRegPushReport extends ApiReportCommon implements ApiReportGenerator {


  private static final Logger logger = LoggerFactory.getLogger(SurveyRegPushReport.class);
  private static final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
  @Override
  public JSONObject getReportData(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    JSONObject jsonObject = jsonRepresentation.getJsonObject();
    if (jsonObject == null) {
      logger.warn("handling report {} jsonObject is NULL!!!", reportName);
    }
    if (reportName == null) {
      logger.warn("reportname is null");
      return null;
    }

    if (reportName.equals("surveyRegsPush")) {
      JSONObject returnObject =  patientRegistrationsByType(databaseProvider.get(), siteInfo , getChoice(jsonObject, "patientId"), getChoice(jsonObject, "surveyType"));
      logger.debug(returnObject.toString());
      return returnObject;
    }
    return  jsonObject;
  }

  @Override
  public JSONObject getReportParameters(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName) throws ApiStatusException {
    if (!reportName.equalsIgnoreCase("surveyRegsPush")) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "Report " + reportName + " not supported");
    }
    logger.debug("getting params for {}", reportName  );
    JSONObject params = new JSONObject();
    JSONObject patientParam = makeReportInputOption("Patient", "patientId", "string");
    JSONObject typeParam = makeReportInputOption("Survey Type", "surveyType", "string");
    // Default the from date to a year ago
    Date today = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(today);
    calendar.add(Calendar.YEAR, -1);
    params.accumulate("reportParameters", patientParam);
    // And the to date to today
    params.accumulate("reportParameters", typeParam);
    ArrayList<String> statustypes = new ArrayList<>();

    params.accumulate("reportParameters", makeReportSelectOption("radio","Patients with response", "patresponse", statustypes));
    return params;
  }


  private JSONObject patientRegistrationsByType(Database database, SiteInfo siteInfo, String patientId, String surveyType) {


    ArrayList<AssessmentObj> reportObjs = new ArrayList<>();
    String sql = "select sr.survey_reg_id, sr.survey_dt, sr.survey_type, st.is_complete, sa.data_value  as survey_parent, sa.data_name as attr_name"
        + " from survey_registration sr"
        + " left outer join survey_token st on st.survey_token = sr.token and st.survey_site_id = sr.survey_site_id "
        + " left outer join survey_reg_attr sa on sa.survey_reg_id = sr.survey_reg_id "
        + " where sr.survey_site_id = :site and sr.patient_id = ? and sr.survey_type like ? order by 1";
    SqlSelect select = database.toSelect(sql)
        .argLong(":site", siteInfo.getSiteId())
        .argString(patientId)
        .argString(surveyType + "%");

    return select
        .query(rs -> {
          HashMap<Long, SurveyData> parents = new HashMap();
          JSONObject report = new JSONObject();
          JSONArray datasetArray = new JSONArray();


          while (rs.next()) {
            SurveyData data = new SurveyData();
            data.surveyRegId = rs.getLongOrZero(1);
            logger.debug("surveyRegId is " + data.surveyRegId);
            data.surveyDt = rs.getDateOrNull(2);
            data.surveyType = rs.getStringOrEmpty(3);

            String isComplete = rs.getStringOrNull(4);
            data.isComplete = isComplete == null ? "N"  : isComplete;

            Long parent_reg_id = rs.getLongOrNull(5);
            String attr_name = rs.getStringOrNull(6);



            if (attr_name == null || "STOP".equals(attr_name)) { // its a parent
              data.status = attr_name == null ? "ACTIVE" : attr_name;
              if ("STOP".equalsIgnoreCase(data.status)) {

              }
              data.children = new ArrayList<>();
              parents.put(data.surveyRegId, data);
            } else if (parent_reg_id != null) {
              data.status = "";
                SurveyData parent =   parents.get(parent_reg_id);
                ArrayList<SurveyData> children = parent.children;
                children.add(data);
              } else {
                logger.warn("Parent registration id not found for survey_reg_id {} with survey_reg_attr (name, value) ({},{})", data.surveyRegId, attr_name, parent_reg_id);
              }


          }
          logger.debug("Found {} surveys for {} ", parents.keySet().size(), patientId);
          for (Long key : parents.keySet()) {

            SurveyData parent = parents.get(key);
            JSONObject parentJson = new JSONObject();
            parentJson.put("type", "parent");
            parentJson.put("surveyRegId", parent.surveyRegId.toString());
            parentJson.put("surveyDt", parent.surveyDt != null ? formatter.format(parent.surveyDt) : "");
            parentJson.put("surveyType", parent.surveyType);
            parentJson.put("complete", parent.isComplete);
            parentJson.put("status", parent.status);
            datasetArray.put(parentJson);
            //eport.accumulate("reportDataSet", parentJson);
            if (parent.children != null) {
              for (SurveyData child : parent.children) {
                JSONObject childJson = new JSONObject();
                childJson.put("type", "child");
                childJson.put("surveyRegId","");
                childJson.put("surveyDt", child.surveyDt != null ? formatter.format(child.surveyDt) : "");
                childJson.put("surveyType", child.surveyType);
                childJson.put("complete",child.isComplete);
                childJson.put("status", "");
                //report.accumulate("reportDataSet", childJson);
                datasetArray.put(childJson);
              }
            }
          }
          report.put("reportDataSet", datasetArray);
          return report;
        });

  }

  private ArrayList<ArrayList<Object>> patientsByAttribute(Database database, SiteInfo siteInfo, Date fromDate, Date toDate, String patientResponse) {
    String sql =  "SELECT PAT.PATIENT_ID, PAT.FIRST_NAME, PAT.LAST_NAME, PAT.DT_BIRTH, ATTR.DATA_NAME, ATTR.DATA_VALUE, ATTR.DT_CREATED"
        + " FROM PATIENT PAT, PATIENT_ATTRIBUTE ATTR WHERE ATTR.PATIENT_ID = PAT.PATIENT_ID AND "
        + " ATTR.SURVEY_SITE_ID = :site AND ATTR.DT_CREATED BETWEEN ? AND ? AND ATTR.DATA_NAME = ? AND ATTR.DATA_VALUE = ? ";


    logger.debug("starting patientsByAttribute");
    SqlSelect select = database.toSelect(sql)
        .argLong(":site", siteInfo.getSiteId())
        .argDate(DateUtils.getTimestampStart(siteInfo, fromDate))
        .argDate(DateUtils.getTimestampEnd(siteInfo, toDate));


    return select
        .query(rs -> {
          ArrayList<ArrayList<Object>> report = new ArrayList<>();
          /* headings */
          ArrayList<Object> head = new ArrayList<>();
          head.add("Patient Id");
          head.add("First Name");
          head.add("Last Name");
          head.add("Date Birth");
          head.add("Question");
          head.add("Response");
          head.add("Date");
          report.add(head);
          while (rs.next()) {
            ArrayList<Object> line = new ArrayList<>();
            line.add(rs.getStringOrEmpty(1));
            line.add(rs.getStringOrEmpty(2));
            line.add(rs.getStringOrEmpty(3));
            line.add(rs.getDateOrNull(4));
            String attr = rs.getStringOrEmpty(5);
            String val = rs.getStringOrEmpty(6);

            line.add(val);
            line.add(rs.getDateOrNull(7));
            report.add(line);
          }
          return report;
        });
  }

  private class SurveyData {
    Long surveyRegId;
    Date surveyDt;
    String surveyType;
    String isComplete;
    String status;
    ArrayList<SurveyData> children;
  }
}
