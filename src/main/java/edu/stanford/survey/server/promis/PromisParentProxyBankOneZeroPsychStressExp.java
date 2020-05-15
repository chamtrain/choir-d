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
 * Item bank for PROMIS assessment. Generated from OID 26874899-AD14-4276-8726-2C69DAC4155F.
 */
public class PromisParentProxyBankOneZeroPsychStressExp {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
      item("EoS_P_004_PXR1", "In the past 7 days,", "My child felt concerned about what was going on in his/her life.", "", 1.667, new double[] { -0.306, 0.821, 2.072, 3.185 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_P_011_PXR1", "In the past 7 days,", "My child felt stressed.", "", 2.949, new double[] { -0.36, 0.692, 1.979, 2.736 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_P_047_PXR1", "In the past 7 days,", "Small things upset my child.", "", 1.857, new double[] { 0.018, 1.209, 2.229, 3.318 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_P_048_PXR1", "In the past 7 days", "Everything bothered my child.", "", 2.052, new double[] { 0.384, 1.505, 2.466, 3.38 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_P_063_PXR1", "In the past 7 days,", "My child felt under pressure.", "", 3.162, new double[] { 0.174, 1.077, 2.141, 2.959 }, -1, "",
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
      item("EoS_P_099_PXR1", "In the past 7 days,", "My child felt so upset that he/she could not remember what happened or what he/she did.", "", 2.347, new double[] { 1.229, 2.028, 2.695, 3.546 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_P_105_PXR1", "In the past 7 days,", "My child had trouble concentrating.", "", 1.89, new double[] { -0.014, 1.022, 2.053, 3.097 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_P_108_PXR1", "In the past 7 days,", "My child's thoughts went very fast.", "", 1.744, new double[] { 0.193, 1.116, 2.193, 3.261 }, -1, "",
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
      ),
      item("EoS_P_118_PXR1", "In the past 7 days,", "My child felt he/she had too much going on.", "", 2.642, new double[] { 0.086, 1.043, 2.14, 3.002 }, -1, "",
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
