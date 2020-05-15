/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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

/**
 * Interface for hiding the lookup of handlers that will do things upon survey
 * advancement (progress).
 *
 * The implementation of each of these is created for a single site.
 * the constructor is passed a Database and a SiteInfo
 */
public interface SurveyAdvanceHandlerFactory {
  /**
   * Find a class that wants to be notified when surveys advance.
   *
   * @param recipientName name for this handler (see survey_advance_push.recipient_name)
   * @return the corresponding system, or null if none could be found
   */
  SurveyAdvanceHandler handlerForName(String recipientName);

}
