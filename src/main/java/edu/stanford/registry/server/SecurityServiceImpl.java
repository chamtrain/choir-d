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

import edu.stanford.registry.client.service.SecurityService;
import edu.stanford.registry.server.security.Role;
import edu.stanford.registry.server.service.SecurityServices;
import edu.stanford.registry.shared.Provider;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.UserDetail;
import edu.stanford.registry.shared.UserIdp;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * From tris.server.service.UserAdministrationImpl
 */

public class SecurityServiceImpl extends ClinicServiceImpl implements SecurityService {

  // private static Logger logger = Logger.getLogger(SecurityServiceImpl.class);

  private static final long serialVersionUID = -6538194556109741417L;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  private SecurityServices getService() {
    RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
    SecurityServices securityService = (SecurityServices) regRequest.getService();
    return securityService;
  }

  @Override
  public Map<String, String> getRoles(String siteName) throws ServiceUnavailableException {
    return Role.getRoles(siteName);
  }

  @Override
  public UserDetail findUserDetail(String userName) {
    return getService().findUserDetail(userName);
  }

  @Override
  public void saveUserDetail(ArrayList<UserDetail> user) {
    getService().saveUserDetail(user);
  }

  @Override
  public ArrayList<UserDetail> findAllUsers() {
    return getService().findAllUsers();
  }

  @Override
  public ArrayList<UserDetail> findUsersWithDisplayName(String displayName) {
    return getService().findUsersWithDisplayName(displayName);
  }

  @Override
  public ArrayList<Provider> findProviders(boolean onlyUnassigned) {
    return getService().findProviders(onlyUnassigned);
  }

  @Override
  public ArrayList<UserDetail> findAllUsers(String text) {
    return getService().findAllUsers(text);
  }

  @Override
  public ArrayList<UserIdp> findIdentityProviders() {
    return getService().findIdentityProviders();
  }
}
