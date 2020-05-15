/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.PROMISScore;
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
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.xform.Select1Element;
import edu.stanford.registry.shared.xform.SelectItem;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class PainPsychologyService extends QualifyQuestionService {

  private static final Logger logger = LoggerFactory.getLogger(PainPsychologyService.class);
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private final static String PSYCH_ATTRIBUTE = "painPsychology";
  private final static String PSYRESOURCES_ATTRIBUTE = "psyResources";

  /**
   * As an implementor of SurveyServiceIntf, this will be cached and must not cache a database.
   */
  public PainPsychologyService(String patientAttributeName, SiteInfo siteInfo) {
    super(patientAttributeName, siteInfo);
  }

  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    return new PainPsychologyScoreProvider(dbp.get(), siteInfo);
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
                                     String answerJson) {
    if (patStudyExtended == null || patStudyExtended.getDtChanged() != null) { // missing data or survey is already done
      return null;
    }

    AssessDao assessDao = new AssessDao(database, siteInfo);
    ApptRegistration apptRegistration;
    // Get the appointments visit type
    try {
      apptRegistration = assessDao.getApptRegistrationBySurveyRegId(patStudyExtended.getSurveyRegId());
    } catch (Exception e) {
      logger.error(e.toString(), e);
      throw new DataException(e.getMessage());
    }
    if (apptRegistration == null) {
      throw new DataException("Appointment registration not found for surveyRegId");
    }

    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);

    if (!patientQualifies(database, assessDao, apptRegistration.getVisitType(), patStudyExtended.getToken(), patStudyExtended.getPatientId())) {
      patStudyExtended.setContents(emptyForm);
      patStudyDao.setPatientStudyContents(patStudyExtended, emptyForm, true);
      return null;
    }

    PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
    if (patStudy == null) { // doesn't exist !
      throw new DataException("Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
          + patStudyExtended.getToken());
    }
    if (patStudy.getContents() == null) { // This is the first question
      Patient patient = patStudyExtended.getPatient();
      if (patient == null) {
        throw new DataException("Patient not found for surveyRegId");
      }
      if (patStudyExtended.getStudyDescription() == null) {
        throw new DataException("Study " + patStudyExtended.getStudyCode() + " Has no description value!");
      }
      logger.trace("first question for section " + patStudyExtended.getStudyDescription());

      // determine whether patients receive or skip the part of this survey being processed
      if ((patStudyExtended.getStudyDescription().startsWith("pclc") || patStudyExtended.getStudyDescription().startsWith("PCL5"))
          && !qualifyForPcl(database, patStudyDao, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      }
      if (patStudyExtended.getStudyDescription().startsWith("alcoholCage") ||
          patStudyExtended.getStudyDescription().startsWith("drugCage")) {
        if (!qualifyForAlcoholorDrugQuestions(database, patStudyExtended)) {
          patStudyExtended.setContents(emptyForm);
          patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
          return null;
        }
      }
      if (patStudyExtended.getStudyDescription().startsWith("psyPatientResources") && !qualifyForPsyResources(patStudyExtended.getPatient())) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      }
      if (submitStatus != null &&
          (patStudyExtended.getStudyDescription().startsWith("PASS40") ||
           patStudyExtended.getStudyDescription().startsWith("CESD") ||
           patStudyExtended.getStudyDescription().startsWith("PSEQ"))) {
        String xml = XMLFileUtils.getInstance(siteInfo).getXML(database, patStudyExtended.getStudyDescription());
        patStudyExtended.setContents(xml);
        patStudyDao.setPatientStudyContents(patStudy, xml, false);
      }
    } else if (submitStatus != null && patStudyExtended.getStudyDescription().startsWith("psychCommonEnd")) {
      if (submitStatus.getQuestionType() == QuestionType.form) {
        FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
        logger.trace("Processing " + formAnswer.getFieldAnswers().size() + " answers ");
        for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
        /* The format of the fieldId is "ItemOrder:ResponseOrder:Ref" */
          String[] ids = fieldAnswer.getFieldId().split(":");
          if ("2".equals(ids[0])) {
            logger.trace("Processing fieldAnswer " + fieldAnswer.getFieldId() + " to item 2");
            setAttribute(database, patStudyExtended.getPatient());
            setPsyResources(database, patStudyDao, patStudyExtended);
          }
        }
      }
    }

    return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
  }

  private Boolean qualifyForPcl(Database database, PatStudyDao patStudyDao, PatientStudyExtendedData patientStudy) {
    try {
     if (answeredYes(database, patStudyDao, patientStudy, "ptsd")) {
       return true;
     }
     if (answeredYes(database, patStudyDao, patientStudy, "ptsdV2")) {
       return true;
     }

    } catch (DataException | InvalidDataElementException e) {
      logger.error("Caught exception checking qualifyForPcl", e);
    }
    return false;
  }

  private boolean answeredYes(Database database,  PatStudyDao patStudyDao, PatientStudyExtendedData patientStudy, String studyName)
      throws InvalidDataElementException {

    SurveySystDao ssDao = new SurveySystDao(database);
    SurveySystem localSurveySystem = ssDao.getSurveySystem("Local");

    RegistryShortFormScoreProvider surveyService = new RegistryShortFormScoreProvider(database, siteInfo);
    PrintStudy pStudy = new PrintStudy(siteInfo, ssDao.getStudy(localSurveySystem.getSurveySystemId(), studyName),
        localSurveySystem.getSurveySystemName());

    ArrayList<PatientStudyExtendedData> patientStudyExtendedDataArray = getStudySurveys(patStudyDao, patientStudy, studyName);
    for (PatientStudyExtendedData ptsdPatientStudyExt : patientStudyExtendedDataArray) {
      ArrayList<PatientStudyExtendedData> ptsdPatientStudies = new ArrayList<>();
      ptsdPatientStudies.add(ptsdPatientStudyExt);
      ArrayList<SurveyQuestionIntf> questions = surveyService.getSurvey(ptsdPatientStudies, pStudy, patientStudy.getPatient(), true);
      logger.trace(questions.size() + " questions returned for ptsd study");
      for (SurveyQuestionIntf question : questions) {
        if (question.getNumber() > 1) {
          ArrayList<SurveyAnswerIntf> ans = question.getAnswers(true);
          if (ans != null && ans.size() > 0) {
            SurveyAnswerIntf answer = ans.get(0);
            if (answer.getType() == Constants.TYPE_SELECT1) {
              if ("1".equals(getSelectedValue((Select1Element) answer))) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }
  private Boolean qualifyForAlcoholorDrugQuestions(Database database, PatientStudyExtendedData patientStudy) {
    // questions 1 or 8 qualify for alcohol
    try {
      SurveySystDao ssDao = new SurveySystDao(database);
      SurveySystem localSurveySystem = ssDao.getSurveySystem("Local");
      PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);

      RegistryShortFormScoreProvider surveyService = new RegistryShortFormScoreProvider(database, siteInfo);
      PrintStudy pStudy = new PrintStudy(siteInfo, ssDao.getStudy(localSurveySystem.getSurveySystemId(), "alcohol"),
          localSurveySystem.getSurveySystemName());
      ArrayList<PatientStudyExtendedData> patientStudyExtendedDataArray = getStudySurveys(patStudyDao, patientStudy, "alcohol");
      for (PatientStudyExtendedData ptsdPatientStudyExt : patientStudyExtendedDataArray) {
        ArrayList<PatientStudyExtendedData> ptsdPatientStudies = new ArrayList<>();
        ptsdPatientStudies.add(ptsdPatientStudyExt);
        ArrayList<SurveyQuestionIntf> questions = surveyService.getSurvey(ptsdPatientStudies, pStudy, patientStudy.getPatient(), true);
        logger.trace(questions.size() + " questions returned for " + patientStudy.getStudyDescription() + " study");
        for (SurveyQuestionIntf question : questions) {
          if (question.getNumber() == 1 || question.getNumber() == 6 || question.getNumber() == 7
              || question.getNumber() == 8) {
            ArrayList<SurveyAnswerIntf> ans = question.getAnswers(true);
            if (ans != null && ans.size() > 0) {
              SurveyAnswerIntf answer = ans.get(0);
              if (answer.getType() == Constants.TYPE_SELECT1) {
                if ("1".equals(getSelectedValue((Select1Element) answer))) {
                  if (question.getNumber() == 7 || question.getNumber() == 8) {
                    return true;
                  } else if ("alcoholCage".equals(patientStudy.getStudyDescription()) && (question.getNumber() == 1)) {
                    return true;
                  } else if ("drugCage".equals(patientStudy.getStudyDescription()) && (question.getNumber() == 6)) {
                    return true;
                  }
                }
              }
            }
          }
        }
      }
    } catch (DataException | InvalidDataElementException e) {
      logger.error("qualifyForAlcoholorDrugQuestions encountered error", e);
    }

    return false;
  }

  private void setAttribute(Database database, Patient patient) {
    PatientAttribute pattribute = new PatientAttribute(patient.getPatientId(), PSYCH_ATTRIBUTE, "Y",
        PatientAttribute.STRING);

    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    patAttribDao.insertAttribute(pattribute);
    patient.addAttribute(pattribute);
  }

  private boolean qualifyForPsyResources(Patient patient) {
    return (patient.getAttributeString(PSYRESOURCES_ATTRIBUTE, "").length() > 0);
  }

  private void setPsyResources(Database database, PatStudyDao patStudyDao, PatientStudyExtendedData patStudyExtData) {
    StringBuilder attributeValue = new StringBuilder();
    String token = patStudyExtData.getToken();

    // Get the scores for the measures this service handles
    ArrayList<PatientStudyExtendedData> studyExtendedDataList = patStudyDao.getPatientStudyExtendedDataByToken(token);

    int pcl5Score = getIntScore(getScore(getScoreProvider(database, "PCL5"), studyExtendedDataList, "PCL5"));
    int cesdScore = getIntScore(getScore(getScoreProvider(database, "CESD"), studyExtendedDataList, "CESD"));
    logger.trace("PCL5 score is {}, CES-D score is {}", pcl5Score, cesdScore);
    PromisScoreProvider promisScoreProvider = new PromisScoreProvider(siteInfo, 1);
    PROMISScore depressionScore = getPromisScore(promisScoreProvider, studyExtendedDataList, "PROMIS Depression Bank");

    // When PTSD Checklist (PCL5) is under 33 and CES-D is under 22 and PROMIS Depression is not SEVERE
    // The patient qualifies for both the "Free CBT for Chronic Pain Class" and the "Individual Pain Psychology Services"
    if ((pcl5Score >= 0 && pcl5Score < 33) && (cesdScore >= 0 && cesdScore < 22) && depressionScore != null &&
        !"Severe".equals(depressionScore.getCategoryLabel())) {
      attributeValue.append("ChronicPainClass,IPsyServices");
      PROMISScore mobilityScore = getPromisScore(promisScoreProvider, studyExtendedDataList, "PROMIS Bank v1.2 - Mobility");
      Long mobilityPercentile = calculatePercentile(mobilityScore, true); // Mobility is inverted
      PROMISScore interferenceScore = getPromisScore(promisScoreProvider, studyExtendedDataList, "PROMIS Bank v1.1 - Pain Interference");
      Long interferencePercentile = calculatePercentile(interferenceScore, false);

      // If they also have PROMIS Mobility and PROMIS Interference scores that are >= 84th percentile
      // They qualify for the "Back in ACTion class"
      if (mobilityPercentile >= 84L && interferencePercentile >= 84L) {
        appendToList(attributeValue, "ACTionClass");
      }

      PROMISScore sleepDistScore = getPromisScore(promisScoreProvider, studyExtendedDataList, "PROMIS Bank v1.0 - Sleep Disturbance");

      // And if their PROMIS Sleep Disturbance score is > 65
      // They quailify for both the "All about Sleep and Pain (ASAP) Class and "Stanford Sleep Medicine Center" resources
      if (sleepDistScore != null && sleepDistScore.getScore().doubleValue() >= 65.0) {
        appendToList(attributeValue, "ASAPClass");
        appendToList(attributeValue, "StanfordSleep");
      }
    }

    SurveyServiceFactory surveyServiceFactory = new SurveyServiceFactory(siteInfo);
    ScoreProvider localScoreProvider = surveyServiceFactory.getScoreProvider(database, "Local","painCatastrophizingScaleV2");
    ChartScore pcsScore = getScore(localScoreProvider, studyExtendedDataList, "painCatastrophizingScaleV2");
    if (pcsScore instanceof LocalScore) {
      LocalScore localPcsScore = (LocalScore) pcsScore;
      logger.trace("pain catastrophizing scale score is {}", localPcsScore.getScore());

      // If their Pain Catastrophizing Sore is > = 20 they qualify for the "Free One-Time Pain Psychology Class"
      if (localPcsScore.getScore().intValue() >= 20) {
        appendToList(attributeValue,"OTPsyClass");
      }
    }

    PROMISScore socialIsoScore = getPromisScore(promisScoreProvider, studyExtendedDataList, "PROMIS Bank v2.0 - Social Isolation");
    long socialIsoPercentile = calculatePercentile(socialIsoScore, false);

    // If the patients PROMIS Social Isolation score is >= the 84th percentile
    // They quailify for the "American Chronic Pain Association (ACPA) Suppoert Group"
    if (socialIsoPercentile > 84) {
      appendToList(attributeValue, "ACPAGroup");
    }

    // If the patients PTSD Checklist (PCL5) is >= 33 then the qualify for the
    // "Department of Psychiatry and Behavioral Sciences" resource
    if (pcl5Score >= 33 || cesdScore >= 22) {
      appendToList(attributeValue, "DeptPsyBS");
    }

    if (attributeValue.length() > 0) { // If patient qualified for any resources write the attribute
      Patient patient = patStudyExtData.getPatient();
      PatientAttribute pattribute = new PatientAttribute(patient.getPatientId(), "psyResources", attributeValue.toString(),
          PatientAttribute.STRING);
      PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
      patAttribDao.insertAttribute(pattribute);
      patient.addAttribute(pattribute);
    } else { // If they didn't qualify for any skip the psyPatientResources questionnaire
      logger.trace("Didn't qualify for psyResources, writing empty form");
      SurveySystDao ssDao = new SurveySystDao(database);
      Study psyPatientResourcesStudy = ssDao.getStudy(getSurveySystem(database, "").getSurveySystemId(), "psyPatientResources");
      PatientStudy psyPatientResources = patStudyDao.getPatientStudy(patStudyExtData.getPatient().getPatientId(),
          psyPatientResourcesStudy.getStudyCode(), patStudyExtData.getToken(), true);
      if (psyPatientResources != null) {
        psyPatientResources.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(psyPatientResources, emptyForm, true);
      }
    }
  }

  private ArrayList<PatientStudyExtendedData> getStudySurveys(
      PatStudyDao patStudyDao, PatientStudyExtendedData patientStudy, String studyDescription) {
    return patStudyDao.getPatientStudyExtendedDataByPatientAndStudy(patientStudy.getPatientId(), studyDescription);
  }

  private String getSelectedValue(Select1Element select1Element) {
    for (SelectItem item : select1Element.getItems()) {
      if (item.getSelected()) {
        return (item.getValue());
      }
    }
    return "";
  }

  @Override
  public String getSurveySystemName() {
    return "PainPsychologyService";
  }

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    PainPsychologySystem system = PainPsychologySystem.getInstance(getSurveySystemName(), database);
    Study study = new Study(system.getSurveySystemId(), 0, name, 0);
    study.setTitle(title);
    study.setExplanation(explanation);
    SurveySystDao ssDao = new SurveySystDao(database);
    study = ssDao.insertStudy(study);
    return study;
  }

  @Override
  public SurveySystem getSurveySystem(Database database, String qType) {
    return PainPsychologySystem.getInstance(getSurveySystemName(), database);
  }


  @Override
  public void registerAssessment(Database database, Element questionaire, String patientId,
                                 Token tok, User user) throws ServiceUnavailableException {
    if (patientId == null) {
      return;
    }

    // This is a one time survey so skip if they've already had it
    PatientDao patientDao = new PatientDao(database, siteId, user);
    if (patientDao.getAttribute(patientId, PSYCH_ATTRIBUTE) != null) {
      return;
    }

    // Get the visit type and only add this patient study for PSD appointments
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
      PainPsychologySystem system = PainPsychologySystem.getInstance(getSurveySystemName(), database);
      String qOrder = questionaire.getAttribute(Constants.XFORM_ORDER);
      Integer order = Integer.valueOf(qOrder);

      // Get the study
      String studyName = questionaire.getAttribute("value");
      SurveySystDao ssDao = new SurveySystDao(database);
      Study study = ssDao.getStudy(system.getSurveySystemId(), studyName);

      // Add the study if it doesn't exist
      if (study == null) {
        logger.debug("Study " + studyName + " doesn't exist, CREATING ");
        study = registerAssessment(database, studyName, "", "");
      }

      // Get the patient and this study for this patient
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
    boolean qualifies = (visitType != null && ("PSD".equals(visitType.toUpperCase()) ||
        "MDEP PSD".equals(visitType.toUpperCase()) || "DHP PSD".equals(visitType.toUpperCase())) );

    if (!qualifies) { // look for a PSD appointment during the blackout period before another survey would be given
      String lastSurveyDaysOutString = siteInfo.getProperty("appointment.lastsurvey.daysout");
      try {
        int lastSurveyDaysOutInt = Integer.parseInt(lastSurveyDaysOutString);
        SurveyRegistration registration = assessDao.getRegistration(token);
        Date toDate = DateUtils.getDaysFromDate(siteInfo, registration.getSurveyDt(), lastSurveyDaysOutInt);
        int numPSD = db.toSelect("select count(*) from appt_registration r, survey_registration sr where r.patient_id = ? "
            + "and r.visit_type in ('PSD', 'MDEP PSD', 'DHP PSD') and sr.survey_dt > ? and sr.survey_dt < ? and r.ASSESSMENT_REG_ID = sr.ASSESSMENT_REG_ID")
            .argString(patientId)
            .argDate(DateUtils.getTimestampStart(siteInfo, registration.getSurveyDt()))
            .argDate(DateUtils.getTimestampEnd(siteInfo, toDate)).query(rs -> {
              if (rs.next()) {
                return rs.getIntegerOrZero(1);
              }
              return 0;
            });
        if (numPSD > 0) {
          qualifies = true;
        }
      } catch (Exception e) {
        logger.error("Invalid value '" + lastSurveyDaysOutString + "'for the appointment.lastsurvey.daysout parameter");
      }
    }
    return qualifies;
  }

  private void appendToList(StringBuilder sb, String str) {
    if (sb.length() > 0) {
      sb.append(",");
    }
    sb.append(str);
  }

  private ChartScore getScore(ScoreProvider scoreProvider, ArrayList<PatientStudyExtendedData> studyExtendedDataList, String studyDescription) {
    PatientStudyExtendedData studyData = null;
    for (PatientStudyExtendedData patientStudyExtendedData : studyExtendedDataList) {
      if (patientStudyExtendedData.getStudyDescription().startsWith(studyDescription)) {
        studyData = patientStudyExtendedData;
      }
    }
    if (studyData != null && scoreProvider != null) {
      ArrayList<ChartScore> scores = scoreProvider.getScore(studyData);
      if (scores.size() > 0) {
        return scores.get(0);
      }
    }
    return null;
  }

  private int getIntScore(ChartScore score) {
    return score != null ? score.getScore().intValue() : -1;
  }

  private PROMISScore getPromisScore(ScoreProvider scoreProvider, ArrayList<PatientStudyExtendedData> studyExtendedDataList, String desc) {
    ChartScore chartScore = getScore(scoreProvider, studyExtendedDataList, desc);
    if (chartScore instanceof PROMISScore) {
      logger.debug("{} score is {} {}", desc, chartScore.getScore(), chartScore.getCategoryLabel());
      return (PROMISScore) chartScore;
    }
    return null;
  }

  private Long calculatePercentile(PROMISScore promisScore, boolean invert) {
    if (promisScore == null) {
      return 0L;
    }
    double score = promisScore.getScore().doubleValue();
    if (invert) {
      score = invertScore(score);
    }
    NormalDistribution norm = new NormalDistribution(50, 10);
    double percentile = norm.cumulativeProbability(score) * 100;
    logger.debug("PROMIS percentile for {} is {} ", promisScore.getStudyDescription(), Math.round(percentile));
    return Math.round(percentile);
  }

  private double invertScore(Double doubleScore) {
    long score = Math.round(doubleScore);
    if (score < 50) {
      score = 50 + (50 - score);
    } else if (score > 50) {
      score = 50 - (score - 50);
    }
    return score;
  }
}

class PainPsychologySystem extends SurveySystem {

  private static final long serialVersionUID = -4382364022282098050L;
  private static PainPsychologySystem me = null;

  private PainPsychologySystem(SurveySystem ssys) throws DataException {
    this.copyFrom(ssys);
  }

  public static PainPsychologySystem getInstance(String surveySystemName, Database database) throws DataException {
    if (me == null) {
      SurveySystDao ssDao = new SurveySystDao(database);
      SurveySystem ssys = ssDao.getOrCreateSurveySystem(surveySystemName, null);
      me = new PainPsychologySystem(ssys);
    }
    return me;
  }
}
