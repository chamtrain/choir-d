package edu.stanford.registry.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ConfigParam implements IsSerializable, Serializable {
  public final static String CONFIG_TYPE = "configparam";
  private long configId;
  private String configName;
  private String configValue;
  private Long surveySiteId;
  private String configType;

  private String enabled;
  private String cachedValue;
  public  ConfigParam() {
  };

  public ConfigParam(long appConfigId, long surveySiteId, String configType, String configName, String configValue, boolean enabled) {
    setConfigId(appConfigId);
    setSurveySiteId(surveySiteId);
    setConfigType(configType);
    setConfigName(configName);
    setConfigValue(configValue);
    setSurveySiteId(surveySiteId);
    setEnabled(enabled);

  }

  public ConfigParam(long appConfigId, long surveySiteId, RegConfigProperty property, String configName, String configValue, boolean enabled) {
    setConfigId(appConfigId);
    setSurveySiteId(surveySiteId);
    setConfigType(property);
    setConfigName(configName);
    setConfigValue(configValue);
    setSurveySiteId(surveySiteId);
    setEnabled(enabled);
  }

  public Long getSurveySiteId() {
    return surveySiteId;
  }

  public void setSurveySiteId (Long surveySiteId) {
    this.surveySiteId = surveySiteId;
  }

  public void setConfigType(String configType) { this.configType = configType; }

  public String getConfigType() { return configType; }

  public void setConfigType(RegConfigProperty property) {
    configType = property.getCategory().getType();
  }

  public String getEnabled() {
    return checkEnabled();
  }

  public boolean isEnabled() {
    return getEnabled().equals("Y");
  }

  public void setEnabled(boolean isEnabled) {
    enabled = (isEnabled) ? "Y" : "N" ;
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

  public boolean getCached() {
    return cachedValue != null && configValue != null && cachedValue.equals(configValue);
  }

  public long getConfigId() {
    return configId;
  }

  public void setConfigId(long id) {
    configId = id;
  }

  public String getConfigName() {
    return configName;
  }

  public void setConfigName(String name) {
    configName = name;
  }

  public String getConfigValue() {
    return configValue;
  }

  public void setConfigValue(String value) {
    configValue = value;
  }

  public String getCachedValue() { return cachedValue; }

  public void setCachedValue(String value) { cachedValue = value; }


}
