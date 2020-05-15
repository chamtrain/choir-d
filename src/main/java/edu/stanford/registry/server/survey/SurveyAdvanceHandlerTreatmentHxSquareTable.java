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
package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.utils.TxSquareXml;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyAdvance;
import edu.stanford.survey.server.SurveyAdvanceHandler;
import edu.stanford.survey.server.SurveyAdvanceMonitor;
import edu.stanford.survey.server.SurveyAdvancePush;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.Schema;
import com.github.susom.database.Sql;
/**
 * Populate a square table of Treatment Hx survey results upon completion of each survey.
 */
public class SurveyAdvanceHandlerTreatmentHxSquareTable extends TxSurveyAdvance implements SurveyAdvanceHandler {
  private static final String tableName = "rpt_TreatmentHx_Square";
  private static final String treatmentXml[] = {  "medsADV", "medsAnxiety", "medsCAM", "medsHeadache", "medsMood", "medsMuscle",
      "medsNerve", "medsNSAID", "medsOpioid", "medsSleep", "medsOther",
      "treatmentsADV", "treatmentsCAM", "treatmentsIntervention",
      "treatmentsPsych", "treatmentsRehab","treatmentsOther",
  };
  private static final Logger log = Logger.getLogger(SurveyAdvanceHandlerTreatmentHxSquareTable.class);

  public SurveyAdvanceHandlerTreatmentHxSquareTable(SiteInfo siteInfo) {
    super(siteInfo);
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
    boolean exists = db.toSelect("select 'Y' from rpt_treatmenthx_square where survey_site_id=? and survey_token_id=?")
        .argLong(survey.getSurveySiteId())
        .argLong(survey.getSurveyTokenId())
        .queryBooleanOrFalse();
    try {
      Sql sql = new Sql();
      int nbrArgs = 0;
      String separator;
      if (exists) {
        sql.listStart("update " +tableName +" set ");
        separator = "=?, ";
      } else {
        sql.listStart("insert into rpt_TreatmentHx_Square (survey_site_id,survey_token_id,")
            .argLong(survey.getSurveySiteId())
            .argLong(survey.getSurveyTokenId());
        separator = ",";
        nbrArgs += 2;
      }
      sql.listSeparator(separator).append("patient_id").argString(registration.getPatientId());
      nbrArgs++;
      // Get treatment hx assessment answers
      SurveyDao surveyDao = new SurveyDao(db);
      SurveyQuery query = new SurveyQuery(db, surveyDao, survey.getSurveySiteId());
      Survey s = query.surveyBySurveyTokenId(survey.getSurveyTokenId());
      for (int x = 0; x < treatmentXml.length; x++) {
        try {
          nbrArgs += processStudy(db, getProviderId(db, "Local"), treatmentXml[x], s, sql, separator);
        } catch (InvalidDataElementException | NumberFormatException ex) {
          log.error(ex.getMessage(), ex);
        }
      }
      if ((exists && nbrArgs > 1) || (!exists && nbrArgs > 3)) {
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
      throw new RuntimeException("Error storing survey_registration_id " + surveyRegId + " in rpt_TreatmentHx_Square", e);
    }
  }

  public static void main(String[] args) {
    // initialize using -Dbuild.properties
    final String propertiesFile = System.getProperty("build.properties");
    DatabaseProvider.fromPropertyFile(propertiesFile, "registry.")
        .withSqlParameterLogging()
        .withTransactionControl()
        .transact((Supplier<Database> dbp) -> {
            // Initialize server utils
            Properties buildProperties = new Properties();
            try {
              FileInputStream is = new FileInputStream(propertiesFile);
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
            ServerContext serverContext = new ServerContext(dbp);
            SiteInfo siteInfo = serverContext.getSiteInfo(1L); // NOTE ONLY FOR SITE 1

            // Drop & recreate the rpt_TreatmentHx_Square table
            dbp.get().dropTableQuietly(tableName);
            Schema schema = new Schema().addTable(tableName)
                .addColumn("survey_site_id").asLong().table()
                .addColumn("survey_token_id").asLong().table()
                .addColumn("patient_id").asString(50).table()
                .addPrimaryKey(tableName, "survey_site_id", "survey_token_id").table()
                .schema();
            schema.execute(dbp.get());

            // add the columns for each questionnaire
            for (int x = 0; x < treatmentXml.length; x++) {
              TxSquareXml squareXml = new TxSquareXml(dbp.get(), siteInfo, treatmentXml[x], false);
              LinkedHashMap<String, String> columns = squareXml.getColumns();
              for (final String columnName : columns.keySet()) {
                String type = columns.get(columnName);
                if ("select1".equals(type) || "radio".equals(type)) {
                  dbp.get().ddl(
                      "alter table " +tableName +"  add (" + columnName + " " + dbp.get().flavor().typeInteger() + ")").execute();
                } else if ("input".equals(type)) {
                  dbp.get().ddl(
                      "alter table " + tableName + " add (" + columnName + " " + dbp.get().flavor().typeStringVar(4000) + ")").execute();
                } else if ("select".equals(type)) {
                  dbp.get().ddl(
                      "alter table " + tableName + " add (" + columnName + " " + dbp.get().flavor().typeStringFixed(1) + ")").execute();
                }
              }
            }

            // Reset the handler so it will re-populate the rpt_TreatmentHx_Square table
            SurveyDao surveyDao = new SurveyDao(dbp.get());
            Long siteId = 1L;
            SurveyAdvancePush push = surveyDao.findSurveyAdvancePush(siteId, "txSquareTable");
            if (push == null) {
              push = new SurveyAdvancePush();
              push.setSurveySiteId(siteId);
              push.setRecipientName("txSquareTable");
              push.setRecipientDisplayName("Populate rpt_TreatmentHx_Square table");
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

}
