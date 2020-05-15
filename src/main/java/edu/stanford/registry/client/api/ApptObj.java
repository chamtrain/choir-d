/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.client.api;


/**
 * API Appointment Registration definition
 *
 * @author tpacht
 */
@SuppressWarnings("unused") // The unused methods are for the next version of the api
public interface ApptObj {

  Long getApptId();

  void setApptId(Long assessmentId);

  Long getSiteId();

  void setSiteId(Long surveySiteId);

  String getPatientId();

  void setPatientId(String patientId);

  Long getAssessmentId();

  void setAssessmentId(Long assessmentId);

  String getVisitDt();

  void setVisitDt(String visitDt);

  String getRegistrationType();

  void setRegistrationType(String registrationType);

  String getVisitType();

  void setVisitType(String visitType);

  String getComplete();

  void setComplete(String apptComplete);

  String getClinic();

  void setClinic(String clinic);

  String getEncounter();

  void setEncounter(String encounter);

  String getProvider();

  void setProvider(String provider);
}
