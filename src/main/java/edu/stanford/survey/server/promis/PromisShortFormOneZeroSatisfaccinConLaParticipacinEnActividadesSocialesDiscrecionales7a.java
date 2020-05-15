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
 * Item bank for PROMIS assessment. Generated from OID 507F4782-493A-40B6-8CF0-2905B5B2EB0D.
 */
public class PromisShortFormOneZeroSatisfaccinConLaParticipacinEnActividadesSocialesDiscrecionales7a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("SRPSAT05", "En los últimos 7 días", "Estoy satisfecho/a con la cantidad de tiempo que paso realizando actividades de tiempo libre", "", 4.292, new double[] { -1.157, -0.559, 0.15, 0.811 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT10", "En los últimos 7 días", "Estoy satisfecho/a con mi nivel actual de actividades sociales", "", 3.745, new double[] { -1.134, -0.558, 0.192, 0.916 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT20", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de hacer cosas por mis amigos/as", "", 3.888, new double[] { -1.464, -0.781, -0.003, 0.76 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT23", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de realizar actividades de tiempo libre", "", 4.352, new double[] { -1.232, -0.649, 0.002, 0.724 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT25", "En los últimos 7 días", "Estoy satisfecho/a con mi nivel actual de actividades con mis amigos/as", "", 4.083, new double[] { -1.149, -0.577, 0.157, 0.899 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT33", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad de hacer cosas para divertirme fuera de mi casa", "", 4.9, new double[] { -1.072, -0.531, 0.126, 0.723 }, -1, "",
          response("Nada", 1),
          response("Un poco", 2),
          response("Algo", 3),
          response("Mucho", 4),
          response("Muchísimo", 5)
      ),
      item("SRPSAT48", "En los últimos 7 días", "Estoy satisfecho/a con mi capacidad para divertirme en casa (leyendo, escuchando música, etc.)", "", 2.702, new double[] { -1.591, -0.89, -0.277, 0.603 }, -1, "",
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
