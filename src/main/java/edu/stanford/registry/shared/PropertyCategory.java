package edu.stanford.registry.shared;

public enum PropertyCategory {

  G("General", "System parameters"),
  //W("Web user interface", ""),
  I("Import", "For the importing of appointment and patient files"),
  E("Email", ""),
  C("Clinic Interface", ""),
  R("Reporting", ""),
  P("Patient survey", "For survey-specific configuration"),
  A("Assessment center", "");

  String title, desc;
  PropertyCategory(String title, String desc) {
    this.title = title;
    this.desc = desc;
  }
  public String title() {
    return title;
  }

  static PropertyCategory valueOf(char c, boolean badOkay) {
    if (c == 0)
      return null;
    for (PropertyCategory cat: PropertyCategory.values())
      if (((c ^ cat.name().charAt(0)) & 0xDF) == 0) // mask out the Cap/lowercase bit
        return cat;
    if (badOkay) {
      return null;
    }
    throw new RuntimeException("Bad " + PropertyCategory.class.getSimpleName() + " category: (" + c + ") 0x" + (int)c);
  }

}
