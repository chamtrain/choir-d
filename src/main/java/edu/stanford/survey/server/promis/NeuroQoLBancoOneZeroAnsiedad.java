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
 * Item bank for PROMIS assessment. Generated from OID B5A36C02-7C33-496D-BAE9-CB9674E82407.
 */
public class NeuroQoLBancoOneZeroAnsiedad {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("EDANX05", "En los últimos 7 días", "Sentí ansiedad", "", 3.05854, new double[] { -0.7377, 0.0336, 0.9391, 1.7192 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX07", "En los últimos 7 días", "Sentí que necesitaba ayuda para mi ansiedad", "", 2.93979, new double[] { 0.126, 0.68, 1.434, 1.9685 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX18", "En los últimos 7 días", "Tuve sensaciones de pánico repentinas.", "", 3.44614, new double[] { 0.1956, 0.945, 1.5723, 2.2883 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX20", "En los últimos 7 días", "Me sobresalté (asusté) fácilmente", "", 2.07768, new double[] { -0.2514, 0.6111, 1.4756, 2.2568 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX26", "En los últimos 7 días", "Me sentí inquieto/a", "", 2.96216, new double[] { -0.267, 0.43, 1.2885, 1.9618 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX27", "En los últimos 7 días", "Sentí que iba a ocurrir algo terrible", "", 3.23614, new double[] { -0.0085, 0.6078, 1.3955, 2.0268 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX30", "En los últimos 7 días", "Me sentí preocupado/a", "", 3.01128, new double[] { -0.8232, 0.015, 0.8987, 1.5664 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX41", "En los últimos 7 días", "Mis inquietudes fueron demasiado para mí", "", 3.99181, new double[] { 0.1026, 0.6641, 1.2992, 1.9074 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX46", "En los últimos 7 días", "Me sentí nervioso/a", "", 4.29395, new double[] { -0.3869, 0.3746, 1.0974, 1.766 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX48", "En los últimos 7 días", "Muchas situaciones me preocuparon", "", 4.36415, new double[] { -0.3453, 0.4511, 1.0694, 1.6314 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX49", "En los últimos 7 días", "Tuve dificultad para dormir", "", 1.52153, new double[] { -0.7661, 0.065, 0.9806, 1.8113 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX51", "En los últimos 7 días", "Me costó trabajo relajarme", "", 2.95388, new double[] { -0.4777, 0.2946, 1.0536, 1.8134 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX53", "En los últimos 7 días", "Me sentí intranquilo/a", "", 5.52022, new double[] { -0.3203, 0.4249, 1.0894, 1.7105 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX54", "En los últimos 7 días", "Me sentí tenso/a", "", 4.06713, new double[] { -0.4382, 0.2261, 1.0636, 1.6983 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX55", "En los últimos 7 días", "Tuve dificultad para tranquilizarme", "", 3.2996, new double[] { -0.0276, 0.6604, 1.4124, 2.0035 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQANX02", "En los últimos 7 días", "Sentí miedo del futuro", "", 2.33805, new double[] { -0.7343, 0.1357, 0.882, 1.6937 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQANX04", "En los últimos 7 días", "Me preocupó mi salud física", "", 1.39548, new double[] { -1.0502, -0.0324, 1.1028, 2.1702 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQANX07", "En los últimos 7 días", "Me sentí nervioso/a cuando se alteraba mi rutina normal", "", 3.00991, new double[] { -0.2979, 0.3943, 1.1602, 1.9086 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQANX17", "En los últimos 7 días", "De repente sentí miedo sin ningún motivo", "", 2.46306, new double[] { 0.7454, 1.3098, 2.0253, 2.558 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQANX18", "En los últimos 7 días", "Me preocupó morir", "", 1.64134, new double[] { 0.4759, 1.2325, 2.3264, 2.8897 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQANX21", "En los últimos 7 días", "Me sentí tímido/a", "", 1.63541, new double[] { -0.1843, 0.7256, 1.5224, 2.2499 }, -1, "",
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
