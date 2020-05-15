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
 * Item bank for PROMIS assessment. Generated from OID CAEAB8FF-AA11-4C39-B089-F0AE5DDF50F0.
 */
public class PromisShortFormTwoZeroEmotionalSupport8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("FSE31053x2", "", "I have someone who will listen to me when I need to talk", "", 5.23395, new double[] { -1.8827, -1.2382, -0.5505, 0.2688 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("FSE31059x2", "", "I have someone to confide in or talk to about myself or my problems", "", 5.68075, new double[] { -1.6363, -1.0192, -0.4502, 0.2279 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("FSE31066x2", "", "I have someone with whom to share my most private worries and fears", "", 4.38305, new double[] { -1.5159, -0.9434, -0.3677, 0.3016 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("FSE31069x2", "", "I have someone who understands my problems", "", 4.38844, new double[] { -1.834, -1.1945, -0.3903, 0.5488 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("SS12x", "", "I have someone who makes me feel appreciated", "", 3.76044, new double[] { -1.9498, -1.2936, -0.5477, 0.2991 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("SSE-CaPS6", "", "I have someone I trust to talk with about my feelings", "", 5.00456, new double[] { -1.5987, -1.077, -0.4634, 0.2217 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("SSQ3x2", "", "I have someone to talk with when I have a bad day", "", 4.42773, new double[] { -1.8454, -1.1174, -0.4471, 0.3194 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("SSQ4x2", "", "I have someone I trust to talk with about my problems", "", 5.24799, new double[] { -1.6829, -1.0766, -0.5305, 0.1781 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
