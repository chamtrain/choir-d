package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import static edu.stanford.survey.server.ItemBanks.*;
/**
 * Item bank for PROMIS generated from: Social Sat DSA FEE4576A-D94F-4E85-8DAB-5A3181BB14CB.json
 */

public class PromisSocialSatDSA {

  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0, 
  item("SRPSAT05", "In the past 7 days", "I am satisfied with the amount of time I spend doing leisure activities", "", 4.292, new double[] { -1.157, -0.559, 0.15, 0.811 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT10", "In the past 7 days", "I am satisfied with my current level of social activity", "", 3.745, new double[] { -1.134, -0.558, 0.192, 0.916 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT19", "In the past 7 days", "I am satisfied with my ability to do all of the community activities that are really important to me", "", 3.039, new double[] { -1.235, -0.626, 0.109, 0.85 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT20", "In the past 7 days", "I am satisfied with my ability to do things for my friends", "", 3.888, new double[] { -1.464, -0.781, -0.0030, 0.76 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT23", "In the past 7 days", "I am satisfied with my ability to do leisure activities", "", 4.352, new double[] { -1.232, -0.649, 0.0020, 0.724 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT25", "In the past 7 days", "I am satisfied with my current level of activities with my friends", "", 4.083, new double[] { -1.149, -0.577, 0.157, 0.899 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT33", "In the past 7 days", "I am satisfied with my ability to do things for fun outside my home", "", 4.9, new double[] { -1.072, -0.531, 0.126, 0.723 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT34", "In the past 7 days", "I feel good about my ability to do things for my friends", "", 3.325, new double[] { -1.61, -0.836, -0.128, 0.761 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT36", "In the past 7 days", "I am happy with how much I do for my friends", "", 3.346, new double[] { -1.459, -0.757, 0.111, 0.908 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT37", "In the past 7 days", "I am satisfied with the amount of time I spend visiting friends", "", 3.655, new double[] { -1.175, -0.536, 0.308, 0.967 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT48", "In the past 7 days", "I am satisfied with my ability to do things for fun at home (like reading, listening to music, etc.)", "", 2.702, new double[] { -1.591, -0.89, -0.277, 0.603 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT52", "In the past 7 days", "I am satisfied with my ability to do all of the leisure activities that are really important to me", "", 4.356, new double[] { -1.138, -0.559, 0.061, 0.725 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  )
);

  public static ItemBank bank() {
    return bank;
  }
}