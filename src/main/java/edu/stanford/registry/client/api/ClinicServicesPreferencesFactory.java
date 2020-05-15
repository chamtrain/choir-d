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

package edu.stanford.registry.client.api;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

/**
 * Enable use of AutoBean for survey reports .
 * <p/>
 * See http://code.google.com/p/google-web-toolkit/wiki/AutoBean.
 */
public interface ClinicServicesPreferencesFactory extends AutoBeanFactory {
  /**
   * Bump this when making changes client and server need to agree on.
   */
  long compatibilityLevel = 1;

  AutoBean<ClinicServicePreferences> ClinicServicesPreferences();

  AutoBean<FilteredProviders> FilteredProviders();


}
