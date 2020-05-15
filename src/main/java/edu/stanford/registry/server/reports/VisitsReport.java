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
import java.util.Collections;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.github.susom.database.Database;

public class VisitsReport extends TabularReport {
  private static Logger logger = Logger.getLogger(VisitsReport.class);

  private String SECTION0 = "Total Visits";
  private String SECTION1 = "Survey Eligible";
  private String SECTION2 = "Not Eligible";
  private String[] SUBCATEGORIES1 = { "Started and Completed", "Started but not Completed", "Not Started" };
  private String[] SUBCATEGORIES2 = { "Not Registered or Declined", "Declined", "Registered but Suppressed" };

  private Database database;
  private Date startDt;
  private Date endDt;
  private SimpleDateFormat headingInFormat = new SimpleDateFormat("yyyy-MM");
  private SimpleDateFormat headingOutFormat = new SimpleDateFormat("MMM yyyy");
  private DecimalFormat percentFormatter = new DecimalFormat("###.##");

  private SiteInfo siteInfo;

  /**
   * This creates the "Patient Visits Report"
   */
  public VisitsReport(Database database, Date startDt, Date endDt, SiteInfo siteInfo) {
    super(database);
    this.database = database;
    this.startDt = startDt;
    this.endDt = endDt;
    this.siteInfo = siteInfo;
  }

  public ArrayList<ArrayList<Object>> getReportData() {
    ReportUtils utils = new ReportUtils(siteInfo);
    ArrayList<ArrayList<Object>> eligibleResults = utils.eligibleVisitsReport(database, startDt, endDt);
    ArrayList<ArrayList<Object>> inEligibleResults = utils.inEligibleVisitsReport(database, startDt, endDt);
    return createReport(eligibleResults, inEligibleResults);
  }

  public PDDocument getPDF() throws IOException {
    ArrayList<ArrayList<Object>> report = getReportData();
    return makePdf(report);
  }

  private ArrayList<ArrayList<Object>> createReport(ArrayList<ArrayList<Object>> eligibleResults,
                                                   ArrayList<ArrayList<Object>> inEligibleResults) {

    if ((eligibleResults.size() == 0) && (inEligibleResults.size() == 0)) {
      return null;
    }

    ArrayList<ArrayList<Object>> report = new ArrayList<>();

    // create headings
    ArrayList<String> months = new ArrayList<>();
    ArrayList<Object> headings = new ArrayList<>();
    headings.add("");
    String lastHeading = null;

    ArrayList<Date> headingDates = new ArrayList<>();
    for (ArrayList<Object> eligibleResult : eligibleResults) {
      String rowHeading = (String) eligibleResult.get(0);
      if (lastHeading == null || !lastHeading.equals(rowHeading)) {
        lastHeading = rowHeading;
        months.add(lastHeading);
        try {
          Date headingDate = headingInFormat.parse(rowHeading);
          headingDates.add(headingDate);
        } catch (ParseException e) {
          logger.error("parseError in patient visits report for date value  " + rowHeading);
        }
      }
    }

    for (ArrayList<Object> inEligibleResult : inEligibleResults) {
      String rowHeading = (String) inEligibleResult.get(0);
      try {
        Date headingDate = headingInFormat.parse(rowHeading);

        if (!headingDates.contains(headingDate)) {
          headingDates.add(headingDate);
          months.add(rowHeading);
        }
      } catch (ParseException e) {
        logger.error("parseError in patient visits report for date value  " + rowHeading);
      }
    }
    Collections.sort(headingDates);
    Collections.sort(months);

    for (Date headingDate : headingDates) {
      if (headingDate != null) {
        headings.add(headingOutFormat.format(headingDate));
        headings.add("%");
      }
    }
    report.add(headings);
    ArrayList<Object> blankLine = new ArrayList<>(); // empty row
    for (int c = 0; c < headings.size(); c++) {
      blankLine.add("");
    }

    // fill in report
    int numberColumns = (months.size());

    /* Start with eligible counts */
    Integer[] totalEligible = getReportRowCounts(numberColumns);
    Integer[] totalCompleted = getReportRowCounts(numberColumns);
    Integer[] totalUncompleted = getReportRowCounts(numberColumns);
    Integer[] totalUnstarted = getReportRowCounts(numberColumns);

    for (ArrayList<Object> row : eligibleResults) {
      int indx = -1;
      if (row != null && row.size() == 5) {
        indx = getIndex(months, row.get(0).toString());
        if (indx < 0 || indx > totalEligible.length - 1) {
          logger.error("Invalid report cell index for " + row.get(0) + " not including data");
        } else {

          totalEligible[indx] = totalEligible[indx] + Integer.parseInt(row.get(1).toString());
          totalCompleted[indx] = totalCompleted[indx] + Integer.parseInt(row.get(2).toString());
          totalUncompleted[indx] = totalUncompleted[indx] + Integer.parseInt(row.get(3).toString());
          totalUnstarted[indx] = totalUnstarted[indx] + Integer.parseInt(row.get(4).toString());

        }
      }

    }

    /* Do the non eligible */
    Integer[] totalNotEligible = getReportRowCounts(numberColumns);
    Integer[] totalNotAsked = getReportRowCounts(numberColumns);
    Integer[] totalDeclined = getReportRowCounts(numberColumns);
    Integer[] totalSuppressed = getReportRowCounts(numberColumns);
    for (ArrayList<Object> row : inEligibleResults) {
      int indx = -1;
      if (row != null && row.size() == 5) {
        indx = getIndex(months, row.get(0).toString());
        if (indx < 0 || indx > totalEligible.length - 1) {
          logger.error("Invalid report cell index for " + row.get(0) + " not including data");
        } else {
          totalNotEligible[indx] = totalNotEligible[indx] + Integer.parseInt(row.get(1).toString());
          totalNotAsked[indx] = totalNotAsked[indx] + Integer.parseInt(row.get(2).toString());
          totalDeclined[indx] = totalDeclined[indx] + Integer.parseInt(row.get(3).toString());
          totalSuppressed[indx] = totalSuppressed[indx] + Integer.parseInt(row.get(4).toString());

        }
      }

    }
    Integer[] totals = getReportRowCounts(numberColumns);
    for (int indx = 0; indx < totalEligible.length; indx++) {
      totals[indx] = totalEligible[indx] + totalNotEligible[indx];
    }
    // write the totals to the report
    report.add(writeRow(SECTION0, totals, totals));
    report.add(writeRow(SECTION1, totalEligible, totals));
    report.add(writeRow(SUBCATEGORIES1[0], totalCompleted, totalEligible));
    report.add(writeRow(SUBCATEGORIES1[1], totalUncompleted, totalEligible));
    report.add(writeRow(SUBCATEGORIES1[2], totalUnstarted, totalEligible));
    report.add(blankLine);

    report.add(writeRow(SECTION2, totalNotEligible, totals));
    report.add(writeRow(SUBCATEGORIES2[0], totalNotAsked, totalNotEligible));
    report.add(writeRow(SUBCATEGORIES2[1], totalDeclined, totalNotEligible));
    report.add(writeRow(SUBCATEGORIES2[2], totalSuppressed, totalNotEligible));
    report.add(blankLine);

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
