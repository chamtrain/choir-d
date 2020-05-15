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
 * Item bank for PROMIS assessment. Generated from OID E28D33B7-B772-4B2D-A1CE-D640D030D5E5.
 */
public class PromisShortFormOneZeroSleepDisturb8b {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("Sleep108", "In the past 7 days", "My sleep was restless.", "", 2.296, new double[] { -0.2885, 0.687, 1.4524, 2.326 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep109", "In the past 7 days", "My sleep quality was...", "", 3.385, new double[] { -1.2193, -0.0027, 1.0761, 1.902 }, -1, "",
          response("Very poor", 5),
          response("Poor", 4),
          response("Fair", 3),
          response("Good", 2),
          response("Very good", 1)
      ),
      item("Sleep110", "In the past 7 days", "I got enough sleep.", "", 2.1699, new double[] { -1.5596, -0.1623, 0.7654, 1.8068 }, -1, "",
          response("Never", 5),
          response("Rarely", 4),
          response("Sometimes", 3),
          response("Often", 2),
          response("Always", 1)
      ),
      item("Sleep115", "In the past 7 days", "I was satisfied with my sleep.", "", 2.7714, new double[] { -1.248, -0.3351, 0.4303, 1.095 }, -1, "",
          response("Not at all", 5),
          response("A little bit", 4),
          response("Somewhat", 3),
          response("Quite a bit", 2),
          response("Very much", 1)
      ),
      item("Sleep116", "In the past 7 days", "My sleep was refreshing.", "", 2.5768, new double[] { -1.3528, -0.34, 0.4935, 1.2847 }, -1, "",
          response("Not at all", 5),
          response("A little bit", 4),
          response("Somewhat", 3),
          response("Quite a bit", 2),
          response("Very much", 1)
      ),
      item("Sleep44", "In the past 7 days", "I had difficulty falling asleep.", "", 2.5097, new double[] { -0.4634, 0.3113, 0.978, 1.7204 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep87", "In the past 7 days", "I had trouble staying asleep.", "", 2.1858, new double[] { -0.8961, 0.0972, 0.9951, 1.7785 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("Sleep90", "In the past 7 days", "I had trouble sleeping.", "", 3.6552, new double[] { -0.6142, 0.1635, 0.9581, 1.6228 }, -1, "",
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
