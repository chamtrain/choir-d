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
 * Item bank for PROMIS assessment. Generated from OID 85619989-D4A7-4EC5-BF08-A4412DCBDD75.
 */
public class PromisShortFormOneZeroFatigue7a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("FATEXP18", "In the past 7 days", "How often did you run out of energy?", "", 3.38925, new double[] { -1.0135, 0.0387, 1.0796, 2.1624 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("FATEXP20", "In the past 7 days", "How often did you feel tired?", "", 3.25088, new double[] { -1.6209, -0.4778, 0.7291, 1.7908 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("FATEXP5", "In the past 7 days", "How often did you experience extreme exhaustion?", "", 2.6615, new double[] { -0.1122, 0.839, 1.7173, 2.8349 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("FATIMP21", "In the past 7 days", "How often were you too tired to take a bath or shower?", "", 2.11414, new double[] { 0.3825, 1.1347, 2.0812, 3.3136 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("FATIMP30", "In the past 7 days", "How often were you too tired to think clearly?", "", 2.96527, new double[] { -0.1065, 0.8185, 1.8163, 3.0505 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("FATIMP33", "In the past 7 days", "How often did your fatigue limit you at work (include work at home)?", "", 3.09254, new double[] { -0.5432, 0.3259, 1.3544, 2.3236 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("FATIMP40", "In the past 7 days", "How often did you have enough energy to exercise strenuously?", "", 1.17441, new double[] { -2.4817, -0.8409, 0.3359, 1.4922 }, -1, "",
          response("Never", 5),
          response("Rarely", 4),
          response("Sometimes", 3),
          response("Often", 2),
          response("Always", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
