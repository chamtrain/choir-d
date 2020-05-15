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
 * Item bank for PROMIS assessment. Generated from OID 473EE3D5-E8F7-4847-857C-71F2A79E3E22.
 */
public class PromisParentProxyShortFormOneZeroPhysicalActivity4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("PAC_M_002_PXR1", "In the past 7 days,", "How many days did your child exercise so much that he/she breathed hard?", "", 2.907, new double[] { -0.854, -0.446, 0.397, 1.431 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_M_008_PXR1", "In the past 7 days,", "How many days was your child so physically active that he/she sweated?", "", 3.511, new double[] { -1.287, -0.784, 0.133, 1.11 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_M_009_PXR1", "In the past 7 days,", "How many days did your child exercise or play so hard that his/her body got tired?", "", 3.027, new double[] { -1.086, -0.63, 0.332, 1.28 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_M_105_PXR1", "In the past 7 days,", "How many days did your child exercise <u>really hard</u> for 10 minutes or more?", "", 2.847, new double[] { -1.106, -0.703, 0.189, 1.163 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
