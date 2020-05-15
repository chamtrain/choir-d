/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.survey.client.api.DisplayStatus;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class AngerService extends QualifyQuestionService {

  private static final Logger logger = LoggerFactory.getLogger(AngerService.class);
  private static final String surveySystemNameBase = "AngerService";
  private static final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private static final String testConsentText = "Sometimes we need to test new questionnaires. "
      + "This is like testing a recipe. We think we have the right questions and the right number of questions (ingredients and amount of ingredients). "
      + "You can help by choosing ‘Yes’ below and answering the questions truthfully. This should take about 5-10 minutes. "
      + "Of course, all answers will be confidential. No matter what you decide your treatment will be the same. "
      + "<p></p><p></p>Note that at the end of this part, you will be asked if we may contact you for a follow up survey 3 months from now. "
      + "A raffle for 40 Amazon gift cards ($25 each) will take place among all those who complete the current and the follow-up surveys. "
      + "There is an estimated 8% chance of winning! "
      + "<p></p><p></p>Many thanks in advance. People like you move patient care and science forward.";
  private static final String[] testConsentResp = { "Yes, I will take the following test questionnaire",
      "No. I do not wish to test the questionnaire" };
  private static final String followConsentText = "Thank you for completing these items, and the entire survey. "
      + "To provide improved health care to you and other patients, we rely on your collaboration in completing such surveys. "
      + "As part of this effort and as a follow-up, would you be willing to answer a similar survey 3 months from now?\n"
      + "<p></p><p></p>A raffle for 40 Amazon gift cards ($25 each) will take place among all those who completed the current and will "
      + "also complete the follow-up surveys. There is an estimated 8% chance of winning! "
      + "<p></p><p></p>Many thanks in advance. People like you move patient care and science forward.";
  private static final String[] followConsentResp = { "Yes, you may contact me with an additional survey 3 months from now.",
      "No. I do not wish you contact me to test the questionnaire." };
  private static final String followUpSurveyText =
      "Thank you for agreeing to participate in the Pain Management Center’s"
          + " online survey. Three months ago you agreed to be contacted for a follow-up survey. The goal of this survey is "
          + "to improve quality care and provide the basis for more research programs. This survey is independent of other surveys "
          + "related to your continued treatment plan at the center. Thus, some questions might replicate - we apologize in advance "
          + "for any inconvenience. Please answer the questions truthfully – all answers will be confidential. This should "
          + "take about 15-20 minutes.<p></p><p></p> As a token of appreciation, a raffle for 40 Amazon gift cards ($25 each) will take "
          + "place among all those who complete the previous and current surveys. There is an estimated 8% chance of winning! "
          + "<p></p><p></p>Many thanks in advance. People like you move patient care and science forward.";
  private static final String[] followUpSurveyResp = {};
  private static final String[] questionText = { testConsentText, followConsentText, followUpSurveyText };
  private static final String[][] responseText = { testConsentResp, followConsentResp, followUpSurveyResp };
  private static final String[] questionNumber = { "0", "99", "0" };
  private static final String[] refTags = { "agree_to_test", "agree_to_follow_ups", "follow_up_intro" };
  private static final String formYesRespBeg = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items><Item ItemResponse=\"0\" ItemScore=\"0\" Order=\"0\" TimeFinished=\"\"><Description>";
  private static final String formNoRespBeg = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items><Item ItemResponse=\"1\" ItemScore=\"1\" Order=\"0\" TimeFinished=\"\"><Description>";
  private static final String formYesRespEnd =
      "</Description><Responses><Response Order=\"1\" Type=\"select1\" required=\"true\"><item selected=\"true\"><label>Yes</label><value>0</value></item>"
          + "<item selected=\"false\"><label>No</label><value>1</value></item></Response></Responses></Item></Items></Form>";
  private static final String formNoRespEnd =
      "</Description><Responses><Response Order=\"1\" Type=\"select1\" required=\"true\"><item selected=\"false\"><label>Yes</label><value>0</value></item>"
          + "<item selected=\"true\"><label>No</label><value>1</value></item></Response></Responses></Item></Items></Form>";
  public static final String takeConsent = "testTraitAngerConsent";
  public static final String followConsent = "followTraitAngerConsent";
  private static final String followIntro = "followTraitAngerIntro";

  private static final String surveySystemName = "AngerService";
  /**
   * As an implementor of SurveyServiceIntf, this will be cached and must not cache a database.
   */
  public AngerService(String fullServiceName, SiteInfo siteInfo) {
    super(fullServiceName, siteInfo);
  }


  static public boolean isMyService(String serviceName) {
    logger.debug("Checking if {} is me, returning {}", serviceName, serviceName.startsWith(surveySystemNameBase));
    return serviceName.startsWith(surveySystemNameBase);
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
                                     String answerJson) {
    if (patStudyExtended == null || patStudyExtended.getDtChanged() != null) { // missing data or survey is already done
      return null;
    }

    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
    if (patStudy == null) { // doesn't exist !
      throw new DataException("Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
          + patStudyExtended.getToken());
    }

    Patient patient = patStudyExtended.getPatient();
    if (patient == null) {
      throw new DataException("Patient not found for surveyRegId");
    }
    if (patStudyExtended.getStudyDescription() == null) {
      throw new DataException("Study " + patStudyExtended.getStudyCode() + " Has no description value!");
    }

    if (takeConsent.equals(patStudyExtended.getStudyDescription())
        || followConsent.equals(patStudyExtended.getStudyDescription())
        || followIntro.equals(patStudyExtended.getStudyDescription())) {
      if (patStudy.getContents() == null) { // its the first question
        if (submitStatus == null) { // Send the appropriate question
          NextQuestion next = new NextQuestion();
          DisplayStatus displayStatus = factory.displayStatus().as();
          displayStatus.setQuestionType(QuestionType.form);
          displayStatus.setSurveyProviderId(Integer.toString(getSurveySystemId(database)));
          displayStatus.setSurveySectionId(Integer.toString(patStudyExtended.getStudyCode()));
          displayStatus.setQuestionId(questionNumber[getQuestionType(patStudyExtended.getStudyDescription())]);
          next.setDisplayStatus(displayStatus);

          AutoBean<FormQuestion> bean = factory.formQuestion();
          FormQuestion question = bean.as();
          ArrayList<FormField> fields = new ArrayList<>();
          FormField headingField = factory.field().as();
          headingField.setType(FieldType.heading);
          headingField.setFieldId(questionNumber[getQuestionType(patStudyExtended.getStudyDescription())] + ":0:"
              + refTags[getQuestionType(patStudyExtended.getStudyDescription())]);
          headingField.setLabel(questionText[getQuestionType(patStudyExtended.getStudyDescription())]);
          fields.add(headingField);
          String[] options = responseText[getQuestionType(patStudyExtended.getStudyDescription())];
          if (options.length > 0) { // add responses
            fields.add(radiosField(questionNumber[getQuestionType(patStudyExtended.getStudyDescription())] + ":1:"
                + refTags[getQuestionType(patStudyExtended.getStudyDescription())], options));
          }
          question.setFields(fields);
          next.setQuestion(bean);
          return next;
        } else { // handle the response
          logger.debug("Anger: checking TEST Anger survey question");
          // On response to questions 1 if "No" STOP!
          FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();

          if (isQuestionResponse(formAnswer, "0:1:" + refTags[0])) { // Consent to taking the test questionnaire
            if (getFieldChoice(formAnswer, questionNumber[0] + ":1:" + refTags[0]) > 0) {  // NO
              logger.debug("Anger: answer to {} was no, stopping ", refTags[0]);
              addAttribute(database, patStudyExtended.getPatient(), patStudyExtended.getStudyDescription(), attributeValue.N.toString());
              /* Update the testTraitAngerConsent questionnaire */
              patStudyExtended.setContents(formNoRespBeg + testConsentText + formNoRespEnd);
              patStudyDao.setPatientStudyContents(patStudy, patStudyExtended.getContents(), true); // update the will test questionnaire
              logger.debug("updated step {}", patStudyExtended.getOrderNumber());
              /* Get the next questionnaire (the actual TraitAnger questionnaire) and cancel it */
              patStudyExtended = patStudyDao.getPatientStudyExtendedDataByToken(new Token(patStudy.getToken()), ServerUtils.getAdminUser(database));
              patStudy = patStudyExtended;
              patStudyExtended.setContents(emptyForm);
              patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
              logger.debug("updated step {}", patStudyExtended.getOrderNumber());
              /* Get the followTraitAngerConsent questionnaire and cancel it as well */
              patStudyExtended = patStudyDao.getPatientStudyExtendedDataByToken(new Token(patStudy.getToken()), ServerUtils.getAdminUser(database));
              patStudy = patStudyExtended;
              patStudyExtended.setContents(emptyForm);
              patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
              logger.debug("updated step {}", patStudyExtended.getOrderNumber());
              return null;
            } else { // YES
              addAttribute(database, patStudyExtended.getPatient(), patStudyExtended.getStudyDescription(), attributeValue.Y.toString());
              String[] activities = { Constants.REF_TESTQ, Constants.ACTIVITY_COMPLETED }; // mark the survey as complete in test questionnaire
              addActivity(database, patient.getPatientId(), activities, patStudy.getToken());
              /* Update the testTraitAngerConsent questionnaire and return */
              patStudyExtended.setContents(formYesRespBeg + testConsentText + formYesRespEnd);
              patStudyDao.setPatientStudyContents(patStudy, patStudyExtended.getContents(), true);
              return null;
            }
          } else if (isQuestionResponse(formAnswer,
              questionNumber[1] + ":1:" + refTags[1])) { // consent to taking follow ups?
            String value = attributeValue.Y.toString();
            if (getFieldChoice(formAnswer, questionNumber[1] + ":1:" + refTags[1]) > 0) {
              logger.debug("Anger: answer to {} was no, stopping ", refTags[1]);
              value = attributeValue.N.toString();
            }
            addAttribute(database, patStudyExtended.getPatient(), patStudyExtended.getStudyDescription(), value);
            /* Update the followTraitAngerConsent questionnare and move on */
            patStudyExtended.setContents(formNoRespBeg + followConsentText + formYesRespEnd);
            patStudyDao.setPatientStudyContents(patStudy, patStudyExtended.getContents(), true);
            return null;
          } else {  // follow up intro
            /* Update the followUpTrait opening questionnare and move on */
            patStudyExtended.setContents(formYesRespBeg + followUpSurveyText + formYesRespEnd);
            patStudyDao.setPatientStudyContents(patStudy, patStudyExtended.getContents(), true);
            return null;
          }
        }
      }

    }
    return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
  }

  private void addAttribute(Database database, Patient patient, String attributeName, String value) {
    PatientDao patientDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    patientDao.insertAttribute(new PatientAttribute(patient.getPatientId(), attributeName, value, PatientAttribute.STRING));

  }

  private void addActivity(Database database, String patientId, String[] activityType, String token) {
    ActivityDao activityDao = new ActivityDao(database, siteId);
    for (String anActivityType : activityType) {
      activityDao.createActivity(new Activity(patientId, anActivityType, token));
    }
  }

  private int getQuestionType(String study) {
    switch (study) {
    case "testTraitAngerConsent":
      return 0;
    case "followTraitAngerConsent":
      return 1;
    default: // "Anger" survey itself:
      return 2;
    }
  }

  private FormField radiosField(String fieldId, String[] options) {

    FormField field = factory.field().as();
    field.setType(FieldType.radios);
    field.setFieldId(fieldId);
    field.setRequired(false);
    field.setValues(new ArrayList<>());
    for (int inx = 0; inx < options.length; inx++) {
      field.getValues().add(getFormFieldValue(options[inx], Integer.valueOf(inx).toString()));
    }
    return field;
  }

  private FormFieldValue getFormFieldValue(String label, String value) {
    FormFieldValue ffValue = factory.value().as();
    ffValue.setId(value);
    ffValue.setLabel(label);
    return ffValue;
  }

  private boolean isQuestionResponse(FormAnswer formAnswer, String fieldId) {
    for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
      if (fieldId != null && fieldId.equals(fieldAnswer.getFieldId())) {
        return true;
      }
    }
    return false;
  }

  private int getFieldChoice(FormAnswer formAnswer, String fieldId) {
    logger.debug("Anger: getFieldChoice fieldId {}", fieldId);
    for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
      if (fieldId != null && fieldId.equals(fieldAnswer.getFieldId())) {
        logger.debug("found field {} with choice {}", fieldId, fieldAnswer.getChoice().get(0));
        try {
          return Integer.parseInt(fieldAnswer.getChoice().get(0));
        } catch (NumberFormatException nfe) {
          logger.error("invalid number", nfe);
          return -1;
        }
      }
    }
    return -1;
  }

  private int getSurveySystemId(Database database) {
    SurveySystDao dao = new SurveySystDao(database);
    return dao.getSurveySystem(surveySystemName).getSurveySystemId();
  }

  @Override
  public String getSurveySystemName() {
    return surveySystemNameBase;
  }

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    SurveySystem system = getSurveySystem(database, null);
    Study study = new Study(system.getSurveySystemId(), 0, name, 0);
    study.setTitle(title);
    study.setExplanation(explanation);
    SurveySystDao ssDao = new SurveySystDao(database);
    study = ssDao.insertStudy(study);
    return study;
  }

  @Override
  public SurveySystem getSurveySystem(Database database, String qType) {
    SurveySystDao dao = new SurveySystDao(database);
    return dao.getSurveySystem(surveySystemName);
  }

}




