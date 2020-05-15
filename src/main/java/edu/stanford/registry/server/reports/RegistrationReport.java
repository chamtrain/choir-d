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

package edu.stanford.registry.server.reports;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.utils.ReportUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.github.susom.database.Database;

public class RegistrationReport extends TabularReport {
  private static Logger logger = Logger.getLogger(RegistrationReport.class);

  @SuppressWarnings("unused")
  private String title = "Patient Registration Report ";

  private String SECTION0 = "Patients with a Visit";
  private String SECTION1 = "New Patients";
  private String SECTION2 = "Returning Patients";
  private String[] SUBCATEGORIES = { "Registered", "Declined", "Neither" };

  private Database database;
  private Date startDt;
  private Date endDt;
  private SimpleDateFormat headingInFormat = new SimpleDateFormat("yyyy-MM");
  private SimpleDateFormat headingOutFormat = new SimpleDateFormat("MMM yyyy");
  private DecimalFormat percentFormatter = new DecimalFormat("###.##");

  /**
   * This creates the "Patient Registration Report"
   */
  public RegistrationReport(Database database, Date startDt, Date endDt) {
    super(database);
    this.database = database;
    this.startDt = startDt;
    this.endDt = endDt;
  }

  public ArrayList<ArrayList<Object>> getReportData(SiteInfo siteInfo) {
    ReportUtils utils = new ReportUtils(siteInfo);
    ArrayList<ArrayList<Object>> allResults = utils.complianceReport2(database, false, startDt, endDt);
    ArrayList<ArrayList<Object>> newResults = utils.complianceReport2(database, true, startDt, endDt);
    return createReport(allResults, newResults);
  }

  public PDDocument getPDF(SiteInfo siteInfo) throws IOException {
    ArrayList<ArrayList<Object>> report = getReportData(siteInfo);
    return makePdf(report);
  }

  public ArrayList<ArrayList<Object>> createReport(ArrayList<ArrayList<Object>> allResults,
                                                   ArrayList<ArrayList<Object>> newResults) {

    if (allResults == null || allResults.size() == 0) {
      return null;
    }

    ArrayList<ArrayList<Object>> report = new ArrayList<>();

    // create headings
    ArrayList<String> months = new ArrayList<>();
    ArrayList<Object> headings = new ArrayList<>();
    headings.add("");
    String lastHeading = null;
    for (ArrayList<Object> allResult : allResults) {
      if (allResult != null && allResult.size() > 0) {
        String rowHeading = (String) allResult.get(0);
        if (lastHeading == null || !lastHeading.equals(rowHeading)) {
          lastHeading = rowHeading;
          try {
            Date headingDate = headingInFormat.parse(rowHeading);
            headings.add(headingOutFormat.format(headingDate));
            logger.debug("heading for " + rowHeading + " dt=" + headingDate.toString() + " name="
                + headingOutFormat.format(headingDate));
          } catch (ParseException e) {
            logger.debug("parseError in patient registration report for date value  " + rowHeading);
            headings.add(rowHeading);
          }
          headings.add("%");
          months.add(lastHeading);
        }
      }
    }
    report.add(headings);
    ArrayList<Object> blankLine = new ArrayList<>(); // empty row
    for (int c = 0; c < headings.size(); c++) {
      blankLine.add(" ");
    }

    // fill in report
    int numberColumns = (months.size());

    /* Start with all counts */
    Integer[] registered = getReportRowCounts(numberColumns);
    Integer[] declined = getReportRowCounts(numberColumns);
    Integer[] neither = getReportRowCounts(numberColumns);
    Integer[] totals = getReportRowCounts(numberColumns);

    for (ArrayList<Object> row : allResults) {
      int indx = -1;
      if (row != null && row.size() == 3) {
        indx = getIndex(months, row.get(0).toString());
        if (indx < 0 || indx > totals.length - 1) {
          logger.error("Invalid report cell index for " + row.get(0) + " not including data");
        } else {

          Integer value = Integer.parseInt(row.get(1).toString());
          totals[indx] = totals[indx] + value;
          if (row.get(2) != null && row.get(2).toString().toLowerCase().equals("y")) {
            registered[indx] = registered[indx] + value;
          } else if (row.get(2) != null && row.get(2).toString().toLowerCase().equals("n")) {
            declined[indx] = declined[indx] + value;
          } else {
            neither[indx] = neither[indx] + value;
          }
        }
      }

    }
    // write the totals to the report
    report.add(writeRow(SECTION0, totals, totals));
    report.add(writeRow(SUBCATEGORIES[0], registered, totals));
    report.add(writeRow(SUBCATEGORIES[1], declined, totals));
    report.add(writeRow(SUBCATEGORIES[2], neither, totals));
    report.add(blankLine);

    /* Now do the new patients */
    Integer[] registeredNew = getReportRowCounts(numberColumns);
    Integer[] declinedNew = getReportRowCounts(numberColumns);
    Integer[] neitherNew = getReportRowCounts(numberColumns);
    Integer[] totalsNew = getReportRowCounts(numberColumns);
    for (ArrayList<Object> row : newResults) {
      int indx = -1;
      if (row != null && row.size() == 3) {
        indx = getIndex(months, row.get(0).toString());
        if (indx < 0 || indx > totals.length - 1) {
          logger.error("Invalid report cell index for " + row.get(0) + " not including data");
        } else {
          Integer value = Integer.parseInt(row.get(1).toString());
          totalsNew[indx] = totalsNew[indx] + value;
          if (row.get(2) != null && row.get(2).toString().toLowerCase().equals("y")) {
            registeredNew[indx] = registeredNew[indx] + value;
          } else if (row.get(2) != null && row.get(2).toString().toLowerCase().equals("n")) {
            declinedNew[indx] = declinedNew[indx] + value;
          } else {
            neitherNew[indx] = neitherNew[indx] + value;
          }
        }
      }
    }
    report.add(writeRow(SECTION1, totalsNew, totalsNew));
    report.add(writeRow(SUBCATEGORIES[0], registeredNew, totalsNew));
    report.add(writeRow(SUBCATEGORIES[1], declinedNew, totalsNew));
    report.add(writeRow(SUBCATEGORIES[2], neitherNew, totalsNew));
    report.add(blankLine);

    /* Calculate returning patients as (all - new) */
    for (int c = 0; c < totals.length; c++) {
      totals[c] = totals[c] - totalsNew[c];
      registered[c] = registered[c] - registeredNew[c];
      declined[c] = declined[c] - declinedNew[c];
      neither[c] = neither[c] - neitherNew[c];
    }
    report.add(writeRow(SECTION2, totals, totals));
    report.add(writeRow(SUBCATEGORIES[0], registered, totals));
    report.add(writeRow(SUBCATEGORIES[1], declined, totals));
    report.add(writeRow(SUBCATEGORIES[2], neither, totals));
    return report;
  }

  private ArrayList<Object> writeRow(String heading, Integer[] values, Integer[] totals) {
    ArrayList<Object> row = new ArrayList<>();
    row.add(heading);
    for (int r = 0; r < values.length; r++) {
      row.add(values[r]);
      // calculate the % against the total
      if (totals[r] == 0) {
        row.add("0");
      } else {
        double percentage = ((values[r] * 1.00) / (totals[r] * 1.00)) * 100;
        row.add(percentFormatter.format(percentage));
      }
    }
    return row;
  }

  private Integer[] getReportRowCounts(int numberColumns) {
    Integer[] rowValues = new Integer[numberColumns];
    for (int r = 0; r < rowValues.length; r++) {
      rowValues[r] = 0;
    }
    return rowValues;
  }

  private int getIndex(ArrayList<String> months, String data) {
    if (months == null || data == null) {
      return -1;
    }

    for (int m = 0; m < months.size(); m++) {

      if (data.equals(months.get(m))) {
        return m;
      }

    }
    return -1;
  }

}
