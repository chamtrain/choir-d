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
 * Item bank for PROMIS assessment. Generated from OID 73233FB9-B6CF-403A-979C-720B8EE52145.
 */
public class PromisShortFormOneZeroSelfEfficacyManageSymptoms8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("SEMSX008", "CURRENT level of confidence", "I can manage my symptoms when I am at home.", "", 3.4, new double[] { -1.81, -1.27, -0.66, 0.13 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX009", "CURRENT level of confidence", "I can manage my symptoms in a public place.", "", 3.24, new double[] { -1.6, -1.02, -0.34, 0.41 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX010", "CURRENT level of confidence", "I can manage my symptoms during my daily activities.", "", 4.08, new double[] { -1.75, -1.32, -0.61, 0.14 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX011", "CURRENT level of confidence", "I can work with my doctor to manage my symptoms.", "", 2.87, new double[] { -2.33, -1.74, -1.04, -0.23 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX014", "CURRENT level of confidence", "I can keep my symptoms from interfering with relationships with friends and family.", "", 3.33, new double[] { -1.64, -1.09, -0.51, 0.14 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX015", "CURRENT level of confidence", "I can keep my symptoms from interfering with the work I need to do.", "", 3.22, new double[] { -1.38, -0.89, -0.3, 0.37 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX017", "CURRENT level of confidence", "I can keep my symptoms from interfering with my personal care.", "", 3.41, new double[] { -1.82, -1.31, -0.68, 0.02 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX027", "CURRENT level of confidence", "I can find the information I need to manage my symptoms.", "", 2.8, new double[] { -2.06, -1.48, -0.84, -0.02 }, -1, "",
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
