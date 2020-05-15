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
package edu.stanford.registry.server.shc.trauma;

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.SurveyAdvanceBase;
import edu.stanford.registry.server.survey.SurveyAdvanceHandlerFactoryImpl;
import edu.stanford.registry.server.survey.SurveyAdvancePromis;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.GlobalHealthScore;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyAdvance;
import edu.stanford.survey.server.SurveyAdvanceHandler;
import edu.stanford.survey.server.SurveyAdvanceMonitor;
import edu.stanford.survey.server.SurveyAdvancePush;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DbCodeTx;
import com.github.susom.database.Sql;
import com.github.susom.database.Transaction;

/**
 * Created by tpacht on 07/07/2017.
 * This adds the consent question, TQoL & Centrality scores, and the PROMIS scores to the rpt_trauma_surveys table.
 */
public class TraumaAdvanceHandler extends SurveyAdvanceBase implements SurveyAdvanceHandler {
  private static final String tableName = "rpt_trauma_surveys";
  private static final Logger log = LoggerFactory.getLogger(TraumaAdvanceHandler.class);
  // The list of questionnaires this handler processes
  private static final String[] HANDLED_PROMIS_QUESTIONNAIRES = { "PROMIS Bank v1.0 - Anger", "PROMIS Bank v1.0 - Depression",
      "PROMIS Bank v1.0 - Anxiety", "PROMIS Bank v2.0 - Satisfaction Roles Activities",
      "PROMIS Bank v1.1 - Pain Interference", "PROMIS Bank v1.0 - Self-Efficacy Manage Symptoms",
      "PROMIS Bank v1.0 - Self-Efficacy Manage Emotions", "PROMIS Bank v1.0 - Self-Efficacy Manage Soc Inter",
      "PROMIS Bank v1.0 - Self-Efficacy Manage Day Activ"};
  private static final HashMap<String, Study> promisStudiesCache = new HashMap<>();


  public TraumaAdvanceHandler(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public boolean surveyAdvanced(SurveyAdvance surveyAdvance, Supplier<Database> dbp) {
    Database db = dbp.get();
    Long surveyRegId = getSurveyRegistrationId(surveyAdvance, dbp);
    if (surveyRegId == null) {
      log.debug("surveyRegId not found for siteId " + surveyAdvance.getSurveySiteId() + " token_id "
          + surveyAdvance.getSurveyTokenId());
      return false;
    }
    AssessDao assessDao = new AssessDao(db, siteInfo);
    SurveyRegistration registration = assessDao.getSurveyRegistrationByRegId(surveyRegId);
    if (registration == null) {
      throw new RuntimeException("no registration for siteId " + surveyRegId + " token_id " + surveyRegId);
    }
    boolean exists = db.toSelect("select 'Y' from " +tableName + " where survey_site_id=? and survey_token_id=?")
        .argLong(surveyAdvance.getSurveySiteId())
        .argLong(surveyAdvance.getSurveyTokenId())
        .queryBooleanOrFalse();

    try {
      Sql sql = new Sql();
      int nbrArgs = 0;
      String separator;

      SurveyRegistration surveyRegistration = getSurveyRegistration(db, surveyAdvance);
      if (exists) {
        sql.listStart("update " + tableName + " set ");
        separator = "=?, ";
      } else {
        sql.listStart("insert into " + tableName + " (survey_site_id, survey_token_id, patient_id, ")
            .argLong(surveyAdvance.getSurveySiteId())
            .argLong(surveyAdvance.getSurveyTokenId())
            .argString(surveyRegistration.getPatientId());
        separator = ",";
        nbrArgs += 3;
      }
      // Metadata for the assessment
      Survey s = getSurvey(db, surveyAdvance);
      sql.listSeparator(separator).append("assessment_type").argString(registration.getSurveyType());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_scheduled").argDate(registration.getSurveyDt());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_started").argDate(s.startTime());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_ended").argDate(s.endTime());
      nbrArgs++;
      sql.listSeparator(separator).append("is_complete").argBoolean(s.isComplete());
      nbrArgs++;
      log.trace("getting list of questionaires for {} ", surveyRegistration.getSurveyType());
      // get the process names for the survey
      ArrayList<Element> questionnaires = XMLFileUtils.getInstance(siteInfo).getProcessQuestionaires(surveyRegistration.getSurveyType());
      if (questionnaires == null) {
        log.trace("no questionnaires");
        return true;
      }
      // include optional questionnaires
      String consentedProcess = XMLFileUtils.getInstance(siteInfo).getAttribute(surveyRegistration.getSurveyType(), "optional_questionnaires");
      if (consentedProcess != null) {
        ArrayList<Element> processList = XMLFileUtils.getInstance(siteInfo).getProcessQuestionaires(consentedProcess);
        if (processList != null) {
          questionnaires.addAll(processList);
        }
      }
      String localSurveyProvider = getProviderId(db, "Local");

      if (localSurveyProvider != null) {
        try {
          PatStudyDao patStudyDao = new PatStudyDao(db, siteInfo);
          String tQoL = getQuestionnaireStartsWith("TQoL", questionnaires);
          if (tQoL != null) {
            log.trace("appending tqol_score  ");
            sql.listSeparator(separator).append("tqol_score").argBigDecimal(getScore(db, patStudyDao, surveyRegistration, tQoL));
            nbrArgs++;
          }
          String centralityEvents = getQuestionnaireStartsWith("CentralityEvents", questionnaires);
          if (centralityEvents != null) {
            log.trace("appending ces_score   ");
            sql.listSeparator(separator).append("ces_score").argBigDecimal(getScore(db, patStudyDao, surveyRegistration, centralityEvents));
            nbrArgs++;
          }

          String globalHealth = getQuestionnaireEndsWith("GlobalHealth", questionnaires);
          if (globalHealth != null)  {
            log.trace("adding global health");
            nbrArgs += globalHealth(dbp, s, sql, separator, surveyRegistration, globalHealth);
          }
        } catch (Exception ex) {
          log.error("Could not process the local surveys for token_id " + surveyAdvance.getSurveyTokenId(), ex);
        }

      }
      String stanfordCatSurveyProvider = getProviderId(db, "StanfordCat");
      if (stanfordCatSurveyProvider != null) {
        SurveyDao surveyDao = new SurveyDao(db);
        SurveyQuery query = new SurveyQuery(db, surveyDao, surveyAdvance.getSurveySiteId());
        SurveyAdvancePromis surveyAdvancePromis = new SurveyAdvancePromis(db, siteInfo);
        SurveySystDao ssDao = new SurveySystDao(dbp);
        for (String studyDescription : HANDLED_PROMIS_QUESTIONNAIRES) {
          if (hasQuestionnaire(studyDescription, questionnaires)) {
            log.trace("processing {} with provider StanfordCat ", studyDescription);
            Study promisStudy = promisStudiesCache.get(studyDescription);
            if (promisStudy == null) {
              promisStudy = ssDao.getStudy(Integer.parseInt(stanfordCatSurveyProvider), studyDescription);
              promisStudiesCache.put(studyDescription, promisStudy);
            }
            nbrArgs += surveyAdvancePromis.addCompletedSurveyValues(db, surveyAdvance.getSurveyTokenId(), query, promisStudy, "", sql, separator);
            log.trace("after: {}   nbrArgs: {}" ,studyDescription, nbrArgs);
          }
        }
      }

      log.trace("exists: {}  nbrArgs: {}", exists, nbrArgs);
      if ((exists && nbrArgs > 0) || (!exists && nbrArgs > 3)) {
        if (exists) {
          sql.listEnd("=? where survey_site_id=? and survey_token_id=?")
              .argLong(surveyAdvance.getSurveySiteId())
              .argLong(surveyAdvance.getSurveyTokenId());
        } else {
          sql.listEnd(") values (");
          sql.appendQuestionMarks(nbrArgs);
          sql.append(")");
        }
        try {
          db.toInsert(sql.sql()).apply(sql).insert(1);
        } catch (Exception ex) {
          log.error("Error processing token_id {}", surveyAdvance.getSurveyTokenId(), ex);
          return false;
        }
      }
      return true;
    } catch (Exception e) {
      log.error("error processing local surveys ", e);
      throw new RuntimeException("Error storing XYZ survey registration for site " + surveyAdvance.getSurveySiteId() + " token " + surveyAdvance.getSurveyTokenId() +
          " in " + tableName, e);
    }
  }

  private String getQuestionnaireStartsWith(String name, ArrayList<Element> questionnaires) {
    for (Element questionnaire : questionnaires) {
      if (questionnaire.getAttribute("value").startsWith(name)) {
        return questionnaire.getAttribute("value");
      }
    }
    return null;
  }

  private String getQuestionnaireEndsWith(String name, ArrayList<Element> questionnaires) {
    for (Element questionnaire : questionnaires) {
      if (questionnaire.getAttribute("value").endsWith(name)) {
        return questionnaire.getAttribute("value");
      }
    }
    return null;
  }

  private boolean hasQuestionnaire (String name, ArrayList<Element> questionnaires) {
    for (Element questionnaire : questionnaires) {
      if (name.equals(questionnaire.getAttribute("value"))) {
        return true;
      }
    }
    return false;
  }

  private BigDecimal getScore(Database db, PatStudyDao patStudyDao, SurveyRegistration surveyRegistration, String studyDescription){
    ArrayList<PatientStudyExtendedData> patStudies = patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(
        surveyRegistration.getSurveyRegId(), studyDescription);
    if ((patStudies != null) && (patStudies.size() == 1)) {
      PatientStudyExtendedData patientStudy =  patStudies.get(0);
      ScoreProvider scoreProvider = SurveyServiceFactory.getFactory(siteInfo)
          .getScoreProvider(db, "Local", studyDescription);
      ArrayList<ChartScore> chartScores = scoreProvider.getScore(patientStudy);
      if (chartScores.size() > 0) {
        return  chartScores.get(0).getScore();
      }
    } else {
      log.trace("null or > 1 patStudies found for {} ", studyDescription);
    }
    return null;
  }

  private final static String[] ghNames = {"gh_general_health","gh_general_quality_life","gh_physical_health","gh_mental_health",
      "gh_satisfaction_social","gh_carry_out_social","gh_carry_out_physical","gh_emotional_problems","gh_fatigue", "gh_avg_pain"};

  private int globalHealth(Supplier<Database> dbp, Survey s, Sql sql, String separator, SurveyRegistration surveyRegistration, String surveyName) {
    int nbrArgs = 0;
    if (s == null) {
      log.trace("Survey is null");
      return nbrArgs;
    }
    try {
      String localSurveyProvider = getProviderId(dbp.get(), "Local");
      String sectionId = getSectionId(dbp.get(), surveyName);
      GlobalHealthScore chartScore = new GlobalHealthScore(surveyRegistration.getDtChanged(), surveyRegistration.getPatientId(),
          Integer.parseInt(sectionId), surveyName);
      for (int q = 0; q < ghNames.length; q++) {
        int order = q + 1;
        SurveyStep step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, sectionId,
            "Order" + order);
        if (step != null) {
          int ans = selectedFieldInt(step, order + ":1");
          sql.listSeparator(separator).append(ghNames[q]).argInteger(ans);
          nbrArgs++;
          chartScore.setAnswer(order, new BigDecimal(ans));
        } else {
            step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, sectionId,
                "" + order);
            if (step != null) {
              int ans = step.answerNumeric();
              sql.listSeparator(separator).append(ghNames[q]).argInteger(ans);
              nbrArgs++;
              chartScore.setAnswer(order, new BigDecimal(ans));
            }
        }
      }
      if (nbrArgs > 0) {
        sql.listSeparator(separator).append("gh_mental_score").argBigDecimal(new BigDecimal(chartScore.getMentalHealthTScore()));
          nbrArgs++;
        sql.listSeparator(separator).append("gh_physical_score").argBigDecimal(new BigDecimal(chartScore.getPhysicalHealthTScore()));
          nbrArgs++;
      }
    } catch (Exception exc) {
      log.error(exc.getMessage(), exc);
    }
    return nbrArgs;
  }

  private static final long SITE_ID_VALUE = 16L;

  private static ServerContext initServerUtilsAndGetSiteIdOf9(String propFilename) {
    // Initialize server utils
    Properties buildProperties = new Properties();
    try {
      FileInputStream is = new FileInputStream(propFilename);
      buildProperties.load(is);
      is.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    HashMap<String, String> params = new HashMap<>();
    Enumeration<?> names = buildProperties.keys();
    while (names.hasMoreElements()) {
      String key = (String) names.nextElement();
      params.put(key, buildProperties.getProperty(key));
    }
    new ServerUtils("./");
    ServerContext ctxt = new ServerContext("./", params, null, false, false, null);
    ctxt.getSitesInfo().addTestProperties(SITE_ID_VALUE, params, null);

    // If the siteId in the properties file isn't 16, complain and use 16 anyway
    String s = params.get(Constants.SITE_ID);
    if (s != null && !s.isEmpty()) {
      try {
        Long x = Long.valueOf(s);
        if (x != SITE_ID_VALUE) {
          throw new RuntimeException("Site ID in property file ("+x+") differed from expected: "+SITE_ID_VALUE);
        }
      } catch (NumberFormatException nfe) {
        log.error("Error- will use siteId="+SITE_ID_VALUE+" due to error converting '"+s+"' to number from file: "+propFilename);

      }
    }
    log.warn("No siteId property was found in build.properties- using site ID "+SITE_ID_VALUE);
    return ctxt;
  }

  public static void main(String[] args) {
    final String propertiesFile = "../build.properties";

    DatabaseProvider.fromPropertyFile(propertiesFile, "registry.")
        .withSqlParameterLogging()
        .withTransactionControl()
        .transact(new DbCodeTx() { // this seems to compile with java 1.7, so can't use a Lambda
          public void run(Supplier<Database> dbp, Transaction t)  {
            ServerContext serverContext = initServerUtilsAndGetSiteIdOf9(propertiesFile);
            SiteInfo siteInfo = serverContext.getSiteInfo(SITE_ID_VALUE);

            // Reset the handler so it will repopulate the table
            SurveyDao surveyDao = new SurveyDao(dbp.get());

            SurveyAdvancePush push = surveyDao.findSurveyAdvancePush(siteInfo.getSiteId(), "pacStdSquareTable");
            if (push == null) {
              push = new SurveyAdvancePush();
              push.setSurveySiteId(siteInfo.getSiteId());
              push.setRecipientName("traumaSurveys");
              push.setRecipientDisplayName("Populate " + tableName + " table");
              push.setFailedCount(0L);
              push.setEnabled(true);
              surveyDao.insertSurveyAdvancePush(push);
            } else {
              push.setPushedSurveySequence(null);
              push.setLastPushedTime(null);
              surveyDao.updateSurveyAdvancePush(push);
            }
            new ServerUtils(".");
            SurveyAdvanceMonitor monitor = new SurveyAdvanceMonitor(siteInfo.getSiteId(), new SurveyAdvanceHandlerFactoryImpl(siteInfo));
            monitor.pollAndNotify(dbp);
          }
        });
    }
}
