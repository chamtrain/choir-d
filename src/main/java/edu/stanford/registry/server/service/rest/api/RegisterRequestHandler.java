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
package edu.stanford.registry.server.service.rest.api;

import edu.stanford.registry.client.api.PatientDeclineObj;
import edu.stanford.registry.client.api.PatientObj;
import edu.stanford.registry.shared.DeclineReason;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.NotFoundException;
import edu.stanford.registry.server.service.RegisterServices;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.shared.Patient;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.web.bindery.autobean.shared.AutoBeanCodex;

/**
 * Handles API calls to /patreg for the actions that can be done with the "Register Patients" role.
 */
public class RegisterRequestHandler extends ApiPatientCommon {
  private final Logger logger = LoggerFactory.getLogger(edu.stanford.registry.server.service.rest.api.RegisterRequestHandler.class);
  private final SiteInfo siteInfo;
  private final RegisterServices services;

  public RegisterRequestHandler(SiteInfo siteInfo, RegisterServices services) {
    this.siteInfo = siteInfo;
    this.services = services;
  }

  public JSONObject handle(String callString, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    String[] requestElements = callString.split("/");
    try {
      if (requestElements.length > 1) {
        switch (requestElements[1]) {
        case "mod":
          return updatePatient(jsonRepresentation);
        case "register":
          return registerPatient(jsonRepresentation);
        case "decline":
          return declinePatient(jsonRepresentation);
        default:
          return getPatientById(requestElements[1]);
        }
      }
    } catch (NumberFormatException nfe) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, nfe.getMessage());
    } catch (NotFoundException nf) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, nf.getMessage());
    } catch (ApiStatusException ase) {
      ase.setRequestPath(callString);
      throw ase;
    }
    throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "invalid patient request");
  }

  private JSONObject getPatientById(String patientIdString) throws NotFoundException {
    String patientId = siteInfo.getPatientIdFormatter().format(patientIdString);
    Patient patient = services.getPatient(patientId);
    if (patient == null) {
      throw new NotFoundException("Patient");
    }
    return makePatientJson(siteInfo, patient);
  }

  private JSONObject updatePatient(JsonRepresentation jsonRepresentation) throws ApiStatusException, NotFoundException {
    if (jsonRepresentation == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, null, "Missing data");
    }
    String jsonString = jsonRepresentation.getJsonObject().toString();
    logger.debug("Factory is parsing: {} to a PatientObj", jsonString);

    PatientObj patientObj = AutoBeanCodex.decode(factory, PatientObj.class, jsonString).as();
    getPatientById(patientObj.getPatientId()); // this checks that the patient id is valid, exists and in site (if limited)

    Patient patient = makePatient(siteInfo, patientObj);
    services.updatePatient(patient);

    return makePatientJson(siteInfo, patient);
  }

  private JSONObject registerPatient(JsonRepresentation jsonRepresentation) throws ApiStatusException {
    if (jsonRepresentation == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, null, "Missing data");
    }
    String jsonString = jsonRepresentation.getJsonObject().toString();
    logger.debug("Factory is parsing: {} to a PatientObj", jsonString);

    PatientObj patientObj = AutoBeanCodex.decode(factory, PatientObj.class, jsonString).as();
    Patient patient = makePatient(siteInfo, patientObj);

    patient = services.setPatientAgreesToSurvey(patient);

    return makePatientJson(siteInfo, patient);
  }

  private JSONObject declinePatient(JsonRepresentation jsonRepresentation) throws ApiStatusException {
    if (jsonRepresentation == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, null, "Missing data");
    }
    String jsonString = jsonRepresentation.getJsonObject().toString();
    PatientDeclineObj patientDeclineObj = AutoBeanCodex.decode(factory, PatientDeclineObj.class, jsonString).as();
    if (patientDeclineObj == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, null, "Missing decline data");
    }
    Patient patient = services.getPatient(patientDeclineObj.getPatientId());
    if (patient == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, null,
          "Invalid patient " + patientDeclineObj.getPatientId());
    }
    DeclineReason declineReason = patientDeclineObj.getDeclineReason();
    if (declineReason == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, null, "Missing decline reason");
    }
    return makePatientJson(siteInfo, services.declineEnrollment(patient, declineReason, patientDeclineObj.getReasonOther()));
  }
}
