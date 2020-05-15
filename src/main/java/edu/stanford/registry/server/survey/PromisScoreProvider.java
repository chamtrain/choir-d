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

import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.charts.ChartMaker;
import edu.stanford.registry.server.plugin.ProviderFor;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.tools.CustomDateAxis;
import edu.stanford.registry.server.tools.CustomXYErrorRenderer;
import edu.stanford.registry.server.utils.ChartUtilities;
import edu.stanford.registry.server.utils.PROMISAssessmentUtils;
import edu.stanford.registry.server.utils.PROMISItemElementComparator;
import edu.stanford.registry.server.utils.StringUtils;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.PROMISScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
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
import org.xml.sax.SAXException;

@ProviderFor(ScoreProvider.class)
public class PromisScoreProvider implements ScoreProvider {
  private static Logger logger = Logger.getLogger(PromisScoreProvider.class);
  private final DecimalFormat meanFormatter = new DecimalFormat("###0");
  private final DecimalFormat sdFormatter = new DecimalFormat("+##0.0;-##0.0");
  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yy");
  private static final String SURVEY_SYSTEM = "PROMIS";

  private int version = 0;
  protected SiteInfo siteInfo;

  @SuppressWarnings("unused")
  private PromisScoreProvider() {
    // don't allow a no argument constructor;
  }

  public PromisScoreProvider(SiteInfo siteInfo, int version) {
    this.siteInfo = siteInfo;
    this.version = version;
  }

  @Override
  public String getDescription() {
    return "ScoreProvider for PROMIS assessments";
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {
    if (studyName != null && studyName.startsWith(SURVEY_SYSTEM)) {
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
      if (docElement == null) {
        return scores;
      }
      if (docElement.getTagName().equals("Form")) {
        NodeList itemList = null;
        NodeList itemsList = doc.getElementsByTagName("Items");
        if (itemsList != null && itemsList.getLength() > 0) {
          Element itemsNode = (Element) itemsList.item(0);
          itemList = itemsNode.getElementsByTagName("Item");
        } else {
          itemList = doc.getElementsByTagName("Item");
        }

        if (itemList == null) {
          return scores;
        }
        String theta = "0";
        String stdErr = "0";
        ArrayList<Element> sortedList = sortItems(itemList);
        for (Element itemNode : sortedList) {
          // We want the last one.
          theta = itemNode.getAttribute("Theta");
          stdErr = itemNode.getAttribute("StdError");
        } // end of "Item" tags

        Double dblTheta = Double.valueOf(theta);
        Double stdError = Double.valueOf(stdErr);
        PROMISScore score = new PROMISScore(patientData.getDtChanged(), patientData.getPatientId(),
            patientData.getStudyCode(), patientData.getStudyDescription(), dblTheta, stdError);
        logger.debug("Study " + patientData.getStudyCode() + " on " + patientData.getDtChanged() + " xml Theta="
            + theta + " StdError=" + stdErr + " score =" + score.getScore() + " theta=" + score.getTheta()
            + " stderr =" + score.getStdError());
        score.setAssisted(patientData.wasAssisted());
        scores.add(score);
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
  public TimeSeriesCollection getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> stats, PrintStudy study,
      ChartConfigurationOptions opts) {
    if (study != null && study.getInvert()) {

      return getInvertedTimeSet(stats, study.getTitle(), opts);
    }
    final TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(baseLineSeries);

    if (stats == null) {
      return dataset;
    }

    final TimeSeries timeDataSet = new TimeSeries(study.getTitle());
    NormalDistribution norm = new NormalDistribution(50, 10);
    for (ChartScore stat : stats) {
      BigDecimal score = stat.getScore();
      Day day = new Day(stat.getDate());
      try {
        //  If option to chart as percentiles convert
        if (opts.getBooleanOption(ConfigurationOptions.OPTION_CHART_PERCENTILES)) {
          int origScore = score.intValue();
          score = new BigDecimal(norm.cumulativeProbability(origScore) * 100);
          //logger.debug("charting score " + origScore + " as percentile value " + score.intValue());
        }
        // when the same study was taken > once in a day use the later
        // although this shouldn't really happen
        timeDataSet.addOrUpdate(day, score);
      } catch (SeriesException duplicates) {
      }
    }
    dataset.addSeries(timeDataSet);
    return dataset;
  }

  public TimeSeriesCollection getInvertedTimeSet(ArrayList<ChartScore> stats, String legend,
      ChartConfigurationOptions opts) {
    final TimeSeries timeDataSet = new TimeSeries(legend); // description);
    final NormalDistribution norm = new NormalDistribution(50, 10);
    final TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(ChartUtilities.getBaseLineSeries(stats));

    if (stats == null) {
      logger.debug("getInverted: stats is null, returning");
      return dataset;
    }

    for (ChartScore stat : stats) {
      BigDecimal score = stat.getScore();
      Day day = new Day(stat.getDate());
      logger.debug("getInverted: original score " + score.doubleValue());
      if (score.doubleValue() > 50) {
        logger.debug("getInverted: original score " + score.doubleValue());
        score = new BigDecimal(50 - (score.doubleValue() - 50));
      } else if (score.doubleValue() < 50) {
        score = new BigDecimal(50 + (50 - score.doubleValue()));
      }
      try {
        logger.debug("getInverted: new score " + score.doubleValue());
        //      If option to chart as percentiles convert
        if (opts.getBooleanOption(ConfigurationOptions.OPTION_CHART_PERCENTILES)) {
          int origScore = score.intValue();
          score = new BigDecimal(norm.cumulativeProbability(origScore) * 100);
          //logger.debug("charting score " + origScore + " as percentile value " + score.intValue());
        }
        timeDataSet.addOrUpdate(day, score);
      } catch (SeriesException duplicates) {
        /* if the same study was taken more than once a day use the later one,
           although this shouldn't really happen */
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
    table.addHeading(scores.get(0).getStudyDescription());
    try {
      TableRow colHeader = new TableRow(100);
      colHeader.addColumn(new TableColumn("Date", 50));
      colHeader.addColumn(new TableColumn("Score", 25));
      colHeader.addColumn(new TableColumn("SE", 25));
      for (ChartScore score1 : scores) {
        PROMISScore score = (PROMISScore) score1;
        TableRow row = new TableRow(100);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 50));
        row.addColumn(new TableColumn(meanFormatter.format(score.getScore()), 25));
        row.addColumn(new TableColumn(sdFormatter.format(score.getStdError()), 25));
        table.addRow(row);
      }
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }

    return table;
  }

  @Override
  public String formatExplanationText(Study study, ArrayList<ChartScore> scores) {

    String explanation = study.getExplanation();
    if (explanation == null) {
      logger.debug("Study " + study.getStudyDescription() + " has no explanation text");
      return null;
    }
    if (scores == null || scores.size() < 1) {
      logger.debug("Study " + study.getStudyDescription() + "formatExplanationText called without SCORES");
      return explanation;
    }
    /*
     * Calculate the overall mean [VAR_X] and overall standard deviation the
     * most recent mean [VAR_W] and most recent standard deviation
     */
    double numerator = 0;
    double denominator = 0;
    double mostRecentMean = 0;
    double mostRecentSD = 0;
    for (ChartScore score1 : scores) {
      PROMISScore score = (PROMISScore) score1;
      mostRecentSD = Math.pow(score.getStdError(), 2);
      mostRecentMean = score.getScore().doubleValue() * mostRecentSD;
      numerator += mostRecentMean;
      denominator += mostRecentSD;
      logger.debug("SCORES " + score1.getStudyCode() + " " + score1.getStudyDescription() + " "
          + score.getDate() + " StdErr=" + score.getStdError() + " Theta=" + score.getTheta() + " Score="
          + score.getScore() + "( 10 * theta + 50 )" + " Numerator=" + mostRecentMean
          + " ( the score * (stderr to the power of 2) )" + " Denominator=" + mostRecentSD
          + " ( stderr to the power of 2) )");
    }
    double varX = numerator / denominator;
    double varY = (varX - 50) / 10;
    double varW = mostRecentMean / mostRecentSD;
    double varZ = (varW - 50) / 10;

    logger.debug("SCORES [VAR_X]: " + numerator + "/" + denominator + "=" + varX + "; [VAR_Y]: (" + varX + "-50)/10="
        + varY + "; [VAR_W]: " + mostRecentMean + "/" + mostRecentSD + "=" + varW + "; [VARZ]: (" + varW + "-50)/10="
        + varZ);
    /*
     * Replace the variables in the text with the formatted values
     */
    explanation = StringUtils.replace(explanation, "[VAR_X]", meanFormatter.format(varX));
    explanation = StringUtils.replace(explanation, "[VAR_Y]", sdFormatter.format(varY));
    explanation = StringUtils.replace(explanation, "[VAR_W]", meanFormatter.format(varW));
    explanation = StringUtils.replace(explanation, "[VAR_Z]", sdFormatter.format(varZ));

    return explanation;
  }

  @Override
  public ChartInfo createLineChart(ArrayList<ChartScore> stats, XYDataset ds, Study study,
      ChartConfigurationOptions opts) {

    ArrayList<Study> studies = new ArrayList<>();
    studies.add(study);
    ChartInfo chartInfo = new ChartInfo("PROMIS", stats, ds, study);
    final JFreeChart chart = new JFreeChart(study.getTitle(), ChartMaker.getFont(opts, Font.PLAIN,
        ChartMaker.TITLEFONTSIZE), getPlot(chartInfo, studies, opts), true);
    chart.removeLegend();
    chartInfo.setChart(chart);
    return chartInfo;

  }

  private ArrayList<Element> sortItems(NodeList itemList) {
    ArrayList<Element> sortedList = new ArrayList<>();
    for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
      Element itemNode = (Element) itemList.item(itemInx);
      sortedList.add(itemNode);
    }
    PROMISItemElementComparator<Element> promisItemElementComparater = new PROMISItemElementComparator<>();
    Collections.sort(sortedList, promisItemElementComparater);
    return sortedList;
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study,
      Patient patient, boolean allAnswers) {
    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
    if (patientStudies == null || study == null || patient == null) {
      return questions;
    }
    PatientStudyExtendedData patStudy = null;
    for (PatientStudyExtendedData patientStudy : patientStudies) {
      if (patientStudy.getStudyCode().intValue() == study.getStudyCode().intValue()) {
        patStudy = patientStudy;
      }
    }
    return getSurvey(patStudy, study, patient, allAnswers);
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(PatientStudyExtendedData patStudy, PrintStudy study, Patient patient,
      boolean allAnswers) {
    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
    if (patStudy == null) {
      return questions;
    }

    NodeList itemList;
    try {
      itemList = PROMISAssessmentUtils.getDocumentItems(patStudy.getContents());

      if (itemList != null) {
        for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
          Element itemNode = (Element) itemList.item(itemInx);
          questions.add(PROMISAssessmentUtils.getQuestion(itemNode, version, allAnswers));
        }
      }
    } catch (Exception e) {
      logger.error("Error parsing document for token " + patStudy.getToken() + " study " + patStudy.getStudyCode(), e);
    }
    return questions;
  }

  @Override
  public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient) {
    return new Table();
  }

  @Override
  public XYPlot getPlot(ChartInfo chartInfo, ArrayList<Study> studies,
      ChartConfigurationOptions opts) {
    CustomXYErrorRenderer renderer = new CustomXYErrorRenderer(chartInfo, true, true);
    return getPlot(renderer, chartInfo.getDataSet(), studies, opts);

  }

  private XYPlot getPlot(org.jfree.chart.renderer.xy.XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
      ChartConfigurationOptions opts) {
    final NumberAxis rangeAxis = new NumberAxis("");
    if (studies == null || studies.size() < 1 || ds == null) {
      return new XYPlot(ds, new DateAxis(), rangeAxis, new org.jfree.chart.renderer.xy.XYErrorRenderer());
    }
    TimeSeriesCollection collection = (TimeSeriesCollection) ds;
    final CustomDateAxis domainAxis = new CustomDateAxis(collection);
    Date endDate = null;
    if (endDate == null) {
      endDate = new Date();
    }
    Font labelFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.LABELFONTSIZE);
    Font dtTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.DTTICKFONTSIZE);
    Font numTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.NUMTICKFONTSIZE);
    domainAxis.setLabelFont(labelFont);
    domainAxis.setTickLabelPaint(Color.black);
    domainAxis.setTickLabelFont(dtTickFont);
    domainAxis.setTickLabelInsets(new RectangleInsets(5, 30, 5, 5));
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    rangeAxis.setTickUnit(new NumberTickUnit(10)); // , new
    // SurveyChartAxisFormat()
    rangeAxis.setRange(0, 100);
    rangeAxis.setLabelFont(labelFont);
    rangeAxis.setTickLabelFont(numTickFont);

    ChartMaker.setRendererOptions(renderer, opts, ds.getSeriesCount());
    final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
    plot.setDomainGridlinePaint(Color.black);
    plot.setDomainGridlinesVisible(false);
    plot.setRangeGridlinePaint(Color.black);
    plot.setBackgroundPaint(Color.white);
    plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
    plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
    plot.setOutlineStroke(new BasicStroke(0.0f));
    plot.setInsets(ChartMaker.CHART_INSETS);
    /*
     * Add shading to the chart
     */
    Color lighterBand = opts.getOptionColor(ConfigurationOptions.OPTION_BANDING_COLOR_2);
    Color darkerBand = opts.getOptionColor(ConfigurationOptions.OPTION_BANDING_COLOR_1);
    final IntervalMarker marker1 = new IntervalMarker(70, 85, lighterBand);
    final IntervalMarker marker2 = new IntervalMarker(85, 100, darkerBand);
    plot.addRangeMarker(marker1, Layer.BACKGROUND);
    plot.addRangeMarker(marker2, Layer.BACKGROUND);
    final IntervalMarker targetHigh = new IntervalMarker(80, 90);
    targetHigh.setLabel("Worse");
    targetHigh.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
    targetHigh.setLabelAnchor(RectangleAnchor.LEFT);
    targetHigh.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
    targetHigh.setPaint(new Color(255, 255, 255, 100));
    plot.addRangeMarker(targetHigh, Layer.BACKGROUND);
    final ValueMarker centerLine = new ValueMarker(50);
    centerLine.setStroke(new BasicStroke(1.2f));
    centerLine.setPaint(Color.BLACK);
    plot.addRangeMarker(centerLine);
    return plot;
  }

  @Override
  public int getReportTextFontSize(PrintStudy study) {
    String size = siteInfo.getProperty(Constants.PROMIS_QUESTION_REPORT_FONTSIZE);
    if (size != null && size.length() > 0) {
      try {
        return Integer.parseInt(size);
      } catch (Exception e) {}
    }
    return 11;
  }


}
