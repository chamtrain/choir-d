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

import edu.stanford.registry.client.api.PluginPatientDataObj;
import edu.stanford.registry.client.api.PluginPatientGetObj;
import edu.stanford.registry.client.api.PluginPatientHistoryDataObj;
import edu.stanford.registry.client.api.PluginPatientStoreObj;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.NotFoundException;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.shared.Patient;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

public class PluginRequestHandler extends ApiPatientCommon {
  private final ClinicServices services;
  private final SiteInfo siteInfo;

  public PluginRequestHandler(SiteInfo siteInfo, ClinicServices clinicServices) {
    this.services = clinicServices;
    this.siteInfo = siteInfo;
  }

  public JSONObject handle(String callString, JsonRepresentation jsonRepresentation) throws ApiStatusException {

    String[] requestElements = callString.split("/");
    if (requestElements.length < 4) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, requestElements[0]);
    }
    try {
      switch (requestElements[1]) {
      case "patient":
        switch (requestElements[2]) {
        case "post":
          return storeData(jsonRepresentation, callString, requestElements[3]);
        case "getLast":
          return getLast(jsonRepresentation, callString, requestElements[3]);
        case "getAll":
          return getAll(jsonRepresentation, callString, requestElements[3]);
        case "getHistory":
          return getHistory(jsonRepresentation, callString, requestElements[3]);
        }
        break;
      }
    } catch (NotFoundException nf) {
      logger.error("not found exception on {}", callString);
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, nf.getMessage());
    }
    throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "invalid request");

  }

  private JSONObject storeData(JsonRepresentation jsonRepresentation, String callString, String dataType)
      throws ApiStatusException, NotFoundException {
    if (jsonRepresentation == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Missing data");
    }
    logger.trace("Factory is parsing: {} to a PluginPatientStoreObj", jsonRepresentation.getJsonObject().toString());
    final PluginPatientStoreObj pluginPatientStoreObj = AutoBeanCodex.decode(factory, PluginPatientStoreObj.class,
        jsonRepresentation.getJsonObject().toString()).as();

    if (pluginPatientStoreObj.getPatientId() == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, "Invalid or missing patient");
    }

    Patient patient = getPatient(services, siteInfo, pluginPatientStoreObj.getPatientId());
    if (patient == null)
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, "Invalid or missing patient");

    String dataVersion = pluginPatientStoreObj.getDataVersion();
    if (dataVersion == null || dataVersion.isEmpty()) {
      dataVersion = "1";
    }
    if (pluginPatientStoreObj.getDataValue() == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "Missing data value");
    }

    PluginPatientDataObj pluginPatientDataObj = services.addPluginPatientData(dataType, patient.getPatientId(), dataVersion, pluginPatientStoreObj.getDataValue());
    JSONObject responseObj = new JSONObject();
    responseObj.put("dataId", pluginPatientDataObj.getDataId());
    responseObj.put("created", pluginPatientDataObj.getCreatedTime());
    return responseObj;

  }

  private JSONObject getLast(JsonRepresentation jsonRepresentation, String callString, String dataType)
      throws ApiStatusException, NotFoundException {
    if (jsonRepresentation == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Missing data");
    }
    logger.trace("Factory is parsing: {} to a PluginPatientGetObj", jsonRepresentation.getJsonObject().toString());
    final PluginPatientGetObj pluginPatientGetObj = AutoBeanCodex.decode(factory, PluginPatientGetObj.class,
        jsonRepresentation.getJsonObject().toString()).as();

    if (pluginPatientGetObj.getPatientId() == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, "Missing patient");
    }

    Patient patient = getPatient(services, siteInfo, pluginPatientGetObj.getPatientId());
    if (patient == null)
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, "Invalid patient");

    PluginPatientDataObj pluginPatientDataObj = services.findPluginPatientData(dataType,
        pluginPatientGetObj.getPatientId(), pluginPatientGetObj.getDataVersion());

    if (pluginPatientDataObj == null) {
      logger.trace("pluginPatientDataObj is null, returning null");
      return null;
    }

    AutoBean<PluginPatientDataObj> pluginPatientDataAutoBean = AutoBeanUtils.getAutoBean(pluginPatientDataObj);
    JSONObject returnObj = new JSONObject(AutoBeanCodex.encode(pluginPatientDataAutoBean).getPayload());
    logger.trace("getLast returning string {}", returnObj.toString());
    return returnObj;
  }

  private JSONObject getAll(JsonRepresentation jsonRepresentation, String callString, String dataType)
      throws ApiStatusException, NotFoundException {
    if (jsonRepresentation == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Missing data");
    }
    logger.trace("Factory is parsing: {} to a pluginPatientGetObj", jsonRepresentation.getJsonObject().toString());
    final PluginPatientGetObj pluginPatientGetObj = AutoBeanCodex.decode(factory, PluginPatientGetObj.class,
        jsonRepresentation.getJsonObject().toString()).as();

    if (pluginPatientGetObj.getPatientId() == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, "Invalid or missing patient");
    }

    Patient patient = getPatient(services, siteInfo, pluginPatientGetObj.getPatientId());
    if (patient == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, "Invalid or missing patient");
    }

    Date fromTime = pluginPatientGetObj.getFromTime() != null ? new Date(pluginPatientGetObj.getFromTime()) : null;
    Date toTime = pluginPatientGetObj.getToTime() != null ? new Date(pluginPatientGetObj.getToTime()) : null;
    ArrayList<PluginPatientDataObj> list = services.findAllPluginPatientData(dataType,
        pluginPatientGetObj.getPatientId(), pluginPatientGetObj.getDataVersion(), fromTime, toTime);

    JSONObject object = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    for (PluginPatientDataObj pluginPatientDataObj : list) {
      AutoBean<PluginPatientDataObj> pluginPatientDataAutoBean = AutoBeanUtils.getAutoBean(pluginPatientDataObj);
      jsonArray.put(new JSONObject(AutoBeanCodex.encode(pluginPatientDataAutoBean).getPayload()));
    }
    object.put("pluginPatientData", jsonArray);
    logger.trace("getAll returning object {}", object.toString());
    return object;
  }

  private JSONObject getHistory(JsonRepresentation jsonRepresentation, String callString, String dataType)
      throws ApiStatusException, NotFoundException {
    if (jsonRepresentation == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Missing data");
    }
    logger.trace("Factory is parsing: {} to a PluginPatientGetObj", jsonRepresentation.getJsonObject().toString());
    final PluginPatientGetObj pluginPatientGetObj = AutoBeanCodex.decode(factory, PluginPatientGetObj.class,
        jsonRepresentation.getJsonObject().toString()).as();

    if (pluginPatientGetObj.getPatientId() == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, "Invalid or missing patient");
    }

    Patient patient = getPatient(services, siteInfo, pluginPatientGetObj.getPatientId());
    if (patient == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, callString, "Invalid or missing patient");
    }
    ArrayList<PluginPatientHistoryDataObj> list = services.findAllPluginPatientDataHistory(dataType,
        pluginPatientGetObj.getPatientId(), pluginPatientGetObj.getDataVersion());

    JSONObject object = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    for (PluginPatientHistoryDataObj pluginPatientData : list) {
      AutoBean<PluginPatientHistoryDataObj> pluginPatientDataAutoBean = AutoBeanUtils.getAutoBean(pluginPatientData);
      jsonArray.put(new JSONObject(AutoBeanCodex.encode(pluginPatientDataAutoBean).getPayload()));
    }
    object.put("pluginPatientHistoryData", jsonArray);
    logger.trace("getHistory returning object {}", object.toString());
    return object;
  }
}
