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

import java.io.Serializable;

public class Site implements Serializable {
  private static final long serialVersionUID = 1L;

  Long siteId;
  String urlParam;
  String displayName;
  String siteIdString;
  boolean enabled;

  /**
   * This exists just for GWT instantiation
   */
  Site() {
  }

  public Site(Long surveySiteId, String urlParam, String displayName, boolean enabled) {
    this.siteId = surveySiteId;
    this.urlParam = urlParam;
    this.displayName = displayName;
    this.enabled = enabled;
  }

  /**
   * Make it easier for log statements
   * @return  e.g. "Site 3/ped: "
   */
  public String getIdString() {
    if (siteIdString == null) {
      siteIdString = "Site " + siteId + ((siteId == 1L) ? "" : ("/" + urlParam))+ ": ";
    }
    return siteIdString;
  }

  public Long getSiteId() {
    return siteId;
  }

  public String getUrlParam() {
    return urlParam;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || !(other instanceof Site)) {
      return false;
    }
    return siteId.equals(((Site)other).siteId);
  }

  @Override
  public int hashCode() { // this isn't used, but its omission causes compiler warnings.
    return (siteId + urlParam + "." + displayName).hashCode();
  }

  public boolean isEnabled() {
    return enabled;
  }
}
