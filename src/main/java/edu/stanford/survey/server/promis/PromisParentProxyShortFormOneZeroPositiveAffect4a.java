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
 * Item bank for PROMIS assessment. Generated from OID 96C4F220-9E1B-4336-994C-5BF83F7554A6.
 */
public class PromisParentProxyShortFormOneZeroPositiveAffect4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("SWB_P_025_PXR1", "In the past 7 days,", "My child felt great.", "", 4.234, new double[] { -2.92, -1.973, -0.88, 0.525 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_026_PXR1", "In the past 7 days,", "My child felt cheerful.", "", 4.688, new double[] { -3.016, -2.167, -0.921, 0.532 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_027_PXR1", "In the past 7 days,", "My child felt happy.", "", 3.812, new double[] { -2.878, -2.326, -1.225, 0.458 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_029_PXR1", "In the past 7 days,", "My child felt joyful.", "", 4.946, new double[] { -2.799, -1.792, -0.741, 0.564 }, -1, "",
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
