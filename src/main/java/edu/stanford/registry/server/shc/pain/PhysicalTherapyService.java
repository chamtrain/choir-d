/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.pain;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.LEFSScoreProvider;
import edu.stanford.registry.server.survey.NDIScoreProvider;
import edu.stanford.registry.server.survey.NextQuestion;
import edu.stanford.registry.server.survey.PFIQScoreProvider;
import edu.stanford.registry.server.survey.PSFSScoreProvider;
import edu.stanford.registry.server.survey.QDashScoreProvider;
import edu.stanford.registry.server.survey.RMDQScoreProvider;
import edu.stanford.registry.server.survey.RegistryAssessmentsService;
import edu.stanford.registry.server.survey.RegistryShortFormScoreProvider;
import edu.stanford.registry.server.survey.SurveyMapRegions;
import edu.stanford.registry.server.survey.SurveyMapRegions.MapGender;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.DataException;
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
import edu.stanford.survey.client.api.SubmitStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;

public class PhysicalTherapyService extends RegistryAssessmentsService {

  private static final Logger logger = LoggerFactory.getLogger(PhysicalTherapyService.class);

  private static final String SERVICE_NAME = "PhysicalTherapyService";
  private static final String PT_ATTRIBUTE = "PTEval";
  public final static String[] surveys = {"CPAQShortForm", "LEFS", "NDI", "PAVS", "PFIQ", "PSEQ2", "PT1", "PT2",
      "PSFSv2", "RMDQ", "TSK11", "qDash", "bodymapPT"};
  private SurveySystem mySurveySystem;
  /**
   * Handles the questionnaires specific to the Physical Therepy Evaluation appointment
   * As an implementor of SurveyServiceIntf, this will be cached and must not cache a database.
   */
  public PhysicalTherapyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    if (studyName == null) {
      return null;
    }
    if (studyName.equals("bodymapPT") || studyName.equals("bodymapPTF")) {
      return new RegistryShortFormScoreProvider(dbp, siteInfo);
    }
    if ("qDash".equals(studyName)) {
      return new QDashScoreProvider(dbp, siteInfo, studyName);
    }
    if ("NDI".equals(studyName) || studyName.startsWith("NDI@")) {
      return new NDIScoreProvider(dbp, siteInfo, studyName);
    }
    if ("PFIQ".equals(studyName) || studyName.startsWith("PFIQ@")) {
      return new PFIQScoreProvider(dbp, siteInfo, studyName);
    }
    if (studyName.equals("PSFSv2") || studyName.startsWith("PSFSv2@") ||
        studyName.equals("PT1") || studyName.startsWith("PT1@")) {
      return new PSFSScoreProvider(dbp, siteInfo, studyName);
    }
    if (studyName.equals("RMDQ") || studyName.startsWith("RMDQ@")) {
      return new RMDQScoreProvider(dbp, siteInfo, studyName);
    }
    if (studyName.equals("LEFS") || studyName.startsWith("LEFS")) {
      return new LEFSScoreProvider(dbp, siteInfo, studyName);
    }
    return new PhysicalTherapyScoreProvider(dbp.get(), siteInfo);
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
                                     String answerJson) {
    if (patStudyExtended == null || patStudyExtended.getDtChanged() != null) { // missing data or survey is already done
      return null;
    }

    final String emptyForm = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items></Items></Form>";
    AssessDao assessDao = new AssessDao(database, siteInfo);

    // Check that the patient qualifies for the physical therapy evaluation
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);

    PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
    if (patStudy == null || patStudyExtended.getStudyDescription() == null) {
      return null;
    }

    if (patStudy.getContents() == null) { // This is the first question
      if (patStudyExtended.getStudyDescription().startsWith("bodymapPT")) {
        // If its the PT bodymap check that the patient qualifis or skip the PT module
        ApptRegistration apptRegistration;
        try {
          apptRegistration = assessDao.getApptRegistrationBySurveyRegId(patStudyExtended.getSurveyRegId());
        } catch (Exception e) {
          logger.error(e.toString(), e);
          throw new DataException(e.getMessage());
        }
        if (apptRegistration == null) {
          throw new DataException("Appointment registration not found for surveyRegId");
        }
        if (!patientQualifies(database, assessDao, apptRegistration.getVisitType(), patStudyExtended.getToken(), patStudyExtended.getPatientId())) {
          patStudyExtended.setContents(emptyForm);
          patStudyDao.setPatientStudyContents(patStudyExtended, emptyForm, true);
          return null;
        }
        // Add the other PT questionnaires for qualifying patients,
        if (!apptRegistration.getAssessmentType().startsWith("PTFollowUp")) { // It's the intake form so add the other questionnaires
          addPTEvalQuestionnaires(database, assessDao, patStudyExtended);
        }
      }

      /*
       Skip conditional questinnaires that the patient doesn't qualifies for
       */
      if ((patStudyExtended.getStudyDescription().toUpperCase().startsWith("NDI"))
         && !qualifiesForNDI(database, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      }

      if ((patStudyExtended.getStudyDescription().toUpperCase().startsWith("RMDQ"))
          && !qualifiesForRMDQ(database, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      }
      if ((patStudyExtended.getStudyDescription().toUpperCase().startsWith("PFIQ"))
        && !qualifiesForPFIQ(database, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      }
      if ((patStudyExtended.getStudyDescription().toUpperCase().startsWith("QDASH"))
          && !qualifiesForQDASH(database, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      }
      if ((patStudyExtended.getStudyDescription().toUpperCase().startsWith("LEFS"))
          && !qualifiesForLEFS(database, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      }
    }

    Patient patient = patStudyExtended.getPatient();
    if (patient == null) {
      throw new DataException("Patient not found for surveyRegId");
    }
    if (patStudyExtended.getStudyDescription() == null) {
      throw new DataException("Study " + patStudyExtended.getStudyCode() + " Has no description value!");
    }

    if (submitStatus != null && patStudyExtended.getStudyDescription().startsWith("PT2")) {
      if (!patient.hasAttribute(PT_ATTRIBUTE)) {
        setAttribute(database, patStudyExtended.getPatient());
      }
    }

    return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
  }

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    if (isPTQuestionnaire(name)) {
      Study study = new Study(getSurveySystem(database).getSurveySystemId(), 0, name, 0);
      study.setTitle(title);
      study.setExplanation(explanation);
      SurveySystDao ssDao = new SurveySystDao(database);
      study = ssDao.insertStudy(study);
      return study;
    } else {
      logger.error("Not creating study named {}. It is not a recognized Physical Therapy study name!", title);
      return null;
    }
  }

  @Override
  public void registerAssessment(Database database, Element questionaire, String patientId,
                                 Token tok, User user) throws ServiceUnavailableException {
    if (questionaire == null || patientId == null || tok == null) {
      return;
    }

    String studyName = questionaire.getAttribute("value");
    if (!isPTQuestionnaire(studyName)) {
      logger.error("Not registering questionnaire named {} for patient. It is not a recognized physical therapy study!",
          studyName);
      return;
    }

    // Get the visit type and only add this patient study for Physical Therapy (PTD and PTN) appointments
    String visitType = database.toSelect("select visit_type from appt_registration ar, survey_registration sr "
        + " where ar.SURVEY_SITE_ID = sr.survey_site_id and ar.assessment_reg_id = sr.assessment_reg_id "
        + " and sr.token = ?").argString(tok.getToken()).query(rs -> {
          if (rs.next()) {
            return rs.getStringOrNull(1);
          }
          return null;
        });

    AssessDao assessDao = new AssessDao(database, siteInfo);
    if (patientQualifies(database, assessDao, visitType, tok.getToken(), patientId)) {

      String qOrder = questionaire.getAttribute(Constants.XFORM_ORDER);
      Integer order = Integer.valueOf(qOrder);

      // Get the study
      SurveySystDao ssDao = new SurveySystDao(database);
      Study study = ssDao.getStudy(getSurveySystem(database).getSurveySystemId(), studyName);

      // Add the study if it doesn't exist
      if (study == null) {
        logger.debug("Study " + studyName + " doesn't exist, CREATING ");
        study = registerAssessment(database, studyName, "", "");
      }

      // Get the patient and this study for this patient
      PatientDao patientDao = new PatientDao(database, siteId, user);
      Patient pat = patientDao.getPatient(patientId);
      PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
      PatientStudy patStudy = patStudyDao.getPatientStudy(pat, study, tok);

      if (patStudy == null) { // not there yet so lets add it
        patStudy = new PatientStudy(this.siteId);
        patStudy.setExternalReferenceId("");
        patStudy.setMetaVersion(0);
        patStudy.setPatientId(pat.getPatientId());
        patStudy.setStudyCode(study.getStudyCode());
        patStudy.setSurveySystemId(study.getSurveySystemId());
        patStudy.setToken(tok.getToken());
        patStudy.setOrderNumber(order);
        patStudyDao.insertPatientStudy(patStudy);
      }
    }
  }

  private boolean patientQualifies(Database db, AssessDao assessDao, String visitType, String token, String patientId) {
    boolean qualifies = (visitType != null &&
        ("PTD".equals(visitType.toUpperCase()) || "PTN".equals(visitType.toUpperCase()) ||
         "MDEP PTN".equals(visitType.toUpperCase())));

    if (!qualifies) { // look for a PTD appointment during the blackout period before another survey would be given
      String lastSurveyDaysOutString = siteInfo.getProperty("appointment.lastsurvey.daysout");
      try {
        int lastSurveyDaysOutInt = Integer.parseInt(lastSurveyDaysOutString);
        SurveyRegistration registration = assessDao.getRegistration(token);
        Date toDate = DateUtils.getDaysFromDate(siteInfo, registration.getSurveyDt(), lastSurveyDaysOutInt);
        int numPTD = db.toSelect("select count(*) from appt_registration r, survey_registration sr where r.patient_id = ? "
            + "and (r.visit_type = 'PTD' or r.visit_type = 'PTN' or r.visit_type = 'MDEP PTN') and sr.survey_dt > ? and sr.survey_dt < ? "
            + "and r.ASSESSMENT_REG_ID = sr.ASSESSMENT_REG_ID")
            .argString(patientId)
            .argDate(DateUtils.getTimestampStart(siteInfo, registration.getSurveyDt()))
            .argDate(DateUtils.getTimestampEnd(siteInfo, toDate)).query(rs -> {
              if (rs.next()) {
                return rs.getIntegerOrZero(1);
              }
              return 0;
            });
        if (numPTD > 0) {
          qualifies = true;
        }
      } catch (Exception e) {
        logger.error("Invalid value '" + lastSurveyDaysOutString + "'for the appointment.lastsurvey.daysout parameter");
      }
    }
    return qualifies;
  }

  /*
    The following methods look at the body map regions the patient selected at the start of the survey
    to determine whether they qualifies for the  region specific questionnaire
   */
  private boolean qualifiesForNDI(Database database, PatientStudyExtendedData patientStudy) {
    // Neck Disabiliby index
    SurveyMapRegions mapRegions = new SurveyMapRegions(database, siteInfo, patientStudy.getToken(), "bodymapPTF");
    if (mapRegions.wasRegionSelectedOnEitherMap("105")) {
      return true;
    }
    if (mapRegions.wasRegionSelectedOnEitherMap("106")) {
      return true;
    }
    return
        mapRegions.wasRegionSelectedOnEitherMap("205") || mapRegions.wasRegionSelectedOnEitherMap("206");
  }

  private boolean qualifiesForRMDQ(Database database, PatientStudyExtendedData patientStudy) {
    // Roland Morris Disability Questionnaire:
    SurveyMapRegions mapRegions = new SurveyMapRegions(database, siteInfo, patientStudy.getToken(), "bodymapPTF");
    return mapRegions.wasRegionSelectedOnEitherMap("218") || mapRegions.wasRegionSelectedOnEitherMap("219") ||
        mapRegions.wasRegionSelectedOnEitherMap("223") || mapRegions.wasRegionSelectedOnEitherMap("224");
  }

  private boolean qualifiesForPFIQ(Database database, PatientStudyExtendedData patientStudy) {
    // Pelvic Floor Impact Questionnaire
    SurveyMapRegions mapRegions = new SurveyMapRegions(database, siteInfo, patientStudy.getToken(), "bodymapPTF");
    return mapRegions.wasRegionSelectedOnEitherMap("121") || mapRegions.wasRegionSelectedOnEitherMap("122");
        // mapRegions.wasRegionSelectedOnEitherMap("223") || mapRegions.wasRegionSelectedOnEitherMap("224"); -- we may want to include these too
  }

  private boolean qualifiesForQDASH(Database database, PatientStudyExtendedData patientStudy) {
    // Quick Dash Questionnaire
    SurveyMapRegions mapRegions = new SurveyMapRegions(database, siteInfo, patientStudy.getToken(), "bodymapPTF");
    return mapRegions.wasRegionSelectedOnEitherMap("107") || mapRegions.wasRegionSelectedOnEitherMap("110") ||
        mapRegions.wasRegionSelectedOnEitherMap("111") || mapRegions.wasRegionSelectedOnEitherMap("114") ||
        mapRegions.wasRegionSelectedOnEitherMap("115") || mapRegions.wasRegionSelectedOnEitherMap("118") ||
        mapRegions.wasRegionSelectedOnEitherMap("119") || mapRegions.wasRegionSelectedOnEitherMap("124") ||
        mapRegions.wasRegionSelectedOnEitherMap("125") || mapRegions.wasRegionSelectedOnEitherMap("128") ||
        mapRegions.wasRegionSelectedOnEitherMap("207") || mapRegions.wasRegionSelectedOnEitherMap("210") ||
        mapRegions.wasRegionSelectedOnEitherMap("211") || mapRegions.wasRegionSelectedOnEitherMap("214") ||
        mapRegions.wasRegionSelectedOnEitherMap("215") || mapRegions.wasRegionSelectedOnEitherMap("216") ||
        mapRegions.wasRegionSelectedOnEitherMap("217") || mapRegions.wasRegionSelectedOnEitherMap("220") ||
        mapRegions.wasRegionSelectedOnEitherMap("221") || mapRegions.wasRegionSelectedOnEitherMap("226") ||
        mapRegions.wasRegionSelectedOnEitherMap("227") || mapRegions.wasRegionSelectedOnEitherMap("230") ||
        mapRegions.wasRegionSelectedOnEitherMap("111") || mapRegions.wasRegionSelectedOnEitherMap("114") ||
        mapRegions.wasRegionSelectedOnGenderMap("112", MapGender.M) ||
        mapRegions.wasRegionSelectedOnGenderMap("113", MapGender.M) ||
        mapRegions.wasRegionSelectedOnGenderMap("116", MapGender.F) ||
        mapRegions.wasRegionSelectedOnGenderMap("117", MapGender.F) ;
  }

  private boolean qualifiesForLEFS(Database database, PatientStudyExtendedData patientStudy) {
    // Lower Extremity Functional Scale:
    SurveyMapRegions mapRegions = new SurveyMapRegions(database, siteInfo, patientStudy.getToken(), "bodymapPTF");
    return mapRegions.wasRegionSelectedOnEitherMap("120") || mapRegions.wasRegionSelectedOnEitherMap("123") ||
        mapRegions.wasRegionSelectedOnEitherMap("126") || mapRegions.wasRegionSelectedOnEitherMap("127") ||
        mapRegions.wasRegionSelectedOnEitherMap("129") || mapRegions.wasRegionSelectedOnEitherMap("130") ||
        mapRegions.wasRegionSelectedOnEitherMap("131") || mapRegions.wasRegionSelectedOnEitherMap("132") ||
        mapRegions.wasRegionSelectedOnEitherMap("133") || mapRegions.wasRegionSelectedOnEitherMap("134") ||
        mapRegions.wasRegionSelectedOnEitherMap("135") || mapRegions.wasRegionSelectedOnEitherMap("136") ||
        mapRegions.wasRegionSelectedOnEitherMap("222") || mapRegions.wasRegionSelectedOnEitherMap("225") ||
        mapRegions.wasRegionSelectedOnEitherMap("228") || mapRegions.wasRegionSelectedOnEitherMap("229") ||
        mapRegions.wasRegionSelectedOnEitherMap("231") || mapRegions.wasRegionSelectedOnEitherMap("232") ||
        mapRegions.wasRegionSelectedOnEitherMap("233") || mapRegions.wasRegionSelectedOnEitherMap("234") ||
        mapRegions.wasRegionSelectedOnEitherMap("235") || mapRegions.wasRegionSelectedOnEitherMap("236") ||
        mapRegions.wasRegionSelectedOnEitherMap("237") || mapRegions.wasRegionSelectedOnEitherMap("238");
  }

  private void addPTEvalQuestionnaires(Database database, AssessDao assessDao, PatientStudyExtendedData patStudyExtended) {
    SurveyRegistration registration = assessDao.getRegistration(patStudyExtended.getToken());
    XMLFileUtils xmlUtils = XMLFileUtils.getInstance(siteInfo);
    String consentedProcess = xmlUtils.getActiveProcessForName("physicalTherapyIntake", registration.getSurveyDt());
    ArrayList<Element> processList = xmlUtils.getProcessQuestionaires(consentedProcess);
    if (processList != null) {
      for (Element questionaire : processList) {
        // Register the patient in each one
        String questionType = questionaire.getAttribute("type");
        try {
          int questionnaireOrder = patStudyExtended.getOrderNumber();
          try {
            questionnaireOrder = Integer.parseInt(questionaire.getAttribute("order"));
          } catch (NumberFormatException nfe) {
            logger.error("Invalid Order attribute on questionnaire " + questionaire.getAttribute("value"));
          }
          questionaire.setAttribute("Order",  String.valueOf(questionnaireOrder));
          registerAssessment(database, questionaire, patStudyExtended.getPatientId(),
              new Token(patStudyExtended.getToken()), ServerUtils.getAdminUser(database.get()));
        } catch (Exception ex) {
          logger.error("Error registering questionType: " + questionType, ex);
        }
      }
    }
  }

  private SurveySystem getSurveySystem(Database database) {
    if (mySurveySystem == null) {
      SurveySystDao ssDao = new SurveySystDao(database);
      mySurveySystem = ssDao.getOrCreateSurveySystem(SERVICE_NAME, null);
    }
    return mySurveySystem;
  }

  private void setAttribute(Database database, Patient patient) {
    PatientAttribute pattribute = new PatientAttribute(patient.getPatientId(), PT_ATTRIBUTE, "Y",
        PatientAttribute.STRING);
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    patAttribDao.insertAttribute(pattribute);
    patient.addAttribute(pattribute);
  }

  private boolean isPTQuestionnaire(String studyName) {
    if (studyName != null) {
      for (String surveyName : surveys) {
        if (surveyName.equals(studyName) || studyName.startsWith(surveyName + "@")) {
          return true;
        }
      }
    }
    return false;
  }

}
