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

package edu.stanford.registry.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.stanford.registry.shared.AssessmentConfig;
import java.util.Set;

public interface AssessmentConfigServiceAsync {

  void getAssessmentConfig(String clinicName, String assessmentType, AsyncCallback<AssessmentConfig> async);

  void getAllQuestionnaires(String assessmentType, AsyncCallback<Set<String>> async);

  void updateCustomAssessmentConfig(AssessmentConfig assessmentConfig, AsyncCallback<AssessmentConfig> async);
}
