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

import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class PatientVisitsReport implements ClinicReport {
  private final ClinicServiceAsync clinicService;

  private ClientUtils utils = null;
  private Date fromDt, toDt;
  private final Logger logger = Logger.getLogger(PatientVisitsReport.class.getName());

  public PatientVisitsReport(ClientUtils utils, ClinicServiceAsync clinicService, Date fromDt, Date toDt) {
    this.utils = utils;
    this.clinicService = clinicService;
    this.fromDt = fromDt;
    this.toDt = toDt;
  }

  @Override
  public Widget getBody() {
    final VerticalPanel bodyPanel = new VerticalPanel();

    final FlexTable reportBody = new FlexTable();

    reportBody.getRowFormatter().setStylePrimaryName(0, "tableDataHeader");
    reportBody.setStylePrimaryName(RegistryResources.INSTANCE.css().dataList());
    reportBody.addStyleName(RegistryResources.INSTANCE.css().centerPanel());
    reportBody.addStyleName(RegistryResources.INSTANCE.css().report());
    reportBody.setBorderWidth(2);
    reportBody.setCellSpacing(0);
    reportBody.setCellPadding(4);

    logger.log(Level.INFO, "running visits report from " + utils.getDateString(fromDt) + " to "
        + utils.getDateString(toDt));
    clinicService.visitsReportData(fromDt, toDt, new Callback<ArrayList<ArrayList<Object>>>() {

      @Override
      public void handleSuccess(ArrayList<ArrayList<Object>> result) {
        if (result != null && result.size() > 0) {

          for (int row = 0; row < result.size(); row++) {
            ArrayList<Object> line = result.get(row);
            if (line != null && line.size() > 1) {
              Label lbl1 = new Label(line.get(0).toString());
              if (row == 0) {
                lbl1.setStylePrimaryName(RegistryResources.INSTANCE.css().tableDataHeaderLabel()); // top row
              } else {
                lbl1.setStylePrimaryName(RegistryResources.INSTANCE.css().boldReportLabel());
              }
              reportBody.setWidget(row, 0, lbl1);
              reportBody.getCellFormatter().addStyleName(row, 0, "first-child");
              for (int col = 1; col < line.size(); col++) {
                if (row == 0) { // Top row are the headings
                  Label lbl2 = new Label(line.get(col).toString());
                  lbl2.setStylePrimaryName(RegistryResources.INSTANCE.css().tableDataHeaderLabel());
                  lbl2.addStyleName(RegistryResources.INSTANCE.css().numericDataHeaderLabel());
                  reportBody.setWidget(row, col, lbl2);
                  reportBody.getColumnFormatter().setWidth(0, "200px");
                } else {
                  Label numberLabel = new Label(line.get(col).toString());
                  numberLabel.setStylePrimaryName(RegistryResources.INSTANCE.css().numericReportData());
                  reportBody.setWidget(row, col, numberLabel);
                  reportBody.getColumnFormatter().setWidth(col, "55px");
                  reportBody.getCellFormatter().setStylePrimaryName(row, col, RegistryResources.INSTANCE.css().numericReportData());
                }
              }
              reportBody.getCellFormatter().addStyleName(row, line.size() - 1, "last_child");

            } else { // blank line
              if (line != null && line.size() > 1) {
                Label lbl1 = new Label(" ");
                lbl1.setStylePrimaryName(RegistryResources.INSTANCE.css().boldReportLabel());
              }
            }
          }
        }

      }

    });
    bodyPanel.add(reportBody);

    FlexTable legend = new FlexTable();

    legend.setBorderWidth(2);
    legend.setCellPadding(4);
    legend.getColumnFormatter().getElement(0).getStyle().setProperty("backgroundColor", "#BBBBBB");
    legend.getColumnFormatter().getElement(0).getStyle().setProperty("borderRight", "1px solid black");
    legend.addStyleName(RegistryResources.INSTANCE.css().centerPanel());
    legend.setWidget(0, 0, new Label("Total Visits"));
    legend.getWidget(0, 0).addStyleName(RegistryResources.INSTANCE.css().leftLabel());
    legend.setWidget(0, 1, new Label("The # of non-cancelled appointments in CHOIR for the time frame."));
    legend.setWidget(1, 1, new Label("Total Visits is then broken down into 2 sub categories. \"Survey Eligible\" and \"Not Eligible\"."));
    legend.getWidget(0, 1).addStyleName(RegistryResources.INSTANCE.css().leftLabel());
    legend.getWidget(1, 1).addStyleName(RegistryResources.INSTANCE.css().leftLabel());

    legend.getRowFormatter().addStyleName(2, RegistryResources.INSTANCE.css().borderedVPanel());

    legend.setWidget(3, 0, new Label("Survey Eligible"));
    legend.getWidget(3, 0).addStyleName(RegistryResources.INSTANCE.css().leftLabel());
    legend.setWidget(3, 1, new Label("The # of appointments that needed a survey. These are for patients marked as registered who have not completed a survey within the last 14 days. "));
    legend.setWidget(4, 1, new Label("Survey Eligible is then broken down into 3 sub-categories each with both counts & percentages."));
    legend.getWidget(3, 1).addStyleName(RegistryResources.INSTANCE.css().leftLabel());
    legend.getWidget(4, 1).addStyleName(RegistryResources.INSTANCE.css().leftLabel());
    legend.setWidget(5, 1, new HTML(SafeHtmlUtils.fromSafeConstant("<ul><li><u>Started and Completed</u></li>"
        + "<li><u>Started but not Completed</u></li>"
        + "<li><u>Not started</u></li></ul>")));

    legend.getRowFormatter().addStyleName(6, RegistryResources.INSTANCE.css().borderedVPanel());

    legend.setWidget(7, 0, new Label("Not Eligible"));
    legend.getWidget(7, 0).addStyleName(RegistryResources.INSTANCE.css().leftLabel());
    //legend.getWidget(8, 1).addStyleName(RegistryResources.INSTANCE.css().borderedVPanel());
    legend.setWidget(7, 1, new Label("The # of appointments that did not need a survey. "));
    legend.setWidget(8, 1, new Label("Not Eligible is then broken down into 3 sub categories with counts & percentages."));
    legend.getWidget(7, 1).addStyleName(RegistryResources.INSTANCE.css().leftLabel());
    legend.getWidget(8, 1).addStyleName(RegistryResources.INSTANCE.css().leftLabel());
    legend.setWidget(10, 1, new HTML(SafeHtmlUtils.fromSafeConstant(
        "<ul><li><u>Not Registered or Declined</u> - for patients who are not marked as either registered or declined yet.</li>"
        +"<li><u>Declined</u> - for patients who have declined to do the surveys.</li>"
        +"<li><u>Registered but Suppressed</u> - for patients that do not need to do a survey because they have completed one within the last 14 days.</li></ul>")));
    bodyPanel.add(legend);
    return bodyPanel;
  }

  @Override
  public HorizontalPanel getHeader() {
    HorizontalPanel headerPanel = new HorizontalPanel();
    Label title = new Label("Patient Visits " + utils.getDateString(fromDt) + " through " + utils.getDateString(toDt));
    title.setStyleName(RegistryResources.INSTANCE.css().titleLabel());
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
    return "visits";
  }

  @Override
  public boolean xcelDownload() {
    return true;
  }

}
