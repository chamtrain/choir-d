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
 * Item bank for PROMIS assessment. Generated from OID 313FE433-DB92-4502-B5F7-04C4847A9F50.
 */
public class PromisEscalaOneZeroIntensidadDelDolor3a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 3, 3, 3.0,
      item("PAINQU21", "", "¿Cuál es su nivel de dolor <U>en este momento</U>?", "", 2.61126, new double[] { -0.5362, 0.5882, 1.6364, 2.3559 }, -1, "",
          response("Ningún dolor", 1),
          response("Muy poco", 2),
          response("Moderado", 3),
          response("Intenso", 4),
          response("Muy intenso", 5)
      ),
      item("PAINQU6", "En los últimos 7 días ", "¿Qué intensidad tuvo el dolor en su <U>peor</U> momento?", "", 4.47278, new double[] { -1.5379, -0.8035, -0.0338, 0.6159 }, -1, "",
          response("No tuve dolor", 1),
          response("Muy poco", 2),
          response("Moderado", 3),
          response("Intenso", 4),
          response("Muy intenso", 5)
      ),
      item("PAINQU8", "En los últimos 7 días ", "¿Qué intensidad tuvo el dolor que sintió en su <U>punto medio</U>?", "", 6.28042, new double[] { -1.3195, -0.1539, 0.6925, 1.4689 }, -1, "",
          response("No tuve dolor", 1),
          response("Muy poco", 2),
          response("Moderado", 3),
          response("Intenso", 4),
          response("Muy intenso", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
