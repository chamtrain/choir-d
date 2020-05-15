/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.utils;

import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.PromisAnswer;
import edu.stanford.registry.shared.survey.PromisQuestion;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.survey.server.CatAlgorithm.Item;
import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import edu.stanford.survey.server.promis.Bank;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;

public class LocalPromisAssessmentUtils {
  public static final String DESCRIPTION = "Description";
  public static final String[] SURVEY_SYSTEM_NAME = { "0", "LocalPromis" };

  private static Logger logger = Logger.getLogger(PROMISAssessmentUtils.class);


  public LocalPromisAssessmentUtils() {
  }

  public static ArrayList<SurveyQuestionIntf> getSurvey(String xmlDocumentString, ItemBank bank) {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
    try {

      // Using factory get an instance of document builder
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));

      Document messageDom = db.parse(is);

      Element docElement = messageDom.getDocumentElement();

      if (docElement.getTagName().equals("Form")) {

        NodeList itemsNodes = messageDom.getElementsByTagName("Item");

        if (itemsNodes != null && itemsNodes.getLength() > 0) {
          for (int itemInx = 0; itemInx < itemsNodes.getLength(); itemInx++) {
            Element itemNode = (Element) itemsNodes.item(itemInx);
            String itemCode = itemNode.getAttribute("ID");
            Item item = bank.item(itemCode);
            if (item != null) {
              PromisQuestion question = new PromisQuestion();
              PromisAnswer answer = getAnswer(itemNode);
              if (answer != null) {
                question.addAnswer(getAnswer(itemNode));
                if (answer.getSelected()) {
                  question.setAnswered(true);
                }
              }
              String context = item.context();
              if (context != null && context.length() > 0) {
                question.addQuestionLine(item.context());
              }
              String prompt = item.prompt();
              if (prompt != null && prompt.length() > 0) {
                question.addQuestionLine(item.prompt());
              }
              questions.add(question);
            } else {
              logger.warn("No item in bank for " + itemCode + " at index " + itemInx);
            }
          }

        }
      }

    } catch (SAXException | ParserConfigurationException | IOException e) {
      logger.error("Error parsing survey xml " + xmlDocumentString, e);
    }
    return questions;
  }

  public static PromisAnswer getAnswer(Element itemNode) {
    PromisAnswer answer = new PromisAnswer();
    answer.addText(itemNode.getAttribute("ResponseDescription"));
    answer.setClientId(itemNode.getAttribute("ID"));
    answer.setAttribute("Position", itemNode.getAttribute("Position"));
    answer.setAttribute("Value", itemNode.getAttribute("Response"));
    answer.setAttribute("StdError", itemNode.getAttribute("StdError"));
    answer.setAttribute("Theta", itemNode.getAttribute("Theta"));
    answer.setSelected(true);
    answer.setType(Constants.TYPE_SELECT1);
    return answer;
  }

  public static ItemBank itemBankFor(Database database, PatientStudy patStudy) {
    SurveySystDao ssDao = new SurveySystDao(database);
    Study study = ssDao.getStudy(patStudy.getSurveySystemId(), patStudy.getStudyCode());
    return itemBankFor(study);
  }

  public static ItemBank itemBankFor(Study study) {

    if (study == null)
      throw new RuntimeException("Unable to locate item bank for PROMIS study code for null study");

    String description = study.getStudyDescription();
    if (description != null) {
      Bank bank = Bank.byOfficialName(description);
      if (bank != null) {
        return bank.bank();
      }
    }
    throw new RuntimeException("Unable to locate item bank for PROMIS study code " + study.getStudyCode()
                               + " description " + description);
  }
}
