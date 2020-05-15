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
 * Item bank for PROMIS assessment. Generated from OID C5F5985C-A613-4D11-A5CF-BEFC88528EE9.
 */
public class PromisPedShortFormOneZeroPsychStressExperiences8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("EOS_P_011R1", "In the past 7 days,", "I felt stressed.", "", 2.549, new double[] { -0.307, 0.56, 1.468, 2.254 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_048R1", "In the past 7 days,", "everything bothered me.", "", 2.419, new double[] { 0.332, 1.118, 1.955, 2.826 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_063R1", "In the past 7 days,", "I felt under pressure.", "", 2.661, new double[] { 0.137, 0.899, 1.718, 2.467 }, -1, "",
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
      item("EOS_P_105R1", "In the past 7 days,", "I had trouble concentrating.", "", 2.301, new double[] { -0.181, 0.619, 1.673, 2.463 }, -1, "",
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
      ),
      item("EOS_P_118R1", "In the past 7 days,", "I felt I had too much going on.", "", 2.373, new double[] { -0.179, 0.591, 1.557, 2.361 }, -1, "",
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
