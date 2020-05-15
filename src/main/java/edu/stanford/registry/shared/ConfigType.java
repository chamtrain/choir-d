package edu.stanford.registry.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ConfigType implements IsSerializable, Serializable {

  private String category;
  private String name;
  private TypeName type;
  public enum TypeName { view, builder, configparam, survey, physician };

  public ConfigType() {
  }

  public ConfigType(String category, String name, TypeName typeName) {
    setCategory(category);
    setName(name);
    setType(typeName);
  }

  public ConfigType(String category, String name, String type) {
    setCategory(category);
    setName(name);

    if (TypeName.builder.toString().equals(type)) {
      setType(TypeName.builder);
    } else if (TypeName.configparam.toString().equals(type)) {
      setType(TypeName.configparam);
    } else if (TypeName.physician.toString().equals(type)) {
      setType(TypeName.physician);
    } else if (TypeName.survey.toString().equals(type)) {
      setType(TypeName.survey);
    }
  }
  public String getCategory() {
    return category;
  }
  public void setCategory(String cat) {
    category = cat;
  }

  public String getName() {
    return name;
  }

  public void setName(String configName) {
    name = configName;
  }

  public TypeName getType() {
    return type;
  }

  public void setType(TypeName typeName) {
    type = typeName;
  }


}
