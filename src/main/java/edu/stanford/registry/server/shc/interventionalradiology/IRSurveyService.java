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
package edu.stanford.registry.server.shc.interventionalradiology;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.charts.ChartMaker;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.ExtensibleScoreProvider;
import edu.stanford.registry.server.survey.NextQuestion;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.RegistryAssessmentsService;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.survey.SurveyServiceIntf;
import edu.stanford.registry.server.tools.CustomDateAxis;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.EmailTemplateUtils;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.StringUtils;
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
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.RegistryQuestion;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
import edu.stanford.registry.shared.xform.InputElement;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.SessionStatus;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;


/**
 * Created by scweber on 12/4/15.
 */
public class IRSurveyService extends RegistryAssessmentsService {

  private Study FACTHepLangStudy = null;

  public IRSurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  private static final Logger logger = LoggerFactory.getLogger(IRSurveyService.class);
  private static final String HIPAA_AUTH_RESOURCE = "shc/ir/HIPAA_AuthAndConsent.pdf";

  protected static final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended,
      SubmitStatus submitStatus, String answerJson) {
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    ActivityDao activityDao = new ActivityDao(database, patStudyExtended.getSurveySiteId());
    if ("irConsent2".equals(patStudyExtended.getStudyDescription())) {
      if (submitStatus != null) {
        FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
        for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
          String[] ids = fieldAnswer.getFieldId().split(":");
          if (ids.length == 3) {
            // Handle consent response
            if (ids[2].equals("IRCONSENT")) {
              String choice = fieldAnswer.getChoice().get(0);
              if ("1".equals(choice)) {
                PatientAttribute pattribute;
                pattribute = new PatientAttribute(patStudyExtended.getPatientId(), IRCustomizer.ATTR_IR_CONSENT, "Y", PatientAttribute.STRING);
                patAttribDao.insertAttribute(pattribute);

                Token tok = new Token();
                Activity pactivity = new Activity(patStudyExtended.getPatientId(), Constants.ACTIVITY_CONSENTED, tok.getToken());
                activityDao.createActivity(pactivity);
              } else if ("2".equals(choice)) {
                PatientAttribute pattribute;
                pattribute = new PatientAttribute(patStudyExtended.getPatientId(), IRCustomizer.ATTR_IR_CONSENT, "N", PatientAttribute.STRING);
                patAttribDao.insertAttribute(pattribute);
              }
            }
            // Handle email address response
            else if (ids[2].equals("email")) {
              String value = fieldAnswer.getChoice().get(0);
              String emailAddress = StringUtils.cleanString(value);
              if (!emailAddress.equals("")) {
                if (ServerUtils.getInstance().isValidEmail(emailAddress)) {
                  PatientAttribute pattribute;
                  pattribute = new PatientAttribute(patStudyExtended.getPatientId(), Constants.ATTRIBUTE_SURVEYEMAIL_ALT, emailAddress, PatientAttribute.STRING);
                  patAttribDao.insertAttribute(pattribute);
                } else {
                  NextQuestion nextQuestion = super.handleResponse(database, patStudyExtended, null, null);
                  nextQuestion.getDisplayStatus().setServerValidationMessage("Please enter a valid email address");
                  nextQuestion.getDisplayStatus().setSessionStatus(SessionStatus.questionInvalid);
                  return nextQuestion;
                }
              }
            }
            // Handle send consent form response
            else if (ids[2].equals("sendForm")) {
              String choice = fieldAnswer.getChoice().get(0);
              if ("1".equals(choice)) {
                String email = null;
                if (patStudyExtended.getPatient() != null) {
                  email = patStudyExtended.getPatient().getEmailAddress();
                }
                if ((email != null) && (!email.equals(""))) {
                  sendForm(email);
                } else {
                  // if <Item Order="2">
                  if (ids[0].equals("2")) {
                    NextQuestion nextQuestion = super.handleResponse(database, patStudyExtended, null, null);
                    nextQuestion.getDisplayStatus().setServerValidationMessage("Please enter a valid email address");
                    nextQuestion.getDisplayStatus().setSessionStatus(SessionStatus.questionInvalid);
                    return nextQuestion;
                  }
                }
              }
            }
          }
        }
      }
    }
    return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
  }

  protected void sendForm(String emailAddr) {
    try {
      Mailer mailer = siteInfo.getMailer();
      EmailTemplateUtils emailUtils = new EmailTemplateUtils();

      String template = emailUtils.getTemplate(siteInfo, "Send-consent-form");
      String subject = emailUtils.getEmailSubject(template);
      String body = emailUtils.getEmailBody(template);

      List<File> attachments = new ArrayList<>();
      URL url = getClass().getClassLoader().getResource(HIPAA_AUTH_RESOURCE);
      if (url == null ) {
          throw new RuntimeException("Did not find resource " + HIPAA_AUTH_RESOURCE);
      }
      attachments.add(new File(url.getFile()));

      mailer.sendTextWithAttachment(emailAddr, null, null, subject, body, attachments);
    } catch (Exception ex) {
      logger.error("ERROR trying to send Send-consent-form email with {} to {}", HIPAA_AUTH_RESOURCE, emailAddr, ex);
    }
  }

  @Override
  protected void assessmentCompleted(Database database, Patient patient, PatientStudy patStudy) {
    if (FACTHepLangStudy == null) {
      FACTHepLangStudy = getStudy(database, "FACTHepLang");
    }

    // Handle the language selection
    if ((FACTHepLangStudy != null) &&
        (patStudy.getStudyCode().intValue() == FACTHepLangStudy.getStudyCode().intValue()) ) {
      // Get the XML content
      String xmlString = patStudy.getContents();

      // Get the email response value
      String value = XMLFileUtils.xPathQuery(xmlString, "//Response[ref='lang']/item[@selected='true']/value/text()");
      
      if ("1".equals(value)) {
        // Spanish
        addLanguageQuestionnaires(database, patStudy, "FACT-Hep-Spanish");
      } else if ("2".equals(value)) {
        // Russian
        addLanguageQuestionnaires(database, patStudy, "FACT-Hep-Russian");
      } else if ("3".equals(value)) {
        // Vietnamese
        addLanguageQuestionnaires(database, patStudy, "FACT-Hep-Vietnamese");
      } else if ("4".equals(value)) {
        // Cantonese
        addLanguageQuestionnaires(database, patStudy, "FACT-Hep-Cantonese");
      } else if ("5".equals(value)) {
        // Mandarin
        addLanguageQuestionnaires(database, patStudy, "FACT-Hep-Mandarin");
      } else {
        // English
        addLanguageQuestionnaires(database, patStudy, "FACT-Hep-English");
      }
    }
  }

  protected void addLanguageQuestionnaires(Database database, PatientStudy patStudy, String languageProcess) {
    AssessDao assessDao = new AssessDao(database, siteInfo);
    XMLFileUtils xmlUtils = XMLFileUtils.getInstance(siteInfo);

    SurveyRegistration sreg = assessDao.getSurveyRegistrationByRegId(patStudy.getSurveyRegId());
    String process = xmlUtils.getActiveProcessForName(languageProcess, sreg.getSurveyDt());

    ArrayList<Element> processList = xmlUtils.getProcessQuestionaires(process);
    if (processList != null) {
      for (Element questionaire : processList) {
        // Register the patient in each one
        String qType = questionaire.getAttribute("type");
        SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(siteInfo).getSurveyServiceImpl(qType);
        if (surveyService == null) {
          throw new ServiceUnavailableException("No service found for type: " + qType);
        }

        try {
          surveyService.registerAssessment(database, questionaire, patStudy.getPatientId(),
              new Token(patStudy.getToken()),
              ServerUtils.getAdminUser(database.get()));
        } catch (Exception ex) {
          logger.error("Error registering qtype: {}", qType, ex);
        }
      }
    }
  }

  /**
   * Return the appropriate score provider based on the study name.
   */
  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    if (studyName.equals("DVT") ) {
      return new DVTSummaryProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("DVT-QOL")) {
      return new ExtensibleScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("DVT-Activities")) {
      return new ExtensibleScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("DVT-Feelings")) {
      return new ExtensibleScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("Lymphedema") || studyName.equals("LymLeg")) {
      return new LymphedemaSummaryProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("LymphedemaFunction") || studyName.equals("LymLegFunction")) {
      return new LymphedemaScoreProvider(dbp, siteInfo, studyName, 7);
    } else if (studyName.equals("LymphedemaAppearance") || studyName.equals("LymLegAppearance")) {
      return new LymphedemaScoreProvider(dbp, siteInfo, studyName, 4);
    } else if (studyName.equals("LymphedemaSymptoms") || studyName.equals("LymLegSymptoms")) {
      return new LymphedemaScoreProvider(dbp, siteInfo, studyName, 6);
    } else if (studyName.equals("LymArm")) {
      return new LymphedemaSummaryProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("LymArmFunction")) {
      return new LymphedemaScoreProvider(dbp, siteInfo, studyName, 6);
    } else if (studyName.equals("LymArmAppearance")) {
      return new LymphedemaScoreProvider(dbp, siteInfo, studyName, 4);
    } else if (studyName.equals("LymArmSymptoms")) {
      return new LymphedemaScoreProvider(dbp, siteInfo, studyName, 6);
    } else if (studyName.equals("LymphedemaMood")) {
      return new LymphedemaScoreProvider(dbp, siteInfo, studyName, 3);
    } else if (studyName.equals("LymphedemaOverall")) {
      return new LymphedemaOverallScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.startsWith("facthep")) {
      return new FactHepScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("HCC")) {
      return new HCCSummaryProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("irConsent2")) {
      return new ConsentScoreProvider(dbp, siteInfo, studyName);
    }
    return new ExtensibleScoreProvider(dbp, siteInfo, studyName);
  }

  /**
   * DVT Summary Score Provider. Sum the QOL, Activities and Feelings scores
   */
  public class DVTSummaryProvider extends ExtensibleScoreProvider {

    public DVTSummaryProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      List<BigDecimal> ansScores = chartScore.getAnswers();
      int total = 0;

      // Sum answers in the DVT.xml file itself as early versions of the file
      // contained the questions
      for(BigDecimal ansScore : ansScores) {
        total += ansScore.intValue();
      }

      PatStudyDao patStudyDao = new PatStudyDao(dbp.get(), siteInfo);
      Long surveyRegId = patientData.getSurveyRegId();

      total += computeScore(patStudyDao, surveyRegId, "DVT-QOL");
      total += computeScore(patStudyDao, surveyRegId, "DVT-Activities");
      total += computeScore(patStudyDao, surveyRegId, "DVT-Feelings");

      chartScore.setScore(new BigDecimal(total));
    }

    int computeScore(PatStudyDao patStudyDao, Long surveyRegId, String name) {
      List<PatientStudyExtendedData> qolSurveys =
          patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(surveyRegId, name);
      if ((qolSurveys == null) || (qolSurveys.size() == 0)) {
        return 0;
      }

      ScoreProvider scoreProvider = getScoreProvider(dbp, "DVT-QOL");
      List<ChartScore> chartScores = scoreProvider.getScore(qolSurveys.get(0));
      ChartScore chartScore = chartScores.get(0);
      return chartScore.getScore().intValue();
    }

    @Override
    protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow) {
      Table table = new Table();
      if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
        return table;
      }

      table.addHeading(study.getTitle() + " (0:Best - 100:Worst)");
      TableRow colHeader = new TableRow(100);
      colHeader.setColumnGap(3);
      colHeader.addColumn(new TableColumn("Date", 65));
      colHeader.addColumn(new TableColumn("Score", 31));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 65));
        row.addColumn(new TableColumn(scoreFormatter.format(score.getScore()), 31));
        table.addRow(row);
      }

      return table;
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("Score");
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
      rangeAxis.setTickUnit(new NumberTickUnit(20));
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

      final IntervalMarker intervalLow = new IntervalMarker(0, 20);
      intervalLow.setLabel("Better");
      intervalLow.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      intervalLow.setLabelAnchor(RectangleAnchor.LEFT);
      intervalLow.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      intervalLow.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(intervalLow, Layer.BACKGROUND);

      final IntervalMarker intervalHigh = new IntervalMarker(80, 100);
      intervalHigh.setLabel("Worse");
      intervalHigh.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      intervalHigh.setLabelAnchor(RectangleAnchor.LEFT);
      intervalHigh.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      intervalHigh.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(intervalHigh, Layer.BACKGROUND);

      return plot;
    }
  }

  /**
   * Lymphedema Score provider. Score is average of the answers with a minimum
   * number of questions answered.
   */
  public class LymphedemaScoreProvider extends ExtensibleScoreProvider {

    private int minAnswered;

    public LymphedemaScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName, int minAnswered) {
      super(dbp, siteInfo, studyName);
      this.minAnswered = minAnswered;
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      List<BigDecimal> ansScores = chartScore.getAnswers();
      int numberAnswers = 0;
      double total = 0;
      for(BigDecimal ansScore : ansScores) {
        // If the answer is not N/A
        if (ansScore.intValue() != 0) {
          numberAnswers += 1;
          total += ansScore.doubleValue();
        }
      }
      if (numberAnswers < minAnswered) {
        chartScore.setScore(new BigDecimal(0));
      } else {
        double score = total/numberAnswers;
        chartScore.setScore(new BigDecimal(score));
      }
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("Score");
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
      rangeAxis.setTickUnit(new NumberTickUnit(0.5));
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

      final IntervalMarker intervalNA = new IntervalMarker(0, 0.5);
      intervalNA.setLabel("N/A");
      intervalNA.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      intervalNA.setLabelAnchor(RectangleAnchor.LEFT);
      intervalNA.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      intervalNA.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(intervalNA, Layer.BACKGROUND);

      final IntervalMarker intervalLow = new IntervalMarker(1, 1.5);
      intervalLow.setLabel("Better");
      intervalLow.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      intervalLow.setLabelAnchor(RectangleAnchor.LEFT);
      intervalLow.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      intervalLow.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(intervalLow, Layer.BACKGROUND);

      final IntervalMarker intervalHigh = new IntervalMarker(3.5, 4);
      intervalHigh.setLabel("Worse");
      intervalHigh.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      intervalHigh.setLabelAnchor(RectangleAnchor.LEFT);
      intervalHigh.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      intervalHigh.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(intervalHigh, Layer.BACKGROUND);

      return plot;
    }
  }

  /**
   * Lymphedema Overall Score provider.
   */
  public class LymphedemaOverallScoreProvider extends ExtensibleScoreProvider {

    public LymphedemaOverallScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
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
      rangeAxis.setRange(0, 10);
      ChartMaker.setRendererOptions(renderer, opts, ds.getSeriesCount());
      final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
      plot.setDomainGridlinePaint(Color.black);
      plot.setDomainGridlinesVisible(false);
      plot.setRangeGridlinePaint(Color.black);
      plot.setBackgroundPaint(Color.white);
      plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
      plot.setOutlineStroke(new BasicStroke(0.0f));
      plot.setInsets(ChartMaker.CHART_INSETS);

      final IntervalMarker intervalLow = new IntervalMarker(0, 1);
      intervalLow.setLabel("Worse");
      intervalLow.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      intervalLow.setLabelAnchor(RectangleAnchor.LEFT);
      intervalLow.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      intervalLow.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(intervalLow, Layer.BACKGROUND);

      final IntervalMarker intervalHigh = new IntervalMarker(9, 10);
      intervalHigh.setLabel("Better");
      intervalHigh.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      intervalHigh.setLabelAnchor(RectangleAnchor.LEFT);
      intervalHigh.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      intervalHigh.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(intervalHigh, Layer.BACKGROUND);

      return plot;
    }
  }

  /**
   * Lymphedema Summary Score Provider. Create the table of Lymphedema scores.
   */
  public class LymphedemaSummaryProvider extends ExtensibleScoreProvider {

    protected final DecimalFormat scoreFormatter = new DecimalFormat("##.##");

    public LymphedemaSummaryProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies,
        PrintStudy study, Patient patient) {
      ArrayList<ChartScore> scores = new ArrayList<>();
      LocalScore summaryChartScore = null;
      Long surveyRegId = new Long(0);
      for(PatientStudyExtendedData patientStudy : patientStudies) {
        String sname = patientStudy.getStudyDescription();
        if (sname.equals("LymphedemaFunction") || sname.equals("LymLegFunction") || sname.equals("LymArmFunction") ||
            sname.equals("LymphedemaAppearance") || sname.equals("LymLegAppearance") || sname.equals("LymArmAppearance") ||
            sname.equals("LymphedemaSymptoms") || sname.equals("LymLegSymptoms") || sname.equals("LymArmSymptoms") ||
            sname.equals("LymphedemaMood") ||
            sname.equals("LymphedemaOverall")) {
          ScoreProvider scoreProvider = getScoreProvider(dbp, sname);
          List<ChartScore> chartScores = scoreProvider.getScore(patientStudy);
          ChartScore chartScore = chartScores.get(0);
          if ((summaryChartScore == null) ||
              !surveyRegId.equals(patientStudy.getSurveyRegId()) ) {
            summaryChartScore = new LocalScore(DateUtils.getDateStart(siteInfo, chartScore.getDate()), patient.getPatientId(), -1, "DVT");
            Map<String,BigDecimal> scoreMap = new LinkedHashMap<>();
            summaryChartScore.setScores(scoreMap);
            scores.add(summaryChartScore);
            surveyRegId = patientStudy.getSurveyRegId();
          }
          if (sname.equals("LymphedemaFunction") || sname.equals("LymLegFunction") || sname.equals("LymArmFunction")) {
            summaryChartScore.getScores().put("Function", chartScore.getScore());
          } else if (sname.equals("LymphedemaAppearance") || sname.equals("LymLegAppearance") || sname.equals("LymArmAppearance")) {
            summaryChartScore.getScores().put("Appearance", chartScore.getScore());
          } else if (sname.equals("LymphedemaSymptoms") || sname.equals("LymLegSymptoms") || sname.equals("LymArmSymptoms")) {
            summaryChartScore.getScores().put("Symptoms", chartScore.getScore());
          } else if (sname.equals("LymphedemaMood")) {
            summaryChartScore.getScores().put("Mood", chartScore.getScore());
          } else if (sname.equals("LymphedemaOverall")) {
            summaryChartScore.getScores().put("Overall", chartScore.getScore());
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

      table.addHeading(study.getTitle() + " (Domains 1:Best - 4:Worst, 0:N/A; Overall 0:Worst - 10:Best)");

      TableRow colHeader = new TableRow(100);
      colHeader.addColumn(new TableColumn("Date", 20));
      colHeader.addColumn(new TableColumn("Function", 16));
      colHeader.addColumn(new TableColumn("Appearance", 16));
      colHeader.addColumn(new TableColumn("Symptoms", 16));
      colHeader.addColumn(new TableColumn("Mood", 16));
      colHeader.addColumn(new TableColumn("Overall", 16));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
        Map<String,BigDecimal> scoreValues = score.getScores();
        BigDecimal functionScore = scoreValues.get("Function");
        if (functionScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(functionScore), 16));
        } else {
          row.addColumn(new TableColumn(" ", 16));
        }
        BigDecimal appearanceScore = scoreValues.get("Appearance");
        if (appearanceScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(appearanceScore), 16));
        } else {
          row.addColumn(new TableColumn(" ", 16));
        }
        BigDecimal symptomsScore = scoreValues.get("Symptoms");
        if (symptomsScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(symptomsScore), 16));
        } else {
          row.addColumn(new TableColumn(" ", 16));
        }
        BigDecimal moodScore = scoreValues.get("Mood");
        if (moodScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(moodScore), 16));
        } else {
          row.addColumn(new TableColumn(" ", 16));
        }
        BigDecimal overallScore = scoreValues.get("Overall");
        if (overallScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(overallScore), 16));
        } else {
          row.addColumn(new TableColumn(" ", 16));
        }
        table.addRow(row);
      }

      return table;
    }
  }

  /**
   * FACT-Hep Score provider.
  */
  public class FactHepScoreProvider extends ExtensibleScoreProvider {
    
    private final String[] physicalQuestions = new String [] {
        "gp1", "gp2", "gp3", "gp4", "gp5", "gp6", "gp7" };
    private final String[] socialQuestions = new String [] {
        "gs1", "gs2", "gs3", "gs4", "gs5", "gs6", "gs7" };
    private final String[] emotionalQuestions = new String [] {
        "ge1", "ge2", "ge3", "ge4", "ge5", "ge6" };
    private final String[] functionalQuestions = new String [] {
        "gf1", "gf2", "gf3", "gf4", "gf5", "gf6", "gf7" };
    private final String[] hcsQuestions = new String [] {
        "c1", "c2", "c3", "c4", "c5", "c6", "hep1", "cns7", "cx6", "h17",
        "an7", "hep2", "hep3", "hep4", "hep5", "hep6", "hn2", "hep8" };

    // These questions are reverse scored
    private final String[] reverseQuestions = new String[] {
        "gp1", "gp2", "gp3", "gp4", "gp5", "gp6", "gp7",
        "ge1", "ge3", "ge4", "ge5", "ge6",
        "c1", "c2", "c5", "hep1", "cns7", "cx6", "h17",
        "hep2", "hep3", "hep4", "hep5", "hep6", "hn2", "hep8"
    };

    public FactHepScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected ChartScore getScoreInternal(PatientStudyExtendedData patientData) {
      LocalScore localScore = new FactHepScore(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription());
      localScore.setAssisted(patientData.wasAssisted());

      loadAnswers(localScore, patientData);
      calculateScore(localScore, patientData);

      return localScore;
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      List<String> reverse = Arrays.asList(reverseQuestions);

      int physicalCount = 0;
      int physicalTotal = 0;
      for(String question : physicalQuestions) {
        if (chartScore.isAnswered(question)) {
          physicalCount += 1;
          if (reverse.contains(question)) {
            physicalTotal += (4 - chartScore.getAnswer(question).intValue());
          } else {
            physicalTotal += chartScore.getAnswer(question).intValue();
          }
        }
      }
      double physicalScore = 0.0;
      if (physicalCount > 0) {
        physicalScore = physicalTotal * ((double)physicalQuestions.length / (double)physicalCount);
      }

      int socialCount = 0;
      int socialTotal = 0;
      for(String question : socialQuestions) {
        if (chartScore.isAnswered(question)) {
          socialCount += 1;
          if (reverse.contains(question)) {
            socialTotal += (4 - chartScore.getAnswer(question).intValue());
          } else {
            socialTotal += chartScore.getAnswer(question).intValue();
          }
        }
      }
      double socialScore = 0.0;
      if (socialCount > 0) {
        socialScore = socialTotal * ((double)socialQuestions.length / (double)socialCount);
      }

      int emotionalCount = 0;
      int emotionalTotal = 0;
      for(String question : emotionalQuestions) {
        if (chartScore.isAnswered(question)) {
          emotionalCount += 1;
          if (reverse.contains(question)) {
            emotionalTotal += (4 - chartScore.getAnswer(question).intValue());
          } else {
            emotionalTotal += chartScore.getAnswer(question).intValue();
          }
        }
      }
      double emotionalScore = 0.0;
      if (emotionalCount > 0) {
        emotionalScore = emotionalTotal * ((double)emotionalQuestions.length / (double)emotionalCount);
      }

      int functionalCount = 0;
      int functionalTotal = 0;
      for(String question : functionalQuestions) {
        if (chartScore.isAnswered(question)) {
          functionalCount += 1;
          if (reverse.contains(question)) {
            functionalTotal += (4 - chartScore.getAnswer(question).intValue());
          } else {
            functionalTotal += chartScore.getAnswer(question).intValue();
          }
        }
      }
      double functionalScore = 0.0;
      if (functionalCount > 0) {
        functionalScore = functionalTotal * ((double)functionalQuestions.length / (double)functionalCount);
      }

      int hcsCount = 0;
      int hcsTotal = 0;
      for(String question : hcsQuestions) {
        if (chartScore.isAnswered(question)) {
          hcsCount += 1;
          if (reverse.contains(question)) {
            hcsTotal += (4 - chartScore.getAnswer(question).intValue());
          } else {
            hcsTotal += chartScore.getAnswer(question).intValue();
          }
        }
      }
      double hcsScore = 0.0;
      if (hcsCount > 0) {
        hcsScore = hcsTotal * ((double)hcsQuestions.length / (double)hcsCount);
      }
      
      double factHepTOI = physicalScore + functionalScore + hcsScore;
      double factG = physicalScore + socialScore + emotionalScore + functionalScore;
      double factHepTotal = physicalScore + socialScore + emotionalScore + functionalScore + hcsScore;

      chartScore.setScore(new BigDecimal(0));
      Map<String,BigDecimal> scores = new LinkedHashMap<>();
      scores.put("Physical", new BigDecimal(physicalScore));
      scores.put("Social", new BigDecimal(socialScore));
      scores.put("Emotional", new BigDecimal(emotionalScore));
      scores.put("Functional", new BigDecimal(functionalScore));
      scores.put("HCS", new BigDecimal(hcsScore));
      scores.put("FACT-Hep TOI", new BigDecimal(factHepTOI));
      scores.put("FACT-G", new BigDecimal(factG));
      scores.put("FACT-Hep Total", new BigDecimal(factHepTotal));
      chartScore.setScores(scores);
    }

    @Override
    protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow) {
      Table table = new Table();
      if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
        return table;
      }

      table.addHeading(study.getTitle() + " (The higher the score, the better the QOL)");

      TableRow colHeader = new TableRow(100);
      colHeader.addColumn(new TableColumn("Date", 20));
      colHeader.addColumn(new TableColumn("Physical (0-28)", 16));
      colHeader.addColumn(new TableColumn("Social (0-28)", 16));
      colHeader.addColumn(new TableColumn("Emotional (0-24)", 16));
      colHeader.addColumn(new TableColumn("Functional (0-28)", 16));
      colHeader.addColumn(new TableColumn("HCS (0-72)", 16));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
        Map<String,BigDecimal> scoreValues = score.getScores();
        BigDecimal physicalScore = scoreValues.get("Physical");
        if (physicalScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(physicalScore), 16));
        } else {
          row.addColumn(new TableColumn(" ", 16));
        }
        BigDecimal socialScore = scoreValues.get("Social");
        if (socialScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(socialScore), 16));
        } else {
          row.addColumn(new TableColumn(" ", 16));
        }
        BigDecimal emotionalScore = scoreValues.get("Emotional");
        if (emotionalScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(emotionalScore), 16));
        } else {
          row.addColumn(new TableColumn(" ", 16));
        }
        BigDecimal functionalScore = scoreValues.get("Functional");
        if (functionalScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(functionalScore), 16));
        } else {
          row.addColumn(new TableColumn(" ", 16));
        }
        BigDecimal hcsScore = scoreValues.get("HCS");
        if (hcsScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(hcsScore), 16));
        } else {
          row.addColumn(new TableColumn(" ", 16));
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

      TimeSeries timeDataSetHCS = new TimeSeries("HCS");
      TimeSeries timeDataSetTOI = new TimeSeries("FACT-Hep TOI");
      TimeSeries timeDataSetG = new TimeSeries("FACT-G");
      TimeSeries timeDataSetTotal = new TimeSeries("FACT-Hep Total");

      for (ChartScore score : scores) {
        Map<String,BigDecimal> scoreValues = score.getScores();

        Day day = new Day(score.getDate());
        addScoreToDataSet(scoreValues.get("HCS"), timeDataSetHCS, day);
        addScoreToDataSet(scoreValues.get("FACT-Hep TOI"), timeDataSetTOI, day);
        addScoreToDataSet(scoreValues.get("FACT-G"), timeDataSetG, day);
        addScoreToDataSet(scoreValues.get("FACT-Hep Total"), timeDataSetTotal, day);
      }
      dataset.addSeries(timeDataSetHCS);
      dataset.addSeries(timeDataSetTOI);
      dataset.addSeries(timeDataSetG);
      dataset.addSeries(timeDataSetTotal);
      return dataset;
    }

    private void addScoreToDataSet(BigDecimal score, TimeSeries timeSeries, Day day) {
      if (score != null) {
        timeSeries.addOrUpdate(day, score);
      }
    }
    
    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                           ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("Score");
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
      rangeAxis.setTickUnit(new NumberTickUnit(20));
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

      final IntervalMarker intervalLow = new IntervalMarker(80, 100);
      intervalLow.setLabel("Better");
      intervalLow.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      intervalLow.setLabelAnchor(RectangleAnchor.LEFT);
      intervalLow.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      intervalLow.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(intervalLow, Layer.BACKGROUND);

      final IntervalMarker intervalHigh = new IntervalMarker(0, 20);
      intervalHigh.setLabel("Worse");
      intervalHigh.setLabelFont(ChartMaker.getFont(opts, Font.ITALIC, ChartMaker.MARKERFONTSIZE));
      intervalHigh.setLabelAnchor(RectangleAnchor.LEFT);
      intervalHigh.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
      intervalHigh.setPaint(new Color(255, 255, 255, 100));
      plot.addRangeMarker(intervalHigh, Layer.BACKGROUND);
      return plot;
    }
  }

  /**
   * HCC Summary score provider.
  */
  public class HCCSummaryProvider extends FactHepScoreProvider {

    public HCCSummaryProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }
    
    @Override
    public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies,
        PrintStudy study, Patient patient) {
      ArrayList<ChartScore> scores = new ArrayList<>();
      for(PatientStudyExtendedData patientStudy : patientStudies) {
        if (patientStudy.getStudyDescription().startsWith("facthep")) {
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
      colHeader.addColumn(new TableColumn("FACT-Hep TOI", 26));
      colHeader.addColumn(new TableColumn("FACT-G", 27));
      colHeader.addColumn(new TableColumn("FACT-Hep Total", 27));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
        Map<String,BigDecimal> scoreValues = score.getScores();
        BigDecimal factHepTOI = scoreValues.get("FACT-Hep TOI");
        if (factHepTOI != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(factHepTOI), 26));
        } else {
          row.addColumn(new TableColumn(" ", 16));
        }
        BigDecimal factG = scoreValues.get("FACT-G");
        if (factG != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(factG), 27));
        } else {
          row.addColumn(new TableColumn(" ", 16));
        }
        BigDecimal factHepTotal = scoreValues.get("FACT-Hep Total");
        if (factHepTotal != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(factHepTotal), 27));
        } else {
          row.addColumn(new TableColumn(" ", 16));
        }
        table.addRow(row);
      }

      return table;
    }
  }

  static class FactHepScore extends LocalScore implements MultiScore {

    public FactHepScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    @Override
    public int getNumberOfScores() {
      return 4;
    }

    @Override
    public String getTitle(int scoreNumber, String studyDescription) {
      switch(scoreNumber) {
      case 1:
        return "FACT-Hep HCS";
      case 2:
        return "FACT-Hep TOI";
      case 3:
        return "FACT-G";
      case 4:
        return "FACT-Hep Total";
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
        score = scoreValues.get("HCS");
        break;
      case 2:
        score = scoreValues.get("FACT-Hep TOI");
        break;
      case 3:
        score = scoreValues.get("FACT-G");
        break;
      case 4:
        score = scoreValues.get("FACT-Hep Total");
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

  public class ConsentScoreProvider extends ExtensibleScoreProvider {

    public ConsentScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    public ArrayList<SurveyQuestionIntf> getSurvey(
        PatientStudyExtendedData patStudy, PrintStudy study, Patient patient, boolean allAnswers) {
      ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
      if (patStudy == null) {
        return questions;
      }
      String xmlString = patStudy.getContents();
      if (xmlString == null) {
        return questions;
      }

      String response = XMLFileUtils.xPathQuery(xmlString,
          "(//Response[ref='IRCONSENT'])/item[@selected='true']/value");
      String value = "N/A";
      if ("1".equals(response)) {
        value = "Yes";
      } else if ("2".equals(response)) {
        value = "No";
      }

      ArrayList<SurveyAnswerIntf> answers = new ArrayList<>();
      RegistryAnswer a = new InputElement();
      a.setLabel("");
      a.setValue(value);
      answers.add(a);

      RegistryQuestion q = new RegistryQuestion();
      q.addText("Do you agree to participate in the research database?");
      q.setAnswered(true);
      q.setAnswers(answers);
      questions.add(q);

      return questions;
    }

/*
    public ArrayList<SurveyQuestionIntf> getSurvey(
        PatientStudyExtendedData patStudy, PrintStudy study,
        Patient patient, boolean allAnswers) {
      ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
      if (patStudy == null) {
        return questions;
      }
      String xmlDocumentString = patStudy.getContents();
      if (xmlDocumentString == null) {
        // get the file
        xmlDocumentString = XMLFileUtils.getInstance().getXML(dbp.get(), study.getStudyDescription());
        logger.debug("read xml " + xmlDocumentString);
      }

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db;
      try {
        db = dbf.newDocumentBuilder();

        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xmlDocumentString));

        Document messageDom = db.parse(is);
        Element docElement = messageDom.getDocumentElement();
        if (docElement.getTagName().equals(edu.stanford.registry.shared.survey.Constants.FORM)) {
          int index = 0;
          Element itemsNode = (Element) messageDom.getElementsByTagName(edu.stanford.registry.shared.survey.Constants.ITEMS).item(0);
          NodeList itemList = itemsNode.getElementsByTagName(edu.stanford.registry.shared.survey.Constants.ITEM);
          for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
            Element itemNode = (Element) itemList.item(itemInx);
            // check if conditional
            boolean meets = RegistryAssessmentUtils.qualifies(patient, itemNode, xmlDocumentString);
            // If they qualify for this question
//            if (meets) {
              SurveyQuestionIntf question = RegistryAssessmentUtils.getQuestion(itemNode, index, allAnswers);
              questions.add(question);
              index++;
//            }
          }
        }
      } catch (ParserConfigurationException | IOException | SAXException e) {
        logger.error(
            "Error parsing xml for patientStudy token " + patStudy.getToken() + " study " + patStudy.getStudyCode(), e);
      }

      return questions;
    }
*/
  }
}
