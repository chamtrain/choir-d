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
 * Item bank for PROMIS assessment. Generated from OID 99ED6E39-E48C-4E18-BC05-BDB8FFB4ECBF.
 */
public class PromisParentProxyShortFormOneZeroPsychStressExp4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("EoS_P_011_PXR1", "In the past 7 days,", "My child felt stressed.", "", 2.949, new double[] { -0.36, 0.692, 1.979, 2.736 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_P_064_PXR1", "In the past 7 days,", "My child felt that his/her problems kept piling up.", "", 3.588, new double[] { 0.356, 1.302, 2.197, 2.948 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_P_067_PXR1", "In the past 7 days,", "My child felt overwhelmed.", "", 3.5, new double[] { 0.134, 1.084, 2.069, 2.937 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_P_112_PXR1", "In the past 7 days,", "My child felt unable to manage things in his/her life.", "", 3.056, new double[] { 0.49, 1.471, 2.281, 2.891 }, -1, "",
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
