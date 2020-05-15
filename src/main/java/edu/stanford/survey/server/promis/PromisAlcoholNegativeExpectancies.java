package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import static edu.stanford.survey.server.ItemBanks.*;
/**
 * Item bank for PROMIS generated from: Alcohol Negative Expectancies E9F0500B-6305-4924-A3B5-A7B3357AF15D.json
 */

public class PromisAlcoholNegativeExpectancies {

  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 11, 3.0, 
  item("NEXP01", "", "People have trouble thinking when they drink.", "", 2.47101, new double[] { -2.1357, -0.9843, 0.2886, 1.2252 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("NEXP02", "", "People feel sick the day after drinking.", "", 2.03657, new double[] { -1.9701, -0.6572, 0.6862, 1.6721 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("NEXP03", "", "People do things they regret while drinking.", "", 3.00069, new double[] { -2.3198, -1.055, 0.0452, 0.9321 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("NEXP04", "", "People make bad decisions when they drink.", "", 3.19195, new double[] { -2.2336, -1.1311, 0.0049, 0.8821 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("NEXP05", "", "Drinking is harmful to mental health.", "", 2.02054, new double[] { -1.711, -0.6317, 0.4561, 1.1173 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("NEXP06", "", "People are careless when they drink.", "", 3.32191, new double[] { -2.1194, -0.972, 0.1717, 1.0661 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("NEXP07", "", "People are irresponsible when they drink.", "", 3.14977, new double[] { -2.061, -0.8478, 0.1961, 1.0433 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("NEXP08", "", "People are pushy when they drink.", "", 2.42302, new double[] { -1.8855, -0.6602, 0.6901, 1.6539 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("NEXP09", "", "People are rude when they drink.", "", 2.63998, new double[] { -1.9757, -0.6841, 0.6679, 1.5751 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("NEXP10", "", "Drinking can be harmful to physical health.", "", 1.90333, new double[] { -2.6082, -1.3292, -0.1673, 0.6847 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("NEXP14", "", "People are selfish when they drink.", "", 1.89417, new double[] { -1.3955, -0.3515, 0.9184, 1.8777 }, -1, "", 
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