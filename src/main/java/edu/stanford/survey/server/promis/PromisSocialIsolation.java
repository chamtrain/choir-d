package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;

import static edu.stanford.survey.server.ItemBanks.*;

/**
 * Item bank for PROMIS generated from: Social Isolation 8D79D960-EC12-4242-AE66-03EF8EF4D61D.json
 */
public class PromisSocialIsolation {

  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
    item("CARES23x", "", "I find that friends or relatives have difficulty talking with me about my health", "", 1.77884, new double[] { -0.415, 0.6001, 1.8469, 2.653 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("Iso-CaPS1", "", "I feel isolated even when I am not alone", "", 3.7812, new double[] { -0.3641, 0.4074, 1.3811, 1.9137 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("Iso-CaPS2", "", "I feel that people avoid talking to me", "", 3.23796, new double[] { -0.2789, 0.6725, 1.7752, 2.2087 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("Iso-CaPS3", "", "I feel detached from other people", "", 4.01632, new double[] { -0.5802, 0.1805, 1.0973, 1.7709 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("Iso-CaPS9", "", "I feel like a stranger to those around me", "", 4.24321, new double[] { -0.2751, 0.4727, 1.4172, 1.9278 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SCSC2x3 ", "", "People get the wrong idea about my situation", "", 2.27574, new double[] { -1.0466, 0.0024, 1.1942, 1.8983 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SCSC3x2", "", "I feel that some of my friends avoid me", "", 2.96488, new double[] { -0.2113, 0.708, 1.7942, 2.4202 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("SS10x", "", "I feel that some of my family members avoid me", "", 2.11332, new double[] { -0.1423, 0.6663, 1.8113, 2.3418 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("UCLA11x2", "", "I feel left out", "", 3.86945, new double[] { -0.6775, 0.2171, 1.1507, 1.9481 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("UCLA13x3 ", "", "I feel that people barely know me", "", 3.15724, new double[] { -0.8517, 0.0587, 1.1001, 1.8146 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("UCLA14x2", "", "I feel isolated from others", "", 4.2454, new double[] { -0.473, 0.2612, 1.1182, 1.6763 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("UCLA18x2", "", "I feel that people are around me but not with me", "", 3.98622, new double[] { -0.6642, 0.1747, 1.1369, 1.7946 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("UCLA7x2", "", "I feel that I am no longer close to anyone", "", 3.36637, new double[] { -0.2081, 0.427, 1.2019, 1.7682 }, -1, "", 
      response("Never", 1),
      response("Rarely", 2),
      response("Sometimes", 3),
      response("Usually", 4),
      response("Always", 5)
    ),
    item("UCLA8x3 ", "", "I feel that I am alone in my interests and ideas", "", 2.81482, new double[] { -0.8041, 0.0944, 1.1021, 1.9234 }, -1, "", 
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