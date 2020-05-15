/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.tool;

import com.github.susom.database.Database;

import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.randomset.RandomSetter;
import edu.stanford.registry.server.security.Role;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.UserIdp;

import java.util.ArrayList;

/**
 * This is a template for adding data.  Probably you want to subclass
 * CreateRegistryDataDefault and override its methods.  Or just subclass this.
 *
 * CreateRegistrySchema loads in a subclass of this (using CreateRegistryDataDefault
 * as a default) specified in the system property "data.creation.class" and then calls its methods:
 * <br>creator.createUsers();
 * <br>creator.createSites();
 * <br>creator.configSiteParameters();
 * <br>creator.configEmails();
 * <br>creator.createSurveySystemsAndStudies();
 * <br>creator.addSurveyCompletions();
 * <br>creator.addPatientResultTypes();
 * <br>creator.addOther(); // optional.
 *
 * @author rstr
 */
public abstract class CreateRegData {
  protected final Database db;
  protected final String devAdminSites; // admin gets all authority to these, except BUILDER just for the first

  CreateRegData(Database database, String adminSites) {
    db = database;
    devAdminSites = adminSites;
  }

  /**
   * This populates the survey_site table with its 3-value rows, survey_site_id, url_param, display_name
   *
   * This will be called second- some of the other calls populate tables that have a foreign-key to this one.
   * Many don't need it, but it helps ensure a query for one site won't return a value belonging to
   * another site, an added privacy protection.
   */
  abstract public void createSites();

  /**
   * Creates the admin user - you can override it to also create others.
   * The admin user may be needed for some other insertions to work, so this is called first.
   */
  public void createUsers() {
    // This must happen before AppConfig initialization
    UserDao userDao = new UserDao(db, null, null);
    userDao.addOrUpdateIdp("dflt", "Default Identity Provider");
    userDao.addOrEnableUser(userDao.findDefaultIdp().getIdpId(), "admin", "Admin Test-User", null);
    grantAdminAuthorities("admin", userDao);
  }

  /**
   * Utility: gives the user all permissions on all clinic sites, except for the
   * BUILDER permission, given on just the first site, to ensure a developer
   * doesn't get confused which site s/he's using.
   *
   * @param userName - typically "admin"
   * @param userDao - since the caller must have one.
   */
  @SuppressWarnings("SameParameterValue")
  protected void grantAdminAuthorities(String userName, UserDao userDao) {
    boolean hasBuilder = false; // only need this for first site
    Role roles = new Role();
    ArrayList<String> clinicSites = new ArrayList<>();
    for (String siteName: devAdminSites.split(" ")) {
      if (!"test".equals(siteName) && !"stub".equals(siteName) && !"sat".equals(siteName) && !"cat".equals(siteName)) {
        clinicSites.add(siteName);
      }
    }
    for (String siteName: clinicSites) {
      for (String role: roles.getRoleSet()) {
        if ("BUILDER".equals(role)) {
          if (hasBuilder)
            continue;  // only needed for one site, so put just on the first, so it's not confusing in the UI
          else
            hasBuilder = true;
        }
        String authority = role + '[' + siteName + ']';
        userDao.grantAuthority(userDao.findDefaultIdp().getIdpId(), userName, authority);
      }
    }
  }

  // ======= markup, constants and a class for site parameter initialization
  // These violate the style guide -- the all-caps makes the semantics more obvious in examples

  protected String ONLY_GLOBAL(String s) {
    return s;
  }
  protected String SET_IN_PRODUCTION(String s) {
    return s;
  }

  final String DISABLED = AppConfigDao.SIGNIFY_NOT_SET;  // disabled/not-set for a site, overriding a default
  final String NEVER_GLOBAL = AppConfigDao.SIGNIFY_NOT_SET;
  final String NO_VALUE = AppConfigDao.NO_VALUE;

  /**
   * Sets up all the configuration parameters for your sites.
   *
   * This should only be used in production for a new database, otherwise you can override
   * any existing configuration parameters in the database which differ.
   */
  static protected class SiteParams {
    AppConfigDao.ConfigType type=AppConfigDao.ConfigType.CONFIGPARAM;
    Long site;
    final AppConfigDao appDao; // for inserting config params into the database
    public SiteParams(Database db) {
      User admin = new User(1L, "admin", "Admin", 1L, "", true);
      appDao = new AppConfigDao(db, admin); // ServerUtils.getAdminUser - ServerUtils isn't initted yet
    }

    public SiteParams setSite(long siteVal) {
      site = Long.valueOf(siteVal);
      return this;
    }
    public SiteParams setType(AppConfigDao.ConfigType typ) {
      type = typ;
      return this;
    }
    public void add(String key, String value) {
      appDao.addOrEnableAppConfigEntry(site, type, key, value);
    }
    public void addSurveyClassFor(String key, String value) {
      add("SurveyClassFor"+key, value);
    }
    public void addRandomSet(RandomSetter rset) {
      appDao.addOrEnableAppConfigEntry(site, AppConfigDao.ConfigType.RANDOMSET, rset.getName(), rset.toJsonString());
    }
  }

  /**
   * Sets all the database site configuration parameters - the global parameters,
   * the global ones that are just used as site-defaults, and the site-specific ones.
   */
  abstract public void configSiteParameters();

  /**
   * At least one email template must already exist in the database for tests, so
   * the CreateRegistryDefaultData populates site #1's.
   *
   * During server initialization, if a site has no email templates in the database,
   * the software will load them into the database from files. So a do-nothing
   * default is provided.
   */
  @SuppressWarnings("EmptyMethod")
  public void configEmails() {
    // does nothing- lets the server pick them up from resources
  }

  public void configEmails(int siteNumber, boolean doStd6, String...names) {
    String body = "Hello. We would like you to please complete a new questionnaire. You can access the questionnaire by clicking on the following link:\n"
        +"[SURVEY_LINK]\n"
        +"This link will be valid through  [SURVEY_DATE] only.\n\n"
        +"If you have any questions or have any troubles accessing the questionnaire via the link please give us a call at 1-555-1212.\n\n"
        +"Thank you,\n"
        +"The Wonderful Clinic staff";

    final String[] std = { "FollowUp", "FollowUp-reminder", "Initial", "Initial-reminder", "No-appointment", "No-appointment-reminder" };

    SiteParams siteParams = new SiteParams(db).setSite(siteNumber).setType(AppConfigDao.ConfigType.EMAILTEMPLATE);

    if (doStd6) {
      for (String name: std) {
        siteParams.add(name, body);
      }
    }

    for (String name: names) {
      siteParams.add(name, body);
    }
  }

  abstract public void createSurveySystemsAndStudies();

  abstract public void addSurveyCompletions();

  abstract public void addPatientResultTypes();

  /**
   * Lets other developers easily add more.
   */
  public void addOther() {
    // optional
  }
}
