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
 * Item bank for PROMIS assessment. Generated from OID 70BC0109-CFEC-4030-A1CB-7D97B63C9681.
 */
public class NeuroQoLBancoOneZeroDepresin {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("EDDEP04", "En los últimos 7 días", "Sentí que no valía nada", "", 4.77346, new double[] { -0.0968, 0.2889, 1.0307, 1.6231 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP05", "En los últimos 7 días", "Sentí que nada me ilusionaba", "", 4.43495, new double[] { -0.2063, 0.3666, 0.8694, 1.5424 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP06", "En los últimos 7 días", "Me sentí indefenso/a (que no podía hacer nada para ayudarme)", "", 4.3185, new double[] { -0.2198, 0.3672, 0.9833, 1.5348 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP07", "En los últimos 7 días", "Me distancié de los demás", "", 3.47041, new double[] { -0.1968, 0.2768, 1.0306, 1.7071 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP09", "En los últimos 7 días", "Sentí que nada me podía animar", "", 4.66993, new double[] { -0.1071, 0.4488, 1.1186, 1.7643 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP17", "En los últimos 7 días", "Me sentí triste", "", 3.7147, new double[] { -0.7163, -0.0215, 0.7945, 1.5403 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP19", "En los últimos 7 días", "Sentí que quería darme por vencido/a en todo", "", 4.52366, new double[] { 0.0474, 0.4362, 1.0276, 1.6598 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP28", "En los últimos 7 días", "Me sentí solo/a", "", 3.6841, new double[] { -0.3198, 0.1887, 0.9226, 1.6472 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP29", "En los últimos 7 días", "Me sentí deprimido/a", "", 5.78729, new double[] { -0.3129, 0.2181, 0.9407, 1.417 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP31", "En los últimos 7 días", "Me sentí desanimado/a acerca del futuro", "", 3.99322, new double[] { -0.5207, 0.052, 0.6791, 1.3266 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP35", "En los últimos 7 días", "Descubrí que había cosas en mi vida que eran demasiado para mí", "", 3.44407, new double[] { -0.2809, 0.2478, 1.0303, 1.6802 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP36", "En los últimos 7 días", "Me sentí descontento/a", "", 4.69529, new double[] { -0.6883, 0.0064, 0.8354, 1.7386 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP39", "En los últimos 7 días", "Sentí que no tenía ninguna razón para vivir", "", 4.38096, new double[] { 0.379, 0.777, 1.3304, 1.9153 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP41", "En los últimos 7 días", "Me sentí desesperanzado/a", "", 5.24141, new double[] { 0.0199, 0.4926, 1.1465, 1.7244 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP45", "En los últimos 7 días", "Sentí que nada me interesaba", "", 4.12492, new double[] { -0.08, 0.4909, 1.2155, 1.9069 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP46", "En los últimos 7 días", "Me sentí pesimista (que veía las cosas de modo negativo)", "", 2.76073, new double[] { -0.4563, 0.2597, 1.0649, 1.793 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP48", "En los últimos 7 días", "Sentí que mi vida estaba vacía", "", 4.98996, new double[] { -0.0259, 0.3725, 1.0634, 1.6516 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP54", "En los últimos 7 días", "Me sentí completamente agotado emocionalmente", "", 3.58897, new double[] { -0.2845, 0.1688, 0.9415, 1.5427 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQDEP06", "En los últimos 7 días", "Me pareció que todo lo que hacía me suponía un esfuerzo", "", 2.66443, new double[] { -0.5417, 0.0809, 0.9176, 1.4952 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQDEP08", "En los últimos 7 días", "Me critiqué por mis errores", "", 2.66649, new double[] { -0.6697, -0.0625, 0.8761, 1.5897 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQDEP20", "En los últimos 7 días", "Sentí que no me querían", "", 3.22771, new double[] { -0.0802, 0.4298, 1.1598, 1.6957 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQDEP26", "En los últimos 7 días", "Tuve problemas para concentrarme en lo que estaba haciendo", "", 2.42462, new double[] { -0.5014, 0.234, 1.2949, 2.1432 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQDEP29", "En los últimos 7 días", "Sentí que necesitaba ayuda para tratar mi depresión", "", 3.25237, new double[] { 0.251, 0.6651, 1.175, 1.6306 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQDEP30", "En los últimos 7 días", "Tuve problemas para disfrutar de las cosas de las que disfrutaba antes", "", 3.88535, new double[] { -0.0955, 0.3903, 1.0775, 1.5755 }, -1, "",
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
