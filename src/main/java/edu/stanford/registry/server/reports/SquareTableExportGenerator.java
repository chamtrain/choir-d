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
import edu.stanford.registry.server.service.ReportGenerator;
import edu.stanford.registry.shared.CommonUtils;
import edu.stanford.registry.shared.Constants;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import com.github.susom.database.Database;

public class SquareTableExportGenerator implements ReportGenerator {

  private final String table;
  public SquareTableExportGenerator(String tableName) {
    this.table = tableName;
  }

  @Override
  public String createReport(Database database, Date fromDate, Date toDate, SiteInfo siteInfo) {
    String TD = "<td style='border:1px solid black; border-collapse:collapse; text-align:right; padding-right:5px'>%s</td>\n";

    SquareTableExportReport report = new SquareTableExportReport(database.get(), siteInfo);
    ArrayList<ArrayList<Object>> reportData = report.getReportData(table, fromDate, toDate);
    StringWriter strWriter = new StringWriter();
    PrintWriter out = new PrintWriter(strWriter);
    out.println("<h3>Survey responses</h3>");
    out.println("<table style='margin:auto; border:1px solid black; border-collapse:collapse'>");
    if (reportData.size() > 0) {
      ArrayList<Object> rowData;
      out.print("<tr>");
      rowData = reportData.get(0);
      if (rowData != null) {
        for (Object aRowData : rowData) {
          out.print("<th style='border:1px solid black; '>");
          out.print(aRowData.toString());
          out.print("</th>");
        }
      }
      out.println("</tr>");
      for (int row = 1; row < reportData.size(); row++) {
        out.print("<tr>");
        rowData = reportData.get(row);
        if (rowData != null) {
          for (Object value : rowData) {
            String valueStr = value == null ? "" : value.toString();
            out.printf(TD, valueStr);
          }
        }
        out.println("</tr>");
      }

      out.print("<a href=\"");
      StringBuilder urlString = new StringBuilder();
      urlString.append(siteInfo.getProperty("chart.url", "/registry/registry/svc/chart"));
      urlString.append("?")
          .append(Constants.SITE_ID).append('=').append(siteInfo.getSiteName())
          .append("&table=").append(table)
          .append("&rpt=expsq")
      ;
      String dateFormatStr = siteInfo.getProperty("default.dateFormat");
      if (dateFormatStr == null) {
        dateFormatStr = CommonUtils.DFMT;
      }
      urlString.append("&fmt=").append(dateFormatStr).append("&startDt=").append(siteInfo.getDateOnlyFormatter().getDateString(fromDate));
      urlString.append("&endDt=").append(siteInfo.getDateOnlyFormatter().getDateString(toDate));
      out.print(urlString.toString() + "\">Export spreadsheet");
    }
    return strWriter.toString();

  }
}

