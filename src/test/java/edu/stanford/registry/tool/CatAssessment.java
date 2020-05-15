package edu.stanford.registry.tool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Data object representing a computer adaptive testing session.
 */
public class CatAssessment {
  @SuppressWarnings("unused")
  private double initialTheta;
  @SuppressWarnings("unused")
  private Type type;

  private List<Step> steps = new ArrayList<>();
//  private String nextItemCode;

  public CatAssessment(double initialTheta, Type type) {
    this.initialTheta = initialTheta;
    this.type = type;
  }

  public Step addStep(String itemCode, Integer response, double theta, double se, String nextItemCode) {
    Step step = new Step(itemCode, response, theta, se, nextItemCode);
    steps.add(step);
    return step;
  }

  public List<Step> getSteps() {
    return steps;
  }

  public String responseSignature() {
    StringBuilder questionAnswerCsv = new StringBuilder();
    boolean first = true;
    for (Step step : getSteps()) {
      if (first) {
        first = false;
      } else {
        questionAnswerCsv.append(",");
      }
      questionAnswerCsv.append(step.getItemCode());
      questionAnswerCsv.append("=");
      questionAnswerCsv.append(step.getResponse());
    }
    return questionAnswerCsv.toString();
  }

  public enum Type {
    assessmentCenter1("PROMIS"),
    assessmentCenter2("PROMIS.2"),
    localPromis("LocalPromis"),
    stanfordCat("StanfordCat"),
    stanfordCatAllowSkip("StanfordCatAllowSkip");

    private final String systemName;

    Type(String systemName) {
      this.systemName = systemName;
    }

    public String getSystemName() {
      return systemName;
    }

    public static Type bySystemName(String systemName) {
      for (Type type : Type.values()) {
        if (type.systemName.equals(systemName)) {
          return type;
        }
      }
      return null;
    }
  }

  public static class Step {
    private String itemCode;
    private Integer response; // null if skipped
    private double theta;
    private double se;
    private String nextItemCode; // null if last

    /**
     * @param itemCode the question that was asked
     * @param response the response, or null if the question was skipped
     * @param theta the raw score
     * @param se the standard error for the raw score (in thetas)
     * @param nextItemCode the question that was asked after this one, or null if this was the last question
     */
    public Step(String itemCode, Integer response, double theta, double se, String nextItemCode) {
      this.itemCode = itemCode;
      this.response = response;
      this.theta = theta;
      this.se = se;
      this.nextItemCode = nextItemCode;
    }

    public void setNextItemCode(String nextItemCode) {
      this.nextItemCode = nextItemCode;
    }

    public boolean wasSkipped() {
      return response == null;
    }

    public String getItemCode() {
      return itemCode;
    }

    public Integer getResponse() {
      return response;
    }

    @Override
    public String toString() {
      return "Step{" +
          "itemCode='" + itemCode + '\'' +
          ", response=" + response +
          ", theta=" + new BigDecimal(theta).setScale(12, RoundingMode.DOWN) +
          ", se=" + new BigDecimal(se).setScale(12, RoundingMode.DOWN) +
          ", nextItemCode='" + nextItemCode + '\'' +
          '}';
    }
  }
}
