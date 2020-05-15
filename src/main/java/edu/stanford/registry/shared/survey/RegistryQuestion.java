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

package edu.stanford.registry.shared.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class RegistryQuestion implements SurveyQuestionIntf, Serializable {
  private static final long serialVersionUID = 1L;

  int questionNumber;
  String questionId;
  boolean isAnswered;
  boolean isFollowUp;
  ArrayList<String> questionText = new ArrayList<>();
  private ArrayList<SurveyAnswerIntf> answers = new ArrayList<>();
  private ArrayList<String> headings = new ArrayList<>();
  private ArrayList<String> footings = new ArrayList<>();
  private HashMap<String, String> attributes = new HashMap<>();
  private String collapsibleContent = null;
  @Override
  public int getNumber() {
    return questionNumber;
  }

  @Override
  public void setNumber(int questionNumber) {
    this.questionNumber = questionNumber;
  }

  @Override
  public String getId() {
    return questionId;
  }

  @Override
  public void setId(String questionId) {
    this.questionId = questionId;

  }

  @Override
  public ArrayList<String> getText() {
    return questionText;
  }

  public void addText(String txt) {
    questionText.add(txt);
  }

  @Override
  public ArrayList<SurveyAnswerIntf> getAnswers() {
    return answers;
  }

  @Override
  public ArrayList<SurveyAnswerIntf> getAnswers(boolean onlySelected) {
    if (!onlySelected) {
      return getAnswers();
    }
    ArrayList<String> selectedAnswers = new ArrayList<>();

    for (SurveyAnswerIntf answer : answers) {
      if (answer.getSelected()) {
        ArrayList<String> responses = answer.getResponse();
        for (String response : responses) {
          selectedAnswers.add(response);
        }
      }
    }
    return answers;
  }

  public void addAnswer(RegistryAnswer ans) {
    answers.add(ans);
  }

  public RegistryAnswer getAnswer(int inx) {
    if (answers.size() > inx) {
      return (RegistryAnswer) answers.get(inx);
    }
    return null;
  }

  public void setAnswers(ArrayList<SurveyAnswerIntf> answers) {
    this.answers = answers;
  }

  @Override
  public boolean getAnswered() {
    return isAnswered;
  }

  @Override
  public void setAnswered(boolean ans) {
    isAnswered = ans;

  }

  @Override
  public Set<String> getAttributeKeys() {

    return attributes.keySet();
  }

  public boolean hasAttribute(String key) {
    if (attributes != null && key != null && attributes.get(key.toLowerCase()) != null) {
      return true;
    }
    return false;
  }

  @Override
  public String getAttribute(String key) {
    if (key != null) {
      return attributes.get(key.toLowerCase());
    }
    return null;
  }

  @Override
  public void setAttribute(String key, String value) {
    if (key == null || value == null) {
      return;
    }
    attributes.put(key.toLowerCase(), value);

  }

  public void addHeading(String text) {
    headings.add(text);
  }

  public void addFooting(String text) {
    footings.add(text);
  }

  public ArrayList<String> getHeading() {
    return headings;
  }

  public ArrayList<String> getFooting() {
    return footings;
  }

  public boolean isFollowUp() {
    return isFollowUp;
  }

  public void setFollowUp(boolean isFollow) {
    isFollowUp = isFollow;
  }

  public String getCollapsibleContent() {
    return collapsibleContent;
  }

  public void setCollapsibleContent(String content) {
    collapsibleContent = content;
  }

  public boolean hasCollapsibleContent() {
    if (collapsibleContent != null && collapsibleContent.length() > 0) {
      return true;
    }
    return false;
  }
}
