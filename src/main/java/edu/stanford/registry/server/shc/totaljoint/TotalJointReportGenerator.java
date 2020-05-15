package edu.stanford.registry.server.shc.totaljoint;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ReportGenerator;
import edu.stanford.registry.server.utils.DateUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * Generate the TotalJoint custom survey compliance report.
 */
public class TotalJointReportGenerator implements ReportGenerator {
  private static final String START_TABLE = "<table style='margin:auto; border:1px solid black; border-collapse:collapse'>";
  private static final String TABLE_HEADER = "<tr><th style='width:500px'></th><th style='width:80px; text-align:right; padding-right:5px'>Count</th></tr>";
  private static final String ROW_DESC = "<td style='border:1px solid black; border-collapse:collapse'>%s<br><small>%s</small>\n";
  private static final String ROW_VALUE = "<td style='border:1px solid black; border-collapse:collapse; text-align:right; padding-right:5px'>%d</td>\n";

  public TotalJointReportGenerator() {
  }

  private static final String SQL =
      "with tj_appt as ( "
    + "  select  "
    + "    appt.appt_reg_id, appt.patient_id, appt.visit_dt, appt.visit_type, appt.registration_type, appt.appt_complete, "
    + "    asmt.assessment_type, sreg.survey_reg_id, tok.is_complete "
    + "  from appt_registration appt "
    + "    left join assessment_registration asmt on asmt.assessment_reg_id = appt.assessment_reg_id "
    + "    left join survey_registration sreg on sreg.assessment_reg_id = asmt.assessment_reg_id "
    + "    left join survey_token tok on tok.survey_site_id = sreg.survey_site_id and tok.survey_token = sreg.token "
    + "  where appt.survey_site_id = :site "
    + ") "
    + "select "
    + "  tj_appt.*, "
    + "  ( select max(visit_dt) from tj_appt appt2 "
    + "    where appt2.patient_id = tj_appt.patient_id and appt2.assessment_type = tj_appt.assessment_type and "
    + "      trunc(appt2.visit_dt) <= trunc(tj_appt.visit_dt) and upper(appt2.is_complete) = 'Y' "
    + "  ) recently_completed, "
    + "  (select data_value from survey_reg_attr where survey_reg_id = tj_appt.survey_reg_id and data_name = 'PaperAssigned') paper_assigned, "
    + "  (select data_value from survey_reg_attr where survey_reg_id = tj_appt.survey_reg_id and data_name = 'RefusedSurvey') survey_refused, "
    + "  (select data_value from survey_reg_attr where survey_reg_id = tj_appt.survey_reg_id and data_name = 'FollowUpCompleted') followup_completed "
    + "from tj_appt "
    + "where tj_appt.registration_type = 'a' and visit_dt between :from and :to ";

  int apptsTotal = 0;

  ClinicStats ojClinic = new ClinicStats();
  ClinicStats lgClinic = new ClinicStats();

  public String createReport(Database database, Date fromDate, Date toDate, SiteInfo siteInfo) {

    database.toSelect(SQL)
        .argLong(":site", siteInfo.getSiteId())
        .argDate(":from", DateUtils.getDateStart(fromDate))
        .argDate(":to", DateUtils.getDateEnd(toDate))
        .query(new RowsHandler<Void>(){
          public Void process(Rows rs) throws Exception {
            while (rs.next()) {
              // rs.getLongOrNull("appt_reg_id");
              // rs.getStringOrEmpty("patient_id");
              // rs.getStringOrEmpty("registration_type");
              // rs.getLongOrNull("survey_reg_id");
              Date visitDate = rs.getDateOrNull("visit_dt");
              String visitType = rs.getStringOrEmpty("visit_type");
              String apptComplete = rs.getStringOrEmpty("appt_complete");
              String asmtType = rs.getStringOrEmpty("assessment_type");
              String surveyCompleted = rs.getStringOrEmpty("is_complete");
              Date recentlyCompleted = rs.getDateOrNull("recently_completed");
              String paperAssigned = rs.getStringOrEmpty("paper_assigned");
              String surveyRefused = rs.getStringOrEmpty("survey_refused");
              String followupCompleted = rs.getStringOrEmpty("followup_completed");

              apptsTotal += 1;

              ClinicStats clinicStats = null;
              if (visitType.startsWith("LG-")) {
                clinicStats = lgClinic;
                clinicStats.surveys += 1;
              } else {
                clinicStats = ojClinic;
                clinicStats.surveys += 1;
              }

              SurveyStats surveyStats = null;
              if (asmtType.equals("NoSurvey")) {
                clinicStats.nosurvey += 1;
              } else if (asmtType.equals("Ineligible")) {
                clinicStats.ineligible += 1;
              } else {
                if (asmtType.startsWith("Initial")) {
                  surveyStats = clinicStats.initial;
                  surveyStats.surveys += 1;
                } else if (asmtType.startsWith("FollowUp")) {
                  surveyStats = clinicStats.followUp;
                  surveyStats.surveys += 1;
                }
              }

              if (surveyStats != null) {
                if (surveyCompleted.equalsIgnoreCase("Y")) {
                  surveyStats.completed += 1;
                } else if (followupCompleted.equalsIgnoreCase("true")) {
                  surveyStats.recently_completed += 1;
                } else if (asmtType.equals("FollowUpDone")) {
                  surveyStats.recently_completed += 1;
                } else if (isRecentlyCompleted(visitDate, recentlyCompleted)) {
                  surveyStats.recently_completed += 1;
                } else if (surveyCompleted.equalsIgnoreCase("N")) {
                  surveyStats.started_not_completed += 1;
                } else if (paperAssigned.equalsIgnoreCase("true")) {
                  surveyStats.paper_assigned += 1;
                } else if (surveyRefused.equalsIgnoreCase("true")) {
                  surveyStats.refused_survey += 1;
                } else if (!apptComplete.equalsIgnoreCase("Y")) {
                  surveyStats.no_show += 1;
                } else {
                  surveyStats.not_completed_other += 1;
                }
              }
            }
            return null;
          }
        });


    StringWriter strWriter = new StringWriter();
    PrintWriter out = new PrintWriter(strWriter);
    out.println("<h3>Ortho Joint Clinic</h3>");
    clinicTable(out, "Ortho Joint Clinic Appointments", ojClinic);
    surveyTable(out, "Ortho Joint Clinic Initial Surveys", ojClinic.initial, "Initial");
    surveyTable(out, "Ortho Joint Clinic Follow Up Surveys", ojClinic.followUp, "FollowUp");

    out.println("<h3>Los Gatos Clinic</h3>");
    clinicTable(out, "Los Gatos Clinic Appointments", lgClinic);
    surveyTable(out, "Los Gatos Clinic Initial Surveys", lgClinic.initial, "Initial");
    surveyTable(out, "Los Gatos Clinic Follow Up Surveys", lgClinic.followUp, "FollowUp");

    return strWriter.toString();
  }

  private boolean isRecentlyCompleted(Date visitDate, Date recentlyCompleted) {
    Date cutoff = DateUtils.getDaysFromDate(visitDate, -42);
    return (recentlyCompleted != null) && recentlyCompleted.after(cutoff);
  }

  private void clinicTable(PrintWriter out, String title, ClinicStats clinicStats) {
    out.printf("<h4>%s</h4>\n", title);
    out.println(START_TABLE);
    out.println(TABLE_HEADER);
    out.println("<tr>");
    out.printf(ROW_DESC, "Initial Survey", "Pre-op appointments with an initial survey");
    out.printf(ROW_VALUE, clinicStats.initial.surveys);
    out.println("</tr>");
    out.println("<tr>");
    out.printf(ROW_DESC, "No Survey", "Pre-op appointments where an initial survey has not been assigned");
    out.printf(ROW_VALUE, clinicStats.nosurvey);
    out.println("</tr>");
    out.println("<tr>");
    out.printf(ROW_DESC, "Follow Up Survey", "Appointments with a follow up survey");
    out.printf(ROW_VALUE, clinicStats.followUp.surveys);
    out.println("</tr>");
    out.println("<tr>");
    out.printf(ROW_DESC, "Ineligible", "Appointments which do not qualify for a survey");
    out.printf(ROW_VALUE, clinicStats.ineligible);
    out.println("</tr>");
    out.println("</table>");
  }

  private void surveyTable(PrintWriter out, String title, SurveyStats surveyStats, String type) {
    out.printf("<h4>%s</h4>\n", title);
    out.println(START_TABLE);
    out.println(TABLE_HEADER);
    out.println("<tr>");
    out.printf(ROW_DESC, "Completed", "Survey was completed for the appointment");
    out.printf(ROW_VALUE, surveyStats.completed);
    out.println("</tr>");
    out.println("<tr>");
    if (type.equals("Initial")) {
      out.printf(ROW_DESC, "Recently Completed", "Survey was completed for a different appointment within the 6 week pre-op survey window");
    } else {
      out.printf(ROW_DESC, "Recently Completed", "Survey was completed for a different appointment within the follow up survey window");
    }
    out.printf(ROW_VALUE, surveyStats.recently_completed);
    out.println("</tr>");
    out.println("<tr>");
    out.printf(ROW_DESC, "Started (Not Completed)", "Survey was started but not completed");
    out.printf(ROW_VALUE, surveyStats.started_not_completed);
    out.println("</tr>");
    out.println("<tr>");
    out.printf(ROW_DESC, "Paper Survey", "Paper survey was given to the patient");
    out.printf(ROW_VALUE, surveyStats.paper_assigned);
    out.println("</tr>");
    out.println("<tr>");
    out.printf(ROW_DESC, "Survey Refused", "Patient refused the survey");
    out.printf(ROW_VALUE, surveyStats.refused_survey);
    out.println("</tr>");
    out.println("<tr>");
    out.printf(ROW_DESC, "No Show", "Patient did not show up for the appointment");
    out.printf(ROW_VALUE, surveyStats.no_show);
    out.println("</tr>");
    out.println("<tr>");
    out.printf(ROW_DESC, "Not Completed", "Survey was not completed, unknown reason");
    out.printf(ROW_VALUE, surveyStats.not_completed_other);
    out.println("</tr>");
    out.println("</table>");
  }

  public class ClinicStats {
    public long surveys = 0;
    public long ineligible = 0;
    public long nosurvey = 0;
    public SurveyStats initial = new SurveyStats();
    public SurveyStats followUp = new SurveyStats();
  }

  public class SurveyStats {
    public long surveys = 0;
    public long completed = 0;
    public long recently_completed = 0;
    public long started_not_completed = 0;
    public long paper_assigned = 0;
    public long refused_survey = 0;
    public long no_show = 0;
    public long not_completed_other = 0;
  }

}
