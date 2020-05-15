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
 * Item bank for PROMIS assessment. Generated from OID 8E0697A0-708D-4766-9DCE-597B355FD5E7.
 */
public class PromisPediatricScaleOneOneAnger5a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 5, 3.0,
      item("206R1", "In the past 7 days", "I felt mad.", "", 2.15353, new double[] { -1.5391, -0.4705, 1.5601, 2.5562 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("2319aR1", "In the past 7 days", "I was so angry I felt like throwing something.", "", 1.82167, new double[] { 0.1978, 0.9809, 2.1376, 2.7493 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("2581R1", "In the past 7 days", "I was so angry I felt like yelling at somebody.", "", 1.97413, new double[] { -0.4683, 0.4061, 1.5108, 2.1604 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("5045R1", "In the past 7 days", "I felt fed up.", "", 1.3085, new double[] { -0.5389, 0.4103, 2.0707, 3.5448 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("714R1", "In the past 7 days", "I felt upset.", "", 1.52927, new double[] { -1.412, -0.237, 1.5847, 2.6593 }, -1, "",
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
