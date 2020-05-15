package edu.stanford.registry.server.shc.preanesthesia;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.reports.PatientInfo;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.utils.PDFArea;
import edu.stanford.registry.server.utils.PDFUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;

import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.github.susom.database.Database;

/**
 * Created by tpacht on 11/19/2015.
 */
public class PreAnesthesiaPatientReport extends PatientReport {

  public PreAnesthesiaPatientReport(Supplier<Database> dbp, SiteInfo siteInfo, User user) {
    super(dbp, siteInfo, user);
  }

  @Override
  public PDDocument makePdf(ArrayList<PatientStudyExtendedData> patientStudies, String process, String patientId,
                            ChartConfigurationOptions opts, PDDocument pdf) throws IOException, InvalidDataElementException {

    if (patientStudies != null && patientStudies.size() > 0) {
      ArrayList<PrintStudy> printStudies = getPrintStudies(process);
      for (PrintStudy printStudy : printStudies) {
        boolean found = false;
        for (PatientStudyExtendedData patStudy : patientStudies) {
          if (printStudy.getSurveySystemId().intValue() == patStudy.getSurveySystemId().intValue()
              && printStudy.getStudyCode().intValue() == patStudy.getStudyCode().intValue()) {
            found = true;
          }
        }
        if (!found && printStudy.hasPrintType(edu.stanford.registry.shared.Constants.XML_PROCESS_PRINT_TYPE_TEXT)) {
          // Add a blank one to the array so the questions are included on the report

          Long surveyRegId = assessment.getSurveyRegList().get(0).getSurveyRegId();
          PatientStudyExtendedData emptyForm =
          getPatientStudyData(dbp.get(), patientStudies.get(0).getPatient(), printStudy,
              patientStudies.get(0).getToken(), surveyRegId);
          if (emptyForm != null) {
            patientStudies.add(emptyForm);
            logger.debug("Adding an empty " + printStudy.getStudyCode() + " " + printStudy.getStudyDescription());
          }
        }
      }
    }
    logger.debug("patientStudies has " + patientStudies.size() + " objects");
    return super.makePdf(patientStudies, process, patientId, opts, pdf);
  }

  @Override
  public boolean getImageFirst( ) {
    if (FOLLOW_UP_REPORT.equals(report)) {
      return true;
    }
    return false;
  }

  @Override
  public PDFArea drawSelectAnswer(SurveyAnswerIntf ans,  PDFArea aArea, float xStart, float xEnd, float questionFontSize,
                                  Color banding) throws IOException {

    SelectElement answer = (SelectElement) ans;
    boolean checks = false;
    float spacing = 0f;
    float selectionsTextSize = fontSize;
    if (answer.getAttribute("checks")  != null & "true".equals(answer.getAttribute("checks"))) {
      checks = true;
      questionFontSize = 7; // override to print with a smaller font
      selectionsTextSize = PDFUtils.getTextHeight(fontArray[0], questionFontSize) * 1.1f;
      spacing = ((textHeight * 1.1f) - (selectionsTextSize));
    }
    boolean first = true;
    for (SelectItem item : answer.getItems()) {
      float yPos = aArea.getNextY();
      if (item.getSelected() || checks) {
        // draw the checkbox background
        float xPos = xStart + 2;
        float boxHt = aArea.getYfrom() - aArea.getYto();
        if (boxHt < selectionsTextSize ) {
          boxHt = selectionsTextSize * 1.1f;
          yPos = yPos + spacing;
        }
        //PDFUtils.writeHighlightBox(contentStream, xStart, yPos - 2, 12, boxHt, banding);// lastone
        float width = first? (getRightEdge() - xStart) : 7;
        PDFUtils.writeHighlightBox(contentStream, xStart, yPos -2, width, boxHt, banding);
        if (checks && item.getLabel() != null && !item.getLabel().startsWith("<b>X ")) {
          xPos = xStart + 8;
        }
        aArea = PDFUtils.writePDFHtml(contentStream, item.getLabel(), xPos,
            yPos, xEnd, fontArray, questionFontSize, Color.BLACK, banding);
        if (checks) {
          PDFUtils.drawBoxOutline(contentStream, xStart + 2, yPos, 5, selectionsTextSize - 2, Color.black);
        } else {
          PDFUtils.writeHighlightBox(contentStream, xStart, aArea.getYto(), 2,
              aArea.getYfrom() - aArea.getYto(), banding);
        }
      }
      first = false;
    }
    if (checks) {
      aArea.setNextY(aArea.getNextY() - spacing);
    }
    return aArea;
  }

  @Override
  public float questionSize(SurveyQuestionIntf question, int startInx, int qFontSize, float questionWidth, float answerWidth) {

    ArrayList<SurveyAnswerIntf> answers = question.getAnswers(true);
    for (SurveyAnswerIntf ans : answers) {
      if (ans.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_SELECT) {
        if (Constants.TYPE_SELECT == ans.getType()) {
            if ( ans.getAttribute("checks")  != null & "true".equals(ans.getAttribute("checks"))) {
              return ((SelectElement) ans).getItems().size() * PDFUtils.getTextHeight(fontArray[0], 7) * 1.1f;
          }
        }
      }
    }
    return super.questionSize(question, startInx, qFontSize, questionWidth, answerWidth);
  }

  @Override
  public float drawTitleLine3(PDPage page, PatientInfo patientInfo, float y3, float boxMargin) throws IOException {
    patientInfo.getPatient();
    PDFont font = fontArray[0];
    String mrnText = "MRN " + siteInfo.getPatientIdFormatter().printFormat(patientInfo.getMrn());
    String weight = "Weight: " + getWeight(patientInfo.getPatient()) + " lbs";
    String genderText = " Gender " + patientInfo.getGender();
    PDFUtils.writePDFHtml(contentStream, mrnText, XMARGIN + 4f, y3, PDFUtils.pageWidth(page) - boxMargin, fontArray,
        tFontSize, Color.BLACK, Color.white);
    PDFUtils.writePDFHtml(contentStream, weight, page.getMediaBox().getWidth() * .44f, y3,
        PDFUtils.pageWidth(page) - boxMargin, fontArray, tFontSize, Color.BLACK, Color.white);
    float x = page.getMediaBox().getWidth() - boxMargin - ((font.getStringWidth(genderText) / 1000) * tFontSize) - 2;
    y3 = nextY(PDFUtils.writePDFHtml(contentStream, genderText, x, y3, PDFUtils.pageWidth(page) - boxMargin, fontArray,
        tFontSize, Color.BLACK, Color.white));
    return y3;
  }

  @Override
  public float drawTitleLine2(PDPage page, PatientInfo patientInfo, float y2, float boxMargin) throws IOException {
    String ageText = "DOB " + patientInfo.getBirthDt() + " Age " + patientInfo.getAge();
    String height = "Height: " + getHeight(patientInfo.getPatient());
    PDFont font = fontArray[0];
    PDFUtils.writePDFHtml(contentStream, patientInfo.getPatientName(), boxMargin, y2, PDFUtils.pageWidth(page) - boxMargin,
        fontArray, tFontSize, Color.BLACK, Color.white);
    PDFUtils.writePDFHtml(contentStream, height, page.getMediaBox().getWidth() * .44f, y2,
        PDFUtils.pageWidth(page) - boxMargin, fontArray, tFontSize, Color.BLACK, Color.white);
    float x = page.getMediaBox().getWidth() - boxMargin - ((font.getStringWidth(ageText) / 1000) * tFontSize) - 2;
    return nextY(PDFUtils.writePDFHtml(contentStream, ageText, x, y2, PDFUtils.pageWidth(page) - boxMargin, fontArray,
        tFontSize, Color.BLACK, Color.white));
  }


  private String getHeight(Patient patient) {
    if (patient.hasAttribute("Height")) {
      return patient.getAttribute("Height").getDataValue();
    }
    return "";
  }
  private String getWeight(Patient patient) {
    if (patient.hasAttribute("Weight")) {
      return patient.getAttribute("Weight").getDataValue();
    }
    return "";
  }

  private PatientStudyExtendedData getPatientStudyData(Database database, Patient patient, PrintStudy printStudy, String token, Long regId) throws InvalidDataElementException {

    String xmlDocumentString = XMLFileUtils.getInstance(siteInfo).getXML(database, printStudy.getStudyDescription());
    if (xmlDocumentString == null) {
      return null; // we don't need the promis ones, only the xml questionnaires
    }

    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();

      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));

      Document messageDom = db.parse(is);
      org.w3c.dom.Element docElement = messageDom.getDocumentElement();

      if (docElement.getTagName().equals(Constants.FORM)) {

        // unused: NodeList itemsList = messageDom.getElementsByTagName("Items");
        // Convert the XML to string to save it in the db
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        trans.setOutputProperty(OutputKeys.INDENT, "no");
        // create string from xml tree
        StringWriter strWriter = new StringWriter();
        StreamResult result = new StreamResult(strWriter);
        DOMSource source = new DOMSource(messageDom);
        trans.transform(source, result);
        xmlDocumentString = strWriter.toString();

        PatientStudy patStudy = new PatientStudy();
        patStudy.setExternalReferenceId("");
        patStudy.setMetaVersion(0);
        patStudy.setPatientId(patient.getPatientId());
        patStudy.setStudyCode(printStudy.getStudyCode());
        patStudy.setSurveySystemId(printStudy.getSurveySystemId());
        patStudy.setToken(token);
        patStudy.setSurveyRegId(regId);
        patStudy.setOrderNumber(1);

        PatientStudyExtendedData patStudyExtData = new PatientStudyExtendedData(patStudy);
        patStudyExtData.setPatient(patient);
        patStudyExtData.setStudy(printStudy);
        patStudyExtData.setSurveySystemName(printStudy.getSurveySystemName());
        patStudyExtData.setContents(xmlDocumentString);
        return patStudyExtData;
      }
    } catch (Exception e) {
      logger.error(e.toString(), e);
      throw new InvalidDataElementException(e.toString());
    }
    return null;
  }

  private float getRightEdge () {
    PDPage page = new PDPage(PDRectangle.LETTER);
    return PDFUtils.pageWidth(page) - XMARGIN;
  }
}
