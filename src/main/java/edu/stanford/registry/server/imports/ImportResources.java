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
import edu.stanford.registry.server.service.formatter.PatientIdFormatIntf;
import edu.stanford.registry.server.utils.XchgUtils;
import edu.stanford.registry.server.xchg.DefinitionDataSource;
import edu.stanford.registry.server.xchg.DefinitionDataSourcesXlsx;
import edu.stanford.registry.server.xchg.data.Constants;
import edu.stanford.registry.shared.InvalidPatientIdException;

import java.util.HashMap;

public class ImportResources {
  // private static Logger logger = Logger.getLogger(ImportResources.class);
  private ImportDefinitionTypesXlxs importTypes;
  private ImportDefinitionDataSourcesXlxs importDataSources;
  private PatientIdFormatIntf formatter;

  /**
   * Map contains all the defined data sources, keyed by data source name.
   */
  private HashMap<String, DefinitionDataSource> dataSourceDefinitionsHash = new HashMap<>();

  public ImportResources(SiteInfo siteInfo) throws Exception {

    XchgUtils xChgUtils = new XchgUtils(siteInfo);
    // Get the data definitions
    importTypes = new ImportDefinitionTypesXlxs(
        xChgUtils.getImportStandardDefinitionFile(ImportDefinitionTypesXlxs.FILENAME));
    importDataSources = new ImportDefinitionDataSourcesXlxs(
        xChgUtils.getImportStandardDefinitionFile(ImportDefinitionDataSourcesXlxs.FILENAME));
    // importFields = new ImportDefinitionFieldsXlsx(importFileDirectory);

    DefinitionDataSourcesXlsx importDataSources = new DefinitionDataSourcesXlsx(
        xChgUtils.getImportStandardDefinitionFile(Constants.IMPORT_DEFINITION_DATA_SOURCES_FILENAME));
    importDataSources.reset();
    DefinitionDataSource dataSource = importDataSources.getNext();
    while (dataSource != null) {
      dataSourceDefinitionsHash.put(dataSource.getDataSource(), dataSource);
      dataSource = importDataSources.getNext();
    }
    importDataSources.reset();

    /*
     * Get the formatter for validating mrn's
     */
    formatter = siteInfo.getPatientIdFormatter();
  }

  public HashMap<String, DefinitionDataSource> getDataSourceDefinitionsHash() {
    return dataSourceDefinitionsHash;
  }

  public ImportDefinitionDataSourcesXlxs getDefinitionDataSources() {
    return importDataSources;
  }

  public ImportDefinitionTypesXlxs getDefinitionTypes() {
    return importTypes;
  }

  public DefinitionDataSource getDefinitionDataSource(String dataSourceName) {
    return dataSourceDefinitionsHash.get(dataSourceName);
  }

  public ImportDefinitionType getDefinitionType(String typeName) {
    return importTypes.get(typeName);
  }

  /**
   * Checks if the patient ID is valid.
   *
   * @param mrn Patient ID.
   * @return Valid Patient ID.
   * @throws Exception thrown if the patient id is not valid.
   */
  public String validMrn(String mrn) throws Exception {
    if (mrn == null) {
      throw new InvalidPatientIdException("Missing mrn", true);
    }

    mrn = formatter.format(mrn);
    if (formatter.isValid(mrn)) {
      return mrn;
    }
    throw new InvalidPatientIdException(formatter.getInvalidMessage(), true);
  }

}
