package edu.stanford.registry.server.config.tools;

import edu.stanford.registry.shared.ConfigParam;
import edu.stanford.registry.shared.RegConfigCategory;
import edu.stanford.registry.shared.RegConfigProperty;
import edu.stanford.registry.shared.RegConfigUsage;

import java.util.ArrayList;
import java.util.Iterator;

public class AppPropertyList extends RegPropertyList {
  private final RegConfigCategory h = RegConfigCategory.H; // physician
  private final RegConfigCategory s = RegConfigCategory.S; // survey builder
  // private final RegConfigCategory q = RegConfigCategory.Q; // square tables
  private final ArrayList<RegConfigProperty> myPropList = new ArrayList<>();


  public AppPropertyList() {
    super();

    mk(h, RegConfigUsage.SiteSpecific, "physician.survey.path", "This is the URL used for calling the survey system from within the clinic application to run physician type surveys.");
    mk(s, RegConfigUsage.SiteSpecific, "builder.survey.path", "This is the URL used for calling the survey system from within the survey builder for testing surveys" );

    // todo select * from app_config where config_type = 'squaretable'
    //mk(q, RegConfigUsage.SiteSpecific, "")
  }

  public static String getParamType(RegConfigProperty p) {
    switch (p.getCategory().toString()) {
    case "H":
      return "physician";
    case "S":
      return "builder";
    default:
      return ConfigParam.CONFIG_TYPE;
    }
  }

  private void mk(RegConfigCategory cat, RegConfigUsage usage, String name, String desc) {
    myPropList.add(new RegConfigProperty(name, cat, usage, "", desc));
  }

  @Override
  public Iterator<RegConfigProperty> iterator() {
    Iterator iterator = super.iterator();
    while (iterator.hasNext()) {

      myPropList.add((RegConfigProperty)iterator.next());
    }
    return myPropList.iterator();
  }
}
