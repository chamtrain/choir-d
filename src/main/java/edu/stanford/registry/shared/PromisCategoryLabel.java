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


public class PromisCategoryLabel {

  private static final String EMPTY = "";

  public static String categoryLabelFor(String description, Double score) {
    if (description != null) {

      if (description.equals("PROMIS Depression Bank") || description.equals("PROMIS Bank v1.0 - Depression")) {
        return getDepressionLevelLabel(score);
      }
      if (description.equals("PROMIS Anxiety Bank") || description.equals("PROMIS Bank v1.0 - Anxiety")) {
        return getAnxietyLevelLabel(score);
      }
      if (description.equals("PROMIS Bank v1.0 - Sleep Disturbance")) {
        return getSleepDisturbLevelLabel(score);
      }
      if (description.equals("PROMIS Ped Bank v1.0 - Pain Interference") ||
          description.equals("PROMIS Parent Proxy Bank v1.0 - Pain Interference") ||
          description.equals("PROMIS Ped Bank v1.1 - Anxiety") ||
          description.equals("PROMIS Parent Proxy Bank v1.1 - Anxiety") ||
          description.equals("PROMIS Ped Bank v1.1 - Depressive Sx") ||
          description.equals("PROMIS Parent Proxy Bank v1.1 - Depressive Sx") ||
          description.equals("PROMIS Ped Bank v1.0 - Fatigue") ||
          description.equals("PROMIS Parent Proxy Bank v1.0 - Fatigue")) {
        return getPedAngerAnxietyDepressFatigueInterfLabel(score);
      }
      if (description.equals("PROMIS Parent Proxy Bank v1.0 - Peer Relations") ||
          description.equals("PROMIS Ped Bank v1.0 - Peer Rel")) {
        return getPedFamilyPeerStrengthLabel(score);
      }
    }
    return EMPTY;
  }

  // Depression category labels
  private final static String[] DEPRESSION_RANGE_LABELS = { "None/Minimal", "Mild", "Moderate", "Mod-Severe", "Severe" };

  private static String getDepressionLevelLabel(Double score) {
    return DEPRESSION_RANGE_LABELS[getDepressionLevel(score)];
  }

  private static int getDepressionLevel(Double score) {
    if (score >= 70.3) { // Severe
      return 4;
    }
    if (score > 64.7) { // Moderate to Severe
      return 3;
    }
    if (score >= 58.6) { // Moderate
      return 2;
    }
    if (score >= 50.5) { // Mild
      return 1;
    }
    return 0; // Minimal
  }

  // Anxiety category labels
  private static final String[] ANXIETY_RANGE_LABELS = { "Normal", "Mild", "Moderate", "Severe" };

  private static String getAnxietyLevelLabel(Double score) {
    return ANXIETY_RANGE_LABELS[getAnxietyLevel(score)];
  }

  private static int getAnxietyLevel(Double score) {

    if (score >= 67.7) { // Severe
      return 3;
    }
    if (score >= 60.9) { // Moderate
      return 2;
    }
    if (score >= 52.6) { // Mild
      return 1;
    }
    return 0; // Normal
  }

  // Sleep disturbance category labels
  private static final String[] SLEEP_DISTURB_RANGE_LABELS = { "None to Slight", "Mild", "Moderate", "Severe" };

  private static String getSleepDisturbLevelLabel(Double score) {
    return SLEEP_DISTURB_RANGE_LABELS[getSleepDisturbLevel(score)];
  }

  private static int getSleepDisturbLevel(Double score) {
    if (score >= 70) { // Severe
      return 3;
    }
    if (score >= 60) { // Moderate
      return 2;
    }
    if (score >= 55) { // Mild
      return 1;
    }
    return 0; // None to Slight
  }

  /*
   * PROMIS Pediatric and Parent Proxy Anger, Anxiety, Depressive Symptoms, Fatigue and Pain Interference
   */
  private static final String[] PED_PROMIS_ANG_ANX_DEP_FAT_INT_LABELS = { "WNL", "Mild", "At-Risk", "Elevated"};

  private static String getPedAngerAnxietyDepressFatigueInterfLabel(Double score) {
    return PED_PROMIS_ANG_ANX_DEP_FAT_INT_LABELS[getPedAngerAnxietyDepressFatigueInterfLevel(score)];
  }

  private static int getPedAngerAnxietyDepressFatigueInterfLevel(Double score) {
    if (score >= 65) { // Severe
      return 3;
    }
    if (score >= 55) { // Moderate
      return 2;
    }
    if (score >= 50) { // Mild
      return 1;
    }
    return 0; // Within Normal Limits
  }

  /*
   * PROMIS Pediatric and Parent Proxy Family Relationships, Peer Relationships and Strength Impact
   */
  private static final String[] PED_PROMIS_FAMILY_PEER_STRENGTH_LABELS = {"Excellent", "Good", "Fair", "Poor"};

  private static String getPedFamilyPeerStrengthLabel(Double score) {
    return PED_PROMIS_FAMILY_PEER_STRENGTH_LABELS[getPedFamilyPeerStrengthLevel(score)];
  }

  private static int getPedFamilyPeerStrengthLevel(Double score) {
    if (score >= 60) { // Excellent
      return 0;
    }
    if (score >= 40) { // Good
      return 1;
    }
    if (score >= 30) { // Fair
      return 2;
    }
    return 3; // Poor
  }
}
