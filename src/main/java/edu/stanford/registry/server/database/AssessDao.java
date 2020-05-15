/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.database;

import edu.stanford.registry.server.DataTableObjectConverter;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Notification;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.PatientRegistrationSearch;
import edu.stanford.registry.shared.PatientResSurveyRegLink;
import edu.stanford.registry.shared.PatientResult;
import edu.stanford.registry.shared.PatientResultType;
import edu.stanford.registry.shared.SurveyRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlArgs;
import com.github.susom.database.SqlInsert;
import com.github.susom.database.SqlSelect;
import com.github.susom.database.SqlUpdate;

/**
 * Created by rstr on 2/6/2017.
 */
public class AssessDao {
  private static final Logger logger = LoggerFactory.getLogger(AssessDao.class);

  final private Database database;
  final private SiteInfo siteInfo;
  final private Long siteId;

  public AssessDao(Database db, SiteInfo siteInfo) {
    database = db;
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
  }

  private static final String SELECT_SURVEY_REGS_BASE_AND =
        "SELECT survey_reg_id, survey_site_id, patient_id, survey_dt, token, survey_type, " +
             "  assessment_reg_id, survey_name, survey_order, meta_version, dt_created, dt_changed, " +
             "  ( select count(*) FROM patient_study ps WHERE ps.survey_reg_id = sr.survey_reg_id AND ps.dt_changed is null " +
            "    and coalesce(external_reference_id, ' ') != '" + Constants.REF_TESTQ + "') as number_pending,  " +
             "  ( select count(*) FROM patient_study ps WHERE ps.survey_reg_id = sr.survey_reg_id AND ps.dt_changed is not null " +
             "  ) as number_completed " +
        "FROM survey_registration sr " +
        "WHERE survey_site_id = ? AND ";  // parameters so far:  siteId

  private static final String SELECT_SURVEY_REGS_BY_TOKEN =
      SELECT_SURVEY_REGS_BASE_AND + "token = ?";

  private static final String SELECT_SURVEY_REGS_BY_ID =
      SELECT_SURVEY_REGS_BASE_AND + "survey_reg_id = ?";

  private static final String SELECT_SURVEY_REGS_IN_SET =
      SELECT_SURVEY_REGS_BASE_AND + "assessment_reg_id IN (?) ORDER BY survey_order asc";

  private static final String SELECT_SURVEY_REGS_BY_ASSESS_ID =
      SELECT_SURVEY_REGS_BASE_AND + "assessment_reg_id = ? ORDER BY survey_order asc";

  private static final String UPDATE_SURVEY_REG =
      "UPDATE SURVEY_REGISTRATION SET "
      + " survey_dt = ?, token = ?, survey_type = ?, assessment_reg_id = ?, survey_name = ?, survey_order = ?,"
      + " meta_version = ?, dt_changed = :now"
      + " WHERE survey_reg_id = ?";

  private static final String INSERT_SURVEY_REG = "INSERT INTO survey_registration "
      + "(survey_reg_id, survey_site_id, patient_id, survey_dt, token, survey_type, assessment_reg_id, "
      + "survey_name, survey_order, meta_version, dt_created, dt_changed)"
      + " VALUES (:pk, ?, ?, ?, ?, ?, ?, ?, ?, ?, :now, null)";

  private static final String SELECT_APPT_REGISTRATIONS_BASE =
      "SELECT AR.APPT_REG_ID, AR.SURVEY_SITE_ID, AR.PATIENT_ID, AR.VISIT_DT, AR.REGISTRATION_TYPE, AR.VISIT_TYPE,"
      + " AR.APPT_COMPLETE, AR.CLINIC, AR.PROVIDER_ID, AR.ENCOUNTER_EID,"
      + " ASMT.ASSESSMENT_REG_ID, ASMT.EMAIL_ADDR, ASMT.ASSESSMENT_DT, ASMT.ASSESSMENT_TYPE,"
      + " AR.META_VERSION, AR.DT_CREATED, AR.DT_CHANGED,"
      + " (SELECT MAX(EMAIL_DT) FROM NOTIFICATION N WHERE N.ASSESSMENT_REG_ID = AR.ASSESSMENT_REG_ID) AS EMAIL_DT"
      + " FROM APPT_REGISTRATION AR LEFT JOIN ASSESSMENT_REGISTRATION ASMT ON ASMT.ASSESSMENT_REG_ID = AR.ASSESSMENT_REG_ID"
      + " WHERE AR.SURVEY_SITE_ID = ? ";

  private static final String SELECT_APPT_REGISTRATIONS_BY_PATIENT =
      SELECT_APPT_REGISTRATIONS_BASE + " and AR.PATIENT_ID = ? ";

  public SurveyRegistration getRegistration(String token) {
    ArrayList<SurveyRegistration> registrationList =
        database.toSelect(SELECT_SURVEY_REGS_BY_TOKEN)
        .argLong(siteId)
        .argString(token)
        .query(getRegistrationsHandler());

    if (registrationList == null || registrationList.size() < 1)
      return null;

    return registrationList.get(0);
  }


  public SurveyRegistration getSurveyRegistrationByRegId(Long surveyRegId) {
    ArrayList<SurveyRegistration> results = database.toSelect(SELECT_SURVEY_REGS_BY_ID)
        .argLong(siteId)
        .argLong(surveyRegId)
        .query(getRegistrationsHandler());
    return (results.size() > 0) ? results.get(0) : null;
  }


  static final String GET_SURVEY_REG_ID_BY_TOKEN_ID =
      "SELECT survey_reg_id "
          + "FROM survey_registration sr, survey_token st "
          + "WHERE sr.survey_site_id = st.survey_site_id AND st.survey_site_id = ?"
          + " AND sr.token = st.survey_token AND st.survey_token_id = ?";

  public Long getSurveyRegIdByTokenId(Long surveyTokenId) {
    return database.get().toSelect(GET_SURVEY_REG_ID_BY_TOKEN_ID)
        .argLong(siteId)
        .argLong(surveyTokenId)
        .queryLongOrNull();
  }


  /**
   * Get all completed SurveyRegistrations.
   */
  public ArrayList<ApptRegistration> getCompletedRegistrations() {
    String sql = SELECT_APPT_REGISTRATIONS_BASE +
        " and not exists(" + getIncompleteSurveyRegSqlAr() + ")";
    ArrayList<ApptRegistration> results = database.toSelect(sql)
        .argLong(siteId) // for SELECT statement
        .query(new ApptRegistrationHandler(database, siteId));
    return results;
  }

  /**
   * Get all completed Registrations within a date range
   * @param fromDate range start date
   * @param toDate   range end date
   * @return
   */
  public ArrayList<ApptRegistration> getCompletedRegistrations(Date fromDate, Date toDate) {
    StringBuilder sb = new StringBuilder();
    sb.append(SELECT_APPT_REGISTRATIONS_BASE);
    sb.append(" and not exists(" ).append(getIncompleteSurveyRegSqlAr()).append( ")");
    sb.append(" and visit_dt between ? and ? ");
    ArrayList<ApptRegistration> results =
        database.toSelect(sb.toString())
        .argLong(siteId)
        .argDate(DateUtils.getDateStart(siteInfo, fromDate))
        .argDate(DateUtils.getDateEnd(siteInfo, toDate))
        .query(new ApptRegistrationHandler(database, siteId));
    return results;
  }

  public SurveyRegistration insertSurveyRegistration(SurveyRegistration surveyReg) {
    surveyReg.setMetaVersion(0);

    SqlInsert sqlInsert = database.toInsert(INSERT_SURVEY_REG)
    .argLong(surveyReg.getSurveySiteId())
    .argString(surveyReg.getPatientId())
    .argDate(surveyReg.getSurveyDt())
    .argString(surveyReg.getToken())
    .argString(surveyReg.getSurveyType())
    .argLong(surveyReg.getAssessmentRegId())
    .argString(surveyReg.getSurveyName())
    .argLong(surveyReg.getSurveyOrder())
    .argInteger(surveyReg.getMetaVersion())
    .argPkSeq(":pk", "survey_seq")
    .argDateNowPerDb(":now");
    Long regId = sqlInsert.insertReturningPkSeq("survey_reg_id");

    surveyReg.setSurveyRegId(regId);
    return surveyReg;
  }



  public void updateSurveyRegistration(SurveyRegistration surveyReg) {
    SqlUpdate sqlUpdate = database.toUpdate(UPDATE_SURVEY_REG)
        .argDate(surveyReg.getSurveyDt())
        .argString(surveyReg.getToken())
        .argString(surveyReg.getSurveyType())
        .argLong(surveyReg.getAssessmentRegId())
        .argString(surveyReg.getSurveyName())
        .argLong(surveyReg.getSurveyOrder())
        .argInteger(surveyReg.getMetaVersion())
        .argLong(surveyReg.getSurveyRegId())
        .argDateNowPerDb(":now");
    sqlUpdate.update(1);
  }


  public SurveyRegistration updateRegistrationType(SurveyRegistration registration,
      String surveyType) {
    String sql = "UPDATE SURVEY_REGISTRATION SET SURVEY_TYPE  = ?, DT_CHANGED = CURRENT_TIMESTAMP "
        + " WHERE SURVEY_REG_ID = ? ";

    int rowsUpdated = database.toUpdate(sql)
        .argString(surveyType)
        .argLong(registration.getSurveyRegId())
        .update();

    if (rowsUpdated < 1) {
      logger.error("Could not update registration for patient " + registration.getPatientId() + " on "
          + registration.getSurveyDt() + " type " + registration.getSurveyType() + " to type " + surveyType);
    }
    registration.setSurveyType(surveyType);
    return registration;
  }


  public void deleteSurveyRegistration(SurveyRegistration registration) {
    database.toDelete("DELETE FROM patient_res_to_survey_reg WHERE survey_reg_id = ?")
        .argLong(registration.getSurveyRegId())
        .update();
    int rowCount = database.toDelete("DELETE FROM survey_registration WHERE survey_reg_id = ?")
        .argLong(registration.getSurveyRegId())
        .update();
    if (rowCount != 1) {
      logger.error("Survey registration for " + registration.getPatientId() + " on " + registration.getSurveyDt()
      + " not deleted, delete returned: " + rowCount);
    }
  }


  // ===================== Assessments ==================

  private final String selectAssessRegsSQL =
    "SELECT assessment_reg_id, survey_site_id, patient_id, email_addr, assessment_dt, assessment_type,"
        +        " META_VERSION, DT_CREATED, DT_CHANGED,"
        +        " (SELECT MAX(email_dt) FROM notification n WHERE n.assessment_reg_id = ar.assessment_reg_id) AS email_dt"
        + " FROM assessment_registration ar "
        + " WHERE survey_site_id = ? ";


  public AssessmentRegistration getAssessmentById(AssessmentId assessmentId) {
    String sql = selectAssessRegsSQL + " AND assessment_reg_id = ?";

    ArrayList<AssessmentRegistration> registrationList =
        database.toSelect(sql)
        .argLong(siteId)
        .argLong(assessmentId.getId())
        .query(new AssessmentRegsHandler(database, siteId));

    if (registrationList == null || registrationList.size() < 1)
      return null;

    return registrationList.get(0);
  }


  /**
   * Returns all assessments for a site (survey site???)
   * <br>The where clause may refer to the ASESSMENT_REGISTRATION table as AR
   * @param whereClause Must not be null, and may not contain arguments.
   * @return
   */
  public List<AssessmentRegistration> getAssessments(String whereClause) {
    String sql = selectAssessRegsSQL;
    if ((whereClause != null) && !whereClause.isEmpty()) {
      sql = sql + "AND " + whereClause;
    }
    List<AssessmentRegistration> results =
        database.toSelect(sql)
        .argLong(siteId)
        .query(new AssessmentRegsHandler(database, siteId));
    return results;
  }

  private final String completedStudyCount =
      "(select count(*) from survey_registration sr join patient_study ps on ps.survey_reg_id = sr.survey_reg_id " +
      " where sr.assessment_reg_id = ar.assessment_reg_id and ps.dt_changed is not null)";

  public List<AssessmentRegistration> getAssessmentsTypeLikeXAndStudyCountGtY(String likeWhat, int num) {
    String whereClause = "AND assessment_type like ? AND " + completedStudyCount + " > ?";
    String sql = selectAssessRegsSQL + whereClause;
    List<AssessmentRegistration> results =
        database.toSelect(sql)
        .argLong(siteId)
        .argString(likeWhat)
        .argInteger(num)
        .query(new AssessmentRegsHandler(database, siteId));
    return results;
  }

  final String[] qs = { // 0..7 question marks for set contents
      "", "?", "?,?", "?,?,?", "?,?,?,?", "?,?,?,?,?", "?,?,?,?,?,?", "?,?,?,?,?,?,?"
  };

  /**
   * @param types Up to 7 in the array.
   * @param num completedStudyCount must be larger than this
   */
  public List<AssessmentRegistration> getAssessmentsTypeNotInSetAndStudyCountGtZ(String[] types, int num) {
    return getAssessmentsTypeLikeXAndStudyCountGtY(types, num, null, null);
  }

  /**
   * @param types Up to 7 in the array.
   * @param num completedStudyCount must be larger than this
   * @param fromDt include surveys scheduled on or after this date
   * @param toDt and surveys scheduled on or before this date
   * @return
   */
  public List<AssessmentRegistration> getAssessmentsTypeLikeXAndStudyCountGtY(String[] types, int num, Date fromDt, Date toDt) {
    String sql = selectAssessRegsSQL + " AND assessment_type not in (" + qs[types.length] + ") and " + completedStudyCount + " > ?";
    if (fromDt != null && toDt != null) {
      sql = sql + ( " AND ar.assessment_dt between ? and ? ");
    }

    SqlSelect sqlSel =  database.toSelect(sql)
        .argLong(siteId);
    for (String typ: types) {
      sqlSel = sqlSel.argString(typ);
    }
    sqlSel = sqlSel.argInteger(num);

    if (fromDt != null && toDt != null) {
      sqlSel = sqlSel.argDate(fromDt);
      sqlSel = sqlSel.argDate(toDt);
    }

    List<AssessmentRegistration> results =
        sqlSel.query(new AssessmentRegsHandler(database, siteId));
    return results;
  }

  public static String getIncompleteSurveyRegSqlAr() {
    return getIncompleteSurveyRegSql(true);
  }

  public static String getIncompleteSurveyRegSqlAr2() {
      return getIncompleteSurveyRegSql(false);
  }

  /**
   * Return a SQL statement to select the survey reg id for incomplete
   * surveys from an assessment registration specified by 'var'.
   */
  private static String getIncompleteSurveyRegSql(boolean arOrAr2) {
    String sql =
        "SELECT sr.survey_reg_id " +
        "FROM survey_registration sr LEFT JOIN survey_token st" +
        "  ON st.survey_site_id = sr.survey_site_id AND st.survey_token = sr.token " +
        "WHERE sr.assessment_reg_id = " + (arOrAr2 ? "ar" : "ar2") + ".assessment_reg_id" +
        "  AND (st.is_complete != 'Y' OR st.is_complete IS NULL) ";
    return sql;
  }

  public ApptRegistration getLastCompletedRegistration(String patient_id, Date excludeSurveyDate) {
    String sql = SELECT_APPT_REGISTRATIONS_BY_PATIENT +
        " and ar.visit_dt <> ? " +
        " and not exists (" + getIncompleteSurveyRegSqlAr() + ") " +
        "order by ar.visit_dt desc";

    ArrayList<ApptRegistration> results = database.toSelect(sql)
        .argLong(siteId) // for SELECT statement
        .argString(patient_id)
        .argDate(excludeSurveyDate)
        .query(new ApptRegistrationHandler(database, siteId));
    return (results.size() > 0) ? results.get(0) : null;
  }


  /**
   * Returns the patient's last completed registration before the survey date, or null if there was none.
   */
  public ApptRegistration getLastCompletedRegistrationBeforeThis(String patient_id, Date beforeSurveyDate) {
    String sql = SELECT_APPT_REGISTRATIONS_BY_PATIENT +
        " and ar.visit_dt < ? " +
        " and not exists (" + getIncompleteSurveyRegSqlAr() + ") " +
        "order by ar.visit_dt desc";
    ArrayList<ApptRegistration> results = database.toSelect(sql)
        .argLong(siteId) // for SELECT statement
        .argString(patient_id)
        .argDate(beforeSurveyDate)
        .query(new ApptRegistrationHandler(database, siteId));
    return (results.size() > 0) ? results.get(0) : null;
  }


  public ArrayList<ApptRegistration> getCompletedRegistrationsByPatient(String patientId) {
    String sql = SELECT_APPT_REGISTRATIONS_BY_PATIENT +
        " and not exists(" + getIncompleteSurveyRegSqlAr() + ")";
    ArrayList<ApptRegistration> results = database.toSelect(sql)
        .argLong(siteId) // for SELECT statement
        .argString(patientId)
        .query(new ApptRegistrationHandler(database, siteId));
    return results;
  }


  /**
   * Get completed survey count for a patient within the date range excluding the
   * specified registration.
   */
  public int getCompletedSurveyCount(String patientId, Date fromDate, Date toDate, ApptId excludeRegId) {
    String sql =
        "select count(*) from appt_registration ar " +
            "where ar.survey_site_id = :site and ar.patient_id = ? and ar.appt_reg_id != ? and ar.visit_dt between ? and ?" +
            " and not exists (" + getIncompleteSurveyRegSqlAr() + ") ";
    return database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(patientId)
        .argLong(excludeRegId.getId())
        .argDate(DateUtils.getTimestampStart(siteInfo, fromDate))
        .argDate(DateUtils.getTimestampEnd(siteInfo, toDate))
        .query(new RowsHandler<Integer>() {
          @Override
          public Integer process(Rows rs) throws Exception {
            if (rs.next())
              return rs.getIntegerOrZero(1);

            return 0;
          }
        });
  }


  private static final String GET_APPT_REG_ID_BY_TOKEN_ID =
      "SELECT ar.appt_reg_id "
          + "FROM survey_token st, survey_registration sr, appt_registration ar "
          + "WHERE sr.survey_site_id = st.survey_site_id"
          + " AND sr.token = st.survey_token"
          + " AND st.survey_site_id = ? AND st.survey_token_id = ?"
          + " AND ar.assessment_reg_id = sr.assessment_reg_id";

  public Long getApptRegIdByTokenId(Long surveyTokenId) {
    return database.get().toSelect(GET_APPT_REG_ID_BY_TOKEN_ID)
        .argLong(siteId)
        .argLong(surveyTokenId)
        .queryLongOrNull();
  }


  public ApptRegistration getApptRegistrationByRegId(ApptId apptId) {
    String sql = SELECT_APPT_REGISTRATIONS_BASE + " AND AR.APPT_REG_ID = ? ";
    ArrayList<ApptRegistration> results = database.toSelect(sql)
        .argLong(siteId) // for SELECT statement
        .argLong(apptId.getId())
        .query(new ApptRegistrationHandler(database, siteId));
    return (results.size() > 0) ? results.get(0) : null;
  }


  public ApptRegistration getApptRegistrationByAssessmentId(AssessmentId assessmentId) {
    String sql = SELECT_APPT_REGISTRATIONS_BASE + " AND AR.ASSESSMENT_REG_ID = ? ";
    ArrayList<ApptRegistration> results = database.toSelect(sql)
        .argLong(siteId) // for SELECT statement
        .argLong(assessmentId.getId()) // for WHERE clause
        .query(new ApptRegistrationHandler(database, siteId));
    return (results.size() > 0) ? results.get(0) : null;
  }


  public ApptRegistration getApptRegistrationBySurveyRegId(Long surveyRegId) {
    String sql = SELECT_APPT_REGISTRATIONS_BASE +
        " AND AR.ASSESSMENT_REG_ID IN (SELECT ASSESSMENT_REG_ID FROM SURVEY_REGISTRATION WHERE SURVEY_REG_ID = ?) ";
    ArrayList<ApptRegistration> results = database.toSelect(sql)
        .argLong(siteId) // for SELECT statement
        .argLong(surveyRegId) // for WHERE clause
        .query(new ApptRegistrationHandler(database, siteId));
    return (results.size() > 0) ? results.get(0) : null;
  }


  /**
   * Get an array of PatientRegistrations within a date range, includes patients attributes.
   *
   * @param fromDate The start date/time for the range.
   * @param toDate   The end dat/time for the range.
   * @return ArrayList of PatientRegistration objects for all Registrations
   * within the given date range.
   */
  public ArrayList<PatientRegistration> getPatientRegistrations(Date fromDate, Date toDate, PatientRegistrationSearch searchOptions) {
    logger.debug("AssessDao getPatientRegistrations(" + fromDate.toString() + "," + toDate.toString() + ","
        + searchOptions.toString() + ")");
    String sql = PatientRegistrationSql.getSelectStatmentBetweenDates(searchOptions, null);
    Date fromTime = DateUtils.getTimestampStart(siteInfo, fromDate);
    Date toTime = DateUtils.getTimestampEnd(siteInfo, toDate);
    ArrayList<PatientRegistration> registrationList = database.toSelect(sql)
        .argLong(siteId).argLong(siteId)
        .argDate(fromTime).argDate(toTime).query(getPatientRegistrationHandler(database, siteInfo));
    addPatientAttributes(registrationList);
    return registrationList;
  }

  public ArrayList<PatientRegistration> getPatientRegistrations(Date fromDate, Date toDate, String registrationType, PatientRegistrationSearch searchOptions) {
    return getPatientRegistrations(fromDate, toDate, Collections.singletonList(registrationType), searchOptions);
  }


  public ArrayList<PatientRegistration> getPatientRegistrations(Date fromDate, Date toDate, List<String> registrationTypes, PatientRegistrationSearch searchOptions) {
    logger.debug("AssessDao getPatientRegistrations(" + fromDate.toString() + "," + toDate.toString() + ","
        + registrationTypes + "," + searchOptions.toString() + ")");
    String sql = PatientRegistrationSql.getSelectStatmentBetweenDates(searchOptions, registrationTypes);
    Date fromTime = DateUtils.getTimestampStart(siteInfo, fromDate);
    Date toTime = DateUtils.getTimestampEnd(siteInfo, toDate);
    ArrayList<PatientRegistration> registrationList = database.toSelect(sql)
        .argLong(siteId).argLong(siteId)
        .argDate(fromTime).argDate(toTime).query(getPatientRegistrationHandler(database, siteInfo));
    addPatientAttributes(registrationList);
    logger.debug("AssessDao getPatientRegistrations -> "+registrationList.size()+" registrations");
    return registrationList;
  }


  /**
   * For each registration in the list, fetches and sets its patient's attributes
   * and if the registration email address is empty, sets it to the patient's.
   *
   * This fetches the whole set of patients.
   * (For efficient SQL caching, we could do this in sets of, say, 16, 32 .. 256
   * and when there are less than 2^N, repeat the last value..).
   */
  private void addPatientAttributes(ArrayList<PatientRegistration> registrationList) {
    if (registrationList == null || registrationList.size() == 0) {
      return;
    }
    PatientDao pdao = new PatientDao(database, siteId);
    if (registrationList.size() == 1) {
      pdao.loadPatientAttributes(registrationList.get(0).getPatient());
      return;
    }

    ArrayList<Patient> patients = new ArrayList<Patient>(registrationList.size());
    for (PatientRegistration reg: registrationList) {
      patients.add(reg.getPatient());
    }
    pdao.loadPatientsAttributes(patients);

    for (PatientRegistration aRegistrationList : registrationList) {
      Patient pat = aRegistrationList.getPatient();
      /* check survey has email address */
      if (ServerUtils.isEmpty(aRegistrationList.getEmailAddr()) && pat.getEmailAddress() != null) {
        aRegistrationList.setEmailAddr(pat.getEmailAddress());
      }
    }
  }


  /**
   * Get all of the Registrations for a given patient.
   *
   * @param patientId The mrn of the patient.
   * @return ArrayList of PatientRegistration objects for the specified patient.
   */
  public ArrayList<PatientRegistration> getPatientRegistrations(String patientId, PatientRegistrationSearch searchOptions) {
    String sql = PatientRegistrationSql.getSelectStatementByPatientId(searchOptions.getExcludeTypes());
    return database.toSelect(sql)
        .argLong(siteId).argLong(siteId)
        .argString(patientId).query(getPatientRegistrationHandler(database, siteInfo));
  }

  public ArrayList<PatientRegistration> getPatientRegistrationsByType(String patientId, String registrationType) {
    String sql = PatientRegistrationSql.getSelectStatementByPatientIdByType();
    return database.toSelect(sql)
        .argLong(siteId).argLong(siteId)
        .argString(patientId).argString(registrationType).query(getPatientRegistrationHandler(database, siteInfo));
  }

  public PatientRegistration getPatientRegistrationByRegId(ApptId apptId) {
    String sql = PatientRegistrationSql.getSelectStatementByRegId();
    ArrayList<PatientRegistration> registrationList = database.toSelect(sql)
        .argLong(siteId).argLong(siteId)
        .argLong(apptId.getId())
        .query(getPatientRegistrationHandler(database, siteInfo));
    if ((registrationList != null) && (registrationList.size() > 1)) {
      throw new DataException("More than one appointment registration was found for survey_reg_id " + apptId);
    }
    addPatientAttributes(registrationList);
    if ((registrationList != null) && (registrationList.size() == 1)) {
      return registrationList.get(0);
    } else {
      return null;
    }
  }

  public PatientRegistration getPatientRegistrationByAssessmentId(AssessmentId assessmentId) {
    String sql = PatientRegistrationSql.getSelectStatementByAssessmentId();
    ArrayList<PatientRegistration> registrationList = database.toSelect(sql)
        .argLong(siteId).argLong(siteId)
        .argLong(assessmentId.getId())
        .query(getPatientRegistrationHandler(database, siteInfo));
    if ((registrationList != null) && (registrationList.size() > 1)) {
      throw new DataException("More than one appointment registration was found for assessment_reg_id " + assessmentId);
    }
    addPatientAttributes(registrationList);
    if ((registrationList != null) && (registrationList.size() == 1)) {
      return registrationList.get(0);
    } else {
      return null;
    }
  }

  public ArrayList<PatientRegistration> getPatientRegistrations(String patientId, Date fromDate) {
    String sql = PatientRegistrationSql.getSelectStatementByPatientIdAndDateFrom();
    return database.toSelect(sql)
        .argLong(siteId).argLong(siteId)
        .argString(patientId).argDate(fromDate).query(getPatientRegistrationHandler(database, siteInfo));
  }

  /**
   * The patients last scheduled appointment (active not cancelled) before now.
   */
  public Date getPatientsLastAppointmentDate(String patientId) {
    String sql = "SELECT max(visit_dt) visit_dt FROM APPT_REGISTRATION "
        + "where survey_site_id = :site and patient_id = ? and registration_type = ? "
        + "and visit_dt < current_timestamp";
    Date dt = database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(patientId)
        .argString(Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT)
        .query(new RowsHandler<Date>() {
          @Override
          public Date process(Rows rs) throws Exception {
            if (rs.next()) return rs.getDateOrNull("visit_dt");
            return null;
          }
        });
    return dt;
  }


  /**
   * This gets the date of the patients last completed activity from their
   * completed surveys. Can be run run to select only active appointments, type
   * 'a' (not cancelled).
   *
   * @param patientId
   * @param appointmentsOnly
   * @return
   */
  public Date getPatientsLastScheduleCompletedDate(String patientId,
      boolean appointmentsOnly) {
    String sql =
        "select max(a2.activity_dt) as completed " +
            "from appt_registration ar " +
            "  join survey_registration sr2 on sr2.assessment_reg_id = ar.assessment_reg_id " +
            "  join activity a2 on a2.survey_site_id = sr2.survey_site_id and a2.token = sr2.token and " +
            "    a2.activity_type = '" + Constants.ACTIVITY_COMPLETED + "' " +
            "where " +
            "  ar.survey_site_id = :site and ar.patient_id = ? and " +
            "  not exists (" + getIncompleteSurveyRegSqlAr() + ") " +
            ((appointmentsOnly) ? " and ar.registration_type = ?" : "") ;
    SqlArgs args = new SqlArgs();
    args.argLong(":site", siteId);
    args.argString(patientId);
    if (appointmentsOnly) args.argString(Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT);
    Date dt = database.toSelect(sql)
        .apply(args)
        .query(new RowsHandler<Date>() {
          @Override
          public Date process(Rows rs) throws Exception {
            if (rs.next()) {
              return rs.getDateOrNull("completed");
            }
            return null;
          }
        });
    return dt;
  }


  /**
   * Get the date of the next scheduled survey
   *
   * @param patientId
   * @param appointmentsOnly Look at active appointments only (not cancelled).
   * @param uncompletedOnly  Look only at surveys that have not been completed.
   * @return
   */
  public Date getPatientsNextScheduledDate(String patientId, boolean appointmentsOnly,
      boolean uncompletedOnly) {
    StringBuilder sql = new StringBuilder(
        "SELECT min(visit_dt) as visit_dt FROM APPT_REGISTRATION ar "
            + "WHERE survey_site_id = :site and patient_id = ? "
            + "and visit_dt > current_timestamp ");
    SqlArgs args = new SqlArgs();
    args.argLong(":site", siteId);
    args.argString(patientId);
    if (appointmentsOnly) {
      sql.append(" and registration_type = ?");
      args.argString(Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT);
    }
    if (uncompletedOnly) {
      sql.append(" and exists (" + getIncompleteSurveyRegSqlAr() + ") ");
    }
    Date dt = database.toSelect(sql.toString()).apply(args).query(new RowsHandler<Date>() {
      @Override
      public Date process(Rows rs) throws Exception {
        if (rs.next()) {
          return rs.getDateOrNull("visit_dt");
        }
        return null;
      }
    });
    return dt;
  }


  public ArrayList<ApptRegistration> getApptRegistrationByPatientAndDate(String patientId, Date date) {
    String sql = SELECT_APPT_REGISTRATIONS_BASE + " AND AR.PATIENT_ID = ? AND VISIT_DT = ? ";
    return database.toSelect(sql)
        .argLong(siteId) // for SELECT statement
        .argString(patientId)
        .argDate(date)
        .query(new ApptRegistrationHandler(database, siteId));
  }


  public ApptRegistration getApptRegistrationByEncounterId(String encounterEid) {
    String sql = SELECT_APPT_REGISTRATIONS_BASE + " AND AR.ENCOUNTER_EID = ? ";
    ArrayList<ApptRegistration> results = database.toSelect(sql)
        .argLong(siteId) // for SELECT statement
        .argString(encounterEid)
        .query(new ApptRegistrationHandler(database, siteId));
    return (results.size() > 0) ? results.get(0) : null;
  }


  private static class ApptRegistrationHandler implements RowsHandler<ArrayList<ApptRegistration>> {
    final Database database;
    final long siteId;

    ApptRegistrationHandler(Database db, Long siteId) {
      database = db;
      this.siteId = siteId;
    }

    @Override
    public ArrayList<ApptRegistration> process(Rows rs) throws Exception {
      ArrayList<ApptRegistration> registrations = DataTableObjectConverter.convertToObjects(rs, ApptRegistration.class);

      for (ApptRegistration registration : registrations) {
        AssessmentRegistration assessment = registration.getAssessment();
        ArrayList<SurveyRegistration> surveyRegs = database.toSelect(SELECT_SURVEY_REGS_BY_ASSESS_ID)
            .argLong(siteId)
            .argLong(assessment.getAssessmentRegId())
            .query(new SurveyRegHandler());
        registration.setSurveyRegList(surveyRegs);
      }

      return registrations;
    }
  }


  public AssessmentRegistration insertAssessmentRegistration(AssessmentRegistration assessmentReg) {
    assessmentReg.setMetaVersion(0);

    final String insertSql = "INSERT INTO assessment_registration"
        + " (assessment_reg_id, survey_site_id, patient_id, email_addr, assessment_dt, assessment_type,"
        + " meta_version, dt_created, dt_changed)"
        + " VALUES (:pk, ?, ?, ?, ?, ?, ?, :now, null)";

    SqlInsert sqlInsert = database.toInsert(insertSql)
    .argLong(assessmentReg.getSurveySiteId())
    .argString(assessmentReg.getPatientId())
    .argString(assessmentReg.getEmailAddr())
    .argDate(assessmentReg.getAssessmentDt())
    .argString(assessmentReg.getAssessmentType())
    .argInteger(assessmentReg.getMetaVersion())
    .argPkSeq(":pk", "survey_seq")
    .argDateNowPerDb(":now");

     Long regId = sqlInsert.insertReturningPkSeq("assessment_reg_id");

     assessmentReg.setAssessmentRegId(regId);
     return assessmentReg;
  }


  public ApptRegistration insertApptRegistration(ApptRegistration apptReg) {
    Long regId = database.toInsert( "INSERT INTO APPT_REGISTRATION"
        + " (APPT_REG_ID, SURVEY_SITE_ID, PATIENT_ID, VISIT_DT, REGISTRATION_TYPE, VISIT_TYPE, APPT_COMPLETE,"
        + " CLINIC, PROVIDER_ID, ENCOUNTER_EID, ASSESSMENT_REG_ID, META_VERSION, DT_CREATED, DT_CHANGED)"
        + " VALUES (:pk, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, :now, null)")
        .argLong(apptReg.getSurveySiteId())
        .argString(apptReg.getPatientId())
        .argDate(apptReg.getVisitDt())
        .argString(apptReg.getRegistrationType())
        .argString(apptReg.getVisitType())
        .argString(apptReg.getApptComplete())
        .argString(apptReg.getClinic())
        .argLong(apptReg.getProviderId())
        .argString(apptReg.getEncounterEid())
        .argLong(apptReg.getAssessmentRegId())
        .argInteger(0)
        .argPkSeq(":pk", "survey_seq")
        .argDateNowPerDb(":now").insertReturningPkSeq("appt_reg_id");
    apptReg.setApptRegId(regId);
    return apptReg;
  }


  public void updateApptRegistration(ApptRegistration apptReg) {
    database.toUpdate( "UPDATE APPT_REGISTRATION SET VISIT_DT = ?, REGISTRATION_TYPE = ?, VISIT_TYPE = ?, APPT_COMPLETE = ?,"
        + " CLINIC = ?, PROVIDER_ID = ?, ENCOUNTER_EID = ?, ASSESSMENT_REG_ID = ?, META_VERSION = ?, DT_CHANGED = :now"
        + " WHERE APPT_REG_ID = ? ").argDate(apptReg.getVisitDt())
        .argString(apptReg.getRegistrationType())
        .argString(apptReg.getVisitType())
        .argString(apptReg.getApptComplete())
        .argString(apptReg.getClinic())
        .argLong(apptReg.getProviderId())
        .argString(apptReg.getEncounterEid())
        .argLong(apptReg.getAssessmentRegId())
        .argInteger(apptReg.getMetaVersion())
        .argLong(apptReg.getApptRegId())
        .argDateNowPerDb(":now")
    .update(1);
  }

  public void updateAssessmentRegistration(AssessmentRegistration assessmentReg) {
    String stmt = "UPDATE assessment_REGISTRATION SET"
        + " SURVEY_SITE_ID = ?, PATIENT_ID = ?, EMAIL_ADDR = ?, assessment_DT = ?, assessment_TYPE = ?,"
        + " META_VERSION = ?, DT_CHANGED = :now"
        + " WHERE assessment_REG_ID = ? ";
    SqlUpdate updateStmt = database.toUpdate(stmt)
        .argLong(assessmentReg.getSurveySiteId())
        .argString(assessmentReg.getPatientId())
        .argString(assessmentReg.getEmailAddr())
        .argDate(assessmentReg.getAssessmentDt())
        .argString(assessmentReg.getAssessmentType())
        .argInteger(assessmentReg.getMetaVersion())
        .argLong(assessmentReg.getAssessmentRegId())
        .argDateNowPerDb(":now");
    updateStmt.update(1);
  }


  public AssessmentRegistration updateRegistrationType(AssessmentRegistration registration,
      String surveyType) {
    String sql = "UPDATE ASSESSMENT_REGISTRATION SET ASSESSMENT_TYPE  = ?, DT_CHANGED = CURRENT_TIMESTAMP "
        + " WHERE ASSESSMENT_REG_ID = ? ";

    int rowsUpdated = database.toUpdate(sql)
        .argString(surveyType)
        .argLong(registration.getAssessmentRegId())
        .update();

    if (rowsUpdated < 1) {
      logger.error("Could not update registration for patient " + registration.getPatientId() + " on "
          + registration.getAssessmentDt() + " type " + registration.getAssessmentType() + " to type " + surveyType);
    }
    registration.setAssessmentType(surveyType);
    return registration;
  }


  public void deleteAssessmentRegistration(AssessmentRegistration registration) {
    int rowCount = database.toDelete("DELETE FROM assessment_registration WHERE assessment_reg_id = ?")
      .argLong(registration.getAssessmentRegId())
      .update();
    if (rowCount != 1) {
      logger.error("Assessment registration for " + registration.getPatientId() + " on " + registration.getAssessmentDt()
      + " not deleted, delete returned: " + rowCount);
    }
  }


  public void deleteApptRegistration(ApptRegistration registration) {

    int rowCount = database.toUpdate("DELETE FROM APPT_REGISTRATION WHERE APPT_REG_ID = ? ")
        .argLong(registration.getApptRegId())
        .update();
    if (rowCount != 1) {
      logger.error("Appt registration for " + registration.getPatientId() + " on " + registration.getSurveyDt()
      + " not deleted, delete returned: " + rowCount);
    }
  }


  private static final String SELECT_NOTIFICATIONS_BASE =
      "SELECT n.NOTIFICATION_ID, n.SURVEY_SITE_ID, n.PATIENT_ID, n.ASSESSMENT_REG_ID, n.EMAIL_TYPE, n.SURVEY_DT, n.EMAIL_DT, "
          + " n.META_VERSION, n.DT_CREATED, n.DT_CHANGED"
          + " FROM NOTIFICATION n ";

  private static final String NOTIFICATIONS_SELECT_BY_ASSESSMENT_REG = "  WHERE n.SURVEY_SITE_ID = ? AND ASSESSMENT_REG_ID = ?";


  public ArrayList<Notification> getUnsentNotifications(AssessmentId assessmentId) {

    String sql = SELECT_NOTIFICATIONS_BASE + NOTIFICATIONS_SELECT_BY_ASSESSMENT_REG + " AND email_dt is null ";
    return database.toSelect(sql)
        .argLong(siteId)  // for SELECT statement
        .argLong(assessmentId.getId())  // for WHERE clause
        .query(getNotificationHandler());
  }


  public ArrayList<Notification> getSentNotifications(AssessmentId assessmentId) {
    String sql = SELECT_NOTIFICATIONS_BASE + NOTIFICATIONS_SELECT_BY_ASSESSMENT_REG + " AND email_dt is not null ";
    return database.toSelect(sql)
        .argLong(siteId)
        .argLong(assessmentId.getId())
        .query(getNotificationHandler());
  }

  public ArrayList<Notification> getPendingNotifications() {
    String JOIN_PATIENT_ATTRIBUTE = " JOIN PATIENT_ATTRIBUTE pa ON n.PATIENT_ID=pa.PATIENT_ID and n.SURVEY_SITE_ID=pa.SURVEY_SITE_ID ";
    String WHERE_PENDING = " WHERE n.SURVEY_SITE_ID = ? AND EMAIL_DT is null AND pa.DATA_NAME='participatesInSurveys' "
        + "AND pa.DATA_VALUE='y' AND n.SURVEY_DT >= :now and exists (select * from survey_registration sr, patient_study ps "
        + " where sr.assessment_reg_id = n.assessment_reg_id and ps.survey_reg_id = sr.survey_reg_id and ps.xml_clob is null)";
    String sql = SELECT_NOTIFICATIONS_BASE + JOIN_PATIENT_ATTRIBUTE + WHERE_PENDING;
    return database.toSelect(sql)
        .argLong(siteId)
        .argDateNowPerDb(":now")
        .query(
            getNotificationHandler());
  }

  public ArrayList<Notification> getNotificationsByPatient(String patientId) {
    String sql = SELECT_NOTIFICATIONS_BASE + "  WHERE n.SURVEY_SITE_ID = ? AND PATIENT_ID = ? ";
    return database.toSelect(sql)
        .argLong(siteId)
        .argString(patientId)
        .query(getNotificationHandler());
  }


  /**
   * Get the number of notifications for a patient for registrations within the
   * date range excluding the specified registration and excluding any visit types
   * defined in process.xml with excludeFromSurveyCnt="true".
   */
  public int getNotificationCount(String patientId, Date fromDate, Date toDate, ApptId excludeApptId) {
    ArrayList<String> excludeTypes = XMLFileUtils.getInstance(siteInfo).getExcludeFromSurveyCntVisits();
    String sql =
        "select count(*) as cnt from notification n, appt_registration r " +
            "where n.assessment_reg_id = r.assessment_reg_id and r.survey_site_id = :site " +
            "and r.patient_id = ? and r.appt_reg_id != ? and r.registration_type in (?, ?) " +
            "and r.visit_dt between ? and ?";

    if (excludeTypes.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append(" and r.visit_type not in (");
      StringJoiner sj = new StringJoiner(",");
      for (String exc : excludeTypes) {
        sj.add("?");
      }
      sb.append(sj.toString());
      sb.append(")");
      sql+= sb.toString();
    }
    SqlSelect select =  database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(patientId)
        .argLong(excludeApptId.getId())
        .argString(Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT)
        .argString(Constants.REGISTRATION_TYPE_STANDALONE_SURVEY)
        .argDate(DateUtils.getTimestampStart(siteInfo, fromDate))
        .argDate(DateUtils.getTimestampEnd(siteInfo, toDate));
    for (String excl : excludeTypes) {
      select = select.argString(excl);
    }
    return select
        .query(new RowsHandler<Integer>() {
          @Override
          public Integer process(Rows rs) throws Exception {
            int count = 0;
            if (rs.next()) {
              count = rs.getIntegerOrZero(1);
            }
            return count;
          }
        });
  }


  public Notification insertNotification(Notification notif) {
    notif.setMetaVersion(0);
    String sql = "INSERT INTO NOTIFICATION"
        + " (NOTIFICATION_ID, SURVEY_SITE_ID, PATIENT_ID, ASSESSMENT_REG_ID, EMAIL_TYPE, SURVEY_DT, EMAIL_DT, META_VERSION, DT_CREATED, DT_CHANGED)"
        + " VALUES (:pk, :site, :pat, :ari, :et, :sd, null, :mv, :now, null)";
    int rowCount = database.toInsert(sql)
        .argPkSeq(":pk", "notification_id_seq")
        .argLong(":site", notif.getSurveySiteId())
        .argString(":pat", notif.getPatientId())
        .argLong(":ari", notif.getAssessmentRegId())
        .argString(":et", notif.getEmailType())
        .argDate(":sd", notif.getSurveyDt())
        .argInteger(":mv", notif.getMetaVersion())
        .argDateNowPerDb(":now")
        .insert();
    if (rowCount != 1) {
      logger.error("Something went wrong adding notification, add returned: " + rowCount);
    }
    return notif;
  }


  public void deleteNotifications(AssessmentId assessmentId) {
    String sql = "DELETE FROM notification WHERE assessment_reg_id = ?";
    int rows = database.toUpdate(sql)
        .argLong(assessmentId.getId())
        .update();
    logger.debug("Deleted " + rows + " notifications");
  }


  private RowsHandler<ArrayList<SurveyRegistration>> getRegistrationsHandler() {
    return new RowsHandler<ArrayList<SurveyRegistration>>() {
      @Override
      public ArrayList<SurveyRegistration> process(Rows rs) throws Exception {
        return DataTableObjectConverter.convertToObjects(rs, SurveyRegistration.class);
      }
    };
  }


  public Date getLastEmailSentDate(String patientId) {
    ArrayList<String> excludeTypes = XMLFileUtils.getInstance(siteInfo).getExcludeFromSurveyCntVisits();
    String sql =
        "SELECT max(EMAIL_DT) as email_dt FROM NOTIFICATION n, appt_registration r WHERE  n.assessment_reg_id = r.assessment_reg_id "
            + "and n.survey_site_id = r.survey_site_id and n.SURVEY_SITE_ID = :site AND n.PATIENT_ID = ?";
    if (excludeTypes.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append(" and r.visit_type not in (");
      StringJoiner sj = new StringJoiner(",");
      for (String exc : excludeTypes) {
        sj.add("?");
      }
      sb.append(sj.toString());
      sb.append(")");
      sql += sb.toString();
    }
    SqlSelect select = database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(patientId);
    for (String excl : excludeTypes) {
      select = select.argString(excl);
    }
    return select.query(new RowsHandler<Date>() {
      @Override
      public Date process(Rows rs) throws Exception {
        if (rs.next()) {
          return rs.getDateOrNull("email_dt");
        }
        return null;
      }
    });
  }


  public String getSurveyType(String token) {
    String sql = "SELECT survey_type FROM survey_registration WHERE survey_site_id = :site AND token = ? ";
    return database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(token)
        .query(new RowsHandler<String>() {
      @Override
      public String process(Rows rs) throws Exception {
        String type = "FollowUp";
        if (rs.next()) {
          type = rs.getStringOrNull(1);
        }
        return type;
      }
    });
  }


  public PatientResultType getPatientResultType(String resultName) {
    String sql ="SELECT patient_res_typ_id, survey_site_id, result_name, result_title"
        + " FROM patient_result_type  WHERE result_name = ? AND survey_site_id = :site";
    return database.toSelect(sql)
        .argString(resultName)
        .argLong(":site", siteId)
        .query(getPatientResultTypeHandler());
  }


  /**
   * Returns the last result for this assessment registration and type.  If any are found later than the
   * last active survey session, the first one of those is returned.
   * @param assessmentId
   * @param patientResTypId
   * @return
   */
  public PatientResult getPatientResult(AssessmentId assessmentId, Long patientResTypId) {
    // Note assessment_reg_id determines the survey_site_id, so the latter is only only needed to filter out errors.
    String select =
        "SELECT patient_res_id, assessment_reg_id, survey_site_id, document_control_id, patient_res_typ_id, patient_res_vs, "
            + " dt_created, result_blob "
            + "FROM patient_result "
            + " WHERE survey_site_id = ? AND assessment_reg_id = ? AND patient_res_typ_id = ? ";

    String afterLastActivePatientResultQuery = select
            + " AND dt_created > "
            + "( SELECT max(s.last_active) "
            + "  FROM survey_registration sr "
            + "  JOIN survey_token t ON t.survey_token = sr.token AND t.survey_site_id = sr.survey_site_id "
            + "  JOIN survey_session s ON s.survey_token_id = t.survey_token_id "
            + "  WHERE sr.assessment_reg_id = ? AND sr.survey_site_id = ?"
            + ") ORDER BY patient_res_id";

    PatientResult result = database.toSelect(afterLastActivePatientResultQuery)
        .argLong(siteId)
        .argLong(assessmentId.getId())
        .argLong(patientResTypId)
        .argLong(assessmentId.getId())
        .argLong(siteId)
        .query(getPatientResultHandler());

    return result;

    // If it didn't find one later than the last session, don't just take the latest, if one exists.
    // Callers use the returned null to signify they need to regenerate the report
  }


  /**
   * Changed May 2017 to return the inserted result, not insert one result and then
   * return the last completed result
   * @param patientResult Should have everything set except the primary key: patient_res_id
   */
  public PatientResult insertPatientResult(PatientResult patientResult) {
    String sql =
        "INSERT INTO patient_result ( patient_res_id, assessment_reg_id, survey_site_id,"
        + " document_control_id, patient_res_typ_id, patient_res_vs, dt_created, result_blob )"
        + " VALUES (:pk, :ari, :site, :cid, :rt, :rv, :now, :b)";
    SqlInsert sqlInsert = database.toInsert(sql)
        .argPkSeq(":pk", "patient_seq")
        .argLong(":ari", patientResult.getAssessmentRegId())
        .argLong(":site", patientResult.getSurveySiteId())
        .argString(":cid", patientResult.getDocumentControlId())
        .argLong(":rt", patientResult.getPatientResTypId())
        .argLong(":rv", patientResult.getPatientResVs())
        .argDateNowPerDb(":now")
        .argBlobBytes(":b", patientResult.getResultBytes());

    Long id = sqlInsert.insertReturningPkSeq("patient_res_id");
    patientResult.setPatientResId(id);
    return patientResult;
  }


  public void insertPatientResSurveyRegLink(PatientResSurveyRegLink patientResSurveyReg) {
    String stmt = "INSERT INTO patient_res_to_survey_reg ( survey_reg_id, patient_res_id, survey_site_id )"
        + " VALUES (?, ?, ?)";
    database.toInsert(stmt) .argLong(patientResSurveyReg.getSurveyRegId())
        .argLong(patientResSurveyReg.getPatientResId())
        .argLong(patientResSurveyReg.getSurveySiteId()).insert(1);
  }


  // ===============


  private static RowsHandler<ArrayList<Notification>> getNotificationHandler() {
    return new RowsHandler<ArrayList<Notification>>() {
      @Override
      public ArrayList<Notification> process(Rows rs) throws Exception {
        return DataTableObjectConverter.convertToObjects(rs, Notification.class);
      }
    };
  }


  static class AssessmentRegsHandler implements RowsHandler<ArrayList<AssessmentRegistration>> {
    final Database database;
    final Long siteId;
    PatientDao patDao;

    public AssessmentRegsHandler(Database db, Long siteId) {
      database = db;
      this.siteId = siteId;
    }

    @Override
    public ArrayList<AssessmentRegistration> process(Rows rs) throws Exception {
      ArrayList<AssessmentRegistration> registrations = DataTableObjectConverter.convertToObjects(rs, AssessmentRegistration.class);

      for (AssessmentRegistration registration : registrations) {
        ArrayList<SurveyRegistration> surveyRegs =
            database.toSelect(SELECT_SURVEY_REGS_BY_ASSESS_ID)
            .argLong(siteId)
            .argLong(registration.getAssessmentRegId())
            .query(new SurveyRegHandler());
        registration.setSurveyRegList(surveyRegs);
      }

      return registrations;
    }
  }

  private static RowsHandler<PatientResultType> getPatientResultTypeHandler() {
    return new RowsHandler<PatientResultType>() {
      @Override
      public PatientResultType process(Rows rs) throws Exception {
        return DataTableObjectConverter.convertFirstRowToObject(rs, PatientResultType.class);
      }
    };
  }

  private static RowsHandler<PatientResult> getPatientResultHandler() {
    return new RowsHandler<PatientResult>() {
      @Override
      public PatientResult process(Rows rs) throws Exception {
        return DataTableObjectConverter.convertFirstRowToObject(rs, PatientResult.class);
      }
    };
  }


  /**
   * private Row Handler for Patient Registrations
   */
  static RowsHandler<ArrayList<PatientRegistration>> getPatientRegistrationHandler(Database database, final SiteInfo siteInfo) {
    return new RowsHandler<ArrayList<PatientRegistration>>() {

      @Override
      public ArrayList<PatientRegistration> process(Rows rs) throws Exception {
        ArrayList<PatientRegistration> registrations = DataTableObjectConverter.convertToObjects(rs,
            PatientRegistration.class);
        // Done getting the registrations. Now get the SurveyRegs for all the PatientRegs

        if (registrations.size() == 1) {  // do a simple query for its surveyRegistrations
          PatientRegistration registration = registrations.get(0);
          AssessmentRegistration assessment = registration.getAssessment();
          ArrayList<SurveyRegistration> registrationList = database.toSelect(SELECT_SURVEY_REGS_BY_ASSESS_ID)
              .argLong(siteInfo.getSiteId())
              .argLong(assessment.getAssessmentRegId())
              .query(new SurveyRegHandler());
          registration.setSurveyRegList(registrationList);
        }
        else if (registrations.size() > 1) {  // query for the whole set of surveyRegistrations
          HashMap<Long, ArrayList<SurveyRegistration>> assessmentIdToSurveyRegList =
                                                            fetchAllRegistrations(database, siteInfo, registrations);

          // If two patientReg's have the same assessment, the second won't be populated, so check
          for (PatientRegistration preg: registrations) {
            Long assessId = preg.getAssessmentId().getId();
            ArrayList<SurveyRegistration> surRegList = assessmentIdToSurveyRegList.get(assessId);
            preg.setSurveyRegList(surRegList != null ? surRegList : new ArrayList<SurveyRegistration>());
          }
        } // finished querying the database

        int daysOut = -1;
        try {
          daysOut = Integer.parseInt(siteInfo.getProperty("appointment.lastsurvey.daysout"));
        } catch (Exception ex) {
          // ignore
        }

        for (PatientRegistration patientReg : registrations ) {
          if (patientReg != null && patientReg.getSurveyDt() != null) {
            Date surveyDate = DateUtils.getDateStart(siteInfo, patientReg.getSurveyDt());
            Date cutoffDate = new Date(surveyDate.getTime() - (daysOut * DateUtils.MILISECONDS_PER_DAY));
            if (patientReg.getSurveyLastCompleted() != null && patientReg.getSurveyLastCompleted().after(cutoffDate)) {
              patientReg.setSurveyRequired(false);
              logger.debug("Survey not required, for patient " + patientReg.getPatientId() + " ApptRegId " + patientReg.getApptId()
                  + "last completed " + patientReg.getSurveyLastCompleted().toString()
                +  " was after the cutoff of " + cutoffDate.toString());
            }
          }
        }
        return registrations;
      }


      /**
       * Fetches the SurveyRegistrations for all the assessmentIds in the PatientReg list
       * @return a hash of assessmentId -> surveyList.
       */
      private HashMap<Long, ArrayList<SurveyRegistration>> fetchAllRegistrations(Database database,
                                            final SiteInfo siteInfo,  ArrayList<PatientRegistration> registrations) {

        QuerySetExpander expander = new QuerySetExpander("fetchAllRegs", registrations.size(), SELECT_SURVEY_REGS_IN_SET);
        HashMap<Long, ArrayList<SurveyRegistration>> assessmentIdToSurveyRegList = new HashMap<>(registrations.size());

        for (String sql: expander) {
          SqlSelect sqlSelect = database.toSelect(sql).argLong(siteInfo.getSiteId());
          sqlSelect = expander.argLongs(sqlSelect, ix -> registrations.get(ix).getAssessmentRegId());
          ArrayList<SurveyRegistration> partialSurveyRegList = sqlSelect.query(new SurveyRegHandler());

          // put each surveyReg in a list hashed by assessId
          for (SurveyRegistration surveyReg: partialSurveyRegList) {
            Long assessId = surveyReg.getAssessmentRegId();
            ArrayList<SurveyRegistration> sRegs = assessmentIdToSurveyRegList.get(assessId);
            if (sRegs == null) {
              sRegs = new ArrayList<SurveyRegistration>(1);
              sRegs.add(surveyReg);
              assessmentIdToSurveyRegList.put(assessId, sRegs);
            } else {
              addToSurveyListIfUnique(sRegs, surveyReg);
            }
          }
        }
        return assessmentIdToSurveyRegList;
      }


      void addToSurveyListIfUnique(ArrayList<SurveyRegistration>list, SurveyRegistration surveyReg) {
        for (SurveyRegistration reg: list) {
          if (reg.getSurveyRegId().equals(surveyReg.getSurveyRegId())) {
            return;
          }
        }
        list.add(surveyReg);
      }
    };
  }


  static class SurveyRegHandler implements RowsHandler<ArrayList<SurveyRegistration>> {
    @Override
    public ArrayList<SurveyRegistration> process(Rows rs) throws Exception {
      return DataTableObjectConverter.convertToObjects(rs, SurveyRegistration.class);
    }
  }

  private static  class PatientRegistrationSql {

    private static final String SELECT_BASE = "select " +
        "  ar.appt_reg_id, ar.survey_site_id, ar.patient_id, ar.visit_dt, " +
        "  ar.registration_type, ar.visit_type, ar.provider_id, ar.encounter_eid, " +
        "  asmt.assessment_reg_id, asmt.email_addr, asmt.assessment_dt, asmt.assessment_type, " +
        "  ar.appt_complete as appointment_status_string, " +
        "  ar.meta_version, ar.dt_created, ar.dt_changed, " +
        "  p.first_name, p.last_name, p.dt_birth, p.consent, " +
        "  p.meta_version as patient_meta_version, p.dt_created as patient_dt_created, p.dt_changed as patient_dt_changed, " +
        "  ( select max(email_dt) from notification n where n.assessment_reg_id = ar.assessment_reg_id " +
        "  ) as email_dt, " +
        "  ( select count(*) from notification n where n.assessment_reg_id = ar.assessment_reg_id and email_dt is not null " +
        "  ) as number_emails_sent, " +
        "  ( select count(*) from activity a " +
        "    where a.survey_site_id = ar.survey_site_id and a.assessment_reg_id = ar.assessment_reg_id and " +
        "      a.activity_type= '" + Constants.ACTIVITY_CHART_PRINTED + "' " +
        "  ) as number_prints, " +
        "  ( select max(survey_last_completed) from (" +
        "      select appt_reg_id," +
        "             count(sr.survey_reg_id) as possible," +
        "             sum(case when a2.activity_type = 'Completed' then 1 else 0 end) as completed," +
        "             max(a2.activity_dt) as survey_last_completed" +
        "        from appt_registration ar2" +
        "        left join survey_registration sr on sr.assessment_reg_id = ar2.assessment_reg_id" +
        "        left join activity a2 on a2.survey_site_id = sr.survey_site_id " +
        "       and a2.token = sr.token and a2.activity_type = '" + Constants.ACTIVITY_COMPLETED + "'" +
        "       where ar2.appt_reg_id != ar.appt_reg_id" +
        "         and ar2.survey_site_id = ?" +
        "         and ar2.patient_id = ar.patient_id" +
        "      group by appt_reg_id" +
        "  ) last_completed where possible = completed) as survey_last_completed " +
        "from  " +
        "  appt_registration ar " +
        "  left join assessment_registration asmt on asmt.assessment_reg_id = ar.assessment_reg_id " +
        "  join patient p on p.patient_id = ar.patient_id " +
        "where  " +
        "  ar.survey_site_id = ?";

    // TABLEREF patient_attribute
    private static String beginSelectConsented() {
      return SELECT_BASE +
          " and exists ( select * from patient_attribute pa where pa.patient_id = ar.patient_id " +
          "  and pa.survey_site_id = ar.survey_site_id and pa.data_name = '" + Constants.ATTRIBUTE_PARTICIPATES + "' and pa.data_value = 'y'" +
          " )";
    }

    private static final String wasPrinted =
        "(select * from activity a1 where a1.survey_site_id=ar.survey_site_id and a1.assessment_reg_id=ar.assessment_reg_id " +
            "and activity_type = '" + Constants.ACTIVITY_CHART_PRINTED + "')";

    private static final String clauseNotPrinted = " AND not exists " + wasPrinted;
    private static final String clausePrinted = " AND exists " + wasPrinted;
    private static final String orderBy = " ORDER BY ar.visit_dt, p.LAST_NAME ";
    private static final String noNotify = " and not exists (select * from notification n where n.assessment_reg_id = ar.assessment_reg_id and n.email_dt is not null)";

    static String getSelectStatmentBetweenDates(PatientRegistrationSearch searchOptions, List<String> registrationTypes) {
      StringBuffer sql = new StringBuffer();
      if (searchOptions.consented()) {
        sql.append(beginSelectConsented());
      } else {
        sql.append(SELECT_BASE);
      }
      sql.append(" and ar.visit_dt between ? and ? ");
      internalAppendRegTypes(registrationTypes, searchOptions.cancelled(), sql);
      internalAppendExcludeTypes(searchOptions.getExcludeTypes(), sql);
      internalAppendClinics(searchOptions.getIncludeClinics(), sql);
      if (searchOptions.unnotified()) {
        sql.append(noNotify);
      }

      if (searchOptions.printed() && !searchOptions.notPrinted()) {
        sql.append(clausePrinted);
      }
      if (!searchOptions.printed() && searchOptions.notPrinted()) {
        sql.append(clauseNotPrinted);
      }

      sql.append(orderBy);

      return sql.toString();
    }

    private static void internalAppendRegTypes(List<String> registrationTypes, boolean showCancelled, StringBuffer sql) {
      if ((registrationTypes == null) || registrationTypes.isEmpty()) {
        if (!showCancelled) {
          sql.append(" AND ar.REGISTRATION_TYPE != '" + Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT + "' ");
        }
      } else {
        sql.append(" AND ( ar.REGISTRATION_TYPE in (");
        for(int i=0; i<registrationTypes.size(); i++) {
          if (i > 0) {
            sql.append(",");
          }
          sql.append("'").append(registrationTypes.get(i)).append("'");
        }
        sql.append(") ");
        if (showCancelled) {
          sql.append(" OR ar.REGISTRATION_TYPE = '" + Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT + "' ");
        }
        sql.append(")");
      }
    }

    private static void internalAppendExcludeTypes(List<String> excludeTypes, StringBuffer sql) {
      if (excludeTypes != null && excludeTypes.size() > 0) {
        sql.append(" AND asmt.assessment_type not in (");
        for (int e=0;e<excludeTypes.size(); e++) {
          if (e>0) {
            sql.append(",");
          }
          sql.append("'").append(excludeTypes.get(e)).append("'");
        }
        sql.append(")");
      }
    }

    private static void internalAppendClinics(List<String> includeClinics, StringBuffer sql) {
      if ((includeClinics != null) && !includeClinics.isEmpty()) {
        sql.append(" AND ( upper(ar.clinic) in (");
        for (int i=0; i<includeClinics.size(); i++) {
          if (i > 0) {
            sql.append(",");
          }
          sql.append("'").append(includeClinics.get(i)).append("'");
        }
        sql.append(")");
        // Stand alone surveys do not have a clinic so they are are always visible
        // when filtering by clinic if stand alone surveys are included
        sql.append(" OR ar.registration_type = '" + Constants.REGISTRATION_TYPE_STANDALONE_SURVEY + "'");
        sql.append(") ");
      }
    }

    static String getSelectStatementByPatientId(List<String> excludeTypes) {
      StringBuffer sql = new StringBuffer();
      sql.append( SELECT_BASE );
      sql.append(" AND p.PATIENT_ID = ? ");
      internalAppendExcludeTypes(excludeTypes, sql);
      sql.append(" ORDER BY ar.visit_dt, p.LAST_NAME ");
      return sql.toString();
    }

    static String getSelectStatementByRegId() {
      return SELECT_BASE + " and ar.appt_reg_id = ? ";
    }

    static String getSelectStatementByAssessmentId() {
      return SELECT_BASE + " and ar.assessment_reg_id = ? ";
    }

    static String getSelectStatementByPatientIdByType() {
      return SELECT_BASE + "AND p.PATIENT_ID = ? AND ar.REGISTRATION_TYPE = ? ORDER BY ar.VISIT_DT, p.LAST_NAME ";
    }

    static String getSelectStatementByPatientIdAndDateFrom() {
      return SELECT_BASE + " AND p.PATIENT_ID = ? AND ar.VISIT_DT > ?";
    }
  }

}
