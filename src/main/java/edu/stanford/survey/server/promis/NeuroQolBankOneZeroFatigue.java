/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
 * All Rights Reserved.
 *
 * See the NOTICE and LICENSE files distributed with this work for information
 * regarding copyright ownership and licensing. You may not use this file except
 * in compliance with a written license agreement with Stanford University.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See your
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;

import static edu.stanford.survey.server.ItemBanks.*;

/**
 * Item bank for PROMIS assessment. Generated from OID 425D9AC5-3549-49C6-8100-46DCC027129B.
 */
public class NeuroQolBankOneZeroFatigue {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQFTG01", "In the past 7 days", "I needed help doing my usual activities because of my fatigue", "", 2.71903, new double[] { -0.6824, 0.003, 0.9387, 1.8593 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG02", "In the past 7 days", "I had to limit my social activity because I was tired", "", 3.60876, new double[] { -0.7494, -0.1345, 0.7531, 1.9085 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG03", "In the past 7 days", "I needed to sleep during the day", "", 1.88783, new double[] { -1.2046, -0.4079, 0.8369, 1.8845 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG04", "In the past 7 days", "I had trouble starting things because I was too tired", "", 3.84357, new double[] { -0.9201, -0.249, 0.825, 1.8828 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG05", "In the past 7 days", "I had trouble finishing things because I was too tired", "", 3.73878, new double[] { -1.0477, -0.2963, 0.7967, 1.9179 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG06", "In the past 7 days", "I was too tired to do my household chores", "", 4.24433, new double[] { -0.9616, -0.2507, 0.6647, 1.6671 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG07", "In the past 7 days", "I was too tired to leave the house", "", 3.94218, new double[] { -0.6045, 0.0461, 0.9439, 1.9105 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG08", "In the past 7 days", "I was too tired to take a short walk", "", 2.97454, new double[] { -0.6841, -0.0853, 0.6893, 1.5742 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG09", "In the past 7 days", "I was too tired to eat", "", 2.71085, new double[] { -0.1995, 0.6916, 1.8149, 2.7211 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG10", "In the past 7 days", "I was frustrated by being too tired to do the things I wanted to do", "", 4.14659, new double[] { -0.7203, -0.2442, 0.4339, 1.1719 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG11", "In the past 7 days", "I felt that I had no energy", "", 4.57819, new double[] { -1.1769, -0.4209, 0.3346, 1.2972 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG12", "In the past 7 days", "I was so tired that I needed to rest during the day", "", 3.52273, new double[] { -1.1087, -0.3817, 0.6168, 1.4201 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG13", "In the past 7 days", "I felt exhausted", "", 4.68427, new double[] { -0.9271, -0.255, 0.5969, 1.4212 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG14", "In the past 7 days", "I felt tired", "", 3.99133, new double[] { -1.6421, -0.737, 0.3109, 1.3436 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG15", "In the past 7 days", "I felt  fatigued", "", 4.52634, new double[] { -1.3033, -0.4729, 0.4058, 1.3654 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG16", "In the past 7 days", "I felt weak all over", "", 3.13259, new double[] { -0.6647, 0.0435, 0.8894, 1.688 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG17", "In the past 7 days", "I needed help doing my usual activities because of weakness", "", 3.30362, new double[] { -0.2687, 0.3564, 1.2033, 2.0878 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG18", "In the past 7 days", "I had to limit my social activity because I was physically weak", "", 3.28957, new double[] { -0.2825, 0.3586, 1.0424, 1.8479 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQFTG20", "In the past 7 days", "I had to force myself to get up and do things because I was physically too weak", "", 3.15038, new double[] { -0.3644, 0.2649, 1.0437, 2.0103 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
