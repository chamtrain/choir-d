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
 * Item bank for PROMIS assessment. Generated from OID 82A96E84-B1DF-4105-A901-658E25242EC4.
 */
public class NeuroQolBankOneZeroAnxiety {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("EDANX05", "In the past 7 days", "I felt anxious", "", 3.05854, new double[] { -0.7377, 0.0336, 0.9391, 1.7192 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX07", "In the past 7 days", "I felt like I needed help for my anxiety", "", 2.93979, new double[] { 0.126, 0.68, 1.434, 1.9685 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX18", "In the past 7 days", "I had sudden feelings of panic", "", 3.44614, new double[] { 0.1956, 0.945, 1.5723, 2.2883 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX20", "In the past 7 days", "I was easily startled", "", 2.07768, new double[] { -0.2514, 0.6111, 1.4756, 2.2568 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX26", "In the past 7 days", "I felt fidgety", "", 2.96216, new double[] { -0.267, 0.43, 1.2885, 1.9618 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX27", "In the past 7 days", "I felt something awful would happen", "", 3.23614, new double[] { -0.0085, 0.6078, 1.3955, 2.0268 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX30", "In the past 7 days", "I felt worried", "", 3.01128, new double[] { -0.8232, 0.015, 0.8987, 1.5664 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX41", "In the past 7 days", "My worries overwhelmed me", "", 3.99181, new double[] { 0.1026, 0.6641, 1.2992, 1.9074 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX46", "In the past 7 days", "I felt nervous", "", 4.29395, new double[] { -0.3869, 0.3746, 1.0974, 1.766 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX48", "In the past 7 days", "Many situations made me worry", "", 4.36415, new double[] { -0.3453, 0.4511, 1.0694, 1.6314 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX49", "In the past 7 days", "I had difficulty sleeping", "", 1.52153, new double[] { -0.7661, 0.065, 0.9806, 1.8113 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX51", "In the past 7 days", "I had trouble relaxing", "", 2.95388, new double[] { -0.4777, 0.2946, 1.0536, 1.8134 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX53", "In the past 7 days", "I felt uneasy", "", 5.52022, new double[] { -0.3203, 0.4249, 1.0894, 1.7105 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX54", "In the past 7 days", "I felt tense", "", 4.06713, new double[] { -0.4382, 0.2261, 1.0636, 1.6983 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX55", "In the past 7 days", "I had difficulty calming down", "", 3.2996, new double[] { -0.0276, 0.6604, 1.4124, 2.0035 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQANX02", "In the past 7 days", "I felt fearful about my future", "", 2.33805, new double[] { -0.7343, 0.1357, 0.882, 1.6937 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQANX04", "In the past 7 days", "I worried about my physical health", "", 1.39548, new double[] { -1.0502, -0.0324, 1.1028, 2.1702 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQANX07", "In the past 7 days", "I felt nervous when my normal routine was disturbed", "", 3.00991, new double[] { -0.2979, 0.3943, 1.1602, 1.9086 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQANX17", "In the past 7 days", "I suddenly felt scared for no reason", "", 2.46306, new double[] { 0.7454, 1.3098, 2.0253, 2.558 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQANX18", "In the past 7 days", "I worried about dying", "", 1.64134, new double[] { 0.4759, 1.2325, 2.3264, 2.8897 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQANX21", "In the past 7 days", "I felt shy", "", 1.63541, new double[] { -0.1843, 0.7256, 1.5224, 2.2499 }, -1, "",
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
