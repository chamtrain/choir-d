/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.interventionalradiology;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.PatientAttribute;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlSelect;

public class IRSurveyScheduler {

  private static final Logger logger = LoggerFactory.getLogger(IRSurveyScheduler.class);
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
  private static final String FOLLOWUP_VISITTYPE = "FUP";
  private final Database database;
  private final Long siteId;
  private final SiteInfo siteInfo;
  private final PatientDao patAttribDao;
  private final XMLFileUtils xmlFileUtils;
  private int lastSurveyDaysOut;
  private final SurveyRegUtils surveyRegUtils;

  public IRSurveyScheduler(Database database, SiteInfo siteInfo) {
    this.database = database;
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
    xmlFileUtils = XMLFileUtils.getInstance(siteInfo);
    patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    surveyRegUtils = new SurveyRegUtils(siteInfo);

    try {
      lastSurveyDaysOut = Integer.parseInt(siteInfo.getProperty("appointment.lastsurvey.daysout"));
    } catch (Exception ex) {
      lastSurveyDaysOut = 14;
    }
  }

  protected void scheduleSurveys(Date toDate) {
    logger.info("Updating base dates for DVT procedures");
    updateProcedureDate(IRCustomizer.ATTR_DVT_BASE_DATE, "DVT");
    logger.info("Scheduling follow ups for DVT");
    scheduleFollowUp(IRCustomizer.ATTR_DVT_BASE_DATE, "DVT", toDate);

    logger.info("Updating base dates for Lymphedema procedures");
    updateProcedureDate(IRCustomizer.ATTR_LYM_ARM_BASE_DATE, "LymArm");
    updateProcedureDate(IRCustomizer.ATTR_LYM_LEG_BASE_DATE, "LymLeg");
    logger.info("Scheduling follow ups for Lymphedema");
    scheduleFollowUp(IRCustomizer.ATTR_LYM_ARM_BASE_DATE, "LymArm", toDate);
    scheduleFollowUp(IRCustomizer.ATTR_LYM_LEG_BASE_DATE, "LymLeg", toDate);

    // HCC procedure date is manually entered
    // logger.info("Updating base dates for HCC procedures");
    // updateProcedureDate(IRCustomizer.ATTR_HCC_BASE_DATE, "HCC");
    logger.info("Scheduling follow ups for HCC");
    scheduleFollowUp(IRCustomizer.ATTR_HCC_BASE_DATE, "HCC", toDate);
  }

  private void updateProcedureDate(final String attr, String procedureType) {
    // Select the patient and earliest completed survey date for patients
    // with a survey for the procedure type and who's base date attribute is null.
    // Update 02/19: This method updates appointments only.
    String sql =
        "select asmt.patient_id, min(asmt.assessment_dt) " +
            "from appt_registration appt, assessment_registration asmt " +
            "  left join survey_registration survey on survey.assessment_reg_id = asmt.assessment_reg_id " +
            "  left join survey_token token on token.survey_site_id = survey.survey_site_id and token.survey_token = survey.token "
            +
            "  left join patient_attribute attr on attr.survey_site_id = asmt.survey_site_id and attr.patient_id = asmt.patient_id and "
            +
            "    attr.data_name = :attr " +
            "where " +
            "  asmt.survey_site_id = :site and " +
            "  asmt.assessment_type like :survey and " +
            "  upper(token.is_complete) = 'Y' and " +
            "  attr.data_value is null and " +
            "  appt.assessment_reg_id = asmt.assessment_reg_id and " +
            "  appt.registration_type = 'a' " +
            "group by asmt.patient_id ";

    database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(":attr", attr)
        .argString(":survey", "%" + procedureType + "%")
        .query((RowsHandler<Void>) rs -> {
          while (rs.next()) {
            String patientId = rs.getStringOrEmpty(1);
            Date baseDate = rs.getDateOrNull(2);
            // Set the patient's base date attribute
            String value = dateFormat.format(baseDate);
            logger.info("Setting " + patientId + " attribute " + attr + " to " + value);
            PatientAttribute pattribute = new PatientAttribute(patientId, attr, value, PatientAttribute.STRING);
            patAttribDao.insertAttribute(pattribute);
          }
          return null;
        }
        );
  }

  public int scheduleFollowUp(final String baseDateAttr, final String surveyType, final Date surveyDate) {
    int counts = 0;
    Date now = new Date();
    Calendar baseDate = DateUtils.getCalendarDayEnd(siteInfo, surveyDate);
    if (surveyType.equals("HCC")) { // 1 week, 2 months, 6 months
      baseDate.add(Calendar.DAY_OF_YEAR, -7); // one week
      counts += sendFollowUp(baseDateAttr, baseDate.getTime(), now, surveyType);
      counts += sendFollowUp(baseDateAttr, monthsBackDate(baseDate, now, 2), surveyDate, surveyType); // 2 months
      counts += sendFollowUp(baseDateAttr, monthsBackDate(baseDate, now, 6), surveyDate, surveyType); // 6 months
      return counts;
    }
    // Otherwise Follow up schedule is 1 month, 6 months and 12 months
    counts += sendFollowUp(baseDateAttr, monthsBackDate(baseDate, now, 1), surveyDate, surveyType);
    counts += sendFollowUp(baseDateAttr, monthsBackDate(baseDate, now, 6), surveyDate, surveyType);
    counts += sendFollowUp(baseDateAttr, monthsBackDate(baseDate, now, 12), surveyDate, surveyType);
    return counts;
  }

  private Date monthsBackDate(Calendar baseDate, Date surveyDate, int months) {
    baseDate.setTime(surveyDate);
    baseDate.add(Calendar.MONTH, months * -1);
    return baseDate.getTime();
  }

  private int sendFollowUp(String baseDateAttr, Date baseDate, final Date surveyDate, final String surveyType) {
    int followUpsSent = 0;
    try {
      /*
       * Find the patients with the attribute base date = to the given date (2 days before and 2 days after)
       * that do not have another survey within the window
       * The overlap picks up any days missed if the job wasn't run for some reason.
       */
      Calendar fromTime = DateUtils.getCalendarDayStart(siteInfo, surveyDate);
      fromTime.add(Calendar.DAY_OF_YEAR, lastSurveyDaysOut * -1);

      Calendar toTime = DateUtils.getCalendarDayEnd(siteInfo, surveyDate);
      toTime.add(Calendar.DAY_OF_YEAR, lastSurveyDaysOut);

      String sql =
          "SELECT pa.patient_id  from patient_attribute pa "
              + "WHERE data_name = ? and data_value in (?,?,?,?,?) and survey_site_id = :site "
              + "and not exists "
              + "( select * from assessment_registration asmt join appt_registration appt on asmt.assessment_reg_id = appt.assessment_reg_id "
              + "   where asmt.survey_site_id = pa.survey_site_id and asmt.patient_id = pa.patient_id  "
              + "          and asmt.assessment_dt >= ?  "
              + "          and asmt.assessment_dt <= ?  "
              + "          and appt.registration_type != 'c'";
      if (surveyType.equals("HCC")) {
        sql = sql + " and appt.visit_type = ? ";
      }
      sql = sql + ")";
      SqlSelect select = database.toSelect(sql)
          .argString(baseDateAttr)
          .argString(dateFormat.format(DateUtils.getDaysFromDate(siteInfo, baseDate, -2)))
          .argString(dateFormat.format(DateUtils.getDaysFromDate(siteInfo, baseDate, -1)))
          .argString(dateFormat.format(baseDate))
          .argString(dateFormat.format(DateUtils.getDaysFromDate(siteInfo, baseDate, 1)))
          .argString(dateFormat.format(DateUtils.getDaysFromDate(siteInfo, baseDate, 2)))
          .argLong(":site", siteId)
          .argDate(fromTime.getTime())
          .argDate(toTime.getTime());
      if (surveyType.equals("HCC")) {
        select = select.argString(surveyType);
      }
      followUpsSent = select
          .query(rs -> {
            int count = 0;
            while (rs.next()) {
              count += 1;
              createFollowUp(rs.getStringOrNull(), surveyDate, surveyType);
            }
            return count;
          });

    } catch (Exception ex) {
      logger.error("Error getting {} surveys to send follow ups", surveyType);
    }
    return followUpsSent;
  }

  private void createFollowUp(String patientId, Date followUpDate, String survey) {
    // By convention, stand alone surveys are at 11:59:59 PM
    Calendar cal = Calendar.getInstance();
    cal.setTime(followUpDate);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
    Date surveyDate = cal.getTime();

    String surveyType = xmlFileUtils.getActiveProcessForName(survey, surveyDate);
    if (surveyType == null) {
      logger.error("Process not found for survey name {} and date {}", survey, surveyDate);
      return;
    }

    logger.info("Creating follow up survey patientId: " + patientId +
        ", survey: " + surveyType + ", date: " + surveyDate);

    ApptRegistration appt = new ApptRegistration(siteId, patientId, surveyDate,
        null, surveyType, Constants.REGISTRATION_TYPE_STANDALONE_SURVEY,
        FOLLOWUP_VISITTYPE);

    surveyRegUtils.createRegistration(new AssessDao(database, siteInfo), appt);
  }
}
