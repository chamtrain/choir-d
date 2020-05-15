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

package edu.stanford.registry.server;

import edu.stanford.registry.shared.DeclineReason;
import edu.stanford.registry.client.service.PatientIdService;
import edu.stanford.registry.client.service.RegistrationService;
import edu.stanford.registry.server.service.RegisterServices;
import edu.stanford.registry.server.service.formatter.PatientIdFormatIntf;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.InvalidPatientIdException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.ServiceUnavailableException;

import org.apache.log4j.Logger;

public class RegistrationServiceImpl extends InitRegistryServlet implements RegistrationService, PatientIdService {
  /**
   *
   */
  private static final long serialVersionUID = 9094480861420150667L;
  private static Logger logger = Logger.getLogger(RegistrationServiceImpl.class);

  /**
   * A servlet interface, so must only have a default constructor
   */
  public RegistrationServiceImpl() {
  }

  @Override
  public Patient setPatientParticipation(Patient patient, String participatesValue) {
    logger.debug("addPatientsRegistration called");
    boolean participates = "y".equals(participatesValue);

    if (!participates && !participatesValue.equals("n")) { // defensive- won't happen
      logger.error("addPatientsRegistration called with a bad (not y/n) participatesValue of " + participatesValue);
      return patient;
    }

    RegisterServices service = getService();
    if (participates) {
      return service.setPatientAgreesToSurvey(patient); // this also does completePendingRegistrations(pat)
    } else {
      return declineEnrollment(service, patient);
    }
  }


  private Patient declineEnrollment(RegisterServices service, Patient patient) {
    DeclineReason reason = null;
    String reasonCode = patient.getAttributeString(Constants.ATTRIBUTE_DECLINE_REASON_CODE);
    String declineReason = null;
    if (reasonCode != null && !reasonCode.isEmpty()) {
      reason = DeclineReason.valueOf(patient.getAttributeString(Constants.ATTRIBUTE_DECLINE_REASON_CODE));
      if (reason.equals(DeclineReason.other)) {
        declineReason = patient.getAttributeString(Constants.ATTRIBUTE_DECLINE_REASON_OTHER);
      }
    }
    return service.declineEnrollment(patient, reason, declineReason);
  }

  private RegisterServices getService() {
    RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
    RegisterServices registrationService = (RegisterServices) regRequest.getService();
    return registrationService;
  }


  @Override
  public Patient getPatient(String patientId) throws ServiceUnavailableException, InvalidPatientIdException {
    /** First validate the patientId **/
    RegisterServices service = getService();

    PatientIdFormatIntf formatter = service.getPatientIdFormatter();
    Patient pat = null;
    boolean isValid = false;
    try {
      patientId = formatter.format(patientId);
      isValid = formatter.isValid(patientId);
    } catch (NumberFormatException e) {
      logger.error("Error in formatting(" + patientId + ")", e);
      throw new InvalidPatientIdException("Invalid number", false);
    }
    try {
      if (isValid) {
        pat = getService().getPatient(patientId);
      }
    } catch (Exception e) {
      logger.error("Error in getPatient(" + patientId + ")", e);
      throw new ServiceUnavailableException(e.getMessage());
    }
    if (pat != null) {
      return pat;
    }
    logger.debug("getPatient: patient not found, formatter.isValid = " + isValid);
    if (isValid) {
      throw new InvalidPatientIdException(patientId + " Not found", patientId);
    }
    throw new InvalidPatientIdException(formatter.getInvalidMessage(), isValid);

  }

  @Override
  public Patient updatePatient(Patient patient) throws ServiceUnavailableException {
    return getService().updatePatient(patient);
  }

}
