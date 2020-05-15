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
 * Item bank for PROMIS assessment. Generated from OID D8B24FB5-41A1-4183-BF78-F1AE4D1CD726.
 */
public class NeuroQolShortFormTwoZeroCognitiveFunction {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQCOG22r1", "How much DIFFICULTY do you currently have...", "reading and following complex instructions  (e.g., directions for a new medication)?", "", 1.99413, new double[] { -2.7796, -1.9347, -1.0525, -0.18 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG24r1", "How much DIFFICULTY do you currently have...", "Planning for and keeping appointments that are not part of your weekly routine, (e.g., a therapy or doctor appointment, or a social gathering with friends and family)?", "", 2.0036, new double[] { -3.0204, -1.8757, -0.9692, -0.1291 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG25r1", "How much DIFFICULTY do you currently have...", "managing your time to do most of your daily activities?", "", 1.90767, new double[] { -2.9387, -1.8623, -0.8527, 0.2229 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG40r1", "How much DIFFICULTY do you currently have...", "learning new tasks or instructions?", "", 2.27017, new double[] { -2.7483, -1.802, -0.8962, 0.0953 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG64r1", "In the past 7 days", "I had to read something several times to understand it", "", 2.28352, new double[] { -2.3584, -1.6053, -0.5585, 0.5287 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG75r1", "In the past 7 days", "my thinking was slow", "", 3.2253, new double[] { -1.8633, -1.37, -0.7535, -0.0608 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG77r1", "In the past 7 days", "I had to work really hard to pay attention or I would make a mistake", "", 3.0151, new double[] { -1.9623, -1.358, -0.7005, 0.007 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG80r1", "In the past 7 days", "I had trouble concentrating", "", 3.32175, new double[] { -1.8978, -1.4142, -0.6082, 0.204 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
