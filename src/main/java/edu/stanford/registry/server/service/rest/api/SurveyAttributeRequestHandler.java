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
package edu.stanford.registry.server.service.rest.api;

import edu.stanford.registry.client.api.SurveyRegistrationAttributeObj;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.NotFoundException;
import edu.stanford.registry.server.service.rest.ApiStatusException;

import java.util.Map;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

import com.google.web.bindery.autobean.shared.AutoBeanCodex;

public class SurveyAttributeRequestHandler extends ApiCommon {
  @SuppressWarnings({ "unused", "FieldCanBeLocal" })
  private final SiteInfo siteInfo;
  private final ClinicServices clinicServices;

  public SurveyAttributeRequestHandler(SiteInfo siteInfo, ClinicServices clinicServices) {
    this.siteInfo = siteInfo;
    this.clinicServices = clinicServices;
  }

  public JSONObject handle(String callString, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    String[] requestElements = callString.split("/");
    try {
      if (requestElements.length > 1) {
        if ("mod".equals(requestElements[1])) {
          return updateSurveyAttribute(jsonRepresentation, callString);
        }
        if (requestElements.length > 2) {
          return getSurveyRegistrationAttribute(requestElements[1], requestElements[2]);
        } else {
          return getSurveyRegistrationAttributes(requestElements[1]);
        }
      }
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Missing data");
    } catch (NotFoundException nf) {
      logger.error("not found exception on {}", callString);
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, nf.getMessage());
    } catch (NumberFormatException nfe) {
      logger.error("number format exception on {}", callString);
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString);
    }
  }

  private JSONObject updateSurveyAttribute (JsonRepresentation jsonRepresentation, String callString)
      throws ApiStatusException {
    if (jsonRepresentation == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Missing data");
    }
    try {
      final SurveyRegistrationAttributeObj attributeObj = AutoBeanCodex.decode(factory, SurveyRegistrationAttributeObj.class,
          jsonRepresentation.getJsonObject().toString()).as();
      if (attributeObj.getSurveyId() == null || attributeObj.getName() == null || attributeObj.getValue() == null) {
        throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Missing data");
      }

      clinicServices.setSurveyRegistrationAttribute(new Long(attributeObj.getSurveyId()), attributeObj.getName(), attributeObj.getValue() );
      return jsonFromString("success", "updated");
    } catch (NumberFormatException nfe) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString);
    } catch (NotFoundException notfe) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, "Invalid or missing data");
    }
  }

  private JSONObject getSurveyRegistrationAttribute(String surveyRegIdStr, String attributeName)
      throws NotFoundException {
    try {
      long surveyRegId = Long.parseLong(surveyRegIdStr);
      Map<String, String> attributes = clinicServices.getSurveyRegistrationAttributes(surveyRegId);
      for (String dataName : attributes.keySet()) {
        if (attributeName.equalsIgnoreCase(dataName)) {
          return beanObjToJsonObj(makeAttributeObj(surveyRegId, dataName, attributes.get(dataName)));
        }
      }
    } catch (NumberFormatException nfe) {
      throw new NotFoundException("Invalid id");
    }
    throw new NotFoundException("attribute " + attributeName);

  }

  private JSONObject getSurveyRegistrationAttributes(String surveyRegIdStr) throws NotFoundException {

    try {
      long surveyRegId = Long.parseLong(surveyRegIdStr);
      Map<String, String> attributes = clinicServices.getSurveyRegistrationAttributes(surveyRegId);
      JSONObject json = new JSONObject();
      for (String attrName : attributes.keySet()) {

        json.append("attributes", makeAttributeObj(surveyRegId, attrName, attributes.get(attrName)));
      }
      return json;
    } catch (NumberFormatException nfe) {
      throw new NotFoundException("Invalid id");
    }
  }

  private SurveyRegistrationAttributeObj makeAttributeObj(Long surveyRegId, String attrName, String attrValue) {

    SurveyRegistrationAttributeObj surveyRegistrationAttributeObj = factory.surveyRegistrationAttributeObj().as();
    surveyRegistrationAttributeObj.setSurveyId(surveyRegId.toString());
    surveyRegistrationAttributeObj.setName(attrName);
    surveyRegistrationAttributeObj.setValue(attrValue);
    return surveyRegistrationAttributeObj;
  }
}
