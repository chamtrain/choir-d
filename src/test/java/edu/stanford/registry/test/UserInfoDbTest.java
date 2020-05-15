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

package edu.stanford.registry.test;

import edu.stanford.registry.server.security.Role;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.server.security.UserInfo;
import edu.stanford.registry.server.service.Service;
import edu.stanford.registry.shared.User;

import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

/**
 * Test cases to verify UserInfo works properly.
 */
public class UserInfoDbTest extends DatabaseTestCase {
  private Supplier<Database> database;
  private UserDao userDao;
  private static Logger logger = Logger.getLogger(UserInfoDbTest.class);

  public static final Long DEFAULT_SITE_ID = 1l;  // the default value for Stanford Medical
  public static final String DEFAULT_SITE_SHORT_NAME = "1";

  @Override
  protected void postSetUp() throws Exception {
    database = getDatabaseProvider();
    userDao = new UserDao(database.get(), null, null);
  }

  /**
   */
  public void testAdminAccess() throws Exception {
    UserInfo userInfo = new UserInfo(serverContext.getSitesInfo());
    userInfo.load(database);
    User user = userInfo.forName("admin");
    logger.info("testAdmin Access: " + user.getUsername()
        + " can access security, clinic, file, editor and registry services");

    assertTrue(canAccess(user, Service.SECURITY_SERVICES, DEFAULT_SITE_SHORT_NAME));
    assertTrue(canAccess(user, Service.CLINIC_SERVICES, DEFAULT_SITE_SHORT_NAME));
    assertTrue(canAccess(user, Service.FILE_SERVICES, DEFAULT_SITE_SHORT_NAME));
    assertTrue(canAccess(user, Service.EDITOR_SERVICES, DEFAULT_SITE_SHORT_NAME));
    assertTrue(canAccess(user, Service.REGISTER_SERVICES, DEFAULT_SITE_SHORT_NAME));
  }

  public void testAdminHasSites() {
    UserInfo userInfo = new UserInfo(serverContext.getSitesInfo());
    userInfo.load(database);
    User user = userInfo.forName("admin");
    assertNotNull(user.getSurveySites());
    assertTrue(user.getSurveySites().length > 0);
  }

  /**
   * Test the new tables for authorization information (users & users_authority). Use a new user that will not conflict with anything
   * already in the usual tables.
   */
  public void testNewUser() throws Exception {
    insertUser("johndoe");
    insertAuthority("johndoe", Service.CLINIC_SERVICES);

    UserInfo userInfo = new UserInfo(serverContext.getSitesInfo());
    userInfo.load(database);
    User user = userInfo.forName("johndoe");
    logger.info("testNewUser (0): " + user.getRoles() + " roles assigned to new user");
    assertEquals(1, user.getRoles().size());
    assertTrue(canAccess(user, Service.CLINIC_SERVICES, DEFAULT_SITE_SHORT_NAME));
  }

  private void insertUser(String sunetId) {
    userDao.addOrEnableUser(userDao.findDefaultIdp().getIdpId(), sunetId, sunetId, "");
  }

  private void insertAuthority(String sunetId, Service service) {
    String role = Role.getRole(service.getInterfaceClass().getName());
    String authority = Role.getAuthority(role, DEFAULT_SITE_SHORT_NAME);
    userDao.grantAuthority(userDao.findDefaultIdp().getIdpId(), sunetId, authority);
  }

  private boolean canAccess(User user, Service service, String siteUrlParam) {
    String authority = Role.getRole(service.getInterfaceClass().getName());
    if (user.hasRole(String.format("%s[%s]", authority, siteUrlParam))) return true;
    return false;
  }
}
