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
 * Item bank for PROMIS assessment. Generated from OID BA87E860-F863-4746-9F44-D1AFE02D2313.
 */
public class PromisShortFormOneZeroSatisfaccinConLaParticipacinEnRolesSociales6a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 6, 6, 3.0,
      item("SRPSAT07", "En los últimos 7 días", "Estoy satisfecho/a con la cantidad de trabajo que puedo hacer (incluya el trabajo en el hogar)", "", 4.424, new double[] { -1.338, -0.799, -0.131, 0.675 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT24", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad para trabajar (incluya el trabajo en el hogar)", "", 4.688, new double[] { -1.433, -0.936, -0.288, 0.519 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT39", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de  hacer los quehaceres o las tareas del hogar", "", 4.08, new double[] { -1.429, -0.831, -0.145, 0.617 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT47", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad para ocuparme de mis responsabilidades personales y domésticas habituales", "", 4.377, new double[] { -1.421, -0.895, -0.215, 0.57 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT49", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad para desempeñar mis actividades de rutina diarias", "", 5.577, new double[] { -1.564, -0.92, -0.218, 0.458 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT50", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de atender a las necesidades de los que dependen de mí", "", 3.679, new double[] { -1.568, -1.009, -0.361, 0.552 }, -1, "",
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
