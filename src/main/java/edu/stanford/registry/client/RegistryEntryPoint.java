/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import edu.stanford.registry.client.RegistryRpcRequestBuilder;
import edu.stanford.registry.shared.Constants;

/**
 * Every GWT service call needs a URL specified using gwtSvcWidget.setServiceEntryPoint(url),
 * and most need to pass the siteId in a request header.  This utility does both.
 * The methods are called setServiceEntryPoint(obj, url [, bldr]) so all entry points can be
 * found by searching for "setServiceEntryPoint".
 */
public class RegistryEntryPoint {
  /**
   * Sets the request URL and adds a request builder that adds the siteId to the request header.
   * @param gwtServiceObject Created with GWT.create(SOMEService.class)
   * @param urlPage the last word of the URL (so the path becomes baseURL+/svc/urlPage)
   */
  public void setServiceEntryPoint(Object gwtServiceObject, String urlPage) {
    setServiceEntryPoint(gwtServiceObject, urlPage, null);
  }

  /**
   * Sets the request URL and a builder (with your custom overrides) that adds the siteId to the request header.
   * @param gwtServiceObject Created with GWT.create(SOMEService.class)
   * @param urlPage the last word of the URL (so the path becomes baseURL+/svc/urlPage)
   * @param bldr Your builder with your custom overrides
   */
  public void setServiceEntryPoint(Object gwtServiceObject, String urlPage, RegistryRpcRequestBuilder bldr) {
    ServiceDefTarget service = (ServiceDefTarget)gwtServiceObject;
    service.setServiceEntryPoint(GWT.getModuleBaseURL() + Constants.SERVLET_PATH + urlPage);
    if (bldr == null) {
      bldr = new RegistryRpcRequestBuilder();
    }
    service.setRpcRequestBuilder(bldr);
  }
}
