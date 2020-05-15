package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import static edu.stanford.survey.server.ItemBanks.*;
/**
 * Item bank for PROMIS generated from: Alcohol Positive Expectancies A841AF31-A0A1-4FB0-8DC6-B5AF77DE2877.json
 */

public class PromisAlcoholPositiveExpectancies {

  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 9, 3.0, 
  item("PEXP02", "", "People are outgoing when they drink.", "", 2.38018, new double[] { -1.8151, -0.6428, 0.6016, 1.6202 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("PEXP04", "", "People have more desire for sex when they drink.", "", 1.65245, new double[] { -1.3698, -0.2887, 0.9746, 1.9375 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("PEXP05", "", "People sleep better when they drink.", "", 1.25654, new double[] { -1.1331, 0.1286, 1.5569, 2.7555 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("PEXP06", "", "People have more fun at social occasions when they drink.", "", 2.62814, new double[] { -1.7, -0.5385, 0.7023, 1.5886 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("PEXP07", "", "Alcohol makes it easier to talk to people.", "", 2.19785, new double[] { -1.3257, -0.3544, 0.7648, 1.6944 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("PEXP09", "", "Drinking eases physical pain.", "", 1.40533, new double[] { -1.0388, 0.1517, 1.5505, 2.7652 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("PEXP11", "", "People forget their problems when they drink.", "", 1.87105, new double[] { -1.1257, -0.1827, 1.0187, 1.9565 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("PEXP12", "", "Drinking improves a person's mood.", "", 1.86412, new double[] { -1.0219, 0.1512, 1.7423, 2.6789 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("PEXP13", "", "People feel happy when they drink.", "", 2.51175, new double[] { -1.7952, -0.5333, 0.8601, 1.9501 }, -1, "", 
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