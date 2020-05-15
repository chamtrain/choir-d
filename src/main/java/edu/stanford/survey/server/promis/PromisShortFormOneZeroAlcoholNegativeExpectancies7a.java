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
 * Item bank for PROMIS assessment. Generated from OID E09E32C0-4D6C-4183-8A98-D65E6FE23CD8.
 */
public class PromisShortFormOneZeroAlcoholNegativeExpectancies7a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("NEXP01", "", "People have trouble thinking when they drink.", "", 2.47101, new double[] { -2.1357, -0.9843, 0.2886, 1.2252 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("NEXP03", "", "People do things they regret while drinking.", "", 3.00069, new double[] { -2.3198, -1.055, 0.0452, 0.9321 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("NEXP04", "", "People make bad decisions when they drink.", "", 3.19195, new double[] { -2.2336, -1.1311, 0.0049, 0.8821 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("NEXP06", "", "People are careless when they drink.", "", 3.32191, new double[] { -2.1194, -0.972, 0.1717, 1.0661 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("NEXP07", "", "People are irresponsible when they drink.", "", 3.14977, new double[] { -2.061, -0.8478, 0.1961, 1.0433 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("NEXP08", "", "People are pushy when they drink.", "", 2.42302, new double[] { -1.8855, -0.6602, 0.6901, 1.6539 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("NEXP09", "", "People are rude when they drink.", "", 2.63998, new double[] { -1.9757, -0.6841, 0.6679, 1.5751 }, -1, "",
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
