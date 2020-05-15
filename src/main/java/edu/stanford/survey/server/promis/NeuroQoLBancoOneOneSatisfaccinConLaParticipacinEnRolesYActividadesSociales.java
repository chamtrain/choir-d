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
 * Item bank for PROMIS assessment. Generated from OID 971B5C75-871E-4F53-BF69-F0E276EFAC5E.
 */
public class NeuroQoLBancoOneOneSatisfaccinConLaParticipacinEnRolesYActividadesSociales {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQSAT01", "En los últimos 7 días", "Me parece que mi familia está decepcionada con mi capacidad de socializar con ellos", "", 3.44117, new double[] { -1.688, -1.3527, -0.794, -0.3364 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT02", "En los últimos 7 días", "Estoy decepcionado/a con mi capacidad de atender a las necesidades de mi familia", "", 4.02613, new double[] { -1.4656, -1.0524, -0.674, -0.2563 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT03", "En los últimos 7 días", "Me molestan mis limitaciones en las actividades de familia habituales", "", 4.91734, new double[] { -1.3877, -0.9496, -0.6424, -0.3162 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT04", "En los últimos 7 días", "Me siento bien acerca de mi capacidad de hacer cosas por mi familia", "", 3.59176, new double[] { -1.3291, -1.0004, -0.5435, 0.0141 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT05", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de atender a las necesidades de los que dependen de mí", "", 5.14874, new double[] { -1.2291, -0.8862, -0.5418, -0.0344 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT08", "En los últimos 7 días", "Estoy satisfecho/a con mi nivel actual de actividades con los miembros de mi familia", "", 4.95377, new double[] { -1.2093, -0.9376, -0.4027, 0.0648 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT10", "En los últimos 7 días", "Me parece que mis amigos/as están decepcionados/as con mi capacidad de socializar con ellos/as", "", 3.46607, new double[] { -1.7084, -1.3281, -0.8489, -0.449 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT11", "En los últimos 7 días", "Estoy decepcionado/a con mi capacidad de atender a las necesidades de mis amigos/as", "", 4.71696, new double[] { -1.4915, -1.1183, -0.6988, -0.3722 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT12", "En los últimos 7 días", "Estoy decepcionado/a con mi capacidad de hacer cosas por mis amigos/as", "", 4.60318, new double[] { -1.463, -1.0875, -0.6805, -0.3035 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT13", "En los últimos 7 días", "Estoy decepcionado/a con mi capacidad de socializar con mis amigos/as", "", 4.2474, new double[] { -1.5086, -1.1097, -0.7373, -0.3584 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT14", "En los últimos 7 días", "Me molestan mis limitaciones en las actividades habituales con mis amigos/as", "", 4.78275, new double[] { -1.4742, -1.0535, -0.6893, -0.2994 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT15", "En los últimos 7 días", "Estoy decepcionado/a con mi capacidad de mantener el contacto con otras personas", "", 3.61494, new double[] { -1.6539, -1.1803, -0.7345, -0.2538 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT18", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad para hacer cosas por mis amigos/as", "", 4.85975, new double[] { -1.1959, -0.7877, -0.3076, 0.1212 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT19", "En los últimos 7 días", "Estoy contento/a con cuánto hago por mis amigos/as", "", 4.17953, new double[] { -1.1507, -0.7715, -0.2652, 0.2158 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT20", "En los últimos 7 días", "Estoy satisfecho/a con mi nivel actual de actividades con mis amigos/as", "", 4.87261, new double[] { -1.087, -0.7092, -0.2769, 0.1617 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT21", "En los últimos 7 días", "Estoy satisfecho/a con la cantidad de tiempo que paso visitando a mis amigos/as", "", 3.63374, new double[] { -1.0773, -0.6945, -0.2111, 0.278 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT22", "En los últimos 7 días", "Me parece que la gente está decepcionada con mi capacidad de realizar actividades comunitarias", "", 2.78435, new double[] { -1.7999, -1.4247, -0.9367, -0.4796 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT23", "En los últimos 7 días", "Estoy decepcionado/a con mi capacidad de socializar con mi familia", "", 4.09645, new double[] { -1.4386, -1.0998, -0.7227, -0.3443 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT24", "En los últimos 7 días", "Estoy decepcionado/a con mi capacidad de realizar actividades de tiempo libre", "", 5.10245, new double[] { -1.3542, -0.9944, -0.6692, -0.2797 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT25", "En los últimos 7 días", "Me molestan mis limitaciones para realizar mis aficiones o actividades de tiempo libre", "", 4.1824, new double[] { -1.3553, -0.9991, -0.6403, -0.2249 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT27", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad para divertirme en casa (leyendo, escuchando música, etc.)", "", 3.0201, new double[] { -1.5475, -1.1436, -0.5937, -0.0922 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT29", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de realizar actividades de tiempo libre", "", 4.74385, new double[] { -1.2707, -0.8269, -0.387, 0.062 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT30", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de realizar todas las actividades de tiempo libre que son verdaderamente importantes para mí", "", 5.13581, new double[] { -1.2053, -0.863, -0.4088, 0.0406 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT31", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de realizar todas las actividades comunitarias que son verdaderamente importantes para mí", "", 3.8364, new double[] { -1.1684, -0.7657, -0.2847, 0.1036 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT32", "En los últimos 7 días", "Estoy satisfecho/a con la cantidad de tiempo que paso realizando actividades de tiempo libre", "", 4.55839, new double[] { -1.317, -0.8891, -0.3175, 0.0922 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT33", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de hacer cosas para divertirme fuera de mi casa", "", 5.22911, new double[] { -1.0572, -0.7345, -0.3049, 0.1093 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT34", "En los últimos 7 días", "Estoy satisfecho/a con mi nivel actual de actividades sociales", "", 4.44238, new double[] { -1.1201, -0.7686, -0.3078, 0.1328 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT35", "En los últimos 7 días", "Me parece que estoy decepcionando a otras personas en el trabajo", "", 2.67136, new double[] { -1.8849, -1.6043, -1.1884, -0.8906 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT36", "En los últimos 7 días", "Estoy decepcionado/a con mi capacidad para desempeñar mis actividades de rutina diarias", "", 5.19369, new double[] { -1.3297, -1.0529, -0.7935, -0.4128 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT37", "En los últimos 7 días", "Estoy decepcionado/a con mi capacidad de trabajar (incluya el trabajo en el hogar)", "", 5.21751, new double[] { -1.3305, -1.0135, -0.7589, -0.418 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT38", "En los últimos 7 días", "Me molestan mis limitaciones para desempeñar mis actividades de rutina diarias", "", 5.47056, new double[] { -1.3164, -0.9815, -0.623, -0.2828 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT39", "En los últimos 7 días", "Estoy decepcionado/a con mi capacidad de llevar a cabo mis responsabilidades personales y domésticas", "", 5.76723, new double[] { -1.3569, -1.0373, -0.6656, -0.3165 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT40", "En los últimos 7 días", "Me molestan mis limitaciones para hacer mi trabajo (incluya el trabajo en el hogar)", "", 5.00662, new double[] { -1.3693, -1.0471, -0.7125, -0.3551 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQSAT41", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de hacer mandados", "", 3.37631, new double[] { -1.2907, -0.9823, -0.5457, -0.073 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT42", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad para desempeñar mis actividades de rutina diarias", "", 5.51738, new double[] { -1.2866, -0.9615, -0.5175, -0.1567 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT43", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad para trabajar (incluya el trabajo en el hogar)", "", 5.85949, new double[] { -1.1702, -0.9035, -0.423, -0.0907 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT44", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad para hacer el trabajo que es verdaderamente importante para mí (incluya el trabajo en el hogar)", "", 6.11907, new double[] { -1.2267, -0.8672, -0.4598, -0.0765 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT45", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad para ocuparme de mis responsabilidades personales y domésticas", "", 6.73558, new double[] { -1.2791, -0.9283, -0.5091, -0.1273 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT46", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de hacer los quehaceres o las tareas del hogar", "", 6.2651, new double[] { -1.2034, -0.8833, -0.4545, -0.0895 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT47", "En los últimos 7 días", "Estoy satisfecho/a con la cantidad de trabajo que puedo hacer (incluya el trabajo en el hogar)", "", 6.43296, new double[] { -1.1628, -0.8568, -0.4508, 0.0128 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT48", "En los últimos 7 días", "Estoy satisfecho/a con la cantidad de tiempo que paso trabajando (incluya el trabajo en el hogar)", "", 5.65589, new double[] { -1.1609, -0.8504, -0.3782, 0.0792 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT49", "En los últimos 7 días", "Estoy satisfecho/a con la cantidad de tiempo que paso desempeñando mis actividades de rutina diarias", "", 5.79896, new double[] { -1.2028, -0.8992, -0.4243, 0.0198 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("NQSAT50", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad para trabajar", "", 5.2688, new double[] { -1.0591, -0.8529, -0.4672, -0.0795 }, -1, "",
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
