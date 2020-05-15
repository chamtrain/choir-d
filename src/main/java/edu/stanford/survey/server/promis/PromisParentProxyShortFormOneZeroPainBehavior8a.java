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
 * Item bank for PROMIS assessment. Generated from OID D568889C-B1F4-4EF5-9E27-3315AC528E4F.
 */
public class PromisParentProxyShortFormOneZeroPainBehavior8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("PAINBE46_PROX", "In the past 7 days, when my child was in pain", "he/she protected the part of his/her body that hurt.", "", 2.34, new double[] { -2.71, -1.02, -0.5, 0.53, 1.21 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PAINBE4_PROX", "In the past 7 days, when my child was in pain", "he/she asked for medicine.", "", 1.98, new double[] { -2.83, -0.92, -0.43, 0.7, 1.32 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PAINBE5_PROX", "In the past 7 days, when my child was in pain", "he/she talked about his/her pain.", "", 2.28, new double[] { -2.77, -1.3, -0.61, 0.52, 1.28 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PAINBE8_PROX", "In the past 7 days, when my child was in pain", "he/she moved slower.", "", 2.59, new double[] { -3.01, -1.3, -0.81, 0.33, 1.0 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PBNEW24_PROX", "In the past 7 days, when my child was in pain", "he/she asked for someone to help him/her.", "", 2.81, new double[] { -2.53, -0.65, -0.16, 0.91, 1.23 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PBNEW2_PROX", "In the past 7 days, when my child was in pain", "it showed on his/her face.", "", 2.94, new double[] { -2.68, -1.11, -0.64, 0.33, 1.07 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PBNEW32_PROX", "In the past 7 days, when my child was in pain", "he/she lay down.", "", 2.62, new double[] { -2.53, -1.22, -0.78, 0.37, 0.86 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PBNEW_PROX15", "In the past 7 days, when my child was in pain", "he/she had to stop what he/she was doing.", "", 3.02, new double[] { -2.34, -0.76, -0.27, 0.85, 1.4 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
