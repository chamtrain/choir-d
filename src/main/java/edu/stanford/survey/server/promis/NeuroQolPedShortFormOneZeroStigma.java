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
 * Item bank for PROMIS assessment. Generated from OID 0E7C17B1-7169-4E92-9A63-92EF9504B417.
 */
public class NeuroQolPedShortFormOneZeroStigma {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQSTGped03", "Lately", "Because of my illness, others my age avoided me", "", 3.05708, new double[] { 0.2785, 0.624, 1.1933, 1.9356 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTGped04", "Lately", "Because of my illness, I felt left out of things", "", 3.05708, new double[] { -0.3168, 0.0592, 0.8396, 1.5577 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTGped06", "Lately", "Because of my illness, others my age made fun of me", "", 3.05708, new double[] { 0.2354, 0.6318, 1.2291, 1.7656 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTGped07", "Lately", "Because of my illness, I felt embarrassed when I was in front of others my age", "", 3.05708, new double[] { -0.0737, 0.4577, 1.2143, 1.8196 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTGped10", "Lately", "Because of my illness, I was treated unfairly by others my age", "", 3.05708, new double[] { 0.194, 0.5329, 1.2369, 1.7109 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTGped13", "Lately", "Because of my illness, others my age tended to ignore my good points", "", 3.05708, new double[] { 0.1821, 0.4906, 1.2007, 1.7936 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTGped17", "Lately", "Because of my illness, I felt different from others my age", "", 3.05708, new double[] { -0.4536, 0.0865, 0.7, 1.1748 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTGped19", "Lately", "I avoided making new friends to avoid talking about my illness", "", 3.05708, new double[] { 0.2904, 0.6347, 1.1283, 1.7036 }, -1, "",
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
