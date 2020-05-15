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
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.github.susom.database.Database;

public class ComplianceReport1 extends TabularReport {
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(ComplianceReport1.class);

  /*
  private String title = "Compliance by Month Report ";

  private int fontSize = 12;
  private static final int XMARGIN = 20;
  private static final int YMARGIN = 40;
  // private static final int TABLE_WIDTH = 130;
  private static final int CELL_MARGIN = 5;
  */

  private Database database;

  public ComplianceReport1(Database database) {
    super(database);
    this.database = database;
  }

  public ArrayList<ArrayList<Object>> getReportData(SiteInfo siteInfo) {
    ReportUtils utils = new ReportUtils(siteInfo);
    ArrayList<ArrayList<Object>> results = utils.complianceReport1(database);
    return createReport(results);
  }

  public PDDocument getPDF(SiteInfo siteInfo) throws IOException {
    ArrayList<ArrayList<Object>> report = getReportData(siteInfo);
    return makePdf(report);
  }

  public ArrayList<ArrayList<Object>> createReport(ArrayList<ArrayList<Object>> queryResults) {

    if (queryResults == null || queryResults.size() == 0) {
      return null;
    }

    ArrayList<ArrayList<Object>> report = new ArrayList<>();
    // for (int rIndx = 0; rIndx < queryResults.size(); rIndx++) {
    // data.add(new ReportData(queryResults.get(rIndx)));
    // }

    return report;

  }

  // TODO: What's this for?
  /*
  static private class ReportData {
    private SimpleDateFormat monthFormatter = new SimpleDateFormat("MMMMM");

    private String month;
    private int count;
    private String visitType;
    private String participates;


    public ReportData(ArrayList<String> line) {
      if (line == null) {
        return;
      }
      if (line.size() < 4) {
        return;
      }
      try {
        int monthNumber = Integer.valueOf(line.get(0));
        month = monthNumber + "";
      } catch (NumberFormatException nfe) {
      }

      try {
        count = Integer.valueOf(line.get(1));
      } catch (NumberFormatException nfe) {
      }

      visitType = line.get(2);
      participates = line.get(3);
    }

    public String getMonth() {
      if (month == null) {
        return null;
      }

      Calendar cal = Calendar.getInstance();
      try {
        return monthFormatter.format(monthFormatter.parse(month));
      } catch (ParseException e) {
        logger.debug("Invalid month value of " + month + " returned in ComplianceReport1");
      }
      return null;
    }

    public int getCount() {
      return count;
    }

    public String getParticipates() {
      return participates;
    }

    public String getVisitType() {
      return visitType;
    }
  }
  /* */
}
