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
public interface SurveyReportStudy {
  @PropertyName(value = "StudyDescription")
  String getStudyDescription();
  void setStudyDescription(String studyDescription);

  @PropertyName(value = "SurveySystemName")
  String getSurveySystemName();
  void setSurveySystemName(String surveySystemName);

  @PropertyName(value = "SectionHeading")
  String getSectionHeading();
  void setSectionHeading(String sectionHeading);

  @PropertyName(value = "StudyCode")
  String getStudyCode();
  void setStudyCode(String studyCode);

  @PropertyName(value = "SurveySystemId")
  String getSurveySystemId();
  void setSurveySystemId(String surveySystemId);

  @PropertyName(value = "questions")
  List<SurveyReportQuestion> getQuestionList();
  void setQuestionList(List<SurveyReportQuestion> choices);

  @PropertyName(value = "SURVEYSTUDYOBJ")
  SurveyStudyObj  getSurveyStudyObj();
  void setSurveyStudyObj(SurveyStudyObj studyObjs);

}
