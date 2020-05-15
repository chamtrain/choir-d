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
package edu.stanford.registry.server.service.rest.api;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.security.Role;
import edu.stanford.registry.server.service.SecurityServices;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.shared.UserDetail;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

public class UserRequestHandler extends ApiPatientCommon {
  //private final SiteInfo siteInfo;
  private final SecurityServices services;
  private final Map<String,String> siteRoles ;

  public UserRequestHandler(SiteInfo siteInfo, SecurityServices services) {
    //this.siteInfo = siteInfo;
    this.services = services;
    siteRoles = Role.getRoles(siteInfo.getSiteName());
  }

  public JSONObject handle(String callString, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    String[] requestElements = callString.split("/");
    if (requestElements.length < 2) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, requestElements[0]);
    }
    switch (requestElements[1]) {
    case "all":
      return makeUserList(services);
    case "uname":
      if (requestElements.length > 2) {
        return getUsersByUserName(services, requestElements[2]);
      }
      break;
    case "dname":
      if (requestElements.length > 2) {
        return getUsersByDisplayName(services, requestElements[2]);
      }
      break;
    }
    throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "invalid request");
  }

  private JSONObject makeUserList(SecurityServices securityService) {
    JSONObject userList = new JSONObject();
    for (UserDetail user : securityService.findAllUsers()) {
      JSONObject userObj = new JSONObject();
      userObj.put("username", user.getUsername());
      userObj.put("displayname", user.getDisplayName());
      userObj.put("id", user.getUserPrincipalId());
      userList.accumulate("user", userObj);
    }
    return userList;
  }

  private JSONObject getUsersByUserName(SecurityServices securityService, String userName) throws ApiStatusException {
    UserDetail user = securityService.findUserDetail(userName);
    if (user == null || !hasRoleInSite(user)) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, "/user/" + userName);
    }
    JSONObject userObj = new JSONObject();
    userObj.put("username", user.getUsername());
    userObj.put("displayname", user.getDisplayName());
    userObj.put("id", user.getUserPrincipalId());
    return userObj;
  }

  private JSONObject getUsersByDisplayName(SecurityServices securityServices, String displayName) throws ApiStatusException {
    JSONObject userList = new JSONObject();
    ArrayList<UserDetail> users = securityServices.findUsersWithDisplayName(displayName);
    if (users.size() < 1) {
      throw new ApiStatusException(Status.CLIENT_ERROR_NOT_FOUND, "/user/dname/" + displayName);
    }
    for (UserDetail user: users) {
      JSONObject userObj = new JSONObject();
      userObj.put("username", user.getUsername());
      userObj.put("displayname", user.getDisplayName());
      userObj.put("id", user.getUserPrincipalId());
      userList.accumulate("user", userObj);
    }
    return userList;
  }

  private boolean hasRoleInSite(UserDetail user) {
    for (String role : user.getGrantedRoles()) {
      if (siteRoles.containsKey(role)) {
        return true;
      }
    }
    return false;
  }
}
