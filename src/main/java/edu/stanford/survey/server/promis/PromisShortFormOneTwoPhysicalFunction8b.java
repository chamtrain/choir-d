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
 * Item bank for PROMIS assessment. Generated from OID 26BF1878-3FAC-4BA4-8F43-204C11E28BEF.
 */
public class PromisShortFormOneTwoPhysicalFunction8b {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("PFA11", "", "Are you able to do chores such as vacuuming or yard work?", "", 4.83487, new double[] { -1.9837, -1.5298, -1.1056, -0.4545 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA21", "", "Are you able to go up and down stairs at a normal pace?", "", 4.23685, new double[] { -1.885, -1.4805, -1.0464, -0.3953 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA23", "", "Are you able to go for a walk of at least 15 minutes?", "", 4.28753, new double[] { -1.9146, -1.5792, -1.1944, -0.6814 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA4", "", "Does your health now limit you in doing heavy work around the house like scrubbing floors, or lifting or moving heavy furniture?", "", 4.39902, new double[] { -1.5594, -0.9773, -0.415, 0.0586 }, -1, "",
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
      item("PFA53", "", "Are you able to run errands and shop?", "", 4.43957, new double[] { -2.546, -1.9738, -1.4509, -0.8195 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFB1", "", "Does your health now limit you in doing moderate work around the house like vacuuming, sweeping floors or carrying in groceries?", "", 4.38889, new double[] { -2.4474, -1.6384, -1.076, -0.632 }, -1, "",
          response("Not at all", 5),
          response("Very little", 4),
          response("Somewhat", 3),
          response("Quite a lot", 2),
          response("Cannot do", 1)
      ),
      item("PFC12", "", "Does your health now limit you in doing two hours of physical labor?", "", 4.68283, new double[] { -1.4213, -0.9182, -0.3854, 0.0586 }, -1, "",
          response("Not at all", 5),
          response("Very little", 4),
          response("Somewhat", 3),
          response("Quite a lot", 2),
          response("Cannot do", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
