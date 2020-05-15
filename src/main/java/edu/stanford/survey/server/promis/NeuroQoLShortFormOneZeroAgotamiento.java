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
 * Item bank for PROMIS assessment. Generated from OID 10CD3AF9-63D6-4DBE-96A3-DC9B47395D2C.
 */
public class NeuroQoLShortFormOneZeroAgotamiento {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQFTG02", "En los últimos 7 días", "Tuve que limitar mis actividades sociales debido al cansancio", "", 3.60876, new double[] { -0.7494, -0.1345, 0.7531, 1.9085 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG06", "En los últimos 7 días", "Estuve demasiado cansado/a para hacer mis tareas de la casa", "", 4.24433, new double[] { -0.9616, -0.2507, 0.6647, 1.6671 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG07", "En los últimos 7 días", "Estuve demasiado cansado/a para salir de casa", "", 3.94218, new double[] { -0.6045, 0.0461, 0.9439, 1.9105 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG10", "En los últimos 7 días", "Sentí frustración porque estaba demasiado cansado/a para hacer las cosas que quería hacer", "", 4.14659, new double[] { -0.7203, -0.2442, 0.4339, 1.1719 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG11", "En los últimos 7 días", "Me sentí sin energía", "", 4.57819, new double[] { -1.1769, -0.4209, 0.3346, 1.2972 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG13", "En los últimos 7 días", "Me sentí exhausto/a", "", 4.68427, new double[] { -0.9271, -0.255, 0.5969, 1.4212 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG14", "En los últimos 7 días", "Sentí cansancio", "", 3.99133, new double[] { -1.6421, -0.737, 0.3109, 1.3436 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG15", "En los últimos 7 días", "Sentí agotamiento", "", 4.52634, new double[] { -1.3033, -0.4729, 0.4058, 1.3654 }, -1, "",
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
