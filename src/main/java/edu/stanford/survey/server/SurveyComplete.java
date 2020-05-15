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

/**
 * Bean representing the survey_complete table in the database.
 */
public class SurveyComplete {
  private Long completeSequence;
  private Long surveySiteId;
  private Long surveyTokenId;

  public Long getCompleteSequence() {
    return completeSequence;
  }

  public void setCompleteSequence(Long completeSequence) {
    this.completeSequence = completeSequence;
  }

  public Long getSurveySiteId() {
    return surveySiteId;
  }

  public void setSurveySiteId(Long surveySiteId) {
    this.surveySiteId = surveySiteId;
  }

  public Long getSurveyTokenId() {
    return surveyTokenId;
  }

  public void setSurveyTokenId(Long surveyTokenId) {
    this.surveyTokenId = surveyTokenId;
  }
}
