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
 * Item bank for PROMIS assessment. Generated from OID 1B5A7A47-9D20-496D-A644-F6B6C91BE425.
 */
public class PromisShortFormV10SocialSatRole6a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
      item("SRPSAT07", "In the past 7 days", "I am satisfied with how much work I can do (include work at home)", "", 4.424, new double[] { -1.338, -0.799, -0.131, 0.675 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT24", "In the past 7 days", "I am satisfied with my ability to work (include work at home)", "", 4.688, new double[] { -1.433, -0.936, -0.288, 0.519 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT39", "In the past 7 days", "I am satisfied with my ability to do household chores/tasks", "", 4.08, new double[] { -1.429, -0.831, -0.145, 0.617 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT47", "In the past 7 days", "I am satisfied with my ability to do regular personal and household responsibilities", "", 4.377, new double[] { -1.421, -0.895, -0.215, 0.57 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT49", "In the past 7 days", "I am satisfied with my ability to perform my daily routines", "", 5.577, new double[] { -1.564, -0.92, -0.218, 0.458 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT50", "In the past 7 days", "I am satisfied with my ability to meet the needs of those who depend on me", "", 3.679, new double[] { -1.568, -1.009, -0.361, 0.552 }, -1, "",
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
