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

package edu.stanford.registry.server.export;

import edu.stanford.registry.server.xchg.QualifierIntf;

public class ExportDefinition {
  private String field;
  private QualifierIntf<?> qualifier;
  private ExportFormatterIntf<?> formatter;
  private int order;

  public ExportDefinition() {

  }

  public ExportDefinition(String fieldName, QualifierIntf<?> qualifier,
                          ExportFormatterIntf<?> formatter, int orderNumber) {

    setField(fieldName);
    setQualifier(qualifier);
    setFormatter(formatter);
    setOrder(orderNumber);

  }

  public void setField(String fieldName) {
    this.field = fieldName;
  }

  public void setQualifier(QualifierIntf<?> exportQualifier) {
    this.qualifier = exportQualifier;
  }

  public void setFormatter(ExportFormatterIntf<?> formatr) {
    formatter = formatr;
  }

  public void setOrder(int orderNumber) {
    this.order = orderNumber;
  }

  public String getField() {
    return field;
  }

  public QualifierIntf<?> getQualifier() {
    return qualifier;
  }

  public ExportFormatterIntf<?> getFormatter() {
    return formatter;
  }

  public int getOrder() {
    return order;
  }

}
