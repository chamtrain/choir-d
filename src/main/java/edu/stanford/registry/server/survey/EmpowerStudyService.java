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
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class EmpowerStudyService extends RegistryAssessmentsService {

  private final static Logger log = LoggerFactory.getLogger(EmpowerStudyService.class);
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  private static final String surveySystemName = "EmpowerStudyService";
  private final static String emptyForm = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items></Items></Form>";


  public EmpowerStudyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  static public boolean isMyService(String serviceName) {
    return serviceName.equalsIgnoreCase(surveySystemName);
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended,
                                     SubmitStatus submitStatus, String answerJson) {

    log.debug("handleResponse starting");
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
    if (patStudy == null || patStudy.getDtChanged() != null) { // doesn't exist or already answered
      log.debug("study is null, returning null");
        return null;
    }

    if (submitStatus != null) {
      log.debug("getting next question");
      NextQuestion nextQuestion = super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
      //patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);

      FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();

      if (isQuestionResponse(formAnswer, "1:1:learn_about_study")) { // Qualifying question
        if (getFieldChoice(formAnswer, "1:1:learn_about_study") == 2) {  // NO
          patStudyDao.setPatientStudyContents(patStudy, patStudy.getContents(), true);
          writeAttribute(database, patStudyExtended.getPatient(), "empower_learn" , "n" );
          return null;
        } else {
          writeAttribute(database, patStudyExtended.getPatient(), "empower_learn" , "y" );
        }
      } else if  (isQuestionResponse(formAnswer, "3:1:take_opioids_daily")) {
        if (getFieldChoice(formAnswer, "3:1:take_opioids_daily") == 2 ||
            getQ2Selection(database, patStudyExtended) == 2) { // Qualifying question
          patStudyDao.setPatientStudyContents(patStudy, patStudy.getContents(), true);
          return null;
        }
      } else if (isQuestionResponse(formAnswer, "4:1:interest_speak_dr" )) {
        int ans = getFieldChoice(formAnswer, "4:1:interest_speak_dr" );
        switch (ans) {
        case 1:
          writeAttribute(database, patStudyExtended.getPatient() , "empower_interest" , "y");
          break;
        case 2:
          writeAttribute(database, patStudyExtended.getPatient() , "empower_interest" , "n");
          break;
        default:
          break;
        }
      }
      return nextQuestion;
    } else if (skip(patStudyExtended.getPatient())) {
      log.debug("skipping");
      // if already answered yes or no to the last question of the survey,  or if asked within the last 3 months skip the survey
        patStudy = patStudyExtended;
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
    }

    log.debug("returning super handleresponse");
    return super.handleResponse(database, patStudyExtended , null , answerJson);
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
    log.debug("Anger: getFieldChoice fieldId {}", fieldId);
    for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
      if (fieldId != null && fieldId.equals(fieldAnswer.getFieldId())) {
        log.debug("found field {} with choice {}", fieldId, fieldAnswer.getChoice().get(0));
        try {
          return Integer.parseInt(fieldAnswer.getChoice().get(0));
        } catch (NumberFormatException nfe) {
          log.error("invalid number", nfe);
          return -1;
        }
      }
    }
    return -1;
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

  private boolean skip(Patient patient) {

    PatientAttribute empowerInterest =  patient.getAttribute("empower_interest");
    if (empowerInterest != null) {
      if ("y".equalsIgnoreCase(empowerInterest.getDataValue()) || "n".equalsIgnoreCase(empowerInterest.getDataValue()) ) {
        return true; // study was completed
      }
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.MONTH, -3);
    PatientAttribute empowerLearn = patient.getAttribute("empower_learn");
    if (empowerLearn == null) {
      return false; // study hasn't been asked yet
    }
    if (empowerLearn.getDtChanged() == null && empowerLearn.getDtCreated().after(calendar.getTime()) ) {
      return true; // study was recently asked
    }
    return empowerLearn.getDtChanged() != null && empowerLearn.getDtChanged().after(calendar.getTime());
  }

  private int getQ2Selection(Database database, PatientStudyExtendedData registration) {
    SurveyQuery surveyQuery = new SurveyQuery(database, new SurveyDao(database), siteInfo.getSiteId());
    Survey survey = surveyQuery.surveyBySurveyToken(registration.getToken());
    SurveyHelper helper = new SurveyHelper(siteInfo);
    String providerId = helper.getProviderId(database, surveySystemName);
    return helper.getSelect1Response(survey, providerId, String.valueOf(registration.getStudyCode()), "Order2", "2:1:exper_ongoing_pain");
  }

  private SurveySystem getSurveySystem(Database database ) {
      SurveySystDao ssDao = new SurveySystDao(database);
      return ssDao.getOrCreateSurveySystem(surveySystemName, null);
  }

  private void writeAttribute(Database database, Patient patient, String dataName, String value) {
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    PatientAttribute pattribute = patient.getAttribute(dataName);
    if (pattribute == null) {
      pattribute = new PatientAttribute(patient.getPatientId(), dataName, value);
    } else {
      pattribute.setDataValue(value);
    }
    patAttribDao.insertAttribute(pattribute);
  }
}
