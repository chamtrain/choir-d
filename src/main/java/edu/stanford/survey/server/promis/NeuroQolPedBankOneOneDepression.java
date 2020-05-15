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
 * Item bank for PROMIS assessment. Generated from OID 0E1CD7E6-6143-48D6-A14F-D5429964A2F8.
 */
public class NeuroQolPedBankOneOneDepression {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQEMNped01", "In the past 7 days", "I felt too sad to do things with friends", "", 2.62405, new double[] { -0.0256, 0.6573, 1.9175, 2.5957 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped04", "In the past 7 days", "I felt sad", "", 2.90917, new double[] { -0.4953, 0.2996, 1.4805, 2.4839 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped08", "In the past 7 days", "I was bored.", "", 1.82794, new double[] { -1.5341, -0.8213, 0.8062, 1.9733 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped09", "In the past 7 days", "I felt lonely", "", 3.26573, new double[] { -0.4863, 0.1466, 1.238, 1.982 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped11", "In the past 7 days", "I felt frustrated.", "", 2.5974, new double[] { -1.004, -0.2174, 1.1018, 2.0567 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped31", "In the past 7 days", "I was less interested in doing things I usually enjoy", "", 3.93245, new double[] { -0.0307, 0.7006, 1.6276, 2.2268 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped32", "In the past 7 days", "My mood swings from good feelings to bad feelings.", "", 3.66325, new double[] { -0.2001, 0.6967, 1.3875, 2.0942 }, -1, "",
          response("not at all", 1),
          response("a little bit", 2),
          response("somewhat", 3),
          response("quite a bit", 4),
          response("very much", 5)
      ),
      item("NQEMNped33", "In the past 7 days", "I had trouble sleeping.", "", 2.383, new double[] { -0.2262, 0.6199, 1.4668, 2.0727 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped34", "In the past 7 days", "It was hard for me to care about anything.", "", 4.45517, new double[] { 0.148, 0.7918, 1.523, 2.2621 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped36", "In the past 7 days", "It was hard for me to have fun", "", 4.77898, new double[] { -0.0436, 0.5842, 1.3948, 2.0513 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped37", "In the past 7 days", "I felt that no one loved me.", "", 3.55103, new double[] { 0.1989, 0.8061, 1.6729, 2.1882 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped38", "In the past 7 days", "I cried more often than usual.", "", 3.33291, new double[] { 0.4101, 1.1219, 1.8118, 2.3694 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped39", "In the past 7 days", "I felt alone", "", 4.50642, new double[] { 0.0649, 0.6762, 1.5238, 2.2166 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped40", "In the past 7 days", "I felt like I couldn't do anything right", "", 3.90534, new double[] { -0.2431, 0.4248, 1.3776, 1.8663 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped41", "In the past 7 days", "I felt everything in my life went wrong", "", 4.97058, new double[] { -0.0139, 0.5668, 1.3458, 1.8465 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped42", "In the past 7 days", "I felt too sad to do my schoolwork.", "", 4.76297, new double[] { 0.2354, 0.8241, 1.5381, 2.1447 }, -1, "",
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
