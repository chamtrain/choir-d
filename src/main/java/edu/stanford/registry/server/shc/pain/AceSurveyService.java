/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.pain;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;

import edu.stanford.registry.server.survey.ExtensibleScoreProvider;

import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.RegistryAssessmentsService;
import edu.stanford.registry.server.survey.SurveyAdvanceBase;

import edu.stanford.registry.server.utils.SquareXml;

import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.Constants;
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
import org.w3c.dom.Element;

import com.github.susom.database.Database;

public class AceSurveyService extends RegistryAssessmentsService {
  private static final String SERVICE_NAME = "AceSurveyService";
  private static final String STUDY_NAME = "ACELongform";
  private static final Logger logger = LoggerFactory.getLogger(AceSurveyService.class);

  public AceSurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public Study registerAssessment(Database database, String studyName, String title, String explanation) {
    if (isMyQuestionnaire(studyName)) {
      Study study = new Study(getSurveySystem(database).getSurveySystemId(), 0, studyName, 0);
      study.setTitle(title);
      study.setExplanation(explanation);
      SurveySystDao ssDao = new SurveySystDao(database);
      study = ssDao.insertStudy(study);
      return study;
    } else {
      logger.error("Not creating study named {}. It is not a recognized {} study name!", studyName, SERVICE_NAME);
      return null;
    }
  }

  @Override
  public void registerAssessment(Database database, Element questionnaire, String patientId,
                                 Token tok, User user) throws ServiceUnavailableException {
    if (questionnaire == null || patientId == null || tok == null) {
      logger.warn("not registering assessment. Missing at least on value for questionnaire {} patientId {} tok {}",
          questionnaire, patientId, tok != null? tok.getToken() : null);
      return;
    }

    String studyName = questionnaire.getAttribute("value");
    if (!isMyQuestionnaire(studyName)) {
      logger.warn("Not registering questionnaire named {} for patient. It is not a recognized {} study!",
          studyName, SERVICE_NAME);
      return;
    }
    String qOrder = questionnaire.getAttribute(Constants.XFORM_ORDER);
    Integer order = Integer.valueOf(qOrder);

    // Get the study
    SurveySystDao ssDao = new SurveySystDao(database);
    SurveySystem system = getSurveySystem(database);
    Study study = ssDao.getStudy(system.getSurveySystemId(), studyName);

    // Add the study if it doesn't exist
    if (study == null) {
      study = registerAssessment(database, studyName, "", "");
    }

    // Get the patient and this study for this patient
    PatientDao patientDao = new PatientDao(database, siteId, user);
    Patient pat = patientDao.getPatient(patientId);
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    PatientStudy patStudy = patStudyDao.getPatientStudy(pat, study, tok);

    if (patStudy == null) { // not there yet so lets add it
      patStudy = new PatientStudy(this.siteId);
      patStudy.setExternalReferenceId("");
      patStudy.setMetaVersion(0);
      patStudy.setPatientId(pat.getPatientId());
      patStudy.setStudyCode(study.getStudyCode());
      patStudy.setSurveySystemId(study.getSurveySystemId());
      patStudy.setToken(tok.getToken());
      patStudy.setOrderNumber(order);
      patStudyDao.insertPatientStudy(patStudy);
    }
  }

  private SurveySystem getSurveySystem(Database database) {
    return AceSurveySystem.getInstance(SERVICE_NAME, database);
  }

  private boolean isMyQuestionnaire(String studyName) {
    return studyName != null && studyName.startsWith(STUDY_NAME);
  }

  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    return new AceScoreProvider(dbp, siteInfo, studyName);
  }

  private static class AceScoreProvider extends ExtensibleScoreProvider {
    private static final Logger logger = LoggerFactory.getLogger(AceScoreProvider.class);
    private final SurveyAdvanceUtils surveyAdvanceUtils;

    public AceScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
      surveyAdvanceUtils = new SurveyAdvanceUtils(siteInfo);
    }

    @Override
    public boolean acceptsSurveyName(String studyName) {
      return studyName != null && studyName.startsWith(STUDY_NAME);
    }

    @Override
    public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
      ArrayList<ChartScore> scores = new ArrayList<>();
      if (patientData == null) {
        return scores;
      }
      try {
        AceScore aceScore = new AceScore(patientData.getDtChanged(), patientData.getPatientId(),
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

          if (step != null) {
            Integer score;
            score = surveyAdvanceUtils.selectedFieldChoice(step, fieldId);
            if (score != null) {
              aceScore.setAnswer(Integer.parseInt(parts[0]), new BigDecimal(score));
            }
          }
          inx++;
        }
        aceScore.setAssisted(patientData.wasAssisted());
        scores.add(aceScore);
      } catch (Exception e) {
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

      TimeSeries timeDataSet = new TimeSeries(STUDY_NAME);
      for (ChartScore stat1 : stats) {
        AceScore score = (AceScore) stat1;
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
              ghRow.addColumn(new TableColumn(score.getScore().toString(), 75));
              tableRows.add(ghRow);
            }
          }
        }
      }
      Table table = new Table();
      if (tableRows.size() > 0) {
        table.addHeading("ACE");
        TableRow ghHeadingRow = new TableRow(500);
        ghHeadingRow.addColumn(new TableColumn("Date", 15));
        ghHeadingRow.addColumn(new TableColumn("ACE Score (0-10)", 35));
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

      for (IntervalMarker marker : markers) {
        marker.setLabel(""); // remove the worse label
      }
      return plot;
    }

    static private class SurveyAdvanceUtils extends SurveyAdvanceBase {
      SurveyAdvanceUtils(SiteInfo siteInfo) {
        super(siteInfo);
      }
    }

    static class AceScore extends LocalScore {
      AceScore(Date dt, String patientId, Integer studyCode, String description) {
        super(dt, patientId, studyCode, description);
      }

      @Override
      public Map<String, BigDecimal> getScores() {
        HashMap<String, BigDecimal> scores = new HashMap<>();
        if (getScore() != null && getScore().intValue() > -1) {
          scores.put("ACE_SCORE", getScore());
        }
        return scores;
      }

      @Override
      public BigDecimal getScore() {
        // Survey has 16 questions with a score range of 0 - 10
        // Survey questions start with 0 (not 1)
        // If yes on # 1 and/or #2, coded as 1
        int scoreSum = getResponseValue(0, 1);
        //If yes on # 3 and/or #4, coded as 1
        scoreSum += getResponseValue(2, 3);
        //If yes on # 5 and/or #6, coded as 1
        scoreSum += getResponseValue(4, 5);
        //If yes on # 7 and/or #8, coded as 1
        scoreSum += getResponseValue(6, 7);
        //If yes on # 9 and/or #10, coded as 1
        scoreSum += getResponseValue(8, 9);
        //If yes on # 11 and/or #12 and/or #13, coded as 1
        if (isResponseYes(10) || isResponseYes(11) || isResponseYes(12)) {
          scoreSum += 1;
        }
        //If yes on # 14, coded as 1
        scoreSum += isResponseYes(13) ? 1 : 0;
        //If yes on # 15, coded as 1
        scoreSum += isResponseYes(14) ? 1 : 0;
        //If yes on # 16, coded as 1
        scoreSum += isResponseYes(15) ? 1 : 0;
        //If yes on # 17, coded as 1
        scoreSum += isResponseYes(16) ? 1 : 0;
        return new BigDecimal(scoreSum);
      }

      private int getResponseValue(int q1, int q2) {
        // No=0; Yes=1; Decline to answer=2;
        if (isResponseYes(q1) || isResponseYes(q2)) {
          return 1;
        }
        return 0;
      }

      private boolean isResponseYes(int q) {
        return (getAnswer(q).intValue() == 1);
      }
    }
  }

  static class AceSurveySystem extends SurveySystem {

    private static final long serialVersionUID = -4382364022282098050L;
    private static AceSurveySystem me = null;

    private AceSurveySystem(SurveySystem surveySystem) throws DataException {
      this.copyFrom(surveySystem);
    }

    public static AceSurveySystem getInstance(String surveySystemName, Database database) throws DataException {
      if (me == null) {
        SurveySystDao ssDao = new SurveySystDao(database);
        SurveySystem surveySystem = ssDao.getOrCreateSurveySystem(surveySystemName, null);
        me = new AceSurveySystem(surveySystem);
      }
      return me;
    }
  }
}
