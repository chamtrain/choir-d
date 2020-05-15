package edu.stanford.registry.server.shc.totaljoint;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.utils.StanfordMrn;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * Bean class to represent a Total Joint scheduled surgery for a pre-op
 * appointment. A pre-op scheduled surgery consists of a surgery date,
 * side and laterality.
 */
public class PreOpSurgery {

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

  private String patientId;
  private Date surgeryDate;
  private String joint;
  private String laterality;
  public final SiteInfo siteInfo;

  public PreOpSurgery(SiteInfo siteInfo, String patientId, Date surgeryDate, String joint, String laterality) {
    this.siteInfo = siteInfo;
    this.patientId = patientId;
    this.surgeryDate = surgeryDate;
    this.joint = joint;
    this.laterality = laterality;
  }

  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public Date getSurgeryDate() {
    return surgeryDate;
  }

  public void setSurgeryDate(Date surgeryDate) {
    this.surgeryDate = surgeryDate;
  }

  public String getJoint() {
    return joint;
  }

  public void setJoint(String joint) {
    this.joint = joint;
  }

  public String getLaterality() {
    return laterality;
  }

  public void setLaterality(String laterality) {
    this.laterality = laterality;
  }

  /**
   * Get the survey type for the pre-op surgery
   */
  protected String getSurveyType(Patient patient, Date surveyDate) {
    PatientAttribute attr = patient.getAttribute(TotalJointCustomizer.ATTR_HOOS_KOOS_TYPE);
    boolean useFull = (attr != null) && (attr.getDataValue() != null) && (attr.getDataValue().equalsIgnoreCase("full"));

    XMLFileUtils xmlFileUtils = XMLFileUtils.getInstance(siteInfo);
    String joint = getJoint();
    String laterality = getLaterality();

    String surveyType = null;
    if (joint.equals(TotalJointCustomizer.JOINT_HIP)) {
      if (laterality.equals(TotalJointCustomizer.SIDE_LEFT)) {
        if (useFull) {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_INITIAL_LEFT_HIP, surveyDate);
        } else {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_INITIAL_LEFT_HIP_JR, surveyDate);
        }
      } else if (laterality.equals(TotalJointCustomizer.SIDE_RIGHT)) {
        if (useFull) {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_INITIAL_RIGHT_HIP, surveyDate);
        } else {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_INITIAL_RIGHT_HIP_JR, surveyDate);
        }
      } else if (laterality.equals(TotalJointCustomizer.SIDE_BILATERAL)) {
        if (useFull) {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_INITIAL_BI_HIP, surveyDate);
        } else {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_INITIAL_BI_HIP_JR, surveyDate);
        }
      }
    } else if (joint.equals(TotalJointCustomizer.JOINT_KNEE)) {
      if (laterality.equals(TotalJointCustomizer.SIDE_LEFT)) {
        if (useFull) {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_INITIAL_LEFT_KNEE, surveyDate);
        } else {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_INITIAL_LEFT_KNEE_JR, surveyDate);
        }
      } else if (laterality.equals(TotalJointCustomizer.SIDE_RIGHT)) {
        if (useFull) {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_INITIAL_RIGHT_KNEE, surveyDate);
        } else {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_INITIAL_RIGHT_KNEE_JR, surveyDate);
        }
      } else if (laterality.equals(TotalJointCustomizer.SIDE_BILATERAL)) {
        if (useFull) {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_INITIAL_BI_KNEE, surveyDate);
        } else {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_INITIAL_BI_KNEE_JR, surveyDate);
        }
      }
    }

    return surveyType;
  }

  public String toString() {
    return patientId + " " + dateFormat.format(surgeryDate) + " " + joint + " " + laterality;
  }

  /**
   * Get the list of pre-op surgeries from the tj_preop_surgery_date table.
   */
  public static List<PreOpSurgery> getPreOpSurgeries(final SiteInfo siteInfo, Database database) {
    String sql =
        "select mrn, surgery_date, joint, laterality from tj_preop_surgery_date";
    List<PreOpSurgery> result = database.toSelect(sql)
      .query(new RowsHandler<List<PreOpSurgery>>(){
        public List<PreOpSurgery> process(Rows rows) throws Exception {
          List<PreOpSurgery> result = new ArrayList<>();
          while(rows.next()) {
            String mrn = rows.getStringOrNull("mrn");
            Date surgeryDate = rows.getDateOrNull("surgery_date");
            String joint = rows.getStringOrNull("joint");
            String laterality = rows.getStringOrNull("laterality");

            String patientId = new StanfordMrn().format(mrn);
            PreOpSurgery surgery = new PreOpSurgery(siteInfo, patientId, surgeryDate, joint, laterality);
            result.add(surgery);
          }
          return result;
        }
      });
    return result;
  }

  /**
   * Get a map of patient id to list of pre-op surgeries for that patient.
   */
  public static Map<String,List<PreOpSurgery>> getPatientPreOpSurgeries(SiteInfo siteInfo, Database database) {
    Map<String,List<PreOpSurgery>> patientToSurgeries = new HashMap<>();
    List<PreOpSurgery> allSurgeries = getPreOpSurgeries(siteInfo, database);
    for(PreOpSurgery surgery : allSurgeries) {
      List<PreOpSurgery> patientSurgeries = patientToSurgeries.get(surgery.getPatientId());
      if (patientSurgeries == null) {
        patientSurgeries = new ArrayList<>();
        patientToSurgeries.put(surgery.getPatientId(), patientSurgeries);
      }
      patientSurgeries.add(surgery);

    }
    return patientToSurgeries;
  }
}
