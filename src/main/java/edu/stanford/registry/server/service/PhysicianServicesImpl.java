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

import edu.stanford.registry.client.api.SurveyReport;
import edu.stanford.registry.client.api.SurveyReportFactory;
import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.randomset.RandomSetter;
import edu.stanford.registry.server.reports.JsonReport;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class PhysicianServicesImpl extends PatientServicesImpl implements PhysicianServices  {

  private Supplier<Database> dbp;
  private ServerContext serverContext;
  private static final Logger logger = LoggerFactory.getLogger(PhysicianServicesImpl.class);
  private SurveyReportFactory factory = AutoBeanFactorySource.create(SurveyReportFactory.class);
  private User user;

  @SuppressWarnings("unused")
  private PatientServicesImpl patientService;

  public PhysicianServicesImpl(User user, Supplier<Database> databaseProvider, ServerContext serverContext, SiteInfo siteInfo) {
    super(databaseProvider.get(), siteInfo);
    this.user = user;
    this.dbp = databaseProvider;
    this.serverContext = serverContext;
  }

  @Override
  public ArrayList<String> getProcessNames(String patientId) {
    XMLFileUtils xmlFileUtils = XMLFileUtils.getInstance(siteInfo);
    Date now = new Date();
    ArrayList<String> allProcessNames = xmlFileUtils.getProcessNamesByCategory("physician");
    ArrayList<String> activeProcessNames = new ArrayList<>();

    for (String name : allProcessNames) {
      if (xmlFileUtils.getActiveProcessForName(name, now) != null) {
        activeProcessNames.add(name);
      }
    }
    return activeProcessNames;
  }

  private Database getDatabase() {
    return dbp.get();
  }

  /*
  private Question nextQuestion(Answer answer, Survey survey) {

    SubmitStatus submitStatus = answer.getSubmitStatus();
    //Long surveyTokenId = survey.getSurveyTokenId();
    //String sessionToken = submitStatus.getSessionToken();
    NextQuestion nextQuestion =  handleResponse(null, answer.getSubmitStatusJson(), answer.getAnswerJson());
    if (nextQuestion == null) {
      return null;
    }
    return new Question(nextQuestion.getDisplayStatus(), nextQuestion.getQuestion());
  }/* */

  /*
   * Get the internal service to handle the request
   * /
  private PatientServicesImpl getPatientService() {
    if (patientService == null) {
      patientService = new PatientServicesImpl(getDatabase(), siteId);
    }
    return patientService;
  }/* */

  @Override
  public String createSurvey(String patientId, String processName) {

    try {
      PatientDao patientDao = new PatientDao(getDatabase(), siteId, user);
      SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
      Patient patient = patientDao.getPatient(patientId);
      if (patient == null) {
        logger.error("Patient not found for id:" + patientId);
        return null;
      }
      Date surveyDt = DateUtils.getDateEnd(siteInfo, new Date());
      ApptRegistration registration = new ApptRegistration(siteId, patientId, surveyDt,
          patient.getEmailAddress(), processName,
          Constants.REGISTRATION_TYPE_STANDALONE_SURVEY,
          "");
      if (user.getProviderEid() != null) {
        registration.setProviderId(user.getProviderId());
      }
      registration.setSendEmail(false);
      registration = surveyRegUtils.createRegistration(new AssessDao(dbp.get(), siteInfo), registration);
      logger.debug("starting fill out registration");
      surveyRegUtils.registerAssessments(getDatabase(), registration.getAssessment(), user);
      logger.debug("fill out registration is done, returning " + registration.getSurveyReg("Default").getToken());
      return registration.getSurveyReg("Default").getToken();
    } catch (Exception e) {
       logger.error("Cant start survey", e);
    }
    return null;
  }

  @Override
  public String getSurveyJson(ApptId apptId) {
    AssessDao assessDao = new AssessDao(getDatabase(), siteInfo);
    ApptRegistration apptRegistration = assessDao.getApptRegistrationByRegId(apptId);
    if (apptRegistration == null)
      return "";
    ArrayList<PatientStudyExtendedData> patientStudies = new ArrayList<>();
    List<SurveyRegistration> surveys = apptRegistration.getSurveyRegList();
    for(SurveyRegistration survey : surveys) {
      List<PatientStudyExtendedData> surveyPatStudies = getPatientStudies(survey);
      patientStudies.addAll(surveyPatStudies);
    }
    try {
      RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
      ChartConfigurationOptions opts = customizer.getConfigurationOptions();
      JsonReport jReport = new JsonReport(dbp, siteInfo, user);
      String surveyReport = (jReport.makeClientJson(patientStudies, apptRegistration.getSurveyType(), apptRegistration.getPatientId())).toString();
      logger.debug("surveyReport:" + surveyReport);
      SurveyReport bean = AutoBeanCodex.decode(factory, SurveyReport.class, surveyReport).as();
      AutoBean<SurveyReport> surveyReportBean = AutoBeanUtils.getAutoBean(bean);
      logger.warn("Persons name is " + bean.getName());
      try {
        return AutoBeanCodex.encode(surveyReportBean).getPayload();
      } catch (Exception abe) {
        logger.error("Error encoding surveyReportBean", abe);
      }
    } catch (IOException | org.json.JSONException | NullPointerException e ) {
       logger.error("Error generating the jsonreport ", e);
    }
    return "";

  }

  private ArrayList<PatientStudyExtendedData> getPatientStudies(SurveyRegistration surveyRegistration) {
    PatStudyDao patStudyDao = new PatStudyDao(getDatabase(), siteInfo);
    return patStudyDao.getPatientStudyDataBySurveyRegId(surveyRegistration.getSurveyRegId());
  }

  @Override
  public String getPhysicianSurveyPath() {
    String configSurveyPath = serverContext.appConfig().forName(siteId, "physician", "physician.survey.path");
    if (configSurveyPath == null) {
      configSurveyPath = "survey2";
    }
    return configSurveyPath;
  }

  @Override
  public Boolean isFinished(String token) {
    ActivityDao activityDao = new ActivityDao(getDatabase(), siteId);
    ArrayList<Activity> activities = activityDao.getActivityByToken(token, Constants.ACTIVITY_COMPLETED);
    return activities != null && activities.size() > 0;
  }

  @Override
  public String toString() {
    return "PhysicianServicesImpl with a user, dpb, serverContext and site="+siteId;
  }

  @Override
  public RandomSetParticipant updateRandomSetParticipant(RandomSetParticipant rsp) {
    RandomSetter randomSetter = siteInfo.getRandomSet(rsp.getName());
    if (randomSetter == null) { // defensive
      logger.error(siteInfo.getIdString()+" no RandomSet exists with name: "+rsp.getName());
      return rsp;
    }
    return randomSetter.updateParticipant(siteInfo, dbp.get(), user, rsp);
  }
}
