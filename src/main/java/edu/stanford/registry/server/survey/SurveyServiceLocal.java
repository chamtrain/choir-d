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

package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.RegistryServletRequest;
import edu.stanford.survey.client.api.SurveyService;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * On the patient-facing web application we use a proxy to send these service
 * calls through to the real service running on a different Tomcat.
 */
public class SurveyServiceLocal extends RemoteServiceServlet implements SurveyService {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(SurveyServiceLocal.class);

  @Override
  public String[] startSurvey(String systemId, String surveyToken) {
    try {
      return getService().startSurvey(systemId, surveyToken);
    } catch (Exception e) {
      log.error("Unexpected error", e);
      return retry();
    }
  }

  @Override
  public String[] continueSurvey(String statusJson, String answerJson) {
    try {
      return getService().continueSurvey(statusJson, answerJson);
    } catch (Exception e) {
      log.error("Unexpected error", e);
      return retry();
    }
  }

  @Override
  public String[] resumeSurvey(String resumeToken) {
    try {
      return getService().resumeSurvey(resumeToken);
    } catch (Exception e) {
      log.error("Unexpected error", e);
      return retry();
    }
  }

  @Override
  public void addPlayerProgress(String statusJson, String targetId, String action, Long milliseconds) {
    try {
      getService().addPlayerProgress(statusJson, targetId, action, milliseconds);
    } catch (Exception e) {
      log.error("Unexpected error", e);
    }
  }

  @Override
  public String[] getSurveySites() {
    return getService().getSurveySites();
  }

  /**
   * Get the internal service to handle the request
   */
  private SurveyService getService() {
    return (SurveyService) RegistryServletRequest.forCurrentThread().getService();
  }

  private String[] retry() {
    return new String[] { "{\"sessionStatus\"=\"retry\"}", "{}" };
  }
 
}
