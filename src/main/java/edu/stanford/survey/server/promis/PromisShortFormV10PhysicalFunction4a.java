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
 * Item bank for PROMIS assessment. Generated from OID C81F5920-D9DB-48F7-A0AD-A6F2A4BF6D36.
 */
public class PromisShortFormV10PhysicalFunction4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("PFA11", "", "Are you able to do chores such as vacuuming or yard work?", "", 4.83487, new double[] { -1.9837, -1.5298, -1.1056, -0.4545 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA21", "", "Are you able to go up and down stairs at a normal pace?", "", 4.23685, new double[] { -1.885, -1.4805, -1.0464, -0.3953 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA23", "", "Are you able to go for a walk of at least 15 minutes?", "", 4.28753, new double[] { -1.9146, -1.5792, -1.1944, -0.6814 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      ),
      item("PFA53", "", "Are you able to run errands and shop?", "", 4.43957, new double[] { -2.546, -1.9738, -1.4509, -0.8195 }, -1, "",
          response("Without any difficulty", 5),
          response("With a little difficulty", 4),
          response("With some difficulty", 3),
          response("With much difficulty", 2),
          response("Unable to do", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
