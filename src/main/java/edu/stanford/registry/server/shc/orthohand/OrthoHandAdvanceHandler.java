/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.orthohand;

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.shc.orthohand.PrwheScoreProvider.PrwheScore;
import edu.stanford.registry.server.survey.SurveyAdvanceHandlerFactoryImpl;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.GlobalHealthScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyAdvance;
import edu.stanford.survey.server.SurveyAdvanceHandler;
import edu.stanford.survey.server.SurveyAdvanceMonitor;
import edu.stanford.survey.server.SurveyAdvancePush;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.Schema;
import com.github.susom.database.Sql;

/**
 * Populate a square table of results upon completion of each survey.
 */
public class OrthoHandAdvanceHandler implements SurveyAdvanceHandler {
  private static final Logger log = Logger.getLogger(OrthoHandAdvanceHandler.class);
  private final HashMap<String, String> providers = new HashMap<>();
  private final HashMap<String, String> studies = new HashMap<>();
  private final SiteInfo siteInfo;

  public OrthoHandAdvanceHandler(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  @Override
  public boolean surveyAdvanced(SurveyAdvance survey, Supplier<Database> dbp) {
    Long surveyRegId = getSurveyRegistrationId(survey, dbp);
    if (surveyRegId == null) {
      log.debug("surveyRegId not found for siteId " + survey.getSurveySiteId() + " token_id "
          + survey.getSurveyTokenId());
      return false;
    }

    Database db = dbp.get();
    AssessDao assessDao = new AssessDao(db, siteInfo);
    SurveyRegistration registration = assessDao.getSurveyRegistrationByRegId(surveyRegId);
    if (registration == null) {
      throw new RuntimeException("no registration for siteId " + surveyRegId + " token_id " + surveyRegId);
    }

    boolean exists = db.toSelect("select 'Y' from rpt_hand_surveys where survey_site_id=? and survey_token_id=?")
        .argLong(survey.getSurveySiteId())
        .argLong(survey.getSurveyTokenId())
        .queryBooleanOrFalse();

    try {
      Sql sql = new Sql();
      int nbrArgs = 0;
      String separator;
      if (exists) {
        sql.listStart("update rpt_hand_surveys set ");
        separator = "=?, ";
      } else {
        sql.listStart("insert into rpt_hand_surveys (survey_site_id,survey_token_id,")
            .argLong(survey.getSurveySiteId())
            .argLong(survey.getSurveyTokenId());
        separator = ",";
        nbrArgs += 2;
      }

      // Metadata for the assessment
      SurveyDao surveyDao = new SurveyDao(db);
      SurveyQuery query = new SurveyQuery(db, surveyDao, survey.getSurveySiteId());
      Survey s = query.surveyBySurveyTokenId(survey.getSurveyTokenId());
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
      // Should migrate to tracking serverTimeMillis() and userTimeMillis(), but need to regenerate table
      sql.listSeparator(separator).append("survey_user_time_ms").argLong(s.serverTimeMillis(Integer.MAX_VALUE));
      nbrArgs++;

      // Add patient demographics
      PatientDao patientDao = new PatientDao(db, siteInfo.getSiteId(), null);
      Patient patient = patientDao.getPatient(registration.getPatientId());
      sql.listSeparator(separator).append("patient_id").argString(patient.getPatientId());
      nbrArgs++;
      sql.listSeparator(separator).append("patient_dob").argDate(patient.getDtBirth());
      nbrArgs++;
      sql.listSeparator(separator).append("patient_gender").argString(getPatientAttribute(patient,
          Constants.ATTRIBUTE_GENDER));
      nbrArgs++;
      sql.listSeparator(separator).append("patient_race").argString(getPatientAttribute(patient,
          Constants.ATTRIBUTE_RACE));
      nbrArgs++;
      sql.listSeparator(separator).append("patient_ethnicity").argString(getPatientAttribute(patient,
          Constants.ATTRIBUTE_ETHNICITY));
      nbrArgs++;

      // Add regular assessment answers
      String localSurveyProvider = getProviderId(db);
      SurveyStep step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "leftArm"), "1");
      if (step != null) {
        sql.listSeparator(separator).append("map_leftarm").argString(step.answerRegionsCsv());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "leftHand"), "1");
      if (step != null) {
        sql.listSeparator(separator).append("map_lefthand").argString(step.answerRegionsCsv());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "rightArm"), "1");
      if (step != null) {
        sql.listSeparator(separator).append("map_rightarm").argString(step.answerRegionsCsv());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "rightHand"), "1");
      if (step != null) {
        sql.listSeparator(separator).append("map_righthand").argString(step.answerRegionsCsv());
        nbrArgs++;
      }
      // PRWHE
      int nbrB4prwhe = nbrArgs;
      String[] prwhe_columns = {"", "prwhe_avg_at_rest", "prwhe_avg_repeat_move", "prwhe_avg_lift_heavy_obj",
          "prwhe_avg_at_worst", "prwhe_how_often_pain", "prwhe_turning_doorknob", "prwhe_cutting_meat",
          "prwhe_fasten_buttons", "prwhe_push_up_chair", "prwhe_carry_10_lbs","prwhe_use_bath_tissue",
          "prwhe_personal_care", "prwhe_house_work", "prwhe_job_or_everyday", "prwhe_recreational_act"};
      for (int col=1; col< (prwhe_columns.length + 1); col++) {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "prwhe"), col+"");
        if (step != null) {
          sql.listSeparator(separator).append(prwhe_columns[col]).argInteger(step.answerNumeric());
          nbrArgs++;
        }
      }

      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "prwhe"), "Order16");
      if (step != null) {
        sql.listSeparator(separator).append("prwhe_import_appearance").argInteger(selectedFieldInt(step, "16:1"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "prwhe"), "17");
      if (step != null) {
        sql.listSeparator(separator).append("prwhe_dissat_appearance").argInteger(step.answerNumeric());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "prwhe"), "Order18");
      if (step != null) {
        String comments = formFieldValue(step, "18:1:PRWHECOMMENTS");
        if (comments != null) {
          if (comments.length() > 4000) {
            comments = comments.substring(0, 3998);
          }
          sql.listSeparator(separator).append("prwhe_comments").argString(comments);
          nbrArgs++;
        }
      }
      if (nbrArgs > nbrB4prwhe) {
        ChartScore chartScore = chartScore(db, registration, "prwhe");
        if (chartScore instanceof PrwheScore) {
          Double painScore = ((PrwheScore) chartScore).getPainScore();
          Double functionScore = ((PrwheScore) chartScore).getFunctionScore();
          sql.listSeparator(separator).append("prwhe_pain_score").argInteger(painScore.intValue());
          nbrArgs++;
          sql.listSeparator(separator).append("prwhe_function_score").argInteger(functionScore.intValue());
          nbrArgs++;
        }
      }

      // qDash score
      ChartScore qScore = chartScore(db, registration, "handqDASH");
      if (qScore != null && qScore.getScore() != null) {
        sql.listSeparator(separator).append("qdash_score").argBigDecimal(qScore.getScore());
        nbrArgs++;
      }

      // Global Health
      int ghCnt = nbrArgs;
      String[] ghNames = {"",  "gh_general_health","gh_general_quality_life","gh_physical_health","gh_mental_health",
          "gh_satisfaction_social","gh_carry_out_social","gh_carry_out_physical","gh_emotional_problems","gh_fatigue"};
      for (int q=1; q<10; q++) {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "handGlobalHealth"), "Order" + q);
        if (step != null) {
          sql.listSeparator(separator).append(ghNames[q]).argInteger(selectedFieldInt(step, q + ":1"));
          nbrArgs++;
        }
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "handGlobalHealth"), "10");
      if (step != null) {
        sql.listSeparator(separator).append("gh_rate_pain").argInteger(step.answerNumeric());
        nbrArgs++;
      }
      if (ghCnt < nbrArgs) {
        ChartScore chartScore = chartScore(db, registration, "handGlobalHealth");
        if (chartScore instanceof GlobalHealthScore) {
          GlobalHealthScore ghScore = (GlobalHealthScore) chartScore;
          sql.listSeparator(separator).append("gh_mental_score").argBigDecimal(new BigDecimal(ghScore.getMentalHealthTScore()));
          nbrArgs++;
          sql.listSeparator(separator).append("gh_physical_score").argBigDecimal(new BigDecimal(ghScore.getPhysicalHealthTScore()));
          nbrArgs++;
        }
      }

      sql.listSeparator(separator).append("promis_physical_function_upe").argBigDecimal(score(db, registration,
          "PROMIS Bank v1.2 - Upper Extremity"));
      nbrArgs++;

      sql.listSeparator(separator).append("promis_depression").argBigDecimal(score(db, registration,
          "PROMIS Depression Bank"));
      nbrArgs++;

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
        db.toInsert(sql.sql()).apply(sql).insert(1);
      } catch (Exception ex) {
        log.error(ex);
        return false;
      }
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Error storing survey_registration_id " + surveyRegId + " in rpt_hand_surveys", e);
    }
  }

  private BigDecimal score(Database db, SurveyRegistration reg, String studyDesc) {
    ChartScore chartScore = chartScore(db, reg, studyDesc);
    return chartScore == null ? null : chartScore.getScore();
  }

  private ChartScore chartScore(Database db, SurveyRegistration reg, String studyDesc) {
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
    return chartScore;
  }

  private String formFieldValue(SurveyStep step, String fieldId) {
    if (step == null || step.answer() == null) {
      return null;
    } else {
      return step.answer().formFieldValue(fieldId);
    }
  }


  private int selectedFieldInt(SurveyStep step, String fieldId) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null) {
            //System.out.println("formFieldValue=" + formFieldValue(step, field.getFieldId()) );
            if (field.getFieldId().equals(fieldId)) {
              try {
                return Integer.parseInt(formFieldValue(step, field.getFieldId()));
              } catch (NumberFormatException nfe) {
                log.warn("Unable to save question " + step.questionJson() + " with answer " + step.answer().getAnswerJson() + " as integer");
              }
            }
          }
        }
      }
    }
    return 0;
  }

  private String getPatientAttribute(Patient patient, String attribute) {
    PatientAttribute attr = patient.getAttribute(attribute);
    return (attr == null) ? null : attr.getDataValue();
  }

  private Long getSurveyRegistrationId(SurveyAdvance survey, Supplier<Database> database) {
    // TABLEREF survey_registration
    return database.get().toSelect("select survey_reg_id from survey_registration sr, survey_token st"
        + " where sr.survey_site_id=st.survey_site_id and sr.token=st.survey_token"
        + " and st.survey_site_id=? and st.survey_token_id=?")
        .argLong(survey.getSurveySiteId())
        .argLong(survey.getSurveyTokenId())
        .queryLongOrNull();
  }

  public static void main(String[] args) {
    DatabaseProvider.fromPropertyFile("../build.properties", "registry.")
        .withSqlParameterLogging()
        .withTransactionControl()
        .transact((Supplier<Database> dbp) -> {
            ServerContext serverContext = new ServerContext(dbp);
            SiteInfo siteInfo = serverContext.getSiteInfo(10L);
            Long siteId = siteInfo.getSiteId();
            Schema schema = new Schema().addTable("rpt_hand_surveys")
                .addColumn("survey_site_id").asLong().table()
                .addColumn("survey_token_id").asLong().table()
                .addColumn("assessment_type").asString(50).table()
                .addColumn("survey_scheduled").asDate().table()
                .addColumn("survey_started").asDate().table()
                .addColumn("survey_ended").asDate().table()
                .addColumn("is_complete").asBoolean().table()
                .addColumn("survey_user_time_ms").asLong().table()
                .addColumn("patient_id").asString(50).table()
                .addColumn("patient_dob").asDate().table()
                .addColumn("patient_gender").asString(250).table()
                .addColumn("patient_race").asString(250).table()
                .addColumn("patient_ethnicity").asString(250).table()
                .addColumn("map_leftarm").asString(4000).table()
                .addColumn("map_lefthand").asString(4000).table()
                .addColumn("map_rightarm").asString(4000).table()
                .addColumn("map_righthand").asString(4000).table()
                .addColumn("prwhe_avg_at_rest").asInteger().table()
                .addColumn("prwhe_avg_repeat_move").asInteger().table()
                .addColumn("prwhe_avg_lift_heavy_obj").asInteger().table()
                .addColumn("prwhe_avg_at_worst").asInteger().table()
                .addColumn("prwhe_how_often_pain").asInteger().table()
                .addColumn("prwhe_turning_doorknob").asInteger().table()
                .addColumn("prwhe_cutting_meat").asInteger().table()
                .addColumn("prwhe_fasten_buttons").asString(4000).table()
                .addColumn("prwhe_push_up_chair").asInteger().table()
                .addColumn("prwhe_carry_10_lbs").asInteger().table()
                .addColumn("prwhe_use_bath_tissue").asInteger().table()
                .addColumn("prwhe_personal_care").asInteger().table()
                .addColumn("prwhe_house_work").asInteger().table()
                .addColumn("prwhe_job_or_everyday").asInteger().table()
                .addColumn("prwhe_recreational_act").asInteger().table()
                .addColumn("prwhe_import_appearance").asInteger().table()
                .addColumn("prwhe_dissat_appearance").asInteger().table()
                .addColumn("prwhe_comments").asInteger().table()
                .addColumn("prwhe_pain_score").asInteger().table()
                .addColumn("prwhe_function_score").asInteger().table()
                .addColumn("qdash_score").asInteger().table()
                .addColumn("gh_general_health").asInteger().table()
                .addColumn("gh_general_quality_life").asInteger().table()
                .addColumn("gh_physical_health").asInteger().table()
                .addColumn("gh_mental_health").asInteger().table()
                .addColumn("gh_satisfaction_social").asInteger().table()
                .addColumn("gh_carry_out_social").asInteger().table()
                .addColumn("gh_carry_out_physical").asInteger().table()
                .addColumn("gh_emotional_problems").asInteger().table()
                .addColumn("gh_fatigue").asInteger().table()
                .addColumn("gh_rate_pain").asInteger().table()
                .addColumn("gh_mental_score").asBigDecimal(10, 5).table()
                .addColumn("gh_physical_score").asBigDecimal(10, 5).table()
                .addColumn("promis_physical_function_upe").asBigDecimal(10, 5).table()
                .addColumn("promis_depression").asBigDecimal(10, 5).table()
                .addPrimaryKey("rpt_hand_surveys_pk", "survey_site_id", "survey_token_id").table()
                .schema();

//            System.out.println(schema.print(Flavor.oracle));
//            if (true) { System.exit(0); }

            // Drop and re-create the square table representation
            Database db = dbp.get();

            db.dropTableQuietly("rpt_hand_surveys");
            schema.execute(db);

            // Reset the handler so it will repopulate the square table
            SurveyDao surveyDao = new SurveyDao(db);
            SurveyAdvancePush push = surveyDao.findSurveyAdvancePush(siteId, "handSquareTable");
            if (push == null) {
              push = new SurveyAdvancePush();
              push.setSurveySiteId(siteId);
              push.setRecipientName("handSquareTable");
              push.setRecipientDisplayName("Populate rpt_hand_surveys table");
              push.setFailedCount(0L);
              push.setEnabled(true);
              surveyDao.insertSurveyAdvancePush(push);
            } else {
              push.setPushedSurveySequence(null);
              push.setLastPushedTime(null);
              surveyDao.updateSurveyAdvancePush(push);
            }

            new ServerUtils(".");
            SurveyAdvanceMonitor monitor = new SurveyAdvanceMonitor(siteId, new SurveyAdvanceHandlerFactoryImpl(siteInfo));
            monitor.pollAndNotify(dbp);
        });
  }



  private String getProviderId(Database db) {
    if (providers.get("Local") == null) {
      String providerId = getInternalId(db,
          "SELECT survey_system_id FROM survey_system WHERE survey_system_name = ?", "Local");
      if (providerId != null) {
        providers.put("Local", providerId);
      }
    }
    return providers.get("Local");
  }

  private String getSectionId(Database db,  String sectionName) {
    if (studies.get(sectionName) == null) {
      String providerId = getInternalId(db,
          "SELECT study_code FROM study WHERE study_description = ?",sectionName);
      if (providerId != null) {
        studies.put(sectionName, providerId);
      } else {
        return "";
      }
    }
    return studies.get(sectionName);
  }

  private String getInternalId(Database db, String sqlString, String name) {
      return db.toSelect(sqlString).argString(name).query(
          rs -> {
            if (rs.next()) {
              return Integer.toString(rs.getIntegerOrZero());
            }
            return null;
          });
  }

}
