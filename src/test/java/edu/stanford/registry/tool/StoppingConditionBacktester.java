package edu.stanford.registry.tool;

import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.tool.CatAssessment.Step;

import edu.stanford.survey.server.CatAlgorithm;
import edu.stanford.survey.server.CatAlgorithm.Item;
import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import edu.stanford.survey.server.CatAlgorithm.Response;
import edu.stanford.survey.server.CatAlgorithm2;
import edu.stanford.survey.server.CatAlgorithmPromis;
import edu.stanford.survey.server.CatAlgorithmPromisTwoItemStop;
import edu.stanford.survey.server.CatAlgorithmStanford;
import edu.stanford.survey.server.CatAlgorithmStanfordPrior;
import edu.stanford.survey.server.CatAlgorithmStanfordTwoItemStop;
import edu.stanford.survey.server.promis.PromisAnger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * The purpose of this tool is to test new CAT stopping conditions against existing data to
 * reveal how much more efficient they are and how they impact scoring.
 */
public class StoppingConditionBacktester extends Tool {
  private static final Logger logger = Logger.getLogger(StoppingConditionBacktester.class);

  private static void checkStudies() {
//    checkStudies("Depression", 1007, new CatAlgorithmStanford(), PromisDepression2.bank(), PromisDepression.bank());
    checkStudies("Anger", 1044, new CatAlgorithmStanford(), PromisAnger.bank());
    checkStudies("Anger", 1044, new CatAlgorithmStanfordPrior(), PromisAnger.bank());
    checkStudies("Anger", 1044, new CatAlgorithmPromisTwoItemStop(), PromisAnger.bank());
    checkStudies("Anger", 1044, new CatAlgorithmStanfordTwoItemStop(), PromisAnger.bank());
//    checkStudies("Anxiety", 1008, new CatAlgorithmStanford(), PromisAnxiety2.bank(), PromisAnxiety.bank());
//    checkStudies("Pain Intensity", 1003, new CatAlgorithmStanford(), PromisPainIntensity.bank());
//    checkStudies("Pain Behavior", 1004, new CatAlgorithmStanford(), PromisPainBehavior.bank());
//    checkStudies("Physical Function", 1005, new CatAlgorithmStanford(), PromisPhysicalFunction.bank());
//    checkStudies("Fatigue", 1006, new CatAlgorithmStanford(), PromisFatigue2.bank(), PromisFatigue.bank());
//    checkStudies("Sleep Disturbance", 1042, new CatAlgorithmStanford(), PromisSleepDisturbance.bank());
//    checkStudies("Sleep-Related Impairment", 1043, new CatAlgorithmStanford(), PromisSleepRelatedImpairment.bank());
  }

  private static void checkStudies(String description, final int studyCode, CatAlgorithm2 newAlgorithm,
                                   final ItemBank version2Bank) {
    checkStudies(description, studyCode, newAlgorithm, version2Bank, version2Bank);
  }

  private static void checkStudies(final String description, final int studyCode, final CatAlgorithm2 newAlgorithm,
                                   final ItemBank version2Bank, final ItemBank version1Bank) {
    final int version1SystemId = 1001;

    runTransaction("<>", new ServerRunnable() {
      @Override
      public void run() {
        CatAssessmentLoader assessmentLoader = new CatAssessmentLoader(database, 1L);
        List<PatientStudyExtendedData> studies = assessmentLoader.studiesByStudyCode(studyCode, true);
        System.out.println("***** " + description + " surveys: " + studies.size());

        CatAlgorithm cat1 = new CatAlgorithmPromis().initialize(version1Bank);
        CatAlgorithm cat2 = new CatAlgorithmPromis().initialize(version2Bank);
        Map<String, CatAssessment> uniqueResults = new HashMap<>();
        Counts counts = new Counts();
        Counts uniqueCounts = new Counts();
        List<ScoreDiff> diffs = new ArrayList<>();
        int dupCount = 0;
        int matchCount = 0;
        int fewerQuestions = 0;
        double maxScoreDeviation = 0;
        for (PatientStudyExtendedData study : studies) {
          CatAssessment assessment = assessmentLoader.assessmentForStudy(study);
          if (assessment == null) {
            logger.error("No assessment for study " + study.getToken());
            continue;
          }

          String responseSignature = assessment.responseSignature();
          boolean isDup = uniqueResults.containsKey(responseSignature);
          if (isDup) {
            dupCount++;
          }

          uniqueResults.put(responseSignature, assessment);

          // Simulate both standard and new algorithm with given responses to figure out score and # items
          CatAlgorithm cat;
          CatAlgorithm2 newCat;
          if (study.getSurveySystemId() == version1SystemId) {
            cat = cat1;
            newCat = newAlgorithm.initialize(version1Bank);
          } else {
            cat = cat2;
            newCat = newAlgorithm.initialize(version2Bank);
          }

          List<Response> answers = new ArrayList<>();
          List<Response> stanfordAnswers = new ArrayList<>();
          Item nextItem = cat.nextItem(answers);
          Item stanfordNextItem = newCat.nextItem(answers);
          boolean error = false;
          while (nextItem != null) {
            Response found = null;
            for (Step step : assessment.getSteps()) {
              if (step.getItemCode().equals(nextItem.code())) {
                found = nextItem.responses()[step.getResponse()];
                answers.add(found);
                break;
              }
            }
            if (found == null) {
              logger.error("No response in study " + study.getToken() + " for item " + nextItem.code());
              error = true;
              break;
            }

            if (stanfordNextItem != null) {
              if (!stanfordNextItem.equals(nextItem)) {
                logger.error("For study " + study.getToken() + " Stanford item " + stanfordNextItem.code()
                    + " does not match " + nextItem.code());
                error = true;
                break;
              }
              stanfordAnswers.add(found);
              stanfordNextItem = newCat.nextItem(stanfordAnswers);
            }

            nextItem = cat.nextItem(answers);
          }
          if (!error) {
            double oldScore = cat.score(answers).score();
            int oldItemCount = answers.size();
            double newScore = newCat.score(stanfordAnswers).score();
            int newItemCount = stanfordAnswers.size();
            counts.add(oldScore, oldItemCount, newScore, newItemCount);
            if (!isDup) {
              uniqueCounts.add(oldScore, oldItemCount, newScore, newItemCount);
              diffs.add(new ScoreDiff(study.getToken(), oldScore, oldItemCount, newScore, newItemCount));
            }
//            System.out.println(study.getToken() + "," + cat.score(answers).score() + "," + answers.size() + ","
//                + newCat.score(stanfordAnswers).score() + "," + stanfordAnswers.size());
            maxScoreDeviation = Math.max(maxScoreDeviation, Math.abs(cat.score(answers).score()
                - newCat.score(stanfordAnswers).score()));
            matchCount++;
            if (stanfordAnswers.size() < answers.size()) {
              fewerQuestions++;
            }
          }
        }

        StringBuilder buf = new StringBuilder();

        buf.append("All ").append(description).append(" Surveys using ")
            .append(newAlgorithm.getClass()).append(":\n\n");
        printCounts(buf, counts, false);
        buf.append("\nAll ").append(description).append(" Surveys With Percents:\n\n");
        printCounts(buf, counts, true);

        buf.append("\nUnique ").append(description).append(" Surveys:\n\n");
        printCounts(buf, uniqueCounts, false);
        buf.append("\nUnique ").append(description).append(" Surveys With Percents:\n\n");
        printCounts(buf, uniqueCounts, true);

        buf.append("\nTop ").append(description).append(" Score Discrepancies:\n\n");

        Collections.sort(diffs, new Comparator<ScoreDiff>() {
          @Override
          public int compare(ScoreDiff scoreDiff, ScoreDiff scoreDiff2) {
            return Double.compare(Math.abs(scoreDiff2.oldScore - scoreDiff2.newScore),
                Math.abs(scoreDiff.oldScore - scoreDiff.newScore));
          }
        });
        buf.append("Token,OldScore,OldItemCount,NewScore,NewItemCount\n");
        for (int i = 0; i < 10 && i < diffs.size(); i++) {
          buf.append(diffs.get(i).token).append(",");
          buf.append(diffs.get(i).oldScore).append(",");
          buf.append(diffs.get(i).oldItemCount).append(",");
          buf.append(diffs.get(i).newScore).append(",");
          buf.append(diffs.get(i).newItemCount).append("\n");
        }

        System.out.println(buf.toString());

        System.out.println("***** " + description + " summary: total=" + studies.size() + " unique="
            + (studies.size() - dupCount) + " match=" + matchCount + " "
            + Math.round(matchCount / (double) (studies.size()/*-dupCount*/) * 100.0) + "% fewerQs=" + fewerQuestions
            + " maxDelta=" + maxScoreDeviation + " wrong="
            + (studies.size()/*-dupCount*/ - matchCount) + " "
            + Math.round((studies.size()/*-dupCount*/ - matchCount) / (double) (studies.size()/*-dupCount*/) * 100.0)
            + "%");
      }
    });
  }

  private static void printCounts(StringBuilder buf, Counts counts, boolean printPercents) {
    buf.append("Items    Survey  Original Items                                Score Change\n");
    buf.append("Avoided  Count     12   11   10    9    8    7    6    5    4    <1  1-2  2-3  3-4  4-5 5-10  >10\n");
    for (int i = 0; i < counts.itemCounts.length; i++) {
      ItemCounts itemCount = counts.itemCounts[i];
      if (itemCount == null) {
        buf.append(lpad(i, 5)).append("         0\n");
        continue;
      }
      buf.append(lpad(i, 5)).append("    ").append(lpad(itemCount.total, 6)).append("  ");
      for (int j = itemCount.counts.length - 1; j > 2; j--) {
        buf.append(lpad(itemCount.counts[j], 4)).append(" ");
      }
      buf.append(" ");
      ScoreCounts scoreCount = counts.scoreCounts[i];
      buf.append(lpad(scoreCount.ltOne, 4)).append(" ");
      buf.append(lpad(scoreCount.oneToTwo, 4)).append(" ");
      buf.append(lpad(scoreCount.twoToThree, 4)).append(" ");
      buf.append(lpad(scoreCount.threeToFour, 4)).append(" ");
      buf.append(lpad(scoreCount.fourToFive, 4)).append(" ");
      buf.append(lpad(scoreCount.fiveToTen, 4)).append(" ");
      buf.append(lpad(scoreCount.gtTen, 4)).append(" \n");

      if (printPercents) {
        buf.append("         ");
        buf.append(lpadpct(itemCount.total, counts.surveyCount, 6)).append("  ");
        for (int j = itemCount.counts.length - 1; j > 2; j--) {
          buf.append(lpadpct(itemCount.counts[j], itemCount.total, 4)).append(" ");
        }
        buf.append(" ");
        buf.append(lpadpct(scoreCount.ltOne, itemCount.total, 4)).append(" ");
        buf.append(lpadpct(scoreCount.oneToTwo, itemCount.total, 4)).append(" ");
        buf.append(lpadpct(scoreCount.twoToThree, itemCount.total, 4)).append(" ");
        buf.append(lpadpct(scoreCount.threeToFour, itemCount.total, 4)).append(" ");
        buf.append(lpadpct(scoreCount.fourToFive, itemCount.total, 4)).append(" ");
        buf.append(lpadpct(scoreCount.fiveToTen, itemCount.total, 4)).append(" ");
        buf.append(lpadpct(scoreCount.gtTen, itemCount.total, 4)).append(" \n");
      }
    }
  }

  static String lpad(int number, int width) {
    String result = Integer.toString(number);

    while (result.length() < width) {
      result = " " + result;
    }

    return result;
  }

  static String lpadpct(int number, int percentOf, int width) {
    int pct = (int) Math.round(number / (float) percentOf * 100.0);
    return lpad(pct, width-1) + "%";
  }

  static class ScoreDiff {
    String token;
    double oldScore;
    int oldItemCount;
    double newScore;
    int newItemCount;

    ScoreDiff(String token, double oldScore, int oldItemCount, double newScore, int newItemCount) {
      this.token = token;
      this.oldScore = oldScore;
      this.oldItemCount = oldItemCount;
      this.newScore = newScore;
      this.newItemCount = newItemCount;
    }
  }

  private static class Counts {
    // Index for both arrays is the number of items saved (oldItemCount - newItemCount)
    ItemCounts[] itemCounts = new ItemCounts[12];
    ScoreCounts[] scoreCounts = new ScoreCounts[12];
    int surveyCount;

    void add(double oldScore, int oldItemCount, double newScore, int newItemCount) {
      int index = oldItemCount - newItemCount;
      if (itemCounts[index] == null) {
        itemCounts[index] = new ItemCounts();
      }
      if (scoreCounts[index] == null) {
        scoreCounts[index] = new ScoreCounts();
      }
      itemCounts[index].add(oldItemCount);
      scoreCounts[index].add(oldScore, newScore);
      surveyCount++;
    }
  }

  private static class ItemCounts {
    // Index is oldItemCount - 1
    int[] counts = new int[12];
    int total;

    public void add(int oldItemCount) {
      counts[oldItemCount - 1]++;
      total++;
    }
  }

  private static class ScoreCounts {
    int ltOne;
    int oneToTwo;
    int twoToThree;
    int threeToFour;
    int fourToFive;
    int fiveToTen;
    int gtTen;

    public void add(double oldScore, double newScore) {
      double scoreDiff = Math.abs(oldScore - newScore);
      if (scoreDiff < 1) {
        ltOne++;
      } else if (scoreDiff < 2) {
        oneToTwo++;
      } else if (scoreDiff < 3) {
        twoToThree++;
      } else if (scoreDiff < 4) {
        threeToFour++;
      } else if (scoreDiff < 5) {
        fourToFive++;
      } else if (scoreDiff < 10) {
        fiveToTen++;
      } else if (scoreDiff > 10) {
        gtTen++;
      }
    }
  }

  public static void main(String[] args) {
    try {
      checkStudies();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
