/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server;

import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveyRegistrationAttributeDao;
import edu.stanford.registry.server.survey.AngerService;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.ConfigParam;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.SurveyRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

/**
 * Created by tpacht   04/2018.
 */
public class PainSurveyScheduler {
  private final static Logger logger = LoggerFactory.getLogger(PainSurveyScheduler.class);
  private final Database database;
  private final Long siteId;
  private final SiteInfo siteInfo;
  private ArrayList<String> processNames = null;
  private final SurveyRegistrationAttributeDao surveyRegAttrDao;
  private final SimpleDateFormat dateFormat;

  public static final String LUMBAREPI= "ProcLumbarEpi";
  public static final String ACUPUNCT = "ProcAccupunct";
  public static final String NERVEBLOCK = "ProcNerveBlock";
  public static final String SCS = "ProcSpinalCordStim";
  public static final String PERINERVE = "ProcPeriNerveStim";

  public PainSurveyScheduler(Database database, SiteInfo siteInfo) {
    this.database = database;
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
    this.surveyRegAttrDao = new SurveyRegistrationAttributeDao(database);
    this.dateFormat = DateUtils.newDateFormat(siteInfo, "yyyy-MM-dd hh:mm:ss.S");
  }

  /**
   * Create stand alone follow up surveys
   *
   * @param surveyDate The survey date of the standalone surveys to be created
   */
  public void scheduleSurveys(Date surveyDate) {
    surveyDate = endOfDay(surveyDate); // set it to 0 millis or the email won't send until tomorrow
    sendRandomSetFollowUps(surveyDate);
    sendTraitAngerFollowUps(surveyDate);
    sendProcedureFollowUps(surveyDate);
  }

  private void sendRandomSetFollowUps(Date surveyDate) {
    Calendar assignedDate = Calendar.getInstance();
    assignedDate.setTime(new Date());
    endOfDay(assignedDate); // set it to 11:59 pm
    int counts = 0;

    /*
     * Send surveys to patients assigned to the DDTTreatmentSet on or about this day for each of the last 6 months
     */
    for (int i = 0; i < 6; i++) {
      assignedDate.add(Calendar.MONTH, -1);
      counts += buildOutTreatmentSetSurveys(DateUtils.getDateEnd(siteInfo, assignedDate.getTime()), surveyDate,
          "DDTreatmentSet" );
    }
    logger.info("Created DDTtreatment set registrations for {} patients ", counts);

    /*
     * Send follow up surveys to patients assigned to the HF10 randomization at 1-3-6-9-12-18-24-30-36 months
     */
    int countHF10 = 0;
    String setnameHF10 = "HF10vBurstDR";
    Calendar assignedDateHF10 = Calendar.getInstance();
    int[] monthsHF10 = {-1, -3, -6, -9, -12, -18, -24, -30, -36};
    for (int monthsBack : monthsHF10) {
      assignedDateHF10.setTime(new Date());
      endOfDay(assignedDate); // set it to 11:59 pm
      assignedDateHF10.add(Calendar.MONTH, monthsBack);
      countHF10 += buildOutTreatmentSetSurveys(DateUtils.getDateEnd(siteInfo, assignedDateHF10.getTime()), surveyDate, setnameHF10);
    }
    logger.info("Created {} registrations for {} treatment set patients", setnameHF10, countHF10);
  }

  /**
   * Creates stand alone survey_registrations for patients
   * who were assigned to a treatment set in a given date range.
   * Patients must have an email address and have agreed to participate.
   *
   * @param assignedDate Date patient was assigned into a treatment set
   * @param surveyDate   Date of the 0survey_registration to be created
   * @param setName Name of the treatment set
   * @return The # of surveys created
   */
  private int buildOutTreatmentSetSurveys(final Date assignedDate, final Date surveyDate, final String setName) {

    logger.debug("buildOutTreatmentSetSurveys: look for patients assigned in treatment set surveys  "
        + " {} to send surveys dated {} ", dateFormat.format(assignedDate), dateFormat.format(surveyDate));
    int counts = 0;
    try {
      /*
       * Find the consented patients (set to 'y' participatesInSurveys) assigned to a treatment for the given date (and
       * three days prior). The overlap will pick up the days missed due to varying number of days in a month and
       * just in case the job wasn't run for some reason.
       */
      Calendar fromTime = DateUtils.getCalendarDayStart(siteInfo, assignedDate);
      fromTime.add(Calendar.DAY_OF_MONTH, -3); // look back 3 days as well

      String sql;
      if ("HF10vBurstDR".equals(setName)) {
        sql = "SELECT rps.patient_id, rps.set_name FROM "
            + "  (SELECT sr.patient_id, rp.set_name , MIN(sr.survey_dt) survey_dt FROM survey_registration sr, randomset_participant rp "
            + "   WHERE sr.survey_site_id = rp.survey_site_id AND sr.survey_site_id = :site "
            + "   AND sr.survey_type like 'HF10vBurstDR%' "
            + "   AND sr.patient_id= rp.patient_id "
            + "   AND rp.state='Assigned' "
            + "   AND rp.set_name = ? GROUP BY sr.patient_id, rp.set_name) rps "
            + "WHERE rps.survey_dt >= ? AND rps.survey_dt <= ? ";
      } else {
        sql = "SELECT rp.patient_id, rp.set_name from randomset_participant rp  "
            + "WHERE rp.survey_site_id = :site  "
            + "  and rp.state = 'Assigned'  "
            + "  and rp.set_name = ? "
            + "  and rp.dt_assigned between ? and ? ";
      }
      ArrayList<AppointmentRegistration> appointmentRegistrationList = database.toSelect(sql)
          .argLong(":site", siteId)
          .argString(setName)
          .argDate(fromTime.getTime())
          .argDate(assignedDate)
          .query(rs -> {
            ArrayList<AppointmentRegistration> appointmentRegistrations = new ArrayList<>();
            while (rs.next()) {
              AppointmentRegistration apptReg = new AppointmentRegistration();
              apptReg.patientId = rs.getStringOrEmpty(1);
              apptReg.setName = rs.getStringOrEmpty(2);
              appointmentRegistrations.add(apptReg);
            }
            return appointmentRegistrations;
          });
      logger.debug("buildOutTreatmentSetSurveys: found {} patients for {}", appointmentRegistrationList.size(), setName);

      /*
       *  Make sure the patient is registered, has an email address and hasn't been sent a survey for this treatment set
       *  within 27 days of this surveys date, and then create them a standalone survey registration
       */
      if (appointmentRegistrationList.size() > 0) {
        SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
        PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));

        for (AppointmentRegistration appt : appointmentRegistrationList) {
          Patient thisPatient = patAttribDao.getPatient(appt.patientId);
          if (thisPatient.hasConsented()
              && thisPatient.hasValidEmail() && (thisPatient.getEmailAddress() != null)
              && !hasRecentAssessment(appt.patientId, appt.setName, surveyDate)) {
            counts = counts + createAssessment(surveyRegUtils, surveyDate, appt, thisPatient.getEmailAddress());
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Error creating a treatment set survey for patient registered in treatmentset on {}",
          dateFormat.format(assignedDate), ex);
    }
    return counts;
  }

  private boolean hasRecentAssessment(String patientId, final String setName, Date surveyDate) {

    Date fromTime = DateUtils.getDaysFromDate(siteInfo, surveyDate, -27); // not in the last few weeks
    String sql =
        "SELECT survey_type from survey_registration    "
            + "WHERE  survey_site_id = :site  and patient_id = ? and survey_dt between ? and ?  ";
    return database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(patientId)
        .argDate(fromTime)
        .argDate(surveyDate)
        .query(rs -> {
          while (rs.next()) {
            if (rs.getStringOrEmpty(1).startsWith(setName)) {
              logger.debug("buildOutTreatmentSetSurveys: found a {} survey for {} so not creating another", setName, patientId);
              return true;
            }
          }
          return false;
        });

  }

  private void sendTraitAngerFollowUps(Date surveyDate) {
    Calendar assignedDate = Calendar.getInstance();
    assignedDate.setTime(new Date());
    endOfDay(assignedDate); // set it to 11:59 pm
    int counts = 0;
    // Send surveys to patients that consented to the follow ups on their initial survey 3 months prior


    assignedDate.add(Calendar.MONTH, -3);
    counts += buildOutAngerSurveys(DateUtils.getDateEnd(siteInfo, assignedDate.getTime()), surveyDate);

    logger.info("Created trait anger registrations for {} patients ", counts);
  }

  private int buildOutAngerSurveys( final Date assignedDate, final Date surveyDate ) {
    int counts = 0;
    try {
      /*
       * Find the consented patients (set to 'Y' followTraitAngerConsent) who consented on the given date (and
       * three days prior). The overlap will pick up the days missed due to varying number of days in a month and
       * just in case the job wasn't run for some reason.
       */
      Calendar fromTime = DateUtils.getCalendarDayStart(siteInfo, assignedDate);
      fromTime.add(Calendar.DAY_OF_MONTH, -3); // look back 3 days as well

      String sql =
          "SELECT pa.patient_id  from patient_attribute pa "
              + "WHERE pa.survey_site_id = :site  "
              + "  and pa.data_name = ? "
              + "  and pa.data_value = ? "
              + "  and pa.dt_created between ? and ?";
      ArrayList<String> patientList = database.toSelect(sql)
          .argLong(":site", siteId)
          .argString(AngerService.followConsent )
          .argString("Y")
          .argDate(fromTime.getTime())
          .argDate(assignedDate)
          .query(rs -> {
            ArrayList<String> patients = new ArrayList<>();
            while (rs.next()) {

              patients.add(rs.getStringOrEmpty(1));

            }
            return patients;
          });
      logger.debug("buildOutAngerSurveys: found {} patients between {} and {}", patientList.size(), fromTime, assignedDate);
      /*
       *  Make sure the patient is registered, has an email address and hasn't been sent a survey for this treatment set
       *  within 27 days of this surveys date, and then create them a standalone survey registration
       */
      if (patientList.size() > 0) {
        SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
        PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));

        for (String patientId : patientList) {
          Patient thisPatient = patAttribDao.getPatient(patientId);
          if (thisPatient.hasConsented()
              && thisPatient.hasValidEmail() && (thisPatient.getEmailAddress() != null)
              && !hasRecentAssessment(patientId, "AngerFollowUp", surveyDate)) {
            AppointmentRegistration apptReg = new AppointmentRegistration();
            apptReg.patientId  = patientId;
            apptReg.setName = "AngerFollowUp";
            counts = counts + createAssessment(surveyRegUtils, surveyDate, apptReg, thisPatient.getEmailAddress());
          } else {
            if (!thisPatient.hasConsented())
            logger.debug("Skipping Anger Follow Up. Patient has not consented");
            if (!thisPatient.hasValidEmail())
              logger.debug("Skipping Anger Follow Up. Patient does not have a valid email");
            else if (thisPatient.getEmailAddress() == null)
              logger.debug("Skipping Anger Follow Up. Patients email address is null");
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Error creating an Anger survey for patient "
          + assignedDate.toString(), ex);
    }
    return counts;
  }

  private void sendProcedureFollowUps(Date surveyDate) {
    Calendar assignedDate = Calendar.getInstance();
    assignedDate.setTime(new Date());
    endOfDay(assignedDate); // set it to 11:59 pm
    int counts = 0;

    // If the configuration variable is there; send follow-ups of the Botox procedure surveys every 6 weeks
    AppConfigDao configDao = new AppConfigDao(database);
    AppConfigEntry configSend = configDao.findAppConfigEntry(siteId, ConfigParam.CONFIG_TYPE,  PainManagementCustomizer.CONFIG_SEND_BOTOX);
    if (configSend != null && configSend.isEnabled()) {
      int stopDays = 9999;
      AppConfigEntry configStop = configDao.findAppConfigEntry(siteId, ConfigParam.CONFIG_TYPE, PainManagementCustomizer.CONFIG_STOP_BOTOX);
      if (configStop != null && configStop.isEnabled()) {
        try {
          stopDays = Integer.parseInt(configStop.getConfigValue());
        } catch (NumberFormatException nfe) {
          logger.error("invalid configuration value {} for {}", configStop.getConfigValue(), PainManagementCustomizer.CONFIG_STOP_BOTOX);
        }
      }
      try {
        assignedDate.add(Calendar.DAY_OF_YEAR, -1 * Integer.parseInt(configSend.getConfigValue()));
        counts += buildOutProcedureFollowUps(DateUtils.getDateEnd(siteInfo, assignedDate.getTime()), surveyDate, "ProcBotox", stopDays);
        logger.info("Created {} follow up surveys for {} patients ", "ProcBotox", counts);
      } catch (NumberFormatException nfe) {
        logger.error("invalid configuration value {} for {}", configSend.getConfigValue(), PainManagementCustomizer.CONFIG_SEND_BOTOX);
      }
    }

    // Acupuncture: First one 2 weeks after (sent manually) then every 2 weeks (14 days) for 3 months (91 days)
    try {
      int acuCounts = 0;
      Calendar acuDate = Calendar.getInstance();
      acuDate.setTime(surveyDate);
      endOfDay(acuDate); // set it to 11:59 pm
      acuDate.add(Calendar.DAY_OF_YEAR, -14);
      acuCounts += buildOutProcedureFollowUps(DateUtils.getDateEnd(siteInfo, acuDate.getTime()), surveyDate, ACUPUNCT, 91, true);
      logger.info("Created {} follow up surveys for {} patients ", ACUPUNCT, acuCounts);
    } catch (NumberFormatException nfe) {
      logger.error("Error sending {} follow ups", ACUPUNCT, nfe);
    }

    // LumbarEpi: First one 2 weeks after (sent manually) then every 2 weeks (14 days) for 3 months (91 days)
    try {
      int lumCounts = 0;
      Calendar lumbarDate = Calendar.getInstance();
      lumbarDate.setTime(surveyDate);
      endOfDay(lumbarDate); // set it to 11:59 pm
      lumbarDate.add(Calendar.DAY_OF_YEAR, -14);
      lumCounts += buildOutProcedureFollowUps(DateUtils.getDateEnd(siteInfo, lumbarDate.getTime()), surveyDate, LUMBAREPI, 91, true);
      logger.info("Created {} follow up surveys for {} patients ", LUMBAREPI, lumCounts);
    } catch (NumberFormatException nfe) {
      logger.error("Error sending {} follow ups", LUMBAREPI, nfe);
    }

    // Nerve block twice. First is 48 hours (sent manually) second is 2 weeks after (12 days after first)
    try {
      int nervCounts = 0;
      Calendar nervDate = Calendar.getInstance();
      nervDate.setTime(surveyDate);
      endOfDay(nervDate); // set it to 11:59 pm
      nervDate.add(Calendar.DAY_OF_YEAR, -12);
      nervCounts += buildOutProcedureFollowUps(DateUtils.getDateEnd(siteInfo, nervDate.getTime()), surveyDate, NERVEBLOCK, 14, true);
      logger.info("Created {} follow up surveys for {} patients ", NERVEBLOCK, nervCounts);
    } catch (NumberFormatException nfe) {
      logger.error("Error sending {} follow ups", NERVEBLOCK, nfe);
    }

    // SCS Trial Three times; 3 (sent manually) AND 5 AND 7 days after so followups are 2 and 4 days after the initial
    try {
      Calendar scsDate = Calendar.getInstance();
      scsDate.setTime(surveyDate);
      endOfDay(scsDate); // set it to 11:59 pm
      scsDate.add(Calendar.DAY_OF_YEAR, -2);
      int scsCounts = sendProcedureFollowUp(DateUtils.getDateEnd(siteInfo, scsDate.getTime()), surveyDate, SCS, "SCSFU");
      scsDate.add(Calendar.DAY_OF_YEAR, -2);
      scsCounts += sendProcedureFollowUp(DateUtils.getDateEnd(siteInfo, scsDate.getTime()), surveyDate, SCS, "SCSFU");
      logger.info("Created {} SCSFU surveys for {} patients ", scsCounts, SCS);
    } catch (Exception ex) {
      logger.error("Error sending {} follow ups", SCS, ex);
    }

    // Peripheral Nerve Stim: First one 2 weeks (sent manually), second at 4 weeks, and every three months for 5 years
    try {
      // second one 4 weeks (14 days after the first)
      Calendar periDate = Calendar.getInstance();
      periDate.setTime(surveyDate);
      endOfDay(periDate);
      periDate.add(Calendar.DAY_OF_YEAR, -14);
      int periCounts = sendProcedureFollowUp(DateUtils.getDateEnd(siteInfo, periDate.getTime()), surveyDate, PERINERVE, "PNSFU");
      logger.info("Created {} surveys with visitType PNSFU for patients sent a {} survey two weeks ago", periCounts, PERINERVE);
      // then every three months for 5 years
      Calendar peri3Date = Calendar.getInstance();
      peri3Date.setTime(surveyDate);
      endOfDay(peri3Date); // set it to 11:59 pm
      peri3Date.add(Calendar.DAY_OF_YEAR, - 90);
      periCounts += buildOutProcedureFollowUps(DateUtils.getDateEnd(siteInfo, peri3Date.getTime()), surveyDate, LUMBAREPI, 5 * 365, true);
      logger.info("Created {} follow up surveys for {} patients three months ago", LUMBAREPI, periCounts);
    } catch (NumberFormatException nfe) {
      logger.error("Error sending {} follow ups", LUMBAREPI);
    }
  }

  private int buildOutProcedureFollowUps(final Date assignedDate, final Date surveyDate, final String surveyType, int cutoff) {
    return buildOutProcedureFollowUps(assignedDate, surveyDate, surveyType, cutoff, false);
  }

  private int buildOutProcedureFollowUps(final Date assignedDate, final Date surveyDate, final String surveyType, int cutoff, boolean ignoreRecent) {

    logger.debug("buildOutProcedureFollowUps: look for patients sent {} surveys  "
        + " {} to send another dated {} unless stopped", surveyType, dateFormat.format(assignedDate), dateFormat.format(surveyDate));
    int counts = 0;
    try {
      /*
       * Find the patients sent this type of survey for the given date (and three days prior).
       * The overlap picks up any days missed in case the job wasn't run for some reason.
       */
      Calendar fromTime = DateUtils.getCalendarDayStart(siteInfo, assignedDate);
      fromTime.add(Calendar.DAY_OF_MONTH, -3); // look back 3 days

      Calendar windowStart = DateUtils.getCalendarDayStart(siteInfo,  assignedDate);
      windowStart.add(Calendar.DAY_OF_YEAR, 1);

      String sql =
          "SELECT patient_id, survey_reg_id, survey_dt from survey_registration  sr  "
              + " WHERE sr.survey_site_id = :site and sr.survey_dt between ? and ? and sr.survey_type like ? "
              + " and 'c' != (SELECT registration_type from appt_registration ar where ar.assessment_reg_id = sr.assessment_reg_id)"
              + " and not exists (SELECT * from survey_registration s2 "
              + " WHERE s2.survey_site_id = sr.survey_site_id and s2.survey_dt between ? and ? and s2.survey_type like ? "
              + " and s2.patient_id = sr.patient_id and s2.survey_reg_id != sr.survey_reg_id "
              + " and 'c' != (SELECT registration_type from appt_registration a2 where a2.assessment_reg_id = s2.assessment_reg_id))";

      ArrayList<ProcedureRegistration> procedureRegistrationList = database.toSelect(sql)
          .argLong(":site", siteId)
          .argDate(fromTime.getTime())
          .argDate(assignedDate)
          .argString(surveyType + "%")
          .argDate(fromTime.getTime())
          .argDate(surveyDate)
          .argString(surveyType + "%")
          .query(rs -> {
            ArrayList<ProcedureRegistration> procRegistrations = new ArrayList<>();
            while (rs.next()) {
              ProcedureRegistration procReg = new ProcedureRegistration();
              procReg.patientId = rs.getStringOrEmpty(1);
              procReg.surveyRegId = rs.getLongOrNull(2);
              procReg.surveyDt = rs.getDateOrNull(3);
              procRegistrations.add(procReg);
            }
            return procRegistrations;
          });
      logger.debug("buildOutProcedureFollowUps: found {} {} surveys between {} and {}", procedureRegistrationList.size(),
          surveyType, fromTime.toString(), assignedDate);
      /*
       *  Make sure the patient is registered, has an email address and hasn't been sent this type of survey since the
       *  found surveys date
       */
      if (procedureRegistrationList.size() > 0) {
        AssessDao assessDao = new AssessDao(database, siteInfo);
        SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);

        // Get the process type to create and its corresponding visit type
        String processType = getProcessName(surveyType);
        String visitType = XMLFileUtils.getInstance(siteInfo).getAttribute(processType, "visitType");
        PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
        for (ProcedureRegistration procReg : procedureRegistrationList) {
          Patient thisPatient = patAttribDao.getPatient(procReg.patientId);
          if (thisPatient.hasConsented()
              && thisPatient.hasValidEmail() && (thisPatient.getEmailAddress() != null)
              && (ignoreRecent || !hasRecentAssessment(procReg.patientId, surveyType, surveyDate))) {
              procReg.surveyDt = surveyDate;
              procReg.emailAddress = thisPatient.getEmailAddress();
              counts = counts + sendFollowup(assessDao, surveyRegUtils, procReg, processType, visitType, cutoff);
          } else if (!thisPatient.hasConsented()) {
            logger.debug("Skipping {} followup,  patient has not Consented", surveyType);
          } else if (!thisPatient.hasValidEmail()) {
            logger.debug("Skipping {} followup, patient does not have a valid Email", surveyType);
          } else if (thisPatient.getEmailAddress() == null) {
            logger.debug("Skipping {} followup, patients email address is null", surveyType);
          } else {
            logger.debug("Skipping {} followup, patient hasRecentAssessment", surveyType);
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Error creating a {} followup dated {} for patient ", surveyType, dateFormat.format(assignedDate), ex);
    }
    return counts;
  }

  private int createAssessment(SurveyRegUtils surveyRegUtils, Date surveyDate, AppointmentRegistration apptReg, String emailAddress) {

    // Get the active survey type
    String processType = getProcessName(apptReg.setName);
    // Get the visit type
    String visitType = XMLFileUtils.getInstance(siteInfo).getAttribute(processType, "visitType");

    AssessDao assessDao = new AssessDao(database, siteInfo);
    ApptRegistration reg = new ApptRegistration(siteId, apptReg.patientId, surveyDate, emailAddress, processType,
        Constants.REGISTRATION_TYPE_STANDALONE_SURVEY, visitType);

    reg = surveyRegUtils.createRegistration(assessDao, reg);
    logger.debug("buildOutTreatmentSetSurveys Created a new registration {} with survey type: {}, visit type: {}, dated {} ",
        reg.getApptRegId(), reg.getSurveyType(), reg.getVisitType(),  dateFormat.format(reg.getAssessmentDt()));
    return 1;
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

  private void endOfDay(Calendar cal) {
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
  }

  private Date endOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  private int sendFollowup(AssessDao assessDao, SurveyRegUtils surveyRegUtils, ProcedureRegistration procedureRegistration,
                           String processType, String visitType, int cutoff) {
    Map<String,String> surveyRegAttrMap = surveyRegAttrDao.getAttributes(procedureRegistration.surveyRegId);
    if (surveyRegAttrMap.containsKey("STOP")) {
      return 0;
    }
    Long parentId = null;
    if (surveyRegAttrMap.containsKey("PARENT")) {
      try {
        parentId = new Long(surveyRegAttrMap.get("PARENT"));
      } catch (NumberFormatException nfe) {
        logger.error("getParentRegId encountered error trying to obtain the parent survey_reg_id from string {} ", surveyRegAttrMap.get("PARENT"), nfe);
        return 0;
      }
    }
    if (parentId == null) {
      parentId = procedureRegistration.surveyRegId;
    }

    // Get the parent survey
    SurveyRegistration parentRegistration = assessDao.getSurveyRegistrationByRegId(parentId);
    if (parentRegistration == null || parentRegistration.getSurveyDt().before(DateUtils.getDateStart(siteInfo, DateUtils.getDaysAgoDate(siteInfo, cutoff)))) {
      logger.debug("Stopping proc followups for registration {} starting survey was not found or was more than {} days ago ", parentId, cutoff);
      return 0;
    }
    // Get the active survey type
    ApptRegistration reg = new ApptRegistration(siteId, procedureRegistration.patientId, procedureRegistration.surveyDt, procedureRegistration.emailAddress, processType,
        Constants.REGISTRATION_TYPE_STANDALONE_SURVEY, visitType);

    reg = surveyRegUtils.createRegistration(assessDao, reg);
    logger.debug("buildOutProcedureFollowUps Created a new registration {} with survey type: {}, visit type: {}, dated {} ",
        reg.getApptRegId(), reg.getSurveyType(), reg.getVisitType(), dateFormat.format(reg.getAssessmentDt()));

    surveyRegAttrDao.setAttribute(reg.getSurveyRegList().get(0).getSurveyRegId(), "PARENT", parentId.toString() );

    return 1;
  }

  private int sendProcedureFollowUp(final Date assignedDate, final Date surveyDate, final String surveyType, final String visitType) {
    int followUpsSent = 0;
    try {
      /*
       * Find the patients sent this type of survey for the given date (and three days prior) but NOT this visitType.
       * The overlap picks up any days missed in case the job wasn't run for some reason.
       */
      Calendar fromTime = DateUtils.getCalendarDayStart(siteInfo, assignedDate);

      Calendar windowStart = DateUtils.getCalendarDayStart(siteInfo,  assignedDate);
      windowStart.add(Calendar.DAY_OF_YEAR, 1);

      String sql =
          "SELECT sr.patient_id, survey_reg_id, survey_dt from survey_registration sr  "
              + " JOIN appt_registration ar on sr.assessment_reg_id = ar.assessment_reg_id "
              + " WHERE sr.survey_site_id = :site and sr.survey_dt between ? and ? and sr.survey_type like ? "
              + " and (visit_type is null or visit_type != ?) "
              + " and 'c' != (SELECT registration_type from appt_registration ar where ar.assessment_reg_id = sr.assessment_reg_id)"
              + " and not exists (SELECT * from survey_registration s2 "
              + " WHERE s2.survey_site_id = sr.survey_site_id and s2.survey_dt between ? and ? and s2.survey_type like ? "
              + " and s2.patient_id = sr.patient_id and s2.survey_reg_id != sr.survey_reg_id "
              + " and 'c' != (SELECT registration_type from appt_registration a2 where a2.assessment_reg_id = s2.assessment_reg_id)"
              + " and ? != (select visit_type from appt_registration a2 where a2.assessment_reg_id = s2.assessment_reg_id)"
              + ")";

      ArrayList<ProcedureRegistration> procedureRegistrationList = database.toSelect(sql)
          .argLong(":site", siteId)
          .argDate(fromTime.getTime())
          .argDate(assignedDate)
          .argString(surveyType + "%")
          .argString(visitType)
          .argDate(fromTime.getTime())
          .argDate(surveyDate)
          .argString(surveyType + "%")
          .argString(visitType)
          .query(rs -> {
            ArrayList<ProcedureRegistration> procRegistrations = new ArrayList<>();

            while (rs.next()) {
              ProcedureRegistration procReg = new ProcedureRegistration();
              procReg.patientId = rs.getStringOrEmpty(1);
              procReg.surveyRegId = rs.getLongOrNull(2);
              procReg.surveyDt = rs.getDateOrNull(3);
              procRegistrations.add(procReg);
            }
            return procRegistrations;
          });
      if (procedureRegistrationList.size() > 0) {
        AssessDao assessDao = new AssessDao(database, siteInfo);
        SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);

        // Get the process type to create
        String processType = getProcessName(surveyType);
        PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
        for (ProcedureRegistration procReg : procedureRegistrationList) {
          Patient thisPatient = patAttribDao.getPatient(procReg.patientId);
          if (thisPatient.hasConsented()
              && thisPatient.hasValidEmail() && (thisPatient.getEmailAddress() != null)) {
            procReg.surveyDt = surveyDate;
            procReg.emailAddress = thisPatient.getEmailAddress();
            // Get the active survey type
            ApptRegistration reg = new ApptRegistration(siteId, procReg.patientId, procReg.surveyDt, procReg.emailAddress, processType,
                Constants.REGISTRATION_TYPE_STANDALONE_SURVEY, visitType);

            reg = surveyRegUtils.createRegistration(assessDao, reg);
            followUpsSent++;
            logger.debug("sendProcedureFollowUp Created a new registration {} with survey type: {}, visit type: {}, dated {} ",
                reg.getApptRegId(), reg.getSurveyType(), reg.getVisitType(), dateFormat.format(reg.getAssessmentDt()));

          } else if (!thisPatient.hasConsented()) {
            logger.debug("Skipping {} followup,  patient has not Consented", surveyType);
          } else if (!thisPatient.hasValidEmail()) {
            logger.debug("Skipping {} followup, patient does not have a valid Email", surveyType);
          } else if (thisPatient.getEmailAddress() == null) {
            logger.debug("Skipping {} followup, patients email address is null", surveyType);
          } else {
            logger.debug("Skipping {} followup, patient hasRecentAssessment", surveyType);
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Error getting {} surveys to send follow ups", surveyType);
    }
    return followUpsSent;
  }

  static class AppointmentRegistration {
    String patientId;
    String setName;
  }

  static class ProcedureRegistration {
    String patientId;
    String emailAddress;
    Long surveyRegId;
    Date surveyDt;
  }
}

