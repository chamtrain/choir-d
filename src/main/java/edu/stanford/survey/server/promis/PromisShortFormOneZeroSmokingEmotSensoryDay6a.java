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
 * Item bank for PROMIS assessment. Generated from OID E4974A9E-9E68-4A07-B4CE-2FA8EB3F59EF.
 */
public class PromisShortFormOneZeroSmokingEmotSensoryDay6a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
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
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
