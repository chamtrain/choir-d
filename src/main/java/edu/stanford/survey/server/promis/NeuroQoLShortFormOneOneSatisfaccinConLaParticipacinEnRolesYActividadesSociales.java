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
 * Item bank for PROMIS assessment. Generated from OID DD832A45-307D-4653-8722-AD3F0471AD45.
 */
public class NeuroQoLShortFormOneOneSatisfaccinConLaParticipacinEnRolesYActividadesSociales {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQSAT03", "En los últimos 7 días", "Me molestan mis limitaciones en las actividades de familia habituales", "", 4.91734, new double[] { -1.3877, -0.9496, -0.6424, -0.3162 }, -1, "",
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
      item("NQSAT14", "En los últimos 7 días", "Me molestan mis limitaciones en las actividades habituales con mis amigos/as", "", 4.78275, new double[] { -1.4742, -1.0535, -0.6893, -0.2994 }, -1, "",
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
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
