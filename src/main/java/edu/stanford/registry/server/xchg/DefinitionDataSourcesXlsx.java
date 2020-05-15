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

import edu.stanford.registry.server.utils.ExcelReader;
import edu.stanford.registry.server.utils.Spreadsheet;
import edu.stanford.registry.server.utils.XchgUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;

public class DefinitionDataSourcesXlsx {
  Spreadsheet sheet = null;

  private static Logger logger = Logger.getLogger(DefinitionDataSourcesXlsx.class);
  private int typeColumn, dataSourceColumn, classColumn, rowPointer;
  private HashMap<String, DefinitionDataSource> map = new HashMap<>();

  public DefinitionDataSourcesXlsx(String path, String fileName) throws Exception {
    logger.debug("Processing file: " + path + " " + fileName);
    sheet = ExcelReader.loadSpreadSheet(path, fileName);
    init();
  }

  public DefinitionDataSourcesXlsx(File fileIn) throws Exception {
    logger.debug("Processing file: " + fileIn.getAbsolutePath());
    sheet = ExcelReader.loadSpreadSheet(fileIn);
    init();
  }

  private void init() throws Exception {

    typeColumn = sheet.getColumnIndex(DefinitionDataSource.TYPE);
    dataSourceColumn = sheet.getColumnIndex(DefinitionDataSource.DATASOURCE);
    classColumn = sheet.getColumnIndex(DefinitionDataSource.CLASSNAME);
    rowPointer = 0;

    for (int s = 0; s < sheet.size(); s++) {
      DefinitionDataSource defType = get(s);
      if (defType != null) {
        map.put(defType.getType(), defType);
      }
    }
  }

  public DefinitionDataSource get(int index) {
    if (sheet != null && sheet.size() > index) {
      ArrayList<Cell> row = sheet.getRow(index);
      if (row != null) {
        String type = XchgUtils.getContents(sheet.getRow(index).get(typeColumn));
        String dataSource = XchgUtils.getContents(sheet.getRow(index).get(dataSourceColumn));
        String className = XchgUtils.getContents(sheet.getRow(index).get(classColumn));
        return new DefinitionDataSource(type, dataSource, className);
      }
    }
    return null;
  }

  public DefinitionDataSource get(String str) {
    return map.get(str);
  }

  public DefinitionDataSource getNext() {
    if (rowPointer >= 0 && rowPointer < sheet.size()) {
      rowPointer++;
      return get(rowPointer - 1);
    }
    return null;
  }

  public void reset() {
    rowPointer = 0;
  }

}
