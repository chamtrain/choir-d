/*
 * Copyright 2020

 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.shc.ohsu;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.charts.ChartMaker;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.ExtensibleScoreProvider;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.RegistryShortFormScoreProvider;
import edu.stanford.registry.server.tools.CustomDateAxis;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.MultiScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.RegistryQuestion;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
import edu.stanford.registry.shared.xform.InputElement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;

/**
 * Score provider factory for Pediatric Pain
 */
public class OhsuScoreProviderFactory {
  private static final Logger logger = LoggerFactory.getLogger(OhsuScoreProviderFactory.class);

  static public ScoreProvider getScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    if (studyName.equals("childPainCurrent")) {
      return new BodyMapScoreProvider(dbp.get(), siteInfo, studyName);

    } else if (studyName.equals("painIntensity") || studyName.equals("proxyPainIntensity")) {
      return new PainIntensityScoreProvider(dbp.get(), siteInfo, studyName);

    } else if (studyName.equals("OhsuCatastrophizingScale") ||
        studyName.equals("proxyPainCatastrophizingScale") ||
        studyName.equals("proxyPainCatastrophizingScale2") ||
        studyName.equals("painCatastrophizingScale")) {
      return new PCSScoreProvider(dbp.get(), siteInfo, studyName);

    } else if (studyName.equals("childSelfEfficacy") || studyName.equals("proxySelfEfficacy") ||
        studyName.equals("proxySelfEfficacy2")) {
      return new SEScoreProvider(dbp.get(), siteInfo, studyName);

    } else if (studyName.equals("childCPAQPainBeliefs")) {
      return new CPAQScoreProvider(dbp.get(), siteInfo, studyName, true);

    } else if (studyName.equals("proxyCPAQPainBeliefs")) {
      return new CPAQScoreProvider(dbp.get(), siteInfo, studyName, false);
    } else if (studyName.equals("ARCSPainResponse")) {
      return new ARCSScoreProvider(dbp.get(), siteInfo, studyName);
    } else if (studyName.equals("ARCSProtect2")) {
      return new ARCSProtectScoreProvider(dbp.get(), siteInfo, studyName);
    } else if (studyName.equals("childFAD") || studyName.equals("parentFAD")) {
      return new FADScoreProvider(dbp.get(), siteInfo, studyName);
    } else if (studyName.equals("childPedsQL2")) {
      return new PedsQL2ScoreProvider(dbp.get(), siteInfo, studyName);
    } else if (studyName.equals("childFOPQ")) {
      return new FOPQScoreProvider(dbp.get(), siteInfo, studyName);
    } else if (studyName.equals("childSleepDisturbance")) {
      return new SleepDisturbanceScoreProvider(dbp.get(), siteInfo, studyName);
    } else if (studyName.equals("research")) {
      return new ResearchScoreProvider(dbp.get(), siteInfo, studyName);
    } else {
      return new RegistryShortFormScoreProvider(dbp.get(), siteInfo);
    }
  }

  /**
   * ScoreProvider for Body Map
   */
  static class BodyMapScoreProvider extends ExtensibleScoreProvider {
    public BodyMapScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }
    
    @Override
    protected void loadAnswer(LocalScore localScore, Element itemNode, int itemIndex) {
      String itemOrder = itemNode.getAttribute(Constants.ORDER);
      String itemClass = itemNode.getAttribute(Constants.CLASS);
      if (itemClass.equals("surveyQuestionBodymap")) {
        String ref = itemNode.getAttribute(Constants.XFORM_REF);
        String itemScore = itemNode.getAttribute("ItemScore");
        if ( !itemScore.equals("")) {
          localScore.setAnswer(Integer.parseInt(itemOrder), ref, new BigDecimal(itemScore));                
        }
      }
    }

    @Override
    public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> stats,
        PrintStudy study, ChartConfigurationOptions opts) {
      final TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(baseLineSeries);
      if (stats == null || study == null) {
        return dataset;
      }

      TimeSeries timeDataSetCnt = new TimeSeries("Areas selected");
      for (ChartScore stat3 : stats) {
        LocalScore score = (LocalScore) stat3;

        ArrayList<BigDecimal> answers = score.getAnswers();
        if (answers != null) {
          Day day = new Day(score.getDate());
          Double ttl = 0.0;
          for (BigDecimal answer : answers) {
            ttl = ttl + answer.doubleValue();
          }

          try {
            timeDataSetCnt.addOrUpdate(day, ttl);
          } catch (SeriesException duplicates) {
          }
        }
      }
      dataset.addSeries(timeDataSetCnt);
      return dataset;
    }
  }

  /**
   * Score provider for Pain Intensity
   */
  static class PainIntensityScoreProvider extends ExtensibleScoreProvider {

    private String title = "";
    private String painCurrentStudyName = "";
    private String painPastStudyName1 = "";
    private String painPastStudyName2 = "";

    public PainIntensityScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
      if (studyName.equals("painIntensity")) {
        title = "Pain Intensity";
        painCurrentStudyName = "childPainCurrent";
        painPastStudyName1 = "childPainPast";
        painPastStudyName2 = "childPainPastWeek";
      } else if (studyName.equals("proxyPainIntensity")) {
        title = "Parent Proxy Pain Intensity";
        painCurrentStudyName = "proxyPainCurrent";
        painPastStudyName1 = "proxyPainPast";
        painPastStudyName2 = "proxyPainPastWeek";
      }
    }

    @Override
    protected void loadAnswers(LocalScore localScore, PatientStudyExtendedData patientData) {
      PatStudyDao patStudyDao = new PatStudyDao(dbp.get(), siteInfo);

      LocalScore painCurrentScore = null;
      List<PatientStudyExtendedData> painCurrentSurveys =
          patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(patientData.getSurveyRegId(), painCurrentStudyName);
      if ((painCurrentSurveys != null) && (painCurrentSurveys.size() > 0)) {
        ExtensibleScoreProvider painCurrentScoreProvider = new ExtensibleScoreProvider(dbp, siteInfo, painCurrentStudyName);
        List<ChartScore> painCurrentChartScores = painCurrentScoreProvider.getScore(painCurrentSurveys.get(0));
        painCurrentScore = (LocalScore) (painCurrentChartScores.get(0));
      }

      LocalScore painPastScore = null;
      List<PatientStudyExtendedData> painPastSurveys =
          patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(
              patientData.getSurveyRegId(), painPastStudyName1);

      if ((painPastSurveys != null) && (painPastSurveys.size() > 0)) {
        ExtensibleScoreProvider painPastScoreProvider = new ExtensibleScoreProvider(dbp, siteInfo, painPastStudyName1);
        List<ChartScore> painPastChartScores = painPastScoreProvider.getScore(painPastSurveys.get(0));
        painPastScore = (LocalScore) (painPastChartScores.get(0));
      }

      if ((painPastScore == null) && !painPastStudyName2.equals("")) {
        painPastSurveys = patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(patientData.getSurveyRegId(), painPastStudyName2);
        if ((painPastSurveys != null) && (painPastSurveys.size() > 0)) {
          ExtensibleScoreProvider painPastScoreProvider = new ExtensibleScoreProvider(dbp, siteInfo, painPastStudyName2);
          List<ChartScore> painPastChartScores = painPastScoreProvider.getScore(painPastSurveys.get(0));
          painPastScore = (LocalScore) (painPastChartScores.get(0));
        }
      }

      if ((painCurrentScore == null) && (painPastScore == null)) {
        super.loadAnswers(localScore, patientData);
        return;
      }

      BigDecimal now = null;
      BigDecimal average = null;
      BigDecimal lowest = null;
      BigDecimal highest = null;
      if (painCurrentScore != null) {
        now = painCurrentScore.getAnswer("pain-intensity-now");
      }
      if (painPastScore != null) {
        average = painPastScore.getAnswer("pain-intensity-average");
        lowest = painPastScore.getAnswer("pain-intensity-lowest");
        highest = painPastScore.getAnswer("pain-intensity-highest");
      }

      localScore.setAnswer(1, "pain-intensity-highest", highest);
      localScore.setAnswer(2, "pain-intensity-average", average);
      localScore.setAnswer(3, "pain-intensity-now", now);
      localScore.setAnswer(4, "pain-intensity-lowest", lowest);
    }

    @Override
    public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies,
        PrintStudy study, Patient patient) {
      PatientStudyExtendedData painIntensity = null;
      for(PatientStudyExtendedData patientStudy : patientStudies) {
        if (patientStudy.getStudyCode().equals(study.getStudyCode())) {
          painIntensity = patientStudy;
        }
      }

      ArrayList<ChartScore> scores = new ArrayList<ChartScore>();
      if (painIntensity != null) {
        scores = getScore(painIntensity);
      }

      return getTableInternal(study, scores, false);
    }

    @Override
    protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow) {
      Table table = new Table();
      if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
        return table;
      }

      ArrayList<String> headings = new ArrayList<>();
      headings.add(title + ":  0=No Pain, 10=Worst Pain Imaginable");
      table.setHeadings(headings);
      TableRow qRow = new TableRow();
      qRow.addColumn(new TableColumn("Worst", 25));
      qRow.addColumn(new TableColumn("Average", 25));
      qRow.addColumn(new TableColumn("Now", 25));
      qRow.addColumn(new TableColumn("Least", 25));
      table.addRow(qRow);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        for(int i=1; i<=4; i++) {
          BigDecimal value = ((LocalScore)score).getAnswer(i);
          if (value != null) {
            row.addColumn(new TableColumn(scoreFormatter.format(value), 25));
          } else {
            row.addColumn(new TableColumn("", 25));
          }
        }
        table.addRow(row);
      }

      return table;
    }

    @Override
    public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> stats,
        PrintStudy study, ChartConfigurationOptions opts) {
      final TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(baseLineSeries);
      if (stats == null || study == null) {
        return dataset;
      }

      TimeSeries timeDataSetAvg = new TimeSeries("Average");
      TimeSeries timeDataSetWorst = new TimeSeries("Worst");
      for (ChartScore stat4 : stats) {
        LocalScore score = (LocalScore) stat4;

        Day day = new Day(score.getDate());
        // when the same study was taken > once in a day use the later
        // although this shouldn't really happen
        try {
          BigDecimal avg = score.getAnswer(INTENSITY_QUESTION_AVG);
          if (avg == null) {
            avg = score.getAnswer(INTENSITY_QUESTION_NOW);
          }
          if (avg != null) {
            timeDataSetAvg.addOrUpdate(day, avg);
          }
        } catch (SeriesException duplicates) {
        }
        try {
          BigDecimal worst = score.getAnswer(INTENSITY_QUESTION_WORST);
          if (worst != null) {
            timeDataSetWorst.addOrUpdate(day, worst);
          }
        } catch (SeriesException duplicates) {
        }
      }
      dataset.addSeries(timeDataSetAvg);
      dataset.addSeries(timeDataSetWorst);
      return dataset;
    }
  }

  /**
   * Score provider for CPAQ
   */
  static class CPAQScoreProvider extends ExtensibleScoreProvider {

    private static final String[] activitiesQuestions = new String[] {"Q1", "Q2", "Q3", "Q5", "Q6", "Q8", "Q9", "Q10", "Q12", "Q15", "Q19"};
    private static final String[] painQuestions = new String[] {"Q4", "Q7", "Q11", "Q13", "Q14", "Q16", "Q17", "Q18", "Q20"};
    
    private boolean isChild;
    private boolean is7PointScale = false;

    public CPAQScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName, boolean isChild) {
      super(dbp, siteInfo, studyName);
      this.isChild = isChild;
    }
    
    @Override
    protected void loadAnswers(LocalScore localScore, PatientStudyExtendedData patientData) {
      // Get the xml for the patient study
      String xmlString = null;
      if (patientData != null) {
        xmlString = patientData.getContents();
      }

      // Check if Q1 has a response item with the value 6. If so, this survey is using
      // the 7 point response scale.
      if ((xmlString != null) && !xmlString.equals("")) {
        String hasValue6 = XMLFileUtils.xPathQuery(xmlString,"//Response[@ref='Q1']/item/value=6");
        is7PointScale = Boolean.valueOf(hasValue6);
      }

      localScore.setAnswer(Integer.valueOf(0), "scale", is7PointScale ? new BigDecimal(7) : new BigDecimal(5));
      super.loadAnswers(localScore, patientData);     
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      // Versions of the childCPAQPainBeliefs before 10/21/2015 incorrectly used the same 7 point
      // scale as the parent proxyCPAQPainBeliefs. The childCPAQPainBeliefs is suppose to
      // to use a 5 point scale. If this is an older child CPAQ with the 7 point scale then
      // do not calculate a score as the score would be incorrect.
      if (isChild && is7PointScale) {
        return;
      }

      boolean missingActivitiesAnswer = false;
      int activitiesSum = 0;
      for(String question : activitiesQuestions) {
        if (chartScore.isAnswered(question)) {
          BigDecimal ans = chartScore.getAnswer(question);
          activitiesSum = activitiesSum + ans.intValue();
        } else {
          missingActivitiesAnswer = true;
        }
      }

      boolean missingPainAnswer = false;
      int painSum = 0;
      for(String question : painQuestions) {
        if (chartScore.isAnswered(question)) {
          BigDecimal ans = chartScore.getAnswer(question);
          if (isChild) {
            // Child questionnaire uses a 5 point scale, subtract from 4 to invert
            painSum = painSum + (4 - ans.intValue());            
          } else {
            // Parent questionnaire uses a 7 point scale, subtract from 6 to invert
            painSum = painSum + (6 - ans.intValue());
          }
        } else {
          missingPainAnswer = true;
        }
      }

      chartScore.setScore(new BigDecimal(0));

      Map<String,BigDecimal> scores = new LinkedHashMap<>();
      if (!missingActivitiesAnswer) {
        scores.put("Activities", new BigDecimal(activitiesSum));
      }
      if (!missingPainAnswer) {  
        scores.put("Pain", new BigDecimal(painSum));
      }
      if (!missingActivitiesAnswer && !missingPainAnswer) {
        scores.put("Total", new BigDecimal(activitiesSum + painSum));
      }
      chartScore.setScores(scores);
    }

    @Override
    protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow) {
      Table table = new Table();
      if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
        return table;
      }

      table.addHeading(study.getTitle());

      TableRow colHeader = new TableRow(100);
      colHeader.addColumn(new TableColumn("Date", 25));
      colHeader.addColumn(new TableColumn("Activities engagement", 25));
      colHeader.addColumn(new TableColumn("Pain willingness", 25));
      colHeader.addColumn(new TableColumn("Total", 25));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 25));
        Map<String,BigDecimal> scoreValues = score.getScores();
        BigDecimal activitiesScore = scoreValues.get("Activities");
        if (activitiesScore != null) {          
          row.addColumn(new TableColumn(scoreFormatter.format(activitiesScore), 25));
        } else {
          row.addColumn(new TableColumn("", 25));
        }
        BigDecimal painScore = scoreValues.get("Pain");
        if (painScore != null) {          
          row.addColumn(new TableColumn(scoreFormatter.format(painScore), 25));
        } else {
          row.addColumn(new TableColumn("", 25));
        }
        BigDecimal totalScore = scoreValues.get("Total");
        if (totalScore != null) {          
          row.addColumn(new TableColumn(scoreFormatter.format(totalScore), 25));
        } else {
          row.addColumn(new TableColumn("", 25));
        }
        table.addRow(row);
      }

      return table;
    }
  }

  /**
   * Score provider for ARCS
   */
  static class ARCSScoreProvider extends ExtensibleScoreProvider {

    protected static final DecimalFormat arcsScoreFormatter = new DecimalFormat("##.##");

    private static final String[] protectQuestions = new String[] {"Q3", "Q6", "Q8", "Q9", "Q10", "Q13", "Q15", "Q16",
        "Q18", "Q19", "Q20", "Q22", "Q24", "Q26", "Q27"};
    private static final String [] minimizeQuestions = new String[] {"Q2", "Q11", "Q17", "Q21", "Q25", "Q32"};
    private static final String [] distractQuestions = new String[] {"Q1", "Q5", "Q7", "Q12", "Q14", "Q29", "Q31", "Q33"};

    private final int studyARCSPainResponseCode;
    private final int studyARCSProtectCode;


    public ARCSScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
      SurveySystDao surveySystDao = new SurveySystDao(dbp.get());
      SurveySystem localSurveySystem = surveySystDao.getSurveySystem("Local");
      studyARCSPainResponseCode = getStudyCode(surveySystDao, localSurveySystem, "ARCSPainResponse");
      studyARCSProtectCode = getStudyCode(surveySystDao, localSurveySystem, "ARCSProtect2");
    }
 
    @Override
    protected ChartScore getScoreInternal(PatientStudyExtendedData patientData) {
      LocalScore localScore = new ARCSScore(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription());
      localScore.setAssisted(patientData.wasAssisted());

      loadAnswers(localScore, patientData);
      calculateScore(localScore, patientData);

      return localScore;
    }

    private int getStudyCode(SurveySystDao surveySystDao, SurveySystem localSurveySystem, String title) {
      Study study =  surveySystDao.getStudy(localSurveySystem.getSurveySystemId(), title);
      if (study == null) {
        logger.warn("Study: '"+title+"' was not found in the database...");
        return -1;
      }
      return study.getStudyCode();
    }


    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      // Protect score
      int protectAnswers = 0;
      int protectSum = 0;
      for(String question : protectQuestions) {
        if (chartScore.isAnswered(question)) {
          protectAnswers = protectAnswers + 1;
          int ans = chartScore.getAnswer(question).intValue();
          protectSum = protectSum + ans;
        }
      }
      Double protectScore = null;
      if (protectAnswers >= 12) {
        protectScore = (double)protectSum / (double)protectAnswers;
      }

      // Minimize score
      int minimizeAnswers = 0;
      int minimizeSum = 0;
      for(String question : minimizeQuestions) {
        if (chartScore.isAnswered(question)) {
          minimizeAnswers = minimizeAnswers + 1;
          int ans = chartScore.getAnswer(question).intValue();
          minimizeSum = minimizeSum + ans;
        }
      }
      Double minimizeScore = null;
      if (minimizeAnswers >= 4) {
        minimizeScore = (double)minimizeSum / (double)minimizeAnswers;
      }

      // Distract score
      int distractAnswers = 0;
      int distractSum = 0;
      for(String question : distractQuestions) {
        if (chartScore.isAnswered(question)) {
          distractAnswers = distractAnswers + 1;
          int ans = chartScore.getAnswer(question).intValue();
          distractSum = distractSum + ans;
        }
      }
      Double distractScore = null;
      if (distractAnswers >= 6) {
        distractScore = (double)distractSum / (double)distractAnswers;
      }

      // Total score
      Double totalScore = null;
      if ((protectScore != null) && (minimizeScore != null) && (distractScore != null)) {
        totalScore = (protectScore + minimizeScore + distractScore)/3.0;
      }
      
      chartScore.setScore(new BigDecimal(0));

      Map<String,BigDecimal> scores = new LinkedHashMap<>();
      if (protectScore != null) {
        scores.put("Protect", new BigDecimal(protectScore).setScale(2, BigDecimal.ROUND_HALF_UP));
      }
      if (minimizeScore != null) {
        scores.put("Minimize", new BigDecimal(minimizeScore).setScale(2, BigDecimal.ROUND_HALF_UP));
      }
      if (distractScore != null) {
        scores.put("Distract", new BigDecimal(distractScore).setScale(2, BigDecimal.ROUND_HALF_UP));
      }
      if (totalScore != null) {
        scores.put("Total", new BigDecimal(totalScore).setScale(2, BigDecimal.ROUND_HALF_UP));
      }
      chartScore.setScores(scores);
    }

    @Override
    public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies,
        PrintStudy study, Patient patient) {
      ArrayList<ChartScore> scores = new ArrayList<>();
      for(PatientStudyExtendedData patientStudy : patientStudies) {
        // Include both ARCSPainResponse and ARCSProtect studies in the table
        int code = (patientStudy.getStudyCode() == null) ? -2 : patientStudy.getStudyCode().intValue();
        if (code == studyARCSPainResponseCode || code == studyARCSProtectCode) {
          ChartScore score = getScoreInternal(patientStudy);
          if (score != null) {
            scores.add(score);
          }
        }
      }
      return getTableInternal(study, scores, false);
    }

    @Override
    protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow) {
      Table table = new Table();
      if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
        return table;
      }

      table.addHeading(study.getTitle());

      TableRow colHeader = new TableRow(100);
      colHeader.addColumn(new TableColumn("Date", 20));
      colHeader.addColumn(new TableColumn("Protect", 20));
      colHeader.addColumn(new TableColumn("Minimize", 20));
      colHeader.addColumn(new TableColumn("Distract", 20));
      colHeader.addColumn(new TableColumn("Total", 20));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
        Map<String,BigDecimal> scoreValues = score.getScores();
        BigDecimal protectScore = scoreValues.get("Protect");
        if (protectScore != null) {          
          row.addColumn(new TableColumn(arcsScoreFormatter.format(protectScore), 20));
        } else {
          row.addColumn(new TableColumn("", 20));
        }
        BigDecimal minimizeScore = scoreValues.get("Minimize");
        if (minimizeScore != null) {          
          row.addColumn(new TableColumn(arcsScoreFormatter.format(minimizeScore), 20));
        } else {
          row.addColumn(new TableColumn("", 20));
        }
        BigDecimal distractScore = scoreValues.get("Distract");
        if (distractScore != null) {          
          row.addColumn(new TableColumn(arcsScoreFormatter.format(distractScore), 20));
        } else {
          row.addColumn(new TableColumn("", 20));
        }
        BigDecimal totalScore = scoreValues.get("Total");
        if (totalScore != null) {          
          row.addColumn(new TableColumn(arcsScoreFormatter.format(totalScore), 20));
        } else {
          row.addColumn(new TableColumn("", 20));
        }
        table.addRow(row);
      }

      return table;
    }

    @Override
    public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> scores,
        PrintStudy study, ChartConfigurationOptions opts) {
      final TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(baseLineSeries);
      if (scores == null || study == null) {
        return dataset;
      }

      TimeSeries timeDataSetProtect = new TimeSeries("Protect");
      TimeSeries timeDataSetMinimize = new TimeSeries("Minimize");
      TimeSeries timeDataSetDistract = new TimeSeries("Distact");
      for (ChartScore score : scores) {
        Map<String,BigDecimal> scoreValues = score.getScores();

        Day day = new Day(score.getDate());
        BigDecimal protect = scoreValues.get("Protect");
        if (protect != null) {
          timeDataSetProtect.addOrUpdate(day, protect);
        }
        BigDecimal minimize = scoreValues.get("Minimize");
        if (minimize != null) {
          timeDataSetMinimize.addOrUpdate(day, minimize);
        }
        BigDecimal distract = scoreValues.get("Distract");
        if (distract != null) {
          timeDataSetDistract.addOrUpdate(day, distract);
        }
      }
      dataset.addSeries(timeDataSetProtect);
      dataset.addSeries(timeDataSetMinimize);
      dataset.addSeries(timeDataSetDistract);
      return dataset;
    }
    
    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = new CustomDateAxis(collection);
      Font labelFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.LABELFONTSIZE);
      Font dtTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.DTTICKFONTSIZE);
      domainAxis.setLabelFont(labelFont);
      domainAxis.setTickLabelPaint(Color.black);
      domainAxis.setTickLabelFont(dtTickFont);
      domainAxis.setTickLabelInsets(new RectangleInsets(5, 60, 5, 5));
      Font numTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.NUMTICKFONTSIZE);
      rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      rangeAxis.setLabelFont(labelFont);
      rangeAxis.setTickLabelFont(numTickFont);
      rangeAxis.setTickUnit(new NumberTickUnit(1));
      rangeAxis.setRange(0, 4);
      ChartMaker.setRendererOptions(renderer, opts, ds.getSeriesCount());
      final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
      plot.setDomainGridlinePaint(Color.black);
      plot.setDomainGridlinesVisible(false);
      plot.setRangeGridlinePaint(Color.black);
      plot.setBackgroundPaint(Color.white);
      plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
      plot.setOutlineStroke(new BasicStroke(0.0f));
      plot.setInsets(ChartMaker.CHART_INSETS);

      ValueMarker label = new ValueMarker(3.8);
      label.setLabel("More frequent behavior");
      label.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      label.setLabelAnchor(RectangleAnchor.LEFT);
      label.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      label.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(label, Layer.BACKGROUND);

      return plot;
    }
  }

  static class ARCSScore extends LocalScore implements MultiScore {

    public ARCSScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    @Override
    public int getNumberOfScores() {
      return 3;
    }

    @Override
    public String getTitle(int scoreNumber, String studyDescription) {
      switch(scoreNumber) {
      case 1:
        return studyDescription + " - Protect";
      case 2:
        return studyDescription + " - Minimize";
      case 3:
        return studyDescription + " - Distract";
      default:
        return studyDescription;
      }
    }

    @Override
    public double getScore(int scoreNumber) {
      Map<String,BigDecimal> scoreValues = getScores();
      BigDecimal score;
      switch(scoreNumber) {
      case 1:
        score = scoreValues.get("Protect");
        break;
      case 2:
        score = scoreValues.get("Minimize");
        break;
      case 3:
        score = scoreValues.get("Distract");
        break;
      default:
        score = null;
      }
      return (score != null) ? score.doubleValue() : 0;
    }

    @Override
    public Double getPercentileScore(int scoreNumber) {
      return null;
    }
  }

  /**
   * Score provider for ARCS - Protect
   */
  static class ARCSProtectScoreProvider extends ARCSScoreProvider {

    public ARCSProtectScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected ChartScore getScoreInternal(PatientStudyExtendedData patientData) {
      LocalScore localScore = new ARCSProtectScore(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription());
      localScore.setAssisted(patientData.wasAssisted());

      loadAnswers(localScore, patientData);
      calculateScore(localScore, patientData);

      return localScore;
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
      colHeader.addColumn(new TableColumn("Protect", 50));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 50));
        Map<String,BigDecimal> scoreValues = score.getScores();
        BigDecimal protectScore = scoreValues.get("Protect");
        if (protectScore != null) {          
          row.addColumn(new TableColumn(OhsuScoreProviderFactory.ARCSScoreProvider.arcsScoreFormatter.format(protectScore), 50));
        } else {
          row.addColumn(new TableColumn("", 50));
        }
        table.addRow(row);
      }

      return table;
    }

    @Override
    public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> scores,
        PrintStudy study, ChartConfigurationOptions opts) {
      final TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(baseLineSeries);
      if (scores == null || study == null) {
        return dataset;
      }

      TimeSeries timeDataSetProtect = new TimeSeries("Protect");
      for (ChartScore score : scores) {
        Map<String,BigDecimal> scoreValues = score.getScores();

        Day day = new Day(score.getDate());
        BigDecimal protect = scoreValues.get("Protect");
        if (protect != null) {
          timeDataSetProtect.addOrUpdate(day, protect);
        }
      }
      dataset.addSeries(timeDataSetProtect);
      return dataset;
    }
  }

  static class ARCSProtectScore extends LocalScore {

    public ARCSProtectScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    @Override
    public BigDecimal getScore() {
      Map<String,BigDecimal> scores = getScores();
      return scores.get("Protect");
    }
  }

  /**
   * Score provider for Pain Catastrophizing Scale
   */
  static class PCSScoreProvider extends ExtensibleScoreProvider {

    public PCSScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
      ArrayList<ChartScore> localScores = super.getScore(patientData);
      ArrayList<ChartScore> pcsScores = new ArrayList<>();
      for (ChartScore score : localScores) {
        PCSScore pcsScore = new PCSScore(score.getDate(), score.getPatientId(), score.getStudyCode().hashCode(),
            score.getStudyDescription(), score.getScore(), score.getScores(), score.getAssisted(), score.wasReplaced());
        pcsScores.add(pcsScore);
      }
      return pcsScores;
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = new CustomDateAxis(collection);
      Font labelFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.LABELFONTSIZE);
      Font dtTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.DTTICKFONTSIZE);
      domainAxis.setLabelFont(labelFont);
      domainAxis.setTickLabelPaint(Color.black);
      domainAxis.setTickLabelFont(dtTickFont);
      domainAxis.setTickLabelInsets(new RectangleInsets(5, 60, 5, 5));
      Font numTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.NUMTICKFONTSIZE);
      rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      rangeAxis.setLabelFont(labelFont);
      rangeAxis.setTickLabelFont(numTickFont);
      rangeAxis.setTickUnit(new NumberTickUnit(13));
      rangeAxis.setRange(0, 52);
      ChartMaker.setRendererOptions(renderer, opts, ds.getSeriesCount());
      final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
      plot.setDomainGridlinePaint(Color.black);
      plot.setDomainGridlinesVisible(false);
      plot.setRangeGridlinePaint(Color.black);
      plot.setBackgroundPaint(Color.white);
      plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
      plot.setOutlineStroke(new BasicStroke(0.0f));
      plot.setInsets(ChartMaker.CHART_INSETS);

      ValueMarker cutoffLine = new ValueMarker(23);
      cutoffLine.setStroke(new BasicStroke(1.2f));
      cutoffLine.setPaint(Color.BLACK);
      plot.addRangeMarker(cutoffLine);
      GradientPaint paint = new GradientPaint(
          0, 100, opts.getOptionColor(ConfigurationOptions.OPTION_BANDING_COLOR_1),
          0, 0, opts.getOptionColor(ConfigurationOptions.OPTION_BANDING_COLOR_2));
      IntervalMarker band = new IntervalMarker(23, 52, paint);
      band.setGradientPaintTransformer(new StandardGradientPaintTransformer());
      plot.addRangeMarker(band, Layer.BACKGROUND);

      ValueMarker worstLabel = new ValueMarker(50);
      worstLabel.setLabel("Worse");
      worstLabel.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      worstLabel.setLabelAnchor(RectangleAnchor.LEFT);
      worstLabel.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      worstLabel.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(worstLabel, Layer.BACKGROUND);

      return plot;
    }
  }

  static class PCSScore extends LocalScore {

    public PCSScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    PCSScore(Date dt, String patientId, Integer studyCode, String description,
             BigDecimal score, Map<String, BigDecimal> scores,
             boolean assisted, boolean wasReplaced) {
      super(dt, patientId, studyCode, description, score, scores, assisted, wasReplaced);
    }

    @Override
    public String getCategoryLabel() {
      BigDecimal score = getScore();
      // Child
      if ("OhsuCatastrophizingScale".equals(getStudyDescription())) {
        if (score.doubleValue() >= 26) {
          return "High";
        }
        if (score.doubleValue() >= 15) { // 15-25
          return "Moderate";
        }
        return "Low"; // 0-14
      }
      // Parent
      if (score.doubleValue() >= 23) {
        return "High";
      }
      return "";
    }
  }

  /**
   * Score provider for Self Efficacy
   */
  static class SEScoreProvider extends ExtensibleScoreProvider {

    public SEScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = new CustomDateAxis(collection);
      Font labelFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.LABELFONTSIZE);
      Font dtTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.DTTICKFONTSIZE);
      domainAxis.setLabelFont(labelFont);
      domainAxis.setTickLabelPaint(Color.black);
      domainAxis.setTickLabelFont(dtTickFont);
      domainAxis.setTickLabelInsets(new RectangleInsets(5, 60, 5, 5));
      Font numTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.NUMTICKFONTSIZE);
      rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      rangeAxis.setLabelFont(labelFont);
      rangeAxis.setTickLabelFont(numTickFont);
      rangeAxis.setTickUnit(new NumberTickUnit(7));
      rangeAxis.setRange(7, 35);
      ChartMaker.setRendererOptions(renderer, opts, ds.getSeriesCount());
      final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
      plot.setDomainGridlinePaint(Color.black);
      plot.setDomainGridlinesVisible(false);
      plot.setRangeGridlinePaint(Color.black);
      plot.setBackgroundPaint(Color.white);
      plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
      plot.setOutlineStroke(new BasicStroke(0.0f));
      plot.setInsets(ChartMaker.CHART_INSETS);

      ValueMarker worstLabel = new ValueMarker(9);
      worstLabel.setLabel("Greater Self-Efficacy");
      worstLabel.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      worstLabel.setLabelAnchor(RectangleAnchor.LEFT);
      worstLabel.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      worstLabel.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(worstLabel, Layer.BACKGROUND);

      return plot;
    }
  }

  /**
   * Score provider for Family Assessment Device
   */
  static class FADScoreProvider extends ExtensibleScoreProvider {
    protected final DecimalFormat decimalScoreFormatter = new DecimalFormat("##.##");

    public FADScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      List<BigDecimal> ansScores = chartScore.getAnswers();
      int numberAnswers = ansScores.size();
      double total = 0;
      for(BigDecimal ansScore : ansScores) {
        total += ansScore.doubleValue();
      }
      double score = total/numberAnswers;
      chartScore.setScore(new BigDecimal(score));
    }

    @Override
    protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow) {
      Table table = new Table();
      if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
        return table;
      }

      table.addHeading(study.getTitle());
      if (headerRow) {
        TableRow colHeader = new TableRow(100);
        colHeader.setColumnGap(3);
        colHeader.addColumn(new TableColumn("Date", 65));
        colHeader.addColumn(new TableColumn("Score", 31));
        table.addRow(colHeader);
      }
      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 65));
        row.addColumn(new TableColumn(decimalScoreFormatter.format(score.getScore()), 31));
        table.addRow(row);
      }

      return table;
    }
  }

  /**
   * Score provider for PedsQL (long form)
   */
  static class PedsQL2ScoreProvider extends ExtensibleScoreProvider {
    
    private static final String[] physicalQuestions = new String[] { "physical1", "physical2", "physical3",
      "physical4", "physical5", "physical6", "physical7", "physical8" };
    private static final String[] emotionalQuestions = new String[] { "emotional1", "emotional2", "emotional3", "emotional4", "emotional5" };
    private static final String[] socialQuestions = new String[] { "social1", "social2", "social3", "social4", "social5" };
    private static final String[] schoolQuestions = new String[] { "school1", "school2", "school3", "school4", "school5" };

    public PedsQL2ScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected ChartScore getScoreInternal(PatientStudyExtendedData patientData) {
      int age = DateUtils.getAge(patientData.getPatient().getDtBirth());
      LocalScore localScore = new PedsQLScore(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription(), age);
      localScore.setAssisted(patientData.wasAssisted());

      loadAnswers(localScore, patientData);
      calculateScore(localScore, patientData);

      return localScore;
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      // Physical  score
      int physicalAnswers = 0;
      int physicalSum = 0;
      for(String question : physicalQuestions) {
        if (chartScore.isAnswered(question)) {
          physicalAnswers = physicalAnswers + 1;
          int ans = chartScore.getAnswer(question).intValue();
          physicalSum = physicalSum + (4-ans)*25;
        }
      }
      Integer physicalScore = null;
      if (physicalAnswers >= 4) {
        physicalScore = physicalSum/physicalAnswers;
      }

      // Emotional  score
      int emotionalAnswers = 0;
      int emotionalSum = 0;
      for(String question : emotionalQuestions) {
        if (chartScore.isAnswered(question)) {
          emotionalAnswers = emotionalAnswers + 1;
          int ans = chartScore.getAnswer(question).intValue();
          emotionalSum = emotionalSum + (4-ans)*25;
        }
      }
      Integer emotionalScore = null;
      if (emotionalAnswers >= 3) {
        emotionalScore = emotionalSum/emotionalAnswers;
      }

      // Social  score
      int socialAnswers = 0;
      int socialSum = 0;
      for(String question : socialQuestions) {
        if (chartScore.isAnswered(question)) {
          socialAnswers = socialAnswers + 1;
          int ans = chartScore.getAnswer(question).intValue();
          socialSum = socialSum + (4-ans)*25;
        }
      }
      Integer socialScore = null;
      if (socialAnswers >= 3) {
        socialScore = socialSum/socialAnswers;
      }

      // School score
      int schoolAnswers = 0;
      int schoolSum = 0;
      for(String question : schoolQuestions) {
        if (chartScore.isAnswered(question)) {
          schoolAnswers = schoolAnswers + 1;
          int ans = chartScore.getAnswer(question).intValue();
          schoolSum = schoolSum + (4-ans)*25;
        }
      }
      Integer schoolScore = null;
      if (schoolAnswers >= 3) {
        schoolScore = schoolSum/schoolAnswers;
      }

      // Total score
      Integer totalScore = null;
      if ((physicalScore != null) && (emotionalScore != null) && (socialScore != null) && (schoolScore != null)) {
        int totalAnswers = physicalAnswers + emotionalAnswers + socialAnswers + schoolAnswers;
        int totalSum = physicalSum + emotionalSum + socialSum + schoolSum;
        totalScore = totalSum/totalAnswers;
      }
      
      chartScore.setScore(new BigDecimal(0));
      
      Map<String,BigDecimal> scores = new LinkedHashMap<>();
      if (physicalScore != null) {
        scores.put("Physical", new BigDecimal(physicalScore));
      }
      if (emotionalScore != null) {
        scores.put("Emotional", new BigDecimal(emotionalScore));
      }
      if (socialScore != null) {
        scores.put("Social", new BigDecimal(socialScore));
      }
      if (schoolScore != null) {
        scores.put("School", new BigDecimal(schoolScore));
      }
      if (totalScore != null) {
        scores.put("Total", new BigDecimal(totalScore));
      }
      chartScore.setScores(scores);
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
      colHeader.addColumn(new TableColumn("School", 50));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 50));
        Map<String,BigDecimal> scoreValues = score.getScores();
        BigDecimal schoolScore = scoreValues.get("School");
        if (schoolScore != null) {          
          row.addColumn(new TableColumn(scoreFormatter.format(schoolScore), 00));
        } else {
          row.addColumn(new TableColumn("", 50));
        }
        table.addRow(row);
      }

      return table;
    }

    @Override
    public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> scores,
        PrintStudy study, ChartConfigurationOptions opts) {
      final TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(baseLineSeries);
      if (scores == null || study == null) {
        return dataset;
      }

      TimeSeries timeDataSetSchool = new TimeSeries("School");
      for (ChartScore score : scores) {
        Map<String,BigDecimal> scoreValues = score.getScores();

        Day day = new Day(score.getDate());
        BigDecimal school = scoreValues.get("School");
        if (school != null) {
          timeDataSetSchool.addOrUpdate(day, school);
        }
      }
      dataset.addSeries(timeDataSetSchool);
      return dataset;
    }
    
    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = new CustomDateAxis(collection);
      Font labelFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.LABELFONTSIZE);
      Font dtTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.DTTICKFONTSIZE);
      domainAxis.setLabelFont(labelFont);
      domainAxis.setTickLabelPaint(Color.black);
      domainAxis.setTickLabelFont(dtTickFont);
      domainAxis.setTickLabelInsets(new RectangleInsets(5, 60, 5, 5));
      Font numTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.NUMTICKFONTSIZE);
      rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      rangeAxis.setLabelFont(labelFont);
      rangeAxis.setTickLabelFont(numTickFont);
      rangeAxis.setTickUnit(new NumberTickUnit(10));
      rangeAxis.setRange(0, 100);
      ChartMaker.setRendererOptions(renderer, opts, ds.getSeriesCount());
      final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
      plot.setDomainGridlinePaint(Color.black);
      plot.setDomainGridlinesVisible(false);
      plot.setRangeGridlinePaint(Color.black);
      plot.setBackgroundPaint(Color.white);
      plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
      plot.setOutlineStroke(new BasicStroke(0.0f));
      plot.setInsets(ChartMaker.CHART_INSETS);

      ValueMarker cutoffLine = new ValueMarker(63);
      cutoffLine.setStroke(new BasicStroke(1.2f));
      cutoffLine.setPaint(Color.BLACK);
      plot.addRangeMarker(cutoffLine);
      
      GradientPaint paint = new GradientPaint(
          0, 100, opts.getOptionColor(ConfigurationOptions.OPTION_BANDING_COLOR_2),
          0, 0, opts.getOptionColor(ConfigurationOptions.OPTION_BANDING_COLOR_1));
      IntervalMarker band = new IntervalMarker(0, 63, paint);
      band.setGradientPaintTransformer(new StandardGradientPaintTransformer());
      plot.addRangeMarker(band, Layer.BACKGROUND);
      
      ValueMarker worstLabel = new ValueMarker(5);
      worstLabel.setLabel("Worse");
      worstLabel.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      worstLabel.setLabelAnchor(RectangleAnchor.LEFT);
      worstLabel.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      worstLabel.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(worstLabel, Layer.BACKGROUND);

      return plot;
    }
  }

  static class PedsQLScore extends LocalScore implements MultiScore {
    int age;
    PedsQLScore(Date dt, String patientId, Integer studyCode, String description, int age) {
      super(dt, patientId, studyCode, description);
      this.age = age;
    }

    @Override
    public int getNumberOfScores() {
      return 1;
    }

    @Override
    public String getTitle(int scoreNumber, String studyDescription) {
      switch(scoreNumber) {
      case 1:
        return studyDescription + " - School";
      default:
        return studyDescription;
      }
    }

    @Override
    public double getScore(int scoreNumber) {
      Map<String,BigDecimal> scoreValues = getScores();
      BigDecimal score;
      switch(scoreNumber) {
      case 1:
        score = scoreValues.get("School");
        break;
      default:
        score = null;
      }
      return (score != null) ? score.doubleValue() : 0;
    }

    @Override
    public Double getPercentileScore(int scoreNumber) {
      return null;
    }

    @Override
    public String getCategoryLabel() {
      double score = getScore(1);
      //For children <8, <78 is at risk
      //For children >8, <82 is at risk
      if ((age < 8 && score < 78) || (age >= 8 && score < 82)) {
        return "At-Risk";
      }
      return "";
    }
  }

  /**
   * Score provider for FOPQ
   */
  static class FOPQScoreProvider extends ExtensibleScoreProvider {

    private static final String[] fearQuestions = new String[] {"Q1", "Q2", "Q3", "Q5", "Q6"};
    private static final String[] avoidanceQuestions = new String[] {"Q4", "Q7", "Q8", "Q9", "Q10"};

    public FOPQScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected ChartScore getScoreInternal(PatientStudyExtendedData patientData) {
      LocalScore localScore = new FOPQScore(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription());
      localScore.setAssisted(patientData.wasAssisted());

      loadAnswers(localScore, patientData);
      calculateScore(localScore, patientData);

      return localScore;
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      // Fear score
      int fearAnswers = 0;
      int fearSum = 0;
      for(String question : fearQuestions) {
        if (chartScore.isAnswered(question)) {
          fearAnswers = fearAnswers + 1;
          int ans = chartScore.getAnswer(question).intValue();
          fearSum = fearSum + ans;
        }
      }
      Integer fearScore = null;
      if (fearAnswers >= fearQuestions.length) {
        fearScore = fearSum;
      }

      // Avoidance  score
      int avoidanceAnswers = 0;
      int avoidanceSum = 0;
      for(String question : avoidanceQuestions) {
        if (chartScore.isAnswered(question)) {
          avoidanceAnswers = avoidanceAnswers + 1;
          int ans = chartScore.getAnswer(question).intValue();
          avoidanceSum = avoidanceSum + ans;
        }
      }
      Integer avoidanceScore = null;
      if (avoidanceAnswers >= avoidanceQuestions.length) {
        avoidanceScore = avoidanceSum;
      }

      // Total score
      Integer totalScore = null;
      if ((fearScore != null) && (avoidanceScore != null)) {
        totalScore = fearScore + avoidanceScore;
      }
      
      chartScore.setScore(new BigDecimal(0));

      Map<String,BigDecimal> scores = new LinkedHashMap<>();
      if (fearScore != null) {
        scores.put("Fear", new BigDecimal(fearScore));
      }
      if (avoidanceScore != null) {
        scores.put("Avoidance", new BigDecimal(avoidanceScore));
      }
      if (totalScore != null) {
        scores.put("Total", new BigDecimal(totalScore));
      }
      chartScore.setScores(scores);
    }

    @Override
    protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow) {
      Table table = new Table();
      if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
        return table;
      }

      table.addHeading(study.getTitle());

      TableRow colHeader = new TableRow(100);
      colHeader.addColumn(new TableColumn("Date", 25));
      colHeader.addColumn(new TableColumn("Fear", 25));
      colHeader.addColumn(new TableColumn("Avoidance", 25));
      colHeader.addColumn(new TableColumn("Total", 25));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 25));
        Map<String,BigDecimal> scoreValues = score.getScores();
        BigDecimal fearScore = scoreValues.get("Fear");
        if (fearScore != null) {          
          row.addColumn(new TableColumn(scoreFormatter.format(fearScore), 25));
        } else {
          row.addColumn(new TableColumn("", 25));
        }
        BigDecimal avoidanceScore = scoreValues.get("Avoidance");
        if (avoidanceScore != null) {          
          row.addColumn(new TableColumn(scoreFormatter.format(avoidanceScore), 25));
        } else {
          row.addColumn(new TableColumn("", 25));
        }
        BigDecimal totalScore = scoreValues.get("Total");
        if (totalScore != null) {          
          row.addColumn(new TableColumn(scoreFormatter.format(totalScore), 25));
        } else {
          row.addColumn(new TableColumn("", 25));
        }
        table.addRow(row);
      }

      return table;
    }

    @Override
    public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> scores,
        PrintStudy study, ChartConfigurationOptions opts) {
      final TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(baseLineSeries);
      if (scores == null || study == null) {
        return dataset;
      }

      TimeSeries timeDataSetTotal = new TimeSeries("Total");
      TimeSeries timeDataSetFear = new TimeSeries("Fear");
      TimeSeries timeDataSetAvoidance = new TimeSeries("Avoidance");
      for (ChartScore score : scores) {
        Map<String,BigDecimal> scoreValues = score.getScores();

        Day day = new Day(score.getDate());
        BigDecimal fear = scoreValues.get("Fear");
        if (fear != null) {
          timeDataSetFear.addOrUpdate(day, fear);
        }
        BigDecimal avoidance = scoreValues.get("Avoidance");
        if (avoidance != null) {
          timeDataSetAvoidance.addOrUpdate(day, avoidance);
        }
        BigDecimal total = scoreValues.get("Total");
        if (total != null) {
          timeDataSetTotal.addOrUpdate(day, total);
        }
      }
      dataset.addSeries(timeDataSetTotal);
      dataset.addSeries(timeDataSetFear);
      dataset.addSeries(timeDataSetAvoidance);
      return dataset;
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = new CustomDateAxis(collection);
      Font labelFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.LABELFONTSIZE);
      Font dtTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.DTTICKFONTSIZE);
      domainAxis.setLabelFont(labelFont);
      domainAxis.setTickLabelPaint(Color.black);
      domainAxis.setTickLabelFont(dtTickFont);
      domainAxis.setTickLabelInsets(new RectangleInsets(5, 60, 5, 5));
      Font numTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.NUMTICKFONTSIZE);
      rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      rangeAxis.setLabelFont(labelFont);
      rangeAxis.setTickLabelFont(numTickFont);
      rangeAxis.setTickUnit(new NumberTickUnit(10));
      rangeAxis.setRange(0, 40);
      ChartMaker.setRendererOptions(renderer, opts, ds.getSeriesCount());
      final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
      plot.setDomainGridlinePaint(Color.black);
      plot.setDomainGridlinesVisible(false);
      plot.setRangeGridlinePaint(Color.black);
      plot.setBackgroundPaint(Color.white);
      plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
      plot.setOutlineStroke(new BasicStroke(0.0f));
      plot.setInsets(ChartMaker.CHART_INSETS);

      ValueMarker cutoffLine1 = new ValueMarker(16);
      cutoffLine1.setStroke(new BasicStroke(1.2f));
      cutoffLine1.setPaint(Color.BLACK);
      plot.addRangeMarker(cutoffLine1);
      IntervalMarker band1 = new IntervalMarker(16, 20,
          opts.getOptionColor(ConfigurationOptions.OPTION_BANDING_COLOR_2));
      plot.addRangeMarker(band1, Layer.BACKGROUND);
      IntervalMarker band2 = new IntervalMarker(20, 40,
          opts.getOptionColor(ConfigurationOptions.OPTION_BANDING_COLOR_1));
      plot.addRangeMarker(band2, Layer.BACKGROUND);
      
      ValueMarker atriskLabel = new ValueMarker(18);
      atriskLabel.setLabel("At risk");
      atriskLabel.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      atriskLabel.setLabelAnchor(RectangleAnchor.LEFT);
      atriskLabel.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      atriskLabel.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(atriskLabel, Layer.BACKGROUND);

      ValueMarker elevatedLabel = new ValueMarker(25);
      elevatedLabel.setLabel("Elevated Fear");
      elevatedLabel.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      elevatedLabel.setLabelAnchor(RectangleAnchor.LEFT);
      elevatedLabel.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      elevatedLabel.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(elevatedLabel, Layer.BACKGROUND);

      return plot;
    }
  }

  static class FOPQScore extends LocalScore implements MultiScore {

    public FOPQScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    @Override
    public int getNumberOfScores() {
      return 3;
    }

    @Override
    public String getTitle(int scoreNumber, String studyDescription) {
      switch(scoreNumber) {
      case 1:
        return studyDescription + " - Total";
      case 2:
        return studyDescription + " - Fear";
      case 3:
        return studyDescription + " - Avoidance";
      default:
        return studyDescription;
      }
    }

    @Override
    public double getScore(int scoreNumber) {
      Map<String,BigDecimal> scoreValues = getScores();
      BigDecimal score;
      switch(scoreNumber) {
      case 1:
        score = scoreValues.get("Total");
        break;
      case 2:
        score = scoreValues.get("Fear");
        break;
      case 3:
        score = scoreValues.get("Avoidance");
        break;
      default:
        score = null;
      }
      return (score != null) ? score.doubleValue() : 0;
    }

    @Override
    public Double getPercentileScore(int scoreNumber) {
      return null;
    }

    @Override
    public String getCategoryLabel() {
      double totalScore = getScore(1);
      if (totalScore >= 20) {
        return "Elevated";
      }
      if (totalScore >= 16) {
        return "At-Risk";
      }
      return "";
    }
  }

  /**
   * Score provider for PROMIS Sleep Disturbance
   */
  static class SleepDisturbanceScoreProvider extends ExtensibleScoreProvider {

    public SleepDisturbanceScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      ArrayList<BigDecimal> answers = chartScore.getAnswers();
      long sum = 0;
      for(BigDecimal answer : answers) {
        sum = sum + answer.longValue();
      }

      long score;
      if (answers.size() == 8) {
        score = sum;
      } else if (answers.size() >= 6) {
        score = Math.round((sum * 8.0)/answers.size());
      } else {
        score = 0;
      }
      
      chartScore.setScore(new BigDecimal(score));
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = new CustomDateAxis(collection);
      Font labelFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.LABELFONTSIZE);
      Font dtTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.DTTICKFONTSIZE);
      domainAxis.setLabelFont(labelFont);
      domainAxis.setTickLabelPaint(Color.black);
      domainAxis.setTickLabelFont(dtTickFont);
      domainAxis.setTickLabelInsets(new RectangleInsets(5, 60, 5, 5));
      Font numTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.NUMTICKFONTSIZE);
      rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      rangeAxis.setLabelFont(labelFont);
      rangeAxis.setTickLabelFont(numTickFont);
      rangeAxis.setTickUnit(new NumberTickUnit(5));
      rangeAxis.setRange(0, 40);
      ChartMaker.setRendererOptions(renderer, opts, ds.getSeriesCount());
      final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
      plot.setDomainGridlinePaint(Color.black);
      plot.setDomainGridlinesVisible(false);
      plot.setRangeGridlinePaint(Color.black);
      plot.setBackgroundPaint(Color.white);
      plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
      plot.setOutlineStroke(new BasicStroke(0.0f));
      plot.setInsets(ChartMaker.CHART_INSETS);

      return plot;
    }
  }

  /**
   * Score provider for research question
   */
  static class ResearchScoreProvider extends ExtensibleScoreProvider {

    public ResearchScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    public ArrayList<SurveyQuestionIntf> getSurvey(
        PatientStudyExtendedData patStudy, PrintStudy study, Patient patient, boolean allAnswers) {

      // Get the current patient attribute value rather than the survey answer. The survey answer
      // sets the patient attribute, but the patient attribute may have been manually changed
      // after the survey. Show the current value.
      PatientAttribute attr = patient.getAttribute("research");
      String value = "N/A";
      if ((attr != null) && (attr.getDataValue() != null)) {
        if (attr.getDataValue().equalsIgnoreCase("y")) {
          value = "Yes";
        } else if (attr.getDataValue().equalsIgnoreCase("n")) {
          value = "No";
        }
      }
      
      ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();

      ArrayList<SurveyAnswerIntf> answers = new ArrayList<>();
      RegistryAnswer a = new InputElement();
      a.setLabel("");
      a.setValue(value);
      answers.add(a);

      RegistryQuestion q = new RegistryQuestion();
      q.addText("Would you like to be contacted about future educational, group, or research opportunities?");
      q.setAnswered(true);
      q.setAnswers(answers);
      questions.add(q);

      return questions;
    }
  }

}
