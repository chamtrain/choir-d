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
 * Item bank for PROMIS assessment. Generated from OID 9790B662-FEB1-4BD1-9E09-6193D8EDA76A.
 */
public class PromisParentProxyShortFormOneZeroAsthma8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("Pdisabasth10", "In the past 7 days", "My child felt scared that he/she might have trouble breathing because of asthma.", "", 2.12, new double[] { -0.32, 0.51, 1.56, 2.21 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Ppaqlqasth1", "In the past 7 days", "It was hard for my child to play sports or exercise because of asthma.", "", 2.51, new double[] { -0.48, 0.28, 1.42, 2.28 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Ppaqlqasth3", "In the past 7 days", "My child had trouble sleeping at night because of asthma.", "", 3.15, new double[] { -0.14, 0.37, 1.48, 2.35 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Ppedsa2asth1", "In the past 7 days", "My child's chest felt tight because of asthma.", "", 3.34, new double[] { -0.56, -0.03, 1.23, 1.85 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Ppedsa2asth2", "In the past 7 days", "My child felt wheezy because of his/her asthma.", "", 4.39, new double[] { -0.41, 0.28, 1.35, 2.0 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Ppedsa2asth7", "In the past 7 days", "It was hard for my child to take a deep breath because of asthma.", "", 3.83, new double[] { -0.52, 0.09, 1.43, 2.27 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Ppedsdiaryasth4", "In the past 7 days", "My child had trouble breathing because of his/her asthma.", "", 4.47, new double[] { -0.58, 0.13, 1.29, 1.96 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Ppedsdiaryasth5", "In the past 7 days", "My child's asthma bothered him/her.", "", 5.32, new double[] { -0.55, 0.06, 1.18, 1.92 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
