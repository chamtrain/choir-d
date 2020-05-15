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

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.server.SiteInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XxcelReader {
  private static Logger logger = Logger.getLogger(XxcelReader.class);

  final SiteInfo siteInfo;
  public XxcelReader(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  public static Spreadsheet loadSpreadSheet(String path, String fileName) throws IOException {

    File dataFile = new File(path, fileName);
    return loadSpreadSheet(dataFile);
  }

  public static Spreadsheet loadSpreadSheet(String fullyQualifiedFileName) throws IOException {

    File dataFile = new File(fullyQualifiedFileName);
    return loadSpreadSheet(dataFile);
  }

  public static Spreadsheet loadSpreadSheet(File fileIn) throws IOException {

    if (fileIn == null) {
      logger.error("loadSpreadSheet called with null file");
      return null;
    }

    if (!fileIn.exists()) {
      logger.error("cannot load spreadsheet, file " + fileIn.getAbsolutePath() + " does not exist");
      return null;
    }

    FileInputStream fis = null;
    Spreadsheet returnSheet = null;

    try {
      fis = new FileInputStream(fileIn);
      XSSFWorkbook workbook = new XSSFWorkbook(fis);

      XSSFSheet sheet = workbook.getSheetAt(0);

      Iterator<Row> rows = sheet.rowIterator();
      XSSFRow row = (XSSFRow) rows.next();

      Iterator<Cell> cells = row.cellIterator();

      ArrayList<Cell> header = new ArrayList<>();
      while (cells.hasNext()) {
        XSSFCell cell = (XSSFCell) cells.next();
        header.add(cell);
      }

      showRow(header);
      returnSheet = new Spreadsheet(header);

      // read the next line and update the patient
      while (rows.hasNext()) {

        row = (XSSFRow) rows.next();
        cells = row.cellIterator();
        ArrayList<Cell> data = new ArrayList<>();

        for (int j = 0; j < header.size(); j++) {
          XSSFCell cell = row.getCell((short) j);
          data.add(cell);
        }
        showRow(data);
        returnSheet.addRow(data);
      } // while

      workbook.close();
    } catch (IOException e) {
      logger.error("Error loading spreadsheet for file " + fileIn.getAbsolutePath(), e);
      throw e;
    } finally {
      try {
        if (fis != null) {
          fis.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
    return returnSheet;
  }

  public static Spreadsheet loadSpreadSheetXls(File fileIn) {
    FileInputStream fis = null;
    Spreadsheet returnSheet = null;

    try {
      fis = new FileInputStream(fileIn);
      XSSFWorkbook workbook = new XSSFWorkbook(fis);

      XSSFSheet sheet = workbook.getSheetAt(0);

      Iterator<Row> rows = sheet.rowIterator();
      Row row = rows.next();

      Iterator<Cell> cells = row.cellIterator();

      ArrayList<Cell> header = new ArrayList<>();

      while (cells.hasNext()) {
        XSSFCell cell = (XSSFCell) cells.next();
        header.add(cell);
      }

      showRow(header);
      returnSheet = new Spreadsheet(header);

      // read the next line and add it to the sheet
      while (rows.hasNext()) {

        row = rows.next();
        cells = row.cellIterator();
        ArrayList<Cell> data = new ArrayList<>();

        for (int j = 0; j < header.size(); j++) {
          Cell cell = row.getCell((short) j);
          data.add(cell);
        }

        showRow(data);
        returnSheet.addRow(data);

        // deal with the data line
        // Map<String, String> currentLine = readNextPatientLine(header, data);

        // find person
        // String mrn = (currentLine.get("MRN")).trim(); //store mrn for
        // reporting
        // String lastname = (currentLine.get("Last Name")).trim();

        // find the one with the right date
        // Date sampleDate = df2.parse(currentLine.get("Sample Date"));

      } // while

      workbook.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (fis != null) {
          fis.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
    return returnSheet;
  }
/*
  protected Map<String, String> readNextPatientLine(ArrayList<Cell> header, ArrayList<Cell> data) throws Exception {
    Map<String, String> thisLine = new HashMap<>();
    String token = null;
    String headerString = null;

    for (int i = 0; i < header.size(); i++) {
      XSSFCell headercell = (XSSFCell) header.get(i);
      headerString = headercell.getStringCellValue();
      token = null;

      if (i < data.size()) {
        XSSFCell cell = (XSSFCell) data.get(i);

        if (cell != null) {
          if (cell.getCellType() == 0) { // 0 is numeric format
            token = new BigDecimal(cell.getNumericCellValue()).toPlainString();
          } else {
            token = cell.getStringCellValue();
          }
        }
      }
      thisLine.put(headerString, token);
    }
    return thisLine;
  }
*/
  private static void showRow(List<Cell> row) {
    StringBuilder printRow = new StringBuilder("ROW: ");
    for (int j = 0; j < row.size(); j++) {
      Cell cell = row.get(j);
      if (cell == null) {
        printRow.append(", ");
      } else {
        try {
          if (cell.getCellType() == 0) { // 0 is numeric format
            printRow.append(new BigDecimal(cell.getNumericCellValue()).toPlainString());
          } else {
            printRow.append(cell.getStringCellValue());
          }

          if (j < row.size() - 1) {
            printRow.append(", ");
          }

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    logger.debug(printRow.toString());
  }

  public String getDefaultTemplateDirectory() {
    return Constants.XL_TEMPLATE_DIRECTORY_DEFAULT;
  }

  private File getCustomTemplateDirectory() {
    String path = siteInfo.getPathProperty(Constants.XL_TEMPLATE_DIRECTORY, null);
    if (path != null) {
      File customDir = ServerUtils.getInstance().getDirectory(path);
      if (customDir.exists()) {
        return customDir;
      }
    }
    return null;
  }

  private XSSFWorkbook getDefaultTemplate(String name) throws IOException {

    logger.debug("Getting defaultTemplate  " + getDefaultTemplateDirectory() + name);
    InputStream istream = getClass().getClassLoader().getResourceAsStream(getDefaultTemplateDirectory() + name);
    if (istream == null) {
      return null;
    }
    XSSFWorkbook wb = new XSSFWorkbook(istream);
    istream.close();
    return wb;
  }

  private XSSFWorkbook getCustomTemplate(String name) throws IOException {
    File customTemplate = new File(getCustomTemplateDirectory(), name);
    if (!customTemplate.exists()) {
      return null;
    }
    return new XSSFWorkbook(new FileInputStream(customTemplate.getAbsolutePath()));
  }

  public XSSFWorkbook getTemplate(String name) throws IOException {
    XSSFWorkbook template = getCustomTemplate(name);
    if (template != null) {
      return template;
    }
    return getDefaultTemplate(name);
  }

}
