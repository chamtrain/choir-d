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
 * Item bank for PROMIS assessment. Generated from OID 5B82897A-8F43-4A9A-A870-A20B704EE313.
 */
public class PromisBancoOneZeroAnsiedad {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("EDANX01", "En los últimos 7 días", "Sentí miedo", "", 3.60215, new double[] { 0.3416, 1.0895, 1.9601, 2.6987 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX02", "En los últimos 7 días", "Sentí mucho temor", "", 3.45041, new double[] { 0.4912, 1.33, 2.1552, 2.8802 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX03", "En los últimos 7 días", "Me dio miedo cuando me sentí nervioso/a", "", 3.128, new double[] { 0.2474, 1.0098, 1.8269, 2.7837 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX05", "En los últimos 7 días", "Sentí ansiedad", "", 3.35528, new double[] { -0.1897, 0.5981, 1.5749, 2.4458 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX07", "En los últimos 7 días", "Sentí que necesitaba ayuda para mi ansiedad", "", 3.55093, new double[] { 0.5394, 1.046, 1.865, 2.3847 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX08", "En los últimos 7 días", "Me preocupó mi salud mental", "", 2.84824, new double[] { 0.368, 1.0641, 1.8533, 2.5447 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX12", "En los últimos 7 días", "Me sentí angustiado/a", "", 2.7345, new double[] { -0.5687, 0.4721, 1.6717, 2.9695 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX13", "En los últimos 7 días", "El corazón me latió muy rápido o muy fuerte", "", 1.42999, new double[] { 0.3203, 1.3853, 2.7406, 4.6573 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX16", "En los últimos 7 días", "Sentí ansiedad si se alteraba mi rutina normal", "", 1.80263, new double[] { -0.0383, 0.8787, 2.1126, 3.0457 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX18", "En los últimos 7 días", "Tuve sensaciones de pánico repentinas.", "", 2.98577, new double[] { 0.5662, 1.3021, 2.1496, 3.0608 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX20", "En los últimos 7 días", "Me sobresalté (asusté) fácilmente", "", 1.72352, new double[] { 0.0104, 1.1685, 2.203, 3.1085 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX21", "En los últimos 7 días", "Me costó trabajo prestar atención", "", 2.23209, new double[] { -0.3004, 0.7373, 1.8598, 2.9248 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX24", "En los últimos 7 días", "Evité actividades o lugares públicos", "", 1.83002, new double[] { 0.3319, 0.9821, 1.8873, 3.0235 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX26", "En los últimos 7 días", "Me sentí inquieto/a", "", 1.95065, new double[] { -0.0265, 0.8792, 2.0145, 3.2407 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX27", "En los últimos 7 días", "Sentí que iba a ocurrir algo terrible", "", 2.86023, new double[] { 0.4361, 1.1269, 2.0295, 2.7778 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX30", "En los últimos 7 días", "Me sentí preocupado/a", "", 3.03184, new double[] { -0.5154, 0.3156, 1.3516, 2.2997 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX33", "En los últimos 7 días", "Me sentí aterrado/a", "", 2.58191, new double[] { 1.147, 1.8167, 2.7165, 3.5891 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX37", "En los últimos 7 días", "Me preocuparon las reacciones de otras personas hacia mí", "", 1.70917, new double[] { -0.0396, 0.8836, 2.0016, 3.1044 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX40", "En los últimos 7 días", "Tuve dificultad para concentrarme en otra cosa que no fuera mi ansiedad", "", 3.8832, new double[] { 0.4859, 1.2632, 2.1111, 2.8985 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX41", "En los últimos 7 días", "Mis inquietudes fueron demasiado para mí", "", 3.65951, new double[] { 0.3644, 1.0338, 1.7805, 2.6212 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX44", "En los últimos 7 días", "Mis músculos temblaron o se movieron solos", "", 1.2705, new double[] { 0.5719, 1.459, 2.6779, 3.9723 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX46", "En los últimos 7 días", "Me sentí nervioso/a", "", 3.39814, new double[] { -0.2166, 0.6321, 1.6444, 2.7312 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX47", "En los últimos 7 días", "Me sentí indeciso/a (que tenía dificultad para tomar decisiones)", "", 2.53651, new double[] { -0.239, 0.6778, 1.7956, 2.9007 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX48", "En los últimos 7 días", "Muchas situaciones me preocuparon", "", 3.04334, new double[] { -0.3316, 0.5559, 1.4559, 2.3399 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX49", "En los últimos 7 días", "Tuve dificultad para dormir", "", 1.5157, new double[] { -0.8298, 0.0869, 1.1677, 2.3797 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX51", "En los últimos 7 días", "Me costó trabajo relajarme", "", 2.41166, new double[] { -0.4618, 0.3984, 1.3636, 2.4028 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX53", "En los últimos 7 días", "Me sentí intranquilo/a", "", 3.65611, new double[] { -0.2318, 0.5952, 1.5635, 2.4991 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX54", "En los últimos 7 días", "Me sentí tenso/a", "", 3.35033, new double[] { -0.5094, 0.3107, 1.2502, 2.2966 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("EDANX55", "En los últimos 7 días", "Tuve dificultad para tranquilizarme", "", 3.12733, new double[] { 0.0744, 0.9409, 1.8477, 2.7757 }, -1, "",
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
