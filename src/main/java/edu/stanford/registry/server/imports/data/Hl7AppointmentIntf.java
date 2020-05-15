/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.imports.data;

import edu.stanford.registry.server.xchg.ImportException;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.util.Date;

public interface Hl7AppointmentIntf {

  /**
   * Override to implement your own patient processing
   */
  Patient processPatient(String patientIdStr, String firstName, String lastName, String dobString) throws ImportException;

  /**
   * Override to add custom code after processPatient has completed
   */
  Patient postPatient(Patient patient);

  /**
   * Override to implement your own patient attribute handling
   */
  PatientAttribute processPatientAttribute(Patient patient, String attributeName, String attributeValue);

  /**
   * Override to run custom code after processPatientAttribute has completed
   */
  PatientAttribute postPatientAttribute(PatientAttribute patientAttribute);

  /**
   * Override to implement your own appointment handling
   */
  ApptRegistration processAppointment(Patient patient, String apptDateTmStr, String visitEid, String visitDescription,
                                      int apptStatus, String encounterEid, String providerEid, String department) throws ImportException;

  /**
   * Override to run custom code after processAppointment has completed
   */
  ApptRegistration postAppointment(ApptRegistration apptRegistration);

  /**
   * Override for custom visit types
   *
   * @param visitEidStr      Visit External ID String found in the hl7 message
   * @param visitDescription Visit Description found in the hl7 message
   * @return String appt_registration.visit_code for this ID and description.
   */
  String getVisitType(String visitEidStr, String visitDescription);

  String getSurveyType(String patientId, Date apptDate);

  String getSurveyType(String patientId, String visitType, Date apptDate, String curSurveyType);

  String getSurveyType(Patient patient, String visitType, Date apptDate, String providerEid, String curSurveyType);

  /**
   * Override the visit types to be skipped (not loaded)
   *
   * @param visitType  visit type code
   * @param department clinic name
   * @return True if appointment is to be skipped
   */
  boolean skipType(String visitType, String department);
}
