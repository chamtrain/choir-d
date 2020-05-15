/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.pain;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyComplete;
import edu.stanford.survey.server.SurveyCompleteHandler;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.github.susom.database.Sql;

/**
 * Writes the square table with the scores from questionnaires in the physical therapy module.
 * Created by tpacht on 08/01/2019.
 */

public class PhysicalTherapyCompletionHandler implements SurveyCompleteHandler {

  private final SiteInfo siteInfo;
  private static final Logger logger = LoggerFactory.getLogger(PhysicalTherapyCompletionHandler.class);


  public PhysicalTherapyCompletionHandler(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }


  @Override
  public boolean surveyCompleted(SurveyComplete survey, Supplier<Database> database) throws RuntimeException {

    PhysicalTherapyService service = new PhysicalTherapyService(siteInfo);
    Database db = database.get();
    SurveyRegistration registration = getSurveyRegistration(survey, database);
    SurveySystDao surveySystDao = new SurveySystDao(database);
    PatStudyDao patStudyDao = new PatStudyDao(db, siteInfo);
    String processName = registration.getSurveyType();
    List<Element> questionnaires = XMLFileUtils.getInstance(siteInfo).getProcessQuestionaires(processName);

    if (questionnaires != null) {
      boolean exists = db.toSelect("select 'Y' from rpt_pain_std_surveys_square where survey_site_id=? and survey_token_id=?")
          .argLong(survey.getSurveySiteId())
          .argLong(survey.getSurveyTokenId())
          .queryBooleanOrFalse();
      try {
        Sql sql = new Sql();
        int nbrArgs = 0;
        String separator;

        if (exists) {
          sql.listStart("update rpt_pain_std_surveys_square set ");
          separator = "=?, ";
        } else {
          sql.listStart("insert into rpt_pain_std_surveys_square (survey_site_id,survey_token_id")
              .argLong(survey.getSurveySiteId())
              .argLong(survey.getSurveyTokenId());
          separator = ",";
          nbrArgs += 2;
        }
        SurveyDao surveyDao = new SurveyDao(db);
        SurveyQuery query = new SurveyQuery(db, surveyDao, survey.getSurveySiteId());
        Survey s = query.surveyBySurveyTokenId(survey.getSurveyTokenId());
        SurveySystem localSurveySystem = surveySystDao.getSurveySystem("Local");

        for (Element questionnaire : questionnaires) {
          String studyShortName = questionnaire.getAttribute("xml");
          /*
            Add the regions selected for the the Physical Therapy bodymaps to the insert/update
           */
          if (studyShortName.equals("bodymap") || studyShortName.equals("bodymapPTF")) {
            Study study = surveySystDao.getStudy(localSurveySystem.getSurveySystemId(), studyShortName);
            String regionsCsv = getBodymapRegions(s, localSurveySystem, study);
            if (regionsCsv != null) {
              sql.listSeparator(separator).append(getBodyMapColumnName(studyShortName)).argString(regionsCsv);
              nbrArgs++;
            }
          }
          /*
             Get the scoreProvider from the PhysicalTherapyService for the questionnaire
             If the scoreProvider that was returned does handle this type get the score columns/values
             and add them to the insert/update SQL statement to write them to the square table
           */
          ScoreProvider scoreProvider = service.getScoreProvider(database, studyShortName);

          if (scoreProvider.acceptsSurveyName(studyShortName)) {
            String surveySystemName = questionnaire.getAttribute("type");
            String studyDescription = questionnaire.getAttribute("value");
            SurveySystem surveySystem = surveySystDao.getSurveySystem(surveySystemName);
            Study study = surveySystDao.getStudy(surveySystem.getSurveySystemId(), studyDescription);
            PatientStudy patientStudy = patStudyDao.getPatientStudy(registration.getPatientId(), study.getStudyCode(), registration.getToken(), true);
            if (patientStudy != null) {
              PatientStudyExtendedData patientStudyExtendedData = new PatientStudyExtendedData(patientStudy);
              PatientDao patientDao = new PatientDao(db, siteInfo.getSiteId(), ServerUtils.getAdminUser(db));
              patientStudyExtendedData.setPatient(patientDao.getPatient(patientStudy.getPatientId()));
              patientStudyExtendedData.setStudy(study);
              patientStudyExtendedData.setSurveySystemName(surveySystem.getSurveySystemName());
              patientStudyExtendedData.setContents(patientStudy.getContents());
              ArrayList<ChartScore> scores = scoreProvider.getScore(patientStudyExtendedData);
              if (scores.size() > 0) {
                Map<String, BigDecimal> scoresMap = scores.get(0).getScores();
                if (scoresMap != null) {
                  for (String columnName : scoresMap.keySet()) {
                    if (studyShortName.equals("bodymapPT") || studyShortName.equals("bodymapPTF")) {
                      columnName = getBodyMapColumnName(studyShortName);
                    }
                    logger.trace("adding {} column {} with value {} for survey_token_id {}", studyDescription, columnName, scoresMap.get(columnName), survey.getSurveyTokenId());
                    sql.listSeparator(separator).append(columnName).argBigDecimal(scoresMap.get(columnName));
                    nbrArgs++;
                  }
                }
              }
            }
          }
        }
        if (exists) {
          sql.listEnd("=? where survey_site_id=? and survey_token_id=?")
              .argLong(survey.getSurveySiteId())
              .argLong(survey.getSurveyTokenId());
        } else {
          sql.append(") values (?");
          while (nbrArgs-- > 1) {
            sql.append(",?");
          }
          sql.append(")");
        }
        try {
          /*
            If we found any scores, run the SQL to write them to the database
           */
          if (!exists || nbrArgs > 2) {
            db.toInsert(sql.sql()).apply(sql).insert(1);
          }
        } catch (Exception ex) {
          logger.error("Error saving scores to square table", ex);
          return false;
        }
        return true;
      } catch (Exception e) {
        throw new RuntimeException(
            "Error storing score values for completed survey_token_id " + survey.getSurveyTokenId() + " in rpt_pain_std_surveys_square", e);
      }
    }
    return false;
  }

  private String getBodymapRegions(Survey s, SurveySystem surveySystem, Study study) {

    SurveyStep step = s.answeredStepByProviderSectionQuestion(String.valueOf(surveySystem.getSurveySystemId()), String.valueOf(study.getStudyCode()), "2");
    if (step != null) {
      return step.answerRegionsCsv();
    } else {
      step = s.answeredStepByProviderSectionQuestion(String.valueOf(surveySystem.getSurveySystemId()), String.valueOf(study.getStudyCode()), "1");
      if (step != null) {
        return step.answerRegionsCsv();
      }
    }
    return null;
  }

  private String getBodyMapColumnName (String studyName) {
    if ("bodymapPT".equals(studyName)) {
      return "bodymap_pt_regions_csv";
    } else if ("bodymapPTF".equals(studyName)) {
      return "bodymap_ptf_regions_csv";
    }
    return "bodymap_regions_csv";
  }

  private SurveyRegistration getSurveyRegistration(SurveyComplete survey, Supplier<Database> database)
      throws RuntimeException {

    Database db = database.get();
    Long surveyRegId = getSurveyRegistrationId(survey.getSurveySiteId(), survey.getSurveyTokenId(), db);
    if (surveyRegId == null) {
      throw new RuntimeException("surveyRegId not found for siteId " + survey.getSurveySiteId() + " token_id "
          + survey.getSurveyTokenId());
    }
    AssessDao assessDao = new AssessDao(db, siteInfo);
    SurveyRegistration registration = assessDao.getSurveyRegistrationByRegId(surveyRegId);
    if (registration == null) {
      throw new RuntimeException("Registration not found for siteId " + survey.getSurveySiteId() + " surveyRegId " + surveyRegId);
    }
    return registration;
  }

  private Long getSurveyRegistrationId(Long surveySiteId, Long surveyTokenId, Supplier<Database> database) {
    return database.get().toSelect("select survey_reg_id from survey_registration sr, survey_token st"
        + " where sr.survey_site_id=st.survey_site_id and sr.token=st.survey_token"
        + " and st.survey_site_id=? and st.survey_token_id=?")
        .argLong(surveySiteId)
        .argLong(surveyTokenId)
        .queryLongOrNull();
  }
}
