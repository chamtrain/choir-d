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
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PatientRegistrationSearch implements IsSerializable, Serializable {

  private static final long serialVersionUID = 4361707152683068033L;
  public static final int CANCELLED = 1;
  public static final int CONSENTED = 2;
  public static final int NOTPRINTED = 3;
  public static final int PRINTED = 4;
  public static final int UNOTIFIED = 5;
  public static final String CONSENTTYPE = "Patient Consented";
  public static final String DECLINETYPE = "Patient Declined";

  private boolean cancelled = false;
  private boolean consented = false;
  private boolean notPrinted = false;
  private boolean printed = false;
  private boolean unnotified = false;

  private ArrayList<String> excludeTypes = new ArrayList<>();
  private List<String> includeClinics = null;
  private SortBy sortBy = SortBy.apptTime;
  private boolean isAscending = true;

  @Override
  public String toString() {
    String strBuf = "cancelled=" + cancelled + ";"
        + "consented=" + consented + ";"
        + "printed=" + printed + ";"
        + "notPrinted=" + notPrinted + ";"
        + "unnotified=" + unnotified + ";";
    return strBuf;
  }

  public PatientRegistrationSearch() {
    /*
     * By default we exclude consent and decline registrations
     */
    excludeTypes.add(CONSENTTYPE);
    excludeTypes.add(DECLINETYPE);
  }

  public void setOption(int option) {
    if (option == CANCELLED) {
      cancelled = true;
    }
    if (option == CONSENTED) {
      consented = true;
    }
    if (option == NOTPRINTED) {
      notPrinted = true;
    }
    if (option == PRINTED) {
      printed = true;
    }
    if (option == UNOTIFIED) {
      unnotified = true;
    }
  }

  public boolean cancelled() {
    return cancelled;
  }

  public boolean consented() {
    return consented;
  }

  public boolean notPrinted() {
    return notPrinted;
  }

  public boolean printed() {
    return printed;
  }

  public boolean unnotified() {
    return unnotified;
  }

  public SortBy getSortBy() {
    return sortBy;
  }



  public List<String> getExcludeTypes() {
    return excludeTypes;
  }

  public void includeType(String str) {
    excludeTypes.remove(str);
  }

  public void excludeType(String str) {
    if (!excludeTypes.contains(str)) {
      excludeTypes.add(str);
    }
  }

  public void excludeType(List<String> types) {
    for (String type : types) {
      excludeType(type);
    }
  }

  public void includeClinics(List<String> clinics) {
    includeClinics = clinics;
  }

  public List<String> getIncludeClinics() {
    return includeClinics;
  }

  public void setSortBy(String sortByString) {
    try {
      if (sortByString == null) {
        return;
      }
      sortBy = SortBy.valueOf(sortByString);
    } catch (IllegalArgumentException iae) {
      sortBy = null;
    }
  }

  public boolean getSortAscending() {
    return isAscending;
  }

  public void setSortAscending(boolean asc) {
    isAscending = asc;
  }

  public enum SortBy {mrn, lastName, firstName, apptType, apptTime, surveyType}
}
