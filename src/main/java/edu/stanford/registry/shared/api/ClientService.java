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

package edu.stanford.registry.shared.api;

import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.User;

import java.util.HashMap;
import java.util.List;
import java.util.logging.LogRecord;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("/clientService")
public interface ClientService extends RemoteService {
  User getUser();

  // WindowInfo loadWindow(Service app, String reloadingWindowId);

  void clientLog(List<LogRecord> log);

  ClientConfig getClientConfig();

  HashMap<String, String> getInitParams();

  String getSiteConfig(String configName);

  
}
