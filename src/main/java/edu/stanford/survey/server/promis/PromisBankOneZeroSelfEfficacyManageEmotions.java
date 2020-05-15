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
 * Item bank for PROMIS assessment. Generated from OID FCFC20C4-3651-4BBD-B5E3-F7DFD210EEF4.
 */
public class PromisBankOneZeroSelfEfficacyManageEmotions {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("SEMEM001", "CURRENT level of confidence", "I can keep anxiety from becoming overwhelming.", "", 2.81, new double[] { -1.89, -1.19, -0.37, 0.5 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM002", "CURRENT level of confidence", "I can use relaxation to deal with worries.", "", 3.02, new double[] { -1.86, -1.13, -0.38, 0.39 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM003", "CURRENT level of confidence", "I can relax my body to reduce my anxiety.", "", 3.15, new double[] { -1.7, -1.04, -0.31, 0.43 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM004", "CURRENT level of confidence", "I can manage anxiety about injuring myself or others (for example: falling, dropping a child, a driving accident).", "", 2.07, new double[] { -2.16, -1.62, -0.91, -0.08 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM005", "CURRENT level of confidence", "I can focus on something else to decrease anxiety.", "", 3.16, new double[] { -1.92, -1.32, -0.53, 0.31 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM006", "CURRENT level of confidence", "I can prevent my illness from making me feel discouraged.", "", 3.01, new double[] { -1.73, -1.08, -0.4, 0.43 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM007", "CURRENT level of confidence", "I can avoid feeling helpless.", "", 3.72, new double[] { -1.8, -1.17, -0.59, 0.13 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM008", "CURRENT level of confidence", "When I'm feeling down, I can find ways to make myself feel better.", "", 3.66, new double[] { -2.01, -1.26, -0.56, 0.27 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM009", "CURRENT level of confidence", "I can manage my frustration.", "", 3.63, new double[] { -1.98, -1.28, -0.45, 0.45 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM010", "CURRENT level of confidence", "I can bounce back from disappointment.", "", 3.81, new double[] { -2.12, -1.34, -0.62, 0.22 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM011", "CURRENT level of confidence", "I can avoid becoming angry.", "", 2.55, new double[] { -1.92, -1.21, -0.35, 0.61 }, -1, "",
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
      item("SEMEM013", "CURRENT level of confidence", "I can hear about symptoms and side effects without getting discouraged.", "", 2.63, new double[] { -2.03, -1.38, -0.5, 0.46 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM014", "CURRENT level of confidence", "I can avoid upsetting thoughts.", "", 3.5, new double[] { -1.82, -1.09, -0.29, 0.54 }, -1, "",
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
      item("SEMEM016", "CURRENT level of confidence", "I can handle upsetting situations.", "", 4.14, new double[] { -1.8, -1.17, -0.45, 0.48 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM017", "CURRENT level of confidence", "I can keep emotional distress from interfering with things I want to do.", "", 3.91, new double[] { -1.78, -1.14, -0.39, 0.39 }, -1, "",
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
      ),
      item("SEMEM019", "CURRENT level of confidence", "I can handle the stress of going for treatment of my medical conditions.", "", 2.52, new double[] { -2.31, -1.66, -0.86, 0.05 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM021", "CURRENT level of confidence", "I can manage the loss of my ability to do things that are important to me (for example: parenting, work, hobbies, attend school).", "", 1.96, new double[] { -1.71, -0.93, -0.03, 0.85 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM022", "CURRENT level of confidence", "I can manage my anxiety about telling others I have health problems.", "", 2.12, new double[] { -2.11, -1.43, -0.65, 0.27 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM023", "CURRENT level of confidence", "I can manage my anger when others make insensitive comments about my health problems.", "", 2.62, new double[] { -1.91, -1.29, -0.61, 0.22 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM024", "CURRENT level of confidence", "I can manage my anger when others don't understand what I am going through.", "", 2.98, new double[] { -1.86, -1.23, -0.52, 0.31 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM025", "CURRENT level of confidence", "I can stay positive when I feel like I am the only one going through this.", "", 3.44, new double[] { -1.84, -1.17, -0.49, 0.26 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMEM026", "CURRENT level of confidence", "I can use a strategy (for example: humor, leaving a situation) to keep from getting upset.", "", 2.97, new double[] { -2.2, -1.46, -0.69, 0.17 }, -1, "",
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
