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
 * Item bank for PROMIS assessment. Generated from OID 18B90183-FE27-4DD4-8A23-7C1E4E311F67.
 */
public class NeuroQolBankOneZeroStigma {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
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
      item("NQSTG03", "Lately", "Because of my illness, I felt emotionally distant from other people.", "", 3.52929, new double[] { -0.0496, 0.3764, 0.9948, 1.6697 }, -1, "",
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
      item("NQSTG06", "Lately", "Because of my illness, people made fun of me.", "", 2.85095, new double[] { 0.8895, 1.4822, 2.2944, 2.965 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG07", "Lately", "Because of my illness, I felt embarrassed in social situations.", "", 3.98822, new double[] { 0.1738, 0.6155, 1.2692, 1.9027 }, -1, "",
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
      item("NQSTG09", "Lately", "Because of my illness, strangers tended to stare at me.", "", 2.65475, new double[] { 0.7411, 1.3513, 2.0372, 2.5424 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG10", "Lately", "Because of my illness, I worried about other people's attitudes towards me.", "", 3.27662, new double[] { 0.3538, 0.7686, 1.3021, 1.9673 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG11", "Lately", "Because of my illness, I was treated unfairly by others.", "", 3.76064, new double[] { 0.5404, 1.1235, 1.8159, 2.3161 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG12", "Lately", "I was unhappy about how my illness affected my appearance.", "", 2.67305, new double[] { 0.1724, 0.6167, 1.1909, 1.6331 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG13", "Lately", "Because of my illness, it was hard for me to stay neat and clean.", "", 2.42666, new double[] { 0.5108, 0.9947, 1.7392, 2.4247 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG14", "Lately", "Because of my illness, people tended to ignore my good points.", "", 4.18968, new double[] { 0.5171, 1.0161, 1.6609, 2.133 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG15", "Lately", "Because of my illness, I worried that I was a burden to others.", "", 3.28362, new double[] { -0.1601, 0.22, 0.9341, 1.4723 }, -1, "",
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
      item("NQSTG18", "Lately", "I felt embarrassed about my speech.", "", 1.94282, new double[] { 0.6076, 0.983, 1.6902, 2.4294 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG19", "Lately", "Because of my illness, I felt different from others.", "", 3.34828, new double[] { -0.1124, 0.417, 0.9551, 1.4508 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG20", "Lately", "I tended to blame myself for my problems.", "", 1.65755, new double[] { -0.3368, 0.3114, 1.2398, 2.1552 }, -1, "",
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
      ),
      item("NQSTG22", "Lately", "I avoided making new friends to avoid telling others about my illness.", "", 3.09105, new double[] { 0.5357, 0.9754, 1.4343, 1.932 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG25", "Lately", "People with my illness lost their jobs when their employers found out about it.", "", 1.48903, new double[] { 0.0055, 0.6193, 1.8062, 2.8934 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQSTG26", "Lately", "I lost friends by telling them that I have this illness.", "", 2.51833, new double[] { 0.8785, 1.393, 1.9593, 2.6939 }, -1, "",
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
