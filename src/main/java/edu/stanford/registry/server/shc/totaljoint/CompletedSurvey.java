package edu.stanford.registry.server.shc.totaljoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * Bean class to represent a completed Total Joint survey.
 */
public class CompletedSurvey {

  private String patientId;
  private Date scheduledDate;
  private Date completionDate;
  private String joint;
  private String side;

  public CompletedSurvey(String patientId, Date scheduledDate, Date completionDate, String joint, String side) {
    this.patientId = patientId;
    this.scheduledDate = scheduledDate;
    this.completionDate = completionDate;
    this.joint = joint;
    this.side = side;
  }

  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public Date getScheduledDate() {
    return scheduledDate;
  }

  public void setScheduledDate(Date scheduledDate) {
    this.scheduledDate = scheduledDate;
  }

  public Date getCompletionDate() {
    return completionDate;
  }

  public void setCompletionDate(Date completionDate) {
    this.completionDate = completionDate;
  }

  public String getJoint() {
    return joint;
  }

  public void setJoint(String joint) {
    this.joint = joint;
  }

  public String getSide() {
    return side;
  }

  public void setSide(String side) {
    this.side = side;
  }

  /**
   * Get a map of patient id to list of completed surveys for that patient.
   */
  public static Map<String,List<CompletedSurvey>> getCompletedSurveys(Database database) {
    // Select from
    //  tj_hip_responses - Completed hip surveys
    //  tj_knee_responses - Completed knee surveys
    //  tj_stride_completed_survey - Surveys directly entered into Stride (for example paper surveys)
    String sql =
        "select patient_id, 'Hip' as joint, side, scheduled, completed from tj_hip_responses " +
        "union " +
        "select patient_id, 'Knee' as joint, side, scheduled, completed from tj_knee_responses " +
        "union " +
        "select ltrim(mrn,'0'), joint, side, scheduled, completed from tj_stride_completed_survey";
    Map<String,List<CompletedSurvey>> result = database.toSelect(sql)
      .query(new RowsHandler<Map<String,List<CompletedSurvey>>>(){
        public Map<String,List<CompletedSurvey>> process(Rows rows) throws Exception {
          Map<String,List<CompletedSurvey>> patientToSurveys = new HashMap<>();
          while(rows.next()) {
            String patientId = rows.getStringOrNull("patient_id");
            String joint = rows.getStringOrNull("joint");
            String side = rows.getStringOrNull("side");
            Date scheduledDate = rows.getDateOrNull("scheduled");
            Date completionDate = rows.getDateOrNull("completed");

            if (scheduledDate == null) {
              scheduledDate = completionDate;
            }

            List<CompletedSurvey> surveys = patientToSurveys.get(patientId);
            if (surveys == null) {
              surveys = new ArrayList<>();
              patientToSurveys.put(patientId, surveys);
            }
            CompletedSurvey survey = new CompletedSurvey(patientId, scheduledDate, completionDate, joint, side);
            surveys.add(survey);
          }
          return patientToSurveys;
        }
      });
    return result;
  }
}

