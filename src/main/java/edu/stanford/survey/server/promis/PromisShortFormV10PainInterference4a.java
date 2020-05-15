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
 * Item bank for PROMIS assessment. Generated from OID C3305A82-E1AA-4E88-9F4D-E9C8099C296F.
 */
public class PromisShortFormV10PainInterference4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("PAININ22", "In the past 7 days", "how much did pain interfere with work around the home?", "", 5.3971, new double[] { 0.1709, 0.8366, 1.3266, 1.9589 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ31", "In the past 7 days", "How much did pain interfere with your ability to participate in social activities?", "", 5.90514, new double[] { 0.473, 1.0094, 1.5063, 2.077 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ34", "In the past 7 days", "How much did pain interfere with your household chores?", "", 4.92619, new double[] { 0.1771, 0.8447, 1.363, 1.9795 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ9", "In the past 7 days", "How much did pain interfere with your day to day activities?", "", 6.53406, new double[] { 0.1579, 0.8959, 1.4377, 2.0103 }, -1, "",
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
