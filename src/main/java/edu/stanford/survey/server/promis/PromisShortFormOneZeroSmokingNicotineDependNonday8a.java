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
 * Item bank for PROMIS assessment. Generated from OID DE280BF4-23E9-47D6-9B82-E37FDB1A21D3.
 */
public class PromisShortFormOneZeroSmokingNicotineDependNonday8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
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
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
