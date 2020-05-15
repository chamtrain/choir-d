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
 * Item bank for PROMIS assessment. Generated from OID 03956105-FF0D-444C-9F95-96415502F850.
 */
public class NeuroQoLBancoOneZeroCapacidadParaParticiparEnRolesYActividadesSociales {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQPRF01", "En los últimos 7 días", "Puedo cumplir con mis responsabilidades de familia", "", 3.87096, new double[] { -2.2799, -1.6637, -0.985, -0.3689 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF02", "En los últimos 7 días", "Tengo problemas para atender a las necesidades de mi familia", "", 2.97426, new double[] { -2.0635, -1.5778, -0.8416, -0.138 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF04", "En los últimos 7 días", "Tengo que limitar las actividades habituales con mi familia.", "", 3.52172, new double[] { -1.9306, -1.2489, -0.6491, -0.1808 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF05", "En los últimos 7 días", "Puedo realizar todas las actividades con mi familia que la gente espera que haga", "", 4.60756, new double[] { -1.8259, -1.2508, -0.7788, -0.227 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF06", "En los últimos 7 días", "Puedo realizar todas las actividades que quiero hacer con mi familia", "", 4.43707, new double[] { -1.7079, -1.1509, -0.6539, -0.1624 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF07", "En los últimos 7 días", "Puedo conservar mis amistades tanto como deseo", "", 4.17977, new double[] { -1.7506, -1.2415, -0.7497, -0.164 }, -1, "",
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
      item("NQPRF11", "En los últimos 7 días", "Puedo hacer todo lo que quiero por mis amigos/as", "", 5.90291, new double[] { -1.4736, -0.9608, -0.488, -0.012 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF12", "En los últimos 7 días", "Puedo realizar todas las actividades con mis amigos/as que la gente espera de mí", "", 6.37643, new double[] { -1.6044, -1.0047, -0.4923, -0.053 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF13", "En los últimos 7 días", "Me siento limitado/a en mi capacidad para visitar a mis amigos/as", "", 3.67329, new double[] { -1.4479, -0.9986, -0.4859, 0.0039 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF14", "En los últimos 7 días", "Puedo realizar todas las actividades que quiero con mis amigos/as", "", 5.44687, new double[] { -1.4727, -0.9511, -0.509, -0.0707 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF15", "En los últimos 7 días", "Me siento limitado/a en la cantidad de tiempo que tengo para visitar a mis amigos/as", "", 2.57152, new double[] { -1.6935, -1.056, -0.3698, 0.1659 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF16", "En los últimos 7 días", "Tengo que limitar lo que hago en casa para divertirme (por ejemplo, leer, escuchar música, etc.)", "", 2.3246, new double[] { -2.1058, -1.4885, -0.6597, -0.0023 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF17", "En los últimos 7 días", "Puedo cumplir con mis compromisos sociales", "", 5.47607, new double[] { -1.6737, -1.0828, -0.6184, -0.1237 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF18", "En los últimos 7 días", "Puedo realizar todas mis actividades de tiempo libre habituales", "", 4.67856, new double[] { -1.8149, -1.1445, -0.5925, -0.0539 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF19", "En los últimos 7 días", "Tengo que limitar mis aficiones o mis actividades de tiempo libre", "", 3.25184, new double[] { -1.6759, -1.0795, -0.4859, 0.1147 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF20", "En los últimos 7 días", "Puedo realizar mis aficiones o mis actividades de tiempo libre", "", 4.74556, new double[] { -1.7466, -1.1892, -0.5594, 0.0213 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF21", "En los últimos 7 días", "Puedo realizar todas las actividades comunitarias que quiero", "", 4.8628, new double[] { -1.4725, -0.9081, -0.4163, -0.0036 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF22", "En los últimos 7 días", "Puedo realizar todas las actividades de tiempo libre que la gente espera que haga", "", 5.77176, new double[] { -1.5587, -1.0287, -0.4822, 0.0302 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF23", "En los últimos 7 días", "Tengo que realizar mis aficiones o mis actividades de tiempo libre durante periodos más cortos de lo que acostumbro", "", 3.13026, new double[] { -1.5589, -0.9506, -0.3851, 0.2224 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF24", "En los últimos 7 días", "Tengo que limitar mis actividades sociales fuera de mi casa", "", 4.48516, new double[] { -1.4024, -0.9071, -0.4094, 0.094 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF25", "En los últimos 7 días", "Tengo dificultad para mantenerme en contacto con otras personas", "", 3.19098, new double[] { -1.8043, -1.2367, -0.5488, 0.0514 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF26", "En los últimos 7 días", "Puedo participar en actividades de tiempo libre", "", 5.00188, new double[] { -1.7589, -1.2822, -0.5052, 0.0281 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF27", "En los últimos 7 días", "Puedo realizar todas las actividades de tiempo libre que quiero", "", 5.33741, new double[] { -1.5544, -0.983, -0.4478, 0.0209 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF28", "En los últimos 7 días", "Puedo realizar todas las actividades comunitarias que la gente espera que haga", "", 5.08067, new double[] { -1.4371, -0.9007, -0.3843, 0.1416 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF29", "En los últimos 7 días", "Puedo salir a divertirme tanto como quiero", "", 3.67975, new double[] { -1.3908, -0.8294, -0.3461, 0.1865 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF31", "En los últimos 7 días", "Estoy participando en menos actividades sociales con grupos de gente de lo que acostumbro.", "", 3.45202, new double[] { -1.4275, -0.9485, -0.4077, 0.1227 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF32", "En los últimos 7 días", "Puedo desempeñar mis actividades de rutina diarias", "", 5.91819, new double[] { -1.7836, -1.3472, -0.7761, -0.3297 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF33", "En los últimos 7 días", "Puedo hacer mandados sin dificultad", "", 5.0874, new double[] { -1.5418, -1.2129, -0.684, -0.2549 }, -1, "",
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
      ),
      item("NQPRF35", "En los últimos 7 días", "Puedo realizar todo mi trabajo habitual (incluya el trabajo en el hogar)", "", 6.32861, new double[] { -1.5611, -1.1169, -0.6356, -0.1657 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF37", "En los últimos 7 días", "Estoy llevando a cabo todo el trabajo que habitualmente realizo (incluya el trabajo en el hogar)", "", 5.05495, new double[] { -1.529, -1.0554, -0.5586, -0.046 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF38", "En los últimos 7 días", "Mi capacidad de realizar mi trabajo es tan buena como es posible (incluya el trabajo en el hogar)", "", 4.24225, new double[] { -1.632, -1.2035, -0.641, -0.0875 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF39", "En los últimos 7 días", "Puedo realizar todas las tareas de trabajo que deseo (incluya el trabajo en el hogar)", "", 5.72968, new double[] { -1.463, -0.9969, -0.5159, -0.0084 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF40", "En los últimos 7 días", "Tengo dificultad para realizar mis tareas o quehaceres habituales", "", 5.21835, new double[] { -1.4962, -1.0252, -0.4818, 0.0326 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF41", "En los últimos 7 días", "Puedo realizar todo el trabajo que la gente espera de mí (incluya el trabajo en el hogar)", "", 6.1631, new double[] { -1.5364, -1.0891, -0.5258, -0.0376 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF42", "En los últimos 7 días", "Tengo limitaciones para hacer mi trabajo (incluya el trabajo en el hogar)", "", 4.74014, new double[] { -1.4319, -1.0316, -0.5321, 0.0049 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF43", "En los últimos 7 días", "Tengo que dedicar menos tiempo del que acostumbro a realizar mi trabajo (incluya el trabajo en el hogar)", "", 3.84003, new double[] { -1.396, -0.916, -0.4064, 0.1354 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF46", "En los últimos 7 días", "Puedo realizar todo mi trabajo habitual", "", 5.8095, new double[] { -1.4761, -1.0582, -0.5879, -0.1514 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF47", "En los últimos 7 días", "Tengo limitaciones para hacer mi trabajo", "", 4.69369, new double[] { -1.328, -0.9923, -0.4641, 0.017 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      ),
      item("NQPRF48", "En los últimos 7 días", "Puedo realizar todo el trabajo que la gente espera de mí", "", 5.55725, new double[] { -1.5035, -1.0822, -0.4939, -0.0724 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPRF49", "En los últimos 7 días", "Tengo que dedicar menos tiempo del que acostumbro a realizar mi trabajo (incluya el trabajo en el hogar)", "", 3.72442, new double[] { -1.4281, -0.9082, -0.3994, 0.0612 }, -1, "",
          response("Nunca", 5),
          response("Rara vez", 4),
          response("Algunas veces", 3),
          response("A menudo", 2),
          response("Siempre", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
