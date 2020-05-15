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
 * Item bank for PROMIS assessment. Generated from OID 7D3B96F8-5F79-4BFE-8AF6-4608935AFFEC.
 */
public class NeuroQolShortFormOneZeroStigma {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQSTG01", "Lately", "Because of my illness, some people seemed  uncomfortable with me", "", 3.44314, new double[] { 0.0958, 0.7472, 1.4286, 2.4042 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG02", "Lately", "Because of my illness, some people avoided me.", "", 4.06104, new double[] { 0.3513, 0.8893, 1.5553, 2.1985 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG04", "Lately", "Because of my illness, I felt left out of things", "", 4.00129, new double[] { -0.0589, 0.3539, 0.9446, 1.6095 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG05", "Lately", "Because of my illness, people were unkind to me", "", 3.30964, new double[] { 0.6524, 1.2615, 2.0968, 3.0858 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG08", "Lately", "Because of my illness, people avoided looking at me", "", 3.92346, new double[] { 0.6715, 1.2285, 1.8107, 2.6988 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG16", "Lately", "I felt embarrassed about my illness", "", 3.45683, new double[] { 0.1838, 0.5917, 1.1831, 1.6868 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG17", "Lately", "I felt embarrassed because of my physical limitations", "", 3.39119, new double[] { -0.0744, 0.3473, 1.016, 1.6108 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG21", "Lately", "Some people acted as though it was my fault I have this illness", "", 2.8818, new double[] { 0.5032, 0.9522, 1.5378, 2.1911 }, -1, "",
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
