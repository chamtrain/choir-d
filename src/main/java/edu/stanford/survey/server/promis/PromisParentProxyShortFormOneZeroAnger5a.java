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
 * Item bank for PROMIS assessment. Generated from OID 298AA607-EF33-43C3-AF50-3D420C735C20.
 */
public class PromisParentProxyShortFormOneZeroAnger5a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 5, 3.0,
      item("Pf1anger1", "In the past 7 days", "My child felt mad.", "", 3.16, new double[] { -1.75, -0.51, 1.53, 2.85 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1anger10", "In the past 7 days", "My child felt upset.", "", 2.02, new double[] { -1.67, -0.33, 2.02, 3.34 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1anger3", "In the past 7 days", "My child was so angry he/she felt like throwing something.", "", 2.21, new double[] { 0.08, 1.03, 2.55, 3.34 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1anger5", "In the past 7 days", "My child was so angry he/she felt like yelling at somebody.", "", 2.42, new double[] { -1.15, -0.05, 1.55, 2.72 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1anger8", "In the past 7 days", "When my child got mad, he/she stayed mad.", "", 1.77, new double[] { -0.08, 1.41, 2.72, 3.49 }, -1, "",
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
