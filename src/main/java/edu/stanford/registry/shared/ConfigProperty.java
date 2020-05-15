package edu.stanford.registry.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ConfigProperty implements IsSerializable, Serializable {


  public final static String BUILDER_TYPE = "builder";

  private String configName;

  private ConfigType configType;
  private PropertyCategory propertyCategory;



  public String getConfigName() {
    return configName;
  }

  public void setConfigName(String name) {
    configName = name;
  }



  public ConfigType getConfigType() {
    return configType;
  }

  public void setConfigType(ConfigType value) {
    configType = value;
  }

  public PropertyCategory getPropertyCategory () {
    return propertyCategory;
  }
  public void setPropertyCategory (PropertyCategory category) {
    propertyCategory = category;
  }
}
