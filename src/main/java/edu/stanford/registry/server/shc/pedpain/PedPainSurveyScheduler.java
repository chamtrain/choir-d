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
package edu.stanford.registry.server.shc.pedpain;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

public class PedPainSurveyScheduler {

  private final static Logger logger = LoggerFactory.getLogger(PedPainSurveyScheduler.class);


  private final SiteInfo siteInfo;
  private final Long siteId;

  public PedPainSurveyScheduler(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
  }

  protected void scheduleSurveys(Database database, Date toDate) {
    PedPainAppointment.refreshPatientAttrs(database, siteId);
    scheduleFollowUpSurveys(database, toDate);
  }

  /**
   * Schedule follow up surveys at intervals from either the initial
   * survey or the PREP program completion date.
   */
  @SuppressWarnings("UnusedReturnValue")
  private int scheduleFollowUpSurveys(Database database, Date surveyDate) {
    List<FollowUpData> followUps = new ArrayList<>();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(surveyDate);
    // 3, 6 and 9 months
    for (int i=1; i<=3; i++) {
      calendar.add(Calendar.MONTH, -3);
      followUps.addAll(getPrepEndFollowUpData(database, calendar.getTime(), surveyDate, "survey " + i*3 + "mn"));
    }
    // 1, 2, and 3 years
    calendar.setTime(new Date());
    for (int i=1; i<=3; i++) {
      calendar.add(Calendar.YEAR, -1);
      followUps.addAll(getPrepEndFollowUpData(database, calendar.getTime(), surveyDate, "survey " + i + "yr"));
    }

    Date today = DateUtils.getDateStart(siteInfo, new Date());
    Date twoWeeksAgo = DateUtils.getDaysFromDate(siteInfo, today, -14);

    // Loop through the follow up objects and schedule a follow up
    int countFollowUps = 0;
    for(FollowUpData followUp : followUps) {
      try {
        // Only schedule the follow up if the base date (initial survey or PREP program end)
        // has passed. This is to prevent PREP program follow ups from being scheduled before
        // the PREP program has been completed.
        if (followUp != null && followUp.getBaseDate().before(twoWeeksAgo)) {
          String surveyType = PedPainCustomizer.SURVEY_FOLLOWUP;
          if (PedPainCustomizer.is18AndOver(database, siteId, followUp.getPatientId())) {
            surveyType = PedPainCustomizer.SURVEY_FOLLOWUP_18;
          }
          createFollowUp(database, followUp.getPatientId(), followUp.getFollowUpDate(), followUp.getFollowUpName(),
              surveyType);
          countFollowUps++;
        }
      } catch (Exception e) {
        logger.error("Error while creating a scheduled follow up", e);
      }
    }
    logger.info("Sent {} PRepEnd scheduled follow up surveys", countFollowUps);

    // CAPTIVATE sends one follow up 3 months later
    calendar.setTime(surveyDate);
    calendar.add(Calendar.MONTH, -3);
    List<FollowUpData> captivateFollowUps = getCaptivateFollowUpData(database, calendar.getTime(), surveyDate);

    int countCaptivate = 0;
    for(FollowUpData followUp : captivateFollowUps) {
      try {
        // Only schedule the follow up if the base date has passed.
        // This is to prevent follow ups from being scheduled befor the program has been completed.
        if (followUp != null && followUp.getBaseDate().before(twoWeeksAgo)) {
          String surveyType = PedPainCustomizer.SURVEY_CAPTIVATE_FOLLOWUP;
          createFollowUp(database, followUp.getPatientId(), followUp.getFollowUpDate(), followUp.getFollowUpName(),
              surveyType);
          countCaptivate++;
        }
      } catch (Exception e) {
        logger.error("Error while creating a scheduled follow up", e);
      }
    }
    logger.info("Sent {} CAPTIVATE scheduled follow up surveys", countCaptivate);
    return countFollowUps + countCaptivate;
  }

  private List<FollowUpData> getPrepEndFollowUpData(Database database, Date searchDate, Date surveyDate, String followUpName) {
    final String prepEndSql =
      "  select  registered.patient_id, to_date(prepend.data_value, 'MM/DD/YYYY') as prepend, "
      + "        to_date(init.data_value, 'MM/DD/YYYY') as init from patient_attribute registered"
      + " left join patient_attribute init on init.patient_id = registered.patient_id and init.survey_site_id = registered.survey_site_id "
      + "      and init.data_name = ? and to_date(init.data_value, 'MM/DD/YYYY') > ? and to_date(init.data_value, 'MM/DD/YYYY') <= ?"
      + " left join patient_attribute prepend on prepend.patient_id = registered.patient_id and prepend.survey_site_id = registered.survey_site_id "
      + "      and prepend.data_name = ? and to_date(prepend.data_value, 'MM/DD/YYYY') > ? and to_date(prepend.data_value, 'MM/DD/YYYY') <= ? "
      + "where registered.data_name = 'participatesInSurveys' and lower(registered.data_value) = 'y' and registered.survey_site_id = :site "
      + "  and exists "
      + "      (select * from patient_attribute pa "
      + "        where pa.patient_id = registered.patient_id and pa.survey_site_id = registered.survey_site_id "
      + "          and data_name in (?, ?) and to_date(pa.data_value, 'MM/DD/YYYY') > ? and to_date(pa.data_value, 'MM/DD/YYYY') <= ?)"
      + "  and not exists "
      + "      (select * from appt_registration appt "
      + "        where appt.patient_id = registered.patient_id and appt.survey_site_id = registered.survey_site_id "
      + "          and appt.visit_dt > ? and appt.visit_dt <= ? and appt.registration_type != 'c')";
    Date twoWeeksBack = DateUtils.getDaysFromDate(siteInfo, searchDate, -14);

    return database.toSelect(prepEndSql)
        .argString(PedPainCustomizer.ATTR_INITIAL )
        .argDate(twoWeeksBack)
        .argDate(searchDate)
        .argString(PedPainCustomizer.ATTR_PREP_END)
        .argDate(twoWeeksBack)
        .argDate(searchDate)
        .argLong(":site", siteId)
        .argString(PedPainCustomizer.ATTR_INITIAL )
        .argString(PedPainCustomizer.ATTR_PREP_END)
        .argDate(twoWeeksBack)
        .argDate(searchDate)
        .argDate(DateUtils.getDaysFromDate(siteInfo, surveyDate, -14))
        .argDate(surveyDate)
        .query(rs -> {
              List<FollowUpData> followUps = new ArrayList<>();
              while(rs.next()) {
                String patientId = rs.getStringOrNull(1);
                Date prependDate = rs.getDateOrNull(2);
                Date initDate = rs.getDateOrNull(3);
                Date qualifyingDate = prependDate != null ? prependDate : initDate;
                if (initDate != null && initDate.after(qualifyingDate)) {
                  qualifyingDate = initDate;
                }
                logger.trace("patient is {}, prepend date is {} initDate is {} qualifying date is {}", patientId, prependDate, initDate, qualifyingDate);
                if (qualifyingDate != null && qualifyingDate.after(DateUtils.getDateStart(siteInfo, twoWeeksBack)) &&
                    qualifyingDate.before(DateUtils.getDateEnd(siteInfo, searchDate))) {
                  FollowUpData fup = new FollowUpData();
                  fup.setPatientId(patientId);
                  fup.setBaseDate(qualifyingDate);
                  fup.setFollowUpName(followUpName);
                  fup.setFollowUpDate(surveyDate);
                  followUps.add(fup);
                }
              }
              return followUps;
            }
        );
  }

  private List<FollowUpData> getCaptivateFollowUpData(Database database, Date searchDate, Date surveyDate) {
    final String captivateSql =
        "  select registered.patient_id, to_date(capend.data_value, 'MM/DD/YYYY') as capend "
            + " from patient_attribute registered"
            + " join patient_attribute capend on capend.patient_id = registered.patient_id and capend.survey_site_id = registered.survey_site_id "
            + "      and capend.data_name = ? and to_date(capend.data_value, 'MM/DD/YYYY') > ? and to_date(capend.data_value, 'MM/DD/YYYY') <= ? "
            + "where registered.data_name = 'participatesInSurveys' and lower(registered.data_value) = 'y' and registered.survey_site_id = :site "
            + "  and not exists "
            + "      (select * from appt_registration appt "
            + "        where appt.patient_id = registered.patient_id and appt.survey_site_id = registered.survey_site_id "
            + "          and appt.visit_dt > ? and appt.visit_dt <= ? and appt.registration_type != 'c')";
    Date twoWeeksBack = DateUtils.getDaysFromDate(siteInfo, searchDate, -14);

    return database.toSelect(captivateSql)
        .argString(PedPainCustomizer.ATTR_CAPTIVATE_END)
        .argDate(twoWeeksBack)
        .argDate(searchDate)
        .argLong(":site", siteId)
        .argDate(DateUtils.getDaysFromDate(siteInfo, surveyDate, -14))
        .argDate(surveyDate)
        .query(rs -> {
              List<FollowUpData> followUps = new ArrayList<>();
              while(rs.next()) {
                FollowUpData fup = new FollowUpData();
                fup.setPatientId(rs.getStringOrNull(1));
                fup.setBaseDate(rs.getDateOrNull(2));
                fup.setFollowUpName("survey 3mn");
                fup.setFollowUpDate(surveyDate);
                followUps.add(fup);
              }
              return followUps;
            }
        );
  }

  private void createFollowUp(Database database, String patientId, Date followUpDate, String followUpName, String survey)
      throws Exception {
    // By convention, stand alone surveys are at 11:59:59 PM
    Calendar cal = Calendar.getInstance();
    cal.setTime(followUpDate);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
    Date surveyDate = cal.getTime();

    String surveyType = getSurveyTypeFor(survey, surveyDate);

    // Get existing registrations for the patient on the follow up date
    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<ApptRegistration> regs = assessDao.getApptRegistrationByPatientAndDate(patientId, surveyDate);

    // See if an existing registration has already been created for the visit type
    ApptRegistration reg = null;
    for(ApptRegistration sr : regs) {
      if (followUpName.equals(sr.getVisitType())) {
        reg = sr;
      }
    }

    SurveyRegUtils surveyRegUtilsm = new SurveyRegUtils(siteInfo);
    if (reg != null) {
      // If found check if the survey type needs to be changed
      logger.debug("Existing appt found, survey type: " + reg.getSurveyType() +
          ", visit type: " + reg.getVisitType() + ", appt: " + reg.getApptId());
      if (!surveyType.equals(reg.getSurveyType())) {
        logger.debug("Updating survey type to " + surveyType);
        try {
          surveyRegUtilsm.changeSurveyType(database, reg.getAssessment(), surveyType, ServerUtils.getAdminUser(database));
        } catch (IllegalArgumentException e) {
          // Can not update because the survey has been started
          logger.debug("Can not modify survey registration for encounterEid " + reg.getEncounterEid() + " because survey has been started");
        }
      }
    } else {
      // If not found then create a new registration
      reg = new ApptRegistration(siteId, patientId, surveyDate,
          null, surveyType, Constants.REGISTRATION_TYPE_STANDALONE_SURVEY, followUpName);
      surveyRegUtilsm.createRegistration(new AssessDao(database, siteInfo), reg);
      logger.debug("Creating new appt, survey type: " + reg.getSurveyType() +
          ", visit type: " + reg.getVisitType() + ", appt: " + reg.getApptId());
    }
  }

  private String getSurveyTypeFor(String name, Date apptDate) throws Exception {
    XMLFileUtils xmlFileUtils = XMLFileUtils.getInstance(siteInfo);
    String surveyType = xmlFileUtils.getActiveProcessForName(name, apptDate);
    if (surveyType == null) {
      throw new Exception("Process not found for survey name " + name + " and date " + apptDate);
    }
    return surveyType;
  }

  private class FollowUpData {
    String patientId;
    Date baseDate;
    String followUpName;
    Date followUpDate;

     String getPatientId() {
      return patientId;
    }
    void setPatientId(String patientId) {
      this.patientId = patientId;
    }
    Date getBaseDate() {
      return baseDate;
    }
    void setBaseDate(Date baseDate) {
      this.baseDate = baseDate;
    }
    String getFollowUpName() {
      return followUpName;
    }
    void setFollowUpName(String followUpName) {
      this.followUpName = followUpName;
    }
    Date getFollowUpDate() {
      return followUpDate;
    }
    void setFollowUpDate(Date followUpDate) {
      this.followUpDate = followUpDate;
    }
  }
}
