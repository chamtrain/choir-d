/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Study;

import java.util.ArrayList;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;

public class ChartInfo {

  private JFreeChart chart;
  ArrayList<ChartScore> stats;
  XYDataset ds;
  Study study;
  String surveySystemName;

  public ChartInfo(String surveySystemName, ArrayList<ChartScore> stats, XYDataset ds, Study study) {
    this.surveySystemName = surveySystemName;
    this.stats = stats;
    this.ds = ds;
    this.study = study;
  }

  public void setChart(JFreeChart chart) {
    this.chart = chart;
  }

  public JFreeChart getChart() {
    return chart;
  }

  public void setScores(ArrayList<ChartScore> stats) {
    this.stats = stats;
  }

  public ArrayList<ChartScore> getScores() {
    return stats;
  }

  public void setDataSet(XYDataset set) {
    this.ds = set;
  }

  public XYDataset getDataSet() {
    return ds;
  }

  public void setStudy(Study study) {
    this.study = study;
  }

  public Study getStudy() {
    return study;
  }

  public void setSurveySystemName(String surveySystemName) {
    this.surveySystemName = surveySystemName;
  }

  public String getSurveySystemName() {
    return surveySystemName;
  }

  public ArrayList<ChartScore> getSeriesScores(int series) {
    ArrayList<ChartScore> sScores = new ArrayList<>();
    if (ds == null || stats == null) {
      return sScores;
    }
    if (series == 0 || series >= ds.getSeriesCount()) {
      return sScores;
    }
    int seriesIndex = 1;
    int scoreIndex = 0;
    while (seriesIndex < series) {
      scoreIndex += ds.getItemCount(seriesIndex);
      seriesIndex++;
    }

    for (int i = 0; i < ds.getItemCount(series); i++) {
      sScores.add(stats.get(scoreIndex + i));
    }
    return sScores;
  }

  public ChartScore getSeriesScore(int series, int item) {

    if (ds != null && stats != null) {
      if (series > 0 && series < ds.getSeriesCount()) {
        int seriesIndex = 1;
        int scoreIndex = 0;
        while (seriesIndex < series) {
          scoreIndex += ds.getItemCount(seriesIndex);
          seriesIndex++;
        }
        if (stats.size() > scoreIndex + item) {
          return stats.get(scoreIndex + item);
        }
      }
    }
    return null;
  }
}
