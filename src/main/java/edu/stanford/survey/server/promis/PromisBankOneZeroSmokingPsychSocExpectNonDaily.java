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
 * Item bank for PROMIS assessment. Generated from OID 2DF70ECD-8F70-430E-A42D-95845B6756E8.
 */
public class PromisBankOneZeroSmokingPsychSocExpectNonDaily {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
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
      ),
      item("SMKPSY07_nd", "", "If I quit smoking I will be more attractive to others.", "", 1.9, new double[] { -0.83, -0.18, 0.84, 1.47 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKPSY08_nd", "", "I get upset when I think about my smoking.", "", 2.39, new double[] { -0.23, 0.59, 1.33, 1.99 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKPSY09_nd", "", "My smoking makes me respect myself less.", "", 3.24, new double[] { -0.03, 0.58, 1.16, 1.73 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKPSY10_nd", "", "I feel embarrassed when I smoke.", "", 2.65, new double[] { -0.38, 0.4, 1.49, 2.39 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKPSY11_nd", "", "I look ridiculous while smoking.", "", 2.29, new double[] { 0.05, 0.72, 1.48, 2.05 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKPSY12_nd", "", "People I care about respect me less because I smoke.", "", 1.76, new double[] { 0.06, 0.95, 1.84, 2.5 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKPSY13_nd", "", "People think I'm foolish for ignoring the warnings about cigarette smoking.", "", 1.62, new double[] { -1.67, -0.61, 0.42, 1.3 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKPSY14_nd", "", "My smoking makes me less attractive to other people.", "", 2.19, new double[] { -0.9, -0.05, 0.99, 1.71 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKPSY21_nd", "", "People close to me disapprove of my smoking.", "", 1.79, new double[] { -1.47, -0.42, 0.38, 1.02 }, -1, "",
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
