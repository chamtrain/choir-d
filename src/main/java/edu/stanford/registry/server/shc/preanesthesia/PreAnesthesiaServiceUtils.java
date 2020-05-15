package edu.stanford.registry.server.shc.preanesthesia;

import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.RegistryQuestion;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;

import java.util.ArrayList;

/**
 * Created by tpacht on 5/26/2016.
 */
public class PreAnesthesiaServiceUtils {

  public static void selectAll(SelectElement selectElement) {
    selectElement.setAttribute("checks", "true");
    for (SelectItem item : selectElement.getItems()) {
      if (item.getSelected()) {
        item.setLabel("<b>X " + item.getLabel() + "</b>");
      } else {
        //item.setLabel("&nbsp;&nbsp; " + item.getLabel() );
        item.setLabel(item.getLabel());
        item.setSelected(true);
      }
    }
  }
  public static SurveyQuestionIntf getQuestion(ArrayList<SurveyQuestionIntf> questions, int number) {
    RegistryQuestion returnQuestion = null;
    if (questions != null) {
      for (SurveyQuestionIntf question : questions) {
        if (question.getAttribute(Constants.ORDER) != null) {
          try {
            int orderNumber = Integer.parseInt(question.getAttribute(Constants.ORDER));
            if (orderNumber == number) {
              return question;
            }
          } catch (NumberFormatException nfe) {
          }
        }
      }
    }
    return returnQuestion;
  }
}
