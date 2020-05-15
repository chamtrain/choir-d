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
 * Item bank for PROMIS assessment. Generated from OID 60C4498E-ECF7-43D9-836B-CC135462AB67.
 */
public class NeuroQoLShortFormOneZeroDepresin {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("EDDEP04", "En los últimos 7 días", "Sentí que no valía nada", "", 4.77346, new double[] { -0.0968, 0.2889, 1.0307, 1.6231 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP09", "En los últimos 7 días", "Sentí que nada me podía animar", "", 4.66993, new double[] { -0.1071, 0.4488, 1.1186, 1.7643 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP29", "En los últimos 7 días", "Me sentí deprimido/a", "", 5.78729, new double[] { -0.3129, 0.2181, 0.9407, 1.417 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP36", "En los últimos 7 días", "Me sentí descontento/a", "", 4.69529, new double[] { -0.6883, 0.0064, 0.8354, 1.7386 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP39", "En los últimos 7 días", "Sentí que no tenía ninguna razón para vivir", "", 4.38096, new double[] { 0.379, 0.777, 1.3304, 1.9153 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP41", "En los últimos 7 días", "Me sentí desesperanzado/a", "", 5.24141, new double[] { 0.0199, 0.4926, 1.1465, 1.7244 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP45", "En los últimos 7 días", "Sentí que nada me interesaba", "", 4.12492, new double[] { -0.08, 0.4909, 1.2155, 1.9069 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDDEP48", "En los últimos 7 días", "Sentí que mi vida estaba vacía", "", 4.98996, new double[] { -0.0259, 0.3725, 1.0634, 1.6516 }, -1, "",
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
