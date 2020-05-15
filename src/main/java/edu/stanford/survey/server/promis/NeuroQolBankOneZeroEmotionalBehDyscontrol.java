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
 * Item bank for PROMIS assessment. Generated from OID 71D33441-51AD-4F3D-BFAC-D77A4C31639C.
 */
public class NeuroQolBankOneZeroEmotionalBehDyscontrol {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("EDANG09", "In the past 7 days", "I felt angry", "", 1.8694, new double[] { -1.0752, 0.2868, 1.6635, 3.0941 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG31", "In the past 7 days", "I was stubborn with others", "", 2.41506, new double[] { -0.7727, 0.2668, 1.4196, 2.367 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
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
      item("NQPER08", "In the past 7 days", "I felt impulsive", "", 1.98463, new double[] { -0.7091, 0.4793, 1.8967, 3.1277 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPER09", "In the past 7 days", "People told me that I talked in a loud or excessive manner", "", 1.61501, new double[] { 0.4335, 1.3373, 2.3809, 3.3896 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPER10", "In the past 7 days", "I said or did things that other people probably thought were inappropriate", "", 2.23161, new double[] { -0.0133, 1.0041, 2.246, 3.3185 }, -1, "",
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
      item("NQPER13", "In the past 7 days", "I suddenly became emotional for no reason", "", 2.2865, new double[] { -0.2575, 0.5676, 1.5016, 2.7528 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPER14", "In the past 7 days", "I felt restless", "", 1.76249, new double[] { -0.9515, -0.0158, 1.5015, 3.1229 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPER15", "In the past 7 days", "It was hard to adjust to unexpected changes", "", 2.15697, new double[] { -0.5207, 0.4149, 1.5688, 2.5348 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPER16", "In the past 7 days", "I had a hard time accepting criticism from other people", "", 2.32386, new double[] { -0.6616, 0.3676, 1.3008, 1.9902 }, -1, "",
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
      ),
      item("NQPER20", "In the past 7 days", "I threatened violence toward people or property", "", 2.05079, new double[] { 1.5712, 2.5151, 3.0389, 3.5242 }, -1, "",
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
