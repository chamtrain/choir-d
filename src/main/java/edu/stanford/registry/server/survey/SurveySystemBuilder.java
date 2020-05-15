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

package edu.stanford.registry.server.survey;

import edu.stanford.registry.client.api.SurveyBuilderForm;
import edu.stanford.registry.client.api.SurveyBuilderFormCondition;
import edu.stanford.registry.client.api.SurveyBuilderFormCondition.Method;
import edu.stanford.registry.client.api.SurveyBuilderFormCondition.Type;
import edu.stanford.registry.client.api.SurveyBuilderFormFieldValue;
import edu.stanford.registry.client.api.SurveyBuilderFormQuestion;
import edu.stanford.registry.client.api.SurveyBuilderFormResponse;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfig;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.utils.RegistryQuestionUtils;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.User;
import edu.stanford.survey.client.api.DisplayStatus;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.server.Answer;
import edu.stanford.survey.server.Question;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;
import edu.stanford.survey.server.SurveySystemBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Implementation of surveys for testing surveys created with the survey builder.
 */
public class SurveySystemBuilder extends SurveySystemBase {
  private static final Logger log = LoggerFactory.getLogger(SurveySystemBuilder.class);

  private final static ConcurrentHashMap<String, Long> studySite = new ConcurrentHashMap<>();

  private final Supplier<Database> database;
  private final SiteInfo siteInfo; // for the bldr (e.g. 100L) site
  private final AppConfig appConfig;
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  private String studyName = null; // comes in as the token
  private Long surveySiteId;

  public SurveySystemBuilder(Supplier<Database> database, AppConfig config, SiteInfo siteInfo) {
    this.database = database;
    this.siteInfo = siteInfo;
    this.appConfig = config;
  }

  /**
   * Extracts N site decimal digits D and the <studyName> from a token like NDD<studyName.
   */
  private String getTokenSetSite(String token) {
    if (surveySiteId != null) {
      log.warn("validateStartToken() was called a 2nd time, with token="+token);
    }
    int n = token.charAt(0) - '0';
    if (n < 0 || n > 9) {
      log.warn("Expected nDstudy, but where 0<n<10, but n="+n);
      return token;
    }
    int siteN = 0;
    for (int i = 1;  i<=n;  i++) {
      siteN = (siteN * 10) + token.charAt(i) - '0';
    }
    this.surveySiteId = (long) siteN;
    token = token.substring(1 + n);
    studySite.put(token, surveySiteId);
    return token;
  }

  @Override
  public String validateStartToken(String token) {
    this.studyName = getTokenSetSite(token);
    log.debug("validateStartToken set token to " + token);
    return null;  // signals caller to generate a real token
  }

  @Override
  public Question startWithValidToken(String token, Survey survey) {
    log.debug("startWithValidToken set token to " + token + "and surveyToken " +
        survey.getSurveyToken() + " and this.token " + this.studyName);
    return firstQuestion(this.studyName);
  }

  @Override
  public Question nextQuestion(Answer answer, Survey survey) {
    if (answer != null) {
      SubmitStatus submitStatus = answer.getSubmitStatus();
      String studyContent = surveyBuilderContents(submitStatus.getSurveySectionId());
      final SurveyBuilderForm form = AutoBeanCodex.decode(factory, SurveyBuilderForm.class, studyContent).as();
      SurveyBuilderFormQuestion builderFormQuestion = getNextFormQuestion(form, answer, survey);
      if (builderFormQuestion == null) {
        return thankYou();
      }

      return makeQuestion(builderFormQuestion, submitStatus.getSurveySectionId());
    } else {
      return firstQuestion(this.studyName);
    }
   }

  @Override
  public String getStyleSheetName() {
    return "painmanagement-2016-01-19.cache.css";
  }

  @Override
  public Question getThankYouPage(String token) {
    return thankYou();
  }

  private Question firstQuestion(String studyName) {

    String studyContent = surveyBuilderContents(studyName);
    if (studyContent == null) {
      return null;
    }
    final SurveyBuilderForm form = AutoBeanCodex.decode(factory, SurveyBuilderForm.class, studyContent).as();
    SurveyBuilderFormQuestion builderFormQuestion = form.getQuestions().get(0);
    return makeQuestion(builderFormQuestion, studyName);
  }

  private SurveyBuilderFormQuestion getNextFormQuestion(SurveyBuilderForm form, Answer answer, Survey s) {
    boolean foundLastQuestion = false;
    for (SurveyBuilderFormQuestion formQuestion : form.getQuestions()) {
      if (foundLastQuestion && qualifies(answer, form, formQuestion, s)) {
        return formQuestion;
      }
      if (answer.questionId().equals(formQuestion.getId())) {
        foundLastQuestion = true;
      }
    }
    return null;
  }

  private Question makeQuestion(SurveyBuilderFormQuestion builderFormQuestion , String studyName) {

    return formQuestion(builderFormQuestion, studyName);
  }

  private Question formQuestion(SurveyBuilderFormQuestion builderFormQuestion, String studyName) {
    // the others are all FormQuestion
    AutoBean< FormQuestion> bean = factory.formQuestion();
    FormQuestion question = bean.as();
    question.setTitle1(builderFormQuestion.getTitle1());
    question.setTitle2(builderFormQuestion.getTitle2());
    question.setFields(new ArrayList<>());

    for (SurveyBuilderFormResponse response : builderFormQuestion.getResponses()) {

      if (response.getFieldType() == FieldType.radios) {
        question.getFields().add(getRadioField(response, builderFormQuestion.getOrder()));
      } else if (response.getFieldType() == FieldType.checkboxes){
        question.getFields().add(getRadioField(response, builderFormQuestion.getOrder()));
      } else if (response.getFieldType() == FieldType.textArea) {
        question.getFields().add(getInputField(response, builderFormQuestion.getOrder()));
      } else if (response.getFieldType() == FieldType.number) {
        question.getFields().add(getInputField(response, builderFormQuestion.getOrder()));
      } else if (response.getFieldType() == FieldType.text) {
        question.getFields().add(getInputField(response, builderFormQuestion.getOrder()));
      } else if (response.getFieldType() == FieldType.collapsibleContentField) {
        question.getFields().add(getCollapsibleField(response, builderFormQuestion.getOrder()));
      } else if (response.getFieldType() == FieldType.numericScale) {
        question.getFields().add(getNumericScaleField(response, builderFormQuestion.getOrder()));
      } else if (response.getFieldType() == FieldType.numericSlider) {
        question.getFields().add(getNumericSliderField(response, builderFormQuestion.getOrder()));
      } else if (response.getFieldType() == FieldType.datePicker) {
        question.getFields().add(getDatePickerField(response, builderFormQuestion.getOrder()));
      } else if (response.getFieldType() == FieldType.textBoxSet) {
        question.getFields().add(getTextBoxSetField(response, builderFormQuestion.getOrder()));
      } else if (response.getFieldType() == FieldType.dropdown) {
        question.getFields().add(getDropDownField(response, builderFormQuestion.getOrder()));
      } else if (response.getFieldType() == FieldType.radioSetGrid) {
        question.getFields().add( getRadioSetGrid(response, builderFormQuestion.getOrder()));
      }
    }

    DisplayStatus displayStatus = makeDisplayStatus(studyName, builderFormQuestion.getId());
    return new Question(displayStatus, bean);
  }

  private Question thankYou() {
    Question q = new Question("thanks", QuestionType.form);

    AutoBean<FormQuestion> form = q.formQuestion("Done testing");
    form.as().setTerminal(true);
    q.setQuestion(form);
    return q;
  }

  private DisplayStatus makeDisplayStatus(String studyName, String questionId) {
    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setQuestionType(QuestionType.form);
    displayStatus.setSurveyProviderId( "SurveyBuilder" );
    displayStatus.setSurveySectionId(studyName);
    displayStatus.setQuestionId(questionId);
    return displayStatus;
  }

  private FormField makeField(SurveyBuilderFormResponse response, String order) {
    FormField field = factory.field().as();
    field.setType(response.getFieldType());
    field.setFieldId(order + ":" + response.getOrder() + ":" + response.getRef());
    field.setRequired(response.getRequired());
    field.setValues(new ArrayList<>());
    return field;
  }

  private FormField getRadioField(SurveyBuilderFormResponse response, String order) {
    FormField field = makeField(response, order);
    field.setLabel(response.getLabel());
    for (SurveyBuilderFormFieldValue value : response.getValues()) {
        FormFieldValue ffValue = factory.value().as();
        ffValue.setId(value.getId());
        ffValue.setLabel(value.getLabel());
        field.getValues().add(ffValue);
    }
    return field;
  }

  private FormField getInputField(SurveyBuilderFormResponse response, String order) {
    FormField field = makeField(response, order);
    field.setLabel(response.getLabel());
    field.setAttributes(response.getAttributes());
    if (response.getFieldType() == FieldType.number && field.getAttributes() != null) {
      String min = field.getAttributes().get("min");
      if (min != null) {
        field.setMin(min);
      }
      String max = field.getAttributes().get("max");
      if (max != null) {
        field.setMax(max);
      }
    }
    return field;
  }

  private FormField getCollapsibleField(SurveyBuilderFormResponse response,  String order) {
    FormField field = makeField(response, order);
    field.setLabel(response.getLabel());
    HashMap<String, String> attributes = new HashMap<>();
    String collapsibleContentLabel = "More Information";
    for (SurveyBuilderFormFieldValue value : response.getValues()) {
      collapsibleContentLabel = value.getLabel();
    }
    attributes.put("collapsibleContent", collapsibleContentLabel);
    field.setAttributes(attributes);
    return field;
  }

  private FormField getNumericScaleField(SurveyBuilderFormResponse response, String order) {
    FormField field = makeField(response, order);
    field.setLabel(response.getLabel());
    HashMap<String, String> attributes = new HashMap<>();
    List<SurveyBuilderFormFieldValue> fieldValues = response.getValues();
    if (fieldValues.get(0).getLabel() != null && fieldValues.get(fieldValues.size() - 1).getLabel() != null) {
      attributes.put("leftLabel", fieldValues.get(0).getLabel());
      attributes.put("rightLabel", fieldValues.get(fieldValues.size() - 1).getLabel());
    }
    field.setAttributes(attributes);
    field.setMin(fieldValues.get(0).getId());
    field.setMax(fieldValues.get(fieldValues.size()-1).getId());
    return field;
  }

  private FormField getTextBoxSetField(SurveyBuilderFormResponse response, String order) {
    FormField field = makeField(response, order);
    field.setLabel(response.getLabel());
    for (SurveyBuilderFormFieldValue value : response.getValues()) {
      FormFieldValue formFieldValue = factory.value().as();
      formFieldValue.setId(value.getId());
      formFieldValue.setLabel(value.getLabel());
      field.getValues().add(formFieldValue);
    }
    return field;
  }

  private FormField getNumericSliderField(SurveyBuilderFormResponse response, String order) {
    FormField field = makeField(response, order);
    HashMap<String, String> attributes = new HashMap<>();
    if (response.getLabel() != null) {
      field.setLabel(response.getLabel());
    }
    if (response.getValues() != null && response.getValues().size() > 0
          && response.getValues().get(0) != null && response.getValues().get(0).getId() != null)
        attributes.put("lowerBound", response.getValues().get(0).getId());
    if (response.getValues() != null && response.getValues().size() > 0 &&
        response.getValues().get(response.getValues().size() - 1).getId() != null)
        attributes.put("upperBound",response.getValues().get(response.getValues().size() - 1).getId());

    field.setAttributes(attributes);

    return field;
  }

  private FormField getDatePickerField(SurveyBuilderFormResponse response, String order) {
    FormField field = makeField(response, order);
    field.setLabel(response.getLabel());
    field.setAttributes(response.getAttributes());
    return field;
  }

  private FormField getDropDownField(SurveyBuilderFormResponse response, String order) {
    FormField field = makeField(response, order);
    field.setLabel(response.getLabel());
    for (SurveyBuilderFormFieldValue value : response.getValues()) {
      FormFieldValue ffValue = factory.value().as();
      ffValue.setId(value.getId());
      ffValue.setLabel(value.getLabel());
      field.getValues().add(ffValue);
    }
    if (response.getAttributes() != null) {
      field.setAttributes(response.getAttributes());
    }
    return field;
  }

  private FormField getRadioSetGrid(SurveyBuilderFormResponse response, String order) {
    log.debug("SurveySystemBuilder in makeField");
    FormField field = makeField(response, order);

    for (SurveyBuilderFormFieldValue value : response.getValues()) {
      FormFieldValue ffValue = factory.value().as();
      log.debug("SurveySystemBuilder setting label: " + value.getLabel() + " and id: " + value.getId());
      ffValue.setLabel(value.getLabel());
      ffValue.setId( value.getId());
      field.getValues().add(ffValue);
    }
    if (response.getAttributes() != null) {
      field.setAttributes(response.getAttributes());
    }
    log.debug(field.toString());
    return field;
  }

  private String surveyBuilderContents(String token) {
    if (surveySiteId == null) {
      surveySiteId = studySite.get(token);
    }
    return appConfig.forName(surveySiteId, "surveycontent", token);
  }

  private boolean qualifies(Answer answer, SurveyBuilderForm form, SurveyBuilderFormQuestion formQuestion, Survey s) {
    if (formQuestion.getConditions() == null) {
      return true;
    }
    boolean qualifies = true;
    for (SurveyBuilderFormCondition formCondition : formQuestion.getConditions()) {
      if (formCondition.getType() == Type.patientAttribute) {
        if (!attributeQualifies(formCondition)) {
          qualifies = false;
        }
      }

      if (formCondition.getType() == Type.response) {
        if (!responseQualifies(answer, form, formCondition, s, formQuestion.getOrder())) {
          qualifies = false;
        }
      } else {
        if (!itemQualifies(answer, form, formCondition, s)) {
          qualifies = false;
        }
      }
    }
    return qualifies;
  }

  private boolean responseQualifies(Answer answer, SurveyBuilderForm form, SurveyBuilderFormCondition formCondition, Survey s, String questionOrder) {
    SurveyBuilderFormFieldValue formFieldValue = formCondition.getValue();
    String reference = formFieldValue.getRef();
    Method condition = formCondition.getMethod();
    if (condition == null || reference == null) {
      return true;
    }
    String dataValue = formCondition.getDataValue();
    // Find the question for the reference and see if the condition is met by the answer given
    for (SurveyBuilderFormQuestion fQuestion : form.getQuestions()) {
      for (SurveyBuilderFormResponse fResponse : fQuestion.getResponses()) {
        if (fResponse.getRef() != null && fResponse.getRef().equals(reference)) {
          FieldType fieldType = fResponse.getFieldType();
          String fieldId = fQuestion.getOrder() + ":" + fResponse.getOrder() + ":" + reference;

          SurveyDao surveyDao = new SurveyDao(database);
          SurveyQuery surveyQuery = new SurveyQuery(database, surveyDao, siteInfo.getSiteId());
          s = surveyQuery.surveyBySurveyTokenId(s.getSurveyTokenId(), s.getSurveyToken(), s.isComplete());
          SurveyStep step = s.answeredStepByQuestion(fQuestion.getId());
          if (step == null ) { // The question wasn't asked
            return Method.notexists == condition || Method.notequal == condition;
          }
          Answer compareToAnswer = step.answer();
          if (step.answer() == null || step.answerJson() == null) {
            try {
              if (Integer.parseInt(fQuestion.getOrder()) + 1 == Integer.parseInt(questionOrder) ||
                  fResponse.getRef().equals(formFieldValue.getRef())) {
                compareToAnswer = answer;
                log.trace("Using the last answer > {}", compareToAnswer.getAnswerJson());
              }
            } catch (NumberFormatException nfe) {
              //
            }
          }

          if (fieldType == FieldType.textBoxSet) {
            List<String> choices = compareToAnswer.formFieldValues(fieldId);
            for (String choice : choices) {
              if (meets(condition, choice, dataValue)) {
                return true;
              }
            }
          } else if (fieldType == FieldType.text || fieldType == FieldType.textArea) {
            return meets(condition, compareToAnswer.formFieldValue(fieldId), dataValue);
          } else if (fieldType == FieldType.number) {
            try {
              return meets(condition, new Integer(compareToAnswer.formFieldValue(fieldId)), new Integer(dataValue));
            } catch (NumberFormatException nfe) {
              log.debug("Cant compare the entered value of  {} to {} as integers, comparing as strings", compareToAnswer.formFieldValue(fieldId), dataValue);
              meets(condition, compareToAnswer.formFieldValue(fieldId), dataValue);
            }
          } else if (fieldType == FieldType.datePicker) {
            return meets(condition, compareToAnswer.formFieldValue(fieldId), dataValue);
          } else if (fieldType == FieldType.numericScale) {
            SurveyAdvanceUtils surveyAdvUtils = new SurveyAdvanceUtils(siteInfo);
            String choice = surveyAdvUtils.getChoice(compareToAnswer, fieldId);
            if (choice == null || choice.isEmpty()) {
              Integer numAnswer = step.answerNumeric();
              if (numAnswer != null) {
                choice = numAnswer.toString();
              }
            }
            try {
              if (choice != null) {
                return meets(condition, new Integer(choice), new Integer(dataValue));
              }
            } catch (NumberFormatException nfe) {
              log.debug("Cant compare the entered value of  {} to {} as integers, comparing as strings", choice, dataValue);
            }
            return meets(condition, choice, dataValue);
          } else if (fieldType == FieldType.numericSlider) {
            SurveyAdvanceUtils surveyAdvUtils = new SurveyAdvanceUtils(siteInfo);
            try {
              String choice = surveyAdvUtils.getChoice(compareToAnswer, fieldId);
              if (choice != null) {
                return meets(condition, new Integer(choice), new Integer(dataValue));
              }
            } catch (NumberFormatException nfe) {
              log.debug("Cant compare the entered value of  {} to {} as integers, comparing as strings", surveyAdvUtils.getChoice(compareToAnswer, fieldId), dataValue);
            }
            return meets(condition, surveyAdvUtils.getChoice(compareToAnswer, fieldId), dataValue);
          }
        }
      }
    }
   return true;
  }
    private boolean itemQualifies(Answer lastAnswer, SurveyBuilderForm form, SurveyBuilderFormCondition formCondition, Survey s) {


      SurveyBuilderFormFieldValue conditionFieldValue = formCondition.getValue();
      if (conditionFieldValue == null || conditionFieldValue.getRef() == null || formCondition.getMethod() == null) {
        return true;
      }
      String reference = conditionFieldValue.getRef();
      Method method = formCondition.getMethod();
      if (method == null) {
        return true;
      }
      log.trace("Question is Checking for condition {} {} on survey with tokenId = {}", method.value(), reference,
                s.getSurveyTokenId());
      boolean foundMatch = false;

      // Find the question for the reference and see if the condition is met by the answer given
      for (SurveyBuilderFormQuestion formQuestion : form.getQuestions()) {
        for (SurveyBuilderFormResponse fResponse : formQuestion.getResponses()) {
          if (//formQuestion != null && formQuestion.getOrder() != null &&
              fResponse.getRef() != null && fResponse.getRef().equals(reference)) {
            String fieldId = formQuestion.getOrder() + ":" + fResponse.getOrder() + ":" + reference;
            log.trace("Found the reference on questionId {} FieldId {} Fieldtype is ", formQuestion.getId(), fieldId,
                      fResponse.getFieldType().toString());
            SurveyStep step = s.answeredStepByQuestion(formQuestion.getId());
            Answer conditionnAnswer = null;
            if (step != null) {
                conditionnAnswer = step.answer();
            }
            if (step == null || step.answer() == null || step.answerJson() == null) {
              try {
                if (lastAnswer.getSubmitStatus() != null && lastAnswer.getSubmitStatus().getQuestionId().equals(formQuestion.getId())) {
                  conditionnAnswer = lastAnswer;
                  log.trace("Using the last answer : {}", conditionnAnswer.getAnswerJson());
                }
              } catch (NumberFormatException nfe) {
                //
              }
            }
            if (conditionnAnswer != null) {
              log.trace("answer json is {}", conditionnAnswer.getAnswerJson());
            }
            if (conditionnAnswer != null && conditionnAnswer.formFieldValues(fieldId) != null) {
              List<String> choices = conditionnAnswer.formFieldValues(fieldId);
              for (String choice : choices) { // answers selected
                for (SurveyBuilderFormResponse conditionResp : conditionFieldValue.getResponses()) {
                  if (conditionResp.getFieldType() == FieldType.checkboxes) {

                    for (SurveyBuilderFormFieldValue responseValue : fResponse.getValues()) {
                      log.trace("Comparing cb response ref {} to {}", responseValue.getRef(), conditionResp.getRef());
                      if (conditionResp.getRef().equals(responseValue.getRef())) {
                        if (responseValue.getId().equals(choice)) {
                          log.trace("Found {} equals choice {} setting foundMatch=true", responseValue.getId(), choice);
                          foundMatch = true;
                        }
                      }
                    }
                  } else { // radios
                    log.trace("Comparing radio response ref {} to {}", fResponse.getRef(), conditionFieldValue.getRef());
                    if (conditionFieldValue.getRef() != null && fResponse.getRef().equals(conditionFieldValue.getRef())) {
                      List<SurveyBuilderFormResponse> conditionResponses = conditionFieldValue.getResponses();
                      for (SurveyBuilderFormResponse conditionalResponse : conditionResponses)
                         for (SurveyBuilderFormFieldValue responseValue : fResponse.getValues()) {
                           log.trace("Comparing {} == {} && {} == {} [choice]", conditionalResponse.getLabel(),
                                 responseValue.getLabel(), responseValue.getId(), choice);
                          if (conditionalResponse.getLabel().equals(responseValue.getLabel()) && responseValue.getId().equals(choice) ) {
                            log.trace("setting foundMatch=true ");
                            foundMatch = true;
                        }
                      }
                    }
                  }

                }
              }
            }
          }
        }
      }
      if (method == Method.exists  && foundMatch) { // selected
        log.trace("Comparing exists and foundmatch returning true");
        return true;
      } else if (method == Method.notexists && !foundMatch) { // not selected
        log.trace("Comparing notexists and !foundmatch returning true");
        return true;
      }
      log.trace("Comparing returning false");
      return false;
  }

  private boolean attributeQualifies(SurveyBuilderFormCondition condition) {

    User adminUser = ServerUtils.getAdminUser(database.get());
    AppConfigDao appConfigDao = new AppConfigDao(database.get(), adminUser);
    AppConfigEntry appConfigEntry = appConfigDao.findAppConfigEntry(siteInfo.getSiteId(), "builder", "test.patient");
    if (appConfigEntry == null || appConfigEntry.getConfigValue() == null
        || appConfigEntry.getConfigValue().isEmpty()) {
      return true;
    }
    PatientDao patientDao = new PatientDao(database.get(), siteInfo.getSiteId(), adminUser);
    Patient patient = patientDao.getPatient(appConfigEntry.getConfigValue());
    return patient == null
        || RegistryQuestionUtils.meetsCondition(patient, condition.getAttributes().get(Type.patientAttribute.value()), condition.getDataValue(), condition.getMethod().value());
  }

  private boolean meets (Method method, String answer, String compareToValue) {

    if (method == Method.exists && answer != null) {
      return true;
    }
    if (method == Method.notexists && answer == null) {
      return true;
    }
    if (method == null || answer == null || compareToValue == null) {
      log.trace("skipping question. method, answer or compareToValue is null");
      return false;
    }

    log.trace(" comparing string {} to {} for condition {}", answer, compareToValue, method.value());
    boolean meets = false;
    if (method == Method.notequal) {
      meets = !compareToValue.equals(answer);
    }
    if (method == Method.equal) {
      meets = compareToValue.equals(answer);
    }
    if (method == Method.greaterthan) {
      meets = (answer.compareTo(compareToValue) > 0);
    }
    if (method == Method.greaterequal) {
      meets = answer.compareTo(compareToValue) >= 0;
    }
    if (method == Method.lessequal) {
      meets = answer.compareTo(compareToValue) <= 0;
    }
    if (method == Method.lessthan) {
      meets = answer.compareTo(compareToValue) < 0;
    }
    log.trace(" returning {} ", meets);
    return meets;
  }

  private boolean meets(Method method, Integer answer, Integer compareToValue) {

    if (method == Method.exists && answer != null) {
      return true;
    }
    if (method == Method.notexists && answer == null) {
      return true;
    }
    if (method == null || answer == null || compareToValue == null) {
      return false;
    }

    log.trace(" comparing integer value {} to {} for condition {}", answer, compareToValue, method.value());
    boolean meets = false;
    if (method == Method.notequal) {
      meets = !compareToValue.equals(answer);
    }
    if (method == Method.equal) {
      meets = compareToValue.equals(answer);
    }
    if (method == Method.greaterthan) {
      meets = (answer.compareTo(compareToValue) > 0);
    }
    if (method == Method.greaterequal) {
      meets = answer.compareTo(compareToValue) >= 0;
    }
    if (method == Method.lessequal) {
      meets = answer.compareTo(compareToValue) <= 0;
    }
    if (method == Method.lessthan) {
      meets = answer.compareTo(compareToValue) < 0;
    }
    log.trace(" returning {} ", meets);
    return meets;
  }

  static private class SurveyAdvanceUtils extends SurveyAdvanceBase {
    // --Commented out by Inspection START (5/25/17, 8:53 AM):
    //  public Integer answerNumeric(SurveyFactory factory, String json) {
    //    //SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
    //    NumericAnswer answer = AutoBeanCodex.decode(factory, NumericAnswer.class, json).as();
    //    return answer == null ? null : answer.getChoice();
    //  }
    // --Commented out by Inspection STOP (5/25/17, 8:53 AM)

    SurveyAdvanceUtils(SiteInfo siteInfo) {
    super(siteInfo);
  }

    String getChoice(Answer answer, String fieldId) {
      if (answer.formFieldValues(fieldId) != null) {
        List<String> choices = answer.formFieldValues(fieldId);
        if (choices != null && choices.size() > 0) {
          return choices.get(0);
        }
      }
      return "";
    }
  }
}
