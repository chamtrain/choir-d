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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.utils.PROMISAssessmentUtils;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.PromisQuestion;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseException;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class PromisSurveyService extends SurveySiteBase implements SurveyServiceIntf {

  private static final Logger logger = LoggerFactory.getLogger(PromisSurveyService.class);
  private int version = 1;
  private SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  public PromisSurveyService(SiteInfo siteInfo){
    super(siteInfo);
  }

  private String registerPromisAssessment(Database database, Element questionaire, String token)
      throws ServiceUnavailableException, FileNotFoundException {
    PROMISAssessmentUtils promisUtils = new PROMISAssessmentUtils(database, version, siteInfo);
    String result;
    try {
      result = promisUtils.registerAssessment(version, questionaire, token);
    } catch (UnsupportedEncodingException e) {
      throw new ServiceUnavailableException(e);
    }
    logger.trace("results={}", result);
    return result;
  }

  private PatientStudy getPatientStudyInProgress(final Database database, final PatientStudy patStudy) {
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    return patStudyDao.getPatientStudy(patStudy.getPatientId(), patStudy.getStudyCode(), patStudy.getToken(), false);
  }

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    if (name == null)
      return null;

    if (title == null)
      title = name;
    if (explanation == null)
      explanation = "";
    PROMISAssessmentUtils promisUtils = new PROMISAssessmentUtils(database, version, siteInfo);
    Study study = new Study(promisUtils.getSystemId(), 0, name, 0);
    study.setTitle(title);
    study.setExplanation(explanation);
    SurveySystDao ssDao = new SurveySystDao(database);
    study = ssDao.insertStudy(study);
    return study;
  }

  private PatientStudy registerAssessment(Database database, Element questionaire, String patientId, Token tok,
                                          Integer order) throws ServiceUnavailableException, FileNotFoundException {
    String studyName = questionaire.getAttribute("value");
    PatientStudy patStudy = new PatientStudy(this.siteId);
    PROMISAssessmentUtils promisUtils = null;
    int systemId = 0;
    try {
      promisUtils = new PROMISAssessmentUtils(database, version, siteInfo);
      systemId = promisUtils.getSystemId();
    } catch (Exception e) {
      logger.error("ERROR getting promisId: {}", e.getMessage(), e);
      throw new IllegalArgumentException("Failed to initialize promis survey system");
    }
    // lets make sure the assessment is in the study table
    // and if not we'll add it
    SurveySystDao ssDao = new SurveySystDao(database);
    Study study = ssDao.getStudy(systemId, studyName);
    if (study == null) {
      logger.debug("Did not find study '{}' adding", studyName );

      study = registerAssessment(database, studyName, studyName, null);
    }

    // make the call out to register the study for this patient
    String xmlDocumentString = registerPromisAssessment(database, questionaire, tok.getToken());
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    // Using factory get an instance of document builder
    DocumentBuilder db;

    try {
      db = dbf.newDocumentBuilder();

      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));

      Document messageDom = db.parse(is);

      // get the assessment name
      switch (version) {
      case 2:
        /*
         * Example doc returned in version 2: <?xml version="1.0" encoding="utf-16"?> <Assessment OID="7584099e-ce4f-4d26-bb65-3e07534b0b13" ID=""
         * Expiration="10/21/2012 12:13:26 PM" />
         */
        Element docElement = messageDom.getDocumentElement();
        if (docElement.getTagName().equals("Assessment")) { // we're good
          String refId = docElement.getAttribute("OID");
          patStudy.setExternalReferenceId(refId);
        }
        break;
      default: // version 1
        /*
         * Example doc returned in version 1: <?xml version="1.0" encoding="utf-8" ?> <Assessments> <assessment name="67b6022e-2bf1-4f1c-a13e-fe882847e44c"
         * value="67b6022e-2bf1-4f1c-a13e-fe882847e44c" form="PROMIS Depression Bank" /> </Assessments>
         */
        if (messageDom.getDocumentElement().getTagName().equals("Assessments")) {
          NodeList assessments = messageDom.getElementsByTagName("assessment");
          Element assessment = (Element) assessments.item(0);
          String refId = assessment.getAttribute("name");
          patStudy.setExternalReferenceId(refId);
        } else {
          logger.error("Promis returned a document with DocumentElement tagname that isn't Assessments its {}",
              messageDom.getDocumentElement().getTagName());
        }
        break;
      }

      patStudy.setMetaVersion(0);
      try {
        patStudy.setPatientId(patientId);
      } catch (NumberFormatException nfe) {
        patStudy.setPatientId("1000");
      }
      patStudy.setStudyCode(study.getStudyCode());
      patStudy.setSurveySystemId(study.getSurveySystemId());
      patStudy.setToken(tok.getToken());
      patStudy.setOrderNumber(order);

      // see if it already exists and isn't complete yet
      PatientStudy patientfound = getPatientStudyInProgress(database, patStudy);

      if (patientfound == null) {
        logger.trace("patient registration not found writing to db");
        PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
        patStudy = patStudyDao.insertPatientStudy(patStudy);
      } else {
        patStudy = patientfound;
      }
    } catch (ParserConfigurationException | DatabaseException | IOException | SAXException e) {
      logger.error(e.getMessage(), e);
      throw new ServiceUnavailableException(e.getMessage());
    }
    return patStudy;

  }

  @Override
  public void registerAssessment(Database database, Element questionaire, String patientId,
                                 Token tok, User user) throws ServiceUnavailableException {

    String studyName = questionaire.getAttribute("value");
    String qOrder = questionaire.getAttribute("order");
    Integer order = Integer.valueOf(qOrder);
    try {
      PatientStudy patStudy = registerAssessment(database, questionaire, patientId, tok, order);
      PatientStudyExtendedData patStudyE = new PatientStudyExtendedData(patStudy);
      PatientDao patientDao = new PatientDao(database, siteId, user);
      Patient pat = patientDao.getPatient(patStudy.getPatientId());
      if (pat != null) {
        patStudyE.setPatient(pat);
      }
      SurveySystDao ssDao = new SurveySystDao(database);
      Study study = ssDao.getStudy(patStudy.getSurveySystemId(), patStudy.getStudyCode());
      patStudyE.setStudy(study);
      patStudyE.setSurveySystemName(PROMISAssessmentUtils.SURVEY_SYSTEM_NAME[version]);
      //return patStudyE;
    } catch (FileNotFoundException fnf) {
      logger.error("Failed to register assessment {} for patient", studyName);
      throw new ServiceUnavailableException("Failed to register assessment " + studyName + " for patient " + patientId);
    }
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudy, SubmitStatus submitStatus,
                                     String answerJson) {

    FormFieldAnswer answer = null;
    if (answerJson != null) {
      FormAnswer answers = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();

      if (answers != null && answers.getFieldAnswers() != null && answers.getFieldAnswers().size() > 0) {
        answer = answers.getFieldAnswers().get(0);
      }
    }
    if (answer != null && answer.getChoice() != null) {
      List<String> choices = answer.getChoice();
      for (String choice : choices) {
        if (choice != null) {
          String[] parts = choice.split("[|]");
          if (parts.length == 2) {
            PromisQuestion question = new PromisQuestion();
            question.setId(submitStatus.getQuestionId());
            return toNextQuestion(patStudy, handleQuestion(database, patStudy, question, parts[0], parts[1]));
          }
        }
      }
    }
    return toNextQuestion(patStudy, handleQuestion(database, patStudy, null, null, null));
  }

  private NextQuestion toNextQuestion(PatientStudyExtendedData surveyData, ArrayList<SurveyQuestionIntf> questionIntfs) {

    if (questionIntfs == null || questionIntfs.size() < 1) {
      return null;
    }

    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setQuestionType(QuestionType.form);
    displayStatus.setSurveyProviderId(Integer.toString(surveyData.getSurveySystemId()));
    displayStatus.setSurveySectionId(Integer.toString(surveyData.getStudyCode()));

    AutoBean<FormQuestion> bean = factory.formQuestion();
    FormQuestion question = bean.as();

    //unused: String fieldIdPrefix = surveyData.getSurveySystemId() + "-" + surveyData.getStudyCode() + "-";

    for (SurveyQuestionIntf questionIntf : questionIntfs) {

      int nbrText = questionIntf.getText().size();
      if (nbrText > 0) {
        question.setTitle1(questionIntf.getText().get(0));
      }
      if (nbrText > 1) {
        question.setTitle2(questionIntf.getText().get(1));
      }
      ArrayList<FormField> fields = new ArrayList<>();
      FormField field = factory.field().as();
      field.setType(FieldType.radios);
      field.setFieldId(questionIntf.getId());
      field.setRequired(true);

      for (SurveyAnswerIntf answerIntf : questionIntf.getAnswers()) {
        if (answerIntf.getType() == Constants.TYPE_RADIO) {

          if (field.getValues() == null) {
            field.setValues(new ArrayList<FormFieldValue>());
          }
          FormFieldValue value = factory.value().as();
          value.setId(answerIntf.getClientId());
          value.setLabel(answerIntf.getText().get(0));
          field.getValues().add(value);

          for (int i = 1; i < answerIntf.getText().size(); i++) {
            value.setLabel(value.getLabel() + "<BR>" + answerIntf.getText().get(i));
          }
        }

      }
      fields.add(field);
      question.setFields(fields);
    }

    NextQuestion next = new NextQuestion();
    next.setDisplayStatus(displayStatus);
    next.setQuestion(bean);
    return next;
  }

  public String administerAssessment(Database database, PatientStudy patStudy, int question,
                                     ArrayList<Integer> responses, Date started, Date finished) {
    return "Multiple answers aren't supported for PROMIS assessments";
  }

  public ArrayList<SurveyQuestionIntf> handleQuestion(Database database, PatientStudy patStudy,
                                                      PromisQuestion question, String responseOID, String response) {

    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();

    if ("ERROR".equals(responseOID)) {
      try {
        String xmlString = "<Form><Error>" + response + "</Error></Form>";
        PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
        patStudyDao.setPatientStudyContents(patStudy, xmlString);
      } catch (DatabaseException ex) {
        logger.error(ex.getMessage(), ex);
        throw new ServiceUnavailableException(ex.getLocalizedMessage());
      }
      // question.setAnswered(true);
      // questions.add(question);
      return null;
    }
    PROMISAssessmentUtils promisUtils = new PROMISAssessmentUtils(database, version, siteInfo);
    String result = null;
    try {
      result = promisUtils.administerAssessment(patStudy.getStudyCode(), version, patStudy.getExternalReferenceId(), responseOID, response);
    } catch (FileNotFoundException fnf) {
      result = "<Form><Items><Item>Not found</Item></Items></Form>"; // return
      return questions;
    }
    boolean complete = false;
    if (result != null) {
      try {
        complete = promisUtils.checkComplete(version, patStudy, result, patStudy.getExternalReferenceId());
      } catch (DatabaseException ex) {
        logger.error(ex.getMessage(), ex);
        throw new ServiceUnavailableException(ex.getLocalizedMessage());
      }
    }

    if (complete) {
      return null;
    }
    PromisQuestion newQuestion = promisUtils.getQuestion(result, patStudy, true);
    newQuestion.setAnswered(complete);

    // questions.clear();
    questions.add(newQuestion);
    return questions;
  }

  public String getAssessments(Database database) throws IllegalArgumentException, ServiceUnavailableException,
      FileNotFoundException {
    logger.trace("calling promis: getBanks");
    PROMISAssessmentUtils promisUtils = new PROMISAssessmentUtils(database, version, siteInfo);
    String result = promisUtils.getBanks();
    logger.trace("result={}", result);
    return result;
  }

  @Override
  public String getAssessments(Database database, int version) throws ServiceUnavailableException,
      FileNotFoundException {
    logger.trace("calling promis: getBanks({})", version );
    PROMISAssessmentUtils promisUtils = new PROMISAssessmentUtils(database, version, siteInfo);
    String result = promisUtils.getBanks(version);
    logger.trace("result={}", result);
    return result;
  }

  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    return new PromisScoreProvider(siteInfo, version);
  }

  @Override
  public void setVersion(int vs) {
    version = vs;
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(Database database, PatientStudy study, User user) {
    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
    if (study == null) return questions;
    String xmlDocumentString = study.getContents();
    if (xmlDocumentString == null) return questions;
    PROMISAssessmentUtils promisUtils = new PROMISAssessmentUtils(database, version, siteInfo);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      // Using factory get an instance of document builder
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));
      Document messageDom;
      messageDom = db.parse(is);
      Element docElement = messageDom.getDocumentElement();
      if (docElement.getTagName().equals("Form")) {
        if (version == 2) {
          String finished = docElement.getAttribute("DateFinished");
          if (finished != null && finished.length() > 0) {
            // question.setAnswered(true);
          }
        }
        NodeList itemsNodes = messageDom.getElementsByTagName("Items");
        if (itemsNodes == null || itemsNodes.getLength() < 1) itemsNodes = messageDom.getElementsByTagName("Item");
        if (itemsNodes != null && itemsNodes.getLength() > 0) {
          for (int itemInx = 0; itemInx < itemsNodes.getLength(); itemInx++) {
            Element itemNode = (Element) itemsNodes.item(itemInx);
            questions.add(promisUtils.getQuestion(itemNode, false));
          }
        }
      }
    } catch (SAXException | ParserConfigurationException | IOException e) {
      logger.error("Error parsing survey xml {}", xmlDocumentString, e);
    }
    return null;
  }

  @Override
  public SiteInfo getSiteInfo() {
    return siteInfo;
  }
}
