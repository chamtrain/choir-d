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
 * Item bank for PROMIS assessment. Generated from OID 3813AC70-6CBC-470B-998D-E8F1EE33E253.
 */
public class PromisPedBankOneZeroPositiveAffect {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
      item("SWB_P_001R1", "In the past 7 days,", "I felt calm.", "", 2.403, new double[] { -2.561, -1.574, -0.481, 0.706 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_004R1", "In the past 7 days,", "I felt peaceful.", "", 3.027, new double[] { -2.236, -1.519, -0.521, 0.5 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_005R1", "In the past 7 days,", "I felt satisfied.", "", 3.486, new double[] { -2.322, -1.539, -0.643, 0.398 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_006R1", "In the past 7 days,", "I felt content.", "", 2.806, new double[] { -2.554, -1.65, -0.646, 0.549 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_007R1", "In the past 7 days,", "I felt grateful.", "", 2.636, new double[] { -2.833, -1.907, -0.691, 0.399 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_008R1", "In the past 7 days,", "I felt thankful.", "", 1.988, new double[] { -3.1, -2.014, -0.744, 0.436 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_009R1", "In the past 7 days,", "I felt positive.", "", 3.507, new double[] { -2.575, -1.636, -0.729, 0.346 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_011R1", "In the past 7 days,", "I felt carefree.", "", 1.64, new double[] { -2.302, -1.396, -0.298, 0.933 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_012R1", "In the past 7 days,", "I felt relaxed.", "", 2.106, new double[] { -2.556, -1.557, -0.468, 0.731 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_014R1", "In the past 7 days,", "I felt comfortable.", "", 2.984, new double[] { -2.627, -1.748, -0.797, 0.346 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_015R1", "In the past 7 days,", "I felt fulfilled.", "", 3.43, new double[] { -2.292, -1.457, -0.388, 0.673 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_020R1", "In the past 7 days,", "I felt respected.", "", 2.438, new double[] { -2.37, -1.458, -0.433, 0.643 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_021R1", "In the past 7 days,", "I felt appreciated.", "", 2.422, new double[] { -2.381, -1.46, -0.517, 0.436 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_022R1", "In the past 7 days,", "I felt proud.", "", 2.917, new double[] { -2.442, -1.561, -0.593, 0.38 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_023R1", "In the past 7 days,", "I had much to be proud about.", "", 2.214, new double[] { -2.621, -1.651, -0.597, 0.401 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_024R1", "In the past 7 days,", "I felt pleased.", "", 2.577, new double[] { -2.873, -1.852, -0.672, 0.615 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_025R1", "In the past 7 days,", "I felt great.", "", 4.059, new double[] { -2.319, -1.572, -0.656, 0.361 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_026R1", "In the past 7 days,", "I felt cheerful.", "", 3.839, new double[] { -2.215, -1.49, -0.624, 0.446 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_027R1", "In the past 7 days,", "I felt happy.", "", 3.804, new double[] { -2.361, -1.776, -0.907, 0.287 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_028R1", "In the past 7 days,", "I had very strong happy feelings.", "", 3.753, new double[] { -2.143, -1.343, -0.492, 0.437 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_029R1", "In the past 7 days,", "I felt joyful.", "", 4.435, new double[] { -2.235, -1.457, -0.549, 0.45 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_030R1", "In the past 7 days,", "I felt delighted.", "", 3.984, new double[] { -2.193, -1.398, -0.478, 0.498 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_032R1", "In the past 7 days,", "My life was pleasurable.", "", 3.598, new double[] { -2.386, -1.581, -0.609, 0.367 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_033R1", "In the past 7 days,", "I smiled a lot.", "", 2.021, new double[] { -3.008, -1.891, -0.876, 0.285 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_034R1", "In the past 7 days,", "I laughed a lot.", "", 2.094, new double[] { -3.085, -2.011, -0.941, 0.145 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_036R1", "In the past 7 days,", "I was merry.", "", 2.579, new double[] { -2.144, -1.381, -0.401, 0.745 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_037R1", "In the past 7 days,", "I was in a good mood.", "", 2.813, new double[] { -2.724, -1.989, -0.854, 0.552 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_038R1", "In the past 7 days,", "I was in good spirits.", "", 2.879, new double[] { -2.593, -1.718, -0.728, 0.595 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_039R1", "In the past 7 days,", "I felt good.", "", 2.694, new double[] { -2.706, -1.89, -0.903, 0.367 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_040R1", "In the past 7 days,", "I felt blissful.", "", 2.774, new double[] { -2.184, -1.39, -0.265, 0.75 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_042R1", "In the past 7 days,", "I felt enthusiastic.", "", 2.425, new double[] { -2.563, -1.661, -0.582, 0.479 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_044R1", "In the past 7 days,", "I felt energetic.", "", 2.321, new double[] { -2.688, -1.7, -0.639, 0.443 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_045R1", "In the past 7 days,", "I had a lot of energy.", "", 2.203, new double[] { -2.715, -1.704, -0.635, 0.359 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_047R1", "In the past 7 days,", "I felt wide awake.", "", 1.514, new double[] { -2.787, -1.509, -0.146, 1.177 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_049R1", "In the past 7 days,", "I felt refreshed.", "", 2.667, new double[] { -2.199, -1.439, -0.315, 0.748 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_050R1", "In the past 7 days,", "I felt active.", "", 1.882, new double[] { -2.914, -1.914, -0.888, 0.276 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_054R1", "In the past 7 days,", "I felt full of pep.", "", 2.289, new double[] { -2.254, -1.42, -0.392, 0.555 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_055R1", "In the past 7 days,", "I felt strong.", "", 2.144, new double[] { -2.752, -1.859, -0.752, 0.309 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SWB_P_057R1", "In the past 7 days,", "I felt healthy.", "", 1.67, new double[] { -3.297, -2.231, -1.17, 0.119 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
