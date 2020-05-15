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
 * Item bank for PROMIS assessment. Generated from OID EFDEC04D-E708-4916-9F8B-0CA1B2E14467.
 */
public class PromisBankTwoZeroCognitiveFunction {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("PC-CaPS25r", "In the past 7 days,", "I have had difficulty multi-tasking", "", 2.7386, new double[] { -1.9268, -1.437, -0.6558, 0.1434 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC10r", "In the past 7 days,", "I have had trouble finding my way to a familiar place", "", 2.3124, new double[] { -2.4696, -1.7177, -1.0423, -0.3694 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC11r", "In the past 7 days,", "I have had trouble remembering where I put things, like my keys or my wallet", "", 1.7435, new double[] { -2.4635, -1.7478, -0.6616, 0.4924 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC12r", "In the past 7 days,", "I have had trouble remembering whether I did things I was supposed to do, like taking a medicine or buying something I needed", "", 2.3544, new double[] { -2.2475, -1.5343, -0.7285, 0.1054 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC13r", "In the past 7 days,", "I have had trouble remembering new information, like phone numbers or simple instructions", "", 2.5915, new double[] { -2.0912, -1.4269, -0.7303, 0.1036 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC14r", "In the past 7 days,", "I have had trouble recalling the name of an object while talking to someone", "", 1.855, new double[] { -2.4112, -1.6559, -0.705, 0.3183 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC18r", "In the past 7 days,", "I have had trouble speaking fluently", "", 2.3508, new double[] { -2.3204, -1.6299, -0.8442, -0.1436 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC1r", "In the past 7 days,", "I have had trouble forming thoughts", "", 3.1023, new double[] { -1.8914, -1.4055, -0.81, -0.1057 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC21r", "In the past 7 days,", "I have walked into a room and forgotten what I meant to get or do there", "", 1.6663, new double[] { -2.6085, -1.6206, -0.534, 0.795 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC22r", "In the past 7 days,", "I have needed medical instructions repeated because I could not keep them straight", "", 2.7118, new double[] { -2.315, -1.6719, -0.9843, -0.4034 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC25r", "In the past 7 days,", "I have had to work really hard to pay attention or I would make a mistake", "", 3.0151, new double[] { -1.9623, -1.358, -0.7005, 0.007 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC26r", "In the past 7 days,", "I have forgotten names of people soon after being introduced", "", 1.3583, new double[] { -2.7042, -1.6718, -0.4905, 0.643 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC28r", "In the past 7 days,", "My reactions in everyday situations have been slow", "", 2.8515, new double[] { -2.0641, -1.4814, -0.7782, -5.0E-4 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC2r", "In the past 7 days,", "My thinking has been slow", "", 3.2253, new double[] { -1.8633, -1.37, -0.7535, -0.0608 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC30r", "In the past 7 days,", "Other people have told me I seemed to have trouble remembering information", "", 2.85, new double[] { -2.0578, -1.4673, -0.8521, -0.2979 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC35r", "In the past 7 days,", "It has seemed like my brain was not working as well as usual", "", 2.6891, new double[] { -1.969, -1.4102, -0.6554, 0.1612 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC36r", "In the past 7 days,", "I have had to work harder than usual to keep track of what I was doing", "", 3.4153, new double[] { -1.7812, -1.3247, -0.7109, 0.0305 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC37r", "In the past 7 days,", "My thinking has been slower than usual", "", 3.2345, new double[] { -1.8562, -1.3234, -0.6833, 0.0434 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC38r", "In the past 7 days,", "I have had to work harder than usual to express myself clearly", "", 3.3824, new double[] { -1.8596, -1.3172, -0.6936, -0.0738 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC39r", "In the past 7 days,", "I have had more problems conversing with others", "", 3.349, new double[] { -1.8573, -1.3387, -0.776, -0.1457 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC3r", "In the past 7 days,", "My thinking has been foggy", "", 1.768, new double[] { -2.7438, -1.8129, -0.7006, 0.5871 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC40r", "In the past 7 days,", "I have had to use written lists more often than usual so I would not forget things", "", 2.0674, new double[] { -2.1889, -1.3924, -0.6068, 0.2658 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC41r", "In the past 7 days,", "I have had trouble keeping track of what I was doing when interrupted", "", 2.5824, new double[] { -2.0827, -1.3762, -0.6259, 0.296 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC42r", "In the past 7 days,", "I have had trouble shifting back and forth between different activities that require thinking", "", 2.88, new double[] { -2.0648, -1.4349, -0.6712, 0.0503 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC48r", "In the past 7 days,", "I have hidden my problems with memory, concentration, or making mental mistakes so that others would not notice", "", 3.0558, new double[] { -1.8402, -1.315, -0.8004, -0.2229 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC49r", "In the past 7 days,", "I have been upset about my problems with memory, concentration, or making mental mistakes", "", 3.0478, new double[] { -1.7101, -1.2188, -0.6878, -0.0353 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC50r", "In the past 7 days,", "My problems with memory, concentration, or making mental mistakes have interfered with my ability to work", "", 3.3478, new double[] { -1.805, -1.3392, -0.8115, -0.2406 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC51r", "In the past 7 days,", "My problems with memory, concentration, or making mental mistakes have interfered with my ability to do things I enjoy", "", 3.8222, new double[] { -1.7461, -1.2762, -0.7901, -0.2387 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC53r", "In the past 7 days,", "My problems with memory, concentration, or making mental mistakes have interfered with the quality of my life", "", 3.5524, new double[] { -1.7328, -1.2975, -0.7838, -0.2568 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC5r", "In the past 7 days,", "I have had trouble adding or subtracting numbers in my head", "", 2.0232, new double[] { -2.442, -1.5617, -0.8013, 0.1024 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC7r", "In the past 7 days,", "I have made mistakes when writing down phone numbers", "", 2.0592, new double[] { -2.4589, -1.6761, -0.9013, -0.0023 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      ),
      item("PC8r", "In the past 7 days,", "I have had trouble concentrating", "", 2.1855, new double[] { -2.126, -1.4102, -0.4442, 0.6139 }, -1, "",
          response("Never", 5),
          response("Rarely (Once)", 4),
          response("Sometimes (Two or three times)", 3),
          response("Often (About once a day)", 2),
          response("Very often (Several times a day)", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
