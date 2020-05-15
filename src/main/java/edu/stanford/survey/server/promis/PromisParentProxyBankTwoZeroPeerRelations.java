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
 * Item bank for PROMIS assessment. Generated from OID 3B5666FF-3876-45AB-BA30-474649A21E89.
 */
public class PromisParentProxyBankTwoZeroPeerRelations {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
      item("Pf1socabil2r", "In the past 7 days", "Other kids wanted to be my child's friend.", "", 2.3, new double[] { -2.72, -2.09, -1.05, -0.07 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1socabil6r", "In the past 7 days", "My child felt good about his/her friendships.", "", 3.41, new double[] { -2.97, -2.31, -1.21, -0.32 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf1socrole2r", "In the past 7 days", "My child was able to have fun with his/her friends.", "", 2.64, new double[] { -2.6, -2.29, -1.43, -0.6 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2socabil6r", "In the past 7 days", "My child shared with other kids (food, games, pens, etc.).", "", 1.52, new double[] { -3.63, -3.14, -1.75, -0.55 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2socabil7r", "In the past 7 days", "My child was a good friend.", "", 2.04, new double[] { -4.03, -3.34, -2.01, -0.78 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2socabil9r", "In the past 7 days", "Other kids wanted to talk to my child.", "", 2.63, new double[] { -3.27, -2.41, -1.36, -0.45 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2socrole4r", "In the past 7 days", "My child and his/her friends helped each other out.", "", 2.32, new double[] { -2.99, -2.54, -1.04, -0.12 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3socabil4r", "In the past 7 days", "My child was good at making friends.", "", 2.56, new double[] { -2.78, -2.14, -0.95, -0.16 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3socabil9r", "In the past 7 days", "My child felt accepted by other kids his/her age.", "", 2.49, new double[] { -2.32, -1.93, -1.14, -0.22 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3socrole4r", "In the past 7 days", "Other kids wanted to be with my child.", "", 3.53, new double[] { -2.53, -2.03, -0.96, -0.15 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf4socabil10r", "In the past 7 days", "My child was able to talk about everything with his/her friends.", "", 2.12, new double[] { -2.8, -2.0, -0.75, 0.07 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf4socabil12r", "In the past 7 days", "My child was able to count on his/her friends.", "", 2.54, new double[] { -2.88, -2.07, -0.79, 0.13 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf4socabil15r", "In the past 7 days", "My child played alone and kept to himself/herself.", "", 1.05, new double[] { -3.5, -2.53, -0.58, 0.95 }, -1, "",
          response("Never", 5),
          response("Almost Never", 4),
          response("Sometimes", 3),
          response("Often", 2),
          response("Almost Always", 1)
      ),
      item("Pf4socabil4r", "In the past 7 days", "My child liked being around other kids his/her age.", "", 2.2, new double[] { -2.95, -2.43, -1.54, -0.63 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf4socrole3r", "In the past 7 days", "My child spent time with his/her friends.", "", 1.68, new double[] { -3.01, -2.16, -0.63, 0.63 }, -1, "",
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