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

package edu.stanford.registry.server.service;

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.security.Role;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.server.security.UserDao.UserAuthority;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Provider;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.UserDetail;
import edu.stanford.registry.shared.UserIdp;
import edu.stanford.registry.shared.UserPrincipal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

public class SecurityServicesImpl implements SecurityServices {

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(SecurityServicesImpl.class);

  private final Supplier<Database> dbp;
  private final ServerContext serverContext;
  private final User user;
  private final SiteInfo siteInfo;

  public SecurityServicesImpl(User user, Supplier<Database> dbp, ServerContext serverContext, SiteInfo siteInfo) {
    this.user = user;
    this.dbp = dbp;
    this.serverContext = serverContext;
    this.siteInfo = siteInfo;
  }

  @Override
  public UserDetail findUserDetail(String username) {
    username = username.toLowerCase();
    UserDao userDao = new UserDao(dbp.get(), user, user);
    UserPrincipal userPrincipal = userDao.findUserPrincipal(username);
    User currentUser = serverContext.userInfo().forName(username);
    if (userPrincipal == null && (currentUser == null || currentUser.getRoles().isEmpty())) {
      return null;
    }

    UserDetail detail;
    if (userPrincipal == null) {
      detail = new UserDetail(currentUser.getIdpId(), currentUser.getUsername(), currentUser.getEmailAddr());
    } else {
      detail = new UserDetail(userPrincipal);
    }
    if (currentUser != null && currentUser.getRoles() != null)  detail.setRoles(currentUser.getRoles());
    // Get the available roles for the site
    Map<String,String> siteRoles = getAvailableRoles();

    // Get the granted roles. For this site, these are the roles granted to the user.
    ArrayList<String> grantedRoles = new ArrayList<>();
    for (UserAuthority userAuthority : userDao.findAllUserAuthority(username)) {
      String authority = userAuthority.authority;
      if (siteRoles.containsKey(authority)) grantedRoles.add(authority);
    }
    detail.setGrantedRoles(grantedRoles);
    return detail;
  }

  @Override
  public void saveUserDetail(ArrayList<UserDetail> newDetailArr) {
    long userPrincipalId = 0;
    UserDao dao = new UserDao(dbp.get(), user, user);
    for (UserDetail newDetail: newDetailArr) {
      String username = newDetail.getUsername();
      UserDetail oldDetail = findUserDetail(username);
      if (oldDetail != null) {
        userPrincipalId = oldDetail.getUserPrincipalId();
      }
      if (newDetail.isEnabled()) {
        if (newDetail.getUserPrincipalId() == 0 && userPrincipalId > 0) {
          newDetail.setUserPrincipalId(userPrincipalId);
        }
        dao.addOrEnableUser(newDetail.getIdpId(), username, newDetail.getDisplayName(), newDetail.getEmailAddr(), newDetail.getUserPrincipalId());
        if (newDetail.getUserPrincipalId() == 0) {
          UserDetail addedUser = findUserDetail(username); // find the one we just added
          if (addedUser != null) {
            userPrincipalId = addedUser.getUserPrincipalId();
          }
        }
        // Revoke granted roles which are not in the new user detail
        if (oldDetail != null && oldDetail.getGrantedRoles() != null) {
          for (String roleName : oldDetail.getGrantedRoles()) {
            //if (!roleName.startsWith(Constants.ROLE_DEVELOPER) && !newDetail.hasGrantedRole(roleName)) {
            if (!newDetail.hasGrantedRole(roleName)) {
              dao.revokeAuthority(newDetail.getIdpId(), username, roleName);
            }
          }
        }

        // Grant roles in the new user detail that isn't in the existing one
        if (newDetail.getGrantedRoles() != null) {
          for (String roleName : newDetail.getGrantedRoles()) {
            if (oldDetail == null || !oldDetail.hasGrantedRole(roleName)) {
              dao.grantAuthority(newDetail.getIdpId(), username, roleName);
            }
          }
        }

        // update the old provider if it had one and its been changed
        if (oldDetail != null && oldDetail.getProviderId() > 0 && (oldDetail.getProviderId()
            != newDetail.getProviderId())) {
          edu.stanford.registry.shared.Provider oldProvider = dao.getUserProvider(newDetail.getUserPrincipalId());
          oldProvider.setUserPrincipalId(null);
          dao.updateProvider(oldProvider);
        }
        // update the new one
        if (newDetail.getProviderId() > 0) {
          edu.stanford.registry.shared.Provider newProvider = dao.getProvider(newDetail.getProviderId());
          if (newProvider != null) {
            if (newDetail.getUserPrincipalId() == 0) {
              newDetail.setUserPrincipalId(dao.findUserPrincipal(newDetail.getUsername()).userPrincipalId);
            }
            newProvider.setUserPrincipalId(newDetail.getUserPrincipalId());
            dao.updateProvider(newProvider);
          }
        }
      } else {
        dao.disableUser(newDetail.getIdpId(), username);
      }
    }
    // Probably not the best idea to do this here, but it keeps things up to date...
    serverContext.userInfo().load(dbp);
  }

  @Override
  public ArrayList<UserDetail> findAllUsers() {
    ArrayList<UserDetail> users = new ArrayList<>();
    UserDao dao = new UserDao(dbp.get(), user, user);
    List<UserPrincipal> userPrincipalList = dao.findAllUserPrincipal(siteInfo.getUrlParam());
    List<Long> adminUserList = dao.findAllAdmins(siteInfo.getUrlParam());
    for (UserPrincipal userPrincipal : userPrincipalList) {
      UserDetail detail = new UserDetail(userPrincipal);
      if (adminUserList.contains(userPrincipal.userPrincipalId)) {
        detail.addRole(Role.getAuthority(Constants.ROLE_SECURTY, siteInfo.getUrlParam()));
      }
      users.add(detail);
    }
    users.sort((o1, o2) -> {
      if (o1 == null || o2 == null || o1.getUsername() == null || o2.getUsername() == null) return 0;
      return o1.getUsername().compareTo(o2.getUsername());
    });
    return users;
  }

  @Override
  public ArrayList<UserDetail> findUsersWithDisplayName(String displayName) {
    ArrayList<UserDetail> users = new ArrayList<>();
    UserDao dao = new UserDao(dbp.get(), user, user);
    List<UserPrincipal> userPrincipalList = dao.findUsersWithDisplayName(siteInfo.getUrlParam(), displayName);
    for (UserPrincipal userPrincipal : userPrincipalList) {
      UserDetail detail = new UserDetail(userPrincipal);
      users.add(detail);
    }
    return users;
  }

  @Override
  public ArrayList<edu.stanford.registry.shared.Provider> findProviders(boolean onlyUnassigned) {
    UserDao dao = new UserDao(dbp.get(), user, user);
    ArrayList<edu.stanford.registry.shared.Provider> providers = dao.findProviders(onlyUnassigned);
    providers.sort(Comparator.comparing(Provider::getProviderEid));
    return providers;
  }

  @Override
  public ArrayList<UserDetail> findAllUsers(String username) {
    ArrayList<UserDetail> userDetailArr = new ArrayList<>();
    username = username.toLowerCase().trim();
    UserDao userDao = new UserDao(dbp.get(), user, user);
    UserPrincipal userPrincipal = userDao.findUserPrincipal(username);
    User currentUser = serverContext.userInfo().forName(username);
    if (userPrincipal == null && (currentUser == null || currentUser.getRoles().isEmpty())) {
      return userDetailArr;
    }

    UserDetail detail;
    if (userPrincipal == null) {
      detail = new UserDetail(currentUser.getIdpId(), currentUser.getUsername(), currentUser.getEmailAddr());
      detail.setProviderEid(currentUser.getProviderEid());
      detail.setProviderId(currentUser.getProviderId());
      detail.setUserPrincipalId(currentUser.getUserPrincipalId());
      detail.setDisplayName(currentUser.getDisplayName());
      if (currentUser.getRoles() != null)  {
        detail.setRoles(currentUser.getRoles());
      }
      userDetailArr.add(detail);
    } else {
      ArrayList<UserPrincipal> userPrincipalArr = userDao.findUserPrincipal(userPrincipal.userPrincipalId);
      for (UserPrincipal up : userPrincipalArr) {
        UserDetail userDetail = new UserDetail(up);
        User cachedUser = serverContext.userInfo().forName(up.username);
        if (cachedUser.getRoles() != null) {
          userDetail.setRoles(cachedUser.getRoles());
        }
        userDetailArr.add(userDetail);
      }
    }

    // Get the available roles for the site
    Map<String,String> siteRoles = getAvailableRoles();

    for (UserDetail userDetail : userDetailArr) {
      // Get the granted roles. For this site, these are the roles granted to the user.
      ArrayList<String> grantedRoles = new ArrayList<>();
      for (UserAuthority userAuthority : userDao.findAllUserAuthority(userDetail.getUsername())) {
        String authority = userAuthority.authority;
        if (siteRoles.containsKey(authority)) {
          grantedRoles.add(authority);
        }
      }
      userDetail.setGrantedRoles(grantedRoles);
    }
    return userDetailArr;
  }

  @Override
  public ArrayList<UserIdp> findIdentityProviders() {
    UserDao dao = new UserDao(dbp.get(), user, user);
    return dao.findAllIdp();
  }

  private Map<String,String> getAvailableRoles() {
    // Get the available roles for the site
    String siteName = siteInfo.getUrlParam();
    Map<String,String> siteRoles = Role.getRoles(siteName);
    Map<String, String> customViews = siteInfo.getRegistryCustomizer().getClientConfig().getCustomViews();
    for (String roleName : customViews.keySet()) {
      siteRoles.put(roleName, customViews.get(roleName));
    }
    return siteRoles;
  }
}

