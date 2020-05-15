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
 * Item bank for PROMIS assessment. Generated from OID 48B8D539-1BEB-4350-B03B-CCB568B4B77E.
 */
public class PromisBancoOneZeroSatisfaccinConLaParticipacinEnRolesSociales {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("SRPSAT06", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de hacer cosas por mi familia", "", 3.445, new double[] { -1.579, -0.955, -0.305, 0.636 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT07", "En los últimos 7 días", "Estoy satisfecho/a con la cantidad de trabajo que puedo hacer (incluya el trabajo en el hogar)", "", 4.424, new double[] { -1.338, -0.799, -0.131, 0.675 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT08", "En los últimos 7 días", "Me siento bien acerca de mi capacidad de hacer cosas por mi familia", "", 3.152, new double[] { -1.65, -1.083, -0.315, 0.544 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT09", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad para hacer el trabajo que es verdaderamente importante para mí (incluya el trabajo en el hogar)", "", 3.607, new double[] { -1.582, -0.941, -0.228, 0.562 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT21", "En los últimos 7 días", "Estoy satisfecho/a con la cantidad de tiempo que paso trabajando (incluya el trabajo en el hogar)", "", 3.329, new double[] { -1.492, -0.85, -0.036, 0.779 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT22", "En los últimos 7 días", "Estoy contento/a con cuánto hago por mi familia", "", 3.009, new double[] { -1.581, -0.967, -0.198, 0.707 }, -1, "",
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
      item("SRPSAT35", "En los últimos 7 días", "La calidad de mi trabajo es tan buena como quiero que sea (incluya el trabajo en el hogar)", "", 3.059, new double[] { -1.486, -0.895, -0.187, 0.785 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT38", "En los últimos 7 días", "Estoy satisfecho/a con la cantidad de tiempo que paso desempeñando mis actividades de rutina diarias", "", 3.731, new double[] { -1.512, -0.848, 0.008, 0.766 }, -1, "",
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
      ),
      item("SRPSAT51", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de hacer mandados", "", 3.274, new double[] { -1.536, -0.98, -0.377, 0.521 }, -1, "",
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
