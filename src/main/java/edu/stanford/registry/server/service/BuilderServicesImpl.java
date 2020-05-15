/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.service;

import edu.stanford.registry.server.RegistryDao;
import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.survey.SurveyUtils;
import edu.stanford.registry.server.utils.SquareXml;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.StudyContent;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.SurveyException;
import edu.stanford.survey.server.SurveyDao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

public class BuilderServicesImpl implements BuilderServices {
  private static final Logger logger = Logger.getLogger(BuilderServicesImpl.class);

  private static final Long GlobalSurveyContentSite = 0L;

  private final ServerContext context;
  private final Supplier<Database> dbp;
  private final User user;
  private final SiteInfo siteInfo;

  public BuilderServicesImpl(User usr, Supplier<Database> databaseProvider, ServerContext svrContext, SiteInfo siteInfo) {
    dbp = databaseProvider;
    user = usr;
    context = svrContext;
    this.siteInfo = siteInfo;
    checkTestSite();
  }

  @Override
  public ArrayList<StudyContent> getStudies() {

    SurveySystDao ssDao = new SurveySystDao(dbp.get());
    ArrayList<Study> studies = ssDao.getStudies();

    ArrayList<StudyContent> studyContents = new ArrayList<>();

    getStudiesFromAppConfig(studies, studyContents, false);
    getStudiesFromAppConfig(studies, studyContents, true);

    studyContents.sort(new StudyContentComparator<>());
    return studyContents;
  }

  private void getStudiesFromAppConfig(ArrayList<Study> studies, ArrayList<StudyContent> studyContents, boolean global) {
    AppConfigDao configDao = new AppConfigDao(dbp.get(), user);
    Long siteIdOr0 = global ? GlobalSurveyContentSite : siteInfo.getSiteId();
    ArrayList<AppConfigEntry> appConfigEntries = configDao.findAllAppConfigEntry(siteIdOr0, "surveycontent");

    for (AppConfigEntry entry : appConfigEntries) {
      if (entry == null || !entry.isEnabled() || entry.getConfigName() == null) {
        continue;
      }
      for (Study study : studies) {
        String studyName = study.getStudyDescription();
        if (studyName == null) {
          continue;
        }
        if (studyName.contains("@")) {
          studyName = studyName.substring(0, studyName.indexOf("@"));
        }
        if (entry.getConfigName().equalsIgnoreCase(studyName) && !alreadyExists(studyContents, studyName)) {
          StudyContent content = new StudyContent();
          content.setAppConfigId(entry.getAppConfigId());
          content.setSurveySiteId(siteIdOr0);
          content.setConfigValue(entry.getConfigValue());
          content.setEnabled(entry.isEnabled());
          content.setSurveySystemId(study.getSurveySystemId());
          content.setStudyCode(study.getStudyCode());
          content.setStudyName(studyName);
          content.setTitle(study.getTitle());
          content.setDtCreated(study.getDtCreated());
          studyContents.add(content);
          logger.debug("Found study (code) " + study.getStudyCode() + " (desc) " + study.getStudyDescription()
              + " (ssid) " + study.getSurveySystemId() + " (title) " + study.getTitle());
        }
      }
    }
  }


  private boolean alreadyExists(ArrayList<StudyContent> studyContents, String studyName) {
    for (StudyContent studyContent : studyContents) {
      if (studyName.equalsIgnoreCase(studyContent.getStudyName())) {
        return true;
      }
    }
    return false;
  }


  @Override
  public StudyContent addStudyContent(StudyContent studyContent) {

    if (studyContent.getStudyName() == null || studyContent.getStudyName().trim().isEmpty()) {
      logger.error("Cannot add study, the study name is missing!");
      return null;
    }

    RegistryDao dao = new RegistryDao(dbp);
    SurveySystDao ssDao = new SurveySystDao(dbp);
    Integer surveySystemId = ssDao.getSurveySystem("Local").getSurveySystemId();
    Study study = ssDao.getStudy(surveySystemId, studyContent.getStudyCode());
    Integer studyCode;
    if (study == null) {
      studyCode = dao.addStudy(surveySystemId.longValue(), studyContent.getStudyName(), studyContent.getTitle()).intValue();
    } else {
      studyCode = study.getStudyCode();
    }
    studyContent.setStudyCode(studyCode);
    studyContent.setSurveySystemId(surveySystemId);
    if (studyContent.getSurveySiteId() == null) {
      studyContent.setSurveySiteId(siteInfo.getSiteId());
    }
    return updateStudy(studyContent, "adding");
  }


  @Override
  public void saveStudyContent(StudyContent studyContent) {
    logger.debug("in savestudycontent");
    if (studyContent != null) {
      updateStudy(studyContent, "saving");
    }
  }


  private StudyContent updateStudy(StudyContent studyContent, String addOrSave) {
    String name = studyContent.getStudyName();
    logger.debug(siteInfo.getIdString()+addOrSave+" study '" + name + "' id " + studyContent.getAppConfigId());

    String value = studyContent.getConfigValue();
    AppConfigDao appConfigDao = new AppConfigDao(dbp.get(), user);
    appConfigDao.addOrEnableAppConfigEntry(siteInfo.getSiteId(), studyContent.getConfigType(), name, value);

    String xml = SurveyUtils.convertToXmlString(value);
    if (xml != null && !xml.isEmpty()) {  // empty when SurveyBuilder is saving just the name and prefix of a survey
      XMLFileUtils xmlUtils = XMLFileUtils.getInstance(siteInfo);
      xmlUtils.updateStudyContents(name, xml);
    }
    return studyContent;
  }


  @Override
  public void disableStudyContent(Long appConfigId, String studyName) {
    AppConfigDao configDao = new AppConfigDao(dbp.get(), user);
    configDao.disableAppConfig(appConfigId);

    // If the appConfigId belongs to an (old) global survey, clear all caches
    AppConfigEntry entry = configDao.findAppConfigEntry(appConfigId);
    XMLFileUtils xmlUtils = XMLFileUtils.getInstance(siteInfo);
    if (entry.getSurveySiteId().equals(GlobalSurveyContentSite)) {
      xmlUtils.removeStudyFromAllSiteCaches(studyName);
    } else {
      xmlUtils.updateStudyContents(studyName, null);
    }
  }

  @Override
  public String getStudyAsXml(Long appConfigId) {
    AppConfigDao configDao = new AppConfigDao(dbp.get(), user);
    AppConfigEntry appConfigEntry = configDao.findAppConfigEntry(appConfigId);
    String xmlString = "NOT FOUND!";
    if (appConfigEntry != null) {
      xmlString = SurveyUtils.convertToXmlString(appConfigEntry.getConfigValue());
    }
    return xmlString;
  }

  @Override
  public ArrayList<String> getStudyDocumentation(StudyContent studyContent, String prefix) throws SurveyException {
    prefix = prefix.toUpperCase();
    if (!prefix.endsWith("_")) {
      prefix = prefix + "_";
    }
    SquareXml squareXml = new SquareXml(dbp.get(), siteInfo, studyContent.getStudyName(), prefix, true);
    StringBuilder sb = new StringBuilder();
    String newLine = System.getProperty("line.separator");
    if (!squareXml.isValid()) {
      sb.append("Study is not valid ");
      ArrayList<String> problemLog = squareXml.getProblemLog();

      for (String text : problemLog) {
        sb.append(newLine).append(text);
      }
      throw new SurveyException(sb.toString());
    }
    ArrayList<String> squareDOc = squareXml.getDocumentationLog();

    for (String str : squareDOc) {
      System.out.println(str);
    }

    return squareXml.getDocumentationLog();
  }

  @Override
  public void updateStudyTitle(StudyContent studyContent) throws SurveyException {
    try {
      Study study = new Study(studyContent.getSurveySystemId(), studyContent.getStudyCode(), studyContent.getStudyName(), 0);
      study.setTitle(studyContent.getTitle());
      AppConfigDao dao = new AppConfigDao(dbp.get(), user);
      dao.updateStudyTitle(study);
    } catch (Exception ex) {
      throw new SurveyException("Update study title failed: " + ex.getMessage());
    }
  }

  @Override
  public String getTestSurveyPath() {
    String configSurveyPath = context.appConfig().forName(siteInfo.getSiteId(), "builder", "builder.survey.path");
    if (configSurveyPath == null) {
      configSurveyPath = "survey2";
      logger.warn("No value found in app_config for app_config_type 'builder', app_config_name 'builder.survey.path'. Using default value " + configSurveyPath);
    }
    logger.debug("SurveyBuilder testing path = " + configSurveyPath);
    return configSurveyPath;
  }

  @Override
  public boolean isTestFinished(String token) {
    ActivityDao activityDao = new ActivityDao(dbp.get(), siteInfo.getSiteId());
    ArrayList<Activity> activities = activityDao.getActivityByToken(token, Constants.ACTIVITY_COMPLETED);
    return activities != null && activities.size() > 0;
  }

  private void checkTestSite() {
    Long testSiteId = 100L;
    SurveyDao dao = new SurveyDao(dbp.get());
    Long site = dao.findSurveySiteId("bldr");
    if (site == null || site.longValue() != testSiteId.longValue()) {
      logger.error("Survey Builder Test Site is Missing!");
    }
  }

  class StudyContentComparator<T> implements Comparator<T> {
    public StudyContentComparator() {
      // Sorts by studyName
    }

    @Override
    public int compare(T o1, T o2) {
      if (o1 == null || o2 == null) {
        return 0;
      }
      StudyContent study1 = (StudyContent) o1;
      StudyContent study2 = (StudyContent) o2;
      if (study1.getStudyName() == null || study2.getStudyName() == null) {
        return 0;
      }
      return study1.getStudyName().compareTo(study2.getStudyName());
    }
  }
}
