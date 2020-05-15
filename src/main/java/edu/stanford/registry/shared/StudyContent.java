/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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

public class StudyContent  implements IsSerializable {

  private Long appConfigId;
  private Long surveySiteId;  // study content is global, so this is 0


  private String configValue;
  private Boolean enabled;
  private Integer surveySystemId;
  private Integer studyCode;
  private String studyName;
  private String title;
  private Date dtCreated;


  public StudyContent() {

  }

  public StudyContent(Long appConfigId, long surveySiteId, String configValue, boolean enabled,
                      Integer surveySystemId, Integer studyCode, String studyName, String title, Date dtCreated) {
    setAppConfigId(appConfigId);
    setSurveySiteId(surveySiteId);
    setConfigValue(configValue);
    setEnabled(enabled);
    setSurveySystemId(surveySystemId);
    setStudyCode(studyCode);
    setStudyName(studyName);
    setTitle(title);
    setDtCreated(dtCreated);
  }

  public Long getAppConfigId() {
    return appConfigId;
  }

  public void setAppConfigId(Long appConfigId) {
    this.appConfigId = appConfigId;
  }

  public Long getSurveySiteId() {
    return surveySiteId;
  }

  public void setSurveySiteId(Long surveySiteId) {
    this.surveySiteId = surveySiteId;
  }

  public String getConfigType() {
    return "surveycontent";
  }

  public String getConfigValue() {
    return configValue;
  }

  public void setConfigValue(String configValue) {
    this.configValue = configValue;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Integer getSurveySystemId() {
    return surveySystemId;
  }

  public void setSurveySystemId(Integer surveySystemId) {
    this.surveySystemId = surveySystemId;
  }

  public Integer getStudyCode() {
    return studyCode;
  }

  public void setStudyCode(Integer studyCode) {
    this.studyCode = studyCode;
  }

  public Date getDtCreated() {
    return dtCreated;
  }

  public void setDtCreated(Date dtCreated) {
    this.dtCreated = dtCreated;
  }

  public String getStudyName() {
    return studyName;
  }

  public void setStudyName(String studyName) {
    this.studyName = studyName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}

