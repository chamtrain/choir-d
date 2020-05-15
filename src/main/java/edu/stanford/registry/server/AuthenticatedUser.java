/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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

import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.security.Role;
import edu.stanford.registry.server.security.UserInfo;
import edu.stanford.registry.server.service.Service;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.User;

import org.slf4j.Logger;

/**
 * A user must be authenticated for a service on a site.
 *
 * This contains the logic to get user and site from the request.
 * It's used by ServiceFilter, which is called before clinic or restlet access
 * and also after the survey proxy forwards a patient.
 *
 * On the Survey side, similar work is done by SurveyServiceImpl.
 */
public class AuthenticatedUser {

  final private Logger logger;
  final private ServerContext serverContext;
  final private HttpServletRequest request;
  final private String serviceName;

  // These are deduced from the request and the serviceName (which the caller extracted)
  private User user;
  private SiteInfo siteInfo;

  /**
   * Call this object's init method to determine the user and the site and throw an
   * exception if the user isn't authorized for the site and the service.
   */
  public AuthenticatedUser(Logger logr, ServerContext context, HttpServletRequest req, String serviceName) {
    logger = logr;
    serverContext = context;
    this.request = req;
    this.serviceName = serviceName;
  }

  /**
   * Determines the user and site and returns false if either is bad
   * or the user needs but lacks permission to use the request's service on the site.
   */
  public boolean isAuthenticated() {
    boolean isSurveySvc = serviceName.equals(Service.SURVEY2_SERVICE.getInterfaceClass().getName());
    // ClientService lets anyone get site config and user info (later, tabs won't be loaded)
    boolean isClientSvc = serviceName.equals(Service.CLIENT_SERVICES.getInterfaceClass().getName());
    boolean isChart = serviceName.equals(Service.CHART_SERVICES.getInterfaceClass().getName());
    boolean isApi = serviceName.equals(Service.REST_SERVICES.getInterfaceClass().getName()) || serviceName.equals(Service.APPLICATION_API_SERVICES.getInterfaceClass().getName());
    boolean isStatus = isStatus(isApi); // status needs no permission

    user = determineValidUser();

    // SurveyServiceProxy authenticates the site before ServiceFilter is called, so we don't look for
    //   for the site in its (X_SURVEY_SYSTEM) header.
    // For both surveys and status, if the user is valid, we're done
    if (isSurveySvc || isStatus) {
      return user != null;
    }

    // Everything else must have a site
    String urlParam = findSite(isChart, isApi, isClientSvc);
    if (isEmpty(urlParam)) {
      logger.warn("Could not find site for userservice call '"+serviceName+"' url: "+request.getRequestURL().toString());
      return false; // violation
    }

    siteInfo = getSiteInfo(urlParam);
    if (user == null) {
      return false; // violation
    }
    if (siteInfo == null) {
      if (serviceName.equals(Service.SURVEY2_SERVICE.getInterfaceClass().getName())) {
        return true;
      }
      return false;
    }
    if (isClientSvc) {
      return true; // no permission is needed for getting the UI look-n-feel and the user info
    }

    return userHasPermissionForService();
  }


  /**
   * Returns the authorized user, after hasViolation returns false
   */
  public User getUser() {
    return user;
  }

  /**
   * Returns the siteInfo for the site, , after hasViolation returns false
   */
  public SiteInfo getSiteInfo() {
    return siteInfo;
  }


  /**
   * Logic to determine if a service needs permission to be used.
   * Just status, surveys and the ClientService don't need permission.
   */
  private boolean isStatus(boolean isApi) {
    if (isApi) {
      String path = request.getPathInfo();
      if ("/status".equals(path) || "/status/text".equals(path)) {
        return true;
      }
    }
    return false;
  }


  /**
   * Ensures the user has a role for the service, if the service needs roles needed.
   */
  private boolean userHasPermissionForService() {
    // Output all the roles, if trace is enabled
    if (logger.isTraceEnabled()) {
      StringBuilder debugText = new StringBuilder("User ").append(user.getUsername()).append(" has roles: ");
      if (user.getRoles() != null) {
        for (int i = 0; i < user.getRoles().size(); i++) {
          debugText.append(i == 0 ? "" : ", ").append(user.getRoles().get(i));
        }
      }
      logger.trace(debugText.toString());
    }

    if (user.hasRole(Role.getRole(serviceName), siteInfo.getUrlParam())) {
      return true; // all is well
    }

    // Violation
    String role = Role.getRole(serviceName);
    if (role == null || role.isEmpty()) {
      role = "(no role for: "+serviceName+")";
    }
    logger.error("Security Error - User " + user.getUsername() + " not authorized to access service: " + role);
    return false;
  }


  /**
   * Returns a valid user from the cache.
   * Patients taking surveys should come in as user="-survey-app"
   * @throws ServletException - if the user isn't valid (it's a RuntimeException)
   */
  private User determineValidUser() throws SecurityException {
    Principal userPrincipal = request.getUserPrincipal();
    if (userPrincipal == null) {
      logger.error("User has not authenticated (principal is null)");
      return null;
    }

    String username = userPrincipal.getName();
    if (username == null) {
      logger.error("User principal name is not valid (null)");
      return null;
    }

    if (username.length() == 0) {
      logger.error("User principal name is not valid (zero-length)");
      return null;
    }

    UserInfo userInfo = serverContext.userInfo();
    User user = userInfo.forName(username);
    if (user == null) {
      logger.error("User [" + username + "] is not a registered user");
      return null;
    }

    return user;
  }


  /**
   * Return site name from the header or if none was set, the default site if there
   * is one and the user has permission for it, else one they have permission for.
   * @param isChart Charts open in a new window by specifying a URL, so add ?siteId=ped
   * @param isApi API calls either don't need a siteId, or specify it as ?siteId=ped
   * @param isClientSvc The first ClientSvc call might have no site, so we pick first allowable.
   * Note if the first one has a very narrow permission, too bad for now?
   */
  private String findSite(boolean isChart, boolean isApi, boolean isClientSvc) {
    SitesInfo sitesInfo = serverContext.getSitesInfo();

    // Tomcat will give the GWT UI to anyone - it's a static resource
    // Everything else comes to the server through GWT server entry points,
    //   which we've configured to put the site into a header
    // A user might not specify a site, though, so we'll send her to the default site,
    //   if she has permission for it, otherwise a random one she has permission for,
    String siteName = getHeaderParam(request, Constants.SITE_ID_HEADER); // Site-Id
    if (isEmpty(siteName) && (isChart || isApi)) {
      siteName = getUrlParam(request); // ?siteId=
    }

    if (!isEmpty(siteName)) { // if a site was specified, we're done
      return siteName;
    }

    if (!isClientSvc) { // Only first contact (ClientSvc) should guess a site
      return siteName;  // For others, it's a problem
    }

    String defaultSite = sitesInfo.getGlobalProperty("default.site");
    if (!isEmpty(defaultSite) && userHasPermissionForSite(defaultSite, user)) {
      return defaultSite;
    }

    return getNameOfFirstAllowedSite(user);
  }


  String getNameOfFirstAllowedSite(User user) {
    for (String role: user.getRoles()) {
      int ix = role.indexOf('[');
      if (ix < 0) {
        continue;  // no '[", should not happen
      }
      int jx = role.indexOf(']', ix);
      if (jx < 0) {
        jx = role.length(); // missing ] should not happen...
      }
      return (role.substring(ix+1, jx));
    }
    return null;
  }


  /**
   * Returns the site from the header, or "" if it's not there or null
   */
  String getHeaderParam(HttpServletRequest req, String param) {
    String site = req.getHeader(param);
    if (site == null || site.isEmpty()) {
      return "";
    }

    if (site.endsWith("/")) {  // a case we saw
      site = site.substring(0, site.length()-2);
    }
    logger.debug("The SITENAME was in header "+param+"="+site+", "+req.getPathInfo());
    return site;
  }


  /**
   * Returns the site from the ?siteId=ped URL parameter, or "" if it's not there or null
   */
  String getUrlParam(HttpServletRequest req) {
    String site = req.getParameter(Constants.SITE_ID);  // siteId
    if (site == null || site.isEmpty()) {
      return "";
    }
    if (site.endsWith("/")) {  // a case we saw with getHeaderParam.
      site = site.substring(0, site.length()-2);
    }

    logger.debug("The SITENAME was in urlParam "+Constants.SITE_ID+"="+site+", "+req.getPathInfo());
    return site;
  }


  boolean userHasPermissionForSite(String url, User user) {
    String toMatch = '[' + url + ']';
    for (String auth: user.getRoles()) {
      if (auth.contains(toMatch)) {
        return true;
      }
    }

    return false;
  }


  private boolean isEmpty(String word) {
    return word == null || word.isEmpty();
  }


  /**
   * Returns the SiteInfo for the parameter or throws an exception if it's not known
   */
  private SiteInfo getSiteInfo(String urlParam) {
    SitesInfo sitesInfo = serverContext.getSitesInfo();
    SiteInfo siteInfo = sitesInfo.byUrlParam(urlParam);
    if (siteInfo != null) {
      return siteInfo;
    }
    logger.error("("+request.getRequestURL()+") The specified site is not known: "+urlParam);
    return null;
  }

}
