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
 * Item bank for PROMIS assessment. Generated from OID 6DDF7120-5D8D-47DF-8DC1-380E27676478.
 */
public class PromisBankOneZeroSmokingCopingExpectNonDaily {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("SMKCOP01_nd", "", "I rely on smoking to deal with stress.", "", 2.36, new double[] { -1.59, -0.75, 0.04, 0.81 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKCOP02_nd", "", "When I'm angry, a cigarette can calm me down.", "", 3.32, new double[] { -1.82, -1.06, 0.01, 0.9 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKCOP03_nd", "", "Smoking allows me to take a break from my problems for a few minutes.", "", 2.06, new double[] { -1.54, -0.78, 0.1, 0.88 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKCOP04_nd", "", "I am tempted to smoke when I feel depressed.", "", 2.51, new double[] { -1.93, -1.13, -0.14, 0.66 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKCOP05_nd", "", "When I go too long without a cigarette I lose my temper more easily.", "", 1.45, new double[] { -0.95, 0.05, 1.18, 2.1 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKCOP06_nd", "", "When I'm upset with someone, a cigarette helps me cope.", "", 3.69, new double[] { -1.44, -0.67, 0.01, 0.68 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKCOP07_nd", "", "Smoking helps me when I'm upset about something.", "", 4.16, new double[] { -1.79, -1.12, -0.12, 0.72 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKCOP08_nd", "", "Smoking helps me reduce tension.", "", 3.41, new double[] { -1.83, -0.82, -0.06, 0.72 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKCOP09_nd", "", "When I am worrying about something, a cigarette is helpful.", "", 3.63, new double[] { -1.88, -1.15, -0.11, 0.79 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKCOP10_nd", "", "Smoking helps me deal with anxiety.", "", 3.42, new double[] { -1.48, -0.71, -0.04, 0.67 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKCOP11_nd", "", "Smoking calms me down.", "", 3.33, new double[] { -1.84, -0.82, -0.02, 0.77 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKCOP12_nd", "", "I am tempted to smoke when I am anxious.", "", 3.02, new double[] { -2.11, -1.4, -0.4, 0.48 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKCOP16_nd", "", "Smoking keeps me from overeating.", "", 1.17, new double[] { -0.72, 0.16, 1.1, 1.81 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKCOP17_nd", "", "I am tempted to smoke when I have arguments or conflicts with others.", "", 3.3, new double[] { -1.83, -1.11, -0.16, 0.58 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKCOP18_nd", "", "I am tempted to smoke when I am stressed.", "", 4.55, new double[] { -2.2, -1.55, -0.58, 0.28 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKCOP19_nd", "", "I am tempted to smoke when I experience an emotional crisis.", "", 3.65, new double[] { -2.09, -1.34, -0.5, 0.36 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKCOP20_nd", "", "I am tempted to smoke when I feel nervous.", "", 3.29, new double[] { -1.95, -1.16, -0.23, 0.61 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKCOP21_nd", "", "When I feel stressed I want a cigarette.", "", 3.74, new double[] { -2.16, -1.42, -0.54, 0.36 }, -1, "",
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
