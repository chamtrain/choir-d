package edu.stanford.registry.server.shc.trauma;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.EmailSendStatus;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.server.SurveyComplete;
import edu.stanford.survey.server.SurveyCompleteHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
/**
 * Writes the square table with the survey data. For PatientInitial surveys it also looks to see if the patient has provided the email addresses for
 * family members to also take surveys. Created by tpacht on 8/7/17.
 */

public class TraumaCompletionHandler implements SurveyCompleteHandler {

  private final SiteInfo siteInfo;
  private static final Logger logger = LoggerFactory.getLogger(TraumaCompletionHandler.class);
  private final TraumaUtils utils;
  private final String initialSurveyType;
  public TraumaCompletionHandler(SiteInfo siteInfo) {

    this.siteInfo = siteInfo;
    utils = new TraumaUtils(siteInfo);
    initialSurveyType = utils.getSurveyType("FamilyInitial");
  }


  @Override
  public boolean surveyCompleted(SurveyComplete survey, Supplier<Database> database) {

    SurveyRegistration surveyRegistration = getSurveyRegistration(survey, database);
    if (surveyRegistration != null && surveyRegistration.getSurveyType() != null && surveyRegistration.getSurveyType().startsWith("PatientInitial")) {
      String patientId = surveyRegistration.getPatientId();
      Patient patient = getPatient(patientId, database);
      ArrayList<ApptRegistration> regs = new ArrayList<>();
      if (patient != null && patient.hasAttribute("Family2")) {
        ApptRegistration reg = sendFamilyInitial(patient.getPatientId(), patient.getAttribute("Family2").getDataValue(), database);
        if (reg != null) {
          regs.add(reg);
        }
      }
      if (patient != null && patient.hasAttribute("Family4")) {
        ApptRegistration reg = sendFamilyInitial(patient.getPatientId(), patient.getAttribute("Family4").getDataValue(), database);
        if (reg != null) {
          regs.add(reg);
        }
      }
      if (!regs.isEmpty()) {
        logger.debug("Sending {} emails ", regs.size());
        ArrayList<EmailSendStatus> emailSendStatuses = utils.sendEmails(regs, database.get());
        for (EmailSendStatus status : emailSendStatuses) {
          logger.debug("sent status ", status.toString());
        }
      }
    }
    return false;
  }

  private SurveyRegistration getSurveyRegistration(SurveyComplete survey, Supplier<Database> database) {
    Database db = database.get();
    Long surveyRegId = getSurveyRegistrationId(survey.getSurveySiteId(), survey.getSurveyTokenId(), db);
    if (surveyRegId == null) {
      throw new RuntimeException("surveyRegId not found for siteId " + survey.getSurveySiteId() + " token_id "
          + survey.getSurveyTokenId());
    }
    AssessDao assessDao = new AssessDao(db, siteInfo);
    SurveyRegistration registration = assessDao.getSurveyRegistrationByRegId(surveyRegId);
    if (registration == null) {
      throw new RuntimeException("no registration for siteId " + surveyRegId + " token_id " + surveyRegId);
    }
    return registration;
  }

  private Long getSurveyRegistrationId(Long surveySiteId, Long surveyTokenId, Supplier<Database> database) {
    return database.get().toSelect("select survey_reg_id from survey_registration sr, survey_token st"
        + " where sr.survey_site_id=st.survey_site_id and sr.token=st.survey_token"
        + " and st.survey_site_id=? and st.survey_token_id=?")
        .argLong(surveySiteId)
        .argLong(surveyTokenId)
        .queryLongOrNull();
  }

  private Patient getPatient(String patientId, Supplier<Database> database) {
    PatientDao patientDao = new PatientDao(database.get(), siteInfo.getSiteId());
    return  patientDao.getPatient(patientId);
  }

  private ApptRegistration sendFamilyInitial(String patientId, String emailAddress, Supplier<Database> database) {
    String initialDaysOutString = siteInfo.getProperty("appointment.initialemail.daysout");
    if (initialDaysOutString == null || initialDaysOutString.trim().length() < 1) {
      logger.error("Not sending family initial emails. Missing value for the 'appointment.initialemail.daysout' parameter.");
    } else {
      try {
        int initialDaysOutInt = Integer.parseInt(initialDaysOutString);
        Date surveyDate = DateUtils.getDaysOutDate(initialDaysOutInt);
        return utils.createAssessment(surveyDate, patientId, emailAddress, initialSurveyType, database.get());

      } catch (Exception e) {
        logger.error("Invalid value for the appointment.initialemail.daysout parameter, must be a valid integer", e);
      }
    }
    return null;
  }
}
