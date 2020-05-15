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

package edu.stanford.survey.client.api;

/**
 * Status and other information submitted along with an answer to a survey page.
 * This is passed and parsed separately, so it should not include question- or
 * answer-specific Java types.
 */
public interface SubmitStatus {
  long getCompatLevel();

  void setCompatLevel(long level);

  long getStepNumber();

  void setStepNumber(long stepNumber);

  QuestionType getQuestionType();

  void setQuestionType(QuestionType type);

  String getQuestionId();

  void setQuestionId(String questionid);

  // TODO keep provider and section server-side only
  String getSurveyProviderId();

  void setSurveyProviderId(String surveyProviderId);

  String getSurveySectionId();

  void setSurveySectionId(String surveySectionId);

  String getSessionToken();

  void setSessionToken(String sessionToken);

  String getSurveySystemName();

  void setSurveySystemName(String systemName);

  Long getCallTimeMillis();

  void setCallTimeMillis(Long callTimeMillis);

  Long getRenderTimeMillis();

  void setRenderTimeMillis(Long renderTimeMillis);

  Long getThinkTimeMillis();

  void setThinkTimeMillis(Long thinkTimeMillis);

  Long getRetryCount();

  void setRetryCount(Long retries);
}
