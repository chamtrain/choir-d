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
 * Item bank for PROMIS assessment. Generated from OID 40796BF3-29E2-4686-B175-38ECCCB3D72B.
 */
public class PromisParentProxyShortFormOneZeroPhysStressExp8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("EoS_S_017_PXR1", "In the past 7 days,", "My child's muscles felt tight.", "", 1.983, new double[] { 0.962, 1.859, 3.061, 3.879 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_024_PXR1", "In the past 7 days,", "My child's mouth was dry.", "", 1.903, new double[] { 1.115, 2.141, 3.231, 4.019 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_032_PXR1", "In the past 7 days,", "My child's heart beat faster than usual, even when he/she was not exercising or playing hard.", "", 1.791, new double[] { 1.369, 2.221, 3.38, 4.587 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_033_PXR1", "In the past 7 days,", "My child had trouble breathing, even when he/she was not exercising or playing hard.", "", 2.087, new double[] { 1.528, 2.346, 3.323, 3.814 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_039_PXR1", "In the past 7 days,", "My child's body shook.", "", 2.269, new double[] { 2.038, 2.727, 3.585, 4.152 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_042_PXR1", "In the past 7 days,", "My child had a headache.", "", 1.397, new double[] { 0.282, 1.451, 3.122, 4.751 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_044_PXR1", "In the past 7 days,", "My child's back hurt.", "", 1.694, new double[] { 1.035, 1.965, 3.094, 3.908 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_046_PXR1", "In the past 7 days,", "My child had pain that really bothered him/her.", "", 1.684, new double[] { 0.97, 1.872, 2.944, 3.941 }, -1, "",
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
