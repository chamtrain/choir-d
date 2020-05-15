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

package edu.stanford.registry.server.security;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.service.Service;
import edu.stanford.registry.shared.Site;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.UserIdp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

public class UserInfo {
  private static final Logger logger = Logger.getLogger(UserInfo.class.getName());

  private volatile Map<String, User> users = new HashMap<>();
  private final Object usersLock = new Object();
  private final boolean loadUsers;
  private final Set<Service> restrictServices;
  private long lastCounterVal = -2L;
  private final SitesInfo sitesInfo;
  private Long defaultIpdId;

  @SuppressWarnings("unused")
  private long lastSunetViewRefreshTime;

  /**
   * Create with no additional restrictions on what services can be accessed.
   */
  public UserInfo(SitesInfo sitesInfo) {
      this(true, sitesInfo, null);
  }

  /**
   * @param restrictServices a list of permitted services, or null to allow any service
   */
  public UserInfo(boolean loadUsers, SitesInfo sitesInfo, Set<Service> restrictServices) {
    this.loadUsers = loadUsers;
    this.restrictServices = restrictServices;
    this.sitesInfo = sitesInfo;
  }

  public void load(Supplier<Database> database) {

    long oldCounterValue = lastCounterVal;
    lastCounterVal = getUserCacheCounter(database);  // -1 if ! loadUsers so will continue at least once
    if (lastCounterVal == oldCounterValue) {
      logger.debug("Not Loading users and their authority, still at revision = "+lastCounterVal);
      return;
    }

    lastSunetViewRefreshTime = System.currentTimeMillis();

    // Record our position in updates first to avoid race conditions if someone
    // updates while
    // we are reading

    logger.debug("Loading users and their authority at revision " + lastCounterVal);
    final Map<String, User> newUsers = new HashMap<>();

    // Loading all users can be slow, so allow skipping it in development

    if (loadUsers) {
      logger.debug("Loading users");
      // now add their services
      database.get().toSelect("select username, display_name, a.user_principal_id, authority, c.idp_id "
          + "from user_authority a, user_principal p, user_credential c "
          + "where a.user_principal_id = p.user_principal_id "
          + "and a.user_principal_id = c.user_principal_id "
          + "and enabled = 'Y'")
          .query(new RowsHandler<Object>() {
        @Override
        public Object process(Rows rs) {
              while (rs.next()) {
                String sunetId = rs.getStringOrNull(1);
                String display = rs.getStringOrNull(2);
                long id = rs.getLongOrNull(3);
                String authority = rs.getStringOrNull(4);
                long idpId = rs.getLongOrNull(5);
                User user = newUsers.get(sunetId);
                if (user == null) {
                  user = new User(idpId, sunetId, display, id, true);
                }

                if (logger.isTraceEnabled()) {
                  logger.trace("adding authority=" + authority + " to user " + sunetId);
                }
                user.addRole(authority);

                newUsers.put(sunetId, user);
              }
              return null;
            }
          });
    } else
     logger.debug("Loading users is disabled.");

    // Built-in user for the remote survey application to use
    UserDao userDao = new UserDao(database.get(), null, null);
    UserIdp idp = userDao.findDefaultIdp();

    if (idp != null) {
      defaultIpdId = idp.getIdpId();
    } else {
      defaultIpdId = 0L;
    }
    User patient = new User(defaultIpdId, "-survey-app", "Survey Application (Internal)", 0, true);
    patient.addRole("PATIENT");
    newUsers.put("-survey-app", patient);

    synchronized (usersLock) {
      users = newUsers;
    }
  }


  /**
   * private ArrayList<String> clinicServiceName() { ArrayList<String> serviceNames = new ArrayList<String>();
   * serviceNames.add(Service.CLINIC_SERVICES.getInterfaceClass().getName());
   * serviceNames.add(Service.CHART_SERVICES.getInterfaceClass().getName()); return serviceNames; }
   * <p/>
   * private ArrayList<String> allServiceNames() { ArrayList<String> serviceNames = new ArrayList<String>(); Iterator<Service> it =
   * EnumSet.allOf(Service.class).iterator();
   * <p/>
   * while (it.hasNext()) { Service svc = it.next(); serviceNames.add(svc.getInterfaceClass().getName()); } return serviceNames; }
   */
  public User forName(final String userName) {
    User user;
    synchronized (usersLock) {
      user = users.get(userName);
      if (user == null) {
        // Return a user with no privileges
        return new User(defaultIpdId, userName, null, 0, true);
      }
      if (userName.equals("-survey-app")) {
        return user; // no authorized sites are needed
      }

      fillUserSites(user);
    }

    return user;
  }


  /**
   * Produces the allowable site array from the roles.
   *
   * This could be moved to the UI.
   */
  void fillUserSites(User user) {
    if (user.areSitesInitted()) {
      return;
    }

    HashMap<String, Site> results = new HashMap<>();

    for (String role: user.getRoles()) {  // role is like "DATA_EXCHANGE[hand]"
      if (role == null || role.isEmpty()) {
        continue;
      }

      String urlParam = UserDao.extractSiteParamFromRole(role);
      if (results.containsKey(urlParam)) {
        continue;
      }

      SiteInfo site = sitesInfo.byUrlParam(urlParam);
      if (site != null) {
        results.put(urlParam, site.copySite());  // extracts foo from any[foo]
      }
    }

    Collection<Site> values = results.values();
    Site siteArray[] = new Site[values.size()];
    siteArray = values.toArray(siteArray);
    user.setSurveySites(siteArray);
  }


  /**
   * @return -1 if loadUsers=false, else the largest revision_number from user_change_history
   */
  private long getUserCacheCounter(Supplier<Database> database) {
    if (!loadUsers) {
      return -1L; // if not loading users, this revision number prevents subsequent queries
    }
    return database.get().toSelect("select max(revision_number) from user_change_history").queryLongOrZero();
  }


  public boolean isAllowedService(Service serv) {
    if (restrictServices == null) {
      return false;
    } else {
      return restrictServices.contains(serv);
    }
  }
}
