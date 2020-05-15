/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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

import com.google.web.bindery.autobean.shared.AutoBean.PropertyName;



/**
 * AutoBean for the data to be displayed on a RadiosetPage.
 */
public interface SurveyReport {
  @PropertyName(value = "Name")
  String getName();
  void setName(String name);

  @PropertyName(value = "LastNameFirst")
  String getLastNameFirst();
  void setLastNameFirst(String lastNameFirst);

  @PropertyName(value = "AGE")
  String getAge();
  void setAge(String age);

  @PropertyName(value = "DocumentControlId")
  String getDocumentControlId();
  void setDocumentControlId(String documentControlId);

  @PropertyName(value = "Assisted")
  String getAssisted();
  void setAssisted(String assisted);

  @PropertyName(value = "DOB")
  String getDob();
  void setDob(String dob);

  @PropertyName(value = "Gender")
  Gender getGender();
  void setGender(Gender gender);

  @PropertyName(value = "ReportTitle")
  String getReportTitle();
  void setReportTitle(String reportTitle);

  @PropertyName(value = "MRN")
  String getMRN();
  void setMRN(String mrn);

  @PropertyName(value = "Inverted")
  String getInverted();
  void setInverted(String inverted);

  @PropertyName(value = "Studies")
  List<SurveyReportStudy> getStudies();
  void setStudies(List<SurveyReportStudy> studies);

}
