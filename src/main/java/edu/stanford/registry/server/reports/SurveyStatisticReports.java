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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ApiReportGenerator;
import edu.stanford.registry.server.service.ApiReportGeneratorBase;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.server.service.rest.api.ApiReportCommon;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Flavor;

public class SurveyStatisticReports extends ApiReportGeneratorBase implements ApiReportGenerator {


  private static final Logger logger = LoggerFactory.getLogger(SurveyStatisticReports.class);


  @Override
  public JSONObject getReportData(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName,
                                  JsonRepresentation jsonRepresentation)  {
    JSONObject jsonObject = jsonRepresentation.getJsonObject();
    if (jsonObject == null) {
      logger.warn("handling report {} jsonObject is NULL!!!", reportName);
    }
    if (reportName == null) {
      logger.warn("reportname is null");
      return null;
    }
    ApiReportCommon utils = new ApiReportCommon();
    try {

        Date fromDate = utils.getStartDt(jsonObject);
        Date toDate = utils.getEndDt(jsonObject);
        JSONObject reportData = utils.getReturnObject(run(databaseProvider.get(), siteInfo , fromDate,
            toDate));
        reportData.put("h1", "Patient Surveys");
        reportData.put("h2", "From " +  siteInfo.getDateOnlyFormatter().getDateString(fromDate)
        + " to " + siteInfo.getDateOnlyFormatter().getDateString(toDate));
        return reportData;

    } catch (ApiStatusException apise) {
      String message = "Invalid date parameter sent for statsUnique report";
      logger.error(message);
      ArrayList<ArrayList<Object>> returnError = new ArrayList<>();
      ArrayList<Object> head = new ArrayList<>();
      head.add(message);
      returnError.add(head);
      return utils.getReturnObject(returnError);
    }
  }



  private ArrayList<ArrayList<Object>> run(Database database, SiteInfo siteInfo, Date fromDate, Date toDate) {
    final String WHERE_CLAUSE =  " where survey_site_id = :site and survey_user_time_ms > 0 "
        + " and patient_id not in (select patient_id from patient where last_name like 'Test-Patient')";
    final String AND_NOTQST = " and assessment_type not like 'QST%'";
    final String DT_RANGE = getSqlDateRangeClause(database);

    logger.debug("starting surveysAnswered");
    final ArrayList<ArrayList<Object>> report = new ArrayList<>();
    ArrayList<Object> head = new ArrayList<>();
    head.add(" ");
    head.add(" ");
    report.add(head);
    // Unique patient counts
    String sqlUniqStarted =  "with uniquepats as ("
    + "select patient_id, count(*) FROM rpt_pain_std_surveys_square"
        + WHERE_CLAUSE + DT_RANGE + AND_NOTQST
        + " group by patient_id )"
        + " select 'Number of unique patients that have begun a survey ', count(*) from uniquepats";
    report.add(runSql(database,sqlUniqStarted, siteInfo.getSiteId(), fromDate, toDate));

    String sqlUniqCompleted = "with uniquepats as ("
        + "select patient_id, count(*) from rpt_pain_std_surveys_square "
        + WHERE_CLAUSE + DT_RANGE + AND_NOTQST
        + " and is_complete = 'Y'"
        + " group by patient_id ) "
        + " select 'Number of unique patients that have completed a survey',  count(*) from uniquepats";
    report.add(runSql(database, sqlUniqCompleted, siteInfo.getSiteId(),fromDate,toDate));

    // Completed surveys
    String sqlCompleted = "select 'Total number of completed surveys', count(*) from rpt_pain_std_surveys_square"
        + WHERE_CLAUSE + DT_RANGE + " and is_complete = 'Y'" + AND_NOTQST;
    report.add(runSql(database, sqlCompleted, siteInfo.getSiteId(), fromDate, toDate));

    String sqlCompInit = "select  ' --- Initial ', count(*) from rpt_pain_std_surveys_square"
        + WHERE_CLAUSE + DT_RANGE + " and is_complete = 'Y' and assessment_type like 'Initial%'";
    report.add(runSql(database, sqlCompInit, siteInfo.getSiteId(), fromDate, toDate));

    String sqlCompFoup = "select ' --- Follow up', count(*) from rpt_pain_std_surveys_square"
        + WHERE_CLAUSE + DT_RANGE + " and is_complete = 'Y' and assessment_type like 'Follow%'";
    report.add(runSql(database, sqlCompFoup, siteInfo.getSiteId(), fromDate, toDate));

    String sqlCompProc = "select  ' --- Procedure ', count(*) from rpt_pain_std_surveys_square"
        + WHERE_CLAUSE + DT_RANGE + " and is_complete = 'Y' and assessment_type like 'Proc%'";
    report.add(runSql(database, sqlCompProc, siteInfo.getSiteId(), fromDate, toDate));

    String sqlCompTreat = "select  ' --- Treatement ', count(*) from rpt_pain_std_surveys_square"
        + WHERE_CLAUSE + DT_RANGE + " and is_complete = 'Y' and assessment_type like '%Treatment%'";
    report.add(runSql(database, sqlCompTreat, siteInfo.getSiteId(), fromDate, toDate));

    String sqlCompAnger = "select  ' --- Anger Follow up ', count(*) from rpt_pain_std_surveys_square"
        + WHERE_CLAUSE + DT_RANGE + " and is_complete = 'Y' and assessment_type like 'AngerFollow'";
    report.add(runSql(database, sqlCompAnger, siteInfo.getSiteId(), fromDate, toDate));

    // Incomplete surveys
    String sqlIncomplete = "select 'Total number of Incomplete surveys', count(*) from rpt_pain_std_surveys_square"
        + WHERE_CLAUSE + DT_RANGE + " and is_complete = 'N' " + AND_NOTQST;
    report.add(runSql(database, sqlIncomplete, siteInfo.getSiteId(), fromDate, toDate));


    sqlIncomplete = "select  ' --- Initial', count(*) from rpt_pain_std_surveys_square"
        + WHERE_CLAUSE + DT_RANGE + " and is_complete = 'N' and assessment_type like 'Initial%'";
    report.add(runSql(database, sqlIncomplete, siteInfo.getSiteId(), fromDate, toDate));

    sqlIncomplete = "select ' --- Follow up ', count(*) from rpt_pain_std_surveys_square "
        + WHERE_CLAUSE + DT_RANGE + " and is_complete = 'N' and assessment_type like 'Follow%'";
    report.add(runSql(database, sqlIncomplete, siteInfo.getSiteId(), fromDate, toDate));

    sqlIncomplete = "select  ' --- Procedure ', count(*) from rpt_pain_std_surveys_square"
        + WHERE_CLAUSE + DT_RANGE + " and is_complete = 'N' and assessment_type like 'Proc%'";
    report.add(runSql(database, sqlIncomplete, siteInfo.getSiteId(), fromDate, toDate));

    sqlIncomplete= "select  ' --- Treatement ', count(*) from rpt_pain_std_surveys_square"
        + WHERE_CLAUSE + DT_RANGE + " and is_complete = 'N' and assessment_type like '%Treatment%'";
    report.add(runSql(database, sqlIncomplete, siteInfo.getSiteId(), fromDate, toDate));

    sqlIncomplete= "select  ' --- Anger Follow up ', count(*) from rpt_pain_std_surveys_square"
        + WHERE_CLAUSE + DT_RANGE + " and is_complete = 'N' and assessment_type like 'AngerFollow'";
    report.add(runSql(database, sqlIncomplete, siteInfo.getSiteId(), fromDate, toDate));
    return report;
  }

  private ArrayList<Object> runSql(Database database, String sql, Long siteId, Date fromDate, Date toDate) {
    return database.toSelect(sql)
        .argLong(":site", siteId)
        .argDate(fromDate)
        .argDate(toDate)
        .query(rs -> {
          ArrayList<Object> line = new ArrayList<>();
          while (rs.next()) {
            line.add(rs.getStringOrEmpty(1));
            line.add(rs.getLongOrZero(2));
          }
          return line;
        });
  }

  private String getStartOfDay(Database database) {
    if (database.get().flavor() == Flavor.oracle) {
      return("trunc(?)");
    }
    if (database.get().flavor() == Flavor.postgresql) {
      return ("date_trunc('day', ?)");
    }
    return "current_timestamp";
  }
  private String getStartOfDate(Database database) {
    if (database.get().flavor() == Flavor.oracle) {
      return("trunc(?)");
    }
    if (database.get().flavor() == Flavor.postgresql) {
      return ("date_trunc('day', ?)");
    }
    return " ? ";
  }

  private String getSqlDateRangeClause(Database database) {
    return " and survey_started >= "
        + getStartOfDate(database)
        + " and survey_started < "
        + getStartOfDay(database);
  }
}

