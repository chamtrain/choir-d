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

package edu.stanford.registry.server.reports;

import edu.stanford.registry.server.ResultGeneratorIntf;
import edu.stanford.registry.server.ServerException;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.charts.ChartMaker;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.RandomSetDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PageNumber;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.RegistryShortFormScoreProvider;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.utils.ChartUtilities;
import edu.stanford.registry.server.utils.PDFArea;
import edu.stanford.registry.server.utils.PDFUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.GlobalHealthScore;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.MultiScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientResultType;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

public class PatientReport implements ResultGeneratorIntf {
  protected static final Logger logger = LoggerFactory.getLogger(PatientReport.class);

  protected static final String FOLLOW_UP_REPORT = "FollowUp";

  protected static final int tFontSize = 13;
  protected static final int fontSize = 11; // options (12,11,11)
  protected static final int tblFontSize = 9; // options (10, 9, 8 )

  protected static final int lgnFontSize = 7;
  protected static final int XMARGIN = 20;
  protected static final int YMARGIN = 20;
  protected static final int FOOTER = 12;
  protected static final int GAP = 10;
  protected static final int HALFGAP = 5;
  protected static final int WEEGAP = 3;

  public static final Long VERSION = 1L;
  // PDFont[] timesArray = { PDType1Font.TIMES_ROMAN, PDType1Font.TIMES_BOLD,
  // PDType1Font.TIMES_ITALIC,
  // PDType1Font.TIMES_BOLD_ITALIC };
  PDFont[] helveArray = { PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE,
      PDType1Font.HELVETICA_BOLD_OBLIQUE };
  // PDFont[] couriArray = { PDType1Font.COURIER, PDType1Font.COURIER_BOLD,
  // PDType1Font.COURIER_OBLIQUE,
  // PDType1Font.COURIER_BOLD_OBLIQUE };

  public PDFont[] fontArray = helveArray; // TESTs(timesArray,helveArray,couriArray)

  float titleHeight = PDFUtils.getTextHeight(fontArray[0], tFontSize) * 1.1f;
  public float textHeight = PDFUtils.getTextHeight(fontArray[0], fontSize) * 1.1f;
  public float tblTextHeight = PDFUtils.getTextHeight(fontArray[0], tblFontSize) * 1.1f;

  final public Supplier<Database> dbp;
  final protected Database database;
  final protected SiteInfo siteInfo;
  public PatientDao patientDao;
  ChartUtilities chartUtils;
  HashMap<String, Study> studyCodes = null;
  HashMap<String, SurveySystem> surveySystems = null;
  public Float topOfPage = null;
  protected AssessmentRegistration assessment = null;


  public PageNumber pageCount;
  public PDDocument pdf = null;
  public PDPageContentStream contentStream;
  public PDImageXObject[] legend = new PDImageXObject[3];
  public String report = FOLLOW_UP_REPORT;

  // defaults
  public Color HEADING_BACKGROUND_COLOR = ChartConfigurationOptions.getColor(ConfigurationOptions.CARDINAL);
  public Color HEADING_TEXT_COLOR = Color.WHITE;
  public Color BODYMAP_FILL_COLOR = ChartConfigurationOptions.getColor(ConfigurationOptions.SANDSTONE);
  public Color BODYMAP_STROKE_COLOR = ChartConfigurationOptions.getColor(ConfigurationOptions.BLACK80);
  public Color OPTION_SECTION_BACKGROUND_COLOR = ChartConfigurationOptions.getColor(ConfigurationOptions.SANDSTONE);
  private String LOGO_PATH = "images/ChoirLogo20150529.jpg";
  protected final User user;

  public PatientReport(Supplier<Database> dbp, SiteInfo siteInfo, User user) {
    this.dbp = dbp;
    this.database = dbp.get();
    this.siteInfo = siteInfo;
    patientDao = new PatientDao(dbp.get(), siteInfo.getSiteId(), user);
    chartUtils = new ChartUtilities(siteInfo);
    /* get a studyCode:studyName map of the studies in the patients array */
    studyCodes = PDFUtils.getStudyCodeHash(database);
    surveySystems = PDFUtils.getSurveySystemHash(database);
    pageCount = new PageNumber(0);
    this.user = user;
  }

  public PDDocument makePdf(ArrayList<PatientStudyExtendedData> patientStudies, AssessmentRegistration assessment,
      ChartConfigurationOptions opts, PDDocument pdf) throws IOException, InvalidDataElementException {
    this.assessment = assessment;
    return makePdf(patientStudies, assessment.getAssessmentType(), assessment.getPatientId(), opts, pdf);
  }

  public PDDocument makePdf(ArrayList<PatientStudyExtendedData> patientStudies, String process, String patientId,
      ChartConfigurationOptions opts, PDDocument pdf) throws IOException, InvalidDataElementException {
    this.pdf = pdf;
    if (patientId == null || patientStudies == null || patientStudies.size() < 1) {
      writeError(patientId, "No completed surveys were found");
      return pdf;
    }
    Patient patient = patientDao.getPatient(patientId);
    if (patient == null) {
      writeError(patientId, "Patient not" + patientId);
      return pdf;
    }
    PatientInfo patientInfo = new PatientInfo(patient);
    /* get the study names ordered by print order */
    ArrayList<PrintStudy> printStudies = null;
    try {
      printStudies = getPrintStudies(process);
      report = XMLFileUtils.getInstance(siteInfo).getAttribute(process, "report");
    } catch (Exception e) {
      logger.error("Error getting ordered list of study descriptions from process.xml", e);
      printStudies = new ArrayList<>();
    }
    if (printStudies == null || printStudies.size() < 1) {
      logger.error("Error getting list of studies for process: " + process);
      writeError(patientInfo.getMrn(), "Unknown survey type " + process);
      return pdf;
    }
    HEADING_BACKGROUND_COLOR = ChartConfigurationOptions.getColor(opts
        .getColorOption(ConfigurationOptions.OPTION_HEADING_BACKGROUND_COLOR));
    HEADING_TEXT_COLOR = ChartConfigurationOptions.getColor(opts
        .getColorOption(ConfigurationOptions.OPTION_HEADING_TEXT_COLOR));
    BODYMAP_FILL_COLOR = ChartConfigurationOptions.getColor(
        opts.getColorOption(ConfigurationOptions.OPTION_BODYMAP_FILL_COLOR), 100);
    BODYMAP_STROKE_COLOR = ChartConfigurationOptions.getColor(
        opts.getColorOption(ConfigurationOptions.OPTION_BODYMAP_STROKE_COLOR), 255);
    OPTION_SECTION_BACKGROUND_COLOR = ChartConfigurationOptions.getColor(opts
        .getColorOption(ConfigurationOptions.OPTION_SECTION_BACKGROUND_COLOR));
    /*
     * We have to make the images before opening the content stream or 'out of
     * memory' error will occur!!
     */
    // First get the bodymap images
    HashMap<String, ArrayList<PDImageXObject>> ximages = getImages(printStudies, patientStudies, patientId, opts);
    logger.debug(" Processing " + ximages.size() + " Images");
    // Then create the jfreechart images
    HashMap<Integer, ChartInfo> chartInfoHash = getChartInfo(printStudies, patientStudies, patientId, opts);
    HashMap<Integer, PDImageXObject> charts = new HashMap<>();
    Integer height = getScaledValue(opts, opts.getHeight());
    Integer width = getScaledValue(opts, opts.getWidth());
    int scaleImageBy = 3;
    for (Integer key : chartInfoHash.keySet()) {
      ChartInfo chartInfo = chartInfoHash.get(key);
      Study study = chartInfo.getStudy();
      JFreeChart chart = chartInfo.getChart();
      logger.debug(" Generating jpg for chart " + ((study != null) ? study.getStudyDescription() : "null"));
      PDImageXObject jpg = JPEGFactory.createFromByteArray(pdf, PDFUtils.getImage(chart, width * scaleImageBy, height
          * scaleImageBy));
      charts.put(key, jpg);
    }
    logger.debug(" Processing " + charts.size() + " Charts");
    legend[0] = JPEGFactory.createFromImage(pdf, PDFUtils.squareImg(0, Color.yellow, true));
    legend[1] = JPEGFactory.createFromImage(pdf, PDFUtils.triangleImg(1, Color.yellow, true));
    legend[2] = JPEGFactory.createFromImage(pdf, PDFUtils.circleImg(2, Color.yellow, true));
    PDImageXObject downArrow = null;
    ArrayList<PatientStudyExtendedData> currentPatientStudies = getCurrentPatientStudies(assessment, patientStudies);
    for (PatientStudyExtendedData patientStudy : currentPatientStudies) {
      BufferedImage downArrowImg = null;
      if (downArrowImg == null && patientStudy.wasAssisted()) {
        Color backColor = ChartConfigurationOptions.getColor(opts
            .getColorOption(ConfigurationOptions.OPTION_TABLE_BACKGROUND_COLOR));
        downArrowImg = PDFUtils.downArrow(Color.red, backColor);
        downArrow = JPEGFactory.createFromImage(pdf, downArrowImg);
      }
    }
    float y = -1;
    PDPage page = new PDPage(PDRectangle.LETTER);
    contentStream = PDFUtils.startNewPage(page, contentStream, pdf, fontArray[0], tFontSize);
    y = drawTitle(page, pageCount, patientInfo);

    topOfPage = y;
    y = drawTreatmentSets(page, patientInfo, y); // add a line for each TreatmentSet the patient is in
    int index = 0;
    float startX = XMARGIN;
    float startY = y;
    PDFUtils.writePDFHtml(contentStream, "-", XMARGIN, startY, PDFUtils.pageWidth(page) - XMARGIN, fontArray,
        tblFontSize, OPTION_SECTION_BACKGROUND_COLOR, OPTION_SECTION_BACKGROUND_COLOR);
    if (!getImageFirst()) {
      /* add the measures with print_type "text" */
      try {
        y = drawText(patientStudies, printStudies, patientInfo, opts, page, 0, y);
      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      }
      startY = y;
    }
    /* BODYMAP */
    boolean hasBodyMap = false;
    PDFArea mapArea = new PDFArea();
    PDFArea chartArea = new PDFArea();
    mapArea.setXfrom(XMARGIN);
    mapArea.setYfrom(startY);
    mapArea.setXto(topOfPage);
    mapArea.setYto(startY);
    mapArea.setNextY(y - (fontSize * 1.5f));
    chartArea.setXfrom(PDFUtils.pageWidth(page)/2);
    chartArea.setYfrom(startY);
    chartArea.setXto(topOfPage);
    chartArea.setYto(startY);
    if (printStudies.get(index).hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_IMG) ) {
      Integer printOrder = printStudies.get(index).getPrintOrder();
      ArrayList<PDImageXObject> jpegs = ximages.get(printOrder.toString());
      float mapSpace = page.getMediaBox().getHeight() / 2; // default space required
      if (jpegs != null && jpegs.size() > 0) {
        hasBodyMap = true;
        if (jpegs.get(0)!= null && jpegs.get(0).getHeight() > mapSpace)
          mapSpace = jpegs.get(0).getHeight() *.4f;
      }
      // Check if we need to start a new page for the bodymap & charts
      if (!getImageFirst()) {
        if (needNewPage(y + tblTextHeight, mapSpace)) {
          writeFooter(page);
          page = new PDPage();
          contentStream = PDFUtils.startNewPage(page, contentStream, pdf, fontArray[0], fontSize);
          y = drawTitle(page, pageCount, patientInfo);
          topOfPage = y;
        } else {
          // move down just a bit
          y = y - GAP;
        }
      }
      PDFArea area = drawImage(page, printStudies.get(index), patientStudies, jpegs, y + tblTextHeight, opts);
      mapArea.setXto(area.getXto());
      mapArea.setYto(area.getYto());
      mapArea.setNextY(area.getNextY());
      startX = XMARGIN + 260 + GAP;
      if (FOLLOW_UP_REPORT.equals(report)) {
        /* Add the BODYMAP chart on the followup report */
        PDImageXObject jpeg = charts.get(printStudies.get(index).getPrintOrder());
        if (jpeg != null) {
          if (!hasBodyMap) {
            /* if this survey doesn't have a bodymap image draw the chart background */
            float backHeight = lgnFontSize + WEEGAP + height ;
            float backBase = startY - ((fontSize * 3) + backHeight);
            PDFUtils.writeHighlightBox(contentStream, XMARGIN, backBase, PDFUtils.pageWidth(page) - XMARGIN * 2,
                backHeight, OPTION_SECTION_BACKGROUND_COLOR);
          }
          PDFUtils.writePDFHtml(contentStream, "Body Map", startX, startY, PDFUtils.pageWidth(page) - XMARGIN,
              fontArray, tblFontSize);
          startY -= (height + WEEGAP);
          contentStream.drawImage(jpeg, startX, startY, width, height);
          chartArea.setYto(chartArea.getYto() - height);
          chartArea.setXto(chartArea.getXto() + width);
          chartArea.setNextY(chartArea.getYto() + fontSize * 1.5f);
        } else {
          startY = y;
        }
      }
      index++;
      // check the index; it may not be the next one
      for (int i=0; i< printStudies.size(); i++) {
        if (printStudies.get(i) != null && printStudies.get(i).getStudyDescription() != null &&
            RegistryShortFormScoreProvider.studies[RegistryShortFormScoreProvider.PAIN_INTENSITY].equals(
            printStudies.get(i).getStudyDescription().toLowerCase())) {
          index = i;
        }
      }
    }

    boolean intensity = false;
    if (FOLLOW_UP_REPORT.equals(report) && printStudies.size() > index) {
      PrintStudy study = printStudies.get(index);
      /* Pain intensity chart */
      if (study != null
          && study.getStudyDescription() != null
          && RegistryShortFormScoreProvider.studies[RegistryShortFormScoreProvider.PAIN_INTENSITY].equals(study
              .getStudyDescription().toLowerCase())) {
        PDImageXObject jpeg = charts.get(printStudies.get(index).getPrintOrder());
        if (jpeg != null) {
          intensity = true;
          if (!hasBodyMap) {
            /* if this survey doesn't have a bodymap image draw the chart background */
            float backHeight = lgnFontSize + WEEGAP + height - (tblFontSize * 2);
            float backBase = startY - ((fontSize * 3) + backHeight);
            PDFUtils.writeHighlightBox(contentStream, XMARGIN, backBase, PDFUtils.pageWidth(page) - XMARGIN * 2,
                ((fontSize * 3) + backHeight), OPTION_SECTION_BACKGROUND_COLOR);
          }
          startY -= tblTextHeight;
          PDFUtils.writePDFHtml(contentStream, "Pain Intensity", startX, startY, PDFUtils.pageWidth(page) - XMARGIN,
              fontArray, tblFontSize);
          float legendX = startX + 125f;
          contentStream.drawImage(legend[1], legendX, startY + 2f, 12f, 3f);
          legendX += 15f;
          PDFUtils.writePDFHtml(contentStream, "Worst", legendX, startY, PDFUtils.pageWidth(page) - XMARGIN, fontArray,
              lgnFontSize);
          legendX += 20f;
          contentStream.drawImage(legend[0], legendX, startY + 2f, 12f, 3f);
          legendX += 15f;
          PDFUtils.writePDFHtml(contentStream, "Average", legendX, startY, PDFUtils.pageWidth(page) - XMARGIN,
              fontArray, lgnFontSize);
          startY -= WEEGAP;
          startY -= height;

          contentStream.drawImage(jpeg, startX, startY, width, height);
          chartArea.setYto(chartArea.getYto() - height);
          chartArea.setNextY(chartArea.getYto() + fontSize * 1.5f);
        }
        index++;
      }

      if (!intensity) {
        chartArea.setYto((PDFUtils.writePDFHtml(contentStream, "There are no Pain Intensity surveys for this patient", page.getMediaBox()
            .getWidth() * .5f, topOfPage - fontSize - GAP , PDFUtils.pageWidth(page) - XMARGIN, fontArray, fontSize)).getYto());
        chartArea.setNextY(chartArea.getYto() + fontSize * 1.5f);
      }
      y = mapArea.getNextY();
      if (chartArea.getNextY() < mapArea.getNextY()) {
        y = chartArea.getNextY();
      }
      if (!hasBodyMap) {
        y = y - 60f; // move down so promis measures don't overlap
      }
      y += GAP;
      /* Add the promis tables and charts */
      y = drawCharts(patientStudies, printStudies, chartInfoHash, charts, patientInfo, opts, page, index, y, startX);
      y -= GAP;
      drawDownArrow(downArrow, " <i>Indicates an assisted survey</i>", true, startX, y, opts, page, XMARGIN);
      y -= GAP;

    } else { /* INITIAL */

      /* Combine all the promis measures into the same printOrder */
      Integer newChartPrintOrder = 1;
      ArrayList<PrintStudy> newChartPrintStudies = new ArrayList<>();
      for (PrintStudy printStudy : printStudies) {
        if (printStudy.getSurveySystemName().startsWith("PROMIS")
            || printStudy.getSurveySystemName().startsWith("LocalPromis")
            || RegistryShortFormScoreProvider.studies[RegistryShortFormScoreProvider.GLOBAL_HEALTH]
            .equals(printStudy.getStudyDescription().toLowerCase())
            || RegistryShortFormScoreProvider.studies[RegistryShortFormScoreProvider.PARENT_GLOBAL_HEALTH]
            .equals(printStudy.getStudyDescription().toLowerCase())
            || printStudy.getSurveySystemName().startsWith("StanfordCat")) {
          PrintStudy newPrintStudy = printStudy.clone();
          newPrintStudy.setPrintOrder(newChartPrintOrder);
          newChartPrintStudies.add(newPrintStudy);
        }
      }

      /* create a combined chartInfo hashmap */
      HashMap<Integer, ChartInfo> newChartInfo = getChartInfo(newChartPrintStudies, patientStudies, patientId, opts);
      logger.debug("Initial report has " + newChartPrintStudies.size() + "studies with "
          + newChartInfo.size() + " charts");

      if (newChartPrintStudies.size()  > 0 && newChartInfo.size() > 0) {
        float scoresTableW = ((PDFUtils.pageWidth(page) - (XMARGIN * 2)) / 2);
        drawChartBackgroundBox(opts, scoresTableW, startX, y, (textHeight * 2));
        chartArea = drawScoresTable(newChartInfo.get(newChartPrintOrder), newChartPrintStudies,
          newChartPrintOrder, opts, startX,
          y + 7, scoresTableW, "PROMIS Outcomes Measures");

        PDFUtils.writeHighlightBox(contentStream, chartArea.getXto(), chartArea.getYto(), (PDFUtils.pageWidth(page) - XMARGIN) - chartArea.getXto(),
          chartArea.getYfrom() - chartArea.getYto(), OPTION_SECTION_BACKGROUND_COLOR);

        y = chartArea.getNextY();
      } else {
        if (!hasBodyMap) {
          y -= XMARGIN; // move down
        }
      }
      if (downArrow != null) {
          PDFArea asstArea = drawDownArrow(downArrow, " <i>This was an assisted survey</i>", false, startX + GAP / 2, y, opts, page,
            startX + GAP / 2);
        chartArea.setYto(asstArea.getYto());
      }
      if (mapArea.getYto() > chartArea.getYto()) { // the bodymap was shorter so fill to the end of the chart
        PDFUtils.writeHighlightBox(contentStream, XMARGIN, chartArea.getYto(), chartArea.getXfrom() - XMARGIN,
                                   mapArea.getYto() - chartArea.getYto(), OPTION_SECTION_BACKGROUND_COLOR);
        /* draw a thin bar under both */
        if (downArrow == null) {
          PDFUtils.writeHighlightBox(contentStream, XMARGIN, chartArea.getYto() - HALFGAP, PDFUtils.pageWidth(page) - (XMARGIN * 2),
              HALFGAP, OPTION_SECTION_BACKGROUND_COLOR);
          chartArea.setYto(chartArea.getYto() - WEEGAP);
          y -= HALFGAP;
        }
      } else { // the chart was shorter so fill to the end of the bodymap
        PDFUtils.writeHighlightBox(contentStream, chartArea.getXfrom(), mapArea.getYto(), (PDFUtils.pageWidth(page) - XMARGIN) -chartArea.getXfrom(),
            chartArea.getYto() - mapArea.getYto(), OPTION_SECTION_BACKGROUND_COLOR);
        y = mapArea.getYto();
      }
      y -= HALFGAP; // move down
     }
    y -= HALFGAP; // move down

    y = drawCustom(printStudies, patientStudies, ximages, patientInfo, opts, page, y);
    if (getImageFirst()) {
      /* add the measures with print_type "text" */
      try {
        y = drawText(patientStudies, printStudies, patientInfo, opts, page, 0, y);
      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      }
    }
    writeFooter(page);

    // close the content stream
    if (contentStream != null) {
      logger.debug("closing content stream");
      contentStream.close();
    }
    return pdf;
  }

  public float drawCustom(ArrayList<PrintStudy> printStudies, ArrayList<PatientStudyExtendedData> patientStudies,
                          HashMap<String, ArrayList<PDImageXObject>> ximages, PatientInfo patientInfo,
                          ChartConfigurationOptions opts, PDPage page, Float y )
  throws IOException {
    return y;
  }

  public float drawCharts(ArrayList<PatientStudyExtendedData> patientStudies, ArrayList<PrintStudy> printStudies,
      HashMap<Integer, ChartInfo> chartInfoHash, HashMap<Integer, PDImageXObject> charts, PatientInfo patientInfo,
      ChartConfigurationOptions opts, PDPage page, int index, float y, float startX) throws IOException {
    Integer height = getScaledValue(opts, opts.getHeight());
    Integer width = getScaledValue(opts, opts.getWidth());
    boolean promisHeaderPrinted = false;
    Integer printOrder = -1;
    for (int s = index; s < printStudies.size(); s++) {
      if (printStudies.get(s).hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_CHART)) {
        if (printStudies.get(s).getPrintOrder() > printOrder) { // draw the chart
          logger.debug(" Drawing chart for studyCode " + printStudies.get(s).getStudyCode());
          printOrder = printStudies.get(s).getPrintOrder();
          /*
           *  Check it will fit. If not write the footer and start a new page
           */
          if (needNewPage(y,height)) {
            logger.debug("Starting a new page because " + height + " won't fit at current position on page:" + y );
            writeFooter(page);
            page = new PDPage(PDRectangle.LETTER);
            contentStream = PDFUtils.startNewPage(page, contentStream, pdf, fontArray[0], fontSize);
            y = drawTitle(page, pageCount, patientInfo);
            topOfPage = y;
          }

          startX = XMARGIN;
          if (contentStream != null) {
            contentStream.moveTo(XMARGIN, y);
            if (!promisHeaderPrinted) {
              PDFUtils.writePDFHtml(contentStream, "-", XMARGIN, y - (GAP + WEEGAP), XMARGIN + 2f, fontArray,
                  tFontSize, HEADING_BACKGROUND_COLOR, HEADING_BACKGROUND_COLOR);
              y = nextY(PDFUtils.writePDFHtml(contentStream, "PROMIS Outcomes Measures", XMARGIN + 2f, y
                  - (GAP + WEEGAP), PDFUtils.pageWidth(page) - XMARGIN, fontArray, tFontSize, HEADING_TEXT_COLOR,
                  HEADING_BACKGROUND_COLOR));
              y += HALFGAP;
              promisHeaderPrinted = true;
            }

            PDImageXObject jpeg = charts.get(printOrder);
            if (jpeg != null) {

              logger.debug("drawing chart (w/h):" + width + "/" + height + " for fullsized image of (w/h):" + jpeg.getWidth() + "/"
                  + jpeg.getHeight() + " at position (x/y):" + startX + "/" + y);
              drawChartBackgroundBox(opts, PDFUtils.pageWidth(page) - (XMARGIN * 2), startX, y, height + GAP);
              y += WEEGAP;
              float scoreTableWidth = ((PDFUtils.pageWidth(page)) - (XMARGIN * 2f))/2 - 25;
              drawScoresTable(chartInfoHash.get(printOrder), printStudies, printOrder, opts, startX, y,
                  scoreTableWidth, "Measures");
              y -= height;
              contentStream.drawImage(jpeg, startX + scoreTableWidth + GAP, y, width, height);
              y -= HALFGAP;
            }

          } else {
            throw new IOException("Error: Expected non-null content stream.");
          }
        }
      }
    }

    return y;
  }

  /**
   * Draws the questions and answer text for studies with print_type 'text'
   *
   * @param patientStudies The patients questionnaires
   * @param printStudies   The studies from the patients last survey type
   * @param opts           The report options
   * @param page           The current page on the pdf
   * @param index          The index to start processing the patients questionnaires
   * @param y              The vertical location on the page to begin to write
   * @return The vertical location on the page it finished at
   * @throws Exception
   */
  public float drawText(ArrayList<PatientStudyExtendedData> patientStudies, ArrayList<PrintStudy> printStudies,
      PatientInfo patientInfo, ChartConfigurationOptions opts, PDPage page, int index, float y) throws Exception {

    if (patientStudies == null || patientStudies.size() < 1) {
      return y;
    }

    float xEdge = PDFUtils.pageWidth(page) - XMARGIN;
    float xMidLeft = page.getMediaBox().getWidth() * .64f;
    float xMidRight = xMidLeft + WEEGAP;
    String patientId = patientStudies.get(0).getPatientId();

    // Get subset of the patient studies which are part of the current assessment
    ArrayList<PatientStudyExtendedData> currentPatientStudies = getCurrentPatientStudies(assessment, patientStudies);

    /**
     * Print the questions and answers
     */
    final Color oddColor = ChartConfigurationOptions.getColor(opts.getColorOption(ConfigurationOptions.OPTION_ODD_COLOR));
    final Color evenColor = ChartConfigurationOptions.getColor(opts.getColorOption(ConfigurationOptions.OPTION_EVEN_COLOR));

    final Patient patient = patientDao.getPatient(patientId);

    // Print the questions and answers

    boolean odd = true;
    boolean newPage = true;
    String surveyTitle = null;
    int lastQuestionFontSize = 0;

    for (int s = index; s < printStudies.size(); s++) {
      logger.debug("Processing Q&A Text for printstudy (" + printStudies.get(s).getStudyCode() + ") "
          + printStudies.get(s).getStudyDescription());
      try {
        if (printStudies.get(s).hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_TEXT)) {

          ScoreProvider provider = SurveyServiceFactory.getFactory(siteInfo).getScoreProvider(
              dbp, printStudies.get(s).getSurveySystemName(), printStudies.get(s).getStudyDescription());

          // Only get the questions and answers from the current assessment
          ArrayList<SurveyQuestionIntf> questions = provider.getSurvey(
              currentPatientStudies, printStudies.get(s), patient, false);
          String lastText = null;
          //logger.debug(questions.size() + " questions for " + printStudies.get(s).getStudyDescription() + " returned from provider " + provider.getClass().getCanonicalName());
          int questionFontSize = provider.getReportTextFontSize(printStudies.get(s));
          int qPrt = 0;
          for (int qInx = 0; qInx < questions.size(); qInx++) {
            ArrayList<String> questionText = questions.get(qInx).getText();
            if (questions.get(qInx).getAnswered()) {
              if (printStudies.get(s).getTitle() != null && printStudies.get(s).getTitle().length() > 0) {
                surveyTitle = " " + printStudies.get(s).getTitle();
              }
              int indent = 2;
              float qSpace = xMidLeft - (XMARGIN + indent);
              float aSpace = xEdge - (xMidRight + indent);

              float space = getQuestionSpaceNeeded(questions, qInx, opts, printStudies.get(s).getTitle(), questionFontSize, qSpace, aSpace);
              // check the next question will fit (2 and title if first question)
              // if not start new page
              if (y != topOfPage && needNewPage(y,space)) {
                writeFooter(page);
                page = new PDPage(PDRectangle.LETTER);
                contentStream = PDFUtils.startNewPage(page, contentStream, pdf, fontArray[0], tFontSize);
                y = drawTitle(page, pageCount, patientInfo);
                newPage = true;
              }

              /*
               * If the study has a title, write it before the first question
               */
              if ((qPrt == 0 && printStudies.get(s).getTitle() != null && printStudies.get(s).getTitle().length() > 0)
                  || newPage) {
                if (surveyTitle != null) {
                  if (!newPage && lastQuestionFontSize > 0 && lastQuestionFontSize < fontSize) {
                    /* move down a little more because the title is a larger font than the last thing written */
                    y -= textHeight - (PDFUtils.getTextHeight(fontArray[0], questionFontSize) * 1.1f);
                  }
                  y -= HALFGAP;
                  PDFUtils.writePDFHtml(contentStream, "-", XMARGIN, y, XMARGIN + 2f, fontArray, tFontSize,
                      HEADING_BACKGROUND_COLOR, HEADING_BACKGROUND_COLOR);
                  y = nextY(PDFUtils.writePDFHtml(contentStream, surveyTitle, XMARGIN + 2f, y, PDFUtils.pageWidth(page)
                      - XMARGIN, fontArray, tFontSize, HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR));

                  if (questionFontSize < fontSize) {
                    /* move up a little more because the question is smaller than the title  */
                    y += textHeight - (PDFUtils.getTextHeight(fontArray[0], questionFontSize) * 1.1f);
                  }
                  odd = true;
                  newPage = false;
                }
              }

              contentStream.setFont(fontArray[0], questionFontSize);
              Color banding = oddColor;
              if (!odd) {
                banding = evenColor;
              }

              /*
               * Write the question (slightly indented so not right up against the
               * edge)
               */
              PDFArea qArea = new PDFArea();
              qArea.setYto(y);
              qArea.setNextY(y);

              for (int qtInx = 0; qtInx < questionText.size(); qtInx++) {
                if (questionText.get(qtInx) != null && questionText.get(qtInx).length() > 0) {
                  if (qInx == 0 || qtInx > 0 || lastText == null ||
                      !lastText.equals(questionText.get(qtInx))) {
                    String qString = StringEscapeUtils.unescapeHtml3(questionText.get(qtInx));
                    qArea = PDFUtils.writePDFHtml(contentStream, qString, XMARGIN + indent, qArea.getNextY(), xMidLeft,
                        fontArray, questionFontSize, Color.BLACK, banding);
                    PDFUtils.writeHighlightBox(contentStream, XMARGIN, qArea.getYto(), indent,
                        qArea.getYfrom() - qArea.getYto(), banding);
                    if (qtInx == 0 || lastText == null) {
                      lastText = questionText.get(qtInx);
                    }
                  }
                  indent = XMARGIN;
                }
              }
              ArrayList<SurveyAnswerIntf> answers = questions.get(qInx).getAnswers(true);

              // Write the answer (also indented)
              PDFArea aArea = new PDFArea();
              aArea.setYto(y);
              aArea.setNextY(y);
              indent = 2;

              /* If multiple choice concatenate the answers */
              for (SurveyAnswerIntf ans : answers) {
                ArrayList<String> ansStrings;
                if (ans.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_RADIO) {
                  String ansValue = ans.getAttribute(edu.stanford.registry.shared.survey.Constants.DESCRIPTION);
                  if (ansValue == null || ansValue.length() == 0) {
                    ansValue = ans.getAttribute(edu.stanford.registry.shared.survey.Constants.REPORT_RESPONSE_TEXT);
                  }
                  if (ansValue == null || ansValue.length() == 0) {
                    ansValue = ans.getAttribute(edu.stanford.registry.shared.survey.Constants.VALUE);
                  }
                  ansStrings = new ArrayList<>();
                  ansStrings.add(ansValue);
                } else {
                  ansStrings = ans.getResponse();
                }

                if (ans.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_SELECT) {
                  aArea = drawSelectAnswer(ans, aArea, xMidRight, xEdge, questionFontSize, banding);
                } else {
                  for (String ansString : ansStrings) {
                    if (ansString != null && ansString.length() > 0) {
                      aArea = PDFUtils.writePDFHtml(contentStream, ansString, xMidRight + indent,
                          aArea.getNextY(), xEdge, fontArray, questionFontSize, Color.BLACK, banding);
                      PDFUtils.writeHighlightBox(contentStream, xMidRight, aArea.getYto(), indent,
                          aArea.getYfrom() - aArea.getYto(), banding);
                    }
                  }
                }
              }
              logger.info(" question.getNextY = " + qArea.getNextY() + " answer.getNextY= " + aArea.getNextY());
              y = aArea.getNextY();

              if (qArea.getNextY() < y) { // If the question was longer use that y
                // instead & add highlighting in the
                // answer column
                y = qArea.getNextY();
                PDFUtils.writeHighlightBox(contentStream, xMidRight, qArea.getYto(), xEdge - xMidRight, aArea.getNextY()
                    - qArea.getNextY(), banding);
              } else if (qArea.getNextY() > y) {// if the answer was longer add highlighting to the question column
                // add highlighting to the
                // question column
                PDFUtils.writeHighlightBox(contentStream, XMARGIN, aArea.getYto(), xMidLeft - XMARGIN, qArea.getYto()
                    - aArea.getYto(), banding);
                logger.info("Answer to question " + qInx + " was longer.  a.getYto= " + aArea.getYto() + " q.getYto= "
                    + qArea.getYto());
              }
              lastQuestionFontSize = questionFontSize;
              odd = !odd;
              qPrt++;
            }
          }

        }
        if (printStudies.get(s).hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_TABLE)) {
          ScoreProvider provider = SurveyServiceFactory.getFactory(siteInfo).getScoreProvider(
              dbp, printStudies.get(s).getSurveySystemName(), printStudies.get(s).getStudyDescription());
          // Create table from all patient studies
          Table reportTable = provider.getTable(patientStudies, printStudies.get(s), patientInfo.getPatient());
          if (reportTable != null) {
            ArrayList<TableRow> rows = reportTable.getRows();
            if (rows != null && rows.size() > 0) {
              float tableSpace = 0;
              if (reportTable.getHeadings() != null && reportTable.getHeadings().size() > 0) {
                tableSpace += reportTable.getHeadings().size();
              }
              tableSpace += rows.size();
              tableSpace = tableSpace * textHeight * 1.2f;
              // check the table will fit or start new page
              y -= GAP;
              if (y != topOfPage && needNewPage(y, tableSpace)) {
                writeFooter(page);
                page = new PDPage(PDRectangle.LETTER);
                contentStream = PDFUtils.startNewPage(page, contentStream, pdf, fontArray[0], fontSize);
                y = drawTitle(page, pageCount, patientInfo);
                y -= WEEGAP;
                newPage = true;
              }
              Float pageWidth = PDFUtils.pageWidth(page) - XMARGIN;
              for (String heading : reportTable.getHeadings()) {
                PDFUtils.writePDFHtml(contentStream, "-", XMARGIN, y, XMARGIN + 2f, fontArray, tFontSize,
                    HEADING_BACKGROUND_COLOR, HEADING_BACKGROUND_COLOR);
                y = nextY(PDFUtils.writePDFHtml(contentStream, heading, XMARGIN + 2f, y, pageWidth, fontArray,
                    tFontSize, HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR));
              }
              odd = true;
              for (TableRow row : rows) {
                if (row != null && row.getColumns() != null) {
                  // write background
                  Color bColor = odd ? oddColor : evenColor;
                  Point pt = drawRow(row, pageWidth, bColor, new Point(XMARGIN, (int) y));
                  y = pt.y;
                }
              }
            }

          }
        }
      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
        y = nextY(PDFUtils.writePDFHtml(contentStream, "* * * Failure occurred writing report details for " + printStudies.get(s).getStudyDescription()
            +" * * * ",XMARGIN + 2f, y, PDFUtils.pageWidth(page) - XMARGIN, fontArray, tFontSize, HEADING_BACKGROUND_COLOR, HEADING_TEXT_COLOR));
        y = nextY(PDFUtils.writePDFHtml(contentStream, "* * * CONTACT CHOIR TECHNICAL SUPPORT * * *",
            XMARGIN + 2f, y, PDFUtils.pageWidth(page) - XMARGIN, fontArray, tFontSize, HEADING_BACKGROUND_COLOR, HEADING_TEXT_COLOR));
      }
    }
    return y;
  }

  protected ArrayList<PatientStudyExtendedData> getCurrentPatientStudies(AssessmentRegistration asmt, ArrayList<PatientStudyExtendedData> patientStudies) {
    if (asmt == null) {
      throw new ServerException("The AssessmentRegistration has not been set.");
    }

    ArrayList<Long> surveyRegIds = new ArrayList<>();
    for(SurveyRegistration surveyReg : asmt.getSurveyRegList()) {
      surveyRegIds.add(surveyReg.getSurveyRegId());
    }

    ArrayList<PatientStudyExtendedData> currentPatientStudies = new ArrayList<>();
    for(PatientStudyExtendedData patStudy : patientStudies) {
      if (surveyRegIds.contains(patStudy.getSurveyRegId())) {
        currentPatientStudies.add(patStudy);
      }
    }
    return currentPatientStudies;
  }

  protected PDFArea drawSelectAnswer(SurveyAnswerIntf ans,  PDFArea aArea, float xStart, float xEnd, float questionFontSize,
                                  Color banding) throws IOException {
    ArrayList<String> ansStrings = ans.getResponse();
    StringBuilder answerText = new StringBuilder();
    for (int atInx = 0; atInx < ansStrings.size(); atInx++) {
      if (atInx > 0) {
        answerText.append(", ");
      }
      answerText.append(ansStrings.get(atInx));
    }
    if (answerText.length() > 0) {
      aArea = PDFUtils.writePDFHtml(contentStream, answerText.toString(), xStart  + 2,
          aArea.getNextY(), xEnd, fontArray, questionFontSize, Color.BLACK, banding);
      PDFUtils.writeHighlightBox(contentStream, xStart, aArea.getYto(), 2,
          aArea.getYfrom() - aArea.getYto(), banding);
    }
    return aArea;
  }

  protected Point drawRow(TableRow row, Float pageWidth, Color bColor, Point xy) throws IOException {

    float y = xy.y;
    float x = xy.x + 2; // indent the first columns text
    logger.info("drawRow painting background from " + XMARGIN + " to " + pageWidth + " at " + y);
    //PDFUtils.writePDFBackground(contentStream, " - ", XMARGIN, y, pageWidth, fontArray, fontSize, bColor);
    PDFUtils.writePDFBackground(contentStream, " - ", XMARGIN, y, x, fontArray, fontSize, bColor);
    xy.x += 2; // indent the first columns text
    ArrayList<TableColumn> columns = row.getColumns();
    int lineWidth = pageWidth.intValue() - (int) x;
    float endX = x;
    logger.info("pain intensity drawRow starting at " + x + " with " + columns.size() + " columns and lineWidth "
        + lineWidth);
    int colWidth = lineWidth;

    for (int c = 0; c < columns.size(); c++) {

      TableColumn column = columns.get(c);
      if (column != null) {
        if (column.getWidth() > 0) {
          colWidth = lineWidth * column.getWidth() / 100;
        }

      }
      endX += colWidth;

      if (endX > pageWidth) {
        endX = pageWidth.intValue();
      }
      y = nextY(PDFUtils.writePDFHtml(contentStream, columns.get(c).getValue(), xy.x, xy.y, endX, fontArray, fontSize,
          Color.black, bColor));
      xy.x = (int) endX;
      if (row.getColumnGap() > 0 && c < (columns.size() - 1)) {
        logger.info("drawRow painting gap from " + xy.x + " to " + xy.x + row.getColumnGap() + " at " + xy.y);
        xy.x += row.getColumnGap();
      }
      x = xy.x;
    }
    /* fill in the end */
    PDFUtils.writePDFBackground(contentStream, " - ", x, xy.y, pageWidth, fontArray, fontSize, bColor);
    xy.y = (int) y;
    return xy;

  }

  public PDFArea drawImage(PDPage page, PrintStudy study, ArrayList<PatientStudyExtendedData> patientStudies,
                           ArrayList<PDImageXObject> jpegs, float yCoord, ChartConfigurationOptions opts) throws IOException {
    // Currently the only image we handle is the bodymap
    // UNUSED Color backgroundColor = ChartConfigurationOptions.getColor(opts
    //    .getColorOption(ConfigurationOptions.OPTION_SECTION_BACKGROUND_COLOR));
    float frameWidth = (PDFUtils.pageWidth(page) - (XMARGIN * 2));
    PDFArea area = new PDFArea();
    area.setXfrom(XMARGIN);
    area.setXto(XMARGIN + frameWidth);
    area.setYfrom(yCoord);

    String studyName = study.getStudyDescription().toLowerCase();
    if (studyName.startsWith(RegistryShortFormScoreProvider.studies[RegistryShortFormScoreProvider.BODY_MAP]) ||
        studyName.equals("childpaincurrent") // ChildPainCurrent contains the body map for Pediatric Pain
        ) {
      if (jpegs == null || jpegs.size() < 1) {
        //PDFUtils.drawBoxOutline(contentStream, XMARGIN, yCoord - (fontSize * 5) + 1, frameWidth ,fontSize * 4.5f, OPTION_SECTION_BACKGROUND_COLOR);
        PDFUtils.writeHighlightBox(contentStream, XMARGIN, yCoord - (fontSize * 5) + 1, frameWidth , fontSize * 4.5f,
            OPTION_SECTION_BACKGROUND_COLOR);

        contentStream.moveTo(XMARGIN, yCoord - fontSize * 4);
       PDFArea msgArea = PDFUtils.writePDFHtml(contentStream, "No areas were selected on the bodymap", XMARGIN + GAP, yCoord - fontSize * 2,
            frameWidth, fontArray, fontSize);
            area.setYto( msgArea.getYto() );
            area.setNextY(msgArea.getNextY());
        return area;
      }

      float imgHeight = jpegs.get(0).getHeight() * .4f;
      float imgWidth = jpegs.get(0).getWidth() * .4f;
      float frameHeight = imgHeight + (XMARGIN * 2);

      int startX = XMARGIN;
      if (contentStream != null) {
        contentStream.moveTo(startX, yCoord);
        if (jpegs != null) {
          PDFUtils.drawBoxOutline(contentStream, XMARGIN, yCoord - frameHeight,  frameWidth , frameHeight, OPTION_SECTION_BACKGROUND_COLOR);
          PDFUtils.writeHighlightBox(contentStream, startX, yCoord - frameHeight, frameWidth, frameHeight, OPTION_SECTION_BACKGROUND_COLOR);
          area.setYto(yCoord - frameHeight);
          area.setNextY(area.getYto() - fontSize * 1.5f);
          startX += XMARGIN;
          for (PDImageXObject jpeg : jpegs) {
            if (jpeg != null) {
              PDFUtils.writeHighlightBox(contentStream, startX, yCoord - (imgHeight + HALFGAP), imgWidth, imgHeight,
                  Color.white);
              contentStream.drawImage(jpeg, startX, yCoord - (imgHeight + HALFGAP), imgWidth, imgHeight);
              startX += imgWidth;
            }
          }
          yCoord -= (imgHeight + HALFGAP);
        }
        PatientStudyExtendedData pData = null;
        for (PatientStudyExtendedData patientStudy : patientStudies) {
          if (patientStudy.getStudyCode().intValue() == study.getStudyCode().intValue()) pData = patientStudy;
        }
        if (pData != null) {
          ScoreProvider provider = SurveyServiceFactory.getFactory(siteInfo)
              .getScoreProvider(dbp, study.getSurveySystemName(), study.getStudyDescription());
          logger.debug("Score provider for " + study.getStudyDescription() + " system "  + study.getSurveySystemName() + " is " +
              provider.getClass().getCanonicalName());
          ArrayList<ChartScore> scores = provider.getScore(pData);
          if (scores != null) {
            Double ttl = 0.0;
            int indx = scores.size() - 1;
            if (scores.get(indx) instanceof LocalScore) {
              LocalScore lscore = (LocalScore) scores.get(indx);
              ArrayList<BigDecimal> answers = lscore.getAnswers();
              for (BigDecimal answer : answers) {
                ttl = ttl + answer.doubleValue();
              }
            }
            PDFUtils.writePDFHtml(contentStream, ttl.intValue() + " areas selected on the most recent body map ",
                XMARGIN * 2, area.getYto() + XMARGIN, frameWidth / 2 - HALFGAP, fontArray, tblFontSize);
          }
       }
      } else {
        throw new IOException("Error: Expected non-null content stream.");
      }
      return area;
    }
    return area;
  }

  public float drawTitle(PDPage page, PageNumber pageCount, PatientInfo patientInfo) throws IOException {
    /*
     * Writes page heading rows last to first to minimize the gap between lines
     */
    PDFont font = fontArray[0];
    pageCount.increment();
    float textHeight = PDFUtils.getTextHeight(fontArray[0], tFontSize);
    float sp = textHeight * 0.2f;
    float y = (page.getMediaBox().getHeight() - YMARGIN) - GAP * 2;
    // get the choir logo
    BufferedImage logo = PDFUtils.getImage(LOGO_PATH);
    float boxHeight = textHeight + 11;
    if (logo != null) {
      if (logo.getHeight() > textHeight) {
        y -= logo.getHeight()/4 - (textHeight + (WEEGAP * 2)); // add space for the image height and lines
        boxHeight = logo.getHeight()/4 + (WEEGAP * 2) -2;
      }
    }

    y -= sp * 3;
    contentStream.setFont(font, tFontSize);
    String pageText = "Page " + pageCount.intValue();



    // line 3: MRN ..... GENDER
    float y3 = y - (textHeight * 2 + WEEGAP);
    y3 += sp;
    float boxMargin = XMARGIN + 4f;
    y3 = drawTitleLine3(page, patientInfo, y3, boxMargin);
    // line 2 NAME .....
    PDFUtils.drawBoxOutline(contentStream, XMARGIN, y - boxHeight, PDFUtils.pageWidth(page) - (XMARGIN * 2), boxHeight,
        OPTION_SECTION_BACKGROUND_COLOR);
    float y2 = y - (textHeight);
    drawTitleLine2(page, patientInfo, y2, boxMargin);
    // line 1 LOGO - TITLE - PAGE#
    if (logo != null) {
      PDImageXObject jpg = JPEGFactory.createFromImage(pdf, logo);
      contentStream.drawImage(jpg, boxMargin, y + 3, logo.getWidth()/4, logo.getHeight()/4);
    }
    PDFUtils.drawBoxOutline(contentStream, XMARGIN, y, PDFUtils.pageWidth(page) - (XMARGIN * 2), boxHeight,
        OPTION_SECTION_BACKGROUND_COLOR);

    // center the report title

    float x = ((page.getMediaBox().getWidth() - boxMargin - ((font.getStringWidth(getResultTitle()) / 1000) * tFontSize)) / 2);
    PDFUtils.writePDFHtml(contentStream, getResultTitle(), x + 50, y + 10, PDFUtils.pageWidth(page)
        - boxMargin, fontArray, tFontSize, Color.BLACK, Color.white); //
    x = page.getMediaBox().getWidth() - boxMargin - ((font.getStringWidth(pageText) / 1000) * tFontSize) - 2;
    nextY(PDFUtils.writePDFHtml(contentStream, pageText, x, y + 10, PDFUtils.pageWidth(page) - boxMargin, fontArray,
        tFontSize, Color.BLACK, Color.white)); //

    // Adjust up a bit
    return y3 + WEEGAP;
  }

  public float drawTitleLine3(PDPage page, PatientInfo patientInfo, float y3, float boxMargin) throws IOException {
    PDFont font = fontArray[0];
    String mrnText = "MRN " + siteInfo.getPatientIdFormatter().printFormat(patientInfo.getMrn());
    String genderText = "Gender " + patientInfo.getGender();
    PDFUtils.writePDFHtml(contentStream, mrnText, XMARGIN + 4f, y3, PDFUtils.pageWidth(page) - boxMargin, fontArray,
        tFontSize, Color.BLACK, Color.white);
    float x = page.getMediaBox().getWidth() - boxMargin - ((font.getStringWidth(genderText) / 1000) * tFontSize) - 2;
    y3 = nextY(PDFUtils.writePDFHtml(contentStream, genderText, x, y3, PDFUtils.pageWidth(page) - boxMargin, fontArray,
        tFontSize, Color.BLACK, Color.white));
    return y3;
  }

  public float drawTitleLine2(PDPage page, PatientInfo patientInfo, float y2, float boxMargin) throws IOException {
    String ageText = "DOB " + patientInfo.getBirthDt() + " Age " + patientInfo.getAge();
    PDFont font = fontArray[0];
    PDFUtils.writePDFHtml(contentStream, patientInfo.getPatientName(), boxMargin, y2, PDFUtils.pageWidth(page) - boxMargin,
        fontArray, tFontSize, Color.BLACK, Color.white);
    float x = page.getMediaBox().getWidth() - boxMargin - ((font.getStringWidth(ageText) / 1000) * tFontSize) - 2;
    return nextY(PDFUtils.writePDFHtml(contentStream, ageText, x, y2, PDFUtils.pageWidth(page) - boxMargin, fontArray,
        tFontSize, Color.BLACK, Color.white));
  }


  private float drawTreatmentSets(PDPage page, PatientInfo patientInfo, float y) throws IOException {
    if (siteInfo.getRandomSets().isEmpty()) {
      return y;
    }
    RandomSetDao rsetDao = new RandomSetDao(siteInfo, database);
    ArrayList<RandomSetParticipant> list = rsetDao.getTreatmentSetParticipantsForReport(patientInfo.getMrn());
    list.sort(RandomSetParticipant.getComparator(false));  // sort descending, i.e. most recent assignment first

    y -= WEEGAP;
    int fontSize = tFontSize - 2;
    for (RandomSetParticipant rsp: list) {
      String string = getTreatmentSetParticipantString(rsp);
      if (!string.isEmpty()) {
        y = nextY(PDFUtils.writePDFHtml(contentStream, string, XMARGIN, y, PDFUtils.pageWidth(page),
            fontArray, fontSize, Color.BLACK, Color.white));
      }
    }
    return y + WEEGAP - 2;
  }

  final static SimpleDateFormat ymd = new SimpleDateFormat("dd MMM yy");

  private String getTreatmentSetParticipantString(RandomSetParticipant rsp) {
    switch (rsp.getState()) {
    case Assigned:
    case Completed:
    case Withdrawn:
      break;
    default:
      return "";
    }

    String assignedDt = ymd.format(rsp.getAssignedDate());
    String groupName = rsp.getGroup();
    String setName = rsp.getName() + ": ";
    if (!setName.contains("Treatment")) {
      setName = setName + " treatment set ";
    }
    switch (rsp.getState()) {
    case Assigned:
      return setName + "Assigned to group: " + groupName + ", since " + assignedDt;

    case Completed:
      return setName + "Completed, group: " + groupName+" was assigned " + assignedDt;

    case Withdrawn:
      String withdrawnDt = ymd.format(rsp.getWithdrawnDate());
      return setName + "Withdrew " + withdrawnDt + " after being assigned to group " + groupName + " on "+assignedDt;

    default:
      return "";
    }
  }


  public PDFArea drawDownArrow(PDImageXObject downArrow, String message, boolean drawArrow, float x, float y,
      ChartConfigurationOptions opts,
      PDPage page, float startX) throws IOException {
    float drawX = x;
    if (downArrow != null) {
      float width = PDFUtils.pageWidth(page) - (XMARGIN) - startX;
      float yText = y + textHeight -4f;
      y = y + tblTextHeight - 5f;
      PDFUtils.writeHighlightBox(contentStream, startX , y, width, tblTextHeight,
        OPTION_SECTION_BACKGROUND_COLOR);
      if (drawArrow) {
        contentStream.drawImage(downArrow, drawX, yText, 5f, 5f);
        drawX += 7;
      }
      PDFArea assistedArea =
          PDFUtils
      .writePDFHtml(contentStream, message, drawX, yText, PDFUtils.pageWidth(page) - XMARGIN, fontArray, tblFontSize * .75f,
          Color.RED, OPTION_SECTION_BACKGROUND_COLOR);
      assistedArea.setYto(y);
      return assistedArea;
    }
    PDFArea area = new PDFArea();
    area.setXfrom(x);
    area.setXto(x);
    area.setYfrom(y);
    area.setYto(y);
    area.setNextY(y);
    return area;
  }

  public HashMap<Integer, ChartInfo> getChartInfo(ArrayList<PrintStudy> printStudies,
      ArrayList<PatientStudyExtendedData> patientStudies, String patientId, ChartConfigurationOptions opts)
          throws InvalidDataElementException, IOException {
    HashMap<Integer, ChartInfo> chartInfoHash = new HashMap<>();
    return addChartInfo(chartInfoHash, printStudies, patientStudies, patientId, opts);
  }

  private HashMap<Integer, ChartInfo> addChartInfo(HashMap<Integer, ChartInfo> chartInfoHash,
      ArrayList<PrintStudy> printStudies,
      ArrayList<PatientStudyExtendedData> patientStudies, String patientId, ChartConfigurationOptions opts)
          throws InvalidDataElementException, IOException {
    if (printStudies.size() < 1) {
      return chartInfoHash;
    }

    /* get the highest print order */
    Integer printOrderLast = printStudies.get(printStudies.size() - 1).getPrintOrder();
    ArrayList<PrintStudy> studies = new ArrayList<>();
    for (int i = 0; i <= printOrderLast; i++) {
      for (PrintStudy pStudy : printStudies) {
        if (pStudy != null && pStudy.getStudyDescription() != null
            && pStudy.hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_CHART) && pStudy.getPrintOrder() == i) {
          boolean hasStudies = false;

          for (PatientStudyExtendedData patientStudy : patientStudies) {
            if (patientStudy != null
                && pStudy.getStudyCode().intValue() == patientStudy.getStudyCode().intValue()) {
              hasStudies = true;
            }
          }
          if (hasStudies) {
            Study s = studyCodes.get(pStudy.getStudyDescription());
            if (s != null) {
              studies.add(pStudy);
            }
          }
        }
      }
      if (studies.size() > 0) {
        ChartInfo chartInfo = null;
        try {
          chartInfo = getChartInfo(patientStudies, studies, opts);
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        }
        if (chartInfo != null) {
          chartInfoHash.put(i, chartInfo);
        }
        studies = new ArrayList<>();
      }
    }

    return chartInfoHash;
  }

  public HashMap<String, ArrayList<PDImageXObject>> getImages(ArrayList<PrintStudy> studies,
                                                      ArrayList<PatientStudyExtendedData> patientStudies,
                                                      String patientId, ChartConfigurationOptions opts)
          throws IOException {
    HashMap<String, ArrayList<PDImageXObject>> images = new HashMap<>();
    for (PrintStudy study1 : studies) {
      if (study1.hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_IMG)) {
        ArrayList<PDImageXObject> pdjpegs = new ArrayList<>();
        PrintStudy study = study1;
        SurveyServiceFactory ssFactory = SurveyServiceFactory.getFactory(siteInfo);
        ScoreProvider provider = ssFactory.getScoreProvider(dbp, study.getSurveySystemName(), study.getStudyDescription());
        Patient patient = patientDao.getPatient(patientId);
        ArrayList<SurveyQuestionIntf> questions = provider.getSurvey(patientStudies, study1, patient, true);
        for (SurveyQuestionIntf question : questions) {
          if (question.getAnswered()) {
            HashMap<String, BufferedImage> bufImages = PDFUtils.getPaintedImages(question, pdf,
                BODYMAP_STROKE_COLOR, BODYMAP_FILL_COLOR);
            for (String imageName : bufImages.keySet()) {
              /*
               * Make the PD images
               */
              BufferedImage image = bufImages.get(imageName);
              if (image != null) {
                if (image.getWidth() == 263) { // resize female back a touch
                  // bigger to match front
                  image = PDFUtils.resizeImage(image, 1.03f);
                }
                PDImageXObject img = JPEGFactory.createFromImage(pdf, image);
                pdjpegs.add(img);
              }
            }
          }
        }
        images.put(study.getPrintOrder() + "", pdjpegs);
      }
    }
    return images;

  }

  public ArrayList<PrintStudy> getPrintStudies(String processType) {

    SurveySystDao dao = new SurveySystDao(database);
    ArrayList<SurveySystem> surveySystems = dao.getSurveySystems();
    ArrayList<Study> studies = dao.getStudies();
    return XMLFileUtils.getInstance(siteInfo).getPrintStudies(processType, surveySystems, studies, true);
  }


  public PDFArea drawScoresTable(ChartInfo chartInfo, ArrayList<PrintStudy> printStudies, Integer printOrder,
                                 ChartConfigurationOptions opts, float x, float y, float tableWidth, String heading) throws IOException {

    float spWidth = 4f;

    float scorWidth = Math.round(PDFUtils.getTextWidth(fontArray[0], tblFontSize, "Score"));
    float percWidth = Math.round(PDFUtils.getTextWidth(fontArray[0], tblFontSize, "%tile"));
    float catgWidth = Math.round(PDFUtils.getTextWidth(fontArray[0], tblFontSize, "Category") + 26f);
    float lgndWidth = 15f;
    float titlWidth = tableWidth - (spWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + lgndWidth + spWidth);
    x += GAP / 2;
    //float lgndWidth = Math.round(tableWidth - (titlWidth + scorWidth + percWidth + catgWidth + (spWidth * 4)));
    if (!FOLLOW_UP_REPORT.equals(report)) {
      titlWidth += (lgndWidth - (spWidth));
      lgndWidth = spWidth;
    }

    logger.info("tablewidth: " + tableWidth);
    logger.info("titlWidth: " + titlWidth);
    logger.info("scorWidth: " + scorWidth);
    logger.info("percWidth: " + percWidth);
    logger.info("catgWidth: " + catgWidth);
    logger.info("lgndWidth: " + lgndWidth);
    int imageHeight = getScaledValue(opts, opts.getHeight());
    PDFArea returnArea = new PDFArea();
    returnArea.setXfrom(x);
    returnArea.setXto(x + tableWidth);

    Color tableColor = ChartConfigurationOptions.getColor(opts
        .getColorOption(ConfigurationOptions.OPTION_TABLE_BACKGROUND_COLOR));
    float endX = x;
    float backY = y - imageHeight;
    float backH = imageHeight - GAP;
    returnArea.setYfrom(y);
    y -= tblTextHeight;

    // Write the table header

    /** write the table backgound with the same size as the charts **/
    PDFUtils.writeHighlightBox(contentStream, x, backY, tableWidth, backH, tableColor);

    /** write the table header */
    PDFUtils.writeHighlightBox(contentStream, x, y + tblTextHeight / 2, tableWidth, tblTextHeight / 2,
        HEADING_BACKGROUND_COLOR);
    PDFUtils.writePDFHtml(contentStream, "-", x, y, endX + spWidth, fontArray, tblFontSize, HEADING_BACKGROUND_COLOR,
        HEADING_BACKGROUND_COLOR);
    endX += spWidth;

    PDFUtils.writePDFHtml(contentStream, heading, endX, y, endX + titlWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);
    endX += titlWidth;

    PDFUtils.writePDFHtml(contentStream, "Score", endX, y, endX + scorWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);
    endX += scorWidth;

    PDFUtils.writePDFHtml(contentStream, "-", endX, y, endX + spWidth, fontArray, tblFontSize,
        HEADING_BACKGROUND_COLOR, HEADING_BACKGROUND_COLOR);
    endX += spWidth;

    PDFUtils.writePDFHtml(contentStream, "%ile", endX, y, endX + percWidth, fontArray, tblFontSize, HEADING_TEXT_COLOR,
        HEADING_BACKGROUND_COLOR);
    endX += percWidth;

    PDFUtils.writePDFHtml(contentStream, "-", endX, y, endX + spWidth, fontArray, tblFontSize,
        HEADING_BACKGROUND_COLOR, HEADING_BACKGROUND_COLOR);
    endX += spWidth;

    PDFUtils.writePDFHtml(contentStream, "Category", endX, y, endX + catgWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);
    endX += catgWidth;

    y = nextY(PDFUtils.writePDFHtml(contentStream, "-", endX, y, endX + lgndWidth + spWidth, fontArray, tblFontSize,
        HEADING_BACKGROUND_COLOR, HEADING_BACKGROUND_COLOR));
    returnArea.setNextY(y);

    PDFUtils.writeHighlightBox(contentStream, x, y + tblTextHeight / 2, tableWidth, tblTextHeight / 2,
        HEADING_BACKGROUND_COLOR);

    //if (!FOLLOW_UP_REPORT.equals(report)) {
    //  y = returnArea.getNextY();
    //  y -= tblTextHeight / 2;
    //  y -= tblTextHeight / 2;
    //} else {
      y = returnArea.getNextY() - tblTextHeight;
    //}

    endX = x; // back to start
    /*
     * Write the measures
     */
    titlWidth += 8; // give the study title a little more room
    scorWidth -= 8; // score values don't need as much as the heading
    int colorInx = 0;
    boolean printInvertedMessage = false;
    boolean printOldVersions = false;
    PDFArea area;
    for (PrintStudy printStudy1 : printStudies) {
      if (printStudy1.getPrintOrder() == printOrder) {
        PrintStudy printStudy = printStudy1;
        logger.debug("drawScoresTable: study " + printStudy.getStudyDescription() + "(" + printStudy.getStudyCode()
            + ")  has printorder " + printStudy1.getPrintOrder());

        /* move down a bit and then draw the background */
        y -= tblTextHeight;
        if (FOLLOW_UP_REPORT.equals(report) && printStudies.size() > 3) {
          y += tblTextHeight/2; // when > 3 in a graph we need to use a little less space in between
        }
        area = PDFUtils.writePDFHtml(contentStream, "-", x, y, endX + tableWidth, fontArray, tblFontSize, tableColor,
            tableColor);

        endX += spWidth;

        String title = printStudy1.getTitle();
        if (title.indexOf("PROMIS") == 0) {
          title = title.substring(7);
        }
        boolean inverted = false;
        Double score = null;
        String categoryLabel = "";
        ChartScore chartScore = null;
        if (chartInfo != null) {
          ArrayList<ChartScore> scores = chartInfo.getScores();
          for (ChartScore score1 : scores) {
            if (score1.getStudyCode().intValue() == printStudy.getStudyCode().intValue()) {
              chartScore = score1;
              if (score1.wasReplaced()) {
                printOldVersions = true;
              }
            }
          }
        }
        if (chartScore != null) {
          score = chartScore.getScore().doubleValue();
          categoryLabel = chartScore.getCategoryLabel();
          if (printStudy1.getInvert()) {
            logger.debug("printStudy " + printStudy1.getTitle() + " is inverted ");
            inverted = true;
            score = invertScore(score);
          }
        }

        /*
         * Write the study name
         */
        if (chartScore instanceof GlobalHealthScore) {
          title = title + " - Physical";
        }
        if (chartScore instanceof MultiScore) {
          title = ((MultiScore) chartScore).getTitle(1, title);
        }
        if (inverted) {
          title = title + " *";
          printInvertedMessage = true;
        }
        area = PDFUtils.writePDFHtml(contentStream, title, endX, y, endX + titlWidth, fontArray, tblFontSize,
            Color.black, tableColor);
        returnArea.setNextY(nextY(area));
        returnArea.setYto(area.getYto());
        Float lineSize = y - returnArea.getNextY();
        Float multiSpace = 1.4f * lineSize;
        if (!FOLLOW_UP_REPORT.equals(report)) {
          multiSpace = 2f * lineSize;
        }
        if (chartScore instanceof GlobalHealthScore) {
          // Draw additional background
          float ghBackground = 2.5f * lineSize;
          if (!FOLLOW_UP_REPORT.equals(report)) {
            ghBackground = 4 * lineSize;
            PDFUtils.writeHighlightBox(contentStream, x, y - ghBackground, tableWidth, ghBackground, tableColor);
          }

          title = printStudy1.getTitle() + " - Mental";
          if (inverted) {
            title = title + " *";
          }
          PDFUtils.writePDFHtml(contentStream, title, endX, y - multiSpace, endX + titlWidth, fontArray,
              tblFontSize, Color.black, tableColor);
          float moveDown = 2f * lineSize;
          if (FOLLOW_UP_REPORT.equals(report)) {
            moveDown = +8;
          }
          returnArea.setNextY(returnArea.getNextY() - moveDown);
          returnArea.setYto(returnArea.getYto() - moveDown);
        }
        if (chartScore instanceof MultiScore) {
          for (int s=2; s<=((MultiScore) chartScore).getNumberOfScores(); s++) {
            // Draw additional background
            float ghBackground = 2.5f * lineSize;
            if (!FOLLOW_UP_REPORT.equals(report)) {
              ghBackground = 4 * lineSize;
              PDFUtils.writeHighlightBox(contentStream, x, y - (ghBackground * (s-1)), tableWidth, ghBackground, tableColor);
            }

            title = ((MultiScore) chartScore).getTitle(s, printStudy1.getTitle());
            if (inverted) {
              title = title + " *";
            }
            PDFUtils.writePDFHtml(contentStream, title, endX, y - (multiSpace * (s-1)), endX + titlWidth, fontArray,
                tblFontSize, Color.black, tableColor);
            float moveDown = 2f * lineSize;
            if (FOLLOW_UP_REPORT.equals(report)) {
              moveDown = +8;
            }
            returnArea.setNextY(returnArea.getNextY() - moveDown);
            returnArea.setYto(returnArea.getYto() - moveDown);
          }
        }
        endX += titlWidth;

        /*
         * Write score column
         */
        float writeAt = endX;
        if (chartScore == null) {
          PDFUtils.writePDFHtml(contentStream, "-", endX, y, endX + scorWidth, fontArray, tblFontSize, tableColor,
              tableColor);
        } else {
          if (chartScore instanceof GlobalHealthScore) {
            GlobalHealthScore gscore = ((GlobalHealthScore) chartScore);
            logger.debug("GlobalHealth physical raw score = " + gscore.getPhysicalRawScore() + " tscore= "
                + gscore.getPhysicalHealthTScore());
            logger.debug("GlobalHealth mental raw score = " + gscore.getMentalRawScore() + " tscore= "
                + gscore.getMentalHealthTScore());
            score = invertScore(((GlobalHealthScore) chartScore).getPhysicalHealthTScore());
          } else if (chartScore instanceof MultiScore) {
            score = ((MultiScore) chartScore).getScore(1);
            if (inverted) {
              score = invertScore(score);
            }
          }
          writeAt += (scorWidth - PDFUtils.getTextWidth(fontArray[0], tblFontSize, formatScore(score,printStudy)));
          PDFUtils.writePDFHtml(contentStream, formatScore(score,printStudy), writeAt, y, endX + scorWidth, fontArray, tblFontSize,
              Color.black, tableColor);

          if (chartScore instanceof GlobalHealthScore) {
            Double mhScore = invertScore(((GlobalHealthScore) chartScore).getMentalHealthTScore());
            PDFUtils.writePDFHtml(contentStream, formatScore(mhScore,printStudy), writeAt, y - multiSpace, endX + scorWidth,
                fontArray, tblFontSize, Color.black, tableColor);
          } else if (chartScore instanceof MultiScore) {
            for (int s=2; s<= ((MultiScore) chartScore).getNumberOfScores(); s++) {
              Double multiScore = ((MultiScore) chartScore).getScore(s);
              if (inverted) {
                multiScore = invertScore(multiScore);
              }
              PDFUtils.writePDFHtml(contentStream, formatScore(multiScore,printStudy), writeAt, y - multiSpace * (s-1), endX + scorWidth,
                  fontArray, tblFontSize, Color.black, tableColor);
            }
          }

        }
        endX += scorWidth;

        /*
         * Write percentile column
         */
        writeAt = endX;
        if (chartScore == null) {
          PDFUtils.writePDFHtml(contentStream, "-", endX, y, endX + percWidth, fontArray, tblFontSize, tableColor,
              tableColor);
        } else {
          if (chartScore instanceof MultiScore) {
            MultiScore mScore = (MultiScore) chartScore;
            for (int s = 2; s <= mScore.getNumberOfScores(); s++) {
              if (mScore.getPercentileScore(s) != null) {
                Double percScore = mScore.getPercentileScore(s);
                if (inverted) {
                  percScore = invertScore(percScore);
                }
                Long percentile = calculatePercentile(Math.round(percScore), printStudy);
                PDFUtils.writePDFHtml(contentStream, percentile.toString(), writeAt,
                    y - (2 * lineSize) * (s - 1), endX + percWidth,
                    fontArray, tblFontSize, Color.black, tableColor);
              }
            }
          } else {
            Long percentile = calculatePercentile(Math.round(score), printStudy);
            if (percentile != null) {
              logger.debug(printStudy.getStudyDescription() + " Percentile = " + percentile + " for score " + score);
              writeAt += (percWidth - PDFUtils.getTextWidth(fontArray[0], tblFontSize, percentile.toString()));
              PDFUtils.writePDFHtml(contentStream, percentile.toString(), writeAt, y, endX + percWidth, fontArray,
                  tblFontSize, Color.black, tableColor);
            }
            if (chartScore instanceof GlobalHealthScore) {
              Double mhScore = invertScore(((GlobalHealthScore) chartScore).getMentalHealthTScore());
              percentile = calculatePercentile(Math.round(mhScore), printStudy);
              PDFUtils.writePDFHtml(contentStream, percentile.toString(), writeAt, y - multiSpace, endX + percWidth,
                  fontArray, tblFontSize, Color.black, tableColor);
            }
          }
        }
        endX += percWidth + spWidth * 2;

        /*
         * Write category column
         */
        if (chartScore == null) {
          PDFUtils.writePDFHtml(contentStream, "-", endX, y, endX + catgWidth, fontArray, tblFontSize, tableColor,
              tableColor);
        } else {
          if (chartScore instanceof GlobalHealthScore && "parentGlobalHealth".equals(chartScore.getStudyDescription())) {
            categoryLabel = ((GlobalHealthScore) chartScore).getPhysicalCategoryLabel();
          }
          PDFUtils.writePDFHtml(contentStream, categoryLabel, endX, y, endX + catgWidth, fontArray, tblFontSize,
              Color.black, tableColor);
          if (chartScore instanceof GlobalHealthScore && "parentGlobalHealth".equals(chartScore.getStudyDescription())) {
              categoryLabel = ((GlobalHealthScore) chartScore).getMentalCategoryLabel();
              PDFUtils.writePDFHtml(contentStream, categoryLabel, endX, y - multiSpace, endX + catgWidth,
                  fontArray, tblFontSize, Color.black, tableColor);
          }
        }
        endX += catgWidth;

        /*
         * write legend column
         */
        writeAt = endX + (lgndWidth / 2) - 16f;
        if (colorInx >= ChartConfigurationOptions.LINE_COLORS.length) {
          colorInx = 0;
        }
        if (FOLLOW_UP_REPORT.equals(report)) {
          contentStream.drawImage(legend[colorInx], endX, y + (tblTextHeight / 2) - 4f, 16f, 4f);
          if (chartScore instanceof GlobalHealthScore) {
            colorInx++;
            if (colorInx >= ChartConfigurationOptions.LINE_COLORS.length) {
              colorInx = 0;
            }
            contentStream.drawImage(legend[colorInx], endX, y - multiSpace + (tblTextHeight / 2) - 4f, 16f, 4f);
          }
          if (chartScore instanceof MultiScore) {
            for (int s=2; s<= ((MultiScore) chartScore).getNumberOfScores(); s++) {
              colorInx++;
              if (colorInx >= ChartConfigurationOptions.LINE_COLORS.length) {
                colorInx = 0;
              }
              contentStream.drawImage(legend[colorInx], endX, y - multiSpace * (s-1) + (tblTextHeight / 2) - 4f, 16f, 4f);
              if (s==((MultiScore) chartScore).getNumberOfScores()) {
                  addChartSpaceAfterMultiScore(returnArea, lineSize);
              }
            }
          }
        }
        endX = x; // move back to the beginning of the line
        y = returnArea.getNextY();
        colorInx++;
        /* Write a blank line with the table background color */
        if (!FOLLOW_UP_REPORT.equals(report)) {
          PDFUtils.writePDFHtml(contentStream, "-", x, y,
              x + tableWidth, fontArray, tblFontSize, tableColor, tableColor);
        }
      }
    }
    if (printInvertedMessage) {
      if (!FOLLOW_UP_REPORT.equals(report)) {
        PDFUtils.writeHighlightBox(contentStream, x, y - GAP, tableWidth, GAP, tableColor);
        y -= tblTextHeight * .5f;
        area = PDFUtils.writePDFHtml(contentStream, " <i>* Scores and percentiles have been inverted</i> ", x, y,
            tableWidth + x - spWidth, fontArray, tblFontSize * .75f, Color.black, tableColor);
      } else {
        area = PDFUtils.writePDFHtml(contentStream, " <i>* Scores and percentiles have been inverted</i> ", x, backY + 5,
            tableWidth + x , fontArray, tblFontSize * .75f, Color.black, tableColor);
      }
      returnArea.setNextY(nextY(area));
      returnArea.setYto(area.getYto());
      if (!FOLLOW_UP_REPORT.equals(report)) {
        area = PDFUtils.writePDFHtml(contentStream, " - ", x, nextY(area), x + tableWidth, fontArray, tblFontSize * .75f,
            tableColor, tableColor);
        returnArea.setNextY(nextY(area));
        returnArea.setYto(area.getYto());
      }
    }
    if (printOldVersions && FOLLOW_UP_REPORT.equals(report)) {
      if (printInvertedMessage) {
        x = x + (tableWidth * .6f);
      }

      PDImageXObject oldVsArrow = JPEGFactory.createFromImage(pdf, PDFUtils.upArrow(Color.gray, tableColor));
      contentStream.drawImage(oldVsArrow, x, backY + 5, 5f, 5f);
      PDFUtils.writePDFHtml(contentStream, " <i> Indicates older versions</i> ", x + 6f, backY + 5,
         (tableWidth + XMARGIN) , fontArray, tblFontSize * .75f, Color.black, tableColor);
    }
    return returnArea;

  }

  public float drawChartBackgroundBox(ChartConfigurationOptions opts, float bgWidth, float backBoxX, float y,
      float backgBoxH) throws IOException {

    float backgBoxY = y + (textHeight / 2) - backgBoxH;

    PDFUtils.writeHighlightBox(contentStream, backBoxX, backgBoxY, bgWidth, backgBoxH, OPTION_SECTION_BACKGROUND_COLOR);
    return backgBoxY;
  }

  private ChartInfo getChartInfo(ArrayList<PatientStudyExtendedData> pstudies,
      ArrayList<PrintStudy> chosenStudies, ChartConfigurationOptions opts) throws InvalidDataElementException,
      IOException {

    if (chosenStudies == null || chosenStudies.size() < 1) {
      throw new InvalidDataElementException("No patient studies");
    }

    if (pstudies.size() < 1) {
      throw new InvalidDataElementException("No patient studies");
    }
    if (chosenStudies.size() < 1) {
      throw new InvalidDataElementException("Called without any studies");
    }
    Long siteId = siteInfo.getSiteId();
    ScoreProvider provider = chartUtils.getScoreProvider(dbp, pstudies, chosenStudies.get(0).getStudyCode(), siteId);
    ChartInfo chartInfo = null;
    //Combine the series collections and the chartscores into one ChartInfo
    TimeSeriesCollection collection = null;
    ArrayList<ChartScore> chartScores = null;
    for (PrintStudy study : chosenStudies) {
      if (study == null) {
        logger.warn("A study in chosenStudies list was null, of "+chosenStudies.size());
        continue;
      }
      if (provider == null) {
        provider = chartUtils.getScoreProvider(dbp, pstudies, study.getStudyCode(), siteId);
        if (provider == null) {
          logger.error("provider was null, other way of getting provider also ended up with null");
          return null;
        } else {
          logger.error("provider was null, other way of getting provider worked...");
        }
      }
      if (!provider.acceptsSurveyName(study.getStudyDescription())) {
        provider = chartUtils.getScoreProvider(dbp, pstudies, study.getStudyCode(), siteId);
      }
      ChartInfo thisChartInfo = chartUtils.createChartInfo(dbp.get(), pstudies, study, provider, opts);
      if (chartInfo == null) {
        chartInfo = thisChartInfo;
        if (thisChartInfo != null) {
          collection = (TimeSeriesCollection) chartInfo.getDataSet();
          chartScores = chartInfo.getScores();
        }
      } else {
        if (thisChartInfo != null) {
          TimeSeriesCollection thisCollection = (TimeSeriesCollection) thisChartInfo.getDataSet();
          for (int d = 1; d < thisCollection.getSeriesCount(); d++) {
            collection.addSeries(thisCollection.getSeries(d));
          }
          ArrayList<ChartScore> thisScores = thisChartInfo.getScores();
          for (ChartScore thisScore : thisScores) {
            chartScores.add(thisScore);
          }
        }
      }
    }
    if (chartInfo == null) return null;
    chartInfo.setScores(chartScores);
    chartInfo.setDataSet(collection);
    ArrayList<Study> studies = new ArrayList<>();
    for (PrintStudy chosenStudy : chosenStudies) {
      studies.add(chosenStudy);
    }
    XYPlot plot = provider.getPlot(chartInfo, studies, opts);
    if (plot == null) plot = new XYPlot();
    ChartMaker chartMaker = new ChartMaker(chartInfo, opts);
    JFreeChart chart = chartMaker.getChart("", false, plot);
    chartInfo.setChart(chart);
    if (chart == null) throw new InvalidDataElementException("chart returned from chartmaker is null");
    return chartInfo;
  }

  public Integer getScaledValue(ChartConfigurationOptions opts, Integer value) {

    Integer scaleBy = opts.getIntegerOption(ConfigurationOptions.OPTION_SCALE_IMAGES);
    if (scaleBy > 1) {
      double scale = scaleBy / 100.0;
      value = Long.valueOf(Math.round(value * scale)).intValue();
    }
    return value;
  }

  /**
   * This calculates the space needed to print the next question. When handling
   * the first question it also adds in the title plus checks that 2 questions
   * will fit.
   *
   * @param questions The array of questions for this survey
   * @param startInx  The question number we're checking
   * @param opts      Chart configuration options
   * @param title     Title of the survey being written
   * @return Approximate size of the survey
   */
  private float getQuestionSpaceNeeded(ArrayList<SurveyQuestionIntf> questions, int startInx,
      ChartConfigurationOptions opts, String title, int qFontSize, float questionWidth, float answerWidth) {
    float size = 0;
    if (questions.size() <= startInx) {
      return size;
    }

    if (title != null && title.length() > 0 && startInx == 0) { // add the size
      // of the title
      // bar
      size += PDFUtils.getTextHeight(fontArray[0], titleHeight) * 1.10f;
      size += (HALFGAP * 2);
    }

    SurveyQuestionIntf thisQuestion = questions.get(startInx);
    if (thisQuestion == null || thisQuestion.getText() == null || thisQuestion.getText().size() < 1) {
      return size;
    }

    int qTextInx = 0;
    if (startInx > 0 && questions.size() > startInx) {
      SurveyQuestionIntf prevQuestion = questions.get(startInx - 1);
      if (prevQuestion != null && prevQuestion.getText() != null && prevQuestion.getText().size() > 1
          && prevQuestion.getText().get(0) != null
          && prevQuestion.getText().get(0).equals(thisQuestion.getText().get(0))) {
        qTextInx = 1;
      }
    }
    size += questionSize(thisQuestion, qTextInx, qFontSize, questionWidth, answerWidth);

    // if its the first one, also add the next question
    if (startInx == 0 && title != null && title.length() > 0) {
      if (questions.size() > (startInx + 1)) {
        startInx++;
      }
      size += questionSize(questions.get(startInx), qTextInx, fontSize, questionWidth, answerWidth);
    }

    return size;
  }

  public float questionSize(SurveyQuestionIntf question, int startInx, int qFontSize, float questionWidth, float answerWidth) {

    ArrayList<String> questionText = question.getText();
    ArrayList<SurveyAnswerIntf> answers = question.getAnswers(true);
    float questionTextHeight = PDFUtils.getTextHeight(fontArray[0], qFontSize) * 1.1f;

    //
    float answerSize = 0;
    for (SurveyAnswerIntf ans : answers) {
      ArrayList<String> ansStrings = new ArrayList<>();
      if (ans.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_RADIO) {
        String ansValue = ans.getAttribute(edu.stanford.registry.shared.survey.Constants.DESCRIPTION);
        if (ansValue == null || ansValue.length() == 0) {
          ansValue = ans.getAttribute(edu.stanford.registry.shared.survey.Constants.REPORT_RESPONSE_TEXT);
        }
        if (ansValue == null || ansValue.length() == 0) {
          ansValue = ans.getAttribute(edu.stanford.registry.shared.survey.Constants.VALUE);
        }
        ansStrings.add(ansValue);
      }  else {
        ansStrings = ans.getResponse();
      }
      if (ans.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_SELECT) {
        StringBuilder answerText = new StringBuilder();
        for (int atInx = 0; atInx < ansStrings.size(); atInx++) {
          if (atInx > 0) {
            answerText.append(", ");
          }
          answerText.append(ansStrings.get(atInx));
        }
        ansStrings.add(answerText.toString());
      }
      if (ans.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_INPUT) {
        for (String ansString : ans.getResponse())
        ansStrings.add(ansString);
      }
      if (ans.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_DATEPICKER) {
        for (String ansString : ans.getResponse())
          ansStrings.add(ansString);
      }
      for (String ansString : ansStrings) {
        ansString = PDFUtils.removeWinAnsiChars(ansString);
        if (ansString != null && ansString.length() > 0) {
          answerSize++;
          try {
            float width = PDFUtils.getTextWidth(fontArray[0], qFontSize, ansString);
            while (width > answerWidth) {
              answerSize++;
              width = width - answerWidth;
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    float questionSize = 0f;
    for (int q=startInx; q < questionText.size(); q++) {
      questionSize++;
      try {
        float width = PDFUtils.getTextWidth(fontArray[0], qFontSize, questionText.get(q));
        while (width > questionWidth) {
          questionSize++;
          width = width - questionWidth;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (questionSize > answerSize) {
      return (questionSize ) * questionTextHeight * 1.10f;
    }
    return (answerSize - 1) * questionTextHeight * 1.10f;
  }

  public float nextY(PDFArea area) {
    return area.getNextY();
  }


  public void writeError(String patientId, String errorMessage) throws IOException {
    logger.debug(errorMessage + " for patient " + patientId);
    PDPage page = new PDPage(PDRectangle.LETTER);
    contentStream = PDFUtils.startNewPage(page, contentStream, pdf, fontArray[0], tFontSize);
    float y = (page.getMediaBox().getHeight() - YMARGIN) - GAP;
    PDFUtils.writePDFHtml(contentStream, errorMessage, XMARGIN, y, PDFUtils.pageWidth(page) - XMARGIN, fontArray,
        fontSize);
    // close the content stream
    if (contentStream != null) {
      logger.debug("closing content stream");
      contentStream.close();
    }
  }

  public void writeFooter(PDPage page) throws IOException {
    PDFArea area = PDFUtils.writePDFHtml(contentStream, getDocumentControlId(), XMARGIN, YMARGIN, PDFUtils.pageWidth(page) - XMARGIN, fontArray,
        tblFontSize, Color.BLACK, Color.white);
    logger.info("Wrote at " + YMARGIN + " end at " + area.getYfrom() + " start at " + area.getYto() + " size of " + (area.getYfrom() - area.getYto()) );
  }

  public void addChartSpaceAfterMultiScore(PDFArea returnArea, float lineSize) {
    // default does nothing
  }
  protected boolean needNewPage(float currentPosition, float spaceNeeded) {
    return currentPosition < (YMARGIN + FOOTER + spaceNeeded);
  }

  public Long calculatePercentile(Long score, PrintStudy printStudy) {
    NormalDistribution norm = new NormalDistribution(50, 10);
    Double percentile = norm.cumulativeProbability(score) * 100;
    return Math.round(percentile);
  }

  public String formatScore(double score, PrintStudy printStudy) {
    return Long.toString(Math.round(score));
  }

  protected double invertScore(double dscore) {
    long score = Math.round(dscore);
    if (score < 50) {
      score = 50 + (50 - score);
    } else if (score > 50) {
      score = 50 - (score - 50);
    }
    return score;
  }

  public boolean getImageFirst() {
    return true;
  }

  private static final String DOC_ID_NAME = "PARPT";
  private static final String FIELD_SEPARATOR = "/";
  private static final SimpleDateFormat DOC_ID_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd_HH:mm");

  private static final int UPPER_LIMIT = 99999999;

  private Random randomGenerator = new Random(System.currentTimeMillis());
  private String documentControlId = getResultName() + FIELD_SEPARATOR + "v" + getResultVersion() + FIELD_SEPARATOR
      + randomGenerator.nextInt(UPPER_LIMIT) + FIELD_SEPARATOR + DOC_ID_TIME_FMT.format(new Date());

  @Override
  public String getResultName() {
    return DOC_ID_NAME;
  }

  @Override
  public Long getResultVersion() {
    return VERSION;
  }

  @Override
  public String getResultTitle() {
    return getResultType().getResultTitle();
  }

  @Override
  public String getDocumentControlId() {
    return documentControlId;
  }

  @Override
  public PatientResultType getResultType() {
    AssessDao assessDao = new AssessDao(database, siteInfo);
    return assessDao.getPatientResultType(getResultName());
  }
}
