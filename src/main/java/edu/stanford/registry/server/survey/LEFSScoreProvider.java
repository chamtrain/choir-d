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
 * Scoring for the Lower Extremity Functional Scale (LEFS) questionnaire.
 * Created by tpacht on 09/04/2019.
 */
public class LEFSScoreProvider extends ExtensibleScoreProvider {
  private static final Logger logger = LoggerFactory.getLogger(LEFSScoreProvider.class);

  private static final String LEFS = "LEFS";
  public LEFSScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {
    return (studyName != null && (LEFS.equals(studyName) || studyName.startsWith(LEFS)));
  }

  @Override
  public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
    ArrayList<ChartScore> scores = new ArrayList<>();
    if (patientData == null) {
      return scores;
    }
    try {
      SurveyDao surveyDao = new SurveyDao(dbp.get());
      SurveyQuery query = new SurveyQuery(dbp.get(), surveyDao, patientData.getSurveySiteId());
      Survey s = query.surveyBySurveyToken(patientData.getToken());
      SquareXml squareXml = new SquareXml(dbp.get(), siteInfo, patientData.getStudyDescription(), LEFS);
      LinkedHashMap<String, String> columns = squareXml.getColumns();
      LinkedHashMap<String, String> references = squareXml.getReferences();
      Object[] refKeys = references.keySet().toArray();
      int inx = 0;
      SurveyAdvanceUtils surveyAdvanceUtils = new SurveyAdvanceUtils(siteInfo);
      LEFSScore lefsScore = new LEFSScore(patientData.getDtChanged(), patientData.getPatientId(), patientData.getStudyCode(), patientData.getStudyDescription());

      for (String columnName : columns.keySet()) {
        logger.trace("getting " + patientData.getStudyDescription() + " score for {}", columnName);
        String refKey = refKeys[inx].toString();
        String fieldId = references.get(refKey) + ":" + refKey;
        String[] parts = fieldId.split(":");
        try {
          String qId = "Order" + parts[0];
          SurveyStep step = s.answeredStepByProviderSectionQuestion(patientData.getSurveySystemId().toString(),
              patientData.getStudyCode().toString(), qId);
          Integer score;
          logger.trace(" selectedFieldChoice(step(surveyProvider {}, section {}, qId {}, fieldId {}",
              patientData.getSurveySystemId().toString(), patientData.getStudyCode().toString(), qId, fieldId);
          score = surveyAdvanceUtils.selectedFieldChoice(step, fieldId);
          if (score != null) {
            logger.trace(" adding score answer {}, {}", parts[0], score);
            lefsScore.setAnswer(Integer.parseInt(parts[0]), refKey, new BigDecimal(score));
          }
        } catch (NumberFormatException nfe) {
          logger.error("could not determine LEFS score for item {}", parts[0]);
        }
        inx++;
      }
      scores.add(lefsScore);
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
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

    TimeSeries timeDataSet = new TimeSeries(LEFS);
    for (ChartScore stat1 : stats) {
      LEFSScore score = (LEFSScore) stat1;
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
      if (patientStudy2 != null && patientStudy2.getStudyCode().intValue() == study.getStudyCode().intValue()) {
        ArrayList<ChartScore> scores = getScore(patientStudy2);
        for (ChartScore score : scores) {
          TableRow ghRow = new TableRow(100);
          ghRow.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 15));
          ghRow.addColumn(new TableColumn(score.getScore().toString(), 75));
          tableRows.add(ghRow);
        }
      }
    }
    Table table = new Table();
    if (tableRows.size() > 0) {
      table.addHeading("Lower Extremity Functional Scale (LEFS)");
      TableRow ghHeadingRow = new TableRow(500);
      ghHeadingRow.addColumn(new TableColumn("Date", 15));
      ghHeadingRow.addColumn(new TableColumn("LEFS Score", 75));
      table.addRow(ghHeadingRow);
      for (TableRow tableRow : tableRows) {
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

  private class LEFSScore extends LocalScore {

    private LEFSScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    @Override
    public Map<String, BigDecimal> getScores() {
      HashMap<String, BigDecimal> scores = new HashMap<>();
      if (getScore().intValue()  > -1) {
        scores.put("LEFS_SCORE", getScore());
      }
      return scores;
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
