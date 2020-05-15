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
 * Item bank for PROMIS assessment. Generated from OID 443A0700-F949-4BC7-BA6E-60C442B39B74.
 */
public class PromisParentProxyShortFormOneZeroPeerRelations7a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("Pf1socabil2", "In the past 7 days", "Other kids wanted to be my child's friend.", "", 2.3, new double[] { -2.72, -2.09, -1.05, -0.07 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2socabil9", "In the past 7 days", "Other kids wanted to talk to my child.", "", 2.63, new double[] { -3.27, -2.41, -1.36, -0.45 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf2socrole4", "In the past 7 days", "My child and his/her friends helped each other out.", "", 2.32, new double[] { -2.99, -2.54, -1.04, -0.12 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3socabil4", "In the past 7 days", "My child was good at making friends.", "", 2.56, new double[] { -2.78, -2.14, -0.95, -0.16 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3socabil9", "In the past 7 days", "My child felt accepted by other kids his/her age.", "", 2.49, new double[] { -2.32, -1.93, -1.14, -0.22 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf3socrole4", "In the past 7 days", "Other kids wanted to be with my child.", "", 3.53, new double[] { -2.53, -2.03, -0.96, -0.15 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("Pf4socabil12", "In the past 7 days", "My child was able to count on his/her friends.", "", 2.54, new double[] { -2.88, -2.07, -0.79, 0.13 }, -1, "",
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
