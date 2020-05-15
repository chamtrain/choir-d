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
 * Item bank for PROMIS assessment. Generated from OID 8D8D0C3E-994A-4E88-BFD0-7292E35A6FDD.
 */
public class PromisParentProxyShortFormOneZeroPositiveAffect8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("SWB_P_001_PXR1", "In the past 7 days,", "My child felt calm.", "", 2.098, new double[] { -3.978, -2.209, -0.765, 0.937 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_004_PXR1", "In the past 7 days,", "My child felt peaceful.", "", 2.871, new double[] { -3.385, -2.139, -0.733, 0.649 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
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
      ),
      item("SWB_P_037_PXR1", "In the past 7 days,", "My child was in a good mood.", "", 2.97, new double[] { -3.602, -2.424, -1.14, 0.734 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_049_PXR1", "In the past 7 days,", "My child felt refreshed.", "", 3.015, new double[] { -3.027, -2.016, -0.563, 0.943 }, -1, "",
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
