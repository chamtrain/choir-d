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

import edu.stanford.registry.server.export.ExportDefinitionType;
import edu.stanford.registry.server.utils.ExcelReader;
import edu.stanford.registry.server.utils.Spreadsheet;
import edu.stanford.registry.server.utils.XchgUtils;

import java.io.File;
import java.util.HashMap;

public class ImportDefinitionTypesXlxs {

  Spreadsheet sheet = null;

  public static final String FILENAME = "ImportDefinitionTypes.xlsx";
  private int typeColumn, classColumn, rowPointer;
  private HashMap<String, ImportDefinitionType> map = new HashMap<>();

  public ImportDefinitionTypesXlxs(File excelFile) throws Exception {

    sheet = ExcelReader.loadSpreadSheet(excelFile);
    typeColumn = sheet.getColumnIndex(ExportDefinitionType.TYPE);
    classColumn = sheet.getColumnIndex(ExportDefinitionType.CLASS);
    rowPointer = 0;
    for (int s = 0; s < sheet.size(); s++) {
      ImportDefinitionType defType = get(s);
      if (defType != null) {
        map.put(defType.getType(), defType);
      }
    }
  }

  public ImportDefinitionType get(int index) {
    if (sheet != null && sheet.size() > index) {
      String typeCol = XchgUtils.getContents(sheet.getRow(index).get(typeColumn));
      String classCol = XchgUtils.getContents(sheet.getRow(index).get(classColumn));
      return new ImportDefinitionType(typeCol, classCol);
    }
    return null;
  }

  public ImportDefinitionType get(String str) {
    return map.get(str);
  }

  public ImportDefinitionType getNext() {
    rowPointer++;
    return get(rowPointer - 1);
  }

  public void reset() {
    rowPointer = 0;
  }

}
