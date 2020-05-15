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
 * Item bank for PROMIS assessment. Generated from OID 51833A22-B2AB-4642-BF77-EDC26C48839A.
 */
public class PromisShortFormOneZeroComportamientoAnteElDolor7a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("PAINBE2", "En los últimos 7 días", "Cuando tuve dolor me puse irritable", "", 4.63048, new double[] { -0.5861, 0.2553, 0.7279, 1.2795, 1.7953 }, -1, "",
          response("No tuve dolor", 1),
          response("Nunca", 2),
          response("Rara vez", 3),
          response("Algunas veces", 4),
          response("A menudo", 5),
          response("Siempre", 6)
      ),
      item("PAINBE24", "En los últimos 7 días", "Cuando tuve dolor me moví con rigidez", "", 3.9655, new double[] { -0.5069, 0.6107, 1.1, 1.5662, 2.1717 }, -1, "",
          response("No tuve dolor", 1),
          response("Nunca", 2),
          response("Rara vez", 3),
          response("Algunas veces", 4),
          response("A menudo", 5),
          response("Siempre", 6)
      ),
      item("PAINBE25", "En los últimos 7 días", "Cuando tuve dolor, grité para que alguien me ayudara", "", 4.01381, new double[] { -0.4872, 1.1307, 1.5537, 2.0637, 2.5424 }, -1, "",
          response("No tuve dolor", 1),
          response("Nunca", 2),
          response("Rara vez", 3),
          response("Algunas veces", 4),
          response("A menudo", 5),
          response("Siempre", 6)
      ),
      item("PAINBE3", "En los últimos 7 días", "Cuando tuve dolor hice muecas (gestos con la cara)", "", 4.06509, new double[] { -0.635, 0.2322, 0.7487, 1.4077, 2.0393 }, -1, "",
          response("No tuve dolor", 1),
          response("Nunca", 2),
          response("Rara vez", 3),
          response("Algunas veces", 4),
          response("A menudo", 5),
          response("Siempre", 6)
      ),
      item("PAINBE37", "En los últimos 7 días", "Cuando tuve dolor me aislé de los demás", "", 4.32988, new double[] { -0.6464, 0.6185, 0.9334, 1.3468, 1.9306 }, -1, "",
          response("No tuve dolor", 1),
          response("Nunca", 2),
          response("Rara vez", 3),
          response("Algunas veces", 4),
          response("A menudo", 5),
          response("Siempre", 6)
      ),
      item("PAINBE45", "En los últimos 7 días", "Cuando tuve dolor me contorsioné violentamente", "", 3.83072, new double[] { -0.587, 1.2296, 1.573, 2.0245, 2.5756 }, -1, "",
          response("No tuve dolor", 1),
          response("Nunca", 2),
          response("Rara vez", 3),
          response("Algunas veces", 4),
          response("A menudo", 5),
          response("Siempre", 6)
      ),
      item("PAINBE8", "En los últimos 7 días", "Cuando tuve dolor me moví con suma lentitud", "", 4.26526, new double[] { -0.6228, 0.1852, 0.6573, 1.1987, 1.7007 }, -1, "",
          response("No tuve dolor", 1),
          response("Nunca", 2),
          response("Rara vez", 3),
          response("Algunas veces", 4),
          response("A menudo", 5),
          response("Siempre", 6)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
