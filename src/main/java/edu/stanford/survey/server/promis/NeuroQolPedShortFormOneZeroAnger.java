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
 * Item bank for PROMIS assessment. Generated from OID 060D3038-B8AA-410A-AA49-64D169F9F658.
 */
public class NeuroQolPedShortFormOneZeroAnger {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQEMNped12", "In the past 7 days", "Being angry made it hard for me to be with my friends.", "", 3.31373, new double[] { 0.0405, 0.599, 1.5575, 2.41 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped13", "In the past 7 days", "It was hard to do schoolwork because I was angry.", "", 3.22238, new double[] { -0.0218, 0.5438, 1.4991, 2.2016 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped14", "In the past 7 days", "I felt angry.", "", 3.79062, new double[] { -0.6375, 0.1733, 1.3767, 2.1596 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped15", "In the past 7 days", "I was so mad that I felt like throwing something.", "", 5.90559, new double[] { -0.1569, 0.4499, 1.3576, 1.988 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped16", "In the past 7 days", "I was so mad that I felt like hitting something.", "", 6.57166, new double[] { -0.0387, 0.6039, 1.4349, 1.9646 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped17", "In the past 7 days", "I was so mad that I felt like yelling at someone.", "", 4.9357, new double[] { -0.5385, 0.1764, 1.1753, 1.9309 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped18", "In the past 7 days", "I was so mad that I felt like breaking things.", "", 5.45497, new double[] { 0.0639, 0.7063, 1.5209, 2.1738 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQEMNped19", "In the past 7 days", "I was so mad that I acted grouchy towards other people.", "", 3.20809, new double[] { -0.6821, 0.0102, 1.2128, 2.0484 }, -1, "",
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
