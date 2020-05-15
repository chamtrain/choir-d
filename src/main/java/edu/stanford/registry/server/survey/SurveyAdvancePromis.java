package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.server.SurveyQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Sql;

/**
 * Created by tpacht on 6/3/2016.
 */
public class SurveyAdvancePromis extends SurveyAdvanceBase implements SurveyToSquareIntf {
  private static final Logger log = Logger.getLogger(SurveyAdvanceGenerated.class);

  private Database db;
  public SurveyAdvancePromis(Database db, SiteInfo siteInfo) {
    super(siteInfo);
    this.db = db;
  }

  @Override
  public int addCompletedSurveyValues(Database database, Long tokenId, SurveyQuery query, Study study, String prefix,
                                      Sql sql, String separator) {
    Long surveyRegId = getSurveyRegistrationId(siteInfo.getSiteId(), tokenId, db);
    AssessDao assessDao = new AssessDao(db, siteInfo);
    SurveyRegistration registration = assessDao.getSurveyRegistrationByRegId(surveyRegId);
    if (registration == null)
      return 0;
    sql.listSeparator(separator)
    .append(columnName(study, prefix))
    .argBigDecimal(score(registration, study.getStudyDescription()));
    return 1;
  }

  @Override
  public ArrayList<String> getSurveyDocumentation(Database database, Study study, String prefix) {
    return null;
  }

  @Override
  public LinkedHashMap<String, FieldType> getSquareTableColumns(Database database, Study study, String prefix) {
    LinkedHashMap<String, FieldType> squareTableColumns = new LinkedHashMap<>();
    squareTableColumns.put(columnName(study, prefix), FieldType.number);
    return squareTableColumns;
  }

  public String getColumnName(Study study, String prefix) {
    return columnName(study, prefix);
  }

  BigDecimal score(  SurveyRegistration reg, String studyDesc) {
    ChartScore chartScore = chartScore( reg, studyDesc);
    return chartScore == null ? null : chartScore.getScore();
  }

  ChartScore chartScore(SurveyRegistration reg, String studyDesc) {
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

  private String columnName(Study study, String prefix) {
    String validName = study.getStudyDescription().toUpperCase().replaceAll("[^0-9a-zA-Z]+","");
    if (validName.contains("BANK")) {
      int inx = validName.indexOf("BANK");
      validName = validName.substring(0, inx) + validName.substring(inx+4);
    }
    if (validName.length() > (30 - prefix.length())) {
      validName = validName.substring(0, 30 - prefix.length());
    }
    return prefix + validName;
  }

}
