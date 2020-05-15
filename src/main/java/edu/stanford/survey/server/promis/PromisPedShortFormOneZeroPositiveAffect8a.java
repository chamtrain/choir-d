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
 * Item bank for PROMIS assessment. Generated from OID 488922EF-E56D-4C29-8D12-6FD6965BC613.
 */
public class PromisPedShortFormOneZeroPositiveAffect8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("SWB_P_001R1", "In the past 7 days,", "I felt calm.", "", 2.403, new double[] { -2.561, -1.574, -0.481, 0.706 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_004R1", "In the past 7 days,", "I felt peaceful.", "", 3.027, new double[] { -2.236, -1.519, -0.521, 0.5 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_025R1", "In the past 7 days,", "I felt great.", "", 4.059, new double[] { -2.319, -1.572, -0.656, 0.361 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_026R1", "In the past 7 days,", "I felt cheerful.", "", 3.839, new double[] { -2.215, -1.49, -0.624, 0.446 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_027R1", "In the past 7 days,", "I felt happy.", "", 3.804, new double[] { -2.361, -1.776, -0.907, 0.287 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_029R1", "In the past 7 days,", "I felt joyful.", "", 4.435, new double[] { -2.235, -1.457, -0.549, 0.45 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_037R1", "In the past 7 days,", "I was in a good mood.", "", 2.813, new double[] { -2.724, -1.989, -0.854, 0.552 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_049R1", "In the past 7 days,", "I felt refreshed.", "", 2.667, new double[] { -2.199, -1.439, -0.315, 0.748 }, -1, "",
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
