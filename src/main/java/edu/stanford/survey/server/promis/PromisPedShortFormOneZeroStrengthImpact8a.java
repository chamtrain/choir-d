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
 * Item bank for PROMIS assessment. Generated from OID A5AC2206-7197-4AFE-83FA-B1A3A69E278B.
 */
public class PromisPedShortFormOneZeroStrengthImpact8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("PAC_S_015R1", "In the past 7 days,", "How many days were you strong enough to carry heavy things with your hands?", "", 3.782, new double[] { -2.105, -1.737, -1.419, -1.128 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_016R1", "In the past 7 days,", "How many days were you strong enough to go up and down stairs?", "", 6.247, new double[] { -2.128, -1.923, -1.706, -1.497 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_017R1", "In the past 7 days,", "How many days were you strong enough to reach above your head to get heavy things?", "", 4.43, new double[] { -1.994, -1.784, -1.527, -1.203 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_018R1", "In the past 7 days,", "How many days were you strong enough to open a heavy door?", "", 5.631, new double[] { -2.04, -1.839, -1.589, -1.316 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_022R1", "In the past 7 days,", "How many days were you strong enough to jump up and down?", "", 5.372, new double[] { -2.162, -1.895, -1.659, -1.387 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_023R1", "In the past 7 days,", "How many days were you strong enough to pour a drink from a full pitcher or carton?", "", 5.072, new double[] { -2.133, -1.918, -1.679, -1.474 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_025R1", "In the past 7 days,", "How many days were you strong enough to open a jar by yourself?", "", 3.085, new double[] { -2.104, -1.857, -1.56, -1.249 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_113R1", "In the past 7 days,", "How many days were you strong enough to lift heavy things over your head?", "", 3.808, new double[] { -1.971, -1.694, -1.358, -1.068 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
