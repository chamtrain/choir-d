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
 * Item bank for PROMIS assessment. Generated from OID 2A08D88E-3582-41F5-BEC2-26A07E38E0CE.
 */
public class PromisShortFormOneZeroEfectosDelDolor6a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
      item("PAININ12", "En los últimos 7 días", "¿En qué medida el dolor interfirió en las actividades que hace habitualmente para divertirse?", "", 5.29892, new double[] { 0.1808, 0.8281, 1.2865, 1.8824 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ22", "En los últimos 7 días", "¿En qué medida el dolor interfirió en el trabajo en el hogar?", "", 5.3971, new double[] { 0.1709, 0.8366, 1.3266, 1.9589 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ31", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para participar en actividades sociales?", "", 5.90514, new double[] { 0.473, 1.0094, 1.5063, 2.077 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ34", "En los últimos 7 días", "¿En qué medida el dolor interfirió en sus tareas domésticas?", "", 4.92619, new double[] { 0.1771, 0.8447, 1.363, 1.9795 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ36", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para disfrutar de actividades sociales?", "", 5.69697, new double[] { 0.3417, 0.959, 1.4786, 2.036 }, -1, "",
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
