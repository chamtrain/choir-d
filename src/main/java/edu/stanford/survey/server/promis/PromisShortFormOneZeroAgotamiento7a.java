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
 * Item bank for PROMIS assessment. Generated from OID DA116543-78BA-4C5A-B824-B79977553F94.
 */
public class PromisShortFormOneZeroAgotamiento7a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("FATEXP18", "En los últimos 7 días", "¿Con qué frecuencia se quedó sin energía?", "", 3.38925, new double[] { -1.0135, 0.0387, 1.0796, 2.1624 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("FATEXP20", "En los últimos 7 días", "¿Con qué frecuencia se sintió cansado/a?", "", 3.10957, new double[] { -1.2274, -0.0753, 0.8494, 1.6576 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("FATEXP5", "En los últimos 7 días", "¿Con qué frecuencia sintió extenuación extrema?", "", 2.6615, new double[] { -0.1122, 0.839, 1.7173, 2.8349 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("FATIMP21", "En los últimos 7 días", "¿Con qué frecuencia sintió demasiado cansancio como para darse un baño o una ducha?", "", 2.41555, new double[] { -0.4706, 0.4104, 1.2536, 2.4243 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("FATIMP30", "En los últimos 7 días", "¿Con qué frecuencia sintió demasiado cansancio como para pensar con claridad?", "", 2.96527, new double[] { -0.1065, 0.8185, 1.8163, 3.0505 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("FATIMP33", "En los últimos 7 días", "¿Con qué frecuencia se vio limitado/a en el trabajo debido al agotamiento (incluya el trabajo en el hogar)?", "", 3.09254, new double[] { -0.5432, 0.3259, 1.3544, 2.3236 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("FATIMP40", "En los últimos 7 días", "¿Con qué frecuencia tuvo suficiente energía como para hacer ejercicio vigoroso?", "", 0.88981, new double[] { -2.72562, -0.93135, 0.6811, 2.5401 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
