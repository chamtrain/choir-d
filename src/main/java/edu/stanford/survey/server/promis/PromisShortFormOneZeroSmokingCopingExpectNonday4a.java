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
 * Item bank for PROMIS assessment. Generated from OID F45BA0A4-F3AF-409E-B776-D2FA2CC02BC8.
 */
public class PromisShortFormOneZeroSmokingCopingExpectNonday4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
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
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
