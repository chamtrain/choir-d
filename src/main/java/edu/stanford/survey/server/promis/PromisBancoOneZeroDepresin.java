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
 * Item bank for PROMIS assessment. Generated from OID 3FD9AA9A-A4B6-4B7E-8F0E-65C639351AD4.
 */
public class PromisBancoOneZeroDepresin {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("EDDEP04", "En los últimos 7 días", "Sentí que no valía nada", "", 4.26142, new double[] { 0.4011, 0.9757, 1.6963, 2.4441 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP05", "En los últimos 7 días", "Sentí que nada me ilusionaba", "", 3.93174, new double[] { 0.3049, 0.9131, 1.5935, 2.4117 }, -1, "",
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
      item("EDDEP07", "En los últimos 7 días", "Me distancié de los demás", "", 2.8018, new double[] { 0.1477, 0.7723, 1.6027, 2.5381 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP09", "En los últimos 7 días", "Sentí que nada me podía animar", "", 3.65743, new double[] { 0.312, 0.9818, 1.7821, 2.5711 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP14", "En los últimos 7 días", "Sentí que no valía tanto como otras personas", "", 2.33338, new double[] { 0.186, 0.9473, 1.7288, 2.6326 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP17", "En los últimos 7 días", "Me sentí triste", "", 3.27403, new double[] { -0.4985, 0.4059, 1.4131, 2.3755 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP19", "En los últimos 7 días", "Sentí que quería darme por vencido/a en todo", "", 3.4886, new double[] { 0.1101, 0.9414, 1.6451, 2.8386 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP21", "En los últimos 7 días", "Sentí que tenía la culpa de lo que pasaba", "", 2.7361, new double[] { 0.0725, 0.8098, 1.8031, 2.6734 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP22", "En los últimos 7 días", "Me sentí fracasado/a", "", 3.97003, new double[] { 0.2038, 0.7955, 1.6487, 2.2955 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP23", "En los últimos 7 días", "Me costó  trabajo sentir una relación estrecha (cercana) con la gente", "", 2.56443, new double[] { -0.0384, 0.6927, 1.6528, 2.5836 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP26", "En los últimos 7 días", "Me sentí decepcionado/a de mí mismo/a", "", 3.09337, new double[] { -0.3576, 0.4125, 1.4039, 2.224 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP27", "En los últimos 7 días", "Sentí que nadie me necesitaba", "", 2.92006, new double[] { 0.2043, 0.8909, 1.6547, 2.5284 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP28", "En los últimos 7 días", "Me sentí solo/a", "", 2.58834, new double[] { -0.0791, 0.6326, 1.4773, 2.3277 }, -1, "",
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
      item("EDDEP30", "En los últimos 7 días", "Tuve problemas para tomar decisiones", "", 2.61285, new double[] { -0.0234, 0.8684, 1.8643, 2.8263 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP31", "En los últimos 7 días", "Me sentí desanimado/a acerca del futuro", "", 3.18287, new double[] { -0.2609, 0.3968, 1.3055, 2.134 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP35", "En los últimos 7 días", "Descubrí que había cosas en mi vida que eran demasiado para mí", "", 3.10586, new double[] { 0.0437, 0.7224, 1.6388, 2.4715 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP36", "En los últimos 7 días", "Me sentí descontento/a", "", 3.48301, new double[] { -0.5359, 0.3476, 1.3468, 2.3548 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP39", "En los últimos 7 días", "Sentí que no tenía ninguna razón para vivir", "", 3.1096, new double[] { 0.5433, 1.0889, 1.7981, 2.6089 }, -1, "",
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
      ),
      item("EDDEP42", "En los últimos 7 días", "Sentí que la gente no me hacía caso", "", 2.36441, new double[] { 0.2101, 0.9871, 1.9059, 2.9338 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP44", "En los últimos 7 días", "Me sentí angustiado/a sin ningún motivo", "", 2.54916, new double[] { 0.1935, 1.0117, 2.0131, 3.1265 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP45", "En los últimos 7 días", "Sentí que nada me interesaba", "", 2.8336, new double[] { 0.1407, 0.9065, 1.8461, 2.8752 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP46", "En los últimos 7 días", "Me sentí pesimista (que veía las cosas de modo negativo)", "", 2.38063, new double[] { -0.4579, 0.478, 1.5457, 2.6315 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP48", "En los últimos 7 días", "Sentí que mi vida estaba vacía", "", 3.18524, new double[] { 0.1981, 0.7819, 1.5258, 2.3241 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP50", "En los últimos 7 días", "Me sentí culpable", "", 2.0181, new double[] { -0.0504, 0.9259, 1.9995, 2.9655 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP54", "En los últimos 7 días", "Me sentí completamente agotado emocionalmente", "", 2.6853, new double[] { -0.2988, 0.4235, 1.3579, 2.3076 }, -1, "",
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
