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
 * Item bank for PROMIS assessment. Generated from OID D3FFE67C-FE0D-4DB4-B680-CDDBBA3B360B.
 */
public class PromisCaBankOneZeroDepression {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("EDANG09", "In the past 7 days", "I felt angry", "", 2.20686, new double[] { -0.5881, 0.4663, 1.8098, 3.0878 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG29", "In the past 7 days", "I felt irritable", "", 2.29685, new double[] { -0.8322, 0.264, 1.4385, 2.7399 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP02", "In the past 7 days", "I felt lonely even when I was with other people", "", 3.02432, new double[] { 0.1705, 0.7765, 1.7905, 2.7724 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP04", "In the past 7 days", "I felt worthless", "", 4.26142, new double[] { 0.4011, 0.9757, 1.6963, 2.4441 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP05", "In the past 7 days", "I felt that I had nothing to look forward to", "", 3.93174, new double[] { 0.3049, 0.9131, 1.5935, 2.4117 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP06", "In the past 7 days", "I felt helpless", "", 4.14476, new double[] { 0.3501, 0.9153, 1.6782, 2.4705 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP07", "In the past 7 days", "I withdrew from other people", "", 2.8018, new double[] { 0.1477, 0.7723, 1.6027, 2.5381 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP09", "In the past 7 days", "I felt that nothing could cheer me up", "", 3.65743, new double[] { 0.312, 0.9818, 1.7821, 2.5711 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP12", "In the past 7 days", "I had mood swings", "", 2.37164, new double[] { -0.5407, 0.3483, 1.4711, 2.5607 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP14", "In the past 7 days", "I felt that I was not as good as other people", "", 2.33338, new double[] { 0.186, 0.9473, 1.7288, 2.6326 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP16", "In the past 7 days", "I felt like crying", "", 2.55088, new double[] { -0.3329, 0.4227, 1.4081, 2.2342 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP17", "In the past 7 days", "I felt sad", "", 3.27403, new double[] { -0.4985, 0.4059, 1.4131, 2.3755 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP19", "In the past 7 days", "I felt that I wanted to give up on everything", "", 3.24097, new double[] { 0.4605, 1.0344, 1.8336, 2.5147 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP21", "In the past 7 days", "I felt that I was to blame for things", "", 2.7361, new double[] { 0.0725, 0.8098, 1.8031, 2.6734 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP22", "In the past 7 days", "I felt like a failure", "", 3.97003, new double[] { 0.2038, 0.7955, 1.6487, 2.2955 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP23", "In the past 7 days", "I had trouble feeling close to people", "", 2.56443, new double[] { -0.0384, 0.6927, 1.6528, 2.5836 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP26", "In the past 7 days", "I felt disappointed in myself", "", 3.09337, new double[] { -0.3576, 0.4125, 1.4039, 2.224 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP27", "In the past 7 days", "I felt that I was not needed", "", 2.92006, new double[] { 0.2043, 0.8909, 1.6547, 2.5284 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP28", "In the past 7 days", "I felt lonely", "", 2.58834, new double[] { -0.0791, 0.6326, 1.4773, 2.3277 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP29", "In the past 7 days", "I felt depressed", "", 4.34292, new double[] { -0.1173, 0.5977, 1.4282, 2.2725 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP31", "In the past 7 days", "I felt discouraged about the future", "", 3.18287, new double[] { -0.2609, 0.3968, 1.3055, 2.134 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP35", "In the past 7 days", "I found that things in my life were overwhelming", "", 3.10586, new double[] { 0.0437, 0.7224, 1.6388, 2.4715 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP36", "In the past 7 days", "I felt unhappy", "", 3.48301, new double[] { -0.5359, 0.3476, 1.3468, 2.3548 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP38", "In the past 7 days", "I felt unloved", "", 2.40525, new double[] { 0.5626, 1.036, 1.9248, 2.7188 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP39", "In the past 7 days", "I felt I had no reason for living", "", 3.13121, new double[] { 0.918, 1.4813, 2.164, 2.8564 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP41", "In the past 7 days", "I felt hopeless", "", 4.45416, new double[] { 0.5584, 1.0742, 1.7793, 2.5301 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP46", "In the past 7 days", "I felt pessimistic", "", 2.38063, new double[] { -0.4579, 0.478, 1.5457, 2.6315 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP48", "In the past 7 days", "I felt that my life was empty", "", 3.18524, new double[] { 0.1981, 0.7819, 1.5258, 2.3241 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP54", "In the past 7 days", "I felt emotionally exhausted", "", 2.6853, new double[] { -0.2988, 0.4235, 1.3579, 2.3076 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP55", "In the past 7 days", "I felt like I needed help for my depression", "", 3.22289, new double[] { 0.3141, 0.8028, 1.598, 1.946 }, -1, "",
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
