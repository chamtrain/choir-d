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

import com.google.gwt.user.client.rpc.IsSerializable;

public class RegConfigProperty implements IsSerializable  {


  public RegConfigProperty() {
  }

  // All package-private, so other classes with main() methods can access them
  private String name;
  public String getName() {
    return name;
  }

  private String desc;
  public String getDesc() {
    return desc;
  }
  private String defValue;
  private RegConfigUsage usage;
  public String getUsageAbbrev() {
    return usage.abbrev;
  }

  public RegConfigUsage getUsage() {
    return usage;
  }


  private RegConfigCategory category;  // AssessCtr, ClinicUI, DataImport, Email, GeneralSystem, PatientSurvey, Reporting
  public RegConfigCategory getCategory() {
    return category;
  }

  /**
   * 
   * @param name     Try to keep this to lowercase words separate by dots
   * @param desc     Description
   * @param usage     GlobalOnly, GlobalPrefixed, SiteOnly, SiteAndGlobal
   * @param category  first character of: AssessCtr, ClinicUI, DataImport, Email, GeneralSystem, PatientSurvey, Reporting
   * @param defValue  default value, if not set
   */
  public RegConfigProperty(String name, RegConfigCategory category, RegConfigUsage usage, String defValue, String desc) {
    if (name == null) // || usage == null || defValue == null || valueTtype == null)
      throw new RuntimeException("Registry property poorly defined: null");

    boolean required = (name.charAt(name.length()-1) == '*');
    this.name = required ? name.substring(0, name.length()-1) : name;
    this.desc = desc;
    this.usage = usage;
    this.defValue = defValue;

    this.category = category;
  }

  public String getDefValue() {
    return defValue;
  }
  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof RegConfigProperty && ((RegConfigProperty) other).name.equals(name);

  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

// --Commented out by Inspection START (2/28/18, 11:12 AM):
//  static Comparator<RegConfigProperty> getComparable() {
//    return new Comparator<RegConfigProperty>() {
//      @Override
//      public int compare(RegConfigProperty first, RegConfigProperty second) {
//        return first.name.compareTo(second.name);
//      }
//    };
//  }
// --Commented out by Inspection STOP (2/28/18, 11:12 AM)
}
