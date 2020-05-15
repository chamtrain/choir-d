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
 * Scoring for the Neck Disability Index (NDI) survey.
 * Created by tpacht on 07/30/2019.
 */
public class NDIScoreProvider extends ExtensibleScoreProvider {
  private static final Logger logger = LoggerFactory.getLogger(NDIScoreProvider.class);
  private final SurveyAdvanceUtils surveyAdvanceUtils;
  public NDIScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
    surveyAdvanceUtils = new SurveyAdvanceUtils(siteInfo);
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {
    return (studyName != null && ("NDI".equals(studyName) || studyName.startsWith("NDI")));
  }

  @Override
  public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
    ArrayList<ChartScore> scores = new ArrayList<>();
    if (patientData == null) {
      return scores;
    }
    try {
      NDIScore ndiScore = new NDIScore(patientData.getDtChanged(), patientData.getPatientId(),
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
            ndiScore.setAnswer(inx, new BigDecimal(score));
          }
        }
        inx++;
      }
      scores.add(ndiScore);
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

    TimeSeries timeDataSet = new TimeSeries("NDI");
    for (ChartScore stat1 : stats) {
      NDIScore score = (NDIScore) stat1;
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
          if (score.getScore().intValue() > -1) {
            TableRow ghRow = new TableRow(100);
            ghRow.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 15));
            ghRow.addColumn(new TableColumn(score.getScore().toString(), 35));
            tableRows.add(ghRow);
          }
        }
      }
    }

    Table table = new Table();
    if (tableRows.size() > 0) {
      table.addHeading("Neck Disability Index (NDI)");
      TableRow ghHeadingRow = new TableRow(500);
      ghHeadingRow.addColumn(new TableColumn("Date", 15));
      ghHeadingRow.addColumn(new TableColumn("NDI Score", 35));
      ghHeadingRow.addColumn(new TableColumn("Percentage Score", 40));
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

  private class NDIScore extends LocalScore {

    private NDIScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    @Override
    public Map<String, BigDecimal> getScores() {
      HashMap<String, BigDecimal> scores = new HashMap<>();
      if (getScore().intValue()  > -1) {
        scores.put("NDI_SCORE", getScore());
      }
      BigDecimal percentageScore = getPercentageScore();
      if (percentageScore.intValue() > -1) {
        scores.put("NDI_PCT_SCORE",percentageScore);
      }
      return scores;
    }

    BigDecimal getPercentageScore() {
      if (getAnswers().size() < 1) {
        // No score if more than 1 answer is missing
        return new BigDecimal(-1);
      } else {
        int totalPossibleScore = 5 * getAnswers().size();

        BigDecimal calculatedScore = new BigDecimal(((getScore().doubleValue() / totalPossibleScore)) * 100);
        BigDecimal roundedScore  = calculatedScore.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        logger.trace("Sum of NDI responses is {} with a calculated percentage score of {}", getScore(), calculatedScore);
        return roundedScore;
      }
    }

    @Override
    public BigDecimal getScore() {
      ArrayList<BigDecimal> answers = getAnswers();
      if (answers.size() < 1) {
        return new BigDecimal(-1);
      }
      double sum = 0;
      for (BigDecimal answer : answers) {
        sum = sum + answer.doubleValue();
      }
      return new BigDecimal(sum);
    }

  }
}
