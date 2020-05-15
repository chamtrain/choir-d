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
 * Item bank for PROMIS assessment. Generated from OID 4AFAF9D2-8D23-419E-9094-8B89D47A6A90.
 */
public class PromisPediatricShortFormOneOneAnxiety8b {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("2220R2", "In the past 7 days", "I felt like something awful might happen.", "", 1.71273, new double[] { -0.4315, 0.5121, 1.7544, 2.6483 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("2230R1", "In the past 7 days", "I got scared really easy.", "", 1.48993, new double[] { 0.295, 1.1594, 2.0685, 2.74 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("227bR1", "In the past 7 days", "I felt scared.", "", 1.88971, new double[] { -0.2524, 0.5948, 1.7156, 2.5163 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("231R1", "In the past 7 days", "I worried about what could happen to me.", "", 1.84156, new double[] { -0.2383, 0.4826, 1.5372, 2.2129 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("3150bR2", "In the past 7 days", "I worried when I went to bed at night.", "", 1.82871, new double[] { 0.2539, 0.914, 1.8276, 2.567 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("3459bR1", "In the past 7 days", "I worried when I was at home.", "", 1.64033, new double[] { 0.4015, 1.217, 2.6107, 3.2957 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("5044R1", "In the past 7 days", "I felt worried.", "", 1.8054, new double[] { -0.784, 0.2506, 1.5896, 2.6525 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("713R1", "In the past 7 days", "I felt nervous.", "", 1.50953, new double[] { -0.8533, 0.1793, 1.8578, 2.8535 }, -1, "",
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
