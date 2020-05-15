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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import edu.stanford.registry.shared.UserPrincipal;


/**
 * This represents an identified system user.
 *
 * @author tpacht
 */
public class User implements Serializable {

  private static final long serialVersionUID = 1L;
  private static Site[] NO_SITES = new Site[0];

  // This is the basic data for a user
  private ArrayList<String> roles = null;
  private String username;
  private String displayName;
  private boolean enabled;
  private long userPrincipalId;
  private String emailAddr;
  private long providerId;
  private String providerEid;
  private Long idpId;

  // These are only needed when the UI does the getUser() API call (it caches the user)
  private HashMap<String, String> userPreferences = new HashMap<>(); // from the database, site-specific
  private Site[] availableSites = NO_SITES;  // sites this user has roles on, derived from the roles

  /**
   * This is ONLY to be used by serialization
   */
  public User() {
    // This is ONLY to be used by serialization
  }

  public User(Long idpId, String username, String displayName, long principalId, boolean isEnabled) {
    this(idpId, username, displayName, principalId, null, isEnabled);
  }

  public User(Long idpId, String username, String displayName, long principalId, String emailAddr, boolean isEnabled) {
    this(idpId, username, displayName, principalId, emailAddr, isEnabled, new ArrayList<String>());
  }

  public User(UserPrincipal up) {
    this(up.idpId, up.username, up.displayName, up.userPrincipalId, up.emailAddr, up.enabled);
    this.providerEid = up.providerEid;
    if (up.providerId != null) {
      this.providerId = up.providerId;
    }
  }

  User(Long idpId, String name, String displayName, long principalId, String emailAddr, boolean isEnabled, ArrayList<String> roles) {
    this.idpId = idpId;
    this.username = name;
    this.displayName = displayName;
    this.userPrincipalId = principalId;
    this.emailAddr = emailAddr;
    this.enabled = isEnabled;
    this.roles = roles;
  }

  public Long getIdpId() {
    return idpId;
  }

  public void setIdpId(Long idpId) {
    this.idpId = idpId;
  }
  public String getUsername() {
    return username;
  }

  public void setUsername(String name) {
    this.username = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public long getUserPrincipalId() {
    return userPrincipalId;
  }

  public void setUserPrincipalId(long id) {
    userPrincipalId = id;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean b) {
    enabled = b;
  }

  public String getEmailAddr() {
    return emailAddr;
  }

  public void setEmailAddr(String emailAddr) {
    this.emailAddr = emailAddr;
  }

  public long getProviderId() {
    return providerId;
  }

  public void setProviderId(long id) {
    providerId = id;
  }

  public String getProviderEid() {
    return providerEid;
  }

  public void setProviderEid(String eid) {
    this.providerEid = eid;
  }

  public ArrayList<String> getRoles() {
    return roles;
  }

  public void setRoles(ArrayList<String> newRoles) {
    if (newRoles == null) newRoles = new ArrayList<>();
    roles = newRoles;
  }

  public void addRole(String role) {
    if (!roles.contains(role)) roles.add(role);
  }

  /**
   * Check if the user has the role for the site. The specified
   * role should be the role name without the siteId qualifier suffix,
   * for example "CLINIC_STAFF".
   */
  public boolean hasRole(String role, String siteName) {
    // Create the site qualified role name by appending the site suffix
    String authority = getAuthority(role, siteName);

    // Check if the user has the site qualified role name
    if (roles.contains(authority))
      return true;

    // The PATIENT role is special in that it does not have a site qualifier
    if (role.equals(Constants.ROLE_PATIENT) && roles.contains(role))
      return true;

    return false;
  }

  /**
   * Check if the user has the specified authority. The authority is
   * the role name with the siteId qualifier suffix. For example
   * "CLINIC_STAFF[sat]".
   */
  public boolean hasRole(String authority) {
    return roles.contains(authority);
  }

  // This method should correspond to the server side method in
  // edu.stanford.registry.server.security.Role
  private String getAuthority(String role, String siteName) {
    return role + "[" + siteName + "]";
  }

  /**
   * This is only reliably set on the server if the returned HashMap is not empty or
   * clientServices.getUser() was called.
   *
   * On the client, it should be cached after the clientServices.getUser() call,
   * and updated (via SetUserPreferences) just before or after
   * clinicService.updateUserPreferences() is called.
   */
  public HashMap<String, String> getUserPreferences() {
    return userPreferences;
  }

  /**
   * @param service the key used to look up a value. A service or web page might put multiple values
   * in a serialized bean or a JSon string.
   */
  public String getUserPreferences(String service) {
    return userPreferences.get(service);
  }

  /**
   * Used to initialize the preferences after they've been fetched from the database.
   */
  public void setUserPreferences(HashMap<String, String> preferences) {
    this.userPreferences = preferences;
  }

  /**
   * This adds the preference(s) to this object's hash. They must also be sent to the server to
   * be stored longer than the session.
   * @param service This is used simply as a hash key
   * @param preferences Multiple preferences can be stored as a JSon string or as a serialized bean
   */
  public void setUserPreferences(String service, String preferences) {
    userPreferences.put(service, preferences);
  }

  /**
   * Sets the sites the user is allowed to access.  Interprets null as zero sites.
   */
  public void setSurveySites(Site[] allowedSites) {
    // used by ServiceFilter to verify  create a map of Long -> ArrayList[urlParam,displayName]
    availableSites = (allowedSites == null) ? NO_SITES : allowedSites;
  }

  /**
   * Tells whether the sites have been initialized. Currently returns a false negative
   * if they've been initialized, but the user is enabled for no site.
   */
  public boolean areSitesInitted() {
    return availableSites.length > 0;
  }

  public Site[] getSurveySites() {
    return availableSites;
  }

  @Override
  public String toString() {
    return "user: "+displayName;
  }
}
