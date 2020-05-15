package edu.stanford.registry.server.shc.trauma;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.service.EmailMonitor;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.EmailTemplateUtils;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.EmailSendStatus;
import edu.stanford.registry.shared.Notification;
import edu.stanford.registry.shared.Patient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/*
 * Common methods used for creating Trauma follow up surveys. Created by tpacht on 8/7/17.
 */
public class TraumaUtils {

  private final static Logger logger = LoggerFactory.getLogger(TraumaUtils.class);
  private final XMLFileUtils fileUtils;
  private final SiteInfo siteInfo;


  public TraumaUtils(SiteInfo siteInfo) {
    fileUtils = XMLFileUtils.getInstance(siteInfo);
    this.siteInfo = siteInfo;
  }
  public String getSurveyType(String startString) {
    ArrayList<String> processes = fileUtils.getActiveVisitProcessNames();
    for (String processName : processes) {
      if (processName.startsWith(startString)) {
        return processName;
      }
    }
    return null;
  }
  ApptRegistration createAssessment(Date surveyDt, String patientId, String emailAddress, String surveyType, Database database) {
    return createAssessment(surveyDt, patientId, emailAddress, surveyType, database, true);
  }

  ApptRegistration createAssessment(Date surveyDt, String patientId, String emailAddress, String surveyType, Database database, boolean consentRequired) {
    logger.debug("createAssessment starting for surveydt {} patient {} email {} surveyType {}", surveyDt, patientId, emailAddress, surveyType);
    // By convention, stand alone surveys are at 11:59:59 PM
    Calendar cal = Calendar.getInstance();
    cal.setTime(surveyDt);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
    Date surveyDate = cal.getTime();

    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);

    // Get existing registrations for the patient on the follow up date
    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<ApptRegistration> regs = assessDao.getApptRegistrationByPatientAndDate(patientId, surveyDate);
    logger.debug("createAssessment found {} registrations for {}", regs.size(), surveyDate);
    // See if an existing registration has already already been created for the survey type and email
    ApptRegistration reg = null;
    for (ApptRegistration sr : regs) {
      if (surveyType.equals(sr.getSurveyType())) {
        AssessmentRegistration assessmentRegistration = sr.getAssessment();
        if (assessmentRegistration != null && emailAddress.equals(assessmentRegistration.getEmailAddr())) {
          reg = sr;  // found a match
        }
      }
    }

    if (reg == null) {
      logger.debug("createAssessment existing registration not found");
    }
    if (isPatientCompletingSurveys(patientId, database)) {
      logger.debug("createAssessment isPatientCompletingSurveys is true");
    } else {
      logger.debug("createAssessment isPatientCompletingSurveys is false");
    }
    // If not found then create a new registration
    if (reg == null && (isPatientCompletingSurveys(patientId, database ) || !consentRequired)) {
      reg = new ApptRegistration(siteInfo.getSiteId(), patientId, surveyDate, emailAddress, surveyType,
          Constants.REGISTRATION_TYPE_STANDALONE_SURVEY, getVisitType(surveyType));
      reg = surveyRegUtils.createRegistration(assessDao, reg);
      logger.debug("Creating new appt, survey type: {}, visit type: {}, appt: {} " , reg.getSurveyType(),reg.getVisitType(), reg.getApptId());
      return reg;
    }

    logger.debug("Survey registration already exists for follow up");
    return null;
  }

  public ArrayList<EmailSendStatus>  sendEmails(ArrayList<ApptRegistration> registrations, Database database) {
    AssessDao assessDao = new AssessDao(database, siteInfo);
    ArrayList<EmailSendStatus> emailSendStatuses = new ArrayList<>();
    for (ApptRegistration appt : registrations) {
      ArrayList<Notification> notifications = assessDao.getUnsentNotifications(appt.getAssessmentId());
      if ((notifications == null) || (notifications.size() < 1)) {
        logger.debug("Notification not found, adding a new one for " + appt.getPatientId() + ";"
            + appt.getAssessmentId() + ";" + appt.getAssessmentType() + ";"
            + appt.getAssessmentDt().toString() + ";");
        Notification notify = new Notification(appt.getPatientId(), appt.getAssessmentId(),
            appt.getAssessmentType(), appt.getAssessmentDt(), 0, appt.getSurveySiteId());
        assessDao.insertNotification(notify);
      }
      Mailer mailer = siteInfo.getMailer();
      String serverUrl = siteInfo.getProperty("survey.link");

      String templateName;
      if (appt.isAppointment()) {
        templateName = fileUtils.getAttribute(appt.getSurveyType(),
            XMLFileUtils.ATTRIBUTE_APPOINTMENT_TEMPLATE);
      } else {
        templateName = fileUtils.getAttribute(appt.getSurveyType(),
            XMLFileUtils.ATTRIBUTE_SCHEDULE_TEMPLATE);
      }
      EmailTemplateUtils emailTemplateUtils = new EmailTemplateUtils();

      HashMap<String, String> oneTemplate = new HashMap<>(1);
      String content = emailTemplateUtils.getTemplate(siteInfo, templateName);
      if (content != null) {
        oneTemplate.put(templateName, content);
      }

      EmailMonitor monitor = new EmailMonitor(mailer, database, serverUrl, siteInfo);
      Calendar beforeDate = Calendar.getInstance();
      beforeDate.setTime(appt.getSurveyDt());
      beforeDate.add(Calendar.MINUTE, 1);
      logger.debug("Sending email for assessmentId {} emailAddr {}", appt.getAssessment().getAssessmentId(), appt.getEmailAddr());
      emailSendStatuses.addAll(monitor.sendEmail(oneTemplate, beforeDate.getTime(), appt.getAssessment().getAssessmentId()));
    }
    return emailSendStatuses;
  }

  void createFamilySurvey(Database database, Patient patient, String type) throws Exception {
    if (type == null) {
      logger.error("Not creating FamilyInitial survey for patient {}. Method called with null type", patient.getPatientId());
    }

    String familyEmail = patient.getAttributeString(type);
    if (familyEmail == null || familyEmail.isEmpty()) {
      logger.error("Not creating, a FamilyInitial survey for patient {}. Missing email", type, patient.getPatientId());
    }

    Date surveyDate = getSurveyDate();
    ApptRegistration apptRegistration = createAssessment(surveyDate, patient.getPatientId(), familyEmail,
        XMLFileUtils.getInstance(siteInfo).getActiveProcessForName("FamilyInitial", surveyDate), database, false);
    if (apptRegistration == null) {
      throw new Exception("Not creating.<p> <p>A FamilyInitial survey, dated 7 days from now, already exists for this email address.");
    }
    ArrayList<ApptRegistration> registrations = new ArrayList<>();
    registrations.add(apptRegistration);
    ArrayList<EmailSendStatus> emailSendStatuses = sendEmails(registrations, database);
    for (EmailSendStatus status : emailSendStatuses) {
      logger.debug("Email sent status ", status.toString());
    }
    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
    surveyRegUtils.registerAssessments(database, apptRegistration.getAssessment(), ServerUtils.getAdminUser(database) );
    logger.debug("FamilyInitial Survey created for {} on trauma patient {}", type, patient.getPatientId());
  }

  private Date getSurveyDate() {
    String initialDaysOutString = siteInfo.getProperty("appointment.initialemail.daysout");
    if (initialDaysOutString != null && initialDaysOutString.trim().length() > 0) {
      try {
        int initialDaysOutInt = Integer.parseInt(initialDaysOutString);
        return DateUtils.getDaysOutDate(initialDaysOutInt);
      } catch (Exception e) {
        logger.error("Not creating family survey. Missing value for the 'appointment.initialemail.daysout' parameter.");
      }
    }
    return null;
  }

  private boolean isPatientCompletingSurveys(String patientId,  Database database) {

    String sql = "select survey_dt, survey_type, is_complete from survey_registration sr, survey_token st "
      + "where sr.survey_site_id = :site and sr.survey_site_id = st.survey_site_id and sr.token = st.survey_token "
        + "and patient_id = ? order by survey_dt desc";

    return database.toSelect(sql)
        .argLong(":site", siteInfo.getSiteId())
        .argString(patientId)
        .query(new RowsHandler<Boolean>() {
          @Override
          public Boolean process(Rows rs) throws Exception {
            int count = 0;

            while (rs.next() && count < 3) {
              if ("Y".equals(rs.getStringOrEmpty(3))) {
                logger.debug("isPatientCompletingSurveys returning true");
                return true;
              }
              count++;
            }
            return false;
          }
        });
  }

  private String getVisitType(String surveyType) {
    if (surveyType.startsWith("Patient")) {
      return "FOL";
    }
    if (surveyType.startsWith("FamilyInitial")) {
      return "FIN";
    }
    return "FFO";
  }
}
