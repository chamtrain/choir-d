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

package edu.ufl.registry.server.database.objects;

import edu.stanford.registry.server.database.impl.DatabaseSyntaxIntf;
import edu.stanford.registry.server.database.objects.TableStatement;
import edu.stanford.registry.shared.DataTable;
import edu.ufl.registry.shared.EpicLog;

public class EpicLogTable implements TableStatement<EpicLog> {
    public static int SELECT_ALL                         = 0;
    public static int SELECT_BY_REGISTRATIONID           = 1;
    public static int SELECT_BY_PATIENTID                = 2;
    public static int SELECT_BY_DTCREATED                = 3;
    public static int SELECT_BY_ID = 4;

    private String[] WHERE_CLAUSE = {
            " ",
            " WHERE SURVEY_REG_ID = ? ",
            " WHERE PATIENT_ID = ? ",
            " WHERE DT_CREATED = ? ",
            " WHERE ID = ? " };

    private EpicLog            epicLog;
    private DatabaseSyntaxIntf db;

    public EpicLogTable( DatabaseSyntaxIntf syntax, EpicLog epicLog ) {
        this.epicLog = epicLog;
        this.db = syntax;
    }

    @Override
    public String getSelectStatement( int selectBy ) {
        if ( selectBy < WHERE_CLAUSE.length ) {

            return
                    "SELECT ID, SURVEY_REG_ID, PATIENT_ID, SUCCESS, MESSAGE, META_VERSION, DT_CREATED, DT_CHANGED, OUTGOING"
                    + " FROM EPIC_LOG " + WHERE_CLAUSE[selectBy];
        }
        return null;
    }

    @Override
    public String getInsertStatement() {
        return
                "INSERT INTO EPIC_LOG (ID, SURVEY_REG_ID, PATIENT_ID, SUCCESS, MESSAGE, META_VERSION, DT_CREATED,DT_CHANGED,OUTGOING)"
                + " VALUES (EPIC_LOG_SEQ.NEXTVAL,?, ?, ?, ?, ?,CURRENT_TIMESTAMP,null, ?)";
    }

    @Override
    public Object[] getInsertStatementParameters() {
        Object[] objs = { epicLog.getSurveyRegistrationId(), epicLog.getPatientId(), epicLog.getSuccess(), epicLog.getMessage(),
                          epicLog.getMetaVersion(), epicLog.getOutgoing() };
        return objs;
    }

    public String getUpdateStatement() {
        return "UPDATE EPIC_LOG set SUCCESS =?, MESSAGE =?, META_VERSION =?, DT_CHANGED = CURRENT_TIMESTAMP"
        + " WHERE ID =?";
  }

  public Object[] getUpdateStatementParameters() {
    Object[] objs = { epicLog.getSuccess(), epicLog.getMessage(), epicLog.getMetaVersion(),
                      epicLog.getId() };

    return objs;
  }

  /**
   * Get the sql statement to get a row by its primary key
   */
  public String getSelectByPrimaryKey() {
    return getSelectStatement(SELECT_BY_ID);
  }

  /**
   * Gets the sql statement to delete this row by its primary key.
   *
   * @return prepared statment sql.
   */
  public String getDeleteStatement() {
    return "DELETE FROM EPIC_LOG WHERE ID = ? ";
  }

  /**
   * Gets the parameters for deleting this row.
   *
   * @return Array of parameters.
   */
  public Object[] getDeleteStatementParameters() {
    return getPrimaryKey();
  }

  /**
   * Gets the primary key elements needed to select by primary key
   */
  public Object[] getPrimaryKey() {
      Object[] objs = { epicLog.getId() };
      return objs;
  }

  @Override
  public DataTable<EpicLog> getSharedObject() {
    return epicLog;
  }

  public String getTableName() {
    return "EPIC_LOG";
  }

}
