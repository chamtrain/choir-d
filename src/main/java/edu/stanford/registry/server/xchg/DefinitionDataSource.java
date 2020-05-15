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

import edu.stanford.registry.server.xchg.data.Constants;

/**
 * This class represents a single row in the ExportDefinitionDataSources.xlsx
 * file.
 */
public class DefinitionDataSource {

  public static final String TYPE = "Type";
  public static final String DATASOURCE = "DataSource";
  public static final String CLASSNAME = "Class";

  private String type;
  private String dataSource;
  private String className;

  /**
   * Constructs a new ExportDefinitionDataSource object with the values
   * provided.
   *
   * @param type       The value of the "Type" column.
   * @param dataSource The value of the "DataSource" column.
   * @param className  The value of the "Class" column.
   */
  public DefinitionDataSource(String type, String dataSource, String className) {

    this.type = type;
    this.dataSource = dataSource;
    this.className = className;
  }

  /**
   * Get the Type column value.
   *
   * @return String Type.
   */
  public String getType() {
    return type;
  }

  /**
   * Get the DataSource column value.
   *
   * @return String DataSource.
   */
  public String getDataSource() {
    return dataSource;
  }

  /**
   * Get the Class column value.
   *
   * @return String Name of the class.
   */
  public String getClassName() {
    return className;
  }

  public int getExportOrder() {
    int order;
    for (order = 0; order < Constants.EXPORT_DATASOURCES.length; order++) {
      if (Constants.EXPORT_DATASOURCES[order].equals(getDataSource())) {
        return order;
      }
    }
    order++;
    return order;
  }

  public int getImportOrder() {
    int order;
    for (order = 0; order < Constants.IMPORT_DATASOURCES.length; order++) {
      if (Constants.IMPORT_DATASOURCES[order].equals(getDataSource())) {
        return order;
      }
    }
    order++;
    return order;
  }
}
