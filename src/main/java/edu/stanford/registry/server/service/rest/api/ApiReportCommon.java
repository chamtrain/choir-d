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

import edu.stanford.registry.server.service.rest.ApiStatusException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for running reports in the Api
 *
 * @author tpacht
 */
public class ApiReportCommon extends ApiCommon {
  private final static Logger logger = LoggerFactory.getLogger(edu.stanford.registry.server.service.rest.api.ApiReportCommon.class);
  private final SimpleDateFormat dateFieldFormat = new SimpleDateFormat("yyyy-MM-dd");

  public JSONObject makeReportInputOption(String title, String name, String type) {
    JSONObject object = new JSONObject();
    object.put("title", title);
    object.put("name", name);
    object.put("type", type);
    return object;
  }

  public JSONObject makeReportSelectOption(String type, String title, String name, ArrayList<String> choices) {
    logger.trace("{} has {} choices", name, choices.size());
    JSONObject optionObject = new JSONObject();
    optionObject.put("type", type);
    optionObject.put("title", title);
    optionObject.put("name", name);
    for (String choice: choices) {
      optionObject.accumulate("VALUELIST", choice);
      logger.trace(" -- value {} ", choice);
    }
    return optionObject;
  }

  JSONArray dataRow(List<Object> row) {

    JSONArray array = new JSONArray();
    for (Object value : row) {
      array.put(
          value == null ? " " : value.toString());
    }
    return array;
  }

  public Date getStartDt(JSONObject jsonObject) throws ApiStatusException {
    try {
      return getDate(jsonObject, "fromDt");
    } catch (ParseException e) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "", "Invalid startDt date format");
    }
  }
  public Date getEndDt(JSONObject jsonObject) throws ApiStatusException {
    try {
      return getDate(jsonObject, "toDt");
    } catch (ParseException e) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "", "Invalid endDt date format");
    }
  }

  private Date getDate(JSONObject jsonObject, String parameterName) throws ApiStatusException, ParseException {
    if (isEmpty(jsonObject.getString(parameterName) )) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "Missing " +  parameterName + " value");
    }
    return dateFieldFormat.parse(jsonObject.getString(parameterName));
  }

  public String getChoice(JSONObject jsonObject, String parameterName) {
    if (jsonObject.has(parameterName)) {
      return jsonObject.getString(parameterName);
    }
    return null;
  }

  private boolean isEmpty(String value) {
    String valueStr = value == null ? "" : value;
    return valueStr.isEmpty();
  }

  public JSONObject getReturnObject (ArrayList<ArrayList<Object>> reportData) {
    List<List<Object>> list = new ArrayList<>();
    list.addAll(reportData);
    return getJSONObject(list);
  }

  public JSONObject getJSONObject (List<List<Object>> reportData) {
    JSONObject returnObject = new JSONObject();
    JSONArray jsonReportData = new JSONArray();
    for (List<Object> row : reportData) {
      jsonReportData.put(dataRow(row));
    }
    returnObject.put("reportDataSet", jsonReportData);

    logger.trace("Returning: {}", returnObject.toString());
    return returnObject;
  }

  /**
   * Get the display name for the clinic that's used in the sites Clinic filters
   * @param clinicMapping ClinicFilterMapping from the sites customizer
   * @param importedClinicName Actual name of the clinic stored in the database
   * @return Display name of clinic found in the mapping. If not found it returns the given name.
   */
  protected String getClinicDisplayName(Map<String, List<String>> clinicMapping , String importedClinicName) {
    if (importedClinicName == null || importedClinicName.length() < 1) {
      return importedClinicName;
    }

    for (String clinicName : clinicMapping.keySet()) {
      List<String> epicNames = clinicMapping.get(clinicName);
      for (String epicName : epicNames) {
        if (importedClinicName.equalsIgnoreCase(epicName)) {
          return clinicName;
        }
      }
    }
    return importedClinicName;
  }
}
