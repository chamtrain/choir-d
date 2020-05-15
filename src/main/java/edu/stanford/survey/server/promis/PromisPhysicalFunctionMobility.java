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
 * Item bank for PROMIS physical function mobility assessment.
 */
public class PromisPhysicalFunctionMobility {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("PFC37", "", "Does your health now limit you in climbing one flight of stairs?", "", 4.45984, new double[] { -2.3092, -1.6285, -1.0464, -0.5728 }, -1, "",
          response("Not at all", 5),
          response("Very little", 4),
          response("Somewhat", 3),
          response("Quite a lot", 2),
          response("Cannot do", 1)
      ),
      item("PFC38", "", "Are you able to walk at a normal speed?", "", 4.33821, new double[] { -2.0429, -1.6877, -1.224, -0.711 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA15", "", "Are you able to stand up from an armless straight chair?", "", 3.19284, new double[] { -2.8025, -2.1119, -1.5397, -0.7702 }, -1, "",
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
      item("PFA31", "", "Are you able to get up off the floor from lying on your back without help?", "", 3.26379, new double[] { -2.1316, -1.5496, -1.0464, -0.3065 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFB9", "", "Are you able to jump up and down?", "", 3.58814, new double[] { -1.6482, -1.2832, -0.8787, -0.3657 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFB10", "", "Are you able to climb up five steps?", "", 3.73005, new double[] { -2.5953, -2.0725, -1.5792, -1.0859 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFB24", "", "Are you able to run a short distance, such as to catch a bus?", "", 3.84154, new double[] { -1.4114, -1.0859, -0.7406, -0.1289 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFB32", "", "Are you able to stand unsupported for 10 minutes?", "", 3.4057, new double[] { -2.4671, -2.1218, -1.6581, -1.1352 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA10", "", "Are you able to stand for one hour?", "", 3.37529, new double[] { -1.7469, -1.3522, -0.9182, -0.3361 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFB40", "", "Are you able to stand up on tiptoes?", "", 3.02053, new double[] { -2.2796, -1.9146, -1.5496, -0.9773 }, -1, "", // same
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFB42", "", "Are you able to stand unsupported for 30 minutes?", "", 3.09148, new double[] { -2.0133, -1.6581, -1.2635, -0.7208 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFB49", "", "Does your health now limit you in going for a short walk (less than 15 minutes)?", "", 3.57801, new double[] { -2.1908, -1.6778, -1.1845, -0.7406 }, -1, "",
          response("Not at all", 5),
          response("Very little", 4),
          response("Somewhat", 3),
          response("Quite a lot", 2),
          response("Cannot do", 1)
      ),
      item("PFC10", "", "Does your health now limit you in climbing several flights of stairs?", "", 4.1963, new double[] { -1.8061, -1.1451, -0.563, -0.1092 }, -1, "",
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
