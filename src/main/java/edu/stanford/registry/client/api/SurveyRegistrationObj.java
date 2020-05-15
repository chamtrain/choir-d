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

import java.util.List;

/**
 * API Survey Registration definition
 *
 * @author tpacht
 */

public interface SurveyRegistrationObj {

  Long getSurveyId();

  void setSurveyId(Long regId);

  Long getSiteId();

  void setSiteId(Long surveySiteId);

  String getPatientId();

  void setPatientId(String patientId);

  String getSurveyDt();

  void setSurveyDt(String dt);

  String getToken();

  void setToken(String tok);

  String getSurveyType();

  void setSurveyType(String type);

  Long getAssessmentId();

  void setAssessmentId(Long assessmentId);

  String getSurveyName();

  void setSurveyName(String surveyName);

  Long getSurveyOrder();

  void setSurveyOrder(Long surveyOrder);

  Integer getNumberCompleted();


  void setNumberCompleted(Integer numberCompleted);

  Integer getNumberPending();

  void setNumberPending(Integer numberPending);

  List<SurveyRegistrationAttributeObj> getAttributes();

  void setAttributes(List<SurveyRegistrationAttributeObj> attributes);
}
