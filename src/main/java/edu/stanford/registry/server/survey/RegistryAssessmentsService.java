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

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.server.utils.StringUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.server.utils.XmlFormatter;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.Contingency;
import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.RegistryQuestion;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.xform.InputElement;
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;
import edu.stanford.survey.client.api.BodyMapAnswer;
import edu.stanford.survey.client.api.BodyMapQuestion;
import edu.stanford.survey.client.api.CollapsibleRadiosetQuestion;
import edu.stanford.survey.client.api.DisplayStatus;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.NumericAnswer;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.RadiosetAnswer;
import edu.stanford.survey.client.api.SliderQuestion;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
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

public class RegistryAssessmentsService extends SurveySiteBase {

  private static final Logger logger = Logger.getLogger(RegistryAssessmentsService.class);
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  private int version = 1;

  public RegistryAssessmentsService(SiteInfo siteInfo) {
    super(siteInfo);
  };

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    LocalSystem localSystem = LocalSystem.getInstance(database);
    Study study = new Study(localSystem.getSurveySystemId(), 0, name, 0);
    study.setTitle(title);
    study.setExplanation(explanation);
    SurveySystDao ssDao = new SurveySystDao(database);
    study = ssDao.insertStudy(study);
    return study;
  }

  @Override
  public void registerAssessment(Database database, Element questionaire, String patientId,
                                 Token tok, User user) {
    String studyName = questionaire.getAttribute(Constants.XFORM_VALUE);
    String qOrder = questionaire.getAttribute(Constants.XFORM_ORDER);
    String xmlFile = questionaire.getAttribute(Constants.XFORM_XML);

    // Handle survey builder contents by revision number
    String xmlValue = questionaire.getAttribute(Constants.XFORM_VALUE);
    if (xmlValue.startsWith(xmlFile + "@")) {
      xmlFile = xmlValue;
    }
    Integer order = Integer.valueOf(qOrder);

    // Get the study
    Study study = getStudy(database, xmlFile);

    // Add the study if it doesn't exist
    if (study == null) {
      study = registerAssessment(database, xmlFile, studyName, "");
    }

    // Get the patient and this study for this patient
    PatientDao patientDao = new PatientDao(database, siteId, user);
    Patient pat = patientDao.getPatient(patientId);
    PatStudyDao patStudyDao= new PatStudyDao(database, siteInfo);
    PatientStudy patStudy = patStudyDao.getPatientStudy(pat, study, tok);

    if (patStudy == null) { // not there yet so lets add it
      patStudy = new PatientStudy(siteId);
      patStudy.setExternalReferenceId(""); // the last question answered
      patStudy.setMetaVersion(0);
      patStudy.setPatientId(pat.getPatientId());
      patStudy.setStudyCode(study.getStudyCode());
      patStudy.setSurveySystemId(study.getSurveySystemId());
      patStudy.setToken(tok.getToken());
      patStudy.setOrderNumber(order);
      // Write the patient study into the database
      patStudyDao.insertPatientStudy(patStudy);
    }
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExt,
                                     SubmitStatus submitStatus, String answer) {
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);

    // Get the patient study contents from the database
    PatientStudy patStudy = patStudyExt;
    Study study;
    try {
      patStudy = patStudyDao.getPatientStudy(patStudyExt, true);
      SurveySystDao ssDao = new SurveySystDao(database);
      study = ssDao.getStudy(patStudy.getSurveySystemId(), patStudy.getStudyCode());
    } catch (DatabaseException e) {
      logger.error(e.toString(), e);
      throw new DataException(e.toString());
    }
    return handleResponse(database, patStudy, study, submitStatus, answer);
  }

  private NextQuestion handleResponse(Database database, PatientStudy patStudy, Study study, SubmitStatus submitStatus,
                                     String answer) {
    try {
      String xmlDocumentString = patStudy.getContents();
      if (xmlDocumentString == null) {  // get the file
          xmlDocumentString = XMLFileUtils.getInstance(siteInfo).getXML(database, study.getStudyDescription());
      }
      if (xmlDocumentString == null)
        throw new Exception("Got a null document for study: "+study.getStudyDescription());

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));

      Document messageDom = db.parse(is);
      org.w3c.dom.Element docElement = messageDom.getDocumentElement();
      PatientDao patientDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
      Patient patient = patientDao.getPatient(patStudy.getPatientId());
      boolean setDtChanged = false;

      if (docElement.getTagName().equals(Constants.FORM)) {
        NodeList itemsList = messageDom.getElementsByTagName("Items");
        NodeList itemList = null;
        if (itemsList != null && itemsList.getLength() > 0) {
          itemList = ((Element) itemsList.item(0)).getElementsByTagName("Item");
        }
        if (itemsList == null || itemsList.getLength() < 1) {
          itemList = messageDom.getElementsByTagName("Item");
        }

        if (submitStatus != null) {
          int maxItemOrderUpdated = updateQuestion(database, messageDom, patient, itemList, submitStatus, answer, patientDao);

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
          xmlDocumentString = strWriter.toString();

          int lastQuestion = getLastQuestion(patient, itemList, xmlDocumentString, -1);
          // logger.debug("Updated question " + maxItemOrderUpdated + " lastQuestion = " + lastQuestion);
          if (maxItemOrderUpdated >= lastQuestion) {
            setDtChanged = true;
          }

          PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
          patStudy = patStudyDao.setPatientStudyContents(patStudy, xmlDocumentString, setDtChanged);

          if (setDtChanged) {
            assessmentCompleted(database, patient, patStudy);
          }
        }
        int numItems = RegistryAssessmentUtils.getIntegerAttribute(docElement, "questionsPerPage", 1);
        return getNextQuestion(database, patStudy, patient, itemList, numItems);
      } else {
        throw new DataException("docElement isn't form its " + docElement.getTagName());
      }

      // } catch (SQLException e) {
      // logger.error(e.toString(), e);
      // throw new DataException(e.toString());
    } catch (Exception e) {
      logger.error(e.toString(), e);
      throw new DataException(e.toString());
    }
  }

  /**
   * @return the largest order number of any item updated
   */
  private int updateQuestion(Database database, Document messageDom, Patient patient, NodeList itemList, SubmitStatus submitStatus,
                             String answerJson, PatientDao patientDao) {
    if (submitStatus.getQuestionType() == QuestionType.form) {
      FormAnswer answer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
      return updateQuestion(database, messageDom, patient, itemList, submitStatus, answer, patientDao);
    }

    int maxItemOrderUpdated = -1;
    int score = 0;
    for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
      Element itemNode = (Element) itemList.item(itemInx);

      // If this is the question then update the responses and score
      Integer ordr = RegistryAssessmentUtils.getIntegerAttribute(itemNode, Constants.ORDER, itemInx);
      if (itemNode.getAttribute("Order").equals(submitStatus.getQuestionId())) {
        if (ordr > maxItemOrderUpdated) {
          maxItemOrderUpdated = ordr;
        }

        StringBuilder answerBuf = new StringBuilder();
        ArrayList<Integer> selectedAnswers = new ArrayList<>();
        if ("surveyQuestionHorizontal".equals(itemNode.getAttribute("Class"))) {
          // Just used for pain intensity right now
          NumericAnswer answer = AutoBeanCodex.decode(factory, NumericAnswer.class, answerJson).as();
          if (answerBuf.length() > 0) {
            answerBuf.append(",");
          }
          // for each response; get the order # for the answer text
          String answerText =answer.getChoice()+"";
          Element responsesElement = (Element) itemNode.getElementsByTagName(Constants.RESPONSES).item(0);
          NodeList responseList = responsesElement.getElementsByTagName(Constants.RESPONSE);
          for (int respInx = 0; respInx < responseList.getLength(); respInx++) {
            Element response = (Element) responseList.item(respInx);
            String description = RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.DESCRIPTION);
            String value = RegistryAssessmentUtils.getAttributeOrElementText(response, "Value");
            if (answerText.equals(description) || answerText.equals(value)) {
              String responseOrder = response.getAttribute(Constants.ORDER);
              selectedAnswers.add(Integer.parseInt(responseOrder));
              answerBuf.append(responseOrder);
            }
          }
          //answerBuf.append(answer.getChoice());
          //selectedAnswers.add(answer.getChoice());
        } else if ("surveyQuestionBodymap".equals(itemNode.getAttribute("Class"))) {
          BodyMapAnswer answer = AutoBeanCodex.decode(factory, BodyMapAnswer.class, answerJson).as();
          if (answer.getRegionsCsv() != null && answer.getRegionsCsv().length() > 0) {
            if (answerBuf.length() > 0) {
              answerBuf.append(",");
            }
            answerBuf.append(answer.getRegionsCsv());
            score = answer.getRegionsCsv().split(",").length;
          }
        } else if ("surveyQuestionCollapsible".equals(itemNode.getAttribute("Class"))) {
          RadiosetAnswer answer = AutoBeanCodex.decode(factory, RadiosetAnswer.class, answerJson).as();

          String answerText = answer.getChoice();
          if (answerText != null) {
            Element responsesElement = (Element) itemNode.getElementsByTagName(Constants.RESPONSES).item(0);
            NodeList responseList = responsesElement.getElementsByTagName(Constants.RESPONSE);

            // for each response
            for (int respInx = 0; respInx < responseList.getLength(); respInx++) {
              Element response = (Element) responseList.item(respInx);
              String description = RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.DESCRIPTION);
              if (answerText.equals(description)) {
                String responseOrder = response.getAttribute(Constants.ORDER);
                selectedAnswers.add(Integer.parseInt(responseOrder));
                answerBuf.append(responseOrder);
              }
            }
          }
        } else {
          throw new RuntimeException("Unexpected type of Item: " + itemNode.getAttribute("Class"));
        }
        itemNode.setAttribute("ItemResponse", answerBuf.toString());

        // Calculate the score
        Element responsesElement = (Element) itemNode.getElementsByTagName(Constants.RESPONSES).item(0);
        NodeList responseList = responsesElement.getElementsByTagName(Constants.RESPONSE);

        for (Integer selectedAnswer : selectedAnswers) {
          // For each selected answers get the score
          int respInx = selectedAnswer;
          Element el = (Element) responseList.item(respInx);
          NodeList scoresList = el.getElementsByTagName(Constants.SCORES);
          if (scoresList != null && scoresList.getLength() > 0) {
            Element Scores = (Element) scoresList.item(0);
            String attribName = Scores.getAttribute("dataName");
            logger.debug("Attribute on the Scores for answer " + respInx + " is " + attribName);
            // See if the patient has the attribute on the scores tag and get
            // its value
            String patientAttributeValue = null;
            if (patient != null && attribName != null && patient.hasAttribute(attribName)) {
              patientAttributeValue = patient.getAttribute(attribName).getDataValue().toString();
            }

            // Get the score
            NodeList scoreList = Scores.getElementsByTagName(Constants.SCORE);
            logger.debug("Found " + scoreList.getLength() + " scores for this answer");
            for (int sc = 0; sc < scoreList.getLength(); sc++) {
              Element scoreElement = (Element) scoreList.item(sc);
              // if scored for this attribute or all (no attribute-value)
              String scoreAttributeValue = scoreElement.getAttribute("attribute-value");
              if (scoreAttributeValue == null || (scoreAttributeValue.length() < 1)
                  || (patientAttributeValue != null && patientAttributeValue.equals(scoreAttributeValue))) {
                String scoreValue = scoreElement.getAttribute(Constants.XFORM_VALUE);
                logger.debug("including score of " + scoreValue + " for Response " + respInx + " , "
                    + el.getAttribute(Constants.DESCRIPTION));
                try {
                  score = score + Integer.valueOf(scoreValue);
                  logger.debug("Added, score is now " + score);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              } // end if this score is for this patients attribute
            }
          } // end if Scores tag has elements
          doAttributeActions(patient, el, database, patientDao);
        } // for each of their responses
        // Get attributes effected by this answer
        logger.debug("Setting item " + ordr + " score to " + score);
        itemNode.setAttribute(Constants.ITEM_SCORE, score + "");
        // Finish time is necessary in case where body map has no regions selected (empty response value)
        itemNode.setAttribute(Constants.TIME_FINISH, new Date().getTime() + "");

      }
    }
    return maxItemOrderUpdated;
  }

  /**
   * @return the largest order number of any item updated
   */
  protected static int updateQuestion(Database database, Document messageDom, Patient patient, NodeList itemList, SubmitStatus submitStatus,
                             FormAnswer formAnswer, PatientDao patientDao) {
    // Index the fields of the form by order to match them up with Items below
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
    int maxItemOrderUpdated = -1;
    int score = 0;
    for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
      Element itemNode = (Element) itemList.item(itemInx);

      // If this is the question then update the responses and score
      Integer ordr = RegistryAssessmentUtils.getIntegerAttribute(itemNode, Constants.ORDER, itemInx);
      String itemOrder = itemNode.getAttribute("Order");
      if (itemOrderToResponseOrderToField.containsKey(itemOrder)) {
        if (ordr > maxItemOrderUpdated) {
          maxItemOrderUpdated = ordr;
        }

        StringBuilder answerBuf = new StringBuilder();
        ArrayList<String> selectedAnswers = new ArrayList<>();

        Map<String, FormFieldAnswer> responseOrderToField = itemOrderToResponseOrderToField.get(itemOrder);
        for (Entry<String, FormFieldAnswer> field : responseOrderToField.entrySet()) {
          Element response = RegistryAssessmentUtils.getResponse(field.getKey(), itemNode);
          if (response != null && response.getAttribute("Type") != null) {
            if ("input".equals(response.getAttribute("Type")) && !field.getValue().getChoice().isEmpty()) {
              String stringValue = field.getValue().getChoice().get(0);
              if (stringValue != null && stringValue.length() > 0) {
                stringValue = StringUtils.cleanString(stringValue);
                Element value = messageDom.createElement(Constants.XFORM_VALUE);
                value.appendChild(messageDom.createTextNode(stringValue));
                response.appendChild(value);
                selectedAnswers.add(field.getKey());
              }
            } else if ("select".equals(response.getAttribute("Type"))
                || "select1".equals(response.getAttribute("Type"))
                || "dropdown".equals(response.getAttribute("Type"))) {
              if (!field.getValue().getChoice().isEmpty()) {

                NodeList respItemList = response.getElementsByTagName(Constants.XFORM_ITEM);
                for (int itmInx = 0; itmInx < respItemList.getLength(); itmInx++) {
                  Element respItem = (Element) respItemList.item(itmInx);
                  Boolean selected = false;
                  NodeList valueNodes = respItem.getElementsByTagName(Constants.XFORM_VALUE);

                  if (valueNodes.getLength() > 0) {
                    NodeList valueNodeList = valueNodes.item(0).getChildNodes();
                    if (valueNodeList.getLength() > 0) {
                      String value = valueNodeList.item(0).getNodeValue();
                      if (value != null) {
                        for (String v : field.getValue().getChoice()) {
                          if (value.equals(v)) {
                            selected = true;
                            selectedAnswers.add(field.getKey());
                          }
                        }
                      }
                    }
                  }

                  respItem.setAttribute(Constants.XFORM_SELECTED, selected.toString());
                }
              }
            } else if ("textboxset".equals(response.getAttribute("Type").toLowerCase())) {
              if (!field.getValue().getChoice().isEmpty()) {
                NodeList respItemList = response.getElementsByTagName(Constants.XFORM_ITEM);
                if (respItemList != null) {
                  List<String> answers = field.getValue().getChoice();
                  if (answers != null) {
                    for (int itmInx = 0; itmInx < respItemList.getLength(); itmInx++) {
                      Element respItem = (Element) respItemList.item(itmInx);
                      if (answers.size() > itmInx) {
                        Element value;
                        NodeList valueNodes = respItem.getElementsByTagName(Constants.XFORM_VALUE);
                        if (valueNodes != null && valueNodes.getLength() > 0) {
                          value = (Element) valueNodes.item(0);
                        } else {
                          value = messageDom.createElement(Constants.XFORM_VALUE);
                          response.appendChild(value);
                        }
                        String stringValue = field.getValue().getChoice().get(itmInx);
                        if (stringValue != null && stringValue.length() > 0) {
                          stringValue = StringUtils.cleanString(stringValue);
                          value.appendChild(messageDom.createTextNode(stringValue));
                          selectedAnswers.add(field.getKey());
                        }
                      }
                    }
                  }
                }
              }
            } else if ("collapsible".equals(response.getAttribute("Type"))) {
              // nothing to update. Type is display only.
            } else if ("datepicker".equals(response.getAttribute("Type").toLowerCase())) {
              if (!field.getValue().getChoice().isEmpty() ) {
                String stringValue = field.getValue().getChoice().get(0);
                if (stringValue != null && stringValue.length() > 0) {
                  stringValue = StringUtils.cleanString(stringValue);
                  Element value = messageDom.createElement(Constants.XFORM_VALUE);
                  value.appendChild(messageDom.createTextNode(stringValue));
                  response.appendChild(value);
                  selectedAnswers.add(field.getKey());
                }
              }
            } else if ("slider".equals(response.getAttribute("Type").toLowerCase())) {
              if (!field.getValue().getChoice().isEmpty()) {
                  String stringValue = field.getValue().getChoice().get(0);
                if (stringValue != null && !stringValue.isEmpty()) {
                  stringValue = StringUtils.cleanString(stringValue);
                  Element value = messageDom.createElement(Constants.XFORM_VALUE);
                  value.appendChild(messageDom.createTextNode(stringValue));
                  response.appendChild(value);
                  selectedAnswers.add(field.getKey());
                }
              }
            } else if ("radiosetgrid".equals(response.getAttribute("Type").toLowerCase())) {
              // Handle the response json [FormFieldAnswer] updating the xml with the selections
              boolean answered = false;
              if (!field.getValue().getChoice().isEmpty()) {
                for (String choiceValue : field.getValue().getChoice()) {
                  String[] choiceValues = choiceValue.split(":");
                  String yRef = choiceValues[0];
                  String xVal = choiceValues[1];
                  NodeList respItemList = response.getElementsByTagName(Constants.XFORM_ITEM);
                  if (respItemList != null) {
                    for (int itmInx = 0; itmInx < respItemList.getLength(); itmInx++) {
                      Element respItem = (Element) respItemList.item(itmInx);
                      String elementGroup = RegistryAssessmentUtils.getAttributeOrElementText( respItem, "group");
                      if ( "y-axis".equals(elementGroup)) {
                        String elementRef = RegistryAssessmentUtils.getAttributeOrElementText(respItem, "ref");
                        if (yRef != null && yRef.equals(elementRef)) {
                          logger.debug("updating RadiosetGrid response for group:" + elementGroup + ", ref:" + elementRef
                              + " to selected:true with value:" + xVal);
                          Element value;
                          NodeList valueNodes = respItem.getElementsByTagName(Constants.XFORM_VALUE);
                          if (valueNodes != null && valueNodes.getLength() > 0) {
                            value = (Element) valueNodes.item(0);
                          } else {
                            value = messageDom.createElement(Constants.XFORM_VALUE);
                            respItem.appendChild(value);
                          }
                          value.appendChild(messageDom.createTextNode(xVal));
                          respItem.setAttribute(Constants.XFORM_SELECTED, "true");
                        }
                      }
                    }
                  }
                  answered = true;
                }
              }
              if (answered) {
                selectedAnswers.add(response.getAttribute(Constants.ORDER));
              }
            }
            else {
              throw new RuntimeException("Didn't understand response type " + response.getAttribute("Type"));
            }
          }
          if (selectedAnswers.size() > 0) {
            if (answerBuf.length() > 0) {
              answerBuf.append(",");
            }
            answerBuf.append(field.getKey());
          }
        }

        logger.debug("setting ItemResponse to " + answerBuf.toString());
        itemNode.setAttribute("ItemResponse", answerBuf.toString());

        // Calculate the score
        for (String respInx : selectedAnswers) {
          // For each selected answers get the score
          //Element el = (Element) responseList.item(respInx);
          Element el = RegistryAssessmentUtils.getResponse(respInx, itemNode);

          NodeList scoresList = el.getElementsByTagName(Constants.SCORES);
          if (scoresList != null && scoresList.getLength() > 0) {
            Element Scores = (Element) scoresList.item(0);
            String attribName = Scores.getAttribute("dataName");
            logger.debug("Attribute on the Scores for answer " + respInx + " is " + attribName);
            // See if the patient has the attribute on the scores tag and get
            // its value
            String patientAttributeValue = null;
            if (patient != null && attribName != null && patient.hasAttribute(attribName)) {
              patientAttributeValue = patient.getAttribute(attribName).getDataValue().toString();
            }

            // Get the score
            NodeList scoreList = Scores.getElementsByTagName(Constants.SCORE);
            logger.debug("Found " + scoreList.getLength() + " scores for this answer");
            for (int sc = 0; sc < scoreList.getLength(); sc++) {
              Element scoreElement = (Element) scoreList.item(sc);
              // if scored for this attribute or all (no attribute-value)
              String scoreAttributeValue = scoreElement.getAttribute("attribute-value");
              if (scoreAttributeValue == null || (scoreAttributeValue.length() < 1)
                  || (patientAttributeValue != null && patientAttributeValue.equals(scoreAttributeValue))) {
                String scoreValue = scoreElement.getAttribute(Constants.XFORM_VALUE);
                logger.debug("including score of " + scoreValue + " for Response " + respInx + " , "
                    + el.getAttribute(Constants.DESCRIPTION));
                try {
                  score = score + Integer.valueOf(scoreValue);
                  logger.debug("Added, score is now " + score);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              } // end if this score is for this patients attribute
            }
          } // end if Scores tag has elements
          doAttributeActions(patient, el, database, patientDao);
        } // for each of their responses

        //
        logger.debug("Setting item " + ordr + " score to " + score);
        itemNode.setAttribute(Constants.ITEM_SCORE, score + "");
        itemNode.setAttribute(Constants.TIME_FINISH, new Date().getTime() + "");

      }
    }
    // If there was no response in the formAnswer check if the question response is collapsible content only
    if (maxItemOrderUpdated < 0 && formAnswer != null && formAnswer.getFieldAnswers().size() == 0 && itemList != null && submitStatus != null) {
      for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
        Element itemNode = (Element) itemList.item(itemInx);
        if (itemNode.getAttribute("Order") != null &&
            (submitStatus.getQuestionId().equals("Order" + itemNode.getAttribute("Order")))) {
          Element responses = (Element) itemNode.getElementsByTagName(Constants.RESPONSES).item(0);
          if (responses !=null && responses.getElementsByTagName(Constants.RESPONSE) != null &&
              responses.getElementsByTagName(Constants.RESPONSE).getLength() == 1) {
            Element response = (Element) responses.getElementsByTagName(Constants.RESPONSE).item(0);
            if ("collapsible".equals(response.getAttribute("Type"))) {
              maxItemOrderUpdated = RegistryAssessmentUtils.getIntegerAttribute(itemNode, Constants.ORDER, itemInx);
              itemNode.setAttribute(Constants.TIME_FINISH, new Date().getTime() + "");
            }
            // or if just a statement with no responses
          } else if ((responses == null || responses.getElementsByTagName(Constants.RESPONSE) == null ||
              responses.getElementsByTagName(Constants.RESPONSE).getLength() == 0 ) && submitStatus != null) {
            maxItemOrderUpdated = RegistryAssessmentUtils.getIntegerAttribute(itemNode, Constants.ORDER, itemInx);
            itemNode.setAttribute(Constants.TIME_FINISH, new Date().getTime() + "");
          }
        }
      }
    }
    return maxItemOrderUpdated;
  }

  private NextQuestion getNextQuestion(Database database, PatientStudy patStudy, Patient patient, NodeList itemList, int numQuestions) {

    ArrayList<RegistryQuestion> questions = new ArrayList<>();
    ArrayList<Integer> selections = new ArrayList<>();
    QuestionMini miniQ;
    boolean pageSection = false;
    int index = 0;
    do {
      miniQ = null;
      int skipped=0;
      for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
        Element itemNode = (Element) itemList.item(itemInx);

        /*
         * Check if the question has a response or finished timestamp.
         */
        String itemResponse = itemNode.getAttribute(Constants.ITEM_RESPONSE);
        String timeFinished = itemNode.getAttribute(Constants.TIME_FINISH);

        if (((itemResponse == null || itemResponse.trim().length() == 0)
            && (timeFinished == null || timeFinished.trim().length() == 0))) {
          // check if conditional
          boolean meets = RegistryAssessmentUtils.qualifies(patient, itemNode, patStudy.getContents());
          // If they qualify for this question
          if (meets) {
            try {
              int ordr = RegistryAssessmentUtils.getIntegerAttribute(itemNode, Constants.ORDER, itemInx);
              boolean selected = false;
              // check we haven't already picked this one up
              for (Integer selection : selections) {
                if (ordr == selection) {
                  selected = true;
                }
              }
              // If not picked up yet and none selected or this one's order is
              // lower than pick it
              if (!selected && (miniQ == null || miniQ.getOrder() > ordr)) {
                miniQ = new QuestionMini(ordr, itemInx);
                index = itemInx;
              }
            } catch (Exception e) {
              logger.error("Error getting attribute 'Order' at index " + itemInx, e);
            }
          } else {
            logger.debug("Skipping item at index " + itemInx
                + " patient does not meet the condition required for the question ");
            skipped++;
          }
        } else {
          logger.debug("question at index " + itemInx + " has a response");
        }
      }

      if (miniQ==null && itemList.getLength() == skipped) {
        // Didn't qualify for any so write an empty <FORM/>
        String emptyForm= "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items/></Form>";
        PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
        patStudy = patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
      }

      // found one
      if (miniQ != null) {
        logger.debug("getting question # " + miniQ.getOrder());
        Element itemNode = (Element) itemList.item(miniQ.getIndex());
        if ("surveyQuestionHorizontal".equals(itemNode.getAttribute("Class"))) {
          // Just used for pain intensity right now
          assert numQuestions == 1;
          DisplayStatus displayStatus = factory.displayStatus().as();

          displayStatus.setQuestionType(QuestionType.numericScale);
          displayStatus.setQuestionId(itemNode.getAttribute("Order"));
          displayStatus.setSurveyProviderId(Integer.toString(patStudy.getSurveySystemId()));
          displayStatus.setSurveySectionId(Integer.toString(patStudy.getStudyCode()));

          AutoBean<SliderQuestion> bean = factory.sliderQuestion();
          SliderQuestion question = bean.as();

          RegistryQuestion rq = RegistryAssessmentUtils.getQuestion(itemNode, index);
          String[] titles = descriptionToTitles(rq);
          if (titles.length > 0) {
            question.setTitle1(titles[0]);
          }
          if (titles.length > 1) {
            question.setTitle2(titles[1]);
          }
          int highestOrder = 0;
          for (SurveyAnswerIntf answerIntf : rq.getAnswers()) {
            assert answerIntf.getType() == Constants.TYPE_RADIO;
            int order = Integer.parseInt(answerIntf.getAttribute("Order"));
            int value ;
            question.setShowValue(true);
            if (answerIntf.getAttribute("ShowValue") != null && answerIntf.getAttribute("ShowValue").equalsIgnoreCase("false")) {
              question.setShowValue(false);
            }
            if (answerIntf.getText().get(0).length() > 0 ) {
              value = Integer.parseInt(answerIntf.getText().get(0));

            } else {
              // Sometimes you want to leave the description field blank to show empty boxes for a Likert scale question
              // in this case supply the numeric value in an attribute by the name of "Value"
              // e.g. <Response Type="radio" Class="surveyAnswerHorizontal" Order="0" Value="0" ShowValue="false" Description=""
              //                Description2="Yes, that is true" ><Scores><Score value="0"/></Scores></Response>

              value = Integer.parseInt(answerIntf.getAttribute("Value"));
            }
            String label = answerIntf.getAttribute("Description2");
            if (answerIntf.getAttribute("Description3") != null) {
              label += " " + answerIntf.getAttribute("Description3");
            }
            if (order == 0) {
              question.setLowerBound(value);
              question.setLowerBoundLabel(label);
            } else if (order > highestOrder) {
              question.setUpperBound(value);
              question.setUpperBoundLabel(label);
              highestOrder = order;
            }
          }
          question.setRequired(true); // sliders are required by default; check for an override
          if (itemNode.getAttribute("required") != null && "false".equals(itemNode.getAttribute("required").toLowerCase())) {
            question.setRequired(false);
          }
          NextQuestion next = new NextQuestion();
          next.setDisplayStatus(displayStatus);
          next.setQuestion(bean);
          return next;
        } else if ("surveyQuestionBodymap".equals(itemNode.getAttribute("Class"))) {
          assert numQuestions == 1;
          DisplayStatus displayStatus = factory.displayStatus().as();

          displayStatus.setQuestionType(QuestionType.bodyMap);
          displayStatus.setQuestionId(itemNode.getAttribute("Order"));
          displayStatus.setSurveyProviderId(Integer.toString(patStudy.getSurveySystemId()));
          displayStatus.setSurveySectionId(Integer.toString(patStudy.getStudyCode()));

          AutoBean<BodyMapQuestion> bean = factory.bodyMapQuestion();
          BodyMapQuestion question = bean.as();

          RegistryQuestion rq = RegistryAssessmentUtils.getQuestion(itemNode, index);

          NodeList imgList = itemNode.getElementsByTagName("img");
          NodeList mapList = itemNode.getElementsByTagName("map");

          for (int mapInx = 0; mapInx < mapList.getLength(); mapInx++) {
            Element mapElement = (Element) mapList.item(mapInx);
            int mapOrder = RegistryAssessmentUtils.getIntegerAttribute(mapElement, Constants.ORDER, 0);
            String name = "#" + RegistryAssessmentUtils.getAttributeOrElementText(mapElement, "name");
            try {

              if (mapOrder == 1) {
                question.setMapTag1(XmlFormatter.format(mapElement));
                for (int imgInx = 0; imgInx < imgList.getLength(); imgInx++) {
                  if (name.equals(((Element) imgList.item(imgInx)).getAttribute("usemap"))) {
                    question.setImgTag1(XmlFormatter.format((Element) imgList.item(imgInx)));
                  }
                }
              }
              if (mapOrder == 2) {
                question.setMapTag2(XmlFormatter.format(mapElement));
                for (int imgInx = 0; imgInx < imgList.getLength(); imgInx++) {
                  if (name.equals(((Element) imgList.item(imgInx)).getAttribute("usemap"))) {
                    question.setImgTag2(XmlFormatter.format((Element) imgList.item(imgInx)));
                  }
                }
              }
            } catch (TransformerFactoryConfigurationError | TransformerException e) {
              logger.error("Unable to generate string from map element " + mapOrder, e);

            }

          }
          if (itemNode.getElementsByTagName("highlight") != null && itemNode.getElementsByTagName("highlight").getLength() > 0) {
            Element highlightNode = (Element) itemNode.getElementsByTagName("highlight").item(0);
            String highlightColor = RegistryAssessmentUtils.getAttributeOrElementText(highlightNode, "color");
            if (highlightColor != null && !highlightColor.isEmpty()) {
              question.setHighlightColor(highlightColor);
            }
            String fillOpacity = RegistryAssessmentUtils.getAttributeOrElementText(highlightNode, "fillOpacity");
            if (fillOpacity != null && !fillOpacity.isEmpty()) {
                question.setFillOpacity(fillOpacity);
            }
          }
          String[] titles = descriptionToTitles(rq);
          if (titles.length > 0) {
            question.setTitle1(titles[0]);
          }
          if (titles.length > 1) {
            question.setTitle2(titles[1]);
          }

          // Display the female map unless the patient has a gender attribute with a value of "Male"
          question.setFemale(!(patient.hasAttribute("gender") && "Male".equals(patient.getAttribute("gender")
              .getDataValue())));
          question.setNoPainCheckboxLabel("I have no pain");
          // See if there's a custom label for the checkbox
          NodeList responseList = itemNode.getElementsByTagName("Response");
          if (responseList != null) {
            for (int respInx = 0; respInx < responseList.getLength(); respInx++) {
              Element respElement = (Element) responseList.item(respInx);
              if ("noPainCheckboxLabel".equals(respElement.getAttribute("Description"))) {
                NodeList labelNodes = respElement.getElementsByTagName(Constants.XFORM_LABEL);
                if (labelNodes.getLength() > 0) {
                  NodeList labelTextList = labelNodes.item(0).getChildNodes();
                  if (labelTextList.getLength() > 0) {
                    question.setNoPainCheckboxLabel(labelTextList.item(0).getNodeValue());
                  }
                }
              }
            }
          }

          NextQuestion next = new NextQuestion();
          next.setDisplayStatus(displayStatus);
          next.setQuestion(bean);
          return next;
          // } else if ("registrySurvey".equals(itemNode.getAttribute("Class"))) {
          // assert numQuestions == 1;
          // DisplayStatus displayStatus = factory.displayStatus().as();
          //
          // displayStatus.setQuestionType(QuestionType.bodyMap);
          // displayStatus.setQuestionId(itemNode.getAttribute("Order"));
          //
          // AutoBean<BodyMapQuestion> bean = factory.bodyMapQuestion();
          // BodyMapQuestion question = bean.as();
          //
          // RegistryQuestion rq = RegistryAssessmentUtils.getQuestion(itemNode, index);
          // String[] titles = descriptionToTitles(rq);
          // if (titles.length > 0) {
          // question.setTitle1(titles[0]);
          // }
          // if (titles.length > 1) {
          // question.setTitle2(titles[1]);
          // }
          // question.setFemale("Female".equals(patient.getAttribute("gender")));
          // question.setNoPainCheckboxLabel("I have no pain");
          //
          // NextQuestion next = new NextQuestion();
          // next.setDisplayStatus(displayStatus);
          // next.setQuestionJson(AutoBeanCodex.encode(bean).getPayload());
          // return next;
        } else if ("surveyQuestionCollapsible".equals(itemNode.getAttribute("Class"))) {
          assert numQuestions == 1;
          DisplayStatus displayStatus = factory.displayStatus().as();

          displayStatus.setQuestionType(QuestionType.collapsibleRadioset);
          displayStatus.setQuestionId(itemNode.getAttribute("Order"));
          displayStatus.setSurveyProviderId(Integer.toString(patStudy.getSurveySystemId()));
          displayStatus.setSurveySectionId(Integer.toString(patStudy.getStudyCode()));

          AutoBean<CollapsibleRadiosetQuestion> crbean = factory.collapsibleRadiosetQuestion();
          CollapsibleRadiosetQuestion crquestion = crbean.as();
          RegistryQuestion rq = RegistryAssessmentUtils.getQuestion(itemNode, index);
          String[] titles = descriptionToTitles(rq);
          if (titles.length > 0) {
            crquestion.setTitle1(titles[0]);
          }
          if (titles.length > 1) {
            crquestion.setTitle2(titles[1]);
          }
          crquestion.setCollapsibleHeading("More Information");
          crquestion.setCollapsibleContent(RegistryAssessmentUtils.getAttributeOrElementText(itemNode,
              Constants.COLLAPSIBLE_CONTENT));
          ArrayList<String> choices = new ArrayList<>();
          for (SurveyAnswerIntf cqanswer : rq.getAnswers()) {
              assert cqanswer.getType() == Constants.TYPE_RADIO;
              if (cqanswer.getText() != null && cqanswer.getText().size() == 1) {
                choices.add(cqanswer.getText().get(0));
              }
          }
          crquestion.setChoices(choices);

          NextQuestion next = new NextQuestion();
          next.setDisplayStatus(displayStatus);
          next.setQuestion(crbean);
          return next;
        }

        // Page="start" on the item means this item should start a new page
        String page = itemNode.getAttribute("Page");
        if ((page != null) && page.equalsIgnoreCase("start")) {
          // If we currently have some questions then handle that page first
          if (questions.size() > 0) {
            break;
          }
        }

        // Section="start" on the item means this item is the first item of a section
        // Section="end" on the item means this item is the last item in a section
        // All items in a section are placed on the same page
        String section = itemNode.getAttribute("Section");
        if ((section != null) && section.equalsIgnoreCase("start")) {
          pageSection = true;
        } else if ((section != null) && section.equalsIgnoreCase("end")) {
          pageSection = false;
        }

        // Visible="false" is used on hidden questions which are enabled by an <onselect>
        // element. A hidden question must be preceded by a visible parent question on
        // the page which contains the <onselect> element which enables the hidden question.
        // A hidden question from a prior page may show up here as unanswered because the
        // hidden question did not get enabled when the parent question was asked.
        // We want to skip any prior unasked hidden questions until we find the next
        // unasked visible question.
        String visibleAttr = itemNode.getAttribute("Visible");
        boolean visible = (visibleAttr == null) || !visibleAttr.equalsIgnoreCase("false");

        // Add the question to the page
        if (visible || (questions.size() > 0)) {
          questions.add(RegistryAssessmentUtils.getQuestion(itemNode, index));
        }

        // Add the question to the list of questions we already looked at
        selections.add(miniQ.getOrder());
      }
    } while (miniQ != null &&
        ((questions.size() < numQuestions) || pageSection) );

    if (questions.size() > 0) {
      return toNextQuestion(patStudy, questions);
    } else {
      return null;
    }
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
            if (input.getAttribute("step") != null) {
              attributes.put("step", input.getAttribute("step"));
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
        } else if (answerIntf.getType() == Constants.TYPE_DATEPICKER) {
          RegistryAnswer answer = (RegistryAnswer) answerIntf;
          field.setType(FieldType.datePicker);
          if (answer.getAttribute("inlineBlind") != null) {
            attributes.put("inlineBlind", answer.getAttribute("inlineBlind"));
          }
          if (answer.getAttribute("useFocus") != null) {
            attributes.put("useFocus", answer.getAttribute("useFocus"));
          }
          if (answer.getAttribute("lockInput") != null) {
            attributes.put("lockInput", answer.getAttribute("lockInput"));
          }
          field.setAttributes(attributes);
          String fieldId = questionIntf.getNumber() + ":" + answer.getAttribute("Order");
          if (answer.getReference() != null && answer.getReference().length() > 0) {
            fieldId += ":" + answer.getReference();
          }
          field.setFieldId(fieldId);
          field.setLabel(answer.getLabel());
          field.setRequired(Boolean.parseBoolean(answer.getAttribute("required")));
        } else if (answerIntf.getType() == Constants.TYPE_SLIDER) {
          RegistryAnswer answer = (RegistryAnswer) answerIntf;
          field.setType(FieldType.numericSlider);
          if (answer.getAttribute("lowerBound") != null) {
            attributes.put("lowerBound", answer.getAttribute("lowerBound"));
          }
          if (answer.getAttribute("upperBound") != null) {
            attributes.put("upperBound", answer.getAttribute("upperBound"));
          }
          field.setAttributes(attributes);
          String fieldId = questionIntf.getNumber() + ":" + answer.getAttribute("Order");
          if (answer.getReference() != null && answer.getReference().length() > 0) {
            fieldId += ":" + answer.getReference();
          }
          field.setFieldId(fieldId);
          field.setLabel(answer.getLabel());
          field.setRequired(Boolean.parseBoolean(answer.getAttribute("required")));
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
                  throw new RuntimeException("Can't have two fields trying to show/hide the same thing: "+contingency.getValue());
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
                  throw new RuntimeException("Can't have two fields trying to show/hide the same thing! "+contingency.getValue());
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
        } else if (answerIntf.getType() == Constants.TYPE_DROPDOWN) {
          field.setType(FieldType.dropdown);
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
                  throw new RuntimeException("Can't have two fields trying to show/hide the same thing! "+contingency.getValue());
                } else {
                  orderToParent.put(contingency.getValue(), value);
                }
              }
            }
          }
          if (answerIntf.getAttribute("Filter") != null) {
            attributes.put("Filter", answerIntf.getAttribute("Filter"));
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
          if (answerIntf.getAttributeKeys() != null) {
            for (String attrKey : answerIntf.getAttributeKeys()) {
              attributes.put(attrKey, answerIntf.getAttribute(attrKey));
            }
            field.setAttributes(attributes);
          }
          for (SelectItem item : input.getItems()) {
            FormFieldValue value = factory.value().as();
            value.setId(item.getValue());
            value.setLabel(item.getLabel());
            field.getValues().add(value);

            // Use the show/hide event logic to figure out which other fields should be children of this one
            if (item.getContingency(Constants.ACTION_ONSELECT) != null) {
              for (Contingency contingency : item.getContingency(Constants.ACTION_ONSELECT)) {
                if (orderToParent.containsKey(contingency.getValue())) {
                  throw new RuntimeException("Can't have two fields trying to show/hide the same thing / "+contingency.getValue());
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
        } else if (answerIntf.getType() == Constants.TYPE_RADIOSETGRID) {
          logger.debug("Next question is type RadiosetGrid");
          field.setType(FieldType.radioSetGrid);
          SelectElement input = (SelectElement) answerIntf;

          field.setValues(new ArrayList<FormFieldValue>());
          if (answerIntf.getAttributeKeys() != null) {
            for (String attrKey : answerIntf.getAttributeKeys()) {

              attributes.put(attrKey, answerIntf.getAttribute(attrKey));
            }
            field.setAttributes(attributes);
          }
          ArrayList<FormFieldValue> formFieldValues = new ArrayList<FormFieldValue>();
          field.setValues(formFieldValues);

          for (SelectItem item : input.getItems()) {
            FormFieldValue value = factory.value().as();
            value.setLabel(item.getLabel());
            value.setId(item.getGroup() + ":" + item.getValue());
            field.getValues().add(value);
            logger.debug("   with formFieldValue: { id:" + value.getId() + ",label:" + item.getLabel() + "}");

            // Use the show/hide event logic to figure out which other fields should be children of this one
            if (item.getContingency(Constants.ACTION_ONSELECT) != null) {
              for (Contingency contingency : item.getContingency(Constants.ACTION_ONSELECT)) {
                if (orderToParent.containsKey(contingency.getValue())) {
                  throw new RuntimeException("Can't have two fields trying to show/hide the same thing / "+contingency.getValue());
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
          throw new RuntimeException("Unknown answer type: " + answerIntf.getType() + answerIntf.getText());
        }
        if (answerIntf.getAttribute(Constants.ALIGN) != null && answerIntf.getAttribute(Constants.ALIGN).length() > 0) {
          attributes.put(Constants.ALIGN, answerIntf.getAttribute(Constants.ALIGN).toLowerCase());
        }
        if (answerIntf.getAttribute("StyleName") != null && answerIntf.getAttribute("StyleName").length() > 0) {
          attributes.put("StyleName", answerIntf.getAttribute("StyleName"));
        }
        if (answerIntf.getAttribute("leftLabel") != null && answerIntf.getAttribute("leftLabel").length() > 0) {
          attributes.put("leftLabel", answerIntf.getAttribute("leftLabel"));
        }
        if (answerIntf.getAttribute("rightLabel") != null && answerIntf.getAttribute("rightLabel").length() > 0) {
          attributes.put("rightLabel", answerIntf.getAttribute("rightLabel"));
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
      // if (questionIntf.getAttribute("Alert") != null) {
      // question.setServerValidationMessage(questionIntf.getAttribute("Alert"));
      // displayStatus.setValidationFailed(true);
      // }
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

  private String[] descriptionToTitles(RegistryQuestion rq) {
    if (rq.getText() == null || rq.getText().size() == 0) {
      return new String[0];
    }
    return rq.getText().toArray(new String[0]);
  }

  private int getLastQuestion(Patient patient, NodeList itemList, String xmlDocument, int question) {
    // logger.debug("getLastQuestion starting at " + question);
    int lastQuestion = question;
    // only want to count the items this person qualifies for
    for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
      Element itemNode = (Element) itemList.item(itemInx);
      boolean meets = RegistryAssessmentUtils.qualifies(patient, itemNode, xmlDocument);
      try {
        int questionNumber = RegistryAssessmentUtils.getIntegerAttribute(itemNode, Constants.ORDER, itemInx);
        if (meets && questionNumber > lastQuestion) {
          if (itemNode.hasAttribute(Constants.VISIBLE)) {
            if (itemNode.getAttribute(Constants.VISIBLE) != null
                && itemNode.getAttribute(Constants.VISIBLE).toLowerCase().equals("false")) {
              meets = false; // we don't look at invisible ones
            }
          }
        }
        if (meets) {

          if (questionNumber > lastQuestion) {
            lastQuestion = questionNumber;
          }
        }
      } catch (NumberFormatException nfe) {
        logger.warn("getLastQuestion caught " + nfe.getMessage()
            + " getting the Order attribute from question at index " + itemInx);
      }
    }
    return lastQuestion;
  }

  private static Patient doAttributeActions(Patient patient, Element answerNode, Database database, PatientDao patAttribDao ) {

    ArrayList<PatientAttribute> attributes = new ArrayList<>();
    NodeList attributeList = answerNode.getElementsByTagName(Constants.SET + Constants.PATIENT_ATTRIBUTE);
    if (attributeList != null && attributeList.getLength() > 0) {
      for (int attributeInx = 0; attributeInx < attributeList.getLength(); attributeInx++) {
        Element attributeEl = (Element) attributeList.item(attributeInx);
        String dataName = attributeEl.getAttribute("data_name");
        String dataValue = attributeEl.getAttribute("data_value");
        attributes.add(new PatientAttribute(patient.getPatientId(), dataName, dataValue,
            PatientAttribute.STRING));
      }
    }

    /* handle each attribute listed */
    //= new PatientDao(database, ServerUtils.getInstance().getAdminUser(database));
    for (PatientAttribute patientAttribute : attributes) {
      String value = patientAttribute.getDataValue();
      if (value == null || "null".equals(value) || value.isEmpty()) {
        /**
         * remove the attribute
         */
        if (patient.hasAttribute(patientAttribute.getDataName())) {
          patAttribDao.deleteAttribute(patientAttribute);
          patient.removeAttribute(patientAttribute.getDataName());
        }
      } else {
        /**
         * Add or update an existing one
         */
        patAttribDao.insertAttribute(patientAttribute);
        patient.addAttribute(patientAttribute);
      }
    }
    return patient;
  }

  /**
   * Override this method to perform actions when an assessment
   * is completed.
   */
  protected void assessmentCompleted(Database database, Patient patient, PatientStudy patStudy) {
  }

  protected Study getStudy(Database database, String name) {
    LocalSystem localSystem = LocalSystem.getInstance(database);
    SurveySystDao ssDao = new SurveySystDao(database);
    Study study = ssDao.getStudy(localSystem.getSurveySystemId(), name);
    return study;
  }

  @Override
  public String getAssessments(Database database, int version) throws Exception {
    LocalSystem localSystem = LocalSystem.getInstance(database);
    SurveySystDao ssDao = new SurveySystDao(database);
    ArrayList<Study> allStudies = ssDao.getStudies();
    if (localSystem == null) {
      logger.debug("localSystem == null");
    }
    if (allStudies == null) {
      logger.debug("allStudies == null");

    }
    if (allStudies.size() < 1) {
      logger.debug("allStudies.size < 1");
    }
    if (localSystem == null || allStudies == null || allStudies.size() < 1) {
      return "<forms></forms>";
    }
    StringBuilder myStudies = new StringBuilder();
    myStudies.append("<forms>");
    for (int indx = 0; indx < allStudies.size(); indx++) {
      Study study = allStudies.get(indx);
      if (study != null && study.getSurveySystemId().intValue() == localSystem.getSurveySystemId().intValue()) {
        myStudies.append("<form OID=\"");
        myStudies.append(study.getStudyCode());
        myStudies.append("\" name=\"");
        myStudies.append(study.getStudyDescription());
        myStudies.append("\" />");
      } else {
        if (study == null) {
          logger.debug("study " + indx + " is null");
        } else {
          logger
              .debug("studySystem " + study.getSurveySystemId() + "!= localSystem " + localSystem.getSurveySystemId());
        }
      }
    }
    myStudies.append("</forms>");
    return myStudies.toString();
  }

  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    return new RegistryShortFormScoreProvider(dbp.get(), siteInfo);
  }

  @Override
  public void setVersion(int vs) {
    version = vs;
  }

  public int getVersion() {
    return version;
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(Database database, PatientStudy study, User user) {
    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
    if (study == null) {
      return questions;
    }
    String xmlDocumentString = study.getContents();
    if (xmlDocumentString == null) {
      return questions;
    }
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    try {
      db = dbf.newDocumentBuilder();

      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));

      Document messageDom;
      messageDom = db.parse(is);

      org.w3c.dom.Element docElement = messageDom.getDocumentElement();
      PatientDao patientDao = new PatientDao(database, siteId, user);
      Patient patient = patientDao.getPatient(study.getPatientId());

      if (docElement.getTagName().equals(Constants.FORM)) {
        Element itemsNode = (Element) messageDom.getElementsByTagName(Constants.ITEMS).item(0);
        NodeList itemList = itemsNode.getElementsByTagName(Constants.ITEM);

        /* only include the questions the patient qualifies for */
        for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
          Element itemNode = (Element) itemList.item(itemInx);
          if (RegistryAssessmentUtils.qualifies(patient, itemNode, xmlDocumentString)) {
            RegistryQuestion question = RegistryAssessmentUtils.getQuestion(itemNode, itemInx);
            String itemResponse = itemNode.getAttribute(Constants.ITEM_RESPONSE);
            if (itemResponse != null && itemResponse.trim().length() > 0) {
              question.setAnswered(true);
              // mark the answers as selected
              StringTokenizer stoken = new StringTokenizer(itemResponse, ",");
              while (stoken.hasMoreTokens()) {
                String ans = stoken.nextToken();
                if (ans.indexOf(":") > 0) { // todo handle values

                } else { // simple selected or not response
                  int ansNum = Integer.parseInt(ans);
                  question.getAnswer(ansNum).setSelected(true);
                }
              }
            }
          }
        }
      }
    } catch (SAXException | ParserConfigurationException | IOException e) {
      logger.error("Exception in getSurvey token " + study.getToken() + " study " + study.getStudyCode(), e);
    }
    return questions;
  }

} // end of RegistryAssessmentsService class


class LocalSystem extends SurveySystem {
  private static final long serialVersionUID = -4382364022282098050L;
  private static LocalSystem me = null;

  private LocalSystem(Database database) throws DataException {
    SurveySystem ssys = new SurveySystDao(database).getSurveySystem(Constants.REGISTRY_SURVEY_SYSTEM_NAME);

    if (ssys == null) {
      throw new DataException("The SURVEY_SYSTEM table does not have an entry for the SURVEY_SYSTEM_NAME of "+
                              Constants.REGISTRY_SURVEY_SYSTEM_NAME);
    }

    this.copyFrom(ssys);
    me = this;
  }

  public static LocalSystem getInstance(Database database) throws DataException {
    if (me == null) {
      me = new LocalSystem(database);
    }
    return me;
  }

} // end of LocalSystem

class QuestionMini {
  int order = -1;
  int index = -1;

  public QuestionMini(int order, int index) {
    this.order = order;
    this.index = index;
  }

  public int getIndex() {
    return index;
  }

  public int getOrder() {
    return order;
  }
}
