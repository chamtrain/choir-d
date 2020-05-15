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
 * Item bank for PROMIS assessment. Generated from OID AE9518D3-B510-49E8-8431-702EEC7E7D54.
 */
public class PromisBankOneZeroSelfEfficacyManageSymptoms {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("SEMSX001", "CURRENT level of confidence", "I can make a moderate reduction in my symptoms.", "", 2.08, new double[] { -1.71, -1.11, -0.23, 0.65 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX002", "CURRENT level of confidence", "I can reduce my symptoms to my satisfaction.", "", 2.23, new double[] { -1.31, -0.7, 0.01, 0.8 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX003", "CURRENT level of confidence", "I can control my symptoms by taking my medications.", "", 2.5, new double[] { -1.93, -1.35, -0.64, 0.13 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX004", "CURRENT level of confidence", "I can control my symptoms by using methods other than taking medication (for example: relaxation exercises, distraction).", "", 1.82, new double[] { -1.59, -0.91, -0.15, 0.76 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX005", "CURRENT level of confidence", "I can do something to reduce my symptoms when they worsen.", "", 2.41, new double[] { -1.58, -0.97, -0.19, 0.62 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX006", "CURRENT level of confidence", "I can do something to prevent my symptoms from worsening.", "", 2.31, new double[] { -1.41, -0.83, -0.09, 0.7 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX007", "CURRENT level of confidence", "I can manage unexpected or new symptoms.", "", 2.65, new double[] { -1.53, -0.9, -0.07, 0.74 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
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
      item("SEMSX012", "CURRENT level of confidence", "I can manage my symptoms as well as other people with symptoms like mine.", "", 2.63, new double[] { -2.13, -1.48, -0.71, 0.08 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX013", "CURRENT level of confidence", "I can keep my symptoms from interfering with my sleep.", "", 2.04, new double[] { -1.61, -0.93, -0.2, 0.49 }, -1, "",
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
      item("SEMSX016", "CURRENT level of confidence", "I can keep my symptoms from interfering with my recreational activities.", "", 3.14, new double[] { -1.34, -0.8, -0.21, 0.48 }, -1, "",
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
      item("SEMSX018", "CURRENT level of confidence", "I can enjoy things, despite my symptoms.", "", 3.03, new double[] { -1.92, -1.35, -0.75, -0.05 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX019", "CURRENT level of confidence", "I can still accomplish most of my goals in life, despite my symptoms.", "", 3.2, new double[] { -1.59, -1.13, -0.53, 0.15 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX020", "CURRENT level of confidence", "I can live a normal life, despite my symptoms.", "", 3.18, new double[] { -1.63, -1.18, -0.51, 0.18 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX021", "CURRENT level of confidence", "I can be physically active, despite my symptoms.", "", 2.7, new double[] { -1.53, -0.99, -0.39, 0.21 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX022", "CURRENT level of confidence", "I can maintain my sense of humor, despite my symptoms.", "", 2.74, new double[] { -2.22, -1.59, -0.97, -0.27 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX023", "CURRENT level of confidence", "I can recognize when my symptoms change.", "", 2.19, new double[] { -2.44, -1.83, -1.13, -0.2 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX024", "CURRENT level of confidence", "I know what to do when my symptoms worsen.", "", 2.36, new double[] { -2.07, -1.54, -0.78, 0.01 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX025", "CURRENT level of confidence", "I can rely on my judgment to manage my symptoms, even when others disagree with me.", "", 2.49, new double[] { -2.03, -1.46, -0.7, 0.21 }, -1, "",
          response("I am not at all confident", 1),
          response("I am a little confident", 2),
          response("I am somewhat confident", 3),
          response("I am quite confident", 4),
          response("I am very confident", 5)
      ),
      item("SEMSX026", "CURRENT level of confidence", "I can manage my symptoms when I am in an unfamiliar place.", "", 3.7, new double[] { -1.84, -1.24, -0.62, 0.13 }, -1, "",
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
      ),
      item("SEMSX028", "CURRENT level of confidence", "I can manage my symptoms when I am tired.", "", 3.3, new double[] { -1.62, -1.06, -0.45, 0.3 }, -1, "",
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
