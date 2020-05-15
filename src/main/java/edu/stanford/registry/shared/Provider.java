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

import com.google.gwt.user.client.rpc.IsSerializable;

public class Provider extends DataTableBase implements IsSerializable, Serializable, DataTable {

  /**
   * Represents an activity table row
   */
  private static final long serialVersionUID = -2401124985999440578L;
  private Long providerId;
  private String providerEid;
  private Long userPrincipalId;


  public static final String[] HEADERS = { "Provider Id", "Provider External Id", "User Principal Id" };

  public static final int[] CHANGE_INDICATORS = { 0, 1, 1 };

  public Provider() {

  }

  public Provider(long providerId, String providerEid, long userPrincipalId) {
    this.providerId = providerId;
    this.providerEid = providerEid;
    this.userPrincipalId = userPrincipalId;
  }

  public Provider(Long providerId, String providerEid, Long userPrincipalId) {
    this.providerId = providerId;
    this.providerEid = providerEid;
    this.userPrincipalId = userPrincipalId;
  }

  /**
   * Get the providerId
   */
  public Long getProviderId() {
    return providerId;
  }

  /**
   * Set the providerId
   */
  public void setProviderId(Long providerId) {
    this.providerId = providerId;
  }

  /**
   * Get the external provider id.
   *
   * @return providerEid
   */
  public String getProviderEid() {
    return providerEid;
  }

  /**
   * Set the external provider id.
   */
  public void setProviderEid(String providerEid) {
    this.providerEid = providerEid;
  }

  /**
   * Get the value of the user that is this provider.
   *
   * @return associated user.
   */
  public Long getUserPrincipalId() {
    return userPrincipalId;
  }

  /**
   * Set the value of the user id
   */
  public void setUserPrincipalId(Long userId) {
    userPrincipalId = userId;
  }

  /**
   * Get the headers to display when the contents of this table are viewed.
   */
  @Override
  public String[] getAllHeaders() {
    return HEADERS;
  }

  /**
   * Get the on/off indicators of which fields can be modified.
   */
  @Override
  public int[] getChangeIndicators() {
    return CHANGE_INDICATORS;
  }

  /**
   * Gets the objects data as a string array.
   */
  @Override
  public String[] getData(DateUtilsIntf utils) {
    String data[] = new String[8];
    data[0] = getProviderId().toString();
    data[1] = getProviderEid();
    data[2] = getUserPrincipalId().toString();
    return data;
  }

  /**
   * Sets the objects data from a string array.
   */
  @Override
  public void setData(String[] data) throws InvalidDataElementException {
    // check that the array has the correct number of entries
    if (data == null || data.length < 3) {
      throw new InvalidDataElementException("Invalid number of data elements ");
    }

    // check that the required elements are not missing
    if (data[0] == null || data[1] == null || data[2] == null || data[3] == null) {
      throw new InvalidDataElementException("Invalid null data value");
    }

    try {
      setProviderId(Long.valueOf(data[0]));
      setProviderEid(data[1]);
      setUserPrincipalId(Long.valueOf(data[2]));
      setDtChanged(getNow());
    } catch (Exception e) {
      throw new InvalidDataElementException(e.getMessage(), e);
    }
  }

  /**
   * Get the registrations primary key.
   *
   * @return object array with the providerEid and activityDate
   */
  public Object[] getPrimaryKey() {
    return new Object[] { getProviderId() };
  }
}
