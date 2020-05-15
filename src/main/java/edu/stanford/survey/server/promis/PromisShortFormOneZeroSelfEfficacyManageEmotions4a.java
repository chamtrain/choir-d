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
 * Item bank for PROMIS assessment. Generated from OID FFABFF2C-09ED-468A-A0B8-5CEF663134D7.
 */
public class PromisShortFormOneZeroSelfEfficacyManageEmotions4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("SEMEM010", "CURRENT level of confidence", "I can bounce back from disappointment.", "", 3.81, new double[] { -2.12, -1.34, -0.62, 0.22 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM012", "CURRENT level of confidence", "I can avoid feeling discouraged.", "", 4.0, new double[] { -1.77, -1.02, -0.31, 0.56 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM015", "CURRENT level of confidence", "I can handle negative feelings.", "", 4.23, new double[] { -1.77, -1.18, -0.45, 0.41 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM018", "CURRENT level of confidence", "I can find ways to manage stress.", "", 4.23, new double[] { -1.89, -1.23, -0.47, 0.36 }, -1, "",
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
