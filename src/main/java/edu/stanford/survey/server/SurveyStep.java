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

import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.client.api.FormQuestion;

import java.util.Date;

/**
 * Interface for accessing data collected during a survey.
 */
public interface SurveyStep {
  /**
   * Each client/server interaction in a survey is assigned a sequential step number 1..n.
   */
  Long stepNumber();

  /**
   * Raw JSON representation of the answer as sent to the server by the client.
   */
  String answerJson();

  /**
   * Java representation of the {@link #answerJson()}, if possible.
   */
  Answer answer();

  /**
   * Read the selected regions from a {@link edu.stanford.survey.client.api.BodyMapAnswer}.
   */
  String answerRegionsCsv();

  /**
   * Read the value from a {@link edu.stanford.survey.client.api.NumericAnswer}.
   */
  Integer answerNumeric();

  /**
   * Raw JSON representation of the question as sent from the server to the client.
   */
  String questionJson();

  /**
   * Java representation of {@link #questionJson()}, assuming it is a FormQuestion.
   */
  FormQuestion questionForm();

  /**
   * Search within the FormQuestion recursively for a specific field.
   *
   * @param fieldId the id (not label) of the field you want
   * @return the field, or null if it is not found
   */
  FormField questionFormField(String fieldId);

  /**
   * Search for a particular value within a specified FormField, if possible.
   *
   * @param fieldId the field you want (recursively searched using {@link #questionFormField(String)})
   * @param valueId the id (not label) of the value you want within that field
   * @return Java representation of the value, or null if it could not be found
   */
  FormFieldValue questionFormFieldValue(String fieldId, String valueId);

  /**
   * Timestamp of when the question was sent to the client.
   */
  Date getQuestionTime();

  /**
   * Timestamp of when the answer was sent to the server.
   */
  Date getAnswerTime();
}
