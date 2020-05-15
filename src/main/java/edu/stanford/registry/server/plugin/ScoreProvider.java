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

package edu.stanford.registry.server.plugin;

import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.survey.Table;

import java.util.ArrayList;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYDataset;

public interface ScoreProvider {

  String getDescription();

  boolean acceptsSurveyName(String studyName);

  ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData);

  //public abstract ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData, boolean wasAssisted);

  XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> scores, PrintStudy study,
                       ChartConfigurationOptions opts);

  Table getScoreTable(ArrayList<ChartScore> scores);

  String formatExplanationText(Study study, ArrayList<ChartScore> scores);


  ChartInfo createLineChart(ArrayList<ChartScore> stats, XYDataset ds, Study study,
                            ChartConfigurationOptions opts);

  ArrayList<SurveyQuestionIntf> getSurvey(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study,
                                          Patient patient, boolean allAnswers);

  ArrayList<SurveyQuestionIntf> getSurvey(PatientStudyExtendedData patStudy, PrintStudy study, Patient patient,
                                          boolean allAnswers);

  XYPlot getPlot(ChartInfo chartInfo, // ArrayList<ChartScore> stats, XYDataset ds,
                 ArrayList<Study> studies,
                 ChartConfigurationOptions opts);


  Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient);

  int getReportTextFontSize(PrintStudy study);


}
