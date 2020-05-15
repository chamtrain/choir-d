/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.NotFoundException;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.util.List;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

import com.google.web.bindery.autobean.shared.AutoBeanCodex;

public class PatientRequestHandler extends ApiPatientCommon {
  private final SiteInfo siteInfo;
  private final ClinicServices clinicServices;

  public PatientRequestHandler(SiteInfo siteInfo, ClinicServices clinicServices) {
    this.siteInfo = siteInfo;
    this.clinicServices = clinicServices;
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
          return getPatientById(requestElements[1] );
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

  private JSONObject getPatientById(String patientId) throws NotFoundException {
    Patient patient = getPatient(clinicServices, siteInfo, patientId);
    return makePatientJson(siteInfo, patient);
  }

  protected JSONObject updatePatient(JsonRepresentation jsonRepresentation) throws ApiStatusException, NotFoundException {
    if (jsonRepresentation == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, null, "Missing data");
    }
    String jsonString = jsonRepresentation.getJsonObject().toString();
    logger.debug("Factory is parsing: {} to a PatientObj", jsonString);

    PatientObj patientObj = AutoBeanCodex.decode(factory, PatientObj.class, jsonString).as();
    getPatient(clinicServices, siteInfo, patientObj.getPatientId()); // checks format and if limits by site
    Patient patient = makePatient(siteInfo, patientObj);

    clinicServices.updatePatient(patient);
    List<PatientAttribute> attrs = patient.getAttributes();
    if (attrs != null) {
      for(PatientAttribute attr : attrs) {
        clinicServices.addPatientAttribute(attr);
      }
    }
    
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

    patient = clinicServices.setPatientAgreesToSurvey(patient);

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
    Patient patient = clinicServices.getPatient(patientDeclineObj.getPatientId());
    if (patient == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, null,
          "Invalid patient " + patientDeclineObj.getPatientId());
    }
    DeclineReason declineReason = patientDeclineObj.getDeclineReason();
    if (declineReason == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, null, "Missing decline reason");
    }
    return makePatientJson(siteInfo, clinicServices.declineEnrollment(patient, declineReason, patientDeclineObj.getReasonOther()));
  }
}
