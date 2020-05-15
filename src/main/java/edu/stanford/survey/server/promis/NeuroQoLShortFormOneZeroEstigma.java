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
 * Item bank for PROMIS assessment. Generated from OID 27B9EC13-6C67-4AA9-A21F-E61B19842C35.
 */
public class NeuroQoLShortFormOneZeroEstigma {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQSTG01", "Recientemente", "Debido a mi enfermedad, algunas personas parecieron sentirse incómodos conmigo", "", 3.44314, new double[] { 0.0958, 0.7472, 1.4286, 2.4042 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG02", "Recientemente", "Debido a mi enfermedad, algunas personas me evitaron", "", 4.06104, new double[] { 0.3513, 0.8893, 1.5553, 2.1985 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG04", "Recientemente", "Debido a mi enfermedad, me sentí excluido/a de muchas cosas", "", 4.00129, new double[] { -0.0589, 0.3539, 0.9446, 1.6095 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG05", "Recientemente", "Debido a mi enfermedad, la gente fue poco amable conmigo", "", 3.30964, new double[] { 0.6524, 1.2615, 2.0968, 3.0858 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG08", "Recientemente", "Debido a mi enfermedad, la gente evitó mirarme", "", 3.92346, new double[] { 0.6715, 1.2285, 1.8107, 2.6988 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG16", "Recientemente", "Me sentí avergonzado/a de mi enfermedad", "", 3.45683, new double[] { 0.1838, 0.5917, 1.1831, 1.6868 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG17", "Recientemente", "Me sentí avergonzado/a debido a mis limitaciones físicas", "", 3.39119, new double[] { -0.0744, 0.3473, 1.016, 1.6108 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG21", "Recientemente", "Algunas personas actuaron como si fuera mi culpa tener esta enfermedad", "", 2.8818, new double[] { 0.5032, 0.9522, 1.5378, 2.1911 }, -1, "",
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
