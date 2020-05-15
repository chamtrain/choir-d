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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ComplianceReport implements ClinicReport {
  private final ClinicServiceAsync clinicService;

  private ClientUtils utils = null;
  private final Date fromDt;
  private final Date toDt;
  private final Logger logger = Logger.getLogger(ComplianceReport.class.getName());

  public ComplianceReport(ClientUtils utils, ClinicServiceAsync clinicService, Date fromDt, Date toDt) {
    this.utils = utils;
    this.clinicService = clinicService;
    this.fromDt = fromDt;
    this.toDt = toDt;
  }

  @Override
  public Widget getBody() {
    final FlexTable reportBody = new FlexTable();
    reportBody.getRowFormatter().setStylePrimaryName(0, "tableDataHeader");
    reportBody.setStylePrimaryName("fixedList");
    reportBody.addStyleName("dataList");
    reportBody.addStyleName("report");
    reportBody.setBorderWidth(2);
    reportBody.setCellSpacing(6);
    reportBody.setCellPadding(4);

    for (int indx = 0; indx < Constants.COMPLIANCE1_RPT_HEADERS.length; indx++) {
      reportBody.getCellFormatter().setWidth(0, indx, "200px");
      Label headerLabel = new Label(Constants.COMPLIANCE1_RPT_HEADERS[indx]);
      headerLabel.setStylePrimaryName("tableDataHeaderLabel");
      reportBody.setWidget(0, indx, headerLabel);
    }
    reportBody.getCellFormatter().addStyleName(0, 0, "first-child");
    reportBody.getCellFormatter().addStyleName(0, Constants.COMPLIANCE1_RPT_HEADERS.length - 1, "last-child");

    logger.log(Level.INFO, "running compliance report from " + utils.getDateString(fromDt) + " to "
        + utils.getDateString(toDt));
    clinicService.complianceSummaryReport(fromDt, toDt, new Callback<ArrayList<ArrayList<Object>>>() {

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
    return reportBody;
  }

  @Override
  public HorizontalPanel getHeader() {
    HorizontalPanel headerPanel = new HorizontalPanel();
    Label title = new Label("Compliance Report " + utils.getDateString(fromDt) + " through "
        + utils.getDateString(toDt));
    title.setStyleName("title");
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
    return "";
  }

  @Override
  public String getRptName() {
    return "cr1";
  }

  @Override
  public boolean xcelDownload() {
    return false;
  }
}
