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

import edu.stanford.registry.client.service.BuilderService;
import edu.stanford.registry.server.service.BuilderServices;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.StudyContent;
import edu.stanford.registry.shared.survey.SurveyException;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class BuilderServiceImpl extends RemoteServiceServlet implements BuilderService {
  private static final long serialVersionUID = 1L;
  private static Logger logger = Logger.getLogger(BuilderServiceImpl.class);


  @Override
  public ArrayList<StudyContent> getStudies() {

      return getService().getStudies();
  }

  @Override
  public StudyContent addStudyContent(StudyContent studyContent) {
    try {
      return getService().addStudyContent(studyContent);
    } catch (Exception e) {
      logger.error("Error adding study content", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  /**@Override
  public StudyContent getStudyContent(Long appConfigId) {
    return getService().getStudyContent(appConfigId);
  }**/

  @Override
  public void saveStudyContent(StudyContent studyContent) {
    logger.debug("SaveStudyContent: " + studyContent);
    getService().saveStudyContent(studyContent);
  }

  @Override
  public void disableStudyContent(Long appConfigId, String studyName) {
    getService().disableStudyContent(appConfigId, studyName);
  };

  @Override
  public String getStudyAsXml(Long appConfigId) {
    return getService().getStudyAsXml(appConfigId);
  }

  @Override
  public ArrayList<String> getStudyDocumentation(StudyContent studyContent, String prefix) throws SurveyException {
    return getService().getStudyDocumentation(studyContent, prefix);
  }

  @Override
  public void updateStudyTitle(StudyContent studyContent) throws SurveyException {
    getService().updateStudyTitle(studyContent);
  }

  @Override
  public String getTestSurveyPath() {
    return getService().getTestSurveyPath();
  }

  @Override
  public Boolean isTestFinished(String testStudyName) {
    return getService().isTestFinished(testStudyName);
  }

  private BuilderServices getService()  {
     try {
    RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
    return (BuilderServices) regRequest.getService();
     } catch (Exception ex) {
       logger.error("Failed to get BuilderServices ", ex);
       return null;
    }
  }

}
