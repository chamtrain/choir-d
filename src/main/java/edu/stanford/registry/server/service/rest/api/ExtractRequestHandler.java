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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ApiExtractServices;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.shared.InvalidDataElementException;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtractRequestHandler extends ApiPatientCommon {
  @SuppressWarnings("unused")
  private final SiteInfo siteInfo;
  private final ApiExtractServices extractServices;
  private final Logger logger = LoggerFactory.getLogger(edu.stanford.registry.server.service.rest.api.ExtractRequestHandler.class);

  public ExtractRequestHandler(SiteInfo siteInfo, ApiExtractServices extractServices) {
    this.siteInfo = siteInfo;
    this.extractServices = extractServices;
  }

  public JSONObject handle(String callString, JsonRepresentation jsonRepresentation) throws ApiStatusException  {
    String[] requestElements = callString.split("/");
    if (requestElements.length != 2) {
      return jsonFromString("response", "api/json/extract failure. Invalid call");
    }
    String tableName = requestElements[1];
    try {
      if (jsonRepresentation == null || jsonRepresentation.getText() == null || jsonRepresentation.getText().equals("null")) {
        // no json (list of columns) received so return a list of column names
        return getTableColumns(tableName);
      }
      return getRequestedData(tableName, jsonRepresentation);
    } catch (IOException ioe) {
      logger.debug("IO Exception caught parsing extract API request ", ioe);
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString);
    }
  }

  private JSONObject getTableColumns(String tableName) {
    try {
      ArrayList<String> columnList = extractServices.listSquareTableColumns(tableName);
      JSONObject colList = new JSONObject();
      for (String column : columnList) {
        colList.accumulate("fieldList", column);
      }
      //logger.debug("returning collist:" + colList.toString());
      return colList;
    } catch (Exception ex) {
      JSONObject errorMessage = new JSONObject();
      errorMessage.append("ERROR", ex.getMessage());
      return errorMessage;
    }
  }

  private JSONObject getRequestedData (String tableName, JsonRepresentation jsonRepresentation)
      throws ApiStatusException {

    String callString = "/extract/" + tableName;
    try {
      logger.debug("Received: " + jsonRepresentation.getText()); // no json list of columns so return the column names
      JSONObject object = jsonRepresentation.getJsonObject();
      JSONArray colArray = object.getJSONArray("fields");

      if (colArray == null)
        throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, "/extract/" + tableName, "Invalid or missing column list");


      ArrayList<String> columns = new ArrayList<>();
      for (int inx = 0; inx < colArray.length(); inx++) {
        columns.add(colArray.getString(inx));
      }
      JSONArray array = new JSONArray();
      try {
        ArrayList<ArrayList<Object>> data = extractServices.exportSquareTable(tableName, columns);
        for (ArrayList<Object> row : data) {
          array.put(dataRow(row));
        }
        JSONObject returnObject = new JSONObject();
        returnObject.put("EXTRACTDATA", array);
        logger.debug("Returning: " + returnObject.toString());
        return returnObject;
      } catch (InvalidDataElementException e) {
        throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, e.getMessage());
      }
    } catch (IOException ioe) {
        logger.debug("IO Exception caught parsing extract API request ", ioe);
        throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString);
      }
  }

  private JSONArray dataRow( ArrayList<Object> row) {

    JSONArray array = new JSONArray();
      for (Object value : row) {
        array.put(

            value == null ? " " : value.toString());
      }
      return array;
    }
}
