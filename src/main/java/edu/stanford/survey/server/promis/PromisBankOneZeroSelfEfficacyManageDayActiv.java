/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
 * Item bank for PROMIS assessment. Generated from OID 4F2916ED-F787-409B-AA65-784D8D324BE4.
 */
public class PromisBankOneZeroSelfEfficacyManageDayActiv {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("SEMDA001", "CURRENT level of confidence", "I can take a bath or shower.", "", 3.08, new double[] { -2.18, -1.75, -1.33, -0.79 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA002", "CURRENT level of confidence", "I can eat without help from anyone.", "", 3.03, new double[] { -2.61, -2.03, -1.61, -1.2 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA003", "CURRENT level of confidence", "I can take care of my personal hygiene without help from anyone (for example: brush my teeth, comb my hair, shave, apply makeup).", "", 3.23, new double[] { -2.27, -2.0, -1.49, -1.1 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA004", "CURRENT level of confidence", "I can dress myself in the way I want to be dressed (including buttoning clothes and putting on shoes).", "", 2.77, new double[] { -2.43, -1.84, -1.31, -0.82 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA005", "CURRENT level of confidence", "I can get in and out of bed without falling.", "", 3.26, new double[] { -2.11, -1.75, -1.21, -0.71 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA006", "CURRENT level of confidence", "I can get in and out of a chair.", "", 3.27, new double[] { -2.32, -1.81, -1.28, -0.72 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA007", "CURRENT level of confidence", "I can get to the bathroom in time.", "", 2.46, new double[] { -2.38, -1.73, -1.07, -0.57 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA008", "CURRENT level of confidence", "I can manage my clothes when I need to use the toilet.", "", 3.43, new double[] { -2.21, -1.91, -1.4, -0.86 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA009", "CURRENT level of confidence", "I can stand for 5 minutes (for example: waiting in a line, waiting for a bus).", "", 3.57, new double[] { -1.66, -1.33, -0.96, -0.6 }, -1, "",
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
      item("SEMDA011", "CURRENT level of confidence", "I can walk a block (about 300 feet or 100 meters) on flat ground.", "", 3.92, new double[] { -1.44, -1.21, -0.87, -0.5 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA012", "CURRENT level of confidence", "I can exercise at a moderate level for 10 minutes (for example: walking briskly, biking, swimming, aerobics).", "", 2.94, new double[] { -1.36, -1.05, -0.61, -0.2 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA013", "CURRENT level of confidence", "I can exercise at a vigorous level for 10 minutes (for example: running, jogging).", "", 1.95, new double[] { -0.83, -0.39, 0.12, 0.59 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA014", "CURRENT level of confidence", "I can get around in an unfamiliar environment.", "", 3.07, new double[] { -1.69, -1.14, -0.58, 0.02 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA015", "CURRENT level of confidence", "I can travel to a new destination alone.", "", 2.99, new double[] { -1.4, -1.03, -0.6, -0.1 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA016", "CURRENT level of confidence", "I can go outside in challenging weather for me.", "", 3.39, new double[] { -1.47, -1.07, -0.59, -0.09 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA017", "CURRENT level of confidence", "I can climb one flight of stairs (with or without rails).", "", 3.19, new double[] { -1.43, -1.08, -0.67, -0.27 }, -1, "",
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
      item("SEMDA019", "CURRENT level of confidence", "I can perform my daily activities even if someone is rushing me.", "", 3.95, new double[] { -1.55, -1.14, -0.61, -0.05 }, -1, "",
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
      item("SEMDA022", "CURRENT level of confidence", "I can drive a car.", "", 2.1, new double[] { -1.32, -1.11, -0.87, -0.4 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA023", "CURRENT level of confidence", "I can use public transportation.", "", 3.05, new double[] { -1.33, -1.04, -0.74, -0.28 }, -1, "",
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
      item("SEMDA025", "CURRENT level of confidence", "I can use a computer (for example: use keyboard, see screen, login).", "", 1.98, new double[] { -2.04, -1.69, -1.23, -0.67 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA026", "CURRENT level of confidence", "I can use a telephone to schedule an appointment.", "", 2.86, new double[] { -2.37, -1.93, -1.5, -1.04 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA027", "CURRENT level of confidence", "I can engage in hobbies or recreational activities.", "", 3.51, new double[] { -1.72, -1.24, -0.8, -0.35 }, -1, "",
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
      item("SEMDA029", "CURRENT level of confidence", "I can maintain my finances (for example: write checks, pay bills).", "", 2.24, new double[] { -2.21, -1.67, -1.19, -0.57 }, -1, "",
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
      ),
      item("SEMDA031", "CURRENT level of confidence", "I can concentrate on something difficult.", "", 2.26, new double[] { -1.98, -1.31, -0.64, 0.09 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA032", "CURRENT level of confidence", "I can prepare my own meals (for example: plan and cook full meals by myself).", "", 3.09, new double[] { -1.73, -1.39, -0.91, -0.45 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA033", "CURRENT level of confidence", "I can take my medications in the right doses and at the right times.", "", 1.9, new double[] { -2.61, -2.1, -1.53, -0.8 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA034", "CURRENT level of confidence", "I can find new ways to manage daily activities when the old way doesn't work.", "", 2.62, new double[] { -2.28, -1.71, -1.01, -0.2 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMDA035", "CURRENT level of confidence", "I can recognize risks (for example: handling hot liquids, walking on uneven ground) and take steps to prevent accidents.", "", 2.53, new double[] { -2.28, -1.76, -1.17, -0.59 }, -1, "",
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
