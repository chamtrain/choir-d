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
 * Item bank for PROMIS assessment. Generated from OID 33E6B1A5-F7D4-4008-A96E-D64C30A28B58.
 */
public class PromisShortFormOneZeroAnsiedad4a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 4, 3.0,
      item("EDANX01", "En los últimos 7 días", "Sentí miedo", "", 3.60215, new double[] { 0.3416, 1.0895, 1.9601, 2.6987 }, -1, "",
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
      item("EDANX53", "En los últimos 7 días", "Me sentí intranquilo/a", "", 3.65611, new double[] { -0.2318, 0.5952, 1.5635, 2.4991 }, -1, "",
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
