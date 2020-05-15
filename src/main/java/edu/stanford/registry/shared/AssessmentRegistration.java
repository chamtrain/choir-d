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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AssessmentRegistration extends DataTableBase implements IsSerializable, DataTable {
  private static final long serialVersionUID = 1L;

  private Long assessmentRegId;
  private Long surveySiteId;
  private String patientId;
  private String emailAddr;
  private Date assessmentDt;
  private String assessmentType;
  private List<SurveyRegistration> surveyRegList;
  
  // This value is not stored in the assessment_regisration table but
  // is loaded from the notification table
  private Date emailDt;
  // This value is not stored in the assessment_registration table
  private boolean sendEmail = false;
  

  public AssessmentRegistration() {
    this.surveyRegList = new ArrayList<>();
  }

  /**
   * Get the assessment id
   */
  public AssessmentId getAssessmentId() {
    return new AssessmentId(assessmentRegId);
  }
  
  /**
   * Add a survey registration to the assessment.
   */
  public void addSurveyReg(SurveyRegistration surveyReg) {
    surveyRegList.add(surveyReg);
  }

  /**
   * Get the survey registration for the assessment.
   * This assumes that the assessment has only one survey registration.
   */
  @Deprecated
  public SurveyRegistration getSurveyReg() {
    return (surveyRegList.size() > 0) ? surveyRegList.get(0) : null;
  }

  /**
   * Get the survey registration for the assessment by name.
   */
  public SurveyRegistration getSurveyReg(String name) {
    for(SurveyRegistration surveyReg : surveyRegList) {
      if (name.equals(surveyReg.getSurveyName())) {
        return surveyReg;
      }
    }
    return null;
  }

  /**
   * Get the number of completed studies for all surveys in the assessment.
   */
  public Integer getNumberCompleted() {
    int numberCompleted = 0;
    for(SurveyRegistration surveyReg : getSurveyRegList()) {
      numberCompleted += surveyReg.getNumberCompleted();
    }
    return numberCompleted;
  }

  /**
   * Get the number of pending studies for all surveys in the assessment.
   */
  public Integer getNumberPending() {
    int numberPending = 0;
    for(SurveyRegistration surveyReg : getSurveyRegList()) {
      numberPending += surveyReg.getNumberPending();
    }
    return numberPending;
  }

  // Getter/Setter methods

  public Long getAssessmentRegId() {
    return assessmentRegId;
  }

  public void setAssessmentRegId(Long regId) {
    assessmentRegId = regId;
  }

  public Long getSurveySiteId() {
    return surveySiteId;
  }

  public void setSurveySiteId(Long surveySiteId) {
    this.surveySiteId = surveySiteId;
  }

  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public String getEmailAddr() {
    return emailAddr;
  }

  public void setEmailAddr(String emailAddr) {
    this.emailAddr = emailAddr;
  }

  public Date getAssessmentDt() {
    return assessmentDt;
  }

  public void setAssessmentDt(Date assessmentDt) {
    this.assessmentDt = assessmentDt;
  }

  public String getAssessmentType() {
    return assessmentType;
  }

  public void setAssessmentType(String assessmentType) {
    this.assessmentType = assessmentType;
  }

  public List<SurveyRegistration> getSurveyRegList() {
    return surveyRegList;
  }

  public void setSurveyRegList(List<SurveyRegistration> surveyRegList) {
    this.surveyRegList = surveyRegList;
  }

  public Date getEmailDt() {
    return emailDt;
  }

  public void setEmailDt(Date emailDt) {
    this.emailDt = emailDt;
  }

  public boolean getSendEmail() {
    return sendEmail;
  }

  public void setSendEmail(boolean doSend) {
    sendEmail = doSend;
  }

  // DataTable methods

  @Override
  public String[] getAllHeaders() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int[] getChangeIndicators() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getData(DateUtilsIntf utils) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setData(String[] values) throws InvalidDataElementException {
    // TODO Auto-generated method stub 
  }
  
}
