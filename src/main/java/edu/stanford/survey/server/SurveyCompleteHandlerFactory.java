/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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
 * completion.
 *
 * The implementation of this is created for a single site only.  If the same class is 
 * used for multiple sites, a different instance is created for each site.
 */
public interface SurveyCompleteHandlerFactory {
  /**
   * Find a class that wants to be notified when surveys complete.
   *
   * @param recipientName name for this handler (see survey_complete_push.recipient_name)
   * @return the corresponding system, or null if none could be found
   */
  SurveyCompleteHandler handlerForName(String recipientName, Long siteId);
}
