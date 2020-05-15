package edu.stanford.registry.server.shc.cfs;

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
import edu.stanford.registry.server.survey.RegistryShortFormScoreProvider;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.survey.SurveyServiceIntf;
import edu.stanford.registry.server.tools.CustomDateAxis;
import edu.stanford.registry.server.utils.EmailTemplateUtils;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.StringUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.MultiScore;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Chronic Fatigue Syndrome Survey Service
 *
 * Created by scweber on 12/4/15.
 */
public class CFSSurveyService extends RegistryAssessmentsService {

  private static final Logger logger = Logger.getLogger(CFSSurveyService.class);
  private static final String HIPAA_AUTH_RESOURCE = "shc/cfs/HIPAA_AuthAndConsent.pdf";

  public CFSSurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  protected SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended,
      SubmitStatus submitStatus, String answerJson) {
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    ActivityDao activityDao = new ActivityDao(database, patStudyExtended.getSurveySiteId());
    if ("cfsConsent2".equals(patStudyExtended.getStudyDescription())) {
      if (submitStatus == null) {
        // Handle the initial question
        PatientAttribute consentAttr = patAttribDao.getAttribute(patStudyExtended.getPatientId(), CFSCustomizer.ATTR_CFS_CONSENT);
        String consent = (consentAttr != null) ? consentAttr.getDataValue() : null;
        if ((consent != null) && consent.equals("Y")) {
          addConsentedQuestionnaires(database, patStudyExtended);
        }
      } else {
        FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
        for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
          String[] ids = fieldAnswer.getFieldId().split(":");
          if (ids.length == 3) {
            // Handle consent response
            if (ids[2].equals("CFSCONSENT")) {
              String choice = fieldAnswer.getChoice().get(0);
              if ("1".equals(choice)) {
                PatientAttribute pattribute;
                pattribute = new PatientAttribute(patStudyExtended.getPatientId(), CFSCustomizer.ATTR_CFS_CONSENT, "Y", PatientAttribute.STRING);
                patAttribDao.insertAttribute(pattribute);

                Token tok = new Token();
                Activity pactivity = new Activity(patStudyExtended.getPatientId(), Constants.ACTIVITY_CONSENTED, tok.getToken());
                activityDao.createActivity(pactivity);
                addConsentedQuestionnaires(database, patStudyExtended);
              } else if ("2".equals(choice)) {
                PatientAttribute pattribute;
                pattribute = new PatientAttribute(patStudyExtended.getPatientId(), CFSCustomizer.ATTR_CFS_CONSENT, "N", PatientAttribute.STRING);
                patAttribDao.insertAttribute(pattribute);
                pattribute = new PatientAttribute(patStudyExtended.getPatientId(), Constants.ATTRIBUTE_PARTICIPATES, "n", PatientAttribute.STRING);
                patAttribDao.insertAttribute(pattribute);

                Token tok = new Token();
                Activity pactivity = new Activity(patStudyExtended.getPatientId(), Constants.ACTIVITY_DECLINED, tok.getToken());
                activityDao.createActivity(pactivity);
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
                  // if <Item Order="3">
                  if (ids[0].equals("3")) {
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

  protected void addConsentedQuestionnaires(Database database, PatientStudy patStudyExtended) {
    AssessDao assessDao = new AssessDao(database, siteInfo);
    String surveyType = assessDao.getSurveyType(patStudyExtended.getToken());
    XMLFileUtils xmlUtils = XMLFileUtils.getInstance(siteInfo);
    String consentedProcess = xmlUtils.getAttribute(surveyType, "optional_questionnaires");
    ArrayList<Element> processList = xmlUtils.getProcessQuestionaires(consentedProcess);
    if (processList != null) {
      for (Element questionaire : processList) {
        // Register the patient in each one
        String qType = questionaire.getAttribute("type");
        SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(siteInfo).getSurveyServiceImpl(qType);
        if (surveyService == null) {
          throw new ServiceUnavailableException("No service found for type: " + qType);
        }

        try {
          surveyService.registerAssessment(database, questionaire, patStudyExtended.getPatientId(),
              new Token(patStudyExtended.getToken()),
              ServerUtils.getAdminUser(database.get()));
        } catch (Exception ex) {
          logger.error("Error registering qtype: " + qType, ex);
        }
      }
    }
  }

  protected void sendForm(String emailAddr) {
    String templateName = "Send-consent-form";

    try {
      Mailer mailer = siteInfo.getMailer();
      EmailTemplateUtils emailUtils = new EmailTemplateUtils();

      String template = emailUtils.getTemplate(siteInfo, templateName);
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
      logger.error("ERROR trying to send " + templateName + " email with " + HIPAA_AUTH_RESOURCE + " to " + emailAddr, ex);
    }
  }

  /**
   * Return the appropriate score provider based on the study name.
   */
  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    if (studyName.equals("MFI20") ) {
      return new MFIScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.startsWith("MFI")) {
      return new MFIScoreChartProvider(dbp, siteInfo, studyName);
    } else if (studyName.equals("EnergyIndex")) {
      return new EnergyIndexScoreProvider(dbp, siteInfo, studyName);
    } else if (studyName.startsWith("painIntensity")) {
      return new PainIntensityScoreProvider(dbp, siteInfo, studyName);
    } else if(studyName.equals("FSS")){
      return new FatigueSeverityScaleScoreProvider(dbp, siteInfo, studyName);
    } else if(studyName.equals("PhysicalFunction")){
      return new PhysicalFunctionScoreProvider(dbp, siteInfo, studyName);
    }
    return new RegistryShortFormScoreProvider(dbp.get(), siteInfo);
  }

  /**
   * Multidimensional Fatigue Inventory - 20 (MFI-20) score provider.
   */
  public class MFIScoreProvider extends ExtensibleScoreProvider {

    private final String[] generalQuestions = new String[] {
      "Q1_POS_FITNESS", "Q5_NEG_TIRED", "Q12_POS_RESTED", "Q16_NEG_TIRED" };
    private final String[] physicalQuestions = new String[] {
      "Q2_NEG_PHYS_ABILITY", "Q8_POS_PHYS_CAPACITY", "Q14_NEG_PHYS_COND", "Q20_POS_FITNESS" };
    private final String[] activityQuestions = new String[] {
      "Q3_POS_ACTIVE", "Q6_POS_ACHIEVEMENT", "Q10_NEG_ACHIEVEMENT", "Q17_NEG_ACHIEVEMENT" };
    private final String[] motivationQuestions = new String[] {
      "Q4_POS_PLANS", "Q9_NEG_FEAR_PLANS", "Q15_POS_PLANS", "Q18_NEG_MOTIVATION" };
    private final String[] mentalQuestions = new String[] {
      "Q7_POS_CONCENTRATION", "Q11_POS_CONCENTRATION", "Q13_NEG_CONCENTRATION", "Q19_NEG_CONCENTRATION" };

    public MFIScoreProvider (Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      int generalFatigue = 0;
      for(String question : generalQuestions) {
        if (chartScore.isAnswered(question)) {
          generalFatigue += chartScore.getAnswer(question).intValue();
        }
      }

      int physicalFatigue = 0;
      for(String question : physicalQuestions) {
        if (chartScore.isAnswered(question)) {
          physicalFatigue += chartScore.getAnswer(question).intValue();
        }
      }

      int reducedActivity = 0;
      for(String question : activityQuestions) {
        if (chartScore.isAnswered(question)) {
          reducedActivity += chartScore.getAnswer(question).intValue();
        }
      }

      int reducedMotivation = 0;
      for(String question : motivationQuestions) {
        if (chartScore.isAnswered(question)) {
          reducedMotivation += chartScore.getAnswer(question).intValue();
        }
      }

      int mentalFatigue = 0;
      for(String question : mentalQuestions) {
        if (chartScore.isAnswered(question)) {
          mentalFatigue += chartScore.getAnswer(question).intValue();
        }
      }

      int total = generalFatigue + physicalFatigue + reducedActivity + reducedMotivation + mentalFatigue;

      chartScore.setScore(new BigDecimal(0));
      Map<String,BigDecimal> scores = new LinkedHashMap<>();
      scores.put("GeneralFatigue", new BigDecimal(generalFatigue));
      scores.put("PhysicalFatigue", new BigDecimal(physicalFatigue));
      scores.put("ReducedActivity", new BigDecimal(reducedActivity));
      scores.put("ReducedMotivation", new BigDecimal(reducedMotivation));
      scores.put("MentalFatigue", new BigDecimal(mentalFatigue));
      scores.put("Total", new BigDecimal(total));
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
      colHeader.addColumn(new TableColumn("Date", 16));
      colHeader.addColumn(new TableColumn("General", 14));
      colHeader.addColumn(new TableColumn("Physical", 14));
      colHeader.addColumn(new TableColumn("Activity", 14));
      colHeader.addColumn(new TableColumn("Motivation", 14));
      colHeader.addColumn(new TableColumn("Mental", 14));
      colHeader.addColumn(new TableColumn("Total", 14));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 16));
        Map<String,BigDecimal> scoreValues = score.getScores();
        BigDecimal generalFatigue = scoreValues.get("GeneralFatigue");
        if (generalFatigue != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(generalFatigue), 14));
        } else {
          row.addColumn(new TableColumn(" ", 14));
        }
        BigDecimal physicalFatigue = scoreValues.get("PhysicalFatigue");
        if (physicalFatigue != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(physicalFatigue), 14));
        } else {
          row.addColumn(new TableColumn(" ", 14));
        }
        BigDecimal reducedActivity = scoreValues.get("ReducedActivity");
        if (reducedActivity != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(reducedActivity), 14));
        } else {
          row.addColumn(new TableColumn(" ", 14));
        }
        BigDecimal reducedMotiviation = scoreValues.get("ReducedMotivation");
        if (reducedMotiviation != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(reducedMotiviation), 14));
        } else {
          row.addColumn(new TableColumn(" ", 14));
        }
        BigDecimal mentalFatigue = scoreValues.get("MentalFatigue");
        if (mentalFatigue != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(mentalFatigue), 14));
        } else {
          row.addColumn(new TableColumn(" ", 14));
        }
        BigDecimal total = scoreValues.get("Total");
        if (total != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(total), 14));
        } else {
          row.addColumn(new TableColumn(" ", 14));
        }
        table.addRow(row);
      }

      return table;
    }
  }

  /**
   * Multidimensional Fatigue Inventory - 20 (MFI-20) score chart provider.
   *
   * This class generates the chart for a single MFI component score.
   */
  public class MFIScoreChartProvider extends ExtensibleScoreProvider {

    public MFIScoreChartProvider (Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected ChartScore getScoreInternal(PatientStudyExtendedData patientData) {
      LocalScore localScore = new MFIScore(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription());
      localScore.setAssisted(patientData.wasAssisted());

      loadAnswers(localScore, patientData);
      calculateScore(localScore, patientData);

      return localScore;
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      BigDecimal score = null;

      PatStudyDao patStudyDao = new PatStudyDao(dbp.get(), siteInfo);
      List<PatientStudyExtendedData> mfiSurveys =
          patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(patientData.getSurveyRegId(), "MFI20");
      if ((mfiSurveys != null) && (mfiSurveys.size() > 0)) {
        ScoreProvider scoreProvider = getScoreProvider(dbp, "MFI20");
        List<ChartScore> chartScores = scoreProvider.getScore(mfiSurveys.get(0));
        LocalScore mfiScore = (LocalScore) chartScores.get(0);
        Map<String, BigDecimal> scores = mfiScore.getScores();

        if (studyName.equals("MFIGeneralFatigue")) {
          score = scores.get("GeneralFatigue");
        } else if (studyName.equals("MFIPhysicalFatigue")) {
          score = scores.get("PhysicalFatigue");
        } else if (studyName.equals("MFIReducedActivity")) {
          score = scores.get("ReducedActivity");
        } else if (studyName.equals("MFIReducedMotivation")) {
          score = scores.get("ReducedMotivation");
        } else if (studyName.equals("MFIMentalFatigue")) {
          score = scores.get("MentalFatigue");
        } else if (studyName.equals("MFITotal")) {
          score = scores.get("Total");
        }
      }

      chartScore.setScore(score);
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
      if (studyName.equals("MFITotal")) {
        rangeAxis.setTickUnit(new NumberTickUnit(20));
        rangeAxis.setRange(1, 100);
      } else {
        rangeAxis.setTickUnit(new NumberTickUnit(4));
        rangeAxis.setRange(1, 20);
      }
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
   * ChartScore for MFI. This implements the MultiScore interface which
   * is used by drawScoresTable in PatientReport to generate the scores/legend
   * box for a chart. This implementation returns null for the percentile.
   */
  public class MFIScore extends LocalScore implements MultiScore {

    public MFIScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    public int getNumberOfScores() {
      return 1;
    }

    public String getTitle(int scoreNumber, String studyDescription) {
      return studyDescription;
    }

    public double getScore(int scoreNumber) {
      BigDecimal score = getScore();
      return (score != null) ? score.doubleValue() : 0;
    }

    public Double getPercentileScore(int scoreNumber) {
      return null;
    }
  }

  /**
   * Score provider for Energy Index
   */
  public class EnergyIndexScoreProvider extends ExtensibleScoreProvider {

    public EnergyIndexScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected ChartScore getScoreInternal(PatientStudyExtendedData patientData) {
      LocalScore localScore = new EnergyIndexScore(patientData.getDtChanged(), patientData.getPatientId(),
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

      table.addHeading(study.getTitle() + ": 0=Bed-ridden, 10=Normal");
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
      return plot;
    }
  }

  /**
   * EnergyIndex score. This implements the MultiScore interface which
   * is used by drawScoresTable in PatientReport to generate the scores/legend
   * box for a chart. This implementation returns null for the percentile.
   */
  public class EnergyIndexScore extends LocalScore implements MultiScore {

    public EnergyIndexScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    public int getNumberOfScores() {
      return 1;
    }

    public String getTitle(int scoreNumber, String studyDescription) {
      return studyDescription;
    }

    public double getScore(int scoreNumber) {
      BigDecimal score = getScore();
      return (score != null) ? score.doubleValue() : 0;
    }

    public Double getPercentileScore(int scoreNumber) {
      return null;
    }
  }

  /**
   * Score provider for Pain Intensity
   */
  public class PainIntensityScoreProvider extends ExtensibleScoreProvider {

    public PainIntensityScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected ChartScore getScoreInternal(PatientStudyExtendedData patientData) {
      LocalScore localScore = new PainIntensityScore(patientData.getDtChanged(), patientData.getPatientId(),
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

      table.addHeading(study.getTitle() + ": 0=No Pain, 10=Worst Pain Imaginable");

      TableRow colHeader = new TableRow();
      colHeader.addColumn(new TableColumn("Date", 20));
      colHeader.addColumn(new TableColumn("Now", 20));
      colHeader.addColumn(new TableColumn("Average", 20));
      colHeader.addColumn(new TableColumn("Least", 20));
      colHeader.addColumn(new TableColumn("Worst", 20));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));

        BigDecimal now = ((LocalScore) score).getAnswer("intensity_now");
        if (now != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(now), 20));
        } else {
          row.addColumn(new TableColumn(" ", 20));
        }
        BigDecimal average = ((LocalScore) score).getAnswer("intensity_average");
        if (average != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(average), 20));
        } else {
          row.addColumn(new TableColumn(" ", 20));
        }
        BigDecimal least = ((LocalScore) score).getAnswer("intensity_lowest");
        if (least != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(least), 20));
        } else {
          row.addColumn(new TableColumn(" ", 20));
        }
        BigDecimal worst = ((LocalScore) score).getAnswer("intensity_highest");
        if (worst != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(worst), 20));
        } else {
          row.addColumn(new TableColumn(" ", 20));
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
        PainIntensityScore score = (PainIntensityScore) stat4;

        Day day = new Day(score.getDate());
        try {
          double avg = score.getScore(1);
          timeDataSetAvg.addOrUpdate(day, avg);
        } catch (SeriesException duplicates) {
          // Ignore
        }
        try {
          double worst = score.getScore(2);
          timeDataSetWorst.addOrUpdate(day, worst);
        } catch (SeriesException duplicates) {
          // Ignore
        }
      }
      dataset.addSeries(timeDataSetAvg);
      dataset.addSeries(timeDataSetWorst);
      return dataset;
    }
  }

  /**
   * ChartScore for PainIntensity. This implements the MultiScore interface which
   * is used by drawScoresTable in PatientReport to generate the scores/legend
   * box for a chart. This implementation returns two values for the chart,
   * the average and worst pain levels.
   */
  public class PainIntensityScore extends LocalScore implements MultiScore {

    public PainIntensityScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    public int getNumberOfScores() {
      return 2;
    }

    public String getTitle(int scoreNumber, String studyDescription) {
      switch (scoreNumber) {
      case 1:
        return studyDescription + " - Average";
      case 2:
        return studyDescription + " - Worst";
      default:
        return studyDescription;
      }
    }

    public double getScore(int scoreNumber) {
      BigDecimal score = null;
      if (scoreNumber == 1) {
        score = getAnswer("intensity_average");
        if (score == null) {
          score = getAnswer("intensity_now");
        }
      } else if (scoreNumber == 2) {
        score = getAnswer("intensity_highest");
      }

      return (score != null) ? score.doubleValue() : 0;
    }

    public Double getPercentileScore(int scoreNumber) {
      return null;
    }

  }

  /**
   * Score provider for FatigueSeverityScale
   */
  public class FatigueSeverityScaleScoreProvider extends ExtensibleScoreProvider{

    public FatigueSeverityScaleScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow){
      Table table = new Table();
      if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
        return table;
      }

      table.addHeading(study.getTitle());
      TableRow colHeader = new TableRow(100);
      colHeader.setColumnGap(3);
      colHeader.addColumn(new TableColumn("Date", 65));
      colHeader.addColumn(new TableColumn("Total Score", 31));
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
  }

  /**
   * Score provider for PhysicalFunction
   */
  public class PhysicalFunctionScoreProvider extends ExtensibleScoreProvider{

    public PhysicalFunctionScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow){
      Table table = new Table();
      if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
        return table;
      }

      table.addHeading(study.getTitle());
      TableRow colHeader = new TableRow(100);
      colHeader.setColumnGap(3);
      colHeader.addColumn(new TableColumn("Date", 65));
      colHeader.addColumn(new TableColumn("Total Score", 31));
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
  }
}
