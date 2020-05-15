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
 * Item bank for PROMIS assessment. Generated from OID 76A575A9-58FD-4E97-B14B-289A43F818B0.
 */
public class NeuroQoLShortFormPedOneZeroDepresin {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQEMNped01", "En los últimos 7 días", "Me sentí demasiado triste para hacer cosas con amigos/as", "", 2.62405, new double[] { -0.0256, 0.6573, 1.9175, 2.5957 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped04", "En los últimos 7 días", "Me sentí triste", "", 2.90917, new double[] { -0.4953, 0.2996, 1.4805, 2.4839 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped09", "En los últimos 7 días", "Me sentí solo/a", "", 3.26573, new double[] { -0.4863, 0.1466, 1.238, 1.982 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped31", "En los últimos 7 días", "Estuve menos interesado/a en hacer las cosas que disfruto habitualmente", "", 3.93245, new double[] { -0.0307, 0.7006, 1.6276, 2.2268 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped34", "En los últimos 7 días", "Me resultó difícil interesarme en algo", "", 4.45517, new double[] { 0.148, 0.7918, 1.523, 2.2621 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped36", "En los últimos 7 días", "Me resultó difícil divertirme", "", 4.77898, new double[] { -0.0436, 0.5842, 1.3948, 2.0513 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped40", "En los últimos 7 días", "Sentí que no podía hacer nada bien", "", 3.90534, new double[] { -0.2431, 0.4248, 1.3776, 1.8663 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQEMNped41", "En los últimos 7 días", "Sentí que todo me salía mal en la vida", "", 4.97058, new double[] { -0.0139, 0.5668, 1.3458, 1.8465 }, -1, "",
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
