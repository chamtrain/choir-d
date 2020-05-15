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
 * Bean representing the survey_token table in the database.
 */
public class SurveyToken {
  private Long surveySiteId;
  private Long surveyTokenId;
  private String surveyToken;
  private String sessionToken;
  private String resumeToken;
  private Date resumeTokenTime;
  private boolean isComplete;
  private Long lastStepNumber;
  private Long lastSessionNumber;

  public Long getLastStepNumber() {
    return lastStepNumber;
  }

  public void setLastStepNumber(Long lastStepNumber) {
    this.lastStepNumber = lastStepNumber;
  }

  public Long getLastSessionNumber() {
    return lastSessionNumber;
  }

  public void setLastSessionNumber(Long lastSessionNumber) {
    this.lastSessionNumber = lastSessionNumber;
  }

  public boolean isComplete() {
    return isComplete;
  }

  public void setComplete(boolean complete) {
    isComplete = complete;
  }

  public Date getLastActive() {
    return resumeTokenTime;
  }

  public void setLastActive(Date resumeTokenTime) {
    this.resumeTokenTime = resumeTokenTime;
  }

  public String getResumeToken() {
    return resumeToken;
  }

  public void setResumeToken(String resumeToken) {
    this.resumeToken = resumeToken;
  }

  public String getSessionToken() {
    return sessionToken;
  }

  public void setSessionToken(String sessionToken) {
    this.sessionToken = sessionToken;
  }

  public String getSurveyToken() {
    return surveyToken;
  }

  public void setSurveyToken(String surveyToken) {
    this.surveyToken = surveyToken;
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
}
