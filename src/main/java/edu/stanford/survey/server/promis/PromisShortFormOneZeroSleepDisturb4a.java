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
 * Item bank for PROMIS assessment.
 */
public class PromisShortFormOneZeroSleepDisturb4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("Sleep116", "In the past 7 days", "My sleep was refreshing.", "", 2.5768, new double[] { -1.3528, -0.34, 0.4935, 1.2847 }, -1, "",
          response("Not at all", 5),
          response("A little bit", 4),
          response("Somewhat", 3),
          response("Quite a bit", 2),
          response("Very much", 1)
      ),
      item("Sleep20", "In the past 7 days", "I had a problem with my sleep.", "", 2.7964, new double[] { -0.5612, 0.3267, 0.9827, 1.7406 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep44", "In the past 7 days", "I had difficulty falling asleep.", "", 2.5097, new double[] { -0.4634, 0.3113, 0.978, 1.7204 }, -1, "",
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
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
