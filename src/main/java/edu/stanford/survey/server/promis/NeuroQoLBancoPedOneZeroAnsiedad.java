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
 * Item bank for PROMIS assessment. Generated from OID 3BBD1D6B-A249-4DEA-B9B8-05675AAFA2A2.
 */
public class NeuroQoLBancoPedOneZeroAnsiedad {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQEMNped02", "En los últimos 7 días", "Me pongo ansioso/a cuando regreso al hospital o la clínica", "", 1.68695, new double[] { 0.3259, 1.299, 1.9864, 2.7889 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQEMNped03", "En los últimos 7 días", "Me preocupa cómo afectará mi salud a mi futuro", "", 1.99849, new double[] { 0.1191, 1.042, 1.6749, 2.4915 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQEMNped06", "En los últimos 7 días", "A causa de mi salud, me preocupa tener un novio o una novia", "", 2.43757, new double[] { 0.4307, 0.9457, 1.4653, 2.1525 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQEMNped10", "En los últimos 7 días", "Me preocupa si podré conseguir un buen trabajo debido a mi condición médica", "", 2.89538, new double[] { 0.5698, 1.0452, 1.5512, 1.9735 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQEMNped20", "En los últimos 7 días", "Me pongo nervioso/a con más facilidad que otras personas", "", 2.85736, new double[] { -0.2015, 0.7765, 1.4498, 2.3593 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQEMNped21", "En los últimos 7 días", "Me preocupé cuando estaba lejos de mi familia", "", 2.82862, new double[] { -0.1294, 0.6452, 1.4433, 2.1902 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
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
      item("NQEMNped25", "En los últimos 7 días", "Me asusté con facilidad", "", 3.74236, new double[] { 0.1121, 0.8848, 1.7364, 2.2561 }, -1, "",
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
      item("NQEMNped27", "En los últimos 7 días", "Me preocupó la posibilidad de morir", "", 3.58471, new double[] { 0.5292, 1.1316, 1.8748, 2.3995 }, -1, "",
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
      item("NQEMNped30", "En los últimos 7 días", "Me sentí nervioso/a", "", 3.82646, new double[] { -0.3677, 0.3933, 1.5245, 2.3004 }, -1, "",
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
      item("NQEMNped44", "En los últimos 7 días", "Debido a mi salud, me preocupa si podré ir a la universidad", "", 3.26346, new double[] { 0.5305, 1.0641, 1.6022, 1.9929 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQEMNped45", "En los últimos 7 días", "Debido a mi salud, me preocupa si podré conseguir un trabajo para poder mantenerme", "", 3.53959, new double[] { 0.3928, 0.9904, 1.4153, 1.8819 }, -1, "",
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
