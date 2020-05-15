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

import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Window;

import edu.stanford.registry.shared.Constants;

/**
 * GWT uses a RpcRequestBuilder to specify callbacks and do other work before or after calling the server.
 * We use this wrapper to add the site to any such call, into a header the server will look for.
 *
 * If you override the doFinish method, be sure to call super.doFinish(rb)
 *
 * Plus, it contains a static method createApiUrl(page) to create an API call back to the server.
 * It prepends the API route and appends a parameter with the site urlParam.
 */
public class RegistryRpcRequestBuilder extends RpcRequestBuilder {
  static String currentSiteId;  // used if we're at some kind of default site so no URL parameter is set

  /**
   * Called when the pages are initted, so we can set the siteId on calls to the server
   * even if this is some sort of default site
   * @param siteName  Usually from clientUtils.getSiteName()
   */
  static public void setSiteName(String siteName) {
    if (siteName != null && !siteName.isEmpty()) {
      currentSiteId = siteName;
    }
  }

  static public String getSiteName() {
    if (currentSiteId == null) {
      return Window.Location.getParameter(Constants.SITE_ID);
    }
    return currentSiteId;
  }

  @Override  // same as in RegistryApp.java- add a header with the SiteId
  protected void doFinish(RequestBuilder rb) {
    super.doFinish(rb);
    String siteIdParam = getSiteName();
    if (siteIdParam != null && !siteIdParam.isEmpty()) {
      // should always be set, except maybe for initial ClientService calls
      rb.setHeader(Constants.SITE_ID_HEADER, siteIdParam);
    }
    // Some servers/proxies use this header to determine whether to send a
    // redirect (HTTP 302) or unauthorized (HTTP 401) in response to AJAX
    // requests after session expiration.
    rb.setHeader("X-Requested-With", "XMLHttpRequest");
  }

  /**
   * This creates an API URL suitable for, e.g.:
   * <br> &nbsp; new ClientResource(RegistryRpcRequestBuilder.createApiUrl("jsonload"));
   * <br>or
   * <br> &nbsp; form.setAction(RegistryRpcRequestBuilder.createApiUrl("Appointment"))
   * <p>If you want to add parameters, you should append them, starting with '&'
   * @return the api url path + the passed page string + ?siteId=<site>
   */
  static public String createApiUrl(String page) {
    return "registry/svc/api/" + page + "?siteId=" + getSiteName();
  }
}
