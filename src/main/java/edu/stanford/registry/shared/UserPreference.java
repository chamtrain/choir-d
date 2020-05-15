package edu.stanford.registry.shared;

import java.io.Serializable;

/**
 * Created by tpacht on 11/23/2015.
 */
public class UserPreference implements Serializable {
  private static final long serialVersionUID = 1L;

  private long userPrincipalId;
  private long surveySiteId;
  private String preferenceKey;
  private String preferenceValue;

  public void setUserPrincipalId(long userPrincipalId) {
    this.userPrincipalId = userPrincipalId;
  }

  public long getUserPrincipalId() {
    return userPrincipalId;
  }

  public void setSurveySiteId(long surveySiteId) {this.surveySiteId = surveySiteId; }

  public long getSurveySiteId() { return surveySiteId; }

  public void setPreferenceKey(String preferenceKey) {
    this.preferenceKey = preferenceKey;
  }

  public String getPreferenceKey() {
    return preferenceKey;
  }

  public void setPreferenceValue(String preferenceValue) {
    this.preferenceValue = preferenceValue;
  }

  public String getPreferenceValue() {
    return preferenceValue;
  }
}
