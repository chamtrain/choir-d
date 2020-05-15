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

import edu.stanford.registry.server.DataTableObjectConverter;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DisplayProvider;
import edu.stanford.registry.shared.Provider;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.UserIdp;
import edu.stanford.registry.shared.UserPreference;
import edu.stanford.registry.shared.UserPrincipal;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Flavor;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlInsert;

/**
 * CRUD operations for user-related data in our database.
 */
public class UserDao {
  private final Database database;
  private final User authenticatedUser;
  private final User authorizinguser;
  private final Logger logger = LoggerFactory.getLogger(UserDao.class);
  private enum ChangeType {
    ADD_USER("A") {
    },
    ENABLE_USER("E") {
    },
    DISABLE_USER("D") {
    },
    MODIFY_USER("M") {
    },
    GRANT_AUTHORITY("G") {
    },
    REVOKE_AUTHORITY("R") {
    };

    private final String databaseFlag;

    ChangeType(String databaseFlag) {
      this.databaseFlag = databaseFlag;
    }

    public String getDatabaseFlag() {
      return databaseFlag;
    }
  }

  public UserDao(Database database, User authenticatedUser, User authorizingUser) {
    this.database = database;
    this.authenticatedUser = authenticatedUser;
    this.authorizinguser = authorizingUser;
  }

  /**
   * Find the first occurance of this username
   * @param username The users credentials
   * @return UserPrincipal for the user with this username
   */
  public UserPrincipal findUserPrincipal(final String username) {
    String sql = "SELECT uc.user_principal_id, up.display_name, up.email_addr, uc.idp_id, uc.enabled, provider_eid, provider_id "
        + " from user_credential uc "
        + " join user_principal up on uc.user_principal_id = up.user_principal_id "
        + " left outer join provider p on (p.USER_PRINCIPAL_ID = up.USER_PRINCIPAL_ID) "
        + " WHERE uc.username = ?  order by idp_id";
    return database.toSelect(sql).argString(username.toLowerCase()).query(rs -> {
      if (rs.next()) {
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.userPrincipalId = rs.getLongOrNull();
        userPrincipal.displayName = rs.getStringOrNull();
        userPrincipal.emailAddr = rs.getStringOrNull();
        userPrincipal.username = username.toLowerCase();
        userPrincipal.idpId = rs.getLongOrNull();
        userPrincipal.enabled = "Y".equals(rs.getStringOrNull());
        userPrincipal.providerEid = rs.getStringOrNull();
        userPrincipal.providerId = rs.getLongOrZero();
        return userPrincipal;
      }
      return null;
    });
  }

  public UserPrincipal findUserPrincipal(long idpId, String username) {
    System.out.println("findUserPrincipal username is " + username);
    return database.toSelect(
        "select up.user_principal_id, uc.enabled,up.display_name, up.email_addr,provider_eid, provider_id from user_principal up "
            + "join user_credential uc on (uc.USER_PRINCIPAL_ID = up.USER_PRINCIPAL_ID) "
            + "left outer join provider p on (p.USER_PRINCIPAL_ID = up.USER_PRINCIPAL_ID) where idp_id=? and username=?")
        .argLong(idpId)
        .argString(username.toLowerCase())
        .query(rs -> {
            UserPrincipal result = null;
            if (rs.next()) {
              result = new UserPrincipal();
              result.userPrincipalId = rs.getLongOrNull(1);
              result.enabled = "Y".equals(rs.getStringOrNull(2));
              result.username = username.toLowerCase();
              result.displayName = rs.getStringOrNull(3);
              result.emailAddr = rs.getStringOrNull(4);
              result.providerEid = rs.getStringOrNull(5);
              result.providerId = rs.getLongOrZero(6);
              result.idpId = idpId;
            }
            return result;
        });
  }

  public ArrayList<UserPrincipal> findUserPrincipal(Long userPrincipalId) {
    return database.toSelect("select c.user_principal_id, p.display_name, p.email_addr, c.username, c.idp_id, "
        + " prov.provider_id, prov.provider_eid "
        + " from user_credential c join user_principal p on c.user_principal_id = p.user_principal_id "
        + " left outer join provider prov on (prov.user_principal_id = c.user_principal_id)"
        + " where c.user_principal_id = ?").argLong(userPrincipalId).query(
        rs -> {
          ArrayList<UserPrincipal> userPrincipalArr = new ArrayList<>();
          while (rs.next()) {
            UserPrincipal userPrincipal = new UserPrincipal();
            userPrincipal.userPrincipalId = rs.getLongOrNull();
            userPrincipal.displayName = rs.getStringOrNull();
            userPrincipal.emailAddr = rs.getStringOrNull();
            userPrincipal.username = rs.getStringOrNull();
            userPrincipal.idpId = rs.getLongOrNull();
            userPrincipal.providerId = rs.getLongOrNull();
            userPrincipal.providerEid = rs.getStringOrEmpty();
            userPrincipalArr.add(userPrincipal);
          }
          return userPrincipalArr;
        });
  }

  public UserAuthority[] findAllUserAuthority(final String username) {
    return database.toSelect("SELECT user_principal_id, authority FROM user_authority "
        + "WHERE user_principal_id=(SELECT user_principal_id FROM user_credential where username=?)")
        .argString(username.toLowerCase())
        .query(rs -> {
            List<UserAuthority> result = new ArrayList<>();
            while (rs.next()) {
              UserAuthority ua = new UserAuthority(rs.getLongOrNull(1), rs.getStringOrNull(2));
              result.add(ua);
            }
            return result.toArray(new UserAuthority[0]);
        });
  }

  private UserAuthority findUserAuthority(long idpId, final String username, final String authority) {
    return database.toSelect("SELECT user_principal_id,authority FROM user_authority "
        + "WHERE user_principal_id=(SELECT user_principal_id FROM user_credential WHERE idp_id = ? AND username=?)"
        + " AND authority=?")
        .argLong(idpId)
        .argString(username.toLowerCase())
        .argString(authority)
        .query(rs -> {
            if (rs.next())
              return new UserAuthority(rs.getLongOrNull(1), rs.getStringOrNull(2));
            return null;
        });
  }

  public boolean addOrEnableUser(long idpId, String username, String displayName, String emailAddress) {
    return addOrEnableUser(idpId, username, displayName, emailAddress, 0L);
  }

  public boolean addOrEnableUser(long idpId, String username, String displayName, String emailAddress, long userPrincipalId) {
    username = username.toLowerCase();
    if (displayName == null || displayName.length() == 0) {
      displayName = username;
    }
    boolean modified = false;
    logger.trace("addOrEnableUser({}, {}, {}, {})", idpId, username, displayName, emailAddress);
    UserPrincipal existing = findUserPrincipal(username);

    if (existing == null) {
      // There could be an existing entity for this user for some reason, so let's keep the same entity_id
      // if that is the case (not strictly necessary, but helpful in some cases)

      Long newUserPrincipalId;
      if (userPrincipalId != 0) {
        newUserPrincipalId = userPrincipalId;
        database.toUpdate("update user_principal set display_name=? where user_principal_id=?")
            .argString(displayName).argLong(userPrincipalId).update(1);
      } else {
        newUserPrincipalId = database.toInsert(
            "insert into user_principal (user_principal_id, display_name, email_addr) "
                + "values (:pk,?,?)").argPkSeq(":pk", "user_principal_sequence")
            .argString(displayName).argString(emailAddress)
            .insertReturningPkSeq("user_principal_id");
      }
      database.toInsert("insert into user_credential (idp_id, user_principal_id, username, enabled) "
          + "values (?, ?, ?, 'Y')").argLong(idpId).argLong(newUserPrincipalId).argString(username).insert(1);
      insertChangeHistory(idpId, newUserPrincipalId, username, ChangeType.ADD_USER, null, "for user_principal_id:" + newUserPrincipalId);
      modified = true;
    } else {
      logger.trace("existing({}, {}, {}, {}, {})", existing.idpId, existing.username, existing.displayName, existing.emailAddr, existing.enabled);
      if (!displayName.equals(existing.displayName)) {
        database.toUpdate("update user_principal set display_name=? where user_principal_id=?")
            .argString(displayName).argLong(existing.userPrincipalId).update(1);
        insertChangeHistory(idpId, existing.userPrincipalId, username, ChangeType.MODIFY_USER, null, "Updated display name from '"
            + existing.displayName + "' to '" + displayName + "' " + "for user_principal_id:" +  existing.userPrincipalId);
        modified = true;
      }
      if (!existing.enabled) {
        database.toUpdate("update user_credential set enabled='Y' where username=?").argString(username).update(1);
        insertChangeHistory(idpId, existing.userPrincipalId, username, ChangeType.ENABLE_USER, null,
            "for user_principal_id:" + existing.userPrincipalId.toString());
        modified = true;
      }
      if (emailAddress == null) {
        if (existing.emailAddr != null && existing.emailAddr.trim().length() > 0) {
          database.toUpdate("update user_principal set email_addr=? where user_principal_id=?")
              .argString(null).argLong(existing.userPrincipalId).update(1);
          modified = true;
        }
      } else if (!emailAddress.equals(existing.emailAddr)) {
        database.toUpdate("update user_principal set email_addr=? where user_principal_id=?")
            .argString(emailAddress).argLong(existing.userPrincipalId).update(1);
        modified = true;
      }
      if (idpId != existing.idpId) {
        database.toUpdate("update user_credential set idp_id = ? where user_principal_id = ? and username = ?")
            .argLong(idpId)
            .argLong(existing.userPrincipalId)
            .argString(existing.username).update();
        insertChangeHistory(idpId, existing.userPrincipalId, username, ChangeType.MODIFY_USER, null, "change user_credential.idpId");
        modified = true;
      }
    }
    return modified;
  }

  public boolean disableUser(long idpId, String username) {
    boolean modified = false;
    UserPrincipal existing = findUserPrincipal(idpId, username);
    if (existing != null && existing.enabled) {
      database.toUpdate("update user_credential set enabled='N' where idp_id=? and username = ?")
          .argLong(idpId).argString(username.toLowerCase()).update(1);
      insertChangeHistory(idpId, existing.userPrincipalId, username.toLowerCase(), ChangeType.DISABLE_USER, null,
          "for user_principal_id:" + existing.userPrincipalId.toString());
      modified = true;
    }
    return modified;
  }

  public boolean grantAuthority(long idpId, String username, String authority) {
    boolean modified = false;

    UserAuthority existing = findUserAuthority(idpId, username, authority);
    UserPrincipal userPrincipal = findUserPrincipal(idpId, username);
    if (existing == null) {
      database.toInsert("insert into user_authority (user_principal_id, authority) "
          + "values ((select user_principal_id from user_credential where idp_id=? and username = ?),?)")
          .argLong(idpId).argString(username.toLowerCase()).argString(authority)
          .insert(1);
      insertChangeHistory(idpId, userPrincipal.userPrincipalId, username.toLowerCase(), ChangeType.GRANT_AUTHORITY, authority,
          "for user_principal_id:" + userPrincipal.userPrincipalId.toString());
      modified = true;
    }
    return modified;
  }

  public boolean revokeAuthority(long idpId, String username, String authority) {
    boolean modified = false;

    UserAuthority existing = findUserAuthority(idpId, username, authority);
    if (existing != null) {
      database.toUpdate("delete from user_authority where user_principal_id=? and authority=?")
          .argLong(existing.userPrincipalId).argString(authority).update(1);
      insertChangeHistory(idpId, existing.userPrincipalId, username.toLowerCase(), ChangeType.REVOKE_AUTHORITY, authority,
          "for user_principal_id:" + existing.userPrincipalId.toString());
      modified = true;
    }
    return modified;
  }

  private void insertChangeHistory(long idpId, Long userPrincipalId, String username, ChangeType changeType, String authority, String description) {
    if (authority == null) {
      authority = "";
    }
    if (description == null) {
      description = "";
    }

    // Special case for bootstrapping when we don't have any users
    Long authenticatedUserId;
    if (authenticatedUser == null) {
      UserPrincipal self = findUserPrincipal(idpId, username);
      authenticatedUserId = self.userPrincipalId;
    } else {
      authenticatedUserId = authenticatedUser.getUserPrincipalId();
    }

    database.toInsert("insert into user_change_history (revision_number,user_principal_id,changed_at_time,"
        + "authenticated_user,authorizing_user,change_type,authority,description) "
        + "values (:rev,(select user_principal_id from user_principal where user_principal_id=:up),"
        + ":dt,:uidAuth,:uidApprover,:chType,:authority,:description)")
        .argPkSeq(":rev", "user_change_sequence")
        .argLong(":up", userPrincipalId)
        .argDateNowPerDb(":dt")
        .argLong(":uidAuth", authenticatedUserId)
        .argLong(":uidApprover", authorizinguser == null ? null : authorizinguser.getUserPrincipalId())
        .argString(":chType", changeType == null ? null : changeType.getDatabaseFlag())
        .argString(":authority", authority)
        .argString(":description", description).insert(1);
  }

  /**
   * @param role  role[site]
   * @return the site string from the role
   */
  static public String extractSiteParamFromRole(String role) {
    int ix = role.indexOf("[");
    if (ix < 0) // shouldn't happen
      return "";
    int start = ix + 1;
    int end = role.indexOf("]", ix);
    if (end < 0) // handle "foo[ped"
      end = role.length();
    return role.substring(start, end);
  }

  public static class UserAuthority {
    final Long userPrincipalId;
    final public String authority;

    UserAuthority(Long principalId, String authRole) {
      userPrincipalId = principalId;
      authority = authRole;
    }
    @SuppressWarnings("unused")
    public String getSiteUrlParam() {
      return extractSiteParamFromRole(authority);
    }
  }

  public List<UserPrincipal> findUsersWithDisplayName(String siteName, final String displayName) {
    if (displayName == null || displayName.trim().length() < 1) {
      return findAllUserPrincipal(siteName);
    }
    String siteId = "%[" + siteName + "]";
    String name = "%" + displayName.trim() + "%";
    return database.toSelect(
          "select up.user_principal_id,enabled, display_name, email_addr, username, provider_id, provider_eid, uc.idp_id "
        + "from user_principal up join user_credential uc on up.user_principal_id = uc.user_principal_id "
        + "left outer join provider p on (p.user_principal_id = up.user_principal_id), user_authority ua "
        + "where ua.user_principal_id = up.user_principal_id and ua.authority like ? and lower(display_name) like ? "
        + "group by uc.idp_id, up.user_principal_id, enabled, display_name, email_addr, username, provider_id, provider_eid"
        ).argString(siteId).argString(name.toLowerCase())
        .query(new UserPrincipalRowHandler());
  }

  public List<UserPrincipal> findAllUserPrincipal(String siteShortName) {
    String siteId = "%[" + siteShortName + "]";
    return database.toSelect(
        "select up.user_principal_id, enabled, display_name, email_addr, username, provider_id, provider_eid, uc.idp_id "
        + "from user_principal up join user_credential uc on up.user_principal_id = uc.user_principal_id "
        + "left outer join  provider p on (p.user_principal_id = up.user_principal_id), user_authority ua "
        + "where ua.user_principal_id = up.user_principal_id and ua.authority like ? "
        + "group by uc.idp_id, up.user_principal_id, enabled, display_name, email_addr, username, provider_id, provider_eid")
        .argString(siteId)
        .query(new UserPrincipalRowHandler());
  }

  public List<Long> findAllAdmins(String siteShortName) {
    String authority = Constants.ROLE_SECURTY + "[" + siteShortName + "]";
    return database.toSelect( "select user_principal_id from user_authority where authority = ? ")
        .argString(authority).query(rs -> {
          List<Long> admins = new ArrayList<>();
          while (rs.next()) {
            admins.add(rs.getLongOrZero(1));
          }
          return admins;
        });
  }
  private static class UserPrincipalRowHandler implements RowsHandler<List<UserPrincipal>> {
    @Override
    public List<UserPrincipal> process(Rows rs) {
      List<UserPrincipal> result = new ArrayList<>();
      while (rs.next()) {
        UserPrincipal user = new UserPrincipal();
        user.userPrincipalId = rs.getLongOrNull(1);
        user.enabled = "Y".equals(rs.getStringOrNull(2));
        user.displayName = rs.getStringOrNull(3);
        user.emailAddr = rs.getStringOrNull(4);
        user.username = rs.getStringOrNull(5);
        user.providerId = rs.getLongOrZero(6);
        user.providerEid = rs.getStringOrNull(7);
        user.idpId = rs.getLongOrNull(8);
        result.add(user);
      }
      return result;
    }
  }
  private static final String providerSql = "select provider_id, provider_eid, user_principal_id, dt_created, dt_changed from provider";
  public ArrayList<Provider> findProviders(boolean onlyUnassigned) {
    String sql = providerSql;
    if (onlyUnassigned) {
      sql = sql + " where user_principal_id is null and provider_eid != '-'";
    }
    return database.toSelect(sql).query(rs ->{

        ArrayList<Provider>  result = new ArrayList<>();
        while (rs.next()) {
          Provider provider = new Provider();
          provider.setProviderId(rs.getLongOrNull(1));
          provider.setProviderEid(rs.getStringOrNull(2));
          provider.setUserPrincipalId(rs.getLongOrNull(3));
          provider.setDtCreated(rs.getDateOrNull(4));
          provider.setDtChanged(rs.getDateOrNull(5));
          result.add(provider);
        }
        return result;
    });
  }

  public Provider getProvider(long providerId) {
    String sql = providerSql + " WHERE provider_id = ? ";
    ArrayList<Provider> providers = database.toSelect(sql).argLong(providerId).query(rs -> DataTableObjectConverter.convertToObjects(rs, Provider.class));
    if (providers != null && providers.size() > 0) {
      return providers.get(0);
      }
    return null;
  }

  public Provider getProviderByEid(String providerEid) {
    String sql = providerSql + " where provider_eid = ?  ";
    ArrayList<Provider> providers = database.toSelect(sql).argString(providerEid).query(rs -> DataTableObjectConverter.convertToObjects(rs, Provider.class));

    if (providers != null && providers.size() > 0) {
      return providers.get(0);
    }
    return null;
  }

  public Provider getUserProvider(Long userPrincipalId) {

    String sql = providerSql + " where user_principal_id = ? ";
    ArrayList<Provider> providers = database.toSelect(sql).argLong(userPrincipalId).query(rs -> DataTableObjectConverter.convertToObjects(rs, Provider.class));

    if (providers != null && providers.size() > 0) {
      return providers.get(0);
    }
    return null;
  }

  public void updateProvider(Provider provider) {
    String stmt = "UPDATE provider SET provider_eid = ?, user_principal_id = ?, DT_CHANGED = :now "
        + " WHERE provider_id = ? ";
    database.toUpdate(stmt)
        .argString(provider.getProviderEid())
        .argLong(provider.getUserPrincipalId())
        .argDateNowPerDb(":now")
        .argLong(provider.getProviderId())
    .update(1);
  }

  private Provider insertProvider(Provider provider) {
    if (provider == null) {
      return null;
    }
    String stmt = "INSERT INTO provider (provider_id, provider_eid, user_principal_id, dt_created) "
        + "VALUES (:pk, ?, ?, :now) ";

    provider.setProviderId(database.toInsert(stmt)
        .argPkSeq(":pk", "user_principal_sequence")
        .argString(provider.getProviderEid())
        .argLong(provider.getUserPrincipalId())
        .argDateNowPerDb(":now")
        .insertReturningPkSeq("provider_id"));
    return provider;
  }

  public Provider writeProvider(Provider provider) {
    if (provider != null) {
      if (provider.getProviderId() == null) {
        return insertProvider(provider);
      } else {
        updateProvider(provider);
      }
    }
    return provider;
  }

  public ArrayList<DisplayProvider> findDisplayProviders(SiteInfo siteInfo) {
    // Select providers where the provider is referenced by an appointment or the
    // provider has a permission on the site
    String sql = "select p.provider_id, p.provider_eid, p.user_principal_id, p.dt_created, p.dt_changed, " +
        "c.username, u.display_name from provider p, user_principal u, user_credential c " +
        "where u.user_principal_id = p.user_principal_id and u.user_principal_id = c.user_principal_id and " +
        "( exists (select * from appt_registration ar where ar.provider_id = p.provider_id and ar.survey_site_id = ?) or " +
        "  exists (select * from user_authority ua where ua.user_principal_id = p.user_principal_id and authority like ('%[' || ? || ']')) )";

    return database.toSelect(sql)
        .argLong(siteInfo.getSiteId())
        .argString(siteInfo.getUrlParam())
        .query(rs -> {
        ArrayList<DisplayProvider>  result = new ArrayList<>();
        while (rs.next()) {
          DisplayProvider provider = new DisplayProvider();
          provider.setProviderId(rs.getLongOrNull(1));
          provider.setProviderEid(rs.getStringOrNull(2));
          provider.setUserPrincipalId(rs.getLongOrNull(3));
          provider.setDtCreated(rs.getDateOrNull(4));
          provider.setDtChanged(rs.getDateOrNull(5));
          provider.setUsername(rs.getStringOrNull(6));
          provider.setDisplayName(rs.getStringOrNull(7));
          result.add(provider);
        }
        return result;
    });
  }

  private UserPreference findUserPreference(final long principalId, final long surveySiteId, final String userPreferenceKey) {
    return database.toSelect("select preference_value from user_preference where user_principal_id = ? and survey_site_id = ? and preference_key = ?")
        .argLong(principalId)
        .argLong(surveySiteId)
        .argString(userPreferenceKey)
        .query(
        (rs -> {
            UserPreference userPreference = null;
            if (rs.next()) {
              userPreference = new UserPreference();
              userPreference.setPreferenceValue(rs.getStringOrNull(1));
              userPreference.setUserPrincipalId(principalId);
              userPreference.setSurveySiteId(surveySiteId);
              userPreference.setPreferenceKey(userPreferenceKey);
            }
            return userPreference;
        }));
  }

  public ArrayList<UserPreference> findUserPreferences(final long principalId, final long surveySiteId) {
    return database.toSelect("select preference_key, preference_value from user_preference where user_principal_id = ?  and survey_site_id = ?")
        .argLong(principalId)
        .argLong(surveySiteId)
        .query(
        (rs -> {
          ArrayList<UserPreference> preferences = new ArrayList<>();

            while (rs.next()) {
              UserPreference userPreference = new UserPreference();
              userPreference.setPreferenceKey(rs.getStringOrNull(1));
              userPreference.setPreferenceValue(rs.getStringOrNull(2));
              userPreference.setUserPrincipalId(principalId);
              userPreference.setSurveySiteId(surveySiteId);
              preferences.add(userPreference);
            }
            return preferences;
        }));
  }

  public void setUserPreference(UserPreference userPreference) {
    UserPreference preference = findUserPreference(userPreference.getUserPrincipalId(), userPreference.getSurveySiteId(),
        userPreference.getPreferenceKey());
    if (preference == null) {
      database.toInsert("insert into user_preference (user_principal_id, survey_site_id, preference_key, preference_value) values(?, ?, ?, ?)")
          .argLong(userPreference.getUserPrincipalId())
          .argLong(userPreference.getSurveySiteId())
          .argString(userPreference.getPreferenceKey())
          .argString(userPreference.getPreferenceValue()).insert(1);

    } else {
      database.toUpdate("update user_preference set preference_value = ? where user_principal_id = ? and survey_site_id = ? and preference_key = ?")
          .argString(userPreference.getPreferenceValue())
          .argLong(userPreference.getUserPrincipalId())
          .argLong(userPreference.getSurveySiteId())
          .argString(userPreference.getPreferenceKey()).update(1);
    }
  }

  public UserIdp findDefaultIdp() {
    ArrayList<UserIdp> userIdps = findAllIdp();
    if (userIdps.size() > 0) {
      return userIdps.get(0);
    }
    return null;
  }

  public ArrayList<UserIdp> findAllIdp() {
    assert database != null;
    return database.toSelect("select idp_id, abbr_name, display_name from user_idp order by idp_id")
        .query(
            rs -> {
              ArrayList<UserIdp> allIdps = new ArrayList<>();
              while (rs.next()) {
                UserIdp userIdp = new UserIdp(rs.getLongOrNull(), rs.getStringOrNull(), rs.getStringOrNull());
                allIdps.add(userIdp);
              }
              return allIdps;
            });
  }

  public UserIdp addOrUpdateIdp(String abbrName, String displayName) {
    SqlInsert sqlInsert;
    if (database.flavor().equals(Flavor.oracle)) {
      sqlInsert = database.toInsert("merge into user_idp idp using (select ? as abbr_name, ? as display_name from dual) src "
          + " on (idp.abbr_name = src.abbr_name) "
          + " when matched then update set idp.display_name = src.display_name"
          + " when not matched then insert(idp_id, abbr_name, display_name) "
          + " values( ?, ?, ?)")
          .argString(abbrName)
          .argString(displayName)
          .argPkSeq("user_idp_sequence")
          .argString(abbrName)
          .argString(displayName);
    } else {
      sqlInsert = database.toInsert("insert into user_idp (idp_id, abbr_name, display_name) values (?, ?, ?) "
          + "on conflict (abbr_name) do update set display_name = excluded.display_name ")
          .argPkSeq("user_idp_sequence")
          .argString(abbrName)
          .argString(displayName);
    }
    Long idpId = sqlInsert
          .insertReturningPkSeq("idp_id");

    // PK is not being returned when running on oracle so check and look it up if necessary
    logger.trace("Insert idpd {} returned ipdId {}", abbrName, idpId);
    if (idpId == null) {
      idpId = database.toSelect("select idp_id from user_idp where abbr_name = ?")
          .argString(abbrName)
          .query(
          rs -> {

            if (rs.next()) {
              return rs.getLongOrNull();
            }
            return null;
          });
    }
    return new UserIdp(idpId, abbrName, displayName);
  }
}
