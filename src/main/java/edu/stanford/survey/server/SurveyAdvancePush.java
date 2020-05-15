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

package edu.stanford.survey.server;

import java.util.Date;

/**
 * Bean representing the survey_advance_push table in the database.
 */
public class SurveyAdvancePush {
  private Long surveyRecipientId;
  private Long surveySiteId;
  private String recipientName;
  private String recipientDisplayName;
  private Long pushedSurveySequence;
  private Date lastPushedTime;
  private Long failedSurveySequence;
  private Long failedCount;
  private Date lastFailedTime;
  private boolean isEnabled;

  public Long getSurveyRecipientId() {
    return surveyRecipientId;
  }

  public void setSurveyRecipientId(Long surveyRecipientId) {
    this.surveyRecipientId = surveyRecipientId;
  }

  public Long getSurveySiteId() {
    return surveySiteId;
  }

  public void setSurveySiteId(Long surveySiteId) {
    this.surveySiteId = surveySiteId;
  }

  public String getRecipientName() {
    return recipientName;
  }

  public void setRecipientName(String recipientName) {
    this.recipientName = recipientName;
  }

  public String getRecipientDisplayName() {
    return recipientDisplayName;
  }

  public void setRecipientDisplayName(String recipientDisplayName) {
    this.recipientDisplayName = recipientDisplayName;
  }

  public Long getPushedSurveySequence() {
    return pushedSurveySequence;
  }

  public void setPushedSurveySequence(Long pushedSurveySequence) {
    this.pushedSurveySequence = pushedSurveySequence;
  }

  public Date getLastPushedTime() {
    return lastPushedTime;
  }

  public void setLastPushedTime(Date lastPushedTime) {
    this.lastPushedTime = lastPushedTime;
  }

  public Long getFailedSurveySequence() {
    return failedSurveySequence;
  }

  public void setFailedSurveySequence(Long failedSurveySequence) {
    this.failedSurveySequence = failedSurveySequence;
  }

  public Long getFailedCount() {
    return failedCount;
  }

  public void setFailedCount(Long failedCount) {
    this.failedCount = failedCount;
  }

  public Date getLastFailedTime() {
    return lastFailedTime;
  }

  public void setLastFailedTime(Date lastFailedTime) {
    this.lastFailedTime = lastFailedTime;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }
}
