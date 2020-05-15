/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.database;

import edu.stanford.registry.server.security.Role;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.server.security.UserDao.UserAuthority;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DisplayProvider;
import edu.stanford.registry.shared.Provider;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.UserIdp;
import edu.stanford.registry.shared.UserPrincipal;
import edu.stanford.registry.test.DatabaseTestCase;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDaoTest extends DatabaseTestCase {

  private static final Logger logger = LoggerFactory.getLogger(UserDaoTest.class);
  private static final String[] userNames = { "testUser1", "testUser2", "testUser3"};
  private static final String[] displayNames = {"Test UserOne", "Testuser Two", "TestUs ErThree"};
  private static final String[] emailAddresses = { "testuser@one.com", "testuser@two.com", "testuser@3.com" };
  private static final String[] providerEids = { "E10000", "E10001", "E10002"};
  public static final String NOBODY = "NOBODY";
  private static final long idp = 1L;
  private final User admin = new User(idp,"admin", "Admin", 1L, "", true);
  private UserDao dao;
  private final UserPrincipal[] userPrincipals = new UserPrincipal[3];
  private final Long[] providerIds = new Long[3];
  private UserIdp userIdp;

  @Override
  protected void postSetUp() {
    dao = new UserDao(getDatabase(), admin, admin);
    userIdp = dao.findDefaultIdp();
    createUsers();
    createProviders();

  }
  public void test_addOrEnableUser() {
    UserPrincipal userPrincipal = dao.findUserPrincipal(NOBODY);
    assertNull("Expected null since username NOBODY shouldn't exist", userPrincipal);
    int orig_revnum = getRevisionNumber();
    assertTrue("Expected true, we're changing the displayname", dao.addOrEnableUser(userIdp.getIdpId(), userNames[1], displayNames[2], emailAddresses[1]));
    int new_revnum = getRevisionNumber();
    assertTrue("Expected the revision number to increase for writing the change to displayname ", new_revnum > orig_revnum);

    orig_revnum = new_revnum;
    assertFalse("Expected no change to user[0]", dao.addOrEnableUser(userIdp.getIdpId(), userNames[0], displayNames[0], emailAddresses[0]));
    assertTrue("Expected a change to user[0] email", dao.addOrEnableUser(userIdp.getIdpId(), userNames[0], displayNames[0], null));
    new_revnum = getRevisionNumber();
    assertTrue("Expected the same revision number as no change was made", new_revnum == orig_revnum);
  }

  public void test_addOrEnableUserWithUserPrincipalId() {
    String displayName = "Test UserFour";
    String username1 = "testUser4@idp1";
    String username2 = "testUser4@idp2";

    String emailAddr = "test4@4.org";
    boolean added1 = dao.addOrEnableUser(idp, username1, displayName, null);

    UserIdp idp2 = dao.addOrUpdateIdp("two", "Second idp");
    boolean added2 = dao.addOrEnableUser(idp2.getIdpId(), username2, displayName, null, userPrincipals[2].userPrincipalId);
    assertTrue("Expected the first username to be added", added1);
    assertTrue("Expected the 2nd username to be added", added2);
    UserPrincipal userPrincipal = dao.findUserPrincipal(idp, username1);
    assertNotNull("Expected to find the newly created user ", userPrincipal);
    assertEquals("Expected display name to match", displayName, userPrincipal.displayName);
    boolean added3 = dao.addOrEnableUser(idp, username1, displayName, emailAddr, userPrincipals[2].userPrincipalId);
    assertTrue("Expected the user to be updated", added3);
    userPrincipal = dao.findUserPrincipal(idp, username1);
    assertEquals("Expected email addr to match", emailAddr, userPrincipal.emailAddr);
  }

  public void test_findUserPrincipal() {
    UserPrincipal userPrincipal = dao.findUserPrincipal(userPrincipals[0].username);
    assertEquals("Expected the displayname to be the one it was created with",userPrincipal.displayName, userPrincipals[0].displayName);
    assertEquals("Expected the username to be the one it was created with", userPrincipal.username, userPrincipals[0].username );
    assertEquals("Expected the email address to be the one it was created with", userPrincipal.emailAddr, userPrincipals[0].emailAddr );
    assertEquals("Expected the id to be the one the create returned",  userPrincipal.userPrincipalId, userPrincipal.userPrincipalId);
    userPrincipal = dao.findUserPrincipal(NOBODY);
    assertNull("Expected null because username null should't exist", userPrincipal);

    userPrincipal = dao.findUserPrincipal(userNames[1]);
    assertNotNull("Expected not null, we should find " + userPrincipal + " created in postsetup", userPrincipal);
  }

  public void test_findUserPrincipalForIdp() {
    UserPrincipal userPrincipal = dao.findUserPrincipal(idp, userPrincipals[0].username);
    assertEquals("Expected the displayname to be the one it was created with",userPrincipal.displayName, userPrincipals[0].displayName);
    assertEquals("Expected the username to be the one it was created with", userPrincipal.username, userPrincipals[0].username );
    assertEquals("Expected the email address to be the one it was created with", userPrincipal.emailAddr, userPrincipals[0].emailAddr );
    assertEquals("Expected the id to be the one the create returned",  userPrincipal.userPrincipalId, userPrincipal.userPrincipalId);
    userPrincipal = dao.findUserPrincipal(NOBODY);
    assertNull("Expected null because username null should't exist", userPrincipal);

    userPrincipal = dao.findUserPrincipal(userNames[1]);
    assertNotNull("Expected not null, we should find " + userPrincipal + " created in postsetup", userPrincipal);
  }

  public void test_findUserPrincipalbyId() {
    ArrayList<UserPrincipal> userPrincipalArr = dao.findUserPrincipal(userPrincipals[2].userPrincipalId);
    assertNotNull("Expected an array of UserPrincipal", userPrincipalArr);
    assertEquals("Expected one UserPrincipal to be returned", 1, userPrincipalArr.size());
    UserPrincipal userPrincipal = userPrincipalArr.get(0);
    assertEquals("Expected the displayname to be the one it was created with", userPrincipals[2].displayName, userPrincipal.displayName);
    assertEquals("Expected the username to be the one it was created with", userPrincipals[2].username, userPrincipal.username );
    assertEquals("Expected the email address to be the one it was created with", userPrincipals[2].emailAddr, userPrincipal.emailAddr );
    assertEquals("Expected this providerEid to be the one the create returned", providerEids[2], userPrincipal.providerEid);
    assertEquals("Expected this UserPrincipalId to be the one the create returned", userPrincipals[2].userPrincipalId, userPrincipal.userPrincipalId);
  }

  public void test_findUserWithDisplayName() {
    List<UserPrincipal> users = dao.findUsersWithDisplayName(getSiteInfo().getSiteName(), "Test");
    assertTrue("Expected to find at the least the 3 this creates at setup", users.size() > 0 );
  }

  public void test_findAllUserPrincipal() {

    List<UserPrincipal> users = dao.findAllUserPrincipal(getSiteInfo().getSiteName());
    assertTrue("Expected to find at the least the 3 this creates at setup",users.size() >= 3 );
  }

  public void test_grantAuthority() {

    String authority = grantAuthority(userPrincipals[0].username, Constants.ROLE_EDITOR);

    UserAuthority[] userAuthorities = dao.findAllUserAuthority(userPrincipals[0].username);
    boolean found = false;
    for (UserAuthority userAuthority : userAuthorities) {
      if (authority.equals(userAuthority.authority)) {
        found = true;
        break;
      }
    }
    assertTrue("Expected to find the the newly granted authority", found);
  }

  public void test_findAllUserAuthority() {

    grantAuthority(userPrincipals[0].username, Constants.ROLE_REGISTRATION);
    UserAuthority[] userAuthorities = dao.findAllUserAuthority(userPrincipals[0].username);
    assertEquals("Expected to find 2 authorities, CLINIC_STAFF granted at setup and the one granted here",
        2, userAuthorities.length );
  }

  public void test_findAllAdmins() {

    grantAuthority(userPrincipals[0].username, Constants.ROLE_SECURTY);
    grantAuthority(userPrincipals[1].username, Constants.ROLE_SECURTY);
    UserPrincipal userPrincipal2 = userPrincipals[2];
    List<Long> users = dao.findAllAdmins(getSiteInfo().getSiteName());
    assertTrue("Expected " + userPrincipals[0] + " to be an admin", users.contains(userPrincipals[0].userPrincipalId));
    assertTrue("Expected " + userPrincipals[1] + " to be an admin", users.contains(userPrincipals[1].userPrincipalId));
    assertFalse("Did not expect " + userPrincipals[2] + " to be an admin", users.contains(userPrincipal2.userPrincipalId));
  }

  public void test_disableUser() {
    assertFalse("Expected disable to return false on user NOBODY", dao.disableUser(userIdp.getIdpId(), NOBODY));
    assertTrue("Expected disable to work on existing user " + userPrincipals[1].username, dao.disableUser(userIdp.getIdpId(), userNames[1]));
  }

  public void test_revokeAuthority() {
    grantAuthority(userPrincipals[0].username, Constants.ROLE_REGISTRATION);
    UserAuthority[] userAuthorities = dao.findAllUserAuthority(userPrincipals[0].username);
    assertEquals(2, userAuthorities.length );
    assertTrue(dao.revokeAuthority(userIdp.getIdpId(), userPrincipals[0].username, getAuthority(Constants.ROLE_REGISTRATION)));
    assertFalse( dao.revokeAuthority(userIdp.getIdpId(), userPrincipals[0].username,  getAuthority(Constants.ROLE_SECURTY)));
  }

  public void test_public_findProviders() {
    ArrayList<Provider> providers = dao.findProviders ( false );
    assertEquals(3, providers.size());
    providers = dao.findProviders(true);
    assertEquals(2, providers.size());
  }

  public void test_getProvider() {
    // Find one of the providers we created
    Provider provider = dao.getProvider(providerIds[0]);
    assertNotNull(provider);

    provider = dao.getProvider(999564312);
    assertNull(provider);
  }

  public void test_getProviderByEid() {

    Provider provider = dao.getProviderByEid(NOBODY);
    assertNull(provider);

    provider = dao.getProviderByEid(providerEids[2]);
    assertNotNull(provider);
    assertEquals(provider.getProviderEid(), providerEids[2]);
    assertEquals(provider.getUserPrincipalId(), userPrincipals[2].userPrincipalId );
    //System.out.println("PROVIDER userid for EID " + providerEids[0] + " is " + userPrincipal0 + " we got " + )
  }


  public void test_getUserProvider() {
    Provider provider = dao.getUserProvider(userPrincipals[0].userPrincipalId);
    assertNull(provider);
    provider = dao.getUserProvider(userPrincipals[2].userPrincipalId);
    assertNotNull(provider);
  }

  public void test_updateProvider() {
    Provider provider = new Provider();
    provider.setUserPrincipalId(9999L);
    String exceptionMsg = null;
    try {
      dao.updateProvider(provider);
    } catch (Exception ex) {
      exceptionMsg = ex.getMessage();
    }
    assertNotNull("Expecting an error to be thrown, user does't exist", exceptionMsg);
    provider = dao.getProvider(providerIds[0]);
    provider.setUserPrincipalId(userPrincipals[1].userPrincipalId);
    exceptionMsg = null;
    try {
      dao.updateProvider(provider);
    } catch (Exception ex) {
      exceptionMsg = ex.getMessage();
    }
    assertNull("Expecting no error to be thrown, user does exist", exceptionMsg);
  }

  public void test_writeProvider() {
    Provider provider = new Provider();
    provider.setProviderEid(NOBODY);
    provider = dao.writeProvider(provider);
    assertNotNull("Expected the insert to update provider id ", provider.getProviderId());
  }

  public void testFindDisplayProviders() {
     ArrayList<DisplayProvider>  displayProviders= dao.findDisplayProviders(getSiteInfo());
  }

  public void test_findAllIdp() {
    UserIdp idp2 = dao.addOrUpdateIdp("two", "Second idp");
    assertNotNull(idp2);
    assertNotNull(idp2.getIdpId());
    ArrayList<UserIdp> idps = dao.findAllIdp();
    assertNotNull("Expecting an array of UserIdp", idps);
    assertTrue("Expecting at least the default plus the one just added", idps.size() >= 2);

    UserIdp addedIdp = dao.addOrUpdateIdp("two", "Second Identity Provider");
    ArrayList<UserIdp> idps2 = dao.findAllIdp();
    assertEquals("Expecting the idp to be updated not added, so same number of idps", idps.size(), idps2.size());
    assertEquals("Expecting the display name to be the new one", "Second Identity Provider", addedIdp.getDisplayName());
    String displayName = "";
    for (UserIdp userIdp: idps2) {
      if (idp2.getIdpId().equals(userIdp.getIdpId())) {
        displayName = userIdp.getDisplayName();
      }
    }
    assertEquals("Expecting to find the new display name", addedIdp.getDisplayName(), displayName);
  }

  private void createUsers() {
    userPrincipals[0] = createUser(0);
    userPrincipals[1] = createUser(1);
    userPrincipals[2] = createUser(2);
  }

  private UserPrincipal createUser(String username, String displayName, String emailAddress) {
    assertEquals(true, dao.addOrEnableUser(userIdp.getIdpId(), username, displayName , emailAddress));
    return dao.findUserPrincipal(username);
  }

  private UserPrincipal createUser(int num) {
    if (num < 0 || num > 2) return null ;
    UserPrincipal userPrincipal = createUser(userNames[num], displayNames[num], emailAddresses[num]);
    grantAuthority(userPrincipal.username, Constants.ROLE_CLINIC_STAFF);
    return userPrincipal;
  }

  private String grantAuthority(String username, String role) {
    String authority = getAuthority(role);
    dao.grantAuthority(userIdp.getIdpId(), username, authority );
    return authority;
  }

  private String getAuthority(String role) {
    return Role.getAuthority(role, getSiteInfo().getUrlParam() );
  }

  private void createProviders() {
    providerIds[0] = createProvider(providerEids[0]);
    providerIds[1] = createProvider(providerEids[1]);
    providerIds[2] = createProvider(providerEids[2], userPrincipals[2].userPrincipalId);
  }

  private Long createProvider(String eid) {
    String sql =  "INSERT INTO provider (provider_id, provider_eid, dt_created) "
        + "VALUES (:pk, ?, :now) ";
    return getDatabase().toInsert(sql)
        .argPkSeq(":pk", "user_principal_sequence")
        .argString(eid)
        .argDateNowPerDb(":now")
        .insertReturningPkSeq("provider_id");
  }

  private Long createProvider(String eid, Long userId) {
    String sql =  "INSERT INTO provider (provider_id, provider_eid, user_principal_id, dt_created) "
        + "VALUES (:pk, ?, ?, :now) ";
    return getDatabase().toInsert(sql)
        .argPkSeq(":pk", "user_principal_sequence")
        .argString(eid)
        .argLong(userId)
        .argDateNowPerDb(":now")
        .insertReturningPkSeq("provider_id");
  }

  private int getRevisionNumber() {
    return databaseProvider.get().toSelect("select max(revision_number) from user_change_history").queryIntegerOrZero();
  }
}
