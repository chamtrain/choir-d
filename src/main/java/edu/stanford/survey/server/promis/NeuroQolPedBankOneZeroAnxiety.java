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
 * Item bank for PROMIS assessment. Generated from OID 037D7B69-FCB2-482E-A1CE-9A4D017D24AD.
 */
public class NeuroQolPedBankOneZeroAnxiety {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQEMNped02", "In the past 7 days", "I become anxious when I go back to the hospital or clinic.", "", 1.68695, new double[] { 0.3259, 1.299, 1.9864, 2.7889 }, -1, "",
          response("not at all", 1),
          response("a little bit", 2),
          response("somewhat", 3),
          response("quite a bit", 4),
          response("very much", 5)
      ),
      item("NQEMNped03", "In the past 7 days", "I worry about how my health will affect my future.", "", 1.99849, new double[] { 0.1191, 1.042, 1.6749, 2.4915 }, -1, "",
          response("not at all", 1),
          response("a little bit", 2),
          response("somewhat", 3),
          response("quite a bit", 4),
          response("very much", 5)
      ),
      item("NQEMNped06", "In the past 7 days", "Because of my health, I worry about having a boyfriend or girlfriend.", "", 2.43757, new double[] { 0.4307, 0.9457, 1.4653, 2.1525 }, -1, "",
          response("not at all", 1),
          response("a little bit", 2),
          response("somewhat", 3),
          response("quite a bit", 4),
          response("very much", 5)
      ),
      item("NQEMNped10", "In the past 7 days", "I worry about getting a good job because of my medical condition.", "", 2.89538, new double[] { 0.5698, 1.0452, 1.5512, 1.9735 }, -1, "",
          response("not at all", 1),
          response("a little bit", 2),
          response("somewhat", 3),
          response("quite a bit", 4),
          response("very much", 5)
      ),
      item("NQEMNped20", "In the past 7 days", "I get nervous more easily than other people.", "", 2.85736, new double[] { -0.2015, 0.7765, 1.4498, 2.3593 }, -1, "",
          response("not at all", 1),
          response("a little bit", 2),
          response("somewhat", 3),
          response("quite a bit", 4),
          response("very much", 5)
      ),
      item("NQEMNped21", "In the past 7 days", "I worried when I was away from my family.", "", 2.82862, new double[] { -0.1294, 0.6452, 1.4433, 2.1902 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
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
      item("NQEMNped25", "In the past 7 days", "I got scared easily.", "", 3.74236, new double[] { 0.1121, 0.8848, 1.7364, 2.2561 }, -1, "",
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
      item("NQEMNped27", "In the past 7 days", "I was worried that I might die.", "", 3.58471, new double[] { 0.5292, 1.1316, 1.8748, 2.3995 }, -1, "",
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
      item("NQEMNped30", "In the past 7 days", "I felt nervous", "", 3.82646, new double[] { -0.3677, 0.3933, 1.5245, 2.3004 }, -1, "",
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
      item("NQEMNped44", "In the past 7 days", "Because of my health, I worry about being able to go to college.", "", 3.26346, new double[] { 0.5305, 1.0641, 1.6022, 1.9929 }, -1, "",
          response("not at all", 1),
          response("a little bit", 2),
          response("somewhat", 3),
          response("quite a bit", 4),
          response("very much", 5)
      ),
      item("NQEMNped45", "In the past 7 days", "Because of my health, I worry about getting a job to support myself.", "", 3.53959, new double[] { 0.3928, 0.9904, 1.4153, 1.8819 }, -1, "",
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
