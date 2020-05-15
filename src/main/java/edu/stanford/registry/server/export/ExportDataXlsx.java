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

import edu.stanford.registry.server.utils.ExcelReader;
import edu.stanford.registry.server.utils.Spreadsheet;
import edu.stanford.registry.server.utils.XchgUtils;
import edu.stanford.registry.server.xchg.XchgData;

public class ExportDataXlsx {
  Spreadsheet sheet = null;

  private static final String FILENAME = "ExportDefinitionFields.xlsx";
  private int dataSourceColumn, fieldColumn, qualifierColumn, formatterColumn;
  private int rowPointer;

  public ExportDataXlsx(String path) throws Exception {

    sheet = ExcelReader.loadSpreadSheet(path, FILENAME);
    initSheet();
  }

  public ExportDataXlsx(String path, String fileName) throws Exception {
    sheet = ExcelReader.loadSpreadSheet(path, fileName);
    initSheet();

  }

  private void initSheet() {

    dataSourceColumn = sheet.getColumnIndex(XchgData.DATASOURCE);
    fieldColumn = sheet.getColumnIndex(XchgData.FIELD);
    qualifierColumn = sheet.getColumnIndex(XchgData.QUALIFIER);
    formatterColumn = sheet.getColumnIndex(XchgData.FORMATTER);
    rowPointer = 0;
  }

  public XchgData getNext() {
    String dataSource = XchgUtils.getContents(sheet.getRow(rowPointer).get(dataSourceColumn));
    String field = XchgUtils.getContents(sheet.getRow(rowPointer).get(fieldColumn));
    String qualifier = XchgUtils.getContents(sheet.getRow(rowPointer).get(qualifierColumn));
    String formatter = XchgUtils.getContents(sheet.getRow(rowPointer).get(formatterColumn));
    if (rowPointer >= 0 && rowPointer < sheet.size()) {
      rowPointer++;
      return new XchgData(dataSource, field, qualifier, formatter);
    }
    return null;
  }

  public void reset() {
    rowPointer = 0;
  }

  public int size() {
    if (sheet != null) {
      return sheet.size();
    }
    return -1;
  }
}
