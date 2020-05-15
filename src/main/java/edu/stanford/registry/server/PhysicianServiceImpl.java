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

package edu.stanford.registry.server;

import edu.stanford.registry.client.service.PhysicianService;
import edu.stanford.registry.server.service.PhysicianServices;
import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.User;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.DatabaseProvider;

public class PhysicianServiceImpl extends InitRegistryServlet implements PhysicianService {
  /**
   *
   */
  private static final long serialVersionUID = 9094480861420150667L;

  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(PhysicianServiceImpl.class);

  public PhysicianServiceImpl() {
  }

  public PhysicianServiceImpl(User usr, DatabaseProvider databaseProvider, ServerContext context) {
  }



  private PhysicianServices getService() {

    RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
    PhysicianServices registrationService = (PhysicianServices) regRequest.getService();
    return registrationService;
  }


  @Override
  public ArrayList<String> getProcessNames(String patientId) {
    return getService().getProcessNames(patientId);
  }

  @Override
  public String createSurvey(String patientId, String processName) {
    return getService().createSurvey(patientId, processName);
  }

  @Override
  public String getSurveyJson(ApptId apptId) {
    return getService().getSurveyJson(apptId);
  }

  @Override
  public String getPhysicianSurveyPath() {
    return getService().getPhysicianSurveyPath();
  }

  @Override
  public Boolean isFinished(String token) {
    return getService().isFinished(token);
  }

  @Override
  public RandomSetParticipant updateRandomSetParticipant(RandomSetParticipant rsp) {
    return getService().updateRandomSetParticipant(rsp);
  }
}
