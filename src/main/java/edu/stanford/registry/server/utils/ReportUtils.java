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

package edu.stanford.registry.server.utils;

import edu.stanford.registry.server.ResultGeneratorIntf;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.reports.JsonReport;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.reports.TextReport;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.PatientResSurveyRegLinkList;
import edu.stanford.registry.shared.PatientResult;
import edu.stanford.registry.shared.PatientStudyExtendedData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseException;
import com.github.susom.database.Flavor;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlSelect;

public class ReportUtils {
  private static final Logger logger = Logger.getLogger(ReportUtils.class);

  final SiteInfo siteInfo;
  final Long siteId;
  public ReportUtils(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
  }

  public ArrayList<ArrayList<Object>> averageSurveyTimeReportByMonth(final Database database, Date fromDate, Date toDate) {
    StringBuilder sql = new StringBuilder();
    sql.append("with times as (");
    sql.append("select survey_token_id, round(sum(extract(second from (answer_time-question_time)))) time_seconds from survey_progress where survey_token_id in ( ");
    sql.append("select survey_token_id from survey_token where is_complete='Y' and survey_site_id= :site ");
    sql.append(" and survey_token in (select token from survey_registration  ");
    sql.append("where survey_dt >= :fromdt and survey_dt <= :todt and ");
    sql.append(" patient_id not in (select patient_id from patient where last_name = 'Test-Patient') ");
    sql.append(" and survey_site_id = :site )) group by survey_token_id)");
    sql.append(" select to_char(survey_dt, 'yyyy-mm') as survey_month,        ");
    if (database.get().flavor().equals(Flavor.oracle)) {
      sql.append(" trunc(avg(time_seconds)/60) || '-' || mod(round(avg(time_seconds)),60) ms, ");
      sql.append(" to_char(to_date(round(stddev(time_seconds)),'sssss'),'hh24-mi-ss') std_dev ");
      sql.append(" from times, survey_registration sr, survey_token st     ");
      sql.append(" where to_char(sr.token) = st.survey_token and st.survey_token_id = times.survey_token_id ");
    }
    if (database.get().flavor().equals(Flavor.postgresql)) {
      sql.append(" trunc(avg(time_seconds)/60) || '-' || mod( cast ( round(avg(time_seconds) ) as bigint) ,60) ms, ");
      sql.append(" TO_CHAR(CAST ((round(stddev(time_seconds)) || ' second') as interval), 'HH24-MI-SS') std_dev ");
      sql.append(" from times, survey_registration sr, survey_token st     ");
      sql.append(" where sr.token = st.survey_token and st.survey_token_id = times.survey_token_id ");
    }
    sql.append(" group by to_char(survey_dt, 'yyyy-mm') order by 1");
    return database.toSelect(sql.toString())
        .argLong(":site", siteId)
        .argDate("fromdt", DateUtils.getTimestampStart(siteInfo, fromDate))
        .argDate(":todt", DateUtils.getTimestampEnd(siteInfo, toDate))
        .query(new RowsHandler<ArrayList<ArrayList<Object>>>() {
          @Override
          public ArrayList<ArrayList<Object>> process(Rows rs) throws Exception {
            ArrayList<ArrayList<Object>> report = new ArrayList<>();
            while (rs.next()) {
              ArrayList<Object> line = new ArrayList<>();
              line.add(rs.getStringOrEmpty(1));
              line.add(rs.getStringOrEmpty(2).replaceAll("-", ":"));
              line.add(rs.getStringOrEmpty(3).replaceAll("-", ":"));
              report.add(line);
            }
            return report;
          }
        });
  }

  public ArrayList<ArrayList<Object>> averageSurveyTimeReportByType(final Database database, Date fromDate, Date toDate) {
    StringBuilder sql = new StringBuilder();
    sql.append("with times as (");
    sql.append("select survey_token_id, round(sum(extract(second from (answer_time-question_time)))) time_seconds from survey_progress where survey_token_id in ( ");
    sql.append("select survey_token_id from survey_token where is_complete='Y' and survey_site_id= :site ");
    sql.append(" and survey_token in (select token from survey_registration  ");
    sql.append("where survey_dt >= :fromdt and survey_dt <= :todt and ");
    sql.append(" patient_id not in (select patient_id from patient where last_name = 'Test-Patient') ");
    sql.append(" and survey_site_id = :site )) group by survey_token_id)");
    sql.append(" select survey_type,         ");
    if (database.get().flavor().equals(Flavor.oracle)) {
      sql.append(" trunc(avg(time_seconds)/60) || '-' || mod(round(avg(time_seconds)),60) ms, ");
      sql.append(" to_char(to_date(round(stddev(time_seconds)),'sssss'),'hh24-mi-ss') std_dev ");
      sql.append(" from times, survey_registration sr, survey_token st     ");
      sql.append(" where to_char(sr.token) = st.survey_token and st.survey_token_id = times.survey_token_id ");
    }
    if (database.get().flavor().equals(Flavor.postgresql)) {
      sql.append(" trunc(avg(time_seconds)/60) || '-' || mod( cast ( round(avg(time_seconds) ) as bigint) ,60) ms, ");
      sql.append(" TO_CHAR(CAST ((round(stddev(time_seconds)) || ' second') as interval), 'HH24-MI-SS') std_dev ");
      sql.append(" from times, survey_registration sr, survey_token st     ");
      sql.append(" where sr.token = st.survey_token and st.survey_token_id = times.survey_token_id ");
    }
    sql.append(" group by survey_type");


    return database.toSelect(sql.toString())
        .argLong(":site", siteId)
        .argDate(":fromdt", DateUtils.getTimestampStart(siteInfo, fromDate))
        .argDate(":todt", DateUtils.getTimestampEnd(siteInfo, toDate))
        .query(new RowsHandler<ArrayList<ArrayList<Object>>>() {
          @Override
          public ArrayList<ArrayList<Object>> process(Rows rs) throws Exception {
            ArrayList<ArrayList<Object>> report = new ArrayList<>();
            while (rs.next()) {
              ArrayList<Object> line = new ArrayList<>();
              line.add(rs.getStringOrEmpty(1));
              line.add(rs.getStringOrEmpty(2).replaceAll("-", ":"));
              line.add(rs.getStringOrEmpty(3).replaceAll("-", ":"));
              report.add(line);
            }
            return report;
          }
        });
  }

  public ArrayList<ArrayList<Object>> averageSurveyTimeReport(final Database database, Date fromDate, Date toDate,
                                                              Long siteId) {
    StringBuilder sql = new StringBuilder();
    sql.append("with times as (");
    sql.append("select survey_token_id, round(sum(extract(second from (answer_time-question_time)))) time_seconds from survey_progress where survey_token_id in ( ");
    sql.append("select survey_token_id from survey_token where is_complete='Y' and survey_site_id= :site ");
    sql.append(" and survey_token in (select token from survey_registration  ");
    sql.append("where survey_dt >= :fromdt and survey_dt <= :todt and ");
    sql.append(" patient_id not in (select patient_id from patient where last_name = 'Test-Patient') ");
    sql.append(" and survey_site_id = :site )) group by survey_token_id)");
    if (database.get().flavor().equals(Flavor.oracle)) {
      sql.append(" select trunc(avg(time_seconds)/60) || '-' || mod(round(avg(time_seconds)),60) ms, ");
      sql.append(" to_char(to_date(round(stddev(time_seconds)),'sssss'),'hh24-mi-ss') std_dev ");
      sql.append(" from times, survey_registration sr, survey_token st     ");
      sql.append(" where to_char(sr.token) = st.survey_token and st.survey_token_id = times.survey_token_id ");
    }
    if (database.get().flavor().equals(Flavor.postgresql)) {
      sql.append(" select trunc(avg(time_seconds)/60) || '-' || mod( cast ( round(avg(time_seconds) ) as bigint) ,60) ms, ");
      sql.append(" TO_CHAR(CAST ((round(stddev(time_seconds)) || ' second') as interval), 'HH24-MI-SS') std_dev ");
      sql.append(" from times, survey_registration sr, survey_token st     ");
      sql.append(" where  sr.token  = st.survey_token and st.survey_token_id = times.survey_token_id ");
    }
    return database.toSelect(sql.toString())
        .argLong(":site", siteId)
        .argDate(":fromdt", DateUtils.getTimestampStart(siteInfo, fromDate))
        .argDate(":todt", DateUtils.getTimestampEnd(siteInfo, toDate))
        .query(new RowsHandler<ArrayList<ArrayList<Object>>>() {
          @Override
          public ArrayList<ArrayList<Object>> process(Rows rs) throws Exception {
            ArrayList<ArrayList<Object>> report = new ArrayList<>();
            while (rs.next()) {
              ArrayList<Object> line = new ArrayList<>();
              line.add(rs.getStringOrEmpty(1).replaceAll("-", ":"));
              line.add(rs.getStringOrEmpty(2).replaceAll("-", ":"));
              report.add(line);
            }
            return report;
          }
        });
  }

  /**
   * Format a duration in milliseconds as hh:mm:ss, omitting the hh: portion if it is zero.
   */
  @SuppressWarnings("unused")
  private String formatMillisAsStopWatch(int milliseconds) {
    int sec = (milliseconds / 1000) % 60;
    int min = ((milliseconds / (1000 * 60)) % 60);
    int hrs = ((milliseconds / (1000 * 60 * 60)) % 24);
    StringBuilder str = new StringBuilder();
    if (hrs > 0) {
      str.append(hrs).append(":");
    }
    str.append(min).append(":").append(sec);
    return str.toString();
  }

  public ArrayList<ArrayList<Object>> complianceSummaryReport(final Database database, Date fromDate, Date toDate) {

    String sql = "SELECT status, number_of_patients from ( "
        + " with patient_status as ( "
        + " select patient_id, "
        + " sum(CASE WHEN activity_type = 'Declined' THEN 1 ELSE 0 END) as declined, "
        + " sum(CASE WHEN activity_type = 'Consented' THEN 1 ELSE 0 END) as consented, "
        + " sum(CASE WHEN activity_type = 'Completed' THEN 1 ELSE 0 END) as completed   "
        + " from activity where patient_id not in (select patient_id from patient where last_name = 'Test-Patient')  "
        + " and activity.survey_site_id = :site and activity_dt between ? AND ? "
        + "group by patient_id) "
        + " select 'Declined' as status, count(1) as number_of_patients, 1 as order_by from patient_status where declined > 0 "
        + " union " + " select 'Consented', count(1), 2 from patient_status where consented > 0 " + " union "
        + " select 'Completed Survey', count(1), 3 from patient_status where completed > 0 ) ";
    if (database.get().flavor().equals(Flavor.postgresql)) {
      sql = sql + "AS csr ";
    }
    sql = sql + "order by order_by ";

    return database.toSelect(sql)
        .argLong(":site", siteId)
        .argDate(DateUtils.getTimestampStart(siteInfo, fromDate))
        .argDate(DateUtils.getTimestampEnd(siteInfo, toDate))
        .query(new RowsHandler<ArrayList<ArrayList<Object>>>() {
          @Override
          public ArrayList<ArrayList<Object>> process(Rows rs) throws Exception {
            ArrayList<ArrayList<Object>> report = new ArrayList<>();
            while (rs.next()) {
              ArrayList<Object> line = new ArrayList<>();
              line.add(rs.getStringOrEmpty(1));
              line.add(rs.getLongOrZero(2));
              report.add(line);
            }
            return report;
          }
        });
  }

  public ArrayList<ArrayList<Object>> complianceReport1(Database database) {
    String sql =
        " SELECT to_char(sr.visit_dt, 'mm') as survey_month,  count(*), sr.visit_type, coalesce(sru.participates, ' ') "
            + " FROM appt_registration sr,  "
            + "   ( select min(visit_dt) visit_dt, appt_registration.patient_id, data_value as participates "
            + "     from appt_registration "
            + "     left outer join patient_attribute "
            + "     on appt_registration.patient_id = patient_attribute.patient_id "
            + "     and appt_registration.survey_site_id = patient_attribute.survey_site_id "
            + "     and patient_attribute.data_name = 'participatesInSurveys' "
            + "     where appt_registration.survey_site_id = :site and registration_type in ('a') "
            + "     and appt_registration.patient_id not in (select patient_id from patient where last_name = 'Test-Patient')"
            // --and visit_type in ('NPV60') --, 'NPV75', 'NHA', 'TMJN')
            + "     group by appt_registration.patient_id, to_char(visit_dt,'mm'), data_value  ) AS sru "
            + " WHERE  sr.survey_site_id = :site and sr.patient_id = sru.patient_id and sr.visit_dt = sru.visit_dt  "
            + "   AND sr.visit_dt > to_date((select '01' || TO_CHAR(ADD_MONTHS(SYSDATE, -12), 'MONyyyy') || ' 00:00' from dual), 'ddMONyyyy hh24:mi') "
            + "   AND sr.visit_dt < sysdate "
            + " GROUP BY to_char(sr.visit_dt,'mm'), sr.visit_type, sru.participates "
            + " ORDER BY survey_month, sr.visit_type, participates ";

    return database.toSelect(sql)
        .argLong(":site", siteId)
        .query(new RowsHandler<ArrayList<ArrayList<Object>>>() {
          @Override
          public ArrayList<ArrayList<Object>> process(Rows rs) throws Exception {
            ArrayList<ArrayList<Object>> report = new ArrayList<>();
            while (rs.next()) {
              ArrayList<Object> line = new ArrayList<>();
              line.add(rs.getStringOrEmpty(1));
              line.add(rs.getLongOrZero(2));
              line.add(rs.getStringOrEmpty(3));
              line.add(rs.getStringOrEmpty(4));
              report.add(line);
            }
            return report;
          }
        });
  }

  public ArrayList<ArrayList<Object>> complianceReport2(Database database, boolean onlyNew, Date startDt, Date endDt) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT survey_date, count(*),  participates FROM ( ");
    sql.append(" SELECT appt_registration.patient_id, to_char(appt_registration.visit_dt,'yyyy-mm') as survey_date, ");
    sql.append(" 			coalesce(to_char(patient_attribute.dt_created,'yyyy-mm') ,'9999-99'), ");
    sql.append(" 			CASE when coalesce(to_char(patient_attribute.dt_created,'yyyy-mm') ,'9999-99')");
    sql.append(" 			<= to_char(appt_registration.visit_dt,'yyyy-mm') ");
    sql.append(" 			then data_value else null END as participates ");
    sql.append(" FROM appt_registration ");
    sql.append("      LEFT OUTER JOIN assessment_registration ");
    sql.append("      on assessment_registration.assessment_reg_id = appt_registration.assessment_reg_id ");
    sql.append(" 			LEFT OUTER JOIN patient_attribute ");
    sql.append(" 			on appt_registration.patient_id = patient_attribute.patient_id ");
    sql.append("      and appt_registration.survey_site_id = patient_attribute.survey_site_id ");
    sql.append(" 			and patient_attribute.data_name = 'participatesInSurveys'");
    sql.append(" WHERE appt_registration.survey_site_id = :site and appt_registration.registration_type in ('a','s') ");
    sql.append(" AND assessment_registration.assessment_type not in ('Patient Consented', 'Patient Declined') ");
    sql.append(" AND appt_registration.patient_id not in (select patient_id from patient where last_name = 'Test-Patient') ");
    if (onlyNew) {
      List<String> newVisitTypes = XMLFileUtils.getInstance(siteInfo).getVisitTypesInGroup("new");
      String visitList = "";
      for(String visitType : newVisitTypes) {
        visitList += (visitList.length() > 0) ? "," : "";
        visitList += "'" + visitType + "'";
      }
      sql.append(" AND visit_type in (" + visitList + ") ");
    }
    sql.append(" AND appt_registration.visit_dt >= ? ");
    sql.append(" AND appt_registration.visit_dt <= ? ");
    sql.append(" GROUP BY appt_registration.patient_id, to_char(visit_dt,'yyyy-mm'), ");
    sql.append(" data_value, to_char(patient_attribute.dt_created,'yyyy-mm')  )");

    if (database.get().flavor().equals(Flavor.postgresql)) {
      sql.append(" AS cr");
    }

    sql.append(" GROUP BY survey_date, participates ");
    sql.append(" ORDER BY survey_date, participates");

    return database.toSelect(sql.toString())
        .argLong(":site", siteId)
        .argDate(DateUtils.getTimestampStart(siteInfo, startDt))
        .argDate(DateUtils.getTimestampEnd(siteInfo, endDt)).query(new RowsHandler<ArrayList<ArrayList<Object>>>() {
          @Override
          public ArrayList<ArrayList<Object>> process(Rows rs) throws Exception {
            ArrayList<ArrayList<Object>> report = new ArrayList<>();
            while (rs.next()) {
              ArrayList<Object> line = new ArrayList<>();
              line.add(rs.getStringOrEmpty(1));
              line.add(rs.getLongOrZero(2));
              line.add(rs.getStringOrEmpty(3));
              report.add(line);
            }
            return report;
          }
        });
  }

  public ArrayList<ArrayList<Object>> eligibleVisitsReport(final Database database, Date fromDate, Date toDate) {
    String truncatedRegistered = "trunc(dt_created) registered ";
    String truncatedSurveyDt = "trunc(sr.survey_dt) survey_dt ";
    String truncatedSurveyDate = "trunc(survey_dt) survey_date ";
    if (database.get().flavor().equals(Flavor.postgresql)) {
      truncatedRegistered = "date_trunc('day', dt_created) registered ";
      truncatedSurveyDt = "date_trunc('day', sr.survey_dt) survey_dt ";
      truncatedSurveyDate = "date_trunc('day', survey_dt) survey_date ";
    }
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT mnth, sum(possible) possible, sum(completed) completed, ");
    sql.append("       sum(uncompleted) uncompleted, sum(unstarted) unstarted from (with ");
    sql.append("date_registered as ");
    sql.append("  ( select patient_id, ").append(truncatedRegistered).append(" ");
    sql.append("  from patient_attribute where survey_site_id = :site and data_name='participatesInSurveys' and data_value='y'  ), ");
    sql.append("surveys as ");
    sql.append("  ( select sr.survey_site_id, sr.patient_id, ").append(truncatedSurveyDt).append(", sr.token from survey_registration sr ");
    sql.append("    where survey_site_id = :site and survey_type not in ('Patient Declined', 'Patient Consented') ");
    sql.append("    and sr.patient_id not in (select patient_id from patient where last_name = 'Test-Patient') ), ");
    sql.append("opportunities as ");
    sql.append("  ( select distinct sr.patient_id, ").append(truncatedSurveyDate).append(", sr.token ");
    sql.append("    from surveys sr join date_registered de on (sr.patient_id=de.patient_id) ");
    sql.append("    join patient_study ps on (sr.survey_site_id = ps.survey_site_id and sr.token = ps.token) ");
    sql.append("    where sr.survey_dt >= de.registered ), ");
    sql.append("completed as ");
    sql.append("  ( select distinct sr.patient_id, ").append(truncatedSurveyDate).append(", sr.token from surveys sr ");
    sql.append("    join activity act on (sr.survey_site_id = act.survey_site_id and sr.token = act.token) ");
    sql.append("    where act.activity_type = 'Completed' ), ");
    sql.append("uncompleted as ");
    sql.append("  ( select distinct sr.patient_id, ").append(truncatedSurveyDate).append(", sr.token from surveys sr ");
    sql.append("    join activity act on (sr.survey_site_id = act.survey_site_id and sr.token = act.token) ");
    sql.append("    where act.activity_type in ('Sent Response') and not exists ");
    sql.append("    (select * from activity act2 where act2.survey_site_id=act.survey_site_id and act2.token=act.token and activity_type='Completed') ), ");
    sql.append("nonstarted as ");
    sql.append("  ( select distinct sr.patient_id, ").append(truncatedSurveyDate).append(", sr.token ");
    sql.append("    from surveys sr join date_registered de on (sr.patient_id=de.patient_id) ");
    sql.append("    where sr.survey_dt >= de.registered ");
    sql.append("    and exists (select * from patient_study ps where ps.survey_site_id=sr.survey_site_id and ps.token=sr.token) ");
    sql.append("    and not exists (select * from activity act where act.survey_site_id=sr.survey_site_id and act.token=sr.token ");
    sql.append("    and activity_type in ('Completed','Validated', 'Sent Response')) ) ");
    sql.append(" SELECT to_char(o.survey_date, 'yyyy-mm') as mnth, o.survey_date, count(o.patient_id) possible, ");
    sql.append(" sum(case when c.patient_id is null then 0 else 1 end) completed, ");
    sql.append(" sum(case when u.patient_id is null then 0 else 1 end) uncompleted,");
    sql.append(" sum(case when n.patient_id is null then 0 else 1 end) unstarted");
    sql.append(" FROM opportunities o ");
    sql.append("   left join completed c on (o.survey_date = c.survey_date and o.patient_id = c.patient_id and o.token = c.token)");
    sql.append("   left join uncompleted u on (o.survey_date = u.survey_date and o.patient_id = u.patient_id and o.token = u.token)");
    sql.append("   left join nonstarted n on (o.survey_date = n.survey_date and o.patient_id = n.patient_id and o.token = n.token)");
    //sql.append(" GROUP BY o.survey_date ORDER BY o.survey_date) where survey_date >= trunc(?) and survey_date <= trunc(?)");
    sql.append(" GROUP BY o.survey_date ORDER BY o.survey_date)");
    if (database.get().flavor().equals(Flavor.postgresql)) {
      sql.append(" AS evr");
    }

    if (database.get().flavor().equals(Flavor.oracle)) {
      sql.append("	WHERE survey_date >= trunc(?) AND survey_date <= trunc(?)");
    } else if (database.get().flavor().equals(Flavor.postgresql)) {
      sql.append("	WHERE survey_date >= date_trunc('day', CAST(? as timestamp without time zone)) AND survey_date <= date_trunc('day', CAST(? as timestamp without time zone))");
    }
    sql.append(" group by mnth order by mnth");

    return database.toSelect(sql.toString())
        .argLong(":site", siteId)
        .argDate(fromDate)
        .argDate(toDate)
        .query(new RowsHandler<ArrayList<ArrayList<Object>>>() {
          @Override
          public ArrayList<ArrayList<Object>> process(Rows rs) throws Exception {
            ArrayList<ArrayList<Object>> report = new ArrayList<>();
            while (rs.next()) {
              ArrayList<Object> line = new ArrayList<>();
              line.add(rs.getStringOrEmpty(1));
              line.add(rs.getLongOrZero(2));
              line.add(rs.getLongOrZero(3));
              line.add(rs.getLongOrZero(4));
              line.add(rs.getLongOrZero(5));
              report.add(line);
            }
            return report;
          }
        });
  }

  public ArrayList<ArrayList<Object>> inEligibleVisitsReport(final Database database, Date fromDate, Date toDate) {
    String truncatedRegistered = "trunc(dt_created) registered ";
    String truncatedSurveyDt = "trunc(sr.survey_dt) survey_dt ";
    String truncatedSurveyDate = "trunc(survey_dt) survey_date ";
    if (database.get().flavor().equals(Flavor.postgresql)) {
      truncatedRegistered = "date_trunc('day', dt_created) registered ";
      truncatedSurveyDt = "date_trunc('day', sr.survey_dt) survey_dt ";
      truncatedSurveyDate = "date_trunc('day', survey_dt) survey_date ";
    }
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT mnth, sum(not_eligible) not_eligible, sum(not_asked) not_asked, ");
    sql.append("             sum(declined) declined, sum(suppressed) suppressed ");
    sql.append("FROM  ( with ");
    sql.append(" date_registered as ");
    sql.append(" ( select patient_id, ").append(truncatedRegistered).append(" ");
    sql.append("     from patient_attribute where survey_site_id = :site and data_name='participatesInSurveys' ), ");
    sql.append("date_declined as ");
    sql.append("( select patient_id, ").append(truncatedRegistered).append(" ");
    sql.append("    from patient_attribute where survey_site_id = :site and data_name='participatesInSurveys' and data_value='n'  ), ");
    sql.append("date_agreed as ");
    sql.append("( select patient_id, ").append(truncatedRegistered).append(" ");
    sql.append("    from patient_attribute where survey_site_id = :site and data_name='participatesInSurveys' and data_value='y'  ),");
    sql.append("surveys as ");
    sql.append("( select sr.patient_id, ").append(truncatedSurveyDt).append(", sr.token from survey_registration sr ");
    sql.append("   where survey_site_id = :site and  survey_type not in ('Patient Declined', 'Patient Consented')");
    sql.append("    and sr.patient_id not in (select patient_id from patient where last_name = 'Test-Patient') ), ");
    sql.append("not_asked as ");
    sql.append("( select distinct sr.patient_id, ").append(truncatedSurveyDate).append(" ");
    sql.append("    from surveys sr ");
    sql.append("    left outer join date_registered de on ");
    sql.append("    (sr.patient_id=de.patient_id and sr.survey_dt >= de.registered) ");
    sql.append("   where registered is null ), ");
    sql.append("declined as ");
    sql.append("( select distinct sr.patient_id, ").append(truncatedSurveyDate).append(" ");
    sql.append("    from surveys sr join date_declined dd on (sr.patient_id=dd.patient_id) ");
    sql.append("   where sr.survey_dt >= dd.registered), ");
    sql.append("suppressed as ");
    sql.append("( select distinct sr.patient_id, ").append(truncatedSurveyDate).append(" ");
    sql.append("    from surveys sr join date_agreed da on (sr.patient_id = da.patient_id) ");
    sql.append("   where sr.survey_dt >= da.registered ");
    sql.append("and not exists ");
    sql.append("        (select * from patient_study ps ");
    sql.append("          where sr.patient_id = ps.patient_id and sr.token = ps.token and ps.survey_site_id = :site) ), ");
    sql.append("not_eligible as ");
    sql.append("( select patient_id, survey_date from not_asked union ");
    sql.append("  select patient_id, survey_date from declined union ");
    sql.append("  select patient_id, survey_date from suppressed) ");
    sql.append("  select to_char(ne.survey_date, 'yyyy-mm') as mnth, ");
    sql.append("         ne.survey_date, count(ne.patient_id) not_eligible, ");
    sql.append("         sum(case when na.patient_id is null then 0 else 1 end) not_asked,  ");
    sql.append("         sum(case when de.patient_id is null then 0 else 1 end) declined, ");
    sql.append("         sum(case when su.patient_id is null then 0 else 1 end) suppressed ");
    sql.append("    from not_eligible ne ");
    sql.append("    left join not_asked na on ");
    sql.append("         (ne.survey_date = na.survey_date and ne.patient_id = na.patient_id) ");
    sql.append("    left join declined de on (ne.survey_date = de.survey_date ");
    sql.append("          and ne.patient_id = de.patient_id) ");
    sql.append("    left join suppressed su on (ne.survey_date = su.survey_date ");
    sql.append("          and ne.patient_id = su.patient_id) ");
    sql.append("   group by ne.survey_date ");
    sql.append("   order by ne.survey_date ");
    //sql.append(") WHERE survey_date >= trunc(?) ");
    //sql.append("    AND survey_date <= trunc(?) ");
    sql.append(") ");
    if (database.get().flavor().equals(Flavor.postgresql)) {
      sql.append(" AS iver");
    }
    if (database.get().flavor().equals(Flavor.oracle)) {
      sql.append("	WHERE survey_date >= trunc(?) AND survey_date <= trunc(?)");
    } else if (database.get().flavor().equals(Flavor.postgresql)) {
      sql.append("	WHERE survey_date >= date_trunc('day', CAST(? as timestamp without time zone)) AND survey_date <= date_trunc('day', CAST(? as timestamp without time zone))");
    }
    sql.append("GROUP BY mnth order by mnth");

    return database.toSelect(sql.toString())
        .argLong(":site", siteId)
        .argDate(fromDate)
        .argDate(toDate).query(new RowsHandler<ArrayList<ArrayList<Object>>>() {
          @Override
          public ArrayList<ArrayList<Object>> process(Rows rs) throws Exception {
            ArrayList<ArrayList<Object>> report = new ArrayList<>();
            while (rs.next()) {
              ArrayList<Object> line = new ArrayList<>();
              line.add(rs.getStringOrEmpty(1));
              line.add(rs.getLongOrZero(2));
              line.add(rs.getLongOrZero(3));
              line.add(rs.getLongOrZero(4));
              line.add(rs.getLongOrZero(5));
              report.add(line);
            }
            return report;
          }
        });
  }

  public  ArrayList<ArrayList<Object>> patientSurveysReport(final Database database, Date fromDate, Date toDate) {
    Date fromTime = DateUtils.getTimestampStart(siteInfo, fromDate);
    Date toTime = DateUtils.getTimestampEnd(siteInfo, toDate);

    StringBuilder sql = new StringBuilder();
    sql.append("select sr.patient_id, p.first_name, p.last_name, ar.email_addr,  ");
    sql.append(dateSql("sr.survey_dt", database.get().flavor())).append(" survey_dt,");
    sql.append(" (select ").append(dateSql("min(question_time)", database.get().flavor()));
    sql.append(" from survey_progress sp, survey_token st where sp.survey_site_id = st.survey_site_id and ");
    sql.append(" sp.survey_token_id = st.survey_token_id and st.survey_site_id = sr.survey_site_id and st.survey_token = sr.token) as started, ");
    sql.append(" (select ").append(dateSql("max(question_time)", database.get().flavor()));
    sql.append(" from survey_progress sp, survey_token st where sp.survey_site_id = st.survey_site_id and ");
    sql.append(" sp.survey_token_id = st.survey_token_id and st.survey_site_id = sr.survey_site_id and st.survey_token = sr.token ");
    sql.append(" and st.is_complete = 'Y') as finished from survey_registration sr , patient p, assessment_registration ar, patient_attribute pa ");
    sql.append(" where sr.survey_site_id = :site and sr.survey_site_id = ar.survey_site_id and sr.assessment_reg_id = ar.assessment_reg_id ");
    sql.append(" and pa.patient_id = p.patient_id and pa.survey_site_id = sr.survey_site_id and pa.data_name ='participatesInSurveys' and upper(pa.data_value) = 'Y'");
    sql.append(" and sr.patient_id = p.patient_id and sr.survey_dt between ? and ? order by 5, 3, 2 ");
    return database.toSelect(sql.toString())
        .argLong(":site", siteId)
        .argDate(fromTime)
        .argDate(toTime).query(new RowsHandler<ArrayList<ArrayList<Object>>>() {
          @Override
          public ArrayList<ArrayList<Object>> process(Rows rs) throws Exception {
            int totalNumberSurveys = 0;
            int numberStarted = 0;
            int numberCompleted = 0;
            ArrayList<ArrayList<Object>> report = new ArrayList<>();
            while (rs.next()) {
              ArrayList<Object> line = new ArrayList<>();
              line.add(rs.getStringOrEmpty(1));
              line.add(rs.getStringOrEmpty(2));
              line.add(rs.getStringOrEmpty(3));
              line.add(rs.getStringOrEmpty(4));
              line.add(siteInfo.getDateOnlyFormatter().getDateString(rs.getDateOrNull(5)));
              Date started = rs.getDateOrNull(6);
              if (started != null) {
                line.add(siteInfo.getDateOnlyFormatter().getDateString(started));
                numberStarted++;
              } else {
                line.add("");
              }
              Date completed = rs.getDateOrNull(7);
              if (completed != null) {
                line.add(siteInfo.getDateOnlyFormatter().getDateString(completed));
                numberCompleted++;
              } else {
                line.add("");
              }
              report.add(line);
              totalNumberSurveys++;
            }
            report.add(summaryLine("Total # surveys", totalNumberSurveys));
            report.add(summaryLine("Started", numberStarted));
            report.add(summaryLine("Completed", numberCompleted));
            return report;
          }
        });
  }

  public ArrayList<ArrayList<Object>> standardReport(final Database database, String report, Date fromDate, Date toDate) {
    Date fromTime = DateUtils.getTimestampStart(siteInfo, fromDate);
    Date toTime = DateUtils.getTimestampEnd(siteInfo, toDate);

    if ("IRBCounts".equals(report)) {
      return IRBCountsReport(database, fromTime, toTime);
    }
    return null;
  }

  private ArrayList<ArrayList<Object>> IRBCountsReport(final Database database, Date fromTime, Date toTime) {
    final ArrayList<ArrayList<Object>> report = new ArrayList<>();

    // Add the report header line
    ArrayList<Object> headingLine = new ArrayList<>();
    headingLine.add("#");
    headingLine.add("Category");
    report.add(headingLine);

    // Check if the site has a consent parameter that should be checked
    String consentAttr = siteInfo.getRegistryCustomizer().IRBCountsConsentAttribute();
    String consentString = "";
    if (consentAttr != null && consentAttr.length() > 0) {
      consentString = " and exists (select * from patient_attribute_history pah "
          + " where pa.patient_id = pah.patient_id and pa.survey_site_id = pah.survey_site_id "
          + " and pah.data_name = ? and pah.data_value = 'Y' and pah.dt_created between ? and ?) ";
    }

    // Get the total # of patients enrolled during the time frame
    String sqlEnrolled = "select count(*) from patient_attribute pa where pa.survey_site_id = :site and "
        + " pa.data_name = 'participatesInSurveys' and pa.dt_created between ? and ? "
        + consentString;
    SqlSelect selectEnrolled = database.toSelect(sqlEnrolled)
        .argLong(":site", siteId)
        .argDate(fromTime)
        .argDate(toTime);
    if (consentString.length() > 0) {
      selectEnrolled = selectEnrolled.argString(consentAttr).argDate(fromTime).argDate(toTime);
    }
    report.add(selectEnrolled.query(rs -> {
          ArrayList<Object> line = new ArrayList<>();
          if (rs.next()) {
            line.add(String.valueOf(rs.getIntegerOrZero()));
            line.add("Patients agreed to participate during the period");
          }
          return line;
        })
    );

    // Get the # of patients enrolled by gender
    String sqlGender = "select count(*)  from patient_attribute pa, patient_attribute pa2 "
        + " where pa.survey_site_id = :site and pa.patient_id = pa2.patient_id "
        + " and pa.survey_site_id  = pa2.survey_site_id "
        + " and pa.data_name = 'participatesInSurveys' and pa.dt_created between ? and ? "
        + " and pa2.data_name = 'gender' and pa2.data_value = ? "
        + consentString;
    // Males
    SqlSelect selectMale = database.toSelect(sqlGender)
        .argLong(":site", siteId)
        .argDate(fromTime)
        .argDate(toTime)
        .argString("Male");
    if (consentString.length() > 0) {
      selectMale = selectMale.argString(consentAttr).argDate(fromTime).argDate(toTime);
    }
    report.add(selectMale
        .query(rs -> {
          ArrayList<Object> line = new ArrayList<>();
          if (rs.next()) {
            line.add(String.valueOf(rs.getIntegerOrZero()));
            line.add("Gender is Male");
          }
          return line;
        })
    );
    // Females
    SqlSelect selectFemale = database.toSelect(sqlGender)
        .argLong(":site", siteId)
        .argDate(fromTime)
        .argDate(toTime)
        .argString("Female");
    if (consentString.length() > 0) {
      selectFemale = selectFemale.argString(consentAttr).argDate(fromTime).argDate(toTime);
    }
    report.add(selectFemale
        .query(rs -> {
          ArrayList<Object> line = new ArrayList<>();
          if (rs.next()) {
            line.add(String.valueOf(rs.getIntegerOrZero()));
            line.add("Gender is Female");
          }
          return line;
        })
    );

    // Get the # that have no gender defined
    String sqlNoAttribute = "select count(*) from patient_attribute pa "
        + " where pa.survey_site_id = :site and pa.data_name = 'participatesInSurveys' "
        + "    and pa.dt_created between ? and ? and not exists "
        + " (select * from patient_attribute pa2 where pa.patient_id = pa2.patient_id  "
        + "     and pa.survey_site_id  = pa2.survey_site_id and pa2.data_name = ?)"
        + consentString;
    SqlSelect selectNoGender = database.toSelect(sqlNoAttribute)
        .argLong(":site", siteId)
        .argDate(fromTime)
        .argDate(toTime)
        .argString("gender");
    if (consentString.length() > 0) {
      selectNoGender = selectNoGender.argString(consentAttr).argDate(fromTime).argDate(toTime);
    }
    report.add(selectNoGender
        .query(rs -> {
          ArrayList<Object> line = new ArrayList<>();
          if (rs.next()) {
            line.add(String.valueOf(rs.getIntegerOrZero()));
            line.add("Gender is undefined");
          }
          return line;
        })
    );

    // Get the counts for each ethnicity
    String sqlEthnicity = "select count(*), pa2.data_value from patient_attribute pa, patient_attribute pa2 "
        + " where pa.survey_site_id = :site and pa.patient_id = pa2.patient_id "
        + " and pa.survey_site_id  = pa2.survey_site_id and pa2.data_name = 'ethnicity' "
        + " and pa.data_name = 'participatesInSurveys' and pa.dt_created between ? and ? "
        + consentString + " group by pa2.data_value order by 2 ";
    SqlSelect selectEthnicity = database.toSelect(sqlEthnicity)
        .argLong(":site", siteId)
        .argDate(fromTime)
        .argDate(toTime);
    if (consentString.length() > 0) {
      selectEthnicity = selectEthnicity.argString(consentAttr).argDate(fromTime).argDate(toTime);
    }
    selectEthnicity
        .query(rs -> {
          while (rs.next()) {
            ArrayList<Object> line = new ArrayList<>();
            line.add(String.valueOf(rs.getIntegerOrZero()));
            line.add("Ethnicity is " +rs.getStringOrEmpty());
            report.add(line);
          }
          return null;
        });
    // Get the # that have no ethnicity defined
    SqlSelect selectNoEthnicity = database.toSelect(sqlNoAttribute)
        .argLong(":site", siteId)
        .argDate(fromTime)
        .argDate(toTime)
        .argString("ethnicity");
    if (consentString.length() > 0) {
      selectNoEthnicity = selectNoEthnicity.argString(consentAttr).argDate(fromTime).argDate(toTime);
    }
    report.add(selectNoEthnicity
        .query(rs -> {
          ArrayList<Object> line = new ArrayList<>();
          if (rs.next()) {
            line.add(String.valueOf(rs.getIntegerOrZero()));
            line.add("Ethnicity is undefined");
          }
          return line;
        })
    );
    // Get the # of patients under 18 at the end of the period
    String sqlMinor = " select count(*) from patient p join patient_attribute pa on pa.patient_id = p.patient_id "
        + " where pa.survey_site_id = :site and pa.data_name = 'participatesInSurveys'  "
        + " and pa.dt_created between ? and ? "
        + " and p.dt_birth >= ? " + consentString;
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(toTime);
    calendar.add(Calendar.YEAR, -18);
    SqlSelect selectMinor = database.toSelect(sqlMinor)
        .argLong(":site", siteId)
        .argDate(fromTime)
        .argDate(toTime)
        .argDate(calendar.getTime());
    if (consentString.length() > 0) {
      selectMinor = selectMinor.argString(consentAttr).argDate(fromTime).argDate(toTime);
    }
    report.add(selectMinor
        .query(rs -> {
          ArrayList<Object> line = new ArrayList<>();
          if (rs.next()) {
            line.add(String.valueOf(rs.getIntegerOrZero()));
            line.add("Under 18 ");
          }
          return line;
        })
    );
    // Get the # of patients who declined to participate during the time frame
    StringBuilder sqlDeclinedPart = new StringBuilder();
    sqlDeclinedPart.append("select count(*) from patient_attribute pa "
        + "where pa.data_value = 'n' and pa.data_name = 'participatesInSurveys' and pa.survey_site_id = :site "
        + " and exists (select * from activity a where a.patient_id = pa.patient_id "
        + "             and a.survey_site_id = pa.survey_site_id and activity_type = 'Declined'"
        + "             and a.dt_created between ? and ? ) "
        + " and not exists (select * from survey_registration sr, survey_token st where sr.patient_id = pa.patient_id "
        + "                 and sr.survey_site_id = pa.survey_site_id and sr.token = st.survey_token ) ");
    if (consentAttr != null && consentAttr.length() > 0) {
      sqlDeclinedPart.append(" and not exists (select * from patient_attribute_history pah "
          + " where pa.patient_id = pah.patient_id and pa.survey_site_id = pah.survey_site_id "
          + " and pah.data_name = ? and pah.data_value = 'Y') ");
    }
    SqlSelect selectDeclinedPart = database.toSelect(sqlDeclinedPart.toString())
        .argLong(":site", siteId)
        .argDate(fromTime)
        .argDate(toTime);
    if (consentAttr != null && consentAttr.length() > 0) {
      selectDeclinedPart = selectDeclinedPart.argString(consentAttr);
    }
    report.add(selectDeclinedPart.query(rs -> {
          ArrayList<Object> line = new ArrayList<>();
          if (rs.next()) {
            line.add(String.valueOf(rs.getIntegerOrZero()));
            line.add("Declined to participate during the period");
          }
          return line;
        })
    );
    // Get the # of patients who declined to continue during the time frame
    StringBuilder sqlDeclinedCont = new StringBuilder();
    sqlDeclinedCont.append("select count(*) from patient_attribute pa "
        + "where pa.data_value = 'n' and pa.data_name = 'participatesInSurveys' and pa.survey_site_id = :site "
        + "  and pa.dt_changed is not null and pa.dt_changed between ? and ?"
        + "  and exists (select * from patient_attribute_history pah "
        + "            where pah.data_value = 'y' and pah.data_name = 'participatesInSurveys' "
        + "            and pah.patient_id = pa.patient_id and pah.survey_site_id = pa.survey_site_id ) ");
    if (consentAttr != null && consentAttr.length() > 0) {
      sqlDeclinedCont.append(
          "  and exists (select * from patient_attribute_history pah where pah.data_name = ? and pah.data_value = 'Y' "
          + "             and pah.patient_id = pa.patient_id and pah.survey_site_id = pa.survey_site_id)");
    }
    SqlSelect selectDeclinedCont = database.toSelect(sqlDeclinedCont.toString())
        .argLong(":site", siteId)
        .argDate(fromTime)
        .argDate(toTime);
    if (consentAttr != null && consentAttr.length() > 0) {
      selectDeclinedCont = selectDeclinedCont.argString(consentAttr);
    }
    report.add(selectDeclinedCont.query(rs -> {
          ArrayList<Object> line = new ArrayList<>();
          if (rs.next()) {
            line.add(String.valueOf(rs.getIntegerOrZero()));
            line.add("Declined to continue participating during the period");
          }
          return line;
        })
    );
    return report;
  }

  private ArrayList<Object> summaryLine(String title, int count) {
    ArrayList<Object> line = new ArrayList<>();
    line.add(title);
    line.add( count);
    for (int i=0; i<5; i++) {
      line.add(""); // download needs something in all columns
    }
    return line;
  }

  private String dateSql( String column,  Flavor flavor) {
    if (Flavor.postgresql.equals(flavor)) {
      return "date_trunc('day', " + column + ") ";
    }
    return "trunc(" + column + ") ";
  }

  public XSSFWorkbook writeXlsx(ArrayList<ArrayList<Object>> report, String name, String sheetName, int skipRows)
      throws IOException {
    if (report == null || report.size() == 0) {
      logger.warn("There were no reports to output");
      return null;
    }

    XxcelReader reader = new XxcelReader(siteInfo);
    try {
      XSSFSheet sheet;
      XSSFWorkbook workBook = reader.getTemplate(name);
      if (workBook != null) {
        logger.debug("workbook is not null, using template sheet " + sheetName);

        sheet = workBook.getSheet(sheetName);
      } else {
        logger.debug("workbook is null, not using template");
        workBook = new XSSFWorkbook();
        sheet = workBook.createSheet(name);
        sheet.createRow(0);
      }
      if (sheet == null) {
        sheet = workBook.createSheet(name);
        throw new IOException("Workbook sheet is null, cannot create");
      }
      logger.debug("reading rows");
      XxcelWriter writer = new XxcelWriter(sheet);
      for (int row = 0; row < report.size(); row++) {
        ArrayList<Object> rowData = report.get(row);
        int numberColumns = 0;
        if (rowData != null) {
          numberColumns = rowData.size();
        }
        int writeRow = row + skipRows;
        for (int col = 0; col < numberColumns; col++) {
          Object value = rowData.get(col);
          String valueStr = value == null ? "" : value.toString();
          if (valueStr != null && !valueStr.isEmpty()) {
            try {
              if (value instanceof String) {
                writer.writeColumn(writeRow, col, valueStr);
              } else if (value instanceof Integer) {
                writer.writeColumn(writeRow, col, (Integer) value);
              } else if (value instanceof Double) {
                writer.writeColumn(writeRow, col, (Double) value);
              } else if (value instanceof Long) {
                writer.writeColumn(writeRow, col, (Long) value);
              } else if (value instanceof Date) {
                writer.writeColumn(writeRow, col, (Date) value);
              } else if (value instanceof Calendar) {
                writer.writeColumn(writeRow, col, (Calendar) value);
              } else {
                writer.writeColumn(writeRow, col, valueStr);
              }
            } catch (Exception ex) {
              logger.error("Error in  XxcelWriter.writeColumn(row,col):(" + writeRow + "," + col + ") value=" + valueStr + "; "+ex, ex);
            }
          } else {
            writer.writeColumn(writeRow, col, " ");
          }
        }
        // delete left over cells
        XSSFRow templateRow = writer.getRow(writeRow);
        while (templateRow != null && templateRow.getLastCellNum() > (numberColumns)) {
          int lastCellNumber = templateRow.getLastCellNum() - 1;
          templateRow.removeCell(templateRow.getCell(lastCellNumber));
          logger.debug("deleting left over cells " + writeRow + ", " + lastCellNumber);
        }
      }
      logger.debug("DONE");
      return workBook;
    } catch (Exception ex) {
      logger.error("Error in writeXlxs:" + ex, ex);
      return new XSSFWorkbook();
    }
  }

  public ArrayList<String> generateText(Database database, Long siteId, TextReport tReport, JSONObject object,
                                        AssessmentId assessmentId, ChartConfigurationOptions opts) {
    ArrayList<String> reportStringArray = new ArrayList<>();
    if (tReport == null || object == null) {
      return reportStringArray;
    }

    AssessDao assessDao = new AssessDao(database, siteInfo);

    String newLine = System.getProperty("line.separator");
    ByteArrayOutputStream baos = null;
    try {
      reportStringArray = tReport.makeReport(object, opts);
      baos = new ByteArrayOutputStream();
      for (String line : reportStringArray) {
        baos.write(line.getBytes(StandardCharsets.UTF_8));
        baos.write(newLine.getBytes(StandardCharsets.UTF_8));
      }
      logger.debug("RPTTXT testing: Writing text report to database");
      writeResult(assessDao, assessmentId, tReport, baos.toByteArray());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }finally {
      try {
        if (baos != null) {
          baos.close();
        }
      } catch (IOException ex) {
        logger.error(ex);
      }
    }
    return reportStringArray;
  }

  public JSONObject generateJson(Database database, JsonReport jReport, AssessmentRegistration assessment,
                                 ChartConfigurationOptions opts) throws IOException {
    JSONObject json = new JSONObject();

    if (jReport == null || assessment == null) {
      return json;
    }
    AssessDao assessDao = new AssessDao(database, siteInfo);
    try {
      ArrayList<PatientStudyExtendedData> patientStudies = this.getPatientStudies(database, assessment);
      json = jReport.makeJson(patientStudies, assessment.getAssessmentType(), assessment.getPatientId(), opts);
      if (logger.isTraceEnabled()) {
        logger.trace("writing json string " + json.toString());
      }
      writeResult(assessDao, assessment, patientStudies, jReport, json.toString().getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return json;

  }

  /**
   * @param close Pass true if you don't need to modify the pdf, eg set its catalog's setOpenAction.
   * If you pass false, you must call its close() method.
   */
  public PDDocument generatePdf(Database database, PatientReport pReport, AssessmentRegistration assessment,
                                ChartConfigurationOptions opts, boolean close) throws IOException {
    /*
     * If not found create & save it to the database
     */
    PDDocument pdf = new PDDocument();
    if (pReport == null || assessment == null) {
      return pdf;
    }

    AssessDao assessDao = new AssessDao(database, siteInfo);
    try {
      ArrayList<PatientStudyExtendedData> patientStudies = getPatientStudies(database, assessment);
      pdf = pReport.makePdf(patientStudies, assessment, opts, pdf);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      pdf.save(baos);
      writeResult(assessDao, assessment, patientStudies, pReport, baos );
      if (close) {
        pdf.close();  // don't close if you need to, e.g. write javascript call the catalog setOpenAction()
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return pdf;
  }

  public ArrayList<PatientStudyExtendedData> getPatientStudies(Database database, AssessmentRegistration assessment) {
    /*
     * Find all the completed surveys up through the requested id
     */
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    ArrayList<PatientStudyExtendedData> patientStudies =
        patStudyDao.getPatientStudyExtendedDataByPatientId(assessment.getPatientId(), assessment.getAssessmentDt());
    try {
      Map<String, Boolean> assisted = patStudyDao.getPatientAssistedStudyTokens(assessment.getPatientId());
      for (PatientStudyExtendedData patientStudy : patientStudies) {
        if (patientStudy.getToken() != null && assisted.get(patientStudy.getToken()) != null) patientStudy.setAssisted(true);
      }
    } catch (DatabaseException sqle) {
      logger.error("checking for assisted surveys for patient " + assessment.getPatientId(), sqle);
    }
    return patientStudies;
  }

  private void writeResult(AssessDao assessDao, AssessmentRegistration assessment, ArrayList<PatientStudyExtendedData> patientStudies, ResultGeneratorIntf report, ByteArrayOutputStream baos) {

    /*
     * Save the pdf into the database.
     * Write the patient result to survey registration rows identifying the registrations included in this report.
     * Write the view or print report activity.
     */
    PatientResult result = new PatientResult();
    result.setAssessmentRegId(assessment.getAssessmentRegId());
    result.setSurveySiteId(assessment.getSurveySiteId());
    result.setDocumentControlId(report.getDocumentControlId());
    result.setPatientResTypId(report.getResultType().getPatientResTypId());
    result.setPatientResVs(report.getResultVersion());


    result.setResultBytes(baos.toByteArray());
    result = assessDao.insertPatientResult(result);
    writeChartSurveyIds(assessDao, result.getPatientResId(), patientStudies);
  }

  private PatientResult writeResult(AssessDao assessDao, AssessmentRegistration assessment, ArrayList<PatientStudyExtendedData> patientStudies, ResultGeneratorIntf report, byte[] bytes) {
    PatientResult result = new PatientResult();
    result.setAssessmentRegId(assessment.getAssessmentRegId());
    result.setSurveySiteId(assessment.getSurveySiteId());
    result.setDocumentControlId(report.getDocumentControlId());
    result.setPatientResTypId(report.getResultType().getPatientResTypId());
    result.setPatientResVs(report.getResultVersion());
    result.setResultBlob(bytes);
    result = assessDao.insertPatientResult(result);
    writeChartSurveyIds(assessDao, result.getPatientResId(), patientStudies);
    return result;
  }

  private void writeChartSurveyIds (AssessDao assessDao, Long patientResultId, ArrayList<PatientStudyExtendedData> patientStudies) {
    logger.debug("writeChartSurveyIds starting");
    // Create a list of survey_reg_id, patient_res_id, survey_site_id
    PatientResSurveyRegLinkList list = null;
    if (patientStudies != null && patientStudies.size() > 0 && patientResultId != null) {
      for (PatientStudyExtendedData patientStudy : patientStudies) {
        if (patientStudy != null) {
          Long surveyRegId = patientStudy.getSurveyRegId();
          Long surveySiteId = patientStudy.getSurveySiteId();
          if (list == null) {
            logger.debug("writeChartSurveyIds starting Link list for " + surveySiteId + "," + patientResultId);
            list = new PatientResSurveyRegLinkList(surveySiteId, patientResultId);
          }
          if (surveyRegId != null && surveySiteId != null && !list.contains(surveyRegId)) {
            logger.debug("writeChartSurveyIds: added relationship for " + surveyRegId);
            list.addRelationship(surveyRegId);
          }
        }
      }
    }

    if (list != null) {
      logger.debug("writeChartSurveyIds: list has " + list.size() + " entries");
      for (int t = 0; t < list.size(); t++) {
        assessDao.insertPatientResSurveyRegLink(list.getRelationship(t));
      }
    }
  }

  /** write the text report */
  private PatientResult writeResult(AssessDao assessDao, AssessmentId assessmentId, ResultGeneratorIntf report, byte[] bytes) {
    PatientResult result = new PatientResult();
    result.setAssessmentRegId(assessmentId.getId());
    result.setSurveySiteId(siteId);
    result.setDocumentControlId(report.getDocumentControlId());
    result.setPatientResTypId(report.getResultType().getPatientResTypId());
    result.setPatientResVs(report.getResultVersion());
    result.setResultBlob(bytes);
    result = assessDao.insertPatientResult(result);
    return result;
  }
}
