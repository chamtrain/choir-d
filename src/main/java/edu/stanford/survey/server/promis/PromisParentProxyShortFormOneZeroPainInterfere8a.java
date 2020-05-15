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
 * Item bank for PROMIS assessment. Generated from OID A98737F8-98C7-4759-9955-FC2967291B64.
 */
public class PromisParentProxyShortFormOneZeroPainInterfere8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("Pf1pain4", "In the past 7 days", "It was hard for my child to walk one block when he/she had pain.", "", 2.68, new double[] { 0.46, 0.97, 1.58, 1.92 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2pain2", "In the past 7 days", "My child had trouble doing schoolwork when he/she had pain.", "", 3.54, new double[] { 0.23, 0.8, 1.65, 2.08 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2pain4", "In the past 7 days", "It was hard for my child to run when he/she had pain.", "", 3.02, new double[] { -0.08, 0.47, 1.1, 1.5 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2pain5", "In the past 7 days", "My child had trouble sleeping when he/she had pain.", "", 3.2, new double[] { 0.05, 0.76, 1.53, 2.07 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3pain2", "In the past 7 days", "It was hard for my child to pay attention when he/she had pain.", "", 3.84, new double[] { -0.19, 0.49, 1.46, 1.91 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3pain4", "In the past 7 days", "It was hard for my child to have fun when he/she had pain.", "", 4.17, new double[] { -0.21, 0.36, 1.32, 1.72 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3pain7", "In the past 7 days", "My child felt angry when he/she had pain.", "", 2.53, new double[] { 0.16, 0.8, 1.76, 2.28 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf4pain6", "In the past 7 days", "It was hard for my child to stay standing when he/she had pain.", "", 2.9, new double[] { 0.25, 0.69, 1.43, 1.87 }, -1, "",
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
