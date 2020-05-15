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
package edu.stanford.registry.server.service;

import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.service.rest.ApiController;

import java.util.List;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns the root of the application API.
 *
 * @author tpacht
 */
public class ApiServiceManager extends Application {
  private static final Logger logger = LoggerFactory.getLogger(ApiServiceManager.class);

  public ApiServiceManager() {
    // A public default construct is required- TomCat creates this as a restlet
  }

  /**
   * During construction, the getRequest() returns null...
   */
  @Override
  public Restlet createInboundRoot() {  // all these must have public default constructors
    Router router = new Router(getContext());

    router.attach("/json/assessment/id/{id}", ApiController.class);
    router.attach("/json/assessment/patient/{patientId}", ApiController.class);
    router.attach("/json/assessment/mod", ApiController.class); // todo: implement
    router.attach("/json/assessment/modarray", ApiController.class); // todo: implement
    router.attach("/json/assessment/cancel/{id}", ApiController.class);// todo: implement

    router.attach("/json/patient/{id}", ApiController.class);
    router.attach("/json/patient/mod", ApiController.class);
    router.attach("/json/patient/decline", ApiController.class);
    router.attach("/json/patient/register", ApiController.class);

    router.attach("/json/patattribute/{patientId}/{attributeName}", ApiController.class);
    router.attach("/json/patattribute/{patientId}", ApiController.class);
    router.attach("/json/patattribute/mod/{patientId}", ApiController.class);
    router.attach("/json/patattribute/rem/{patientId}", ApiController.class);

    router.attach("/json/patreport/token/{token}", ApiController.class);
    router.attach("/json/patreport/patient/{patientId}", ApiController.class);
    router.attach("/json/patreport/id/{id}", ApiController.class);

    router.attach("/json/patreg/{id}", ApiController.class);
    router.attach("/json/patreg/mod", ApiController.class);
    router.attach("/json/patreg/decline", ApiController.class);
    router.attach("/json/patreg/register", ApiController.class);

    router.attach("/json/survey/token/{token}", ApiController.class);

    router.attach("/json/surveyattribute/{surveyregid}/attributeName}", ApiController.class);
    router.attach("/json/surveyattribute/{surveyregid}", ApiController.class);
    router.attach("/json/surveyattribute/mod", ApiController.class);

    router.attach("/json/user/all", ApiController.class);
    router.attach("/json/user/dname/{name}", ApiController.class);
    router.attach("/json/user/uname/{name}", ApiController.class);

    router.attach("/json/extract/{tablename}", ApiController.class);
    router.attach("/json/report/{reportname}", ApiController.class);

    router.attach("/json/site/all", ApiController.class);
    router.attach("/json/site/id/{id}", ApiController.class);
    router.attach("/json/site/param/{urlparam}", ApiController.class);

    router.attach("/json/pluginData/patient/post/{dataType}", ApiController.class);
    router.attach("/json/pluginData/patient/getLast/{dataType}", ApiController.class);
    router.attach("/json/pluginData/patient/getAll/{dataType}", ApiController.class);

    // Add path templates for custom APIs
    Iterable<SiteInfo> iterator = SitesInfo.getSites(RestletServiceManager.class.getName());
    if (iterator != null) {
      for (SiteInfo siteInfo: iterator) {
        RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
        if (customizer != null) {
          List<String> pathTemplates = customizer.getApiCustomPaths();
          if (pathTemplates != null) {
            for(String pathTemplate : pathTemplates) {
              router.attach(pathTemplate, ApiController.class);
            }
          }
        }
      }
    }

    logger.debug("ApiServiceManager routes are initialized");
    return router;
  }
}
