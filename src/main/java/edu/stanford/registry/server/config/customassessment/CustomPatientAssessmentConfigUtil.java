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

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import java.util.ArrayList;
import java.util.List;

interface AssignedAssessmentWrapper {

  void setValues(List<PatientAssessmentConfig> values);

  List<PatientAssessmentConfig> getValues();
}

interface PatientAssessmentConfig {

  void setClinicName(String clinicName);

  String getClinicName();

  List<InstrumentEntry> getInstrumentEntry();

  void setInstrumentEntry(List<InstrumentEntry> instrumentEntry);
}

interface PatientAssessmentConfigFactory extends AutoBeanFactory {

  AutoBean<AssignedAssessmentWrapper> assignedAssessmentWrapper();

  AutoBean<PatientAssessmentConfig> patientAssessmentConfig();

  AutoBean<InstrumentEntry> instrumentEntry();

}

public class CustomPatientAssessmentConfigUtil {

  private PatientAssessmentConfigFactory factory = AutoBeanFactorySource.create(PatientAssessmentConfigFactory.class);
  private AssignedAssessmentWrapper assignedAssessmentWrapper;
  private String json;

  public CustomPatientAssessmentConfigUtil(String json) {
    if (json != null && !json.isEmpty()) {
      this.json = json;
    } else {
      this.json = "{\"values\":[]}";
    }
    assignedAssessmentWrapper = deserializeFromJson(this.json);
  }

  private AssignedAssessmentWrapper deserializeFromJson(String json) {
    AutoBean<AssignedAssessmentWrapper> bean = AutoBeanCodex.decode(factory, AssignedAssessmentWrapper.class, json);
    return bean.as();
  }

  AssignedAssessmentWrapper newAssignedAssessmentWrapper() {
    AutoBean<AssignedAssessmentWrapper> bean = factory.assignedAssessmentWrapper();
    return bean.as();
  }

  PatientAssessmentConfig newPatientAssessmentConfig() {
    AutoBean<PatientAssessmentConfig> bean = factory.patientAssessmentConfig();
    return bean.as();
  }

  InstrumentEntry newInstrumentEntry() {
    AutoBean<InstrumentEntry> bean = factory.instrumentEntry();
    return bean.as();
  }

  private String serializeToJson() {
    AutoBean<AssignedAssessmentWrapper> bean = AutoBeanUtils.getAutoBean(assignedAssessmentWrapper);
    return AutoBeanCodex.encode(bean).getPayload();
  }

  public String getJson() {
    this.json = serializeToJson();
    return json;
  }

  AssignedAssessmentWrapper getAssignedAssessmentWrapper() {
    if (assignedAssessmentWrapper != null && assignedAssessmentWrapper.getValues() != null
        && !assignedAssessmentWrapper.getValues().isEmpty()) {
      return assignedAssessmentWrapper;
    }

    AssignedAssessmentWrapper newWrapper = newAssignedAssessmentWrapper();
    newWrapper.setValues(new ArrayList<>());
    return newWrapper;
  }

  void addAssignmentForClinic(PatientAssessmentConfig value) {
    assignedAssessmentWrapper = getAssignedAssessmentWrapper();
    assignedAssessmentWrapper.getValues().add(value);
    getJson();
  }

}
