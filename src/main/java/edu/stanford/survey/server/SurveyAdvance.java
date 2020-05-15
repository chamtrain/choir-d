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

/**
 * Bean representing the survey_complete table in the database.
 */
public class SurveyAdvance {
  private Long advanceSequence;
  private Long surveySiteId;
  private Long surveyTokenId;

  public Long getAdvanceSequence() {
    return advanceSequence;
  }

  public void setAdvanceSequence(Long advanceSequence) {
    this.advanceSequence = advanceSequence;
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
