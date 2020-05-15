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


/**
 * Interface for hiding the lookup of various survey provider implementations.
 */
public interface SurveySystemFactory {
  /**
   * Find a survey system (a context for administering various surveys) based
   * on a system id.
   *
   * @param siteId the id to lookup
   * @return the corresponding system, or null if none could be found
   */
  SurveySystem systemForSiteId(Long siteId);

  Long siteIdFor(String systemName);

  String proxyPropertyKeyFor(String systemName);

  String getPageTitle(Long siteId);

  String getStyleSheetName(Long siteId);

  String[] proxyPropertyKeys();

}
