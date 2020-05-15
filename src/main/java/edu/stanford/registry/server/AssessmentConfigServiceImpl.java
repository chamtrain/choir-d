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

package edu.stanford.registry.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import edu.stanford.registry.client.service.AssessmentConfigService;
import edu.stanford.registry.server.service.AssessmentConfigServices;
import edu.stanford.registry.shared.AssessmentConfig;
import java.util.Set;

public class AssessmentConfigServiceImpl extends RemoteServiceServlet implements AssessmentConfigService {

  @Override
  public AssessmentConfig getAssessmentConfig(String clinicName, String assessmentType) {
    return getService().getAssessmentConfig(clinicName, assessmentType);
  }

  @Override
  public Set<String> getAllQuestionnaires(String assessmentType) {
    return getService().getAllQuestionnaires(assessmentType);
  }

  @Override
  public AssessmentConfig updateCustomAssessmentConfig(AssessmentConfig assessmentConfig) {
    return getService().updateCustomAssessmentConfig(assessmentConfig);
  }

  private AssessmentConfigServices getService() {
    RegistryServletRequest sReq = (RegistryServletRequest) getThreadLocalRequest();
    return (AssessmentConfigServices) sReq.getService();
  }
}
