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
 * Additional status and other information passed to the client along with the question.
 * This is passed and parsed separately, so it should not include question- or
 * answer-specific Java types.
 */
public interface DisplayStatus {
  long getCompatLevel();

  void setCompatLevel(long level);

  long getStepNumber();

  void setStepNumber(long stepNumber);

  double getProgress();

  void setProgress(double progress);

  String getSurveyToken();

  void setSurveyToken(String surveyToken);

  QuestionType getQuestionType();

  void setQuestionType(QuestionType type);

  String getQuestionId();

  void setQuestionId(String questionId);

  String getSurveyProviderId();

  void setSurveyProviderId(String surveyProviderId);

  String getSurveySectionId();

  void setSurveySectionId(String surveySectionId);

  String getServerValidationMessage();

  void setServerValidationMessage(String message);

  String getSessionToken();

  void setSessionToken(String sessionToken);

  // TODO session timeout, custom heading, and progress information
  SessionStatus getSessionStatus();

  void setSessionStatus(SessionStatus status);

  String getResumeToken();

  void setResumeToken(String resumeToken);

  Long getResumeTimeoutMillis();

  void setResumeTimeoutMillis(Long timeout);

  String getSurveySystemName();

  void setSurveySystemName(String systemName);

  String getStyleSheetName();

  void setStyleSheetName(String styleSheetName);

  String getPageTitle();

  void setPageTitle(String pageTitle);
}
