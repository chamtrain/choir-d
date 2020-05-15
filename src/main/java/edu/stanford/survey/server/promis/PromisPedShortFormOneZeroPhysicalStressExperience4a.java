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
 * Item bank for PROMIS assessment. Generated from OID 202A1199-CE68-4C33-8252-E2A75F7A5223.
 */
public class PromisPedShortFormOneZeroPhysicalStressExperience4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("EoS_S_032R1", "In the past 7 days,", "My heart beat faster than usual, even when I was not exercising or playing hard.", "", 2.475, new double[] { 1.135, 1.877, 2.724, 3.349 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_033R1", "In the past 7 days,", "I had trouble breathing, even when I was not exercising or playing hard.", "", 2.397, new double[] { 1.376, 2.064, 2.975, 3.446 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_039R1", "In the past 7 days,", "My body shook.", "", 2.254, new double[] { 1.36, 2.119, 2.87, 3.599 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_046R1", "In the past 7 days,", "I had pain that really bothered me.", "", 1.824, new double[] { 0.685, 1.499, 2.4, 3.18 }, -1, "",
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
