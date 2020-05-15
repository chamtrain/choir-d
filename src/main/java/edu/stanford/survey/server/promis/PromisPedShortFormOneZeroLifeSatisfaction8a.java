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
 * Item bank for PROMIS assessment. Generated from OID 3B08BED7-386E-4DDB-A596-C4F894E8C2E1.
 */
public class PromisPedShortFormOneZeroLifeSatisfaction8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("SWB_LS_003R1", "Thinking about the past 4 weeks,", "My life was the best.", "", 3.708, new double[] { -1.965, -1.376, -0.639, 0.297 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_004R1", "Thinking about the past 4 weeks,", "My life was outstanding.", "", 3.833, new double[] { -1.824, -1.288, -0.604, 0.209 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_006R1", "Thinking about the past 4 weeks,", "My life was great.", "", 5.344, new double[] { -2.024, -1.447, -0.803, -0.037 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_019R1", "Thinking about the past 4 weeks,", "I had what I wanted in life.", "", 2.521, new double[] { -2.273, -1.491, -0.613, 0.451 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_046R1", "Thinking about the past 4 weeks,", "I was satisfied with my life.", "", 3.872, new double[] { -2.387, -1.798, -1.026, -0.108 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_048R1", "Thinking about the past 4 weeks,", "I was happy with my life.", "", 5.342, new double[] { -2.271, -1.653, -1.025, -0.214 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_051R1", "Thinking about the past 4 weeks,", "I had a good life.", "", 4.911, new double[] { -2.484, -1.795, -1.107, -0.236 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_055R1", "Thinking about the past 4 weeks,", "I enjoyed my life.", "", 4.986, new double[] { -2.325, -1.673, -1.072, -0.248 }, -1, "",
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
