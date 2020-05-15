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
 * Item bank for PROMIS assessment. Generated from OID 39909545-DA53-46C7-9019-1E6834821A7A.
 */
public class PromisShortFormOneZeroSmokingPsychSocExpectNonday6a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
      item("SMKPSY01_nd", "", "If I quit smoking I will be more in control of my life.", "", 1.77, new double[] { -1.02, -0.39, 0.54, 1.16 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKPSY02_nd", "", "If I quit smoking my friends will respect me more.", "", 1.89, new double[] { -0.59, 0.07, 1.01, 1.67 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKPSY03_nd", "", "My need for cigarettes makes me feel disappointed in myself.", "", 2.94, new double[] { -0.43, 0.22, 0.8, 1.33 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKPSY04_nd", "", "My smoking makes me feel less attractive.", "", 3.03, new double[] { -0.26, 0.4, 0.97, 1.5 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKPSY05_nd", "", "People think less of me if they see me smoking.", "", 1.86, new double[] { -0.64, 0.28, 1.31, 2.04 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKPSY06_nd", "", "My cigarette smoking bothers others.", "", 1.65, new double[] { -1.68, -0.32, 1.01, 1.88 }, -1, "",
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
