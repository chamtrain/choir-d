/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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


public class GlobalHealthScore extends LocalScore {

  public GlobalHealthScore(Date dt, String patientId, Integer studyCode, String description) {
    super(dt, patientId, studyCode, description);
    // TODO Auto-generated constructor stub
  }
  @Override
  public BigDecimal getScore() {
    Double score = 0.0;
    if (answers != null && answers.size() > 0) {
      for (Answer answer : answers) {
        score = score + answer.getItemScore().doubleValue();
      }
    }
    return new BigDecimal(score);
  }

    // need two scores mental health & physical health
  /* Align our question "Order" numbers to the assessment center "global##"'s
   * 1-5    =   global01-05
   * 6      =   global09
   * 7      =   global06
   * 8      =   global10
   * 9      =   global08
   * painInt=   global07
   */


  public double getMentalHealthTScore() {
    return getTranslatedValue(getMentalRawScore(), MENT_TSCORE_ARRAY);
  }

  public double getMentalHealthSE() {
    return getTranslatedValue(getMentalRawScore(), MENT_SE_ARRAY);
  }

  // mentalhealth score = sum[global02, global04, global05, global10rescored] (our survey question #"s 2, 4, 5, 8)
  public int getMentalRawScore() {
    int rawScore =0;
    rawScore += this.getAnswer(2).intValue();
    rawScore += this.getAnswer(4).intValue();
    rawScore += this.getAnswer(5).intValue();
    rawScore += this.getAnswer(8).intValue();
    return rawScore;
  }

  public double getPhysicalHealthTScore() {
    return getTranslatedValue(getPhysicalRawScore(), PHYS_TSCORE_ARRAY);
  }

  public double getPhysicalHealthSE() {
    return getTranslatedValue(getPhysicalRawScore(), PHYS_SE_ARRAY);
  }

  // physicalhealth score = sum[global03, global06, global07rescored, global08rescored] (our survey question #"s 3, 7, 9, painInt)
  public int getPhysicalRawScore() {
    int rawScore =0;
    rawScore += this.getAnswer(3).intValue();
    rawScore += this.getAnswer(7).intValue();
    rawScore += this.getAnswer(9).intValue();
    //rawScore += this.getAnswer(10).intValue();
    int painScore = 5; // No Pain
    if (this.getAnswer(10).intValue() > 9) {
      painScore = 1;
    } else if (this.getAnswer(10).intValue() > 6) {
      painScore = 2;
    } else if (this.getAnswer(10).intValue() > 3) {
      painScore = 3;
    } else if (this.getAnswer(10).intValue() > 0) {
      painScore = 4;
    }
    rawScore += painScore;
    return rawScore;
  }

  //our question #6 (global09) is not counted in either score.

  public double getTranslatedValue(int rawScore, double[] array) {
    if (rawScore <= RAWSCORE_ARRAY[0]) {
      return array[0];
    }
    for (int r=0; r<RAWSCORE_ARRAY.length; r++) {
      if (rawScore == RAWSCORE_ARRAY[r]) {
        return array[r];
      }
    }
    return array[array.length - 1];

  }

  /*see http://www.assessmentcenter.net/documents/Scoring%20PROMIS%20Global%20short%20form.pdf
    for scoring instructions and tables (Tscore & SE) */
  public static int[] RAWSCORE_ARRAY = {
    4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 ,16, 17, 18, 19, 20
  };
  public static double[] PHYS_TSCORE_ARRAY = {
    16.2, 19.9, 23.5, 26.7, 29.6, 32.4, 34.9, 37.4, 39.8, 42.3, 44.9, 47.7, 50.8, 54.1, 57.7, 61.9, 67.7
  };
  public static double[] PHYS_SE_ARRAY = {
      4.8, 4.7, 4.5, 4.3, 4.2, 4.2, 4.1, 4.1, 4.1, 4.2, 4.3, 4.4, 4.6, 4.7, 4.9, 5.2, 5.9
  };
  public static double[] MENT_TSCORE_ARRAY = {
    21.2, 25.1, 28.4, 31.3, 33.8, 36.3, 38.8, 41.1, 43.5, 45.8, 48.3, 50.8, 53.3, 56.0, 59.0, 62.5, 67.6
  };
  public static double[] MENT_SE_ARRAY = {
    4.6,  4.1, 3.9, 3.7, 3.7, 3.7, 3.6, 3.6, 3.6, 3.6, 3.7, 3.7, 3.7, 3.8, 3.9, 4.2, 5.3
  };

  public String getPhysicalCategoryLabel() {
    if (studyDescription.equals("parentGlobalHealth")) {
      return getGlobalPhysicalHealthLabel(getPhysicalHealthTScore());
    }
    return super.getCategoryLabel();
  }

  public String getMentalCategoryLabel() {

    if (studyDescription.equals("parentGlobalHealth")) {
      return getGlobalMentalHealthLabel(getMentalHealthTScore());
    }
    return super.getCategoryLabel();
  }

  private static final String[] PROMIS_GLOBAL_HEALTH_LABELS =  {"Excellent", "Very Good", "Good", "Fair", "Poor"};

  private static String getGlobalMentalHealthLabel(Double score) {
    return PROMIS_GLOBAL_HEALTH_LABELS[getGlobalMentalHealthLevel(score)];
  }
  private static int getGlobalMentalHealthLevel(Double score) {
    if (score >= 56) { // Excellent
      return 0;
    }
    if (score >= 48) { // Very Good
      return 1;
    }
    if (score >= 40) { // Good
      return 2;
    }
    if (score >= 29) { // Fair
      return 3;
    }
    return 4; // Poor
  }

  private static String getGlobalPhysicalHealthLabel(Double score) {
    return PROMIS_GLOBAL_HEALTH_LABELS[getGlobalPhysicalHealthLevel(score)];
  }

  private static int getGlobalPhysicalHealthLevel(Double score) {
    if (score >= 58) { // Excellent
      return 0;
    }
    if (score >= 50) { // Very Good
      return 1;
    }
    if (score >= 42) { // Good
      return 2;
    }
    if (score >= 35) { // Fair
      return 3;
    }
    return 4; // Poor
  }
}
