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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * This caches all the values from the AppConfig table.
 *
 * It is initialized before Users, so must not depend on them.
 *
 * Never update these hashes. If the app_config table changes, tell the cache to reload.
 */
public class AppConfig {
  private static final Logger logger = Logger.getLogger(AppConfig.class.getName());

  // Map<siteId, Map<ConfigType, Map<ConfigName, AppConfigEntry>>>
  // This is a ConcurrentHashMap so we don't have to use the lock on each get.
  private final ConcurrentHashMap<Long, Map<String, Map<String, AppConfigEntry>>> siteEntries = new ConcurrentHashMap<>();

  private PropertyMap buildProperties; // These override what's in the database, wi syntax, type.prop, configparam.import_url

  private long lastRevision = 0;       // maximum revision fetched
  final private HashMap<Long,Long> lastRevisions;  // maximum revision per site

  public AppConfig() {  // needed for Mock to work
    this(null);
  }

  /**
   * @param loadEntries - determines whether or not to load the cache from the database
   */
  public AppConfig(PropertyMap buildProperties) {
    this.buildProperties = buildProperties;
    lastRevisions = new HashMap<Long,Long>();
  }

  public long getLastRevision() {
    return lastRevision;
  }

  public Long getLastRevision(Long siteId) {
    return lastRevisions.get(siteId);
  }

  /**
   * This only updates sites that have changed. It only updates sites that have changed.
   * It's synchronized, so can be called asynchronously if the database changes.
   */
  public synchronized void refresh(final Supplier<Database> database) {
    long maxRevision = refreshChangedSitesFromDb(database);
    if (lastRevision < maxRevision) {
      lastRevision = maxRevision;
      logger.debug("Last revision is now: "+lastRevision);
    }
  }

  /**
   * Returns the largest revision number for the appConfig table
   * after reloading all config data for sites with larger numbers.
   */
  protected Long refreshChangedSitesFromDb(final Supplier<Database> database) {
    // TODO: Extract this to a function and write unit tests
    RowsHandler<Long> rowsHandler = new RowsHandler<Long>() {
      @Override
      public Long process(Rows rs) throws Exception {
        Long biggestRevision = lastRevision;
        while (rs.next()) {
          long surveySiteId = rs.getLongOrNull(1);
          long revision = rs.getLongOrZero(2);
          if (biggestRevision < revision)
            biggestRevision = revision;
          loadSite(database, surveySiteId, revision);
        }
        return biggestRevision;
      }
    };

    if (lastRevision == 0) {
      return database.get()  // the first time, give each site the max revision_number in the history
          .toSelect("SELECT survey_site_id, (select max(revision_number) from app_config_change_history)rev "
                  + "FROM app_config WHERE enabled = 'Y' GROUP BY survey_site_id")
          .query(rowsHandler);
    }
    else
      return database.get() // subsequent times, just update sites that have changed
          .toSelect("SELECT survey_site_id, max(revision_number)rev FROM app_config_change_history "
                  + "WHERE revision_number > ? GROUP BY survey_site_id")
          .argLong(lastRevision)
          .query(rowsHandler);
  }

  /**
   * Loads in a new single site, then puts it into the hash-by-site at the very end.
   * Thus the hash-by-site always has a full set of site values.
   *
   * Note that config values come from SitesInfo, and that's not updated till this is done.
   * This whole object might be unnecessary until we have a UI for viewing and changing
   * app_config entries.
   *
   * This is protected to aid testing.
   *
   * @param revision is the new max(revision) of this site's values. It doesn't matter if it's
   * the max of this site's values or all site's values. Any change will have a greater value.
   */
  private void loadSite(Supplier<Database> database, final Long siteId, final Long revision) {
    final Map<String, Map<String, AppConfigEntry>> newEntries = new HashMap<>();

    // Record our position in updates first to avoid race conditions if someone
    // updates while we are reading

    logger.debug("For site "+siteId+", Loading appConfig parameters from db, since revision" + lastRevision);

    // add their configurations
    database.get().toSelect("SELECT app_config_id, config_type, config_name, config_value "
                          + "FROM app_config WHERE survey_site_id = ? and enabled = 'Y'")
        .argLong(siteId)
        .query(new RowsHandler<Object>() {
      @Override
      public Object process(Rows rs) {
            while (rs.next()) {
              long appConfigId = rs.getLongOrNull(1);
              String appConfigType = rs.getStringOrEmpty(2);
              String appConfigName = rs.getStringOrNull(3);
              String appConfigValue = AppConfigDao.changeNoValueToNull(rs.getStringOrNull(4));

              Map<String, AppConfigEntry> typeEntries = newEntries.get(appConfigType);
              if (typeEntries == null) {
                typeEntries = new HashMap<>();
              }
              AppConfigEntry configEntry = typeEntries.get(appConfigName);
              if (configEntry == null) {
                configEntry = new AppConfigEntry(siteId, appConfigId, appConfigType, appConfigName, appConfigValue, true);
              }

              if (logger.isTraceEnabled()) {
                logger.trace("adding configuration =" + appConfigType + "/" + appConfigName + " to site " + siteId);
              }
              // See if there's an override
              String overrideValue = buildProperties.getString(appConfigType + "." + appConfigName);
              if (!isNullOrEmpty(overrideValue)) {
                if (!overrideValue.equals(appConfigValue)) {
                  logger.warn("Site: "+siteId+" Value in database for appConfig named '"+appConfigName+"' won't override build.properties value.");
                }
                configEntry.setConfigValue(overrideValue);
              }
              typeEntries.put(appConfigName, configEntry);
              newEntries.put(appConfigType, typeEntries);
            }

            siteEntries.put(siteId, newEntries);
            lastRevisions.put(siteId, revision); // this could trigger a reload, so newEntries is updated first.
            return null;
          }
        });
  }

  public String forName(final Long surveySiteId, final String appConfigType, final String appConfigName) {
    Map<String, Map<String, AppConfigEntry>> thisSiteEntries = siteEntries.get(surveySiteId);
    if (thisSiteEntries != null) {
      Map<String, AppConfigEntry> thisTypeEntries = thisSiteEntries.get(appConfigType);
      if (thisTypeEntries != null) {
        AppConfigEntry appConfigEntry = thisTypeEntries.get(appConfigName);
        if (appConfigEntry != null) {
          return appConfigEntry.getConfigValue();
        }
      }
    }

    return null;
  }

  public HashMap<String, String> forAll(final Long surveySiteId, final String appConfigType) {
    HashMap<String, String> results = new HashMap<>();

    Map<String, Map<String, AppConfigEntry>> thisSiteEntries = siteEntries.get(surveySiteId);
    if (thisSiteEntries != null) {
      Map<String, AppConfigEntry> thisTypeEntries = thisSiteEntries.get(appConfigType);
      if (thisTypeEntries != null) {
        for (Map.Entry<String, AppConfigEntry> entry: thisTypeEntries.entrySet()) {
          results.put(entry.getKey(), entry.getValue().getConfigValue());
        }
      }
    }
    return results;
  }

  public void updateEntry(AppConfigEntry configEntry) {
    throw new RuntimeException("Do not update AppConfig entries. Cause the cache to reload");
  }

  private boolean isNullOrEmpty(String value) {
    if (value == null) {
      return true;
    }
    if (value.trim().isEmpty()) {
      return true;
    }
    return false;
  }
}
