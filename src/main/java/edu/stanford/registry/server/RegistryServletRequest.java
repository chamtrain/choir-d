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

package edu.stanford.registry.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import edu.stanford.registry.server.SiteInfo;

public class RegistryServletRequest extends HttpServletRequestWrapper {
  private static final ThreadLocal<RegistryServletRequest> threadLocal = new ThreadLocal<>();
  
  private final Object svce;
  private final SiteInfo siteInfo;

  /**
   * Wraps a service and a siteInfo into a request.
   * This is called by PatientServiceFilter for a survey request.
   * There should be no downstream users that call its getSiteInfo().
   */
  public RegistryServletRequest(HttpServletRequest req, Object svce) {
    this(null, req, svce);
  }

  /**
   * Wraps a service and a siteInfo into a request.
   * Note: a status restlet call has a null siteInfo.
   */
  public RegistryServletRequest(SiteInfo siteInfo, HttpServletRequest req, Object svce) {
    super(req);
    this.siteInfo = siteInfo;  // null for a survey or status restlet request
    this.svce = svce;
  }

  /**
   * Gets the siteInfo for the site this request was made from.
   * Do NOT call this for a survey or status request- it'll throw an AssertionError.
   */
  public SiteInfo getSiteInfo() {
    if (siteInfo == null) // for a survey call
      throw new AssertionError("Can't get siteInfo (it's null) from a survey or status request ");
    return siteInfo;
  }

  public void addToCurrentThread() {
    threadLocal.set(this);
  }

  public static void removeFromCurrentThread() {
    threadLocal.remove();
  }

  public static RegistryServletRequest forCurrentThread() {
    return threadLocal.get();
  }

  public Object getService() {
    return svce;
  }
}
