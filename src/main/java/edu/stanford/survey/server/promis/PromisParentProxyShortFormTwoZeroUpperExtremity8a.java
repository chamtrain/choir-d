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
 * Item bank for PROMIS assessment. Generated from OID 64C65877-446C-4EC2-9716-2F46DB3C8BC5.
 */
public class PromisParentProxyShortFormTwoZeroUpperExtremity8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("Pf2uprext2r", "In the past 7 days", "My child could put on his/her shoes without help.", "", 4.83, new double[] { -2.81, -2.52, -2.23, -1.82 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf2uprext3r", "In the past 7 days", "My child could button his/her shirt or pants.", "", 4.64, new double[] { -2.75, -2.63, -2.24, -1.66 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf3uprext11r", "In the past 7 days", "My child could open the rings in school binders.", "", 3.12, new double[] { -3.37, -2.93, -2.27, -1.7 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf3uprext4r", "In the past 7 days", "My child could pull a shirt on over his/her head without help.", "", 4.83, new double[] { -3.35, -2.89, -2.42, -2.05 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf3uprext7r", "In the past 7 days", "My child could use a key to unlock a door.", "", 3.14, new double[] { -2.9, -2.74, -2.5, -1.8 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf3uprext9r", "In the past 7 days", "My child could pull open heavy doors.", "", 2.34, new double[] { -2.81, -2.5, -2.01, -1.15 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf4uprext10r", "In the past 7 days", "My child could pour a drink from a full pitcher.", "", 3.02, new double[] { -2.84, -2.53, -2.08, -1.17 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf4uprext1r", "In the past 7 days", "My child could open a jar by himself/herself.", "", 2.39, new double[] { -2.83, -2.34, -1.72, -1.01 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
