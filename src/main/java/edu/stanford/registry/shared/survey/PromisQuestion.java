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

public class PromisQuestion implements SurveyQuestionIntf, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -6068917723246860155L;
  private int questionNumber = -1;
  private ArrayList<String> question = new ArrayList<>();
  private ArrayList<SurveyAnswerIntf> answers = new ArrayList<>();
  private int answerDisplay = Constants.DISPLAY_ANSWERS_VERTICALLY;
  private String questionId;
  private boolean isAnswered = false;
  private HashMap<String, String> attributes = new HashMap<>();

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
    return question;
  }

  @Override
  public ArrayList<SurveyAnswerIntf> getAnswers() {
    return answers;
  }

  public PromisAnswer getAnswer(int inx) {
    if (answers.size() > inx) {
      return (PromisAnswer) answers.get(inx);
    }
    return null;
  }

  public void addQuestionLine(String line) {
    question.add(line);
  }

  public void addAnswer(PromisAnswer answer) {
    answers.add(answer);
  }

  public int getAnswerDisplayType() {
    return answerDisplay;
  }

  public void setAnswerDisplayType(int displayType) {
    answerDisplay = displayType;
  }

  @Override
  public boolean getAnswered() {
    return isAnswered;
  }

  @Override
  public void setAnswered(boolean ans) {
    isAnswered = ans;

  }

  public void selectAnswer(int index) {
    setAnswered(false);
    for (int a = 0; a < answers.size(); a++) {
      PromisAnswer ans = (PromisAnswer) answers.get(a);
      if (a == index) {
        ans.setSelected(true);
        setAnswered(true);
      } else {
        ans.setSelected(false);
      }
    }
  }

  public PromisAnswer getSelectedAnswer() {
    if (getAnswered()) {
      for (SurveyAnswerIntf answer : answers) {
        PromisAnswer ans = (PromisAnswer) answer;
        if (ans.getSelected()) {
          return ans;
        }
      }
    }
    return null;
  }

  @Override
  public Set<String> getAttributeKeys() {

    return attributes.keySet();
  }

  @Override
  public String getAttribute(String key) {
    return attributes.get(key);
  }

  @Override
  public void setAttribute(String key, String value) {
    attributes.put(key, value);

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
}
