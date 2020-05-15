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

package edu.stanford.registry.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

abstract public class InitRegistryServlet extends RemoteServiceServlet {

  private static final long serialVersionUID = 6416553453634731583L;

  private Logger logger = Logger.getLogger(InitRegistryServlet.class);
  ServletContext servletContext = null;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    if (servletContext != null) {
      return; // -- we are already initialized
    }

    servletContext = config.getServletContext();
    if (servletContext == null) {
      logger.debug(" ServletContext is null");
    }

    // Initialize server utilities
    ServerUtils.initialize(servletContext.getRealPath("/"));
  }

}
