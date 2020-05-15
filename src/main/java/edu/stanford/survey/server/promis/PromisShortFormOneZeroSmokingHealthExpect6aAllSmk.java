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
 * Item bank for PROMIS assessment. Generated from OID 01A817C5-ACB0-4D06-B46A-7E9BF43A7311.
 */
public class PromisShortFormOneZeroSmokingHealthExpect6aAllSmk {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
      item("SMKHLTH01", "", "Smoking is taking years off my life.", "", 2.64, new double[] { -1.46, -0.49, 0.26, 0.8 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH02", "", "Smoking makes me worry about getting heart troubles.", "", 2.96, new double[] { -0.93, -0.1, 0.52, 1.06 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH03", "", "Smoking causes me to get tired easily.", "", 1.78, new double[] { -0.15, 0.6, 1.44, 2.07 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH04", "", "Smoking makes me short of breath.", "", 1.94, new double[] { -1.01, 0.1, 0.84, 1.51 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH05", "", "Smoking irritates my mouth and throat.", "", 1.58, new double[] { -0.34, 0.85, 1.81, 2.57 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH06", "", "I worry that smoking will lower my quality of life.", "", 3.03, new double[] { -0.89, -0.15, 0.47, 0.96 }, -1, "",
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
