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

import edu.stanford.registry.shared.api.ClientService;
import edu.stanford.registry.shared.api.ClientServiceAsync;
import edu.stanford.registry.server.utils.XchgUtils;
import edu.stanford.registry.server.xchg.DefinitionDataSource;
import edu.stanford.registry.server.xchg.QualifierIntf;
import edu.stanford.registry.server.xchg.XchgData;
import edu.stanford.registry.server.xchg.data.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class ExportDataProcessor {

  private HashMap<String, DefinitionDataSource> dataSourceDefinitionsHash = new HashMap<>();
  private ArrayList<String> dataTypesList = new ArrayList<>();

  private static final String FolderProperty = "RegistryExportFileDirectory";

  static void usage(String err) {
    if (err != null)
      System.err.println("\n  ERROR: "+err);
    System.err.println();
    System.err.println("  USAGE: java... [ InputFileName XlsxFileName ]\n");
    System.err.println("    If the filenames are not given,");
    System.err.println("    InputFileName = " + Constants.EXPORT_PROPERTIES_FILE);
    System.err.println("    XlsxFileName = " + Constants.EXPORT_REGISTRY_DEFINITION_FILE);
    System.err.println("      both in the folder specified from the "+FolderProperty+" parameter fetched from the server.");
    System.err.println();
    System.exit(-1);
  }
  /**
   * @param args
   */
  public static void main(final String[] args) {  // NOT WORKING- DISABLED

    final ExportDataProcessor me = new ExportDataProcessor();
    usage("This has never been used and almost certainly doesn't work- you'll have to modify this code and test it.");
    
    if (args.length != 0 && args.length != 2) { 
      usage("Must have 0 or 2 parameters, not "+args.length);
    } else {
      final String inFilename   = args.length > 0 ? args[0] : null;
      final String xlsxFilename = args.length > 1 ? args[1] : null;

      // Call the service to get the runtime parameters
      final ClientServiceAsync clientService = GWT.create(ClientService.class);
      ((ServiceDefTarget) clientService).setServiceEntryPoint(GWT.getModuleBaseURL()
          + edu.stanford.registry.shared.Constants.SERVLET_PATH + "clientService");
      clientService.getInitParams(new AsyncCallback<HashMap<String, String>>() {
        @Override
        public void onFailure(Throwable caught) {
          // nothing
        }

        @Override
        public void onSuccess(HashMap<String, String> params) {
          try {
            me.runExport(params, inFilename, xlsxFilename);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
    }
  }

  public void runExport(HashMap<String, String> parameters, String inFilename, String xlsxName) throws Exception {
    ExportDataXlsx exportDataSheet = null;
    FileInputStream in = null;

    // Get the directory for the export files from the configuration parameters
    String exportFileDirectory = parameters.get(FolderProperty);

    // If they provided override definition & props files use those.
    if (inFilename != null)
      in = new FileInputStream(new File(inFilename));
    else  // Otherwise use the default ones for submitting to the repository.
      in = new FileInputStream(new File(exportFileDirectory, Constants.EXPORT_PROPERTIES_FILE));

    if (xlsxName != null)
      exportDataSheet = new ExportDataXlsx(xlsxName);
    else
      exportDataSheet = new ExportDataXlsx(exportFileDirectory, Constants.EXPORT_REGISTRY_DEFINITION_FILE);

    Properties exportProps = new Properties();
    exportProps.load(in);
    in.close();

    // Get the data definitions
    @SuppressWarnings("unused")
    ExportDefinitionTypesXlsx exportTypes = new ExportDefinitionTypesXlsx(exportFileDirectory);
    @SuppressWarnings("unused")
    ExportDefinitionFieldsXlsx exportFields = new ExportDefinitionFieldsXlsx(exportFileDirectory);
    makeDataSourceDefinitionsHash(exportFileDirectory);

    // Build the ExportDefinitionQueues
    HashMap<String, ExportDefinitionQueue> queuesMap = new HashMap<>();
    for (int row = 0; row < exportDataSheet.size(); row++) {
      XchgData exportData = exportDataSheet.getNext();

      String dataSourceName = exportData.getDataSource();
      ExportDefinitionQueue queue = queuesMap.get(dataSourceName);

      if (queue == null) {
        queue = new ExportDefinitionQueue(dataSourceName);
      }

      QualifierIntf<?> qualifier = ExportFactory.getFactory().getQualifier(dataSourceName,
          getDataSourceClassName(dataSourceName), exportData.getQualifierString());
      ExportFormatterIntf<?> formatter = ExportFactory.getFactory().getFormatter(dataSourceName,
          exportData.getFormatterString());
      ExportDefinition definition = new ExportDefinition(exportData.getField(), qualifier, formatter, queue.size());

      queue.add(definition);
      queuesMap.put(dataSourceName, queue);
    }

    // TODO: create an ExportManagers for Study and Patient
    // then call an ExportManager to process each type in datatTypes list
  }

  private void makeDataSourceDefinitionsHash(String exportFileDirectory) throws Exception {

    ExportDefinitionDataSourcesXlsx exportDataSources = new ExportDefinitionDataSourcesXlsx(exportFileDirectory);

    exportDataSources.reset();
    DefinitionDataSource dataSource = exportDataSources.getNext();
    while (dataSource != null) {
      if (!dataTypesList.contains(dataSource.getType())) {
        dataTypesList.add(dataSource.getType());
      }
      dataSourceDefinitionsHash.put(dataSource.getDataSource(), dataSource);
      dataSource = exportDataSources.getNext();
    }
    exportDataSources.reset();
  }

  private String getDataSourceClassName(String dataSourceName) {

    DefinitionDataSource dataSource = dataSourceDefinitionsHash.get(dataSourceName);
    if (dataSource != null) {
      if (!isNullOrBlank(dataSource.getClassName())) {
        return dataSource.getClassName();
      }
    }

    // Get the name for system classes.

    XchgUtils.ExportDataSourceClassName(dataSourceName);
    return null;
  }

  private static boolean isNullOrBlank(String str) {
    if (str == null) {
      return true;
    }
    if (str.trim().length() < 1) {
      return true;
    }
    return false;
  }
}
