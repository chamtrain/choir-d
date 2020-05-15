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
 * Item bank for PROMIS assessment. Generated from OID 09676A0F-DAF2-4691-9DE7-627908837C32.
 */
public class PromisShortFormTwoZeroSocialIsolation8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("Iso-CaPS1", "", "I feel isolated even when I am not alone", "", 3.7812, new double[] { -0.3641, 0.4074, 1.3811, 1.9137 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("Iso-CaPS2", "", "I feel that people avoid talking to me", "", 3.23796, new double[] { -0.2789, 0.6725, 1.7752, 2.2087 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("Iso-CaPS3", "", "I feel detached from other people", "", 4.01632, new double[] { -0.5802, 0.1805, 1.0973, 1.7709 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("Iso-CaPS9", "", "I feel like a stranger to those around me", "", 4.24321, new double[] { -0.2751, 0.4727, 1.4172, 1.9278 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("UCLA11x2", "", "I feel left out", "", 3.86945, new double[] { -0.6775, 0.2171, 1.1507, 1.9481 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("UCLA13x3 ", "", "I feel that people barely know me", "", 3.15724, new double[] { -0.8517, 0.0587, 1.1001, 1.8146 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("UCLA14x2", "", "I feel isolated from others", "", 4.2454, new double[] { -0.473, 0.2612, 1.1182, 1.6763 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("UCLA18x2", "", "I feel that people are around me but not with me", "", 3.98622, new double[] { -0.6642, 0.1747, 1.1369, 1.7946 }, -1, "",
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
