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

package edu.stanford.registry.shared;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class LocalScore implements ChartScore {

  String patientId;
  Integer studyCode;
  String studyDescription;
  Date dt;
  BigDecimal score;
  Map<String,BigDecimal> scores;
  boolean assisted;
  private boolean replaced;
  ArrayList<Answer> answers = new ArrayList<>();

  public LocalScore(Date dt, String patientId, Integer studyCode, String description) {
    this.dt = dt;
    this.patientId = patientId;
    this.studyCode = studyCode;
    this.studyDescription = description;
  }

  public LocalScore(Date dt, String patientId, Integer studyCode, String description,
                    BigDecimal score, Map<String,BigDecimal> scores,
                    boolean assisted, boolean wasReplaced) {
    this(dt, patientId, studyCode, description);
    this.score = score;
    this.scores = scores;
    this.assisted = assisted;
    this.replaced = wasReplaced;
  }

  @Override
  public String getPatientId() {
    return patientId;
  }

  @Override
  public Integer getStudyCode() {
    return studyCode;
  }

  @Override
  public String getStudyDescription() {
    return studyDescription;
  }

  @Override
  public Date getDate() {
    return dt;
  }

  @Override
  public String getCategoryLabel() {
    return "";
  }

  @Override
  public boolean getAssisted() {
    return assisted;
  }

  @Override
  public void setAssisted(boolean isAssisted) {
    this.assisted = isAssisted;
  }

  @Override
  public boolean wasReplaced() {
    return replaced;
  }

  @Override
  public void setReplaced(boolean wasReplaced) {
    replaced = wasReplaced;
  }

  @Override
  public BigDecimal getScore() {
    // If an explicit score value was set then return it
    if (score != null) {
      return score;
    }

    // Otherwise the default is to sum all of the answers
    Double sum = 0.0;
    for(Answer answer : answers) {
      sum = sum + answer.getItemScore().doubleValue();
    }
    return new BigDecimal(sum);
  }

  /**
   * Set an explicit score value.
   */
  public void setScore(BigDecimal score) {
    this.score = score;
  }

  @Override
  public Map<String,BigDecimal> getScores() {
    // If an explicit scores map was set then return it
    if (scores != null) {
      return scores;
    }

    // Otherwise the scores map just contains the pair {getStudyDescription(), getScore()}.
    // Use a LinkedHashMap so that the values are returned in the order they were added.
    Map<String,BigDecimal> scoresMap = new LinkedHashMap<>();
    scoresMap.put(getStudyDescription(), getScore());
    return scoresMap;
  }

  /**
   * Set an explicit scores map.
   */
  public void setScores(Map<String,BigDecimal> scores) {
    this.scores = scores;
  }

  /**
   * Set the answer value for a question identified by the question number.
   */
  public void setAnswer(Integer question, BigDecimal answer) {
    answers.add(new Answer(question.intValue(), answer));
  }

  /**
   * Set the answer value for a question identified by question number and
   * reference id.
   */
  public void setAnswer(Integer question, String ref, BigDecimal answer) {
    answers.add(new Answer(question.intValue(), ref, answer));
  }

  /**
   * Is the question identified by question number answered.
   */
  public boolean isAnswered(int question) {
    for (Answer answer : answers) {
      if (answer.getItemNumber() == question) {
        return true;
      }
    }
    return false;
  }

  /**
   * Is the question identified by reference id answered.
   */
  public boolean isAnswered(String ref) {
    for (Answer answer :answers) {
      if (ref.equals(answer.getRefId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the answer value for a question identified by question number.
   */
  public BigDecimal getAnswer(int question) {
    for (Answer answer : answers) {
      if (answer.getItemNumber() == question) {
        return answer.getItemScore();
      }
    }
    return new BigDecimal(0.0);
  }

  /**
   * Get the answer value for a question identified by reference id.
   */
  public BigDecimal getAnswer(String ref) {
    for (Answer answer :answers) {
      if (ref.equals(answer.getRefId())) {
        return answer.getItemScore();
      }
    }
    return new BigDecimal(0.0);
  }

  /**
   * Get all the answer values.
   */
  public ArrayList<BigDecimal> getAnswers() {
    ArrayList<BigDecimal> answerValues = new ArrayList<>();
    for (Answer answer : answers) {
      answerValues.add(answer.getItemScore());
    }
    return answerValues;
  }

  /**
   * Get all the question numbers.
   */
  public ArrayList<Integer> getQuestions() {
    ArrayList<Integer> questionNumbers = new ArrayList<>();
    for (Answer answer : answers) {
      questionNumbers.add(answer.getItemNumber());
    }
    return questionNumbers;
  }

  @Override
  public ChartScore clone() {
    return new LocalScore(dt, patientId, studyCode, studyDescription, score,scores, assisted, replaced);
  }

  /**
   * Class to hold the answer value for a question.
   */
  static public class Answer {
    int itemNumber;
    String refId;
    BigDecimal itemScore;

    public Answer(int num, BigDecimal score) {
      itemNumber = num;
      refId = null;
      itemScore = score;
    }

    public Answer(int num, String ref, BigDecimal score) {
      itemNumber = num;
      refId = ref;
      itemScore = score;
    }

    public int getItemNumber() {
      return itemNumber;
    }

    public String getRefId() {
      return refId;
    }

    public BigDecimal getItemScore() {
      return itemScore;
    }
  }

}
