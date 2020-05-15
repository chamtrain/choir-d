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

import edu.stanford.registry.client.api.PatientAttributeObj;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.NotFoundException;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.util.Objects;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

import com.google.web.bindery.autobean.shared.AutoBeanCodex;

public class PatientAttributeRequestHandler extends ApiPatientCommon {
  private final SiteInfo siteInfo;
  private final ClinicServices clinicServices;
  public PatientAttributeRequestHandler(SiteInfo siteInfo, ClinicServices clinicServices) {
    this.siteInfo = siteInfo;
    this.clinicServices = clinicServices;
  }
  public JSONObject handle(String callString, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    String[] requestElements = callString.split("/");
    try {
      switch (requestElements[1]) {
      // handle modify and delete patientAttribute requests
      case "mod":
        if (requestElements.length == 3) {
          return updatePatientAttribute(requestElements[2], jsonRepresentation, callString);
        }
        throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid data supplied: {}", callString);
      case "rem":
        if (requestElements.length == 3) {
          return deletePatientAttribute(requestElements[2], jsonRepresentation, callString);
        }
        throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Invalid data supplied");
        // handle get patientAttribute requests
      default:
        if (requestElements.length > 2) {
          return getPatientAttribute(requestElements[1], requestElements[2]);
        } else {
          return getPatientAttributes(requestElements[1]);
        }
      }
    } catch (NotFoundException nf) {
      logger.error("not found exception on {}", callString);
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, nf.getMessage());
    } catch (NumberFormatException nfe) {
      logger.error("number format exception on {}", callString);
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString);
    }
  }

  private JSONObject updatePatientAttribute (String patientId, JsonRepresentation jsonRepresentation, String callString)
      throws ApiStatusException, NotFoundException {
    Patient patient = getPatient(clinicServices, siteInfo, patientId);
    if (patient == null)
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, "Invalid or missing patient");
    if (jsonRepresentation == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Missing data");
    }
    logger.debug("Factory is parsing: {} to a PatientAttributeOjb", jsonRepresentation.getJsonObject().toString());
    final PatientAttributeObj attributeObj = AutoBeanCodex.decode(factory, PatientAttributeObj.class,
        jsonRepresentation.getJsonObject().toString()).as();
    clinicServices.addPatientAttribute(makePatientAttribute(siteInfo, patient, attributeObj));
    return jsonFromString("success", "updated");
  }


  private JSONObject deletePatientAttribute (String patientId, JsonRepresentation jsonRepresentation, String callString)
      throws ApiStatusException, NotFoundException {
    Patient patient = getPatient(clinicServices, siteInfo, patientId);

    if (patient == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, "Invalid or missing patient");
    }
    if (jsonRepresentation == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Missing data");
    }
    final PatientAttributeObj attributeObj = AutoBeanCodex.decode(factory, PatientAttributeObj.class,
        jsonRepresentation.getJsonObject().toString()).as();

    PatientAttribute existingAttribute = patient.getAttribute(attributeObj.getName());

    if (existingAttribute != null &&
        Objects.equals(existingAttribute.getPatientAttributeId(), attributeObj.getAttributeId())) {
      clinicServices.deletePatientAttribute(makePatientAttribute(siteInfo, patient, attributeObj));
      return jsonFromString("success", "deleted");
    }
    throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Invalid data");
  }

  private JSONObject getPatientAttribute(String patientId, String attributeName)
      throws NotFoundException {
    return super.getPatientAttribute(siteInfo, clinicServices, patientId, attributeName);
  }

  private JSONObject getPatientAttributes(String patientId) throws NotFoundException {
    return super.getPatientAttributes(siteInfo, clinicServices, patientId);
  }
}
