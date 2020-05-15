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
package edu.stanford.registry.server.shc.cfs;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlSelect;

public class CFSSurveyScheduler {

  private static final Logger logger = LoggerFactory.getLogger(CFSSurveyScheduler.class);
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

  private final Database database;
  private final Long siteId;
  private final SiteInfo siteInfo;
  private final PatientDao patAttribDao;
  private final SurveyRegUtils surveyRegUtils;
  private final XMLFileUtils xmlFileUtils;
  private int lastSurveyDaysOut;

  public CFSSurveyScheduler(Database database, SiteInfo siteInfo) {
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
    logger.info("Updating base dates for follow up surveys");
    updateBaseDate();
    int followUps = scheduleFollowUp(toDate);
    logger.info("Sent {} follow up surveys", followUps);
    int threeMonthsFollowUps = scheduleThreeMonthsFollowUp(toDate);
    logger.info("Sent {} 3 months follow up surveys", threeMonthsFollowUps);
  }

  private void updateBaseDate() {
    // Select the patient and earliest completed survey date for patients
    // and who's base date attribute is null.
    String sql =
        "select asmt.patient_id, min(asmt.assessment_dt) " +
        "from assessment_registration asmt " +
        "  left join survey_registration survey on survey.assessment_reg_id = asmt.assessment_reg_id " +
        "  left join survey_token token on token.survey_site_id = survey.survey_site_id and token.survey_token = survey.token " +
        "  left join patient_attribute attr on attr.survey_site_id = asmt.survey_site_id and attr.patient_id = asmt.patient_id and " +
        "    attr.data_name = :attr " +
        "where " +
        "  asmt.survey_site_id = :site and " +
        "  upper(token.is_complete) = 'Y' and " +
        "  attr.data_value is null " +
        "group by asmt.patient_id " ;

    database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(":attr", CFSCustomizer.ATTR_BASE_DATE)
        .query((RowsHandler<Void>) rs -> {
          while(rs.next()) {
            String patientId = rs.getStringOrEmpty(1);
            Date baseDate = rs.getDateOrNull(2);
            // Set the patient's base date attribute
            String value = dateFormat.format(baseDate);
            logger.info("Setting {} attribute {} to {}",  patientId, CFSCustomizer.ATTR_BASE_DATE, value);
            PatientAttribute pattribute = new PatientAttribute(patientId, CFSCustomizer.ATTR_BASE_DATE, value, PatientAttribute.STRING);
            patAttribDao.insertAttribute(pattribute);
          }
          return null;
        }
        );
  }
  private int scheduleFollowUp(Date surveyDate) {
    int followUpsSent = 0;
    // Follow up schedule is every 12 months for 10 years
    Calendar baseDateCal = Calendar.getInstance();
    baseDateCal.setTime(surveyDate);
    for (int i=1; i<= 10; i++) {
      baseDateCal.add(Calendar.YEAR, -1);
      followUpsSent += sendFollowUp(baseDateCal.getTime(), surveyDate);
    }
    return followUpsSent;
  }

  private int sendFollowUp(Date baseDate, final Date surveyDate) {
    int followUpsSent = 0;
    try {
      /*
       * Find the patients with the attribute base date = to the given date
       * (and 2 days before incase the job didn't run for some reason)
       * that do not have another survey within the window
       */
      Calendar fromTime = DateUtils.getCalendarDayStart(siteInfo, surveyDate);
      fromTime.add(Calendar.DAY_OF_YEAR, lastSurveyDaysOut * -1);

      Date fromDate = DateUtils.getDateStart(siteInfo, DateUtils.getDaysFromDate(siteInfo, surveyDate, lastSurveyDaysOut * -1));
      Date toDate = DateUtils.getDateEnd(siteInfo, DateUtils.getDaysFromDate(siteInfo, surveyDate, lastSurveyDaysOut));

      String sql =
          "SELECT pa.patient_id  from patient_attribute pa "
              + "WHERE data_name = ? and data_value in (?,?,?) and survey_site_id = :site "
              + "and not exists "
              + "( select * from assessment_registration asmt join appt_registration appt on asmt.assessment_reg_id = appt.assessment_reg_id "
              + "   where asmt.survey_site_id = pa.survey_site_id and asmt.patient_id = pa.patient_id  "
              + "          and asmt.assessment_dt >= ?  "
              + "          and asmt.assessment_dt <= ?  "
              + "          and appt.registration_type != 'c'";

      sql = sql + ")";
      SqlSelect select = database.toSelect(sql)
          .argString(CFSCustomizer.ATTR_BASE_DATE)
          .argString(dateFormat.format(DateUtils.getDaysFromDate(siteInfo, baseDate, -2)))
          .argString(dateFormat.format(DateUtils.getDaysFromDate(siteInfo, baseDate, -1)))
          .argString(dateFormat.format(baseDate))
          .argLong(":site", siteId)
          .argDate(fromDate)//.argDate(fromTime.getTime())
          .argDate(toDate);

      followUpsSent = select
          .query(rs -> {
            int count = 0;
            while (rs.next()) {
              count += 1;
              createFollowUp(rs.getStringOrNull(), surveyDate);
            }
            return count;
          });

    } catch (Exception ex) {
      logger.error("Error getting surveys with base date {} to send follow ups", baseDate, ex);
    }
    return followUpsSent;
  }

  private void createFollowUp(String patientId, Date followUpDate) {
    // By convention, stand alone surveys are at 11:59:59 PM
    Calendar cal = Calendar.getInstance();
    cal.setTime(followUpDate);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
    Date surveyDate = cal.getTime();

    String surveyType = xmlFileUtils.getActiveProcessForName(CFSCustomizer.SURVEY_FOLLOW_UP, surveyDate);
    if (surveyType == null) {
      logger.error("Process not found for survey name {} and date {} ", CFSCustomizer.SURVEY_FOLLOW_UP, surveyDate);
      return;
    }

    logger.info("Creating follow up survey patientId: " + patientId +
        ", survey: " + surveyType + ", date: " + surveyDate);

    ApptRegistration appt = new ApptRegistration(siteId, patientId, surveyDate,
          null, surveyType, Constants.REGISTRATION_TYPE_STANDALONE_SURVEY,
        "FUP");
    surveyRegUtils.createRegistration(new AssessDao(database, siteInfo), appt);
  }

  //To survey patients every 3 months if they have not had a clinic visit and do not have a visit scheduled in the next 2 weeks.
  private int scheduleThreeMonthsFollowUp(Date toDate) {
    int count = 0;
    try {
      // a year mark for starting the filtering
      Date oneYearBack = DateUtils.getDaysAgoDate(siteInfo, 365);
      //Get start and end date of patient visit
      Date ninetyDaysBack = DateUtils.getDaysAgoDate(siteInfo, 90);
      Date fifteenDaysAfter = DateUtils.getDaysOutDate(siteInfo, 15);

      //Start and end date boundary values
      Date oneYearBackTime = DateUtils.getTimestampStart(siteInfo, oneYearBack);
      Date ninetyDaysBackTime = DateUtils.getTimestampStart(siteInfo, ninetyDaysBack);
      Date fifteenDaysAfterTime = DateUtils.getTimestampEnd(siteInfo, fifteenDaysAfter);

      //Check if a visit occurred between one year ago and 90 days ago AND the patient does not have any surveys scheduled between 90 days ago and 15 days from now
      String sql = "SELECT DISTINCT" + " ar.patient_id " + "FROM  appt_registration ar,"
          + "    patient_attribute pa WHERE ar.survey_site_id = :site "
          + "    AND (ar.visit_dt > ? and ar.visit_dt < ?) AND pa.patient_id = ar.patient_id "
          + "    AND pa.survey_site_id = ar.survey_site_id AND pa.data_name = 'participatesInSurveys' "
          + "    AND ar.registration_type = 'a' AND pa.data_value = 'y'"
          + "    AND pa.patient_id = ar.patient_id AND pa.survey_site_id = ar.survey_site_id"
          + "    AND not exists "
          + "    (select * from appt_registration s2 where s2.patient_id = ar.patient_id and s2.survey_site_id = ar.survey_site_id "
          + "    and s2.visit_dt > ? and s2.visit_dt < ? and registration_type != 'c')";

      //Collect patients
      ArrayList<String> patientIds = database.toSelect(sql).argLong(":site", siteId)
          .argDate(oneYearBackTime).argDate(ninetyDaysBackTime).argDate(ninetyDaysBackTime).argDate(fifteenDaysAfterTime)
          .query(rs -> {
            ArrayList<String> patients = new ArrayList<>();
            while (rs.next()) {
              patients.add(rs.getStringOrEmpty(1));
            }
            return patients;
          });

      //Create surveys
      PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
      for (String patientId : patientIds) {
        Patient thisPatient = patAttribDao.getPatient(patientId);
        if (thisPatient != null) {
          String emailAddress = thisPatient.getEmailAddress();
          if (emailAddress != null) {
            // Create Survey and email to patient, keep count of standalone surveys sent
            createFollowUp(patientId, toDate);
            count++;
          }
        }
      }

      //log # of surveys created
      if (count > 0)
        logger.debug("Created " + count + " standalone assessment registrations.");
      else
        logger.debug("No standalone assessment registration created.");
    } catch (Exception ex) {
      logger.error("Error processing scheduled surveys for patients without appointments!", ex);
    }
    return count;
  }
}
