package edu.stanford.registry.server.shc.orthohand;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.JFreeChart;

import com.github.susom.database.Database;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.reports.PatientInfo;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.utils.PDFArea;
import edu.stanford.registry.server.utils.PDFUtils;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;


/**
 * Created by tpacht on 10/9/2015.
 */
public class OrthoHandPatientReport extends PatientReport {

  private static final int GAP = 10;
  private static final int HALFGAP = 5;
  private static final int WEEGAP = 3;
  private static final Logger logger = Logger.getLogger(OrthoHandPatientReport.class);

  private final PDFont[] helveArray = { PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE,
      PDType1Font.HELVETICA_BOLD_OBLIQUE };
  private final PDFont[] fontArray = helveArray;

  private static final HashSet<String> chartedSurveys = new HashSet<>();


  private String[] getChartedSurveys() {
    return new String[] {"handGlobalHealth", "prwhe", "handqDASH", "handSane"};
  }


  public OrthoHandPatientReport(Supplier<Database> dbp, SiteInfo siteInfo, User user) {
    super(dbp, siteInfo, user);
    String[] chartedSurveyAr = getChartedSurveys(); // expect a subclass to override this
    Collections.addAll(chartedSurveys, chartedSurveyAr);
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
    ArrayList<PrintStudy> printStudies;

    try {
      printStudies = getPrintStudies(process);

    } catch (Exception e) {
      logger.error("Error getting ordered list of study descriptions from process.xml", e);
      printStudies = new ArrayList<>();
    }

    if (printStudies == null || printStudies.size() < 1) {
      logger.error("Error getting list of studies for process: " + process);
      writeError(patientInfo.getMrn(), "Unknown survey type " + process);
      return pdf;
    }
    BODYMAP_FILL_COLOR = ChartConfigurationOptions.getColor(
        (ConfigurationOptions.GRAY), 100);
    OPTION_SECTION_BACKGROUND_COLOR = ChartConfigurationOptions.getColor(ConfigurationOptions.GRAY5);

    /*
     * We have to make the images before opening the content stream or 'out of
     * memory' error will occur!!
     */

    // First get the body map images
    HashMap<String, ArrayList<PDImageXObject>> ximages = getImages(printStudies, patientStudies, patientId, opts);
    logger.debug(" Processing " + ximages.size() + " Images");
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
      if (downArrow == null && patientStudy.wasAssisted()) {
        Color backColor = ChartConfigurationOptions.getColor(opts
            .getColorOption(ConfigurationOptions.OPTION_TABLE_BACKGROUND_COLOR));
        downArrow = JPEGFactory.createFromImage(pdf, PDFUtils.downArrow(Color.red, backColor));
      }
    }

    PDPage page = new PDPage(PDRectangle.LETTER);
    contentStream = PDFUtils.startNewPage(page, contentStream, pdf, fontArray[0], tFontSize);
    float y = drawTitle(page, pageCount, patientInfo);
    topOfPage = y;
    int index = 0;
    float startX = XMARGIN + 20;
    PDFUtils.writeHighlightBox(contentStream, XMARGIN, y, PDFUtils.pageWidth(page) - (XMARGIN * 2), 10,
        ChartConfigurationOptions.getColor(opts.getColorOption(ConfigurationOptions.OPTION_SECTION_BACKGROUND_COLOR)));
    y -= 10;
    int studyCount = 0;
    float endY = y;
    for (PrintStudy printStudy : printStudies) {
      if (printStudy.hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_IMG)) {
        Integer printOrder = printStudy.getPrintOrder();
        ArrayList<PDImageXObject> jpegs = ximages.get(printOrder.toString());
        PDFArea area = drawImages(page, printStudy,jpegs, startX, y );
        startX = ((PDFUtils.pageWidth(page) - (XMARGIN * 2)) / 2) + 20 ;
        if (area.getYto() < endY) {
          endY = area.getYto();
        }
        studyCount++;
        if (studyCount > 1) {
          y = endY;
          startX = XMARGIN + 20;
          studyCount = 0;
        }
      }
    }

    PDFUtils.writeHighlightBox(contentStream, XMARGIN, y, 10, topOfPage - y,
        OPTION_SECTION_BACKGROUND_COLOR); // Left
    PDFUtils.writeHighlightBox(contentStream, (PDFUtils.pageWidth(page) - (XMARGIN)) - 10, y, 10, topOfPage - y,
        OPTION_SECTION_BACKGROUND_COLOR); // Right
    PDFUtils.writeHighlightBox(contentStream, XMARGIN, y - 10, PDFUtils.pageWidth(page) - (XMARGIN * 2), 10,
        OPTION_SECTION_BACKGROUND_COLOR); // Bottom
    y -= 12;
    int surveys = 0;
    long surveyRegId =  0L;
    for (PatientStudyExtendedData patStudy : patientStudies) {
      if (patStudy.getSurveyRegId() != surveyRegId) {
        surveyRegId = patStudy.getSurveyRegId();
        surveys++;
      }
    }
    if (surveys > 1) {
      /* Add the promis tables and charts */
      y = drawCharts(patientStudies, printStudies, chartInfoHash, charts, patientInfo, opts, page, index, y, startX);
      y -= GAP;
      drawDownArrow(downArrow, " <i>Indicates an assisted survey</i>", true, startX, y, opts, page, XMARGIN);
      y -= GAP;
    } else {  /* Combine all the promis measures into the same printOrder */
      report = "Initial";
      Integer newChartPrintOrder = 1;
      ArrayList<PrintStudy> newChartPrintStudies = new ArrayList<>();
      for (PrintStudy printStudy : printStudies) {
        logger.debug(" checking if " + printStudy.getStudyDescription() + " should be included in combo chart");
        if (printStudy.getSurveySystemName().startsWith("PROMIS")
            || printStudy.getSurveySystemName().startsWith("LocalPromis")
            || printStudy.getSurveySystemName().startsWith("edu.stanford.registry.server.shc.orthohand.OrthoPromis")
            || chartedSurveys.contains(printStudy.getStudyDescription())
            ) {
          logger.debug("including " + printStudy.getStudyDescription());
          PrintStudy newPrintStudy = printStudy.clone();
          newPrintStudy.setPrintOrder(newChartPrintOrder);
          newChartPrintStudies.add(newPrintStudy);
        }
      }

      /* create a combined chartInfo hashmap */
      HashMap<Integer, ChartInfo> newChartInfo = getChartInfo(newChartPrintStudies, patientStudies, patientId, opts);
      logger.debug("Initial report has " + newChartPrintStudies.size() + "studies with "
          + newChartInfo.size() + " charts");
      float scoresTableW = ((PDFUtils.pageWidth(page)) - (XMARGIN * 2f)) * .64f;
      if (newChartPrintStudies.size()  > 0) {
        PDFArea area = drawScoresTable(newChartInfo.get(newChartPrintOrder), newChartPrintStudies,
            newChartPrintOrder, opts, XMARGIN - WEEGAP, y, scoresTableW, "Outcome Scores");

        y = area.getNextY();
      }
      //y -= GAP + 20;
      if (downArrow != null) {
        PDFArea asstArea = drawDownArrow(downArrow, " <i>This was an assisted survey</i>", false, startX + GAP / 2, y, opts, page,
            startX + GAP / 2);
        y-= asstArea.getYto();
      }
      y -= HALFGAP; // move down
    }
    y -= HALFGAP; // move down

    /* add the measures with print_type "text" */
    try {
      drawText(patientStudies, printStudies, patientInfo, opts, page, 0, y);
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
  public HashMap<String, ArrayList<PDImageXObject>> getImages(ArrayList<PrintStudy> studies,
                                                      ArrayList<PatientStudyExtendedData> patientStudies,
                                                      String patientId, ChartConfigurationOptions opts)
      throws IOException {
    // override the method because we print all images, even if no areas were selected
    HashMap<String, ArrayList<PDImageXObject>> images = new HashMap<>();
    for (PrintStudy study : studies) {
      if (!study.hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_IMG)) {
        continue;
      }
      ScoreProvider provider = SurveyServiceFactory.getFactory(siteInfo).getScoreProvider(dbp, study.getSurveySystemName(),
          study.getStudyDescription());

      ArrayList<SurveyQuestionIntf> questions = provider.getSurvey(patientStudies, study, patientDao.getPatient(patientId), true);
      ArrayList<PDImageXObject> pdjpegs = new ArrayList<>();
      for (SurveyQuestionIntf question : questions) {
        if (question.getAttribute("ItemResponse") != null && !question.getAttribute("ItemResponse").trim().isEmpty()) {
          HashMap<String, BufferedImage> bufImages = PDFUtils.getPaintedImages(question, pdf,
              BODYMAP_STROKE_COLOR, BODYMAP_FILL_COLOR);
          PDImageXObject orderedImgs[] = new PDImageXObject[2];
          for (String imageName : bufImages.keySet()) {
            BufferedImage image = bufImages.get(imageName);
            if (image != null) {
              PDImageXObject img = JPEGFactory.createFromImage(pdf, image);
              if (imageName.contains("left")) { // Left images need to print 'back - front'
                if (imageName.contains("back")) {
                  orderedImgs[0] = img;
                } else {
                  orderedImgs[1] = img;
                }
              } else { // Right images need to print 'front - back'
                if (imageName.contains("front")) {
                  orderedImgs[0] = img;
                } else {
                  orderedImgs[1] = img;
                }
              }
            }
          }
          for (PDImageXObject img : orderedImgs) {
            if (img != null) {
              pdjpegs.add(img);
            }
          }
        }
      }
      images.put(study.getPrintOrder() + "", pdjpegs);
    } // main loop ends
    return images;
  }

  @Override
  public Long calculatePercentile(Long score, PrintStudy printStudy) {
    if ("handqDASH".equals(printStudy.getStudyDescription())) {
      return null;
    } else {
      return super.calculatePercentile(score, printStudy);
    }
  }

  @Override
  public void addChartSpaceAfterMultiScore(PDFArea returnArea, float lineSize) {
    if (FOLLOW_UP_REPORT.equals(report)) {
      returnArea.setNextY(returnArea.getNextY() - (lineSize));
    }
  }

  private PDFArea  drawImages(PDPage page, PrintStudy study, ArrayList<PDImageXObject> jpegs,float xCoord, float yCoord) throws IOException {
    logger.debug("IMG - drawing images for " + study.getStudyDescription());
    PDFArea area = new PDFArea();
    area.setXfrom(xCoord);
    area.setYfrom(yCoord);
    float frameWidth = (PDFUtils.pageWidth(page) - (XMARGIN * 2)) / 2;
    float imgHeight = getImagesHeight(jpegs) * .4f;
    if (jpegs == null || imgHeight == 0L) {
      String message = "No areas selected on the " + study.getTitle();
      contentStream.moveTo(xCoord, yCoord - fontSize);
      PDFUtils.writePDFHtml(contentStream, message,
          xCoord + 10, yCoord - fontSize * 3, frameWidth + xCoord + 10, fontArray, fontSize);
      area.setXto(xCoord + 10 + frameWidth );
      area.setYto(yCoord - (fontSize * 5) + 1);
      return area;
    }

    contentStream.moveTo(xCoord, yCoord);

    for (PDImageXObject jpeg : jpegs) {
      if (jpeg != null) {
        float imgWidth = jpeg.getWidth() * .4f;
        contentStream.drawImage(jpeg, xCoord, yCoord - (imgHeight + HALFGAP), imgWidth , imgHeight);
        xCoord += imgWidth ;
        area.setXto(xCoord + imgWidth + GAP);
      }
    }
    yCoord -= (imgHeight + GAP);
    area.setYto(yCoord);
    area.setXto(area.getXto() + GAP);

    return area;
  }


  private float getImagesHeight(ArrayList<PDImageXObject> jpegs  ) {
    float gridHeight = 0f;
    if (jpegs != null) {
      for (int j = 0; j < jpegs.size(); j++) {
        if (gridHeight < jpegs.get(j).getHeight()) {
          gridHeight = jpegs.get(j).getHeight();
        }
      }
    }
    return gridHeight;
  }
}
