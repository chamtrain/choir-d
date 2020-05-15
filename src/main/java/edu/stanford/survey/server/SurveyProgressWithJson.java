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
 * Bean representing the survey_progress table in the database, with additional columns
 * for the actual JSON rather than just the foreign keys.
 */
public class SurveyProgressWithJson extends SurveyProgress {
  private String questionJson;
  private String answerJson;

  public String getQuestionJson() {
    return questionJson;
  }

  public void setQuestionJson(String questionJson) {
    this.questionJson = questionJson;
  }

  public String getAnswerJson() {
    return answerJson;
  }

  public void setAnswerJson(String answerJson) {
    this.answerJson = answerJson;
  }
}
