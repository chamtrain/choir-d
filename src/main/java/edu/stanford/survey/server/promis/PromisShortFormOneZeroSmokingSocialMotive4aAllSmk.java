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
 * Item bank for PROMIS assessment. Generated from OID 127EC506-531E-4CD4-9114-5AE801DA8611.
 */
public class PromisShortFormOneZeroSmokingSocialMotive4aAllSmk {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
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
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
