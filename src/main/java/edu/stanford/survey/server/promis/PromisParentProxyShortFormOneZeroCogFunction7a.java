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
 * Item bank for PROMIS assessment. Generated from OID C3E65FA1-73B6-450F-85E5-0C43AD06A158.
 */
public class PromisParentProxyShortFormOneZeroCogFunction7a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("NQCOG44_2Ar", "In the past 4 weeks,", "It is hard for your child to pay attention to one thing for more than 5-10 minutes", "", 3.13796, new double[] { -1.9803, -1.2534, -0.6802, 0.1538 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      ),
      item("NQCOG65_2Ar", "In the past 4 weeks,", "Your child has trouble keeping track of what he/she is doing if he/she gets interrupted", "", 3.39081, new double[] { -2.012, -1.2737, -0.6192, 0.2345 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      ),
      item("pB10_FA", "In the past 4 weeks,", "Your child has to read things several times to understand them", "", 3.16792, new double[] { -2.0823, -1.3162, -0.6672, 0.2003 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      ),
      item("pB8_FA", "In the past 4 weeks,", "Your child forgets things easily", "", 3.76, new double[] { -1.9554, -1.2813, -0.6415, 0.1909 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      ),
      item("pedsPCF5_FA", "In the past 4 weeks,", "Your child has to use written lists more often than other people his/her age so he/she will not forget things", "", 3.2558, new double[] { -2.2343, -1.524, -0.9912, -0.3259 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      ),
      item("pedsPCF6_FA", "In the past 4 weeks,", "Your child has trouble remembering to do things like school projects or chores", "", 3.56157, new double[] { -1.9784, -1.2707, -0.5837, 0.3036 }, -1, "",
          response("None of the time", 5),
          response("A little of the time", 4),
          response("Some of the time", 3),
          response("Most of the time", 2),
          response("All of the time", 1)
      ),
      item("pedsPCF8_FA", "In the past 4 weeks,", "Your child has to work really hard to pay attention or he/she makes mistakes", "", 4.34684, new double[] { -1.8257, -1.0978, -0.5915, 0.1802 }, -1, "",
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
