package edu.stanford.registry.server.shc.pedortho;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.charts.ChartMaker;
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
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.MultiScore;
import edu.stanford.registry.shared.Patient;
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
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class PedOrthoSurveyService extends RegistryAssessmentsService implements SurveyServiceIntf {
  private static final Logger logger = LoggerFactory.getLogger(PedOrthoSurveyService.class);
  private static final String TIMING_CONSENT = "TimingConsent";
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private static final String emptyForm= "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items/></Form>";
  public PedOrthoSurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended,
                                     SubmitStatus submitStatus, String answerJson) {
    if (patStudyExtended.getStudyDescription() != null && patStudyExtended.getStudyDescription().startsWith(TIMING_CONSENT)) {
      PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
      PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
      if (patStudy == null) { // doesn't exist !
        throw new DataException(
            "Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
                + patStudyExtended.getToken());
      }
      if (submitStatus != null) { // Handling answer
        if (patStudyExtended.getStudyDescription().startsWith(TIMING_CONSENT)) {
          FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
          for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
            String[] ids = fieldAnswer.getFieldId().split(":");
            if (ids.length == 3) {
              PatientDao patientDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
              if ("0".equals(ids[0]) && "1".equals(ids[1])
                  && "consent".equalsIgnoreCase(ids[2])) { // Item 1, response 2, ref 'consent'
                String value = ("1").equals(fieldAnswer.getChoice().get(0)) ? "y" : "n";
                PatientAttribute parentConsentAttr = new PatientAttribute(
                    patStudyExtended.getPatient().getPatientId(), PedOrthoCustomizer.ATTR_TIMING_PARENT_CONSENT, value, PatientAttribute.STRING);
                patientDao.insertAttribute(parentConsentAttr);
                if ("n".equals(value)) {
                  patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
                  return null;
                }
              } else if ("1".equals(ids[0]) && "1".equals(ids[1])
                  && "assent".equalsIgnoreCase(ids[2])) { // Item 2, response 2, ref 'consent'
                String value = ("1").equals(fieldAnswer.getChoice().get(0)) ? "y" : "n";
                PatientAttribute parentConsentAttr = new PatientAttribute(
                    patStudyExtended.getPatient().getPatientId(), PedOrthoCustomizer.ATTR_TIMING_CHILD_ASSENT, value, PatientAttribute.STRING);
                patientDao.insertAttribute(parentConsentAttr);
                if ("n".equals(value)) {
                  patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
                  return null;
                }
                addOptionalQuestionnaires(database, patStudy);
              }
            }
          }
        }
      } else { // Not answered yet
        if (patStudy.getContents() == null) { // first question
          Patient patient = patStudyExtended.getPatient();
          /*
             If already asked and either parent or patient said no, skip everything
           */
          if (patient.hasAttribute(PedOrthoCustomizer.ATTR_TIMING_PARENT_CONSENT) &&
              ("n").equals(patient.getAttributeString(PedOrthoCustomizer.ATTR_TIMING_PARENT_CONSENT))) {
            patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
            return null;
          }
          if (patient.hasAttribute(PedOrthoCustomizer.ATTR_TIMING_CHILD_ASSENT) &&
              ("n").equals(patient.getAttributeString(PedOrthoCustomizer.ATTR_TIMING_CHILD_ASSENT))) {
            patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
            return null;
          }
          /*
             If already asked and both said yes, skip the consent survey and add the questionnaires
           */
          if (patient != null &&
              ("y").equals(patient.getAttributeString(PedOrthoCustomizer.ATTR_TIMING_CHILD_ASSENT, "n")) &&
              ("y".equals(patient.getAttributeString(PedOrthoCustomizer.ATTR_TIMING_PARENT_CONSENT, "n"))) &&
              ("y".equals(patient.getAttributeString(Constants.ATTRIBUTE_PARTICIPATES, "n")))) {
            logger.trace("() already consented, skipping question", TIMING_CONSENT);
            patStudy = patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
            addOptionalQuestionnaires(database, patStudy);
            // And move onto the next questionnaire
            return null;
          }
        }
      }
    }
    return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
  }


  /**
   * Perform actions when an assessment is completed.
   */
  @Override
  protected void assessmentCompleted(Database database, Patient patient, PatientStudy patStudy) {
    Study scoliosisConsent = getStudy(database, "scoliosisConsent");
    if ((scoliosisConsent != null) &&
        (patStudy.getStudyCode().intValue() == scoliosisConsent.getStudyCode().intValue()) ) {
      handleScoliosisConsent(database, patient, patStudy);
    }
  }

  protected void handleScoliosisConsent(Database database, Patient patient, PatientStudy patStudy) {
    PatientDao patientDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));

    String xmlString = patStudy.getContents();

    String consentAns = XMLFileUtils.xPathQuery(xmlString, "//Response[ref='consent']/item[@selected='true']/value/text()");
    if (consentAns != null) {
      String value = consentAns.equals("1") ? "y" : "n";
      PatientAttribute consentAttr = new PatientAttribute(
          patient.getPatientId(), PedOrthoCustomizer.ATTR_SCOLIOSIS_CONSENT, value, PatientAttribute.STRING);
      patientDao.insertAttribute(consentAttr);
    }

    String parentConsentAns = XMLFileUtils.xPathQuery(xmlString, "//Response[ref='parent_consent']/item[@selected='true']/value/text()");
    if (parentConsentAns != null) {
      String value = parentConsentAns.equals("1") ? "y" : "n";
      PatientAttribute parentConsentAttr = new PatientAttribute(
          patient.getPatientId(), PedOrthoCustomizer.ATTR_SCOLIOSIS_PARENT_CONSENT, value, PatientAttribute.STRING);
      patientDao.insertAttribute(parentConsentAttr);
    }

    String childAssentAns = XMLFileUtils.xPathQuery(xmlString, "//Response[ref='child_assent']/item[@selected='true']/value/text()");
    if (childAssentAns != null) {
      String value = childAssentAns.equals("1") ? "y" : "n";
      PatientAttribute childConsentAttr = new PatientAttribute(
          patient.getPatientId(), PedOrthoCustomizer.ATTR_SCOLIOSIS_CHILD_ASSENT, value, PatientAttribute.STRING);
      patientDao.insertAttribute(childConsentAttr);
    }

    // The consent questions are required, so these will only be null if the question was not asked
    Boolean consent = getBooleanAttribute(patientDao, patient.getPatientId(), PedOrthoCustomizer.ATTR_SCOLIOSIS_CONSENT);
    Boolean parentConsent = getBooleanAttribute(patientDao, patient.getPatientId(), PedOrthoCustomizer.ATTR_SCOLIOSIS_PARENT_CONSENT);
    Boolean childAssent = getBooleanAttribute(patientDao, patient.getPatientId(), PedOrthoCustomizer.ATTR_SCOLIOSIS_CHILD_ASSENT);
    if ( ((consent == null) || consent) &&
         ((parentConsent == null) || parentConsent) &&
         ((childAssent == null) || childAssent) ) {
      addOptionalQuestionnaires(database, patStudy);
    }
  }

  protected void addOptionalQuestionnaires(Database database, PatientStudy patStudy) {
    AssessDao assessDao = new AssessDao(database, siteInfo);
    XMLFileUtils xmlUtils = XMLFileUtils.getInstance(siteInfo);

    String surveyType = assessDao.getSurveyType(patStudy.getToken());
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
          surveyService.registerAssessment(database, questionaire, patStudy.getPatientId(),
              new Token(patStudy.getToken()), ServerUtils.getAdminUser(database));
        } catch (Exception ex) {
          throw new ServiceUnavailableException("Error registering qtype: " + qType, ex);
        }
      }
    }
  }

  protected Boolean getBooleanAttribute(PatientDao patientDao, String patientId, String dataName) {
    PatientAttribute attr = patientDao.getAttribute(patientId, dataName);
    if (attr == null) {
      return null;
    }
    String strValue = attr.getDataValue();
    return (strValue != null) && strValue.equalsIgnoreCase("Y");
  }

  /**
   * Return the appropriate score provider based on the study name.
   */
  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    if (studyName.toLowerCase().startsWith("srs30")) {
      return new SRS30ScoreProvider(dbp, siteInfo, studyName);
    }
    return new ExtensibleScoreProvider(dbp, siteInfo, studyName);
  }

  /**
   * Score provider for SRS 30
   */
  static class SRS30ScoreProvider extends ExtensibleScoreProvider {

    private static final String[] functionQuestions = new String[] {
        "activity", "workactivity", "house", "financial", "goout", "changefunction", "changesports"};
    private static final String[] painQuestions = new String[] {
        "6months", "1month", "restpain", "med", "sickdays", "changepain"};
    private static final String[] appearanceQuestions = new String[] {
        "shape", "clothes", "trunk", "relationships", "attractive", "selfimage", "changeconfidence", "changeothers", "changeselfimage"};
    private static final String[] mentalHealthQuestions = new String[] {
        "nervous", "dumps", "calm", "blue", "happy"};
    private static final String[] satisfactionQuestions = new String[] {
        "satisfied", "management", "look"};

    public SRS30ScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected ChartScore getScoreInternal(PatientStudyExtendedData patientData) {
      LocalScore localScore = new SRS30Score(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription());
      localScore.setAssisted(patientData.wasAssisted());

      loadAnswers(localScore, patientData);
      calculateScore(localScore, patientData);

      return localScore;
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      // Function score
      int functionAnswers = 0;
      int functionSum = 0;
      for(String question : functionQuestions) {
        if (chartScore.isAnswered(question)) {
          int ans = chartScore.getAnswer(question).intValue();
          if (ans < 99) {
            functionAnswers = functionAnswers + 1;
            functionSum = functionSum + ans;
          }
        }
      }
      Double functionScore = null;
      if (functionAnswers >= 3) {
        functionScore = ((double)functionSum) / ((double)functionAnswers);
      }

      // Pain score
      int painAnswers = 0;
      int painSum = 0;
      for(String question : painQuestions) {
        if (chartScore.isAnswered(question)) {
          int ans = chartScore.getAnswer(question).intValue();
          if (ans < 99) {
            painAnswers = painAnswers + 1;
            painSum = painSum + ans;
          }
        }
      }
      Double painScore = null;
      if (painAnswers >= 3) {
        painScore = ((double)painSum) / ((double)painAnswers);
      }

      // Appearance score
      int appearanceAnswers = 0;
      int appearanceSum = 0;
      for(String question : appearanceQuestions) {
        if (chartScore.isAnswered(question)) {
          int ans = chartScore.getAnswer(question).intValue();
          if (ans < 99) {
            appearanceAnswers = appearanceAnswers + 1;
            appearanceSum = appearanceSum + ans;
          }
        }
      }
      Double appearanceScore = null;
      if (appearanceAnswers >= 3) {
        appearanceScore = ((double)appearanceSum) / ((double)appearanceAnswers);
      }

      // Mental Health score
      int mentalAnswers = 0;
      int mentalSum = 0;
      for(String question : mentalHealthQuestions) {
        if (chartScore.isAnswered(question)) {
          int ans = chartScore.getAnswer(question).intValue();
          if (ans < 99) {
            mentalAnswers = mentalAnswers + 1;
            mentalSum = mentalSum + ans;
          }
        }
      }
      Double mentalScore = null;
      if (mentalAnswers >= 3) {
        mentalScore = ((double)mentalSum) / ((double)mentalAnswers);
      }

      // Satisfaction score
      int satisfactionAnswers = 0;
      int satisfactionSum = 0;
      for(String question : satisfactionQuestions) {
        if (chartScore.isAnswered(question)) {
          int ans = chartScore.getAnswer(question).intValue();
          if (ans < 99) {
            satisfactionAnswers = satisfactionAnswers + 1;
            satisfactionSum = satisfactionSum + ans;
          }
        }
      }
      Double satisfactionScore = null;
      if (satisfactionAnswers >= 3) {
        satisfactionScore = ((double)satisfactionSum) / ((double)satisfactionAnswers);
      }

      // Overall score (excludes Satisfaction)
      int overallAnswers = functionAnswers + painAnswers + appearanceAnswers + mentalAnswers;
      int overallSum = functionSum + painSum + appearanceSum + mentalSum;
      Double overallScore = null;
      if (overallAnswers >= 3) {
        overallScore = ((double)overallSum) / ((double)overallAnswers);
      }

      // Total score
      int totalAnswers = functionAnswers + painAnswers + appearanceAnswers + mentalAnswers + satisfactionAnswers;
      int totalSum = functionSum + painSum + appearanceSum + mentalSum + satisfactionSum;
      Double totalScore = null;
      if (totalAnswers >= 3) {
        totalScore = ((double)totalSum) / ((double)totalAnswers);
      }

      Map<String,BigDecimal> scores = new LinkedHashMap<>();
      if (functionScore != null) {
        scores.put("Function", new BigDecimal(functionScore));
      }
      if (painScore != null) {
        scores.put("Pain", new BigDecimal(painScore));
      }
      if (appearanceScore != null) {
        scores.put("Appearance", new BigDecimal(appearanceScore));
      }
      if (mentalScore != null) {
        scores.put("Mental", new BigDecimal(mentalScore));
      }
      if (satisfactionScore != null) {
        scores.put("Satisfaction", new BigDecimal(satisfactionScore));
      }
      if (overallScore != null) {
        scores.put("Overall", new BigDecimal(overallScore));
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
      colHeader.addColumn(new TableColumn("Date", 20));
      colHeader.addColumn(new TableColumn("Function", 20));
      colHeader.addColumn(new TableColumn("Pain", 20));
      colHeader.addColumn(new TableColumn("Appearance", 20));
      colHeader.addColumn(new TableColumn("Mental", 20));
      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
        Map<String,BigDecimal> scoreValues = score.getScores();
        BigDecimal functionScore = scoreValues.get("Function");
        if (functionScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(functionScore), 20));
        } else {
          row.addColumn(new TableColumn("", 20));
        }
        BigDecimal painScore = scoreValues.get("Pain");
        if (painScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(painScore), 20));
        } else {
          row.addColumn(new TableColumn("", 20));
        }
        BigDecimal appearanceScore = scoreValues.get("Appearance");
        if (appearanceScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(appearanceScore), 20));
        } else {
          row.addColumn(new TableColumn("", 20));
        }
        BigDecimal mentalScore = scoreValues.get("Mental");
        if (mentalScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(mentalScore), 20));
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

      TimeSeries timeDataSet1 = new TimeSeries("Function");
      TimeSeries timeDataSet2 = new TimeSeries("Pain");
      TimeSeries timeDataSet3 = new TimeSeries("Appearance");
      for (ChartScore score : scores) {
        Map<String,BigDecimal> scoreValues = score.getScores();

        Day day = new Day(score.getDate());
        BigDecimal function = scoreValues.get("Function");
        if (function != null) {
          timeDataSet1.addOrUpdate(day, function);
        }
        BigDecimal pain = scoreValues.get("Pain");
        if (pain != null) {
          timeDataSet2.addOrUpdate(day, pain);
        }
        BigDecimal appearance = scoreValues.get("Appearance");
        if (appearance != null) {
          timeDataSet3.addOrUpdate(day, appearance);
        }
      }
      dataset.addSeries(timeDataSet1);
      dataset.addSeries(timeDataSet2);
      dataset.addSeries(timeDataSet3);
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
      rangeAxis.setRange(0, 5);
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
   * Chart score object for SRS 30
   */
  static class SRS30Score extends LocalScore implements MultiScore {

    public SRS30Score(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    public int getNumberOfScores() {
      return 7;
    }

    public String getTitle(int scoreNumber, String studyDescription) {
      switch(scoreNumber) {
      case 1:
        return studyDescription + " - Function/Activity";
      case 2:
        return studyDescription + " - Pain";
      case 3:
        return studyDescription + " - Self Image/Appearance";
      case 4:
        return studyDescription + " - Mental Health";
      case 5:
        return studyDescription + " - Satisfaction with Management";
      case 6:
        return studyDescription + " - Overall (excludes Satisfaction)";
      case 7:
        return studyDescription + " - Total";
      default:
        return studyDescription;
      }
    }

    public double getScore(int scoreNumber) {
      Map<String,BigDecimal> scoreValues = getScores();
      BigDecimal score;
      switch(scoreNumber) {
      case 1:
        score = scoreValues.get("Function");
        break;
      case 2:
        score = scoreValues.get("Pain");
        break;
      case 3:
        score = scoreValues.get("Appearance");
        break;
      case 4:
        score = scoreValues.get("Mental");
        break;
      case 5:
        score = scoreValues.get("Satisfaction");
        break;
      case 6:
        score = scoreValues.get("Overall");
        break;
      case 7:
        score = scoreValues.get("Total");
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
}
