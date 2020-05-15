package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.tools.CustomXYErrorRenderer;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.server.utils.XmlFormatter;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MedsOpioid4ScoreProvider implements ScoreProvider {
  private static Logger logger = Logger.getLogger(MedsOpioid4ScoreProvider.class);
  @Override
  public String getDescription() {
    return "Score provider for  " + MedsOpioid4SurveyService.studyName + " survey";
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {
    if (MedsOpioid4SurveyService.studyName.equals(studyName)) {
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
      Document doc = ScoreService.getDocument(patientData);
      if (doc == null) {
          return scores;
      }
      Element docElement = doc.getDocumentElement();

      if (docElement.getTagName().equals("Form")) {
        LocalScore chartScore = new LocalScore(patientData.getDtChanged(), patientData.getPatientId(),
            patientData.getStudyCode(), patientData.getStudyDescription());
        chartScore.setAssisted(patientData.wasAssisted());
        NodeList itemList = doc.getElementsByTagName("Item");
        if (itemList != null) {
          for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
            Element itemNode = (Element) itemList.item(itemInx);
            String itemScoreAttrib = itemNode.getAttribute("ItemScore");
            String orderAttrib = itemNode.getAttribute("Order");
            if (itemScoreAttrib != null && !itemScoreAttrib.equals("") && orderAttrib != null
                  && !orderAttrib.equals("")) {
            try {
               // int response = Integer.parseInt(itemResponseAttrib);
               int score = Integer.parseInt(itemScoreAttrib);
               int order = Integer.parseInt(orderAttrib);
               chartScore.setAnswer(order, new BigDecimal(score));
             } catch (Exception e) {
                  logger.error("error getting ItemResponse and ItemScore for study "
                      + patientData.getStudyDescription() + " for patient " + patientData.getPatientId());
                  chartScore.setAnswer(0, null);
                }
              }

            }
          }

          scores.add(chartScore);
        }

    } catch (IOException ioe) {
      logger.error(
          "IOException parsing xml for patient " + patientData.getPatientId() + " study " + patientData.getStudyCode(),
          ioe);
    } catch (ParserConfigurationException pe) {
      logger.error(
          "ParserException parsing xml for patient " + patientData.getPatientId() + " study "
              + patientData.getStudyCode(), pe);
    } catch (SAXException se) {
      logger
          .error(
              "SAXException parsing xml for patient " + patientData.getPatientId() + " study "
                  + patientData.getStudyCode(), se);
    }
    return scores;
  }

  @Override
  public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> scores, PrintStudy study,
      ChartConfigurationOptions opts) {
    final TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(baseLineSeries);
    if (scores == null || study == null) {
      return dataset;
    }
    TimeSeries timeDataSetCnt = new TimeSeries("Areas selected");
    for (ChartScore score1 : scores) {
      LocalScore score = (LocalScore) score1;

      ArrayList<BigDecimal> answers = score.getAnswers();
      if (answers != null) {
        Day day = new Day(score.getDate());
        Double ttl = 0.0;
        for (BigDecimal answer : answers) {
          ttl = ttl + answer.doubleValue();
        }

        try {
          timeDataSetCnt.addOrUpdate(day, ttl);
        } catch (SeriesException duplicates) {
        }
      }
    }
    dataset.addSeries(timeDataSetCnt);
    return dataset;
  }

  @Override
  public Table getScoreTable(ArrayList<ChartScore> scores) {
    return null;
  }

  @Override
  public String formatExplanationText(Study study, ArrayList<ChartScore> scores) {
    return null;
  }

  @Override
  public ChartInfo createLineChart(ArrayList<ChartScore> stats, XYDataset ds, Study study,
      ChartConfigurationOptions opts) {
    return null;
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study,
      Patient patient, boolean allAnswers) {

    if (study == null || patientStudies == null || patient == null) {
      return new ArrayList<>();
    }
    PatientStudyExtendedData patStudy = null;
    for (PatientStudyExtendedData pStudy : patientStudies) {
      if (study.getStudyCode().intValue() == pStudy.getStudyCode().intValue()) {
        patStudy = pStudy;
      }
    }
    if (patStudy == null || patStudy.getContents() == null) {
      return new ArrayList<>();
    }
    return getSurvey(patStudy, study, patient, allAnswers);
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(PatientStudyExtendedData patStudy, PrintStudy study, Patient patient,
      boolean allAnswers) {
    if (study == null || patStudy == null || patient == null) {
      return new ArrayList<>();
    }

    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
    try {
      NodeList itemList = XmlFormatter.getNodeList(patStudy.getContents(), Constants.ITEM);
      for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
        Element itemNode = (Element) itemList.item(itemInx);
        SurveyQuestionIntf question = RegistryAssessmentUtils.getQuestion(itemNode, Integer.parseInt(itemNode.getAttribute(Constants.ORDER)), allAnswers);
        questions.add(question);
      }
  } catch (Exception e) {
    logger.error(
        "Error parsing xml for patientStudy token " + patStudy.getToken() + " study " + patStudy.getStudyCode(), e);
  }

  return questions;
  }

  @Override
  public XYPlot getPlot(ChartInfo chartInfo, ArrayList<Study> studies, ChartConfigurationOptions opts) {
    logger.debug("using CustomXYErrorRenderer");
    CustomXYErrorRenderer renderer = new CustomXYErrorRenderer(chartInfo, true, true);
    return getPlot(renderer, chartInfo.getDataSet(), studies, opts);
  }

  private XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                         ChartConfigurationOptions opts) {
    final NumberAxis rangeAxis = new NumberAxis("Score");
    return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
  }

  @Override
  public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient) {
    ArrayList<SurveyQuestionIntf> questions = getSurvey(patientStudies, study, patient, true);
    Table table = new Table();
    if (questions.size() < 1) {
      return table;
    }
    ArrayList<String> headings = new ArrayList<>();
    //headings.add(questions.get(0).getText().get(0));
    table.setHeadings(headings);
    questions = getSurvey(patientStudies, study, patient, false);
    for (SurveyQuestionIntf question : questions) {
      TableRow row = new TableRow();
      StringBuilder questionStr = new StringBuilder();
      ArrayList<String> questionText = question.getText();
      for (String qText : questionText) {
        questionStr.append(qText).append(" ");
      }
      row.addColumn(new TableColumn(questionStr.toString(), 50));
      ArrayList<SurveyAnswerIntf> answers = question.getAnswers();
      StringBuilder answerStr = new StringBuilder();
      for (SurveyAnswerIntf answer : answers) {
        ArrayList<String> answerText = answer.getText();
        for (String aText : answerText) {
          answerStr.append(aText).append(" ");
        }
      }
      row.addColumn(new TableColumn(answerStr.toString(), 50));
      table.addRow(row);
    }

    return table;
  }

  @Override
  public int getReportTextFontSize(PrintStudy study) {
    return 11;
  }

}
