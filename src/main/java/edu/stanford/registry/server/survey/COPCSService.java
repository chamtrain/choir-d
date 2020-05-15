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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.SurveyMapRegions.MapGender;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class COPCSService extends QualifyQuestionService {

  private static final Logger logger = LoggerFactory.getLogger(COPCSService.class);
  //private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private final static String SURVEY_SYSTEM_NAME = "COPCSService";

  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  /**
   * As an implementor of SurveyServiceIntf, this will be cached and must not cache a database.
   */
  public COPCSService(String patientAttributeName, SiteInfo siteInfo) {
    super(patientAttributeName, siteInfo);
  }

  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    if (studyName.contains("@")) {
      int inx = studyName.indexOf("@");
      studyName = studyName.substring(0, inx);
    }
    switch (studyName) {
    case "COPCS":
    case "COPSCTD":
    case "COPCSLBP":
    case "COPCSHA":
    case "COPCSIBS":
    case "COPSCME":
      return new COPCSTDScoreProvider(dbp.get(), siteInfo, studyName);
    }
    return new RegistryShortFormScoreProvider(dbp.get(), siteInfo);
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
    logger.trace("first question for section " + patStudyExtended.getStudyDescription());

    // determine whether patients receive or skip the part of this survey being processed
    if ((patStudy.getContents() == null)  // This is the first question
        && (patStudyExtended.getStudyDescription().startsWith("COPSCTD"))
        && !qualifyForTD(database, patStudyExtended)) {
      patStudyExtended.setContents(emptyForm);
      patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
      return null;
    }
    if (patStudyExtended.getStudyDescription().startsWith("COPCSLBP")) {
      if (patStudy.getContents() == null) {  // This is the first question!
        if (!qualifyForLBP(database, patStudyExtended)) {
          patStudyExtended.setContents(emptyForm);
          patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
          return null;
        }
      } else if (submitStatus != null) {
        logger.debug("CLBP: submitStatus is not null");
        // On response to questions 1 and 2 if !cLBP  STOP!
        FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
        if (isQuestionResponse(formAnswer, "1:0:PainHowLong") &&
            getFieldChoice(formAnswer, "1:0:PainHowLong") < 2) {
          return stopQuestions(database, patStudyDao, patStudyExtended, submitStatus, answerJson);
        }
        if (isQuestionResponse(formAnswer, "2:0:PainFreq") &&
            getFieldChoice(formAnswer, "2:0:PainFreq") < 1) {
          return stopQuestions(database, patStudyDao, patStudyExtended, submitStatus, answerJson);
        }
      }
    }

    if (patStudyExtended.getStudyDescription().startsWith("COPCSHA")) {
      if ((patStudy.getContents() == null)  // This is the first question
          && !qualifyForHA(database, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      } else if (submitStatus != null) {
          /*
            If the number headaches in last 30 days < 15 or last 90 days was < 45 then !cLBP so STOP
           */
        logger.debug("SHA looking at responses");
        FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
        if (isQuestionResponse(formAnswer, "3:0:HACountDays30")) {
          if (getFieldChoice(formAnswer, "3:0:HACountDays30") < 15 &&
              (getSelection(database, patStudyExtended, "Order2", "2:0:HACountDays90") < 45)) {
            return stopQuestions(database, patStudyDao, patStudyExtended, submitStatus, answerJson);
          }
        }
      }
    }
    if (patStudyExtended.getStudyDescription().startsWith("COPCSIBS")) {
      if (patStudy.getContents() == null // This is the first question)
          && !qualifyForIBS(database, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      } else if (submitStatus != null) {
        FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
        // On response to question 1: Check if !frequency STOP!
        if (isQuestionResponse(formAnswer, "1:0:HowOftenPain") &&
            getFieldChoice(formAnswer, "1:0:HowOftenPain") < 4) {
          return stopQuestions(database, patStudyDao, patStudyExtended, submitStatus, answerJson);
        }
      }
    }
    if (patStudyExtended.getStudyDescription().startsWith("COPSCME")) {
      if (patStudy.getContents() == null  // This is the first question)
          && !qualifyForME(database, patStudyDao, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      } else if (submitStatus != null) {
        FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
        // On response to question 2: check if !duration STOP!
        if (isQuestionResponse(formAnswer, "2:0:FatCurrent6MDur")) {
          if (getFieldChoice(formAnswer, "2:0:FatCurrent6MDur") == 0 ||
              getSelection(database, patStudyExtended, "Order1", "1:0:FatigueEver6M") == 0) {
            return stopQuestions(database, patStudyDao, patStudyExtended, submitStatus, answerJson);
          }

        }

        // on response to question 4: Check  if !activityReduction STOP!
        if (isQuestionResponse(formAnswer, "4:0:FatModSev")) {
          if (getFieldChoice(formAnswer, "4:0:FatModSev") == 0 ||
              getSelection(database, patStudyExtended, "Order3", "3:0:FatInterfereLife") == 0) {
            return stopQuestions(database, patStudyDao, patStudyExtended, submitStatus, answerJson);
          }
        }
        // on response to question 5 if ! postExertionalMalaise STOP!
        if ((isQuestionResponse(formAnswer, "5:0:PostExerWorse") &&
            (getFieldChoice(formAnswer, "5:0:PostExerWorse") == 0)) ||
            (isQuestionResponse(formAnswer, "6:0:FatPostExertFreq") &&
                (getFieldChoice(formAnswer, "6:0:FatPostExertFreq") == 0))) {
          return stopQuestions(database, patStudyDao, patStudyExtended, submitStatus, answerJson);
        }
        // if response to question 7 or question 8 is 'No' STOP!
        if ((isQuestionResponse(formAnswer, "7:0:UnrefreshSleep") &&
            (getFieldChoice(formAnswer, "7:0:UnrefreshSleep") == 0)) ||
            (isQuestionResponse(formAnswer, "8:0:UnrefreshSleepFreq") &&
                (getFieldChoice(formAnswer, "8:0:UnrefreshSleepFreq") == 0))) {
          return stopQuestions(database, patStudyDao, patStudyExtended, submitStatus, answerJson);
        }
      }
    }
    return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
  }

  // Temporomandibular Disorder
  private Boolean qualifyForTD(Database database, PatientStudyExtendedData patientStudy) {
    // qualifies if selected any of the COPCS body map jaw areas: 3, 4, 41,42
    // on CHOIR maps (Male & Female) the areas are: 103, 104 , 203, 204
    SurveyMapRegions mapRegions = new SurveyMapRegions(database, siteInfo, patientStudy.getToken(), "bodymap");
    return mapRegions.wasRegionSelectedOnEitherMap("103") || mapRegions.wasRegionSelectedOnEitherMap("104") ||
        mapRegions.wasRegionSelectedOnEitherMap("203") || mapRegions.wasRegionSelectedOnEitherMap("204");
  }

  // chronic Low Back Pain
  private Boolean qualifyForLBP(Database database, PatientStudyExtendedData patientStudy) {
    // Person must click on one of the following COPCS body map lower back areas: 61,62,63,64,65,66
    // On CHOIR maps (Male & Female) those areas are: 218, 219, 222, 223, 224, 225
    SurveyMapRegions mapRegions = new SurveyMapRegions(database, siteInfo, patientStudy.getToken(), "bodymap");
    return (mapRegions.wasRegionSelectedOnEitherMap("218") || mapRegions.wasRegionSelectedOnEitherMap("219") ||
        mapRegions.wasRegionSelectedOnEitherMap("222") || mapRegions.wasRegionSelectedOnEitherMap("223") ||
        mapRegions.wasRegionSelectedOnEitherMap("224") || mapRegions.wasRegionSelectedOnEitherMap("225"));

  }

  // migraine/chronic tension-type HeadAche
  private Boolean qualifyForHA(Database database, PatientStudyExtendedData patientStudy) {
    // Person must click on one of the following COPCS body map areas: 1,2,3,4,39,40,41,42
    // These are areas: 101, 102, 103, 104, 201, 202, 203, 2014 on both Male and Female CHOIR body maps
    SurveyMapRegions mapRegions = new SurveyMapRegions(database, siteInfo, patientStudy.getToken(), "bodymap");
    return (mapRegions.wasRegionSelectedOnEitherMap("101") || mapRegions.wasRegionSelectedOnEitherMap("102") ||
        mapRegions.wasRegionSelectedOnEitherMap("103") || mapRegions.wasRegionSelectedOnEitherMap("104") ||
        mapRegions.wasRegionSelectedOnEitherMap("201") || mapRegions.wasRegionSelectedOnEitherMap("202") ||
        mapRegions.wasRegionSelectedOnEitherMap("203") || mapRegions.wasRegionSelectedOnEitherMap("204"));
  }

  // Irritable Bowel Syndrome
  // Person must click on one of these COPCS body map areas: 21,22,23,24 = CHOIR Male: 116, 117, Female: 112 113
  //                          or these COPCS body map areas: 26,27  = CHOIR both: 121 122
  //                          or these COPCS body map areas: 61,62,64,65 = CHOIR 218, 219, 223, 224
  // CHOIR equivalent maps areas are:
  private Boolean qualifyForIBS(Database database, PatientStudyExtendedData patientStudy) {
    SurveyMapRegions mapRegions = new SurveyMapRegions(database, siteInfo, patientStudy.getToken(), "bodymap");
    if (mapRegions.wasRegionSelectedOnGenderMap("116", MapGender.M) ||
        mapRegions.wasRegionSelectedOnGenderMap("117", MapGender.M) ||
        mapRegions.wasRegionSelectedOnGenderMap("112", MapGender.F) ||
        mapRegions.wasRegionSelectedOnGenderMap("113", MapGender.F)) {
      return true;
    }
    return (mapRegions.wasRegionSelectedOnEitherMap("121") || mapRegions.wasRegionSelectedOnEitherMap("122") ||
        mapRegions.wasRegionSelectedOnEitherMap("218") || mapRegions.wasRegionSelectedOnEitherMap("122") ||
        mapRegions.wasRegionSelectedOnEitherMap("223") || mapRegions.wasRegionSelectedOnEitherMap("224"));
  }

  // Myalgic Encephalomyelitis/ chronic fatigue syndrome
  private Boolean qualifyForME(Database database, PatStudyDao patStudyDao, PatientStudyExtendedData patientStudy) {
    // Find the COPCS screener
    ArrayList<PatientStudyExtendedData> patientStudyExtendedDataArrayList = patStudyDao.getPatientStudyExtendedDataByToken(patientStudy.getToken());
    PatientStudyExtendedData data = null;
    for (PatientStudyExtendedData patientStudyExtendedData : patientStudyExtendedDataArrayList) {
      if (patientStudyExtendedData.getStudyDescription().startsWith("COPCS@")) {
        data = patientStudyExtendedData;
      }
    }
    if (data != null) {
      // if response to COPCS question 0 is 1
      return getSelection(database, data, "Order0", "0:0:PersistentFatigue") == 1;
    }
    return false;
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


    for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
      if (fieldId != null && fieldId.equals(fieldAnswer.getFieldId())) {
        logger.debug("found field {} with choice ", fieldId, fieldAnswer.getChoice().get(0));
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


  private int getSelection(Database database, PatientStudyExtendedData registration, String question, String fieldId) {
    SurveyQuery surveyQuery = new SurveyQuery(database, new SurveyDao(database), siteInfo.getSiteId());
    Survey survey = surveyQuery.surveyBySurveyToken(registration.getToken());
    SurveyHelper helper = new SurveyHelper(siteInfo);
    String providerId = helper.getProviderId(database, getSurveySystemName());
    return helper.getSelect1Response(survey, providerId, String.valueOf(registration.getStudyCode()), question, fieldId);
  }


  @Override
  public String getSurveySystemName() {
    return SURVEY_SYSTEM_NAME;
  }

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    COPCSSystem system = COPCSSystem.getInstance(getSurveySystemName(), database);
    Study study = new Study(system.getSurveySystemId(), 0, name, 0);
    study.setTitle(title);
    study.setExplanation(explanation);
    SurveySystDao ssDao = new SurveySystDao(database);
    study = ssDao.insertStudy(study);
    return study;
  }

  @Override
  public SurveySystem getSurveySystem(Database database, String qType) {
    return COPCSSystem.getInstance(getSurveySystemName(), database);
  }

  @Override
  public void registerAssessment(Database database, Element questionaire, String patientId,
                                 Token tok, User user) throws ServiceUnavailableException {
    if (patientId == null) {
      return;
    }

    COPCSSystem system = COPCSSystem.getInstance(getSurveySystemName(), database);
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

  @SuppressWarnings("SameReturnValue")
  private NextQuestion stopQuestions(Database database, PatStudyDao patStudyDao, PatientStudyExtendedData patientStudyExtendedData,
                                     SubmitStatus submitStatus, String answerJson) {
    super.handleResponse(database, patientStudyExtendedData, submitStatus, answerJson); // record the answer
    PatientStudy patStudy = patStudyDao.getPatientStudy(patientStudyExtendedData, true); // get updated
    patStudyDao.setPatientStudyContents(patStudy, patStudy.getContents(), true);
    return null;
  }

  static private class SurveyHelper extends SurveyAdvanceBase {

    SurveyHelper(SiteInfo siteInfo) {
      super(siteInfo);
    }

    @Override
    public Integer getSelect1Response(Survey s, String provider, String section, String questionId, String fieldId) {
      logger.debug("looking for provider {}, section {}, questionId {}, fieldId {}", provider, section, questionId, fieldId);
      SurveyStep step = s.answeredStepByProviderSectionQuestion(provider, section, questionId);
      // if the step or step answer for this question id isn't found check if it's been stored within another question
      // This happens when conditional questions are shown on the same page as the prior question
      if (step != null) {
        Integer value = selectedFieldInt(step, fieldId);
        logger.debug("returning " + (value == null ? 0 : value));
        return value == null ? 0 : value;
      } else {
        logger.debug("returning a 0");
        return 0;
      }
    }
  }

  private class COPCSTDScoreProvider extends ExtensibleScoreProvider {

    COPCSTDScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
      String studyName = patientData.getStudyDescription();
      if (studyName.contains("@")) {
        int inx = studyName.indexOf("@");
        studyName = studyName.substring(0, inx);
      }
      switch (studyName) {
      case "COPSCTD": //Temporomandibular Disorder
        return getTDScore(patientData);
      case "COPCSLBP": // Chronic low back pain has no number score
      case "COPCSHA": // Migraine / chronic tension type headache has no number score
      case "COPCSIBS": // Irritable bowel syndrome has no number score
      case "COPSCME": // Myalgic Encephalomyselitis / Chronic Fatigue Syndrom has no number score
        break;
      }
      return new ArrayList<>();
    }

    @Override
    public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies,
                          PrintStudy printStudy, Patient patient) {
      Table table = new Table();
      table.setBorder(2);

      /* Find the last one */
      PatientStudyExtendedData patStudy = null;
      for (PatientStudyExtendedData patientStudy : patientStudies) {
        if (patientStudy.getStudyCode().intValue() == printStudy.getStudyCode().intValue()) {
          patStudy = patientStudy;
        }
      }
      if (printStudy == null || printStudy.getStudyDescription() == null || patStudy == null) {
        return table;
      }
      if (patStudy.getContents() == null || patStudy.getContents().length() < 100) {
        return table;
      }
      String studyDesc = printStudy.getStudyDescription().substring(0, printStudy.getStudyDescription().indexOf("@"));
      switch (studyDesc) {
      case ("COPSCTD"):
        // Temporomandibular Disorder
        ArrayList<ChartScore> scoreArrayList = getTDScore(patStudy);
        if (scoreArrayList.size() > 0) {
          ChartScore score = scoreArrayList.get(0);
          table.addRow(makeRow("TMD Screener Score", String.valueOf(score.getScore())));
          table.addRow(makeRow("TMD", score.getScore().intValue() >= 3));
        }
        break;

      case "COPCSLBP":
        // Chronic Low Back Pain
        table.addRow(makeRow("cLBP", cLBP(patStudy)));
        table.addRow(makeRow("Radicular Subtype", radicularSubtype(patStudy)));
        break;
      case "COPCSHA":
        // Migraine/ Chronic Tension-type Headache
        boolean frequency = headacheFrequency(patStudy);
        boolean symptoms = headacheSymptoms(patStudy);
        boolean medication = headacheMedications(patStudy);
        boolean migraine = frequency && (symptoms || medication);
        boolean tension = frequency && !symptoms && !medication;
        table.addRow(makeRow("Frequency", frequency));
        table.addRow(makeRow("Symptoms", symptoms));
        table.addRow(makeRow("Medication Use", medication));
        table.addRow(makeRow("Migraine", migraine));
        table.addRow(makeRow("Tension Headache", tension));
        break;
      case "COPCSIBS":
        // Irritable Bowel Syndrome
        boolean iFrequency = ibsFrequency(patStudy);
        boolean iPainBowel = ibsPainWithBowelMovement(patStudy);
        boolean iPainChgAp = ibsPainWithChgInAppearance(patStudy);
        boolean iPainChgFr = ibsPainWithChgInFrequency(patStudy);
        table.addRow(makeRow("Frequency", iFrequency));
        table.addRow(makeRow("Pain with bowel movement", iPainBowel));
        table.addRow(makeRow("Pain with change in appearance", iPainChgAp));
        table.addRow(makeRow("Pain with change in frequency", iPainChgFr));
        int checked = (iPainBowel ? 1 : 0) + (iPainChgAp ? 1 : 0) + (iPainChgFr ? 1 : 0);
        table.addRow(makeRow("IBS", iFrequency && checked >= 2));
        break;
      case "COPSCME":
        // Myalgic Encephalomyelitis/ Chronic Fatigue Syndrome
        boolean cfDuration = selectionEq1(patStudy, "Order1", "1:0:FatigueEver6M")
            && selectionGtEq(patStudy, "Order2", "2:0:FatCurrent6MDur", 1);
        boolean cfActivRed = selectionGtEq(patStudy, "Order3", "3:0:FatInterfereLife", 1) &&
            selectionGtEq(patStudy, "Order4", "4:0:FatModSev", 1);
        boolean cfPostExer = selectionEq1(patStudy, "Order5", "5:0:PostExerWorse") &&
            selectionGtEq(patStudy, "Order6", "6:0:FatPostExertFreq", 1);
        boolean cfUnrefSl = selectionEq1(patStudy, "Order7", "7:0:UnrefreshSleep") &&
            selectionGtEq(patStudy, "Order8", "8:0:UnrefreshSleepFreq", 1);
        boolean cfCogImpair = selectionEq1(patStudy, "Order9", "9:0:CogImpair") &&
            selectionGtEq(patStudy, "Order10", "10:0:CogImpairFreq", 1);
        boolean cfOrthoInt = selectionEq1(patStudy, "Order11", "11:0:OrthoFatigue") &&
            selectionGtEq(patStudy, "Order12", "12:0:OrthoFatigueFreq", 1);

        table.addRow(makeRow("Duration", cfDuration));
        table.addRow(makeRow("Activity Reduction", cfActivRed));
        table.addRow(makeRow("Post-Exertional Malaise", cfPostExer));
        table.addRow(makeRow("Unrefreshing Sleep", cfUnrefSl));
        table.addRow(makeRow("Cognitive Impairment", cfCogImpair));
        table.addRow(makeRow("Orthostatic Intolerance", cfOrthoInt));
        break;
      case "COPCS":
        int numberDiagnosis = 0;
        PatientStudyExtendedData data;
        try {
          // ALL
          data = getLastOne(patientStudies, "COPSCME");
          if (data != null) {
            boolean cfDur = selectionEq1(data, "Order1", "1:0:FatigueEver6M")
                && selectionGtEq(data, "Order2", "2:0:FatCurrent6MDur", 1);
            boolean cfActiv = selectionGtEq(data, "Order3", "3:0:FatInterfereLife", 1) &&
                selectionGtEq(data, "Order4", "4:0:FatModSev", 1);
            boolean cfPost = selectionEq1(data, "Order5", "5:0:PostExerWorse") &&
                selectionGtEq(data, "Order6", "6:0:FatPostExertFreq", 1);
            boolean cfUnref = selectionEq1(data, "Order7", "7:0:UnrefreshSleep") &&
                selectionGtEq(data, "Order8", "8:0:UnrefreshSleepFreq", 1);
            boolean cfCog = selectionEq1(data, "Order9", "9:0:CogImpair") &&
                selectionGtEq(data, "Order10", "10:0:CogImpairFreq", 1);
            boolean cfOrtho = selectionEq1(data, "Order11", "11:0:OrthoFatigue") &&
                selectionGtEq(data, "Order12", "12:0:OrthoFatigueFreq", 1);
            boolean mecfs = (cfDur && cfActiv && cfPost && cfUnref && (cfCog || cfOrtho));
            table.addRow(makeRow("ME/CFS", mecfs));
            if (mecfs) {
              numberDiagnosis++;
            }
          }
          //
          data = getLastOne(patientStudies, "COPSCTD");
          if (data != null) {
            ArrayList<ChartScore> scoreList = getTDScore(data);
            int scoreValue = 0;
            if (scoreList.size() > 0) {
              scoreValue = scoreList.get(0).getScore().intValue();
            }
            table.addRow(makeRow("TMD", scoreValue >= 3));
            if (scoreValue >= 3) {
              numberDiagnosis++;
            }
          }
          // Irritable Bowel syndrome
          data = getLastOne(patientStudies, "COPCSIBS");
          if (data != null) {
            boolean iFreq = ibsFrequency(data);
            boolean iBowel = ibsPainWithBowelMovement(data);
            boolean iChgAp = ibsPainWithChgInAppearance(data);
            boolean iChgFr = ibsPainWithChgInFrequency(data);
            int ichecked = (iBowel ? 1 : 0) + (iChgAp ? 1 : 0) + (iChgFr ? 1 : 0);
            table.addRow(makeRow("IBS", iFreq && ichecked >= 2));
            if (iFreq && ichecked >= 2) {
              numberDiagnosis++;
            }
          }
          // Lower back pain
          data = getLastOne(patientStudies, "COPCSLBP");
          if (data != null) {
            table.addRow(makeRow("cLBP", cLBP(data)));
            if (cLBP(data)) {
              numberDiagnosis++;
            }
          } else {
            table.addRow(makeRow("cLBP", false));
          }
          // Headache
          data = getLastOne(patientStudies, "COPCSHA");
          if (data != null) {
            boolean freq = headacheFrequency(data);
            boolean sympt = headacheSymptoms(data);
            boolean med = headacheMedications(data);
            boolean mi = freq && (sympt || med);
            boolean tn = freq && !sympt && !med;
            table.addRow(makeRow("Migraine", mi));
            table.addRow(makeRow("Tension Headache", tn));
            if (mi) {
              numberDiagnosis++;
            }
            if (tn) {
              numberDiagnosis++;
            }
          }
          table.addRow(makeRow("No Diagnoses", numberDiagnosis == 0));
          table.addRow(makeRow("1 Diagnoses", numberDiagnosis == 1));
          table.addRow(makeRow("2 or 3 Diagnoses", (numberDiagnosis == 2 || numberDiagnosis == 3)));
          table.addRow(makeRow("> 3 Diagnosis", numberDiagnosis > 3));
        } catch (Exception e) {
          logger.error("Error creating COPCS table", e);
          throw new DataException("Survey registration not found for surveyRegId");
        }
        break;
      }
      return table;
    }

    private TableRow makeRow(String col1, boolean checked) {
      return makeRow(col1, checked ? "X" : "");
    }

    private TableRow makeRow(String col1, String value) {
      TableRow qRow = new TableRow();
      qRow.addColumn(new TableColumn(col1, 65));
      qRow.addColumn(new TableColumn("", 1));
      if (value.length() == 0) {
        return qRow;
      }
      qRow.addColumn(new TableColumn(value, 10));
      return qRow;
    }

    private ArrayList<ChartScore> getTDScore(PatientStudyExtendedData patientData) {
      Survey s = getSurvey(patientData);
      SurveyHelper helper = new SurveyHelper(siteInfo);
      String providerId = helper.getProviderId(dbp.get(), SURVEY_SYSTEM_NAME);
      int intScore = helper.getSelect1Response(s, providerId, patientData.getStudyCode().toString(), "Order1",
          "1:0:PainJawTemple");
      intScore = intScore + helper.getSelect1Response(s, providerId, patientData.getStudyCode().toString(), "Order2",
          "2:0:JawPainAwakening");
      String[] fieldIds = { ":PainChewFood", ":PainOpeningMouth", ":PainClench", ":PainTalkKissYawn" };
      for (int r = 0; r < 4; r++) {
        intScore = intScore +
            (helper.getSelect1Response(s, providerId, patientData.getStudyCode().toString(), "Order3",
                "3:" + r + fieldIds[r]));
      }

      LocalScore localScore = new LocalScore(patientData.getDtChanged(), patientData.getPatientId(), patientData.getStudyCode()
          , patientData.getStudyDescription());
      localScore.setAnswer(3, new BigDecimal(intScore));
      ArrayList<ChartScore> scoreArray = new ArrayList<>();
      scoreArray.add(localScore);
      return scoreArray;
    }

    private boolean cLBP(PatientStudyExtendedData patientData) {
      Survey s = getSurvey(patientData);
      SurveyHelper helper = new SurveyHelper(siteInfo);
      String providerId = helper.getProviderId(dbp.get(), SURVEY_SYSTEM_NAME);
      int duration = helper.getSelect1Response(s, providerId, patientData.getStudyCode().toString(), "Order1", "1:0:PainHowLong");
      if (duration >= 2) {
        int frequency = helper.getSelect1Response(s, providerId, patientData.getStudyCode().toString(), "Order2", "2:0:PainFreq");
        return frequency >= 1;
      }
      return false;
    }

    private String radicularSubtype(PatientStudyExtendedData patientData) {
      if (getSelection(dbp.get(), patientData, "Order4", "4:0:ElectricShocks") == 1) {
        return "X";
      }
      return " ";
    }

    private boolean headacheFrequency(PatientStudyExtendedData patientData) {

      return selectionGtEq(patientData, "Order2", "2:0:HACountDays90", 45) ||
          selectionGtEq(patientData, "Order3", "3:0:HACountDays30", 15);
    }

    private boolean headacheSymptoms(PatientStudyExtendedData patientData) {
      if ((getSelection(dbp.get(), patientData, "Order5", "5:0:SoundSensitive") >= 2)
          && (getSelection(dbp.get(), patientData, "Order6", "6:0:PainModSev") >= 2)) {
        return true;
      }
      return (getSelection(dbp.get(), patientData, "Order6", "6:0:PainModSev") >= 2) &&
          (getSelection(dbp.get(), patientData, "Order10", "10:0:Nauseated") >= 2);
    }

    private boolean headacheMedications(PatientStudyExtendedData patientData) {
      return (getInputNumberQ13(dbp.get(), patientData, "13:0:OTC") >= 10) ||
          (getInputNumberQ13(dbp.get(), patientData, "13:1:Prescription") >= 10);
    }

    private boolean ibsFrequency(PatientStudyExtendedData patientData) {
      return selectionGtEq(patientData, "Order1", "1:0:HowOftenPain", 4);
    }

    private boolean ibsPainWithBowelMovement(PatientStudyExtendedData patientData) {
      return selectionGtEq(patientData, "Order2", "2:0:PainTimed2Bowel", 3);
    }

    private boolean ibsPainWithChgInAppearance(PatientStudyExtendedData patientData) {
      return selectionGtEq(patientData, "Order3", "3:0:FreqPainAppearance", 3);
    }

    private boolean ibsPainWithChgInFrequency(PatientStudyExtendedData patientData) {
      return selectionGtEq(patientData, "Order4", "4:0:Stools", 3);
    }

    private boolean selectionGtEq(PatientStudyExtendedData patientData, String question, String fieldId, int value) {
      logger.debug("Checking if question {} fieldId {} is gt or eq {}", question, fieldId, value);
      boolean ans = getSelection(dbp.get(), patientData, question, fieldId) >= value;
      logger.debug("Returning {}", ans);
      return ans;
    }

    private boolean selectionEq1(PatientStudyExtendedData patientData, String question, String fieldId) {
      logger.debug("Checking if question {} fieldId {} equals 1", question, fieldId);
      boolean ans = getSelection(dbp.get(), patientData, question, fieldId) >= 1;
      logger.debug("Returning {}", ans);
      return ans;
    }

    private int getInputNumberQ13(Database database, PatientStudyExtendedData patientData, String fieldId) {
      Survey s = getSurvey(patientData);
      SurveyHelper helper = new SurveyHelper(siteInfo);
      String providerId = helper.getProviderId(database, SURVEY_SYSTEM_NAME);
      String daysStr = helper.getInputStringResponse(s, providerId, patientData.getStudyCode().toString(), "Order13", fieldId);
      if (daysStr == null) {
        return 0;
      }
      try {
        return Integer.parseInt(daysStr);
      } catch (NumberFormatException nfe) {
        return 0;
      }
    }

    private PatientStudyExtendedData getLastOne(ArrayList<PatientStudyExtendedData> patientStudies, String studyName) {

      if (studyName == null) {
        return null;
      }
      PatientStudyExtendedData patStudy = null;
      for (PatientStudyExtendedData patientStudy : patientStudies) {
        if (patientStudy.getStudyDescription() != null && patientStudy.getStudyDescription().startsWith(studyName)) {
          patStudy = patientStudy;
        }
      }
      return patStudy;
    }

    Survey getSurvey(PatientStudyExtendedData patientData) {
      SurveyDao surveyDao = new SurveyDao(dbp.get());
      SurveyQuery query = new SurveyQuery(dbp.get(), surveyDao, patientData.getSurveySiteId());
      return query.surveyBySurveyToken(patientData.getToken());
    }


  }
}


class COPCSSystem extends SurveySystem {

  private static final long serialVersionUID = -1;

  @SuppressWarnings({ "StaticVariableOfConcreteClass", "StaticNonFinalField" })
  private static COPCSSystem me = null;

  private COPCSSystem(SurveySystem ssys) throws DataException {
    this.copyFrom(ssys);
  }

  public static COPCSSystem getInstance(String surveySystemName, Database database) throws DataException {
    if (me == null) {
      SurveySystDao ssDao = new SurveySystDao(database);
      SurveySystem ssys = ssDao.getOrCreateSurveySystem(surveySystemName, null);
      me = new COPCSSystem(ssys);
    }
    return me;
  }

}



