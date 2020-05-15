/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.trauma;


import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.NextQuestion;
import edu.stanford.registry.server.survey.RegistryAssessmentsService;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.survey.SurveyServiceIntf;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;
import edu.stanford.survey.client.api.DisplayStatus;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Custom survey service for Trauma. Created by tpacht on 06/19/2017.
 * Specified in APP_CONFIG: config_type "configparam", configName "SurveyClassForLocal"
 */
public class TraumaSurveyService extends RegistryAssessmentsService
    implements SurveyServiceIntf {

  @SuppressWarnings({ "WeakerAccess", "unused" })
  public TraumaSurveyService(SiteInfo siteInfo) {
    super(siteInfo);
    utils = new TraumaUtils(siteInfo);
  }

  private static final Logger logger = LoggerFactory.getLogger(TraumaSurveyService.class);
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private enum attributeValue {Y, N}
  private final String TRAUMA_CONSENT_ATTRIB = "traumaConsent";
  private final String FAMILY2_ATT = "Family2";
  private final String FAMILY4_ATT = "Family4";
  private final static String emptyForm = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items></Items></Form>";
  private final TraumaUtils utils;

  @Override
  public void registerAssessment(Database database, Element questionaire, String patientId,
                                 Token tok, User user) {

    final String TRAUMA_FAMILY2_CONSENT_ATTRIB = getFamilyConsentAttribute("Family2");
    final String TRAUMA_FAMILY4_CONSENT_ATTRIB = getFamilyConsentAttribute("Family4");
    Patient patient =  new PatientDao(database, siteId, user).getPatient(patientId);

    String studyName = questionaire.getAttribute(edu.stanford.registry.shared.survey.Constants.XFORM_VALUE);
    if (studyName != null && (studyName.startsWith("TraumaConsent") || studyName.startsWith("TraumaFamilyConsent")
      || studyName.startsWith("sendFamily"))) {
      super.registerAssessment(database, questionaire, patientId, tok, user);
    } else {
      SurveyRegistration registration = new AssessDao(database, siteInfo).getRegistration(tok.getToken());
      if (registration.getSurveyType().startsWith("Patient") && patient.hasAttribute(TRAUMA_CONSENT_ATTRIB) &&
          "Y".equals(patient.getAttribute(TRAUMA_CONSENT_ATTRIB).getDataValue())) {
        super.registerAssessment(database, questionaire, patientId, tok, user);
      } else {
        // checks if emailed to a family member who consented
        String mailedTo = whichFamilyMember(database, tok.getToken());
        if (FAMILY2_ATT.equals(mailedTo) && patient.hasAttribute(TRAUMA_FAMILY2_CONSENT_ATTRIB) &&
            "Y".equals(patient.getAttribute(TRAUMA_FAMILY2_CONSENT_ATTRIB).getDataValue())) {
          super.registerAssessment(database, questionaire, patientId, tok, user);
        } else if (FAMILY4_ATT.equals(mailedTo) && (patient.hasAttribute(TRAUMA_FAMILY4_CONSENT_ATTRIB) &&
            "Y".equals(patient.getAttribute(TRAUMA_FAMILY4_CONSENT_ATTRIB).getDataValue()))) {
          super.registerAssessment(database, questionaire, patientId, tok, user);
        }
      }
    }
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended,
                                     SubmitStatus submitStatus, String answerJson) {
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);


    if (patStudyExtended.getStudyDescription() != null
        && patStudyExtended.getStudyDescription().startsWith("TraumaConsent")) {
      //
      // Handle the patient consent survey
      //
      PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
      if (patStudy == null) { // doesn't exist !
        throw new DataException(
            "Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
                + patStudyExtended.getToken());
      }
      if (submitStatus != null) {
        checkIfConsent(database, patStudyExtended, "StudyAttest", TRAUMA_CONSENT_ATTRIB, answerJson);
        checkIfFamily(database, patStudyExtended, answerJson);
        checkIfPatientEmail(database, patStudyExtended, answerJson);
      } else {
        if (patStudy.getContents() == null) { // first question

          Patient patient = patStudyExtended.getPatient();
          if (patient != null && patient.getAttribute(TRAUMA_CONSENT_ATTRIB) != null &&
              attributeValue.Y.toString().equals(patient.getAttribute(TRAUMA_CONSENT_ATTRIB).getDataValue()) &&
              patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES) != null &&
              "y".equals(patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue())) {
            logger.trace( "{} : already exists, skipping question", TRAUMA_CONSENT_ATTRIB);
            // The patient has already consented so skip this questionnaire by writing an empty form
            // and add the optional_questionnaires
            patStudy = patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
            addConsentedQuestionnaires(database, patStudy);
            return null;
          } else if (patient != null && patient.getAttribute(TRAUMA_CONSENT_ATTRIB) != null &&
              (attributeValue.N.toString().equals(patient.getAttribute(TRAUMA_CONSENT_ATTRIB).getDataValue()) ||
                  (patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES) != null &&
                      "n".equals(patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue()))
              )) {
            logger.trace( "{} :  exists, but declined or set to not participating, stopping", TRAUMA_CONSENT_ATTRIB);
            // The patient has already declined participation, skip the questionnaire by
            // writing an empty form
            patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
            deleteRemainingQuestionnaires(database, patStudy.getToken(), patStudy.getOrderNumber());
          }
        }
      }
    } else if (patStudyExtended.getStudyDescription() != null
        && patStudyExtended.getStudyDescription().startsWith("TraumaFamilyConsent")) {
      //
      // Handle the Family consent survey
      PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
      if (patStudy == null) { // doesn't exist !
        throw new DataException(
            "Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
                + patStudyExtended.getToken());
      }

      // Get which family member we're processing
      String family = whichFamilyMember(database, patStudyExtended.getToken());
      if (family == null || family.isEmpty()) {
        // Skip the conssent questionnaire by writing an empty form to this questionnaire
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      }
      String familyAttribute = getFamilyConsentAttribute(family);
      if (submitStatus != null) {
        logger.debug("Checking if consented");
        checkIfConsent(database, patStudyExtended, "FamConsent2", familyAttribute, answerJson);
      } else {
        if (patStudy.getContents() == null) { // first question
          Patient patient = patStudyExtended.getPatient();
          if (patient != null && patient.attributeEquals(familyAttribute, attributeValue.Y.toString())) {
            // The family member has already consented so add the optional_questionnaires
              addConsentedQuestionnaires(database, patStudy);
          } else if (patient != null && (patient.attributeEquals(familyAttribute, attributeValue.N.toString()) ||
                     patient.attributeEquals(Constants.ATTRIBUTE_PARTICIPATES,"n"))) {
              logger.trace( "{} :  exists, but declined or set to not participating, stopping", familyAttribute);
              // Have declined so remove any other questionnaires
              deleteRemainingQuestionnaires(database, patStudy.getToken(), patStudy.getOrderNumber());
          }
        }
      }
    } else if (patStudyExtended.getStudyDescription() != null
        && patStudyExtended.getStudyDescription().startsWith("sendFamilyInitial") && answerJson != null) {
      FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
      for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
        String[] ids = fieldAnswer.getFieldId().split(":");
        if (ids.length == 3) {
          if (ids[2] != null && !ids[2].isEmpty()) {
            if ((ids[2].startsWith("Family")) && "1".equals(fieldAnswer.getChoice().get(0))) { // YES
              try {
                utils.createFamilySurvey(database, patStudyExtended.getPatient(), ids[2]);
              } catch (Exception ex) {
                return messageQuestion(patStudyExtended, ex.getMessage(), false);
              }
            }
          }
        }
      }
      if (formAnswer.getFieldAnswers().isEmpty()) {
        writeCompletedActivity(new ActivityDao(database, siteId), patStudyExtended);
        return messageQuestion(patStudyExtended, "Processing ...", true);
      }
    } else if (patStudyExtended.getStudyDescription() != null
        && patStudyExtended.getStudyDescription().toUpperCase().startsWith("FOLLOWUPINTRODUCTION")  && answerJson != null) {
      FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
      for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
        String[] ids = fieldAnswer.getFieldId().split(":");
        if (ids.length == 3 && ids[2] != null && ids[2].equalsIgnoreCase("CaregiverEmail")) {
          // Patient entered a caregiver email
          if (fieldAnswer.getChoice().get(0) != null && !fieldAnswer.getChoice().get(0).isEmpty()) {
            String newEmail = fieldAnswer.getChoice().get(0);
            try {
              if (!patStudyExtended.getPatient().hasAttribute("Family2")) {
                // Set the Family2 attribute with the new email address & send a family survey invitation
                setAttribute(database, patStudyExtended.getSurveySiteId(), patStudyExtended.getPatient(), FAMILY2_ATT, newEmail);
                utils.createFamilySurvey(database, patStudyExtended.getPatient(), FAMILY2_ATT);
              } else { // Otherwise, check they don't have a Family4 & that this email address isn't = their Family2 value
                String existingEmail = patStudyExtended.getPatient().getAttributeString(FAMILY2_ATT);
                if (existingEmail != null && !existingEmail.equalsIgnoreCase(newEmail) &&
                    !patStudyExtended.getPatient().hasAttribute(FAMILY4_ATT)) {
                  // Set the Family4 attribute with the new email address & send a family survey invitation
                  setAttribute(database, patStudyExtended.getSurveySiteId(), patStudyExtended.getPatient(), FAMILY4_ATT, newEmail);
                  utils.createFamilySurvey(database, patStudyExtended.getPatient(), FAMILY4_ATT);
                }
              }
            } catch (Exception ex) {
              logger.error("Not sending family invitation for patient " + patStudyExtended.getPatient().getPatientId() + " to address " + newEmail, ex);
            }
          }
        }
      }
    }
    return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
  }

  /*
    This checks if the response is to the consent question. If so set the attribute with the value of their response.
    If the response is yes add the optional_questionnaires
    And if "No" remove any remaining questionnaires
   */
  private void checkIfConsent(Database database, PatientStudyExtendedData patStudyExtended, String ref, String attributeName, String answerJson) {
    logger.trace("traumaConsent checking for consent question {}", ref);
    FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
    for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
      String[] ids = fieldAnswer.getFieldId().split(":");
      if (ids.length == 3) {
        if ("0".equals(ids[1])
            && ref.equals(ids[2])) { // Item 0, response 0
          if ("0".equals(fieldAnswer.getChoice().get(0))) { // YES
            setAttribute(database, patStudyExtended.getSurveySiteId(), patStudyExtended.getPatient(), attributeName, attributeValue.Y);

            addConsentedQuestionnaires(database, patStudyExtended);
          } else { // NO
            setAttribute(database, patStudyExtended.getSurveySiteId(), patStudyExtended.getPatient(), attributeName, attributeValue.N);
            deleteRemainingQuestionnaires(database, patStudyExtended.getToken(), patStudyExtended.getOrderNumber());
          }
        }
      }
    }
  }

  private void checkIfPatientEmail(Database database, PatientStudyExtendedData patStudyExtended, String answerJson) {
    FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
    for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
      String[] ids = fieldAnswer.getFieldId().split(":");
      if (ids.length == 3) {
        if (ids[2] != null && "PatientEMAIL".equals(ids[2])) {
          if (fieldAnswer.getChoice().get(0) != null && !fieldAnswer.getChoice().get(0).isEmpty()) {
            setAttribute(database, patStudyExtended.getSurveySiteId(), patStudyExtended.getPatient(), "surveyEmailAddressAlt", fieldAnswer.getChoice().get(0));
          }
        }
      }
    }
  }

  /*
   *  If this reponse was for any of the patients questions about family then save the response as an attribute
   */
  private void checkIfFamily(Database database, PatientStudyExtendedData patStudyExtended, String answerJson) {
    FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
    for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
      String[] ids = fieldAnswer.getFieldId().split(":");
      if (ids.length == 3) {
        if (ids[2] != null && !ids[2].isEmpty() && ids[2].startsWith("Family")) {
          if (fieldAnswer.getChoice().get(0) != null && !fieldAnswer.getChoice().get(0).isEmpty()) {
            setAttribute(database, patStudyExtended.getSurveySiteId(), patStudyExtended.getPatient(), ids[2], fieldAnswer.getChoice().get(0));
          }
        }
      }
    }
  }

  /*
   * This compares the email_address on the assessment to the email_addresses stored in the patients attributes
   * "Family2" and "Family4" and returns the name that matched.
   */
  private String whichFamilyMember(Database database, String token) {
    return database.get().toSelect("select ar.email_addr, pa.data_name, pa.data_value "
        + " from assessment_registration ar, survey_registration sr, patient_attribute pa "
        + " where sr.assessment_reg_id = ar.assessment_reg_id and sr.patient_id = pa.patient_id  "
        + " and pa.data_name in (?, ?) and sr.token=?").argString(FAMILY2_ATT).argString(FAMILY4_ATT)
        .argString(token).query(
            new RowsHandler<String>() {
              @Override
              public String process(Rows rs) throws Exception {
                while (rs.next()) {
                  String emailedTo = rs.getStringOrEmpty(1);
                  if (!emailedTo.isEmpty()) {
                    if (emailedTo.equals(rs.getStringOrEmpty(3))) {
                      return rs.getStringOrEmpty(2);
                    }
                  }
                }
                return "";
              }
            });
  }

  private String getFamilyConsentAttribute(String whichFamily) {
    return TRAUMA_CONSENT_ATTRIB + whichFamily;
  }

  private void setAttribute(Database database, Long siteId, Patient patient, String attributeName, Enum<?> dataValue) {
    logger.trace("setting attribute {} to {}", attributeName, dataValue);
    setAttribute(database, siteId, patient, attributeName, dataValue.toString());
    Token tok = new Token();
    ActivityDao activityDao = new ActivityDao(database, siteId);
    // If consent is null change participates to no
    if (dataValue == attributeValue.Y) {
      Activity pactivity = new Activity(patient.getPatientId(), Constants.ACTIVITY_CONSENTED, tok.getToken());
      activityDao.createActivity(pactivity);
    } else {
      setAttribute(database, siteId, patient, Constants.ATTRIBUTE_PARTICIPATES, "n");
      Activity pactivity = new Activity(patient.getPatientId(), Constants.ACTIVITY_DECLINED, tok.getToken());
      activityDao.createActivity(pactivity);
    }
  }

  private void setAttribute(Database database, Long siteId, Patient patient, String attributeName, String attributeValue) {
    if (attributeName == null || attributeName.isEmpty()) {
      logger.debug("no attributeName");
      return;
    }

    PatientAttribute pattribute = patient.getAttribute(attributeName);
    if (pattribute == null) {
      pattribute = new PatientAttribute(patient.getPatientId(), attributeName, attributeValue, PatientAttribute.STRING);
    } else {
      if (attributeValue.equals(pattribute.getDataValue())) {
        return;
      }
      pattribute.setDataValue(attributeValue);
    }
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    patAttribDao.insertAttribute(pattribute);
    patient.setAttribute(attributeName, attributeValue);
  }

  private void addConsentedQuestionnaires(Database database, PatientStudy patStudyExtended) {
    logger.trace("addConsentedQuestionnaires");
    AssessDao assessDao = new AssessDao(database, siteInfo);
    String surveyType = assessDao.getSurveyType(patStudyExtended.getToken());
    String consentedProcess = XMLFileUtils.getInstance(siteInfo).getAttribute(surveyType, "optional_questionnaires");
    ArrayList<Element> processList = XMLFileUtils.getInstance(siteInfo).getProcessQuestionaires(consentedProcess);
    if (processList != null) {
      for (Element questionaire : processList) {

        // Register the patient in each one
        String qType = questionaire.getAttribute("type");
        SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(siteInfo).getSurveyServiceImpl(qType);
        if (surveyService == null) {
          throw new ServiceUnavailableException("No service found for type: " + qType);
        }

        try {
          surveyService.registerAssessment(database, questionaire, patStudyExtended.getPatientId(),
              new Token(patStudyExtended.getToken()),
              ServerUtils.getAdminUser(database.get()));
        } catch (Exception ex) {
          logger.error("Error registering qtype: {}", qType, ex);
        }
      }
    }
  }

  private void deleteRemainingQuestionnaires(Database database, String token, int orderNumber) {
    database.toUpdate("DELETE FROM PATIENT_STUDY WHERE TOKEN = ? and order_number > ?")
        .argString(token).argInteger(orderNumber).update();
  }


  /**
   * Return the appropriate score provider based on the study name.
   */
  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    if (studyName.startsWith("TQoL") || studyName.startsWith("CentralityEvents")) {
      return new SumScoreProvider(dbp, siteInfo, studyName);
    }
    return super.getScoreProvider(dbp, studyName);
  }

  private NextQuestion messageQuestion(PatientStudyExtendedData patStudyExtended, String message, boolean terminal) {
    AutoBean<FormQuestion> bean = factory.formQuestion();
    NextQuestion nextQuestion = new NextQuestion();
    FormQuestion formQuestion = bean.as();
    formQuestion.setTitle1(message);
    formQuestion.setTerminal(terminal);
    nextQuestion.setQuestion(bean);
    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setQuestionType(QuestionType.form);
    displayStatus.setQuestionId("MSG");
    displayStatus.setSurveyProviderId(Integer.toString(patStudyExtended.getSurveySystemId()));
    displayStatus.setSurveySectionId(Integer.toString(patStudyExtended.getStudyCode()));
    nextQuestion.setDisplayStatus(displayStatus);
    return nextQuestion;
  }

  private void writeCompletedActivity(ActivityDao activityDao, PatientStudyExtendedData patStudyExtended) {
    activityDao.createActivity(new Activity(patStudyExtended.getPatientId(), Constants.ACTIVITY_COMPLETED, patStudyExtended.getToken()));
  }
}
