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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface AdminServiceAsync extends ClinicServiceAsync {

  void getTableData(String tableName, AsyncCallback<ArrayList<DataTable>> callback);

  void loadPendingImports(String definitionName, AsyncCallback<Integer> callback) throws ServiceUnavailableException;

  void getFileImportDefinitions(AsyncCallback<ArrayList<String>> callback) throws ServiceUnavailableException;

  void reloadXml(AsyncCallback<Boolean> asyncCallback);

  void doSurveyInvitations(AsyncCallback<Integer> callback);

  void reloadUsers(AsyncCallback<Void> callback);

  void reloadConfig(AsyncCallback<Void> callback);

  void getRunningProcesses(AsyncCallback<ArrayList<ProcessInfo>> callback);

  void clearProcesses(AsyncCallback<Void> callback);

  void updateConfig(ConfigParam param, AsyncCallback<ConfigParam> async);

  void getRegConfigProperties(AsyncCallback<ArrayList<RegConfigProperty>> async);

  void getConfig(RegConfigProperty property, AsyncCallback<ConfigParam> async);
}
