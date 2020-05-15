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
 * Item bank for PROMIS assessment. Generated from OID 204126E0-3B76-4224-AF2E-E74AB6457477.
 */
public class PromisPedShortFormOneZeroPsychStressExperiences4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("EOS_P_011R1", "In the past 7 days,", "I felt stressed.", "", 2.549, new double[] { -0.307, 0.56, 1.468, 2.254 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_064R1", "In the past 7 days,", "I felt that my problems kept piling up.", "", 3.198, new double[] { 0.226, 0.957, 1.656, 2.28 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_067R1", "In the past 7 days,", "I felt overwhelmed.", "", 2.991, new double[] { 0.098, 0.852, 1.634, 2.35 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_112R1", "In the past 7 days,", "I felt unable to manage things in my life.", "", 3.316, new double[] { 0.38, 1.13, 1.918, 2.482 }, -1, "",
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
