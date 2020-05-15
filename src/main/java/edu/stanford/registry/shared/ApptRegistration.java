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
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ApptRegistration extends DataTableBase implements IsSerializable, DataTable {
  private static final long serialVersionUID = 1L;

  private Long apptRegId;
  private Long surveySiteId;
  private String patientId;
  private Date visitDt;
  private String registrationType;
  private String visitType;
  private String apptComplete;
  private String clinic;
  private String encounterEid;
  private Long providerId;
  private AssessmentRegistration assessment;

  public ApptRegistration() {
    assessment = new AssessmentRegistration();
  }

  public ApptRegistration(Long surveySiteId, String patientId, Date surveyDt, String emailAddr,
      String surveyType, String regType, String visitType) {
    assessment = new AssessmentRegistration();
    setSurveySiteId(surveySiteId);
    setPatientId(patientId);
    setSurveyDt(surveyDt);
    setEmailAddr(emailAddr);
    setSurveyType(surveyType);
    setRegistrationType(regType);
    setVisitType(visitType);
    setMetaVersion(0);
  }

  /**
   * Get the appointment id
   */
  public ApptId getApptId() {
    return new ApptId(apptRegId);
  }

  /**
   * Get the assessment id
   */
  public AssessmentId getAssessmentId() {
    return assessment.getAssessmentId();
  }

  /**
   * Return the assessment data as the survey date
   */
  public Date getSurveyDt() {
    return assessment.getAssessmentDt();
  }

  /**
   * Set both the visit date and the assessment date
   */
  public void setSurveyDt(Date surveyDt) {
    this.visitDt = surveyDt;
    assessment.setAssessmentDt(surveyDt);
  }

  /**
   * Add a survey registration to the appointment.
   */
  public void addSurveyReg(SurveyRegistration surveyReg) {
    assessment.addSurveyReg(surveyReg);
  }

  /**
   * Get the survey registration for the appointment.
   * This assumes that the appointment has only one survey registration.
   */
  @Deprecated
  public SurveyRegistration getSurveyReg() {
    return assessment.getSurveyReg();
  }

  /**
   * Get the survey registration for the appointment by name.
   */
  public SurveyRegistration getSurveyReg(String name) {
    return assessment.getSurveyReg(name);
  }

  /**
   * Is the appointment an active appointment
   */
  public boolean isAppointment() {
    if (registrationType != null && registrationType.equals(Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT)) {
      return true;
    }
    return false;
  }

  /**
   * Is the appointment a canceled appointment
   */
  public boolean isCancelled() {
    if (registrationType != null && registrationType.equals(Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT)) {
      return true;
    }
    return false;
  }

  /**
   * Get the number of completed studies for all surveys in the appointment.
   */
  public Integer getNumberCompleted() {
    return assessment.getNumberCompleted();
  }

  /**
   * Get the number of pending studies for all surveys in the appointment.
   */
  public Integer getNumberPending() {
    return assessment.getNumberPending();
  }

  /**
   * Has the study been done for this registration
   */
  public boolean getIsDone() {
    for(SurveyRegistration surveyReg : assessment.getSurveyRegList()) {
      boolean done = (surveyReg.getNumberPending() != null) && (surveyReg.getNumberPending() == 0) &&
          (surveyReg.getNumberCompleted() != null) && (surveyReg.getNumberCompleted() > 0);
      if (!done) {
        return false;
      }
    }
    return true;
  }

  // Getter/Setter methods

  public Long getApptRegId() {
    return apptRegId;
  }

  public void setApptRegId(Long regId) {
    apptRegId = regId;
  }

  public Long getAssessmentRegId() {
    return assessment.getAssessmentRegId();
  }

  public void setAssessmentRegId(Long regId) {
    assessment.setAssessmentRegId(regId);
  }

  public Long getSurveySiteId() {
    return surveySiteId;
  }

  public void setSurveySiteId(Long surveySiteId) {
    this.surveySiteId = surveySiteId;
    assessment.setSurveySiteId(surveySiteId);
  }

  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
    assessment.setPatientId(patientId);
  }

  public String getEmailAddr() {
    return assessment.getEmailAddr();
  }

  public void setEmailAddr(String emailAddr) {
    assessment.setEmailAddr(emailAddr);
  }

  public String getSurveyType() {
    return assessment.getAssessmentType();
  }

  public void setSurveyType(String surveyType) {
    assessment.setAssessmentType(surveyType);
  }

  public Date getAssessmentDt() {
    return assessment.getAssessmentDt();
  }

  public void setAssessmentDt(Date assessmentDt) {
    assessment.setAssessmentDt(assessmentDt);
  }

  public String getAssessmentType() {
    return assessment.getAssessmentType();
  }

  public void setAssessmentType(String assessmentType) {
    assessment.setAssessmentType(assessmentType);
  }

  public Date getVisitDt() {
    return visitDt;
  }

  public void setVisitDt(Date visitDt) {
    this.visitDt = visitDt;
  }

  public String getRegistrationType() {
    return registrationType;
  }

  public void setRegistrationType(String registrationType) {
    this.registrationType = registrationType;
  }

  public String getVisitType() {
    return visitType;
  }

  public void setVisitType(String visitType) {
    this.visitType = visitType;
  }

  public String getApptComplete() {
    return apptComplete;
  }

  public void setApptComplete(String apptComplete) {
    this.apptComplete = apptComplete;
  }

  public String getClinic() {
    return clinic;
  }

  public void setClinic(String clinic) {
    this.clinic = clinic;
  }

  public String getEncounterEid() {
    return encounterEid;
  }

  public void setEncounterEid(String encounterEid) {
    this.encounterEid = encounterEid;
  }

  public Long getProviderId() {
    return providerId;
  }

  public void setProviderId(Long providerId) {
    this.providerId = providerId;
  }

  /**
   * Returns the appointment's non-null assessment.
   */
  public AssessmentRegistration getAssessment() {
    return assessment;
  }

  /**
   * @param assessment Should not be null
   */
  public void setAssessment(AssessmentRegistration assessment) {
    this.assessment = assessment;
  }

  public List<SurveyRegistration> getSurveyRegList() {
    return assessment.getSurveyRegList();
  }

  public void setSurveyRegList(List<SurveyRegistration> surveyRegList) {
    assessment.setSurveyRegList(surveyRegList);
  }

  public Date getEmailDt() {
    return assessment.getEmailDt();
  }

  public void setEmailDt(Date emailDt) {
    assessment.setEmailDt(emailDt);
  }

  public boolean getSendEmail() {
    return assessment.getSendEmail();
  }

  public void setSendEmail(boolean doSend) {
    assessment.setSendEmail(doSend);
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
