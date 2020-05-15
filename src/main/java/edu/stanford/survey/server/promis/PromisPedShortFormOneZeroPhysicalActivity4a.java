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
 * Item bank for PROMIS assessment. Generated from OID 9FA3C34F-AD5A-446B-B9F9-B996D3DF66FD.
 */
public class PromisPedShortFormOneZeroPhysicalActivity4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("PAC_M_002R1", "In the past 7 days,", "How many days did you exercise so much that you breathed hard?", "", 3.743, new double[] { -0.889, -0.418, 0.343, 1.165 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_M_008R1", "In the past 7 days,", "How many days were you so physically active that you sweated?", "", 3.436, new double[] { -1.183, -0.66, 0.121, 0.861 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_M_009R1", "In the past 7 days,", "How many days did you exercise or play so hard that your body got tired?", "", 3.439, new double[] { -1.004, -0.422, 0.389, 1.198 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("PAC_M_105R1", "In the past 7 days,", "How many days did you exercise <u>really hard</u> for 10 minutes or more?", "", 3.472, new double[] { -0.855, -0.382, 0.326, 1.044 }, -1, "",
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
