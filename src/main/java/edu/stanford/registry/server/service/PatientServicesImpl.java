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

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.config.AppConfig;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.survey.NextQuestion;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.survey.SurveyServiceIntf;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.InvalidCredentials;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.PatientStudyExtendedDataException;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.PageConfiguration;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.SessionStatus;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.client.api.SurveyPageComponent;
import edu.stanford.survey.server.Question;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class PatientServicesImpl {
  private static final Logger logger = LoggerFactory.getLogger(PatientServicesImpl.class);
  private final Supplier
  <Database> dbp;
  private final Database database;
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  protected final Long siteId;
  protected final SiteInfo siteInfo;
  protected final ActivityDao activityDao;

  public PatientServicesImpl(Supplier<Database> database, SiteInfo siteInfo) {
    dbp = database;
    this.database = dbp.get();
    this.siteId = siteInfo.getSiteId();
    this.siteInfo = siteInfo;
    this.activityDao = new ActivityDao(dbp.get(), siteId);
  }

  public PatientStudyExtendedData getPatientSurveyFromToken(String token) throws InvalidCredentials {
    User user = ServerUtils.getAdminUser(database);

    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    PatientStudyExtendedData patStudy = patStudyDao.getPatientStudyExtendedDataByToken(new Token(token), user);
    AssessDao assessDao = new AssessDao(database, siteInfo);
    SurveyRegistration registration = assessDao.getRegistration(token);

    /* If not found let's figure out why, fix it, switch to another or
       throw a useful error */
    int error = -1;
    if (patStudy == null) {
      if (registration == null) {
        logger.debug("throwing InvalidCredentials.TOKEN_NOT_FOUND exception");
        return sendPatientStudyException(InvalidCredentials.TOKEN_NOT_FOUND, token);
      }
      // Check the activities to see if the survey is registered or completed
      boolean registered = false;
      boolean completed = false;
      boolean testing = false;
      ActivityDao activityDao = new ActivityDao(database, registration.getSurveySiteId());
      ArrayList<Activity> activities = activityDao.getActivityByToken(token);
      if (activities != null) {
        for (Activity activity : activities) {
          if (activity.getActivityType().equals(Constants.ACTIVITY_REGISTERED))
            registered = true;
          if (activity.getActivityType().equals(Constants.ACTIVITY_COMPLETED))
            completed = true;
          if (Constants.REF_TESTQ.equals(activity.getActivityType())) {
            testing = true;
          }
        }
      }

      if (!registered) {
        logger.debug("TOKEN_NOT_REGISTERED: attempting to fix");
        SurveyRegUtils srUtils = new SurveyRegUtils(siteInfo);
        srUtils.registerAssessments(database, registration, user);
        patStudy = patStudyDao.getPatientStudyExtendedDataByToken(new Token(token), user);
        if (patStudy == null) {
          logger.debug("returning InvalidCredentials.TOKEN_NOT_REGISTERED exception");
          return sendPatientStudyException(InvalidCredentials.TOKEN_NOT_REGISTERED, token);
        }

      } else { // got the patient study for the non-registered patient
        if (completed) {
          if (testing) {
            return null;
          }
          if (registration.getSurveyDt().after(DateUtils.getDateStart(siteInfo, new Date()))) {
            logger.debug("returning InvalidCredentials.SURVEY_IS_COMPLETED exception, this appointment is for today or later");
            return sendPatientStudyException(InvalidCredentials.SURVEY_IS_COMPLETED, token);
          }
          error = InvalidCredentials.SURVEY_IS_COMPLETED;
        }

        if (error < 0) { // wasn't yet set
          // check if no patient_study rows, the survey details may just need to be re-generated
          if (registration.getNumberCompleted() == 0 && registration.getNumberPending() == 0) {
            logger.debug("The study details are missing: attempting to fix");
            SurveyRegUtils srUtils = new SurveyRegUtils(siteInfo);
            srUtils.registerAssessments(database, registration, user);
            patStudy = patStudyDao.getPatientStudyExtendedDataByToken(new Token(token), user);
          }
          if (patStudy == null) {
            logger.debug("no more assessments for token " + token + " with survey_dt " + registration.getSurveyDt());
            return null; // they've just finished
          }
        }
      }
    }

    // Expired if before yesterday or configured number of days
    // First check if there is a survey specific number of days configured
    // then check for a generic number of days configured.
    int expiresAfterDays = 1;
    String expiresAfterDaysStr = null;
    if ((registration != null) && (registration.getSurveyType() != null)) {
      String surveyType = registration.getSurveyType();
      int i = surveyType.lastIndexOf(".");
      if (i > 0) {
        surveyType = surveyType.substring(0, i);
      }
      expiresAfterDaysStr = siteInfo.getProperty("appointment.surveyexpires.afterdays." + surveyType);
    }
    if (expiresAfterDaysStr == null) {
      expiresAfterDaysStr = siteInfo.getProperty("appointment.surveyexpires.afterdays");
    }
    if (expiresAfterDaysStr != null) {
      try {
        expiresAfterDays = Integer.parseInt(expiresAfterDaysStr);
        if (expiresAfterDays < 1)
          expiresAfterDays = 1;
      } catch (NumberFormatException e) {
        logger.warn("appointment.surveyexpires.afterdays value of " + expiresAfterDaysStr + " is not a valid number");
      }
    }
    Date earliestNonExpiredDate = DateUtils.getDaysAgoDate(siteInfo, expiresAfterDays);
    
    if (registration != null && registration.getSurveyDt() != null && error < 0) {
      if (registration.getSurveyDt().before(earliestNonExpiredDate)) error = InvalidCredentials.SURVEY_EXPIRED;
    }
    if (registration != null && registration.getSurveyDt() != null && error >= 0) {
      // try and find another eligible token
      return getAnotherToken(registration.getPatientId(), registration.getSurveyDt(), error, token);
    }
    /* Check the patient consented */
    if (patStudy != null) {
      Patient pat = patStudy.getPatient();
      if (pat == null || !pat.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)) {
        logger.debug("returning InvalidCredentials.PATIENT_NOT_CONSENTED exception (patient==null");
        return sendPatientStudyException(InvalidCredentials.PATIENT_NOT_CONSENTED, token);
      }
      if (!pat.attributeEquals(Constants.ATTRIBUTE_PARTICIPATES, "y")) {
        logger.debug("returning InvalidCredentials.PATIENT_NOT_CONSENTED exception (participates!=y)");
        return sendPatientStudyException(InvalidCredentials.PATIENT_NOT_CONSENTED, token);
      }
      /* Check the patients name has been set up correctly */
      if (isEmpty(pat.getFirstName()) || isEmpty(pat.getLastName())) {
        logger.debug("returning InvalidCredentials.NO_PATIENT_NAME exception");
        return sendPatientStudyException(InvalidCredentials.NO_PATIENT_NAME, token);
      }
    }
    return patStudy;
  }

  private PatientStudyExtendedDataException sendPatientStudyException(int exceptionType, String token) {
    PatientStudyExtendedDataException patStudyException = new PatientStudyExtendedDataException(siteId);
    patStudyException.setError(new InvalidCredentials(exceptionType, token));
    return patStudyException;
  }

  /* Handle new client style response */
  public NextQuestion handleResponse(PatientStudyExtendedData patStudy, String submitStatusJson, String answerJson) {
    SubmitStatus submitStatus = null;
    if (submitStatusJson != null && submitStatusJson.length() > 0) {
      submitStatus = AutoBeanCodex.decode(factory, SubmitStatus.class, submitStatusJson).as();
    }
    NextQuestion next = handleResponseOnce(patStudy, submitStatus, answerJson);

    while(next == null) {
      // The current survey provider doesn't have any more questions; see if we have another provider
      try {
        patStudy = getPatientSurveyFromToken(patStudy.getToken());
      } catch (InvalidCredentials e) {
        logger.error("Credentials should always be valid here", e);
        return null;
      }
      if (patStudy == null) {
        return null;
      }
      next = handleResponseOnce(patStudy, null, null);
    }
    return next;
  }

  private NextQuestion handleResponseOnce(PatientStudyExtendedData patStudy, SubmitStatus submitStatus,
      String answerJson) {
    if (patStudy == null) {
      return null;
    }
    if (submitStatus != null && submitStatus.getSurveyProviderId() != null
        && submitStatus.getSurveySectionId() != null) {
      if (!Integer.toString(patStudy.getSurveySystemId()).equals(submitStatus.getSurveyProviderId())
          || !Integer.toString(patStudy.getStudyCode()).equals(submitStatus.getSurveySectionId())) {
        logger.warn(
            "Discarding answer and fetching latest question because it seems to be for the wrong system/survey: "
                + submitStatus + " " + answerJson);
        return null;
      }
    }
    SurveySystDao ssDao = new SurveySystDao(database);
    SurveySystem syst = ssDao.getSurveySystem(patStudy.getSurveySystemId());
    NextQuestion next;
    if (syst != null) {
      SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(siteInfo).getSurveyServiceImpl(
          syst.getSurveySystemName());
      next = surveyService.handleResponse(database, patStudy, submitStatus, answerJson);
      if (next != null && next.getDisplayStatus() != null
          && next.getDisplayStatus().getQuestionType().equals(QuestionType.skip) && next.getQuestion() == null
          ) {
        // skipping
        return handleResponse(patStudy, null, null);
      }
    } else {
      throw new DataException("Did not find a survey system for systemId " + patStudy.getSurveySystemId());
    }
    return next;
  }

  public Activity addActivity(Activity act) {
    activityDao.createActivity(act);
    return act;
  }

  public Question getThankYouPage(String token, AppConfig appConfig) {
    Question question = new Question(":thanks",QuestionType.form);
    question.getDisplayStatus().setSessionStatus(SessionStatus.clearSession);
    AssessDao assessDao = new AssessDao(database, siteInfo);
    String surveyType = assessDao.getSurveyType(token);
    String action = XMLFileUtils.getInstance(siteInfo).getAttribute(surveyType, "onComplete");

    if (action != null && "close".equals(action.toLowerCase())) {
      return noThankYou(new Question(":close", QuestionType.close));
    }
    /*
     *  Look in the database for the thank you page configuration entry.
     */
    String thankYouPageOptionsJson = appConfig.forName(siteId, "survey", "survey.thankyoupage");
    if (thankYouPageOptionsJson == null) {
      return simpleThankYou(question);
    }

    if (surveyType == null) {
      return simpleThankYou(question);
    }

    PatientDao patientDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    Patient patient = patientDao.getPatientByToken(token);
    if (patient == null) {
      return simpleThankYou(question);
    }

    /*
     * See if the process.xml has any media listed for the thank you page.
     */
    ArrayList<String> mediaNames = XMLFileUtils.getInstance(siteInfo).getMediaNames(surveyType,"survey.thankyoupage" );
    if (mediaNames == null || mediaNames.size() < 1) {
      logger.debug("No media for survey type: " + surveyType);
      return simpleThankYou(question);
    }
    /*
     * Find the component with the name configured in process.xml.
     * Currently the only component type we know how to deal with is a FieldType.videoLink
     * and its attributes are those needed for a kaltura player.
     */
    PageConfiguration pageConfiguration = AutoBeanCodex.decode(factory, PageConfiguration.class, thankYouPageOptionsJson).as();
    List<SurveyPageComponent> surveyPageComponents = pageConfiguration.getSurveyPageComponents();
    int fieldNumber=1;
    ArrayList<FormField> videoFields = new ArrayList<>();
    for (String mediaName : mediaNames) {
      for (SurveyPageComponent surveyPageComponent : surveyPageComponents) {
        Map <String, String> attributes;
        if (mediaName != null && surveyPageComponent != null
            && mediaName.equals(surveyPageComponent.getComponentName())
            && FieldType.videoLink.toString().equals(surveyPageComponent.getComponentType())) {
          attributes = surveyPageComponent.getAttributes();
          // See if there are qualifying attributes
          Map <String, String> qualifyingAttributes = XMLFileUtils.getInstance(siteInfo).getMediaAttributes(surveyType, "survey.thankyoupage",
              mediaName);
          // Patient qualifies for seeing the video if they have all of the qualifying attribute/value listed in processType
          if (qualifyingAttributes != null) {
            for (String dataName : qualifyingAttributes.keySet()) {
              String dataValue = qualifyingAttributes.get(dataName);
              if (dataValue != null) {
                PatientAttribute patientAttribute = patientDao.getAttribute(patient.getPatientId(), dataName);
                if (patientAttribute == null || patientAttribute.getDataValue() == null ||
                    !dataValue.equals(patientAttribute.getDataValue())) {
                    logger.debug("patient doesn't qualify for media");
                    attributes = null;
                }
              }
            }
          }
          if (attributes != null) {
            // Add the heading if there is one
            if (surveyPageComponent.getComponentHeading() != null) {
              FormField headingField = factory.field().as();
              headingField.setType(FieldType.heading);
              headingField.setFieldId("1:" + fieldNumber + ":thankYou");
              fieldNumber++;
              headingField.setLabel(surveyPageComponent.getComponentHeading());
              videoFields.add(headingField);
            }
            // Add the component field
            FormField videoField = factory.field().as();
            videoField.setType(FieldType.videoLink);
            videoField.setFieldId("1:" + fieldNumber + ":thankYou");
            fieldNumber++;
            videoField.setAttributes(attributes);
            videoFields.add(videoField);
          }
        }
      }
    }
    // no media components
    if (videoFields.size() < 1) return simpleThankYou(question);
    // Make the question
    AutoBean<FormQuestion> form = factory.formQuestion();
    ArrayList<FormField> fields = new ArrayList<>();
    FormField headingField1 = factory.field().as();
    headingField1.setType(FieldType.heading);
    headingField1.setFieldId("1:0:thankYou");
    headingField1.setLabel("Thank you for completing this questionnaire.");
    fields.add(headingField1);
    for (FormField videoField : videoFields) {
      fields.add(videoField);
    }
    FormQuestion formQuestion = form.as();
    formQuestion.setFields(fields);
    form.as().setTerminal(true);
    question.setQuestion(form);
    return question;
  }

  private Question simpleThankYou(Question q) {
    AutoBean<FormQuestion> form = q.formQuestion("Thank you for completing this questionnaire.");
    form.as().setTerminal(true);
    q.setQuestion(form);
    return q;
  }

  private Question noThankYou(Question q) {
    AutoBean<FormQuestion> form = q.formQuestion("Processing ...");
    form.as().setTerminal(true);
    q.setQuestion(form);
    return q;
  }

  private boolean isEmpty(String value) {
    return value == null || value.length() < 1 || value.trim().equals("-");
  }

  private PatientStudyExtendedData getAnotherToken(String patientId, Date surveyDate, int error, String token) throws InvalidCredentials {
    // if the survey is more than 180 days old (or configured value) then throw an error
    int invalidAfterDays = 180;
    String invalidAfterDaysStr = siteInfo.getProperty("appointment.surveyinvalid.afterdays");
    try {
      if (invalidAfterDaysStr != null) {
        invalidAfterDays = Integer.parseInt(invalidAfterDaysStr);
        if (invalidAfterDays < 1) {
          invalidAfterDays = 180;
        }
      }
    } catch (NumberFormatException e) {
      logger.warn("appointment.surveyinvalid.afterdays value of " + invalidAfterDaysStr + " is not a valid number");
    }

    if (surveyDate.before(DateUtils.getDaysAgoDate(siteInfo, invalidAfterDays))) {
      logger.debug("returning InvalidCredentials " + error + " exception, this survey was more than " + invalidAfterDays + " days ago");
      return sendPatientStudyException(error, token);
    }

    // else look for a current survey to use instead
    logger.debug("checking for an appointment within the window (back 1 day, out 7) from today for expired token " + token);

    String sql = "select token from survey_registration" +
                 " where survey_site_id = :site and patient_id = ? and survey_dt > ? and survey_dt between " +
                 database.when().oracle("trunc(:dt)").other("date_trunc('day',:dt)") + " - interval '1' day and " +
                 database.when().oracle("trunc(:dt)").other("date_trunc('day',:dt)") + " + interval '8' day";
    String newToken = database.toSelect(sql)
        .argDateNowPerDb(":dt")
        .argLong(":site", siteId)
        .argString(patientId)
        .argDate(surveyDate)
        .query(new RowsHandler<String>() {

          @Override
          public String process(Rows rs) throws Exception {
            if (rs.next()) {
              return rs.getStringOrNull(1);
            }
            return null;
          }
        });
    if (newToken != null) {
      logger.debug("returning  token " + newToken + " to replace the expired token " + token);
      return getPatientSurveyFromToken(newToken);
    }
    logger.debug("returning InvalidCredentials " + error + " exception, no survey found within the next 7 days");
    return sendPatientStudyException(error, token);

  }

  public double getProgress(String surveyToken) {
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    return patStudyDao.getProgress(surveyToken);
  }
}
