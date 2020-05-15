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
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.reports.ScoresExportReport;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.GlobalHealthScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.survey.Constants;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.github.susom.database.Database;
import com.github.susom.database.Flavor;

public class PedPainSurveyData extends ScoresExportReport {

  private static final Logger logger = LoggerFactory.getLogger(PedPainSurveyData.class);

  protected static final String[] FOLLOW_UP_TYPES = new String[] {PedPainCustomizer.SURVEY_FOLLOWUP, PedPainCustomizer.SURVEY_FOLLOWUP_18};

  public PedPainSurveyData(Database database, SiteInfo siteInfo) {
    super(database, siteInfo);
  }

  public List<Object> getAssessmentValues(AssessmentRegistration asmt) {
    List<ReportColumn> reportColumns = getColumnDefs();
    List<Object> values = new ArrayList<>();
    for(ReportColumn column : reportColumns) {
      values.addAll(column.getValues(asmt));
    }
    return values;
  }
  /**
   * Get the column definitions for the report.
   */
  @Override
  protected List<ReportColumn> getColumnDefs() {
    List<ReportColumn> columns = new ArrayList<>();
    columns.add(new PatientColumns());

    columns.add(new SurveyRegColumns());
    columns.add(new SurveyFollowUpColumns());
    columns.add(new ChildAssistedColumn("assistedChild"));
    columns.add(new StartParentColumn("startParentSurvey2"));

    columns.add(new PainIntensityColumns("ped_pain", "painIntensity"));
    columns.add(new PainIntensityColumns("proxy_pain", "proxyPainIntensity"));

    // PROMIS scores
    columns.add(new ScoreColumn("ped_mobility", "PROMIS Ped Bank v1.0 - Mobility", true));
    columns.add(new ScoreColumn("proxy_mobility", "PROMIS Parent Proxy Bank v1.0 - Mobility", true));
    columns.add(new ScoreColumn("ped_pain_inter", "PROMIS Ped Bank v1.0 - Pain Interference", false));
    columns.add(new ScoreColumn("proxy_pain_inter", "PROMIS Parent Proxy Bank v1.0 - Pain Interference", false));
    columns.add(new ScoreColumn("ped_peer_rel", "PROMIS Ped Bank v1.0 - Peer Rel", true));
    columns.add(new ScoreColumn("proxy_peer_rel", "PROMIS Parent Proxy Bank v1.0 - Peer Relations", true));
    columns.add(new ScoreColumn("ped_fatigue", "PROMIS Ped Bank v1.0 - Fatigue", false));
    columns.add(new ScoreColumn("proxy_fatigue", "PROMIS Parent Proxy Bank v1.0 - Fatigue", false));
    columns.add(new ScoreColumn("ped_anxiety", "PROMIS Ped Bank v1.1 - Anxiety", false));
    columns.add(new ScoreColumn("proxy_anxiety", "PROMIS Parent Proxy Bank v1.1 - Anxiety", false));
    columns.add(new ScoreColumn("ped_depressive", "PROMIS Ped Bank v1.1 - Depressive Sx", false));
    columns.add(new ScoreColumn("proxy_depressive", "PROMIS Parent Proxy Bank v1.1 - Depressive Sx", false));

    columns.add(new ScoreColumn("adult_physical", "PROMIS Bank v1.2 - Physical Function", true));
    columns.add(new ScoreColumn("adult_pain_inter", "PROMIS Bank v1.1 - Pain Interference", false));
    columns.add(new ScoreColumn("adult_pain_behavior", "PROMIS Pain Behavior Bank", false));
    columns.add(new ScoreColumn("adult_social_iso", "PROMIS Bank v2.0 - Social Isolation", false));
    columns.add(new ScoreColumn("adult_fatigue", "PROMIS Fatigue Bank", false));
    columns.add(new ScoreColumn("adult_anxiety", "PROMIS Anxiety Bank", false));
    columns.add(new ScoreColumn("adult_depression", "PROMIS Depression Bank", false));
    columns.add(new ScoreColumn("adult_sleep_dist", "PROMIS Bank v1.0 - Sleep Disturbance", false));
    columns.add(new ScoreColumn("adult_sleep_impair", "PROMIS Bank v1.0 - Sleep-Related Impairment", false));
    columns.add(new ScoreColumn("adult_anger", "PROMIS Bank v1.0 - Anger", false));

    // Other child/proxy measure scores
    columns.add(new ScoreColumn("ped_pcs", "pedPainCatastrophizingScale", false));
    columns.add(new ScoreColumn("proxy_pcs", "proxyPainCatastrophizingScale2", false));
    columns.add(new ScoreColumn("adult_pcs", "painCatastrophizingScale", false));
    columns.add(new CPAQScoreColumns("ped_cpaq", "childCPAQPainBeliefs"));
    columns.add(new CPAQScoreColumns("proxy_cpaq", "proxyCPAQPainBeliefs"));
    columns.add(new ScoreColumn("child_fad_total", "childFAD"));
    columns.add(new ScoreColumn("parent_fad_total", "parentFAD"));
    columns.add(new ScoreColumn("child_self_efficacy", "childSelfEfficacy"));
    columns.add(new ScoreColumn("proxy_self_efficacy", "proxySelfEfficacy2"));
    columns.add(new PedsQLScoreColumns("ped_ql", "childPedsQL2"));
    columns.add(new FOPQScoreColumns("child_fopq", "childFOPQ"));
    columns.add(new ScoreColumn("child_sleep_disturbance", "childSleepDisturbance"));

    // Parent measure scores
    columns.add(new ARCSScoreColumns("parent_arcs", "ARCSPainResponse", "ARCSProtect2"));
    columns.add(new GlobalHealthColumns("parent_health", "parentGlobalHealth"));

    // Item responses
    columns.add(new PCSDataColumns("ped_pcs", "pedPainCatastrophizingScale", "pedPainCatastrophizingScale"));
    columns.add(new PCSDataColumns("proxy_pcs", "proxyPainCatastrophizingScale", "proxyPainCatastrophizingScale2"));
    columns.add(new CPAQDataColumns("ped_cpaq", "childCPAQPainBeliefs"));
    columns.add(new CPAQDataColumns("proxy_cpaq", "proxyCPAQPainBeliefs"));
    columns.add(new SEDataColumns("ped_se", "childSelfEfficacy", "childSelfEfficacy"));
    columns.add(new SEDataColumns("proxy_se", "proxySelfEfficacy", "proxySelfEfficacy2"));
    columns.add(new FADDataColumns("child_fad", "childFAD"));
    columns.add(new FADDataColumns("parent_fad", "parentFAD"));
    columns.add(new PedsQLDataColumns("ped_ql", "childPedsQL2"));
    columns.add(new FOPQDataColumns("child_fopq", "childFOPQ"));
    columns.add(new SleepDisturbanceDataColumns("child_sleep", "childSleepDisturbance"));
    columns.add(new ARCSDataColumns("arcs", "ARCSPainResponse", "ARCSProtect2"));
    columns.add(new ProxyPainImprovementColumns("proxyPainImprovement", "proxyPainImprovementCap2"));

    columns.add(new BodyMapColumns("body_map", "bodymap"));
    return columns;
  }

  /**
   * Get the assessments to be included in the report.
   */
  @Override
  protected List<? extends AssessmentRegistration> getAssessments() {
    final String[] list = { "NoSurvey", "Diagnosis" };
    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<AssessmentRegistration> assessments = assessDao.getAssessmentsTypeNotInSetAndStudyCountGtZ(list, 4);
    return filterTestPatients(assessments);
  }

  /**
   * Get the assessments to be included in the report within a date range.
   */
  @Override
  protected List<? extends AssessmentRegistration> getAssessments(Date fromDt, Date toDt) {
    final String[] list = { "NoSurvey", "Diagnosis" };
    AssessDao assessDao = new AssessDao(database, siteInfo);
    List<AssessmentRegistration> assessments = assessDao.getAssessmentsTypeLikeXAndStudyCountGtY(list, 4, fromDt, toDt);
    return filterTestPatients(assessments);
  }

  private List<? extends AssessmentRegistration> filterTestPatients(List<AssessmentRegistration> assessments) {
    // Filter out test patients
    List<String> testPatientIds = database.toSelect("select patient_id from patient_test_only").queryStrings();
    List<AssessmentRegistration> result = new ArrayList<>();
    for(AssessmentRegistration assessment : assessments) {
      String patientId = assessment.getPatientId();
      if (!testPatientIds.contains(patientId)) {
        result.add(assessment);
      }
    }

    return result;
  }

  /**
   * Report column definition to return follow up period for follow up surveys.
   *
   * The follow up period is determined by looking a stand alone survey representing
   * the scheduled follow up within 30 days of the appointment. The visit type of the
   * stand alone survey indicates the follow up period. The stand alone survey for
   * scheduled follow ups is created by the PedPainSurveyScheduler.
   */
  protected class SurveyFollowUpColumns implements ReportColumn {

    public SurveyFollowUpColumns() {
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList("follow_up");
    }

    @Override
    public List<Object> getValues(AssessmentRegistration asmt) {
      String followUp = null;
      if (PedPainCustomizer.sameSurveyTypes(asmt.getAssessmentType(), FOLLOW_UP_TYPES)) {
        Date asmtDate = asmt.getAssessmentDt();
        if (asmtDate != null) {
          Date fromDate = DateUtils.getDaysFromDate(siteInfo, asmtDate, -30);
          Date toDate = DateUtils.getDaysFromDate(siteInfo, asmtDate, 30);
          String sql =
              "select visit_type from appt_registration "
            + "where visit_dt between :fromDate and :toDate and patient_id = :patientId and survey_site_id = :siteId and "
            + "  registration_type = 's' and visit_type like 'survey %' "
            + "order by visit_dt asc";
          String visitType = database.toSelect(sql)
              .argDate(":fromDate", fromDate)
              .argDate(":toDate", toDate)
              .argString(":patientId", asmt.getPatientId())
              .argLong(":siteId", asmt.getSurveySiteId())
              .queryStringOrNull();
          if (visitType != null) {
            if (visitType.equals("survey 3mn")) {
              followUp = "3 Mn";
            } else if (visitType.equals("survey 6mn")) {
              followUp = "6 Mn";
            } else if (visitType.equals("survey 9mn")) {
              followUp = "9 Mn";
            } else if (visitType.equals("survey 1yr")) {
              followUp = "1 Yr";
            } else if (visitType.equals("survey 2yr")) {
              followUp = "2 Yr";
            }
          }
        }
      }

      Object[] values = new Object[getValueNames().size()];
      values[0] = followUp;
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the patient attribute values.
   */
  protected class PatientColumns implements ReportColumn {

    public PatientColumns() {
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList("pid", "patient_id", "patient_dob", "patient_gender", "patient_race", "patient_ethnicity",
          "consent", "assent", "consent18", "prep_consent", "prep_assent", "prep_consent18", "photo_permission",
          "cap_consent", "cap_assent", "cap_video_perm", "research_db");
    }

    @Override
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
        values[2] = patient.getDtBirth();
        values[3] = getPatientAttribute(patient, edu.stanford.registry.shared.Constants.ATTRIBUTE_GENDER);
        values[4] = getPatientAttribute(patient, edu.stanford.registry.shared.Constants.ATTRIBUTE_RACE);
        values[5] = getPatientAttribute(patient, edu.stanford.registry.shared.Constants.ATTRIBUTE_ETHNICITY);
        values[6] = getPatientAttribute(patient, PedPainCustomizer.ATTR_CONSENT);
        values[7] = getPatientAttribute(patient, PedPainCustomizer.ATTR_ASSENT);
        values[8] = getPatientAttribute(patient, PedPainCustomizer.ATTR_CONSENT_18);
        values[9] = getPatientAttribute(patient, PedPainCustomizer.ATTR_PREP_CONSENT);
        values[10] = getPatientAttribute(patient, PedPainCustomizer.ATTR_PREP_ASSENT);
        values[11] = getPatientAttribute(patient, PedPainCustomizer.ATTR_PREP_CONSENT_18);
        values[12] = getPatientAttribute(patient, PedPainCustomizer.ATTR_PHOTO_PERMISSION);
        values[13] = getPatientAttribute(patient, PedPainCustomizer.ATTR_CAPTIVATE_CONSENT);
        values[14] = getPatientAttribute(patient, PedPainCustomizer.ATTR_CAPTIVATE_ASSENT);
        values[15] = getPatientAttribute(patient, PedPainCustomizer.ATTR_CAPTIVATE_VIDEO_PERMISSION);
        values[16] = getPatientAttribute(patient, PedPainCustomizer.ATTR_RESEARCH_DATABASE);
      }
      return Arrays.asList(values);
    }

    protected String getPatientAttribute(Patient patient, String attribute) {
      PatientAttribute attr = patient.getAttribute(attribute);
      return (attr == null) ? null : attr.getDataValue();
    }
  }

  /**
   * Report column definition to return the pain intensity values.
   */
  protected class PainIntensityColumns extends ScoreColumn {

    public PainIntensityColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc, false);
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList(valueName+"_worst", valueName+"_avg", valueName+"_now", valueName+"_least");
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        if (chartScore instanceof LocalScore) {
          LocalScore localScore = (LocalScore) chartScore;
          if (localScore.isAnswered(1) && (localScore.getAnswer(1) != null)) {
            values[0] = localScore.getAnswer(1).longValue();
          }
          if (localScore.isAnswered(2) && (localScore.getAnswer(2) != null)) {
            values[1] = localScore.getAnswer(2).longValue();
          }
          if (localScore.isAnswered(3) && (localScore.getAnswer(3) != null)) {
            values[2] = localScore.getAnswer(3).longValue();
          }
          if (localScore.isAnswered(4) && (localScore.getAnswer(4) != null)) {
            values[3] = localScore.getAnswer(4).longValue();
          }
        } else {
          logger.error("ChartScore is not an instance of LocalScore for PainIntensity");
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the global health values.
   */
  protected class GlobalHealthColumns extends ScoreColumn {

    public GlobalHealthColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc, true);
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList(valueName+"_physical", valueName+"_mental", valueName+"_pain");
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        if (chartScore instanceof GlobalHealthScore) {
          values[0] = invertScore(new Double( ((GlobalHealthScore)chartScore).getPhysicalHealthTScore() ).longValue());
          values[1] = invertScore(new Double( ((GlobalHealthScore)chartScore).getMentalHealthTScore() ).longValue());
          values[2] = ((GlobalHealthScore) chartScore).getAnswer(10);
        } else {
          logger.error("ChartScore is not an instance of GlobalHealthScore for GlobalHealth");
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the CPAQ scores.
   */
  protected class CPAQScoreColumns extends ScoreColumn {

    public CPAQScoreColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc);
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList(valueName+"_activities", valueName+"_pain", valueName+"_total");
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        Map<String,BigDecimal> scores = chartScore.getScores();
        if (scores != null) {
          values[0] = scores.get("Activities");
          values[1] = scores.get("Pain");
          values[2] = scores.get("Total");
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the ARCS scores.
   */
  protected class ARCSScoreColumns extends ScoreColumn {
    protected String studyDesc2;

    public ARCSScoreColumns(String valueName, String studyDesc, String studyDesc2) {
      super(valueName, studyDesc);
      this.studyDesc2 = studyDesc2;
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList(valueName+"_protect", valueName+"_minimize", valueName+"_distract",
          valueName+"_total");
    }

    @Override
    public List<Object> getValues(AssessmentRegistration asmt) {
      PatientStudyExtendedData patientStudy = getPatientStudy(asmt, studyDesc);
      if (patientStudy == null) {
        patientStudy = getPatientStudy(asmt, studyDesc2);
      }
      return getValues(patientStudy);
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        Map<String,BigDecimal> scores = chartScore.getScores();
        if (scores != null) {
          values[0] = scores.get("Protect");
          values[1] = scores.get("Minimize");
          values[2] = scores.get("Distract");
          values[3] = scores.get("Total");
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the ARCS data.
   */
  protected class ARCSDataColumns extends ScoreColumn {
    protected String studyDesc2;

    private String[] VALUE_NAMES = new String[] {
      "Q1",  "Q2",  "Q3",  "Q4",  "Q5",  "Q6",  "Q7",  "Q8",  "Q9",  "Q10",
      "Q11", "Q12", "Q13", "Q14", "Q15", "Q16", "Q17", "Q18", "Q19", "Q20",
      "Q21", "Q22", "Q23", "Q24", "Q25", "Q26", "Q27", "Q28", "Q29", "Q30",
      "Q31", "Q32", "Q33"
      };

    public ARCSDataColumns(String valueName, String studyDesc, String studyDesc2) {
      super(valueName, studyDesc);
      this.studyDesc2 = studyDesc2;
    }

    @Override
    public List<String> getValueNames() {
      List<String> valueNames = new ArrayList<>();
      for(int i=0; i<VALUE_NAMES.length; i++) {
        valueNames.add(VALUE_NAMES[i].replace("Q", valueName + "_q"));
      }
      return valueNames;
    }

    @Override
    public List<Object> getValues(AssessmentRegistration asmt) {
      PatientStudyExtendedData patientStudy = getPatientStudy(asmt, studyDesc);
      if (patientStudy == null) {
        patientStudy = getPatientStudy(asmt, studyDesc2);
      }
      return getValues(patientStudy);
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      LocalScore localScore = (LocalScore) chartScore;
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        for(int i=0; i<VALUE_NAMES.length; i++) {
          if (localScore.isAnswered(VALUE_NAMES[i])) {
            BigDecimal ans = localScore.getAnswer(VALUE_NAMES[i]);
            values[i] = ans;
          }
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the PCS data.
   */
  protected class PCSDataColumns extends ScoreColumn {
    protected String studyDesc2;
    private String[] VALUE_NAMES = new String[] {
      "PCS1",  "PCS2",  "PCS3",  "PCS4",  "PCS5",  "PCS6",  "PCS7",  "PCS8",  "PCS9",  "PCS10",
      "PCS11", "PCS12", "PCS13"
      };

    public PCSDataColumns(String valueName, String studyDesc, String studyDesc2) {
      super(valueName, studyDesc);
      this.studyDesc2 = studyDesc2;
    }

    @Override
    public List<String> getValueNames() {
      List<String> valueNames = new ArrayList<>();
      for(int i=0; i<VALUE_NAMES.length; i++) {
        valueNames.add(VALUE_NAMES[i].replace("PCS", valueName + "_q"));
      }
      return valueNames;
    }

    @Override
    public List<Object> getValues(AssessmentRegistration asmt) {
      PatientStudyExtendedData patientStudy = getPatientStudy(asmt, studyDesc);
      if (patientStudy == null && studyDesc2 != null) {
        patientStudy = getPatientStudy(asmt, studyDesc2);
      }
      return getValues(patientStudy);
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      LocalScore localScore = (LocalScore) chartScore;
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        for(int i=0; i<VALUE_NAMES.length; i++) {
          if (localScore.isAnswered(VALUE_NAMES[i])) {
            BigDecimal ans = localScore.getAnswer(VALUE_NAMES[i]);
            values[i] = ans;
          }
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the Self Efficacy data.
   */
  protected class SEDataColumns extends ScoreColumn {

    private String prefix;
    private String[] VALUE_NAMES = new String[] {
      "SE1",  "SE2",  "SE3",  "SE4",  "SE5",  "SE6",  "SE7"
      };
    protected String studyDesc2;
    public SEDataColumns(String valueName, String studyDesc, String studyDesc2) {
      super(valueName, studyDesc);
      this.studyDesc2 = studyDesc2;
      if (studyDesc.equals("childSelfEfficacy")) {
        prefix = "C";
      } else if (studyDesc.equals("proxySelfEfficacy") ) {
        prefix = "P";
      } else {
        prefix = "";
      }
    }

    @Override
    public List<String> getValueNames() {
      List<String> valueNames = new ArrayList<>();
      for(int i=0; i<VALUE_NAMES.length; i++) {
        valueNames.add(VALUE_NAMES[i].replace("SE", valueName + "_q"));
      }
      return valueNames;
    }

    @Override
    public List<Object> getValues(AssessmentRegistration asmt) {
      PatientStudyExtendedData patientStudy = getPatientStudy(asmt, studyDesc);
      if (patientStudy == null && studyDesc2 != null) {
        patientStudy = getPatientStudy(asmt, studyDesc2);
      }
      return getValues(patientStudy);
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      LocalScore localScore = (LocalScore) chartScore;
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        for(int i=0; i<VALUE_NAMES.length; i++) {
          String valueName = prefix + VALUE_NAMES[i];
          if (localScore.isAnswered(valueName)) {
            BigDecimal ans = localScore.getAnswer(valueName);
            values[i] = ans;
          }
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the CPAQ data.
   */
  protected class CPAQDataColumns extends ScoreColumn {

    private String[] VALUE_NAMES = new String[] {
        "Q1",  "Q2",  "Q3",  "Q4",  "Q5",  "Q6",  "Q7",  "Q8",  "Q9",  "Q10",
        "Q11", "Q12", "Q13", "Q14", "Q15", "Q16", "Q17", "Q18", "Q19", "Q20"
      };

    public CPAQDataColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc);
    }

    @Override
    public List<String> getValueNames() {
      List<String> valueNames = new ArrayList<>();
      // Special value to indicate either the 5 point response scale or
      // the 7 point response scale
      valueNames.add(valueName + "_scale");

      for(int i=0; i<VALUE_NAMES.length; i++) {
        valueNames.add(VALUE_NAMES[i].replace("Q", valueName + "_q"));
      }
      return valueNames;
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      LocalScore localScore = (LocalScore) chartScore;
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        values[0] = localScore.getAnswer("scale");
        for(int i=0; i<VALUE_NAMES.length; i++) {
          if (localScore.isAnswered(VALUE_NAMES[i])) {
            BigDecimal ans = localScore.getAnswer(VALUE_NAMES[i]);
            values[i+1] = ans;
          }
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the FAD data.
   */
  protected class FADDataColumns extends ScoreColumn {

    private String[] VALUE_NAMES = new String[] {
        "GF1",  "GF2",  "GF3",  "GF4",  "GF5",  "GF6",  "GF7",  "GF8",  "GF9",  "GF10", "GF11", "GF12"
      };

    public FADDataColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc);
    }

    @Override
    public List<String> getValueNames() {
      List<String> valueNames = new ArrayList<>();
      for(int i=0; i<VALUE_NAMES.length; i++) {
        valueNames.add(VALUE_NAMES[i].replace("GF", valueName + "_q"));
      }
      return valueNames;
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      LocalScore localScore = (LocalScore) chartScore;
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        for(int i=0; i<VALUE_NAMES.length; i++) {
          if (localScore.isAnswered(VALUE_NAMES[i])) {
            BigDecimal ans = localScore.getAnswer(VALUE_NAMES[i]);
            values[i] = ans;
          }
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the Peds QL scores.
   */
  protected class PedsQLScoreColumns extends ScoreColumn {

    public PedsQLScoreColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc);
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList(valueName+"_school");
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        Map<String,BigDecimal> scores = chartScore.getScores();
        if (scores != null) {
          values[0] = scores.get("School");
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the Peds QL data.
   */
  protected class PedsQLDataColumns extends ScoreColumn {

    private String[] VALUE_NAMES  = new String[] { "school1", "school2", "school3", "school4", "school5" };

    public PedsQLDataColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc);
    }

    @Override
    public List<String> getValueNames() {
      List<String> valueNames = new ArrayList<>();
      for(int i=0; i<VALUE_NAMES.length; i++) {
        valueNames.add(VALUE_NAMES[i].replace("school", valueName + "_school_q"));
      }
      return valueNames;
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      LocalScore localScore = (LocalScore) chartScore;
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        for(int i=0; i<VALUE_NAMES.length; i++) {
          if (localScore.isAnswered(VALUE_NAMES[i])) {
            BigDecimal ans = localScore.getAnswer(VALUE_NAMES[i]);
            values[i] = ans;
          }
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the FOPQ scores.
   */
  protected class FOPQScoreColumns extends ScoreColumn {

    public FOPQScoreColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc);
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList(valueName+"_fear", valueName+"_avoidance", valueName+"_total");
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        Map<String,BigDecimal> scores = chartScore.getScores();
        if (scores != null) {
          values[0] = scores.get("Fear");
          values[1] = scores.get("Avoidance");
          values[2] = scores.get("Total");
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the FOPQ data.
   */
  protected class FOPQDataColumns extends ScoreColumn {

    private String[] VALUE_NAMES = new String[] {
      "Q1",  "Q2",  "Q3",  "Q4",  "Q5",  "Q6",  "Q7",  "Q8",  "Q9",  "Q10"
      };

    public FOPQDataColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc);
    }

    @Override
    public List<String> getValueNames() {
      List<String> valueNames = new ArrayList<>();
      for(int i=0; i<VALUE_NAMES.length; i++) {
        valueNames.add(VALUE_NAMES[i].replace("Q", valueName + "_q"));
      }
      return valueNames;
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      LocalScore localScore = (LocalScore) chartScore;
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        for(int i=0; i<VALUE_NAMES.length; i++) {
          if (localScore.isAnswered(VALUE_NAMES[i])) {
            BigDecimal ans = localScore.getAnswer(VALUE_NAMES[i]);
            values[i] = ans;
          }
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the Sleep Disturbance data.
   */
  protected class SleepDisturbanceDataColumns extends ScoreColumn {

    private String[] VALUE_NAMES = new String[] {"Q1",  "Q2",  "Q3",  "Q4",  "Q5",  "Q6",  "Q7",  "Q8" };

    public SleepDisturbanceDataColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc);
    }

    @Override
    public List<String> getValueNames() {
      List<String> valueNames = new ArrayList<>();
      for(int i=0; i<VALUE_NAMES.length; i++) {
        valueNames.add(VALUE_NAMES[i].replace("Q", valueName + "_q"));
      }
      return valueNames;
    }

    @Override
    protected List<Object> getValues(ChartScore chartScore) {
      LocalScore localScore = (LocalScore) chartScore;
      Object[] values = new Object[getValueNames().size()];
      if (chartScore != null) {
        for(int i=0; i<VALUE_NAMES.length; i++) {
          if (localScore.isAnswered(VALUE_NAMES[i])) {
            BigDecimal ans = localScore.getAnswer(VALUE_NAMES[i]);
            values[i] = ans;
          }
        }
      }
      return Arrays.asList(values);
    }
  }

  /**
   * Report column definition to return the selected body map areas. This returns
   * a column for each of the body map areas with a value of 0 for not selected
   * and 1 for selected.
   */
  protected class BodyMapColumns extends ScoreColumn {
    List<Integer> valueCodes = new ArrayList<>();
    List<String> valueNames = new ArrayList<>();

    public BodyMapColumns(String valueName, String studyDesc) {
      super(valueName, studyDesc, false);

      // For each of the 36 areas on the front image, add it to
      // the list of codes and value names
      for(int i=101; i<=136; i++) {
        valueCodes.add(i);
        valueNames.add(valueName + "_" + i);
      }
      // For each of the 38 areas on the back image, add it to
      // the list of codes and value names
      for(int i=201; i<=238; i++) {
        valueCodes.add(i);
        valueNames.add(valueName + "_" + i);
      }
    }

    @Override
    public List<String> getValueNames() {
      return valueNames;
    }

    @Override
    public List<Object> getValues(AssessmentRegistration asmt) {
      // From 07/2015 the body map is in the childPainCurrent study
      PatientStudyExtendedData childPainCurrent = getPatientStudy(asmt, "childPainCurrent");
      if (childPainCurrent != null) {
        return getValues(childPainCurrent);
      }

      // Otherwise the body map is in the bodyMap study
      PatientStudyExtendedData bodyMap = getPatientStudy(asmt, "bodyMap");
      return getValues(bodyMap);
    }

    @Override
    protected List<Object> getValues(PatientStudyExtendedData patientStudy) {
      // Initialize all values to not selected
      Object[] values = new Object[valueCodes.size()];
      for(int i=0; i<valueCodes.size(); i++) {
        values[i] = 0;
      }

      // Get the XML document for the body map
      Document doc = null;
      if (patientStudy != null) {
        try {
          doc = ScoreService.getDocument(patientStudy);
        } catch (Exception e) {
          logger.error("Exception parsing XML for PatientStudy {}", patientStudy.getPatientStudyId(), e);
        }
      }

      if (doc != null) {
        Element docElement = doc.getDocumentElement();
        if (docElement.getTagName().equals("Form")) {
          NodeList itemsNodes = doc.getElementsByTagName("Items");
          if ((itemsNodes != null) && (itemsNodes.getLength() > 0)) {
            Element itemsNode = (Element) itemsNodes.item(0);
            NodeList itemList = itemsNode.getElementsByTagName("Item");

            if (itemList != null) {
              // The  xml contains two surveyQuestionBodymap Item nodes, one for
              // the male image and one for the female image. Only one of them
              // should have a non-empty ItemResponse attribute. The ItemResponse
              // attribute will have a comma separated list of codes for the selected
              // body map areas.
              for (int i = 0; i < itemList.getLength(); i++) {
                Element itemNode = (Element) itemList.item(i);
                String itemClass = itemNode.getAttribute(Constants.CLASS);
                if (itemClass.equals("surveyQuestionBodymap")) {
                  String itemResponse = itemNode.getAttribute("ItemResponse");
                  if ((itemResponse != null) && !itemResponse.equals("")) {
                    String[] codes = itemResponse.split(",");
                    for(String code : codes) {
                      // Find the index of the code in the list of value codes.
                      // This is also the index of the code in the returned values.
                      // Set the return value for the code to selected.
                      int index = valueCodes.indexOf(new Integer(code));
                      if (index >= 0) {
                        values[index] = 1;
                      } else {
                        logger.error("Invalid body map code {} for PatientStudy {}", code, patientStudy.getPatientStudyId());
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      return Arrays.asList(values);
    }
  }

  protected class ChildAssistedColumn extends ScoreColumn {

    public ChildAssistedColumn(String studyDesc) {
      super(null, studyDesc, false);
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList("assisted_child", "assisted_child_helper");
    }

    @Override
    protected List<Object> getValues(PatientStudyExtendedData patientStudy) {
      Object[] values = new Object[getValueNames().size()];

      // Get the xml for the patient study
      String xmlString = null;
      if (patientStudy != null) {
        xmlString = patientStudy.getContents();
      }

      if ((xmlString != null) && !xmlString.equals("")) {
        values[0] = XMLFileUtils.xPathQuery(xmlString,
            "//Response[ref='ASSISTED']/item[@selected='true']/label");
        values[1] = XMLFileUtils.xPathQuery(xmlString,
            "//Response[ref='HELPER']/item[@selected='true']/label");
      }

      return Arrays.asList(values);
    }
  }

  protected class StartParentColumn extends ScoreColumn {

    public StartParentColumn(String studyDesc) {
      super(null, studyDesc, false);
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList("parent_respondent");
    }

    @Override
    protected List<Object> getValues(PatientStudyExtendedData patientStudy) {
      Object[] values = new Object[getValueNames().size()];

      // Get the xml for the patient study
      String xmlString = null;
      if (patientStudy != null) {
        xmlString = patientStudy.getContents();
      }

      if ((xmlString != null) && !xmlString.equals("")) {
        values[0] = XMLFileUtils.xPathQuery(xmlString,
            "//Response[ref='RESPONDENT']/item[@selected='true']/label");
      }

      return Arrays.asList(values);
    }
  }
  protected class ProxyPainImprovementColumns extends ScoreColumn {
    protected String studyDesc2;

    public ProxyPainImprovementColumns(String studyDesc, String studyDesc2) {
      super(null, studyDesc);
      this.studyDesc2 = studyDesc2;
    }

    @Override
    public List<String> getValueNames() {
      return Arrays.asList(
          "visits_doctor", "visits_pt", "visits_ot", "visits_acupuncturist", "visits_psychologist", "visits_psychiatrist",
          "visits_chiropractor", "visits_massage", "visits_er", "visits_hospital", "visits_other");
    }

    @Override
    public List<Object> getValues(AssessmentRegistration asmt) {
      PatientStudyExtendedData patientStudy = getPatientStudy(asmt, studyDesc);
      if (patientStudy == null) {
        patientStudy = getPatientStudy(asmt, studyDesc2);
      }
      return getValues(patientStudy);
    }

    @Override
    protected List<Object> getValues(PatientStudyExtendedData patientStudy) {
      Object[] values = new Object[getValueNames().size()];

      // Get the xml for the patient study
      String xmlString = null;
      if (patientStudy != null) {
        xmlString = patientStudy.getContents();
      }

      if ((xmlString != null) && !xmlString.equals("")) {
        values[0] = toInteger(0,XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='medical-visits']/value"));
        values[1] = toInteger(1,XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='pt-visits']/value"));
        values[2] = toInteger(2,XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='ot-visits']/value"));
        values[3] = toInteger(3,XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='acupuncture-visits']/value"));
        values[4] = toInteger(4,XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='psychologist-visits']/value"));
        values[5] = toInteger(5,XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='psychiatrist-visits']/value"));
        values[6] = toInteger(6,XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='chiropractor-visits']/value"));
        values[7] = toInteger(7,XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='massage-visits']/value"));
        values[8] = toInteger(8,XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='emergency-visits']/value"));
        values[9] = toInteger(9,XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='inpatient-visits']/value"));
        values[10] = toInteger(10,XMLFileUtils.xPathQuery(xmlString,
            "//Response[@ref='other-visits']/value"));
      }

      return Arrays.asList(values);
    }

    private Integer toInteger(int index, String value) {
      Integer intValue = null;
      try {
        if (value != null) {
          intValue = Integer.parseInt(value);
        }
      } catch (NumberFormatException nfe) {
        logger.warn("Unable to save question {} with answer {} as integer", getValueNames().get(index), value);
      }
      return intValue;
    }
  }
}
