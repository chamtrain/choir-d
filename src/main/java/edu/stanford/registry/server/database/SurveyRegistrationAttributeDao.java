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

package edu.stanford.registry.server.database;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * Survey registration attribute data access object.
 * 
 * This class provides access to the survey registration attributes. Survey
 * registration attributes are name-value pairs which are associated with
 * a survey registration.
 */
public class SurveyRegistrationAttributeDao {

  private Database database;
  
  public SurveyRegistrationAttributeDao(Database database) {
    this.database = database;
  }

  /**
   * Retrieve the survey registration attributes for a survey registration
   * as a map of attribute name to value.
   */
  public Map<String,String> getAttributes(Long surveyRegId) {
    String sql = "select data_name, data_value from survey_reg_attr where survey_reg_id = ?";
    Map<String,String> attrs = database.toSelect(sql)
        .argLong(surveyRegId)
        .query(new RowsHandler<Map<String,String>>() {
          @Override
          public Map<String, String> process(Rows rows) throws Exception {
            Map<String,String> attrs = new HashMap<>();
            while(rows.next()) {
              String name = rows.getStringOrEmpty("data_name");
              String value = rows.getStringOrNull("data_value");
              attrs.put(name, value);
            }
            return attrs;
          }
        });
    return attrs;
  }

  /**
   * Set the value of a survey registration attribute. Specifying a null
   * value for the attribute value will remove that attribute.
   */
  public void setAttribute(Long surveyRegId, String name, String newValue) {
    String sql;

    @SuppressWarnings("unused") // needed because calls that set it are tagged @CheckReturnValue
    int n;

    // Get the current attribute value. If unchanged don't do anything
    sql = "select data_value from survey_reg_attr where survey_reg_id = ? and data_name = ?";
    String currentValue = database.toSelect(sql)
        .argLong(surveyRegId)
        .argString(name)
        .queryStringOrNull();
    if (((currentValue == null) && (newValue == null)) ||
        ((currentValue != null) && currentValue.equals(newValue))) {
      return;
    }

    if (newValue == null) {
      // Delete the existing attribute
      sql = "delete from survey_reg_attr where survey_reg_id = ? and data_name = ?";
      n = database.toDelete(sql)
          .argLong(surveyRegId)
          .argString(name)
          .update();
    } else if (currentValue == null) {
      // Insert a new attribute
      sql = "insert into survey_reg_attr (survey_reg_id, data_name, data_value) values (?, ?, ?)";
      n = database.toInsert(sql)
          .argLong(surveyRegId)
          .argString(name)
          .argString(newValue)
          .insert();
    } else {
      // Update the existing attribute
      sql = "update survey_reg_attr set data_value = ? where survey_reg_id = ? and data_name = ?";
      n = database.toUpdate(sql)
          .argString(newValue)
          .argLong(surveyRegId)
          .argString(name)
          .update();
    }

    // Write the change to the attribute history table
    sql = "insert into survey_reg_attr_hist (survey_reg_attr_hist_id, change_time, survey_reg_id, data_name, data_value) values(?, ?, ?, ?, ?)";
    n = database.toInsert(sql)
        .argPkSeq("survey_reg_attr_hist_seq")
        .argDate(new Date())
        .argLong(surveyRegId)
        .argString(name)
        .argString(newValue)
        .insert();
  }

  /**
   * Delete all of the survey registration attributes for a survey registration.
   */
  public void deleteAttributes(Long surveyRegId) {
    String sql = "delete from survey_reg_attr where survey_reg_id = ?";

    @SuppressWarnings("unused") // needed because the is tagged @CheckReturnValue
    int n = database.toDelete(sql)
        .argLong(surveyRegId)
        .update();    
  }
}
