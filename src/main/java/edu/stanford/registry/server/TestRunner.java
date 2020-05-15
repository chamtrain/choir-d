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

package edu.stanford.registry.server;

import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.charts.ChartMaker;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.formatter.PatientIdFormatIntf;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.utils.PDFUtils;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.InvalidPatientIdException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class TestRunner {

  private static final int XMARGIN = 20;
  private static final int YMARGIN = 40;
  private static final int TABLE_WIDTH = 130;

  private static final Color EVEN_COLOR = new Color(210, 194, 149);
  private static final Color BODYMAP_FILL_COLOR = EVEN_COLOR;
  private static final Color BODYMAP_STROKE_COLOR = new Color(77, 79, 83);

  private static final Integer DEFAULT_WIDTH = 450;
  private static final Integer DEFAULT_HEIGHT = 150;
  private static final Integer DEFAULT_SPACER = 20;
  private static final PDFont[] timesArray = { PDType1Font.TIMES_ROMAN, PDType1Font.TIMES_BOLD,
      PDType1Font.TIMES_ITALIC, PDType1Font.TIMES_BOLD_ITALIC };
  private static Logger logger = Logger.getLogger(TestRunner.class);
  private Shape dot = new Ellipse2D.Float(-2.0f, -2.0f, 4.0f, 4.0f);
  ClinicServices service = null;

  final SiteInfo siteInfo;
  TestRunner(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  public void service(HttpServletRequest req, HttpServletResponse res, ClinicServices service) throws ServletException,
      IOException {
    this.service = service;
    doTest(req, res);

  }

  private void doTest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    logger.debug("Doing scores report");
    int testNumber = -1;
    try {
      testNumber = Integer.parseInt(req.getParameter("tno"));
    } catch (Exception e) {
      res.sendError(999, e.getMessage());
    }
    if (testNumber > -1) {
      try {
        doTest(req, res, testNumber);
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        res.sendError(999, e.getMessage());
      }
    }

  }

  private void doTest(HttpServletRequest req, HttpServletResponse res, int testNumber) throws Exception {
    String patientId = req.getParameter("patient");
    Patient pat = checkPatientId(1L, patientId);
    patientId = pat.getPatientId();

    String title = req.getParameter("title");
    if (title == null) {
      title = "";
    }
    String studyCodes[] = req.getParameter("study").split(",");
    if (studyCodes.length == 0) {
      studyCodes = new String[1];
      studyCodes[0] = "1003";
    }
    Integer[] studies = new Integer[studyCodes.length];
    for (int s = 0; s < studies.length; s++) {
      studies[s] = Integer.valueOf(studyCodes[s]);
    }
    ConfigurationOptions options = new ConfigurationOptions(ConfigurationOptions.CHART_TEST, req.getParameter("ops"));
    ChartConfigurationOptions opts = new ChartConfigurationOptions(options);
    Integer scaleBy = opts.getIntegerOption(ConfigurationOptions.OPTION_SCALE_IMAGES);
    Integer height = getInteger(req, "height", DEFAULT_HEIGHT);
    Integer width = getInteger(req, "width", DEFAULT_WIDTH);
    Integer gap = getInteger(req, "gap", DEFAULT_SPACER);

    if (scaleBy > 1) {
      double scale = scaleBy / 100.0;
      height = Long.valueOf(Math.round(height * scale)).intValue();
      width = Long.valueOf(Math.round(width * scale)).intValue();
      logger.error("scaleBy=" + scaleBy + " scale=" + scale + " height=" + height + " width=" + width);
    }

    switch (testNumber) {
    case 1:
      // Get chart of a single study selected for this patient and write as a
      // pdf
      // options.setType(ConfigurationOptions.CHART_TEST);
      sendPdf(req, res, makeLineChart(title, patientId, studies, height, width, gap, opts));
      break;
    case 2:
      // chart multiple studies
      // options = new
      // ConfigurationOptions(ConfigurationOptions.FOLLOWUP_CHART);
      // options.fromString(req.getParameter("ops"));
      // opts = new ChartConfigurationOptions(options);
      sendPdf(req, res, makeLineChart(patientId, studies, opts));
      break;
    case 3:
      // image map
      sendPdf(req, res, makeImageMap(patientId, studies, height, width, gap, opts));
      break;
    case 999:
      // Send back the chart for one study as an image
      // sendImage(req, res, height, width);
    }

  }

  private ArrayList<Study> getChosenStudies(Integer[] studyCodes) throws Exception {
    ArrayList<Study> studies = service.getStudies(true);
    ArrayList<Study> chosenStudies = new ArrayList<>();
    for (Study study : studies) {
      for (Integer studyCode : studyCodes) {
        if (study.getStudyCode().intValue() == studyCode.intValue()) {
          chosenStudies.add(study);
        }
      }
    }
    return chosenStudies;
  }

  private ArrayList<PatientStudyExtendedData> getPatientStudies(String patientId, Integer[] studyCodes)
      throws Exception {
    ArrayList<PatientStudyExtendedData> patientStudies = service.searchForPatientStudyDataScores(patientId);
    ArrayList<PatientStudyExtendedData> pstudies = new ArrayList<>();

    for (PatientStudyExtendedData patientStudy : patientStudies) {
      for (Integer studyCode : studyCodes) {
        if (patientStudy.getStudyCode().intValue() == studyCode.intValue()) {
          pstudies.add(patientStudy);
        }
      }
    }
    return pstudies;
  }

  private PDDocument makeLineChart(String patientId, Integer[] studyCodes, ChartConfigurationOptions opts)
      throws Exception {
    // lines aren't green
    Integer height = opts.getHeight();
    Integer width = opts.getWidth();

    Integer scaleBy = opts.getIntegerOption(ConfigurationOptions.OPTION_SCALE_IMAGES);
    if (scaleBy > 1) {
      double scale = scaleBy / 100.0;
      height = Long.valueOf(Math.round(opts.getHeight() * scale)).intValue();
      width = Long.valueOf(Math.round(width * scale)).intValue();
      logger.error("scaleBy=" + scaleBy + " scale=" + scale + " height=" + height + " width=" + width);
    }

    ArrayList<Study> chosenStudies = getChosenStudies(studyCodes);
    if (chosenStudies == null || chosenStudies.size() < 1) {
      throw new InvalidDataElementException("No patient studies");
    }

    ArrayList<PatientStudyExtendedData> pstudies = getPatientStudies(patientId, studyCodes);

    if (pstudies.size() < 1) {
      logger.error("patient " + patientId + " doesn't have any completed studies for the selected studyCode(s)");
      throw new InvalidDataElementException("No patient studies");
    }

    ScoreProvider provider = service.getScoreProvider(pstudies, chosenStudies.get(0).getStudyCode());

    ChartInfo chartInfo = createChartInfoFromStudies(chosenStudies, pstudies, opts, false);

    XYPlot plot = provider.getPlot(chartInfo, chosenStudies, opts);
    ChartMaker chartMaker = new ChartMaker(chartInfo, opts);

    PDDocument pdf = new PDDocument();
    JFreeChart chart = chartMaker.getChart("", true, plot);
    String message = null;
    PDImageXObject jpeg = null;
    if (chart == null) {
      message = "chart returned from chartmaker is null";
    } else {
      jpeg = JPEGFactory.createFromByteArray(pdf, PDFUtils.getImage(chart, width * 3, height * 3));
    }
    PDPage page = new PDPage(PDRectangle.LETTER);
    PDFont font = PDType1Font.TIMES_ROMAN;
    int fontSize = 12;
    PDPageContentStream contentStream = null;
    contentStream = PDFUtils.startNewPage(page, contentStream, pdf, font, fontSize);

    // Position the cursor down to write the next image
    int startX = XMARGIN + TABLE_WIDTH + 40;
    float y = page.getMediaBox().getHeight() - YMARGIN;

    contentStream.moveTo(XMARGIN, y);
    // drawExplanationText(contentStream, y, width, patientStudies, study,
    // chartUtils);
    // contentStream.moveTo(startX, y);
    y -= height;

    if (message != null) {
      PDFUtils.writePDFHtml(contentStream, message, startX, y, PDFUtils.pageWidth(page) - XMARGIN, timesArray, 16);
    }

    if (jpeg != null) {
      contentStream.drawImage(jpeg, startX, y, width, height);
    }
    // close the content stream
    if (contentStream != null) {
      contentStream.close();
    }
    return pdf;
  }

  private PDDocument makeLineChart(String title, String patientId, Integer[] studyCodes, Integer height, Integer width,
                                   Integer gap, ChartConfigurationOptions opts) throws Exception {
    PDDocument pdf = new PDDocument();
    ArrayList<Study> chosenStudies = getChosenStudies(studyCodes);
    ArrayList<PatientStudyExtendedData> pstudies = getPatientStudies(patientId, studyCodes);

    if (pstudies.size() < 1) {
      logger.error("patient " + patientId + " doesn't have any completed studies for the selected studyCode(s)");
      throw new InvalidDataElementException("No patient studies");
    }
    boolean isPromis = false;
    if (chosenStudies.get(0).getStudyDescription().contains("PROMIS")) {
      logger.error("CHOSEN STUDY " + chosenStudies.get(0).getStudyCode() + " DESCRIPTION "
          + chosenStudies.get(0).getStudyDescription() + " indexOf(PROMIS) "
          + chosenStudies.get(0).getStudyDescription().indexOf("PROMIS"));
      isPromis = true;
    }

    // Get the chartInfo from the first non-null study, and collect all the dataSets
    ChartInfo chartInfo = createChartInfoFromStudies(chosenStudies, pstudies, opts, true);

    XYDataset ds = chartInfo.getDataSet();
    int numberSeries = ds.getSeriesCount();
    logger.debug("for a total of " + numberSeries + " series");
    ChartMaker chartMaker = new ChartMaker(chartInfo, opts);
    Font titleFont = chartMaker.getTitleFont();
    //
    XYErrorRenderer renderer = new XYErrorRenderer();
    renderer.setDrawXError(false);
    renderer.setDrawYError(false);
    if (numberSeries == 1) {
      renderer.setSeriesShapesVisible(0, true);
      renderer.setSeriesPaint(0, Color.BLACK);
      renderer.setSeriesItemLabelsVisible(0, true);
      renderer.setBasePaint(Color.BLACK);
      renderer.setSeriesShape(0, dot);
      renderer.setSeriesShapesFilled(0, true);
      renderer.setSeriesShapesVisible(0, true);
      renderer.setLegendTextFont(0, titleFont);
      renderer.setLegendTextPaint(0, ChartConfigurationOptions.BLACK_80);
    } else if (numberSeries > 1) {
      renderer.setSeriesShapesVisible(0, false);
      renderer.setSeriesPaint(0, Color.WHITE);
      renderer.setSeriesItemLabelsVisible(0, false);
      renderer.setBasePaint(Color.BLACK);
      int line = 1;
      int colorInx = 0;

      while (line < numberSeries) {
        if (colorInx >= ChartConfigurationOptions.LINE_COLORS.length) {
          colorInx = 0;
        }
        renderer.setSeriesLinesVisible(line, true);
        renderer.setSeriesShapesVisible(line, true);
        if (opts.getBooleanOption(ConfigurationOptions.OPTION_GRAY)) {
          renderer.setSeriesPaint(line, Color.black);
        } else {
          renderer.setSeriesPaint(line, ChartConfigurationOptions.LINE_COLORS[colorInx]);
        }
        renderer.setSeriesStroke(line, ChartConfigurationOptions.LINE_STYLES[colorInx]);
        renderer.setSeriesItemLabelsVisible(line, true);
        renderer.setBasePaint(Color.BLACK);
        renderer.setLegendTextFont(line, titleFont);
        renderer.setLegendTextPaint(line, ChartConfigurationOptions.BLACK_80);

        line++;
        colorInx++;
      }

    }

    //
    boolean addLegend = (title == null || title.isEmpty());
    JFreeChart chart = chartMaker.getTestChart(title, addLegend, isPromis, renderer);
    String message = null;
    PDImageXObject jpeg = null;
    if (chart == null) {
      message = "chart returned from chartmaker is null";
    } else {
      jpeg = JPEGFactory.createFromByteArray(pdf, PDFUtils.getImage(chart, width * 3, height * 3));
    }
    PDPage page = new PDPage(PDRectangle.LETTER);
    PDFont font = PDType1Font.TIMES_ROMAN;
    int fontSize = 12;
    PDPageContentStream contentStream = null;
    contentStream = PDFUtils.startNewPage(page, contentStream, pdf, font, fontSize);

    // Position the cursor down to write the next image
    int startX = XMARGIN + TABLE_WIDTH + 40;
    float y = page.getMediaBox().getHeight() - YMARGIN;

    contentStream.moveTo(XMARGIN, y);
    // drawExplanationText(contentStream, y, width, patientStudies, study,
    // chartUtils);
    // contentStream.moveTo(startX, y);
    y -= height;

    if (message != null) {
      PDFUtils.writePDFHtml(contentStream, message, startX, y, PDFUtils.pageWidth(page) - XMARGIN, timesArray, 16);
    }

    if (jpeg != null) {
      contentStream.drawImage(jpeg, startX, y, width, height);
    }
    // close the content stream
    if (contentStream != null) {
      contentStream.close();
    }
    return pdf;
  }


  /**
   * Creates a ChartInfo for the first study in a list that produces a non-null chart.
   * Adds to its data collection all the other data sets (series) in the other studies.
   */
  private ChartInfo createChartInfoFromStudies(ArrayList<Study> chosenStudies,
      ArrayList<PatientStudyExtendedData> pstudies, ChartConfigurationOptions opts, boolean log)
          throws InvalidDataElementException {
    ChartInfo chartInfo = null;
    TimeSeriesCollection collection = null;
    logger.debug("Collecting dataSets from up to " + chosenStudies.size() + " chosen studies");
    for (Study chosenStudy : chosenStudies) {
      PrintStudy printStudy = new PrintStudy(siteInfo, chosenStudy, ""); // can throw exc if study.data is bad
      ChartInfo thisChartInfo = service.createChartInfo(pstudies, printStudy, false, opts);
      Integer studyCode = chosenStudy.getStudyCode();

      if (thisChartInfo == null) {
        debugLog(log, "thischartinfo was null for study " + studyCode);
        continue;
      }

      TimeSeriesCollection thisCollection = (TimeSeriesCollection) thisChartInfo.getDataSet();
      int seriesCount = thisCollection.getSeriesCount();
      if (chartInfo == null) {
        debugLog(log, "using chartinfo with %d dataSets from study %d", seriesCount, studyCode);
        chartInfo = thisChartInfo;
        collection = thisCollection;
      } else {
        debugLog(log, "chartinfo: adding %d dataSets from study %d", seriesCount, studyCode);
        for (int d = 1; d < seriesCount; d++) {
          collection.addSeries(thisCollection.getSeries(d));
        }
      }
    }
    return chartInfo;
  }


  private void debugLog(boolean log, String format, Object...args) {
    if (log) {
      logger.debug(String.format(format, args));
    }
  }


  private PDDocument makeImageMap(String patientId, Integer[] studyCodes, Integer height, Integer width, Integer gap,
                                  ChartConfigurationOptions opts) throws Exception {
    PDDocument pdf = new PDDocument();

    ArrayList<Study> studies = getChosenStudies(studyCodes);
    ArrayList<PatientStudyExtendedData> pstudies = getPatientStudies(patientId, studyCodes);

    if (pstudies == null || pstudies.size() < 1 || studies == null || studies.size() < 1) {
      return pdf;
    }
    Patient patient = service.getPatient(patientId);
    if (patient == null) {
      return pdf;
    }

    Color fillColor = BODYMAP_FILL_COLOR;
    Color strokeColor = BODYMAP_STROKE_COLOR;
    if (opts.getIntegerOption(ConfigurationOptions.OPTION_BACKGROUND_COLOR) != 0) {
      fillColor = ChartConfigurationOptions.getColor(opts
          .getIntegerOption(ConfigurationOptions.OPTION_BACKGROUND_COLOR));
    }

    for (Study study : studies) {
      String surveySystemName = null;
      for (PatientStudyExtendedData pstudy : pstudies) {
        if (pstudy != null && pstudy.getStudyCode() != null && study != null
            && study.getStudyCode() != null
            && pstudy.getStudyCode().intValue() == study.getStudyCode().intValue()) {
          surveySystemName = pstudy.getSurveySystemName();
        }
      }
      if (surveySystemName == null) {
        return pdf;
      }

      ScoreProvider provider = service.getScoreProvider(surveySystemName, study.getTitle());

      ArrayList<SurveyQuestionIntf> questions = provider.getSurvey(pstudies, new PrintStudy(siteInfo, study, surveySystemName),
          patient, false);
      HashMap<String, PDImageXObject> mapImages = new HashMap<>();
      for (SurveyQuestionIntf question : questions) {
        if (question.getAnswered()) {
          HashMap<String, BufferedImage> bufImages = PDFUtils.getPaintedImages(question, pdf, strokeColor,
              fillColor);

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
              mapImages.put(imageName, img);
            }
          }
        }

        if (question.getAnswered() && study != null && study.getTitle() != null) {
          PDPage page = new PDPage(PDRectangle.LETTER);
          PDFont font = PDType1Font.TIMES_ROMAN;
          int fontSize = 12;
          PDPageContentStream contentStream = null;
          contentStream = PDFUtils.startNewPage(page, contentStream, pdf, font, fontSize);

          // Position the cursor down to write the next image
          int startX = XMARGIN;
          float y = page.getMediaBox().getHeight() - YMARGIN;

          contentStream.moveTo(XMARGIN, y);
          // drawExplanationText(contentStream, y, width, patientStudies, study,
          // chartUtils);
          // contentStream.moveTo(startX, y);

          Integer scaleBy = opts.getIntegerOption(ConfigurationOptions.OPTION_SCALE_IMAGES);
          double scale = 1.0;
          if (scaleBy > 1) {
            scale = scaleBy / 100.0;
            logger.error("scaleBy=" + scaleBy + " scale=" + scale + " height=" + height + " width=" + width);
          }
          ArrayList<SurveyAnswerIntf> answers = question.getAnswers();
          for (int aInx = answers.size(); aInx > 0; aInx--) {
            SurveyAnswerIntf ans = answers.get(aInx - 1);
            String imageName = ans.getAttribute("usemap");
            PDImageXObject img = mapImages.get(imageName);
            if (img != null) {
              // contentStream.drawImage(img, startX, y - img.getHeight());
              // startX += img.getWidth();
              height = Long.valueOf(Math.round(img.getHeight() * scale)).intValue();
              width = Long.valueOf(Math.round(img.getWidth() * scale)).intValue();
              contentStream.drawImage(img, startX, y - height, width, height);
              startX += width;
            }
          }
          if (contentStream != null) {
            contentStream.close();
          }
        }
      }
    }

    return pdf;
  }

  /**
   * Send a pdf to the client with or without printing directly
   */
  private void sendPdf(HttpServletRequest req, HttpServletResponse res, PDDocument pdf) throws Exception {
    // }
    // Save the pdf and send it to the client
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // remove the no-cache and cache control headers to send the pdf.
    try {
      res.reset();
    } catch (Exception ex) {
      logger.warn("Unable to reset the ServletResponse: " + ex.getMessage());
    }
    try {
      if (!ServerUtils.isEmpty(req.getParameter("print")) && req.getParameter("print").toLowerCase().equals("y")) {
        PDActionJavaScript javascript = new PDActionJavaScript(" this.print(); this.closeDoc(); ");
        pdf.getDocumentCatalog().setOpenAction(javascript);
        res.setContentType("application/octet-stream"); // open it with adobe
        // not browser
        res.addHeader("Content-Disposition", "inline; filename=chart.pdf");
        // res.addHeader("Content-Disposition", "inline; filename=chart.pdf");
      } else {
        res.setContentType("application/pdf");
      }
      pdf.save(baos);
    } catch (IOException e) {
      logger.error("Unable to create pdf", e);
      throw new ServletException("Unable to create pdf");
    }
    pdf.close();
    res.getOutputStream().write(baos.toByteArray());
    res.getOutputStream().close();
  }

  private Integer getInteger(HttpServletRequest req, String paramName, Integer dflt) {
    Integer val = null;

    try {
      val = Integer.valueOf(req.getParameter(paramName));
    } catch (Exception ex) {
      // Ignored
    }

    return val != null ? val : dflt;
  }

  private Patient checkPatientId(Long siteId, String patientId) {
    PatientIdFormatIntf formatter;
    Patient pat = null;
    boolean isValid = false;
    try {
      formatter = siteInfo.getPatientIdFormatter();
      patientId = formatter.format(patientId);
      isValid = formatter.isValid(patientId);
    } catch (NumberFormatException e) {
      logger.error("Error in formatting(" + patientId + ")", e);
      throw new InvalidPatientIdException("Invalid number", false);
    }
    try {
      if (isValid) {
        pat = service.getPatient(patientId);
      }
    } catch (Exception e) {
      logger.error("Error in getPatient(" + patientId + ")", e);
      throw new ServiceUnavailableException(e.getMessage());
    }
    if (pat != null) {
      return pat;
    }
    logger.debug("getPatient: patient not found, formatter.isValid = " + isValid);
    if (isValid) {
      throw new InvalidPatientIdException(patientId + " Not found", patientId);
    }
    throw new InvalidPatientIdException(formatter.getInvalidMessage(), isValid);
  }

}
