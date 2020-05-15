/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class PatientSurveysReport implements ClinicReport {
  private final ClinicServiceAsync clinicService;

  private final ClientUtils utils;
  private final Date fromDt;
  private final Date toDt;
  private static final String[] HEADERS = {"MRN", "First Name", "Last Name", "Email Address", "Survey Date", "Date Started", "Date Completed"};
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();

  public PatientSurveysReport(ClientUtils utils, ClinicServiceAsync clinicService, Date fromDt, Date toDt) {
    this.utils = utils;
    this.clinicService = clinicService;
    this.fromDt = fromDt;
    this.toDt = toDt;

  }

  @Override
  public Widget getBody() {
    FlowPanel rptPanel = new FlowPanel();
    rptPanel.setStyleName(css.centerPanel());

    rptPanel.setWidth("1000px");
    final FlexTable reportBody = new FlexTable();

    reportBody.getRowFormatter().setStylePrimaryName(0, "tableDataHeader");
    reportBody.setStylePrimaryName(css.fixedList());
    reportBody.addStyleName(css.dataList());

    reportBody.setBorderWidth(2);
    reportBody.setCellSpacing(6);
    reportBody.setCellPadding(8);
    reportBody.getCellFormatter().setWidth(0, 0, "100px");
    reportBody.getCellFormatter().setWidth(0, 1, "150px");
    reportBody.getCellFormatter().setWidth(0, 2, "150px");
    reportBody.getCellFormatter().setWidth(0, 3, "300px");
    reportBody.getCellFormatter().setWidth(0, 4, "80px");
    reportBody.getCellFormatter().setWidth(0, 5, "80px");
    reportBody.getCellFormatter().setWidth(0, 6, "80px");
    for (int indx = 0; indx < HEADERS.length; indx++) {
      Label headerLabel = new Label(HEADERS[indx]);
      headerLabel.setStylePrimaryName(css.tableDataHeaderLabel());
      reportBody.setWidget(0, indx, headerLabel);
    }
    reportBody.getCellFormatter().addStyleName(0, 0, css.dataListFirst());
    reportBody.getCellFormatter().addStyleName(0, HEADERS.length - 1, css.dataListLast());

    final FlexTable reportSummary = new FlexTable();
    reportSummary.setBorderWidth(0);
    reportSummary.getElement().setAttribute("Style", "margin-top: 40px; width: 240px;");
    reportSummary.setCellPadding(8);
    reportSummary.getCellFormatter().setWidth(0, 0, "100px");
    reportSummary.getCellFormatter().setWidth(0, 0, "100px");
    clinicService.patientSurveysReport(fromDt, toDt, new Callback<ArrayList<ArrayList<Object>>>() {

      @Override
      public void handleSuccess(ArrayList<ArrayList<Object>> result) {
      if (result != null && result.size() > 0) {
        boolean odd = false;
        for (int row = 0; row < (result.size() - 3); row++) { // the last 3 are the total lines
          ArrayList<Object> line = result.get(row);
          if (line != null && line.size() > 0) {
            for (int col = 0; col < line.size(); col++) {
              reportBody.setText(row + 1, col, line.get(col).toString());
              if (odd) {
                reportBody.getCellFormatter().getElement(row
                    + 1, col).setAttribute("Style", "border: 1px solid #ddd; background-color: #f9f9f9; padding: 8px; ");
              } else {
                reportBody.getCellFormatter().getElement(
                    row + 1, col).setAttribute("Style", "border: 1px solid #ddd; padding: 8px; ");
              }
            }
            odd = !odd;
          }
        }
      }

      if (result != null && result.size() > 0) { // show the totals in a different table
        for (int row = (result.size() - 3); row < result.size(); row++) {
          ArrayList<Object> line = result.get(row);
          for (int col = 0; col < 2; col++) {
            reportSummary.setText(row + 1, col, line.get(col).toString());
          }
          reportSummary.getCellFormatter().setAlignment(
              row + 1, 1, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
          reportSummary.getCellFormatter().setAlignment(
              row + 1, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
        }
      }
      }
    });

    rptPanel.add(reportBody);
    rptPanel.add(reportSummary);
    return rptPanel;
  }

  @Override
  public HorizontalPanel getHeader() {
    HorizontalPanel headerPanel = new HorizontalPanel();
    Label title = new Label("Survey Times Report " + utils.getDateString(fromDt) + " through "
        + utils.getDateString(toDt));

    title.setStyleName(css.titleLabel());
    headerPanel.setHorizontalAlignment(headerPanel.getHorizontalAlignment());
    headerPanel.add(title);
    headerPanel.setStyleName(css.centerPanel());
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
    sb.append(utils.getDateString(new Date(fromDt.getTime())));
    sb.append("&endDt=");
    sb.append(utils.getDateString(new Date(toDt.getTime())));
    return sb.toString();
  }

  @Override
  public String getRptName() {

      return "PatientSurveysReport";

  }

  @Override
  public boolean xcelDownload() {
    return true;
  }
}
