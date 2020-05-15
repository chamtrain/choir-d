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
 * Item bank for PROMIS assessment. Generated from OID 856F5AFB-30F2-436B-817A-797C0FA96F14.
 */
public class PromisBankOneZeroSelfEfficacyManageMedsTx {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("SEMMT001", "CURRENT level of confidence", "I can take several medications on different schedules.", "", 2.51, new double[] { -2.28, -1.79, -1.19, -0.37 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT002", "CURRENT level of confidence", "I can remember to take my medication as prescribed.", "", 2.75, new double[] { -2.56, -1.89, -1.26, -0.37 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT003", "CURRENT level of confidence", "I know when and how to take my medications.", "", 4.15, new double[] { -2.47, -1.94, -1.47, -0.76 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT004", "CURRENT level of confidence", "I can fit my medication schedule into my daily routine.", "", 3.54, new double[] { -2.61, -2.11, -1.42, -0.67 }, -1, "",
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
      item("SEMMT007", "CURRENT level of confidence", "I can get help when I am not sure how to take my medicine.", "", 3.14, new double[] { -2.49, -2.12, -1.63, -0.8 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT008", "CURRENT level of confidence", "I can remember to refill my prescriptions before they run out.", "", 2.8, new double[] { -2.45, -1.94, -1.36, -0.62 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT009", "CURRENT level of confidence", "I can remember to take my medications when there is no one to remind me.", "", 3.47, new double[] { -2.37, -2.04, -1.38, -0.63 }, -1, "",
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
      item("SEMMT011", "CURRENT level of confidence", "I can actively participate in decisions about my treatment.", "", 3.34, new double[] { -2.6, -2.21, -1.43, -0.66 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT012", "CURRENT level of confidence", "I can find information to learn more about my treatment.", "", 3.16, new double[] { -2.47, -2.0, -1.36, -0.61 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT013", "CURRENT level of confidence", "I can use my own judgment regarding treatment alternatives (including not having treatment).", "", 2.24, new double[] { -2.49, -2.03, -1.31, -0.43 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT014", "CURRENT level of confidence", "I can work with my doctor to choose the treatment that seems right for me.", "", 3.1, new double[] { -2.72, -2.24, -1.5, -0.73 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT015", "CURRENT level of confidence", "I know what to do when my medication refill looks different than usual.", "", 3.27, new double[] { -2.5, -1.9, -1.46, -0.69 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT016", "CURRENT level of confidence", "I know what to do if I forget to take my medication(s).", "", 3.25, new double[] { -2.59, -1.99, -1.42, -0.62 }, -1, "",
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
      item("SEMMT018", "CURRENT level of confidence", "I can continue my treatment when traveling.", "", 3.24, new double[] { -2.39, -1.97, -1.45, -0.72 }, -1, "",
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
      item("SEMMT020", "CURRENT level of confidence", "I can take my medicine even if it causes mild side effects.", "", 1.86, new double[] { -2.63, -2.12, -1.3, -0.23 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT021", "CURRENT level of confidence", "I understand the difference between my symptoms and medication side effects.", "", 2.34, new double[] { -2.61, -1.9, -1.21, -0.23 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT022", "CURRENT level of confidence", "I can continue my treatment when I am not feeling well.", "", 2.78, new double[] { -2.81, -2.1, -1.37, -0.43 }, -1, "",
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
      ),
      item("SEMMT026", "CURRENT level of confidence", "I can follow a full treatment plan (including medication, diet, physical activity).", "", 2.19, new double[] { -2.76, -1.92, -1.09, -0.19 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMMT027", "CURRENT level of confidence", "I can travel to my local pharmacy to fill my prescriptions.", "", 1.97, new double[] { -2.47, -2.08, -1.59, -0.95 }, -1, "",
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
