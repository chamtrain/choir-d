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
 * Item bank for PROMIS assessment. Generated from OID 6C577423-FEBB-4B72-B063-37CA745BD475.
 */
public class NeuroQoLShortFormTwoZeroFuncinCognitiva {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQCOG22r1", "¿Cuánta DIFICULTAD tiene actualmente para…", "leer y seguir instrucciones complicadas (por ejemplo, las instrucciones de un medicamento nuevo)?", "", 1.99413, new double[] { -2.7796, -1.9347, -1.0525, -0.18 }, -1, "",
          response("Nada", 5),
          response("Poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("No puedo hacerlo", 1)
      ),
      item("NQCOG24r1", "¿Cuánta DIFICULTAD tiene actualmente para…", "planear y acudir a citas que no formen parte de su rutina semanal (por ejemplo, una cita para terapia o con un médico, o una reunión social con amigos y familiares)?", "", 2.0036, new double[] { -3.0204, -1.8757, -0.9692, -0.1291 }, -1, "",
          response("Nada", 5),
          response("Poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("No puedo hacerlo", 1)
      ),
      item("NQCOG25r1", "¿Cuánta DIFICULTAD tiene actualmente para…", "organizar su tiempo para realizar la mayor parte de sus actividades diarias?", "", 1.90767, new double[] { -2.9387, -1.8623, -0.8527, 0.2229 }, -1, "",
          response("Nada", 5),
          response("Poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("No puedo hacerlo", 1)
      ),
      item("NQCOG40r1", "¿Cuánta DIFICULTAD tiene actualmente para…", "aprender tareas o instrucciones nuevas?", "", 2.27017, new double[] { -2.7483, -1.802, -0.8962, 0.0953 }, -1, "",
          response("Nada", 5),
          response("Poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("No puedo hacerlo", 1)
      ),
      item("NQCOG64r1", "En los últimos 7 días", "Tuve que leer algo más de una vez para entenderlo", "", 2.28352, new double[] { -2.3584, -1.6053, -0.5585, 0.5287 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG75r1", "En los últimos 7 días", "Pensé con lentitud", "", 3.2253, new double[] { -1.8633, -1.37, -0.7535, -0.0608 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG77r1", "En los últimos 7 días", "Tuve que hacer mucho esfuerzo para prestar atención, de lo contrario cometería un error", "", 3.0151, new double[] { -1.9623, -1.358, -0.7005, 0.007 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG80r1", "En los últimos 7 días", "Tuve dificultad para concentrarme", "", 3.32175, new double[] { -1.8978, -1.4142, -0.6082, 0.204 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
