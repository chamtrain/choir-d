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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.ExtensibleScoreProvider;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.SurveyAdvanceBase;
import edu.stanford.registry.server.utils.SquareXml;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

public class PsychiatryScoreProviderFactory {
  private static final Logger logger = LoggerFactory.getLogger(PsychiatryScoreProviderFactory.class);

  static public ScoreProvider create(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    if (studyName.equals("DERS18")) {
      return new DERS18ScoreProvider(dbp.get(), siteInfo, studyName);
    }
    if (studyName.equals("PCL5")) {
      return new PCL5ScoreProvider(dbp.get(), siteInfo, studyName);
    }

    if (studyName.equals("QIDS")) {
      return new QIDSScoreProvider(dbp.get(), siteInfo, studyName);
    }

    if (studyName.equals("ERQ")) {
      return new ERQScoreProvider(dbp.get(), siteInfo, studyName);
    }

    if (studyName.equals("EDEQ")) {
      return new EDEQScoreProvider(dbp.get(), siteInfo, studyName);
    }

    if (studyName.equals("FOCI")) {
      return new FOCIScoreProvider(dbp.get(), siteInfo, studyName);
    }

    if (studyName.equals("SCS")) {
      return new SCSScoreProvider(dbp.get(), siteInfo, studyName);
    }

    if (studyName.equals("TRGI")) {
      return new TRGIScoreProvider(dbp.get(), siteInfo, studyName);
    }

    if (studyName.equals("Qol")) {
      return new QoLScoreProvider(dbp.get(), siteInfo, studyName);
    }

    // No custom provider
    return new ExtensibleScoreProvider(dbp.get(), siteInfo, studyName);
  }

  /**
   * PCL-5 Score Provider
   */
  static class PCL5ScoreProvider extends ExtensibleScoreProvider {

    public PCL5ScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }
    @Override
    public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
      ArrayList<ChartScore> scores = new ArrayList<>();
      if (patientData == null || patientData.getStudyDescription() == null) {
        return scores;
      }
      return getPCL5score(patientData, scores);
    }

    private ArrayList<ChartScore> getPCL5score(PatientStudyExtendedData patientData, ArrayList<ChartScore> scores) {
      String type = "PCL5";
      LocalScore localScore = new LocalScore(patientData.getDtChanged(), patientData.getPatientId(), patientData.getStudyCode(), patientData.getStudyDescription());
      String surveyProvider = patientData.getSurveySystemId().toString();
      String sectionId = patientData.getStudyCode().toString();
      SquareXml squareXml = new SquareXml(dbp.get(), siteInfo, patientData.getStudyDescription(), type);
      SurveyDao surveyDao = new SurveyDao(dbp.get());
      SurveyQuery query = new SurveyQuery(dbp.get(), surveyDao, patientData.getSurveySiteId());

      Survey s = query.surveyBySurveyToken(patientData.getToken());
      LinkedHashMap<String, String> columns = squareXml.getColumns();
      LinkedHashMap<String, String> references = squareXml.getReferences();
      Object refKeys[] = references.keySet().toArray();
      int inx = 0;

      SurveyAdvanceUtils surveyAdvanceUtils = new SurveyAdvanceUtils(siteInfo);

      for (String columnName : columns.keySet()) {
        logger.trace("getting " + type + " score for {}", columnName);
        String refKey = refKeys[inx].toString();
        String fieldId = references.get(refKey) + ":" + refKey;
        String[] parts = fieldId.split(":");
        try {
          SurveyStep step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "Order" + parts[0]);
          Integer score;
          logger.trace(
              " surveyAdvanceUtils.selectedFieldChoice(step(surveyProvider, {}, Order{}), {}", sectionId, parts[0], fieldId);
          score = surveyAdvanceUtils.selectedFieldChoice(step, fieldId);
          if (score != null) {
            logger.trace(" adding score answer {}, {}", parts[0], score);
            localScore.setAnswer(Integer.parseInt(parts[0]), refKey, new BigDecimal(score));
          }
        } catch (NumberFormatException nfe) {
          logger.error("could not determine " + type + " score for item {}", parts[0]);
        }
        inx++;
      }

      scores.add(localScore);
      return scores;

    }
    @Override
    public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient) {

      Table table = new Table();
      if (patientStudies == null || patientStudies.size() < 1 || study == null || study.getStudyDescription() == null) {
        return table;
      }
      logger.trace("getTable called for {}", study.getStudyDescription());
      if (study.getStudyDescription().startsWith("PCL5")) {
        for (PatientStudyExtendedData patientStudyExtendedData : patientStudies) {
          if (patientStudyExtendedData.getStudyCode().intValue() == study.getStudyCode().intValue()) {
            ArrayList<ChartScore> scores = new ArrayList<>();
            int width = 50;
            scores = getPCL5score(patientStudyExtendedData, scores);
            logger.trace("getScore returned {} scores", scores.size());
            for (ChartScore score : scores) {
              TableRow row = new TableRow(100);
              row.addColumn(new TableColumn("Score", width));
              BigDecimal scoreValue = score.getScore();
              row.addColumn(new TableColumn(String.valueOf(scoreValue), 10));
              table.addRow(row);
            }
          }
        }
      }
      return table;
    }
  }
  // Make our own because SurveyAdvanceBase is abstract
  static private class SurveyAdvanceUtils extends SurveyAdvanceBase {
    public SurveyAdvanceUtils(SiteInfo siteInfo) {
      super(siteInfo);
    }
  }

    /**
   * DERS-18 Score Provider
   */
  static class DERS18ScoreProvider extends ExtensibleScoreProvider {
    private final Map<String, List<String>> subScales = new LinkedHashMap<>();

    public DERS18ScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
      subScales.put("Awareness", Arrays.asList("pay_att_feel", "attentive_feeling", "ack_emotions"));
      subScales.put("Clarity", Arrays.asList("no_idea_feeling", "difficulty_sense", "conf_how_feel"));
      subScales.put("Goals", Arrays.asList("difficulty_work", "difficult_focus", "difficulty_conc"));
      subScales.put("Impulse", Arrays.asList("out_of_control", "difficulty_control", "lose_control"));
      subScales.put("Nonacceptance", Arrays.asList("emb_feeling", "feel_ashamed", "feel_guilty"));
      subScales.put("Strategies", Arrays.asList("remain_that_way", "feeling_depressed", "believe_wallow"));
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      int totalScore = 0;
      Map<String, Integer> scores = new LinkedHashMap<>();
      scores.put("Awareness", 0);
      scores.put("Clarity", 0);
      scores.put("Goals", 0);
      scores.put("Impulse", 0);
      scores.put("Nonacceptance", 0);
      scores.put("Strategies", 0);

      for (String subScale : subScales.keySet()) {
        List<String> questions = subScales.get(subScale);
        for (String question: questions) {
          int currentScore = (getAnsweredScore(question, chartScore));
          totalScore += currentScore;
          scores.put(subScale, scores.get(subScale) + currentScore);
        }
      }

      Map<String, BigDecimal> finaScores = new LinkedHashMap<>();
      for (String subScale : scores.keySet()){
        finaScores.put(subScale, new BigDecimal(scores.get(subScale)));
      }
      finaScores.put("Total", new BigDecimal(totalScore));
      chartScore.setScore(new BigDecimal(totalScore));
      chartScore.setScores(finaScores);
    }

    @Override
    protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow) {
      Table table = new Table();
      if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
        return table;
      }

      table.addHeading(study.getTitle());
      TableRow colHeader = new TableRow(100);

      colHeader.addColumn(new TableColumn("Date", 16));
      colHeader.addColumn(new TableColumn("Awareness", 13));
      colHeader.addColumn(new TableColumn("Clarity", 10));
      colHeader.addColumn(new TableColumn("Goals", 9));
      colHeader.addColumn(new TableColumn("Impulse", 11));
      colHeader.addColumn(new TableColumn("Nonacceptance", 18));
      colHeader.addColumn(new TableColumn("Strategies", 12));
      colHeader.addColumn(new TableColumn("Total", 11));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 16));
        Map<String,BigDecimal> scoreValues = score.getScores();
        row.addColumn(new TableColumn((scoreValues.get("Awareness") != null)
            ? scoreFormatter.format(scoreValues.get("Awareness"))
            : "", 13));
        row.addColumn(new TableColumn((scoreValues.get("Clarity") != null)
            ? scoreFormatter.format(scoreValues.get("Clarity"))
            : "", 10));
        row.addColumn(new TableColumn((scoreValues.get("Goals") != null)
            ? scoreFormatter.format(scoreValues.get("Goals"))
            : "", 9));
        row.addColumn(new TableColumn((scoreValues.get("Impulse") != null)
            ? scoreFormatter.format(scoreValues.get("Impulse"))
            : "", 11));
        row.addColumn(new TableColumn((scoreValues.get("Nonacceptance") != null)
            ? scoreFormatter.format(scoreValues.get("Nonacceptance"))
            : "", 18));
        row.addColumn(new TableColumn((scoreValues.get("Strategies") != null)
            ? scoreFormatter.format(scoreValues.get("Strategies"))
            : "", 12));
        row.addColumn(new TableColumn((scoreValues.get("Total") != null)
            ? scoreFormatter.format(scoreValues.get("Total"))
            : "", 11));
        table.addRow(row);
      }
      return table;
    }

    public int getAnsweredScore(String question, LocalScore chartScore){
      return chartScore.isAnswered(question)? chartScore.getAnswer(question).intValue(): 0;
    }
  }
}
