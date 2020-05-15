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

package edu.stanford.registry.server.xchg.data;

public class Constants {
  public static final String PROTOCOL = "http";
  public static final String HOST = "localhost";
  public static final int PORT = 80;
  public static final String DATE_FORMAT = "MM/dd/yyyy";
  public static final String TIME_FORMAT = "HH:mm";
  public static final String DATE_TIME_FORMAT = "MM/dd/yyyy";
  public static final String DEFINITIONS_SUBDIRECTORY = "data_definitions";
  public static final String IMPORT_DESCRIPTION_SUBDIRECTORY = "import_types";
  public static final String IMPORT_FILES_PENDING = "importPendingFileDirectory";
  public static final String IMPORT_FILES_PROCESSED = "importProcessedFileDirectory";
  // Exports
  public static final String EXPORT_PROPERTIES_FILE = "exportRegistry.properties";
  public static final String EXPORT_DEFAULT_OBJECTS_PACXAGE = "edu.stanford.registry.server.export.";
  public static final String[] EXPORT_DATASOURCES = { "SurveySystem", "Study", "Profile", "Attribute", "PatientStudy",
    "Appointment", "StudyResult", "Diagnosis", "Medication", "Treatment" };

  // Export Definition files
  public static final String EXPORT_DEFINITION_FIELDS_FILENAME = "ExportDefinitionFields.xlsx";
  public static final String EXPORT_DEFINITION_DATA_SOURCES_FILENAME = "ExportDefinitionDatasources.xlsx";
  public static final String EXPORT_REGISTRY_DEFINITION_FILE = "exportRegistryDefinition.xls";

  // Export Definition Types
  public static final String[] EXPORT_TYPES = { "Study", "Patient" };
  public static final String[] EXPORT_TYPE_CLASSNAMES = { "ExportPatientType", "ExportStudyType" };

  public static final int EXPORT_TYPE_STUDY = 0;
  public static final int EXPORT_TYPE_PATIENT = 1;

  // Imports
  public static final String IMPORT_PROPERTIES_FILE = "importRegistry.properties";

  // Import Definition Files
  public static final String IMPORT_DEFINITION_DIRECTORY_DEFAULT = "default/xchg/";
  public static final String IMPORT_DEFINITION_FIELDS_FILENAME = "ImportDefinitionFields.xlsx";
  public static final String IMPORT_DEFINITION_DATA_SOURCES_FILENAME = "ImportDefinitionDatasources.xlsx";
  public static final String IMPORT_DEFINITION_TYPES_FILENAME = "ImportDefinitionTypes.xlsx";
  public static final String IMPORT_REGISTRY_DEFINITION_FILE = "importRegistryDefinition.xls";

  // Import classes
  public static final String IMPORT_DEFAULT_OBJECTS_PACKAGE = "edu.stanford.registry.server.imports.";

  public static final String[] IMPORT_DATASOURCES = { "PatientProfile", "PatientAttribute", "Appointment" };
  public static final String[] IMPORT_DATASOURCE_CLASSNAMES = { "data.PatientProfile", "data.PatientAttribute",
  "data.Appointment2" };
  public static final String[] IMPORT_TYPES = { "BasicTableData" };
  public static final String[] IMPORT_TYPE_CLASSNAMES = { "data.BasicTableDataManager" };

  public static final int IMPORT_TYPE_PATIENT_PROFILE = 0;
  public static final int IMPORT_TYPE_APPOINTMENT = 1;

  public static final String XCHG_FORMATTER_PACKAGE = "edu.stanford.registry.server.xchg.data.";
  public static final String XCHG__QUALIFIER_PACKAGE = "edu.stanford.registry.server.xchg.data.";

}
