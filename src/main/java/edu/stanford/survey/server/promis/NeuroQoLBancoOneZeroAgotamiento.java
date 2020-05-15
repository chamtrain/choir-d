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
 * Item bank for PROMIS assessment. Generated from OID 2C763554-78CA-4B27-B97E-49813DF9DF19.
 */
public class NeuroQoLBancoOneZeroAgotamiento {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQFTG01", "En los últimos 7 días", "Necesité ayuda para hacer mis actividades habituales debido al agotamiento", "", 2.71903, new double[] { -0.6824, 0.003, 0.9387, 1.8593 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG02", "En los últimos 7 días", "Tuve que limitar mis actividades sociales debido al cansancio", "", 3.60876, new double[] { -0.7494, -0.1345, 0.7531, 1.9085 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG03", "En los últimos 7 días", "Tuve que dormir durante el día", "", 1.88783, new double[] { -1.2046, -0.4079, 0.8369, 1.8845 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG04", "En los últimos 7 días", "Tuve dificultad para comenzar las cosas porque estaba demasiado cansado/a", "", 3.84357, new double[] { -0.9201, -0.249, 0.825, 1.8828 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG05", "En los últimos 7 días", "Tuve dificultad para terminar las cosas porque estaba demasiado cansado/a", "", 3.73878, new double[] { -1.0477, -0.2963, 0.7967, 1.9179 }, -1, "",
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
      item("NQFTG08", "En los últimos 7 días", "Estuve demasiado cansado/a para dar un paseo corto", "", 2.97454, new double[] { -0.6841, -0.0853, 0.6893, 1.5742 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG09", "En los últimos 7 días", "Estuve demasiado cansado/a para comer", "", 2.71085, new double[] { -0.1995, 0.6916, 1.8149, 2.7211 }, -1, "",
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
      item("NQFTG12", "En los últimos 7 días", "Estuve tan cansado/a que necesité descansar durante el día", "", 3.52273, new double[] { -1.1087, -0.3817, 0.6168, 1.4201 }, -1, "",
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
      ),
      item("NQFTG16", "En los últimos 7 días", "Sentí debilidad en todo el cuerpo", "", 3.13259, new double[] { -0.6647, 0.0435, 0.8894, 1.688 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG17", "En los últimos 7 días", "Necesité ayuda para hacer mis actividades habituales debido a la debilidad", "", 3.30362, new double[] { -0.2687, 0.3564, 1.2033, 2.0878 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG18", "En los últimos 7 días", "Tuve que limitar mis actividades sociales debido a la debilidad física", "", 3.28957, new double[] { -0.2825, 0.3586, 1.0424, 1.8479 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQFTG20", "En los últimos 7 días", "Tuve que obligarme a levantarme y hacer cosas porque estaba demasiado débil físicamente", "", 3.15038, new double[] { -0.3644, 0.2649, 1.0437, 2.0103 }, -1, "",
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
