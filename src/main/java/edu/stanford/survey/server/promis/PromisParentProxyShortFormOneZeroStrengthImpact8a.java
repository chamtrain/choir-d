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
 * Item bank for PROMIS assessment. Generated from OID F6F85739-F288-41DC-B905-6A51430303C1.
 */
public class PromisParentProxyShortFormOneZeroStrengthImpact8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("PAC_S_015_PXR1", "In the past 7 days,", "How many days was your child strong enough to carry heavy things with his/her hands?", "", 4.137, new double[] { -2.027, -1.853, -1.606, -1.374 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_016_PXR1", "In the past 7 days,", "How many days was your child strong enough to go up and down stairs?", "", 4.728, new double[] { -2.114, -1.938, -1.831, -1.683 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_017_PXR1", "In the past 7 days,", "How many days was your child strong enough to reach above his/her head to get heavy things?", "", 4.27, new double[] { -1.858, -1.71, -1.516, -1.243 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_018_PXR1", "In the past 7 days,", "How many days was your child strong enough to open a heavy door?", "", 5.104, new double[] { -2.01, -1.854, -1.598, -1.34 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_022_PXR1", "In the past 7 days,", "How many days was your child strong enough to jump up and down?", "", 4.56, new double[] { -2.229, -2.035, -1.763, -1.591 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_023_PXR1", "In the past 7 days,", "How many days was your child strong enough to pour a drink from a full pitcher or carton?", "", 3.056, new double[] { -2.114, -1.926, -1.704, -1.421 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_025_PXR1", "In the past 7 days,", "How many days was your child strong enough to open a jar by himself/herself?", "", 2.685, new double[] { -1.914, -1.737, -1.452, -1.192 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_S_113_PXR1", "In the past 7 days,", "How many days was your child strong enough to lift heavy things over his/her head?", "", 4.405, new double[] { -1.815, -1.688, -1.503, -1.188 }, -1, "",
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
