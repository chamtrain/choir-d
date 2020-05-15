/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.trauma;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.reports.PatientInfo;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.utils.PDFArea;
import edu.stanford.registry.server.utils.PDFUtils;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.User;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

/**
 * Custom Report for Trauma Outcomes Project. Created by tpacht on 06/16/2017.
 */
class PatientReport extends edu.stanford.registry.server.reports.PatientReport {

  private static final int GAP = 10;
  private static final int HALFGAP = 5;
  private static final int WEEGAP = 3;
  private static final Logger logger = LoggerFactory.getLogger(PatientReport.class);

  private final PDFont[] helveArray = { PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE,
      PDType1Font.HELVETICA_BOLD_OBLIQUE };
  private final PDFont[] fontArray = helveArray;

  private static final HashSet<String> chartedSurveys = new HashSet<>();


  private String[] getChartedSurveys() {
    return new String[] {};
  }


  PatientReport(Supplier<Database> dbp, SiteInfo siteInfo, User user) {
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
      logger.error("Error getting list of studies for process: {}", process);
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

    // Trauma has no images

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
    logger.debug(" Processing {} Charts", charts.size());
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


    PDFUtils.writeHighlightBox(contentStream, XMARGIN, y, 10, topOfPage - y,
        OPTION_SECTION_BACKGROUND_COLOR); // Left
    PDFUtils.writeHighlightBox(contentStream, (PDFUtils.pageWidth(page) - (XMARGIN)) - 10, y, 10, topOfPage - y,
        OPTION_SECTION_BACKGROUND_COLOR); // Right
    PDFUtils.writeHighlightBox(contentStream, XMARGIN, y - 10, PDFUtils.pageWidth(page) - (XMARGIN * 2), 10,
        OPTION_SECTION_BACKGROUND_COLOR); // Bottom
    y -= 12;
    int surveys = 0;
    long surveyRegId = 0L;
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
        logger.debug(" checking if {} should be included in combo chart", printStudy.getStudyDescription());
        if (printStudy.getSurveySystemName().startsWith("PROMIS")
            || printStudy.getSurveySystemName().startsWith("LocalPromis")
            || chartedSurveys.contains(printStudy.getStudyDescription())
            ) {
          logger.debug("including {}", printStudy.getStudyDescription());
          PrintStudy newPrintStudy = printStudy.clone();
          newPrintStudy.setPrintOrder(newChartPrintOrder);
          newChartPrintStudies.add(newPrintStudy);
        }
      }

      /* create a combined chartInfo hashmap */
      HashMap<Integer, ChartInfo> newChartInfo = getChartInfo(newChartPrintStudies, patientStudies, patientId, opts);
      logger.debug("Initial report has {} studies with {} charts", newChartPrintStudies.size(), newChartInfo.size() );
      float scoresTableW = ((PDFUtils.pageWidth(page)) - (XMARGIN * 2f)) * .64f;
      if (newChartPrintStudies.size() > 0) {
        PDFArea area = drawScoresTable(newChartInfo.get(newChartPrintOrder), newChartPrintStudies,
            newChartPrintOrder, opts, XMARGIN - WEEGAP, y, scoresTableW, "Outcome Scores");

        y = area.getNextY();
      }
      //y -= GAP + 20;
      if (downArrow != null) {
        PDFArea asstArea = drawDownArrow(downArrow, " <i>This was an assisted survey</i>", false,
            startX + GAP / 2, y, opts, page,
            startX + GAP / 2);
        y -= asstArea.getYto();
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


}
