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
 * Item bank for PROMIS assessment. Generated from OID 15DF3725-5529-45D5-8955-2CF9E830969C.
 */
public class PromisShortFormOneOneAnger5a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 5, 3.0,
      item("EDANG03", "In the past 7 days", "I was irritated more than people knew", "", 2.34877, new double[] { -0.7617, 0.0609, 1.1574, 2.2002 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG09", "In the past 7 days", "I felt angry", "", 2.82932, new double[] { -0.826, 0.3331, 1.6847, 2.9031 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG15", "In the past 7 days", "I felt like I was ready to explode", "", 2.82094, new double[] { 0.2872, 1.0593, 1.9753, 3.0347 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG30", "In the past 7 days", "I was grouchy", "", 2.98863, new double[] { -0.8157, 0.2649, 1.5402, 2.8419 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANG35", "In the past 7 days", "I felt annoyed", "", 2.49943, new double[] { -1.1088, -0.0099, 1.2882, 2.8597 }, -1, "",
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
