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
package edu.stanford.registry.server.service;

import edu.stanford.registry.client.api.ApiObjectFactory;
import edu.stanford.registry.client.api.ApptObj;
import edu.stanford.registry.client.api.AssessmentObj;
import edu.stanford.registry.client.api.NotificationObj;
import edu.stanford.registry.client.api.SurveyObj;
import edu.stanford.registry.client.api.SurveyRegistrationObj;
import edu.stanford.registry.client.api.SurveyStudyObj;
import edu.stanford.registry.client.api.SurveyStudyStepObj;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SiteDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Notification;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.PatientRegistrationSearch;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Provider;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.api.SiteObj;
import edu.stanford.survey.client.api.BodyMapAnswer;
import edu.stanford.survey.client.api.BodyMapQuestion;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.NumericAnswer;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.RadiosetAnswer;
import edu.stanford.survey.client.api.RadiosetQuestion;
import edu.stanford.survey.client.api.SliderQuestion;
import edu.stanford.survey.client.api.TextInputAnswer;
import edu.stanford.survey.client.api.TextInputQuestion;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Provide the clinic service method implementations for Api functionality
 *
 * @author tpacht
 */
public class ApiClinicServicesUtils {
  private final ApiObjectFactory factory = AutoBeanFactorySource.create(ApiObjectFactory.class);
  private final Supplier<Database> dbp;
  private final User user;
  private final SiteInfo siteInfo;

  public ApiClinicServicesUtils(User usr, Supplier<Database> databaseProvider, SiteInfo siteInfo) {
    user = usr;
    dbp = databaseProvider;
    this.siteInfo = siteInfo;
  }


  ApptRegistration getLastCompletedRegistration(AssessDao assessDao, String patientId) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.YEAR, 10);
    return assessDao.getLastCompletedRegistration(patientId, cal.getTime());
  }

  SurveyObj getSurveyObj(AssessDao assessDao, String token) throws NotFoundException {
    SurveyObj surveyObj = factory.surveyObj().as();
    SurveyQuery surveyQuery = new SurveyQuery(dbp.get(), new SurveyDao(dbp.get()), siteInfo.getSiteId());
    Survey survey = surveyQuery.surveyBySurveyToken(token);
    surveyObj.setStartTime(timeString(survey.startTime()));
    String endTime = survey.endTime() == null ? "" : siteInfo.getDateFormatter().getDateString(survey.endTime());
    surveyObj.setEndTime(endTime);
    SurveyRegistration registration = assessDao.getRegistration(token);
    if (registration == null) {
      throw new NotFoundException("Registration");
    }
    SurveyRegistrationObj surveyRegistrationObj = getSurveyRegistrationObj(registration);
    surveyObj.setSurveyRegistration(surveyRegistrationObj);
    surveyObj.setSurveyOrder(registration.getSurveyOrder());
    surveyObj.setNumberCompleted(registration.getNumberCompleted());
    surveyObj.setNumberPending(registration.getNumberPending());
    surveyObj.setAssessmentId(registration.getAssessmentRegId());
    surveyObj.setSurveyName(registration.getSurveyName());
    surveyObj.setSurveyType(registration.getSurveyType());
    ArrayList<PatientStudyExtendedData> patientStudies = getPatientStudyExtendedDataByToken(token);
    ArrayList<SurveyStudyObj> surveyStudies = new ArrayList<>();
    for (PatientStudyExtendedData study : patientStudies) {
      SurveyStudyObj studyObj = getStudyObj(survey, registration, study);
      surveyStudies.add(studyObj);
    }
    surveyObj.setStudyList(surveyStudies);
    return surveyObj;
  }

  public SurveyStudyObj getStudyObj(Survey survey, SurveyRegistration registration, PatientStudyExtendedData study) {
    SurveyStudyObj studyObj = factory.surveyStudyObj().as();
    studyObj.setSurveyId(registration.getSurveyRegId());
    studyObj.setSurveyStudyId(study.getPatientStudyId());
    studyObj.setOrder(study.getOrderNumber());
    studyObj.setSurveySystemName(study.getSurveySystemName());
    studyObj.setSurveySystemId(study.getSurveySystemId());
    studyObj.setStudyName(study.getStudyDescription());
    studyObj.setStudyCode(study.getStudyCode());
    ArrayList<SurveyStudyStepObj> steps = new ArrayList<>();
    List<SurveyStep> answeredSteps = survey.answeredStepsByProviderSection(String.valueOf(study.getSurveySystemId()), String.valueOf(study.getStudyCode()));
    for (SurveyStep step : answeredSteps) {
      SurveyStudyStepObj surveyStudyStepObj = factory.surveyStudyStepObj().as();
      if (step != null && step.answer() != null && step.answer().getSubmitStatus() != null
          && step.answer().getSubmitStatus().getQuestionType() != null) {
        if (step.answer().getSubmitStatus().getQuestionType() == QuestionType.bodyMap) {
          surveyStudyStepObj.setBodyMapQuestion(AutoBeanCodex.decode(factory, BodyMapQuestion.class, step.questionJson()).as());
          surveyStudyStepObj.setBodyMapAnswer(AutoBeanCodex.decode(factory, BodyMapAnswer.class, step.answerJson()).as());
        } else if (step.answer().getSubmitStatus().getQuestionType() == QuestionType.buttonList ||
            step.answer().getSubmitStatus().getQuestionType() == QuestionType.collapsibleRadioset ||
            step.answer().getSubmitStatus().getQuestionType() == QuestionType.radioset) {
          surveyStudyStepObj.setRadiosetQuestion(AutoBeanCodex.decode(factory, RadiosetQuestion.class, step.questionJson()).as());
          surveyStudyStepObj.setRadioSetAnswer(AutoBeanCodex.decode(factory, RadiosetAnswer.class, step.answerJson()).as());
        } else if (step.answer().getSubmitStatus().getQuestionType() == QuestionType.numericScale ||
            step.answer().getSubmitStatus().getQuestionType() == QuestionType.slider) {
          surveyStudyStepObj.setSliderQuestion(AutoBeanCodex.decode(factory, SliderQuestion.class, step.questionJson()).as());
          surveyStudyStepObj.setNumericAnswer(AutoBeanCodex.decode(factory, NumericAnswer.class, step.answerJson()).as());
        } else if (step.answer().getSubmitStatus().getQuestionType() == QuestionType.textList) {
          surveyStudyStepObj.setTextInputQuestion(AutoBeanCodex.decode(factory, TextInputQuestion.class, step.questionJson()).as());
          surveyStudyStepObj.setTextInputAnswer(AutoBeanCodex.decode(factory, TextInputAnswer.class, step.answerJson()).as());
        } else {
          surveyStudyStepObj.setQuestion(AutoBeanCodex.decode(factory, FormQuestion.class, step.questionJson()).as());
          surveyStudyStepObj.setAnswer(AutoBeanCodex.decode(factory, FormAnswer.class, step.answerJson()).as());
        }
        steps.add(surveyStudyStepObj);
      }
    }
    studyObj.setSteps(steps);
    return studyObj;
  }

  private SurveyRegistrationObj getSurveyRegistrationObj(SurveyRegistration registration) {
    SurveyRegistrationObj surveyRegistrationObj = factory.surveyRegistrationObj().as();
    surveyRegistrationObj.setSurveyId(registration.getSurveyRegId());
    surveyRegistrationObj.setSiteId(registration.getSurveySiteId());
    surveyRegistrationObj.setPatientId(registration.getPatientId());
    String surveyDateString = (registration.getSurveyDt()
        == null) ? "" : siteInfo.getDateOnlyFormatter().getDateString(registration.getSurveyDt());
    surveyRegistrationObj.setSurveyDt(surveyDateString);
    surveyRegistrationObj.setToken(registration.getToken());
    surveyRegistrationObj.setSurveyType(registration.getSurveyType());
    surveyRegistrationObj.setAssessmentId(registration.getAssessmentRegId());
    surveyRegistrationObj.setSurveyName(registration.getSurveyName());
    surveyRegistrationObj.setNumberCompleted(registration.getNumberCompleted());
    surveyRegistrationObj.setNumberPending(registration.getNumberPending());
    return surveyRegistrationObj;
  }


  AssessmentObj getAssessmentObj(AssessDao assessDao, Long assessmentId) throws NotFoundException {
    AssessmentRegistration assessmentRegistration = assessDao.getAssessmentById(new AssessmentId(assessmentId));
    return getAssessmentObj(assessDao, assessmentRegistration);
  }


  private AssessmentObj getAssessmentObj(AssessDao assessDao, AssessmentRegistration assessmentRegistration) throws NotFoundException {
    if (assessmentRegistration == null) {
      throw new NotFoundException("Assessment");
    }
    AssessmentObj assessmentObj = factory.assessmentObj().as();
    assessmentObj.setAssessmentId(assessmentRegistration.getAssessmentRegId());
    assessmentObj.setSiteId(assessmentRegistration.getSurveySiteId());
    assessmentObj.setPatientId(assessmentRegistration.getPatientId());
    assessmentObj.setEmailAddr(assessmentRegistration.getEmailAddr());
    assessmentObj.setAssessmentDt(dateString(assessmentRegistration.getAssessmentDt()));
    assessmentObj.setAssessmentType(assessmentRegistration.getAssessmentType());
    ArrayList<SurveyRegistrationObj> registrations = new ArrayList<>();
    for (SurveyRegistration registration : assessmentRegistration.getSurveyRegList()) {
      SurveyRegistrationObj surveyRegistrationObj = getSurveyRegistrationObj(registration);
      registrations.add(surveyRegistrationObj);
    }
    assessmentObj.setSurveyRegistrations(registrations);

    ArrayList<ApptObj> appointments = new ArrayList<>();
    ApptRegistration apptRegistration =
        assessDao.getApptRegistrationByAssessmentId(new AssessmentId(assessmentRegistration.getAssessmentRegId()));
    if (apptRegistration == null) {
      throw new NotFoundException("Registration for Assessment");
    }
    appointments.add(getApptObj(apptRegistration));
    assessmentObj.setAppointments(appointments);

    ArrayList<NotificationObj> notifications = new ArrayList<>();
    ArrayList<Notification> notificationList = assessDao.getSentNotifications(new AssessmentId(assessmentRegistration.getAssessmentRegId()));
    for (Notification notification : notificationList) {
      NotificationObj notificationObj = factory.notificationObj().as();
      notificationObj.setNotificationId(notification.getNotificationId());
      notificationObj.setSiteId(notification.getSurveySiteId());
      notificationObj.setAssessmentId(notification.getAssessmentRegId());
      notificationObj.setPatientId(notification.getPatientId());
      notificationObj.setSurveyType(notification.getEmailType());
      notificationObj.setSurveyDt(dateString(notification.getSurveyDt()));
      notificationObj.setEmailDt(dateString(notification.getEmailDt()));
      notifications.add(notificationObj);
    }
    assessmentObj.setNotificationList(notifications);
    return assessmentObj;
  }

  ArrayList<AssessmentObj> getAssessmentObjs(AssessDao assessDao, String patientId) throws NotFoundException {

    PatientRegistrationSearch searchOptions = new PatientRegistrationSearch();

    ArrayList<PatientRegistration> registrations = assessDao.getPatientRegistrations(patientId, searchOptions);
    ArrayList<AssessmentObj> assessmentObjs = new ArrayList<>();
    for (PatientRegistration patientRegistration : registrations) {
      assessmentObjs.add(getAssessmentObj(assessDao, patientRegistration.getAssessment()));
    }
    return assessmentObjs;
  }

  private ApptObj getApptObj(ApptRegistration apptRegistration) {
    ApptObj apptObj = factory.apptObj().as();
    apptObj.setApptId(apptRegistration.getAssessmentRegId());
    apptObj.setSiteId(apptRegistration.getSurveySiteId());
    apptObj.setPatientId(apptRegistration.getPatientId());
    apptObj.setAssessmentId(apptRegistration.getAssessmentId().getId());
    apptObj.setVisitDt(dateString(apptRegistration.getVisitDt()));
    apptObj.setVisitType(apptRegistration.getVisitType());
    apptObj.setRegistrationType(apptRegistration.getRegistrationType());
    apptObj.setComplete(apptRegistration.getApptComplete());
    apptObj.setClinic(apptRegistration.getClinic());
    apptObj.setEncounter(apptRegistration.getEncounterEid());
    if (apptRegistration.getProviderId() != null) {
      Provider provider = getProvider(apptRegistration.getProviderId());
      String providerEid = provider == null ? "" : provider.getProviderEid();
      apptObj.setProvider(providerEid == null ? "" : providerEid);
    }
    return apptObj;
  }

  private ArrayList<PatientStudyExtendedData> getPatientStudyExtendedDataByToken(String token) {
    PatStudyDao patStudyDao = new PatStudyDao(dbp.get(), siteInfo);
    return patStudyDao.getPatientStudyExtendedDataByToken(token);
  }

  private Provider getProvider(long providerId) {
    UserDao userDao = new UserDao(dbp.get(), user, user);
    return userDao.getProvider(providerId);
  }

  private String dateString(Date date) {
    return (date == null) ? "" : siteInfo.getDateOnlyFormatter().getDateString(date);
  }

  private String timeString(Date time) {
    return (time == null) ? "" : siteInfo.getDateFormatter().getDateString(time);
  }

  private ArrayList<SiteInfo> getSurveySites() {
    return new SiteDao(dbp.get()).getSurveySites();
  }

  public ArrayList<SiteObj> getSiteObjs() {
    ArrayList<SiteObj> objs = new ArrayList<>();
    ArrayList<SiteInfo> sites = getSurveySites();
    for (SiteInfo siteInfo : sites) {
      objs.add(getSiteObj(siteInfo));
    }
    return objs;
  }

  SiteObj getSiteByParam(String param) {
    if (param == null) {
      return null;
    }
    ArrayList<SiteInfo> sites = getSurveySites();
    for (SiteInfo siteInfo : sites) {
      if (param.equals(siteInfo.getUrlParam())) {
        return getSiteObj(siteInfo);
      }
    }
    return null;
  }

  SiteObj getSiteById(Long siteId) {
    if (siteId == null) {
      return null;
    }
    ArrayList<SiteInfo> sites = getSurveySites();
    for (SiteInfo siteInfo : sites) {
      if (Objects.equals(siteId, siteInfo.getSiteId())) {
        return getSiteObj(siteInfo);
      }
    }
    return null;
  }

  private SiteObj getSiteObj(SiteInfo siteInfo) {
    SiteObj siteObj = factory.siteObj().as();
    siteObj.setSiteId(siteInfo.getSiteId());
    siteObj.setUrlParam(siteInfo.getUrlParam());
    siteObj.setDisplayName(siteInfo.getDisplayName());
    siteObj.setEnabled("Y");
    return siteObj;
  }
}
