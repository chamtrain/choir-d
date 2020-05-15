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
 * Item bank for PROMIS assessment. Generated from OID 9F601C2B-1DF3-4D0E-ADA4-BC9622ACA8FF.
 */
public class PromisShortFormTwoZeroCognitiveFunction6a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
      item("PC25r", "In the past 7 days,", "I have had to work really hard to pay attention or I would make a mistake", "", 3.0151, new double[] { -1.9623, -1.358, -0.7005, 0.007 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC2r", "In the past 7 days,", "My thinking has been slow", "", 3.2253, new double[] { -1.8633, -1.37, -0.7535, -0.0608 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC35r", "In the past 7 days,", "It has seemed like my brain was not working as well as usual", "", 2.6891, new double[] { -1.969, -1.4102, -0.6554, 0.1612 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC36r", "In the past 7 days,", "I have had to work harder than usual to keep track of what I was doing", "", 3.4153, new double[] { -1.7812, -1.3247, -0.7109, 0.0305 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC42r", "In the past 7 days,", "I have had trouble shifting back and forth between different activities that require thinking", "", 2.88, new double[] { -2.0648, -1.4349, -0.6712, 0.0503 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC8r", "In the past 7 days,", "I have had trouble concentrating", "", 2.1855, new double[] { -2.126, -1.4102, -0.4442, 0.6139 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
