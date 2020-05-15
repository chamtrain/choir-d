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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class PROMISScore implements ChartScore {

  String patientId;
  Integer studyCode;
  String studyDescription;
  Date dt;
  boolean assisted = false;
  private boolean replaced = false;

  Double theta;
  Double stdDeviation;
  Integer lineNumber = -1;
  String categoryLabel = null;

  public PROMISScore(Date dt, String patientId, Integer studyCode, String description,
                     Double theta, Double stdDev) {
    this.dt = dt;
    this.patientId = patientId;
    this.studyCode = studyCode;
    this.studyDescription = description;
    this.theta = theta;
    this.stdDeviation = stdDev;
    this.categoryLabel = PromisCategoryLabel.categoryLabelFor(description, getScore().doubleValue());
  }

  public PROMISScore(Date dt, String patientId, Integer studyCode, String description,
                     Double theta, Double stdDev, Integer lineNumber, String categoryLabel, boolean assisted,
                     boolean wasReplaced) {
    this(dt, patientId, studyCode, description, theta, stdDev);
    this.categoryLabel = categoryLabel;
    this.lineNumber = lineNumber;
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
  public BigDecimal getScore() {

    Double scoreDouble = 10 * theta + 50;
    BigDecimal score = new BigDecimal(scoreDouble);
    return score.setScale(2, BigDecimal.ROUND_HALF_EVEN);
  }

  @Override
  public Date getDate() {
    return dt;
  }

  public Double getTheta() {
    return theta;
  }

  public Double getStdError() {
    return stdDeviation;
  }

  public Integer getQuestionNumber() {
    return lineNumber;
  }

  public void setCategoryLabel(String label) {
    categoryLabel = label;
  }

  @Override
  public String getCategoryLabel() {
    if (categoryLabel == null) {
      categoryLabel = "";
    }
    return categoryLabel;
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
  public ChartScore clone() {
    return new PROMISScore(dt, patientId, studyCode, studyDescription, theta, stdDeviation,
        lineNumber, categoryLabel, assisted, replaced);
  }

  @Override
  public Map<String,BigDecimal> getScores() {
    // Use a LinkedHashMap so that the values are returned in
    // the order in which they were added.
    Map<String,BigDecimal> scores = new LinkedHashMap<>();
    scores.put(getStudyDescription(), getScore());
    return scores;
  }
}
