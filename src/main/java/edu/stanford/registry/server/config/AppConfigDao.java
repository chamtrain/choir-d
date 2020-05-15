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

package edu.stanford.registry.server.config;

import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.User;

import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * CRUD operations for application site specific configuration data in our database.
 *
 * There are different types of configuration data.  See the ConfigType enum.
 *
 * To change a configuration parameter, you'll want to use this DAO's
 */
public class AppConfigDao {
  // To signify a value is not set, use this value and a disabled config param will be added.
  // Nulls are automatically disabled and set to this.  This goes into the database as -no-value-
  public static final String SIGNIFY_NOT_SET = "-not-set-";

  // To add an empty value, like when a site wants to override an existing global value, use this.
  public static final String NO_VALUE = "-no-value-";


  private final Database database;
  private final User authenticatedUser;
  private static final Logger logger = LoggerFactory.getLogger(AppConfigDao.class);

  public enum ConfigType {
    CONFIGPARAM,   // configparam
    EMAILTEMPLATE, // emailtemplate
    EMAILCONTENTTYPE, // Content-Type
    CUSTOM,        // custom
    SQUARETABLE,   // squaretable
    SURVEYCONTENT, // surveycontent
    RANDOMSET,     // randomset
    BUILDER;

    @Override
    public String toString() {
      return this.name().toLowerCase();
    }

    static HashSet<String>misses;
    static String listMessage;

    // With a bad config type, output an info message with the standard ones
    static void logMessageWithStandardTypes() {
      if (listMessage == null) {
        StringBuilder sb = new StringBuilder(100);
        char delim = ':';
        sb.append("Standard app_config types are");
        for (ConfigType t: ConfigType.values()) {
          sb.append(delim).append(' ').append(t.toString());
          delim = ',';
        }
        listMessage = sb.toString();
      }
      logger.info(listMessage);
    }

    /**
     * This gives the log a small warning about non-standard configuration types, including null.
     * It only prints out a message the first time it is called, and when it misses.
     */
    static public void warnNewConfigTypes(String configType) {
      if (misses == null) {
        misses = new HashSet<String>();
        logMessageWithStandardTypes();
      }

      try {
        if (ConfigType.valueOf(configType.toUpperCase()) != null)
          return;  // it's a known type
      } catch (Throwable t) {  // null->NPE, bad->IllegalArg
        // failed
      }

      if (configType == null)
        configType = "null";
      if (misses.contains(configType))
        return;  // already logged
      misses.add(configType);
      logMessageWithStandardTypes();
      logger.warn("Single warning for this config type: Non-standard config type used: "+configType);
    }
  }

  private enum ChangeType {
    ADD_CONFIG("A") {
    },
    DISABLE_CONFIG("D") {
    },
    ENABLE_CONFIG("E") {
    },
    MODIFY_CONFIG("M") {
    };

    private final String databaseFlag;

    ChangeType(String databaseFlag) {
      this.databaseFlag = databaseFlag;
    }

    public String getDatabaseFlag() {
      return databaseFlag;
    }
  }

  /**
   * This is for fetching only. Changes require the authenticated user
   */
  public AppConfigDao(Database database) {
    this(database, null);
  }

  /**
   * @param authenticatedUser - Only needed for updates inserting change history
   */
  public AppConfigDao(Database database, User authenticatedUser) {
    this.database = database;
    this.authenticatedUser = authenticatedUser;
  }

  final static String SELECT_ALL6_FROM_APPCONFIG_AC =
      "SELECT ac.app_config_id, ac.survey_site_id, ac.config_name, "
      +      "ac.config_value,  ac.enabled,        ac.config_type FROM app_config ac ";
  final static String SELECT_FROM_APPCONFIGHIST_ACCH =
      "SELECT ac.app_config_id, ac.survey_site_id, ac.config_name, "
          +      "acch.config_value,  ac.enabled,  ac.config_type FROM app_config ac ";
  /**
   * Fetches an app_config entry from the database.
   * @param surveySiteId  For surveycontent types are only global, site==0
   * @param configType  The lowercase names of the ConfigType enum
   * @param configName  Only surveycontent types can contain "@revisionNumber"
   */
  public AppConfigEntry findAppConfigEntry(final Long surveySiteId, final String configType, final String configName) {

    if (configName != null && !configName.contains("@")) {
        return  database.toSelect(SELECT_ALL6_FROM_APPCONFIG_AC
                               + "WHERE config_type = ? AND config_name = ? AND survey_site_id = ?")
            .argString(configType.toLowerCase()).argString(configName).argLong(surveySiteId)
            .query(new SingleEntryCollector());
      } else if (configName != null) {
        // Looking for survey content.  '@' is used to mark a version found in the history table, so a published survey
        // will use this version even if people change the app_config.config_value with the SurveyBuilder.
        String values[] = configName.split("@");
        AppConfigEntry entry = null;
        entry = database.toSelect(SELECT_FROM_APPCONFIGHIST_ACCH + ", app_config_change_history acch "
                  + " where ac.config_type = ? and ac.config_name = ? and ac.survey_site_id = ?"
                  + " and acch.revision_number = ? and ac.app_config_id = acch.app_config_id ")
              .argString(configType.toLowerCase())
              .argString(values[0])
              .argLong(surveySiteId)
              .argLong(Long.valueOf(values[1]))
              .query(new SingleEntryCollector());

        if (entry == null && "squaretable".equals(configType)) {
          entry = database.toSelect(SELECT_ALL6_FROM_APPCONFIG_AC
                  + " WHERE ac.config_type = ? and ac.config_name = ? and ac.survey_site_id = ?")
              .argString(configType.toLowerCase())
              .argString(configName)
              .argLong(surveySiteId)
              .query(new SingleEntryCollector());
        }
        return entry;
      }

    return null;
  }

  public AppConfigEntry findAppConfigEntry(final Long appConfigId) {
    return database.toSelect(SELECT_ALL6_FROM_APPCONFIG_AC + "where app_config_id = ?")
        .argLong(appConfigId)
        .query(new SingleEntryCollector());
  }

  public ArrayList<AppConfigEntry> findAllAppConfigEntrySortedById(final String configType) {
    return database.toSelect(SELECT_ALL6_FROM_APPCONFIG_AC + "WHERE config_type = ? ORDER BY survey_site_id")
        .argString(configType.toLowerCase())
        .query(new MultipleEntryCollector());
  }

  public ArrayList<AppConfigEntry> findAllAppConfigEntry(final Long surveySiteId, final String configType) {
    return database.toSelect(SELECT_ALL6_FROM_APPCONFIG_AC + "WHERE config_type = ? AND survey_site_id = ?")
        .argString(configType.toLowerCase()).argLong(surveySiteId)
        .query(new MultipleEntryCollector());
  }

  /**
   * See the other addOrEnableAppConfigEntry(...) method
   * @param configType The enum from this class, rather than a string
   */
  public boolean addOrEnableAppConfigEntry(Long surveySiteId, ConfigType configType, String configName, String configValue) {
    return addOrEnableAppConfigEntry(surveySiteId, configType.toString(), configName, configValue);
  }

  /**
   * This leaves the site's named property of the given type in an enabled state, with the specified value,
   * with two exceptions.
   *
   * If you pass in a value of null or SIGNIFY_NOT_SET, the value is set to "-no-value-" and the
   * parameter is disabled. This makes it easy to add a disabled value so each site can have a full
   * set of parameters, making it easier to see what might need to be changed.
   *
   * Empty values are handy so the code can override a global default value with a site-specific null.
   * To signify a site-specific value is not set, there's a special value, NO_VALUE = "-no-value-".
   * If you pass an empty string as a configuration value, it'll be an enabled "-no-value-".
   *
   * Note this is different from the version-1 behavior which substituted the property name for null and empty values.
   *
   * Note that a zero siteId value makes it a global value.  Usually, a site-specific value overrides a global
   * value, which is treated as a default for all sites.  Some values are site-only, and ignore the global.
   * Some parameters are only global, and can't be overridden by the site.
   *
   * Globals specified in the context.xml or web.xml are called "static" and are not overridden by database values.
   * There are 3 reasons to set one of these in the database:
   * 1) As preparation for removing it from the xml file and restarting.
   * 2) Simply to document the value (though there's no way to signify that it's overridden by a static value)
   * 3) To mislead other admins
   *
   * @param siteId  0 (zero) means it's a global parameter, or a default for sites.
   * @param configValue  If null, this entry is set to -no-value- and disabled.  If blank, it becomes an enabled -no-value-.
   * @return true if the value was modified.
   */
  public boolean addOrEnableAppConfigEntry(Long siteId, String configType, String configName, String configValue) {
    if (siteId.intValue() < 0) {
      throw new RuntimeException(String.format("Setting a siteId(%d) < 0 is prohibited, typ=%s, %s = %s", siteId, configType, configName, configValue));
    }
    String enabled = "Y";

    if (configValue == null || SIGNIFY_NOT_SET.equals(configValue)) {
      configValue = NO_VALUE;
      enabled = "N";
    } else if (configValue.isEmpty()) {
      configValue = NO_VALUE;
    }

    if (configType !=null)
      configType = configType.toLowerCase();
    ConfigType.warnNewConfigTypes(configType);

    boolean modified = false;
    AppConfigEntry existing = findAppConfigEntry(siteId, configType, configName);
    if (existing == null) {
      // There could be an existing entity for this configuration for some reason, so let's keep the same entity_id
      // if that is the case (not strictly necessary, but helpful in some cases)
      Long configId = database.toInsert("insert into app_config (app_config_id, survey_site_id, config_type, config_name, enabled) "
              + "values (:pk,?,?,?,?)")
          .argPkSeq(":pk", "app_config_sequence")
          .argLong(siteId)
          .argString(configType)
          .argString(configName)
          .argString(enabled)
          .insertReturningPkSeq("app_config_id");
      database.toUpdate("update app_config set config_value = ? where app_config_id = ?")
          .argClobString(configValue)
          .argLong(configId).update(1);

      insertChangeHistory(ChangeType.ADD_CONFIG, findAppConfigEntry(configId));
      modified = true;
    } else {
      if (!configValue.equals(existing.getConfigValue())) {
        database.toUpdate("update app_config set config_value = ? where survey_site_id =? and config_name = ? and config_type = ?")
            .argClobString(configValue).argLong(siteId).argString(configName).argString(configType).update(1);
        insertChangeHistory(ChangeType.MODIFY_CONFIG, existing);
        modified = true;
      }
      if (!existing.isEnabled()) {
        database.toUpdate("update app_config set enabled='Y' where survey_site_id =? and config_name = ? and config_type = ?")
        .argLong(siteId).argString(configName).argString(configType).update(1);
        insertChangeHistory(ChangeType.ENABLE_CONFIG, existing);
        modified = true;
      } else if (existing.isEnabled() && enabled.equals("N")) { // only for config params
        disableAppConfig(existing.getAppConfigId());
      }
    }
    return modified;
  }

  public boolean disableAppConfig(Long appConfigId) {
    boolean modified = false;
    AppConfigEntry existing =  findAppConfigEntry(appConfigId);
    if (existing != null && existing.isEnabled()) {
      database.toUpdate("update app_config set enabled='N' where app_config_id=?")
          .argLong(appConfigId).update(1);
      insertChangeHistory( ChangeType.DISABLE_CONFIG, existing);
      modified = true;
    }
    return modified;
  }

  private void insertChangeHistory( ChangeType changeType, AppConfigEntry existing) {
    Long authenticatedUserId = authenticatedUser.getUserPrincipalId(); // NPE if created wi no user
    if (existing == null || authenticatedUserId == null) {
      return;
    }
    Long revNumber = database.toInsert("insert into app_config_change_history (revision_number, user_principal_id, changed_at_time,"
        + "change_type, app_config_id, survey_site_id, config_name, config_type) "
        + "values (:rev,:u,:dt,:chType,:appConfigId,:siteId,:configName,:configType)")
        .argPkSeq(":rev", "app_config_change_sequence")
        .argLong(":u", authenticatedUserId)
        .argDateNowPerDb(":dt")
        .argString(":chType", changeType == null ? null : changeType.getDatabaseFlag())
        .argLong(":appConfigId", existing.getAppConfigId())
        .argLong(":siteId", existing.getSurveySiteId())
        .argString(":configName", existing.getConfigName())
        .argString(":configType", existing.getConfigType())
        .insertReturningPkSeq("revision_number");

      if (existing.getConfigValue() != null) {
        try {
          database.toUpdate("update app_config_change_history set config_value = ? where revision_number = ?")
              .argClobString(existing.getConfigValue())
              .argLong(revNumber)
              .update(1);
        } catch (Exception ex) {
          logger.error("Error updating history with clob value: " + existing.getConfigValue());
        }
      }
  }

  public void updateStudyTitle(Study study) {
    database.toUpdate("update study set title = ?, dt_changed = sysdate where survey_system_id = ? and study_code = ?")
        .argString(study.getTitle())
        .argInteger(study.getSurveySystemId())
        .argInteger(study.getStudyCode())
        .update(1);
  }

  // ==== classes to process queries - each fetches all 6 columns, Id, SiteId, Name, Value, Enabled, Type

  static public String changeNoValueToNull(String value) {
    return NO_VALUE.equals(value) ? null : value;
  }

  static class EntryCollector {
    protected AppConfigEntry newAppConfigEntryFrom6Cols(Rows rs) {
      AppConfigEntry result = new AppConfigEntry();
      result.setAppConfigId( rs.getLongOrNull(1) );
      result.setSurveySiteId( rs.getLongOrNull(2) );
      result.setConfigName( rs.getStringOrNull(3) );
      result.setConfigValue( rs.getStringOrNull(4) );
      result.setEnabled ( "Y".equals(rs.getStringOrNull(5)) );
      result.setConfigType(rs.getStringOrNull(6));
      fixNoValueConfigParam(result);
      return result;
    }

    void fixNoValueConfigParam(AppConfigEntry result) {
      if (!result.getConfigType().equals(ConfigType.CONFIGPARAM.toString()))
          return; // this logic is only for configparams

      if (result.getConfigValue().equals(NO_VALUE) && result.isEnabled())
        result.setConfigValue("");
    }
  }

  static class MultipleEntryCollector extends EntryCollector implements RowsHandler<ArrayList<AppConfigEntry>> {
    @Override
    public ArrayList<AppConfigEntry> process(Rows rs) throws Exception {
      ArrayList<AppConfigEntry> list = new ArrayList<>();
      while (rs.next()) {
        list.add(newAppConfigEntryFrom6Cols(rs));
      }
      return list;
    }
  }

  static class SingleEntryCollector extends EntryCollector implements RowsHandler<AppConfigEntry> {
    @Override
    public AppConfigEntry process(Rows rs) throws Exception {
      if (rs.next())
        return newAppConfigEntryFrom6Cols(rs);

      return null;
    }
  }

}
