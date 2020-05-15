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
 * Item bank for PROMIS assessment. Generated from OID 6D67CEC1-45E1-4E4E-BA61-4EF8A0C4222E.
 */
public class NeuroQoLShortFormPedOneZeroAnsiedad {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQEMNped22", "En los últimos 7 días", "Sentí miedo de salir solo/a", "", 3.09737, new double[] { 0.2284, 0.8292, 1.7093, 2.2066 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped23", "En los últimos 7 días", "Sentirme preocupado/a me dificultó estar con mis amigos/as", "", 5.31528, new double[] { 0.2437, 0.748, 1.5426, 2.3122 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped24", "En los últimos 7 días", "Fue difícil hacer las tareas escolares porque estaba nervioso/a o preocupado/a", "", 4.46707, new double[] { 0.0623, 0.6312, 1.5263, 2.1384 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped26", "En los últimos 7 días", "Sentí miedo", "", 4.27429, new double[] { 0.0095, 0.7873, 1.8115, 2.226 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped28", "En los últimos 7 días", "Me preocupé cuando estaba en casa", "", 4.24311, new double[] { 0.2125, 0.9065, 1.8681, 2.4693 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped29", "En los últimos 7 días", "Me sentí preocupado/a", "", 3.63505, new double[] { -0.2681, 0.4697, 1.6297, 2.2317 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped43", "En los últimos 7 días", "Me preocupa que mi salud pueda empeorar", "", 3.95967, new double[] { 0.4059, 1.0616, 1.6345, 2.154 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQEMNped46", "En los últimos 7 días", "Me preocupo porque me vaya bien en la escuela", "", 1.9172, new double[] { -0.6203, 0.4684, 1.2674, 2.1276 }, -1, "",
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
