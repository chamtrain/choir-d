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
 * Item bank for PROMIS assessment. Generated from OID E60A53EE-0690-47F0-B5D0-547B4394ED5B.
 */
public class PromisPedShortFormOneZeroPainBehavior8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("PAINBE46_Ped", "In the past 7 days, when I was in pain", "I protected the part of my body that hurt.", "", 3.55, new double[] { -1.46, -0.32, -0.03, 0.47, 0.83 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PAINBE4_Ped", "In the past 7 days, when I was in pain", "I asked for medicine.", "", 2.65, new double[] { -1.75, -0.27, 0.01, 0.65, 1.13 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PAINBE5_Ped", "In the past 7 days, when I was in pain", "I talked about my pain.", "", 2.22, new double[] { -1.93, -0.47, 0.06, 0.8, 1.29 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PAINBE8_Ped", "In the past 7 days, when I was in pain", "I moved slower.", "", 4.39, new double[] { -1.41, -0.46, -0.17, 0.37, 0.74 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PBNEW24_Ped", "In the past 7 days, when I was in pain", "I asked for someone to help me.", "", 3.65, new double[] { -1.41, -0.14, 0.23, 0.73, 1.04 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PBNEW2_Ped", "In the past 7 days, when I was in pain", "it showed on my face.", "", 3.44, new double[] { -1.44, -0.29, 0.0, 0.52, 0.89 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PBNEW32_Ped", "In the past 7 days, when I was in pain", "I lay down.", "", 3.26, new double[] { -1.41, -0.6, -0.25, 0.33, 0.89 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      ),
      item("PBNEW_Ped15", "In the past 7 days, when I was in pain", "I had to stop what I was doing.", "", 4.58, new double[] { -1.26, -0.32, 0.03, 0.62, 0.99 }, -1, "",
          response("Had No Pain", 1),
          response("Never", 2),
          response("Almost Never", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Almost Always", 6)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
