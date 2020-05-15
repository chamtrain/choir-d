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
 * Item bank for PROMIS assessment. Generated from OID DBE92586-E64D-428B-B5D8-194FAC4F984A.
 */
public class PromisShortFormOneZeroSocialSatDSA7a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("SRPSAT05", "In the past 7 days", "I am satisfied with the amount of time I spend doing leisure activities", "", 4.292, new double[] { -1.157, -0.559, 0.15, 0.811 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT10", "In the past 7 days", "I am satisfied with my current level of social activity", "", 3.745, new double[] { -1.134, -0.558, 0.192, 0.916 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT20", "In the past 7 days", "I am satisfied with my ability to do things for my friends", "", 3.888, new double[] { -1.464, -0.781, -0.003, 0.76 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT23", "In the past 7 days", "I am satisfied with my ability to do leisure activities", "", 4.352, new double[] { -1.232, -0.649, 0.002, 0.724 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT25", "In the past 7 days", "I am satisfied with my current level of activities with my friends", "", 4.083, new double[] { -1.149, -0.577, 0.157, 0.899 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT33", "In the past 7 days", "I am satisfied with my ability to do things for fun outside my home", "", 4.9, new double[] { -1.072, -0.531, 0.126, 0.723 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SRPSAT48", "In the past 7 days", "I am satisfied with my ability to do things for fun at home (like reading, listening to music, etc.)", "", 2.702, new double[] { -1.591, -0.89, -0.277, 0.603 }, -1, "",
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
