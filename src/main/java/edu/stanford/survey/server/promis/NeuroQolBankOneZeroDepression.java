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
 * Item bank for PROMIS assessment. Generated from OID EAA1BBC4-6D00-44D5-846A-9C13BB680151.
 */
public class NeuroQolBankOneZeroDepression {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("EDDEP04", "In the past 7 days", "I felt worthless", "", 4.77346, new double[] { -0.0968, 0.2889, 1.0307, 1.6231 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP05", "In the past 7 days", "I felt that I had nothing to look forward to", "", 4.43495, new double[] { -0.2063, 0.3666, 0.8694, 1.5424 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP06", "In the past 7 days", "I felt helpless", "", 4.3185, new double[] { -0.2198, 0.3672, 0.9833, 1.5348 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP07", "In the past 7 days", "I withdrew from other people", "", 3.47041, new double[] { -0.1968, 0.2768, 1.0306, 1.7071 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP09", "In the past 7 days", "I felt that nothing could cheer me up", "", 4.66993, new double[] { -0.1071, 0.4488, 1.1186, 1.7643 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP17", "In the past 7 days", "I felt sad", "", 3.7147, new double[] { -0.7163, -0.0215, 0.7945, 1.5403 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP19", "In the past 7 days", "I felt that I wanted to give up on everything", "", 4.52366, new double[] { 0.0474, 0.4362, 1.0276, 1.6598 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP28", "In the past 7 days", "I felt lonely", "", 3.6841, new double[] { -0.3198, 0.1887, 0.9226, 1.6472 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP29", "In the past 7 days", "I felt depressed", "", 5.78729, new double[] { -0.3129, 0.2181, 0.9407, 1.417 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP31", "In the past 7 days", "I felt discouraged about the future", "", 3.99322, new double[] { -0.5207, 0.052, 0.6791, 1.3266 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP35", "In the past 7 days", "I found that things in my life were overwhelming", "", 3.44407, new double[] { -0.2809, 0.2478, 1.0303, 1.6802 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP36", "In the past 7 days", "I felt unhappy", "", 4.69529, new double[] { -0.6883, 0.0064, 0.8354, 1.7386 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP39", "In the past 7 days", "I felt I had no reason for living", "", 4.38096, new double[] { 0.379, 0.777, 1.3304, 1.9153 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP41", "In the past 7 days", "I felt hopeless", "", 5.24141, new double[] { 0.0199, 0.4926, 1.1465, 1.7244 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP45", "In the past 7 days", "I felt that nothing was interesting", "", 4.12492, new double[] { -0.08, 0.4909, 1.2155, 1.9069 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP46", "In the past 7 days", "I felt pessimistic", "", 2.76073, new double[] { -0.4563, 0.2597, 1.0649, 1.793 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP48", "In the past 7 days", "I felt that my life was empty", "", 4.98996, new double[] { -0.0259, 0.3725, 1.0634, 1.6516 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDDEP54", "In the past 7 days", "I felt emotionally exhausted", "", 3.58897, new double[] { -0.2845, 0.1688, 0.9415, 1.5427 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQDEP06", "In the past 7 days", "I felt that everything I did was an effort", "", 2.66443, new double[] { -0.5417, 0.0809, 0.9176, 1.4952 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQDEP08", "In the past 7 days", "I was critical of myself for my mistakes", "", 2.66649, new double[] { -0.6697, -0.0625, 0.8761, 1.5897 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQDEP20", "In the past 7 days", "I felt unloved", "", 3.22771, new double[] { -0.0802, 0.4298, 1.1598, 1.6957 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQDEP26", "In the past 7 days", "I had trouble keeping my mind on what I was doing", "", 2.42462, new double[] { -0.5014, 0.234, 1.2949, 2.1432 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQDEP29", "In the past 7 days", "I felt like I needed help for my depression", "", 3.25237, new double[] { 0.251, 0.6651, 1.175, 1.6306 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQDEP30", "In the past 7 days", "I had trouble enjoying things that I used to enjoy", "", 3.88535, new double[] { -0.0955, 0.3903, 1.0775, 1.5755 }, -1, "",
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
