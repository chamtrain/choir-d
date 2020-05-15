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
 * Item bank for PROMIS assessment. Generated from OID FD422168-F4A7-4928-8CD6-EC004FD409EC.
 */
public class PromisParentProxyBankOneZeroPhysicalActivity {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 10, 4.0,
      item("PAC_M_002_PXR1", "In the past 7 days,", "How many days did your child exercise so much that he/she breathed hard?", "", 2.907, new double[] { -0.854, -0.446, 0.397, 1.431 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_M_006_PXR1", "In the past 7 days,", "How many days did your child play sports for 10 minutes or more?", "", 2.033, new double[] { -0.963, -0.644, 0.16, 1.262 }, -1, "",
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
      item("PAC_M_010_PXR1", "In the past 7 days,", "How many days did your child exercise or play so hard that his/her muscles burned?", "", 2.31, new double[] { -0.31, 0.115, 1.034, 2.07 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_M_011_PXR1", "In the past 7 days,", "How many days did your child exercise or play so hard that he/she felt tired?", "", 3.774, new double[] { -0.987, -0.501, 0.405, 1.361 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_M_066_PXR1", "In the past 7 days,", "<u>On a usual day</u>, how physically active was your child?", "", 1.799, new double[] { -3.946, -2.595, -1.299, -0.056 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("PAC_M_105_PXR1", "In the past 7 days,", "How many days did your child exercise <u>really hard</u> for 10 minutes or more?", "", 2.847, new double[] { -1.106, -0.703, 0.189, 1.163 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_M_114_PXR1", "In the past 7 days,", "How many days was your child physically active for 10 minutes or more?", "", 1.843, new double[] { -2.137, -1.549, -0.475, 0.455 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_M_134_PXR1", "In the past 7 days,", "How many days did your child run for 10 minutes or more?", "", 1.808, new double[] { -0.61, -0.214, 0.707, 1.743 }, -1, "",
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
