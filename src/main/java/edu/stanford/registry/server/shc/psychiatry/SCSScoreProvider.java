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

import com.github.susom.database.Database;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.survey.ExtensibleScoreProvider;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SCSScoreProvider extends ExtensibleScoreProvider {

  private final Map<String, List<String>> subScales = new LinkedHashMap<>();

  public SCSScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
    subScales.put("Self-Kindness",
        Arrays.asList("self_love", "self_caring", "kind_to_self", "self_tolerant", "self_patience"));
    subScales.put("Self-Judgment",
        Arrays.asList("judgmental", "tough_on_myself", "intolerant", "neg_self_aspect", "cold_hearted"));
    subScales.put("Common Humanity",
        Arrays.asList("app_difficulties", "same_with_others", "shared_by_most", "human_condition"));
    subScales.put("Isolation", Arrays.asList("feel_separate", "feel_least_happier", "others_easy", "feel_alone"));
    subScales.put("Mindfulness",
        Arrays.asList("balance_emotions", "balanced_view", "keep_perspective", "curiosity_openness"));
    subScales.put("Over-Identified", Arrays.asList("obsess", "consumed", "carried_away", "out_of_proportion"));
  }

  @Override
  protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
    double totalScore;
    Map<String, BigDecimal> finalScores = new LinkedHashMap<>();

    subScales.keySet().forEach(ss -> {
      int ssTotalScore = subScales.get(ss).stream()
          .mapToInt(question -> getAnsweredScore(question, chartScore)).sum();
      finalScores.put(ss, BigDecimal.valueOf(ssTotalScore));
    });

    totalScore = finalScores.values().stream().mapToDouble(BigDecimal::doubleValue).sum();

    finalScores.put("Total", new BigDecimal(totalScore));
    chartScore.setScore(new BigDecimal(totalScore));
    chartScore.setScores(finalScores);

  }

  @Override
  protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow) {
    Table table = new Table();
    if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
      return table;
    }

    table.addHeading(study.getTitle());
    TableRow colHeader = new TableRow(100);

    colHeader.addColumn(new TableColumn("Date", 11));
    colHeader.addColumn(new TableColumn("Self-Kindness", 13));
    colHeader.addColumn(new TableColumn("Self-Judgment", 13));
    colHeader.addColumn(new TableColumn("Common Humanity", 13));
    colHeader.addColumn(new TableColumn("Isolation", 13));
    colHeader.addColumn(new TableColumn("Mindfulness", 13));
    colHeader.addColumn(new TableColumn("Over-Identified", 13));
    colHeader.addColumn(new TableColumn("Total", 11));
    table.addRow(colHeader);

    for (ChartScore score : scores) {
      TableRow row = new TableRow(100);
      row.setColumnGap(3);
      row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 11));
      Map<String, BigDecimal> scoreValues = score.getScores();
      row.addColumn(new TableColumn(
          (scoreValues.get("Self-Kindness") != null) ? scoreFormatter.format(scoreValues.get("Self-Kindness"))
              : "", 13));
      row.addColumn(new TableColumn(
          (scoreValues.get("Self-Judgment") != null) ? scoreFormatter.format(scoreValues.get("Self-Judgment")) : "",
          13));
      row.addColumn(new TableColumn(
          (scoreValues.get("Common Humanity") != null) ? scoreFormatter.format(scoreValues.get("Common Humanity")) : "",
          13));
      row.addColumn(new TableColumn(
          (scoreValues.get("Isolation") != null) ? scoreFormatter.format(scoreValues.get("Isolation")) : "", 13));
      row.addColumn(new TableColumn(
          (scoreValues.get("Mindfulness") != null) ? scoreFormatter.format(scoreValues.get("Mindfulness")) : "", 13));
      row.addColumn(new TableColumn(
          (scoreValues.get("Over-Identified") != null) ? scoreFormatter.format(scoreValues.get("Over-Identified")) : "",
          13));
      row.addColumn(
          new TableColumn((scoreValues.get("Total") != null) ? scoreFormatter.format(scoreValues.get("Total")) : "",
              11));
      table.addRow(row);
    }
    return table;
  }

  private int getAnsweredScore(String question, LocalScore chartScore) {
    return chartScore.isAnswered(question) ? chartScore.getAnswer(question).intValue() : 0;
  }

}
