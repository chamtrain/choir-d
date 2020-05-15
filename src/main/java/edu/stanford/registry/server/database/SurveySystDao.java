/*
 * Copyright 2016-2017 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.shared.DataTable;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.RowsHandler;

/**
 * Created by rstr on 12/29/2016.
 *
 * Note: This should never reference a SiteId.  That should be a SiteSurveySystDao which would inherit from this.
 */
public class SurveySystDao {
  private final Database database;

  private static final Logger logger = Logger.getLogger(SurveySystDao.class);

  private static final String SELECT_SURVEY_SYSTEM_SQL = "SELECT SURVEY_SYSTEM_ID, SURVEY_SYSTEM_NAME, META_VERSION, DT_CREATED, DT_CHANGED"
      + " FROM SURVEY_SYSTEM ";

  public SurveySystDao(Supplier<Database> db) {
    database = db.get();
    //this.siteId = siteId;
    if (database == null)
      throw new RuntimeException("Database is null");
  }

  public SurveySystDao(Database dbase) {
    database = dbase;
    if (database == null)
      throw new RuntimeException("Database is null");
  }

  /**
   * Convenience method to do the most common operation.
   * @param name Name of the survey system
   * @param debugLogger Pass a logger to get a debug message if a new row is created
   * @return the found or created survey system
   */
  public SurveySystem getOrCreateSurveySystem(String name, Logger debugLogger) {
    SurveySystem ss = getSurveySystem(name);
    if (ss == null) {
      if (debugLogger != null)
        debugLogger.debug("The SURVEY_SYSTEM table has no entry named " + name + ", creating it.");

      ss = insertSurveySystem(name);
    }
    return ss;
  }

  public SurveySystem getSurveySystem(String name) {

    return database.toSelect(
        SELECT_SURVEY_SYSTEM_SQL +  " WHERE SURVEY_SYSTEM_NAME = ? ")
        .argString(name).query(rs -> {
          if (rs.next()) {
            return DataTableObjectConverter.convertToObject(rs, SurveySystem.class);
          }
          return null;
        });
  }

  public SurveySystem getSurveySystem(Integer surveySystemId) {

    return database.toSelect(
        SELECT_SURVEY_SYSTEM_SQL + " WHERE SURVEY_SYSTEM_ID = ? ")
        .argInteger(surveySystemId).query(rs -> {
          if (rs.next()) {
            return DataTableObjectConverter.convertToObject(rs, SurveySystem.class);
          }
          return null;
        });
  }

  public ArrayList<DataTable> getSurveySystemsAsDataTables() {
    return database.toSelect(SELECT_SURVEY_SYSTEM_SQL).query(
        rs -> {
          ArrayList<DataTable> result = new ArrayList<>();
          while (rs.next()) {
            result.add(DataTableObjectConverter.convertToObject(rs, SurveySystem.class));
          }
          return result;
        });
  }

  public ArrayList<SurveySystem> getSurveySystems() {

    return database.toSelect(SELECT_SURVEY_SYSTEM_SQL).query(
        rs -> DataTableObjectConverter.convertToObjects(rs,
            SurveySystem.class));
  }

  public SurveySystem insertSurveySystem(String surveyName) {
    Long nextId = database.nextSequenceValue("SURVEY_SYSTEM_SEQ");
    SurveySystem ss = new SurveySystem();
    ss.setSurveySystemName(surveyName);
    ss.setSurveySystemId(nextId.intValue());
    ss.setMetaVersion(0);

    String stmt = "INSERT INTO SURVEY_SYSTEM (SURVEY_SYSTEM_ID, SURVEY_SYSTEM_NAME, META_VERSION, DT_CREATED, DT_CHANGED)"
        + " VALUES (?, ?, ?, :now, null) ";

    int rowCount = database.toInsert(stmt).argInteger(ss.getSurveySystemId())
        .argString(ss.getSurveySystemName())
        .argInteger(ss.getMetaVersion())
        .argDateNowPerDb(":now").insert();
    if (rowCount != 1) {
      logger.error("Something went wrong adding survey system " + ss.getSurveySystemName() + " add returned rowCount: " + rowCount);
    }
    return ss;
  }

  // ===========
  private final static String SELECT_STUDY_SQL =
      "SELECT SURVEY_SYSTEM_ID, STUDY_CODE, STUDY_DESCRIPTION, META_VERSION, DT_CREATED, DT_CHANGED, TITLE, EXPLANATION "
      + " FROM STUDY WHERE SURVEY_SYSTEM_ID = ? ";

  public Study getStudy(Integer surveySystemId, String studyName) {
    String sql =  SELECT_STUDY_SQL + " AND STUDY_DESCRIPTION = ? ";

    return database.toSelect(sql)
        .argInteger(surveySystemId).argString(studyName).query(rs -> DataTableObjectConverter.convertFirstRowToObject(rs, Study.class));
  }

  public Study getStudy(Integer surveySystemId, Integer studyCode) {
    String sql = SELECT_STUDY_SQL + " AND STUDY_CODE = ? ";

    return database.toSelect(sql)
        .argInteger(surveySystemId).argInteger(studyCode).query(rs -> DataTableObjectConverter.convertFirstRowToObject(rs, Study.class));
  }

  /**
   * @return The studies for a survey system
   */
  public ArrayList<Study> getStudies(Integer surveySystemId) {
    return database.toSelect(SELECT_STUDY_SQL)
        .argInteger(surveySystemId)
        .query(getStudyHandler());
  }

  private RowsHandler<ArrayList<Study>> getStudyHandler() {
    return rs -> DataTableObjectConverter.convertToObjects(rs, Study.class);
  }

  /**
   * @return all Study objects defined in the database
   */
  public ArrayList<Study> getStudies() {

    String sql = "SELECT survey_system_id, study_code, study_description, meta_version, dt_created, dt_changed, title, "
    + " explanation, replaced_by_code FROM study";
    return database.toSelect(sql).query(getStudyHandler());
  }

  /**
   * Gets a Hashmap of obsoleted studyCodes with the studyCode that replaced it
   *
   * @return HashMap Of Study codes replaced by a new study
   */
  public HashMap<Integer, Integer> getStudiesReplaced() {

    String sql = "SELECT study_code, replaced_by_code FROM study where replaced_by_code is not null";
    return  database.toSelect(sql).query(rs -> {
      HashMap<Integer, Integer> map = new HashMap<>();
      while (rs.next()) {
        map.put(rs.getIntegerOrZero(1), rs.getIntegerOrZero(2));
      }
      return map;
    });
  }


  public Study insertStudy(Study study) {
    study.setStudyCode(database.nextSequenceValue("study_code_seq").intValue());
    study.setMetaVersion(0);
    String INSERT_STUDY_SQL = "INSERT INTO STUDY (SURVEY_SYSTEM_ID, STUDY_CODE, STUDY_DESCRIPTION, "
        + "META_VERSION, DT_CREATED, DT_CHANGED, TITLE, EXPLANATION) VALUES (?, ?, ?, ?, :now, null, ?, ?)";

    int rowCount = database.toInsert(INSERT_STUDY_SQL)
        .argInteger(study.getSurveySystemId())
        .argInteger(study.getStudyCode())
        .argString(study.getStudyDescription())
        .argInteger(study.getMetaVersion())
        .argDateNowPerDb(":now")
        .argString(study.getTitle())
        .argString(study.getExplanation()).insert();
    if (rowCount != 1) {
      logger.error("Something went wrong adding study, add returned: " + rowCount);
    }
    return study;
  }

  public ArrayList<DataTable> getStudiesAsDataTables() {
    String sql = "SELECT SURVEY_SYSTEM_ID, STUDY_CODE, STUDY_DESCRIPTION, META_VERSION, DT_CREATED, DT_CHANGED, TITLE, EXPLANATION "
        + " FROM STUDY ";
    return database.toSelect(sql).query(rs -> {
      ArrayList<DataTable> result = new ArrayList<>();
      while (rs.next()) {
        result.add(DataTableObjectConverter.convertToObject(rs, Study.class));
      }
      return result;
    });
  }

}
