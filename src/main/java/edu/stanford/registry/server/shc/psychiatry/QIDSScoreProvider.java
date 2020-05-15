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


class QIDSScoreProvider extends ExtensibleScoreProvider {
  private final Map<String, List<String>> subScales = new LinkedHashMap<>();

  public QIDSScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
    subScales.put("Sleep", Arrays.asList("fall_asleep", "sleep_during_night", "waking_early", "sleep_too_much"));
    subScales.put("Appetite", Arrays.asList("decr_appetite", "incr_appetite", "decr_weight", "incr_weight"));
    subScales.put("Psychomotor", Arrays.asList("feel_slw_dwn", "feel_restless"));
    subScales.put("Other", Arrays.asList("feel_sad", "conc_dcsn_making", "view_myself", "thought_death_suicide", "gen_interest", "energy_lvl"));
  }

  @Override
  protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
    int totalScore = 0;
    int sleepScore = 0;
    int appetiteScore = 0;
    int psychomotorScore = 0;

    for (String question : subScales.get("Sleep")) {
      int answer = getAnsweredScore(question, chartScore);
      if(answer > sleepScore){
        sleepScore = answer;
      }
    }

    for (String question : subScales.get("Appetite")) {
      int answer = getAnsweredScore(question, chartScore);
      if( answer > appetiteScore){
        appetiteScore = answer;
      }
    }

    for (String question : subScales.get("Psychomotor")) {
      int answer = getAnsweredScore(question, chartScore);
      if( answer > psychomotorScore){
        psychomotorScore = answer;
      }
    }

    totalScore = sleepScore + appetiteScore + psychomotorScore;

    for (String question : subScales.get("Other")) {
      totalScore = totalScore + getAnsweredScore(question, chartScore);
    }

    Map<String, BigDecimal> finaScores = new LinkedHashMap<>();
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
    colHeader.addColumn(new TableColumn("Date", 50));
    colHeader.addColumn(new TableColumn("Total", 50));
    table.addRow(colHeader);

    for (ChartScore score : scores) {
      TableRow row = new TableRow(100);
      row.setColumnGap(3);
      row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 50));
      Map<String,BigDecimal> scoreValues = score.getScores();

      row.addColumn(new TableColumn((scoreValues.get("Total") != null)
          ? scoreFormatter.format(scoreValues.get("Total"))
          : "", 50));
      table.addRow(row);
    }
    return table;
  }

  public int getAnsweredScore(String question, LocalScore chartScore){
    return chartScore.isAnswered(question)? chartScore.getAnswer(question).intValue(): 0;
  }
}
