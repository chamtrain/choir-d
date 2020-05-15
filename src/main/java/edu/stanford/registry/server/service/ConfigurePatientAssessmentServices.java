/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.service;

import edu.stanford.registry.shared.AssignedPatientAssessment;
import edu.stanford.registry.shared.PatientAssignedAssessmentEntry;
import java.util.List;

public interface ConfigurePatientAssessmentServices extends AssessmentConfigServices {

  AssignedPatientAssessment updatePatientAssignedAssessments(AssignedPatientAssessment assignedInstruments);

  PatientAssignedAssessmentEntry getLowFrequencyByInstrumentName(String patientID, Long siteId, String instrument);

  AssignedPatientAssessment getAssignmentByClinic(String patientID, Long siteId, String clinicName);

  List<AssignedPatientAssessment> getAllAssignment(String patientID, Long siteId);
}
