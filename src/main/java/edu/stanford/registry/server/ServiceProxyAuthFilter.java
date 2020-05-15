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

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.github.susom.database.Config;

/**
 * Authentication filter for the remote patient survey war to connect to the patient service.
 *
 * @author garricko
 */
public class ServiceProxyAuthFilter implements Filter {
  private static final Logger log = Logger.getLogger(ServiceProxyAuthFilter.class);
  private static final String PROPERTY_KEY_PREFIX = "property.key.prefix";
  private String user;
  private String password;
  private Config config;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

    config = Config.from()
        .systemProperties()
        .propertyFile(System.getProperty("properties", "").split(","))
        .custom(filterConfig::getInitParameter)
        .custom(filterConfig.getServletContext()::getInitParameter)
        .value(PROPERTY_KEY_PREFIX, "registry").get();

    String propertyKeyPrefix = config.getString(PROPERTY_KEY_PREFIX);
    log.debug(filterConfig.getServletContext().getContextPath() + " using property.key.prefix=" + propertyKeyPrefix);

    user = config.getString(propertyKeyPrefix + ".service.user");
    if (user == null) {
      user = "-survey-app";
    }

    password = config.getString(propertyKeyPrefix + ".service.password");
    if (password == null) {
      log.warn("Proxy services will be blocked because " + propertyKeyPrefix + ".service.password is not set");
    }
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) resp;

    String allegedAuthenticationValue = request.getHeader("X-Authentication");

    if (password != null && password.equals(allegedAuthenticationValue)) {
      doFilterAsSurveyApp(request, resp, chain);
    } else {
      log.error("Authentication failed from " + request.getRemoteHost()
          + " X-Authentication " + (allegedAuthenticationValue == null ? "not set" : "set"));
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Check your server configuration");
    }
  }

  private void doFilterAsSurveyApp(final HttpServletRequest request, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
    final Principal principal = new Principal() {
      @Override
      public String getName() {
        return user;
      }
    };
    try {
      MDC.put("userId", user);

      chain.doFilter(new HttpServletRequestWrapper(request) {
        @Override
        public Principal getUserPrincipal() {
          return principal;
        }
      }, resp);
    } finally {
      MDC.remove("userId");
    }
  }

  @Override
  public void destroy() {
    // Nothing to do
  }
}
