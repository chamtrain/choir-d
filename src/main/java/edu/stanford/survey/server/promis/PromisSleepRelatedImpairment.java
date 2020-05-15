/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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
 * Item bank for PROMIS sleep-related impairment assessment. Generated from OID E038718E-F556-4D0D-9B00-BD178DE6A2C8.
 */
public class PromisSleepRelatedImpairment {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("Sleep10", "In the past 7 days", "I had a hard time getting things done because I was sleepy.", "", 3.4501, new double[] { 0.0969, 0.9731, 1.6464, 2.3752 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep11", "In the past 7 days", "I had a hard time concentrating because I was sleepy.", "", 3.3963, new double[] { -0.0947, 0.8757, 1.5794, 2.2811 }, -1, "",
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
      item("Sleep120", "In the past 7 days", "When I woke up I felt ready to start the day.", "", 1.8706, new double[] { -1.505, -0.4786, 0.3928, 1.1881 }, -1, "",
          response("Not at all", 5),
          response("A little bit", 4),
          response("Somewhat", 3),
          response("Quite a bit", 2),
          response("Very much", 1)
      ),
      item("Sleep123", "In the past 7 days", "I had difficulty waking up.", "", 1.1797, new double[] { -0.1489, 1.042, 2.0213, 2.9929 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep124", "In the past 7 days", "I still felt sleepy when I woke up.", "", 1.72, new double[] { -1.2686, 0.1173, 0.805, 1.6577 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep18", "In the past 7 days", "I felt tired.", "", 2.67, new double[] { -1.5406, 0.1764, 0.9385, 1.8972 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep19", "In the past 7 days", "I tried to sleep whenever I could.", "", 1.4251, new double[] { -0.4401, 0.6862, 1.8761, 3.1847 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
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
      item("Sleep29", "In the past 7 days", "My daytime activities were disturbed by poor sleep.", "", 3.6572, new double[] { -0.0501, 0.738, 1.653, 2.4712 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("Sleep30", "In the past 7 days", "I felt irritable because of poor sleep.", "", 2.9195, new double[] { -0.034, 0.8901, 1.5595, 2.3266 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep33", "In the past 7 days", "I had a hard time controlling my emotions because of poor sleep.", "", 2.597, new double[] { 0.3609, 1.2565, 1.9928, 2.6766 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep4", "In the past 7 days", "I had enough energy.", "", 1.8303, new double[] { -1.6799, -0.1147, 1.1723, 2.1949 }, -1, "",
          response("Not at all", 5),
          response("A little bit", 4),
          response("Somewhat", 3),
          response("Quite a bit", 2),
          response("Very much", 1)
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
