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
 * Item bank for PROMIS assessment. Generated from OID C1014465-7C7C-4ADF-B57A-C4F53D9024C6.
 */
public class PromisShortFormTwoZeroCogFunctionAbilitiesSubset4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("PC43_2r", "In the past 7 days,", "My mind has been as sharp as usual", "", 2.8139, new double[] { -1.6685, -1.0181, -0.3185, 0.5545 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PC44_2r", "In the past 7 days,", "My memory has been as good as usual", "", 2.792, new double[] { -1.6347, -1.0014, -0.3391, 0.5771 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PC45_2r", "In the past 7 days,", "My thinking has been as fast as usual", "", 2.8961, new double[] { -1.6206, -0.9866, -0.2999, 0.5102 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PC47_2r", "In the past 7 days,", "I have been able to keep track of what I am doing, even if I am interrupted", "", 2.2397, new double[] { -2.0354, -1.2018, -0.3448, 0.6825 }, -1, "",
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
