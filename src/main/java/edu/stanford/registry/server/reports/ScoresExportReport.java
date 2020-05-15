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
package edu.stanford.registry.server.reports;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.utils.RegistryAssessmentUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import edu.stanford.registry.shared.xform.InputElement;
import edu.stanford.registry.shared.xform.Select1Element;
import edu.stanford.registry.shared.xform.SelectElement;
import edu.stanford.registry.shared.xform.SelectItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.github.susom.database.Database;

/**
 * This class creates the Scores Export Report. The Scores Export Report has
 * one row for each assessment. Each row will have one or more columns
 * for each of the studies in the report which will contain the score(s) for
 * that study.
 */
public class ScoresExportReport {

  private static final Logger logger = LoggerFactory.getLogger(ScoresExportReport.class);

  final protected Database database;
  final protected PatientDao patientDao;
  final protected SiteInfo siteInfo;

  public ScoresExportReport(Database database, SiteInfo siteInfo) {
    this.database = database;
    this.siteInfo = siteInfo;
    User user = ServerUtils.getAdminUser(database);
    this.patientDao = new PatientDao(database, siteInfo.getSiteId(), user);
  }

  /**
   * Return a table for the scores data. The first row in the table will be a
   * header row. Each subsequent row will contain the study scores for a
   * survey registration.
   *
   * @return A list of rows where each row is a list of objects
   */
  public List<List<Object>> getReportData(Long siteId) {
    return getReportData(siteId, null, null);
  }

  public List<List<Object>> getReportData(Long siteId, Date fromDt, Date toDt) {
    if (fromDt != null && toDt != null) {
      logger.debug("Running for the date range {} to {} ", fromDt, toDt);
    } else {
      logger.debug("Running for all");
    }
    // Get the columns to be returned
    List<ReportColumn> reportColumns = getColumnDefs();

    // Create the table for the results
    List<List<Object>> reportData = new ArrayList<>();

    // Create the header row
    List<Object> headerRow = new ArrayList<>();
    for(ReportColumn column : reportColumns) {
      for(String valueName : column.getValueNames()) {
        headerRow.add(valueName);
      }
    }
    reportData.add(headerRow);

    // Get the assessments to be returned
    List<? extends AssessmentRegistration> assessments = null;
    if (fromDt == null || toDt == null) {
      assessments = getAssessments();
    } else {
      assessments = getAssessments(fromDt, toDt);
    }

    // For each assessment
    for(AssessmentRegistration asmt : assessments) {
      logger.debug("Processsing assessment " + asmt.getAssessmentRegId());
      List<Object> rowData = new ArrayList<>();
      for(ReportColumn column : reportColumns) {
        rowData.addAll(column.getValues(asmt));
      }
      reportData.add(rowData);
    }
    logger.debug("Finished. " + assessments.size() + " assessments processed.");

    return reportData;
  }

  /**
   * Get the column definitions for the report.
   */
  protected List<ReportColumn> getColumnDefs() {
    List<ReportColumn> columns = new ArrayList<>();
    columns.add(new PatientColumns());
    columns.add(new SurveyRegColumns());
    return columns;
  }

  /**
   * Get the assessments to be included in the report.
   */
  protected List<? extends AssessmentRegistration> getAssessments() {
    // Get all completed assessments
    List<AssessmentRegistration> result = new ArrayList<>();
    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<ApptRegistration> apptRegs = assessDao.getCompletedRegistrations();
    for (ApptRegistration apptReg : apptRegs) {
      result.add(apptReg.getAssessment());
    }
    return result;
  }
  /**
   * Get the assessments in the date range to be included in the report.
   */
  protected List<? extends AssessmentRegistration> getAssessments(Date fromDt, Date toDate) {
    // Get completed assessments for a date range
    List<AssessmentRegistration> result = new ArrayList<>();
    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<ApptRegistration> apptRegs = assessDao.getCompletedRegistrations(fromDt, toDate);
    for (ApptRegistration apptReg : apptRegs) {
      result.add(apptReg.getAssessment());
    }
    return result;
  }

  /**
   * Get the first patient study from the assessment which matches the study description.
   */
  protected PatientStudyExtendedData getPatientStudy(AssessmentRegistration asmt, String studyDesc) {
    List<SurveyRegistration> regs = asmt.getSurveyRegList();
    for(SurveyRegistration reg : regs) {
      // Find the patient study for the survey registration
      PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
      ArrayList<PatientStudyExtendedData> patStudies =
          patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(reg.getSurveyRegId(), studyDesc);
      if ((patStudies == null) || (patStudies.size() == 0)) {
        // patient study not found, ignore
      } else if (patStudies.size() > 1) {
        // more than one patient study found
        logger.error("More than one patient study found for survey registration " + reg.getSurveyRegId() + " and study " + studyDesc);
      } else {
        return patStudies.get(0);
      }
    }

    return null;
  }

  /**
   * Interface for a report column definition. A single report column
   * definition may define more than one actual column in the report if
   * there are multiple values to be reported for the column definition.
   */
  public interface ReportColumn {
    /**
     * Get list of value names for the columns
     */
    List<String> getValueNames();

    /**
     * Get list of values from the survey for the columns
     */
    List<Object> getValues(AssessmentRegistration asmt);
  }

  /**
   * Report column definition to return the study answers.
   */
  protected class AnswerColumns implements ReportColumn {
    protected String valuePrefix;
    protected String[] valueRefs;
    protected String studyDesc;

    public AnswerColumns(String valuePrefix, String studyDesc, String[] valueRefs) {
      this.valuePrefix = valuePrefix;
      this.valueRefs = valueRefs;
      this.studyDesc = studyDesc;
    }

    @Override
    public List<String> getValueNames() {
      List<String> valueNames = new ArrayList<>();
      for(String valueRef : valueRefs) {
        String name = valuePrefix + valueRef.replace("-", "_");
        valueNames.add(name);
      }
      return valueNames;
    }

    @Override
    public List<Object> getValues(AssessmentRegistration asmt) {
      PatientStudyExtendedData patientStudy = getPatientStudy(asmt, studyDesc);
      return getValues(patientStudy);
    }

    protected List<Object> getValues(PatientStudyExtendedData patientStudy) {
      Object[] values = new Object[getValueNames().size()];

      if (patientStudy != null) {
        Map<String,Object> answers = loadAnswers(patientStudy);
        int i = 0;
        for(String valueRef : valueRefs) {
          values[i++] = answers.get(valueRef);
        }
      }
      return Arrays.asList(values);
    }

    protected Map<String,Object> loadAnswers(PatientStudyExtendedData patientData) {
      Map<String,Object> answers = new HashMap<>();

      Document doc = null;
      try {
        doc = ScoreService.getDocument(patientData);
      } catch (Exception e) {
        logger.error("Exception parsing xml for patient " + patientData.getPatientId() + " study " + patientData.getStudyCode(), e);
      }
      if (doc == null) {
        return answers;
      }

      Element docElement = doc.getDocumentElement();
      if (docElement.getTagName().equals(Constants.FORM)) {
        NodeList itemsList = doc.getElementsByTagName(Constants.ITEMS);
        if (itemsList != null && itemsList.getLength() > 0) {
          Element itemsNode = (Element) itemsList.item(0);
          NodeList itemList = itemsNode.getElementsByTagName(Constants.ITEM);
          if (itemList != null) {
            for (int itemIndex = 0; itemIndex < itemList.getLength(); itemIndex++) {
              Element itemNode = (Element) itemList.item(itemIndex);
              loadAnswer(answers, itemNode, itemIndex);
            }
          }
        }
      }
      return answers;
    }

    protected void loadAnswer(Map<String,Object> answers, Element itemNode, int itemIndex) {
      String itemClass = itemNode.getAttribute(Constants.CLASS);
      if (itemClass.equals("surveyQuestionHorizontal")) {
        String ref = RegistryAssessmentUtils.getAttributeOrElementText(itemNode, Constants.XFORM_REF);
        Integer value = null;
        String itemScore = itemNode.getAttribute("ItemScore");
        if ((itemScore != null) && !itemScore.equals("")) {
          value = Integer.valueOf(itemScore);
        }
        answers.put(ref, value);
      } else if (itemClass.equals("surveyQuestionBodymap")) {
        // Not implemented
      } else if (itemClass.equals("surveyQuestionCollapsible")) {
        // Not implemented
      } else {
        SurveyQuestionIntf item = RegistryAssessmentUtils.getQuestion(itemNode, itemIndex, false);
        ArrayList<SurveyAnswerIntf> responses = item.getAnswers(true);
        for(SurveyAnswerIntf response : responses) {
          String ref = ((RegistryAnswer) response).getReference();
          if (response instanceof Select1Element) {
            List<SelectItem> selectItems = ((Select1Element) response).getSelectedItems();
            if (selectItems != null) {
              String value = selectItems.get(0).getValue();
              answers.put(ref, value);
            }
          } else if (response instanceof SelectElement) {
            List<SelectItem> selectItems = ((SelectElement) response).getSelectedItems();
            if (selectItems != null) {
              for(SelectItem selectItem : selectItems) {
                String value = selectItem.getValue();
                answers.put(ref + "_" + value, Integer.valueOf(1));
              }
            }
          } else if (response instanceof InputElement) {
            int dataType = ((InputElement) response).getDataType();
            Object value = ((InputElement) response).getValue();
            if (dataType == 1) {
              try {
              value = Integer.valueOf((String)value);
              } catch(NumberFormatException e) {
                logger.error("Invalid number: " + value, e);
              }
            }
            answers.put(ref, value);
          } else {
            // Not implemented
          }
        }
      }
    }
  }

  /**
   * Report column definition to return the study score.
   */
  protected class ScoreColumn implements ReportColumn{
    protected String valueName;
    protected String studyDesc;
    boolean inverted;

    public ScoreColumn(String valueName, String studyDesc) {
      this.valueName = valueName;
      this.studyDesc = studyDesc;
      this.inverted = false;
    }

    public ScoreColumn(String valueName, String studyDesc, boolean inverted) {
      this.valueName = valueName;
      this.studyDesc = studyDesc;
      this.inverted = inverted;
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList(valueName);
    }

    @Override
    public List<Object> getValues(AssessmentRegistration asmt) {
      PatientStudyExtendedData patientStudy = getPatientStudy(asmt, studyDesc);
      return getValues(patientStudy);
    }

    protected List<Object> getValues(PatientStudyExtendedData patientStudy) {
      ChartScore chartScore = null;
      // Get the chart score for the patient study
      if (patientStudy != null) {
        ScoreProvider scoreProvider = SurveyServiceFactory.getFactory(siteInfo).
            getScoreProvider(database, patientStudy.getSurveySystemName(), studyDesc);
        ArrayList<ChartScore> chartScores = scoreProvider.getScore(patientStudy);
        if ((chartScores == null) || (chartScores.size() == 0)) {
          logger.error("ScoreProvider did not return a ChartScore for PatientStudy " + patientStudy.getPatientStudyId());
        } else if (chartScores.size() > 1) {
          logger.error("ScoreProvider returned more than one ChartScore for PatientStudy " + patientStudy.getPatientStudyId());
        } else {
          chartScore = chartScores.get(0);
        }
      }
      return getValues(chartScore);
    }

    protected List<Object> getValues(ChartScore chartScore) {
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        Long score = chartScore.getScore().longValue();
        if ((score != null) && inverted) {
          score = invertScore(score);
        }
        values[0] = score;
      }
      return Arrays.asList(values);
    }

    protected Long invertScore(Long score) {
      if (score < 50) {
        score = 50 + (50 - score);
      } else if (score > 50) {
        score = 50 - (score - 50);
      }
      return score;
    }
  }

  /**
   * Report column definition for the survey registration related values.
   */
  static protected class SurveyRegColumns implements ReportColumn {
    public SurveyRegColumns() {
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList("survey_date", "survey_type");
    }

    @Override
    public List<Object> getValues(AssessmentRegistration asmt) {
      Object[] values = new Object[getValueNames().size()];
      values[0] = asmt.getAssessmentDt();
      values[1] = asmt.getAssessmentType();
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition for the patient related values.
   */
  protected class PatientColumns implements ReportColumn {
    public PatientColumns() {
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList("patient_id", "patient_dob", "patient_gender", "patient_race", "patient_ethnicity");
    }

    @Override
    public List<Object> getValues(AssessmentRegistration asmt) {
      Object[] values = new Object[getValueNames().size()];
      Patient patient = patientDao.getPatient(asmt.getPatientId());
      if (patient != null) {
        values[0] = patient.getPatientId();
        values[1] = patient.getDtBirth();
        values[2] = getPatientAttribute(patient, edu.stanford.registry.shared.Constants.ATTRIBUTE_GENDER);
        values[3] = getPatientAttribute(patient, edu.stanford.registry.shared.Constants.ATTRIBUTE_RACE);
        values[4] = getPatientAttribute(patient, edu.stanford.registry.shared.Constants.ATTRIBUTE_ETHNICITY);
      }
      return Arrays.asList(values);
    }

    protected String getPatientAttribute(Patient patient, String attribute) {
      PatientAttribute attr = patient.getAttribute(attribute);
      return (attr == null) ? null : attr.getDataValue();
    }
  }

}
