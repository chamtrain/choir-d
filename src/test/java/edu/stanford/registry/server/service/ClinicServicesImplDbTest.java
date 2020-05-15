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

import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.api.ClientService;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.Utils;

import java.util.Date;
import java.util.HashMap;

import org.junit.Assert;

public class ClinicServicesImplDbTest extends DatabaseTestCase {

  private static final String uniqueRuntimeString = String.valueOf(System.currentTimeMillis());
  private static final String testKey = "theKey";
  private final String patientId = "8888888-8";


  @Override
  protected void postSetUp() {
    Patient testPatient0 = new Patient(patientId, "John", "Doe", new Date(DateUtils.getDaysAgoDate(getSiteInfo(), 35 * 365).getTime()));
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    patientDao.addPatient(testPatient0);
  }

  public void testAfterUpdatePreferencesUserIsUpdated() {
    String preference = "val1-"+uniqueRuntimeString;

    User userFromBeforeUpdate = serverContext().userInfo().forName("admin");
    makeClinicServices().updateUserPreferences(testKey, preference);

    String gotValue = userFromBeforeUpdate.getUserPreferences(testKey);
    Assert.assertEquals("updatePrefs should update the cached user object", preference, gotValue);
  }


  public void testAfterUpdatePreferencesClientSvcsGetUserReturnsNewPrefs() {
    String preference = "val2-"+uniqueRuntimeString;

    makeClinicServices().updateUserPreferences(testKey, preference);

    User user = makeClientSvcs().getUser();  // This is the getUser() call that must get the updated prefs
    String gotValue = user.getUserPreferences(testKey);
    Assert.assertEquals("After updateUserPreferences(..), getUser() should have the new preference", preference, gotValue);
  }


  // tests the values made it to the database and were refetched
  public void testUpdatePrefsAndUserCacheRefreshClientSvcsGetUserReturnsNewPrefs() {
    String preference = "val3-"+uniqueRuntimeString;
    makeClinicServices().updateUserPreferences(testKey, preference);

    updateUserCache(true);

    User gottenUser = makeClientSvcs().getUser();
    String gotValue = gottenUser.getUserPreferences(testKey);
    Assert.assertEquals("After updateUserPrefs() and user cache reload, getUser() should have new preference", preference, gotValue);
  }


  // tests the values made it to the database and are refetched
  //   even if user cache is not updated
  public void testUpdatePrefsAndUserCacheNotRefreshClientSvcsGetUserReturnsNewPrefs() {
    String preference = "val3-"+uniqueRuntimeString;
    makeClinicServices().updateUserPreferences(testKey, preference);

    // Clear the cached user object's preferences
    User gottenUser = makeClientSvcs().getUser();
    gottenUser.setUserPreferences(new HashMap<>());

    updateUserCache(false);  // tests that refreshing the cache w/o changing anything does nothing.

    gottenUser = makeClientSvcs().getUser();
    String gotValue = gottenUser.getUserPreferences(testKey);
    Assert.assertEquals("After updateUserPrefs() and user cache reload, getUser() should have new preference", preference, gotValue);
  }

  public void testGetUserNoRoles() {
    User user = new User(1L, "rascal", "GuyWiNoRoles", 23, "ras@cal.com", true);
    ClientService service = new ClientServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
    // Once, getUser() wi no roles was causing an NPE cuz user.availableSites was null and its count was printed
    user = service.getUser();
    user.hasRole("EnsureThisThrowsNoNPE");
    Assert.assertNotNull(user.getSurveySites());
  }

  public void testDeleteRegistration() {
    Utils utils = new Utils(databaseProvider.get(), getSiteInfo());
    User user = utils.getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
    Date today = DateUtils.getDaysOutDate(getSiteInfo(),0);
    ApptRegistration apptRegistration  = utils.addInitialRegistration(databaseProvider.get(), patientId, today, "testing@test.stanford.edu", "NPV");
    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(getSiteInfo());
    surveyRegUtils.registerAssessments(databaseProvider.get(), apptRegistration.getAssessment(),user);
    makeClinicServices().deletePatientRegistration(apptRegistration);
  }

  // ======== non-tests

  // This loses any values cached in the user object
  private void updateUserCache(boolean changeCache) {
    User oldUser = serverContext().userInfo().forName("admin");
    if (changeCache) {
      UserDao dao = new UserDao(databaseProvider.get(), oldUser, oldUser);
      dao.addOrEnableUser(oldUser.getIdpId(), "bosco", "Bosco Bear", "basco@bear.com");
    }

    // The cache only updates if user_change_history is updated
    serverContext().userInfo().load(databaseProvider);

    User newUser = serverContext().userInfo().forName("admin");
    if (changeCache)
      Assert.assertNotSame("After refresh cache, user is a new object", newUser, oldUser);
    else
      Assert.assertSame("After refresh cache, user is a new object", newUser, oldUser);
  }


  // Creates the service using the user from the cache
  private ClinicServices makeClinicServices() {
    User user = serverContext().userInfo().forName("admin");
    return new ClinicServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }


  // Creates the service using the user from the cache
  private ClientService makeClientSvcs() {
    User user = serverContext().userInfo().forName("admin");
    return new ClientServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }
}