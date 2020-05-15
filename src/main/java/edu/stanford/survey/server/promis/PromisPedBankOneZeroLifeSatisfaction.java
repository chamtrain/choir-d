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
 * Item bank for PROMIS assessment. Generated from OID C2CF48D7-3939-4D56-BF74-4D243037269F.
 */
public class PromisPedBankOneZeroLifeSatisfaction {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
      item("SWB_LS_002R1", "Thinking about the past 4 weeks,", "My life was ideal.", "", 3.331, new double[] { -2.065, -1.421, -0.642, 0.198 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_003R1", "Thinking about the past 4 weeks,", "My life was the best.", "", 3.708, new double[] { -1.965, -1.376, -0.639, 0.297 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_004R1", "Thinking about the past 4 weeks,", "My life was outstanding.", "", 3.833, new double[] { -1.824, -1.288, -0.604, 0.209 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_005R1", "Thinking about the past 4 weeks,", "My life was excellent.", "", 3.284, new double[] { -2.207, -1.49, -0.793, 0.111 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_006R1", "Thinking about the past 4 weeks,", "My life was great.", "", 5.344, new double[] { -2.024, -1.447, -0.803, -0.037 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_008R1", "Thinking about the past 4 weeks,", "My life was good.", "", 4.636, new double[] { -2.315, -1.755, -1.028, -0.131 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_009R1", "Thinking about the past 4 weeks,", "My life was going very well.", "", 5.438, new double[] { -2.211, -1.542, -0.947, -0.061 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_011R1", "Thinking about the past 4 weeks,", "My life was just right.", "", 3.351, new double[] { -2.009, -1.491, -0.776, 0.11 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_015R1", "Thinking about the past 4 weeks,", "The conditions of my life were excellent.", "", 4.005, new double[] { -1.938, -1.438, -0.745, 0.085 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_016R1", "Thinking about the past 4 weeks,", "My life situation was excellent.", "", 2.7, new double[] { -2.519, -1.756, -0.853, 0.199 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_018R1", "Thinking about the past 4 weeks,", "I was happy with the way things were.", "", 3.285, new double[] { -2.227, -1.536, -0.862, 0.119 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_019R1", "Thinking about the past 4 weeks,", "I had what I wanted in life.", "", 2.521, new double[] { -2.273, -1.491, -0.613, 0.451 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_020R1", "Thinking about the past 4 weeks,", "I had what I needed in life.", "", 2.045, new double[] { -2.917, -2.035, -1.124, -0.034 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_022R1", "Thinking about the past 4 weeks,", "I got the things I wanted in life.", "", 2.291, new double[] { -2.507, -1.592, -0.681, 0.464 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_028R1", "Thinking about the past 4 weeks,", "My life was better than most kids' lives.", "", 1.878, new double[] { -2.295, -1.439, -0.504, 0.559 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_030R1", "Thinking about the past 4 weeks,", "I enjoyed my life more than most kids enjoyed their lives.", "", 2.264, new double[] { -2.353, -1.569, -0.658, 0.349 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_031R1", "Thinking about the past 4 weeks,", "I lived as well as other kids.", "", 2.756, new double[] { -2.39, -1.749, -0.92, 0.103 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_032R1", "Thinking about the past 4 weeks,", "My life was as good as most kids' lives.", "", 1.977, new double[] { -2.555, -1.8, -0.917, 0.167 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_036R1", "Thinking about the past 4 weeks,", "I was satisfied with the friends I have.", "", 1.337, new double[] { -3.507, -2.489, -1.595, -0.371 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_037R1", "Thinking about the past 4 weeks,", "I was happy with my social life.", "", 1.848, new double[] { -2.786, -1.958, -1.059, 0.058 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_038R1", "Thinking about the past 4 weeks,", "I was happy with my family life.", "", 2.973, new double[] { -2.343, -1.684, -1.101, -0.281 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_039R1", "Thinking about the past 4 weeks,", "I was happy with my life at school.", "", 1.98, new double[] { -2.458, -1.61, -0.844, 0.184 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_040R1", "Thinking about the past 4 weeks,", "I was happy with my life at home.", "", 2.896, new double[] { -2.416, -1.714, -1.033, -0.168 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_041R1", "Thinking about the past 4 weeks,", "I was happy with my life in my neighborhood.", "", 1.73, new double[] { -2.819, -1.955, -0.972, 0.026 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_042R1", "Thinking about the past 4 weeks,", "I was happy with my life in my community.", "", 2.564, new double[] { -2.604, -1.753, -0.868, 0.113 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_043R1", "Thinking about the past 4 weeks,", "I was satified with my free time.", "", 2.022, new double[] { -2.504, -1.846, -1.061, -0.087 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_044R1", "Thinking about the past 4 weeks,", "I was satisfied with my skills and talents.", "", 1.875, new double[] { -2.824, -1.931, -1.077, -0.086 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_046R1", "Thinking about the past 4 weeks,", "I was satisfied with my life.", "", 3.872, new double[] { -2.387, -1.798, -1.026, -0.108 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_047R1", "Thinking about the past 4 weeks,", "I felt extremely positive about my life.", "", 3.782, new double[] { -2.072, -1.411, -0.721, 0.098 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_048R1", "Thinking about the past 4 weeks,", "I was happy with my life.", "", 5.342, new double[] { -2.271, -1.653, -1.025, -0.214 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_049R1", "Thinking about the past 4 weeks,", "I felt very good about my life.", "", 5.465, new double[] { -2.287, -1.624, -0.949, -0.128 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_050R1", "Thinking about the past 4 weeks,", "I felt good about my life.", "", 4.135, new double[] { -2.473, -1.635, -0.98, -0.081 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_051R1", "Thinking about the past 4 weeks,", "I had a good life.", "", 4.911, new double[] { -2.484, -1.795, -1.107, -0.236 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_052R1", "Thinking about the past 4 weeks,", "I felt positive about my life.", "", 4.103, new double[] { -2.374, -1.65, -0.97, -0.051 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_053R1", "Thinking about the past 4 weeks,", "I had fun.", "", 3.286, new double[] { -2.62, -1.897, -1.204, -0.313 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_054R1", "Thinking about the past 4 weeks,", "I had a lot of fun.", "", 2.959, new double[] { -2.598, -1.896, -1.168, -0.268 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_055R1", "Thinking about the past 4 weeks,", "I enjoyed my life.", "", 4.986, new double[] { -2.325, -1.673, -1.072, -0.248 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_056R1", "Thinking about the past 4 weeks,", "I liked the way I lived my life.", "", 3.438, new double[] { -2.438, -1.745, -0.982, -0.034 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_057R1", "Thinking about the past 4 weeks,", "My life was worthwhile.", "", 2.672, new double[] { -2.645, -1.862, -1.144, -0.219 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_059R1", "Thinking about the past 4 weeks,", "My life went well.", "", 4.878, new double[] { -2.433, -1.751, -0.996, -0.097 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_064R1", "Thinking about the past 4 weeks,", "I lived my life well.", "", 2.827, new double[] { -2.681, -1.966, -1.021, -0.031 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_LS_065R1", "Thinking about the past 4 weeks,", "I was satisfied with my life in general.", "", 3.858, new double[] { -2.31, -1.73, -1.048, -0.204 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
