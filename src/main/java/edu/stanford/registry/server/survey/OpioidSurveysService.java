package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.InvalidDataElementException;
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
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;
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
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;


public class OpioidSurveysService extends NamedSurveyService implements CustomSurveyServiceIntf  {

  private final String surveySystemName = "edu.stanford.registry.server.survey.OpioidSurveysService";
  private static final Logger logger = LoggerFactory.getLogger(OpioidSurveysService.class);
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private static final String TAPS_ATTR = "tapsToolTaken";

  public enum attributeValue { Y, N, Q }

  /*
   * The first question is a qualifying question asked when this part of a Follow up survey.
   * With Initial surveys it checks the patients response to the medsOpioid survey to see if the patient qualifies.
   */
  private final String[] promisSurveyQuestionText = {
      "Are you currently taking any opioid medications (such as Vicodin, Oxycontin, Oxycodone, Morphine, MS-Contin, Codeine, Actiq, Duragesic, Dilaudid, Demerol, Methadone, Percocet, Opana, Nucynta, Stadol, Ultram, Norco)? ",
      "I experienced cravings for pain medication",
      "I used street drugs because they treated my pain better than my prescription pain medication.",
      "I needed more prescription pain medication to relieve my pain.",
      "I wanted more prescription pain medication to relieve my pain.",
      "I used more pain medication before the effects wore off.",
      "When my prescription for pain medication ran out, I felt anxious.",
      "I got prescription pain medication from someone other than my healthcare provider.",
      "I borrowed prescription pain medication from someone.",
      "I ran out of my prescription pain medication early.",
      "I counted the hours to know when I could take my next dose of pain medication.",
      "I saved my unused prescription pain medication just in case I needed it later.",
      "I kept a hidden supply of pain medication.",
      "I used someone else's prescription pain medication.",
      "I hid my use of prescribed pain medication from others.",
      "I got the same prescription pain medication from more than one healthcare provider.",
      "Other people obtained pain medication for me from their own healthcare providers.",
      "I went to the emergency room to get additional pain medication.",
      "I told my healthcare provider that I lost my pain medication and I needed more.",
      "I abused prescription pain medication.",
      "My prescription pain medication was gone too soon.",
      "I used more of my prescribed pain medication than I was supposed to.",
      "I used pain medication against my healthcare provider's advice.",
      "I felt better with a higher dose of pain medication than prescribed.",
      "I used additional medications to help my prescription pain medication work better.",
      "My prescription pain medication was less effective than it used to be."

      };
  /*
  private static final String LOW="Low";
  private static final String MOD="Moderate";
  private static final String HIGH="High";
  private final String[] promisSurveyQuestionThreshold = {MOD, LOW, LOW, LOW, HIGH, HIGH,
      HIGH, HIGH, HIGH, HIGH, MOD, MOD, HIGH, HIGH, HIGH, HIGH, MOD, MOD, HIGH, MOD, MOD,
      MOD, MOD, MOD, LOW};
  */
  private final String[] frequencyAns = { "Never", "Rarely", "Sometimes", "Often", "Almost always"};
  private final String[] intensityAns = {"Not at all", "A little bit", "Somewhat", "Quite a bit", "Very much"};
  private final String[] yesNoAnswers = { "Yes", "No"};
  public OpioidSurveysService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
      String answerJson)  {

    if (patStudyExtended.getDtChanged() != null) {
      return null;
    }
    /* Lookup the patient study to get it with contents */
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
    if (patStudy == null) {
      return null;
    }

    int lastQuestion = -1;
    boolean surveyIsDone = false;
    if (submitStatus != null) {
      FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
      Map<String, Map<String, FormFieldAnswer>> itemOrderToResponseOrderToField = new HashMap<>();
      ArrayList<FormQuestion> questions = new ArrayList<>();

      for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
        /* The format of the fieldId is "ItemOrder:ResponseOrder:Ref" */
        String[] ids = fieldAnswer.getFieldId().split(":");
        String itemOrder = ids[0];
        try {
          Integer questionNumber = Integer.parseInt(itemOrder);
          if (questionNumber > lastQuestion) {
            lastQuestion = questionNumber;
          }
        } catch (NumberFormatException nfe) {
          logger.error("Number format exception getting questionNumber from fieldId " + fieldAnswer.getFieldId(), nfe);
        }
        String responseOrder = ids[1];
        Map<String, FormFieldAnswer> responseOrderToField = itemOrderToResponseOrderToField.get(itemOrder);
        if (responseOrderToField == null) {
          responseOrderToField = new HashMap<>();
          itemOrderToResponseOrderToField.put(itemOrder, responseOrderToField);
        }
        responseOrderToField.put(responseOrder, fieldAnswer);
      }
      /* Answered no to the qualifying question. End the survey. */
      if ( lastQuestion == 0) {
        if ( "1".equals(getSelection(lastQuestion, itemOrderToResponseOrderToField)) ) {

        /* Answered NO to question 1: Mark them as NOT an Opioid patient and stop the survey */
          logger.debug("Patient taking followup answered no to qualifying question, skipping opioid surveys");
          setPatientAttribute(database, patStudyExtended.getPatient(), attributeValue.N);
          skipTAPSTool(database, patStudyDao, patStudyExtended);
          surveyIsDone = true;
        }
      }
      /* Finished the survey */
      if (lastQuestion >= 1) {
        surveyIsDone = true;
        if (!takeTaps(database, patStudyExtended)) { // check if taken within 90 days
          skipTAPSTool(database, patStudyDao, patStudyExtended);
        }
      }

      logger.trace("Saving question " + lastQuestion + " done = " + surveyIsDone);
      for (int q=lastQuestion; q<=lastQuestion; q++) {
        questions.add(getFormQuestion(q).as());
      }
      saveResponses(database, patStudyExtended.getPatient(), patStudy, itemOrderToResponseOrderToField, questions, formAnswer, surveyIsDone);
      if (surveyIsDone) {
        return null;
      }
    } else {
      if (patStudy.getContents() != null) {
          // get lastQuestion from the survey in the database
        SurveyQuery surveyQuery = new SurveyQuery(database, new SurveyDao(database), patStudy.getSurveySiteId());
        Survey survey = surveyQuery.surveyBySurveyToken(patStudy.getToken());
        for (SurveyStep step : survey.answeredStepsByProvider(Integer.toString(getSurveySystemId(database)))) {
          String questionId = step.answer().getSubmitStatus().getQuestionId();
          if (questionId != null && Integer.parseInt(questionId) > lastQuestion) {
            lastQuestion = Integer.parseInt(questionId);
          }
        }
      }
    }
    int nextQuestion= lastQuestion + 1;
    logger.trace("Next Question = " + nextQuestion);
    // survey has not started, check if they should be given these surveys
    if (nextQuestion == 0) {
      /* Checks if patient should be given the survey and which type */
      SurveyRegistration survey ;
      try { /* look up the survey to get the type */
        AssessDao assessDao = new AssessDao(database, siteInfo);
        survey = assessDao.getSurveyRegistrationByRegId(patStudyExtended.getSurveyRegId());
        if (survey == null) { /* doesn't exist ! */
          throw new DataException("Survey Registration not found for surveyRegId:" + patStudyExtended.getSurveyRegId());
        }
      } catch (Exception e) {
        logger.error(e.toString(), e);
        throw new DataException(e.getMessage());
      }

      SurveyType opioidSurveyType = qualifies(database, patStudyExtended, survey);

      /*
       * If the patient doesn't qualify for this survey write an empty form to the database
       */
      if (opioidSurveyType == SurveyType.none) {
        logger.trace("Patient did not qualify for opioid surveys");
        /* update their tapsTool study to skip */
        skipTAPSTool(database, patStudyDao, patStudyExtended);
        if (survey.getSurveyType().startsWith("Initial")) { // also skip all of these
          logger.trace("Patient did not qualify for opioid surveys on initial survey, skippint POS");
          patStudyExtended.setContents(emptyForm);
          updateStudy(database, patStudyExtended, true);
          patStudyExtended.setDtChanged(new Date()); // we do this so when called again we quit
          /* Return a skip question to end this survey. */
          return getSkipQuestion(database, patStudyExtended.getStudyCode());
        }
      }
      if (opioidSurveyType == SurveyType.initial) {
        logger.trace("Patient qualified for opioid surveys on initial, skipping first POS question ");
        nextQuestion++; // skip qualifying question
      }
    } else if (nextQuestion > 1) {
      /*
       * We're not doing the opioidsurvey anymore so skip over it and go onto the TAPSTool
       */
      patStudyExtended.setContents(emptyForm);
      updateStudy(database, patStudyExtended, true);
      patStudyExtended.setDtChanged(new Date()); // we do this so when called again we quit
      return getSkipQuestion(database, patStudyExtended.getStudyCode());
    }

    logger.trace("Returning question " + nextQuestion);

    /* Send back the question */
    NextQuestion next = new NextQuestion();
    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setQuestionType(QuestionType.form);
    displayStatus.setSurveyProviderId(Integer.toString(getSurveySystemId(database)));
    displayStatus.setSurveySectionId(Integer.toString(patStudyExtended.getStudyCode()));
    displayStatus.setQuestionId("PromisOpioidQ" + nextQuestion);
    next.setDisplayStatus(displayStatus);
    next.setQuestion(getFormQuestion(nextQuestion));
    return next;
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(Database database, PatientStudy study, User user) {
    return getRegistrySurvey(database, study, user);
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy prtStudy,
      Patient patient, boolean allAnswers) {
    return super.getSurvey(patientStudies, prtStudy, patient, allAnswers);

  }

  @Override
  public String getAssessments(Database database, int version)  {

    StringBuilder myStudies = new StringBuilder();
    myStudies.append("<forms>");
    myStudies.append("<form OID=\"");
    String sql = "select studyCode,  studyDescription, title from study where survey_system_id = "
    + mySurveySystem.getInstance(database, getSurveySystemName()).getSurveySystemId();

    final ArrayList<Study> studies =  database.toSelect(sql).query(new RowsHandler<ArrayList<Study>>() {
      @Override
      public ArrayList<Study> process(Rows rs)  {
        ArrayList<Study> studies = new ArrayList<>();
        while (rs.next()) {
          Study study = new Study();
          study.setStudyCode(rs.getIntegerOrNull("studyCode"));
          study.setStudyDescription(rs.getStringOrNull("studyDescription"));
          study.setTitle(rs.getStringOrNull("title"));
          studies.add(study);
        }
        return studies;
      }
    });

    for (Study study : studies) {
        myStudies.append(study.getStudyCode());
        myStudies.append("\" name=\"");
        myStudies.append(study.getStudyDescription());
        myStudies.append("\" />");
    }
    myStudies.append("</forms>");
    return myStudies.toString();
  }

  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    return this;
  }

  @Override
  public int getVersion() {
    return super.getVersion();
  }
  @Override
  public void setVersion(int version) {
    super.setVersion(version);
  }

  @Override
  public String getSurveySystemName() {
    return surveySystemName;
  }

  @Override
  public int getSurveySystemId(Database database) {
    return mySurveySystem.getInstance(database, surveySystemName).getSurveySystemId();
  }

  @Override
  public String getStudyName() {
    return "opioidPromisSurvey";
  }

  @Override
  public String getTitle() {
    return "PROMIS Opioid Pain Medications" ;
  }

  @Override
  public void setValue(String value) {
  }

  private AutoBean<FormQuestion > getFormQuestion(int questionNumber) {
    if (questionNumber > promisSurveyQuestionText.length - 1) {
      logger.error("We don't have " + questionNumber + " questions! ");
      return null;
    }
    AutoBean<FormQuestion> bean = factory.formQuestion();
    FormQuestion question = bean.as();

    question.setFields(getFormFields(questionNumber));
    return bean;
  }

  private void saveResponses(Database database, Patient patient, PatientStudy patStudy, Map<String, Map<String, FormFieldAnswer>> itemOrderToResponseOrderToField ,
      ArrayList<FormQuestion> questions, FormAnswer formAnswer, boolean isSurveyDone) {
    try {

      /* Create the xml from the response */
      String xmlDocumentString = patStudy.getContents();
      if (xmlDocumentString == null) {
        xmlDocumentString = emptyForm;
      }
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));
      Document doc = db.parse(is);
      org.w3c.dom.Element docElement = doc.getDocumentElement();

      HashMap<String, Element> itemElements = new HashMap<>();
      if (!docElement.getTagName().equals(Constants.FORM)) {
        throw new Exception("opioidPromisSurvey No FORM tag on document!");
      }
      NodeList itemsList = doc.getElementsByTagName(Constants.ITEMS);
      if (itemsList == null || itemsList.getLength() < 1) {
        throw new Exception("opioidPromisSurvey Items not found on xml !");
      }
      String timeFinished = (Long.valueOf((new Date()).getTime())).toString();
      for (FormQuestion formQuestion : questions) {
        Element itemElement = null;

        for (FormField field : formQuestion.getFields()) {
          itemElement = addFieldToItemElement(doc, itemElement, field);
          itemElement.setAttribute(Constants.TIME_FINISH, timeFinished);
          itemElements.put(itemElement.getAttribute(Constants.ORDER), itemElement);
          itemsList.item(0).appendChild(itemElement);
          if (field.getValues() != null && field.getValues().size() > 0) {
            List<FormField> subFields = field.getValues().get(0).getFields();
            if (subFields != null) {
               for (FormField subfield : subFields) {
                 itemElement = addFieldToItemElement(doc, itemElement, subfield);
                 itemElement.setAttribute(Constants.TIME_FINISH, timeFinished);
               }
            }
          }
        }

        for (String key : itemElements.keySet()) {
          itemElement = itemElements.get(key);
          StringBuffer itemResponse = itemElement.hasAttribute(Constants.ITEM_RESPONSE) ? new StringBuffer(
              itemElement.getAttribute(Constants.ITEM_RESPONSE)) : new StringBuffer();
          Map<String, FormFieldAnswer> responseOrderToField = itemOrderToResponseOrderToField.get(key);
          for (Entry<String, FormFieldAnswer> fieldAns : responseOrderToField.entrySet()) {
            String[] ids = fieldAns.getValue().getFieldId().split(":");
            if (!fieldAns.getValue().getChoice().isEmpty()) {
              logger.trace("looking up key:" + fieldAns.getKey() + " item " + ids[0] + " response " + ids[1]);
              if (itemElement.getElementsByTagName(Constants.RESPONSE) != null
                  && itemElement.getElementsByTagName(Constants.RESPONSE).getLength() > 0) {
                NodeList responseList = itemElement.getElementsByTagName(Constants.RESPONSE);
                for (int r = 0; r < responseList.getLength(); r++) {
                  Element respElement = (Element) itemElement.getElementsByTagName(Constants.RESPONSE).item(r);
                  NodeList respItemList = respElement.getElementsByTagName(Constants.XFORM_ITEM);
                  for (int itmInx = 0; itmInx < respItemList.getLength(); itmInx++) {
                    Element respItem = (Element) respItemList.item(itmInx);
                    NodeList valueNodes = respItem.getElementsByTagName(Constants.XFORM_VALUE);
                    if (valueNodes.getLength() > 0) {
                      NodeList valueNodeList = valueNodes.item(0).getChildNodes();
                      if (valueNodeList.getLength() > 0) {
                        String value = valueNodeList.item(0).getNodeValue();
                        if (value != null) {
                          for (String v : fieldAns.getValue().getChoice()) {
                            if (value.equals(v)) {
                              if (itemResponse.length() > 0) {
                                itemResponse.append(", ");
                              }
                              itemResponse.append(ids[1]);
                              itemElement.setAttribute(Constants.ITEM_RESPONSE, itemResponse.toString());
                              respItem.setAttribute("selected", "true");
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
        }
      }
      Transformer trans = TransformerFactory.newInstance().newTransformer();
      trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      trans.setOutputProperty(OutputKeys.INDENT, "yes");
      StringWriter strWriter = new StringWriter();
      trans.transform(new DOMSource(doc), new StreamResult(strWriter));
      patStudy.setContents(strWriter.toString());
      updateStudy(database, patStudy, isSurveyDone);
      if (isSurveyDone && attributeValue.Q.toString().equals(patient.getAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_PROMIS_PAIN_MEDS).getDataValue())) {
        setPatientAttribute(database, patient, attributeValue.Y);
      }
    } catch (Exception e) {
      logger.error("Error creating xml for opioidPromisSurvey study " + patStudy.getStudyCode() + " for token " + patStudy.getToken(), e);
    }
  }

  private void skipTAPSTool(Database database, PatStudyDao patStudyDao,
                              PatientStudyExtendedData patStudyExtended) {
    logger.trace("Skipping TAPSTool");
    SurveySystDao ssDao = new SurveySystDao(database);
    SurveySystem surveySystem = ssDao.getSurveySystem("Local");
    Study tapsStudy = ssDao.getStudy(surveySystem.getSurveySystemId(), "TAPSTool");
    if (tapsStudy != null){
      PatientStudy tapsPatientStudy = patStudyDao.getPatientStudy(patStudyExtended.getPatientId(),
          tapsStudy.getStudyCode(), patStudyExtended.getToken(), true);
      if (tapsPatientStudy != null) {
        tapsPatientStudy.setContents(emptyForm);
        updateStudy(database, tapsPatientStudy, true);
      }
    }
  }

  private boolean takeTaps(Database database, PatientStudyExtendedData patStudyExtended) {
    // before going to the TAPSTool check they haven't done it in the last 90 days
    Patient patient = patStudyExtended.getPatient();

    if (patient.hasAttribute(TAPS_ATTR) &&
        patient.getAttribute(TAPS_ATTR) != null) {
      String dataValue = patient.getAttribute(TAPS_ATTR).getDataValue();
      try {
        Date lastTAPS = new Date(Long.parseLong(dataValue));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -90);
        if (lastTAPS.after(calendar.getTime())) {
          logger.trace("skipping TAPS, last taken {} was after {} ", lastTAPS.toString(), calendar.getTime().toString() );
          return false;
        }
      } catch (NumberFormatException nfe) {
        logger.error("Invalid date value in patient {} attribute ", TAPS_ATTR);
      }
    }
    Long now = (new Date()).getTime();
    PatientAttribute patientAttribute = new PatientAttribute(patient.getPatientId(), TAPS_ATTR,  now.toString());
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    patAttribDao.insertAttribute(patientAttribute);
    patient.addAttribute(patientAttribute);
    return true;
  }

  private Element addFieldToItemElement(Document doc, Element itemElement, FormField field) {
     String[] ids = field.getFieldId().split(":");
    if (itemElement == null || !itemElement.getAttribute(Constants.ORDER).equals(ids[0])) {
      itemElement = getItem(doc, ids[0], ids[2]);
    }
    Element responsesElement = null;
    NodeList responses = itemElement.getElementsByTagName(Constants.RESPONSES);
    if (responses != null && responses.getLength() > 0) {
      responsesElement = (Element) responses.item(0);
    }
    if (responsesElement == null) {
        responsesElement = doc.createElement(Constants.RESPONSES);
    }
    if (field.getType() == FieldType.heading) {
      Element descElement = getDescription(doc, field.getLabel());
      itemElement.appendChild(descElement);
    }
    if (field.getType() == FieldType.radios) {

      Element respElement = getResponse(doc, ids[1], "select1", "Full");
      for (FormFieldValue value : field.getValues()) {
        Element respItemElement = getResponseItem(doc, value.getLabel(), value.getId());
        respElement.appendChild(respItemElement);
      }
      responsesElement.appendChild(respElement);
      itemElement.appendChild(responsesElement);
    }
    if (field.getType() == FieldType.checkboxes) {
      Element respElement = getResponse(doc, ids[1], "select", "Full");
      for (FormFieldValue value : field.getValues()) {
        Element respItemElement = getResponseItem(doc, value.getLabel(), value.getId());
        respElement.appendChild(respItemElement);
      }
      responsesElement.appendChild(respElement);
      itemElement.appendChild(responsesElement);
    }
    if (field.getType() == FieldType.number) {
      Element respElement = getResponse(doc, ids[1], "input", "");
      responsesElement.appendChild(respElement);
      itemElement.appendChild(responsesElement);
    }
    return itemElement;
  }

  private void updateStudy(Database database, PatientStudy patStudy, boolean isDone) {
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    patStudyDao.setPatientStudyContents(patStudy, isDone);
  }

  private FormFieldValue getFormFieldValue(String label, String value) {
    FormFieldValue ffValue = factory.value().as();
    ffValue.setId(value);
    ffValue.setLabel(label);
    return ffValue;
  }

  @Override
  public void registerAssessment(Database database, Element questionaire, String patientId,
                                 Token tok, User user) throws ServiceUnavailableException {
    checkService();

    String qOrder = questionaire.getAttribute(Constants.XFORM_ORDER);
    Integer order = Integer.valueOf(qOrder);

    /* Get the study */
    SurveySystDao ssDao = new SurveySystDao(database);
    Study study = ssDao.getStudy(service.getSurveySystemId(database), service.getStudyName());

    /* Add the study if it doesn't exist */
    if (study == null) {
      study = registerAssessment(database, service.getStudyName(), service.getTitle(), "");
    }

    /* Get the patient and the study for this patient */
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

  private SurveyType qualifies(Database database, PatientStudyExtendedData patStudyExtended, SurveyRegistration survey) {
    if (patStudyExtended == null || patStudyExtended.getPatient() == null) {
      return SurveyType.none;
    }
    Patient patient = patStudyExtended.getPatient();

    /* See if already marked as not qualified or already taken
    if (patient.hasAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_PROMIS_PAIN_MEDS) &&
        patient.getAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_PROMIS_PAIN_MEDS) != null) {
        String dataValue = patient.getAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_PROMIS_PAIN_MEDS).getDataValue();
        if (!attributeValue.Q.toString().equals(dataValue.toUpperCase())) {
          return SurveyType.none;
        }
    }*/
    /* On initial surveys see if they chose any opioid medications and selected "Still taking" */
    if (survey.getSurveyType().startsWith("Initial")) {
      logger.trace("patient is taking an initial survey, checking opioid meds");
      if (checkMeds(database, patient, survey.getSurveySiteId(), survey.getToken(), "medsOpioid")) {
        setPatientAttribute(database, patient, attributeValue.Y);
        logger.debug("Patient taking the Initial survey qualified.  Returning type initial");
        return SurveyType.initial;
      } else {
        setPatientAttribute(database, patient, attributeValue.N);
        logger.debug("Patient taking the Initial survey didn't qualify.  Returning type none");
        return SurveyType.none;
      }
    }
    setPatientAttribute(database, patient, attributeValue.Q);
    return SurveyType.followup;
  }

  /**
   * Check the Opioid medications selected, if any are 'Still Taking' returns true
   */
  private boolean checkMeds(Database database, Patient patient, Long siteId, String token, String surveyName) {
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    SurveySystDao ssDao = new SurveySystDao(database);
    SurveySystem surveySystem = ssDao.getSurveySystem("Local");
    Study medsStudy = ssDao.getStudy(surveySystem.getSurveySystemId(), surveyName);
    try {
      PatientStudy mhPatientStudy = patStudyDao.getPatientStudy(patient.getPatientId(),
                                                                medsStudy.getStudyCode(), token, true);
      if (mhPatientStudy != null) {
        RegistryShortFormScoreProvider mhSurveyService = new RegistryShortFormScoreProvider(database, siteInfo);
        PatientStudyExtendedData mhPatientStudyExt = new PatientStudyExtendedData(mhPatientStudy);
        mhPatientStudyExt.setPatient(patient);
        mhPatientStudyExt.setStudy(medsStudy);
        mhPatientStudyExt.setSurveySystemName(surveySystem.getSurveySystemName());
        mhPatientStudyExt.setContents(mhPatientStudy.getContents());
        PrintStudy pStudy = new PrintStudy(siteInfo, medsStudy, surveySystem.getSurveySystemName());
        ArrayList<PatientStudyExtendedData> mhPatientStudies = new ArrayList<>();
        mhPatientStudies.add(mhPatientStudyExt);
        ArrayList<SurveyQuestionIntf> questions = mhSurveyService.getSurvey(mhPatientStudies, pStudy, patient, true);
        boolean med = true;
        String medNameTaking = null;
        logger.trace(questions.size()+" questions returned for " + surveyName +" study");
        for (int q = 0; q < questions.size(); q++) {
          ArrayList<SurveyAnswerIntf> ans = questions.get(q).getAnswers(true);

          if (ans == null) {
            logger.trace("null answers returned for question " + q);
          } else {
            logger.trace(ans.size() + " answers returned for question " + q);
          }

          if (med) {
            if (ans != null && ans.size() > 0 ) {
              SurveyAnswerIntf answer = ans.get(0);
              if (answer.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_SELECT) {

                logger.trace("answer " + answer.getText() + " is TYPE_SELECT and getSelected is " + answer.getSelected() );
                SelectElement select = (SelectElement) answer;
                ArrayList<SelectItem> selectItems = select.getSelectedItems();
                logger.trace("with " + selectItems.size() + " selected items");
                for (SelectItem item : selectItems) {

                  medNameTaking = item.getValue().toLowerCase();
                  logger.trace("found med named " + medNameTaking + " for question " + q);
                }
              }
            }
            med = false;
          } else {
            if (ans != null && ans.size() > 0 && medNameTaking != null ) {
              SurveyAnswerIntf answer = ans.get(0);
              if (answer.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_SELECT) {
                SelectElement select = (SelectElement) answer;
                ArrayList<SelectItem> selectItems = select.getSelectedItems();
                for (SelectItem item : selectItems) {
                  if ("Still taking".equals(item.getLabel())) {
                    logger.trace("found Still taking label");
                      return true;
                  }
                }
              }
            }
            medNameTaking = null;
            med = true;
          }
        }
      }
     } catch (DataException | InvalidDataElementException e) {
       logger.error("Error checking medsOpioid ", e);
     }
    return false;
  }

  private NextQuestion getSkipQuestion(Database database, Integer studyCode) {
    NextQuestion next = new NextQuestion();
    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setQuestionType(QuestionType.skip);
    displayStatus.setSurveyProviderId(Integer.toString(getSurveySystemId(database)));
    displayStatus.setSurveySectionId(Integer.toString(studyCode));
    next.setDisplayStatus(displayStatus);
    return next;
  }

  private void setPatientAttribute(Database database, Patient patient, Enum<?> dataValue) {

    PatientAttribute pattribute = patient.getAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_PROMIS_PAIN_MEDS);
    if (pattribute == null) {
        pattribute = new PatientAttribute(patient.getPatientId(),
        edu.stanford.registry.shared.Constants.ATTRIBUTE_PROMIS_PAIN_MEDS, dataValue.toString(),
        PatientAttribute.STRING);
    } else {
      if (dataValue.toString().equals(pattribute.getDataValue())) {
        return;
      }
      pattribute.setDataValue(dataValue.toString());
    }
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    patAttribDao.insertAttribute(pattribute);
    patient.addAttribute(pattribute);
  }

  private ArrayList<FormField> getFormFields(int questionNumber) {
    ArrayList<FormField> fields = new ArrayList<>();
    FormField headingField = factory.field().as();
    headingField.setType(FieldType.heading);
    headingField.setFieldId(questionNumber+":0:PROMISPAINMED"+questionNumber);
    headingField.setLabel(promisSurveyQuestionText[questionNumber]);
    fields.add(headingField);
    switch (questionNumber) {
      /* Qualifying question on Follow up surveys */
      case 0:
        fields.add(radiosField(questionNumber, 1, yesNoAnswers));
        break;
      /* Intensity */
      case 6:
      case 23:
      case 25:
        fields.add(radiosField(questionNumber, 1, intensityAns));
        break;
      /* Frequency */
      default:
        fields.add(radiosField(questionNumber, 1, frequencyAns));
        break;
    }
    return fields;
  }

  private Element getItem(Document doc, String order, String ID) {
    Element itemElement = doc.createElement(Constants.ITEM);
    itemElement.setAttribute(Constants.ORDER, order);
    itemElement.setAttribute(Constants.ITEM_RESPONSE,"");
    itemElement.setAttribute(Constants.ITEM_SCORE,"");
    itemElement.setAttribute("ID", ID);
    return itemElement;
  }

  private Element getDescription(Document doc, String desc) {
    Element descElement = doc.createElement(Constants.DESCRIPTION);
    descElement.appendChild(doc.createTextNode(desc));
    return descElement;
  }
  private Element getResponse(Document doc, String itemNumber, String type, String appearance) {
    Element responseElement = doc.createElement(Constants.RESPONSE);
    responseElement.setAttribute(Constants.ORDER, itemNumber);
    responseElement.setAttribute(Constants.TYPE, type);
    responseElement.setAttribute(Constants.APPEARANCE, appearance);
    return responseElement;
  }

  private Element getResponseItem(Document doc, String label, String value) {
    Element itemElement = doc.createElement(Constants.XFORM_ITEM);
    Element labelElement = doc.createElement("label");
    labelElement.appendChild(doc.createTextNode(label));
    Element valueElement = doc.createElement("value");
    valueElement.appendChild(doc.createTextNode(value));
    itemElement.appendChild(labelElement);
    itemElement.appendChild(valueElement);
    return itemElement;
  }

  private String getSelection(int question, Map<String, Map<String, FormFieldAnswer>> itemOrderToResponseOrderToField ) {

    Map<String, FormFieldAnswer> responseOrderToField = itemOrderToResponseOrderToField.get((Integer.valueOf(question)).toString());
    for (Entry<String, FormFieldAnswer> fieldAns : responseOrderToField.entrySet()) {

      for (String v : fieldAns.getValue().getChoice()) {
        logger.trace("found answer to question " + question + " field " +   fieldAns.getKey() + " value " + v);
        return v;
      }
    }
    return "";
  }

  private FormField radiosField(int questionNumber, int fieldNumber, String[] options) {
    FormField field = factory.field().as();
    field.setType(FieldType.radios);
    field.setFieldId(questionNumber + ":" + fieldNumber +":OPIOIDSURVEYS" + questionNumber);
    field.setRequired(false);
    field.setValues(new ArrayList<>());
    for (int inx = 0; inx < options.length; inx++) {
      field.getValues().add(getFormFieldValue(options[inx], Integer.valueOf(inx).toString()));
    }
    return field;
  }

}



