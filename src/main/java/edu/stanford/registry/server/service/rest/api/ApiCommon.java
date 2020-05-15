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

import edu.stanford.registry.client.api.ApiObjectFactory;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.rest.ApiStatusException;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public abstract class ApiCommon {
  final static Logger logger = LoggerFactory.getLogger(edu.stanford.registry.server.service.rest.api.ApiCommon.class);
  protected final ApiObjectFactory factory = AutoBeanFactorySource.create(ApiObjectFactory.class);

  protected String dateString(SiteInfo siteInfo, Date date) {
    return (date == null) ? "" : siteInfo.getDateOnlyFormatter().getDateString(date);
  }
  
  protected Date parseDate(SiteInfo siteInfo, String dateStr)  throws ApiStatusException {
    Date date = null;
    if ((dateStr != null) && !dateStr.isEmpty()) {
      try {
        date = siteInfo.parseDateOnly(dateStr);
      } catch (ParseException pe) {
        throw new ApiStatusException(Status.CLIENT_ERROR_NOT_ACCEPTABLE, "Invalid date format");
      }
    }
    return date;
  }

  /**
   * Convert a JSONObject into an object that implements the specified
   * bean interface backed by an AutoBean.
   */
  protected <T> T jsonObjToBeanObj(JSONObject jsonObj, Class<T> intf) throws ApiStatusException {
    if (jsonObj == null) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, null, "Missing data");
    }
    try {
      T beanObj = AutoBeanCodex.decode(factory, intf, jsonObj.toString()).as();
      return beanObj;
    } catch(Exception e) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, null, "JSON error: " + e.getMessage());      
    }
  }

  /**
   * Convert an object backed by an AutoBean into a JSONObject.
   */
  protected JSONObject beanObjToJsonObj(Object autoBeanObj) {
    AutoBean<?> autobean = AutoBeanUtils.getAutoBean(autoBeanObj);
    String json = AutoBeanCodex.encode(autobean).getPayload();
    return new JSONObject(json);
  }


  /**
   * Make a JSON Object containing a JSON Array from a list of autoBean objects.
   */
  protected JSONObject makeResultList(String listName, List autoBeanObjs) {
    JSONArray jsonData = new JSONArray();
    for(Object autoBeanObj : autoBeanObjs) {
      jsonData.put(beanObjToJsonObj(autoBeanObj));
    }

    JSONObject result = new JSONObject();
    result.put(listName, jsonData);
    return result;
  }

  protected JSONObject jsonFromString(String str, String str2) {
    JSONObject jsonObj = null;
    try {
      jsonObj = new JSONObject().put(str, str2);
    } catch (JSONException e) {
      logger.error("JSON error creating response", e);
    }

    return jsonObj;
  }
  
  protected JSONObject notImplemented(String service) {
    return jsonFromString("Response:", "The API call " + service + " is not implemented yet.");
  }
}
