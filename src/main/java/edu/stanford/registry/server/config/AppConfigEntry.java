/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.config;

public class AppConfigEntry {
  
  private long appConfigId;
  private Long surveySiteId;
  private String configType;
  private String configName;
  private String configValue;
  private String enabled;
  
  public AppConfigEntry() {}
  
  public AppConfigEntry(long appConfigId, long surveySiteId, String configType, String configName, String configValue, boolean enabled) {
    this.appConfigId = appConfigId;
    this.surveySiteId = surveySiteId;
    this.configType = configType;
    this.configName = configName;
    this.configValue = configValue;
    if (enabled) {
      setEnabled("Y");
    } else {
      setEnabled("N");
    }
  }
  
  public void setAppConfigId (Long appConfigId ) {
    this.appConfigId = appConfigId;
  }
  
  public Long getAppConfigId() {
    return appConfigId;
  }
  
  public void setSurveySiteId (Long surveySiteId) {
    this.surveySiteId = surveySiteId;
  }
  
  public Long getSurveySiteId() {
    return surveySiteId;
  }

  public void setConfigType(String configType) { this.configType = configType; }

  public String getConfigType() { return configType; }

  public void setConfigName (String configName) {
    this.configName = configName;
  }
  
  public String getConfigName() {
    return configName;
  }
  
  public void setConfigValue (String configValue) {
    this.configValue = configValue;
  }
  
  public String getConfigValue() {
    return configValue;
  }
  
  public void setEnabled(String enabled) {
    this.enabled = enabled;
  }
  
  public void setEnabled(boolean isEnabled) {
    enabled = (isEnabled) ? "Y" : "N" ;
  }
  
  public String getEnabled() {
    return checkEnabled();
  }
  
  public boolean isEnabled() {
    return getEnabled().equals("Y");
  }
  
  private String checkEnabled() {
    String isEnabled = "N";
    if (enabled == null) {
      enabled = isEnabled;
    }
    if (!enabled.equals("N") && !enabled.equals("Y")) {
      enabled = "N";
    }
    return enabled;
  }

}
