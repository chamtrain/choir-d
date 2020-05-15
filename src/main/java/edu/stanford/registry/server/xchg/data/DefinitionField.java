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

package edu.stanford.registry.server.xchg.data;

/**
 * This class represents a single row in the ExportDefinitionFields.xlsx file.
 */
public class DefinitionField {

  public static final String DATASOURCE = "DataSource";
  public static final String FIELD = "Field";
  public static final String DATATYPE = "DataType";
  private String dataSource;
  private String field;
  private String dataType;

  /**
   * Constructs a new DefinitionFields object with the values provided.
   *
   * @param dataSource Value of the "DataSource" column.
   * @param field      Value of the "Field" column.
   * @param dataType   Value of the "DataType" column
   */
  public DefinitionField(String dataSource, String field, String dataType) {
    this.dataSource = dataSource;
    this.field = field;
    this.dataType = dataType;
  }

  /**
   * Get the value of the "DataSource" column.
   *
   * @return
   */
  public String getDataSource() {
    return dataSource;
  }

  /**
   * Get the value of the "Field" column.
   *
   * @return
   */
  public String getField() {
    return field;
  }

  /**
   * Get the value of the "DataType" column.
   *
   * @return
   */
  public String getDataType() {
    return dataType;
  }
}
