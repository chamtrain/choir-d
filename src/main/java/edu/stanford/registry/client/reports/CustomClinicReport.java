package edu.stanford.registry.client.reports;

import java.util.Date;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.shared.CustomClinicReportConfig;

/**
 * Class to represent a custom clinic report. This class makes a 
 * request to the server to get the report which is then displayed
 * as HTML.
 */
public class CustomClinicReport implements ClinicReport {
  
  private final ClinicServiceAsync clinicService;
  private CustomClinicReportConfig config;
  private Date fromDt;
  private Date toDt;

  public CustomClinicReport(CustomClinicReportConfig config, ClinicServiceAsync clinicService, Date fromDt, Date toDt) {
    this.config = config;
    this.clinicService = clinicService;
    this.fromDt = fromDt;
    this.toDt = toDt;
  }

  @Override
  public Widget getBody() {
    final HTML html = new HTML("Generating Report ...");
    clinicService.customReport(config.getReportType(), fromDt, toDt, new Callback<String>() {
      @Override
      public void handleSuccess(String result) {
        html.setHTML(result);
      }
    });
    return html;
  }

  @Override
  public boolean hasHeader() {
    return false;
  }

  @Override
  public boolean hasFooter() {
    return false;
  }

  @Override
  public HorizontalPanel getHeader() {
    return null;
  }

  @Override
  public HorizontalPanel getFooter() {
    return null;
  }

  @Override
  public String getRptName() {
    return null;
  }

  @Override
  public String getRequestParameters() {
    return null;
  }

  @Override
  public boolean xcelDownload() {
    return false;
  }

}
