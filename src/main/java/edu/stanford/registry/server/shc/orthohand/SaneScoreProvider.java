/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.survey.ExtensibleScoreProvider;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.SurveyAdvanceBase;
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
import java.util.function.Supplier;

import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

/**
 * Scoring for the SANE survey. Created by tpacht on 10/20/2017.
 */
public class SaneScoreProvider extends ExtensibleScoreProvider {
  private static final Logger logger = LoggerFactory.getLogger(SaneScoreProvider.class);
  public SaneScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {
    return "handSane".equals(studyName);
  }

  @Override
  public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
    ArrayList<ChartScore> scores = new ArrayList<>();
    if (patientData == null) {
      return scores;
    }
    try {
      SaneScore chartScore = new SaneScore(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription());

      SurveyDao surveyDao = new SurveyDao(dbp);
      SurveyQuery query = new SurveyQuery(dbp, surveyDao, patientData.getSurveySiteId());
      Survey s = query.surveyBySurveyToken(patientData.getToken());
      String surveyProvider = String.valueOf(patientData.getSurveySystemId());
      String sectionId = String.valueOf(patientData.getStudyCode());


      ScoreHelper helper = new ScoreHelper(siteInfo);
      Integer rightScore = getChoice(s, surveyProvider, sectionId, SaneScore.RIGHT, "RATERIGHT", helper);
      if (rightScore != null) {
        chartScore.setAnswer(SaneScore.RIGHT, rightScore);
      }
      Integer leftScore = getChoice(s, surveyProvider, sectionId, SaneScore.LEFT, "RATELEFT", helper);
      if (leftScore != null) {
        chartScore.setAnswer(SaneScore.LEFT, leftScore);
      }
      scores.add(chartScore);
    } catch (Exception e) {
      logger.error("Exception getting score for patient {} study {}", patientData.getPatientId(),
          patientData.getStudyCode(), e);
    }

    return scores;
  }

  private Integer getChoice(Survey s, String surveyProvider, String sectionId, int qId, String ref, ScoreHelper helper) {
    SurveyStep step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "Order" + qId);
    if (step != null) {
      String fieldId = qId + ":1:" + ref;
      Integer choice = helper.selectedFieldChoice(step, fieldId);
      logger.debug("choice was for fieldId {} is {}", fieldId, choice);
      return choice;
    }
    return null;
  }
  public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> stats, PrintStudy study,
                              ChartConfigurationOptions opts) {

    final TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(baseLineSeries);
    if (stats == null || study == null) {
      return dataset;
    }

    // commented out because they don't want graphing (for now)
    /*
    TimeSeries timeDataSetRight = new TimeSeries("RIGHT");
    TimeSeries timeDataSetLeft = new TimeSeries("LEFT");
    for (ChartScore stat1 : stats) {
      SaneScore score = (SaneScore) stat1;
      ArrayList<BigDecimal> answers = score.getAnswers();
      if (answers != null) {
        Day day = new Day(score.getDate());
        try {
          double rScore = score.getScore(SaneScore.RIGHT);
          double lScore = score.getScore(SaneScore.LEFT);

          timeDataSetRight.addOrUpdate(day, rScore);
          timeDataSetLeft.addOrUpdate(day, lScore);
        } catch (SeriesException duplicates) {
          // ignore duplicates
        }
      }
    }
    dataset.addSeries(timeDataSetRight);
    dataset.addSeries(timeDataSetLeft);
    */
    return dataset;

  }

  @Override
  public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient) {
    logger.debug("gettable starting");
    Table table = new Table();

    for (PatientStudyExtendedData patStudy : patientStudies) {
      if (patStudy.getStudyCode().intValue() == study.getStudyCode().intValue()) {
        logger.debug("Found a " + study.getStudyDescription() + " study dated " + patStudy.getDtChanged());
        ArrayList<ChartScore> scores = getScore(patStudy);
        if (table.getNumberRows() < 1) { // add the heading on the first study
          table.addHeading("SANE");
          TableRow headingRow = new TableRow(500);
          headingRow.addColumn(new TableColumn("Date", 40));
          headingRow.addColumn(new TableColumn("RIGHT", 30));
          headingRow.addColumn(new TableColumn("LEFT", 30));
          table.addRow(headingRow);
        }
        for (ChartScore score : scores) {
          TableRow row = new TableRow(100);
          row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 40));
          row.addColumn(getScoreColumn((SaneScore) score, SaneScore.RIGHT ));
          row.addColumn(getScoreColumn((SaneScore) score, SaneScore.LEFT));
          table.addRow(row);
        }
      }
    }
    return table;
  }

  private TableColumn getScoreColumn(SaneScore score, int side) {
    TableColumn column;
    double answer = score.getScore(side);
    if (answer <=0) {
      column = new TableColumn("N/A", 30);
    } else {
      column = new TableColumn(String.valueOf(Math.round(answer)) , 30);
    }
    return column;
  }

  static private class ScoreHelper extends SurveyAdvanceBase {
    ScoreHelper(SiteInfo siteInfo) {
      super(siteInfo);
    }
  }

  public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                        ChartConfigurationOptions opts) {
    XYPlot plot = super.getPlot(renderer, ds, studies, opts);
    Collection<IntervalMarker> markers = plot.getRangeMarkers(Layer.BACKGROUND);

    for (IntervalMarker marker : markers ) {
      marker.setLabel(""); // remove the worse label
    }
    return plot;
  }

  static class SaneScore extends LocalScore implements MultiScore {

    static final int RIGHT = 1;
    static final int LEFT = 2;
    SaneScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    private double rightScore = 0;
    private double leftScore = 0;

    @Override
    public int getNumberOfScores() {
      return 2;
    }

    @Override
    public String getTitle(int scoreNumber, String studyDescription) {
      switch (scoreNumber) {
      case RIGHT:
        return studyDescription + " - RIGHT";
      case LEFT:
        return studyDescription + " - LEFT";
      default:
        return studyDescription;
      }
    }

    @Override
    public double getScore(int scoreNumber) {
      switch (scoreNumber) {
      case RIGHT:
        return rightScore;
      case LEFT:
        return leftScore;
      }
      return 0;
    }

    @Override
    public void setAnswer(Integer question, BigDecimal value) {
      if (value == null) {
        return;
      }
      switch (question) {
      case RIGHT:
        rightScore = value.doubleValue();
        return;
      case LEFT:
        leftScore = value.doubleValue();
      }
    }

    void setAnswer(Integer question, double value) {
      switch (question) {
      case RIGHT:
        rightScore = value;
        return;
      case LEFT:
        leftScore = value;
      }
    }
    public Double getPercentileScore(int scoreNumber) {
      return null;
    }

  }

  @Override
  public int getStudyIndex(String studyDescription) {
    return 6; // used by getPlot(). This gets the same chart background as GLOBAL_HEALTH uses.
  }
}
