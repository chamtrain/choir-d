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

public class SurveyRegistration extends DataTableBase implements IsSerializable, DataTable {

  private static final long serialVersionUID = -8481176676103420737L;

  private Long surveyRegId;
  private Long surveySiteId;
  private String patientId;
  private Date surveyDt;
  private String token;
  private String surveyType;
  
  private Long assessmentRegId;
  private String surveyName;
  private Long surveyOrder;

  private Integer numberCompleted = 0;
  private Integer numberPending = 0;

  public static final String[] HEADERS = { "Survey Registration Id", "Survey Site Id", "Patient Id", "Survey Date",
      "Token", "Survey Type", "MetaData Version", "Date Created", "Date Changed" };

  public static final int[] CHANGE_INDICATORS = { 0, 0, 0, 1, 1, 1, 0, 0, 1 };

  public SurveyRegistration() {

  }

  /**
   * Constructor to make an registration
   */
  public SurveyRegistration(Long surveySiteId, String patientID, Date surveyDate, String surveyTyp) {
    this.surveySiteId = surveySiteId;
    patientId = patientID;
    surveyDt = surveyDate;
    token = null;
    surveyType = surveyTyp;
    setMetaVersion(0);
  }

  /**
   * Get the survey registration id (pk)
   */
  public Long getSurveyRegId() {
    return surveyRegId;
  }

  public void setSurveyRegId(Long regId) {
    surveyRegId = regId;
  }

  /**
   * Get the survey site
   */
  public Long getSurveySiteId() {
    return surveySiteId;
  }

  public void setSurveySiteId(Long siteId) {
    surveySiteId = siteId;
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
   *
   * @param patientID
   */
  public void setPatientId(String patientID) {
    patientId = patientID;
  }

  /**
   * Gets the registration date and time.
   *
   * @return surveyTimestamp
   */
  public Date getSurveyDt() {
    return surveyDt;
  }

  /**
   * Set the value of the patients registration.
   *
   * @param dt registration survey date
   */
  public void setSurveyDt(Date dt) {
    surveyDt = dt;
  }

  /**
   * Get the token
   *
   * @return
   */
  public String getToken() {
    return token;
  }

  /**
   * Set the token
   *
   * @param tok
   */
  public void setToken(String tok) {
    token = tok;
  }

  /**
   * Get the type of survey
   */
  public String getSurveyType() {
    return surveyType;
  }

  /**
   * Set the type of survey, this defines which studies they will be presented
   * with.
   *
   * @param type
   */
  public void setSurveyType(String type) {
    surveyType = type;
  }

  public Long getAssessmentRegId() {
    return assessmentRegId;
  }

  public void setAssessmentRegId(Long assessmentRegId) {
    this.assessmentRegId = assessmentRegId;
  }

  public String getSurveyName() {
    return surveyName;
  }

  public void setSurveyName(String surveyName) {
    this.surveyName = surveyName;
  }

  public Long getSurveyOrder() {
    return surveyOrder;
  }

  public void setSurveyOrder(Long surveyOrder) {
    this.surveyOrder = surveyOrder;
  }

  public Integer getNumberCompleted() {
    return numberCompleted;
  }

  public void setNumberCompleted(Integer numberCompleted) {
    this.numberCompleted = numberCompleted;
  }

  public Integer getNumberPending() {
    return numberPending;
  }

  public void setNumberPending(Integer numberPending) {
    this.numberPending = numberPending;
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
    String data[] = new String[9];
    data[0] = getSurveyRegId().toString();
    data[1] = getSurveySiteId().toString();
    data[2] = getPatientId().toString();
    data[3] = utils.getDateString(getSurveyDt());
    data[4] = getToken();
    data[5] = getSurveyType();
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
    if (data == null || data.length < 7) {
      throw new InvalidDataElementException("Invalid number of data elements ");
    }

    // check that the required elements are not missing
    if (data[0] == null || data[1] == null || data[3] == null || data[4] == null || data[5] == null || data[6] == null
        || data[7] == null || data[8] == null) {
      throw new InvalidDataElementException("Invalid null data value");
    }

    try {
      setSurveyRegId(Long.valueOf(data[0]));
      setSurveySiteId(Long.valueOf(data[1]));
      setPatientId(data[2]);
      setSurveyDt(CommonUtils.dateFromYyyyDashMmDashDd(data[3]));
      setToken(data[4]);
      setSurveyType(data[5]);
      setMetaVersion(Integer.valueOf(data[6]));
      setDtCreated(CommonUtils.dateFromYyyyDashMmDashDd(data[7]));
      setDtChanged(getNow());
    } catch (Exception e) {
      throw new InvalidDataElementException(e.getMessage(), e);
    }

  }

}
