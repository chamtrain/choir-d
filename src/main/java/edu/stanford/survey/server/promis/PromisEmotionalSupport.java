package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;

import static edu.stanford.survey.server.ItemBanks.*;

/**
 * Item bank for PROMIS generated from: Emotional Support 29654543-C99B-4CE9-884B-3D401B4AD857.json
 */
public class PromisEmotionalSupport {

  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
    item("FSE31053x2", "", "I have someone who will listen to me when I need to talk", "", 5.23395, new double[] { -1.8827, -1.2382, -0.5505, 0.2688 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("FSE31059x2", "", "I have someone to confide in or talk to about myself or my problems", "", 5.68075, new double[] { -1.6363, -1.0192, -0.4502, 0.2279 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("FSE31066x2", "", "I have someone with whom to share my most private worries and fears", "", 4.38305, new double[] { -1.5159, -0.9434, -0.3677, 0.3016 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("FSE31069x2", "", "I have someone who understands my problems", "", 4.38844, new double[] { -1.834, -1.1945, -0.3903, 0.5488 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("GS1x", "", "I feel close to my friends", "", 2.03484, new double[] { -2.2049, -1.4858, -0.4684, 0.6898 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("GS2x", "", "I get emotional support from my family", "", 2.67883, new double[] { -1.9372, -1.3088, -0.58, 0.2755 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SS11x", "", "I have someone who makes me feel needed", "", 3.22493, new double[] { -2.0642, -1.3942, -0.5923, 0.1659 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SS12x", "", "I have someone who makes me feel appreciated", "", 3.76044, new double[] { -1.9498, -1.2936, -0.5477, 0.2991 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SSE-CaPS6", "", "I have someone I trust to talk with about my feelings", "", 5.00456, new double[] { -1.5987, -1.077, -0.4634, 0.2217 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SSE-CaPS7", "", "I have people who I can talk to about my health", "", 3.25858, new double[] { -2.1958, -1.4477, -0.6739, 0.1915 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SSQ1x", "", "I have people who care about what happens to me", "", 3.02064, new double[] { -2.6111, -1.991, -1.1219, -0.3601 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SSQ2x", "", "I get love and affection", "", 3.08962, new double[] { -2.0876, -1.2723, -0.5791, 0.2118 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SSQ3x2", "", "I have someone to talk with when I have a bad day", "", 4.42773, new double[] { -1.8454, -1.1174, -0.4471, 0.3194 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SSQ4x2", "", "I have someone I trust to talk with about my problems", "", 5.24799, new double[] { -1.6829, -1.0766, -0.5305, 0.1781 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("UCLA16x2", "", "I feel there are people who really understand me", "", 2.92893, new double[] { -1.9117, -1.1543, -0.2496, 0.839 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("UCLA19x3", "", "There are people I can talk to", "", 3.8843, new double[] { -2.0552, -1.4842, -0.5905, 0.1987 }, -1, "", 
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