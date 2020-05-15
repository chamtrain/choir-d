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
 * Item bank for PROMIS assessment. Generated from OID 4468873E-B8B0-4671-890D-38CB039510B7.
 */
public class PromisPedShortFormOneZeroMeaningAndPurpose8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("SWB_FO_010R1", "Thinking about my life,", "I expect to enjoy my future life.", "", 3.287, new double[] { -2.923, -2.334, -1.549, -0.573 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_024R1", "Thinking about my life,", "I feel hopeful about my future.", "", 3.316, new double[] { -2.679, -2.079, -1.326, -0.401 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_025R1", "Thinking about my life,", "I am positive about my future.", "", 3.582, new double[] { -2.698, -1.962, -1.233, -0.312 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_037R1", "Thinking about my life,", "I know where I am going in life.", "", 2.287, new double[] { -2.427, -1.735, -0.836, 0.072 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_038R1", "Thinking about my life,", "I can reach my goals in life.", "", 2.941, new double[] { -2.86, -2.155, -1.332, -0.287 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_045R1", "Thinking about my life,", "My life is filled with meaning.", "", 2.923, new double[] { -2.545, -1.923, -1.091, -0.189 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_047R1", "Thinking about my life,", "My life has purpose.", "", 3.435, new double[] { -2.51, -1.958, -1.19, -0.433 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_052R1", "Thinking about my life,", "I have a reason for living.", "", 3.044, new double[] { -2.655, -2.133, -1.475, -0.723 }, -1, "",
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
