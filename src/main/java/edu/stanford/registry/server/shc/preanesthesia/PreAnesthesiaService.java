package edu.stanford.registry.server.shc.preanesthesia;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.survey.NextQuestion;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.RegistryAssessmentsService;
import edu.stanford.registry.server.survey.RegistryShortFormScoreProvider;
import edu.stanford.registry.server.survey.SurveyAdvanceBase;
import edu.stanford.registry.server.survey.SurveyToSquareIntf;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.server.utils.SquareXml;
import edu.stanford.registry.server.utils.StringUtils;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.RegistryQuestion;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.xform.InputElement;
import edu.stanford.registry.shared.xform.Select1Element;
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;
import com.github.susom.database.Sql;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Created by tpacht on 11/9/2015.
 */
public class PreAnesthesiaService extends RegistryAssessmentsService implements SurveyToSquareIntf {
  private final static Logger logger = Logger.getLogger(PreAnesthesiaService.class);
  private SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  public PreAnesthesiaService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  protected static Logger getLogger() {
    return logger;
  }

  protected String getConsentAttrib() {
    return "pacFollowUp";
  }

  public final static String emptyForm = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items></Items></Form>";

  @Override
  public int addCompletedSurveyValues(Database database, Long tokenId, SurveyQuery query, Study study, String prefix, Sql sql, String separator) {
    if (study == null) {
      logger.debug("WON'T add survey values study=null with prefix " + prefix);
      return 0;
    }
    logger.debug("add survey values starting for study " + study.getStudyDescription() + " with prefix " + prefix);

    String surveyProvider = study.getSurveySystemId().toString();
    String sectionId = study.getStudyCode().toString();
    SquareXml squareXml = new SquareXml(database, siteInfo, study.getStudyDescription(), prefix);
    int nbrArgs=0;
    Survey s = query.surveyBySurveyTokenId(new Long(tokenId.longValue()));
    LinkedHashMap<String, String> columns = squareXml.getColumns();

    int inx = 0;
    LocalSurveyAdvance advance = new LocalSurveyAdvance(siteInfo);

    for (String columnName : columns.keySet()) {

      try {
        String fieldId = advance.getFieldId(squareXml, inx);
        String[] parts = advance.getParts(fieldId);
        String questionId = advance.getQuestionId(study.getStudyDescription(), parts);
        logger.debug("Field id: " + fieldId + "study: " + study.getStudyDescription() + "col:" + columnName + " is a " + columns.get(columnName) + ". With questionId: " + questionId + " fieldId:" + fieldId);
        if (columns.get(columnName).equals("select1")) {
          Integer intColumn = advance.getSelect1Response(s, surveyProvider, sectionId, questionId, fieldId);
          if (intColumn != null) {
            sql.listSeparator(separator).append(columnName).argInteger(intColumn);
            nbrArgs++;
          }
        } else if (columns.get(columnName).equals("select") ) {
          fieldId = parts[0] + ":" + parts[1] + ":" + parts[3];
          if ("OPIOIDS4TYPEFORSURG".equals(parts[3]) || "OPIOIDS4TYPEFOROTHE".equals(parts[3])) {
            fieldId = parts[0] + ":" + parts[1] + ":" + "OPIOIDS4TYPE";
          }
          logger.debug(
                "looking up section " + sectionId + "  " + questionId + " fieldId:" + fieldId + " choice: " + parts[2]);

            if (advance.isCheckboxSelected(s, surveyProvider, sectionId, questionId, fieldId, parts[2])) {
              sql.listSeparator(separator).append(columnName).argString("1");
              nbrArgs++;
            }
        } else if (columns.get(columnName).equals("input") || columns.get(columnName).equals("datePicker")) {
          logger.debug("looking up section " + sectionId + " " + questionId + " fieldId:" + fieldId);
          String response = advance.getInputStringResponse(s, surveyProvider, sectionId, questionId, fieldId);
          if (response != null) {
            sql.listSeparator(separator).append(columnName).argString(response);
            nbrArgs++;
          }
        } else if (columns.get(columnName).equals("radio")  ) { //&&  step.answerNumeric()!= null){
          logger.debug("looking up section " + sectionId + " " + parts[0] );
          Integer response = advance.getRadioIntegerResponse(s, surveyProvider, sectionId, parts[0]);
          if (response != null) {
            sql.listSeparator(separator).append(columnName).argInteger(response);
            nbrArgs++;
          }
        } else {
          logger.error("Cannot process column type " + columns.get(columnName) + " in study " + study.getStudyDescription());
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        System.out.println("failed on column " + columnName + " section " + sectionId );
      }
      inx++;
    }
    return nbrArgs;
  }


  @Override
  public ArrayList<String> getSurveyDocumentation(Database database, Study study, String prefix ) {
    SquareXml squareXml = new SquareXml(database, siteInfo, study.getStudyDescription(), prefix, true);
    return squareXml.getDocumentationLog();
  }

  @Override
  public LinkedHashMap<String, FieldType> getSquareTableColumns(Database database, Study study, String prefix) {
    SquareXml squareXml = new SquareXml(database, siteInfo, study.getStudyDescription(), prefix, false);
    return squareXml.getColumnTypes();
  }

  private enum attributeValue {Y, N}

  private PacQuestionProvider pacQuestionProvider = null;
  private PacVicesProvider pacVicesProvider = null;
  private PacPainMedsProvider pacPainMedsProvider = null;
  private String INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;";

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended,
                                     SubmitStatus submitStatus, String answerJson) {
    if (patStudyExtended.getStudyDescription() == null) {
      return null;
    }
    logger.debug("processing preanesthesia survey " + patStudyExtended.getStudyDescription());
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    if (patStudyExtended.getStudyDescription().equals("pacFollowUp")) {
      PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
      if (patStudy == null) { // doesn't exist !
        throw new DataException(
            "Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
                + patStudyExtended.getToken());
      }
      if (submitStatus != null) {
        if (patStudy.getContents() == null) { // This is the first question
          getLogger().trace("Consent: first question answered");
          FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
          for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
            String[] ids = fieldAnswer.getFieldId().split(":");
            if (ids.length == 3) {
              if ("1".equals(ids[0]) && "1".equals(ids[1])
                  && "FOLLOWCONS".equals(ids[2])) { // Item 1, response 1 with ref value of "FOLLOWONS"
                if ("1".equals(fieldAnswer.getChoice().get(0))) { // YES
                  setConsentAttribute(database, patStudyExtended.getPatient(), attributeValue.Y);
                }
                if ("2".equals(fieldAnswer.getChoice().get(0))) { // NO
                  setConsentAttribute(database, patStudyExtended.getPatient(), attributeValue.N);
                }
              }
            }
          }
        }
      }
    }
    if (patStudyExtended.getStudyDescription().equals("pacQuestions")) {
      PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
      if (patStudy == null) { // doesn't exist !
        throw new DataException(
            "Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
                + patStudyExtended.getToken());
      }
      String height = "";
      String weight = "";
      if (submitStatus != null) {

        FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
        for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
        /* The format of the fieldId is "ItemOrder:ResponseOrder:Ref" */
          String[] ids = fieldAnswer.getFieldId().split(":");
          if (ids.length > 2) {
            String ref = ids[2];
            if ("HEIGHTFT".equals(ref)) {
                String feet = fieldAnswer.getChoice().get(0);
                if (feet != null && feet.length() > 0) {
                  height = StringUtils.cleanString(feet) + "' ";
                }
            } else if ("HEIGHTIN".equals(ref)) {
                String inches = fieldAnswer.getChoice().get(0);
                if (inches != null && inches.length() > 0) {
                  height = height + StringUtils.cleanString(inches) + "\"";
                }
            } else if ("WEIGHTLBS".equals(ref)) {
              weight = fieldAnswer.getChoice().get(0);
              if (weight != null && weight.length() > 0) {
                weight = StringUtils.cleanString(weight);
              }
            }
          }
        }
        if (height.length() > 0) {
          setAttribute(database, patStudyExtended.getPatient(), "Height", height);
        }
        if (weight.length() > 0) {
          setAttribute(database, patStudyExtended.getPatient(), "Weight", weight);
        }
      }
    }
    if (patStudyExtended.getStudyDescription().equals("pacMenstrual")) {
      PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
      if (patStudy == null) { // doesn't exist !
        throw new DataException(
            "Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
                + patStudyExtended.getToken());
      }
      if (patStudy.getContents() == null) { // first question, check if Female, 10 - 55 yrs old
        logger.debug("First question, checking gender and age");
        Patient patient = patStudyExtended.getPatient();
        if (patient.hasAttribute("gender") && "Female".equals(patient.getAttribute("gender").getDataValue())
            && patient.getDtBirth() != null && DateUtils.getAge(patient.getDtBirth()) > 10
            && DateUtils.getAge(patient.getDtBirth()) < 55) {
          logger.debug("Qualifies");
          return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
        }

        // skip
        logger.debug("Does not qualify");
        patStudyDao.setPatientStudyContents(patStudyExtended, emptyForm, true);
        return null;
      }
      logger.debug("not the 1st question");
      return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
    }
    return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
  }

  protected void setConsentAttribute(Database database, Patient patient, Enum<?> dataValue) {
    PatientAttribute pattribute = patient.getAttribute(getConsentAttrib());
    if (pattribute == null) {
      pattribute = new PatientAttribute(patient.getPatientId(), getConsentAttrib(), dataValue.toString(), PatientAttribute.STRING);
    } else {
      if (dataValue.toString().equals(pattribute.getDataValue())) {
        return;
      }
      pattribute.setDataValue(dataValue.toString());
    }
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    patAttribDao.insertAttribute(pattribute);
  }

  protected void setAttribute(Database database, Patient patient, String attributeName, String attributeValue) {
    PatientAttribute pattribute = patient.getAttribute(attributeName);
    if (pattribute == null) {
      pattribute = new PatientAttribute(patient.getPatientId(),
          attributeName, attributeValue,
          PatientAttribute.STRING);
    } else {
      if (attributeValue.equals(pattribute.getDataValue())) {
        return;
      }
      pattribute.setDataValue(attributeValue);
    }
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    patAttribDao.insertAttribute(pattribute);
    patient.addAttribute(pattribute);
  }

  /**
   * Return the appropriate score provider based on the study name.
   */
  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    if ("pacAlcohol".equals(studyName) || "pacDrugs".equals(studyName) || "pacSmoking".equals(studyName)) {
      if (pacVicesProvider == null) {
        pacVicesProvider = new PacVicesProvider(dbp, siteInfo);
      }
      return pacVicesProvider;
    } else if ("pacPainMeds".equals(studyName)) {
      if (pacPainMedsProvider == null) {
        pacPainMedsProvider = new PacPainMedsProvider(dbp, siteInfo);
      }
      return pacPainMedsProvider;
    } else if (studyName != null && studyName.startsWith("pac")) {
      if (pacQuestionProvider == null) {
        pacQuestionProvider = new PacQuestionProvider(dbp, siteInfo);
      }
      return pacQuestionProvider;
    }
    return super.getScoreProvider(dbp, studyName);
  }

  private class PacQuestionProvider extends RegistryShortFormScoreProvider {

    public PacQuestionProvider(Supplier<Database> dbp, SiteInfo siteInfo) {
      super(dbp, siteInfo);
    }

    @Override
    public ArrayList<SurveyQuestionIntf> getSurvey(PatientStudyExtendedData patStudy, PrintStudy study,
                                                   Patient patient, boolean allAnswers) {

      Document doc;
      ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();

      try {
        doc = ScoreService.getDocument(patStudy);
        if (doc == null) {
          return questions;
        }
        NodeList itemList = doc.getElementsByTagName(Constants.ITEM);
        if (itemList == null || itemList.getLength() == 0) {
          return questions;
        }
        for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
          Element itemNode = (Element) itemList.item(itemInx);
          // Get all the questions, whether answered or not
          RegistryQuestion question = RegistryAssessmentUtils.getQuestion(itemNode, itemInx, true);
          if (question.getAttribute("print") == null || "true".equals(question.getAttribute("print").toLowerCase())) {
            ArrayList<SurveyAnswerIntf> answers = question.getAnswers(false);
            if (answers == null) {
              answers = new ArrayList<>();
              question.setAnswers(answers);
            }
            for (SurveyAnswerIntf answerIntf : answers) {
              String ref = answerIntf.getAttribute("ref");
              logger.debug("ref:" + ref);
              if ("ISSPRESENT".equals(ref)) {
                replaceText(question.getText(), "Review of symptoms");
                question.setAnswered(true);
                addAnswered(questions, question);
                if (Constants.TYPE_SELECT == answerIntf.getType()) {
                  PreAnesthesiaServiceUtils.selectAll((SelectElement) answerIntf);
                }
                SelectElement select = (SelectElement) answerIntf;
                SelectItem lastItem = select.getItems().get(select.getItems().size()-1);
                lastItem.setLabel("False, capped, loose, chipped teeth; braces");
              } else if ("ISSALLERGIES".equals(ref)) {
                if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_SELECT1) {
                  if ("1".equals(getSelectedValue((Select1Element) answerIntf))) {
                    replaceText(question.getText(), "Has allergies");
                  } else {
                    replaceText(question.getText(), "No allergies");
                  }
                  question.setAnswers(new ArrayList<SurveyAnswerIntf>());
                  addAnswered(questions, question);
                }
              } else if (ref != null && ref.startsWith("ISSALLERGY")) {
                logger.debug("starts with ISSALLERGY");
                if (answerIntf.getType() == Constants.TYPE_SELECT) {
                  ArrayList<String> questionText = question.getText();
                  if (questionText != null && questionText.size() > 0) {
                    logger.debug(" found text. adding Allergic to label ");
                    questionText.set(0, "&nbsp;&nbsp;Allergic to ");
                  } else {
                    logger.debug(" adding label " + getSelectedLabel((SelectElement) answerIntf));
                    questionText.add("Allergic to ");
                  }
                  addAnswered(questions, question);
                } else if (answerIntf.getType() == Constants.TYPE_INPUT) {
                  InputElement input = ((InputElement) answerIntf);
                  replaceText(question.getText(), "Type of reaction");
                  input.setLabel("");
                  addAnswered(questions,question);
                }
              } else if ("ISSPROBLEMS".equals(ref)) {
                replaceText(question.getText(), "Past medical history");
                question.setAnswered(true);
                if (Constants.TYPE_SELECT == answerIntf.getType()) {
                  PreAnesthesiaServiceUtils.selectAll((SelectElement) answerIntf);
                }
                addAnswered(questions, question);
              } else if ("ISPROBLEMSOTH".equals(ref)) {
                addColon(answerIntf);
              } else if (ref != null && "ISSDENTAL".equals(ref)) {
                replaceText(question.getText(), "Dental Issues");
                PreAnesthesiaServiceUtils.selectAll((SelectElement) answerIntf);
              } else if (ref != null && "CARDIONAME".equals(ref)) {
                replaceText(question.getText(), "Cardiologist");
                InputElement input = ((InputElement) answerIntf);
                input.setLabel("Name:");
                if (input.getResponse().size() <= 0 || input.getResponse().get(0).isEmpty()) {
                  input.setValue("-");
                  input.setSelected(true);
                }
                addAnswered(questions, question);
              } else if (ref != null && "CARDIONUMBER".equals(ref)) {
                InputElement input = ((InputElement) answerIntf);
                input.setLabel("Phone:");
                if (input.getResponse().size() <= 0 || input.getResponse().get(0).isEmpty()) {
                  input.setValue("-");
                  input.setSelected(true);
                }
                // don't add question again, 2nd response to same question as CARDIONAME
              } else if (ref != null && "HEARTMGRNAME".equals(ref)) {
                replaceText(question.getText(), "Physician managing heart care");
                addAnswered(questions,question);
                InputElement input = ((InputElement) answerIntf);
                input.setLabel("Name:");
                if (input.getResponse().size() <= 0 || input.getResponse().get(0).isEmpty()) {
                  input.setValue("-");
                  input.setSelected(true);
                }
              } else if (ref != null && "HEARTMGRNUMBER".equals(ref)) {
                InputElement input = ((InputElement) answerIntf);
                input.setLabel("Phone:");
                if (input.getResponse().size() <= 0 || input.getResponse().get(0).isEmpty()) {
                  input.setValue("-");
                  input.setSelected(true);
                }
                // don't add question again, 2nd response to same question as HEARTMGRNAME
              } else if (ref != null && "ILLNESSTWO".equals(ref)) { // Special handling for pacIllness questions
                replaceText(question.getText(), "Acute illness in last 2 weeks");
                addAnswered(questions,question);
              } else if (ref != null && "ILLNESSTYPE".equals(ref) || "ISSEXPLAIN".equals(ref)) {
                RegistryQuestion illnessQuestion = (RegistryQuestion) PreAnesthesiaServiceUtils.getQuestion(questions,
                    Integer.parseInt(question.getAttribute(Constants.ORDER)) - 1);
                if (illnessQuestion != null) {
                  if (Constants.TYPE_SELECT == answerIntf.getType()) {
                    illnessQuestion.addAnswer((SelectElement) answerIntf);
                  }
                  if (Constants.TYPE_INPUT == answerIntf.getType()) {
                    illnessQuestion.addAnswer((InputElement) answerIntf);
                  }
                }
              } else if ("HOSP6MON".equals(ref)) {
                if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_SELECT1) {
                  if ("1".equals(getSelectedValue((Select1Element) answerIntf))) {
                    replaceText(question.getText(), "Has been hospitalized in the past 6 months");
                  } else {
                    replaceText(question.getText(), "No hospitalization in the past 6 month");
                  }
                  question.setAnswers(new ArrayList<SurveyAnswerIntf>());
                  addAnswered(questions,question);
                }
              } else if ("HOSPWHERE".equals(ref) || "HOSPWHY".equals(ref)) {
                InputElement input = ((InputElement) answerIntf);
                if (input.getResponse() != null && input.getResponse().size() > 0
                    && !input.getResponse().get(0).isEmpty()) {
                  InputElement newAnswer = new InputElement();
                  newAnswer.setLabel("");
                  if ("HOSPWHERE".equals(ref)) {
                    newAnswer.setValue("at " + input.getValue());
                  } else {
                    newAnswer.setValue("for " + input.getValue());
                  }
                  RegistryQuestion hospQuestion = (RegistryQuestion) PreAnesthesiaServiceUtils.getQuestion(questions,
                      Integer.parseInt(question.getAttribute(Constants.ORDER)) - 1);
                  hospQuestion.addAnswer(newAnswer);
                }
              } else if ("ISSSTEROIDS".equals(ref)) {
                replaceText(question.getText(), "Corticosteroid use in last 6 months");
                addAnswered(questions,question);
              } else if ("ISSSTEROIDSLAST".equals(ref)) {
                RegistryQuestion steriodsQuestion = (RegistryQuestion) PreAnesthesiaServiceUtils.getQuestion(questions,
                    Integer.parseInt(question.getAttribute(Constants.ORDER)) - 1);
                if (steriodsQuestion != null) {
                  steriodsQuestion.addAnswer((SelectElement) answerIntf);
                }
                //questions.add(steriodsQuestion);
              } else if ("PAST12".equals(ref)) {
                replaceText(question.getText(), "Anesthesia Pre-op Clinic visit in the last 12 months");
                addAnswered(questions,question);
              } else if ("SHCSURGERY".equals(ref)) {
                replaceText(question.getText(), "Have had surgery at Stanford");
                addAnswered(questions,question);
              } else if ("ISSCOMPLIC".equals(ref)) {
                replaceText(question.getText(), "Personal or family history of anesthesia complications");
                addAnswered(questions,question);
              } else if ("ISSCOMPLICEXPL".equals(ref)) {
                InputElement input = ((InputElement) answerIntf);
                if (input.getResponse() != null && input.getResponse().size() > 0
                    && !input.getResponse().get(0).isEmpty()) {
                  addAnswered(questions,question);
                }
              } else if ("ISSCHRONICWORSE".equals(ref)) {
                replaceText(question.getText(), "Chronic conditions with exacerbation in the last month");
                InputElement input = ((InputElement) answerIntf);
                if (input.getResponse().size() <= 0 || input.getResponse().get(0).isEmpty()) {
                  input.setLabel("");
                  input.setValue("-");
                  input.setSelected(true);
                }
                question.setAnswered(true);
                addAnswered(questions, question);
              } else if ("ISSCHRONICNONE".equals(ref)) {
                PreAnesthesiaServiceUtils.selectAll((SelectElement) answerIntf);
                logger.debug("Adding CHRONICNONE selected");
              } else if ("QUESTIONS".equals(ref)) {
                replaceText(question.getText(), "Questions for the anesthesiologist");
                addAnswered(questions,question);
              } else if ("QUESTIONSEXP".equals(ref)) {
                RegistryQuestion anesthQuestion = (RegistryQuestion) PreAnesthesiaServiceUtils.getQuestion(questions,
                    Integer.parseInt(question.getAttribute(Constants.ORDER)) - 1);
                if (anesthQuestion != null && anesthQuestion.getAnswers() != null && anesthQuestion.getAnswers().size() > 0) {
                  SurveyAnswerIntf anesthAns = anesthQuestion.getAnswers().get(0);
                  if (anesthAns.getType() == Constants.TYPE_SELECT1 && "1".equals(getSelectedValue((Select1Element) anesthAns))) { // Yes!
                    replaceText(question.getText(), "Using opioids for pain");
                    InputElement input = ((InputElement) answerIntf);
                    input.setLabel("");
                    if (input.getResponse().size() <= 0 || input.getResponse().get(0).isEmpty()) {
                      input.setValue("&nbsp;&nbsp; -");
                      input.setSelected(true);
                    }
                    anesthQuestion.addAnswer(input);
                  }
                }
              } else if ("HEIGHTFT".equals(ref) || "HEIGHTIN".equals(ref) || "WEIGHTLBS".equals(ref)) {
                // skip, we don't print q&a for height/weight
              } else if ("EXERCISEOTH".equals(ref)) {
                addColon(answerIntf); // 2nd response so don't add question
              } else if ("CONTACTNUM".equals(ref)) {
                replaceText(question.getText(), "Best contact phone number before surgery");
                InputElement input = ((InputElement) answerIntf);
                if (input.getResponse().size() <= 0 || input.getResponse().get(0).isEmpty()) {
                  input.setLabel("");
                  input.setValue("- ");
                  input.setSelected(true);
                }
                question.setAnswered(true);
                addAnswered(questions,question);
              } else if ("CONTACTWHO".equals(ref)) {
                replaceText(question.getText(), "Phone number is for ");
                addAnswered(questions,question);
              } else if ("HASMENSTRUAL".equals(ref)) {
                replaceText(question.getText(), "Having menstrual periods");
                addAnswered(questions,question);
              } else if ("POSSPREGNANT".equals(ref)) {
                replaceText(question.getText(), "Possibly pregnant");
                addAnswered(questions,question);
              } else if ("OPIOIDS4PAIN".equals(ref)) {
                if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_SELECT1) {
                  if ("1".equals(getSelectedValue((Select1Element) answerIntf))) { // Yes!
                    replaceText(question.getText(), "Using opioids for pain");
                  } else {
                    replaceText(question.getText(), "Not using opioids for pain");
                  }
                  question.setAnswers(new ArrayList<SurveyAnswerIntf>());
                  addAnswered(questions,question);
                }
              } else if ("OPIOIDS4TYPE".equals(ref)) {
                RegistryQuestion opioidsQuestion = (RegistryQuestion) PreAnesthesiaServiceUtils.getQuestion(questions,
                    Integer.parseInt(question.getAttribute(Constants.ORDER)) - 1);
                if (opioidsQuestion != null && answerIntf.getType() == Constants.TYPE_SELECT) {
                  opioidsQuestion.addAnswer((SelectElement) answerIntf);
                }
              } else if ("OPIOIDS4DESC".equals(ref)) {
                InputElement input = ((InputElement) answerIntf);
                replaceText(question.getText(), INDENT + " ");
                input.setLabel("");
                addAnswered(questions, question);
              } else if (ref != null && ref.toUpperCase().equals("PAINDOC") ) {
                Select1Element select1Element = (Select1Element) answerIntf;
                if ("1".equals(getSelectedValue(select1Element))) {
                  replaceText(question.getText(), "Being seen by a Pain doctor" );
                }
                question.setAnswers(new ArrayList<SurveyAnswerIntf>());
                addAnswered(questions, question);
              } else if (ref != null && ref.toUpperCase().equals("STANFORDDOC")) {
                // skip
              } else if (ref != null && ref.toUpperCase().equals("STANPHYS")) {
                replaceText(question.getText(), "Stanford Physician");
                InputElement input = ((InputElement) answerIntf);
                if (input.getResponse().size() <= 0 || input.getResponse().get(0).isEmpty()) {
                  input.setValue("-");
                }
                addColon(answerIntf);
                input.setSelected(true);
                addAnswered(questions, question);
              }  else if (ref != null && (ref.toUpperCase().equals("PHYSNAME") || ref.toUpperCase().equals("PHYSNUMBER"))) {
                replaceText(question.getText(), "Non-Stanford Physician");
                InputElement input = ((InputElement) answerIntf);
                if (input.getResponse().size() <= 0 || input.getResponse().get(0).isEmpty()) {
                  input.setValue("-");
                }
                input.setSelected(true);
                if (ref.toUpperCase().equals("PHYSNAME")) {
                  input.setLabel("Name:");
                  addAnswered(questions, question);
                } else {
                  input.setLabel("Phone:");
                }
              } else {
                addAnswered(questions,question);
              }
              addUnanswered(questions, question);
            }
          }
        }
      } catch (ParserConfigurationException | SAXException | IOException e) {
        logger.error(
            "Error getting questions for patientStudy token " + patStudy.getToken() + " study "
                + patStudy.getStudyCode(), e);
      }
      return questions;
    }
  }

private class PacPainMedsProvider extends RegistryShortFormScoreProvider {

  public PacPainMedsProvider(Supplier<Database> dbp, SiteInfo siteInfo) {
    super(dbp, siteInfo);
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(PatientStudyExtendedData patStudy, PrintStudy study,
                                                 Patient patient, boolean allAnswers) {

    Document doc;
    ArrayList<SurveyQuestionIntf> drugQuestions = new ArrayList<>();

    try {
      doc = ScoreService.getDocument(patStudy);
      if (doc == null) {
        return drugQuestions;
      }
      NodeList itemList = doc.getElementsByTagName(Constants.ITEM);
      if (itemList == null || itemList.getLength() == 0) {
        return drugQuestions;
      }
      for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
        Element itemNode = (Element) itemList.item(itemInx);
        RegistryQuestion question = RegistryAssessmentUtils.getQuestion(itemNode, itemInx, true);

        if (question != null  && question.getAnswers().size() > 0 && question.getAnswered()) {
           if (question.getAnswer(0).getType() == Constants.TYPE_SELECT) {
             replaceText(question.getText(), ((SelectElement) question.getAnswer(0)).getItems().get(0).getLabel());
             question.setAnswers(new ArrayList<SurveyAnswerIntf>());
             drugQuestions.add(question);
           } else {
             SurveyQuestionIntf drugQuestion = PreAnesthesiaServiceUtils.getQuestion(drugQuestions,Integer.parseInt(question.getAttribute(Constants.ORDER)) - 1 );
             if (drugQuestion != null) {
               for (SurveyAnswerIntf answerIntf : question.getAnswers()) {
                 InputElement input = (InputElement) answerIntf;
                 if (input.getValue() == null || input.getValue().isEmpty()) {
                   input.setValue("-");
                 }
                 String label = input.getValue();
                 String value = input.getLabel().substring(2);
                 input.setLabel(label);
                 input.setValue(value);
                 drugQuestion.getAnswers().add(input);
               }
             }
           }
        }
      }
      return drugQuestions;
    } catch (ParserConfigurationException | SAXException | IOException e) {
      logger.error(
          "Error getting questions for patientStudy token " + patStudy.getToken() + " study "
              + patStudy.getStudyCode(), e);
    }
    return drugQuestions;
  }
}

  private void addAnswered(ArrayList<SurveyQuestionIntf> questions, SurveyQuestionIntf thisQuestion) {
    if (thisQuestion.getAnswered()) {
      questions.add(thisQuestion);
    }
  }

  private void addUnanswered(ArrayList<SurveyQuestionIntf> questions, SurveyQuestionIntf question) {
    if (question.getAttribute("Visible") != null && question.getAttribute("Visible").toLowerCase().equals("false") ) {
      return;
    }
    if (!question.getAnswered() && question.getAnswers().size() > 0
        ) {
      RegistryQuestion returnQuestion = new RegistryQuestion();

      for (String qt : question.getText()) {
        logger.debug("Adding in question: " + qt);
        returnQuestion.addText(qt);
      }
      if (question.getAnswers().get(0).getType() == Constants.TYPE_SELECT1) {
        InputElement returnAnswer = new InputElement();
        returnAnswer.setLabel("");
        returnAnswer.setValue("-");
        returnQuestion.setAnswers(new ArrayList<SurveyAnswerIntf>());
        returnQuestion.getAnswers().add(returnAnswer);
        returnQuestion.setAnswered(true);

      } else if (question.getAnswers().get(0).getType() == Constants.TYPE_SELECT) {
        returnQuestion.setAnswered(true);
      }
      questions.add(returnQuestion);
    }
  }
  private class PacVicesProvider extends RegistryShortFormScoreProvider {

    public PacVicesProvider(Supplier<Database> dbp, SiteInfo siteInfo) {
      super(dbp, siteInfo);
    }

    @Override
    public ArrayList<SurveyQuestionIntf> getSurvey(PatientStudyExtendedData patStudy, PrintStudy study,
                                                   Patient patient, boolean allAnswers) {

      Document doc;
      ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();

      try {
        doc = ScoreService.getDocument(patStudy);
        if (doc == null) {
          return questions;
        }
        NodeList itemList = doc.getElementsByTagName(Constants.ITEM);
        if (itemList == null || itemList.getLength() == 0) {
          return questions;
        }
        RegistryQuestion returnQuestion = null;
        InputElement returnAnswer = new InputElement();
        returnAnswer.setLabel("");
        returnAnswer.setValue("");
        StringBuilder questionText = new StringBuilder();

        for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
          Element itemNode = (Element) itemList.item(itemInx);
          RegistryQuestion question = RegistryAssessmentUtils.getQuestion(itemNode, itemInx, allAnswers);

          if (question.getAttribute("print") == null
              || "true".equals(question.getAttribute("print").toLowerCase())) {
            ArrayList<SurveyAnswerIntf> answers = question.getAnswers(true);
            String orderNumberStr = question.getAttribute(Constants.ORDER);
            Integer orderNumber = null;
            try {
              orderNumber = Integer.parseInt(orderNumberStr);
            } catch (NumberFormatException nfe) {
              orderNumber = 0;
            }

            if (answers != null) {
              if ("pacSmoking".equals(study.getStudyDescription())) {
                switch (orderNumber) {
                case 1:
                  returnQuestion = question;
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_SELECT1) {
                      if ("1".equals(getSelectedValue((Select1Element) answerIntf))) {
                        questionText.append("Smoked ");
                      } else {
                        questionText.append("Never smoked");
                      }
                    }
                  }
                  addAnswered(questions, question);
                  break;
                case 2:
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_INPUT) {
                      String val = ((InputElement) answerIntf).getValue();
                      if (val != null && !val.isEmpty()) {
                        questionText.append(val + " pack(s) per day ");
                      }
                    }
                  }
                  break;
                case 3:
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_INPUT) {
                      String val = ((InputElement) answerIntf).getValue();
                      if (val != null && !val.isEmpty()) {
                        questionText.append(val + " years");
                      }

                    }
                  }
                  break;
                case 4:
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_INPUT) {
                      String val = ((InputElement) answerIntf).getValue();
                      if (val != null && !val.isEmpty()) {
                        returnAnswer.setLabel("Quit");
                        returnAnswer.setValue(val);
                      }
                    }
                  }
                }
              } else if ("pacAlcohol".equals(study.getStudyDescription())) {
                logger.debug("processing alcohol question #" + orderNumber);

                switch (orderNumber) {
                case 1:  //Do you drink alcoholic beverages
                  returnQuestion = question;
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_SELECT1) {
                      if ("1".equals(getSelectedValue((Select1Element) answerIntf))) {
                        questionText.append("Do drink alcohol. ");
                      } else {
                        questionText.append("Do not drink alcohol. ");
                      }
                    }
                  }
                  addAnswered(questions, question);
                  break;
                case 2:
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_SELECT1) {
                      questionText.append(getSelectedLabel((Select1Element) answerIntf));
                      questionText.append(" drinks per week.");
                    }
                  }
                  break;
                case 3:
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_SELECT1) {
                      if ("1".equals(getSelectedValue((Select1Element) answerIntf))) {
                        questionText.append("Every day. ");
                      } else {
                        question.addText("Not every day. ");
                      }
                    }
                  }
                  break;
                case 4:
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_INPUT) {
                      String val = ((InputElement) answerIntf).getValue();
                      if (val != null && !val.isEmpty()) {
                        returnAnswer.setLabel(val);
                        returnAnswer.setValue(" drinks per day");

                      }
                    }
                  }
                }
              } else if ("pacDrugs".equals(study.getStudyDescription())) {
                switch (orderNumber) {
                case 1:  // Used drugs in last 5 years
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_SELECT1) {
                      if ("1".equals(getSelectedValue((Select1Element) answerIntf))) {
                        replaceText(question.getText(), "Recreational drug use in the last 5 years");
                      } else {
                        replaceText(question.getText(), "Have not used recreational drugs in the last 5 years");
                      }
                      question.setAnswers(new ArrayList<SurveyAnswerIntf>());
                    }
                  }
                  addAnswered(questions, question);
                  break;
                case 2:
                case 5:
                case 8:
                case 11:
                case 14:
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_SELECT) {
                      question.addText(getSelectedLabel((SelectElement) answerIntf));
                      question.setAnswers(new ArrayList<SurveyAnswerIntf>());
                      addAnswered(questions,question);
                    }
                  }
                  break;
                case 15:
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_INPUT) {
                      String val = ((InputElement) answerIntf).getValue();
                      if (val != null && !val.isEmpty()) {
                        RegistryQuestion drugTypeQuestion = (RegistryQuestion) PreAnesthesiaServiceUtils.getQuestion(questions, 14);
                        if (drugTypeQuestion != null) {
                          if (drugTypeQuestion.getText() == null || drugTypeQuestion.getText().size() < 1) {
                            drugTypeQuestion.addText(val);
                          } else {
                            drugTypeQuestion.getText().set(0, val);
                          }
                        }
                      }
                    }
                  }
                  break;
                case 3:
                case 6:
                case 9:
                case 12:
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_SELECT1) {
                      InputElement input = new InputElement();
                      input.setLabel(getSelectedLabel((Select1Element) answerIntf));
                      input.setValue(" days per week");
                      SurveyQuestionIntf drugTypeQuestion = PreAnesthesiaServiceUtils.getQuestion(questions,
                          orderNumber - 1);
                      if (drugTypeQuestion != null) {
                        ArrayList<SurveyAnswerIntf> answerIntfs = drugTypeQuestion.getAnswers();
                        if (answerIntfs == null) {
                          answerIntfs = new ArrayList<>();
                        }
                        answerIntfs.add(input);
                      }
                    }
                  }
                  break;
                case 16:
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_SELECT1) {
                      InputElement input = new InputElement();
                      input.setLabel(getSelectedLabel((Select1Element) answerIntf));
                      input.setValue(" days per week");
                      SurveyQuestionIntf drugTypeQuestion = PreAnesthesiaServiceUtils.getQuestion(questions,
                          orderNumber - 2);
                      if (drugTypeQuestion != null) {
                        ArrayList<SurveyAnswerIntf> answerIntfs = drugTypeQuestion.getAnswers();
                        if (answerIntfs == null) {
                          answerIntfs = new ArrayList<>();
                        }
                        answerIntfs.add(input);
                      }
                    }
                  }
                  break;
                case 4:
                case 7:
                case 10:
                case 13:
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_INPUT) {
                      String val = ((InputElement) answerIntf).getValue();
                      if (val != null && !val.isEmpty()) {
                        SurveyQuestionIntf mainQuestion = PreAnesthesiaServiceUtils.getQuestion(questions,
                            orderNumber - 2);
                        if (mainQuestion != null) {
                          InputElement input = (InputElement) answerIntf;
                          input.setLabel(input.getValue());
                          input.setValue(" per day");
                          mainQuestion.getAnswers().add(input);
                        }
                      }
                    }
                  }
                  break;
                case 17:
                  for (SurveyAnswerIntf answerIntf : answers) {
                    if (answerIntf.getSelected() && answerIntf.getType() == Constants.TYPE_INPUT) {
                      String val = ((InputElement) answerIntf).getValue();
                      if (val != null && !val.isEmpty()) {
                        SurveyQuestionIntf drugTypeQuestion = PreAnesthesiaServiceUtils.getQuestion(questions,
                            orderNumber - 3);
                        if (drugTypeQuestion != null) {
                          InputElement input = (InputElement) answerIntf;
                          input.setLabel(input.getValue());
                          input.setValue(" per day");
                          drugTypeQuestion.getAnswers().add(input);
                        }
                      }
                    }
                  }
                  break;
                }
              }
            }
          }

          if (returnQuestion != null) {
            if (returnQuestion.getText() != null && returnQuestion.getText().size() > 0) {
              returnQuestion.getText().set(0, questionText.toString());
            }
            ArrayList<SurveyAnswerIntf> returnAnswers = new ArrayList<>();
            returnAnswers.add(returnAnswer);
            returnQuestion.setAnswers(returnAnswers);
            //questions.add(returnQuestion);
          }

          if (!question.getAnswered()) {
            addUnanswered(questions, question);
          }
        }
      } catch (ParserConfigurationException | SAXException | IOException e) {
        logger.error(
            "Error getting questions for patientStudy token " + patStudy.getToken() + " study "
                + patStudy.getStudyCode(), e);
      }
      return questions;
    }
  }

  private String getSelectedValue(Select1Element select1Element) {
    for (SelectItem item : select1Element.getItems()) {
      if (item.getSelected()) {
        return (item.getValue());
      }
    }
    return "";
  }

  private String getSelectedLabel(Select1Element select1Element) {
    for (SelectItem item : select1Element.getItems()) {
      if (item.getSelected()) {
        return (item.getLabel());
      }
    }
    return "";
  }

  private String getSelectedLabel(SelectElement selectElement) {
    for (SelectItem item : selectElement.getItems()) {
      if (item.getSelected()) {
        return (item.getLabel());
      }
    }
    return "";
  }

  private void replaceText(ArrayList<String> questionText, String newText) {
    if (questionText == null) {
      questionText = new ArrayList<>();
    }
    if (questionText.size() > 0) {
      questionText.set(0, newText);
    } else {
      questionText.add(newText);
    }
  }


  private void addColon(SurveyAnswerIntf answerIntf) {
    // don't add question again, this response is same question as ISSPROBLEMS
    if (Constants.TYPE_INPUT == answerIntf.getType()) {
      ((InputElement) answerIntf).setLabel(
          ((InputElement) answerIntf).getLabel() + ": ");
    }
  }



  public String getTitle() {
    return "Pre Anesthesia clinic survey service";
  }

  private class LocalSurveyAdvance extends SurveyAdvanceBase {
    LocalSurveyAdvance(SiteInfo siteInfo) {
      super(siteInfo);
    }

    @Override
    public String getQuestionId(  String sectionId, String[] fieldIdParts) {
      if ("pacMenstrual".equals(sectionId) || "pacSmoking".equals(sectionId) || "pacAlcohol".equals(sectionId) ||
          "pacDrugs".equals(sectionId) || "pacFollowUp".equals(sectionId) || "pacPainDoc".equals(sectionId) || "pacOpioids".equals(sectionId)) {
        return "Order1";
      }
      if ("pacQuestions".equals(sectionId)) {
        return "Order" + fieldIdParts[0];
      }

      int questionNo = 1;
      try {
        if (fieldIdParts != null && fieldIdParts.length > 0) {
          questionNo = Integer.parseInt(fieldIdParts[0]);
        }
      } catch (NumberFormatException nfe) {
        // keep default value
      }

      if ("pacIssues".equals(sectionId)) {
        switch(questionNo) {
        case 13:
        case 14:
          return "Order13";
        case 15:
        case 16:
          return "Order15";
        case 17:
        case 18:
        case 19:
        case 20:
          return "Order17";
        case 21:
          return "Order21";
        case 23:
          return "Order23";
        default:
          return "Order1";
        }
      } else if ("pacIllness".equals(sectionId) || "pacContactInfo".equals(sectionId)) {
        return (questionNo < 3) ? "Order1" : "Order3";
      }

      return super.getQuestionId(sectionId, fieldIdParts);
    }
    @Override
    public String getFieldId(SquareXml squareXml, int inx ) {
      String fieldId = super.getFieldId(squareXml, inx);
      String[] parts = getParts(fieldId);
      String[] refNames = {"ISSPROBLEMS","ISSPRESENT","ILLNESSTYPE","EXERCISE"};
      if (parts.length == 4) {
        for (int i=0; i<refNames.length; i++) {
          if (parts[3].startsWith(refNames[i])) {
            return parts[0] + ":" + parts[1] + ":" + parts[2] +":" +refNames[i];
          }
        }
        if ("ISSCHRONICNO".equals(parts[3])) {
          return fieldId + "NE";
        }
      } else if (parts.length == 3) {
        if ("SMOKEMUCH4".equals(parts[2])) {
          return parts[0] + ":" + parts[1] + ":SMOKEMUCH";
        }
      }
      return fieldId;
    }
    public String[] getParts(String fieldId) {
      if (fieldId != null) {
        return fieldId.split(":");
      }
      return new String[0];
    }
  }
}