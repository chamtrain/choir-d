package edu.stanford.registry.server.shc.totaljoint;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.server.SurveyComplete;
import edu.stanford.survey.server.SurveyCompleteHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.SqlUpdate;

public class TotalJointCompletionHandler implements SurveyCompleteHandler {

  private static final Logger logger = Logger.getLogger(TotalJointCompletionHandler.class);

  SiteInfo siteInfo;
  public TotalJointCompletionHandler(SiteInfo siteInfo) {
      this.siteInfo = siteInfo;
  }

  public boolean surveyCompleted(SurveyComplete survey, Supplier<Database> dbp) {
    logger.info("TotalJointCompletionHandler invoked for survey token id: " + survey.getSurveyTokenId());

    // Get the survey_reg_id from the survey_token_id
    String sql = "select sr.survey_reg_id from survey_token st, survey_registration sr " +
        "where st.survey_token_id = ? and sr.survey_site_id = st.survey_site_id and sr.token = st.survey_token";
    Long regId = dbp.get().toSelect(sql).argLong(survey.getSurveyTokenId()).queryLongOrNull();
    if (regId == null) {
      throw new RuntimeException("Unable to get survey_reg_id from survey_token_id: " + survey.getSurveyTokenId());
    }

    AssessDao assessDao = new AssessDao(dbp.get(), siteInfo);
    SurveyRegistration reg = assessDao.getSurveyRegistrationByRegId(regId);
    if (reg == null) {
      throw new RuntimeException("Unable to get survey registration for survey_reg_id: " + regId);
    }

    surveyResponses(dbp.get(), reg);
    return true;
  }

  protected void surveyResponses(Database database, SurveyRegistration reg) {

    // Get the type (Initial or FollowUp), side and joint by parsing the survey type name
    String type = reg.getSurveyType();
    String side = getSide(type);
    String joint = getJoint(type);

    // If unable to determine type, side and joint then ignore the survey
    if ((type == null) || (side == null) || (joint == null)) {
      return;
    }

    // Get the date the survey was completed
    Date completed = null;
    ActivityDao activityDao = new ActivityDao(database, reg.getSurveySiteId());
    List<Activity> activities = activityDao.getActivityByToken(reg.getToken(), Constants.ACTIVITY_COMPLETED);
    if ((activities != null) || (activities.size() > 0)) {
      completed = activities.get(0).getActivityDt();
    }
    if (completed == null) {
      throw new RuntimeException("Unable to determine completion date, no Completed activity found for token " + reg.getToken());
    }

    // Bilateral surveys will create two rows in the responses table, one for each side

    if (side.equals(TotalJointCustomizer.SIDE_BILATERAL)) {
      // UCLA and VR12 are only asked once but the values are stored in both rows
      uclaResponses(database, reg, completed, type, joint, TotalJointCustomizer.SIDE_LEFT);
      vr12Responses(database, reg, completed, type, joint, TotalJointCustomizer.SIDE_LEFT);
      uclaResponses(database, reg, completed, type, joint, TotalJointCustomizer.SIDE_RIGHT);
      vr12Responses(database, reg, completed, type, joint, TotalJointCustomizer.SIDE_RIGHT);
    } else {
      uclaResponses(database, reg, completed, type, joint, side);
      vr12Responses(database, reg, completed, type, joint, side);
    }

    if (joint.equals(TotalJointCustomizer.JOINT_HIP)) {
      if (side.equals(TotalJointCustomizer.SIDE_BILATERAL)) {
        // HOOS and Harris are asked separately for both left and right
        hoosResponses(database, reg, completed, type, TotalJointCustomizer.SIDE_LEFT);
        harrisResponses(database, reg, completed, type, TotalJointCustomizer.SIDE_LEFT);
        hoosResponses(database, reg, completed, type, TotalJointCustomizer.SIDE_RIGHT);
        harrisResponses(database, reg, completed, type, TotalJointCustomizer.SIDE_RIGHT);
      } else {
        hoosResponses(database, reg, completed, type, side);
        harrisResponses(database, reg, completed, type, side);
      }
    }

    if (joint.equals(TotalJointCustomizer.JOINT_KNEE)) {
      if (side.equals(TotalJointCustomizer.SIDE_BILATERAL)) {
        // KOOS and Knee Society are asked separately for both left and right
        koosResponses(database, reg, completed, type, TotalJointCustomizer.SIDE_LEFT);
        ksResponses(database, reg, completed, type, TotalJointCustomizer.SIDE_LEFT);
        koosResponses(database, reg, completed, type, TotalJointCustomizer.SIDE_RIGHT);
        ksResponses(database, reg, completed, type, TotalJointCustomizer.SIDE_RIGHT);
      } else {
        koosResponses(database, reg, completed, type, side);
        ksResponses(database, reg, completed, type, side);
      }
    }
  }

  protected void uclaResponses(Database database, SurveyRegistration reg, Date date, String type, String joint, String side) {
    String[] fields = new String[] { "UCLA_Q1" };
    String[] values = new String[fields.length];

    String xmlString = getXML(database, reg, "UCLAActivityScore");
    if (xmlString != null) {
      values[0] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='1']//Response[@Order='1']/item[@selected='true']/value");  // UCLA_Q1
    }

    insertResponses(database, reg, date, type, joint, side, fields, values);
  }

  protected void vr12Responses(Database database, SurveyRegistration reg, Date date, String type, String joint, String side) {
    String[] fields = new String[]
        { "VR12_GH1", "VR12_PF02", "VR12_PF04", "VR12_RP2", "VR12_RP3", "VR12_RE2", "VR12_RE3",
          "VR12_BP2", "VR12_MH3", "VR12_VT2", "VR12_MH4", "VR12_SF2", "VR12_Q8", "VR12_Q9"
        };
    String[] values = new String[fields.length];

    String xmlString = getXML(database, reg, "VR12");
    if (xmlString != null) {
      values[0]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='GH1']/item[@selected='true']/value");  // VR12_GH1
      values[1]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='PF02']/item[@selected='true']/value"); // VR12_PF02
      values[2]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='PF04']/item[@selected='true']/value"); // VR12_PF04
      values[3]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='RP2']/item[@selected='true']/value");  // VR12_RP2
      values[4]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='RP3']/item[@selected='true']/value");  // VR12_RP3
      values[5]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='RE2']/item[@selected='true']/value");  // VR12_RE2
      values[6]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='RE3']/item[@selected='true']/value");  // VR12_RE3
      values[7]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='BP2']/item[@selected='true']/value");  // VR12_BP2
      values[8]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='MH3']/item[@selected='true']/value");  // VR12_MH3
      values[9]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='VT2']/item[@selected='true']/value");  // VR12_VT2
      values[10] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='MH4']/item[@selected='true']/value");  // VR12_MH4
      values[11] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='SF2']/item[@selected='true']/value");  // VR12_SF2
      values[12] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q8']/item[@selected='true']/value");   // VR12_Q8
      values[13] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q9']/item[@selected='true']/value");   // VR12_Q9
    }

    insertResponses(database, reg, date, type, joint, side, fields, values);
  }

  protected void hoosResponses(Database database, SurveyRegistration reg, Date date, String type, String side) {
    String[] fields = new String[]
        { "HOOS_S1", "HOOS_S2", "HOOS_S3", "HOOS_S4", "HOOS_S5",
          "HOOS_P1", "HOOS_P2", "HOOS_P3", "HOOS_P4", "HOOS_P5", "HOOS_P6", "HOOS_P7", "HOOS_P8", "HOOS_P9", "HOOS_P10",
          "HOOS_A1", "HOOS_A2", "HOOS_A3", "HOOS_A4", "HOOS_A5", "HOOS_A6", "HOOS_A7", "HOOS_A8", "HOOS_A9", "HOOS_A10",
          "HOOS_A11", "HOOS_A12", "HOOS_A13", "HOOS_A14", "HOOS_A15", "HOOS_A16", "HOOS_A17",
          "HOOS_SP1", "HOOS_SP2", "HOOS_SP3", "HOOS_SP4",
          "HOOS_Q1", "HOOS_Q2", "HOOS_Q3", "HOOS_Q4",
          "HOOS_VERSION"
        };
    String[] values = new String[fields.length];

    values[40] = "HOOS"; // HOOS_VERSION

    String xmlString;
    xmlString = getXML(database, reg, side+"HOOSHipSymptoms");
    if (xmlString != null) {
      values[0]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S1']/item[@selected='true']/value");  // HOOS_S1
      values[1]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S2']/item[@selected='true']/value");  // HOOS_S2
      values[2]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S3']/item[@selected='true']/value");  // HOOS_S3
      values[3]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S4']/item[@selected='true']/value");  // HOOS_S4
      values[4]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S5']/item[@selected='true']/value");  // HOOS_S5
    }

    xmlString = getXML(database, reg, side+"HOOSHipPain");
    if (xmlString != null) {
      values[5]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P1']/item[@selected='true']/value");  // HOOS_P1
      values[6]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P2']/item[@selected='true']/value");  // HOOS_P2
      values[7]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P3']/item[@selected='true']/value");  // HOOS_P3
      values[8]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P4']/item[@selected='true']/value");  // HOOS_P4
      values[9]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P5']/item[@selected='true']/value");  // HOOS_P5
      values[10] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P6']/item[@selected='true']/value");  // HOOS_P6
      values[11] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P7']/item[@selected='true']/value");  // HOOS_P7
      values[12] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P8']/item[@selected='true']/value");  // HOOS_P8
      values[13] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P9']/item[@selected='true']/value");  // HOOS_P9
      values[14] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P10']/item[@selected='true']/value"); // HOOS_P10
    }

    xmlString = getXML(database, reg, side+"HOOSHipFunction");
    if (xmlString != null) {
      values[15] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A1']/item[@selected='true']/value");  // HOOS_A1
      values[16] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A2']/item[@selected='true']/value");  // HOOS_A2
      values[17] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A3']/item[@selected='true']/value");  // HOOS_A3
      values[18] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A4']/item[@selected='true']/value");  // HOOS_A4
      values[19] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A5']/item[@selected='true']/value");  // HOOS_A5
      values[20] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A6']/item[@selected='true']/value");  // HOOS_A6
      values[21] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A7']/item[@selected='true']/value");  // HOOS_A7
      values[22] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A8']/item[@selected='true']/value");  // HOOS_A8
      values[23] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A9']/item[@selected='true']/value");  // HOOS_A9
      values[24] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A10']/item[@selected='true']/value"); // HOOS_A10
      values[25] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A11']/item[@selected='true']/value"); // HOOS_A11
      values[26] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A12']/item[@selected='true']/value"); // HOOS_A12
      values[27] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A13']/item[@selected='true']/value"); // HOOS_A13
      values[28] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A14']/item[@selected='true']/value"); // HOOS_A14
      values[29] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A15']/item[@selected='true']/value"); // HOOS_A15
      values[30] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A16']/item[@selected='true']/value"); // HOOS_A16
      values[31] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A17']/item[@selected='true']/value"); // HOOS_A17
    }

    xmlString = getXML(database, reg, side+"HOOSHipSports");
    if (xmlString != null) {
      values[32] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='SP1']/item[@selected='true']/value"); // HOOS_SP1
      values[33] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='SP2']/item[@selected='true']/value"); // HOOS_SP2
      values[34] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='SP3']/item[@selected='true']/value"); // HOOS_SP3
      values[35] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='SP4']/item[@selected='true']/value"); // HOOS_SP4
    }

    xmlString = getXML(database, reg, side+"HOOSHipQOL");
    if (xmlString != null) {
      values[36] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q1']/item[@selected='true']/value");  // HOOS_Q1
      values[37] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q2']/item[@selected='true']/value");  // HOOS_Q2
      values[38] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q3']/item[@selected='true']/value");  // HOOS_Q3
      values[39] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q4']/item[@selected='true']/value");  // HOOS_Q4
    }

    xmlString = getXML(database, reg, side+"HOOSJR");
    if (xmlString != null) {
      values[9]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P5']/item[@selected='true']/value");  // HOOS_P5
      values[14] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P10']/item[@selected='true']/value"); // HOOS_P10
      values[17] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A3']/item[@selected='true']/value");  // HOOS_A3
      values[19] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A5']/item[@selected='true']/value");  // HOOS_A5
      values[26] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A12']/item[@selected='true']/value"); // HOOS_A12
      values[28] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A14']/item[@selected='true']/value"); // HOOS_A14
      values[40] = "HOOS_JR"; // HOOS_VERSION
    }

    insertResponses(database, reg, date, type, TotalJointCustomizer.JOINT_HIP, side, fields, values);
  }

  protected void koosResponses(Database database, SurveyRegistration reg, Date date, String type, String side) {
    String[] fields = new String[]
        { "KOOS_S1", "KOOS_S2", "KOOS_S3", "KOOS_S4", "KOOS_S5", "KOOS_S6", "KOOS_S7",
          "KOOS_P1", "KOOS_P2", "KOOS_P3", "KOOS_P4", "KOOS_P5", "KOOS_P6", "KOOS_P7", "KOOS_P8", "KOOS_P9",
          "KOOS_A1", "KOOS_A2", "KOOS_A3", "KOOS_A4", "KOOS_A5", "KOOS_A6", "KOOS_A7", "KOOS_A8", "KOOS_A9",
          "KOOS_A10", "KOOS_A11", "KOOS_A12", "KOOS_A13", "KOOS_A14", "KOOS_A15", "KOOS_A16", "KOOS_A17",
          "KOOS_SP1", "KOOS_SP2", "KOOS_SP3", "KOOS_SP4", "KOOS_SP5",
          "KOOS_Q1", "KOOS_Q2", "KOOS_Q3", "KOOS_Q4",
          "KOOS_VERSION"
        };
    String[] values = new String[fields.length];

    values[42] = "KOOS"; // KOOS_VERSION

    String xmlString;
    xmlString = getXML(database, reg, side+"KOOSKneeSymptoms");
    if (xmlString != null) {
      values[0]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S1']/item[@selected='true']/value");  // KOOS_S1
      values[1]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S2']/item[@selected='true']/value");  // KOOS_S2
      values[2]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S3']/item[@selected='true']/value");  // KOOS_S3
      values[3]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S4']/item[@selected='true']/value");  // KOOS_S4
      values[4]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S5']/item[@selected='true']/value");  // KOOS_S5
      values[5]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S6']/item[@selected='true']/value");  // KOOS_S6
      values[6]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S7']/item[@selected='true']/value");  // KOOS_S7
    }

    xmlString = getXML(database, reg, side+"KOOSKneePain");
    if (xmlString != null) {
      values[7]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P1']/item[@selected='true']/value");  // KOOS_P1
      values[8]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P2']/item[@selected='true']/value");  // KOOS_P2
      values[9]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P3']/item[@selected='true']/value");  // KOOS_P3
      values[10] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P4']/item[@selected='true']/value");  // KOOS_P4
      values[11] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P5']/item[@selected='true']/value");  // KOOS_P5
      values[12] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P6']/item[@selected='true']/value");  // KOOS_P6
      values[13] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P7']/item[@selected='true']/value");  // KOOS_P7
      values[14] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P8']/item[@selected='true']/value");  // KOOS_P8
      values[15] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P9']/item[@selected='true']/value");  // KOOS_P9
    }

    xmlString = getXML(database, reg, side+"KOOSKneeFunction");
    if (xmlString != null) {
      values[16] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A1']/item[@selected='true']/value");  // KOOS_A1
      values[17] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A2']/item[@selected='true']/value");  // KOOS_A2
      values[18] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A3']/item[@selected='true']/value");  // KOOS_A3
      values[19] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A4']/item[@selected='true']/value");  // KOOS_A4
      values[20] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A5']/item[@selected='true']/value");  // KOOS_A5
      values[21] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A6']/item[@selected='true']/value");  // KOOS_A6
      values[22] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A7']/item[@selected='true']/value");  // KOOS_A7
      values[23] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A8']/item[@selected='true']/value");  // KOOS_A8
      values[24] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A9']/item[@selected='true']/value");  // KOOS_A9
      values[25] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A10']/item[@selected='true']/value"); // KOOS_A10
      values[26] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A11']/item[@selected='true']/value"); // KOOS_A11
      values[27] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A12']/item[@selected='true']/value"); // KOOS_A12
      values[28] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A13']/item[@selected='true']/value"); // KOOS_A13
      values[29] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A14']/item[@selected='true']/value"); // KOOS_A14
      values[30] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A15']/item[@selected='true']/value"); // KOOS_A15
      values[31] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A16']/item[@selected='true']/value"); // KOOS_A16
      values[32] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A17']/item[@selected='true']/value"); // KOOS_A17
    }

    xmlString = getXML(database, reg, side+"KOOSKneeSports");
    if (xmlString != null) {
      values[33] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='SP1']/item[@selected='true']/value"); // KOOS_SP1
      values[34] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='SP2']/item[@selected='true']/value"); // KOOS_SP2
      values[35] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='SP3']/item[@selected='true']/value"); // KOOS_SP3
      values[36] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='SP4']/item[@selected='true']/value"); // KOOS_SP4
      values[37] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='SP5']/item[@selected='true']/value"); // KOOS_SP5
    }

    xmlString = getXML(database, reg, side+"KOOSKneeQOL");
    if (xmlString != null) {
      values[38] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q1']/item[@selected='true']/value");  // KOOS_Q1
      values[39] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q2']/item[@selected='true']/value");  // KOOS_Q2
      values[40] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q3']/item[@selected='true']/value");  // KOOS_Q3
      values[41] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q4']/item[@selected='true']/value");  // KOOS_Q4
    }

    xmlString = getXML(database, reg, side+"KOOSJR");
    if (xmlString != null) {
      values[5]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='S6']/item[@selected='true']/value");  // KOOS_S6
      values[8]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P2']/item[@selected='true']/value");  // KOOS_P2
      values[9]  = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P3']/item[@selected='true']/value");  // KOOS_P3
      values[12] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P6']/item[@selected='true']/value");  // KOOS_P6
      values[15] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='P9']/item[@selected='true']/value");  // KOOS_P9
      values[18] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A3']/item[@selected='true']/value");  // KOOS_A3
      values[20] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='A5']/item[@selected='true']/value");  // KOOS_A5
      values[42] = "KOOS_JR"; // KOOS_VERSION
    }

    insertResponses(database, reg, date, type, TotalJointCustomizer.JOINT_KNEE, side, fields, values);
  }

  protected void harrisResponses(Database database, SurveyRegistration reg, Date date, String type, String side) {
    String[] fields = new String[]
        { "HARRIS_Q1", "HARRIS_Q2", "HARRIS_Q3", "HARRIS_Q4", "HARRIS_Q5", "HARRIS_Q6", "HARRIS_Q7", "HARRIS_Q8"
        };
    String[] values = new String[fields.length];

    String xmlString;
    xmlString = getXML(database, reg, side+"HarrisHipPain");
    if (xmlString != null) {
      values[0] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='1']//Response[@Order='1']/item[@selected='true']/value");  // HARRIS_Q1
    }

    xmlString = getXML(database, reg, side+"HarrisHipFunction");
    if (xmlString != null) {
      values[1] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='2']//Response[@Order='1']/item[@selected='true']/value");  // HARRIS_Q2
      values[2] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='3']//Response[@Order='1']/item[@selected='true']/value");  // HARRIS_Q3
      values[3] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='4']//Response[@Order='1']/item[@selected='true']/value");  // HARRIS_Q4
      values[4] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='5']//Response[@Order='1']/item[@selected='true']/value");  // HARRIS_Q5
      values[5] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='6']//Response[@Order='1']/item[@selected='true']/value");  // HARRIS_Q6
      values[6] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='7']//Response[@Order='1']/item[@selected='true']/value");  // HARRIS_Q7
      values[7] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='8']//Response[@Order='1']/item[@selected='true']/value");  // HARRIS_Q8
    }

    insertResponses(database, reg, date, type, TotalJointCustomizer.JOINT_HIP, side, fields, values);
  }

  protected void ksResponses(Database database, SurveyRegistration reg, Date date, String type, String side) {
    String[] fields = new String[]
        { "KS_S1", "KS_S2", "KS_S3",
          "KS_PS1", "KS_PS2", "KS_PS3", "KS_PS4", "KS_PS5",
          "KS_PE1", "KS_PE2", "KS_PE3", "KS_PE4", "KS_PE5", "KS_PE6",
          "KS_FA1", "KS_FA2", "KS_FA3", "KS_FA4",
          "KS_Q1", "KS_Q2", "KS_Q3", "KS_Q4", "KS_Q5", "KS_Q6", "KS_Q7",
          "KS_Q8", "KS_Q9", "KS_Q10", "KS_Q11", "KS_Q12", "KS_Q13",
          "KS_R1A", "KS_R1B", "KS_R2A", "KS_R2B", "KS_R3A", "KS_R3B",
        };
    String[] values = new String[fields.length];

    String xmlString;
    xmlString = getXML(database, reg, side+"KneeSocietySymptoms");
    if (xmlString != null) {
      values[0]  = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='1']/@ItemScore");  // KS_S1
      values[1]  = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='2']/@ItemScore");  // KS_S2
      values[2]  = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='3']//Response[@Order='1']/item[@selected='true']/value");  // KS_S3
    }

    xmlString = getXML(database, reg, side+"KneeSocietySatisfaction");
    if (xmlString != null) {
      values[3]  = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='2']//Response[@Order='1']/item[@selected='true']/value");  // KS_PS1
      values[4]  = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='3']//Response[@Order='1']/item[@selected='true']/value");  // KS_PS2
      values[5]  = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='4']//Response[@Order='1']/item[@selected='true']/value");  // KS_PS3
      values[6]  = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='5']//Response[@Order='1']/item[@selected='true']/value");  // KS_PS4
      values[7]  = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='6']//Response[@Order='1']/item[@selected='true']/value");  // KS_PS5
    }

    xmlString = getXML(database, reg, side+"KneeSocietyPreExpect");
    if (xmlString != null) {
      values[8]  = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='1']//Response[@Order='1']/item[@selected='true']/value");  // KS_PE1
      values[9]  = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='2']//Response[@Order='1']/item[@selected='true']/value");  // KS_PE2
      values[10] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='3']//Response[@Order='1']/item[@selected='true']/value");  // KS_PE3
    }

    xmlString = getXML(database, reg, side+"KneeSocietyPostExpect");
    if (xmlString != null) {
      values[11] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='1']//Response[@Order='1']/item[@selected='true']/value");  // KS_PE4
      values[12] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='2']//Response[@Order='1']/item[@selected='true']/value");  // KS_PE5
      values[13] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='3']//Response[@Order='1']/item[@selected='true']/value");  // KS_PE6
    }

    xmlString = getXML(database, reg, side+"KneeSocietyFunction2");
    if (xmlString != null) {
      values[14] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='NeedAid']/item[@selected='true']/value");  // KS_FA1
      values[15] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='AidDevice']/item[@selected='true']/value");  // KS_FA2
      values[16] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='AidDeviceOther']/value");  // KS_FA3
      values[17] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='AidDeviceKnee']/item[@selected='true']/value");  // KS_FA4
      values[18] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q1']/item[@selected='true']/value");  // KS_Q1
      values[19] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q2']/item[@selected='true']/value");  // KS_Q2
      values[20] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q3']/item[@selected='true']/value");  // KS_Q3
      values[21] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q4']/item[@selected='true']/value");  // KS_Q4
      values[22] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q5']/item[@selected='true']/value");  // KS_Q5
      values[23] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q6']/item[@selected='true']/value");  // KS_Q6
      values[24] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q7']/item[@selected='true']/value");  // KS_Q7
      values[25] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q8']/item[@selected='true']/value");  // KS_Q8
      values[26] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q9']/item[@selected='true']/value");  // KS_Q9
      values[27] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q10']/item[@selected='true']/value"); // KS_Q10
      values[28] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q11']/item[@selected='true']/value"); // KS_Q11
      values[29] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q12']/item[@selected='true']/value"); // KS_Q12
      values[30] = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='Q13']/item[@selected='true']/value"); // KS_Q13
    }

    xmlString = getXML(database, reg, side+"KneeSocietyRecreation");
    if (xmlString != null) {
      values[31] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='1']//Response[@Order='1']/item[@selected='true']/value");  // KS_R1A
      values[32] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='2']//Response[@Order='1']/item[@selected='true']/value");  // KS_R1B
      values[33] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='3']//Response[@Order='1']/item[@selected='true']/value");  // KS_R2A
      values[34] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='4']//Response[@Order='1']/item[@selected='true']/value");  // KS_R2B
      values[35] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='5']//Response[@Order='1']/item[@selected='true']/value");  // KS_R3A
      values[36] = XMLFileUtils.xPathQuery(xmlString, "//Item[@Order='6']//Response[@Order='1']/item[@selected='true']/value");  // KS_R3B
    }

    insertResponses(database, reg, date, type, TotalJointCustomizer.JOINT_KNEE, side, fields, values);
  }

  protected String getSide(String surveyType) {
    String side = null;
    if (surveyType.matches(".*Left.*")) {
      side = TotalJointCustomizer.SIDE_LEFT;
    } else if (surveyType.matches(".*Right.*")) {
      side = TotalJointCustomizer.SIDE_RIGHT;
    } else if (surveyType.matches(".*Bi.*")) {
      side = TotalJointCustomizer.SIDE_BILATERAL;
    }
    return side;
  }

  protected String getJoint(String surveyType) {
    String joint = null;
    if (surveyType.matches(".*Knee.*")) {
      joint = TotalJointCustomizer.JOINT_KNEE;
    } else if (surveyType.matches(".*Hip.*")) {
      joint = TotalJointCustomizer.JOINT_HIP;
    }
    return joint;
  }

  protected String getXML(Database database, SurveyRegistration reg, String studyDesc) {
    String xmlString = null;
    PatientStudyExtendedData patStudy = getPatientStudy(database, reg.getSurveyRegId(), studyDesc);
    if (patStudy != null) {
      xmlString = patStudy.getContents();
    }
    return xmlString;
  }

  protected PatientStudyExtendedData getPatientStudy(Database database, Long regId, String studyDesc) {
    PatientStudyExtendedData patStudy = null;

    // Find the patient study for the survey registration
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    ArrayList<PatientStudyExtendedData> patStudies =
        patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(regId, studyDesc);
    if ((patStudies == null) || (patStudies.size() == 0)) {
      // patient study not found, ignore
    } else if (patStudies.size() > 1) {
      // more than one patient study found
      throw new RuntimeException("More than one patient study found for survey registration " + regId + " and study " + studyDesc);
    } else {
      patStudy = patStudies.get(0);
    }

    return patStudy;
  }

  protected void insertResponses(Database database, SurveyRegistration reg, Date completed,
      String type, String joint, String side, String[] fields, String[] values) {
    String sql;

    // Hip goes into tj_hip_responeses and Knee goes into tj_knee_responses
    String table = "";
    if (joint.equals(TotalJointCustomizer.JOINT_HIP)) {
      table = "tj_hip_responses";
    } else if (joint.equals(TotalJointCustomizer.JOINT_KNEE)) {
      table = "tj_knee_responses";
    } else {
      throw new RuntimeException("Invalid value specified for joint: " + joint);
    }

    // Check if there are any answers
    boolean answered = false;
    for(String value: values) {
      if (value != null) {
        answered = true;
      }
    }
    if (!answered) {
      return;
    }

    // Bilateral surveys create two rows, one for each side.
    // Check if a row already exists for the regId and side.
    sql = "select tj_responses_id from " + table + " where survey_reg_id = ? and side = ?";
    Long primaryKey = database.toSelect(sql)
      .argLong(reg.getSurveyRegId())
      .argString(side)
      .queryLongOrNull();

    @SuppressWarnings("unused")
    int countRows;
    if (primaryKey == null) {
      // If a row does not exist then insert a new row
      sql = "insert into " + table +" (tj_responses_id, survey_reg_id, survey_token, patient_id, scheduled, completed, survey_type, side) values (?,?,?,?,?,?,?,?)";
      countRows = database.toInsert(sql)
        .argPkSeq("tj_responses_seq")
        .argLong(reg.getSurveyRegId())
        .argString(reg.getToken())
        .argString(reg.getPatientId())
        .argDate(reg.getSurveyDt())
        .argDate(completed)
        .argString(type)
        .argString(side)
        .insert();

      // Get the primary key for the row
      sql = "select tj_responses_id from " + table + " where survey_reg_id = ? and side = ?";
      primaryKey = database.toSelect(sql)
        .argLong(reg.getSurveyRegId())
        .argString(side)
        .queryLongOrNull();
    }

    // Update the row to set the answer values
    sql = "update " + table + " set ";
    for(int i=0; i<fields.length; i++) {
      sql += (i>0) ? ", " : "";
      sql += fields[i] + "=?";
    }
    sql += " where tj_responses_id = ?";
    SqlUpdate update = database.toUpdate(sql);
    for (String value : values) {
      update = update.argString(value);
    }
    update = update.argLong(primaryKey);
    countRows = update.update();
  }
}
