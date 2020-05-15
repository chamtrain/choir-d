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
 * Item bank for PROMIS assessment. Generated from OID 6B3AE8AC-BD94-43E2-AEAD-CEDCC36A69B2.
 */
public class PromisParentProxyBankTwoZeroDepressiveSx {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
      item("Pf1depr1r", "In the past 7 days", "My child wanted to be by himself/herself.", "", 0.89, new double[] { -1.42, 0.12, 2.65, 5.14 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1depr4r", "In the past 7 days", "My child felt alone.", "", 2.23, new double[] { 0.4, 1.38, 2.76, 3.16 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1depr5r", "In the past 7 days", "My child felt like he/she couldn't do anything right.", "", 2.18, new double[] { -0.27, 0.75, 1.99, 2.89 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1depr7r", "In the past 7 days", "My child felt everything in his/her life went wrong.", "", 2.75, new double[] { 0.26, 0.91, 1.89, 2.65 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1depr8r", "In the past 7 days", "It was hard for my child to do school work because he/she felt sad.", "", 2.7, new double[] { 0.65, 1.46, 2.28, 2.91 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2depr10r", "In the past 7 days", "My child felt lonely.", "", 2.12, new double[] { -0.12, 0.87, 2.28, 3.82 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2depr11r", "In the past 7 days", "My child felt unhappy.", "", 3.23, new double[] { -0.75, 0.31, 1.71, 2.69 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2depr1r", "In the past 7 days", "My child felt too sad to eat.", "", 1.89, new double[] { 1.31, 2.09, 3.68, 4.05 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2depr2r", "In the past 7 days", "My child didn't care about anything.", "", 1.14, new double[] { 0.58, 1.88, 3.54, 4.82 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2depr3r", "In the past 7 days", "My child felt sad.", "", 2.43, new double[] { -0.79, 0.4, 1.99, 3.05 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2depr6r", "In the past 7 days", "It was hard for my child to have fun.", "", 2.52, new double[] { 0.07, 1.04, 2.15, 2.92 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2depr7r", "In the past 7 days", "My child could not stop feeling sad.", "", 2.98, new double[] { 0.59, 1.39, 2.14, 2.82 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2depr8r", "In the past 7 days", "My child felt stressed.", "", 1.84, new double[] { -0.92, 0.28, 1.69, 2.99 }, -1, "",
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
