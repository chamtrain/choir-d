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

package edu.stanford.survey.server;

import edu.stanford.survey.client.api.DisplayStatus;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.SessionStatus;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.client.api.SurveyService;
import edu.stanford.survey.client.api.SurveySite;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Server-side implementation of a survey provider.
 */
@SuppressWarnings("StaticNonFinalField")
public class SurveyServiceImpl implements SurveyService {
  private static final Logger log = Logger.getLogger(SurveyServiceImpl.class);
  private static final long RESUME_TIMEOUT_MILLIS = 30000L;
  private static long lastBadTokenTime;
  private static final Object lastBadTokenLock = new Object();
  private static final String DEFAULT_PAGE_TITLE = "";
  private static final String DEFAULT_STYLESHEET_NAME = "default.css";
  private final Supplier<Database> database;
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private final SurveySystemFactory providerFactory;
  private final SurveyDao surveyDao;
  private final SessionKeyGenerator keyGenerator;
  private final ClientIdentifiers clientIds;
  private boolean useProbingDelay = false;

  public SurveyServiceImpl(SurveySystemFactory providerFactory, Supplier<Database> database, ClientIdentifiers clientIds) {
    this(providerFactory, new SurveyDao(database), new SessionKeyGenerator(), database, clientIds);
    useProbingDelay = true;
  }

  // Used by tests
  public SurveyServiceImpl(SurveySystemFactory providerFactory, SurveyDao surveyDao, SessionKeyGenerator keyGenerator,
                           Supplier<Database> database, ClientIdentifiers clientIds) {
    this.providerFactory = providerFactory;
    this.surveyDao = surveyDao;
    this.keyGenerator = keyGenerator;
    this.database = database;
    this.clientIds = clientIds;
  }

  @Override
  public String[] startSurvey(String systemName, String surveyToken) {
    String prefix = "Starting: (" + systemName + "/" + surveyToken + ") ";

    try {
      // Need somebody who knows how to handle these survey tokens
      final Long siteId = providerFactory.siteIdFor(systemName);
      if (siteId == null) {
        log.error("Unable to lookup siteId for systemName " + systemName);
        return log(prefix, withDefaultDisplayOptions(unableToStartPage()));
      }
      SurveySystem surveySystem = providerFactory.systemForSiteId(siteId);
      if (surveySystem == null) {
        log.error("Unable to lookup survey system for siteId " + siteId);
        return log(prefix, withDefaultDisplayOptions(unableToStartPage()));
      }

      // No token ==> clinic tablet; prompt to type in the token
      if (surveyToken == null) {
        Question question = tokenLookupQuestion(systemName, surveySystem);
        return log(prefix, question);
      }

      tokenProbingDelay();

      // Bail out if the provided token is no good
      try {
        String newSurveyToken = surveySystem.validateStartToken(surveyToken);

        // Feature to automatically provide tokens if the survey system requests it
        if (newSurveyToken == null) {
          newSurveyToken = keyGenerator.create();
        }

        if (!surveyToken.equals(newSurveyToken)) {
          prefix = prefix.substring(0, prefix.length() - 2) + "/" + newSurveyToken + ") ";
        }
        surveyToken = newSurveyToken;
      } catch (TokenInvalidException e) {
        log.error("Invalid token " + surveyToken + " for survey system " + systemName, e);
        tokenProbingMark();
        return logWarn(prefix, withDisplayOptions(unableToStartPage(e.getValidationMessage()), siteId));
      }

      return startOrContinueWithValidToken(siteId, surveySystem, surveyToken, prefix);
    } catch (Exception e) {
      log.error("Unexpected error starting with token " + surveyToken + " for survey system " + systemName, e);
      return logWarn(prefix, withDefaultDisplayOptions(unableToStartPage()));
    }
  }

  @Override
  public String[] continueSurvey(String statusJson, String answerJson) {
    if (log.isDebugEnabled()) {
      log.debug("Submitted: " + statusJson + " " + answerJson);
    }

    String prefix = "Continuing: ";
    SubmitStatus submitStatus = AutoBeanCodex.decode(factory, SubmitStatus.class, statusJson).as();
    String sessionToken = submitStatus.getSessionToken();
    String questionId = submitStatus.getQuestionId();

    if (sessionToken == null && questionId != null && questionId.startsWith(":enterToken")) {
      tokenProbingDelay();

      String systemName = submitStatus.getSurveySystemName();

      // Need somebody who knows how to handle these survey tokens
      final Long siteId = providerFactory.siteIdFor(systemName);
      if (siteId == null) {
        log.error("Unable to lookup siteId for systemName " + systemName);
        return log(prefix, withDefaultDisplayOptions(unableToStartPage()));
      }
      SurveySystem surveySystem = providerFactory.systemForSiteId(siteId);
      if (surveySystem == null) {
        log.error("Unable to lookup survey system for siteId " + siteId);
        return log(prefix, withDefaultDisplayOptions(unableToStartPage()));
      }

      // Extract the token from the token lookup page answer
      String surveyToken;
      if (questionId.endsWith(":default")) {
        surveyToken = new Answer(statusJson, submitStatus, answerJson).formFieldValue("code");
      } else {
        try {
          surveyToken = surveySystem.tokenLookup(answerJson);
        } catch (TokenInvalidException e) {
          log.error("Unable to lookup token from answer " + answerJson + " for survey system " + systemName, e);
          Question question = tokenLookupQuestion(systemName, surveySystem);
          question.getDisplayStatus().setSessionStatus(SessionStatus.tokenLookupInvalid);
          question.getDisplayStatus().setServerValidationMessage(e.getValidationMessage());
          return log(prefix, withDisplayOptions(question, siteId));
        }
      }

      // Verify the token is good
      try {
        String newSurveyToken = surveySystem.validateStartToken(surveyToken);

        // Feature to automatically provide tokens if the survey system requests it
        if (newSurveyToken == null) {
          newSurveyToken = keyGenerator.create();
        }

        if (!surveyToken.equals(newSurveyToken)) {
          prefix = prefix.substring(0, prefix.length() - 2) + "/" + newSurveyToken + ") ";
        }
        surveyToken = newSurveyToken;
      } catch (TokenInvalidException e) {
        log.error("Invalid token " + surveyToken + " for survey system " + systemName);
        /* if (InvalidCredentials.COMPLETE_MESSAGE.equals(e.getMessage())) {
          return log(prefix, withDisplayOptions(thankYouPage(siteId, null), siteId));
        } */
        tokenProbingMark();
        Question question = tokenLookupQuestion(systemName, surveySystem);
        question.getDisplayStatus().setSessionStatus(SessionStatus.tokenLookupInvalid);
        question.getDisplayStatus().setServerValidationMessage(e.getValidationMessage());
        return log(prefix, withDisplayOptions(question, siteId));
      }

      return startOrContinueWithValidToken(siteId, surveySystem, surveyToken, prefix);
    }

    if (sessionToken == null) {
      log.warn(
          "Client should have provided either a session token or a token lookup answer. Submitted: " + statusJson + " "
              + answerJson);
      return logWarn(prefix, withDisplayOptions(sessionExpiredPage(),
          providerFactory.siteIdFor(submitStatus.getSurveySystemName())));
    }

    // Find the current survey by session
    SurveyToken st = surveyDao.findSurveyTokenBySessionAndLockIt(sessionToken);
    if (st == null) {
      return logWarn(prefix, withDisplayOptions(sessionExpiredPage(),
          providerFactory.siteIdFor(submitStatus.getSurveySystemName())));
    }

    Answer answer = new Answer(statusJson, submitStatus, answerJson);
    // Make sure it has not already been completed
    if (st.isComplete()) {
      return log(prefix, withDisplayOptions(alreadyCompletedPage(), st.getSurveySiteId()));
    }

    // Need somebody who knows how to handle these survey tokens
    final Long siteId = st.getSurveySiteId();
    SurveySystem surveySystem = providerFactory.systemForSiteId(siteId);
    if (surveySystem == null) {
      log.error("Unable to lookup survey system for siteId " + siteId);
      return log(prefix, withDefaultDisplayOptions(unableToStartPage()));
    }

    // Make sure the token is still valid
    String surveyToken = st.getSurveyToken();
    try {
      surveySystem.revalidateToken(surveyToken);
    } catch (TokenInvalidException e) {
//      log.error("Invalid token " + surveyToken + " for survey system siteId " + siteId, e);
      tokenProbingMark();
      return logWarn(prefix, withDisplayOptions(noLongerAvailable(), siteId));
    }

    // Make sure we haven't already received an answer for this step
    Long lastStepNumber = st.getLastStepNumber();
    Long surveyTokenId = st.getSurveyTokenId();
    long stepNumber = submitStatus.getStepNumber();
    if (stepNumber != lastStepNumber) {
      SurveyProgress ignoredStep = new SurveyProgress();
      ignoredStep.setStepNumber(stepNumber);
      ignoredStep.setSurveySiteId(siteId);
      ignoredStep.setSurveyTokenId(surveyTokenId);
      ignoredStep.setAnswerApiCompatLevel(submitStatus.getCompatLevel());
      ignoredStep.setSubmitStatusJsonId(surveyDao.createJson(siteId, statusJson));
      ignoredStep.setAnswerJsonId(surveyDao.createJson(siteId, answerJson));
      ignoredStep.setAnswerTime(new Date());
      ignoredStep.setQuestionType(submitStatus.getQuestionType().name());
      ignoredStep.setCallTimeMillis(submitStatus.getCallTimeMillis());
      ignoredStep.setRenderTimeMillis(submitStatus.getRenderTimeMillis());
      ignoredStep.setThinkTimeMillis(submitStatus.getThinkTimeMillis());
      ignoredStep.setRetryCount(submitStatus.getRetryCount());
      surveyDao.createProgressDup(ignoredStep);

      SurveyProgress lastStep = surveyDao.findProgress(siteId, surveyTokenId, lastStepNumber);
      if (lastStep == null) {
        log.error("Couldn't find progress " + siteId + " " + surveyTokenId + " " + lastStepNumber);
        return log(prefix, withDisplayOptions(unableToStartPage(), siteId));
      }

      log.debug("Re-sending step " + lastStepNumber + " for surveyTokenId " + surveyTokenId);
      String jsonString = surveyDao.findJson(lastStep.getDisplayStatusJsonId());
      try {
        DisplayStatus displayStatus = AutoBeanCodex.decode(factory, DisplayStatus.class, jsonString).as();
        setDisplayOptions(displayStatus, siteId, surveySystem.getProgress(surveyToken));
        return log(prefix, displayStatus,
            surveyDao.findJson(lastStep.getQuestionJsonId()), false);
      } catch (Exception ex) {
        log.error("invalid displayStatus json: " + jsonString);
      }
      return log(prefix, jsonString,
          surveyDao.findJson(lastStep.getQuestionJsonId()), false);
    }

    // Locate the latest step in the survey
    Long nextStepNumber = lastStepNumber + 1;
    SurveyProgress lastStep = surveyDao.findProgress(siteId, surveyTokenId, lastStepNumber);
    if (lastStep == null) {
      log.error("Couldn't find progress " + siteId + " " + surveyTokenId + " " + lastStepNumber);
      return log(prefix, withDisplayOptions(unableToStartPage(), siteId));
    }
    lastStep.setAnswerApiCompatLevel(submitStatus.getCompatLevel());
    lastStep.setSubmitStatusJsonId(surveyDao.createJson(siteId, answer.getSubmitStatusJson()));
    lastStep.setAnswerJsonId(surveyDao.createJson(siteId, answer.getAnswerJson()));
    lastStep.setAnswerTime(new Date());
    lastStep.setCallTimeMillis(submitStatus.getCallTimeMillis());
    lastStep.setRenderTimeMillis(submitStatus.getRenderTimeMillis());
    lastStep.setThinkTimeMillis(submitStatus.getThinkTimeMillis());
    lastStep.setRetryCount(submitStatus.getRetryCount());

    // Defer to the survey system to validate and/or advance to next question
    SurveyQuery surveyQuery = new SurveyQuery(database, surveyDao, siteId);
    Question question = surveySystem.nextQuestion(answer, surveyQuery.surveyBySurveyTokenId(surveyTokenId));
    if (question == null) {
      question = withDisplayOptions(thankYouPage(siteId, surveyToken), siteId);
    }
    DisplayStatus displayStatus = question.getDisplayStatus();
    if (displayStatus.getSessionStatus() == null) {
      displayStatus.setSessionStatus(SessionStatus.question);
    }

    // Provider, section, question, and question type should already be set; update/override all others
    displayStatus.setCompatLevel(SurveyFactory.compatibilityLevel);
    displayStatus.setStepNumber(nextStepNumber);
    displayStatus.setSurveyToken(surveyToken);
    displayStatus.setSessionToken(sessionToken);
    displayStatus.setProgress(0);
    setDisplayOptions(displayStatus, siteId, surveySystem.getProgress(surveyToken));

    AutoBean<DisplayStatus> statusBean = AutoBeanUtils.getAutoBean(displayStatus);
    String displayStatusJson = AutoBeanCodex.encode(statusBean).getPayload();
    String questionJson = AutoBeanCodex.encode(question.getQuestion()).getPayload();

    st.setLastStepNumber(nextStepNumber);
    if (displayStatus.getSessionStatus() == SessionStatus.clearSession) {
      st.setComplete(true);
      surveyDao.updateSurveyTokenInvalidateSession(st);
    } else {
      surveyDao.updateSurveyTokenTouchSession(st);
    }

    SurveyProgress nextStep = new SurveyProgress();
    if (displayStatus.getSessionStatus() == SessionStatus.questionInvalid) {
      lastStep.setStepStatus("X");
      nextStep.setQuestionStepNumber(lastStep.getQuestionStepNumber());
    } else {
      lastStep.setStepStatus("A");
      nextStep.setQuestionStepNumber(nextStepNumber);
    }
    surveyDao.updateProgressAnswer(lastStep);
    nextStep.setSurveySiteId(siteId);
    nextStep.setSurveyTokenId(surveyTokenId);
    nextStep.setStepNumber(nextStepNumber);
    nextStep.setStepStatus("Q");
    nextStep.setSurveyName(question.getSurveyName());
    nextStep.setSurveyCompatLevel(question.getSurveyCompatLevel());
    nextStep.setDisplayStatusJsonId(surveyDao.createJson(siteId, displayStatusJson));
    nextStep.setProviderId(displayStatus.getSurveyProviderId());
    nextStep.setSectionId(displayStatus.getSurveySectionId());
    nextStep.setQuestionId(displayStatus.getQuestionId());
    nextStep.setQuestionType(displayStatus.getQuestionType().name());
    nextStep.setQuestionApiCompatLevel(displayStatus.getCompatLevel());
    nextStep.setQuestionJsonId(surveyDao.createJson(siteId, questionJson));
    nextStep.setQuestionTime(new Date());
    nextStep.setUserAgentId(clientIds.userAgentId(siteId));
    nextStep.setClientIpAddress(clientIds.getClientIpAddress());
    nextStep.setDeviceToken(clientIds.getDeviceToken());
    surveyDao.createProgress(nextStep);

    return log(prefix, withDisplayOptions(question, siteId));
  }


  @Override
  public String[] resumeSurvey(String resumeToken) {
    String prefix = "Resume (" + resumeToken + ") ";

    try {
      // Check to see if this survey has already been started
      SurveyToken st = surveyDao.findSurveyTokenByResumeAndLockIt(resumeToken);

      if (st == null) {
        return log(prefix, withDefaultDisplayOptions(sessionExpiredPage()));
      }
      if (new Date().after(st.getLastActive())) {
        return log(prefix, withDisplayOptions(sessionExpiredPage(), st.getSurveySiteId()));
      }

      if (st.isComplete()) {
        return log(prefix, withDisplayOptions(alreadyCompletedPage(), st.getSurveySiteId()));
      }

      // Need somebody who knows how to handle these survey tokens
      final Long siteId = st.getSurveySiteId();
      SurveySystem surveySystem = providerFactory.systemForSiteId(siteId);
      if (surveySystem == null) {
        log.error("Unable to lookup survey system for siteId " + siteId);
        return log(prefix, withDefaultDisplayOptions(unableToStartPage()));
      }

      // Bail out if the provided token is no good
      String surveyToken = st.getSurveyToken();
      try {
        surveySystem.revalidateToken(surveyToken);
      } catch (TokenInvalidException e) {
//        log.error("Invalid token " + surveyToken + " for survey siteId " + siteId, e);
        tokenProbingMark();
        return logWarn(prefix, withDisplayOptions(noLongerAvailable(), siteId));
      }

      return continueSurveyToken(siteId, surveySystem, st, prefix, surveyToken, true);
    } catch (Exception e) {
      log.error("Unexpected error resuming with token " + resumeToken, e);
      return logWarn(prefix, withDefaultDisplayOptions(unableToStartPage()));
    }
  }

  private String[] startOrContinueWithValidToken(Long siteId, SurveySystem surveySystem, String surveyToken, String prefix) {
    // Check to see if this survey has already been started
    SurveyToken st = surveyDao.findSurveyTokenAndLockIt(siteId, surveyToken);
    if (st != null) {
      if (st.isComplete()) {
        return log(prefix, withDisplayOptions(alreadyCompletedPage(), st.getSurveySiteId()));
      }

      return continueSurveyToken(siteId, surveySystem, st, prefix, surveyToken, false);
    }

    return startWithValidToken(siteId, surveySystem, prefix, surveyToken, null);
  }

  private String[] continueSurveyToken(Long siteId, SurveySystem surveySystem, SurveyToken st, String prefix,
                                       String surveyToken, boolean wasResumeTokenUsed) {
    // Start a new session and re-send the most recent question
    Long lastStepNumber = st.getLastStepNumber();
    Long surveyTokenId = st.getSurveyTokenId();
    SurveyProgress progress = surveyDao.findProgress(siteId, surveyTokenId, lastStepNumber);
    if (progress == null) {
      // Looks like an error occurred during the last step, leaving things in an inconsistent state;
      // try a bit of recovery
      log.error("Couldn't find progress for siteId=" + siteId + " surveyTokenId=" + surveyTokenId
          + " stepNumber=" + lastStepNumber);
      if (lastStepNumber == 1) {
        return startWithValidToken(siteId, surveySystem, prefix, surveyToken, st);
      }
      lastStepNumber--;
      progress = surveyDao.findProgress(siteId, surveyTokenId, lastStepNumber);
      if (progress == null) {
        // Don't know...giving up
        log.error("Couldn't find progress for siteId=" + siteId + " surveyTokenId=" + surveyTokenId
            + " stepNumber=" + lastStepNumber);
        return log(prefix, withDisplayOptions(unableToStartPage(), siteId));
      }
    }

    // Now potentially go backwards in the progress to skip steps with validation errors
    // and get to the "real" question
    Long originalQuestion = progress.getQuestionStepNumber();
    if (originalQuestion < lastStepNumber) {
      progress = surveyDao.findProgress(siteId, surveyTokenId, originalQuestion);
      if (progress == null) {
        log.error("Couldn't find progress " + siteId + " " + surveyTokenId + " " + originalQuestion);
        return log(prefix, withDisplayOptions(unableToStartPage(), siteId));
      }
    }

    Long questionApiCompatLevel = progress.getQuestionApiCompatLevel();
    if (questionApiCompatLevel != SurveyFactory.compatibilityLevel) {
      // Will need to handle this when we rev the API...should never happen for now
      log.error("Trying to restart a survey with incompatible format " + questionApiCompatLevel + " ("
          + siteId + " " + surveyTokenId + " " + originalQuestion + ")");
      return log(prefix, withDisplayOptions(unableToStartPage(), siteId));
    }

    long nextStepNumber = lastStepNumber + 1;
    st.setLastStepNumber(nextStepNumber);
    if (wasResumeTokenUsed) {
      surveyDao.updateSurveyTokenResumeSession(st, keyGenerator.create(), keyGenerator.create());
    } else {
      surveyDao.updateSurveyTokenRestartSession(st, keyGenerator.create(), keyGenerator.create());
    }

    AutoBean<DisplayStatus> displayStatusBean = factory.displayStatus();
    DisplayStatus displayStatus = displayStatusBean.as();

    displayStatus.setCompatLevel(questionApiCompatLevel);
    displayStatus.setStepNumber(nextStepNumber);
    displayStatus.setSurveyToken(surveyToken);
    displayStatus.setSessionToken(st.getSessionToken());
    displayStatus.setSessionStatus(SessionStatus.question);
    displayStatus.setResumeToken(st.getResumeToken());
    displayStatus.setResumeTimeoutMillis(RESUME_TIMEOUT_MILLIS);
    displayStatus.setQuestionType(QuestionType.valueOf(progress.getQuestionType()));
    displayStatus.setQuestionId(progress.getQuestionId());
    displayStatus.setSurveyProviderId(progress.getProviderId());
    displayStatus.setSurveySectionId(progress.getSectionId());
    setDisplayOptions(displayStatus, siteId, surveySystem.getProgress(surveyToken));

    progress.setStepNumber(nextStepNumber);
    progress.setStepStatus("Q");
    String statusJson = AutoBeanCodex.encode(displayStatusBean).getPayload();
    progress.setDisplayStatusJsonId(surveyDao.createJson(siteId, statusJson));
    String questionJson = surveyDao.findJson(progress.getQuestionJsonId());
    progress.setQuestionTime(new Date());
    progress.setUserAgentId(clientIds.userAgentId(siteId));
    progress.setClientIpAddress(clientIds.getClientIpAddress());
    progress.setDeviceToken(clientIds.getDeviceToken());

    surveyDao.createProgress(progress);

    return log(prefix, statusJson, questionJson, false);
  }

  private String[] startWithValidToken(Long siteId, SurveySystem surveySystem, String logPrefix, String surveyToken,
                                       SurveyToken st) {
    if (st == null) {
      st = new SurveyToken();
      st.setSurveySiteId(siteId);
      st.setSurveyToken(surveyToken);
      st.setSessionToken(keyGenerator.create());
      st.setResumeToken(keyGenerator.create());
      st.setLastStepNumber(1L);
      surveyDao.createSurveyToken(st);
    } else {
      surveyDao.updateSurveyTokenRestartSession(st, keyGenerator.create(), keyGenerator.create());
    }
    Long nextStepNumber = st.getLastStepNumber();

    SurveyQuery surveyQuery = new SurveyQuery(database, surveyDao, siteId);
    Survey survey = surveyQuery.surveyBySurveyTokenId(st.getSurveyTokenId());
    Question question = surveySystem.startWithValidToken(surveyToken, survey);
    if (question == null) {
      return log(logPrefix, withDisplayOptions(alreadyCompletedPage(), siteId));
    }

    SurveyProgress progress = new SurveyProgress();
    DisplayStatus displayStatus = question.getDisplayStatus();

    // Provider, section, question, and question type should already be set; update/override all others
    displayStatus.setCompatLevel(SurveyFactory.compatibilityLevel);
    displayStatus.setStepNumber(nextStepNumber);
    displayStatus.setSurveyToken(surveyToken);
    displayStatus.setSessionToken(st.getSessionToken());
    displayStatus.setSessionStatus(SessionStatus.question);
    displayStatus.setResumeToken(st.getResumeToken());
    displayStatus.setResumeTimeoutMillis(RESUME_TIMEOUT_MILLIS);
    setDisplayOptions(question.getDisplayStatus(), siteId, surveySystem.getProgress(surveyToken));

    AutoBean<DisplayStatus> statusBean = AutoBeanUtils.getAutoBean(displayStatus);
    String displayStatusJson = AutoBeanCodex.encode(statusBean).getPayload();
    String questionJson = AutoBeanCodex.encode(question.getQuestion()).getPayload();

    progress.setSurveySiteId(siteId);
    progress.setSurveyTokenId(st.getSurveyTokenId());
    progress.setStepNumber(nextStepNumber);
    progress.setStepStatus("Q");
    progress.setQuestionStepNumber(nextStepNumber);
    progress.setSurveyName(question.getSurveyName());
    progress.setSurveyCompatLevel(question.getSurveyCompatLevel());
    progress.setDisplayStatusJsonId(surveyDao.createJson(siteId, displayStatusJson));
    progress.setProviderId(displayStatus.getSurveyProviderId());
    progress.setSectionId(displayStatus.getSurveySectionId());
    progress.setQuestionId(displayStatus.getQuestionId());
    progress.setQuestionType(displayStatus.getQuestionType().name());
    progress.setQuestionApiCompatLevel(displayStatus.getCompatLevel());
    progress.setQuestionJsonId(surveyDao.createJson(siteId, questionJson));
    progress.setQuestionTime(new Date());
    progress.setUserAgentId(clientIds.userAgentId(siteId));
    progress.setClientIpAddress(clientIds.getClientIpAddress());
    progress.setDeviceToken(clientIds.getDeviceToken());

    surveyDao.createProgress(progress);

    return log(logPrefix, question);
  }

  private Question tokenLookupQuestion(String systemName, SurveySystem surveySystem) {
    Question question = surveySystem.tokenLookupQuestion();
    if (question == null) {
      question = withDisplayOptions(tokenLookupPage(), providerFactory.siteIdFor(systemName));
    } else {
      question.getDisplayStatus().setQuestionId(":enterToken");
    }
    question.getDisplayStatus().setSessionStatus(SessionStatus.tokenLookup);
    question.getDisplayStatus().setCompatLevel(SurveyFactory.compatibilityLevel);
    question.getDisplayStatus().setSurveySystemName(systemName);
    return withDisplayOptions(question, providerFactory.siteIdFor(systemName));
  }

  private String[] log(String prefix, Question question) {
    return log(prefix, question.getDisplayStatus(), question.getQuestion());
  }

  private String[] logWarn(String prefix, Question question) {
    String questionJson = AutoBeanCodex.encode(question.getQuestion()).getPayload();
    return log(prefix, question.getDisplayStatus(), questionJson, true);
  }

  private String[] log(String prefix, DisplayStatus status, AutoBean<?> question) {
    String questionJson = AutoBeanCodex.encode(question).getPayload();
    return log(prefix, status, questionJson, false);
  }

  private String[] log(String prefix, DisplayStatus status, String questionJson, boolean isWarn) {
    AutoBean<DisplayStatus> statusBean = AutoBeanUtils.getAutoBean(status);
    String statusJson = AutoBeanCodex.encode(statusBean).getPayload();

    return log(prefix, statusJson, questionJson, isWarn);
  }

  private String[] log(String prefix, String statusJson, String questionJson, boolean isWarn) {
    if (isWarn) {
      log.warn("Error page: " + prefix + statusJson + " " + questionJson);
    } else {
      log.debug(prefix + statusJson + " " + questionJson);
    }
    return new String[] { statusJson, questionJson };
  }

  private void tokenProbingDelay() {
    if (useProbingDelay) {
      synchronized (lastBadTokenLock) {
        if (lastBadTokenTime > System.currentTimeMillis() - 120000) {
          log.debug("Inserting 2 second pause to discourage token guessing...");
          try {
            Thread.sleep(2000L);
          } catch (InterruptedException e) {
            log.debug("Sleep interrupted", e);
          }
        }
      }
    }
  }

  private void tokenProbingMark() {
    if (useProbingDelay) {
      synchronized (lastBadTokenLock) {
        lastBadTokenTime = System.currentTimeMillis();
      }
    }
  }

  private Question unableToStartPage(String reason) {
    Question q = new Question(":error", QuestionType.form);
    q.getDisplayStatus().setSessionStatus(SessionStatus.clearSession);
    AutoBean<FormQuestion> form = q.formQuestion("Unable to start survey", reason);
    form.as().setTerminal(true);
    q.setQuestion(form);
    return q;
  }

  private Question unableToStartPage() {
    Question q = new Question(":error", QuestionType.form);
    q.getDisplayStatus().setSessionStatus(SessionStatus.clearSession);
    AutoBean<FormQuestion> form = q.formQuestion("Unable to start survey",
        "Make sure you are looking at the most recent email and try clicking the link again");
    form.as().setTerminal(true);
    q.setQuestion(form);
    return q;
  }

  private Question noLongerAvailable() {
    Question q = new Question(":error", QuestionType.form);
    q.getDisplayStatus().setSessionStatus(SessionStatus.clearSession);
    AutoBean<FormQuestion> form = q.formQuestion("Unable to start survey",
        "Make sure you are looking at the most recent email and try clicking the link again");
    form.as().setTerminal(true);
    q.setQuestion(form);
    return q;
  }

  private Question sessionExpiredPage() {
    Question q = new Question(":expired", QuestionType.form);
    q.getDisplayStatus().setSessionStatus(SessionStatus.clearSession);
    AutoBean<FormQuestion> form = q.formQuestion("Your survey session has expired",
        "Try reloading this page in your browser");
    form.as().setTerminal(true);
    q.setQuestion(form);
    return q;
  }

  private Question thankYouPage(Long siteId, String surveyToken) {
    // Get title and stylesheet from SurveySystem
    SurveySystem surveySystem = providerFactory.systemForSiteId(siteId);

    Question q = surveySystem.getThankYouPage(surveyToken);

    if (q == null) {
      q = new Question(":done", QuestionType.form);
      q.getDisplayStatus().setSessionStatus(SessionStatus.clearSession);
      AutoBean<FormQuestion> form = q.formQuestion("Thank you for completing this questionnaire.");
      form.as().setTerminal(true);
      q.setQuestion(form);
    }
    return q;
  }

  private Question alreadyCompletedPage() {
    Question q = new Question(":done", QuestionType.form);
    q.getDisplayStatus().setSessionStatus(SessionStatus.clearSession);
    AutoBean<FormQuestion> form = q.formQuestion("Survey already completed",
        "Make sure you are looking at the most recent email and try clicking the link again");
    form.as().setTerminal(true);
    q.setQuestion(form);
    return q;
  }

  private Question tokenLookupPage() {
    Question q = new Question(":enterToken:default");
    q.form("Ask the front desk to enter your code", q.field("code", FieldType.number, "PIN code"));
    return q;
  }

  private Question withDisplayOptions(Question q, Long siteId) {
    double progress = q.getDisplayStatus() != null ? q.getDisplayStatus().getProgress() : 0;
    setDisplayOptions(q.getDisplayStatus(), siteId, progress);
    return q;
  }

  private void setDisplayOptions(DisplayStatus status, Long siteId, double progress) {
    String pageTitle = null;
    String styleSheetName = null;

    // Get title and stylesheet from SurveySystem
    SurveySystem surveySystem = providerFactory.systemForSiteId(siteId);
    if (surveySystem != null) {
      pageTitle = surveySystem.getPageTitle();
      styleSheetName = surveySystem.getStyleSheetName();
    }

    // If not specified in SurveySystem get from SurveySystemFactory
    if (pageTitle == null) {
      pageTitle = providerFactory.getPageTitle(siteId);
    }
    if (styleSheetName == null) {
      styleSheetName = providerFactory.getStyleSheetName(siteId);
    }

    // Otherwise use defaults
    if (pageTitle == null) {
      pageTitle = DEFAULT_PAGE_TITLE;
    }
    if (styleSheetName == null) {
      styleSheetName = DEFAULT_STYLESHEET_NAME;
    }

    status.setPageTitle(pageTitle);
    status.setStyleSheetName(styleSheetName);
    status.setProgress(progress);
  }

  private Question withDefaultDisplayOptions(Question q) {
    q.getDisplayStatus().setPageTitle(DEFAULT_PAGE_TITLE);
    q.getDisplayStatus().setStyleSheetName(DEFAULT_STYLESHEET_NAME);
    return q;
  }


  @Override
  public void addPlayerProgress(String statusJson, String targetId, String action, Long milliseconds) {
    SubmitStatus submitStatus = AutoBeanCodex.decode(factory, SubmitStatus.class, statusJson).as();
    String sessionToken = submitStatus.getSessionToken();
    SurveyToken st = surveyDao.findSurveyTokenBySessionAndLockIt(sessionToken);
    String systemName = submitStatus.getSurveySystemName();

    // Need somebody who knows how to handle these survey tokens
    final Long siteId = providerFactory.siteIdFor(systemName);
    if (siteId == null) {
      log.error("Cant add video progress! Unable to lookup siteId for systemName " + systemName);
      return;
    }
    log.debug(
        "addPlayerProgress(" + siteId + "," + st.getSurveyTokenId() + "," + targetId + "," + action + "," + milliseconds
            + ")");
    surveyDao.addPlayerProgress(siteId, st.getSurveyTokenId(), targetId, action, milliseconds);
  }


  @Override
  public String[] getSurveySites() {

    SurveySystem surveySystem = providerFactory.systemForSiteId(1L);
    ArrayList<SurveySite> sites = surveySystem.getSurveySites();
    sites.sort(new SurveySiteComparator());
    String[] surveySites = new String[sites.size()];
    int cnt = 0;
    for (SurveySite surveySite : sites) {
      AutoBean<SurveySite> siteBean = AutoBeanUtils.getAutoBean(surveySite);
      surveySites[cnt] = AutoBeanCodex.encode(siteBean).getPayload();
      cnt++;
    }
    return surveySites;
  }

  private class SurveySiteComparator implements Comparator<SurveySite> {
    @Override
    public int compare(SurveySite site1, SurveySite site2) {
      if (site1 == null || site2 == null) {
        return 0;
      }
      if (site1.getDisplayName() == null || site2.getDisplayName() == null) {
        return 0;
      }
      return site1.getDisplayName().compareTo(site2.getDisplayName());
    }
  }
}
