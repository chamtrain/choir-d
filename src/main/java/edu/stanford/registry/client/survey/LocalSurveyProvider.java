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

import edu.stanford.registry.shared.InvalidDataElementException;

import java.util.ArrayList;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.impl.DOMParseException;

public class LocalSurveyProvider implements SurveyProvider {

  public static final String[] HEADINGS = { "Score" };

  Element docElement = null;
  ArrayList<ArrayList<String>> questions;
  ArrayList<ArrayList<String>> answers;
  ArrayList<ArrayList<Integer>> choices;
  ArrayList<String[]> scoreDetails;
  String score = null;
  int version = 0;

  public LocalSurveyProvider(int version, Document doc) throws DOMParseException, InvalidDataElementException {
    this.docElement = doc.getDocumentElement();
    this.version = version;
    parse();
  }

  private void parse() throws DOMParseException, InvalidDataElementException {
    if (!docElement.getTagName().equals("Form")) {
      throw new InvalidDataElementException("doc tagname: " + docElement.getTagName() + "not '<Form>");
    }
    questions = new ArrayList<>();
    answers = new ArrayList<>();
    choices = new ArrayList<>();
    scoreDetails = new ArrayList<>();
    score = "";
    Element itemsNode = (Element) docElement.getElementsByTagName("Items").item(0);
    if (itemsNode != null) {
      NodeList sortedList = itemsNode.getElementsByTagName("Item");

      if (sortedList != null) {
      }
    }
  }

  @Override
  public String getScoreHeading() {

    return HEADINGS[0];
  }

  @Override
  public String[] getScoreDetailsHeading() {

    return HEADINGS;
  }

  private Integer getItemScoreInteger(Element itemNode) {
    String valuStr = itemNode.getAttribute("ItemScore");
    if (valuStr == null) {
      valuStr = "0";
    } else {
      if (valuStr.equals("")) {
        valuStr = "0";
      }
    }
    return Integer.valueOf(valuStr);
  }

  @Override
  public String[] getItemScoreDetails(Element itemNode) {

    String[] response = new String[1];
    response[0] = getItemScoreInteger(itemNode).toString();
    return response;
  }

  @Override
  public String getAssessmentName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArrayList<ArrayList<String>> getQuestions() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArrayList<String> getAnswers(int inx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ArrayList<Integer> getAnswered(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getItemScoreDetails(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getFinalScore() {
    // TODO Auto-generated method stub
    return null;
  }

}
