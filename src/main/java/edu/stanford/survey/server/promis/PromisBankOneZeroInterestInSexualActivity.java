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
 * Item bank for PROMIS assessment. Generated from OID 208CC8BD-4376-47F2-8EAE-60BFFB7692F0.
 */
public class PromisBankOneZeroInterestInSexualActivity {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("SFINT101", "In the past 30 days", "How interested have you been in sexual activity?", "", 5.2, new double[] { -0.92, -0.17, 0.48, 1.26 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very", 5)
      ),
      item("SFINT102", "In the past 30 days", "How often have you felt like you wanted to have sex?", "", 5.91, new double[] { -1.2, -0.44, 0.59, 1.66 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SFINT103", "In the past 30 days", "How often have you had sexual thoughts or fantasies while you were awake?", "", 2.21, new double[] { -1.2, -0.06, 1.14, 2.75 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SFINT104", "In the past 30 days", "How often were you interested enough to start a sexual activity?", "", 2.88, new double[] { -0.64, 0.07, 1.16, 2.38 }, -1, "",
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
