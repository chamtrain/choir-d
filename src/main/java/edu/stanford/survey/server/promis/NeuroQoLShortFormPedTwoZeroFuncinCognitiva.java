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
 * Item bank for PROMIS assessment. Generated from OID 1BBE48B6-8925-403B-BE19-BBC6CE8EDF20.
 */
public class NeuroQoLShortFormPedTwoZeroFuncinCognitiva {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQCOGped03", "", "Me olvido de las tareas escolares que tengo que hacer", "", 2.75, new double[] { -2.0, -1.34, -0.66, 0.38 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQCOGped05", "", "Algunas veces me olvido de lo que iba a decir", "", 2.18, new double[] { -2.49, -1.57, -0.72, 0.42 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQCOGped08", "", "Reacciono más despacio que la mayoría de las personas de mi edad cuando participo en juegos", "", 2.41, new double[] { -2.28, -1.64, -0.94, -0.28 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQCOGped15", "", "Olvido las cosas con facilidad", "", 3.02, new double[] { -2.04, -1.45, -0.73, 0.12 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQCOGped17", "", "Tengo problemas para acordarme de hacer las cosas (por ejemplo, proyectos escolares)", "", 3.74, new double[] { -2.01, -1.33, -0.74, 0.21 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQCOGped18", "", "Me resulta difícil concentrarme en la escuela", "", 3.73, new double[] { -1.82, -1.22, -0.59, 0.4 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQCOGped19", "", "Tengo problemas para prestar atención a los maestros", "", 3.63, new double[] { -1.85, -1.22, -0.55, 0.37 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      ),
      item("NQCOGped20", "", "Tengo que hacer mucho esfuerzo para prestar atención, de lo contrario cometeré un error", "", 3.48, new double[] { -1.86, -1.11, -0.58, 0.24 }, -1, "",
          response("Nada", 5),
          response("Un poco", 4),
          response("Algo", 3),
          response("Mucho", 2),
          response("Muchísimo", 1)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
