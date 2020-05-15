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

import edu.stanford.registry.server.SiteInfo;

/**
 * Note that its getUrlParam uses global properties, only.
 */
public class PROMISProperties {
  // version 1
  public static String PROTOCOL = "http";
  public static String SERVER_PATH = "www.assessmentcenter.net";
  public static String BANK_PATH = "CATS/Forms/";
  public static String ASSESSMENTS_PATH = "/Assessments";
  public static String PARTICIPANTS_PATH = "CATS/Participants/";
  public static String UID_VAR = "UID";
  public static String SURVEY_SYSTEM1_NAME = "PROMIS";

  // version 2
  // public static String[] SERVER_URLS = { "",
  // "http://www.assessmentcenter.net/CATS/Forms",
  // "https://msswebtest.fsm.northwestern.edu/assessmentcenter/qa/api/2012-01/"
  // "https://www.assessmentcenter.net/ac_api/2012-01/" };
  public static String[] ASSESSMENT_PATHS = { "", "/Assessments", "Assessments/" };
  public static String[] PARTICIPANTS_PATHS = { "", "CATS/Participants/", "Participants/" };

  public static String getUrlParam(SiteInfo siteInfo, int version) {
    return siteInfo.getGlobalProperty("promis." + version + ".url");
  }

  /**
   * Makes the url string for getting the list of assessments
   *
   * @return url string
   */
  public static String getBankUrl() {
    return PROTOCOL + "://" + SERVER_PATH + "/" + BANK_PATH;
  }

  public static String getBankUrl(SiteInfo siteInfo, int version) {
    if (version == 2) {
      return // SERVER_URLS[version] + "Forms/.xml";
          getUrlParam(siteInfo, version) + "Forms/.xml";
    }
    return getBankUrl();
  }

  /**
   * Builds the url for registering for an assessment
   *
   * @param formName The assessment form name
   * @param uid      (optional) UID for registering
   * @return url string
   */
  public static String getRegisterAssessmentsUrl(SiteInfo siteInfo, int version, String formName, String uid) {
    StringBuilder strbuf = new StringBuilder();
    switch (version) {
    case 2:
      strbuf.append(getUrlParam(siteInfo, version)).append(ASSESSMENT_PATHS[version]).append(formName).append(".xml");
      break;
    default:

      strbuf.append(getBankUrl()).append(formName)
          .append(ASSESSMENTS_PATH);

      break;
    }
    return strbuf.toString();
  }

  public static String getAdministerAssessmentUrl(SiteInfo siteInfo, int version, String assessmentName) {
    StringBuilder strbuf = new StringBuilder();
    switch (version) {
    case 2:
      strbuf.append(getUrlParam(siteInfo, version)).append(PARTICIPANTS_PATHS[version]).append(assessmentName).append(".xml");
      break;
    default:
      strbuf.append(PROTOCOL).append("://");
      strbuf.append(SERVER_PATH).append("/").append(PARTICIPANTS_PATH)
          .append(assessmentName);
      break;
    }
    return strbuf.toString();
  }

  public static String getResultsUrl(SiteInfo siteInfo, int version, String assessmentOid) {
    switch (version) {
    case 2:
      return getUrlParam(siteInfo, version) + "Results/" + assessmentOid + ".xml";

    default:
      return null;
    }
  }

}
