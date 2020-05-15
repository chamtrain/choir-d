package edu.stanford.registry.tool;

import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.tool.CatAssessment.Step;
import edu.stanford.registry.tool.CatAssessment.Type;
import edu.stanford.survey.server.CatAlgorithm;
import edu.stanford.survey.server.CatAlgorithm.Item;
import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import edu.stanford.survey.server.CatAlgorithm.Response;
import edu.stanford.survey.server.CatAlgorithm.Score;
import edu.stanford.survey.server.CatAlgorithmPromis;
import edu.stanford.survey.server.promis.PromisAnger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * The purpose of this tool is to load PROMIS results stored in the database and simulate
 * walking through the same scenario with the local CAT algorithm. It then reports any
 * discrepancies for review. The goal is to make sure the local CAT algorithm correctly
 * replicates the behavior of the Northwestern implementation.
 */
public class CompatibilityBacktester extends Tool {
  private static final Logger logger = Logger.getLogger(CompatibilityBacktester.class);

  private static void checkStudies() {
//    checkStudies("Depression", 1007, PromisDepression2.bank(), PromisDepression.bank());
    checkStudies("Anger", 1044, PromisAnger.bank());
//    checkStudies("Anxiety", 1008, PromisAnxiety2.bank(), PromisAnxiety.bank());
//    checkStudies("Pain Intensity", 1003, PromisPainIntensity.bank());
//    checkStudies("Pain Behavior", 1004, PromisPainBehavior.bank());
//    checkStudies("Physical Function", 1005, PromisPhysicalFunction.bank());
//    checkStudies("Fatigue", 1006, PromisFatigue2.bank(), PromisFatigue.bank());
//    checkStudies("Sleep Disturbance", 1042, PromisSleepDisturbance.bank());
//    checkStudies("Sleep-Related Impairment", 1043, PromisSleepRelatedImpairment.bank());
  }

  private static void checkStudies(String description, final int studyCode, final ItemBank version2Bank) {
    checkStudies(description, studyCode, version2Bank, version2Bank);
  }

  private static void checkStudies(final String description, final int studyCode,
                                   final ItemBank version2Bank, final ItemBank version1Bank) {
    final int version1SystemId = 1001;

    runTransaction("admin", new ServerRunnable() {
      @Override
      public void run() {
        CatAssessmentLoader assessmentLoader = new CatAssessmentLoader(database, 1L);
        List<PatientStudyExtendedData> studies = assessmentLoader.studiesByStudyCode(studyCode, false);
        System.out.println("***** " + description + " surveys: " + studies.size());

        CatAlgorithm cat1 = new CatAlgorithmPromis().initialize(version1Bank);
        CatAlgorithm cat2 = new CatAlgorithmPromis().initialize(version2Bank);
        Map<String, CatAssessment> uniqueResults = new HashMap<>();
        int dupCount = 0;
        int matchCount = 0;
        for (PatientStudyExtendedData study : studies) {
          CatAssessment assessment = assessmentLoader.assessmentForStudy(study);
          if (assessment == null) {
            logger.error("No assessment for study " + study.getToken());
            continue;
          }

          String responseSignature = assessment.responseSignature();
          if (uniqueResults.containsKey(responseSignature)) {
            String compare = compareResults(uniqueResults.get(responseSignature), assessment);
            if (compare != null) {
              logger.error("Two results in database that do not match: " + compare);
            } else {
              dupCount++;
            }
          } else {
            uniqueResults.put(responseSignature, assessment);
            CatAssessment localScores = new CatAssessment(0, Type.localPromis);
            List<Response> answers = new ArrayList<>();
            for (Step step : assessment.getSteps()) {
              // Use version 2 item bank unless we have version 1 XML
              CatAlgorithm cat = cat2;
              if (study.getSurveySystemId() == version1SystemId) {
                cat = cat1;
              }

              answers.add(cat.bank().item(step.getItemCode()).responses()[step.getResponse()]);
              Score score = cat.score(answers);
              Item next = cat.nextItem(answers);
              if (next == null) {
                localScores.addStep(step.getItemCode(), step.getResponse(), score.theta(), score.standardError()/10, null);
              } else {
                localScores.addStep(step.getItemCode(), step.getResponse(), score.theta(), score.standardError()/10, next.code());
              }
            }
            String compare = compareResults(assessment, localScores);
            if (compare != null) {
              logger.error("Local score does not match (" + study.getToken() + "): " + compare
//                + "\nXML:\n" + study.getContents()
              );
            } else {
              matchCount++;
            }
          }
        }
        System.out.println("***** " + description + " summary: total=" + studies.size() + " unique="
            + (studies.size()-dupCount) + " match=" + matchCount + " "
            + Math.round(matchCount/(double)(studies.size()-dupCount)*100.0) + "% wrong="
            + (studies.size()-dupCount-matchCount) + " "
            + Math.round((studies.size()-dupCount-matchCount)/(double)(studies.size()-dupCount)*100.0) + "%");
      }
    });
  }

  private static String compareResults(CatAssessment expected, CatAssessment actual) {
    String expectedString = toString(expected);
    String actualString = toString(actual);

    if (!expectedString.equals(actualString)) {
      return "expected:\n" + expectedString + "\nactual:\n" + actualString;
    }

    return null;
  }

  private static String toString(CatAssessment scores) {
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (Step score : scores.getSteps()) {
      if (first) {
        first = false;
      } else {
        buf.append("\n");
      }
      buf.append(score.toString());
    }
    return buf.toString();
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
