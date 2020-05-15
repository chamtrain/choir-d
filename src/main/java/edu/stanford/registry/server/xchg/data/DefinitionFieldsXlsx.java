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

import edu.stanford.registry.server.export.ExportDefinitionField;
import edu.stanford.registry.server.utils.ExcelReader;
import edu.stanford.registry.server.utils.Spreadsheet;
import edu.stanford.registry.server.utils.XchgUtils;

public abstract class DefinitionFieldsXlsx {

  protected Spreadsheet sheet = null;
  protected int dataSourceColumn;
  protected int fieldColumn;
  protected int dataTypeColumn;
  protected int rowPointer;

  public void init(String path, String fileName) throws Exception {
    sheet = ExcelReader.loadSpreadSheet(path, fileName);
    dataSourceColumn = sheet.getColumnIndex(DefinitionField.DATASOURCE);
    fieldColumn = sheet.getColumnIndex(DefinitionField.FIELD);
    dataTypeColumn = sheet.getColumnIndex(DefinitionField.DATATYPE);
    rowPointer = 0;

  }

  public ExportDefinitionField getNext() {
    String dataSource = XchgUtils.getContents(sheet.getRow(rowPointer).get(dataSourceColumn));
    String field = XchgUtils.getContents(sheet.getRow(rowPointer).get(fieldColumn));
    String dataType = XchgUtils.getContents(sheet.getRow(rowPointer).get(dataTypeColumn));
    if (rowPointer >= 0 && rowPointer < sheet.size()) {
      rowPointer++;
      return new ExportDefinitionField(dataSource, field, dataType);
    }
    return null;
  }

  public void reset() {
    rowPointer = 0;
  }
}
