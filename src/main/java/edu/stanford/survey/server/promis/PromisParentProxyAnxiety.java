package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import static edu.stanford.survey.server.ItemBanks.*;

/**
 * Item bank for PROMIS generated from: Parent Proxy-Anxiety v1.1.csv
 */
public class PromisParentProxyAnxiety {

  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
    item("Pf1anxiety8", "In the past 7 days", "My child felt nervous.", "", 1.85, new double[] { -0.82, 0.34, 1.92, 3.21 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("Pf2anxiety2", "In the past 7 days", "My child felt scared.", "", 2.84, new double[] { 0.1, 1.06, 2.27, 2.94 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("Pf2anxiety9", "In the past 7 days", "My child felt worried.", "", 2.65, new double[] { -0.69, 0.32, 1.88, 2.85 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("Pf2anxiety1", "In the past 7 days", "My child felt like something awful might happen.", "", 2.35, new double[] { 0.19, 1.25, 2.51, 3.35 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("Pf1anxiety3", "In the past 7 days", "My child worried about what could happen to him/her.", "", 2.24, new double[] { -0.37, 0.61, 1.94, 2.97 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("Pf2anxiety4", "In the past 7 days", "My child worried when he/she went to bed at night.", "", 2.51, new double[] { 0.28, 1.01, 2.12, 3.11 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("Pf2anxiety5", "In the past 7 days", "My child worried when he/she was at home.", "", 2.63, new double[] { 0.28, 1.08, 2.53, 3.26 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("Pf1anxiety6", "In the past 7 days", "My child worried when he/she was away from home.", "", 1.96, new double[] { 0.55, 1.57, 2.55, 2.84 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("Pf1anxiety1", "In the past 7 days", "My child got scared really easy.", "", 1.9, new double[] { -0.05, 1.23, 2.46, 3.35 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("Pf1anxiety5", "In the past 7 days", "My child woke up at night scared.", "", 1.8, new double[] { 0.71, 1.82, 3.04, 3.33 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("Pf2anxiety3", "In the past 7 days", "My child was worried he/she might die.", "", 1.68, new double[] { 1.26, 2.15, 4.06 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 4)
    ),
    item("Pf1anxiety9", "In the past 7 days", "It was hard for my child to relax.", "", 1.58, new double[] { -0.38, 0.76, 2.21, 3.26 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("Pf2anxiety7", "In the past 7 days", "My child was afraid of going to school.", "", 1.53, new double[] { 1.23, 2.21, 3.18, 3.56 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    )
  );

  public static ItemBank bank() {
    return bank;
  }
}