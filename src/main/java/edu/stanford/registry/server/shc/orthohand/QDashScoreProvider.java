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
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.survey.ExtensibleScoreProvider;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.SurveyAdvanceBase;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.LocalScore;
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
 * Scoring for the Q-Dash survey. Created by tpacht on 07/20/2017.
 */
public class QDashScoreProvider extends ExtensibleScoreProvider {
  private static final Logger logger = LoggerFactory.getLogger(QDashScoreProvider.class);
  public QDashScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
  }
  @Override
  public boolean acceptsSurveyName(String studyName) {
    if ("handqDASH".equals(studyName)) {
      return true;
    }
    return false;
  }
  @Override
  public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
    ArrayList<ChartScore> scores = new ArrayList<>();
    if (patientData == null) {
      return scores;
    }
    try {
      LocalScore chartScore = new LocalScore(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription());

      SurveyDao surveyDao = new SurveyDao(dbp);
      SurveyQuery query = new SurveyQuery(dbp, surveyDao, patientData.getSurveySiteId());
      Survey s = query.surveyBySurveyToken(patientData.getToken());
      String surveyProvider = String.valueOf(patientData.getSurveySystemId());
      String sectionId = String.valueOf(patientData.getStudyCode());

      int[] qDashQuestions = { 1, 4, 7, 8, 9, 10, 11};
      String[] qDashRefs = {"JAR", "WASHBACK", "SOCIAL", "LIMITED", "PAIN", "PINS", "SLEEP"};

      ScoreHelper helper = new ScoreHelper(siteInfo);
      int refInx = 0;
      for (int qId : qDashQuestions) {
        logger.debug("Looking for section {} questionId Order{} ", sectionId,  qId);
        SurveyStep step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "Order" + qId);
        if (step != null) {
          String fieldId = qId+":2:"+ qDashRefs[refInx];
          Integer choice = helper.selectedFieldChoice(step, fieldId);
          logger.debug("choice was for fieldId {} is {}", fieldId, choice);
          if (choice != null)
            chartScore.setAnswer(qId, new BigDecimal(choice));
        }
        refInx++;
      }

      // Add in the 5 answers from the prwhe converted to the equivalent qDash response values
      SurveySystDao ssDao = new SurveySystDao(dbp);
      Study prwhe = ssDao.getStudy(patientData.getSurveySystemId(), "prwhe");
      sectionId = String.valueOf(prwhe.getStudyCode());
      chartScore.setAnswer(7, getConvertedScore(s, surveyProvider, sectionId, "7")); // knife
      chartScore.setAnswer(10, getConvertedScore(s, surveyProvider, sectionId, "10")); // carry 10lb
      chartScore.setAnswer(13, getConvertedScore(s, surveyProvider, sectionId, "13")); // chores
      chartScore.setAnswer(15, getConvertedScore(s, surveyProvider, sectionId, "15")); // recreational

      BigDecimal responseSum = chartScore.getScore();
      Double calculatedScore = ((responseSum.doubleValue() / 11.0) - 1.0) * 25.0;
      logger.debug("Sum of qDash responses is {} with a calculated score of {}", responseSum, calculatedScore);
      chartScore.setScore(new BigDecimal(calculatedScore));
      chartScore.setAssisted(patientData.wasAssisted());
      scores.add(chartScore);

    } catch (Exception e) {
      logger.error("Exception getting score for patient {} study {}", patientData.getPatientId(),
          patientData.getStudyCode(), e);
    }

    return scores;
  }

  public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> stats, PrintStudy study,
                              ChartConfigurationOptions opts) {

    final TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(baseLineSeries);
    if (stats == null || study == null) {
      return dataset;
    }

    TimeSeries timeDataSet = new TimeSeries("Q-Dash");
    for (ChartScore stat1 : stats) {
      LocalScore score = (LocalScore) stat1;
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
  public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient) {
    Table table = new Table();
    PatientStudyExtendedData prwheStudy = null;
    for (PatientStudyExtendedData patientStudy2 : patientStudies) {
      if (patientStudy2.getStudyCode().intValue() == study.getStudyCode().intValue()) {
        prwheStudy = patientStudy2;
      }
    }
    if (prwheStudy != null) {
      ArrayList<ChartScore> scores = getScore(prwheStudy);
      table.addHeading("PRWHE");
      TableRow ghHeadingRow = new TableRow(500);
      ghHeadingRow.addColumn(new TableColumn("Date", 20));
      ghHeadingRow.addColumn(new TableColumn("Pain Score", 20));
      ghHeadingRow.addColumn(new TableColumn("%ile", 20));
      ghHeadingRow.addColumn(new TableColumn("Function Score", 20));
      ghHeadingRow.addColumn(new TableColumn("%ile", 20));
      table.addRow(ghHeadingRow);

      for (ChartScore score : scores) {
        TableRow ghRow = new TableRow(100);
        ghRow.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
        ghRow.addColumn(new TableColumn(score.getScore().toString(), 20));
        ghRow.addColumn(new TableColumn(calculatePercentile(score.getScore().doubleValue()).toString(), 20));
        table.addRow(ghRow);
      }
    }
    return table;
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

  private BigDecimal getConvertedScore(Survey s, String surveyProvider, String sectionId, String questionId) {
    SurveyStep step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, questionId);

    if (step != null) {
      logger.debug("prwhe question {} response was {} returning {}", questionId, step.answerNumeric(),
          getQDashEquivalent(step.answerNumeric()));
      return new BigDecimal(getQDashEquivalent(step.answerNumeric()));
    }
    return new BigDecimal(0);
  }

  private int getQDashEquivalent(int prwheAnswer) {
    switch (prwheAnswer) {
    case 0:
      return 1;
    case 1:
    case 2:
    case 3:
      return 2;
    case 4:
    case 5:
    case 6:
      return 3;
    case 7:
    case 8:
    case 9:
      return 4;
    case 10:
      return 5;
    }
    return 0;
  }

  static private class ScoreHelper extends SurveyAdvanceBase {

    public ScoreHelper(SiteInfo siteInfo) {
      super(siteInfo);
    }


  }

  @Override
  public int getStudyIndex(String studyDescription) {
    return 6; // This gets the same chart background as GLOBAL_HEALTH uses.
  }
}
