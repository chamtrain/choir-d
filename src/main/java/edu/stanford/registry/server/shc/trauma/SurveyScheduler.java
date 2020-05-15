/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.trauma;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.ServiceUnavailableException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * Custom follow up survey scheduling for Trauma Outcomes Project.. Created by tpacht on 06/16/2017.
 */
class SurveyScheduler {
  private static final Logger logger = LoggerFactory.getLogger(SurveyScheduler.class);
  private final Database database;
  private final Long siteId;
  private final SiteInfo siteInfo;

  SurveyScheduler(Database database, SiteInfo siteInfo) {
    this.database = database;
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
  }

  /**
   * Create stand alone surveys for patients whose last appointment
   * was 30 days prior to the date provided and it was an Initial or
   * they have completed a  survey within the last 100 days.
   *
   * @param toDate The survey date of the standalone surveys to be created
   */
  public void scheduleSurveys(Date toDate) {
    int FIRST_YEAR[] = {90, 180, 270, 360};
    int followUpSurveyCnt = 0;
    for (int days: FIRST_YEAR) {
      Date lastApptDayToFind = DateUtils.getDaysFromDate(toDate, -1 * days);
      int surveyCnt = buildOutStandaloneSurveys(lastApptDayToFind, toDate);
      logger.debug("{} surveys sent as followups to {} surveys", surveyCnt, DateUtils.getDateStart(lastApptDayToFind));
      followUpSurveyCnt = followUpSurveyCnt + surveyCnt;
    }
    // Every 180 days after that

    Date lastApptDayToFind = DateUtils.getDaysFromDate(toDate, -1 * 540);
    Calendar cal = Calendar.getInstance();
    cal.set(2017, Calendar.SEPTEMBER, 1);
    Date projectStartDate = DateUtils.getDateStart(cal.getTime());
    while (lastApptDayToFind.after(projectStartDate)) {
      int surveyCnt = buildOutStandaloneSurveys(lastApptDayToFind, toDate);
      logger.debug("{} surveys sent as followups to a {}", surveyCnt, DateUtils.getDateStart(lastApptDayToFind));
      followUpSurveyCnt = followUpSurveyCnt + surveyCnt;
    }

    logger.debug("{} Trauma follow up surveys created", followUpSurveyCnt);
  }

  /**
   * Creates stand alone survey_registrations for patients
   * whose initial (not cancelled) survey registration was on the given date.
   * Patients must have an email address and have agreed to participate.
   *
   * @param lastAppointmentDate  Date of patients initial registration
   * @param standAloneSurveyDate Date for the new stand alone survey to be created
   * @return The # of surveys created
   */
  private int buildOutStandaloneSurveys(Date lastAppointmentDate, Date standAloneSurveyDate)
      throws ServiceUnavailableException {

    TraumaUtils utils = new TraumaUtils(siteInfo);
    // checks that the type completed was an Initial

    logger.debug("buildOutStandaloneSurveys: last appointment day to find for creating schedule surveys is {} new survey date is {}",
        lastAppointmentDate.toString(), standAloneSurveyDate.toString());
    int patientCounts = 0;
    int familyCounts = 0;
    try {
      String patientFollowUpType = utils.getSurveyType("PatientFollowup");
      String familyFollowUpType = utils.getSurveyType("FamilyFollowup");
      /*
       * Get the patients ids whose initial survey registration was on the given date, for the given type and did not have
       * an appointment since then or one scheduled before the date that this standalone survey will be set for
       * Include only consented patients for whom we have an email address.
       */
      Date fromTime = DateUtils.getTimestampStart(lastAppointmentDate);
      Date toTime = DateUtils.getTimestampEnd(lastAppointmentDate);
      Date nextDate = DateUtils.getTimestampEnd(standAloneSurveyDate);
      String sql =
          "SELECT distinct sr.patient_id from appt_registration ar, survey_registration sr, survey_token st, patient_attribute pa1, patient_attribute pa2 "
              // , survey_token st
              + "where ar.survey_site_id = :site " // and st.survey_token = ar.token and st.is_complete = 'Y'
              + "and registration_type != ? "
              + "and visit_dt between ? and ? "
              + "and pa1.patient_id = ar.patient_id and pa1.survey_site_id = ar.survey_site_id and pa1.data_name='participatesInSurveys' and pa1.data_value='y' "
              + "and pa2.patient_id = ar.patient_id and pa2.survey_site_id = ar.survey_site_id and pa2.data_name='traumaConsent' and pa2.data_value='Y' "
              + "and sr.survey_type like ? and ar.assessment_reg_id = sr.assessment_reg_id and ar.survey_site_id = sr.survey_site_id "
              + "and sr.token = st.survey_token and sr.survey_site_id = st.survey_site_id and st.is_complete = ? "
              + " and not exists "
              + " (select * from appt_registration s2 where s2.patient_id = ar.patient_id and s2.survey_site_id = ar.survey_site_id "
              + " and s2.visit_dt = ? and registration_type != 'c')";
      ArrayList<String> patientIds = database.toSelect(sql)
          .argLong(":site", siteId)
          .argString("c")
          .argDate(fromTime)
          .argDate(toTime)
          .argString("%Initial%")
          .argString("Y")
          .argDate(nextDate)
          .query(new RowsHandler<ArrayList<String>>() {
            @Override
            public ArrayList<String> process(Rows rs) throws Exception {
              ArrayList<String> patients = new ArrayList<>();
              while (rs.next()) {
                patients.add(rs.getStringOrEmpty(1));
              }
              return patients;
            }
          });

      /*
       * Create the standalone survey registrations
       */
      PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));


      for (String patientId : patientIds) {
        Patient thisPatient = patAttribDao.getPatient(patientId);
        if (thisPatient != null && consented(thisPatient)) {
          String emailAddress = thisPatient.getEmailAddress();
          if (emailAddress != null) {
            ApptRegistration appt = utils.createAssessment(nextDate, patientId, emailAddress, patientFollowUpType, database);
            patientCounts = patientCounts +  (appt == null ? 0 : 1);
          }
          if (thisPatient.hasAttribute("Family2")) {
            ApptRegistration appt = utils.createAssessment(nextDate, patientId, thisPatient.getAttribute("Family2").getDataValue(), familyFollowUpType, database);
            familyCounts = familyCounts + (appt == null ? 0 : 1);
          }
          if (thisPatient.hasAttribute("Family4")) {
            ApptRegistration appt = utils.createAssessment(nextDate, patientId, thisPatient.getAttribute("Family4").getDataValue(), familyFollowUpType, database);
            familyCounts = familyCounts + (appt == null ? 0 : 1);
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Error processing scheduled surveys for patients without appointments!", ex);
    }
    logger.info("Created {} patient follow up registrations  ", patientCounts);
    logger.info("Created {} family follow up registrations  ", familyCounts);
    return patientCounts + familyCounts;
  }



  private boolean consented(Patient patient) {

    return patient != null && patient.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)
        && "y".equals(patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue());
  }
}

