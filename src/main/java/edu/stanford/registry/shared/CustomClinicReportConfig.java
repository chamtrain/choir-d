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

package edu.stanford.registry.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Configuration values for a custom clinic report
 */
public class CustomClinicReportConfig implements IsSerializable {

  private String name;
  private String reportType;
  private WidgetType widgetType = WidgetType.standard;
  private Integer startDaysBack = null;
  public enum WidgetType { standard, api }

  public CustomClinicReportConfig() {
  }

  public CustomClinicReportConfig(String name, String reportType) {
    this.name = name;
    this.reportType = reportType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getReportType() {
    return reportType;
  }

  public void setWidgetType(WidgetType typ) {
    widgetType = typ;
  }

  public WidgetType getWidgetType() {
    return widgetType;
  }

  public void setReportType(String reportType) {
    this.reportType = reportType;
  }

  public Integer getStartDaysBack() {
    return startDaysBack;
  }

  public void setStartDaysBack(int days) {
    startDaysBack = new Integer(days);
  }
}
