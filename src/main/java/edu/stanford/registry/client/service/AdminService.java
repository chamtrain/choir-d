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

package edu.stanford.registry.client.service;

import edu.stanford.registry.shared.ConfigParam;
import edu.stanford.registry.shared.DataTable;
import edu.stanford.registry.shared.ProcessInfo;
import edu.stanford.registry.shared.RegConfigProperty;
import edu.stanford.registry.shared.ServiceUnavailableException;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service for administrative services
 */
@RemoteServiceRelativePath("main")
public interface AdminService extends ClinicService {

  ArrayList<DataTable> getTableData(String tableName) throws IllegalArgumentException, ServiceUnavailableException;

  int loadPendingImports(String definitionName) throws ServiceUnavailableException;

  ArrayList<String> getFileImportDefinitions() throws ServiceUnavailableException;

  boolean reloadXml() throws ServiceUnavailableException;

  int doSurveyInvitations();

  void reloadUsers();

  void reloadConfig();

  ArrayList<ProcessInfo> getRunningProcesses();

  void clearProcesses();

  ConfigParam updateConfig(ConfigParam param) throws Exception;

   ArrayList<RegConfigProperty> getRegConfigProperties ();

   ConfigParam getConfig(RegConfigProperty property);
}
