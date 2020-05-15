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
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.server.service.rest.api.ApiReportCommon;
import edu.stanford.registry.server.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

import com.github.susom.database.Database;
import com.github.susom.database.Flavor;
import com.github.susom.database.SqlSelect;

/**
 * Runs the Treatment Set Participant report
 *
 * @author tpacht
 */
public class TreatmentSetReportGenerator extends ApiReportCommon implements ApiReportGenerator {

  public TreatmentSetReportGenerator() {
  }

  @Override
  public JSONObject getReportData(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    JSONObject jsonObject = jsonRepresentation.getJsonObject();
    return getReturnObject(tsetReport(databaseProvider.get(), siteInfo, getStartDt(jsonObject), getEndDt(jsonObject), getChoice(jsonObject,"setName"),
        getChoice(jsonObject, "details")));
  }

  @Override
  public JSONObject getReportParameters(Supplier<Database> database, SiteInfo siteInfo, String rpt) throws ApiStatusException {
    if (!rpt.equalsIgnoreCase("tset7days")) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "Report " + rpt + " not supported");
    }
    JSONObject params = new JSONObject();
    params.accumulate("reportParameters", makeReportInputOption("From", "fromDt", "date"));
    params.accumulate("reportParameters", makeReportInputOption("To", "toDt", "date"));
    ArrayList<String> tsetNames = new ArrayList<>();
    tsetNames.add("All");
    tsetNames.addAll(tsetNames(database, siteInfo));
    params.accumulate("reportParameters", makeReportSelectOption("radio","Set name", "setname", tsetNames));
    ArrayList<String> details = new ArrayList<>();
    details.add("Yes");
    details.add("No");
    params.accumulate("reportParameters", makeReportSelectOption("radio", "Include patient details", "details", details));
    return params;
  }

  private ArrayList<String> tsetNames(Supplier<Database> dbp, SiteInfo siteInfo) {
    String sql = "select set_name from randomset where survey_site_id=?   ";
    return dbp.get().toSelect(sql).argLong(siteInfo.getSiteId()).query(rs -> {
      ArrayList<String> names = new ArrayList<>();
      while (rs.next()) {
        names.add(rs.getStringOrEmpty(1));
      }
      return names;
    });
  }

  private ArrayList<ArrayList<Object>> tsetReport(Database database, SiteInfo siteInfo, Date fromDate, final Date toDate, String name, String details) {

    boolean withDetails = "Yes".equals(details);
    if (withDetails) {
      return tsetDetailReport(database, siteInfo, fromDate, toDate, name);
    } else {
      return tsetReport(database, siteInfo, fromDate, toDate, name);
    }
  }

  private ArrayList<ArrayList<Object>> tsetReport(Database database, SiteInfo siteInfo, Date fromDate, final Date toDate, String name) {
    String trunc;
    if (database.get().flavor() == Flavor.oracle) {
      trunc = " trunc(?) ";

    } else  {
      trunc = " date_trunc('day', CAST(? as timestamp without time zone)) ";
    }
    String innerSql = "  SELECT set_name, stratum_name, 1 as countall, "
        + " case when dt_assigned >  " + trunc + "and state not in ('Withdrawn') then 1 else 0 end as countasgn, "
        + " case when dt_withdrawn > " + trunc  + " and state in ('Withdrawn') then 1 else 0 end as countwith "
        + "        FROM RandomSet_Participant WHERE state IN ('Assigned','Withdrawn','Completed') and survey_site_id = :site "
        + " and dt_assigned >= :from and dt_assigned <= :to ";
    StringBuilder sql = new StringBuilder();

    if (database.get().flavor() == Flavor.oracle) {
      sql.append("with tsetcounts as ");
    } else {
      sql.append("select set_name, stratum_name, sum(countall) as period_participants, sum(countasgn) as period_assigned, sum(countwith) as period_withdrawn, "
          + "(select count(*) from randomset_participant rp where state = 'Assigned' and rp.set_name = ts.set_name and rp.stratum_name = ts.stratum_name) as current_assigned, "
          + "(select count(*) from randomset_participant rp where state = 'Withdrawn' and rp.set_name = ts.set_name and rp.stratum_name = ts.stratum_name) as current_withdr, "
          + "(select count(*) from randomset_participant rp where state = 'Completed' and rp.set_name = ts.set_name and rp.stratum_name = ts.stratum_name) as current_compl "
          + " from  ");
    }
    sql.append("(");
    sql.append(innerSql);
    if (name != null && !name.equals("All")) {
      sql.append(" and set_name = ? ");
    }
    sql.append(")");
    if (database.get().flavor() == Flavor.oracle) {
      sql.append( " select set_name, stratum_name, sum(countall) as period_participants, sum(countasgn) as period_assigned, sum(countwith) as period_withdrawn, "
          + "(select count(*) from randomset_participant rp where state = 'Assigned' and rp.set_name = ts.set_name and rp.stratum_name = ts.stratum_name) as current_assigned, "
          + "(select count(*) from randomset_participant rp where state = 'Withdrawn' and rp.set_name = ts.set_name and rp.stratum_name = ts.stratum_name) as current_withdr, "
          + "(select count(*) from randomset_participant rp where state = 'Completed' and rp.set_name = ts.set_name and rp.stratum_name = ts.stratum_name) as current_compl "
          + "FROM tsetcounts ts"
          + " group by set_name, stratum_name ");
    } else {
      sql.append("as ts GROUP BY set_name, stratum_name  order by set_name, stratum_name ");
    }

    Date last7days = DateUtils.getDaysFromDate(siteInfo, toDate, -7);
    SqlSelect sqlSelect = database.toSelect(sql.toString())
        .argDate(last7days)
        .argDate(last7days)
        .argLong(":site", siteInfo.getSiteId())
        .argDate(":from", fromDate)
        .argDate(":to", toDate);
    if (name != null && !name.equals("All")) {
      sqlSelect = sqlSelect.argString(name);
    }

    return sqlSelect.query(rs -> {
      ArrayList<ArrayList<Object>> report = new ArrayList<>();
      /* headings */
      ArrayList<Object> head = new ArrayList<>();
      head.add("Set name");
      head.add("Stratum");
      head.add("Assigned in period ");
      head.add("Assigned in last 7 days");
      head.add("Withdrawn in last 7 days");
      head.add("Currently Assigned");
      head.add("Currently Withdrawn");
      head.add("Currently Completed");
      report.add(head);
      while (rs.next()) {
        ArrayList<Object> line = new ArrayList<>();
        line.add(rs.getStringOrEmpty(1));
        line.add(rs.getStringOrEmpty(2));
        line.add(rs.getStringOrEmpty(3));
        line.add(rs.getLongOrZero(4));
        line.add(rs.getLongOrZero(5));
        line.add(rs.getLongOrZero(6));
        line.add(rs.getLongOrZero(7));
        line.add(rs.getLongOrZero(8));
        report.add(line);
      }
      return report;
    });
  }
  private ArrayList<ArrayList<Object>> tsetDetailReport(Database database, SiteInfo siteInfo, Date fromDate, final Date toDate, String name) {

    StringBuilder sql = new StringBuilder();
    sql.append("SELECT rp.patient_id, last_name, first_name, set_name, stratum_name, dt_assigned, state "
        + "FROM randomSet_participant rp, patient p "
        + "WHERE p.patient_id = rp.patient_id and state IN ('Assigned','Withdrawn','Completed') "
        +  " and survey_site_id = :site and dt_assigned >= :from and dt_assigned <= :to ");

    if (name != null && !name.equals("All")) {
      sql.append(" and set_name = ? ");
    }
    sql.append("order by dt_assigned");
    SqlSelect sqlSelect = database.toSelect(sql.toString())
        .argLong(":site", siteInfo.getSiteId())
        .argDate(":from", fromDate)
        .argDate(":to", toDate);
    if (name != null && !name.equals("All")) {
      sqlSelect = sqlSelect.argString(name);
    }
    return sqlSelect.query(rs -> {
      ArrayList<ArrayList<Object>> report = new ArrayList<>();
      /* headings */
      ArrayList<Object> head = new ArrayList<>();
      head.add("MRN");
      head.add("Last Name");
      head.add("First Name");
      head.add("Set name");
      head.add("Stratum");
      head.add("Date - Time");
      head.add("State");

      report.add(head);
      while (rs.next()) {
        ArrayList<Object> line = new ArrayList<>();
        line.add(rs.getStringOrEmpty(1));
        line.add(rs.getStringOrEmpty(2));
        line.add(rs.getStringOrEmpty(3));
        line.add(rs.getStringOrEmpty(4));
        line.add(rs.getStringOrEmpty(5));
        Date assignedDate = rs.getDateOrNull(6);
        line.add(siteInfo.getDateFormatter().getDateString(assignedDate));
        line.add(rs.getStringOrEmpty(7));
        report.add(line);
      }
      return report;
    });
  }
}
