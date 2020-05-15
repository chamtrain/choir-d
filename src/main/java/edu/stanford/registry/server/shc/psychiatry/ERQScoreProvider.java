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

public class ERQScoreProvider extends ExtensibleScoreProvider {
  private final Map<String, List<String>> subScales = new LinkedHashMap<>();

  public ERQScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
    subScales.put("Cognitive Reappraisal facet", Arrays.asList("feel_positive", "feel_less_neg", "think_calm", "feel_pos_thinking", "cntr_emot_think", "feel_neg_thinking"));
    subScales.put("Expressive Suppression facet", Arrays.asList("keep_emotions", "feel_pos_exp", "cntr_emot_exp", "neg_no_exp"));
  }

  @Override
  protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
    int totalScore = 0;
    Map<String, Integer> scores = new LinkedHashMap<>();
    scores.put("Cognitive Reappraisal facet", 0);
    scores.put("Expressive Suppression facet", 0);


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
    colHeader.addColumn(new TableColumn("Cognitive Reappraisal facet", 36));
    colHeader.addColumn(new TableColumn("Expressive Suppression facet", 36));
    colHeader.addColumn(new TableColumn("Total", 12));
    table.addRow(colHeader);

    for (ChartScore score : scores) {
      TableRow row = new TableRow(100);
      row.setColumnGap(3);
      row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 16));
      Map<String,BigDecimal> scoreValues = score.getScores();
      row.addColumn(new TableColumn((scoreValues.get("Cognitive Reappraisal facet") != null)
          ? scoreFormatter.format(scoreValues.get("Cognitive Reappraisal facet"))
          : "", 36));
      row.addColumn(new TableColumn((scoreValues.get("Expressive Suppression facet") != null)
          ? scoreFormatter.format(scoreValues.get("Expressive Suppression facet"))
          : "", 36));
      row.addColumn(new TableColumn((scoreValues.get("Total") != null)
          ? scoreFormatter.format(scoreValues.get("Total"))
          : "", 12));
      table.addRow(row);
    }
    return table;
  }

  public int getAnsweredScore(String question, LocalScore chartScore){
    return chartScore.isAnswered(question)? chartScore.getAnswer(question).intValue(): 0;
  }
}
