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

package edu.stanford.registry.client.reports;

import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.shared.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class AverageSurveyTimeReport implements ClinicReport {
  private final ClinicServiceAsync clinicService;

  private ClientUtils utils = null;
  private final Date fromDt;
  private final Date toDt;
  private final String[] HEADERS;
  private final Logger logger = Logger.getLogger(AverageSurveyTimeReport.class.getName());
  private int reportType = 0;
  public static final int REPORT_SUMMARY = 0;
  public static final int REPORT_BYMONTH = 1;
  public static final int REPORT_BYTYPE = 2;

  public AverageSurveyTimeReport(ClientUtils utils, ClinicServiceAsync clinicService, Date fromDt, Date toDt, int reportType) {
    this.utils = utils;
    this.clinicService = clinicService;
    this.fromDt = fromDt;
    this.toDt = toDt;
    this.reportType = reportType;
    switch (reportType) {
    case REPORT_BYMONTH:
      HEADERS = Constants.AVG_TIME_RPT_MONTH_HEADERS;
      break;
    case REPORT_BYTYPE:
      HEADERS = Constants.AVG_TIME_RPT_TYPE_HEADERS;
      break;
    default:
      HEADERS = Constants.AVG_TIME_RPT_SUMM_HEADERS;
      break;
    }
  }

  @Override
  public Widget getBody() {
    FlowPanel rptPanel = new FlowPanel();
    rptPanel.setStyleName("centerPanel");
    int width = 200 * HEADERS.length;
    rptPanel.setWidth(width + "px");
    final FlexTable reportBody = new FlexTable();
    reportBody.getRowFormatter().setStylePrimaryName(0, "tableDataHeader");
    reportBody.setStylePrimaryName("fixedList");
    reportBody.addStyleName("dataList");
    reportBody.addStyleName("report");
    reportBody.setBorderWidth(2);
    reportBody.setCellSpacing(6);
    reportBody.setCellPadding(4);

    for (int indx = 0; indx < HEADERS.length; indx++) {
      reportBody.getCellFormatter().setWidth(0, indx, "180px");
      Label headerLabel = new Label(HEADERS[indx]);
      headerLabel.setStylePrimaryName("tableDataHeaderLabel");
      reportBody.setWidget(0, indx, headerLabel);
    }
    reportBody.getCellFormatter().addStyleName(0, 0, "first-child");
    reportBody.getCellFormatter().addStyleName(0, HEADERS.length - 1, "last-child");
    if (reportType == REPORT_SUMMARY) {
      logger.log(Level.INFO,
          "running averageSurveyTimeReport from " + utils.getDateString(fromDt) + " to " + utils.getDateString(toDt));
      clinicService.averageSurveyTimeReport(fromDt, toDt, new Callback<ArrayList<ArrayList<Object>>>() {

        @Override
        public void handleSuccess(ArrayList<ArrayList<Object>> result) {
          if (result != null && result.size() > 0) {
            for (int row = 0; row < result.size(); row++) {
              ArrayList<Object> line = result.get(row);
              if (line != null && line.size() > 0) {
                for (int col = 0; col < line.size(); col++) {
                  reportBody.setText(row + 1, col, line.get(col).toString());
                }
              }
            }
          }

        }

      });
    } else if (reportType == REPORT_BYMONTH) {
      logger.log(Level.INFO, "running averageSurveyTimeReportByMonth from " + utils.getDateString(fromDt) + " to "
          + utils.getDateString(toDt));
      clinicService.averageSurveyTimeReportByMonth(fromDt, toDt, new Callback<ArrayList<ArrayList<Object>>>() {

        @Override
        public void handleSuccess(ArrayList<ArrayList<Object>> result) {
          if (result != null && result.size() > 0) {
            for (int row = 0; row < result.size(); row++) {
              ArrayList<Object> line = result.get(row);
              if (line != null && line.size() > 0) {
                for (int col = 0; col < line.size(); col++) {
                  reportBody.setText(row + 1, col, line.get(col).toString());
                }
              }
            }
          }

        }

      });
    } else if (reportType == REPORT_BYTYPE) {
      logger.log(Level.INFO, "running averageSurveyTimeReportByType from " + utils.getDateString(fromDt) + " to "
          + utils.getDateString(toDt));
      clinicService.averageSurveyTimeReportByType(fromDt, toDt, new Callback<ArrayList<ArrayList<Object>>>() {

        @Override
        public void handleSuccess(ArrayList<ArrayList<Object>> result) {
          if (result != null && result.size() > 0) {
            for (int row = 0; row < result.size(); row++) {
              ArrayList<Object> line = result.get(row);
              if (line != null && line.size() > 0) {
                for (int col = 0; col < line.size(); col++) {
                  reportBody.setText(row + 1, col, line.get(col).toString());
                }
              }
            }
          }

        }

      });
    }
    // return reportBody;
    rptPanel.add(reportBody);
    return rptPanel;
  }

  @Override
  public HorizontalPanel getHeader() {
    HorizontalPanel headerPanel = new HorizontalPanel();
    Label title = new Label("Survey Times Report " + utils.getDateString(fromDt) + " through "
        + utils.getDateString(toDt));

    title.setStyleName("titleLabel");
    headerPanel.setHorizontalAlignment(headerPanel.getHorizontalAlignment());
    headerPanel.add(title);
    headerPanel.setStyleName("centerPanel");
    return headerPanel;
  }

  @Override
  public HorizontalPanel getFooter() {
    return null;
  }

  @Override
  public boolean hasHeader() {
    return true;
  }

  @Override
  public boolean hasFooter() {
    return false;
  }

  @Override
  public String getRequestParameters() {
    StringBuilder sb = new StringBuilder();
    sb.append("startDt=");
    sb.append(utils.getDateString(new java.util.Date(fromDt.getTime())));
    sb.append("&endDt=");
    sb.append(utils.getDateString(new java.util.Date(toDt.getTime())));
    return sb.toString();
  }

  @Override
  public String getRptName() {
    switch (reportType) {
    case REPORT_BYMONTH:
      return "AverageSurveyTimeByMonth";
    case REPORT_BYTYPE:
      return "AverageSurveyTimeByType";
    default:
      return "AverageSurveyTime";
    }
  }

  @Override
  public boolean xcelDownload() {
    return true;
  }
}
