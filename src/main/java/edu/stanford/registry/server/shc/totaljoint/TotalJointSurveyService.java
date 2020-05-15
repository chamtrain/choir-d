/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.shc.totaljoint;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.charts.ChartMaker;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveyRegistrationAttributeDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.ExtensibleScoreProvider;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.RegistryAssessmentsService;
import edu.stanford.registry.server.survey.SurveyServiceIntf;
import edu.stanford.registry.server.tools.CustomDateAxis;
import edu.stanford.registry.server.utils.EmailTemplateUtils;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.MultiScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.RegistryQuestion;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.xform.InputElement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import com.github.susom.database.Database;

/**
 * SurveyServiceInf implementation for TotalJoint.
 */
public class TotalJointSurveyService extends RegistryAssessmentsService
    implements SurveyServiceIntf {

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

  private static Study emailAddressStudy = null;
  private static Study consentStudy = null;

  public TotalJointSurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  protected void assessmentCompleted(Database database, Patient patient, PatientStudy patStudy) {
    if (emailAddressStudy == null) {
      emailAddressStudy = getStudy(database, "EmailAddress");
    }
    if (consentStudy == null) {
      consentStudy = getStudy(database, "TJAOutcomesStudyConsent");
    }

    if ((emailAddressStudy != null) &&
        (patStudy.getStudyCode().intValue() == emailAddressStudy.getStudyCode().intValue()) ) {
      handleEmailAddr(database, patient, patStudy);
    }

    if ((consentStudy != null) &&
        (patStudy.getStudyCode().intValue() == consentStudy.getStudyCode().intValue()) ) {
      handleConsent(patient, patStudy);
    }
  }

  private void handleEmailAddr(Database database, Patient patient, PatientStudy patStudy) {
    // Get the XML content
    String xmlString = patStudy.getContents();

    // Get the email response value
    String email = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='email']/value");

    // Set the email attribute if an email address was provided in the
    // EmailAddress survey
    if ((email != null) && !email.equals("")) {
      PatientAttribute emailAttr = new PatientAttribute(
          patient.getPatientId(),Constants.ATTRIBUTE_SURVEYEMAIL_ALT,email,PatientAttribute.STRING);

      User admin = ServerUtils.getAdminUser(database);
      PatientDao patientDao = new PatientDao(database, siteId, admin);
      patientDao.insertAttribute(emailAttr);
    }
  }

  private void handleConsent(Patient patient, PatientStudy patStudy) {
    // Get the XML content
    String xmlString = patStudy.getContents();

    // Look up the ItemResponse value for the Item element with ref='consent'
    String itemResponse = XMLFileUtils.xPathQuery(xmlString, "//Item[@ref='consent']/@ItemResponse");

    // The Yes response has response order 0. If Yes send the TJAOutcomesConsent email
    if ((itemResponse != null) && itemResponse.equals("0")) {
      String emailAddr = patient.getEmailAddress();

      if (emailAddr != null) {
        Mailer mailer = siteInfo.getMailer();
        EmailTemplateUtils emailUtils = new EmailTemplateUtils();

        String template = emailUtils.getTemplate(siteInfo, "TJAOutcomesConsent");
        String subject = emailUtils.getEmailSubject(template);
        String body = emailUtils.getEmailBody(template);

        List<File> attachments = new ArrayList<>();
        URL url = getClass().getClassLoader().getResource("shc/totaljoint/ResearchInformationSheet.pdf");
        if (url == null ) {
          throw new RuntimeException("Did not find shc/totaljoint/ResearchInformationSheet.pdf file");
        }
        attachments.add(new File(url.getFile()));
        url = getClass().getClassLoader().getResource("shc/totaljoint/HipaaAuthorization.pdf");
        if (url == null) {
          throw new RuntimeException("Did not find shc/totaljoint/HipaaAuthorization.pdf file");
        }
        attachments.add(new File(url.getFile()));

        try {
          mailer.sendTextWithAttachment(emailAddr, null, null, subject, body, attachments);
        } catch (Exception ex) {
          throw new RuntimeException("ERROR trying to send mail TJAOutcomesConsent to " + emailAddr, ex);
        }
      }
    }
  }

  /**
   * Return the appropriate score provider based on the study name.
   */
  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    if (studyName.startsWith("KOOS") ||
        studyName.startsWith("HOOS")) {
      return new KOOSScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("KneeSocietyFunctionAids")) {
      return new KSFunctionAidsScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("KneeSocietyFunction")) {
      return new KSFunctionScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.startsWith("LeftKOOSJR") ||
        studyName.startsWith("RightKOOSJR") ||
        studyName.startsWith("LeftHOOSJR") ||
        studyName.startsWith("RightHOOSJR")) {
      return new KOOSJRScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.startsWith("LeftKOOS") ||
        studyName.startsWith("RightKOOS") ||
        studyName.startsWith("LeftHOOS") ||
        studyName.startsWith("RightHOOS")) {
      return new KOOSScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("LeftKneeSocietyFunction2") ||
               studyName.equals("RightKneeSocietyFunction2")) {
      return new KSFunction2ScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("LeftHarrisHipPain") ||
               studyName.equals("RightHarrisHipPain")) {
      return new HarrisScoreProvider(dbp, siteInfo, studyName, 1);
    } else if (studyName.equals("LeftHarrisHipFunction") ||
        studyName.equals("RightHarrisHipFunction")) {
      return new HarrisScoreProvider(dbp, siteInfo, studyName, 7);
    } else if (studyName.equals("VR12")) {
      return new VR12ScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("UCLAActivityScore")) {
      return new UCLAScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("SurveyInfo")) {
      return new SurveyInfoScoreProvider(dbp, siteInfo, studyName);
    } else {
      return new ExtensibleScoreProvider(dbp, siteInfo, studyName);
    }
  }

  /**
   * Add the surgery date to the baseline series (collection.get(0)) so
   * that the surgery date will be included in the chart domain axis.
   */
  protected static CustomDateAxis getDateAxis(TimeSeriesCollection collection) {
    CustomDateAxis dateAxis;
    Date surgeryDate = TotalJointPatientReport.getSurgeryDate();
    if (surgeryDate == null) {
      dateAxis = new CustomDateAxis(collection);
    } else {
      TimeSeries timeDataSet = collection.getSeries(0);
      timeDataSet.add(new Day(surgeryDate), 0);
      dateAxis = new CustomDateAxis(collection);
    }
    return dateAxis;
  }

  /**
   * Add a dashed line to indicate the surgery date.
   */
  protected static void addSurgeryDateMarker(XYPlot plot) {
    Date surgeryDate = TotalJointPatientReport.getSurgeryDate();
    if (surgeryDate != null) {
      ValueMarker cutoffLine = new ValueMarker(surgeryDate.getTime());
      cutoffLine.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {10}, 0));
      cutoffLine.setPaint(Color.BLACK);
      plot.addDomainMarker(cutoffLine);
    }
  }

  /**
   * Score provider for KOOS/HOOS.
   *
   * The KOOS/HOOS score is calculated as the average of all the answered
   * questions. The average is in the range 0-4. This is multiplied by 25
   * to produce a range of 0-100. The final score is inverted to produce
   * a range of 100 (good) to 0 (bad).
   */
  static class KOOSScoreProvider extends ExtensibleScoreProvider {

    public KOOSScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
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
      double score = 100.0 - ((total/numberAnswers) * 25.0);
      chartScore.setScore(new BigDecimal(score));
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("Score");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = getDateAxis(collection);
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
      addSurgeryDateMarker(plot);

      return plot;
    }
  }

  /**
   * Score provider for KOOS/HOOS JR
   *
   * KOOS, JR is scored by summing the raw response (range 0-28) and then
   * converting it to an interval score using the table provided below.
   * The interval score ranges from 0 to 100 where 0 represents total knee
   * disability and 100 represents perfect knee health.
   *
   * HOOS, JR is scored by summing the raw response (range 0-24) and then
   * converting it to an interval score using the table provided below.
   * The interval score ranges from 0 to 100 where 0 represents total hip
   * disability and 100 represents perfect hip health.
   */
  static class KOOSJRScoreProvider extends ExtensibleScoreProvider {

    private static final double[] KoosJRScoreTable = new double[]
        { 100.000, 91.975, 84.600, 79.914, 76.332, 73.342, 70.704, 68.284, 65.994, 63.776,
           61.583, 59.381, 57.140, 54.840, 52.465, 50.012, 47.487, 44.905, 42.281, 39.625,
           36.931, 34.174, 31.307, 28.251, 24.875, 20.941, 15.939,  8.291,  0.000 };

    private static final double[] HoosJRScoreTable = new double[]
        { 100.000, 92.340, 85.257, 80.550, 76.776, 73.472, 70.426, 67.516, 64.664, 61.815,
           58.930, 55.985, 52.965, 49.858, 46.652, 43.335, 39.902, 36.363, 32.735, 29.009,
           25.103, 20.805, 15.633,  8.104,  0.000 };

    public KOOSJRScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      List<BigDecimal> ansScores = chartScore.getAnswers();
      int total = 0;
      for(BigDecimal ansScore : ansScores) {
        total += ansScore.intValue();
      }
      double score;
      if (studyName.contains("KOOS")) {
        score = KoosJRScoreTable[total];
      } else if (studyName.contains("HOOS")) {
        score = HoosJRScoreTable[total];
      } else {
        throw new RuntimeException("Error - study name is not a KOOS or HOOS JR study.");
      }
      chartScore.setScore(new BigDecimal(score));
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("Score");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = getDateAxis(collection);
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
      addSurgeryDateMarker(plot);

      return plot;
    }
  }

  /**
   * Score provider for Knee Society Function Aids
   *
   * The function aids score the the answer value of the aid device question.
   * The function aids score is a negative number.
   */
  static class KSFunctionAidsScoreProvider extends ExtensibleScoreProvider {

    public KSFunctionAidsScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      BigDecimal score = chartScore.getAnswer("AidDevice");
      chartScore.setScore(score);
    }
  }

  /**
   * Score provider for Knee Society Function
   *
   * The function score is the sum of the answer values for all of
   * the function questions plus the Function Aids score (which is
   * a negative number).
   */
  static class KSFunctionScoreProvider extends ExtensibleScoreProvider {

    public KSFunctionScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      List<BigDecimal> ansScores = chartScore.getAnswers();
      int total = 0;
      for(BigDecimal ansScore : ansScores) {
        total += ansScore.intValue();
      }

      PatStudyDao patStudyDao = new PatStudyDao(dbp.get(), siteInfo);
      List<PatientStudyExtendedData> functionAidSurveys =
          patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(
              patientData.getSurveyRegId(), "KneeSocietyFunctionAids");
      if ((functionAidSurveys != null) && (functionAidSurveys.size() > 0)) {
        ExtensibleScoreProvider aidScoreProvider = new KSFunctionAidsScoreProvider(dbp, siteInfo, "KneeSocietyFunctionAids");
        List<ChartScore> aidChartScores = aidScoreProvider.getScore(functionAidSurveys.get(0));
        ChartScore aidChartScore = aidChartScores.get(0);
        total += aidChartScore.getScore().intValue();
      }

      chartScore.setScore(new BigDecimal(total));
    }
  }

  static class KSFunction2ScoreProvider extends ExtensibleScoreProvider {

    public KSFunction2ScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      String[] questions = new String[] {"Q1", "Q2", "Q3", "Q4", "Q5", "Q6", "Q7", "Q8", "Q9", "Q10",
          "Q11", "Q12", "Q13"};
      int total = 0;
      for(String question : questions) {
        BigDecimal value = chartScore.getAnswer(question);
        if ((value != null) && (value.intValue() != 99)) {
          total += value.intValue();
        }
      }

      BigDecimal aidDevice = chartScore.getAnswer("AidDevice");
      int value = 0;
      if (aidDevice == null) {
        value = 0;
      } else if (aidDevice.intValue() == 1) {
        value = -10;
      } else if (aidDevice.intValue() == 2) {
        value = -8;
      } else if (aidDevice.intValue() == 3) {
        value = -8;
      } else if (aidDevice.intValue() == 4) {
        value = -6;
      } else if (aidDevice.intValue() == 5) {
        value = -4;
      } else if (aidDevice.intValue() == 6) {
        value = -4;
      } else if (aidDevice.intValue() == 7) {
        value = -2;
      } else if (aidDevice.intValue() == 8) {
        value = 0;
      } else {
        value = 0;
      }
      total += value;

      chartScore.setScore(new BigDecimal(total));
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("Score");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = getDateAxis(collection);
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
      addSurgeryDateMarker(plot);

      return plot;
    }
  }

  /**
   * Harris Hip Score Provider
   */
  static class HarrisScoreProvider extends ExtensibleScoreProvider {
    int minAnswered;

    public HarrisScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName, int minAnswered) {
      super(dbp, siteInfo, studyName);
      this.minAnswered = minAnswered;
    }

    @Override
    public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> stats,
        PrintStudy study, ChartConfigurationOptions opts) {
      final TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(baseLineSeries);
      if (stats == null || study == null) {
        return dataset;
      }

      TimeSeries timeDataSet = new TimeSeries(studyName);
      for (ChartScore stat : stats) {
        LocalScore localScore = (LocalScore) stat;
        List<BigDecimal> ansScores = localScore.getAnswers();
        int numberAnswers = ansScores.size();

        if (numberAnswers >= minAnswered) {
          Day day = new Day(localScore.getDate());
          BigDecimal score = localScore.getScore();
          timeDataSet.addOrUpdate(day, score);
        }
      }
      dataset.addSeries(timeDataSet);
      return dataset;
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("Score");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = getDateAxis(collection);
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
      rangeAxis.setRange(0, 50);
      ChartMaker.setRendererOptions(renderer, opts, ds.getSeriesCount());
      final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
      plot.setDomainGridlinePaint(Color.black);
      plot.setDomainGridlinesVisible(false);
      plot.setRangeGridlinePaint(Color.black);
      plot.setBackgroundPaint(Color.white);
      plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
      plot.setOutlineStroke(new BasicStroke(0.0f));
      plot.setInsets(ChartMaker.CHART_INSETS);
      addSurgeryDateMarker(plot);

      return plot;
    }
  }

  /**
   * Score provider for UCLA Activity Score
   */
  static class UCLAScoreProvider extends ExtensibleScoreProvider {

    public UCLAScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("Score");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = getDateAxis(collection);
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
      rangeAxis.setRange(1, 10);
      ChartMaker.setRendererOptions(renderer, opts, ds.getSeriesCount());
      final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
      plot.setDomainGridlinePaint(Color.black);
      plot.setDomainGridlinesVisible(false);
      plot.setRangeGridlinePaint(Color.black);
      plot.setBackgroundPaint(Color.white);
      plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
      plot.setOutlineStroke(new BasicStroke(0.0f));
      plot.setInsets(ChartMaker.CHART_INSETS);
      addSurgeryDateMarker(plot);

      return plot;
    }
  }

  /**
   * Score provider for VR-12
   */
  static class VR12ScoreProvider extends ExtensibleScoreProvider {

    // Physical Composite score
    // The constant and coefficient values were obtained from
    // the file pcs90_vr12_mar14_native_mail.sas7bdat. The values
    // used from the file are for key 0 (all questions have responses)
    static double PCS_CONSTANT = 21.0468597412109;
    static Map<String,Double> PCS_COEFFICIENT = new HashMap<>();
    static {
      PCS_COEFFICIENT.put("GH1", 0.078252375125885);
      PCS_COEFFICIENT.put("PF02", 0.0650640130043029);
      PCS_COEFFICIENT.put("PF04", 0.0748361349105835);
      PCS_COEFFICIENT.put("RP2", 0.0716978311538696);
      PCS_COEFFICIENT.put("RP3", 0.0741541385650634);
      PCS_COEFFICIENT.put("RE2", -0.0575982630252838);
      PCS_COEFFICIENT.put("RE3", -0.0322689414024353);
      PCS_COEFFICIENT.put("BP2", 0.133974909782409);
      PCS_COEFFICIENT.put("MH3", -0.0424118638038635);
      PCS_COEFFICIENT.put("VT2", 0.0299689620733261);
      PCS_COEFFICIENT.put("MH4", -0.0533623993396759);
      PCS_COEFFICIENT.put("SF2", 0.00460967794060707);
    }

    // Mental Composite score
    // The constant and coefficient values were obtained from
    // the file mcs90_vr12_mar14_native_mail.sas7bdat. The values
    // used from the file are for key 0 (all questions have responses)
    static double MCS_CONSTANT = 12.6620483398437;
    static Map<String,Double> MCS_COEFFICIENT = new HashMap<>();
    static {
      MCS_COEFFICIENT.put("GH1", -0.00091592501848936);
      MCS_COEFFICIENT.put("PF02", -0.0354986488819122);
      MCS_COEFFICIENT.put("PF04", -0.0315771400928497);
      MCS_COEFFICIENT.put("RP2", -0.0251735001802444);
      MCS_COEFFICIENT.put("RP3", -0.0246522277593612);
      MCS_COEFFICIENT.put("RE2", 0.126686096191406);
      MCS_COEFFICIENT.put("RE3", 0.080872356891632);
      MCS_COEFFICIENT.put("BP2", -0.0243713706731796);
      MCS_COEFFICIENT.put("MH3", 0.109408497810363);
      MCS_COEFFICIENT.put("VT2", 0.0694271326065063);
      MCS_COEFFICIENT.put("MH4", 0.149378895759582);
      MCS_COEFFICIENT.put("SF2", 0.10857343673706);
    }

    public VR12ScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected ChartScore getScoreInternal(PatientStudyExtendedData patientData) {
      VR12Score localScore = new VR12Score(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription());
      localScore.setAssisted(patientData.wasAssisted());

      loadAnswers(localScore, patientData);
      calculateScore(localScore, patientData);

      return localScore;
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      // Convert the answer codes into a value from 0 to 100
      int gh1;
      switch(chartScore.getAnswer("GH1").intValue()) {
        case 1:
          gh1 = 100;
          break;
        case 2:
          gh1 = 85;
          break;
        case 3:
          gh1 = 60;
          break;
        case 4:
          gh1 = 35;
          break;
        default:
          gh1 = 0;
      }

      int pf02 = (chartScore.getAnswer("PF02").intValue() - 1) * 50;
      int pf04 = (chartScore.getAnswer("PF04").intValue() - 1) * 50;
      int rp2 = (5 - chartScore.getAnswer("RP2").intValue()) * 25;
      int rp3 = (5 - chartScore.getAnswer("RP3").intValue()) * 25;
      int re2 = (5 - chartScore.getAnswer("RE2").intValue()) * 25;
      int re3 = (5 - chartScore.getAnswer("RE3").intValue()) * 25;
      int bp2 = (5 - chartScore.getAnswer("BP2").intValue()) * 25;
      int mh3 = (6 - chartScore.getAnswer("MH3").intValue()) * 20;
      int vt2 = (6 - chartScore.getAnswer("VT2").intValue()) * 20;
      int mh4 = (chartScore.getAnswer("MH4").intValue() - 1) * 20;
      int sf2 = (chartScore.getAnswer("SF2").intValue() - 1) * 25;

      // PCS score is the sum of each answer times it's PCS coefficient
      // plus a PCS constant value
      double pcs =
          (gh1 * PCS_COEFFICIENT.get("GH1")) +
          (pf02 * PCS_COEFFICIENT.get("PF02")) +
          (pf04 * PCS_COEFFICIENT.get("PF04")) +
          (rp2 * PCS_COEFFICIENT.get("RP2")) +
          (rp3 * PCS_COEFFICIENT.get("RP3")) +
          (re2 * PCS_COEFFICIENT.get("RE2")) +
          (re3 * PCS_COEFFICIENT.get("RE3")) +
          (bp2 * PCS_COEFFICIENT.get("BP2")) +
          (mh3 * PCS_COEFFICIENT.get("MH3")) +
          (vt2 * PCS_COEFFICIENT.get("VT2")) +
          (mh4 * PCS_COEFFICIENT.get("MH4")) +
          (sf2 * PCS_COEFFICIENT.get("SF2")) +
          PCS_CONSTANT;

      // MCS score is the sum of each answer times it's MCS coefficient
      // plus a MCS constant value
      double mcs =
          (gh1 * MCS_COEFFICIENT.get("GH1")) +
          (pf02 * MCS_COEFFICIENT.get("PF02")) +
          (pf04 * MCS_COEFFICIENT.get("PF04")) +
          (rp2 * MCS_COEFFICIENT.get("RP2")) +
          (rp3 * MCS_COEFFICIENT.get("RP3")) +
          (re2 * MCS_COEFFICIENT.get("RE2")) +
          (re3 * MCS_COEFFICIENT.get("RE3")) +
          (bp2 * MCS_COEFFICIENT.get("BP2")) +
          (mh3 * MCS_COEFFICIENT.get("MH3")) +
          (vt2 * MCS_COEFFICIENT.get("VT2")) +
          (mh4 * MCS_COEFFICIENT.get("MH4")) +
          (sf2 * MCS_COEFFICIENT.get("SF2")) +
          MCS_CONSTANT;

      chartScore.setScore(new BigDecimal(0));

      Map<String,BigDecimal> scores = new LinkedHashMap<>();
      scores.put("PCS", new BigDecimal(pcs));
      scores.put("MCS", new BigDecimal(mcs));
      chartScore.setScores(scores);
    }

    @Override
    public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> stats,
        PrintStudy study, ChartConfigurationOptions opts) {
      final TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(baseLineSeries);
      if (stats == null || study == null) {
        return dataset;
      }

      TimeSeries timeDataSetPCS = new TimeSeries("Physical");
      TimeSeries timeDataSetMCS = new TimeSeries("Mental");
      for (ChartScore stat : stats) {
        LocalScore localScore = (LocalScore) stat;
        Map<String,BigDecimal> scores = localScore.getScores();

        Day day = new Day(localScore.getDate());
        BigDecimal pcs = scores.get("PCS");
        if (pcs != null) {
          timeDataSetPCS.addOrUpdate(day, pcs);
        }
        BigDecimal mcs = scores.get("MCS");
        if (mcs != null) {
          timeDataSetMCS.addOrUpdate(day, mcs);
        }
      }
      dataset.addSeries(timeDataSetPCS);
      dataset.addSeries(timeDataSetMCS);
      return dataset;
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("Score");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = getDateAxis(collection);
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
      addSurgeryDateMarker(plot);

      return plot;
    }
  }

  static class VR12Score extends LocalScore implements MultiScore {

    public VR12Score(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    public int getNumberOfScores() {
      return 2;
    }

    public String getTitle(int scoreNumber, String studyDescription) {
      switch (scoreNumber) {
      case 1:
        return studyDescription + " - Physical";
      case 2:
        return studyDescription + " - Mental";
      default:
        return studyDescription;
      }
    }

    public double getScore(int scoreNumber) {
      Map<String,BigDecimal> scores = getScores();
      BigDecimal score;
      switch (scoreNumber) {
      case 1:
        score = scores.get("PCS");
        break;
      case 2:
        score = scores.get("MCS");
        break;
      default:
        score = null;
      }
      return (score != null) ? score.doubleValue() : 0;
    }

    public Double getPercentileScore(int scoreNumber) {
      return null;
    }
  }

  /**
   * Score provider for Survey Info
   */
  static class SurveyInfoScoreProvider extends ExtensibleScoreProvider {

    public SurveyInfoScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    public ArrayList<SurveyQuestionIntf> getSurvey(PatientStudyExtendedData patStudy, PrintStudy study,
        Patient patient, boolean allAnswers) {
      ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
      if (patStudy == null) {
        return questions;
      }

      // Get the date the survey was completed
      Date completed = null;
      String token = patStudy.getToken();
      ActivityDao activityDao = new ActivityDao(dbp.get(), patStudy.getSurveySiteId());
      List<Activity> activities = activityDao.getActivityByToken(token, Constants.ACTIVITY_COMPLETED);
      if ((activities != null) && (activities.size() > 0)) {
        completed = activities.get(0).getActivityDt();
      }

      // Get the surgery date
      String surgeryDate = null;
      Long regId = patStudy.getSurveyRegId();
      SurveyRegistrationAttributeDao surveyRegAttrDao = new SurveyRegistrationAttributeDao(dbp.get());
      Map<String,String> attrs = surveyRegAttrDao.getAttributes(regId);
      surgeryDate = attrs.get(TotalJointCustomizer.ATTR_SURGERY_DATE);

      RegistryQuestion question;
      InputElement answer;
      ArrayList<SurveyAnswerIntf> answers;

      if (completed != null) {
        question = new RegistryQuestion();
        question.addText("Questionnaire completed");
        question.setAnswered(true);
        answers = new ArrayList<>();
        answer = new InputElement();
        answer.setType(edu.stanford.registry.shared.survey.Constants.TYPE_INPUT);
        answer.setLabel("");
        answer.setValue(dateFormat.format(completed));
        answers.add(answer);
        question.setAnswers(answers);
        questions.add(question);
      }

      if ((surgeryDate != null) && !surgeryDate.equals("")) {
        question = new RegistryQuestion();
        question.addText("Surgery Date");
        question.setAnswered(true);
        answers = new ArrayList<>();
        answer = new InputElement();
        answer.setType(edu.stanford.registry.shared.survey.Constants.TYPE_INPUT);
        answer.setLabel("");
        answer.setValue(surgeryDate);
        answers.add(answer);
        question.setAnswers(answers);
        questions.add(question);
      }

      return questions;
    }
  }
}
