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
 * Item bank for PROMIS assessment. Generated from OID E8BD4AD3-06E2-4512-83E5-952DC349DFE2.
 */
public class PromisBankOneZeroSmokingNicotineDependNonDaily {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("SMKNDEP01_nd", "", "When I haven't been able to smoke for a few hours, the craving gets intolerable.", "", 3.13, new double[] { -1.25, -0.31, 0.64, 1.38 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKNDEP02_nd", "", "I find myself reaching for cigarettes without thinking about it.", "", 1.41, new double[] { -1.54, -0.48, 0.8, 2.16 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKNDEP03_nd", "", "I drop everything to go out and buy cigarettes.", "", 1.84, new double[] { -1.23, -0.03, 1.1, 2.03 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKNDEP04_nd", "", "I smoke more before going into a situation where smoking is not allowed.", "", 1.57, new double[] { -1.96, -0.99, 0.17, 1.19 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKNDEP05_nd", "", "I crave cigarettes at certain times of day.", "", 1.62, new double[] { -2.13, -1.11, -0.04, 0.94 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP06_nd", "", "When I'm really craving a cigarette, it feels like I'm in the grip of some unknown force that I cannot control.", "", 2.27, new double[] { -1.2, -0.36, 0.59, 1.44 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKNDEP07_nd", "", "My urges to smoke keep getting stronger if I don't smoke.", "", 2.56, new double[] { -1.46, -0.57, 0.2, 0.97 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP08_nd", "", "After not smoking for a while, I need to smoke in order to avoid feeling any discomfort.", "", 2.33, new double[] { -1.35, -0.53, 0.48, 1.34 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKNDEP09_nd", "", "My desire to smoke seems overpowering.", "", 3.02, new double[] { -1.19, -0.46, 0.28, 0.98 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP10_nd", "", "Cravings for a cigarette make it difficult for me to quit.", "", 3.17, new double[] { -1.76, -0.95, -0.31, 0.4 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP11_nd", "", "It is hard to ignore urges to smoke.", "", 2.99, new double[] { -1.86, -0.96, -0.19, 0.62 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP12_nd", "", "When I go without a cigarette for a few hours, I experience craving.", "", 3.66, new double[] { -1.35, -0.53, 0.09, 0.73 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP13_nd", "", "I frequently crave cigarettes.", "", 3.18, new double[] { -1.75, -0.69, 0.08, 0.9 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP14_nd", "", "The idea of not having any cigarettes causes me stress.", "", 2.49, new double[] { -1.4, -0.44, 0.22, 0.9 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP15_nd", "", "When I run out of cigarettes, I find it almost unbearable.", "", 2.56, new double[] { -1.56, -0.67, 0.21, 0.93 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKNDEP16_nd", "", "I get a real gnawing hunger for a cigarette when l haven't smoked in a while.", "", 2.8, new double[] { -2.03, -1.04, 0.04, 0.9 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKNDEP17_nd", "", "I smoke even when I am so ill that I am in bed most of the day.", "", 1.46, new double[] { -1.21, 0.05, 1.14, 2.23 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKNDEP18_nd", "", "When I go too long without a cigarette I feel impatient.", "", 2.82, new double[] { -1.64, -0.81, 0.22, 1.09 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKNDEP19_nd", "", "When I go too long without a cigarette I get strong urges that are hard to get rid of.", "", 2.81, new double[] { -1.65, -0.77, 0.23, 1.02 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKNDEP20_nd", "", "It is hard for me to go without smoking for a whole day.", "", 2.6, new double[] { -1.23, -0.55, 0.04, 0.64 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP21_nd", "", "Smoking is a large part of my daily life.", "", 1.67, new double[] { -1.34, -0.39, 0.34, 1.22 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP22_nd", "", "I am tempted to smoke when I realize I haven't smoked for a while.", "", 1.64, new double[] { -2.33, -1.26, 0.09, 1.23 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKNDEP28_nd", "", "I become more addicted the more I smoke.", "", 1.53, new double[] { -1.35, -0.5, 0.5, 1.38 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP29_nd", "", "I feel like I smoke all the time.", "", 2.09, new double[] { -0.78, -0.18, 0.53, 1.11 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP30_nd", "", "I would go crazy if I couldn't smoke.", "", 2.4, new double[] { -1.13, -0.13, 0.59, 1.28 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP31_nd", "", "If I quit smoking I will experience intense cravings for a cigarette.", "", 1.8, new double[] { -2.3, -1.25, -0.38, 0.44 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKNDEP32_nd", "", "My life is full of reminders to smoke.", "", 1.61, new double[] { -1.72, -0.59, 0.41, 1.31 }, -1, "",
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
