/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Listener to initialize Log4j when a web application is deployed,
 * and shutdown cleanly when it is destroyed.
 *
 * This is used by the survey-proxy application, but registry
 * handles it as part of its ServerInit life-cycle.
 */
public class Log4jContextListener implements ServletContextListener {

  static protected Log4jContextListener listener;

  /**
   * This is here so destroy can easily be called
   * @return
   */
  static public Log4jContextListener get() {
    return listener;
  }

  @Override
  public void contextInitialized(ServletContextEvent contextEvent) {
    contextInitialized(contextEvent.getServletContext());
  }

  // To call from another servlet, e.g. FilterService
  public void contextInitialized(ServletContext servletContext) {
    if (listener != null)
      return;

    listener = this;
    String contextPath = servletContext.getContextPath();

    // Initialize logging if requested
    String log4jConfig = servletContext.getInitParameter("log4j.configuration");
    if (log4jConfig == null) {
      servletContext.log("ERROR: Log4j cannot be setup due to lack of servlet context 'log4j.configuration' parameter for " + contextPath);
      return;
    }

    if (log4jConfig.contains("${context.path}")) {
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
      servletContext.log("Successfully configured log4j for " + contextPath + " using file: " + log4jConfig);
    } catch (Exception e) {
      servletContext.log("Unable to configure log4j for " + contextPath + " using file: " + log4jConfig, e);
    }

    Logger log = Logger.getLogger(Log4jContextListener.class);
    log.info("Initialized log4j for '" + contextPath + "' using file: " + log4jConfig);
  }


  @Override
  public void contextDestroyed(ServletContextEvent contextEvent) {
    ServletContext servletContext = contextEvent.getServletContext();
    String contextPath = servletContext.getContextPath();

    // Make sure locks on log files are released
    LogManager.shutdown();
    servletContext.log("Shutdown log4j for " + contextPath);
  }
}
