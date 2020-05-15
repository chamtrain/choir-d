/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.service;

import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.EmailTemplateUtils;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DateUtilsIntf;
import edu.stanford.registry.shared.EmailContentType;
import edu.stanford.registry.shared.EmailSendStatus;
import edu.stanford.registry.shared.Notification;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.SurveyRegistration;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

public class EmailMonitor {

  private static final Logger logger = Logger.getLogger(EmailMonitor.class);
  private Mailer mailer;
  private Database database;
  private String serverUrl;
  // Value for s=<systemName> parameter in survey link
  private String siteName;
  // TRUE to bypass the check if the patient is over 18
  // Pediatric Pain clinic sends the email to the parent
  private boolean emailParent;
  final SiteInfo siteInfo;

  public EmailMonitor(Mailer mailer, Supplier<Database> dbp, String url, SiteInfo siteInfo) {
    this.mailer = mailer;
    if (dbp != null) database = dbp.get();
    this.serverUrl = url;
    this.siteInfo = siteInfo;
    this.emailParent = siteInfo.getProperty("registry.email.toParent", false);
    this.siteName = siteInfo.getUrlParam();
  }

  private void checkEnv() throws ServiceUnavailableException {
    /**
     * Make sure we got the resources we need to process.
     */
    if (mailer == null) {
      logger.error("handlePendingNotifications with null mailer");
      throw new ServiceUnavailableException("mailer is null!");
    }
    if (database == null) {
      logger.error("handlePendingNotifications with null database");
      throw new ServiceUnavailableException("database is null!");
    }
    if (serverUrl == null) {
      logger.error("handlePendingNotifications with null serverUrl");
      throw new ServiceUnavailableException("serverUrl is null!");
    }
  }

  /**
   * Customize this method to control the subject line of the email for patient counts.
   */
  private String emailSubject(EmailTemplateUtils eu, String template, String type) {
    String subject = eu.getEmailSubject(template);
    if (subject == null) {
      subject = "Stanford Pain Clinic " + type + " Patient questionnaire";
    }
    return subject;
  }

  private String emailBody(EmailTemplateUtils eu, String template, Notification not, PatientRegistration patRegistration) {
    template = eu.getEmailBody(template);

    String email = template;

    // Handle any customized substitution variables first to allow these to
    // override the standard substitutions
    RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
    Map<String,String> substitutions = customizer.getEmailSubstitutions(database, patRegistration);
    for(String key : substitutions.keySet()) {
      email = replace(email, key, substitutions.get(key));
    }

    DateUtilsIntf dateFmtr = siteInfo.getDateFormatter();
    email = replace(email, "[SURVEY_DATE]", dateFmtr.getDateString(patRegistration.getSurveyDt()));

    // Do the substitutions for the survey link and survey token
    List<SurveyRegistration> surveys = patRegistration.getSurveyRegList();
    for(SurveyRegistration survey : surveys) {
      String surveyLink;
      String surveyToken;
      if (survey.getSurveyName().equals("Default")) {
        surveyLink = "[SURVEY_LINK]";
        surveyToken = "[TOKEN]";
      } else {
        String surveyName = survey.getSurveyName().toUpperCase();
        surveyLink = "[SURVEY_LINK_" + surveyName + "]";
        surveyToken = "[TOKEN_" + surveyName + "]";
      }

      String patientLink = serverUrl + "/survey2?s="+siteName+"&tk=" + survey.getToken();

      email = replace(email, surveyLink, patientLink);
      email = replace(email, surveyToken, survey.getToken());
    }

//    String participationUrl = serverUrl + "/Participation.html?tk=" + patRegistration.getToken();
//    email = replace(email, "[PATIENT_OPT_OUT_LINK]", participationUrl);

    logger.debug("emailbody= " + email);
    return email;
  }

  public Integer handlePendingNotifications(HashMap<String, String> templates, Date beforeDate)
      throws ServiceUnavailableException {
    checkEnv();
    AssessDao assessDao = new AssessDao(database, siteInfo);
    ArrayList<Notification> pendingNotifications = assessDao.getPendingNotifications();
    ArrayList<EmailSendStatus> sendStatusList = sendEmail(templates, pendingNotifications, beforeDate);
    int sentAppointments = 0;
    if (sendStatusList != null && sendStatusList.size() > 0) {
      for (EmailSendStatus sendStatus : sendStatusList) {
        if (EmailSendStatus.sent == sendStatus) {
          sentAppointments++;
        }
      }
    }
    return sentAppointments;
  }

  public ArrayList<EmailSendStatus> sendEmail(HashMap<String, String> templates, Date beforeDate, AssessmentId assessmentId) {
    checkEnv();
    AssessDao assessDao = new AssessDao(database, siteInfo);
    ArrayList<Notification> tokenNotifications = assessDao.getUnsentNotifications(assessmentId);
   return sendEmail(templates, tokenNotifications, beforeDate);
  }

  /**
   * This caches AssessDaos just for one run of sendEmail.
   * It's fine that this cache is not updated
   */
  static class DaoCache {
    Hashtable<Long, AssessDao> hash = new Hashtable<Long, AssessDao>();
    public AssessDao getAssessDao(Database database, SiteInfo siteInfo) {
      AssessDao d = hash.get(siteInfo.getSiteId());
      if (d == null)
        hash.put(siteInfo.getSiteId(), d = new AssessDao(database, siteInfo));
      return d;
    }
  }

  private ArrayList<EmailSendStatus> sendEmail(HashMap<String, String> templates, ArrayList<Notification> pendingNotifications,
                            Date beforeDate) throws ServiceUnavailableException {

    ArrayList<EmailSendStatus> mailSendStatus = new ArrayList<>();

    if (pendingNotifications != null) {
      logger.debug(siteInfo.getIdString()+"Processing " + pendingNotifications.size() + " notifications.");
      DaoCache doaCache = new DaoCache();

      for (Notification not : pendingNotifications) {
        AssessDao assessDao = doaCache.getAssessDao(database, siteInfo);
        PatientRegistration registration = assessDao.getPatientRegistrationByAssessmentId(not.getAssessmentId());

        /* If the registration doesn't have an email address but the patient has one use that */
        if (registration != null && registration.getEmailAddr() == null && registration.getPatient() != null) {
          String emailAddress = registration.getPatient().getEmailAddress();
          if (emailAddress != null) {
            registration.setEmailAddr(emailAddress);
          }
        }
        EmailSendStatus emailStatus = EmailSendStatus.sent;
        if (registration == null) {
          logger.debug("Not sending email for notification with AssessmentRegId " + not.getAssessmentId() + " registration not found");
          emailStatus = EmailSendStatus.no_registration;
        } else {
          if (registration.getSurveyDt() != null && !registration.getSurveyDt().before(beforeDate)) {
            logger.debug("Not sending email for notification with ApptRegId " + registration.getApptId() + " survey dt "
                + registration.getSurveyDt() + " not before " + beforeDate);
            emailStatus = EmailSendStatus.invalid_survey_dt;
          }
          if (registration.getEmailAddr() == null || registration.getEmailAddr().trim().length() < 1) {
            logger.debug("Not sending email for notification with ApptRegId " + registration.getApptId() + " no email address");
            emailStatus = EmailSendStatus.no_email_addr;
          }
          if (!registration.getPatient().hasValidEmail()) {
            logger.debug("Not sending email for notification with ApptRegId " + registration.getApptId() + " patient email address marked invalid");
            emailStatus = EmailSendStatus.invalid_email_addr;
          }
          if (!consentedAnd18(registration.getPatient())) {
            logger.debug("Not sending email for notification with ApptRegId " + registration.getApptId() + " !consentedAnd18");
            emailStatus = EmailSendStatus.not_18;
            if (!consented(registration.getPatient())) {
              emailStatus = EmailSendStatus.not_consented;
            }
          }
        }

				/* Only create if they have an email address and they have consented and they are over 18 */
        if (emailStatus.equals(EmailSendStatus.sent)) {
          String templateName;
          XMLFileUtils xmlUtils = XMLFileUtils.getInstance(siteInfo);
          if (registration.isAppointment()) {
            templateName = xmlUtils.getAttribute(registration.getSurveyType(),
                XMLFileUtils.ATTRIBUTE_APPOINTMENT_TEMPLATE);
          } else {
            templateName = xmlUtils.getAttribute(registration.getSurveyType(),
                XMLFileUtils.ATTRIBUTE_SCHEDULE_TEMPLATE);
          }
          String template = templates.get(templateName);
          if (template != null) {
            String sql = "UPDATE NOTIFICATION SET EMAIL_DT = :now, DT_CHANGED = :now "
                + " WHERE NOTIFICATION_ID = :n AND EMAIL_DT IS NULL";

            int updateCount = database.toUpdate(sql).argDateNowPerDb(":now")
                .argInteger(":n", not.getNotificationId()).update();
            if (updateCount == 1) {
              logger.debug("Sending email for notification with ApptRegId " + registration.getApptId());
              EmailTemplateUtils eu = new EmailTemplateUtils();
              String subject = emailSubject(eu, template, registration.getSurveyType());
              String body = emailBody(eu, template, not, registration);
              boolean sent = false;
              try {
                RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
                logger.debug("got customizer " + customizer.getClass().getCanonicalName());
                List<File> attachments = customizer.getEmailAttachments(templateName);
                if (attachments != null && attachments.size() > 0) {
                  sent = mailer.sendTextWithAttachment(registration.getEmailAddr(), null, null, subject, body, attachments);
                } else {
                  EmailContentType contentType = eu.getEmailContentType(database, siteInfo, templateName);
                  if (contentType.isHtml()) {
                    sent = mailer.sendHtml(registration.getEmailAddr(), null, null, subject, body);
                  } else {
                    sent = mailer.sendText(registration.getEmailAddr(), null, null, subject, body);
                  }
                }
              } catch (Exception ex) {
                logger.error("ERROR trying to send mail for notification id " + not.getNotificationId(), ex);
              }
              if (sent) {
                database.commitNow();
              } else {
                logger.error("mailer was unable to send the email to patient " + registration.getPatientId()
                    + " with email address " + registration.getEmailAddr());
                database.rollbackNow();
              }
            } else {
              throw new ServiceUnavailableException("update notification count was " + updateCount + " should be 1");
            }
          } else {
            logger.debug("Template named " + templateName + " not found for '" + registration.getSurveyType() + "'");
          }
        }
        mailSendStatus.add(emailStatus);
      }
    }

    return mailSendStatus;
  }

  private String replace(String text, String variable, String value) {
    int inx = text.indexOf(variable);

    while (inx > -1) {
      logger.debug("Found variable " + variable + " at position " + inx + " replacing with " + value);
      StringBuilder sbuf = new StringBuilder();
      sbuf.append(text.subSequence(0, inx));
      sbuf.append(value);
      if (text.length() > inx + value.length()) {
        sbuf.append(text.substring(inx + variable.length()));
      }
      text = sbuf.toString();
      inx = text.indexOf(variable);
    }
    return text;
  }

  private boolean consented(Patient patient) {
    if (patient != null && patient.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)
        && "y".equals(patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue())) {
      return true;
    }
    return false;
  }

  private boolean consentedAnd18(Patient patient) {
    if (consented(patient)
        && ((DateUtils.getAge(patient.getDtBirth()) >= 18) || emailParent)) {
      return true;
    }
    return false;
  }
}
