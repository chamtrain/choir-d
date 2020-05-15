/*
 * Copyright 2013-2017 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.AuthenticatedUser;
import edu.stanford.registry.server.RegistryServletRequest;
import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.config.ServerInit;
import edu.stanford.registry.server.database.Metric;
import edu.stanford.registry.shared.User;
import edu.stanford.survey.server.ClientIdentifiers;
import edu.stanford.survey.server.SurveyDao;

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
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.DatabaseProvider.Builder;

public class ServiceFilter implements Filter {
  Builder databaseBuilder;
  ServerContext serverContext;
  Logger logger;
  ServerInit serverInit;
  boolean errorDuringInit = false; // turn off filter if there's an error

  @Override
  public void destroy() {
    if (serverInit != null) {
      serverInit.destroy();
    }
  }

  /**
   * This is called by Tomcat. It fails catastrophically if the logger or database can not initialize
   */
  @Override
  public void init(FilterConfig config) throws ServletException {
    init(config.getServletContext());
  }

 /**
   * Call this from another environment, like an Eclipse Google App Engine project.
   * Subclass ServletContext, and implement
   * <br>void log(String)
   * <br>String getContextPath()
   * <br>String getRealPath()
   * <br>Enumeration<String> e = servletContext.getInitParameterNames()
   * <br>String getInitParameter(String key)

   */
  public void init(ServletContext servletContext) throws ServletException {
    serverInit = ServerInit.getInstance(servletContext);
    logger = LoggerFactory.getLogger(ServiceFilter.class);
    serverContext = serverInit.getServerContext();
    databaseBuilder = serverInit.getDatabaseBuilder();
  }

  // ====================
  protected final Object createSessionLock = new Object();
  protected final Object sessionsLock = new Object();

  @Override
  public void doFilter(ServletRequest servReq, ServletResponse servRes, FilterChain chain) throws IOException,
      ServletException {

    final HttpServletRequest request = (HttpServletRequest) servReq;
    HttpServletResponse response = (HttpServletResponse) servRes;
    Metric metric = new Metric(logger.isDebugEnabled());
    Service service = requestedService(request);
    if (service == null) {
      chain.doFilter(servReq, servRes);
      return;
    }

    if (errorDuringInit) {
      return;
    }

    preventBrowserResponseCaching(response);  // for security

    if (serverContext == null) {
      logger.info("ServiceFilter.doFilter: serverContext is null");
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "The server has not been configured properly (see previous log entries)");
      return;
    }

    String serviceName = service.getInterfaceClass().getName();

    AuthenticatedUser authedUser = new AuthenticatedUser(logger, serverContext, request, serviceName);
    if (!authedUser.isAuthenticated()) {
      User user = authedUser.getUser();
      logger.info((user == null ? "(null)" : user.getUsername()) + " lacks permission to access: "+serviceName);
      String msg = (user == null ? "" : (user.getUsername()) + ": ") + "You lack permission to use this site";
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Security Error: "+msg);
      return;
    }
    User user = authedUser.getUser();

    handleSessions(request, service, user);

    ClientIdentifiers clientIds = getClientIds(request);

    // Create the service for this request and pass it to downstream filters
    SiteInfo siteInfo = authedUser.getSiteInfo();
    Object svce = new ServiceProxyFactory(databaseBuilder).createService(clientIds, user, serverContext, service, siteInfo);
    RegistryServletRequest registryReq = new RegistryServletRequest(siteInfo, request, svce);
    try {
      registryReq.addToCurrentThread();
      chain.doFilter(registryReq, servRes);
    } finally {
      RegistryServletRequest.removeFromCurrentThread();
      if (logger.isDebugEnabled()) {
        metric.done();
        logger.debug("Serve request: " + metric.getMessage() + " for " + request.getRequestURI());
      }
    }
  }


  void handleSessions(HttpServletRequest request, Service service, User user) {
    // Log each session's first access to a service
    String remoteAddr = request.getRemoteAddr();
    String sessionKey = "Servlet.accessed." + service.getUrlPath();
    HttpSession session = request.getSession(false);

    // Avoids creation of multiple sessions for a browser
    if (session == null) {
      synchronized (createSessionLock) {
        session = request.getSession(false);
        if (session == null) {
          session = request.getSession(true);
          if (logger.isTraceEnabled()) logger.trace("Created new session: " + session.getId());
        } else if (logger.isTraceEnabled()) {
            logger.trace("Using existing session: " + session.getId());
        }
      }
    }

    String sessionValue = (String) session.getAttribute(sessionKey);
    // We also log the access if the user's IP changed
    if (sessionValue == null || !sessionValue.equals(remoteAddr)) {
      boolean logIt = false;
      synchronized (sessionsLock) {
        String doubleCheckValue = (String) session.getAttribute(sessionKey);
        if (doubleCheckValue == null || !doubleCheckValue.equals(remoteAddr)) {
          session.setAttribute(sessionKey, remoteAddr);
          logIt = true;
        }
      }
      if (logIt) {
        ServiceLogin login = new ServiceLogin(user, service, remoteAddr, request.getHeader("User-Agent"));
        serverContext.recordServiceLogin(login);
      }
    }
  }

  ClientIdentifiers getClientIds(HttpServletRequest request) {
    return new ClientIdentifiers() {
      Long userAgentId;
      @Override
      public Long userAgentId(final Long siteId) {
        if (userAgentId == null) {
          String userAgentStr = request.getHeader("User-Agent");
          if (userAgentStr == null || userAgentStr.length() == 0) userAgentStr = "Unknown";
          // Ensure we do this in a separate transaction so only unique values of user agent strings are stored
          final String finalUserAgentStr = userAgentStr;
          databaseBuilder.transact(dbp -> {
            SurveyDao dao = new SurveyDao(dbp);
            userAgentId = dao.findOrCreateUserAgent(siteId, finalUserAgentStr);
          });
        }
        return userAgentId;
      }

      @Override
      public String getClientIpAddress() {
        return request.getHeader("X-FORWARDED-FOR");
      }

      @Override
      public String getDeviceToken() {
        return request.getHeader("X-DEVICE-TOKEN");
      }
    };
  }


  private void preventBrowserResponseCaching(HttpServletResponse response) {
    Date now = new Date();
    response.setDateHeader("Date", now.getTime());
    response.setDateHeader("Expires", now.getTime() - 86400000L /* one day */);
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
  }


  private String getReqServiceName(HttpServletRequest req) {
    String path = req.getServletPath();
    int idx = path.lastIndexOf('/');
    if (idx != -1) {
      return path.substring(idx);
    }

    return null;
  }


  /**
   * Figure out what service is needed to handle a particular request.
   *
   * @return the service, or null if no appropriate service could be located
   */
  private Service requestedService(HttpServletRequest req) {
    Service service = null;
    String serviceName = getReqServiceName(req);
    if (serviceName == null) {
      logger.warn("No path, no service");
      return null;
    }

    logger.info(String.format("service url name[%s]", serviceName));
    service = Service.byUrlPath(serviceName);
    if (service == null) {
      logger.warn(String.format("No service for path[%s]", serviceName));
      return null;
    }

    if (logger.isTraceEnabled()) {
        logger.trace("Returning service with interface class [" + service.getInterfaceClass() + "] for path: " + serviceName);
    }
    return service;
  }

}
