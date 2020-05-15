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
 * Item bank for PROMIS assessment. Generated from OID B73BC983-6C1A-4911-B69C-FD12BE4B645B.
 */
public class PromisPediatricShortFormOneOneDepressiveSymptoms8b {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("228R1", "In the past 7 days", "I felt sad.", "", 1.90183, new double[] { -0.7467, 0.2707, 1.7397, 2.7537 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("3952aR2", "In the past 7 days", "It was hard for me to have fun.", "", 1.71154, new double[] { 0.3061, 1.0867, 2.261, 2.9965 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("461R1", "In the past 7 days", "I felt alone.", "", 2.10701, new double[] { 0.3142, 0.9805, 1.9058, 2.5769 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("488R1", "In the past 7 days", "I could not stop feeling sad.", "", 2.53484, new double[] { 0.6094, 1.1281, 1.923, 2.4619 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("5035R1", "In the past 7 days", "I felt like I couldn't do anything right.", "", 2.42385, new double[] { 0.0599, 0.7987, 1.7014, 2.3208 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("5041R1", "In the past 7 days", "I felt everything in my life went wrong.", "", 2.46047, new double[] { 0.35, 0.9589, 1.7392, 2.1924 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("711R1", "In the past 7 days", "I felt lonely.", "", 2.04306, new double[] { -0.1657, 0.6286, 1.7399, 2.3899 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("712R1", "In the past 7 days", "I felt unhappy.", "", 2.13731, new double[] { -0.6324, 0.4584, 1.6838, 2.423 }, -1, "",
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
