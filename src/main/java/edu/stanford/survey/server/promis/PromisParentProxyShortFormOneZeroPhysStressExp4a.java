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
 * Item bank for PROMIS assessment. Generated from OID 8F6A378E-1760-46A6-888B-5C46AEF7D01E.
 */
public class PromisParentProxyShortFormOneZeroPhysStressExp4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
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
