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
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyAdvance;
import edu.stanford.survey.server.SurveyAdvanceHandler;
import edu.stanford.survey.server.SurveyAdvanceMonitor;
import edu.stanford.survey.server.SurveyAdvancePush;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.Schema;
import com.github.susom.database.Sql;

/**
 * Populate a square table of Chronic Migraine survey results upon completion of each survey.
 */
public class SurveyAdvanceHandlerChronicMigraineSquareTable implements SurveyAdvanceHandler {
  private static final Logger log = Logger.getLogger(SurveyAdvanceHandlerChronicMigraineSquareTable.class);
  private HashMap<String, String> providers = new HashMap<>();
  private HashMap<String, String> studies = new HashMap<>();

  SiteInfo siteInfo;
  SurveyAdvanceHandlerChronicMigraineSquareTable(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  @Override
  public boolean surveyAdvanced(SurveyAdvance survey, Supplier<Database> dbp) { // can I move the dbp to the constructor?
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
    boolean exists = db.toSelect("select 'Y' from rpt_chronic_migraine_square where survey_site_id=? and survey_token_id=?")
        .argLong(survey.getSurveySiteId())
        .argLong(survey.getSurveyTokenId())
        .queryBooleanOrFalse();
    try {
      Sql sql = new Sql();
      int nbrArgs = 0;
      String separator;
      if (exists) {
        sql.listStart("update rpt_chronic_migraine_square set ");
        separator = "=?, ";
      } else {
        sql.listStart("insert into rpt_chronic_migraine_square (survey_site_id,survey_token_id,")
            .argLong(survey.getSurveySiteId())
            .argLong(survey.getSurveyTokenId());
        separator = ",";
        nbrArgs += 2;
      }

      // Get chronic migraine assessment answers
      SurveyDao surveyDao = new SurveyDao(db);
      SurveyQuery query = new SurveyQuery(db, surveyDao, survey.getSurveySiteId());
      Survey s = query.surveyBySurveyTokenId(survey.getSurveyTokenId());
      String surveyProvider = getProviderId(db, "ChronicMigraineSurveyService");
      String sectionId = getSectionId(db, "chronicMigraine");
      SurveyStep step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ0");
      if (step != null && step.answer() != null) {
        sql.listSeparator(separator).append("consent").argInteger(getConsentChoice(step.answer().getAnswerJson()));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ1");
      if (step != null) {
        sql.listSeparator(separator).append("num_days_headaches_in90").argString(formFieldValue(step,
                "1:3:CHRONICMIGRAINE1"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ2");
      if (step != null) {
        sql.listSeparator(separator).append("num_days_headaches_in30").argString(formFieldValue(step,
                "2:3:CHRONICMIGRAINE2"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ3");
      if (step != null) {
        sql.listSeparator(separator).append("sensitive_to_light").argString(formFieldValue(step,
                "3:3:CHRONICMIGRAINE3"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ4");
      if (step != null) {
        sql.listSeparator(separator).append("sensitive_to_sound").argString(formFieldValue(step,
                "4:3:CHRONICMIGRAINE4"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ5");
      if (step != null) {
        sql.listSeparator(separator).append("moderate_or_severe").argString(formFieldValue(step,
                "5:3:CHRONICMIGRAINE5"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ6");
      if (step != null) {
        sql.listSeparator(separator).append("nauseated").argString(formFieldValue(step,
                "6:3:CHRONICMIGRAINE6"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ7");
      if (step != null) {
        sql.listSeparator(separator).append("over_counter_meds").argString(formFieldValue(step,
                "7:3:CHRONICMIGRAINE7"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ8");
      if (step != null) {
        sql.listSeparator(separator).append("presciption_meds").argString(formFieldValue(step,
                "8:3:CHRONICMIGRAINE8"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ9");
      if (step != null) {
        sql.listSeparator(separator).append("miss_work_school").argString(formFieldValue(step,
                "9:2:HEADACHE9"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ10");
      if (step != null) {
        sql.listSeparator(separator).append("miss_family_leisure").argString(formFieldValue(step,
                "10:2:HEADACHE10"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ11");
      if (step != null) {
        sql.listSeparator(separator).append("interfere_making_plans").argString(formFieldValue(step,
                "11:2:CHRONICMIGRAINE11"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "chronicMigraineQ12");
      if (step != null) {
        sql.listSeparator(separator).append("worry_making_plans").argString(formFieldValue(step,
                "12:2:CHRONICMIGRAINE12"));
        nbrArgs++;
      }

      if ((exists && nbrArgs > 0) || (!exists && nbrArgs > 2)) {
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
      }
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Error storing survey_registration_id " + surveyRegId + " in rpt_chronic_migraine_square", e);
    }
  }

  private String formFieldValue(SurveyStep step, String fieldId) {
    if (step == null || step.answer() == null) {
      return null;
    } else {
      return step.answer().formFieldValue(fieldId);
    }
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
        .transact(dbp -> {
            SiteInfo siteInfo = new ServerContext(dbp).getSiteInfo(1L);
            Schema schema = new Schema().addTable("rpt_chronic_migraine_square")
                .addColumn("survey_site_id").asLong().table()
                .addColumn("survey_token_id").asLong().table()
                .addColumn("consent").asInteger().table()
                .addColumn("num_days_headaches_in90").asString(4000).table()
                .addColumn("num_days_headaches_in30").asString(4000).table()
                .addColumn("sensitive_to_light").asString(4000).table()
                .addColumn("sensitive_to_sound").asString(4000).table()
                .addColumn("moderate_or_severe").asString(4000).table()
                .addColumn("nauseated").asString(4000).table()
                .addColumn("over_counter_meds").asString(4000).table()
                .addColumn("presciption_meds").asString(4000).table()
                .addColumn("miss_work_school").asString(4000).table()
                .addColumn("miss_family_leisure").asString(4000).table()
                .addColumn("interfere_making_plans").asString(4000).table()
                .addColumn("worry_making_plans").asString(4000).table()
                .addPrimaryKey("rpt_chronic_migraine_square_pk", "survey_site_id", "survey_token_id").table()
                .schema();

            // Drop and re-create the square table representation for the chronic migraine study
            Database db = dbp.get();

            db.dropTableQuietly("rpt_chronic_migraine_square");
            schema.execute(db);

            // Reset the handler so it will repopulate the chronic migraine square table
            SurveyDao surveyDao = new SurveyDao(db);
            SurveyAdvancePush push = surveyDao.findSurveyAdvancePush(siteInfo.getSiteId(), "cmSquareTable");
            if (push == null) {
              push = new SurveyAdvancePush();
              push.setSurveySiteId(siteInfo.getSiteId());
              push.setRecipientName("cmSquareTable");
              push.setRecipientDisplayName("Populate rpt_chronic_migraine_square table");
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
        });
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

  private int getConsentChoice(String json) {
    if (json != null && "{\"choice\":\"Yes, ask me 12 questions and share my data\"}".equals(json)) {
      return 1;
    }
    if (json != null && "{\"choice\":\"Ask me again later\"}".equals(json)) {
      return 3;
    }
    return 2;
  }
}
