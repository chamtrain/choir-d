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

import edu.stanford.registry.client.api.SurveyObj;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.NotFoundException;
import edu.stanford.registry.server.service.rest.ApiStatusException;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

public class SurveyRequestHandler extends ApiPatientCommon {
  private final ClinicServices clinicServices;

  public SurveyRequestHandler(SiteInfo siteInfo, ClinicServices clinicServices) {
    //this.siteInfo = siteInfo;
    this.clinicServices = clinicServices;
  }

  public JSONObject handle(String callString, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    String[] requestElements = callString.split("/");
    if (requestElements.length < 3) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, requestElements[0]);
    }

    try {
      switch (requestElements[1]) {
      case "token":
        return getSurveyByToken(requestElements[2]);
      }
    } catch (NotFoundException nf) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, nf.getMessage());
    }
    throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid call structure: " + callString);

  }

  private JSONObject getSurveyByToken(String token) throws NotFoundException, ApiStatusException{
    if (!token.matches("^[a-zA-Z0-9:]*$")) {
      logger.warn("invalid token value {} in API call", token);
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "invalid data");
    }
    SurveyObj surveyObj = clinicServices.getSurveyObj(token);
    return new JSONObject(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(surveyObj)).getPayload());
  }
}
