package edu.stanford.registry.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
public enum RegConfigCategory implements IsSerializable {
  G("General", "System parameters"),
  //W("Web user interface", ""),
  I("Import", "For the importing of appointment and patient files"),
  E("Email", "For sending patient emails"),
  C("Clinic Interface", "Used in the application interface"),
  R("Reporting", "For running the reports"),
  P("Patient survey", "For survey-specific configuration"),
  A("Assessment center", "No longer used"),
  H("Physician survey", "Only For physician entered data", "physician"),
  S("Survey Builder", "Used when testing surveys in the survey builder","builder"),
  Q("Square Table", "For square table handling");
  private String title;
  public String desc;
  private String type;

  RegConfigCategory(String title, String desc) {
    this.title = title;
    this.desc = desc;
    this.type = "configparam";
  }

  RegConfigCategory(String title, String desc, String type) {
    this.title = title;
    this.desc = desc;
    this.type = type;
  }

  public String title() {
    return title;
  }
  public String getTitle() {
    return title;
  }
  public String getDesc() {
    return desc;
  }
  public String getType() { return type; }


  public static RegConfigCategory valueOf(char c, boolean badOkay) {
    if (c == 0)
      return null;
    for (RegConfigCategory cat: RegConfigCategory.values())
      if (((c ^ cat.name().charAt(0)) & 0xDF) == 0) // mask out the Cap/lowercase bit
        return cat;
    if (badOkay) {
      return null;
    }

    throw new RuntimeException("Bad RegConfigProperty category: (" + c + ") 0x" + ((int)c));
  }
}
