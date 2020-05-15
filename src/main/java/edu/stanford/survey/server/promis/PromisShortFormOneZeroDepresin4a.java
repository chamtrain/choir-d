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
 * Item bank for PROMIS assessment. Generated from OID FCCEECE7-22E9-4B58-B804-C582282B4F87.
 */
public class PromisShortFormOneZeroDepresin4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("EDDEP04", "En los últimos 7 días", "Sentí que no valía nada", "", 4.26142, new double[] { 0.4011, 0.9757, 1.6963, 2.4441 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP06", "En los últimos 7 días", "Me sentí indefenso/a (que no podía hacer nada para ayudarme)", "", 4.14476, new double[] { 0.3501, 0.9153, 1.6782, 2.4705 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP29", "En los últimos 7 días", "Me sentí deprimido/a", "", 4.34292, new double[] { -0.1173, 0.5977, 1.4282, 2.2725 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP41", "En los últimos 7 días", "Me sentí desesperanzado/a", "", 3.24244, new double[] { -0.0915, 0.7333, 1.583, 2.4995 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
