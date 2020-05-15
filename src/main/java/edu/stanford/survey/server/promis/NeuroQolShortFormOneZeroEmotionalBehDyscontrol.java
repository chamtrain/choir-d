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
 * Item bank for PROMIS assessment. Generated from OID 973A8747-D029-4547-AA6C-66FE943037C6.
 */
public class NeuroQolShortFormOneZeroEmotionalBehDyscontrol {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("EDANG42", "In the past 7 days", "I had trouble controlling my temper", "", 2.67203, new double[] { -0.1397, 0.8976, 1.9405, 2.7996 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPER05", "In the past 7 days", "It was hard to control my behavior", "", 2.84685, new double[] { -0.0016, 0.9465, 2.1141, 2.9448 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPER06", "In the past 7 days", "I said or did things without thinking", "", 2.54842, new double[] { -0.593, 0.4369, 1.7508, 2.7897 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPER07", "In the past 7 days", "I got impatient with other people", "", 3.11799, new double[] { -1.1973, -0.0493, 1.0672, 2.1818 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPER11", "In the past 7 days", "I was irritable around other people", "", 2.98734, new double[] { -0.5516, 0.4324, 1.561, 2.3579 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPER12", "In the past 7 days", "I was bothered by little things", "", 3.17743, new double[] { -0.9556, 0.0154, 1.1723, 2.1197 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPER17", "In the past 7 days", "I became easily upset", "", 3.60515, new double[] { -0.498, 0.3585, 1.2771, 2.0088 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPER19", "In the past 7 days", "I was in conflict with others", "", 2.70232, new double[] { -0.5444, 0.6502, 1.7875, 2.6579 }, -1, "",
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
