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
 * Item bank for PROMIS assessment. Generated from OID 47E88339-68CE-437E-BB75-E13BD9784187.
 */
public class PromisShortFormOneZeroAnsiedad7a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 7, 7, 3.0,
      item("EDANX01", "En los últimos 7 días", "Sentí miedo", "", 3.60215, new double[] { 0.3416, 1.0895, 1.9601, 2.6987 }, -1, "",
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
      item("EDANX30", "En los últimos 7 días", "Me sentí preocupado/a", "", 3.03184, new double[] { -0.5154, 0.3156, 1.3516, 2.2997 }, -1, "",
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
      item("EDANX46", "En los últimos 7 días", "Me sentí nervioso/a", "", 3.39814, new double[] { -0.2166, 0.6321, 1.6444, 2.7312 }, -1, "",
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
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
