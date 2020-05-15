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

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AssessmentConfig implements IsSerializable {

  private Long siteId;
  private String clinicName;
  private String assessmentType;

  private Map<String, Integer> instruments;

  public AssessmentConfig() { }

  public AssessmentConfig(String clinicName, String assessmentType, Map<String, Integer> instruments, Long siteId) {
    setSiteId(siteId);
    setClinicName(clinicName);
    setAssessmentType(assessmentType);
    setInstruments(instruments);
  }

  public Long getSiteId() { return siteId; }

  public void setSiteId(Long siteId) { this.siteId = siteId; }

  public String getClinicName() { return clinicName; }

  public void setClinicName(String clinicName) { this.clinicName = clinicName; }

  public String getAssessmentType() { return assessmentType; }

  public void setAssessmentType(String assessmentType) { this.assessmentType = assessmentType; }

  public Map<String, Integer> getInstruments() { return instruments; }

  public void setInstruments(Map<String, Integer> instruments) { this.instruments = instruments; }
}
