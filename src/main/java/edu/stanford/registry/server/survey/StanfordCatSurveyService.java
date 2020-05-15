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

package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.utils.LocalPromisAssessmentUtils;
import edu.stanford.registry.server.utils.PROMISItemElementComparator;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.server.Answer;
import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import edu.stanford.survey.server.CatAlgorithm.Response;
import edu.stanford.survey.server.CatAlgorithm.Score;
import edu.stanford.survey.server.Question;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveySystemStanfordCat;
import edu.stanford.survey.server.promis.Bank;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseException;

public class StanfordCatSurveyService extends SurveySiteBase {
  private static final Logger log = LoggerFactory.getLogger(StanfordCatSurveyService.class);
  public static final String STANFORD_CAT_NO_SKIP = "StanfordCat";
  public static final String STANFORD_CAT_ALLOW_SKIP = "StanfordCatAllowSkip";
  private final boolean allowSkipping;
  private int version = 1;
  private final String systemName;

  public StanfordCatSurveyService(SiteInfo siteInfo, boolean allowSkipping) {
    super(siteInfo);
    this.allowSkipping = allowSkipping;
    if (allowSkipping) {
      systemName = STANFORD_CAT_ALLOW_SKIP;
    } else {
      systemName = STANFORD_CAT_NO_SKIP;
    }
  }

  private PatientStudy getPatientStudyInProgress(final Database database, final PatientStudy patStudy) {
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    return patStudyDao.getPatientStudy(patStudy.getPatientId(), patStudy.getStudyCode(), patStudy.getToken(), false);
  }

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    if (name == null) {
      return null;
    }
    if (title == null) {
      title = name;
    }
    if (explanation == null) {
      explanation = "";
    }

    SurveySystDao ssDao = new SurveySystDao(database);
    SurveySystem surveySystem = ssDao.getSurveySystem(systemName);
    Study study = new Study(surveySystem.getSurveySystemId(), 0, name, 0);
    study.setTitle(title);
    study.setExplanation(explanation);
    study = ssDao.insertStudy(study);
    return study;
  }

  private PatientStudy registerAssessment(Database database, Element questionaire, String patientId, Token tok,
                                          Integer order) throws ServiceUnavailableException, FileNotFoundException {
    String studyName = questionaire.getAttribute("value");
    PatientStudy patStudy = new PatientStudy(siteId);

    int systemId;
    try {
      SurveySystem surveySystem = new SurveySystDao(database).getSurveySystem(systemName);
      systemId = surveySystem.getSurveySystemId();
    } catch (Exception e) {
      log.error("ERROR getting promisId: {}" , e.getMessage(), e);
      throw new IllegalArgumentException("Failed to initialize promis survey system");
    }

    // lets make sure the assessment is in the study table
    // and if not we'll add it
    SurveySystDao ssDao = new SurveySystDao(database);
    Study study = ssDao.getStudy(systemId, studyName);
    if (study == null) {
      log.debug("Did not find study {} adding", studyName);

      study = registerAssessment(database, studyName, studyName, null);
    }

    patStudy.setMetaVersion(0);
    patStudy.setPatientId(patientId);
    patStudy.setStudyCode(study.getStudyCode());
    patStudy.setSurveySystemId(study.getSurveySystemId());
    patStudy.setToken(tok.getToken());
    patStudy.setOrderNumber(order);

    // see if it already exists and isn't complete yet
    PatientStudy patientfound = getPatientStudyInProgress(database, patStudy);

    if (patientfound == null) {
      log.trace("patient registration not found writing to db");
      PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
      try {
        patStudy = patStudyDao.insertPatientStudy(patStudy);
      } catch (DatabaseException e) {
        log.error(e.getMessage(), e);
        throw new ServiceUnavailableException(e.getMessage());
      }
    } else {
      patStudy = patientfound;
    }

    return patStudy;
  }

  @Override
  public void registerAssessment(Database database, Element questionaire, String patientId,
                                 Token tok, User user) throws ServiceUnavailableException {

    String studyName = questionaire.getAttribute("value");
    String qOrder = questionaire.getAttribute("order");
    Integer order = Integer.valueOf(qOrder);
    try {
      PatientStudy patStudy = registerAssessment(database, questionaire, patientId, tok, order);
      PatientStudyExtendedData patStudyE = new PatientStudyExtendedData(patStudy);
      PatientDao patientDao = new PatientDao(database, siteId, user);
      Patient pat = patientDao.getPatient(patStudy.getPatientId());
      if (pat != null) {
        patStudyE.setPatient(pat);
      }
      SurveySystDao ssDao = new SurveySystDao(database);
      Study study = ssDao.getStudy(patStudy.getSurveySystemId(), patStudy.getStudyCode());
      patStudyE.setStudy(study);
      patStudyE.setSurveySystemName(systemName);
    } catch (FileNotFoundException fnf) {
      log.error("Failed to register assessment {} for patient", studyName);
      throw new ServiceUnavailableException("Failed to register assessment " + studyName + " for patient " + patientId);
    }
  }


  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudy, SubmitStatus submitStatus,
                                     String answerJson) {
    // Figure out which item bank we are dealing with
    Bank bank = Bank.byOfficialName(patStudy.getStudyDescription());
    ItemBank itemBank = bank.bank();

    // Extract the current response, if there is one
    Response currentResponse = null;
    if (answerJson != null) {
      Answer answer = new Answer(null, submitStatus, answerJson);
      String itemCode = answer.questionId();
      String choice = answer.formFieldValue("response");
      if (choice != null) {
        currentResponse = itemBank.response(itemCode, choice);
      }
    }

    // Fetch any prior responses, if there are any
    List<Response> priorResponses = new ArrayList<>();
    try {
      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc;
      if (patStudy.getContents() == null) {
        // The patStudy passed in may be lazy and not load the contents
        PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
        PatientStudy study = patStudyDao.getPatientStudy(patStudy, true);
        patStudy.setPatientStudyId(study.getPatientStudyId());
        if (study.getContents() == null) {
          doc = db.newDocument();
        } else {
          doc = db.parse(new InputSource(new StringReader(study.getContents())));
        }
      } else {
        doc = db.parse(new InputSource(new StringReader(patStudy.getContents())));
      }

      Element formElement = doc.getDocumentElement();
      if (formElement == null) {
        formElement = doc.createElement("Form");
        doc.appendChild(formElement);
      }

      Element itemsElement;
      NodeList itemsList = formElement.getElementsByTagName("Items");
      if (itemsList != null && itemsList.getLength() > 0) {
        itemsElement = (Element) itemsList.item(0);
      } else {
        itemsElement = doc.createElement("Items");
        formElement.appendChild(itemsElement);
      }

      // <Item Position="1" ID="EDDEP06" Response="3" ResponseDescription="Sometimes"
      //       Theta="1.75839221167403" StdError="0.237197910725796" Next="EDDEP07"/>
      List<Element> sortedItems = sortItemsUsingPosition(itemsElement.getElementsByTagName("Item"));
      String nextItemCode = formElement.getAttribute("First");
      for (Element itemNode : sortedItems) {
        nextItemCode = itemNode.getAttribute("Next");

        String itemCode = itemNode.getAttribute("ID");
        int response = Integer.parseInt(itemNode.getAttribute("Response")) - 1;

        priorResponses.add(itemBank.item(itemCode).responses()[response]);

        if (currentResponse != null && itemCode.equals(currentResponse.item().code())) {
          log.warn("Received response {} twice, ignoring last", itemCode);
          currentResponse = null;
        }
      }

      if (nextItemCode != null && nextItemCode.length() > 0
          && currentResponse != null && !nextItemCode.equals(currentResponse.item().code())) {
        log.error("Received response {} but expecting {}", currentResponse.item().code(), nextItemCode);
        currentResponse = null;
      }

      SurveySystemStanfordCat cat = new SurveySystemStanfordCat(bank, allowSkipping,
          Integer.toString(patStudy.getSurveySystemId()), Integer.toString(patStudy.getStudyCode()));

      Answer answer = null;
      if (answerJson != null) {
        answer = new Answer(null, submitStatus, answerJson);
      }
      Question q = cat.nextQuestion(answer, new SurveyQuery(database, new SurveyDao(database),
          patStudy.getSurveySiteId()).surveyBySurveyToken(patStudy.getToken()));

      Element newItemElement = null;
      if (currentResponse != null) {
        newItemElement = doc.createElement("Item");
        itemsElement.appendChild(newItemElement);
        newItemElement.setAttribute("Position", Integer.toString(sortedItems.size() + 1));
        newItemElement.setAttribute("ID", currentResponse.item().code());
        newItemElement.setAttribute("Response", Integer.toString(currentResponse.index() + 1));
        newItemElement.setAttribute("ResponseDescription", currentResponse.text());

        priorResponses.add(currentResponse);
        Score score = cat.score();

        newItemElement.setAttribute("Theta", Double.toString(score.theta()));
        newItemElement.setAttribute("StdError", Double.toString(score.standardError() / 10));
      }

      NextQuestion next = null;
      if (q != null) {
        if (priorResponses.isEmpty()) {
          formElement.setAttribute("First", q.getDisplayStatus().getQuestionId());
        }

        if (newItemElement != null) {
          newItemElement.setAttribute("Next", q.getDisplayStatus().getQuestionId());
        }

        next = new NextQuestion();
        next.setDisplayStatus(q.getDisplayStatus());
        next.setQuestion(q.getQuestion());
      }

      Transformer trans = TransformerFactory.newInstance().newTransformer();
      trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      trans.setOutputProperty(OutputKeys.INDENT, "yes");
      StringWriter strWriter = new StringWriter();
      trans.transform(new DOMSource(doc), new StreamResult(strWriter));

      patStudy.setContents(strWriter.toString());
      PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
      patStudyDao.setPatientStudyContents(patStudy,  (next == null));
      return next;
    } catch (Exception e) {
      log.error("Error parsing xml for token {} study {}", patStudy.getToken(), patStudy.getStudyCode(), e);
      return null;
    }
  }

  private static ArrayList<Element> sortItemsUsingPosition(NodeList itemList) {
    ArrayList<Element> sortedList = new ArrayList<>();
    if (itemList != null) {
      for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
        Element itemNode = (Element) itemList.item(itemInx);
        sortedList.add(itemNode);
      }
      Collections.sort(sortedList, new PROMISItemElementComparator<Element>());
    }
    return sortedList;
  }

  @Override
  public String getAssessments(Database database, int version) throws ServiceUnavailableException,
      FileNotFoundException {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, final String studyName) {
    return new PromisScoreProvider(siteInfo, version) {
      @Override
      public ArrayList<SurveyQuestionIntf> getSurvey(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study,
                                                     Patient patient, boolean allAnswers) {

        ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
        if (patientStudies == null || study == null || patient == null) {
          return questions;
        }
        PatientStudyExtendedData patStudy = null;
        for (PatientStudyExtendedData patientStudy : patientStudies) {
          if (patientStudy.getStudyCode().intValue() == study.getStudyCode().intValue()) {
            patStudy = patientStudy;
          }
        }
        if (patStudy == null) {
          return questions;
        }
        String xmlDocumentString = patStudy.getContents();
        if (xmlDocumentString == null || xmlDocumentString.length() < 1) {
          return questions;
        }

        return LocalPromisAssessmentUtils.getSurvey(xmlDocumentString, Bank.byOfficialName(studyName).bank());
      }
    };
  }

  @Override
  public void setVersion(int vs) {
    version = vs;
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(Database database, PatientStudy study, User user) {
    throw new RuntimeException("Not implemented");
  }
}
