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
 * Item bank for PROMIS assessment. Generated from OID 5463216C-C1FE-4BFA-AE86-B7C8C6B15930.
 */
public class PromisShortFormOneZeroAppliedCogAbilities6a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
      item("PC-CaPS3", "In the past 7 days", "I have been able to think clearly without extra effort", "", 5.22487, new double[] { -1.3871, -0.9931, -0.339, 0.3329 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PC43_2", "In the past 7 days", "My mind has been as sharp as usual", "", 5.22086, new double[] { -1.2466, -0.9054, -0.2232, 0.4819 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PC44_2", "In the past 7 days", "My memory has been as good as usual", "", 4.46652, new double[] { -1.1795, -0.7908, -0.1935, 0.5067 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PC45_2", "In the past 7 days", "My thinking has been as fast as usual", "", 4.59306, new double[] { -1.2163, -0.8521, -0.2092, 0.5497 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PC47_2", "In the past 7 days", "I have been able to keep track of what I am doing, even if I am interrupted", "", 3.9623, new double[] { -1.5299, -0.9995, -0.2587, 0.519 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PC6", "In the past 7 days", "I have been able to concentrate", "", 5.18421, new double[] { -1.9058, -1.1007, -0.3265, 0.3917 }, -1, "",
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
