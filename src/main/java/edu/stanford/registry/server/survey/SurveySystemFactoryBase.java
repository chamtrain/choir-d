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

package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.config.AppConfig;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.survey.server.SurveySystem;
import edu.stanford.survey.server.SurveySystemFactory;

import java.util.ArrayList;
import java.util.function.Supplier;

import com.github.susom.database.Database;

/**
 * Place to figure out which survey system should be used. This must currently be
 * hard-coded to match the contents of the survey_site table in the database.
 *
 * Refactored in Jan 2017 to be more similar to when it comes from the database.
 */
abstract public class SurveySystemFactoryBase implements SurveySystemFactory {

  // NONE of these are set when initialized by the SurveyServiceProxy
  protected final Supplier<Database> database;
  protected final AppConfig appConfig;
  protected final SitesInfo sitesInfo;
  private final ArrayList<SSInfo> ssinfolist = new ArrayList<>(10);
  private Long defaultSiteId;

  protected void setDefaultSite(Long defaultSiteId) {
    this.defaultSiteId = defaultSiteId;
  }

  protected void addSite(long id, String name, String alt, String proxyKey, String css, String title) {
    ssinfolist.add(new SSInfo(id, name, alt, proxyKey, css, title));
  }

  /**
   * Stores the protected database, appConfig, and sitesInfo
   * <br>Add to your constructor code like init(), commented out, below.
   * <br>If your site has a default, call setDefaultSite(defaultSiteId), too.
   *
   * Note when called by the SurveyServiceProxy, all these parameters are null.
   * It only needs to call proxyPropertyKeyFor().
   */
  public SurveySystemFactoryBase(Supplier<Database> database, AppConfig config, SitesInfo sitesInfo) {
    this.database = database;
    this.appConfig = config;
    this.sitesInfo = sitesInfo;
  }

  @Override
  public Long siteIdFor(String systemName) {
    if (systemName == null)
      return defaultSiteId;

    // If system name ends with .bldr (such as ped.bldr) then this a
    // special case for a survey builder test request. Return the id for
    // the survey builder
    if (systemName.endsWith(".bldr")) {
      systemName = "bldr";
    }

    for (SSInfo s: ssinfolist)
      if (s.siteName.equals(systemName) || s.altName.equals(systemName))
        return s.siteId;

    return null;
  }

  protected final String REG_URL_KEY = "registry.service.url";
  protected final String CHOIR_URL_KEY = "choir.service.url";
  private String nullNamePropertyKey = REG_URL_KEY;

  protected void setNullNamePropertyKey(String key) {
    nullNamePropertyKey = key;
  }

  @Override
  public String proxyPropertyKeyFor(String systemName) {
    if (systemName == null || systemName.isEmpty()) {
      return nullNamePropertyKey;
    }

    // If system name ends with .bldr (such as ped.bldr) then this a
    // special case for a survey builder test request. Use the proxy property key
    // for the base system name.
    int n = systemName.indexOf(".bldr");
    if (n > 0) {
      systemName = systemName.substring(0, n);
    }

    for (SSInfo s: ssinfolist)
      if (s.siteName.equals(systemName))
        return s.proxyPropertyKey;

    return null;
  }

  @Override
  public String getPageTitle(Long siteId) {
    for (SSInfo s: ssinfolist)
      if (s.siteId.equals(siteId))
        return s.title;

    return null;
  }

  @Override
  public String getStyleSheetName(Long siteId) {
    for (SSInfo s: ssinfolist)
      if (s.siteId.equals(siteId))
        return s.cssName;

    return null;
  }

  /**
   * This is for you to override.  Usually a switch statement for each siteId
   * <br>SiteInfo siteInfo = sitesInfo.getBySiteId(siteId);
   * <br>switch (siteId.intValue()) {
   * <br>case N: return new SurveySystem(database, appConfig, siteInfo);
   * <br>default: return null;
   * <br>}
   */
  @Override
  abstract public SurveySystem systemForSiteId(Long siteId);

  // Super-classes probably won't need this, but it's protected, not private, just in case
  public static class SSInfo {
    final Long siteId;
    final String siteName;
    final String altName;
    final String proxyPropertyKey;
    final String cssName;
    final String title;

    SSInfo(long id, String name, String alt, String proxyKey, String css, String title) {
      siteId = id;
      siteName = name;
      altName = alt;
      proxyPropertyKey = proxyKey;
      cssName = css;
      this.title = title;
    }

  }


}
