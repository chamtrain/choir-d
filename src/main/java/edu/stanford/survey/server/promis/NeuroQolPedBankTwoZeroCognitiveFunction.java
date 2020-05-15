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
 * Item bank for PROMIS assessment. Generated from OID 869BB22D-CAAB-470F-AE30-6AF33BC2BA9F.
 */
public class NeuroQolPedBankTwoZeroCognitiveFunction {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQCOGped02", "", "??I have a hard time keeping track of my homework", "", 2.63, new double[] { -2.04, -1.31, -0.69, 0.39 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped03", "", "I forget schoolwork that I need to do", "", 2.75, new double[] { -2.0, -1.34, -0.66, 0.38 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped04", "", "I forget to bring books or worksheets home that I need for homework??", "", 2.44, new double[] { -2.16, -1.45, -0.73, 0.39 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped05", "", "I sometimes forget what I was going to say", "", 2.18, new double[] { -2.49, -1.57, -0.72, 0.42 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped07", "", "I have to read something several times to understand it", "", 2.5, new double[] { -2.11, -1.28, -0.57, 0.51 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped08", "", "I react slower than most people my age when I play games", "", 2.41, new double[] { -2.28, -1.64, -0.94, -0.28 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped10", "", "It is hard for me to find the right words to say what I mean", "", 2.24, new double[] { -2.26, -1.5, -0.68, 0.26 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped14", "", "It takes me longer than other people to get my schoolwork done", "", 3.27, new double[] { -1.9, -1.24, -0.61, 0.14 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped15", "", "I forget things easily", "", 3.02, new double[] { -2.04, -1.45, -0.73, 0.12 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped16", "", "I have to use written lists more often than other people my age so I will not forget things", "", 2.45, new double[] { -2.22, -1.6, -0.98, -0.23 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped17", "", "I have trouble remembering to do things (e.g., school projects)", "", 3.74, new double[] { -2.01, -1.33, -0.74, 0.21 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped18", "", "It is hard for me to concentrate in school", "", 3.73, new double[] { -1.82, -1.22, -0.59, 0.4 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped19", "", "I have trouble paying attention to the teacher", "", 3.63, new double[] { -1.85, -1.22, -0.55, 0.37 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      ),
      item("NQCOGped20", "", "I have to work really hard to pay attention or I will make a mistake", "", 3.48, new double[] { -1.86, -1.11, -0.58, 0.24 }, -1, "",
          response("not at all", 5),
          response("a little bit", 4),
          response("somewhat", 3),
          response("quite a bit", 2),
          response("very much", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
