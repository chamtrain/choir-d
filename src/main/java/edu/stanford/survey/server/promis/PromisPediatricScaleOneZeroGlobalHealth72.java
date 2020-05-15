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
 * Item bank for PROMIS assessment. Generated from OID BFE356C6-D177-43D8-AB62-2650972F6A4F.
 */
public class PromisPediatricScaleOneZeroGlobalHealth72 {
  private static final ItemBank bank = itemBank(0.0, 0.0, 9, 9, 3.0,
      item("Global01R1", "", "In general, would you say your health is:", "", 3.522, new double[] { -2.65, -1.942, -0.782, 0.4 }, -1, "",
          response("Excellent", 5),
          response("Very Good", 4),
          response("Good", 3),
          response("Fair", 2),
          response("Poor", 1)
      ),
      item("Global02R1", "", "In general, would you say your quality of life is:", "", 2.97, new double[] { -2.812, -1.895, -0.81, 0.417 }, -1, "",
          response("Excellent", 5),
          response("Very Good", 4),
          response("Good", 3),
          response("Fair", 2),
          response("Poor", 1)
      ),
      item("Global03R1", "", "In general, how would you rate your physical health?", "", 3.592, new double[] { -2.581, -1.796, -0.719, 0.363 }, -1, "",
          response("Excellent", 5),
          response("Very Good", 4),
          response("Good", 3),
          response("Fair", 2),
          response("Poor", 1)
      ),
      item("Global04R1", "", "In general, how would you rate your mental health, including your mood and your ability to think?", "", 2.627, new double[] { -2.684, -1.795, -0.759, 0.329 }, -1, "",
          response("Excellent", 5),
          response("Very Good", 4),
          response("Good", 3),
          response("Fair", 2),
          response("Poor", 1)
      ),
      item("PedGlobal2R1", "", "How often do you feel really sad?", "", 0.951, new double[] { -4.289, -2.852, -0.751, 2.351 }, -1, "",
          response("Never", 5),
          response("Rarely", 4),
          response("Sometimes", 3),
          response("Often", 2),
          response("Always", 1)
      ),
      item("PedGlobal5R1", "", "How often do you have fun with friends?", "", 1.163, new double[] { -4.408, -3.071, -1.341, 0.518 }, -1, "",
          response("Always", 5),
          response("Often", 4),
          response("Sometimes", 3),
          response("Rarely", 2),
          response("Never", 1)
      ),
      item("PedGlobal6R1", "", "how often do your parents listen to your ideas?", "", 0.998, new double[] { -4.567, -2.961, -0.834, 1.116 }, -1, "",
          response("Always", 5),
          response("Often", 4),
          response("Sometimes", 3),
          response("Rarely", 2),
          response("Never", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
