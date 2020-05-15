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

import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import java.util.ArrayList;
import java.util.List;

import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Convenience class bundling the submit status and answer for a survey question that has been answered.
 */
public class Answer {
  protected SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private String submitStatusJson;
  private SubmitStatus submitStatus;
  private String answerJson;

  public Answer(String submitStatusJson, String answerJson) {
    this.submitStatusJson = submitStatusJson;
    if (submitStatusJson != null) {
      this.submitStatus = AutoBeanCodex.decode(factory, SubmitStatus.class, submitStatusJson).as();
    }
    this.answerJson = answerJson;
  }

  public Answer(String submitStatusJson, SubmitStatus submitStatus, String answerJson) {
    this.submitStatusJson = submitStatusJson;
    this.submitStatus = submitStatus;
    this.answerJson = answerJson;
  }

  public String getSubmitStatusJson() {
    return submitStatusJson;
  }

  public SubmitStatus getSubmitStatus() {
    return submitStatus;
  }

  public String getAnswerJson() {
    return answerJson;
  }

  public boolean providedBy(String surveyProviderId) {
    return surveyProviderId.equals(getSubmitStatus().getSurveyProviderId());
  }

  public boolean inSection(String sectionId) {
    return sectionId.equals(getSubmitStatus().getSurveySectionId());
  }

  public FormAnswer form() {
    return AutoBeanCodex.decode(factory, FormAnswer.class, getAnswerJson()).as();
  }

  public FormFieldAnswer formField(FormAnswer form, String fieldId) {
    FormFieldAnswer result = null;

    if (form != null && fieldId != null) {
      List<FormFieldAnswer> fields = form.getFieldAnswers();

      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (fieldId.equals(field.getFieldId())) {
            result = field;
            break;
          }
        }
      }
    }

    return result;
  }

  public FormFieldAnswer formField(String fieldId) {
    return formField(form(), fieldId);
  }

  /**
   * Note this is actually the "id" field of the value, not the "label", which may have defaulted
   * to the label.
   *
   * @return a collection with values, if any; never null
   */
  public List<String> formFieldValues(String fieldId) {
    List<String> values = null;
    FormFieldAnswer fieldAnswer = formField(form(), fieldId);
    if (fieldAnswer != null) {
      values = fieldAnswer.getChoice();
    }
    if (values == null) {
      values = new ArrayList<>();
    }
    return values;
  }

  /**
   * Note this is actually the "id" field of the value, not the "label", which may have defaulted
   * to the label.
   *
   * @return the first value in the given field if any, or null; empty string will get converted to null
   */
  public String formFieldValue(String fieldId) {
    String value = null;
    List<String> values = formFieldValues(fieldId);
    if (values.size() > 0) {
      value = values.get(0);
    }
    if (value != null && value.length() == 0) {
      value = null;
    }
    return value;
  }

  public boolean formFieldContains(FormFieldAnswer fieldAnswer, String choice) {
    boolean result = false;

    if (fieldAnswer != null && choice != null) {
      List<String> choices = fieldAnswer.getChoice();

      if (choices != null) {
        result = choices.contains(choice);
      }
    }

    return result;
  }

  public boolean formFieldContains(FormAnswer form, String fieldId, String choice) {
    return formFieldContains(formField(form, fieldId), choice);
  }

  public boolean formFieldContains(String fieldId, String choice) {
    return formFieldContains(formField(form(), fieldId), choice);
  }

  public boolean formFieldEquals(FormFieldAnswer fieldAnswer, String choice) {
    boolean result = false;

    if (fieldAnswer != null && choice != null) {
      List<String> choices = fieldAnswer.getChoice();

      if (choices != null && choices.size() == 1) {
        result = choices.contains(choice);
      }
    }

    return result;
  }

  public boolean formFieldEquals(String fieldId, String choice) {
    return formFieldContains(formField(form(), fieldId), choice);
  }

  public boolean questionIdHasPrefix(String prefix) {
    String questionId = getSubmitStatus().getQuestionId();
    return questionId != null && questionId.startsWith(prefix);
  }

  public String questionId() {
    return getSubmitStatus().getQuestionId();
  }

  public String questionIdAfterPrefix(String prefix) {
    String questionId = getSubmitStatus().getQuestionId();
    if (questionId != null && questionId.startsWith(prefix)) {
      return questionId.substring(prefix.length());
    }
    return null;
  }
}
