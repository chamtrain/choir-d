package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.utils.SquareDocumentationBuilder;
import edu.stanford.registry.server.utils.StringUtils;
import edu.stanford.registry.shared.ApptRegistration;
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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.xml.sax.SAXException;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.Sql;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;


public class HeadacheSurveyService extends NamedSurveyService implements CustomSurveyServiceIntf, SurveyToSquareIntf   {
  public final static String studyName = "headache";

  private final String surveySystemName = "edu.stanford.registry.server.survey.HeadacheSurveyService";
  private static final Logger logger = LoggerFactory.getLogger(HeadacheSurveyService.class);
  private SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  LinkedHashMap<String, FieldType> columns = null;

  private enum attributeValue { Y, N, Q }

  private final String[] questionTextInitial = { "Within the past year have you had headaches or facial pain that limit you from working, studying or doing what you need to do?",
      "Does light or sound bother you when you have a headache (a lot more than when you don't have a headache)?",
      "Do you feel nauseated or sick to your stomach when you have a headache?",
      "How old were you when you first started having headaches of any kind?",
      "When did your most bothersome headaches start?", "In the last 3 months,",
      "Do you have more than one type of headache?", "Describe the quality of your pain for your most severe headache (select all that apply)",
      "How long does your most severe headache last?", "Are greater than 90% of your headaches one sided?",
      "Which side?", "How long does it take for your headache to get to maximal intensity?",
      "What is the most common location of your pain (select all that apply)?", "On average how many headache free days do you have in a month?",
      "Does routine physical activity worsen your headache?", "Are you restless during your headaches?",
      "Do you have any of the following with your headaches?", "With the headaches do you have (select all that apply)",
      "Have you been able to identify triggers for your headaches?","What are your most common triggers",
      "Do you have aura with your headache?","What kind of aura do you have?",
      "Have you been given triptan medications for your headaches (Sumatriptan, Imitrex, Treximet, Rizatriptan, Maxalt, Eletriptan, Relpax, Almotriptan, Axert,  Naratriptan, Amerge, Frovatriptan, Frova, Zolmitriptan,  Zomig)?",
      "Which of these triptan medications have you taken",
      "Have you been given opioid medication (Stadol, Butorphanol, Codeine, Tylenol #3,#4, Actiq, Fentanyl lozenges, Duragesic, Fentanyl patch, Vicodin, Lortab, Norco, Hydrocodone, Dilaudid, Hydromorphone, Demerol, Meperidine,"
        +"Methadone, MS Contin, Avinza, Kadian, Morphine, Percocet, Roxycodone, Oxycontin, Oxycodone, Opana, Oxymorphone, Nucynta, Tapentadol, Ultram, Tramadol)?",
      "Were the opioid medications effective for your headaches?",
      "In the last 3 months, "
      };
  private final String[] questionTextFollowUp = {"In the last 3 months",
      "How many days are you using acute medications (Triptans, Opiates, Ergots, Acetaminophen, NSAIDS, Midrin, Fioricet, Excedrin)?",
      "Since our last clinic visit, have you been taking a daily medicine to help prevent your headaches?",
      "Since our last clinic visit, have you been taking a narcotic/opioid containing medicine for breakthrough pain?",
      "Have you had any side effects from the headache medicines you are currently taking?", "How would you rate your:",
      "Since our last clinic visit, has there been a reduction in your headache:",
      "How many urgent care or emergency room visits have you made for management of a severe headache since our last clinic visit?",
      "What is the most common location of your pain (select all that apply)?"};
  private final String[][] questionText = { questionTextInitial, questionTextFollowUp };
  private final String[] yesNoAnswers = { "Yes", "No"};
  private final String[] rangesBy5 = {"Less than 5","5-10","10-15","15-20","Greater than 20"};
  private final String[] qualOptions = {"Throbbing","Pulsating","Sharp","Pressure", "Piercing", "Shooting", "Cramping", "Stinging","Tingling",
      "Pounding", "Burning", "Heavy", "Splitting", "Tightness", "Dull"};
  final String[] lastOptions = {"Less than 1 hour", "1-4 hours", "Greater than 4 hours"};
  final String[] sidesOptions = {"Left", "Right", "Shifts sides"};
  final String[] timeOptions = {"Seconds", "Minutes", "Hours"};
  final String[] locationOptions = {"Front of head", "Temples", "Back of head", "Behind the eyes", "Neck"};
  final String[] physOptions = {"Yes", "No", "Sometimes", "Don't know"};
  final String[] cond1Options = {"Nausea", "Vomiting", "Sensitivity to light", "Sensitivity to sound", "Sensitivity to smells", "Sensitivity to heat"};
  final String[] cond2Options = {"Runny nose", "Tearing of the eye", "Droopy eye", "Redness or irritation of eye"};
  final String[] cond2SubOptions = {"Always on one side, never on the other side", "Always on one side but can shift sides", "On both sides", "Don't know"};
  final String[] triggerOptions = {"Food","Skipping meals","Dehydration","Alcohol", "Stress", "Let down from stress", "High altitude", "Menstrual period","Too much or too little caffeine",
      "Bright lights", "Coughing", "Bearing down", "Exercise"};
  final String[] auraOptions = {"Visual","Aural (hearing is affected)","Sensory (numbness or tingling in any part of the body", "Motor (weakness)", "None of the above"};
  final String[] sleepOptions = { "Good", "Fair", "Poor" };
  final String[] nutExOptions = { "Regular", "Fair", "Poor" };
  final String[] triptans = { "Sumatriptan", "Imitrex", "Treximet", "Rizatriptan", "Maxalt", "Eletriptan", "Relpax", "Almotriptan", "Axert", "Naratriptan", "Amerge", "Frovatriptan","Frova", "Zolmitriptan", "Zomig"};
  final String[] effective = { "Effective", "Not effective"};


  public HeadacheSurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
                                     String answerJson) {
    // missing data or survey is already done
    if (patStudyExtended == null || patStudyExtended.getDtChanged() != null)  {
      return null;
    }
    SurveyRegistration surveyRegistration = null;
    PatientStudy patStudy = null;
    AssessDao assessDao = new AssessDao(database, siteInfo);
    try { /* look up the survey to get the type */
      surveyRegistration = assessDao.getSurveyRegistrationByRegId(patStudyExtended.getSurveyRegId());
      if (surveyRegistration == null) { /* doesn't exist ! */
        throw new DataException("Survey Registration not found for surveyRegId:" + patStudyExtended.getSurveyRegId());
      }
    } catch (Exception e) {
      logger.error(e.toString(), e);
      throw new DataException(e.getMessage());
    }
    SurveyType headacheSurveyType = qualifies(database, patStudyExtended.getPatient(), surveyRegistration);
    /**
     * If the patient doesn't qualify for this survey write an empty form to the database,
     */
    if (headacheSurveyType.equals(SurveyType.none)) {
      patStudyExtended.setContents(emptyForm);
      updateStudy(database, patStudyExtended, true);
      patStudyExtended.setDtChanged(new Date()); // we do this so when called again we quit
      NextQuestion next = new NextQuestion();
      DisplayStatus displayStatus = factory.displayStatus().as();
      displayStatus.setQuestionType(QuestionType.skip);
      displayStatus.setSurveyProviderId(Integer.toString(getSurveySystemId(database)));
      displayStatus.setSurveySectionId(Integer.toString(patStudyExtended.getStudyCode()));
      next.setDisplayStatus(displayStatus);
      return next;
    }

    /* Lookup the patient study to get it with contents */
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    try {
      patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
      if (patStudy == null) { /* doesn't exist ! */
        throw new DataException("Patient Study not found for study " + patStudyExtended.getStudyCode() + " token " + patStudyExtended.getToken());
      }
    } catch (Exception e) {
      logger.error(e.toString(), e);
      throw new DataException(e.getMessage());
    }
    int nextQuestionNumber=-1;
    int lastQuestionNumber = 0;
    if (submitStatus != null) {
      FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
      logger.trace("Processing {} answers", formAnswer.getFieldAnswers().size());
      Map<String, Map<String, FormFieldAnswer>> itemOrderToResponseOrderToField = new HashMap<>();
      int firstQuestion = 99;
      for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
        /* The format of the fieldId is "ItemOrder:ResponseOrder:Ref" */
        String[] ids = fieldAnswer.getFieldId().split(":");
        String itemOrder = ids[0];
        logger.trace("Processing fieldAnswer {} (itemOrder={})", fieldAnswer.getFieldId(), itemOrder);
        try {
          Integer questionNumber = Integer.parseInt(itemOrder);
          if (questionNumber > lastQuestionNumber) lastQuestionNumber = questionNumber;
          if (questionNumber < firstQuestion) firstQuestion = questionNumber;
        } catch (NumberFormatException nfe) {
          logger.error("Number format exception getting questionNumber from fieldId ", fieldAnswer.getFieldId(), nfe);
        }
        String responseOrder = ids[1];
        Map<String, FormFieldAnswer> responseOrderToField = itemOrderToResponseOrderToField.get(itemOrder);
        if (responseOrderToField == null) {
          responseOrderToField = new HashMap<>();
          itemOrderToResponseOrderToField.put(itemOrder, responseOrderToField);
        }
        responseOrderToField.put(responseOrder, fieldAnswer);
      }
      boolean surveyIsDone = false;
      if (headacheSurveyType.equals(SurveyType.initial) && lastQuestionNumber == 0 && "1".equals(getSelection(lastQuestionNumber, itemOrderToResponseOrderToField)) ) {
        /* Answered NO to question 1: Mark them as NOT a Headache patient and stop the survey */
        setHeadacheAttribute(database, patStudyExtended.getPatient(), attributeValue.N);
        surveyIsDone = true;
      }
      ArrayList<FormQuestion> questions = new ArrayList<>();
      for (int q=firstQuestion; q<=lastQuestionNumber; q++) {
        questions.add(getFormQuestion(q, headacheSurveyType).as());
      }
      lastQuestionNumber = skipConditionalQuestions(database, patStudyExtended, surveyRegistration, headacheSurveyType, lastQuestionNumber);
      if (lastQuestionNumber >= questionText[headacheSurveyType.equals(SurveyType.initial) ? 0 : 1].length - 1) {
        surveyIsDone = true;
      }
      if (headacheSurveyType.equals(SurveyType.initial) && lastQuestionNumber == 14
          && askedCOPCSHA(database, patStudyExtended.getToken())) {
        surveyIsDone = true; // skip remaining duplicate questions
      }
      logger.trace("Saving questions {} - {} done: {}", firstQuestion, lastQuestionNumber, surveyIsDone);
      saveResponses(database, patStudyExtended, patStudy, itemOrderToResponseOrderToField, questions, formAnswer, surveyIsDone);
      if (surveyIsDone) return null;
      nextQuestionNumber = lastQuestionNumber;
    } else {
      if (patStudy.getContents() != null) {
        // get lastQuestionNumber from the survey in the database
        SurveyQuery surveyQuery = new SurveyQuery(database, new SurveyDao(database), patStudy.getSurveySiteId());
        Survey survey = surveyQuery.surveyBySurveyToken(patStudy.getToken());
        for (SurveyStep step : survey.answeredStepsByProvider(Integer.toString(getSurveySystemId(database)))) {
          if (step != null && step.answer() != null && step.answer().getSubmitStatus() != null) {
            String questionIdString = step.answer().getSubmitStatus().getQuestionId();
            String questionId = "0";
            if (questionIdString.contains("HeadacheQ")) questionId = questionIdString.substring(9);
            logger.trace("last question was {}", questionId);
            if (questionId != null && Integer.parseInt(questionId) > lastQuestionNumber) {
              lastQuestionNumber = Integer.parseInt(questionId);
            }
          }
        }
        lastQuestionNumber = skipConditionalQuestions(database, patStudyExtended, surveyRegistration, headacheSurveyType, lastQuestionNumber);
        nextQuestionNumber = lastQuestionNumber;
      }
    }
    nextQuestionNumber++;

    if (headacheSurveyType.equals(SurveyType.initial)) {
      switch (nextQuestionNumber) {
      case 1:
        if (askedCOPCSHA(database, patStudyExtended.getToken())) { // skip duplicate questions 1-2 asked in the COPCS
          nextQuestionNumber = 3;
        }
        break;
      case 5:
        if (askedCOPCSHA(database, patStudyExtended.getToken())) { // skip duplicate question 5 asked in the COPCS
          nextQuestionNumber = 6;
        }
        break;
      case 7:
      case 8:
      case 9:
      case 10:
      case 11:
        if (askedCOPCSHA(database, patStudyExtended.getToken())) { // skip duplicate questions 7-11 asked in the COPCS
          nextQuestionNumber = 12;
        }
        break;
      default:
        break; // Ask questions 0,3,4,6,12,13,14
      }
    }
    /* Send back the question */
    NextQuestion next = new NextQuestion();
    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setQuestionType(QuestionType.form);
    displayStatus.setSurveyProviderId(Integer.toString(getSurveySystemId(database)));
    displayStatus.setSurveySectionId(Integer.toString(patStudyExtended.getStudyCode()));
    displayStatus.setQuestionId("HeadacheQ" + nextQuestionNumber);
    next.setDisplayStatus(displayStatus);

    // if COPCSHA was asked don't include the conditional question 7
    if (nextQuestionNumber == 6 && headacheSurveyType.equals(SurveyType.initial)
        && askedCOPCSHA(database, patStudyExtended.getToken())) {
      next.setQuestion(getFormQuestion(nextQuestionNumber, headacheSurveyType, true));
      return next;
    }
    next.setQuestion(getFormQuestion(nextQuestionNumber, headacheSurveyType));
    return next;
  }

  private int skipConditionalQuestions(Database database, PatientStudyExtendedData patStudyExtended, SurveyRegistration surveyRegistration, SurveyType headacheSurveyType,
      int lastQuestionNumber) {
    if (!headacheSurveyType.equals(SurveyType.initial)) {
      return lastQuestionNumber;
    }

    /* When a question with a conditional 2nd question returns alone skip the next (conditional) question */
    if ((lastQuestionNumber == 6 || lastQuestionNumber == 9 || lastQuestionNumber == 18 || lastQuestionNumber == 20 || lastQuestionNumber == 22 || lastQuestionNumber == 24)) {
      lastQuestionNumber++;
    }

    if (!surveyRegistration.getSurveyType().startsWith("Initial")) { // The rest are only if the survey is an Initial.xxxx
      return lastQuestionNumber;
    }

    /* Skips asking the headache meds question if they've picked any triptans on the standard medsHeadache survey question */
    if (lastQuestionNumber == 21 && checkMeds(database, patStudyExtended.getPatient(),
        patStudyExtended.getSurveySiteId(), patStudyExtended.getToken(), "medsHeadache", triptans)) {
      logger.trace("Skipping question {} patient has already selected a triptans medication", questionTextInitial[23]);
      lastQuestionNumber = 23;
    }

    /* Skips asking the opioid question when type is initial as it was already asked on the standard medsOpioid survey. */
    if (lastQuestionNumber == 23) {
      lastQuestionNumber = 24;
    }

    /* If they didn't select the specific opioid medications listed in our question 23 on the initial survey medsOpioid skip the opioid followup. */
    if (lastQuestionNumber == 24 && !checkMeds(database, patStudyExtended.getPatient(),
        patStudyExtended.getSurveySiteId(), patStudyExtended.getToken(), "medsOpioid", null)) {
      lastQuestionNumber = 25;
    }
    return lastQuestionNumber;
  }



  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(Database database, PatientStudy study, User user) {
    return getRegistrySurvey(database, study, user);
  }

  @Override
  public String getAssessments(Database database, int version) throws Exception {

    StringBuilder myStudies = new StringBuilder();
    myStudies.append("<forms>");
    myStudies.append("<form OID=\"");
    String sql = "select studyCode,  studyDescription, title from study where survey_system_id = "
    + mySurveySystem.getInstance(database, getSurveySystemName()).getSurveySystemId();

    final ArrayList<Study> studies =  database.toSelect(sql).query(new RowsHandler<ArrayList<Study>>() {
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
    return "Headache Survey";
  }

  @Override
  public void setValue(String value) {
  }

  private AutoBean<FormQuestion > getFormQuestion(int questionNumber, SurveyType headacheSurveyType) {
    return getFormQuestion(questionNumber, headacheSurveyType, false);
  }

  private AutoBean<FormQuestion> getFormQuestion(int questionNumber, SurveyType headacheSurveyType, boolean withoutConditionalQuestions) {
    if (questionNumber > questionText[headacheSurveyType.equals(SurveyType.initial) ? 0 : 1].length - 1) {
      logger.error("We don't have {} questions! ", questionNumber );
      return null;
    }
    AutoBean<FormQuestion> bean = factory.formQuestion();
    FormQuestion question = bean.as();

    question.setFields(getFormFields(questionNumber, headacheSurveyType, withoutConditionalQuestions));
    return bean;
  }

  private void saveResponses(Database database, PatientStudyExtendedData patStudyExt, PatientStudy patStudy, Map<String, Map<String, FormFieldAnswer>> itemOrderToResponseOrderToField ,
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
              logger.trace("looking up key:{} item {} response {}", fieldAns.getKey(), ids[0], ids[1]);
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
      updateStudy(database, patStudy, isSurveyDone);
      if (isSurveyDone && attributeValue.Q.toString().equals(patient.getAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_HEADACHE).getDataValue())) {
        setHeadacheAttribute(database, patient, attributeValue.Y);
      }
    } catch (Exception e) {
      logger.error("Error creating xml for headache study {} for token {}", patStudyExt.getStudyCode(), patStudyExt.getToken(), e);
    }
  }

  private Element addFieldToItemElement(Document doc, Element itemElement, FormField field) throws Exception {
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
    if (field.getType().equals(FieldType.heading)) {
      Element descElement = getDescription(doc, field.getLabel());
      itemElement.appendChild(descElement);
    }
    if (field.getType().equals(FieldType.radios)) {
      String fieldLabel = "";
      if (field.getLabel() != null && !field.getLabel().isEmpty() && field.getAttributes() != null
          && field.getAttributes().get("REPORT") != null
          && ("Y".equals(field.getAttributes().get("REPORT")))) {
        fieldLabel = field.getLabel() + " "; // prepend the field label
      }
      Element respElement = getResponse(doc, ids[1], "select1", "Full");
      for (FormFieldValue value : field.getValues()) {
        Element respItemElement = getResponseItem(doc, fieldLabel + value.getLabel(), value.getId());
        respElement.appendChild(respItemElement);
      }

      responsesElement.appendChild(respElement);
      itemElement.appendChild(responsesElement);
    }
    if (field.getType().equals(FieldType.checkboxes)) {
      Element respElement = getResponse(doc, ids[1], "select", "Full");
      for (FormFieldValue value : field.getValues()) {
        Element respItemElement = getResponseItem(doc, value.getLabel(), value.getId());
        respElement.appendChild(respItemElement);
      }
      responsesElement.appendChild(respElement);
      itemElement.appendChild(responsesElement);
    }
    if (field.getType().equals(FieldType.number) || field.getType().equals(FieldType.text)) {
      Element respElement = getResponse(doc, ids[1], "input", "");
      if (!field.getLabel().isEmpty() && field.getAttributes() != null
          && field.getAttributes().get("REPORT") != null
          && ("Y".equals(field.getAttributes().get("REPORT")))) {
        Element labelElement = doc.createElement("label");
        labelElement.appendChild(doc.createTextNode(field.getLabel()));
        respElement.appendChild(labelElement);
      }
      //TODO Element respItemElement = getResponseItem(doc, field.getValues(), value.getId());
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
      patStudy = new PatientStudy(siteId);
      patStudy.setExternalReferenceId("");
      patStudy.setMetaVersion(0);
      patStudy.setPatientId(pat.getPatientId());
      patStudy.setStudyCode(study.getStudyCode());
      patStudy.setSurveySystemId(study.getSurveySystemId());
      patStudy.setToken(tok.getToken());
      patStudy.setOrderNumber(order);
      patStudy = patStudyDao.insertPatientStudy(patStudy);
    }
  }

  private SurveyType qualifies(Database database, Patient patient, SurveyRegistration survey) {
    if (patient == null) {
      return SurveyType.none;
    }

    /** See if already marked */
    if (patient.hasAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_HEADACHE) &&
        patient.getAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_HEADACHE) != null) {
      String dataValue = patient.getAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_HEADACHE).getDataValue();
      if (dataValue != null && attributeValue.N.toString().equals(dataValue.toUpperCase())) {
        return SurveyType.none;
      }
      if (dataValue != null && attributeValue.Y.toString().equals(dataValue.toUpperCase())) {
        return SurveyType.followup;
      }
      if (dataValue != null && attributeValue.Q.toString().equals(dataValue.toUpperCase())) {
        return SurveyType.initial;
      }
    }

    logger.trace("Patient didn't qualify by attribute, checking visit type");
    /** Check the visit type is a headache appointment */
    if (survey != null) {
      AssessDao assessDao = new AssessDao(database, siteInfo);
      ApptRegistration appt = assessDao.getApptRegistrationBySurveyRegId(survey.getSurveyRegId());
      if ((appt != null) && (appt.getVisitType() != null)) {
        String visitType = appt.getVisitType();
        if ("NHA".equalsIgnoreCase(visitType) || "RHA".equalsIgnoreCase(visitType)) {
          setHeadacheAttribute(database, patient, attributeValue.Q);
          return SurveyType.initial;
        }
      }
    }

    logger.trace("Patient didn't qualify by visit type, checking body map");
    /** See if head areas were selected on the bodymap */
    if (checkBodymap(database, survey.getSurveyRegId(), survey.getSurveySiteId())) {
      setHeadacheAttribute(database, patient, attributeValue.Q);
      return SurveyType.initial;
    }

    /** On initial surveys see if they chose any headache medications and selected "Still taking" */
    if (survey.getSurveyType().startsWith("Initial")) {
      logger.trace("Patient still didn't qualify, but is taking an initial survey so checking headache meds");
      if (checkMeds(database, patient, survey.getSurveySiteId(), survey.getToken(), "medsHeadache", triptans)) {
        setHeadacheAttribute(database, patient, attributeValue.Q);
        return SurveyType.initial;
      }
    }

    logger.trace("Patient didn't qualify. Returning survey type of none");
    return SurveyType.none;
  }

  /**
   * Check the headache medications selected, if any are 'Still Taking' returns true
   */
  private boolean checkMeds(Database database, Patient patient, Long siteId, String token,
                            String surveyName, String[] medNames) {
    SurveySystDao ssDao = new SurveySystDao(database);
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
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
        logger.trace("{} questions returned for {} study", questions.size(), surveyName);
        for (SurveyQuestionIntf question : questions) {
          ArrayList<SurveyAnswerIntf> ans = question.getAnswers(true);
          if (med) {
            if (ans != null && ans.size() > 0) {
              SurveyAnswerIntf answer = ans.get(0);
              if (answer.getType() == Constants.TYPE_SELECT) {
                SelectElement select = (SelectElement) answer;
                ArrayList<SelectItem> selectItems = select.getSelectedItems();
                logger.trace("with {} selected items", selectItems.size() );
                for (SelectItem item : selectItems) {
                  medNameTaking = item.getValue().toLowerCase();
                  // logger.debug("found med named " + medNameTaking + " for question " + q);
                }
              }
            }
            med = false;
          } else {
            if (ans != null && ans.size() > 0 && medNameTaking != null) {
              SurveyAnswerIntf answer = ans.get(0);
              if (answer.getType() == Constants.TYPE_SELECT) {
                SelectElement select = (SelectElement) answer;
                ArrayList<SelectItem> selectItems = select.getSelectedItems();
                for (SelectItem item : selectItems) {
                  if ("Still taking".equals(item.getLabel())) {
                    if (medNames == null) {
                      return true;
                    } else {
                      for (String medName1 : medNames) {
                        String medName = medName1.toLowerCase();
                        if (medNameTaking != null && medNameTaking.contains(medName)) {
                          logger.trace( "{} contains {} returning true", medNameTaking, medName);
                          return true;
                        }
                      }
                    }
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
       logger.error("checkMeds caught error", e);
     }
    return false;
  }

  /** Check the bodymap areas selected on this survey for the head */
  private boolean checkBodymap(Database database, Long surveyRegId, Long siteId) {
    String[] headAreas = {"101","102","103","104","201","202","203","204"};
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    ArrayList<PatientStudyExtendedData> bodymaps = 
        patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(surveyRegId, "bodymap");
    if (bodymaps != null && bodymaps.size() > 0) {
      for (PatientStudyExtendedData patStudy : bodymaps) {
        if (patStudy.getContents() != null) {
          try {
            Document doc = ScoreService.getDocument(patStudy);
            NodeList itemList = doc.getElementsByTagName(Constants.ITEM);
            if (itemList != null && itemList.getLength() > 0) {
              for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
                Element itemNode = (Element) itemList.item(itemInx);
                String itemResponse = itemNode.getAttribute(Constants.ITEM_RESPONSE);
                if (itemResponse != null && itemResponse.trim().length() > 0) {
                  String[] responses = itemResponse.split(",");
                  for (String area : responses) {
                    for (String headArea : headAreas) {
                      if (headArea.equals(area.trim())) {
                        return true;
                      }
                    }
                  }
                }
              }
            }
          } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error("checkMBodymap caught error", e);
          }
        }
      }
    }

    return false;
  }

  private boolean askedCOPCSHA(Database database, String token) {
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    ArrayList<PatientStudyExtendedData> copcs = patStudyDao.getPatientStudyExtendedDataByToken(token);
    PatientStudyExtendedData data = null;
    for (PatientStudyExtendedData patientStudyExtendedData : copcs) {
      if (patientStudyExtendedData.getStudyDescription().startsWith("COPCSHA@")) {
        data = patientStudyExtendedData;
      }
    }
    if (data == null) {
      return false;
    }

    SurveyQuery surveyQuery = new SurveyQuery(database, new SurveyDao(database), siteInfo.getSiteId());
    Survey survey = surveyQuery.surveyBySurveyToken(token);
    SquareHelper helper = new SquareHelper(siteInfo, "");
    String providerId = helper.getProviderId(database, "COPCSService");
    SurveyStep step = survey.answeredStepByProviderSectionQuestion(providerId, String.valueOf(data.getStudyCode()), "Order2");

    if (step != null) {
      Integer ans = helper.selectedFieldInt(step, "2:0:HACountDays90");
      if (ans != null && ans >= 45) {
        return true;
      }
    }
    step = survey.answeredStepByProviderSectionQuestion(providerId, String.valueOf(data.getStudyCode()), "Order3");
    if (step != null) {

      Integer ans = helper.selectedFieldInt(step, "3:0:HACountDays30");
      return ans != null && ans >= 15;
    }
    return false;

  }


  private void setHeadacheAttribute(Database database, Patient patient, Enum<?> dataValue) {

    PatientAttribute pattribute = patient.getAttribute(edu.stanford.registry.shared.Constants.ATTRIBUTE_HEADACHE);
    if (pattribute == null) {
        pattribute = new PatientAttribute(patient.getPatientId(),
        edu.stanford.registry.shared.Constants.ATTRIBUTE_HEADACHE, dataValue.toString(),
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

  private ArrayList<FormField> getFormFields(int questionNumber, SurveyType headacheSurveyType) {
    return getFormFields(questionNumber, headacheSurveyType, false);
  }

  private ArrayList<FormField> getFormFields(int questionNumber, SurveyType headacheSurveyType, boolean withoutConditionalQuestions) {
    ArrayList<FormField> fields = new ArrayList<>();
    int headacheSurveyIndex = headacheSurveyType.equals(SurveyType.initial) ? 0 : 1;
    fields.add(headingField(headacheSurveyIndex, questionNumber));
    switch (headacheSurveyIndex) {
    case 0: // Initial
      switch (questionNumber) {
      case 3:
      case 4:
        FormField fieldI1 = factory.field().as();
        fieldI1.setType(FieldType.text);
        fieldI1.setFieldId(questionNumber + ":1:HEADACHE" + questionNumber);
        fieldI1.setRequired(false);
        fieldI1.setLabel("Age");
        fieldI1.setAttributes(reportAttributes());
        fields.add(fieldI1);
        break;
      case 5:
        FormField fieldH2 = factory.field().as();
        fieldH2.setType(FieldType.heading);
        fieldH2.setFieldId(questionNumber+":1:HEADACHE"+questionNumber);
        fieldH2.setLabel("what is the average number of headaches that you have per:");
        fields.add(fieldH2);
        FormField fieldI2 = factory.field().as();
        fieldI2.setType(FieldType.text);
        fieldI2.setFieldId(questionNumber + ":1:HEADACHE" + questionNumber);
        fieldI2.setRequired(false);
        fieldI2.setLabel("Day");
        fieldI2.setAttributes(reportAttributes());
        /*
        String[] headacheDaySubs = {"1-8","8-40","41-200","Greater than 200"};
        FormField radio2Cond2Sub = radiosField(questionNumber, 11, headacheDaySubs);
        Map<String, String> q2Attributes = new HashMap<String, String>();
        q2Attributes.put("StyleName", "");
        q2Attributes.put("StyleName", "dependantQuestion");
        radio2Cond2Sub.setAttributes(q2Attributes);
        ArrayList<FormField> q2SubFields = new ArrayList<FormField>();
        q2SubFields.add(radio2Cond2Sub);
        fieldI2.getValues().get(0).setFields(q2SubFields); -- doesn't work getValues returns null for text field
        */
        fields.add(fieldI2);
        FormField fieldI3 = factory.field().as();
        fieldI3.setType(FieldType.text);
        fieldI3.setFieldId(questionNumber + ":2:HEADACHE" + questionNumber);
        fieldI3.setRequired(false);
        fieldI3.setLabel("Week");
        fieldI3.setAttributes(reportAttributes());
        fields.add(fieldI3);
        FormField fieldI4 = factory.field().as();
        fieldI4.setType(FieldType.text);
        fieldI4.setFieldId(questionNumber + ":3:HEADACHE" + questionNumber);
        fieldI4.setRequired(false);
        fieldI4.setLabel("Month");
        fieldI4.setAttributes(reportAttributes());
        fields.add(fieldI4);
        break;
      case 6:
        if (withoutConditionalQuestions) {
          fields.add(radiosField(questionNumber, 1, yesNoAnswers));
          break;
        }
        FormField fieldI6 = radiosField(questionNumber, 1, yesNoAnswers);
        fieldI6.getValues().get(0).setFields(getFormFields(7, headacheSurveyType));
        fields.add(fieldI6);
        break;
      case 7:
        /* dependent question */
        fields.add(checkboxField(questionNumber, 2, qualOptions));
        break;
      case 8:
        fields.add(radiosField(questionNumber, 1, lastOptions));
        break;
      case 9:
        FormField fieldI9 = radiosField(questionNumber, 1, yesNoAnswers);
        fieldI9.getValues().get(0).setFields(getFormFields(10, headacheSurveyType));
        fields.add(fieldI9);
        break;
      case 10:
        /* dependent question */
        fields.add(radiosField(questionNumber, 1, sidesOptions));
        break;
      case 11:
        fields.add(radiosField(questionNumber, 1, timeOptions));
        break;
      case 12:
        fields.add(checkboxField(questionNumber, 1, locationOptions));
        break;
      case 13:
        fields.add(radiosField(questionNumber, 1, rangesBy5));
        break;
      case 14:
        fields.add(radiosField(questionNumber, 1, physOptions));
        break;
      case 16:
        fields.add(checkboxField(questionNumber, 1, cond1Options));
        break;
      case 17:
        int fieldNumber17=10;
        for (String label : cond2Options) {
          FormField checkBoxCond = factory.field().as();
          checkBoxCond.setType(FieldType.checkboxes);
          checkBoxCond.setFieldId(questionNumber + ":" + fieldNumber17 + ":HEADACHE" + questionNumber);
          checkBoxCond.setRequired(false);
          checkBoxCond.setValues(new ArrayList<FormFieldValue>());
          checkBoxCond.getValues().add(getFormFieldValue(label, label));
          FormField radioCond2Sub = radiosField(questionNumber, fieldNumber17 + 1, cond2SubOptions);
          Map<String, String> attributes = new HashMap<>();
          //attributes.put(Constants.ALIGN, "horizontal");
          attributes.put("StyleName", "");
          attributes.put("StyleName", "dependantQuestion");
          radioCond2Sub.setAttributes(attributes);
          ArrayList<FormField> cond2SubFields = new ArrayList<>();
          cond2SubFields.add(radioCond2Sub);
          checkBoxCond.getValues().get(0).setFields(cond2SubFields);
          fields.add(checkBoxCond);
          fieldNumber17 = fieldNumber17 + 10;
        }
        break;
      case 18:
        FormField fieldI18 = radiosField(questionNumber, 1, yesNoAnswers);
        fieldI18.getValues().get(0).setFields(getFormFields(19, headacheSurveyType));
        fields.add(fieldI18);
        break;
      case 19:
        /* dependent question */
        fields.add(checkboxField(questionNumber, 1, triggerOptions));
        break;
      case 20:
        ArrayList<FormField> sharedDependentQuestions = getFormFields(21, headacheSurveyType);
        FormField fieldI20 = radiosField(questionNumber, 1, physOptions);
        fieldI20.getValues().get(0).setFields(sharedDependentQuestions);
        fieldI20.getValues().get(2).setFields(sharedDependentQuestions);
        fieldI20.getValues().get(3).setFields(sharedDependentQuestions);
        fields.add(fieldI20);
        break;
      case 21:
        /* dependent question */
        fields.add(checkboxField(questionNumber, 1, auraOptions));
        break;
      case 22:
        FormField fieldI22 = radiosField(questionNumber, 1, yesNoAnswers);
        fieldI22.getValues().get(0).setFields(getFormFields(23, headacheSurveyType));
        fields.add(fieldI22);
        break;
      case 23:
        int fieldNumber=10;
        for (String label : triptans) {
          FormField checkBoxMed = factory.field().as();
          checkBoxMed.setType(FieldType.checkboxes);
          checkBoxMed.setFieldId(questionNumber + ":" + fieldNumber + ":HEADACHE" + questionNumber);
          checkBoxMed.setRequired(false);
          checkBoxMed.setValues(new ArrayList<FormFieldValue>());
          checkBoxMed.getValues().add(getFormFieldValue(label, label));
          FormField radioEffective = radiosField(questionNumber, fieldNumber + 1, effective);
          Map<String, String> attributes = new HashMap<>();
          attributes.put(Constants.ALIGN, "horizontal");
          attributes.put("StyleName", "");
          attributes.put("StyleName", "dependantQuestion");
          radioEffective.setAttributes(attributes);
          ArrayList<FormField> effectiveFields = new ArrayList<>();
          effectiveFields.add(radioEffective);
          checkBoxMed.getValues().get(0).setFields(effectiveFields);
          fields.add(checkBoxMed);
          fieldNumber = fieldNumber + 10;
        }
        break;
      case 24:
        FormField fieldI24 = radiosField(questionNumber, 1, yesNoAnswers);
        fieldI24.getValues().get(0).setFields(getFormFields(25, headacheSurveyType));
        fields.add(fieldI24);
        break;
      case 25:
        /* dependent question */
        fields.add(radiosField(questionNumber, 1, yesNoAnswers));
        break;
      case 26:
        FormField field26 = factory.field().as();
        field26.setType(FieldType.heading);
        field26.setFieldId(questionNumber+":1:HEADACHE"+questionNumber);
        field26.setLabel("have you used opiates greater than 15 days per month?");
        fields.add(field26);
        fields.add(radiosField(questionNumber, 2, yesNoAnswers));
        break;
      default:
        fields.add(radiosField(questionNumber, 1, yesNoAnswers));
        break;
      }
      break;
    case 1: // Follow up
      switch (questionNumber) {
      case 0:
        FormField field = factory.field().as();
        field.setType(FieldType.heading);
        field.setFieldId(questionNumber+":1:HEADACHE"+questionNumber);
        field.setLabel("on average, how many headache free days did you have per month?");
        fields.add(field);
        fields.add(radiosField(questionNumber, 2, rangesBy5));
        break;
      case 1:
        FormField fieldF3 = factory.field().as();
        fieldF3.setType(FieldType.number);
        fieldF3.setFieldId(questionNumber + ":1:HEADACHE" + questionNumber);
        fieldF3.setRequired(false);
        fieldF3.setMax("31");
        fieldF3.setLabel("Days");
        fields.add(fieldF3);
        break;
      case 5:
        FormField fieldF4 = radiosField(questionNumber, 1, sleepOptions);
        fieldF4.setLabel("Sleep");
        fieldF4.setAttributes(new HashMap<String, String>());
        fieldF4.getAttributes().put("Align", "horizontal");
        fieldF4.getAttributes().put("REPORT", "Y");
        fields.add(fieldF4);
        FormField fieldF5 = radiosField(questionNumber, 2, nutExOptions);
        fieldF5.setLabel("Nutrition");
        fieldF5.setAttributes(new HashMap<String, String>());
        fieldF5.getAttributes().put("Align", "horizontal");
        fieldF5.getAttributes().put("REPORT", "Y");
        fields.add(fieldF5);
        FormField fieldF6 = radiosField(questionNumber, 3, nutExOptions);
        fieldF6.setLabel("Exercise");
        fieldF6.setAttributes(new HashMap<String, String>());
        fieldF6.getAttributes().put("Align", "horizontal");
        fieldF6.getAttributes().put("REPORT", "Y");
        fields.add(fieldF6);
        break;
      case 6:
        FormField fieldF7 = radiosField(questionNumber, 1, yesNoAnswers);
        fieldF7.setLabel("Frequency");
        fieldF7.setAttributes(new HashMap<String, String>());
        fieldF7.getAttributes().put("Align", "horizontal");
        fieldF7.getAttributes().put("REPORT", "Y");
        fields.add(fieldF7);
        FormField fieldF8 = radiosField(questionNumber, 2, yesNoAnswers);
        fieldF8.setLabel("Severity");
        fieldF8.setAttributes(new HashMap<String, String>());
        fieldF8.getAttributes().put("Align", "horizontal");
        fieldF8.getAttributes().put("REPORT", "Y");
        fields.add(fieldF8);
        FormField fieldF9 = radiosField(questionNumber, 3, yesNoAnswers);
        fieldF9.setLabel("Duration");
        fieldF9.setAttributes(new HashMap<String, String>());
        fieldF9.getAttributes().put("Align", "horizontal");
        fieldF9.getAttributes().put("REPORT", "Y");
        fields.add(fieldF9);
        break;
      case 7:
        FormField fieldF10 = factory.field().as();
        fieldF10.setType(FieldType.number);
        fieldF10.setFieldId(questionNumber + ":1:HEADACHE" + questionNumber);
        fieldF10.setRequired(false);
        fieldF10.setMax("999");
        fieldF10.setLabel("Visits");
        fields.add(fieldF10);
        break;
      case 8:
        fields.add(checkboxField(questionNumber, 1, locationOptions));
        break;
      default:
        fields.add(radiosField(questionNumber, 1, yesNoAnswers));
        break;
      }
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
      logger.trace("found answer to question {} field {}", question, fieldAns.getKey());
      for (String v : fieldAns.getValue().getChoice()) {
        return v;
      }
    }
    return "";
  }

  private FormField checkboxField(int questionNumber, int fieldNumber, String[] options) {
    FormField field = factory.field().as();
    field.setType(FieldType.checkboxes);
    field.setFieldId(questionNumber + ":" + fieldNumber +":HEADACHE" + questionNumber);
    field.setRequired(false);
    field.setValues(new ArrayList<FormFieldValue>());
    for (int inx = 0; inx < options.length; inx++) {
      field.getValues().add(getFormFieldValue(options[inx], Integer.valueOf(inx).toString()));
    }
    return field;

  }

  private FormField radiosField(int questionNumber, int fieldNumber, String[] options) {
    FormField field = factory.field().as();
    field.setType(FieldType.radios);
    field.setFieldId(questionNumber + ":" + fieldNumber +":HEADACHE" + questionNumber);
    field.setRequired(false);
    field.setValues(new ArrayList<FormFieldValue>());
    for (int inx = 0; inx < options.length; inx++) {
      field.getValues().add(getFormFieldValue(options[inx], Integer.valueOf(inx).toString()));
    }
    return field;
  }

  private FormField headingField(int surveyType, int questionNumber) {
    FormField field = factory.field().as();
    field.setType(FieldType.heading);
    field.setFieldId(questionNumber+":0:HEADACHE"+questionNumber);
    field.setLabel(questionText[surveyType][questionNumber]);
    return field;
  }

  private Map<String, String> reportAttributes() {
    Map<String, String> attributes = new HashMap<>();
    attributes.put("REPORT", "Y");
    return attributes;
  }

  @Override
  public int addCompletedSurveyValues(Database database, Long tokenId, SurveyQuery query, Study study, String prefix, Sql sql, String separator) {

    int nbrArgs=0;
    String surveyProvider = study.getSurveySystemId().toString();
    String sectionId = study.getStudyCode().toString();
    Survey s = query.surveyBySurveyTokenId(Long.valueOf(tokenId.longValue()));
    // Figure out whether it's an initial or follow up
    SurveyStep step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId , "HeadacheQ0");
    SurveyType surveyType = null;
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null) {
            if (field.getFieldId().equals("0:1:HEADACHE0")) {
              surveyType = SurveyType.initial;
              logger.trace("Initial headache survey");

            } else if (field.getFieldId().equals("0:2:HEADACHE0")) {
              surveyType = SurveyType.followup;
              logger.trace("Follow up headache survey");
            }
          }
        }
      }
    }
    if (surveyType != null) {
      SquareHelper squareHelper = new SquareHelper(siteInfo, prefix);
      for (int q=0; q <  questionText[surveyType.equals(SurveyType.initial) ? 0 : 1].length; q++) {
        AutoBean<FormQuestion> question = getFormQuestion(q, surveyType);
        FormQuestion formQuestion = question.as();
        List<FormField> formFields = formQuestion.getFields();
        ArrayList<String> documentationLog = new ArrayList<>();
        addQuestionText(documentationLog,formQuestion.getTitle1());
        addQuestionText(documentationLog, formQuestion.getTitle2());
        int f=0;
        for ( FormField field : formFields ) {
          String questionId = "HeadacheQ" + q;
          if (q == 7 || q == 10 || q == 19 || q == 21  || q == 23 || q == 25) {
            questionId = "HeadacheQ" + (q-1);
          }
          if (FieldType.heading.equals(field.getType())) {
            documentationLog.add(field.getLabel());
          }
          if (FieldType.radios.equals(field.getType())) {
            Integer intColumn = squareHelper.getSelect1Response(s, surveyProvider, sectionId, questionId, field.getFieldId());
            if (intColumn != null) {
              String columnName = squareHelper.getColumn(surveyType, q, f);
              // Add 1 so choices begin with 1 not 0; this way Yes=1, No=2
              sql.listSeparator(separator).append(columnName).argInteger(intColumn+1);
              nbrArgs++;
              dumpLog(documentationLog);
            }
            f++;
          } else if (FieldType.checkboxes.equals(field.getType())) {
            for ( FormFieldValue formFieldValue : field.getValues()) {
              //logger.debug("handling question " + q + " field " + f + " val " + field.getValues());
              String columnName = squareHelper.getColumn(surveyType,q,f);
              String selection = formFieldValue.getId();
              if  (q == 17 || q == 23) {
                selection = formFieldValue.getLabel();
              }
              if (squareHelper.isCheckboxSelected(s, surveyProvider, sectionId,
                   questionId, field.getFieldId(), selection)) {
                sql.listSeparator(separator).append(columnName).argString("1");
                dumpLog(documentationLog);
                // logger.debug("set " + columnName + " = " + "1 [" + formFieldValue.getLabel() + "]");
                nbrArgs++;
                if (field.getValues() != null && field.getValues().size() > 0 && field.getValues().get(0).getFields() != null) {
                  for (FormField subField : field.getValues().get(0).getFields() ) {
                    if (subField.getType().equals(FieldType.radios)) {
                      Integer intColumn = squareHelper.getSelect1Response(s, surveyProvider, sectionId, questionId, subField.getFieldId());
                      if (intColumn != null) {
                        String subColumnName = squareHelper.getSubColumn(surveyType, q, f);
                        // Add 1 so choices begin with 1 not 0; this way Yes=1, No=2
                        sql.listSeparator(separator).append(subColumnName).argInteger(intColumn+1);
                        nbrArgs++;
                      }
                    }
                  }
                }
              }
              f++;
            }
          } else if (FieldType.number.equals(field.getType()) || FieldType.text.equals(field.getType())) {
            String response = squareHelper.getInputStringResponse(s, surveyProvider, sectionId, "HeadacheQ" + q, field.getFieldId());
            if (response != null) {
              String columnName = squareHelper.getColumn(surveyType,q,f);
              if (FieldType.number.equals(field.getType())) {
                try {
                  sql.listSeparator(separator).append(columnName).argInteger(Integer.parseInt(response));
                  nbrArgs++;
                } catch (NumberFormatException nfe) {
                  logger.warn("Unable to save question {} with answer {} as integer", step.questionJson(), step.answer().getAnswerJson());
                }
              } else {
                sql.listSeparator(separator).append(columnName).argString(response);
                nbrArgs++;
              }
              documentationLog.add("set " + columnName + " = " + response + "[" + field.getType().toString() + "]");
              dumpLog(documentationLog);
            }
            f++;
          }
        }
        documentationLog.clear();
      }
    }
    return nbrArgs;
  }
  private void dumpLog(ArrayList<String> log) {
    for (String l : log) {
      logger.debug(l);
    }
    log.clear();
  }

  @Override
  public ArrayList<String> getSurveyDocumentation(Database database, Study study, String prefix) {
    columns = new LinkedHashMap<>();
    ArrayList<String> documentation = new ArrayList<>();
    // Process initial
    documentation.add("Initial " + study.getTitle());
    documentation.addAll(getSurveyDocumentation(prefix, SurveyType.initial));
    // Add follow up
    documentation.add("Follow up " + study.getTitle());
    documentation.addAll(getSurveyDocumentation(prefix, SurveyType.followup));
    return documentation;
  }



  public ArrayList<String> getSurveyDocumentation(String prefix, SurveyType surveyType) {
    ArrayList<String> documentation = new ArrayList<>();

    if (surveyType != null) {
      SquareHelper helper = new SquareHelper(siteInfo, prefix);
      for (int q = 0; q < questionText[surveyType.equals(SurveyType.initial) ? 0 : 1].length; q++) {
        AutoBean<FormQuestion> question = getFormQuestion(q, surveyType);
        FormQuestion formQuestion = question.as();
        List<FormField> formFields = formQuestion.getFields();
        ArrayList<String> documentationLog = new ArrayList<>();
        ArrayList<String> questionText = new ArrayList<>();
        addQuestionText(questionText, formQuestion.getTitle1());
        addQuestionText(questionText, formQuestion.getTitle2());
        int f = 0;
        String lastColumnName = "";
        for (FormField field : formFields) {
          if (FieldType.heading.equals(field.getType())) {
            questionText.add(field.getLabel());
          }
          if (FieldType.radios.equals(field.getType())) {
            String columnName = helper.getColumn(surveyType,q,f);
            if (!columnName.equals(lastColumnName)) {
              documentationLog.addAll(SquareDocumentationBuilder.question(questionText, columnName, FieldType.radios.toString(), ""));
              addColumn(columnName, FieldType.radios);
              lastColumnName = columnName;
            }
            for (FormFieldValue formFieldValue : field.getValues()) {
              int response = Integer.parseInt(formFieldValue.getId());
              response++; // Add 1 so choices begin with 1 not 0; this way Yes=1, No=2
              documentationLog.add(SquareDocumentationBuilder.option(formFieldValue.getLabel(), Integer.toString(response)));
            }
            f++;
          } else if (FieldType.checkboxes.equals(field.getType())) {
            if (f==0) {
              documentationLog.addAll(SquareDocumentationBuilder.question(questionText, "", "checkbox", " "));
            }
            for (FormFieldValue formFieldValue : field.getValues()) {
              String columnName = helper.getColumn(surveyType,q,f);
              documentationLog.add(SquareDocumentationBuilder.option(columnName, formFieldValue.getLabel(), "1"));
              addColumn(columnName, FieldType.checkboxes);
              // Add conditional sub-questions
              if (field.getValues() != null && field.getValues().size() > 0 && field.getValues().get(0).getFields() != null) {
                for (FormField subField : field.getValues().get(0).getFields() ) {
                  if (subField.getType().equals(FieldType.radios)) {
                    for (int i = 0; i < subField.getValues().size(); i++) {
                      String subColumnName = helper.getSubColumn(surveyType, q, f);
                      int response = Integer.parseInt(subField.getValues().get(i).getId());
                      response++;  // Add 1 so choices begin with 1 not 0; this way Yes=1, No=2
                      if (i == 0) {
                        documentationLog.add(SquareDocumentationBuilder.option(subColumnName, "radio",
                            "\t" + subField.getValues().get(i).getLabel(), Integer.toString(response)));
                        addColumn(subColumnName, FieldType.radios);
                      } else {
                        documentationLog.add(SquareDocumentationBuilder.option(subField.getValues().get(i).getLabel(), Integer.toString(response)));
                      }
                    }
                  }
                }
              }
              f++;
            }
          } else if (FieldType.number.equals(field.getType()) || FieldType.text.equals(field.getType())) {
            String columnName = helper.getColumn(surveyType,q,f);
            documentationLog.addAll(SquareDocumentationBuilder.question(questionText, columnName, "text", ""));
            addColumn(columnName, field.getType());
            f++;
          }

        }
        documentation.addAll(documentationLog);
      }
    }
    return documentation;
  }
  @Override
  public LinkedHashMap<String, FieldType> getSquareTableColumns(Database database, Study study, String prefix) {
    if (columns == null) {
      getSurveyDocumentation(database, study, prefix);
    }
    return columns;
  }

  private void addColumn(String columnName, FieldType columnType) {
    if (30 < (columnName).length()) {
      System.err.println("ERROR! Column is too large: " + columnName);
    } else if (columns.get(columnName) != null && !columns.get(columnName).equals(columnType)) {
      System.err.println("ERROR! Column " + columnName + " can not be used as " + columnType.toString() + " already exists as " + columns.get(columnName).toString());
    } else {
      columns.put(columnName, columnType);
    }
  }

  private void addQuestionText(ArrayList<String> questionText, String text) {
    if (text != null && !text.isEmpty()) {
      questionText.add(text);
    }
  }

  static private class SquareHelper extends SurveyAdvanceBase {
    String prefix;
    public SquareHelper (SiteInfo siteInfo, String prefix) {
      super(siteInfo);
      if (prefix != null) {
        this.prefix = prefix.toUpperCase();
        if (!this.prefix.endsWith("_")) {
          this.prefix = this.prefix + "_";
        }
      }
    }

    private String[][] initialColumns = {
        {"LIMITING"},
        {"BOTHERED"},
        {"NAUSEATED"},
        {"FIRST_START"},
        {"BOTHER_START"},
        {"AVG_PER_DAY", "AVG_PER_WK", "AVG_PER_MO"},
        {"MANY_TYPES"},
        {"THROBBING", "PULSATING", "SHARP", "PRESSURE", "PIERCING", "SHOOTING", "CRAMPING", "STINGING", "TINGING", "POUNDING", "BURNING", "HEAVY", "SPLITTING", "TIGHTNESS", "DULL"},
        {"LASTS"},
        {"ONE_SIDED"},
        {"WHICH_SIDE"},
        {"MAX_INTENSE"},
        {"FRONT", "TEMPLES", "BACK", "EYES", "NECK"},
        {"FREE_DAYS"},
        {"PHYS_ACT"},
        {"RESTLESS"},
        {"NAUSEA", "VOMITING", "SENS_LIGHT","SENS_SOUND", "SENS_SMELL", "SENS_HEAT" },
        {"RUN_NOSE", "TEAR_EYE", "DROOP_EYE", "RED_EYE"},
        {"TRIGGERS"},
        {"TRIG_FOOD", "TRIG_SKIP_MEAL", "TRIG_DEHYDRATE", "TRIG_ALCOHOL", "TRIG_STRESS", "TRIG_LET_DOWN", "TRIG_HI_ALTITUE", "TRIG_MENSTRUAL", "TRIG_CAFFEINE", "TRIG_LIGHTS", "TRIG_COUGHING", "TRIG_BEARNING", "TRIG_EXERCISE"},
        {"AURA"},
        {"VISUAL", "AUREL", "SENSORY","MOTOR", "NONE" },
        {"TRIPTAN"},
        {"SUMATRIPTAN", "IMITREX", "TREXIMET", "RIZATRIPTAN", "MAXALT", "ELETRIPTAN","RELPAX", "ALMOTRIPTAN", "AXERT",  "NARATRIPTAN", "AMERGE", "FROVATRIPTAN", "FROVA", "ZOLMITRIPTAN",  "ZOMIG"},
        {"OPIOID_GIVEN"},
        {"OPIOID_EFFECT"},
        {"OPIATES_GT_15"}};
    private String[][] followupColumns = {{"AVG_FREE_MO"}, {"DAYS_MEDS"}, {"DAILY_MEDS"}, {"NARC_MEDS"}, {"SIDE_EFF"},
        {"SLEEP", "NUTRITION", "EXERCISE"},
        {"REDUC_FREQ", "REDUC_SEV", "REDUC_DUR"},
        {"EMER_VISITS"},
        {"FRONT", "TEMPLES", "BACK", "EYES", "NECK"}, };

    private String[][][] allColumns = { initialColumns, followupColumns };

    //private final String[] cond2SubColumns = {"ONE", "SHIFT", "BOTH", "UNKNO"};
    //private final String[] effectiveColumns = { "EFFECTIVE" };
    public String getColumn(SurveyType surveyType, int q, int f) {

      return prefix + allColumns[surveyType.equals(SurveyType.initial) ? 0 : 1][q][f];
    }

    public String getSubColumn(SurveyType surveyType, int q, int f) {
       if (SurveyType.initial.equals(surveyType) && q == 17) {
         return prefix + initialColumns[q][f] + "_SIDES";
       }
      if (SurveyType.initial.equals(surveyType) && q == 23) {
        return prefix + initialColumns[q][f] + "_EFFECTIVE";
      }
      return "PROBLEM_NO_COLUMN";
    }
  }
}



