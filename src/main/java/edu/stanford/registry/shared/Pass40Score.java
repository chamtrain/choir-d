/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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
import java.util.HashMap;
import java.util.Map;

/*
 * 4 part score for the PASS-40 survey
 */
public class Pass40Score extends LocalScore implements MultiScore {
  private static final int SCALE_COGNITIVE_ANXIETY = 0;
  private static final int ESCAPE_AND_AVOIDANCE = 1;
  private static final int FEARFUL_AVOIDANCE = 2;
  private static final int PHYSIOLOGICAL_ANXIETY = 3;
  private static final String scoreTitle[] = { "Cognitive Anxiety", "Escape and Avoidance", "Fearful Avoidance", "Physiological Anxiety"};
  private int[] scores = {0,0,0,0};

  public Pass40Score(Date dt, String patientId, Integer studyCode, String description) {
    super(dt, patientId, studyCode, description);
  }

  public Pass40Score(Date dt, String patientId, Integer studyCode, String description,
                     BigDecimal score, Map<String,BigDecimal> scores,
                     boolean assisted, boolean wasReplaced) {
    super(dt, patientId, studyCode, description, score, scores, assisted, wasReplaced);
  }

  @Override
  public int getNumberOfScores() {
    return 4;
  }

  @Override
  public String getTitle(int scoreNumber, String studyDescription) {
    if (scoreNumber >= 0 && scoreNumber < scores.length) {
      return scoreTitle[scoreNumber];
    }

    return "Pain Anxiety Symptom Scale [PASS-40]";
  }

  @Override
  public double getScore(int scoreNumber) {
    return scores[scoreNumber];
  }

  @Override
  public Double getPercentileScore(int scoreNumber) {
    return getPercentileScore(scoreNumber, getScore(scoreNumber));
  }

  @Override
  public String getCategoryLabel() {
    return null;
  }

  @Override
  public BigDecimal getScore() {
    return new BigDecimal(getScore(0));
  }

  @Override
  public Map<String, BigDecimal> getScores() {
    HashMap<String, BigDecimal> scoresMap = new HashMap<>();
    for (int inx = 0; inx < scores.length; inx++) {

      scoresMap.put(scoreTitle[inx], new BigDecimal(scores[inx]));
    }
    return scoresMap;
  }

  @Override
  public ChartScore clone() {
    return new Pass40Score(getDate(), getPatientId(), getStudyCode(), getStudyDescription(), getScore() ,getScores(), getAssisted(), wasReplaced());
  }

  public void addScore(String refKey, int scoreValue) {
    String reversed[] = {"2", "8", "16", "31", "40"};
    String scaleCog[] = {"2", "6", "10", "14", "22", "26", "30", "34", "37", "40"};
    String escapAnd[] = {"3", "7", "11", "15", "19", "23", "27", "31", "35", "39"};
    String fearfApp[] = {"1", "5", "8", "13", "16", "18", "21", "25", "29", "33"};
    String physiAnx[] = {"4", "9", "12", "17", "20", "24", "28", "32", "36", "38"};

    for (String reverse : reversed) {
      if (reverse.equals(refKey)) {
        scoreValue = reversed(scoreValue);
      }
    }
    for (String key : scaleCog) {
      if (key.equals(refKey)) {
        addScore(SCALE_COGNITIVE_ANXIETY, scoreValue);
      }
    }
    for (String key: escapAnd) {
      if (key.equals(refKey)) {
        addScore(ESCAPE_AND_AVOIDANCE, scoreValue);
      }
    }
    for (String key: fearfApp) {
      if (key.equals(refKey)) {
        addScore(FEARFUL_AVOIDANCE, scoreValue);
      }
    }
    for (String key: physiAnx) {
      if (key.equals(refKey)) {
        addScore(PHYSIOLOGICAL_ANXIETY, scoreValue);
      }
    }
  }

  private void addScore(int scoreNumber,  int scoreValue) {
    scores[scoreNumber] = scores[scoreNumber] + scoreValue;
  }

  private int reversed(int scoreValue) {
    return Math.abs(scoreValue - 5);
  }

  public Double getPercentileScore(int scoreNumber, Double score) {

    // percentileChart { { percentile-rank, cognitive-anxiety, escape-avoidence, fearful-appraisal, physiological-anxiety, total } }
    int[][] percentileChart = {
        { 95, 46, 42, 43, 40, 163 },
        { 90, 43, 40, 41, 38, 152 },
        { 85, 41, 38, 36, 32, 144 },
        { 80, 39, 37, 34, 30, 135 },
        { 75, 37, 35, 32, 28, 127 },
        { 70, 34, 33, 29, 26, 118 },
        { 65, 32, 32, 27, 25, 109 },
        { 60, 29, 30, 26, 22, 103 },
        { 55, 27, 29, 24, 20, 97 },
        { 50, 26, 28, 22, 19, 93 },
        { 45, 24, 27, 20, 17, 89 },
        { 40, 22, 25, 18, 15, 86 },
        { 35, 21, 24, 16, 13, 82 },
        { 30, 19, 22, 15, 12, 72 },
        { 25, 17, 21, 14, 9, 65 },
        { 20, 16, 20, 12, 6, 60 },
        { 15, 15, 17, 11, 5, 54 },
        { 10, 10, 15, 9, 3, 44 },
        { 5, 6, 9, 6, 1, 34 },

    };
    int entry = scoreNumber + 1;
    for (int inx = percentileChart.length -1; inx >= 0; inx--) {
      if (percentileChart[inx][entry] >= score) {
        return Double.valueOf(percentileChart[inx][0]);
      }
    }
    return 0d;
  }


}
