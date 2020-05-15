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
 * Item bank for PROMIS assessment. Generated from OID A0F1475C-F9C3-407C-BB24-8CF895C0DCC3.
 */
public class PromisShortFormOneOneEnojo5a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 5, 3.0,
      item("EDANG03", "En los últimos 7 días", "Sentí más irritación de la que demostré", "", 2.24763, new double[] { -0.32, 0.777, 1.8744, 2.9198 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANG09", "En los últimos 7 días", "Me sentí enojado/a", "", 2.82932, new double[] { -0.826, 0.3331, 1.6847, 2.9031 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANG15", "En los últimos 7 días", "Sentí como si estuviera a punto de explotar", "", 2.82094, new double[] { 0.2872, 1.0593, 1.9753, 3.0347 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANG30", "En los últimos 7 días", "Me sentí malhumorado/a", "", 2.98863, new double[] { -0.8157, 0.2649, 1.5402, 2.8419 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANG35", "En los últimos 7 días", "Me sentí molesto/a", "", 2.21677, new double[] { -1.0126, 0.5528, 2.0013, 3.184 }, -1, "",
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
