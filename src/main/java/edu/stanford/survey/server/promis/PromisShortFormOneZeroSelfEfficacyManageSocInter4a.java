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
 * Item bank for PROMIS assessment. Generated from OID 169B88EB-B2B2-48DA-B7B5-AC183F00000E.
 */
public class PromisShortFormOneZeroSelfEfficacyManageSocInter4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("SEMSS012", "CURRENT level of confidence", "I can ask for help when I don't understand something.", "", 2.69, new double[] { -2.52, -2.04, -1.4, -0.56 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS013", "CURRENT level of confidence", "I can get emotional support when I need it.", "", 3.15, new double[] { -2.02, -1.45, -0.87, -0.22 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS014", "CURRENT level of confidence", "I can talk about my health problems with someone.", "", 3.97, new double[] { -2.0, -1.52, -1.04, -0.4 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS024", "CURRENT level of confidence", "If I need help, I can find someone to take me to the doctor's office.", "", 3.35, new double[] { -2.04, -1.66, -1.25, -0.57 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
