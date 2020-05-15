/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyProgressWithJson;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.susom.database.Database;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SurveyQuery.
 */
@RunWith(MockitoJUnitRunner.class)
public class SurveyQueryTest {
  @InjectMocks
  private SurveyQuery query;
  @Mock
  private SurveyDao surveyDao;
  @Mock
  private Database database;
  @Mock
  private SurveyProgressWithJson step1;
  @Mock
  private SurveyProgressWithJson step2;
  @Mock
  private SurveyProgressWithJson step3;

  @Before
  public void initializeMocks() {
    when(step1.getStepNumber()).thenReturn(1L);
    when(step2.getStepNumber()).thenReturn(2L);
    when(step3.getStepNumber()).thenReturn(3L);
  }

  @Test
  public void testSimpleRetrieve() {
    List<SurveyProgressWithJson> progress = new ArrayList<>();
    progress.add(step1);
    when(surveyDao.findProgressEagerReverseOrder(1L, 123L)).thenReturn(progress);
    when(step1.getStepNumber()).thenReturn(1L);
    when(step1.getAnswerJson()).thenReturn("{\"answer\":1}");
    when(step1.getQuestionJson()).thenReturn("{\"question\":1}");
    when(step1.getAnswerTime()).thenReturn(new Date(123456L));
    when(step1.getQuestionTime()).thenReturn(new Date(54321L));

    query = new SurveyQuery(database, surveyDao, 1L);
    Survey survey = query.surveyBySurveyTokenId(123L, "t123", true);

    assertEquals(Long.valueOf(123L), survey.getSurveyTokenId());
    assertEquals("t123", survey.getSurveyToken());
    assertTrue(survey.isComplete());
    assertEquals(new Date(54321L), survey.startTime());
    assertEquals(69135L, survey.serverTimeMillis());
    SurveyStep step = survey.firstStep();
    assertNotNull(step);
    assertEquals(Long.valueOf(1L), step.stepNumber());
    assertEquals("{\"answer\":1}", step.answer().getAnswerJson());
    assertEquals("{\"answer\":1}", step.answerJson());
    assertEquals("{\"question\":1}", step.questionJson());
    // TODO questionForm*() and other methods on step
    assertEquals(new Date(123456L), step.getAnswerTime());
    assertEquals(new Date(54321L), step.getQuestionTime());
  }

  @Test
  public void testRetrieveOrderAndFilter() {
    List<SurveyProgressWithJson> progress = new ArrayList<>();
    progress.add(step3);
    progress.add(step2);
    progress.add(step1);

    when(surveyDao.findProgressEagerReverseOrder(1L, 123L)).thenReturn(progress);
    when(step1.getProviderId()).thenReturn("p1");
    when(step1.getSectionId()).thenReturn("s1");
    when(step1.getQuestionId()).thenReturn("q1");
    when(step2.getProviderId()).thenReturn("p1");
    when(step2.getSectionId()).thenReturn("s2");
    when(step2.getQuestionId()).thenReturn("q2");
    when(step3.getProviderId()).thenReturn("p1");
    when(step3.getSectionId()).thenReturn("s1");
    when(step3.getQuestionId()).thenReturn("q1"); // 2nd answer to q1, overrides first

    SurveyQuery query = new SurveyQuery(database, surveyDao, 1L);
    Survey survey = query.surveyBySurveyTokenId(123L, "t123", true);

    assertEquals(Long.valueOf(3L), survey.answeredStepByQuestion("q1").stepNumber());
    assertEquals(Long.valueOf(2L), survey.answeredStepByQuestion("q2").stepNumber());
    assertEquals(Long.valueOf(3L), survey.answeredStepByProviderSectionQuestion("p1", "s1", "q1").stepNumber());
    assertEquals(Long.valueOf(2L), survey.answeredStepByProviderSectionQuestion("p1", "s2", "q2").stepNumber());
    List<SurveyStep> steps = survey.answeredStepsByProvider("xyz");
    assertEquals(0, steps.size());
    steps = survey.answeredStepsByProvider("p1");
    assertEquals(2, steps.size());
    assertEquals(Long.valueOf(2L), steps.get(0).stepNumber());
    assertEquals(Long.valueOf(3L), steps.get(1).stepNumber());
    steps = survey.answeredStepsByProviderSection("p1", "s1");
    assertEquals(1, steps.size());
    assertEquals(Long.valueOf(3L), steps.get(0).stepNumber());
  }
}
