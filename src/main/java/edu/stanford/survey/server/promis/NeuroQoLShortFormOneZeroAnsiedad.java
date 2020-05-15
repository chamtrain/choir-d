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
 * Item bank for PROMIS assessment. Generated from OID DABF557A-D5B2-4E19-B16F-A7A06E33B87A.
 */
public class NeuroQoLShortFormOneZeroAnsiedad {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("EDANX18", "En los últimos 7 días", "Tuve sensaciones de pánico repentinas.", "", 3.44614, new double[] { 0.1956, 0.945, 1.5723, 2.2883 }, -1, "",
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
      item("NQANX07", "En los últimos 7 días", "Me sentí nervioso/a cuando se alteraba mi rutina normal", "", 3.00991, new double[] { -0.2979, 0.3943, 1.1602, 1.9086 }, -1, "",
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
