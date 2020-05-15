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
 * Item bank for PROMIS assessment. Generated from OID BE5C09D1-1FB2-474C-B17C-8EC8F98F396D.
 */
public class PromisShortFormTwoZeroSocialIsolation4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
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
