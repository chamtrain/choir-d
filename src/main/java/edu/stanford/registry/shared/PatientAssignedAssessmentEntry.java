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


package edu.stanford.registry.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PatientAssignedAssessmentEntry implements IsSerializable {

  private String instrumentName;
  private Integer frequency;
  private String clinicName;

  public PatientAssignedAssessmentEntry() {
  }

  public PatientAssignedAssessmentEntry(String instrumentName, Integer frequency, String clinicName) {
    this.instrumentName = instrumentName;
    this.frequency = frequency;
    this.clinicName = clinicName;
  }

  public String getInstrumentName() {
    return instrumentName;
  }

  public void setInstrumentName(String instrumentName) {
    this.instrumentName = instrumentName;
  }

  public Integer getFrequency() {
    return frequency;
  }

  public void setFrequency(Integer frequency) {
    this.frequency = frequency;
  }

  public String getClinicName() {
    return clinicName;
  }

  public void setClinicName(String clinicName) {
    this.clinicName = clinicName;
  }
}
