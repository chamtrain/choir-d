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
 * Item bank for PROMIS assessment. Generated from OID A09CDF4E-E70C-40DC-849D-756A7C60C454.
 */
public class PromisShortFormOneZeroAppliedCogGeneralConcerns6a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
      item("PC2", "In the past 7 days", "My thinking has been slow", "", 4.15961, new double[] { -1.6535, -1.2356, -0.4838, 0.164 }, -1, "",
          response("Never", 1),
          response("Rarely (Once)", 2),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 4),
          response("Very often (Several times a day)", 5)
      ),
      item("PC25", "In the past 7 days", "I have had to work really hard to pay attention or I would make a mistake", "", 3.73301, new double[] { -2.0063, -1.2472, -0.5132, 0.1426 }, -1, "",
          response("Never", 1),
          response("Rarely (Once)", 2),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 4),
          response("Very often (Several times a day)", 5)
      ),
      item("PC35", "In the past 7 days", "It has seemed like my brain was not working as well as usual", "", 3.84063, new double[] { -1.6535, -1.1479, -0.4481, 0.2316 }, -1, "",
          response("Never", 1),
          response("Rarely (Once)", 2),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 4),
          response("Very often (Several times a day)", 5)
      ),
      item("PC36", "In the past 7 days", "I have had to work harder than usual to keep track of what I was doing", "", 4.73554, new double[] { -1.8222, -1.2857, -0.5996, 0.0708 }, -1, "",
          response("Never", 1),
          response("Rarely (Once)", 2),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 4),
          response("Very often (Several times a day)", 5)
      ),
      item("PC42", "In the past 7 days", "I have had trouble shifting back and forth between different activities that require thinking", "", 4.17844, new double[] { -1.9114, -1.4467, -0.6833, -0.0197 }, -1, "",
          response("Never", 1),
          response("Rarely (Once)", 2),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 4),
          response("Very often (Several times a day)", 5)
      ),
      item("PC8", "In the past 7 days", "I have had trouble concentrating", "", 3.6457, new double[] { -1.7413, -1.108, -0.3472, 0.4052 }, -1, "",
          response("Never", 1),
          response("Rarely (Once)", 2),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 4),
          response("Very often (Several times a day)", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
