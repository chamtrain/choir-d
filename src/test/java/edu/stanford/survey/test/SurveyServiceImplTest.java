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

package edu.stanford.survey.test;

import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.survey.client.api.DisplayStatus;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.SessionStatus;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.server.Answer;
import edu.stanford.survey.server.ClientIdentifiers;
import edu.stanford.survey.server.Question;
import edu.stanford.survey.server.SessionKeyGenerator;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyProgress;
import edu.stanford.survey.server.SurveyServiceImpl;
import edu.stanford.survey.server.SurveySystem;
import edu.stanford.survey.server.SurveySystemFactory;
import edu.stanford.survey.server.SurveyToken;
import edu.stanford.survey.server.TokenInvalidException;

import java.util.Date;

import org.easymock.IArgumentMatcher;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

import static org.easymock.EasyMock.*;

/**
 * Tests for SurveyServiceImpl.
 */
public class SurveyServiceImplTest extends DatabaseTestCase {
  private SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private static final Long SITE_ID = 999L;
  private static final Long OneLong = 01L; // else looks like a site...
  private static final String STYLESHEET_NAME = "default.css";
  private static final String PAGE_TITLE = "";
  
  /**
   * Survey system ID is garbage, display an error page immediately.
   */
  public void testStartInvalidSystemId() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);

    expect(systemFactory.siteIdFor("unknownsystem")).andReturn(null);
    replay(systemFactory, surveyDao, keyGenerator, clientIds);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.clearSession, ":error");
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    check(service.startSurvey("unknownsystem", "123"), expectedDispStatus, unableToStartForm());

    verify(systemFactory, surveyDao, keyGenerator, clientIds);
  }

  /**
   * Token provided but is invalid.
   */
  public void testStartInvalidToken() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);

    expect(systemFactory.siteIdFor("testsystem")).andReturn(SITE_ID);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.validateStartToken("123")).andThrow(new TokenInvalidException(null));
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.clearSession, ":error");
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    FormQuestion question = tokenLookupForm();
    question.setFields(null);
    question.setTerminal(true);
    question.setTitle1("Unable to start survey");
    check(service.startSurvey("testsystem", "123"), expectedDispStatus, question);
    verify(systemFactory, system, surveyDao, keyGenerator, clientIds);
  }

  /**
   * Token not provided, show default screen prompting for token.
   */
  public void testStartNoToken() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);

    expect(systemFactory.siteIdFor("testsystem")).andReturn(SITE_ID).anyTimes();
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system).anyTimes();
    expect(system.tokenLookupQuestion()).andReturn(null);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE).anyTimes();
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME).anyTimes();
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.tokenLookup, ":enterToken:default");
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    expectedDispStatus.setSurveySystemName("testsystem");
    check(service.startSurvey("testsystem", null), expectedDispStatus, tokenLookupForm());

    verify(systemFactory, system, surveyDao, keyGenerator, clientIds);
  }

  /**
   * Valid token provided, proceed to first question.
   */
  public void testStartGoodToken() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SurveyToken surveyToken = createMock(SurveyToken.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);

    expect(systemFactory.siteIdFor("testsystem")).andReturn(SITE_ID);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.validateStartToken("123")).andReturn("456");
    expect(surveyDao.findSurveyTokenAndLockIt(SITE_ID, "456")).andReturn(null);
    expect(system.startWithValidToken(eq("456"), anyObject(Survey.class))).andReturn(name());
    expect(keyGenerator.create()).andReturn("session777");
    expect(keyGenerator.create()).andReturn("resume888");
    surveyDao.createSurveyToken(check(new Check<SurveyToken>() {
      @Override
      public void check(SurveyToken param) {
        assertEquals(SITE_ID, param.getSurveySiteId());
        assertEquals("session777", param.getSessionToken());
        assertEquals("resume888", param.getResumeToken());
        assertEquals(OneLong, param.getLastStepNumber());
        assertEquals("456", param.getSurveyToken());

        param.setSurveyTokenId(555L);
        param.setLastActive(new Date());
      }
    }));
    expect(clientIds.userAgentId(SITE_ID)).andReturn(678L);
    expect(clientIds.getClientIpAddress()).andReturn("1.2.3.4,5.6.7.8");
    expect(clientIds.getDeviceToken()).andReturn("device123");
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.question, "name");
    expectedDispStatus.setSessionToken("session777");
    expectedDispStatus.setSurveyToken("456");
    expectedDispStatus.setStepNumber(OneLong);
    expectedDispStatus.setResumeToken("resume888");
    expectedDispStatus.setResumeTimeoutMillis(30000L);
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    FormQuestion expectedQuestion = nameForm();
    expect(surveyDao.createJson(eq(SITE_ID), displayStatus(expectedDispStatus))).andReturn(2L);
    expect(surveyDao.createJson(eq(SITE_ID), formQuestion(expectedQuestion))).andReturn(OneLong);
    surveyDao.createProgress(check(new Check<SurveyProgress>() {
      @Override
      public void check(SurveyProgress param) {
        assertEquals(SITE_ID, param.getSurveySiteId());
        assertEquals((Long)555L, param.getSurveyTokenId());
        assertEquals(OneLong, param.getStepNumber());

        assertEquals("Q", param.getStepStatus());
        assertEquals(OneLong, param.getQuestionStepNumber());
        assertEquals("testSurvey", param.getSurveyName());
        assertEquals((Long)3L, param.getSurveyCompatLevel());
        assertEquals(OneLong, param.getQuestionApiCompatLevel());
        assertEquals((Long)2L, param.getDisplayStatusJsonId());
        assertEquals(OneLong, param.getQuestionJsonId());
        assertNotNull(param.getQuestionTime());
        assertNull(param.getProviderId());
        assertNull(param.getSectionId());
        assertEquals("name", param.getQuestionId());
        assertEquals("form", param.getQuestionType());
        assertNull(param.getAnswerApiCompatLevel());
        assertNull(param.getSubmitStatusJsonId());
        assertNull(param.getAnswerJsonId());
        assertNull(param.getAnswerTime());
        assertEquals((Long)678L, param.getUserAgentId());
        assertEquals("1.2.3.4,5.6.7.8", param.getClientIpAddress());
        assertEquals("device123", param.getDeviceToken());
      }
    }));
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    expect(system.getProgress("456")).andReturn(0.0);
    replay(systemFactory, system, surveyDao, surveyToken, keyGenerator, clientIds);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    check(service.startSurvey("testsystem", "123"), expectedDispStatus, expectedQuestion);

    verify(systemFactory, system, surveyDao, surveyToken, keyGenerator, clientIds);
  }

  /**
   * Valid token provided, survey has already been completed, display message.
   */
  public void testStartGoodTokenCompleted() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SurveyToken surveyToken = createMock(SurveyToken.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);

    expect(systemFactory.siteIdFor("testsystem")).andReturn(SITE_ID);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.validateStartToken("123")).andReturn("123");
    expect(surveyDao.findSurveyTokenAndLockIt(SITE_ID, "123")).andReturn(surveyToken);
    expect(surveyToken.isComplete()).andReturn(true);
    expect(surveyToken.getSurveySiteId()).andReturn(SITE_ID);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    replay(systemFactory, system, surveyDao, surveyToken, keyGenerator, clientIds);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.clearSession, ":done");
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    check(service.startSurvey("testsystem", "123"), expectedDispStatus, alreadyCompletedForm());

    verify(systemFactory, system, surveyDao, surveyToken, keyGenerator, clientIds);
  }

  /**
   * Valid token provided, resume an in-progress survey.
   */
  public void testStartGoodTokenInProgress() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);
    SurveyToken surveyToken = createMock(SurveyToken.class);
    SurveyProgress surveyProgress = createMock(SurveyProgress.class);
    SurveyProgress surveyProgress2 = createMock(SurveyProgress.class);

    expect(systemFactory.siteIdFor("testsystem")).andReturn(SITE_ID);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.validateStartToken("123")).andReturn("123");
    expect(surveyDao.findSurveyTokenAndLockIt(SITE_ID, "123")).andReturn(surveyToken);
    expect(surveyToken.isComplete()).andReturn(false);
    expect(surveyToken.getSurveyTokenId()).andReturn(17L);
    expect(surveyToken.getLastStepNumber()).andReturn(3L);
    expect(surveyDao.findProgress(SITE_ID, 17L, 3L)).andReturn(surveyProgress);
    expect(surveyProgress.getQuestionStepNumber()).andReturn(2L);
    expect(surveyDao.findProgress(SITE_ID, 17L, 2L)).andReturn(surveyProgress2);
    expect(surveyProgress2.getQuestionApiCompatLevel()).andReturn(SurveyFactory.compatibilityLevel);
    surveyToken.setLastStepNumber(4L);
    expect(keyGenerator.create()).andReturn("session777");
    expect(keyGenerator.create()).andReturn("resume888");
    surveyDao.updateSurveyTokenRestartSession(surveyToken, "session777", "resume888");
    expect(surveyToken.getSessionToken()).andReturn("session777");
    expect(surveyToken.getResumeToken()).andReturn("resume888");
    expect(surveyProgress2.getQuestionType()).andReturn("form");
    expect(surveyProgress2.getQuestionId()).andReturn("q123");
    expect(surveyProgress2.getProviderId()).andReturn("p123");
    expect(surveyProgress2.getSectionId()).andReturn("s123");
    surveyProgress2.setStepNumber(4L);
    surveyProgress2.setStepStatus("Q");
    // TODO user agent, ip and metrics
    expect(surveyProgress2.getQuestionJsonId()).andReturn(21L);
    expect(surveyDao.findJson(21L)).andReturn("oldQuestionJson");
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.question, "q123");
    expectedDispStatus.setSessionToken("session777");
    expectedDispStatus.setSurveyToken("123");
    expectedDispStatus.setSurveySectionId("s123");
    expectedDispStatus.setSurveyProviderId("p123");
    expectedDispStatus.setStepNumber(4L);
    expectedDispStatus.setResumeToken("resume888");
    expectedDispStatus.setResumeTimeoutMillis(30000L);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    expect(surveyDao.createJson(eq(SITE_ID), displayStatus(expectedDispStatus))).andReturn(OneLong);
    surveyProgress2.setDisplayStatusJsonId(OneLong);
    surveyProgress2.setQuestionTime(notNull(Date.class));
    expect(clientIds.userAgentId(SITE_ID)).andReturn(678L);
    expect(clientIds.getClientIpAddress()).andReturn("1.2.3.4");
    expect(clientIds.getDeviceToken()).andReturn("device123");
    surveyProgress2.setUserAgentId(678L);
    surveyProgress2.setClientIpAddress("1.2.3.4");
    surveyProgress2.setDeviceToken("device123");
    surveyDao.createProgress(surveyProgress2);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    expect(system.getProgress("123")).andReturn(0.0);
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, surveyProgress, surveyProgress2);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    check(service.startSurvey("testsystem", "123"), expectedDispStatus, "oldQuestionJson");

    verify(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, surveyProgress, surveyProgress2);
  }

  /**
   * Successfully answer one question and advance to the next question.
   */
  public void testContinueQuestionToQuestion() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);
    SurveyToken surveyToken = createMock(SurveyToken.class);
    SurveyProgress progress = createMock(SurveyProgress.class);

    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(surveyDao.findSurveyTokenBySessionAndLockIt("session777")).andReturn(surveyToken);
    expect(surveyToken.isComplete()).andReturn(false);
    expect(surveyToken.getSurveyToken()).andReturn("123");
    system.revalidateToken("123");
    expect(surveyToken.getSurveySiteId()).andReturn(SITE_ID);
    expect(surveyToken.getSurveyTokenId()).andReturn(17L);
    expect(surveyToken.getLastStepNumber()).andReturn(3L);
    surveyToken.setLastStepNumber(4L);
    expect(surveyDao.findProgress(SITE_ID, 17L, 3L)).andReturn(progress);
    surveyDao.updateSurveyTokenTouchSession(surveyToken);

    expect(system.nextQuestion(anyObject(Answer.class), anyObject(Survey.class))).andReturn(name());

    progress.setAnswerApiCompatLevel(SurveyFactory.compatibilityLevel);
    expect(surveyDao.createJson(SITE_ID, "{\"sessionToken\":\"session777\",\"surveySectionId\":\"s123\",\"surveyToken\":\"123\","
        + "\"questionId\":\"q123\",\"compatLevel\":\"1\",\"questionType\":\"form\",\"stepNumber\":\"3\","
        + "\"surveyProviderId\":\"p123\",\"pageTitle\":\"\",\"styleSheetName\":\"registry.css\"}")).andReturn(31L);
    progress.setSubmitStatusJsonId(31L);
    expect(surveyDao.createJson(SITE_ID, "{\"fieldAnswers\":[{\"choice\":[\"dentist\"],\"fieldId\":\"occupation\"}]}")).andReturn(32L);
    progress.setAnswerJsonId(32L);
    progress.setAnswerTime(notNull(Date.class));
    progress.setStepStatus("A");
    progress.setCallTimeMillis(null);
    progress.setRenderTimeMillis(null);
    progress.setThinkTimeMillis(null);
    progress.setRetryCount(null);
    surveyDao.updateProgressAnswer(progress);
    expect(clientIds.userAgentId(SITE_ID)).andReturn(678L);
    expect(clientIds.getClientIpAddress()).andReturn("1.2.3.4");
    expect(clientIds.getDeviceToken()).andReturn("device123");
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.question, "name");
    expectedDispStatus.setSessionToken("session777");
    expectedDispStatus.setSurveyToken("123");
    expectedDispStatus.setStepNumber(4L);
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    FormQuestion expectedQuestion = nameForm();
    expect(surveyDao.createJson(eq(SITE_ID), displayStatus(expectedDispStatus))).andReturn(41L);
    expect(surveyDao.createJson(eq(SITE_ID), formQuestion(expectedQuestion))).andReturn(42L);
    surveyDao.createProgress(check(new Check<SurveyProgress>() {
      @Override
      public void check(SurveyProgress param) {
        assertEquals(SITE_ID, param.getSurveySiteId());
        assertEquals((Long)17L, param.getSurveyTokenId());
        assertEquals((Long)4L, param.getStepNumber());
        assertEquals("Q", param.getStepStatus());
        assertEquals((Long)4L, param.getQuestionStepNumber());
        // TODO survey name and compat level
        assertEquals(OneLong, param.getQuestionApiCompatLevel());
        assertEquals((Long)41L, param.getDisplayStatusJsonId());
        assertEquals((Long)42L, param.getQuestionJsonId());
        assertNotNull(param.getQuestionTime());
        assertNull(param.getProviderId());
        assertNull(param.getSectionId());
        assertEquals("name", param.getQuestionId());
        assertEquals("form", param.getQuestionType());
        assertNull(param.getAnswerApiCompatLevel());
        assertNull(param.getSubmitStatusJsonId());
        assertNull(param.getAnswerJsonId());
        assertNull(param.getAnswerTime());
        assertEquals((Long)678L, param.getUserAgentId());
        assertEquals("1.2.3.4", param.getClientIpAddress());
        assertEquals("device123", param.getDeviceToken());
      }
    }));
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    expect(system.getProgress("123")).andReturn(0.0);
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, progress);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    check(service.continueSurvey("{\"sessionToken\":\"session777\",\"surveySectionId\":\"s123\",\"surveyToken\":\"123\",\"questionId\":\"q123\",\"compatLevel\":\"1\",\"questionType\":\"form\",\"stepNumber\":\"3\",\"surveyProviderId\":\"p123\","+
        "\"pageTitle\":\"\",\"styleSheetName\":\"registry.css\"}",
        "{\"fieldAnswers\":[{\"choice\":[\"dentist\"],\"fieldId\":\"occupation\"}]}"), expectedDispStatus, expectedQuestion);

    verify(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, progress);
  }

  /**
   * Send a duplicate answer to a question and advance to the next question.
   */
  public void testContinueQuestionToQuestionDuplicate() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);
    SurveyToken surveyToken = createMock(SurveyToken.class);
    SurveyProgress progress = createMock(SurveyProgress.class);

    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(surveyDao.findSurveyTokenBySessionAndLockIt("session777")).andReturn(surveyToken);
    expect(surveyToken.isComplete()).andReturn(false);
    expect(surveyToken.getSurveyToken()).andReturn("123");
    system.revalidateToken("123");
    expect(surveyToken.getSurveySiteId()).andReturn(SITE_ID);
    expect(surveyToken.getSurveyTokenId()).andReturn(17L);
    expect(surveyToken.getLastStepNumber()).andReturn(3L);
    expect(surveyDao.createJson(SITE_ID, "{\"sessionToken\":\"session777\",\"surveySectionId\":\"s123\",\"surveyToken\":\"123\",\"questionId\":\"q123\","
        + "\"compatLevel\":\"1\",\"questionType\":\"form\",\"stepNumber\":\"2\",\"surveyProviderId\":\"p123\"}")).andReturn(41L);
    expect(surveyDao.createJson(SITE_ID, "{\"fieldAnswers\":[{\"choice\":[\"dentist\"],\"fieldId\":\"occupation\"}]}")).andReturn(42L);
    surveyDao.createProgressDup(check(new Check<SurveyProgress>() {
      @Override
      public void check(SurveyProgress param) {
        assertEquals(SITE_ID, param.getSurveySiteId());
        assertEquals((Long)17L, param.getSurveyTokenId());
        assertEquals((Long)2L, param.getStepNumber());

        assertEquals("form", param.getQuestionType());
        assertEquals(OneLong, param.getAnswerApiCompatLevel());
        assertEquals((Long)41L, param.getSubmitStatusJsonId());
        assertEquals((Long)42L, param.getAnswerJsonId());
        assertNotNull(param.getAnswerTime());
        assertNull(param.getUserAgentId());
        // TODO timings, user agent, ip
      }
    }));
    expect(surveyDao.findProgress(SITE_ID, 17L, 3L)).andReturn(progress);
    expect(progress.getDisplayStatusJsonId()).andReturn(5L);
    expect(surveyDao.findJson(5L)).andReturn("oldStatusJson");
    expect(progress.getQuestionJsonId()).andReturn(6L);
    expect(surveyDao.findJson(6L)).andReturn("oldQuestionJson");
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, progress);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    check(service.continueSurvey("{\"sessionToken\":\"session777\",\"surveySectionId\":\"s123\",\"surveyToken\":\"123\",\"questionId\":\"q123\","
        + "\"compatLevel\":\"1\",\"questionType\":\"form\",\"stepNumber\":\"2\",\"surveyProviderId\":\"p123\"}",
        "{\"fieldAnswers\":[{\"choice\":[\"dentist\"],\"fieldId\":\"occupation\"}]}"),
        "oldStatusJson",
        "oldQuestionJson");

    verify(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, progress);
  }

  // TODO continue finish one of multiple bundled surveys

  /**
   * Successfully answer the last question, and proceed to the thank you page.
   */
  public void testContinueFinishedDefault() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);
    SurveyToken surveyToken = createMock(SurveyToken.class);
    SurveyProgress progress = createMock(SurveyProgress.class);

    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system).anyTimes();
    expect(surveyDao.findSurveyTokenBySessionAndLockIt("session777")).andReturn(surveyToken);
    expect(surveyToken.isComplete()).andReturn(false);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE).anyTimes();
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME).anyTimes();
    expect(surveyToken.getSurveyToken()).andReturn("123");
    system.revalidateToken("123");
    expect(surveyToken.getSurveySiteId()).andReturn(SITE_ID);
    expect(surveyToken.getSurveyTokenId()).andReturn(17L);
    expect(surveyToken.getLastStepNumber()).andReturn(3L);
    surveyToken.setLastStepNumber(4L);
    surveyToken.setComplete(true);
    expect(surveyDao.findProgress(SITE_ID, 17L, 3L)).andReturn(progress);
    surveyDao.updateSurveyTokenInvalidateSession(surveyToken);

    expect(system.nextQuestion(anyObject(Answer.class), anyObject(Survey.class))).andReturn(null);

    progress.setAnswerApiCompatLevel(SurveyFactory.compatibilityLevel);
    expect(surveyDao.createJson(SITE_ID, "{\"sessionToken\":\"session777\",\"surveySectionId\":\"s123\",\"surveyToken\":\"123\",\"questionId\":\"q123\",\"compatLevel\":\"1\",\"questionType\":\"form\",\"stepNumber\":\"3\",\"surveyProviderId\":\"p123\",\"callTimeMillis\":\"200\",\"renderTimeMillis\":\"100\",\"thinkTimeMillis\":\"5000\",\"retryCount\":\"3\"}")).andReturn(31L);
    progress.setSubmitStatusJsonId(31L);
    expect(surveyDao.createJson(SITE_ID, "{\"fieldAnswers\":[{\"choice\":[\"dentist\"],\"fieldId\":\"occupation\"}]}")).andReturn(32L);
    progress.setAnswerJsonId(32L);
    progress.setAnswerTime(notNull(Date.class));
    progress.setStepStatus("A");
    progress.setCallTimeMillis(200L);
    progress.setRenderTimeMillis(100L);
    progress.setThinkTimeMillis(5000L);
    progress.setRetryCount(3L);
    surveyDao.updateProgressAnswer(progress);
    expect(clientIds.userAgentId(SITE_ID)).andReturn(678L);
    expect(clientIds.getClientIpAddress()).andReturn("1.2.3.4");
    expect(clientIds.getDeviceToken()).andReturn("device123");
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.clearSession, ":done");
    expectedDispStatus.setSessionToken("session777");
    expectedDispStatus.setSurveyToken("123");
    expectedDispStatus.setStepNumber(4L);
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    //expectedDispStatus.setSurveySystemName("testsystem");
    FormQuestion expectedQuestion = thankYouForm();
    expect(surveyDao.createJson(eq(SITE_ID), displayStatus(expectedDispStatus))).andReturn(41L);
    expect(surveyDao.createJson(eq(SITE_ID), formQuestion(expectedQuestion))).andReturn(42L);
    surveyDao.createProgress(check(new Check<SurveyProgress>() {
      @Override
      public void check(SurveyProgress param) {
        assertEquals(SITE_ID, param.getSurveySiteId());
        assertEquals((Long)17L, param.getSurveyTokenId());
        assertEquals((Long)4L, param.getStepNumber());
        // TODO survey name and compat level
        assertEquals("Q", param.getStepStatus());
        assertEquals((Long)4L, param.getQuestionStepNumber());
        assertEquals(OneLong, param.getQuestionApiCompatLevel());
        assertEquals((Long)41L, param.getDisplayStatusJsonId());
        assertEquals((Long)42L, param.getQuestionJsonId());
        assertNotNull(param.getQuestionTime());
        assertNull(param.getProviderId());
        assertNull(param.getSectionId());
        assertEquals(":done", param.getQuestionId());
        assertEquals("form", param.getQuestionType());
        assertNull(param.getAnswerApiCompatLevel());
        assertNull(param.getSubmitStatusJsonId());
        assertNull(param.getAnswerJsonId());
        assertNull(param.getAnswerTime());
        assertEquals((Long)678L, param.getUserAgentId());
        assertEquals("1.2.3.4", param.getClientIpAddress());
        assertEquals("device123", param.getDeviceToken());
      }
    }));
    expect(system.getThankYouPage("123")).andReturn(null);
    expect(system.getProgress("123")).andReturn(0.0);
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, progress);
    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    check(service.continueSurvey("{\"sessionToken\":\"session777\",\"surveySectionId\":\"s123\",\"surveyToken\":\"123\",\"questionId\":\"q123\",\"compatLevel\":\"1\",\"questionType\":\"form\",\"stepNumber\":\"3\",\"surveyProviderId\":\"p123\",\"callTimeMillis\":\"200\",\"renderTimeMillis\":\"100\",\"thinkTimeMillis\":\"5000\",\"retryCount\":\"3\"}",
        "{\"fieldAnswers\":[{\"choice\":[\"dentist\"],\"fieldId\":\"occupation\"}]}"), expectedDispStatus, expectedQuestion);

    verify(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, progress);
  }

  /**
   * Answer a question, but the answer fails server-side validation. Bounce back
   * to the same question with a message.
   */
  public void testContinueQuestionServerValidation() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);
    SurveyToken surveyToken = createMock(SurveyToken.class);
    SurveyProgress progress = createMock(SurveyProgress.class);

    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(surveyDao.findSurveyTokenBySessionAndLockIt("session777")).andReturn(surveyToken);
    expect(surveyToken.isComplete()).andReturn(false);
    expect(surveyToken.getSurveyToken()).andReturn("123");
    system.revalidateToken("123");
    expect(surveyToken.getSurveySiteId()).andReturn(SITE_ID);
    expect(surveyToken.getSurveyTokenId()).andReturn(17L);
    expect(surveyToken.getLastStepNumber()).andReturn(3L);
    surveyToken.setLastStepNumber(4L);
    expect(surveyDao.findProgress(SITE_ID, 17L, 3L)).andReturn(progress);
    surveyDao.updateSurveyTokenTouchSession(surveyToken);

    expect(system.nextQuestion(anyObject(Answer.class), anyObject(Survey.class))).andReturn(nameValidationFailed());

    expect(progress.getQuestionStepNumber()).andReturn(2L);
    progress.setAnswerApiCompatLevel(SurveyFactory.compatibilityLevel);
    expect(surveyDao.createJson(SITE_ID, "{\"sessionToken\":\"session777\",\"surveySectionId\":\"s123\",\"surveyToken\":\"123\",\"questionId\":\"q123\",\"compatLevel\":\"1\",\"questionType\":\"form\",\"stepNumber\":\"3\",\"surveyProviderId\":\"p123\",\"pageTitle\":\"\",\"styleSheetName\":\"registry.css\"}")).andReturn(31L);
    progress.setSubmitStatusJsonId(31L);
    expect(surveyDao.createJson(SITE_ID, "{\"fieldAnswers\":[{\"choice\":[\"dentist\"],\"fieldId\":\"occupation\"}]}")).andReturn(32L);
    progress.setAnswerJsonId(32L);
    progress.setAnswerTime(notNull(Date.class));
    progress.setStepStatus("X");
    progress.setCallTimeMillis(null);
    progress.setRenderTimeMillis(null);
    progress.setThinkTimeMillis(null);
    progress.setRetryCount(null);
    surveyDao.updateProgressAnswer(progress);
    expect(clientIds.userAgentId(SITE_ID)).andReturn(678L);
    expect(clientIds.getClientIpAddress()).andReturn("1.2.3.4");
    expect(clientIds.getDeviceToken()).andReturn("device123");
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.questionInvalid, "name");
    expectedDispStatus.setSessionToken("session777");
    expectedDispStatus.setSurveyToken("123");
    expectedDispStatus.setStepNumber(4L);
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    FormQuestion expectedQuestion = nameForm();
    expectedQuestion.setServerValidationMessage("Wrong patient name");
    expect(surveyDao.createJson(eq(SITE_ID), displayStatus(expectedDispStatus))).andReturn(41L);
    expect(surveyDao.createJson(eq(SITE_ID), formQuestion(expectedQuestion))).andReturn(42L);
    surveyDao.createProgress(check(new Check<SurveyProgress>() {
      @Override
      public void check(SurveyProgress param) {
        assertEquals(SITE_ID, param.getSurveySiteId());
        assertEquals((Long)17L, param.getSurveyTokenId());
        assertEquals((Long)4L, param.getStepNumber());
        // TODO survey name and compat level
        assertEquals("Q", param.getStepStatus());
        assertEquals((Long)2L, param.getQuestionStepNumber());
        assertEquals(OneLong, param.getQuestionApiCompatLevel());
        assertEquals((Long)41L, param.getDisplayStatusJsonId());
        assertEquals((Long)42L, param.getQuestionJsonId());
        assertNotNull(param.getQuestionTime());
        assertNull(param.getProviderId());
        assertNull(param.getSectionId());
        assertEquals("name", param.getQuestionId());
        assertEquals("form", param.getQuestionType());
        assertNull(param.getAnswerApiCompatLevel());
        assertNull(param.getSubmitStatusJsonId());
        assertNull(param.getAnswerJsonId());
        assertNull(param.getAnswerTime());
        assertEquals((Long)678L, param.getUserAgentId());
        assertEquals("1.2.3.4", param.getClientIpAddress());
        assertEquals("device123", param.getDeviceToken());
      }
    }));
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    expect(system.getProgress("123")).andReturn(0.0);
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, progress);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    check(service.continueSurvey("{\"sessionToken\":\"session777\",\"surveySectionId\":\"s123\",\"surveyToken\":\"123\",\"questionId\":\"q123\",\"compatLevel\":\"1\",\"questionType\":\"form\",\"stepNumber\":\"3\",\"surveyProviderId\":\"p123\",\"pageTitle\":\"\",\"styleSheetName\":\"registry.css\"}",
        "{\"fieldAnswers\":[{\"choice\":[\"dentist\"],\"fieldId\":\"occupation\"}]}"), expectedDispStatus, expectedQuestion);

    verify(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, progress);
  }

  /**
   * User filled out token lookup form, but the lookup failed.
   */
  public void testContinueTokenLookupInvalid() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);

    expect(systemFactory.siteIdFor("testsystem")).andReturn(SITE_ID).anyTimes();
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system).anyTimes();
    expect(system.validateStartToken("123")).andThrow(new TokenInvalidException("oops"));
    expect(system.tokenLookupQuestion()).andReturn(null);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE).anyTimes();
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME).anyTimes();
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.tokenLookupInvalid, ":enterToken:default");
    expectedDispStatus.setSurveySystemName("testsystem");
    expectedDispStatus.setServerValidationMessage("oops");
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    check(service.continueSurvey("{\"questionId\":\":enterToken:default\",\"compatLevel\":\"1\",\"surveySystemName\":\"testsystem\",\"questionType\":\"form\",\"styleSheetName\":\"registry.css\"}",
        "{\"fieldAnswers\":[{\"choice\":[\"123\"],\"fieldId\":\"code\"}]}"), expectedDispStatus, tokenLookupForm());

    verify(systemFactory, system, surveyDao, keyGenerator, clientIds);
  }

  /**
   * User filled out token lookup form, and proceeds to first question.
   */
  public void testContinueTokenLookupValid() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);
    SurveyToken surveyToken = createMock(SurveyToken.class);

    expect(systemFactory.siteIdFor("testsystem")).andReturn(SITE_ID);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.validateStartToken("123")).andReturn("456");
    expect(surveyDao.findSurveyTokenAndLockIt(SITE_ID, "456")).andReturn(null);
    expect(system.startWithValidToken(eq("456"), anyObject(Survey.class))).andReturn(name());
    expect(keyGenerator.create()).andReturn("session777");
    expect(keyGenerator.create()).andReturn("resume888");
    surveyDao.createSurveyToken(check(new Check<SurveyToken>() {
      @Override
      public void check(SurveyToken param) {
        assertEquals(SITE_ID, param.getSurveySiteId());
        assertEquals("session777", param.getSessionToken());
        assertEquals("resume888", param.getResumeToken());
        assertEquals(OneLong, param.getLastStepNumber());
        assertEquals("456", param.getSurveyToken());

        param.setSurveyTokenId(555L);
        param.setLastActive(new Date());
      }
    }));
    expect(clientIds.userAgentId(SITE_ID)).andReturn(678L);
    expect(clientIds.getClientIpAddress()).andReturn("1.2.3.4");
    expect(clientIds.getDeviceToken()).andReturn("device123");
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.question, "name");
    expectedDispStatus.setSessionToken("session777");
    expectedDispStatus.setSurveyToken("456");
    expectedDispStatus.setStepNumber(OneLong);
    expectedDispStatus.setResumeToken("resume888");
    expectedDispStatus.setResumeTimeoutMillis(30000L);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    FormQuestion expectedQuestion = nameForm();
    expect(surveyDao.createJson(eq(SITE_ID), displayStatus(expectedDispStatus))).andReturn(2L);
    expect(surveyDao.createJson(eq(SITE_ID), formQuestion(expectedQuestion))).andReturn(OneLong);
    surveyDao.createProgress(check(new Check<SurveyProgress>() {
      @Override
      public void check(SurveyProgress param) {
        assertEquals(SITE_ID, param.getSurveySiteId());
        assertEquals((Long)555L, param.getSurveyTokenId());
        assertEquals(OneLong, param.getStepNumber());
        // TODO survey name and compat level
        assertEquals("Q", param.getStepStatus());
        assertEquals(OneLong, param.getQuestionStepNumber());
        assertEquals(OneLong, param.getQuestionApiCompatLevel());
        assertEquals((Long)2L, param.getDisplayStatusJsonId());
        assertEquals(OneLong, param.getQuestionJsonId());
        assertNotNull(param.getQuestionTime());
        assertNull(param.getProviderId());
        assertNull(param.getSectionId());
        assertEquals("name", param.getQuestionId());
        assertEquals("form", param.getQuestionType());
        assertNull(param.getAnswerApiCompatLevel());
        assertNull(param.getSubmitStatusJsonId());
        assertNull(param.getAnswerJsonId());
        assertNull(param.getAnswerTime());
        assertEquals((Long)678L, param.getUserAgentId());
        assertEquals("1.2.3.4", param.getClientIpAddress());
        assertEquals("device123", param.getDeviceToken());
      }
    }));
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    expect(system.getProgress("456")).andReturn(0.0);
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    check(service.continueSurvey("{\"questionId\":\":enterToken:default\",\"compatLevel\":\"1\",\"surveySystemName\":\"testsystem\",\"questionType\":\"form\",\"pageTitle\":\"\",\"styleSheetName\":\"registry.css\"}",
        "{\"fieldAnswers\":[{\"choice\":[\"123\"],\"fieldId\":\"code\"}]}"), expectedDispStatus, expectedQuestion);

    verify(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken);
  }

  /**
   * User filled out token lookup form, and proceeds to last seen question of an in-progress survey.
   */
  public void testContinueTokenLookupValidInProgress() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);
    SurveyToken surveyToken = createMock(SurveyToken.class);
    SurveyProgress surveyProgress = createMock(SurveyProgress.class);
    SurveyProgress surveyProgress2 = createMock(SurveyProgress.class);

    expect(systemFactory.siteIdFor("testsystem")).andReturn(SITE_ID);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.validateStartToken("123")).andReturn("456");
    expect(surveyDao.findSurveyTokenAndLockIt(SITE_ID, "456")).andReturn(surveyToken);
    expect(surveyToken.isComplete()).andReturn(false);
    expect(surveyToken.getSurveyTokenId()).andReturn(17L);
    expect(surveyToken.getLastStepNumber()).andReturn(3L);
    expect(surveyDao.findProgress(SITE_ID, 17L, 3L)).andReturn(surveyProgress);
    expect(surveyProgress.getQuestionStepNumber()).andReturn(2L);
    expect(surveyDao.findProgress(SITE_ID, 17L, 2L)).andReturn(surveyProgress2);
    expect(surveyProgress2.getQuestionApiCompatLevel()).andReturn(SurveyFactory.compatibilityLevel);
    surveyToken.setLastStepNumber(4L);
    expect(keyGenerator.create()).andReturn("session777");
    expect(keyGenerator.create()).andReturn("resume888");
    surveyDao.updateSurveyTokenRestartSession(surveyToken, "session777", "resume888");
    expect(surveyToken.getSessionToken()).andReturn("session777");
    expect(surveyToken.getResumeToken()).andReturn("resume888");
    expect(surveyProgress2.getQuestionType()).andReturn("form");
    expect(surveyProgress2.getQuestionId()).andReturn("q123");
    expect(surveyProgress2.getProviderId()).andReturn("p123");
    expect(surveyProgress2.getSectionId()).andReturn("s123");
    surveyProgress2.setStepNumber(4L);
    surveyProgress2.setStepStatus("Q");
    expect(surveyProgress2.getQuestionJsonId()).andReturn(21L);
    expect(surveyDao.findJson(21L)).andReturn("oldQuestion");
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.question, "q123");
    expectedDispStatus.setSessionToken("session777");
    expectedDispStatus.setSurveyToken("456");
    expectedDispStatus.setStepNumber(4L);
    expectedDispStatus.setSurveySectionId("s123");
    expectedDispStatus.setSurveyProviderId("p123");
    expectedDispStatus.setResumeToken("resume888");
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    expectedDispStatus.setResumeTimeoutMillis(30000L);
    expect(surveyDao.createJson(eq(SITE_ID), displayStatus(expectedDispStatus))).andReturn(OneLong);
    surveyProgress2.setDisplayStatusJsonId(OneLong);
    surveyProgress2.setQuestionTime(notNull(Date.class));
    expect(clientIds.userAgentId(SITE_ID)).andReturn(678L);
    expect(clientIds.getClientIpAddress()).andReturn("1.2.3.4");
    expect(clientIds.getDeviceToken()).andReturn("device123");
    surveyProgress2.setUserAgentId(678L);
    surveyProgress2.setClientIpAddress("1.2.3.4");
    surveyProgress2.setDeviceToken("device123");
    surveyDao.createProgress(surveyProgress2);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    expect(system.getProgress("456")).andReturn(0.0);
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, surveyProgress, surveyProgress2);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    check(service.continueSurvey("{\"questionId\":\":enterToken:default\",\"compatLevel\":\"1\",\"surveySystemName\":\"testsystem\",\"questionType\":\"form\"}",
        "{\"fieldAnswers\":[{\"choice\":[\"123\"],\"fieldId\":\"code\"}]}"),
        expectedDispStatus,
        "oldQuestion");

    verify(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, surveyProgress, surveyProgress2);
  }

  // TODO valid/invalid token lookup with custom page

  /**
   * Try to continue a survey without a session token.
   */
  public void testContinueSessionInvalid() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SubmitStatus status = createMock(SubmitStatus.class);
    SurveySystem system = createMock(SurveySystem.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);

    expect(systemFactory.siteIdFor("testsystem")).andReturn(SITE_ID);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    expect(surveyDao.findSurveyTokenBySessionAndLockIt("session777")).andReturn(null);
    replay(systemFactory, surveyDao, status, system, keyGenerator, clientIds);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.clearSession, ":expired");
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    check(service.continueSurvey("{\"sessionToken\":\"session777\",\"surveySectionId\":\"s123\",\"surveyToken\":\"123\",\"questionId\":\"q123\",\"compatLevel\":\"1\",\"surveySystemName\":\"testsystem\",\"questionType\":\"form\",\"stepNumber\":\"3\",\"surveyProviderId\":\"p123\"}",
        "{\"fieldAnswers\":[{\"choice\":[\"dentist\"],\"fieldId\":\"occupation\"}]}"), expectedDispStatus, sessionExpiredForm());

    verify(systemFactory, status, system, surveyDao, keyGenerator, clientIds);
  }

  /**
   * Try to resume a survey with a valid resume token.
   */
  public void testResumeSession() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);
    SurveyToken surveyToken = createMock(SurveyToken.class);
    SurveyProgress surveyProgress = createMock(SurveyProgress.class);
    SurveyProgress surveyProgress2 = createMock(SurveyProgress.class);

    expect(surveyDao.findSurveyTokenByResumeAndLockIt("resume123")).andReturn(surveyToken);
    expect(surveyToken.getLastActive()).andReturn(new Date(System.currentTimeMillis() + 100000));
    expect(surveyToken.getSurveySiteId()).andReturn(SITE_ID);
    expect(surveyToken.getSurveyToken()).andReturn("123");
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    system.revalidateToken("123");
    expect(surveyToken.isComplete()).andReturn(false);
    expect(surveyToken.getSurveyTokenId()).andReturn(17L);
    expect(surveyToken.getLastStepNumber()).andReturn(3L);
    expect(surveyDao.findProgress(SITE_ID, 17L, 3L)).andReturn(surveyProgress);
    expect(surveyProgress.getQuestionStepNumber()).andReturn(2L);
    expect(surveyDao.findProgress(SITE_ID, 17L, 2L)).andReturn(surveyProgress2);
    expect(surveyProgress2.getQuestionApiCompatLevel()).andReturn(SurveyFactory.compatibilityLevel);
    surveyToken.setLastStepNumber(4L);
    expect(keyGenerator.create()).andReturn("session777");
    expect(keyGenerator.create()).andReturn("resume888");
    surveyDao.updateSurveyTokenResumeSession(surveyToken, "session777", "resume888");
    expect(surveyToken.getSessionToken()).andReturn("session777");
    expect(surveyToken.getResumeToken()).andReturn("resume888");
    expect(surveyProgress2.getQuestionType()).andReturn("form");
    expect(surveyProgress2.getQuestionId()).andReturn("q123");
    expect(surveyProgress2.getProviderId()).andReturn("p123");
    expect(surveyProgress2.getSectionId()).andReturn("s123");
    surveyProgress2.setStepNumber(4L);
    surveyProgress2.setStepStatus("Q");
    expect(surveyProgress2.getQuestionJsonId()).andReturn(21L);
    expect(surveyDao.findJson(21L)).andReturn("oldQuestionJson");
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.question, "q123");
    expectedDispStatus.setSessionToken("session777");
    expectedDispStatus.setSurveyToken("123");
    expectedDispStatus.setStepNumber(4L);
    expectedDispStatus.setSurveySectionId("s123");
    expectedDispStatus.setSurveyProviderId("p123");
    expectedDispStatus.setResumeToken("resume888");
    expectedDispStatus.setResumeTimeoutMillis(30000L);
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    expect(surveyDao.createJson(eq(SITE_ID), displayStatus(expectedDispStatus))).andReturn(OneLong);
    surveyProgress2.setDisplayStatusJsonId(OneLong);
    surveyProgress2.setQuestionTime(notNull(Date.class));
    expect(clientIds.userAgentId(SITE_ID)).andReturn(678L);
    expect(clientIds.getClientIpAddress()).andReturn("1.2.3.4");
    expect(clientIds.getDeviceToken()).andReturn("device123");
    surveyProgress2.setUserAgentId(678L);
    surveyProgress2.setClientIpAddress("1.2.3.4");
    surveyProgress2.setDeviceToken("device123");
    surveyDao.createProgress(surveyProgress2);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    expect(system.getProgress("123")).andReturn(0.0);
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, surveyProgress, surveyProgress2);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    check(service.resumeSurvey("resume123"), expectedDispStatus, "oldQuestionJson");

    verify(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, surveyProgress, surveyProgress2);
  }

  /**
   * Try to resume a survey with a valid resume token, but the survey is already done.
   */
  public void testResumeSessionAlreadyCompleted() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);
    SurveyToken surveyToken = createMock(SurveyToken.class);
    SurveyProgress surveyProgress = createMock(SurveyProgress.class);
    SurveyProgress surveyProgress2 = createMock(SurveyProgress.class);

    expect(surveyDao.findSurveyTokenByResumeAndLockIt("resume123")).andReturn(surveyToken);
    expect(surveyToken.getLastActive()).andReturn(new Date(System.currentTimeMillis() + 100000));
    expect(surveyToken.isComplete()).andReturn(true);
    expect(surveyToken.getSurveySiteId()).andReturn(SITE_ID);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, surveyProgress, surveyProgress2);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.clearSession, ":done");
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    check(service.resumeSurvey("resume123"), expectedDispStatus, alreadyCompletedForm());

    verify(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken, surveyProgress, surveyProgress2);
  }

  /**
   * Try to resume a survey with a valid resume token.
   */
  public void testResumeSessionTimeout() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveySystem system = createMock(SurveySystem.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);
    SurveyToken surveyToken = createMock(SurveyToken.class);

    expect(surveyDao.findSurveyTokenByResumeAndLockIt("resume123")).andReturn(surveyToken);
    expect(surveyToken.getLastActive()).andReturn(new Date(System.currentTimeMillis() - 100000));
    expect(surveyToken.getSurveySiteId()).andReturn(SITE_ID);
    expect(systemFactory.systemForSiteId(SITE_ID)).andReturn(system);
    expect(system.getPageTitle()).andReturn(PAGE_TITLE);
    expect(system.getStyleSheetName()).andReturn(STYLESHEET_NAME);
    replay(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.clearSession, ":expired");
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    check(service.resumeSurvey("resume123"), expectedDispStatus, sessionExpiredForm());

    verify(systemFactory, system, surveyDao, keyGenerator, clientIds, surveyToken);
  }

  /**
   * Try to resume a survey without a valid resume token.
   */
  public void testResumeSessionInvalid() throws Exception {
    SurveySystemFactory systemFactory = createMock(SurveySystemFactory.class);
    SurveyDao surveyDao = createMock(SurveyDao.class);
    SessionKeyGenerator keyGenerator = createMock(SessionKeyGenerator.class);
    ClientIdentifiers clientIds = createMock(ClientIdentifiers.class);

    expect(surveyDao.findSurveyTokenByResumeAndLockIt("resume123")).andReturn(null);
    replay(systemFactory, surveyDao, keyGenerator, clientIds);

    SurveyServiceImpl service = new SurveyServiceImpl(systemFactory, surveyDao, keyGenerator, null, clientIds);
    DisplayStatus expectedDispStatus = formStatus(SessionStatus.clearSession, ":expired");
    expectedDispStatus.setPageTitle(PAGE_TITLE);
    expectedDispStatus.setStyleSheetName(STYLESHEET_NAME);
    check(service.resumeSurvey("resume123"), expectedDispStatus, sessionExpiredForm());

    verify(systemFactory, surveyDao, keyGenerator, clientIds);
  }

  private Question name() {
    Question q = new Question("name").withSurvey("testSurvey", 3L);
    q.form("", q.field("name", FieldType.text, "Patient name"));
    q.getDisplayStatus().setPageTitle(PAGE_TITLE);
    q.getDisplayStatus().setStyleSheetName(STYLESHEET_NAME);
    return q;
  }

  private FormQuestion nameForm() {
    Question q = new Question("name");
    q.getDisplayStatus().setPageTitle(PAGE_TITLE);
    q.getDisplayStatus().setStyleSheetName(STYLESHEET_NAME);
    return q.formQuestion("", q.field("name", FieldType.text, "Patient name")).as();
  }

  private FormQuestion tokenLookupForm() {
    Question q = new Question(":enterToken:default");
    return q.formQuestion("Ask the front desk to enter your code", q.field("code", FieldType.number, "PIN code")).as();
  }

  private FormQuestion thankYouForm() {
    Question q = new Question(":done");
    q.getDisplayStatus().setPageTitle(PAGE_TITLE);
    q.getDisplayStatus().setStyleSheetName(STYLESHEET_NAME);
    FormQuestion form = q.formQuestion("Thank you for completing this questionnaire.").as();
    form.setTerminal(true);
    return form;
  }

  private FormQuestion unableToStartForm() {
    Question q = new Question(":error");
    q.getDisplayStatus().setPageTitle(PAGE_TITLE);
    q.getDisplayStatus().setStyleSheetName(STYLESHEET_NAME);
    FormQuestion form = q.formQuestion("Unable to start survey",
        "Make sure you are looking at the most recent email and try clicking the link again").as();
    form.setTerminal(true);
    return form;
  }

  private FormQuestion alreadyCompletedForm() {
    Question q = new Question(":done");
    FormQuestion form = q.formQuestion("Survey already completed",
        "Make sure you are looking at the most recent email and try clicking the link again").as();
    form.setTerminal(true);
    return form;
  }

  private FormQuestion sessionExpiredForm() {
    Question q = new Question(":expired");
    FormQuestion form = q.formQuestion("Your survey session has expired",
        "Try reloading this page in your browser").as();
    form.setTerminal(true);
    return form;
  }

  private Question nameValidationFailed() {
    Question q = new Question("name", QuestionType.form);
    AutoBean<FormQuestion> formQuestion = q.formQuestion("", q.field("name", FieldType.text, "Patient name"));
    formQuestion.as().setServerValidationMessage("Wrong patient name");
    q.setQuestion(formQuestion);
    q.getDisplayStatus().setSessionStatus(SessionStatus.questionInvalid);
    q.getDisplayStatus().setPageTitle(PAGE_TITLE);
    q.getDisplayStatus().setStyleSheetName(STYLESHEET_NAME);
    return q;
  }

  private void check(String[] response, String displayStatusJson, String questionJson) {
    assertEquals(displayStatusJson, response[0]);
    assertEquals(questionJson, response[1]);
  }

  private void check(String[] response, DisplayStatus displayStatus, FormQuestion question) {
    checkEquals(displayStatus, displayStatus(response[0]));
    checkEquals(question, formQuestion(response[1]));
  }

  private void check(String[] response, DisplayStatus displayStatus, String questionJson) {
    checkEquals(displayStatus, displayStatus(response[0]));
    assertEquals(questionJson, response[1]);
  }

  private void checkEquals(DisplayStatus expected, DisplayStatus actual) {
    AutoBean<DisplayStatus> expectedBean = AutoBeanUtils.getAutoBean(expected);
    AutoBean<DisplayStatus> actualBean = AutoBeanUtils.getAutoBean(actual);
    assertTrue("AutoBeans don't match: " + AutoBeanUtils.diff(expectedBean, actualBean)
        + "DISP_EXPECTED=" + AutoBeanUtils.getAllProperties(expectedBean).toString()
        + " DISP_ACTUAL=" + AutoBeanUtils.getAllProperties(actualBean).toString(),
        AutoBeanUtils.deepEquals(expectedBean, actualBean));
    assertTrue(AutoBeanUtils.deepEquals(AutoBeanUtils.getAutoBean(expected), AutoBeanUtils.getAutoBean(actual)));
  }

  private void checkEquals(FormQuestion expected, FormQuestion actual) {
    AutoBean<FormQuestion> expectedBean = AutoBeanUtils.getAutoBean(expected);
    AutoBean<FormQuestion> actualBean = AutoBeanUtils.getAutoBean(actual);
    assertTrue("AutoBeans don't match: " + AutoBeanUtils.diff(expectedBean, actualBean)
        + "FORM_EXPECTED=" + AutoBeanUtils.getAllProperties(expectedBean).toString()
        + " FORM_ACTUAL=" + AutoBeanUtils.getAllProperties(actualBean).toString(),
        AutoBeanUtils.deepEquals(expectedBean, actualBean));
  }

  private DisplayStatus displayStatus(String json) {
    return AutoBeanCodex.decode(factory, DisplayStatus.class, json).as();
  }

  private FormQuestion formQuestion(String json) {
    return AutoBeanCodex.decode(factory, FormQuestion.class, json).as();
  }

  private String displayStatus(final DisplayStatus expected) {
    return check(new Check<String>() {
      @Override
      public void check(String param) {
        AutoBean<DisplayStatus> expectedBean = AutoBeanUtils.getAutoBean(expected);
        AutoBean<DisplayStatus> actualBean = AutoBeanUtils.getAutoBean(displayStatus(param));
        assertTrue("AutoBeans don't match: " + AutoBeanUtils.diff(expectedBean, actualBean)
            + "DISP_EXPECTED=" + AutoBeanUtils.getAllProperties(expectedBean).toString()
            + " DISP_ACTUAL=" + AutoBeanUtils.getAllProperties(actualBean).toString(),
            AutoBeanUtils.deepEquals(expectedBean, actualBean));
      }
    });
  }

  private String formQuestion(final FormQuestion expected) {
    return check(new Check<String>() {
      @Override
      public void check(String param) {
        AutoBean<FormQuestion> expectedBean = AutoBeanUtils.getAutoBean(expected);
        AutoBean<FormQuestion> actualBean = AutoBeanUtils.getAutoBean(formQuestion(param));
        assertTrue("AutoBeans don't match: " + AutoBeanUtils.diff(expectedBean, actualBean),
            AutoBeanUtils.deepEquals(expectedBean, actualBean));
      }
    });
  }

  private DisplayStatus formStatus(SessionStatus sessionStatus, String questionId) {
    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setCompatLevel(SurveyFactory.compatibilityLevel);
    displayStatus.setQuestionType(QuestionType.form);
    displayStatus.setSessionStatus(sessionStatus);
    displayStatus.setQuestionId(questionId);
    return displayStatus;
  }

  public interface Check<T> {
    void check(T param);
  }

  public static class Checker<T> implements IArgumentMatcher {
    private Check<T> check;

    public Checker(Check<T> check) {
      this.check = check;
    }

    @Override
    public boolean matches(Object actual) {
      @SuppressWarnings("unchecked")
      T actualT = (T) actual;

      check.check(actualT);
      return true;
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("check(");
      buffer.append(check.getClass().getName());
      buffer.append(")");
    }
  }

  public static <T> T check(Check<T> check) {
    reportMatcher(new Checker<>(check));
    return null;
  }
}
