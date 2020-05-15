package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import static edu.stanford.survey.server.ItemBanks.*;
/**
 * Item bank for PROMIS generated from: Informational Support 9A2F920A-F470-4F3A-B82E-EC9D19F38F8D.json
 */

public class PromisInformationalSupport {

  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0, 
  item("FSE31054x2", "", "I have someone to give me good advice about a crisis if I need it", "", 5.14644, new double[] { -1.7237, -1.0292, -0.2362, 0.6047 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Usually", 4),
    response("Always", 5)
  ),
  item("FSE31058x2", "", "I have someone to give me information if I need it", "", 4.21638, new double[] { -1.9425, -1.2691, -0.2813, 0.6966 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Usually", 4),
    response("Always", 5)
  ),
  item("INF-CaPS1", "", "Other people help me get information when I have a problem", "", 2.76097, new double[] { -1.8275, -0.9151, 0.1093, 1.1399 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Usually", 4),
    response("Always", 5)
  ),
  item("INF-CaPS2", "", "I can get helpful advice from others when dealing with a problem", "", 3.88948, new double[] { -1.8693, -1.0696, -0.1182, 0.9541 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Usually", 4),
    response("Always", 5)
  ),
  item("INF-CaPS3", "", "I can turn to people who know how to solve problems like mine", "", 4.3397, new double[] { -1.8148, -1.0353, -0.3098, 0.5777 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Usually", 4),
    response("Always", 5)
  ),
  item("INF-CaPS4", "", "My friends have useful information when I have problems to solve", "", 2.57927, new double[] { -1.8133, -0.8763, 0.3162, 1.3779 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Usually", 4),
    response("Always", 5)
  ),
  item("INF-CaPS5", "", "My family has useful information when I have problems to solve", "", 2.90318, new double[] { -1.6074, -0.9379, -0.0045, 0.9755 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Usually", 4),
    response("Always", 5)
  ),
  item("SS7x", "", "I have someone to turn to for suggestions about how to deal with a problem", "", 4.6411, new double[] { -1.8014, -1.0455, -0.2894, 0.5998 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Usually", 4),
    response("Always", 5)
  ),
  item("SSQ5x", "", "I have someone to talk with about money matters", "", 2.4696, new double[] { -1.637, -0.8913, -0.2529, 0.5912 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Usually", 4),
    response("Always", 5)
  ),
  item("SSQ7x", "", "I get useful advice about important things in life", "", 3.37114, new double[] { -1.8992, -1.0605, 0.0285, 1.0206 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Usually", 4),
    response("Always", 5)
  )
);

  public static ItemBank bank() {
    return bank;
  }
}