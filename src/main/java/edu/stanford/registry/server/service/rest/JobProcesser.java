/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.service.rest;

import edu.stanford.registry.server.RegistryServletRequest;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.AdministrativeServices;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;

public class JobProcesser extends ServerResource {

  private static Logger logger = Logger.getLogger(JobProcesser.class);

  public JobProcesser() {  // A public default construct is required- TomCat creates this as a restlet
    super();
    getVariants().add(new Variant(MediaType.TEXT_HTML));
  }

  @Override
  public Representation doHandle(Variant variant) {

    if (variant != null) {
      if (MediaType.TEXT_HTML.equals(variant.getMediaType(), true)) {
        return handleText();
      } else {
        logger.warn("FileLoader: mediatype of " + variant.getMediaType() + " is not valid.");
        setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
      }
    } else {
      // POST request with no entity.
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }
    return null;
  }

  /**
   * This accepts a text request to process a job which is identified by the path
   *
   * @param adminService
   * @return
   */
  private Representation handleText() {
    RegistryServletRequest req = (RegistryServletRequest) ServletUtils.getRequest(getRequest());
    logger.debug("req uri='" + req.getRequestURI() + "' pathinfo='" + req.getPathInfo() + "' context path='"
        + req.getContextPath() + "'");

    // String serverUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
    String jobName = req.getPathInfo();
    if (jobName != null && jobName.startsWith("/") && jobName.length() > 1) {
      jobName = jobName.substring(1);
    }
    logger.debug("servlet called with jobName:" + jobName);

    if (jobName == null) {
      return getResponse("ERROR=null_job");
    }
    if (jobName.equals("sendemails")) {
      try {
        AdministrativeServices adminService = (AdministrativeServices) req.getService();
        SiteInfo siteInfo = req.getSiteInfo();
        String serverUrl = siteInfo.getProperty("survey.link");
        adminService.doSurveyInvitations(siteInfo.getMailer(), serverUrl);
        return getResponse("success");
      } catch (IOException e) {
        logger.error("Error processing 'sendemails' ", e);
        return getResponse("ERROR=FAILED:" + e.getMessage());
      }
    } else {
      return getResponse("ERROR=UNRECOGNIZED_JOBNAME:" + jobName);
    }

  }

  private JsonRepresentation getResponse(String responseString) {
    JsonRepresentation rep = null;
    JSONObject jsonObj = null;
    try {
      jsonObj = new JSONObject("{ response : " + responseString + " }");
    } catch (JSONException e) {
      logger.error("JSON error creating response", e);
    }
    rep = new JsonRepresentation(jsonObj);
    return rep;
  }
}
