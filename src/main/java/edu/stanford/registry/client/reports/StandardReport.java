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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class StandardReport implements ClinicReport {
  private final ClinicServiceAsync clinicService;
  private final ClientUtils utils;
  private final Date fromDt;
  private final Date toDt;
  private final String reportName;
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();

  public StandardReport(ClientUtils utils, ClinicServiceAsync clinicService, String reportName, Date fromDt, Date toDt) {
    this.utils = utils;
    this.clinicService = clinicService;
    this.fromDt = fromDt;
    this.toDt = toDt;
    this.reportName = reportName;
  }

  @Override
  public Widget getBody() {
    FlowPanel rptPanel = new FlowPanel();
    rptPanel.setStyleName(css.centerPanel());

    rptPanel.setWidth("100%");
    final FlexTable reportBody = new FlexTable();

    reportBody.getRowFormatter().setStylePrimaryName(0, "tableDataHeader");
    reportBody.setStylePrimaryName(css.fixedList());
    reportBody.addStyleName(css.dataList());

    reportBody.setBorderWidth(2);
    reportBody.setCellSpacing(5);
    reportBody.setCellPadding(4);

    clinicService.standardReport(reportName, fromDt, toDt, new Callback<ArrayList<ArrayList<Object>>>() {

      @Override
      public void handleSuccess(ArrayList<ArrayList<Object>> result) {
        if (result != null && result.size() > 0) { // first line is the heading
          ArrayList<Object> headers = result.get(0);
          for (int indx = 0; indx < headers.size(); indx++) {
            reportBody.getCellFormatter().setWidth(0, indx, "100px");
            Label headerLabel = new Label(headers.get(indx).toString());
            headerLabel.setStylePrimaryName(css.tableDataHeaderLabel());
            reportBody.setWidget(0, indx, headerLabel);
          }
          reportBody.getCellFormatter().addStyleName(0, 0, css.dataListFirst());
          reportBody.getCellFormatter().addStyleName(0, headers.size() - 1, css.dataListLast());

          if (result.size() > 1) {
            boolean odd = false;
            for (int row = 1; row < (result.size()); row++) { // the last 3 are the total lines
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
        }
      }
    });

    rptPanel.add(reportBody);
    return rptPanel;
  }

  @Override
  public HorizontalPanel getHeader() {
    HorizontalPanel headerPanel = new HorizontalPanel();
    Label title = new Label("For the period " + utils.getDateString(fromDt) + " to " + utils.getDateString(toDt));
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
    return reportName;
  }

  @Override
  public boolean xcelDownload() {
    return true;
  }
}
