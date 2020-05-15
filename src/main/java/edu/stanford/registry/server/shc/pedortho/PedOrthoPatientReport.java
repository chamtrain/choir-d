package edu.stanford.registry.server.shc.pedortho;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.reports.PatientInfo;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.survey.PageNumber;
import edu.stanford.registry.server.utils.PDFUtils;
import edu.stanford.registry.shared.User;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Supplier;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.github.susom.database.Database;

public class PedOrthoPatientReport extends PatientReport {

  public PedOrthoPatientReport(Supplier<Database> dbp, SiteInfo siteInfo, User user) {
    super(dbp, siteInfo, user);
  }

  @Override
  public float drawTitle(PDPage page, PageNumber pageCount, PatientInfo patientInfo) throws IOException {
    float y = super.drawTitle(page, pageCount, patientInfo);

    // Print the CHOIR Questionnaire barcode on the first page
    if (pageCount.intValue() == 1) {
      BufferedImage barcode = PDFUtils.getImage("shc/portho/CHOIR_Questionnaire_Barcode.jpg");
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
}
