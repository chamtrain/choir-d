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

import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;

import edu.stanford.survey.client.api.SubmitStatus;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.w3c.dom.Element;

import com.github.susom.database.Database;

/**
 * Implementors of these are cached by site, so they must not cache a database connection,
 * or it'll become stale, and you'll get a "Connection is closed" or "Connection is null" Exception.
 * Thus, a Database is passed to each method.
 */
public interface SurveyServiceIntf {
  SiteInfo getSiteInfo();
  Long getSiteId();

  Study registerAssessment(Database database, String name, String title, String explanation);

  void registerAssessment(Database database, Element questionaire, String patientId,
                          Token tok, User user) throws ServiceUnavailableException;

  NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudy,
                                     SubmitStatus submitStatus, String answerJson);

  ArrayList<SurveyQuestionIntf> getSurvey(Database database, PatientStudy study, User user);

  String getAssessments(Database database, int version) throws Exception;

  ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName);

  void setVersion(int version);

}
