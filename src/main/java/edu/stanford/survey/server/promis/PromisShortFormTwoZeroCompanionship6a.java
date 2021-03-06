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
 * Item bank for PROMIS assessment. Generated from OID CE6F55D7-72C5-43AC-B8D6-5DC5F06C19A0.
 */
public class PromisShortFormTwoZeroCompanionship6a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
      item("FSE31057x2", "", "Do you have someone with whom to have fun?", "", 4.60812, new double[] { -1.955, -1.2166, -0.4057, 0.4374 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("FSE31061x2", "", "Do you have someone with whom to relax?", "", 4.80503, new double[] { -1.6809, -1.0482, -0.3834, 0.3959 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("FSE31068x", "", "Do you have someone with whom you can do something enjoyable?", "", 5.69576, new double[] { -2.0393, -1.2859, -0.4714, 0.3806 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("SSC-CaPS1", "", "Do you have someone to keep you company at home?", "", 2.29648, new double[] { -1.6756, -0.9929, -0.422, 0.2495 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("SSC-CaPS10", "", "Do you have someone to go with you to an event?", "", 3.38271, new double[] { -1.8961, -1.0863, -0.3914, 0.5146 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      ),
      item("UCLA15x2", "", "Can you find companionship when you want it?", "", 3.98648, new double[] { -1.8117, -1.0929, -0.4153, 0.4913 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Usually", 4),
          response("Always", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
