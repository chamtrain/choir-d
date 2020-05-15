/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.hl7;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.imports.data.Hl7Appointment;
import edu.stanford.registry.server.imports.data.Hl7AppointmentIntf;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.User;

import com.github.susom.database.Database;

/*
 * Automatically consents patients when they are created during hl7 message processing
 */
public class Hl7AutoConsentCustomizer extends Hl7Appointment implements Hl7AppointmentIntf {

  private final Database database;
  private final SiteInfo siteInfo;

  public Hl7AutoConsentCustomizer(Database database, SiteInfo siteInfo) {
    super(database, siteInfo);
    this.database = database;
    this.siteInfo = siteInfo;
  }

  @Override
  public Patient postPatient(Patient patient) {
    patient = super.postPatient(patient);
    if (patient != null && !patient.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)) {
      User user = ServerUtils.getAdminUser(database);
      PatientDao patientDao = new PatientDao(database, siteInfo.getSiteId(), user);
      PatientAttribute patientAttribute = new PatientAttribute(patient.getPatientId(),
          Constants.ATTRIBUTE_PARTICIPATES, "y", PatientAttribute.STRING);
      patientDao.insertAttribute(patientAttribute);
    }
    return patient;
  }
}
