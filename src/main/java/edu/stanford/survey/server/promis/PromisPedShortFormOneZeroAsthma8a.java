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
 * Item bank for PROMIS assessment. Generated from OID D02A1A75-6CEF-48EC-B85B-A67B0DFF7DE0.
 */
public class PromisPedShortFormOneZeroAsthma8a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("1498R2", "In the past 7 days", "I had trouble breathing because of my asthma.", "", 2.93138, new double[] { -0.8327, -0.2954, 0.8308, 1.4982 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("1499R1", "In the past 7 days", "My asthma bothered me.", "", 2.64375, new double[] { -0.8654, -0.263, 0.8579, 1.5122 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("1610R1", "In the past 7 days", "It was hard for me to play sports or exercise because of my asthma.", "", 2.00072, new double[] { -0.4498, 0.0821, 1.1493, 1.7787 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("1664R1", "In the past 7 days", "I had trouble sleeping at night because of my asthma.", "", 2.00017, new double[] { -0.0458, 0.5024, 1.3455, 1.9459 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("2bR2", "In the past 7 days", "My chest felt tight because of my asthma.", "", 1.96172, new double[] { -0.9783, -0.3907, 0.697, 1.3957 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("3R2", "In the past 7 days", "I felt wheezy because of my asthma.", "", 2.28016, new double[] { -0.566, 0.0445, 1.1029, 1.8733 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("5304R1", "In the past 7 days", "I felt scared that I might have trouble breathing because of my asthma.", "", 1.92607, new double[] { -0.7483, -0.0865, 1.0593, 1.8673 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("8R2", "In the past 7 days", "It was hard to take a deep breath because of my asthma.", "", 2.14871, new double[] { -0.589, 0.0399, 0.9983, 1.6442 }, -1, "",
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
