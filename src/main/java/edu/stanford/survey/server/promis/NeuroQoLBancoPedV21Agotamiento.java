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
 * Item bank for PROMIS assessment. Generated from OID F7CF8A91-DBAB-426E-89C6-12800999ECA9.
 */
public class NeuroQoLBancoPedV21Agotamiento {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 11, 3.0,
      item("NQFTGped01", "En los últimos 7 días", "Sentí cansancio", "", 1.98, new double[] { -1.44, 0.16, 1.34, 2.21 }, -1, "",
          response("nunca", 1),
          response("pocas veces", 2),
          response(" algunas veces", 3),
          response("la mayoría de las veces", 4),
          response("siempre", 5)
      ),
      item("NQFTGpeds04", "En los últimos 7 días", "Tuve dificultad para comenzar las cosas porque estaba demasiado cansado/a", "", 2.79, new double[] { -0.47, 0.58, 1.45, 2.07 }, -1, "",
          response("nunca", 1),
          response("pocas veces", 2),
          response(" algunas veces", 3),
          response("la mayoría de las veces", 4),
          response("siempre", 5)
      ),
      item("NQFTGpeds05", "En los últimos 7 días", "Tuve dificultad para terminar las cosas porque estaba demasiado cansado/a", "", 2.64, new double[] { -0.57, 0.53, 1.46, 2.54 }, -1, "",
          response("nunca", 1),
          response("pocas veces", 2),
          response(" algunas veces", 3),
          response("la mayoría de las veces", 4),
          response("siempre", 5)
      ),
      item("NQFTGpeds06", "En los últimos 7 días", "Tuve que dormir durante el día", "", 2.83, new double[] { -0.18, 0.61, 1.39, 2.11 }, -1, "",
          response("nunca", 1),
          response("pocas veces", 2),
          response(" algunas veces", 3),
          response("la mayoría de las veces", 4),
          response("siempre", 5)
      ),
      item("NQFTGpeds07", "En los últimos 7 días", "Estuve frustrado/a porque estaba demasiado cansado/a para hacer las cosas que quería hacer", "", 3.54, new double[] { -0.01, 0.71, 1.4, 2.08 }, -1, "",
          response("nunca", 1),
          response("pocas veces", 2),
          response(" algunas veces", 3),
          response("la mayoría de las veces", 4),
          response("siempre", 5)
      ),
      item("NQFTGpeds08", "En los últimos 7 días", "Como estaba cansado/a, me resultó difícil jugar o salir con mis amigos/as tanto como me habría gustado", "", 3.62, new double[] { 0.09, 0.71, 1.48, 2.16 }, -1, "",
          response("nunca", 1),
          response("pocas veces", 2),
          response(" algunas veces", 3),
          response("la mayoría de las veces", 4),
          response("siempre", 5)
      ),
      item("NQFTGpeds09", "En los últimos 7 días", "Necesité ayuda para hacer las cosas habituales en casa", "", 2.51, new double[] { 0.05, 0.92, 1.79, 2.73 }, -1, "",
          response("nunca", 1),
          response("pocas veces", 2),
          response(" algunas veces", 3),
          response("la mayoría de las veces", 4),
          response("siempre", 5)
      ),
      item("NQFTGpeds10", "En los últimos 7 días", "Sentí debilidad", "", 3.73, new double[] { 0.26, 0.94, 1.63, 1.99 }, -1, "",
          response("nunca", 1),
          response("pocas veces", 2),
          response(" algunas veces", 3),
          response("la mayoría de las veces", 4),
          response("siempre", 5)
      ),
      item("NQFTGpeds11r1", "En los últimos 7 días", "Estuve demasiado cansado/a para comer", "", 3.44, new double[] { 0.54, 1.13, 1.79, 2.48 }, -1, "",
          response("nunca", 1),
          response("pocas veces", 2),
          response("algunas veces", 3),
          response("la mayoría de las veces", 4),
          response("siempre", 5)
      ),
      item("NQFTGpeds12", "En los últimos 7 días", "Sentirme cansado/a me entristece", "", 3.72, new double[] { 0.29, 0.84, 1.48, 2.05 }, -1, "",
          response("nunca", 1),
          response("pocas veces", 2),
          response(" algunas veces", 3),
          response("la mayoría de las veces", 4),
          response("siempre", 5)
      ),
      item("NQFTGpeds13", "En los últimos 7 días", "Sentirme cansado/a me enoja", "", 3.26, new double[] { 0.46, 0.97, 1.54, 2.2 }, -1, "",
          response("nunca", 1),
          response("pocas veces", 2),
          response(" algunas veces", 3),
          response("la mayoría de las veces", 4),
          response("siempre", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
