/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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
import edu.stanford.registry.client.clinictabs.CustomHTMLWidget;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.CustomClinicReportConfig;

import java.util.Date;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Handles custom reports run by the API
 *
 * @author tpacht
 */
public class ApiClinicReport implements ClinicReport {

  private final CustomClinicReportConfig config;
  private final Date fromDt;
  private final Date toDt;
  private final ClinicUtils utils;
  public ApiClinicReport(ClinicUtils utils, CustomClinicReportConfig config, Date fromDt, Date toDt) {
    this.utils = utils;
    this.config = config;
    this.fromDt = fromDt;
    this.toDt = toDt;
    exportShowLoadingPopup();
    exportHideLoadingPopup();
  }

  @Override
  public Widget getBody() {
    CustomHTMLWidget widget = new CustomHTMLWidget(utils, config.getReportType(), null);
    widget.addFunction( "getFromDt", utils.xmlFmtDt.format(fromDt));
    widget.addFunction("getFromTm", String.valueOf(fromDt.getTime()));
    widget.addFunction("getToDt", utils.xmlFmtDt.format(toDt));
    widget.addFunction("getToTm", String.valueOf(toDt.getTime()));
    widget.addFunction("getReportName", config.getName() );

    widget.setStylePrimaryName(RegistryResources.INSTANCE.css().centerPanel());
    widget.setSize("1000px", "700px");
    return widget;
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

  public void showLoadingPopup() {
    utils.showLoadingPopUp();
  }

  public void hideLoadingPopup() {
    utils.hideLoadingPopUp();
  }

  public native void exportShowLoadingPopup() /*-{
    var loadInstance = this;
    $wnd.showLoadingPopup = $entry(function() {
      loadInstance.@edu.stanford.registry.client.reports.ApiClinicReport::showLoadingPopup()();
    });
  }-*/;

  public native void exportHideLoadingPopup() /*-{
    var hideInstance = this;
    $wnd.hideLoadingPopup = $entry(function() {
      hideInstance.@edu.stanford.registry.client.reports.ApiClinicReport::hideLoadingPopup()();
    });
  }-*/;
}
