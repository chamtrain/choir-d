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
 * Item bank for PROMIS assessment. Generated from OID 6C13662F-179A-45AD-A8DF-16843D902E28.
 */
public class PromisShortFormV10Anxiety4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("EDANX01", "In the past 7 days", "I felt fearful", "", 3.60215, new double[] { 0.3416, 1.0895, 1.9601, 2.6987 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX40", "In the past 7 days", "I found it hard to focus on anything other than my anxiety", "", 3.8832, new double[] { 0.4859, 1.2632, 2.1111, 2.8985 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX41", "In the past 7 days", "My worries overwhelmed me", "", 3.65951, new double[] { 0.3644, 1.0338, 1.7805, 2.6212 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX53", "In the past 7 days", "I felt uneasy", "", 3.65611, new double[] { -0.2318, 0.5952, 1.5635, 2.4991 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
