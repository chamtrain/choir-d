package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.server.utils.XmlFormatter;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.RegistryQuestion;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.function.Supplier;

import javax.xml.parsers.ParserConfigurationException;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class NamedSurveyService extends SurveySiteBase implements ScoreProvider {

  public final static String emptyForm = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items></Items></Form>";
  public enum SurveyType {
    initial, followup, none
  }
  CustomSurveyServiceIntf service;
  private int version = 0;
  private static final Logger logger = LoggerFactory.getLogger(NamedSurveyService.class);
  protected SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  public NamedSurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  public NamedSurveyService(CustomSurveyServiceIntf customService) {
    super(customService.getSiteInfo());
    service = customService;
    checkService();
  }

  public NamedSurveyService(SiteInfo siteInfo, String surveyServiceClassName) {
    super(siteInfo);
    try {
      logger.debug("Loading service {}", surveyServiceClassName);
      Class<?> surveyImplClass = Class.forName(surveyServiceClassName.trim());
      Constructor<?> constructor = surveyImplClass.getConstructor(SiteInfo.class);
      service = (CustomSurveyServiceIntf) constructor.newInstance(siteInfo);
   } catch (Exception ex) {
      logger.error("Cannot create Survey Service named: {}", surveyServiceClassName, ex);
    }
    checkService();
  }

  @Override  // need this or error-prone complains...
  public Long getSiteId() {
    return siteInfo.getSiteId();
  }

  @Override  // need this or error-prone complains...
  public SiteInfo getSiteInfo() {
    return siteInfo;
  }

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    checkService();
    SurveySystDao ssDao = new SurveySystDao(database);

    Study study = new Study(service.getSurveySystemId(database), 0, name, 0);
    study.setTitle(title);
    study.setExplanation(explanation);
    study = ssDao.insertStudy(study);
    return study;
  }

  @Override
  public void registerAssessment(Database database, Element questionaire, String patientId,
                                 Token tok, User user) throws ServiceUnavailableException {
    checkService();

    String qOrder = questionaire.getAttribute(Constants.XFORM_ORDER);
    Integer order = Integer.valueOf(qOrder);

    // Get the study
    if (service.getStudyName() == null) {
      service.setValue(questionaire.getAttribute("value"));
    }
    SurveySystDao ssDao = new SurveySystDao(database);
    Study study = ssDao.getStudy(service.getSurveySystemId(database), service.getStudyName());

    // Add the study if it doesn't exist
    if (study == null) {
      study = registerAssessment(database, service.getStudyName(), service.getTitle(), "");
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
      patStudy.setOrderNumber(order);
      patStudyDao.insertPatientStudy(patStudy);
    }
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudy, SubmitStatus submitStatus,
      String answerJson) {
    return service.handleResponse(database, patStudy, submitStatus, answerJson);
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(Database database, PatientStudy study, User user) {
    return service.getSurvey(database, study, user);
  }

  public ArrayList<SurveyQuestionIntf> getRegistrySurvey(Database database, PatientStudy study, User user) {
    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
    if (study == null) {
      return questions;
    }
    String xmlDocumentString = study.getContents();
    if (xmlDocumentString == null) {
      return questions;
    }
    try {
      PatientDao patientDao = new PatientDao(database, siteId, user);
      Patient patient = patientDao.getPatient(study.getPatientId());
      NodeList itemList = XmlFormatter.getNodeList(xmlDocumentString, Constants.ITEM);
        for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
          Element itemNode = (Element) itemList.item(itemInx);
          /* only include the questions the patient qualifies for */
          if (RegistryAssessmentUtils.qualifies(patient, itemNode, xmlDocumentString)) {
            RegistryQuestion question = RegistryAssessmentUtils.getQuestion(itemNode, itemInx);
            String itemResponse = itemNode.getAttribute(Constants.ITEM_RESPONSE);
            if (itemResponse != null && itemResponse.trim().length() > 0) {
              question.setAnswered(true);
              StringTokenizer stoken = new StringTokenizer(itemResponse, ",");
              while (stoken.hasMoreTokens()) {
                question.getAnswer(Integer.parseInt(stoken.nextToken())).setSelected(true);
              }
            }
          }
        }
    } catch (Exception e) {
      logger.error("Exception in getSurvey token {} study {}" + study.getToken(), study.getStudyCode(), e);
    }
    return questions;
  }

  @Override
  public String getAssessments(Database database, int version) throws Exception {
    return service.getAssessments(database, version);
  }

  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    return service.getScoreProvider(dbp, studyName);
  }

  @Override
  public void setVersion(int version) {
    this.version = version;
  }

  public int getVersion() {
    return version;
  }

  void checkService() throws ServiceUnavailableException {
    if (service == null) {
      logger.debug("No service");
      throw new ServiceUnavailableException();
    }
  }

  @Override
  public String getDescription() {
    return this.getClass().getName();
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {
    return false;
  }

  @Override
  public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
    return null;
  }

  @Override
  public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> scores, PrintStudy study,
      ChartConfigurationOptions opts) {
    return null;
  }

  @Override
  public Table getScoreTable(ArrayList<ChartScore> scores) {
    return null;
  }

  @Override
  public String formatExplanationText(Study study, ArrayList<ChartScore> scores) {
    return null;
  }

  @Override
  public ChartInfo createLineChart(ArrayList<ChartScore> stats, XYDataset ds, Study study,
      ChartConfigurationOptions opts) {
    return null;
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study,
      Patient patient, boolean allAnswers) {
    logger.trace("getSurvey starting for {} ", study.getStudyCode());
    if (patientStudies == null || patientStudies.size() < 1) {
      logger.trace("no studies, returning");
      return new ArrayList<>();
    }
    logger.trace("found {} studies", patientStudies.size());
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
    return getSurvey(patStudy, study, patStudy.getPatient(), allAnswers);
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(PatientStudyExtendedData patStudy, PrintStudy study, Patient patient,
      boolean allAnswers) {
    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
    Document doc;
    logger.trace("Getting survey");
    try {
      doc = ScoreService.getDocument(patStudy);
      NodeList itemList = doc.getElementsByTagName(Constants.ITEM);
      if (itemList != null && itemList.getLength() > 0) {
        logger.trace("Getting {} items",  itemList.getLength() );
        for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
          Element itemNode = (Element) itemList.item(itemInx);
          questions.add(RegistryAssessmentUtils.getQuestion(itemNode, itemInx, allAnswers));
        }
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      logger.error(e.getMessage(), e);
    }
    return questions;
  }

  @Override
  public XYPlot getPlot(ChartInfo chartInfo, ArrayList<Study> studies, ChartConfigurationOptions opts) {
    return null;
  }

  @Override
  public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient) {
    return null;
  }

  @Override
  public int getReportTextFontSize(PrintStudy study) {
   return 11;
  }

  protected void updatePatientStudy(Database database, PatientStudy patStudy, boolean isDone) {
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    patStudyDao.setPatientStudyContents(patStudy, isDone);
  }

  protected Element itemElement(Document doc, String order, String ID) {
    Element itemElement = doc.createElement(Constants.ITEM);
    itemElement.setAttribute(Constants.ORDER, order);
    itemElement.setAttribute(Constants.ITEM_RESPONSE,"");
    itemElement.setAttribute(Constants.ITEM_SCORE,"");
    itemElement.setAttribute("ID", ID);
    return itemElement;
  }

  protected Element descriptionElement(Document doc, String desc) {
    Element descElement = doc.createElement(Constants.DESCRIPTION);
    descElement.appendChild(doc.createTextNode(desc));
    return descElement;
  }

  protected Element responseElement(Document doc, String itemNumber, String type, String appearance) {
    Element responseElement = doc.createElement(Constants.RESPONSE);
    responseElement.setAttribute(Constants.ORDER, itemNumber);
    responseElement.setAttribute(Constants.TYPE, type);
    responseElement.setAttribute(Constants.APPEARANCE, appearance);
    return responseElement;
  }

  protected Element responseItemElement(Document doc, String label, String value) {
    Element itemElement = doc.createElement(Constants.XFORM_ITEM);
    Element labelElement = doc.createElement("label");
    labelElement.appendChild(doc.createTextNode(label));
    Element valueElement = doc.createElement("value");
    valueElement.appendChild(doc.createTextNode(value));
    itemElement.appendChild(labelElement);
    itemElement.appendChild(valueElement);
    return itemElement;
  }

  protected String selection(int question, Map<String, Map<String, FormFieldAnswer>> itemOrderToResponseOrderToField ) {

    Map<String, FormFieldAnswer> responseOrderToField = itemOrderToResponseOrderToField.get((Integer.valueOf(question)).toString());
    for (Entry<String, FormFieldAnswer> fieldAns : responseOrderToField.entrySet()) {
      logger.trace("found answer to question {} field {}", question, fieldAns.getKey());
      for (String v : fieldAns.getValue().getChoice()) {
        return v;
      }
    }
    return "";
  }

  protected String questionId() {
    return "";
  }

  public String fieldId(int questionNumber, int fieldNumber, String questionId) {

    return questionNumber + ":" + fieldNumber +":" + questionId + questionNumber;
  }
  protected FormFieldValue formFieldValue(String label, String value) {
    FormFieldValue ffValue = factory.value().as();
    ffValue.setId(value);
    ffValue.setLabel(label);
    return ffValue;
  }

  protected FormField checkboxFormField(String fieldId, String[] options) {
    FormField field = factory.field().as();
    field.setType(FieldType.checkboxes);
    field.setFieldId(fieldId);
    field.setRequired(false);
    field.setValues(new ArrayList<FormFieldValue>());
    for (int inx = 0; inx < options.length; inx++) {
      field.getValues().add(formFieldValue(options[inx], Integer.valueOf(inx).toString()));
    }
    return field;
  }




  protected FormField radiosFormField(int questionNumber, int fieldNumber, String[] options) {
    FormField field = factory.field().as();
    field.setType(FieldType.radios);
    field.setFieldId(questionNumber + ":" + fieldNumber +":" + questionId() + questionNumber);
    field.setRequired(false);
    field.setValues(new ArrayList<FormFieldValue>());
    for (int inx = 0; inx < options.length; inx++) {
      field.getValues().add(formFieldValue(options[inx], Integer.valueOf(inx).toString()));
    }
    return field;
  }

  protected FormField headingFormField(String questionText, int questionNumber, int fieldNumber) {
    FormField field = factory.field().as();
    field.setType(FieldType.heading);
    field.setFieldId(questionNumber + ":"+fieldNumber+":" + questionId() + questionNumber);
    field.setLabel(questionText);
    return field;
  }

  protected FormField collapsibleHeadingFormField(int questionNumber, int fieldNumber, String label, String collapsedContent) {
    FormField field = factory.field().as();
    field.setType(FieldType.collapsibleContentField);
    field.setFieldId(questionNumber + ":" + fieldNumber + ":" + questionId() + questionNumber);
    field.setLabel(label);
    Map<String, String> attrs = new HashMap<>();
    attrs.put("collapsibleContent", collapsedContent);
    field.setAttributes(attrs);
    return field;
  }

}
class mySurveySystem extends SurveySystem {

  private static final long serialVersionUID = 6892518545769623563L;
  private static final HashMap<String, mySurveySystem> mySystems = new HashMap<>();
  private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(mySurveySystem.class);

  private mySurveySystem(Database database, String surveySystemName) throws DataException {
    SurveySystDao ssDao = new SurveySystDao(database);
    SurveySystem ssys = ssDao.getOrCreateSurveySystem(surveySystemName, logger);
    this.copyFrom(ssys);
  }

  public static SurveySystem getInstance(Database database, String surveySystemName) throws DataException {
    if (mySystems.get(surveySystemName) == null) {
      mySystems.put(surveySystemName, new mySurveySystem(database, surveySystemName));
    }
    return mySystems.get(surveySystemName);

  }
}
