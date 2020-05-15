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
 * Item bank for PROMIS assessment. Generated from OID ED3A865E-9813-4307-8C14-F08B8921E11D.
 */
public class PromisShortFormOneZeroSelfEfficacyManageMedsTx8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("SEMMT002", "CURRENT level of confidence", "I can remember to take my medication as prescribed.", "", 2.75, new double[] { -2.56, -1.89, -1.26, -0.37 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT005", "CURRENT level of confidence", "I can follow directions when my doctor changes my medications.", "", 4.25, new double[] { -2.44, -2.02, -1.45, -0.67 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT006", "CURRENT level of confidence", "I can manage my medication without help.", "", 3.5, new double[] { -2.19, -1.83, -1.39, -0.68 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT010", "CURRENT level of confidence", "I can list my medications, including the doses and schedule.", "", 2.08, new double[] { -2.19, -1.67, -0.96, -0.22 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT017", "CURRENT level of confidence", "I can use technology to help me manage my medication and treatments (for example: to get information, avoid side-effects, schedule reminders).", "", 2.7, new double[] { -2.32, -1.86, -1.34, -0.6 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT019", "CURRENT level of confidence", "I can take my medication when I am working or away from home.", "", 3.76, new double[] { -2.42, -2.06, -1.5, -0.77 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT023", "CURRENT level of confidence", "I can take my medication when there is a change in my usual day (unexpected things happen).", "", 3.55, new double[] { -2.37, -1.9, -1.18, -0.38 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT024", "CURRENT level of confidence", "I can figure out what treatment I need when my symptoms change.", "", 1.82, new double[] { -2.35, -1.62, -0.74, 0.22 }, -1, "",
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
