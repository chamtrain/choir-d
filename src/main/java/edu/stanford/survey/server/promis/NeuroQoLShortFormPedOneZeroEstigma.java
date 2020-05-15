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
 * Item bank for PROMIS assessment. Generated from OID AAE755EF-F798-4202-AE2F-3535C6335247.
 */
public class NeuroQoLShortFormPedOneZeroEstigma {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQSTGped03", "Recientemente", "Debido a mi enfermedad, otros niños de mi edad me evitaron", "", 3.05708, new double[] { 0.2785, 0.624, 1.1933, 1.9356 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTGped04", "Recientemente", "Debido a mi enfermedad, me sentí excluido/a de muchas cosas", "", 3.05708, new double[] { -0.3168, 0.0592, 0.8396, 1.5577 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTGped06", "Recientemente", "Debido a mi enfermedad, otros niños de mi edad se burlaron de mi", "", 3.05708, new double[] { 0.2354, 0.6318, 1.2291, 1.7656 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTGped07", "Recientemente", "Debido a mi enfermedad, me sentí avergonzado/a cuando estuve en presencia de otros niños de mi edad", "", 3.05708, new double[] { -0.0737, 0.4577, 1.2143, 1.8196 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTGped10", "Recientemente", "Debido a mi enfermedad, otros niños de mi edad me trataron injustamente", "", 3.05708, new double[] { 0.194, 0.5329, 1.2369, 1.7109 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTGped13", "Recientemente", "Debido a mi enfermedad, otros niños de mi edad ignoraron mis buenas cualidades", "", 3.05708, new double[] { 0.1821, 0.4906, 1.2007, 1.7936 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTGped17", "Recientemente", "Debido a mi enfermedad, me sentí diferente a otros niños de mi edad", "", 3.05708, new double[] { -0.4536, 0.0865, 0.7, 1.1748 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTGped19", "Recientemente", "Evité hacer nuevos amigos/as para no tener que contarles a otras personas sobre mi enfermedad", "", 3.05708, new double[] { 0.2904, 0.6347, 1.1283, 1.7036 }, -1, "",
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
