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

package edu.stanford.registry.server.utils;

import edu.stanford.registry.client.xform.XFormSurveyIntf;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.survey.Child;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.Contingency;
import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.RegistryQuestion;
import edu.stanford.registry.shared.xform.InputElement;
import edu.stanford.registry.shared.xform.Select1Element;
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;
import edu.stanford.registry.shared.xform.StringItem;
import edu.stanford.registry.shared.xform.StringsElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RegistryAssessmentUtils {
  private static final Logger logger = Logger.getLogger(RegistryAssessmentUtils.class);

  public static RegistryQuestion getQuestion(Element itemNode, int index) {
    return getQuestion(itemNode, index, true);
  }

  public static RegistryQuestion getQuestion(Element itemNode, int index, boolean allAnswers) {
    RegistryQuestion question = new RegistryQuestion();
    question.setNumber(getIntegerAttribute(itemNode, Constants.ORDER, index));

    //question.addText(RegistryAssessmentUtils.getAttributeOrElementText(itemNode, Constants.DESCRIPTION));
    ArrayList<String> questionText = getAttributeOrElementList(itemNode, Constants.DESCRIPTION);
    for (String text: questionText) {
      String[] segments = text.split("<BR>");
      for (int s=0; s<segments.length;  s++) {
        question.addText(segments[s]);
      }
    }
    question.setCollapsibleContent(RegistryAssessmentUtils.getAttributeOrElementText(itemNode, Constants.COLLAPSIBLE_CONTENT));
    if (itemNode.hasAttribute(Constants.HEADING)) {
      question.addHeading(itemNode.getAttribute(Constants.HEADING));
    } else {
      NodeList nodes = itemNode.getElementsByTagName(Constants.HEADING);
      if (nodes != null && nodes.getLength() > 0) {
        for (int n = 0; n < nodes.getLength(); n++) {
          NodeList children = nodes.item(n).getChildNodes();
          if (children != null && children.getLength() > 0) {
            for (int c = 0; c < children.getLength(); c++) {
              question.addHeading(children.item(c).getNodeValue());
            }
          }
        }
      }
    }
    if (itemNode.hasAttribute(Constants.FOOTING)) {
      question.addFooting(itemNode.getAttribute(Constants.FOOTING));
    } else {
      NodeList nodes = itemNode.getElementsByTagName(Constants.FOOTING);
      if (nodes != null && nodes.getLength() > 0) {
        for (int n = 0; n < nodes.getLength(); n++) {
          NodeList children = nodes.item(n).getChildNodes();
          if (children != null && children.getLength() > 0) {
            for (int c = 0; c < children.getLength(); c++) {
              question.addFooting(children.item(c).getNodeValue());
            }
          }
        }
      }
    }

    // Add the items attributes
    NamedNodeMap attributes = itemNode.getAttributes();
    for (int n = 0; n < attributes.getLength(); n++) {
      Node item = attributes.item(n);
      question.setAttribute(item.getNodeName(), item.getNodeValue());
    }
    ArrayList<Integer> selectedResponses = new ArrayList<>();

    if (question.hasAttribute(Constants.ITEM_RESPONSE)) {
      StringTokenizer stok = new StringTokenizer(question.getAttribute(Constants.ITEM_RESPONSE), ",");
      while (stok.hasMoreTokens()) {
        String respSelected = stok.nextToken().trim();
        try {
          selectedResponses.add(Integer.valueOf(respSelected));

        } catch (NumberFormatException nfe) {
          logger.error("getQuestion: Error reading " + Constants.ITEM_RESPONSE + " value  of " + respSelected
              + " is not a valid integer ");
        }
      }
    }
    if (selectedResponses.size() > 0) {
      question.setAnswered(true);
    }
    // Add alert
    question
        .setAttribute(Constants.ALERT, RegistryAssessmentUtils.getAttributeOrElementText(itemNode, Constants.ALERT));
    // Add reference
    question.setAttribute(Constants.XFORM_REF, RegistryAssessmentUtils.getAttributeOrElementText(itemNode, Constants.XFORM_REF));
    String align = Constants.VERTICAL;
    if (itemNode.hasAttribute(Constants.ALIGN)) {
      align = itemNode.getAttribute(Constants.ALIGN);
    }
    if (align.equals(Constants.IMAGE)) {
      NodeList imgList = itemNode.getElementsByTagName(Constants.XFORM_IMG);
      for (int imgInx = 0; imgInx < imgList.getLength(); imgInx++) {
        Element imageElement = (Element) imgList.item(imgInx);
        RegistryAnswer answer = new RegistryAnswer();
        answer.setType(Constants.TYPE_IMAGE);
        NamedNodeMap imgAttributes = imageElement.getAttributes();
        for (int attInx = 0; attInx < imgAttributes.getLength(); attInx++) {
          Node imgAttributeNode = imgAttributes.item(attInx);
          // logger.debug("setting img attribute(" +
          // imgAttributeNode.getNodeName() + ","
          // + imgAttributeNode.getNodeValue() + ")");
          answer.setAttribute(imgAttributeNode.getNodeName(), imgAttributeNode.getNodeValue());
        }
        question.addAnswer(answer);
      }
      NodeList mapList = itemNode.getElementsByTagName(Constants.XFORM_MAP);
      for (int mapInx = 0; mapInx < imgList.getLength(); mapInx++) {
        Element mapElement = (Element) mapList.item(mapInx);
        RegistryAnswer answer = new RegistryAnswer();
        answer.setType(Constants.TYPE_MAP);
        NamedNodeMap mapAttributes = mapElement.getAttributes();
        for (int attInx = 0; attInx < mapAttributes.getLength(); attInx++) {
          Node mapAttributeNode = mapAttributes.item(attInx);
          answer.setAttribute(mapAttributeNode.getNodeName(), mapAttributeNode.getNodeValue());
        }
        NodeList areas = mapElement.getElementsByTagName(Constants.XFORM_AREA);
        for (int areaInx = 0; areaInx < areas.getLength(); areaInx++) { // Areas
          Element areaElement = (Element) areas.item(areaInx);
          HashMap<String, String> areaAttributes = new HashMap<>();
          NamedNodeMap areaAttributeNodeMap = areaElement.getAttributes();
          for (int areaAttInx = 0; areaAttInx < areaAttributeNodeMap.getLength(); areaAttInx++) {
            areaAttributes.put(areaAttributeNodeMap.item(areaAttInx).getNodeName(),
                areaAttributeNodeMap.item(areaAttInx).getNodeValue());
          }
          Child area = new Child(Constants.XFORM_AREA, areaAttributes);
          answer.addChild(area);
        }
        logger.debug("added " + areas.getLength() + "area children to answer " + mapInx);
        question.addAnswer(answer);
      }
    }

    Element responsesElement = (Element) itemNode.getElementsByTagName(Constants.RESPONSES).item(0);
    NodeList responseList = responsesElement.getElementsByTagName(Constants.RESPONSE);

    // for each response
    for (int respInx = 0; respInx < responseList.getLength(); respInx++) {

      Element response = (Element) responseList.item(respInx);
      String type = response.getAttribute(Constants.TYPE);
      // String appear = response.getAttribute(Constants.APPEARANCE);
      if (type == null) {
        logger.debug("no response type for response Order + " + response.getAttribute("Order") + " defaulting to radio");
        type = "radio"; // default type is radio
      }
      RegistryAnswer answer;
      int answerType = RegistryAssessmentUtils.getAnswerType(type);

      switch (answerType) {
      case edu.stanford.registry.shared.survey.Constants.TYPE_MAP:
        answer = new RegistryAnswer();
        answer.setType(edu.stanford.registry.shared.survey.Constants.TYPE_MAP_RESPONSE);
        break;
      case edu.stanford.registry.shared.survey.Constants.TYPE_LABEL_LIST:
        StringsElement headings = new StringsElement();
        setXformValues(response, headings);
        if (response.hasAttribute(Constants.ALIGN)) {
          headings.setAlign(response.getAttribute(edu.stanford.registry.shared.survey.Constants.ALIGN));
        }
        NodeList itemNodes = response.getElementsByTagName(Constants.XFORM_ITEM);
        for (int lInx = 0; lInx < itemNodes.getLength(); lInx++) {
          StringItem stringItem = new StringItem();
          if (itemNodes.item(lInx).getNodeType() == Node.ELEMENT_NODE) {
            Element itemElement = (Element) itemNodes.item(lInx);
            NodeList labelNodes = itemElement.getElementsByTagName(Constants.XFORM_LABEL);
            if (labelNodes.getLength() > 0) {
              NodeList labelTextList = labelNodes.item(0).getChildNodes();
              if (labelTextList.getLength() > 0) {
                stringItem.setValue(labelTextList.item(0).getNodeValue());
              }
            }
          }
          headings.addItem(stringItem);
        }
        answer = headings;
        answer.setType(edu.stanford.registry.shared.survey.Constants.TYPE_LABEL_LIST);
        break;
      case edu.stanford.registry.shared.survey.Constants.TYPE_SELECT1:
        Select1Element select1 = new Select1Element();
        setSelectValues(response, select1);
        answer = select1;
        answer.setType(edu.stanford.registry.shared.survey.Constants.TYPE_SELECT1);

        if (selectedResponses.contains(Integer.valueOf(respInx))) {
          answer.setSelected(true);
        }
        break;
      case edu.stanford.registry.shared.survey.Constants.TYPE_SELECT:
        SelectElement select = new SelectElement();
        setSelectValues(response, select);
        answer = select;
        answer.setType(edu.stanford.registry.shared.survey.Constants.TYPE_SELECT);
        if (selectedResponses.contains(Integer.valueOf(respInx))) {
          answer.setSelected(true);
        }
        break;
      case edu.stanford.registry.shared.survey.Constants.TYPE_INPUT:
        InputElement textElement = new InputElement();
        setXformValues(response, textElement);
        textElement.setReference(RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_REF));
        textElement.setLabel(RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_LABEL));
        String pos = RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_LOCATION);
        if (pos.length() > 0) {
          textElement.setLabelPosition(pos);
        }
        String val = getAttributeOrElementText(response, Constants.XFORM_VALUE);
        if (val != null && val.trim().length() > 0) {
          textElement.setValue(val);
          textElement.setSelected(true);
        }

        answer = textElement;
        break;
      case Constants.TYPE_TEXTBOXSET:
        SelectElement selectbox = new SelectElement();
        selectbox.setType(Constants.TYPE_TEXTBOXSET);
        setSelectValues(response, selectbox);
        answer = selectbox;
        answer.setType(Constants.TYPE_TEXTBOXSET);
        if (selectbox.getSelectedItems() != null && selectbox.getSelectedItems().size() > 0)
          answer.setSelected(true);
        break;
      case Constants.TYPE_COLLAPSIBLE:
        RegistryAnswer collapse = new RegistryAnswer();
        collapse.setLabel(RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_LABEL));
        answer = collapse;
        answer.setType(Constants.TYPE_COLLAPSIBLE);
        break;
      case Constants.TYPE_DATEPICKER:
        InputElement dateElement = new InputElement();
        setXformValues(response, dateElement);
        dateElement.setReference(RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_REF));
        dateElement.setLabel(RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_LABEL));
        String dt = getAttributeOrElementText(response, Constants.XFORM_VALUE);
        if (dt != null && dt.trim().length() > 0) {
          dateElement.setValue(dt);
          dateElement.setSelected(true);
        }
        answer = dateElement;
        answer.setType(Constants.TYPE_DATEPICKER);
        break;
      case Constants.TYPE_SLIDER:
        InputElement sliderElement = new InputElement();
        setXformValues(response, sliderElement);
        sliderElement.setReference(RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_REF));
        sliderElement.setLabel(RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_LABEL));
        sliderElement.setAttribute("lowerBound", RegistryAssessmentUtils.getAttributeOrElementText(response, "lowerBound"));
        sliderElement.setAttribute("upperBound", RegistryAssessmentUtils.getAttributeOrElementText(response, "upperBound"));
        String sliderValue = getAttributeOrElementText(response, Constants.XFORM_VALUE);
        if (sliderValue != null && sliderValue.trim().length() > 0) {
          sliderElement.setValue(sliderValue);
          sliderElement.setSelected(true);
        }
        answer = sliderElement;
        answer.setType(Constants.TYPE_SLIDER);
        break;
      case Constants.TYPE_DROPDOWN:
        Select1Element dropdown = new Select1Element();
        setSelectValues(response, dropdown);
        answer = dropdown;
        answer.setType(edu.stanford.registry.shared.survey.Constants.TYPE_DROPDOWN);

        if (selectedResponses.contains(Integer.valueOf(respInx))) {
          answer.setSelected(true);
        }
        break;
      case Constants.TYPE_RADIOSETGRID:
        SelectElement selectAnswer = new SelectElement();
        if (response.hasChildNodes()) {
          HashMap<String, String> xAxisMap = new HashMap<>();
          NodeList selectItemList = response.getElementsByTagName(Constants.XFORM_ITEM);
          selectAnswer.setType(edu.stanford.registry.shared.survey.Constants.TYPE_RADIOSETGRID);

          // build hash of the x-axis ( value : label ) items for looking up the labels for answered xml
          for (int iInx = 0; iInx < selectItemList.getLength(); iInx++) {
            Element itemElement = (Element) selectItemList.item(iInx);
            if ("x-axis".equals(RegistryAssessmentUtils.getAttributeOrElementText(itemElement, "group"))) {
              xAxisMap.put(RegistryAssessmentUtils.getAttributeOrElementText(itemElement, Constants.XFORM_VALUE),
                  RegistryAssessmentUtils.getAttributeOrElementText(itemElement, Constants.XFORM_LABEL));
            }
          }
          for (int iInx = 0; iInx < selectItemList.getLength(); iInx++) {
            Element itemElement = (Element) selectItemList.item(iInx);
            String itemGroup = RegistryAssessmentUtils.getAttributeOrElementText(itemElement, "group");
            String itemLabel = RegistryAssessmentUtils.getAttributeOrElementText(itemElement, Constants.XFORM_LABEL);
            String itemValue = RegistryAssessmentUtils.getAttributeOrElementText(itemElement, Constants.XFORM_VALUE);
            String itemRef = RegistryAssessmentUtils.getAttributeOrElementText(itemElement, Constants.XFORM_REF);

            SelectItem selectItem = new SelectItem();
            selectItem.setLabel(itemLabel);
            selectItem.setGroup(itemGroup);

            if ("x-axis".equals(itemGroup)) {
              selectItem.setValue(itemValue);
            } else {
              if (itemValue == null || itemValue.isEmpty()) {
                selectItem.setValue(itemRef);
              } else if (xAxisMap.containsKey(itemValue)) {
                selectItem.setLabel(selectItem.getLabel() + "  :  " + xAxisMap.get(itemValue));
                selectItem.setValue(itemValue);
                selectItem.setSelected(true);
              }
            }
            logger.trace("Adding selectItem: group:" + selectItem.getGroup() +  ", value:"  + selectItem.getValue() +
                ", label:" + selectItem.getLabel());
            selectAnswer.addItem(selectItem);
          }

        }
        logger.trace(selectAnswer.toString());
        answer = selectAnswer;
        break;
      case edu.stanford.registry.shared.survey.Constants.TYPE_RADIO:
      default:
        logger.debug("adding response " + respInx);
        answer = new RegistryAnswer();
        answer.setType(edu.stanford.registry.shared.survey.Constants.TYPE_RADIO);
        if (selectedResponses.contains(Integer.valueOf(respInx))) {
          answer.setSelected(true);
        }
        answer.setReference(RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_REF));
      }
      String val = RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.DESCRIPTION); // response.getAttribute(Constants.DESCRIPTION);
      answer.addText(val);
      /* get children and attributes */
      RegistryAssessmentUtils.addFormatChild(answer, response);
      NamedNodeMap responseAttributes = response.getAttributes();

      for (int n = 0; n < responseAttributes.getLength(); n++) {
        Node responseAttributeNode = responseAttributes.item(n);
        answer.setAttribute(responseAttributeNode.getNodeName(), responseAttributeNode.getNodeValue());
      }
      if (response.hasChildNodes()) {
        HashMap<String, String> childAttributes = RegistryAssessmentUtils.getChildrenAttributes(response);
        for (String key : childAttributes.keySet()) {
          if (childAttributes.get(key) != null) {
            try {
              answer.setAttribute(key, childAttributes.get(key));
            } catch (Exception ex) {
              logger.debug(ex.getMessage() + " - problem occurred setting:" + key + " to value:"
                  + childAttributes.get(key));
            }
          }
        }

      }
      logger.debug("answer " + Constants.TYPE + "=" + type + " = " +  Constants.TYPE_ANSWER[answer.getType()] + ", " + Constants.ALIGN
          + "=" + answer.getAlign() + ", " + Constants.XFORM_HINT + "=" + answer.getAttribute(Constants.XFORM_HINT)
          + ", " + Constants.XFORM_LOCATION + "=" + answer.getLabelPosition() + ", " + Constants.CLASS + "="
          + answer.getStyle() + ", selected=" + answer.getSelected());
      if (allAnswers || answer.getSelected()) {
        question.addAnswer(answer);
      }
    }
    return question;
  }

  private static void setXformValues(Element response, XFormSurveyIntf xform) {
    if (response.hasAttribute(edu.stanford.registry.shared.survey.Constants.ALIGN)) {
      xform.setAlign(response.getAttribute(edu.stanford.registry.shared.survey.Constants.ALIGN));
    }
    if (response.hasAttribute(Constants.DESCRIPTION)) {
      xform.setLabel(response.getAttribute(Constants.DESCRIPTION));
    }
    if (response.hasAttribute(Constants.DESCRIPTION_POSITION)) {
      xform.setLabelPosition(response.getAttribute(Constants.DESCRIPTION_POSITION));
    }
  }

  private static void setSelectValues(Element response, SelectElement select) {
    setXformValues(response, select);
    select.setAppearance(response.getAttribute(Constants.APPEARANCE));
    select.setReference(RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_REF));
    select.setLabel(RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_LABEL));
    if (select.getLabel() == null || select.getLabel().trim().length() < 1) {
      // see if set with "Description" instead
      if (response.hasAttribute(Constants.DESCRIPTION)) {
        select.setLabel(response.getAttribute(Constants.DESCRIPTION));
      }
    }
    String pos = RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_LOCATION);
    if (pos.length() > 0) {
      select.setLabelPosition(pos);
    }
    if (response.hasAttribute("StyleName")) {
      select.setAttribute("StyleName", response.getAttribute("StyleName"));
    }

    if (response.hasChildNodes()) {
      NodeList selectItemList = response.getElementsByTagName(Constants.XFORM_ITEM);
      for (int iInx = 0; iInx < selectItemList.getLength(); iInx++) {
        SelectItem selectItem = new SelectItem();
        Element itemElement = (Element) selectItemList.item(iInx);
        if (getBooleanAttribute(itemElement, "selected") || getBooleanAttribute(itemElement, "checked")) {
          selectItem.setSelected(true);
        }
        NodeList labelNodes = itemElement.getElementsByTagName(Constants.XFORM_LABEL);
        if (labelNodes.getLength() > 0) {
          NodeList labelTextList = labelNodes.item(0).getChildNodes();
          if (labelTextList.getLength() > 0) {
            selectItem.setLabel(labelTextList.item(0).getNodeValue());
          }
        }
        NodeList valueNodes = itemElement.getElementsByTagName(Constants.XFORM_VALUE);
        if (valueNodes.getLength() > 0) {
          NodeList valueNodeList = valueNodes.item(0).getChildNodes();
          if (valueNodeList.getLength() > 0) {
            selectItem.setValue(valueNodeList.item(0).getNodeValue());
            if (Constants.TYPE_TEXTBOXSET == select.getType() && valueNodeList.getLength() > 1
                && valueNodeList.item(1).getTextContent() != null) {
              selectItem.setSelected(true);
              selectItem.setLabel(selectItem.getLabel() + " [" + valueNodeList.item(1).getTextContent() + "]");
            }
          }
        }
        if (selectItem.getSelected()) {
          if (selectItem.getLabel() == null || selectItem.getLabel().length() < 1 || "<p>".equalsIgnoreCase(selectItem.getLabel())) {
            NodeList altNodes = itemElement.getElementsByTagName("printlabel");
            if (altNodes != null && altNodes.getLength() > 0) {
              NodeList altTextList = altNodes.item(0).getChildNodes();
              if (altTextList != null && altTextList.getLength() > 0) {
                selectItem.setLabel(altTextList.item(0).getNodeValue());
              }
            }
          }
          logger.debug("select label: " + selectItem.getLabel() + " value: " + selectItem.getValue() + " selected  ");
        }

        // get conditional behavior
        String[] conditionTypes = { Constants.ACTION_ONSELECT, Constants.ACTION_ONDESELECT };
        for (String conditionType : conditionTypes) {
          NodeList onselectNodes = itemElement.getElementsByTagName(conditionType);
          if (onselectNodes.getLength() > 0) {
            for (int sInx = 0; sInx < onselectNodes.getLength(); sInx++) {
              Element selectNode = (Element) onselectNodes.item(sInx);
              Contingency contingency = RegistryAssessmentUtils.getContingency(selectNode);
              logger.debug("adding " + conditionType + " contingency to selectItem " + select.getItems().size()
                  + " type: " + contingency.getType() + "attribute:" + contingency.getAttribute() + " value: "
                  + contingency.getValue());
              selectItem.addContingency(conditionType, contingency);
              // logging only
              for (String attKey : contingency.getAttributeKeys(Contingency.TYPES[Contingency.ATTRIBUTE])) {
                String attValue = contingency.getAttribute(Contingency.TYPES[Contingency.ATTRIBUTE], attKey);
                logger.debug(" found' attribute' key,value =" + attKey + "," + attValue);
              }
            }
          }
        }
        logger.trace("selectItem:{label," +  selectItem.getLabel() + "}{value," + selectItem.getValue() + "}{group," + selectItem.getGroup()+"}");
        select.addItem(selectItem);
      }
    }
  }

  public static boolean qualifies(Patient patient, Element itemNode, String xmlDocument) {
    String conditionsValue = itemNode.getAttribute(Constants.ITEM_CONDITIONS);
    boolean anyConditions = (conditionsValue != null) && conditionsValue.equals("any");
    boolean meets = true;
    NodeList attributeList = itemNode.getElementsByTagName("PatientAttribute");
    if (attributeList != null) {
      for (int attributeInx = 0; attributeInx < attributeList.getLength(); attributeInx++) {
        Element attributeEl = (Element) attributeList.item(attributeInx);
        /** only look at immediate children, ignoring those related to responses **/
        if (itemNode.equals(attributeEl.getParentNode())) {
          String dataName = attributeEl.getAttribute("data_name");
          String dataValue = attributeEl.getAttribute("data_value");
          String condition = attributeEl.getAttribute("condition");
          if (RegistryQuestionUtils.meetsCondition(patient, dataName, dataValue, condition)) {
            logger.debug(patient.getPatientId() + " is asked question with attribute data_name: " + dataName
              + " data_value: " + dataValue + " condition: " + condition);
            if (anyConditions) {
              return true;
            }
          } else {
            logger.debug(patient.getPatientId() + " is not asked question with attribute data_name: " + dataName
              + " data_value: " + dataValue + " condition: " + condition);
            meets = false;
          }
        }
      }
    }

    attributeList = itemNode.getElementsByTagName("ResponseValue");
    if (attributeList != null) {
      for (int attributeInx = 0; attributeInx < attributeList.getLength(); attributeInx++) {
        Element attributeEl = (Element) attributeList.item(attributeInx);
        /** only look at immediate children, ignoring those related to responses **/
        if (itemNode.equals(attributeEl.getParentNode())) {
          String xpathQuery = attributeEl.getAttribute("xpath_query");
          String dataValue = attributeEl.getAttribute("data_value");
          String condition = attributeEl.getAttribute("condition");
          if (RegistryQuestionUtils.hasResponse(xmlDocument, xpathQuery, dataValue, condition)) {
            logger.debug(patient.getPatientId() + " is asked question with response xpath_query: " + xpathQuery
              + " data_value: " + dataValue + " condition: " + condition);
            if (anyConditions) {
              return true;
            }
          } else {
            logger.debug(patient.getPatientId() + " is not asked question with response xpath_query: " + xpathQuery
              + " data_value: " + dataValue + " condition: " + condition);
            meets = false;
          }
        }
      }
    }

    return meets;
  }

  public static boolean getBooleanAttribute(Element node, String name) {
    if (name == null) {
      return false;
    }
    String value = RegistryAssessmentUtils.getAttributeOrElementText(node, name);
    return !(value == null || value.length() < 1) && Boolean.parseBoolean(value);
  }

  public static int getIntegerAttribute(Element node, String name, int defaultValue) {

    if (node.hasAttribute(name)) {
      try {
        return Integer.parseInt(node.getAttribute(name));
      } catch (NumberFormatException nfe) {
        return defaultValue;
      }
    }
    return defaultValue;

  }

  public static int getAnswerType(String typeString) {

    if (typeString != null) {
      String typStr = typeString.toLowerCase().trim();
      for (int typInx = 0; typInx < Constants.TYPE_ANSWER.length; typInx++) {
        if (Constants.TYPE_ANSWER[typInx].equals(typStr)) {
          return typInx;
        }
      }
    }
    return 0;
  }

  public static HashMap<String, String> getChildrenAttributes(Element elem) {
    HashMap<String, String> attributes = new HashMap<>();
    if (elem == null || elem.getNodeName() == null) {
      return attributes;
    }
    NodeList nodes = elem.getChildNodes();
    if (nodes != null && nodes.getLength() > 0) {
      for (int n = 0; n < nodes.getLength(); n++) {
        if (nodes.item(n).getNodeType() == Node.ELEMENT_NODE) {
          String value = RegistryAssessmentUtils.getAttributeOrElementText(elem, nodes.item(n).getNodeName());
          if (value != null && value.length() > 0) {
            attributes.put(nodes.item(n).getNodeName(), value);
          }
        }
      }
    }
    return attributes;
  }

  public static String getAttributeOrElementText(Element elem, String name) {
    if (elem == null || name == null) {
      return "";
    }

    if (elem.hasAttribute(name) && elem.getAttribute(name).trim().length() > 0) {
      return elem.getAttribute(name);
    }

    NodeList nodes = elem.getChildNodes();
    if (nodes != null && nodes.getLength() > 0) {
      StringBuilder buf = new StringBuilder();
      for (int n = 0; n < nodes.getLength(); n++) {
        if (nodes.item(n).getNodeName().equals(name)) {
          NodeList children = nodes.item(n).getChildNodes();
          if (children != null && children.getLength() > 0) {
            for (int c = 0; c < children.getLength(); c++) {
              buf.append(children.item(c).getNodeValue());
            }
          }
        }

      }
      return buf.toString();
    }
    return "";
  }

  public static ArrayList<String> getAttributeOrElementList(Element elem, String name) {
    ArrayList<String> list = new ArrayList<>();
    if (elem == null || name == null) {
      return list;
    }

    if (elem.hasAttribute(name) && elem.getAttribute(name).trim().length() > 0) {
      list.add(elem.getAttribute(name));
    }

    NodeList nodes = elem.getChildNodes();
    if (nodes != null && nodes.getLength() > 0) {
      for (int n = 0; n < nodes.getLength(); n++) {
        if (nodes.item(n).getNodeName().equals(name)) {
          NodeList children = nodes.item(n).getChildNodes();
          if (children != null && children.getLength() > 0) {
            for (int c = 0; c < children.getLength(); c++) {
              if (children.item(c) != null && children.item(c).getNodeValue() != null &&
                  children.item(c).getNodeValue().trim().length() > 0) {
                list.add(children.item(c).getNodeValue());
              }
            }
          }
        }
      }
    }
    return list;

  }
  public static Element getResponse(RegistryAnswer ans, Element itemNode) throws NumberFormatException {
    return getResponse(ans.getAttribute(Constants.ORDER), itemNode);
  }

  public static Element getResponse(String order, Element itemNode) throws NumberFormatException {
    // Get the answers order #
    int answerOrder = Integer.parseInt(order);
    // Get the response element for this answer
    Element responses = (Element) itemNode.getElementsByTagName(Constants.RESPONSES).item(0);
    NodeList responseList = responses.getElementsByTagName(Constants.RESPONSE);
    for (int respInx = 0; respInx < responseList.getLength(); respInx++) { // for
      // each
      // response
      Element response = (Element) responseList.item(respInx);
      int responseOrder = Integer.parseInt(response.getAttribute(Constants.ORDER));
      if (responseOrder == answerOrder) {
        return response;
      }
    }
    return null;
  }

  public static Contingency getContingency(Element contingencyNode) {
    Contingency contingency = new Contingency(contingencyNode.getAttribute(Constants.TYPE),
        contingencyNode.getAttribute(Constants.WHERE), contingencyNode.getAttribute(Constants.VALUE));
    NodeList setNodes = contingencyNode.getElementsByTagName(Constants.SET);
    for (int setInx = 0; setInx < setNodes.getLength(); setInx++) {
      // for each response
      Element setItem = (Element) setNodes.item(setInx);
      String type = getAttributeOrElementText(setItem, Constants.TYPE);
      String key = getAttributeOrElementText(setItem, Constants.NAME);
      String value = getAttributeOrElementText(setItem, Constants.VALUE);
      logger.log(Level.INFO, "adding attribute to contingency: " + type + "," + key + "," + value);
      contingency.setAttribute(type, key, value);
    }
    return contingency;
  }

  public static void addFormatChild(RegistryAnswer ans, Element response) {
    NodeList formatNodes = response.getElementsByTagName(Constants.XFORM_FORMAT);
    if (formatNodes != null && formatNodes.getLength() > 0 && formatNodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
      HashMap<String, String> formatAttributes = RegistryAssessmentUtils.getChildrenAttributes((Element) formatNodes
          .item(0));
      ans.addChild(new Child(Constants.XFORM_FORMAT, formatAttributes));
    }
  }

  public static ArrayList<String> getSelectedValues(String responseNum, Element itemNode) {
    ArrayList<String> selectedList = new ArrayList<>();
    Element response = getResponse(responseNum, itemNode);
    if (response != null) {
      if ("select".equals(response.getAttribute("Type"))
          || "select1".equals(response.getAttribute("Type"))) {
        SelectElement select = new Select1Element();
        setSelectValues(response, select);
        for (SelectItem item : select.getSelectedItems()) {
          selectedList.add(item.getValue());
        }
      }
    }
    return selectedList;
  }
}

