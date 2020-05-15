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

import edu.stanford.registry.shared.api.SiteObj;
import edu.stanford.survey.client.api.SurveyFactory;

import com.google.web.bindery.autobean.shared.AutoBean;


/**
 * AutoBeans for the API.
 *
 * See http://www.gwtproject.org/doc/latest/DevGuideAutoBeans.html
 *
 * @author tpacht
 */
public interface ApiObjectFactory extends SurveyFactory {

  AutoBean<PatientObj> patientObj();

  AutoBean<PatientAttributeObj> patientAttributeObj();

  AutoBean<SurveyRegistrationObj> surveyRegistrationObj();

  AutoBean<SurveyRegistrationAttributeObj> surveyRegistrationAttributeObj();

  AutoBean<AssessmentObj> assessmentObj();

  AutoBean<ApptObj> apptObj();

  AutoBean<NotificationObj> notificationObj();

  AutoBean<SurveyObj> surveyObj();

  AutoBean<SurveyStudyObj> surveyStudyObj();

  AutoBean<SurveyStudyStepObj> surveyStudyStepObj();

  AutoBean<SiteObj> siteObj();

  AutoBean<PatientDeclineObj> patientDeclineObj();

  AutoBean<PluginPatientStoreObj> pluginPatientStoreObj();

  AutoBean<PluginPatientGetObj> pluginPatientGetObj();

  AutoBean<PluginPatientDataObj> pluginPatientData();

  AutoBean<PluginPatientHistoryDataObj> pluginPatientHistoryData();

}
