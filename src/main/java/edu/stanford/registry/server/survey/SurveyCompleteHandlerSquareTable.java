/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyComplete;
import edu.stanford.survey.server.SurveyCompleteHandler;
import edu.stanford.survey.server.SurveyCompleteMonitor;
import edu.stanford.survey.server.SurveyCompletePush;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.Schema;
import com.github.susom.database.Sql;

/**
 * Populate a square table of results upon completion of each survey.
 */
public class SurveyCompleteHandlerSquareTable implements SurveyCompleteHandler {
  private static final Logger log = LoggerFactory.getLogger(SurveyCompleteHandlerSquareTable.class);
  private SiteInfo siteInfo;

  public SurveyCompleteHandlerSquareTable(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }
  @Override
  public boolean surveyCompleted(SurveyComplete survey, Supplier<Database> dbp) {
    if (!siteInfo.getSiteId().equals(survey.getSurveySiteId()))
      return false;
    Database db = dbp.get();
    AssessDao assessDao = new AssessDao(db, siteInfo);
    Long surveyRegId = assessDao.getSurveyRegIdByTokenId(survey.getSurveyTokenId());
    if (surveyRegId == null) {
      log.debug("surveyRegId not found for siteId " + survey.getSurveySiteId()
                + " token_id " + survey.getSurveyTokenId());
      return false;
    }
    SurveyRegistration registration = assessDao.getSurveyRegistrationByRegId(surveyRegId);
    if (registration == null) {
      throw new RuntimeException("no registration for siteId " + surveyRegId + " token_id " + surveyRegId);
    }
    try {
      Sql sql = new Sql();
      sql.append("insert into rpt_surveys_square (survey_site_id,survey_token_id")
          .argLong(survey.getSurveySiteId())
          .argLong(survey.getSurveyTokenId());
      int nbrArgs = 2;
      // Metadata for the assessment
      SurveyDao surveyDao = new SurveyDao(db);
      SurveyQuery query = new SurveyQuery(db, surveyDao, survey.getSurveySiteId());
      Survey s = query.surveyBySurveyTokenId(survey.getSurveyTokenId());
      sql.append(",assessment_type").argString(registration.getSurveyType());
      nbrArgs++;
      sql.append(",survey_scheduled").argDate(registration.getSurveyDt());
      nbrArgs++;
      sql.append(",survey_started").argDate(s.startTime());
      nbrArgs++;
      sql.append(",survey_ended").argDate(s.endTime());
      nbrArgs++;
      // Should migrate to tracking serverTimeMillis() and userTimeMillis(), but need to regenerate table
      sql.append(",survey_user_time_ms").argLong(s.serverTimeMillis(Integer.MAX_VALUE));
      nbrArgs++;
      // Add patient demographics
      PatientDao patientDao = new PatientDao(db, siteInfo.getSiteId(), null);
      Patient patient = patientDao.getPatient(registration.getPatientId());
      sql.append(",patient_id").argString(patient.getPatientId());
      nbrArgs++;
      sql.append(",patient_dob").argDate(patient.getDtBirth());
      nbrArgs++;
      sql.append(",patient_gender").argString(getPatientAttribute(patient, Constants.ATTRIBUTE_GENDER));
      nbrArgs++;
      sql.append(",patient_race").argString(getPatientAttribute(patient, Constants.ATTRIBUTE_RACE));
      nbrArgs++;
      sql.append(",patient_ethnicity").argString(getPatientAttribute(patient, Constants.ATTRIBUTE_ETHNICITY));
      nbrArgs++;
      // Add regular assessment answers
      SurveyStep step = s.answeredStepByProviderSectionQuestion("1000", "1000", "Order1");
      sql.append(",patient_name_self_report").argString(formFieldValue(step, "1:1:PATIENTNAME"));
      nbrArgs++;
      sql.append(",was_assisted").argString(formFieldLabel(step, "2:1:ASSISTED"));
      nbrArgs++;
      sql.append(",assisted_by").argString(formFieldLabel(step, "3:1:HELPER"));
      nbrArgs++;
      step = s.answeredStepByProviderSectionQuestion("1000", "1002", "2");
      if (step != null) {
        sql.append(",bodymap_regions_csv").argString(step.answerRegionsCsv());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion("1000", "1001", "1");
      if (step != null) {
        sql.append(",pain_intensity_worst").argInteger(step.answerNumeric());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion("1000", "1001", "2");
      if (step != null) {
        sql.append(",pain_intensity_average").argInteger(step.answerNumeric());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion("1000", "1001", "3");
      if (step != null) {
        sql.append(",pain_intensity_now").argInteger(step.answerNumeric());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion("1000", "1001", "4");
      if (step != null) {
        sql.append(",pain_intensity_least").argInteger(step.answerNumeric());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion("1000", "1015", "Order1");
      if (step != null) {
        sql.append(",pat_or_dr_questions").argString(formFieldValue(step, "1:1:QUESTIONS"));
        nbrArgs++;
      }
      // TODO Rest of follwup: HeadacheSurveyService, OpioidSurveysService, opioidRiskReworded
      // painCatastrophizingScale.xml, globalHealth.xml, research.xml
      // TODO Rest of initial: ...

      // This item bank was named "intensity" in PROMIS, but is actually "interference"
      sql.append(",promis_pain_interference").argBigDecimal(score(db, siteInfo, registration, "PROMIS Pain Intensity Bank"));
      nbrArgs++;
      sql.append(",promis_pain_behavior").argBigDecimal(score(db, siteInfo, registration, "PROMIS Pain Behavior Bank"));
      nbrArgs++;
      sql.append(",promis_physical_function").argBigDecimal(score(db, siteInfo, registration, "PROMIS Physical Function Bank"));
      nbrArgs++;
      sql.append(",promis_fatigue").argBigDecimal(score(db, siteInfo, registration, "PROMIS Fatigue Bank"));
      nbrArgs++;
      sql.append(",promis_depression").argBigDecimal(score(db, siteInfo, registration, "PROMIS Depression Bank"));
      nbrArgs++;
      sql.append(",promis_anxiety").argBigDecimal(score(db, siteInfo, registration, "PROMIS Anxiety Bank"));
      nbrArgs++;
      sql.append(",promis_sleep_disturb_v1_0").argBigDecimal(score(db, siteInfo, registration,
          "PROMIS Bank v1.0 - Sleep Disturbance"));
      nbrArgs++;
      sql.append(",promis_sleep_impair_v1_0").argBigDecimal(score(db, siteInfo, registration,
          "PROMIS Bank v1.0 - Sleep-Related Impairment"));
      nbrArgs++;
      sql.append(",promis_anger_v1_0").argBigDecimal(score(db, siteInfo, registration, "PROMIS Bank v1.0 - Anger"));
      nbrArgs++;
      sql.append(",promis_emot_support_v2_0").argBigDecimal(score(db, siteInfo, registration,
          "PROMIS Bank v2.0 - Emotional Support"));
      nbrArgs++;
      sql.append(",promis_sat_roles_act_v2_0").argBigDecimal(score(db, siteInfo, registration,
          "PROMIS Bank v2.0 - Satisfaction Roles Activities"));
      nbrArgs++;
      sql.append(",promis_social_iso_v2_0").argBigDecimal(score(db, siteInfo, registration,
          "PROMIS Bank v2.0 - Social Isolation"));
      nbrArgs++;
      sql.append(") values (?");
      while (nbrArgs-- > 1) {
        sql.append(",?");
      }
      sql.append(")");
      db.toInsert(sql.sql()).apply(sql).insert(1);
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Error storing survey_registration_id " + surveyRegId + " in rpt_surveys_square", e);
    }
  }

  BigDecimal score(Database db, SiteInfo siteInfo, SurveyRegistration reg, String studyDesc) {
    // Find the patient study for the survey registration
    PatStudyDao patStudyDao = new PatStudyDao(db, siteInfo);
    ArrayList<PatientStudyExtendedData> patStudies =
        patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(reg.getSurveyRegId(), studyDesc);
    PatientStudyExtendedData patientStudy;
    if ((patStudies == null) || (patStudies.size() == 0)) {
      // patient study not found, ignore
      patientStudy = null;
    } else if (patStudies.size() > 1) {
      // more than one patient study found
      throw new RuntimeException("More than one patient study found for survey registration " + reg.getSurveyRegId()
          + " and study " + studyDesc);
    } else {
      patientStudy = patStudies.get(0);
    }
    // Get the chart score for the patient study
    ChartScore chartScore = null;
    if (patientStudy != null) {
      ScoreProvider scoreProvider = SurveyServiceFactory.getFactory(siteInfo)
           .getScoreProvider(db, patientStudy.getSurveySystemName(), studyDesc);
      ArrayList<ChartScore> chartScores = scoreProvider.getScore(patientStudy);

      if ((chartScores == null) || (chartScores.size() == 0)) {
        log.error("ScoreProvider did not return a ChartScore for PatientStudy " + patientStudy.getPatientStudyId());
      } else if (chartScores.size() > 1) {
        log.error("ScoreProvider returned more than one ChartScore for PatientStudy "
            + patientStudy.getPatientStudyId());
      } else {
        chartScore = chartScores.get(0);
      }
    }
    return chartScore == null ? null : chartScore.getScore();
  }

  String formFieldValue(SurveyStep step, String fieldId) {
    if (step == null) {
      return null;
    } else {
      return step.answer().formFieldValue(fieldId);
    }
  }

  String formFieldLabel(SurveyStep step, String fieldId) {
    if (step == null) {
      return null;
    } else {
      String answerId = step.answer().formFieldValue(fieldId);
      String answerLabel = null;
      if (answerId != null) {
        FormFieldValue fieldValue = step.questionFormFieldValue(fieldId, answerId);
        if (fieldValue == null) {
          // Probably need to revise the container interface to provide support for logging warnings
          log.error("Could not locate question field value for fieldId: " + fieldId + " answerId: " + answerId
              + "\nanswerJson: " + step.answerJson() + "\nquestionJson: " + step.questionJson());
          answerLabel = "id --> " + answerId;
        } else {
          answerLabel = fieldValue.getLabel();
        }
      }
      return answerLabel;
    }
  }

  private String getPatientAttribute(Patient patient, String attribute) {
    PatientAttribute attr = patient.getAttribute(attribute);
    return (attr == null) ? null : attr.getDataValue();
  }

  public static void main(String[] args) {
    DatabaseProvider.fromPropertyFile("../build.properties", "registry.")
        .withSqlParameterLogging()
        .withTransactionControl()
        .transact(dbp -> {
            ServerContext serverContext = new ServerContext(dbp.get());
            SiteInfo siteInfo = serverContext.getSiteInfo(1L);
            Schema schema = new Schema().addTable("rpt_surveys_square")
                .addColumn("survey_site_id").asLong().table()
                .addColumn("survey_token_id").asLong().table()
                .addColumn("assessment_type").asString(50).table()
                .addColumn("survey_scheduled").asDate().table()
                .addColumn("survey_started").asDate().table()
                .addColumn("survey_ended").asDate().table()
                .addColumn("survey_user_time_ms").asLong().table()
                .addColumn("patient_id").asString(50).table()
                .addColumn("patient_dob").asDate().table()
                .addColumn("patient_gender").asString(250).table()
                .addColumn("patient_race").asString(250).table()
                .addColumn("patient_ethnicity").asString(250).table()
                .addColumn("patient_name_self_report").asString(4000).table()
                .addColumn("was_assisted").asString(4000).table()
                .addColumn("assisted_by").asString(4000).table()
                .addColumn("bodymap_regions_csv").asString(4000).table()
                .addColumn("pain_intensity_worst").asInteger().table()
                .addColumn("pain_intensity_average").asInteger().table()
                .addColumn("pain_intensity_now").asInteger().table()
                .addColumn("pain_intensity_least").asInteger().table()
                .addColumn("pat_or_dr_questions").asString(4000).table()
                .addColumn("promis_pain_interference").asBigDecimal(10, 5).table()
                .addColumn("promis_pain_behavior").asBigDecimal(10, 5).table()
                .addColumn("promis_physical_function").asBigDecimal(10, 5).table()
                .addColumn("promis_fatigue").asBigDecimal(10, 5).table()
                .addColumn("promis_depression").asBigDecimal(10, 5).table()
                .addColumn("promis_anxiety").asBigDecimal(10, 5).table()
                .addColumn("promis_sleep_disturb_v1_0").asBigDecimal(10, 5).table()
                .addColumn("promis_sleep_impair_v1_0").asBigDecimal(10, 5).table()
                .addColumn("promis_anger_v1_0").asBigDecimal(10, 5).table()
                .addColumn("promis_emot_support_v2_0").asBigDecimal(10, 5).table()
                .addColumn("promis_sat_roles_act_v2_0").asBigDecimal(10, 5).table()
                .addColumn("promis_social_iso_v2_0").asBigDecimal(10, 5).table()
                .addPrimaryKey("rpt_surveys_square", "survey_site_id", "survey_token_id").table()
                .schema();

//            System.out.println(schema.print(Flavor.oracle));
//            if (true) { System.exit(0); }

            // Drop and re-create the square table representation
            Database db = dbp.get();
            db.dropTableQuietly("rpt_surveys_square");
            schema.execute(db);

            // Reset the handler so it will repopulate the square table
            SurveyDao surveyDao = new SurveyDao(db);
            SurveyCompletePush push = surveyDao.findSurveyCompletePush(siteInfo.getSiteId(), "squareTable");
            if (push == null) {
              push = new SurveyCompletePush();
              push.setSurveySiteId(siteInfo.getSiteId());
              push.setRecipientName("squareTable");
              push.setRecipientDisplayName("Populate rpt_surveys_square table");
              push.setFailedCount(0L);
              push.setEnabled(true);
              surveyDao.insertSurveyCompletePush(push);
            } else {
              push.setPushedSurveySequence(null);
              push.setLastPushedTime(null);
              surveyDao.updateSurveyCompletePush(push);
            }

            new ServerUtils(".");
            SurveyCompleteHandlerFactoryImpl factory = new SurveyCompleteHandlerFactoryImpl(serverContext, siteInfo);
            SurveyCompleteMonitor monitor = new SurveyCompleteMonitor(siteInfo.getSiteId(), factory);
            monitor.pollAndNotify(dbp);
        });
  }
}
