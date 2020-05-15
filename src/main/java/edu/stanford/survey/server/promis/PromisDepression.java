/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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
 * Item bank for PROMIS pain interference assessment corresponding to what Northwestern
 * used for the version 1 XML API.
 */
public class PromisDepression {
  private static final ItemBank bank = itemBank(9.02, 6.18, 5, 20, 3.0,
      item("EDDEP04", "In the past 7 days", "I felt worthless", "worthless", 4.26142, new double[] { 0.4011, 0.9757, 1.6963, 2.4441 }, 3, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP05", "In the past 7 days", "I felt that I had nothing to look forward to", "nothing to look forward to", 3.93174, new double[] { 0.3049, 0.9131, 1.5935, 2.4117 }, 3, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP06", "In the past 7 days", "I felt helpless", "helpless", 4.14476, new double[] { 0.3501, 0.9153, 1.6782, 2.4705 }, 3, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP07", "In the past 7 days", "I withdrew from other people", "withdrew from other people", 2.80180, new double[] { 0.1477, 0.7723, 1.6027, 2.5381 }, 1, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP09", "In the past 7 days", "I felt that nothing could cheer me up", "nothing could cheer me up", 3.65743, new double[] { 0.3120, 0.9818, 1.7821, 2.5711 }, 2, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP14", "In the past 7 days", "I felt that I was not as good as other people", "not as good as other people", 2.33338, new double[] { 0.1860, 0.9473, 1.7288, 2.6326 }, 1, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP17", "In the past 7 days", "I felt sad", "sad", 3.27403, new double[] { -0.4985, 0.4059, 1.4131, 2.3755 }, 2, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP19", "In the past 7 days", "I felt that I wanted to give up on everything", "wanted to give up on everything", 3.24097, new double[] { 0.4605, 1.0344, 1.8336, 2.5147 }, 2, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP21", "In the past 7 days", "I felt that I was to blame for things", "I was to blame for things", 2.73610, new double[] { 0.0725, 0.8098, 1.8031, 2.6734 }, 1, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP22", "In the past 7 days", "I felt like a failure", "like a failure", 3.97003, new double[] { 0.2038, 0.7955, 1.6487, 2.2955 }, 3, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP23", "In the past 7 days", "I had trouble feeling close to people", "trouble feeling close to people", 2.56443, new double[] { -0.0384, 0.6927, 1.6528, 2.5836 }, 1, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP26", "In the past 7 days", "I felt disappointed in myself", "disappointed in myself", 3.09337, new double[] { -0.3576, 0.4125, 1.4039, 2.2240 }, 2, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP27", "In the past 7 days", "I felt that I was not needed", "I was not needed", 2.92006, new double[] { 0.2043, 0.8909, 1.6547, 2.5284 }, 2, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP28", "In the past 7 days", "I felt lonely", "lonely", 2.58834, new double[] { -0.0791, 0.6326, 1.4773, 2.3277 }, 1, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP29", "In the past 7 days", "I felt depressed", "depressed", 4.34292, new double[] { -0.1173, 0.5977, 1.4282, 2.2725 }, 3, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP30", "In the past 7 days", "I had trouble making decisions", "trouble making decisions", 2.61285, new double[] { -0.0234, 0.8684, 1.8643, 2.8263 }, 1, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP31", "In the past 7 days", "I felt discouraged about the future", "discouraged about the future", 3.18287, new double[] { -0.2609, 0.3968, 1.3055, 2.1340 }, 2, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP35", "In the past 7 days", "I found that things in my life were overwhelming", "things in my life were overwhelming", 3.10586, new double[] { 0.0437, 0.7224, 1.6388, 2.4715 }, 2, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP36", "In the past 7 days", "I felt unhappy", "unhappy", 3.48301, new double[] { -0.5359, 0.3476, 1.3468, 2.3548 }, 2, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP39", "In the past 7 days", "I felt I had no reason for living", "no reason for living", 3.13121, new double[] { 0.9180, 1.4813, 2.1640, 2.8564 }, 2, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP41", "In the past 7 days", "I felt hopeless", "hopeless", 4.45416, new double[] { 0.5584, 1.0742, 1.7793, 2.5301 }, 3, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP42", "In the past 7 days", "I felt ignored by people", "ignored by people", 2.36441, new double[] { 0.2101, 0.9871, 1.9059, 2.9338 }, 1, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP44", "In the past 7 days", "I felt upset for no reason", "upset for no reason", 2.54916, new double[] { 0.1935, 1.0117, 2.0131, 3.1265 }, 1, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP45", "In the past 7 days", "I felt that nothing was interesting", "nothing was interesting", 2.83360, new double[] { 0.1407, 0.9065, 1.8461, 2.8752 }, 1, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP46", "In the past 7 days", "I felt pessimistic", "pessimistic", 2.38063, new double[] { -0.4579, 0.4780, 1.5457, 2.6315 }, 1, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP48", "In the past 7 days", "I felt that my life was empty", "my life was empty", 3.18524, new double[] { 0.1981, 0.7819, 1.5258, 2.3241 }, 2, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP50", "In the past 7 days", "I felt guilty", "guilty", 2.01810, new double[] { -0.0504, 0.9259, 1.9995, 2.9655 }, 1, "categoryC",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP54", "In the past 7 days", "I felt emotionally exhausted", "emotionally exhausted", 2.68530, new double[] { -0.2988, 0.4235, 1.3579, 2.3076 }, 1, "categoryC",
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
