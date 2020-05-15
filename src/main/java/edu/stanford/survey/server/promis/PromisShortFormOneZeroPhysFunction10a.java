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
 * Item bank for PROMIS assessment. Generated from OID F95F01E8-027A-4524-A1D7-0AAEF489B272.
 */
public class PromisShortFormOneZeroPhysFunction10a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 10, 10, 3.0,
      item("PFA1", "", "Does your health now limit you in doing vigorous activities, such as running, lifting heavy objects, participating in strenuous sports?", "", 3.31447, new double[] { -1.1155, -0.5038, 0.1276, 0.6012 }, -1, "",
          response("Not at all", 5),
          response("Very little", 4),
          response("Somewhat", 3),
          response("Quite a lot", 2),
          response("Cannot do", 1)
      ),
      item("PFA11", "", "Are you able to do chores such as vacuuming or yard work?", "", 4.83487, new double[] { -1.9837, -1.5298, -1.1056, -0.4545 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA16", "", "Are you able to dress yourself, including tying shoelaces and doing buttons?", "", 3.36515, new double[] { -3.138, -2.5559, -1.9146, -1.2437 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA3", "", "Does your health now limit you in bending, kneeling, or stooping?", "", 2.94958, new double[] { -2.25, -1.2635, -0.563, 0.029 }, -1, "",
          response("Not at all", 5),
          response("Very little", 4),
          response("Somewhat", 3),
          response("Quite a lot", 2),
          response("Cannot do", 1)
      ),
      item("PFA5", "", "Does your health now limit you in lifting or carrying groceries?", "", 4.13549, new double[] { -2.3684, -1.6186, -1.0365, -0.5334 }, -1, "",
          response("Not at all", 5),
          response("Very little", 4),
          response("Somewhat", 3),
          response("Quite a lot", 2),
          response("Cannot do", 1)
      ),
      item("PFA55", "", "Are you able to wash and dry your body?", "", 3.57801, new double[] { -3.3451, -2.6545, -2.0725, -1.4805 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFB26", "", "Are you able to shampoo your hair?", "", 3.32461, new double[] { -3.1676, -2.8815, -2.3388, -1.7567 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFC36", "", "Does your health now limit you in walking more than a mile?", "", 4.45984, new double[] { -1.4312, -1.0069, -0.5926, -0.2275 }, -1, "",
          response("Not at all", 5),
          response("Very little", 4),
          response("Somewhat", 3),
          response("Quite a lot", 2),
          response("Cannot do", 1)
      ),
      item("PFC37", "", "Does your health now limit you in climbing one flight of stairs?", "", 4.45984, new double[] { -2.3092, -1.6285, -1.0464, -0.5728 }, -1, "",
          response("Not at all", 5),
          response("Very little", 4),
          response("Somewhat", 3),
          response("Quite a lot", 2),
          response("Cannot do", 1)
      ),
      item("PFC45", "", "Are you able to get on and off the toilet?", "", 3.11175, new double[] { -3.1084, -2.7828, -2.2106, -1.4608 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
