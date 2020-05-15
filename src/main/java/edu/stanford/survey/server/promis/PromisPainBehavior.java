/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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
 * Item bank for PROMIS pain behavior assessment. Generated from OID C4ADCFAB-6B75-498E-9E94-AFD3BA211DC4.
 */
public class PromisPainBehavior {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("PAINBE11", "In the past 7 days", "When I was in pain I clenched my teeth", "", 4.32363, new double[] { -0.4187, 0.6904, 0.9898, 1.4224, 1.8625 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE13", "In the past 7 days", "When I was in pain I tried to stay very still", "", 4.3246, new double[] { -0.4131, 0.4374, 0.7946, 1.3042, 1.8665 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE16", "In the past 7 days", "When I was in pain I appeared upset or sad", "", 5.68157, new double[] { -0.3887, 0.4998, 0.8579, 1.28, 1.6844 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE17", "In the past 7 days", "When I was in pain I gasped", "", 5.6521, new double[] { -0.3775, 0.7089, 1.072, 1.5371, 2.0004 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE18", "In the past 7 days", "When I was in pain I asked for help doing things that needed to be done", "", 4.09809, new double[] { -0.3334, 0.5999, 0.9695, 1.4092, 1.8816 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE2", "In the past 7 days", "When I was in pain I became irritable", "", 5.50922, new double[] { -0.3571, 0.288, 0.6984, 1.1961, 1.6378 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE21", "In the past 7 days", "When I was in pain it showed on my face (squinching eyes, opening eyes wide, frowning)", "", 5.6598, new double[] { -0.3691, 0.3507, 0.743, 1.1979, 1.6085 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE22", "In the past 7 days", "Pain caused me to bend over while walking", "", 3.99343, new double[] { -0.3449, 0.8648, 1.1687, 1.5795, 2.0325 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE23", "In the past 7 days", "When I was in pain I asked one or more people to leave me alone", "", 4.96536, new double[] { -0.3445, 0.867, 1.1444, 1.5077, 1.9839 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE24", "In the past 7 days", "When I was in pain I moved stiffly", "", 4.53196, new double[] { -0.4029, 0.194, 0.5348, 1.0434, 1.4722 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE25", "In the past 7 days", "When I was in pain I called out for someone to help me", "", 5.06239, new double[] { -0.3056, 1.0062, 1.3672, 1.7784, 2.1113 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE26", "In the past 7 days", "Pain caused me to curl up in a ball", "", 6.07633, new double[] { -0.3438, 0.9698, 1.2305, 1.588, 2.0012 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE27", "In the past 7 days", "I had pain so bad it made me cry", "", 6.50844, new double[] { -0.3599, 0.8932, 1.128, 1.4566, 1.8622 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE28", "In the past 7 days", "When I was in pain I squirmed", "", 5.05816, new double[] { -0.3559, 0.6088, 0.8986, 1.3424, 1.7968 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE29", "In the past 7 days", "When I was in pain I used a cane or something else for support", "", 4.18279, new double[] { -0.3476, 1.0253, 1.2296, 1.4818, 1.7303 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE3", "In the past 7 days", "When I was in pain I grimaced", "", 5.03393, new double[] { -0.3654, 0.2884, 0.6979, 1.2893, 1.824 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE31", "In the past 7 days", "I limped because of pain", "", 3.91852, new double[] { -0.4169, 0.4789, 0.7667, 1.2034, 1.5839 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE32", "In the past 7 days", "When I was in pain I became quiet and withdrawn", "", 5.05369, new double[] { -0.4084, 0.4687, 0.7987, 1.259, 1.7457 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE33", "In the past 7 days", "When I was in pain I frowned", "", 5.21573, new double[] { -0.4075, 0.2699, 0.6372, 1.2118, 1.6958 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE35", "In the past 7 days", "When I was in pain I groaned", "", 4.52867, new double[] { -0.4196, 0.5917, 0.9918, 1.4703, 1.9466 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE37", "In the past 7 days", "When I was in pain I isolated myself from others", "", 5.38168, new double[] { -0.4043, 0.6155, 0.8702, 1.2189, 1.7178 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE38", "In the past 7 days", "When I was in pain I drew my knees up", "", 4.6239, new double[] { -0.3763, 0.8831, 1.1409, 1.6142, 2.0964 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE39", "In the past 7 days", "When I was in pain I moaned, whined or whimpered", "", 5.20977, new double[] { -0.3722, 0.7172, 1.0631, 1.4705, 1.9006 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      // I manually checked this against assessment center, and it seems to match, but this can't be right...
      // and it breaks the algorithm because alpha=0 causes information value=NaN, boosting to top of list
//      item("PAINBE41", "In the past 7 days", "When I was in pain I screamed", "", 0.0, new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 }, -1, "",
//          response("Had no pain", 1),
//          response("Never", 2),
//          response("Rarely", 3),
//          response("Sometimes", 4),
//          response("Often", 5),
//          response("Always", 6)
//      ),
      item("PAINBE42", "In the past 7 days", "When I was in pain my upper body would tense up", "", 4.69639, new double[] { -0.3697, 0.5325, 0.7949, 1.1932, 1.5933 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE43", "In the past 7 days", "When I was in pain I walked carefully", "", 4.45538, new double[] { -0.3467, 0.2667, 0.5514, 0.9734, 1.347 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE44", "In the past 7 days", "When I was in pain I bit or pursed my lips", "", 4.69558, new double[] { -0.3579, 0.7375, 1.0358, 1.4578, 1.9767 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE45", "In the past 7 days", "When I was in pain I thrashed", "", 4.98842, new double[] { -0.3394, 1.1122, 1.385, 1.7463, 2.184 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE46", "In the past 7 days", "When I was in pain I protected the part of my body that hurt", "", 4.25825, new double[] { -0.3867, 0.2752, 0.5752, 1.041, 1.4908 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE47", "In the past 7 days", "When I was in pain my body became stiff", "", 4.33321, new double[] { -0.3843, 0.5068, 0.7647, 1.1956, 1.6882 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE48", "In the past 7 days", "When I was in pain I clenched my jaw or gritted my teeth", "", 5.41694, new double[] { -0.3637, 0.626, 0.9042, 1.2978, 1.7222 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE49", "In the past 7 days", "When I was in pain I winced", "", 4.98691, new double[] { -0.393, 0.295, 0.6522, 1.2242, 1.7359 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE50", "In the past 7 days", "When I was in pain I moved my limbs protectively", "", 4.60196, new double[] { -0.3299, 0.3445, 0.6428, 1.1258, 1.542 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE51", "In the past 7 days", "When I was in pain I avoided physical contact with others", "", 5.42293, new double[] { -0.3404, 0.6181, 0.9002, 1.2349, 1.662 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE6", "In the past 7 days", "When I was in pain I would lie down", "", 4.63048, new double[] { -0.3634, 0.3675, 0.7252, 1.2279, 1.7637 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE8", "In the past 7 days", "When I was in pain I moved extremely slowly", "", 5.71973, new double[] { -0.2939, 0.2927, 0.658, 1.1056, 1.5116 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      ),
      item("PAINBE9", "In the past 7 days", "When I was in pain I became angry", "", 5.09556, new double[] { -0.3174, 0.6877, 1.0387, 1.469, 1.8947 }, -1, "",
          response("Had no pain", 1),
          response("Never", 2),
          response("Rarely", 3),
          response("Sometimes", 4),
          response("Often", 5),
          response("Always", 6)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
