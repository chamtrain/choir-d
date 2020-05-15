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
package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.utils.SquareXml;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.MultiScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

/**
 * Scoring for the Pelvic Floor Impact Questionnaire survey.
 * Created by tpacht on 08/05/2019.
 */
public class PFIQScoreProvider extends ExtensibleScoreProvider {
  private static final Logger logger = LoggerFactory.getLogger(PFIQScoreProvider.class);
  private final SurveyAdvanceUtils surveyAdvanceUtils;
  private static final String PFIQ = "PFIQ";
  private static final String[] SCORE_COLUMNS = {"PFIQ_UIQ7", "PFIQ_CRAIQ7", "PFIQ_POPIQ7", "PFIQ_SCALE", "PFIQ_SCORE"};

  public PFIQScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
    surveyAdvanceUtils = new SurveyAdvanceUtils(siteInfo);
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {
    return (studyName != null && ("PFIQ".equals(studyName) || studyName.startsWith(PFIQ)));
  }

  @Override
  public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
    ArrayList<ChartScore> scores = new ArrayList<>();
    if (patientData == null) {
      return scores;
    }
    try {
      PFIQScore PFIQScore = new PFIQScore(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription());
      SquareXml squareXml = new SquareXml(dbp.get(), siteInfo, patientData.getStudyDescription(), "");
      LinkedHashMap<String, String> columns = squareXml.getColumns();
      LinkedHashMap<String, String> references = squareXml.getReferences();
      Object[] refKeys = references.keySet().toArray();
      SurveyDao surveyDao = new SurveyDao(dbp.get());
      SurveyQuery query = new SurveyQuery(dbp.get(), surveyDao, patientData.getSurveySiteId());
      Survey s = query.surveyBySurveyToken(patientData.getToken());
      int inx = 0;

      for (String columnName : columns.keySet()) {
        logger.trace("getting " + patientData.getStudyDescription() + " score for {}", columnName);
        String refKey = refKeys[inx].toString();
        String fieldId = references.get(refKey) + ":" + refKey;
        String[] parts = fieldId.split(":");

        SurveyStep step = s.answeredStepByProviderSectionQuestion(patientData.getSurveySystemId().toString(), patientData.getStudyCode().toString(),
            "Order" + parts[0]);

        if (step != null){
          // select1 questions
          Integer score = surveyAdvanceUtils.selectedFieldChoice(step, fieldId);
          if (score != null) {
            PFIQScore.setAnswer(inx, refKey, new BigDecimal(score));
          }
        }
        inx++;
      }
      scores.add(PFIQScore);
    } catch(Exception e){
          logger.error("Exception getting score for patient {} study {}", patientData.getPatientId(),
              patientData.getStudyCode(), e);
    }
    return scores;
  }

  @Override
  public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> stats, PrintStudy study,
                              ChartConfigurationOptions opts) {

    final TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(baseLineSeries);
    if (stats == null || study == null) {
      return dataset;
    }

    TimeSeries timeDataSet = new TimeSeries(PFIQ);
    for (ChartScore stat1 : stats) {
      PFIQScore score = (PFIQScore) stat1;
      ArrayList<BigDecimal> answers = score.getAnswers();
      if (answers != null) {
        Day day = new Day(score.getDate());
        try {
          timeDataSet.addOrUpdate(day, score.getScore());
        } catch (SeriesException duplicates) {
          // ignore duplicates
        }
      }
    }
    dataset.addSeries(timeDataSet);
    return dataset;
  }

  @Override
  public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient) {

    ArrayList<TableRow> tableRows = new ArrayList<>();
    for (PatientStudyExtendedData patientStudy2 : patientStudies) {
      if (patientStudy2.getStudyCode().intValue() == study.getStudyCode().intValue()) {
        ArrayList<ChartScore> scores = getScore(patientStudy2);
        for (ChartScore score : scores) {
          PFIQScore pfiqScore = (PFIQScore) score;
          if (pfiqScore.getScore().intValue() > -1) {
            TableRow ghRow = new TableRow(100);
            ghRow.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 15));
            pfiqScore.getScore(PFIQScore.UIQ7);
            ghRow.addColumn(new TableColumn(Double.toString(pfiqScore.getScore(PFIQScore.UIQ7)), 17));
            ghRow.addColumn(new TableColumn(Double.toString(pfiqScore.getScore(PFIQScore.CRAIQ7)), 17));
            ghRow.addColumn(new TableColumn(Double.toString(pfiqScore.getScore(PFIQScore.POPIQ7)), 17));
            ghRow.addColumn(new TableColumn(Double.toString(pfiqScore.getScore(PFIQScore.SCALE)), 17));
            ghRow.addColumn(new TableColumn(Double.toString(pfiqScore.getScore(PFIQScore.SUMMARY)), 17));
            tableRows.add(ghRow);
          }
        }
      }
    }
    Table table = new Table();
    if (tableRows.size() > 0) {
      table.addHeading("Pelvic Floor Impact Questionnaire (PFIQ)");
      TableRow ghHeadingRow = new TableRow(500);
      ghHeadingRow.addColumn(new TableColumn("Date", 15));
      ghHeadingRow.addColumn(new TableColumn("UIQ-7", 17));
      ghHeadingRow.addColumn(new TableColumn("CRAIQ-7", 17));
      ghHeadingRow.addColumn(new TableColumn("POPIQ-7", 17));
      ghHeadingRow.addColumn(new TableColumn("PFIQ Scale", 17));
      ghHeadingRow.addColumn(new TableColumn("PFIQ Score", 17));
      table.addRow(ghHeadingRow);
      for (TableRow tableRow: tableRows) {
        table.addRow(tableRow);
      }
    }
    return table;
  }

  @Override
  public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                               ChartConfigurationOptions opts) {
    XYPlot plot = super.getPlot(renderer, ds, studies, opts);

    @SuppressWarnings("unchecked")
    Collection<IntervalMarker> markers = plot.getRangeMarkers(Layer.BACKGROUND);

    for (IntervalMarker marker : markers ) {
      marker.setLabel(""); // remove the worse label
    }
    return plot;
  }


  static private class SurveyAdvanceUtils extends SurveyAdvanceBase {
          SurveyAdvanceUtils(SiteInfo siteInfo) {
            super(siteInfo);
          }
  }

  private class PFIQScore extends LocalScore implements MultiScore {

    static final int UIQ7 = 1;
    static final int CRAIQ7 = 2;
    static final int POPIQ7 = 3;
    static final int SCALE = 4;
    static final int SUMMARY = 5;
    final ArrayList<Answer> answers = new ArrayList<>();

    private PFIQScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    @Override
    public void setAnswer(Integer question, String ref, BigDecimal answer) {
      super.setAnswer(question, ref, answer);
      answers.add(new Answer(question, ref, answer));
    }

    @Override
    public int getNumberOfScores() {
      return 5;
    }

    @Override
    public String getTitle(int scoreNumber, String defaultTitle) {
      switch(scoreNumber) {
      case UIQ7:
        return "Urinary Impact Questionnaire (UIQ-7)";
      case CRAIQ7:
        return "Colorectal-Anal Impact questionnaire (CRAIQ-7)";
      case POPIQ7:
        return "Pelvic Organ Prolapse Impact Questionnaire (POPIQ-7)";
      case SCALE:
        return "PFIQ-7 Scale Score";
      case SUMMARY:
        return "PFIQ-7 Summary score";
      default:
        return defaultTitle;
      }
    }

    @Override
    public double getScore(int scoreNumber) {
      switch (scoreNumber) {
      case UIQ7: //  7 items under column heading "Bladder/urine"
        return getCategoryScore("bladder");
      case CRAIQ7: // 7 items under column heading "Bowel/rectum"
        return getCategoryScore("bowel");
      case POPIQ7: // 7 items under column heading "Pelvis/Vagina"
        return getCategoryScore("pelvis");
      case SCALE:
        return getScaleScore();
      default:
        return getSumScore();
      }
    }

    @Override
    public Map<String, BigDecimal> getScores() {
      HashMap<String, BigDecimal> scores = new HashMap<>();
      for (int s=1; s<=5; s++) {
        scores.put(SCORE_COLUMNS[s-1], new BigDecimal(getScore(s)));
      }
      return scores;
    }

    @Override
    public Double getPercentileScore(int scoreNumber) {
      return null;
    }
    
    @Override
    public BigDecimal getScore() {
      return new BigDecimal(getSumScore());
    }

    private double getSumScore() {
      ArrayList<BigDecimal> answers = getAnswers();
      if (answers.size() < 1) {
        return -1;
      }
      double sum = 0;
      for (BigDecimal answer : answers) {
        sum = sum + answer.doubleValue();
      }
      return sum;
    }

    private double getCategoryScore(String category) {
      double score = 0;
      if (category == null) {
        return score;
      }

      for (Answer answer : answers) {
        if (answer != null && answer.getRefId() != null) {
          if (category.equals(answer.getRefId()) || answer.getRefId().startsWith(category)) {
            score += answer.getItemScore().intValue();
          }
        }
      }
      return score;
    }

    private double getScaleScore() {
      // Scale score is = (The sum of all answers / # of answers)  * (100/3)
      ArrayList<BigDecimal> answers = getAnswers();
      if (answers.size() < 1) {
        return -1;
      }
      Float scaleScore = ((float) getSumScore()) / answers.size() * (100.0f/3.0f);
      return scaleScore.doubleValue();
    }
  }
}
