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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.imports.data.ImportTypeComparator;
import edu.stanford.registry.server.imports.data.ImportTypeManagerIntf;
import edu.stanford.registry.server.xchg.DefinitionDataSource;
import edu.stanford.registry.server.xchg.FormatterIntf;
import edu.stanford.registry.server.xchg.ProcessError;
import edu.stanford.registry.server.xchg.ProcessResults;
import edu.stanford.registry.server.xchg.QualifierIntf;
import edu.stanford.registry.server.xchg.XchgData;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

public class ImportDataProcessor {

  private static Logger logger = Logger.getLogger(ImportDataProcessor.class);

  private ImportResources resources;
  SiteInfo siteInfo;

  public ImportDataProcessor(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  /**
   * This contains the types used in this import.
   */
  private ArrayList<ImportDefinitionType> myTypesList = new ArrayList<>();

  /**
   * This map contains the queue of fields in the import, keyed by data source name.
   */
  HashMap<String, ImportDefinitionQueue> queuesMap = new HashMap<>();
  /**
   * These are the data type managers needed for each line
   */
  ImportTypeManagerIntf[] managers;

  /**
   * This handles an .xlsx definitions file and .csv data file
   */
  public void doImport(Database database, File definitionFile, File dataFile) throws IOException {
    /**
     * Load the definition file for this import
     */
    String fileName = definitionFile.getAbsolutePath();
    logger.debug("doImport called for definitions file: " + fileName + " and data file: " + dataFile.getAbsolutePath());
    ImportDataXlsx importDataSheet = new ImportDataXlsx(definitionFile);

    Reader dataReader = new java.io.InputStreamReader(new java.io.FileInputStream(dataFile), StandardCharsets.UTF_8);
    CSVReader reader;
    try {
      resources = new ImportResources(siteInfo);

      logger.debug("importDataSheet has " + importDataSheet.size() + " rows");
      processImportDefinitions(importDataSheet);
      createTypeManagers(database);

      /*
       * open the csv file and load the data
       * 
       * A comma separated file has the fields quoted with ". A tab delimited file
       * does not support quoted fields.
       */
      boolean isTabSeparated = dataFile.getName().endsWith(".tsv") || dataFile.getName().endsWith(".txt");
      char separator = isTabSeparated ? '\t' : ',';
      char quote = isTabSeparated ? '\u0000' : '"';
      reader = new CSVReader(dataReader, separator, quote,
          CSVParser.DEFAULT_ESCAPE_CHARACTER, 1, CSVParser.DEFAULT_STRICT_QUOTES, true);

      // parse the file and process each line
      int lineNumber = 0;
      ProcessResults results = new ProcessResults();
      String[] lineValues;
      while ((lineValues = reader.readNext()) != null) {
        lineNumber++;
        if (lineNumber >= 1) {

          /**
           * Removed leading and trailing blanks
           */
          for (int l = 0; l < lineValues.length; l++) {
            lineValues[l] = trimValue(lineValues[l]);
          }

          /**
           * Process the line through each type of data manager
           */
          ProcessError pErr = null;
          String managerType = null;
          try {
            for (int m = 0; m < managers.length; m++) {
              if (managers[m] != null && pErr == null) {
                managerType = myTypesList.get(m).getType();
                managers[m].importData(lineValues);
              }
            }
            results.incrementNumberSucceeded();
          } catch (Exception e) {
            pErr = new ProcessError(lineNumber, managerType + " failed: " + e.getMessage());
            logger.error("line " + lineNumber + " " + managerType + " failed:" + e.getMessage(), e);
            results.incrementNumberFailed();
            results.addError(pErr);
          }
        }
      }

      // Notify each type of data manager of the end of the input data
      for (int m = 0; m < managers.length; m++) {
        if (managers[m] != null) {
          try {
            managers[m].importDataEnd();
          } catch (Exception e) {
            String managerType = myTypesList.get(m).getType();
            ProcessError pErr = new ProcessError(lineNumber, managerType + " failed: " + e.getMessage());
            logger.error(managerType + " failed: " + e.getLocalizedMessage(), e);
            results.incrementNumberFailed();
            results.addError(pErr);
          }
        }
      }

      /**
       * Write the results to the log
       */
      StringBuilder resultString = new StringBuilder("Import file: " + dataFile.getAbsolutePath() + " completed\n");
      resultString.append(results.getNumberSucceeded()).append(" lines imported successfully \n");
      resultString.append(results.getNumberFailed()).append(" failed: \n");
      ArrayList<ProcessError> errors = results.getErrors();
      if (errors != null && errors.size() > 0) {
        for (ProcessError error : errors) {
          resultString.append(error.getLineNumber()).append(":").append(error.getError()).append("\n");
        }
      }
      logger.info(resultString.toString());
    } catch (Exception e) {
      logger.error("Error occurred in import ", e);
    } finally {
      if (dataReader != null) {
        try {
          dataReader.close();
        } catch (IOException e) {
          logger.error("Error closing file " + e.getMessage(), e);
        }
      }
    }

  }

  private void processImportDefinitions(ImportDataXlsx importDataSheet) {
    // Build the ImportDefinitionQueues && list of managers needed for this
    // import

    for (int row = 0; row < importDataSheet.size(); row++) {
      XchgData xData = importDataSheet.getNext();

      String dataSourceName = xData.getDataSource();
      if (!ServerUtils.isEmpty(dataSourceName)) {
        dataSourceName = dataSourceName.trim(); // remove leading or trailing blanks
        DefinitionDataSource dds = resources.getDefinitionDataSource(dataSourceName);
        ImportDefinitionQueue queue = queuesMap.get(dataSourceName);
        if (queue == null) {
          queue = new ImportDefinitionQueue(dataSourceName);
          logger.debug("handling datasource: " + dataSourceName);
          ImportDefinitionType type = resources.getDefinitionType(dds.getType());

          // make sure we've included its type
          if (type != null && !myTypesList.contains(resources.getDefinitionType(dds.getType()))) {
            myTypesList.add(type);
          }

          // temp
          if (type == null) {
            logger.debug("TYPE NOT FOUND!");
          }

        }
        QualifierIntf<?> qualifier = ImportFactory.getFactory().getQualifier(siteInfo, xData.getQualifierString());
        FormatterIntf<?> formatter = ImportFactory.getFactory().getFormatter(siteInfo, dataSourceName, xData.getFormatterString());
        ImportDefinition definition = new ImportDefinition(xData.getField(), qualifier, formatter, row);
        if (!ServerUtils.isEmpty(dataSourceName)) {
          queue.add(definition);
          queuesMap.put(dataSourceName, queue);
        }
      }
    }
  }

  private void createTypeManagers(Database database) {
    // Get an import manager for each type of data we are importing.
    managers = new ImportTypeManagerIntf[myTypesList.size()];

    ImportFactory factory = ImportFactory.getFactory();
    Collections.sort(myTypesList, new ImportTypeComparator<ImportDefinitionType>());
    for (int t = 0; t < myTypesList.size(); t++) {
      managers[t] = factory.getTypeManager(siteInfo, myTypesList.get(t), resources, database, queuesMap);
    }
    logger.debug(myTypesList.size() + " types managers will be used. ");
  }

  private String trimValue(String value) {
    if (value == null) {
      value = "";
    }
    return value.trim();
  }

}
