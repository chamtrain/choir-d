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

package edu.stanford.registry.server.reports;

import edu.stanford.registry.client.api.SurveyStudyObj;
import edu.stanford.registry.server.ResultGeneratorIntf;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.service.ApiClinicServicesUtils;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PageNumber;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.RegistryShortFormScoreProvider;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.utils.ChartUtilities;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.StringUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.GlobalHealthScore;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.MultiScore;
import edu.stanford.registry.shared.PainIntensityScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientResultType;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

public class JsonReport implements ResultGeneratorIntf {
 private static final Logger logger = LoggerFactory.getLogger(JsonReport.class);

 public static final Long VERSION = (long) 1;

 public static final String TITLE="ReportTitle";
 public static final String MRN= "MRN";
 public static final String NAME="Name";
 public static final String LNAMEF="LastNameFirst";
 public static final String AGE="AGE";
 public static final String DOB="DOB";
 public static final String GENDER="Gender";
 public static final String QUESTION="question";
 public static final String ANSWER="answer";
 public static final String PRINT_TYPES="printTypes";
 public static final String QUESTION_ARRAY="questions";
 public static final String SECTION_HEADING="SectionHeading";
 public static final String SURVEY_ARRAY="Studies";
 public static final String SCORES_ARRAY="Scores";
 private static final String ALL_SCORES_ARRAY="AllScores";
 public static final String SCORE_HEADING="ScoreTitle";
 public static final String SCORE_VALUE="ScoreValue";
 private static final String SCORE_DATE="ScoreDate";
 public static final String SCORE_PERCENTILE="ScorePercentile";
 public static final String SCORE_CATEGORY="ScoreCategory";
 public static final String INVERTED_MSG="Inverted";
 public static final String ASSISTED_MSG="Assisted";
 public static final String CONTROL_ID="DocumentControlId";
 public static final String TABLE="Table";
 public static final String HEADING="Heading";
 public static final String ROWS="Rows";

  private final Supplier<Database> dbp;
  private final Database database;
  private final PatientDao patientDao;
  private final ChartUtilities chartUtils;

  PageNumber pageCount;
  private JSONObject reportJson;
  private final SiteInfo siteInfo;
  private final User user;
  private final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
  // defaults
  public JsonReport(Supplier<Database> dbp, SiteInfo siteInfo, User user) {
    this.dbp = dbp;
    this.database = dbp.get();
    this.patientDao = new PatientDao(dbp.get(), siteInfo.getSiteId(), user);
    chartUtils = new ChartUtilities(siteInfo);
    /* get a studyCode:studyName map of the studies in the patients array */
    pageCount = new PageNumber(0);
    this.siteInfo = siteInfo;
    this.user = user;
  }

  public JSONObject makeJson(ArrayList<PatientStudyExtendedData> patientStudies, String process, String patientId,
      ChartConfigurationOptions opts) throws JSONException {
    logger.debug("makeJson starting");
    this.reportJson = new JSONObject();
    if (patientId == null || patientStudies == null || patientStudies.size() < 1) {
      writeError(patientId, "No completed surveys were found");
      return reportJson;
    }
    Patient patient = patientDao.getPatient(patientId);
    if (patient == null) {
      writeError(patientId, "Patient record was not found " + patientId);
      return reportJson;
    }
    PatientInfo patientInfo = new PatientInfo(patient);
    /* get the study names ordered by print order */
    ArrayList<PrintStudy> printStudies;
    try {
      printStudies = getPrintStudies(process);
    } catch (Exception e) {
      logger.error("Error getting ordered list of study descriptions from process.xml", e);
      printStudies = new ArrayList<>();
    }
    if (printStudies == null || printStudies.size() < 1) {
      logger.error("Error getting list of studies for process: {}", process);
      writeError(patientInfo.getMrn(), "Unknown survey type " + process);
      return reportJson;
    }
    for ( PrintStudy printStudy : printStudies ) {
      if (prints(printStudy)) {
        SurveyServiceFactory ssfactory = SurveyServiceFactory.getFactory(siteInfo);
        ScoreProvider provider = ssfactory.getScoreProvider(dbp, printStudy.getSurveySystemName(),
          printStudy.getStudyDescription());
        JSONObject studyJson = new JSONObject();
        if (printStudy.hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_TEXT) ||
            printStudy.hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_IMG)) {
          try {
            ArrayList<SurveyQuestionIntf> questions = provider.getSurvey(patientStudies, printStudy,
              patientDao.getPatient(patientId), false);
          for (SurveyQuestionIntf question : questions) {
            //if (questions.get(qInx).getAnswered()) {
            JSONObject jsonQuestion = makeJsonQuestion(question);
              /* Make sure there's both a question and answer */
              if (jsonQuestion.has(QUESTION) && jsonQuestion.has(ANSWER)) {
                studyJson.accumulate(QUESTION_ARRAY, jsonQuestion);
              }
            }
          } catch (RuntimeException ex) {
            logger.warn(printStudy.getStudyDescription()+ " could not be added to jsonreport: " +  ex.getMessage());
            JSONObject jQuestion = new JSONObject();
            jQuestion.append(QUESTION, printStudy.getStudyDescription());
            jQuestion.append(ANSWER, "Questionnaire is not available.");
            studyJson.accumulate(QUESTION_ARRAY, jQuestion);
          }
        }
        if (printStudy.hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_TABLE)) {
          Table reportTable = provider.getTable(patientStudies, printStudy, patient);
          studyJson.put(TABLE, tableToJson(reportTable));
        }
        if (printStudy.hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_CHART)) {
          try {
            addScores(patientStudies, printStudy, opts, studyJson);
          } catch (Exception e) {
            logger.error("could not get scores for study " + printStudy.getStudyCode(), e);
          }
        }
        if (printStudy.getStudyDescription() != null && printStudy.getStudyDescription().length() > 0) {
          studyJson.put("StudyDescription", printStudy.getStudyDescription());
        }
        if (printStudy.getTitle() != null && printStudy.getTitle().length() > 0) {
          studyJson.put(SECTION_HEADING, printStudy.getTitle());
        }
        studyJson.put("StudyCode", printStudy.getStudyCode());
        studyJson.put("SurveySystemId", printStudy.getSurveySystemId());
        studyJson.put("SurveySystemName", printStudy.getSurveySystemName());
        reportJson.append(SURVEY_ARRAY, studyJson);
      }
    }
    boolean assisted = false;
    for (PatientStudyExtendedData patientStudy : patientStudies) {
      if (patientStudy.wasAssisted()) assisted = true;
    }
    reportJson.put("Assisted", assisted);
    reportJson.put(MRN, patientInfo.getMrn());
    reportJson.put(NAME, patientInfo.getPatientName());
    reportJson.put(LNAMEF, patientInfo.getLastNameFirst());
    reportJson.put(AGE, patientInfo.getAge());
    reportJson.put(DOB, patientInfo.getBirthDt());
    reportJson.put(TITLE, getResultTitle());
    reportJson.put(GENDER, patientInfo.getGender());
    reportJson.put(INVERTED_MSG, "* Scores and percentiles have been inverted");
    if (assisted) reportJson.put(ASSISTED_MSG, "This was an assisted survey");
    reportJson.put(CONTROL_ID,getDocumentControlId());
    return reportJson;
  }

  public JSONObject makeClientJson(ArrayList<PatientStudyExtendedData> patientStudies, String process, String patientId) throws IOException, JSONException {
    logger.debug("makeClientJson starting");
    ApiClinicServicesUtils utils = new ApiClinicServicesUtils(user, dbp, siteInfo);
    AssessDao assessDao = new AssessDao(dbp.get(), siteInfo);
    SurveyQuery surveyQuery = new SurveyQuery(dbp.get(), new SurveyDao(dbp.get()), siteInfo.getSiteId());
    this.reportJson = new JSONObject();
    if (patientId == null || patientStudies == null || patientStudies.size() < 1) {
      writeError(patientId, "No completed surveys were found");
      return reportJson;
    }
    Patient patient = patientDao.getPatient(patientId);
    if (patient == null) {
      writeError(patientId, "Patient record was not found " + patientId);
      return reportJson;
    }
    PatientInfo patientInfo = new PatientInfo(patient);
    /* get the study names ordered by print order */
    ArrayList<PrintStudy> printStudies;
    try {
      printStudies = getPrintStudies(process);
    } catch (Exception e) {
      logger.error("Error getting ordered list of study descriptions from process.xml", e);
      printStudies = new ArrayList<>();
    }
    if (printStudies == null || printStudies.size() < 1) {
      logger.error("Error getting list of studies for process: " + process);
      writeError(patientInfo.getMrn(), "Unknown survey type " + process);
      return reportJson;
    }

    for ( PrintStudy printStudy : printStudies ) {
      if (prints(printStudy)) {
        JSONObject studyJson = new JSONObject();

        if (printStudy.hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_TEXT) ||
            printStudy.hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_IMG)) {
          try {
            // get the last one of this study type
            PatientStudyExtendedData patStudy = null;
            for (PatientStudyExtendedData patientStudy : patientStudies) {
              if (patientStudy.getStudyCode().intValue() == printStudy.getStudyCode().intValue()) {
                patStudy = patientStudy;
              }
            }

            if (patStudy != null) {
              Survey survey = surveyQuery.surveyBySurveyToken(patStudy.getToken());
              SurveyStudyObj surveyStudyObj = utils.getStudyObj(survey, assessDao.getRegistration(patStudy.getToken()), patStudy);
              studyJson.put("SURVEYSTUDYOBJ", new JSONObject(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(surveyStudyObj)).getPayload()));
            }
          } catch (RuntimeException ex) {
            logger.warn(printStudy.getStudyDescription()+ " could not be added to jsonreport: " +  ex.getMessage());
            JSONObject jQuestion = new JSONObject();
            jQuestion.append(QUESTION, printStudy.getStudyDescription());
            jQuestion.append(ANSWER, "Questionnaire is not available.");
            studyJson.accumulate(QUESTION_ARRAY, jQuestion);
          }
        }

        if (printStudy.getStudyDescription() != null && printStudy.getStudyDescription().length() > 0) {
          studyJson.put("StudyDescription", printStudy.getStudyDescription());
        }
        if (printStudy.getTitle() != null && printStudy.getTitle().length() > 0) {
          studyJson.put(SECTION_HEADING, printStudy.getTitle());
        }
        studyJson.put("StudyCode", printStudy.getStudyCode());
        studyJson.put("SurveySystemId", printStudy.getSurveySystemId());
        studyJson.put("SurveySystemName", printStudy.getSurveySystemName());
        reportJson.append(SURVEY_ARRAY, studyJson);
      }
    }
    boolean assisted = false;
    for (PatientStudyExtendedData patientStudy : patientStudies) {
      if (patientStudy.wasAssisted()) assisted = true;
    }
    reportJson.put("Assisted", assisted);
    reportJson.put(MRN, patientInfo.getMrn());
    reportJson.put(NAME, patientInfo.getPatientName());
    reportJson.put(LNAMEF, patientInfo.getLastNameFirst());
    reportJson.put(AGE, patientInfo.getAge());
    reportJson.put(DOB, patientInfo.getBirthDt());
    reportJson.put(TITLE, getResultTitle());
    reportJson.put(GENDER, patientInfo.getGender());
    reportJson.put(INVERTED_MSG, "* Scores and percentiles have been inverted");
    if (assisted) reportJson.put(ASSISTED_MSG, "This was an assisted survey");
    reportJson.put(CONTROL_ID,getDocumentControlId());
    return reportJson;
  }

  private JSONObject makeJsonQuestion(SurveyQuestionIntf questionIntf) throws JSONException {

    JSONObject jQuestion = new JSONObject();

    if ("surveyQuestionHorizontal".equals(questionIntf.getAttribute("Class"))) {
       // Just used for pain intensity right now
       for (SurveyAnswerIntf answerIntf : questionIntf.getAnswers()) {
         assert answerIntf.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_RADIO;
         if (answerIntf.getText().size() > 0) {
           jQuestion.append(ANSWER, answerIntf.getText().get(0));
         }
       }
    } else if ("surveyQuestionBodymap".equals(questionIntf.getAttribute("Class"))) {
       if (!ServerUtils.isEmpty(questionIntf.getAttribute(edu.stanford.registry.shared.survey.Constants.ITEM_RESPONSE))) {
        jQuestion.append(ANSWER, questionIntf.getAttribute(edu.stanford.registry.shared.survey.Constants.ITEM_RESPONSE));
      }
    } else {
      for (SurveyAnswerIntf answerIntf : questionIntf.getAnswers(true)) {
        /* If multiple choice concatenate the answers */
        ArrayList<String> ansStrings = answerIntf.getResponse();
        if (answerIntf.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_SELECT ||
            answerIntf.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_SELECT1) {
          StringBuilder answerText = new StringBuilder();
          for (int atInx = 0; atInx < ansStrings.size(); atInx++) {
            if (atInx > 0) {
              answerText.append(", ");
            }
            answerText.append(ansStrings.get(atInx));
          }
          jQuestion.append(ANSWER, answerText.toString());
        } else if (answerIntf.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_TEXTBOXSET) {
          SelectElement selectElement = (SelectElement) answerIntf;
          ArrayList<SelectItem> items = selectElement.getSelectedItems();
          JSONObject jsonAnswers = new JSONObject();
          for (SelectItem item : items) {
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put(edu.stanford.registry.shared.survey.Constants.XFORM_LABEL, item.getLabel());
            jsonAnswer.put(edu.stanford.registry.shared.survey.Constants.XFORM_VALUE, item.getValue() );
            jsonAnswers.append(edu.stanford.registry.shared.survey.Constants.XFORM_ITEM, jsonAnswer);
          }
          jQuestion.append(ANSWER, jsonAnswers);
        }
        else {
          if (ansStrings != null && ansStrings.size() > 0) {
            for (String str : ansStrings) {
              jQuestion.append(ANSWER, str);
            }
          }
        }
      }
    }
    for (String s : questionIntf.getText()) {
      jQuestion.append(QUESTION, StringUtils.stripOutMarkup(s));
    }
    return jQuestion;
  }

  private ArrayList<PrintStudy> getPrintStudies(String processType) {
    SurveySystDao ssDao = new SurveySystDao(database);
    ArrayList<SurveySystem> surveySystems = ssDao.getSurveySystems();
    ArrayList<Study> studies = ssDao.getStudies();
    return XMLFileUtils.getInstance(siteInfo).getPrintStudies(processType, surveySystems, studies, true);
  }

  private void addScores(ArrayList<PatientStudyExtendedData> pStudies, PrintStudy printStudy,
      ChartConfigurationOptions opts, JSONObject jsonStudy ) throws IOException, InvalidDataElementException {

    ChartInfo chartInfo = getChartInfo(pStudies, printStudy, opts);
    /*
     * Create the score json objects
     */
    JSONArray jsonAllScoresArray = new JSONArray();
    ChartScore chartScore = null;
    if (chartInfo != null) {
      ArrayList<ChartScore> scores = chartInfo.getScores();
      for (ChartScore score : scores) {
        if (score.getStudyCode().intValue() == printStudy.getStudyCode().intValue()) {
          chartScore = score;
          jsonAllScoresArray.put(makeAllScoresJSON(score, printStudy.getInvert()));
        }
      }
    }
    jsonStudy.put(ALL_SCORES_ARRAY, jsonAllScoresArray);
    if (chartScore != null) {
      try {
        String title = printStudy.getTitle();
        if (title.indexOf("PROMIS") == 0) {
          title = title.substring(7);
        }
        boolean inverted = false;
        if (printStudy.getInvert()) {
          inverted = true;
          title = title + " *";
        }

        Map<String,BigDecimal> scores = chartScore.getScores();
        for (Map.Entry<String,BigDecimal> scoreEntry : scores.entrySet()) {
          Long score = scoreEntry.getValue().longValue();
          if (inverted) {
            score = invert(score);
          }
          JSONObject jsonScore2 = null;
          if (chartScore instanceof GlobalHealthScore) {
            title = printStudy.getTitle() + " - Physical *";
            /* global health scores are already inverted */
            score = invert(Math.round(((GlobalHealthScore) chartScore).getPhysicalHealthTScore()));
            jsonScore2 = new JSONObject();
            jsonScore2.put("Inverted", inverted);
            jsonScore2.put("Assisted", chartScore.getAssisted());
            jsonScore2.put(SCORE_HEADING, printStudy.getTitle() + " - Mental *");
            Long mentalHealthScore = invert(Math.round(((GlobalHealthScore) chartScore).getMentalHealthTScore()));
            jsonScore2.put(SCORE_VALUE, mentalHealthScore);
            jsonScore2.put(SCORE_PERCENTILE,(calculatePercentile(mentalHealthScore)));
          }
          JSONObject jsonScore1 = new JSONObject();
          jsonScore1.put("Inverted", inverted);
          jsonScore1.put("Assisted", chartScore.getAssisted());
          jsonScore1.put(SCORE_HEADING, title);

          jsonScore1.put(SCORE_VALUE, score);
          if (!RegistryShortFormScoreProvider.studies[RegistryShortFormScoreProvider.PAIN_INTENSITY].equals(printStudy
              .getStudyDescription().toLowerCase()) &&
              !RegistryShortFormScoreProvider.studies[RegistryShortFormScoreProvider.BODY_MAP].equals(printStudy
              .getStudyDescription().toLowerCase())
          ) {
            jsonScore1.put(SCORE_PERCENTILE,(calculatePercentile(score)));
          }
          if (!empty(chartScore.getCategoryLabel())) {
            jsonScore1.put(SCORE_CATEGORY, chartScore.getCategoryLabel());
          }
          if (chartScore instanceof PainIntensityScore) {
            jsonScore1.put(SCORE_CATEGORY, scoreEntry.getKey());
          }
          jsonStudy.append(SCORES_ARRAY, jsonScore1);
          if (jsonScore2 != null) {
            jsonStudy.append(SCORES_ARRAY, jsonScore2);
          }
        }
      } catch (JSONException e) {
        logger.error("Error adding scores", e);
      }
    }
  }

  private ChartInfo getChartInfo(ArrayList<PatientStudyExtendedData> pstudies,
      PrintStudy printStudy, ChartConfigurationOptions opts) {
    return chartUtils.createChartInfo(this.dbp, pstudies, printStudy, false, opts);
  }

  static private class PatientInfo {
    private final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    private Patient patient;

    public PatientInfo(Patient patient) {
      this.patient = patient;
    }

    public String getPatientName() {
      if (patient != null) {
        return patient.getFirstName() + " " + patient.getLastName();
      }
      return "";
    }

    public String getLastNameFirst() {
      if (patient != null) {
        return patient.getLastName() + ", " + patient.getFirstName();
      }
      return "";
    }

    public String getMrn() {
      if (patient != null) {
        return patient.getPatientId();
      }
      return "";
    }

    public String getBirthDt() {
      if (patient != null && patient.getDtBirth() != null) {
        Date dob = new Date(patient.getDtBirth().getTime());
        return formatter.format(dob);
      }
      return "";
    }

    public String getAge() {
      if (patient != null && patient.getDtBirth() != null) {
        Date dob = new Date(patient.getDtBirth().getTime());
        int age = DateUtils.getAge(dob);
        if (age > 0) {
          return age + "";
        }
      }
      return "N/A";
    }

    public String getGender() {
      if (patient != null) {
        PatientAttribute pattrib = patient.getAttribute(Constants.ATTRIBUTE_GENDER);
        if (pattrib != null && pattrib.getDataValue().length() > 0) {
          return pattrib.getDataValue().substring(0, 1);
        }
      }
      return "NA";
    }
  }

  private void writeError(String patientId, String errorMessage) throws JSONException {
    logger.debug("{} for patient {}", errorMessage, patientId);
    reportJson.append("Error", errorMessage);
  }

  private Long calculatePercentile(Long score) {
    NormalDistribution norm = new NormalDistribution(50, 10);
    double percentile = norm.cumulativeProbability(score) * 100;
    return Math.round(percentile);
  }

  private boolean prints(PrintStudy printStudy) {
    for ( int t=0; t<Constants.XML_PROCESS_PRINT_TYPES.length; t++) {
        if (printStudy.hasPrintType(t)) {
          return true;
        }
    }
    return false;
  }

  private static final String DOC_ID_NAME = "PARPTJSON";
  private static final String FIELD_SEPARATOR = "/";
  private static final SimpleDateFormat DOC_ID_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd_HH:mm");

  private static final int UPPER_LIMIT = 99999999;

  private final Random randomGenerator = new Random(System.currentTimeMillis());
  private final String documentControlId = getResultName() + FIELD_SEPARATOR + "v" + getResultVersion() + FIELD_SEPARATOR
      + randomGenerator.nextInt(UPPER_LIMIT) + FIELD_SEPARATOR + DOC_ID_TIME_FMT.format(new Date());

  @Override
  public String getResultName() {
    return DOC_ID_NAME;
  }

  @Override
  public Long getResultVersion() {
    return VERSION;
  }

  @Override
  public String getResultTitle() {
    return getResultType().getResultTitle();
  }

  @Override
  public String getDocumentControlId() {
    return documentControlId;
  }


  @Override
  public PatientResultType getResultType() {
    AssessDao assessDao = new AssessDao(database, siteInfo);
    return assessDao.getPatientResultType(getResultName());
  }

  private boolean empty(String str) {
    if (str == null) {
      return true;
    }
    return str.length() <= 0;
  }
  private Long invert(Long score) {
    if (score < 50) {
      score = 50 + (50 - score);
    } else if (score > 50) {
      score = 50 - (score - 50);
    }
    return score;
  }
  private Long invert(BigDecimal score) {
    if (score == null) {
      return 0L;
    }
    return invert(score.longValue());
  }


  private JSONObject tableToJson(Table reportTable) throws JSONException {
    JSONObject jsonTable = new JSONObject();
    if (reportTable.getHeadings() != null && reportTable.getHeadings().size() > 0) {
      for (String heading : reportTable.getHeadings()) {
        jsonTable.append(HEADING, heading);
      }
    }
    ArrayList<TableRow> rows = reportTable.getRows();
    for (TableRow row : rows) {
      jsonTable.append(ROWS, makeJsonRow(row));
    }
    logger.debug("created jsonTable: {}", jsonTable.toString());
    return jsonTable;
  }

  private JSONObject makeJsonRow(TableRow row) throws JSONException {
    JSONObject jsonRow = new JSONObject();
    jsonRow.put("gap", row.getColumnGap());
    jsonRow.put("width", row.getWidth());
    ArrayList<TableColumn> columns = row.getColumns();
    for (TableColumn column : columns) {
      if (column != null) {
        if (column.getWidth() > 0) {
          JSONObject jsonColumn = new JSONObject();
          jsonColumn.put("value", column.getValue());
          jsonColumn.put("width", column.getWidth());
          jsonRow.append("column", jsonColumn);
        }
      }
    }
    return jsonRow;
  }

  private JSONObject makeAllScoresJSON(ChartScore chartScore, boolean inverted) {

    JSONObject jsonScore = new JSONObject();
    jsonScore.put(ASSISTED_MSG, chartScore.getAssisted());
    jsonScore.put(SCORE_CATEGORY, chartScore.getCategoryLabel());
    jsonScore.put(INVERTED_MSG, inverted);
    jsonScore.put(SCORE_CATEGORY, chartScore.getCategoryLabel());
    jsonScore.put(SCORE_DATE, formatter.format(chartScore.getDate()));

    Map<String, BigDecimal> scores = chartScore.getScores();
    if (chartScore instanceof GlobalHealthScore) {
      scores = new HashMap<>();
      scores.put(chartScore.getStudyDescription() + " - Physical",
          new BigDecimal(Math.round(((GlobalHealthScore) chartScore).getPhysicalHealthTScore())));
      scores.put(chartScore.getStudyDescription() + " - Mental",
          new BigDecimal(Math.round(((GlobalHealthScore) chartScore).getMentalHealthTScore())));
    }
    JSONArray scoreArray = new JSONArray();
    int inx = 0;
    for (String name : scores.keySet()) {
      JSONObject scoreJSON = new JSONObject();
      Long value = inverted ? invert(scores.get(name)) : scores.get(name).longValue();
      Long percentile = calculatePercentile(value);
      if (chartScore instanceof MultiScore) {
        percentile = ((MultiScore) chartScore).getPercentileScore(inx).longValue();
      }
      if (inverted) {
        name = name + " *";
      }
      scoreJSON.put(SCORE_HEADING, name);
      scoreJSON.put(SCORE_VALUE, value);
      scoreJSON.put(SCORE_PERCENTILE, percentile);
      scoreArray.put(scoreJSON);
    }
    jsonScore.put("SCORES", scoreArray);
    return jsonScore;
  }

}
