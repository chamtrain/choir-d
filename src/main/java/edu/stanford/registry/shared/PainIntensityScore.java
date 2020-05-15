package edu.stanford.registry.shared;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


public class PainIntensityScore extends LocalScore {

  private final String[] scoreDescriptions = {"Worst","Average","Now","Least"};

  public PainIntensityScore(Date dt, String patientId, Integer studyCode, String description) {
    super(dt, patientId, studyCode, description);
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

  /**
   * Return a map of all the pain intensity values.
   */
  @Override
  public Map<String,BigDecimal> getScores() {
    // Use a LinkedHashMap so that the values are returned in
    // the order in which they were added.
    Map<String,BigDecimal> scores = new LinkedHashMap<>();
    if (answers != null && answers.size() > 0) {
      for (int inx = 0; inx < answers.size(); inx++) {
        BigDecimal score = answers.get(inx).getItemScore();
        scores.put(scoreDescriptions[inx], score);
      }
    }
    return scores;
 }

}
