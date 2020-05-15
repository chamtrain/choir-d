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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AssignedPatientAssessment implements IsSerializable {

  private String patientId;
  private Long siteId;
  private String clinicName;
  private Map<String, Integer> assignedInstruments;

  public AssignedPatientAssessment() {
  }

  public AssignedPatientAssessment(String patientId, Long siteId, String clinicName) {
    this.patientId = patientId;
    this.siteId = siteId;
    this.clinicName = clinicName;
    assignedInstruments = new HashMap<>();
  }

  public void assignAssessment(String instrumentName, Integer frequency) {
    if (assignedInstruments == null) {
      assignedInstruments = new HashMap<>();
    }

    if (!instrumentName.isEmpty() && frequency != null) {
      assignedInstruments.put(instrumentName, frequency);
    }
  }

  public Integer getFrequency(String instrumentName) {
    if (instrumentName != null && !instrumentName.isEmpty()) {
      return assignedInstruments.get(instrumentName);
    }
    return null;
  }

  public String getPatientId() {
    return patientId;
  }

  public Long getSiteId() {
    return siteId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public void setSiteId(Long siteId) {
    this.siteId = siteId;
  }

  public String getClinicName() {
    return clinicName;
  }

  public void setClinicName(String clinicName) {
    this.clinicName = clinicName;
  }

  public boolean hasAssignment() {
    if (assignedInstruments != null && !assignedInstruments.isEmpty()) {
      return true;
    }
    return false;
  }

  public List<String> getAssignedInstrumentNames() {
    List<String> instruments = new ArrayList<>();
    if (assignedInstruments != null && !assignedInstruments.isEmpty()) {
      for (String instrument : assignedInstruments.keySet()) {
        instruments.add(instrument);
      }
    }
    return (instruments != null && !instruments.isEmpty()) ? instruments : Collections.<String>emptyList();
  }

  public boolean hasInstrument(String instrument) {
    if (instrument == null || instrument.isEmpty()) {
      return false;
    }
    return assignedInstruments.containsKey(instrument);
  }
}
