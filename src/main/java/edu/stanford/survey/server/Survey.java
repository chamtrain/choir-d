/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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

import java.util.Date;
import java.util.List;

/**
 * Interface for accessing data collected during a survey.
 */
public interface Survey {
  /**
   * Access a previous survey for this same patient (if it exists). The survey must have
   * at least one successfully answered question with the indicated provider (an answer
   * was provided and accepted as valid). If multiple surveys exist, only the most recent
   * will be returned.
   *
   * @param providerId the providerId (see survey_progress.provider_id) for the questions;
   *                   may be null, in which case a null will be returned
   * @return the survey, or null if no match was found
   */
//  Survey priorSurveyByProvider(String providerId);

//  List<Survey> priorSurveys(Date includeAfter);

  /**
   * Whether this survey has been marked as completed (the flag on the survey_token table).
   */
  boolean isComplete();

  Long getSurveyTokenId();

  String getSurveyToken();

  SurveyStep firstStep();

  Date startTime();

  SurveyStep lastStep();

  Date endTime();

  /**
   * Convenience method. Same as {@link #serverTimeMillis(int)} with a value of 300000 (5 minutes).
   */
  long serverTimeMillis();

  /**
   * Approximate aggregate time spent between displaying each question and submitting
   * each answer. This is calculated by subtracting the server-recorded time of the
   * question from the server-recorded time of the answer. If for a given step there
   * was no answer, no time is accrued.
   *
   * @param maxMillisPerStep if the time at a given step exceeds this amount, this amount
   *                         of time will be counted in the aggregate rather than the actual time
   */
  long serverTimeMillis(int maxMillisPerStep);

  /**
   * Convenience method. Same as {@link #clientTimeMillis(int)} with a value of 300000 (5 minutes).
   */
  long clientTimeMillis();

  /**
   * Approximate aggregate time spent by the user on this survey. This is calculated by
   * adding up three timings that are tracked by the client: 1) call time, the time the
   * client waited between sending request to server and receiving a response, 2) render
   * time, the time it took to process the server response and put it on the screen, and
   * 3) think time, the time between rendering and the user tapping continue.
   *
   * <p>This timing is probably more accurate in general than {@link #serverTimeMillis()},
   * but involves the client tracking the timings, so it is subject to manipulation.</p>
   *
   * @param maxMillisThinkTime if the think time at a given step exceeds this amount, this amount
   *                         of time will be counted in the aggregate rather than the actual time
   */
  long clientTimeMillis(int maxMillisThinkTime);

  /**
   * Access the most recent step within this survey that has the provided question id
   * and has been successfully answered (an answer was provided and accepted as valid).
   *
   * @param questionId the questionId (see survey_progress.question_id); may be null,
   *                   in which case a null will be returned
   * @return the step, or null if none could be found
   */
  SurveyStep answeredStepByQuestion(String questionId);

  SurveyStep answeredStepByProviderSectionQuestion(String providerId, String sectionId, String questionId);

  /**
   * Access all steps within this survey that have the indicated provider and have been
   * successfully answered (an answer was provided and accepted as valid). The list will
   * be in order, with the earliest step (smallest step number) first.
   *
   * @param providerId the providerId (see survey_progress.provider_id) for the questions;
   *                   may be null, in which case an empty list will be returned
   * @return the set of steps, or an empty list (never returns null)
   */
  List<SurveyStep> answeredStepsByProvider(String providerId);

  List<SurveyStep> answeredStepsByProviderSection(String providerId, String sectionId);
}
