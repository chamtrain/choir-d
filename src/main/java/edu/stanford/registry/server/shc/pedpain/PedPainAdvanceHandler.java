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
package edu.stanford.registry.server.shc.pedpain;

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.reports.ScoresExportReport.ReportColumn;
import edu.stanford.registry.server.survey.SurveyAdvanceBase;
import edu.stanford.registry.server.utils.SquareUtils;
import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Patient;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyAdvance;
import edu.stanford.survey.server.SurveyAdvanceHandler;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.Schema;
import com.github.susom.database.Sql;

public class PedPainAdvanceHandler extends SurveyAdvanceBase implements SurveyAdvanceHandler {

  private static final Logger log = LoggerFactory.getLogger(PedPainAdvanceHandler.class);
  private static final String tableName = "rpt_pedpain_surveys";

  public PedPainAdvanceHandler(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public boolean surveyAdvanced(SurveyAdvance surveyAdvance, Supplier<Database> dbp) {

    Database database = dbp.get();
    AssessDao assessDao = new AssessDao(database, siteInfo);
    Long apptRegId = assessDao.getApptRegIdByTokenId(surveyAdvance.getSurveyTokenId());
    if (apptRegId == null) {
      log.error("AssessDao.getApptRegIdByTokenId() did not find an apptRegId for tokenId {}", surveyAdvance.getSurveyTokenId());
      return false;
    }

    ApptRegistration apptRegistration = assessDao.getApptRegistrationByRegId(new ApptId(apptRegId));
    if (apptRegistration == null) {
      log.error("AssessDao.getApptRegistrationByRegId did not find the ApptRegistration for {}", apptRegId);
      return false;
    }

    AssessmentRegistration assessmentRegistration = assessDao.getAssessmentById(apptRegistration.getAssessmentId());
    if (assessmentRegistration == null) {
      log.error("assessDao.getAssessmentById did not find the Assessment for assessmentId {}", apptRegistration.getAssessmentId());
      return false;
    }

    PatientDao patientDao = new PatientDao(database, surveyAdvance.getSurveySiteId());
    Patient patient = patientDao.getPatient(assessmentRegistration.getPatientId());
    if (patient == null) {
      log.error("Patient {} not found for surveyTokenId {}", assessmentRegistration.getPatientId(), surveyAdvance.getSurveyTokenId());
      return false;
    }

    boolean exists = database.toSelect("select 'Y' from " + tableName + " where survey_site_id=? and survey_token_id=?")
        .argLong(surveyAdvance.getSurveySiteId())
        .argLong(surveyAdvance.getSurveyTokenId())
        .queryBooleanOrFalse();

    try {
      Sql sql = new Sql();
      int nbrArgs = 0;
      String separator;
      if (exists) {
        sql.listStart("update " + tableName + " set ");
        separator = "=?, ";
      } else {
        sql.listStart("insert into " + tableName + " (survey_site_id, survey_token_id,")
            .argLong(surveyAdvance.getSurveySiteId())
            .argLong(surveyAdvance.getSurveyTokenId());
        separator = ",";
        nbrArgs += 2;
      }

      // Metadata for the assessment
      SurveyDao surveyDao = new SurveyDao(database);
      SurveyQuery query = new SurveyQuery(database, surveyDao, surveyAdvance.getSurveySiteId());
      Survey s = query.surveyBySurveyTokenId(surveyAdvance.getSurveyTokenId());
      sql.listSeparator(separator).append("assessment_type").argString(assessmentRegistration.getAssessmentType());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_started").argDate(s.startTime());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_ended").argDate(s.endTime());
      nbrArgs++;
      sql.listSeparator(separator).append("is_complete").argBoolean(s.isComplete());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_user_time_ms").argLong(s.serverTimeMillis(Integer.MAX_VALUE));
      nbrArgs++;

      // Get the ReportColumns to be included
      PedPainSurveyData pedPainSurveyData = new PedPainSurveyData(database, siteInfo);
      List<ReportColumn> reportColumns = pedPainSurveyData.getColumnDefs();

      // Create a String array of the individual column names
      List<String> columnNames = new ArrayList<>();
      for (ReportColumn column : reportColumns) {
        columnNames.addAll(column.getValueNames());
      }

      // Get the values for this survey
      List<Object> assessmentValues = pedPainSurveyData.getAssessmentValues(assessmentRegistration);
      if (columnNames.size() != assessmentValues.size()) {
        log.warn("HEADER AND ROWDATA SIZES DON'T MATCH for assessment_reg_id {} !!!", assessmentRegistration.getAssessmentRegId());
      }

      // Add the column name and value to the SQL statement
      for (int i = 0; i < assessmentValues.size(); i++) {
        Object value = assessmentValues.get(i);
        if (value != null) {
          if (value instanceof String) {
            sql.listSeparator(separator).append(columnNames.get(i)).argString((String) value);
            nbrArgs++;
          } else if (value instanceof Date) {
            sql.listSeparator(separator).append(columnNames.get(i)).argDate((Date) value);
            nbrArgs++;
          } else if (value instanceof BigDecimal) {
            sql.listSeparator(separator).append(columnNames.get(i)).argBigDecimal((BigDecimal) value);
            nbrArgs++;
          } else if (value instanceof Integer) {
            sql.listSeparator(separator).append(columnNames.get(i)).argInteger((Integer) value);
            nbrArgs++;
          } else if (value instanceof Long) {
            sql.listSeparator(separator).append(columnNames.get(i)).argLong((Long) value);
            nbrArgs++;
          } else {
            log.warn("Column named {}  is of unknown type", columnNames.get(i));
          }
        }
      }
      if (exists) {
        sql.listEnd("=? where survey_site_id=? and survey_token_id=?")
            .argLong(surveyAdvance.getSurveySiteId())
            .argLong(surveyAdvance.getSurveyTokenId());
      } else {
        sql.append(") values (?");
        while (nbrArgs-- > 1) {
          sql.append(",?");
        }
        sql.append(")");
      }

      // Write the database
      try {
        database.toInsert(sql.sql()).apply(sql).insert(1);
      } catch (Exception ex) {
        log.error("Error processing pedpain survey_token_id {} ", surveyAdvance.getSurveyTokenId(), ex);
        return false;
      }
      return true;
    } catch (Exception e) {
      throw new RuntimeException(
          "Error storing survey_token_id " + surveyAdvance.getSurveyTokenId() + " in " + tableName, e);
    }
  }

  private static void createTable(Database database) {
    Schema schema = new Schema().addTable(tableName)
        .addColumn("survey_site_id").asLong().table()
        .addColumn("survey_token_id").asLong().table()
        .addColumn("assessment_type").asString(50).table()
        .addColumn("survey_started").asDate().table()
        .addColumn("survey_ended").asDate().table()
        .addColumn("is_complete").asBoolean().table()
        .addColumn("survey_user_time_ms").asLong().table()
        .addColumn("pid").asLong().table()
        .addColumn("patient_id").asString(50).table()
        .addColumn("patient_dob").asDate().table()
        .addColumn("patient_gender").asString(250).table()
        .addColumn("patient_race").asString(250).table()
        .addColumn("patient_ethnicity").asString(250).table()
        .addColumn("consent").asString(10).table().addColumn("assent").asString(10).table().addColumn("consent18").asString(10).table()
        .addColumn("prep_consent").asString(10).table().addColumn("prep_assent").asString(10).table()
        .addColumn("prep_consent18").asString(10).table().addColumn("photo_permission").asString(10).table()
        .addColumn("cap_consent").asString(10).table().addColumn("cap_assent").asString(10).table()
        .addColumn("cap_video_perm").asString(10).table().addColumn("research_db").asString(10).table()
        .addColumn("survey_date").asDate().table()
        .addColumn("survey_type").asString(50).table()
        .addColumn("follow_up").asString(50).table().addColumn("assisted_child").asString(50).table()
        .addColumn("assisted_child_helper").asString(250).table().addColumn("parent_respondent").asString(50).table()
        .addColumn("ped_pain_worst").asLong().table().addColumn("ped_pain_avg").asLong().table()
        .addColumn("ped_pain_now").asLong().table().addColumn("ped_pain_least").asLong().table()
        .addColumn("proxy_pain_worst").asLong().table().addColumn("proxy_pain_avg").asLong().table()
        .addColumn("proxy_pain_now").asLong().table().addColumn("proxy_pain_least").asLong().table()
        .addColumn("ped_mobility").asLong().table().addColumn("proxy_mobility").asLong().table()
        .addColumn("ped_pain_inter").asLong().table().addColumn("proxy_pain_inter").asLong().table()
        .addColumn("ped_peer_rel").asLong().table().addColumn("proxy_peer_rel").asLong().table()
        .addColumn("ped_fatigue").asLong().table().addColumn("proxy_fatigue").asLong().table()
        .addColumn("ped_anxiety").asLong().table().addColumn("proxy_anxiety").asLong().table()
        .addColumn("ped_depressive").asLong().table().addColumn("proxy_depressive").asLong().table()
        .addColumn("adult_physical").asLong().table().addColumn("adult_pain_inter").asLong().table()
        .addColumn("adult_pain_behavior").asLong().table().addColumn("adult_social_iso").asLong().table()
        .addColumn("adult_fatigue").asLong().table().addColumn("adult_anxiety").asLong().table()
        .addColumn("adult_depression").asLong().table().addColumn("adult_sleep_dist").asLong().table()
        .addColumn("adult_sleep_impair").asLong().table().addColumn("adult_anger").asLong().table()
        .addColumn("ped_pcs").asLong().table().addColumn("proxy_pcs").asLong().table()
        .addColumn("adult_pcs").asLong().table().addColumn("ped_cpaq_activities").asLong().table()
        .addColumn("ped_cpaq_pain").asLong().table().addColumn("ped_cpaq_total").asLong().table()
        .addColumn("proxy_cpaq_activities").asLong().table().addColumn("proxy_cpaq_pain").asLong().table()
        .addColumn("proxy_cpaq_total").asLong().table().addColumn("child_fad_total").asLong().table()
        .addColumn("parent_fad_total").asLong().table().addColumn("child_self_efficacy").asLong().table()
        .addColumn("proxy_self_efficacy").asLong().table().addColumn("ped_ql_school").asLong().table()
        .addColumn("child_fopq_fear").asLong().table().addColumn("child_fopq_avoidance").asLong().table()
        .addColumn("child_fopq_total").asLong().table().addColumn("child_sleep_disturbance").asLong().table()
        .addColumn("parent_arcs_protect").asLong().table().addColumn("parent_arcs_minimize").asLong().table()
        .addColumn("parent_arcs_distract").asLong().table().addColumn("parent_arcs_total").asLong().table()
        .addColumn("parent_health_physical").asLong().table().addColumn("parent_health_mental").asLong().table()
        .addColumn("parent_health_pain").asLong().table().addColumn("ped_pcs_q1").asLong().table()
        .addColumn("ped_pcs_q2").asLong().table().addColumn("ped_pcs_q3").asLong().table()
        .addColumn("ped_pcs_q4").asLong().table().addColumn("ped_pcs_q5").asLong().table()
        .addColumn("ped_pcs_q6").asLong().table().addColumn("ped_pcs_q7").asLong().table()
        .addColumn("ped_pcs_q8").asLong().table().addColumn("ped_pcs_q9").asLong().table()
        .addColumn("ped_pcs_q10").asLong().table().addColumn("ped_pcs_q11").asLong().table()
        .addColumn("ped_pcs_q12").asLong().table().addColumn("ped_pcs_q13").asLong().table()
        .addColumn("proxy_pcs_q1").asLong().table().addColumn("proxy_pcs_q2").asLong().table()
        .addColumn("proxy_pcs_q3").asLong().table().addColumn("proxy_pcs_q4").asLong().table()
        .addColumn("proxy_pcs_q5").asLong().table().addColumn("proxy_pcs_q6").asLong().table()
        .addColumn("proxy_pcs_q7").asLong().table().addColumn("proxy_pcs_q8").asLong().table()
        .addColumn("proxy_pcs_q9").asLong().table().addColumn("proxy_pcs_q10").asLong().table()
        .addColumn("proxy_pcs_q11").asLong().table().addColumn("proxy_pcs_q12").asLong().table()
        .addColumn("proxy_pcs_q13").asLong().table().addColumn("ped_cpaq_scale").asLong().table()
        .addColumn("ped_cpaq_q1").asLong().table().addColumn("ped_cpaq_q2").asLong().table()
        .addColumn("ped_cpaq_q3").asLong().table().addColumn("ped_cpaq_q4").asLong().table()
        .addColumn("ped_cpaq_q5").asLong().table().addColumn("ped_cpaq_q6").asLong().table()
        .addColumn("ped_cpaq_q7").asLong().table().addColumn("ped_cpaq_q8").asLong().table()
        .addColumn("ped_cpaq_q9").asLong().table().addColumn("ped_cpaq_q10").asLong().table()
        .addColumn("ped_cpaq_q11").asLong().table().addColumn("ped_cpaq_q12").asLong().table()
        .addColumn("ped_cpaq_q13").asLong().table().addColumn("ped_cpaq_q14").asLong().table()
        .addColumn("ped_cpaq_q15").asLong().table().addColumn("ped_cpaq_q16").asLong().table()
        .addColumn("ped_cpaq_q17").asLong().table().addColumn("ped_cpaq_q18").asLong().table()
        .addColumn("ped_cpaq_q19").asLong().table().addColumn("ped_cpaq_q20").asLong().table()
        .addColumn("proxy_cpaq_scale").asLong().table().addColumn("proxy_cpaq_q1").asLong().table()
        .addColumn("proxy_cpaq_q2").asLong().table().addColumn("proxy_cpaq_q3").asLong().table()
        .addColumn("proxy_cpaq_q4").asLong().table().addColumn("proxy_cpaq_q5").asLong().table()
        .addColumn("proxy_cpaq_q6").asLong().table().addColumn("proxy_cpaq_q7").asLong().table()
        .addColumn("proxy_cpaq_q8").asLong().table().addColumn("proxy_cpaq_q9").asLong().table()
        .addColumn("proxy_cpaq_q10").asLong().table().addColumn("proxy_cpaq_q11").asLong().table()
        .addColumn("proxy_cpaq_q12").asLong().table().addColumn("proxy_cpaq_q13").asLong().table()
        .addColumn("proxy_cpaq_q14").asLong().table().addColumn("proxy_cpaq_q15").asLong().table()
        .addColumn("proxy_cpaq_q16").asLong().table().addColumn("proxy_cpaq_q17").asLong().table()
        .addColumn("proxy_cpaq_q18").asLong().table().addColumn("proxy_cpaq_q19").asLong().table()
        .addColumn("proxy_cpaq_q20").asLong().table().addColumn("ped_se_q1").asLong().table()
        .addColumn("ped_se_q2").asLong().table().addColumn("ped_se_q3").asLong().table()
        .addColumn("ped_se_q4").asLong().table().addColumn("ped_se_q5").asLong().table()
        .addColumn("ped_se_q6").asLong().table().addColumn("ped_se_q7").asLong().table()
        .addColumn("proxy_se_q1").asLong().table().addColumn("proxy_se_q2").asLong().table()
        .addColumn("proxy_se_q3").asLong().table().addColumn("proxy_se_q4").asLong().table()
        .addColumn("proxy_se_q5").asLong().table().addColumn("proxy_se_q6").asLong().table()
        .addColumn("proxy_se_q7").asLong().table().addColumn("child_fad_q1").asLong().table()
        .addColumn("child_fad_q2").asLong().table().addColumn("child_fad_q3").asLong().table()
        .addColumn("child_fad_q4").asLong().table().addColumn("child_fad_q5").asLong().table()
        .addColumn("child_fad_q6").asLong().table().addColumn("child_fad_q7").asLong().table()
        .addColumn("child_fad_q8").asLong().table().addColumn("child_fad_q9").asLong().table()
        .addColumn("child_fad_q10").asLong().table().addColumn("child_fad_q11").asLong().table()
        .addColumn("child_fad_q12").asLong().table().addColumn("parent_fad_q1").asLong().table()
        .addColumn("parent_fad_q2").asLong().table().addColumn("parent_fad_q3").asLong().table()
        .addColumn("parent_fad_q4").asLong().table().addColumn("parent_fad_q5").asLong().table()
        .addColumn("parent_fad_q6").asLong().table().addColumn("parent_fad_q7").asLong().table()
        .addColumn("parent_fad_q8").asLong().table().addColumn("parent_fad_q9").asLong().table()
        .addColumn("parent_fad_q10").asLong().table().addColumn("parent_fad_q11").asLong().table()
        .addColumn("parent_fad_q12").asLong().table().addColumn("ped_ql_school_q1").asLong().table()
        .addColumn("ped_ql_school_q2").asLong().table().addColumn("ped_ql_school_q3").asLong().table()
        .addColumn("ped_ql_school_q4").asLong().table().addColumn("ped_ql_school_q5").asLong().table()
        .addColumn("child_fopq_q1").asLong().table().addColumn("child_fopq_q2").asLong().table()
        .addColumn("child_fopq_q3").asLong().table().addColumn("child_fopq_q4").asLong().table()
        .addColumn("child_fopq_q5").asLong().table().addColumn("child_fopq_q6").asLong().table()
        .addColumn("child_fopq_q7").asLong().table().addColumn("child_fopq_q8").asLong().table()
        .addColumn("child_fopq_q9").asLong().table().addColumn("child_fopq_q10").asLong().table()
        .addColumn("child_sleep_q1").asLong().table().addColumn("child_sleep_q2").asLong().table()
        .addColumn("child_sleep_q3").asLong().table().addColumn("child_sleep_q4").asLong().table()
        .addColumn("child_sleep_q5").asLong().table().addColumn("child_sleep_q6").asLong().table()
        .addColumn("child_sleep_q7").asLong().table().addColumn("child_sleep_q8").asLong().table()
        .addColumn("arcs_q1").asLong().table().addColumn("arcs_q2").asLong().table()
        .addColumn("arcs_q3").asLong().table().addColumn("arcs_q4").asLong().table()
        .addColumn("arcs_q5").asLong().table().addColumn("arcs_q6").asLong().table()
        .addColumn("arcs_q7").asLong().table().addColumn("arcs_q8").asLong().table()
        .addColumn("arcs_q9").asLong().table().addColumn("arcs_q10").asLong().table()
        .addColumn("arcs_q11").asLong().table().addColumn("arcs_q12").asLong().table()
        .addColumn("arcs_q13").asLong().table().addColumn("arcs_q14").asLong().table()
        .addColumn("arcs_q15").asLong().table().addColumn("arcs_q16").asLong().table()
        .addColumn("arcs_q17").asLong().table().addColumn("arcs_q18").asLong().table()
        .addColumn("arcs_q19").asLong().table().addColumn("arcs_q20").asLong().table()
        .addColumn("arcs_q21").asLong().table().addColumn("arcs_q22").asLong().table()
        .addColumn("arcs_q23").asLong().table().addColumn("arcs_q24").asLong().table()
        .addColumn("arcs_q25").asLong().table().addColumn("arcs_q26").asLong().table()
        .addColumn("arcs_q27").asLong().table().addColumn("arcs_q28").asLong().table()
        .addColumn("arcs_q29").asLong().table().addColumn("arcs_q30").asLong().table()
        .addColumn("arcs_q31").asLong().table().addColumn("arcs_q32").asLong().table()
        .addColumn("arcs_q33").asLong().table().addColumn("visits_doctor").asLong().table()
        .addColumn("visits_pt").asLong().table().addColumn("visits_ot").asLong().table()
        .addColumn("visits_acupuncturist").asLong().table().addColumn("visits_psychologist").asLong().table()
        .addColumn("visits_psychiatrist").asLong().table().addColumn("visits_chiropractor").asLong().table()
        .addColumn("visits_massage").asLong().table().addColumn("visits_er").asLong().table()
        .addColumn("visits_hospital").asLong().table().addColumn("visits_other").asLong().table()
        .addColumn("body_map_101").asLong().table().addColumn("body_map_102").asLong().table()
        .addColumn("body_map_103").asLong().table().addColumn("body_map_104").asLong().table()
        .addColumn("body_map_105").asLong().table().addColumn("body_map_106").asLong().table()
        .addColumn("body_map_107").asLong().table().addColumn("body_map_108").asLong().table()
        .addColumn("body_map_109").asLong().table().addColumn("body_map_110").asLong().table()
        .addColumn("body_map_111").asLong().table().addColumn("body_map_112").asLong().table()
        .addColumn("body_map_113").asLong().table().addColumn("body_map_114").asLong().table()
        .addColumn("body_map_115").asLong().table().addColumn("body_map_116").asLong().table()
        .addColumn("body_map_117").asLong().table().addColumn("body_map_118").asLong().table()
        .addColumn("body_map_119").asLong().table().addColumn("body_map_120").asLong().table()
        .addColumn("body_map_121").asLong().table().addColumn("body_map_122").asLong().table()
        .addColumn("body_map_123").asLong().table().addColumn("body_map_124").asLong().table()
        .addColumn("body_map_125").asLong().table().addColumn("body_map_126").asLong().table()
        .addColumn("body_map_127").asLong().table().addColumn("body_map_128").asLong().table()
        .addColumn("body_map_129").asLong().table().addColumn("body_map_130").asLong().table()
        .addColumn("body_map_131").asLong().table().addColumn("body_map_132").asLong().table()
        .addColumn("body_map_133").asLong().table().addColumn("body_map_134").asLong().table()
        .addColumn("body_map_135").asLong().table().addColumn("body_map_136").asLong().table()
        .addColumn("body_map_201").asLong().table().addColumn("body_map_202").asLong().table()
        .addColumn("body_map_203").asLong().table().addColumn("body_map_204").asLong().table()
        .addColumn("body_map_205").asLong().table().addColumn("body_map_206").asLong().table()
        .addColumn("body_map_207").asLong().table().addColumn("body_map_208").asLong().table()
        .addColumn("body_map_209").asLong().table().addColumn("body_map_210").asLong().table()
        .addColumn("body_map_211").asLong().table().addColumn("body_map_212").asLong().table()
        .addColumn("body_map_213").asLong().table().addColumn("body_map_214").asLong().table()
        .addColumn("body_map_215").asLong().table().addColumn("body_map_216").asLong().table()
        .addColumn("body_map_217").asLong().table().addColumn("body_map_218").asLong().table()
        .addColumn("body_map_219").asLong().table().addColumn("body_map_220").asLong().table()
        .addColumn("body_map_221").asLong().table().addColumn("body_map_222").asLong().table()
        .addColumn("body_map_223").asLong().table().addColumn("body_map_224").asLong().table()
        .addColumn("body_map_225").asLong().table().addColumn("body_map_226").asLong().table()
        .addColumn("body_map_227").asLong().table().addColumn("body_map_228").asLong().table()
        .addColumn("body_map_229").asLong().table().addColumn("body_map_230").asLong().table()
        .addColumn("body_map_231").asLong().table().addColumn("body_map_232").asLong().table()
        .addColumn("body_map_233").asLong().table().addColumn("body_map_234").asLong().table()
        .addColumn("body_map_235").asLong().table().addColumn("body_map_236").asLong().table()
        .addColumn("body_map_237").asLong().table().addColumn("body_map_238").asLong().table()
        .addPrimaryKey(tableName + "_pk", "survey_site_id", "survey_token_id").table()
        .schema();
    System.out.println("Dropping TABLE " + tableName);
    database.dropTableQuietly(tableName);
    System.out.println("Creating TABLE " + tableName);
    schema.execute(database);
  }

  private static void testSurveys(SiteInfo siteInfo, Supplier<Database> dbp, String response) {
    do {
      Long tokenId;
      try {
        tokenId = new Long(response);
        // Fake a surveyAdvance for testing
        SurveyAdvance surveyAdvance = new SurveyAdvance();
        surveyAdvance.setAdvanceSequence(1L);
        surveyAdvance.setSurveySiteId(siteInfo.getSiteId());
        surveyAdvance.setSurveyTokenId(tokenId);
        PedPainAdvanceHandler advanceHandler = new PedPainAdvanceHandler(siteInfo);
        advanceHandler.surveyAdvanced(surveyAdvance, dbp);

      } catch (NumberFormatException nfe) {
        System.out.println("Invalid surveyTokenId of " + response + " expecting a Long");
        System.exit(1);
      }
      response = SquareUtils.getResponse("Enter another survey_token_id or 'q' to quit", true);
    } while (!"q".equals(response));
    dbp.get().commitNow();
  }

  /*
   * Run from the command line to create the square table and/or test writing individual survey data to the table.
   */
  public static void main(String[] args) {
    try {
      String resp = SquareUtils.getResponse(
          "Enter 'create' to CREATE TABLE " + tableName
              + ", a SURVEY_TOKEN_ID to test adding survey data to existing table, or 'q' to quit", true);

      if ("q".equals(resp)) {
        System.exit(0);
      }

      boolean createTable = ("create".equalsIgnoreCase(resp));

      DatabaseProvider.fromPropertyFile("../build.properties", "registry.")
          .withSqlParameterLogging()
          .withTransactionControl()
          .transact((Supplier<Database> dbp) -> {
            ServerContext serverContext = new ServerContext(dbp);
            SiteInfo siteInfo = serverContext.getSiteInfo(6L);
            new ServerUtils(".");
            if (createTable) {
              createTable(dbp.get());
            } else {
              testSurveys(siteInfo, dbp, resp);
            }
          });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
