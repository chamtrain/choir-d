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

public interface PatientIdFormatIntf {

  /**
   * This used to formalize potentially abbreviated data. It is called when searching by patient Id.
   *
   * @param patientIdIn The string to be formatted
   * @return The patientIdIn string in the sites required format
   * @throws NumberFormatException When the string contains non-numeric characters where expected
   */
  String format(String patientIdIn) throws NumberFormatException;

  /**
   * Checks that the string entered is fully formated and correct. This is called to validate the patientId when adding new patients.
   *
   * @param patientIdIn The string to be formatted
   * @return true if the  patientId string is valid
   * @throws NumberFormatException When the string contains non-numeric characters where expected
   */
  boolean isValid(String patientIdIn) throws NumberFormatException;

  String getInvalidMessage();

  String printFormat(String patientIdIn);
}
