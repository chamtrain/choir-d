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

public class EmpowerInterestReport extends ApiReportCommon implements ApiReportGenerator {

  private static final Logger logger = LoggerFactory.getLogger(EmpowerInterestReport.class);
  private static final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
  private static final SimpleDateFormat tmFormatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

  @Override
  public JSONObject getReportData(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    JSONObject jsonObject = jsonRepresentation.getJsonObject();
    if (jsonObject == null) {
      logger.warn("handling report {} jsonObject is NULL!", reportName);
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "Report " + reportName + " called with no json");
    }

    if (reportName == null || !reportName.equals("empowerInterest")) {
      logger.warn("reportname is null");
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "Report " + reportName + " not supported");
    }

    return getReturnObject(patientsResponses(databaseProvider.get(), siteInfo, getStartDt(jsonObject), getEndDt(jsonObject)));
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

    return params;
  }

  private ArrayList<ArrayList<Object>> patientsResponses(Database database, SiteInfo siteInfo, Date fromDate, Date toDate) {
    logger.trace("Patients interested in the Empower Study in Survey Date range {} -  {} for: {}", fromDate.toString(), toDate.toString());

    final String sql =
        " select rpt.PATIENT_ID, p.FIRST_NAME, p.LAST_NAME, rpt.SURVEY_SCHEDULED,"
            + "  rpt.SURVEY_ENDED, ar.CLINIC "
            + "from RPT_PAIN_STD_SURVEYS_SQUARE rpt "
            + "join PATIENT p on p.PATIENT_ID = rpt.PATIENT_ID "
            + "join SURVEY_TOKEN st on rpt.SURVEY_TOKEN_ID = st.SURVEY_TOKEN_ID and rpt.SURVEY_SITE_ID = st.SURVEY_SITE_ID "
            + "join SURVEY_REGISTRATION sr on sr.TOKEN = st.SURVEY_TOKEN and sr.SURVEY_SITE_ID = st.SURVEY_SITE_ID "
            + "join APPT_REGISTRATION ar on ar.ASSESSMENT_REG_ID = sr.ASSESSMENT_REG_ID and ar.SURVEY_SITE_ID = sr.SURVEY_SITE_ID "
            + "where EMPOWER_LEARN_ABOUT_STUDY = 1 and rpt.is_complete = 'Y' "
            + " and ar.SURVEY_SITE_ID = :site and (ar.visit_dt between ? and ?) and ar.registration_type != 'c' "
            + " order by rpt.SURVEY_SCHEDULED, p.LAST_NAME";

    Map<String, List<String>> clinicMapping =  siteInfo.getRegistryCustomizer().getClientConfig().getClinicFilterMapping();
    /*
      Run the report
     */
    return database.toSelect(sql)
        .argLong(":site", siteInfo.getSiteId())
        .argDate(DateUtils.getTimestampStart(siteInfo, fromDate))
        .argDate(DateUtils.getTimestampEnd(siteInfo, toDate))
        .query(rs -> {
          ArrayList<ArrayList<Object>> report = new ArrayList<>();

          /* heading line */
          ArrayList<Object> head = new ArrayList<>();
          head.add("Patient Id  ");
          head.add("First Name");
          head.add("Last Name");
          head.add("Survey Date");
          head.add("Completed");
          head.add("Clinic");
          report.add(head);

          /* details */
          while (rs.next()) {
            ArrayList<Object> reportLine = new ArrayList<>();
            reportLine.add(rs.getStringOrEmpty(1));
            reportLine.add(rs.getStringOrEmpty(2));
            reportLine.add(rs.getStringOrEmpty(3));
            Date scheduled = rs.getDateOrNull(4);
            String scheduledStr = "";
            if (scheduled != null) {
              scheduledStr = tmFormatter.format(scheduled);
            }
            reportLine.add(scheduledStr);
            Date completed = rs.getDateOrNull(5);
            String completedStr = "";
            if (completed != null) {
              completedStr = tmFormatter.format(completed);
            }
            reportLine.add(completedStr);
            reportLine.add(getClinicDisplayName(clinicMapping, rs.getStringOrEmpty(6)));
            report.add(reportLine);
          }
          return report;
        });
  }
}

