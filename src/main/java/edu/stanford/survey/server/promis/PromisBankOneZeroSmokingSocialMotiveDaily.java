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
 * Item bank for PROMIS assessment. Generated from OID 238E4813-4513-4A8C-BBB3-A95E8F96E369.
 */
public class PromisBankOneZeroSmokingSocialMotiveDaily {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("SMKSOC01", "", "Smoking makes me feel better in social situations.", "", 2.4249, new double[] { -0.445, 0.2118, 1.0656, 1.7528 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC02", "", "Smoking helps me feel more relaxed when I'm with other people.", "", 2.3511, new double[] { -0.83, -0.0556, 0.8069, 1.6035 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC03", "", "I feel like part of a group when I'm around other smokers.", "", 2.0064, new double[] { -0.9718, -0.11, 0.729, 1.5225 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC04", "", "I enjoy the social aspect of smoking with other smokers.", "", 1.3637, new double[] { -1.9512, -0.6158, 0.5544, 1.5762 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC05", "", "If I quit smoking I will be less welcome around my friends who smoke.", "", 0.902, new double[] { 1.695, 2.4615, 3.7504, 4.6075 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC06", "", "Smoking is a part of my self-image.", "", 1.1483, new double[] { -0.2833, 0.8501, 2.0907, 3.0762 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC07", "", "I feel a bond with other smokers.", "", 1.4486, new double[] { -1.4164, -0.261, 0.7807, 1.7491 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC08", "", "I am tempted to smoke when I am with other people who are smoking.", "", 2.1977, new double[] { -2.7102, -1.9676, -0.6383, 0.4699 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKSOC09", "", "Smoking gives me something to do with my hands.", "", 1.1854, new double[] { -2.0387, -0.7576, 0.3121, 1.3303 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC10", "", "If I always smoke in a certain place it is hard to be there and not smoke.", "", 1.4122, new double[] { -1.6942, -0.9509, 0.1691, 1.194 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC11", "", "If I quit smoking I will feel uncomfortable around smokers.", "", 0.9786, new double[] { -0.6092, 0.3269, 1.4769, 2.4076 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKSOC12", "", "I am tempted to smoke when I see someone enjoying a cigarette.", "", 2.2233, new double[] { -2.2751, -1.2363, 0.1037, 1.1185 }, -1, "",
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
