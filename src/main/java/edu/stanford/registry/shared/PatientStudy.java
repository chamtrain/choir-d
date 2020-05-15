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

public class PatientStudy extends DataTableBase implements IsSerializable, Serializable, DataTable {
  private static final long serialVersionUID = 4359428161682298828L;
  
  private Long patientStudyId;
  private Long surveySiteId;
  private Long surveyRegId;
  private String patientId;
  private Integer surveySystemId;
  private Integer studyCode;
  private String token;
  private String externalReferenceId;
  private Integer orderNumber;
  private String contents;

  public static final String[] HEADERS = { "Patient Study Id", "Survey Site Id", "Survey Registration Id",
      "Patient Id", "Survey System Id", "Study Code", "Token",
      "External Reference ID", "Order", "MetaData Version", "Date Created", "Date Changed" };

  public static final int[] CHANGE_INDICATORS = { 1, 1, 1, 1, 1, 1, 1, 0, 0 };

  // Used by DataTableObjectConverter and when serializing
  public PatientStudy() {
  }

  public PatientStudy(Long siteId) {
    surveySiteId = siteId;
  }

  public PatientStudy(PatientStudy pat) {
    this(pat.getPatientStudyId(), pat.getSurveySiteId(), pat.getSurveyRegId(), pat.getPatientId(), pat
        .getSurveySystemId(), pat.getStudyCode(),
        pat.getToken(), pat.getExternalReferenceId(),
        pat.getOrderNumber(), pat.getMetaVersion(), pat.getDtCreated(), pat.getDtChanged());
  }

  public PatientStudy(Long patientStudyId, Long siteId, Long surveyRegId, String patientID,
                      Integer surveySystemId, Integer studyCode, String token, String extRef,
                      Integer order, Integer version) {
    this(siteId);
    setPatientId(patientID);
    setSurveySystemId(surveySystemId);
    setStudyCode(studyCode);
    setToken(token);
    setOrderNumber(order);
    setExternalReferenceId(extRef);
    setMetaVersion(version);
    setPatientStudyId(patientStudyId);
    setSurveyRegId(surveyRegId);
  }

  public PatientStudy(Long patientStudyId, Long siteId, Long surveyRegId, String patientID,
                      Integer surveySystemId, Integer studyCode, String token, String extRef,
                      Integer order, Integer version, Date dtCreated, Date dtChanged) {
    this(patientStudyId, siteId, surveyRegId, patientID, surveySystemId, studyCode, token, extRef, order, version);
    setDtCreated(dtCreated);
    setDtChanged(dtChanged);
  }

  /**
   * Get the survey registration id (pk)
   */
  public Long getPatientStudyId() {
    return patientStudyId;
  }

  public void setPatientStudyId(Long regId) {
    patientStudyId = regId;
  }

  /**
   * Get the survey registration id
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
   */
  public void setPatientId(String patientID) {
    patientId = patientID;
  }

  public Integer getSurveySystemId() {
    return surveySystemId;
  }

  public void setSurveySystemId(Integer id) {
    surveySystemId = id;
  }

  public Integer getStudyCode() {
    return studyCode;
  }

  public void setStudyCode(Integer code) {
    studyCode = code;
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

  /**
   * Get the external systems reference id.
   */
  public String getExternalReferenceId() {
    return externalReferenceId;
  }

  /**
   * Set the reference id (used by northwestern api for promis studies)
   */
  public void setExternalReferenceId(String refId) {
    externalReferenceId = refId;
  }

  /**
   * Get the order number
   */
  public Integer getOrderNumber() {
    return orderNumber;
  }

  /**
   * Set the order for this study
   */
  public void setOrderNumber(Integer order) {
    orderNumber = order;
  }

  /**
   * Get the display column headers.
   */
  @Override
  public String[] getAllHeaders() {
    return HEADERS;
  }

  /**
   * Returns an int array indicating which data elements can be modified. 0 =
   * no, 1 = yes.
   *
   * @return int array
   */
  @Override
  public int[] getChangeIndicators() {
    return CHANGE_INDICATORS;
  }

  @Override
  public String[] getData(DateUtilsIntf utils) {
    String data[] = new String[10];
    data[0] = getPatientId();
    data[1] = getSurveySystemId().toString();
    data[2] = getStudyCode().toString();
    data[3] = getToken();
    data[4] = getExternalReferenceId();
    data[5] = getOrderNumber().toString();
    data[6] = getMetaVersion().toString();
    data[7] = utils.getDateString(getDtCreated());
    if (getDtChanged() == null) {
      data[8] = "";
    } else {
      data[8] = utils.getDateString(getDtChanged());
    }

    return data;
  }

  /**
   * setData will set the local values for the elements that can be changed and
   * will set the dtChanged value to now.
   */
  @Override
  public void setData(String data[]) throws InvalidDataElementException {
    if (data == null || data.length != 8) {
      throw new InvalidDataElementException("Invalid number of data elements ");
    }

    for (int i = 0; i < 6; i++) {
      if (data[i] == null) {
        throw new InvalidDataElementException("Invalid null data value");
      }
    }
    try {
      setPatientId(data[0]);
      setSurveySystemId(Integer.valueOf(data[1]));
      setStudyCode(Integer.valueOf(data[2]));
      setToken(data[3]);
      setExternalReferenceId(data[4]);
      setOrderNumber(Integer.valueOf(data[5]));
      setMetaVersion(Integer.valueOf(data[6]));
      setDtChanged(getNow());
    } catch (Exception e) {
      throw new InvalidDataElementException(e.getMessage(), e);
    }
  }

  public String getContents() {
    return contents;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }

}
