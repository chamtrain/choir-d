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
 * Item bank for PROMIS assessment. Generated from OID 1B66CDEA-2687-4CF8-A642-9432A13A7272.
 */
public class PromisShortFormTwoZeroInstrumentalSupport4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("CCC31052x", "", "Do you have someone to help you if you are confined to bed?", "", 4.43878, new double[] { -1.2537, -0.8036, -0.3463, 0.375 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("CCC31055x", "", "Do you have someone to take you to the doctor if you need it?", "", 4.21559, new double[] { -1.5832, -1.1107, -0.5415, 0.1595 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("CCC31065x", "", "Do you have someone to help with your daily chores if you are sick?", "", 4.20553, new double[] { -1.2351, -0.6932, -0.1307, 0.558 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("SS6", "", "Do you have someone to run errands if you need it?", "", 3.9316, new double[] { -1.3992, -0.9113, -0.2531, 0.5287 }, -1, "",
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
