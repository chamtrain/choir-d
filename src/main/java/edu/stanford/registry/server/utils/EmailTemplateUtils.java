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

package edu.stanford.registry.server.utils;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigDao.ConfigType;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.EmailContentType;
import edu.stanford.registry.shared.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

/**
 * This just wraps knowledge about email templates.
 *
 * The most complex thing about it is the initialization of the database if it has no email
 * templates at the beginning.
 */
public class EmailTemplateUtils {
  private static final Logger logger = LoggerFactory.getLogger(EmailTemplateUtils.class);

  public EmailTemplateUtils() {
    // no state, just methods
  }

  int getNumberOfTemplates(SiteInfo siteInfo) {
    HashMap<String, String> templates = siteInfo.getEmailTemplates();
    return templates == null ? 0 : templates.size();
  }

  public String getEmailSubject(String template) {
    // If the first line in the template is Subject: <subject>
    // then use that as the subject
    if (containsSubjectLine(template)) {
      String[] lines = template.split("\n",2);
      int i = lines[0].indexOf(":");
      String subject = lines[0].substring(i+1).trim();
      return subject;
    }
    return null;
  }

  public String getEmailBody(String template) {
    // If the first line in the template is Subject: <subject>
    if (containsSubjectLine(template)) {
      // Remove the first line of the template
      template = template.replaceFirst("\\A.*\n", "");
      // Remove the next line if it is blank
      template = template.replaceFirst("\\A\\s*\n", "");
    }

    return template;
  }

  private boolean containsSubjectLine(String template) {
    // Check if the template begins with Subject:
    String[] lines = template.split("\n",2);
    if (lines.length > 0) {
      String firstLine = lines[0];
      if ( firstLine.toUpperCase().matches("\\s*SUBJECT\\s*:.*")) {
        return true;
      }
    }
    return false;
  }

  public HashMap<String,String> getTemplates(SiteInfo siteInfo) {
    return siteInfo.getEmailTemplates();
  }

  public String getTemplate(SiteInfo siteInfo, String name) {
    HashMap<String, String> templates = siteInfo.getEmailTemplates();
    return templates.get(name);
  }

  public ArrayList<String> getAllTemplateNames(SiteInfo siteInfo) {
    String missingSfx = siteInfo.getProperty(SiteInfo.MISSING_EMAIL_SFX, SiteInfo.MISSING_EMAIL_SFX_DFLT);
    HashMap<String, String> templates = siteInfo.getEmailTemplates();
    ArrayList<String> allTemplateNames = new ArrayList<String>(templates.size());
    for (String key: templates.keySet()) {
      allTemplateNames.add(key);
    }

    HashMap<String,String> referencedEmailNames = XMLFileUtils.getInstance(siteInfo).getReferencedEmailTemplateNames();
    for (Entry<String, String> name: referencedEmailNames.entrySet()) {
      if (!templates.containsKey(name.getKey())) {
        logger.warn("Undefined email template '"+name.getKey()+"' used in "+name.getValue()+" sending+"+missingSfx);
        allTemplateNames.add(name.getKey() + missingSfx);
      }
    }
    Collections.sort(allTemplateNames);
    return allTemplateNames;
  }

  public String updateTemplate(Database database, SiteInfo siteInfo, User user, String name, String value) {
    Long siteId = siteInfo.getSiteId();
    AppConfigDao appConfigDao = new AppConfigDao(database, user);
    appConfigDao.addOrEnableAppConfigEntry(siteId, AppConfigDao.ConfigType.EMAILTEMPLATE, name, value);
    siteInfo.getEmailTemplates().put(name, value);  // shouldn't need this- this should be stale momentarily
    return value;
  }

  public EmailContentType getEmailContentType(Database database, SiteInfo siteInfo, String templateName) {
    AppConfigDao appConfigDao = new AppConfigDao(database);
    AppConfigEntry entry = appConfigDao.findAppConfigEntry(siteInfo.getSiteId(), ConfigType.EMAILCONTENTTYPE.toString(), templateName );
    if (entry != null && EmailContentType.HTML.toString().equals(entry.getConfigValue())) {
      return EmailContentType.HTML;
    }
    return EmailContentType.Plain;
  }
  // ==== ==== Initialization

  /**
   * Called at startup by ServerContext.
   * Loads any new email templates from a site's resource folder into the database.
   * @param database null if this is the 2nd time, after files were uploaded to the database
   *        to get a status message, confirming they were loaded.
   * @return true if some were loaded into the database, so appConfig needs refreshing
   */
  public static boolean initTemplateUtilsForSites(Database database, SitesInfo sitesInfo) {
    boolean needsRefresh = false;
    for (SiteInfo siteInfo: sitesInfo) {
      EmailTemplateUtils utils = new EmailTemplateUtils();
      int n = siteInfo.getEmailTemplates().size();
      if (database != null) {
        needsRefresh |= utils.readTemplateFiles(database, siteInfo);
      } else {
        logger.info(siteInfo.getIdString()+" has "+n+" email templates from the database");
      }
    }
    return needsRefresh; // tells caller to refresh the config cache
  }

  private boolean readTemplateFiles(Database database, SiteInfo siteInfo) {
    try {
      TemplateReader reader = new TemplateReader(database, siteInfo);
      return reader.loadTemplates();
    } catch (Throwable t) {
      logger.error(siteInfo.getIdString()+"Problem reading email templates into the database", t);
      return false;
    }
  }

  static class TemplateReader {
    final Database database;
    final AppConfigDao appConfigDao;
    final SiteInfo siteInfo;

    TemplateReader(Database database, SiteInfo siteInfo) {
      this.database = database;
      appConfigDao = new AppConfigDao(database, ServerUtils.getAdminUser(database));
      this.siteInfo = siteInfo;
    }

    /**
     * Just loads templates found in the resource folder (default is default/email-templates) that don't
     * already exist in the database.
     * @return true if any were loaded.
     */
    private boolean loadTemplates() {
      URL dirURL = this.getClass().getClassLoader().getResource(getDefaultPath());
      if (dirURL == null) {
        return false;
      }

      URI uri = null;
      File dir = null;
      try {
        uri = dirURL.toURI();
        if (uri != null) {
          dir = new File(dirURL.toURI());
        }
      } catch (URISyntaxException e) {
        logger.error("Error loading resource for email template directory " + getDefaultPath() + "- " + e.getMessage(), e);
      }

      if (dir == null) {
        return false;
      }

      String[] fileList = dir.list();
      if (fileList == null) {
        return false;
      }

      HashMap<String, String> curList = siteInfo.getEmailTemplates();
      boolean loaded = false;
      for (String fileName : fileList) {
        if (fileName == null) { // should never happen...
          continue;
        }

        if (curList.get(fileName) != null) {
          continue; // already have it, or one that's been updated
        }
        URL fileUrl = this.getClass().getClassLoader().getResource(getDefaultPath() + fileName);
        try {
          new File(fileUrl.toURI()); // call just for the exception possibility
          String content = getTemplateFromDefaultFile(fileName);
          loaded |= addToDatabase(fileName, content);
        } catch (URISyntaxException e) {
          logger.error("Error loading resource for email template " + getDefaultPath() + fileName + "- "
                       + e.getMessage(), e);
        }
      }
      return loaded;
    }

    private boolean addToDatabase(String name, String content) {
      if (content == null || content.isEmpty()) {
        return false;
      }
      return appConfigDao.addOrEnableAppConfigEntry(
          siteInfo.getSiteId(), AppConfigDao.ConfigType.EMAILTEMPLATE, name, content);
    }

    private String getDefaultPath() {
      return siteInfo.getPathProperty("emailTemplateResource", Constants.EMAIL_TEMPLATE_DIRECTORY_DEFAULT);
    }

    String getTemplateFromDefaultFile(String name) {
      logger.debug("getting default from resource");

      try {
        InputStreamReader isr = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
            getDefaultPath() + name), StandardCharsets.UTF_8);
        BufferedReader response = new BufferedReader(isr);
        String line;
        StringBuilder contents = new StringBuilder();
        while ((line = response.readLine()) != null) {
          contents.append(line);
          contents.append("\n");
        }
        int n = contents.length();
        logger.info(String.format("%sLoaded into the database email template: %s, size=%d",
                                  siteInfo.getIdString(), name, n));
        return contents.toString();
      } catch (IOException e) {
        logger.error("Error getting email template contents for " + name + ": ", e);
      }
      return null;
    }
  } // end of inner class
}
