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
 * Item bank for PROMIS assessment. Generated from OID AF545CE0-C6B1-4308-AC88-7F782EC67D7C.
 */
public class PromisShortFormOneZeroAgotamiento4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("An3", "En los últimos 7 días", "Tengo dificultad para <u>comenzar</u> las cosas porque estoy cansado/a", "", 4.34709, new double[] { -0.3111, 0.5041, 1.1796, 1.9585 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("FATEXP40", "En los últimos 7 días", "¿Qué tan agotado/a estuvo en promedio?", "", 4.17556, new double[] { -0.8828, 0.3275, 1.2048, 2.1884 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("FATEXP41", "En los últimos 7 días", "¿Qué tan rendido/a se sintió en promedio?", "", 4.32191, new double[] { -0.5302, 0.4511, 1.1503, 2.0368 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("HI7", "En los últimos 7 días", "Me siento agotado/a", "", 4.67165, new double[] { -0.7789, 0.1997, 0.7943, 1.5833 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
