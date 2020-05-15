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
 * Item bank for PROMIS assessment. Generated from OID 095CC452-70F3-4805-A859-466C8847C558.
 */
public class PromisBankOneZeroSelfEfficacyManageSocInter {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("SEMSS001", "CURRENT level of confidence", "I can stay involved in community or religious activities.", "", 1.74, new double[] { -2.24, -1.67, -0.87, -0.11 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS003", "CURRENT level of confidence", "I have someone who will go out and do things with me.", "", 2.52, new double[] { -2.05, -1.58, -1.05, -0.4 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS004", "CURRENT level of confidence", "I can maintain my usual social activities.", "", 2.18, new double[] { -1.98, -1.46, -0.81, -0.08 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS005", "CURRENT level of confidence", "I can keep in touch with friends and family.", "", 2.72, new double[] { -2.34, -1.77, -1.23, -0.47 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS006", "CURRENT level of confidence", "I have someone to help me plan and make decisions related to my illness.", "", 2.85, new double[] { -1.82, -1.46, -1.04, -0.41 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS007", "CURRENT level of confidence", "I have someone who helps me understand medical information.", "", 2.97, new double[] { -2.0, -1.65, -1.12, -0.46 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS008", "CURRENT level of confidence", "If I need help, I have someone to help me manage my daily activities.", "", 2.88, new double[] { -1.96, -1.54, -1.07, -0.4 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS009", "CURRENT level of confidence", "People understand when I need help and when I don't need help.", "", 2.22, new double[] { -1.84, -1.33, -0.59, 0.21 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS010", "CURRENT level of confidence", "If I need help, I have someone to help with my financial affairs.", "", 2.49, new double[] { -1.9, -1.47, -1.04, -0.39 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS011", "CURRENT level of confidence", "I can refuse help when I don't need it.", "", 2.38, new double[] { -2.9, -2.21, -1.5, -0.6 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
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
      item("SEMSS015", "CURRENT level of confidence", "I can tell others about my health problems.", "", 2.37, new double[] { -2.33, -1.64, -0.91, -0.19 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS016", "CURRENT level of confidence", "I can communicate well with my doctors and nurses.", "", 2.51, new double[] { -2.8, -2.14, -1.39, -0.49 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS017", "CURRENT level of confidence", "I know when to stop talking about my condition.", "", 1.93, new double[] { -2.7, -2.12, -1.22, -0.34 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS018", "CURRENT level of confidence", "I can talk to others about my condition without being embarrassed.", "", 1.94, new double[] { -2.52, -1.77, -1.07, -0.24 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS019", "CURRENT level of confidence", "People are accepting of my condition.", "", 2.3, new double[] { -2.3, -1.67, -0.88, -0.03 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS020", "CURRENT level of confidence", "People are concerned about my health.", "", 2.02, new double[] { -2.34, -1.86, -0.99, -0.1 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS021", "CURRENT level of confidence", "Friends and family will come to see me when I am sick.", "", 2.26, new double[] { -2.16, -1.58, -0.93, -0.23 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS022", "CURRENT level of confidence", "My doctors and nurses listen to my needs.", "", 2.22, new double[] { -3.01, -2.01, -1.37, -0.43 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSS023", "CURRENT level of confidence", "I can attend social events without being embarrassed.", "", 2.29, new double[] { -2.18, -1.57, -0.96, -0.22 }, -1, "",
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
