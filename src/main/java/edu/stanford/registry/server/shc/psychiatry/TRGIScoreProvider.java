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

public class TRGIScoreProvider extends ExtensibleScoreProvider {

  private final Map<String, List<String>> subScales = new LinkedHashMap<>();

  public TRGIScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
    subScales.put("Global Guilt", Arrays.asList("intense_guilt", "freq_guilt", "guilt_severity", "overall_guilt_feel"));
    subScales.put("Distress", Arrays.asList("distressed_happened", "cause_emot_pain", "feel_sorrow",
        "sev_emot_distress", "cause_pain", "reminded_phy_react"));
    subScales.put("Guilt Cognitions",
        Arrays.asList("cld_prevent", "justified", "resp_cause", "against_values", "made_sense", "knew_better",
            "incstnt_belief", "knew_to_do", "known_better", "belief_shdnt_have", "good_reason_todo", "self_blame",
            "feel_shdnt_hv", "self_blame_doing", "hold_self_resp", "act_not_justified", "violated_personal",
            "did_shdnt_hv_done", "should_hv_done", "did_unforgivable", "didnt_do_wrong", ""));
    subScales.put("Hindsight Bias", Arrays.asList("cld_prevent", "resp_cause", "knew_better", "known_better",
        "self_blame", "self_blame_doing", "hold_self_resp"));
    subScales.put("Wrongdoing",
        Arrays.asList("feel_unneeded", "against_values", "incstnt_belief", "belief_shdnt_have", "feel_shdnt_hv"));
    subScales.put("Lack of Justification", Arrays.asList("justified", "made_sense", "knew_to_do", "good_reason_todo"));
  }

  @Override
  protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
    double totalScore;
    Map<String, BigDecimal> finalScores = new LinkedHashMap<>();

    subScales.keySet().forEach(ss -> {
      double ssScore = subScales.get(ss).stream().mapToInt(question -> {
        // revert the answer if the sub-scale is Lack of Justification and Item 17
        if (question.equalsIgnoreCase("good_reason_todo") && ss.equalsIgnoreCase("Lack of Justification")) {
          int ans = Math.abs(getAnsweredScore(question, chartScore) - 4);
          return ans;
        }
        return getAnsweredScore(question, chartScore);
      }).average().orElse(0.0);
      finalScores.put(ss, BigDecimal.valueOf(ssScore));
    });

    // Total score is only the sum of all the sub-scales
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
    colHeader.addColumn(new TableColumn("Global Guilt", 13));
    colHeader.addColumn(new TableColumn("Distress", 10));
    colHeader.addColumn(new TableColumn("Guilt Cognitions", 14));
    colHeader.addColumn(new TableColumn("Hindsight Bias", 13));
    colHeader.addColumn(new TableColumn("Wrongdoing", 12));
    colHeader.addColumn(new TableColumn("Lack of Justification", 16));
    colHeader.addColumn(new TableColumn("Total", 11));
    table.addRow(colHeader);

    for (ChartScore score : scores) {
      TableRow row = new TableRow(100);
      row.setColumnGap(3);
      row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 11));
      Map<String, BigDecimal> scoreValues = score.getScores();
      row.addColumn(new TableColumn(
          (scoreValues.get("Global Guilt") != null) ? scoreFormatter.format(scoreValues.get("Global Guilt")) : "", 13));
      row.addColumn(new TableColumn(
          (scoreValues.get("Distress") != null) ? scoreFormatter.format(scoreValues.get("Distress")) : "", 10));
      row.addColumn(new TableColumn(
          (scoreValues.get("Guilt Cognitions") != null) ? scoreFormatter.format(scoreValues.get("Guilt Cognitions"))
              : "", 14));
      row.addColumn(new TableColumn(
          (scoreValues.get("Hindsight Bias") != null) ? scoreFormatter.format(scoreValues.get("Hindsight Bias")) : "",
          13));
      row.addColumn(new TableColumn(
          (scoreValues.get("Wrongdoing") != null) ? scoreFormatter.format(scoreValues.get("Wrongdoing")) : "", 12));
      row.addColumn(new TableColumn((scoreValues.get("Lack of Justification") != null) ? scoreFormatter
          .format(scoreValues.get("Lack of Justification")) : "", 16));
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
