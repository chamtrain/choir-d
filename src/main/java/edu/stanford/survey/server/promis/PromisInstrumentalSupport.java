package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;

import static edu.stanford.survey.server.ItemBanks.*;

/**
 * Item bank for PROMIS generated from: Instrumental Support 48AA3E5F-CDE6-4FAE-819A-D65992D539C6.json
 */
public class PromisInstrumentalSupport {

  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
    item("CCC31051x3", "", "Is someone available to help you if you need it?", "", 4.09581, new double[] { -1.8555, -1.1385, -0.4572, 0.3808 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("CCC31052x", "", "Do you have someone to help you if you are confined to bed?", "", 4.43878, new double[] { -1.2537, -0.8036, -0.3463, 0.375 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("CCC31055x", "", "Do you have someone to take you to the doctor if you need it?", "", 4.21559, new double[] { -1.5832, -1.1107, -0.5415, 0.1595 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("CCC31062x", "", "Do you have someone to prepare your meals if you are unable to do it yourself?", "", 3.37645, new double[] { -1.2153, -0.7147, -0.2234, 0.5093 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("CCC31065x", "", "Do you have someone to help with your daily chores if you are sick?", "", 4.20553, new double[] { -1.2351, -0.6932, -0.1307, 0.558 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("INS-CaPS1", "", "Do you have someone to bring you to an appointment if you need it?", "", 3.58272, new double[] { -1.5854, -1.0731, -0.5249, 0.2787 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("INS-CaPS2", "", "Do you have someone to help you clean up around the home if you need it?", "", 3.57454, new double[] { -1.3233, -0.7014, -0.1342, 0.6194 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("INS-CaPS6", "", "Could you get a friend or family member to help move furniture around your home if you need it?", "", 2.63783, new double[] { -1.9234, -1.2515, -0.426, 0.53 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SS6", "", "Do you have someone to run errands if you need it?", "", 3.9316, new double[] { -1.3992, -0.9113, -0.2531, 0.5287 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SS8", "", "Do you have someone to pick up a prescription if you need it?", "", 3.91025, new double[] { -1.4387, -1.0932, -0.5207, 0.1742 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SS9", "", "Do you have someone to take over all of your responsibilities at home if you need it?", "", 3.18521, new double[] { -1.0071, -0.485, 0.0222, 0.7116 }, -1, "", 
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