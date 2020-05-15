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
 * Item bank for PROMIS assessment. Generated from OID 309591DB-4E0E-4A52-8B89-FA965EF4F70C.
 */
public class PromisShortFormOneZeroSleepRelatedImpairment8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("Sleep10", "In the past 7 days", "I had a hard time getting things done because I was sleepy.", "", 3.4501, new double[] { 0.0969, 0.9731, 1.6464, 2.3752 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep119", "In the past 7 days", "I felt alert when I woke up.", "", 1.6659, new double[] { -1.5841, -0.3866, 0.5226, 1.3856 }, -1, "",
          response("Not at all", 5),
          response("A little bit", 4),
          response("Somewhat", 3),
          response("Quite a bit", 2),
          response("Very much", 1)
      ),
      item("Sleep18", "In the past 7 days", "I felt tired.", "", 2.67, new double[] { -1.5406, 0.1764, 0.9385, 1.8972 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep25", "In the past 7 days", "I had problems during the day because of poor sleep.", "", 3.7589, new double[] { -0.09, 0.8449, 1.5251, 2.2529 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep27", "In the past 7 days", "I had a hard time concentrating because of poor sleep.", "", 4.8182, new double[] { 0.0973, 1.0174, 1.6069, 2.2157 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep30", "In the past 7 days", "I felt irritable because of poor sleep.", "", 2.9195, new double[] { -0.034, 0.8901, 1.5595, 2.3266 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep6", "In the past 7 days", "I was sleepy during the daytime.", "", 2.2395, new double[] { -1.2933, 0.2651, 1.0718, 2.114 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep7", "In the past 7 days", "I had trouble staying awake during the day.", "", 2.1972, new double[] { -0.1386, 0.932, 1.7274, 2.5495 }, -1, "",
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
