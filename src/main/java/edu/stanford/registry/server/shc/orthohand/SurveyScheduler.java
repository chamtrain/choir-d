/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.orthohand;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.ServiceUnavailableException;

/**
 * Created by tpacht on 10/27/2015.
 */
public class SurveyScheduler {
  private static final Logger logger = Logger.getLogger(SurveyScheduler.class);
  protected final Database database;
  protected final Long siteId;
  protected final SiteInfo siteInfo;

  public SurveyScheduler(Database database, SiteInfo siteInfo) {
    this.database = database;
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
  }

  /**
   * Create stand alone surveys for patients whose last appointment
   * was 90, 180 or 365 days prior to the date provided or last stand alone
   * survey was 365 days prior (for repeating yearly followups).
   * @param toDate The survey date of the standalone surveys to be created
   */
  public void scheduleSurveys(Date toDate) {
    Integer scheduledDaysOut[] = { 90, 180, 365 };
    for (Integer scheduledDayOut : scheduledDaysOut) {
      Date lastApptDayToFind = DateUtils.getDaysFromDate(siteInfo, toDate, -1 * scheduledDayOut);
      buildOutStandaloneSurveys(lastApptDayToFind, "a", toDate);
    }
    Date lastApptDayToFind = DateUtils.getDaysFromDate(siteInfo, toDate, -365);
    buildOutStandaloneSurveys(lastApptDayToFind, "s", toDate);
  }

  /**
   * Creates stand alone survey_registrations for patients
   * whose last (not cancelled) survey registration was on the given date.
   * Patients must have an email address and have agreed to participate.
   *
   * @param lastAppointmentDate Date of patients last registration
   * @param lastRegistrationType Type of last survey_registration
   * @param standAloneSurveyDate Date for the new stand alone survey to be created
   * @return The # of surveys created
   * @throws ServiceUnavailableException
   */
  private int buildOutStandaloneSurveys(Date lastAppointmentDate, String lastRegistrationType, Date standAloneSurveyDate)
      throws ServiceUnavailableException {

    logger.debug("buildOutStandaloneSurveys: last appointment day to find for creating schedule surveys is "
        + lastAppointmentDate.toString() + " new survey date is " + standAloneSurveyDate.toString());
    int counts=0;
    try {
      /*
       * Get the patients ids whose last survey registration was on the given date, for the given type and did not have
       * an appointment since then or one scheduled before the date that this standalone survey will be set for
       * Include only consented patients for whom we have an email address.
       */
      Date fromTime = DateUtils.getTimestampStart(siteInfo, lastAppointmentDate);
      Date toTime = DateUtils.getTimestampEnd(siteInfo, lastAppointmentDate);
      Date nextDate = DateUtils.getTimestampEnd(siteInfo, standAloneSurveyDate);
      String sql =
          "SELECT distinct sr.patient_id from appt_registration sr, patient_attribute pa1, patient_attribute pa2 " // , survey_token st
              + "where sr.survey_site_id = :site " // and st.survey_token = sr.token and st.is_complete = 'Y'
              + "and registration_type = ? "
              + "and visit_dt between ? and ? "
              + "and pa1.patient_id = sr.patient_id and pa1.survey_site_id = sr.survey_site_id and pa1.data_name='participatesInSurveys' and pa1.data_value='y' "
              + "and pa2.patient_id = sr.patient_id and pa2.survey_site_id = sr.survey_site_id and pa2.data_name='orthoHandConsent' and pa2.data_value='Y' "
              + " and not exists "
              + " (select * from appt_registration s2 where s2.patient_id = sr.patient_id and s2.survey_site_id = sr.survey_site_id "
              + " and s2.visit_dt > ? and s2.visit_dt < ? and registration_type != 'c')";
      ArrayList<String> patientIds = database.toSelect(sql)
          .argLong(":site", siteId)
          .argString(lastRegistrationType)
          .argDate(fromTime)
          .argDate(toTime)
          .argDate(toTime)
          .argDate(nextDate)
          .query(new RowsHandler<ArrayList<String>>() {
            @Override
            public ArrayList<String> process(Rows rs) {
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
        if (thisPatient != null && consented(thisPatient, true)) {
          String emailAddress = thisPatient.getEmailAddress();
          if (emailAddress != null) {
            counts = counts + createAssessment(nextDate, patientId, emailAddress);
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Error processing scheduled surveys for patients without appointments!", ex);
    }
    logger.info("Created registrations for " + counts + " stand alone schedules. " );
    return counts;
  }

  protected int createAssessment(Date surveyDt, String patientId, String emailAddress) {
    // By convention, stand alone surveys are at 11:59:59 PM
    Calendar cal = Calendar.getInstance();
    cal.setTime(surveyDt);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
    Date surveyDate = cal.getTime();

    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);

    // Get the follow up visit type
    String visitType = surveyRegUtils.getVisitType(1, surveyDt);
    if (visitType == null) {
      logger.error("No visit type found for creating stand alone follow up schedules!");
      return 0;
    }

    // Get existing registrations for the patient on the follow up date
    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<ApptRegistration> regs = assessDao.getApptRegistrationByPatientAndDate(patientId, surveyDate);

    // See if an existing registration has already already been created for the visit type
    ApptRegistration reg = null;
    for (ApptRegistration sr : regs) {
      if (visitType.equals(sr.getVisitType())) {
        reg = sr;  // keep the last one found - the latest
      }
    }

    // If not found then create a new registration
    if (reg == null) {
      reg = new ApptRegistration(siteId, patientId, surveyDate, emailAddress, visitType,
          Constants.REGISTRATION_TYPE_STANDALONE_SURVEY, "STA");
      reg = surveyRegUtils.createRegistration(assessDao, reg);
      logger.debug("Creating new appt, survey type: " + reg.getSurveyType() +
        ", visit type: " + reg.getVisitType() + ", appt: " + reg.getApptId());
      return 1;
    }

    logger.debug("Survey registration already exists for follow up");
    return 0;
  }

  private boolean consented(Patient patient, @SuppressWarnings("unused") boolean isNew) {

    return patient != null && patient.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)
        && "y".equals(patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue());
  }

  /*
  private HashMap<String, String> getTemplates() {
    HashMap<String, String> useTemplateHash = new HashMap<>();
    ArrayList<String> processNames = XMLFileUtils.getInstance().getProcessNames();
    /*
     * Create hash of the process_name and the schedule_template text so the
     * schedule_template emails are used.
     * /
    String templateText = EmailTemplateUtils.getInstance().getTemplate("No-appointment");
    if (templateText != null) {
      for (String processName : processNames) {
        logger.debug("adding templateText for " + processName);
        useTemplateHash.put(processName, templateText);
        useTemplateHash.put("No-appointment", templateText);
      }
    }
    return useTemplateHash;
  } /* */
}

