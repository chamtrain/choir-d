/*
 * Copyright 2016-2017 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.SiteInfo;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * This just reads the sites from the site table, and can add or delete one.
 * Add is used in CreateRegistry and later we'll add a way to add() and delete()
 * sites from an admin dashboard.0
 *
 * This is rarely needed. The SitesInfo.getInstance() has everything cached.
 */
public class SiteDao {
  private Database database;

  private static Logger logger = LoggerFactory.getLogger(SiteDao.class);
  final String TABLE_NAME = "survey_site";

  private final String SELECT_ENABLED =
      "SELECT survey_site_id, url_param, display_name, enabled "
      + "FROM "+TABLE_NAME+" WHERE enabled='Y' ORDER BY survey_site_id";

  public SiteDao(Database database) {
    this.database = database;
  }

  // We only fetch all sites together- they are cached in SitesInfo
  public ArrayList<SiteInfo> getSurveySites() {
    return database.toSelect(SELECT_ENABLED).query(new MultiSiteHandler());
  }

  static class MultiSiteHandler implements RowsHandler<ArrayList<SiteInfo>> {
    @Override
    public ArrayList<SiteInfo> process(Rows rs) throws Exception {
      ArrayList<SiteInfo> list = new ArrayList<SiteInfo>(10);
      while (rs.next()) {
        list.add(makeSurveySiteFromRow(rs));
      }
      return list;
    }
  }

  static SiteInfo makeSurveySiteFromRow(Rows rs) {
    Long siteId = rs.getLongOrNull(1);
    String abbrev = rs.getStringOrNull(2);
    String display = rs.getStringOrNull(3);
    boolean enabled = rs.getBooleanOrTrue(4);
    SiteInfo result = new SiteInfo(siteId, abbrev, display, enabled);
    return result;
  }

  /**
   * Adds an enabled site to the database.  This is just used when initializing a database,
   * and by LoadConfig. Later it'll be used to add a site using the UI.
   * @return True unless there's an error (like adding a site with an existing ID.)
   */
  public boolean addSite(Long siteId, String urlParam, String displayName) {
    return addSite(siteId, urlParam, displayName, Boolean.TRUE);
  }

  /**
   * Adds a site to the database.  This is just used when initializing a database,
   * and by LoadConfig. Later it'll be used to add a site using the UI.
   * @return True unless there's an error (like adding a site with an existing ID.)
   */
  public boolean addSite(Long siteId, String urlParam, String displayName, Boolean bool) {
    if (siteId.longValue() < 1) {
       logger.error("Can not add a site < 1, #"+siteId+"/"+urlParam+", "+displayName);
    }
    try {
      database.get().toInsert("INSERT INTO "+TABLE_NAME+
          " (survey_site_id, url_param, display_name, enabled) VALUES (?,?,?,?)")
        .argLong(siteId)
        .argString(urlParam)
        .argString(displayName)
        .argBoolean(bool)
        .insert(1);
      return true;
    } catch (Throwable t) {
      logger.error("Could not add: "+siteId+", "+urlParam+", "+displayName+", 'Y'", t);
      return false;
    }
  }

  /**
   * Delete a just-created site.  It would need to do a lot of cascading
   * to remove all the initialized tables that reference a site that's in use.
   * @return true if it works, false if there was an error, like other tables reference
   * the site.
   */
  public boolean deleteSite(Long siteId) {
    int n = database.get().toDelete("DELETE FROM "+TABLE_NAME+" WHERE survey_site_id = ?")
        .argLong(siteId)
        .update();
    if (n == 0) {
      logger.warn("Failed to delete from "+TABLE_NAME+" where survey_site_id = "+siteId);
      return false;
    }
    return true;
  }
}
