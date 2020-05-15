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
 * Item bank for PROMIS assessment. Generated from OID B3228EB4-E250-4AD2-9963-7A6D6E538D2C.
 */
public class PromisPedShortFormOneZeroPeerRel8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("1147R1", "In the past 7 days", "I was good at making friends.", "", 1.75929, new double[] { -2.6899, -2.1918, -1.079, -0.1481 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("210R1", "In the past 7 days", "Other kids wanted to be with me.", "", 1.82751, new double[] { -2.5084, -2.1507, -0.7006, 0.3684 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("233R2", "In the past 7 days", "Other kids wanted to be my friend.", "", 1.5447, new double[] { -2.6506, -2.1054, -0.7832, 0.4354 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("5018R1", "In the past 7 days", "I felt accepted by other kids my age.", "", 2.00436, new double[] { -1.9282, -1.5688, -0.8036, -0.0481 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("5055R1", "In the past 7 days", "My friends and I helped each other out.", "", 1.74234, new double[] { -3.0064, -2.5259, -1.3561, -0.4823 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("5056R1", "In the past 7 days", "I was able to talk about everything with my friends.", "", 1.94265, new double[] { -2.1794, -1.7575, -0.5905, 0.0958 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("5058R1", "In the past 7 days", "I was able to count on my friends.", "", 2.69421, new double[] { -1.9937, -1.7193, -0.8775, -0.1921 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("9020R1", "In the past 7 days", "Other kids wanted to talk to me.", "", 1.89534, new double[] { -2.9605, -2.2454, -1.005, -0.0636 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
