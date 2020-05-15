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
 * Item bank for PROMIS assessment. Generated from OID 336741A7-EE8D-432D-A3A5-2381F4E2EE65.
 */
public class NeuroQolPedShortFormV21Fatigue {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQFTGped01", "In the past 7 days", "I felt tired", "", 1.98, new double[] { -1.44, 0.16, 1.34, 2.21 }, -1, "",
          response("none of the time", 1),
          response("a little bit of time", 2),
          response("some of the time", 3),
          response("most of the time", 4),
          response("all of the time", 5)
      ),
      item("NQFTGpeds04", "In the past 7 days", "I had trouble starting things because I was too tired", "", 2.79, new double[] { -0.47, 0.58, 1.45, 2.07 }, -1, "",
          response("none of the time", 1),
          response("a little bit of time", 2),
          response("some of the time", 3),
          response("most of the time", 4),
          response("all of the time", 5)
      ),
      item("NQFTGpeds05", "In the past 7 days", "I had trouble finishing things because I was too tired", "", 2.64, new double[] { -0.57, 0.53, 1.46, 2.54 }, -1, "",
          response("none of the time", 1),
          response("a little bit of time", 2),
          response("some of the time", 3),
          response("most of the time", 4),
          response("all of the time", 5)
      ),
      item("NQFTGpeds06", "In the past 7 days", "I needed to sleep during the day", "", 2.83, new double[] { -0.18, 0.61, 1.39, 2.11 }, -1, "",
          response("none of the time", 1),
          response("a little bit of time", 2),
          response("some of the time", 3),
          response("most of the time", 4),
          response("all of the time", 5)
      ),
      item("NQFTGpeds08", "In the past 7 days", "Being tired made it hard to play or go out with my friends as much as I would like.", "", 3.62, new double[] { 0.09, 0.71, 1.48, 2.16 }, -1, "",
          response("none of the time", 1),
          response("a little bit of time", 2),
          response("some of the time", 3),
          response("most of the time", 4),
          response("all of the time", 5)
      ),
      item("NQFTGpeds11r1", "In the past 7 days", "I was too tired to eat", "", 3.44, new double[] { 0.54, 1.13, 1.79, 2.48 }, -1, "",
          response("none of the time", 1),
          response("a little bit of time", 2),
          response("some of the time", 3),
          response("most of the time", 4),
          response("all of the time", 5)
      ),
      item("NQFTGpeds12", "In the past 7 days", "Being tired makes me sad", "", 3.72, new double[] { 0.29, 0.84, 1.48, 2.05 }, -1, "",
          response("none of the time", 1),
          response("a little bit of time", 2),
          response("some of the time", 3),
          response("most of the time", 4),
          response("all of the time", 5)
      ),
      item("NQFTGpeds13", "In the past 7 days", "Being tired makes me mad", "", 3.26, new double[] { 0.46, 0.97, 1.54, 2.2 }, -1, "",
          response("none of the time", 1),
          response("a little bit of time", 2),
          response("some of the time", 3),
          response("most of the time", 4),
          response("all of the time", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
