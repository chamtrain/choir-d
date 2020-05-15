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
 * Item bank for PROMIS assessment. Generated from OID 3F7BD7BF-DD2D-46F3-B6C6-769BAB07A1F1.
 */
public class PromisShortFormOneZeroDepression8b {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
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
      item("EDDEP17", "In the past 7 days", "I felt sad", "", 3.27403, new double[] { -0.4985, 0.4059, 1.4131, 2.3755 }, -1, "",
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
      item("EDDEP29", "In the past 7 days", "I felt depressed", "", 4.34292, new double[] { -0.1173, 0.5977, 1.4282, 2.2725 }, -1, "",
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
      item("EDDEP41", "In the past 7 days", "I felt hopeless", "", 4.45416, new double[] { 0.5584, 1.0742, 1.7793, 2.5301 }, -1, "",
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
