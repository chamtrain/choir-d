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

package edu.stanford.registry.server.shc.gi;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.reports.PatientInfo;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.RegistryShortFormScoreProvider;
import edu.stanford.registry.server.utils.PDFArea;
import edu.stanford.registry.server.utils.PDFUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.User;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.JFreeChart;

import com.github.susom.database.Database;

public class GIPatientReport extends PatientReport {

  public GIPatientReport(Supplier<Database> dbp, SiteInfo siteInfo, User user) {
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
//    y = drawTreatmentSets(page, patientInfo, y); // add a line for each TreatmentSet the patient is in
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
            || printStudy.getSurveySystemName().startsWith("GISQSurveyService")
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
            y + 7, scoresTableW, "Outcomes Scores");

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



}
