package edu.stanford.registry.server.shc.pedortho;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.reports.ScoresExportReport;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.LocalScore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.susom.database.Database;

public class PedOrthoSurveyData extends ScoresExportReport {

  public PedOrthoSurveyData(Database database, SiteInfo siteInfo) {
    super(database, siteInfo);
  }


  /**
   * Get the column definitions for the report.
   */
  @Override
  protected List<ReportColumn> getColumnDefs() {
    List<ReportColumn> columns = new ArrayList<>();
    columns.add(new PatientColumns());
    columns.add(new SurveyRegColumns());
    columns.add(new SRS30DataColumns("srs30_","srs30"));
    columns.add(new SRS30ScoreColumns("srs30_", "srs30"));
    columns.add(new AnswerColumns("", "brace", new String[] {"brace", "brace_hours"}));
    columns.add(new HealthMindsetDataColumns("HealthMindset"));
    return columns;
  }

  /**
   * Get the assessments to be included in the report.
   */
  @Override
  protected List<? extends AssessmentRegistration> getAssessments() {
    final String[] list = { "NoSurvey", "Ineligible" };
    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<AssessmentRegistration> assessments = assessDao.getAssessmentsTypeNotInSetAndStudyCountGtZ(list, 2);

    // Filter out test patients
    List<String> testPatientIds = database.toSelect("select patient_id from patient_test_only").queryStrings();
    List<AssessmentRegistration> result = new ArrayList<AssessmentRegistration>();
    for(AssessmentRegistration assessment : assessments) {
      String patientId = assessment.getPatientId();
      if (!testPatientIds.contains(patientId)) {
        result.add(assessment);
      }
    }

    return result;
  }

  /**
   * Report column definition to return the SRS30 data.
   */
  protected class SRS30DataColumns extends ScoreColumn {

    private String[] VALUE_NAMES = new String[] {
        "exam", "6months", "1month", "nervous", "shape", "activity",
        "clothes", "dumps", "restpain", "workactivity", "trunk",
        "med", "medname", "medfreq", "house", "calm", "relationships",
        "financial", "blue", "sickdays", "goout", "attractive",
        "happy", "satisfied", "management", "selfimage", "look",
        "changefunction", "changesports", "changepain", "changeconfidence",
        "changeothers", "changeselfimage"
      };

    public SRS30DataColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc);
    }

    @Override
    public List<String> getValueNames() {
      List<String> valueNames = new ArrayList<>();
      for(int i=0; i<VALUE_NAMES.length; i++) {
        valueNames.add("srs_" + VALUE_NAMES[i]);
      }
      return valueNames;
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      LocalScore localScore = (LocalScore) chartScore;
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        for(int i=0; i<VALUE_NAMES.length; i++) {
          if (localScore.isAnswered(VALUE_NAMES[i])) {
            BigDecimal ans = localScore.getAnswer(VALUE_NAMES[i]);
            values[i] = ans;
          }
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the SRS30 scores.
   */
  protected class SRS30ScoreColumns extends ScoreColumn {

    public SRS30ScoreColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc);
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList(new String[] {
          valueName+"_function",
          valueName+"_pain",
          valueName+"_appearance",
          valueName+"_mental",
          valueName+"_satisfaction",
          valueName+"_overall",
          valueName+"_total"});
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        Map<String,BigDecimal> scores = chartScore.getScores();
        if (scores != null) {
          values[0] = scores.get("Function");
          values[1] = scores.get("Pain");
          values[2] = scores.get("Appearance");
          values[3] = scores.get("Mental");
          values[4] = scores.get("Satisfaction");
          values[5] = scores.get("Overall");
          values[6] = scores.get("Total");
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the Health Mindset data.
   */
  protected class HealthMindsetDataColumns extends ScoreColumn {

    private String[] VALUE_NAMES = new String[] {"HMS1", "HMS2", "HMS3"};
    
    public HealthMindsetDataColumns(String studyDesc) {
      super(null, studyDesc);
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList(VALUE_NAMES);
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      LocalScore localScore = (LocalScore) chartScore;
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        for(int i=0; i<VALUE_NAMES.length; i++) {
          if (localScore.isAnswered(VALUE_NAMES[i])) {
            BigDecimal ans = localScore.getAnswer(VALUE_NAMES[i]);
            values[i] = ans;
          }
        }
      }
      return Arrays.asList(values);
    }
  }

}
