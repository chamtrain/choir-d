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
 * Item bank for PROMIS assessment. Generated from OID 584B17F2-3FB7-4D06-BAF8-4C0BD71A2FB4.
 */
public class PromisParentProxyShortFormOneZeroFatigue10a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 10, 10, 3.0,
      item("Pf2fatigue4", "In the past 7 days", "My child had trouble finishing things because he/she was too tired.", "", 2.13, new double[] { -0.54, 0.66, 1.82, 2.4 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2fatigue8", "In the past 7 days", "Being tired made it hard for my child to keep up with schoolwork.", "", 2.67, new double[] { 0.02, 0.75, 1.68, 2.3 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3fatigue12", "In the past 7 days", "My child was so tired it was hard for him/her to pay attention.", "", 2.43, new double[] { -0.46, 0.76, 1.88, 2.6 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3fatigue4", "In the past 7 days", "My child was too tired to do things outside.", "", 1.91, new double[] { -0.18, 0.89, 2.13, 2.82 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3fatigue7", "In the past 7 days", "My child had trouble starting things because he/she was too tired.", "", 3.1, new double[] { -0.38, 0.72, 1.87, 2.59 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3fatigue8", "In the past 7 days", "My child was too tired to do sports or exercise.", "", 2.48, new double[] { -0.31, 0.62, 1.72, 2.4 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf4fatigue12", "In the past 7 days", "Being tired made it hard for my child to play or go out with friends as much as he/she would like.", "", 3.43, new double[] { 0.21, 1.01, 2.23, 2.72 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf4fatigue3", "In the past 7 days", "My child got tired easily.", "", 2.61, new double[] { -0.68, 0.36, 1.51, 2.32 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf4fatigue4", "In the past 7 days", "My child was too tired to enjoy the things he/she likes to do.", "", 4.36, new double[] { -0.1, 0.96, 1.98, 2.93 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf4fatigue8", "In the past 7 days", "My child felt weak.", "", 2.75, new double[] { 0.06, 0.87, 1.96, 3.05 }, -1, "",
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
