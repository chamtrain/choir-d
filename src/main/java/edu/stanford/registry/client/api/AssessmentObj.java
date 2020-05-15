/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
 * All Rights Reserved.
 *
 * See the NOTICE and LICENSE files distributed with this work for information
 * regarding copyright ownership and licensing. You may not use this file except
 * in compliance with a written license agreement with Stanford University.
 *
 * Unless required by applicab        le law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTES OR CONDITIONS OF ANY KIND, either express or implied. See your
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.stanford.registry.client.api;

import java.util.List;

/**
 * API Asssessment Registration definition
 *
 * @author tpacht
 */
@SuppressWarnings("unused") // to be used in the next version of the api
public interface AssessmentObj {

  Long getAssessmentId();

  void setAssessmentId(Long assessmentId);

  Long getSiteId();

  void setSiteId(Long surveySiteId);

  String getPatientId();

  void setPatientId(String patientId);

  String getEmailAddr();

  void setEmailAddr(String emailAddr);

  String getAssessmentDt();

  void setAssessmentDt(String assessmentDt);

  String getAssessmentType();

  void setAssessmentType(String assessmentType);

  List<SurveyRegistrationObj> getSurveyRegistrations();

  void setSurveyRegistrations(List<SurveyRegistrationObj> surveyRegList);

  List<ApptObj> getAppointments();

  void setAppointments(List<ApptObj> apptObj);

  List<NotificationObj> getNotificationList();

  void setNotificationList(List<NotificationObj> notificationList);
}
