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

package edu.stanford.registry.server;

import java.util.function.Supplier;

import com.github.susom.database.Database;

/**
 * Data access object for registry-related database tables.
 *
 * @author garricko
 */
public class RegistryDao {
  private Supplier<Database> db;

  public RegistryDao(Supplier<Database> db) {
    this.db = db;
  }

  /**
   * Add a new survey system and return it's generated id.
   */
  public Long addSurveySystem(String surveySystemName) {
    return db.get().toInsert(
        "insert into survey_system (survey_system_id, survey_system_name, meta_version, dt_created, "
            + "dt_changed) values (:system_id, :system_name, 0, :dt_created, null)")
        .argPkSeq(":system_id", "survey_system_seq")
        .argString(":system_name", surveySystemName)
        .argDateNowPerDb(":dt_created").insertReturningPkSeq("survey_system_id");
  }

  /**
   * Add a new study and return it's generated id.
   *
   * @param surveySystemId foreign key to survey_system
   * @param name study_description, and internal name for this study
   * @param title a display name for this study
   */
  public Long addStudy(Long surveySystemId, String name, String title) {
    return db.get().toInsert("insert into study (survey_system_id, study_code, study_description, meta_version, "
        + "dt_created, dt_changed, title) values (:system_id, :study_code, :name, 0, :dt_created, null, :title)")
        .argLong(":system_id", surveySystemId)
        .argPkSeq(":study_code", "study_code_seq")
        .argString(":name", name)
        .argDateNowPerDb(":dt_created")
        .argString(":title", title).insertReturningPkSeq("study_code");
  }

  public void addPatientResultType(Long typeId, Long siteId, String name, String title) {
    // TABLEREF patient_result_type
    db.get().toInsert("insert into patient_result_type (patient_res_typ_id, survey_site_id, result_name, result_title) "
        + "values (?,?,?,?)").argLong(typeId).argLong(siteId).argString(name).argString(title).insert(1);
  }
}
