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

package edu.stanford.registry.server;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.utils.ClassCreator;

import org.apache.log4j.Logger;

/**
 * Creates a server-side customizer for the site, from a configured class name.
 */
public class RegistryCustomizerFactory {
  private static Logger logger = Logger.getLogger(ServerUtils.class);

  public static final String CUSTOMIZER_PROPERTY = SiteInfo.REGISTRY_CUSTOMIZER_CLASS;

  private static ClassCreator<RegistryCustomizer> customizerCreator =
      new ClassCreator<RegistryCustomizer>("RegistryCustomizerFactory.create", "RegistryCustomizer", logger, SiteInfo.class)
      .check("edu.stanford.registry.server.PainManagementCustomizer")
      .check("edu.stanford.registry.server.RegistryCustomizerDefault");

  /**
   * Returns a server-side customizer for the site, from a site-specific class name
   * @return the customizer, or if no property is set, or there's an error, RegistryCustomizerDefault
   */
  public RegistryCustomizer create(SiteInfo siteInfo) {
    String className = siteInfo.getProperty(SiteInfo.REGISTRY_CUSTOMIZER_CLASS);
    if (className == null) {
      return new RegistryCustomizerDefault(siteInfo);
    }

    return customizerCreator.createClass(className, siteInfo);
  }

}
