package edu.stanford.registry.shared;

/**
 * Created by tpacht on 1/22/2016.
 */
public interface MultiScore extends ChartScore {
  public int getNumberOfScores();

  public String getTitle(int scoreNumber, String studyDescription);

  public double getScore(int scoreNumber);

  public Double getPercentileScore(int scoreNumber);

}
