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
 * Item bank for PROMIS assessment. Generated from OID BA062F28-2288-4FDF-B7B1-F79F71F77072.
 */
public class PromisBankOneZeroSmokingHealthExpectNonDaily {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("SMKHLTH01_nd", "", "Smoking is taking years off my life.", "", 2.64, new double[] { -1.46, -0.49, 0.26, 0.8 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH02_nd", "", "Smoking makes me worry about getting heart troubles.", "", 2.96, new double[] { -0.93, -0.1, 0.52, 1.06 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH03_nd", "", "Smoking causes me to get tired easily.", "", 1.78, new double[] { -0.15, 0.6, 1.44, 2.07 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH04_nd", "", "Smoking makes me short of breath.", "", 1.94, new double[] { -1.01, 0.1, 0.84, 1.51 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH05_nd", "", "Smoking irritates my mouth and throat.", "", 1.58, new double[] { -0.34, 0.85, 1.81, 2.57 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH06_nd", "", "I worry that smoking will lower my quality of life.", "", 3.03, new double[] { -0.89, -0.15, 0.47, 0.96 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH07_nd", "", "Smoking makes me worry about getting emphysema.", "", 2.91, new double[] { -1.01, -0.19, 0.43, 0.94 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH08_nd", "", "Smoking makes my lungs hurt.", "", 1.79, new double[] { 0.1, 0.99, 1.91, 2.53 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH09_nd", "", "Smoking causes damage to my gums and teeth.", "", 1.73, new double[] { -1.5, -0.41, 0.5, 1.18 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH10_nd", "", "Smoking leaves an unpleasant taste in my mouth.", "", 1.35, new double[] { -1.77, -0.38, 0.49, 1.35 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH11_nd", "", "It takes me longer to recover from a cold because I smoke.", "", 1.66, new double[] { -0.41, 0.36, 1.13, 1.82 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH12_nd", "", "If I quit smoking I will feel more energetic.", "", 1.95, new double[] { -1.3, -0.57, 0.44, 1.15 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH13_nd", "", "If I quit smoking I will breathe easier.", "", 2.48, new double[] { -1.9, -1.03, -0.27, 0.35 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH20_nd", "", "Smoking makes me worry about getting high blood pressure.", "", 2.23, new double[] { -0.6, 0.14, 0.8, 1.3 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH21_nd", "", "Smoking makes me feel weaker physically.", "", 2.51, new double[] { -0.5, 0.36, 1.11, 1.95 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH22_nd", "", "Smoking makes it harder for me to exercise or play sports.", "", 2.18, new double[] { -0.88, 0.05, 0.71, 1.3 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH23_nd", "", "Smoking gives me a morning cough.", "", 1.41, new double[] { -0.12, 1.01, 1.88, 2.56 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKHLTH24_nd", "", "Smoking leaves a stain on my fingers.", "", 1.24, new double[] { 0.11, 1.09, 1.88, 2.69 }, -1, "",
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
