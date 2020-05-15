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

import edu.stanford.registry.server.xchg.FormatterIntf;
import edu.stanford.registry.server.xchg.QualifierIntf;

public class ImportDefinition {
  private String field;
  private QualifierIntf<?> qualifier;
  private FormatterIntf<?> formatter;
  // private int order;
  private int column;

  public ImportDefinition() {

  }

  public ImportDefinition(String fieldName, QualifierIntf<?> qualifier, FormatterIntf<?> formatter,
                          // int orderNumber
                          int column) {

    setField(fieldName);
    setQualifier(qualifier);
    setFormatter(formatter);
    setColumn(column);

  }

  public void setField(String fieldName) {
    this.field = fieldName;
  }

  public void setQualifier(QualifierIntf<?> exportQualifier) {
    this.qualifier = exportQualifier;
  }

  public void setFormatter(FormatterIntf<?> formatr) {
    formatter = formatr;
  }

  public void setColumn(int column) {
    this.column = column;
  }

  public String getField() {
    return field;
  }

  public QualifierIntf<?> getQualifier() {
    return qualifier;
  }

  public FormatterIntf<?> getFormatter() {
    return formatter;
  }

  public int getColumn() {
    return column;
  }

}
