/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
import edu.stanford.registry.shared.xform.InputElement;
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.github.susom.database.Database;

/**
 * Extensible ScoreProvider implementation
 */
public class ExtensibleScoreProvider extends RegistryShortFormScoreProvider implements ScoreProvider {
  private static Logger logger = Logger.getLogger(ExtensibleScoreProvider.class);
  protected final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yy");
  protected final DecimalFormat scoreFormatter = new DecimalFormat("####");

  final protected String studyName;

  public ExtensibleScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo);
    this.studyName = studyName;
  }

  @Override
  public String getDescription() {
    return studyName + " Score Provider";
  }

  @Override
  public boolean acceptsSurveyName(String studyName) {
    return this.studyName.equals(studyName);
  }

  @Override
  public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
    ArrayList<ChartScore> result = new ArrayList<>();
    ChartScore chartScore = getScoreInternal(patientData);
    result.add(chartScore);
    return result;
  }

  @Override
  public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies,
      PrintStudy study, Patient patient) {
    ArrayList<ChartScore> scores = new ArrayList<>();
    for(PatientStudyExtendedData patientStudy : patientStudies) {
      if (patientStudy.getStudyCode().equals(study.getStudyCode())) {
        ChartScore score = getScoreInternal(patientStudy);
        if (score != null) {
          scores.add(score);
        }
      }
    }
    return getTableInternal(study, scores, false);
  }

  @Override
  public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> stats,
      PrintStudy study, ChartConfigurationOptions opts) {
    TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(baseLineSeries);
    if (stats == null || study == null) {
      return dataset;
    }
    TimeSeries timeDataSet = new TimeSeries(study.getTitle());
    for (ChartScore stat : stats) {
      ChartScore chartScore = stat;
      BigDecimal score = chartScore.getScore();
      Day day = new Day(chartScore.getDate());
      // TODO: Handle inverted
      // TODO: Handle OPTION_CHART_PERCENTILES
      try {
        if (score != null) {
          timeDataSet.addOrUpdate(day, score);
        }
      } catch (SeriesException duplicates) {
        // Ignore 
      }
    }
    dataset.addSeries(timeDataSet);
    return dataset;
  }

  @Override
  public String formatExplanationText(Study study, ArrayList<ChartScore> scores) {
    return study.getExplanation();
  }

  @Override
  public int getReportTextFontSize(PrintStudy study) {
    final int defaultFontSize = 11;
    return defaultFontSize;
  }

  protected ChartScore getScoreInternal(PatientStudyExtendedData patientData) {
    LocalScore localScore = new LocalScore(patientData.getDtChanged(), patientData.getPatientId(),
        patientData.getStudyCode(), patientData.getStudyDescription());
    localScore.setAssisted(patientData.wasAssisted());

    loadAnswers(localScore, patientData);
    calculateScore(localScore, patientData);

    return localScore;
  }

  protected void loadAnswers(LocalScore localScore, PatientStudyExtendedData patientData) {
    Document doc = null;
    try {
      doc = ScoreService.getDocument(patientData);
    } catch (Exception e) {
      logger.error("Exception parsing xml for patient " + patientData.getPatientId() + " study " + patientData.getStudyCode(), e);
    }
    if (doc == null) {
      return;
    }

    Element docElement = doc.getDocumentElement();
    if (docElement.getTagName().equals(Constants.FORM)) {
      NodeList itemsList = doc.getElementsByTagName(Constants.ITEMS);
      if (itemsList != null && itemsList.getLength() > 0) {
        Element itemsNode = (Element) itemsList.item(0);
        NodeList itemList = itemsNode.getElementsByTagName(Constants.ITEM);
        if (itemList != null) {
          for (int itemIndex = 0; itemIndex < itemList.getLength(); itemIndex++) {
            Element itemNode = (Element) itemList.item(itemIndex);
            loadAnswer(localScore, itemNode, itemIndex);
          }
        }
      }
    }
  }

  protected void loadAnswer(LocalScore localScore, Element itemNode, int itemIndex) {
    String itemOrder = itemNode.getAttribute(Constants.ORDER);
    String itemClass = itemNode.getAttribute(Constants.CLASS);
    if (itemClass.equals("surveyQuestionHorizontal")) {
      String ref = RegistryAssessmentUtils.getAttributeOrElementText(itemNode, Constants.XFORM_REF);
      String itemScore = itemNode.getAttribute("ItemScore");
      if ( !itemScore.equals("")) {
        localScore.setAnswer(Integer.parseInt(itemOrder), ref, new BigDecimal(itemScore));                
      }
    } else if (itemClass.equals("surveyQuestionBodymap")) {
      String ref = RegistryAssessmentUtils.getAttributeOrElementText(itemNode, Constants.XFORM_REF);
      String itemScore = itemNode.getAttribute("ItemScore");
      if ( !itemScore.equals("")) {
        localScore.setAnswer(Integer.parseInt(itemOrder), ref, new BigDecimal(itemScore));                
      }
    } else {
      SurveyQuestionIntf item = RegistryAssessmentUtils.getQuestion(itemNode, itemIndex, false);
      ArrayList<SurveyAnswerIntf> responses = item.getAnswers(true);
      for(SurveyAnswerIntf response : responses) {
        String ref = ((RegistryAnswer) response).getReference();
        BigDecimal score = getResponseScore(response);
        if (score != null) {
          localScore.setAnswer(Integer.parseInt(itemOrder), ref, score);
        }
      }
    }
  }

  protected BigDecimal getResponseScore(SurveyAnswerIntf response) {
    int type = response.getType();
    if ((type == Constants.TYPE_SELECT) || (type == Constants.TYPE_SELECT1) || (type == Constants.TYPE_DROPDOWN)) {
      SelectElement select = (SelectElement) response;
      ArrayList<SelectItem> selectItems = select.getSelectedItems();

      int score = 0;
      for (SelectItem selectItem : selectItems) {
        String value = selectItem.getValue();
        score += Integer.parseInt(value);
      }
      return new BigDecimal(score);
    } else if (type == Constants.TYPE_SLIDER) {
      InputElement select = (InputElement) response;
      String value = select.getValue();
      int score = Integer.parseInt(value);
      return new BigDecimal(score);
    }
    return null;
  }

  protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
    // By default LocalScore sums all answers
  }

  protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow) {
    Table table = new Table();
    if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
      return table;
    }

    table.addHeading(study.getTitle());
    if (headerRow) {
      TableRow colHeader = new TableRow(100);
      colHeader.setColumnGap(3);
      colHeader.addColumn(new TableColumn("Date", 65));
      colHeader.addColumn(new TableColumn("Score", 31));
      table.addRow(colHeader);
    }
    for (ChartScore score : scores) {
      TableRow row = new TableRow(100);
      row.setColumnGap(3);
      row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 65));
      row.addColumn(new TableColumn(scoreFormatter.format(score.getScore()), 31));
      table.addRow(row);
    }

    return table;
  }

}
