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
 * Item bank for PROMIS assessment. Generated from OID A2207272-A02D-4054-9F3A-6657F126145F.
 */
public class PromisShortFormTwoZeroInformationalSupport4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("FSE31054x2", "", "I have someone to give me good advice about a crisis if I need it", "", 5.14644, new double[] { -1.7237, -1.0292, -0.2362, 0.6047 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("FSE31058x2", "", "I have someone to give me information if I need it", "", 4.21638, new double[] { -1.9425, -1.2691, -0.2813, 0.6966 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("SS7x", "", "I have someone to turn to for suggestions about how to deal with a problem", "", 4.6411, new double[] { -1.8014, -1.0455, -0.2894, 0.5998 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("SSQ7x", "", "I get useful advice about important things in life", "", 3.37114, new double[] { -1.8992, -1.0605, 0.0285, 1.0206 }, -1, "",
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
