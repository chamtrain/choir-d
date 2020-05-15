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
 * Item bank for PROMIS assessment. Generated from OID A73781ED-F8FD-4823-9F10-E2F9ADD9C238.
 */
public class NeuroQoLShortFormOneZeroCapacidadParaParticiparEnRolesYActividadesSociales {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQPRF01", "En los últimos 7 días", "Puedo cumplir con mis responsabilidades de familia", "", 3.87096, new double[] { -2.2799, -1.6637, -0.985, -0.3689 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF08", "En los últimos 7 días", "Puedo socializar con mis amigos/as", "", 3.73361, new double[] { -1.7862, -1.1625, -0.524, -0.0805 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF09", "En los últimos 7 días", "Puedo realizar todas las actividades habituales con mis amigos/as", "", 5.27177, new double[] { -1.5401, -1.014, -0.5058, -0.0611 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF17", "En los últimos 7 días", "Puedo cumplir con mis compromisos sociales", "", 5.47607, new double[] { -1.6737, -1.0828, -0.6184, -0.1237 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF26", "En los últimos 7 días", "Puedo participar en actividades de tiempo libre", "", 5.00188, new double[] { -1.7589, -1.2822, -0.5052, 0.0281 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF32", "En los últimos 7 días", "Puedo desempeñar mis actividades de rutina diarias", "", 5.91819, new double[] { -1.7836, -1.3472, -0.7761, -0.3297 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF34", "En los últimos 7 días", "Puedo cumplir con mis responsabilidades de trabajo (incluya el trabajo en el hogar)", "", 5.63317, new double[] { -1.5789, -1.1749, -0.5997, -0.1874 }, -1, "",
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
