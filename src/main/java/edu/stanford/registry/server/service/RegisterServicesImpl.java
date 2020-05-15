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

package edu.stanford.registry.server.service;

import edu.stanford.registry.shared.DeclineReason;
import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.formatter.PatientIdFormatIntf;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.User;

import java.util.function.Supplier;

import com.github.susom.database.Database;

public class RegisterServicesImpl extends ClinicServicesImpl implements RegisterServices {

  public RegisterServicesImpl(User user, Supplier<Database> databaseProvider, ServerContext context, SiteInfo siteInfo) {
    super(user, databaseProvider, context, siteInfo);
  }

  @Override
  public Patient getPatient(String patientId) {
    return super.getPatient(patientId);
  }

  @Override
  public Patient setPatientAgreesToSurvey(Patient patient) throws ServiceUnavailableException {
    return super.setPatientAgreesToSurvey(patient);
  }

  @Override
  public Patient declineEnrollment(Patient patient, DeclineReason reasonCode, String reasonOther) throws ServiceUnavailableException {
    return super.declineEnrollment(patient, reasonCode, reasonOther);
  }

  @Override
  public void completePendingRegistrations(Patient patient) {
    super.completePendingRegistrations(patient);
  }

  /**
   * Update or Add the patient and attributes
   */
  @Override
  public Patient updatePatient(Patient patient) throws ServiceUnavailableException {
    patient = super.updatePatient(patient);
    super.updatePatientEmail(patient);
    return patient;
  }

  @Override
  public PatientIdFormatIntf getPatientIdFormatter() {
    return siteInfo.getPatientIdFormatter();
  }

}
