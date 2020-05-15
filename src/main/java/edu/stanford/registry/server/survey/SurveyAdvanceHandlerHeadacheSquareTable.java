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
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.client.api.FieldType;
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
 * Populate a square table of Headache survey results upon completion of each survey.
 *
 * This object may be called 
 */
public class SurveyAdvanceHandlerHeadacheSquareTable extends SurveyAdvanceBase implements SurveyAdvanceHandler {

  private static final Logger log = Logger.getLogger(SurveyAdvanceHandlerHeadacheSquareTable.class);
  private static String tableName = "rpt_headache_square";
  private static String prefix = "HEAD";
  public final HeadacheSurveyService headacheSurveyService;

  public SurveyAdvanceHandlerHeadacheSquareTable(SiteInfo siteInfo) {
    super(siteInfo);
    headacheSurveyService = new HeadacheSurveyService(siteInfo);
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
    boolean exists = db.toSelect("select 'Y' from " + tableName + " where survey_site_id=? and survey_token_id=?")
        .argLong(survey.getSurveySiteId())
        .argLong(survey.getSurveyTokenId())
        .queryBooleanOrFalse();
    try {
      Sql sql = new Sql();
      int nbrArgs = 0;
      String separator;
      if (exists) {
        sql.listStart("update " + tableName + " set ");
        separator = "=?, ";
      } else {
        sql.listStart("insert into " + tableName + " (survey_site_id,survey_token_id,")
            .argLong(survey.getSurveySiteId())
            .argLong(survey.getSurveyTokenId());
        separator = ",";
        nbrArgs += 2;
      }
      sql.listSeparator(separator).append("patient_id").argString(registration.getPatientId());
      nbrArgs++;
      // Get a query object for retrieving answers
      SurveyDao surveyDao = new SurveyDao(db);
      SurveyQuery query = new SurveyQuery(db, surveyDao, survey.getSurveySiteId());
      SurveySystDao ssdao = new SurveySystDao(dbp);
      try {
        Study study = ssdao.getStudy(headacheSurveyService.getSurveySystemId(db), headacheSurveyService.getStudyName());
        nbrArgs += headacheSurveyService.addCompletedSurveyValues(db, survey.getSurveyTokenId(), query, study, prefix, sql, separator);
      } catch (NumberFormatException ex) {
        log.error(ex.getMessage(), ex);
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
      throw new RuntimeException(
          "Error storing survey_registration_id " + surveyRegId + " in " + tableName, e);
    }
  }

  static void Usage(String err) {
    if (err != null)
        System.err.println("  ERROR: "+err);
    System.err.println("  USAGE: ... [ numericSiteId ]");
    System.err.println("    If no siteId is given, 1 (Stanford Medicine) is assumed.");
    System.err.println("    ");
    System.exit(-1);
  }

  static private Long getSiteIdFromArgs(String[] args) {
    if (args.length == 0)
      Usage("You must specify the numeric site Id");

    if (args.length > 2)
      Usage("Zero or one arguments are allowed, not "+args.length+"; they were (+"+String.join(", ", args)+")");

    try {
        return Long.valueOf(args[0]);
    } catch (NumberFormatException e) {
      Usage("Could not parse string as a number: ("+args[0]+")");
      return Long.valueOf(0); //
    }
  }

  public static void main(String[] args) {
    Long siteId = 1L;

    // initialize using -Dbuild.properties
    DatabaseProvider.fromPropertyFile("../build.properties", "registry.")
        .withSqlParameterLogging()
        .withTransactionControl()
        .transact((Supplier<Database> dbp) -> {
            ServerContext serverContext = new ServerContext(dbp);
            SiteInfo siteInfo = serverContext.getSiteInfo(siteId);

            // Drop & recreate the square table
            dbp.get().dropTableQuietly(tableName);
            Schema schema = new Schema().addTable(tableName)
                .addColumn("survey_site_id").asLong().table()
                .addColumn("survey_token_id").asLong().table()
                .addColumn("patient_id").asString(50).table()
                .addPrimaryKey(tableName, "survey_site_id", "survey_token_id").table()
                .schema();
            schema.execute(dbp.get());

            // add the columns for the questionnaire
            HeadacheSurveyService headacheSurveyService = new HeadacheSurveyService(siteInfo);
            SurveySystDao ssdao = new SurveySystDao(dbp);
            Study study = ssdao.getStudy(headacheSurveyService.getSurveySystemId(dbp.get()), headacheSurveyService.getStudyName());
            LinkedHashMap<String, FieldType> columns = headacheSurveyService.getSquareTableColumns(dbp.get(), study, prefix);


            for (final String columnName : columns.keySet()) {
              FieldType type = columns.get(columnName);

              if (FieldType.radios.equals(type)) {
                dbp.get().ddl(
                    "alter table " + tableName + "  add (" + columnName + " " + dbp.get().flavor().typeInteger()
                        + ")").execute();
              } else if (FieldType.text.equals(type)) {
                dbp.get().ddl(
                    "alter table " + tableName + " add (" + columnName + " " + dbp.get().flavor().typeStringVar(4000)
                        + ")").execute();
              } else if (FieldType.checkboxes.equals(type)) {
                dbp.get().ddl(
                    "alter table " + tableName + " add (" + columnName + " " + dbp.get().flavor().typeStringFixed(1)
                        + ")").execute();
              } else if (FieldType.number.equals(type)) {
                dbp.get().ddl(
                    "alter table " + tableName + " add (" + columnName + " " + dbp.get().flavor().typeBigDecimal(10,2)
                        + ")").execute();
              }

            }

            // Reset the handler so it will repopulate the square table
            SurveyDao surveyDao = new SurveyDao(dbp.get());
            Long siteId1 = 1L;
            SurveyAdvancePush push = surveyDao.findSurveyAdvancePush(siteId1, "headacheSquareTable");
            if (push == null) {
              push = new SurveyAdvancePush();
              push.setSurveySiteId(siteId1);
              push.setRecipientName("headacheSquareTable");
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
            SurveyAdvanceMonitor monitor = new SurveyAdvanceMonitor(siteId1, new SurveyAdvanceHandlerFactoryImpl(siteInfo));
            monitor.pollAndNotify(dbp);

        });
  }
}
