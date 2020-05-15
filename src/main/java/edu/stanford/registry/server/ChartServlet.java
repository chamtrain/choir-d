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
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.ReportUtils;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ResultAction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

public class ChartServlet extends HttpServlet {

  private static final long serialVersionUID = -4960853435336228469L;
  private static final Integer DEFAULT_WIDTH = 450;
  private static final Integer DEFAULT_HEIGHT = 150;
  private static final Integer DEFAULT_SPACER = 20;
  private static final Logger logger = LoggerFactory.getLogger(ChartServlet.class);

  private final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
  private static final int TIME_REPORT = 0;
  private static final int TIME_REPORT_BY_MONTH = 1;
  private static final int TIME_REPORT_BY_TYPE = 2;

  @Override
  public void init() throws ServletException {
    super.init();
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse res) 
      throws ServletException, IOException {
    RegistryServletRequest regRequest = (RegistryServletRequest) req;
    SiteInfo siteInfo = regRequest.getSiteInfo();
    ClinicServices service = (ClinicServices) regRequest.getService();
    if (service == null) {
      logger.error("NO SERVICE IN REQUEST!");
      res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    String rpt = req.getParameter("rpt");
    try {
      if (ServerUtils.isEmpty(rpt)) {
        doScoresReport(service, siteInfo, req, res);
      } else if (rpt.equals("cr1")) {
        doComplianceReport1(service, siteInfo, res);
      } else if (rpt.equals("registration")) {
        doRegistrationReport(service, siteInfo, req, res);
      } else if (rpt.equals("visits")) {
        doVisitsReport(service, siteInfo, req, res);
      } else if (rpt.equals("AverageSurveyTimeByMonth")) {
        doSurveyTimeReport(service, siteInfo, req, res, TIME_REPORT_BY_MONTH);
      } else if (rpt.equals("AverageSurveyTimeByType")) {
        doSurveyTimeReport(service, siteInfo, req, res,TIME_REPORT_BY_TYPE);
      } else if (rpt.equals("AverageSurveyTime")) {
        doSurveyTimeReport(service, siteInfo, req, res, TIME_REPORT);
      } else if (rpt.equals("test")) {
        doTest(service, siteInfo, req, res);
      } else if (rpt.equals("expsq")) {
        doExportSquareTable(service, siteInfo, req, res);
      } else if (rpt.equals("PatientSurveysReport")) {
        doPatientSurveysReport(service, siteInfo, req, res);
      } else {
        doOtherReport(service, req, res);
      }
    } catch (InvalidParameterException e) {
      logger.warn(e.getMessage());
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());        
    } catch (Exception e) {
      logger.error("Error generating report", e);
      res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void doScoresReport(ClinicServices service, SiteInfo siteInfo, HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {    
    logger.debug("Doing scores report");

    String assessmentId = req.getParameter(Constants.ASSESSMENT_ID);
    String patientId = req.getParameter("patient");
    String study = req.getParameter("study");

    boolean print = getBooleanParam(req, "print", false);
    boolean txt = getBooleanParam(req, "txt", false);
    boolean json = getBooleanParam(req, "json", false);

    Integer height = getIntegerParam(req, "height", DEFAULT_HEIGHT);
    Integer width = getIntegerParam(req, "width", DEFAULT_WIDTH);
    Integer gap = getIntegerParam(req, "gap", DEFAULT_SPACER);

    RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
    ChartConfigurationOptions opts = customizer.getConfigurationOptions();
    opts.setHeight(height);
    opts.setWidth(width);
    opts.setGap(gap);

    ResultAction action = (print ? ResultAction.print : ResultAction.view);

    if (!ServerUtils.isEmpty(study)) {
      // Send back the chart for one study as an image
      Integer studyCode = getIntegerParam(req, "study", 0);
      sendImage(service, req, res, studyCode, opts);
    } else if (!ServerUtils.isEmpty(assessmentId)) {
      if (assessmentId.equals(Constants.ASSESSMENT_ID_LIST)) {
        // Send back a pdf for a list of assessmentIds
        action.setPrefix(null);
        sendPdfIdList(service, req, res, opts, action);                      
      } else if (!isValidAssessmentId(service, assessmentId)) {
        // assessmentId is not valid
        throw new InvalidParameterException(Constants.ASSESSMENT_ID, assessmentId);
      } else if (txt) {
        // Send back text for the assessmentId
        action.setPrefix('T');
        sendTextID(service, siteInfo, req, res, assessmentId, opts, action);
      } else if (json) {
        // Send back json for the assessmentId
        action.setPrefix('J');
        sendJsonID(service, req, res, assessmentId, opts, action);
      } else {
        // Send back a pdf for the assessmentId
        action.setPrefix(null);
        sendPdfID(service, req, res, assessmentId, opts, action);
      }
    } else if (!ServerUtils.isEmpty(patientId)) {
      if (!isValidPatientId(service, patientId)) {
        // patientId is not valid
        throw new InvalidParameterException("patient", patientId);
      } else {
        // Get charts of all the studies for this patient and write as a pdf
        action.setPrefix(null);
        sendPdf(service, req, res, patientId, opts, action);
      }
    } else {
      String msg = "ChartServlet did not receive either patient or asssessmentId parameter.";
      logger.warn(msg);
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
    }
  }

  /**
   * Sends a single jpg image of a chart for the study identified back to the
   * client
   */
  private void sendImage(ClinicServices service, HttpServletRequest req, HttpServletResponse res, Integer studyCode,
      ChartConfigurationOptions opts) throws IOException {
    ArrayList<PatientStudyExtendedData> patientStudies = getPatientStudies(req);
    if (patientStudies == null) {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "no patient studies for study " + studyCode);
    }

    ArrayList<PatientStudyExtendedData> theseStudies = new ArrayList<>();
    for (PatientStudyExtendedData patientStudy : patientStudies) {
      if (patientStudy != null && patientStudy.getStudyCode() != null
          && patientStudy.getStudyCode().intValue() == studyCode) {
        theseStudies.add(patientStudy);
      }
    }
    res.setContentType("img/png");
    res.getOutputStream().write(service.makePng(theseStudies, opts));
    res.getOutputStream().close();
  }

  /**
   * Returns a pdf of all the charts for the patient formatted for printing.
   */
  private void sendPdfIdList(ClinicServices service, HttpServletRequest req, HttpServletResponse res,
      ChartConfigurationOptions opts, ResultAction action) throws IOException, ServletException {
    // Process a list of id's
    logger.debug("Processing " + Constants.ASSESSMENT_ID_LIST );

    ArrayList<Long> regIds = getAssessmentIds(req);
    if (regIds == null  || regIds.size() < 1) {
      logger.error("No assessmentId value in session");
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "No assessmentId value in session");
      return;
    }

    PDDocument pdf = new PDDocument();
    ArrayList<PDDocument> pdfList = new ArrayList<>();
    for (Long regId : regIds) {
      AssessmentId assessmentId = new AssessmentId(regId);
      PDDocument thisPdf = service.getPdf(assessmentId, opts, action);
      if (thisPdf != null) {
        @SuppressWarnings("unchecked")
        PDPageTree pagesTree = thisPdf.getDocumentCatalog().getPages();
        List<PDPage> list = new ArrayList<>();
        pagesTree.forEach(list::add);
        for (PDPage page : list) {
          pdf.addPage(page);
        }
        pdfList.add(thisPdf);
      }
    }
    returnPdf(req, res, pdf, action);

    // Now we can close the individual pdfs
    for (PDDocument aPdfList : pdfList) {
      aPdfList.close();
    }
  }

  /**
   * Return pdf for a single survey_reg_id
   */
  private void sendPdfID(ClinicServices service, HttpServletRequest req, HttpServletResponse res, String assessmentIdStr,
      ChartConfigurationOptions opts, ResultAction action) throws IOException, ServletException {
    logger.debug("sendPdfID starting for id " + assessmentIdStr);
    AssessmentId assessmentId = new AssessmentId(Long.valueOf(assessmentIdStr));

    if (getBooleanParam(req,"rrr",false)) {
      /* not asking for this survey, want to R[eprint] R[ecent] R[esults] */
      logger.debug("calling service.getPdfBefore(" + assessmentId );
      PDDocument pdf = service.getPdfBefore(assessmentId, opts, action);
      if (pdf == null) {
        pdf = new PDDocument();
      }
      returnPdf(req, res, pdf, action);
    } else {
      logger.debug("calling service.getPdf(" + assessmentId );
      PDDocument pdf = service.getPdf(assessmentId, opts, action);
      returnPdf(req, res, pdf, action);
    }
  }

  private void sendPdf(ClinicServices service, HttpServletRequest req, HttpServletResponse res, String patientId,
      ChartConfigurationOptions opts, ResultAction action) throws IOException, ServletException {
    PDDocument pdf = service.getPdf(patientId, opts, action);
    returnPdf(req, res, pdf, action);
  }

  private void returnPdf(HttpServletRequest req, HttpServletResponse res, PDDocument pdf,
      ResultAction action ) throws IOException, ServletException {
    // Save the pdf and send it to the client
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // remove the no-cache and cache control headers to send the pdf.
    res.reset();
    try {
      if (action.equals(ResultAction.print)) {
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

  private void sendTextID(ClinicServices service, SiteInfo siteInfo, HttpServletRequest req, HttpServletResponse res,
      String assessmentIdStr, ChartConfigurationOptions opts, ResultAction action) throws IOException {
    PrintWriter out = null;
    try {

      AssessmentId assessmentId = new AssessmentId(Long.valueOf(assessmentIdStr));
      AssessmentRegistration ar = service.getAssessmentRegistration(assessmentId);
      Patient patient = service.getPatient(ar.getPatientId());
      ArrayList<String> stringArr = service.getText(assessmentId, opts, action);

      // remove the no-cache and cache control headers to send the pdf.
      res.reset();
      res.setContentType("text/html;charset=UTF-8");
      res.setCharacterEncoding("UTF-8");
      out = res.getWriter();
      // Add header if configured for patient identification
      RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
      String closingString = "";
      if ("1".equals(customizer.getClientConfig().getParam("patientIdentificationViewVs"))) {
        addStyle(out);
        out.println("<table><tr height='68px'><td>");
        createPatientIdentificationHeader(patient, out);
        out.println("</td></tr><tr height='9px'><td></td></tr><tr><td>");
        closingString = "</td></tr></table>";
      }
      int rows = (stringArr.size() < 60) ? stringArr.size() : 60;
      out.println("<textarea rows=\"" + rows + "\" cols=\"175\" onFocus=\"this.select()\">");
      for (String line : stringArr) {
        out.println(line);
      }
      out.println("</textarea>");
      out.println(closingString);
    } catch (Exception e) {
      logger.error("Problem creating text for assessment id: " + assessmentIdStr, e);
      out.println("Unable to send text for assessment id: " + assessmentIdStr);
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  private void sendJsonID(ClinicServices service, HttpServletRequest req, HttpServletResponse res, String assessmentIdStr,
      ChartConfigurationOptions opts, ResultAction action ) throws IOException, ServletException {
    PrintWriter out = null;
    try {
      AssessmentId assessmentId = new AssessmentId(Long.valueOf(assessmentIdStr));
      JSONObject jsonObj = service.getJSON(assessmentId, opts, action);

      // remove the no-cache and cache control headers to send the json.
      res.reset();
      res.setContentType("application/json");
      out = res.getWriter();
      out.println(jsonObj.toString());
    } catch (Exception e) {
      logger.error("Problem creating json for assessment id: " + assessmentIdStr, e);
      out.println(e.toString());
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  @SuppressWarnings("unchecked")
  private ArrayList<PatientStudyExtendedData> getPatientStudies(HttpServletRequest req) {
    ArrayList<PatientStudyExtendedData> patientStudies = null;
    HttpSession session = req.getSession();
    if (session == null) {
      logger.error("Session is null");
    } else {
      patientStudies = (ArrayList<PatientStudyExtendedData>) session.getAttribute("patientStudyData");
      if (patientStudies == null) {
        logger.error("patients is null");
      }
    }
    return patientStudies;
  }

  @SuppressWarnings("unchecked")
  private ArrayList<Long> getAssessmentIds(HttpServletRequest req) {
    ArrayList<Long> assessmentIds = null;
    HttpSession session = req.getSession();
    if (session == null) {
      logger.error("Session is null");
    } else {
      assessmentIds = (ArrayList<Long>) session.getAttribute(Constants.ASSESSMENT_ID_LIST);
      if (assessmentIds == null) {
        logger.error("assessmentRegIdList returned null");
      }
    }
    return assessmentIds;
  }

  private void doComplianceReport1(ClinicServices service, SiteInfo siteInfo, HttpServletResponse res) throws IOException, ServletException {
    ArrayList<ArrayList<Object>> reportData = service.complianceReport1();
    // Save the pdf and send it to the client
    if (reportData == null) {
      logger.debug("reportData is null");
    } else {
      logger.debug("reportData has " + reportData.size() + " rows");
    }
    String templateName = "Compliance";
    ReportUtils reportWriter = new ReportUtils(siteInfo);
    XSSFWorkbook wb = reportWriter.writeXlsx(reportData, templateName, "Registration", 0);

    res.reset(); // clear any no-cache or cache-control headers
    res.setHeader("Content-Disposition", "inline; filename=\"" + templateName + "\"");
    res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    wb.write(res.getOutputStream());
  }

  private void doRegistrationReport(ClinicServices service, SiteInfo siteInfo, HttpServletRequest req, HttpServletResponse res) throws ServletException,
  IOException, DataException, ParseException {
    Date from = getDateParam(req, "startDt");
    Date to = getDateParam(req, "endDt");

    String templateName = "PatientRegistration.xlsx";
    ArrayList<ArrayList<Object>> reportData = service.registrationReportData(from, to);
    if (reportData == null) {
      logger.debug("reportData is null");
    } else {
      logger.debug("reportData has " + reportData.size() + " rows");
    }
    ReportUtils reportWriter = new ReportUtils(siteInfo);
    XSSFWorkbook wb = reportWriter.writeXlsx(reportData, templateName, "Registration", 0);

    res.reset(); // clear any no-cache or cache-control headers
    res.setHeader("Content-Disposition", "inline; filename=\"" + templateName + "\"");
    res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    wb.write(res.getOutputStream());
    res.getOutputStream().close();

  }
  private void doVisitsReport(ClinicServices service, SiteInfo siteInfo,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException,
  DataException, ParseException {
    Date from = getDateParam(req, "startDt");
    Date to = getDateParam(req, "endDt");

    sendReport(service, siteInfo, req, res, "PatientVisits.xlsx", "Visits", 0, service.visitsReportData(from, to));
  }

  private void doSurveyTimeReport(ClinicServices service, SiteInfo siteInfo, HttpServletRequest req, HttpServletResponse res, int type)
      throws ServletException, IOException, DataException, ParseException {
    Date from = getDateParam(req, "startDt");
    Date to = getDateParam(req, "endDt");

    logger.debug("surveyTimeReport running for :" + from.toString() + " endDt:" + to.toString());
    switch (type) {
    case TIME_REPORT_BY_MONTH:
      sendReport(service, siteInfo, req, res, "SurveyTimeByMonth.xlsx", "AverageSurveyTime", 1, service.averageSurveyTimeReportByMonth(from, to));
      break;
    case TIME_REPORT_BY_TYPE:
      sendReport(service, siteInfo, req, res, "SurveyTimeByType.xlsx", "AverageSurveyTime", 1, service.averageSurveyTimeReportByType(from, to));
      break;
    default:
      sendReport(service, siteInfo, req, res, "SurveyTime.xlsx", "AverageSurveyTime", 1, service.averageSurveyTimeReport(from, to));
    }
  }

  private void doPatientSurveysReport(ClinicServices service, SiteInfo siteInfo, HttpServletRequest req, HttpServletResponse res)
      throws IOException, DataException, ServletException, ParseException {
    Date from = getDateParam(req, "startDt");
    Date to = getDateParam(req, "endDt");
    logger.debug("patientSurveysReport running for :" + from.toString() + " endDt:" + to.toString());
    sendReport(service, siteInfo, req, res, "PatientSurveys.xlsx", "PatientSurveys", 2,
        service.patientSurveysReport(from, to));
  }

  private void doExportSquareTable(ClinicServices service, SiteInfo siteInfo, HttpServletRequest req, HttpServletResponse res)
      throws ServletException, ParseException, IOException {
    String tableName = req.getParameter("table");
    Date from = null;
    Date to = null;

    if (ServerUtils.isEmpty(req.getParameter("startDt"))) {
      from = getDateParam(req, "startDt");
    }
    if (ServerUtils.isEmpty(req.getParameter("endDt"))) {
      to = getDateParam(req, "endDt");
    }

    if (from != null && to != null) {
      sendReport(service, siteInfo, req, res, tableName + ".xlsx", tableName, 0, service.exportSquareTable(tableName, from, to));
    }
    sendReport(service, siteInfo, req, res, tableName + ".xlsx", tableName, 0, service.exportSquareTable(tableName));
  }

  private void doOtherReport(ClinicServices service, HttpServletRequest req, HttpServletResponse res)
      throws DataException {
    Date from = getDateParam(req, "startDt");
    Date to = getDateParam(req, "endDt");
    String rpt = req.getParameter("rpt");
    logger.debug("doOtherReport running report {} for :{} through {}", rpt,  from.toString(), to.toString());
    ArrayList<ArrayList<Object>> report = service.standardReport(rpt, from, to);
    if (report == null) {
      throw new InvalidParameterException("rpt", rpt);
    }
    sendCsv(res, rpt, report);
  }

  private void sendCsv(HttpServletResponse res, String reportName, ArrayList<ArrayList<Object>> reportData) {
    res.reset();
    res.setContentType("text/csv");
    res.setHeader("Content-Disposition", "attachment; filename=" + reportName + ".csv");
    try {
      CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(res.getOutputStream(), StandardCharsets.UTF_8));
      for(List<Object> lineData : reportData) {
        String[] line = new String[lineData.size()];
        for(int i=0; i<line.length; i++) {
          Object value = lineData.get(i);
          if (value == null) {
            line[i] = null;
          } else if (value instanceof String) {
            line[i] = (String) value;
          } else {
            line[i] = value.toString();
          }
        }
        csvWriter.writeNext(line);
      }
      // Flush the CSVWriter to the underlying OutputStream but
      csvWriter.flush();
      res.getOutputStream().close();
    }
    catch(IOException ioe) {
      logger.error("Exception in sendCsv for resport {}", reportName, ioe);
    }
  }

  private void sendReport(ClinicServices service, SiteInfo siteInfo, HttpServletRequest req, HttpServletResponse res, String templateName, String tabName,
      int skipRows, ArrayList<ArrayList<Object>> reportData) throws ServletException, IOException, DataException,
      ParseException {

    ReportUtils reportWriter = new ReportUtils(siteInfo);
    XSSFWorkbook wb = reportWriter.writeXlsx(reportData, templateName, tabName, skipRows);
    if (wb == null) {
      String id = siteInfo.getIdString();
      res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, id+"There were no reports to download. Go back to the previous page");
      return; // nothing to report
    }

    res.reset(); // clear any no-cache or cache-control headers
    res.setHeader("Content-Disposition", "inline; filename=\"" + templateName + "\"");
    res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    wb.write(res.getOutputStream());
    res.getOutputStream().close();
  }

  private void doTest(ClinicServices service, SiteInfo siteInfo, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    TestRunner testRunner = new TestRunner(siteInfo);
    testRunner.service(req, res, service);
  }

  private boolean isValidPatientId(ClinicServices service, String patientId) {
    Patient patient = service.getPatient(patientId);
    return (patient != null);
  }

  private boolean isValidAssessmentId(ClinicServices service, String assessmentIdStr) {
    AssessmentId regId = null;
    try {
      regId = new AssessmentId(Long.valueOf(assessmentIdStr));
    } catch (NumberFormatException e) {
      return false;
    }
    AssessmentRegistration registration = service.getAssessmentRegistration(regId);
    return (registration != null);
  }

  private Integer getIntegerParam(HttpServletRequest req, String paramName, Integer dflt) {
    String value = req.getParameter(paramName);
    if (ServerUtils.isEmpty(value)) {
      return dflt;
    }
    
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException e) {
      throw new InvalidParameterException(paramName, value);
    }
  }

  private boolean getBooleanParam(HttpServletRequest req, String paramName, boolean dflt) {
    String value = req.getParameter(paramName);
    if (ServerUtils.isEmpty(value)) {
      return dflt;
    }
    if (value.equalsIgnoreCase("y")) {
      return true;
    }
    if (value.equalsIgnoreCase("n")) {
      return false;
    }
    throw new InvalidParameterException(paramName, value);
  }

  private Date getDateParam(HttpServletRequest req, String paramName) {
    String value = req.getParameter(paramName);
    if (ServerUtils.isEmpty(value)) {
      throw new InvalidParameterException(paramName, null);
    }

    String fmt = req.getParameter("fmt");
    if (ServerUtils.isEmpty(fmt)) {
      fmt = "MM/dd/yyyy";
    }

    try {
      SimpleDateFormat sdf = new SimpleDateFormat(fmt);
      return sdf.parse(value);
    } catch (IllegalArgumentException e) {
      throw new InvalidParameterException("fmt",fmt);
    } catch (ParseException e) {
      throw new InvalidParameterException(paramName, value);
    }
  }

  static private class InvalidParameterException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidParameterException(String param, String value) {
      super("Invalid value for ChartServlet parameter " + param + ", value is " + value);
    }
  }

  private void createPatientIdentificationHeader(Patient pat, PrintWriter out) {
    if (pat == null) {
      return;
    }
    out.write("<table cellspacing='0' cellpadding='0' class='patientIdentificationPanel patientIdentificationLarge patientHeader' style='width: 865px; '>");
    out.write("<tbody><tr><td align='left' style='vertical-align: top;'><table style='width: 100%; height: 2.8em;'><colgroup><col></colgroup><tbody><tr>");
    out.write("<td width='25%' class='patientIdentification' style='vertical-align: middle;'><table cellspacing='0' cellpadding='0' class='patientIdentification'>");
    out.write("<tbody><tr><td align='left' style='vertical-align: top;'><div class='gwt-Label patientIdLabel patientIdLabelLarge'>");
    if (pat.getLastName() != null && pat.getFirstName() != null) {
      String lnf = pat.getLastName() + ", " + pat.getFirstName();
      out.write(lnf);
    }
    out.write("</div></td></tr></tbody></table></td><td width='25%' class='patientIdentification' style='vertical-align: middle;'>");
    out.write("<table cellspacing='0' cellpadding='0' class='patientIdentification'><tbody><tr>");
    out.write("<td align='left' style='vertical-align: top;'><div class='gwt-Label patientIdLabel patientIdLabelLarge'>");
    out.write("MRN:</div></td><td align='left' style='vertical-align: top;'><div class='gwt-Label patientIdLabel patientIdLabelLarge'>");
    out.write(pat.getPatientId());
    out.write("</div></td></tr></tbody></table></td><td width='25%' class='patientIdentification' style='vertical-align: middle;'>");
    out.write("<table cellspacing='0' cellpadding='0' class='patientIdentification'><tbody><tr>");
    out.write("<td align='left' style='vertical-align: top;'><div class='gwt-Label patientIdLabel patientIdLabelLarge'>");
    out.write("Age:</div></td><td align='left' style='vertical-align: top;'><div class='gwt-Label patientIdLabel patientIdLabelLarge'>");
    if (pat.getDtBirth() != null) {
      int age = DateUtils.getAge(pat.getDtBirth());
      if (age > 0) {
        out.write(age+"");
      } else {
        out.write("NA");
      }
    }
    out.write("</div></td></tr></tbody></table></td><td width='25%' class='patientIdentification' style='vertical-align: middle;'>");
    out.write("<table cellspacing='0' cellpadding='0' class='patientIdentification'><tbody><tr>");
    out.write("<td align='left' style='vertical-align: top;'><div class='gwt-Label patientIdLabel patientIdLabelLarge'>");
    out.write("DOB:</div></td><td align='left' style='vertical-align: top;'><div class='gwt-Label patientIdLabel patientIdLabelLarge'>");
    if (pat.getDtBirth() != null) {
      Date dob = new Date(pat.getDtBirth().getTime());
      out.write(formatter.format(dob));
    }
    out.write("</div></td></tr></tbody></table></td><td width='25%' class='patientIdentification' style='vertical-align: middle;'>");
    out.write("<table cellspacing='0' cellpadding='0' class='patientIdentification'><tbody><tr>");
    out.write("<td align='left' style='vertical-align: top;'><div class='gwt-Label patientIdLabel patientIdLabelLarge'>");
    out.write("</div></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table>");
  }

  private void addStyle (PrintWriter out) {
    out.println("<head><style>");
    out.println(".patientIdentificationPanel { border: 3px solid #690c0c; color: #690c0c; }");
    out.println(".patientIdentificationPanel td { color: #690c0c; font-weight: bold; }");
    out.println(".patientIdentification {  margin-left: 10px; }");
    out.println(" .patientIdentificationLarge { border: 7px solid #690c0c; }");
    out.println(".patientIdLabel { text-align: center; padding: 4px 2px 0 0; vertical-align: middle; font: normal 1.8em; }");
    out.println(".patientIdLabelLarge { font-size: 120%; vertical-align: middle; }");
    out.println(".patientReportHeader { padding: 12px; margin-top: 12px; vertical-align: middle; background: lightgray; color: #333; }");
    out.println("</style></head>");
  }
}
