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
 * Item bank for PROMIS assessment. Generated from OID 233C714B-3B13-4895-841A-D120B1C0E9F2.
 */
public class NeuroQolShortFormOneZeroAbilityToPartInSRA {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQPRF01", "In the past 7 days", "I can keep up with my family responsibilities", "", 3.87096, new double[] { -2.2799, -1.6637, -0.985, -0.3689 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPRF03", "In the past 7 days", "I am able to do all of my regular family activities", "", 4.52693, new double[] { -1.876, -1.4403, -0.7972, -0.2821 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPRF08", "In the past 7 days", "I am able to socialize with my friends", "", 3.73361, new double[] { -1.7862, -1.1625, -0.524, -0.0805 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPRF09", "In the past 7 days", "I am able to do all of my regular activities with friends", "", 5.27177, new double[] { -1.5401, -1.014, -0.5058, -0.0611 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPRF17", "In the past 7 days", "I can keep up with my social commitments", "", 5.47607, new double[] { -1.6737, -1.0828, -0.6184, -0.1237 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPRF26", "In the past 7 days", "I am able to participate in leisure activities", "", 5.00188, new double[] { -1.7589, -1.2822, -0.5052, 0.0281 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPRF32", "In the past 7 days", "I am able to perform my daily routines", "", 5.91819, new double[] { -1.7836, -1.3472, -0.7761, -0.3297 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPRF34", "In the past 7 days", "I can keep up with my work responsibilities (include work at home)", "", 5.63317, new double[] { -1.5789, -1.1749, -0.5997, -0.1874 }, -1, "",
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
