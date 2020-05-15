/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.RegistryServletRequest;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.service.rest.CustomRestletHandler.ResponseType;
import edu.stanford.registry.shared.Constants;

/**
 * Custom Restlet. This gets the RegistryCustomizer to map the
 * Restlet request to the handler for the request.
 */
public class CustomRestlet extends ServerResource {

  private static Logger logger = Logger.getLogger(CustomRestlet.class);

  public CustomRestlet() {
    // This must have a public default constructor
  }

  @Override
  protected Representation get() {
    // Get the URL path which is the name of the requested action
    RegistryServletRequest request = (RegistryServletRequest) ServletUtils.getRequest(getRequest());
    AdministrativeServices service = (AdministrativeServices) request.getService();
    String action = request.getPathInfo();
    Map<String,String[]> params = request.getParameterMap();

    // Get the mapping from action to handler class from the customizer
    RegistryCustomizer customizer = request.getSiteInfo().getRegistryCustomizer();
    Map<String,Class<? extends CustomRestletHandler>> actions = customizer.getRestletActions();
    Class<? extends CustomRestletHandler> handlerClass = actions.get(action);
    if (handlerClass == null) {
      logger.error("Unable to find handler class for service " + action);
      return null;
    }

    Long siteId = getSiteId(request);

    // Instantiate the handler
    CustomRestletHandler handler;
    try {
      handler = handlerClass.getConstructor(Long.class).newInstance(siteId);
      handler.setAdminServices(service);
      handler.setParams(params);
    } catch(Exception e) {
      logger.error("Error instantiating handler class " + handlerClass, e);
      return null;
    }

    // Get the representation of the result
    ResponseType type = handler.getResponseType();
    Representation rep = getRepresentation(type, handler);

    // If unable to get contents log an error and return null
    if (rep == null) {
      logger.error("Unable to get content for request to " + this.getClass().getName());
      return null;
    }

    // If a filename is specified then set the filename in the content disposition
    String filename = handler.getFilename();
    if (filename != null) {
      Disposition dep = new Disposition();
      dep.setFilename(filename);
      dep.setType(Disposition.TYPE_INLINE);
      rep.setDisposition(dep);
    }

    return rep;
  }

  Long getSiteId(RegistryServletRequest request) {
    String siteName = request.getHeader(Constants.SITE_ID_HEADER);
    if (siteName == null || siteName.isEmpty())
      siteName = request.getParameter(Constants.SITE_ID);
    
    return null;
  }

  /**
   * Get the representation of the result. This calls the
   * writeContents method to write the actual content stream.
   */
  protected Representation getRepresentation(ResponseType type, final CustomRestletHandler handler) {
    // Set the appropriate media type for the result
    MediaType mediaType = null;
    switch(type) {
      case TEXT_PLAIN:
        mediaType = MediaType.TEXT_PLAIN;
        break;
      case TEXT_CSV:
        mediaType = MediaType.TEXT_CSV;
        break;
      case JSON:
        mediaType = MediaType.APPLICATION_JSON;
        break;
      default:
        logger.error("Invalid response type for restlet service");
        return null;
    }

    Representation rep = new OutputRepresentation(mediaType) {
      @Override
      public void write(OutputStream outStream) throws IOException {
        handler.writeContents(outStream);
      }
    };

    return rep;
  }
}
