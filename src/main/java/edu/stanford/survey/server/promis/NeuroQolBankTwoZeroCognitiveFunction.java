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
 * Item bank for PROMIS assessment. Generated from OID A8FD52D9-45C8-4FFE-8CF7-EFDE76B1DC6A.
 */
public class NeuroQolBankTwoZeroCognitiveFunction {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQCOG15r1", "How much DIFFICULTY do you currently have...", "keeping track of time (eg., using a clock)?", "", 1.47682, new double[] { -3.5042, -2.2687, -1.3111, -0.3711 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG16r1", "How much DIFFICULTY do you currently have...", "checking the accuracy of financial documents, (e,g., bills, checkbook, or bank statements)?", "", 1.7684, new double[] { -3.2158, -1.8714, -1.0939, -0.1969 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG22r1", "How much DIFFICULTY do you currently have...", "reading and following complex instructions  (e.g., directions for a new medication)?", "", 1.99413, new double[] { -2.7796, -1.9347, -1.0525, -0.18 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG24r1", "How much DIFFICULTY do you currently have...", "Planning for and keeping appointments that are not part of your weekly routine, (e.g., a therapy or doctor appointment, or a social gathering with friends and family)?", "", 2.0036, new double[] { -3.0204, -1.8757, -0.9692, -0.1291 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG25r1", "How much DIFFICULTY do you currently have...", "managing your time to do most of your daily activities?", "", 1.90767, new double[] { -2.9387, -1.8623, -0.8527, 0.2229 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG26r1", "How much DIFFICULTY do you currently have...", "planning an activity several days in advance (e.g., a meal, trip, or visit to friends)?", "", 2.02141, new double[] { -3.0628, -1.8299, -0.9105, -0.1812 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG31r1", "How much DIFFICULTY do you currently have...", "getting things organized?", "", 1.78896, new double[] { -2.865, -1.6761, -0.8127, 0.2993 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG38r1", "How much DIFFICULTY do you currently have...", "remembering where things were placed or put away (e.g., keys)?", "", 1.86625, new double[] { -3.0364, -1.8178, -0.8332, 0.4463 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG39r1", "How much DIFFICULTY do you currently have...", "remembering a list of 4 or 5 errands without writing it down?", "", 1.75433, new double[] { -2.5668, -1.5049, -0.6076, 0.6798 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG40r1", "How much DIFFICULTY do you currently have...", "learning new tasks or instructions?", "", 2.27017, new double[] { -2.7483, -1.802, -0.8962, 0.0953 }, -1, "",
          response("None", 5),
          response("A little", 4),
          response("Somewhat", 3),
          response("A lot", 2),
          response("Cannot Do", 1)
      ),
      item("NQCOG46r1", "In the past 7 days", "I made simple mistakes more easily", "", 2.5146, new double[] { -2.2788, -1.7424, -0.8698, 0.2918 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG53r1", "In the past 7 days", "Words I wanted to use  seemed to be on the \"tip of my tongue\"", "", 1.89768, new double[] { -2.3811, -1.5584, -0.4516, 0.8557 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG64r1", "In the past 7 days", "I had to read something several times to understand it", "", 2.28352, new double[] { -2.3584, -1.6053, -0.5585, 0.5287 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG65r1", "In the past 7 days", "I had trouble keeping track of what I was doing if I was interrupted", "", 3.25336, new double[] { -2.1387, -1.3852, -0.6261, 0.3412 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG66r1", "In the past 7 days", "I had difficulty doing more than one thing at a time", "", 3.15804, new double[] { -1.9812, -1.4575, -0.617, 0.1831 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG67r1", "In the past 7 days", "I had trouble remembering whether I did things I was supposed to do, like taking a medicine or buying something I needed", "", 2.3544, new double[] { -2.2475, -1.5343, -0.7285, 0.1054 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG68r1", "In the past 7 days", "I had trouble remembering new information, like phone numbers or simple instructions", "", 2.5915, new double[] { -2.0912, -1.4269, -0.7303, 0.1036 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG69r1", "In the past 7 days", "I walked into a room and forgot what I meant to get or do there", "", 1.6663, new double[] { -2.6085, -1.6206, -0.534, 0.795 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG70r1", "In the past 7 days", "I had trouble remembering the name of a familiar person", "", 2.52673, new double[] { -2.4482, -1.6404, -0.8509, 0.0061 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG72r1", "In the past 7 days", "I had trouble thinking clearly", "", 3.73568, new double[] { -1.9485, -1.41, -0.763, 0.053 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG73r1", "In the past 7 days", "I reacted slowly to things that were said or done", "", 3.93185, new double[] { -1.9529, -1.4097, -0.7942, -0.0714 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG74r1", "In the past 7 days", "I had trouble forming thoughts", "", 3.1023, new double[] { -1.8914, -1.4055, -0.81, -0.1057 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG75r1", "In the past 7 days", "my thinking was slow", "", 3.2253, new double[] { -1.8633, -1.37, -0.7535, -0.0608 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG77r1", "In the past 7 days", "I had to work really hard to pay attention or I would make a mistake", "", 3.0151, new double[] { -1.9623, -1.358, -0.7005, 0.007 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG80r1", "In the past 7 days", "I had trouble concentrating", "", 3.32175, new double[] { -1.8978, -1.4142, -0.6082, 0.204 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG83r1", "In the past 7 days", "I had trouble getting started on very  simple tasks", "", 3.47148, new double[] { -1.9291, -1.4014, -0.746, -0.0346 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG84r1", "In the past 7 days", "I had trouble making decisions", "", 3.1843, new double[] { -1.9619, -1.3612, -0.6953, 0.0301 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      ),
      item("NQCOG86r1", "In the past 7 days", "I had trouble planning out steps of a task", "", 3.7266, new double[] { -2.036, -1.453, -0.8257, -0.117 }, -1, "",
          response("Never", 5),
          response("Rarely (once)", 4),
          response("Sometimes (2-3 times)", 3),
          response("Often (once a day)", 2),
          response("Very Often (several times a day)", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
