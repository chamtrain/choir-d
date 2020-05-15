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
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class PainPsychMindfulnessService extends RegistryAssessmentsService {

  private final static Logger log = LoggerFactory.getLogger(edu.stanford.registry.server.survey.PainPsychMindfulnessService.class);
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private final static String ATTR_NAME = "mindfulness";
  private static final String surveySystemName = "PainPsychMindfulnessService";
  private final static String emptyForm = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items></Items></Form>";

  public PainPsychMindfulnessService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  static public boolean isMyService(String serviceName) {
    return serviceName.equalsIgnoreCase(surveySystemName);
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended,
                                     SubmitStatus submitStatus, String answerJson) {

    log.trace("Starting handleResponse for study " + patStudyExtended.getStudyDescription());
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    if ("mindfulnessConsent".equals(patStudyExtended.getStudyDescription())) {
      if (submitStatus != null) {
        FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
        for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
          String[] ids = fieldAnswer.getFieldId().split(":");
          if (ids.length == 3) {
            // Handle consent response
            if (ids[2].equals("MINDFULNESS_CONSENT")) {
              String choice = fieldAnswer.getChoice().get(0);
              if ("1".equals(choice)) {
                PatientAttribute pattribute;
                pattribute = new PatientAttribute(patStudyExtended.getPatientId(), ATTR_NAME, "Y", PatientAttribute.STRING);
                patAttribDao.insertAttribute(pattribute);
                addConsentedQuestionnaires(database, patStudyExtended);
              } else if ("2".equals(choice)) {
                PatientAttribute pattribute;
                pattribute = new PatientAttribute(patStudyExtended.getPatientId(), ATTR_NAME, "N", PatientAttribute.STRING);
                patAttribDao.insertAttribute(pattribute);
              }
            }
          }
        }
      } else {
        PatientAttribute patientAttribute = patStudyExtended.getPatient().getAttribute( ATTR_NAME);
        if (patientAttribute != null) {
          PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
          PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
          patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
          if ("Y".equalsIgnoreCase(patientAttribute.getDataValue())) {
            addConsentedQuestionnaires(database, patStudyExtended);
          }
        }
      }
    }
    return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
  }

  private void addConsentedQuestionnaires(Database database, PatientStudyExtendedData patStudyExtended) {

    int consentOrder = patStudyExtended.getOrderNumber() ;
    AssessDao assessDao = new AssessDao(database, siteInfo);
    SurveyRegistration registration = assessDao.getRegistration(patStudyExtended.getToken());
    XMLFileUtils xmlUtils = XMLFileUtils.getInstance(siteInfo);
    String consentedProcess = xmlUtils.getActiveProcessForName("mindfulness-consented", registration.getSurveyDt());
    ArrayList<Element> processList = xmlUtils.getProcessQuestionaires(consentedProcess);
    if (processList != null) {
      for (Element questionaire : processList) {

        // Register the patient in each one
        String questionType = questionaire.getAttribute("type");
        SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(siteInfo).getSurveyServiceImpl(questionType);
        if (surveyService == null) {
          throw new ServiceUnavailableException("No service found for type: " + questionType);
        }

        try {
          int questionnaireOrder = 1;
          try {
            questionnaireOrder = Integer.parseInt(questionaire.getAttribute("order"));
          } catch (NumberFormatException nfe) {
            log.error("Invalid Order attribute on questionnaire " + questionaire.getAttribute("value"));
          }
          questionaire.setAttribute("Order",  String.valueOf(consentOrder + questionnaireOrder));
          surveyService.registerAssessment(database, questionaire, patStudyExtended.getPatientId(),
              new Token(patStudyExtended.getToken()),
              ServerUtils.getAdminUser(database.get()));
        } catch (Exception ex) {
          log.error("Error registering questionType: " + questionType, ex);
        }
      }
    }
  }

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    Study study = new Study(getSurveySystem(database).getSurveySystemId(), 0, name, 0);
    study.setTitle(title);
    study.setExplanation(explanation);
    SurveySystDao ssDao = new SurveySystDao(database);
    study = ssDao.insertStudy(study);
    return study;
  }

  @Override
  public void registerAssessment(Database database, Element questionaire, String patientId,
                                 Token tok, User user) throws ServiceUnavailableException {

    // Get the study
    String studyName = questionaire.getAttribute("value");
    SurveySystDao ssDao = new SurveySystDao(database);
    Study study = ssDao.getStudy(getSurveySystem(database).getSurveySystemId(), studyName);

    // Add the study if it doesn't exist
    if (study == null) {
      study = registerAssessment(database, studyName, studyName, "");
    }

    // Get the patient and this study for this patient
    PatientDao patientDao = new PatientDao(database, siteId, user);
    Patient pat = patientDao.getPatient(patientId);
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    PatientStudy patStudy = patStudyDao.getPatientStudy(pat, study, tok);

    if (patStudy == null) { // not there yet so lets add it
      patStudy = new PatientStudy(siteId);
      patStudy.setExternalReferenceId("");
      patStudy.setMetaVersion(0);
      patStudy.setPatientId(pat.getPatientId());
      patStudy.setStudyCode(study.getStudyCode());
      patStudy.setSurveySystemId(study.getSurveySystemId());
      patStudy.setToken(tok.getToken());
      patStudy.setOrderNumber(Integer.valueOf(questionaire.getAttribute(Constants.XFORM_ORDER)));
      patStudyDao.insertPatientStudy(patStudy);
    }
  }

  private SurveySystem getSurveySystem(Database database ) {
      SurveySystDao ssDao = new SurveySystDao(database);
      return ssDao.getOrCreateSurveySystem(surveySystemName, null);
  }
}
