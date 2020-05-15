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
 * Item bank for PROMIS Bank v1.2 - Upper Extremity. Created by pulling the 16 Upper Extremity questions out
 * of the PROMIS physical function assessment that was generated from OID 98DB589F-04D5-4529-9E4D-0D77F022C2CC.
 * Note: questions PFA16 and PFA29 were modified to match the PFA16r1 and PFA29r1 questions on the upper extremity assessment PDF
 * downloaded from assessment center.
 */
public class PromisPhysicalFunctionUpperExtremity {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("PFA16r1", "", "Are you able to dress yourself, including tying shoelaces and buttoning your clothes?", "", 3.36515, new double[] { -3.138, -2.5559, -1.9146, -1.2437 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA17", "", "Are you able to reach into a high cupboard?", "", 2.17924, new double[] { -2.7236, -2.3092, -1.7469, -0.928 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA18", "", "Are you able to use a hammer to pound a nail?", "", 2.48332, new double[] { -2.9209, -2.4671, -1.9442, -1.3424 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA20", "", "Are you able to cut your food using eating utensils?", "", 2.60495, new double[] { -3.3353, -2.8716, -2.4967, -1.9541 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA28", "", "Are you able to open a can with a hand can opener?", "", 2.17924, new double[] { -3.1873, -2.5066, -1.9442, -1.1747 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA29", "", "Are you able to pull heavy objects (10 pounds/5 kg) towards yourself?", "", 2.87862, new double[] { -2.5953, -2.0626, -1.5101, -0.928 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA35", "", "Are you able to open and close a zipper?", "", 2.36169, new double[] { -3.7694, -3.0788, -2.4671, -1.8455 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA38", "", "Are you able to dry your back with a towel?", "", 2.80767, new double[] { -3.0294, -2.5066, -1.9442, -1.3917 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA44", "", "Are you able to put on a shirt or blouse?", "", 3.23338, new double[] { -3.4142, -2.99, -2.4178, -1.7173 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA48", "", "Are you able to peel fruit?", "", 2.2806, new double[] { -3.5918, -2.9604, -2.5066, -1.8455 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA54", "", "Are you able to button your shirt?", "", 2.14883, new double[] { -3.868, -3.286, -2.6151, -1.9245 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFB21", "", "Are you able to pick up coins from a table top?", "", 2.32114, new double[] { -3.868, -3.0097, -2.3882, -1.6877 }, -1, "",
              response("Without any difficulty", 5),
              response("With a little difficulty", 4),
              response("With some difficulty", 3),
              response("With much difficulty", 2),
              response("Unable to do", 1)
      ),
      item("PFB22", "", "Are you able to hold a plate full of food?", "", 3.11175, new double[] { -3.6214, -2.7828, -2.2895, -1.5989 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),

      item("PFB30", "", "Are you able to open a new milk carton?", "", 2.04747, new double[] { -4.0456, -3.4043, -2.7137, -1.7567 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFB33", "", "Are you able to remove something from your back pocket?", "", 2.57454, new double[] { -3.5227, -2.911, -2.3783, -1.7469 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFB36", "", "Are you able to put on a pullover sweater?", "", 2.48332, new double[] { -3.7792, -3.1182, -2.2895, -1.5101 }, -1, "",
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
