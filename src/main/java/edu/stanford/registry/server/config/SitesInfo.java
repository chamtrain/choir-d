/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.config.AppConfig;
import edu.stanford.registry.server.config.SiteDao;
import edu.stanford.registry.shared.CommonUtils;
import edu.stanford.registry.shared.DateUtilsIntf;
import edu.stanford.registry.server.SiteInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

/**
 * This caches the Sites from the SurveySite table, plus the site configuration.
 */
public class SitesInfo implements Iterable<SiteInfo> {
  private static Logger logger = LoggerFactory.getLogger(SitesInfo.class.getName());

  private static SitesInfo instance;

  /**
   * The RestletServiceManager needs to iterate over the sites to route any custom actions.
   *
   * If it comes here before we're initialized by the ServiceFilter, the caller will
   * both issue errors as well.
   */
  public static  Iterable<SiteInfo> getSites(String callerClassName) {
    if (instance == null) {
      if (logger != null) {
        logger.error(callerClassName+" must not be initialized before ServiceFilter");
      }
      return null;
    }
    return instance.getAll();
  }

  /**
   * These come from the web server and can't be overridden. They take precedence.
   * The database.* properties and registry.database.* properties are omitted,
   * (except for the driver, which need not be secret) so the application code has no access to them.
   */
  private final PropertyMap globalStaticProperties; // read-only, these have precedence

  /**
   * These come from the database config table, but are overridden by the globalStaticProperties.
   */
  private Map<String,String> globalProperties; // from database, overwritten wi statics

  private final ConcurrentHashMap<Long, SiteInfo> surveySites = new ConcurrentHashMap<>();
  private int numRefreshes = 0;
  public final AppConfig appConfig;
  final String contextPath;
  final String appName;  // static property property.key.prefix, or if it doesn't exist, last part of contextPath

  Revisions revisions = new Revisions();


  public SitesInfo(String contextPath, boolean loadSurveySites, Map<String,String> globals, AppConfig appConfig) {
    this.contextPath = contextPath;
    this.globalStaticProperties = initStatics(globals);
    this.appName = setAppName();

    this.globalProperties = globalStaticProperties.getMap();
    this.appConfig = appConfig;
    logger.debug("Creating SitesInfo");
    AppConfigDao.ConfigType.logMessageWithStandardTypes();
    instance = this; // so others can iterate through the sites
  }


  PropertyMap initStatics(Map<String,String>globals) {
    if (globals == null) {
      return new PropertyMapFromHash(null);
    }

    HashMap<String,String> newMap = new HashMap<String,String>(globals.size());

    for (Entry<String, String> entry: globals.entrySet()) {
      String key = entry.getKey();
      if (key.startsWith("registry.database.") && !key.endsWith(".driver")) {
        continue;  // don't pass these to sites
      }
      if (key.startsWith("database.")) {
        continue;  // don't pass these to sites
      }

      newMap.put(key, entry.getValue());
    }
    return new PropertyMapFromHash(newMap);
  }


  /**
   * This is only for tests. When live, these will be lost when the properties are refreshed.
   * eg. call new SitesInfo(false, globals, null).addTestProperties(1L, more)
   */
  public void addTestProperties(Long siteId, HashMap<String,String> localsToAdd, HashMap<String,String> randomSets) {
    SiteInfo siteInfo = getBySiteId(siteId);
    HashMap<String,String> emails = new HashMap<String,String>(0);
    randomSets = (randomSets != null) ? randomSets : emails;
    if (siteInfo == null) {
      siteInfo = new SiteInfo(siteId, siteId.toString(), siteId.toString(), true);
      surveySites.put(siteId, siteInfo);
      siteInfo.initSiteConfig(null, this, globalProperties, localsToAdd, emails, randomSets);
    } else {
      HashMap<String, String> params = siteInfo.getProperties();
      for (Entry<String,String> entry: localsToAdd.entrySet()) {
        params.put(entry.getKey(), entry.getValue());
      }
    }
    siteInfo.initSiteConfig(null, this, globalProperties, localsToAdd, emails, randomSets);
    siteInfo.setRevisionNumber(-1); // same value given to a missing appConfig site
    siteInfo.wasMarkedLeaveClear(); // Call this just for its side-effect, that it clears the mark
  }


  /**
   * @return the property.key.prefix for the web app, or the last segment of the contextPath
   */
  public String getAppName() {
    return appName;
  }


  public String getContextPath() {
    return contextPath;
  }


  private String setAppName() {
    String value = globalStaticProperties.getString("property.key.prefix");
    if (value != null && !value.isEmpty()) {
      return value;
    }
    value = contextPath;
    int ix = contextPath.lastIndexOf('/');
    while (ix > -1 && ix == contextPath.length() - 1) {  // remove any ending slashes
      value = value.substring(0, ix);
      ix = value.lastIndexOf('/');
    }
    if (ix < 0) {
      return value;
    }
    return value.substring(ix+1);
  }


  /**
   * If this has already been loaded, this updates it gently- replacing old data
   * when the new is ready.
   */
  public void refresh(Supplier<Database> dbp) {
    if (revisions.getMaxRevision() == appConfig.getLastRevision()) {
      logger.trace("SitesInfo.refresh: no reload is needed.");
      return;
    }

    ArrayList<SiteInfo> list = new SiteDao(dbp.get()).getSurveySites();

    updateAllSites(dbp, list);

    deleteSitesNotUpdated();

    numRefreshes++;
    int numGlobals = globalStaticProperties.size();
    logger.debug("SitesInfo refreshed, there are "+list.size()+" sites, with "+numGlobals+" global props");
  }


  /**
   * Side effect: updates myMap for the siteId with the appConfig revision for the site.
   * If a site is updated, the new revision number > both SitesInfo revision number and SiteInfo's.
   * @return true if the site needs updating
   */
  /**
   * Updates all the sites with any new information in the list, plus params from appConfig.
   * Each updated site is marked.
   *
   * Slight problem: if you make a property global, and remove it from sites
   * there can be a race condition where the property isn't found.
   * @param dbp
   *
   * @param listOfEmptySitesInSiteTable List of all the sites known by the database, with the config list not set.
   */
  void updateAllSites(Supplier<Database> dbp, ArrayList<SiteInfo> listOfEmptySitesNowInSiteTable) {
    // Init globals from database, overwritten by statics

    Map<String,String> globalPropsForSites = makeGlobalProps();
    HashMap<String,String> localProps = new HashMap<String,String>(0);
    HashMap<String,String> emailTemplates = new HashMap<String,String>(0);
    HashMap<String,String> randomsets = new HashMap<String,String>(0);

    // Update globals

    // Update each site if new or appConfig says it needs refreshing
    for (SiteInfo newSite: listOfEmptySitesNowInSiteTable) {
      SiteInfo old = surveySites.get(newSite.getSiteId());
      boolean siteNeedsUpdate = revisions.siteNeedsUpdateFromAppConfig(appConfig, old, newSite);

      if (old == null || !old.sameSiteStrings(newSite) || siteNeedsUpdate) {  // make a new one;
        String updateType = (old == null) ? "init" : "update";

        if (appConfig != null) { // tests may run with this null
          localProps = appConfig.forAll(newSite.getSiteId(), AppConfigDao.ConfigType.CONFIGPARAM.toString());
          emailTemplates = appConfig.forAll(newSite.getSiteId(), AppConfigDao.ConfigType.EMAILTEMPLATE.toString());
          randomsets = appConfig.forAll(newSite.getSiteId(), AppConfigDao.ConfigType.RANDOMSET.toString());
        }
        newSite.initSiteConfig(dbp, this, globalPropsForSites, localProps, emailTemplates, randomsets);
        surveySites.put(newSite.getSiteId(), newSite);

        logger.debug(String.format("SiteInfo(%d,%s).%s() is being called, num local props: %d",
            newSite.getSiteId(), updateType, newSite.getUrlParam(), localProps.size()));
      } else {
        old.mark(); // keep it
      }
    }
    if (globalProperties != globalPropsForSites) {
      globalProperties = globalPropsForSites;
      dateFormatter = new DateFormatter();  // in case the date string changed
      revisions.updateGlobal(appConfig);
    }
  }


  /**
   * @return All global database properties overwritten by static properties
   *         except database and registry.database properties.
   */
  Map<String, String> makeGlobalProps() {
    HashMap<String, String> globalProps;
    if (appConfig == null) { // for tests
      return new HashMap<String,String>();
    } else if (!revisions.globalNeedsUpdating(appConfig)) {
      return globalProperties;
    }

    globalProps = appConfig.forAll(0L, AppConfigDao.ConfigType.CONFIGPARAM.toString());

    Enumeration<String> e = globalStaticProperties.getKeys();
    while (e.hasMoreElements()) {
      String key = e.nextElement();
      if (key.startsWith("registry.database.") && !key.endsWith(".driver")) {
        continue;  // don't pass these to sites
      }
      if (key.startsWith("database.")) {
        continue;  // don't pass these to sites
      }
      globalProps.put(key, globalStaticProperties.getString(key));
    }

    if (logger.isDebugEnabled()) {
      for (String key: SiteInfo.knownSiteProps) {
        String value = globalProps.get(key);
        if (value != null) {
          logger.debug("Global Key "+key+" globalValue: "+value);
        }
      }
    }
return globalProps;
  }


  /**
   * Delete any that weren't marked, and clear the marks that had been checked
   */
  void deleteSitesNotUpdated() {
    Iterator<Entry<Long, SiteInfo>> it = surveySites.entrySet().iterator();
    while (it.hasNext()) {
      Entry<Long, SiteInfo> entry = it.next();
      if (!entry.getValue().wasMarkedLeaveClear()) { // and clears it
        it.remove();
      }
    }
  }


  /**
   * @return number of times this was loaded or updated or updated from the database.
   */
  public int numTimesRefreshed() {
    return numRefreshes;
  }


  public String getProperty(Long siteId, String key) {
    SiteInfo info = getBySiteId(siteId);
    if (info != null) {
      return info.getProperty(key);
    }
    return globalStaticProperties.getString(key);
  }


  public PropertyMap getGlobalPropertyMap() {
    return new PropertyMapFromHash(globalProperties);
  }

  public String getGlobalProperty(String key) {
    return globalProperties.get(key);
  }

  public String getGlobalProperty(String key, String dflt) {
    String value = globalProperties.get(key);
    if (value == null || value.isEmpty()) {
      return dflt;
    }
    return value;
  }

  public boolean getGlobalProperty(String key, boolean dflt) {
    return PropertyMap.getBool(getGlobalProperty(key), dflt);
  }


  public int getNumberOfSites() {
    return surveySites.size();
  }


  /**
   * This should not be cached longer than a service call.
   * If a config parameter changes, it'll become stale.
   */
  public SiteInfo getBySiteId(Long siteId) {
    if (siteId.longValue() == 0L) {
      Exception e = new Exception("Zero passed to SitesInfo.bySiteId(0)- will use 1L instead");
      logger.error("Error", e);
      siteId = 1L;
    }
    return surveySites.get(siteId);
  }


  /**
   * This should not be cached longer than a service call.
   * If a config parameter changes, it'll become stale.
   * @param urlParam i.e. the part of the URL representing a Site ?s=tj, cat, ped, etc
   * @return
   */
  public SiteInfo byUrlParam(String urlParam) {
    urlParam = urlParam == null ? null : urlParam.trim();
    for (SiteInfo site: surveySites.values()) {
      if (site.getUrlParam().equalsIgnoreCase(urlParam)) {
        return site;
      }
    }
    return null;
  }

  public Collection<SiteInfo> getAll() {
    return surveySites.values();
  }

  public void updateSurveySite(SiteInfo surveySite) {
    surveySites.put(surveySite.getSiteId(), surveySite);
  }

  @Override
  public Iterator<SiteInfo> iterator() {
    return surveySites.values().iterator();
  }

  // =================

  DateFormatter dateFormatter;  // initialized when the global config is reloaded;

  public Date parseDate(String s) {
    return dateFormatter.parseDate(s);
  }

  public String getDateString(Date date) {
    return dateFormatter.getDateString(date);
  }

  public DateUtilsIntf getDateFormatter() {
    return dateFormatter;
  }

  /**
   * This implements a global date format.
   * A global date should not be needed, but some legacy code might use it...
   *
   * In case parsing a string doesn't work, it tries the default format as well.
   * This might arise if some work was done before the global date was set.
   */
  class DateFormatter implements DateUtilsIntf {
    SimpleDateFormat globalDateTimeFormat;
    SimpleDateFormat globalDefaultDateTimeFormat = new SimpleDateFormat(CommonUtils.DTFMT);
    public static final String DATETIME_FMT = "default.dateTimeFormat";

    // Initializes the global time format (used?) whenever global config is reloaded
    DateFormatter() {
      globalDateTimeFormat = null; // null means use the global Default
      String formatStr = getGlobalProperty(DATETIME_FMT);
      if (formatStr == null) {
        return;  // leave globalDateTimeFormat null
      }
      formatStr = formatStr.trim();
      if (formatStr.isEmpty()) {
        return;  // leave globalDateTimeFormat null
      }
      if (formatStr.equals(CommonUtils.DTFMT)) {
        return;
      }

      try {
        globalDateTimeFormat = new SimpleDateFormat(formatStr);
      } catch (Exception e) {
        logger.error("The global date format from 'default.dateTimeFormat' is invalid, using: '"
                   + CommonUtils.DTFMT + "' - " + e.getMessage());
      }
    }

    public Date parseDate(String s) {
      if (s == null) {
        return null;
      }
      s = s.trim();
      if (s.isEmpty()) {
        return null;
      }

      // Use the default method if we have the default format
      if (globalDateTimeFormat == null) {
        return parseDefaultDate(s, null);
      }

      try {
        return globalDateTimeFormat.parse(s);
      } catch (Exception e) {
        return parseDefaultDate(s, e);
      }
    }

    // Only called by parseDate if there was no format or just the default format
    Date parseDefaultDate(String s, Exception e) {
      Date d = null;
      try {
        return globalDefaultDateTimeFormat.parse(s);
      } catch (Exception e2) {
        e = (e != null) ? e : e2; // set if the first had no exception
      }
      String msg = (d == null)
          ? "Parsing date '%s' was made to work, but failed with the global format property %s='%s'"
          : "Parsing date '%s' failed, global format property %s='%s'; see log";
      String pattern = getGlobalProperty(DATETIME_FMT, "<NotSet>").trim();
      logger.error(String.format(msg, s, DATETIME_FMT, pattern, e));
      return null;
    }

    @Override // for DateUtilsIntf
    public String getDateString(Date date) {
      if (globalDateTimeFormat != null) {
        return globalDateTimeFormat.format(date);
      } else {
        return globalDefaultDateTimeFormat.format(date);
      }
    }
  }


  class Revisions {
    long maxVersionOfAll = -1;       // maximum revision fetched, from AppConfig
    long lastGlobalVersion = -1;      // last time globals were fetched from AppConfig
    HashMap<Long,Long> lastVersions = // maximum revision per site, from AppConfig
        new HashMap<Long,Long>();

    long getGlobalRevision() {
      return lastGlobalVersion;
    }

    long getMaxRevision() {
      return maxVersionOfAll;
    }

    // @return the site's last revision, -1 if never initialized
    long getSitesOldVersion(Long siteId) {
      SiteInfo siteInfo = getBySiteId(siteId);
      if (siteInfo == null) {
        return -1;
      }
      return siteInfo.getRevisionNumber();
    }

    public boolean siteNeedsUpdateFromAppConfig(AppConfig config, SiteInfo oldSite, SiteInfo newSite) {
      long oldVersion = (oldSite == null) ? -2L : oldSite.getRevisionNumber();
      Long siteId = newSite.getSiteId();
      Long newVersion = config.getLastRevision(siteId);
      if (newVersion == null) {
        newVersion = Long.valueOf(-1L); // there are no configurations for this site, but new must be made
      }
      newSite.setRevisionNumber(newVersion);

      return oldVersion < newVersion.longValue();
    }

    void updateSite(Long siteId, Long newVersion) {
      if (maxVersionOfAll < newVersion.longValue()) {
        maxVersionOfAll = newVersion.longValue();
      }
      lastVersions.put(siteId, newVersion);
    }

    void updateGlobal(AppConfig config) {
      lastGlobalVersion = config.getLastRevision();
      if (maxVersionOfAll < lastGlobalVersion) {
        maxVersionOfAll = lastGlobalVersion;
      }
    }

    public boolean globalNeedsUpdating(AppConfig config) {
      if (config == null) {
        return false;
      }
      Long object = config.getLastRevision(0L);
      long configVersion = (object == null) ? -1 : object.longValue();
      if (lastGlobalVersion < configVersion) {
        if (maxVersionOfAll < configVersion) {
          maxVersionOfAll = configVersion;
        }
        return true;
      }
      return false;
    }
  }
}
