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
 * Item bank for PROMIS assessment. Generated from OID 126114D4-129E-48B6-A0D5-389801B26A89.
 */
public class NeuroQoLShortFormOneZeroTrastornosDelSueo {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQSLP02", "En los últimos 7 días", "Tuve que obligarme a levantarme por la mañana", "", 1.58794, new double[] { -0.5879, 0.3225, 1.3287, 2.2905 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSLP03", "En los últimos 7 días", "Tuve problemas para dejar de pensar a la hora de dormir", "", 2.30312, new double[] { -0.5896, 0.1447, 1.0281, 2.0049 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSLP04", "En los últimos 7 días", "Tuve sueño durante el día", "", 1.60035, new double[] { -1.821, -0.7657, 0.6945, 1.9469 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSLP05", "En los últimos 7 días", "Tuve problemas para dormir a causa de las pesadillas", "", 1.66502, new double[] { 0.5271, 1.5699, 2.5337, 3.5181 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSLP07", "En los últimos 7 días", "Tuve dificultad para quedarme dormido/a", "", 2.23852, new double[] { -0.6242, 0.2845, 1.2643, 2.1503 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSLP12", "En los últimos 7 días", "El dolor me despertó", "", 1.33617, new double[] { 0.054, 0.8407, 2.0007, 3.4493 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSLP13", "En los últimos 7 días", "Evité o cancelé actividades con mis amigos porque estaba cansado/a debido a la mala noche que había pasado", "", 2.46753, new double[] { 0.4991, 1.1223, 2.0856, 2.9696 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSLP18", "En los últimos 7 días", "Me sentí físicamente tenso/a en medio de la noche o en la madrugada", "", 1.80312, new double[] { 0.5693, 1.1263, 2.3135, 3.7588 }, -1, "",
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
