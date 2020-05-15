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
 * Item bank for PROMIS assessment. Generated from OID F926AB35-6863-499C-852A-258595E051E1.
 */
public class PromisShortFormOneZeroSelfEfficacyManageDayActiv8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("SEMDA008", "CURRENT level of confidence", "I can manage my clothes when I need to use the toilet.", "", 3.43, new double[] { -2.21, -1.91, -1.4, -0.86 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA010", "CURRENT level of confidence", "I can walk around inside my house.", "", 4.41, new double[] { -1.86, -1.52, -1.2, -0.76 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA018", "CURRENT level of confidence", "I can go shopping and run errands.", "", 4.75, new double[] { -1.49, -1.21, -0.83, -0.37 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA020", "CURRENT level of confidence", "I can lift and carry groceries.", "", 4.06, new double[] { -1.41, -1.06, -0.63, -0.23 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA021", "CURRENT level of confidence", "I can perform my household chores.", "", 4.79, new double[] { -1.49, -1.07, -0.7, -0.26 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA024", "CURRENT level of confidence", "I can keep doing my usual activities at work.", "", 3.06, new double[] { -1.04, -0.84, -0.53, -0.11 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA028", "CURRENT level of confidence", "I can take care of others (for example: cook for others, help them dress, watch children).", "", 4.03, new double[] { -1.33, -0.99, -0.59, -0.11 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA030", "CURRENT level of confidence", "I can maintain a regular exercise program.", "", 2.27, new double[] { -1.6, -0.99, -0.38, 0.23 }, -1, "",
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
