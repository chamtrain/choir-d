/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Registry App client configuration bean. This class provides configuration
 * values for the Registry App client.
 */
public class ClientConfig implements IsSerializable {

  public static final String[] PATIENT_ATTRIBUTE_TYPE_DATE = new String[] {"DATE_PICKER"};
  public final static String[] PATIENT_ATTRIBUTE_TYPE_YESNO = { "", "Y", "N" };
  
  private HashMap<String,String> params = new HashMap<String,String>();
  private Long siteId;

  // Clinic filter enaabled
  private boolean clinicFilterEnabled = false;
  // "All clinics" option enabled for clinic filter
  private boolean clinicFilterAllEnabled = false;
  // Initial clinic filter value
  private String clinicFilterValue = null;
  // Map from clinic name to list of clinics (department IDs) to be included
  private Map<String,List<String>> clinicFilterMapping = null;
  private Map<String, String> customPatientAttributeHeadings = null;
  private Map<String, String[]> customPatientAttributeValues = null;
  // Custom reports
  private CustomClinicReportConfig[] customReports = null;
  // Custom tabs
  private ArrayList<CustomTab> customTabs = new ArrayList<>();
  private ArrayList<CustomTab> customPatientTabs = new ArrayList<>();
  private ArrayList<CustomView> customViews = new ArrayList<>();

  public ClientConfig() {
  }

  /**
   * Useful for adding a key,value pair
   */
  public HashMap<String, String> getParams() {
    return params;
  }

  public Long getSiteId() {
    return siteId;
  }

  public String getSiteName() {
    return getParam("siteName");
  }

  /**
   * Returns the parameter, or null if not found (never an empty string).
   */
  public String getParam(String key) {
    return params == null ? null : params.get(key);
  }

  /**
   * Returns the parameter, or dflt if not found.
   */
  public String getParam(String key, String dflt) {
    String value = params == null ? null : params.get(key);
    return value == null ? dflt : value;
  }

  /**
   * Returns true if getParam(key, dflt) matches toMatch,
   * or if getParam(key, dflt) returns null or empty and toMatch is null or empty.
   */
  public boolean paramEquals(String key, String dflt, String toMatch) {
    String value = getParam(key, dflt);
    if (value == null || value.isEmpty()) {
      return toMatch == null || toMatch.isEmpty();
    }
    return value.equals(toMatch);
  }

  /**
   * Returns true if getParam(key, dflt) matches toMatch (ignoring case)
   * or if getParam(key, dflt) returns null or empty and toMatch is null or empty.
   */
  public boolean paramEqualsIgnoreCase(String key, String dflt, String toMatch) {
    String value = getParam(key, dflt);
    if (value == null || value.isEmpty()) {
      return toMatch == null || toMatch.isEmpty();
    }
    return value.equalsIgnoreCase(toMatch);
  }

  public void init(Long siteId, HashMap<String, String> params) {
    this.siteId = siteId;
    this.params = params;
  }

  public boolean isClinicFilterEnabled() {
    return clinicFilterEnabled;
  }

  public void setClinicFilterEnabled(boolean clinicFilterEnabled) {
    this.clinicFilterEnabled = clinicFilterEnabled;
  }

  public boolean isClinicFilterAllEnabled() {
    return clinicFilterAllEnabled;
  }

  public void setClinicFilterAllEnabled(boolean clinicFilterAllEnabled) {
    this.clinicFilterAllEnabled = clinicFilterAllEnabled;
  }

  public String getClinicFilterValue() {
    return clinicFilterValue;
  }

  public void setClinicFilterValue(String clinicFilterValue) {
    this.clinicFilterValue = clinicFilterValue;
  }

  public Map<String,List<String>> getClinicFilterMapping() {
    return clinicFilterMapping;
  }

  public void setClinicFilterMapping(Map<String,List<String>> clinicFilterMapping) {
    this.clinicFilterMapping = clinicFilterMapping;
  }

  public CustomClinicReportConfig[] getCustomReports() {
    return customReports;
  }

  public void setCustomReports(CustomClinicReportConfig[] customReports) {
    this.customReports = customReports;
  }

  public void addCustomPatientAttribute(String dataName, String heading, String[] values ) {
    if (dataName == null) {
      return;
    }
    if (customPatientAttributeHeadings == null) {
      customPatientAttributeHeadings = new LinkedHashMap<>();
    }
    if (heading == null) {
      heading = dataName;
    }
    customPatientAttributeHeadings.put(dataName, heading);
    if (customPatientAttributeValues == null) {
      customPatientAttributeValues = new HashMap<>();
    }
    if (values != null) {
      customPatientAttributeValues.put(dataName, values);
    }
  }
  public String getCustomPatientAttributeHeading(String dataName)  {
    if (customPatientAttributeHeadings == null) {
      return null;
    }
    return customPatientAttributeHeadings.get(dataName);
  }

  public String[] getCustomPatientAttributeValues(String dataName)  {
    if (customPatientAttributeValues == null) {
      return null;
    }
    return customPatientAttributeValues.get(dataName);
  }

  public Set<String> getCustomPatientAttributeNames() {
    if (customPatientAttributeHeadings == null) {
      return null;
    }
    return customPatientAttributeHeadings.keySet();
  }

  public ArrayList<CustomTab> getCustomTabs() {
    return customTabs;
  }

  /**
   * Add a tab to the CHOIR application
   * @param tab CustomTab object that holds the content for the tab
   */
  public void addCustomTab(CustomTab tab) {
    customTabs.add(tab);
  }

  /**
   * Add a tab to the CHOIR application
   * @param authority the array of Roles and views required to be given this tab
   * @param path relative path of the html page
   * @param title title of the tab that shows in the client
   */
  public void addCustomTab(String[] authority, String path, String title) {
    addCustomTab(new CustomTab(authority, path, title));
  }

  /**
   * Convenience method to add a tab to the CHOIR application when only one role is required
   * @param authority the Role the user must have to see this tab
   * @param path relative path of the html page
   * @param title title of the tab that shows in the client
   */
  public void addCustomTab(String authority, String path, String title) {
    String[] authorityArr = { authority };
    addCustomTab(authorityArr, path, title);
  }

  /**
   * Returns a list of patient tabs that the user has permissions to see
   *
   * @param user The CHOIR user
   */
  public ArrayList<CustomTab> getCustomPatientTabs(User user) {
    ArrayList<CustomTab> qualifiesTabs = new ArrayList<>();
    for (CustomTab customTab : customPatientTabs) {
      if (qualifiesForTab(customTab, user)) {
        qualifiesTabs.add(customTab);
      }
    }
    return qualifiesTabs;
  }

  /**
   * Add a sub-tab to the CHOIR application's patient tab
   * @param tab CustomTab object that holds the content for the tab
   */
  public void addCustomPatientTab(CustomTab tab) {
    customPatientTabs.add(tab);
  }

  /**
   * Add a sub-tab to the CHOIR application's patient tab
   * @param authority the array of Roles/Views required to be given this tab
   * @param path relative path of the html page
   * @param title title of the tab that shows on the patient tab
   */
  public void addCustomPatientTab(String[] authority, String path, String title) {
    addCustomPatientTab(new CustomTab(authority, path, title));
  }

  /**
   * Convenience method to add a sub-tab to the CHOIR application's patient tab
   * Use when there is only one role the user needs to see the tab
   * @param authority the Role the user must have to see this tab
   * @param path relative path of the html page
   * @param title title of the tab that shows on the patient tab
   */
  public void addCustomPatientTab(String authority, String path, String title) {
    String[] authorityArr = { authority };
    addCustomPatientTab(authorityArr, path, title);
  }

  /**
   * Add a custom view to control showing/not showing custom tabs to users
   *
   * @param customView the custom role
   */
  public void addCustomView(CustomView customView) {
    customViews.add(customView);
  }

  /**
   * Convenience method to add custom views for controlling showing tabs
   *
   * @param viewName name of the role that's stored in user_authority
   * @param title    the descriptive title of the role shown on the user admin tab
   */
  public CustomView addCustomView(String viewName, String title) {
    CustomView customView = new CustomView(viewName, title, getSiteName());
    addCustomView(customView);
    return customView;
  }

  /**
   * Retuns all custom views defined for this site
   *
   * @return Map of custom views
   */
  public Map<String, String> getCustomViews() {
    Map<String, String> views = new LinkedHashMap<>();
    for (CustomView role : customViews) {
      views.put(role.getAuthorityName(), role.getTitle());
    }
    return views;
  }

  /**
   * Does this user have a view allowing them to see the given tab
   *
   * @param customTab Tab in question
   * @param user      CHOIR user
   * @return true if this user has one of the views for the tab
   */
  public boolean qualifiesForTab(CustomTab customTab, User user) {
    String[] authorityArr = customTab.getAuthority();
    boolean requiresView = false;
    int qualifyingViews = 0;

    for (String authority : authorityArr) {
      if (!authority.startsWith(CustomView.PREFIX)) { // must be a role
        if (!user.hasRole(authority, getSiteName())) {
          GWT.log("User does not have role " + authority + " for site " + getSiteName() + " returning false");
          return false; // must have all roles
        }
      } else {
        requiresView = true; // IF custom views are listed then the user must have at least one of them
        if (user.hasRole(authority)) {
          qualifyingViews++;
        }
      }
    }
    GWT.log("User has all roles");
    if (requiresView && qualifyingViews < 1) {
      GWT.log("User does not have any of the qualifying views return false");
      return false;
    }
    GWT.log("User has all roles and at least one view, returning true");

    return true;
  }
}
