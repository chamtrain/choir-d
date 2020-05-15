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

package edu.stanford.registry.client.service;

import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.ServiceUnavailableException;

/**
 * The client side stub for the RPC service.
 */
// @RemoteServiceRelativePath("")
public interface RegistrationService extends PatientIdService {

  /**
   * Client code should never set the
   * @param patient
   * @param participatesValue
   * @return
   */
  Patient setPatientParticipation(Patient patient, String participatesValue);

  Patient updatePatient(Patient patient) throws ServiceUnavailableException;

}
