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
 * Item bank for PROMIS assessment. Generated from OID F84FFAE3-0AB2-49A3-B210-2FC1A37B7348.
 */
public class NeuroQoLBancoOneZeroDescontrolEmocionalYConductual {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("EDANG09", "En los últimos 7 días", "Me sentí enojado/a", "", 1.8694, new double[] { -1.0752, 0.2868, 1.6635, 3.0941 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANG31", "En los últimos 7 días", "Fui testarudo/a con los demás", "", 2.41506, new double[] { -0.7727, 0.2668, 1.4196, 2.367 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANG42", "En los últimos 7 días", "Tuve problemas para controlar mi mal genio", "", 2.67203, new double[] { -0.1397, 0.8976, 1.9405, 2.7996 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER05", "En los últimos 7 días", "Fue difícil controlar mi comportamiento", "", 2.84685, new double[] { -0.0016, 0.9465, 2.1141, 2.9448 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER06", "En los últimos 7 días", "Dije o hice cosas sin pensar", "", 2.54842, new double[] { -0.593, 0.4369, 1.7508, 2.7897 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER07", "En los últimos 7 días", "Me impacienté con los demás", "", 3.11799, new double[] { -1.1973, -0.0493, 1.0672, 2.1818 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER08", "En los últimos 7 días", "Me sentí impulsivo/a", "", 1.98463, new double[] { -0.7091, 0.4793, 1.8967, 3.1277 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER09", "En los últimos 7 días", "La gente me dijo que hablaba muy alto o de manera excesiva", "", 1.61501, new double[] { 0.4335, 1.3373, 2.3809, 3.3896 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER10", "En los últimos 7 días", "Dije o hice cosas que otras personas probablemente consideraron inapropiadas", "", 2.23161, new double[] { -0.0133, 1.0041, 2.246, 3.3185 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER11", "En los últimos 7 días", "Me irrité con los demás", "", 2.98734, new double[] { -0.5516, 0.4324, 1.561, 2.3579 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER12", "En los últimos 7 días", "Me molestaron cosas sin importancia", "", 3.17743, new double[] { -0.9556, 0.0154, 1.1723, 2.1197 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER13", "En los últimos 7 días", "De repente las emociones se apoderaron de mí sin ninguna razón", "", 2.2865, new double[] { -0.2575, 0.5676, 1.5016, 2.7528 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER14", "En los últimos 7 días", "Me sentí inquieto/a", "", 1.76249, new double[] { -0.9515, -0.0158, 1.5015, 3.1229 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER15", "En los últimos 7 días", "Me fue difícil adaptarme a cambios inesperados", "", 2.15697, new double[] { -0.5207, 0.4149, 1.5688, 2.5348 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER16", "En los últimos 7 días", "Me costó aceptar las críticas de otras personas", "", 2.32386, new double[] { -0.6616, 0.3676, 1.3008, 1.9902 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER17", "En los últimos 7 días", "Me alteré con facilidad", "", 3.60515, new double[] { -0.498, 0.3585, 1.2771, 2.0088 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER19", "En los últimos 7 días", "Tuve conflictos con los demás", "", 2.70232, new double[] { -0.5444, 0.6502, 1.7875, 2.6579 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQPER20", "En los últimos 7 días", "Amenacé con usar la violencia contra personas o bienes materiales", "", 2.05079, new double[] { 1.5712, 2.5151, 3.0389, 3.5242 }, -1, "",
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
