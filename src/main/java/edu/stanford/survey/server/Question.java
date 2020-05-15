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

import edu.stanford.survey.client.api.DisplayStatus;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.SessionStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Simple container for representing a question on the server side.
 */
public class Question {
  protected SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private DisplayStatus displayStatus;
  private AutoBean<?> question;
  private String surveyName;
  private Long surveyCompatLevel;

  public Question(String questionId) {
    displayStatus = factory.displayStatus().as();
    displayStatus.setCompatLevel(SurveyFactory.compatibilityLevel);
    displayStatus.setQuestionId(questionId);
  }

  public Question(String questionId, QuestionType type) {
    this(questionId);
    displayStatus.setQuestionType(type);
  }

  public Question(DisplayStatus displayStatus, AutoBean<?> question) {
    this.displayStatus = displayStatus;
    this.question = question;
  }

  /**
   * Set the validation message and session status, if the passed parameter is non-null.
   * Do nothing if the parameter is null.
   */
  public Question validate(String message) {
    if (message != null) {
      displayStatus.setServerValidationMessage(message);
      displayStatus.setSessionStatus(SessionStatus.questionInvalid);
    }
    return this;
  }

  public Question withProvider(String surveyProviderId) {
    displayStatus.setSurveyProviderId(surveyProviderId);
    return this;
  }

  public DisplayStatus getDisplayStatus() {
    return displayStatus;
  }

  public void setDisplayStatus(DisplayStatus displayStatus) {
    this.displayStatus = displayStatus;
  }

  public AutoBean<?> getQuestion() {
    return question;
  }

  public void setQuestion(AutoBean<?> question) {
    this.question = question;
  }

  public void form(String title1, FormField... fields) {
    displayStatus.setQuestionType(QuestionType.form);
    setQuestion(formQuestion(title1, null, fields));
  }

  public void form(String title1, String title2, FormField... fields) {
    displayStatus.setQuestionType(QuestionType.form);
    setQuestion(formQuestion(title1, title2, fields));
  }

  public void formTerminal(String title1, FormField... fields) {
    displayStatus.setQuestionType(QuestionType.form);
    displayStatus.setSessionStatus(SessionStatus.clearSession);
    AutoBean<FormQuestion> form = formQuestion(title1, null, fields);
    form.as().setTerminal(true);
    setQuestion(form);
  }

  public void formTerminal(String title1, String title2, FormField... fields) {
    displayStatus.setQuestionType(QuestionType.form);
    displayStatus.setSessionStatus(SessionStatus.clearSession);
    AutoBean<FormQuestion> form = formQuestion(title1, title2, fields);
    form.as().setTerminal(true);
    setQuestion(form);
  }

  public AutoBean<FormQuestion> formQuestion(String title1, FormField... fields) {
    return formQuestion(title1, null, fields);
  }

  public AutoBean<FormQuestion> formQuestion(String title1, String title2, FormField... fields) {
    AutoBean<FormQuestion> bean = factory.formQuestion();
    FormQuestion question = bean.as();

    question.setTitle1(title1);
    question.setTitle2(title2);

    if (fields.length > 0) {
      List<FormField> fieldList = new ArrayList<>();
      Collections.addAll(fieldList, fields);
      question.setFields(fieldList);
    }

    return bean;
  }

  public FormField required(FormField field) {
    field.setRequired(true);
    return field;
  }

  public FormField range(FormField field, String min, String max) {
    field.setMin(min);
    field.setMax(max);
    return field;
  }

  public FormField field(String fieldId, FieldType type, String label) {
    FormField field = factory.field().as();
    field.setFieldId(fieldId);
    field.setType(type);
    field.setLabel(label);
    return field;
  }

  public FormField field(String fieldId, FieldType type, FormFieldValue... values) {
    return field(fieldId, type, null, values);
  }

  public FormField field(String fieldId, FieldType type, String label, FormFieldValue... values) {
    FormField field = factory.field().as();
    field.setFieldId(fieldId);
    field.setType(type);
    field.setLabel(label);
    ArrayList<FormFieldValue> valuesList = new ArrayList<>();
    Collections.addAll(valuesList, values);
    field.setValues(valuesList);
    return field;
  }

  public FormFieldValue value(String label) {
    return value(label, label);
  }

  public FormFieldValue value(String id, String label) {
    FormFieldValue value = factory.value().as();
    value.setId(id);
    value.setLabel(label);
    return value;
  }

  public FormFieldValue value(String label, FormField... fields) {
    return value(label, label, fields);
  }

  public FormFieldValue value(String id, String label, FormField... fields) {
    FormFieldValue value = factory.value().as();
    value.setId(id);
    value.setLabel(label);

    ArrayList<FormField> fieldList = new ArrayList<>();
    Collections.addAll(fieldList, fields);
    value.setFields(fieldList);

    return value;
  }

  public String getSurveyName() {
    return surveyName;
  }

  public void setSurveyName(String surveyName) {
    this.surveyName = surveyName;
  }

  public Long getSurveyCompatLevel() {
    return surveyCompatLevel;
  }

  public void setSurveyCompatLevel(Long surveyCompatLevel) {
    this.surveyCompatLevel = surveyCompatLevel;
  }

  public Question withSurvey(String surveyName, long surveyCompatLevel) {
    setSurveyName(surveyName);
    setSurveyCompatLevel(surveyCompatLevel);
    return this;
  }
}
