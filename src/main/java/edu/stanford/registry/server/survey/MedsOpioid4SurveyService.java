/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.xform.Select1Element;
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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseException;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;


public class MedsOpioid4SurveyService extends NamedSurveyService implements CustomSurveyServiceIntf  {

  public final static String studyName = "medsOpioid4A";
  private final static String emptyForm = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items></Items></Form>";

  private final String surveySystemName = "edu.stanford.registry.server.survey.MedsOpioid4SurveyService";
  private static final Logger logger = LoggerFactory.getLogger(MedsOpioid4SurveyService.class);
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private final String[] questionText = {"How much has this improved your activity or general level of function?",
      "How did the medication affect your pain level?",
      "Are you taking your opioid pain medications any differently than prescribed by your doctor?",
      "Are you having any problematic side effects?" };
  private final String[] q1Answers = { "Improved very much", "Improved quite a bit", "Improved somewhat", "Improved slightly", "Had no effect", "Worsened slightly", "Worsened somewhat", "Worsened quite a bit", "Worsened very much"};
  private final String[] q2Answers = { "Not at all", "Just take the edge off", "Somewhat helpful", "Quite a bit", "Very much" };
  private final String[] q3Answers = { "Taking more than prescribed", "Stockpiling medications for a \"rainy day\"", "Changing the dosing frequency", "Getting additional pills from friends and family", "Not taking as much as prescribed", "Not taking the medications"};
  private final String[] q4Answers = { "Constipation", "Loss of sexual interest", "Exquisite sensitivity to pain", "Slowed thinking", "Nausea", "Drowsiness", "Others", "Weight gain", "Weight loss"};
  private final ArrayList<String[]> allAnswers = new ArrayList<>();

  public MedsOpioid4SurveyService(SiteInfo siteInfo) {
    super(siteInfo);

    allAnswers.add(q1Answers);
    allAnswers.add(q2Answers);
    allAnswers.add(q3Answers);
    allAnswers.add(q4Answers);
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
      String answerJson) {
    /*
     * For Initial surveys:
     * See if they answered 'still taking' to any of the opioid medications
     * For Follow up surveys:
     * See if they answered 'Yes' to the OpioidSurveyService question 'Are you currently taking any opioid medications..'
     */
    PatientStudy patStudy;
    ArrayList<String> opioidMeds;
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    try {
      patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
      if (patStudy == null || patStudy.getContents() != null) { // doesn't exist or already answered
        return null;
      }
      AssessDao assessDao = new AssessDao(database, siteInfo);
      SurveyRegistration surveyRegistration = assessDao.getSurveyRegistrationByRegId(patStudyExtended.getSurveyRegId());
      if (surveyRegistration.getSurveyType().startsWith("Initial")) {
        opioidMeds = opioidMedsStillTaking(database, patStudyDao, patStudyExtended.getPatientId(), patStudy.getSurveyRegId());
      } else {
        opioidMeds = opioidsCurrentlyTaking(database, patStudyDao, patStudy.getSurveyRegId());
      }
    } catch (DatabaseException e) {
      logger.error(e.toString(), e);
      throw new DataException(e.toString());
    }

    if (opioidMeds == null || opioidMeds.size() < 1){
      /* write empty results */
      saveEmptyForm(database, patStudy);
      NextQuestion next = new NextQuestion();

      DisplayStatus displayStatus = factory.displayStatus().as();
      displayStatus.setQuestionType(QuestionType.skip);
      displayStatus.setSurveyProviderId(Integer.toString(getSurveySystemId(database)));
      displayStatus.setSurveySectionId(Integer.toString(patStudy.getStudyCode()));
      next.setDisplayStatus(displayStatus);
      //next.setQuestion(getFormQuestion(opioidMeds));
      return next;

    }

    /*
     * See if this is an answer
     */
    if (submitStatus != null) {
      AutoBean<FormQuestion> question = getFormQuestion(opioidMeds);
      FormQuestion formQuestion =  question.as();
      FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
      saveResponse(database, patStudy, formQuestion, formAnswer);
      return null;
    }

    /*
     * Send back the question
     */
    NextQuestion next = new NextQuestion();

    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setQuestionType(QuestionType.form);
    displayStatus.setSurveyProviderId(Integer.toString(getSurveySystemId(database)));
    displayStatus.setSurveySectionId(Integer.toString(patStudy.getStudyCode()));

    next.setDisplayStatus(displayStatus);
    next.setQuestion(getFormQuestion(opioidMeds));
    return next;
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(Database database, PatientStudy study, User user) {
    return getRegistrySurvey(database, study, user);
  }

  @Override
  public String getAssessments(Database database, int version) {

    StringBuilder myStudies = new StringBuilder();
    myStudies.append("<forms>");
    myStudies.append("<form OID=\"");
    String sql = "select studyCode,  studyDescription, title from study where survey_system_id = "
    + mySurveySystem.getInstance(database, getSurveySystemName()).getSurveySystemId();

    final ArrayList<Study> studies =  database.toSelect(sql).query(rs -> {
      ArrayList<Study> studies1 = new ArrayList<>();
      while (rs.next()) {
        Study study = new Study();
        study.setStudyCode(rs.getIntegerOrNull("studyCode"));
        study.setStudyDescription(rs.getStringOrNull("studyDescription"));
        study.setTitle(rs.getStringOrNull("title"));
        studies1.add(study);
      }
      return studies1;
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
    return new MedsOpioid4ScoreProvider();
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
    return null;
  }

  @Override
  public void setValue(String value) {
  }

  private AutoBean<FormQuestion > getFormQuestion(ArrayList<String> opioidMedsTaking) {
    StringBuilder medications = new StringBuilder();
    String sep = "";
    for (String med : opioidMedsTaking) {
      medications.append(sep);
      // pick out the generic name
      int open = med.indexOf("(") + 1;
      int clos = med.indexOf(")");
      if (open > -1 && clos > -1 && clos > open && med.length() > clos) {
        medications.append(med, open, clos);
      } else {
        medications.append(med);
      }
      sep = ", ";
    }
    AutoBean<FormQuestion> bean = factory.formQuestion();
    FormQuestion question = bean.as();
    ArrayList<FormField> fields = new ArrayList<>();
    question.setTitle1("You indicated that you are taking opioid pain medications: " + medications.toString() + ".");
    FormField field1 = factory.field().as();
    field1.setType(FieldType.heading);
    field1.setFieldId("0:0:FOURA1");
    field1.setLabel(questionText[0]);
    fields.add(field1);
    FormField field2 = factory.field().as();
    field2.setType(FieldType.radios);
    field2.setFieldId("0:1:FOURA1");
    field2.setRequired(false);
    ArrayList<FormFieldValue> values = new ArrayList<>();
    for (int inx=0; inx < q1Answers.length; inx++) {
      values.add(getFormFieldValue(q1Answers[inx], Integer.valueOf(inx).toString()));
    }
    field2.setValues(values);
    fields.add(field2);

    FormField field3 = factory.field().as();
    field3.setType(FieldType.heading);
    field3.setFieldId("1:0:FOURA2");
    field3.setLabel(questionText[1]);
    fields.add(field3);
    FormField field4 = factory.field().as();
    field4.setType(FieldType.radios);
    field4.setFieldId("1:1:FOURA2");
    field4.setRequired(false);
    field4.setValues(new ArrayList<>());
    for (int inx=0; inx < q2Answers.length; inx++) {
      field4.getValues().add(getFormFieldValue( q2Answers[inx], Integer.valueOf(inx).toString()));
    }
    fields.add(field4);

    FormField field5 = factory.field().as();
    field5.setType(FieldType.heading);
    field5.setFieldId("2:0:FOURA3");
    field5.setLabel(questionText[2]);
    fields.add(field5);
    FormField field6 = factory.field().as();
    field6.setType(FieldType.checkboxes);
    field6.setFieldId("2:1:FOURA3");
    field6.setRequired(false);
    field6.setValues(new ArrayList<>());
    for (int inx=0; inx < q3Answers.length; inx++) {
      field6.getValues().add(getFormFieldValue(q3Answers[inx], Integer.valueOf(inx).toString()));
    }
    fields.add(field6);

    FormField field7 = factory.field().as();
    field7.setType(FieldType.heading);
    field7.setFieldId("3:0:FOURA4");
    field7.setLabel(questionText[3]);
    fields.add(field7);
    FormField field8 = factory.field().as();
    field8.setType(FieldType.checkboxes);
    field8.setFieldId("3:1:FOURA4");
    field8.setRequired(false);
    field8.setValues(new ArrayList<>());
    for (int inx=0; inx < q4Answers.length; inx++) {
      field8.getValues().add(getFormFieldValue(q4Answers[inx], Integer.valueOf(inx).toString()));
    }
    fields.add(field8);

    question.setFields(fields);

    return bean;
  }

  private void saveResponse(Database database, PatientStudy patStudy, FormQuestion formQuestion, FormAnswer formAnswer) {
    try {
      Map<String, Map<String, FormFieldAnswer>> itemOrderToResponseOrderToField = new HashMap<>();
      for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
        // Format the fieldId as "ItemOrder:ResponseOrder:Ref"
        String[] ids = fieldAnswer.getFieldId().split(":");
        String itemOrder = ids[0];
        String responseOrder = ids[1];
        Map<String, FormFieldAnswer> responseOrderToField = itemOrderToResponseOrderToField.get(itemOrder);
        if (responseOrderToField == null) {
          responseOrderToField = new HashMap<>();
          itemOrderToResponseOrderToField.put(itemOrder, responseOrderToField);
        }
        responseOrderToField.put(responseOrder, fieldAnswer);
      }

      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = db.newDocument();
      Element formElement = doc.createElement("Form");
      Element itemsElement = doc.createElement("Items");
      for (int q=0; q<questionText.length; q++) {
        String itemOrder = (Integer.valueOf(q)).toString();
        String[] answerText = allAnswers.get(q);
        boolean[] ansSelected = new boolean[answerText.length];
        for (@SuppressWarnings("unused") String anAnswerText : answerText) {
          ansSelected[0] = false;
        }

        Map<String, FormFieldAnswer> responseOrderToField = itemOrderToResponseOrderToField.get(itemOrder);
        if (responseOrderToField !=null) {
          for (Entry<String, FormFieldAnswer> field : responseOrderToField.entrySet()) {
            for (String v : field.getValue().getChoice()) {
              logger.debug("question " + q + " selected response " + v);
              if (v != null) {
                logger.debug("question " + q + " selected response value is " + answerText[Integer.parseInt(v)]);
                ansSelected[Integer.parseInt(v)] = true;
              }
            }
          }
        }
        // Now create the xml item
        Element newItemElement = doc.createElement(Constants.ITEM);
        newItemElement.setAttribute("Order", (Integer.valueOf(q).toString()));
        newItemElement.setAttribute("ItemResponse", "1");
        newItemElement.setAttribute("ItemScore","");

        Element descElement = doc.createElement("Description");
        descElement.appendChild(doc.createTextNode(questionText[q]));
        newItemElement.appendChild(descElement);

        Element responsesElement = doc.createElement("Responses");
        Element responseElement = doc.createElement("Response");
        responseElement.setAttribute("Order","1");
        if (q < 2) {
          responseElement.setAttribute("Type", "select1");
        } else {
          responseElement.setAttribute("Type","select");
        }
        boolean response=false;
        for (boolean anAnsSelected : ansSelected) {
          if (anAnsSelected) {
            response = true;
          }
        }
        responseElement.setAttribute("Appearance","full");
        if (!response) {
          responseElement.appendChild(getResponseItemElement(doc, answerText.length, "No", true));
        }
        for (int a=0; a<ansSelected.length; a++) {
          responseElement.appendChild(getResponseItemElement(doc, a, answerText[a], ansSelected[a]));
        }
        responsesElement.appendChild(responseElement);
        newItemElement.appendChild(responsesElement);
        itemsElement.appendChild(newItemElement);
      }
      formElement.appendChild(itemsElement);
      doc.appendChild(formElement);

      Transformer trans = TransformerFactory.newInstance().newTransformer();
      trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      trans.setOutputProperty(OutputKeys.INDENT, "yes");
      StringWriter strWriter = new StringWriter();
      trans.transform(new DOMSource(doc), new StreamResult(strWriter));

      patStudy.setContents(strWriter.toString());
      PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
      patStudyDao.setPatientStudyContents(patStudy, true);
    } catch (Exception e) {
      logger.error("Error creating xml for medsopioid4 study " + patStudy.getStudyCode() +" for token " + patStudy.getToken(), e);
     }
  }

  private void saveEmptyForm(Database database, PatientStudy patStudy) {
    try {
      patStudy.setContents(emptyForm);
      PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
      patStudyDao.setPatientStudyContents(patStudy, true);
    } catch (Exception e) {
      logger.error("Error creating empty xml for medsopioid4 study " + patStudy.getStudyCode() +" for token " + patStudy.getToken(), e);
    }
  }

  private FormFieldValue getFormFieldValue(String label, String value) {
    FormFieldValue ffValue = factory.value().as();
    ffValue.setId(value);
    ffValue.setLabel(label);
    return ffValue;
  }

  private ArrayList<String> opioidMedsStillTaking(Database database, PatStudyDao patStudyDao, String patient, Long surveyRegId) {
      ArrayList<String> medsStillTaking = new ArrayList<>();
      ArrayList<PatientStudyExtendedData> opioidSurveys = patStudyDao
          .getPatientStudyDataBySurveyRegIdAndStudyDescription(surveyRegId, "medsOpioid");
      if (opioidSurveys != null) {
        try {
        RegistryShortFormScoreProvider opioidSurveyService = new RegistryShortFormScoreProvider(database, siteInfo);
        for (PatientStudyExtendedData patientStudy : opioidSurveys) {
          Study medsOpioidStudy = new Study(patientStudy.getSurveySystemId(), patientStudy.getStudyCode(), patientStudy.getStudyDescription(), patientStudy.getMetaVersion());
          PrintStudy pStudy = new PrintStudy(siteInfo, medsOpioidStudy, patientStudy.getSurveySystemName());
          ArrayList<SurveyQuestionIntf> questions = opioidSurveyService.getSurvey(patientStudy, pStudy, patientStudy.getPatient(), true);
          boolean med = true;
          String medNameTaking = null;

          for (SurveyQuestionIntf question : questions) {
            ArrayList<SurveyAnswerIntf> ans = question.getAnswers(true);
            if (med) {
              if (ans != null && ans.size() > 0) {
                SurveyAnswerIntf answer = ans.get(0);
                if (answer.getType() == Constants.TYPE_SELECT) {
                  SelectElement select = (SelectElement) answer;
                  ArrayList<SelectItem> selectItems = select.getSelectedItems();
                  for (SelectItem item : selectItems) {
                    medNameTaking = item.getLabel();
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
                      medsStillTaking.add(medNameTaking);
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
          logger.error("Error checking medsOpioid answers", e);
        }
      }
      return medsStillTaking;
  }

  private Element getResponseItemElement(Document doc, int answerNumber, String answer, boolean selected) {
    Element responseItemElement = doc.createElement(Constants.XFORM_ITEM);
    Element responseItemLabel = doc.createElement(Constants.XFORM_LABEL);
    responseItemLabel.appendChild(doc.createTextNode(answer));
    responseItemElement.appendChild(responseItemLabel);
    Element responseItemSelected = doc.createElement(Constants.XFORM_SELECTED);
    responseItemSelected.appendChild(doc.createTextNode((Boolean.valueOf(selected).toString())));
    responseItemElement.appendChild(responseItemSelected);
    Element responseItemValue = doc.createElement(Constants.XFORM_VALUE);
    responseItemValue.appendChild(doc.createTextNode((Integer.valueOf(answerNumber)).toString()));
    responseItemElement.appendChild(responseItemValue);
    return responseItemElement;
  }

  private ArrayList<String> opioidsCurrentlyTaking(Database database, PatStudyDao patStudyDao, Long surveyRegId) {
    ArrayList<String> opioidMeds = new ArrayList<>();
    ArrayList<PatientStudyExtendedData> opioidSurveys = patStudyDao
        .getPatientStudyDataBySurveyRegIdAndStudyDescription(surveyRegId, "opioidPromisSurvey");
    if (opioidSurveys != null) {
      try {
        SurveySystDao surveySystDao = new SurveySystDao(database);
        SurveySystem surveySystem = surveySystDao.getSurveySystem("edu.stanford.registry.server.survey.OpioidSurveysService");
        if (surveySystem == null) {
          return opioidMeds;
        }
        Study study = surveySystDao.getStudy(surveySystem.getSurveySystemId(), "opioidPromisSurvey");
        if (study == null) {
          return opioidMeds;
        }
        RegistryShortFormScoreProvider opioidSurveyService = new RegistryShortFormScoreProvider(database, siteInfo);
        for (PatientStudyExtendedData patientStudy : opioidSurveys) {
          PrintStudy pStudy = new PrintStudy(siteInfo, study, surveySystem.getSurveySystemName());
          ArrayList<SurveyQuestionIntf> questions = opioidSurveyService.getSurvey(patientStudy, pStudy, patientStudy.getPatient(), true);
          for (SurveyQuestionIntf question : questions) {
            if (question.getNumber() == 0) {
              ArrayList<SurveyAnswerIntf> ans = question.getAnswers(true);
              for (SurveyAnswerIntf answerIntf : ans) {
                if (answerIntf.getType() == Constants.TYPE_SELECT1) {
                  Select1Element select = (Select1Element) answerIntf;
                  ArrayList<String> ansStrings = select.getResponse();
                  if (ansStrings.size() > 0) {
                    logger.trace("Answer to question 0 is {}", ansStrings.get(0));
                    if ("Yes".equals(ansStrings.get(0))) {
                      opioidMeds.add("Yes");
                    }
                  }
                }
              }
            }
          }
        }
      } catch (DataException | InvalidDataElementException e) {
        logger.error("Error checking medsOpioid answers", e);
      }
    }
    return opioidMeds;
  }
}

