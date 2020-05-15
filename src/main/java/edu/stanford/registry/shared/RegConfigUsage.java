package edu.stanford.registry.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum RegConfigUsage implements IsSerializable {

  Static("ST", true, "Static-only (context.xml or web.xml) - a global that the server can't access read from the database"),
  StaticRec("GR", true, "Recommend it be static - best if only IT changes this- many require a server restart to take effect."),
  Global("GL", true, "This is only read as a global property"),
  SitePath("SP", false, "This is site-specific, but can be a global value. When fetched, '{site}' in it is changed to the site's urlParam."),
  SiteSpecific("SI", false, "This is a site property. If missing, the global value, if it exists, will be used as a default"),
  Retired("RE", true, "This is no longer used in the version 2");

  public final String abbrev;
  public final String desc;
  final boolean global;
  RegConfigUsage(String abbrev, boolean global, String desc) {
    this.abbrev = abbrev;
    this.desc = desc;
    this.global = global;
  }
  public boolean isGlobal() {
    return global && (this != Retired);
  }
  public boolean isSiteSpecific() {
    return (!global) && (this != Retired);
  }

}
