/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.shc.trauma;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.survey.ExtensibleScoreProvider;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import javax.xml.parsers.ParserConfigurationException;

import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;

/**
 * Simple sum score provider totals the values of the selected answers. Created by tpacht on 06/29/2017.
 */
public class SumScoreProvider extends ExtensibleScoreProvider {
  private static final Logger logger = LoggerFactory.getLogger(SumScoreProvider.class);

  public SumScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {
    return studyName != null && (studyName.startsWith("TQoL") || studyName.startsWith("Centrality"));
  }

  @Override
  public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {

    ArrayList<ChartScore> scores = new ArrayList<>();
    if (patientData == null) {
      return scores;
    }
    try {
      Document doc = ScoreService.getDocument(patientData);
      if (doc == null) {
        return scores;
      }
      Element docElement = doc.getDocumentElement();

      if (docElement.getTagName().equals("Form")) {
        LocalScore chartScore = new LocalScore(patientData.getDtChanged(), patientData.getPatientId(),
            patientData.getStudyCode(), patientData.getStudyDescription());

        NodeList itemsList = doc.getElementsByTagName("Items");
        NodeList itemList;
        if (itemsList != null && itemsList.getLength() > 0) {
          Element itemsNode = (Element) itemsList.item(0);
          itemList = itemsNode.getElementsByTagName("Item");
        } else {
          itemList = doc.getElementsByTagName("Item");
        }

        if (itemList != null) {
          for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
            Element itemNode = (Element) itemList.item(itemInx);
            String scoreAttrib = itemNode.getAttribute("ItemScore");
            String orderAttrib = itemNode.getAttribute("Order");
            chartScore.setAnswer(Integer.parseInt(orderAttrib), new BigDecimal(Integer.parseInt(scoreAttrib)));
          }
        }
        chartScore.setAssisted(patientData.wasAssisted());
        scores.add(chartScore);

      }
    } catch (IOException ioe) {
      logger.error(
          "IOException parsing xml for patient {} study {}",patientData.getPatientId(), patientData.getStudyCode(),
          ioe);
    } catch (ParserConfigurationException pe) {
      logger.error(
          "ParserException parsing xml for patient {} study {}",patientData.getPatientId(),
              patientData.getStudyCode(), pe);
    } catch (SAXException se) {
      logger
          .error(
              "SAXException parsing xml for patient {} study {}",  patientData.getPatientId(),
                  patientData.getStudyCode(), se);
    }

    logger.trace("getScore returning {} scores", scores.size());
    return scores;
  }


  public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient) {

    Table table = new Table();
    PatientStudyExtendedData patientStudyExtendedData = null;
    for (PatientStudyExtendedData patientStudy2 : patientStudies) {
      if (patientStudy2.getStudyCode().intValue() == study.getStudyCode().intValue()) {
        patientStudyExtendedData = patientStudy2;
      }
    }
    if (patientStudyExtendedData != null) {
      ArrayList<ChartScore> scores = getScore(patientStudyExtendedData);
      table.addHeading("Trauma Quality of Life");
      TableRow ghHeadingRow = new TableRow(500);
      ghHeadingRow.addColumn(new TableColumn("Date", 20));
      ghHeadingRow.addColumn(new TableColumn("Score", 20));
      table.addRow(ghHeadingRow);

      for (ChartScore score : scores) {

        TableRow tableRowRow = new TableRow(100);
        tableRowRow.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
        BigDecimal scoreValue = score.getScore();
        tableRowRow.addColumn(new TableColumn(scoreValue.toString(), 20));
        table.addRow(tableRowRow);
      }
    }
    return table;
  }

  public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                        ChartConfigurationOptions opts) {
    XYPlot plot = super.getPlot(renderer, ds, studies, opts);
    plot.getRangeAxis().setRange(0, 50);

    @SuppressWarnings("unchecked")
    Collection<IntervalMarker> markers = plot.getRangeMarkers(Layer.BACKGROUND);

    for (IntervalMarker marker : markers) {
      marker.setLabel(""); // remove the worse label
    }
    return plot;
  }


}
