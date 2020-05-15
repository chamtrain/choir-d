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

package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.RegistryServletRequest;
import edu.stanford.registry.server.config.AppConfig;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.service.Service;
import edu.stanford.survey.client.api.SurveyService;
import edu.stanford.survey.server.SurveySystemFactory;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Config;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.seguedevelopment.gwtrpccommlayer.client.GwtRpcClientSideProxy;
import com.seguedevelopment.gwtrpccommlayer.client.IGwtRpcClientSideProxy;

/**
 * On the patient-facing web application we use a proxy to send these service
 * calls through to the real service running on a different Tomcat.
 */
public class SurveyServiceProxy extends RemoteServiceServlet implements SurveyService {
  private static final long serialVersionUID = 1L;
  private static final Logger log = LoggerFactory.getLogger(SurveyServiceProxy.class);
  private static final String FACTORY_KEY = "factory.survey.system";
  private static final String DISABLED = "disabled";
  private Config config;
  private SurveySystemFactory factory;

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);

    config = Config.from()
        .systemProperties()
        .propertyFile(System.getProperty("properties", "").split(","))
        .custom(servletConfig::getInitParameter)
        .custom(servletConfig.getServletContext()::getInitParameter)
        .value(FACTORY_KEY, SurveySystemFactoryImpl.class.getName()).get();

    String factoryClass = config.getString(FACTORY_KEY);
    try {
      factory = (SurveySystemFactory) Thread.currentThread().getContextClassLoader().loadClass(factoryClass)
          .getConstructor(Supplier.class, AppConfig.class, SitesInfo.class).newInstance(null, null, null);
      log.info("Created {} as {}", FACTORY_KEY, factoryClass);
    } catch (Exception e) {
      throw new ServletException("Unable to load class: " + FACTORY_KEY + "=" + factoryClass, e);
    }
  }

  @Override
  public String[] startSurvey(String systemId, String surveyToken) {
    try {
      SurveyService svc = getService();
      return svc.startSurvey(systemId, surveyToken);
    } catch (Exception e) {
      log.error("Unexpected error", e);
      return retry();
    }
  }

  @Override
  public String[] continueSurvey(String statusJson, String answerJson) {
    try {
      return getService().continueSurvey(statusJson, answerJson);
    } catch (Exception e) {
      log.error("Unexpected error", e);
      return retry();
    }
  }

  @Override
  public String[] resumeSurvey(String resumeToken) {
    try {
      final RegistryServletRequest request = RegistryServletRequest.forCurrentThread();
      String surveySystem = request.getHeader("X-SURVEY-SYSTEM");
      if ("0".equals(surveySystem)) {
        return getSurveySites();
      }
      return getService().resumeSurvey(resumeToken);
    } catch (Exception e) {
      log.error("Unexpected error", e);
      return retry();
    }
  }
  
  @Override
  public void addPlayerProgress(String statusJson, String targetId, String action, Long milliseconds) {
    try {
      getService().addPlayerProgress(statusJson, targetId, action, milliseconds);
    } catch (Exception e) {
      log.error("Unexpected error", e);
    }
  }

  @Override
  public String[] getSurveySites() {
    ArrayList<String> allSites = new ArrayList<>();

    for (String key : factory.proxyPropertyKeys()) {
      String serviceUrl = config.getString(key);
      log.trace("SitesPage getSurveySites() {} sites for proxy: {} with url {} ",
          (DISABLED.equals(serviceUrl) ? "not including" : "including"), key, serviceUrl);
      if (!DISABLED.equals(serviceUrl)) {
        String[] sites = getService(RegistryServletRequest.forCurrentThread(), key).getSurveySites();
        Collections.addAll(allSites, sites);
      }
    }
    Collections.sort(allSites);
    String[] returnSites = new String[allSites.size()];
    returnSites = allSites.toArray(returnSites);
    return returnSites;
  }

  private String[] retry() {
    return new String[] { "{\"sessionStatus\"=\"retry\"}", "{}" };
  }

  /**
   * Get the internal service to handle the request
   */
  private SurveyService getService() {
    final RegistryServletRequest request = RegistryServletRequest.forCurrentThread();
    String surveySystem = request.getHeader("X-SURVEY-SYSTEM");

    if ("0".equals(surveySystem)
        && !(RegistryServletRequest.forCurrentThread().getService() == null)) {
      return (SurveyService) RegistryServletRequest.forCurrentThread().getService();
    }
    // Figure out the location of the appropriate remote service
    try {
      String serviceUrlKey = factory.proxyPropertyKeyFor(surveySystem);
      return getService(request, serviceUrlKey);
    } catch (RuntimeException ex) {
      log.error("For survey system: {}", surveySystem);
      throw ex;
    }
  }

  private SurveyService getService(RegistryServletRequest request, String serviceUrlKey) throws RuntimeException {
    String serviceUrl = config.getString(serviceUrlKey);
    if (serviceUrl == null || serviceUrl.length() == 0) {
      log.error("Couldn't find service url with key = {} ", serviceUrlKey);
      throw new RuntimeException("Configuration error - see server logs");
    }
    if (serviceUrl.endsWith("/patient")) {
      serviceUrl = serviceUrl.substring(0, serviceUrl.length() - 8);
    }
    if (serviceUrl.endsWith("/")) {
      serviceUrl = serviceUrl.substring(0, serviceUrl.length() - 1);
    }
    if (!serviceUrl.endsWith(Service.SURVEY2_SERVICE.getUrlPath())) {
      serviceUrl += Service.SURVEY2_SERVICE.getUrlPath();
    }
    if (serviceUrl.startsWith("/")) {
      StringBuffer requestURL = request.getRequestURL();
      requestURL.setLength(requestURL.length() - request.getRequestURI().length());
      serviceUrl = requestURL.toString() + serviceUrl;
    }
    URL url;
    try {
      url = new URL(serviceUrl);
    } catch (MalformedURLException e) {
      log.error("Bad value for configuration property: {}", serviceUrlKey, e);
      throw new RuntimeException("Configuration error - see server logs");
    }
    if (log.isTraceEnabled()) {
      log.trace("Will connect to remote SurveyService using url: {}", serviceUrl);
    }

    // Lookup a password to use with this service
    String servicePasswordKey;
    if (serviceUrlKey.endsWith(".url")) {
      servicePasswordKey = serviceUrlKey.substring(0, serviceUrlKey.length() - 4) + ".password";
    } else {
      servicePasswordKey = serviceUrlKey + ".password";
    }
    final String servicePassword = config.getString(servicePasswordKey);
    if (servicePassword == null || servicePassword.length() == 0) {
      log.error("Couldn't find configuration property: {}", servicePasswordKey);
      throw new RuntimeException("Configuration error - see server logs");
    }

    IGwtRpcClientSideProxy handler = new GwtRpcClientSideProxy() {
      @Override
      protected void onBeforeRequest(HttpPost post) {
        post.setHeader("X-Authentication", servicePassword);
        post.setHeader("X-DEVICE-TOKEN", validateToken(request.getHeader("X-DEVICE-TOKEN")));
        post.setHeader("X-SURVEY-TOKEN", validateToken(request.getHeader("X-SURVEY-TOKEN")));
        post.setHeader("X-FORWARDED-FOR", validateForwardedFor(request.getHeader("X-FORWARDED-FOR")));
        post.setHeader("User-Agent", validateUserAgent(request.getHeader("User-Agent")));
      }
    };
    handler.setUrl(url);
    handler.setShowResponseHeaders(false);

    return (SurveyService) Proxy.newProxyInstance(SurveyService.class.getClassLoader(),
        new Class[] { SurveyService.class }, handler);
  }

  private String validateToken(String header) {
    if (header == null) {
      return null;
    }

    if (header.matches("^[a-zA-Z0-9:]*$")) {
      return header;
    }
    log.warn("Blocking request because of invalid header value token: {}", header);
    throw new SecurityException("Validation failed");
  }

  private String validateForwardedFor(String header) {
    if (header == null) {
      return null;
    }

    if (header.matches("^[a-zA-Z0-9., :]*$")) {
      return header;
    }
    log.warn("Not setting forwarded-for because of invalid header value: {}", header);
    return null;
  }

  private String validateUserAgent(String header) {
    if (header == null) {
      return null;
    }

    if (header.matches("^[\\u0020-\\u007E]*$")) {
      return header;
    }
    log.warn("Not setting user-agent because of invalid header value: {}", header);
    return null;
  }

}
