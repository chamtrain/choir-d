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

package edu.stanford.registry.client.clinictabs;

import edu.stanford.registry.client.ErrorDialogWidget;
import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.TextBoxDatePicker;
import edu.stanford.registry.client.reports.ApiClinicReport;
import edu.stanford.registry.client.reports.AverageSurveyTimeReport;
import edu.stanford.registry.client.reports.ClinicReport;
import edu.stanford.registry.client.reports.ComplianceReport;
import edu.stanford.registry.client.reports.CustomClinicReport;
import edu.stanford.registry.client.reports.PatientRegistrationReport;
import edu.stanford.registry.client.reports.PatientSurveysReport;
import edu.stanford.registry.client.reports.PatientVisitsReport;
import edu.stanford.registry.client.reports.StandardReport;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.CustomClinicReportConfig;
import edu.stanford.registry.shared.CustomClinicReportConfig.WidgetType;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class ClinicReportsWidget extends TabWidget {

  private final ClinicServiceAsync clinicService;
  protected ErrorDialogWidget basicErrorPopUp = new ErrorDialogWidget();

  private DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);
  private FlowPanel reportPanel = new FlowPanel();
  private FlexTable reportTable = new FlexTable();

  private final String[] STD_REPORTS = {"Activity Counts ", "Registered patients from ", "Patient Visits from ",
      "Average Survey Times ", "Avg Survey Times by Month", "Avg Survey Times by Type", "Patient Surveys from ",
      "Patient Counts for IRB Renewal"};
  private CustomClinicReportConfig[] customReports;
  
  private final Button[] runButtons;
  private final int NUM_RPTS;
  private final TextBoxDatePicker[] showFromPicker;
  private final TextBoxDatePicker[] showToPicker;
  private final Label[] showToLabel;
  
  private Date defaultDateFrom, defaultDateTo;

  private final Logger logger = Logger.getLogger(ClinicReportsWidget.class.getName());

  private final Image printerImage = new Image(RegistryResources.INSTANCE.printer());
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();

  public ClinicReportsWidget(ClinicUtils utils, ClinicServiceAsync clinicService) {
    super(utils);
    this.clinicService = clinicService;
    if (utils == null) {
      logger.log(Level.SEVERE, "ERROR", new Exception("No utils"));
    }

    // Get the custom report names
    utils.getClientConfig();
    customReports = utils.getClientConfig().getCustomReports();
    if (customReports == null) {
      customReports = new CustomClinicReportConfig[0];
    }
    
    // Create buttons for the standard and custom reports
    runButtons = new Button[STD_REPORTS.length + customReports.length];
    for(int i=0; i<STD_REPORTS.length; i++) {
      runButtons[i] = new Button(STD_REPORTS[i]);
    }
    for(int i=0; i<customReports.length; i++) {
      runButtons[STD_REPORTS.length + i] = new Button(customReports[i].getName());
    }

    NUM_RPTS = runButtons.length;
    showFromPicker = new TextBoxDatePicker[NUM_RPTS];
    showToPicker = new TextBoxDatePicker[NUM_RPTS];
    showToLabel = new Label[NUM_RPTS];
    
    initWidget(mainPanel);
  }

  @Override
  public void load() {
    mainPanel.addNorth(messageBar, 4);

    defaultDateTo = new Date();
    defaultDateFrom = new Date();
    CalendarUtil.addMonthsToDate(defaultDateFrom, -11);
    CalendarUtil.setToFirstDayOfMonth(defaultDateFrom);

    reportTable.setStylePrimaryName(css.dataList());
    reportTable.addStyleName(css.fixedList());
    reportTable.addStyleName(css.centerPanel());
    reportTable.addStyleName(css.customizedFlowPanel());
    reportTable.setCellSpacing(10);
    reportTable.getCellFormatter().setWidth(0, 0, "240px");
    reportTable.getCellFormatter().setWidth(0, 1, "110px");
    reportTable.getCellFormatter().setWidth(0, 2, "40px");
    reportTable.getCellFormatter().setWidth(0, 3, "110px");
    reportTable.setVisible(true);

    for (int inx = 0; inx < runButtons.length; inx++) {
      /* Default the start and end dates for all of the compliance reports */
      showFromPicker[inx] = new TextBoxDatePicker(getUtils().getDefaultDateFormat());
      showToPicker[inx] = new TextBoxDatePicker(getUtils().getDefaultDateFormat());
      showFromPicker[inx].setValue(defaultDateFrom);
      /*
       * Check for a custom # of days back to use for the report start date
       */
      if (inx >= STD_REPORTS.length) {
        Integer daysBack = customReports[inx - STD_REPORTS.length].getStartDaysBack();
        if (daysBack != null) {
          Date startDate = new Date();
          CalendarUtil.addDaysToDate(startDate, daysBack * -1);
          showFromPicker[inx].setValue(startDate);
        }
      }
      showToPicker[inx].setValue(defaultDateTo);
      showFromPicker[inx].setWidth("100px");
      showToPicker[inx].setWidth("100px");

      showToLabel[inx] = new Label("To");
      showToLabel[inx].setStylePrimaryName(css.boldReportLabel());
      runButtons[inx].setWidth("85%");
      reportTable.setWidget(inx, 0, runButtons[inx]);

      final Integer reportNumber = inx;
      runButtons[inx].addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          runReport(reportNumber);
        }
      });

      reportTable.setWidget(inx, 1, showFromPicker[inx]);
      reportTable.setWidget(inx, 2, showToLabel[inx]);
      reportTable.setWidget(inx, 3, showToPicker[inx]);
    }

    /* hide the dates for the 1st one because they're not used */
    showFromPicker[0].setVisible(false);
    showToPicker[0].setVisible(false);
    showToLabel[0].setVisible(false);

    mainPanel.add(reportPanel);
    showReportList();
  }

  private void showReportList() {
    reportPanel.clear();
    reportPanel.setStylePrimaryName(css.centerPanel());
    reportPanel.add(reportTable);
  }

  public void onClick(ClickEvent event) {
    //Widget sender = (Widget) event.getSource();
  }

  private void runReport(Integer reportNumber) {

    if (showFromPicker[reportNumber].getValue() == null
        || showToPicker[reportNumber].getValue() == null) {
      basicErrorPopUp.setText("Can not run report!");
      basicErrorPopUp.setError("You must choose both a from and to report date");
      return;
    }

    Button closeButton = new Button("Close");
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showReportList();
      }
    });

    logger.log(Level.INFO, "Creating report");

    reportPanel.clear();
    getUtils().showLoadingPopUp();
    ClinicReport report = getReport(reportNumber);
    closeButton.setStylePrimaryName(css.rightButton());
    reportPanel.add(closeButton);

    if (report != null) {
      if (report.xcelDownload()) {
        Anchor download = getXlsxAnchor(report);
        download.setStylePrimaryName(css.rightButton());
        reportPanel.add(download);
      }
      if (report.hasHeader()) {
        reportPanel.add(report.getHeader());
      }

      final ScrollPanel scroller = new ScrollPanel();
      scroller.setSize("98%", "90%");

      scroller.add(report.getBody());
      reportPanel.add(scroller);
      if (report.hasFooter()) {
        reportPanel.add(report.getFooter());
      }

    } else {
      reportPanel.add(new Label("No report data"));
    }
    getUtils().hideLoadingPopUp();

    reportPanel.setWidth("98%");

  }

  private ClinicReport getReport(final Integer reportNumber) {

    switch (reportNumber) {

    case 0:
      return new ComplianceReport(getUtils(), clinicService, showFromPicker[reportNumber].getValue(),
          showToPicker[reportNumber].getValue());
    case 1:
      return new PatientRegistrationReport(getUtils(), clinicService, showFromPicker[reportNumber].getValue(),
          showToPicker[reportNumber].getValue());
    case 2:
      return new PatientVisitsReport(getUtils(), clinicService, showFromPicker[reportNumber].getValue(),
          showToPicker[reportNumber].getValue());
    case 3:
      return new AverageSurveyTimeReport(getUtils(), clinicService, showFromPicker[reportNumber].getValue(),
          showToPicker[reportNumber].getValue(), AverageSurveyTimeReport.REPORT_SUMMARY);
    case 4:
      return new AverageSurveyTimeReport(getUtils(), clinicService, showFromPicker[reportNumber].getValue(),
          showToPicker[reportNumber].getValue(), AverageSurveyTimeReport.REPORT_BYMONTH);
    case 5:
      return new AverageSurveyTimeReport(getUtils(), clinicService, showFromPicker[reportNumber].getValue(),
          showToPicker[reportNumber].getValue(), AverageSurveyTimeReport.REPORT_BYTYPE);
    case 6:
      return new PatientSurveysReport(getUtils(), clinicService, showFromPicker[reportNumber].getValue(),
          showToPicker[reportNumber].getValue());
    case 7:
      return new StandardReport(getUtils(), clinicService,  "IRBCounts", showFromPicker[reportNumber].getValue(),
          showToPicker[reportNumber].getValue());
    default:
      int customReportNumber = reportNumber - STD_REPORTS.length;
      if (customReports[customReportNumber].getWidgetType() == WidgetType.api) {
        return new ApiClinicReport(getUtils(), customReports[customReportNumber],
            showFromPicker[reportNumber].getValue(), showToPicker[reportNumber].getValue());
      }
      return new CustomClinicReport(customReports[customReportNumber], clinicService,
          showFromPicker[reportNumber].getValue(), showToPicker[reportNumber].getValue());
    }
  }

  public Button exportXlsx(final ClinicReport report) {
    // images
    final Button exportButton = new Button("<img src='" + printerImage.getUrl() + "' />");
    exportButton.setTitle("Print");
    exportButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String urlString = getUtils().getChartUrl("rpt", report.getRptName(), "fmt", getUtils().dateFormatStr, report.getRequestParameters());
        logger.log(Level.INFO, "Calling url: " + urlString);
        Window.open(urlString, "Print", "");
      }
    });
    return exportButton;
  }

  public Anchor getXlsxAnchor(final ClinicReport report) {
    String urlString = getUtils().getChartUrl("rpt", report.getRptName(), "fmt", getUtils().dateFormatStr, report.getRequestParameters());
    return new Anchor("Download", urlString);
  }

  @Override
  public String serviceName() {
    return Constants.ROLE_CLINIC_STAFF;
  }
}
