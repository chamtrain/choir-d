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
 * Item bank for PROMIS assessment. Generated from OID E87BAC2A-D658-4E80-87F8-E891152DBC7C.
 */
public class PromisShortFormOneZeroPainBehavior7a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("PAINBE2", "In the past 7 days", "When I was in pain I became irritable", "", 5.50922, new double[] { -0.3571, 0.288, 0.6984, 1.1961, 1.6378 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE24", "In the past 7 days", "When I was in pain I moved stiffly", "", 4.53196, new double[] { -0.4029, 0.194, 0.5348, 1.0434, 1.4722 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE25", "In the past 7 days", "When I was in pain I called out for someone to help me", "", 5.06239, new double[] { -0.3056, 1.0062, 1.3672, 1.7784, 2.1113 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE3", "In the past 7 days", "When I was in pain I grimaced", "", 5.03393, new double[] { -0.3654, 0.2884, 0.6979, 1.2893, 1.824 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE37", "In the past 7 days", "When I was in pain I isolated myself from others", "", 5.38168, new double[] { -0.4043, 0.6155, 0.8702, 1.2189, 1.7178 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE45", "In the past 7 days", "When I was in pain I thrashed", "", 4.98842, new double[] { -0.3394, 1.1122, 1.385, 1.7463, 2.184 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE8", "In the past 7 days", "When I was in pain I moved extremely slowly", "", 5.71973, new double[] { -0.2939, 0.2927, 0.658, 1.1056, 1.5116 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
