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
package edu.stanford.registry.server.reports;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.utils.PDFArea;
import edu.stanford.registry.server.utils.PDFUtils;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.User;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.github.susom.database.Database;

public class PainManagementPatientReport extends PatientReport  {


  public PainManagementPatientReport(Supplier<Database> dbp, SiteInfo siteInfo, User user) {
    super(dbp, siteInfo, user);
  }

  private final int[] printText = { Constants.XML_PROCESS_PRINT_TYPE_TEXT };
  @Override
  public float drawText(ArrayList<PatientStudyExtendedData> patientStudies, ArrayList<PrintStudy> printStudies,
                        PatientInfo patientInfo, ChartConfigurationOptions opts, PDPage page, int index, float y) throws Exception {

    float returnFloat =  super.drawText(patientStudies, printStudies, patientInfo, opts, page, index, y);

    // Find the painCatastrophizing scale and add its questions to the end of the report
    for (PrintStudy printStudy : printStudies) {
      if ("painCatastrophizingScale".equals(printStudy.getStudyDescription())) {
        printStudy.setPrintTypes(printText);
        ArrayList<PrintStudy> pcsArray = new ArrayList<>();
        pcsArray.add(printStudy);
        returnFloat = super.drawText(patientStudies, pcsArray, patientInfo, opts, page, index, returnFloat);
      }
    }

    return returnFloat;
  }

  @Override
  public float drawCustom(ArrayList<PrintStudy> printStudies, ArrayList<PatientStudyExtendedData> patientStudies,
                          HashMap<String, ArrayList<PDImageXObject>> ximages, PatientInfo patientInfo,
                          ChartConfigurationOptions opts, PDPage page, Float y)
      throws IOException {
    /*
     * Handle 2nd bodymap on PT Eval surveys
     */
    for (PrintStudy printStudy: printStudies) {
      if ("bodymapPT".equals(printStudy.getStudyDescription())) {
        Integer printOrder = printStudy.getPrintOrder();
        ArrayList<PDImageXObject> jpegs = ximages.get(printOrder.toString());
        float mapSpace = page.getMediaBox().getHeight() / 2; // default space required
        if (jpegs != null && jpegs.size() > 0) {
          if (jpegs.get(0) != null && jpegs.get(0).getHeight() > mapSpace)
            mapSpace = jpegs.get(0).getHeight() * .4f;
        } else {
          return y;  // If no image skip it
        }
        // Check if we need to start a new page for the bodymap & charts
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

        PDFArea area = drawImage(page,printStudy, patientStudies, jpegs, y + tblTextHeight, opts);
        // Write the PT Header
        PDFUtils.writePDFHtml(contentStream, "-", PDFUtils.pageWidth(page) / 2 - (XMARGIN) , y - (GAP + WEEGAP),
            area.getXto() + 2f, fontArray, tFontSize, HEADING_BACKGROUND_COLOR, HEADING_BACKGROUND_COLOR);
        y = nextY(PDFUtils.writePDFHtml(contentStream, "Physical Therapy Initial Evaluation",
            PDFUtils.pageWidth(page) / 2 - GAP, y - (GAP + WEEGAP), PDFUtils.pageWidth(page) - XMARGIN,
            fontArray, tFontSize, HEADING_TEXT_COLOR, HEADING_BACKGROUND_COLOR));
        y += HALFGAP;
        PDFUtils.writePDFHtml(contentStream,
            "Areas with other unpleasant sensations (tingling, prickling, burning, numbness, heaviness, other)",
            PDFUtils.pageWidth(page) / 2 - GAP, y - (GAP + WEEGAP), PDFUtils.pageWidth(page) - (XMARGIN + WEEGAP),
            fontArray, tFontSize, Color.BLACK, ChartConfigurationOptions.getColor(opts.getColorOption(ConfigurationOptions.OPTION_ODD_COLOR)));
        y = area.getYto();
        y -= HALFGAP;
      }
    }
    return y;
  }
}
