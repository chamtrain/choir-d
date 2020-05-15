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
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.database.Database;
import edu.stanford.registry.server.database.ResultSetHandler;
import edu.stanford.registry.server.database.impl.DataColumnHandler;
import edu.stanford.registry.server.reports.JsonReport;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.reports.TextReport;
import edu.stanford.registry.shared.*;
import edu.ufl.registry.server.service.hl7message.EpicExportData;
import edu.ufl.registry.server.service.hl7message.HL7Generator;
import edu.ufl.registry.server.service.hl7message.HL7Transmission.HL7Sender;
import edu.ufl.registry.shared.EpicLog;
import oracle.sql.CLOB;
import oracle.sql.TIMESTAMP;
import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Date;

public class ReportUtils {

  private static Logger logger = Logger.getLogger(ReportUtils.class);

  public ReportUtils() {
  };

  public ArrayList<ArrayList<Object>> averageSurveyTimeReportByMonth(final Database database, Date fromDate, Date toDate) {
    StringBuffer sql = new StringBuffer();
    sql.append("select survey_month, trunc(avg(total_milliseconds)) as avg_minutes, trunc(stddev(total_milliseconds)) from ");
    sql.append("( SELECT survey_month, extract( day from dtdiff )*24*60*60*1000 + extract( hour from dtdiff )*60*60*1000 ");
    sql.append("       + extract( minute from dtdiff )*60*1000 + round(extract( second from dtdiff )*1000) as total_milliseconds");
    sql.append("  FROM (with starttimes as ( ");
    sql.append("          SELECT token, max(activity_dt) as started from activity where activity_type in('Validated') ");
    sql.append("       		group by token   ),");
    sql.append("            finishtimes as (");
    sql.append("          SELECT token, activity_dt as completed from activity where activity_type in('Completed') )");
    sql.append(" 			 select to_char(ft.completed, 'yyyy-mm') as survey_month, completed - started as dtdiff");
    sql.append("		     from starttimes st, finishtimes ft where st.token=ft.token and ft.completed > st.started ");
    // Only include surveys started & finished on the same day, this removes odd balls that skew the results
    sql.append("         and to_char(ft.completed, 'yyyymmdd') = to_char(st.started, 'yyyymmdd') ");
    sql.append("         and ft.completed >= ? and ft.completed <= ? ) ");
    sql.append(") GROUP BY survey_month ORDER BY survey_month desc");
    Object args[] = { DateUtils.getTimestampStart(fromDate), DateUtils.getTimestampEnd(toDate) };

    ArrayList<ArrayList<Object>> data = runReportQuery(database, sql.toString(), args);
    for (int rowIndx = 0; rowIndx < data.size(); rowIndx++) {
      ArrayList<Object> row = data.get(rowIndx);
      for (int colIndx = 1; colIndx < row.size(); colIndx++) {

        Integer milliseconds = Integer.parseInt(row.get(colIndx).toString());
        int sec = (milliseconds / 1000) % 60;
        int min = ((milliseconds / (1000 * 60)) % 60);
        int hrs = ((milliseconds / (1000 * 60 * 60)) % 24);
        StringBuffer str = new StringBuffer();
        if (hrs > 0) {
          str.append(hrs + ":");
        }
        str.append(min + ":");
        str.append(sec);
        row.set(colIndx, str.toString());
      }
    }

    return data;
  }

  public ArrayList<ArrayList<Object>> averageSurveyTimeReportByType(final Database database, Date fromDate, Date toDate) {
    StringBuffer sql = new StringBuffer();
    sql.append("select survey_type, trunc(avg(total_milliseconds)) as avg_minutes, trunc(stddev(total_milliseconds)) from ");
    sql.append("( SELECT survey_type, extract( day from dtdiff )*24*60*60*1000 + extract( hour from dtdiff )*60*60*1000 ");
    sql.append("       + extract( minute from dtdiff )*60*1000 + round(extract( second from dtdiff )*1000) as total_milliseconds");
    sql.append("  FROM (with starttimes as ( ");
    sql.append("          SELECT token, max(activity_dt) as started from activity where activity_type in('Validated') ");
    sql.append("          group by token   ),");
    sql.append("            finishtimes as (");
    sql.append("          SELECT token, activity_dt as completed from activity where activity_type in('Completed') )");
    sql.append("       select survey_type, completed - started as dtdiff");
    sql.append("         from starttimes st, finishtimes ft, survey_registration sr ");
    sql.append("       where st.token=ft.token and st.token = sr.token and completed > started ");
    sql.append("         and to_char(ft.completed, 'yyyymmdd') = to_char(st.started, 'yyyymmdd') ");
    sql.append("         and ft.completed >= ? and ft.completed <= ? ) ");
    sql.append(") GROUP BY survey_type ORDER BY survey_type");
    Object args[] = { DateUtils.getTimestampStart(fromDate), DateUtils.getTimestampEnd(toDate) };

    ArrayList<ArrayList<Object>> data = runReportQuery(database, sql.toString(), args);
    for (int rowIndx = 0; rowIndx < data.size(); rowIndx++) {
      ArrayList<Object> row = data.get(rowIndx);
      for (int colIndx = 1; colIndx < row.size(); colIndx++) {

        Integer milliseconds = Integer.parseInt(row.get(colIndx).toString());
        int sec = (milliseconds / 1000) % 60;
        int min = ((milliseconds / (1000 * 60)) % 60);
        int hrs = ((milliseconds / (1000 * 60 * 60)) % 24);
        StringBuffer str = new StringBuffer();
        if (hrs > 0) {
          str.append(hrs + ":");
        }
        str.append(min + ":");
        str.append(sec);
        row.set(colIndx, str.toString());
      }
    }

    return data;
  }

  public ArrayList<ArrayList<Object>> averageSurveyTimeReport(final Database database, Date fromDate, Date toDate) {
    StringBuffer sql = new StringBuffer();
    sql.append("select nvl(avg(total_milliseconds),0) as avg_minutes, nvl(stddev(total_milliseconds),0) as stddev from ");
    sql.append("( SELECT survey_month, extract( day from dtdiff )*24*60*60*1000 + extract( hour from dtdiff )*60*60*1000 ");
    sql.append("       + extract( minute from dtdiff )*60*1000 + round(extract( second from dtdiff )*1000) as total_milliseconds");
    sql.append("  FROM (with starttimes as ( ");
    sql.append("          SELECT token, max(activity_dt) as started from activity where activity_type in('Validated') ");
    sql.append("       		group by token   ),");
    sql.append("            finishtimes as (");
    sql.append("          SELECT token, activity_dt as completed from activity where activity_type in('Completed') )");
    sql.append(" 			 select to_char(ft.completed, 'yyyy-mm') as survey_month, completed - started as dtdiff");
    sql.append("		     from starttimes st, finishtimes ft where st.token=ft.token ");
    sql.append("         and to_char(ft.completed, 'yyyymmdd') = to_char(st.started, 'yyyymmdd') ");
    sql.append("         and ft.completed >= ? and ft.completed <= ? ) ");
    sql.append(") ");
    Object args[] = { DateUtils.getTimestampStart(fromDate), DateUtils.getTimestampEnd(toDate) };

    ArrayList<ArrayList<Object>> data = runReportQuery(database, sql.toString(), args);
    for (int rowIndx = 0; rowIndx < data.size(); rowIndx++) {
      ArrayList<Object> row = data.get(rowIndx);
      for (int colIndx = 0; colIndx < row.size(); colIndx++) {

        Integer milliseconds = Integer.parseInt(row.get(colIndx).toString());
        int sec = (milliseconds / 1000) % 60;
        int min = ((milliseconds / (1000 * 60)) % 60);
        int hrs = ((milliseconds / (1000 * 60 * 60)) % 24);
        StringBuffer str = new StringBuffer();
        if (hrs > 0) {
          str.append(hrs + ":");
        }
        str.append(min + ":");
        str.append(sec);
        row.set(colIndx, str.toString());
      }
    }

    return data;
  }

  public ArrayList<ArrayList<Object>> complianceSummaryReport(final Database database, Date fromDate, Date toDate) {

    String sql = "SELECT status, number_of_patients from ( "
                 + " with patient_status as ( "
                 + " select patient_id, "
                 + " sum(decode(activity_type,'Declined',1,0)) as declined, "
                 + " sum(decode(activity_type,'Consented',1,0)) as consented, "
                 + " sum(decode(activity_type,'Completed',1,0)) as completed   "
                 + " from activity where patient_id not in (select patient_id from patient where last_name = 'Test-Patient')  "
                 + " and activity_dt between ? AND ? "
                 + "group by patient_id) "
                 + " select 'Declined' as status, count(1) as number_of_patients, 1 as order_by from patient_status where declined > 0 "
                 + " union " + " select 'Consented', count(1), 2 from patient_status where consented > 0 " + " union "
                 + " select 'Completed Survey', count(1), 3 from patient_status where completed > 0 " + " ) order by order_by ";

    Object args[] = { DateUtils.getTimestampStart(fromDate), DateUtils.getTimestampEnd(toDate) };
    return runReportQuery( database, sql, args );
  }

  public ArrayList<ArrayList<Object>> complianceReport1(Database database) {
    String sql =
            " SELECT to_char(sr.survey_dt, 'mm') as survey_month,  count(*), sr.visit_type, nvl(sru.participates, ' ') "
            + " FROM survey_registration sr,  "
            + "   ( select min(survey_dt) survey_dt, survey_registration.patient_id, data_value as participates "
            + "     from survey_registration "
            + "     left outer join patient_attribute "
            + "     on survey_registration.patient_id = patient_attribute.patient_id "
            + "     and patient_attribute.data_name = 'participatesInSurveys' "
            + "     where registration_type in ('a') "
            + "     and survey_registration.patient_id not in (select patient_id from patient where last_name = 'Test-Patient')"
            // --and visit_type in ('NPV60') --, 'NPV75', 'NHA', 'TMJN')
            + "     group by survey_registration.patient_id, to_char(survey_dt,'mm'), data_value  ) sru "
            + " WHERE  sr.patient_id = sru.patient_id and sr.survey_dt = sru.survey_dt  "
            + "   AND sr.survey_dt > to_date((select '01' || TO_CHAR(ADD_MONTHS(SYSDATE, -12), 'MONyyyy') || ' 00:00' from dual), 'ddMONyyyy hh24:mi') "
            + "   AND sr.survey_dt < sysdate "
            + " GROUP BY to_char(sr.survey_dt,'mm'), sr.visit_type, sru.participates "
            + " ORDER BY survey_month, sr.visit_type, participates ";
    return database.query(sql, getResultToArrayHandler(database));
  }

  public ArrayList<ArrayList<Object>> complianceReport2(Database database, boolean onlyNew, Date startDt, Date endDt) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT survey_date, count(*),  participates FROM ( ");
    sql.append(" SELECT survey_registration.patient_id, to_char(survey_dt,'yyyy-mm') as survey_date, ");
    sql.append(" 			nvl(to_char(patient_attribute.dt_created,'yyyy-mm') ,'9999-99'), ");
    sql.append(" 			CASE when nvl(to_char(patient_attribute.dt_created,'yyyy-mm') ,'9999-99')");
    sql.append(" 			<= to_char(survey_dt,'yyyy-mm') ");
    sql.append(" 			then data_value else null END as participates ");
    sql.append(" FROM survey_registration ");
    sql.append(" 			LEFT OUTER JOIN patient_attribute ");
    sql.append(" 			on survey_registration.patient_id = patient_attribute.patient_id ");
    sql.append(" 			and patient_attribute.data_name = 'participatesInSurveys'");
    sql.append(" WHERE registration_type in ('a','s') ");
    sql.append(" AND survey_type not in ('Patient Consented', 'Patient Declined') ");
    sql.append(" AND survey_registration.patient_id not in (select patient_id from patient where last_name = 'Test-Patient') ");
    if (onlyNew) {
      List<String> newVisitTypes = XMLFileUtils.getInstance().getVisitTypesInGroup("new");
      String visitList = "";
      for(String visitType : newVisitTypes) {
        visitList += (visitList.length() > 0) ? "," : "";
        visitList += "'" + visitType + "'";
      }
      sql.append(" AND visit_type in (" + visitList + ") ");
    }
    sql.append(" AND survey_registration.survey_dt >= ? ");
    sql.append(" AND survey_registration.survey_dt <= ? ");
    sql.append(" GROUP BY survey_registration.patient_id, to_char(survey_dt,'yyyy-mm'), ");
    sql.append(" data_value, to_char(patient_attribute.dt_created,'yyyy-mm')  )");
    sql.append(" GROUP BY survey_date, participates ");
    sql.append(" ORDER BY survey_date, participates");
    ;
    Object[] params = { DateUtils.getTimestampStart(startDt), DateUtils.getTimestampEnd(endDt) };
    return database.query(sql.toString(), params, getResultToArrayHandler(database));
  }

  public ArrayList<ArrayList<Object>> eligibleVisitsReport(final Database database, Date fromDate, Date toDate) {

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT mnth, sum(possible) possible, sum(completed) completed, ");
    sql.append(" 		   sum(uncompleted) uncompleted, sum(unstarted) unstarted from (with ");
    sql.append("date_enrolled as ");
    sql.append("  ( select patient_id, trunc(dt_created) enrolled ");
    sql.append("		from patient_attribute where data_name='participatesInSurveys' and data_value='y'  ), ");
    sql.append("surveys as ");
    sql.append("	( select sr.patient_id, trunc(sr.survey_dt) survey_dt, sr.token from survey_registration sr ");
    sql.append(" 		where survey_type not in ('Patient Declined', 'Patient Consented') ");
    sql.append("    and sr.patient_id not in (select patient_id from patient where last_name = 'Test-Patient') ), ");
    sql.append("opportunities as ");
    sql.append("	( select distinct sr.patient_id, trunc(survey_dt) survey_date, sr.token ");
    sql.append("	  from surveys sr join date_enrolled de on (sr.patient_id=de.patient_id) ");
    sql.append("    join patient_study ps on (sr.patient_id = ps.patient_id and sr.token=ps.token) ");
    sql.append("    where sr.survey_dt >= de.enrolled ), ");
    sql.append("completed as ");
    sql.append(" ( select distinct sr.patient_id, trunc(survey_dt) survey_date, sr.token from surveys sr ");
    sql.append("    join activity act on (sr.patient_id = act.patient_id and sr.token = act.token) ");
    sql.append("    where act.activity_type = 'Completed' ), ");
    sql.append("uncompleted as ");
    sql.append("  ( select distinct sr.patient_id, trunc(survey_dt) survey_date, sr.token ");
    sql.append(" 		from surveys sr, activity act where sr.patient_id = act.patient_id and sr.token = act.token ");
    sql.append(" 		and act.activity_type in ('Sent Response') and not exists ");
    sql.append("    (select * from activity act2 where activity_type = 'Completed' and act2.token = act.token) ), ");
    sql.append("nonstarted as ");
    sql.append("	( select distinct sr.patient_id, trunc(survey_dt) survey_date, sr.token ");
    sql.append(" 		from surveys sr join date_enrolled de on (sr.patient_id=de.patient_id) ");
    sql.append(" 		where sr.survey_dt >= de.enrolled and exists (select * from patient_study ps where ps.token=sr.token ");
    sql.append(" and ps.patient_id = sr.patient_id) and not exists (select * from activity act where act.token=sr.token ");
    sql.append(" and act.patient_id = sr.patient_id and activity_type in ('Completed','Validated', 'Sent Response')) ) ");
    sql.append(" SELECT to_char(o.survey_date, 'yyyy-mm') as mnth, o.survey_date, count(o.patient_id) possible, ");
    sql.append(" sum(case when c.patient_id is null then 0 else 1 end) completed, ");
    sql.append(" sum(case when u.patient_id is null then 0 else 1 end) uncompleted,");
    sql.append(" sum(case when n.patient_id is null then 0 else 1 end) unstarted");
    sql.append(" FROM opportunities o left join completed c on (o.survey_date = c.survey_date and o.patient_id = c.patient_id and o.token = c.token)");
    sql.append("                   left join uncompleted u on (o.survey_date = u.survey_date and o.patient_id = u.patient_id and o.token = u.token)");
    sql.append("                   left join nonstarted n on (o.survey_date = n.survey_date and o.patient_id = n.patient_id and o.token = n.token)");
    sql.append(" GROUP BY o.survey_date ORDER BY o.survey_date) where " + " survey_date >= ? and survey_date <= ?");
    sql.append(" group by mnth order by mnth");

    Object args[] = { DateUtils.getTimestampStart(fromDate), DateUtils.getTimestampEnd(toDate) };

    return runReportQuery(database, sql.toString(), args);
  }

  public ArrayList<ArrayList<Object>> inEligibleVisitsReport(final Database database, Date fromDate, Date toDate) {

    StringBuffer sql = new StringBuffer();
    sql.append("SELECT mnth, sum(not_eligible) not_eligible, sum(not_asked) not_asked, ");
    sql.append("             sum(declined) declined, sum(suppressed) suppressed ");
    sql.append("FROM  ( with ");
    sql.append(" date_enrolled as ");
    sql.append(" ( select patient_id, trunc(dt_created) enrolled ");
    sql.append("     from patient_attribute where data_name='participatesInSurveys' ), ");
    sql.append("date_declined as ");
    sql.append("( select patient_id, trunc(dt_created) enrolled ");
    sql.append("    from patient_attribute where data_name='participatesInSurveys' and data_value='n'  ), ");
    sql.append("date_agreed as ");
    sql.append("( select patient_id, trunc(dt_created) enrolled ");
    sql.append("    from patient_attribute where data_name='participatesInSurveys' and data_value='y'  ),");
    sql.append("surveys as ");
    sql.append("( select sr.patient_id, trunc(sr.survey_dt) survey_dt, sr.token from survey_registration sr ");
    sql.append("   where survey_type not in ('Patient Declined', 'Patient Consented')");
    sql.append("    and sr.patient_id not in (select patient_id from patient where last_name = 'Test-Patient') ), ");
    sql.append("not_asked as ");
    sql.append("( select distinct sr.patient_id, trunc(survey_dt) survey_date ");
    sql.append("    from surveys sr ");
    sql.append("    left outer join date_enrolled de on ");
    sql.append("    (sr.patient_id=de.patient_id and sr.survey_dt >= de.enrolled) ");
    sql.append("   where enrolled is null ), ");
    sql.append("declined as ");
    sql.append("( select distinct sr.patient_id, trunc(survey_dt) survey_date ");
    sql.append("    from surveys sr join date_declined dd on (sr.patient_id=dd.patient_id) ");
    sql.append("   where sr.survey_dt >= dd.enrolled), ");
    sql.append("suppressed as ");
    sql.append("( select distinct sr.patient_id, trunc(survey_dt) survey_date ");
    sql.append("    from surveys sr join date_agreed da on (sr.patient_id = da.patient_id) ");
    sql.append("   where sr.survey_dt >= da.enrolled ");
    sql.append("and not exists ");
    sql.append("        (select * from patient_study ps ");
    sql.append("          where sr.patient_id = ps.patient_id and sr.token = ps.token) ), ");
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
    sql.append(") WHERE survey_date >= ? ");
    sql.append("    AND survey_date <= ? ");
    sql.append("GROUP BY mnth order by mnth");

    Object args[] = { DateUtils.getTimestampStart(fromDate), DateUtils.getTimestampEnd(toDate) };
    return runReportQuery(database, sql.toString(), args);

  }

  /**
   * patientDataStatusReport
   *
   * Returns results from a query run on the Epic_Log table
   *
   * @param database database to use
   * @param fromDate beginning date range
   * @param toDate ending date range
   * @return query statement
   *
   * @author kpharvey
   */
  public ArrayList<ArrayList<Object>> patientDataStatusReport( final Database database, Date fromDate, Date toDate ) {
    StringBuffer sql = new StringBuffer();

    sql.append( "SELECT survey_registration.patient_id, to_char(survey_registration.survey_dt, 'yyyy-mm-dd'), epic_log.success, epic_log.message " );
    sql.append( "FROM survey_registration FULL JOIN epic_log " );
    sql.append( "ON survey_registration.survey_reg_id = epic_log.survey_reg_id " );
    sql.append( "WHERE survey_registration.survey_dt >= ? " );
    sql.append( "AND survey_registration.survey_dt <= ? " );
    sql.append( "ORDER BY survey_registration.survey_dt DESC" );

    Object args[] = {DateUtils.getTimestampStart( fromDate ), DateUtils.getTimestampEnd( toDate )};
    return runReportQuery( database, sql.toString(), args );
  }

  private ArrayList<ArrayList<Object>> runReportQuery(final Database database, String sql, Object args[]) {

    return database.query(sql.toString(), args, new ResultSetHandler<ArrayList<ArrayList<Object>>>() {
      @Override
      public ArrayList<ArrayList<Object>> process(ResultSet rs) throws Exception {
        ArrayList<ArrayList<Object>> report = new ArrayList<ArrayList<Object>>();
        if (rs != null) {
          try {
            int colCount = rs.getMetaData().getColumnCount();
            logger.debug(colCount + " columns in resultset");
            while (rs.next()) {
              ArrayList<Object> reportLine = getDataLine(rs, database.getDataColumnHandler(), colCount);
              report.add(reportLine);
            }
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return report;
          }
          logger.debug("Report Query Returning arraylist with " + report.size() + " rows");
        } else {
          logger.debug("Report Query resultset is null");
        }
        return report;
      }
    });
  }

  public ResultSetHandler<ArrayList<ArrayList<Object>>> getResultToArrayHandler(final Database database) {
    return new ResultSetHandler<ArrayList<ArrayList<Object>>>() {
      @Override
      public ArrayList<ArrayList<Object>> process(ResultSet rs) throws Exception {
        ArrayList<ArrayList<Object>> report = new ArrayList<ArrayList<Object>>();
        if (rs != null) {

          try {
            int colCount = rs.getMetaData().getColumnCount();
            logger.debug(colCount + " columns in resultset");
            while (rs.next()) {
              ArrayList<Object> reportLine = getDataLine(rs, database.getDataColumnHandler(), colCount);
              report.add(reportLine);
            }
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return report;
          }

        } else {
          logger.debug("complianceReport1 resultset is null");
        }
        logger.debug("complianceReport1 Returning arraylist with " + report.size() + " rows");
        return report;
      }

    };
  }

  public ArrayList<Object> getDataLine(ResultSet rs, DataColumnHandler colHandler, Integer colCount)
          throws SQLException {

    ArrayList<Object> line = new ArrayList<Object>();
    ReportObjectConverter objectConverter = new ReportObjectConverter(colHandler);
    for (int i = 1; i <= colCount; i += 1) {
      Object value = rs.getObject(i);
      line.add(objectConverter.toString(value));
    }
    return line;
  }

  private class ReportObjectConverter {
    DataColumnHandler colHandler = null;

    public ReportObjectConverter(DataColumnHandler colHandler) {
      this.colHandler = colHandler;
    }

    public String toString(Object value) {
      if (value == null) {
        return "";
      }
      if (value instanceof String) {
        return (String) value;
      } else if (value instanceof Timestamp) {
        return toString((Timestamp) colHandler.processResultSetData(value, "java.sql.Timestamp"));
      } else if (value instanceof TIMESTAMP) {
        return toString((Timestamp) colHandler.processResultSetData(value, "java.sql.Timestamp"));
      } else if (value instanceof BigDecimal) {
        return toString((Integer) colHandler.processResultSetData(value, "java.lang.Integer"));
      } else if (value instanceof CLOB) {
        return (String) colHandler.processResultSetData(value, "java.lang.String");
      }
      logger.debug("value type not handled, returning value.toString()");
      return value.toString();
    }

    public String toString(Integer value) {
      return value.toString();
    }

    public String toString(java.sql.Date value) {
      return ServerUtils.getInstance().getDateString(value);
    }

    public String toString(Timestamp value) {
      return ServerUtils.getInstance().getDateString(value);
    }

  }

  public XSSFWorkbook writeXlsx(ArrayList<ArrayList<Object>> report, String name, String sheetName, int skipRows)
          throws IOException {
    XxcelReader reader = new XxcelReader();
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

        for (int col = 0; col < numberColumns; col++) {
          Object value = rowData.get(col);

          if (value != null && value.toString().length() > 0) {
            try {
              if (value instanceof String) {
                writer.writeColumn(row + skipRows, col, value.toString());
              } else if (value instanceof Integer) {
                writer.writeColumn(row, col, (Integer) value);
              } else if (value instanceof Double) {
                writer.writeColumn(row, col, (Double) value);
              } else if (value instanceof Long) {
                writer.writeColumn(row, col, (Long) value);
              } else if (value instanceof Date) {
                writer.writeColumn(row, col, (Date) value);
              } else if (value instanceof Calendar) {
                writer.writeColumn(row, col, (Calendar) value);
              } else {
                writer.writeColumn(row, col, value.toString());
              }
            } catch (Exception ex) {
              logger.error("Error in  XxcelWriter.writeColumn(row,col):(" + row + "," + col + ") " + ex, ex);
            }
          } else {
            logger.debug("row, col : " + row + "," + col + " Data value is empty");
          }
        }
        // delete left over cells
        XSSFRow templateRow = writer.getRow(row);
        while (templateRow != null && templateRow.getLastCellNum() > (numberColumns)) {
          int lastCellNumber = templateRow.getLastCellNum() - 1;
          templateRow.removeCell(templateRow.getCell(lastCellNumber));
          logger.debug("deleting left over cells " + row + ", " + lastCellNumber);
        }
      }
      logger.debug("DONE");
      return workBook;
    } catch (Exception ex) {
      logger.error("Error in writeXlxs:" + ex);
      return new XSSFWorkbook();
    }
  }

  public ArrayList<String> generateText(Database database, TextReport tReport, JSONObject object,
                                        Long surveyRegId, ChartConfigurationOptions opts) {
    ArrayList<String> reportStringArray = new ArrayList<String>();
    if (tReport == null || object == null) {
      return reportStringArray;
    }

    String newLine = System.getProperty("line.separator");
    ByteArrayOutputStream baos = null;
    try {
      reportStringArray = tReport.makeReport(object, opts);
      baos = new ByteArrayOutputStream();
      for (String line : reportStringArray) {
        baos.write(line.getBytes());
        baos.write(newLine.getBytes());
      }
      logger.debug("RPTTXT testing: Writing text report to database");
      writeResult(database,ServerUtils.getInstance().getSiteId(), surveyRegId, tReport, baos.toByteArray());
    } catch (InvalidDataElementException e) {
      logger.error(e.getMessage(), e);
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

  public JSONObject generateJson(Database database, JsonReport jReport, SurveyRegistration registration,
                                 ChartConfigurationOptions opts) throws IOException {
    JSONObject json = new JSONObject();

    if (jReport == null || registration == null) {
      return json;
    }
    try {
      ArrayList<PatientStudyExtendedData> patientStudies = getPatientStudies(database, registration);
      json = jReport.makeJson(patientStudies, registration.getPatientId(), opts);
      System.out.println( "writing json string " + json.toString() );
      writeResult(database, registration, patientStudies, jReport, json.toString().getBytes());
    } catch (InvalidDataElementException e) {
      logger.error(e.getMessage(), e);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return json;

  }

  public PDDocument generatePdf(Database database, PatientReport pReport, SurveyRegistration registration,
                                ChartConfigurationOptions opts) throws IOException {
    /*
     * If not found create & save it to the database
     */
    PDDocument pdf = new PDDocument();
    if (pReport == null || registration == null) {
      return pdf;
    }

    try {
      // getting a list of the patient Studies
      ArrayList<PatientStudyExtendedData> patientStudies = getPatientStudies(database, registration);
      // producing a PDF for the report.
      pdf = pReport.makePdf( patientStudies, registration.getPatientId(), opts, pdf );
      // creating a container for the PDF information
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      // saving the PDF data to the container.
      pdf.save( baos );

      sendPdfMessage( database, registration, baos );

      writeResult(database, registration, patientStudies, pReport, baos);
      return pdf;
    } catch (InvalidDataElementException e) {
      logger.error(e.getMessage(), e);
    } catch (COSVisitorException e) {
      logger.error(e.getMessage(), e);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return pdf;
  }


  /**
   * sendPdfMessage
   *
   * This method sends an HL7 message as part of processing
   *
   * @param registration
   * @param baos
   *
   * @author jrpence
   * @author kpharvey
   */
  public void sendPdfMessage( Database database,SurveyRegistration registration, ByteArrayOutputStream baos ) throws IOException, Exception {
    //encode pdf results from the container.
    String encodedResults = encodePdfResults( registration, baos.toByteArray() );

    EpicExportData surveyData = null;
    try {
      // Generation of an Epic Export Data object to hold the pdf message data
      surveyData = new EpicExportData( database, new PatientReport( database ), registration );
    }
    catch ( IOException | InvalidDataElementException e ) {
      logger.error( "Issue creating the Epic Export Data: " + e.getMessage() );
      e.printStackTrace();
    }

    HL7Generator hl7Generator = new HL7Generator();
    String hl7PDF = hl7Generator.generateHL7PDF( surveyData, encodedResults );

    HL7Sender hl7Sender = new HL7Sender();
    String response = hl7Sender.translate( hl7PDF ); //response from the sent HL7 message

    String successStatus = "Failure";
    Long surveyRegistrationId = surveyData.getSurveyRegistration().getSurveyRegId();

    if ( !response.contains( "Error" ) ) {
      successStatus = "Success";
    }

    //log response based off translate method using epic log object
    if ( response.contains( "MSA|AA" ) ) {
      //success
      successStatus = "Success";
    } else {
      //failure
      successStatus = "Failure";
    }
    EpicLog logMessage = new EpicLog( surveyRegistrationId, surveyData.getPatientId(), successStatus, response, 0, hl7PDF );
    DataBaseUtils.addEpicLog( database, logMessage );

    logger.debug( "Translator response for PDF HL7 = " + response );
  }

  /**
   * encodePdfResults
   *
   * Takes byte stream for creating a PDF of results
   * and encodes the bytes as a string
   *
   * @param registration questionnaire taken
   * @param byteArray array of bytes for file
   *
   * @return encoded file string
   *
   * @throws IOException
   * @throws NullPointerException
   *
   * @author kpharvey
   */
  public String encodePdfResults( SurveyRegistration registration, byte[] byteArray ) throws IOException, NullPointerException {

    if ( registration == null ){
      return "";
    }

    String encodedMessage = DatatypeConverter.printBase64Binary( byteArray );

    return encodedMessage;
  }

  public ArrayList<PatientStudyExtendedData> getPatientStudies(Database database, SurveyRegistration registration) {
    /*
     * Find all the completed surveys up through the requested id
     */
    ArrayList<PatientStudyExtendedData> patientStudies =
            DataBaseUtils.getPatientStudyExtendedDataByPatientId(database, registration.getPatientId(),
                                                                 registration.getSurveyDt());
    try {
      Hashtable<Integer, Boolean> assisted = DataBaseUtils.getPatientAssistedStudyTokens(database, registration.getPatientId());
      for (int i = 0; i < patientStudies.size(); i++) {
        PatientStudyExtendedData patientStudy = patientStudies.get(i);
        if (patientStudy.getToken() != null) {
          if (assisted.get(patientStudy.getToken()) != null) {
            patientStudy.setAssisted(true);
          }
        }
      }
    } catch (SQLException sqle) {
      logger.error("checking for assisted surveys for patient " + registration.getPatientId()+ " returned: " + sqle.getMessage());
    }
    return patientStudies;
  }

  private void writeResult(Database database, SurveyRegistration registration, ArrayList<PatientStudyExtendedData> patientStudies, ResultGeneratorIntf report, ByteArrayOutputStream baos) {

    /*
     * Save the pdf into the database.
     * Write the patient result to survey registration rows identifying the registrations included in this report.
     * Write the view or print report activity.
     */
    PatientResult result = new PatientResult();
    result.setSurveyRegId(registration.getSurveyRegId());
    result.setSurveySiteId(registration.getSurveySiteId());
    result.setDocumentControlId(report.getDocumentControlId());
    result.setPatientResTypId(report.getResultType().getPatientResTypId());
    result.setPatientResVs(report.getResultVersion());


    result.setResultBytes(baos.toByteArray());
    result = DataBaseUtils.insertPatientResult(database, result);
    writeChartSurveyIds(database, result.getPatientResId(), patientStudies);
  }

  private PatientResult writeResult(Database database, SurveyRegistration registration, ArrayList<PatientStudyExtendedData> patientStudies, ResultGeneratorIntf report, byte[] bytes) {
    PatientResult result = new PatientResult();
    result.setSurveyRegId(registration.getSurveyRegId());
    result.setSurveySiteId(registration.getSurveySiteId());
    result.setDocumentControlId(report.getDocumentControlId());
    result.setPatientResTypId(report.getResultType().getPatientResTypId());
    result.setPatientResVs(report.getResultVersion());
    result.setResultBlob(bytes);
    result = DataBaseUtils.insertPatientResult(database, result);
    writeChartSurveyIds(database, result.getPatientResId(), patientStudies);
    return result;
  }

  private void writeChartSurveyIds (Database database, Long patientResultId, ArrayList<PatientStudyExtendedData> patientStudies) {
    logger.debug("writeChartSurveyIds starting");
    // Create a list of survey_reg_id, patient_res_id, survey_site_id
    PatientResSurveyRegLinkList list = null;
    if (patientStudies != null && patientStudies.size() > 0 && patientResultId != null) {
      for (int p = 0; p < patientStudies.size(); p++) {
        if (patientStudies.get(p) != null) {
          Long surveyRegId = patientStudies.get(p).getSurveyRegId();
          Long surveySiteId = patientStudies.get(p).getSurveySiteId();
          if (list == null) {
            logger.debug("writeChartSurveyIds starting Link list for " + surveySiteId +"," + patientResultId);
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
        DataBaseUtils.insertPatientResSurveyRegLink(database, list.getRelationship(t));
      }
    }
  }

  /** write the text report */
  private PatientResult writeResult(Database database, Long siteId, Long surveyRegId, ResultGeneratorIntf report, byte[] bytes) {
    PatientResult result = new PatientResult();
    result.setSurveyRegId(surveyRegId);
    result.setSurveySiteId(siteId);
    result.setDocumentControlId(report.getDocumentControlId());
    result.setPatientResTypId(report.getResultType().getPatientResTypId());
    result.setPatientResVs(report.getResultVersion());
    result.setResultBlob(bytes);
    result = DataBaseUtils.insertPatientResult(database, result);
    return result;
  }
}
