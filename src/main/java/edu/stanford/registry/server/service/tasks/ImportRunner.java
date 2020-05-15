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

package edu.stanford.registry.server.service.tasks;

import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.utils.XchgUtils;
import static edu.stanford.registry.server.xchg.data.Constants.IMPORT_FILES_PENDING;
import static edu.stanford.registry.server.xchg.data.Constants.IMPORT_FILES_PROCESSED;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * 
 * Imports for a given importType over all the sites managed by this server.
 * This is run by the PollingThread, so it needn't implement BackgroundTaskIntf
 */
public class ImportRunner {
  private static Logger logger = Logger.getLogger(ImportRunner.class);
  final static String IMPORT_URL_KEY = "import.url";

  private String definitionName;
  private boolean sayZeroes = true;  // the first time its runs, report for each site 
  private int numSitesForWhichZeroFilesFound; // after the first time, just report the number of sites wi zero imports

  private SitesInfo sitesInfo;


  public ImportRunner(SitesInfo sitesInfo, String importType) {
    this.sitesInfo = sitesInfo;
    definitionName = importType;
    if (sitesInfo == null)
      throw new RuntimeException("SitesInfo is null!");

    for (SiteInfo siteInfo: sitesInfo)
      makeUrl(siteInfo); // output Invalid Url error right away if property is null or malformed.
  }

  /**
   * Note: It ignores sites that don't have a directory or url set.
   * The directory is site-specific only.  Any global default is ignored
   * @return null if there are duplicates, else a list with enabled sites.
   */
  ArrayList<SiteInfo> getUniqueEnabledSites(boolean verbose) {
    ArrayList<SiteInfo> consistentSites = new ArrayList<SiteInfo>(20);
    HashMap<String,SiteInfo> uniqIn = new HashMap<String,SiteInfo>(20);
    HashMap<String,SiteInfo> uniqOut = new HashMap<String,SiteInfo>(20);

    for (SiteInfo siteInfo: sitesInfo) {
      String dir = siteInfo.getPathProperty(IMPORT_FILES_PENDING, null);
      String dir2 = siteInfo.getPathProperty(IMPORT_FILES_PROCESSED, null);
      String url = getImportUrlString(siteInfo);
      if (dir == null || url == null || dir2 == null) {
        if (dir == null || url == null) {
          if (verbose)
            logger.info(siteInfo+"Import is disabled, pending directory="+dir+" url="+url);
        } else if (dir2 == null && sayZeroes) // always say- if other 2 are on, this should work
          logger.warn(siteInfo+"Import is disabled, processed directory="+dir2);
        continue;
      }
      if (!(new File(dir)).exists() && sayZeroes) {
          logger.warn(siteInfo+"Import is disabled, pending directory does not exist: "+dir);
        continue;
      }
      if (!(new File(dir2)).exists()) {
        continue;
      }

      SiteInfo b4i = uniqIn.get(dir);
      SiteInfo b4o = uniqOut.get(dir2);
      SiteInfo b4io = uniqIn.get(dir2);
      if (b4i != null || b4o != null || b4io != null || dir.equals(dir2)) {
        if (b4i != null)
          logger.error(siteInfo+"Has same Import folder as "+b4i+": "+dir);
        else
          uniqIn.put(dir, siteInfo);
        if (b4o != null)
          logger.error(siteInfo+"Has same Import output folder as "+b4o+": "+dir);
        else
          uniqOut.put(dir2, siteInfo);
        if (b4io != null)
          logger.error(siteInfo+"Has same Import output folder as input folder of: "+b4io+": "+dir);
        if (dir.equals(dir2))
          logger.error(siteInfo+"The Import input and output folders are the same: "+siteInfo+": "+dir);
        consistentSites = null;
      } else {
        if (sayZeroes) {
          logger.info(siteInfo + "Import is enabled: " + dir2);
        }
        uniqIn.put(dir, siteInfo);
        uniqOut.put(dir2, siteInfo);
        if (consistentSites != null)
          consistentSites.add(siteInfo);
      }
    }
    if (consistentSites == null)
      logger.error("Importing is aborted until site folders are unique.");
    return consistentSites;
  }

  String getImportUrlString(SiteInfo siteInfo) {
    return siteInfo.getProperty(IMPORT_URL_KEY);
  }

  URL makeUrl(SiteInfo siteInfo) {
    String importUrl = getImportUrlString(siteInfo);
    if (importUrl == null) {
      logger.error("Site "+siteInfo.getUrlParam()+": importUrl is missing from configuration, not importing files");
      return null;
    }

    StringBuilder urlString = new StringBuilder(importUrl);
    if (!importUrl.endsWith("/")) {
      urlString.append("/");
    }
    urlString.append(definitionName);
    try {
      return new URL(urlString.toString());
    } catch (MalformedURLException e) {
      logger.error("Site "+siteInfo.getUrlParam()+": Could not start. invalid url ", e);
      return null;
    }
  }

  /**
   * Looks for import files for all sites
   */
  public void run(boolean verbose) {
    ArrayList<SiteInfo> sites = getUniqueEnabledSites(verbose);
    if (sites == null) {
      logger.error("Aborting "+definitionName+" Import due to non-unique folder names");
      return;
    }

    numSitesForWhichZeroFilesFound = 0;
    for (SiteInfo siteInfo: sites)
      try {
        doImport(siteInfo);
      } catch (IOException ioe) {
        logger.error(siteInfo.getUrlParam()+": Error loading import files for " + definitionName + " " + ioe.toString(), ioe);
      }
    if (sayZeroes)
      sayZeroes = false; // only say all the directories the first time
    else if (numSitesForWhichZeroFilesFound > 0)
      logger.info(String.format("For %d sites, no import files for %s were found", numSitesForWhichZeroFilesFound, definitionName));
  }


  void doImport(SiteInfo siteInfo) throws IOException {

    XchgUtils xchgUtils = new XchgUtils(siteInfo);
    URL url = makeUrl(siteInfo);
    if (url == null)
      return; // already output a message in makeUrl

    File loadFileDir = xchgUtils.getImportFilesPendingDirectory(definitionName);

    // process pending files
    File[] pendingFiles = loadFileDir.listFiles();
    if (pendingFiles.length == 0)
      numSitesForWhichZeroFilesFound++;

    if (sayZeroes || pendingFiles.length > 0)
      logger.debug("Site #"+siteInfo.getIdString()+": "+pendingFiles.length + " " + 
                   definitionName + " files were found in pending directory " + loadFileDir.getPath());

    if (pendingFiles.length == 0)
      return;

    // Weird- this doesn't actually pass the files!
    contactTheWebService(siteInfo, url);
  }

  void contactTheWebService(SiteInfo siteInfo, URL url) {
    Timer timer = new Timer();
    try {
      logger.debug(siteInfo.getIdString() + "making urlconnection to " + url.getProtocol() + "://"
                   + url.getHost() + ":" + url.getPort() + url.getPath());

      HttpURLConnection uc = (HttpURLConnection) url.openConnection();
      uc.setRequestMethod("GET");
      uc.setRequestProperty("Content-type", "text/html; charset=utf-8");
      uc.setRequestProperty("Content-Language", "en-US");
      uc.setRequestProperty("webauth_at", "123");
      uc.setRequestProperty("X-WEBAUTH-USER", "admin");
      uc.setRequestProperty(edu.stanford.registry.shared.Constants.SITE_ID_HEADER, siteInfo.getUrlParam().toString());
      uc.setUseCaches(false);
      uc.setDoOutput(true);
      uc.setDoInput(true);
      uc.connect();

      // write parameters
      String data = "";
      java.io.OutputStream reqStream = uc.getOutputStream();
      reqStream.write(data.getBytes(StandardCharsets.UTF_8));
      reqStream.flush();
      // get the response back
      InputStreamReader isr = new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8);
      logger.debug("input stream encoding is " + isr.getEncoding());
      BufferedReader response = new BufferedReader(isr);
      String line;
      StringBuilder result = new StringBuilder();
      while ((line = response.readLine()) != null) {
        result.append(line);
      }
      response.close();
      logger.debug("RESPONSE:" + result.toString());

    } catch (Exception e) {
      logger.error("Error connection to url", e);
    }
    logger.info(siteInfo.getIdString() + "Imported "+definitionName+" in "+timer.getSeconds());
  }
}
