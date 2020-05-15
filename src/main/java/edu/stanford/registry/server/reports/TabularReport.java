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

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.github.susom.database.Database;

public abstract class TabularReport {
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(TabularReport.class);
  private String title = "Compliance by Month Report ";

  private int fontSize = 12;
  private static final int XMARGIN = 20;
  private static final int YMARGIN = 40;
  // private static final int TABLE_WIDTH = 130;
  private static final int CELL_MARGIN = 5;

  public TabularReport(Database database) {

  }

  public PDDocument makePdf(ArrayList<ArrayList<Object>> report) throws IOException {

    PDDocument pdf = new PDDocument();
    PDPage page = null;
    PDPageContentStream contentStream = null;
    PDFont font = PDType1Font.COURIER_BOLD;

    float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000;
    textHeight = textHeight * fontSize * 1.85f;
    int pageCount = 0;
    float y = -1;

    ArrayList<Integer> columnWidths = getColumnWidths(report, font);
    for (int rIndx = 0; rIndx < report.size(); rIndx++) {

      if (y < (YMARGIN + textHeight)) {
        // Start a new page
        page = new PDPage(PDRectangle.LETTER);
        pdf.addPage(page);
        pageCount++;
        if (contentStream != null) {
          contentStream.close();
        }
        contentStream = new PDPageContentStream(pdf, page, true, false);
        contentStream.setFont(font, fontSize * 1.8f);

        // Write the title line just below the margin
        y = page.getMediaBox().getHeight() - YMARGIN;
        contentStream.beginText();
        contentStream.moveTextPositionByAmount(XMARGIN, y);
        contentStream.drawString(title);
        String pageText = new String("Page " + pageCount);
        float x = page.getMediaBox().getWidth() - (XMARGIN * 2) - ((font.getStringWidth(pageText) / 1000) * fontSize);
        contentStream.moveTextPositionByAmount(x, 0); // right justify the page#
        contentStream.drawString(pageText);
        contentStream.endText();
        y -= textHeight;
        contentStream.setFont(font, fontSize);
      } else { // when writing on the same page
        // y -= gap; // put some space before the next image
      }

      // write the data

      contentStream.setFont(font, fontSize);
      if (report == null || report.size() == 0) {
        contentStream.beginText();
        contentStream.moveTextPositionByAmount(XMARGIN, y);
        contentStream.drawString("NO DATA");
      } else {
        // write the report data
        for (ArrayList<Object> reportLine : report) {
          float x = XMARGIN;
          if (reportLine != null) {
            for (int colIndx = 0; colIndx < reportLine.size(); colIndx++) {
              writePDFText(contentStream, reportLine.get(colIndx).toString(), x, y);
              x = +(columnWidths.get(colIndx) + CELL_MARGIN);
            }
          }
        }
      }

    }

    return pdf;

  }

  public ArrayList<Integer> getColumnWidths(ArrayList<ArrayList<Object>> data, PDFont font) throws IOException {
    int columns = 0;
    ArrayList<Integer> widths = new ArrayList<>();
    if (data == null) {
      return widths;
    }

    for (ArrayList<Object> aData : data) {
      if (aData != null && aData.size() > columns) {
        columns = aData.size();
      }
    }

    for (int colIndx = 0; colIndx < columns; colIndx++) {
      widths.add(0);
    }

    for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
      ArrayList<Object> row = data.get(rowIndex);
      if (data != null) {

        for (int colIndx = 0; colIndx < row.size(); colIndx++) {
          String value = row.get(colIndx).toString();
          if (value != null) {
            Float lineLength = (font.getStringWidth(value) / 1000) * (fontSize);
            if (lineLength > widths.get(colIndx)) {
              widths.set(colIndx, lineLength.intValue());
            }
          }
        }
      }
    }

    return widths;

  }

  public void writePDFText(PDPageContentStream contentStream, String string, float x, float y) throws IOException {
    contentStream.beginText();
    contentStream.moveTextPositionByAmount(x, y);
    contentStream.drawString(string);
    contentStream.endText();
  }

}
