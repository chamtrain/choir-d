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

import java.util.Date;

/**
 * Bean representing the survey_progress table in the database.
 */
public class SurveyProgress {
  private Long surveySiteId;
  private Long surveyTokenId;
  private Long stepNumber;
  private String stepStatus;
  private Long questionStepNumber;
  private Long questionApiCompatLevel;
  private Long displayStatusJsonId;
  private Long questionJsonId;
  private Date questionTime;
  private Long answerApiCompatLevel;
  private Long submitStatusJsonId;
  private Long answerJsonId;
  private Date answerTime;
  private String providerId;
  private String sectionId;
  private String questionId;
  private String questionType;
  private String surveyName;
  private Long surveyCompatLevel;
  private Long callTimeMillis;
  private Long renderTimeMillis;
  private Long thinkTimeMillis;
  private Long retryCount;
  private Long userAgentId;
  private String clientIpAddress;
  private String deviceToken;

  public Date getAnswerTime() {
    return answerTime;
  }

  public void setAnswerTime(Date answerTime) {
    this.answerTime = answerTime;
  }

  public Date getQuestionTime() {
    return questionTime;
  }

  public void setQuestionTime(Date questionTime) {
    this.questionTime = questionTime;
  }

  public Long getAnswerJsonId() {
    return answerJsonId;
  }

  public void setAnswerJsonId(Long answerJsonId) {
    this.answerJsonId = answerJsonId;
  }

  public Long getSubmitStatusJsonId() {
    return submitStatusJsonId;
  }

  public void setSubmitStatusJsonId(Long submitStatusJsonId) {
    this.submitStatusJsonId = submitStatusJsonId;
  }

  public Long getQuestionJsonId() {
    return questionJsonId;
  }

  public void setQuestionJsonId(Long questionJsonId) {
    this.questionJsonId = questionJsonId;
  }

  public Long getDisplayStatusJsonId() {
    return displayStatusJsonId;
  }

  public void setDisplayStatusJsonId(Long displayStatusJsonId) {
    this.displayStatusJsonId = displayStatusJsonId;
  }

  public Long getUserAgentId() {
    return userAgentId;
  }

  public void setUserAgentId(Long userAgentId) {
    this.userAgentId = userAgentId;
  }

  public String getQuestionType() {
    return questionType;
  }

  public void setQuestionType(String questionType) {
    this.questionType = questionType;
  }

  public String getQuestionId() {
    return questionId;
  }

  public void setQuestionId(String questionId) {
    this.questionId = questionId;
  }

  public String getSectionId() {
    return sectionId;
  }

  public void setSectionId(String sectionId) {
    this.sectionId = sectionId;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public Long getStepNumber() {
    return stepNumber;
  }

  public void setStepNumber(Long stepNumber) {
    this.stepNumber = stepNumber;
  }

  public Long getSurveyTokenId() {
    return surveyTokenId;
  }

  public void setSurveyTokenId(Long surveyTokenId) {
    this.surveyTokenId = surveyTokenId;
  }

  public Long getSurveySiteId() {
    return surveySiteId;
  }

  public void setSurveySiteId(Long surveySiteId) {
    this.surveySiteId = surveySiteId;
  }

  public Long getQuestionStepNumber() {
    return questionStepNumber;
  }

  public void setQuestionStepNumber(Long questionStepNumber) {
    this.questionStepNumber = questionStepNumber;
  }

  public Long getQuestionApiCompatLevel() {
    return questionApiCompatLevel;
  }

  public void setQuestionApiCompatLevel(Long questionApiCompatLevel) {
    this.questionApiCompatLevel = questionApiCompatLevel;
  }

  public Long getAnswerApiCompatLevel() {
    return answerApiCompatLevel;
  }

  public void setAnswerApiCompatLevel(Long answerApiCompatLevel) {
    this.answerApiCompatLevel = answerApiCompatLevel;
  }

  public void setStepStatus(String stepStatus) {
    this.stepStatus = stepStatus;
  }

  public String getStepStatus() {
    return stepStatus;
  }

  public String getDeviceToken() {
    return deviceToken;
  }

  public void setDeviceToken(String deviceToken) {
    this.deviceToken = deviceToken;
  }

  public String getClientIpAddress() {
    return clientIpAddress;
  }

  public void setClientIpAddress(String clientIpAddress) {
    this.clientIpAddress = clientIpAddress;
  }

  public Long getThinkTimeMillis() {
    return thinkTimeMillis;
  }

  public void setThinkTimeMillis(Long thinkTimeMillis) {
    this.thinkTimeMillis = thinkTimeMillis;
  }

  public Long getRenderTimeMillis() {
    return renderTimeMillis;
  }

  public void setRenderTimeMillis(Long renderTimeMillis) {
    this.renderTimeMillis = renderTimeMillis;
  }

  public Long getCallTimeMillis() {
    return callTimeMillis;
  }

  public void setCallTimeMillis(Long callTimeMillis) {
    this.callTimeMillis = callTimeMillis;
  }

  public Long getSurveyCompatLevel() {
    return surveyCompatLevel;
  }

  public void setSurveyCompatLevel(Long surveyCompatLevel) {
    this.surveyCompatLevel = surveyCompatLevel;
  }

  public Long getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(Long retryCount) {
    this.retryCount = retryCount;
  }

  public String getSurveyName() {
    return surveyName;
  }

  public void setSurveyName(String surveyName) {
    this.surveyName = surveyName;
  }
}
