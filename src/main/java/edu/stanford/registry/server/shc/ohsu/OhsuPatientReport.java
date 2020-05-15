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

package edu.stanford.registry.server.shc.ohsu;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.reports.PatientInfo;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.RegistryShortFormScoreProvider;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.PDFArea;
import edu.stanford.registry.server.utils.PDFUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.GlobalHealthScore;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.User;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.jfree.chart.JFreeChart;

import com.github.susom.database.Database;

public class OhsuPatientReport extends PatientReport {

  protected enum Measure {
    MOBILITY, PAIN_INTERFERENCE, PEER_RELATION, FATIGUE, ANXIETY, DEPRESSION, GLOBAL_HEALTH_PHYSICAL, GLOBAL_HEALTH_MENTAL, SLEEP_DISTURBANCE,
    PCS, PEDSQL_SCHOOL, FOPQ_TOTAL, FOPQ_FEAR, FOPQ_AVOIDANCE, ARCS_PROTECT, ARCS_MINIMIZE, ARCS_DISTRACT, SELF_EFFICACY
  }

  protected static final Measure[] PROMIS_MEASURES = new Measure[] {
      Measure.MOBILITY, Measure.PAIN_INTERFERENCE, Measure.PEER_RELATION, Measure.FATIGUE, Measure.ANXIETY, Measure.DEPRESSION,
      Measure.GLOBAL_HEALTH_PHYSICAL, Measure.GLOBAL_HEALTH_MENTAL, Measure.SLEEP_DISTURBANCE
    };

  protected static final Measure[] OTHER_MEASURES = new Measure[] {
      Measure.PCS, Measure.PEDSQL_SCHOOL, Measure.FOPQ_TOTAL, Measure.FOPQ_FEAR, Measure.FOPQ_AVOIDANCE,
      Measure.ARCS_PROTECT, Measure.ARCS_MINIMIZE, Measure.ARCS_DISTRACT, Measure.SELF_EFFICACY
    };
  protected static final String[] FOLLOW_UP_TYPES = new String[] {OhsuCustomizer.SURVEY_FOLLOWUP, OhsuCustomizer.SURVEY_FOLLOWUP_18};

  protected String surveyTitle;

  public OhsuPatientReport(Supplier<Database> dbp, SiteInfo siteInfo, User user) {
    super(dbp, siteInfo, user);
  }

  @Override
  public PDDocument makePdf(ArrayList<PatientStudyExtendedData> patientStudies, AssessmentRegistration assessment,
      ChartConfigurationOptions opts, PDDocument pdf) throws IOException, InvalidDataElementException {
    // Set the survey title to the follow up period for a follow up survey
    surveyTitle = null;
    if (OhsuCustomizer.sameSurveyTypes(assessment.getAssessmentType(), FOLLOW_UP_TYPES)) {
      Date asmtDate = assessment.getAssessmentDt();
      if (asmtDate != null) {
        Date fromDate = DateUtils.getDaysFromDate(siteInfo, asmtDate, -30);
        Date toDate = DateUtils.getDaysFromDate(siteInfo, asmtDate, 30);
        String sql =
            "select visit_type from appt_registration "
          + "where visit_dt between :fromDate and :toDate and patient_id = :patientId and survey_site_id = :siteId and "
          + "  registration_type = 's' and visit_type like 'survey %' "
          + "order by visit_dt asc";
        String visitType = dbp.get().toSelect(sql)
            .argDate(":fromDate", fromDate)
            .argDate(":toDate", toDate)
            .argString(":patientId", assessment.getPatientId())
            .argLong(":siteId", assessment.getSurveySiteId())
            .queryStringOrNull();
        if (visitType != null) {
          if (visitType.equals("survey 3mn")) {
            surveyTitle = "3 Month Follow Up";
          } else if (visitType.equals("survey 6mn")) {
            surveyTitle = "6 Month Follow Up";
          } else if (visitType.equals("survey 9mn")) {
            surveyTitle = "9 Month Follow Up";
          } else if (visitType.equals("survey 1yr")) {
            surveyTitle = "1 Year Follow Up";
          } else if (visitType.equals("survey 2yr")) {
            surveyTitle = "2 Year Follow Up";
          }
        }
      }
    }
    return super.makePdf(patientStudies, assessment, opts, pdf);
  }

  @Override
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
    for (PatientStudyExtendedData patientStudy : patientStudies) {
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
        if (jpegs.get(0)!= null && jpegs.get(0).getHeight() > mapSpace) {
          mapSpace = jpegs.get(0).getHeight() *.4f;
        }
      }
      // Check if we need to start a new page for the bodymap & charts
      if (!getImageFirst()) {
        if (needNewPage(y + tblTextHeight, mapSpace)) {
          writeFooter(page);
          page = new PDPage(PDRectangle.LETTER);
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
                backHeight, OPTION_SECTION_BACKGROUND_COLOR);
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

      //
      // This section overrides the code in PatientReport to generate two custom initial report
      // score tables.
      //
      // Get subset of the patient studies which are part of the current assessment
      ArrayList<PatientStudyExtendedData> currentPatientStudies = getCurrentPatientStudies(assessment, patientStudies);

      float scoresTableW = ((PDFUtils.pageWidth(page) - (XMARGIN * 2)) / 2) + XMARGIN;
      PDFArea promisScoresArea = drawPromisScoresTable(printStudies, currentPatientStudies, opts, startX - 15, y + 7, scoresTableW);

      y = promisScoresArea.getNextY() - textHeight;
      PDFArea otherScoresArea = drawOtherScoresTable(printStudies, currentPatientStudies, opts, startX - 15, y + 7, scoresTableW);

      PDFUtils.writeHighlightBox(contentStream, otherScoresArea.getXto(), otherScoresArea.getYto(), (PDFUtils.pageWidth(page) - XMARGIN) - otherScoresArea.getXto(),
          otherScoresArea.getYfrom() - otherScoresArea.getYto(), OPTION_SECTION_BACKGROUND_COLOR);

      if (otherScoresArea != null) {
        chartArea = otherScoresArea;
        y = chartArea.getNextY();
      }

      // End of overridden code

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

  @Override
  public float drawTitleLine3(PDPage page, PatientInfo patientInfo, float y3, float boxMargin) throws IOException {
    PDFont font = fontArray[0];
    String mrnText = "MRN " + siteInfo.getPatientIdFormatter().printFormat(patientInfo.getMrn());
    String genderText = "Assigned sex " + patientInfo.getGender(); // customized for pediatrics

    PDFUtils.writePDFHtml(contentStream, mrnText, XMARGIN + 4f, y3, PDFUtils.pageWidth(page) - boxMargin, fontArray,
        tFontSize, Color.BLACK, Color.white);

    // Display the survey title centered on the line
    if (surveyTitle != null) {
      float x1 = (page.getMediaBox().getWidth() - ((font.getStringWidth(surveyTitle) / 1000) * tFontSize))/2;
      PDFUtils.writePDFHtml(contentStream, surveyTitle, x1, y3, PDFUtils.pageWidth(page) - boxMargin, fontArray,
          tFontSize, Color.BLACK, Color.white);
    }

    float x = page.getMediaBox().getWidth() - boxMargin - ((font.getStringWidth(genderText) / 1000) * tFontSize) - 2;
    y3 = nextY(PDFUtils.writePDFHtml(contentStream, genderText, x, y3, PDFUtils.pageWidth(page) - boxMargin, fontArray,
        tFontSize, Color.BLACK, Color.white));

    return y3;
  }

  /**
   * Draw the Initial Report PROMIS scores table.
   */
  public PDFArea drawPromisScoresTable(ArrayList<PrintStudy> printStudies, ArrayList<PatientStudyExtendedData> patientStudies,
      ChartConfigurationOptions opts, float x, float y, float tableWidth) throws IOException {
    float spWidth = 3f;
    float scorWidth = Math.round(PDFUtils.getTextWidth(fontArray[0], tblFontSize, "Score"));
    float percWidth = Math.round(PDFUtils.getTextWidth(fontArray[0], tblFontSize, "%tile"));
    float pedWidth = Math.round(PDFUtils.getTextWidth(fontArray[0],  tblFontSize, "Pediatric"));
    float proxyWidth = Math.round(PDFUtils.getTextWidth(fontArray[0],  tblFontSize,"Parent"));
    float catgWidth = Math.round(PDFUtils.getTextWidth(fontArray[0], tblFontSize, "Category") + 8f);
    float titlWidth = tableWidth - (spWidth + scorWidth + spWidth + percWidth + catgWidth)*2 - GAP;
    x += GAP / 2;

    PDFArea returnArea = new PDFArea();
    returnArea.setXfrom(x);
    returnArea.setXto(x + tableWidth);
    returnArea.setYto(y);

    Color tableColor = ChartConfigurationOptions.getColor(opts
        .getColorOption(ConfigurationOptions.OPTION_TABLE_BACKGROUND_COLOR));
    float posX = x;
    returnArea.setYfrom(y);
    y -= tblTextHeight;

    // write the table header line 1
    PDFUtils.writeHighlightBox(contentStream, x, y + tblTextHeight / 2, tableWidth, tblTextHeight / 2,
        HEADING_BACKGROUND_COLOR);
    PDFUtils.writePDFHtml(contentStream, "-", x, y, x + tableWidth, fontArray, tblFontSize, HEADING_BACKGROUND_COLOR,
        HEADING_BACKGROUND_COLOR);

    posX = x + spWidth;
    PDFUtils.writePDFHtml(contentStream, "PROMIS Outcome Measures", posX, y, posX + titlWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth;
    PDFUtils.writePDFHtml(contentStream, "Pediatric", posX, y, posX + pedWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth;
    PDFUtils.writePDFHtml(contentStream, "Parent", posX, y, posX + proxyWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + catgWidth + spWidth + spWidth + scorWidth + spWidth+ percWidth + catgWidth + spWidth;
    y = nextY(PDFUtils.writePDFHtml(contentStream, "-", posX, y, posX + spWidth, fontArray, tblFontSize,
        HEADING_BACKGROUND_COLOR, HEADING_BACKGROUND_COLOR));
    returnArea.setNextY(y);

    // write the table header line 2
    PDFUtils.writeHighlightBox(contentStream, x, y + tblTextHeight / 2, tableWidth, tblTextHeight / 2,
        HEADING_BACKGROUND_COLOR);
    PDFUtils.writePDFHtml(contentStream, "-", x, y, x + tableWidth, fontArray, tblFontSize, HEADING_BACKGROUND_COLOR,
        HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth;
    PDFUtils.writePDFHtml(contentStream, "Score", posX, y, posX + scorWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth;
    PDFUtils.writePDFHtml(contentStream, "%ile", posX, y, posX + percWidth, fontArray, tblFontSize, HEADING_TEXT_COLOR,
        HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth;
    PDFUtils.writePDFHtml(contentStream, "Category", posX, y, posX + catgWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth;
    PDFUtils.writePDFHtml(contentStream, "Score", posX, y, posX + scorWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth + scorWidth + spWidth;
    PDFUtils.writePDFHtml(contentStream, "%ile", posX, y, posX + percWidth, fontArray, tblFontSize, HEADING_TEXT_COLOR,
        HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth + scorWidth + spWidth+ percWidth;
    PDFUtils.writePDFHtml(contentStream, "Category", posX, y, posX + catgWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth + scorWidth + spWidth+ percWidth + spWidth + catgWidth;
    y = nextY(PDFUtils.writePDFHtml(contentStream, "-", posX, y, posX + spWidth, fontArray, tblFontSize,
        HEADING_BACKGROUND_COLOR, HEADING_BACKGROUND_COLOR));
    returnArea.setNextY(y);

    // Write the measure values
    boolean printInvertedMessage = false;
    for(Measure measure : PROMIS_MEASURES) {
      ScoreData scoreData = getScoreData(measure, printStudies, patientStudies);
      if (scoreData == null) {
        continue;
      }
      if (scoreData.inverted) {
        printInvertedMessage = true;
        scoreData.title = scoreData.title + " *";
        if (scoreData.pedScore != null) {
          scoreData.pedScore = invertScore(scoreData.pedScore);
        }
        if (scoreData.proxyScore != null) {
          scoreData.proxyScore = invertScore(scoreData.proxyScore);
        }
      }
      Long pedPercent = null;
      if (scoreData.pedScore != null) {
        pedPercent = calculatePercentile(Math.round(scoreData.pedScore), scoreData.printStudy);
      }
      Long proxyPercent = null;
      if (scoreData.proxyScore != null) {
        proxyPercent = calculatePercentile(Math.round(scoreData.proxyScore), scoreData.printStudy);
      }

      PDFArea area;

      PDFUtils.writePDFHtml(contentStream, "-", x, y, x + tableWidth, fontArray, tblFontSize,
          tableColor, tableColor);
      y -= tblTextHeight/2;
      area = PDFUtils.writePDFHtml(contentStream, "-", x, y, x + tableWidth, fontArray, tblFontSize,
          tableColor, tableColor);
      returnArea.setNextY(nextY(area));
      returnArea.setYto(area.getYto());

      posX = x + spWidth;
      area = PDFUtils.writePDFHtml(contentStream, scoreData.title, posX, y, posX + titlWidth, fontArray, tblFontSize,
          Color.black, tableColor);

      posX = x + spWidth + titlWidth;
      if (scoreData.pedScore != null) {
        String strValue = formatScore(scoreData.pedScore, scoreData.printStudy);
        float writeAt = posX + (scorWidth - PDFUtils.getTextWidth(fontArray[0], tblFontSize, strValue));
        PDFUtils.writePDFHtml(contentStream, strValue, writeAt, y, posX + scorWidth, fontArray, tblFontSize,
            Color.black, tableColor);
      }

      posX = x + spWidth + titlWidth + scorWidth + spWidth;
      if (pedPercent != null) {
        float writeAt = posX + (percWidth - PDFUtils.getTextWidth(fontArray[0], tblFontSize, pedPercent.toString()));
        PDFUtils.writePDFHtml(contentStream, pedPercent.toString(), writeAt, y, posX + percWidth, fontArray,
            tblFontSize, Color.black, tableColor);
      }

      posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth;
      if (scoreData.pedCategoryLabel.length() > 0) {
        float writeAt = posX + (catgWidth - PDFUtils.getTextWidth(fontArray[0], tblFontSize, scoreData.pedCategoryLabel));
        PDFUtils.writePDFHtml(contentStream, scoreData.pedCategoryLabel, writeAt, y, posX + catgWidth, fontArray,
            tblFontSize, Color.black, tableColor);
      }

      posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth;
      if (scoreData.proxyScore != null) {
        String strValue = formatScore(scoreData.proxyScore, scoreData.printStudy);
        float writeAt = posX + (scorWidth - PDFUtils.getTextWidth(fontArray[0], tblFontSize, strValue));
        PDFUtils.writePDFHtml(contentStream, strValue, writeAt, y, posX + scorWidth, fontArray, tblFontSize,
            Color.black, tableColor);
      }

      posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth + scorWidth + spWidth;
      if (proxyPercent != null) {
        float writeAt = posX + (percWidth - PDFUtils.getTextWidth(fontArray[0], tblFontSize, proxyPercent.toString()));
        PDFUtils.writePDFHtml(contentStream, proxyPercent.toString(), writeAt, y, posX + percWidth, fontArray,
            tblFontSize, Color.black, tableColor);
      }

      posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth + scorWidth + spWidth + percWidth + spWidth;
      if (scoreData.proxyCategoryLabel.length() > 0) {
        float writeAt = posX + (catgWidth - PDFUtils.getTextWidth(fontArray[0], tblFontSize, scoreData.proxyCategoryLabel));
        PDFUtils.writePDFHtml(contentStream, scoreData.proxyCategoryLabel, writeAt, y, posX + catgWidth, fontArray,
            tblFontSize, Color.black, tableColor);
      }

      y = returnArea.getNextY();
    }

    if (printInvertedMessage) {
      PDFArea area;
      PDFUtils.writePDFHtml(contentStream, "-", x, y, x + tableWidth, fontArray, tblFontSize,
          tableColor, tableColor);
      y -= tblTextHeight/2;
      area = PDFUtils.writePDFHtml(contentStream, " <i>* Scores and percentiles have been inverted</i> ", x, y,
          x + tableWidth, fontArray, tblFontSize * .75f, Color.black, tableColor);
      returnArea.setNextY(nextY(area));
      returnArea.setYto(area.getYto());
    }

    return returnArea;
  }

  /**
   * Draw the Initial Report other scores table.
   */
  public PDFArea drawOtherScoresTable(ArrayList<PrintStudy> printStudies, ArrayList<PatientStudyExtendedData> patientStudies,
      ChartConfigurationOptions opts, float x, float y, float tableWidth) throws IOException {
    float spWidth = 4f;
    float scorWidth = Math.round(PDFUtils.getTextWidth(fontArray[0], tblFontSize, "Score"));
    float percWidth = Math.round(PDFUtils.getTextWidth(fontArray[0], tblFontSize, "%tile"));
    float pedWidth = Math.round(PDFUtils.getTextWidth(fontArray[0],  tblFontSize, "Pediatric"));
    float proxyWidth = Math.round(PDFUtils.getTextWidth(fontArray[0],  tblFontSize,"Parent"));
    float catgWidth = Math.round(PDFUtils.getTextWidth(fontArray[0], tblFontSize, "Category") + 8f);
    float titlWidth = tableWidth - (spWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth)*2 - GAP;
    x += GAP / 2;

    PDFArea returnArea = new PDFArea();
    returnArea.setXfrom(x);
    returnArea.setXto(x + tableWidth);
    returnArea.setYto(y);

    Color tableColor = ChartConfigurationOptions.getColor(opts
        .getColorOption(ConfigurationOptions.OPTION_TABLE_BACKGROUND_COLOR));
    float posX = x;
    returnArea.setYfrom(y);
    y -= tblTextHeight;

    // write the table header line 1
    PDFUtils.writeHighlightBox(contentStream, x, y + tblTextHeight / 2, tableWidth, tblTextHeight / 2,
        HEADING_BACKGROUND_COLOR);
    PDFUtils.writePDFHtml(contentStream, "-", x, y, x + tableWidth, fontArray, tblFontSize, HEADING_BACKGROUND_COLOR,
        HEADING_BACKGROUND_COLOR);

    posX = x + spWidth;
    PDFUtils.writePDFHtml(contentStream, "Other Outcome Measures", posX, y, posX + titlWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth;
    PDFUtils.writePDFHtml(contentStream, "Pediatric", posX, y, posX + pedWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth;
    PDFUtils.writePDFHtml(contentStream, "Parent", posX, y, posX + proxyWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + scorWidth + spWidth+ percWidth + spWidth + catgWidth;
    y = nextY(PDFUtils.writePDFHtml(contentStream, "-", posX, y, posX + spWidth, fontArray, tblFontSize,
        HEADING_BACKGROUND_COLOR, HEADING_BACKGROUND_COLOR));
    returnArea.setNextY(y);

    // write the table header line 2
    PDFUtils.writeHighlightBox(contentStream, x, y + tblTextHeight / 2, tableWidth, tblTextHeight / 2,
        HEADING_BACKGROUND_COLOR);
    PDFUtils.writePDFHtml(contentStream, "-", x, y, x + tableWidth, fontArray, tblFontSize, HEADING_BACKGROUND_COLOR,
        HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth;
    PDFUtils.writePDFHtml(contentStream, "Score", posX, y, posX + scorWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + spWidth + scorWidth;
    PDFUtils.writePDFHtml(contentStream, "Category", posX, y, posX + catgWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth;
    PDFUtils.writePDFHtml(contentStream, "Score", posX, y, posX + scorWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth + scorWidth + spWidth ;
    PDFUtils.writePDFHtml(contentStream, "Category", posX, y, posX + catgWidth, fontArray, tblFontSize,
        HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR);

    posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth + scorWidth + spWidth + catgWidth + spWidth + percWidth;
    y = nextY(PDFUtils.writePDFHtml(contentStream, "-", posX, y, posX + spWidth, fontArray, tblFontSize,
        HEADING_BACKGROUND_COLOR, HEADING_BACKGROUND_COLOR));
    returnArea.setNextY(y);

    // Write the measure values
    boolean printInvertedMessage = false;
    for(Measure measure : OTHER_MEASURES) {
      ScoreData scoreData = getScoreData(measure, printStudies, patientStudies);
      if (scoreData == null) {
        continue;
      }
      if (scoreData.inverted) {
        printInvertedMessage = true;
        scoreData.title = scoreData.title + " *";
        if (scoreData.pedScore != null) {
          scoreData.pedScore = invertScore(scoreData.pedScore);
        }
        if (scoreData.proxyScore != null) {
          scoreData.proxyScore = invertScore(scoreData.proxyScore);
        }
      }

      PDFArea area;
      PDFUtils.writePDFHtml(contentStream, "-", x, y, x + tableWidth, fontArray, tblFontSize,
          tableColor, tableColor);
      y -= tblTextHeight/2;
      area = PDFUtils.writePDFHtml(contentStream, "-", x, y, x + tableWidth, fontArray, tblFontSize,
          tableColor, tableColor);
      returnArea.setNextY(nextY(area));
      returnArea.setYto(area.getYto());

      posX = x + spWidth;
      area = PDFUtils.writePDFHtml(contentStream, scoreData.title, posX, y, posX + titlWidth, fontArray, tblFontSize,
          Color.black, tableColor);

      posX = x + spWidth + titlWidth;
      if (scoreData.pedScore != null) {
        String strValue = formatScore(scoreData.pedScore, scoreData.printStudy);
        float writeAt = posX + (scorWidth - PDFUtils.getTextWidth(fontArray[0], tblFontSize, strValue));
        PDFUtils.writePDFHtml(contentStream, strValue, writeAt, y, posX + scorWidth, fontArray, tblFontSize,
            Color.black, tableColor);
        if (scoreData.pedCategoryLabel.length() > 0) {
          writeAt = writeAt + spWidth + scorWidth;
          PDFUtils.writePDFHtml(contentStream, scoreData.pedCategoryLabel, writeAt, y, posX + catgWidth, fontArray, tblFontSize,
              Color.black, tableColor);
        }
      }

      posX = x + spWidth + titlWidth + scorWidth + spWidth + percWidth + spWidth + catgWidth + spWidth;
      if (scoreData.proxyScore != null) {
        String strValue = formatScore(scoreData.proxyScore, scoreData.printStudy);
        float writeAt = posX + (scorWidth - PDFUtils.getTextWidth(fontArray[0], tblFontSize, strValue));
        PDFUtils.writePDFHtml(contentStream, strValue, writeAt, y, posX + scorWidth, fontArray, tblFontSize,
            Color.black, tableColor);
        if (scoreData.proxyCategoryLabel.length() > 0) {
          writeAt = writeAt + spWidth + scorWidth;
          PDFUtils.writePDFHtml(contentStream, scoreData.proxyCategoryLabel, writeAt, y, posX + catgWidth, fontArray, tblFontSize,
              Color.black, tableColor);
        }
      }

      y = returnArea.getNextY();
    }

    if (printInvertedMessage) {
      PDFArea area;
      PDFUtils.writePDFHtml(contentStream, "-", x, y, x + tableWidth, fontArray, tblFontSize,
          tableColor, tableColor);
      y -= tblTextHeight/2;
      area = PDFUtils.writePDFHtml(contentStream, " <i>* Scores and percentiles have been inverted</i> ", x, y,
          x + tableWidth, fontArray, tblFontSize * .75f, Color.black, tableColor);
      returnArea.setNextY(nextY(area));
      returnArea.setYto(area.getYto());
    }

    return returnArea;
  }

  @Override
  public String formatScore(double score, PrintStudy printStudy) {
    String desc = printStudy.getStudyDescription();
    if ("ARCSPainResponse".equals(desc) ||
        "ARCSProtect2".equals(desc)) {
      DecimalFormat arcsScoreFormatter = new DecimalFormat("##.##");
      return arcsScoreFormatter.format(score);
    }

    return Long.toString(Math.round(score));
  }

  @Override
  public Long calculatePercentile(Long score, PrintStudy printStudy) {
    String desc = printStudy.getStudyDescription();
    if ("OhsuCatastrophizingScale".equals(desc) ||
        "proxyPainCatastrophizingScale".equals(desc) ||
        "proxyPainCatastrophizingScale2".equals(desc) ||
        "childPedsQL2".equals(desc) ||
        "childSleepDisturbance".equals(desc) ||
        "ARCSProtect2".equals(desc) ||
        "childSelfEfficacy".equals(desc) ||
        "proxySelfEfficacy".equals(desc) ||
        "proxySelfEfficacy2".equals(desc)) {
      return null;
    }
    return super.calculatePercentile(score, printStudy);
  }


  /**
   * Get the data for a row in the Initial Report scores table.
   */
  protected ScoreData getScoreData(Measure measure, ArrayList<PrintStudy> printStudies, ArrayList<PatientStudyExtendedData> patientStudies) {
    PrintStudy printStudy = null;
    String title = "";
    Double pedScore = null;
    Double proxyScore = null;
    boolean inverted = false;
    String pedCategoryLabel = "";
    String proxyCategoryLabel = "";

    switch(measure) {
    case MOBILITY:
      title = "Mobility";
      printStudy = getPrintStudy("PROMIS Ped Bank v1.0 - Mobility", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("PROMIS Ped Bank v1.0 - Mobility", patientStudies);
        if (pedChartScore != null) {
          pedScore = pedChartScore.getScore().doubleValue();
          pedCategoryLabel = pedChartScore.getCategoryLabel();
        }
        ChartScore proxyChartScore = getScore("PROMIS Parent Proxy Bank v1.0 - Mobility", patientStudies);
        if (proxyChartScore != null) {
          proxyScore = proxyChartScore.getScore().doubleValue();
          proxyCategoryLabel = proxyChartScore.getCategoryLabel();
        }
      }
      break;
    case PAIN_INTERFERENCE:
      title = "Pain Interference";
      printStudy = getPrintStudy("PROMIS Ped Bank v1.0 - Pain Interference", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("PROMIS Ped Bank v1.0 - Pain Interference", patientStudies);
        if (pedChartScore != null) {
          pedScore = pedChartScore.getScore().doubleValue();
          pedCategoryLabel = pedChartScore.getCategoryLabel();
        }
        ChartScore proxyChartScore = getScore("PROMIS Parent Proxy Bank v1.0 - Pain Interference", patientStudies);
        if (proxyChartScore != null) {
          proxyScore = proxyChartScore.getScore().doubleValue();
          proxyCategoryLabel = proxyChartScore.getCategoryLabel();
        }
      }
      break;
    case PEER_RELATION:
      title = "Peer Relations";
      printStudy = getPrintStudy("PROMIS Ped Bank v1.0 - Peer Rel", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("PROMIS Ped Bank v1.0 - Peer Rel", patientStudies);
        if (pedChartScore != null) {
          pedScore = pedChartScore.getScore().doubleValue();
          pedCategoryLabel = pedChartScore.getCategoryLabel();
        }
        ChartScore proxyChartScore = getScore("PROMIS Parent Proxy Bank v1.0 - Peer Relations", patientStudies);
        if (proxyChartScore != null) {
          proxyScore = proxyChartScore.getScore().doubleValue();
          proxyCategoryLabel = proxyChartScore.getCategoryLabel();
        }
      }
      break;
    case FATIGUE:
      title = "Fatigue";
      printStudy = getPrintStudy("PROMIS Ped Bank v1.0 - Fatigue", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("PROMIS Ped Bank v1.0 - Fatigue", patientStudies);
        if (pedChartScore != null) {
          pedScore = pedChartScore.getScore().doubleValue();
          pedCategoryLabel = pedChartScore.getCategoryLabel();
        }
        ChartScore proxyChartScore = getScore("PROMIS Parent Proxy Bank v1.0 - Fatigue", patientStudies);
        if (proxyChartScore != null) {
          proxyScore = proxyChartScore.getScore().doubleValue();
          proxyCategoryLabel = proxyChartScore.getCategoryLabel();
        }
      }
      break;
    case ANXIETY:
      title = "Anxiety";
      printStudy = getPrintStudy("PROMIS Ped Bank v1.1 - Anxiety", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("PROMIS Ped Bank v1.1 - Anxiety", patientStudies);
        if (pedChartScore != null) {
          pedScore = pedChartScore.getScore().doubleValue();
          pedCategoryLabel = pedChartScore.getCategoryLabel();
        }
        ChartScore proxyChartScore = getScore("PROMIS Parent Proxy Bank v1.1 - Anxiety", patientStudies);
        if (proxyChartScore != null) {
          proxyScore = proxyChartScore.getScore().doubleValue();
          proxyCategoryLabel = proxyChartScore.getCategoryLabel();
        }
      }
      break;
    case DEPRESSION:
      title = "Depression";
      printStudy = getPrintStudy("PROMIS Ped Bank v1.1 - Depressive Sx", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("PROMIS Ped Bank v1.1 - Depressive Sx", patientStudies);
        if (pedChartScore != null) {
          pedScore = pedChartScore.getScore().doubleValue();
          pedCategoryLabel = pedChartScore.getCategoryLabel();
        }
        ChartScore proxyChartScore = getScore("PROMIS Parent Proxy Bank v1.1 - Depressive Sx", patientStudies);
        if (proxyChartScore != null) {
          proxyScore = proxyChartScore.getScore().doubleValue();
          proxyCategoryLabel = proxyChartScore.getCategoryLabel();
        }
      }
      break;
    case GLOBAL_HEALTH_PHYSICAL:
      title = "Parent GH - Physical";
      printStudy = getPrintStudy("parentGlobalHealth", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore proxyChartScore = getScore("parentGlobalHealth", patientStudies);
        if (proxyChartScore != null) {
          proxyScore = ((GlobalHealthScore) proxyChartScore).getPhysicalHealthTScore();
          proxyCategoryLabel = ((GlobalHealthScore) proxyChartScore).getPhysicalCategoryLabel();
        }
      }
      break;
    case GLOBAL_HEALTH_MENTAL:
      title = "Parent GH - Mental";
      printStudy = getPrintStudy("parentGlobalHealth", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore proxyChartScore = getScore("parentGlobalHealth", patientStudies);
        if (proxyChartScore != null) {
          proxyScore =((GlobalHealthScore) proxyChartScore).getMentalHealthTScore();
          proxyCategoryLabel = ((GlobalHealthScore) proxyChartScore).getMentalCategoryLabel();
        }
      }
      break;
    case SLEEP_DISTURBANCE:
      title = "Pediatric Sleep Disturbance (0-40)";
      printStudy = getPrintStudy("childSleepDisturbance", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("childSleepDisturbance", patientStudies);
        if (pedChartScore != null) {
          pedScore = pedChartScore.getScore().doubleValue();
          pedCategoryLabel = pedChartScore.getCategoryLabel();
        }
      }
      break;
    case PCS:
      title = "Pain Catastrophizing";
      printStudy = getPrintStudy("OhsuCatastrophizingScale", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("OhsuCatastrophizingScale", patientStudies);
        if (pedChartScore != null) {
          pedScore = pedChartScore.getScore().doubleValue();
          pedCategoryLabel = pedChartScore.getCategoryLabel();
        }
        ChartScore proxyChartScore = getScore("proxyPainCatastrophizingScale", patientStudies);
        if (proxyChartScore == null) {
          proxyChartScore = getScore("proxyPainCatastrophizingScale2", patientStudies);
        }
        if (proxyChartScore != null) {
          proxyScore = proxyChartScore.getScore().doubleValue();
          proxyCategoryLabel = proxyChartScore.getCategoryLabel();
        }
      }
      break;
    case PEDSQL_SCHOOL:
      title = "Pedatric PedsQL School Functioning";
      printStudy = getPrintStudy("childPedsQL2", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("childPedsQL2", patientStudies);
        if (pedChartScore != null) {
          BigDecimal score = pedChartScore.getScores().get("School");
          pedScore = (score != null) ? score.doubleValue() : null;
          pedCategoryLabel = (score != null) ? pedChartScore.getCategoryLabel() : "";
        }
      }
      break;
    case FOPQ_TOTAL:
      title = "Pediatic Fear of Pain";
      printStudy = getPrintStudy("childFOPQ", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("childFOPQ", patientStudies);
        if (pedChartScore != null) {
          BigDecimal score = pedChartScore.getScores().get("Total");
          pedScore = (score != null) ? score.doubleValue() : null;
          pedCategoryLabel = (score != null) ? pedChartScore.getCategoryLabel() : "";
        }
      }
      break;
    case FOPQ_FEAR:
      title = "&nbsp;&nbsp;&nbsp;&nbsp;Fear subscale";
      printStudy = getPrintStudy("childFOPQ", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("childFOPQ", patientStudies);
        if (pedChartScore != null) {
          BigDecimal score = pedChartScore.getScores().get("Fear");
          pedScore = (score != null) ? score.doubleValue() : null;
        }
      }
      break;
    case FOPQ_AVOIDANCE:
      title = "&nbsp;&nbsp;&nbsp;&nbsp;Avoidance subscale";
      printStudy = getPrintStudy("childFOPQ", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("childFOPQ", patientStudies);
        if (pedChartScore != null) {
          BigDecimal score = pedChartScore.getScores().get("Avoidance");
          pedScore = (score != null) ? score.doubleValue() : null;
        }
      }
      break;
    case ARCS_PROTECT:
      title = "Parent Protective Behaviors (ARCS)";
      printStudy = getPrintStudy("ARCSProtect2", printStudies);
      if (printStudy == null) {
        printStudy = getPrintStudy("ARCSPainResponse", printStudies);
      }
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore proxyChartScore = getScore(printStudy.getStudyDescription(), patientStudies);
        if (proxyChartScore != null) {
          BigDecimal score = proxyChartScore.getScores().get("Protect");
          proxyScore = (score != null) ? score.doubleValue() : null;
          proxyCategoryLabel = (score != null) ? proxyChartScore.getCategoryLabel() : "";
        }
      }
      break;
    case ARCS_MINIMIZE:
      title = "Parent Minimize Behaviors (ARCS)";
      printStudy = getPrintStudy("ARCSPainResponse", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore proxyChartScore = getScore("ARCSPainResponse", patientStudies);
        if (proxyChartScore != null) {
          BigDecimal score = proxyChartScore.getScores().get("Protect");
          proxyScore = (score != null) ? score.doubleValue() : null;
          proxyCategoryLabel = (score != null) ? proxyChartScore.getCategoryLabel() : "";
        }
      }
      break;
    case ARCS_DISTRACT:
      title = "Parent Distract Behaviors (ARCS)";
      printStudy = getPrintStudy("ARCSPainResponse", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore proxyChartScore = getScore("ARCSPainResponse", patientStudies);
        if (proxyChartScore != null) {
          BigDecimal score = proxyChartScore.getScores().get("Protect");
          proxyScore = (score != null) ? score.doubleValue() : null;
          proxyCategoryLabel = (score != null) ? proxyChartScore.getCategoryLabel() : "";
        }
      }
      break;
    case SELF_EFFICACY:
      title = "Self-Efficacy";
      printStudy = getPrintStudy("childSelfEfficacy", printStudies);
      if (printStudy != null) {
        inverted = printStudy.getInvert();
        ChartScore pedChartScore = getScore("childSelfEfficacy", patientStudies);
        if (pedChartScore != null) {
          pedScore = pedChartScore.getScore().doubleValue();
          pedCategoryLabel = pedChartScore.getCategoryLabel();
        }
        ChartScore proxyChartScore = getScore("proxySelfEfficacy", patientStudies);
        if (proxyChartScore == null) {
          proxyChartScore = getScore("proxySelfEfficacy2", patientStudies);
        }
        if (proxyChartScore != null) {
          proxyScore = proxyChartScore.getScore().doubleValue();
          proxyCategoryLabel = proxyChartScore.getCategoryLabel();
        }
      }
      break;
    }

    ScoreData scoreData = null;
    if (printStudy != null) {
      scoreData = new ScoreData();
      scoreData.printStudy = printStudy;
      scoreData.title = title;
      scoreData.pedScore = pedScore;
      scoreData.proxyScore = proxyScore;
      scoreData.inverted = inverted;
      scoreData.pedCategoryLabel = pedCategoryLabel;
      scoreData.proxyCategoryLabel = proxyCategoryLabel;
    }
    return scoreData;
  }

  protected PrintStudy getPrintStudy(String name, ArrayList<PrintStudy> printStudies) {
    for(PrintStudy printStudy : printStudies) {
      if (name.equals(printStudy.getStudyDescription())) {
        return printStudy;
      }
    }
    return null;
  }

  protected PatientStudyExtendedData getPatientStudy(String name, ArrayList<PatientStudyExtendedData> patientStudies) {
    for(PatientStudyExtendedData patientStudy : patientStudies) {
      if (name.equals(patientStudy.getStudyDescription())) {
        return patientStudy;
      }
    }
    return null;
  }

  protected ChartScore getScore(String name, ArrayList<PatientStudyExtendedData> patientStudies) {
    PatientStudyExtendedData patientStudy = getPatientStudy(name, patientStudies);
    if (patientStudy != null) {
      ScoreProvider scoreProvider = SurveyServiceFactory.getFactory(siteInfo)
          .getScoreProvider(dbp, patientStudy.getSurveySystemName(), patientStudy.getStudyDescription());
      if (scoreProvider != null) {
        List<ChartScore> scores = scoreProvider.getScore(patientStudy);
        if ((scores != null) && !scores.isEmpty()) {
          return scores.get(0);
        }
      }
    }
    return null;
  }

  private class ScoreData {
    public PrintStudy printStudy;
    public String title;
    public Double pedScore;
    public Double proxyScore;
    public boolean inverted;
    String pedCategoryLabel;
    String proxyCategoryLabel;
  }
}
