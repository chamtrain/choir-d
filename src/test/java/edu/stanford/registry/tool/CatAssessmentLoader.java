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
package edu.stanford.registry.tool;

import edu.stanford.registry.server.DataTableObjectConverter;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.utils.PROMISItemElementComparator;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.tool.CatAssessment.Step;
import edu.stanford.registry.tool.CatAssessment.Type;
import edu.stanford.survey.server.CatAlgorithm;
import edu.stanford.survey.server.CatAlgorithm.Item;
import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import edu.stanford.survey.server.CatAlgorithm.Response;
import edu.stanford.survey.server.CatAlgorithmPromis;
import edu.stanford.survey.server.promis.PromisAnger;
import edu.stanford.survey.server.promis.PromisAnxiety;
import edu.stanford.survey.server.promis.PromisDepression;
import edu.stanford.survey.server.promis.PromisFatigue;
import edu.stanford.survey.server.promis.PromisPainBehavior;
import edu.stanford.survey.server.promis.PromisPainInterference1;
import edu.stanford.survey.server.promis.PromisPhysicalFunction2;
import edu.stanford.survey.server.promis.PromisSleepDisturbance;
import edu.stanford.survey.server.promis.PromisSleepRelatedImpairment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * Load CAT assessments from the database, dealing with various historical formats as best we can.
 */
public class CatAssessmentLoader {
  private static final Logger logger = Logger.getLogger(CatAssessmentLoader.class);
  private final Supplier<Database> dbp;
  private final Long siteId;

  public CatAssessmentLoader(Supplier<Database> dbp, Long siteId) {
    this.dbp = dbp;
    this.siteId = siteId;
  }

  public List<PatientStudyExtendedData> studiesByStudyCode(int studyCode, boolean excludeTestPatients) {
    String sql = PatStudyDao.SELECT_PAT_STUDY_EXT + "and ps.study_code= ?";

    List<PatientStudyExtendedData> studies = dbp.get()
        .toSelect(sql)
        .argLong(siteId)
        .argInteger(studyCode)
        .query(new RowsHandler<ArrayList<PatientStudyExtendedData>>() {
          @Override
          public ArrayList<PatientStudyExtendedData> process(Rows rs) throws Exception {
            return DataTableObjectConverter.convertToObjects(rs, PatientStudyExtendedData.class);
          }
        });
    List<PatientStudyExtendedData> filteredStudies = new ArrayList<>(studies.size());
    for (PatientStudyExtendedData study : studies) {
      if (excludeTestPatients && study.getLastName().equals("Test-Patient")) {
        continue;
      }
      filteredStudies.add(study);
    }
    return filteredStudies;
  }

  public CatAssessment assessmentForStudy(PatientStudyExtendedData patientData) {
    if (patientData == null) {
      return null;
    }

    Type assessmentType = Type.bySystemName(patientData.getSurveySystemName());
    if (assessmentType == null) {
      throw new RuntimeException("Unknown survey system name: " + patientData.getSurveySystemName());
    }

    try {
      Document doc = ScoreService.getDocument(patientData);
      if (doc == null) {
        return null;
      }
      Element docElement = doc.getDocumentElement();
      if (docElement == null) {
        return null;
      }
      if (docElement.getTagName().equals("Form")) {
        NodeList itemList;
        NodeList itemsList = doc.getElementsByTagName("Items");
        if (itemsList != null && itemsList.getLength() > 0) {
          Element itemsNode = (Element) itemsList.item(0);
          itemList = itemsNode.getElementsByTagName("Item");
        } else {
          itemList = doc.getElementsByTagName("Item");
        }

        if (itemList == null || itemList.getLength() == 0) {
          return null;
        }

        List<Element> sortedItems;
        if (assessmentType == Type.assessmentCenter1) {
          // Version 1 of the Assessment Center API did not provide any indication of item order
          ItemBank itemBank;
          if (patientData.getSurveySystemId() == 1007) {
            itemBank = PromisDepression.bank();
          } else if (patientData.getSurveySystemId() == 1044) {
            itemBank = PromisAnger.bank();
          } else if (patientData.getSurveySystemId() == 1008) {
            itemBank = PromisAnxiety.bank();
          } else if (patientData.getSurveySystemId() == 1003) {
            itemBank = PromisPainInterference1.bank();
          } else if (patientData.getSurveySystemId() == 1004) {
            itemBank = PromisPainBehavior.bank();
          } else if (patientData.getSurveySystemId() == 1005) {
            itemBank = PromisPhysicalFunction2.bank();
          } else if (patientData.getSurveySystemId() == 1042) {
            itemBank = PromisSleepDisturbance.bank();
          } else if (patientData.getSurveySystemId() == 1006) {
            itemBank = PromisFatigue.bank();
          } else if (patientData.getSurveySystemId() == 1043) {
            itemBank = PromisSleepRelatedImpairment.bank();
          } else {
            throw new RuntimeException("Don't know how to handle survey system " + patientData.getSurveySystemId());
          }
          sortedItems = sortItemsUsingCat(itemList, new CatAlgorithmPromis().initialize(itemBank));
        } else {
          sortedItems = sortItemsUsingPosition(itemList);
        }

        double initialTheta = 0;
//        if (assessmentType == Type.cat2) {
//          initialTheta = ...; // If we eventually store assessments done with priors included we will deal with it
//        }
        CatAssessment assessment = new CatAssessment(initialTheta, assessmentType);
        Step last = null;
        for (Element itemNode : sortedItems) {
          String itemCode = itemNode.getAttribute("ID");

          if (last != null) {
            last.setNextItemCode(itemCode);
          }

          Integer response = null;
          if (itemNode.getAttribute("Response") != null && itemNode.getAttribute("Response").length() > 0) {
            response = Integer.parseInt(itemNode.getAttribute("Response")) - 1;
          } else {
            String oid = itemNode.getAttribute("ItemResponseOID");
            NodeList mapNodes = itemNode.getElementsByTagName("Map");
            for (int i = 0; i < mapNodes.getLength(); i++) {
              Element mapNode = (Element) mapNodes.item(i);
              if (oid.equals(mapNode.getAttribute("ItemResponseOID"))) {
                response = Integer.parseInt(mapNode.getAttribute("Value")) - 1;
              }
            }
          }
          double theta = Double.parseDouble(itemNode.getAttribute("Theta"));
          double stdErr = Double.parseDouble(itemNode.getAttribute("StdError"));

          last = assessment.addStep(itemCode, response, theta, stdErr, null);
        }
        return assessment;
      }
    } catch (Exception e) {
      logger.error(
          "IOException parsing xml for patient " + patientData.getPatientId() + " study " + patientData.getStudyCode(),
          e);
    }
    return null;
  }

  /**
   * This is used for XML returned from Northwestern by the version 2 API. This XML contains
   * a Position attribute on the item indicated the order questions were administered (1..n).
   */
  private static ArrayList<Element> sortItemsUsingPosition(NodeList itemList) {
    ArrayList<Element> sortedList = new ArrayList<>();
    for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
      Element itemNode = (Element) itemList.item(itemInx);
      sortedList.add(itemNode);
    }
    Collections.sort(sortedList, new PROMISItemElementComparator<Element>());
    return sortedList;
  }

  /**
   * This is used for XML returned from Northwestern by the version 1 API. There is no way by
   * inspecting the XML to determine the order questions were administered. Here we "cheat"
   * by using the local CAT to walk through a new assessment using the same answers provided
   * in the XML, for the purpose of figuring out the sequence of questions. If anything goes
   * wrong, fallback to sorting by descending standard error.
   */
  private static List<Element> sortItemsUsingCat(NodeList itemList, CatAlgorithm cat) {
    Map<String, Element> itemCodeToItem = new HashMap<>();
    for (int i = 0; i < itemList.getLength(); i++) {
      Element itemNode = (Element) itemList.item(i);
      itemCodeToItem.put(itemNode.getAttribute("ID"), itemNode);
    }

    List<Element> sortedItemNodes = new ArrayList<>();
    List<Response> answers = new ArrayList<>();
    Item next = cat.nextItem(answers);
    while (next != null && itemCodeToItem.containsKey(next.code())) {
      Element itemNode = itemCodeToItem.remove(next.code());
      sortedItemNodes.add(itemNode);
      int response = Integer.parseInt(itemNode.getAttribute("Response")) - 1;
      answers.add(cat.bank().response(next.code(), next.responses()[response].text()));
      next = cat.nextItem(answers);
    }

    if (itemCodeToItem.size() > 0) {
      List<Element> extraItemNodes = new ArrayList<>();
      extraItemNodes.addAll(itemCodeToItem.values());
      Collections.sort(extraItemNodes, new PROMISItemElementComparator<Element>());
      sortedItemNodes.addAll(extraItemNodes);
    }
    return sortedItemNodes;
  }
}
