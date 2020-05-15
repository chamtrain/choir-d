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

package edu.stanford.survey.client.api;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Service for providing questions to the Survey client, and handling the completed ones.
 */
@RemoteServiceRelativePath("SurveyService")
public interface SurveyService extends RemoteService {
  /**
   * The client will call this method first to initialize a survey form.
   *
   * @param systemId    identifies the "system" (software component) that defines the context
   *                    for the survey we want to start; null if unknown (a list of possible
   *                    choices might be shown, or an error page)
   * @param surveyToken unique identifier for a survey; may or may not be necessary
   *                    depending on what is allowed for the survey type
   * @return a JSON representation of an AutoBean that represents a form to be displayed
   * by the client
   */
  String[] startSurvey(String systemId, String surveyToken);

  String[] continueSurvey(String statusJson, String answerJson);

  String[] resumeSurvey(String resumeToken);

  void addPlayerProgress(String statusJson, String targetId, String action, Long milliseconds);

  String[] getSurveySites();
}
