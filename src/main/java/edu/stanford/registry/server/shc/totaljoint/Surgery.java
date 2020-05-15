package edu.stanford.registry.server.shc.totaljoint;

import edu.stanford.registry.server.utils.StanfordMrn;

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
 * Bean class to represent a Total Joint surgery.
 */
public class Surgery {

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

  private String patientId;
  private Date surgeryDate;
  private String joint;
  private String side;

  public Surgery(String patientId, Date surgeryDate, String joint, String side) {
    this.patientId = patientId;
    this.surgeryDate = surgeryDate;
    this.joint = joint;
    this.side = side;
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

  public String getSide() {
    return side;
  }

  public void setSide(String side) {
    this.side = side;
  }

  public String toString() {
    return patientId + " " + dateFormat.format(surgeryDate) + " " + joint + " " + side;
  }

  /**
   * Get the list of surgeries from the tj_surgery_date table. Only the most
   * recent surgery for a patient, joint and side is included in the list if
   * the patient had multiple surgeries for the same joint and side.
   */
  public static List<Surgery> getSurgeries(Database database) {
    String sql =
        "select mrn, joint, side, max(surgery_date) as surgery_date from tj_surgery_date " +
        "where surgery_date is not null and joint is not null and side is not null " +
        "group by mrn, joint, side";
    List<Surgery> result = database.toSelect(sql)
      .query(new RowsHandler<List<Surgery>>(){
        public List<Surgery> process(Rows rows) throws Exception {
          List<Surgery> result = new ArrayList<>();
          while(rows.next()) {
            String mrn = rows.getStringOrNull("mrn");
            String joint = rows.getStringOrNull("joint");
            String side = rows.getStringOrNull("side");
            Date surgeryDate = rows.getDateOrNull("surgery_date");

            String patientId = new StanfordMrn().format(mrn);
            Surgery surgery = new Surgery(patientId, surgeryDate, joint, side);
            result.add(surgery);
          }
          return result;
        }
      });
    return result;
  }

  /**
   * Get a map of patient id to list of surgeries for that patient. Only the most
   * recent surgery for a patient, joint and side is included in the list if
   * the patient had multiple surgeries for the same joint and side.
   */
  public static Map<String,List<Surgery>> getPatientSurgeries(Database database) {
    Map<String,List<Surgery>> patientToSurgeries = new HashMap<>();
    List<Surgery> allSurgeries = getSurgeries(database);
    for(Surgery surgery : allSurgeries) {
      List<Surgery> patientSurgeries = patientToSurgeries.get(surgery.getPatientId());
      if (patientSurgeries == null) {
        patientSurgeries = new ArrayList<>();
        patientToSurgeries.put(surgery.getPatientId(), patientSurgeries);
      }
      patientSurgeries.add(surgery);

    }
    return patientToSurgeries;
  }
}
