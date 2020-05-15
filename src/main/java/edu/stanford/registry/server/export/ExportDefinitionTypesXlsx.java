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

public class ExportDefinitionTypesXlsx {
  Spreadsheet sheet = null;

  private static final String FILENAME = "ExportDefinitionTypes.xlsx";
  private int typeColumn, classColumn, rowPointer;

  public ExportDefinitionTypesXlsx(String path) throws Exception {

    sheet = ExcelReader.loadSpreadSheet(path, FILENAME);
    typeColumn = sheet.getColumnIndex(ExportDefinitionType.TYPE);
    classColumn = sheet.getColumnIndex(ExportDefinitionType.CLASS);
    rowPointer = 0;
  }

  public ExportDefinitionType getNext() {
    String typeCol = XchgUtils.getContents(sheet.getRow(rowPointer).get(typeColumn));
    String classCol = XchgUtils.getContents(sheet.getRow(rowPointer).get(classColumn));
    if (rowPointer >= 0 && rowPointer < sheet.size()) {
      rowPointer++;
      return new ExportDefinitionType(typeCol, classCol);
    }
    return null;
  }

  public void reset() {
    rowPointer = 0;
  }

}
