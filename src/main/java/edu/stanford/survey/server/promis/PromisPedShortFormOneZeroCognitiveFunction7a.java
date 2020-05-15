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
 * Item bank for PROMIS assessment. Generated from OID 9C2E3FDD-1827-451E-B565-6B8B6D5A4017.
 */
public class PromisPedShortFormOneZeroCognitiveFunction7a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("NQCOG44_2C", "In the past 4 weeks,", "It is hard for me to pay attention to one thing for more than 5-10 minutes", "", 3.7189, new double[] { -1.9431, -1.2755, -0.6731, 0.0651 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      ),
      item("NQCOG65_2C", "In the past 4 weeks,", "I have trouble keeping track of what I am doing if I get interrupted", "", 3.7269, new double[] { -1.7668, -1.1757, -0.5172, 0.2895 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      ),
      item("pB10_FC", "In the past 4 weeks,", "I have to read things several times to understand them", "", 3.0189, new double[] { -1.8013, -1.2219, -0.566, 0.3431 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      ),
      item("pB8_FC", "In the past 4 weeks,", "I forget things easily", "", 4.1538, new double[] { -1.8239, -1.2195, -0.6015, 0.1583 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      ),
      item("pedsPCF5_FC", "In the past 4 weeks,", "I have to use written lists more often than other people my age so I will not forget things", "", 3.2677, new double[] { -1.9608, -1.396, -0.8931, -0.2882 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      ),
      item("pedsPCF6_FC", "In the past 4 weeks,", "I have trouble remembering to do things like school projects or chores", "", 3.6468, new double[] { -1.8746, -1.2632, -0.5643, 0.299 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      ),
      item("pedsPCF8_FC", "In the past 4 weeks,", "I have to work really hard to pay attention or I make mistakes", "", 4.3691, new double[] { -1.5632, -1.0343, -0.5447, 0.226 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
