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

import edu.stanford.registry.shared.Provider;
import edu.stanford.registry.shared.UserDetail;
import edu.stanford.registry.shared.UserIdp;

import java.util.ArrayList;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface SecurityServiceAsync extends ClinicServiceAsync {

  void getRoles(String displayName, AsyncCallback<Map<String, String>> callback);

  void findUserDetail(String userName, AsyncCallback<UserDetail> callback);

  void saveUserDetail(ArrayList<UserDetail> user, AsyncCallback<Void> callback);

  void findAllUsers(AsyncCallback<ArrayList<UserDetail>> callback);

  void findUsersWithDisplayName(String displayName, AsyncCallback<ArrayList<UserDetail>> callback);

  void findProviders(boolean onlyUnassigned, AsyncCallback<ArrayList<Provider>> callback);

  void findAllUsers(String userName, AsyncCallback<ArrayList<UserDetail>> callback);

  void findIdentityProviders(AsyncCallback<ArrayList<UserIdp>> callback);

}
