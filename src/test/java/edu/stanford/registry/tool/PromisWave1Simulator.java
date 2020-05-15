package edu.stanford.registry.tool;

import edu.stanford.survey.server.CatAlgorithm;
import edu.stanford.survey.server.CatAlgorithm.Item;
import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import edu.stanford.survey.server.CatAlgorithm.Response;
import edu.stanford.survey.server.CatAlgorithm.Score;
import edu.stanford.survey.server.CatAlgorithm2;
import edu.stanford.survey.server.CatAlgorithmPromis;
import edu.stanford.survey.server.CatAlgorithmPromisTwoItemStop;
import edu.stanford.survey.server.CatAlgorithmStanford;
import edu.stanford.survey.server.promis.PromisAnger;
import edu.stanford.survey.server.promis.PromisAnxiety2;
import edu.stanford.survey.server.promis.PromisDepression2;
import edu.stanford.survey.server.promis.PromisFatigue2;
import edu.stanford.survey.server.promis.PromisPainBehavior;
import edu.stanford.survey.server.promis.PromisPainInterference1;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

/**
 * The purpose of this tool is to simulate the effect of various algorithms against
 * the official PROMIS Wave 1 data (real cases where subjects answered the entire
 * item bank). We can then compare the CAT algorithm results.
 */
public class PromisWave1Simulator {
  void checkItemBanks() throws IOException {
//    checkStudies("Depression", PromisDepression2.bank());
//    checkItemBank("Anger", PromisAnger.bank());
//    checkStudies("Anxiety", PromisAnxiety2.bank());
//    checkStudies("Pain Intensity", PromisPainIntensity.bank());
//    checkStudies("Pain Behavior", PromisPainBehavior.bank());
//    checkStudies("Fatigue", PromisFatigue2.bank());

    checkStabilityTuning("Depression", PromisDepression2.bank());
    checkStabilityTuning("Anger", PromisAnger.bank());
    checkStabilityTuning("Anxiety", PromisAnxiety2.bank());
    checkStabilityTuning("Pain Interference", PromisPainInterference1.bank());
    checkStabilityTuning("Pain Behavior", PromisPainBehavior.bank());
    checkStabilityTuning("Fatigue", PromisFatigue2.bank());

    // Physical function was administered in three parts, so no patient has a complete set
//    checkStudies("Physical Function", PromisPhysicalFunction.bank());
    // No sleep items in Wave 1 as far as I can see
//    checkStudies("Sleep Disturbance", PromisSleepDisturbance.bank());
//    checkStudies("Sleep-Related Impairment", PromisSleepRelatedImpairment.bank());
  }

  // Avoids DefaultCharSet exception
  private Reader makeFileReader(String path) throws IOException {
    try {
      return new java.io.InputStreamReader(new java.io.FileInputStream(path), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  /**
   * Run the Stanford algorithm while varying the stability thresholds to see
   * what affect they have on scoring.
   */
  void checkStabilityTuning(String description, final ItemBank itemBank) throws IOException {
    CSVReader reader = null;
    try {
//      dataReader = new FileReader("/Users/garricko/Documents/Registry Framework/PROMIS Wave 1 Data/test.csv");
      Reader dataReader = makeFileReader("/Users/garricko/Documents/Registry Framework/PROMIS Wave 1 Data/promis_w1_final_20100824.csv");

      reader = new CSVReader(dataReader);
      String[] header = reader.readNext();
      System.out.println("Varying score stability threshold (left axis) and error stability threshold (top axis) for "
          + description + ":\n\n");

      ComparisonBucket[][] comparisons = new ComparisonBucket[30][30];
      for (int i = 0; i < comparisons.length; i++) {
        for (int j = 0; j < comparisons[i].length; j++) {
          comparisons[i][j] = new ComparisonBucket();
        }
      }

      CatAlgorithmStanford[][] cats = new CatAlgorithmStanford[30][30];
      for (int i = 0; i < cats.length; i++) {
        for (int j = 0; j < cats[i].length; j++) {
          cats[i][j] = new CatAlgorithmStanford(0.1 + 0.02 * i, 0.01 + 0.01 * j);
          cats[i][j].initialize(itemBank);
        }
      }

      String[] row;
      CatAlgorithm cat = new CatAlgorithmPromis().initialize(itemBank);
      while ((row = reader.readNext()) != null) {
        Map<String, String> headerToValue = new HashMap<>();
        for (int i = 0; i < row.length; i++) {
          headerToValue.put(header[i], row[i]);
        }

        // Calculate the standard score using all available items
        List<Response> allAnswers = new ArrayList<>();
        for (Item item : itemBank.items()) {
          String value = headerToValue.get(item.code());
          if (value != null && value.length() > 0) {
            if (value.endsWith(".0")) {
              value = value.substring(0, value.length() - 2);
            }
            int difficulty = Integer.parseInt(value);
            if (itemBank == PromisFatigue2.bank() && (item.code().startsWith("An") || item.code().startsWith("HI"))) {
              // They seem to have coded the spreadsheet with zero-based index for answers
              // for certain legacy fatigue items
              difficulty += 1;
            }
            Response response = responseByDifficulty(item, difficulty);
            if (response == null) {
              throw new RuntimeException("Check the response coding for item " + item.code() + " because "
                  + value + " is not a valid response");
            }
            allAnswers.add(response);
          }
        }
        Score allItemScore = cat.score(allAnswers);

        // Calculate the score for each instance of our Stanford algorithm
        for (int i = 0; i < cats.length; i++) {
          for (int j = 0; j < cats[i].length; j++) {
            List<Response> catAnswers = new ArrayList<>();
            Item next = cats[i][j].nextItem(catAnswers);
            while (next != null) {
              boolean foundResponse = false;
              for (Response response : allAnswers) {
                if (response.item().equals(next)) {
                  catAnswers.add(response);
                  foundResponse = true;
                  break;
                }
              }
              if (!foundResponse) {
                break;
              }
              next = cats[i][j].nextItem(catAnswers);
            }
            if (next == null) {
              Score catScore = cats[i][j].score(catAnswers);
              // Calculate the score using only answers from standard algorithm
              List<Response> regularCatAnswers = new ArrayList<>();
              next = cat.nextItem(regularCatAnswers);
              while (next != null) {
                boolean foundResponse = false;
                for (Response response : allAnswers) {
                  if (response.item().equals(next)) {
                    regularCatAnswers.add(response);
                    foundResponse = true;
                    break;
                  }
                }
                if (!foundResponse) {
                  break;
                }
                next = cat.nextItem(regularCatAnswers);
              }
              comparisons[i][j].add(allItemScore.score(), regularCatAnswers.size(), catScore.score(), catAnswers.size());
            }
          }
        }
      }

      StringBuilder buf = new StringBuilder("Count with Score +/- 2 of Full Set:\n      ");
      for (int j = 0; j < cats[0].length; j++) {
        buf.append(rpad5(cats[0][j].getErrorStabilityThreshold()));
      }
      buf.append("\n");
      for (int i = 0; i < cats.length; i++) {
        buf.append(rpad5(cats[i][0].getScoreStabilityThreshold()));
        for (int j = 0; j < cats[i].length; j++) {
          buf.append(lpad5(comparisons[i][j].within2));
        }
        buf.append("\n");
      }
      System.out.println(buf);

      buf = new StringBuilder("Count with Score 2-5 Points from Full Set:\n      ");
      for (int j = 0; j < cats[0].length; j++) {
        buf.append(rpad5(cats[0][j].getErrorStabilityThreshold()));
      }
      buf.append("\n");
      for (int i = 0; i < cats.length; i++) {
        buf.append(rpad5(cats[i][0].getScoreStabilityThreshold()));
        for (int j = 0; j < cats[i].length; j++) {
          buf.append(lpad5(comparisons[i][j].neg2to5 + comparisons[i][j].pos2to5));
        }
        buf.append("\n");
      }
      System.out.println(buf);

      buf = new StringBuilder("Count with Score 5-10 Points from Full Set:\n      ");
      for (int j = 0; j < cats[0].length; j++) {
        buf.append(rpad5(cats[0][j].getErrorStabilityThreshold()));
      }
      buf.append("\n");
      for (int i = 0; i < cats.length; i++) {
        buf.append(rpad5(cats[i][0].getScoreStabilityThreshold()));
        for (int j = 0; j < cats[i].length; j++) {
          buf.append(lpad5(comparisons[i][j].neg5to10 + comparisons[i][j].pos5to10));
        }
        buf.append("\n");
      }
      System.out.println(buf);

      buf = new StringBuilder("Count with Score > 10 Points from Full Set:\n      ");
      for (int j = 0; j < cats[0].length; j++) {
        buf.append(rpad5(cats[0][j].getErrorStabilityThreshold()));
      }
      buf.append("\n");
      for (int i = 0; i < cats.length; i++) {
        buf.append(rpad5(cats[i][0].getScoreStabilityThreshold()));
        for (int j = 0; j < cats[i].length; j++) {
          buf.append(lpad5(comparisons[i][j].neg10plus + comparisons[i][j].pos10plus));
        }
        buf.append("\n");
      }
      System.out.println(buf);

      buf = new StringBuilder("Percent Items Saved Relative to Regular CAT:\n      ");
      for (int j = 0; j < cats[0].length; j++) {
        buf.append(rpad5(cats[0][j].getErrorStabilityThreshold()));
      }
      buf.append("\n");
      for (int i = 0; i < cats.length; i++) {
        buf.append(rpad5(cats[i][0].getScoreStabilityThreshold()));
        for (int j = 0; j < cats[i].length; j++) {
          buf.append(rpad5(((double) comparisons[i][j].netSaved) / comparisons[i][j].netSavedFrom * 100));
        }
        buf.append("\n");
      }
      buf.append("\n");
      System.out.println(buf);
    } finally {
      if (reader != null) {
          reader.close();
      }
    }
  }

  void checkItemBank(String description, final ItemBank itemBank)
      throws IOException {
    Reader dataReader = null;
    CSVReader reader = null;
    try {
//      dataReader = new FileReader("/Users/garricko/Documents/Registry Framework/PROMIS Wave 1 Data/test.csv");
      dataReader = makeFileReader("/Users/garricko/Documents/Registry Framework/PROMIS Wave 1 Data/promis_w1_final_20100824.csv");

      reader = new CSVReader(dataReader);
      String[] header = reader.readNext();
      System.out.println("Comparative results for " + description + ":");
//      System.out.println("all_item_score,cat_score,two_item_score,stability_score");

      ComparisonBucket catComparison = new ComparisonBucket();
      ComparisonBucket twoItemComparison = new ComparisonBucket();
      ComparisonBucket stabilityComparison = new ComparisonBucket();
      ComparisonBucket catTwoItemComparison = new ComparisonBucket();
      ComparisonBucket catStabilityComparison = new ComparisonBucket();
      ComparisonBucket twoItemStabilityComparison = new ComparisonBucket();

      String[] row;
      while ((row = reader.readNext()) != null) {
        Map<String,String> headerToValue = new HashMap<>();
        for (int i = 0; i < row.length; i++) {
          headerToValue.put(header[i], row[i]);
        }

        // Calculate the standard score using all available items
        List<Response> allAnswers = new ArrayList<>();
        for (Item item : itemBank.items()) {
          String value = headerToValue.get(item.code());
          if (value != null && value.length() > 0) {
            if (value.endsWith(".0")) {
              value = value.substring(0, value.length()-2);
            }
            allAnswers.add(responseByDifficulty(item, Integer.parseInt(value)));
          }
        }
        CatAlgorithm cat = new CatAlgorithmPromis().initialize(itemBank);
        Score allItemScore = cat.score(allAnswers);

        // Calculate the score using only answers from standard algorithm
        List<Response> catAnswers = new ArrayList<>();
        Item next = cat.nextItem(catAnswers);
        while (next != null) {
          boolean foundResponse = false;
          for (Response response : allAnswers) {
            if (response.item().equals(next)) {
              catAnswers.add(response);
              foundResponse = true;
              break;
            }
          }
          if (!foundResponse) {
            break;
          }
          next = cat.nextItem(catAnswers);
        }
        Score catScore = null;
        if (next == null) {
          catScore = cat.score(catAnswers);
        }

        // Calculate the score using only answers from the two item stopping rule algorithm
        List<Response> twoItemAnswers = new ArrayList<>();
        CatAlgorithm2 cat2 = new CatAlgorithmPromisTwoItemStop().initialize(itemBank);
        next = cat2.nextItem(twoItemAnswers);
        while (next != null) {
          boolean foundResponse = false;
          for (Response response : allAnswers) {
            if (response.item().equals(next)) {
              twoItemAnswers.add(response);
              foundResponse = true;
              break;
            }
          }
          if (!foundResponse) {
            break;
          }
          next = cat2.nextItem(twoItemAnswers);
        }
        Score twoItemScore = null;
        if (next == null) {
          twoItemScore = cat2.score(twoItemAnswers);
        }

        // Calculate the score using only answers from the stability stopping rule algorithm
        List<Response> stabilityAnswers = new ArrayList<>();
        cat2 = new CatAlgorithmStanford().initialize(itemBank);
        next = cat2.nextItem(stabilityAnswers);
        while (next != null) {
          boolean foundResponse = false;
          for (Response response : allAnswers) {
            if (response.item().equals(next)) {
              stabilityAnswers.add(response);
              foundResponse = true;
              break;
            }
          }
          if (!foundResponse) {
            break;
          }
          next = cat2.nextItem(stabilityAnswers);
        }
        Score stabilityScore = null;
        if (next == null) {
          stabilityScore = cat2.score(stabilityAnswers);
        }

        if (catScore != null && twoItemScore != null && stabilityScore != null) {
//          System.out.println(allItemScore.score() + "," + catScore.score()
//              + "," + twoItemScore.score() + "," + stabilityScore.score());
          catComparison.add(allItemScore.score(), allAnswers.size(), catScore.score(), catAnswers.size());
          twoItemComparison.add(allItemScore.score(), allAnswers.size(), twoItemScore.score(), twoItemAnswers.size());
          stabilityComparison.add(allItemScore.score(), allAnswers.size(), stabilityScore.score(), stabilityAnswers.size());
          catTwoItemComparison.add(catScore.score(), catAnswers.size(), twoItemScore.score(), twoItemAnswers.size());
          catStabilityComparison.add(catScore.score(), catAnswers.size(), stabilityScore.score(), stabilityAnswers.size());
          twoItemStabilityComparison.add(twoItemScore.score(), twoItemAnswers.size(), stabilityScore.score(), stabilityAnswers.size());
        }
      }

      System.out.println("\nAll Items Vs. Cat score differences:\n" + catComparison);
      System.out.println("\nAll Items Vs. Two Item score differences:\n" + twoItemComparison);
      System.out.println("\nAll Items Vs. Stability score differences:\n" + stabilityComparison);
      System.out.println("\nCat Vs. Two Item score differences:\n" + catTwoItemComparison);
      System.out.println("\nCat Vs. Stability score differences:\n" + catStabilityComparison);
      System.out.println("\nTwo Item Vs. Stability score differences:\n" + twoItemStabilityComparison);
    } finally {
      if (reader != null) {
          reader.close();
      }
    }
  }

  Response responseByDifficulty(Item item, int difficulty) {
    Response result = null;
    for (Response response : item.responses()) {
      if (response.difficulty() == difficulty) {
        result = response;
        break;
      }
    }
    return result;
  }

  static class ComparisonBucket {
    int neg10plus;
    int neg5to10;
    int neg2to5;
    int within2;
    int pos2to5;
    int pos5to10;
    int pos10plus;
    int correctlyNonClinical;
    int correctlyMidRange;
    int correctlyClinical;
    int incorrectCategory;
    int oppositeCategory;
    int saved1;
    int saved2;
    int saved3;
    int saved4;
    int saved5;
    int saved6;
    int saved7;
    int saved8;
    int saved9;
    int saved10;
    int saved11;
    int savedOther;
    int netSavedFrom;
    int netSaved;

    void add(double expected, int expectedAnswers, double actual, int actualAnswers) {
      double diff = actual - expected;

      if (diff <= -10) {
        neg10plus++;
      } else if (diff <= -5) {
        neg5to10++;
      } else if (diff <= -2) {
        neg2to5++;
      } else if (diff < 2) {
        within2++;
      } else if (diff < 5) {
        pos2to5++;
      } else if (diff < 10) {
        pos5to10++;
      } else {
        pos10plus++;
      }

      if (actual < 40) {
        if (expected < 40) {
          correctlyNonClinical++;
        } else if (expected <= 60) {
          incorrectCategory++;
        } else {
          oppositeCategory++;
        }
      } else if (actual <= 60) {
        if (expected < 40) {
          incorrectCategory++;
        } else if (expected <= 60) {
          correctlyMidRange++;
        } else {
          incorrectCategory++;
        }
      } else {
        if (expected < 40) {
          oppositeCategory++;
        } else if (expected <= 60) {
          incorrectCategory++;
        } else {
          correctlyClinical++;
        }
      }

      int saved = expectedAnswers - actualAnswers;
      netSavedFrom += expectedAnswers;
      netSaved += saved;

      if (saved == 1) {
        saved1++;
      } else if (saved == 2) {
        saved2++;
      } else if (saved == 3) {
        saved3++;
      } else if (saved == 4) {
        saved4++;
      } else if (saved == 5) {
        saved5++;
      } else if (saved == 6) {
        saved6++;
      } else if (saved == 7) {
        saved7++;
      } else if (saved == 8) {
        saved8++;
      } else if (saved == 9) {
        saved9++;
      } else if (saved == 10) {
        saved10++;
      } else if (saved == 11) {
        saved11++;
      } else {
        savedOther++;
      }
    }

    @Override
    public String toString() {
      int total = neg10plus + neg5to10 + neg2to5 + neg2to5 + within2 + pos2to5 + pos5to10 + pos10plus;

      return "neg10plus: " + lpad3(neg10plus) + bar(neg10plus, total) + "\n"
          + " neg5to10: " + lpad3(neg5to10) + bar(neg5to10, total) + "\n"
          + "  neg2to5: " + lpad3(neg2to5) + bar(neg2to5, total) + "\n"
          + "  within2: " + lpad3(within2) + bar(within2, total) + "\n"
          + "  pos2to5: " + lpad3(pos2to5) + bar(pos2to5, total) + "\n"
          + " pos5to10: " + lpad3(pos5to10) + bar(pos5to10, total) + "\n"
          + "pos10plus: " + lpad3(pos10plus) + bar(pos10plus, total) + "\n\n"
          + "correctlyNonClinical: " + lpad3(correctlyNonClinical) + bar(correctlyNonClinical, total) + "\n"
          + "   correctlyMidRange: " + lpad3(correctlyMidRange) + bar(correctlyMidRange, total) + "\n"
          + "   correctlyClinical: " + lpad3(correctlyClinical) + bar(correctlyClinical, total) + "\n"
          + "   incorrectCategory: " + lpad3(incorrectCategory) + bar(incorrectCategory, total) + "\n"
          + "    oppositeCategory: " + lpad3(oppositeCategory) + bar(oppositeCategory, total) + "\n\n"
          + "    saved1: " + lpad3(saved1) + bar(saved1, total) + "\n"
          + "    saved2: " + lpad3(saved2) + bar(saved2, total) + "\n"
          + "    saved3: " + lpad3(saved3) + bar(saved3, total) + "\n"
          + "    saved4: " + lpad3(saved4) + bar(saved4, total) + "\n"
          + "    saved5: " + lpad3(saved5) + bar(saved5, total) + "\n"
          + "    saved6: " + lpad3(saved6) + bar(saved6, total) + "\n"
          + "    saved7: " + lpad3(saved7) + bar(saved7, total) + "\n"
          + "    saved8: " + lpad3(saved8) + bar(saved8, total) + "\n"
          + "    saved9: " + lpad3(saved9) + bar(saved9, total) + "\n"
          + "   saved10: " + lpad3(saved10) + bar(saved10, total) + "\n"
          + "   saved11: " + lpad3(saved11) + bar(saved11, total) + "\n"
          + "     other: " + lpad3(savedOther) + bar(savedOther, total) + "\n\n"
          + "netSaved: " + netSaved + " items (" + (netSaved*100/netSavedFrom) + "%)\n"
          ;
    }

    String bar(int pct, int total) {
      String bar = "***************************************************";
      return " " + bar.substring(0, pct*50/total);
    }

    String lpad3(int value) {
      String s = Integer.toString(value);
      while (s.length() < 3) {
        s = " " + s;
      }
      return s;
    }
  }

  String rpad5(double value) {
    String s = Double.toString(value);
    if (s.length() > 4) {
      s = s.substring(0, 4);
    }
    while (s.length() < 5) {
      s = s + " ";
    }
    return s;
  }

  String lpad5(int value) {
    String s = Integer.toString(value);
    while (s.length() < 5) {
      s = " " + s;
    }
    return s;
  }

  public static void main(String[] args) {
    try {
      new PromisWave1Simulator().checkItemBanks();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
