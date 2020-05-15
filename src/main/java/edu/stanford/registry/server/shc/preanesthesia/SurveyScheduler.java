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
package edu.stanford.registry.server.shc.preanesthesia;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveyRegistrationAttributeDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.SurveyRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * Created by tpacht on 02/09/2016.
 */
public class SurveyScheduler {
  final static Logger logger = Logger.getLogger(SurveyScheduler.class);
  final Database database;
  final Long siteId;
  final SiteInfo siteInfo;
  final SurveyRegistrationAttributeDao srAttrDao;
  ArrayList<String> processNames = null;

  public SurveyScheduler(Database database, SiteInfo siteInfo) {
    this.database = database;
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
    this.srAttrDao = new SurveyRegistrationAttributeDao(database);
  }

  /**
   * Create stand alone surveys for patients whose Initial appointment
   * was 1, 2, 3, and 4 weeks, 3 and 6 months prior to the date provided.
   * @param toDate The survey date of the standalone surveys to be created
   */
  public void scheduleSurveys(Date toDate) {
    Integer scheduledWeeksOut[] = { 14, 21, 28, 35 };

    for (int i = 0; i < scheduledWeeksOut.length; i++) {
      Date lastApptDayToFind = DateUtils.getDaysFromDate(toDate, -1 * scheduledWeeksOut[i]);
      buildOutStandaloneSurveys(lastApptDayToFind, "a", toDate, getProcessName("PostOpWkly"));
    }

    Integer scheduledQuarterlyOut[] = { 98, 188 };
    for (int i = 0; i < scheduledQuarterlyOut.length; i++) {
      Date lastApptDayToFind = DateUtils.getDaysFromDate(toDate, -1 * scheduledQuarterlyOut[i]);
      buildOutStandaloneSurveys(lastApptDayToFind, "a", toDate, getProcessName("PostOpQtly"));
    }

    bulkSendPostOpBacklog(toDate, getProcessName("PostOpWkly"));
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
  private int buildOutStandaloneSurveys(Date lastAppointmentDate, String lastRegistrationType, Date standAloneSurveyDate, String surveyType)
      throws ServiceUnavailableException {

    logger.debug("buildOutStandaloneSurveys: last appointment day to find for creating schedule surveys is "
        + lastAppointmentDate.toString() + " new survey date is " + standAloneSurveyDate.toString());
    int counts=0;
    try {
      /**
       * Get the patients ids whose last survey registration was on the given date, for the given type and did not have
       * an appointment since then or one scheduled before the date that this standalone survey will be set for
       * Include only consented patients for whom we have an email address, are set to participatesInSurveys
       * and have agreed to the follow up surveys.
       */
      Date fromTime = DateUtils.getTimestampStart(lastAppointmentDate);
      Date toTime = DateUtils.getTimestampEnd(lastAppointmentDate);
      Date nextDate = DateUtils.getTimestampEnd(standAloneSurveyDate);
      String sql =
          "SELECT distinct sr.patient_id, sr.appt_reg_id from appt_registration sr, patient_attribute pa1, patient_attribute pa2 " // , survey_token st
              + "where sr.survey_site_id = :site " // and st.survey_token = sr.token and st.is_complete = 'Y'
              + "and registration_type = ? "
              + "and visit_dt between ? and ? "
              + "and pa1.patient_id = sr.patient_id and pa1.survey_site_id = sr.survey_site_id and pa1.data_name='participatesInSurveys' and pa1.data_value='y' "
              + "and pa2.patient_id = sr.patient_id and pa2.survey_site_id = sr.survey_site_id and pa2.data_name='pacFollowUp' and pa2.data_value='Y' "
              + " and not exists "
              + " (select * from appt_registration s2 where s2.patient_id = sr.patient_id and s2.survey_site_id = sr.survey_site_id "
              + " and s2.visit_dt > ? and s2.visit_dt < ? and registration_type = 'a')"
              + " and not exists "
              + " (select * from appt_registration s2 where s2.patient_id = sr.patient_id and s2.survey_site_id = sr.survey_site_id "
              + " and s2.visit_dt > ? and s2.visit_dt < ? and registration_type = 's')";
      ArrayList<AppointmentRegistration> appointmentRegistrationList = database.toSelect(sql)
          .argLong(":site", siteId)
          .argString(lastRegistrationType)
          .argDate(fromTime)
          .argDate(toTime)
          .argDate(toTime)
          .argDate(nextDate)
          .argDate(DateUtils.getDaysFromDate(siteInfo, nextDate , -6 ))
          .argDate(nextDate)
          .query(new RowsHandler<ArrayList<AppointmentRegistration>>() {
            @Override
            public ArrayList<AppointmentRegistration> process(Rows rs) throws Exception {
              ArrayList<AppointmentRegistration> appointmentRegistrations = new ArrayList<AppointmentRegistration>();
              while (rs.next()) {
                AppointmentRegistration apptReg = new AppointmentRegistration();
                apptReg.patientId = rs.getStringOrEmpty(1);
                apptReg.apptRegId = rs.getLongOrNull(2);
                appointmentRegistrations.add(apptReg);
              }
              return appointmentRegistrations;
            }
          });

      /**
       * Create the standalone survey registrations
       */
      PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));

      for (int p = 0; p < appointmentRegistrationList.size(); p++) {
        Patient thisPatient = patAttribDao.getPatient(appointmentRegistrationList.get(p).patientId) ;
        if (thisPatient != null && consented(thisPatient, true)) {
          String emailAddress = thisPatient.getEmailAddress();
          if (emailAddress != null) {
              counts = counts + createAssessment(nextDate, appointmentRegistrationList.get(p), emailAddress, surveyType);
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Error creating a post-op survey for patient with a " + standAloneSurveyDate + " appointments!", ex);
    }
    logger.info("Created registrations for " + counts + " stand alone schedules. " );
    return counts;
  }

  protected int createAssessment(Date surveyDt, AppointmentRegistration apptReg, String emailAddress, String surveyType) {
    // By convention, stand alone surveys are at 11:59:59 PM
    Calendar cal = Calendar.getInstance();
    cal.setTime(surveyDt);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
    Date surveyDate = cal.getTime();

    // Get the follow up visit type

    // Get existing registrations for the patient on the follow up date
    String patientId = apptReg.patientId;
    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<ApptRegistration> regs = assessDao.getApptRegistrationByPatientAndDate(patientId, surveyDate);

    // See if an existing registration has already already been created for the visit type
    ApptRegistration reg = null;
    for(ApptRegistration sr : regs) {
      if (surveyType.equals(sr.getVisitType())) {
        reg = sr;
      }
    }

    // If not found then create a new registration
    if (reg == null) {
      reg = new ApptRegistration(siteId, patientId, surveyDate, emailAddress, surveyType,
          Constants.REGISTRATION_TYPE_STANDALONE_SURVEY, "STA");
      SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
      reg = surveyRegUtils.createRegistration(assessDao, reg);
      for(SurveyRegistration surveyReg : reg.getSurveyRegList()) {
        srAttrDao.setAttribute(surveyReg.getSurveyRegId(), "apptRegId", apptReg.apptRegId.toString());
      }
      logger.debug("Creating new appointment registration, survey type: " + reg.getSurveyType() +
        ", visit type: " + reg.getVisitType() + ", apptRegId: " + reg.getApptRegId());
      return 1;
    }

    logger.debug("Survey registration already exists for follow up");
    return 0;
  }

  private boolean consented(Patient patient, @SuppressWarnings("unused") boolean isNew) {

    if (patient != null && patient.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)
        && "y".equals(patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue())) {
        return true;
    }
    return false;
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
  public static class AppointmentRegistration {
    public String patientId;
    public Long apptRegId;
  }

  private String getProcessName(String startingString) {
    if (processNames == null) {
      processNames = XMLFileUtils.getInstance(siteInfo).getActiveVisitProcessNames();
    }

    if (processNames != null) {
      for (String name : processNames) {
        if (name.startsWith(startingString)) {
          return name;
        }
      }
    }
    return startingString;
  }

  /* Unused???
  static class SiteContext {
    final PatientDao patAttribDao;
    final SiteInfo siteInfo;
    SiteContext(Database db, SiteInfo siteInfo) {
      this.siteId = siteId;
      User admin = ServerUtils.getAdminUser(db, siteId);
      patAttribDao = new PatientDao(db, siteId, admin);
    }
  }
  static class SiteContexts {
    Hashtable<Long,SiteContext> contexts = new Hashtable<Long,SiteContext>();
    final Database db;
    SiteContexts(Database dbase) {
      db = dbase;
    }

  } /* */

  private void bulkSendPostOpBacklog(Date standAloneSurveyDate, String surveyType) {
    AppConfigDao appConfigDao = new AppConfigDao(database, ServerUtils.getAdminUser(database));
    AppConfigEntry appConfigEntry = appConfigDao.findAppConfigEntry(siteId, "custom", "buildEmail");
    int counts = 0;
    if (appConfigEntry != null && "Y".equals(appConfigEntry.getConfigValue())) {

      try {
        ArrayList<AppointmentRegistration> appointmentRegistrationList = getBulkSendQualifyingRegistrations();
        if (appointmentRegistrationList == null || appointmentRegistrationList.size() == 0) {
          return;
        }

        /**
         * Create the standalone survey registrations
         */
        Date nextDate = DateUtils.getTimestampEnd(standAloneSurveyDate);
        PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));

        for (AppointmentRegistration pp: appointmentRegistrationList) {
          Patient thisPatient = patAttribDao.getPatient(pp.patientId);
          if (thisPatient != null && consented(thisPatient, true)) {
            String emailAddress = thisPatient.getEmailAddress();
            if (emailAddress != null) {
              counts = counts + createAssessment(nextDate, pp, emailAddress, surveyType);
            }
          }
        }
      } catch(Exception ex){
        logger.error(
            "Error creating a post-op survey for patient with a " + standAloneSurveyDate + " appointments!", ex);
      }
      logger.info("Created " + counts + " additional PostOp registrations for consented patients that had not yet been sent one. ");
    }

  }

  private ArrayList<AppointmentRegistration> getBulkSendQualifyingRegistrations() {

    String sql = "select ar.patient_id, max(ar.appt_reg_id) from appt_registration ar, survey_token st, survey_registration sr, patient_attribute pa, patient_attribute pe, patient_attribute pf "
        + " where ar.survey_site_id = :site  and ar.visit_dt < (trunc(sysdate)-7) "
        + "  and ar.ASSESSMENT_REG_ID = sr.ASSESSMENT_REG_ID and ar.survey_site_id = sr.survey_site_id and sr.survey_type not like 'Post%' "
        + "  and st.survey_site_id = sr.survey_site_id and st.survey_token = sr.token and IS_COMPLETE = 'Y' "
        + "  and sr.patient_id = pa.patient_id and sr.survey_site_id = pa.survey_site_id and pa.data_name = 'participatesInSurveys' and pa.data_value = 'y' "
        + "  and sr.patient_id = pe.patient_id and sr.survey_site_id = pe.survey_site_id and pe.data_name = 'surveyEmailAddress' "
        + "  and sr.patient_id = pf.patient_id and sr.survey_site_id = pf.survey_site_id and pf.data_name = 'pacFollowUp' and pf.data_value = 'Y' "
        + "  and not exists (select * from survey_registration srp where srp.survey_type like 'PostOp%' "
        + "                  and srp.patient_id = ar.patient_id and srp.survey_site_id = ar.survey_site_id ) "
        + "  and not exists (select * from survey_registration srp where survey_dt >= (trunc(sysdate) - 14)  "
        + "                  and srp.patient_id = ar.patient_id and srp.survey_site_id = ar.survey_site_id ) "
        + "                  group by ar.patient_id";
    ArrayList<AppointmentRegistration> registrations = database.toSelect(sql)
        .argLong(":site", siteId)
        .query(new RowsHandler<ArrayList<AppointmentRegistration>>() {
          @Override
          public ArrayList<AppointmentRegistration> process(Rows rs) throws Exception {
            ArrayList<AppointmentRegistration> appointmentRegistrations = new ArrayList<AppointmentRegistration>();
            while (rs.next()) {
              AppointmentRegistration apptReg = new AppointmentRegistration();
              apptReg.patientId = rs.getStringOrEmpty(1);
              apptReg.apptRegId = rs.getLongOrNull(2);
              appointmentRegistrations.add(apptReg);
            }
            return appointmentRegistrations;
          }
        });
    return registrations;
  }
}

