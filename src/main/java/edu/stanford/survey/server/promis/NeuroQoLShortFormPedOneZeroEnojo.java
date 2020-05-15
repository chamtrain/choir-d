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
 * Item bank for PROMIS assessment. Generated from OID B594B400-C390-4679-BCB8-EB5EF0ABAD23.
 */
public class NeuroQoLShortFormPedOneZeroEnojo {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQEMNped12", "En los últimos 7 días", "Al estar enojado/a se me hizo difícil estar con mis amigos/as", "", 3.31373, new double[] { 0.0405, 0.599, 1.5575, 2.41 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped13", "En los últimos 7 días", "Fue difícil hacer las tareas escolares porque estaba enojado/a", "", 3.22238, new double[] { -0.0218, 0.5438, 1.4991, 2.2016 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped14", "En los últimos 7 días", "Me sentí enojado/a", "", 3.79062, new double[] { -0.6375, 0.1733, 1.3767, 2.1596 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped15", "En los últimos 7 días", "Estuve tan enojado/a que tuve ganas de arrojar algo", "", 5.90559, new double[] { -0.1569, 0.4499, 1.3576, 1.988 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped16", "En los últimos 7 días", "Estuve tan enojado/a que tuve ganas de golpear algo", "", 6.57166, new double[] { -0.0387, 0.6039, 1.4349, 1.9646 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped17", "En los últimos 7 días", "Estuve tan enojado/a que tuve ganas de gritarle a alguien", "", 4.9357, new double[] { -0.5385, 0.1764, 1.1753, 1.9309 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped18", "En los últimos 7 días", "Estuve tan enojado/a que tuve ganas de romper cosas", "", 5.45497, new double[] { 0.0639, 0.7063, 1.5209, 2.1738 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped19", "En los últimos 7 días", "Estuve tan enojado/a que me comporté como un cascarrabias con los demás", "", 3.20809, new double[] { -0.6821, 0.0102, 1.2128, 2.0484 }, -1, "",
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
