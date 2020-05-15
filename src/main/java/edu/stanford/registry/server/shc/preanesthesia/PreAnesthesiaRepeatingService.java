package edu.stanford.registry.server.shc.preanesthesia;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.RepeatingSurveyService;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.RegistryQuestion;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.xform.InputElement;
import edu.stanford.registry.shared.xform.Select1Element;
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Supplier;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;

/**
 * Created by tpacht on 11/9/2015.
 */
public class PreAnesthesiaRepeatingService extends RepeatingSurveyService {
  private static final Logger logger = Logger.getLogger(PreAnesthesiaRepeatingService.class);
  private SurgeryScoreProvider surgeryScoreProvider = null;
  private final static String INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;";

  public PreAnesthesiaRepeatingService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  /**
   * Return the appropriate score provider based on the study name.
   */
  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    if (studyName.equals("pacSurgeries")) {
      if (surgeryScoreProvider == null) {
        surgeryScoreProvider = new SurgeryScoreProvider(siteInfo);
      }
      return surgeryScoreProvider;
    }
    return this;
  }

  private static class SurgeryScoreProvider extends RepeatingSurveyService {
    public SurgeryScoreProvider(SiteInfo siteInfo) {
      super(siteInfo);
    }

    @Override
    public ArrayList<SurveyQuestionIntf> getSurvey(
        PatientStudyExtendedData patStudy, PrintStudy study,
        Patient patient, boolean allAnswers) {

      Document doc;
      logger.debug("Getting survey " + study.getStudyDescription());
      ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
      try {
        doc = ScoreService.getDocument(patStudy);
        NodeList itemList = doc.getElementsByTagName(Constants.ITEM);
        if (itemList == null || itemList.getLength() == 0) {
          return questions;
        }
        for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
          Element itemNode = (Element) itemList.item(itemInx);
          RegistryQuestion question = RegistryAssessmentUtils.getQuestion(itemNode, itemInx, allAnswers);
          if (question.getAttribute("print") == null || "true".equals(question.getAttribute("print").toLowerCase())) {
            ArrayList<SurveyAnswerIntf> answers = question.getAnswers(true);
            ArrayList<SurveyAnswerIntf> newAnswers = new ArrayList<>();
            if (answers != null) {
              for (SurveyAnswerIntf answerIntf : answers) {
                String ref = answerIntf.getAttribute("ref");
                if (ref != null) {
                  if ("SURGTYPE".equals(ref)) {
                    questions.add(question);
                  } else if ( "SURGYEAR".equals(ref)) {
                    ArrayList<String> text = question.getText();
                    text.set(0, INDENT + text.get(0));
                    questions.add(question);
                  } else if ("SURGANESTHGEN".equals(ref)
                      || "SURGANESTHNB".equals(ref)  || "SURGANESTHIV".equals(ref)
                      || "SURGANESTHNS".equals(ref) || "SURGANESTHOTH".equals(ref)
                      ) {
                    question.getText().clear();
                    if ("SURGANESTHGEN".equals(ref)) {
                      question.addText(INDENT + "Anesthetic given: General anesthesia");
                    } else {
                      SelectElement input = (SelectElement) answerIntf;
                      for (SelectItem item : input.getItems()) {
                        question.addText(INDENT + "Anesthetic given: " + item.getLabel());
                      }
                    }
                    RegistryAnswer ans = new RegistryAnswer();
                    ans.setType(Constants.TYPE_RADIO);
                    ans.setSelected(true);
                    ans.setAttribute(Constants.DESCRIPTION, "Yes");
                    ans.setValue("1");
                    newAnswers.add(ans);
                    question.setAnswers(newAnswers);
                    questions.add(question);
                  } else if ("SURGANESTHGENPROB".equals(ref)||
                      "SURGANESTHNBPROB".equals(ref) || "SURGANESTHIVPROB".equals(ref)
                      || "SURGANESTHNSPROB".equals(ref) ||"SURGANESTHOTHPROB".equals(ref)) {
                    question.getText().clear();
                    question.addText(INDENT + "Problems or side effects?");
                    if (Constants.TYPE_SELECT1 == answerIntf.getType()) {
                      Select1Element select1 = (Select1Element) answerIntf;
                      for (SelectItem item : select1.getItems()) {
                        if (item.getSelected()) {
                          RegistryAnswer ans = new RegistryAnswer();
                          ans.setType(Constants.TYPE_RADIO);
                          ans.setSelected(item.getSelected());
                          ans.setAttribute(Constants.DESCRIPTION, item.getLabel());
                          ans.setValue(item.getValue());
                          newAnswers.add(ans);
                        }
                      }
                    }
                    question.setAnswers(newAnswers);
                    questions.add(question);
                  } else if ("SURGANESTHGENPRSEL".equals(ref)) {
                    RegistryQuestion problemsQuestion =  (RegistryQuestion) PreAnesthesiaServiceUtils.getQuestion(questions,
                        Integer.parseInt(question.getAttribute(Constants.ORDER)) - 1);
                    if (problemsQuestion != null &&  problemsQuestion.getAnswers() != null &&
                        problemsQuestion.getAnswers().size() > 0 ) { //&& problemsQuestion.getAnswer(0).getType() == Constants.TYPE_RADIO) {
                      for (SurveyAnswerIntf problemAns : problemsQuestion.getAnswers()) {
                        if (problemAns.getType() == Constants.TYPE_RADIO) {
                          RegistryAnswer regAns = (RegistryAnswer) problemAns;
                          if ("Yes".equals(regAns.getAttribute(Constants.DESCRIPTION))) {
                            question.setAnswered(true);
                            SelectElement ansGenAnsSide = (SelectElement) answerIntf;
                            PreAnesthesiaServiceUtils.selectAll(ansGenAnsSide);
                          }
                          if (question.getAnswered() == true) {
                            question.addText(" - ");
                            questions.add(question);
                          }
                        }
                      }
                    }
                  } else if ("SURGANESTHGENPROBEXP".equals(ref) && answerIntf.getType() == Constants.TYPE_INPUT) {
                      InputElement inputAnswer = (InputElement) answerIntf;

                      for (String txt : answerIntf.getResponse()) {
                        inputAnswer.addText(txt);
                      }
                    question.getText().clear();
                    question.addText(INDENT + inputAnswer.getLabel());
                      if (answers.size() < 2) { // no checkboxes

                        newAnswers.add(inputAnswer);
                        question.setAnswers(newAnswers);
                        question.setAnswered(true);
                        questions.add(question);
                      } else {
                        // nothing. it'll get listed with the checkboxes
                      }
                      inputAnswer.setLabel("");
                    } else if ("SURGANESTHNBPROBEXP".equals(ref) || "SURGANESTHIVPROBEXP".equals(ref)
                      || "SURGANESTHNSPROBEXP".equals(ref) || "SURGANESTHOTHEXP".equals(ref)
                      || "SURGANESTHOTHPROBEXP".equals(ref)  ) {
                    if (answerIntf.getType() == Constants.TYPE_INPUT) {
                      question.getText().clear();
                      InputElement inputAnswer = (InputElement) answerIntf;
                      question.addText(INDENT + inputAnswer.getLabel());
                      inputAnswer.setLabel("");
                      for (String txt : answerIntf.getResponse()) {
                        inputAnswer.addText(txt);
                      }
                      newAnswers.add(inputAnswer);
                      question.setAnswers(newAnswers);
                      questions.add(question);
                    }
                  }
                }
              }
            }
          }
        }
      } catch (ParserConfigurationException  | SAXException | IOException e) {
        logger.error(
            "Error getting questions for patientStudy token " + patStudy.getToken() + " study "
                + patStudy.getStudyCode(), e);
      }
      if (questions.size() < 1) {
        RegistryQuestion question = new RegistryQuestion();
        question.addText("Past Surgeries not performed at Stanford");
        question.setAnswered(true);
        RegistryAnswer noAnswer = new RegistryAnswer();
        noAnswer.setType(Constants.TYPE_RADIO);
        noAnswer.setSelected(true);
        noAnswer.setAttribute(Constants.DESCRIPTION, "-");
        noAnswer.setValue("1");
        ArrayList<SurveyAnswerIntf> answers = new ArrayList<>();
        answers.add(noAnswer);
        question.setAnswers(answers);
        questions.add(question);
      }
      return questions;
    }
  }

}
