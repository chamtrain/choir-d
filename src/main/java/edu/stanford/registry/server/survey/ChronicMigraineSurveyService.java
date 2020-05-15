package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.utils.StringUtils;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.survey.client.api.CollapsibleRadiosetQuestion;
import edu.stanford.survey.client.api.DisplayStatus;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.RadiosetAnswer;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
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

import org.apache.log4j.Logger;
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

/**
 Survey service for the 12 Question Chronic Migraine Research Study.
 This survey is offered to patients who:
 - are identified as a headache patient,
 - answered Yes to the standard research question
 - have just completed an initial headache survey or chose 'Ask me again later' when last presented with this surveys qualifying question.
 The first question this displays is the research consent. If the patient selects "YES" then they are asked the 12 questions. Any other
 response stops the survey.
  */
public class ChronicMigraineSurveyService extends NamedSurveyService implements CustomSurveyServiceIntf {
  public final static String studyName = "chronicMigraine";

  final String surveySystemName = "ChronicMigraineSurveyService";
  static Logger logger = Logger.getLogger(ChronicMigraineSurveyService.class);
  final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  private enum attributeValue {Y, N, A}; // Y(es), N(o), A(sk me again later)

  private final String[] questionText = { "",
      "on how many days did you have a headache of any type? ",
      "on how many days did you have a headache of any type? ",
      "how often were you unusually sensitive to light (eg. you felt more comfortable in a dark place)?",
      "how often were you unusually sensitive to sound (eg. you felt more comfortable in a quiet place)?",
      "how often was the pain moderate or severe?",
      "how often did you feel nauseated or sick to your stomach?",
      "how many days did you use over-the-counter medications to treat your headache attacks?",
      "how many days did you use prescription medications to treat your headache attacks?",
      "how many days did you miss work or school because of your headaches?",
      "how many days did you miss family, social, or leisure activities because of your headaches?",
      "how often did your headaches interfere with making plans?",
      "how often did you worry about making plans because of your headaches?"
  };

  final String[] listOptions = { "Never", "Rarely", "Less than half the time", "Half the time or more" };

  public ChronicMigraineSurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  public String getQuestionId() {
    return "chronicMigraine";
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
                                     String answerJson) {
    if (patStudyExtended == null || patStudyExtended.getDtChanged() != null) { // missing data or survey is already done
      return null;
    }
    SurveyRegistration surveyRegistration = null;
    PatientStudy patStudy = null;
    AssessDao assessDao = new AssessDao(database, siteInfo);
    try { // look up the survey to get the type
      surveyRegistration = assessDao.getSurveyRegistrationByRegId(patStudyExtended.getSurveyRegId());
      if (surveyRegistration == null) { // doesn't exist !
        throw new DataException("Survey Registration not found for surveyRegId:" + patStudyExtended.getSurveyRegId());
      }
      // Lookup the patient study to get it with contents
      PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
      patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
      if (patStudy == null) { // doesn't exist !
        throw new DataException("Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
            + patStudyExtended.getToken());
      }
    } catch (Exception e) {
      logger.error(e.toString(), e);
      throw new DataException(e.getMessage());
    }

    SurveyType migraineSurveyType;
    if (attributeValue.Y.toString().equals(getAttributeValue(patStudyExtended.getPatient(), edu.stanford.registry.shared.Constants.ATTRIBUTE_CHRONIC_MIGRAINE)) &&
        patStudy.getContents() != null && patStudy.getDtChanged() == null) { // currently taking it
      migraineSurveyType = SurveyType.initial;
    } else {
      migraineSurveyType = qualifies(database, patStudyExtended.getPatient(), surveyRegistration);
    }

    // If the patient doesn't qualify for this survey write an empty form to the database,
    if (migraineSurveyType.equals(SurveyType.none)) {
      logger.debug("Patient didn't qualify. Writing empty survey");
      patStudyExtended.setContents(emptyForm);
      updatePatientStudy(database, patStudyExtended, true);
      patStudyExtended.setDtChanged(new Date()); // we do this so when called again we quit
      NextQuestion next = new NextQuestion();
      DisplayStatus displayStatus = factory.displayStatus().as();
      displayStatus.setQuestionType(QuestionType.skip);
      displayStatus.setSurveyProviderId(Integer.toString(getSurveySystemId(database)));
      displayStatus.setSurveySectionId(Integer.toString(patStudyExtended.getStudyCode()));
      next.setDisplayStatus(displayStatus);
      return next;
    }

    int nextQuestionNumber = -1;
    int lastQuestionNumber = 0;
    if (submitStatus != null) {
      boolean surveyIsDone = false;
      if (patStudy.getContents() == null) { // This is the first question
        RadiosetAnswer answer = AutoBeanCodex.decode(factory, RadiosetAnswer.class, answerJson).as();
        String answerText = answer.getChoice();
        if (answerText != null) {
          ConsentQuestion consent = new ConsentQuestion(factory);
          String itemResponse = consent.getItemResponse(answerText);
          if ("0".equals(itemResponse)) {
            setChronicMigraineAttribute(database, patStudyExtended.getPatient(), attributeValue.Y);
          } else if ("1".equals(itemResponse)) {
            setChronicMigraineAttribute(database, patStudyExtended.getPatient(), attributeValue.N);
            surveyIsDone = true;
          } else {
            setChronicMigraineAttribute(database, patStudyExtended.getPatient(), attributeValue.A);
            surveyIsDone = true;
          }
          saveConsent(database, patStudy, consent.getAnsweredXmlString(answerText), surveyIsDone);
        }
      } else {
        int firstQuestion = 99;
        FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
        Map<String, Map<String, FormFieldAnswer>> itemOrderToResponseOrderToField = new HashMap<>();
        for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
        // The format of the fieldId is "ItemOrder:ResponseOrder:Ref"
          String[] ids = fieldAnswer.getFieldId().split(":");
          String itemOrder = ids[0];
          try {
            Integer questionNumber = Integer.parseInt(itemOrder);
            if (questionNumber > lastQuestionNumber) {
              lastQuestionNumber = questionNumber;
            }
            if (questionNumber < firstQuestion) {
              firstQuestion = questionNumber;
            }
          } catch (NumberFormatException nfe) {
            logger.error(
                "Number format exception getting questionNumber from fieldId " + fieldAnswer.getFieldId(), nfe);
          }
          String responseOrder = ids[1];
          Map<String, FormFieldAnswer> responseOrderToField = itemOrderToResponseOrderToField.get(itemOrder);
          if (responseOrderToField == null) {
            responseOrderToField = new HashMap<>();
            itemOrderToResponseOrderToField.put(itemOrder, responseOrderToField);
          }
          responseOrderToField.put(responseOrder, fieldAnswer);
        }

        ArrayList<FormQuestion> questions = new ArrayList<>();
        for (int q = firstQuestion; q <= lastQuestionNumber; q++) {
          questions.add(getFormQuestion(q, migraineSurveyType).as());
        }
        if (lastQuestionNumber >= questionText.length - 1) {
          surveyIsDone = true;
        }
        logger.debug("Saving questions " + firstQuestion + " - " + lastQuestionNumber + " done: " + surveyIsDone);
        saveResponses(database, patStudyExtended, patStudy, itemOrderToResponseOrderToField, questions, formAnswer, surveyIsDone);
      }
      nextQuestionNumber = lastQuestionNumber;
      if (surveyIsDone) {
        return null;
      }
    } else {
      if (patStudy.getContents() != null) {
        // get lastQuestionNumber from the survey in the database
        SurveyQuery surveyQuery = new SurveyQuery(database, new SurveyDao(database), patStudy.getSurveySiteId());
        Survey survey = surveyQuery.surveyBySurveyToken(patStudy.getToken());
        for (SurveyStep step : survey.answeredStepsByProvider(Integer.toString(getSurveySystemId(database)))) {
          if (step != null && step.answer() != null && step.answer().getSubmitStatus() != null) {
            String questionIdString = step.answer().getSubmitStatus().getQuestionId();
            String questionId = "0";
            if (questionIdString.contains(getQuestionId())) {
              questionId = questionIdString.substring(9);
            }
            logger.debug("last question was " + questionId);
            if (questionId != null && Integer.parseInt(questionId) > lastQuestionNumber) {
              lastQuestionNumber = Integer.parseInt(questionId);
            }
          }
        }
        nextQuestionNumber = lastQuestionNumber;
      }
    }
    nextQuestionNumber++;


    // Send back the question
    if (nextQuestionNumber == 0) {
      ConsentQuestion consent = new ConsentQuestion(factory);
      return consent.getQualifyingQuestion(patStudyExtended, getQuestionId());
    }
    NextQuestion next = new NextQuestion();

    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setQuestionType(QuestionType.form);
    displayStatus.setSurveyProviderId(Integer.toString(getSurveySystemId(database)));
    displayStatus.setSurveySectionId(Integer.toString(patStudyExtended.getStudyCode()));
    displayStatus.setQuestionId(getQuestionId() + "Q" + nextQuestionNumber);
    next.setDisplayStatus(displayStatus);
    next.setQuestion(getFormQuestion(nextQuestionNumber, migraineSurveyType));
    return next;
  }

  @Override
  public String getAssessments(Database database, int version) throws Exception {

    StringBuilder myStudies = new StringBuilder();
    myStudies.append("<forms>");
    myStudies.append("<form OID=\"");
    String sql = "select studyCode,  studyDescription, title from study where survey_system_id = "
        + mySurveySystem.getInstance(database, getSurveySystemName()).getSurveySystemId();

    final ArrayList<Study> studies = database.toSelect(sql).query(new RowsHandler<ArrayList<Study>>() {
      @Override
      public ArrayList<Study> process(Rows rs) throws Exception {
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
    return studyName;
  }

  @Override
  public String getTitle() {
    return "Chronic Migraine";
  }

  @Override
  public void setValue(String value) {
  }

  private AutoBean<FormQuestion> getFormQuestion(int questionNumber, SurveyType headacheSurveyType) {
    if (questionNumber > questionText.length - 1) {
      logger.error("We don't have " + questionNumber + " questions! ");
      return null;
    }
    AutoBean<FormQuestion> bean = factory.formQuestion();
    FormQuestion question = bean.as();

    question.setFields(getFormFields(questionNumber));
    return bean;
  }

  private void saveResponses(Database database, PatientStudyExtendedData patStudyExt, PatientStudy patStudy, Map<String, Map<String, FormFieldAnswer>> itemOrderToResponseOrderToField,
                             ArrayList<FormQuestion> questions, FormAnswer formAnswer, boolean isSurveyDone) {
    try {


      // Create the xml from the response
      String xmlDocumentString = patStudy.getContents();
      if (xmlDocumentString == null) {
        xmlDocumentString = emptyForm;
      }
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));
      Document doc = db.parse(is);
      Element docElement = doc.getDocumentElement();

      HashMap<String, Element> itemElements = new HashMap<>();
      if (!docElement.getTagName().equals(Constants.FORM)) {
        throw new Exception("headacheSurvey No FORM tag on document!");
      }
      NodeList itemsList = doc.getElementsByTagName(Constants.ITEMS);
      if (itemsList == null || itemsList.getLength() < 1) {
        throw new Exception("headacheSurvey Items not found on xml !");
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
              logger.debug("looking up key:" + fieldAns.getKey() + " item " + ids[0] + " response " + ids[1]);
              if (itemElement.getElementsByTagName(Constants.RESPONSE) != null
                  && itemElement.getElementsByTagName(Constants.RESPONSE).getLength() > 0) {
                NodeList responseList = itemElement.getElementsByTagName(Constants.RESPONSE);
                for (int r = 0; r < responseList.getLength(); r++) {
                  Element respElement = (Element) itemElement.getElementsByTagName(Constants.RESPONSE).item(r);
                  if (respElement.hasAttribute(Constants.ORDER)
                      && ids[1].equals(respElement.getAttribute(Constants.ORDER))
                      && respElement.hasAttribute(Constants.TYPE)
                      && "input".equals(respElement.getAttribute(Constants.TYPE))) {
                    if (!fieldAns.getValue().getChoice().isEmpty()) {
                      String stringValue = fieldAns.getValue().getChoice().get(0);
                      if (stringValue != null && stringValue.length() > 0) {
                        stringValue = StringUtils.cleanString(stringValue);
                        Element value = doc.createElement(Constants.XFORM_VALUE);
                        value.appendChild(doc.createTextNode(stringValue));
                        respElement.appendChild(value);

                        if (itemResponse.length() > 0) {
                          itemResponse.append(", ");
                        }
                        itemResponse.append(ids[1]);
                        itemElement.setAttribute(Constants.ITEM_RESPONSE, itemResponse.toString());
                      }
                    }
                  } else if (respElement.hasAttribute(Constants.ORDER)
                      && ids[1].equals(respElement.getAttribute(Constants.ORDER))
                      && respElement.hasAttribute(Constants.TYPE)
                      && ("select".equals(respElement.getAttribute(Constants.TYPE))
                      || "select1".equals(respElement.getAttribute(Constants.TYPE)))) {
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
      }
      Transformer trans = TransformerFactory.newInstance().newTransformer();
      trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      trans.setOutputProperty(OutputKeys.INDENT, "yes");
      StringWriter strWriter = new StringWriter();
      trans.transform(new DOMSource(doc), new StreamResult(strWriter));
      Patient patient = patStudyExt.getPatient();
      patStudy.setContents(strWriter.toString());
      updatePatientStudy(database, patStudy, isSurveyDone);
      if (isSurveyDone) {
        setChronicMigraineAttribute(database, patient, attributeValue.Y);
      }
    } catch (Exception e) {
      logger.error("Error creating xml for headache study " + patStudyExt.getStudyCode() + " for token "
          + patStudyExt.getToken(), e);
    }
  }

  private Element addFieldToItemElement(Document doc, Element itemElement, FormField field) throws Exception {
    String[] ids = field.getFieldId().split(":");
    if (itemElement == null || !itemElement.getAttribute(Constants.ORDER).equals(ids[0])) {
      itemElement = itemElement(doc, ids[0], ids[2]);
    }
    Element responsesElement = null;
    NodeList responses = itemElement.getElementsByTagName(Constants.RESPONSES);
    if (responses != null && responses.getLength() > 0) {
      responsesElement = (Element) responses.item(0);
    }
    if (responsesElement == null) {
      responsesElement = doc.createElement(Constants.RESPONSES);
    }
    if (field.getType().equals(FieldType.heading)) {
      Element descElement = descriptionElement(doc, field.getLabel());
      itemElement.appendChild(descElement);
    }
    if (field.getType().equals(FieldType.radios)) {
      String fieldLabel = "";
      if (field.getLabel() != null && !field.getLabel().isEmpty() && field.getAttributes() != null
          && field.getAttributes().get("REPORT") != null
          && ("Y".equals(field.getAttributes().get("REPORT").toString()))) {
        fieldLabel = field.getLabel() + " "; // prepend the field label
      }
      Element respElement = responseElement(doc, ids[1], "select1", "Full");
      for (FormFieldValue value : field.getValues()) {
        Element respItemElement = responseItemElement(doc, fieldLabel + value.getLabel(), value.getId());
        respElement.appendChild(respItemElement);
      }

      responsesElement.appendChild(respElement);
      itemElement.appendChild(responsesElement);
    }
    if (field.getType().equals(FieldType.checkboxes)) {
      Element respElement = responseElement(doc, ids[1], "select", "Full");
      for (FormFieldValue value : field.getValues()) {
        Element respItemElement = responseItemElement(doc, value.getLabel(), value.getId());
        respElement.appendChild(respItemElement);
      }
      responsesElement.appendChild(respElement);
      itemElement.appendChild(responsesElement);
    }
    if (field.getType().equals(FieldType.number) || field.getType().equals(FieldType.text)) {
      Element respElement = responseElement(doc, ids[1], "input", "");
      if (!field.getLabel().isEmpty() && field.getAttributes() != null
          && field.getAttributes().get("REPORT") != null
          && ("Y".equals(field.getAttributes().get("REPORT").toString()))) {
        Element labelElement = doc.createElement("label");
        labelElement.appendChild(doc.createTextNode(field.getLabel()));
        respElement.appendChild(labelElement);
      }
      //TODO Element respItemElement = responseItemElement(doc, field.getValues(), value.getId());
      responsesElement.appendChild(respElement);
      itemElement.appendChild(responsesElement);
    }
    return itemElement;
  }

  private void saveConsent(Database database, PatientStudy patStudy, String xmlString, boolean isSurveyDone) {
    patStudy.setContents(xmlString);
    updatePatientStudy(database, patStudy, isSurveyDone);
  }

  private SurveyType qualifies(Database database, Patient patient, SurveyRegistration survey) {
    if (patient == null) {
      return SurveyType.none;
    }

    // If they have not said OK to the reseach question skip it
    if (!patient.hasAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_RESEARCH)
        || patient.getAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_RESEARCH).getDataValue() == null) {
      return SurveyType.none;
    }
    if (attributeValue.N.toString().equals(getAttributeValue(patient, edu.stanford.registry.shared.Constants.ATTRIBUTE_RESEARCH))) {
      return SurveyType.none;
    }

    // If they are not a headache patient skip this questionnaire
    if (!patient.hasAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_HEADACHE)
        || patient.getAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_HEADACHE).getDataValue() == null) {
      return SurveyType.none;
    }
    if (!attributeValue.Y.toString().equals(getAttributeValue(patient, edu.stanford.registry.shared.Constants.ATTRIBUTE_HEADACHE))) {
      return SurveyType.none;
    }

    // Check if already marked for this questionnaire
    if (patient.hasAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_CHRONIC_MIGRAINE) &&
        patient.getAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_CHRONIC_MIGRAINE).getDataValue()
            != null) {
      // If marked 'Ask me again later' start the questionnaire
      if (attributeValue.A.toString().equals(getAttributeValue(patient, edu.stanford.registry.shared.Constants.ATTRIBUTE_CHRONIC_MIGRAINE))) {
        return SurveyType.initial;
      }
      // Otherwise skip it
      return SurveyType.none;
    }

    if (survey.getSurveyType().startsWith("Initial")) {
      return SurveyType.initial;
    }

    // See if they were just identified a headache patient (taking the headache initial survey)
    if (checkHeadacheSurvey(database, survey)) {
      return SurveyType.initial;
    }

    return SurveyType.none;
  }

  private boolean checkHeadacheSurvey(Database database, SurveyRegistration surveyRegistration) {
    SurveyQuery surveyQuery = new SurveyQuery(database, new SurveyDao(database), surveyRegistration.getSurveySiteId());
    SurveySystDao ssDao = new SurveySystDao(database);
    SurveySystem surveySystem = ssDao.getSurveySystem("edu.stanford.registry.server.survey.HeadacheSurveyService");
    Survey survey = surveyQuery.surveyBySurveyToken(surveyRegistration.getToken());
    Study study = ssDao.getStudy(surveySystem.getSurveySystemId(), "headache");
    SurveyStep step = survey.answeredStepByProviderSectionQuestion(surveySystem.getSurveySystemId().toString(),
        study.getStudyCode().toString(), "HeadacheQ0");
    // Look at the text of the first question to see if they just took an initial or follow up headache survey
    if (step != null && step.questionForm() != null && step.questionForm().getFields() != null) {
      for (FormField field: step.questionForm().getFields()) {
        if ("0:0:HEADACHE0".equals(field.getFieldId()) && field.getLabel() != null
            && field.getLabel().startsWith("Within the past year")) {
          return true;
        }
      }
    }
    return false;
  }

  private void setChronicMigraineAttribute(Database database, Patient patient, Enum<?> dataValue) {

    PatientAttribute pattribute = patient.getAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_CHRONIC_MIGRAINE);
    if (pattribute == null) {
      pattribute = new PatientAttribute(patient.getPatientId(),
          edu.stanford.registry.shared.Constants.ATTRIBUTE_CHRONIC_MIGRAINE, dataValue.toString(),
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
    // Add the question text
    switch (questionNumber) {
    case 1:
      fields.add(headingFormField("In the last 3 months (past 90 days),", questionNumber, 0));
      break;
    default:
      fields.add(headingFormField("In the last month (past 30 days),", questionNumber, 0));
      break;
    }
    fields.add(headingFormField(questionText[questionNumber], questionNumber, 1));
    // Add collapsible help and the input field (number, checkbox)
    switch (questionNumber) {
    case 1:
      fields.add(collapsibleHeadingFormField(questionNumber, 2,"question clarification",
          "If you do not remember the exact number of headache days, please give the best answer you can. If a headache lasted more than 1 day, count each day."));
      FormField field1 = factory.field().as();
      field1.setType(FieldType.number);
      field1.setFieldId(questionNumber + ":3:" + questionId() + questionNumber);
      field1.setRequired(false);
      field1.setMax("90");
      field1.setLabel("Days");
      field1.setAttributes(reportAttributes());
      fields.add(field1);
      break;
    case 2:
      fields.add(collapsibleHeadingFormField(questionNumber, 2, "question clarification",
          "If you do not remember the exact number of headache days, please give the best answer you can. If a headache lasted more than 1 day, count each day."));
      FormField field2 = factory.field().as();
      field2.setType(FieldType.number);
      field2.setFieldId(questionNumber + ":3:" + questionId() + questionNumber);
      field2.setRequired(false);
      field2.setMax("30");
      field2.setLabel("Days");
      field2.setAttributes(reportAttributes());
      fields.add(field2);
      break;
    case 3:
    case 4:
    case 5:
    case 6:
      fields.add(collapsibleHeadingFormField(questionNumber, 2, "question clarification",
        "If you have more than 1 type of headache, please answer for your most severe type."));
      fields.add(radiosFormField(questionNumber, 3, listOptions));
      break;
    case 7:
    case 8:
      fields.add(collapsibleHeadingFormField(questionNumber, 2, "question clarification",
          "Only count medications you take as needed to relieve your headaches."));
      FormField field7 = factory.field().as();
      field7.setType(FieldType.number);
      field7.setFieldId(questionNumber + ":3:" + questionId() + questionNumber);
      field7.setRequired(false);
      field7.setMax("30");
      field7.setLabel("Days");
      field7.setAttributes(reportAttributes());
      fields.add(field7);
      break;
    case 9:
      FormField field9 = factory.field().as();
      field9.setType(FieldType.number);
      field9.setFieldId(questionNumber + ":2:HEADACHE" + questionNumber);
      field9.setRequired(false);
      field9.setMax("30");
      field9.setLabel("Days");
      fields.add(field9);
      break;
    case 10:
      FormField field10 = factory.field().as();
      field10.setType(FieldType.number);
      field10.setFieldId(questionNumber + ":2:HEADACHE" + questionNumber);
      field10.setRequired(false);
      field10.setMax("30");
      field10.setLabel("Days");
      fields.add(field10);
      break;
    case 11:
      fields.add(radiosFormField(questionNumber, 2, listOptions));
      break;
    case 12:
      fields.add(radiosFormField(questionNumber, 2, listOptions));
      break;
    }
    return fields;
  }

  private Map<String, String> reportAttributes() {
    Map<String, String> attributes = new HashMap<>();
    attributes.put("REPORT", "N");
    return attributes;
  }

  @Override
  protected String questionId() {
    return "CHRONICMIGRAINE";
  }
  private String getAttributeValue(Patient patient, String attributeName) {
    if (patient != null && patient.hasAttribute(attributeName)
        && patient.getAttribute(attributeName) != null
        && patient.getAttribute(attributeName).getDataValue() != null) {
      return patient.getAttribute(attributeName).getDataValue().toUpperCase();
    }
    return "";
  }
}
class ConsentQuestion {
  SurveyFactory factory;
  ConsentQuestion(SurveyFactory factory) {
    this.factory = factory;
  }
  NextQuestion getQualifyingQuestion(PatientStudyExtendedData patStudy, String questionId) {
    AutoBean<CollapsibleRadiosetQuestion> crbean = factory.collapsibleRadiosetQuestion();
    CollapsibleRadiosetQuestion crquestion = crbean.as();
    crquestion.setTitle1(consentTitle1String);
    crquestion.setTitle2(consentTitle2String);
    crquestion.setCollapsibleHeading(collapsibleLabel);
    crquestion.setCollapsibleContent(collapsibleContent);
    ArrayList<String> choices = new ArrayList<>();
    choices.add(consentAnswerYes);
    choices.add(consentAnswerNo);
    choices.add(consentAnswerLater);
    crquestion.setChoices(choices);
    NextQuestion next = new NextQuestion();
    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setQuestionType(QuestionType.collapsibleRadioset);
    displayStatus.setQuestionId(questionId+"Q0");
    displayStatus.setSurveyProviderId(Integer.toString(patStudy.getSurveySystemId()));
    displayStatus.setSurveySectionId(Integer.toString(patStudy.getStudyCode()));
    next.setDisplayStatus(displayStatus);
    next.setQuestion(crbean);
    return next;
  }

  String getItemResponse(String answer) {
    String itemResponse = "";
    if (consentAnswerYes.equals(answer)) {
      itemResponse = "0";
    } else if (consentAnswerNo.equals(answer)) {
      itemResponse = "1";
    } else if (consentAnswerLater.equals(answer)) {
      itemResponse = "2";
    }
    return itemResponse;
  }

  String getAnsweredXmlString(String answer) {
    String today = ServerUtils.getInstance().parseXMLDate(new Date());
    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\"?><Form questionsPerPage=\"1\" ");
    xml.append("DateFinished=\"").append(today).append("\" ");
    xml.append("DateStarted=\"").append(today).append("\" ");
    xml.append("Description=\"chronicMigrain\">");
    xml.append("<Items><Item Class=\"surveyQuestionCollapsible\" Align=\"Horizontal\" RequiredMax=\"1\" RequiredMin=\"1\" ItemScore=\"\" ");
    xml.append("ItemResponse=\"" + getItemResponse(answer) + "\" Order=\"1\">");
    xml.append("<label>").append(collapsibleLabel).append("</label>");
    xml.append("<Description><![CDATA[").append(consentTitle1String).append("]]></Description>");
    xml.append("<Description><![CDATA[").append(consentTitle2String).append("]]></Description>");
    xml.append("<CollapsibleContent><![CDATA[").append(collapsibleContent).append("]]></CollapsibleContent>");
    xml.append("<Responses>");
    xml.append("<Response Class=\"registrySurvey\" Description=\"").append(consentAnswerYes).append("\" Order=\"0\" Type=\"radio\"><Scores><Score value=\"0\"/></Scores></Response>");
    xml.append("<Response Class=\"registrySurvey\" Description=\"").append(consentAnswerNo).append("\" Order=\"1\" Type=\"radio\"><Scores><Score value=\"1\"/></Scores></Response>");
    xml.append("<Response Class=\"registrySurvey\" Description=\"").append(consentAnswerYes).append("\" Order=\"2\" Type=\"radio\"><Scores><Score value=\"2\"/></Scores></Response>");
    xml.append("</Responses></Item></Items></Form>");

    return xml.toString();
  }

  String getYesAnswer() {
    return consentAnswerYes;
  }
  private String consentTitle1String = "As part of a research study, we would like to ask you 12 additional questions about your headaches and share some of your medical information with our research team as described below.";
  private String consentTitle2String = "Would you like to participate?";
  private String consentAnswerYes = "Yes, ask me 12 questions and share my data";
  private String consentAnswerNo = "No, skip this section";
  private String consentAnswerLater = "Ask me again later";
  private String collapsibleLabel = "More Information";

  private String collapsibleContent = "<p><b>DESCRIPTION:</b> You are invited to participate in a research study that will help us "
      + "validate screening tools for an easier and more accurate diagnosis of chronic migraine. "
      + "You will be asked to complete 12 questions about the symptoms of your headaches, "
      + "medication you use, and how the headaches interfere with your routine.</p>\n"
      + "<p><b>TIME INVOLVEMENT:</b>  Your participation will take approximately 5 minutes."
      + "<p><b>RISKS AND BENEFITS:</b>  There are no known risks associated with this study. "
      + "The benefits which may reasonably be expected to result from this study are that we may "
      + "have better tools to diagnose chronic migraine. We cannot and do not guarantee or "
      + "promise that you will receive any benefits from this study. Your decision whether or not "
      + "to participate in this study will not affect your medical care.</p>\n"
      + "<p><b>PAYMENTS:</b>  You will receive no payment for your participation.</p>\n"
      + "<p><b>SUBJECT'S RIGHTS:</b>  If you have read this form and have decided to participate in this "
      + "project, please understand your participation is voluntary and you have the right to "
      + "withdraw your consent or discontinue participation at any time without penalty or loss of "
      + "benefits to which you are otherwise entitled.  The alternative is not to participate.  You "
      + "have the right to refuse to answer particular questions.  Your individual privacy will be "
      + "maintained in all published and written data resulting from the study.</p>\n"
      + "<p><b>Authorization To Use Your Health Information For Research Purposes</b></p>\n"
      + "<p>Because information about you and your health is personal and private, it "
      + "generally cannot be used for research purposes without your authorization. "
      + "By consenting to participate in this research study by following the "
      + "instructions at the end of this form, you are providing your authorization.  "
      + "The form is intended to inform you about how your health information will "
      + "be used or disclosed in the study.  Your information will only be used in "
      + "accordance with this authorization form and the informed consent form and "
      + "as required or allowed by law.  Please read it carefully before indicating your consent.</p>\n"
      + "<p>The purpose of this research study is to validate diagnostic tools for chronic "
      + "migraine. Your health information, including but not limited to, information "
      + "from your headache appointment, including headache history and "
      + "neurological exam and your responses to the surveys, could be utilized in this study.</p>\n"
      + "<p>You do not have to provide your authorization.  But if you do not, you will "
      + "not be able to participate in this research study. Agreeing to this will not "
      + "impact your clinical care.</p>\n"
      + "<p>If you decide to participate, you are free to withdraw your authorization "
      + "regarding the use and disclosure of your health information (and to "
      + "discontinue any other participation in the study) at any time.  After any "
      + "revocation, your health information will no longer be used or disclosed in "
      + "the study, except to the extent that the law allows us to continue using your "
      + "information (e.g., necessary to maintain integrity of research).  If you wish "
      + "to revoke your authorization for the research use or disclosure of your health "
      + "information in this study, you must write to: Dr. Nada Hindiyeh at "
      + "nhindiye@stanford.edu</p>\n"
      + "<p>Your health information related to this study, may be used or disclosed in "
      + "connection with this research study, including, but not limited to, your name, "
      + "contact information, current medical status and medical history as you report on the surveys.<p>\n"
      + "<p>The following parties are authorized to use and/or disclose your health information in connection with this research study:\n"
      + "            <ul>"
      + "              <li>The Protocol Director Nada Hindiyeh, MD, and Sheena Aurora, MD</li>"
      + "              <li>The Stanford University Administrative Panel on Human Subjects in Medical Research and any other unit of Stanford University as necessary</li>"
      + "              <li>Research Staff</li>"
      + "            </ul>\n"
      + "          </p>\n"
      + "The parties listed in the preceding paragraph may disclose your health "
      + "information to the following persons and organizations for their use in "
      + "connection with this research study: \n"
      + "            <ul>"
      + "              <li>The Office for Human Research Protections in the U.S. Department of Health and Human Services</li>"
      + "              <li>Allergan</li>"
      + "              <li>Vedanta Research</li>"
      + "            </ul>"
      + "          </p>"
      + "<p>Your information may be re-disclosed by the recipients described above, if "
      + "they are not required by law to protect the privacy of the information.</p>\n"
      + "<p>Your authorization for the use and/or disclosure of your health information "
      + "will end on December 31, 2050 or when the research project ends, whichever is earlier.</p>\n"
      + "<p><b>CONTACT INFORMATION:</b></p>"
      + "<p><i>Questions:</i>  If you have any questions, concerns or complaints about this research, its "
      + "procedures, risks and benefits, contact the Protocol Director, Dr. Nada Hindiyeh or Dr. Sheena Aurora. "
      + "You may contact them now or later at (650) 723-5184.</p>\n"
      + "<p><i>Independent Contact:</i>  If you are not satisfied with how this study is being conducted, or "
      + "if you have any concerns, complaints, or general questions about the research or your rights as a participant,"
      + " please contact the Stanford Institutional Review Board (IRB) to speak to someone independent of the research "
      + "team at (650)-723-2480 or toll free at 1-866-680-2906.  You can also write to the Stanford IRB, Stanford University,"
      + " 3000 El Camino Real, Five Palo Alto Square, 4th Floor, Palo Alto, CA 94306.</p>\n\n"
      + "<p><b>Please print a copy of this consent form for your records.</p>\n\n"
      + "<p><b>If you agree to participate in this research, please complete the following survey.</p>\n";
}



