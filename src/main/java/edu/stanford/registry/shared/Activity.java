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
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Activity extends DataTableBase implements IsSerializable, Serializable, DataTable {

  /**
   * Represents an activity table row
   */
  private static final long serialVersionUID = -2401124985999440578L;
  private Long activityId;
  private Long surveySiteId;
  private String patientId;
  private Date activityDt;
  private String activityType;
  private String token;
  private Long assessmentRegId;
  private Long userPrincipalId;
  private String displayName;
  private String userDisplayName;

  public static final String[] HEADERS = { "Activity Id", "Survey Site Id", "Patient Id", "Activity Date", "Activity Type",
      "Token", "MetaData Version", "Date Created", "Date Changed" };

  public static final int[] CHANGE_INDICATORS = { 0, 0, 0, 1, 1, 1, 1, 0, 0 };

  public Activity() {

  }

  /**
   * Constructor to make an Activity
   *
   * @param patientId    the patients unique id.
   * @param activityType the type of activity
   * @param token          the token identifying which survey the activity is related to.
   */
  public Activity(String patientId, String activityType, String token) {
    this(patientId, activityType, null, token, null);
  }

  public Activity(String patientId, String activityType, AssessmentId assessmentId, String token, Long userPrincipalId) {
    this.patientId = patientId;
    this.activityType = activityType;
    this.token = token;
    this.assessmentRegId = (assessmentId == null) ? null : assessmentId.getId();
    this.userPrincipalId = userPrincipalId;
    setMetaVersion(0);
  }

  /**
   * Get the activity id (pk)
   */
  public Long getActivityId() {
    return activityId;
  }

  public void setActivityId(Long activityId) {
    this.activityId = activityId;
  }

  /**
   * Get the survey site
   */
  public Long getSurveySiteId() {
    return surveySiteId;
  }

  public void setSurveySiteId(Long surveySiteId) {
    this.surveySiteId = surveySiteId;
  }

  /**
   * Get the patient id.
   *
   * @return patientsId
   */
  public String getPatientId() {
    return patientId;
  }

  /**
   * Set the patient id.
   */
  public void setPatientId(String patientID) {
    patientId = patientID;
  }

  /**
   * Gets the date and time of the activity.
   *
   * @return activityDate
   */
  public Date getActivityDt() {
    return activityDt;
  }

  /**
   * Set the value of the patients activity date and time.
   */
  public void setActivityDt(Date dt) {
    activityDt = dt;
  }

  /**
   * Gets the patients activity type.
   *
   * @return activityType
   */
  public String getActivityType() {
    return activityType;
  }

  /**
   * Set the value of the patients activity type
   */
  public void setActivityType(String activType) {
    activityType = activType;
  }

  /**
   * Get the token
   */
  public String getToken() {
    return token;
  }

  /**
   * Set the token
   */
  public void setToken(String tok) {
    token = tok;
  }

  public Long getAssessmentRegId() {
    return assessmentRegId;
  }

  public void setAssessmentRegId(Long assessmentRegId) {
    this.assessmentRegId = assessmentRegId;
  }

  /**
   * Get the value of the user that did the action.
   *
   * @return null if no user associated to the activity.
   */
  public Long getUserPrincipalId() {
    return userPrincipalId;
  }

  /**
   * Set the value of the user id performing the action
   */
  public void setUserPrincipalId(Long userId) {
    userPrincipalId = userId;
  }

  public String getDisplayName() {
    return userDisplayName;
  }

  public void setDisplayName(String name) {
    userDisplayName = name;
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
    String[] data = new String[9];
    data[0] = getActivityId().toString();
    data[1] = getSurveySiteId().toString();
    data[2] = getPatientId();
    data[3] = utils.getDateString(getActivityDt());
    data[4] = getActivityType();
    data[5] = getToken();
    data[6] = getMetaVersion().toString();
    data[7] = utils.getDateString(getDtCreated());
    data[8] = utils.getDateString(getDtChanged());

    return data;
  }

  /**
   * Sets the objects data from a string array.
   */
  @Override
  public void setData(String[] data) throws InvalidDataElementException {
    // check that the array has the correct number of entries
    if (data == null || data.length < 8) {
      throw new InvalidDataElementException("Invalid number of data elements ");
    }

    // check that the required elements are not missing
    if (data[0] == null || data[1] == null || data[2] == null || data[3] == null || data[4] == null || data[5] == null) {
      throw new InvalidDataElementException("Invalid null data value");
    }

    try {
      setActivityId(Long.valueOf(data[0]));
      setSurveySiteId(Long.valueOf(data[1]));
      setPatientId(data[2]);
      setActivityDt(CommonUtils.dateFromYyyyDashMmDashDd(data[3]));
      setActivityType(data[4]);
      setToken(data[5]);
      setMetaVersion(Integer.valueOf(data[6]));
      setDtCreated(CommonUtils.dateFromYyyyDashMmDashDd(data[7]));
      setDtChanged(getNow());
    } catch (Exception e) {
      throw new InvalidDataElementException(e.getMessage(), e);
    }

  }

}
