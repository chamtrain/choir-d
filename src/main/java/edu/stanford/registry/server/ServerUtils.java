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

package edu.stanford.registry.server;

import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.shared.CommonUtils;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.UserIdp;

import java.io.File;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;

public class ServerUtils {

  protected String homeDirectory; // this is global (for all sites, not site-specific)

  private SimpleDateFormat xmlDateFormat = new SimpleDateFormat("MM-dd-yyyy");
  private SimpleDateFormat xmlTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  private static Logger logger = Logger.getLogger(ServerUtils.class);
  protected static ServerUtils utilsInstance; // protected so a test can override it
  static SitesInfo sitesInfo;
  static Long adminUserPrincipalId;  // cached
  static Long defaultIdpId;

  /**
   * @param realPath Use servletContext.getRealPath("/") or if no servlet, "."
   */
  public static void initialize(String realPath) {
    // Already initialized
    if (utilsInstance != null)
      return;

    new ServerUtils(realPath);
    logger.info("serverUtils have been initialized");
  }

  public ServerUtils(String realPath) {
    logger.info("ServerUtils is being initialized, home="+realPath);
    utilsInstance = this;

    homeDirectory = realPath;
    if (homeDirectory == null || homeDirectory.isEmpty())
      homeDirectory = "./";
  }

  public static ServerUtils getInstance() throws DataException {
    if (utilsInstance == null) {
      throw new DataException("ServerUtils is not initialized");
    }
    return utilsInstance;
  }

  public static boolean initialized() {
    if (utilsInstance == null) {
      return false;
    }
    return true;
  }

  /**
   * This is for the server, not site-specific.
   * @return a central application folder path
   */
  public String getHomeDirectory() {
    return homeDirectory;
  }

  public static Document convertXML(String xmlDocumentString) throws ParserConfigurationException, SAXException,
      IOException {

    logger.debug("convertXML called with " + xmlDocumentString);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    // Using factory get an instance of document builder
    DocumentBuilder db = dbf.newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(xmlDocumentString));

    return db.parse(is);
  }

  // TODO: Appears to be unused- site-specific?
  public java.util.Date parseXMLDate(String dtStr) throws ParseException {
    return xmlDateFormat.parse(dtStr);
  }

  // TODO: Used by ChronicMigraineSurveyService which is constructed with a site id.
  //       Should this be site-specific?
  public String parseXMLDate(Date dt) {
    return xmlDateFormat.format(dt);
  }

  // TODO: Appears to be unused- site-specific?
  public java.util.Date parseXMLTimeStamp(String dtStr) throws ParseException {
    return xmlTimestampFormat.parse(dtStr);
  }

  // TODO: Appears to be unused- site-specific?
  public String parseXMLTimestamp(java.util.Date dt) {
    return xmlTimestampFormat.format(dt);
  }

  /**
   * Escape a string variable. Escaping data received from the client helps to prevent cross-site script vulnerabilities.
   *
   * @param html the html string to escape
   * @return the escaped string
   */
  public static String escapeInput(String html) {
    if (html == null) {
      return null;
    }
    return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
  }

  /*
   * getParam() is now SitesInfo.getInstance().getBySiteId(siteId).getP...
   * Removed:  removeParam(), addParam() and addParameters()
   */


  public File getDirectory(String path) {
    File dir = new File(path);

    if (!dir.exists()) {
      dir = new File(homeDirectory + path);
    }

    return dir;
  }

  // This is just randomly in here...
  public static String[] getTokens(String stringValue, String fieldSep) {
    StringTokenizer st = new StringTokenizer(stringValue, fieldSep);

    String[] tokens = new String[st.countTokens()];
    int i = 0;
    while (st.hasMoreTokens()) {
      tokens[i++] = st.nextToken().trim();
    }

    return tokens;
  }

  // Package-private, just to be used by tests
  void setAdminUserId(Long idpId, Long id) {
    adminUserPrincipalId = id;
    defaultIdpId = idpId;
  }

  public void initAdminUser(Database database) {
    UserDao dao = new UserDao(database, null, null);
    UserIdp userIdp = dao.findDefaultIdp();
    if (userIdp == null) {
      logger.warn("No idp's exist in table USER_IDP! Admin user can not initialize !!");
      defaultIdpId = null;
    } else {
      defaultIdpId = userIdp.getIdpId();
    }
    adminUserPrincipalId = dao.findUserPrincipal("admin").userPrincipalId;
  }

  /**
   * Creates an admin user that looks like it's logged into the given site.
   * Calls the database just the first time.
   * <br>Assumes the "admin" user has access to all sites
   */
  private User getTheAdminUser(Database database) {
    if (adminUserPrincipalId == null) {
      initAdminUser(database);
    }

    User admin = new User(defaultIdpId, "admin", "Admin", adminUserPrincipalId, true);
    return admin;
  }

  /**
   * Used by surveys and external processes like the appointment load to obtain a logged in user
   * for methods that require one for things like writing to audit tables.
   */
  public static User getAdminUser(Database database) {
    return getInstance().getTheAdminUser(database);
  }

  public static boolean isEmpty(String str) {
    return CommonUtils.isEmpty(str);
  }

  public boolean isValidEmail(String email) {
    return email.matches(CommonUtils.REGEX_EMAIL);
  }
}
