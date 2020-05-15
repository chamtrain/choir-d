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

import edu.stanford.registry.server.service.Service;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * This filter is used by incoming survey requests (from patients).
 * The site id should be on a header.
 */
public class PatientServiceFilter implements Filter {
  private Logger logger;

  @Override
  public void init(FilterConfig config) throws ServletException {
    ServletContext servletContext = config.getServletContext();

    // Initialize logging if requested
    String log4jConfig = servletContext.getInitParameter("log4j.configuration");
    if (log4jConfig != null) {
      if (log4jConfig.contains("${context.path}")) {
        String contextPath = servletContext.getContextPath();
        if (contextPath.length() == 0) {
          contextPath = "ROOT";
        } else {
          contextPath = contextPath.substring(1);
        }
        log4jConfig = log4jConfig.replaceAll("\\$\\{context.path\\}", contextPath);
      }
      try {
        log4jConfig = new File(log4jConfig).getAbsolutePath();
        DOMConfigurator.configure(log4jConfig);
      } catch (Exception e) {
        System.err.println("Unable to configure log4j using file: " + log4jConfig);
        e.printStackTrace();
      }
    }
    logger = Logger.getLogger(PatientServiceFilter.class);
    if (log4jConfig == null) {
      logger.info("Appears that log4j has already been initialized");
    } else {
      logger.info("Initialized log4j using file: " + log4jConfig);
    }
  }

  @Override
  public void doFilter(ServletRequest servReq, ServletResponse servRes, FilterChain chain) throws IOException,
      ServletException {

    HttpServletRequest request = (HttpServletRequest) servReq;
    HttpServletResponse response = (HttpServletResponse) servRes;
    Service service = requestedService(request);

    if (service == null) {
      chain.doFilter(servReq, servRes);
      return;
    }

    preventResponseCaching((HttpServletResponse) servRes);
    try {
      if (service == Service.SURVEY2_SERVICE) {
        RegistryServletRequest registryReq = new RegistryServletRequest(request, null);
        try {
          registryReq.addToCurrentThread();
          chain.doFilter(registryReq, servRes);
        } finally {
          RegistryServletRequest.removeFromCurrentThread();
        }
      } else {
        logger.error("Attempt to call service " + service + " - sending 403 Forbidden response");
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Calling that service is not allowed");
        return;
      }
    } catch (Exception e) {
      logger.error("Unable to process the request - sending 500 response", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to process the request");
      return;
    }
  }

  /**
   * Figure out what service is needed to handle a particular request. This will throw a SecurityException if a service cannot be located.
   */
  private Service requestedService(HttpServletRequest req) {
    Service service = null;

    String path = req.getServletPath();

    int idx = path.lastIndexOf('/');
    if (idx != -1) {
      service = Service.byUrlPath(path.substring(idx));
      if (service != null) {
        if (logger.isTraceEnabled()) {
          logger.trace("returning service with interface class " + service.getInterfaceClass() + " for path:" + path);
        }
      } else {
        logger.info("no service for path:" + path);
      }
    } else {
      logger.info("no path, no service");
    }

    return service;
  }

  private void preventResponseCaching(HttpServletResponse response) {
    Date now = new Date();
    response.setDateHeader("Date", now.getTime());
    response.setDateHeader("Expires", now.getTime() - 86400000L /* one day */);
    response.setHeader("Pragma", "no-cache");
    logger.trace("Setting no-cache");
    response.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
  }

  @Override
  public void destroy() {
    // Nothing to do
  }
}
