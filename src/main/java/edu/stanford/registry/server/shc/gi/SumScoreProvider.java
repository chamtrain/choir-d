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
package edu.stanford.registry.server.shc.gi;

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
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

public class SumScoreProvider extends ExtensibleScoreProvider {

  private static final Logger logger = LoggerFactory.getLogger(SumScoreProvider.class);
  private int answeredQuestions = 0;

  private List<String> refs = new ArrayList<>();
  public SumScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName, List<String> refs) {
    super(dbp, siteInfo, studyName);
    this.refs = refs;
  }

  @Override
  protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {

    logger.info(" calculateScore for study {}", patientData.getStudyDescription());
    int totalScore = refs.stream().mapToInt(question -> getAnsweredScore(question, chartScore))
        .filter(num -> num < 99)
        .sum();
    logger.info(" totalScore " + totalScore);
    chartScore.setScore(new BigDecimal(totalScore));
  }

  @Override
  protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow) {
    Table table = new Table();
    if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null) || (scores.get(0).getScore().intValue() < 0)) {
      return table;
    }

    table.addHeading(study.getTitle());
    TableRow colHeader = new TableRow(100);
    if (scores.size() > 0) {
      ChartScore score = scores.get(0);
      TableRow ghHeadingRow = new TableRow(500);
      ghHeadingRow.addColumn(new TableColumn("Date", 20));
      ghHeadingRow.addColumn(new TableColumn("Score", 20));
      table.addRow(ghHeadingRow);

      int totalScore = score.getScore().intValue();
      TableRow ghRow = new TableRow(100);
      ghRow.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
      ghRow.addColumn(new TableColumn(String.valueOf(totalScore), 20));

      table.addRow(ghRow);
    }
    return table;
  }
  private int getAnsweredScore(String question, LocalScore chartScore) {
    //since all questions are required questions so far
    return chartScore.isAnswered(question) ? chartScore.getAnswer(question).intValue() : -1;
  }

}