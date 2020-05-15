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
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.shared.api.SiteObj;

import java.util.ArrayList;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

public class SiteRequestHandler extends ApiPatientCommon {
  private final ClinicServices services;

  public SiteRequestHandler(SiteInfo siteInfo, ClinicServices clinicServices) {
    this.services = clinicServices;
  }

  public JSONObject handle(String callString, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    String[] requestElements = callString.split("/");
    if (requestElements.length < 2) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, requestElements[0]);
    }
    switch (requestElements[1]) {
    case "all":
      return getSiteList(services);
    case "id":
      if (requestElements.length > 2) {
        return getSiteById(services, requestElements[2]);
      }
      break;
    case "param":
      if (requestElements.length > 2) {
        return getSiteByParam(services, requestElements[2]);
      }
      break;
    }
    throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "invalid request");
  }

  private JSONObject getSiteList(ClinicServices clinicServices) {
    JSONObject sitesObj = new JSONObject();
    ArrayList<SiteObj> sites = clinicServices.getSiteObjs();
    for (SiteObj site : sites) {
      JSONObject siteObj = new JSONObject(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(site)).getPayload());
      sitesObj.accumulate("site", siteObj);
    }
    return sitesObj;
  }

  private JSONObject getSiteByParam(ClinicServices clinicServices, String param) throws ApiStatusException {
    SiteObj obj = clinicServices.getSiteObjByParam(param);
    if (obj != null) {
      return new JSONObject((AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(obj)).getPayload()));
    }
    throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, "/site/param/" + param);
  }

  private JSONObject getSiteById(ClinicServices clinicServices, String id) throws ApiStatusException {
    try {
      SiteObj obj = clinicServices.getSiteObjById(Long.parseLong(id));
      if (obj != null) {
        return new JSONObject(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(obj)).getPayload());
      }
    } catch (NumberFormatException nfe) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "/site/id/" + id);

    }
    throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, "/site/id/" + id);
  }
}
