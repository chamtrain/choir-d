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

import edu.stanford.registry.client.api.AssessmentObj;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.NotFoundException;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.shared.AssessmentId;

import java.util.ArrayList;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

public class AssessmentRequestHandler extends ApiCommon {
  private final SiteInfo siteInfo;
  private final ClinicServices clinicServices;
  public AssessmentRequestHandler(SiteInfo siteInfo, ClinicServices  clinicServices) {
    this.siteInfo = siteInfo;
    this.clinicServices = clinicServices;
  }
  public JSONObject handle(String callString, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    String[] requestElements = callString.split("/");
    try {
      if (requestElements.length > 1) {
        switch (requestElements[1]) {
        case "mod":
          return notImplemented(callString);
        case "modarray":
          return notImplemented(callString);
        case "cancel":
          if (requestElements.length > 2) {
            return cancelAssessment(requestElements[2]);
          }
          throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Missing data");
        case "patient":
          if (requestElements.length > 2) {
            return getAssessmentsByPatient(requestElements[2]);
          }
          throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Missing data");
        case "id":
          if (requestElements.length > 2) {
            return getAssessmentById(requestElements[2]);
          }
          throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Missing data");
        }
      }
    } catch (NumberFormatException nfe) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, requestElements[2], "invalid assessment id");
    } catch (NotFoundException nf) {
      logger.trace("NOTFOUND creating ApiStatusException with " + nf.getMessage());
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, nf.getMessage());
    }
    throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, requestElements[0], "invalid assessment request structure");
  }

  private JSONObject getAssessmentsByPatient(String patientIdStr) throws NotFoundException {
    String patientId  = siteInfo.getPatientIdFormatter().format(patientIdStr);
    ArrayList<AssessmentObj> assessmentObjs = clinicServices.getAssessmentObjs(patientId);
    if (assessmentObjs.size() < 1) {
      throw new NotFoundException("Assessments");
    }
    JSONObject assessmentList = new JSONObject();
    for (AssessmentObj assessmentObj : assessmentObjs) {
      assessmentList.accumulate("ASSESSMENTS", makeAssessmentJson(assessmentObj));
    }
    return assessmentList;
  }

  private JSONObject getAssessmentById(String assessmentId) throws NotFoundException {
    try {
      AssessmentObj assessmentObj = clinicServices.getAssessmentObj(Long.parseLong(assessmentId));
      return makeAssessmentJson(assessmentObj);
    } catch (NumberFormatException nfe) {
      throw new NotFoundException("Assessment id");
    }
    //return new JsonRepresentation(makeAssessmentJson(clinicServices, Long.parseLong(assessmentId)));
  }
  private JSONObject makeAssessmentJson(AssessmentObj assessmentObj) {
    return new JSONObject(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(assessmentObj)).getPayload());
  }

  private JSONObject cancelAssessment(String assessmentIdString) throws NotFoundException {
    AssessmentId assessmentId = new AssessmentId(Long.parseLong(assessmentIdString));
    clinicServices.cancelRegistration(assessmentId);
    return jsonFromString("success", "updated");
  }
}
