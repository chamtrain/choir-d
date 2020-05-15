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
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.UserDetail;
import edu.stanford.registry.shared.UserIdp;

import java.util.ArrayList;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service for security services.
 */
@RemoteServiceRelativePath("/registry/svc/security")
public interface SecurityService extends ClinicService {

  Map<String, String> getRoles(String displayName) throws ServiceUnavailableException;

  UserDetail findUserDetail(String text);

  void saveUserDetail(ArrayList<UserDetail> user);

  ArrayList<UserDetail> findUsersWithDisplayName(String displayName);

  ArrayList<UserDetail> findAllUsers();

  ArrayList<Provider> findProviders(boolean onlyUnassigned);

  ArrayList<UserDetail> findAllUsers(String text);

  ArrayList<UserIdp> findIdentityProviders();

}
