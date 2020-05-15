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
 * Item bank for PROMIS assessment. Generated from OID 90980DDD-074E-4237-85B1-1C96C9DBCDEF.
 */
public class PromisShortFormOneZeroEfectosDelDolor6b {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
      item("PAININ10", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para disfrutar de actividades recreativas?", "", 5.15317, new double[] { 0.131, 0.7914, 1.26, 1.8452 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ14", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para realizar tareas fuera del hogar (p. ej., hacer la compra o los mandados)?", "", 4.83004, new double[] { 0.4265, 0.9994, 1.4568, 2.044 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ26", "En los últimos 7 días", "¿Con qué frecuencia el dolor le impidió socializar con otras personas?", "", 4.87755, new double[] { 0.578, 1.085, 1.676, 2.5191 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ3", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para disfrutar de la vida?", "", 4.98134, new double[] { 0.1293, 0.8809, 1.3825, 1.9123 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ8", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para concentrarse?", "", 3.74877, new double[] { 0.3996, 1.1057, 1.6892, 2.3425 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ9", "En los últimos 7 días", "¿En qué medida el dolor interfirió en sus actividades diarias?", "", 6.53406, new double[] { 0.1579, 0.8959, 1.4377, 2.0103 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
