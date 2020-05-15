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

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class PatientRegistrationReport implements ClinicReport {
  private final ClinicServiceAsync clinicService;

  private ClientUtils utils = null;
  private Date fromDt, toDt;
  private final Logger logger = Logger.getLogger(PatientRegistrationReport.class.getName());

  public PatientRegistrationReport(ClientUtils utils, ClinicServiceAsync clinicService, Date fromDt, Date toDt) {
    this.utils = utils;
    this.clinicService = clinicService;
    this.fromDt = fromDt;
    this.toDt = toDt;
  }

  @Override
  public Widget getBody() {
    final FlexTable reportBody = new FlexTable();

    reportBody.getRowFormatter().setStylePrimaryName(0, "tableDataHeader");
    reportBody.setStylePrimaryName("dataList");

    reportBody.addStyleName("report");
    reportBody.setBorderWidth(2);
    reportBody.setCellSpacing(6);
    reportBody.setCellPadding(4);

    // headerLabel.setStylePrimaryName("tableDataHeaderLabel");

    // reportBody.getCellFormatter().addStyleName(0, HEADERS.length - 1, "last-child");

    logger.log(Level.INFO, "running registration report from " + utils.getDateString(fromDt) + " to "
        + utils.getDateString(toDt));
    clinicService.enrollmentReportData(fromDt, toDt, new Callback<ArrayList<ArrayList<Object>>>() {

      @Override
      public void handleSuccess(ArrayList<ArrayList<Object>> result) {
        if (result != null && result.size() > 0) {

          for (int row = 0; row < result.size(); row++) {
            ArrayList<Object> line = result.get(row);
            if (line != null && line.size() > 1) {
              Label lbl1 = new Label(line.get(0).toString());
              if (row == 0) {
                lbl1.setStylePrimaryName("tableDataHeaderLabel"); // top row
              } else {
                lbl1.setStylePrimaryName("boldReportLabel");
              }
              reportBody.setWidget(row, 0, lbl1);
              reportBody.getCellFormatter().addStyleName(row, 0, "first-child");

              for (int col = 1; col < line.size(); col++) {
                if (row == 0) { // Top row are the headings
                  Label lbl2 = new Label(line.get(col).toString());
                  lbl2.setStylePrimaryName("tableDataHeaderLabel");
                  reportBody.setWidget(row, col, lbl2);
                  reportBody.getColumnFormatter().setWidth(0, "200px");

                } else {
                  Label numberLabel = new Label(line.get(col).toString());
                  numberLabel.setStylePrimaryName("numericReportData ");
                  reportBody.setWidget(row, col, numberLabel);
                  reportBody.getColumnFormatter().setWidth(col, "55px");
                  reportBody.getCellFormatter().setStylePrimaryName(row, col, "numericReportData");
                  // reportBody.getCellFormatter().addStyleName(row, col, );
                  // reportBody.getCellFormatter().setAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT,
                  // HasVerticalAlignment.ALIGN_MIDDLE);

                }
              }

              reportBody.getCellFormatter().addStyleName(row, line.size() - 1, "last_child");
            }
          }
        }

      }

    });
    return reportBody;
  }

  @Override
  public HorizontalPanel getHeader() {
    HorizontalPanel headerPanel = new HorizontalPanel();
    Label title = new Label("Patient Registrations " + utils.getDateString(fromDt) + " through "
        + utils.getDateString(toDt));
    title.setStyleName("titleLabel");
    headerPanel.setHorizontalAlignment(headerPanel.getHorizontalAlignment());
    headerPanel.add(title);
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
    return "registration";
  }

  @Override
  public boolean xcelDownload() {
    return true;
  }

}
