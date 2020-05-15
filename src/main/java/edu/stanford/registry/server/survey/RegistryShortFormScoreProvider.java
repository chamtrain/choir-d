/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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
import edu.stanford.registry.server.charts.ChartMaker;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.plugin.ProviderFor;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.tools.CustomDateAxis;
import edu.stanford.registry.server.tools.CustomXYErrorRenderer;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.server.utils.StringUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.GlobalHealthScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.PainIntensityScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.comparator.SurveyQuestionComparator;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.RegistryQuestion;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
import edu.stanford.registry.shared.xform.InputElement;
import edu.stanford.registry.shared.xform.Select1Element;
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;

@ProviderFor(ScoreProvider.class)
public class RegistryShortFormScoreProvider implements ScoreProvider {
  private static final Logger logger = Logger.getLogger(RegistryShortFormScoreProvider.class);
  private final DecimalFormat scoreFormatter = new DecimalFormat("####");
  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yy");

  public static final int INTENSITY_QUESTION_NOW = 3;
  public static final int INTENSITY_QUESTION_AVG = 2;
  public static final int INTENSITY_QUESTION_WORST = 1;
  public static final int INTENSITY_QUESTION_LEAST = 4;

  public static final int PAIN_INTENSITY = 0;
  public static final int BODY_MAP = 1;
  private static final int BODY_MAP_PT = 14;
  private static final int BODY_MAP_PTF = 15;
  public static final int PCS = 3;
  public static final int PROXY_PAIN_INTENSITY = 4;
  public static final int PROXY_PCS = 5;
  public static final int GLOBAL_HEALTH = 6;
  public static final int PEDIATRIC_PCS = 7;
  public static final int OPIOID_RISK = 8;
  public static final int PARENT_GLOBAL_HEALTH = 9;
  public static final int OPIOID_RISK2 = 10;
  public static final int INJUSTICE = 11;
  public static final int CPAQ=12;
  public static final int PCS2=13;

  public static final String[] studies = { "painintensity", "bodymap", "Names", "paincatastrophizingscale", "proxypainintensity", "proxypaincatastrophizingscale",
    "globalhealth", "pedpaincatastrophizingscale", "opioidrisk", "parentglobalhealth", "opioidriskreworded", "injustice", "cpaq", "paincatastrophizingscalev2",
    "bodymappt", "bodymapptf"};
  public static final String MEDS="meds";
  public static final String TREATMENTS="treatments";

  protected final Supplier<Database> dbp;
  protected final SiteInfo siteInfo;

  public RegistryShortFormScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo) {
    this.dbp = dbp;
    this.siteInfo = siteInfo;
  }

  @Override
  public String getDescription() {
    return "Registry Local assessments Score Provider";
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {
    int indx = getStudyIndex(studyName);
    if (indx >= 0 && indx < studies.length) {
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
        int studyIndex = getStudyIndex(patientData.getStudyDescription());
        if ((studyIndex == GLOBAL_HEALTH) || (studyIndex == PARENT_GLOBAL_HEALTH)) {
          chartScore = new GlobalHealthScore(patientData.getDtChanged(), patientData.getPatientId(),
              patientData.getStudyCode(), patientData.getStudyDescription());
        }
        if ((studyIndex == PAIN_INTENSITY) || (studyIndex == PROXY_PAIN_INTENSITY)) {
          chartScore = new PainIntensityScore(patientData.getDtChanged(), patientData.getPatientId(),
              patientData.getStudyCode(), patientData.getStudyDescription());
        }
        NodeList itemsList = doc.getElementsByTagName("Items");
        NodeList itemList = null;
        if (itemsList != null && itemsList.getLength() > 0) {
          Element itemsNode = (Element) itemsList.item(0);
          itemList = itemsNode.getElementsByTagName("Item");
        } else {
          itemList = doc.getElementsByTagName("Item");
        }

        if (itemList != null) {
          for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
            Element itemNode = (Element) itemList.item(itemInx);
            String itemScoreAttrib = itemNode.getAttribute("ItemScore");
            String orderAttrib = itemNode.getAttribute("Order");
            switch (studyIndex) {
            case PAIN_INTENSITY:
            case PROXY_PAIN_INTENSITY:
            case BODY_MAP:
            case BODY_MAP_PT:
            case BODY_MAP_PTF:
              if (itemScoreAttrib != null && !itemScoreAttrib.equals("") && orderAttrib != null
                  && !orderAttrib.equals("")) {
                try {
                  // int response = Integer.parseInt(itemResponseAttrib);
                  int score = Integer.parseInt(itemScoreAttrib);
                  int order = Integer.parseInt(orderAttrib);
                  logger.debug(patientData.getStudyDescription() + "question " + order + "=" + score);
                  chartScore.setAnswer(order, new BigDecimal(score));
                } catch (Exception e) {
                  logger.error("error getting ItemResponse and ItemScore for study "
                      + patientData.getStudyDescription() + " for patient " + patientData.getPatientId());
                  chartScore.setAnswer(0, null);
                }
              }
              break;
            case PCS:
            case PCS2:
            case PROXY_PCS:
            case PEDIATRIC_PCS:
            case INJUSTICE:
            case CPAQ:
                // Scores are cummulative
                RegistryQuestion question = RegistryAssessmentUtils.getQuestion(itemNode, itemInx, false);
                ArrayList<SurveyAnswerIntf> answers = question.getAnswers(true);
                int summaryScore = 0;
              for (SurveyAnswerIntf answer : answers) {
                summaryScore += getItemSelectedScore(answer);
              }
                chartScore.setAnswer(Integer.parseInt(orderAttrib), new BigDecimal(summaryScore));
                break;
            case GLOBAL_HEALTH: {
              // Get each answer
              RegistryQuestion ghQuestion = RegistryAssessmentUtils.getQuestion(itemNode, itemInx, false);
              ArrayList<SurveyAnswerIntf> ghAnswers = ghQuestion.getAnswers(true);
              int answerNumber = Integer.parseInt(orderAttrib);
              for (SurveyAnswerIntf ghAnswer : ghAnswers) {
                int itemScore = getItemSelectedScore(ghAnswer);
                chartScore.setAnswer(answerNumber, new BigDecimal(itemScore));
              }
              // we also need to add in the answer to a question from another survey
              answerNumber ++;
              if (itemInx + 1 == itemList.getLength()) {
                PatStudyDao patStudyDao = new PatStudyDao(dbp.get(), siteInfo);
                ArrayList<PatientStudyExtendedData> intensitySurveys = 
                    patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(
                                        patientData.getSurveyRegId(), studies[PAIN_INTENSITY]);
                if (intensitySurveys != null && intensitySurveys.size() > 0) {
                  ArrayList<ChartScore> intScores = getScore(intensitySurveys.get(0));
                  for (ChartScore intScore : intScores) {
                    LocalScore score = (LocalScore) intScore;
                    int avgPain = score.getAnswer(INTENSITY_QUESTION_AVG).intValue();
                    chartScore.setAnswer(answerNumber, new BigDecimal(avgPain));
                  }
                }
              }

              break;
            }
            case PARENT_GLOBAL_HEALTH: {
              // Get each answer
              final int PARENT_GLOBAL_HEALTH_AVG_PAIN_QUESTION = 10;
              RegistryQuestion ghQuestion = RegistryAssessmentUtils.getQuestion(itemNode, itemInx, false);
              ArrayList<SurveyAnswerIntf> ghAnswers = ghQuestion.getAnswers(true);
              int answerNumber = Integer.parseInt(orderAttrib);
              if (answerNumber == PARENT_GLOBAL_HEALTH_AVG_PAIN_QUESTION) {
                // for the average pain intensity question the score is stored as
                // ItemScore attribute on the question.
                String itemScore = ghQuestion.getAttribute("ItemScore");
                if (itemScore != null) {
                  chartScore.setAnswer(answerNumber, new BigDecimal(Integer.parseInt(itemScore)));
                }
              } else {
                // otherwise the score is the value of the selected item
                for (SurveyAnswerIntf ghAnswer : ghAnswers) {
                  int itemScore = getItemSelectedScore(ghAnswer);
                  chartScore.setAnswer(answerNumber, new BigDecimal(itemScore));
                }
              }
              break;
            }
            case OPIOID_RISK:
            case OPIOID_RISK2:
              RegistryQuestion opQuestion = RegistryAssessmentUtils.getQuestion(itemNode, itemInx, false);
              ArrayList<SurveyAnswerIntf> opAnswers = opQuestion.getAnswers(true);
              int opAnswerNumber = Integer.parseInt(orderAttrib);
              for (SurveyAnswerIntf opAnswer : opAnswers) {
                int itemScore = getItemSelectedScore(opAnswer);
                chartScore.setAnswer(opAnswerNumber, new BigDecimal(itemScore));
              }
              break;
            default:
              break;
            }
          }

        }
        chartScore.setAssisted(patientData.wasAssisted());
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

    logger.debug("getScore returning " + scores.size());
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

    switch (getStudyIndex(study.getStudyDescription())) {
    case PAIN_INTENSITY:
    case PROXY_PAIN_INTENSITY:
      TimeSeries timeDataSetAvg = new TimeSeries("Average");
      TimeSeries timeDataSetNow = new TimeSeries("Worst");
      for (ChartScore stat4 : stats) {
        LocalScore score = (LocalScore) stat4;

        Day day = new Day(score.getDate());
        try {
          // when the same study was taken > once in a day use the later
          // although this shouldn't really happen
          BigDecimal avg = score.getAnswer(INTENSITY_QUESTION_AVG);
          if (avg != null) {
            timeDataSetAvg.addOrUpdate(day, avg);
            logger.debug("Timeseries avg: added(" + day.toString() + "," + avg + ")");
          }
        } catch (SeriesException duplicates) {
        }
        try {
          BigDecimal worst = score.getAnswer(INTENSITY_QUESTION_WORST);
          if (worst != null) {
            timeDataSetNow.addOrUpdate(day, worst);
            logger.debug("Timeseries worst: added(" + day.toString() + "," + worst + ")");
          }
        } catch (SeriesException duplicates) {
        }
      }
      dataset.addSeries(timeDataSetAvg);
      dataset.addSeries(timeDataSetNow);
      return dataset;
    case BODY_MAP:
    case BODY_MAP_PT:
    case BODY_MAP_PTF:
      TimeSeries timeDataSetCnt = new TimeSeries("Areas selected");
      for (ChartScore stat3 : stats) {
        LocalScore score = (LocalScore) stat3;

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
    case PCS:
    case PCS2:
    case PROXY_PCS:
    case PEDIATRIC_PCS:
    case INJUSTICE:
    case CPAQ:
        TimeSeries timeDataSetPcs = new TimeSeries("PCS Score");
      for (ChartScore stat2 : stats) {
        LocalScore score = (LocalScore) stat2;
        ArrayList<BigDecimal> answers = score.getAnswers();
        if (answers != null) {
          Day day = new Day(score.getDate());
          BigDecimal pcsScore = new BigDecimal(0);
          for (BigDecimal answer : answers) {
            pcsScore = answer;
          }
          try {
            timeDataSetPcs.addOrUpdate(day, pcsScore);
          } catch (SeriesException duplicates) {
          }
        }
      }
        dataset.addSeries(timeDataSetPcs);
        return dataset;
    case GLOBAL_HEALTH: // report inverted
    case PARENT_GLOBAL_HEALTH:
        TimeSeries timeDataSetPhysic = new TimeSeries("Physical Health");
        TimeSeries timeDataSetMental = new TimeSeries("Mental Health");
      for (ChartScore stat1 : stats) {
        GlobalHealthScore score = (GlobalHealthScore) stat1;
        ArrayList<BigDecimal> answers = score.getAnswers();
        if (answers != null) {
          Day day = new Day(score.getDate());
          try {
            double physicalScore = score.getPhysicalHealthTScore();
            double mentalScore = score.getMentalHealthTScore();
            if (study.getInvert()) {
              if (physicalScore > 50) {
                physicalScore = 50 - (physicalScore - 50);
              } else if (physicalScore < 50) {
                physicalScore = (50 + (50 - physicalScore));
              }
              if (mentalScore > 50) {
                mentalScore = 50 - (mentalScore - 50);
              } else if (mentalScore < 50) {
                mentalScore = (50 + (50 - mentalScore));
              }
            }

//            If option to chart as percentiles convert
            if (opts.getBooleanOption(ConfigurationOptions.OPTION_CHART_PERCENTILES)) {
              logger.debug("charting (physical/mental) scores " + physicalScore + "/" + mentalScore);
              physicalScore = calculatePercentile(physicalScore);
              mentalScore = calculatePercentile(mentalScore);
              logger.debug("as (physical/mental) percentiles " + physicalScore + "/" + mentalScore);
            }
            timeDataSetPhysic.addOrUpdate(day, physicalScore);
            timeDataSetMental.addOrUpdate(day, mentalScore);
          } catch (SeriesException duplicates) {
          }
        }
      }
        dataset.addSeries(timeDataSetPhysic);
        dataset.addSeries(timeDataSetMental);
        return dataset;
    case OPIOID_RISK:
    case OPIOID_RISK2:
      TimeSeries timeDataSetOR = new TimeSeries("Opioid Risk Score");
      for (ChartScore stat : stats) {
        LocalScore score = (LocalScore) stat;
        ArrayList<BigDecimal> answers = score.getAnswers();
        if (answers != null) {
          Day day = new Day(score.getDate());
          BigDecimal orScore = new BigDecimal(0);
          for (BigDecimal answer : answers) {
            orScore = answer;
          }
          try {
            timeDataSetOR.addOrUpdate(day, orScore);
          } catch (SeriesException duplicates) {
          }
        }
      }
      dataset.addSeries(timeDataSetOR);
      return dataset;
    default: //
      logger.debug("study named " + study.getStudyDescription() + " is not recognized");
      return dataset;
    }
  }

  @Override
  public Table getScoreTable(ArrayList<ChartScore> scores) {
       return null;
  }

  @Override
  public String formatExplanationText(Study study, ArrayList<ChartScore> scores) {
    try {

      String explanation = null;
      if (study != null) {
        explanation = study.getExplanation();
      }

      if (explanation == null) {
        logger.debug("formatExplanationText study has null explanation");
        return explanation;
      }
      if (scores == null || scores.size() < 1) {
        logger.debug("Study " + study.getStudyDescription() + "formatExplanationText called without SCORES");
        return explanation;
      }

      switch (getStudyIndex(study.getStudyDescription())) {
      case PAIN_INTENSITY:
      case PROXY_PAIN_INTENSITY:
        logger.debug("formatExplanationText painIntensity");
        Hashtable<Integer, Double> totals = new Hashtable<>();
        for (int indx = 0; indx < scores.size(); indx++) {
          if (scores.get(indx) instanceof LocalScore) {
            LocalScore lscore = (LocalScore) scores.get(indx);
            ArrayList<Integer> questions = lscore.getQuestions();
            ArrayList<BigDecimal> answers = lscore.getAnswers();
            for (int qIndx = 0; qIndx < questions.size(); qIndx++) {
              Integer question = questions.get(qIndx);
              BigDecimal answer = answers.get(qIndx);
              logger.debug("formatExplanationText question " + question + " = " + answer.toString());
              Double ttl = totals.get(questions.get(qIndx));
              if (ttl == null) {
                ttl = 0.0;
              }
              ttl = ttl + answer.doubleValue();
              totals.put(question, ttl);
              // use the last set of responses in the explanation
              if (indx == (scores.size() - 1)) {
                explanation = StringUtils.replace(explanation, "[LAST_" + question + "]",
                    scoreFormatter.format(answer));
              }
            }
          }
        }

        Enumeration<Integer> totalKeys = totals.keys();
        while (totalKeys.hasMoreElements()) {
          Integer question = totalKeys.nextElement();
          Double answerTotal = totals.get(question);
          Double average = answerTotal / scores.size();
          explanation = StringUtils.replace(explanation, "[AVG_" + question + "]", scoreFormatter.format(average));
          logger.debug("formatExplanationText question " + question + " had total of " + answerTotal + " with "
              + scores.size() + " surveys for avg of " + average);
        }
        break;
      case BODY_MAP:
      case BODY_MAP_PT:
      case BODY_MAP_PTF:
        Double ttl = 0.0;
        int indx = scores.size() - 1;
        if (scores.get(indx) instanceof LocalScore) {
          LocalScore lscore = (LocalScore) scores.get(indx);
          ArrayList<BigDecimal> answers = lscore.getAnswers();
          for (BigDecimal answer : answers) {
            ttl = ttl + answer.doubleValue();
          }
        }
        explanation = StringUtils.replace(explanation, "[LAST]", scoreFormatter.format(ttl.intValue()));
        break;
      default:
        break;
      }
      return explanation;
    } catch (Exception e) {
      logger.error("error", e);
      return (e.getMessage());
    }
  }

  @Override
  public ChartInfo createLineChart(ArrayList<ChartScore> stats, XYDataset ds, Study study,
                                   ChartConfigurationOptions opts) {

    ArrayList<Study> studies = new ArrayList<>();
    studies.add(study);

    ChartInfo chartInfo = new ChartInfo("Local", stats, ds, study);
    final JFreeChart chart = new JFreeChart(study.getTitle(), ChartMaker.getFont(opts, Font.PLAIN,
        ChartMaker.TITLEFONTSIZE), getPlot(chartInfo, studies, opts), true);
    chart.getLegend().setItemFont(ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.LEGENDFONTSIZE));
    LegendTitle legend1 = chart.getLegend(1);
    if (legend1 != null) {
      legend1.visible = true;
    }
    chart.getLegend().setItemPaint(Color.black);
    switch (getStudyIndex(study.getStudyDescription())) {
    case BODY_MAP:
    case BODY_MAP_PT:
    case BODY_MAP_PTF:
      chart.removeLegend();
      break;
    default:
      break;
    }

    chartInfo.setChart(chart);
    return chartInfo;
  }


  public int getStudyIndex(String studyDescription) {
    if (studyDescription != null) {
      for (int indx = 0; indx < studies.length; indx++) {
        if (studies[indx].equals(studyDescription.toLowerCase())) {
          return indx;
        }
      }
    }
    return -1;
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study,
                                                 Patient patient, boolean allAnswers) {
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
  public ArrayList<SurveyQuestionIntf> getSurvey(PatientStudyExtendedData patStudy, PrintStudy study,
			Patient patient, boolean allAnswers) {
    if (patStudy == null) {
      return new ArrayList<>();
    }
    String xmlDocumentString = patStudy.getContents();
    if (xmlDocumentString == null) {
      // get the file
      xmlDocumentString = XMLFileUtils.getInstance(siteInfo).getXML(dbp.get(), study.getStudyDescription());
      logger.debug("read xml " + xmlDocumentString);
    }
    ArrayList<SurveyQuestionIntf> questionIntfs = getSurveyInternal(patStudy, xmlDocumentString, patient, allAnswers);
    // If requesting only answered responses and there is a print version defined for the survey
    if (!allAnswers && study.getPrintVersion() != null && study.getPrintVersion().length() > 0 &&
        !study.getPrintVersion().equals(study.getStudyDescription())) {
      // Get the print version of the questionnaire to use its descriptions and labels text for reporting results
      xmlDocumentString = XMLFileUtils.getInstance(siteInfo).getXML(dbp.get(), study.getPrintVersion());
      ArrayList<SurveyQuestionIntf> printQuestionIntfs = getSurveyInternal(patStudy, xmlDocumentString, patient, true);
      for (SurveyQuestionIntf surveyQuestionIntf : questionIntfs) {
        SurveyQuestionIntf printQuestionIntf = null;
        for (SurveyQuestionIntf questionIntf : printQuestionIntfs) {
          if (surveyQuestionIntf.getAttribute("Order") != null &&
              surveyQuestionIntf.getAttribute("Order").equals(questionIntf.getAttribute("Order"))) {
            printQuestionIntf = questionIntf;
          }
        }
        if (printQuestionIntf != null) {
          while (surveyQuestionIntf.getText().size() > 0) { // replace the question text
            surveyQuestionIntf.getText().remove(0);
          }
          for (String text : printQuestionIntf.getText()) { // with the print version text
            surveyQuestionIntf.getText().add(text);
          }
          ArrayList<SurveyAnswerIntf> surveyAnswerIntfs = surveyQuestionIntf.getAnswers();
          ArrayList<SurveyAnswerIntf> printAnswerIntfs = printQuestionIntf.getAnswers();
          int surveyAnswerIntfInx = 0;
          for (SurveyAnswerIntf surveyAnswerIntf : surveyAnswerIntfs) {
            SurveyAnswerIntf printAnswerIntf = null;
            for (SurveyAnswerIntf answerIntf : printAnswerIntfs) {
              if (surveyAnswerIntf.getClientId() != null
                  && surveyAnswerIntf.getClientId().equals(answerIntf.getClientId())) {
                printAnswerIntf = answerIntf;
              }
            }

            if (printAnswerIntf != null) {
              // copy the answers onto the print version and make the print version the answer for the question
              if (surveyAnswerIntf.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_RADIO) {
                // nothing needed. the answer is numeric.
              } else if (surveyAnswerIntf.getType() == Constants.TYPE_SELECT1
                  || surveyAnswerIntf.getType() == Constants.TYPE_DROPDOWN) {
                replaceSelectAnswers((Select1Element) surveyAnswerIntf, (Select1Element) printAnswerIntf);
                surveyQuestionIntf.getAnswers().set(surveyAnswerIntfInx, printAnswerIntf);
              } else if ((surveyAnswerIntf.getType() == Constants.TYPE_SELECT
                  || surveyAnswerIntf.getType() == Constants.TYPE_RADIOSETGRID)
                  && surveyAnswerIntf instanceof SelectElement && printAnswerIntf instanceof SelectElement) {
                SelectElement surveyAnswer = (SelectElement) surveyAnswerIntf;
                SelectElement printAnswer = (SelectElement) printAnswerIntf;
                HashMap<String, String> xAxisMap = new HashMap<>();
                for (int iInx = 0; iInx < printAnswer.getItems().size(); iInx++) {
                  if ("x-axis".equals(printAnswer.getItems().get(iInx).getGroup())) {
                    xAxisMap.put(printAnswer.getItems().get(iInx).getValue(), printAnswer.getItems().get(iInx).getLabel() );
                  }
                }
                replaceSelectAnswers(surveyAnswer, printAnswer);
                for (int iInx = 0; iInx < surveyAnswer.getItems().size(); iInx++) {
                  if (hasGroup(surveyAnswer.getItems().get(iInx), "y-axis")) {
                    printAnswer.getItems().get(iInx).setLabel(printAnswer.getItems().get(iInx).getLabel()
                    + " : " + xAxisMap.get(surveyAnswer.getItems().get(iInx).getValue()));
                    printAnswer.getItems().get(iInx).setSelected(surveyAnswer.getItems().get(iInx).getSelected());
                  }
                }
                for (int inx = printAnswer.getItems().size(); inx > 0; inx --) {
                  if (!printAnswer.getItems().get(inx - 1).getSelected() ) {
                    if (hasGroup(printAnswer.getItems().get(inx -1), "x-axis")) {
                      printAnswer.getItems().remove(inx -1);
                    }
                  }
                }
                surveyQuestionIntf.getAnswers().set(surveyAnswerIntfInx, printAnswerIntf);
              } else if ((surveyAnswerIntf.getType() == Constants.TYPE_DATEPICKER
                  || surveyAnswerIntf.getType() == Constants.TYPE_INPUT)
                  && surveyAnswerIntf instanceof InputElement && printAnswerIntf instanceof InputElement) {
                InputElement surveyElement = (InputElement) surveyAnswerIntf;
                InputElement printElement = (InputElement) printAnswerIntf;
                printElement.setValue(surveyElement.getValue());
                surveyQuestionIntf.getAnswers().set(surveyAnswerIntfInx, printElement);
              } else if ((surveyAnswerIntf.getType() == Constants.TYPE_TEXTBOXSET
                  && surveyAnswerIntf instanceof SelectElement
                  && printAnswerIntf instanceof SelectElement)) {

                SelectElement surveyAnswer = (SelectElement) surveyAnswerIntf;
                SelectElement printAnswer = (SelectElement) printAnswerIntf;
                ArrayList<SelectItem> selectItemArrayList = surveyAnswer.getItems();
                ArrayList<SelectItem> printItemArrayList = printAnswer.getItems();
                printAnswer.setSelected(surveyAnswer.getSelected());
                if (selectItemArrayList != null && printItemArrayList != null &&
                    selectItemArrayList.size() == printItemArrayList.size()) {
                  for (int inx = 0; inx < selectItemArrayList.size(); inx++) {
                    printItemArrayList.get(inx).setSelected(true);
                    if (selectItemArrayList.get(inx).getLabel() != null && selectItemArrayList.get(inx).getLabel().contains("[")) {
                      int startStr = selectItemArrayList.get(inx).getLabel().indexOf("[");
                      printItemArrayList.get(inx).setLabel(printItemArrayList.get(inx).getLabel() + " " +
                          selectItemArrayList.get(inx).getLabel().substring(startStr));
                      printItemArrayList.get(inx).setSelected(selectItemArrayList.get(inx).getSelected());
                    }
                  }
                }
                surveyQuestionIntf.getAnswers().set(surveyAnswerIntfInx, printAnswer);
              }
            }
            surveyAnswerIntfInx++;
          }
        }
      }
    }
    return questionIntfs;
  }

  private void replaceSelectAnswers(SelectElement selectElement, SelectElement printElement) {
    selectElement.setLabel(printElement.getLabel());
    ArrayList<SelectItem> selectItemArrayList = selectElement.getItems();
    for (int inx=0; inx<selectItemArrayList.size(); inx++) {
      SelectItem item = selectItemArrayList.get(inx);
      if (item.getSelected()) {
        printElement.getItems().get(inx).setSelected(true);
        printElement.getResponse().add(printElement.getItems().get(inx).getLabel());
      }
    }
  }

  private boolean hasGroup(SelectItem item, String group) {
    return item != null && item.getGroup()!= null && item.getGroup().equals(group);
  }

  private ArrayList<SurveyQuestionIntf> getSurveyInternal(PatientStudyExtendedData patStudy, String xmlDocumentString, Patient patient, boolean allAnswers) {

    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    try {
      db = dbf.newDocumentBuilder();

      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));

      Document messageDom = db.parse(is);
      Element docElement = messageDom.getDocumentElement();
      if (docElement.getTagName().equals(Constants.FORM)) {
        int index = 0;
        Element itemsNode = (Element) messageDom.getElementsByTagName(Constants.ITEMS).item(0);
        NodeList itemList = itemsNode.getElementsByTagName(Constants.ITEM);
        for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
          Element itemNode = (Element) itemList.item(itemInx);
          // check if conditional
          boolean meets = RegistryAssessmentUtils.qualifies(patient, itemNode, xmlDocumentString);
          // If they qualify for this question
          if (meets) {
            SurveyQuestionIntf question = RegistryAssessmentUtils.getQuestion(itemNode, index, allAnswers);
            questions.add(question);
            index++;
          }
        }
      }
    } catch (ParserConfigurationException | IOException | SAXException e) {
      logger.error(
          "Error parsing xml for patientStudy token " + patStudy.getToken() + " study " + patStudy.getStudyCode(), e);
    }

    return questions;
  }

  @Override
  public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient) {
	  ArrayList<SurveyQuestionIntf> questions;
	    Table table = new Table();
	    ArrayList<String> headings = new ArrayList<>();
    switch (getStudyIndex(study.getStudyDescription())) {
    case PAIN_INTENSITY:
    case PROXY_PAIN_INTENSITY:
      headings.add(study.getTitle() + ":  0=No Pain, 10=Worst Pain Imaginable");
      table.setHeadings(headings);
      TableRow qRow = new TableRow();
      qRow.addColumn(new TableColumn("Worst", 25));
      qRow.addColumn(new TableColumn("Average", 25));
      qRow.addColumn(new TableColumn("Now", 25));
      TableRow aRow = new TableRow();
      questions = getSurvey(patientStudies, study, patient, false);
      if (questions.size() > 3) {
        qRow.addColumn(new TableColumn("Least", 25));
      }

      for (int q = 0; q < questions.size(); q++) {
        if (questions.get(q) != null && questions.get(q).getAnswered()) {
          ArrayList<SurveyAnswerIntf> ans = questions.get(q).getAnswers();
           String answerStr = null;
          if (ans != null && ans.size() > 0) {
            for (SurveyAnswerIntf an : ans) {
              if (an != null && an.getSelected()) {
                answerStr = an.getAttribute(Constants.DESCRIPTION);
              }
            }
          }
          if (answerStr != null) {
            aRow.addColumn(new TableColumn(answerStr.toString(), 25));
          } else {
            logger.debug("no answer for pain intensity question " + q);
          }
        }
      }
      table.addRow(qRow);
      table.addRow(aRow);
      break;
    case PCS:
    case PCS2:
    case PROXY_PCS:
    case PEDIATRIC_PCS:
        headings.add(study.getTitle());
        table.setHeadings(headings);
        PatientStudyExtendedData patStudy = null;
      for (PatientStudyExtendedData patientStudy3 : patientStudies) {
        if (patientStudy3.getStudyCode().intValue() == study.getStudyCode().intValue()) {
          patStudy = patientStudy3;
          questions = getSurvey(patStudy, study, patient, false);
          int pcsScore = 0;
          for (SurveyQuestionIntf question : questions) {
            ArrayList<SurveyAnswerIntf> ans = question.getAnswers(true);
            for (SurveyAnswerIntf an : ans) {
              pcsScore += getItemSelectedScore(an);
            }
          }
          TableRow row = new TableRow(100);
          row.setColumnGap(3);
          row.addColumn(new TableColumn(dateFormatter.format(patStudy.getDtChanged()), 65));
          row.addColumn(new TableColumn(pcsScore + "", 31));
          table.addRow(row);
        }
      }
        break;
    case GLOBAL_HEALTH:
    case PARENT_GLOBAL_HEALTH:
      // get the last one of this study type
      PatientStudyExtendedData ghStudy = null;
      for (PatientStudyExtendedData patientStudy2 : patientStudies) {
        if (patientStudy2.getStudyCode().intValue() == study.getStudyCode().intValue()) {
          ghStudy = patientStudy2;
        }
      }
      if (ghStudy != null) {
        ArrayList<ChartScore> scores = getScore(ghStudy);
        table.addHeading("Global Health");
        TableRow ghHeadingRow = new TableRow(500);
        ghHeadingRow.addColumn(new TableColumn("Date", 20));
        ghHeadingRow.addColumn(new TableColumn("Physical Health Score", 20));
        ghHeadingRow.addColumn(new TableColumn("%ile", 20));
        ghHeadingRow.addColumn(new TableColumn("Mental Health Score", 20));
        ghHeadingRow.addColumn(new TableColumn("%ile", 20));
        table.addRow(ghHeadingRow);

        for (ChartScore score1 : scores) {
          GlobalHealthScore score = (GlobalHealthScore) score1;
          TableRow ghRow = new TableRow(100);
          ghRow.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
          Double physicalScore = score.getPhysicalHealthTScore();
          ghRow.addColumn(new TableColumn(physicalScore.toString(), 20));
          ghRow.addColumn(new TableColumn(calculatePercentile(physicalScore).toString(), 20));
          Double mentalScore = score.getMentalHealthTScore();
          ghRow.addColumn(new TableColumn(mentalScore.toString(), 20));
          ghRow.addColumn(new TableColumn(calculatePercentile(mentalScore).toString(), 20));
          table.addRow(ghRow);
        }
      }
    case OPIOID_RISK:
    case OPIOID_RISK2:
      // get the last one of this study type
      PatientStudyExtendedData ortStudy = null;
      for (PatientStudyExtendedData patientStudy1 : patientStudies) {
        if (patientStudy1.getStudyCode().intValue() == study.getStudyCode().intValue()) {
          ortStudy = patientStudy1;
        }
      }
      if (ortStudy != null) {
        ArrayList<ChartScore> scores = getScore(ortStudy);
        table.addHeading("Opioid Risk");
        TableRow orHeadingRow = new TableRow(500);
        orHeadingRow.addColumn(new TableColumn("Date", 20));
        orHeadingRow.addColumn(new TableColumn("Score", 20));
        orHeadingRow.addColumn(new TableColumn("Risk", 30));
        table.addRow(orHeadingRow);
        BigDecimal riskScore = new BigDecimal(0);
        for (ChartScore score1 : scores) {
          LocalScore score = (LocalScore) score1;
          TableRow row = new TableRow(100);
          row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
          ArrayList<BigDecimal> answers = score.getAnswers();
          if (answers != null && answers.size() > 0) {
            for (BigDecimal answer : answers) {
              riskScore = riskScore.add(answer);
            }
          }
          row.addColumn(new TableColumn(riskScore.toBigInteger().toString() + "", 20));
          String risk = "Low";
          if (riskScore.intValue() > 7) {
            risk = "High";
          } else if (riskScore.intValue() > 3) {
            risk = "Moderate";
          }
          row.addColumn(new TableColumn(risk, 30));
          table.addRow(row);
        }
      }
      break;
    default:
      if (study.getStudyDescription().startsWith(TREATMENTS) || study.getStudyDescription().startsWith(MEDS)) {
        headings.add(study.getTitle());
        patStudy = null;
        /* Find the last one */
        for (PatientStudyExtendedData patientStudy : patientStudies) {
          if (patientStudy.getStudyCode().intValue() == study.getStudyCode().intValue()) {
            patStudy = patientStudy;
          }
        }
        if (patStudy != null) {
          questions = getSurvey(patStudy, study, patient, false);
          Collections.sort(questions, new SurveyQuestionComparator<SurveyQuestionIntf>());
          TableRow row = new TableRow(100);
          for (SurveyQuestionIntf question1 : questions) {
            boolean conditional = false;
            SurveyQuestionIntf question = question1;
            String ref = question.getAttribute(Constants.XFORM_REF);
            if (ref != null && Constants.ACTION_ONSELECT.equals(ref)) {
              conditional = true;
            }
            ArrayList<SurveyAnswerIntf> ans = question.getAnswers(true);
            if (ans != null && ans.size() > 0) {
              logger.debug(
                  study.getStudyDescription() + " question:" + question.getAttribute(Constants.ORDER) + " ref: " + ref
                      + " conditional is " + conditional + " and has " + ans.size() + " answers ");
              SurveyAnswerIntf answer = ans.get(0);
              if (answer.getType() == Constants.TYPE_SELECT ||
                  answer.getType() == Constants.TYPE_SELECT1) {
                logger.debug(
                    study.getStudyDescription() + " question:" + question.getAttribute(Constants.ORDER) + " ref: " + ref
                        + " is a select");
                SelectElement select = (SelectElement) answer;
                ArrayList<SelectItem> selectedItems = select.getSelectedItems();
                logger.debug(
                    study.getStudyDescription() + " question:" + question.getAttribute(Constants.ORDER) + " ref: " + ref
                        + " has " + selectedItems.size() + " selectedItems");
                StringBuilder strBuf = new StringBuilder();
                String sep = "";
                for (SelectItem item : selectedItems) {
                  String displayText = conditional ? item.getLabel() : item.getValue();
                  strBuf.append(sep).append(displayText);
                  sep = ", ";
                }
                if (strBuf.length() > 0) {
                  if (!conditional || row.getColumns().size() > 0) {
                    row.addColumn(new TableColumn(strBuf.toString(), 50));
                    logger.debug(" row has " + row.getColumns().size() + " columns");
                  } else {
                    if (conditional) {
                      if (row.getColumns().size() > 0) {
                        logger.debug("Huh?");
                      } else {
                        logger.debug(" skipping because conditional responses but no columns in table ");
                      }
                    }
                  }
                }
              } else {
                logger.debug(
                    study.getStudyDescription() + " question:" + question.getAttribute(Constants.ORDER) + " ref: " + ref
                        + " not a select");
              }
            }
            /**
             * if on the conditional then we've processed both and can add the row.
             */
            if (conditional && row.getColumns().size() > 0) {
              table.addRow(row);
              row = new TableRow(100);
            }
          }
        }
        if (table.getRows().size() > 0) {
          table.setHeadings(headings);
        }
      }
      break;
    }
    return table;
  }

  @Override
  public XYPlot getPlot(ChartInfo chartInfo, //ArrayList<ChartScore> stats, XYDataset ds,
                        ArrayList<Study> studies,
                        ChartConfigurationOptions opts) {
    logger.debug("using CustomXYErrorRenderer");
    CustomXYErrorRenderer renderer = new CustomXYErrorRenderer(chartInfo, true, true);
    return getPlot(renderer, chartInfo.getDataSet(), studies, opts);
  }

  public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                         ChartConfigurationOptions opts) {
    final NumberAxis rangeAxis = new NumberAxis("Score");
    if (studies == null || studies.size() < 1 || ds == null) {
      return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
    }
    int study = getStudyIndex(studies.get(0).getStudyDescription());
    TimeSeriesCollection collection = (TimeSeriesCollection) ds;
    final CustomDateAxis domainAxis = new CustomDateAxis(collection);
    Font labelFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.LABELFONTSIZE);
    Font dtTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.DTTICKFONTSIZE);
    domainAxis.setLabelFont(labelFont);
    domainAxis.setTickLabelPaint(Color.black);
    domainAxis.setTickLabelFont(dtTickFont);
    domainAxis.setTickLabelInsets(new RectangleInsets(5, 60, 5, 5));
    Font numTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.NUMTICKFONTSIZE);
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    rangeAxis.setLabelFont(labelFont);
    rangeAxis.setTickLabelFont(numTickFont);
    switch (study) {
    case PAIN_INTENSITY:
    case PROXY_PAIN_INTENSITY:
      rangeAxis.setTickUnit(new NumberTickUnit(1));
      rangeAxis.setRange(0, 10);
      break;
    case BODY_MAP:
    case BODY_MAP_PT:
    case BODY_MAP_PTF:
      rangeAxis.setTickUnit(new NumberTickUnit(15));
      rangeAxis.setRange(0, 75);
      rangeAxis.setLabel("Regions");
      renderer.setBaseLinesVisible(true);
      break;
    case GLOBAL_HEALTH:
    case PARENT_GLOBAL_HEALTH:
      rangeAxis.setTickUnit(new NumberTickUnit(10));
      rangeAxis.setRange(0, 100);
      break;
    default: // PCS
      rangeAxis.setTickUnit(new NumberTickUnit(5));
      rangeAxis.setRange(0, 55);
      break;
    }
    ChartMaker.setRendererOptions(renderer, opts, ds.getSeriesCount());
    final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
    plot.setDomainGridlinePaint(Color.black);
    plot.setDomainGridlinesVisible(false);
    plot.setRangeGridlinePaint(Color.black);
    plot.setBackgroundPaint(Color.white);
    plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
    plot.setOutlineStroke(new BasicStroke(0.0f));
    plot.setInsets(ChartMaker.CHART_INSETS);
    /*
     * Add shading to the chart
     */
    float begClr, midClr, endClr;
    switch (study) {
    case BODY_MAP:
    case BODY_MAP_PT:
    case BODY_MAP_PTF:
      begClr = 25;
      midClr = 50;
      endClr = 75;
      break;
    case GLOBAL_HEALTH:
    case PARENT_GLOBAL_HEALTH:
      begClr = 70;
      midClr = 85;
      endClr = 100;
      break;
    default:
      begClr = 6;
      midClr = 8;
      endClr = 10;
      break;
    }
    final IntervalMarker marker3 = new IntervalMarker(begClr, midClr,
        opts.getOptionColor(ConfigurationOptions.OPTION_BANDING_COLOR_2));
    final IntervalMarker marker4 = new IntervalMarker(midClr, endClr,
        opts.getOptionColor(ConfigurationOptions.OPTION_BANDING_COLOR_1));
    plot.addRangeMarker(marker3, Layer.BACKGROUND);
    plot.addRangeMarker(marker4, Layer.BACKGROUND);
    final IntervalMarker targetHigh = new IntervalMarker(midClr, endClr);
    targetHigh.setLabel("Worse");
    targetHigh.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
    targetHigh.setLabelAnchor(RectangleAnchor.LEFT);
    targetHigh.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
    targetHigh.setPaint(new Color(255, 255, 255, 100));
    plot.addRangeMarker(targetHigh, Layer.BACKGROUND);
    return plot;
  }

  private int getItemSelectedScore(SurveyAnswerIntf answer) {
    int score = 0;
    if (answer.getType() == Constants.TYPE_SELECT ||
        answer.getType() == Constants.TYPE_SELECT1) {
      SelectElement select = (SelectElement) answer;
      ArrayList<SelectItem> selectItems = select.getSelectedItems();
      for (SelectItem selectItem1 : selectItems) {
        try {
          SelectItem selectItem = selectItem1;
          score += Integer.parseInt(selectItem.getValue());
        } catch (NumberFormatException nfe) {
          logger.error("error getting " + PCS
              + " score, invalid numericValue");
        }
      }
    }
    return score;
  }

  public Long calculatePercentile(Double score) {
    NormalDistribution norm = new NormalDistribution(50, 10);
    Double percentile = norm.cumulativeProbability(score) * 100;
    return Math.round(percentile);
  }

  @Override
  public int getReportTextFontSize(PrintStudy study) {
    final int defaultFontSize = 11;
    int studyInx  = getStudyIndex(study.getStudyDescription());
    switch (studyInx) {
    case GLOBAL_HEALTH:
    case PARENT_GLOBAL_HEALTH:
      return getPromisReportTextFontSize(study, defaultFontSize);
    }
    return defaultFontSize;
  }


  public int getPromisReportTextFontSize(PrintStudy study, int defaultFontSize) {
     String size = siteInfo.getProperty(Constants.PROMIS_QUESTION_REPORT_FONTSIZE);
    if (size != null && size.length() > 0) {
      try {
        return Integer.parseInt(size);
      } catch (Exception e) {}
    }
    return defaultFontSize;
  }

}
