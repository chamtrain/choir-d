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
 * Item bank for PROMIS assessment. Generated from OID 6C45966B-AB23-4CF9-9E35-F3E9F5FD90D7.
 */
public class NeuroQolPedShortFormOneZeroPain {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQPAIped01", "In the past 7 days", "I had a lot of pain", "", 3.95782, new double[] { -0.025, 0.5568, 1.3086, 1.8715 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQPAIped02", "In the past 7 days", "my pain was so bad that I needed to take medicine for it", "", 3.95782, new double[] { 0.3321, 0.7842, 1.2651, 1.4552 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQPAIped03", "In the past 7 days", "I missed school when I had pain", "", 3.95782, new double[] { 0.4724, 0.8048, 1.458, 2.3101 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQPAIped04", "In the past 7 days", "I had so much pain that I had to stop what I was doing", "", 3.95782, new double[] { 0.424, 0.8385, 1.437, 1.9024 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQPAIped05", "In the past 7 days", "I hurt all over my body", "", 3.95782, new double[] { 0.541, 0.9967, 1.4614, 2.1091 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQPAIped06", "In the past 7 days", "I had pain", "", 3.95782, new double[] { -0.1797, 0.5298, 1.2915, 1.8986 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQPAIped07", "In the past 7 days", "When you had pain, how long did it last?", "", 3.95782, new double[] { -0.2261, 0.5533, 1.1479, 1.7339 }, -1, "",
          response("a few seconds", 1),
          response("a few minutes", 2),
          response("a few hours", 3),
          response("a few days (less than a week)", 4),
          response("more than a week", 5)
      ),
      item("NQPAIped08", "In the past 7 days", "I had trouble sleeping when I had pain", "", 3.95782, new double[] { 0.1979, 0.6206, 1.1162, 1.6564 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQPAIped09", "In the past 7 days", "I had trouble watching TV when I had pain", "", 3.95782, new double[] { 0.655, 1.0264, 1.4602, 1.8841 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQPAIped10", "In the past 7 days", "It was hard for me to play or hang out with my friends when I had pain", "", 3.95782, new double[] { 0.1768, 0.7852, 1.2697, 1.5286 }, -1, "",
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
