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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.UserPreference;
import edu.stanford.registry.shared.api.ClientService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.LogRecord;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

public class ClientServicesImpl implements ClientService {
  private static Logger logger = Logger.getLogger(ClientServicesImpl.class);

  final private Database db;
  final private User user;
  final private SiteInfo siteInfo;

  public ClientServicesImpl(User user, Supplier<Database> database, ServerContext context, SiteInfo siteInfo) {
    this.user = user;
    this.db = database.get();
    this.siteInfo = siteInfo;
  }

  /**
   * Fills the user with his/her site preferences.
   *
   * Note the available sites are filled when the user first is fetched from the cache.
   */
  @Override
  public User getUser() {
    // Fetch the user preferences for the site
    UserDao userDao = new UserDao(db, user, user);
    ArrayList<UserPreference> preferences = userDao.findUserPreferences(user.getUserPrincipalId(), siteInfo.getSiteId());
    if (preferences != null) {
      HashMap<String,String>prefs = new HashMap<String,String>(preferences.size());
      for (UserPreference preference : preferences) {
        prefs.put(preference.getPreferenceKey(), preference.getPreferenceValue());
      }
      user.setUserPreferences(prefs);
    }

    // This user is cached in the UI after this call, so the preferences need only be filled here
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%sgetUser: returns user %s, can access %d sites",
          siteInfo.getIdString(), user.getUsername(), user.getSurveySites().length));
    }
    return user;
  }

  /**
   * Does nothing- the caller de-obfuscates the stack to the log.
   * This exists as a stub because it and the caller share the same interface.
   */
  @Override
  public void clientLog(List<LogRecord> log) {
    // an unused stub. Later, errors and/or warnings could be logged to the database, for the UI
  }

  @Override
  public ClientConfig getClientConfig() {
    return siteInfo.getRegistryCustomizer().getClientConfig();
  }

  @Override
  public HashMap<String, String> getInitParams() {
      return siteInfo.getProperties(); // is this right? Or getClientParams?
  }

  @Override
  public String getSiteConfig(String configName) {
    return siteInfo.getProperty(configName);
  }
}
