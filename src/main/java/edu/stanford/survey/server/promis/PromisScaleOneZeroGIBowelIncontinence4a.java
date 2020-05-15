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
 * Item bank for PROMIS assessment. Generated from OID 76DB88BA-CDC0-4796-9C15-D6D8662A2BBD.
 */
public class PromisScaleOneZeroGIBowelIncontinence4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("GISX45", "In the past 7 days", "How often did you have bowel incontinence-that is, have an accident because you could not make it to the bathroom in time?", "", 4.3657, new double[] { 1.2671, 1.7916, 2.3005, 2.7546 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("GISX46", "In the past 7 days", "How often did you soil or dirty your underwear before getting to a bathroom?", "", 36.159, new double[] { 1.1015, 1.655, 2.1031, 2.3972 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("GISX47", "In the past 7 days", "How often did you leak stool or soil your underwear?", "", 4.4882, new double[] { 1.0322, 1.6208, 2.1216, 2.4235 }, -1, "",
          response("No days", 1),
          response("1 day", 2),
          response("2-3 days", 3),
          response("4-5 days", 4),
          response("6-7 days", 5)
      ),
      item("GISX48", "In the past 7 days", "How often did you think you were going to pass gas, but stool or liquid came out instead?", "", 1.5114, new double[] { -0.6806, 1.3338, 2.657, 4.0652 }, -1, "",
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
