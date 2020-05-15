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


import edu.stanford.survey.client.api.SurveySite;

import java.util.ArrayList;

/**
 * Implementations of this are responsible for coordinating a survey ("authenticating",
 * determining the next question, validating and storing answers, etc.).
 */
public interface SurveySystem {
  /**
   * Called when we are trying to start a survey based on a provided token (usually a
   * parameter from an email link, or similar). Note the "start" means the application
   * has started, and may be called multiple times for a given token if the user starts
   * the survey, stops, and restarts again at a later time or from a different browser.
   *
   * @param token the survey token to look up
   * @return the same token passed or a new token to be used; return null if you want
   * a new token to be generated automatically
   * @throws TokenInvalidException the token passed in cannot be used for some reason
   */
  String validateStartToken(String token) throws TokenInvalidException;

  /**
   * Called when we are trying to ensure a previously validated token is still valid
   * (has not expired, for example). Note the token passed in here will be guaranteed
   * to have been valid at some point, and was returned from validateStartToken() or
   * auto generated, so you don't have to check for illegal or malicious values.
   *
   * @param token the survey token to look up
   * @throws TokenInvalidException the token passed in cannot be used for some reason
   */
  void revalidateToken(String token) throws TokenInvalidException;

  /**
   * Called after validateStartToken(), in the same instance (so you can cache local
   * data retrieved during token validation and use them in this method). The token
   * passed in here will be the one returned by the validation method. If validation
   * fails (throws the exception) this method will not be called.
   *
   * @param token the token returned from validateStartToken() or automatically
   *              generated if that method returned null
   * @param survey provides access to the current survey in cases where this is
   *               a restart (there could be previous questions/answers); this
   *               parameter will never be null
   * @return the first question of the survey, or the current question if this is
   * a restart of a partially completed survey
   */
  Question startWithValidToken(String token, Survey survey);

  /**
   * Advance from answering one question to displaying the next. The answer passed in
   * has already been recorded at this point. This method will not ordinarily be called
   * multiple times with the same answer (except for some edge cases where a failure
   * occurs inside this method, or before the returned question can be saved).
   * <p/>
   * Called after revalidateToken(), in the same instance (so you can cache local
   * data retrieved during token validation and use them in this method).
   *
   * @param answer the answer submitted for the last question; never null; no server-side
   *               validation has been done on the answer
   * @param survey provides access to the current survey (previous questions/answers); this
   *               parameter will never be null
   * @return the next question to be displayed; null to display the default thank you
   * page and terminate the survey
   */
  Question nextQuestion(Answer answer, Survey survey); // Patient characteristics? A/B randomization?

  /**
   * Optionally provide a question to be used when there is no survey token. This allows
   * direct entry of the survey token, or lookup based on other factors.
   *
   * @return the question to be displayed, or null to get the default one
   */
  Question tokenLookupQuestion();

  /**
   * Take the answer from the tokenLookupQuestion() and provide the token. This method
   * will not be called if using the default token lookup.
   *
   * @param answerJson the answer to the token lookup question, never null
   * @return the token to be used for this survey; not null; need not be validated yet,
   * as validateToken() will still be called
   */
  String tokenLookup(String answerJson) throws TokenInvalidException;

  String getPageTitle();
  String getStyleSheetName();
  
  Question getThankYouPage(String surveyToken);
  // Add hook here to provide a function to flatten results to square table and/or do
  // other post-processing?

  ArrayList<SurveySite> getSurveySites();

  double getProgress(String surveyToken);
}
