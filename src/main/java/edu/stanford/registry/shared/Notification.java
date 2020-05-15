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

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Notification extends DataTableBase implements IsSerializable, DataTable {

  private static final long serialVersionUID = -3839996091080757526L;
  private Integer notificationId;
  private Long surveySiteId;
  private String patientId;
  private Long assessmentRegId;
  private String emailType;
  private Date surveyDt;
  private Date emailSentDt;

  public static final String[] HEADERS = { "Notification Id", "Survey Site Id", "Patient Id", "Appt Registration Id",
      "Type", "Date", "Email Sent Date",
      "MetaData Version", "Date Created", "Date Changed" };

  public static final int[] CHANGE_INDICATORS = { 0, 0, 1, 0, 1, 1, 1, 1, 0, 0 };

  public Notification() {

  }

  public Notification(String patientId, AssessmentId assessmentId, String type, Date surveyDate, Integer version,
                      Long surveySiteId) {
    this.patientId = patientId;
    this.assessmentRegId = assessmentId.getId();
    this.emailType = type;
    this.surveyDt = surveyDate;
    this.surveySiteId = surveySiteId;
    setMetaVersion(version);
  }

  /**
   * Get the Notification id.
   *
   * @return Notification Id
   */
  public Integer getNotificationId() {
    return notificationId;
  }

  /**
   * Set the notification id.
   */
  public void setNotificationId(Integer notifID) {
    notificationId = notifID;
  }

  /**
   * Get the survey siteId id.
   */
  public Long getSurveySiteId() {
    return surveySiteId;
  }

  /**
   * Set the survey siteId id.
   */
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
   * Get the assessment registration id
   */
  public Long getAssessmentRegId() {
    return assessmentRegId;
  }

  /**
   * Set the assessment registration id
   */
  public void setAssessmentRegId(Long asessmentRegId) {
    this.assessmentRegId = asessmentRegId;
  }

  /**
   * Get the assessment id
   */
  public AssessmentId getAssessmentId() {
    return new AssessmentId(assessmentRegId);
  }

  /**
   * Get the type of email to send
   */
  public String getEmailType() {
    return emailType;
  }

  /**
   * Set the type of email to be sent
   */
  public void setEmailType(String type) {
    emailType = type;
  }

  /**
   * Gets the survey date and time.
   *
   * @return surveyTimestamp
   */
  public Date getSurveyDt() {
    return surveyDt;
  }

  /**
   * Set the value of the registration survey survey date and time.
   */
  public void setSurveyDt(Date dt) {
    surveyDt = dt;
  }

  /**
   * Gets the date and time the email was sent.
   *
   * @return email Date
   */
  public Date getEmailDt() {
    return emailSentDt;
  }

  /**
   * Set the value of the email date.
   */
  public void setEmailDt(Date dt) {
    emailSentDt = dt;
  }

  @Override
  public String[] getAllHeaders() {
    return HEADERS;
  }

  @Override
  public int[] getChangeIndicators() {
    return CHANGE_INDICATORS;
  }

  @Override
  public String[] getData(DateUtilsIntf utils) {
    String data[] = new String[11];
    data[0] = getNotificationId().toString();
    data[1] = getSurveySiteId().toString();
    data[2] = getPatientId();
    data[3] = getAssessmentRegId().toString();
    data[4] = getEmailType();
    data[5] = utils.getDateString(getSurveyDt());
    data[6] = utils.getDateString(getEmailDt());
    data[7] = getMetaVersion().toString();
    data[8] = utils.getDateString(getDtCreated());
    data[9] = utils.getDateString(getDtChanged());

    return data;
  }

  @Override
  public void setData(String[] data) throws InvalidDataElementException {
    // check that the array has the correct number of entries
    if (data == null || data.length < 8) {
      throw new InvalidDataElementException("Invalid number of data elements ");
    }

    // check that the required elements are not missing
    if (data[0] == null || data[1] == null || data[2] == null || data[3] == null) {
      throw new InvalidDataElementException("Invalid null data value");
    }

    try {
      setNotificationId(Integer.valueOf(data[0]));
      setSurveySiteId(Long.valueOf(data[1]));
      setPatientId(data[2]);
      setAssessmentRegId(Long.valueOf(data[3]));
      setEmailType(data[4]);
      if (data[5] != null) {
        setSurveyDt(CommonUtils.dateFromYyyyDashMmDashDd(data[5]));
      }
      if (data[6] != null) {
        setEmailDt(CommonUtils.dateFromYyyyDashMmDashDd(data[6]));
      }
      setMetaVersion(Integer.valueOf(data[7]));
      setDtChanged(getNow());
    } catch (Exception e) {
      throw new InvalidDataElementException(e.getMessage(), e);
    }

  }

}
