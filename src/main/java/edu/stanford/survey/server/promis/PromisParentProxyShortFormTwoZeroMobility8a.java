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
 * Item bank for PROMIS assessment. Generated from OID 3EBD0F78-AEA0-4610-95A4-9EF8E9C855B7.
 */
public class PromisParentProxyShortFormTwoZeroMobility8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("Pf1mobil1r", "In the past 7 days", "My child has been physically able to do the activities he/she enjoys most.", "", 1.92, new double[] { -2.95, -2.49, -1.66, -0.92 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf1mobil3r", "In the past 7 days", "My child could do sports and exercise that other kids his/her age could do.", "", 3.79, new double[] { -2.01, -1.67, -1.2, -0.53 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf2mobil4r", "In the past 7 days", "My child could walk up stairs without holding on to anything.", "", 3.76, new double[] { -2.1, -1.87, -1.47, -1.07 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf2mobil7r", "In the past 7 days", "My child could stand up on his/her tiptoes.", "", 3.18, new double[] { -2.23, -2.09, -1.8, -1.4 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf3mobil3r", "In the past 7 days", "My child could stand up without help.", "", 3.9, new double[] { -2.73, -2.53, -2.2, -1.76 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf3mobil8r", "In the past 7 days", "My child could move his/her legs.", "", 3.06, new double[] { -3.75, -2.78, -2.13, -1.64 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf3mobil9r", "In the past 7 days", "My child could get up from the floor.", "", 4.0, new double[] { -2.98, -2.35, -1.94, -1.38 }, -1, "",
          response("With no trouble", 5),
          response("With a little trouble", 4),
          response("With some trouble", 3),
          response("With a lot of trouble", 2),
          response("Not able to do", 1)
      ),
      item("Pf4mobil4r", "In the past 7 days", "My child could keep up when he/she played with other kids.", "", 2.58, new double[] { -2.57, -2.18, -1.68, -0.93 }, -1, "",
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
