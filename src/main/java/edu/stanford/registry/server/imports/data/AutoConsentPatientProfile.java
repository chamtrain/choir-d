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
package edu.stanford.registry.server.imports.data;

import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

/**
 * When new patients are created during the appointment load, set them to registered. (tpacht 10/17/2017).
 */
public class AutoConsentPatientProfile extends edu.stanford.registry.server.imports.data.PatientProfile
    implements ImportDataSourceManagerIntf {

  /**
   * Imports a patient and automatically sets them to consented
   *
   * @param data String array of the data fields for the patient being imported
   * @return true if successfully added/updated the patient
   * @throws Exception
   */
  @Override
  public boolean importData(String[] data) throws Exception {
    boolean imported = super.importData(data);
    if (imported) {
      Patient patient = getPatient(data);
      // After importing new patients set them to participates
      if (patient != null && !patient.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)) {
        PatientAttribute patientAttribute = new PatientAttribute(patient.getPatientId(), Constants.ATTRIBUTE_PARTICIPATES, "y", PatientAttribute.STRING);
        getPatientDao().insertAttribute(patientAttribute);
      }
    }
    return imported;
  }

  @Override
  public String getDataSource() {
    return "AutoConsentPatientProfile";
  }
}
