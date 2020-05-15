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

import edu.stanford.registry.client.api.SurveyBuilderFactory;
import edu.stanford.registry.client.api.SurveyBuilderForm;
import edu.stanford.registry.client.api.SurveyBuilderFormCondition;
import edu.stanford.registry.client.api.SurveyBuilderFormCondition.Method;
import edu.stanford.registry.client.api.SurveyBuilderFormCondition.Type;
import edu.stanford.registry.client.api.SurveyBuilderFormFieldValue;
import edu.stanford.registry.client.api.SurveyBuilderFormQuestion;
import edu.stanford.registry.client.api.SurveyBuilderFormResponse;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.survey.client.api.FieldType;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class SurveyUtils {
  private final static Logger logger = Logger.getLogger(SurveyUtils.class);
  private final static String emptyForm = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form Description=\"Survey builder\" DateStarted=\"\" DateFinished=\"\" questionsPerPage=\"1\"><Items></Items></Form>";
  private final static SurveyBuilderFactory surveyBuilderFactory = AutoBeanFactorySource.create(SurveyBuilderFactory.class);

  public static String convertToXmlString(String jsonString) {
    String xmlString = "";
    final SurveyBuilderForm form = AutoBeanCodex.decode(surveyBuilderFactory, SurveyBuilderForm.class, jsonString).as();
    try {
      if (form == null || form.getQuestions() == null) { // a just-started survey-builder has a null form
        return "";
      }
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(emptyForm));
      xmlString = SurveyUtils.createXmlDocumentStringFromForm(form.getQuestions());
    } catch (ParserConfigurationException | IOException | SAXException e ) {
      logger.error(e);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return xmlString;
  }

  private static String createXmlDocumentStringFromForm(List<SurveyBuilderFormQuestion> questions) throws ParserConfigurationException, IOException, SAXException, TransformerException {
     /* Create the xml from the response */
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(emptyForm));
    Document doc = db.parse(is);
    NodeList itemsList = doc.getElementsByTagName(Constants.ITEMS);
    for (SurveyBuilderFormQuestion formQuestion : questions) {
      Element itemElement = getItem(doc, formQuestion.getOrder(), formQuestion.getConditionsType());
      if (formQuestion.getTitle1() != null && !formQuestion.getTitle1().isEmpty()) {
        itemElement.appendChild(getDescription(doc, formQuestion.getTitle1()));
      }
      if (formQuestion.getTitle2() != null && !formQuestion.getTitle2().isEmpty()) {
        itemElement.appendChild(getDescription(doc, formQuestion.getTitle2()));
      }
      int index = 0;
      if (formQuestion.getConditions() != null) {
        for (SurveyBuilderFormCondition condition : formQuestion.getConditions()) {
          if (condition != null && condition.getType() != null && condition.getAttributes() != null
              && condition.getMethod() != null) {
            if (condition.getType().value().equals(Type.patientAttribute.value())) {
              if (condition.getAttributes().get(Type.patientAttribute.value()) != null) {
                addPatientAttribute(doc, itemElement, condition.getAttributes().get(Type.patientAttribute.value()),
                    condition.getDataValue(), condition.getMethod().value());
              }
            } else if (condition.getType() == Type.response) {
              if (condition.getValue() != null) {
                // find the response for the ref value
                FieldType checkFieldType = null;
                for (SurveyBuilderFormQuestion checkQuestion : questions) {
                  for (SurveyBuilderFormResponse checkResponse : checkQuestion.getResponses()) {
                    if (checkResponse.getRef() != null
                        && checkResponse.getRef().equals(condition.getValue().getRef())) {
                      checkFieldType = checkResponse.getFieldType();
                    }
                  }
                }
                if (checkFieldType == FieldType.numericScale) {
                  addResponseScore(doc, itemElement, condition.getValue().getRef(),
                      condition.getDataValue(), condition.getMethod().value());
                } else if ((checkFieldType == FieldType.radios) ||
                    (checkFieldType == FieldType.dropdown)) {
                  addResponseSelectedValue(doc, itemElement, condition.getValue().getRef(),
                      condition.getDataValue(), condition.getMethod().value());
                } else {
                  addResponseValue(doc, itemElement, condition.getValue().getRef(),
                      condition.getDataValue(), condition.getMethod().value());
                }
              }
            } else if (condition.getType() == Type.item) {
              if (condition.getValue() != null) {
                addItemValue(doc, itemElement, condition.getValue(), condition.getMethod() );
              }
            }
          }
        }
      }
      if (formQuestion.getResponses() != null && formQuestion.getResponses().size() > 0) {
        for (SurveyBuilderFormResponse response : formQuestion.getResponses()) {
          addFieldToItemElement(doc, itemElement, response, index);
          index++;
        }
      } else {
        Element responsesElement = doc.createElement(Constants.RESPONSES);
        itemElement.appendChild(responsesElement);
        Element responseElement = getResponse(doc, "1", "select1", "Full");
        responseElement.setAttribute("required", "false");
        responsesElement.appendChild(responseElement);
      }
      itemsList.item(0).appendChild(itemElement);
    }
    Transformer trans = TransformerFactory.newInstance().newTransformer();
    trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    trans.setOutputProperty(OutputKeys.INDENT, "yes");
    trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    StringWriter strWriter = new StringWriter();
    trans.transform(new DOMSource(doc), new StreamResult(strWriter));
    logger.debug(strWriter.toString());
    return strWriter.toString();
  }

  private static void addFieldToItemElement(Document doc, Element itemElement, SurveyBuilderFormResponse formResponse, int index)
  {
    if (formResponse == null || formResponse.getFieldType() == null) {
      logger.error("addFieldToItemElement was called with a null formResponse.fieldType");
      return;
    }
    logger.debug("adding response " + index + " with Fieldtype " + formResponse.getFieldType().toString());
    Element responsesElement;
    NodeList responses = itemElement.getElementsByTagName(Constants.RESPONSES);
    if (responses != null && responses.getLength() > 0) {
      responsesElement = (Element) responses.item(0);
    } else {
      responsesElement = doc.createElement(Constants.RESPONSES);
      itemElement.appendChild(responsesElement);
    }
    Element responseElement = null;
    if (formResponse.getFieldType() == FieldType.heading) {
      responseElement = getDescription(doc, formResponse.getLabel());
    } else if (formResponse.getFieldType() == FieldType.radios) {
      responseElement = getResponse(doc, Integer.toString(index), "select1", "Full");
      for (SurveyBuilderFormFieldValue value : formResponse.getValues()) {
        Element respItemElement = getResponseItem(doc, value.getLabel(), value.getId());
        responseElement.appendChild(respItemElement);
      }
    } else if (formResponse.getFieldType().toString().equals(FieldType.checkboxes.toString())) {
      responseElement = getResponse(doc, Integer.toString(index), "select", "Full");
      for (SurveyBuilderFormFieldValue value : formResponse.getValues()) {
        Element respItemElement = getResponseItem(doc, value.getLabel(), value.getId());
        if (value.getRef() != null) {
          Element refElement = doc.createElement(Constants.XFORM_REF);
          refElement.appendChild(doc.createCDATASection(value.getRef()));
          respItemElement.appendChild(refElement);
        }
        responseElement.appendChild(respItemElement);
      }
    } else if (formResponse.getFieldType() == FieldType.dropdown) {
      responseElement = getResponse(doc, Integer.toString(index), "dropdown", "Full");
      if ((formResponse.getAttributes() != null) && formResponse.getAttributes().containsKey("Filter")) {
        responseElement.setAttribute("Filter", formResponse.getAttributes().get("Filter"));
      }
      for (SurveyBuilderFormFieldValue value : formResponse.getValues()) {
        Element respItemElement = getResponseItem(doc, value.getLabel(), value.getId());
        responseElement.appendChild(respItemElement);
      }
    } else if (formResponse.getFieldType() == FieldType.number
        || formResponse.getFieldType() == FieldType.text
        || formResponse.getFieldType() == FieldType.textArea) {
      responseElement = getResponse(doc, Integer.toString(index), "input", "");
      if (formResponse.getFieldType() == FieldType.textArea) {
        Element formatElement = doc.createElement(Constants.XFORM_FORMAT);
        addChildElement(doc, formatElement, Constants.XFORM_LINES, "2");
        responseElement.appendChild(formatElement);
      } else if (formResponse.getFieldType() == FieldType.number) {
        Element formatElement = doc.createElement(Constants.XFORM_FORMAT);
        addChildElement(doc, formatElement, "datatype", "integer");
        responseElement.appendChild(formatElement);
        if (formResponse.getAttributes() != null && formResponse.getAttributes().size() > 0) {
          if (formResponse.getAttributes().containsKey("min")) {
            addChildElement(doc, responseElement, "min", formResponse.getAttributes().get("min"));
          }
          if (formResponse.getAttributes().containsKey("max")) {
            addChildElement(doc, responseElement, "max", formResponse.getAttributes().get("max"));
          }
          if (formResponse.getAttributes().containsKey("step")) {
            addChildElement(doc, responseElement, "step", formResponse.getAttributes().get("step"));
          }
        }
      }
    } else if (formResponse.getFieldType() == FieldType.textBoxSet) {
      responseElement = getTextBoxSetResponse(doc, formResponse);
    } else if (formResponse.getFieldType() == FieldType.radioSetGrid) {
      responseElement = getRadiosetGridResponse(doc, formResponse );
    } else if (formResponse.getFieldType().toString().equals(FieldType.numericScale.toString())) {
      itemElement.setAttribute("Class", "surveyQuestionHorizontal");

      if (formResponse.getRef() != null) {
        Element refElement = doc.createElement(Constants.XFORM_REF);
        refElement.appendChild( doc.createTextNode(formResponse.getRef()) );
        itemElement.appendChild(refElement);
      }
      int orderNumber = 0;
      for (SurveyBuilderFormFieldValue value : formResponse.getValues()) {
        Element respElement = getNumericScaleResponse(doc, orderNumber, value.getId(), value.getLabel());
        Element scoresElement = doc.createElement("Scores");
        Element scoreElement = doc.createElement("Score");
        scoreElement.setAttribute(Constants.XFORM_VALUE, value.getId());
        scoresElement.appendChild(scoreElement);
        respElement.appendChild(scoresElement);
        responsesElement.appendChild(respElement);
        orderNumber++;
      }
      if (formResponse.getRequired() != null && notNullOrEmpty(formResponse.getRequired().toString()) && (Boolean.valueOf(formResponse.getRequired().toString()))) {
        itemElement.setAttribute("RequiredMax", "1");
        itemElement.setAttribute("RequiredMin", "1");
      }
    } else if (formResponse.getFieldType().toString().equals(FieldType.numericSlider.toString())) {
      responseElement = getSliderResponse(doc, formResponse);
    } else if (formResponse.getFieldType().toString().equals(FieldType.collapsibleContentField.toString())) {
      responseElement = getCollapsibleContentResponse(doc, formResponse);
    } else if (formResponse.getFieldType() == FieldType.datePicker) {
      responseElement = getDatePickerResponse(doc, formResponse);
    } else if (formResponse.getFieldType() == FieldType.textBoxSet) {
      responseElement = getTextBoxSetResponse(doc, formResponse);
    } else {
      logger.error("addFieldToItemElement can't handle type " + formResponse.getFieldType());
    }
    if (responseElement != null) {
      responseElement = addResponseAttributes(doc, formResponse, responseElement);
      responsesElement.appendChild(responseElement);
    }
  }
  private static void addChildElement(Document doc, Element parentElement, String tagName, String textValue) {
    Element childElement = doc.createElement(tagName);
    childElement.appendChild(doc.createTextNode(textValue));
    parentElement.appendChild(childElement);
  }
  private static void addChildCDataElement(Document doc, Element parentElement, String tagName, String textValue) {
    Element childElement = doc.createElement(tagName);
    childElement.appendChild(doc.createCDATASection(textValue));
    parentElement.appendChild(childElement);
  }
  private static void addPatientAttribute(Document doc, Element itemElement, String dataName, String dataValue, String method) {

    Element patAttrElement = doc.createElement(Constants.PATIENT_ATTRIBUTE);
    patAttrElement.setAttribute(Constants.ATTR_NAME, dataName);
    patAttrElement.setAttribute(Constants.ATTR_VALUE, getValue(dataValue));
    patAttrElement.setAttribute(Constants.ATTR_CONDITION, method);
    itemElement.appendChild(patAttrElement);
  }

  private static void addResponseValue(Document doc, Element itemElement, String reference, String dataValue, String method) {

    Element respValElement = doc.createElement("ResponseValue");
    respValElement.setAttribute("xpath_query", "//Item/Responses/Response[ref='" + reference + "']/value/text()");
    respValElement.setAttribute(Constants.ATTR_VALUE, getValue(dataValue));
    respValElement.setAttribute(Constants.ATTR_CONDITION, method);
    itemElement.appendChild(respValElement);
  }

  private static void addResponseScore(Document doc, Element itemElement, String reference, String dataValue, String method) {
    Element respValElement = doc.createElement("ResponseValue");
    respValElement.setAttribute("xpath_query", "//Item[ref='" + reference + "']//@ItemScore");
    respValElement.setAttribute(Constants.ATTR_VALUE, getValue(dataValue));
    respValElement.setAttribute(Constants.ATTR_CONDITION, method);
    itemElement.appendChild(respValElement);
  }

  private static void addResponseSelectedValue(Document doc, Element itemElement, String reference, String dataValue, String method) {
    Element respValElement = doc.createElement("ResponseValue");
    respValElement.setAttribute("xpath_query", "//Item/Responses/Response[ref='" + reference + "']/item[@selected='true']/value/text()");
    respValElement.setAttribute(Constants.ATTR_VALUE, getValue(dataValue));
    respValElement.setAttribute(Constants.ATTR_CONDITION, method);
    itemElement.appendChild(respValElement);
  }

  private static void addItemValue(Document doc, Element itemElement, SurveyBuilderFormFieldValue value, Method method) {
    if (value == null || value.getResponses() == null) {
      return;
    }
    Element respValElement = doc.createElement("ResponseValue");
    StringBuilder attributeString = new StringBuilder(
        "//Item/Responses/Response[ref='" + value.getRef() + "']/item[@selected='true' and (label=");
    String connector = "";

    for (SurveyBuilderFormResponse resp : value.getResponses()) {
      attributeString.append(connector).append("'").append(resp.getLabel()).append("'");
      connector = " or label=";
    }
    attributeString.append(")]/@selected");
    respValElement.setAttribute("xpath_query", attributeString.toString());
    respValElement.setAttribute(Constants.ATTR_VALUE, "true");
    if (Method.exists == method) {
      respValElement.setAttribute(Constants.ATTR_CONDITION, "equal");
    } else {
      respValElement.setAttribute(Constants.ATTR_CONDITION, "notequal");
    }
    itemElement.appendChild(respValElement);
  }

  private static Element getItem(Document doc, String order, String conditionType) {
    Element itemElement = doc.createElement(Constants.ITEM);
    itemElement.setAttribute(Constants.ORDER, order);
    itemElement.setAttribute(Constants.ITEM_RESPONSE,"");
    itemElement.setAttribute(Constants.ITEM_SCORE,"");
    if (conditionType != null) {
      itemElement.setAttribute(Constants.ITEM_CONDITIONS, conditionType);      
    }
    return itemElement;
  }

  private static Element getDescription(Document doc, String desc) {
    Element descElement = doc.createElement(Constants.DESCRIPTION);
    descElement.appendChild(doc.createCDATASection(desc));
    return descElement;
  }
  private static Element getResponse(Document doc, String itemNumber, String type, String appearance) {
    Element responseElement = doc.createElement(Constants.RESPONSE);
    responseElement.setAttribute(Constants.ORDER, itemNumber);
    responseElement.setAttribute(Constants.TYPE, type);
    responseElement.setAttribute(Constants.APPEARANCE, appearance);
    return responseElement;
  }

  private static Element getNumericScaleResponse(Document doc, int orderNumber, String desc, String desc2) {
    Element responseElement = doc.createElement(Constants.RESPONSE);
    responseElement.setAttribute(Constants.ORDER, Integer.toString(orderNumber));
    responseElement.setAttribute(Constants.TYPE, "radio");
    responseElement.setAttribute(Constants.CLASS, "surveyAnswerHorizontal");
    responseElement.setAttribute(Constants.DESCRIPTION, desc);
    responseElement.setAttribute(Constants.DESCRIPTION + "2", desc2);
    return responseElement;
  }

  private static Element getSliderResponse(Document doc, SurveyBuilderFormResponse formResponse) {
    Element responseElement = doc.createElement(Constants.RESPONSE);
    responseElement.setAttribute(Constants.ORDER, Integer.toString(formResponse.getOrder()));
    responseElement.setAttribute(Constants.TYPE, "slider");
    try {
      if (formResponse.getValues() != null && formResponse.getValues().size() > 0) {
        if (formResponse.getValues().get(0) != null && formResponse.getValues().get(0).getId() != null)
          responseElement.setAttribute("lowerBound", formResponse.getValues().get(0).getId());
        if (formResponse.getValues().get(formResponse.getValues().size() - 1).getId() != null)
          responseElement.setAttribute("upperBound", formResponse.getValues().get(
              formResponse.getValues().size() - 1).getId());
      }
    } catch (Exception ex) {
      logger.error("Error setting upper/lower bound on sliderResponse " + ex.getMessage());
    }
    return responseElement;
  }

  private static Element getCollapsibleContentResponse(Document doc, SurveyBuilderFormResponse formResponse) {
    Element responseElement = doc.createElement(Constants.RESPONSE);
    responseElement.setAttribute(Constants.ORDER, Integer.toString(formResponse.getOrder()));
    responseElement.setAttribute(Constants.TYPE, "collapsible");
    if (formResponse.getValues() != null || formResponse.getValues().size() > 0) {
        SurveyBuilderFormFieldValue field = formResponse.getValues().get(0);
        addChildElement(doc, responseElement, "CollapsibleContent", field.getLabel());

    }
    return responseElement;
  }

  private static Element getDatePickerResponse(Document doc, SurveyBuilderFormResponse formResponse) {
    Element responseElement = doc.createElement(Constants.RESPONSE);
    responseElement.setAttribute(Constants.ORDER, Integer.toString(formResponse.getOrder()));
    responseElement.setAttribute(Constants.TYPE, FieldType.datePicker.toString());
    return responseElement;
  }

  private static Element getTextBoxSetResponse(Document doc, SurveyBuilderFormResponse formResponse) {
    Element responseElement = doc.createElement(Constants.RESPONSE);
    responseElement.setAttribute(Constants.ORDER, Integer.toString(formResponse.getOrder()));
    responseElement.setAttribute(Constants.TYPE, FieldType.textBoxSet.toString());
    for (SurveyBuilderFormFieldValue value : formResponse.getValues()) {
      Element respItemElement = getResponseItem(doc, value.getLabel(), value.getId());
      responseElement.appendChild(respItemElement);
    }
    return responseElement;
  }

  private static Element getRadiosetGridResponse( Document doc, SurveyBuilderFormResponse formResponse) {
    Element responseElement = doc.createElement(Constants.RESPONSE);
    responseElement.setAttribute(Constants.ORDER, Integer.toString(formResponse.getOrder()));
    responseElement.setAttribute(Constants.TYPE, FieldType.radioSetGrid.toString());
    for (SurveyBuilderFormFieldValue value : formResponse.getValues()) {
      Element itemElement = doc.createElement(Constants.XFORM_ITEM);
      if (value.getLabel() != null) {
        Element labelElement = doc.createElement("label");
        labelElement.appendChild(doc.createCDATASection(value.getLabel()));
        itemElement.appendChild(labelElement);
      }

      if (value.getId().contains(":")) {
        String[] vals = value.getId().split(":");
        if (vals.length > 0) {
          addChildElement(doc, itemElement, "group", vals[0]);
          if (vals.length > 1) {
            if ("y-axis".equalsIgnoreCase(vals[0])) {
              addChildElement(doc, itemElement, "ref", vals[1]);
            } else {
              addChildElement(doc, itemElement, "value", vals[1]);
            }
          }
        }
      } else {
        Element valueElement = doc.createElement("value");
        valueElement.appendChild(doc.createCDATASection(value.getId()));
        itemElement.appendChild(valueElement);
        addChildElement(doc, itemElement ,"value" ,value.getId() );
      }
      responseElement.appendChild(itemElement);
    }
    if ((formResponse.getAttributes() != null) && formResponse.getAttributes().containsKey("Ranking")) {
      responseElement.setAttribute("Ranking", formResponse.getAttributes().get("Ranking"));
    }
    return responseElement;
  }

  private static Element getResponseItem(Document doc, String label, String value) {
    Element itemElement = doc.createElement(Constants.XFORM_ITEM);
    if (label != null) {
      Element labelElement = doc.createElement("label");
      labelElement.appendChild(doc.createCDATASection(label));
      itemElement.appendChild(labelElement);
    }
    Element valueElement = doc.createElement("value");
    valueElement.appendChild(doc.createCDATASection(value));

    itemElement.appendChild(valueElement);
    return itemElement;
  }
  private static Element addResponseAttributes(Document doc, SurveyBuilderFormResponse formResponse, Element respElement) {
    if (notNullOrEmpty(formResponse.getLabel())) {
      addChildCDataElement(doc, respElement, "label", formResponse.getLabel());
    }
    if (notNullOrEmpty(formResponse.getRef())) {
      addChildElement(doc, respElement, "ref", formResponse.getRef());
    }
    if (formResponse.getRequired() !=null && notNullOrEmpty(formResponse.getRequired().toString())) {
      respElement.setAttribute("required", formResponse.getRequired().toString());
    }
    return respElement;
  }

  private static boolean notNullOrEmpty(String string) {
    return string != null && !string.isEmpty();
  }
  private static String getValue(String value) {
    return (value == null ? "" : value);
  }
}
