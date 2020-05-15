/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.shc.psychiatry;

import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.RegistryQuestion;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.xform.Select1Element;
import edu.stanford.registry.shared.xform.SelectItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ScoreToAnswerAdapter {

  private final static Logger logger = LoggerFactory.getLogger(ScoreToAnswerAdapter.class);

  public static ArrayList<SurveyQuestionIntf> transform(PatientStudyExtendedData patStudy, List<String> refs) {
    Document doc;
    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
    try {
      doc = ScoreService.getDocument(patStudy);
      if (doc == null) {
        return questions;
      }
      NodeList itemList = doc.getElementsByTagName(Constants.ITEM);
      if (itemList == null || itemList.getLength() == 0) {
        return questions;
      }

      for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
        Element itemNode = (Element) itemList.item(itemInx);
        RegistryQuestion question = RegistryAssessmentUtils.getQuestion(itemNode, itemInx, true);

        if (question.getAnswers().size() > 0 && question.getAnswered()) {
          ArrayList<SurveyAnswerIntf> answers = new ArrayList<>();
          boolean adapt = false;
          for (SurveyAnswerIntf answerIntf : question.getAnswers()) {
            String ref = answerIntf.getAttribute("ref");

            for (String r : refs) {
              if (r.equalsIgnoreCase(ref)) {
                if (answerIntf instanceof Select1Element) {
                  Select1Element input = (Select1Element) answerIntf;
                  adapt = true;
                  for (SelectItem item : input.getItems()) {
                    item.setLabel(item.getValue());
                  }
                  answers.add(input);
                }
              }
            }
          }
          if (adapt) {
            question.setAnswers(answers);
          }
          questions.add(question);
        }
      }
      return questions;
    } catch (ParserConfigurationException | SAXException | IOException e) {
      logger.error("Error transforming answers for patientStudy token {} study {}", patStudy.getToken(),
          patStudy.getStudyCode(), e);
    }
    return questions;
  }
}
