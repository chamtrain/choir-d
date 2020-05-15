/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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
 * Item bank for PROMIS anger assessment. Generated from OID D2FA612D-C290-4B88-957D-1C27F48EE58C.
 */
public class PromisAnger {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("EDANG01", "In the past 7 days", "When I was frustrated, I let it show", "", 1.56467, new double[] { -1.1758, 0.1564, 1.8111, 3.2703 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG03", "In the past 7 days", "I was irritated more than people knew", "", 2.34877, new double[] { -0.7617, 0.0609, 1.1574, 2.2002 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG04", "In the past 7 days", "I felt envious of others", "", 1.60879, new double[] { 0.1098, 1.2633, 2.5402, 3.6573 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG05", "In the past 7 days", "I disagreed with people", "", 1.50536, new double[] { -1.8183, -0.4383, 1.8945, 3.8793 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG06", "In the past 7 days", "I made myself angry about something just by thinking about it", "", 2.46166, new double[] { -0.4124, 0.513, 1.6381, 2.641 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG07", "In the past 7 days", "I tried to get even when I was angry with someone", "", 1.83864, new double[] { 0.7587, 1.8931, 2.9716, 3.886 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG09", "In the past 7 days", "I felt angry", "", 2.82932, new double[] { -0.826, 0.3331, 1.6847, 2.9031 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG10", "In the past 7 days", "When I was mad at someone, I gave them the silent treatment", "", 1.41254, new double[] { -0.1939, 0.8158, 2.3073, 3.7056 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG11", "In the past 7 days", "I felt like breaking things", "", 2.36002, new double[] { 0.8479, 1.5205, 2.381, 3.3208 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG15", "In the past 7 days", "I felt like I was ready to explode", "", 2.82094, new double[] { 0.2872, 1.0593, 1.9753, 3.0347 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG16", "In the past 7 days", "When I was angry, I sulked", "", 2.39138, new double[] { 0.0951, 0.9547, 2.0294, 2.9623 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG17", "In the past 7 days", "I felt resentful when I didn't get my way", "", 2.20405, new double[] { 0.1283, 1.1604, 2.4889, 3.4742 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG18", "In the past 7 days", "I felt guilty about my anger", "", 1.96501, new double[] { 0.0306, 0.7934, 1.937, 2.7094 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG21", "In the past 7 days", "I felt bitter about things", "", 2.21749, new double[] { -0.1845, 0.7428, 1.746, 2.7832 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG22", "In the past 7 days", "I felt that people were trying to anger me", "", 2.01767, new double[] { 0.3971, 1.2668, 2.4871, 3.4588 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG25", "In the past 7 days", "I stayed angry for hours", "", 2.85602, new double[] { 0.4051, 1.2625, 2.1345, 3.0706 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG26", "In the past 7 days", "I held grudges towards others", "", 2.16528, new double[] { 0.1328, 1.1658, 2.2326, 3.0545 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG28", "In the past 7 days", "I felt angrier than I thought I should", "", 2.61408, new double[] { -0.044, 0.7433, 1.8099, 2.5623 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG30", "In the past 7 days", "I was grouchy", "", 2.98863, new double[] { -0.8157, 0.2649, 1.5402, 2.8419 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG31", "In the past 7 days", "I was stubborn with others", "", 2.29275, new double[] { -0.5624, 0.5503, 1.9122, 3.108 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG35", "In the past 7 days", "I felt annoyed", "", 2.49943, new double[] { -1.1088, -0.0099, 1.2882, 2.8597 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG37", "In the past 7 days", "I had a bad temper", "", 2.14871, new double[] { -0.0586, 1.0736, 2.2657, 3.5977 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG42", "In the past 7 days", "I had trouble controlling my temper", "", 2.04591, new double[] { 0.6382, 1.6702, 2.8494, 3.8581 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG45", "In the past 7 days", "I was angry when I was delayed", "", 1.4627, new double[] { -0.2918, 0.8466, 2.3539, 3.9454 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG47", "In the past 7 days", "Even after I expressed my anger, I had trouble forgetting about it", "", 2.2493, new double[] { -0.2106, 0.6707, 1.7132, 2.6247 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG48", "In the past 7 days", "I felt like I needed help for my anger", "", 2.78759, new double[] { 0.8603, 1.5301, 2.3736, 2.9411 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG54", "In the past 7 days", "I was angry when something blocked my plans", "", 1.82335, new double[] { -0.4085, 0.6569, 2.1329, 3.175 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG55", "In the past 7 days", "I felt like yelling at someone", "", 2.32713, new double[] { -0.3667, 0.5689, 1.7369, 2.7817 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG56", "In the past 7 days", "Just being around people irritated me", "", 2.31045, new double[] { 0.4458, 1.2561, 1.95, 2.771 }, -1, "",
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
