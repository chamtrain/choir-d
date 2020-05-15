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

import edu.stanford.registry.server.utils.ExcelReader;
import edu.stanford.registry.server.utils.Spreadsheet;
import edu.stanford.registry.server.utils.XchgUtils;
import edu.stanford.registry.server.xchg.XchgData;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class ImportDataXlsx {
  Spreadsheet sheet = null;
  private static Logger logger = Logger.getLogger(ImportDataXlsx.class);
  private static final String FILENAME = "ImportDefinitionFields.xlsx";
  private int dataSourceColumn, fieldColumn, qualifierColumn, formatterColumn;
  private int rowPointer;

  /**
   * Imports the default file definition
   *
   * @param path
   * @throws Exception
   */
  public ImportDataXlsx(String path) throws IOException {
    sheet = ExcelReader.loadSpreadSheet(path, FILENAME);
    initSheet();
  }

  /**
   * Imports a custom definition
   *
   * @param path
   * @param fileName
   * @throws Exception
   */
  public ImportDataXlsx(String path, String fileName) throws IOException {
    sheet = ExcelReader.loadSpreadSheet(path, fileName);
    initSheet();
  }

  public ImportDataXlsx(File file) throws IOException {
    logger.debug("ImportDataXlxs starting for " + file.getName());
    sheet = ExcelReader.loadSpreadSheet(file);
    initSheet();
  }

  private void initSheet() {
    if (sheet == null) {
      logger.debug("sheet is null!");
    }
    dataSourceColumn = sheet.getColumnIndex(XchgData.DATASOURCE);
    fieldColumn = sheet.getColumnIndex(XchgData.FIELD);
    qualifierColumn = sheet.getColumnIndex(XchgData.QUALIFIER);
    formatterColumn = sheet.getColumnIndex(XchgData.FORMATTER);
    rowPointer = 0;
  }

  public XchgData getNext() {
    if (rowPointer >= 0 && rowPointer < sheet.size()) {

      String dataSource = null;
      String field = null;
      if (dataSourceColumn >= 0 && dataSourceColumn <= sheet.getRow(rowPointer).size()) {
        dataSource = XchgUtils.getContents(sheet.getRow(rowPointer).get(dataSourceColumn));
      }
      if (fieldColumn >= 0 && fieldColumn <= sheet.getRow(rowPointer).size()) {
        field = XchgUtils.getContents(sheet.getRow(rowPointer).get(fieldColumn));
      }
      String qualifierString = null;
      if (qualifierColumn >= 0 && qualifierColumn <= sheet.getRow(rowPointer).size()) {
        qualifierString = XchgUtils.getContents(sheet.getRow(rowPointer).get(qualifierColumn));
      }
      String formatterString = null;
      if (formatterColumn >= 0 && formatterColumn <= sheet.getRow(rowPointer).size()) {
        formatterString = XchgUtils.getContents(sheet.getRow(rowPointer).get(formatterColumn));
      }
      rowPointer++;
      return new XchgData(dataSource, field, qualifierString, formatterString);
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

  public int getCurrentRow() {
    return rowPointer;
  }
}
