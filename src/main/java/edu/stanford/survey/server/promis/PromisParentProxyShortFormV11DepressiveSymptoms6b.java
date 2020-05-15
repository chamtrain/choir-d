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
 * Item bank for PROMIS assessment. Generated from OID B0E61D83-3FE9-42C4-A410-095F6B387FC3.
 */
public class PromisParentProxyShortFormV11DepressiveSymptoms6b {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
      item("Pf1depr5", "In the past 7 days", "My child felt like he/she couldn't do anything right.", "", 2.18, new double[] { -0.27, 0.75, 1.99, 2.89 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1depr7", "In the past 7 days", "My child felt everything in his/her life went wrong.", "", 2.75, new double[] { 0.26, 0.91, 1.89, 2.65 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2depr10", "In the past 7 days", "My child felt lonely.", "", 2.12, new double[] { -0.12, 0.87, 2.28, 3.82 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2depr3", "In the past 7 days", "My child felt sad.", "", 2.43, new double[] { -0.79, 0.4, 1.99, 3.05 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2depr6", "In the past 7 days", "It was hard for my child to have fun.", "", 2.52, new double[] { 0.07, 1.04, 2.15, 2.92 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2depr7", "In the past 7 days", "My child could not stop feeling sad.", "", 2.98, new double[] { 0.59, 1.39, 2.14, 2.82 }, -1, "",
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
