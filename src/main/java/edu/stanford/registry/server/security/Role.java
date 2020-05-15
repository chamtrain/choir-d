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

package edu.stanford.registry.server.security;

import edu.stanford.registry.server.service.Service;
import edu.stanford.registry.shared.Constants;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Role {
  public static final String PHYSICIAN = "PHYSICIAN";

  private static final Map<String,String> roleToDisplayName = new LinkedHashMap<>();
  private static final Map<String,String> serviceToRoleName = new HashMap<>();

  static {
    // Mapping for role name to role description
    roleToDisplayName.put(Constants.ROLE_CLINIC_STAFF, "Schedule, Patient & Reports tabs");
    roleToDisplayName.put(Constants.ROLE_EDITOR, "Edit templates");
    roleToDisplayName.put(Constants.ROLE_DATA_EXCHANGE, "Import/export data");
    roleToDisplayName.put(Constants.ROLE_API_EXTRACT, "API data extracts");
    roleToDisplayName.put(Constants.ROLE_PHYSICIAN, "Enter/view provider survey data");
    roleToDisplayName.put(Constants.ROLE_REGISTRATION, "Register Patients");
    roleToDisplayName.put(Constants.ROLE_SECURTY, "User Administration");
    roleToDisplayName.put(Constants.ROLE_BUILDER, "Survey Builder");
    roleToDisplayName.put(Constants.ROLE_DEVELOPER, "Developer");
    roleToDisplayName.put(Constants.ROLE_ASSESSMENT_CONFIG_EDITOR, "Assessment Config Editor");
    roleToDisplayName.put(Constants.ROLE_ASSIGN_ASSESSMENT,
        "Configure assessments per patient (requires Assessment Configuration option enabled)");

    // Mapping from service to role name
    serviceToRoleName.put(Service.CLINIC_SERVICES.getInterfaceClass().getName(), Constants.ROLE_CLINIC_STAFF);
    serviceToRoleName.put(Service.ADMIN_SERVICES.getInterfaceClass().getName(), Constants.ROLE_DATA_EXCHANGE);
    serviceToRoleName.put(Service.SECURITY_SERVICES.getInterfaceClass().getName(), Constants.ROLE_SECURTY);
    serviceToRoleName.put(Service.REGISTER_SERVICES.getInterfaceClass().getName(), Constants.ROLE_REGISTRATION);
    serviceToRoleName.put(Service.EDITOR_SERVICES.getInterfaceClass().getName(), Constants.ROLE_EDITOR);
    serviceToRoleName.put(Service.SURVEY2_SERVICE.getInterfaceClass().getName(), Constants.ROLE_PATIENT);
    serviceToRoleName.put(Service.PHYSICIAN_SERVICES.getInterfaceClass().getName(), Constants.ROLE_PHYSICIAN);
    serviceToRoleName.put(Service.BUILDER_SERVICES.getInterfaceClass().getName(), Constants.ROLE_BUILDER);
    serviceToRoleName.put(Service.API_EXTRACT_SERVICES.getInterfaceClass().getName(), Constants.ROLE_API_EXTRACT);
    serviceToRoleName
        .put(Service.ASSESSMENT_CONFIG_SERVICES.getInterfaceClass().getName(), Constants.ROLE_ASSESSMENT_CONFIG_EDITOR);
    serviceToRoleName.put(Service.CONFIGURE_PATIENT_ASSESSMENT_SERVICES.getInterfaceClass().getName(),
        Constants.ROLE_ASSIGN_ASSESSMENT);
  }

  /**
   * Return the authorities needed to access the available roles
   * for a given site. The authority name is the role name suffixed
   * with the siteId name. For example "CLINIC_STAFF[sat]".
   */
  public static Map<String,String> getRoles(String siteName) {
    // Use a LinkedHashMap so that the order of the keys is predictable
    Map<String, String> roles = new LinkedHashMap<>();

    // Convert the role names to the authority for this site
    for(Map.Entry<String,String> entry : roleToDisplayName.entrySet()) {
      String authority = getAuthority(entry.getKey(), siteName);
      roles.put(authority, entry.getValue());
    }
    return roles;
  }

  public Set<String> getRoleSet() {
    return roleToDisplayName.keySet();
  }

  /**
   * Return the role name needed to access the given service.
   */
  public static String getRole(String service) {
    String role = serviceToRoleName.get(service);
    return (role != null) ? role : "";
  }

  /**
   * Return the authority corresponding to the role and site. The
   * authority is the role name with the siteId suffix. For example
   * "CLINIC_STAFF[sat]".
   */
  public static String getAuthority(String role, String siteName) {
    return role + "[" + siteName + "]";
  }
}
