/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.hl7;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigDao.ConfigType;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.config.ServerInit;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.shared.ClientConfig;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider.Builder;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ca.uhn.hl7v2.hoh.api.DecodeException;
import ca.uhn.hl7v2.hoh.api.IAuthorizationServerCallback;
import ca.uhn.hl7v2.hoh.api.IMessageHandler;
import ca.uhn.hl7v2.hoh.api.IResponseSendable;
import ca.uhn.hl7v2.hoh.api.MessageMetadataKeys;
import ca.uhn.hl7v2.hoh.api.MessageProcessingException;
import ca.uhn.hl7v2.hoh.encoder.AuthorizationFailureException;
import ca.uhn.hl7v2.hoh.encoder.Hl7OverHttpRequestDecoder;
import ca.uhn.hl7v2.hoh.raw.api.RawReceivable;
import ca.uhn.hl7v2.hoh.sign.SignatureVerificationException;
import ca.uhn.hl7v2.hoh.util.HTTPUtils;

/**
 * Servlet implementation of HAPI HL7 over HTTP (HoH) to receives hL7 messages into CHOIR
 *
 * @author tpacht@stanford.edu
 * @since 09/2019
 */
public class Hl7HohServlet extends RemoteServiceServlet {
  // Configuration property names
  private static final String HL7FACTORY_PARAM = "Hl7CustomizerFactory";

  private Hl7CustomizerFactoryIntf hl7CustomizerFactory;

  private static final Logger logger = LoggerFactory.getLogger(Hl7HohServlet.class);

  private final HashMap<String, SiteInfo> siteNames = new HashMap<>();
  private IAuthorizationServerCallback myAuthorizationCallback;
  private IMessageHandler<String> myMessageHandler;

  public Hl7HohServlet() {
    super();
  }

  @Override
  protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws IOException {
    theResp.setStatus(400);
    theResp.setContentType("text/html");
    String message = "GET method is not supported by this server";
    HTTPUtils.write400BadRequest(theResp.getOutputStream(), message, false);
  }

  /**
   * Initializes the servlet
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    try {
      ServerInit serverInit = ServerInit.getInstance(getServletContext());
      SitesInfo sitesInfo = serverInit.getServerContext().getSitesInfo();
      Builder builder = serverInit.getDatabaseBuilder().withTransactionControl();
      Database database = builder.create().get();

      // See if there's a custom factory class in the configuration
      AppConfigDao appConfigDao = new AppConfigDao(database);
      AppConfigEntry appConfig =
          appConfigDao.findAppConfigEntry(0L, ConfigType.CONFIGPARAM.toString(), HL7FACTORY_PARAM);
      String factoryClass = appConfig != null ? appConfig.getConfigValue() : null;
      if (factoryClass != null) {
        hl7CustomizerFactory = getHl7FactoryFromClass(factoryClass);
      }
      if (hl7CustomizerFactory == null) {
        hl7CustomizerFactory = new Hl7CustomizerFactory();
      }

      myAuthorizationCallback = hl7CustomizerFactory.getServerCallback();
      setAuthorizationCallback(myAuthorizationCallback);

      Collection<SiteInfo> siteCollection = sitesInfo.getAll();
      for (SiteInfo site : siteCollection) {
        ClientConfig clientConfig = site.getRegistryCustomizer().getClientConfig();
        Map<String, List<String>> clinicMap = clientConfig.getClinicFilterMapping();
        if (clinicMap != null) {
          for (String clinicMapKey : clinicMap.keySet()) {
            List<String> clinicNames = clinicMap.get(clinicMapKey);
            if (clinicNames != null) {
              for (String clinicName : clinicNames) {
                logger.trace("Initializing clinic named: {} to site {}", clinicName, site.getSiteId());
                siteNames.put(clinicName.toUpperCase(), site);
              }
            }
          }
        }
      }
    } catch (Exception se) {
      logger.error("Error initializing servlet", se);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void service(HttpServletRequest theReq, HttpServletResponse theResp) throws IOException, ServletException {
    ServerInit serverInit = ServerInit.getInstance(getServletContext());
    Builder builder = serverInit.getDatabaseBuilder().withTransactionControl();
    Database database = builder.create().get();
    myMessageHandler = new IncomingMessageHandler(database, hl7CustomizerFactory, siteNames);
    setMessageHandler(myMessageHandler);

    Hl7OverHttpRequestDecoder decoder = new Hl7OverHttpRequestDecoder();
    decoder.setHeaders(new LinkedHashMap<>());

    Enumeration<?> headerNames = theReq.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String nextName = (String) headerNames.nextElement();
      decoder.getHeaders().put(nextName, theReq.getHeader(nextName));
    }
    decoder.setPath(theReq.getRequestURI());
    decoder.setAuthorizationCallback(myAuthorizationCallback);

    try {
      decoder.readContentsFromInputStreamAndDecode(theReq.getInputStream());
    } catch (AuthorizationFailureException e) {
      logger.error("Authorization failed on request for {}", theReq.getRequestURI(), e);
      theResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      HTTPUtils.write401Unauthorized(theResp.getOutputStream(), false);
      return;
    } catch (DecodeException e) {
      logger.error("Request failure for {}", theReq.getRequestURI(), e);
      theResp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      HTTPUtils.write400BadRequest(theResp.getOutputStream(), e.getMessage(), false);
      return;
    } catch (SignatureVerificationException e) {
      logger.error("Signature verification failed on request for {}", theReq.getRequestURI(), e);
      theResp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      HTTPUtils.write400SignatureVerificationFailed(theResp.getOutputStream(), false);
      return;
    }

    Charset charset = decoder.getCharset();
    logger.trace("Message charset is {}", charset.displayName());
    theResp.setCharacterEncoding(charset.name());

    RawReceivable rawMessage = new RawReceivable(decoder.getMessage());
    rawMessage.addMetadata(MessageMetadataKeys.REMOTE_HOST_ADDRESS.name(), theReq.getRemoteAddr());

    IResponseSendable<String> response;
    try {
      response = myMessageHandler.messageReceived(rawMessage);
    } catch (MessageProcessingException e) {
      logger.error("Processing problem for {}", theReq.getRequestURI(), e);
      theResp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      HTTPUtils.write500InternalServerError(theResp.getOutputStream(), e.getMessage(), false);
      return;
    }
    if (response == null) {
      logger.trace("response is null!");
      theResp.setContentType("text/html");
    } else {
      if (response.getEncodingStyle() == null || empty(response.getEncodingStyle().getContentType())) {
        theResp.setContentType("text/html");
        logger.trace("defaulting theResponse contenttype to text/html");
      } else {
        logger.trace("setting theResponse contenttype to {}", response.getEncodingStyle().getContentType());
        theResp.setContentType(response.getEncodingStyle().getContentType());
      }

      if (response.getResponseCode() != null) {
        logger.trace("setting the status {}", response.getResponseCode().getCode());
        theResp.setStatus(response.getResponseCode().getCode());
      }
      // n.b. don't ask for the writer until headers are set
      response.writeMessage(theResp.getWriter());
    }

  }

  /**
   * If set, provides a callback which will be used to validate incoming
   * credentials
   */
  private void setAuthorizationCallback(IAuthorizationServerCallback theAuthorizationCallback) {
    myAuthorizationCallback = theAuthorizationCallback;
  }

  /**
   * @param theMessageHandler the messageHandler to set
   */
  private void setMessageHandler(IMessageHandler<String> theMessageHandler) {
    myMessageHandler = theMessageHandler;
  }

  private Hl7CustomizerFactoryIntf getHl7FactoryFromClass(String factoryClassName) {
    Hl7CustomizerFactoryIntf service;
    Class<?> factoryImplClass;

    // Get the class
    try {
      factoryImplClass = Class.forName(factoryClassName.trim());
      Constructor<?> constructor = factoryImplClass.getConstructor();
      service = (Hl7CustomizerFactoryIntf) constructor.newInstance();
      return service;
    } catch (Exception e) {
      logger.error("Cannot create HL7Factory, no class with name: {}", factoryClassName, e);
      return null;
    }
  }

  private static boolean empty(String s) {
    return s == null || "".equals(s.trim()) || s.trim().length() == 0;
  }

  static class AppointmentStatus {
    static final List<String> statusStrings = Arrays.asList("", "Sch", "Comp", "Can", "no-show", "left-not-seen", "Arrived");

    static int getStatusCode(String statusStr) {
      return statusStrings.indexOf(statusStr);
    }
  }
}
