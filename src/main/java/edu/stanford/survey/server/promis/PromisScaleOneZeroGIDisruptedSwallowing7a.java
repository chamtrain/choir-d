/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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
 * Item bank for PROMIS assessment. Generated from OID 80BAECB6-5888-48DF-8F44-A776187EB01B.
 */
public class PromisScaleOneZeroGIDisruptedSwallowing7a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("GISX31", "In the past 7 days", "How often did food get stuck in your <u>chest</u> when you were eating?", "", 2.79697, new double[] { 0.3048, 1.1022, 1.8956, 2.6345 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("GISX32", "In the past 7 days", "How often did food get stuck in your <u>throat</u> when you were eating?", "", 3.50162, new double[] { 0.3564, 1.1244, 1.8646, 2.6701 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("GISX33", "In the past 7 days", "How often did you feel pain in your chest when swallowing food?", "", 3.0472, new double[] { 0.4682, 1.189, 2.0876, 2.7911 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("GISX34", "In the past 7 days", "How often did you have difficulty swallowing solid foods like meat, chicken or raw vegetables, even after lots of chewing?", "", 4.25843, new double[] { 0.4358, 1.0543, 1.7026, 2.3056 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("GISX35", "In the past 7 days", "How often did you have difficulty swallowing soft foods like ice cream, apple sauce, or mashed potatoes?", "", 4.37474, new double[] { 1.0391, 1.588, 2.1328, 2.7382 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("GISX36", "In the past 7 days", "How often did you have difficulty swallowing liquids?", "", 2.94457, new double[] { 0.9012, 1.6509, 2.4218, 3.0216 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("GISX37", "In the past 7 days", "How often did you have difficulty swallowing pills?", "", 1.34434, new double[] { -0.0674, 1.1469, 2.3234, 3.2979 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
