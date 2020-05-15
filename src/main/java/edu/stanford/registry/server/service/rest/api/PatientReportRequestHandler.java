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

import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.ResultAction;
import edu.stanford.registry.shared.SurveyRegistration;

import java.io.IOException;

import javax.servlet.ServletException;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

public class PatientReportRequestHandler extends ApiPatientCommon {
  private final SiteInfo siteInfo;
  private final ClinicServices clinicServices;

  public PatientReportRequestHandler(SiteInfo siteInfo, ClinicServices clinicServices) {
    this.siteInfo = siteInfo;
    this.clinicServices = clinicServices;
  }

  public JSONObject handle(String callString, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    String[] requestElements = callString.split("/");
    if (requestElements.length != 3) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, requestElements[0], "invalid request structure returning error");
    }

    try {
      switch (requestElements[1]) {
      case "token":
        return getReportByToken(requestElements[2]);
      case "patient":
        return getReportByPatient(requestElements[2]);
      case "id":
        Long assessmentId = Long.parseLong(requestElements[2]);
        JSONObject json = clinicServices.getJSON(new AssessmentId(assessmentId), getOpts(siteInfo), ResultAction.view);
        if (json == null) {
          logger.debug("json is null!");
          throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, requestElements[0]);
        }
        return json;
      }
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, requestElements[0]);
    } catch (NumberFormatException nfe) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, requestElements[0], "Failed, invalid assessment id format");
    } catch (IOException e) {
      logger.error("api failed to get json report for value {} ", requestElements[2], e);
      throw new ApiStatusException(Status.SERVER_ERROR_INTERNAL, requestElements[0]);
    } catch (ServletException se) {
      logger.error("servlet exception" , se);
      throw new ApiStatusException(Status.SERVER_ERROR_INTERNAL, requestElements[0]);
    }
  }

  private JSONObject getReportByToken(String token)
      throws ApiStatusException, IOException, ServletException {
    logger.debug("getting registration for token " + token);
    SurveyRegistration registration = clinicServices.getRegistration(token);
    if (registration == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, "patReport", "Token");
    }
    JSONObject json = clinicServices.getJSON(new AssessmentId(registration.getAssessmentRegId()), getOpts(siteInfo), ResultAction.view);
    if (json == null) {
      logger.debug("json is null!");
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "patReport");
    }
    return json;
  }

  private JSONObject getReportByPatient(String patientStr)
      throws ApiStatusException, IOException, ServletException {
    String patientId  = siteInfo.getPatientIdFormatter().format(patientStr);
    ApptRegistration registration = clinicServices.getLastCompletedRegistration(patientId);
    if (registration == null) {
      logger.info("No completed registration found");
      return new JSONObject().put("error", "The patient has not completed any assessment.");
    }
    JSONObject json = clinicServices.getJSON(new AssessmentId(registration.getAssessmentRegId()), getOpts(siteInfo), ResultAction.view);
    if (json == null) {
      logger.debug("json is null!");
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "patReport");
    }
    return json;
  }

  private static final Integer DEFAULT_WIDTH = 450;
  private static final Integer DEFAULT_HEIGHT = 150;
  private static final Integer DEFAULT_SPACER = 20;
  private ChartConfigurationOptions getOpts(SiteInfo siteInfo) {
    RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
    ChartConfigurationOptions opts = customizer.getConfigurationOptions();
    opts.setHeight(DEFAULT_HEIGHT);
    opts.setWidth(DEFAULT_WIDTH);
    opts.setGap(DEFAULT_SPACER);
    return opts;
  }
}
