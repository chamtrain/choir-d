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

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ClientServiceAsync {
  void getUser(AsyncCallback<User> callback); // TODO remove

  /**
   * The client should call this method immediately after loading, to get necessary information to determine the available feature set, and
   * get the window id (a cookieless "session" identifier tied to the window rather than the browser as a whole).
   *
   * @param app
   *          limit feature set to a single service, or null if you want everything available
   * @param reloadingWindowId
   *          pass in the window id if reloading an existing window, or null for a new window
   */
  // void loadWindow(Service app, String reloadingWindowId, AsyncCallback<WindowInfo> async);

  /**
   * Report client-side log events to the server, for recording in a server-side log.
   */
  void clientLog(List<LogRecord> log, AsyncCallback<Void> async);

  void getClientConfig(AsyncCallback<ClientConfig> callback);

  void getInitParams(AsyncCallback<HashMap<String, String>> callback);

  void getSiteConfig(String configName, AsyncCallback<String> callback);
}
