/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.server.utils.SquareXml;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.server.utils.XmlFormatter;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.Pass40Score;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

import javax.xml.parsers.ParserConfigurationException;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;

public class PainPsychologyScoreProvider implements ScoreProvider {

  private static final Logger logger = LoggerFactory.getLogger(PainPsychologyScoreProvider.class);
  private static final int[] colWidths = { 50, 6, 32 }; // 'severity score', (each) 'category', 'qualifies'

  private final Supplier<Database> dbp;
  private final SiteInfo siteInfo;

  public PainPsychologyScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo) {
    this.dbp = dbp;
    this.siteInfo = siteInfo;
  }


  @Override
  public String getDescription() {
    return "Pain psychology score provider";
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {

    String surveys[] = { "pclc", "alcoholCage", "drugCage", "psychCommon", "psychCommonEnd", "PASS40", "CESD", "PSEQ", "PCL5" };
    if (studyName != null) {
      for (String survey : surveys) {
        if (studyName.startsWith(survey + "@")) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
    ArrayList<ChartScore> scores = new ArrayList<>();
    if (patientData == null || patientData.getStudyDescription() == null) {
      return scores;
    }
    if (patientData.getStudyDescription().startsWith("PASS40")) {
      return getPass40Score(patientData, scores);
    }

    if (patientData.getStudyDescription().startsWith("CESD")) {
      return getCESDscore(patientData, scores);
    }
    if (patientData.getStudyDescription().startsWith("PSEQ")) {
      return getPSEQscore(patientData, scores);
    }
    if (patientData.getStudyDescription().startsWith("PCL5")) {
      return getPCL5score(patientData, scores);
    }
    if (!patientData.getStudyDescription().startsWith("pclc")) {
      return scores;
    }
    String surveyProvider = patientData.getSurveySystemId().toString();
    String sectionId = patientData.getStudyCode().toString();
    SquareXml squareXml = new SquareXml(dbp.get(), siteInfo, patientData.getStudyDescription(), "psych_pclc");
    SurveyDao surveyDao = new SurveyDao(dbp.get());
    SurveyQuery query = new SurveyQuery(dbp.get(), surveyDao, patientData.getSurveySiteId());

    Survey s = query.surveyBySurveyToken(patientData.getToken());
    LinkedHashMap<String, String> columns = squareXml.getColumns();
    LinkedHashMap<String, String> references = squareXml.getReferences();
    Object refKeys[] = references.keySet().toArray();
    int inx = 0;
    LocalScore chartScore = new LocalScore(patientData.getDtChanged(), patientData.getPatientId(),
        patientData.getStudyCode(), patientData.getStudyDescription());
    SurveyAdvanceUtils surveyAdvanceUtils = new SurveyAdvanceUtils(siteInfo);
    int totalScore = 0;
    for (String columnName : columns.keySet()) {
      logger.trace("getting score for {}", columnName);
      String refKey = refKeys[inx].toString();
      String fieldId = references.get(refKey) + ":" + refKey;
      String[] parts = fieldId.split(":");
      try {
        logger.trace(
            " surveyAdvanceUtils.getSelect1Response( s, surveyProvider, {}, Order {}, Field {}", sectionId, parts[0],
                fieldId);
        Integer score = surveyAdvanceUtils.getSelect1Response(s, surveyProvider, sectionId,
            "Order" + parts[0], fieldId);
        logger.trace(" adding score {} " , score);
        if (score != null) {
          chartScore.setAnswer(Integer.parseInt(parts[0]), new BigDecimal(score));
          totalScore += score;
        }
      } catch (NumberFormatException nfe) {
        logger.error("could not determine pclc score for item {}", parts[0]);
      }
      inx++;
    }

    chartScore.setScore(new BigDecimal(totalScore));
    scores.add(chartScore);
    return scores;
  }

  @Override
  public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> scores, PrintStudy study, ChartConfigurationOptions opts) {
    final TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(baseLineSeries);
    if (scores == null || study == null) {
      return dataset;
    }
    TimeSeries timeDataSetPcs = new TimeSeries("PCS Score");
    for (ChartScore chartScore : scores) {
      LocalScore score = (LocalScore) chartScore;
      ArrayList<BigDecimal> answers = score.getAnswers();
      if (answers != null) {
        Day day = new Day(score.getDate());
        BigDecimal pcsScore = new BigDecimal(0);
        for (BigDecimal answer : answers) {
          pcsScore = answer;
        }
        try {
          timeDataSetPcs.addOrUpdate(day, pcsScore);
        } catch (SeriesException ignored) {
        }
      }
    }
    dataset.addSeries(timeDataSetPcs);
    return dataset;
  }

  @Override
  public Table getScoreTable(ArrayList<ChartScore> scores) {
    Table table = new Table();
    if (scores == null || scores.size() < 1 || scores.get(0) == null) {
      return table;
    }
    table.addHeading(scores.get(0).getStudyDescription());
    table.addRow(getColHeader());
    try {
      for (ChartScore score : scores) {
        if (score != null)
          table.addRow(getScoreRow((LocalScore) score));
      }
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
    return table;
  }

  private TableRow getScoreRow(LocalScore score) {
    int tableWidth = colWidths[0] + (3 * colWidths[1]) + colWidths[2];
    TableRow row = new TableRow(tableWidth);

    // count responses that are considered to be "Moderately or Above" (values 3-5)
    int[] categoryScore = { 0, 0, 0 };
    for (Integer questionNumber : score.getQuestions()) {
      if (questionNumber != null && score.getAnswer(questionNumber) != null) {
        logger.trace(
            "processing score {} for question {}" ,score.getAnswer(questionNumber),  questionNumber);
        BigDecimal ansScore = score.getAnswer(questionNumber);
        if (ansScore.intValue() > 2) {
          if (questionNumber < 5) { // Our items #'s begin at 0 so these are their 1-5
            categoryScore[0] = categoryScore[0] + 1;
          } else if (questionNumber < 12) { // their 6-12
            categoryScore[1] = categoryScore[1] + 1;
          } else { // their 13 - 17
            categoryScore[2] = categoryScore[2] + 1;
          }
        }
      }
    }
    String qualifies = " - ";
    if (categoryScore[0] >= 1 && categoryScore[1] >= 3 && categoryScore[2] >= 2) {
      qualifies = "Clinically significant";
    }
    if (score.getScore() != null) {
      row.addColumn(new TableColumn(score.getScore().toString(), colWidths[0]));
      row.addColumn(new TableColumn(Integer.toString(categoryScore[0]), colWidths[1]));
      row.addColumn(new TableColumn(Integer.toString(categoryScore[1]), colWidths[1]));
      row.addColumn(new TableColumn(Integer.toString(categoryScore[2]), colWidths[1]));
      row.addColumn(new TableColumn(qualifies, colWidths[2]));
    }
    return row;
  }

  private TableRow getColHeader() {
    TableRow colHeader = new TableRow(colWidths[0] + (3 * colWidths[1]) + colWidths[2]);
    colHeader.addColumn(new TableColumn("Total Severity Score", colWidths[0]));
    colHeader.addColumn(new TableColumn("#B", colWidths[1]));
    colHeader.addColumn(new TableColumn("#C", colWidths[1]));
    colHeader.addColumn(new TableColumn("#D", colWidths[1]));
    colHeader.addColumn(new TableColumn(" - ", colWidths[2]));
    return colHeader;
  }

  @Override
  public String formatExplanationText(Study study, ArrayList<ChartScore> scores) {
    return null;
  }

  @Override
  public ChartInfo createLineChart(ArrayList<ChartScore> stats, XYDataset ds, Study study, ChartConfigurationOptions opts) {
    return null; // Survey is only given once so no charting
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient, boolean allAnswers) {

    if (patientStudies == null || study == null || patient == null) {
      return new ArrayList<>();
    }
    PatientStudyExtendedData patStudy = null;

    // get the last one of this study type
    for (PatientStudyExtendedData patientStudy : patientStudies) {
      if (patientStudy.getStudyCode().intValue() == study.getStudyCode().intValue()) {
        patStudy = patientStudy;
      }
    }
    return getSurvey(patStudy, study, patient, allAnswers);
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(PatientStudyExtendedData patStudy, PrintStudy study, Patient patient, boolean allAnswers) {

    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
    if (patStudy == null) {
      return questions;
    }
    String xmlDocumentString = patStudy.getContents();
    if (xmlDocumentString == null) {
      // get the file
      xmlDocumentString = XMLFileUtils.getInstance(siteInfo).getXML(dbp.get(), study.getStudyDescription());
      logger.trace("read xml {}", xmlDocumentString);
    }

    try {
      NodeList itemList = XmlFormatter.getNodeList(xmlDocumentString, Constants.ITEM);
      if (itemList == null) {
        return questions;
      }
      for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
        Element itemNode = (Element) itemList.item(itemInx);
        // check if conditional
        boolean meets = RegistryAssessmentUtils.qualifies(patient, itemNode, xmlDocumentString);
        // If they qualify for this question
        if (meets) {
          SurveyQuestionIntf question = RegistryAssessmentUtils.getQuestion(itemNode, itemInx, allAnswers);
          questions.add(question);
        }
      }
    } catch (ParserConfigurationException | IOException | SAXException e) {
      e.printStackTrace();
    }
    return questions;
  }

  @Override
  public XYPlot getPlot(ChartInfo chartInfo, ArrayList<Study> studies, ChartConfigurationOptions opts) {
    return null; // N/A Survey is only given once
  }

  @Override
  public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient) {

    Table table = new Table();
    if (patientStudies == null || patientStudies.size() < 1 || study == null || study.getStudyDescription() == null) {
      return table;
    }
    logger.trace("getTable called for {}", study.getStudyDescription());
    if (study.getStudyDescription().startsWith("pclc")) {
      try {
        for (PatientStudyExtendedData patientStudyExtendedData : patientStudies) {
          if (patientStudyExtendedData.getStudyCode().intValue() == study.getStudyCode().intValue()) {
            ArrayList<ChartScore> scores = getScore(patientStudyExtendedData);
            logger.trace("getScore returned {} scores",  scores.size());
            for (ChartScore score : scores) {
              if (scores.size() > 0) {
                if (table.getRows().size() < 1) {
                  table.addRow(getColHeader());
                }
                table.addRow(getScoreRow((LocalScore) score));
              }
            }
          }
        }
      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      }
    } else if (study.getStudyDescription().startsWith("PASS40")) {
      try {
        for (PatientStudyExtendedData patientStudyExtendedData : patientStudies) {
          if (patientStudyExtendedData.getStudyCode().intValue() == study.getStudyCode().intValue()) {
            ArrayList<ChartScore> scores = getScore(patientStudyExtendedData);
            logger.trace("getScore returned {} scores", scores.size());
            for (ChartScore score : scores) {
              if (scores.size() > 0) {
                Pass40Score pass40Score = (Pass40Score) score;
                if (table.getRows().size() < 1) {
                  TableRow headingRow1 = new TableRow(100);
                  headingRow1.addColumn(new TableColumn( " Scores", 100));
                  table.addRow(headingRow1);
                  TableRow headingRow2 = new TableRow(100);
                  TableRow headingRow3 = new TableRow(100);
                  for (int i = 0; i < 4; i++) {
                    headingRow2.addColumn(new TableColumn(pass40Score.getTitle(i, patientStudyExtendedData.getStudyDescription()), 24));
                    headingRow3.addColumn(new TableColumn("Score", 12));
                    headingRow3.addColumn(new TableColumn("Percentile", 12));
                  }
                  table.addRow(headingRow2);
                  table.addRow(headingRow3);
                }
                TableRow row = new TableRow(100);
                for (int i = 0; i < 4; i++) {
                  row.addColumn(new TableColumn(getRoundedString(pass40Score.getScore(i)), 12));
                  row.addColumn(new TableColumn(getRoundedString(pass40Score.getPercentileScore(i)), 12));
                }

                table.addRow(row);
              }
            }
          }
        }
      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      }
    } else if (study.getStudyDescription().startsWith("CESD") || study.getStudyDescription().startsWith("PSEQ") || study.getStudyDescription().startsWith("PCL5")) {
      for (PatientStudyExtendedData patientStudyExtendedData : patientStudies) {
        if (patientStudyExtendedData.getStudyCode().intValue() == study.getStudyCode().intValue()) {
          ArrayList<ChartScore> scores = new ArrayList<>();
          int width = 50;
          if (study.getStudyDescription().startsWith("CESD")) {
            scores = getCESDscore(patientStudyExtendedData, scores);
          } else if (study.getStudyDescription().startsWith("PCL5")) {
            scores = getPCL5score(patientStudyExtendedData, scores);
          } else {
            scores = getPSEQscore(patientStudyExtendedData, scores);
            width = 65;
          }
          logger.trace("getScore returned {} scores", scores.size());
          for (ChartScore score : scores) {
            TableRow row = new TableRow(100);
            row.addColumn(new TableColumn("Score", width));
            BigDecimal scoreValue = score.getScore();
            row.addColumn(new TableColumn(String.valueOf(scoreValue), 10));
            if (study.getStudyDescription().startsWith("CESD")) {
              if (scoreValue.intValue() >= 23) {
                row.addColumn(new TableColumn("MDD", 10));
              } else if (scoreValue.intValue() >= 16) {
                row.addColumn(new TableColumn("significatnt depressive symptoms", 40));
              }
            }
            table.addRow(row);
          }
        }
      }
    }
    return table;
  }

  private String getRoundedString(Double doubleValue) {
    return String.valueOf(doubleValue.intValue());
  }

  @Override
  public int getReportTextFontSize(PrintStudy study) {
    return 11;
  }

  // Make our own because SurveyAdvanceBase is abstract
  static private class SurveyAdvanceUtils extends SurveyAdvanceBase {
    public SurveyAdvanceUtils(SiteInfo siteInfo) {
      super(siteInfo);
    }
  }

  private ArrayList<ChartScore> getPass40Score(PatientStudyExtendedData patientData, ArrayList<ChartScore> scores) {
    Pass40Score pass40Score = new Pass40Score(patientData.getDtChanged(), patientData.getPatientId(), patientData.getStudyCode(), patientData.getStudyDescription());
    String surveyProvider = patientData.getSurveySystemId().toString();
    String sectionId = patientData.getStudyCode().toString();
    SquareXml squareXml = new SquareXml(dbp.get(), siteInfo, patientData.getStudyDescription(), "PASS40");
    SurveyDao surveyDao = new SurveyDao(dbp.get());
    SurveyQuery query = new SurveyQuery(dbp.get(), surveyDao, patientData.getSurveySiteId());

    Survey s = query.surveyBySurveyToken(patientData.getToken());
    LinkedHashMap<String, String> columns = squareXml.getColumns();
    LinkedHashMap<String, String> references = squareXml.getReferences();
    Object refKeys[] = references.keySet().toArray();
    int inx = 0;

    SurveyAdvanceUtils surveyAdvanceUtils = new SurveyAdvanceUtils(siteInfo);

    for (String columnName : columns.keySet()) {
      logger.trace("getting score for {} ", columnName);
      String refKey = refKeys[inx].toString();
      String fieldId = references.get(refKey) + ":" + refKey;
      String[] parts = fieldId.split(":");
      try {
        logger.trace(
            " surveyAdvanceUtils.getSelect1Response( s, surveyProvider,{}, {}, {} ", sectionId, parts[0], fieldId);
        Integer score = surveyAdvanceUtils.getRadioIntegerResponse(s, surveyProvider, sectionId, parts[0]);
        logger.trace(" adding score answer {}, {}", parts[0], score);
        if (score != null) {
          pass40Score.addScore(refKey, score);
        }
      } catch (NumberFormatException nfe) {
        logger.error("could not determine PASS40 score for item {}", parts[0]);
      }
      inx++;
    }

    scores.add(pass40Score);
    return scores;
  }
  private ArrayList<ChartScore> getCESDscore(PatientStudyExtendedData patientData, ArrayList<ChartScore> scores) {
    return getSumScore("CESD", patientData, scores);
  }

  private ArrayList<ChartScore> getPSEQscore(PatientStudyExtendedData patientData, ArrayList<ChartScore> scores) {
    return getSumScore("PSEQ", patientData, scores);
  }

  private ArrayList<ChartScore> getPCL5score(PatientStudyExtendedData patientData, ArrayList<ChartScore> scores) {
    return getSumScore("PCL5", patientData, scores);
  }

  private ArrayList<ChartScore> getSumScore(String type, PatientStudyExtendedData patientData, ArrayList<ChartScore> scores) {
    LocalScore localScore = new LocalScore(patientData.getDtChanged(), patientData.getPatientId(), patientData.getStudyCode(), patientData.getStudyDescription());
    String surveyProvider = patientData.getSurveySystemId().toString();
    String sectionId = patientData.getStudyCode().toString();
    SquareXml squareXml = new SquareXml(dbp.get(), siteInfo, patientData.getStudyDescription(), type);
    SurveyDao surveyDao = new SurveyDao(dbp.get());
    SurveyQuery query = new SurveyQuery(dbp.get(), surveyDao, patientData.getSurveySiteId());

    Survey s = query.surveyBySurveyToken(patientData.getToken());
    LinkedHashMap<String, String> columns = squareXml.getColumns();
    LinkedHashMap<String, String> references = squareXml.getReferences();
    Object refKeys[] = references.keySet().toArray();
    int inx = 0;

    SurveyAdvanceUtils surveyAdvanceUtils = new SurveyAdvanceUtils(siteInfo);

    for (String columnName : columns.keySet()) {
      logger.trace("getting " + type + " score for {}", columnName);
      String refKey = refKeys[inx].toString();
      String fieldId = references.get(refKey) + ":" + refKey;
      String[] parts = fieldId.split(":");
      try {
        SurveyStep step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId ,  "Order" + parts[0]);
        Integer score;
        switch (type) {
        case "CESD":
          logger.trace(
              " surveyAdvanceUtils.selectedFieldChoice(step(surveyProvider, {}, Order{}), {}", sectionId, parts[0], fieldId);
          score = surveyAdvanceUtils.selectedFieldChoice(step, fieldId);
          if (score != null) {
            logger.trace(" adding score answer {}, {}", parts[0], score);
            localScore.setAnswer(Integer.parseInt(parts[0]), refKey, new BigDecimal(score));
          }
          break;
        case "PSEQ":
          logger.trace(
              " surveyAdvanceUtils.getRadioIntegerResponse(s, surveyProvider, {}, {}", sectionId, parts[0], parts[0]);
          score = surveyAdvanceUtils.getRadioIntegerResponse(s, surveyProvider, sectionId, parts[0]);
          if (score != null) {
            logger.trace(" adding score answer {}, {}", parts[0], score);
            localScore.setAnswer(Integer.parseInt(parts[0]), refKey, new BigDecimal(score));
          }
          break;
        case "PCL5":
          logger.trace(
              " surveyAdvanceUtils.selectedFieldChoice(step(surveyProvider, {}, Order{}), {}", sectionId, parts[0], fieldId);
          score = surveyAdvanceUtils.selectedFieldChoice(step, fieldId);
          if (score != null) {
            logger.trace(" adding score answer {}, {}", parts[0], score);
            localScore.setAnswer(Integer.parseInt(parts[0]), refKey, new BigDecimal(score));
          }
          break;
        }
      } catch (NumberFormatException nfe) {
        logger.error("could not determine " + type + " score for item {}", parts[0]);
      }
      inx++;
    }

    scores.add(localScore);
    return scores;
  }
}
