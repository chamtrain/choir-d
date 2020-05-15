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

package edu.stanford.registry.server.utils;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExcelWriter {

  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(ExcelWriter.class);

  public ExcelWriter() {

  }

  public static HSSFSheet makeSheet(HSSFWorkbook wb, String name) {

    return wb.createSheet(name);
  }

  public static HSSFRow getRow(HSSFSheet sheet, int row) {
    int lastRow = sheet.getLastRowNum();
    while (lastRow < row) {
      lastRow++;
      sheet.createRow(lastRow);
    }

    return sheet.getRow(row);
  }

  public static HSSFCell getCell(HSSFSheet sheet, int row, int column) {
    HSSFRow sheetRow = getRow(sheet, row);
    int lastColumn = sheetRow.getLastCellNum();
    while (lastColumn < column) {
      lastColumn++;
      sheetRow.createCell(lastColumn);
    }
    return sheetRow.getCell(column);
  }

  public static void writeColumn(HSSFSheet sheet, int row, int column, String value) {
    getCell(sheet, row, column).setCellValue(value);
  }

  public static void writeColumn(HSSFSheet sheet, int row, int column, int value) {
    getCell(sheet, row, column).setCellValue(value);
  }

  public static void writeColumn(HSSFSheet sheet, int row, int column, Date value) {
    getCell(sheet, row, column).setCellValue(value);
  }

  public static void writeColumn(HSSFSheet sheet, int row, int column, Calendar value) {
    getCell(sheet, row, column).setCellValue(value);
  }

  public static void writeColumn(HSSFSheet sheet, int row, int column, double value) {
    getCell(sheet, row, column).setCellValue(value);
  }

}
