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
 * Item bank for PROMIS assessment. Generated from OID 71C2D9CB-78A8-4617-AE93-A44BA941012F.
 */
public class PromisBankOneZeroSmokingHealthExpectAllSmk {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
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
      ),
      item("SMKHLTH07", "", "Smoking makes me worry about getting emphysema.", "", 2.91, new double[] { -1.01, -0.19, 0.43, 0.94 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH08", "", "Smoking makes my lungs hurt.", "", 1.79, new double[] { 0.1, 0.99, 1.91, 2.53 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH09", "", "Smoking causes damage to my gums and teeth.", "", 1.73, new double[] { -1.5, -0.41, 0.5, 1.18 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH11", "", "It takes me longer to recover from a cold because I smoke.", "", 1.66, new double[] { -0.41, 0.36, 1.13, 1.82 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH12", "", "If I quit smoking I will feel more energetic.", "", 1.95, new double[] { -1.3, -0.57, 0.44, 1.15 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH13", "", "If I quit smoking I will breathe easier.", "", 2.48, new double[] { -1.9, -1.03, -0.27, 0.35 }, -1, "",
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
