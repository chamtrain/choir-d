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
 * Item bank for PROMIS assessment. Generated from OID 8BDE0A54-D5D0-4102-B3F7-16CB5E04AA33.
 */
public class NeuroQolShortFormOneZeroSleepDisturbance {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQSLP02", "In the past 7 days", "I had to force myself to get up in the morning", "", 1.58794, new double[] { -0.5879, 0.3225, 1.3287, 2.2905 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSLP03", "In the past 7 days", "I had trouble stopping my thoughts at bedtime", "", 2.30312, new double[] { -0.5896, 0.1447, 1.0281, 2.0049 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSLP04", "In the past 7 days", "I was sleepy during the daytime", "", 1.60035, new double[] { -1.821, -0.7657, 0.6945, 1.9469 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSLP05", "In the past 7 days", "I had trouble sleeping because of bad dreams", "", 1.66502, new double[] { 0.5271, 1.5699, 2.5337, 3.5181 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSLP07", "In the past 7 days", "I had difficulty falling asleep", "", 2.23852, new double[] { -0.6242, 0.2845, 1.2643, 2.1503 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSLP12", "In the past 7 days", "Pain woke me up", "", 1.33617, new double[] { 0.054, 0.8407, 2.0007, 3.4493 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSLP13", "In the past 7 days", "I avoided or cancelled activities with my friends because I was tired from having a bad night's sleep", "", 2.46753, new double[] { 0.4991, 1.1223, 2.0856, 2.9696 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSLP18", "In the past 7 days", "I felt physically tense during the middle of the night or early morning hours", "", 1.80312, new double[] { 0.5693, 1.1263, 2.3135, 3.7588 }, -1, "",
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
