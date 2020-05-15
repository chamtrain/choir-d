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
 * Item bank for PROMIS assessment. Generated from OID 2177E9AC-DBAB-408B-835E-AF27EA15CA4F.
 */
public class PromisBankOneZeroSmokingEmotSensoryDaily {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("SMKEMSEN01", "", "I feel better after smoking a cigarette.", "", 2.37, new double[] { -1.76, -0.62, 0.33, 1.24 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN02", "", "Smoking stimulates me.", "", 1.92, new double[] { -1.32, -0.27, 0.81, 1.74 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN03", "", "Smoking helps me concentrate.", "", 1.93, new double[] { -0.79, -0.01, 0.93, 1.8 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN04", "", "When I stop what I'm doing to have a cigarette it feels like 'my time'.", "", 1.66, new double[] { -1.62, -0.7, 0.2, 1.06 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN05", "", "I love the feel of inhaling the smoke into my mouth.", "", 2.11, new double[] { -1.58, -0.48, 0.4, 1.35 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN06", "", "Smoking is relaxing.", "", 2.06, new double[] { -2.86, -1.48, -0.48, 0.56 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN07", "", "Smoking makes me feel content.", "", 2.31, new double[] { -1.5, -0.51, 0.49, 1.36 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN08", "", "Even when I feel good, smoking helps me feel better.", "", 2.35, new double[] { -1.44, -0.59, 0.75, 1.72 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("SMKEMSEN09", "", "Smoking is the fastest way to reward myself.", "", 1.92, new double[] { -0.75, 0.0, 0.87, 1.61 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN10", "", "Smoking makes me less depressed.", "", 1.69, new double[] { -0.7, 0.0, 1.05, 1.87 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN11", "", "I smoke because it is self-satisfying.", "", 1.78, new double[] { -2.32, -1.12, -0.09, 0.93 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN12", "", "I like the way a cigarette makes me feel physically.", "", 1.76, new double[] { -0.98, -0.14, 0.91, 1.82 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN13", "", "I smoke because smoking feels good.", "", 2.6, new double[] { -1.58, -0.64, 0.22, 1.07 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN14", "", "I enjoy the sensations of a long, slow exhalation of smoke.", "", 1.85, new double[] { -1.39, -0.45, 0.52, 1.46 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN15", "", "I smoke to get a sense of pleasure.", "", 2.51, new double[] { -1.75, -0.81, 0.12, 1.01 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SMKEMSEN16", "", "I enjoy the steps I take to light up a cigarette.", "", 1.42, new double[] { -0.76, 0.28, 1.38, 2.22 }, -1, "",
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
