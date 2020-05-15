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
 * Item bank for PROMIS assessment. Generated from OID 525A8F52-A84C-455C-8998-551D43A2D1FA.
 */
public class PromisParentProxyScaleOneZeroGlobalHealth7 {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("Global01_PXR1", "", "In general, would you say your child's health is:", "", 4.583, new double[] { -2.842, -2.05, -0.97, 0.148 }, -1, "",
          response("Excellent", 5),
          response("Very Good", 4),
          response("Good", 3),
          response("Fair", 2),
          response("Poor", 1)
      ),
      item("Global02_PXR1", "", "In general, would you say your child's quality of life is:", "", 2.787, new double[] { -3.189, -2.409, -1.238, 0.147 }, -1, "",
          response("Excellent", 5),
          response("Very Good", 4),
          response("Good", 3),
          response("Fair", 2),
          response("Poor", 1)
      ),
      item("Global03_PXR1", "", "In general, how would you rate your child's physical health?", "", 4.635, new double[] { -2.84, -2.016, -0.989, 0.122 }, -1, "",
          response("Excellent", 5),
          response("Very Good", 4),
          response("Good", 3),
          response("Fair", 2),
          response("Poor", 1)
      ),
      item("Global04_PXR1", "", "In general, how would you rate your child's mental health, including mood and ability to think?", "", 1.97, new double[] { -3.074, -2.022, -1.007, 0.226 }, -1, "",
          response("Excellent", 5),
          response("Very Good", 4),
          response("Good", 3),
          response("Fair", 2),
          response("Poor", 1)
      ),
      item("PedGlobal2_PXR1", "", "How often does your child feel really sad?", "", 0.618, new double[] { -6.525, -3.906, -1.386, 3.095 }, -1, "",
          response("Never", 5),
          response("Rarely", 4),
          response("Sometimes", 3),
          response("Often", 2),
          response("Always", 1)
      ),
      item("PedGlobal5_PXR1", "", "How often does your child have fun with friends?", "", 1.02, new double[] { -5.653, -3.515, -1.49, 0.883 }, -1, "",
          response("Always", 5),
          response("Often", 4),
          response("Sometimes", 3),
          response("Rarely", 2),
          response("Never", 1)
      ),
      item("PedGlobal6_PXR1", "", "How often does your child feel that you listen to his or her ideas?", "", 0.973, new double[] { -5.557, -3.789, -1.591, 0.88 }, -1, "",
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
