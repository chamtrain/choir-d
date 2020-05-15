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

package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfig;
import edu.stanford.registry.server.service.ApiClinicServicesUtils;
import edu.stanford.registry.server.service.PatientServicesImpl;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.InvalidCredentials;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.api.SiteObj;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.client.api.SurveySite;
import edu.stanford.survey.server.Answer;
import edu.stanford.survey.server.Question;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveySystemBase;
import edu.stanford.survey.server.TokenInvalidException;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Implementation of surveys for the Pain Registry.
 */
public class SurveySystemPainRegistry extends SurveySystemBase {
  private static final Logger log = Logger.getLogger(SurveySystemPainRegistry.class);
  private final Supplier<Database> database;
  private final SiteInfo siteInfo;
  private final AppConfig appConfig;
  private PatientServicesImpl patientService;
  private String lastValidatedSurveyToken;
  private PatientStudyExtendedData lastValidatedSurveyData;
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  public SurveySystemPainRegistry(Supplier<Database> database, AppConfig config, SiteInfo siteInfo) {
    this.database = database;
    this.siteInfo = siteInfo;
    this.appConfig = config;
  }

  private boolean unnec(PatientStudyExtendedData surveyData) throws InvalidCredentials {
    return surveyData.isValid();
  }
  
  @Override
  public String validateStartToken(String surveyToken) throws TokenInvalidException {
    lastValidatedSurveyToken = null;
    lastValidatedSurveyData = null;

    try {
      if (surveyToken != null && surveyToken.matches("[0-9]*")) {
         
        PatientStudyExtendedData surveyData = getPatientService().getPatientSurveyFromToken(surveyToken);
        if (surveyData != null && siteInfo.getSiteId().equals(surveyData.getSurveySiteId()) && unnec(surveyData)) {
          surveyToken = surveyData.getToken(); // it may have changed
          lastValidatedSurveyToken = surveyToken;
          lastValidatedSurveyData = surveyData;
          return surveyToken;
        }
      }
    } catch (InvalidCredentials e) {
      log.warn("Invalid survey token", e);
      throw new TokenInvalidException(e.getMessage());
    }

    throw new TokenInvalidException(InvalidCredentials.DEFAULT_MESSAGE);
  }

  @Override
  public void revalidateToken(String token) throws TokenInvalidException {
    validateStartToken(token);
  }

  @Override
  public Question startWithValidToken(String surveyToken, Survey survey) {
    assert surveyToken.equals(lastValidatedSurveyToken) && lastValidatedSurveyData != null;

    NextQuestion next = getPatientService().handleResponse(lastValidatedSurveyData, null, null);
    if (next == null) {
      // Can't come up with any questions...show generic already completed
      return null;
    }

    return new Question(next.getDisplayStatus(), next.getQuestion());
  }

  @Override
  public Question nextQuestion(Answer answer, Survey survey) {
    NextQuestion next = getPatientService().handleResponse(lastValidatedSurveyData, answer.getSubmitStatusJson(), answer.getAnswerJson());

    // Write a response activity
    writeActivity(lastValidatedSurveyData.getPatientId(), Constants.ACTIVITY_RESPONDED, lastValidatedSurveyToken);

    if (next == null) {
      // Mark the survey complete -- first pause for half a second to ensure the time is unique
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        log.error("Exception when trying to sleep before writing completed activity", e);
      }
      writeActivity(lastValidatedSurveyData.getPatientId(), Constants.ACTIVITY_COMPLETED, lastValidatedSurveyToken);
      return null;
    }
    return new Question(next.getDisplayStatus(), next.getQuestion());
  }

  @Override
  public Question getThankYouPage(String surveyToken) {
    if (surveyToken != null && surveyToken.matches("[0-9]*")) {
      return getPatientService().getThankYouPage(surveyToken, appConfig);
    }
    return null;
  }
  
  private void writeActivity(String patientId, String activityType, String token) {
    try {
      Activity act = new Activity(patientId, activityType, token);
      getPatientService().addActivity(act);
    } catch (Exception e) {
      log.error("Could not write the survey activity " + activityType + " for token " + token, e);
    }
  }

  /**
   * Get the internal service to handle the request
   */
  private PatientServicesImpl getPatientService() {
    if (patientService == null) {
      patientService = new PatientServicesImpl(database, siteInfo);
    }
    return patientService;
  }

  private ApiClinicServicesUtils getApiClinicServices() {
    return new ApiClinicServicesUtils(null, database, siteInfo);
  }

  @Override
  public ArrayList<SurveySite> getSurveySites() {
    ArrayList<SiteObj> siteObjs = getApiClinicServices().getSiteObjs();
    ArrayList<SurveySite> surveySites = new ArrayList<>();
    for (SiteObj siteObj : siteObjs) {
      if (includeOnSitesPage(siteObj.getDisplayName())) {
        SurveySite surveySite = factory.surveySite().as();
        surveySite.setDisplayName(siteObj.getDisplayName());
        surveySite.setEnabled(siteObj.getEnabled());
        surveySite.setUrlParam(siteObj.getUrlParam());
        surveySite.setSiteId(siteObj.getSiteId());
        surveySites.add(surveySite);
      }
    }
    return surveySites;
  }

  @Override
  public double getProgress(String surveyToken) {
    return  getPatientService().getProgress(surveyToken);
  }

  private boolean includeOnSitesPage(String name) {
    if (name.toLowerCase().contains("test")) {
      return false;
    }
    return !name.toLowerCase().contains("satisfaction");
  }

}
