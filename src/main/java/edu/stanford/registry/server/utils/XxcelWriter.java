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
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XxcelWriter {

  private static Logger logger = Logger.getLogger(XxcelWriter.class);
  private XSSFSheet sheet;

  public XxcelWriter(XSSFSheet sheet) {
    this.sheet = sheet;
  }

  public static XSSFSheet makeSheet(XSSFWorkbook wb, String name) {
    return wb.createSheet(name);
  }

  public XSSFRow getRow(int row) {
    logger.debug("getRow(" + row + ")");
    if (sheet == null) {
      logger.error("getRow(sheet, row) called with null sheet");
      return null;
    }
    int lastRow = sheet.getLastRowNum();
    while (lastRow < row) {
      lastRow++;
      if (lastRow > 0) {
        logger.debug("Creating row " + lastRow);
        sheet.createRow(lastRow);
      }
    }
    return sheet.getRow(row);
  }

  public XSSFCell getCell(int row, int column) {
    // logger.error("getCell(" + row + "," + column + ")");
    if (sheet == null) {
      logger.error("getCell(sheet, row) called with null sheet");
      return null;
    }
    XSSFRow sheetRow = getRow(row);
    if (sheetRow == null) {
      logger.error("getRow(sheet, row) called with null sheetRow");
      return null;
    }
    int lastColumn = sheetRow.getLastCellNum() - 1;
    while (lastColumn < column) {
      lastColumn++;
      if (lastColumn >= 0) {
        logger.debug("getCell creating column " + lastColumn);
        sheetRow.createCell(lastColumn);
      }
    }
    logger.debug("getCell returning column " + column);
    return sheetRow.getCell(column);
  }

  public void writeColumn(int row, int column, String value) {
    if (sheet == null) {
      logger.error("writeColumn(sheet, row, column, value) called with null sheet");
    }

    XSSFCell cell = getCell(row, column);
    if (cell == null) {
      logger.error("writeColumn.getCell returned null");
      return;
    }
    cell.setCellValue(value);
    logger.debug("cell(" + row + "," + cell + " str value is " + cell.getStringCellValue());
  }

  public void writeColumn(int row, int column, int value) {
    XSSFCell cell = getCell(row, column);
    cell.setCellValue(value);
    logger.debug("cell(" + row + "," + cell + " int value is " + cell.getNumericCellValue());
  }

  public void writeColumn(int row, int column, Date value) {
    XSSFCell cell = getCell(row, column);
    cell.setCellValue(value);
    logger.debug("cell(" + row + "," + cell + " Date value is " + cell.getDateCellValue().toString());
  }

  public void writeColumn(int row, int column, Calendar value) {
    XSSFCell cell = getCell(row, column);
    cell.setCellValue(value);
    logger.debug("cell(" + row + "," + cell + " calendar value is " + cell.getDateCellValue().toString());
  }

  public void writeColumn(int row, int column, double value) {
    XSSFCell cell = getCell(row, column);
    cell.setCellValue(value);
    logger.debug("cell(" + row + "," + cell + " double value is " + cell.getNumericCellValue());
  }

}
