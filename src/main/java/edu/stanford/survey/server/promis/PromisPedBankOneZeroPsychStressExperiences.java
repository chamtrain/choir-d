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
 * Item bank for PROMIS assessment. Generated from OID FBDBC63F-244D-472C-BB2B-983DBAE00DC9.
 */
public class PromisPedBankOneZeroPsychStressExperiences {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
      item("EOS_P_004R1", "In the past 7 days,", "I felt concerned about what was going on in my life.", "", 2.15, new double[] { -0.001, 0.783, 1.652, 2.392 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_011R1", "In the past 7 days,", "I felt stressed.", "", 2.549, new double[] { -0.307, 0.56, 1.468, 2.254 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_047R1", "In the past 7 days,", "small things upset me.", "", 2.135, new double[] { -0.018, 0.888, 1.858, 2.643 }, -1, "",
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
      item("EOS_P_090R1", "In the past 7 days,", "I forgot things.", "", 1.992, new double[] { -0.492, 0.519, 1.654, 2.466 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_095R1", "In the past 7 days,", "I felt like my thinking was slower than usual.", "", 2.533, new double[] { 0.508, 1.263, 2.146, 2.77 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_097R1", "In the past 7 days,", "I felt unable to remember answers, even for questions I knew the answer to.", "", 2.115, new double[] { 0.254, 1.04, 1.907, 2.858 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_099R1", "In the past 7 days,", "I felt so upset that I could not remember what happened or what I did.", "", 2.655, new double[] { 0.826, 1.384, 2.062, 2.781 }, -1, "",
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
      item("EOS_P_106R1", "In the past 7 days,", "I had trouble making decisions.", "", 2.488, new double[] { 0.039, 0.897, 1.826, 2.539 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_107R1", "In the past 7 days,", "I had trouble controlling my thoughts.", "", 2.095, new double[] { 0.365, 1.232, 2.14, 2.942 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_108R1", "In the past 7 days,", "my thoughts went very fast.", "", 2.099, new double[] { -0.026, 0.767, 1.717, 2.561 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_109R1", "In the past 7 days,", "I was slow to react to things.", "", 2.476, new double[] { 0.635, 1.479, 2.268, 2.93 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EOS_P_111R1", "In the past 7 days,", "I felt unable to react to something that bothered me.", "", 2.44, new double[] { 0.491, 1.226, 2.04, 2.83 }, -1, "",
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
