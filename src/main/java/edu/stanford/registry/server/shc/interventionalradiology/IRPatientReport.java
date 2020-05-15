package edu.stanford.registry.server.shc.interventionalradiology;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.reports.PatientInfo;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PageNumber;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.utils.PDFArea;
import edu.stanford.registry.server.utils.PDFUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.MultiScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.User;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.JFreeChart;

import com.github.susom.database.Database;

/**
 * Created by scweber on 02/24/2016.
 */
public class IRPatientReport extends PatientReport {

  protected final DecimalFormat scoreFormatter = new DecimalFormat("##.##");

  public IRPatientReport(Supplier<Database> dbp, SiteInfo siteInfo, User user) {
    super(dbp, siteInfo, user);
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

    // Then create the jfreechart images
    HashMap<Integer, ChartInfo> chartInfoHash = getChartInfo(printStudies, patientStudies, patientId, opts);
    HashMap<Integer, PDImageXObject> charts = new HashMap<>();
    Integer height = getScaledValue(opts, opts.getHeight());
    Integer width = getScaledValue(opts, opts.getWidth());
    int scaleImageBy = 3;
    for (Integer key : chartInfoHash.keySet()) {
      JFreeChart chart = chartInfoHash.get(key).getChart();
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

    if (FOLLOW_UP_REPORT.equals(report) && printStudies.size() > index) {
      y += GAP;
      /* Add the promis tables and charts */
      y = drawCharts(patientStudies, printStudies, chartInfoHash, charts, patientInfo, opts, page, index, y, startX);
      y -= GAP;
      drawDownArrow(downArrow, " <i>Indicates an assisted survey</i>", true, startX, y, opts, page, XMARGIN);
      y -= GAP;
    }
    y -= HALFGAP; // move down

    /* add the measures with print_type "text" */
    try {
      y = drawText(patientStudies, printStudies, patientInfo, opts, page, 0, y);
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
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
  public float drawTitle(PDPage page, PageNumber pageCount, PatientInfo patientInfo) throws IOException {
    float y = super.drawTitle(page, pageCount, patientInfo);

    // Print the CHOIR Questionnaire barcode on the first page
    if (pageCount.intValue() == 1) {
      BufferedImage barcode = PDFUtils.getImage("shc/ir/CHOIR_Questionnaire_Barcode.jpg");
      if (barcode != null) {
        PDImageXObject jpg = JPEGFactory.createFromImage(pdf, barcode);
        float x = (page.getMediaBox().getWidth() - (barcode.getWidth()/2)) / 2;
        float y2 = y + textHeight - WEEGAP;
        contentStream.drawImage(jpg, x, y2, barcode.getWidth()/2, barcode.getHeight()/2);
      }
    }
    return y;
  }

  @Override
  public float drawTitleLine3(PDPage page, PatientInfo patientInfo, float y3, float boxMargin) throws IOException {
    PDFont font = fontArray[0];
    // Left pad MRN with zeros so that the MRN can be correctly scanned into Epic
    String mrn = patientInfo.getMrn();
    String mrnText = "MRN " + "0000000".substring(0,9-mrn.length()) + mrn;
    String genderText = "Gender " + patientInfo.getGender();
    PDFUtils.writePDFHtml(contentStream, mrnText, XMARGIN + 4f, y3, PDFUtils.pageWidth(page) - boxMargin, fontArray,
        tFontSize, Color.BLACK, Color.white);
    float x = page.getMediaBox().getWidth() - boxMargin - ((font.getStringWidth(genderText) / 1000) * tFontSize) - 2;
    y3 = nextY(PDFUtils.writePDFHtml(contentStream, genderText, x, y3, PDFUtils.pageWidth(page) - boxMargin, fontArray,
        tFontSize, Color.BLACK, Color.white));
    return y3;
  }

  @Override
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
              // Changed the header text from 'PROMIS Outcomes Measures' to 'Outcome Measures'
              y = nextY(PDFUtils.writePDFHtml(contentStream, "Outcomes Measures", XMARGIN + 2f, y
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

  @Override
  // Modified the base version to remove the percentile and category output
  public PDFArea drawScoresTable(ChartInfo chartInfo, ArrayList<PrintStudy> printStudies, Integer printOrder,
      ChartConfigurationOptions opts, float x, float y, float tableWidth, String heading) throws IOException {

    float spWidth = 4f;
    float scorWidth = Math.round(PDFUtils.getTextWidth(fontArray[0], tblFontSize, "Score"));
    float lgndWidth = 15f;
    float titlWidth = tableWidth - (spWidth + scorWidth + spWidth + lgndWidth + spWidth);

    x += GAP / 2;
    if (!FOLLOW_UP_REPORT.equals(report)) {
      titlWidth += (lgndWidth - (spWidth));
      lgndWidth = spWidth;
    }

    int imageHeight = getScaledValue(opts, opts.getHeight());
    PDFArea returnArea = new PDFArea();
    returnArea.setXfrom(x);
    returnArea.setXto(x + tableWidth);

    Color tableColor = ChartConfigurationOptions.getColor(
        opts.getColorOption(ConfigurationOptions.OPTION_TABLE_BACKGROUND_COLOR));
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

    y = nextY(PDFUtils.writePDFHtml(contentStream, "-", endX, y, endX + lgndWidth + spWidth, fontArray, tblFontSize,
        HEADING_BACKGROUND_COLOR, HEADING_BACKGROUND_COLOR));
    returnArea.setNextY(y);

    PDFUtils.writeHighlightBox(contentStream, x, y + tblTextHeight / 2, tableWidth, tblTextHeight / 2,
        HEADING_BACKGROUND_COLOR);

    y = returnArea.getNextY();
    y -= tblTextHeight / 2;

    y -= tblTextHeight / 2;
    endX = x; // back to start

    /*
     * Write the measures
     */
    titlWidth += 8; // give the study title a little more room
    scorWidth -= 8; // score values don't need as much as the heading
    int colorInx = 0;
    PDFArea area;
    for (PrintStudy printStudy1 : printStudies) {
      if (printStudy1.getPrintOrder() == printOrder) {
        PrintStudy printStudy = printStudy1;

        /* move down a bit and then draw the background */
        y -= tblTextHeight;
        area = PDFUtils.writePDFHtml(contentStream, "-", x, y, endX + tableWidth, fontArray, tblFontSize, tableColor,
            tableColor);

        endX += spWidth;

        String title = printStudy1.getTitle();
        if (title.indexOf("PROMIS") == 0) {
          title = title.substring(7);
        }
        boolean inverted = false;
        String scoreVal = "";
        ChartScore chartScore = null;
        if (chartInfo != null) {
          ArrayList<ChartScore> scores = chartInfo.getScores();
          for (ChartScore score1 : scores) {
            if (score1.getStudyCode().intValue() == printStudy.getStudyCode().intValue()) {
              chartScore = score1;
            }
          }
        }
        if (chartScore != null) {
          Double score = chartScore.getScore().doubleValue();
          if (printStudy1.getInvert()) {
            inverted = true;
            score = invertScore(score);
          }
          scoreVal = scoreFormatter.format(score);
        }

        /*
         * Write the study name
         */
        if (chartScore instanceof MultiScore) {
          title = ((MultiScore) chartScore).getTitle(1, title);
          Double score = ((MultiScore) chartScore).getScore(1);
          if (printStudy1.getInvert()) {
            inverted = true;
            score = invertScore(score);
          }
          scoreVal = scoreFormatter.format(score);
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
        writeAt += (scorWidth - PDFUtils.getTextWidth(fontArray[0], tblFontSize, scoreVal));
        PDFUtils.writePDFHtml(contentStream, scoreVal, writeAt, y, endX + scorWidth, fontArray, tblFontSize,
            Color.black, tableColor);

        if (chartScore instanceof MultiScore) {
          for (int s=2; s<= ((MultiScore) chartScore).getNumberOfScores(); s++) {
            Double multiScore = ((MultiScore) chartScore).getScore(s);
            if (inverted) {
              multiScore = invertScore(multiScore);
            }
            scoreVal = scoreFormatter.format(multiScore);
            PDFUtils.writePDFHtml(contentStream, scoreVal, writeAt, y - multiSpace * (s-1), endX + scorWidth,
                fontArray, tblFontSize, Color.black, tableColor);
          }
        }

        endX += scorWidth + spWidth;

        /*
         * write legend column
         */
        writeAt = endX + (lgndWidth / 2) - 16f;
        if (colorInx >= ChartConfigurationOptions.LINE_COLORS.length) {
          colorInx = 0;
        }
        if (FOLLOW_UP_REPORT.equals(report)) {
          contentStream.drawImage(legend[colorInx], endX, y + (tblTextHeight / 2) - 4f, 16f, 4f);

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
    return returnArea;
  }

}
