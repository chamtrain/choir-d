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

package edu.stanford.registry.server.service;

import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.shared.ConfigParam;
import edu.stanford.registry.shared.DataTable;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.ProcessInfo;
import edu.stanford.registry.shared.RegConfigProperty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AdministrativeServices extends ClinicServices {

  ArrayList<DataTable> getTableData(String tablename) throws InvalidDataElementException;

  int loadCsv(File importDefinitionXlxs, File csvFile) throws IOException;

  int loadPendingImports(String definitionName) throws IOException;

  ArrayList<String> getFileImportDefinitions() throws IOException;

  boolean reloadXml();

  int doSurveyInvitations(Mailer mailer, String serverUrl) throws IOException;

  List<List<Object>> scoresExportData(Map<String,String[]> params);

  void reloadUsers();

  void reloadConfig();

  ArrayList<ProcessInfo> getRunningProcesses();

  void clearProcesses();

  ConfigParam updateConfig(ConfigParam param) throws Exception;

  ArrayList<RegConfigProperty> getRegConfigProperties ();

  ConfigParam getConfig(RegConfigProperty property);
  
  HashMap<String,String> getInitialTemplates(List<String> processNames, Map<String,String> allTemplates);
  
  HashMap<String,String> getReminderTemplates(List<String> processNames, Map<String,String> allTemplates);
}
