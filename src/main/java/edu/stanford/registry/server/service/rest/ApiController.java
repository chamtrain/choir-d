/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
 * All Rights Reserved.
 *
 * See the NOTICE and LICENSE files distributed with this work for information
 * regarding copyright ownership and licensing. You may not use this file except
 * in compliance with a written license agreement with Stanford University.
 *
 * Unless required by applicab        le law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTES OR CONDITIONS OF ANY KIND, either express or implied. See your
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.stanford.registry.server.service.rest;

import edu.stanford.registry.server.AuthenticatedUser;
import edu.stanford.registry.server.RegistryServletRequest;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.ServerInit;
import edu.stanford.registry.server.service.ApiExtractServices;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.RegisterServices;
import edu.stanford.registry.server.service.SecurityServices;
import edu.stanford.registry.server.service.Service;
import edu.stanford.registry.server.service.ServiceProxyFactory;
import edu.stanford.registry.server.service.rest.api.AssessmentRequestHandler;
import edu.stanford.registry.server.service.rest.api.ExtractRequestHandler;
import edu.stanford.registry.server.service.rest.api.PatientAttributeRequestHandler;
import edu.stanford.registry.server.service.rest.api.PatientReportRequestHandler;
import edu.stanford.registry.server.service.rest.api.PatientRequestHandler;
import edu.stanford.registry.server.service.rest.api.PluginRequestHandler;
import edu.stanford.registry.server.service.rest.api.RegisterRequestHandler;
import edu.stanford.registry.server.service.rest.api.ReportRequestHandler;
import edu.stanford.registry.server.service.rest.api.SiteRequestHandler;
import edu.stanford.registry.server.service.rest.api.SurveyAttributeRequestHandler;
import edu.stanford.registry.server.service.rest.api.SurveyRequestHandler;
import edu.stanford.registry.server.service.rest.api.UserRequestHandler;
import edu.stanford.registry.shared.Constants;
import edu.stanford.survey.server.ClientIdentifiers;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiController extends ServerResource {

  private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

  /**
   * Handles the application API calls passed in from the ApiServiceManager
   *
   * @author tpacht
   */
  public ApiController() {  // A public default construct is required- TomCat creates this as a restlet
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    getVariants().add(new Variant(MediaType.APPLICATION_JSON));
  }

  @Override
  public Representation doHandle(Variant variant) {
    try {

      RegistryServletRequest regRequest = (RegistryServletRequest) ServletUtils.getRequest(getRequest());
      if (regRequest.getHeader(Constants.SITE_ID_HEADER) == null && regRequest.getParameter(Constants.SITE_ID) == null) {
        logger.warn("API call {} failed. No Site was not found in the request headers or in the request parameters", regRequest.getRequestURI());
        return jsonFromString( "Failed", regRequest.getPathInfo()+ " call failed on server ");
      }

      logger.trace("User principal {}",regRequest.getUserPrincipal().getName());
      Enumeration<String> attributeNames = regRequest.getAttributeNames();

      while (attributeNames.hasMoreElements()) {
        String  attributeName = attributeNames.nextElement();
        logger.trace("RequestAttribute {} = {}", attributeName, regRequest.getAttribute(attributeName));
      }

      HttpSession session = regRequest.getSession(false);
      if (session != null) {
        logger.trace("HttpSession " + session.getId());
        Enumeration<String> sessionNames = session.getAttributeNames();
        while (sessionNames.hasMoreElements()) {
          String attName = sessionNames.nextElement();
          logger.trace("HttpSessionAttribute {} = ",session.getAttribute(attName).toString());
        }
      }
      logger.debug("Handling request uri='" + regRequest.getRequestURI() + "' pathinfo='" + regRequest.getPathInfo()
          + "' context path='" + regRequest.getContextPath() + "'" + "?siteId=" + regRequest.getParameter(Constants.SITE_ID));

      SiteInfo siteInfo = regRequest.getSiteInfo();
      if (siteInfo == null) {
        logger.debug("NO SITE INFO!");
      }


      try {
        ServerInit.getInstance(regRequest.getServletContext());
      } catch (ServletException e) {
        logger.error("Could not initialize serverInit",e);
        ServletUtils.getResponse(getResponse()).setStatus(500);
        return jsonFromString( "Failed", regRequest.getPathInfo()+ " call failed on server");
      }
      String callString = regRequest.getPathInfo();
      if (callString == null) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return jsonFromString("response", "failed. No action identified. ");
      }
      if (variant != null) {
        if (callString.startsWith("/") && callString.length() > 1) {
          callString = callString.substring(1);
        }
        logger.debug("in handle for path:" + callString);
        if (callString.startsWith("json/") && callString.length() > 5) {
          callString = callString.substring(5);
        }
        try {
          JsonRepresentation jsonRepresentation = getJson(variant);
          String []requestElements = callString.split("/");
          if (requestElements.length < 2) {
            throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString);
          }
          logger.debug( "requestElements[0] = " + requestElements[0]);
          switch (requestElements[0]) {
          case "assessment":
            logger.debug("calling handleAssessmentRequests");
            AssessmentRequestHandler assessmentRequestHandler = new AssessmentRequestHandler(siteInfo, getClinicService());
            return new JsonRepresentation(assessmentRequestHandler.handle(callString, jsonRepresentation));
          case "patient":
            PatientRequestHandler patientRequestHandler = new PatientRequestHandler(siteInfo, getClinicService());
            return new JsonRepresentation(patientRequestHandler.handle(callString, jsonRepresentation));
          case "patattribute":
            PatientAttributeRequestHandler patAttributeRequestHandler = new PatientAttributeRequestHandler(siteInfo, getClinicService());
            return new JsonRepresentation(patAttributeRequestHandler.handle(callString, jsonRepresentation));
          case "patreport":
            PatientReportRequestHandler patientReportRequestHandler = new PatientReportRequestHandler(siteInfo, getClinicService());
            return new JsonRepresentation(patientReportRequestHandler.handle(callString, jsonRepresentation));
          case "survey":
            SurveyRequestHandler surveyRequestHandler = new SurveyRequestHandler(siteInfo, getClinicService());
            return new JsonRepresentation(surveyRequestHandler.handle(callString, jsonRepresentation));
          case "surveyattribute":
            SurveyAttributeRequestHandler surveyAttributeRequestHandler = new SurveyAttributeRequestHandler(siteInfo, getClinicService());
            return new JsonRepresentation(surveyAttributeRequestHandler.handle(callString, jsonRepresentation));
          case "user":
            UserRequestHandler userRequestHandler = new UserRequestHandler(siteInfo, getSecurityService());
            return new JsonRepresentation(userRequestHandler.handle(callString, jsonRepresentation));
          case "extract" :
            ExtractRequestHandler extractRequestHandler = new ExtractRequestHandler(siteInfo, getExtractService());
            return new JsonRepresentation(extractRequestHandler.handle(callString, jsonRepresentation));
          case "report" :
            ReportRequestHandler reportRequestHandler = new ReportRequestHandler(siteInfo, getClinicService());
            return new JsonRepresentation(reportRequestHandler.handle(callString, jsonRepresentation));
          case "site":
            SiteRequestHandler siteRequestHandler = new SiteRequestHandler(siteInfo, getClinicService());
            return new JsonRepresentation(siteRequestHandler.handle(callString, jsonRepresentation));
          case "pluginData":
            PluginRequestHandler pluginRequestHandler = new PluginRequestHandler(siteInfo, getClinicService());
            return new JsonRepresentation(pluginRequestHandler.handle(callString, jsonRepresentation));
          case "patreg":
            RegisterRequestHandler registerRequestHandler = new RegisterRequestHandler(siteInfo, getRegisterService());
            return new JsonRepresentation(registerRequestHandler.handle(callString, jsonRepresentation));
          default:
            ClinicServices clinicServices = getClinicService();
            Map<String, String[]> params = regRequest.getParameterMap();
            return new JsonRepresentation(clinicServices.handleCustomApi(callString, params, jsonRepresentation));
          }
        } catch (IOException ioe) {
          logger.error("Error processing json ", ioe);
          setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, "IOException");
          return jsonFromString("Failed", "Error processing json");
        } catch (ServletException se) {
          throw new ApiStatusException(Status.SERVER_ERROR_INTERNAL, callString);
        }
      } else {
        // POST request with no entity.
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      }
      return notImplemented(callString);
    }  catch (ApiStatusException statusEx) {
      logger.error("StatusException: {} {} ", statusEx.getStatus(), statusEx.getMessage(), statusEx);
      ServletUtils.getResponse(getResponse()).setStatus(statusEx.getStatus().getCode());
      getResponse().setStatus(new Status(statusEx.getStatus(), statusEx.getMessage()));
      return jsonErrorResponse(statusEx);
    }
  }

  private JsonRepresentation notImplemented(String service) {
    return jsonFromString("Response:", "The API call " + service + " is not implemented yet.");
  }

  private JsonRepresentation jsonFromString(String str, String str2) {
    logger.debug("returning {} {} ", str, str2);
    JSONObject jsonObj = null;
    try {
      jsonObj = new JSONObject().put(str, str2);
    } catch (JSONException e) {
      logger.error("JSON error creating response", e);
    }

    return new JsonRepresentation(jsonObj);
  }

  private JsonRepresentation jsonErrorResponse(ApiStatusException ex) {
    StringBuilder message = new StringBuilder();
    message.append("API call ");
    if (ex.getRequestPath() != null) {
      message.append(ex.getRequestPath());
    }

    StringBuilder status = new StringBuilder();
    status.append(String.valueOf(ex.getStatus().getCode()));
    status.append(" ");
    status.append(ex.getMessage());
    status.append(" ");
    status.append(ex.getStatus().getReasonPhrase());

    JSONObject jsonObj = new JSONObject();
    try {
      jsonObj.put("Failed", message);
      jsonObj.put("Status", status);
    } catch (JSONException e) {
      logger.error("JSON error creating response", e);
    }

    return new JsonRepresentation(jsonObj);
  }

  private JsonRepresentation getJson(Variant variant) throws IOException {
    // Read in the data sent with the post
    if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
      org.restlet.Request req = getRequest();
      Representation repIn = req.getEntity();
      if (repIn != null) {
        JsonRepresentation jsonIn = new JsonRepresentation(repIn.getText());
        logger.debug("API request JSON is {}", jsonIn.toString());
        return jsonIn;
      }
    }
    return null;
  }

  private ClientIdentifiers getClientIds() {
    return new ClientIdentifiers() {

      @Override
      public Long userAgentId(final Long siteId) {
        return null;
      }

      @Override
      public String getClientIpAddress() {
        return null;
      }

      @Override
      public String getDeviceToken() {
        return null;
      }
    };
  }

  private ClinicServices getClinicService() throws ServletException, ApiStatusException {

    return (ClinicServices) getService(Service.CLINIC_SERVICES.getInterfaceClass().getName(), Service.CLINIC_SERVICES.getUrlPath());
  }

  private ApiExtractServices getExtractService() throws ServletException, ApiStatusException {
    return (ApiExtractServices) getService(Service.API_EXTRACT_SERVICES.getInterfaceClass().getName(), Service.API_EXTRACT_SERVICES.getUrlPath());
  }

  private RegisterServices getRegisterService() throws ServletException, ApiStatusException {
    return (RegisterServices) getService(Service.REGISTER_SERVICES.getInterfaceClass().getName(), Service.REGISTER_SERVICES.getUrlPath());
  }

  private SecurityServices getSecurityService() throws ServletException, ApiStatusException {
    return (SecurityServices) getService(Service.SECURITY_SERVICES.getInterfaceClass().getName(), Service.SECURITY_SERVICES.getUrlPath());
  }

  private Object getService(String serviceName, String servicePath) throws ServletException, ApiStatusException {

    RegistryServletRequest regRequest = (RegistryServletRequest) ServletUtils.getRequest(getRequest());
    logger.debug("req uri='" + regRequest.getRequestURI() + "' pathinfo='" + regRequest.getPathInfo()
        + "' context path='" + regRequest.getContextPath() + "' siteId=" + regRequest.getParameter(Constants.SITE_ID));

    final SiteInfo siteInfo = regRequest.getSiteInfo();
    if (siteInfo == null) {
      logger.debug("NO SITE INFO!");
    }
    final HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(regRequest) {
      @Override
      public String getHeader(String name) {
        if (Constants.SITE_ID_HEADER.equals(name)) {
          return siteInfo != null ? siteInfo.getUrlParam() : null;
        }
        return super.getHeader(name);
      }
    };
    ServerInit serverInit = ServerInit.getInstance(regRequest.getServletContext());
    AuthenticatedUser authenticatedUser = new AuthenticatedUser(logger, serverInit.getServerContext(),
                                                                requestWrapper, serviceName);
    if (!authenticatedUser.isAuthenticated()) {
      throw new ApiStatusException(Status.CLIENT_ERROR_UNAUTHORIZED, "/apiV");
    }
    if (authenticatedUser.getUser() == null) {
      logger.debug("No authenticated user");
    } else {
      logger.debug("Authenticated user is " + authenticatedUser.getUser().getUsername());
    }

    ClientIdentifiers clientIds = getClientIds();
    return new ServiceProxyFactory(serverInit.getDatabaseBuilder()).createService(clientIds, authenticatedUser.getUser(),
        serverInit.getServerContext(), Service.byUrlPath(servicePath), siteInfo);

  }
}
