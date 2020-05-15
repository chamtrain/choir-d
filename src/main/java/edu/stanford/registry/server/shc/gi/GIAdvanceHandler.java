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
package edu.stanford.registry.server.shc.gi;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyAdvance;
import edu.stanford.survey.server.SurveyAdvanceHandler;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.Sql;

/**
 * Populate a square table of results upon completion of each survey.
 */
public class GIAdvanceHandler implements SurveyAdvanceHandler {
  private static final Logger log = LoggerFactory.getLogger(GIAdvanceHandler.class);
  private HashMap<String, String> providers = new HashMap<>();
  private HashMap<String, String> studies = new HashMap<>();
  SiteInfo siteInfo;

  public GIAdvanceHandler(SiteInfo siteInfo) {
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

    boolean exists = db.toSelect("select 'Y' from rpt_gi_surveys where survey_site_id=? and survey_token_id=?")
        .argLong(survey.getSurveySiteId())
        .argLong(survey.getSurveyTokenId())
        .queryBooleanOrFalse();

    try {
      Sql sql = new Sql();
      int nbrArgs = 0;
      String separator;
      if (exists) {
        sql.listStart("update rpt_gi_surveys set ");
        separator = "=?, ";
      } else {
        sql.listStart("insert into rpt_gi_surveys (survey_site_id,survey_token_id,")
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

      String localSurveyProvider = getProviderId(db, "Local");
      SurveyStep step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "bodymap"), "2");
      if (step != null) {
        sql.listSeparator(separator).append("bodymap_regions_csv").argString(step.answerRegionsCsv());
        nbrArgs++;
      } else {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "bodymap"), "1");
        if (step != null) {
          sql.listSeparator(separator).append("bodymap_regions_csv").argString(step.answerRegionsCsv());
          nbrArgs++;
        }
      }

      // This item bank was named "intensity" in PROMIS, but is actually "interference"
      BigDecimal promisScore = score(db, registration, "PROMIS Bank v1.1 - Pain Interference");
      if (promisScore == null || promisScore.longValue() == 0L) {
        promisScore = score(db, registration, "PROMIS Pain Intensity Bank"); // check for the old one
      }
      sql.listSeparator(separator).append("promis_pain_interference").argBigDecimal(promisScore);
      nbrArgs++;
      sql.listSeparator(separator).append("promis_pain_behavior").argBigDecimal(score(db, registration,
          "PROMIS Pain Behavior Bank"));
      nbrArgs++;
      promisScore = score(db, registration, "PROMIS Bank v1.2 - Physical Function");
      if (promisScore == null || promisScore.longValue() == 0L) {
        promisScore = score(db, registration, "PROMIS Physical Function Bank"); // check for the old one
      }
      sql.listSeparator(separator).append("promis_physical_function").argBigDecimal(promisScore);
      nbrArgs++;
      sql.listSeparator(separator).append("promis_physical_function_upe").argBigDecimal(score(db, registration,
          "PROMIS Bank v1.2 - Upper Extremity"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_physical_function_mob").argBigDecimal(score(db, registration,
          "PROMIS Bank v1.2 - Mobility"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_fatigue").argBigDecimal(score(db, registration,
          "PROMIS Fatigue Bank"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_depression").argBigDecimal(score(db, registration,
          "PROMIS Depression Bank"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_anxiety").argBigDecimal(score(db, registration,
          "PROMIS Anxiety Bank"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_sleep_disturb_v1_0").argBigDecimal(score(db, registration,
          "PROMIS Bank v1.0 - Sleep Disturbance"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_sleep_impair_v1_0").argBigDecimal(score(db, registration,
          "PROMIS Bank v1.0 - Sleep-Related Impairment"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_anger_v1_0").argBigDecimal(score(db, registration,
          "PROMIS Bank v1.0 - Anger"));
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
        log.error("Unexpected error", ex);
        return false;
      }
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Error storing survey_registration_id " + surveyRegId + " in rpt_gi_surveys", e);
    }
  }

  BigDecimal score(Database db, SurveyRegistration reg, String studyDesc) {
    ChartScore chartScore = chartScore(db, reg, studyDesc);
    return chartScore == null ? null : chartScore.getScore();
  }

  ChartScore chartScore(Database db, SurveyRegistration reg, String studyDesc) {
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

  String formFieldValue(SurveyStep step, String fieldId) {
    if (step == null || step.answer() == null) {
      return null;
    } else {
      return step.answer().formFieldValue(fieldId);
    }
  }


  String formFieldLabel(SurveyStep step, String fieldId) {
    if (step == null || step.answer() == null) {
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

  String selectedValue(SurveyStep step) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null) {
            return formFieldValue(step, field.getFieldId());
          }
        }
      }
    }
    return null;
  }
  String selectedField(SurveyStep step, String fieldId) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null) {
            if (field.getFieldId().equals(fieldId)) {
              try {
                return formFieldValue(step, field.getFieldId());
              } catch (NumberFormatException nfe) {
                log.warn("Unable to save question " + step.questionJson() + " with answer " + step.answer().getAnswerJson() + " as integer");
              }
            }
          }
        }
      }
    }
    return null;
  }
  int selectedFieldInt(SurveyStep step, String fieldId) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null) {
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

  boolean selectedFieldChoice(SurveyStep step, String fieldId, int choice) {
  if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null && field.getFieldId().equals(fieldId)) {
            List<String> choices = step.answer().formFieldValues(fieldId);
            if (choices != null){
               for (String c : choices) {
                 if (c != null && Integer.toString(choice).equals(c)) {
                   return true;
                 }
              }
            }
          }
        }
      }
    }
    return false;
  }

  int selectedFieldChoiceInt(SurveyStep step, String fieldId) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null && field.getFieldId().equals(fieldId)) {
            List<String> choices = step.answer().formFieldValues(fieldId);
            if (choices != null){
              for (String c : choices) {
                if (c != null) {
                  return Integer.parseInt(c);
                }
              }
            }
          }
        }
      }
    }
    return 0;
  }
  String selectedLabel(SurveyStep step) {
    StringBuilder responses = null;
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getChoice() != null && step.questionFormField(field.getFieldId()) != null && formFieldValue(step, field.getFieldId()) != null) {
            for (FormFieldValue value : step.questionFormField(field.getFieldId()).getValues()) {
              if (value != null && value.getId() != null && value.getId().equals(formFieldValue(step, field.getFieldId()) )) {
                if (responses != null) {
                  responses.append(",");
                } else {
                  responses = new StringBuilder();
                }
                responses.append(value.getLabel());
              }
            }
          }
        }
      }
    }
    if (responses != null) {
      return responses.toString();
    }
    return null;
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

  private String getProviderId(Database db,  String providerName) {
    if (providers.get(providerName) == null) {
      String providerId = getInternalId(db,
          "SELECT survey_system_id FROM survey_system WHERE survey_system_name = ?", providerName);
      if (providerId != null) {
        providers.put(providerName, providerId);
      }
    }
    return providers.get(providerName);
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
          new RowsHandler<String>() {
            @Override
            public String process(Rows rs) throws Exception {
              if (rs.next()) {
                return Integer.toString(rs.getIntegerOrNull());
              }
              return null;
            }
          });
  }

  private int getResearchChoice(String json) {
    if (json != null && json.startsWith("{\"choice\":\"Yes")) {
      return 1;
    }
    if (json != null && json.startsWith("{\"choice\":\"Ask")) {
      return 3;
    }
    return 2;
  }
}
