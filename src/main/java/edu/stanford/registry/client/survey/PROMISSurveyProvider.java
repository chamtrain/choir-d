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

package edu.stanford.registry.client.survey;

import edu.stanford.registry.client.utils.PROMISItemElementComparator;
import edu.stanford.registry.shared.InvalidDataElementException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.impl.DOMParseException;

public class PROMISSurveyProvider implements SurveyProvider {

  public static final String[] HEADINGS = { "Theta", "Std Error", "Score" };

  Element docElement = null;
  ArrayList<ArrayList<String>> questions;
  ArrayList<ArrayList<String>> answers;
  ArrayList<ArrayList<Integer>> choices;
  ArrayList<String[]> scoreDetails;
  String score = null;
  int version = 0;

//	private final Logger log = Logger.getLogger(PROMISSurveyProvider.class.getName());

  public PROMISSurveyProvider(int version, Document doc) throws DOMParseException, InvalidDataElementException {
    this.docElement = doc.getDocumentElement();
    this.version = version;

    parse();
  }

  private void parse() throws DOMParseException, InvalidDataElementException {
    if (!docElement.getTagName().equals("Form")) {
      throw new InvalidDataElementException("doc tagname: " + docElement.getTagName() + "not '<form>");
    }
    questions = new ArrayList<>();
    answers = new ArrayList<>();
    choices = new ArrayList<>();
    scoreDetails = new ArrayList<>();
    score = "";

    NodeList itemList = null;
    NodeList itemsNodes = docElement.getElementsByTagName("Items");
    if (itemsNodes != null && itemsNodes.getLength() > 0) {
      Element itemsNode = (Element) docElement.getElementsByTagName("Items").item(0);
      itemList = itemsNode.getElementsByTagName("Item");
    } else {
      itemList = docElement.getElementsByTagName("Item");
    }
    //
    // Element itemsNode = (Element) docElement.getElementsByTagName("Items").item(0);
    // if (itemsNode != null) {

    if (itemList != null) {
      ArrayList<Element> sortedList = sortItems(itemList);
      for (Element itemNode : sortedList) {
        String responseNumber = itemNode.getAttribute("Response");
        ArrayList<String> questionText = new ArrayList<>();
        ArrayList<String> answerText = new ArrayList<>();
        try {
          ArrayList<Integer> itemChoices = new ArrayList<>();
          // we subtract 1 because the promis "Response" starts at 1 but our index starts at 0
          itemChoices.add(Integer.valueOf(responseNumber) - 1);
          choices.add(itemChoices);
        } catch (NumberFormatException nfe) {
        }

        Element elementsNode = (Element) itemNode.getElementsByTagName("Elements").item(0);
        if (elementsNode != null) {
          NodeList elementList = elementsNode.getElementsByTagName("Element");
          for (int elementInx = 0; elementInx < elementList.getLength(); elementInx++) { // Element
            Element el = (Element) elementList.item(elementInx);
            String elementType = el.getAttribute("ElementType");
            String elementDesc = el.getAttribute("Description");

            if (elementType != null && !elementType.equals("ResponseSet")) {
              questionText.add(elementDesc);
            } else {
              Element mappings = (Element) el.getElementsByTagName("Mappings").item(0);
              NodeList mapList = mappings.getElementsByTagName("Map");
              for (int mapInx = 0; mapInx < mapList.getLength(); mapInx++) { // Map
                Element map = (Element) mapList.item(mapInx);
                Element resources = (Element) map.getElementsByTagName("Resources").item(0);
                Element resource = (Element) resources.getElementsByTagName("Resource").item(0);
                answerText.add(resource.getAttribute("Description"));
                answers.add(answerText);
              }
            }
          }
        }

        questions.add(questionText);
//				log.log(Level.INFO, "question " + itemInx + " has " + questionText.size() + " lines and " + answers.size()
//				    + " answers");
        String[] itemDetails = getItemScoreDetails(itemNode);
        if (itemDetails != null) {
          scoreDetails.add(itemDetails);
          if (itemDetails.length > 2) {
            score = itemDetails[2];
          }
        }
      }
    }

//		log.log(Level.INFO, "There are " + questions.size() + " questions");

  }

  @Override
  public String getScoreHeading() {

    return HEADINGS[2];
  }

  @Override
  public String[] getScoreDetailsHeading() {
    return HEADINGS;
  }

  @Override
  public String[] getItemScoreDetails(Element itemNode) {
    String[] details = new String[3];
    String thetaStr = itemNode.getAttribute("Theta");
    String stdErrorStr = itemNode.getAttribute("StdError");
    if (thetaStr == null) {
      thetaStr = "0";
    }
    if (stdErrorStr == null) {
      stdErrorStr = "0";
    }
    Double theta = Double.valueOf(thetaStr);
    Double stdError = Double.valueOf(stdErrorStr);
    Double scoreDouble = 10 * theta + 50;
    BigDecimal score = new BigDecimal(scoreDouble);

    details[0] = NumberFormat.getFormat("####.####").format(theta);
    details[1] = NumberFormat.getFormat("####.####").format(stdError);
    details[2] = NumberFormat.getFormat("####.####").format(score.setScale(2, BigDecimal.ROUND_HALF_EVEN));
    return details;
  }

  @Override
  public String[] getItemScoreDetails(int index) {
    if (scoreDetails != null && scoreDetails.size() > index) {
      return scoreDetails.get(index);
    }
    return new String[0];
  }

  @Override
  public String getFinalScore() {
    return score;
  }

  @Override
  public String getAssessmentName() {
    return docElement.getAttribute("Description");
  }

  @Override
  public ArrayList<ArrayList<String>> getQuestions() {
    return questions;
  }

  @Override
  public ArrayList<String> getAnswers(int inx) {
    if (answers == null || answers.size() < inx + 1) {
      return null;
    }
    return answers.get(inx);
  }

  @Override
  public ArrayList<Integer> getAnswered(int index) {
    if (choices == null || choices.size() < index + 1) {
      return null;
    }
    return choices.get(index);
  }

  private ArrayList<Element> sortItems(NodeList itemList) {
    ArrayList<Element> sortedList = new ArrayList<>();
    for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
      Element itemNode = (Element) itemList.item(itemInx);
      sortedList.add(itemNode);
    }
    PROMISItemElementComparator<Element> promisItemElementComparater;
    switch (version) {
    case 2:
      promisItemElementComparater = new PROMISItemElementComparator<>(
          PROMISItemElementComparator.SORT_BY_POSITION, PROMISItemElementComparator.ASCENDING);
      Collections.sort(sortedList, promisItemElementComparater);
      return sortedList;
    default:
      promisItemElementComparater = new PROMISItemElementComparator<>(
          PROMISItemElementComparator.SORT_BY_STDERR, PROMISItemElementComparator.DESCENDING);
      Collections.sort(sortedList, promisItemElementComparater);
      return sortedList;
    }

  }
}
