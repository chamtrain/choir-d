package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.Contingency;
import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.RegistryQuestion;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.xform.InputElement;
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;
import edu.stanford.survey.client.api.DisplayStatus;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormField;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 Repeating service gets the questions from the studyDescription.xml
 When processing the response it checks for the repeat condition and if met it loads the xml again
 copying the questions again, incrementing their numbers.
  */
public class RepeatingSurveyService extends NamedSurveyService implements CustomSurveyServiceIntf {
  public String studyName = null;

  private static final String surveySystemName = "RepeatingSurveyService";
  private static Logger logger = Logger.getLogger(RepeatingSurveyService.class);
  private SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  protected RepeatingSurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  public static boolean isMyService(String serviceName) {
    return serviceName.startsWith(surveySystemName);
  }

  public RepeatingSurveyService(SiteInfo siteInfo, String fullServiceName) {
    super(siteInfo);
    if (fullServiceName.startsWith(surveySystemName)) {
      if (fullServiceName.length() + 1 > surveySystemName.length()) {
        setStudyName(fullServiceName.substring(1+surveySystemName.length()));
      }
    } else if (fullServiceName != null && !fullServiceName.isEmpty()) {  // older version: callers stripped the systemName themselves
      setStudyName(fullServiceName);
    }
  }

  public String getQuestionId() {
    return studyName.toUpperCase();
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
                                     String answerJson) {

    if (patStudyExtended == null || patStudyExtended.getDtChanged() != null) { // missing data or survey is already done
      return null;
    }

    studyName = patStudyExtended.getStudyDescription();
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
    if (patStudy == null) { // doesn't exist !
      throw new DataException("Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
          + patStudyExtended.getToken());
    }

    if (submitStatus != null) {
      FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
      return saveResponses(database, patStudy, formAnswer, submitStatus);
    } else {
      int lastQuestionNumber = 0;
      if (patStudy.getContents() != null) {
        // get lastQuestionNumber from the survey in the database
        SurveyQuery surveyQuery = new SurveyQuery(database, new SurveyDao(database), patStudy.getSurveySiteId());
        Survey survey = surveyQuery.surveyBySurveyToken(patStudy.getToken());
        for (SurveyStep step : survey.answeredStepsByProvider(Integer.toString(getSurveySystemId(database)))) {
          if (step != null && step.answer() != null && step.answer().getSubmitStatus() != null) {
            String questionIdString = step.answer().getSubmitStatus().getQuestionId();
            String questionId = "0";
            if (questionIdString.contains(getQuestionId())) {
              questionId = questionIdString.substring(getQuestionId().length());
            }
            logger.debug("last question was " + questionId);
            if (questionId != null && Integer.parseInt(questionId) > lastQuestionNumber) {
              lastQuestionNumber = Integer.parseInt(questionId);
            }
          }
        }
      }

      // Send back the question
      return repeatQuestion(null, database, patStudy, lastQuestionNumber);
    }
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
  public ArrayList<SurveyQuestionIntf> getSurvey(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study,
                                                 Patient patient, boolean allAnswers) {
    /*
    logger.debug("getSurvey starting for " + study.getStudyCode());
    if (patientStudies == null || patientStudies.size() < 1) {
      logger.debug("no studies, returning");
      return new ArrayList<>();
    }
    logger.debug("found " + patientStudies.size() + " studies");
    PatientStudyExtendedData patStudy = null;
    for (PatientStudyExtendedData thisPatStudy : patientStudies) {
      if (thisPatStudy.getStudyCode().intValue() == study.getStudyCode().intValue()
          && thisPatStudy.getContents() != null) {
        patStudy = thisPatStudy;
      }
    }
    if (patStudy == null) {
      return new ArrayList<>();
    }
    */
    ArrayList<SurveyQuestionIntf> allQuestions = //getSurvey(patStudy, study, patStudy.getPatient(), allAnswers);
        super.getSurvey(patientStudies, study, patient, allAnswers);
    ArrayList<SurveyQuestionIntf> printQuestions = new ArrayList<>();
    if (allQuestions != null) {
      for (SurveyQuestionIntf question : allQuestions) {
        if (question.getAttribute("print") == null || "true".equals(question.getAttribute("print").toLowerCase())) {
          printQuestions.add(question);
        }
      }
    }
    return printQuestions;
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
    return "Repeating Survey";
  }

  @Override
  public void setValue(String value) {
  }

  private void setStudyName(String studyName) {
    this.studyName = studyName;
  }


  private NextQuestion saveResponses(Database database, PatientStudy patStudy,
                              FormAnswer formAnswer, SubmitStatus submitStatus) {
    try {
      String xmlDocumentString = patStudy.getContents();
      if (xmlDocumentString == null) {
        // get the file
        xmlDocumentString = XMLFileUtils.getInstance(siteInfo).getXML(database, getStudyName());
      }

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));

      Document messageDom = db.parse(is);
      org.w3c.dom.Element docElement = messageDom.getDocumentElement();
      PatientDao patientDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
      Patient patient = patientDao.getPatient(patStudy.getPatientId());

      if (docElement.getTagName().equals(Constants.FORM)) {
        NodeList itemList = messageDom.getElementsByTagName("Item");
        int numConditions = 0;
        int numMet = 0;
        int newLastQuestionNumber = -1;
        if (submitStatus != null && itemList != null) {
          logger.debug("updating the Question with answers");
          RegistryAssessmentsService.updateQuestion(database, messageDom, patient, itemList, submitStatus, formAnswer, patientDao);
          for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
            Element itemNode = (Element) itemList.item(itemInx);
            int itemOrder = RegistryAssessmentUtils.getIntegerAttribute(itemNode, "Order", 1);
            if (itemOrder > newLastQuestionNumber) {
              newLastQuestionNumber = itemOrder;
            }
            String repeatOnResponse = itemNode.getAttribute("RepeatOnResponse");
            if (repeatOnResponse != null && repeatOnResponse.length() > 0) {
              numConditions++;
              if (isRepeatConditionMet(repeatOnResponse, itemNode)) {
                numMet++;
              }
            }
          }

          if (numConditions > 0 && numConditions == numMet) {
            patStudy = write(database, patStudy, messageDom, false);
            return repeatQuestion(messageDom, database, patStudy, newLastQuestionNumber);
          } else {
            write(database, patStudy, messageDom, true);
          }
        }
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private NextQuestion repeatQuestion(Document oldMessageDom, Database database, PatientStudy patStudy, int lastItemNumber) {

  logger.debug("Repeating " + getStudyName() + " items will start with Order=" + lastItemNumber);

    // Get the questions from disk
    String xmlDocumentString = XMLFileUtils.getInstance(siteInfo).getXML(database, getStudyName());
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = null;
    try {
      db = dbf.newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));
      Document newMessageDom = db.parse(is);
      org.w3c.dom.Element docElement = newMessageDom.getDocumentElement();
      if (docElement.getTagName().equals(Constants.FORM)) {
        NodeList newItemsList = newMessageDom.getElementsByTagName("Items");
        NodeList newItemList = null;
        if (newItemsList != null && newItemsList.getLength() > 0) {
          newItemList = ((Element) newItemsList.item(0)).getElementsByTagName("Item");
        }
        if (newItemsList == null || newItemsList.getLength() < 1) {
          newItemList = newMessageDom.getElementsByTagName("Item");
        }
        NodeList oldItemsList = null;
        if (oldMessageDom != null) {
          oldItemsList = oldMessageDom.getElementsByTagName("Items");
        }
        ArrayList<RegistryQuestion> questions = new ArrayList<>();
        if (newItemList != null) {
          for (int itemInx = 0; itemInx < newItemList.getLength(); itemInx++) {
            Element itemNode = (Element) newItemList.item(itemInx);
            int orderNumber = RegistryAssessmentUtils.getIntegerAttribute(itemNode, "Order", itemInx) + lastItemNumber;
            itemNode.setAttribute("Order", Integer.toString(orderNumber));
            changeContingencyItemOrder(itemNode.getElementsByTagName("onselect"), lastItemNumber);
            changeContingencyItemOrder(itemNode.getElementsByTagName("ondeselect"), lastItemNumber);
            questions.add(RegistryAssessmentUtils.getQuestion(itemNode, orderNumber));
            if (oldItemsList != null) {
              Node newNode = itemNode.cloneNode(true);
              oldMessageDom.adoptNode(newNode);
              oldItemsList.item(0).appendChild(newNode);
            }
          }
        }
        if (oldMessageDom != null) {
          write(database, patStudy, oldMessageDom, false);
        }
        return toNextQuestion(patStudy, questions);
      } else {
        throw new ParserConfigurationException("Invalid Form");
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
        logger.error("Error generating the repeat question ", e);
    }
    return null;
  }

  private NextQuestion toNextQuestion(PatientStudy patStudy, ArrayList<RegistryQuestion> questionIntfs) {
    if (questionIntfs == null || questionIntfs.size() == 0) {
      return null;
    }
    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setQuestionType(QuestionType.form);
    displayStatus.setSurveyProviderId(Integer.toString(patStudy.getSurveySystemId()));
    displayStatus.setSurveySectionId(Integer.toString(patStudy.getStudyCode()));
    AutoBean<FormQuestion> bean = factory.formQuestion();
    FormQuestion question = bean.as();
    boolean firstQuestion = true;
    ArrayList<FormField> fields = new ArrayList<>();
    Map<String, FormFieldValue> orderToParent = new HashMap<>();

    for (RegistryQuestion questionIntf : questionIntfs) {
      boolean firstAnswer = true;
      for (SurveyAnswerIntf answerIntf : questionIntf.getAnswers()) {
        FormField field = factory.field().as();
        Map<String, String> attributes = new HashMap<>();
        if (answerIntf.getType() == Constants.TYPE_INPUT) {
          InputElement input = (InputElement) answerIntf;
          if (input.getDataType() == Constants.DATATYPE_INT) {
            field.setType(FieldType.number);
            String min = input.getAttribute("min");
            if (min != null) {
              field.setMin(min);
            }
            String max = input.getAttribute("max");
            if (max != null) {
              field.setMax(max);
            }
          } else {
            if (input.getLines() > 1) {
              field.setType(FieldType.textArea);
            } else {
              field.setType(FieldType.text);
            }
          }
          String fieldId = questionIntf.getNumber() + ":" + input.getAttribute("Order");
          if (input.getReference() != null && input.getReference().length() > 0) {
            fieldId += ":" + input.getReference();
          }
          field.setFieldId(fieldId);
          field.setLabel(input.getLabel());
          field.setRequired(Boolean.parseBoolean(input.getAttribute("required")));
        } else if (answerIntf.getType() == Constants.TYPE_SELECT) {
          field.setType(FieldType.checkboxes);

          SelectElement input = (SelectElement) answerIntf;
          field.setValues(new ArrayList<FormFieldValue>());
          for (SelectItem item : input.getItems()) {
            FormFieldValue value = factory.value().as();
            value.setId(item.getValue());
            value.setLabel(item.getLabel());
            field.getValues().add(value);

            // Use the show/hide event logic to figure out which other fields should be children of this one
            if (item.getContingency(Constants.ACTION_ONSELECT) != null) {
              for (Contingency contingency : item.getContingency(Constants.ACTION_ONSELECT)) {
                if (orderToParent.containsKey(contingency.getValue())) {
                  throw new RuntimeException("Can't have two fields trying to show/hide the same thing");
                } else {
                  orderToParent.put(contingency.getValue(), value);
                }
              }
            }
          }
          String fieldId = questionIntf.getNumber() + ":" + input.getAttribute("Order");
          if (input.getReference() != null && input.getReference().length() > 0) {
            fieldId += ":" + input.getReference();
          }
          field.setFieldId(fieldId);
          field.setLabel(input.getLabel());
          field.setRequired(Boolean.parseBoolean(input.getAttribute("required")));
        } else if (answerIntf.getType() == Constants.TYPE_SELECT1) {
          field.setType(FieldType.radios);
          SelectElement input = (SelectElement) answerIntf;
          field.setValues(new ArrayList<FormFieldValue>());
          for (SelectItem item : input.getItems()) {
            FormFieldValue value = factory.value().as();
            value.setId(item.getValue());
            value.setLabel(item.getLabel());
            field.getValues().add(value);

            // Use the show/hide event logic to figure out which other fields should be children of this one
            if (item.getContingency(Constants.ACTION_ONSELECT) != null) {
              for (Contingency contingency : item.getContingency(Constants.ACTION_ONSELECT)) {
                if (orderToParent.containsKey(contingency.getValue())) {
                  throw new RuntimeException("Can't have two fields trying to show/hide the same thing");
                } else {
                  orderToParent.put(contingency.getValue(), value);
                }
              }
            }
          }
          String fieldId = questionIntf.getNumber() + ":" + input.getAttribute("Order");
          if (input.getReference() != null && input.getReference().length() > 0) {
            fieldId += ":" + input.getReference();
          }
          field.setFieldId(fieldId);
          field.setLabel(input.getLabel());
          field.setRequired(Boolean.parseBoolean(input.getAttribute("required")));
        } else if (answerIntf.getType() == Constants.TYPE_TEXTBOXSET) {
          field.setType(FieldType.textBoxSet);
          SelectElement input = (SelectElement) answerIntf;
          field.setValues(new ArrayList<FormFieldValue>());
          for (SelectItem item : input.getItems()) {
            FormFieldValue value = factory.value().as();
            value.setId(item.getValue());
            value.setLabel(item.getLabel());
            field.getValues().add(value);

            // Use the show/hide event logic to figure out which other fields should be children of this one
            if (item.getContingency(Constants.ACTION_ONSELECT) != null) {
              for (Contingency contingency : item.getContingency(Constants.ACTION_ONSELECT)) {
                if (orderToParent.containsKey(contingency.getValue())) {
                  throw new RuntimeException("Can't have two fields trying to show/hide the same thing");
                } else {
                  orderToParent.put(contingency.getValue(), value);
                }
              }
            }
          }
          String fieldId = questionIntf.getNumber() + ":" + input.getAttribute("Order");
          if (input.getReference() != null && input.getReference().length() > 0) {
            fieldId += ":" + input.getReference();
          }
          field.setFieldId(fieldId);
          field.setLabel(input.getLabel());
          field.setRequired(Boolean.parseBoolean(input.getAttribute("required")));
        } else if (answerIntf.getType() == Constants.TYPE_COLLAPSIBLE) {
          field.setType(FieldType.collapsibleContentField);
          RegistryAnswer collapsible = (RegistryAnswer) answerIntf;
          String fieldId = questionIntf.getNumber() + ":" + answerIntf.getAttribute("Order");
          if (answerIntf.getAttribute("ref") != null && answerIntf.getAttribute("ref").length() > 0) {
            fieldId += ":" + answerIntf.getAttribute("ref");
          }
          field.setFieldId(fieldId);
          if (collapsible.getLabel() != null) {
            field.setLabel(collapsible.getLabel());
          }
          Map<String, String> attrs = new HashMap<>();
          if (collapsible.getAttribute(Constants.COLLAPSIBLE_CONTENT) != null) {
            attrs.put("collapsibleContent", collapsible.getAttribute(Constants.COLLAPSIBLE_CONTENT));
          }
          if (collapsible.getAttribute("icon") != null) {
            attrs.put("icon", collapsible.getAttribute("icon"));
          }
          field.setAttributes(attrs);
        } else {
          throw new RuntimeException("Unknown answer type: " + answerIntf.getType());
        }
        if (answerIntf.getAttribute(Constants.ALIGN) != null && answerIntf.getAttribute(Constants.ALIGN).length() > 0) {
          attributes.put(Constants.ALIGN, answerIntf.getAttribute(Constants.ALIGN).toLowerCase());
        }
        if (answerIntf.getAttribute("StyleName") != null && answerIntf.getAttribute("StyleName").length() > 0) {
          attributes.put("StyleName", answerIntf.getAttribute("StyleName"));
        }
        String itemOrder = Integer.toString(questionIntf.getNumber());
        List<FormField> addingTo;
        if (orderToParent.containsKey(itemOrder)) {
          List<FormField> parentFields = orderToParent.get(itemOrder).getFields();
          if (parentFields == null) {
            parentFields = new ArrayList<>();
            orderToParent.get(itemOrder).setFields(parentFields);
          }
          addingTo = parentFields;
        } else {
          addingTo = fields;
        }
        if (!firstQuestion && firstAnswer) {
          int nbrText = questionIntf.getText().size();
          if (nbrText > 0) {
            FormField header = factory.field().as();
            String fieldId = questionIntf.getNumber() + ":" + questionIntf.getAttribute("Order");
            header.setFieldId(fieldId);
            header.setType(FieldType.heading);
            header.setLabel(questionIntf.getText().get(0));
            addingTo.add(header);
          }
        }
        if (attributes.size() > 0) {
          field.setAttributes(attributes);
        }
        addingTo.add(field);
        firstAnswer = false;
      }
      // TODO enable server validation once we have it

      if (firstQuestion) {
        int nbrText = questionIntf.getText().size();
        if (nbrText > 0) {
          question.setTitle1(questionIntf.getText().get(0));
        }
        if (nbrText > 1) {
          question.setTitle2(questionIntf.getText().get(1));
        }
        displayStatus.setQuestionId("Order"+questionIntf.getNumber());
      }
      firstQuestion = false;
      if (questionIntf.hasCollapsibleContent()) {
        displayStatus.setQuestionType(QuestionType.collapsibleRadioset);
        factory.collapsibleRadiosetQuestion();
      }
    }
    question.setFields(fields);
    NextQuestion next = new NextQuestion();
    next.setDisplayStatus(displayStatus);
    next.setQuestion(bean);
    return next;
  }

  @Override
  protected String questionId() {
    return "REPEATING";
  }

  private boolean isRepeatConditionMet(String repeatCondition, Element itemNode) {
    /*
     * Handle questions that repeat: check if the "RepeatOnResponse" value equals the itemResponse
     * 1. Integer - If this response # is the ItemResponse then repeat the question.
     * 2. Comma separated list of Integers 1,2,3 - If any of these #'s are in the itemResponse it repeats.
     * 3. Integer:value - If this response is in the itemResponse and the value is the one choosen (for select1 types)
     */
    boolean repeatQuestion = false;
    String itemResponse = itemNode.getAttribute(Constants.ITEM_RESPONSE);
    StringTokenizer repeatOnresponseNums = new StringTokenizer(repeatCondition, ",");
    while (repeatOnresponseNums.hasMoreTokens()) {
      String repeatOn = repeatOnresponseNums.nextToken();
      String selection = null;
      if (repeatOn != null && repeatOn.indexOf(":") > 0 ) {
        String repeatValues[] = repeatOn.split(":");
        selection = repeatValues[1];
        repeatOn = repeatValues[0];
        logger.debug("item " + repeatOn + " repeats when " + selection + " is chosen");
      }
      StringTokenizer itemResponses = new StringTokenizer(itemResponse, ",");
      while (itemResponses.hasMoreTokens()) {
        String response = itemResponses.nextToken();
        if (repeatOn != null && repeatOn.equals(response)) {
          if (selection == null) {
            repeatQuestion = true;
          } else {
            ArrayList<String> selectedValues = RegistryAssessmentUtils.getSelectedValues(response, itemNode);
            for (String value : selectedValues) {
              if (selection.equals(value)) {
                repeatQuestion = true;
              }
            }
          }
        }
      }
    }
    logger.debug("Repeat is " + repeatQuestion);
    return repeatQuestion;
  }

  private PatientStudy write(Database database, PatientStudy patientStudy, Document messageDom, boolean isFinished) {
    try {
      // Convert the XML to string to save it in the db
      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();
      trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      trans.setOutputProperty(OutputKeys.INDENT, "no");
      // create string from xml tree
      StringWriter strWriter = new StringWriter();
      StreamResult result = new StreamResult(strWriter);
      DOMSource source = new DOMSource(messageDom);
      trans.transform(source, result);
      patientStudy.setContents(strWriter.toString());
      PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
      return patStudyDao.setPatientStudyContents(patientStudy, strWriter.toString(), isFinished);
    } catch (TransformerException e) {
      logger.error("Error writing patient_study to database", e);
    }
    return null;
  }

  private void changeContingencyItemOrder(NodeList contingencies, int addNumber) {
    if (contingencies != null) {
      for (int sInx =0; sInx < contingencies.getLength(); sInx++) {
        Element contingency = (Element) contingencies.item(sInx);
        if (contingency.hasAttribute("Type") && "Item".equals(contingency.getAttribute("Type"))
            && contingency.hasAttribute("Where") && "Order".equals(contingency.getAttribute("Where"))
            && contingency.hasAttribute("Value")) {
          try {
            int value= Integer.parseInt(contingency.getAttribute("Value"));
            value = value + addNumber;
            contingency.setAttribute("Value", Integer.toString(value));
          } catch (NumberFormatException nfe) {
            logger.warn("onselect Value=" + contingency.getAttribute("Value") + " is not a valid number.");
          }
        }
      }
    }
  }
}



