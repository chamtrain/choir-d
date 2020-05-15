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

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.charts.ChartMaker;
import edu.stanford.registry.server.database.Database;
import edu.stanford.registry.server.plugin.ProviderFor;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.service.Provider;
import edu.stanford.registry.server.tools.CustomDateAxis;
import edu.stanford.registry.server.tools.CustomXYErrorRenderer;
import edu.stanford.registry.server.utils.DataBaseUtils;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.server.utils.StringUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.*;
import edu.stanford.registry.shared.comparator.SurveyQuestionComparator;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.*;
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;
import edu.ufl.registry.server.service.hl7message.HL7Generator;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

@ProviderFor(ScoreProvider.class)
public class RegistryShortFormScoreProvider implements ScoreProvider {
  private static Logger logger = Logger.getLogger(RegistryShortFormScoreProvider.class);
  private final DecimalFormat scoreFormatter = new DecimalFormat("####");
  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yy");

  public static final int INTENSITY_QUESTION_NOW = 3;
  public static final int INTENSITY_QUESTION_AVG = 2;
  public static final int INTENSITY_QUESTION_WORST = 1;
  public static final int INTENSITY_QUESTION_LEAST = 4;

  public static final int PAIN_INTENSITY = 0;
  public static final int BODY_MAP = 1;
  public static final int NAMES = 2;
  public static final int PCS = 3;
  public static final int PROXY_PAIN_INTENSITY = 4;
  public static final int PROXY_PCS = 5;
  public static final int GLOBAL_HEALTH = 6;
  public static final int PEDIATRIC_PCS = 7;
  public static final int OPIOD_RISK = 8;
  public static final int PARENT_GLOBAL_HEALTH = 9;
  public static String[] studies = { "painintensity", "bodymap", "Names", "paincatastrophizingscale", "proxypainintensity", "proxypaincatastrophizingscale",
    "globalhealth", "pedpaincatastrophizingscale", "opiodrisk", "parentglobalhealth"};
  public static final String MEDS="meds";
  public static final String TREATMENTS="treatments";

  private Supplier<Database> dbp;

  public RegistryShortFormScoreProvider(Supplier<Database> dbp) {
    this();
    this.dbp = dbp;
  }

  private RegistryShortFormScoreProvider() {
  };

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
    ArrayList<ChartScore> scores = new ArrayList<ChartScore>();
    if (patientData == null) {
      return scores;
    }
    int summaryScore = 0; // used for surveys with a single overall score;
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
              break;
            case PCS:
            case PROXY_PCS:
            case PEDIATRIC_PCS:
                // Scores are cummulative sort of like promis in that the last is the final score
                RegistryQuestion question = RegistryAssessmentUtils.getQuestion(itemNode, itemInx, false);
                ArrayList<SurveyAnswerIntf> answers = question.getAnswers(true);
                summaryScore = 0;
                for (int ansInx = 0; ansInx < answers.size(); ansInx++) {
                  summaryScore += getItemSelectedScore(answers.get(ansInx));
                }
                chartScore.setAnswer(Integer.parseInt(orderAttrib), new BigDecimal(summaryScore));
                break;
            case GLOBAL_HEALTH: {
              // Get each answer
              RegistryQuestion ghQuestion = RegistryAssessmentUtils.getQuestion(itemNode, itemInx, false);
              ArrayList<SurveyAnswerIntf> ghAnswers = ghQuestion.getAnswers(true);
              int answerNumber = Integer.parseInt(orderAttrib);
              for (int ansInx = 0; ansInx < ghAnswers.size(); ansInx++) {
                int itemScore = getItemSelectedScore(ghAnswers.get(ansInx));
                chartScore.setAnswer(answerNumber, new BigDecimal(itemScore));
              }
              // we also need to add in the answer to a question from another survey
              answerNumber ++;
              if (itemInx + 1 == itemList.getLength()) {
                ArrayList<PatientStudyExtendedData> intensitySurveys = DataBaseUtils
                    .getPatientStudyDataBySurveyRegIdAndStudyDescription(dbp.get(), patientData.getSurveyRegId(),
                        studies[PAIN_INTENSITY]);
                if (intensitySurveys != null && intensitySurveys.size() > 0) {
                  ArrayList<ChartScore> intScores = getScore(intensitySurveys.get(0));
                  for (int i = 0; i < intScores.size(); i++) {
                    LocalScore score = (LocalScore) intScores.get(i);
                    int avgPain = score.getAnswer(INTENSITY_QUESTION_AVG).intValue();
                    chartScore.setAnswer(answerNumber, new BigDecimal(avgPain) );
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
                for (int ansInx = 0; ansInx < ghAnswers.size(); ansInx++) {
                  int itemScore = getItemSelectedScore(ghAnswers.get(ansInx));
                  chartScore.setAnswer(answerNumber, new BigDecimal(itemScore));
                }
              }
              break;
            }
            case OPIOD_RISK:
                RegistryQuestion opQuestion = RegistryAssessmentUtils.getQuestion( itemNode, itemInx, false );
                ArrayList< SurveyAnswerIntf > opAnswers = opQuestion.getAnswers( true );
                int opAnswerNumber = Integer.parseInt( orderAttrib );
                for ( int ansInx = 0; ansInx < opAnswers.size(); ansInx++ ) {
                    int itemScore = getItemSelectedScore( opAnswers.get( ansInx ) );
                    chartScore.setAnswer( opAnswerNumber, new BigDecimal( itemScore ) );
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

      logger.debug( "getScore returning " + scores.size() );
      for ( ChartScore chScore : scores ) {
          logger.debug( "get score , score = " + chScore.getScore() + " for category " + chScore.getCategoryLabel() );
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

    switch (getStudyIndex(study.getStudyDescription())) {
    case PAIN_INTENSITY:
    case PROXY_PAIN_INTENSITY:
      TimeSeries timeDataSetAvg = new TimeSeries("Average");
      TimeSeries timeDataSetNow = new TimeSeries("Worst");
      for (int indx = 0; indx < stats.size(); indx++) {
        LocalScore score = (LocalScore) stats.get(indx);

        Day day = new Day(score.getDate());
        try {
          // when the same study was taken > once in a day use the later
          // although this shouldn't really happen
          timeDataSetAvg.addOrUpdate(day, score.getAnswer(INTENSITY_QUESTION_AVG));
          logger.debug("Timeseries avg: added(" + day.toString() + score.getAnswer(INTENSITY_QUESTION_AVG) + ")");
        } catch (org.jfree.data.general.SeriesException duplicates) {
        }
        try {
          timeDataSetNow.addOrUpdate(day, score.getAnswer(INTENSITY_QUESTION_WORST));
          logger.debug("Timeseries now: added(" + day.toString() + score.getAnswer(INTENSITY_QUESTION_WORST) + ")");
        } catch (org.jfree.data.general.SeriesException duplicates) {
        }
      }
      dataset.addSeries(timeDataSetAvg);
      dataset.addSeries(timeDataSetNow);
      return dataset;
    case BODY_MAP:
      TimeSeries timeDataSetCnt = new TimeSeries("Areas selected");
      for (int indx = 0; indx < stats.size(); indx++) {
        LocalScore score = (LocalScore) stats.get(indx);

        ArrayList<BigDecimal> answers = score.getAnswers();
        if (answers != null) {
          Day day = new Day(score.getDate());
          Double ttl = new Double(0.0);
          for (int aIndx = 0; aIndx < answers.size(); aIndx++) {
            BigDecimal answer = answers.get(aIndx);
            ttl = ttl.doubleValue() + answer.doubleValue();
          }

          try {
            timeDataSetCnt.addOrUpdate(day, ttl);
          } catch (org.jfree.data.general.SeriesException duplicates) {
          }
        }
      }
      dataset.addSeries(timeDataSetCnt);
      return dataset;
    case PCS:
    case PROXY_PCS:
    case PEDIATRIC_PCS:
        TimeSeries timeDataSetPcs = new TimeSeries("PCS Score");
        for (int indx = 0; indx < stats.size(); indx++) {
          LocalScore score = (LocalScore) stats.get(indx);
          ArrayList<BigDecimal> answers = score.getAnswers();
          if (answers != null) {
            Day day = new Day(score.getDate());
            BigDecimal pcsScore = new BigDecimal(0);
            for (int aIndx = 0; aIndx < answers.size(); aIndx++) {
              pcsScore = answers.get(aIndx);
            }
            try {
              timeDataSetPcs.addOrUpdate(day, pcsScore);
            } catch (org.jfree.data.general.SeriesException duplicates) {
            }
          }
        }
        dataset.addSeries(timeDataSetPcs);
        return dataset;
    case GLOBAL_HEALTH: // report inverted
    case PARENT_GLOBAL_HEALTH:
        TimeSeries timeDataSetPhysic = new TimeSeries("Physical Health");
        TimeSeries timeDataSetMental = new TimeSeries("Mental Health");
        for (int indx = 0; indx < stats.size(); indx++) {
          GlobalHealthScore score = (GlobalHealthScore) stats.get(indx);
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
            } catch (org.jfree.data.general.SeriesException duplicates) {
            }
          }
        }
        dataset.addSeries(timeDataSetPhysic);
        dataset.addSeries(timeDataSetMental);
        return dataset;
    case OPIOD_RISK:
      TimeSeries timeDataSetOR = new TimeSeries( "Opioid Risk Score" );

        for (int indx = 0; indx < stats.size(); indx++) {
        LocalScore score = (LocalScore) stats.get(indx);

        ArrayList<BigDecimal> answers = score.getAnswers();

        if (answers != null) {
          Day day = new Day(score.getDate());

          BigDecimal orScore = new BigDecimal(0);

          for (int aIndx = 0; aIndx < answers.size(); aIndx++) {
            orScore = answers.get(aIndx);

          }
          try {
            timeDataSetOR.addOrUpdate(day, orScore);

          } catch (org.jfree.data.general.SeriesException duplicates) {
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

    /**
     * integerToNominal
     *
     * The integers and nominals are expected to have the same number of subsections and in the same relative order
     *      example:
     *               String nominals = "None/Minimal:Mild:Moderate:Severe";
     *               String integers = "0:1 2 3 4:5 6:7 8 9 10";
     *
     * @param integers  String with subsections divided by ":" characters
     * @param nominals  String with subsections divided by ":" characters
     * @param value     The string version of the integer
     *
     * @author jrpence 20141110
     */
    public String integerToNominal(String nominals, String integers, String value){

        String output = "";

        String[] nominalArray = nominals.split( ":" );
        String[] integerArray = integers.split( ":" );

        boolean found = false;
        for ( int idx = 0; ( idx < integerArray.length ) && ( found == false ); idx++ ) {
            if ( integerArray[idx].contains( value ) ) {
                output = nominalArray[idx];
                found = true;
            }
        }

        if ( ! found ) {
            output = "no value for " + value;
        }

        return output;
    }

    @Override
    public Table getScoreTable( ArrayList< ChartScore > scores ) {

        Table table = new Table();
        // ArrayList<String> colheadings = new ArrayList<String>();
        if ( scores == null ) {
            return table;
        }
        if ( scores.size() < 1 ) {
            return table;
        }
        if ( scores.get( 0 ) != null ) {
            switch ( getStudyIndex( scores.get( 0 ).getStudyDescription() ) ) {
                case PROXY_PAIN_INTENSITY:
                    // Asked order:
                    int worst = 0;
                    int avg = 1;
                    int now = 2;
                    int least = 3;
                    String title = "Pain Intensity";
                    if ( getStudyIndex( scores.get( 0 ).getStudyDescription() ) == PROXY_PAIN_INTENSITY ) {
                        title = "Parent Proxy Pain Intensity";
                    }
                    table.addHeading( title + ": 0 = No pain  10 = Worst pain imaginable" );
                    TableRow colHeadings = new TableRow( 100 );
                    TableColumn avgColHeading = new TableColumn( "Average", 25 );
                    TableColumn nowColHeading = new TableColumn( "Now", 25 );
                    TableColumn lesColHeading = new TableColumn( "Least", 25 );
                    TableColumn worColHeading = new TableColumn( "Worst", 25 );
                    colHeadings.addColumn( avgColHeading );
                    colHeadings.addColumn( nowColHeading );
                    colHeadings.addColumn( lesColHeading );
                    colHeadings.addColumn( worColHeading );
                    table.addRow( colHeadings );
                    String nominals = "None/Minimal:Mild:Moderate:Severe";
                    String integers = "0:1 2 3 4:5 6:7 8 9 10";
                    for ( int indx = 0; indx < scores.size(); indx++ ) {
                        if ( scores.get( indx ) instanceof LocalScore ) {
                            LocalScore lscore = ( LocalScore ) scores.get( indx );
                            ArrayList< BigDecimal > answers = lscore.getAnswers();
                            TableRow row = new TableRow( 100 );
                            row.addColumn( new TableColumn( answers.get( avg ).toBigInteger().toString(), 25 ) );
                            row.addColumn( new TableColumn( answers.get( now ).toBigInteger().toString(), 25 ) );
                            row.addColumn( new TableColumn( answers.get( least ).toBigInteger().toString(), 25 ) );
                            row.addColumn( new TableColumn( answers.get( worst ).toBigInteger().toString(), 25 ) );
                            table.addRow( row );

                            row = new TableRow( 100 );
                            row.addColumn( new TableColumn( integerToNominal( nominals, integers, answers.get( avg ).toBigInteger().toString() ), 25 ) );
                            row.addColumn( new TableColumn( integerToNominal( nominals, integers, answers.get( now ).toBigInteger().toString() ), 25 ) );
                            row.addColumn( new TableColumn( integerToNominal( nominals, integers, answers.get( least ).toBigInteger().toString() ), 25 ) );
                            row.addColumn( new TableColumn( integerToNominal( nominals, integers, answers.get( worst ).toBigInteger().toString() ), 25 ) );
                            table.addRow( row );
                        }
                    }
                    break;
                case PAIN_INTENSITY:
                    table.addHeading( "Pain Intensity: 0 = No pain  10 = Worst pain imaginable" );
                    TableRow headings = new TableRow( 100 );
                    // Same as asked order: WORST, AVG, NOW, LEAST
                    headings.addColumn( new TableColumn( "Worst", 25 ) );
                    headings.addColumn( new TableColumn( "Average", 25 ) );
                    headings.addColumn( new TableColumn( "Now", 25 ) );
                    headings.addColumn( new TableColumn( "Least", 25 ) );
                    table.addRow( headings );
                    for ( int indx = 0; indx < scores.size(); indx++ ) {
                        if ( scores.get( indx ) instanceof LocalScore ) {

                            LocalScore lscore = ( LocalScore ) scores.get( indx );

                            ArrayList< BigDecimal > answers = lscore.getAnswers();
                            TableRow row = new TableRow( 100 );
                            row.addColumn( new TableColumn( answers.get( 0 ).toBigInteger().toString(), 25 ) ); // WORST
                            row.addColumn( new TableColumn( answers.get( 1 ).toBigInteger().toString(), 25 ) ); // AVG
                            row.addColumn( new TableColumn( answers.get( 2 ).toBigInteger().toString(), 25 ) ); // NOW
                            row.addColumn( new TableColumn( answers.get( 3 ).toBigInteger().toString(), 25 ) ); // LEAST
                            table.addRow( row );
                        }
                    }
                    break;
                case BODY_MAP:
                    table.addHeading( "Areas selected on the body map" );
                    TableRow bodyHeadings = new TableRow( 100 );
                    bodyHeadings.addColumn( new TableColumn( "Date", 50 ) );
                    bodyHeadings.addColumn( new TableColumn( "Selected", 50 ) );
                    table.addRow( bodyHeadings );
                    for ( int indx = 0; indx < scores.size(); indx++ ) {
                        LocalScore score = ( LocalScore ) scores.get( indx );
                        TableRow row = new TableRow( 100 );
                        row.addColumn( new TableColumn( dateFormatter.format( score.getDate() ), 50 ) );
                        int numSelected = 0;
                        ArrayList< BigDecimal > answers = score.getAnswers();
                        if ( answers != null && answers.size() > 0 ) {
                            numSelected = answers.get( 0 ).intValue();
                        }
                        row.addColumn( new TableColumn( numSelected + "", 50 ) );
                        table.addRow( row );
                    }
                    break;
                case PCS:
                case PROXY_PCS:
                case PEDIATRIC_PCS:
                    table.addHeading( "Pain Catastrophizing Scale" );

                    TableRow ptsRow = new TableRow( 100 );
                    ptsRow.addColumn( new TableColumn( "Date", 40 ) );
                    ptsRow.addColumn( new TableColumn( "Score", 40 ) );
                    ptsRow.addColumn( new TableColumn( "Category", 30 ) );
                    table.addRow( ptsRow );

                    BigDecimal scoreValue = new BigDecimal( 0 );
                    for ( int indx = 0; indx < scores.size(); indx++ ) {
                        LocalScore score = ( LocalScore ) scores.get( indx );
                        TableRow row = new TableRow( 100 );
                        row.addColumn( new TableColumn( dateFormatter.format( score.getDate() ), 40 ) );
                        ArrayList< BigDecimal > answers = score.getAnswers();
                        if ( answers != null && answers.size() > 0 ) {
                            for ( int a = 0; a < answers.size(); a++ ) {
                                scoreValue = scoreValue.add( answers.get( a ) );
                            }
                        }
                        row.addColumn( new TableColumn( scoreValue.toBigInteger().toString() + "", 40 ) );

                        String nominal = "";
                        Double d = 30.0;
                        BigDecimal threshold = BigDecimal.valueOf(d);
                        int val = scoreValue.compareTo(threshold);
                        if ( val == 0 || val == 1 ) {
                            nominal = "High";
                        }
                        row.addColumn( new TableColumn( nominal + "", 30 ) );

                        table.addRow( row );
                    }
                    break;
                case GLOBAL_HEALTH:
                case PARENT_GLOBAL_HEALTH:
                    table.addHeading( "Global Health" );
                    TableRow ghHeadingRow = new TableRow( 500 );
                    ghHeadingRow.addColumn( new TableColumn( "Date", 20 ) );
                    ghHeadingRow.addColumn( new TableColumn( "Physical Health Score", 20 ) );
                    ghHeadingRow.addColumn( new TableColumn( "%ile", 20 ) );
                    ghHeadingRow.addColumn( new TableColumn( "Mental Health Score", 20 ) );
                    ghHeadingRow.addColumn( new TableColumn( "%ile", 20 ) );
                    table.addRow( ghHeadingRow );

                    for ( int indx = 0; indx < scores.size(); indx++ ) {
                        GlobalHealthScore score = ( GlobalHealthScore ) scores.get( indx );
                        TableRow ghRow = new TableRow( 100 );
                        ghRow.addColumn( new TableColumn( dateFormatter.format( score.getDate() ), 20 ) );
                        Double physicalScore = new Double( score.getPhysicalHealthTScore() );
                        ghRow.addColumn( new TableColumn( physicalScore.toString(), 20 ) );
                        ghRow.addColumn( new TableColumn( calculatePercentile( physicalScore ).toString(), 20 ) );
                        Double mentalScore = new Double( score.getMentalHealthTScore() );
                        ghRow.addColumn( new TableColumn( mentalScore.toString(), 20 ) );
                        ghRow.addColumn( new TableColumn( calculatePercentile( mentalScore ).toString(), 20 ) );
                        table.addRow( ghRow );
                    }
                    break;
                /**
                 * Display nominal value in column instead of percentile
                 *
                 * @author kpharvey v2.0
                 */
                case OPIOD_RISK:
                    table.addHeading( "Opioid Risk" );
                    TableRow orHeadingRow = new TableRow(100);
                    orHeadingRow.addColumn(new TableColumn("Date", 40));
                    orHeadingRow.addColumn(new TableColumn("Score", 30));
                    orHeadingRow.addColumn(new TableColumn("Category", 30));
                    table.addRow(orHeadingRow);

                    ServerUtils serverUtils = ServerUtils.getInstance();

                    String range = serverUtils.getParam( "HL7Message.opiodRisk" ); //get score range for Opioid Risk

                    String[] rangeSplit = range.split( "," );
                    String[] labels = {"None/Minimal", "Mild", "Moderate", "Severe", "High"}; //set up nominal category values

                    for (int indx = 0; indx < scores.size(); indx++) {
                        LocalScore score = ( LocalScore ) scores.get(indx);

                        TableRow row = new TableRow(100);

                        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 40));

                        ArrayList<BigDecimal> answers = score.getAnswers();

                        BigDecimal riskScore = new BigDecimal(0);

                        if (answers != null && answers.size() > 0) {
                            for (BigDecimal answer : answers) {
                                logger.debug("getScoreTable Opioid adding " + answer );
                                riskScore = riskScore.add( answer );
                                logger.debug( "getScoreTable Opioid running riskScore =" + riskScore );
                            }
                        }

                        ArrayList<String> rangeAndCategory = HL7Generator.rangeAndCategory( riskScore.intValue(), rangeSplit, labels );

                        logger.debug("getScoreTable Opioid riskScore =" + riskScore );
                        logger.debug("getScoreTable Opioid percentile =" + calculatePercentile( riskScore.doubleValue() ) );

                        row.addColumn( new TableColumn( String.valueOf( riskScore.intValue() ) , 30 ) );
                        row.addColumn( new TableColumn( rangeAndCategory.get( 1 ), 30) ); //display category for associated score range

                        table.addRow(row);
                    }
                    break;

                default:
                    break;
            }
        }

        return table;
    }

    @Override
    public String formatExplanationText( Study study, ArrayList< ChartScore > scores ) {
        try {

            String explanation = null;
            if ( study != null ) {
                explanation = study.getExplanation();
            }

            if ( explanation == null ) {
                logger.debug( "formatExplanationText study has null explanation" );
                return explanation;
            }
            if ( scores == null || scores.size() < 1 ) {
                logger.debug( "Study " + study.getStudyDescription() + "formatExplanationText called without SCORES" );
                return explanation;
            }

            switch ( getStudyIndex( study.getStudyDescription() ) ) {
                case PAIN_INTENSITY:
                case PROXY_PAIN_INTENSITY:
                    logger.debug( "formatExplanationText painIntensity" );
                    Hashtable< Integer, Double > totals = new Hashtable< Integer, Double >();
                    for ( int indx = 0; indx < scores.size(); indx++ ) {
                        if ( scores.get( indx ) instanceof LocalScore ) {
                            LocalScore lscore = ( LocalScore ) scores.get( indx );
                            ArrayList< Integer > questions = lscore.getQuestions();
                            ArrayList< BigDecimal > answers = lscore.getAnswers();
                            for ( int qIndx = 0; qIndx < questions.size(); qIndx++ ) {
                                Integer question = questions.get( qIndx );
                                BigDecimal answer = answers.get( qIndx );
                                logger.debug( "formatExplanationText question " + question + " = " + answer.toString() );
                                Double ttl = totals.get( questions.get( qIndx ) );
                                if ( ttl == null ) {
                                    ttl = new Double( 0.0 );
                                }
                                ttl = ttl.doubleValue() + answer.doubleValue();
                                totals.put( question, ttl );
                                // use the last set of responses in the explanation
                                if ( indx == ( scores.size() - 1 ) ) {
                                    explanation = StringUtils.replace( explanation, "[LAST_" + question.intValue() + "]",
                                                                       scoreFormatter.format( answer ) );
                                }
                            }
                        }
                    }

                    Enumeration< Integer > totalKeys = totals.keys();
                    while ( totalKeys.hasMoreElements() ) {
                        Integer question = totalKeys.nextElement();
                        Double answerTotal = totals.get( question );
                        Double average = answerTotal / scores.size();
                        explanation = StringUtils.replace( explanation, "[AVG_" + question + "]", scoreFormatter.format( average ) );
                        logger.debug( "formatExplanationText question " + question + " had total of " + answerTotal + " with "
                                      + scores.size() + " surveys for avg of " + average );
                    }
                    break;
                case BODY_MAP:
                    Double ttl = new Double( 0.0 );
                    int indx = scores.size() - 1;
                    if ( scores.get( indx ) instanceof LocalScore ) {
                        LocalScore lscore = ( LocalScore ) scores.get( indx );
                        ArrayList< BigDecimal > answers = lscore.getAnswers();
                        for ( int aIndx = 0; aIndx < answers.size(); aIndx++ ) {
                            BigDecimal answer = answers.get( aIndx );
                            ttl = ttl.doubleValue() + answer.doubleValue();
                        }
                    }
                    explanation = StringUtils.replace( explanation, "[LAST]", scoreFormatter.format( ttl.intValue() ) );
                    break;
                default:
                    break;
            }
            return explanation;
        }
        catch ( Exception e ) {
            logger.error( "error", e );
            return ( e.getMessage() );
        }
    }

    @Override
    public ChartInfo createLineChart( ArrayList< ChartScore > stats, XYDataset ds, Study study,
                                      ChartConfigurationOptions opts ) {

        ArrayList< Study > studies = new ArrayList< Study >();
        studies.add( study );

        ChartInfo chartInfo = new ChartInfo( "Local", stats, ds, study );
        final JFreeChart chart = new JFreeChart( study.getTitle(), ChartMaker.getFont( opts, Font.PLAIN,
                                                                                       ChartMaker.TITLEFONTSIZE ), getPlot( chartInfo, studies, opts ), true );
        chart.getLegend().setItemFont( ChartMaker.getFont( opts, Font.PLAIN, ChartMaker.LEGENDFONTSIZE ) );
        LegendTitle legend1 = chart.getLegend( 1 );
        if ( legend1 != null ) {
            legend1.visible = true;
        }
        chart.getLegend().setItemPaint( Color.black );
        switch ( getStudyIndex( study.getStudyDescription() ) ) {
            case BODY_MAP:
                chart.removeLegend();
                break;
            default:
                break;
        }

        chartInfo.setChart( chart );
        return chartInfo;
    }


    public int getStudyIndex( String studyDescription ) {
        if ( studyDescription != null ) {
            for ( int indx = 0; indx < studies.length; indx++ ) {
                if ( studies[indx].equals( studyDescription.toLowerCase() ) ) {
                    return indx;
                }
            }
        }
        return - 1;
    }

    @Override
    public ArrayList< SurveyQuestionIntf > getSurvey( ArrayList< PatientStudyExtendedData > patientStudies, PrintStudy study,
                                                      Patient patient, boolean allAnswers ) {
        if ( patientStudies == null || study == null || patient == null ) {
            return new ArrayList< SurveyQuestionIntf >();
        }
        PatientStudyExtendedData patStudy = null;

        // get the last one of this study type
        for ( int ps = 0; ps < patientStudies.size(); ps++ ) {
            if ( patientStudies.get( ps ).getStudyCode().intValue() == study.getStudyCode().intValue() ) {
                patStudy = patientStudies.get( ps );
            }
        }
        return getSurvey( patStudy, study, patient, allAnswers );
    }

    public ArrayList< SurveyQuestionIntf > getSurvey(
            PatientStudyExtendedData patStudy, PrintStudy study,
            Patient patient, boolean allAnswers ) {
        ArrayList< SurveyQuestionIntf > questions = new ArrayList< SurveyQuestionIntf >();
        if ( patStudy == null ) {
            return questions;
        }
        String xmlDocumentString = patStudy.getContents();
        if ( xmlDocumentString == null ) {
            // get the file
            xmlDocumentString = XMLFileUtils.getInstance().getXML(
                    study.getStudyDescription() + ".xml" );
            logger.debug( "read xml " + xmlDocumentString );
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream( new StringReader( xmlDocumentString ) );

            Document messageDom = db.parse( is );
            Element docElement = messageDom.getDocumentElement();
            if ( docElement.getTagName().equals( Constants.FORM ) ) {
                int index = 0;
                Element itemsNode = ( Element ) messageDom.getElementsByTagName( Constants.ITEMS ).item( 0 );
                NodeList itemList = itemsNode.getElementsByTagName( Constants.ITEM );
                for ( int itemInx = 0; itemInx < itemList.getLength(); itemInx++ ) {
                    Element itemNode = ( Element ) itemList.item( itemInx );
                    // check if conditional
                    boolean meets = RegistryAssessmentUtils.qualifies( patient, itemNode );
                    // If they qualify for this question
                    if ( meets ) {
                        SurveyQuestionIntf question = RegistryAssessmentUtils.getQuestion( itemNode, index, allAnswers );
                        questions.add( question );
                        index++;
                    }
                }
            }
        }
        catch ( ParserConfigurationException e ) {
            logger.error(
                    "Error parsing xml for patientStudy token " + patStudy.getToken() + " study " + patStudy.getStudyCode(), e );
        }
        catch ( SAXException e ) {
            logger.error(
                    "Error parsing xml for patientStudy token " + patStudy.getToken() + " study " + patStudy.getStudyCode(), e );

        }
        catch ( IOException e ) {
            logger.error(
                    "Error parsing xml for patientStudy token " + patStudy.getToken() + " study " + patStudy.getStudyCode(), e );
        }

        return questions;
    }

    public Table getTable( ArrayList< PatientStudyExtendedData > patientStudies, PrintStudy study, Patient patient ) {
        ArrayList< SurveyQuestionIntf > questions;
        Table table = new Table();
        ArrayList< String > headings = new ArrayList< String >();
        logger.debug( "table STUDY DESCRIPTION = " + study.getStudyDescription() );
        switch ( getStudyIndex( study.getStudyDescription() ) ) {
            case PAIN_INTENSITY:
            case PROXY_PAIN_INTENSITY:
                headings.add( study.getTitle() + ":  0=No Pain, 10=Worst Pain Imaginable" );
                table.setHeadings( headings );
                TableRow qRow = new TableRow(); // question row
                qRow.addColumn( new TableColumn( "Worst", 25 ) );
                qRow.addColumn( new TableColumn( "Average", 25 ) );
                qRow.addColumn( new TableColumn( "Now", 25 ) );
                qRow.addColumn( new TableColumn( "Least", 25 ) );

                TableRow aRow = new TableRow(); //answer Row

                String nominals = "None/Minimal:Mild:Moderate:Severe";
                String integers = "0:1 2 3 4:5 6:7 8 9 10";
                TableRow nRow = new TableRow(); // nominal row
                questions = getSurvey( patientStudies, study, patient, false );
                for ( int q = 0; q < questions.size(); q++ ) {
                    if ( questions.get( q ) != null && questions.get( q ).getAnswered() ) {
                        ArrayList< SurveyAnswerIntf > ans = questions.get( q ).getAnswers();
                        String answerStr = null;
                        if ( ans != null && ans.size() > 0 ) {
                            for ( int a = 0; a < ans.size(); a++ ) {
                                if ( ans.get( a ) != null && ans.get( a ).getSelected() ) {
                                    answerStr = ans.get( a ).getAttribute( Constants.DESCRIPTION );
                                }
                            }
                        }
                        if ( answerStr != null ) {
                            aRow.addColumn( new TableColumn( answerStr.toString(), 25 ) );
                            nRow.addColumn( new TableColumn( integerToNominal( nominals, integers, answerStr.toString() ) ,25 ) );
                        } else {
                            logger.debug( "no answer for pain intensity question " + q );
                        }
                    }
                }
                table.addRow( qRow );
                table.addRow( aRow );
                table.addRow( nRow );
                break;
            case PCS:
            case PROXY_PCS:
            case PEDIATRIC_PCS:
                headings.add( study.getTitle() );
                logger.debug( "study title =" + study.getTitle() );
                table.setHeadings( headings );

                TableRow pcsHeadings = new TableRow(100);
                pcsHeadings.addColumn( new TableColumn( "Date", 40 ) );
                pcsHeadings.addColumn( new TableColumn( "Score", 30 ) );
                pcsHeadings.addColumn( new TableColumn( "Category", 30 ) );
                table.addRow(pcsHeadings);

                PatientStudyExtendedData patStudy = null;
                for ( int ps = 0; ps < patientStudies.size(); ps++ ) {
                    if ( patientStudies.get( ps ).getStudyCode().intValue() == study.getStudyCode().intValue() ) {
                        patStudy = patientStudies.get( ps );
                        questions = getSurvey( patStudy, study, patient, false );
                        int pcsScore = 0;
                        for ( int q = 0; q < questions.size(); q++ ) {
                            ArrayList< SurveyAnswerIntf > ans = questions.get( q ).getAnswers( true );
                            for ( int ansInx = 0; ansInx < ans.size(); ansInx++ ) {
                                pcsScore += getItemSelectedScore( ans.get( ansInx ) );
                            }
                        }
                        TableRow row = new TableRow( 100 );
                        row.setColumnGap( 3 );
                        row.addColumn( new TableColumn( dateFormatter.format( patStudy.getDtChanged() ), 40 ) );
                        row.addColumn( new TableColumn( pcsScore + "", 30 ) );

                        String nominal = "";
                        if ( pcsScore >= 30 ) {
                            nominal = "High";
                        }
                        row.addColumn( new TableColumn( nominal + " ", 30 ) );

                        table.addRow( row );
                    }
                }
                break;
            case GLOBAL_HEALTH:
            case OPIOD_RISK:
                // get the last one of this study type
                PatientStudyExtendedData ghStudy = null;
                for ( int ps = 0; ps < patientStudies.size(); ps++ ) {
                    if ( patientStudies.get( ps ).getStudyCode().intValue() == study.getStudyCode().intValue() ) {
                        ghStudy = patientStudies.get( ps );
                    }
                }
                if ( ghStudy != null ) {
                    ArrayList< ChartScore > scores = getScore( ghStudy );
                    table = getScoreTable( scores );
                }

                if ( OPIOD_RISK == getStudyIndex( study.getStudyDescription() ) ) {
                    logger.debug( "Opioid case in table" );
                    if ( ghStudy != null ) {
                        logger.debug( "gh study wasn't null" );
                    }
                    for ( TableRow tableRow : table.getRows() ) {
                        StringBuilder row = new StringBuilder();
                        for ( TableColumn tableColumn : tableRow.getColumns() ) {
                            row.append( "| " + tableColumn.getValue() + " |" );
                        }
                        logger.debug( row.toString() );
                    }
                }
                break;
            default:
                if ( study.getStudyDescription().startsWith( TREATMENTS ) || study.getStudyDescription().startsWith( MEDS ) ) {
                    headings.add( study.getTitle() );
                    patStudy = null;
        /* Find the last one */
                    for ( int ps = 0; ps < patientStudies.size(); ps++ ) {
                        if ( patientStudies.get( ps ).getStudyCode().intValue() == study.getStudyCode().intValue() ) {
                            patStudy = patientStudies.get( ps );
                        }
                    }
        if ( patStudy != null ) {
         questions = getSurvey(patStudy, study, patient, false);
          Collections.sort(questions, new SurveyQuestionComparator<SurveyQuestionIntf>());
          TableRow row = new TableRow(100);
          for (int q = 0; q < questions.size(); q++) {
            boolean conditional = false;
            SurveyQuestionIntf question = questions.get(q);
            String ref = question.getAttribute(Constants.XFORM_REF);
            if (ref != null && Constants.ACTION_ONSELECT.equals(ref)) {
              conditional = true;;
            }
            ArrayList<SurveyAnswerIntf> ans = question.getAnswers(true);
            if (ans != null && ans.size() > 0 ) {
              logger.debug(study.getStudyDescription() + " question:" + question.getAttribute(Constants.ORDER) + " ref: " + ref + " conditional is " + conditional + " and has " + ans.size() + " answers ");
              SurveyAnswerIntf answer = ans.get(0);
              if (answer.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_SELECT ||
                  answer.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_SELECT1) {
                logger.debug(study.getStudyDescription() + " question:" + question.getAttribute(Constants.ORDER) + " ref: " + ref + " is a select");
                  SelectElement select = (SelectElement) answer;
                  ArrayList<SelectItem> selectedItems = select.getSelectedItems();
                  logger.debug(study.getStudyDescription() + " question:" + question.getAttribute(Constants.ORDER) + " ref: " + ref + " has " + selectedItems.size() + " selectedItems");
                  StringBuffer strBuf = new StringBuffer();
                  String sep="";
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
                logger.debug(study.getStudyDescription() + " question:" + question.getAttribute(Constants.ORDER) + " ref: " + ref + " not a select");
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

  private XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
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
      rangeAxis.setTickUnit(new NumberTickUnit(15));
      rangeAxis.setRange(0, 75);
      rangeAxis.setLabel("Regions");
      renderer.setBaseLinesVisible(true);
      break;
    case GLOBAL_HEALTH:
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
        logger.debug( "getItemSelectedScore" );
        int score = 0;
		if (answer.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_SELECT ||
            answer.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_SELECT1) {
			SelectElement select = (SelectElement) answer;
			ArrayList<SelectItem> selectItems = select.getSelectedItems();
			for (int si = 0; si < selectItems.size(); si++) {
				try {
					SelectItem selectItem = selectItems.get(si);
					score += Integer.parseInt(selectItem.getValue());
                    logger.debug( " adding score of " + selectItem.getValue());
                    logger.debug(selectItem.getLabel());
				} catch (NumberFormatException nfe) {
					logger.error("error getting " + PCS
							+ " score, invalid numericValue");
				}
			}
		}
		return score;
	}

	private Long calculatePercentile(Double score) {
    NormalDistribution norm = new NormalDistribution(50, 10);
    Double percentile = norm.cumulativeProbability(score) * 100;
    return new Long(Math.round(percentile));
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
    String size = ServerUtils.getInstance().getParam(edu.stanford.registry.shared.survey.Constants.PROMIS_QUESTION_REPORT_FONTSIZE);
    if (size != null && size.length() > 0) {
      try {
        return Integer.parseInt(size);
      } catch (Exception e) {}
    }
    return defaultFontSize;
  }

}
