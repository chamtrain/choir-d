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
 * Item bank for PROMIS assessment. Generated from OID F3C66F1A-19DC-442D-8C7B-F48FBEA9C7BC.
 */
public class NeuroQoLBancoPedOneZeroRelacionesSocialesInteraccinConNiosDeLaMismaEdad {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQSCLped09", "En los últimos 7 días", "Me sentí aceptado/a por los otros niños de mi edad", "", 2.74723, new double[] { -2.0936, -1.5123, -0.6156, 0.1482 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped10", "En los últimos 7 días", "Pude hablar con sinceridad con mis amigos/as", "", 3.2523, new double[] { -2.0283, -1.5719, -0.5613, 0.2091 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped11", "En los últimos 7 días", "Me sentí cercano/a a mis amigos/as", "", 3.92531, new double[] { -2.1081, -1.6621, -0.516, 0.2396 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped12", "En los últimos 7 días", "Pude contar con mis amigos/as", "", 3.26195, new double[] { -2.1475, -1.5528, -0.472, 0.3499 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped18", "En los últimos 7 días", "Compartí con otros niños (comida, juegos, plumas o bolígrafos, etc.)", "", 1.8198, new double[] { -2.9097, -2.0116, -0.4847, 0.7123 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped19", "En los últimos 7 días", "Pude defenderme yo solo/a", "", 2.29095, new double[] { -2.8329, -1.9612, -0.7066, 0.1524 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped20", "En los últimos 7 días", "Me sentí a gusto con otros niños de mi edad", "", 4.08497, new double[] { -2.2209, -1.5882, -0.6861, -0.0731 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped28", "En los últimos 7 días", "Me sentí feliz con los/as amigos/as que tenía", "", 3.10793, new double[] { -2.5039, -1.8726, -0.8857, 0.021 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped30", "En los últimos 7 días", "Me sentí a gusto hablando con mis amigos/as", "", 4.49165, new double[] { -2.0487, -1.7106, -0.817, -0.039 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped31", "En los últimos 7 días", "Quise pasar tiempo con mis amigos/as", "", 2.21396, new double[] { -2.9922, -2.4149, -0.9395, 0.1788 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped32", "En los últimos 7 días", "Pasé tiempo con mis amigos/as", "", 2.79358, new double[] { -3.0149, -1.7881, -0.6687, 0.4748 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped33", "En los últimos 7 días", "Hice cosas con otros niños de mi edad", "", 2.87566, new double[] { -2.7259, -1.7331, -0.5715, 0.5059 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped36", "En los últimos 7 días", "Mis amigos/as y yo nos ayudamos", "", 2.76589, new double[] { -2.524, -1.8852, -0.3862, 0.6856 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQSCLped38", "En los últimos 7 días", "Me divertí con mis amigos/as", "", 3.17547, new double[] { -2.4734, -1.9245, -0.7774, 0.1936 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
