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
 * Item bank for PROMIS assessment. Generated from OID 5760C765-95C9-491D-A88F-77889D70E9B4.
 */
public class NeuroQolPedShortFormOneZeroAnxiety {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQEMNped22", "In the past 7 days", "I felt afraid to go out alone", "", 3.09737, new double[] { 0.2284, 0.8292, 1.7093, 2.2066 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped23", "In the past 7 days", "Being worried made it hard for me to be with my friends", "", 5.31528, new double[] { 0.2437, 0.748, 1.5426, 2.3122 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped24", "In the past 7 days", "It was hard to do schoolwork because I was nervous or worried", "", 4.46707, new double[] { 0.0623, 0.6312, 1.5263, 2.1384 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped26", "In the past 7 days", "I felt afraid", "", 4.27429, new double[] { 0.0095, 0.7873, 1.8115, 2.226 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped28", "In the past 7 days", "I worried when I was at home", "", 4.24311, new double[] { 0.2125, 0.9065, 1.8681, 2.4693 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped29", "In the past 7 days", "I felt worried", "", 3.63505, new double[] { -0.2681, 0.4697, 1.6297, 2.2317 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped43", "In the past 7 days", "I worry that my health might get worse", "", 3.95967, new double[] { 0.4059, 1.0616, 1.6345, 2.154 }, -1, "",
          response("not at all", 1),
          response("a little bit", 2),
          response("somewhat", 3),
          response("quite a bit", 4),
          response("very much", 5)
      ),
      item("NQEMNped46", "In the past 7 days", "I worry about doing well in school", "", 1.9172, new double[] { -0.6203, 0.4684, 1.2674, 2.1276 }, -1, "",
          response("not at all", 1),
          response("a little bit", 2),
          response("somewhat", 3),
          response("quite a bit", 4),
          response("very much", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
