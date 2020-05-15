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

public class EDEQScoreProvider extends ExtensibleScoreProvider {

  private final Map<String, List<String>> subScales = new LinkedHashMap<>();

  public EDEQScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
    subScales.put("Restraint", Arrays.asList("lmt_fd_eat", "gn_wt_eating", "exc_food", "flw_rules", "des_emp_stmc"));
    subScales.put("Eating Concern",
        Arrays.asList("thnk_fd_dfclt", "lose_ctrl_eat", "eat_in_scrt", "cncrd_oth_see", "eat_guilt_shp"));
    subScales.put("Shape Concern", Arrays
        .asList("des_flt_stmc", "thnk_shp_dfclt", "shp_influence", "fear_gain_wght", "dstsf_shp", "unc_self_body",
            "unc_oth_body", "felt_fat"));
    subScales.put("Weight Concern",
        Arrays.asList("wght_infuence", "upst_asked_wght", "thnk_shp_dfclt", "dstsf_wght", "dsr_lose_wght"));
  }

  @Override
  protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
    double totalScore = 0;
    Map<String, Integer> scores = new LinkedHashMap<>();
    scores.put("Restraint", 0);
    scores.put("Eating Concern", 0);
    scores.put("Shape Concern", 0);
    scores.put("Weight Concern", 0);

    Map<String, BigDecimal> finaScores = new LinkedHashMap<>();

    scores.keySet().forEach(ss -> {
      Double ssScore = subScales.get(ss).stream()
          .mapToInt(question -> getAnsweredScore(question, chartScore))
          .average()
          .orElse(0.0);
      finaScores.put(ss, BigDecimal.valueOf(ssScore));

      //TODO: These scores are rounded up when generated
    });

    totalScore = finaScores.values().stream()
        .mapToDouble(BigDecimal::doubleValue)
        .average()
        .orElse(0.0);

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

    colHeader.addColumn(new TableColumn("Date", 12));
    colHeader.addColumn(new TableColumn("Restraint", 19));
    colHeader.addColumn(new TableColumn("Eating Concern", 19));
    colHeader.addColumn(new TableColumn("Shape Concern", 19));
    colHeader.addColumn(new TableColumn("Weight Concern", 19));
    colHeader.addColumn(new TableColumn("Total", 12));
    table.addRow(colHeader);

    for (ChartScore score : scores) {
      TableRow row = new TableRow(100);
      row.setColumnGap(3);
      row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 12));
      Map<String, BigDecimal> scoreValues = score.getScores();
      row.addColumn(new TableColumn(
          (scoreValues.get("Restraint") != null) ? scoreFormatter.format(scoreValues.get("Restraint")) : "", 19));
      row.addColumn(new TableColumn(
          (scoreValues.get("Eating Concern") != null) ? scoreFormatter.format(scoreValues.get("Eating Concern")) : "",
          19));
      row.addColumn(new TableColumn(
          (scoreValues.get("Shape Concern") != null) ? scoreFormatter.format(scoreValues.get("Shape Concern")) : "",
          19));
      row.addColumn(new TableColumn(
          (scoreValues.get("Weight Concern") != null) ? scoreFormatter.format(scoreValues.get("Weight Concern")) : "",
          19));
      row.addColumn(
          new TableColumn((scoreValues.get("Total") != null) ? scoreFormatter.format(scoreValues.get("Total")) : "",
              12));
      table.addRow(row);
    }
    return table;
  }

  private int getAnsweredScore(String question, LocalScore chartScore) {
    return chartScore.isAnswered(question) ? chartScore.getAnswer(question).intValue() : 0;
  }
}
