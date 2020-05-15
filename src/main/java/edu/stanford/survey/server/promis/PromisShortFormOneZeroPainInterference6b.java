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
 * Item bank for PROMIS assessment. Generated from OID 0AB0CD9F-94AA-492D-A360-A2124BAB0B7F.
 */
public class PromisShortFormOneZeroPainInterference6b {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
      item("PAININ10", "In the past 7 days", "How much did pain interfere with your enjoyment of recreational activities?", "", 5.15317, new double[] { 0.131, 0.7914, 1.26, 1.8452 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ14", "In the past 7 days", "How much did pain interfere with doing your tasks away from home (e.g., getting groceries, running errands)?", "", 4.83004, new double[] { 0.4265, 0.9994, 1.4568, 2.044 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ26", "In the past 7 days", "How often did pain keep you from socializing with others?", "", 4.87755, new double[] { 0.578, 1.085, 1.676, 2.5191 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("PAININ3", "In the past 7 days", "How much did pain interfere with your enjoyment of life?", "", 4.98134, new double[] { 0.1293, 0.8809, 1.3825, 1.9123 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ8", "In the past 7 days", "How much did pain interfere with your ability to concentrate?", "", 3.74877, new double[] { 0.3996, 1.1057, 1.6892, 2.3425 }, -1, "",
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
