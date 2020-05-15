package edu.stanford.registry.server.shc.interventionalradiology;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.survey.SurveyAdvanceBase;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyAdvance;
import edu.stanford.survey.server.SurveyAdvanceHandler;

import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Sql;

public class IRAdvanceHandler extends SurveyAdvanceBase implements SurveyAdvanceHandler {

  private static final Logger log = Logger.getLogger(IRAdvanceHandler.class);

  private static String tableName = "rpt_ir_surveys";

  public IRAdvanceHandler(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public boolean surveyAdvanced(SurveyAdvance surveyAdvance, Supplier<Database> dbp) {
    Database db = dbp.get();
    boolean exists = db.toSelect("select 'Y' from " + tableName + " where survey_site_id=? and survey_token_id=?")
        .argLong(surveyAdvance.getSurveySiteId())
        .argLong(surveyAdvance.getSurveyTokenId())
        .queryBooleanOrFalse();

    try {
      SurveyRegistration surveyRegistration = getSurveyRegistration(db, surveyAdvance);
      Survey s = getSurvey(db, surveyAdvance);

      Sql sql = new Sql();
      int nbrArgs = 0;
      String separator;
      if (exists) {
        sql.listStart("update " +tableName +" set ");
        separator = "=?, ";
      } else {
        sql.listStart("insert into " + tableName + " (survey_site_id,survey_token_id,patient_id,")
            .argLong(surveyAdvance.getSurveySiteId())
            .argLong(surveyAdvance.getSurveyTokenId())
            .argString(surveyRegistration.getPatientId());
        separator = ",";
        nbrArgs += 3;
      }

      sql.listSeparator(separator).append("assessment_type").argString(surveyRegistration.getSurveyType());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_scheduled").argDate(surveyRegistration.getSurveyDt());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_started").argDate(s.startTime());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_ended").argDate(s.endTime());
      nbrArgs++;
      sql.listSeparator(separator).append("is_complete").argBoolean(s.isComplete());
      nbrArgs++;

      if ((exists && nbrArgs > 0) || (!exists && nbrArgs > 3)) {
        if (exists) {
          sql.listEnd("=? where survey_site_id=? and survey_token_id=?")
              .argLong(surveyAdvance.getSurveySiteId())
              .argLong(surveyAdvance.getSurveyTokenId());
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
      throw new RuntimeException("Error storing survey registration for site " + surveyAdvance.getSurveySiteId() + " token " + surveyAdvance.getSurveyTokenId() +
          " in " + tableName, e);
    }
  }
}
