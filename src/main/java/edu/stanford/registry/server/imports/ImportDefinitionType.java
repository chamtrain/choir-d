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

package edu.stanford.registry.server.imports;

import edu.stanford.registry.server.xchg.data.Constants;

/**
 * This class represents a single row in the ExportDefinitionDataSources.xlsx
 * file.
 */
public class ImportDefinitionType {

  public static final String TYPE = "Type";
  public static final String CLASS = "Class";
  private String type;
  private String className;

  /**
   * Constructs a new ExportDefinitionType object.
   *
   * @param type
   * @param className
   */
  public ImportDefinitionType(String type, String className) {
    this.type = type;
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
   * Get the Class column value.
   *
   * @return String Name of the class.
   */
  public String getClassName() {
    return className;
  }

  public int getOrder() {
    int order;
    for (order = 0; order < Constants.EXPORT_TYPES.length; order++) {
      if (Constants.EXPORT_TYPES[order].equals(getType())) {
        return order;
      }
    }
    order++;
    return order;
  }

}
