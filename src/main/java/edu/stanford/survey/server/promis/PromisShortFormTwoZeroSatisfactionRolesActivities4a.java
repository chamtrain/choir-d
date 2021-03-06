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
 * Item bank for PROMIS assessment. Generated from OID 2AF69E2C-7571-475B-A5DE-E77AF1DF4A17.
 */
public class PromisShortFormTwoZeroSatisfactionRolesActivities4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("SRPSAT06r1", "", "I am satisfied with my ability to do things for my family", "", 3.8899, new double[] { -1.6407, -1.1209, -0.3121, 0.4354 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT33_CaPS", "", "I am satisfied with my ability to do things for fun with others", "", 4.57753, new double[] { -1.4283, -0.8224, -0.0924, 0.6083 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT34r1", "", "I feel good about my ability to do things for my friends", "", 4.04326, new double[] { -1.6063, -0.9906, -0.1608, 0.554 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT49r1", "", "I am satisfied with my ability to perform my daily routines", "", 4.16999, new double[] { -1.5875, -1.0594, -0.3223, 0.3584 }, -1, "",
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
