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
 * Item bank for PROMIS assessment. Generated from OID FEEA703A-6BBE-405C-A34F-8FA36A41057E.
 */
public class PromisBankOneZeroSmokingSocialMotiveNonDaily {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("SMKSOC01_nd", "", "Smoking makes me feel better in social situations.", "", 2.4249, new double[] { -0.445, 0.2118, 1.0656, 1.7528 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC02_nd", "", "Smoking helps me feel more relaxed when I'm with other people.", "", 2.3511, new double[] { -0.83, -0.0556, 0.8069, 1.6035 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC03_nd", "", "I feel like part of a group when I'm around other smokers.", "", 2.0064, new double[] { -0.9718, -0.11, 0.729, 1.5225 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC04_nd", "", "I enjoy the social aspect of smoking with other smokers.", "", 1.3637, new double[] { -1.9512, -0.6158, 0.5544, 1.5762 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC05_nd", "", "If I quit smoking I will be less welcome around my friends who smoke.", "", 0.902, new double[] { 1.695, 2.4615, 3.7504, 4.6075 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC06_nd", "", "Smoking is a part of my self-image.", "", 1.1483, new double[] { -0.2833, 0.8501, 2.0907, 3.0762 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC07_nd", "", "I feel a bond with other smokers.", "", 1.4486, new double[] { -1.4164, -0.261, 0.7807, 1.7491 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC08_nd", "", "I am tempted to smoke when I am with other people who are smoking.", "", 1.0392, new double[] { -3.8897, -2.3415, -0.286, 1.4823 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKSOC09_nd", "", "Smoking gives me something to do with my hands.", "", 1.1257, new double[] { -1.4446, -0.1922, 0.7974, 1.8422 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC13_nd", "", "Smoking makes me feel more self-confident with others.", "", 3.0382, new double[] { -0.0389, 0.5989, 1.3017, 1.9719 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC14_nd", "", "Smoking helps me enjoy people more.", "", 3.0387, new double[] { -0.1559, 0.4412, 1.25, 1.8402 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC15_nd", "", "Smoking can be a good excuse to get out of uncomfortable social situations.", "", 1.4762, new double[] { -0.9118, 0.0228, 0.9256, 1.771 }, -1, "",
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
