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
import edu.stanford.registry.server.xchg.data.Constants;
import edu.stanford.registry.server.SiteInfo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;

public class XchgUtils {
  private static Logger logger = Logger.getLogger(XchgUtils.class);
  protected final SiteInfo siteInfo;

  public XchgUtils(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }


  public static String getContents(Cell cell) {
    if (cell == null) {
      return "";
    }

    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
      return "";
    }
    return cell.toString();
  }

  public static String DefaultImportTypeClassName(String typeName) {

    for (int inx = 0; inx < Constants.IMPORT_TYPES.length; inx++) {
      if (Constants.IMPORT_TYPES[inx].equals(typeName)) {
        return Constants.IMPORT_DEFAULT_OBJECTS_PACKAGE + Constants.IMPORT_TYPE_CLASSNAMES[inx];
      }
    }
    return null;
  }

  public static String DefaultImportDataSourceClassName(String dataSourceName) {
    for (int i = 0; i < Constants.IMPORT_DATASOURCES.length; i++) {
      if (Constants.IMPORT_DATASOURCES[i].equals(dataSourceName)) {
        return Constants.IMPORT_DEFAULT_OBJECTS_PACKAGE + Constants.IMPORT_DATASOURCE_CLASSNAMES[i];
      }
    }
    return null;
  }

  public static String DefaultXchgFormatterClassName(String formatter) {
    return Constants.XCHG_FORMATTER_PACKAGE + formatter;
  }

  public static String DefaultXchgQualifierClassName(String qualifier)  {
    return Constants.XCHG__QUALIFIER_PACKAGE + qualifier;
  }

  public static String TypeClassName(String typ) {
    for (int i = 0; i < Constants.EXPORT_TYPES.length; i++) {
      if (Constants.EXPORT_TYPES[i].equals(typ)) {
        return Constants.EXPORT_TYPE_CLASSNAMES[i];
      }
    }
    return null;
  }

  // Export Definition DataSources
  public static final String[] EXPORT_DATASOURCE_CLASSNAMES = { "SurveySystem" + "Study" + "Patient"
      + "PatientAttribute" + "PatientStudy" + "PatientAppointment" + "PatientScores" + "PatientDiagnosis"
      + "PatientMedication" + "PatientTreatment" };

  public static String ExportDataSourceClassName(String typ) {
    for (int i = 0; i < Constants.EXPORT_DATASOURCES.length; i++) {
      if (Constants.EXPORT_DATASOURCES[i].equals(typ)) {
        return EXPORT_DATASOURCE_CLASSNAMES[i];
      }
    }
    return null;
  }

  private String getImportDefFilePathOrNull(String subdir, String filename) {
    String s = siteInfo.getPathProperty("importDefinitionDirectory", null);
    if (s == null || s.isEmpty()) {
      return null;
    }

    if (!s.endsWith(File.separator)) {
      s += File.separator;
    }

    if (filename == null) {
      return s + subdir;
    } else {
      return s + subdir + File.separator + filename;
    }
  }

  private String getImportDefResourcePathOrDefault(String subdir, String filename) {
    final String IMPORT_RESOURCE_PROPERTY = "importDefinitionResource";
    String s = siteInfo.getPathProperty(IMPORT_RESOURCE_PROPERTY, Constants.IMPORT_DEFINITION_DIRECTORY_DEFAULT);

    if (!s.endsWith("/")) {  // resource paths always use slash, not File.separator
      s += "/";
    }

    if (filename == null) {
      return s + subdir;
    } else {
      return s + subdir + "/" + filename;
    }
  }

  public File getImportStandardDefinitionFile(String fileName) throws URISyntaxException {
    String importDefFilePath = getImportDefFilePathOrNull(Constants.DEFINITIONS_SUBDIRECTORY, fileName);
    if (importDefFilePath != null) {
      return new File(importDefFilePath);
    } else {
      String pathName = getImportDefResourcePathOrDefault(Constants.DEFINITIONS_SUBDIRECTORY, fileName);
      if (ServerUtils.getInstance().getClass().getClassLoader().getResource(pathName) != null) {
        URL url = ServerUtils.getInstance().getClass().getClassLoader().getResource(pathName);
        if (url != null && url.toURI() != null) {
          return new File(url.toURI());
        }
      }
      logger.error(siteInfo.getIdString()+"Definition file was not found for the resource named:" + pathName);
      return null;
    }
  }

  public ArrayList<String> getImportTypeDefinitionDirectoryFiles(Class<?> callingClass)
      throws URISyntaxException {

    // First load the system defaults
    String importDefDir = getImportDefResourcePathOrDefault(Constants.IMPORT_DESCRIPTION_SUBDIRECTORY, null);
    ArrayList<String> names = new ArrayList<>();
    java.net.URL dirURL = callingClass.getClassLoader().getResource(importDefDir);
    if (dirURL != null) {
      URI uri = dirURL.toURI();
      if (uri != null) {
        File dir = new File(dirURL.toURI());
        if (dir != null) {
          String[] fileList = dir.list();
          if (fileList != null) {
            for (String aFileList : fileList) {
              String fileName = getXlsxName(aFileList);
              if (fileName != null) {
                names.add(fileName);
              }
            }
          }
        }
      }
    }
    logger.debug(siteInfo.getIdString()+"Found " + names.size() + " import definitions in system default path: " + importDefDir);

    // Then get the user defined ones
    importDefDir = getImportDefFilePathOrNull(Constants.IMPORT_DESCRIPTION_SUBDIRECTORY, null);
    if (importDefDir != null) {
      File definitionFileDirectory = new File(importDefDir);
      if (definitionFileDirectory.exists()) {
        File[] definitions = definitionFileDirectory.listFiles();
        if (definitions != null && definitions.length > 0) {
          for (File definition : definitions) {
            String fileName = getXlsxName(definition.getName());
            if (fileName != null && !names.contains(fileName)) {
              names.add(fileName);
            }
          }
        }
      }
    }
    return names;
  }

  public File getImportTypeDefinitionFile(String definitionName) {
    String definitionFileName = definitionName + ".xlsx";
    String importDefFilePath = getImportDefFilePathOrNull(Constants.IMPORT_DESCRIPTION_SUBDIRECTORY, definitionFileName);
    if (importDefFilePath != null) {
      return new File(importDefFilePath);
    } else {
      try {
        String pathName = getImportDefResourcePathOrDefault(Constants.IMPORT_DESCRIPTION_SUBDIRECTORY, definitionFileName);
        URL url = ServerUtils.getInstance().getClass().getClassLoader().getResource(pathName);
        if (url.toURI() != null) {
          return new File(url.toURI());
        }
      } catch (URISyntaxException e) {
        logger.error(siteInfo.getIdString()+"Error loading resource for " + definitionName + " " + e.getMessage(), e);
      }
      return null;
    }
  }

  private static String getXlsxName(String fileName) {
    int inx = fileName.indexOf(".");
    if (inx > 0 && inx < fileName.length()) {
      String pre = fileName.substring(0, inx);
      String post = fileName.substring(inx + 1);
      if ("xlsx".equals(post)) {
        return pre;
      }
    }
    return null;
  }

  public File getImportFilesDirectory(String directory, boolean create) throws IOException {
    File inDir = new File(directory);
    if (!inDir.exists()) {
      if (create) {
        inDir.mkdirs();
      } else {
        throw new IOException(siteInfo.getIdString()+"Import directory listed in properties file for '" + directory + "' does not exist");
      }
    }
    return inDir;
  }

  public File getImportFilesDirectory(String directoryType, String fileType) throws IOException {
    File mainDir = getImportFilesDirectory(directoryType, true);
    File subDir = getImportFilesDirectory(mainDir.getPath() + File.separator + fileType, true);
    return subDir;
  }

  public File getImportFilesPendingDirectory(String fileType) throws IOException {
    String pendingDir = siteInfo.getPathProperty(Constants.IMPORT_FILES_PENDING, null);
    if (pendingDir == null || pendingDir.isEmpty()) {
      return null;
    }

    return getImportFilesDirectory(pendingDir, fileType);
  }

  public File getImportFilesProcessedDirectory(String fileType) throws IOException {
    String processedDir = siteInfo.getPathProperty(Constants.IMPORT_FILES_PROCESSED, null);
    if (processedDir == null || processedDir.isEmpty()) {
      return null;
    }
    return getImportFilesDirectory(processedDir, fileType);
  }
}
