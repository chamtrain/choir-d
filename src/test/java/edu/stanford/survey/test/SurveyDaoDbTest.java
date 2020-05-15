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
import edu.stanford.survey.server.SurveyComplete;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyProgress;
import edu.stanford.survey.server.SurveyToken;

import java.util.Date;
import java.util.List;

/**
 * Tests for SurveyDao.
 */
public class SurveyDaoDbTest extends DatabaseTestCase {
  Long siteId = 2L;

  /**
   * Create, read, and update tests for survey_token table.
   */
  public void testSurveyToken() throws Exception {
    SurveyDao dao = new SurveyDao(getDatabaseProvider());

    SurveyToken st = new SurveyToken();
    st.setSurveySiteId(2L);
    st.setSessionToken("session123");
    st.setResumeToken("resume456");
    st.setLastStepNumber(1L);
    st.setSurveyToken("123");

    dao.createSurveyToken(st);

    Long surveyTokenId = st.getSurveyTokenId();
    Date lastActive = st.getLastActive();

    assertEquals((Long)2L, st.getSurveySiteId());
    assertNotNull(surveyTokenId);
    assertEquals("123", st.getSurveyToken());
    assertEquals("session123", st.getSessionToken());
    assertEquals("resume456", st.getResumeToken());
    assertNotNull(lastActive);
    assertEquals((Long)1L, st.getLastStepNumber());
    assertFalse(st.isComplete());

    st = dao.findSurveyTokenAndLockIt(2L, "123");

    assertEquals((Long)2L, st.getSurveySiteId());
    assertNotNull(surveyTokenId);
    assertEquals("123", st.getSurveyToken());
    assertEquals("session123", st.getSessionToken());
    assertEquals("resume456", st.getResumeToken());
    assertNotNull(lastActive);
    assertEquals((Long)1L, st.getLastStepNumber());
    assertFalse(st.isComplete());

    st = dao.findSurveyTokenBySessionAndLockIt("session123");

    assertEquals((Long)2L, st.getSurveySiteId());
    assertNotNull(surveyTokenId);
    assertEquals("123", st.getSurveyToken());
    assertEquals("session123", st.getSessionToken());
    assertEquals("resume456", st.getResumeToken());
    assertNotNull(lastActive);
    assertEquals((Long)1L, st.getLastStepNumber());
    assertFalse(st.isComplete());

    st = dao.findSurveyTokenByResumeAndLockIt("resume456");

    assertEquals((Long)2L, st.getSurveySiteId());
    assertNotNull(surveyTokenId);
    assertEquals("123", st.getSurveyToken());
    assertEquals("session123", st.getSessionToken());
    assertEquals("resume456", st.getResumeToken());
    assertNotNull(lastActive);
    assertEquals((Long)1L, st.getLastStepNumber());
    assertFalse(st.isComplete());

    st.setComplete(true);
    st.setLastStepNumber(2L);
    Thread.sleep(1000); // make sure lastActive will be bigger
    dao.updateSurveyTokenInvalidateSession(st);

    st = dao.findSurveyTokenByResumeAndLockIt("resume456");

    assertEquals((Long)2L, st.getSurveySiteId());
    assertNotNull(surveyTokenId);
    assertEquals("123", st.getSurveyToken());
    assertEquals("session123", st.getSessionToken());
    assertEquals("resume456", st.getResumeToken());
    assertTrue(lastActive.getTime() < st.getLastActive().getTime());
    assertEquals((Long)2L, st.getLastStepNumber());
    assertTrue(st.isComplete());

    List<SurveyComplete> completes = dao.findSurveyComplete(2L, 0L);
    assertEquals(1, completes.size());
    assertEquals((Long)2L, completes.get(0).getSurveySiteId());
    assertEquals(st.getSurveyTokenId(), completes.get(0).getSurveyTokenId());
  }

  public void testSurveySession() throws Exception {
    SurveyDao dao = new SurveyDao(getDatabaseProvider());

    SurveyToken st = new SurveyToken();
    st.setSurveySiteId(siteId);
    st.setSessionToken("session123");
    st.setResumeToken("resume456");
    st.setLastStepNumber(1L);
    st.setSurveyToken("123");

    dao.createSurveyToken(st);

    Date lastActive = st.getLastActive();

    assertEquals((Long)1L, st.getLastSessionNumber());
    assertEquals("session123", st.getSessionToken());
    assertEquals("resume456", st.getResumeToken());
    assertNotNull(lastActive);

    Thread.sleep(1000); // make sure lastActive will be bigger
    dao.updateSurveyTokenTouchSession(st);

    assertEquals((Long)1L, st.getLastSessionNumber());
    assertEquals("session123", st.getSessionToken());
    assertEquals("resume456", st.getResumeToken());
    assertTrue(lastActive.before(st.getLastActive()));

    lastActive = st.getLastActive();
    Thread.sleep(1000); // make sure lastActive will be bigger
    dao.updateSurveyTokenRestartSession(st, "session321", "resume654");

    assertEquals((Long)2L, st.getLastSessionNumber());
    assertEquals("session321", st.getSessionToken());
    assertEquals("resume654", st.getResumeToken());
    assertTrue(lastActive.before(st.getLastActive()));

    lastActive = st.getLastActive();
    Thread.sleep(1000); // make sure lastActive will be bigger
    dao.updateSurveyTokenResumeSession(st, "session333", "resume666");

    assertEquals((Long)3L, st.getLastSessionNumber());
    assertEquals("session333", st.getSessionToken());
    assertEquals("resume666", st.getResumeToken());
    assertTrue(lastActive.before(st.getLastActive()));

    lastActive = st.getLastActive();
    Thread.sleep(1000); // make sure lastActive will be bigger
    dao.updateSurveyTokenTouchSession(st);

    assertEquals((Long)3L, st.getLastSessionNumber());
    assertEquals("session333", st.getSessionToken());
    assertEquals("resume666", st.getResumeToken());
    assertTrue(lastActive.before(st.getLastActive()));
  }

  public void testDifferentUserAgentsDiffer() throws Exception {
    SurveyDao dao = new SurveyDao(getDatabaseProvider());

    Long id = dao.findOrCreateUserAgent(siteId, "my user agent string");

    assertNotNull(id);

    Long id2 = dao.findOrCreateUserAgent(siteId, "another user agent string");

    assertNotNull(id2);
    assertFalse(id.equals(id2));
  }

  public void testSamesUserAgentsSame() throws Exception {
    SurveyDao dao = new SurveyDao(getDatabaseProvider());

    Long id = dao.findOrCreateUserAgent(siteId, "my user agent string");
    assertNotNull(id);

    Long id2 = dao.findOrCreateUserAgent(siteId, "my user agent string");
    assertNotNull(id2);
    assertEquals(id, id2);
  }

  public void testSameUserAgentDifferentSitesDiffer() throws Exception {
    SurveyDao dao = new SurveyDao(getDatabaseProvider());

    Long id = dao.findOrCreateUserAgent(siteId, "my user agent string");
    assertNotNull(id);

    Long site2 = siteId.longValue() + 1;
    Long id2 = dao.findOrCreateUserAgent(site2, "my user agent string");

    assertNotNull(id2);
    assertFalse(id.equals(id2));
  }

  /**
   * Create, read, and update tests for survey_progress table.
   */
  public void testSurveyProgress() throws Exception {
    SurveyDao dao = new SurveyDao(getDatabaseProvider());

    SurveyToken st = new SurveyToken();
    st.setSurveySiteId(siteId);
    st.setSessionToken("session123");
    st.setResumeToken("resume456");
    st.setLastStepNumber(5L);
    st.setSurveyToken("123");

    dao.createSurveyToken(st);

    SurveyProgress progress = new SurveyProgress();
    progress.setSurveySiteId(siteId);
    progress.setSurveyTokenId(st.getSurveyTokenId());
    progress.setStepNumber(5L);
    progress.setStepStatus("Q");
    progress.setQuestionStepNumber(3L);
    progress.setQuestionApiCompatLevel(1L);
    progress.setDisplayStatusJsonId(dao.createJson(siteId, "displayStatusJson"));
    progress.setQuestionJsonId(dao.createJson(siteId, "questionJson"));
    Date questionTime = new Date();
    progress.setQuestionTime(questionTime);
    progress.setProviderId("providerId");
    progress.setSectionId("sectionId");
    progress.setQuestionId("questionId");
    progress.setQuestionType("form");
    progress.setSurveyName("survey1");
    progress.setSurveyCompatLevel(1L);
    Long userAgentId = dao.findOrCreateUserAgent(siteId, "userAgent");
    progress.setUserAgentId(userAgentId);
    progress.setClientIpAddress("1.2.3.4");
    progress.setDeviceToken("deviceToken123");

    dao.createProgress(progress);

    progress = dao.findProgress(siteId, st.getSurveyTokenId(), 5L);

    assertEquals(siteId, progress.getSurveySiteId());
    assertEquals(st.getSurveyTokenId(), progress.getSurveyTokenId());
    assertEquals((Long)5L, progress.getStepNumber());
    assertEquals("Q", progress.getStepStatus());
    assertEquals((Long)3L, progress.getQuestionStepNumber());
    assertEquals((Long)1L, progress.getQuestionApiCompatLevel());
    assertEquals("displayStatusJson", dao.findJson(progress.getDisplayStatusJsonId()));
    assertEquals("questionJson", dao.findJson(progress.getQuestionJsonId()));
    assertEquals(questionTime.getTime(), progress.getQuestionTime().getTime());
    assertEquals("providerId", progress.getProviderId());
    assertEquals("sectionId", progress.getSectionId());
    assertEquals("questionId", progress.getQuestionId());
    assertEquals("form", progress.getQuestionType());
    assertNull(progress.getAnswerApiCompatLevel());
    assertNull(progress.getSubmitStatusJsonId());
    assertNull(progress.getAnswerJsonId());
    assertNull(progress.getAnswerTime());
    assertEquals("survey1", progress.getSurveyName());
    assertEquals((Long)1L, progress.getSurveyCompatLevel());
    assertEquals(userAgentId, progress.getUserAgentId());
    assertEquals("1.2.3.4", progress.getClientIpAddress());
    assertEquals("deviceToken123", progress.getDeviceToken());
    assertNull(progress.getCallTimeMillis());
    assertNull(progress.getRenderTimeMillis());
    assertNull(progress.getThinkTimeMillis());
    assertNull(progress.getRetryCount());

    progress.setAnswerApiCompatLevel(1L);
    progress.setSubmitStatusJsonId(dao.createJson(siteId, "submitStatusJson"));
    progress.setAnswerJsonId(dao.createJson(siteId, "answerJson"));
    Date answerTime = new Date();
    progress.setAnswerTime(answerTime);
    progress.setCallTimeMillis(100L);
    progress.setRenderTimeMillis(200L);
    progress.setThinkTimeMillis(300L);
    progress.setRetryCount(1L);

    dao.updateProgressAnswer(progress);

    progress = dao.findProgress(siteId, st.getSurveyTokenId(), 5L);

    assertEquals(siteId, progress.getSurveySiteId());
    assertEquals(st.getSurveyTokenId(), progress.getSurveyTokenId());
    assertEquals((Long)5L, progress.getStepNumber());
    assertEquals("Q", progress.getStepStatus());
    assertEquals((Long)3L, progress.getQuestionStepNumber());
    assertEquals((Long)1L, progress.getQuestionApiCompatLevel());
    assertEquals("displayStatusJson", dao.findJson(progress.getDisplayStatusJsonId()));
    assertEquals("questionJson", dao.findJson(progress.getQuestionJsonId()));
    assertEquals(questionTime.getTime(), progress.getQuestionTime().getTime());
    assertEquals("providerId", progress.getProviderId());
    assertEquals("sectionId", progress.getSectionId());
    assertEquals("questionId", progress.getQuestionId());
    assertEquals("form", progress.getQuestionType());
    assertEquals((Long)1L, progress.getAnswerApiCompatLevel());
    assertEquals("submitStatusJson", dao.findJson(progress.getSubmitStatusJsonId()));
    assertEquals("answerJson", dao.findJson(progress.getAnswerJsonId()));
    assertEquals(answerTime.getTime(), progress.getAnswerTime().getTime());
    assertEquals((Long)1L, progress.getSurveyCompatLevel());
    assertEquals(userAgentId, progress.getUserAgentId());
    assertEquals("1.2.3.4", progress.getClientIpAddress());
    assertEquals("deviceToken123", progress.getDeviceToken());
    assertEquals((Long)100L, progress.getCallTimeMillis());
    assertEquals((Long)200L, progress.getRenderTimeMillis());
    assertEquals((Long)300L, progress.getThinkTimeMillis());
    assertEquals((Long)1L, progress.getRetryCount());
  }

  /**
   * Create, read, and update tests for survey_progress_dup table.
   */
  public void testSurveyProgressDup() throws Exception {
    SurveyDao dao = new SurveyDao(getDatabaseProvider());

    SurveyToken st = new SurveyToken();
    st.setSurveySiteId(siteId);
    st.setSessionToken("session123");
    st.setResumeToken("resume456");
    st.setLastStepNumber(5L);
    st.setSurveyToken("123");

    dao.createSurveyToken(st);

    SurveyProgress progress = new SurveyProgress();
    progress.setSurveySiteId(siteId);
    progress.setSurveyTokenId(st.getSurveyTokenId());
    progress.setStepNumber(5L);
    progress.setStepStatus("Q");
    progress.setQuestionStepNumber(3L);
    progress.setQuestionApiCompatLevel(1L);
    progress.setDisplayStatusJsonId(dao.createJson(siteId, "displayStatusJson"));
    progress.setQuestionJsonId(dao.createJson(siteId, "questionJson"));
    progress.setProviderId("providerId");
    progress.setSectionId("sectionId");
    progress.setQuestionId("questionId");
    progress.setQuestionType("form");
    progress.setAnswerApiCompatLevel(1L);
    progress.setSubmitStatusJsonId(dao.createJson(siteId, "submitStatusJson"));
    progress.setAnswerJsonId(dao.createJson(siteId, "answerJson"));
    progress.setAnswerTime(new Date());
    progress.setCallTimeMillis(100L);
    progress.setRenderTimeMillis(200L);
    progress.setThinkTimeMillis(300L);
    progress.setRetryCount(1L);

    dao.createProgressDup(progress);

    // TODO verify it was stored properly
  }

  /**
   * Create, read, and update tests for survey_progress table.
   */
  public void testSurveyJson() throws Exception {
    SurveyDao dao = new SurveyDao(getDatabaseProvider());

    assertEquals("shortJson", dao.findJson(dao.createJson(siteId, "shortJson")));

    // Make sure it can store long strings as well as short ones
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 5000; i++) {
      builder.append("longstring");
    }
    String longString = builder.toString();

    assertEquals(longString, dao.findJson(dao.createJson(siteId, longString)));
  }
}
