/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.service.formatter;

public interface AppointmentFormatIntf {

  /**
   * This used to formalize potentially abbreviated data. It is called when
   * searching by patient Id.
   *
   * @param patientIdIn
   * @return
   * @throws Exception
   */
  String format(String patientIdIn) throws Exception;

  /**
   * Checks that the string entered is fully formated and correct. This is
   * called to validate the patientId when adding new patients.
   *
   * @param patientIdIn
   * @return
   * @throws Exception
   */
  boolean isValid(String patientIdIn) throws Exception;

  String getInvalidMessage();

}
