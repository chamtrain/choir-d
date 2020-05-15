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
 * Item bank for PROMIS assessment. Generated from OID 7224FB55-81D7-4131-A136-8C89907CDFBB.
 */
public class PromisScaleOneZeroPainIntensity3a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 3, 3, 3.0,
      item("PAINQU21", "", "What is your level of pain <u>right now</u>?", "", 2.61126, new double[] { -0.5362, 0.5882, 1.6364, 2.3559 }, -1, "",
          response("No pain", 1),
          response("Mild", 2),
          response("Moderate", 3),
          response("Severe", 4),
          response("Very severe", 5)
      ),
      item("PAINQU6", "In the past 7 days", "How intense was your pain at its <u>worst</u>?", "", 4.47278, new double[] { -1.5379, -0.8035, -0.0338, 0.6159 }, -1, "",
          response("Had no pain", 1),
          response("Mild", 2),
          response("Moderate", 3),
          response("Severe", 4),
          response("Very severe", 5)
      ),
      item("PAINQU8", "In the past 7 days", "How intense was your <u>average</u> pain?", "", 6.28042, new double[] { -1.3195, -0.1539, 0.6925, 1.4689 }, -1, "",
          response("Had no pain", 1),
          response("Mild", 2),
          response("Moderate", 3),
          response("Severe", 4),
          response("Very severe", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
