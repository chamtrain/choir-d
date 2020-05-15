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

package edu.stanford.registry.server.service;

import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.service.rest.CustomRestlet;
import edu.stanford.registry.server.service.rest.CustomRestletHandler;
import edu.stanford.registry.server.service.rest.FileLoader;
import edu.stanford.registry.server.service.rest.Importer;
import edu.stanford.registry.server.service.rest.JobProcesser;
import edu.stanford.registry.server.service.rest.ScoresExporter;
import edu.stanford.registry.server.service.rest.StatusRestlet;

import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class RestletServiceManager extends Application {
  private static final Logger logger = Logger.getLogger(RestletServiceManager.class);

  public RestletServiceManager() {
    // A public default construct is required- TomCat creates this as a restlet
  }

  /**
   * Returns the root of this application.
   */
  @Override
  public Restlet createInboundRoot() {  // all these must have public default constructors
    logger.debug("createInboundRoot started");
    Router router = new Router(getContext());
    router.attach("/jsonload", JSONLoader.class);

    router.attach("/fileload", FileLoader.class);
    router.attach("/Patient", FileLoader.class);
    router.attach("/Appointment", FileLoader.class);

    router.attach("/importer", Importer.class);

    router.attach("/sendemails", JobProcesser.class);

    router.attach("/scores", ScoresExporter.class);

    router.attach("/status", StatusRestlet.class);
    router.attach("/status/text", StatusRestlet.class);

    // Add any custom Restlet actions

    Iterable<SiteInfo> iterator = SitesInfo.getSites(RestletServiceManager.class.getName());
    if (iterator != null) {  // If null, the above call already output an error
      for (SiteInfo siteInfo: iterator) {
        RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
        if (customizer != null) {
          Map<String, Class<? extends CustomRestletHandler>> actions = customizer.getRestletActions();
          for (String action : actions.keySet()) {
            router.attach(action, CustomRestlet.class);
          }
        }
      }
    } else {
      getLogger().severe("RestletServiceManager: SitesInfo is not yet initialized, aborted adding custom restlets");
    }
    return router;
  }
}
