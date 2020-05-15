/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.orthohand;


import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.ExtensibleScoreProvider;
import edu.stanford.registry.server.survey.NextQuestion;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.RegistryAssessmentsService;
import edu.stanford.registry.server.survey.RegistryShortFormScoreProvider;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.survey.SurveyServiceIntf;
import edu.stanford.registry.server.utils.EmailTemplateUtils;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.StringUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.GlobalHealthScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.SessionStatus;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Created by tpacht on 9/11/2015 to handle conditional questions for the hand clinic.
 */

public class OrthoHandSurveyService extends RegistryAssessmentsService
        implements SurveyServiceIntf {

    public OrthoHandSurveyService(SiteInfo siteInfo) {
      super(siteInfo);
    }

    private static final Logger logger = LoggerFactory.getLogger(OrthoHandSurveyService.class);
    private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
    private enum attributeValue {Y, N}
    private final String ORTHO_HAND_CONSENT_ATTRIB="orthoHandConsent";
    private final static String emptyForm = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items></Items></Form>";

    @Override
    public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended,
                                       SubmitStatus submitStatus, String answerJson) {
        PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
        if (patStudyExtended.getStudyDescription() != null && patStudyExtended.getStudyDescription().equals("handConsent")) {
            PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
            if (patStudy == null) { // doesn't exist !
                throw new DataException(
                    "Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
                        + patStudyExtended.getToken());
            }
            if (submitStatus != null) {
                String study = patStudy.getContents();

                if (patStudy.getContents() == null || (patStudy.getContents().contains("Description=\"Research Consent\""))) { // This is the first question
                    logger.trace("handConsent first question answered");
                    FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
                    for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
                        String[] ids = fieldAnswer.getFieldId().split(":");
                        if (ids.length == 3) {
                            if ("1".equals(ids[0]) && "2".equals(ids[1])
                                && "ORTHOHANDCONSENT".equals(ids[2])) { // Item 1, response 2
                                if ("1".equals(fieldAnswer.getChoice().get(0))) { // YES
                                    setConsentAttribute(database, patStudyExtended.getSurveySiteId(), patStudyExtended.getPatient(), attributeValue.Y);
                                    addConsentedQuestionnaires(database, patStudyExtended);
                                }
                                if ("2".equals(fieldAnswer.getChoice().get(0))) { // NO
                                    setConsentAttribute(database, patStudyExtended.getSurveySiteId(), patStudyExtended.getPatient(), attributeValue.N);
                                }
                            }
                        }
                    }
                }
            }
            Patient patient = patStudyExtended.getPatient();
            if (patient != null && patient.getAttribute(ORTHO_HAND_CONSENT_ATTRIB) != null &&
                attributeValue.Y.toString().equals(patient.getAttribute(ORTHO_HAND_CONSENT_ATTRIB).getDataValue()) &&
                patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES) != null &&
                "y".equals(patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue())) {
                logger.trace("handConsent: already consented, skipping question");
                // Write empty form to the consent questionnaire, add the other questionnaires
                patStudy = patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
                addConsentedQuestionnaires(database, patStudy);
                // And move onto the next questionnaire
                return null;
            }
        } else if (patStudyExtended.getStudyDescription() != null && patStudyExtended.getStudyDescription().equals("handEmailConsent")) {
            PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
            if (patStudy == null) { // doesn't exist !
                throw new DataException(
                    "Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
                        + patStudyExtended.getToken());
            }
            if (submitStatus != null) {
                if (patStudy.getContents() == null) { // This is the first question
                    logger.trace("handEmailConsent answered");
                    FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
                    String emailAddress = null;
                    boolean sendForm = false;
                    for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
                        String[] ids = fieldAnswer.getFieldId().split(":");
                        if (ids.length == 3) {
                            if ("1".equals(ids[0]) && "1".equals(ids[1])
                                && "email".equals(ids[2])) { // Item 1, response 2
                                if (!fieldAnswer.getChoice().isEmpty()) {
                                    String stringValue = fieldAnswer.getChoice().get(0);
                                    if (stringValue != null && stringValue.length() > 0) {
                                        emailAddress = StringUtils.cleanString(stringValue);
                                    }
                                }
                            } else if ("1".equals(ids[0]) && "2".equals(ids[1]) && "sendForm".equals(ids[2])) {
                                if ("1".equals(fieldAnswer.getChoice().get(0))) { // YES, send the consent form
                                    sendForm = true;
                                }
                            }
                        }
                    }
                    if (emailAddress != null && !emailAddress.trim().equals("")) {
                        // validate the email address && send them the consent form
                        logger.trace("handEmailConsent validating email address");
                        if (!ServerUtils.getInstance().isValidEmail(emailAddress)) {
                            NextQuestion nextQuestion = super.handleResponse(database, patStudyExtended, null, null);
                            nextQuestion.getDisplayStatus().setServerValidationMessage("Please enter a valid email address");
                            nextQuestion.getDisplayStatus().setSessionStatus(SessionStatus.questionInvalid);
                            logger.trace("handEmailConsent returning message to please enter valid email");
                            return nextQuestion;
                        }
                        setAttribute(database, patStudyExtended.getSurveySiteId(), patStudyExtended.getPatient(), Constants.ATTRIBUTE_SURVEYEMAIL_ALT, emailAddress);
                        if (sendForm) {
                            sendForm(emailAddress);
                        }
                    }
                }
            }
            Patient patient = patStudyExtended.getPatient();
            if (patient != null && patient.getEmailAddress() != null) {
                logger.trace("handEmailConsent: we already have an email address, skipping question");
                // Write empty form to the email questionnaire
                patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
                // And move onto the next questionnaire
                return null;
            }
        } else if (patStudyExtended.getStudyDescription() != null && patStudyExtended.getStudyDescription().equals("leftHand")) {
            return handleHand(patStudyDao, patStudyExtended, "leftArm", submitStatus, answerJson);
        } else if (patStudyExtended.getStudyDescription() != null && patStudyExtended.getStudyDescription().equals("rightHand")) {
            return handleHand(patStudyDao, patStudyExtended, "rightArm", submitStatus, answerJson);
        } else if (patStudyExtended.getStudyDescription() != null && patStudyExtended.getStudyDescription().equals("upperExtremityIntro")) {
            return handleUpperExtremityIntro(patStudyDao, patStudyExtended, submitStatus, answerJson);
        } else if (patStudyExtended.getStudyDescription() != null && patStudyExtended.getStudyDescription().equals("handSeekTreat")) {
            return handleSeekTreat(patStudyDao, patStudyExtended, submitStatus, answerJson);
        }
        return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
    }

    private NextQuestion handleHand(PatStudyDao patStudyDao, PatientStudyExtendedData patStudyExtended, String studyName,
                   SubmitStatus submitStatus, String answerJson) {
        Database db = patStudyDao.database;
        if (submitStatus == null) {
            Double mapSelections = getMapSelections(patStudyDao, patStudyExtended.getSurveyRegId(), studyName);
            if (mapSelections > 0) { // show the hand map
                return super.handleResponse(db, patStudyExtended, submitStatus, answerJson);
            }

            // skipping hand
            logger.debug("No regions selected on {}, skipping hand", studyName);
            patStudyDao.setPatientStudyContents(patStudyExtended, emptyForm, true);
            return null;
        }
        return super.handleResponse(db, patStudyExtended, submitStatus, answerJson);
    }

    // Checks if a stand-alone survey
    private NextQuestion handleSeekTreat(PatStudyDao patStudyDao, PatientStudyExtendedData patStudyExtended,
                                         SubmitStatus submitStatus, String answerJson) {

        Database db = patStudyDao.database;
        AssessDao assessDao = new AssessDao(db, getSiteInfo());
        ApptRegistration registration = assessDao.getApptRegistrationBySurveyRegId(patStudyExtended.getSurveyRegId());
        if ("STA".equals(registration.getVisitType())) { // don't ask this question on stand-alone (no appt) surveys
            return null;
        }

        return super.handleResponse(db, patStudyExtended, submitStatus, answerJson);
    }

    private NextQuestion handleUpperExtremityIntro(PatStudyDao patStudyDao, PatientStudyExtendedData patStudyExtended,
                                        SubmitStatus submitStatus, String answerJson) {

        Database db = patStudyDao.database;
        Double leftHandSelections = getMapSelections(patStudyDao, patStudyExtended.getSurveyRegId(), "leftHand");
        if (leftHandSelections > 0) {
            Double rightHandSelections = getMapSelections(patStudyDao, patStudyExtended.getSurveyRegId(), "rightHand");
            if (rightHandSelections > 0) { // show the question
                return super.handleResponse(db, patStudyExtended, submitStatus, answerJson);
            }
        }

        // otherwise skip the intro question
        patStudyDao.setPatientStudyContents(patStudyExtended, emptyForm, true);
        return null;

    }

    private Double getMapSelections(PatStudyDao patStudyDao, Long regId, String studyName) {
        Double selections = 0.0;
        // Get the study
        ArrayList<PatientStudyExtendedData> studies = patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(regId, studyName);

        if (studies != null && studies.size() > 0) {
            PatientStudyExtendedData mapStudy = studies.get(0);
            mapStudy.setStudyDescription("bodymap"); // so score provider treats it as a bodymap
            ScoreProvider scoreProvider = new RegistryShortFormScoreProvider(patStudyDao.database, siteInfo);
            ArrayList<ChartScore> scores = scoreProvider.getScore(mapStudy);
            if (scores != null) {
                int indx = scores.size() - 1;
                if (scores.get(indx) instanceof LocalScore) {
                    LocalScore lscore = (LocalScore) scores.get(indx);
                    ArrayList<BigDecimal> answers = lscore.getAnswers();
                    for (BigDecimal answer : answers) {
                        selections = selections + answer.doubleValue();
                    }
                }
            }
        }
        return selections;
    }

    private void setConsentAttribute(Database database, Long siteId, Patient patient, Enum<?> dataValue) {

        setAttribute(database, siteId, patient, ORTHO_HAND_CONSENT_ATTRIB, dataValue.toString());
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

    private PatientAttribute setAttribute(Database database, Long siteId, Patient patient, String attributeName, String attributeValue) {

        PatientAttribute pattribute = patient.getAttribute(attributeName);
        if (pattribute == null) {
            pattribute = new PatientAttribute(patient.getPatientId(),attributeName, attributeValue, PatientAttribute.STRING);
        } else {
            if (attributeValue.equals(pattribute.getDataValue())) {
                return pattribute;
            }
            pattribute.setDataValue(attributeValue);
        }
        PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
        return patAttribDao.insertAttribute(pattribute);
    }

    private void addConsentedQuestionnaires(Database database, PatientStudy patStudyExtended) {
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

    private void sendForm(String emailAddr) {
        Mailer mailer = siteInfo.getMailer();
        EmailTemplateUtils emailUtils = new EmailTemplateUtils();

        String template = emailUtils.getTemplate(siteInfo, "Send-consent-form");
        String subject = emailUtils.getEmailSubject(template);
        String body = emailUtils.getEmailBody(template);

        List<File> attachments = new ArrayList<>();
        URL url = getClass().getClassLoader().getResource("shc/orthohand/ConsentForm.pdf");
        if (url == null ) {
            throw new RuntimeException("Did not find shc/orthohand/ConsentForm.pdf file");
        }
        attachments.add(new File(url.getFile()));

        try {
            mailer.sendTextWithAttachment(emailAddr, null, null, subject, body, attachments);
        } catch (Exception ex) {
            throw new RuntimeException("ERROR trying to send mail shc/orthohand/ConsentForm.pdf to " + emailAddr, ex);
        }
    }

    /**
     * Return the appropriate score provider based on the study name.
     */
    @Override
    public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
        if (studyName.startsWith("handGlobalHealth")) {
            return new GlobalHealthScoreProvider(dbp, siteInfo, studyName);
        }
        if (studyName.equals("prwhe")) {
            return new PrwheScoreProvider(dbp, siteInfo, studyName);
        }
        if (studyName.equals("handqDASH")) {
            return new QDashScoreProvider(dbp, siteInfo, studyName);
        }
        if (studyName.equalsIgnoreCase("handSane")) {
            return new SaneScoreProvider(dbp, siteInfo, studyName);
        }
        return new RegistryShortFormScoreProvider(dbp.get(), siteInfo);
    }


    /**
     * Score provider for handGlobalHealth.
     *
     * The handGlobalHealth score is calculated as the average of all the answered
     * questions. The average is in the range 0-4. This is multiplied by 25
     * to produce a range of 0-100. The final score is inverted to produce
     * a range of 100 (bad) to 0 (good).
     */
    static class GlobalHealthScoreProvider extends ExtensibleScoreProvider {

        public GlobalHealthScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
            super(dbp, siteInfo, studyName);
        }
        @Override
        public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {

            LocalScore globalScore = new GlobalHealthScore(patientData.getDtChanged(), patientData.getPatientId(),
                patientData.getStudyCode(), patientData.getStudyDescription());
            SurveyDao surveyDao = new SurveyDao(dbp.get());
            SurveyQuery query = new SurveyQuery(dbp.get(), surveyDao, patientData.getSurveySiteId());
            Survey s = query.surveyBySurveyToken(patientData.getToken());
            String localSurveyProvider = getProviderId(dbp.get(), "Local");
            SurveyStep step;
            for (int q=1; q<10; q++) {
                step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(dbp.get(), "handGlobalHealth"), "Order" + q);
                if (step != null) {
                    globalScore.setAnswer(q,new BigDecimal(selectedFieldInt(step, q + ":1")));
                }
            }
            step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(dbp.get(), "handGlobalHealth"), "10");
            globalScore.setAnswer(10, new BigDecimal(step.answerNumeric()));
            ArrayList<ChartScore> scoreArray = new ArrayList<>();
            scoreArray.add(globalScore);
            return scoreArray;
        }

        @Override
        public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> stats, PrintStudy study,
                                    ChartConfigurationOptions opts) {
            final TimeSeriesCollection dataset = new TimeSeriesCollection();
            dataset.addSeries(baseLineSeries);
            if (stats == null || study == null) {
                return dataset;
            }
            TimeSeries timeDataSetPhysic = new TimeSeries("Physical Health");
            TimeSeries timeDataSetMental = new TimeSeries("Mental Health");
            for (ChartScore stat1 : stats) {
                GlobalHealthScore score = (GlobalHealthScore) stat1;
                ArrayList<BigDecimal> answers = score.getAnswers();
                if (answers != null) {
                    Day day = new Day(score.getDate());
                    try {
                        double physicalScore = score.getPhysicalHealthTScore();
                        double mentalScore = score.getMentalHealthTScore();
                        if (study.getInvert()) {
                            if (physicalScore > 50) {
                                physicalScore = 50 - (physicalScore - 50);
                            } else if (physicalScore < 50) {
                                physicalScore = (50 + (50 - physicalScore));
                            }
                            if (mentalScore > 50) {
                                mentalScore = 50 - (mentalScore - 50);
                            } else if (mentalScore < 50) {
                                mentalScore = (50 + (50 - mentalScore));
                            }
                        }

//            If option to chart as percentiles convert
                        if (opts.getBooleanOption(ConfigurationOptions.OPTION_CHART_PERCENTILES)) {
                            logger.debug("charting (physical/mental) scores {} / {}" , physicalScore, mentalScore);
                            physicalScore = calculatePercentile(physicalScore);
                            mentalScore = calculatePercentile(mentalScore);
                            logger.debug("as (physical/mental) percentiles  {} / {}", physicalScore, mentalScore);
                        }
                        timeDataSetPhysic.addOrUpdate(day, physicalScore);
                        timeDataSetMental.addOrUpdate(day, mentalScore);
                    } catch (SeriesException duplicates) {
                      // ignore
                    }
                }
            }
            dataset.addSeries(timeDataSetPhysic);
            dataset.addSeries(timeDataSetMental);
            return dataset;
        }

        @Override
        public int getStudyIndex(String studyDescription) {
            if (studyDescription != null && "handGlobalHealth".equals(studyDescription)) {
                return GLOBAL_HEALTH;
            }
            return super.getStudyIndex(studyDescription);
        }
    }

    private static String getProviderId(Database db, String providerName) {
        return getInternalId(db,
                "SELECT survey_system_id FROM survey_system WHERE survey_system_name = ?", providerName);
    }

    private static String getSectionId(Database db, String sectionName) {
        return getInternalId(db,
                "SELECT study_code FROM study WHERE study_description = ?",sectionName);
    }
    private static String getInternalId(Database db, String sqlString, String name) {
        return db.toSelect(sqlString).argString(name).query(
            new RowsHandler<String>() {
                @Override
                public String process(Rows rs) {
                    if (rs.next()) {
                        return Integer.toString(rs.getIntegerOrNull());
                    }
                    return null;
                }
            });
    }

    private static int selectedFieldInt(SurveyStep step, String fieldId) {
        if (step != null && step.answer() != null && step.answer().form() != null ) {
            List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
            if (fields != null) {
                for (FormFieldAnswer field : fields) {
                    if (field != null && field.getFieldId() != null && step.answer().formFieldValue(field.getFieldId()) != null) {
                        if (field.getFieldId().equals(fieldId)) {
                            try {
                                return Integer.parseInt(step.answer().formFieldValue(field.getFieldId()));
                            } catch (NumberFormatException nfe) {
                                logger.warn("Unable to get choice from question {} with answer {} as an integer",
                                    step.questionJson(), step.answer().getAnswerJson());
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }
}
