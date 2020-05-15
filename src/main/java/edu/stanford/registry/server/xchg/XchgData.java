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

package edu.stanford.registry.server.xchg;

/**
 * This class represents a single row in the ExportDefinitionFields.xlsx file.
 */
public class XchgData {

  public static final String DATASOURCE = "DataSource";
  public static final String FIELD = "Field";
  public static final String QUALIFIER = "Qualifier";
  public static final String FORMATTER = "Formatter";
  private String dataSource;
  private String field;
  private String qualifier, formatter;

  /**
   * Constructs a new ExportDefinitionFields object with the values provided.
   *
   * @param dataSource Value of the "DataSource" column.
   * @param field      Value of the "Field" column.
   * @param qualifierString  Value of the "Qualifier" column
   * @param formatterString  Value of the "Formatter" column
   */
  public XchgData(String dataSource, String field, String qualifierString, String formatterString) {
    this.dataSource = dataSource;
    this.field = field;
    this.qualifier = qualifierString;
    this.formatter = formatterString;
  }

  /**
   * Get the value of the "DataSource" column.
   */
  public String getDataSource() {
    return dataSource;
  }

  /**
   * Get the value of the "Field" column.
   */
  public String getField() {
    return field;
  }

  /**
   * Get the value of the "Qualifier" column.
   */
  public String getQualifierString() {
    return qualifier;
  }

  /**
   * Get the value of the "Formatter" column.
   */
  public String getFormatterString() {
    return formatter;
  }
}
