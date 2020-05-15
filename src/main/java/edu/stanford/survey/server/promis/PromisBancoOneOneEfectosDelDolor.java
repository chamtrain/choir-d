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
 * Item bank for PROMIS assessment. Generated from OID 74A6F57A-5CCC-4108-8B6D-DA56EC7E429F.
 */
public class PromisBancoOneOneEfectosDelDolor {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("PAININ1", "En los últimos 7 días", "¿Cuánta dificultad tuvo para entender información nueva debido al dolor?", "", 3.33832, new double[] { 0.8411, 1.3992, 2.0218, 2.6982 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ10", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para disfrutar de actividades recreativas?", "", 5.15317, new double[] { 0.131, 0.7914, 1.26, 1.8452 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ11", "En los últimos 7 días", "¿Con qué frecuencia sintió tensión emocional debido al dolor?", "", 3.73643, new double[] { 0.3303, 1.016, 1.4928, 2.1544 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ12", "En los últimos 7 días", "¿En qué medida el dolor interfirió en las actividades que hace habitualmente para divertirse?", "", 5.29892, new double[] { 0.1808, 0.8281, 1.2865, 1.8824 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ13", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su vida familiar?", "", 5.03924, new double[] { 0.4688, 1.0753, 1.6277, 2.1736 }, -1, "",
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
      item("PAININ16", "En los últimos 7 días", "¿Con qué frecuencia el dolor le hizo sentirse deprimido/a?", "", 3.18212, new double[] { 0.4199, 1.014, 1.7072, 2.2754 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ17", "En los últimos 7 días", "¿En qué medida el dolor interfirió en sus relaciones con otras personas?", "", 4.69522, new double[] { 0.5776, 1.19, 1.706, 2.3169 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ18", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para trabajar (incluya el trabajo en el hogar)?", "", 4.61543, new double[] { 0.2501, 0.9002, 1.3996, 1.8769 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ19", "En los últimos 7 días", "¿En qué medida el dolor le dificultó dormirse?", "", 2.83373, new double[] { 0.2328, 0.954, 1.4495, 2.0753 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ20", "En los últimos 7 días", "¿En qué medida sintió que el dolor era una carga para usted?", "", 4.28098, new double[] { 0.1079, 0.7623, 1.1885, 1.7206 }, -1, "",
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
      item("PAININ24", "En los últimos 7 días", "¿Con qué frecuencia se sintió afligido/a por el dolor?", "", 3.60354, new double[] { -0.0104, 0.6152, 1.3206, 2.0766 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ26", "En los últimos 7 días", "¿Con qué frecuencia el dolor le impidió socializar con otras personas?", "", 4.87755, new double[] { 0.578, 1.085, 1.676, 2.5191 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ29", "En los últimos 7 días", "¿Con qué frecuencia el dolor fue tan agudo que no pudo pensar en nada más?", "", 3.4297, new double[] { 0.637, 1.1396, 1.8218, 2.9205 }, -1, "",
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
      item("PAININ31", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para participar en actividades sociales?", "", 5.90514, new double[] { 0.473, 1.0094, 1.5063, 2.077 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ32", "En los últimos 7 días", "¿Con qué frecuencia el dolor le hizo sentirse desanimado/a?", "", 3.49408, new double[] { 0.1801, 0.7601, 1.4697, 2.2029 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ34", "En los últimos 7 días", "¿En qué medida el dolor interfirió en sus tareas domésticas?", "", 4.92619, new double[] { 0.1771, 0.8447, 1.363, 1.9795 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ35", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para hacer viajes desde su hogar que le obligaran a estar fuera durante más de 2 horas?", "", 4.14223, new double[] { 0.7493, 1.1849, 1.6303, 2.1296 }, -1, "",
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
      item("PAININ37", "En los últimos 7 días", "¿Con qué frecuencia el dolor le hizo sentirse ansioso/a?", "", 2.85286, new double[] { 0.3791, 1.0382, 1.7622, 2.5517 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ38", "En los últimos 7 días", "¿Con qué frecuencia evitó las actividades sociales porque podrían causarle más dolor?", "", 4.5601, new double[] { 0.563, 0.9911, 1.5776, 2.3206 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ40", "En los últimos 7 días", "¿Con qué frecuencia el dolor le impidió caminar más de 1 milla?", "", 2.98284, new double[] { 0.369, 0.7379, 1.1128, 1.5541 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ42", "En los últimos 7 días", "¿Con qué frecuencia el dolor le impidió estar de pie durante más de una hora?", "", 2.74006, new double[] { 0.402, 0.8057, 1.2324, 1.7405 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ46", "En los últimos 7 días", "¿Con qué frecuencia el dolor le dificultó planear actividades sociales?", "", 4.59, new double[] { 0.452, 0.9384, 1.5214, 2.1187 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ47", "En los últimos 7 días", "¿Con qué frecuencia el dolor le impidió estar de pie durante más de 30 minutos?", "", 2.74829, new double[] { 0.3449, 0.823, 1.3601, 1.9293 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ48", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para realizar tareas domésticas?", "", 5.7, new double[] { 0.36, 1.02, 1.6, 2.15 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ49", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para recordar cosas?", "", 3.07411, new double[] { 0.8847, 1.4167, 1.9753, 2.6075 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ5", "En los últimos 7 días", "¿En qué medida el dolor interfirió en su capacidad para participar en actividades durante su tiempo libre?", "", 5.05151, new double[] { 0.2379, 0.906, 1.445, 2.0389 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ50", "En los últimos 7 días", "¿Con qué frecuencia el dolor le impidió permanecer sentado/a durante más de 30 minutos?", "", 2.82948, new double[] { 0.7303, 1.2605, 1.8401, 2.5902 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ51", "En los últimos 7 días", "¿Con qué frecuencia el dolor le impidió permanecer sentado/a durante más de 10 minutos?", "", 2.52538, new double[] { 1.0408, 1.6646, 2.3444, 3.113 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ52", "En los últimos 7 días", "¿Con qué frecuencia le resultó difícil planear actividades sociales por no saber si tendría dolor?", "", 4.65444, new double[] { 0.6511, 1.073, 1.5559, 2.0063 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ53", "En los últimos 7 días", "¿Con qué frecuencia el dolor limitó su vida social al hogar?", "", 4.13798, new double[] { 0.5115, 0.999, 1.5786, 2.3259 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ54", "En los últimos 7 días", "¿Con qué frecuencia el dolor le impidió ponerse de pie?", "", 2.20125, new double[] { 1.1017, 1.5676, 1.9664, 2.293 }, -1, "",
          response("Nunca", 1),
          response("Una vez a la semana o menos", 2),
          response("Una vez cada pocos días", 3),
          response("Una vez al día", 4),
          response("Una vez cada pocas horas", 5)
      ),
      item("PAININ55", "En los últimos 7 días", "¿Con qué frecuencia el dolor le impidió permanecer sentado/a durante más de una hora?", "", 2.68327, new double[] { 0.7217, 1.2032, 1.7465, 2.4084 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("PAININ56", "En los últimos 7 días", "¿En qué medida se sintió irritable debido al dolor?", "", 3.08298, new double[] { 0.0573, 0.9293, 1.588, 2.2141 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("PAININ6", "En los últimos 7 días", "¿En qué medida el dolor interfirió en sus relaciones personales cercanas?", "", 4.05753, new double[] { 0.6262, 1.1746, 1.6664, 2.1984 }, -1, "",
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
