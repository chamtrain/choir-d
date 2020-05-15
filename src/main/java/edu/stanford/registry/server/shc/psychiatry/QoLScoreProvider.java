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
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
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

public class QoLScoreProvider extends ExtensibleScoreProvider {

  private final List<String> refReverseValue = new ArrayList<>(
      Arrays.asList("work_school", "social_life", "family_home_life"));

  QoLScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(PatientStudyExtendedData patStudy, PrintStudy study,
      Patient patient, boolean allAnswers) {

    return ScoreToAnswerAdapter.transform(patStudy, refReverseValue);
  }

  @Override
  protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
    int totalScore = 0;

    totalScore = refReverseValue.stream().mapToInt(question -> getAnsweredScore(question, chartScore)).sum();

    Map<String, BigDecimal> finalScores = new LinkedHashMap<>();
    finalScores.put("Total", new BigDecimal(totalScore));
    chartScore.setScore(new BigDecimal(totalScore));
    chartScore.setScores(finalScores);
  }

  private int getAnsweredScore(String question, LocalScore chartScore) {

    return chartScore.isAnswered(question) ? chartScore.getAnswer(question).intValue() : 0;
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
      Map<String, BigDecimal> scoreValues = score.getScores();
      row.addColumn(
          new TableColumn((scoreValues.get("Total") != null) ? scoreFormatter.format(scoreValues.get("Total")) : "",
              50));
      table.addRow(row);
    }

    return table;
  }
}
