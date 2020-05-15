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
 * Item bank for PROMIS assessment. Generated from OID DB8E6DC5-CD3E-4C01-A7E0-CE1C78FB7FBE.
 */
public class NeuroQoLBancoTwoZeroFuncinCognitiva {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQCOG15r1", "¿Cuánta DIFICULTAD tiene actualmente para…", "estar al tanto de la hora (por ejemplo, usando un reloj)?", "", 1.47682, new double[] { -3.5042, -2.2687, -1.3111, -0.3711 }, -1, "",
          response("Nada", 5),
          response("Poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("No puedo hacerlo", 1)
      ),
      item("NQCOG16r1", "¿Cuánta DIFICULTAD tiene actualmente para…", "verificar la precisión de documentos financieros (por ejemplo, cuentas, chequera, estados de cuenta bancarios)?", "", 1.7684, new double[] { -3.2158, -1.8714, -1.0939, -0.1969 }, -1, "",
          response("Nada", 5),
          response("Poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("No puedo hacerlo", 1)
      ),
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
      item("NQCOG26r1", "¿Cuánta DIFICULTAD tiene actualmente para…", "planear una actividad con varios días de antelación (por ejemplo, una comida, un viaje o una visita a sus amigos)?", "", 2.02141, new double[] { -3.0628, -1.8299, -0.9105, -0.1812 }, -1, "",
          response("Nada", 5),
          response("Poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("No puedo hacerlo", 1)
      ),
      item("NQCOG31r1", "¿Cuánta DIFICULTAD tiene actualmente para…", "organizar las cosas?", "", 1.78896, new double[] { -2.865, -1.6761, -0.8127, 0.2993 }, -1, "",
          response("Nada", 5),
          response("Poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("No puedo hacerlo", 1)
      ),
      item("NQCOG38r1", "¿Cuánta DIFICULTAD tiene actualmente para…", "recordar dónde ha puesto o guardado cosas (por ejemplo, las llaves)?", "", 1.86625, new double[] { -3.0364, -1.8178, -0.8332, 0.4463 }, -1, "",
          response("Nada", 5),
          response("Poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("No puedo hacerlo", 1)
      ),
      item("NQCOG39r1", "¿Cuánta DIFICULTAD tiene actualmente para…", "recordar una lista de 4 ó 5 mandados sin escribirlos?", "", 1.75433, new double[] { -2.5668, -1.5049, -0.6076, 0.6798 }, -1, "",
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
      item("NQCOG46r1", "En los últimos 7 días", "Cometí errores sencillos con más facilidad", "", 2.5146, new double[] { -2.2788, -1.7424, -0.8698, 0.2918 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG64r1", "En los últimos 7 días", "Tuve que leer algo más de una vez para entenderlo", "", 2.28352, new double[] { -2.3584, -1.6053, -0.5585, 0.5287 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG65r1", "En los últimos 7 días", "Tuve problemas para recordar lo que estaba haciendo si me interrumpían", "", 3.25336, new double[] { -2.1387, -1.3852, -0.6261, 0.3412 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG66r1", "En los últimos 7 días", "Tuve dificultad para hacer más de una cosa a la vez", "", 3.15804, new double[] { -1.9812, -1.4575, -0.617, 0.1831 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG67r1", "En los últimos 7 días", "Tuve dificultad para recordar si hice lo que debía hacer, como tomar un medicamento o comprar algo que necesitaba", "", 2.3544, new double[] { -2.2475, -1.5343, -0.7285, 0.1054 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG68r1", "En los últimos 7 días", "Tuve dificultad para recordar información nueva, como números de teléfono o instrucciones sencillas", "", 2.5915, new double[] { -2.0912, -1.4269, -0.7303, 0.1036 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG69r1", "En los últimos 7 días", "Entré en una habitación y olvidé lo que quería buscar o hacer allí", "", 1.6663, new double[] { -2.6085, -1.6206, -0.534, 0.795 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG70r1", "En los últimos 7 días", "Tuve dificultad para recordar el nombre de una persona conocida", "", 2.52673, new double[] { -2.4482, -1.6404, -0.8509, 0.0061 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG72r1", "En los últimos 7 días", "Me costó trabajo pensar con claridad", "", 3.73568, new double[] { -1.9485, -1.41, -0.763, 0.053 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG74r1", "En los últimos 7 días", "Tuve dificultad para formar mis pensamientos", "", 3.1023, new double[] { -1.8914, -1.4055, -0.81, -0.1057 }, -1, "",
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
      ),
      item("NQCOG83r1", "En los últimos 7 días", "Tuve problemas para empezar tareas muy sencillas", "", 3.47148, new double[] { -1.9291, -1.4014, -0.746, -0.0346 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG84r1", "En los últimos 7 días", "Tuve problemas para tomar decisiones", "", 3.1843, new double[] { -1.9619, -1.3612, -0.6953, 0.0301 }, -1, "",
          response("Nunca", 5),
          response("Rara vez (una vez)", 4),
          response("Algunas veces (dos o tres veces)", 3),
          response("A menudo (como una vez al día)", 2),
          response("Muy a menudo (varias veces al día)", 1)
      ),
      item("NQCOG86r1", "En los últimos 7 días", "Tuve problemas para planear los pasos de una tarea", "", 3.7266, new double[] { -2.036, -1.453, -0.8257, -0.117 }, -1, "",
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
