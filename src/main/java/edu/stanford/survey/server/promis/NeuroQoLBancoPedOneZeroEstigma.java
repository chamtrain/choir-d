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
 * Item bank for PROMIS assessment. Generated from OID 6D7441F7-A502-49C8-A530-E350DB2D128F.
 */
public class NeuroQoLBancoPedOneZeroEstigma {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQSTGped01", "Recientemente", "Debido a mi enfermedad, otros niños de mi edad me intimidaron", "", 3.05708, new double[] { 0.1818, 0.8133, 1.4078, 2.2653 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTGped02", "Recientemente", "Debido a mi enfermedad, otros niños de mi edad parecieron sentirse incómodos conmigo", "", 3.05708, new double[] { 0.0327, 0.4528, 1.1496, 2.021 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
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
      item("NQSTGped05", "Recientemente", "Debido a mi enfermedad, otros niños de mi edad fueron malos conmigo", "", 3.05708, new double[] { 0.2195, 0.5617, 1.4676, 2.0424 }, -1, "",
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
      item("NQSTGped08", "Recientemente", "Debido a mi enfermedad, otros niños de mi edad se quedaron mirándome", "", 3.05708, new double[] { 0.0565, 0.5194, 1.2292, 1.5981 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTGped09", "Recientemente", "Debido a mi enfermedad, me preocupó lo que pensaban de mí otros niños de mi edad", "", 3.05708, new double[] { -0.2142, 0.322, 0.8888, 1.3813 }, -1, "",
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
      item("NQSTGped11", "Recientemente", "Me sentí infeliz respecto a cómo mi enfermedad afectaba mi apariencia", "", 3.05708, new double[] { 0.0136, 0.537, 1.0672, 1.423 }, -1, "",
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
      item("NQSTGped14", "Recientemente", "Debido a mi enfermedad, me preocupó hacer más difícil la vida a mis padres o a los que están legalmente a cargo de mí", "", 3.05708, new double[] { -0.3687, 0.0362, 0.7713, 1.5673 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTGped15", "Recientemente", "Me sentí avergonzado/a de mi enfermedad", "", 3.05708, new double[] { -0.0987, 0.2888, 1.0033, 1.4131 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTGped16", "Recientemente", "Me avergonzó mi forma de hablar", "", 3.05708, new double[] { 0.2183, 0.502, 1.3989, 1.813 }, -1, "",
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
      ),
      item("NQSTGped20", "Recientemente", "Perdí amistades por decirles que tengo esta enfermedad", "", 3.05708, new double[] { 0.7363, 1.0321, 1.7154, 2.2958 }, -1, "",
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
