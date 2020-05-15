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

package edu.stanford.registry.server.database.objects;

import edu.stanford.registry.shared.DataTable;

import com.github.susom.database.Database;
import com.github.susom.database.SqlInsert;
import com.github.susom.database.SqlUpdate;

public abstract class TableStatement {

  /**
   * Build an insert statement for this object type
   */
  public SqlInsert getSqlInsert(Database database) {
    String stmt = getInsertStatement();
    SqlInsert sql = database.toInsert(stmt);
    return applyInsertStatementParameters(sql);
  }

  protected abstract String getInsertStatement();

  protected abstract SqlInsert applyInsertStatementParameters(SqlInsert insert);

  /**
   * Build an update statement to update all columns by the primary key
   */

  public SqlUpdate getSqlUpdate(Database database) {
    String stmt = getUpdateStatement();
    SqlUpdate sql = database.toUpdate(stmt);
    return applyUpdateStatementParameters(sql);
  }

  protected abstract String getUpdateStatement();

  protected abstract SqlUpdate applyUpdateStatementParameters(SqlUpdate update);

  /**
   * Build an update statement to delete a row by the primary key
   */
  
  public SqlUpdate getSqlDelete(Database database) {
    String stmt = getDeleteStatement();
    SqlUpdate sql = database.toUpdate(stmt);
    return applyDeleteStatementParameters(sql);
  }

  protected abstract String getDeleteStatement();

  protected abstract SqlUpdate applyDeleteStatementParameters(SqlUpdate update);

  /**
   * Get the select statement for a single row
   */
  public abstract String getSelectStatement(int whereClause);

  /**
   * Return the current shared object
   */
  public abstract DataTable getSharedObject();

  /**
   * Get the database table name
   */
  public abstract String getTableName();

}
