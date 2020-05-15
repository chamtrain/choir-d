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
 * Item bank for PROMIS assessment. Generated from OID 3C314C93-000D-43A5-A572-1A5CA0DC57C9.
 */
public class PromisParentProxyShortFormOneOneAnxiety8b {
  private static final ItemBank bank = itemBank(0.0, 0.0, 9, 9, 3.0,
      item("Pf1anxiety1", "In the past 7 days", "My child got scared really easy.", "", 1.9, new double[] { -0.05, 1.23, 2.46, 3.35 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1anxiety3", "In the past 7 days", "My child worried about what could happen to him/her.", "", 2.24, new double[] { -0.37, 0.61, 1.94, 2.97 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1anxiety8", "In the past 7 days", "My child felt nervous.", "", 1.85, new double[] { -0.82, 0.34, 1.92, 3.21 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2anxiety1", "In the past 7 days", "My child felt like something awful might happen.", "", 2.35, new double[] { 0.19, 1.25, 2.51, 3.35 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2anxiety2", "In the past 7 days", "My child felt scared.", "", 2.84, new double[] { 0.1, 1.06, 2.27, 2.94 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2anxiety4", "In the past 7 days", "My child worried when he/she went to bed at night.", "", 2.51, new double[] { 0.28, 1.01, 2.12, 3.11 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2anxiety5", "In the past 7 days", "My child worried when he/she was at home.", "", 2.63, new double[] { 0.28, 1.08, 2.53, 3.26 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2anxiety9", "In the past 7 days", "My child felt worried.", "", 2.65, new double[] { -0.69, 0.32, 1.88, 2.85 }, -1, "",
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
