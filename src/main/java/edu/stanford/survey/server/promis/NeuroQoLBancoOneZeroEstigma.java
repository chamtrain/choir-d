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
 * Item bank for PROMIS assessment. Generated from OID 9DCF2B6D-41B2-4827-90DB-7DC083EF1AED.
 */
public class NeuroQoLBancoOneZeroEstigma {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("NQSTG01", "Recientemente", "Debido a mi enfermedad, algunas personas parecieron sentirse incómodos conmigo", "", 3.44314, new double[] { 0.0958, 0.7472, 1.4286, 2.4042 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG02", "Recientemente", "Debido a mi enfermedad, algunas personas me evitaron", "", 4.06104, new double[] { 0.3513, 0.8893, 1.5553, 2.1985 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG03", "Recientemente", "Debido a mi enfermedad, me sentí emocionalmente distante de los demás", "", 3.52929, new double[] { -0.0496, 0.3764, 0.9948, 1.6697 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG04", "Recientemente", "Debido a mi enfermedad, me sentí excluido/a de muchas cosas", "", 4.00129, new double[] { -0.0589, 0.3539, 0.9446, 1.6095 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG05", "Recientemente", "Debido a mi enfermedad, la gente fue poco amable conmigo", "", 3.30964, new double[] { 0.6524, 1.2615, 2.0968, 3.0858 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG06", "Recientemente", "Debido a mi enfermedad, la gente se burló de mí", "", 2.85095, new double[] { 0.8895, 1.4822, 2.2944, 2.965 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG07", "Recientemente", "Debido a mi enfermedad, me sentí avergonzado/a en situaciones sociales", "", 3.98822, new double[] { 0.1738, 0.6155, 1.2692, 1.9027 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG08", "Recientemente", "Debido a mi enfermedad, la gente evitó mirarme", "", 3.92346, new double[] { 0.6715, 1.2285, 1.8107, 2.6988 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG09", "Recientemente", "Debido a mi enfermedad, los extraños se quedaron mirándome fijamente", "", 2.65475, new double[] { 0.7411, 1.3513, 2.0372, 2.5424 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG10", "Recientemente", "Debido a mi enfermedad, me preocupó la actitud de otras personas hacia mí", "", 3.27662, new double[] { 0.3538, 0.7686, 1.3021, 1.9673 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG11", "Recientemente", "Debido a mi enfermedad, los demás me trataron injustamente", "", 3.76064, new double[] { 0.5404, 1.1235, 1.8159, 2.3161 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG12", "Recientemente", "Me sentí infeliz respecto a cómo mi enfermedad afectaba mi apariencia", "", 2.67305, new double[] { 0.1724, 0.6167, 1.1909, 1.6331 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG13", "Recientemente", "Debido a mi enfermedad, me resultó difícil permanecer aseado/a y limpio/a", "", 2.42666, new double[] { 0.5108, 0.9947, 1.7392, 2.4247 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG14", "Recientemente", "Debido a mi enfermedad, la gente ignoró mis buenas cualidades", "", 4.18968, new double[] { 0.5171, 1.0161, 1.6609, 2.133 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG15", "Recientemente", "Debido a mi enfermedad, me preocupó ser una carga para los demás", "", 3.28362, new double[] { -0.1601, 0.22, 0.9341, 1.4723 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG16", "Recientemente", "Me sentí avergonzado/a de mi enfermedad", "", 3.45683, new double[] { 0.1838, 0.5917, 1.1831, 1.6868 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG17", "Recientemente", "Me sentí avergonzado/a debido a mis limitaciones físicas", "", 3.39119, new double[] { -0.0744, 0.3473, 1.016, 1.6108 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG18", "Recientemente", "Me avergonzó mi forma de hablar", "", 1.94282, new double[] { 0.6076, 0.983, 1.6902, 2.4294 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG19", "Recientemente", "Debido a mi enfermedad, me sentí diferente a los demás", "", 3.34828, new double[] { -0.1124, 0.417, 0.9551, 1.4508 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG20", "Recientemente", "Me eché la culpa de mis problemas", "", 1.65755, new double[] { -0.3368, 0.3114, 1.2398, 2.1552 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG21", "Recientemente", "Algunas personas actuaron como si fuera mi culpa tener esta enfermedad", "", 2.8818, new double[] { 0.5032, 0.9522, 1.5378, 2.1911 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG22", "Recientemente", "Evité hacer nuevos amigos para no tener que contarles a otras personas sobre mi enfermedad", "", 3.09105, new double[] { 0.5357, 0.9754, 1.4343, 1.932 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG25", "Recientemente", "Algunas personas con mi enfermedad perdieron el trabajo cuando sus empleadores se enteraron de que la padecían", "", 1.48903, new double[] { 0.0055, 0.6193, 1.8062, 2.8934 }, -1, "",
          response("Nunca", 1),
          response("Rara vez", 2),
          response("Algunas veces", 3),
          response("A menudo", 4),
          response("Siempre", 5)
      ),
      item("NQSTG26", "Recientemente", "Perdí amistades por decirles que tengo esta enfermedad", "", 2.51833, new double[] { 0.8785, 1.393, 1.9593, 2.6939 }, -1, "",
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
