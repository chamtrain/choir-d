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
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlUpdate;

public class PatStudyDao {
  private static final Logger logger = LoggerFactory.getLogger(PatStudyDao.class);

  final public Database database;
  final SiteInfo siteInfo;
  final Long siteId;
  private static final String SELECT_PATIENT_STUDY = "SELECT PATIENT_STUDY_ID, SURVEY_SITE_ID, SURVEY_REG_ID, "
      + "PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, "
      + "DT_CREATED, DT_CHANGED";
  private static final String ORDER_PATIENT_STUDY = " ORDER BY PATIENT_ID, TOKEN, ORDER_NUMBER ";
  public PatStudyDao(Database db, SiteInfo siteInfo) {
    database = db;
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
  }

  public PatientStudy getPatientStudy(Patient pat, Study study, Token tok) {
    return getPatientStudy(pat.getPatientId(), study.getStudyCode(), tok.getToken(), false);
  }

  public PatientStudy getPatientStudy(PatientStudyExtendedData data, boolean withContents) {
    return getPatientStudy(data.getPatientId(), data.getStudyCode(), data.getToken(), withContents);
  }

  public PatientStudy getPatientStudy(String patientId, Integer studyCode, String token, boolean withContents) {
    String sql = SELECT_PATIENT_STUDY + (withContents ? ", XML_CLOB as CONTENTS" : "")
        + " FROM PATIENT_STUDY "
        + " WHERE SURVEY_SITE_ID = ? AND PATIENT_ID = ? AND STUDY_CODE = ? AND TOKEN = ? "
        + ORDER_PATIENT_STUDY;

    return database.toSelect(sql).argLong(siteId)
        .argString(patientId)
        .argInteger(studyCode)
        .argString(token)
        .query(rs -> DataTableObjectConverter.convertFirstRowToObject(rs, PatientStudy.class));
  }

  public ArrayList<PatientStudy> getPatientStudiesByToken(String token, boolean withContents) {
    String sql = SELECT_PATIENT_STUDY + (withContents ? ", XML_CLOB as CONTENTS" : "")
        + " FROM PATIENT_STUDY WHERE SURVEY_SITE_ID = ? AND TOKEN = ? AND DT_CHANGED is null "
        + ORDER_PATIENT_STUDY;
    return database.toSelect(sql).argLong(siteId)
        .argString(token)
        .query(rs -> {
          ArrayList<PatientStudy> studies = new ArrayList<>();
          while (rs.next()) {
            PatientStudy patStudy = new PatientStudy();
            patStudy.setPatientStudyId(rs.getLongOrNull());
            patStudy.setSurveySiteId(rs.getLongOrNull());
            patStudy.setSurveyRegId(rs.getLongOrNull());
            patStudy.setPatientId(rs.getStringOrNull());
            patStudy.setSurveySystemId(rs.getIntegerOrNull());
            patStudy.setStudyCode(rs.getIntegerOrNull());
            patStudy.setToken(rs.getStringOrNull());
            patStudy.setExternalReferenceId(rs.getStringOrNull());
            patStudy.setOrderNumber(rs.getIntegerOrNull());
            patStudy.setMetaVersion(rs.getIntegerOrNull());
            patStudy.setDtCreated(rs.getDateOrNull());
            patStudy.setDtChanged(rs.getDateOrNull());
            studies.add(patStudy);
          }
          return studies;
        });
  }

  public static final String SELECT_PAT_STUDY_EXT =
      "SELECT ps.PATIENT_ID, ps.SURVEY_SYSTEM_ID, ps.STUDY_CODE, ps.TOKEN, ps.EXTERNAL_REFERENCE_ID, " +
      "  ps.ORDER_NUMBER, ps.META_VERSION, ps.DT_CREATED, ps.DT_CHANGED, " +
      "  ( select count(*) from patient_study ps1 where ps1.survey_site_id = ps.survey_site_id and " +
      "      ps1.patient_id = ps.patient_id and ps1.dt_changed is not null ) as NUMBER_COMPLETED, " +
      "  ps.XML_CLOB as CONTENTS, p.first_name, p.last_name, " +
      "  st.study_description, ss.survey_system_name, " +
      "  ps.patient_study_id, ps.survey_site_id, ps.survey_reg_id " +
      "FROM patient_study ps, patient p, study st, survey_system ss " +
      "WHERE ps.dt_changed is not null and " +
      "  ps.survey_site_id = ? and " +
      "  p.patient_id = ps.patient_id and " +
      "  st.study_code = ps.study_code and st.survey_system_id = ps.survey_system_id and " +
      "  ss.survey_system_id = ps.survey_system_id ";

  public ArrayList<PatientStudyExtendedData> getPatientStudyExtendedDataByPatientId(String patientId) {
    String sql = SELECT_PAT_STUDY_EXT + " AND ps.PATIENT_ID = ? order by ps.dt_changed";

    ArrayList<PatientStudyExtendedData> studies = database.toSelect(sql)
        .argLong(siteId)
        .argString(patientId).query(rs -> {
          ArrayList<PatientStudyExtendedData> patientData = DataTableObjectConverter.convertToObjects(rs,
              PatientStudyExtendedData.class);
          return patientData;
        });
    return studies;
  }

  public ArrayList<PatientStudyExtendedData> getPatientStudyExtendedDataByPatientAndStudy(String patientId,
      String studyDescription) {
    String sql = SELECT_PAT_STUDY_EXT + " AND ps.PATIENT_ID = ? AND lower(st.study_description) = lower(?) "
        + " order by ps.dt_changed";

    ArrayList<PatientStudyExtendedData> studies = database.toSelect(sql)
        .argLong(siteId)
        .argString(patientId)
        .argString(studyDescription)
        .query(rs -> {
          ArrayList<PatientStudyExtendedData> patientData =
              DataTableObjectConverter.convertToObjects(rs, PatientStudyExtendedData.class);
          return patientData;
        }
        );
    return studies;
  }


  public ArrayList<PatientStudyExtendedData> getPatientStudyExtendedDataByPatientId(String patientId, Date fromDt, Date toDt) {
    String sql = SELECT_PAT_STUDY_EXT + " AND ps.PATIENT_ID = ? AND ps.dt_changed between ? and ?";

    ArrayList<PatientStudyExtendedData> studies = database.toSelect(sql)
        .argLong(siteId)
        .argString(patientId)
        .argDate(fromDt).argDate(toDt)
        .query(rs -> {
          ArrayList<PatientStudyExtendedData> patientData = DataTableObjectConverter.convertToObjects(rs,
              PatientStudyExtendedData.class);
          return patientData;
        });
    return studies;
  }


  public ArrayList<PatientStudyExtendedData> getPatientStudyExtendedDataByPatientId(String patientId, Date toDt) {
    String sql =
        "SELECT ps.PATIENT_ID, ps.SURVEY_SYSTEM_ID, ps.STUDY_CODE, ps.TOKEN, ps.EXTERNAL_REFERENCE_ID, " +
            "  ps.ORDER_NUMBER, ps.META_VERSION, ps.DT_CREATED, ps.DT_CHANGED, " +
            "  ( select count(*) from patient_study ps1 where ps1.survey_site_id = ps.survey_site_id and " +
            "      ps1.patient_id = ps.patient_id and ps1.dt_changed is not null ) as NUMBER_COMPLETED, " +
            "  ps.XML_CLOB as CONTENTS, p.first_name, p.last_name, " +
            "  st.study_description, ss.survey_system_name, " +
            "  ps.patient_study_id, ps.survey_site_id, ps.survey_reg_id " +
            "FROM patient_study ps, patient p, study st, survey_system ss, survey_registration sr " +
            "WHERE ps.dt_changed is not null and " +
            "  ps.survey_site_id = ? and p.patient_id = ps.patient_id and " +
            "  st.study_code = ps.study_code and st.survey_system_id = ps.survey_system_id and " +
            "  ss.survey_system_id = ps.survey_system_id and " +
            "  sr.survey_site_id = ps.survey_site_id and sr.token = ps.token AND ps.PATIENT_ID = ? " +
            " AND sr.survey_dt <= ? ORDER BY SURVEY_DT";

    ArrayList<PatientStudyExtendedData> studies = database.toSelect(sql)
        .argLong(siteId)
        .argString(patientId).argDate(toDt).query(rs -> {
          ArrayList<PatientStudyExtendedData> patientData = DataTableObjectConverter.convertToObjects(rs,
              PatientStudyExtendedData.class);
          return patientData;
        }
        );
    return studies;
  }


  public ArrayList<PatientStudyExtendedData> getPatientStudyDataByNameLike(
      String lastNameString) {
    String sql = SELECT_PAT_STUDY_EXT + " AND lower(p.last_name) like lower(?)";

    ArrayList<PatientStudyExtendedData> studies = database.toSelect(sql)
        .argLong(siteId)
        .argString("%" + lastNameString + "%").query(rs -> {
          ArrayList<PatientStudyExtendedData> patientData = DataTableObjectConverter.convertToObjects(rs,
              PatientStudyExtendedData.class);
          return patientData;
        }
        );
    return studies;
  }


  public ArrayList<PatientStudyExtendedData> getPatientStudyDataByNameLike(String lastNameString, Date dtFrom, Date dtTo) {
    String sql = SELECT_PAT_STUDY_EXT + " AND lower(p.last_name) like lower(?)"
    + " AND ps.dt_changed between ? and ?";

    ArrayList<PatientStudyExtendedData> studies = database.toSelect(sql)
        .argLong(siteId)
        .argString("%" + lastNameString + "%").argDate(dtFrom).argDate(dtTo)
        .query(rs -> {
          ArrayList<PatientStudyExtendedData> patientData = DataTableObjectConverter.convertToObjects(rs,
              PatientStudyExtendedData.class);
          return patientData;
        }
        );
    return studies;
  }


  public ArrayList<PatientStudyExtendedData> getPatientStudyDataBySurveyRegIdAndStudyDescription(
      Long surveyRegId, String studyDescription) {
    String sql = SELECT_PAT_STUDY_EXT +" AND ps.survey_reg_id = ? " +
    " AND lower(st.study_description) = lower(?) ";

    ArrayList<PatientStudyExtendedData> studies = database.toSelect(sql)
        .argLong(siteId)
        .argLong(surveyRegId).argString(studyDescription)
        .query(rs -> {
          ArrayList<PatientStudyExtendedData> patientData = DataTableObjectConverter.convertToObjects(rs,
              PatientStudyExtendedData.class);
          return patientData;
        }
        );
    return studies;
  }

  public ArrayList<PatientStudyExtendedData> getPatientStudyDataBySurveyRegId(
      Long surveyRegId) {
    String sql = SELECT_PAT_STUDY_EXT +" AND ps.survey_reg_id = ? ";
    ArrayList<PatientStudyExtendedData> studies = database.toSelect(sql)
        .argLong(siteId)
        .argLong(surveyRegId)
        .query(rs -> {
          ArrayList<PatientStudyExtendedData> patientData = DataTableObjectConverter.convertToObjects(rs,
              PatientStudyExtendedData.class);
          return patientData;
        }
        );
    return studies;
  }

  public PatientStudyExtendedData getPatientStudyExtendedDataByToken(Token token, User user) {
    PatientStudyExtendedData patStudyE = getPatientStudyExtendedDataByToken(token, null, user);
    if (patStudyE != null) {
      SurveySystem surveySystem = new SurveySystDao(database).getSurveySystem(patStudyE.getSurveySystemId());
      if (surveySystem != null) {
        patStudyE.setSurveySystemName(surveySystem.getSurveySystemName());
      }
    }
    return patStudyE;
  }


  public PatientStudyExtendedData getPatientStudyExtendedDataByToken(Token token,
      String surveySystemName, User user) {
    PatientStudyExtendedData patStudyE = null;

    String sql = SELECT_PATIENT_STUDY
        + " FROM PATIENT_STUDY "
        + "WHERE SURVEY_SITE_ID = ? AND TOKEN = ? AND DT_CHANGED is null "
        + ORDER_PATIENT_STUDY;
    PatientStudy foundNextStudy =  database.toSelect(sql).argLong(siteId)
        .argString(token.getToken()).query(rs -> {
          // boolean found = false;
          while (rs.next()) {
            // if (dtChanged != null) {
            PatientStudy patStudy = new PatientStudy(rs.getLongOrNull("SURVEY_SITE_ID"));
            patStudy.setPatientId(rs.getStringOrNull("PATIENT_ID"));
            patStudy.setSurveySystemId(rs.getIntegerOrNull("SURVEY_SYSTEM_ID"));
            patStudy.setStudyCode(rs.getIntegerOrNull("STUDY_CODE"));
            patStudy.setToken(rs.getStringOrNull("TOKEN"));
            patStudy.setExternalReferenceId(rs.getStringOrNull("EXTERNAL_REFERENCE_ID"));
            patStudy.setMetaVersion(rs.getIntegerOrNull("META_VERSION"));
            patStudy.setDtCreated(rs.getDateOrNull("DT_CREATED"));
            patStudy.setOrderNumber(rs.getIntegerOrNull("ORDER_NUMBER"));
            patStudy.setDtChanged(rs.getDateOrNull("DT_CHANGED"));
            patStudy.setPatientStudyId(rs.getLongOrNull("PATIENT_STUDY_ID"));
            patStudy.setSurveyRegId(rs.getLongOrNull("SURVEY_REG_ID"));
            return patStudy;
          }
          return null;

        });

    if (foundNextStudy != null && foundNextStudy.getPatientId() != null) {

      patStudyE = new PatientStudyExtendedData(foundNextStudy);
      PatientDao patientDao = new PatientDao(database, siteId, user);
      Patient pat = patientDao.getPatient(foundNextStudy.getPatientId());
      if (pat != null) {
        patStudyE.setPatient(pat);
      }

      SurveySystDao ssdao = new SurveySystDao(database);
      Study study = ssdao.getStudy(foundNextStudy.getSurveySystemId(), foundNextStudy.getStudyCode());
      patStudyE.setStudy(study);

      patStudyE.setSurveySystemName(surveySystemName);

    }

    return patStudyE;

  }


  public Map<String, Boolean> getPatientAssistedStudyTokens(String patientId) {
    StringBuilder sqlbuf = new StringBuilder();
    // TABLEREF patient_study
    /*
   sqlbuf.append("WITH new_patient_study as ( SELECT xmltype(xml_clob) as xml_results, study_code, patient_id, token FROM patient_study ");
   sqlbuf.append("   WHERE survey_site_id = :site and patient_id= ? and xml_clob is not null ), ");
   sqlbuf.append(" responses AS ( SELECT token, extractValue(value(itm), '/Item/Responses/Response/ref') as reference_id , ");
   sqlbuf.append(" xmlType('<form>' || extract(value(itm), '/Item/Responses/Response/item') || '</form>') as RESP_ITEM_XML");
   sqlbuf.append(" FROM new_patient_study ps, study s, table(XMLSequence(extract(XML_RESULTS, '/Form/Items/Item'))) itm ");
   sqlbuf.append(" WHERE ps.study_code = s.study_code and ps.patient_id= ? and study_description in ('names', 'assistedChild', 'assistedParent')) ");
   sqlbuf.append(" select  token ");
   sqlbuf.append(" from responses r, table(XMLSequence(extract(RESP_ITEM_XML, '/form/item'))) ITEM_XML ");
   sqlbuf.append(" where reference_id = 'ASSISTED' and existsNode(value(ITEM_XML), '/item[@selected=\"true\"]') = 1 ");
   sqlbuf.append("  and 'Yes' = extractValue(value(ITEM_XML), '/item/label') ");

   return database.toSelect(sqlbuf.toString())
       .argLong(":site", getTheSiteId())
       .argString(patientId)
       .argString(patientId)
       .query(new RowsHandler<Hashtable<String, Boolean>>() {
     @Override
     public Hashtable<String, Boolean> process(Rows rs) throws Exception {
       final Hashtable<String, Boolean> tokenList = new Hashtable<>();
       while (rs.next()) {
         tokenList.put(rs.getStringOrNull("token"), Boolean.valueOf(true));
       }
       return tokenList;
     }
   });
     */

    SurveyDao surveyDao = new SurveyDao(database);
    final SurveyQuery query = new SurveyQuery(database, surveyDao, siteId);
    // TABLEREF survey_token
    sqlbuf.append("select survey_token_id, st.survey_token, is_complete from survey_token st, patient_study ps, study s where ps.patient_id = ? ")
    .append(" and st.survey_token = ps.token and ps.study_code = s.study_code and study_description = 'names' ")
    .append(" and st.survey_site_id = :site and ps.xml_clob is not null");
    ArrayList<Survey> surveys = database.toSelect(sqlbuf.toString()).argString(patientId)
        .argLong(":site", siteId).query(
            new RowsHandler<ArrayList<Survey>>() {

              @Override
              public ArrayList<Survey>  process(Rows rs) throws Exception {
                ArrayList<Survey> surveys = new ArrayList<>();
                while (rs.next()) {
                  surveys.add(query.surveyBySurveyTokenId(rs.getLongOrZero(1), rs.getStringOrEmpty(2), rs.getBooleanOrFalse(3)));
                }
                return surveys;
              }
            });

    Map<String, Boolean> assistedTokens = new HashMap<>();
    SurveySystem surveySystem = new SurveySystDao(database).getSurveySystem("Local");
    SurveySystDao ssdao = new SurveySystDao(database);
    Study study = ssdao.getStudy(surveySystem.getSurveySystemId(), "names");
    if (surveySystem == null || study == null || surveys == null || surveys.size() < 1) {
      return assistedTokens;
    }

    for (Survey survey : surveys) {
      SurveyStep step = survey.answeredStepByProviderSectionQuestion(surveySystem.getSurveySystemId().toString(), study.getStudyCode().toString(), "Order1");
      //formFieldValue is converting empty string to null and returning, So checking not null
      if (step != null && step.answer() != null && step.answer().formFieldValue("2:1:ASSISTED") != null) {
        String answerId = step.answer().formFieldValue("2:1:ASSISTED");
        String answerLabel = null;
        if (answerId != null) {
          FormFieldValue fieldValue = step.questionFormFieldValue("2:1:ASSISTED", answerId);
          if (fieldValue != null) {
            answerLabel = fieldValue.getLabel();
            if (answerLabel != null && answerLabel.toLowerCase().equals("yes")) {
              assistedTokens.put(survey.getSurveyToken(), true);
            }
          }
        }
      }
    }
    return assistedTokens;
  }


  public PatientStudy setPatientStudyContents(PatientStudy patStudy, String xmlDocumentString) {
    return setPatientStudyContents(patStudy, xmlDocumentString, true);
  }

  public PatientStudy setPatientStudyContents(PatientStudy patStudy, boolean updateDate) {
    return setPatientStudyContents(patStudy, patStudy.getContents(), updateDate);
  }

  public PatientStudy setPatientStudyContents(PatientStudy patStudy,
      String xmlDocumentString, boolean updateDate) {

    StringUtils.replace(xmlDocumentString, "'", "\'"); // zzz escape single quotes
    // before writing
    patStudy.setContents(xmlDocumentString);
    try {
      String sql = "UPDATE patient_study set"
        + (updateDate ? " dt_changed = :now," : "") + " xml_clob = :c"
        + " WHERE patient_study_id = :s";
      SqlUpdate sqlUpdate = database.toUpdate(sql);
      if (updateDate) {
        sqlUpdate = sqlUpdate
          .argClobString(":c", patStudy.getContents())
          .argLong(":s", patStudy.getPatientStudyId())
          .argDateNowPerDb(":now");
      } else {
        sqlUpdate = sqlUpdate
            .argClobString(":c", patStudy.getContents())
            .argLong(":s", patStudy.getPatientStudyId());
      }
      int rows = sqlUpdate.update();
      logger.trace("Update returned {} changed", rows);
    } catch (Exception e) {
      logger.error("setPatientStudyContents caught {}", e.getMessage(), e);
    }
    return patStudy;
  }

  public PatientStudy insertPatientStudy(PatientStudy patStudy) {

    patStudy.setMetaVersion(0);
    if (patStudy != null && patStudy.getToken() != null) {
      // We may need to get the siteId id & survey registration id for this token and add it before inserting row
      if (patStudy.getSurveyRegId() == null || patStudy.getSurveySiteId() == null) {
        SurveyRegistration registration = new AssessDao(database, siteInfo).getRegistration(patStudy.getToken());
        if (registration == null) {// defensive
          return null;
        }
        patStudy.setSurveyRegId(registration.getSurveyRegId());
      }
    }

    String stmt = "INSERT INTO PATIENT_STUDY ( PATIENT_STUDY_ID, SURVEY_SITE_ID, SURVEY_REG_ID, PATIENT_ID, SURVEY_SYSTEM_ID,"
        + " STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED )"
        + " VALUES (:pk, ?, ?, ?, ?, ?, ?, ?, ?, ?, :now, null)";
     database.toInsert(stmt) .argLong(patStudy.getSurveySiteId())
        .argLong(patStudy.getSurveyRegId())
        .argString(patStudy.getPatientId())
        .argInteger(patStudy.getSurveySystemId())
        .argInteger(patStudy.getStudyCode())
        .argString(patStudy.getToken())
        .argString(patStudy.getExternalReferenceId())
        .argInteger(patStudy.getOrderNumber())
        .argInteger(patStudy.getMetaVersion())
        .argPkSeq(":pk", "survey_seq")
        .argDateNowPerDb(":now")
        .insert(1);
    return getPatientStudy(patStudy.getPatientId(), patStudy.getStudyCode(), patStudy.getToken(), true);
  }


  /**
   * Deletes all patient study rows for this token.
   */
  public int deletePatientStudy(String token) {
    int rows = database.toDelete("DELETE FROM PATIENT_STUDY WHERE TOKEN = ?")
        .argString(token)
        .update();

    logger.trace("Deleted {} patient studies", rows);
    return rows;
  }

  private final String patStudyExtSql =
      "SELECT ps.PATIENT_ID, ps.SURVEY_SYSTEM_ID, ps.STUDY_CODE, ps.TOKEN, ps.EXTERNAL_REFERENCE_ID, " +
      "  ps.ORDER_NUMBER, ps.META_VERSION, ps.DT_CREATED, ps.DT_CHANGED, " +
      "  ps.XML_CLOB as CONTENTS, p.first_name, p.last_name, " +
      "  st.study_description, ss.survey_system_name, " +
      "  ps.patient_study_id, ps.survey_site_id, ps.survey_reg_id " +
      "FROM patient_study ps, patient p, study st, survey_system ss " +
      "WHERE ps.survey_site_id = ? and " +
      "  p.patient_id = ps.patient_id and " +
      "  st.study_code = ps.study_code and st.survey_system_id = ps.survey_system_id and " +
      "  ss.survey_system_id = ps.survey_system_id ";

  // gets all, not just the ones with null date changed
  public ArrayList<PatientStudyExtendedData> getPatientStudyExtendedDataByToken(String token) {
    String sql = patStudyExtSql + " and token = ?";
    ArrayList<PatientStudyExtendedData> studies = database.toSelect(sql)
        .argLong(siteInfo.getSiteId()).argString(token)
        .query(new RowsHandler<ArrayList<PatientStudyExtendedData>>() {
                 @Override
                 public ArrayList<PatientStudyExtendedData> process(Rows rs) throws Exception {
                   ArrayList<PatientStudyExtendedData> patientData = DataTableObjectConverter.convertToObjects(rs,
                       PatientStudyExtendedData.class);
                   return patientData;
                 }
               }
        );
    return studies;
  }

  public double getProgress(String token) {
    String sql = "select order_number, dt_changed from patient_study where survey_site_id = :site and token = ? order by order_number";

    return database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(token)
        .query(rs -> {
          int totalSteps = 0;
          int lastStep = 0;

          while (rs.next()) {
            int order = rs.getIntegerOrZero(1);
            Date changed = rs.getDateOrNull(2);
            totalSteps = order;
            if (changed != null) {
              lastStep = order;
            }
          }
          if (totalSteps != 0 && lastStep != 0) {
            return  ((double) lastStep / (double) totalSteps) * 100.00;
          }
          return 0.0;
        });
  }
}
