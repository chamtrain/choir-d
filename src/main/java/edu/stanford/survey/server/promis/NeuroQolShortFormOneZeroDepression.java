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
 * Item bank for PROMIS assessment. Generated from OID B04E56A8-53D4-47F0-A02D-477925E7DEE3.
 */
public class NeuroQolShortFormOneZeroDepression {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("EDDEP04", "In the past 7 days", "I felt worthless", "", 4.77346, new double[] { -0.0968, 0.2889, 1.0307, 1.6231 }, -1, "",
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
      item("EDDEP29", "In the past 7 days", "I felt depressed", "", 5.78729, new double[] { -0.3129, 0.2181, 0.9407, 1.417 }, -1, "",
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
      item("EDDEP48", "In the past 7 days", "I felt that my life was empty", "", 4.98996, new double[] { -0.0259, 0.3725, 1.0634, 1.6516 }, -1, "",
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
