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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ApiReportGeneratorBase;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.shared.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles html requests for reports: /api/json/report/{name}
 *
 * @author tpacht
 */
public class ReportRequestHandler extends ApiReportCommon {

  private final SiteInfo siteInfo;
  private final ClinicServices clinicServices;
  private final static Logger logger = LoggerFactory.getLogger(edu.stanford.registry.server.service.rest.api.ReportRequestHandler.class);

  public ReportRequestHandler(SiteInfo siteInfo, ClinicServices clinicServices) {
    this.siteInfo = siteInfo;
    this.clinicServices = clinicServices;
  }

  public JSONObject handle(String callString, JsonRepresentation jsonRepresentation) throws ApiStatusException  {
    String[] requestElements = callString.split("/");
    if (requestElements.length != 2) {
      return jsonFromString("response", "api/json/report failure. Invalid call");
    }
    String reportName = requestElements[1];
    try {
      if (jsonRepresentation == null || jsonRepresentation.getText() == null || jsonRepresentation.getText().equals("null")) {
        // no json received so return the set of parameters
        return getReportParameters(reportName);
      }
      return getReportData(reportName, jsonRepresentation);
    } catch (IOException ioe) {
      logger.debug("IO Exception caught parsing report API request ", ioe);
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString);
    }
  }

  private JSONObject getReportData (String reportName, JsonRepresentation jsonRepresentation)
      throws ApiStatusException {

    String callString = "/report/" + reportName;
    try {
      logger.debug("Site {} Received: {}", siteInfo.getSiteId(), jsonRepresentation.getText());
      JSONObject jsonObject = jsonRepresentation.getJsonObject();

      ArrayList<ArrayList<Object>> reportData;
      switch (reportName) {
      case "cr1":
        reportData = clinicServices.complianceReport1();
        reportData.add(0, new ArrayList<>(Arrays.asList(Constants.COMPLIANCE1_RPT_HEADERS)));
        break;
      case "registration":
        reportData = clinicServices.registrationReportData(getStartDt(jsonObject), getEndDt(jsonObject));
        break;
      case "visits":
        reportData = clinicServices.visitsReportData(getStartDt(jsonObject), getEndDt(jsonObject));
        break;
      case "AverageSurveyTimeByMonth":
        reportData = clinicServices.averageSurveyTimeReportByMonth(getStartDt(jsonObject), getEndDt(jsonObject));
        reportData.add(0, new ArrayList<>(Arrays.asList(Constants.AVG_TIME_RPT_MONTH_HEADERS)));
        break;
      case "AverageSurveyTimeByType":
        reportData = clinicServices.averageSurveyTimeReportByType(getStartDt(jsonObject), getEndDt(jsonObject));
        reportData.add(0, new ArrayList<>(Arrays.asList(Constants.AVG_TIME_RPT_TYPE_HEADERS)));
        break;
      case "AverageSurveyTime":
        reportData = clinicServices.averageSurveyTimeReport(getStartDt(jsonObject), getEndDt(jsonObject));
        reportData.add(0, new ArrayList<>(Arrays.asList(Constants.AVG_TIME_RPT_SUMM_HEADERS)));
        break;
      default:
        try { // Name not recognized so see if it's a custom report
          return clinicServices.customApiReport(reportName, jsonRepresentation);
        } catch (Exception ex) {
          throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, ex.getMessage());
        }
      }
      JSONObject returnObject = new JSONObject();
      JSONArray jsonReportData = new JSONArray();
      for (ArrayList<Object> row : reportData) {
        jsonReportData.put(dataRow(row));
      }
      returnObject.put("reportDataSet", jsonReportData);
      logger.trace("Returning: {} ", returnObject.toString());
      return returnObject;
    } catch (IOException ioe) {
      logger.error("IO Exception caught parsing report API request ", ioe);
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString);
    }
  }

  private JSONObject getReportParameters(String reportName) throws ApiStatusException {
    logger.debug("getReportParameters called for site {} reportName: {}", siteInfo.getSiteId(), reportName);

    if (reportName.equals("cr1") || reportName.equals("registration") || reportName.equals("visits") ||
        reportName.equals("AverageSurveyTimeByMonth") || reportName.equals("AverageSurveyTimeByType") ||
        reportName.equals("AverageSurveyTime") ) {
      ApiReportGeneratorBase  generator = new ApiReportGeneratorBase();
      return generator.getDefaultReportParameters();
    }
    try {
      JSONObject params = clinicServices.customApiReport(reportName, null);
      if (params != null)
        logger.debug("returning parameters: {}", params.toString());
      return params;
    } catch (Exception ex) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST,  "/report/" + reportName, ex.getMessage());
    }
  }
}
