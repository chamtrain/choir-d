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

import edu.stanford.registry.client.service.AppointmentStatus;

import java.util.Date;

public class PatientRegistration extends ApptRegistration {

  /**
   * This class extends the survey registration to include the patients info
   * and other data the clients need.
   */

  private static final long serialVersionUID = -8794436629538425400L;
  private Patient pat = null;

  private Integer emailsSent = 0;
  private Integer numberPrints = 0;
  private AppointmentStatus appointmentStatus;
  private Date surveyLastCompleted;
  private boolean surveyRequired = true;
  private ApptAction action = null;
  private String notes;

  // Constructors
  public PatientRegistration() {
    pat = new Patient();
  }

  public PatientRegistration(Patient patient) {
    pat = patient;
  }

  /**
   * Gets the patient object for this patient survey registration
   *
   * @return Patient
   */
  public Patient getPatient() {
    return pat;
  }

  /**
   * Sets the patient object for this patient survey registration
   */
  public void setPatient(Patient patient) {
    pat = patient;
  }

  /**
   * Get the patients first name.
   *
   * @return first name.
   */
  public String getFirstName() {
    return pat.getFirstName();
  }

  public void setFirstName(String name) {
    pat.setFirstName(name);
  }

  /**
   * Get the patients last name.
   *
   * @return last name.
   */
  public String getLastName() {
    return pat.getLastName();
  }

  public void setLastName(String name) {
    pat.setLastName(name);
  }

  public void setPatientMetaVersion(Integer vs) {
    pat.setMetaVersion(vs);
  }

  public Integer getPatientMetaVersion() {
    return pat.getMetaVersion();
  }

  public void setPatientDtCreated(Date created) {
    pat.setDtCreated(created);
  }

  public Date getPatientDtCreated() {
    return pat.getDtCreated();
  }

  public void setPatientDtChanged(Date changed) {
    pat.setDtChanged(changed);
  }

  public Date getPatientDtChanged() {
    return pat.getDtChanged();
  }

  @Override
  public void setPatientId(String patientID) {
    super.setPatientId(patientID);
    pat.setPatientId(patientID);
  }

  /**
   * Get the patients date of birth.
   */
  public Date getDtBirth() {
    return pat.getDtBirth();
  }

  /**
   * Set the patients date of birth.
   */
  public void setDtBirth(Date dob) {
    pat.setDtBirth(dob);
  }

  /**
   * Sets the patients consent value
   */
  public void setConsent(String yn) {
    pat.setConsent(yn);
  }

  public String getConsent() {
    return pat.getConsent();
  }

  public void setNumberEmailsSent(Integer count) {
    emailsSent = count;
  }

  public Integer getNumberEmailsSent() {
    return emailsSent;
  }

  public void setNumberPrints(Integer count) {
    numberPrints = count;
  }

  public Integer getNumberPrints() {
    return numberPrints;
  }

  public void setAppointmentStatus(AppointmentStatus appointmentStatus) {
    this.appointmentStatus = appointmentStatus;
  }

  public AppointmentStatus getAppointmentStatus() {
    return appointmentStatus;
  }

  /**
   * @param status Y, N, or null for completed, notCompleted, or null respectively
   */
  public void setAppointmentStatusString(String status) {
    if ("Y".equals(status)) {
      appointmentStatus = AppointmentStatus.completed;
    } else if ("N".equals(status)) {
      appointmentStatus = AppointmentStatus.notCompleted;
    } else {
      appointmentStatus = null;
    }
  }

  public String getAppointmentStatusString() {
    if (appointmentStatus == AppointmentStatus.completed) {
      return "Y";
    } else if (appointmentStatus == AppointmentStatus.notCompleted) {
      return "N";
    } else {
      return null;
    }
  }

  public Date getSurveyLastCompleted() {
    return surveyLastCompleted;
  }

  public void setSurveyLastCompleted(Date dt) {
    surveyLastCompleted = dt;
  }

  public boolean getSurveyRequired() {
    return surveyRequired;
  }

  public void setSurveyRequired(boolean isRequired) {
    surveyRequired = isRequired;
  }

  public ApptAction getAction() {
    return action;
  }

  public void setAction(ApptAction action) {
    this.action = action;
  }

  public boolean hasDeclined() {
    return getPatient() != null && getPatient().hasDeclined();
  }

  public boolean hasConsented() {
    return getPatient() != null && getPatient().hasConsented();
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  @Override
  public String toString() {
    return "PatientRegistration{" +
        "pat=" + pat +
        ", emailsSent=" + emailsSent +
        ", numberPrints=" + numberPrints +
        ", appointmentStatus=" + appointmentStatus +
        ", surveyLastCompleted=" + surveyLastCompleted +
        ", surveyRequired=" + surveyRequired +
        ", action='" + action.getActionType() + '\'' +
        ", notes='" + notes + '\'' +
        '}';
  }
}
