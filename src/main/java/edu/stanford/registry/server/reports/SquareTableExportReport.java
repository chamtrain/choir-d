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
import edu.stanford.registry.shared.InvalidDataElementException;

import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.SqlSelect;

/**
 * This class creates the Scores Export Report. The Scores Export Report has
 * one row for each assessment. Each row will have one or more columns
 * for each of the studies in the report which will contain the score(s) for
 * that study.
 */
public class SquareTableExportReport {

  private static final Logger logger = LoggerFactory.getLogger(SquareTableExportReport.class);

  final private Database database;
  final private SiteInfo siteInfo;

  public SquareTableExportReport(Database database, SiteInfo siteInfo) {
    this.database = database;
    this.siteInfo = siteInfo;
  }

  public ArrayList<String> getReportColumns(String tableName) {

    if (!valid(tableName)) {
      logger.warn(".getReportColumns was called for unknown table: {}", tableName);
      return new ArrayList<>();
    }

    String sql =
        "select * from " + tableName
            + " where survey_site_id = :site  and survey_token_id = "
            + "(select min(survey_token_id) from  " + tableName + " where survey_site_id = :site)";
    SqlSelect sqlSelect = database.get().toSelect(sql)
        .argLong(":site", siteInfo.getSiteId());

    return sqlSelect.query(rs -> {

      ArrayList<String> columns = new ArrayList<>();
      if (rs.next()) {
        ResultSetMetaData metaData = rs.getMetadata();
        logger.debug("metaData has " + metaData.getColumnCount() + " columns");
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
          columns.add(metaData.getColumnName(i));
        }
      }
      return columns;
    });
  }


  /**
   * Return a table for the scores data. The first row in the table will be a
   * header row. Each subsequent row will contain the study scores for a
   * survey registration.
   *
   * @return A list of rows where each row is a list of objects
   */
  public ArrayList<ArrayList<Object>> getReportData(String tableName) {
    return getReportData(tableName, "*");
  }

  public ArrayList<ArrayList<Object>> getReportData(String tableName, ArrayList<String> columns) throws InvalidDataElementException {
    ArrayList<String> allColumns = getReportColumns(tableName);
    return getReportData(tableName, makeColumnList(allColumns, columns));
  }

  private ArrayList<ArrayList<Object>> getReportData(String tableName, String columns) {
    logger.debug("Fetching scores report data.");
    if (!valid(tableName)) {
      return new ArrayList<>();
    }

    String sql =
        "select " + columns + " from " + tableName
            + " where survey_site_id = :site "; // + " where survey_token_id = (select min(survey_token_id) from " + tableName + ")";
    SqlSelect sqlSelect = database.get().toSelect(sql)
        .argLong(":site", siteInfo.getSiteId());
    ArrayList<ArrayList<Object>> results = createSquareTableReport(sqlSelect);
    if (results == null || results.size() == 0) {
      return null;
    }

    return results;
  }

  public ArrayList<ArrayList<Object>> getReportData(String tableName, Date fromDate, Date toDate) {

      return getReportData(tableName, "*", fromDate, toDate);
  }

  public ArrayList<ArrayList<Object>> getReportData(String tableName, Date fromDate, Date toDate, ArrayList<String> columns)
  throws InvalidDataElementException {
    ArrayList<String> allColumns = getReportColumns(tableName);
    return getReportData(tableName, makeColumnList(allColumns, columns), fromDate, toDate);
  }

  private ArrayList<ArrayList<Object>> getReportData(String tableName, String columns, Date fromDate, Date toDate) {

    String sql = "select " + columns + " from " + tableName + " sq where sq.survey_site_id = :site_id1 and survey_token_id in ("
        + " select survey_token_id from survey_token st, survey_registration sr "
        + "  where st.survey_site_id = :site_id2 and st.survey_token = sr.token   "
        + "    and st.survey_site_id = sr.survey_site_id "
        + "    and sr.survey_dt between :from and :to )";
    SqlSelect sqlSelect = database.get().toSelect(sql)
        .argLong(":site_id1", siteInfo.getSiteId())
        .argLong(":site_id2", siteInfo.getSiteId())
        .argDate(":from", fromDate)
        .argDate(":to", toDate);
    return createSquareTableReport(sqlSelect);
  }

  private ArrayList<ArrayList<Object>> createSquareTableReport(SqlSelect sqlSelect) {

    return sqlSelect.query(rs -> {
      ArrayList<ArrayList<Object>> report = new ArrayList<>();
      ArrayList<Object> columns = new ArrayList<>();
      while (rs.next()) {
        ResultSetMetaData metaData = rs.getMetadata();
        if (report.size() < 1) { // add the header row
          logger.debug("metaData has " + metaData.getColumnCount() + " columns");
          for (int i = 1; i <= metaData.getColumnCount(); i++) {
            columns.add(metaData.getColumnName(i));
          }
          report.add(columns);
        }

        ArrayList<Object> line = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
          switch (metaData.getColumnType(i)) {
          case Types.CHAR:
          case Types.VARCHAR:
            line.add(rs.getStringOrEmpty(i));
            break;
          case Types.DATE:
            Date dt = rs.getDateOrNull(i);
            if (dt == null) {
              line.add("");
            } else {
              line.add(siteInfo.getDateOnlyFormatter().getDateString(dt));
            }
            break;
          case Types.TIME:
          case Types.TIMESTAMP:
            Date tm = rs.getDateOrNull();
            if (tm == null) {
              line.add("");
            } else {
              line.add(siteInfo.getDateFormatter().getDateString(tm));
            }
            break;
          case Types.INTEGER:
            Integer intg = rs.getIntegerOrNull(i);
            if (intg == null) {
              line.add("");
            } else {
              line.add(intg);
            }
            break;
          case Types.NUMERIC:
          case Types.BIGINT: // Postgres
            Long dec = rs.getLongOrNull();
            //BigDecimal dec = rs.getBigDecimalOrNull();
            if (dec == null) {
              line.add("");
            } else {
              line.add(dec);
            }
            break;
          case Types.BLOB:
            byte[] blob = rs.getBlobBytesOrNull();
            if (blob == null) {
              line.add("");
            } else {
              line.add(new String(blob));
            }
          case Types.CLOB:
            line.add(rs.getClobStringOrEmpty());
          }
        }
        report.add(line);
      }

      return report;
    });
  }

  private String makeColumnList(ArrayList<String> allColumns, ArrayList<String> columns) throws InvalidDataElementException {
    if (columns == null || columns.size() < 1) {
      throw new InvalidDataElementException("Invalid empty column name list");
    }

    StringBuilder columnString = new StringBuilder();
    columnString.append(validated(allColumns, columns.get(0)));

    // validate the rest of the columns
    for (int colInx = 1; colInx< columns.size(); colInx++) {
      columnString.append(", ");
      columnString.append(validated(allColumns, columns.get(colInx)));
    }
    return columnString.toString();
  }

  private String validated(ArrayList<String> allColumns, String column) throws InvalidDataElementException {
    if (!allColumns.contains(column)) {
      throw new InvalidDataElementException("Invalid column name " + column );
    }
    return column;
  }

  private boolean valid(String tableName ) {
    ArrayList<String> siteTables = siteInfo.getRegistryCustomizer().apiExportTables();
    if (siteTables != null ) {
      for (String table : siteTables) {
        if (tableName.equalsIgnoreCase(table)) {
          return true;
        }
      }
    }
    return false;
  }
}