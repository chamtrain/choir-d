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
 * Item bank for PROMIS assessment. Generated from OID 383F9DBC-EE0F-4168-927C-0CACDA438C6F.
 */
public class PromisShortFormV10Fatigue8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("An3", "During the past 7 days:", "I have trouble <u>starting</u> things because I am tired", "", 4.34709, new double[] { -0.3111, 0.5041, 1.1796, 1.9585 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("FATEXP35", "In the past 7 days", "How much were you bothered by your fatigue on average?", "", 4.22759, new double[] { -0.5079, 0.3959, 1.0696, 1.8718 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("FATEXP40", "In the past 7 days", "How fatigued were you on average?", "", 4.17556, new double[] { -0.8828, 0.3275, 1.2048, 2.1884 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("FATEXP41", "In the past 7 days", "How run-down did you feel on average?", "", 4.32191, new double[] { -0.5302, 0.4511, 1.1503, 2.0368 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("FATIMP16", "In the past 7 days", "How often did you have trouble finishing things because of your fatigue?", "", 3.86264, new double[] { -0.3853, 0.456, 1.3312, 2.2382 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("FATIMP3", "In the past 7 days", "How often did you have to push yourself to get things done because of your fatigue?", "", 4.77251, new double[] { -0.6329, 0.1258, 1.0389, 1.9415 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("FATIMP49", "In the past 7 days", "To what degree did your fatigue interfere with your physical functioning?", "", 4.02223, new double[] { -0.1864, 0.667, 1.3071, 2.0529 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("HI7", "During the past 7 days:", "I feel fatigued", "", 4.32034, new double[] { -1.1444, 0.0806, 0.9561, 1.772 }, -1, "",
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
