package edu.stanford.registry.tool;

import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.tool.CatAssessment.Step;
import edu.stanford.survey.server.CatAlgorithm.Item;
import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import edu.stanford.survey.server.CatAlgorithm.Response;
import edu.stanford.survey.server.promis.PromisSleepDisturbance;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * The purpose of this tool is to take two items from a bank and examine the
 * distribution of answers to one based on answers to the other.
 */
public class ItemCorrelationBacktester extends Tool {
  private static final Logger logger = Logger.getLogger(ItemCorrelationBacktester.class);

  private static void checkStudies() {
//    checkStudies("Depression", 1007, PromisDepression2.bank(), ...);
//    checkStudies("Anger", 1044, PromisAnger.bank(), ...);
//    checkStudies("Anxiety", 1008, PromisAnxiety2.bank(), ...);
//    checkStudies("Pain Intensity", 1003, PromisPainIntensity.bank(), ...);
//    checkStudies("Pain Behavior", 1004, PromisPainBehavior.bank(), ...);
//    checkStudies("Physical Function", 1005, PromisPhysicalFunction.bank(), "PFC7", "PFC33");
    checkStudies("Sleep Disturbance", 1042, PromisSleepDisturbance.bank(), "Sleep72", "Sleep44", 1L);
//    checkStudies("Fatigue", 1006, PromisFatigue2.bank(), ...);
//    checkStudies("Sleep Disturbance", 1042, PromisSleepDisturbance.bank(), ...);
//    checkStudies("Sleep-Related Impairment", 1043, PromisSleepRelatedImpairment.bank(), ...);
  }

  private static void checkStudies(final String description, final int studyCode, final ItemBank itemBank,
                                   final String itemCode1, final String itemCode2, final Long siteId) {
    runTransaction("admin", new ServerRunnable() {
      @Override
      public void run() {
        CatAssessmentLoader assessmentLoader = new CatAssessmentLoader(database, siteId);
        List<PatientStudyExtendedData> studies = assessmentLoader.studiesByStudyCode(studyCode, true);
        System.out.println("***** " + description + " surveys: " + studies.size());

        Item item1 = itemBank.item(itemCode1);
        Item item2 = itemBank.item(itemCode2);
        ResponseToCounts[] item1Counts = ResponseToCounts.fromItems(item1, item2);
        ResponseToCounts[] item2Counts = ResponseToCounts.fromItems(item2, item1);
        int item1FirstCount = 0;
        int item2FirstCount = 0;
        for (PatientStudyExtendedData study : studies) {
          CatAssessment assessment = assessmentLoader.assessmentForStudy(study);
          if (assessment == null) {
            logger.error("No assessment for study " + study.getToken());
            continue;
          }

          Step first = null;
          Step second = null;
          for (Step step : assessment.getSteps()) {
            if (step.getItemCode().equals(item1.code()) || step.getItemCode().equals(item2.code())) {
              if (first == null) {
                first = step;
              } else {
                second = step;
                break;
              }
            }
          }

          if (first != null && second != null) {
            if (first.getItemCode().equals(item1.code())) {
              item1Counts[first.getResponse()].increment();
              item1Counts[first.getResponse()].counts[second.getResponse()].increment();
              item1FirstCount++;
            } else {
              item2Counts[second.getResponse()].increment();
              item2Counts[second.getResponse()].counts[first.getResponse()].increment();
              item2FirstCount++;
            }
          }
        }

        StringBuilder buf = new StringBuilder();

        buf.append(item1FirstCount).append(" ").append(item1Counts[0].response.item().prompt()).append(" (")
            .append(item1Counts[0].response.item().code()).append(")\n    --> ");
        buf.append(item1Counts[0].counts[0].response.item().prompt()).append(" (")
            .append(item1Counts[0].counts[0].response.item().code()).append(")\n\n");
        for (ResponseToCounts rtc : item1Counts) {
          buf.append("    ").append(rtc.count).append(" ").append(rtc.response.text()).append("\n");
          for (ResponseCount rc : rtc.counts) {
            buf.append("        --> ").append(rc.count).append(" ").append(rc.response.text()).append("\n");
          }
        }

        buf.append("\n\n");
        buf.append(item2FirstCount).append(" ").append(item2Counts[0].response.item().prompt()).append(" (")
            .append(item2Counts[0].response.item().code()).append(")\n    --> ");
        buf.append(item2Counts[0].counts[0].response.item().prompt()).append(" (")
            .append(item2Counts[0].counts[0].response.item().code()).append(")\n\n");
        for (ResponseToCounts rtc : item2Counts) {
          buf.append("    ").append(rtc.count).append(" ").append(rtc.response.text()).append("\n");
          for (ResponseCount rc : rtc.counts) {
            buf.append("        --> ").append(rc.count).append(" ").append(rc.response.text()).append("\n");
          }
        }

        System.out.println(buf.toString());
      }
    });
  }

  private static class ResponseCount {
    Response response;
    int count;

    ResponseCount(Response response) {
      this.response = response;
    }

    static ResponseCount[] fromItem(Item item) {
      ResponseCount[] result = new ResponseCount[item.responses().length];

      for (int i = 0; i < item.responses().length; i++) {
        result[i] = new ResponseCount(item.responses()[i]);
      }

      return result;
    }

    void increment() {
      count++;
    }
  }

  private static class ResponseToCounts {
    Response response;
    ResponseCount[] counts;
    int count;

    ResponseToCounts(Response response, ResponseCount[] counts) {
      this.response = response;
      this.counts = counts;
    }

    void increment() {
      count++;
    }

    static ResponseToCounts[] fromItems(Item item1, Item item2) {
      ResponseToCounts[] result = new ResponseToCounts[item1.responses().length];

      for (int i = 0; i < item1.responses().length; i++) {
        result[i] = new ResponseToCounts(item1.responses()[i], ResponseCount.fromItem(item2));
      }

      return result;
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
