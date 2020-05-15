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
 * Item bank for PROMIS assessment. Generated from OID 29DCD710-68BD-4A40-8564-B40EF649B34D.
 */
public class NeuroQoLShortFormOneZeroSentimientosPositivosYBienestar {
  private static final ItemBank bank = itemBank(0.0, 0.0, 9, 9, 3.0,
      item(" NQPPF12", "Recientemente", "Me sentí esperanzado/a", "", 4.96074, new double[] { -1.6487, -0.8302, 0.1234, 0.8777 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item(" NQPPF14", "Recientemente", "Tuve una sensación de bienestar", "", 6.60979, new double[] { -1.4063, -0.7089, 0.0725, 0.8153 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item(" NQPPF15", "Recientemente", "Mi vida me produjo satisfacción", "", 5.82867, new double[] { -1.3787, -0.6955, 0.1716, 0.8919 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF07", "Recientemente", "Muchos aspectos de mi vida me resultaron interesantes", "", 4.01345, new double[] { -1.468, -0.6688, 0.1761, 1.075 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF16", "Recientemente", "Tuve un sentido de equilibrio en mi vida", "", 4.91912, new double[] { -1.3921, -0.6029, 0.2022, 0.9628 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF17", "Recientemente", "Mi vida tuvo sentido", "", 5.60018, new double[] { -1.389, -0.845, 8.0E-4, 0.6868 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF19", "Recientemente", "Mi vida valió la pena", "", 4.15583, new double[] { -1.8868, -1.0572, -0.29, 0.306 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF20", "Recientemente", "Mi vida tuvo un objetivo", "", 5.09529, new double[] { -1.5216, -0.9021, -0.1202, 0.5311 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF22", "Recientemente", "Me sentí alegre", "", 4.58501, new double[] { -1.6527, -0.8785, 0.0931, 1.1194 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
