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
 * Item bank for PROMIS assessment. Generated from OID DA686273-A8AB-4172-A939-3D5AE798D4EC.
 */
public class NeuroQoLBancoOneZeroSentimientosPositivosYBienestar {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
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
      item("NQPPF02", "Recientemente", "Pude disfrutar de la vida", "", 2.85836, new double[] { -1.6382, -0.843, 0.1359, 1.2391 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF03", "Recientemente", "Sentí que mi vida tenía razón de ser", "", 3.69836, new double[] { -1.3651, -0.6816, 0.1953, 1.0351 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF04", "Recientemente", "Me pude reír y apreciar el humor en diferentes situaciones", "", 2.73128, new double[] { -1.8634, -1.2606, -0.1628, 0.7927 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF05", "Recientemente", "Pude sentirme cómodo/a y relajado/a", "", 3.03956, new double[] { -1.6355, -0.8474, 0.0259, 1.2794 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF06", "Recientemente", "Esperé con alegría la llegada de eventos venideros", "", 3.43471, new double[] { -1.5461, -0.9063, 0.1045, 1.0355 }, -1, "",
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
      item("NQPPF08", "Recientemente", "Me sentí emocionalmente estable", "", 2.65589, new double[] { -1.6319, -1.048, -0.18, 0.7789 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF10", "Recientemente", "Sentí que merecía ser querido/a", "", 3.04784, new double[] { -1.6718, -0.8209, 0.0994, 0.9884 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF11", "Recientemente", "Sentí que tenía confianza en mí mismo/a", "", 3.43655, new double[] { -1.5457, -0.8165, 0.0139, 0.9641 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF13", "Recientemente", "Tuve una buena vida", "", 5.20641, new double[] { -1.5009, -0.8824, 0.0111, 0.6963 }, -1, "",
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
      item("NQPPF18", "Recientemente", "Mi vida fue tranquila", "", 3.19238, new double[] { -1.6418, -0.7983, 0.0738, 1.1679 }, -1, "",
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
      item("NQPPF21", "Recientemente", "Aproveché la vida al máximo", "", 3.6484, new double[] { -1.1253, -0.4402, 0.3556, 1.1312 }, -1, "",
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
      ),
      item("NQPPF23", "Recientemente", "En muchos sentidos, mi vida se acercó a lo que considero ideal", "", 3.63087, new double[] { -0.8407, -0.2679, 0.4779, 1.4662 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF24", "Recientemente", "Tuve buen control de mis pensamientos", "", 2.82777, new double[] { -1.8748, -1.0353, -0.1059, 0.7589 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPPF26", "Recientemente", "Aun cuando las cosas iban mal, me sentí optimista", "", 3.19493, new double[] { -1.8862, -1.0821, -0.0979, 0.7449 }, -1, "",
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
