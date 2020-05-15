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
 * Item bank for PROMIS assessment. Generated from OID 777ABF9A-1178-492C-B57E-D294D595C810.
 */
public class NeuroQoLShortFormPedOneZeroDolor {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQPAIped01", "En los últimos 7 días", "Sentí mucho dolor", "", 3.95782, new double[] { -0.025, 0.5568, 1.3086, 1.8715 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQPAIped02", "En los últimos 7 días", "Sentí tanto dolor que necesité tomar medicinas", "", 3.95782, new double[] { 0.3321, 0.7842, 1.2651, 1.4552 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQPAIped03", "En los últimos 7 días", "Falté a la escuela cuando tuve dolor", "", 3.95782, new double[] { 0.4724, 0.8048, 1.458, 2.3101 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQPAIped04", "En los últimos 7 días", "Sentí tanto dolor que tuve que dejar de hacer lo que estaba haciendo", "", 3.95782, new double[] { 0.424, 0.8385, 1.437, 1.9024 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQPAIped05", "En los últimos 7 días", "Me dolió todo el cuerpo", "", 3.95782, new double[] { 0.541, 0.9967, 1.4614, 2.1091 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQPAIped06", "En los últimos 7 días", "Sentí dolor", "", 3.95782, new double[] { -0.1797, 0.5298, 1.2915, 1.8986 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQPAIped07", "En los últimos 7 días", "Cuando sentiste dolor, ¿cuánto tiempo duró?", "", 3.95782, new double[] { -0.2261, 0.5533, 1.1479, 1.7339 }, -1, "",
          response("unos segundos", 1),
          response("unos minutos", 2),
          response("unas horas", 3),
          response("unos días (menos de una semana)", 4),
          response("más de una semana", 5)
      ),
      item("NQPAIped08", "En los últimos 7 días", "Tuve problemas para dormir cuando sentí dolor", "", 3.95782, new double[] { 0.1979, 0.6206, 1.1162, 1.6564 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQPAIped09", "En los últimos 7 días", "Tuve problemas para ver la televisión cuando sentí dolor", "", 3.95782, new double[] { 0.655, 1.0264, 1.4602, 1.8841 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      ),
      item("NQPAIped10", "En los últimos 7 días", "Me resultó difícil jugar o estar con mis amigos cuando sentí dolor", "", 3.95782, new double[] { 0.1768, 0.7852, 1.2697, 1.5286 }, -1, "",
          response("Nunca", 1),
          response("Casi nunca", 2),
          response("A veces", 3),
          response("A menudo", 4),
          response("Casi siempre", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
