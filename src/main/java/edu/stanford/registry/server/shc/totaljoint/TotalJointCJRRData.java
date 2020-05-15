package edu.stanford.registry.server.shc.totaljoint;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.reports.ScoresExportReport;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.SurveyRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

public class TotalJointCJRRData extends ScoresExportReport {

  private static final Logger logger = Logger.getLogger(TotalJointCJRRData.class);
  protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
  public TotalJointCJRRData(Database database, SiteInfo siteInfo) {
    super(database, siteInfo);
  }

  public List<List<Object>> getReportData() {
    logger.debug("Fetching scores report data.");

    // Create the table for the results
    List<List<Object>> reportData = new ArrayList<>();

    // Create the header row
    List<Object> headerRow = new ArrayList<>();
    headerRow.addAll((new PatientData()).getValueNames());
    headerRow.addAll((new ProcedureData("","")).getValueNames());
    headerRow.addAll((new VR12Data()).getValueNames());
    headerRow.addAll((new WomacKneeData("")).getValueNames());
    headerRow.addAll((new UCLAData()).getValueNames());
    headerRow.addAll((new SurveyData()).getValueNames());
    reportData.add(headerRow);

    // Get the assessments to be returned
    List<? extends AssessmentRegistration> assessments = getAssessments();
    logger.debug("Found " + assessments.size() + " assessments.");

    // For each assessment
    for(AssessmentRegistration asmt : assessments) {
      logger.debug("Processsing assessment " + asmt.getAssessmentRegId());
      boolean handled = false;
      String type = asmt.getAssessmentType();
      if (type.startsWith("InitialLeftKnee") || type.startsWith("InitialBiKnee") ||
          type.startsWith("FollowUpLeftKnee") || type.startsWith("FollowUpBiKnee") ) {
        handled = true;
        List<Object> row = new ArrayList<>();
        row.addAll((new PatientData()).getValues(asmt));
        row.addAll((new ProcedureData("K","L")).getValues(asmt));
        row.addAll((new VR12Data()).getValues(asmt));
        row.addAll((new WomacKneeData(TotalJointCustomizer.SIDE_LEFT)).getValues(asmt));
        row.addAll((new UCLAData()).getValues(asmt));
        row.addAll((new SurveyData()).getValues(asmt));
        reportData.add(row);
      }
      if (type.startsWith("InitialRightKnee") || type.startsWith("InitialBiKnee") ||
          type.startsWith("FollowUpRightKnee") || type.startsWith("FollowUpBiKnee") ) {
        handled = true;
        List<Object> row = new ArrayList<>();
        row.addAll((new PatientData()).getValues(asmt));
        row.addAll((new ProcedureData("K","R")).getValues(asmt));
        row.addAll((new VR12Data()).getValues(asmt));
        row.addAll((new WomacKneeData(TotalJointCustomizer.SIDE_RIGHT)).getValues(asmt));
        row.addAll((new UCLAData()).getValues(asmt));
        row.addAll((new SurveyData()).getValues(asmt));
        reportData.add(row);
      }
      if (type.startsWith("InitialLeftHip") || type.startsWith("InitialBiHip") ||
          type.startsWith("FollowUpLeftHip") || type.startsWith("FollowUpBiHip") ) {
        handled = true;
        List<Object> row = new ArrayList<>();
        row.addAll((new PatientData()).getValues(asmt));
        row.addAll((new ProcedureData("H","L")).getValues(asmt));
        row.addAll((new VR12Data()).getValues(asmt));
        row.addAll((new WomacHipData(TotalJointCustomizer.SIDE_LEFT)).getValues(asmt));
        row.addAll((new UCLAData()).getValues(asmt));
        row.addAll((new SurveyData()).getValues(asmt));
        reportData.add(row);
      }
      if (type.startsWith("InitialRightHip") || type.startsWith("InitialBiHip") ||
          type.startsWith("FollowUpRightHip") || type.startsWith("FollowUpBiHip") ) {
        handled = true;
        List<Object> row = new ArrayList<>();
        row.addAll((new PatientData()).getValues(asmt));
        row.addAll((new ProcedureData("H","R")).getValues(asmt));
        row.addAll((new VR12Data()).getValues(asmt));
        row.addAll((new WomacHipData(TotalJointCustomizer.SIDE_RIGHT)).getValues(asmt));
        row.addAll((new UCLAData()).getValues(asmt));
        row.addAll((new SurveyData()).getValues(asmt));
        reportData.add(row);
      }
      if (!handled) {
        logger.error("Ignoring survey type " + type);
      }
    }

    return reportData;
  }

  protected String getXML(AssessmentRegistration asmt, String studyDesc) {
    String xmlString = null;
    PatientStudyExtendedData patStudy = getPatientStudy(asmt, studyDesc);
    if (patStudy != null) {
      xmlString = patStudy.getContents();
    }
    return xmlString;
  }

  protected class PatientData implements ReportColumn {

    public PatientData() {
    }

    public List<String> getValueNames() {
      return Arrays.asList(
          "Record_Type", "Pt_LName", "Pt_FName", "Pt_MName", "Pt_SSN", "Medical_Record_Number",
          "Health_Plan_ID", "Pt_Account_ID", "Pt_Home_Address_Street", "Pt_Home_Address_City", "Pt_Home_Address_State",
          "Pt_Home_Address_Zip", "Pt_Home_Phone_Num", "Pt_Cell_Phone_Num", "Pt_Email_Address", "Pt_DOB",
          "Payer_Type", "Payer_Name", "Payer_ID", "Gender_MF", "Race", "Ethnicity_HispanicLatino"
        );
    }

    public List<Object> getValues(AssessmentRegistration asmt) {
      Object[] values = new Object[getValueNames().size()];
      Patient patient = patientDao.getPatient(asmt.getPatientId());

      values[0] = "S"; // Record_Type
      values[1] = patient.getLastName(); // Pt_LName
      values[2] = patient.getFirstName(); // Pt_FName
      values[3] = null; // Pt_MName
      values[4] = null; // Pt_SSN
      values[5] = getPatientMRN(patient); // Medical_Record_Number
      values[6] = null; // Health_Plan_ID
      values[7] = null; // Pt_Account_ID
      values[8] = null; // Pt_Home_Address_Street
      values[9] = null; // Pt_Home_Address_City
      values[10] = null; // Pt_Home_Address_State
      values[11] = null; // Pt_Home_Address_Zip
      values[12] = null; // Pt_Home_Phone_Num
      values[13] = null; // Pt_Cell_Phone_Num
      values[14] = patient.getEmailAddress(); // Pt_Email_Address
      values[15] = dateFormat.format(patient.getDtBirth()); // Pt_DOB
      values[16] = null; // Payer_Type
      values[17] = null; // Payer_Name
      values[18] = null; // Payer_ID
      values[19] = getPatientGender(patient); // Gender_MF
      values[20] = null; // Race
      values[21] = null; // Ethnicity_HispanicLatino
      return Arrays.asList(values);
    }

    protected String getPatientMRN(Patient patient) {
      String mrn = patient.getPatientId();
      if (mrn != null) {
        mrn = "000000000".substring(mrn.length()) + mrn;
      }
      return mrn;
    }

    protected String getPatientGender(Patient patient) {
      String gender = getPatientAttribute(patient, Constants.ATTRIBUTE_GENDER);
      if ("Male".equals(gender)) {
        return "M";
      } else if ("Female".equals(gender)) {
        return "F";
      } else {
        return null;
      }
    }

    protected String getPatientAttribute(Patient patient, String attribute) {
      PatientAttribute attr = patient.getAttribute(attribute);
      return (attr == null) ? null : attr.getDataValue();
    }
  }

  protected class ProcedureData implements ReportColumn {

    private String joint;
    private String side;

    public ProcedureData(String joint, String side) {
      this.joint = joint;
      this.side = side;
    }

    public List<String> getValueNames() {
      return Arrays.asList(
          "Scheduled_Date_Of_Procedure", "Scheduled_Type_Of_Procedure", "Laterality",
          "Hospital_Name", "Hospital_NPI", "Hospital_Zip"
        );
    }

    public List<Object> getValues(AssessmentRegistration asmt) {
      Object[] values = new Object[getValueNames().size()];
      values[0] = null; // Scheduled_Date_Of_Procedure
      values[1] = joint; // Scheduled_Type_Of_Procedure
      values[2] = side; // Laterality
      values[3] = "Stanford Hospitals and Clinics"; // Hospital_Name
      values[4] = "1003951807"; // Hospital_NPI
      values[5] = "94143-2211"; // Hospital_Zip
      return Arrays.asList(values);
    }
  }

  protected class SF12Data extends ScoreColumn {

    public SF12Data() {
      super("","",false);
    }

    public List<String> getValueNames() {
      return Arrays.asList(
          "SF12_Completed_Flag", "SF12_Date_Survey_Completed", "SF12_Person_Completing_Survey",
          "SF12_Q1", "SF12_Q2A", "SF12_Q2B", "SF12_Q3A", "SF12_Q3B", "SF12_Q4A",
          "SF12_Q4B", "SF12_Q5", "SF12_Q6A", "SF12_Q6B", "SF12_Q6C", "SF12_Q7"
        );
    }

    public List<Object> getValues(AssessmentRegistration asmt) {
      SurveyRegistration reg = asmt.getSurveyReg();
      Object[] values = new Object[getValueNames().size()];
      ActivityDao activityDao = new ActivityDao(database, asmt.getSurveySiteId());
      List<Activity> activities = activityDao.getActivityByToken(reg.getToken(), Constants.ACTIVITY_COMPLETED);

      String xmlString;
      xmlString = getXML(asmt, "VR12");
      if (xmlString != null) {
        values[3]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='GH1']/item[@selected='true']/value"); // SF12_Q1
        values[4]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='PF02']/item[@selected='true']/value"); // SF12_Q2A
        values[5]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='PF04']/item[@selected='true']/value"); // SF12_Q2B
        String q3a = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='RP2']/item[@selected='true']/value"); // SF12_Q3A
        values[6]  = (q3a == null) ? null : (6 - Integer.parseInt(q3a));
        String q3b = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='RP3']/item[@selected='true']/value"); // SF12_Q3B
        values[7]  = (q3b == null) ? null : (6 - Integer.parseInt(q3b));
        String q4a = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='RE2']/item[@selected='true']/value"); // SF12_Q4A
        values[8]  = (q4a == null) ? null : (6 - Integer.parseInt(q4a));
        String q4b = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='RE3']/item[@selected='true']/value"); // SF12_Q4B
        values[9]  = (q4b == null) ? null : (6 - Integer.parseInt(q4b));
        values[10] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='BP2']/item[@selected='true']/value"); // SF12_Q5
        String q6a = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='MH3']/item[@selected='true']/value"); // SF12_Q6A
        values[11] = ((q6a != null) && (Integer.parseInt(q6a) > 2)) ? (Integer.parseInt(q6a) - 1) : q6a;
        String q6b = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='VT2']/item[@selected='true']/value"); // SF12_Q6B
        values[12] = ((q6b != null) && (Integer.parseInt(q6b) > 2)) ? (Integer.parseInt(q6b) - 1) : q6b;
        String q6c = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='MH4']/item[@selected='true']/value"); // SF12_Q6C
        values[13] = ((q6c != null) && (Integer.parseInt(q6c) > 2)) ? (Integer.parseInt(q6c) - 1) : q6c;
        values[14] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='SF2']/item[@selected='true']/value"); // SF12_Q7
      }

      boolean answered = false;
      for(int i=3; i<=14; i++) {
        if (values[i] != null) {
          answered = true;
        }
      }
      if (answered) {
        values[0] = "Y";
        values[1] = dateFormat.format(activities.get(0).getActivityDt());
        values[2] = "P";
      } else {
        values[0] = 'N';
      }
      return Arrays.asList(values);
    }
  }

  protected class VR12Data extends ScoreColumn {

    public VR12Data() {
      super("","",false);
    }

    public List<String> getValueNames() {
      return Arrays.asList(
          "VR12_Completed_Flag", "VR12_Date_Survey_Completed", "VR12_Person_Completing_Survey",
          "VR12_Q1", "VR12_Q2A", "VR12_Q2B", "VR12_Q3A", "VR12_Q3B", "VR12_Q4A",
          "VR12_Q4B", "VR12_Q5", "VR12_Q6A", "VR12_Q6B", "VR12_Q6C", "VR12_Q7"
        );
    }

    public List<Object> getValues(AssessmentRegistration asmt) {
      SurveyRegistration reg = asmt.getSurveyReg();
      Object[] values = new Object[getValueNames().size()];
      ActivityDao activityDao = new ActivityDao(database, asmt.getSurveySiteId());
      List<Activity> activities = activityDao.getActivityByToken(reg.getToken(), Constants.ACTIVITY_COMPLETED);


      String xmlString;
      xmlString = getXML(asmt, "VR12");
      if (xmlString != null) {
        values[3]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='GH1']/item[@selected='true']/value"); // VR12_Q1
        values[4]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='PF02']/item[@selected='true']/value"); // VR12_Q2A
        values[5]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='PF04']/item[@selected='true']/value"); // VR12_Q2B
        values[6]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='RP2']/item[@selected='true']/value"); // VR12_Q3A
        values[7]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='RP3']/item[@selected='true']/value"); // VR12_Q3B
        values[8]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='RE2']/item[@selected='true']/value"); // VR12_Q4A
        values[9]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='RE3']/item[@selected='true']/value"); // VR12_Q4B
        values[10] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='BP2']/item[@selected='true']/value"); // VR12_Q5
        values[11] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='MH3']/item[@selected='true']/value"); // VR12_Q6A
        values[12] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='VT2']/item[@selected='true']/value"); // VR12_Q6B
        values[13] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='MH4']/item[@selected='true']/value"); // VR12_Q6C
        values[14] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='SF2']/item[@selected='true']/value"); // VR12_Q7
      }

      boolean answered = false;
      for(int i=3; i<=14; i++) {
        if (values[i] != null) {
          answered = true;
        }
      }
      if (answered) {
        values[0] = "Y";
        values[1] = dateFormat.format(activities.get(0).getActivityDt());
        values[2] = "P";
      } else {
        values[0] = 'N';
      }
      return Arrays.asList(values);
    }
  }

  protected class WomacKneeData extends ScoreColumn {

    protected String side;

    public WomacKneeData(String side) {
      super("","",false);
      this.side = side;
    }

    public List<String> getValueNames() {
      return Arrays.asList(
          "WOMAC_Completed_Flag", "WOMAC_Date_Survey_Completed", "WOMAC_Person_Completing_Survey",
          "WOMAC_Q1", "WOMAC_Q2", "WOMAC_Q3", "WOMAC_Q4", "WOMAC_Q5",
          "WOMAC_Q6", "WOMAC_Q7", "WOMAC_Q8", "WOMAC_Q9", "WOMAC_Q10",
          "WOMAC_Q11", "WOMAC_Q12", "WOMAC_Q13", "WOMAC_Q14", "WOMAC_Q15",
          "WOMAC_Q16", "WOMAC_Q17", "WOMAC_Q18", "WOMAC_Q19", "WOMAC_Q20",
          "WOMAC_Q21", "WOMAC_Q22", "WOMAC_Q23", "WOMAC_Q24"
        );
    }

    public List<Object> getValues(AssessmentRegistration asmt) {
      SurveyRegistration reg = asmt.getSurveyReg();
      Object[] values = new Object[getValueNames().size()];
      ActivityDao activityDao = new ActivityDao(database, asmt.getSurveySiteId());
      List<Activity> activities = activityDao.getActivityByToken(reg.getToken(), Constants.ACTIVITY_COMPLETED);

      String xmlString;
      xmlString = getXML(asmt, side + "KOOSKneePain");
      if (xmlString != null) {
        values[3] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P5']/item[@selected='true']/value"); // WOMAC_Q1
        values[4] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P6']/item[@selected='true']/value"); // WOMAC_Q2
        values[5] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P7']/item[@selected='true']/value"); // WOMAC_Q3
        values[6] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P8']/item[@selected='true']/value"); // WOMAC_Q4
        values[7] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P9']/item[@selected='true']/value"); // WOMAC_Q5
      }
      xmlString = getXML(asmt, side + "KOOSKneeSymptoms");
      if (xmlString != null) {
        values[8] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S6']/item[@selected='true']/value"); // WOMAC_Q6
        values[9] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S7']/item[@selected='true']/value"); // WOMAC_Q7
      }
      xmlString = getXML(asmt, side + "KOOSKneeFunction");
      if (xmlString != null) {
        values[10] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A1']/item[@selected='true']/value"); // WOMAC_Q8
        values[11] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A2']/item[@selected='true']/value"); // WOMAC_Q9
        values[12] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A3']/item[@selected='true']/value"); // WOMAC_Q10
        values[13] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A4']/item[@selected='true']/value"); // WOMAC_Q11
        values[14] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A5']/item[@selected='true']/value"); // WOMAC_Q12
        values[15] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A6']/item[@selected='true']/value"); // WOMAC_Q13
        values[16] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A7']/item[@selected='true']/value"); // WOMAC_Q14
        values[17] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A8']/item[@selected='true']/value"); // WOMAC_Q15
        values[18] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A9']/item[@selected='true']/value"); // WOMAC_Q16
        values[19] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A10']/item[@selected='true']/value"); // WOMAC_Q17
        values[20] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A11']/item[@selected='true']/value"); // WOMAC_Q18
        values[21] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A12']/item[@selected='true']/value"); // WOMAC_Q19
        values[22] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A14']/item[@selected='true']/value"); // WOMAC_Q20 <- A14 Order switched
        values[23] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A13']/item[@selected='true']/value"); // WOMAC_Q21 <- A13
        values[24] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A15']/item[@selected='true']/value"); // WOMAC_Q22
        values[25] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A16']/item[@selected='true']/value"); // WOMAC_Q23
        values[26] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A17']/item[@selected='true']/value"); // WOMAC_Q24
      }

      boolean answered = false;
      for(int i=3; i<=26; i++) {
        if (values[i] != null) {
          answered = true;
        }
      }
      if (answered) {
        values[0] = "Y";
        values[1] = dateFormat.format(activities.get(0).getActivityDt());
        values[2] = "P";
      } else {
        values[0] = 'N';
      }
      return Arrays.asList(values);
    }
  }

  protected class WomacHipData extends ScoreColumn {

    protected String side;

    public WomacHipData(String side) {
      super("","",false);
      this.side = side;
    }

    public List<String> getValueNames() {
      return Arrays.asList(
          "WOMAC_Completed_Flag", "WOMAC_Date_Survey_Completed", "WOMAC_Person_Completing_Survey",
          "WOMAC_Q1", "WOMAC_Q2", "WOMAC_Q3", "WOMAC_Q4", "WOMAC_Q5",
          "WOMAC_Q6", "WOMAC_Q7", "WOMAC_Q8", "WOMAC_Q9", "WOMAC_Q10",
          "WOMAC_Q11", "WOMAC_Q12", "WOMAC_Q13", "WOMAC_Q14", "WOMAC_Q15",
          "WOMAC_Q16", "WOMAC_Q17", "WOMAC_Q18", "WOMAC_Q19", "WOMAC_Q20",
          "WOMAC_Q21", "WOMAC_Q22", "WOMAC_Q23", "WOMAC_Q24"
        );
    }

    public List<Object> getValues(AssessmentRegistration asmt) {
      SurveyRegistration reg = asmt.getSurveyReg();
      Object[] values = new Object[getValueNames().size()];
      ActivityDao activityDao = new ActivityDao(database, asmt.getSurveySiteId());
      List<Activity> activities = activityDao.getActivityByToken(reg.getToken(), Constants.ACTIVITY_COMPLETED);

      String xmlString;
      xmlString = getXML(asmt, side + "HOOSHipPain");
      if (xmlString != null) {
        values[3] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P4']/item[@selected='true']/value"); // WOMAC_Q1
        values[4] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P5']/item[@selected='true']/value"); // WOMAC_Q2
        values[5] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P6']/item[@selected='true']/value"); // WOMAC_Q3
        values[6] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P7']/item[@selected='true']/value"); // WOMAC_Q4
        values[7] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P8']/item[@selected='true']/value"); // WOMAC_Q5
      }
      xmlString = getXML(asmt, side + "HOOSHipSymptoms");
      if (xmlString != null) {
        values[8] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S4']/item[@selected='true']/value"); // WOMAC_Q6
        values[9] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S5']/item[@selected='true']/value"); // WOMAC_Q7
      }
      xmlString = getXML(asmt, side + "HOOSHipFunction");
      if (xmlString != null) {
        values[10] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A1']/item[@selected='true']/value"); // WOMAC_Q8
        values[11] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A2']/item[@selected='true']/value"); // WOMAC_Q9
        values[12] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A3']/item[@selected='true']/value"); // WOMAC_Q10
        values[13] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A4']/item[@selected='true']/value"); // WOMAC_Q11
        values[14] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A5']/item[@selected='true']/value"); // WOMAC_Q12
        values[15] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A6']/item[@selected='true']/value"); // WOMAC_Q13
        values[16] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A7']/item[@selected='true']/value"); // WOMAC_Q14
        values[17] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A8']/item[@selected='true']/value"); // WOMAC_Q15
        values[18] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A9']/item[@selected='true']/value"); // WOMAC_Q16
        values[19] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A10']/item[@selected='true']/value"); // WOMAC_Q17
        values[20] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A11']/item[@selected='true']/value"); // WOMAC_Q18
        values[21] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A12']/item[@selected='true']/value"); // WOMAC_Q19
        values[22] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A14']/item[@selected='true']/value"); // WOMAC_Q20 <- A14 Order switched
        values[23] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A13']/item[@selected='true']/value"); // WOMAC_Q21 <- A13
        values[24] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A15']/item[@selected='true']/value"); // WOMAC_Q22
        values[25] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A16']/item[@selected='true']/value"); // WOMAC_Q23
        values[26] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A17']/item[@selected='true']/value"); // WOMAC_Q24
      }

      boolean answered = false;
      for(int i=3; i<=26; i++) {
        if (values[i] != null) {
          answered = true;
        }
      }
      if (answered) {
        values[0] = "Y";
        values[1] = dateFormat.format(activities.get(0).getActivityDt());
        values[2] = "P";
      } else {
        values[0] = 'N';
      }
      return Arrays.asList(values);
    }

  }

  protected class UCLAData extends ScoreColumn {

    public UCLAData() {
      super("","",false);
    }

    public List<String> getValueNames() {
      return Arrays.asList(
          "UCLA_Completed_Flag", "UCLA_Date_Survey_Completed", "UCLA_Person_Completing_Survey",
          "UCLA_Q1"
        );
    }

    public List<Object> getValues(AssessmentRegistration asmt) {
      SurveyRegistration reg = asmt.getSurveyReg();
      Object[] values = new Object[getValueNames().size()];
      ActivityDao activityDao = new ActivityDao(database, asmt.getSurveySiteId());
      List<Activity> activities = activityDao.getActivityByToken(reg.getToken(), Constants.ACTIVITY_COMPLETED);

      String xmlString;
      xmlString = getXML(asmt, "UCLAActivityScore");
      if (xmlString != null) {
        values[3] = XMLFileUtils.xPathQuery(xmlString, "//Response[@Order='1']/item[@selected='true']/value"); // UCLA_Q1
      }

      boolean answered = false;
      for(int i=3; i<=3; i++) {
        if (values[i] != null) {
          answered = true;
        }
      }
      if (answered) {
        values[0] = "Y";
        values[1] = dateFormat.format(activities.get(0).getActivityDt());
        values[2] = "P";
      } else {
        values[0] = 'N';
      }
      return Arrays.asList(values);
    }
  }

  protected class SurveyData implements ReportColumn {

    public SurveyData() {
    }

    public List<String> getValueNames() {
      return Arrays.asList(
          "Survey_Token", "Survey_Type", "Survey_Date"
        );
    }

    public List<Object> getValues(AssessmentRegistration asmt) {
      SurveyRegistration reg = asmt.getSurveyReg();
      Object[] values = new Object[getValueNames().size()];
      values[0] = reg.getToken();
      values[1] = reg.getSurveyType();
      ActivityDao activityDao = new ActivityDao(database, asmt.getSurveySiteId());
      List<Activity> activities = activityDao.getActivityByToken(reg.getToken(), Constants.ACTIVITY_COMPLETED);

      values[2] = dateFormat.format(activities.get(0).getActivityDt());
      return Arrays.asList(values);
    }
  }
}
