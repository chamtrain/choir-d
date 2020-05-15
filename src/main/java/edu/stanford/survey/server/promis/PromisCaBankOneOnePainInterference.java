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
 * Item bank for PROMIS assessment. Generated from OID 6CAD2A91-2182-408F-BAF8-1F1F10F9D6BD.
 */
public class PromisCaBankOneOnePainInterference {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("PAININ1", "In the past 7 days", "How difficult was it for you to take in new information because of pain?", "", 3.33832, new double[] { 0.8411, 1.3992, 2.0218, 2.6982 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ10", "In the past 7 days", "How much did pain interfere with your enjoyment of recreational activities?", "", 5.15317, new double[] { 0.131, 0.7914, 1.26, 1.8452 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ11", "In the past 7 days", "How often did you feel emotionally tense because of your pain?", "", 3.73643, new double[] { 0.3303, 1.016, 1.4928, 2.1544 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ12", "In the past 7 days", "How much did pain interfere with the things you usually do for fun?", "", 5.29892, new double[] { 0.1808, 0.8281, 1.2865, 1.8824 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ13", "In the past 7 days", "How much did pain interfere with your family life?", "", 5.03924, new double[] { 0.4688, 1.0753, 1.6277, 2.1736 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ14", "In the past 7 days", "How much did pain interfere with doing your tasks away from home (e.g., getting groceries, running errands)?", "", 4.83004, new double[] { 0.4265, 0.9994, 1.4568, 2.044 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ15", "In the past 7 days", "How much did pain interfere with your ability to pay attention?", "", 3.54294, new double[] { 0.2887, 0.9985, 1.521, 2.2468 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ16", "In the past 7 days", "How often did pain make you feel depressed?", "", 3.18212, new double[] { 0.4199, 1.014, 1.7072, 2.2754 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("PAININ17", "In the past 7 days", "How much did pain interfere with your relationships with other people?", "", 4.69522, new double[] { 0.5776, 1.19, 1.706, 2.3169 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ18", "In the past 7 days", "How much did pain interfere with your ability to work (include work at home)?", "", 4.61543, new double[] { 0.2501, 0.9002, 1.3996, 1.8769 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ20", "In the past 7 days", "How much did pain feel like a burden to you?", "", 4.28098, new double[] { 0.1079, 0.7623, 1.1885, 1.7206 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ22", "In the past 7 days", "how much did pain interfere with work around the home?", "", 5.3971, new double[] { 0.1709, 0.8366, 1.3266, 1.9589 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ24", "In the past 7 days", "How often was pain distressing to you?", "", 3.60354, new double[] { -0.0104, 0.6152, 1.3206, 2.0766 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("PAININ26", "In the past 7 days", "How often did pain keep you from socializing with others?", "", 4.87755, new double[] { 0.578, 1.085, 1.676, 2.5191 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("PAININ29", "In the past 7 days", "How often was your pain so severe you could think of nothing else?", "", 3.4297, new double[] { 0.637, 1.1396, 1.8218, 2.9205 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("PAININ3", "In the past 7 days", "How much did pain interfere with your enjoyment of life?", "", 4.98134, new double[] { 0.1293, 0.8809, 1.3825, 1.9123 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ30", "In the past 7 days", "How often did pain make it hard for you to walk more than 5 minutes at a time?", "", 2.62102, new double[] { 0.3206, 0.8538, 1.4087, 1.9419 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("PAININ31", "In the past 7 days", "How much did pain interfere with your ability to participate in social activities?", "", 5.90514, new double[] { 0.473, 1.0094, 1.5063, 2.077 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ32", "In the past 7 days", "How often did pain make you feel discouraged?", "", 3.49408, new double[] { 0.1801, 0.7601, 1.4697, 2.2029 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("PAININ34", "In the past 7 days", "How much did pain interfere with your household chores?", "", 4.92619, new double[] { 0.1771, 0.8447, 1.363, 1.9795 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ35", "In the past 7 days", "How much did pain interfere with your ability to make trips from home that kept you gone for more than 2 hours?", "", 4.14223, new double[] { 0.7493, 1.1849, 1.6303, 2.1296 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ36", "In the past 7 days", "How much did pain interfere with your enjoyment of social activities?", "", 5.69697, new double[] { 0.3417, 0.959, 1.4786, 2.036 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ37", "In the past 7 days", "How often did pain make you feel anxious?", "", 2.85286, new double[] { 0.3791, 1.0382, 1.7622, 2.5517 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("PAININ4", "In the past 7 days", "How much did you worry about pain?", "", 2.40561, new double[] { -0.2654, 0.7137, 1.3421, 2.2073 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ46", "In the past 7 days", "How often did pain make it difficult for you to plan social activities?", "", 4.59, new double[] { 0.452, 0.9384, 1.5214, 2.1187 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("PAININ48", "In the past 7 days", "How much did pain interfere with your ability to do household chores?", "", 4.79892, new double[] { 0.2082, 0.8049, 1.3437, 1.9535 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ49", "In the past 7 days", "How much did pain interfere with your ability to remember things?", "", 3.07411, new double[] { 0.8847, 1.4167, 1.9753, 2.6075 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ5", "In the past 7 days", "How much did pain interfere with your ability to participate in leisure activities?", "", 5.05151, new double[] { 0.2379, 0.906, 1.445, 2.0389 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ51", "In the past 7 days", "How often did pain prevent you from sitting for more than 10 minutes?", "", 2.52538, new double[] { 1.0408, 1.6646, 2.3444, 3.113 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("PAININ52", "In the past 7 days", "How often was it hard to plan social activities because you didn't know if you would be in pain?", "", 4.65444, new double[] { 0.6511, 1.073, 1.5559, 2.0063 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("PAININ53", "In the past 7 days", "How often did pain restrict your social life to your home?", "", 4.13798, new double[] { 0.5115, 0.999, 1.5786, 2.3259 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("PAININ56", "In the past 7 days", "How irritable did you feel because of pain?", "", 3.08298, new double[] { 0.0573, 0.9293, 1.588, 2.2141 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ6", "In the past 7 days", "How much did pain interfere with your close personal relationships?", "", 4.05753, new double[] { 0.6262, 1.1746, 1.6664, 2.1984 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ8", "In the past 7 days", "How much did pain interfere with your ability to concentrate?", "", 3.74877, new double[] { 0.3996, 1.1057, 1.6892, 2.3425 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAININ9", "In the past 7 days", "How much did pain interfere with your day to day activities?", "", 6.53406, new double[] { 0.1579, 0.8959, 1.4377, 2.0103 }, -1, "",
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
