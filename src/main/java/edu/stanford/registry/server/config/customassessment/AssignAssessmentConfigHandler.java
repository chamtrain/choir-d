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

package edu.stanford.registry.server.config.customassessment;

import edu.stanford.registry.shared.AssignedPatientAssessment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssignAssessmentConfigHandler {

  private static final Logger logger = LoggerFactory.getLogger(AssignAssessmentConfigHandler.class);
  private Long siteId;
  private String patientID;
  private CustomPatientAssessmentConfigUtil cConfig;

  public AssignAssessmentConfigHandler(Long siteId, String patientID, CustomPatientAssessmentConfigUtil cConfig) {
    this.siteId = siteId;
    this.patientID = patientID;
    this.cConfig = cConfig;
  }

  public AssignAssessmentConfigHandler(Long siteId, String patientID, String json) {
    this.siteId = siteId;
    this.patientID = patientID;
    this.cConfig = new CustomPatientAssessmentConfigUtil(json);
  }

  public void addOrUpdate(AssignedPatientAssessment value) {
    if (value.getPatientId().equalsIgnoreCase(this.patientID) && value.getSiteId().equals(this.siteId)) {

      if (!value.hasAssignment()) {
        // clear the assigned assessments and remove entry from config
        if (removePatientAssessmentConfig(value.getClinicName())) {
          cConfig.getJson();
        }
        return; // do not continue further
      }

      boolean newConfig = true;
      List<InstrumentEntry> instrumentEntries = new ArrayList<>();
      for (String instrument : value.getAssignedInstrumentNames()) {
        InstrumentEntry entry = cConfig.newInstrumentEntry();
        entry.setFrequency(value.getFrequency(instrument));
        entry.setName(instrument);
        instrumentEntries.add(entry);
      }

      for (PatientAssessmentConfig pConfig : cConfig.getAssignedAssessmentWrapper().getValues()) {
        if (StringUtils.equalsIgnoreCase(value.getClinicName(), pConfig.getClinicName())) {
          pConfig.setInstrumentEntry(instrumentEntries);
          newConfig = false;
          break; // no need to search further
        }
      }

      if (newConfig) {
        PatientAssessmentConfig p = cConfig.newPatientAssessmentConfig();
        p.setInstrumentEntry(instrumentEntries);
        p.setClinicName(value.getClinicName());
        cConfig.addAssignmentForClinic(p);
      }
    }
  }

  public List<AssignedPatientAssessment> getAllAssignment() {
    List<AssignedPatientAssessment> list = null;

    if (cConfig.getAssignedAssessmentWrapper() != null) {
      List<PatientAssessmentConfig> patientAssessmentConfigs = getAll();
      if (patientAssessmentConfigs != null && !patientAssessmentConfigs.isEmpty()) {
        list = new ArrayList<>();
        for (PatientAssessmentConfig p : patientAssessmentConfigs) {
          AssignedPatientAssessment assigned = new AssignedPatientAssessment(patientID, siteId, p.getClinicName());
          for (InstrumentEntry entry : p.getInstrumentEntry()) {
            assigned.assignAssessment(entry.getName(), entry.getFrequency());
          }
          list.add(assigned);
        }
      }
    }

    return (list != null && !list.isEmpty()) ? list : Collections.emptyList();
  }

  public AssignedPatientAssessment getAssignmentByClinic(String clinicName) {
    try {
      List<AssignedPatientAssessment> list = getAllAssignment();
      if (list == null) {
        logger.info("No assigned assessments configured for : {}" + patientID);
        return null;
      }

      for (AssignedPatientAssessment a : list) {
        if (StringUtils.equalsIgnoreCase(a.getClinicName(), clinicName)) {
          return a;
        }
      }
    } catch (Exception e) {
      logger.error("Error occurred getting patient assigned assessments for the current clinic", e);
    }
    return null;
  }

  private List<PatientAssessmentConfig> getAll() {
    return cConfig.getAssignedAssessmentWrapper().getValues();
  }

  private boolean removePatientAssessmentConfig(String clinicName) {
    for (int i = 0; i < cConfig.getAssignedAssessmentWrapper().getValues().size(); i++) {
      if (StringUtils.equalsIgnoreCase(cConfig.getAssignedAssessmentWrapper().getValues().get(i).getClinicName(),
          clinicName)) {
        cConfig.getAssignedAssessmentWrapper().getValues().remove(i);
        return true;
      }
    }
    return false;
  }

  public String getConfigJson() {
    return cConfig.getJson();
  }

}
