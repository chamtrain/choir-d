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

package edu.stanford.registry.server.service.rest;

import java.io.OutputStream;
import java.util.Map;

import edu.stanford.registry.server.service.AdministrativeServices;

/**
 * Base handler class for a CustomRestlet. Extend this class to
 * perform any actions and write the content of the response for 
 * the CustomRestlet.
 */
public abstract class CustomRestletHandler {

  public enum ResponseType {TEXT_PLAIN, TEXT_CSV, JSON};

  protected AdministrativeServices adminServices;
  protected Map<String,String[]> params;

  /**
   * Get the AdministrativeServices service
   */
  public AdministrativeServices getAdminServices() {
    return adminServices;
  }

  /**
   * Set the AdministrativeServices service
   */
  public void setAdminServices(AdministrativeServices service) {
    this.adminServices = service;
  }

  /**
   * Set the request parameters
   * @return
   */
  public Map<String, String[]> getParams() {
    return params;
  }

  /**
   * Get the request parameters
   */
  public void setParams(Map<String, String[]> params) {
    this.params = params;
  }

  /**
   * Get a parameter value
   */
  public String getParameterValue(String name) {
    String[] values = params.get(name);
    if ((values == null) || (values.length == 0)) {
      return null;
    }
    return values[0];
  }

  /**
   * Get the response type
   */
  public ResponseType getResponseType() {
    return ResponseType.TEXT_PLAIN;
  }

  /**
   * Get the response filename
   */
  public String getFilename() {
    return null;
  }

  /**
   * Write the contents to the response stream. The output
   * stream should be not be closed as this is handled by
   * the Restlet connector.
   */
  public abstract void writeContents(OutputStream outStream);
}
