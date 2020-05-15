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
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
 * Scoring for the The Roland-Morris Disability Questionnaire (RMDQ) survey.
 * Created by tpacht on 08/30/2019.
 */
public class RMDQScoreProvider extends ExtensibleScoreProvider {
  private static final Logger logger = LoggerFactory.getLogger(RMDQScoreProvider.class);

  public RMDQScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {
    return (studyName != null && ("RMDQ".equals(studyName) || studyName.startsWith("RMDQ@")));
  }

  @Override
  public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
    ArrayList<ChartScore> scores = new ArrayList<>();
    if (patientData == null) {
      return scores;
    }
    try {
      RMDQScore rmdqScore = new RMDQScore(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription());
      SurveyDao surveyDao = new SurveyDao(dbp.get());
      SurveyQuery query = new SurveyQuery(dbp.get(), surveyDao, patientData.getSurveySiteId());
      Survey s = query.surveyBySurveyToken(patientData.getToken());
      SurveyStep step = s.answeredStepByProviderSectionQuestion(String.valueOf(patientData.getSurveySystemId()),
            String.valueOf(patientData.getStudyCode()), "Order0");
      if (step != null) {
        List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
        if (fields != null) {
          for (FormFieldAnswer field: fields) {
            if ("0:0:back_pain".equals(field.getFieldId())) {
              rmdqScore.setAnswer(0, new BigDecimal(field.getChoice().size())); // Score is the # of boxes checked
            }
          }
        }
      }
      scores.add(rmdqScore);
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

    TimeSeries timeDataSet = new TimeSeries("RMDQ");
    for (ChartScore stat1 : stats) {
      RMDQScore score = (RMDQScore) stat1;
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
    Table table = new Table();
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
    if (tableRows.size() > 0) {
      table.addHeading("Roland-Morris Disability Questionnaire (RMDQ)");
      TableRow ghHeadingRow = new TableRow(500);
      ghHeadingRow.addColumn(new TableColumn("Date", 15));
      ghHeadingRow.addColumn(new TableColumn("RMDQ Score", 35));
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

  private class RMDQScore extends LocalScore {

    private RMDQScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    @Override
    public Map<String, BigDecimal> getScores() {
      HashMap<String, BigDecimal> scores = new HashMap<>();
      if (getScore().intValue()  > -1) {
        scores.put("RMDQ_SCORE", getScore());
      }
      return scores;
    }

    @Override
    public BigDecimal getScore() {
      ArrayList<BigDecimal> answers = getAnswers();
      if (answers.size() < 1) {
        return new BigDecimal(-1);
      }
      int sum = 0;
      for (BigDecimal answer : answers) {
        sum = sum + answer.intValue(); // Counts the checked items
      }
      return new BigDecimal(sum);
    }

  }
}
