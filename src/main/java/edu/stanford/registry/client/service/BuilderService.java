/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.shared.StudyContent;
import edu.stanford.registry.shared.survey.SurveyException;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;

public interface BuilderService extends RemoteService {

  ArrayList<StudyContent> getStudies();

  StudyContent addStudyContent(StudyContent content);

  void saveStudyContent(StudyContent studyContent);

  void disableStudyContent(Long appConfigId, String studyName);

  String getStudyAsXml(Long appConfigId);

  ArrayList<String> getStudyDocumentation(StudyContent studyContent, String prefix) throws SurveyException;

  void updateStudyTitle(StudyContent studyContent) throws SurveyException;

  String getTestSurveyPath();

  Boolean isTestFinished(String testStudyName);
}
