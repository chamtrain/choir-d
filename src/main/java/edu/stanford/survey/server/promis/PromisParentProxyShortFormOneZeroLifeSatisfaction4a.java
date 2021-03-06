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
 * Item bank for PROMIS assessment. Generated from OID CA7ED548-66CA-44AF-B9A4-5AFE2CC1D063.
 */
public class PromisParentProxyShortFormOneZeroLifeSatisfaction4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("SWB_LS_019_PXR1", "Thinking about the past 4 weeks,", "My child had what he/she wanted in life.", "", 2.887, new double[] { -2.582, -1.958, -1.038, 0.116 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_046_PXR1", "Thinking about the past 4 weeks,", "My child was satisfied with his/her life.", "", 3.851, new double[] { -2.335, -1.948, -1.259, -0.255 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_048_PXR1", "Thinking about the past 4 weeks,", "My child was happy with his/her life.", "", 4.975, new double[] { -2.414, -1.878, -1.228, -0.303 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_051_PXR1", "Thinking about the past 4 weeks,", "My child had a good life.", "", 4.322, new double[] { -2.644, -2.284, -1.537, -0.542 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
