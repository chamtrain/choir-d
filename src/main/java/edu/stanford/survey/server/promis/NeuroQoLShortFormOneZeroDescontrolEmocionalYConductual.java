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
 * Item bank for PROMIS assessment. Generated from OID 2F1C3F29-B1ED-4FBB-BE60-E8B51E3457E4.
 */
public class NeuroQoLShortFormOneZeroDescontrolEmocionalYConductual {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
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
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
