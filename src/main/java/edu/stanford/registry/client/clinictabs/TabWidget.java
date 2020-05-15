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

package edu.stanford.registry.client.clinictabs;

import edu.stanford.registry.client.RegistryEntryPoint;
import edu.stanford.registry.client.RegistryRpcRequestBuilder;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.client.widgets.PageWidget;

public abstract class TabWidget extends PageWidget implements RegistryTabIntf {

  public TabWidget(ClinicUtils clinicUtils) {
    super(clinicUtils);
  }

  public TabWidget(ClientUtils clientUtils) {
    super(clientUtils);
  }

  /**
   * Sets the request URL and the builder that adds the siteId to the request header.
   * @param gwtServiceObject Created with GWT.create(SOMEService.class)
   * @param urlPage the last word of the URL
   */
  protected void setServiceEntryPoint(Object gwtServiceObject, String urlPage) {
    new RegistryEntryPoint().setServiceEntryPoint(gwtServiceObject, urlPage, null);
  }

  /**
   * Sets the request URL and the builder that adds the siteId to the request header.
   * @param gwtServiceObject Created with GWT.create(SOMEService.class)
   * @param urlPage the last word of the URL
   * @param bldr Your builder with your custom overrides
   */
  protected void setServiceEntryPoint(Object gwtServiceObject, String urlPage, RegistryRpcRequestBuilder bldr) {
    new RegistryEntryPoint().setServiceEntryPoint(gwtServiceObject, urlPage, bldr);
  }
}
