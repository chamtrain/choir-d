/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.pedpain;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.reports.ScoresExportReport;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudyExtendedData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.susom.database.Database;
import com.github.susom.database.Flavor;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

public class PedPainBackgroundData extends ScoresExportReport {

  private Map<String,List<DiagnosisData>> diagnosisMap;

  public PedPainBackgroundData(Database database, SiteInfo siteInfo) {
    super(database, siteInfo);
  }

  /**
   * Get the column definitions for the report.
   */
  protected List<ReportColumn> getColumnDefs() {
    List<ReportColumn> columns = new ArrayList<>();

    columns.add(new PatientColumns());
    columns.add(new SurveyRegColumns());

    columns.add(new AnswerColumns("", "parentInitialBackground", new String[] {
        "ethnicity",
        "race_1","race_2","race_3","race_4","race_5","race_9","race-other",
        "school-name","school-grade",
        "school-pgm_1","school-pgm_2","school-pgm_3","school-pgm_4","school-pgm_5","school-pgm_6","school-pgm_99","school-pgm-other",
        "missed-school-days"
      }));
    columns.add(new PainQuestions("child_", "childPainQuestions", new String[] {
        "pain-most-reason","pain-most-seen-dr","pain-menstrual-related","pain-menstrual-explain","what_helped"
      }));
    columns.add(new PainQuestions("proxy_", "proxyPainQuestions", new String[] {
        "pain-most-reason","pain-most-seen-dr","pain-doctors-year","pain-doctors-ever","visits-medical","visits-alternative","visits-mental"
      }));
    columns.add(new ReasonColumns());
    columns.add(new AnswerColumns("sibling", "parentInitialSiblings", new String[] {
        "1-relationship","1-age","1-lives-with-child","1-pain-problems","1-pain-problem-specify","1-psych-difficulties","1-psych-difficulties-specify","1-medical-issues","1-medical-issues-specify",
        "2-relationship","2-age","2-lives-with-child","2-pain-problems","2-pain-problem-specify","2-psych-difficulties","2-psych-difficulties-specify","2-medical-issues","2-medical-issues-specify",
        "3-relationship","3-age","3-lives-with-child","3-pain-problems","3-pain-problem-specify","3-psych-difficulties","3-psych-difficulties-specify","3-medical-issues","3-medical-issues-specify",
        "4-relationship","4-age","4-lives-with-child","4-pain-problems","4-pain-problem-specify","4-psych-difficulties","4-psych-difficulties-specify","4-medical-issues","4-medical-issues-specify"
      }));
    columns.add(new AnswerColumns("caregiver1_", "parentInitialCaregiver", new String[] {
        "relation","relation-other","age","occupation","education","education-other","ethnicity",
        "race_1","race_2","race_3","race_4","race_5","race_9","race-other","marital-status",
        "pain-problem","pain-problems_1","pain-problems_2","pain-problems_3","pain-problems_4","pain-problems_5","pain-problems_6","pain-problems_7","pain-problems_99","pain-problems-other",
        "psych-problem","psych-problem-describe","medical-issues","medical-issues-describe"
      }));
    columns.add(new AnswerColumns("caregiver", "parentInitialCaregiver", new String[] {
        "2-relation","2-relation-other","2-age","2-occupation","2-education","2-education-other","2-ethnicity",
        "2-race_1","2-race_2","2-race_3","2-race_4","2-race_5","2-race_9","2-race-other",
        "2-marital-status","2-lives-with-child","2-doesnt-live-with-child-reason",
        "2-pain-problem","2-pain-problems_1","2-pain-problems_2","2-pain-problems_3","2-pain-problems_4","2-pain-problems_5","2-pain-problems_5","2-pain-problems_7","2-pain-problems_99","2-pain-problems-other",
        "2-psych-problem","2-psych-problem-describe","2-medical-issues","2-medical-issues-describe"
      }));
    columns.add(new AnswerColumns("", "parentInitialMedProblems", new String[] {
        "asthma_1","asthma-age","arthritis_1","arthritis-age","autoimmune_1","autoimmune-age","blood-disease_1","blood-disease-age",
        "burns_1","burns-age","cancer_1","cancer-type","cancer-age","cystic-fibrosis_1","cystic-fibrosis-age","heart-disease_1","heart-disease-age",
        "diabetes_1","diabetes-type","diabetes-age","prematurity_1","abdominal-disorder_1","abdominal-disorder-age","genetic-disease_1","genetic-disease-age",
        "kidney-disease_1","kidney-disease-age","neurological-disorder_1","neurological-disorder-age","obesity_1","obesity-age",
        "transplant_1","transplant-age","sleep-disorder_1","sleep-disorder-age","thyroid-disease_1","thyroid-disease-age","other_1","other-specify","other-age"
      }));
    columns.add(new AnswerColumns("", "parentInitialMedHistory", new String[] {
        "operation","op1-reason","op1-age","op2-reason","op2-age","op3-reason","op3-age",
        "hospitalization","hosp1-reason","hosp1-age","hosp1-days","hosp2-reason","hosp2-age","hosp2-days","hosp3-reason","hosp3-age","hosp3-days",
        "emergency","emergency-pain","emergency1-reason","emergency1-age","emergency2-reason","emergency2-age","emergency3-reason","emergency3-age",
        "unconscious","unconscious1-reason","unconscious1-age","unconscious2-reason","unconscious2-age","unconscious3-reason","unconscious3-age",
        "pain-meds","other-meds","herbal-remedies","pregnancy-term","premature-weeks","pregnancy-difficulties","pregnancy-difficulties-describe",
        "delivery-difficulties","delivery-difficulties-describe","newborn-difficulties","newborn-difficulties-describe","birth-weight","birth-weight-describe"
      }));
    columns.add(new DiagnosisColumns(1));
    columns.add(new DiagnosisColumns(2));

    return columns;
  }

  /**
   * Get the assessments to be included in the report.
   */
  protected List<? extends AssessmentRegistration> getAssessments() {

    String sql = "select patient_study_id, patient_id, dt_created, xml_clob from patient_study " +
        "where survey_site_id = :siteId and " +
        "  survey_system_id = (select survey_system_id from survey_system where survey_system_name = 'Local') and " +
        "  study_code in (select study_code from study where study_description = 'diagnosisSurvey') and " +
        "  xml_clob is not null " +
        "order by dt_created desc ";
    diagnosisMap = database.toSelect(sql)
        .argLong("siteId", siteInfo.getSiteId())
        .query(new RowsHandler<Map<String,List<DiagnosisData>>>() {
          public Map<String, List<DiagnosisData>> process(Rows rs) throws Exception {
            Map<String,List<DiagnosisData>> diagnosisMap = new HashMap<>();
            while(rs.next()) {
              DiagnosisData data = new DiagnosisData();
              data.patientStudyId = rs.getLongOrNull("patient_study_id");
              data.patientId = rs.getStringOrEmpty("patient_id");
              data.dtCreated = rs.getDateOrNull("dt_created");
              data.xml = rs.getClobStringOrEmpty("xml_clob");

              List<DiagnosisData> diagnosisList = diagnosisMap.get(data.patientId);
              if (diagnosisList == null) {
                diagnosisList = new ArrayList<>();
                diagnosisMap.put(data.patientId, diagnosisList);
              }
              diagnosisList.add(data);
            }
            return diagnosisMap;
          }
        });

    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<AssessmentRegistration> assessments = assessDao.getAssessmentsTypeLikeXAndStudyCountGtY("Initial%", 4);

    // Filter out test patients
    List<String> testPatientIds = database.toSelect("select patient_id from patient_test_only").queryStrings();
    List<AssessmentRegistration> result = new ArrayList<AssessmentRegistration>();
    for(AssessmentRegistration assessment : assessments) {
      String patientId = assessment.getPatientId();
      if (!testPatientIds.contains(patientId)) {
        result.add(assessment);
      }
    }

    return result;
  }

  /**
   * Report column definition to return the patient attribute values.
   */
  protected class PatientColumns implements ReportColumn {

    public PatientColumns() {
    }

    public List<String> getValueNames() {
      return Arrays.asList("pid", "patient_id", "patient_name", "patient_email", "patient_dob", "patient_gender", "patient_race", "patient_ethnicity",
          "consent", "assent", "consent18", "prep_consent", "prep_assent", "prep_consent18", "photo_permission",
          "cap_consent", "cap_assent", "cap_video_perm", "research_db");
    }

    public List<Object> getValues(AssessmentRegistration asmt) {
      Object[] values = new Object[getValueNames().size()];
      Patient patient = patientDao.getPatient(asmt.getPatientId());
      if (patient != null) {
        // Create an anonymous patient id from the mrn
        Long pid;
        if (database.get().flavor() == Flavor.postgresql) {
          pid = database.toSelect("select mrn2tid(?)")
              .argString(patient.getPatientId())
              .queryLongOrNull();
        } else {
          pid = database.toSelect("select mrn2tid(?) from dual")
              .argString(patient.getPatientId())
              .queryLongOrNull();
        }
        values[0] = pid;
        values[1] = patient.getPatientId();
        values[2] = patient.getLastName() + ", " + patient.getFirstName();
        values[3] = patient.getEmailAddress();
        values[4] = patient.getDtBirth();
        values[5] = getPatientAttribute(patient, edu.stanford.registry.shared.Constants.ATTRIBUTE_GENDER);
        values[6] = getPatientAttribute(patient, edu.stanford.registry.shared.Constants.ATTRIBUTE_RACE);
        values[7] = getPatientAttribute(patient, edu.stanford.registry.shared.Constants.ATTRIBUTE_ETHNICITY);
        values[8] = getPatientAttribute(patient, PedPainCustomizer.ATTR_CONSENT);
        values[9] = getPatientAttribute(patient, PedPainCustomizer.ATTR_ASSENT);
        values[10] = getPatientAttribute(patient, PedPainCustomizer.ATTR_CONSENT_18);
        values[11] = getPatientAttribute(patient, PedPainCustomizer.ATTR_PREP_CONSENT);
        values[12] = getPatientAttribute(patient, PedPainCustomizer.ATTR_PREP_ASSENT);
        values[13] = getPatientAttribute(patient, PedPainCustomizer.ATTR_PREP_CONSENT_18);
        values[14] = getPatientAttribute(patient, PedPainCustomizer.ATTR_PHOTO_PERMISSION);
        values[15] = getPatientAttribute(patient, PedPainCustomizer.ATTR_CAPTIVATE_CONSENT);
        values[16] = getPatientAttribute(patient, PedPainCustomizer.ATTR_CAPTIVATE_ASSENT);
        values[17] = getPatientAttribute(patient, PedPainCustomizer.ATTR_CAPTIVATE_VIDEO_PERMISSION);
        values[18] = getPatientAttribute(patient, PedPainCustomizer.ATTR_RESEARCH_DATABASE);
      }
      return Arrays.asList(values);
    }

    protected String getPatientAttribute(Patient patient, String attribute) {
      PatientAttribute attr = patient.getAttribute(attribute);
      return (attr == null) ? null : attr.getDataValue();
    }
  }

  /**
   * Report column definition to return the pain duration responses as
   * a number of months.
   */
  protected class PainQuestions extends AnswerColumns {

    public PainQuestions(String valuePrefix, String studyDesc, String[] valueRefs) {
      super(valuePrefix, studyDesc, valueRefs);
    }

    public List<String> getValueNames() {
      List<String> valueNames = new ArrayList<>();
      valueNames.add(valuePrefix + "pain_duration");
      for(String valueRef : valueRefs) {
        String name = valuePrefix + valueRef.replace("-", "_");
        valueNames.add(name);
      }
      return valueNames;
    }

    protected List<Object> getValues(PatientStudyExtendedData patientStudy) {
      Object[] values = new Object[getValueNames().size()];

      if (patientStudy != null) {
        Map<String,Object> answers = loadAnswers(patientStudy);

        int duration = 0;
        Object years = answers.get("pain-most-years");
        if (years instanceof Number) {
          duration += ((Number)years).intValue() * 12;
        }
        Object months = answers.get("pain-most-months");
        if (months instanceof Number) {
          duration += ((Number)months).intValue();
        }
        values[0] = (duration == 0) ? null : new Integer(duration);

        int i = 1;
        for(String valueRef : valueRefs) {
          values[i++] = answers.get(valueRef);
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the selected reason in the
   * primary and secondary reason questions.
   */
  protected class ReasonColumns extends AnswerColumns {

    public ReasonColumns() {
      super(null, null, null);
    }

    public List<String> getValueNames() {
      return Arrays.asList(new String[] {"primary_reason", "secondary_reason"});
    }

    public List<Object> getValues(AssessmentRegistration asmt) {
      // From 07/2015 the primary and secondary reason questions are in parentInitialMedical
      PatientStudyExtendedData parentInitialMedical = getPatientStudy(asmt, "parentInitialMedical");
      if (parentInitialMedical != null) {
        return getValues(parentInitialMedical);
      }

      // Otherwise the questions are in primaryReason and secondaryReason
      PatientStudyExtendedData primaryReason = getPatientStudy(asmt, "primaryReason");
      PatientStudyExtendedData secondaryReason = getPatientStudy(asmt, "secondaryReason");
      return getValues(primaryReason, secondaryReason);
    }

    protected List<Object> getValues(PatientStudyExtendedData parentInitialMedical) {
      Object[] values = new Object[getValueNames().size()];

      // Get the xml for the patient study
      String xmlString = null;
      if (parentInitialMedical != null) {
        xmlString = parentInitialMedical.getContents();
      }

      if ((xmlString != null) && !xmlString.equals("")) {
        values[0] = XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='primary-reason']/item[@selected='true']/label");
        values[1] = XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='secondary-reason']/item[@selected='true']/label");
      }

      return Arrays.asList(values);
    }

    protected List<Object> getValues(PatientStudyExtendedData primaryReason, PatientStudyExtendedData secondaryReason) {
      Object[] values = new Object[getValueNames().size()];

      // Get the xml for the patient study
      String primaryXmlString = null;
      if (primaryReason != null) {
        primaryXmlString = primaryReason.getContents();
      }

      if ((primaryXmlString != null) && !primaryXmlString.equals("")) {
        values[0] = XMLFileUtils.xPathQuery(primaryXmlString,
            "//Response[ref='REASON']/item[@selected='true']/label");
      }

      // Get the xml for the patient study
      String secondaryXmlString = null;
      if (secondaryReason != null) {
        secondaryXmlString = secondaryReason.getContents();
      }

      if ((secondaryXmlString != null) && !secondaryXmlString.equals("")) {
        values[1] = XMLFileUtils.xPathQuery(secondaryXmlString,
            "//Response[ref='REASON']/item[@selected='true']/label");
      }

      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the diagnosis from the
   * Diagnosis physician survey
   */
  protected class DiagnosisColumns extends AnswerColumns {

    private final String[] names = new String[] {"diagnosis_date", "primary_psych_dx", "secondary_psych_dx", "primary_pain_dx", "secondary_pain_dx"};
    private int n;

    public DiagnosisColumns(int n) {
      super(null, null, null);
      this.n = n;
    }

    public List<String> getValueNames() {
      List<String> valueNames = new ArrayList<>();
      for(String valueName : names) {
        String name = valueName + "_" + n;
        valueNames.add(name);
      }
      return valueNames;
    }

    public List<Object> getValues(AssessmentRegistration asmt) {
      // Get the diagnosis survey data for the patient
      List<DiagnosisData> diagnosisList = diagnosisMap.get(asmt.getPatientId());
      if ((diagnosisList != null) && (diagnosisList.size() >= n)) {
        // If there is an nth diagnosis survey then get its values
        return getDiagnosisValues(diagnosisList.get(n-1));
      }

      return getDiagnosisValues(null);
    }

    protected List<Object> getDiagnosisValues(DiagnosisData diagnosisData) {
      Object[] values = new Object[getValueNames().size()];

      if (diagnosisData != null) {
        String xmlString = diagnosisData.xml;
        values[0] = diagnosisData.dtCreated;
        values[1] = XMLFileUtils.xPathQuery(xmlString,
            "//Response[ref='psych_dx']/item[@selected='true']/label");
        values[2] = XMLFileUtils.xPathQuery(xmlString,
            "//Response[ref='psych_dx2']/item[@selected='true']/label");
        values[3] = XMLFileUtils.xPathQuery(xmlString,
            "//Response[ref='pain_dx']/item[@selected='true']/label");
        values[4] = XMLFileUtils.xPathQuery(xmlString,
            "//Response[ref='pain_dx2']/item[@selected='true']/label");
      }

      return Arrays.asList(values);
    }
  }

  protected class DiagnosisData {
    Long patientStudyId;
    String patientId;
    Date dtCreated;
    String xml;
  }
}
