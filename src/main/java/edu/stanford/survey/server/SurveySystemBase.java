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

package edu.stanford.survey.server;

import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.client.api.SurveySite;

import java.util.ArrayList;

import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Some default behavior to make creating surveys more convenient.
 */
public abstract class SurveySystemBase implements SurveySystem {
  private SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  @Override
  public String validateStartToken(String token) throws TokenInvalidException {
    // Default allow any token as valid input, and generate a new internal token
    return null;
  }

  @Override
  public Question startWithValidToken(String token, Survey survey) {
    return nextQuestion(null, survey);
  }

  @Override
  public void revalidateToken(String token) throws TokenInvalidException {
    // Don't expire tokens by default
  }

  @Override
  public Question tokenLookupQuestion() {
    // Null ==> default behavior
    return null;
  }

  @Override
  public String tokenLookup(String answerJson) throws TokenInvalidException {
    // This won't be called by default because tokenLookupQuestion() returns null
    throw new TokenInvalidException("tokenLookup() not implemented");
  }

  protected SurveyFactory factory() {
    return factory;
  }

  @Override
  public String getPageTitle() {
    return null;
  }

  @Override
  public String getStyleSheetName() {
    return null;
  }
  
  @Override
  public Question getThankYouPage(String surveyToken) {
    return null;
  }

  @Override
  public ArrayList<SurveySite> getSurveySites() {
    return new ArrayList<>();
  }

  @Override
  public double getProgress(String surveyToken) {
    return 0;
  }
}
