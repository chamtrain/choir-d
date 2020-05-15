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

package edu.stanford.registry.shared;

import java.util.ArrayList;


public class UserDetail extends User {
  private static final long serialVersionUID = 1L;
  private ArrayList<String> grantedRoles = null;

  /**
   * This exists only for serialization
   */
  UserDetail() {
  }

  public UserDetail(UserPrincipal up) {
    super(up);
    this.grantedRoles = new ArrayList<>();
  }

  /**
   * Creates a UserDetail for an enabled user, with no UserPrincipalId, yet.
   * @param name
   * @param email May be null
   */
  public UserDetail(Long idpId, String name, String email) {
    super(idpId, name, null, 0L, email, true);
    this.grantedRoles = new ArrayList<>();
  }

  public ArrayList<String> getGrantedRoles() {
    return grantedRoles;
  }

  public void addGrantedRole(String role) {
    if (!grantedRoles.contains(role)) {
      grantedRoles.add(role);
    }
  }

  public void removeGrantedRole(String role) {
    if (grantedRoles.contains(role)) {
      grantedRoles.remove(role);
    }
  }

  public void setGrantedRoles(ArrayList<String> newRoles) {
    if (newRoles == null) {
      newRoles = new ArrayList<>();
    }
    grantedRoles = newRoles;
  }

  public boolean hasGrantedRole(String role) {
    if (getGrantedRoles() != null && getGrantedRoles().contains(role)) {
      return true;
    }
    return false;
  }
}
