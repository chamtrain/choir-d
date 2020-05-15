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

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BuilderServiceAsync {

  void getStudies(AsyncCallback<ArrayList<StudyContent>> callback);

  void addStudyContent(StudyContent studyContent, AsyncCallback<StudyContent> callback);

  void saveStudyContent(StudyContent studyContent, AsyncCallback<Void> callback);

  void disableStudyContent(Long appConfigId, String studyName, AsyncCallback<Void> callback);

  void getStudyAsXml(Long appConfigId, AsyncCallback<String> async);

  void getStudyDocumentation(StudyContent studyContent, String prefix, AsyncCallback<ArrayList<String>> async);

  void updateStudyTitle(StudyContent studyContent, AsyncCallback<Void> callback) throws SurveyException;

  void getTestSurveyPath(AsyncCallback<String> async);

  void isTestFinished(String testStudyName, AsyncCallback<Boolean> async);
}
