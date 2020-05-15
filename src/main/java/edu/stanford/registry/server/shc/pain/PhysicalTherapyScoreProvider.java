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
package edu.stanford.registry.server.shc.pain;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.SurveyAdvanceBase;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.server.utils.SquareXml;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.server.utils.XmlFormatter;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.MultiScore;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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

public class PhysicalTherapyScoreProvider implements ScoreProvider {

  private static final Logger logger = LoggerFactory.getLogger(PhysicalTherapyScoreProvider.class);
  private static final int DATE_COLUMN_WIDTH = 15;
  private static final int HR_COLUMN_LENGTH = 20;

  private static final String CPAQ = "CPAQ";
  private static final String SEBS = "SEBS";
  private static final String PAVS = "PAVS";
  private static final String TSK11 = "TSK11";
  private static final String PSEQ2 = "PSEQ2";
  private static final String HR_LABEL = "High risk";
  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yy");
  private final Supplier<Database> dbp;
  private final SiteInfo siteInfo;
  private final SurveyAdvanceUtils surveyAdvanceUtils;

  PhysicalTherapyScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo) {
    this.dbp = dbp;
    this.siteInfo = siteInfo;
    this.surveyAdvanceUtils = new SurveyAdvanceUtils(siteInfo);
  }

  @Override
  public String getDescription() {
    return "Physical therapy score provider";
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {
    if (studyName != null) {
      for (String surveyName : PhysicalTherapyService.surveys) {
        if (surveyName.equals(studyName) || studyName.startsWith(surveyName + "@")) {
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
    if (patientData.getStudyDescription().startsWith(PSEQ2)) {
      return getSumScore(PSEQ2, patientData, scores);
    } else if (patientData.getStudyDescription().startsWith(TSK11)) {
      return getSumScore("TSK", patientData, scores);
    } else if (patientData.getStudyDescription().startsWith(CPAQ)) {
      return getCPAQscore(patientData, scores);
    } else if (patientData.getStudyDescription().startsWith(SEBS)) {
      return getSumScore(SEBS, patientData, scores);
    } else if (patientData.getStudyDescription().startsWith(PAVS)) {
      return getPAVSscore(patientData, scores);
    }
    return scores;
  }

  @Override
  public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> scores, PrintStudy study, ChartConfigurationOptions opts) {
    final TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(baseLineSeries);
    if (scores == null || study == null) {
      return dataset;
    }
    TimeSeries timeDataSet = new TimeSeries("Score");
    for (ChartScore chartScore : scores) {
      LocalScore score = (LocalScore) chartScore;
      ArrayList<BigDecimal> answers = score.getAnswers();
      if (answers != null) {
        Day day = new Day(score.getDate());
        BigDecimal scoreValue = new BigDecimal(0);
        for (BigDecimal answer : answers) {
          scoreValue = answer;
        }
        try {
          timeDataSet.addOrUpdate(day, scoreValue);
        } catch (SeriesException ignored) {
        }
      }
    }
    dataset.addSeries(timeDataSet);
    return dataset;
  }

  @Override
  public Table getScoreTable(ArrayList<ChartScore> scores) {
    Table table = new Table();
    if (scores == null || scores.size() < 1 || scores.get(0) == null) {
      return table;
    }
    table.addRow(getColHeader(scores.get(0).getStudyDescription()));
    try {
      for (ChartScore score : scores) {
        if (score != null)
          table.addRow(getScoreRow(score));
      }
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
    return table;
  }

  private TableRow getScoreRow(ChartScore score) {
    TableRow row = new TableRow(100);
    if (score.getScore() != null) {
      row.addColumn(new TableColumn(dateFormatter.format(score.getDate()),  DATE_COLUMN_WIDTH));
      row.addColumn(new TableColumn(Integer.toString(score.getScore().intValue()), 75));
      if (score.getStudyDescription().startsWith(SEBS) || score.getStudyDescription().startsWith(PAVS)) {
        row.addColumn(new TableColumn(score .getScore().intValue() < 150 ? HR_LABEL : " - ", HR_COLUMN_LENGTH));
      }
      if (score.getStudyDescription().startsWith(PSEQ2)) {
        row.addColumn(new TableColumn(score.getScore().intValue() < 6 ? HR_LABEL : " - ", HR_COLUMN_LENGTH));
      }
      if (score.getStudyDescription().startsWith(TSK11)) {
        row.addColumn(new TableColumn( score.getScore().intValue() > 30 ? HR_LABEL : " - ", HR_COLUMN_LENGTH));
      }
    }
    return row;
  }

  private TableRow getColHeader(String title) {
    TableRow colHeader = new TableRow(100);
    colHeader.addColumn(new TableColumn("Date", DATE_COLUMN_WIDTH));
    colHeader.addColumn(new TableColumn( title + " score", 75));
    colHeader.addColumn(new TableColumn(" - ", HR_COLUMN_LENGTH));
    return colHeader;
  }

  @Override
  public String formatExplanationText(Study study, ArrayList<ChartScore> scores) {
    return null;
  }

  @Override
  public ChartInfo createLineChart(ArrayList<ChartScore> stats, XYDataset ds, Study study, ChartConfigurationOptions opts) {
    return null; // not charting
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
    return null; // N/A
  }

  @Override
  public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient) {

    Table table = new Table();
    table.setWidth(100);
    if (patientStudies == null || patientStudies.size() < 1 || study == null || study.getStudyDescription() == null) {
      return table;
    }
    logger.trace("getTable called for {}", study.getStudyDescription());
    if (study.getStudyDescription().startsWith(CPAQ)) {
      return getCPAQtable(patientStudies, study, table);
    }
    try {
      ArrayList<TableRow> scoreRows = new ArrayList<>();
      for (PatientStudyExtendedData patientStudyExtendedData : patientStudies) {
        if (patientStudyExtendedData.getStudyCode().intValue() == study.getStudyCode().intValue()) {
          ArrayList<ChartScore> scores = getScore(patientStudyExtendedData);
          logger.trace("getScore returned {} scores",  scores.size());
          for (ChartScore score : scores) {
            if (scores.size() > 0 && score.getScore() != null && score.getScore().intValue() > -1) {
              scoreRows.add(getScoreRow(score));
            }
          }
        }
      }
      if (scoreRows.size() > 0) {
        table.addRow((getColHeader(study.getTitle() == null ? study.getStudyDescription() : study.getTitle())));
        for (TableRow scoreRow : scoreRows) {
          table.addRow(scoreRow);
        }
      }
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }

    return table;
  }

  @Override
  public int getReportTextFontSize(PrintStudy study) {
    return 11;
  }

  // Make our own because SurveyAdvanceBase is abstract
  static private class SurveyAdvanceUtils extends SurveyAdvanceBase {
    SurveyAdvanceUtils(SiteInfo siteInfo) {
      super(siteInfo);
    }
  }

  private ArrayList<ChartScore> getCPAQscore(PatientStudyExtendedData patientData, ArrayList<ChartScore> scores) {

    CPAQscore cpaqscore = new CPAQscore(patientData.getDtChanged(), patientData.getPatientId(), patientData.getStudyCode(), patientData.getStudyDescription());
    SquareXml squareXml = new SquareXml(dbp.get(), siteInfo, patientData.getStudyDescription(), CPAQ);
    LinkedHashMap<String, String> columns = squareXml.getColumns();
    LinkedHashMap<String, String> references = squareXml.getReferences();
    Object[] refKeys = references.keySet().toArray();
    Survey s = getSurvey(patientData);
    int inx = 0;
    for (String columnName : columns.keySet()) {
      logger.trace("getting " + patientData.getStudyDescription() + " score for {}", columnName);
      String refKey = refKeys[inx].toString();
      String fieldId = references.get(refKey) + ":" + refKey;
      String[] parts = fieldId.split(":");
      try {
        SurveyStep step = s.answeredStepByProviderSectionQuestion(patientData.getSurveySystemId().toString(), patientData.getStudyCode().toString(),
            "Order" + parts[0]);
        Integer score = surveyAdvanceUtils.selectedFieldChoice(step, fieldId);
        if (score != null) {
          logger.trace(" adding score answer {}, {} = {}", parts[0], refKey, score);
          cpaqscore.setAnswer(Integer.parseInt(parts[0]), refKey, new BigDecimal(score));
        }
      } catch (NumberFormatException nfe) {
        logger.error("could not determine " + CPAQ + " score for item {}", parts[0]);
      }
      inx++;
    }
    scores.add(cpaqscore);
    return scores;
  }

  private Table getCPAQtable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Table table) {
    final int SCORE_COLUMN_WIDTH = 25;
    try {
      for (PatientStudyExtendedData patientStudyExtendedData : patientStudies) {
        if (patientStudyExtendedData.getStudyCode().intValue() == study.getStudyCode().intValue()) {
          ArrayList<ChartScore> scores = getScore(patientStudyExtendedData);
          logger.trace("getScore returned {} scores",  scores.size());
          for (ChartScore score : scores) {
            if (score.getScore() != null && score instanceof CPAQscore) {
              CPAQscore cpaqScore = (CPAQscore) score;
              if (table.getRows().size() < 1) {
                TableRow colHeader = new TableRow(100);
                colHeader.addColumn(new TableColumn("Date", DATE_COLUMN_WIDTH));
                colHeader.addColumn(new TableColumn(cpaqScore.getTitle(CPAQscore.ACTIVITY_ENGAGEMENT_SCORE , "AE Score"), SCORE_COLUMN_WIDTH));
                colHeader.addColumn(new TableColumn(cpaqScore.getTitle(CPAQscore.PAIN_WILLINGNESS_SCORE, "PW Score"), SCORE_COLUMN_WIDTH));
                colHeader.addColumn(new TableColumn(cpaqScore.getTitle(CPAQscore.CPAQ_TOTAL_SCORE, "Total Score"), SCORE_COLUMN_WIDTH));
                table.addRow(colHeader);
              }
              int tableWidth = DATE_COLUMN_WIDTH + (3 * SCORE_COLUMN_WIDTH);
              TableRow row = new TableRow(tableWidth);
              row.addColumn(new TableColumn(dateFormatter.format(cpaqScore.getDate()), DATE_COLUMN_WIDTH));
              row.addColumn(new TableColumn(Double.toString(cpaqScore.getScore(CPAQscore.ACTIVITY_ENGAGEMENT_SCORE)), SCORE_COLUMN_WIDTH));
              row.addColumn(new TableColumn(Double.toString(cpaqScore.getScore(CPAQscore.PAIN_WILLINGNESS_SCORE)), SCORE_COLUMN_WIDTH));
              row.addColumn(new TableColumn(Double.toString(cpaqScore.getScore(3)), SCORE_COLUMN_WIDTH));

              table.addRow(row);
            }
          }
        }
      }
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }

    return table;
  }

  private ArrayList<ChartScore> getPAVSscore(PatientStudyExtendedData patientData, ArrayList<ChartScore> scores) {
    Survey s = getSurvey(patientData);
    SurveyStep step = s.answeredStepByProviderSectionQuestion(surveyAdvanceUtils.getProviderId(dbp.get(), patientData.getSurveySystemName()),
        surveyAdvanceUtils.getSectionId(dbp.get(), patientData.getStudyDescription()), "Order0");
    if (step != null) {
      int days = surveyAdvanceUtils.selectedFieldInt(step, "0:0:mod_to_strenuous");
      step = s.answeredStepByProviderSectionQuestion(surveyAdvanceUtils.getProviderId(dbp.get(), "PhysicalTherapyService"),
          surveyAdvanceUtils.getSectionId(dbp.get(), patientData.getStudyDescription()), "Order1");
      if (step != null) {
        int minutes = surveyAdvanceUtils.selectedFieldInt(step, "1:0:minutes_exer");
        PhysicalTherapyScore score = new PhysicalTherapyScore(patientData.getDtChanged(), patientData.getPatientId(),
            patientData.getStudyCode(), patientData.getStudyDescription());
        score.setScore(new BigDecimal(days * minutes)); // minutes per week
        scores.add(score);
      }
    }
    return scores;
  }

  private Survey getSurvey(PatientStudyExtendedData patientData) {

    SurveyDao surveyDao = new SurveyDao(dbp.get());
    SurveyQuery query = new SurveyQuery(dbp.get(), surveyDao, patientData.getSurveySiteId());
    return query.surveyBySurveyToken(patientData.getToken());
  }

  private ArrayList<ChartScore> getSumScore(String prefix, PatientStudyExtendedData patientData, ArrayList<ChartScore> scores) {
    Survey s = getSurvey(patientData);
    String surveyProvider = patientData.getSurveySystemId().toString();
    String sectionId = patientData.getStudyCode().toString();
    SquareXml squareXml = new SquareXml(dbp.get(), siteInfo, patientData.getStudyDescription(), prefix);
    LinkedHashMap<String, String> columns = squareXml.getColumns();
    LinkedHashMap<String, String> references = squareXml.getReferences();
    Object[] refKeys = references.keySet().toArray();
    int inx = 0;

    SurveyAdvanceUtils surveyAdvanceUtils = new SurveyAdvanceUtils(siteInfo);
    PhysicalTherapyScore physicalTherapyScore = new PhysicalTherapyScore(patientData.getDtChanged(), patientData.getPatientId(), patientData.getStudyCode(), patientData.getStudyDescription());

    if (prefix.equals("TSK") || prefix.equals(PSEQ2) || prefix.equals(SEBS)) {
      for (String columnName : columns.keySet()) {
        logger.trace("getting " + patientData.getStudyDescription() + " score for {}", columnName);
        String refKey = refKeys[inx].toString();
        String fieldId = references.get(refKey) + ":" + refKey;
        String[] parts = fieldId.split(":");
        try {
          SurveyStep step = s.answeredStepByProviderSectionQuestion(patientData.getSurveySystemId().toString(), patientData.getStudyCode().toString(),
              "Order" + parts[0]);
          Integer score;
          switch (prefix) {
          case "TSK":
            logger.trace(
                " surveyAdvanceUtils.selectedFieldChoice(step(surveyProvider, {}, Order{}), {}", sectionId, parts[0], fieldId);
            score = surveyAdvanceUtils.selectedFieldChoice(step, fieldId);
            if (score != null) {
              logger.trace(" adding score answer {}, {}", parts[0], score);
              physicalTherapyScore.setAnswer(Integer.parseInt(parts[0]), refKey, new BigDecimal(score));
            }
            break;
          case PSEQ2:
            logger.trace(
                " surveyAdvanceUtils.getRadioIntegerResponse(s, surveyProvider, {}, {}", sectionId, parts[0], parts[0]);
            score = surveyAdvanceUtils.getRadioIntegerResponse(s, surveyProvider, sectionId, parts[0]);
            if (score != null) {
              logger.trace(" adding score answer {}, {}", parts[0], score);
              physicalTherapyScore.setAnswer(Integer.parseInt(parts[0]), refKey, new BigDecimal(score));
            }
            break;
          case SEBS:
            logger.trace(
                " surveyAdvanceUtils.selectedFieldChoice(step(surveyProvider, {}, Order{}), {}", sectionId, parts[0], fieldId);
            score = surveyAdvanceUtils.selectedFieldChoice(step, fieldId);
            if (score != null) {
              logger.trace(" adding {} score answer {}, {}", SEBS, parts[0], score);
              physicalTherapyScore.setAnswer(Integer.parseInt(parts[0]), refKey, new BigDecimal(sebsScale(score)));
            }
            break;

          }
        } catch (NumberFormatException nfe) {
          logger.error("could not determine " + prefix + " score for item {}", parts[0]);
        }
        inx++;
      }

      scores.add(physicalTherapyScore);
    }
    return scores;
  }

  private Integer sebsScale(Integer score) {
    switch (score) {
    case 1:
      return 15;
    case 2:
      return 45;
    case 3:
      return 120;
    case 4:
      return 180;
    default:
      return 0;
    }
  }

  static class CPAQscore extends LocalScore implements MultiScore {
    static final int PAIN_WILLINGNESS_SCORE = 1;
    static final int ACTIVITY_ENGAGEMENT_SCORE = 2;
    static final int CPAQ_TOTAL_SCORE = 3;

    CPAQscore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    @Override
    public int getNumberOfScores() {
      return 3;
    }

    @Override
    public String getTitle(int scoreNumber, String defaultTitle) {
      switch(scoreNumber) {
      case PAIN_WILLINGNESS_SCORE: // Pain Willingness scale = Items 3,4,6 and 7 (reverse scored),
        return "Pain Willingnes scale";
      case ACTIVITY_ENGAGEMENT_SCORE: // Activity Engagement scale = Items 0, 1, 2 and 5
        return "Activity Engagement scale";
      case 3:
        return "CPAQ-8 Total score";
      default:
        return defaultTitle;
      }
    }

    @Override
    public double getScore(int scoreNumber) {
      switch(scoreNumber) {
      case 1:
        return getPainWillingnessScore();
      case 2:
        return getActivityEngagementScore();
      default:
        return (getPainWillingnessScore() + getActivityEngagementScore());
      }
    }

    @Override
    public Map<String, BigDecimal> getScores() {
      HashMap<String, BigDecimal> scores = new HashMap<>();
      if (getScore(PAIN_WILLINGNESS_SCORE) > -1) {
        scores.put("CPAQ_PW_SCORE", new BigDecimal(getScore(PAIN_WILLINGNESS_SCORE)));
      }
      if (getScore(ACTIVITY_ENGAGEMENT_SCORE) > -1) {
        scores.put("CPAQ_AE_SCORE", new BigDecimal(getScore(ACTIVITY_ENGAGEMENT_SCORE)));
      }
      if (getScore(CPAQ_TOTAL_SCORE) > -1) {
        scores.put("CPAQ_TOTAL_SCORE", new BigDecimal(getScore(CPAQ_TOTAL_SCORE)));
      }
      return scores;
    }

    double getPainWillingnessScore() {
      double score = 0;
      String[] pwrefs = { "businessofliving", "normallife", "fulllife", "responsibilities "};
      for (String ref : pwrefs) {
        score += (6 - getAnswer(ref).intValue());
      }
      return score;
    }

    double getActivityEngagementScore() {
      double score = 0;
      String[] aeRefs = {"painundercontrol", "seriousplans", "avoidsituations", "worriesandfears"};
      for (String ref : aeRefs) {
        score += getAnswer(ref).intValue();
      }
      return score;
    }

    @Override
    public Double getPercentileScore(int scoreNumber) {
      return null;
    }
  }

  class PhysicalTherapyScore extends LocalScore {
    PhysicalTherapyScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    @Override
    public Map<String, BigDecimal> getScores() {
      String columnName = null;
      HashMap<String, BigDecimal> scores = new HashMap<>();
      if (getStudyDescription() != null) {
        for (String surveyName : PhysicalTherapyService.surveys) {
          if (surveyName.equals(getStudyDescription()) || getStudyDescription().startsWith(surveyName + "@")) {
            columnName = surveyName + "_SCORE";
          }
        }
      }
      if (columnName != null && getScore() != null) {
        scores.put(columnName, getScore());
      }
      return scores;
    }
  }
}
